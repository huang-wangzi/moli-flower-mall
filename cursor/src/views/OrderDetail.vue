<template>
  <div class="order-detail-page">
    <button class="back-btn" @click="$router.back()">← 返回</button>

    <div v-if="order.id" class="order-content">
      <!-- 订单状态 -->
      <div class="neumorphic-card status-card">
        <div class="status-info">
          <span class="status-icon">{{ statusIcon }}</span>
          <div class="status-text">
            <h3>{{ order.statusText }}</h3>
            <p>{{ statusDesc }}</p>
          </div>
        </div>
      </div>

      <!-- 物流信息 -->
      <div v-if="order.shipTime" class="neumorphic-card logistics-card">
        <h3 class="section-title">物流信息</h3>
        <div class="logistics-info">
          <p>商家已发货，请注意查收</p>
        </div>
      </div>

      <!-- 收货地址 -->
      <div class="neumorphic-card address-card">
        <h3 class="section-title">收货信息</h3>
        <div class="address-info">
          <p><strong>{{ order.receiverName || '未填写' }}</strong> {{ order.receiverPhone || '' }}</p>
          <p>{{ order.receiverAddress || '未填写地址' }}</p>
        </div>
      </div>

      <!-- 订单备注 -->
      <div v-if="order.remark || order.adminRemark" class="neumorphic-card remark-card">
        <h3 class="section-title">备注信息</h3>
        <div class="amount-list">
          <div v-if="order.remark" class="amount-item">
            <span>订单备注</span>
            <span>{{ order.remark }}</span>
          </div>
          <div v-if="order.adminRemark" class="amount-item">
            <span style="color: #e6a23c; font-weight: bold;">处理备注</span>
            <span style="color: #e6a23c; font-weight: bold;">{{ order.adminRemark }}</span>
          </div>
        </div>
      </div>

      <!-- 订单商品 -->
      <div class="neumorphic-card items-card">
        <h3 class="section-title">商品信息</h3>
        <div class="order-items">
          <div v-for="item in order.items" :key="item.id" class="order-item">
            <img :src="normalizeImage(item.productImage)" class="item-image" @click="$router.push(`/product/${item.productId}`)" />
            <div class="item-info">
              <h4 @click="$router.push(`/product/${item.productId}`)">{{ item.productName }}</h4>
              <p>{{ item.specs || '无规格' }}</p>
            </div>
            <div class="item-price">
              <span>¥{{ Number(item.price || 0).toFixed(2) }}</span>
              <span class="item-qty">x{{ item.quantity }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 订单金额 -->
      <div class="neumorphic-card amount-card">
        <h3 class="section-title">订单金额</h3>
        <div class="amount-list">
          <div class="amount-item">
            <span>商品金额</span>
            <span>¥{{ Number(order.totalAmount || 0).toFixed(2) }}</span>
          </div>
          <div class="amount-item">
            <span>运费</span>
            <span>¥{{ Number(order.freight || 0).toFixed(2) }}</span>
          </div>
          <div v-if="Number(order.discount || 0) > 0" class="amount-item">
            <span>优惠</span>
            <span class="discount">-¥{{ Number(order.discount || 0).toFixed(2) }}</span>
          </div>
          <div class="amount-item total">
            <span>实付金额</span>
            <span class="total-value">¥{{ Number(order.actualAmount || 0).toFixed(2) }}</span>
          </div>
        </div>
      </div>

      <!-- 订单操作 -->
      <div class="order-actions">
        <button v-if="order.status === 0" class="btn-primary" @click="openPayDialog">
          去付款
        </button>
        <button v-if="order.status === 2" class="btn-primary" @click="handleReceive">
          确认收货
        </button>
        <button v-if="order.status === 1" class="neumorphic-btn-sm" disabled>
          等待商家发货
        </button>
        <button v-if="order.status === 3" class="btn-outline" @click="showReviewDialog = true">
          发表评价
        </button>
        <!-- 退款中：显示取消售后按钮 -->
        <button v-if="order.status === 5" class="btn-danger-outline" @click="handleCancelAfterSales">
          取消售后
        </button>
        <!-- 已拒绝：显示再次申诉按钮 -->
        <button v-if="order.status === 7" class="btn-warning-outline" @click="handleReAppeal">
          再次申诉
        </button>
      </div>
    </div>

    <div v-else-if="loading" class="empty-state">
      <p>加载中...</p>
    </div>

    <div v-else class="empty-state">
      <p>订单不存在</p>
    </div>

    <!-- 付款弹窗 -->
    <el-dialog v-model="showPayDialog" title="确认付款" width="600px" :close-on-click-modal="false" destroy-on-close>
      <div class="pay-dialog-content">
        <!-- 商品信息 -->
        <div class="pay-section">
          <h4 class="pay-section-title">商品信息</h4>
          <div class="pay-products">
            <div v-for="item in order.items" :key="item.id" class="pay-product-item">
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
              <span>¥{{ Number(order.totalAmount || 0).toFixed(2) }}</span>
            </div>
            <div class="pay-amount-item">
              <span>运费</span>
              <span>¥{{ Number(order.freight || 0).toFixed(2) }}</span>
            </div>
            <div class="pay-amount-item total">
              <span>实付金额</span>
              <span class="pay-total-value">¥{{ Number(order.actualAmount || 0).toFixed(2) }}</span>
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

    <!-- 评价弹窗 -->
    <el-dialog v-model="showReviewDialog" title="发表评价" width="550px" :close-on-click-modal="false" destroy-on-close>
      <div v-if="order.items && order.items.length > 0" class="review-products">
        <div v-for="item in order.items" :key="item.id" class="review-product-item">
          <div class="review-product-info">
            <img :src="normalizeImage(item.productImage)" class="review-product-img" />
            <div>
              <div class="review-product-name">{{ item.productName }}</div>
              <div class="review-product-price">¥{{ Number(item.price || 0).toFixed(2) }}</div>
            </div>
          </div>
          <div v-if="submittedProductIds.includes(item.productId)" class="review-submitted">
            <el-icon color="#67c23a"><SuccessFilled /></el-icon>
            <span>已评价</span>
          </div>
          <div v-else class="review-form-item">
            <div class="review-rating-row">
              <span class="review-label">评分：</span>
              <el-rate v-model="reviewRatings[item.productId]" />
            </div>
            <el-input
              v-model="reviewContents[item.productId]"
              type="textarea"
              :rows="2"
              :placeholder="`分享对「${item.productName}」的使用体验...`"
              maxlength="200"
              show-word-limit
            />
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="showReviewDialog = false">稍后评价</el-button>
        <el-button type="primary" @click="submitReviews" :loading="submitting">
          提交评价
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { SuccessFilled } from '@element-plus/icons-vue'
import { getOrderDetail, payOrder as apiPayOrder, receiveOrder as apiReceiveOrder, getAddressList, addReview, getUserRefunds, cancelRefund, reAppealRefund } from '@/api'
import { useUserStore } from '@/stores/user'
import { normalizeUploadUrl } from '@/utils/imageUrl'

const normalizeImage = (url) => normalizeUploadUrl(url) || url || 'https://cube.elemecdn.com/6/94/4d3ea53c084bad6931a56d5158a48jpeg.jpeg'

const route = useRoute()
const userStore = useUserStore()

const order = ref({})
const loading = ref(false)

// 付款弹窗
const showPayDialog = ref(false)
const paying = ref(false)
const addresses = ref([])
const selectedAddressId = ref(null)
const payForm = reactive({
  receiverName: '',
  receiverPhone: '',
  receiverAddress: ''
})

// 取消售后
const handleCancelAfterSales = async () => {
  try {
    // 先查出售后单 ID
    const userId = getCurrentUserId()
    if (!userId) return
    
    const resList = await getUserRefunds(userId)
    if (resList.code === 200) {
      const activeRefund = (resList.data || []).find(r => r.orderId === order.value.id && (r.status === 0 || r.status === 1))
      if (activeRefund) {
        await ElMessageBox.confirm('确定要取消售后申请吗？', '确认取消', { type: 'warning' })
        const res = await cancelRefund(activeRefund.id)
        if (res.code === 200) {
          ElMessage.success('已取消售后')
          loadOrder() // 刷新订单详情
        } else {
          ElMessage.error(res.message || '操作失败')
        }
      } else {
        ElMessage.warning('未找到可取消的售后单')
      }
    }
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

// 再次申诉
const handleReAppeal = async () => {
  try {
    const { value } = await ElMessageBox.prompt('请输入再次申诉的理由', '再次申诉', {
      confirmButtonText: '提交申诉',
      cancelButtonText: '取消',
      inputPlaceholder: '请详细描述您的问题...',
      inputValidator: (val) => {
        if (!val || val.trim().length < 5) return '理由不能少于5个字'
        return true
      }
    })

    if (value) {
      // 提交再次申诉，后端会重新创建售后订单并关联投诉
      const res = await reAppealRefund({
        orderId: order.value.id,
        reason: '用户再次申诉',
        description: value
      })

      if (res.code === 200) {
        ElMessage.success('申诉已提交，请等待管理员介入')
        // 跳转至售后列表页查看最新进度
        router.push('/after-sales')
      } else {
        ElMessage.error(res.message || '提交失败')
      }
    }
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

// 评价弹窗
const showReviewDialog = ref(false)
const submitting = ref(false)
const reviewRatings = reactive({})
const reviewContents = reactive({})
const submittedProductIds = ref([])

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

const STATUS_ICON = {
  0: '💳',
  1: '⏳',
  2: '📦',
  3: '✅',
  4: '❌',
  5: '🔄',
  6: '💰',
  7: '🚫'
}

const statusIcon = computed(() => STATUS_ICON[order.value.status] || '📋')

const statusDesc = computed(() => {
  const descMap = {
    0: '请尽快完成支付',
    1: '商家正在准备商品',
    2: '商家已发货，请注意查收',
    3: '交易已完成',
    4: '订单已取消',
    5: '正在处理售后退款',
    6: '款项已原路退回',
    7: '售后申请已被拒绝'
  }
  return descMap[order.value.status] || ''
})

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
const openPayDialog = () => {
  loadAddresses()
  // 如果已有收货信息，填充表单
  if (order.value.receiverName) {
    payForm.receiverName = order.value.receiverName
    payForm.receiverPhone = order.value.receiverPhone
    payForm.receiverAddress = order.value.receiverAddress
  }
  showPayDialog.value = true
}

// 加载订单详情
const loadOrder = async () => {
  loading.value = true
  try {
    const res = await getOrderDetail(route.params.id)
    if (res.code === 200 && res.data) {
      order.value = {
        ...res.data.order,
        items: res.data.items || [],
        statusText: STATUS_TEXT[res.data.order.status] || '未知'
      }
    }
  } catch (e) {
    console.error('加载订单失败', e)
  } finally {
    loading.value = false
  }
}

// 支付
const handlePay = async () => {
  // 验证收货信息
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
      order.value.id,
      1,
      payForm.receiverName,
      payForm.receiverPhone,
      payForm.receiverAddress
    )
    if (res.code === 200) {
      order.value.status = 1
      order.value.statusText = '待发货'
      order.value.receiverName = payForm.receiverName
      order.value.receiverPhone = payForm.receiverPhone
      order.value.receiverAddress = payForm.receiverAddress
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
const handleReceive = async () => {
  try {
    const res = await apiReceiveOrder(order.value.id)
    if (res.code === 200) {
      order.value.status = 3
      order.value.statusText = '已完成'
      ElMessage.success('确认收货成功')

      if (order.value.items && order.value.items.length > 0) {
        submittedProductIds.value = []
        order.value.items.forEach(item => {
          if (reviewRatings[item.productId] === undefined) {
            reviewRatings[item.productId] = 5
          }
          if (reviewContents[item.productId] === undefined) {
            reviewContents[item.productId] = ''
          }
        })
      }
      showReviewDialog.value = true
    } else {
      ElMessage.error(res.message || '确认收货失败')
    }
  } catch (e) {
    ElMessage.error('确认收货失败，请重试')
  }
}

// 提交评价
const submitReviews = async () => {
  const itemsToReview = (order.value.items || []).filter(
    item => !submittedProductIds.value.includes(item.productId)
  )

  if (itemsToReview.length === 0) {
    showReviewDialog.value = false
    ElMessage.info('所有商品已评价')
    return
  }

  // 确保用户已登录
  if (!userStore.userInfo?.id) {
    ElMessage.error('请先登录')
    return
  }

  submitting.value = true
  let successCount = 0
  let failCount = 0

  for (const item of itemsToReview) {
    const rating = reviewRatings[item.productId] || 5
    const content = reviewContents[item.productId] || ''

    if (!content.trim()) {
      failCount++
      continue
    }

    try {
      console.log('[评价] 提交订单评价，商品ID:', item.productId, '订单ID:', order.value.id)
      const res = await addReview({
        productId: item.productId,
        orderId: order.value.id,
        userId: userStore.userInfo.id,
        username: userStore.userInfo.username || '用户',
        avatar: userStore.userInfo.avatar || '',
        rating,
        content: content.trim()
      })
      console.log('[评价] 收到响应:', res)
      if (res && res.code === 200) {
        successCount++
        submittedProductIds.value.push(item.productId)
      } else {
        failCount++
        console.log('[评价] 评价失败:', res?.message)
      }
    } catch (e) {
      failCount++
      console.error('评价失败', e)
    }
  }

  submitting.value = false

  if (successCount > 0) {
    ElMessage.success(`成功评价 ${successCount} 件商品`)
  }
  if (failCount > 0) {
    ElMessage.warning(`${failCount} 件商品评价失败（请填写内容）`)
  }

  const remaining = itemsToReview.filter(
    item => !submittedProductIds.value.includes(item.productId)
  )
  if (remaining.length === 0) {
    showReviewDialog.value = false
  }
}

onMounted(() => {
  loadOrder()
})
</script>

<style scoped>
.order-detail-page {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.back-btn {
  padding: 8px 16px;
  border: none;
  background: #e3e5e7;
  border-radius: 8px;
  cursor: pointer;
  margin-bottom: 20px;
}

.order-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
}

.status-card {
  background: linear-gradient(135deg, #4A7C59 0%, #6B9B7A 100%);
  color: white;
}

.status-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.status-icon {
  font-size: 40px;
}

.status-text h3 {
  font-size: 20px;
  margin-bottom: 4px;
}

.status-text p {
  opacity: 0.8;
  font-size: 14px;
}

.logistics-card,
.address-card,
.items-card,
.amount-card {
  padding: 20px;
}

.logistics-info p {
  margin-bottom: 16px;
  color: #666;
}

.address-info p {
  margin: 4px 0;
  color: #666;
}

.remark-card {
  margin-top: 16px;
}

.order-items {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.order-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 0;
}

.order-item:not(:last-child) {
  border-bottom: 1px solid #eee;
}

.item-image {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  object-fit: cover;
  cursor: pointer;
}

.item-info {
  flex: 1;
}

.item-info h4 {
  font-size: 15px;
  margin-bottom: 4px;
  cursor: pointer;
}

.item-info h4:hover {
  color: #4A7C59;
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

.amount-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.amount-item {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  color: #666;
}

.amount-item.discount {
  color: #27ae60;
}

.amount-item.total {
  padding-top: 12px;
  border-top: 1px solid #eee;
  font-weight: 600;
  font-size: 16px;
  color: #333;
}

.total-value {
  color: #e74c3c;
  font-size: 20px;
}

.order-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.empty-state {
  padding: 100px 20px;
  text-align: center;
  color: #999;
}

.btn-primary {
  padding: 12px 32px;
  border: none;
  border-radius: 25px;
  background: linear-gradient(145deg, #4A7C59, #3d6b4a);
  color: white;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(74, 124, 89, 0.4);
}

.btn-outline {
  padding: 12px 24px;
  border: 2px solid #4A7C59;
  background: transparent;
  color: #4A7C59;
  border-radius: 25px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.btn-outline:hover {
  background: #4A7C59;
  color: white;
}

.btn-danger-outline {
  padding: 8px 20px;
  border-radius: 20px;
  border: 1px solid #fbc4c4;
  background: #fef0f0;
  color: #f56c6c;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.btn-danger-outline:hover {
  background: #f56c6c;
  color: white;
}

.btn-warning-outline {
  padding: 8px 20px;
  border-radius: 20px;
  border: 1px solid #f5dab1;
  background: #fdf6ec;
  color: #e6a23c;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.btn-warning-outline:hover {
  background: #e6a23c;
  color: white;
}

.neumorphic-btn-sm {
  padding: 8px 20px;
  border: 1px solid #ddd;
  border-radius: 20px;
  background: white;
  cursor: default;
  font-size: 13px;
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

/* 评价弹窗 */
.review-products {
  display: flex;
  flex-direction: column;
  gap: 16px;
  max-height: 60vh;
  overflow-y: auto;
}

.review-product-item {
  border: 1px solid #eee;
  border-radius: 12px;
  padding: 14px;
}

.review-product-info {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.review-product-img {
  width: 50px;
  height: 50px;
  border-radius: 8px;
  object-fit: cover;
}

.review-product-name {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 4px;
}

.review-product-price {
  font-size: 13px;
  color: #e74c3c;
}

.review-form-item {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.review-rating-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.review-label {
  font-size: 14px;
  color: #666;
}

.review-submitted {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #67c23a;
  font-size: 14px;
}
</style>
