<template>
  <div class="admin-dashboard">
    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card clickable" @click="goToUsers">
        <div class="stat-icon" style="background: linear-gradient(135deg, #409eff, #66b1ff);">
          <svg width="28" height="28" viewBox="0 0 24 24" fill="white">
            <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
          </svg>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.totalUsers }}</div>
          <div class="stat-label">用户总数</div>
        </div>
        <div class="stat-arrow">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="#999">
            <path d="M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z"/>
          </svg>
        </div>
      </div>
      <div class="stat-card clickable" @click="goToShops">
        <div class="stat-icon" style="background: linear-gradient(135deg, #67c23a, #85ce61);">
          <svg width="28" height="28" viewBox="0 0 24 24" fill="white">
            <path d="M20 4H4v2h16V4zm1 10v-2l-1-5H4l-1 5v2h1v6h10v-6h4v6h2v-6h1zm-9 4H6v-4h6v4z"/>
          </svg>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.totalShops }}</div>
          <div class="stat-label">商家总数</div>
        </div>
        <div class="stat-arrow">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="#999">
            <path d="M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z"/>
          </svg>
        </div>
      </div>
      <div class="stat-card clickable" @click="goToProducts">
        <div class="stat-icon" style="background: linear-gradient(135deg, #e6a23c, #ebb563);">
          <svg width="28" height="28" viewBox="0 0 24 24" fill="white">
            <path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-7 14l-5-5 1.41-1.41L12 14.17l4.59-4.58L18 11l-6 6z"/>
          </svg>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.totalProducts }}</div>
          <div class="stat-label">商品总数</div>
        </div>
        <div class="stat-arrow">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="#999">
            <path d="M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z"/>
          </svg>
        </div>
      </div>
      <div class="stat-card clickable" @click="goToAcquisition">
        <div class="stat-icon" style="background: linear-gradient(135deg, #f56c6c, #f78989);">
          <svg width="28" height="28" viewBox="0 0 24 24" fill="white">
            <path d="M7 18c-1.1 0-1.99.9-1.99 2S5.9 22 7 22s2-.9 2-2-.9-2-2-2zM1 2v2h2l3.6 7.59-1.35 2.45c-.16.28-.25.61-.25.96 0 1.1.9 2 2 2h12v-2H7.42c-.14 0-.25-.11-.25-.25l.03-.12.9-1.63h7.45c.75 0 1.41-.41 1.75-1.03l3.58-6.49c.08-.14.12-.31.12-.48 0-.55-.45-1-1-1H5.21l-.94-2H1zm16 16c-1.1 0-1.99.9-1.99 2s.89 2 1.99 2 2-.9 2-2-.9-2-2-2z"/>
          </svg>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.totalBuyers }}</div>
          <div class="stat-label">收购商总数</div>
        </div>
        <div class="stat-arrow">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="#999">
            <path d="M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z"/>
          </svg>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-row">
      <div class="chart-card">
        <div class="chart-header">
          <h3>用户增长趋势</h3>
          <div class="chart-legend">
            <span class="legend-dot" style="background: #409eff;"></span>
            <span class="legend-text">用户数</span>
          </div>
        </div>
        <div class="chart-container" ref="userChartRef"></div>
      </div>
      <div class="chart-card">
        <div class="chart-header">
          <h3>商品趋势</h3>
          <div class="chart-legend">
            <span class="legend-dot" style="background: #67c23a;"></span>
            <span class="legend-text">商品数</span>
          </div>
        </div>
        <div class="chart-container" ref="productChartRef"></div>
      </div>
    </div>

    <!-- 待处理事项 -->
    <div class="page-container">
      <div class="pending-section">
        <h3 class="section-title">待处理事项</h3>
        <div class="pending-list">
          <div class="pending-item" v-for="item in pendingItems" :key="item.id" @click="handlePending(item)">
            <span class="pending-icon">{{ item.icon }}</span>
            <span class="pending-text">{{ item.text }}</span>
            <el-badge :value="item.count" v-if="item.count" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { useAdminStore } from '@/stores/admin'
import { getUserGrowthTrend, getProductTrend } from '@/api'

const router = useRouter()
const adminStore = useAdminStore()

const stats = computed(() => adminStore.systemStats)

const pendingItems = computed(() => [
  { id: 1, icon: '🏪', text: '待审核商家', count: adminStore.systemStats.pendingShops || 0, path: '/admin/shops' },
  { id: 2, icon: '📦', text: '待审核商品', count: adminStore.systemStats.pendingProducts || 0, path: '/admin/products' },
  { id: 3, icon: '⚠️', text: '待处理投诉', count: adminStore.systemStats.pendingComplaints || 0, path: '/admin/complaints' }
])

const userChartRef = ref(null)
const productChartRef = ref(null)
let userChart = null
let productChart = null

const userGrowthData = ref({ dates: [], counts: [] })
const productTrendData = ref({ dates: [], counts: [] })

const loadUserGrowthTrend = async () => {
  try {
    const res = await getUserGrowthTrend()
    if (res.code === 200 && res.data) {
      userGrowthData.value = {
        dates: res.data.dates || [],
        counts: res.data.counts || []
      }
    }
  } catch (error) {
    console.error('加载用户增长趋势失败:', error)
  }
}

const loadProductTrend = async () => {
  try {
    const res = await getProductTrend()
    if (res.code === 200 && res.data) {
      productTrendData.value = {
        dates: res.data.dates || [],
        counts: res.data.counts || []
      }
    }
  } catch (error) {
    console.error('加载商品趋势失败:', error)
  }
}

