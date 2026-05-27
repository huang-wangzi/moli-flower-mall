<template>
  <div class="admin-acquisition">
    <div class="page-header">
      <h2>收购管理</h2>
      <p class="subtitle">管理所有收购需求、价格监控、违规下架</p>
    </div>

    <!-- 价格统计卡片 - 横向并排均匀摆放 -->
    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon price-low-icon">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="white">
            <path d="M7 14l5-5 5 5H7z"/>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-label">最低收购价</div>
          <div class="stat-value">
            {{ priceStats.minPrice !== null ? '¥' + priceStats.minPrice : '-' }}
          </div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon price-high-icon">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="white">
            <path d="M7 10l5 5 5-5H7z"/>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-label">最高收购价</div>
          <div class="stat-value">
            {{ priceStats.maxPrice !== null ? '¥' + priceStats.maxPrice : '-' }}
          </div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon price-avg-icon">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="white">
            <path d="M3.5 18.49l6-6.01 4 4L22 6.92l-1.41-1.41-7.09 7.97-4-4L2 16.99l1.5 1.5z"/>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-label">平均收购价</div>
          <div class="stat-value">
            {{ priceStats.avgPrice !== null ? '¥' + priceStats.avgPrice : '-' }}
          </div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon active-demand-icon">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="white">
            <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-label">活跃需求数</div>
          <div class="stat-value">{{ priceStats.totalCount || 0 }}</div>
        </div>
      </div>
    </div>

    <!-- 价格趋势图 -->
    <div class="chart-section">
      <div class="chart-header">
        <h3>价格走势（近{{ trendDays }}天）</h3>
        <div class="days-selector">
          <el-button :type="trendDays === 7 ? 'primary' : ''" size="small" @click="loadTrend(7)">7天</el-button>
          <el-button :type="trendDays === 15 ? 'primary' : ''" size="small" @click="loadTrend(15)">15天</el-button>
          <el-button :type="trendDays === 30 ? 'primary' : ''" size="small" @click="loadTrend(30)">30天</el-button>
        </div>
      </div>
      <div class="chart-container" ref="chartContainer"></div>
    </div>

    <!-- 筛选和列表 -->
    <div class="list-section">
      <div class="toolbar">
        <div class="filter-group">
          <el-select v-model="statusFilter" placeholder="状态筛选" style="width: 120px;" clearable @change="loadDemands">
            <el-option label="全部" value="" />
            <el-option label="收购中" :value="1" />
            <el-option label="已下架" :value="0" />
            <el-option label="已完成" :value="2" />
          </el-select>
        </div>
        <div class="stats-info">
          <span>共 {{ demands.length }} 条收购需求</span>
        </div>
      </div>

      <el-table :data="demands" border style="width: 100%;" v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column label="收购商" min-width="120">
          <template #default="{ row }">
            <div class="merchant-info">
              <span>{{ row.shopName || '收购商' + row.shopId }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="收购日期" width="100">
          <template #default="{ row }">
            {{ formatDate(row.demandDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="quantityNeeded" label="收购量" width="80">
          <template #default="{ row }">
            {{ row.quantityNeeded }}{{ row.unit || '斤' }}
          </template>
        </el-table-column>
        <el-table-column prop="quantityRemaining" label="剩余量" width="80" />
        <el-table-column label="价格区间" width="120">
          <template #default="{ row }">
            ¥{{ row.priceMin }} ~ ¥{{ row.priceMax }}
          </template>
        </el-table-column>
        <el-table-column label="品种要求" min-width="100" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.variety || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="发布时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 1" type="warning" size="small" @click="handleOffShelf(row)">
              违规下架
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 申请订单列表 -->
    <div class="orders-section">
      <h3>收购订单记录</h3>
      <el-table :data="applications" border style="width: 100%; margin-top: 16px;" v-loading="loadingOrders">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column label="花农" min-width="100">
          <template #default="{ row }">
            {{ row.farmerName || '用户' + row.userId }}
          </template>
        </el-table-column>
        <el-table-column label="收购商" min-width="100">
          <template #default="{ row }">
            {{ row.shopName || '收购商' + row.shopId }}
          </template>
        </el-table-column>
        <el-table-column label="收购日期" width="100">
          <template #default="{ row }">
            {{ formatDate(row.demandDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="申请量" width="80" />
        <el-table-column prop="expectedPrice" label="期望价" width="80">
          <template #default="{ row }">
            ¥{{ row.expectedPrice }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getAppStatusType(row.status)">{{ getAppStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="申请时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="orderPage"
        v-model:page-size="orderSize"
        :total="orderTotal"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        style="margin-top: 16px; justify-content: flex-end;"
        @size-change="loadApplications"
        @current-change="loadApplications"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import {
  getAllAcquisitionDemands,
  adminOffShelfDemand,
  adminDeleteDemand,
  getAcquisitionPriceStats,
  getAcquisitionPriceTrend,
  getAllAcquisitionApplications
} from '@/api/acquisition'

const loading = ref(false)
const loadingOrders = ref(false)
const demands = ref([])
const applications = ref([])
const priceStats = ref({ minPrice: null, maxPrice: null, avgPrice: null, totalCount: 0 })
const priceTrend = ref([])
const trendDays = ref(7)
const statusFilter = ref('')
const chartContainer = ref(null)

// 分页
const orderPage = ref(1)
const orderSize = ref(20)
const orderTotal = ref(0)

const getStatusType = (status) => {
  const types = { 0: 'info', 1: 'success', 2: 'warning' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = { 0: '已下架', 1: '收购中', 2: '已完成' }
  return texts[status] || '未知'
}

const getAppStatusType = (status) => {
  const types = { 0: 'warning', 1: 'primary', 2: 'info', 3: 'success', 4: 'danger', 5: 'info' }
  return types[status] || 'info'
}

const getAppStatusText = (status) => {
  const texts = { 0: '待审核', 1: '已同意', 2: '交付中', 3: '已完成', 4: '已拒绝', 5: '已取消' }
  return texts[status] || '未知'
}

const formatDate = (date) => {
  if (!date) return '-'
  if (typeof date === 'string') return date.substring(0, 10)
  return date
}

const formatDateTime = (datetime) => {
  if (!datetime) return '-'
  const d = new Date(datetime)
  return d.toLocaleString('zh-CN', { dateStyle: 'short', timeStyle: 'short' })
}

const loadDemands = async () => {
  loading.value = true
  try {
    const res = await getAllAcquisitionDemands(statusFilter.value !== '' ? parseInt(statusFilter.value) : null)
    if (res.code === 200) {
      demands.value = res.data || []
    } else {
      ElMessage.error(res.message || '加载失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('加载收购需求失败')
  } finally {
    loading.value = false
  }
}

const loadApplications = async () => {
  loadingOrders.value = true
  try {
    const res = await getAllAcquisitionApplications(orderPage.value, orderSize.value)
    if (res.code === 200) {
      applications.value = res.data?.records || []
      orderTotal.value = res.data?.total || 0
    }
  } catch (e) {
    console.error(e)
  } finally {
    loadingOrders.value = false
  }
}

const loadPriceStats = async () => {
  try {
    const res = await getAcquisitionPriceStats()
    if (res.code === 200) {
      priceStats.value = res.data || {}
    }
  } catch (e) {
    console.error(e)
  }
}

const loadTrend = async (days) => {
  trendDays.value = days
  try {
    const res = await getAcquisitionPriceTrend(days)
    if (res.code === 200) {
      priceTrend.value = res.data || []
      await nextTick()
      renderChart()
    }
  } catch (e) {
    console.error(e)
  }
}

const renderChart = () => {
  if (!chartContainer.value || priceTrend.value.length === 0) return

  const chart = echarts.getInstanceByDom(chartContainer.value)
  if (chart) chart.dispose()

  const myChart = echarts.init(chartContainer.value)

  const dates = priceTrend.value.map(item => item.date)

  // 准备数据：ECharts 需要数值或 null
  const minPrices = priceTrend.value.map(item => {
    const val = item.minPrice
    return (val === null || val === undefined) ? null : Number(val)
  })
  const maxPrices = priceTrend.value.map(item => {
    const val = item.maxPrice
    return (val === null || val === undefined) ? null : Number(val)
  })

  // 存储填充标记用于 tooltip
  const filledFlags = priceTrend.value.map(item => item.filled === true)

  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        let result = `<div style="font-weight: 600; margin-bottom: 8px;">${params[0].name}</div>`
        params.forEach((p, idx) => {
          if (p.value !== null && p.value !== undefined && p.value !== '-') {
            const isFilled = filledFlags[p.dataIndex]
            result += `<div style="margin: 4px 0;">
              <span style="display: inline-block; width: 10px; height: 10px; border-radius: 50%; background: ${p.color}; margin-right: 8px;"></span>
              ${p.seriesName}: <strong>¥${p.value}</strong>
              ${isFilled ? '<span style="color: #999; font-size: 11px;"> (参考价)</span>' : ''}
            </div>`
          }
        })
        return result
      }
    },
    legend: {
      data: ['最低价', '最高价']
    },
    xAxis: {
      type: 'category',
      data: dates,
      axisLabel: {
        rotate: 30
      }
    },
    yAxis: {
      type: 'value',
      name: '价格（元/斤）',
      axisLabel: {
        formatter: '¥{value}'
      }
    },
    series: [
      {
        name: '最低价',
        type: 'line',
        data: minPrices,
        itemStyle: { color: '#67C23A' },
        smooth: true,
        connectNulls: true,
        lineStyle: {
          type: 'dashed'
        }
      },
      {
        name: '最高价',
        type: 'line',
        data: maxPrices,
        itemStyle: { color: '#F56C6C' },
        smooth: true,
        connectNulls: true,
        lineStyle: {
          type: 'dashed'
        }
      }
    ]
  }

  myChart.setOption(option)
}

const handleOffShelf = async (row) => {
  try {
    await ElMessageBox.confirm(`确定下架收购商「${row.shopName || row.shopId}」的这条收购需求吗？`, '违规下架', {
      confirmButtonText: '确定下架',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await adminOffShelfDemand(row.id)
    if (res.code === 200) {
      ElMessage.success('已下架')
      loadDemands()
      loadPriceStats()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除这条收购需求吗？`, '删除确认', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await adminDeleteDemand(row.id)
    if (res.code === 200) {
      ElMessage.success('已删除')
      loadDemands()
      loadPriceStats()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

onMounted(() => {
  loadDemands()
  loadApplications()
  loadPriceStats()
  loadTrend(7)

  // 监听窗口大小变化
  window.addEventListener('resize', () => {
    const chart = echarts.getInstanceByDom(chartContainer.value)
    if (chart) chart.resize()
  })
})
</script>

<style scoped>
.admin-acquisition {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 统计卡片 - 横向并排均匀摆放 */
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.price-low-icon {
  background: linear-gradient(135deg, #67C23A, #85ce61);
}

.price-high-icon {
  background: linear-gradient(135deg, #F56C6C, #f78989);
}

.price-avg-icon {
  background: linear-gradient(135deg, #409eff, #66b1ff);
}

.active-demand-icon {
  background: linear-gradient(135deg, #e6a23c, #ebb563);
}

.stat-content {
  flex: 1;
  min-width: 0;
}

.stat-label {
  font-size: 13px;
  color: #888;
  margin-bottom: 6px;
}

.stat-value {
  font-size: 22px;
  font-weight: 700;
  color: #1a1a2e;
  line-height: 1.2;
}

@media (max-width: 1200px) {
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 576px) {
  .stats-row {
    grid-template-columns: 1fr;
  }
  .stat-card {
    padding: 16px;
  }
  .stat-value {
    font-size: 20px;
  }
}

/* 图表区域 */
.chart-section {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  margin-bottom: 20px;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 12px;
}

.chart-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #1a1a2e;
}

.days-selector {
  display: flex;
  gap: 8px;
}

.chart-container {
  height: 280px;
  width: 100%;
}

/* 列表区域 */
.list-section,
.orders-section {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.orders-section h3 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #1a1a2e;
}

/* 工具栏 */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 12px;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 12px;
}

.stats-info {
  color: #666;
  font-size: 13px;
}

.merchant-info {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>