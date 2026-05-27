<template>
  <div class="price-page">
    <!-- 顶部标题 -->
    <div class="page-header">
      <h1>茉莉花价格监测</h1>
      <p class="page-desc">实时监测横州市各大市场茉莉鲜花价格走势</p>
    </div>

    <!-- 价格趋势总览图 -->
    <div class="section">
      <h2 class="section-title">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="#4A7C59">
          <path d="M3.5 18.49l6-6.01 4 4L22 6.92l-1.41-1.41-7.09 7.97-4-4L2 16.99z"/>
        </svg>
        多市场茉莉花价格对比
      </h2>
      <div class="chart-controls">
        <el-select v-model="selectedChartMarket" placeholder="全部市场" clearable style="width: 160px; margin-right: 12px;" @change="loadMultiMarketChart">
          <el-option v-for="m in allMarketList" :key="m" :label="m" :value="m" />
        </el-select>
        <el-date-picker
          v-model="chartDate"
          type="date"
          placeholder="选择历史日期"
          value-format="YYYY-MM-DD"
          :clearable="true"
          style="width: 160px;"
          @change="loadMultiMarketChart"
        />
      </div>
      <div class="main-chart" ref="multiMarketChartRef"></div>
    </div>

    <!-- 今日最新价格 -->
    <div class="section">
      <h2 class="section-title">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="#e6a23c">
          <path d="M11.8 10.9c-2.27-.59-3-1.2-3-2.15 0-1.09 1.01-1.85 2.7-1.85 1.78 0 2.44.85 2.5 2.1h2.21c-.07-1.72-1.12-3.3-3.21-3.81V3h-3v2.16c-1.94.42-3.5 1.68-3.5 3.61 0 2.31 1.91 3.46 4.7 4.13 2.5.6 3 1.48 3 2.41 0 .69-.49 1.79-2.7 1.79-2.06 0-2.87-.92-2.98-2.1h-2.2c.12 2.19 1.76 3.42 3.68 3.83V21h3v-2.15c1.95-.37 3.5-1.5 3.5-3.55 0-2.84-2.43-3.81-4.7-4.4z"/>
        </svg>
        今日最新价格
      </h2>

      <div class="price-overview" v-if="todayPrices.length > 0">
        <div class="overview-card avg-price">
          <div class="overview-icon">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="#4A7C59">
              <path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-7 14l-5-5 1.41-1.41L12 14.17l4.59-4.58L18 11l-6 6z"/>
            </svg>
          </div>
          <div class="overview-content">
            <div class="overview-label">市场均价</div>
            <div class="overview-value">¥{{ calculateAvgPrice() }}</div>
            <div class="overview-unit">元/斤</div>
          </div>
        </div>
        <div class="overview-card max-price">
          <div class="overview-icon">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="#f56c6c">
              <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z"/>
            </svg>
          </div>
          <div class="overview-content">
            <div class="overview-label">最高价格</div>
            <div class="overview-value" style="color: #f56c6c;">¥{{ getMaxPrice() }}</div>
            <div class="overview-market">{{ getMaxPriceMarket() }}</div>
          </div>
        </div>
        <div class="overview-card min-price">
          <div class="overview-icon">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="#67c23a">
              <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z"/>
            </svg>
          </div>
          <div class="overview-content">
            <div class="overview-label">最低价格</div>
            <div class="overview-value" style="color: #67c23a;">¥{{ getMinPrice() }}</div>
            <div class="overview-market">{{ getMinPriceMarket() }}</div>
          </div>
        </div>
        <div class="overview-card market-count">
          <div class="overview-icon">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="#9c27b0">
              <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z"/>
            </svg>
          </div>
          <div class="overview-content">
            <div class="overview-label">报价市场</div>
            <div class="overview-value" style="color: #9c27b0;">{{ todayPrices.length }}</div>
            <div class="overview-unit">个</div>
          </div>
        </div>
      </div>

      <!-- 市场报价列表 -->
      <div class="price-list-container" v-if="todayPrices.length > 0">
        <div class="price-list-header">
          <span class="col-market">市场名称</span>
          <span class="col-price">最新价格</span>
          <span class="col-change">涨跌</span>
          <span class="col-date">报价时间</span>
          <span class="col-action">操作</span>
        </div>
        <div class="price-list-body">
          <div
            v-for="(item, index) in todayPrices"
            :key="index"
            class="price-row"
            @click="selectMarket(item.market)"
          >
            <span class="col-market">
              <span class="market-icon">🌸</span>
              {{ item.market }}
            </span>
            <span class="col-price">
              <span class="price-main">¥{{ item.avgPrice || item.minPrice }}</span>
              <span class="price-unit-small">元/斤</span>
            </span>
            <span class="col-change">
              <span class="trend-badge" :class="getTrendClass(item.changePercent)">
                {{ getTrendIcon(item.changePercent) }} {{ formatPercent(item.changePercent) }}
              </span>
            </span>
            <span class="col-date">{{ item.recordDate }}</span>
            <span class="col-action">
              <button class="view-btn" @click.stop="openTrendDialog(item.market)">
                查看走势
              </button>
            </span>
          </div>
        </div>
      </div>

      <div v-else class="empty-state">
        <svg width="64" height="64" viewBox="0 0 24 24" fill="#ccc">
          <path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H5V5h14v14zm-7-2h2v-4h4v-2h-4V7h-2v4H8v2h4z"/>
        </svg>
        <p class="empty-text">暂无今日价格数据</p>
        <p class="empty-hint">管理员正在录入价格数据，请稍后再试</p>
      </div>
    </div>

    <!-- 价格预警设置 -->
    <div class="section price-alert-section">
      <h2 class="section-title">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="#f5a623" style="vertical-align: middle; margin-right: 4px;">
          <path d="M12 22c1.1 0 2-.9 2-2h-4c0 1.1.89 2 2 2zm6-6v-5c0-3.07-1.64-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.63 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2z"/>
        </svg>
        价格预警
      </h2>

      <div class="alert-form-card">
        <h3 class="form-title">设置价格提醒</h3>
        <el-form :model="alertForm" inline class="alert-form">
          <el-form-item label="提醒类型">
            <el-select v-model="alertForm.type" style="width: 130px;">
              <el-option label="低于时提醒" :value="1" />
              <el-option label="高于时提醒" :value="2" />
              <el-option label="两者都提醒" :value="3" />
            </el-select>
          </el-form-item>
          <el-form-item label="目标价格">
            <el-input-number v-model="alertForm.targetPrice" :min="0" :precision="2" style="width: 130px;" />
            <span class="unit-label">元/斤</span>
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="alertForm.remark" placeholder="选填" style="width: 160px;" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="addAlert" :loading="submitting">添加预警</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="alert-list-card">
        <div class="list-header">
          <h3 class="form-title">我的预警设置</h3>
          <span class="alert-count">共 {{ myAlerts.length }} 条预警</span>
        </div>

        <div v-if="myAlerts.length > 0" class="alert-list">
          <div v-for="(alert, index) in myAlerts" :key="index" class="alert-item">
            <div class="alert-info">
              <span class="alert-type" :class="alert.type === 1 ? 'type-low' : alert.type === 2 ? 'type-high' : 'type-both'">
                {{ alert.type === 1 ? '低于' : alert.type === 2 ? '高于' : '双向' }}
              </span>
              <span class="alert-price">¥{{ alert.targetPrice }}</span>
              <span class="alert-unit">元/斤</span>
              <span v-if="alert.remark" class="alert-remark">{{ alert.remark }}</span>
            </div>
            <div class="alert-status">
              <el-tag :type="alert.triggered ? 'danger' : 'success'" size="small" effect="light">
                {{ alert.triggered ? '已触发' : '监控中' }}
              </el-tag>
            </div>
            <div class="alert-actions">
              <el-button type="danger" size="small" text @click="deleteAlert(index)">删除</el-button>
            </div>
          </div>
        </div>
        <div v-else class="empty-alerts">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="#ccc">
            <path d="M12 22c1.1 0 2-.9 2-2h-4c0 1.1.89 2 2 2zm6-6v-5c0-3.07-1.64-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.63 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2z"/>
          </svg>
          <p>暂无预警设置</p>
          <p class="empty-hint">设置价格预警，价格变动时将收到提醒</p>
        </div>
      </div>
    </div>

    <!-- 市场价格走势弹框 -->
    <el-dialog v-model="trendDialogVisible" :title="trendDialogMarket + ' 价格走势'" width="700px" destroy-on-close>
      <div class="trend-dialog-controls">
        <el-radio-group v-model="trendDialogDays" @change="loadTrendDialogChart" size="small">
          <el-radio-button :value="7">近7天</el-radio-button>
          <el-radio-button :value="30">近30天</el-radio-button>
          <el-radio-button :value="90">近90天</el-radio-button>
        </el-radio-group>
      </div>
      <div ref="trendDialogChartRef" style="width:100%;height:320px;margin-top:12px;"></div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getMultiMarketTrend, getSingleMarketTrend, getTodayPrices, getAllPriceMarkets } from '@/api'

