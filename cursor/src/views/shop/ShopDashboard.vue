<template>
  <div class="shop-dashboard">
    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #409eff, #66b1ff);">
          <svg width="28" height="28" viewBox="0 0 24 24" fill="white">
            <path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-5 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z"/>
          </svg>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ localStats.totalOrders }}</div>
          <div class="stat-label">总订单数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #e6a23c, #ebb563);">
          <svg width="28" height="28" viewBox="0 0 24 24" fill="white">
            <path d="M11.8 10.9c-2.27-.59-3-1.2-3-2.15 0-1.09 1.01-1.85 2.7-1.85 1.78 0 2.44.85 2.5 2.1h2.21c-.07-1.72-1.12-3.3-3.21-3.81V3h-3v2.16c-1.94.42-3.5 1.68-3.5 3.61 0 2.31 1.91 3.46 4.7 4.13 2.5.6 3 1.48 3 2.41 0 .69-.49 1.79-2.7 1.79-2.06 0-2.87-.92-2.98-2.1h-2.2c.12 2.19 1.76 3.42 3.68 3.83V21h3v-2.15c1.95-.37 3.5-1.5 3.5-3.55 0-2.84-2.43-3.81-4.7-4.4z"/>
          </svg>
        </div>
        <div class="stat-info">
          <div class="stat-value">¥{{ formatNumber(localStats.totalSales) }}</div>
          <div class="stat-label">总销售额</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #f56c6c, #f78989);">
          <svg width="28" height="28" viewBox="0 0 24 24" fill="white">
            <path d="M20 4H4v2h16V4zm1 10v-2l-1-5H4l-1 5v2h1v6h10v-6h4v6h2v-6h1zm-9 4H6v-4h6v4z"/>
          </svg>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ localStats.totalProducts }}</div>
          <div class="stat-label">在售商品</div>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-row" v-if="localStats.totalOrders > 0">
      <div class="chart-card">
        <div class="chart-header">
          <h3>本周销售额趋势</h3>
          <div class="chart-legend">
            <span class="legend-dot" style="background: #409eff;"></span>
            <span class="legend-text">销售额(元)</span>
          </div>
        </div>
        <div class="chart-container" ref="salesChartRef"></div>
      </div>
    </div>

    <!-- 无数据提示 -->
    <div class="empty-charts" v-else>
      <div class="empty-icon">
        <svg width="64" height="64" viewBox="0 0 24 24" fill="#ddd">
          <path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H5V5h14v14zm-7-2h2v-4h4v-2h-4V7h-2v4H8v2h4z"/>
        </svg>
      </div>
      <p class="empty-text">暂无销售数据</p>
      <p class="empty-hint">请先发布商品并完成交易</p>
    </div>

    <!-- 待处理事项 -->
    <div class="pending-section">
      <h3>待处理事项</h3>
      <div class="pending-list">
        <div class="pending-item" v-for="item in pendingItems" :key="item.id" @click="router.push(item.path)">
          <span class="pending-icon">{{ item.icon }}</span>
          <span class="pending-text">{{ item.text }}</span>
          <el-badge :value="item.count" v-if="item.count" />
        </div>
        <div v-if="pendingItems.every(item => !item.count)" class="empty-pending">
          <p>暂无待处理事项</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { useShopStore } from '@/stores/shop'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const shopStore = useShopStore()
const userStore = useUserStore()

const getShopId = () => userStore.userInfo?.id || null

const localStats = ref({
  totalOrders: 0,
  totalSales: 0,
  totalProducts: 0,
  pendingShip: 0
})

const formatNumber = (num) => {
  if (!num && num !== 0) return '0.00'
  return Number(num).toFixed(2)
}

const salesChartRef = ref(null)
let salesChart = null

// 图表配色方案
const chartColors = {
  primary: '#409eff',
  success: '#67c23a',
  warning: '#e6a23c',
  danger: '#f56c6c',
  purple: '#9c27b0',
  grid: '#f0f0f0',
  text: '#666666'
}

// 计算本周销售数据
const getWeeklySales = () => {
  const days = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
  const values = [0, 0, 0, 0, 0, 0, 0]
  const now = new Date()
  shopStore.shopOrders.forEach(order => {
    if (order.status === 3 && order.createTime) {
      const orderDate = new Date(order.createTime)
      const daysDiff = Math.floor((now - orderDate) / (1000 * 60 * 60 * 24))
      if (daysDiff >= 0 && daysDiff < 7) {
        const dayIndex = (orderDate.getDay() + 6) % 7
        values[dayIndex] += Number(order.actualAmount || 0)
      }
    }
  })
  return { days, values }
}

