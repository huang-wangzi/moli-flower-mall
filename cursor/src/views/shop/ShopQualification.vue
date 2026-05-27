<template>
  <div class="qualification-page" v-loading="loading">
    <div class="qualification-container" v-if="!loading">
      <div class="page-header">
        <h2>资质认证</h2>
        <p>请提交真实有效的资质材料，以便通过审核</p>
      </div>

      <!-- 状态展示 -->
      <div class="status-card" :class="statusClass">
        <div class="status-icon">
          <span v-if="qualificationStatus === 0">⏳</span>
          <span v-else-if="qualificationStatus === 1">✅</span>
          <span v-else-if="qualificationStatus === 2">❌</span>
          <span v-else>📝</span>
        </div>
        <div class="status-info">
          <h3>{{ statusText }}</h3>
          <p>{{ statusDesc }}</p>
        </div>
      </div>

      <!-- 资质申请表单 - 仅未申请时显示 -->
      <div v-if="qualificationStatus === null" class="form-card">
        <div class="form-notice" style="margin-bottom: 20px;">
          <el-alert
            title="提示"
            type="info"
            description="商家入驻需进行实名认证及经营资质核验。审核通常在1-3个工作日内完成。"
            show-icon
            :closable="false"
          />
        </div>
        <h3>填写资质信息</h3>
        <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
          <el-form-item label="店铺名称" prop="shopName">
            <el-input v-model="form.shopName" placeholder="请输入店铺名称" />
          </el-form-item>
          <el-form-item label="联系电话" prop="contact">
            <el-input v-model="form.contact" placeholder="请输入联系电话" />
          </el-form-item>

          <!-- 营业执照上传 -->
          <el-form-item label="营业执照" prop="businessLicense">
            <div 
              class="upload-box" 
              :class="{ 'has-image': form.businessLicense }"
              @click="triggerUpload('businessLicense')"
            >
              <template v-if="form.businessLicense">
                <img :src="getImageUrl(form.businessLicense)" class="preview-image" @error="handleImageError" />
                <div class="image-actions">
                  <span class="action-btn" @click.stop="removeImage('businessLicense')">删除</span>
                </div>
              </template>
              <template v-else>
                <div class="upload-placeholder">
                  <span class="upload-icon">+</span>
                  <span class="upload-text">点击上传营业执照</span>
                  <span class="upload-hint">支持 JPG/PNG，最大 1MB</span>
                </div>
              </template>
              <input
                type="file"
                :ref="el => fileInputRefs.businessLicense = el"
                accept="image/jpeg,image/png,image/jpg"
                @change="(e) => handleFileSelect(e, 'businessLicense')"
                style="display:none"
              />
            </div>
            <div v-if="uploading.businessLicense" class="upload-loading">
              <span>上传中...</span>
            </div>
          </el-form-item>

          <!-- 身份证正面上传 -->
          <el-form-item label="身份证正面" prop="idCardFront">
            <div 
              class="upload-box" 
              :class="{ 'has-image': form.idCardFront }"
              @click="triggerUpload('idCardFront')"
            >
              <template v-if="form.idCardFront">
                <img :src="getImageUrl(form.idCardFront)" class="preview-image" @error="handleImageError" />
                <div class="image-actions">
                  <span class="action-btn" @click.stop="removeImage('idCardFront')">删除</span>
                </div>
              </template>
              <template v-else>
                <div class="upload-placeholder">
                  <span class="upload-icon">+</span>
                  <span class="upload-text">点击上传身份证正面</span>
                  <span class="upload-hint">支持 JPG/PNG，最大 1MB</span>
                </div>
              </template>
              <input
                type="file"
                :ref="el => fileInputRefs.idCardFront = el"
                accept="image/jpeg,image/png,image/jpg"
                @change="(e) => handleFileSelect(e, 'idCardFront')"
                style="display:none"
              />
            </div>
            <div v-if="uploading.idCardFront" class="upload-loading">
              <span>上传中...</span>
            </div>
          </el-form-item>

          <!-- 身份证背面上传 -->
          <el-form-item label="身份证背面" prop="idCardBack">
            <div 
              class="upload-box" 
              :class="{ 'has-image': form.idCardBack }"
              @click="triggerUpload('idCardBack')"
            >
              <template v-if="form.idCardBack">
                <img :src="getImageUrl(form.idCardBack)" class="preview-image" @error="handleImageError" />
                <div class="image-actions">
                  <span class="action-btn" @click.stop="removeImage('idCardBack')">删除</span>
                </div>
              </template>
              <template v-else>
                <div class="upload-placeholder">
                  <span class="upload-icon">+</span>
                  <span class="upload-text">点击上传身份证背面</span>
                  <span class="upload-hint">支持 JPG/PNG，最大 1MB</span>
                </div>
              </template>
              <input
                type="file"
                :ref="el => fileInputRefs.idCardBack = el"
                accept="image/jpeg,image/png,image/jpg"
                @change="(e) => handleFileSelect(e, 'idCardBack')"
                style="display:none"
              />
            </div>
            <div v-if="uploading.idCardBack" class="upload-loading">
              <span>上传中...</span>
            </div>
          </el-form-item>

          <!-- 品质认证上传（选填） -->
          <el-form-item label="品质认证">
            <div 
              class="upload-box" 
              :class="{ 'has-image': form.qualityCert }"
              @click="triggerUpload('qualityCert')"
            >
              <template v-if="form.qualityCert">
                <img :src="getImageUrl(form.qualityCert)" class="preview-image" @error="handleImageError" />
                <div class="image-actions">
                  <span class="action-btn" @click.stop="removeImage('qualityCert')">删除</span>
                </div>
              </template>
              <template v-else>
                <div class="upload-placeholder">
                  <span class="upload-icon">+</span>
                  <span class="upload-text">上传品质认证（选填）</span>
                  <span class="upload-hint">支持 JPG/PNG，最大 1MB</span>
                </div>
              </template>
              <input
                type="file"
                :ref="el => fileInputRefs.qualityCert = el"
                accept="image/jpeg,image/png,image/jpg"
                @change="(e) => handleFileSelect(e, 'qualityCert')"
                style="display:none"
              />
            </div>
            <div v-if="uploading.qualityCert" class="upload-loading">
              <span>上传中...</span>
            </div>
          </el-form-item>

          <!-- 提交按钮 -->
          <el-form-item>
            <el-button type="primary" @click="submitForm" :loading="submitting" :disabled="submitting" class="submit-btn">
              {{ submitting ? '提交中...' : '提交申请' }}
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 已提交待审核 -->
      <div v-else-if="qualificationStatus === 0" class="pending-card">
        <div class="pending-animation">
          <div class="clock">⏰</div>
          <div class="loading-dots">
            <span></span><span></span><span></span>
          </div>
        </div>
        <h3>资质审核中</h3>
        <p>您的资质材料已提交，请耐心等待管理员审核</p>
        <p class="apply-time">申请时间：{{ formatTime(qualificationData?.applyTime) }}</p>

        <!-- 资质详细信息展示 -->
        <div class="qualification-detail" v-if="qualificationData">
          <h4>已提交的材料</h4>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="店铺名称">{{ qualificationData.shopName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ qualificationData.contact || '-' }}</el-descriptions-item>
          </el-descriptions>

          <!-- 营业执照 -->
          <div class="image-section" v-if="qualificationData.businessLicense">
            <h5>营业执照</h5>
            <img :src="getImageUrl(qualificationData.businessLicense)" class="qualification-image" @error="handleImageError" />
          </div>

          <!-- 身份证正面 -->
          <div class="image-section" v-if="qualificationData.idCardFront">
            <h5>身份证正面</h5>
            <img :src="getImageUrl(qualificationData.idCardFront)" class="qualification-image" @error="handleImageError" />
          </div>

          <!-- 身份证背面 -->
          <div class="image-section" v-if="qualificationData.idCardBack">
            <h5>身份证背面</h5>
            <img :src="getImageUrl(qualificationData.idCardBack)" class="qualification-image" @error="handleImageError" />
          </div>

          <!-- 品质认证（选填） -->
          <div class="image-section" v-if="qualificationData.qualityCert">
            <h5>品质认证</h5>
            <img :src="getImageUrl(qualificationData.qualityCert)" class="qualification-image" @error="handleImageError" />
          </div>
        </div>
      </div>

      <!-- 已拒绝 -->
      <div v-else-if="qualificationStatus === 2" class="rejected-card">
        <div class="error-animation">❌</div>
        <h3>资质审核未通过</h3>
        <p class="reject-reason" v-if="qualificationData?.rejectReason">
          拒绝原因：{{ qualificationData.rejectReason }}
        </p>
        <p v-else>您的资质未通过审核，请修改后重新提交</p>

        <!-- 资质详细信息展示（可重新提交） -->
        <div class="qualification-detail" v-if="qualificationData">
          <h4>已提交的材料</h4>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="店铺名称">{{ qualificationData.shopName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ qualificationData.contact || '-' }}</el-descriptions-item>
            <el-descriptions-item label="申请时间">{{ formatTime(qualificationData.applyTime) }}</el-descriptions-item>
            <el-descriptions-item label="审核时间">{{ formatTime(qualificationData.auditTime) }}</el-descriptions-item>
          </el-descriptions>

          <!-- 营业执照 -->
          <div class="image-section" v-if="qualificationData.businessLicense">
            <h5>营业执照</h5>
            <img :src="getImageUrl(qualificationData.businessLicense)" class="qualification-image" @error="handleImageError" />
          </div>

          <!-- 身份证正面 -->
          <div class="image-section" v-if="qualificationData.idCardFront">
            <h5>身份证正面</h5>
            <img :src="getImageUrl(qualificationData.idCardFront)" class="qualification-image" @error="handleImageError" />
          </div>

          <!-- 身份证背面 -->
          <div class="image-section" v-if="qualificationData.idCardBack">
            <h5>身份证背面</h5>
            <img :src="getImageUrl(qualificationData.idCardBack)" class="qualification-image" @error="handleImageError" />
          </div>

          <!-- 品质认证（选填） -->
          <div class="image-section" v-if="qualificationData.qualityCert">
            <h5>品质认证</h5>
            <img :src="getImageUrl(qualificationData.qualityCert)" class="qualification-image" @error="handleImageError" />
          </div>
        </div>

        <el-button type="primary" @click="resetForm" class="resubmit-btn">重新提交</el-button>
      </div>

      <!-- 审核通过 -->
      <div v-else-if="qualificationStatus === 1" class="approved-card">
        <div class="success-animation">🎉</div>
        <h3>资质认证已通过</h3>
        <p>恭喜！您的店铺资质已通过审核，现在可以发布商品了</p>
        <el-button type="success" @click="$router.push('/shop/products')" class="success-btn">去发布商品</el-button>

        <!-- 资质详细信息展示 -->
        <div class="qualification-detail" v-if="qualificationData">
          <h4>资质信息</h4>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="店铺名称">{{ qualificationData.shopName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ qualificationData.contact || '-' }}</el-descriptions-item>
            <el-descriptions-item label="申请时间">{{ formatTime(qualificationData.applyTime) }}</el-descriptions-item>
            <el-descriptions-item label="审核时间">{{ formatTime(qualificationData.auditTime) }}</el-descriptions-item>
          </el-descriptions>

          <!-- 营业执照 -->
          <div class="image-section" v-if="qualificationData.businessLicense">
            <h5>营业执照</h5>
            <img :src="getImageUrl(qualificationData.businessLicense)" class="qualification-image" @error="handleImageError" />
          </div>

          <!-- 身份证正面 -->
          <div class="image-section" v-if="qualificationData.idCardFront">
            <h5>身份证正面</h5>
            <img :src="getImageUrl(qualificationData.idCardFront)" class="qualification-image" @error="handleImageError" />
          </div>

          <!-- 身份证背面 -->
          <div class="image-section" v-if="qualificationData.idCardBack">
            <h5>身份证背面</h5>
            <img :src="getImageUrl(qualificationData.idCardBack)" class="qualification-image" @error="handleImageError" />
          </div>

          <!-- 品质认证（选填） -->
          <div class="image-section" v-if="qualificationData.qualityCert">
            <h5>品质认证</h5>
            <img :src="getImageUrl(qualificationData.qualityCert)" class="qualification-image" @error="handleImageError" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyQualification, submitQualification, uploadQualificationImage } from '@/api'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo)