const todayPrices = ref([])
const selectedMarket = ref(null)

const multiMarketChartRef = ref(null)
const chartDays = ref(7)
const chartDate = ref('')
const selectedChartMarket = ref('')
const allMarketList = ref([])
const showAllMarkets = ref(true)
let multiMarketChart = null

// 走势弹框
const trendDialogVisible = ref(false)
const trendDialogMarket = ref('')
const trendDialogDays = ref(30)
const trendDialogChartRef = ref(null)
let trendDialogChart = null

const marketList = ref([])

// 图表配色（与管理员端一致）
const marketColors = [
  '#e74c3c', '#3498db', '#27ae60', '#9b59b6',
  '#f39c12', '#1abc9c', '#e91e63', '#00bcd4'
]

const calculateAvgPrice = () => {
  if (!todayPrices.value || todayPrices.value.length === 0) return '0.00'
  const sum = todayPrices.value.reduce((acc, item) => acc + (parseFloat(item.avgPrice || item.minPrice) || 0), 0)
  return (sum / todayPrices.value.length).toFixed(2)
}

const getMaxPrice = () => {
  if (!todayPrices.value || todayPrices.value.length === 0) return '0.00'
  const max = Math.max(...todayPrices.value.map(item => parseFloat(item.maxPrice) || 0))
  return max.toFixed(2)
}