// 初始化销售额图表
const initSalesChart = () => {
  if (!salesChartRef.value) return

  salesChart = echarts.init(salesChartRef.value)
  const salesData = getWeeklySales()
  const maxVal = Math.max(...salesData.values, 100)

  const option = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.98)',
      borderColor: chartColors.primary,
      borderWidth: 1,
      padding: [12, 16],
      textStyle: { color: '#333', fontSize: 13 },
      formatter: (params) => {
        const p = params[0]
        return `<div style="font-weight: 600; margin-bottom: 6px;">${p.name}</div>
                <div style="color: ${chartColors.primary}; font-size: 16px; font-weight: bold;">
                  <span style="font-size: 12px; color: #999;">销售额 </span>¥${Number(p.value).toFixed(2)}
                </div>`
      }
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
      data: salesData.days,
      axisLine: { lineStyle: { color: '#e0e0e0' } },
      axisTick: { show: false },
      axisLabel: { color: chartColors.text, fontSize: 12 }
    },
    yAxis: {
      type: 'value',
      min: 0,
      max: maxVal * 1.2,
      splitLine: { lineStyle: { color: chartColors.grid, type: 'dashed' } },
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: {
        color: chartColors.text,
        fontSize: 11,
        formatter: (val) => val >= 1000 ? (val / 1000).toFixed(0) + 'k' : val
      }
    },
    series: [{
      name: '销售额',
      type: 'line',
      data: salesData.values,
      smooth: 0.5,
      symbol: 'circle',
      symbolSize: 10,
      showSymbol: true,
      lineStyle: {
        width: 4,
        color: chartColors.primary,
        shadowColor: 'rgba(64, 158, 255, 0.4)',
        shadowBlur: 12,
        shadowOffsetY: 4
      },
      itemStyle: {
        color: chartColors.primary,
        borderColor: '#fff',
        borderWidth: 3,
        shadowColor: 'rgba(64, 158, 255, 0.4)',
        shadowBlur: 8
      },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.02)' }
          ]
        }
      },
      emphasis: {
        scale: true,
        focus: 'series'
      }
    }]
  }

  salesChart.setOption(option)
}

// 更新图表数据
const updateCharts = () => {
  if (salesChart) {
    salesChart.setOption({
      series: [{ data: getWeeklySales().values }]
    })
  }
}

// 窗口调整
const handleResize = () => {
  if (salesChart) salesChart.resize()
}

watch(
  () => shopStore.shopOrders,
  (orders) => {
    const completedOrders = orders.filter(o => o.status === 3)
    localStats.value.totalOrders = orders.length
    localStats.value.totalSales = completedOrders.reduce((sum, o) => sum + Number(o.actualAmount || 0), 0)
    localStats.value.pendingShip = orders.filter(o => o.status === 1).length
    updateCharts()
  },
  { immediate: true, deep: true }
)

watch(
  () => shopStore.products,
  (products) => {
    localStats.value.totalProducts = products.filter(p => p.status === 1).length
  },
  { immediate: true, deep: true }
)

const pendingItems = computed(() => [
  { id: 1, icon: '📦', text: '待发货订单', count: localStats.value.pendingShip, path: '/shop/orders' },
  { id: 2, icon: '💬', text: '未读消息', count: shopStore.unreadCount || 0, path: '/shop/messages' },
  { id: 3, icon: '⚠️', text: '库存不足商品', count: shopStore.lowStockCount || 0, path: '/shop/products' }
])

onMounted(() => {
  setTimeout(() => {
    initSalesChart()
    updateCharts()
  }, 100)
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (salesChart) salesChart.dispose()
})
</script>

<style scoped>
.shop-dashboard {
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

.stat-info {
  min-width: 0;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a2e;
  line-height: 1.2;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.stat-label {
  font-size: 13px;
  color: #888;
  margin-top: 4px;
  font-weight: 500;
}

/* 图表区域 */
.charts-row {
  display: grid;
  grid-template-columns: 1fr;
  gap: 20px;
}

.chart-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
}

.chart-card:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
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
  height: 280px;
  width: 100%;
}

/* 待处理事项 */
.pending-section {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.pending-section h3 {
  margin: 0 0 16px;
  font-size: 15px;
  font-weight: 600;
  color: #1a1a2e;
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
  border-color: rgba(64, 158, 255, 0.2);
  transform: translateX(4px);
}

.pending-icon {
  font-size: 22px;
  flex-shrink: 0;
}

.pending-text {
  flex: 1;
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.empty-pending {
  text-align: center;
  padding: 20px;
  color: #999;
}

.empty-charts {
  background: white;
  border-radius: 12px;
  padding: 60px 40px;
  text-align: center;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(0, 0, 0, 0.04);
}

.empty-icon {
  margin-bottom: 16px;
}

.empty-text {
  font-size: 16px;
  color: #666;
  margin-bottom: 8px;
}

.empty-hint {
  font-size: 13px;
  color: #999;
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
    font-size: 20px;
  }
}
</style>