const qualificationStatus = ref(null)
const qualificationData = ref(null)
const submitting = ref(false)
const formRef = ref(null)
const uploading = reactive({})
const loading = ref(true)

const loadQualification = async () => {
  if (!userInfo.value?.id) {
    loading.value = false
    return
  }
  
  loading.value = true
  try {
    const res = await getMyQualification(userInfo.value.id)
    if (res.code === 200 && res.data) {
      qualificationData.value = res.data
      qualificationStatus.value = res.data.status
      
      // 同步更新本地状态（可选）
      if (userInfo.value.shopQualificationStatus !== res.data.status + 1) {
        // 后端状态映射：0->1, 1->2, 2->3
        userStore.updateUserInfo({
          ...userInfo.value,
          shopQualificationStatus: res.data.status === 1 ? 2 : (res.data.status === 0 ? 1 : 3)
        })
      }
    } else {
      qualificationStatus.value = null
      qualificationData.value = null
    }
  } catch (error) {
    console.error('获取资质信息失败:', error)
    qualificationStatus.value = null
  } finally {
    loading.value = false
  }
}

const formatTime = (time) => {
  if (!time) return '-'
  try {
    const date = new Date(time)
    return date.toLocaleString('zh-CN')
  } catch {
    return time
  }
}

// 监听用户信息变化，确保在获取到 ID 后加载资质
watch(() => userInfo.value?.id, (newId) => {
  if (newId) {
    loadQualification()
  }
}, { immediate: true })

