<template>
  <div class="shop-price-page">
    <div class="page-header">
      <h1 class="page-title">价格监测</h1>
      <p class="page-desc">查看各市场的茉莉花实时价格，把握市场动态</p>
    </div>

    <!-- 分类筛选 -->
    <div class="category-filter">
      <el-radio-group v-model="selectedCategory" @change="handleCategoryChange">
        <el-radio-button :value="1">茉莉鲜花</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 价格概览 -->
    <div class="section">
      <h2 class="section-title">各市场最新价格</h2>
      <el-table :data="priceData" border stripe v-loading="loading" :header-cell-style="{ background: '#f5f7fa', color: '#333' }">
        <el-table-column prop="market" label="市场" width="180" />
        <el-table-column prop="category" label="品种" width="120">
          <template #default="{ row }">
            {{ getCategoryName(row.category) }}
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格" width="140">
          <template #default="{ row }">
            <span class="price-value">¥{{ row.price }}</span>
            <span class="price-unit">{{ row.unit }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="recordDate" label="记录日期" width="130" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewTrend(row.market, row.category)">查看趋势</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 价格走势图 -->
    <div class="section chart-section">
      <h2 class="section-title">
        {{ selectedMarket || '全部市场' }} - {{ selectedMarketCategoryName }} 价格走势
      </h2>
      <div class="time-range">
        <el-radio-group v-model="timeRange" @change="loadTrendData">
          <el-radio-button :value="7">近7天</el-radio-button>
          <el-radio-button :value="30">近30天</el-radio-button>
          <el-radio-button :value="90">近90天</el-radio-button>
        </el-radio-group>
      </div>
      <div class="chart-container" ref="chartRef" v-show="showChart"></div>
      <div v-if="!showChart" class="empty-chart">
        <svg width="64" height="64" viewBox="0 0 24 24" fill="#ccc">
          <path d="M3.5 18.49l6-6.01 4 4L22 6.92l-1.41-1.41-7.09 7.97-4-4L2 16.99z"/>
        </svg>
        <p>点击"查看趋势"查看详细图表</p>
      </div>
    </div>

    <!-- 价格统计 -->
    <div class="section">
      <h2 class="section-title">价格统计</h2>
      <el-row :gutter="20">
        <el-col :span="6" v-for="stat in priceStats" :key="stat.label">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-label">{{ stat.label }}</div>
            <div class="stat-value" :class="getStatClass(stat.label)">{{ stat.value }}</div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 价格预警设置 -->
    <div class="section price-alert-section">
      <h2 class="section-title">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="#f5a623" style="margin-right: 6px; vertical-align: middle;">
          <path d="M12 22c1.1 0 2-.9 2-2h-4c0 1.1.89 2 2 2zm6-6v-5c0-3.07-1.64-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.63 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2z"/>
        </svg>
        价格预警
      </h2>

      <el-card class="alert-form-card">
        <el-form :model="alertForm" inline>
          <el-form-item label="提醒类型">
            <el-select v-model="alertForm.type" style="width: 130px;">
              <el-option label="低于时提醒" :value="1" />
              <el-option label="高于时提醒" :value="2" />
              <el-option label="两者都提醒" :value="3" />
            </el-select>
          </el-form-item>
          <el-form-item label="目标价格">
            <el-input-number v-model="alertForm.targetPrice" :min="0" :precision="2" style="width: 130px;" />
            <span style="margin-left: 8px; color: #999;">元/斤</span>
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="alertForm.remark" placeholder="选填" style="width: 160px;" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="addAlert" :loading="submitting">添加预警</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <el-card class="alert-list-card">
        <template #header>
          <div style="display: flex; justify-content: space-between; align-items: center;">
            <span>我的预警设置</span>
            <span style="color: #999; font-size: 13px;">共 {{ myAlerts.length }} 条预警</span>
          </div>
        </template>

        <div v-if="myAlerts.length > 0">
          <div v-for="(alert, index) in myAlerts" :key="index" class="alert-item">
            <div class="alert-info">
              <el-tag :type="alert.type === 1 ? 'success' : alert.type === 2 ? 'danger' : 'warning'" size="small">
                {{ alert.type === 1 ? '低于' : alert.type === 2 ? '高于' : '双向' }}
              </el-tag>
              <span class="alert-price">¥{{ alert.targetPrice }}</span>
              <span style="color: #999; font-size: 12px;">元/斤</span>
              <span v-if="alert.remark" style="color: #666; font-size: 13px; margin-left: 12px; padding-left: 12px; border-left: 1px solid #ddd;">{{ alert.remark }}</span>
            </div>
            <div style="display: flex; align-items: center; gap: 12px;">
              <el-tag :type="alert.triggered ? 'danger' : 'success'" size="small">
                {{ alert.triggered ? '已触发' : '监控中' }}
              </el-tag>
              <el-button type="danger" size="small" link @click="deleteAlert(index)">删除</el-button>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无预警设置，添加预警后价格变动时将收到提醒" />
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { getLatestPrices, getPriceTrend, getPriceStats } from '@/api'

const loading = ref(false)
const priceData = ref([])
const selectedCategory = ref(1)
const selectedMarket = ref(null)
const selectedMarketCategory = ref(null)
const timeRange = ref(30)
const showChart = ref(false)
const chartRef = ref(null)
const priceStats = ref([])
let trendChart = null

const categoryMap = {
  1: '茉莉鲜花'
}

const getCategoryName = (category) => {
  return categoryMap[category] || '茉莉鲜花'
}

const selectedMarketCategoryName = computed(() => {
  return selectedMarketCategory.value ? getCategoryName(selectedMarketCategory.value) : '全部'
})

// 图表配色
const chartColors = {
  primary: '#4A7C59',
  primaryLight: '#6b9b7a',
  grid: '#f0f0f0',
  text: '#666666'
}

// 预警表单
const alertForm = ref({
  type: 1,
  targetPrice: 0,
  remark: ''
})
const myAlerts = ref([])
const submitting = ref(false)

const loadMyAlerts = () => {
  const saved = localStorage.getItem('shopPriceAlerts')
  if (saved) {
    myAlerts.value = JSON.parse(saved)
  }
}

const addAlert = () => {
  if (!alertForm.value.targetPrice || alertForm.value.targetPrice <= 0) {
    ElMessage.warning('请输入有效的目标价格')
    return
  }

  const newAlert = {
    type: alertForm.value.type,
    targetPrice: alertForm.value.targetPrice,
    remark: alertForm.value.remark,
    triggered: false,
    createTime: new Date().toLocaleString()
  }

  myAlerts.value.push(newAlert)
  localStorage.setItem('shopPriceAlerts', JSON.stringify(myAlerts.value))

  alertForm.value = { type: 1, targetPrice: 0, remark: '' }
  ElMessage.success('预警设置成功')
}

const deleteAlert = (index) => {
  myAlerts.value.splice(index, 1)
  localStorage.setItem('shopPriceAlerts', JSON.stringify(myAlerts.value))
  ElMessage.success('已删除预警')
}

// 加载价格数据
const loadPriceData = async () => {
  loading.value = true
  try {
    const res = await getLatestPrices()
    if (res.code === 200) {
      let data = res.data || []
      if (selectedCategory.value) {
        data = data.filter(item => item.category === selectedCategory.value)
      }
      priceData.value = data
    }
  } catch (error) {
    console.error('获取价格数据失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载趋势数据
const loadTrendData = async () => {
  if (!selectedMarket.value) {
    ElMessage.warning('请先选择一个市场查看趋势')
    return
  }

  loading.value = true
  try {
    const params = {
      category: selectedMarketCategory.value || undefined,
      days: timeRange.value
    }
    const res = await getPriceTrend(params)
    if (res.code === 200) {
      renderChart(res.data || [])
    }
  } catch (error) {
    console.error('获取趋势数据失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载统计数据
const loadStats = async () => {
  try {
    const params = {
      category: selectedCategory.value || undefined
    }
    const res = await getPriceStats(params)
    if (res.code === 200) {
      const data = res.data || {}
      priceStats.value = [
        { label: '记录数', value: data.count || 0 },
        { label: '平均价格', value: '¥' + (data.avgPrice || '0.00') },
        { label: '最高价格', value: '¥' + (data.maxPrice || '0.00') },
        { label: '最低价格', value: '¥' + (data.minPrice || '0.00') }
      ]
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

const getStatClass = (label) => {
  if (label === '最高价格') return 'stat-high'
  if (label === '最低价格') return 'stat-low'
  return ''
}

// 查看趋势
const viewTrend = (market, category) => {
  selectedMarket.value = market
  selectedMarketCategory.value = category
  showChart.value = true
  nextTick(() => {
    loadTrendData()
  })
}

// 渲染图表
const renderChart = (data) => {
  nextTick(() => {
    if (!chartRef.value) return

    if (!trendChart) {
      trendChart = echarts.init(chartRef.value)
    }

    const dates = [...new Set(data.map(item => item.recordDate))].sort()
    const markets = [...new Set(data.map(item => item.market))]
    const greenColors = ['#4A7C59', '#5BA06B', '#6DC47D', '#8DD99C', '#AEEBBE', '#2D5A3D', '#3D7A4D', '#1D4A2D']

    const series = markets.map((market, index) => {
      const marketData = data.filter(item => item.market === market)
      const priceMap = {}
      marketData.forEach(item => {
        priceMap[item.recordDate] = item.price
      })

      const color = greenColors[index % greenColors.length]

      return {
        name: market,
        type: 'line',
        smooth: 0.4,
        symbol: 'circle',
        symbolSize: 7,
        showSymbol: dates.length <= 15,
        data: dates.map(date => priceMap[date] || null),
        itemStyle: {
          color: color,
          borderColor: '#fff',
          borderWidth: 2,
          shadowColor: color + '40',
          shadowBlur: 6
        },
        lineStyle: {
          width: 3,
          color: color,
          shadowColor: color + '40',
          shadowBlur: 8,
          shadowOffsetY: 3
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: color + '30' },
              { offset: 1, color: color + '05' }
            ]
          }
        }
      }
    })

    trendChart.setOption({
      tooltip: {
        trigger: 'axis',
        backgroundColor: 'rgba(255, 255, 255, 0.98)',
        borderColor: chartColors.primary,
        borderWidth: 1,
        padding: [12, 16],
        textStyle: { color: '#333' },
        formatter: function(params) {
          let result = '<div style="font-weight:bold;margin-bottom:8px;font-size:13px;">' + params[0].name + '</div>'
          params.forEach(p => {
            if (p.value !== null && p.value !== undefined) {
              result += '<div style="display:flex;align-items:center;gap:8px;margin:4px 0;">'
              result += '<span style="display:inline-block;width:10px;height:10px;border-radius:50%;background:' + p.color + ';box-shadow:0 0 4px ' + p.color + '40;"></span>'
              result += '<span style="flex:1;color:#666;">' + p.seriesName + '</span>'
              result += '<span style="font-weight:bold;color:' + p.color + ';font-size:14px;">¥' + p.value + '</span>'
              result += '</div>'
            }
          })
          return result
        }
      },
      legend: {
        data: markets,
        bottom: 10,
        textStyle: { color: chartColors.text }
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '18%',
        top: '8%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: dates,
        axisLine: { lineStyle: { color: '#e0e0e0' } },
        axisTick: { show: false },
        axisLabel: {
          color: chartColors.text,
          rotate: 45,
          fontSize: 11,
          formatter: function(value) {
            return value.substring(5)
          }
        }
      },
      yAxis: {
        type: 'value',
        name: '价格（元）',
        nameTextStyle: { color: chartColors.text, fontSize: 11 },
        axisLine: { show: false },
        axisTick: { show: false },
        axisLabel: { color: chartColors.text },
        splitLine: { lineStyle: { color: chartColors.grid, type: 'dashed' } }
      },
      series: series
    }, true)
  })
}

const handleCategoryChange = () => {
  selectedMarket.value = null
  selectedMarketCategory.value = null
  showChart.value = false
  loadPriceData()
  loadStats()
}

const handleResize = () => {
  if (trendChart) {
    trendChart.resize()
  }
}

onMounted(() => {
  loadPriceData()
  loadStats()
  loadMyAlerts()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (trendChart) {
    trendChart.dispose()
  }
})
</script>

<style scoped>
.shop-price-page {
  padding: 20px;
}

.page-header {
  margin-bottom: 30px;
}

.page-title {
  font-size: 24px;
  color: #1a1a2e;
  margin-bottom: 8px;
  font-weight: 700;
}

.page-desc {
  color: #888;
  font-size: 14px;
}

.category-filter {
  margin-bottom: 20px;
}

.section {
  background: white;
  padding: 24px;
  border-radius: 16px;
  margin-bottom: 20px;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(0, 0, 0, 0.04);
}

.chart-section {
  padding: 24px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 16px;
}

.price-value {
  color: #e74c3c;
  font-weight: bold;
  font-size: 15px;
}

.price-unit {
  color: #999;
  font-size: 12px;
  margin-left: 4px;
}

.time-range {
  margin-bottom: 20px;
}

.chart-container {
  height: 420px;
  width: 100%;
}

.empty-chart {
  height: 300px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f8f9fa 0%, #f0f2f5 100%);
  border-radius: 12px;
  color: #999;
}

.empty-chart svg {
  margin-bottom: 12px;
}

.empty-chart p {
  margin: 0;
  font-size: 14px;
}

.stat-card {
  text-align: center;
  border-radius: 12px;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-label {
  font-size: 13px;
  color: #888;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 22px;
  font-weight: 700;
  color: #4A7C59;
}

.stat-value.stat-high {
  color: #f56c6c;
}

.stat-value.stat-low {
  color: #67c23a;
}

.price-alert-section {
  background: linear-gradient(135deg, #fff 0%, #f8faf8 100%);
}

.alert-form-card {
  margin-bottom: 16px;
  border-radius: 12px;
}

.alert-list-card {
  border-radius: 12px;
}

.alert-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 10px;
  margin-bottom: 12px;
  transition: all 0.2s;
}

.alert-item:hover {
  background: #f0f2f5;
}

.alert-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.alert-price {
  font-size: 22px;
  font-weight: bold;
  color: #4A7C59;
}

@media (max-width: 768px) {
  .section {
    padding: 16px;
  }
  .chart-container {
    height: 300px;
  }
}
</style>
