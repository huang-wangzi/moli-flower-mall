<template>
  <div class="acquirer-dashboard">
    <div class="page-header">
      <h2>收购数据统计</h2>
      <p class="subtitle">实时掌握茉莉花收购情况</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #4A7C59, #6b9b7a);">
          <svg width="28" height="28" viewBox="0 0 24 24" fill="white">
            <path d="M12 22c1.1 0 2-.9 2-2h-4c0 1.1.89 2 2 2zm6-6v-5c0-3.07-1.64-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.63 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2z"/>
          </svg>
        </div>
        <div class="stat-content">
          <span class="stat-label">今日收购量</span>
          <span class="stat-value">{{ stats.todayQuantity }} <span class="stat-unit">斤</span></span>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #e8a045, #f0c078);">
          <svg width="28" height="28" viewBox="0 0 24 24" fill="white">
            <path d="M11.8 10.9c-2.27-.59-3-1.2-3-2.15 0-1.09 1.01-1.85 2.7-1.85 1.78 0 2.44.85 2.5 2.1h2.21c-.07-1.72-1.12-3.3-3.21-3.81V3h-3v2.16c-1.94.42-3.5 1.68-3.5 3.61 0 2.31 1.91 3.46 4.7 4.13 2.5.6 3 1.48 3 2.41 0 .69-.49 1.79-2.7 1.79-2.06 0-2.87-.92-2.98-2.1h-2.2c.12 2.19 1.76 3.42 3.68 3.83V21h3v-2.15c1.95-.37 3.5-1.5 3.5-3.55 0-2.84-2.43-3.81-4.7-4.4z"/>
          </svg>
        </div>
        <div class="stat-content">
          <span class="stat-label">今日收购额</span>
          <span class="stat-value">¥{{ stats.todayAmount }}<span class="stat-unit"></span></span>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #5b8def, #8ab4f8);">
          <svg width="28" height="28" viewBox="0 0 24 24" fill="white">
            <path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H5V5h14v14zm-7-2h2v-4h4v-2h-4V7h-2v4H8v2h4z"/>
          </svg>
        </div>
        <div class="stat-content">
          <span class="stat-label">待处理订单</span>
          <span class="stat-value">{{ stats.pendingOrders }}<span class="stat-unit">单</span></span>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #9c27b0, #ba68c8);">
          <svg width="28" height="28" viewBox="0 0 24 24" fill="white">
            <path d="M16 6l2.29 2.29-4.88 4.88-4-4L2 16.59 3.41 18l6-6 4 4 6.3-6.29L22 12V6z"/>
          </svg>
        </div>
        <div class="stat-content">
          <span class="stat-label">累计收购量</span>
          <span class="stat-value">{{ stats.totalQuantity }}<span class="stat-unit">斤</span></span>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section">
      <div class="chart-card">
        <div class="chart-header">
          <h3>近7日收购趋势</h3>
          <div class="chart-legend">
            <span class="legend-item">
              <span class="legend-bar" style="background: #4A7C59;"></span>
              收购量(斤)
            </span>
            <span class="legend-item">
              <span class="legend-line" style="border-color: #e8a045;"></span>
              收购额(元)
            </span>
          </div>
        </div>
        <div class="chart-container" ref="chartRef"></div>
      </div>
    </div>

    <!-- 最近订单 -->
    <div class="recent-orders">
      <h3>最近收购订单</h3>
      <div v-if="recentOrders.length > 0" class="orders-list">
        <div v-for="order in recentOrders" :key="order.id" class="order-item">
          <div class="order-info">
            <span class="farmer-name">{{ order.farmerName || '花农' }}</span>
            <span class="order-time">{{ formatTime(order.createdAt) }}</span>
          </div>
          <div class="order-detail">
            <span class="quantity">{{ order.quantity }} 斤</span>
            <span class="amount">¥{{ order.totalAmount || 0 }}</span>
          </div>
          <span class="order-status" :class="getStatusClass(order.status)">
            {{ getStatusText(order.status) }}
          </span>
        </div>
      </div>
      <div v-else class="empty-orders">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="#ccc">
          <path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H5V5h14v14zm-7-2h2v-4h4v-2h-4V7h-2v4H8v2h4z"/>
        </svg>
        <p>暂无收购订单</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'