const getMaxPriceMarket = () => {
  if (!todayPrices.value || todayPrices.value.length === 0) return ''
  const maxItem = todayPrices.value.reduce((max, item) =>
    (parseFloat(item.maxPrice) || 0) > (parseFloat(max.maxPrice) || 0) ? item : max
  )
  return maxItem.market
}

const getMinPrice = () => {
  if (!todayPrices.value || todayPrices.value.length === 0) return '0.00'
  const min = Math.min(...todayPrices.value.map(item => parseFloat(item.minPrice) || 0))
  return min.toFixed(2)
}

const getMinPriceMarket = () => {
  if (!todayPrices.value || todayPrices.value.length === 0) return ''
  const minItem = todayPrices.value.reduce((min, item) =>
    (parseFloat(item.minPrice) || 0) < (parseFloat(min.minPrice) || 0) ? item : min
  )
  return minItem.market
}

const alertForm = ref({
  type: 1,
  targetPrice: 0,
  remark: ''
})
const myAlerts = ref([])
const submitting = ref(false)

const loadMyAlerts = () => {
  const saved = localStorage.getItem('priceAlerts')
  if (saved) {
    myAlerts.value = JSON.parse(saved)
  }
}

const addAlert = () => {
  if (!alertForm.value.targetPrice || alertForm.value.targetPrice <= 0) {
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
  localStorage.setItem('priceAlerts', JSON.stringify(myAlerts.value))

  alertForm.value = { type: 1, targetPrice: 0, remark: '' }
}

const deleteAlert = (index) => {
  myAlerts.value.splice(index, 1)
  localStorage.setItem('priceAlerts', JSON.stringify(myAlerts.value))
}

const loadTodayPrices = async () => {
  try {
    const res = await getTodayPrices()
    if (res.code === 200 && res.data) {
      todayPrices.value = res.data
      marketList.value = [...new Set(res.data.map(item => item.market))]
    }
  } catch (error) {
    console.error('加载今日价格失败:', error)
  }
}

const loadMultiMarketChart = async () => {
  if (!multiMarketChartRef.value) return
  await nextTick()
  await new Promise(r => setTimeout(r, 100))

  if (!multiMarketChart) {
    multiMarketChart = echarts.init(multiMarketChartRef.value)
  }

  try {
    let queryDays = chartDays.value
    if (chartDate.value) {
      const diff = Math.ceil((new Date() - new Date(chartDate.value)) / (1000 * 60 * 60 * 24))
      queryDays = Math.max(diff + 1, 1)
    }

    const res = await getMultiMarketTrend({ days: queryDays })
    if (res.code !== 200) return

    const trendData = res.data || []
    const marketGroups = {}
    trendData.forEach(p => {
      if (!marketGroups[p.market]) marketGroups[p.market] = {}
      const date = String(p.recordDate).substring(0, 10)
      marketGroups[p.market][date] = {
        maxPrice: parseFloat(p.maxPrice) || 0,
        minPrice: parseFloat(p.minPrice) || 0
      }
    })

    const dates = chartDate.value ? [chartDate.value] : (() => {
      const arr = []
      for (let i = chartDays.value - 1; i >= 0; i--) {
        const d = new Date(); d.setDate(d.getDate() - i)
        arr.push(d.toISOString().split('T')[0])
      }
      return arr
    })()

    const yearStr = dates[0].substring(0, 4) + '年'
    const markets = Object.keys(marketGroups)
    const filteredMarkets = selectedChartMarket.value
      ? markets.filter(m => m === selectedChartMarket.value)
      : markets

    if (filteredMarkets.length === 0) {
      multiMarketChart.setOption({ graphic: [{ type: 'text', left: 'center', top: 'middle', style: { text: '暂无价格数据', fontSize: 14, fill: '#999' } }] })
      return
    }

    const marketCount = filteredMarkets.length
    const barWidthRatio = Math.min(0.8, 0.6 / marketCount)
    const barGap = barWidthRatio * 0.3
    const series = []

    filteredMarkets.forEach((market, idx) => {
      const color = marketColors[idx % marketColors.length]
      const mData = marketGroups[market] || {}
      series.push({
        name: market, type: 'bar', stack: market,
        data: dates.map(d => mData[d]?.minPrice > 0 ? mData[d].minPrice : 0),
        barWidth: `${barWidthRatio * 100}%`, barGap: `${barGap * 100}%`,
        itemStyle: { color: '#9B8AA6', borderRadius: [0,0,0,0] },
        emphasis: { itemStyle: { color: '#9B8AA6' } }
      })
      series.push({
        name: market, type: 'bar', stack: market,
        data: dates.map(d => {
          const v = mData[d]
          return v?.maxPrice > 0 ? Math.max(0, v.maxPrice - (v.minPrice || 0)) : 0
        }),
        barWidth: `${barWidthRatio * 100}%`, barGap: `${barGap * 100}%`,
        itemStyle: { color, borderRadius: [4,4,0,0] },
        emphasis: { itemStyle: { color } }
      })
    })

    const xAxisData = dates.map(d => { const dt = new Date(d); return `${dt.getMonth()+1}/${dt.getDate()}` })

    multiMarketChart.setOption({
      tooltip: {
        trigger: 'axis', axisPointer: { type: 'shadow' },
        backgroundColor: 'rgba(255,255,255,0.98)', borderColor: '#4A7C59', borderWidth: 1,
        padding: [12, 16], textStyle: { color: '#333' },
        enterable: true, extraCssText: 'max-height:260px;overflow-y:auto;pointer-events:auto;',
        formatter(params) {
          if (!params?.length) return ''
          const date = dates[params[0].dataIndex]
          let html = `<div style="font-weight:bold;margin-bottom:8px;">${date}</div>`
          const mMap = {}
          params.forEach(p => {
            if (!mMap[p.seriesName]) mMap[p.seriesName] = { min: 0, range: 0 }
            if (p.color === '#9B8AA6') mMap[p.seriesName].min = p.value
            else mMap[p.seriesName].range = p.value
          })
          Object.keys(mMap).sort().forEach(m => {
            const d = mMap[m], idx = filteredMarkets.indexOf(m), c = marketColors[idx % marketColors.length]
            html += `<div style="margin:6px 0;padding:4px 0;border-bottom:1px solid #eee;">
              <div style="display:flex;align-items:center;gap:6px;margin-bottom:4px;">
                <span style="display:inline-block;width:10px;height:10px;border-radius:2px;background:${c};"></span>
                <span style="font-weight:600;">${m}</span>
              </div>
              <div style="display:flex;justify-content:space-between;gap:20px;padding-left:16px;">
                <span style="color:#9B8AA6;">最低价</span><span style="font-weight:600;">¥${d.min.toFixed(2)}</span>
              </div>
              <div style="display:flex;justify-content:space-between;gap:20px;padding-left:16px;">
                <span style="color:${c};">最高价</span><span style="font-weight:600;">¥${(d.min+d.range).toFixed(2)}</span>
              </div>
            </div>`
          })
          return html
        }
      },
      legend: {
        data: filteredMarkets.map((m, i) => ({ name: m, itemStyle: { color: marketColors[i % marketColors.length] } })),
        orient: 'vertical', right: 5, top: 10,
        textStyle: { color: '#666', fontSize: 11 }, itemWidth: 14, itemHeight: 10, itemGap: 6,
        type: 'scroll', selectedMode: 'multiple',
        selected: (() => { const s = {}; filteredMarkets.forEach(m => { s[m] = true }); return s })()
      },
      grid: { left: '3%', right: '12%', bottom: '10%', top: 50, containLabel: true },
      xAxis: { type: 'category', boundaryGap: true, data: xAxisData, axisLine: { lineStyle: { color: '#e0e0e0' } }, axisTick: { show: false }, axisLabel: { color: '#666', fontSize: 11, interval: 0 }, categoryGap: '50%' },
      yAxis: { type: 'value', name: `${yearStr} 价格（元/斤）`, nameTextStyle: { color: '#666', fontSize: 11 }, axisLine: { show: false }, axisTick: { show: false }, axisLabel: { color: '#666', fontSize: 11 }, splitLine: { lineStyle: { color: '#f0f0f0', type: 'dashed' } }, min: 0 },
      series
    }, true)

    multiMarketChart.off('legendselectchanged')
    multiMarketChart.on('legendselectchanged', (params) => {
      const clicked = params.name
      if (selectedChartMarket.value === clicked) {
        selectedChartMarket.value = ''
        const s = {}; filteredMarkets.forEach(m => { s[m] = true })
        multiMarketChart.setOption({ legend: { selected: s } })
      } else {
        selectedChartMarket.value = clicked
        const s = {}; filteredMarkets.forEach(m => { s[m] = (m === clicked) })
        multiMarketChart.setOption({ legend: { selected: s } })
      }
    })
  } catch (error) {
    console.error('加载多市场图表失败:', error)
  }
}

// 打开走势弹框
const openTrendDialog = async (market) => {
  trendDialogMarket.value = market
  trendDialogDays.value = 30
  trendDialogVisible.value = true
  await nextTick()
  loadTrendDialogChart()
}

const loadTrendDialogChart = async () => {
  await nextTick()
  if (!trendDialogChartRef.value) return
  if (!trendDialogChart) {
    trendDialogChart = echarts.init(trendDialogChartRef.value)
  }
  try {
    const res = await getSingleMarketTrend({ market: trendDialogMarket.value, days: trendDialogDays.value })
    if (res.code !== 200 || !res.data) return
    const data = res.data
    const dates = data.map(item => String(item.recordDate).substring(0, 10)).sort()
    const prices = dates.map(date => {
      const item = data.find(d => String(d.recordDate).substring(0, 10) === date)
      return item ? parseFloat(item.avgPrice || item.minPrice || 0) : null
    })
    trendDialogChart.setOption({
      tooltip: {
        trigger: 'axis', backgroundColor: 'rgba(255,255,255,0.98)', borderColor: '#4A7C59', borderWidth: 1,
        formatter(params) {
          const p = params[0]
          return `<div style="font-weight:600;margin-bottom:4px;">${p.name}</div><div style="color:#4A7C59;font-size:18px;font-weight:bold;">¥${p.value}/斤</div>`
        }
      },
      grid: { left: '3%', right: '4%', bottom: '15%', top: '8%', containLabel: true },
      xAxis: { type: 'category', boundaryGap: false, data: dates, axisLine: { lineStyle: { color: '#e0e0e0' } }, axisTick: { show: false }, axisLabel: { color: '#666', fontSize: 11, rotate: 45, interval: Math.max(0, Math.floor(dates.length / 8) - 1) } },
      yAxis: { type: 'value', name: '元/斤', axisLine: { show: false }, axisTick: { show: false }, axisLabel: { color: '#666' }, splitLine: { lineStyle: { color: '#f0f0f0', type: 'dashed' } } },
      series: [{
        name: trendDialogMarket.value, type: 'line', smooth: 0.5,
        symbol: 'circle', symbolSize: 7, showSymbol: dates.length <= 20,
        data: prices,
        itemStyle: { color: '#4A7C59', borderColor: '#fff', borderWidth: 2 },
        lineStyle: { width: 3, color: '#4A7C59' },
        areaStyle: { color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: 'rgba(74,124,89,0.25)' }, { offset: 1, color: 'rgba(74,124,89,0.03)' }] } }
      }]
    }, true)
  } catch (e) {
    console.error('加载走势图失败:', e)
  }
}