const form = ref({
  shopName: '',
  contact: '',
  businessLicense: '',
  idCardFront: '',
  idCardBack: '',
  qualityCert: ''
})

const fileInputRefs = reactive({})

const rules = {
  shopName: [{ required: true, message: '请输入店铺名称', trigger: 'blur' }],
  contact: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  businessLicense: [{ required: true, message: '请上传营业执照', trigger: 'change' }],
  idCardFront: [{ required: true, message: '请上传身份证正面', trigger: 'change' }],
  idCardBack: [{ required: true, message: '请上传身份证背面', trigger: 'change' }]
}

const statusTextMap = {
  0: '待审核',
  1: '已通过',
  2: '已拒绝',
  null: '未申请'
}

const statusDescMap = {
  0: '您的资质正在审核中，请耐心等待',
  1: '您的资质已通过审核，可以发布商品了',
  2: '您的资质未通过审核，请重新提交',
  null: '请提交资质材料完成认证'
}

const statusText = computed(() => statusTextMap[qualificationStatus.value] || '未申请')
const statusDesc = computed(() => statusDescMap[qualificationStatus.value] || '请提交资质材料完成认证')
const statusClass = computed(() => ({
  'status-pending': qualificationStatus.value === 0,
  'status-approved': qualificationStatus.value === 1,
  'status-rejected': qualificationStatus.value === 2,
  'status-none': qualificationStatus.value === null
}))