import * as echarts from 'echarts'
import { getMerchantStats, getMerchantDailyStats, getMerchantOrders } from '@/api/acquisition'

const stats = ref({
  todayQuantity: 0,
  todayAmount: 0,
  pendingOrders: 0,
  totalQuantity: 0
})

const chartData = ref([])
const recentOrders = ref([])
const chartRef = ref(null)
let mainChart = null

// 图表配色
const chartColors = {
  primary: '#4A7C59',
  primaryLight: '#6b9b7a',
  secondary: '#e8a045',
  secondaryLight: '#f0c078',
  grid: '#f0f0f0',
  text: '#666666'
}

const loadStats = async () => {
  try {
    const res = await getMerchantStats()
    if (res.code === 200) {
      stats.value = {
        todayQuantity: res.data?.todayQuantity || 0,
        todayAmount: res.data?.todayAmount || 0,
        pendingOrders: res.data?.pendingOrders || 0,
        totalQuantity: res.data?.totalQuantity || 0
      }
    }
  } catch (error) {
    console.error('加载统计数据失败', error)
  }
}

const loadChartData = async () => {
  try {
    // 使用本地时区计算日期
    const today = new Date()
    const year = today.getFullYear()
    const month = String(today.getMonth() + 1).padStart(2, '0')
    const day = String(today.getDate()).padStart(2, '0')
    const endDate = `${year}-${month}-${day}`
    
    const startDateObj = new Date(today)
    startDateObj.setDate(startDateObj.getDate() - 6)
    const startYear = startDateObj.getFullYear()
    const startMonth = String(startDateObj.getMonth() + 1).padStart(2, '0')
    const startDay = String(startDateObj.getDate()).padStart(2, '0')
    const startDate = `${startYear}-${startMonth}-${startDay}`
    
    console.log('[收购商图表] 请求日期范围:', startDate, '至', endDate)
    const res = await getMerchantDailyStats(startDate, endDate)
    console.log('[收购商图表] API响应:', res)
    if (res.code === 200 && res.data) {
      console.log('[收购商图表] 后端返回数据:', res.data)
      const dataMap = {}
      res.data.forEach(item => {
        dataMap[item.date] = item
      })
      console.log('[收购商图表] 数据映射:', dataMap)

      const result = []
      for (let i = 6; i >= 0; i--) {
        const dateObj = new Date(today)
        dateObj.setDate(dateObj.getDate() - i)
        const dateYear = dateObj.getFullYear()
        const dateMonth = String(dateObj.getMonth() + 1).padStart(2, '0')
        const dateDay = String(dateObj.getDate()).padStart(2, '0')
        const dateStr = `${dateYear}-${dateMonth}-${dateDay}`
        const displayDate = `${dateObj.getMonth() + 1}/${dateObj.getDate()}`
        const dayData = dataMap[dateStr] || { date: dateStr, quantity: 0, amount: 0 }
        result.push({
          ...dayData,
          displayDate: displayDate,
          dateStr: dateStr
        })
      }
      console.log('[收购商图表] 处理后数据:', result)
      chartData.value = result
    } else {
      console.log('[收购商图表] 无数据或请求失败')
      chartData.value = []
    }
  } catch (error) {
    console.error('加载图表数据失败', error)
    chartData.value = []
  }
}

const initChart = () => {
  if (!chartRef.value) return

  mainChart = echarts.init(chartRef.value)
  updateChart()
}

