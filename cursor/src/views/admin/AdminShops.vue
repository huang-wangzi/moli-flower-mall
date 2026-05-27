<template>
  <div class="admin-shops">
    <div class="page-header">
      <h2>商家审核</h2>
      <p class="subtitle">管理商家账号注册申请，包括收购商和普通商家</p>
    </div>

    <div class="status-tabs">
      <div class="status-tab" :class="{ active: statusFilter === '' }" @click="statusFilter = ''; loadShops()">
        全部<span class="tab-count">{{ getStatusCount(null) }}</span>
      </div>
      <div class="status-tab" :class="{ active: statusFilter === 0 }" @click="statusFilter = 0; loadShops()">
        未核验<span class="tab-count warning">{{ getStatusCount(0) }}</span>
      </div>
      <div class="status-tab" :class="{ active: statusFilter === 2 }" @click="statusFilter = 2; loadShops()">
        待审核<span class="tab-count warning">{{ getStatusCount(2) }}</span>
      </div>
      <div class="status-tab" :class="{ active: statusFilter === 1 }" @click="statusFilter = 1; loadShops()">
        已通过<span class="tab-count success">{{ getStatusCount(1) }}</span>
      </div>
      <div class="status-tab" :class="{ active: statusFilter === 3 }" @click="statusFilter = 3; loadShops()">
        已拒绝<span class="tab-count danger">{{ getStatusCount(3) }}</span>
      </div>
    </div>

    <div class="table-container">
      <el-table :data="shops" border v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column label="申请类型" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.applyType === 'acquirer' || row.shopType === 2" type="warning" size="small">收购商</el-tag>
            <el-tag v-else type="success" size="small">商家</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="shopName" label="店铺名称" width="150">
          <template #default="{ row }">
            <span v-if="row.shopName">{{ row.shopName }}</span>
            <span v-else style="color: #999;">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="联系电话" width="120" />
        <el-table-column label="账号状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getAccountStatusTagType(row.status)">{{ getAccountStatusName(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="资质状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getQualificationStatusTagType(row.shopQualificationStatus)">
              {{ getQualificationStatusName(row.shopQualificationStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="160">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" type="primary" size="small" @click="showQualification(row)">查看资质</el-button>
            <el-button v-if="row.status === 0" type="success" size="small" @click="approve(row)">通过</el-button>
            <el-button v-if="row.status === 0" type="danger" size="small" @click="reject(row)">拒绝</el-button>
            <el-button v-if="row.status === 2" type="warning" size="small" @click="showQualification(row)">审核资质</el-button>
            <el-button v-if="row.status === 2" type="success" size="small" @click="approve(row)">通过</el-button>
            <el-button v-if="row.status === 2" type="danger" size="small" @click="reject(row)">拒绝</el-button>
            <el-button v-if="row.status === 1" type="info" size="small" @click="showQualification(row)">查看详情</el-button>
            <el-button v-if="row.status === 1" type="warning" size="small" @click="disableShop(row)">禁用</el-button>
            <el-button v-if="row.status === 3" type="info" size="small" @click="showQualification(row)">查看详情</el-button>
            <el-button v-if="row.status === 3" type="success" size="small" @click="approve(row)">重新通过</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="loadShops"
        @current-change="loadShops"
      />
    </div>

    <!-- 资质详情对话框 -->
    <el-dialog v-model="qualificationDialogVisible" title="商家资质详情" width="700px" :close-on-click-modal="false">
      <div v-if="currentShop" class="qualification-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="用户名">{{ currentShop.username }}</el-descriptions-item>
          <el-descriptions-item label="店铺名称">{{ currentShop.shopName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentShop.phone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="申请类型">
            <el-tag v-if="currentShop.shopType === 2" type="warning" size="small">收购商</el-tag>
            <el-tag v-else type="success" size="small">商家</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="账号状态">
            <el-tag :type="getAccountStatusTagType(currentShop.status)">{{ getAccountStatusName(currentShop.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="资质状态">
            <el-tag :type="getQualificationStatusTagType(currentShop.shopQualificationStatus)">
              {{ getQualificationStatusName(currentShop.shopQualificationStatus) }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <div v-if="qualificationData" class="qualification-images">
          <h4>资质材料</h4>
          <div class="image-grid">
            <div class="image-item">
              <span class="image-label">营业执照</span>
              <el-image v-if="qualificationData.businessLicense" :src="getImageUrl(qualificationData.businessLicense)" :preview-src-list="[getImageUrl(qualificationData.businessLicense)]" fit="contain" style="width: 100%; height: 150px; border: 1px solid #eee; border-radius: 4px;" />
              <span v-else class="no-image">未上传</span>
            </div>
            <div class="image-item">
              <span class="image-label">身份证正面</span>
              <el-image v-if="qualificationData.idCardFront" :src="getImageUrl(qualificationData.idCardFront)" :preview-src-list="[getImageUrl(qualificationData.idCardFront)]" fit="contain" style="width: 100%; height: 150px; border: 1px solid #eee; border-radius: 4px;" />
              <span v-else class="no-image">未上传</span>
            </div>
            <div class="image-item">
              <span class="image-label">身份证背面</span>
              <el-image v-if="qualificationData.idCardBack" :src="getImageUrl(qualificationData.idCardBack)" :preview-src-list="[getImageUrl(qualificationData.idCardBack)]" fit="contain" style="width: 100%; height: 150px; border: 1px solid #eee; border-radius: 4px;" />
              <span v-else class="no-image">未上传</span>
            </div>
            <div class="image-item">
              <span class="image-label">品质认证</span>
              <el-image v-if="qualificationData.qualityCert" :src="getImageUrl(qualificationData.qualityCert)" :preview-src-list="[getImageUrl(qualificationData.qualityCert)]" fit="contain" style="width: 100%; height: 150px; border: 1px solid #eee; border-radius: 4px;" />
              <span v-else class="no-image">未上传（选填）</span>
            </div>
          </div>
        </div>
        <div v-else class="no-qualification">
          <p>该商家尚未提交资质材料</p>
        </div>
      </div>
      <template #footer>
        <el-button @click="qualificationDialogVisible = false">关闭</el-button>
        <el-button v-if="qualificationData && qualificationData.status === 0" type="success" @click="auditQualification(1)">通过资质</el-button>
        <el-button v-if="qualificationData && qualificationData.status === 0" type="danger" @click="showRejectReason">拒绝资质</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="rejectDialogVisible" title="拒绝申请" width="400px">
      <el-form :model="rejectForm" label-width="80px">
        <el-form-item label="拒绝原因">
          <el-input v-model="rejectForm.reason" type="textarea" :rows="3" placeholder="请输入拒绝原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject">确认拒绝</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="qualificationRejectDialogVisible" title="拒绝资质" width="400px">
      <el-form :model="qualificationRejectForm" label-width="80px">
        <el-form-item label="拒绝原因">
          <el-input v-model="qualificationRejectForm.reason" type="textarea" :rows="3" placeholder="请输入拒绝原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="qualificationRejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmQualificationReject">确认拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminShops, approveShop, rejectShop, updateUserStatus, getMyQualification, auditQualification as apiAuditQualification } from '@/api'

const loading = ref(false)
const statusFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const allShops = ref([])
const shops = ref([])

const qualificationDialogVisible = ref(false)
const qualificationRejectDialogVisible = ref(false)
const currentShop = ref(null)
const qualificationData = ref(null)
const qualificationRejectForm = ref({ reason: '' })

const rejectDialogVisible = ref(false)
const rejectForm = ref({ reason: '' })
const currentRejectShop = ref(null)

const getAccountStatusName = (status) => ({ 0: '未核验', 1: '已通过', 2: '待审核', 3: '已拒绝' })[status] || '未知'
const getAccountStatusTagType = (status) => ({ 1: 'success', 2: 'warning', 3: 'danger', 0: 'info' })[status] || 'info'

const getQualificationStatusName = (status) => {
  if (status === null || status === undefined) return '未申请'
  return ({ 0: '待审核', 1: '通过', 2: '已通过', 3: '已拒绝' })[status] || '未知'
}
const getQualificationStatusTagType = (status) => {
  if (status === null || status === undefined) return 'info'
  return ({ 1: 'success', 2: 'success', 3: 'danger', 0: 'warning' })[status] || 'info'
}

const formatTime = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

const getStatusCount = (status) => {
  if (status === null) return allShops.value.length
  return allShops.value.filter(s => s.status === status).length
}

const getImageUrl = (path) => {
  if (!path) return ''
  if (path.startsWith('http')) return path
  if (path.startsWith('data:')) return path
  return `/uploads/${path}`
}

const loadShops = async () => {
  loading.value = true
  try {
    const params = { pageNum: currentPage.value, pageSize: pageSize.value }
    const res = await getAdminShops(params)
    if (res.code === 200) {
      allShops.value = res.data.records || []
      total.value = res.data.total || 0
      shops.value = statusFilter.value !== ''
        ? allShops.value.filter(s => s.status === parseInt(statusFilter.value))
        : allShops.value
    }
  } catch (error) {
    console.error('加载商家列表失败', error)
  } finally {
    loading.value = false
  }
}

const showQualification = async (shop) => {
  currentShop.value = shop
  qualificationData.value = null
  qualificationDialogVisible.value = true
  try {
    const res = await getMyQualification(shop.id)
    if (res.code === 200 && res.data) {
      qualificationData.value = res.data
    }
  } catch (error) {
    console.error('加载资质详情失败', error)
  }
}

const approve = async (shop) => {
  try {
    await ElMessageBox.confirm(`确定要通过 ${shop.username} 的申请吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'success'
    })
    const res = await approveShop(shop.id)
    if (res.code === 200) {
      shop.status = 1
      ElMessage.success('已通过审核')
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      console.error('通过失败', e)
    }
  }
}

const reject = async (shop) => {
  currentRejectShop.value = shop
  rejectForm.value.reason = ''
  rejectDialogVisible.value = true
}

const confirmReject = async () => {
  if (!rejectForm.value.reason.trim()) {
    ElMessage.warning('请输入拒绝原因')
    return
  }
  try {
    const res = await rejectShop(currentRejectShop.value.id, rejectForm.value.reason)
    if (res.code === 200) {
      currentRejectShop.value.status = 3
      ElMessage.success('已拒绝申请')
      rejectDialogVisible.value = false
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    console.error('拒绝失败', error)
    ElMessage.error('操作失败')
  }
}

const disableShop = async (shop) => {
  try {
    await ElMessageBox.confirm('确定要禁用该账号吗？禁用后商家将无法登录。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await updateUserStatus(shop.id, 0)
    if (res.code === 200) {
      shop.status = 0
      ElMessage.success('账号已禁用')
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      console.error('禁用失败', e)
    }
  }
}

const showRejectReason = () => {
  qualificationRejectForm.value.reason = ''
  qualificationRejectDialogVisible.value = true
}

const confirmQualificationReject = async () => {
  if (!qualificationRejectForm.value.reason.trim()) {
    ElMessage.warning('请输入拒绝原因')
    return
  }
  try {
    const res = await apiAuditQualification(
      qualificationData.value.id,
      3,
      qualificationRejectForm.value.reason,
      getAdminId()
    )
    if (res.code === 200) {
      qualificationData.value.status = 3
      ElMessage.success('已拒绝资质')
      qualificationRejectDialogVisible.value = false
      currentShop.value.shopQualificationStatus = 3
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    console.error('拒绝资质失败', error)
    ElMessage.error('操作失败')
  }
}

const auditQualification = async (status) => {
  try {
    const backendStatus = status === 1 ? 2 : 3
    const res = await apiAuditQualification(
      qualificationData.value.id,
      backendStatus,
      '',
      getAdminId()
    )
    if (res.code === 200) {
      qualificationData.value.status = backendStatus
      ElMessage.success(status === 1 ? '已通过资质' : '已拒绝资质')
      currentShop.value.shopQualificationStatus = backendStatus
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    console.error('审核资质失败', error)
    ElMessage.error('操作失败')
  }
}

const getAdminId = () => {
  try {
    const userData = JSON.parse(localStorage.getItem('moli_mall_current_user') || '{}')
    return userData.userInfo?.id
  } catch {
    return null
  }
}

onMounted(() => { loadShops() })
</script>

<style scoped>
.admin-shops {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 横向状态标签筛选 */
.status-tabs {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  padding: 16px 20px;
  background: #fafbfc;
  border-radius: 12px;
  flex-wrap: wrap;
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
  font-size: 14px;
  color: #666;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  border: 1px solid transparent;
  white-space: nowrap;
}

.status-tab:hover {
  background: #f0f0f0;
}

.status-tab.active {
  background: linear-gradient(135deg, #4A7C59, #3d6b4a);
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
  background: rgba(255, 255, 255, 0.25);
  color: white;
}

.tab-count.success { background: #e8f5e9; color: #4A7C59; }
.tab-count.warning { background: #fff3e0; color: #faad14; }
.tab-count.danger { background: #ffe0e0; color: #e74c3c; }

/* 表格容器 */
.table-container {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.qualification-detail { padding: 10px 0; }
.qualification-images { margin-top: 20px; }
.qualification-images h4 { margin-bottom: 12px; color: #333; font-size: 15px; }
.image-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px; }
.image-item { display: flex; flex-direction: column; gap: 8px; }
.image-label { font-size: 13px; color: #666; font-weight: 500; }
.no-image { color: #999; font-size: 12px; text-align: center; padding: 50px 0; background: #f5f5f5; border-radius: 4px; }
.no-qualification { text-align: center; padding: 40px; color: #999; }
.no-qualification p { margin: 0; }
</style>
