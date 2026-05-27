<template>
  <div class="acquisition-records-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">我的收购记录</h1>
      <p class="page-subtitle">查看收购申请状态和收入统计</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-section">
      <div class="stats-grid">
        <div class="stat-card neumorphic-card">
          <div class="stat-icon">
            <svg width="32" height="32" viewBox="0 0 24 24" fill="#e8a045">
              <path d="M11.8 10.9c-2.27-.59-3-1.2-3-2.15 0-1.09 1.01-1.85 2.7-1.85 1.78 0 2.44.85 2.5 2.1h2.21c-.07-1.72-1.12-3.3-3.21-3.81V3h-3v2.16c-1.94.42-3.5 1.68-3.5 3.61 0 2.31 1.91 3.46 4.7 4.13 2.5.6 3 1.48 3 2.41 0 .69-.49 1.79-2.7 1.79-2.06 0-2.87-.92-2.98-2.1h-2.2c.12 2.19 1.76 3.42 3.68 3.83V21h3v-2.15c1.95-.37 3.5-1.5 3.5-3.55 0-2.84-2.43-3.81-4.7-4.4z"/>
            </svg>
          </div>
          <div class="stat-info">
            <span class="stat-value">¥{{ formatNumber(stats.totalAmount) }}</span>
            <span class="stat-label">累计总收入</span>
          </div>
        </div>
        <div class="stat-card neumorphic-card">
          <div class="stat-icon">
            <svg width="32" height="32" viewBox="0 0 24 24" fill="#4A7C59">
              <path d="M12 22c1.1 0 2-.9 2-2h-4c0 1.1.89 2 2 2zm6-6v-5c0-3.07-1.64-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.63 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2z"/>
            </svg>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ formatNumber(stats.totalQuantity) }} <span class="stat-unit">斤</span></span>
            <span class="stat-label">累计交货总量</span>
          </div>
        </div>
        <div class="stat-card neumorphic-card">
          <div class="stat-icon">
            <svg width="32" height="32" viewBox="0 0 24 24" fill="#67c23a">
              <path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"/>
            </svg>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ stats.completedCount || 0 }}</span>
            <span class="stat-label">已完成订单</span>
          </div>
        </div>
        <div class="stat-card neumorphic-card">
          <div class="stat-icon">
            <svg width="32" height="32" viewBox="0 0 24 24" fill="#f56c6c">
              <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z"/>
            </svg>
          </div>
          <div class="stat-info">
            <span class="stat-value">{{ stats.pendingCount || 0 }}</span>
            <span class="stat-label">待处理订单</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 收入统计图表 -->
    <div class="chart-section neumorphic-card">
      <div class="chart-header">
        <h3>每日收入统计</h3>
        <div class="chart-controls">
          <el-radio-group v-model="chartDays" @change="handleChartDaysChange" size="small">
            <el-radio-button :value="7">近7天</el-radio-button>
            <el-radio-button :value="15">近15天</el-radio-button>
            <el-radio-button :value="30">近30天</el-radio-button>
          </el-radio-group>
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            size="small"
            style="width: 240px; margin-left: 12px;"
            @change="handleDateRangeChange"
          />
        </div>
      </div>
      <div class="chart-legend">
        <span class="legend-item">
          <span class="legend-line solid" style="border-color: #4A7C59;"></span>
          每日收入
        </span>
        <span class="legend-item">
          <span class="legend-line dashed" style="border-color: #e8a045;"></span>
          交货量
        </span>
      </div>
      <div class="chart-container" ref="chartRef"></div>
      <div v-if="dailyStats.length === 0" class="chart-empty">
        <p>暂无收入数据</p>
      </div>
    </div>

    <!-- 筛选标签 -->
    <div class="filter-section">
      <el-radio-group v-model="statusFilter" @change="loadApplications">
        <el-radio-button value="">全部</el-radio-button>
        <el-radio-button value="0">待审核</el-radio-button>
        <el-radio-button value="1">已同意</el-radio-button>
        <el-radio-button value="3">已完成</el-radio-button>
        <el-radio-button value="4">已拒绝</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 修改申请弹窗 -->
    <el-dialog v-model="showEditDialog" title="修改收购申请" width="500px" :close-on-click-modal="false" destroy-on-close>
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="收购需求">
          <span>{{ editForm.title || editForm.demandTitle || '-' }}</span>
        </el-form-item>
        <el-form-item label="供货数量(斤)" required>
          <el-input-number
            v-model="editForm.quantity"
            :min="1"
            :max="99999"
            controls-position="right"
            style="width: 100%;"
          />
          <div class="form-tip">填写您计划供货的斤数</div>
        </el-form-item>
        <el-form-item label="联系电话" required>
          <el-input v-model="editForm.contactPhone" placeholder="请输入联系电话" maxlength="11" />
        </el-form-item>
        <el-form-item label="茉莉花照片">
          <div class="photo-upload-area">
            <div class="photo-list" v-if="editForm.photoUrls && editForm.photoUrls.length > 0">
              <div v-for="(url, index) in parsePhotos(editForm.photoUrls)" :key="index" class="photo-item">
                <el-image :src="normalizeUrl(url)" fit="cover" />
                <span class="photo-remove" @click="removePhoto(index)">×</span>
              </div>
            </div>
            <el-upload
              :show-file-list="false"
              :before-upload="beforePhotoUpload"
              :http-request="uploadPhoto"
              accept="image/*"
              class="photo-upload-btn"
            >
              <el-button size="small" type="primary">上传照片</el-button>
            </el-upload>
          </div>
          <div class="form-tip">上传茉莉花照片（可选）</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmEdit" :loading="editLoading">确认修改</el-button>
      </template>
    </el-dialog>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <el-icon class="is-loading"><Loading /></el-icon>
      <p>加载中...</p>
    </div>

    <!-- 申请记录列表 -->
    <div v-else-if="applications.length > 0" class="record-list">
      <div
        v-for="app in applications"
        :key="app.id"
        class="record-card neumorphic-card"
        :class="{ 'record-pending': app.status === 0, 'record-active': app.status === 1, 'record-completed': app.status === 3 }"
      >
        <div class="record-header">
          <div class="shop-info">
            <span class="shop-name">{{ app.shopName || '收购商' }}</span>
            <span class="record-date">{{ formatDateTime(app.createdAt) }}</span>
          </div>
          <span class="status-tag" :style="{ background: getStatusColor(app.status) }">
            {{ getStatusText(app.status) }}
          </span>
        </div>

        <div class="record-content">
          <h4 class="demand-title">{{ app.title || app.demandTitle || '收购需求' }}</h4>

          <div class="record-details">
            <div class="detail-item">
              <span class="detail-label">申请数量</span>
              <span class="detail-value">{{ app.quantity }} 斤</span>
            </div>
            <div class="detail-item" v-if="app.status === 3">
              <span class="detail-label">实际成交</span>
              <span class="detail-value success">¥{{ app.totalAmount }} ({{ app.actualQuantity }}斤 × ¥{{ app.actualPrice }})</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">市场地址</span>
              <span class="detail-value">{{ app.marketAddress }}</span>
            </div>
            <div class="detail-item" v-if="app.status === 4 && app.rejectReason">
              <span class="detail-label">拒绝原因</span>
              <span class="detail-value danger">{{ app.rejectReason }}</span>
            </div>
          </div>

          <div class="photo-section" v-if="app.photoUrls">
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
        </div>

        <div class="record-footer">
          <el-button v-if="app.status === 0" type="primary" size="small" @click="openEditDialog(app)">
            修改
          </el-button>
          <el-button v-if="app.status === 0 || app.status === 1" type="danger" size="small" @click="cancelApplication(app)">
            取消申请
          </el-button>
          <span v-if="app.status === 1" class="delivery-tip">
            📍 请将茉莉花送至市场：{{ app.marketAddress }}
          </span>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="empty-state">
      <svg width="64" height="64" viewBox="0 0 24 24" fill="#ccc">
        <path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-5 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z"/>
      </svg>
      <p class="empty-text">暂无申请记录</p>
      <p class="empty-hint">去收购专栏申请供货吧</p>
      <el-button type="primary" @click="$router.push('/acquisition')">
        查看收购需求
      </el-button>
    </div>

    <!-- 分页 -->
    <div class="pagination-wrapper" v-if="total > pageSize">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="loadApplications"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getMyAcquisitionApplications, getMyAcquisitionStats, getMyDailyAcquisitionStats, cancelAcquisitionApplication, updateAcquisitionApplication } from '@/api/acquisition'