const updateChart = () => {
  if (!mainChart) return

  const dates = chartData.value.map(d => d.displayDate)
  // 确保转换为数字，处理 BigDecimal 等特殊类型
  const quantities = chartData.value.map(d => {
    const val = d.quantity ?? d.totalQuantity ?? 0
    return typeof val === 'number' ? val : parseFloat(val) || 0
  })
  const amounts = chartData.value.map(d => {
    const val = d.amount ?? d.totalAmount ?? 0
    return typeof val === 'number' ? val : parseFloat(val) || 0
  })

  console.log('[收购商图表] 提取的日期:', dates)
  console.log('[收购商图表] 提取的收购量:', quantities)
  console.log('[收购商图表] 提取的收购额:', amounts)

  const hasData = chartData.value.length > 0 && quantities.some(q => q > 0)

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
          if (p.seriesName === '收购量') {
            result += `<div style="display: flex; align-items: center; gap: 8px; margin: 4px 0;">
              <span style="display: inline-block; width: 10px; height: 10px; border-radius: 2px; background: ${p.color};"></span>
              <span style="flex: 1;">${p.seriesName}</span>
              <span style="font-weight: 600; color: ${p.color};">${p.value} 斤</span>
            </div>`
          } else {
            result += `<div style="display: flex; align-items: center; gap: 8px; margin: 4px 0;">
              <span style="display: inline-block; width: 10px; height: 3px; border-top: 2px solid ${p.color};"></span>
              <span style="flex: 1;">${p.seriesName}</span>
              <span style="font-weight: 600; color: ${p.color};">¥${Number(p.value).toFixed(2)}</span>
            </div>`
          }
        })
        return result
      }
    },
    legend: {
      show: false
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '12%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: dates,
      axisLine: { lineStyle: { color: '#e0e0e0' } },
      axisTick: { show: false },
      axisLabel: { color: chartColors.text, fontSize: 12 }
    },
    yAxis: [
      {
        type: 'value',
        name: '收购量(斤)',
        position: 'left',
        nameTextStyle: { color: chartColors.text, fontSize: 11 },
        axisLine: { show: false },
        axisTick: { show: false },
        axisLabel: { color: chartColors.text, fontSize: 11 },
        splitLine: { lineStyle: { color: chartColors.grid, type: 'dashed' } }
      },
      {
        type: 'value',
        name: '收购额(元)',
        position: 'right',
        nameTextStyle: { color: chartColors.text, fontSize: 11 },
        axisLine: { show: false },
        axisTick: { show: false },
        axisLabel: {
          color: chartColors.text,
          fontSize: 11,
          formatter: (val) => val >= 1000 ? (val / 1000).toFixed(0) + 'k' : val
        },
        splitLine: { show: false }
      }
    ],
    series: [
      {
        name: '收购量',
        type: 'bar',
        data: quantities,
        yAxisIndex: 0,
        barWidth: '40%',
        barMaxWidth: 40,
        itemStyle: {
          color: {
            type: 'linear',
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: chartColors.primaryLight },
              { offset: 1, color: chartColors.primary }
            ]
          },
          borderRadius: [6, 6, 0, 0]
        },
        emphasis: {
          itemStyle: {
            color: {
              type: 'linear',
              x: 0, y: 0, x2: 0, y2: 1,
              colorStops: [
                { offset: 0, color: '#7daf8a' },
                { offset: 1, color: '#5a9469' }
              ]
            }
          }
        },
        label: {
          show: quantities.some(q => q > 0),
          position: 'top',
          color: chartColors.text,
          fontSize: 10,
          formatter: (p) => p.value > 0 ? p.value : ''
        }
      },
      {
        name: '收购额',
        type: 'line',
        data: amounts,
        yAxisIndex: 1,
        smooth: 0.5,
        symbol: 'circle',
        symbolSize: 8,
        showSymbol: true,
        lineStyle: {
          width: 3,
          color: chartColors.secondary,
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
        emphasis: {
          scale: true
        }
      }
    ]
  }

  mainChart.setOption(option, true)
}