const getImageUrl = (path) => {
  if (!path) return ''
  if (path.startsWith('http')) return path
  // 如果是 Base64 格式，直接返回
  if (path.startsWith('data:')) return path
  // 否则作为文件路径处理
  return `/uploads/${path}`
}

const handleImageError = (e) => {
  e.target.src = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
}

// 重新提交申请（重置状态显示表单）
const resetForm = () => {
  qualificationStatus.value = null
  qualificationData.value = null
}

const triggerUpload = (field) => {
  if (fileInputRefs[field]) {
    fileInputRefs[field].click()
  }
}

const compressImage = (file, maxWidth = 1000, maxSizeMB = 1) => {
  return new Promise((resolve, reject) => {
    // 检查文件大小
    if (file.size > maxSizeMB * 1024 * 1024) {
      // 需要压缩
      const reader = new FileReader()
      reader.onload = (e) => {
        const img = new Image()
        img.onload = () => {
          const canvas = document.createElement('canvas')
          let width = img.width
          let height = img.height
          
          // 按比例缩放
          if (width > maxWidth) {
            height = (height * maxWidth) / width
            width = maxWidth
          }
          
          canvas.width = width
          canvas.height = height
          const ctx = canvas.getContext('2d')
          ctx.drawImage(img, 0, 0, width, height)
          
          // 输出为 JPEG，质量和大小平衡
          const dataUrl = canvas.toDataURL('image/jpeg', 0.8)
          
          // 检查压缩后的大小
          const sizeInMB = (dataUrl.length - dataUrl.indexOf(',') - 1) * 0.75 / 1024 / 1024
          if (sizeInMB > maxSizeMB) {
            // 进一步压缩
            const compressedDataUrl = canvas.toDataURL('image/jpeg', 0.5)
            resolve(compressedDataUrl)
          } else {
            resolve(dataUrl)
          }
        }
        img.onerror = reject
        img.src = e.target.result
      }
      reader.onerror = reject
      reader.readAsDataURL(file)
    } else {
      // 文件大小合适，但检查尺寸
      const reader = new FileReader()
      reader.onload = (e) => {
        const img = new Image()
        img.onload = () => {
          if (img.width > maxWidth || img.height > maxWidth) {
            const canvas = document.createElement('canvas')
            let width = img.width
            let height = img.height
            
            if (width > maxWidth) {
              height = (height * maxWidth) / width
              width = maxWidth
            }
            
            canvas.width = width
            canvas.height = height
            const ctx = canvas.getContext('2d')
            ctx.drawImage(img, 0, 0, width, height)
            resolve(canvas.toDataURL('image/jpeg', 0.8))
          } else {
            resolve(e.target.result)
          }
        }
        img.onerror = reject
        img.src = e.target.result
      }
      reader.onerror = reject
      reader.readAsDataURL(file)
    }
  })
}

