<template>
  <div class="acquisition-detail-page">
    <!-- 返回按钮 -->
    <div class="back-bar">
      <el-button text @click="$router.back()">
        <span>← 返回</span>
      </el-button>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <el-icon class="is-loading"><Loading /></el-icon>
      <p>加载中...</p>
    </div>

    <!-- 详情内容 -->
    <div v-else-if="demand" class="detail-content">
      <!-- 需求信息卡片 -->
      <div class="info-card neumorphic-card">
        <!-- 头部 -->
        <div class="card-header">
          <div class="shop-info">
            <span class="shop-avatar">{{ demand.shopName?.charAt(0) || '收' }}</span>
            <div class="shop-detail">
              <span class="shop-name">{{ demand.shopName }}</span>
              <span class="shop-date">收购日期：{{ formatDate(demand.demandDate) }}</span>
            </div>
          </div>
          <span class="status-badge" :class="getStatusClass(demand.status)">
            {{ getStatusText(demand.status) }}
          </span>
        </div>

        <!-- 标题 -->
        <h1 class="demand-title">{{ demand.title }}</h1>

        <!-- 价格信息 -->
        <div class="price-section">
          <div class="price-item">
            <span class="price-label">最低价</span>
            <span class="price-value">¥{{ demand.priceMin }}</span>
          </div>
          <div class="price-divider">—</div>
          <div class="price-item">
            <span class="price-label">最高价</span>
            <span class="price-value">¥{{ demand.priceMax }}</span>
          </div>
          <div class="price-unit">元 / {{ demand.unit }}</div>
        </div>

        <!-- 需求量进度 -->
        <div class="quantity-section">
          <div class="quantity-header">
            <span>收购进度</span>
            <span class="quantity-percent">{{ getProgress(demand) }}%</span>
          </div>
          <el-progress
            :percentage="getProgress(demand)"
            :stroke-width="12"
            :show-text="false"
            color="#4A7C59"
          />
          <div class="quantity-detail">
            <span>已收：{{ subtract(demand.quantityNeeded, demand.quantityRemaining) }} {{ demand.unit }}</span>
            <span>剩余：{{ demand.quantityRemaining }} {{ demand.unit }}</span>
            <span>计划：{{ demand.quantityNeeded }} {{ demand.unit }}</span>
          </div>
        </div>

        <!-- 详细信息 -->
        <div class="detail-grid">
          <div class="detail-item">
            <span class="detail-icon">📍</span>
            <div class="detail-content">
              <span class="detail-label">市场地址</span>
              <span class="detail-value">{{ demand.marketAddress }}</span>
            </div>
          </div>
          <div class="detail-item">
            <span class="detail-icon">📞</span>
            <div class="detail-content">
              <span class="detail-label">联系电话</span>
              <span class="detail-value">{{ demand.contactPhone }}</span>
            </div>
          </div>
          <div class="detail-item" v-if="demand.contactName">
            <span class="detail-icon">👤</span>
            <div class="detail-content">
              <span class="detail-label">联系人</span>
              <span class="detail-value">{{ demand.contactName }}</span>
            </div>
          </div>
        </div>

        <!-- 备注 -->
        <div class="remark-section" v-if="demand.description">
          <h3>备注说明</h3>
          <p>{{ demand.description }}</p>
        </div>

        <!-- 联系收购商 -->
        <div class="contact-section">
          <el-button type="primary" plain @click="contactMerchant" class="contact-btn">
            <el-icon><ChatDotRound /></el-icon>
            联系收购商
          </el-button>
          <el-button plain @click="callMerchant" class="call-btn" v-if="demand.contactPhone">
            <el-icon><Phone /></el-icon>
            电话联系
          </el-button>
        </div>
      </div>

      <!-- 申请按钮 -->
      <div class="action-section" v-if="demand.status === 1">
        <el-button
          type="primary"
          size="large"
          class="apply-btn"
          @click="applyForDemand"
          :disabled="hasApplied"
        >
          {{ hasApplied ? '您已申请' : '申请供货' }}
        </el-button>
      </div>

      <!-- 已申请提示 -->
      <div class="applied-tip" v-if="hasApplied">
        <el-icon><InfoFilled /></el-icon>
        <span>您已申请过此收购需求，请等待收购商审核</span>
      </div>
    </div>

    <!-- 未找到 -->
    <div v-else class="empty-state">
      <div class="empty-icon">🔍</div>
      <p class="empty-text">未找到该收购需求</p>
      <el-button type="primary" @click="$router.push('/acquisition')">
        返回列表
      </el-button>
    </div>

    <!-- 申请对话框 -->
    <el-dialog
      v-model="applyDialogVisible"
      title="申请供货"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="applyFormRef"
        :model="applyForm"
        :rules="applyRules"
        label-width="100px"
      >
        <el-form-item label="茉莉花斤数" prop="quantity">
          <el-input-number
            v-model="applyForm.quantity"
            :min="1"
            :max="demand?.quantityRemaining || 999"
            :step="1"
            :precision="0"
            style="width: 100%"
          />
          <span class="form-tip">当前需求剩余 {{ demand?.quantityRemaining }} 斤</span>
        </el-form-item>

        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="applyForm.contactPhone" placeholder="请输入联系电话" />
        </el-form-item>

        <el-form-item label="茉莉花照片">
          <div class="photo-upload">
            <el-upload
              action="#"
              :http-request="uploadPhoto"
              :before-upload="beforePhotoUpload"
              list-type="picture-card"
              :file-list="photoList"
              :on-remove="handlePhotoRemove"
              :on-error="handleUploadError"
            >
              <el-icon><Plus /></el-icon>
            </el-upload>
            <div class="upload-tip">上传今日采摘的茉莉花照片（最多3张）</div>
          </div>
        </el-form-item>

        <el-form-item label="备注说明">
          <el-input
            v-model="applyForm.remark"
            type="textarea"
            :rows="3"
            placeholder="可填写茉莉花品种、质量等信息"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="applyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitApplication" :loading="submitting">
          提交申请
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading, InfoFilled, Plus, ChatDotRound, Phone } from '@element-plus/icons-vue'
import { getAcquisitionDemandDetail, checkDemandApplied, createAcquisitionApplication } from '@/api/acquisition'
import { uploadImage } from '@/api'

