<template>
  <div class="shop-reviews">
    <!-- 页面标题 -->
    <div class="page-header">
      <h3>用户评价管理</h3>
    </div>

    <!-- 横向回复状态标签 -->
    <div class="status-tabs">
      <div class="status-tab" :class="{ active: replyFilter === 'all' }" @click="replyFilter = 'all'">
        全部<span v-if="reviews.length" class="tab-count">{{ reviews.length }}</span>
      </div>
      <div class="status-tab" :class="{ active: replyFilter === 'replied' }" @click="replyFilter = 'replied'">
        已回复<span v-if="repliedCount" class="tab-count success">{{ repliedCount }}</span>
      </div>
      <div class="status-tab" :class="{ active: replyFilter === 'unreplied' }" @click="replyFilter = 'unreplied'">
        待回复<span v-if="unrepliedCount" class="tab-count warning">{{ unrepliedCount }}</span>
      </div>
    </div>

    <!-- 评价统计 -->
    <div class="stats-row">
      <div class="stat-item">
        <span class="stat-num">{{ filteredReviews.length }}</span>
        <span class="stat-label">当前显示</span>
      </div>
      <div class="stat-item">
        <span class="stat-num">{{ avgRating }}</span>
        <span class="stat-label">平均评分</span>
      </div>
      <div class="stat-item">
        <span class="stat-num">{{ repliedCount }}</span>
        <span class="stat-label">已回复</span>
      </div>
      <div class="stat-item">
        <span class="stat-num">{{ unrepliedCount }}</span>
        <span class="stat-label">待回复</span>
      </div>
    </div>

    <!-- 评价列表 -->
    <div v-if="loading" class="loading-state">
      <el-icon class="is-loading"><Loading /></el-icon>
      <p>加载中...</p>
    </div>

    <div v-else-if="filteredReviews.length === 0" class="empty-state">
      <div class="empty-icon">⭐</div>
      <p>暂无评价</p>
    </div>

    <div v-else class="reviews-list">
      <div v-for="review in filteredReviews" :key="review.id" class="review-card">
        <div class="review-header">
          <div class="user-info">
            <img :src="normalizeUploadUrl(review.avatar) || defaultAvatar" class="user-avatar" />
            <div>
              <div class="user-name">{{ review.username || '匿名用户' }}</div>
              <div class="review-product">商品：{{ review.productName }}</div>
            </div>
          </div>
          <div class="review-meta">
            <el-rate v-model="review.rating" disabled size="small" />
            <span class="review-time">{{ formatTime(review.createTime) }}</span>
          </div>
        </div>

        <div class="review-body">
          <div class="review-content">{{ review.content }}</div>
        </div>

        <div v-if="review.reply" class="shop-reply-block">
          <div class="reply-header">
            <span class="reply-label">商家回复：</span>
            <span class="reply-time">{{ formatTime(review.replyTime) }}</span>
          </div>
          <div class="reply-content">{{ review.reply }}</div>
        </div>

        <div class="review-actions">
          <div v-if="!review.reply" class="reply-form">
            <el-input
              v-model="replyInputs[review.id]"
              type="textarea"
              :rows="2"
              :placeholder="`回复用户「${review.username || '匿名用户'}」的评价...`"
              maxlength="200"
              show-word-limit
            />
            <el-button type="primary" size="small" style="margin-top: 8px; border-radius: 20px;" @click="submitReply(review)" :loading="replyingId === review.id">
              提交回复
            </el-button>
          </div>
          <el-tag v-else type="success" size="small">已回复</el-tag>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { getShopReviews, replyReview } from '@/api'
import { useUserStore } from '@/stores/user'
import { normalizeUploadUrl } from '@/utils/imageUrl'

const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'

const userStore = useUserStore()

const reviews = ref([])
const loading = ref(false)
const replyingId = ref(null)
const replyInputs = reactive({})
const replyFilter = ref('all')

const filteredReviews = computed(() => {
  if (replyFilter.value === 'replied') return reviews.value.filter(r => !!r.reply)
  if (replyFilter.value === 'unreplied') return reviews.value.filter(r => !r.reply)
  return reviews.value
})

const avgRating = computed(() => {
  if (reviews.value.length === 0) return '0.0'
  const sum = reviews.value.reduce((acc, r) => acc + (r.rating || 0), 0)
  return (sum / reviews.value.length).toFixed(1)
})