import { uploadImage } from '@/api'
import { normalizeUploadUrl } from '@/utils/imageUrl'

const loading = ref(false)
const applications = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const statusFilter = ref('')

const stats = ref({
  totalAmount: 0,
  totalQuantity: 0,
  completed: 0,
  completedCount: 0,
  pending: 0,
  pendingCount: 0,
  pendingOrders: 0
})

const dailyStats = ref([])

// 时间查询相关
const chartDays = ref(30)
const dateRange = ref(null)

// 时间范围查询处理
const handleDateRangeChange = (val) => {
  if (val && val.length === 2) {
    chartDays.value = null
    loadDailyStats()
  }
}

// 天数切换处理
const handleChartDaysChange = () => {
  dateRange.value = null
  loadDailyStats()
}

const showEditDialog = ref(false)
const editLoading = ref(false)
const editForm = reactive({
  id: null,
  title: '',
  demandTitle: '',
  quantity: null,
  contactPhone: '',
  photoUrls: []
})

const chartRef = ref(null)
let mainChart = null

// 图表配色
const chartColors = {
  primary: '#4A7C59',
  primaryLight: '#6b9b7a',
  secondary: '#e8a045',
  grid: '#f0f0f0',
  text: '#666666'
}

const initChart = () => {
  if (!chartRef.value) return

  mainChart = echarts.init(chartRef.value)
  updateChart()
}