const route = useRoute()
const router = useRouter()

// ==================== 数据定义 ====================

const loading = ref(false)
const demand = ref(null)
const hasApplied = ref(false)

// 申请对话框
const applyDialogVisible = ref(false)
const submitting = ref(false)
const applyFormRef = ref(null)
const applyForm = ref({
  quantity: 10,
  contactPhone: '',
  photoUrls: '',
  remark: ''
})
const photoList = ref([])

const applyRules = {
  quantity: [
    { required: true, message: '请输入茉莉花斤数', trigger: 'blur' }
  ],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

// ==================== 方法 ====================

/**
 * 加载需求详情
 */
const loadDemandDetail = async () => {
  loading.value = true
  const id = route.params.id
  try {
    const res = await getAcquisitionDemandDetail(id)
    if (res.code === 200 && res.data) {
      demand.value = res.data
      // 检查是否已申请
      await checkApplicationStatus()
    } else {
      demand.value = null
    }
  } catch (error) {
    console.error('加载需求详情失败:', error)
    demand.value = null
  } finally {
    loading.value = false
  }
}

/**
 * 检查申请状态
 */
const checkApplicationStatus = async () => {
  try {
    const res = await checkDemandApplied(demand.value.id)
    if (res.code === 200) {
      hasApplied.value = res.data
    }
  } catch (error) {
    console.error('检查申请状态失败:', error)
  }
}

/**
 * 申请供货
 */
const applyForDemand = () => {
  if (!demand.value) return
  applyForm.value = {
    demandId: demand.value.id,
    quantity: 10,
    contactPhone: '',
    photoUrls: '',
    remark: ''
  }
  photoList.value = []
  applyDialogVisible.value = true
}

/**
 * 上传照片
 */
const uploadPhoto = async (options) => {
  const { file, onSuccess, onError } = options
  try {
    const res = await uploadImage(file)
    console.log('[上传图片] 响应:', res)
    if (res.code === 200) {
      // 从响应中提取 URL
      const url = res.data || res.message || ''
      if (url) {
        // 手动维护 photoList（不依赖 el-upload 内部状态）
        photoList.value.push({ url, name: file.name, uid: file.uid })
        applyForm.value.photoUrls = JSON.stringify(photoList.value.map(p => p.url))
        // el-upload 期望响应中有 url 字段
        onSuccess({ url, ...res }, file)
      } else {
        onError(new Error('服务器返回的URL为空'))
      }
    } else {
      onError(new Error(res.message || '上传失败'))
    }
  } catch (error) {
    console.error('上传错误:', error)
    onError(error)
  }
}

const beforePhotoUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }
  if (photoList.value.length >= 3) {
    ElMessage.error('最多上传3张照片')
    return false
  }
  return true
}