const repliedCount = computed(() => reviews.value.filter(r => r.reply).length)
const unrepliedCount = computed(() => reviews.value.filter(r => !r.reply).length)

const loadReviews = async () => {
  const shopId = userStore.userInfo?.id
  if (!shopId) return
  loading.value = true
  try {
    const res = await getShopReviews(shopId)
    if (res.code === 200) { reviews.value = res.data || [] }
  } catch (e) { console.error('加载评价失败', e) }
  finally { loading.value = false }
}

const submitReply = async (review) => {
  const content = replyInputs[review.id]
  if (!content || !content.trim()) { ElMessage.warning('请输入回复内容'); return }
  replyingId.value = review.id
  try {
    const res = await replyReview(review.id, content.trim())
    if (res.code === 200) { review.reply = content.trim(); review.replyTime = new Date().toISOString(); replyInputs[review.id] = ''; ElMessage.success('回复成功') }
    else ElMessage.error(res.message || '回复失败')
  } catch (e) { ElMessage.error('回复失败，请重试') }
  finally { replyingId.value = null }
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

onMounted(() => { loadReviews() })
</script>

<style scoped>
.shop-reviews { background: white; border-radius: 8px; padding: 20px; }
.page-header { margin-bottom: 20px; }
.page-header h3 { margin: 0; font-size: 18px; color: #333; }
.status-tabs { display: flex; gap: 12px; margin-bottom: 20px; padding: 12px 16px; background: #f5f7fa; border-radius: 10px; overflow-x: auto; }
.status-tab { display: flex; align-items: center; gap: 8px; padding: 10px 20px; border-radius: 20px; background: white; cursor: pointer; transition: all 0.3s; white-space: nowrap; font-size: 14px; color: #666; box-shadow: 0 2px 6px rgba(0,0,0,0.05); }
.status-tab:hover { background: #e3e5e7; }
.status-tab.active { background: linear-gradient(145deg, #4A7C59, #3d6b4a); color: white; }
.tab-count { padding: 2px 8px; border-radius: 10px; font-size: 12px; background: #f0f0f0; color: #666; }
.status-tab.active .tab-count { background: rgba(255,255,255,0.3); color: white; }
.tab-count.success { background: #e8f5e9; color: #4A7C59; }
.tab-count.warning { background: #fff3e0; color: #faad14; }
.stats-row { display: flex; gap: 20px; margin-bottom: 24px; padding: 16px; background: #f5f7fa; border-radius: 12px; }
.stat-item { flex: 1; text-align: center; }
.stat-num { display: block; font-size: 24px; font-weight: bold; color: #4A7C59; margin-bottom: 4px; }
.stat-label { font-size: 13px; color: #999; }
.reviews-list { display: flex; flex-direction: column; gap: 16px; }
.review-card { border: 1px solid #eee; border-radius: 12px; padding: 20px; transition: box-shadow 0.2s; }
.review-card:hover { box-shadow: 0 4px 16px rgba(0,0,0,0.06); }
.review-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 14px; }
.user-info { display: flex; align-items: center; gap: 12px; }
.user-avatar { width: 44px; height: 44px; border-radius: 50%; object-fit: cover; }
.user-name { font-weight: 600; font-size: 15px; color: #333; margin-bottom: 4px; }
.review-product { font-size: 13px; color: #999; }
.review-meta { display: flex; flex-direction: column; align-items: flex-end; gap: 4px; }
.review-time { font-size: 12px; color: #bbb; }
.review-body { margin-bottom: 14px; }
.review-content { font-size: 14px; color: #666; line-height: 1.7; }
.shop-reply-block { background: #f0f9eb; border-left: 3px solid #67c23a; border-radius: 0 8px 8px 0; padding: 12px 16px; margin-bottom: 14px; }
.reply-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
.reply-label { font-size: 13px; font-weight: 600; color: #67c23a; }
.reply-time { font-size: 12px; color: #999; }
.reply-content { font-size: 14px; color: #555; line-height: 1.6; }
.review-actions { display: flex; justify-content: flex-end; }
.loading-state { text-align: center; padding: 60px 20px; color: #999; }
.loading-state .el-icon { font-size: 32px; margin-bottom: 12px; }
.empty-state { text-align: center; padding: 80px 20px; }
.empty-icon { font-size: 64px; margin-bottom: 20px; }
.empty-state p { font-size: 16px; color: #999; }
</style>
