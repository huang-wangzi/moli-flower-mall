<template>
  <div class="product-detail-page">
    <!-- 商品基本信息 -->
    <div class="product-main" v-if="product.id">
      <!-- 图片展示区 -->
      <div class="image-section">
        <div class="main-image" @click="showImageViewer = true">
          <img :src="currentImage" alt="商品图片" />
          <div class="image-tip">点击查看大图</div>
        </div>
        <div class="thumbnail-list" v-if="product.images && product.images.length > 1">
          <div
            v-for="(img, index) in product.images"
            :key="index"
            class="thumbnail"
            :class="{ active: currentImageIndex === index }"
            @click="currentImageIndex = index"
          >
            <img :src="img" alt="" />
          </div>
        </div>
      </div>

      <!-- 商品信息区 -->
      <div class="info-section">
        <h1 class="product-title">{{ product.name }}</h1>
        <p class="product-subtitle">{{ product.shopName || '横州茉莉花商城' }}</p>

        <!-- 价格 -->
        <div class="price-box">
          <span class="price-symbol">¥</span>
          <span class="price-value">{{ selectedPrice.toFixed(2) }}</span>
          <span v-if="product.originalPrice > product.price" class="original-price">
            ¥{{ product.originalPrice.toFixed(2) }}
          </span>
        </div>

        <!-- 规格选择 -->
        <div class="specs-section" v-if="product.specs && product.specs.length > 0">
          <div v-for="spec in product.specs" :key="spec.name" class="spec-item">
            <span class="spec-label">{{ spec.name }}：</span>
            <div class="spec-options">
              <span
                v-for="option in spec.options"
                :key="option"
                class="spec-option"
                :class="{ active: selectedSpecs[spec.name] === option }"
                @click="selectSpec(spec.name, option)"
              >
                {{ option }}
              </span>
            </div>
          </div>
        </div>

        <!-- 数量选择 -->
        <div class="quantity-section" v-if="stockStatus === 'available'">
          <span class="quantity-label">数量：</span>
          <div class="quantity-input">
            <button class="qty-btn" @click="quantity > 1 && quantity--">-</button>
            <input type="number" v-model.number="quantity" min="1" :max="effectiveStock" />
            <button class="qty-btn" @click="quantity < effectiveStock && quantity++">+</button>
          </div>
          <span class="stock-info">库存 {{ effectiveStock }} 件</span>
        </div>

        <!-- 售罄提示 -->
        <div class="sold-out-notice" v-else>
          <span class="sold-out-text">该商品已售罄</span>
        </div>

        <!-- 核心操作按钮 -->
        <div class="action-buttons">
          <button class="neumorphic-btn-sm contact-btn" @click="handleContact">
            💬 沟通商家
          </button>
          <button class="neumorphic-btn-sm service-btn" @click="handleService">
            🎧 联系客服
          </button>
        </div>

        <div class="main-actions">
          <button class="action-btn cart-btn" @click="handleAddCart" :disabled="stockStatus !== 'available'">
            🛒 加入购物车
          </button>
          <button class="action-btn buy-btn" @click="handleBuyNow" :disabled="stockStatus !== 'available'">
            {{ stockStatus === 'available' ? '立即购买' : '已售罄' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 商品不存在时的提示 -->
    <div v-else class="empty-product">
      <p>商品不存在或已下架</p>
      <button class="btn-primary" @click="$router.push('/products')">返回商品列表</button>
    </div>

    <!-- 商品详情 -->
    <div class="neumorphic-card detail-section" v-if="product.id">
      <h3 class="section-title">商品详情</h3>
      <div class="product-description" v-html="product.description || '<p>暂无商品详情</p>'"></div>
    </div>

    <!-- 用户评价 -->
    <div class="neumorphic-card review-section" v-if="product.id">
      <h3 class="section-title">
        用户评价
        <span class="review-count">({{ reviews.length }}条)</span>
      </h3>

      <div v-if="reviews.length > 0" class="reviews-list">
        <div v-for="review in reviews" :key="review.id" class="review-item">
          <div class="review-header">
            <img :src="normalizeUploadUrl(review.avatar) || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'" class="review-avatar" @error="(e) => e.target.src = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'" />
            <span class="review-user">{{ review.username || '匿名用户' }}</span>
            <div class="review-rating">
              <el-rate v-model="review.rating" disabled text-color="#ff9900" />
            </div>
            <span class="review-time">{{ formatTime(review.createTime) }}</span>
          </div>
          <div class="review-content">{{ review.content }}</div>
          <!-- 商家回复 -->
          <div v-if="review.reply" class="shop-reply">
            <span class="reply-label">商家回复：</span>
            <span class="reply-content">{{ review.reply }}</span>
          </div>
        </div>
      </div>

      <div v-else class="empty-reviews">
        <p>暂无评价，快来购买后评价吧~</p>
      </div>

      <!-- 申请售后按钮 -->
      <div v-if="canReview" class="review-actions">
        <button class="btn-primary" @click="showReviewDialog = true">
          发表评价
        </button>
        <button class="btn-outline" @click="showRefundDialog = true">
          申请退换货
        </button>
      </div>
    </div>

    <!-- 图片查看器 -->
    <el-dialog v-model="showImageViewer" width="80%" top="5vh" destroy-on-close>
      <div class="image-viewer">
        <img :src="normalizeUploadUrl(product.images[currentImageIndex])" />
      </div>
    </el-dialog>

    <!-- 评价弹窗 -->
    <el-dialog v-model="showReviewDialog" title="发表评价" width="500px">
      <el-form :model="reviewForm" label-width="80px">
        <el-form-item label="评分">
          <el-rate v-model="reviewForm.rating" />
        </el-form-item>
        <el-form-item label="评价内容">
          <el-input
            v-model="reviewForm.content"
            type="textarea"
            :rows="4"
            placeholder="分享您的使用体验..."
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showReviewDialog = false">取消</el-button>
        <el-button type="primary" @click="submitReview" :loading="submitting">提交</el-button>
      </template>
    </el-dialog>

    <!-- 退换货弹窗 -->
    <el-dialog v-model="showRefundDialog" title="申请退换货" width="500px">
      <el-form :model="refundForm" :rules="refundRules" ref="refundFormRef" label-width="100px">
        <el-form-item label="售后类型" prop="type">
          <el-radio-group v-model="refundForm.type">
            <el-radio value="refund">仅退款</el-radio>
            <el-radio value="return">退货退款</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="退款原因" prop="reason">
          <el-select v-model="refundForm.reason" placeholder="请选择退款原因">
            <el-option label="商品质量问题" value="商品质量问题" />
            <el-option label="商品与描述不符" value="商品与描述不符" />
            <el-option label="收到商品少件/破损" value="收到商品少件/破损" />
            <el-option label="不想要了" value="不想要了" />
            <el-option label="其他原因" value="其他原因" />
          </el-select>
        </el-form-item>
        <el-form-item label="详细说明" prop="description">
          <el-input
            v-model="refundForm.description"
            type="textarea"
            :rows="3"
            placeholder="请详细描述您遇到的问题..."
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRefundDialog = false">取消</el-button>
        <el-button type="primary" @click="submitRefund">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useCartStore } from '@/stores/cart'
import { useUserStore } from '@/stores/user'
import { getProductDetail, createOrder, addReview, sendChatMessage, getSupportAdminId } from '@/api'
import { normalizeUploadUrl } from '@/utils/imageUrl'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()

// 商品数据
const product = ref({
  id: null,
  name: '',
  images: [],
  price: 0,
  originalPrice: 0,
  stock: 0,
  sales: 0,
  specs: [],
  description: '',
  reviews: [],
  shopId: null,
  shopName: ''
})

// 评价列表
const reviews = ref([])

const currentImageIndex = ref(0)
const quantity = ref(1)

// 选中的规格
const selectedSpecs = reactive({})

// 图片查看器
const showImageViewer = ref(false)

// 评价弹窗
const showReviewDialog = ref(false)
const submitting = ref(false)
const reviewForm = reactive({
  rating: 5,
  content: ''
})

// 退换货弹窗
const showRefundDialog = ref(false)
const refundFormRef = ref(null)
const refundForm = reactive({
  type: 'refund',
  reason: '',
  description: ''
})

// 退换货表单校验规则
const refundRules = {
  reason: [{ required: true, message: '请选择退款原因', trigger: 'change' }],
  description: [{ required: true, message: '请填写详细说明', trigger: 'blur' }]
}

// 当前图片
const currentImage = computed(() => {
  const images = product.value.images
  if (!images || images.length === 0) {
    return 'https://cube.elemecdn.com/6/94/4d3ea53c084bad6931a56d5158a48jpeg.jpeg'
  }
  return images[currentImageIndex.value] || images[0] || 'https://cube.elemecdn.com/6/94/4d3ea53c084bad6931a56d5158a48jpeg.jpeg'
})

// 计算价格
const selectedPrice = computed(() => {
  return product.value.price || 0
})

// 库存状态
const effectiveStock = computed(() => {
  const stock = product.value.stock || 0
  return stock < 0 ? 0 : stock
})

const stockStatus = computed(() => {
  const stock = product.value.stock || 0
  if (stock <= 0) return 'soldout'
  return 'available'
})

// 是否可以评价/申请售后
const canReview = computed(() => {
  if (!userStore.isLoggedIn) return false
  return true
})

// 选择规格
const selectSpec = (specName, value) => {
  selectedSpecs[specName] = value
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString('zh-CN')
}

// 获取商品详情
const loadProduct = async () => {
  const id = route.params.id

  if (!id) {
    ElMessage.error('商品ID不存在')
    router.back()
    return
  }

  try {
    const res = await getProductDetail(id)

    if (res.code === 200) {
      const data = res.data
      product.value = data.product || {}

      // 处理图片
      if (product.value.images) {
        try {
          const images = typeof product.value.images === 'string'
            ? JSON.parse(product.value.images)
            : product.value.images
          const arr = Array.isArray(images) ? images : [images]
          product.value.images = arr.map((u) => normalizeUploadUrl(u))
        } catch (e) {
          product.value.images = [normalizeUploadUrl(product.value.images)]
        }
      } else {
        product.value.images = ['https://cube.elemecdn.com/6/94/4d3ea53c084bad6931a56d5158a48jpeg.jpeg']
      }

      // 处理规格
      if (product.value.specs) {
        try {
          const specs = typeof product.value.specs === 'string'
            ? JSON.parse(product.value.specs)
            : product.value.specs
          product.value.specs = Array.isArray(specs) ? specs : []
        } catch (e) {
          product.value.specs = []
        }
      }

      // 设置评价列表
      reviews.value = data.reviews || []

      // 初始化规格选中第一个
      if (product.value.specs && product.value.specs.length > 0) {
        product.value.specs.forEach(spec => {
          if (spec.options && spec.options.length > 0) {
            selectedSpecs[spec.name] = spec.options[0]
          }
        })
      }

      // 添加浏览历史
      const firstImage = product.value.images && product.value.images.length > 0 
        ? product.value.images[0] 
        : 'https://cube.elemecdn.com/6/94/4d3ea53c084bad6931a56d5158a48jpeg.jpeg'
      userStore.addBrowseHistory({
        id: product.value.id,
        name: product.value.name,
        price: product.value.price,
        image: firstImage,
        browseTime: new Date().toISOString()
      })
    } else {
      ElMessage.error('商品不存在或已下架')
      router.back()
    }
  } catch (error) {
    console.error('加载商品失败', error)
    ElMessage.error('加载商品失败')
    router.back()
  }
}

// 加入购物车
const handleAddCart = () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  const specsStr = Object.values(selectedSpecs).join('/')
  cartStore.addToCart({
    id: product.value.id,
    name: product.value.name,
    price: product.value.price,
    images: product.value.images,
    specs: product.value.specs
  }, quantity.value, specsStr)
  ElMessage.success('已加入购物车')
}