const loadRecentOrders = async () => {
  try {
    const res = await getMerchantOrders(1, 5)
    if (res.code === 200 && res.data) {
      recentOrders.value = (res.data.records || []).slice(0, 5)
    }
  } catch (error) {
    console.error('加载最近订单失败', error)
  }
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getMonth() + 1}月${date.getDate()}日 ${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`
}

const getStatusClass = (status) => {
  switch (status) {
    case 0: return 'status-pending'
    case 1: return 'status-agreed'
    case 2: return 'status-delivering'
    case 3: return 'status-completed'
    default: return ''
  }
}

const getStatusText = (status) => {
  switch (status) {
    case 0: return '待审核'
    case 1: return '已同意'
    case 2: return '交付中'
    case 3: return '已完成'
    default: return '未知'
  }
}

const handleResize = () => {
  if (mainChart) mainChart.resize()
}

onMounted(async () => {
  await loadStats()
  await loadChartData()
  await loadRecentOrders()
  await nextTick()
  initChart()
  window.addEventListener('resize', handleResize)
})

// 监听图表数据变化，自动更新图表
watch(() => chartData.value, () => {
  console.log('[收购商图表] 数据变化触发，当前数据:', chartData.value)
  nextTick(() => {
    if (mainChart) {
      console.log('[收购商图表] 更新图表')
      updateChart()
    } else if (chartRef.value) {
      console.log('[收购商图表] 初始化图表')
      initChart()
    } else {
      console.log('[收购商图表] chartRef.value 为空')
    }
  })
}, { deep: true })

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (mainChart) mainChart.dispose()
})
</script>

<style scoped>
.acquirer-dashboard {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 页面标题 */
.page-header {
  margin-bottom: 4px;
}

.page-header h2 {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 8px;
}

.subtitle {
  color: #888;
  font-size: 14px;
}

/* 统一统计卡片样式 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 4px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 18px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  border: 1px solid rgba(0, 0, 0, 0.04);
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-content {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.stat-label {
  font-size: 13px;
  color: #888;
  margin-bottom: 6px;
  font-weight: 500;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a2e;
  line-height: 1.2;
}

.stat-unit {
  font-size: 12px;
  color: #888;
  font-weight: 400;
  margin-left: 2px;
}

/* 图表区域 */
.charts-section {
  margin-bottom: 4px;
}

.chart-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(0, 0, 0, 0.04);
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.chart-card h3 {
  font-size: 15px;
  color: #1a1a2e;
  margin: 0;
  font-weight: 600;
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
  height: 320px;
  width: 100%;
}

/* 最近订单 */
.recent-orders {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(0, 0, 0, 0.04);
}

.recent-orders h3 {
  font-size: 15px;
  color: #1a1a2e;
  margin: 0 0 16px;
  font-weight: 600;
}

.orders-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.order-item {
  display: flex;
  align-items: center;
  padding: 14px 16px;
  background: linear-gradient(135deg, #f8f9fa 0%, #f5f6f8 100%);
  border-radius: 12px;
  gap: 16px;
  transition: all 0.2s;
  border: 1px solid transparent;
}

.order-item:hover {
  background: linear-gradient(135deg, #f0f4f2 0%, #e8f0ed 100%);
  border-color: rgba(74, 124, 89, 0.15);
}

.order-info {
  flex: 1;
  min-width: 0;
}

.farmer-name {
  font-weight: 600;
  color: #333;
  display: block;
  font-size: 14px;
  margin-bottom: 4px;
}

.order-time {
  font-size: 12px;
  color: #999;
}

.order-detail {
  text-align: center;
  min-width: 100px;
}

.quantity {
  display: block;
  color: #4A7C59;
  font-weight: 700;
  font-size: 15px;
  margin-bottom: 2px;
}

.amount {
  display: block;
  font-size: 14px;
  color: #e8a045;
  font-weight: 600;
}

.order-status {
  padding: 5px 12px;
  border-radius: 20px;
  font-size: 12px;
  color: white;
  font-weight: 500;
  flex-shrink: 0;
}

.status-pending { background: linear-gradient(135deg, #ff9800, #ffb74d); }
.status-agreed { background: linear-gradient(135deg, #2196f3, #64b5f6); }
.status-delivering { background: linear-gradient(135deg, #9c27b0, #ba68c8); }
.status-completed { background: linear-gradient(135deg, #4caf50, #81c784); }

.empty-orders {
  text-align: center;
  padding: 48px 20px;
  color: #999;
}

.empty-orders svg {
  margin-bottom: 12px;
}

.empty-orders p {
  margin: 0;
  font-size: 14px;
}

@media (max-width: 1024px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  .stat-card {
    padding: 18px;
  }
  .stat-value {
    font-size: 20px;
  }
  .chart-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}
</style>
