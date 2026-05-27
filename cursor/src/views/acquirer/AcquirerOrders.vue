<template>
  <div class="acquirer-orders">
    <div class="page-header">
      <h2>收购订单</h2>
      <p class="page-subtitle">查看所有收购订单的进度和状态</p>
    </div>

    <!-- 状态筛选 -->
    <div class="filter-bar">
      <el-radio-group v-model="statusFilter" @change="handleStatusChange">
        <el-radio-button value="">全部</el-radio-button>
        <el-radio-button value="0">待审核</el-radio-button>
        <el-radio-button value="1">已同意</el-radio-button>
        <el-radio-button value="3">已完成</el-radio-button>
        <el-radio-button value="4">已拒绝</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <el-icon class="is-loading"><Loading /></el-icon>
      <p>加载中...</p>
    </div>

    <!-- 订单列表 -->
    <div v-else-if="orders.length > 0" class="orders-list">
      <div v-for="order in orders" :key="order.id" class="order-card neumorphic-card" @click="showOrderDetail(order)">
        <div class="order-header">
          <div class="farmer-info">
            <span class="farmer-name">{{ order.farmerName || order.userNickname || '花农' }}</span>
            <span class="apply-time">{{ formatDateTime(order.createdAt) }}</span>
          </div>
          <span class="status-badge" :class="getStatusClass(order.status)">
            {{ getStatusText(order.status) }}
          </span>
        </div>

        <div class="demand-info">
          <span class="demand-title">{{ order.demandTitle || '收购需求' }}</span>
          <span class="demand-price">{{ getPriceDisplay(order) }}</span>
        </div>

        <div class="order-details">
          <div class="detail-item">
            <span class="label">申请数量</span>
            <span class="value">{{ order.quantity }} 斤</span>
          </div>
          <div class="detail-item" v-if="order.actualQuantity">
            <span class="label">实际斤数</span>
            <span class="value">{{ order.actualQuantity }} 斤</span>
          </div>
          <div class="detail-item" v-if="order.actualPrice">
            <span class="label">成交单价</span>
            <span class="value">¥{{ order.actualPrice }}/斤</span>
          </div>
          <div class="detail-item" v-if="order.totalAmount">
            <span class="label">总金额</span>
            <span class="value amount">¥{{ order.totalAmount }}</span>
          </div>
        </div>

        <!-- 照片预览（如果有） -->
        <div class="order-photos" v-if="order.photoUrls">
          <span class="photo-label">茉莉花照片 <span class="click-hint">(点击卡片查看详情)</span></span>
          <div class="photo-list">
            <el-image
              v-for="(url, index) in parsePhotos(order.photoUrls)"
              :key="index"
              :src="normalizeUrl(url)"
              :preview-src-list="parsePhotos(order.photoUrls).map(normalizeUrl)"
              fit="cover"
              class="photo-item"
            />
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="order-actions" @click.stop>
          <el-button type="primary" size="small" v-if="order.status === 0" @click="handleAgree(order)">
            同意供货
          </el-button>
          <el-button type="danger" size="small" v-if="order.status === 0" @click="handleReject(order)">
            拒绝
          </el-button>
          <el-button type="success" size="small" v-if="order.status === 1" @click="handleComplete(order)">
            确认收货
          </el-button>
          <el-button size="small" @click="showOrderDetail(order)">
            查看详情
          </el-button>
          <el-button size="small" v-if="order.status === 4 || order.status === 5" @click="handleDelete(order)">
            删除记录
          </el-button>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="empty-state">
      <div class="empty-icon">📋</div>
      <p class="empty-text">暂无收购订单</p>
      <p class="empty-hint">发布收购需求后，花农申请供货将显示在这里</p>
    </div>

    <!-- 分页 -->
    <div class="pagination-wrapper" v-if="total > pageSize">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="loadOrders"
      />
    </div>

    <!-- 完成交付对话框 -->
    <el-dialog v-model="completeDialogVisible" title="确认收货" width="400px">
      <el-form :model="completeForm" label-width="100px">
        <el-form-item label="申请数量">
          <span>{{ currentOrder?.quantity }} 斤</span>
        </el-form-item>
        <el-form-item label="实际斤数" required>
          <el-input-number v-model="completeForm.actualQuantity" :min="0" :step="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="成交单价" required>
          <el-input-number v-model="completeForm.actualPrice" :min="0" :precision="2" style="width: 100%" />
          <span class="form-tip">元/斤</span>
        </el-form-item>
        <el-form-item label="应付金额">
          <span class="total-amount">¥{{ calculateTotal() }}</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="completeDialogVisible = false">取消</el-button>
        <el-button type="success" @click="submitComplete" :loading="submitting">确认完成</el-button>
      </template>
    </el-dialog>

    <!-- 订单详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="订单详情" width="600px">
      <div v-if="currentOrder" class="order-detail">
        <!-- 花农信息 -->
        <div class="detail-section">
          <h4 class="section-title">花农信息</h4>
          <div class="info-grid">
            <div class="info-item">
              <span class="label">姓名</span>
              <span class="value">{{ currentOrder.farmerName || currentOrder.userNickname || '花农' }}</span>
            </div>
            <div class="info-item" v-if="currentOrder.username">
              <span class="label">用户名</span>
              <span class="value">{{ currentOrder.username }}</span>
            </div>
            <div class="info-item" v-if="currentOrder.phone">
              <span class="label">联系电话</span>
              <span class="value">{{ currentOrder.phone }}</span>
            </div>
            <div class="info-item" v-if="currentOrder.contactPhone">
              <span class="label">申请填写的电话</span>
              <span class="value">{{ currentOrder.contactPhone }}</span>
            </div>
          </div>
        </div>

        <!-- 收购需求信息 -->
        <div class="detail-section">
          <h4 class="section-title">收购需求</h4>
          <div class="info-grid">
            <div class="info-item full">
              <span class="label">需求标题</span>
              <span class="value">{{ currentOrder.demandTitle || '收购需求' }}</span>
            </div>
            <div class="info-item">
              <span class="label">收购商</span>
              <span class="value">{{ currentOrder.shopName || '收购商' }}</span>
            </div>
            <div class="info-item">
              <span class="label">市场地址</span>
              <span class="value">{{ currentOrder.marketAddress || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="label">价格区间</span>
              <span class="value price">{{ getPriceDisplay(currentOrder) }}</span>
            </div>
          </div>
        </div>

        <!-- 订单信息 -->
        <div class="detail-section">
          <h4 class="section-title">订单信息</h4>
          <div class="info-grid">
            <div class="info-item">
              <span class="label">订单状态</span>
              <span class="value">
                <span class="status-badge" :class="getStatusClass(currentOrder.status)">
                  {{ getStatusText(currentOrder.status) }}
                </span>
              </span>
            </div>
            <div class="info-item">
              <span class="label">申请时间</span>
              <span class="value">{{ formatDateTime(currentOrder.createdAt) }}</span>
            </div>
            <div class="info-item">
              <span class="label">申请数量</span>
              <span class="value">{{ currentOrder.quantity }} 斤</span>
            </div>
            <div class="info-item" v-if="currentOrder.actualQuantity">
              <span class="label">实际斤数</span>
              <span class="value">{{ currentOrder.actualQuantity }} 斤</span>
            </div>
            <div class="info-item" v-if="currentOrder.actualPrice">
              <span class="label">成交单价</span>
              <span class="value">¥{{ currentOrder.actualPrice }}/斤</span>
            </div>
            <div class="info-item" v-if="currentOrder.totalAmount">
              <span class="label">总金额</span>
              <span class="value amount">¥{{ currentOrder.totalAmount }}</span>
            </div>
            <div class="info-item full" v-if="currentOrder.remark">
              <span class="label">备注说明</span>
              <span class="value">{{ currentOrder.remark }}</span>
            </div>
            <div class="info-item full" v-if="currentOrder.rejectReason">
              <span class="label">拒绝原因</span>
              <span class="value danger">{{ currentOrder.rejectReason }}</span>
            </div>
          </div>
        </div>

        <!-- 茉莉花照片 -->
        <div class="detail-section" v-if="currentOrder.photoUrls">
          <h4 class="section-title">茉莉花照片</h4>
          <div class="photo-gallery">
            <el-image
              v-for="(url, index) in parsePhotos(currentOrder.photoUrls)"
              :key="index"
              :src="normalizeUrl(url)"
              :preview-src-list="parsePhotos(currentOrder.photoUrls).map(normalizeUrl)"
              fit="cover"
              class="gallery-item"
            />
          </div>
          <p class="photo-hint">点击图片可放大查看</p>
        </div>

        <!-- 无照片提示 -->
        <div class="detail-section" v-else>
          <h4 class="section-title">茉莉花照片</h4>
          <p class="no-photo">该订单未上传茉莉花照片</p>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button type="primary" v-if="currentOrder?.status === 0" @click="handleAgreeFromDetail">
          同意供货
        </el-button>
        <el-button type="success" v-if="currentOrder?.status === 1" @click="handleCompleteFromDetail">
          确认收货
        </el-button>
        <el-button type="danger" v-if="currentOrder?.status === 0" @click="handleReject(currentOrder)">
          拒绝
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import {
  getMerchantOrders,
  agreeApplication,
  rejectApplication,
  completeDelivery
} from '@/api/acquisition'
import { normalizeUploadUrl } from '@/utils/imageUrl'

const loading = ref(false)
const orders = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const statusFilter = ref('')

const completeDialogVisible = ref(false)
const currentOrder = ref(null)
const detailDialogVisible = ref(false)
const completeForm = ref({
  actualQuantity: null,
  actualPrice: null
})
const submitting = ref(false)

const loadOrders = async () => {
  loading.value = true
  try {
    const res = await getMerchantOrders(currentPage.value, pageSize.value, statusFilter.value ? Number(statusFilter.value) : null)
    if (res.code === 200 && res.data) {
      let records = res.data.records || res.data || []
      orders.value = records
      total.value = res.data.total || records.length
      
      // 调试：检查第一条记录的 photoUrls
      if (records.length > 0) {
        console.log('[收购订单] 第一条订单数据:', records[0])
        console.log('[收购订单] photoUrls 字段:', records[0].photoUrls)
        console.log('[收购订单] jasminePhotos 字段:', records[0].jasminePhotos)
      }
    } else {
      orders.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('加载订单失败', error)
    orders.value = []
  } finally {
    loading.value = false
  }
}

const handleStatusChange = () => {
  currentPage.value = 1
  loadOrders()
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  const d = new Date(dateTime)
  return `${d.getMonth() + 1}月${d.getDate()}日 ${d.getHours()}:${String(d.getMinutes()).padStart(2, '0')}`
}

const getStatusClass = (status) => {
  switch (status) {
    case 0: return 'status-pending'
    case 1: return 'status-agreed'
    case 2: return 'status-delivering'
    case 3: return 'status-completed'
    case 4: return 'status-rejected'
    case 5: return 'status-cancelled'
    default: return ''
  }
}

const getStatusText = (status) => {
  switch (status) {
    case 0: return '待审核'
    case 1: return '已同意'
    case 2: return '交付中'
    case 3: return '已完成'
    case 4: return '已拒绝'
    case 5: return '已取消'
    default: return '未知'
  }
}

const parsePhotos = (photoUrls) => {
  if (!photoUrls) return []
  try {
    const photos = typeof photoUrls === 'string' ? JSON.parse(photoUrls) : photoUrls
    return Array.isArray(photos) ? photos : []
  } catch {
    return []
  }
}

const normalizeUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return normalizeUploadUrl(url)
}

/**
 * 获取价格显示
 */
const getPriceDisplay = (order) => {
  if (order.priceRange) return order.priceRange
  if (order.priceMin && order.priceMax) {
    return `¥${order.priceMin}-${order.priceMax}/斤`
  }
  if (order.actualPrice) {
    return `¥${order.actualPrice}/斤`
  }
  return '价格待定'
}

const handleAgree = async (order) => {
  try {
    await ElMessageBox.confirm(`确定同意 ${order.farmerName || '该花农'} 的供货申请吗？`, '同意供货', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    const res = await agreeApplication(order.id)
    if (res.code === 200) {
      ElMessage.success('已同意')
      loadOrders()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

const handleReject = async (order) => {
  try {
    await ElMessageBox.prompt('请输入拒绝原因（选填）', '拒绝申请', {
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    }).then(({ value }) => {
      doReject(order.id, value)
    }).catch(() => {})
  } catch {}
}

const doReject = async (id, reason) => {
  try {
    const res = await rejectApplication(id, reason || '收购商已拒绝')
    if (res.code === 200) {
      ElMessage.success('已拒绝')
      loadOrders()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

const handleComplete = (order) => {
  currentOrder.value = order
  completeForm.value = {
    actualQuantity: Number(order.quantity),
    actualPrice: order.demandPrice || 5
  }
  completeDialogVisible.value = true
}

const calculateTotal = () => {
  const qty = completeForm.value.actualQuantity || 0
  const price = completeForm.value.actualPrice || 0
  return (qty * price).toFixed(2)
}

const submitComplete = async () => {
  if (!completeForm.value.actualQuantity || !completeForm.value.actualPrice) {
    ElMessage.warning('请填写完整信息')
    return
  }
  
  submitting.value = true
  try {
    const res = await completeDelivery(currentOrder.value.id, {
      actualQuantity: completeForm.value.actualQuantity,
      actualPrice: completeForm.value.actualPrice
    })
    if (res.code === 200) {
      ElMessage.success('订单已完成！')
      completeDialogVisible.value = false
      loadOrders()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

const handleDelete = (order) => {
  // 前端删除，无需调用后端
  const index = orders.value.findIndex(o => o.id === order.id)
  if (index > -1) {
    orders.value.splice(index, 1)
    ElMessage.success('已删除')
  }
}

// ==================== 订单详情相关 ====================

const showOrderDetail = (order) => {
  currentOrder.value = order
  detailDialogVisible.value = true
}

const handleAgreeFromDetail = () => {
  if (currentOrder.value) {
    detailDialogVisible.value = false
    handleAgree(currentOrder.value)
  }
}

const handleCompleteFromDetail = () => {
  if (currentOrder.value) {
    detailDialogVisible.value = false
    handleComplete(currentOrder.value)
  }
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.acquirer-orders {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 页面标题 */
.page-header {
  margin-bottom: 4px;
}

.page-header h2 {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 8px;
}

.page-subtitle {
  color: #888;
  font-size: 14px;
}

/* 筛选栏 */
.filter-bar {
  background: white;
  border-radius: 12px;
  padding: 16px 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

/* 加载状态 */
.loading-state {
  text-align: center;
  padding: 60px 20px;
  color: #999;
  background: white;
  border-radius: 12px;
}

.loading-state .el-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

/* 订单列表 */
.orders-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.order-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: all 0.3s;
}

.order-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.farmer-name {
  font-weight: 600;
  color: #333;
  font-size: 15px;
}

.apply-time {
  font-size: 12px;
  color: #999;
  margin-left: 12px;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  color: white;
}

.status-pending { background: #ff9800; }
.status-agreed { background: #2196f3; }
.status-delivering { background: #9c27b0; }
.status-completed { background: #4caf50; }
.status-rejected { background: #f44336; }
.status-cancelled { background: #9e9e9e; }

.demand-info {
  display: flex;
  justify-content: space-between;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 12px;
}

.demand-title {
  color: #333;
  font-weight: 500;
}

.demand-price {
  color: #e74c3c;
  font-weight: 500;
}

.order-details {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 12px;
}

.detail-item {
  text-align: center;
}

.detail-item .label {
  display: block;
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}

.detail-item .value {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.detail-item .value.amount {
  color: #4caf50;
  font-size: 16px;
}

.order-photos {
  margin-bottom: 12px;
}

.photo-label {
  display: block;
  font-size: 12px;
  color: #999;
  margin-bottom: 8px;
}

.click-hint {
  font-size: 11px;
  color: #4A7C59;
}

.photo-list {
  display: flex;
  gap: 8px;
}

.photo-item {
  width: 60px;
  height: 60px;
  border-radius: 4px;
  cursor: pointer;
}

.order-actions {
  display: flex;
  gap: 8px;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  background: white;
  border-radius: 12px;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.empty-text {
  font-size: 16px;
  color: #666;
  margin-bottom: 8px;
}

.empty-hint {
  font-size: 14px;
  color: #999;
}

/* 分页 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

/* 表单 */
.form-tip {
  display: block;
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.total-amount {
  font-size: 24px;
  font-weight: bold;
  color: #4caf50;
}

/* 订单详情对话框样式 */
.order-detail {
  padding: 0 10px;
}

.detail-section {
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px dashed #eee;
}

.detail-section:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #4A7C59;
  margin-bottom: 12px;
  padding-left: 8px;
  border-left: 3px solid #4A7C59;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.info-item {
  display: flex;
  flex-direction: column;
}

.info-item.full {
  grid-column: 1 / -1;
}

.info-item .label {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}

.info-item .value {
  font-size: 14px;
  color: #333;
}

.info-item .value.price {
  color: #e74c3c;
  font-weight: 500;
}

.info-item .value.amount {
  color: #4caf50;
  font-weight: bold;
  font-size: 16px;
}

.info-item .value.danger {
  color: #f44336;
}

.photo-gallery {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.gallery-item {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.2s;
}

.gallery-item:hover {
  transform: scale(1.05);
}

.photo-hint {
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}

.no-photo {
  color: #999;
  font-size: 14px;
  padding: 20px;
  text-align: center;
  background: #f5f5f5;
  border-radius: 8px;
}

@media (max-width: 768px) {
  .order-details {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