const updateChart = () => {
  if (!mainChart) {
    return
  }

  // 如果有数据，按实际数据的时间范围显示
  if (dailyStats.value && dailyStats.value.length > 0) {
    const dates = dailyStats.value.map(d => {
      let dateKey = d.date || d.dateStr || ''
      if (dateKey && dateKey.includes('T')) {
        dateKey = dateKey.split('T')[0]
      }
      return dateKey
    }).filter(d => d)

    if (dates.length > 0) {
      dates.sort()
      const startDate = dates[0]
      const endDate = dates[dates.length - 1]

      const result = []
      const dataMap = {}
      dailyStats.value.forEach(d => {
        let dateKey = d.date || d.dateStr || ''
        if (dateKey && dateKey.includes('T')) {
          dateKey = dateKey.split('T')[0]
        }
        if (dateKey) {
          dataMap[dateKey] = d
        }
      })

      const start = new Date(startDate)
      const end = new Date(endDate)
      for (let d = new Date(start); d <= end; d.setDate(d.getDate() + 1)) {
        const dateStr = d.toISOString().split('T')[0]
        const dayData = dataMap[dateStr] || { date: dateStr, amount: 0, quantity: 0 }
        result.push(dayData)
      }

      const dates2 = result.map(d => d.date?.slice(5) || '')
      const amounts = result.map(d => {
        const val = d.amount || d.totalAmount || d.income || d.totalIncome || 0
        return Number(val)
      })
      const quantities = result.map(d => {
        const val = d.quantity || d.totalQuantity || d.deliveryQuantity || d.count || 0
        return Number(val)
      })

      const maxAmount = Math.max(...amounts.filter(a => a > 0), 1)
      const maxQuantity = Math.max(...quantities.filter(q => q > 0), 1)

      const option = {
        tooltip: {
          trigger: 'axis',
          backgroundColor: 'rgba(255, 255, 255, 0.98)',
          borderColor: chartColors.primary,
          borderWidth: 1,
          padding: [12, 16],
          textStyle: { color: '#333', fontSize: 13 },
          formatter: (params) => {
            let result = `<div style="font-weight: 600; margin-bottom: 8px; color: #333;">${params[0].name}</div>`
            params.forEach(p => {
              if (p.seriesName === '每日收入') {
                result += `<div style="display: flex; align-items: center; gap: 8px; margin: 4px 0;">
                  <span style="display: inline-block; width: 10px; height: 10px; border-radius: 2px; background: ${p.color};"></span>
                  <span style="flex: 1; color: #666;">${p.seriesName}</span>
                  <span style="font-weight: 700; color: ${p.color};">¥${Number(p.value).toFixed(2)}</span>
                </div>`
              } else {
                result += `<div style="display: flex; align-items: center; gap: 8px; margin: 4px 0;">
                  <span style="display: inline-block; width: 12px; height: 3px; border-top: 2px dashed ${p.color};"></span>
                  <span style="flex: 1; color: #666;">${p.seriesName}</span>
                  <span style="font-weight: 700; color: ${p.color};">${p.value} 斤</span>
                </div>`
              }
            })
            return result
          }
        },
        legend: { show: false },
        grid: { left: '3%', right: '4%', bottom: '12%', top: '10%', containLabel: true },
        xAxis: {
          type: 'category',
          data: dates2,
          axisLine: { lineStyle: { color: '#e0e0e0' } },
          axisTick: { show: false },
          axisLabel: { color: chartColors.text, fontSize: 11, rotate: dates2.length > 15 ? 45 : 0 }
        },
        yAxis: [
          {
            type: 'value',
            name: '收入(元)',
            position: 'left',
            nameTextStyle: { color: chartColors.text, fontSize: 11 },
            axisLine: { show: false },
            axisTick: { show: false },
            axisLabel: {
              color: chartColors.text,
              fontSize: 11,
              formatter: (val) => Math.round(val) >= 1000 ? (Math.round(val) / 1000).toFixed(0) + 'k' : Math.round(val)
            },
            splitLine: { lineStyle: { color: chartColors.grid, type: 'dashed' } },
            max: maxAmount <= 0 ? 100 : Math.ceil(maxAmount * 1.2 / 10) * 10,
            min: 0
          },
          {
            type: 'value',
            name: '交货量(斤)',
            position: 'right',
            nameTextStyle: { color: chartColors.text, fontSize: 11 },
            axisLine: { show: false },
            axisTick: { show: false },
            axisLabel: {
              color: chartColors.text,
              fontSize: 11,
              formatter: (val) => Math.round(val) >= 100 ? (Math.round(val) / 100).toFixed(0) + '百' : Math.round(val)
            },
            splitLine: { show: false },
            max: maxQuantity <= 0 ? 100 : Math.ceil(maxQuantity * 1.2 / 10) * 10,
            min: 0
          }
        ],
        series: [
          {
            name: '每日收入',
            type: 'line',
            data: amounts,
            yAxisIndex: 0,
            smooth: 0.3,
            symbol: 'circle',
            symbolSize: 6,
            showSymbol: amounts.some(a => a > 0),
            lineStyle: {
              width: 3,
              color: chartColors.primary,
              shadowColor: 'rgba(74, 124, 89, 0.4)',
              shadowBlur: 8,
              shadowOffsetY: 3
            },
            itemStyle: {
              color: chartColors.primary,
              borderColor: '#fff',
              borderWidth: 2,
              shadowColor: 'rgba(74, 124, 89, 0.3)',
              shadowBlur: 6
            },
            emphasis: { scale: true, scaleSize: 10 },
            areaStyle: {
              color: {
                type: 'linear',
                x: 0, y: 0, x2: 0, y2: 1,
                colorStops: [
                  { offset: 0, color: chartColors.primary + '30' },
                  { offset: 1, color: chartColors.primary + '05' }
                ]
              }
            }
          },
          {
            name: '交货量',
            type: 'line',
            data: quantities,
            yAxisIndex: 1,
            smooth: 0.5,
            symbol: 'circle',
            symbolSize: 7,
            showSymbol: quantities.some(q => q > 0),
            lineStyle: {
              width: 3,
              color: chartColors.secondary,
              type: 'dashed',
              shadowColor: 'rgba(232, 160, 69, 0.4)',
              shadowBlur: 8,
              shadowOffsetY: 3
            },
            itemStyle: {
              color: chartColors.secondary,
              borderColor: '#fff',
              borderWidth: 2,
              shadowColor: 'rgba(232, 160, 69, 0.3)',
              shadowBlur: 6
            },
            emphasis: { scale: true, scaleSize: 10 }
          }
        ]
      }

      mainChart.setOption(option, true)
      return
    }
  }

  // 如果没有数据，显示空状态
  mainChart.setOption({
    graphic: [{
      type: 'text',
      left: 'center',
      top: 'middle',
      style: {
        text: '暂无收入数据',
        fontSize: 14,
        fill: '#999'
      }
    }]
  })
}

