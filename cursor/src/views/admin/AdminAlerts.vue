<template>
  <!-- 价格预警管理页面 -->
  <div class="admin-alerts">
    <el-tabs v-model="activeTab" type="border-card">
      <!-- 预警列表 -->
      <el-tab-pane label="预警列表" name="list">
        <div class="tab-content">
          <!-- 统计卡片 -->
          <div class="stats-cards">
            <div class="stat-card">
              <div class="stat-icon total">📊</div>
              <div class="stat-info">
                <span class="stat-value">{{ stats.total || 0 }}</span>
                <span class="stat-label">总预警</span>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon unread">🔔</div>
              <div class="stat-info">
                <span class="stat-value">{{ stats.unread || 0 }}</span>
                <span class="stat-label">未读</span>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon warning">⚠️</div>
              <div class="stat-info">
                <span class="stat-value">{{ (stats.byLevel && stats.byLevel[2]) || 0 }}</span>
                <span class="stat-label">警告级别</span>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon urgent">🚨</div>
              <div class="stat-info">
                <span class="stat-value">{{ (stats.byLevel && stats.byLevel[3]) || 0 }}</span>
                <span class="stat-label">紧急级别</span>
              </div>
            </div>
          </div>

          <!-- 操作栏 -->
          <div class="action-bar">
            <el-button type="primary" @click="analyzeAlerts" :loading="analyzing">
              <span>🔍</span> 智能分析预警
            </el-button>
            <el-button type="success" @click="generateBriefing" :loading="generating">
              <span>📰</span> 生成市场简报
            </el-button>
            <div class="filter-group">
              <el-select v-model="filterType" placeholder="预警类型" clearable @change="loadAlerts" style="width: 140px;">
                <el-option label="价格上涨" :value="1" />
                <el-option label="价格下跌" :value="2" />
                <el-option label="天气影响" :value="3" />
                <el-option label="市场异常" :value="4" />
              </el-select>
              <el-select v-model="filterLevel" placeholder="预警级别" clearable @change="loadAlerts" style="width: 120px;">
                <el-option label="提示" :value="1" />
                <el-option label="警告" :value="2" />
                <el-option label="紧急" :value="3" />
              </el-select>
              <el-select v-model="filterStatus" placeholder="处理状态" clearable @change="loadAlerts" style="width: 120px;">
                <el-option label="未读" :value="0" />
                <el-option label="已读" :value="1" />
                <el-option label="已处理" :value="2" />
              </el-select>
              <el-button @click="loadAlerts">刷新</el-button>
            </div>
          </div>

          <!-- 预警列表 -->
          <el-table :data="alerts" border v-loading="loading">
            <el-table-column prop="alertTime" label="预警时间" width="160">
              <template #default="{ row }">
                {{ formatDate(row.alertTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="alertType" label="类型" width="100">
              <template #default="{ row }">
                <el-tag :type="getTypeTagType(row.alertType)" size="small">
                  {{ getTypeName(row.alertType) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="alertLevel" label="级别" width="80">
              <template #default="{ row }">
                <el-tag :type="getLevelTagType(row.alertLevel)" size="small">
                  {{ getLevelName(row.alertLevel) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="title" label="预警标题" min-width="200" />
            <el-table-column prop="category" label="关联品类" width="100">
              <template #default="{ row }">
                {{ row.category ? getCategoryName(row.category) : '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="market" label="市场" width="150" />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="getStatusTagType(row.status)" size="small">
                  {{ getStatusName(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" size="small" @click="viewDetail(row)">查看</el-button>
                <el-button type="success" size="small" v-if="row.status < 2" @click="handleAlert(row)">处理</el-button>
                <el-button type="danger" size="small" @click="deleteAlert(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            style="margin-top: 20px; justify-content: flex-end;"
            @size-change="loadAlerts"
            @current-change="loadAlerts"
          />
        </div>
      </el-tab-pane>

      <!-- 市场简报 -->
      <el-tab-pane label="市场简报" name="briefing">
        <div class="tab-content">
          <div class="action-bar">
            <el-button type="primary" @click="generateBriefing" :loading="generating">
              <span>📰</span> 生成今日简报
            </el-button>
            <el-select v-model="briefingType" placeholder="简报类型" clearable @change="loadBriefings" style="width: 120px;">
              <el-option label="日报" :value="1" />
              <el-option label="周报" :value="2" />
              <el-option label="月报" :value="3" />
            </el-select>
            <el-button @click="loadBriefings">刷新</el-button>
          </div>

          <el-table :data="briefings" border v-loading="loadingBriefing">
            <el-table-column prop="reportDate" label="报告日期" width="120" />
            <el-table-column prop="briefingType" label="类型" width="80">
              <template #default="{ row }">
                {{ getBriefingTypeName(row.briefingType) }}
              </template>
            </el-table-column>
            <el-table-column prop="title" label="标题" min-width="200" />
            <el-table-column prop="summary" label="摘要" min-width="300" show-overflow-tooltip />
            <el-table-column prop="aiGenerated" label="来源" width="80">
              <template #default="{ row }">
                <el-tag :type="row.aiGenerated === 1 ? 'success' : 'info'" size="small">
                  {{ row.aiGenerated === 1 ? 'AI生成' : '手动' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'warning'" size="small">
                  {{ row.status === 1 ? '已发布' : '草稿' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" size="small" @click="viewBriefing(row)">查看</el-button>
                <el-button type="danger" size="small" @click="deleteBriefing(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-pagination
            v-model:current-page="briefingPage"
            v-model:page-size="briefingSize"
            :total="briefingTotal"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            style="margin-top: 20px; justify-content: flex-end;"
            @size-change="loadBriefings"
            @current-change="loadBriefings"
          />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 预警详情弹窗 -->
    <el-dialog v-model="showDetail" title="预警详情" width="600px">
      <div v-if="currentAlert" class="alert-detail">
        <div class="detail-header">
          <el-tag :type="getTypeTagType(currentAlert.alertType)" style="margin-right: 10px;">
            {{ getTypeName(currentAlert.alertType) }}
          </el-tag>
          <el-tag :type="getLevelTagType(currentAlert.alertLevel)">
            {{ getLevelName(currentAlert.alertLevel) }}
          </el-tag>
        </div>
        <h3 class="detail-title">{{ currentAlert.title }}</h3>
        <div class="detail-info">
          <div class="info-row">
            <span class="info-label">预警时间：</span>
            <span class="info-value">{{ formatDate(currentAlert.alertTime) }}</span>
          </div>
          <div class="info-row" v-if="currentAlert.category">
            <span class="info-label">关联品类：</span>
            <span class="info-value">{{ getCategoryName(currentAlert.category) }}</span>
          </div>
          <div class="info-row" v-if="currentAlert.market">
            <span class="info-label">关联市场：</span>
            <span class="info-value">{{ currentAlert.market }}</span>
          </div>
          <div class="info-row" v-if="currentAlert.currentPrice">
            <span class="info-label">当前价格：</span>
            <span class="info-value price">¥{{ currentAlert.currentPrice }} {{ currentAlert.unit }}</span>
          </div>
          <div class="info-row" v-if="currentAlert.changeRate">
            <span class="info-label">涨跌幅：</span>
            <span class="info-value" :class="currentAlert.changeRate >= 0 ? 'up' : 'down'">
              {{ currentAlert.changeRate >= 0 ? '↑' : '↓' }} {{ Math.abs(currentAlert.changeRate) }}%
            </span>
          </div>
        </div>
        <div class="detail-content">
          <div class="content-title">预警内容</div>
          <div class="content-text">{{ currentAlert.content }}</div>
        </div>
        <div class="detail-analysis" v-if="currentAlert.analysis">
          <div class="content-title">影响分析</div>
          <div class="content-text">{{ currentAlert.analysis }}</div>
        </div>
        <div class="detail-suggestion" v-if="currentAlert.suggestion">
          <div class="content-title">建议措施</div>
          <div class="content-text">{{ currentAlert.suggestion }}</div>
        </div>
        <div class="detail-weather" v-if="currentAlert.weatherImpact">
          <div class="content-title">天气影响</div>
          <div class="content-text">{{ currentAlert.weatherImpact }}</div>
        </div>
      </div>
      <template #footer>
        <el-button @click="showDetail = false">关闭</el-button>
        <el-button type="primary" v-if="currentAlert && currentAlert.status < 2" @click="handleAlert(currentAlert)">
          标记处理
        </el-button>
      </template>
    </el-dialog>

    <!-- 简报详情弹窗 -->
    <el-dialog v-model="showBriefingDetail" title="简报详情" width="700px">
      <div v-if="currentBriefing" class="briefing-detail">
        <div class="briefing-header">
          <h2>{{ currentBriefing.title }}</h2>
          <div class="briefing-meta">
            <span>报告日期：{{ currentBriefing.reportDate }}</span>
            <span>类型：{{ getBriefingTypeName(currentBriefing.briefingType) }}</span>
            <el-tag :type="currentBriefing.aiGenerated === 1 ? 'success' : 'info'" size="small">
              {{ currentBriefing.aiGenerated === 1 ? 'AI生成' : '手动生成' }}
            </el-tag>
          </div>
        </div>
        <div class="briefing-summary">
          <strong>摘要：</strong>{{ currentBriefing.summary }}
        </div>
        <div class="briefing-content" v-if="currentBriefing.analysis">
          <h4>市场分析</h4>
          <pre>{{ currentBriefing.analysis }}</pre>
        </div>
        <div class="briefing-content" v-if="currentBriefing.forecast">
          <h4>趋势预测</h4>
          <pre>{{ currentBriefing.forecast }}</pre>
        </div>
        <div class="briefing-content" v-if="currentBriefing.suggestions">
          <h4>交易建议</h4>
          <pre>{{ currentBriefing.suggestions }}</pre>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getAlertList, getAlertStats, getRecentAlerts, analyzeAlerts as apiAnalyzeAlerts,
  markAsRead, deleteAlert as apiDeleteAlert,
  getBriefingList, generateDailyBriefing, getBriefingDetail as apiGetBriefingDetail,
  deleteBriefing as apiDeleteBriefing
} from '@/api'

const activeTab = ref('list')
const loading = ref(false)
const analyzing = ref(false)
const generating = ref(false)
const loadingBriefing = ref(false)

// 预警数据
const alerts = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const filterType = ref(null)
const filterLevel = ref(null)
const filterStatus = ref(null)

// 统计数据
const stats = ref({})

// 简报数据
const briefings = ref([])
const briefingPage = ref(1)
const briefingSize = ref(10)
const briefingTotal = ref(0)
const briefingType = ref(null)

// 弹窗
const showDetail = ref(false)
const showBriefingDetail = ref(false)
const currentAlert = ref(null)
const currentBriefing = ref(null)

// 分类映射
const categoryMap = {
  1: '茉莉鲜花',
  2: '茉莉花茶',
  3: '茉莉盆栽',
  4: '茉莉花苗'
}

// 加载预警列表
const loadAlerts = async () => {
  loading.value = true
  try {
    const res = await getAlertList({
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      alertType: filterType.value,
      alertLevel: filterLevel.value,
      status: filterStatus.value
    })
    if (res.code === 200) {
      alerts.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('加载预警列表失败', error)
  } finally {
    loading.value = false
  }
}

// 加载统计数据
const loadStats = async () => {
  try {
    const res = await getAlertStats()
    if (res.code === 200) {
      stats.value = res.data || {}
    }
  } catch (error) {
    console.error('加载统计数据失败', error)
  }
}

// 智能分析预警
const analyzeAlerts = async () => {
  analyzing.value = true
  try {
    const res = await apiAnalyzeAlerts()
    if (res.code === 200) {
      ElMessage.success(`智能分析完成，生成 ${res.data?.length || 0} 条预警`)
      loadAlerts()
      loadStats()
    } else {
      ElMessage.error(res.message || '分析失败')
    }
  } catch (error) {
    ElMessage.error('分析失败')
  } finally {
    analyzing.value = false
  }
}

// 查看预警详情
const viewDetail = async (row) => {
  currentAlert.value = row
  showDetail.value = true
  // 标记已读
  if (row.status === 0) {
    await markAsRead(row.id)
    row.status = 1
  }
}

// 处理预警
const handleAlert = async (row) => {
  ElMessageBox.prompt('请输入处理备注（可选）', '处理预警', {
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(async ({ value }) => {
    // 调用处理API（需要后端支持）
    showDetail.value = false
    loadAlerts()
    loadStats()
    ElMessage.success('预警已处理')
  }).catch(() => {})
}

// 删除预警
const deleteAlert = (row) => {
  ElMessageBox.confirm('确定要删除该预警吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    const res = await apiDeleteAlert(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadAlerts()
      loadStats()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  }).catch(() => {})
}

// 加载简报列表
const loadBriefings = async () => {
  loadingBriefing.value = true
  try {
    const res = await getBriefingList({
      pageNum: briefingPage.value,
      pageSize: briefingSize.value,
      briefingType: briefingType.value
    })
    if (res.code === 200) {
      briefings.value = res.data.records || []
      briefingTotal.value = res.data.total || 0
    }
  } catch (error) {
    console.error('加载简报列表失败', error)
  } finally {
    loadingBriefing.value = false
  }
}

// 生成简报
const generateBriefing = async () => {
  generating.value = true
  try {
    const res = await generateDailyBriefing()
    if (res.code === 200) {
      ElMessage.success('简报生成成功')
      loadBriefings()
      showBriefingDetail.value = true
      currentBriefing.value = res.data
    } else {
      ElMessage.error(res.message || '生成失败')
    }
  } catch (error) {
    ElMessage.error('生成失败')
  } finally {
    generating.value = false
  }
}

// 查看简报详情
const viewBriefing = async (row) => {
  try {
    const res = await apiGetBriefingDetail(row.id)
    if (res.code === 200) {
      currentBriefing.value = res.data
      showBriefingDetail.value = true
    }
  } catch (error) {
    console.error('加载简报详情失败', error)
  }
}

// 删除简报
const deleteBriefing = (row) => {
  ElMessageBox.confirm('确定要删除该简报吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    const res = await apiDeleteBriefing(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadBriefings()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  }).catch(() => {})
}

// 工具函数
const getTypeName = (type) => {
  const names = { 1: '价格上涨', 2: '价格下跌', 3: '天气影响', 4: '市场异常' }
  return names[type] || '未知'
}

const getTypeTagType = (type) => {
  const types = { 1: 'danger', 2: 'warning', 3: 'info', 4: 'warning' }
  return types[type] || 'info'
}

const getLevelName = (level) => {
  const names = { 1: '提示', 2: '警告', 3: '紧急' }
  return names[level] || '未知'
}

const getLevelTagType = (level) => {
  const types = { 1: 'info', 2: 'warning', 3: 'danger' }
  return types[level] || 'info'
}

const getStatusName = (status) => {
  const names = { 0: '未读', 1: '已读', 2: '已处理' }
  return names[status] || '未知'
}

const getStatusTagType = (status) => {
  const types = { 0: 'danger', 1: 'warning', 2: 'success' }
  return types[status] || 'info'
}

const getCategoryName = (category) => {
  return categoryMap[category] || '未知'
}

const getBriefingTypeName = (type) => {
  const names = { 1: '日报', 2: '周报', 3: '月报', 4: '专题' }
  return names[type] || '未知'
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

onMounted(() => {
  loadAlerts()
  loadStats()
  loadBriefings()
})
</script>

<style scoped>
.admin-alerts {
  background: white;
  border-radius: 8px;
  padding: 20px;
}

.tab-content {
  padding: 20px 0;
}

/* 统计卡片 */
.stats-cards {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: #f5f7f9;
  border-radius: 12px;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.stat-icon.total { background: linear-gradient(135deg, #4A7C59, #5BA06B); color: white; }
.stat-icon.unread { background: linear-gradient(135deg, #ff9800, #ffb74d); color: white; }
.stat-icon.warning { background: linear-gradient(135deg, #f44336, #ef5350); color: white; }
.stat-icon.urgent { background: linear-gradient(135deg, #e91e63, #ec407a); color: white; }

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

/* 操作栏 */
.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  gap: 12px;
  flex-wrap: wrap;
}

.filter-group {
  display: flex;
  gap: 10px;
  align-items: center;
}

/* 详情弹窗 */
.alert-detail, .briefing-detail {
  padding: 10px;
}

.detail-header {
  display: flex;
  margin-bottom: 12px;
}

.detail-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0 0 16px 0;
}

.detail-info {
  background: #f5f7f9;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 16px;
}

.info-row {
  display: flex;
  margin-bottom: 8px;
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-label {
  width: 100px;
  color: #666;
}

.info-value {
  color: #333;
}

.info-value.price {
  color: #e74c3c;
  font-weight: 600;
}

.info-value.up {
  color: #e74c3c;
}

.info-value.down {
  color: #27ae60;
}

.detail-content, .detail-analysis, .detail-suggestion, .detail-weather {
  margin-bottom: 16px;
}

.content-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.content-text {
  font-size: 14px;
  color: #666;
  line-height: 1.8;
}

/* 简报详情 */
.briefing-header {
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #eee;
}

.briefing-header h2 {
  margin: 0 0 12px 0;
  font-size: 20px;
  color: #333;
}

.briefing-meta {
  display: flex;
  gap: 20px;
  font-size: 14px;
  color: #666;
}

.briefing-summary {
  background: #e8f5e9;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 20px;
  font-size: 14px;
  line-height: 1.8;
}

.briefing-content {
  margin-bottom: 20px;
}

.briefing-content h4 {
  font-size: 16px;
  color: #333;
  margin-bottom: 10px;
}

.briefing-content pre {
  background: #f5f7f9;
  padding: 16px;
  border-radius: 8px;
  font-size: 14px;
  line-height: 1.8;
  white-space: pre-wrap;
  margin: 0;
}
</style>
