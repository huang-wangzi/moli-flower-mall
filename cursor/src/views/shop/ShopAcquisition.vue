<template>
  <div class="shop-acquisition-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">收购需求管理</h1>
      <p class="page-subtitle">发布茉莉花收购需求，等待花农申请供货</p>
    </div>

    <!-- 今日统计 -->
    <div class="today-stats">
      <div class="stat-item">
        <span class="stat-icon">📅</span>
        <span class="stat-text">今日收购需求</span>
        <span class="stat-value">{{ todayCount }} 个</span>
      </div>
      <div class="stat-item">
        <span class="stat-icon">📋</span>
        <span class="stat-text">待处理申请</span>
        <span class="stat-value">{{ pendingCount }} 个</span>
      </div>
    </div>

    <!-- 操作栏 -->
    <div class="action-bar">
      <el-button type="primary" @click="showCreateDialog">
        <span>+ 发布收购需求</span>
      </el-button>
      <el-radio-group v-model="statusFilter" @change="loadDemands">
        <el-radio-button value="">全部</el-radio-button>
        <el-radio-button value="1">收购中</el-radio-button>
        <el-radio-button value="0">已下架</el-radio-button>
        <el-radio-button value="2">已结束</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <el-icon class="is-loading"><Loading /></el-icon>
      <p>加载中...</p>
    </div>

    <!-- 需求列表 -->
    <div v-else-if="demands.length > 0" class="demand-list">
      <div
        v-for="demand in demands"
        :key="demand.id"
        class="demand-card neumorphic-card"
        :class="{ 'demand-ended': demand.status !== 1 }"
      >
        <!-- 卡片头部 -->
        <div class="card-header">
          <div class="header-left">
            <h3 class="demand-title">{{ demand.title }}</h3>
            <span class="demand-date">{{ formatDate(demand.demandDate) }}</span>
          </div>
          <div class="header-right">
            <span class="status-badge" :class="getStatusClass(demand.status)">
              {{ getStatusText(demand.status) }}
            </span>
          </div>
        </div>

        <!-- 价格和数量 -->
        <div class="info-grid">
          <div class="info-item">
            <span class="info-label">价格区间</span>
            <span class="info-value price">{{ demand.priceMin }} - {{ demand.priceMax }} 元/{{ demand.unit }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">计划收购量</span>
            <span class="info-value">{{ demand.quantityNeeded }} {{ demand.unit }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">剩余收购量</span>
            <span class="info-value remaining">{{ demand.quantityRemaining }} {{ demand.unit }}</span>
          </div>
        </div>

        <!-- 进度 -->
        <div class="progress-section">
          <el-progress
            :percentage="getProgress(demand)"
            :stroke-width="10"
            :show-text="false"
            :color="demand.status === 1 ? '#4A7C59' : '#999'"
          />
          <div class="progress-text">
            <span>已完成 {{ getProgress(demand) }}%</span>
            <span>剩余 {{ demand.quantityRemaining }} 斤</span>
          </div>
        </div>

        <!-- 市场信息 -->
        <div class="location-info">
          <span class="location-icon">📍</span>
          <span>{{ demand.marketAddress }}</span>
          <span class="contact-info">📞 {{ demand.contactPhone }}</span>
        </div>

        <!-- 操作按钮 -->
        <div class="card-actions">
          <el-button size="small" @click="viewApplications(demand)">
            查看申请 ({{ getApplicationCount(demand) }})
          </el-button>
          <el-button size="small" @click="editDemand(demand)">编辑</el-button>
          <el-button
            v-if="demand.status === 1"
            size="small"
            type="warning"
            @click="offShelfDemand(demand)"
          >
            下架
          </el-button>
          <el-button size="small" type="danger" @click="deleteDemand(demand)">删除</el-button>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="empty-state">
      <div class="empty-icon">📦</div>
      <p class="empty-text">暂无收购需求</p>
      <p class="empty-hint">点击上方按钮发布今日收购需求</p>
    </div>

    <!-- 分页 -->
    <div class="pagination-wrapper" v-if="total > pageSize">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="loadDemands"
      />
    </div>

    <!-- 创建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑收购需求' : '发布收购需求'"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="需求标题" prop="title">
          <el-input v-model="form.title" placeholder="如：今日收购茉莉鲜花" />
        </el-form-item>

        <el-form-item label="收购日期" prop="demandDate">
          <el-date-picker
            v-model="form.demandDate"
            type="date"
            placeholder="选择收购日期"
            style="width: 100%"
            :disabled-date="disabledDate"
          />
        </el-form-item>

        <el-form-item label="价格区间" required>
          <div class="price-range">
            <el-form-item prop="priceMin">
              <el-input-number
                v-model="form.priceMin"
                :min="0"
                :precision="2"
                placeholder="最低价"
                style="width: 100%"
              />
              <span class="price-unit">元/斤（最低）</span>
            </el-form-item>
            <span class="range-divider">—</span>
            <el-form-item prop="priceMax">
              <el-input-number
                v-model="form.priceMax"
                :min="0"
                :precision="2"
                placeholder="最高价"
                style="width: 100%"
              />
              <span class="price-unit">元/斤（最高）</span>
            </el-form-item>
          </div>
        </el-form-item>

        <el-form-item label="收购数量" prop="quantityNeeded">
          <el-input-number
            v-model="form.quantityNeeded"
            :min="1"
            :step="10"
            :precision="Number(0)"
            style="width: 100%"
          />
          <span class="form-tip">计划收购的茉莉花总量</span>
        </el-form-item>

        <el-form-item label="市场地址" prop="marketAddress">
          <el-input v-model="form.marketAddress" placeholder="如：横州茉莉花交易市场" />
        </el-form-item>

        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
        </el-form-item>

        <el-form-item label="联系人" prop="contactName">
          <el-input v-model="form.contactName" placeholder="请输入联系人姓名" />
        </el-form-item>

        <el-form-item label="备注说明">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="可填写收购要求、质量标准等信息"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">
          {{ isEdit ? '保存修改' : '立即发布' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 申请列表对话框 -->
    <el-dialog
      v-model="applicationsDialogVisible"
      :title="`申请列表 - ${currentDemand?.title}`"
      width="800px"
    >
      <div v-if="loadingApplications" class="loading-state">
        <el-icon class="is-loading"><Loading /></el-icon>
        <p>加载中...</p>
      </div>

      <div v-else-if="applications.length > 0" class="application-list">
        <div
          v-for="app in applications"
          :key="app.id"
          class="application-card"
          :class="{ 'app-pending': app.status === 0, 'app-agreed': app.status === 1, 'app-completed': app.status === 3 }"
        >
          <div class="app-header">
            <div class="farmer-info">
              <span class="farmer-name">{{ app.farmerName || app.userNickname || '花农' }}</span>
              <span class="apply-time">{{ formatDateTime(app.createdAt) }}</span>
            </div>
            <span class="status-tag" :style="{ background: getAppStatusColor(app.status) }">
              {{ getAppStatusText(app.status) }}
            </span>
          </div>

          <div class="app-content">
            <div class="app-info">
              <span class="info-label">申请数量</span>
              <span class="info-value">{{ app.quantity }} 斤</span>
            </div>
            <div class="app-info" v-if="app.contactPhone">
              <span class="info-label">联系电话</span>
              <span class="info-value">{{ app.contactPhone }}</span>
            </div>
            <div class="app-info" v-if="app.remark">
              <span class="info-label">备注</span>
              <span class="info-value">{{ app.remark }}</span>
            </div>
            <div class="app-info" v-if="app.status === 3">
              <span class="info-label">实际成交</span>
              <span class="info-value success">¥{{ app.totalAmount }} ({{ app.actualQuantity }}斤 × ¥{{ app.actualPrice }})</span>
            </div>
          </div>

          <!-- 照片 -->
          <div class="app-photos" v-if="app.photoUrls">
            <span class="photo-label">茉莉花照片</span>
            <div class="photo-list">
              <el-image
                v-for="(url, index) in parsePhotos(app.photoUrls)"
                :key="index"
                :src="normalizeUrl(url)"
                :preview-src-list="parsePhotos(app.photoUrls).map(normalizeUrl)"
                fit="cover"
                class="photo-item"
              />
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="app-actions" v-if="app.status === 0">
            <el-button type="primary" size="small" @click="agreeApplication(app)">
              同意供货
            </el-button>
            <el-button type="danger" size="small" @click="rejectApplication(app)">
              拒绝
            </el-button>
          </div>

          <!-- 完成交付 -->
          <div class="app-actions" v-if="app.status === 1">
            <el-button type="success" size="small" @click="showCompleteDialog(app)">
              确认收货
            </el-button>
          </div>
        </div>
      </div>

      <div v-else class="empty-state small">
        <p>暂无申请</p>
      </div>
    </el-dialog>

    <!-- 完成交付对话框 -->
    <el-dialog
      v-model="completeDialogVisible"
      title="确认收货"
      width="400px"
    >
      <el-form
        ref="completeFormRef"
        :model="completeForm"
        :rules="completeRules"
        label-width="100px"
      >
        <el-form-item label="申请数量">
          <span>{{ currentApplication?.quantity }} 斤</span>
        </el-form-item>

        <el-form-item label="实际斤数" prop="actualQuantity">
          <el-input-number
            v-model="completeForm.actualQuantity"
            :min="0"
            :max="currentApplication?.quantity"
            :step="1"
            :precision="Number(0)"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="成交单价" prop="actualPrice">
          <el-input-number
            v-model="completeForm.actualPrice"
            :min="0"
            :precision="2"
            style="width: 100%"
          />
          <span class="form-tip">元/斤（在需求的价格区间内）</span>
        </el-form-item>

        <el-form-item label="应付金额">
          <span class="total-amount">¥{{ calculateTotal() }}</span>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="completeDialogVisible = false">取消</el-button>
        <el-button type="success" @click="submitComplete" :loading="submitting">
          确认完成
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
  getMerchantAcquisitionDemands,
  getTodayDemandCount,
  createMerchantDemand,
  updateMerchantDemand,
  deleteMerchantDemand,
  offShelfDemand as apiOffShelfDemand,
  getDemandApplications,
  agreeApplication as apiAgreeApplication,
  rejectApplication as apiRejectApplication,
  completeDelivery
} from '@/api/acquisition'
import { normalizeUploadUrl } from '@/utils/imageUrl'

// ==================== 数据定义 ====================

const loading = ref(false)
const demands = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const statusFilter = ref('')
const todayCount = ref(0)
const pendingCount = ref(0)

// 创建/编辑对话框
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const form = ref({
  id: null,
  title: '',
  demandDate: new Date(),
  priceMin: null,
  priceMax: null,
  quantityNeeded: null,
  marketAddress: '',
  contactPhone: '',
  contactName: '',
  description: ''
})

const rules = {
  title: [{ required: true, message: '请输入需求标题', trigger: 'blur' }],
  demandDate: [{ required: true, message: '请选择收购日期', trigger: 'change' }],
  priceMin: [{ required: true, message: '请输入最低价', trigger: 'blur' }],
  priceMax: [{ required: true, message: '请输入最高价', trigger: 'blur' }],
  quantityNeeded: [{ required: true, message: '请输入收购数量', trigger: 'blur' }],
  marketAddress: [{ required: true, message: '请输入市场地址', trigger: 'blur' }],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

// 申请列表对话框
const applicationsDialogVisible = ref(false)
const loadingApplications = ref(false)
const currentDemand = ref(null)
const applications = ref([])
const applicationCounts = ref({}) // 存储每个需求的申请数量

// 完成交付对话框
const completeDialogVisible = ref(false)
const completeFormRef = ref(null)
const currentApplication = ref(null)
const completeForm = ref({
  actualQuantity: null,
  actualPrice: null
})

const completeRules = {
  actualQuantity: [{ required: true, message: '请输入实际斤数', trigger: 'blur' }],
  actualPrice: [{ required: true, message: '请输入成交单价', trigger: 'blur' }]
}

// ==================== 方法 ====================

/**
 * 加载需求列表
 */
const loadDemands = async () => {
  loading.value = true
  try {
    const res = await getMerchantAcquisitionDemands(currentPage.value, pageSize.value)
    if (res.code === 200 && res.data) {
      let records = res.data.records || res.data || []
      // 筛选
      if (statusFilter.value !== '') {
        records = records.filter(d => d.status === Number(statusFilter.value))
      }
      demands.value = records
      total.value = res.data.total || records.length
    } else {
      demands.value = []
    }
  } catch (error) {
    console.error('加载需求列表失败:', error)
    demands.value = []
  } finally {
    loading.value = false
  }
}

/**
 * 加载今日统计
 */
const loadTodayStats = async () => {
  try {
    const res = await getTodayDemandCount()
    if (res.code === 200) {
      todayCount.value = res.data || 0
    }
  } catch (error) {
    console.error('加载今日统计失败:', error)
  }
  // 暂时硬编码，待处理申请数
  pendingCount.value = 0
}

/**
 * 显示创建对话框
 */
const showCreateDialog = () => {
  isEdit.value = false
  form.value = {
    id: null,
    title: '',
    demandDate: new Date(),
    priceMin: null,
    priceMax: null,
    quantityNeeded: null,
    marketAddress: '',
    contactPhone: '',
    contactName: '',
    description: ''
  }
  dialogVisible.value = true
}

/**
 * 编辑需求
 */
const editDemand = (demand) => {
  isEdit.value = true
  form.value = {
    id: demand.id,
    title: demand.title,
    demandDate: new Date(demand.demandDate),
    priceMin: Number(demand.priceMin),
    priceMax: Number(demand.priceMax),
    quantityNeeded: Number(demand.quantityNeeded),
    marketAddress: demand.marketAddress,
    contactPhone: demand.contactPhone,
    contactName: demand.contactName,
    description: demand.description
  }
  dialogVisible.value = true
}

/**
 * 提交表单
 */
const submitForm = async () => {
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  // 验证价格
  if (form.value.priceMin > form.value.priceMax) {
    ElMessage.error('最低价不能大于最高价')
    return
  }

  submitting.value = true
  try {
    const data = {
      ...form.value,
      demandDate: formatDateForApi(form.value.demandDate)
    }

    let res
    if (isEdit.value) {
      res = await updateMerchantDemand(form.value.id, data)
    } else {
      res = await createMerchantDemand(data)
    }

    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '修改成功' : '发布成功')
      dialogVisible.value = false
      loadDemands()
      loadTodayStats()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

/**
 * 下架需求
 */
const offShelfDemand = async (demand) => {
  try {
    await ElMessageBox.confirm('确定要下架该收购需求吗？', '下架确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const res = await apiOffShelfDemand(demand.id)
    if (res.code === 200) {
      ElMessage.success('已下架')
      loadDemands()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

/**
 * 删除需求
 */
const deleteDemand = async (demand) => {
  try {
    await ElMessageBox.confirm('确定要删除该收购需求吗？删除后无法恢复！', '删除确认', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const res = await deleteMerchantDemand(demand.id)
    if (res.code === 200) {
      ElMessage.success('已删除')
      loadDemands()
      loadTodayStats()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

/**
 * 查看申请
 */
const viewApplications = async (demand) => {
  currentDemand.value = demand
  applicationsDialogVisible.value = true
  await loadApplications(demand.id)
}

/**
 * 加载申请列表
 */
const loadApplications = async (demandId) => {
  loadingApplications.value = true
  try {
    const res = await getDemandApplications(demandId)
    if (res.code === 200 && res.data) {
      applications.value = res.data.records || res.data || []
      applicationCounts.value[demandId] = applications.value.length
    } else {
      applications.value = []
    }
  } catch (error) {
    console.error('加载申请列表失败:', error)
    applications.value = []
  } finally {
    loadingApplications.value = false
  }
}

/**
 * 获取申请数量
 */
const getApplicationCount = (demand) => {
  return applicationCounts.value[demand.id] || 0
}

/**
 * 同意申请
 */
const agreeApplication = async (app) => {
  try {
    await ElMessageBox.confirm(
      `确定同意 ${app.farmerName || '该花农'} 的供货申请吗？`,
      '同意供货',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'info' }
    )

    const res = await apiAgreeApplication(app.id)
    if (res.code === 200) {
      ElMessage.success('已同意，等待花农送货')
      loadApplications(currentDemand.value.id)
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

/**
 * 拒绝申请
 */
const rejectApplication = async (app) => {
  try {
    await ElMessageBox.prompt('请输入拒绝原因（选填）', '拒绝申请', {
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    }).then(({ value }) => {
      doReject(app.id, value)
    }).catch(() => {})
  } catch {}
}

const doReject = async (id, reason) => {
  const res = await apiRejectApplication(id, reason || '商家已拒绝')
  if (res.code === 200) {
    ElMessage.success('已拒绝')
    loadApplications(currentDemand.value.id)
  } else {
    ElMessage.error(res.message || '操作失败')
  }
}

/**
 * 显示完成交付对话框
 */
const showCompleteDialog = (app) => {
  currentApplication.value = app
  completeForm.value = {
    actualQuantity: Number(app.quantity),
    actualPrice: (Number(currentDemand.value.priceMin) + Number(currentDemand.value.priceMax)) / 2
  }
  completeDialogVisible.value = true
}

/**
 * 计算总金额
 */
const calculateTotal = () => {
  const qty = completeForm.value.actualQuantity || 0
  const price = completeForm.value.actualPrice || 0
  return (qty * price).toFixed(2)
}

/**
 * 提交完成
 */
const submitComplete = async () => {
  try {
    await completeFormRef.value.validate()
  } catch {
    return
  }

  submitting.value = true
  try {
    const res = await completeDelivery(currentApplication.value.id, {
      actualQuantity: completeForm.value.actualQuantity,
      actualPrice: completeForm.value.actualPrice
    })

    if (res.code === 200) {
      ElMessage.success('订单已完成！')
      completeDialogVisible.value = false
      loadApplications(currentDemand.value.id)
      loadDemands()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

/**
 * 日期禁用
 */
const disabledDate = (date) => {
  return date < new Date(new Date().setHours(0, 0, 0, 0))
}

/**
 * 格式化日期
 */
const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getMonth() + 1}月${d.getDate()}日`
}

const formatDateForApi = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  const d = new Date(dateTime)
  return `${d.getMonth() + 1}月${d.getDate()}日 ${d.getHours()}:${String(d.getMinutes()).padStart(2, '0')}`
}

/**
 * 获取状态
 */
const getStatusClass = (status) => {
  switch (status) {
    case 1: return 'status-active'
    case 0: return 'status-offline'
    case 2: return 'status-ended'
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
 * 申请状态
 */
const getAppStatusText = (status) => {
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

const getAppStatusColor = (status) => {
  switch (status) {
    case 0: return '#ff9800'
    case 1: return '#2196f3'
    case 2: return '#9c27b0'
    case 3: return '#4caf50'
    case 4: return '#f44336'
    case 5: return '#9e9e9e'
    default: return '#999'
  }
}

/**
 * 计算进度
 */
const getProgress = (demand) => {
  if (!demand.quantityNeeded) return 100
  const remaining = Number(demand.quantityRemaining || 0)
  const needed = Number(demand.quantityNeeded || 0)
  if (needed === 0) return 100
  return Math.round(((needed - remaining) / needed) * 100)
}

/**
 * 解析照片
 */
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

// ==================== 生命周期 ====================

onMounted(() => {
  loadDemands()
  loadTodayStats()
})
</script>

<style scoped>
.shop-acquisition-page {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

/* 页面标题 */
.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  color: #333;
  margin-bottom: 4px;
}

.page-subtitle {
  font-size: 14px;
  color: #999;
}

/* 今日统计 */
.today-stats {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 20px;
  background: #e3e5e7;
  border-radius: 12px;
  box-shadow: 4px 4px 8px #b6b9ba, -4px -4px 8px #fafafd;
}

.stat-icon {
  font-size: 24px;
}

.stat-text {
  font-size: 14px;
  color: #666;
}

.stat-value {
  font-size: 18px;
  font-weight: bold;
  color: #4A7C59;
}

/* 操作栏 */
.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
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

/* 需求列表 */
.demand-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.demand-card {
  padding: 20px;
}

.demand-ended {
  opacity: 0.7;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.demand-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.demand-date {
  font-size: 12px;
  color: #999;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  color: white;
}

.status-active {
  background: #4A7C59;
}

.status-offline {
  background: #999;
}

.status-ended {
  background: #4caf50;
}

/* 信息网格 */
.info-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
}

.info-label {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}

.info-value {
  font-size: 14px;
  color: #333;
}

.info-value.price {
  color: #e74c3c;
  font-weight: bold;
}

.info-value.remaining {
  color: #4A7C59;
  font-weight: bold;
}

/* 进度 */
.progress-section {
  margin-bottom: 16px;
}

.progress-text {
  display: flex;
  justify-content: space-between;
  margin-top: 8px;
  font-size: 12px;
  color: #999;
}

/* 位置信息 */
.location-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f5f5f5;
  border-radius: 8px;
  margin-bottom: 16px;
  font-size: 14px;
  color: #666;
}

.contact-info {
  margin-left: auto;
}

/* 操作按钮 */
.card-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.empty-state.small {
  padding: 40px 20px;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.empty-text {
  font-size: 18px;
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
.price-range {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  width: 100%;
}

.price-range .el-form-item {
  flex: 1;
  margin-bottom: 0;
}

.range-divider {
  line-height: 32px;
  color: #999;
}

.price-unit {
  display: block;
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.form-tip {
  display: block;
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

/* 申请列表 */
.application-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.application-card {
  padding: 16px;
  border: 1px solid #eee;
  border-radius: 8px;
  border-left: 4px solid #999;
}

.app-pending {
  border-left-color: #ff9800;
}

.app-agreed {
  border-left-color: #2196f3;
}

.app-completed {
  border-left-color: #4caf50;
}

.app-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.farmer-name {
  font-weight: 600;
  color: #333;
}

.apply-time {
  font-size: 12px;
  color: #999;
  margin-left: 8px;
}

.status-tag {
  padding: 2px 10px;
  border-radius: 10px;
  font-size: 12px;
  color: white;
}

.app-content {
  margin-bottom: 12px;
}

.app-info {
  display: flex;
  gap: 8px;
  margin-bottom: 4px;
  font-size: 14px;
}

.app-info .info-label {
  color: #999;
  min-width: 60px;
}

.app-info .info-value {
  color: #333;
}

.app-info .info-value.success {
  color: #4caf50;
  font-weight: bold;
}

.app-photos {
  margin-bottom: 12px;
}

.photo-label {
  font-size: 12px;
  color: #999;
  display: block;
  margin-bottom: 8px;
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

.app-actions {
  display: flex;
  gap: 8px;
}

/* 完成交付 */
.total-amount {
  font-size: 24px;
  font-weight: bold;
  color: #4caf50;
}

/* 响应式 */
@media (max-width: 768px) {
  .info-grid {
    grid-template-columns: 1fr;
  }

  .action-bar {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }

  .price-range {
    flex-direction: column;
  }

  .range-divider {
    display: none;
  }
}
</style>