// 统一的图表样式配置
const chartColors = {
  primary: '#409eff',
  success: '#67c23a',
  grid: '#f0f0f0',
  text: '#666666',
  textLight: '#999999'
}

// 初始化用户增长图表
const initUserChart = () => {
  if (!userChartRef.value) return

  userChart = echarts.init(userChartRef.value)
  const dates = userGrowthData.value.dates.length > 0 ? userGrowthData.value.dates : ['暂无数据']
  const counts = userGrowthData.value.counts.length > 0 ? userGrowthData.value.counts : [0]

  const option = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.98)',
      borderColor: chartColors.primary,
      borderWidth: 1,
      padding: [12, 16],
      textStyle: { color: '#333', fontSize: 13 }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '12%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: dates,
      boundaryGap: false,
      axisLine: { lineStyle: { color: '#e0e0e0' } },
      axisTick: { show: false },
      axisLabel: {
        color: chartColors.text,
        fontSize: 11
      }
    },
    yAxis: {
      type: 'value',
      min: 0,
      splitLine: { lineStyle: { color: chartColors.grid, type: 'dashed' } },
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: chartColors.text, fontSize: 11 }
    },
    series: [{
      name: '用户数',
      type: 'line',
      data: counts,
      smooth: 0.4,
      symbol: 'circle',
      symbolSize: 8,
      showSymbol: counts.length <= 15,
      lineStyle: { width: 3, color: chartColors.primary },
      itemStyle: { color: chartColors.primary, borderColor: '#fff', borderWidth: 2 },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(64, 158, 255, 0.25)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.02)' }
          ]
        }
      }
    }]
  }

  userChart.setOption(option)
}

// 初始化商品趋势图表
const initProductChart = () => {
  if (!productChartRef.value) return

  productChart = echarts.init(productChartRef.value)
  const dates = productTrendData.value.dates.length > 0 ? productTrendData.value.dates : ['暂无数据']
  const counts = productTrendData.value.counts.length > 0 ? productTrendData.value.counts : [0]

  const option = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.98)',
      borderColor: chartColors.success,
      borderWidth: 1,
      padding: [12, 16],
      textStyle: { color: '#333', fontSize: 13 }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '12%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: dates,
      axisLine: { lineStyle: { color: '#e0e0e0' } },
      axisTick: { show: false },
      axisLabel: { color: chartColors.text, fontSize: 11 }
    },
    yAxis: {
      type: 'value',
      min: 0,
      splitLine: { lineStyle: { color: chartColors.grid, type: 'dashed' } },
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: chartColors.text, fontSize: 11 }
    },
    series: [{
      name: '商品数',
      type: 'bar',
      data: counts,
      barWidth: '50%',
      barMaxWidth: 40,
      itemStyle: {
        color: {
          type: 'linear',
          x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: '#7ed07a' },
            { offset: 1, color: chartColors.success }
          ]
        },
        borderRadius: [6, 6, 0, 0]
      }
    }]
  }

  productChart.setOption(option)
}

// 窗口调整
const handleResize = () => {
  if (userChart) userChart.resize()
  if (productChart) productChart.resize()
}

const handlePending = (item) => {
  router.push(item.path)
}

// 统计卡片点击跳转
const goToUsers = () => router.push('/admin/users')
const goToShops = () => router.push('/admin/shops')
const goToProducts = () => router.push('/admin/products')
const goToAcquisition = () => router.push('/admin/acquisition')

onMounted(async () => {
  await adminStore.loadAdminData()
  await loadUserGrowthTrend()
  await loadProductTrend()
  initUserChart()
  initProductChart()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (userChart) userChart.dispose()
  if (productChart) productChart.dispose()
})
</script>

<style scoped>
.admin-dashboard {
  display: flex;
  flex-direction: column;
  gap: 24px;
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

.stat-card.clickable {
  cursor: pointer;
}

.stat-card.clickable:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.stat-card.clickable:hover .stat-arrow svg {
  transform: translateX(4px);
  transition: transform 0.2s ease;
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

.stat-info {
  flex: 1;
  min-width: 0;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a2e;
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: #888;
  margin-top: 4px;
}

.stat-arrow {
  flex-shrink: 0;
  opacity: 0.6;
}

/* 图表区域 */
.charts-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
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
  margin-bottom: 16px;
}

.chart-header h3 {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: #1a1a2e;
}

.chart-legend {
  display: flex;
  align-items: center;
  gap: 6px;
}

.legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.legend-text {
  font-size: 12px;
  color: #888;
}

.chart-container {
  height: 260px;
  width: 100%;
}

/* 待处理事项 */
.pending-section {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0 0 16px;
}

.pending-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.pending-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 18px;
  background: linear-gradient(135deg, #f8f9fa 0%, #f0f2f5 100%);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid transparent;
}

.pending-item:hover {
  background: linear-gradient(135deg, #ecf5ff 0%, #e6f0ff 100%);
  border-color: rgba(64, 158, 255, 0.15);
  transform: translateX(4px);
}

.pending-icon {
  font-size: 20px;
  flex-shrink: 0;
}

.pending-text {
  flex: 1;
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

@media (max-width: 1024px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .charts-row {
    grid-template-columns: 1fr;
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
    font-size: 22px;
  }
}
</style>