// 立即购买
const handleBuyNow = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  // 构建订单项
  const specsStr = Object.values(selectedSpecs).join('/')
  const orderItem = {
    productId: product.value.id,
    name: product.value.name,
    price: product.value.price,
    image: product.value.images && product.value.images.length > 0 ? product.value.images[0] : '',
    specs: specsStr,
    quantity: quantity.value
  }

  try {
    // 直接创建订单
    const res = await createOrder({
      items: [orderItem],
      receiverName: userStore.userInfo?.realName || '',
      receiverPhone: userStore.userInfo?.phone || '',
      receiverAddress: userStore.userInfo?.address || '',
      remark: '立即购买'
    })

    if (res.code === 200 && res.data && res.data.length > 0) {
      const order = res.data[0]
      ElMessage.success('订单创建成功，请完成支付')
      // 跳转到订单详情页，订单状态为待支付(0)，会在页面显示"去付款"按钮
      router.push(`/order/${order.id}`)
    } else {
      ElMessage.error(res.message || '订单创建失败')
    }
  } catch (error) {
    console.error('立即购买失败:', error)
    ElMessage.error('订单创建失败')
  }
}

// 联系商家 - 跳转聊天页面
const handleContact = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  if (!product.value?.id) {
    ElMessage.warning('商品信息加载中，请稍后')
    return
  }
  const sid = product.value?.shopId
  if (sid) {
    router.push(`/chat/${sid}?productId=${product.value.id}`)
  } else {
    ElMessage.warning('无法获取商家信息')
  }
}