const loadApplications = async () => {
  loading.value = true
  try {
    const res = await getMyAcquisitionApplications(currentPage.value, pageSize.value)
    if (res.code === 200 && res.data) {
      let records = res.data.records || res.data || []
      if (statusFilter.value !== '') {
        records = records.filter(r => r.status === Number(statusFilter.value))
      }
      applications.value = records
      total.value = res.data.total || records.length
    } else {
      applications.value = []
    }
  } catch (error) {
    console.error('加载申请记录失败:', error)
    applications.value = []
  } finally {
    loading.value = false
  }
}

const loadStats = async () => {
  try {
    const res = await getMyAcquisitionStats()
    if (res.code === 200 && res.data) {
      const data = res.data
      stats.value = {
        totalAmount: data.totalAmount || 0,
        totalQuantity: data.totalQuantity || 0,
        completed: data.completed || 0,
        completedCount: data.completed || 0,
        pending: data.pending || 0,
        pendingCount: data.pending || data.pendingOrders || 0,
        pendingOrders: data.pendingOrders || data.pending || 0
      }
    }
  } catch (error) {
    console.error('加载统计信息失败:', error)
  }
}

const loadDailyStats = async () => {
  try {
    const res = await getMyDailyAcquisitionStats()
    if (res.code === 200) {
      let data = res.data || []

      // 根据时间范围筛选数据
      if (dateRange.value && dateRange.value.length === 2) {
        const [startDate, endDate] = dateRange.value
        data = data.filter(d => {
          const dateStr = (d.date || '').split('T')[0]
          return dateStr >= startDate && dateStr <= endDate
        })
      } else if (chartDays.value) {
        // 按天数筛选
        const startDate = new Date()
        startDate.setDate(startDate.getDate() - chartDays.value + 1)
        const startStr = startDate.toISOString().split('T')[0]
        data = data.filter(d => {
          const dateStr = (d.date || '').split('T')[0]
          return dateStr >= startStr
        })
      }

      dailyStats.value = data
      await nextTick()
      if (mainChart) {
        updateChart()
      } else {
        initChart()
      }
    }
  } catch (error) {
    console.error('加载每日统计数据失败:', error)
  }
}

