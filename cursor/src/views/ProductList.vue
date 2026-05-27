<template>
  <div class="product-list-page">
    <div class="page-header">
      <h1 class="page-title">商品列表</h1>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索商品名称..."
        clearable
        @keyup.enter="handleSearch"
        style="max-width: 400px;"
      >
        <template #append>
          <el-button @click="handleSearch">搜索</el-button>
        </template>
      </el-input>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <!-- 分类筛选 -->
      <div class="filter-group">
        <span class="filter-label">分类：</span>
        <div class="filter-options">
          <span
            class="filter-option"
            :class="{ active: !selectedCategory }"
            @click="selectCategory(null)"
          >
            全部
          </span>
          <span
            v-for="cat in categories"
            :key="cat.id"
            class="filter-option"
            :class="{ active: selectedCategory === cat.id }"
            @click="selectCategory(cat.id)"
          >
            {{ cat.name }}
          </span>
        </div>
      </div>

      <!-- 排序 -->
      <div class="filter-group">
        <span class="filter-label">排序：</span>
        <div class="filter-options">
          <span
            class="filter-option"
            :class="{ active: sortBy === 'default' }"
            @click="sortBy = 'default'; loadProducts()"
          >
            默认
          </span>
          <span
            class="filter-option"
            :class="{ active: sortBy === 'time' }"
            @click="sortBy = 'time'; loadProducts()"
          >
            最新
          </span>
          <span
            class="filter-option"
            :class="{ active: sortBy === 'sales' }"
            @click="sortBy = 'sales'; loadProducts()"
          >
            销量
          </span>
          <span
            class="filter-option"
            :class="{ active: sortBy === 'price-asc' }"
            @click="sortBy = 'price-asc'; loadProducts()"
          >
            价格↑
          </span>
          <span
            class="filter-option"
            :class="{ active: sortBy === 'price-desc' }"
            @click="sortBy = 'price-desc'; loadProducts()"
          >
            价格↓
          </span>
        </div>
      </div>
    </div>

    <!-- 商品列表 -->
    <div v-if="productList.length > 0" class="product-grid">
      <div
        v-for="product in productList"
        :key="product.id"
        class="neumorphic-card product-card"
        @click="$router.push(`/product/${product.id}`)"
      >
        <div class="product-image">
          <img :src="getProductImage(product)" :alt="product.name" @error="handleImageError" />
          <span class="product-tag">{{ getCategoryName(product.categoryId) }}</span>
          <div v-if="product.originalPrice && product.originalPrice > product.price" class="discount-tag">
            {{ Math.round((1 - product.price / product.originalPrice) * 100) }}% OFF
          </div>
        </div>
        <div class="product-info">
          <h4 class="product-name">{{ product.name }}</h4>
          <p class="product-shop">{{ product.shopName || '横州茉莉花商城' }}</p>
          <div class="product-bottom">
            <div class="product-price">
              <span class="price-symbol">¥</span>
              <span class="price-value">{{ formatPrice(product.price) }}</span>
              <span v-if="product.originalPrice && product.originalPrice > product.price" class="original-price">
                ¥{{ formatPrice(product.originalPrice) }}
              </span>
            </div>
            <span class="product-sales">销量 {{ product.sales || 0 }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="empty-state">
      <div class="empty-state-icon">🔍</div>
      <p class="empty-state-text">暂无相关商品</p>
      <p class="empty-state-hint">试试其他关键词或分类</p>
    </div>

    <!-- 分页 -->
    <div v-if="total > 0" class="pagination-wrapper">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[12, 24, 48]"
        layout="total, sizes, prev, pager, next"
        @size-change="loadProducts"
        @current-change="loadProducts"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getProductList } from '@/api'
import { normalizeUploadUrl } from '@/utils/imageUrl'

const route = useRoute()

// 默认图片
const defaultImage = 'https://cube.elemecdn.com/6/94/4d3ea53c084bad6931a56d5158a48jpeg.jpeg'

// 获取商品图片
const getProductImage = (product) => {
  if (product.images) {
    try {
      const images = typeof product.images === 'string'
        ? JSON.parse(product.images)
        : product.images
      if (Array.isArray(images) && images.length > 0) {
        return normalizeUploadUrl(images[0]) || defaultImage
      }
    } catch (e) {}
  }
  return defaultImage
}

// 处理图片加载失败
const handleImageError = (event) => {
  event.target.src = defaultImage
}

// 格式化价格
const formatPrice = (price) => {
  if (!price) return '0.00'
  return typeof price === 'number' ? price.toFixed(2) : parseFloat(price).toFixed(2)
}

// 分类数据 - 与商品发布页面保持一致
const categories = ref([
  { id: 1, name: '茉莉花茶' },
  { id: 2, name: '茉莉花盆栽' },
  { id: 3, name: '茉莉花制品' },
  { id: 4, name: '茉莉花苗' }
])

// 获取分类名称
const getCategoryName = (categoryId) => {
  if (!categoryId) return '综合'
  const cat = categories.value.find(c => c.id === categoryId)
  return cat ? cat.name : '综合'
}

// 当前筛选条件
const selectedCategory = ref(null)
const sortBy = ref('default')
const searchKeyword = ref('')

// 分页
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)

// 商品列表
const productList = ref([])

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  loadProducts()
}

