<template>
  <div class="shop-products">
    <!-- 横向状态标签筛选 -->
    <div class="status-tabs">
      <div class="status-tab" :class="{ active: statusFilter === '' }" @click="statusFilter = ''; loadProducts()">
        全部<span v-if="getStatusCount(null)" class="tab-count">{{ getStatusCount(null) }}</span>
      </div>
      <div class="status-tab" :class="{ active: statusFilter === 1 }" @click="statusFilter = 1; loadProducts()">
        在售<span v-if="getStatusCount(1)" class="tab-count success">{{ getStatusCount(1) }}</span>
      </div>
      <div class="status-tab" :class="{ active: statusFilter === 0 }" @click="statusFilter = 0; loadProducts()">
        待审核<span v-if="getStatusCount(0)" class="tab-count warning">{{ getStatusCount(0) }}</span>
      </div>
      <div class="status-tab" :class="{ active: statusFilter === 2 }" @click="statusFilter = 2; loadProducts()">
        已拒绝<span v-if="getStatusCount(2)" class="tab-count danger">{{ getStatusCount(2) }}</span>
      </div>
    </div>

    <!-- 搜索和操作栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-input v-model="searchKeyword" placeholder="搜索商品名称" style="width: 200px;" clearable @keyup.enter="loadProducts" />
        <el-button type="primary" style="margin-left: 10px;" @click="loadProducts">搜索</el-button>
      </div>
      <div class="toolbar-right">
        <el-button type="success" @click="$router.push('/shop/product/add')">+ 发布商品</el-button>
      </div>
    </div>

    <el-table :data="filteredProducts" border style="width: 100%; margin-top: 16px;" v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column label="商品图片" width="80">
        <template #default="{ row }">
          <img :src="getImage(row)" style="width: 50px; height: 50px; object-fit: cover; border-radius: 4px;" />
        </template>
      </el-table-column>
      <el-table-column prop="name" label="商品名称" min-width="200" />
      <el-table-column label="价格" width="100"><template #default="{ row }">¥{{ row.price }}</template></el-table-column>
      <el-table-column prop="stock" label="库存" width="80" />
      <el-table-column prop="sales" label="销量" width="80" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.status === 0" type="warning">待审核</el-tag>
          <el-tag v-else-if="row.status === 1" type="success">已上架</el-tag>
          <el-tag v-else type="danger">已拒绝</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="editProduct(row)" v-if="row.status === 2">编辑</el-button>
          <el-button :type="row.status === 1 ? 'warning' : 'success'" size="small" @click="toggleStatus(row)">
            {{ row.status === 1 ? '下架' : '上架' }}
          </el-button>
          <el-button type="danger" size="small" @click="deleteProduct(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :total="total"
      :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next"
      style="margin-top: 20px; justify-content: flex-end;"
      @size-change="loadProducts"
      @current-change="loadProducts"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useShopStore } from '@/stores/shop'
import { useUserStore } from '@/stores/user'
import { getShopProducts } from '@/api'
import { normalizeUploadUrl } from '@/utils/imageUrl'

const router = useRouter()
const shopStore = useShopStore()
const userStore = useUserStore()

const loading = ref(false)
const searchKeyword = ref('')
const statusFilter = ref('')
const allProducts = ref([])
const currentPage = ref(1)
const pageSize = ref(20)

const products = computed(() => shopStore.products)
const total = computed(() => products.value.length)

const getImage = (product) => {
  const fallback = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
  if (!product) return fallback
  if (product.image) return normalizeUploadUrl(product.image) || fallback
  if (product.images) {
    try {
      const arr = JSON.parse(product.images)
      const u = Array.isArray(arr) && arr[0] ? arr[0] : null
      return u ? (normalizeUploadUrl(u) || fallback) : fallback
    } catch {
      return fallback
    }
  }
  return fallback
}

const getStatusCount = (status) => {
  if (status === null) return allProducts.value.length
  return allProducts.value.filter(p => p.status === status).length
}

const filteredProducts = computed(() => {
  let result = allProducts.value
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(p => p.name && p.name.toLowerCase().includes(keyword))
  }
  if (statusFilter.value !== '') {
    const status = parseInt(statusFilter.value)
    result = result.filter(p => p.status === status)
  }
  return result
})

const loadProducts = () => {
  loading.value = true
  const shopId = userStore.userInfo?.id
  if (!shopId) {
    ElMessage.warning('请先登录商家账号')
    loading.value = false
    return
  }
  getShopProducts(shopId).then(res => {
    loading.value = false
    if (res.code === 200) {
      allProducts.value = res.data || []
      shopStore.products = allProducts.value
    } else {
      ElMessage.error(res.message || '获取商品列表失败')
    }
  }).catch(err => {
    loading.value = false
    ElMessage.error('获取商品列表失败')
  })
}

const editProduct = (row) => router.push(`/shop/product/edit/${row.id}`)
const toggleStatus = (row) => { shopStore.toggleProductStatus(row.id); ElMessage.success(row.status === 1 ? '商品已下架' : '商品已上架') }
const deleteProduct = (row) => {
  ElMessageBox.confirm('确定要删除该商品吗？', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    .then(() => { shopStore.deleteProduct(row.id); ElMessage.success('删除成功') })
    .catch(() => {})
}

onMounted(() => {
  loadProducts()
})
</script>

<style scoped>
.shop-products {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 横向状态标签筛选 */
.status-tabs {
  display: flex;
  gap: 12px;
  margin-bottom: 0;
  padding: 16px 20px;
  background: #fafbfc;
  border-radius: 12px;
  flex-wrap: wrap;
}

.status-tab {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border-radius: 24px;
  background: white;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
  color: #666;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  border: 1px solid transparent;
  white-space: nowrap;
}

.status-tab:hover {
  background: #f0f0f0;
}

.status-tab.active {
  background: linear-gradient(135deg, #4A7C59, #3d6b4a);
  color: white;
  border-color: #4A7C59;
}

.tab-count {
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  background: #f0f0f0;
  color: #666;
}

.status-tab.active .tab-count {
  background: rgba(255, 255, 255, 0.25);
  color: white;
}

.tab-count.success { background: #e8f5e9; color: #4A7C59; }
.tab-count.warning { background: #fff3e0; color: #faad14; }
.tab-count.danger { background: #ffe0e0; color: #e74c3c; }

/* 工具栏 */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
  flex-wrap: wrap;
  gap: 12px;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 表格容器 */
.table-container {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  margin-top: 20px;
}
</style>