const handleFileSelect = async (event, field) => {
  const file = event.target.files[0]
  if (!file) return

  // 验证文件类型
  const isImage = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/jpg'
  if (!isImage) {
    ElMessage.error('只能上传 JPG/PNG 格式的图片')
    event.target.value = ''
    return
  }

  uploading.value = true
  if (!uploading[field]) uploading[field] = true

  try {
    // 压缩图片
    const dataUrl = await compressImage(file)
    
    // 直接使用 Base64 格式（简化处理）
    form.value[field] = dataUrl
    ElMessage.success('上传成功')
  } catch (error) {
    console.error('上传失败:', error)
    ElMessage.error('上传失败，请重试')
  } finally {
    uploading[field] = false
    event.target.value = ''
  }
}

const removeImage = (field) => {
  form.value[field] = ''
}

const submitForm = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  // 防重复提交检查
  if (submitting.value) {
    ElMessage.warning('正在提交中，请勿重复点击')
    return
  }

  submitting.value = true
  try {
    const res = await submitQualification({
      userId: userInfo.value.id,
      shopName: form.value.shopName,
      contact: form.value.contact,
      businessLicense: form.value.businessLicense,
      idCardFront: form.value.idCardFront,
      idCardBack: form.value.idCardBack,
      qualityCert: form.value.qualityCert
    })

    if (res.code === 200) {
      ElMessage.success('资质申请已提交，请等待审核')
      
      // 重新加载资质信息，这会自动设置正确的 qualificationStatus (0-待审核)
      await loadQualification()
      
      // 重置表单数据
      form.value = {
        shopName: '',
        contact: '',
        businessLicense: '',
        idCardFront: '',
        idCardBack: '',
        qualityCert: ''
      }
    } else {
      ElMessage.error(res.message || '提交失败')
    }
  } catch (error) {
    console.error('提交资质失败:', error)
    ElMessage.error('提交失败，请重试')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  // 资质加载现在由 watcher 处理
})
</script>

