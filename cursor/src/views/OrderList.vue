<template>
  <div class="order-list-page">
    <h1 class="page-title">我的订单</h1>

    <!-- 订单状态筛选 -->
    <div class="order-tabs">
      <div
        class="order-tab"
        :class="{ active: status === 'all' }"
        @click="status = 'all'"
      >
        全部
      </div>
      <div
        class="order-tab"
        :class="{ active: status === 'pending' }"
        @click="status = 'pending'"
      >
        待付款
        <span v-if="pendingPaymentCount > 0" class="tab-badge">{{ pendingPaymentCount }}</span>
      </div>
      <div
        class="order-tab"
        :class="{ active: status === 'shipped' }"
        @click="status = 'shipped'"
      >
        待收货
        <span v-if="pendingReceiveCount > 0" class="tab-badge">{{ pendingReceiveCount }}</span>
      </div>
      <div
        class="order-tab"
        :class="{ active: status === 'completed' }"
        @click="status = 'completed'"
      >
        已完成
      </div>
      <div
        class="order-tab"
        :class="{ active: status === 'refunding' }"
        @click="status = 'refunding'"
      >
        售后中
        <span v-if="refundingCount > 0" class="tab-badge">{{ refundingCount }}</span>
      </div>
      <div
        class="order-tab"
        :class="{ active: status === 'refunded' }"
        @click="status = 'refunded'"
      >
        已退款
        <span v-if="refundedCount > 0" class="tab-badge">{{ refundedCount }}</span>
      </div>
    </div>

    <!-- 订单列表 -->
    <div class="order-list">
      <div v-for="order in filteredOrders" :key="order.id" class="neumorphic-card order-card">
        <!-- 订单头部 -->
        <div class="order-header">
          <span class="order-no">订单号：{{ order.orderNo }}</span>
          <span class="order-status" :class="getStatusClass(order.status)">{{ order.statusText }}</span>
        </div>

        <!-- 订单商品 -->
        <div class="order-items">
          <div v-for="item in order.items" :key="item.id" class="order-item" @click="router.push(`/product/${item.productId}`)">
            <img :src="normalizeImage(item.productImage)" class="item-image" />
            <div class="item-info">
              <h4>{{ item.productName }}</h4>
              <p>{{ item.specs || '无规格' }}</p>
            </div>
            <div class="item-price">
              <span>¥{{ Number(item.price || 0).toFixed(2) }}</span>
              <span class="item-qty">x{{ item.quantity }}</span>
            </div>
          </div>
        </div>

        <!-- 订单底部 -->
        <div class="order-footer">
          <div class="order-total">
            <span class="total-label">合计：</span>
            <span class="total-value">¥{{ Number(order.actualAmount || 0).toFixed(2) }}</span>
          </div>
          <div class="order-actions">
            <!-- 待付款：去付款 -->
            <button v-if="order.status === 0" class="action-btn primary" @click="openPayDialog(order)">
              去付款
            </button>
            <!-- 待付款：取消订单 -->
            <button v-if="order.status === 0" class="action-btn" @click="handleCancel(order.id)">
              取消订单
            </button>
            <!-- 待发货/待收货/已完成：申请售后 -->
            <!-- 只有在未申请过售后，或者售后已取消(4)的情况下才显示申请按钮 -->
            <!-- 如果订单状态已经是 5(退款中)、6(已退款)、7(退款驳回)，则不再显示申请按钮 -->
            <button v-if="order.status === 1 || order.status === 2 || order.status === 3" class="action-btn" @click="openRefundDialog(order)">
              申请售后
            </button>
            <!-- 退款中：查看售后 -->
            <button v-if="order.status === 5" class="action-btn" @click="router.push('/after-sales')">
              查看售后
            </button>
            <!-- 待收货：确认收货 -->
            <button v-if="order.status === 2" class="action-btn primary" @click="handleReceive(order.id)">
              确认收货
            </button>
            <!-- 已完成：再次购买 -->
            <button v-if="order.status === 3" class="action-btn" @click="router.push(`/product/${order.items[0]?.productId}`)">
              再次购买
            </button>
            <!-- 查看详情 -->
            <button class="action-btn" @click="router.push(`/order/${order.id}`)">
              查看详情
            </button>
          </div>
        </div>
      </div>

      <div v-if="loading" class="loading-state">
        <el-icon class="is-loading"><Loading /></el-icon>
        <p>加载中...</p>
      </div>

      <div v-else-if="filteredOrders.length === 0" class="empty-state">
        <div class="empty-state-icon">📋</div>
        <p class="empty-state-text">暂无订单</p>
      </div>
    </div>

    <!-- 付款弹窗 -->
    <el-dialog v-model="showPayDialog" title="确认付款" width="600px" :close-on-click-modal="false" destroy-on-close>
      <div class="pay-dialog-content" v-if="currentOrder">
        <!-- 商品信息 -->
        <div class="pay-section">
          <h4 class="pay-section-title">商品信息</h4>
          <div class="pay-products">
            <div v-for="item in currentOrder.items" :key="item.id" class="pay-product-item">
              <img :src="normalizeImage(item.productImage)" class="pay-product-img" />
              <div class="pay-product-info">
                <div class="pay-product-name">{{ item.productName }}</div>
                <div class="pay-product-specs">{{ item.specs || '无规格' }}</div>
              </div>
              <div class="pay-product-price">
                <span>¥{{ Number(item.price || 0).toFixed(2) }}</span>
                <span class="pay-product-qty">x{{ item.quantity }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 地址选择 -->
        <div class="pay-section">
          <h4 class="pay-section-title">收货地址</h4>
          <div class="address-list">
            <div
              v-for="addr in addresses"
              :key="addr.id"
              class="address-option"
              :class="{ selected: selectedAddressId === addr.id }"
              @click="selectAddress(addr)"
            >
              <div class="address-option-content">
                <div class="address-option-header">
                  <span class="address-option-name">{{ addr.name }}</span>
                  <span class="address-option-phone">{{ addr.phone }}</span>
                  <span v-if="addr.isDefault" class="address-option-default">默认</span>
                </div>
                <div class="address-option-detail">{{ addr.province }} {{ addr.city }} {{ addr.district }} {{ addr.detail }}</div>
              </div>
              <div class="address-option-check" v-if="selectedAddressId === addr.id">✓</div>
            </div>
            <div v-if="addresses.length === 0" class="address-empty">
              暂无保存的地址，请填写下方信息
            </div>
          </div>

          <!-- 新增收货信息 -->
          <div class="address-form">
            <div class="address-form-row">
              <el-input v-model="payForm.receiverName" placeholder="收货人姓名" />
              <el-input v-model="payForm.receiverPhone" placeholder="手机号码" maxlength="11" />
            </div>
            <el-input v-model="payForm.receiverAddress" type="textarea" :rows="2" placeholder="详细地址（省市区+详细地址）" />
          </div>
        </div>

        <!-- 订单金额 -->
        <div class="pay-section">
          <h4 class="pay-section-title">订单金额</h4>
          <div class="pay-amount-list">
            <div class="pay-amount-item">
              <span>商品金额</span>
              <span>¥{{ Number(currentOrder.totalAmount || 0).toFixed(2) }}</span>
            </div>
            <div class="pay-amount-item">
              <span>运费</span>
              <span>¥{{ Number(currentOrder.freight || 0).toFixed(2) }}</span>
            </div>
            <div class="pay-amount-item total">
              <span>实付金额</span>
              <span class="pay-total-value">¥{{ Number(currentOrder.actualAmount || 0).toFixed(2) }}</span>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="showPayDialog = false">取消</el-button>
        <el-button type="primary" @click="handlePay" :loading="paying">
          确认付款
        </el-button>
      </template>
    </el-dialog>

    <!-- 售后申请弹窗 -->
    <el-dialog v-model="showRefundDialog" title="申请售后" width="500px" destroy-on-close>
      <div class="refund-dialog-content" v-if="currentRefundOrder">
        <!-- 订单信息 -->
        <div class="refund-section">
          <h4 class="refund-section-title">订单信息</h4>
          <div class="refund-order-info">
            <span>订单号：{{ currentRefundOrder.orderNo }}</span>
            <span>退款金额：<strong class="refund-amount">¥{{ Number(currentRefundOrder.actualAmount || 0).toFixed(2) }}</strong></span>
          </div>
        </div>

        <!-- 售后类型 -->
        <div class="refund-section">
          <h4 class="refund-section-title">售后类型</h4>
          <el-radio-group v-model="refundForm.type" class="refund-type-group">
            <el-radio value="refund">仅退款</el-radio>
            <el-radio value="return">退货退款</el-radio>
          </el-radio-group>
        </div>

        <!-- 退款原因 -->
        <div class="refund-section">
          <h4 class="refund-section-title">退款原因</h4>
          <el-select v-model="refundForm.reason" placeholder="请选择退款原因" style="width: 100%;">
            <el-option v-for="reason in refundReasons" :key="reason" :label="reason" :value="reason" />
          </el-select>
        </div>

        <!-- 详细说明 -->
        <div class="refund-section">
          <h4 class="refund-section-title">详细说明</h4>
          <el-input
            v-model="refundForm.description"
            type="textarea"
            :rows="3"
            placeholder="请详细描述您遇到的问题..."
            maxlength="200"
            show-word-limit
          />
        </div>

        <div class="refund-section">
          <el-checkbox v-model="needEscalate">
            同步申请平台客服介入（客诉将出现在管理员后台「交易投诉」）
          </el-checkbox>
        </div>
      </div>
      <template #footer>
        <el-button @click="showRefundDialog = false">取消</el-button>
        <el-button type="primary" @click="submitRefund">
          提交申请
        </el-button>
      </template>
    </el-dialog>

    <!-- 确认收货后弹出评价弹窗 -->
    <el-dialog v-model="showReviewDialog" title="商品评价" width="500px" :close-on-click-modal="false" destroy-on-close>
      <div v-if="pendingReviewOrder" class="review-receive-content">
        <div class="review-order-info">
          <p>感谢您完成交易！请对本次购物体验进行评价~</p>
          <div v-if="pendingReviewOrder.items?.length" class="review-product-name">
            商品：{{ pendingReviewOrder.items[0].productName }}
          </div>
        </div>
        <el-form :model="reviewForm" label-width="60px" style="margin-top: 16px;">
          <el-form-item label="评分">
            <el-rate v-model="reviewForm.rating" />
          </el-form-item>
          <el-form-item label="评价">
            <el-input
              v-model="reviewForm.content"
              type="textarea"
              :rows="4"
              placeholder="分享您的购物体验..."
              maxlength="200"
              show-word-limit
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="skipReview">稍后评价</el-button>
        <el-button type="primary" @click="submitReviewFromReceive">提交评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { useOrderStore } from '@/stores/order'
import { useUserStore } from '@/stores/user'
import { payOrder as apiPayOrder, createRefund as apiCreateRefund, createComplaint as apiCreateComplaint, getAddressList, addReview } from '@/api'
import { normalizeUploadUrl } from '@/utils/imageUrl'

const normalizeImage = (url) => normalizeUploadUrl(url) || url || 'https://cube.elemecdn.com/6/94/4d3ea53c084bad6931a56d5158a48jpeg.jpeg'

const route = useRoute()
const router = useRouter()
const orderStore = useOrderStore()
const userStore = useUserStore()

const status = ref('all')
const loading = ref(false)

// 付款弹窗
const showPayDialog = ref(false)
const paying = ref(false)
const currentOrder = ref(null)
const addresses = ref([])
const selectedAddressId = ref(null)
const payForm = reactive({
  receiverName: '',
  receiverPhone: '',
  receiverAddress: ''
})

const STATUS_TEXT = {
  0: '待付款',
  1: '待发货',
  2: '待收货',
  3: '已完成',
  4: '已取消',
  5: '退款中',
  6: '已退款',
  7: '已拒绝'
}

const getStatusClass = (s) => {
  const m = { 0: 'pending', 1: 'paid', 2: 'shipped', 3: 'completed', 4: 'cancelled', 5: 'refunding', 6: 'refunded', 7: 'rejected' }
  return m[s] || ''
}

// 获取当前用户ID
const getCurrentUserId = () => {
  try {
    const userData = JSON.parse(localStorage.getItem('moli_mall_current_user') || '{}')
    return userData?.userInfo?.id || null
  } catch {
    return null
  }
}

// 加载用户地址
const loadAddresses = async () => {
  try {
    const res = await getAddressList()
    if (res.code === 200) {
      addresses.value = (res.data || []).map(a => ({
        id: a.id,
        name: a.name,
        phone: a.phone,
        province: a.province,
        city: a.city,
        district: a.district,
        detail: a.detail,
        tag: a.tag || '',
        isDefault: a.isDefault === 1
      }))
      const defaultAddr = addresses.value.find(a => a.isDefault)
      if (defaultAddr) {
        selectAddress(defaultAddr)
      }
    } else {
      addresses.value = []
    }
  } catch (e) {
    addresses.value = []
  }
}

// 选择地址
const selectAddress = (addr) => {
  selectedAddressId.value = addr.id
  payForm.receiverName = addr.name
  payForm.receiverPhone = addr.phone
  payForm.receiverAddress = `${addr.province} ${addr.city} ${addr.district} ${addr.detail}`
}

// 打开付款弹窗
const openPayDialog = (order) => {
  currentOrder.value = order
  loadAddresses()
  if (order.receiverName) {
    payForm.receiverName = order.receiverName
    payForm.receiverPhone = order.receiverPhone
    payForm.receiverAddress = order.receiverAddress
  }
  showPayDialog.value = true
}

// 页面加载时从后端拉取真实数据
onMounted(async () => {
  if (route.query.status) {
    status.value = route.query.status
  }
  const userId = userStore.userInfo?.id
  if (userId) {
    loading.value = true
    await orderStore.loadOrders(userId)
    loading.value = false
  }
})

// 筛选订单
const filteredOrders = computed(() => {
  return orderStore.getOrdersByStatus(status.value)
})

// 订单数量
const pendingPaymentCount = computed(() => orderStore.pendingPaymentCount)
const pendingReceiveCount = computed(() => orderStore.pendingReceiveCount)
const refundingCount = computed(() => orderStore.refundingCount)
const refundedCount = computed(() => orderStore.refundedCount)

// 支付订单
const handlePay = async () => {
  if (!payForm.receiverName || !payForm.receiverPhone || !payForm.receiverAddress) {
    ElMessage.warning('请填写完整的收货信息')
    return
  }
  if (!/^1[3-9]\d{9}$/.test(payForm.receiverPhone)) {
    ElMessage.warning('请输入正确的手机号码')
    return
  }

  paying.value = true
  try {
    const res = await apiPayOrder(
      currentOrder.value.id,
      1,
      payForm.receiverName,
      payForm.receiverPhone,
      payForm.receiverAddress
    )
    if (res.code === 200) {
      // 更新本地订单状态
      const order = orderStore.orders.find(o => o.id === currentOrder.value.id)
      if (order) {
        order.status = 1
        order.statusText = '待发货'
        order.receiverName = payForm.receiverName
        order.receiverPhone = payForm.receiverPhone
        order.receiverAddress = payForm.receiverAddress
      }
      showPayDialog.value = false
      ElMessage.success('支付成功')
    } else {
      ElMessage.error(res.message || '支付失败')
    }
  } catch (e) {
    ElMessage.error('支付失败，请重试')
  } finally {
    paying.value = false
  }
}

// 确认收货
const handleReceive = async (orderId) => {
  const ok = await orderStore.confirmReceive(orderId)
  if (ok) {
    ElMessage.success('确认收货成功')
    // 弹出评价入口
    const order = orderStore.orders.find(o => o.id === orderId)
    if (order) {
      pendingReviewOrder.value = order
      showReviewDialog.value = true
    }
  } else {
    ElMessage.error('确认收货失败，请重试')
  }
}

// 取消订单
const handleCancel = async (orderId) => {
  const ok = await orderStore.cancelOrder(orderId)
  if (ok) ElMessage.success('订单已取消')
  else ElMessage.error('取消失败，请重试')
}

// 售后弹窗
const showRefundDialog = ref(false)
const currentRefundOrder = ref(null)
const refundForm = reactive({
  type: 'refund',
  reason: '',
  description: ''
})
const needEscalate = ref(false)

// 售后原因选项
const refundReasons = [
  '商品质量问题',
  '商品与描述不符',
  '收到商品少件/破损',
  '不想要了',
  '其他原因'
]

// 打开售后弹窗
const openRefundDialog = (order) => {
  currentRefundOrder.value = order
  refundForm.type = 'refund'
  refundForm.reason = ''
  refundForm.description = ''
  needEscalate.value = false
  showRefundDialog.value = true
}

// 提交售后申请
const submitRefund = async () => {
  if (!refundForm.reason) {
    ElMessage.warning('请选择售后原因')
    return
  }
  if (!refundForm.description.trim()) {
    ElMessage.warning('请填写详细说明')
    return
  }
  if (!currentRefundOrder.value.shopId) {
    ElMessage.warning('订单缺少商家信息，请返回订单列表刷新后重试')
    return
  }

  try {
    const res = await apiCreateRefund({
      orderId: currentRefundOrder.value.id,
      userId: userStore.userInfo?.id,
      shopId: currentRefundOrder.value.shopId,
      type: refundForm.type,
      reason: refundForm.reason,
      description: refundForm.description,
      amount: Number(currentRefundOrder.value.actualAmount) || 0
    })

    if (res.code === 200) {
      if (needEscalate.value) {
        try {
          const cr = await apiCreateComplaint({
            shopId: currentRefundOrder.value.shopId,
            orderId: currentRefundOrder.value.id,
            refundId: res.data?.id ?? null,
            sourceType: 1,
            reason: '售后客服介入：' + refundForm.reason,
            description: refundForm.description
          })
          if (cr.code !== 200) {
            ElMessage.warning('售后已提交，但客服介入同步失败：' + (cr.message || '请稍后在售后记录中再次申请'))
          }
        } catch (e) {
          ElMessage.warning('售后已提交，客服介入同步失败，请稍后在「售后记录」中点击申请客服介入')
        }
      }
      showRefundDialog.value = false
      ElMessage.success('售后申请已提交，请等待商家处理')
    } else {
      ElMessage.error(res.message || '提交失败')
    }
  } catch (e) {
    ElMessage.error('提交失败，请重试')
  }
}

// ============ 确认收货后弹出评价 ============

// 评价弹窗（用于确认收货后）
const showReviewDialog = ref(false)
const pendingReviewOrder = ref(null)
const reviewForm = reactive({
  rating: 5,
  content: ''
})

const submitReviewFromReceive = async () => {
  if (!reviewForm.content.trim()) {
    ElMessage.warning('请填写评价内容')
    return
  }
  const items = pendingReviewOrder.value?.items
  if (!items?.length) {
    ElMessage.error('订单商品信息不完整，请刷新页面后重试')
    return
  }
  
  // 确保用户已登录
  if (!userStore.userInfo?.id) {
    ElMessage.error('请先登录')
    return
  }
  
  try {
    const firstItem = items[0]
    console.log('[评价] 提交评价，订单ID:', pendingReviewOrder.value.id, '商品ID:', firstItem.productId)
    
    const res = await addReview({
      productId: firstItem.productId,
      userId: userStore.userInfo.id,
      username: userStore.userInfo.username,
      avatar: userStore.userInfo.avatar,
      rating: reviewForm.rating,
      content: reviewForm.content,
      orderId: pendingReviewOrder.value.id
    })
    console.log('[评价] 收到响应:', res)
    
    if (res && res.code === 200) {
      showReviewDialog.value = false
      ElMessage.success('评价提交成功，感谢您的反馈！')
      reviewForm.rating = 5
      reviewForm.content = ''
      pendingReviewOrder.value = null
    } else {
      ElMessage.error(res?.message || '评价提交失败')
    }
  } catch (e) {
    console.error('[评价] 提交失败:', e)
    ElMessage.error('评价提交失败，请重试')
  }
}

const skipReview = () => {
  showReviewDialog.value = false
  pendingReviewOrder.value = null
  reviewForm.rating = 5
  reviewForm.content = ''
}
</script>

<style scoped>
.order-list-page {
  padding: 20px;
}

.order-tabs {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  overflow-x: auto;
  padding-bottom: 8px;
}

.order-tab {
  padding: 10px 20px;
  border-radius: 20px;
  background: #e3e5e7;
  box-shadow: 3px 3px 6px #b6b9ba, -3px -3px 6px #fafafd;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 8px;
}

.order-tab:hover {
  transform: translateY(-2px);
}

.order-tab.active {
  background: linear-gradient(145deg, #4A7C59, #3d6b4a);
  color: white;
}

.tab-badge {
  background: #e74c3c;
  color: white;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.order-card {
  padding: 0;
  overflow: hidden;
}

.order-header {
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

.order-status {
  font-size: 14px;
  font-weight: 600;
}

.order-status.pending { color: #e74c3c; }
.order-status.paid { color: #f39c12; }
.order-status.shipped { color: #f39c12; }
.order-status.completed { color: #27ae60; }
.order-status.cancelled { color: #999; }

.order-items {
  padding: 16px 20px;
}

.order-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 0;
  cursor: pointer;
}

.order-item:not(:last-child) {
  border-bottom: 1px solid #eee;
}

.item-image {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  object-fit: cover;
}

.item-info {
  flex: 1;
}

.item-info h4 {
  font-size: 15px;
  font-weight: 500;
  margin-bottom: 6px;
}

.item-info p {
  font-size: 13px;
  color: #999;
}

.item-price {
  text-align: right;
}

.item-price span:first-child {
  display: block;
  font-weight: 600;
  color: #e74c3c;
}

.item-qty {
  font-size: 13px;
  color: #999;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: #f9f9f9;
  border-top: 1px solid #eee;
}

.total-label {
  color: #666;
}

.total-value {
  font-size: 20px;
  font-weight: bold;
  color: #e74c3c;
}

.order-actions {
  display: flex;
  gap: 12px;
}

.action-btn {
  padding: 8px 20px;
  border: 1px solid #ddd;
  border-radius: 20px;
  background: white;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.3s;
}

.action-btn:hover {
  border-color: #4A7C59;
  color: #4A7C59;
}

.action-btn.primary {
  background: linear-gradient(145deg, #4A7C59, #3d6b4a);
  color: white;
  border: none;
}

.action-btn.primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(74, 124, 89, 0.4);
}

.loading-state {
  padding: 80px 20px;
  text-align: center;
  color: #999;
}

.loading-state .el-icon {
  font-size: 32px;
  margin-bottom: 12px;
}

.empty-state {
  padding: 80px 20px;
  text-align: center;
}

.empty-state-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.empty-state-text {
  font-size: 16px;
  color: #999;
}

/* 付款弹窗样式 */
.pay-dialog-content {
  max-height: 60vh;
  overflow-y: auto;
}

.pay-section {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.pay-section:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.pay-section-title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 12px;
  color: #333;
}

.pay-products {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.pay-product-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px;
  background: #f9f9f9;
  border-radius: 8px;
}

.pay-product-img {
  width: 50px;
  height: 50px;
  border-radius: 6px;
  object-fit: cover;
}

.pay-product-info {
  flex: 1;
}

.pay-product-name {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 4px;
}

.pay-product-specs {
  font-size: 12px;
  color: #999;
}

.pay-product-price {
  text-align: right;
}

.pay-product-price span:first-child {
  display: block;
  font-weight: 600;
  color: #e74c3c;
}

.pay-product-qty {
  font-size: 12px;
  color: #999;
}

.address-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 12px;
}

.address-option {
  display: flex;
  align-items: center;
  padding: 12px;
  border: 2px solid #eee;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s;
}

.address-option:hover {
  border-color: #4A7C59;
}

.address-option.selected {
  border-color: #4A7C59;
  background: #f0f9f0;
}

.address-option-content {
  flex: 1;
}

.address-option-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.address-option-name {
  font-weight: 600;
}

.address-option-phone {
  color: #666;
}

.address-option-default {
  background: #4A7C59;
  color: white;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 11px;
}

.address-option-detail {
  font-size: 13px;
  color: #666;
}

.address-option-check {
  width: 24px;
  height: 24px;
  background: #4A7C59;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
}

.address-empty {
  padding: 20px;
  text-align: center;
  color: #999;
  background: #f5f5f5;
  border-radius: 8px;
  margin-bottom: 12px;
}

.address-form {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.address-form-row {
  display: flex;
  gap: 10px;
}

.pay-amount-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  background: #f9f9f9;
  padding: 15px;
  border-radius: 8px;
}

.pay-amount-item {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  color: #666;
}

.pay-amount-item.total {
  padding-top: 10px;
  border-top: 1px solid #ddd;
  font-weight: 600;
  font-size: 16px;
  color: #333;
}

.pay-total-value {
  color: #e74c3c;
  font-size: 20px;
}

/* 售后申请弹窗样式 */
.refund-dialog-content {
  max-height: 60vh;
  overflow-y: auto;
}

.refund-section {
  margin-bottom: 20px;
}

.refund-section-title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 10px;
  color: #333;
}

.refund-order-info {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #666;
  padding: 10px;
  background: #f5f5f5;
  border-radius: 6px;
}

.refund-amount {
  color: #e74c3c;
  font-size: 16px;
}

.refund-type-group {
  display: flex;
  gap: 20px;
}

/* 确认收货后评价弹窗 */
.review-receive-content {
  padding: 8px 0;
}

.review-order-info {
  background: #f0f9f0;
  border-radius: 8px;
  padding: 16px;
  text-align: center;
}

.review-order-info p {
  margin: 0 0 8px;
  color: #4A7C59;
  font-size: 15px;
  font-weight: 500;
}

.review-product-name {
  color: #666;
  font-size: 13px;
}
</style>