// 联系客服：与平台管理员会话
const handleService = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  if (!product.value?.name || !product.value?.id) {
    ElMessage.warning('商品信息加载中，请稍后')
    return
  }
  try {
    const adminRes = await getSupportAdminId()
    if (adminRes.code !== 200 || adminRes.data == null) {
      ElMessage.error(adminRes.message || '无法连接客服，请稍后重试')
      return
    }
    const adminId = adminRes.data
    await sendChatMessage(
      adminId,
      `用户「${userStore.userInfo?.username}」咨询商品「${product.value.name}」(ID:${product.value.id})`,
      null
    )
    router.push(`/chat/${adminId}`)
  } catch (e) {
    console.error(e)
    ElMessage.error('连接失败，请稍后重试')
  }
}

// 提交评价
const submitReview = async () => {
  if (!reviewForm.content.trim()) {
    ElMessage.warning('请填写评价内容')
    return
  }

  if (!userStore.isLoggedIn || !userStore.userInfo?.id) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  submitting.value = true

  try {
    console.log('[评价] 提交商品评价，商品ID:', product.value.id, '用户ID:', userStore.userInfo.id)
    const res = await addReview({
      productId: product.value.id,
      userId: userStore.userInfo.id,
      username: userStore.userInfo.username,
      avatar: userStore.userInfo.avatar,
      rating: reviewForm.rating,
      content: reviewForm.content
    })
    console.log('[评价] 收到响应:', res)

    if (res && res.code === 200) {
      ElMessage.success('评价提交成功')
      showReviewDialog.value = false
      reviewForm.rating = 5
      reviewForm.content = ''

      // 重新加载商品详情刷新评价
      await loadProduct()
    } else {
      ElMessage.error(res?.message || '评价提交失败')
    }
  } catch (error) {
    console.error('提交评价失败', error)
    ElMessage.error('评价提交失败')
  } finally {
    submitting.value = false
  }
}

