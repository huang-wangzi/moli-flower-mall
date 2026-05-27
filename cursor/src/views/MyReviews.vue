<template>
  <!-- 
    我的评价页面
    功能：显示当前用户的所有评价记录
    数据来源：从后端API获取，按userId隔离
  -->
  <div class="reviews-page">
    <h1 class="page-title">我的评价</h1>

    <!-- 评价列表 -->
    <div v-if="reviews.length > 0" class="reviews-list">
      <div v-for="review in reviews" :key="review.id" class="neumorphic-card review-item">
        <!-- 商品信息 -->
        <div class="review-product">
          <img :src="getProductImage(review)" class="product-image" />
          <div class="product-info">
            <h4>{{ review.productName || '商品' }}</h4>
            <p>{{ review.specs || '' }}</p>
          </div>
        </div>

        <!-- 评价内容 -->
        <div class="review-content">
          <div class="review-header">
            <el-rate v-model="review.rating" disabled />
            <span class="review-time">{{ formatTime(review.createTime) }}</span>
          </div>
          <p class="review-text">{{ review.content }}</p>

          <!-- 评价图片 -->
          <div v-if="review.images && review.images.length > 0" class="review-images">
            <img
              v-for="(img, idx) in parseImages(review.images)"
              :key="idx"
              :src="normalizeUploadUrl(img) || 'https://cube.elemecdn.com/6/94/4d3ea53c084bad6931a56d5158a48jpeg.jpeg'"
              @error="(e) => e.target.src = 'https://cube.elemecdn.com/6/94/4d3ea53c084bad6931a56d5158a48jpeg.jpeg'"
              @click="previewImage(img)"
            />
          </div>

          <!-- 商家回复 -->
          <div v-if="review.reply" class="review-reply">
            <div class="reply-header">商家回复：</div>
            <div class="reply-content">{{ review.reply }}</div>
            <div class="reply-time">{{ formatTime(review.replyTime) }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="empty-state">
      <div class="empty-state-icon">⭐</div>
      <p class="empty-state-text">暂无评价记录</p>
      <p class="empty-state-hint">购买商品后可在此处查看您的评价</p>
    </div>

    <!-- 图片预览弹窗 -->
    <el-dialog v-model="previewVisible" title="图片预览" width="600px">
      <img :src="previewUrl" style="width: 100%;" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getUserReviews } from '@/api'
import { useUserStore } from '@/stores/user'
import { normalizeUploadUrl } from '@/utils/imageUrl'

// ==================== 数据定义 ====================

const userStore = useUserStore()
const reviews = ref([])
const loading = ref(false)

// 图片预览相关
const previewVisible = ref(false)
const previewUrl = ref('')

// ==================== API调用 ====================

/**
 * 从后端API加载当前用户的评价数据
 */
const loadReviews = async () => {
  const userId = userStore.userInfo?.id
  if (!userId) {
    reviews.value = []
    return
  }

  loading.value = true
  try {
    const res = await getUserReviews(userId)
    
    if (res.code === 200 && res.data) {
      reviews.value = res.data.map(item => ({
        id: item.id,
        productId: item.productId,
        productName: item.productName || '商品',
        productImage: item.productImage || getDefaultImage(),
        specs: item.specs || '',
        rating: item.rating || 5,
        content: item.content || '',
        images: item.images || '[]',
        createTime: item.createTime || new Date().toISOString(),
        reply: item.reply || '',
        replyTime: item.replyTime || null
      }))
    } else {
      reviews.value = []
    }
  } catch (error) {
    console.error('加载评价失败:', error)
    reviews.value = []
  } finally {
    loading.value = false
  }
}

// ==================== 工具函数 ====================

/**
 * 解析图片字段（可能是JSON字符串或数组）
 * @param {string|array} images - 图片数据
 * @returns {array} 图片URL数组
 */
const parseImages = (images) => {
  if (!images) return []
  if (Array.isArray(images)) return images
  try {
    return JSON.parse(images)
  } catch {
    return []
  }
}

/**
 * 获取商品图片，确保不会显示undefined
 * @param {object} review - 评价对象
 * @returns {string} 商品图片URL
 */
const getProductImage = (review) => {
  if (review.productImage && review.productImage !== 'undefined' && review.productImage !== 'null') {
    // productImage 可能是 JSON 字符串 ["url"] 或直接是 URL
    try {
      const parsed = JSON.parse(review.productImage)
      if (Array.isArray(parsed) && parsed[0]) return normalizeUploadUrl(parsed[0]) || getDefaultImage()
      if (typeof parsed === 'string') return normalizeUploadUrl(parsed) || getDefaultImage()
    } catch {
      // 不是 JSON，直接用
      return normalizeUploadUrl(review.productImage) || getDefaultImage()
    }
  }
  return getDefaultImage()
}

/**
 * 获取默认商品图片
 * @returns {string} 默认图片URL
 */
const getDefaultImage = () => {
  return 'https://cube.elemecdn.com/6/94/4d3ea53c084bad6931a56d5158a48jpeg.jpeg'
}

/**
 * 格式化时间显示
 * @param {string} time - ISO时间字符串
 * @returns {string} 格式化后的时间
 */
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

/**
 * 预览图片
 * @param {string} url - 图片URL
 */
const previewImage = (url) => {
  previewUrl.value = url
  previewVisible.value = true
}

// ==================== 生命周期 ====================

// 页面加载时获取评价数据
onMounted(() => {
  loadReviews()
})
</script>

<style scoped>
.reviews-page {
  padding: 20px;
}

/* 评价列表容器 */
.reviews-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 单个评价卡片 */
.review-item {
  padding: 20px;
}

/* 商品信息区域 */
.review-product {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #eee;
}

/* 商品图片 */
.product-image {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  object-fit: cover;
}

/* 商品信息文字 */
.product-info h4 {
  font-size: 15px;
  margin-bottom: 6px;
}

.product-info p {
  font-size: 13px;
  color: #999;
}

/* 评价头部 */
.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.review-time {
  font-size: 13px;
  color: #999;
}

/* 评价正文 */
.review-text {
  color: #666;
  line-height: 1.6;
  margin-bottom: 12px;
}

/* 评价图片网格 */
.review-images {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.review-images img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.2s;
}

.review-images img:hover {
  transform: scale(1.05);
}

/* 商家回复样式 */
.review-reply {
  margin-top: 16px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
  border-left: 3px solid #4A7C59;
}

.reply-header {
  font-weight: bold;
  color: #4A7C59;
  margin-bottom: 8px;
}

.reply-content {
  color: #666;
  line-height: 1.6;
}

.reply-time {
  font-size: 12px;
  color: #999;
  margin-top: 8px;
  text-align: right;
}

/* 空状态 */
.empty-state {
  padding: 80px 20px;
  text-align: center;
}

.empty-state-icon {
  font-size: 64px;
  margin-bottom: 20px;
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
</style>
