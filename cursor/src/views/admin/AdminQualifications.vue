<template>
  <div class="qualification-page">
    <div class="page-header">
      <h2>资质核验</h2>
      <p class="subtitle">审核商家提交的资质材料，包括营业执照、身份证等</p>
    </div>

    <!-- 横向状态标签筛选 -->
    <div class="status-tabs">
      <div class="status-tab" :class="{ active: statusFilter === '' }" @click="statusFilter = ''; loadData()">
        全部<span class="tab-count">{{ getStatusCount('') }}</span>
      </div>
      <div class="status-tab" :class="{ active: statusFilter === 0 }" @click="statusFilter = 0; loadData()">
        待审核<span class="tab-count warning">{{ getStatusCount(0) }}</span>
      </div>
      <div class="status-tab" :class="{ active: statusFilter === 1 }" @click="statusFilter = 1; loadData()">
        已通过<span class="tab-count success">{{ getStatusCount(1) }}</span>
      </div>
      <div class="status-tab" :class="{ active: statusFilter === 2 }" @click="statusFilter = 2; loadData()">
        已拒绝<span class="tab-count danger">{{ getStatusCount(2) }}</span>
      </div>
    </div>

    <!-- 表格容器 -->
    <div class="table-container">
      <!-- 待审核列表 -->
      <div v-if="pendingList.length > 0 && (statusFilter === '' || statusFilter === 0)" class="pending-section">
        <h3 class="section-title">待审核 ({{ pendingList.length }})</h3>
        <div class="qualification-cards">
          <div v-for="item in pendingList" :key="item.id" class="qualification-card">
            <div class="card-header">
              <span class="shop-name">{{ item.shopName || '未填写店铺名' }}</span>
              <el-tag type="warning" size="small">待审核</el-tag>
            </div>
            <div class="card-info">
              <div class="info-row">
                <span class="label">用户ID：</span>
                <span class="value">{{ item.userId }}</span>
              </div>
              <div class="info-row">
                <span class="label">联系电话：</span>
                <span class="value">{{ item.contact || '-' }}</span>
              </div>
              <div class="info-row">
                <span class="label">申请时间：</span>
                <span class="value">{{ formatTime(item.applyTime) }}</span>
              </div>
            </div>
            <div class="card-images">
              <div class="image-item" @click="previewImage(item.businessLicense)">
                <span class="image-label">营业执照</span>
                <div v-if="item.businessLicense" class="image-preview">
                  <img :src="getImageUrl(item.businessLicense)" @error="handleImageError" alt="营业执照" />
                </div>
                <span v-else class="no-image">未上传</span>
              </div>
              <div class="image-item" @click="previewImage(item.idCardFront)">
                <span class="image-label">身份证正面</span>
                <div v-if="item.idCardFront" class="image-preview">
                  <img :src="getImageUrl(item.idCardFront)" @error="handleImageError" alt="身份证正面" />
                </div>
                <span v-else class="no-image">未上传</span>
              </div>
              <div class="image-item" @click="previewImage(item.idCardBack)">
                <span class="image-label">身份证背面</span>
                <div v-if="item.idCardBack" class="image-preview">
                  <img :src="getImageUrl(item.idCardBack)" @error="handleImageError" alt="身份证背面" />
                </div>
                <span v-else class="no-image">未上传</span>
              </div>
              <div class="image-item" @click="previewImage(item.qualityCert)">
                <span class="image-label">品质认证</span>
                <div v-if="item.qualityCert" class="image-preview">
                  <img :src="getImageUrl(item.qualityCert)" @error="handleImageError" alt="品质认证" />
                </div>
                <span v-else class="no-image">选填</span>
              </div>
            </div>
            <div class="card-actions">
              <el-button type="success" size="small" @click="audit(item, 1)">通过</el-button>
              <el-button type="danger" size="small" @click="showRejectDialog(item)">拒绝</el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 已审核列表 -->
      <div v-if="reviewedList.length > 0 && (statusFilter === '' || statusFilter === 2 || statusFilter === 3)" class="reviewed-section">
        <h3 class="section-title">已审核 ({{ reviewedList.length }})</h3>
        <el-table :data="filteredReviewedList" border style="width: 100%;" v-loading="loading">
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="shopName" label="店铺名称" min-width="120">
            <template #default="{ row }">
              {{ row.shopName || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="userId" label="用户ID" width="80" />
          <el-table-column prop="contact" label="联系电话" width="120" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                {{ row.status === 1 ? '已通过' : '已拒绝' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="申请时间" width="160">
            <template #default="{ row }">
              {{ formatTime(row.applyTime) }}
            </template>
          </el-table-column>
          <el-table-column label="审核时间" width="160">
            <template #default="{ row }">
              {{ formatTime(row.auditTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="rejectReason" label="拒绝原因" min-width="150" show-overflow-tooltip>
            <template #default="{ row }">
              {{ row.rejectReason || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" link @click="showDetail(row)">查看详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 无数据提示 -->
      <el-empty v-if="qualificationList.length === 0 && !loading" description="暂无资质申请记录" />
    </div>

    <!-- 拒绝原因对话框 -->
    <el-dialog v-model="rejectDialogVisible" title="拒绝资质申请" width="400px">
      <el-form :model="rejectForm" label-width="80px">
        <el-form-item label="拒绝原因">
          <el-input v-model="rejectForm.reason" type="textarea" :rows="4" placeholder="请输入拒绝原因，帮助商家了解需要改进的地方" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject">确认拒绝</el-button>
      </template>
    </el-dialog>

    <!-- 资质详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="资质详情" width="700px">
      <div v-if="currentItem" class="detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="店铺名称">{{ currentItem.shopName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="用户ID">{{ currentItem.userId }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentItem.contact || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentItem.status === 1 ? 'success' : 'danger'" size="small">
              {{ currentItem.status === 1 ? '已通过' : '已拒绝' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ formatTime(currentItem.applyTime) }}</el-descriptions-item>
          <el-descriptions-item label="审核时间">{{ formatTime(currentItem.auditTime) }}</el-descriptions-item>
          <el-descriptions-item label="拒绝原因" :span="2">{{ currentItem.rejectReason || '-' }}</el-descriptions-item>
        </el-descriptions>

        <div class="detail-images">
          <h4>资质材料</h4>
          <div class="image-grid">
            <div class="image-item">
              <span class="image-label">营业执照</span>
              <el-image 
                v-if="currentItem.businessLicense"
                :src="getImageUrl(currentItem.businessLicense)" 
                :preview-src-list="[getImageUrl(currentItem.businessLicense)]"
                fit="contain"
                style="width: 100%; height: 150px; border: 1px solid #eee; border-radius: 4px;"
              />
              <span v-else class="no-image">未上传</span>
            </div>
            <div class="image-item">
              <span class="image-label">身份证正面</span>
              <el-image 
                v-if="currentItem.idCardFront"
                :src="getImageUrl(currentItem.idCardFront)" 
                :preview-src-list="[getImageUrl(currentItem.idCardFront)]"
                fit="contain"
                style="width: 100%; height: 150px; border: 1px solid #eee; border-radius: 4px;"
              />
              <span v-else class="no-image">未上传</span>
            </div>
            <div class="image-item">
              <span class="image-label">身份证背面</span>
              <el-image 
                v-if="currentItem.idCardBack"
                :src="getImageUrl(currentItem.idCardBack)" 
                :preview-src-list="[getImageUrl(currentItem.idCardBack)]"
                fit="contain"
                style="width: 100%; height: 150px; border: 1px solid #eee; border-radius: 4px;"
              />
              <span v-else class="no-image">未上传</span>
            </div>
            <div class="image-item">
              <span class="image-label">品质认证</span>
              <el-image 
                v-if="currentItem.qualityCert"
                :src="getImageUrl(currentItem.qualityCert)" 
                :preview-src-list="[getImageUrl(currentItem.qualityCert)]"
                fit="contain"
                style="width: 100%; height: 150px; border: 1px solid #eee; border-radius: 4px;"
              />
              <span v-else class="no-image">未上传（选填）</span>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 图片预览对话框 -->
    <el-dialog v-model="previewDialogVisible" title="图片预览" width="800px">
      <div class="preview-container">
        <el-image 
          v-if="previewUrl"
          :src="previewUrl" 
          fit="contain"
          style="width: 100%; height: 500px;"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getQualificationAll, auditQualification } from '@/api'

const loading = ref(false)
const statusFilter = ref('')
const qualificationList = ref([])

const rejectDialogVisible = ref(false)
const rejectForm = ref({ reason: '' })
const currentRejectItem = ref(null)

const detailDialogVisible = ref(false)
const currentItem = ref(null)

const previewDialogVisible = ref(false)
const previewUrl = ref('')

// 过滤待审核列表
const pendingList = computed(() => {
  return qualificationList.value.filter(item => item.status === 0)
})

// 过滤已审核列表（status=1:通过，status=2:拒绝）
const reviewedList = computed(() => {
  return qualificationList.value.filter(item => item.status === 1 || item.status === 2)
})

// 过滤后的已审核列表
const filteredReviewedList = computed(() => {
  if (statusFilter.value === '') return reviewedList.value
  // 资质记录状态: 1=通过，2=拒绝
  return reviewedList.value.filter(item => item.status === parseInt(statusFilter.value))
})

const getStatusCount = (status) => {
  if (status === '') return qualificationList.value.length
  if (status === 0) return pendingList.value.length
  // 资质记录状态: 1=通过，2=拒绝
  return reviewedList.value.filter(item => item.status === status).length
}

const getImageUrl = (path) => {
  if (!path) return ''
  if (path.startsWith('http')) return path
  if (path.startsWith('data:')) return path
  return `/uploads/${path}`
}

const handleImageError = (e) => {
  e.target.src = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
}

const formatTime = (time) => {
  if (!time) return '-'
  try {
    const date = new Date(time)
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
  } catch {
    return time
  }
}

const previewImage = (path) => {
  if (path) {
    previewUrl.value = getImageUrl(path)
    previewDialogVisible.value = true
  }
}

const loadData = async () => {
  loading.value = true
  try {
    // 使用获取全部记录的接口，包含待审核、已通过、已拒绝的所有记录
    const res = await getQualificationAll()
    if (res.code === 200) {
      qualificationList.value = res.data || []
    } else {
      ElMessage.error(res.message || '加载失败')
    }
  } catch (error) {
    console.error('加载资质列表失败:', error)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const showRejectDialog = (item) => {
  currentRejectItem.value = item
  rejectForm.value.reason = ''
  rejectDialogVisible.value = true
}

const confirmReject = async () => {
  if (!rejectForm.value.reason.trim()) {
    ElMessage.warning('请输入拒绝原因')
    return
  }
  try {
    // 拒绝状态: 2
    const res = await auditQualification(
      currentRejectItem.value.id, 
      2, // 拒绝状态
      rejectForm.value.reason, 
      getAdminId()
    )
    if (res.code === 200) {
      ElMessage.success('已拒绝')
      rejectDialogVisible.value = false
      loadData()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    console.error('拒绝失败:', error)
    ElMessage.error('操作失败')
  }
}

const audit = async (item, status) => {
  const msg = status === 1 ? '通过' : '拒绝'
  try {
    await ElMessageBox.confirm(`确定要${msg}该资质申请吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: status === 1 ? 'success' : 'warning'
    })
    // 状态值：1=通过，2=拒绝
    const res = await auditQualification(item.id, status, '', getAdminId())
    if (res.code === 200) {
      ElMessage.success(`已${msg}`)
      loadData()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      console.error('审核失败', e)
    }
  }
}

const showDetail = (item) => {
  currentItem.value = item
  detailDialogVisible.value = true
}

const getAdminId = () => {
  try {
    const userData = JSON.parse(localStorage.getItem('moli_mall_current_user') || '{}')
    return userData.userInfo?.id
  } catch {
    return null
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.qualification-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 横向状态标签筛选 */
.status-tabs { 
  display: flex; 
  gap: 12px; 
  margin-bottom: 20px; 
  padding: 12px 16px; 
  background: #f8f9fa; 
  border-radius: 12px; 
  overflow-x: auto; 
}

.status-tab { 
  display: flex; 
  align-items: center; 
  gap: 8px; 
  padding: 10px 20px; 
  border-radius: 24px; 
  background: white; 
  cursor: pointer; 
  transition: all 0.3s; 
  white-space: nowrap; 
  font-size: 14px; 
  color: #666; 
  box-shadow: 0 2px 8px rgba(0,0,0,0.06); 
  border: 1px solid #eee;
}

.status-tab:hover { 
  background: #f0f0f0; 
  border-color: #d0d0d0;
}

.status-tab.active { 
  background: linear-gradient(145deg, #4A7C59, #3d6b4a); 
  color: white;
  border-color: #4A7C59;
}

.tab-count { 
  padding: 2px 8px; 
  border-radius: 10px; 
  font-size: 12px; 
  background: #f0f0f0; 
  color: #666; 
}

.status-tab.active .tab-count { 
  background: rgba(255,255,255,0.3); 
  color: white; 
}

.tab-count.warning { background: #fff3e0; color: #faad14; }
.tab-count.success { background: #e8f5e9; color: #4A7C59; }
.tab-count.danger { background: #ffe0e0; color: #e74c3c; }

/* 待审核卡片 */
.qualification-cards { display: grid; grid-template-columns: repeat(auto-fill, minmax(400px, 1fr)); gap: 16px; margin-bottom: 30px; }
.qualification-card { border: 1px solid #eee; border-radius: 12px; padding: 16px; background: #fafafa; transition: all 0.3s; }
.qualification-card:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.shop-name { font-size: 16px; font-weight: 500; color: #333; }
.card-info { margin-bottom: 12px; }
.info-row { display: flex; font-size: 14px; margin-bottom: 4px; }
.info-row .label { color: #666; min-width: 80px; }
.info-row .value { color: #333; }

.card-images { display: grid; grid-template-columns: repeat(4, 1fr); gap: 8px; margin-bottom: 12px; }
.image-item { display: flex; flex-direction: column; gap: 4px; cursor: pointer; }
.image-label { font-size: 12px; color: #666; }
.image-preview { width: 100%; height: 60px; border: 1px solid #ddd; border-radius: 8px; overflow: hidden; transition: transform 0.3s; }
.image-preview:hover { transform: scale(1.02); }
.image-preview img { width: 100%; height: 100%; object-fit: cover; }
.no-image { font-size: 11px; color: #999; text-align: center; padding: 20px 0; background: #f5f5f5; border-radius: 8px; }

.card-actions { display: flex; justify-content: flex-end; gap: 8px; }

/* 详情对话框 */
.detail-content { padding: 10px 0; }
.detail-images { margin-top: 20px; }
.detail-images h4 { margin-bottom: 12px; color: #333; font-size: 16px; }
.image-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px; }
.image-grid .image-item { display: flex; flex-direction: column; gap: 8px; }
.image-grid .image-label { font-size: 14px; color: #666; font-weight: 500; }
.image-grid .no-image { color: #999; font-size: 13px; text-align: center; padding: 50px 0; background: #f5f5f5; border-radius: 8px; }

/* 图片预览 */
.preview-container { display: flex; justify-content: center; align-items: center; }

.pending-section, .reviewed-section { margin-bottom: 24px; }
.section-title { font-size: 16px; color: #333; margin-bottom: 16px; padding-bottom: 8px; border-bottom: 1px solid #eee; }
</style>