const cancelApplication = async (app) => {
  try {
    await ElMessageBox.confirm(
      `确定要取消申请吗？${app.status === 1 ? '（已同意的申请取消后将释放您的供货配额）' : ''}`,
      '取消申请',
      {
        confirmButtonText: '确定',
        cancelButtonText: '点错了',
        type: 'warning'
      }
    )

    const res = await cancelAcquisitionApplication(app.id)
    if (res.code === 200) {
      ElMessage.success('申请已取消')
      loadApplications()
      loadStats()
    } else {
      ElMessage.error(res.message || '取消失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '取消失败')
    }
  }
}

const openEditDialog = (app) => {
  editForm.id = app.id
  editForm.title = app.title || app.demandTitle || ''
  editForm.demandTitle = app.demandTitle || app.title || ''
  editForm.quantity = Number(app.quantity) || null
  editForm.contactPhone = app.contactPhone || ''
  if (app.photoUrls) {
    editForm.photoUrls = typeof app.photoUrls === 'string' ? JSON.parse(app.photoUrls) : app.photoUrls
  } else {
    editForm.photoUrls = []
  }
  showEditDialog.value = true
}

const confirmEdit = async () => {
  if (!editForm.quantity || editForm.quantity <= 0) {
    ElMessage.warning('请填写正确的供货数量')
    return
  }
  if (!editForm.contactPhone || !editForm.contactPhone.trim()) {
    ElMessage.warning('请填写联系电话')
    return
  }

  editLoading.value = true
  try {
    const photoUrlsStr = Array.isArray(editForm.photoUrls) && editForm.photoUrls.length > 0
      ? JSON.stringify(editForm.photoUrls)
      : null

    const res = await updateAcquisitionApplication(editForm.id, {
      quantity: editForm.quantity,
      contactPhone: editForm.contactPhone,
      photoUrls: photoUrlsStr
    })

    if (res && res.code === 200) {
      ElMessage.success('申请修改成功')
      showEditDialog.value = false
      loadApplications()
    } else {
      ElMessage.error(res?.message || '修改失败')
    }
  } catch (error) {
    console.error('修改申请失败:', error)
    ElMessage.error('修改失败，请重试')
  } finally {
    editLoading.value = false
  }
}

const removePhoto = (index) => {
  editForm.photoUrls.splice(index, 1)
}

const beforePhotoUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过5MB')
    return false
  }
  return true
}