const getTrendClass = (change) => {
  if (!change || change === 0) return 'trend-flat'
  return change > 0 ? 'trend-up' : 'trend-down'
}

const getTrendIcon = (change) => {
  if (!change || change === 0) return '→'
  return change > 0 ? '↑' : '↓'
}

const formatPercent = (change) => {
  if (!change && change !== 0) return '--'
  return (change >= 0 ? '+' : '') + change.toFixed(2) + '%'
}

const handleResize = () => {
  if (multiMarketChart) multiMarketChart.resize()
  if (trendDialogChart) trendDialogChart.resize()
}

// 导入API
import { ElMessage } from 'element-plus'

onMounted(() => {
  loadTodayPrices()
  getAllPriceMarkets().then(res => {
    if (res.code === 200 && res.data) allMarketList.value = res.data
  })
  setTimeout(() => {
    loadMultiMarketChart()
  }, 200)
  loadMyAlerts()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (multiMarketChart) multiMarketChart.dispose()
  if (trendDialogChart) trendDialogChart.dispose()
})
</script>

<style scoped>
.price-page {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
}

.page-header h1 {
  font-size: 28px;
  color: #1a1a2e;
  margin-bottom: 8px;
  font-weight: 700;
}

.page-desc {
  font-size: 14px;
  color: #888;
}