// 提交退换货申请
const submitRefund = async () => {
  if (!refundFormRef.value) return

  try {
    await refundFormRef.value.validate()
    ElMessage.success('退换货申请已提交')
    showRefundDialog.value = false
    router.push('/orders')
  } catch (err) {
    // 表单校验失败
  }
}

// 监听路由参数变化
watch(() => route.params.id, (newId) => {
  if (newId) {
    loadProduct()
  }
})

onMounted(() => {
  loadProduct()
})
</script>

<style scoped>
.product-detail-page {
  padding: 20px;
}

/* 商品主体区域 */
.product-main {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 40px;
  margin-bottom: 40px;
}

/* 图片区域 */
.image-section {
  position: sticky;
  top: 90px;
}

.main-image {
  position: relative;
  border-radius: 20px;
  overflow: hidden;
  cursor: pointer;
  box-shadow: 8px 8px 16px #b6b9ba, -8px -8px 16px #fafafd;
}

.main-image img {
  width: 100%;
  height: 450px;
  object-fit: cover;
}

.image-tip {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(0, 0, 0, 0.5);
  color: white;
  text-align: center;
  padding: 10px;
  font-size: 14px;
}

.thumbnail-list {
  display: flex;
  gap: 12px;
  margin-top: 16px;
  overflow-x: auto;
}

.thumbnail {
  width: 80px;
  height: 80px;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  border: 3px solid transparent;
  transition: all 0.3s;
  flex-shrink: 0;
}

.thumbnail.active {
  border-color: #4A7C59;
}

.thumbnail img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 商品信息区 */
.info-section {
  padding: 20px 0;
}

.product-title {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin-bottom: 10px;
  line-height: 1.4;
}

.product-subtitle {
  font-size: 14px;
  color: #999;
  margin-bottom: 20px;
}

/* 价格 */
.price-box {
  background: #e3e5e7;
  border-radius: 12px;
  padding: 16px 20px;
  display: flex;
  align-items: baseline;
  margin-bottom: 24px;
  box-shadow: inset 4px 4px 8px #b6b9ba, inset -4px -4px 8px #fafafd;
}

.price-box .price-symbol {
  font-size: 18px;
  color: #e74c3c;
}

.price-box .price-value {
  font-size: 36px;
  font-weight: bold;
  color: #e74c3c;
}

.original-price {
  font-size: 16px;
  color: #999;
  text-decoration: line-through;
  margin-left: 12px;
}

/* 规格选择 */
.specs-section {
  margin-bottom: 24px;
}

.spec-item {
  margin-bottom: 16px;
}

.spec-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 10px;
  display: block;
}

