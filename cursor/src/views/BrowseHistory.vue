<template>
  <!-- 
    浏览历史页面
    功能：显示当前用户的浏览历史记录
    数据来源：从后端API获取，按userId隔离
    规则：新用户默认为空，只有点击商品详情才加入历史
  -->
  <div class="history-page">
    <h1 class="page-title">浏览历史</h1>

    <!-- 历史列表 -->
    <div v-if="historyList.length > 0" class="history-grid">
      <div
        v-for="item in historyList"
        :key="item.id"
        class="neumorphic-card history-item"
        @click="viewProduct(item)"
      >
        <div class="item-image">
          <img :src="getProductImage(item)" :alt="item.name" />
        </div>
        <div class="item-info">
          <h4>{{ item.name || '商品名称' }}</h4>
          <p class="item-price">¥{{ formatPrice(item.price) }}</p>
          <p class="item-time">{{ formatTime(item.browseTime) }}</p>
        </div>
        <!-- 删除按钮 -->
        <button class="delete-btn" @click.stop="removeHistory(item.id)">×</button>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="empty-state">
      <div class="empty-state-icon">📜</div>
      <p class="empty-state-text">暂无浏览记录</p>
      <p class="empty-state-hint">浏览商品后会自动记录到这里</p>
    </div>

    <!-- 清空全部按钮 -->
    <div v-if="historyList.length > 0" class="clear-all">
      <el-button @click="clearAllHistory" type="danger" plain size="small">
        清空全部历史
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { normalizeUploadUrl } from '@/utils/imageUrl'

// ==================== 数据定义 ====================

const router = useRouter()
const userStore = useUserStore()

// 浏览历史列表
const historyList = ref([])

// ==================== API调用 ====================

/**
 * 加载浏览历史数据
 */
const loadHistory = () => {
  // 从userStore获取浏览历史
  historyList.value = userStore.browseHistory || []
}

/**
 * 删除单条浏览历史
 */
const removeHistory = (id) => {
  ElMessageBox.confirm('确定要删除这条浏览记录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    const index = historyList.value.findIndex(item => item.id === id)
    if (index > -1) {
      historyList.value.splice(index, 1)
      userStore.browseHistory = [...historyList.value]
      ElMessage.success('已删除')
    }
  }).catch(() => {})
}

/**
 * 清空全部浏览历史
 */
const clearAllHistory = () => {
  ElMessageBox.confirm('确定要清空全部浏览记录吗？此操作不可恢复！', '警告', {
    confirmButtonText: '确定清空',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    historyList.value = []
    userStore.browseHistory = []
    ElMessage.success('已清空全部历史')
  }).catch(() => {})
}

// ==================== 工具函数 ====================

/**
 * 解析图片字段
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
 * 获取商品图片
 */
const getProductImage = (item) => {
  // 优先使用 image 字段
  if (item.image) return normalizeUploadUrl(item.image) || 'https://cube.elemecdn.com/6/94/4d3ea53c084bad6931a56d5158a48jpeg.jpeg'
  // 其次尝试解析 images 字段
  const images = parseImages(item.images)
  if (images.length > 0 && images[0]) {
    return normalizeUploadUrl(images[0]) || 'https://cube.elemecdn.com/6/94/4d3ea53c084bad6931a56d5158a48jpeg.jpeg'
  }
  return 'https://cube.elemecdn.com/6/94/4d3ea53c084bad6931a56d5158a48jpeg.jpeg'
}

/**
 * 格式化价格
 */
const formatPrice = (price) => {
  if (!price && price !== 0) return '0.00'
  return parseFloat(price).toFixed(2)
}

/**
 * 格式化浏览时间
 */
const formatTime = (time) => {
  if (!time) return ''
  
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60 * 1000) return '刚刚浏览'
  if (diff < 60 * 60 * 1000) {
    const minutes = Math.floor(diff / (60 * 1000))
    return `${minutes}分钟前`
  }
  if (diff < 24 * 60 * 60 * 1000) {
    const hours = Math.floor(diff / (60 * 60 * 1000))
    return `${hours}小时前`
  }
  if (diff < 7 * 24 * 60 * 60 * 1000) {
    const days = Math.floor(diff / (24 * 60 * 60 * 1000))
    return `${days}天前`
  }
  
  return date.toLocaleDateString('zh-CN', {
    month: '2-digit',
    day: '2-digit'
  })
}

/**
 * 查看商品详情
 */
const viewProduct = (item) => {
  router.push(`/product/${item.id}`)
}

// ==================== 生命周期 ====================

onMounted(() => {
  loadHistory()
})

// 监听 userStore.browseHistory 变化
watch(() => userStore.browseHistory, (newHistory) => {
  historyList.value = newHistory || []
}, { deep: true })
</script>

<style scoped>
.history-page {
  padding: 20px;
}

/* 历史列表网格布局 */
.history-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

/* 单个历史记录卡片 */
.history-item {
  position: relative;
  cursor: pointer;
  padding: 0;
  overflow: hidden;
  transition: transform 0.3s;
}

.history-item:hover {
  transform: translateY(-5px);
}

/* 商品图片区域 */
.item-image {
  height: 180px;
  overflow: hidden;
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.history-item:hover .item-image img {
  transform: scale(1.1);
}

/* 商品信息 */
.item-info {
  padding: 16px;
}

.item-info h4 {
  font-size: 14px;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-price {
  color: #e74c3c;
  font-weight: bold;
  margin-bottom: 4px;
}

.item-time {
  font-size: 12px;
  color: #999;
}

/* 删除按钮 */
.delete-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.5);
  color: white;
  border: none;
  cursor: pointer;
  font-size: 16px;
  line-height: 1;
  opacity: 0;
  transition: opacity 0.3s;
}

.history-item:hover .delete-btn {
  opacity: 1;
}

.delete-btn:hover {
  background: rgba(231, 76, 60, 0.8);
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

/* 清空全部按钮 */
.clear-all {
  margin-top: 30px;
  text-align: center;
}

/* 响应式布局 */
@media (max-width: 1024px) {
  .history-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .history-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