.section {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(0, 0, 0, 0.04);
  transition: box-shadow 0.3s;
}

.section:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.section-title {
  font-size: 17px;
  color: #1a1a2e;
  margin-bottom: 20px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
}

.chart-controls {
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.trend-dialog-controls {
  display: flex;
  justify-content: flex-end;
}

.main-chart {
  width: 100%;
  height: 420px;
  background: #fafbfc;
  border-radius: 12px;
  padding: 16px;
  box-sizing: border-box;
}

.price-overview {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.overview-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  border: 1px solid #eee;
  transition: all 0.3s;
}

.overview-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
}

.overview-icon {
  font-size: 32px;
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(74, 124, 89, 0.1);
  border-radius: 12px;
  flex-shrink: 0;
}

.overview-card.avg-price .overview-icon { background: rgba(74, 124, 89, 0.1); }
.overview-card.max-price .overview-icon { background: rgba(231, 76, 60, 0.1); }
.overview-card.min-price .overview-icon { background: rgba(39, 174, 96, 0.1); }
.overview-card.market-count .overview-icon { background: rgba(147, 112, 219, 0.1); }

.overview-content {
  flex: 1;
  min-width: 0;
}

.overview-label {
  font-size: 13px;
  color: #888;
  margin-bottom: 4px;
  font-weight: 500;
}