.spec-options {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.spec-option {
  padding: 8px 20px;
  border-radius: 20px;
  background: #e3e5e7;
  box-shadow: 3px 3px 6px #b6b9ba, -3px -3px 6px #fafafd;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.spec-option:hover {
  transform: translateY(-2px);
}

.spec-option.active {
  background: linear-gradient(145deg, #4A7C59, #3d6b4a);
  color: white;
  box-shadow: none;
}

/* 数量选择 */
.quantity-section {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 30px;
}

.quantity-label {
  font-size: 14px;
  color: #666;
}

.quantity-input {
  display: flex;
  align-items: center;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: inset 2px 2px 4px #b6b9ba, inset -2px -2px 4px #fafafd;
}

.qty-btn {
  width: 36px;
  height: 36px;
  border: none;
  background: #e3e5e7;
  cursor: pointer;
  font-size: 18px;
  transition: background 0.3s;
}

.qty-btn:hover {
  background: #d0d2d4;
}

.quantity-input input {
  width: 60px;
  height: 36px;
  border: none;
  background: #e3e5e7;
  text-align: center;
  font-size: 14px;
  outline: none;
}

.stock-info {
  font-size: 13px;
  color: #999;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.contact-btn,
.service-btn {
  flex: 1;
  padding: 12px;
  font-size: 14px;
}

.main-actions {
  display: flex;
  gap: 16px;
}

.action-btn {
  flex: 1;
  padding: 16px;
  border: none;
  border-radius: 30px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.cart-btn {
  background: #e3e5e7;
  color: #4A7C59;
  box-shadow: 4px 4px 8px #b6b9ba, -4px -4px 8px #fafafd;
}

.cart-btn:hover {
  transform: translateY(-3px);
  box-shadow: 6px 6px 12px #b6b9ba, -6px -6px 12px #fafafd;
}

.buy-btn {
  background: linear-gradient(145deg, #4A7C59, #3d6b4a);
  color: white;
}

.buy-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(74, 124, 89, 0.4);
}

.buy-btn:disabled,
.cart-btn:disabled {
  background: #ccc !important;
  color: #888 !important;
  cursor: not-allowed !important;
  box-shadow: none !important;
  transform: none !important;
}

.sold-out-notice {
  padding: 16px;
  margin-bottom: 20px;
  background: #f5f5f5;
  border-radius: 12px;
  text-align: center;
}

.sold-out-text {
  color: #999;
  font-size: 15px;
  font-weight: 500;
}

/* 商品详情 */
.detail-section {
  padding: 24px;
  margin-bottom: 24px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ddd;
}

.product-description {
  line-height: 1.8;
  color: #666;
}

.product-description :deep(h3) {
  font-size: 18px;
  color: #333;
  margin: 20px 0 12px;
}

.product-description :deep(h4) {
  font-size: 16px;
  color: #444;
  margin: 16px 0 10px;
}

.product-description :deep(ul) {
  padding-left: 20px;
}

.product-description :deep(li) {
  margin-bottom: 8px;
}

.product-description :deep(p) {
  margin-bottom: 12px;
}

/* 评价区域 */
.review-section {
  padding: 24px;
}

.review-count {
  font-size: 14px;
  font-weight: normal;
  color: #999;
}

.reviews-list {
  margin-bottom: 24px;
}

.review-item {
  padding: 20px 0;
  border-bottom: 1px solid #eee;
}

.review-item:last-child {
  border-bottom: none;
}

.review-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.review-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
}

.review-user {
  font-weight: 500;
}

.review-rating {
  flex: 1;
}

.review-time {
  color: #999;
  font-size: 13px;
}

.review-content {
  color: #666;
  line-height: 1.6;
  margin-bottom: 10px;
}

.review-images {
  display: flex;
  gap: 10px;
}

.review-images img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
}

.empty-reviews {
  text-align: center;
  padding: 40px;
  color: #999;
}

.review-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  margin-top: 20px;
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

/* 图片查看器 */
.image-viewer {
  text-align: center;
}

.image-viewer img {
  max-width: 100%;
  max-height: 80vh;
  object-fit: contain;
}

/* 响应式 */
@media (max-width: 1024px) {
  .product-main {
    grid-template-columns: 1fr;
  }

  .image-section {
    position: static;
  }
}

/* 空商品状态 */
.empty-product {
  text-align: center;
  padding: 100px 20px;
  background: white;
  border-radius: 12px;
}

.empty-product p {
  font-size: 18px;
  color: #999;
  margin-bottom: 24px;
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
</style>