const uploadPhoto = async (options) => {
  const { file, onSuccess, onError } = options
  try {
    const res = await uploadImage(file)
    if (res && res.code === 200 && res.data) {
      let url = typeof res.data === 'string' ? res.data : res.data.url
      if (url && !url.startsWith('/') && !url.startsWith('http')) {
        url = '/' + url
      }
      editForm.photoUrls.push(url)
      onSuccess(res)
    } else {
      ElMessage.error(res?.message || '上传失败')
      onError(new Error(res?.message || '上传失败'))
    }
  } catch (error) {
    console.error('上传照片失败:', error)
    ElMessage.error('上传失败，请重试')
    onError(error)
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

const getStatusText = (status) => {
  switch (status) {
    case 0: return '待审核'
    case 1: return '已同意/待交付'
    case 2: return '交付中'
    case 3: return '已完成'
    case 4: return '已拒绝'
    case 5: return '已取消'
    default: return '未知'
  }
}

const getStatusColor = (status) => {
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

const formatNumber = (value) => {
  if (!value && value !== 0) return '0'
  const num = Number(value)
  if (num >= 10000) {
    return (num / 10000).toFixed(2) + '万'
  }
  return num.toFixed(2)
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  const d = new Date(dateTime)
  return `${d.getMonth() + 1}月${d.getDate()}日 ${d.getHours()}:${String(d.getMinutes()).padStart(2, '0')}`
}

const handleResize = () => {
  if (mainChart) mainChart.resize()
}

onMounted(async () => {
  // 先初始化图表
  initChart()
  // 然后并行加载数据
  await Promise.all([
    loadApplications(),
    loadStats(),
    loadDailyStats()
  ])
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (mainChart) mainChart.dispose()
})
</script>

<style scoped>
.acquisition-records-page {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
}

.page-title {
  font-size: 26px;
  color: #1a1a2e;
  margin-bottom: 8px;
  font-weight: 700;
}

.page-subtitle {
  font-size: 14px;
  color: #888;
}

.stats-section {
  margin-bottom: 24px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 20px;
  border-radius: 16px;
  background: white;
  transition: all 0.3s;
  border: 1px solid rgba(0, 0, 0, 0.04);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
}

.stat-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-info {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.stat-value {
  font-size: 20px;
  font-weight: 700;
  color: #1a1a2e;
  line-height: 1.2;
}

.stat-unit {
  font-size: 12px;
  color: #888;
  font-weight: 400;
}

.stat-label {
  font-size: 12px;
  color: #888;
  margin-top: 4px;
}

.chart-section {
  padding: 20px;
  margin-bottom: 24px;
  border-radius: 16px;
  background: white;
  border: 1px solid rgba(0, 0, 0, 0.04);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.chart-header h3 {
  font-size: 15px;
  color: #1a1a2e;
  font-weight: 600;
  margin: 0;
}

.chart-legend {
  display: flex;
  gap: 20px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #666;
}

.legend-bar {
  width: 12px;
  height: 12px;
  border-radius: 3px;
}

.legend-line {
  width: 16px;
  height: 0;
  border-top: 3px solid;
}

.chart-container {
  width: 100%;
  height: 300px;
}

.chart-empty {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 14px;
  background: #fafafa;
  border-radius: 8px;
  margin-top: 16px;
}

.filter-section {
  margin-bottom: 20px;
}

.loading-state {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}

.loading-state .el-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.record-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.record-card {
  padding: 20px;
  border-left: 4px solid transparent;
  border-radius: 16px;
  background: white;
  border: 1px solid rgba(0, 0, 0, 0.04);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
}

.record-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.record-pending {
  border-left-color: #ff9800;
}

.record-active {
  border-left-color: #2196f3;
}

.record-completed {
  border-left-color: #4caf50;
}

.record-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.shop-info {
  display: flex;
  flex-direction: column;
}

.shop-name {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.record-date {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}

.status-tag {
  padding: 5px 12px;
  border-radius: 20px;
  font-size: 12px;
  color: white;
  font-weight: 500;
}

.record-content {
  margin-bottom: 12px;
}

.demand-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}

.record-details {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
  margin-bottom: 12px;
}

.detail-item {
  display: flex;
  flex-direction: column;
}

.detail-label {
  font-size: 12px;
  color: #999;
  margin-bottom: 2px;
}

.detail-value {
  font-size: 14px;
  color: #333;
}

.detail-value.success {
  color: #4caf50;
  font-weight: 600;
}

.detail-value.danger {
  color: #f44336;
}

.photo-section {
  margin-top: 12px;
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
  flex-wrap: wrap;
}

.photo-item {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  cursor: pointer;
}

.record-footer {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-top: 12px;
  border-top: 1px solid #eee;
}

.delivery-tip {
  font-size: 13px;
  color: #2196f3;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.empty-state svg {
  margin-bottom: 16px;
}

.empty-text {
  font-size: 18px;
  color: #666;
  margin-bottom: 8px;
}

.empty-hint {
  font-size: 14px;
  color: #999;
  margin-bottom: 20px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

.form-tip {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.photo-upload-area {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.photo-upload-btn {
  display: inline-block;
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .stat-value {
    font-size: 16px;
  }

  .record-details {
    grid-template-columns: 1fr;
  }

  .photo-item {
    width: 60px;
    height: 60px;
  }
}
</style>