const handlePhotoRemove = (file) => {
  const index = photoList.value.findIndex(f => f.uid === file.uid)
  if (index > -1) {
    photoList.value.splice(index, 1)
    applyForm.value.photoUrls = JSON.stringify(photoList.value.map(p => p.url))
  }
}

const handleUploadError = (err, file) => {
  console.error('上传失败:', err)
  ElMessage.error('图片上传失败，请重试')
}

/**
 * 提交申请
 */
const submitApplication = async () => {
  try {
    await applyFormRef.value.validate()
  } catch {
    return
  }

  submitting.value = true
  try {
    const res = await createAcquisitionApplication({
      demandId: demand.value.id,
      quantity: applyForm.value.quantity,
      contactPhone: applyForm.value.contactPhone,
      photoUrls: applyForm.value.photoUrls,
      remark: applyForm.value.remark
    })

    if (res.code === 200) {
      ElMessage.success('申请提交成功，等待收购商审核')
      applyDialogVisible.value = false
      hasApplied.value = true
      await loadDemandDetail()
      // 提示用户查看申请记录
      ElMessageBox.confirm('是否前往申请记录查看？', '提交成功', {
        confirmButtonText: '查看记录',
        cancelButtonText: '留在本页',
        type: 'success'
      }).then(() => {
        router.push('/acquisition/my-records')
      }).catch(() => {})
    } else {
      ElMessage.error(res.message || '提交失败')
    }
  } catch (error) {
    ElMessage.error(error.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

/**
 * 计算进度
 */
const getProgress = (d) => {
  if (!d.quantityNeeded) return 100
  const remaining = Number(d.quantityRemaining || 0)
  const needed = Number(d.quantityNeeded || 0)
  if (needed === 0) return 100
  return Math.round(((needed - remaining) / needed) * 100)
}

const subtract = (a, b) => {
  return (Number(a) - Number(b)).toFixed(1)
}

/**
 * 获取状态
 */
const getStatusClass = (status) => {
  switch (status) {
    case 1: return 'status-active'   // 收购中
    case 0: return 'status-offline'  // 已下架
    case 2: return 'status-ended'    // 已结束
    default: return ''
  }
}

const getStatusText = (status) => {
  switch (status) {
    case 1: return '收购中'
    case 0: return '已下架'
    case 2: return '已结束'
    default: return '未知'
  }
}

/**
 * 格式化日期
 */
const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

/**
 * 联系收购商（跳转聊天）
 */
const contactMerchant = () => {
  if (!demand.value) return
  router.push({
    path: `/chat/${demand.value.shopId}`
  })
}

/**
 * 电话联系
 */
const callMerchant = () => {
  if (demand.value?.contactPhone) {
    window.location.href = `tel:${demand.value.contactPhone}`
  }
}

// ==================== 生命周期 ====================

onMounted(() => {
  loadDemandDetail()
})
</script>

<style scoped>
.acquisition-detail-page {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

/* 返回按钮 */
.back-bar {
  margin-bottom: 20px;
}

.back-bar .el-button {
  color: #4A7C59;
  font-size: 14px;
}

/* 加载状态 */
.loading-state {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}

.loading-state .el-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

/* 信息卡片 */
.info-card {
  padding: 24px;
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.shop-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.shop-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #4A7C59 0%, #6B9B7A 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: bold;
}

.shop-detail {
  display: flex;
  flex-direction: column;
}

.shop-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.shop-date {
  font-size: 12px;
  color: #999;
}

.status-badge {
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}

.status-active {
  background: rgba(74, 124, 89, 0.1);
  color: #4A7C59;
}

.status-offline {
  background: rgba(153, 153, 153, 0.1);
  color: #999;
}

.status-ended {
  background: rgba(76, 175, 80, 0.1);
  color: #4caf50;
}

/* 标题 */
.demand-title {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin-bottom: 20px;
}

/* 价格区域 */
.price-section {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
  padding: 20px;
  background: linear-gradient(135deg, #fff9e6 0%, #fff 100%);
  border-radius: 12px;
  margin-bottom: 20px;
}

.price-item {
  text-align: center;
}

.price-label {
  display: block;
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}

.price-value {
  font-size: 24px;
  font-weight: bold;
  color: #e74c3c;
}

.price-divider {
  font-size: 20px;
  color: #ccc;
}

.price-unit {
  font-size: 14px;
  color: #999;
}

/* 数量区域 */
.quantity-section {
  margin-bottom: 20px;
}

.quantity-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  font-size: 14px;
  color: #666;
}

.quantity-percent {
  font-weight: bold;
  color: #4A7C59;
}

.quantity-detail {
  display: flex;
  justify-content: space-between;
  margin-top: 10px;
  font-size: 12px;
  color: #999;
}

/* 详细信息 */
.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.detail-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  background: #f5f5f5;
  border-radius: 8px;
}

.detail-icon {
  font-size: 24px;
}

.detail-content {
  display: flex;
  flex-direction: column;
}

.detail-label {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}

.detail-value {
  font-size: 14px;
  color: #333;
}

/* 备注 */
.remark-section {
  padding-top: 16px;
  border-top: 1px solid #eee;
}

.remark-section h3 {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.remark-section p {
  font-size: 14px;
  color: #333;
  line-height: 1.6;
}

/* 操作区域 */
.action-section {
  margin-bottom: 16px;
}

.apply-btn {
  width: 100%;
  height: 50px;
  font-size: 18px;
  background: linear-gradient(135deg, #4A7C59 0%, #6B9B7A 100%);
  border: none;
  border-radius: 25px;
}

.apply-btn:hover {
  transform: scale(1.02);
}

.applied-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: rgba(33, 150, 243, 0.1);
  border-radius: 8px;
  color: #2196f3;
  font-size: 14px;
}

/* 联系收购商 */
.contact-section {
  display: flex;
  gap: 12px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #eee;
}

.contact-btn,
.call-btn {
  flex: 1;
  height: 44px;
  border-radius: 22px;
}

.contact-btn {
  background: linear-gradient(135deg, #4A7C59 0%, #6B9B7A 100%);
  color: white;
  border: none;
}

.call-btn {
  border: 1px solid #4A7C59;
  color: #4A7C59;
}

/* 表单 */
.form-tip {
  display: block;
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.photo-upload {
  width: 100%;
}

.upload-tip {
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.empty-text {
  font-size: 18px;
  color: #666;
  margin-bottom: 20px;
}

/* 响应式 */
@media (max-width: 768px) {
  .price-section {
    flex-wrap: wrap;
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>