<style scoped>
.qualification-page {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.qualification-container {
  max-width: 700px;
  margin: 0 auto;
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
}

.page-header h2 {
  font-size: 24px;
  color: #333;
  margin-bottom: 8px;
}

.subtitle {
  color: #666;
  font-size: 14px;
}

.status-card {
  display: flex;
  align-items: center;
  padding: 24px;
  background: white;
  border-radius: 12px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.status-icon {
  font-size: 48px;
  margin-right: 20px;
}

.status-info h3 {
  font-size: 18px;
  margin-bottom: 4px;
  color: #333;
}

.status-info p {
  color: #666;
  font-size: 14px;
}

.status-pending { border-left: 4px solid #faad14; }
.status-approved { border-left: 4px solid #52c41a; }
.status-rejected { border-left: 4px solid #f5222d; }
.status-none { border-left: 4px solid #1890ff; }

.form-card, .pending-card, .approved-card {
  background: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.form-card h3, .pending-card h3, .approved-card h3 {
  font-size: 18px;
  margin-bottom: 24px;
  color: #333;
}

.pending-card, .approved-card {
  text-align: center;
  padding: 50px 30px;
}

/* 虚线框上传样式 */
.upload-box {
  position: relative;
  width: 100%;
  height: 160px;
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  overflow: hidden;
  background: #fafafa;
}

.upload-box:hover {
  border-color: #4A7C59;
  background: #f0f9f0;
}

.upload-box.has-image {
  border-style: solid;
  border-color: #d9d9d9;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.upload-icon {
  font-size: 40px;
  color: #bfbfbf;
  font-weight: 300;
  line-height: 1;
}

.upload-text {
  font-size: 14px;
  color: #666;
}

.upload-hint {
  font-size: 12px;
  color: #999;
}

.upload-loading {
  margin-top: 8px;
  color: #999;
  font-size: 12px;
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  position: absolute;
  top: 0;
  left: 0;
}

.image-actions {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  justify-content: center;
  gap: 20px;
  padding: 8px;
  opacity: 0;
  transition: opacity 0.3s;
}

.upload-box:hover .image-actions {
  opacity: 1;
}

.action-btn {
  color: white;
  font-size: 13px;
  cursor: pointer;
  padding: 4px 12px;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.2);
}

.action-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

.submit-btn {
  width: 200px;
  height: 44px;
  font-size: 16px;
  border-radius: 22px;
  background: #4A7C59;
  border-color: #4A7C59;
}

.submit-btn:hover {
  background: #5a9469;
  border-color: #5a9469;
}

.submit-btn:disabled {
  background: #a0c9a8;
  border-color: #a0c9a8;
  cursor: not-allowed;
}

/* 待审核提示 */
.pending-notice {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px 20px;
  background: linear-gradient(135deg, #fff8e1, #fff3cd);
  border: 1px solid #ffe082;
  border-radius: 12px;
  font-size: 14px;
  color: #856404;
}

.pending-notice .notice-icon {
  font-size: 24px;
}

/* 待审核动画 */
.pending-animation {
  margin-bottom: 20px;
}

.clock {
  font-size: 64px;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

.loading-dots {
  display: flex;
  justify-content: center;
  gap: 6px;
  margin-top: 16px;
}

.loading-dots span {
  width: 8px;
  height: 8px;
  background: #faad14;
  border-radius: 50%;
  animation: bounce 1.4s infinite ease-in-out both;
}

.loading-dots span:nth-child(1) { animation-delay: -0.32s; }
.loading-dots span:nth-child(2) { animation-delay: -0.16s; }

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

.apply-time {
  color: #999;
  font-size: 13px;
  margin-top: 16px;
}

/* 审核通过 */
.success-animation {
  font-size: 80px;
  margin-bottom: 20px;
  animation: celebrate 1s ease-out;
}

@keyframes celebrate {
  0% { transform: scale(0) rotate(-180deg); }
  50% { transform: scale(1.2) rotate(10deg); }
  100% { transform: scale(1) rotate(0deg); }
}

.success-btn {
  margin-top: 20px;
  padding: 12px 40px;
  font-size: 16px;
  border-radius: 24px;
  background: #52c41a;
  border-color: #52c41a;
}

.success-btn:hover {
  background: #73d13d;
  border-color: #73d13d;
}

/* 资质详情展示 */
.qualification-detail {
  margin-top: 30px;
  text-align: left;
  padding: 20px;
  background: #f9f9f9;
  border-radius: 8px;
}

.qualification-detail h4 {
  font-size: 16px;
  color: #333;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e8e8e8;
}

.qualification-detail h5 {
  font-size: 14px;
  color: #666;
  margin: 16px 0 8px;
}

.qualification-image {
  width: 100%;
  max-width: 400px;
  height: auto;
  border-radius: 8px;
  border: 1px solid #e8e8e8;
}

.image-section {
  margin-top: 12px;
}

/* 拒绝状态 */
.rejected-card {
  text-align: center;
  padding: 50px 30px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.error-animation {
  font-size: 80px;
  margin-bottom: 20px;
}

.reject-reason {
  color: #f5222d;
  font-weight: 500;
  margin-bottom: 20px;
}

.resubmit-btn {
  margin-top: 20px;
  padding: 12px 40px;
  font-size: 16px;
  border-radius: 24px;
  background: #4A7C59;
  border-color: #4A7C59;
}

.resubmit-btn:hover {
  background: #5a9469;
  border-color: #5a9469;
}

.pending-card p, .approved-card p {
  color: #666;
  font-size: 14px;
}
</style>