.overview-value {
  font-size: 24px;
  font-weight: 700;
  color: #4A7C59;
  line-height: 1.2;
}

.overview-unit {
  font-size: 12px;
  color: #888;
}

.overview-market {
  font-size: 12px;
  color: #666;
  margin-top: 2px;
}

.price-list-container {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #eee;
}

.price-list-header {
  display: grid;
  grid-template-columns: 2fr 1.5fr 1fr 1.5fr 1fr;
  gap: 16px;
  padding: 14px 20px;
  background: linear-gradient(135deg, #4A7C59 0%, #5BA06B 100%);
  color: #fff;
  font-size: 13px;
  font-weight: 600;
}

.price-list-body {
  max-height: 400px;
  overflow-y: auto;
}

.price-row {
  display: grid;
  grid-template-columns: 2fr 1.5fr 1fr 1.5fr 1fr;
  gap: 16px;
  padding: 16px 20px;
  align-items: center;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: all 0.2s;
}

.price-row:hover {
  background: rgba(74, 124, 89, 0.04);
}

.price-row:last-child {
  border-bottom: none;
}

.col-market {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #333;
  font-size: 14px;
}

.market-icon {
  font-size: 16px;
  flex-shrink: 0;
}

.col-price {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.price-main {
  font-size: 18px;
  font-weight: bold;
  color: #4A7C59;
}

.price-unit-small {
  font-size: 12px;
  color: #888;
}

.col-change {
  display: flex;
  align-items: center;
}

.trend-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.trend-badge.trend-up {
  color: #f56c6c;
  background: rgba(231, 76, 60, 0.1);
}

.trend-badge.trend-down {
  color: #27ae60;
  background: rgba(39, 174, 96, 0.1);
}

.trend-badge.trend-flat {
  color: #888;
  background: rgba(153, 153, 153, 0.1);
}

.col-date {
  font-size: 13px;
  color: #666;
}

.col-action {
  display: flex;
  justify-content: flex-end;
}

.view-btn {
  padding: 6px 14px;
  background: transparent;
  border: 1px solid #4A7C59;
  color: #4A7C59;
  border-radius: 8px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
  font-weight: 500;
}

.view-btn:hover {
  background: #4A7C59;
  color: #fff;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
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
}

.price-alert-section {
  background: linear-gradient(135deg, #fff 0%, #f8faf8 100%);
}

.alert-form-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 20px;
  border: 1px solid #e8e8e8;
}

.form-title {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 16px;
}

.alert-form {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.unit-label {
  margin-left: 8px;
  color: #999;
}

.alert-list-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  border: 1px solid #e8e8e8;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.alert-count {
  font-size: 13px;
  color: #888;
}

.alert-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.alert-item {
  display: flex;
  align-items: center;
  padding: 16px;
  background: #f8faf8;
  border-radius: 10px;
  gap: 16px;
  transition: background 0.2s;
}

.alert-item:hover {
  background: #f0f4f0;
}

.alert-info {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
}

.alert-type {
  padding: 4px 12px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
}

.alert-type.type-low {
  background: rgba(39, 174, 96, 0.1);
  color: #27ae60;
}

.alert-type.type-high {
  background: rgba(231, 76, 60, 0.1);
  color: #e74c3c;
}

.alert-type.type-both {
  background: rgba(147, 112, 219, 0.1);
  color: #9370db;
}

.alert-price {
  font-size: 22px;
  font-weight: bold;
  color: #4A7C59;
}

.alert-unit {
  font-size: 12px;
  color: #888;
}

.alert-remark {
  font-size: 13px;
  color: #666;
  padding-left: 12px;
  border-left: 1px solid #ddd;
}

.alert-status {
  min-width: 80px;
  text-align: center;
}

.alert-actions {
  min-width: 60px;
}

.empty-alerts {
  text-align: center;
  padding: 40px 20px;
  color: #999;
}

.empty-alerts svg {
  margin-bottom: 12px;
}

.empty-alerts p {
  margin: 8px 0;
  font-size: 14px;
}

.empty-alerts .empty-hint {
  font-size: 13px;
  color: #bbb;
}

@media (max-width: 1024px) {
  .price-overview {
    grid-template-columns: repeat(2, 1fr);
  }

  .price-list-header,
  .price-row {
    grid-template-columns: 2fr 1.5fr 1fr 1fr;
  }

  .col-date {
    display: none;
  }
}

@media (max-width: 768px) {
  .price-overview {
    grid-template-columns: 1fr;
  }

  .price-list-header {
    display: none;
  }

  .price-row {
    display: flex;
    justify-content: space-between;
    padding: 16px;
    background: #fff;
    border-radius: 8px;
    margin-bottom: 8px;
    border: 1px solid #eee;
  }

  .section {
    padding: 16px;
  }

  .main-chart {
    height: 300px;
  }
}
</style>