// 选择分类
const selectCategory = (categoryId) => {
  selectedCategory.value = categoryId
  currentPage.value = 1
  loadProducts()
}

// 加载商品列表
const loadProducts = async () => {
  try {
    const res = await getProductList({
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      keyword: searchKeyword.value || undefined,
      categoryId: selectedCategory.value || undefined
    })

    if (res.code === 200) {
      const data = res.data
      productList.value = data.records || []
      total.value = data.total || 0

      // 前端排序
      sortProducts()
    } else {
      ElMessage.error(res.message || '加载商品失败')
    }
  } catch (error) {
    console.error('加载商品失败', error)
    ElMessage.error('加载商品失败')
  }
}

// 前端排序
const sortProducts = () => {
  if (sortBy.value === 'time') {
    // 最新排序 - 已有后端排序
  } else if (sortBy.value === 'sales') {
    productList.value.sort((a, b) => (b.sales || 0) - (a.sales || 0))
  } else if (sortBy.value === 'price-asc') {
    productList.value.sort((a, b) => parseFloat(a.price) - parseFloat(b.price))
  } else if (sortBy.value === 'price-desc') {
    productList.value.sort((a, b) => parseFloat(b.price) - parseFloat(a.price))
  }
}

// 监听路由参数变化
watch(
  () => route.query,
  (query) => {
    if (query.search) {
      searchKeyword.value = query.search
    }
    if (query.category) {
      selectedCategory.value = parseInt(query.category)
    }
    loadProducts()
  },
  { immediate: false }
)

onMounted(() => {
  // 从路由获取初始参数
  if (route.query.search) {
    searchKeyword.value = route.query.search
  }
  if (route.query.category) {
    selectedCategory.value = parseInt(route.query.category)
  }
  loadProducts()
})
</script>

<style scoped>
.product-list-page {
  padding: 20px;
}

/* 搜索栏 */
.search-bar {
  margin-bottom: 20px;
}

/* 筛选栏 */
.filter-bar {
  background: #e3e5e7;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 30px;
  box-shadow: inset 4px 4px 8px #b6b9ba, inset -4px -4px 8px #fafafd;
}

.filter-group {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.filter-group:last-child {
  margin-bottom: 0;
}

.filter-label {
  font-size: 14px;
  color: #666;
  margin-right: 16px;
  min-width: 50px;
}

.filter-options {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.filter-option {
  padding: 8px 16px;
  border-radius: 20px;
  background: #e3e5e7;
  box-shadow: 3px 3px 6px #b6b9ba, -3px -3px 6px #fafafd;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.filter-option:hover {
  transform: translateY(-2px);
}

.filter-option.active {
  background: linear-gradient(145deg, #4A7C59, #3d6b4a);
  color: white;
  box-shadow: none;
}

/* 商品网格 */
.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

.product-card {
  cursor: pointer;
  padding: 0;
  overflow: hidden;
  transition: all 0.3s;
}

.product-card:hover {
  transform: translateY(-8px);
}

.product-image {
  position: relative;
  height: 220px;
  overflow: hidden;
  border-radius: 20px 20px 0 0;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.product-card:hover .product-image img {
  transform: scale(1.1);
}

.product-tag {
  position: absolute;
  top: 12px;
  left: 12px;
  background: rgba(74, 124, 89, 0.9);
  color: white;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
}

.discount-tag {
  position: absolute;
  top: 12px;
  right: 12px;
  background: #e74c3c;
  color: white;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: bold;
}

.product-info {
  padding: 16px;
}

.product-name {
  font-size: 15px;
  color: #333;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-shop {
  font-size: 12px;
  color: #999;
  margin-bottom: 12px;
}

.product-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.product-price {
  display: flex;
  align-items: baseline;
}

.product-price .price-symbol {
  font-size: 14px;
  color: #e74c3c;
}

.product-price .price-value {
  font-size: 22px;
  font-weight: bold;
  color: #e74c3c;
}

.original-price {
  font-size: 12px;
  color: #999;
  text-decoration: line-through;
  margin-left: 8px;
}

.product-sales {
  font-size: 12px;
  color: #999;
}

/* 空状态 */
.empty-state {
  padding: 80px 20px;
  text-align: center;
}

.empty-state-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.empty-state-text {
  font-size: 18px;
  color: #666;
  margin-bottom: 8px;
}

.empty-state-hint {
  font-size: 14px;
  color: #999;
}

/* 分页 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

/* 响应式 */
@media (max-width: 1200px) {
  .product-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 900px) {
  .product-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 15px;
  }
  
  .search-bar {
    flex-direction: column;
    gap: 10px;
  }
  
  .search-bar .el-input {
    max-width: 100% !important;
    width: 100%;
  }
  
  .filter-options {
    flex-wrap: wrap;
  }
}

@media (max-width: 600px) {
  .product-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 10px;
  }
  
  .product-image {
    height: 130px;
  }
  
  .product-info {
    padding: 10px;
  }
  
  .product-name {
    font-size: 13px;
    white-space: normal;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    height: auto;
  }
  
  .product-price .price-value {
    font-size: 16px;
  }
  
  .filter-bar {
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
    padding-bottom: 10px;
  }
  
  .filter-group {
    min-width: max-content;
  }
  
  .page-title {
    font-size: 18px;
  }
}</style>
