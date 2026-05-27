<template>
  <div class="after-sales-page">
    <h1 class="page-title">售后记录</h1>

    <div v-if="afterSalesList.length > 0" class="after-sales-list">
      <div v-for="item in afterSalesList" :key="item.id" class="neumorphic-card after-sales-item">
        <div class="item-header">
          <span class="order-no">订单号：{{ item.orderNo || ('#' + item.orderId) }}</span>
          <span class="status" :class="getStatusClass(item.status)">{{ getStatusText(item.status) }}</span>
        </div>
        <div class="item-content">
          <div class="refund-info">
            <div class="info-row">
              <span>售后类型：</span>
              <span>{{ item.type === 'refund' ? '仅退款' : '退货退款' }}</span>
            </div>
            <div class="info-row">
              <span>退款原因：</span>
              <span>{{ item.reason }}</span>
            </div>
            <div class="info-row">
              <span>退款金额：</span>
              <span class="refund-amount">¥{{ Number(item.amount || 0).toFixed(2) }}</span>
            </div>
            <div class="info-row">
              <span>申请时间：</span>
              <span>{{ formatTime(item.createTime || item.applyTime) }}</span>
            </div>
            <div v-if="item.description" class="info-row">
              <span>详细说明：</span>
              <span>{{ item.description }}</span>
            </div>
            <div v-if="item.status === 0 || item.status === 1" class="escalate-row">
              <button type="button" class="escalate-btn" @click="openEscalate(item)">申请客服介入</button>
              <button type="button" class="cancel-btn" @click="handleCancel(item)">取消售后</button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="empty-state">
      <div class="empty-state-icon">🔄</div>
      <p class="empty-state-text">暂无售后记录</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getUserRefunds, createComplaint, cancelRefund } from '@/api'

const userStore = useUserStore()
const afterSalesList = ref([])
const loading = ref(false)

// 加载售后记录
const loadAfterSales = async () => {
  const userId = userStore.userInfo?.id
  if (!userId) {
    afterSalesList.value = []
    return
  }
  
  loading.value = true
  try {
    const res = await getUserRefunds(userId)
    if (res.code === 200) {
      afterSalesList.value = res.data || []
    } else {
      afterSalesList.value = []
    }
  } catch (e) {
    console.error('加载售后记录失败', e)
    afterSalesList.value = []
  } finally {
    loading.value = false
  }
}

// 状态文本
const getStatusText = (status) => {
  const texts = { 0: '待处理', 1: '处理中', 2: '已同意', 3: '已拒绝', 4: '已取消', 5: '已完成' }
  return texts[status] || '待处理'
}

// 状态样式
const getStatusClass = (status) => {
  const classes = { 0: 'pending', 1: 'processing', 2: 'approved', 3: 'rejected', 4: 'cancelled', 5: 'completed' }
  return classes[status] || 'pending'
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

const openEscalate = async (item) => {
  if (!item.shopId) {
    ElMessage.warning('缺少商家信息，无法提交')
    return
  }
  try {
    await ElMessageBox.confirm(
      '将把本条售后同步至管理员后台「交易投诉」，由平台协助处理。是否继续？',
      '申请客服介入',
      { type: 'info', confirmButtonText: '提交', cancelButtonText: '取消' }
    )
    const res = await createComplaint({
      shopId: item.shopId,
      orderId: item.orderId,
      refundId: item.id,
      sourceType: 1,
      reason: '售后客服介入：' + (item.reason || '售后纠纷'),
      description: item.description || ''
    })
    if (res.code === 200) {
      ElMessage.success('已提交，管理员将尽快处理')
    } else {
      ElMessage.error(res.message || '提交失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('提交失败')
    }
  }
}

const handleCancel = async (item) => {
  try {
    await ElMessageBox.confirm(
      '确定要取消该售后申请吗？取消后商家和平台将不再处理。',
      '确认取消',
      { type: 'warning', confirmButtonText: '确定取消', cancelButtonText: '点错了' }
    )
    const res = await cancelRefund(item.id)
    if (res.code === 200) {
      ElMessage.success('已取消售后申请')
      loadAfterSales()
    } else {
      ElMessage.error(res.message || '取消失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('操作异常')
    }
  }
}

onMounted(() => {
  loadAfterSales()
})
</script>

<style scoped>
.after-sales-page {
  padding: 20px;
}

.after-sales-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.after-sales-item {
  padding: 0;
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: #f5f5f5;
  border-bottom: 1px solid #eee;
}

.order-no {
  font-size: 13px;
  color: #666;
}

.status {
  font-size: 14px;
  font-weight: 600;
}

.status.pending { color: #f39c12; }
.status.processing { color: #409eff; }
.status.approved { color: #27ae60; }
.status.rejected { color: #e74c3c; }
.status.completed { color: #4A7C59; }

.item-content {
  padding: 20px;
}

.product-info {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}

.product-image {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  object-fit: cover;
}

.product-detail h4 {
  font-size: 15px;
  margin-bottom: 6px;
}

.product-detail p {
  font-size: 13px;
  color: #999;
}

.refund-info {
  padding-top: 16px;
  border-top: 1px solid #eee;
}

.info-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  font-size: 14px;
}

.info-row span:first-child {
  color: #666;
}

.refund-amount {
  color: #e74c3c;
  font-weight: bold;
}

.empty-state {
  padding: 80px 20px;
}

.escalate-row {
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px dashed #e0e0e0;
  display: flex;
  gap: 12px;
}

.escalate-btn {
  padding: 8px 16px;
  background: #f0f9eb;
  color: #67c23a;
  border: 1px solid #c2e7b0;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.3s;
}

.escalate-btn:hover {
  background: #67c23a;
  color: white;
}

.cancel-btn {
  padding: 8px 16px;
  background: #fef0f0;
  color: #f56c6c;
  border: 1px solid #fbc4c4;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.3s;
}

.cancel-btn:hover {
  background: #f56c6c;
  color: white;
}
</style>
