<template>
  <div class="home-page">
    <!-- 轮播图 -->
    <div class="banner-section">
      <el-carousel height="420px" indicator-position="outside" :interval="5000">
        <el-carousel-item v-for="(banner, index) in banners" :key="index">
          <div class="banner-item" :style="{ background: banner.bg }">
            <div class="banner-content">
              <h2>{{ banner.title }}</h2>
              <p>{{ banner.desc }}</p>
              <button class="btn-primary" @click="$router.push(banner.link)">
                {{ banner.btnText }}
              </button>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>

    <!-- 价格监测模块 -->
    <div class="price-monitor-section">
      <div class="section-header">
        <h2 class="page-title">横州茉莉鲜花价格监测</h2>
        <span class="more-link" @click="$router.push('/price-detail')">
          查看详情 →
        </span>
      </div>

      <!-- AI价格预警卡片 -->
      <div class="price-alert-card" @click="$router.push('/price-detail')">
        <div class="alert-header">
          <span class="alert-icon">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="#f5a623">
              <path d="M12 22c1.1 0 2-.9 2-2h-4c0 1.1.89 2 2 2zm6-6v-5c0-3.07-1.64-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.63 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2z"/>
            </svg>
          </span>
          <span class="alert-title">AI价格分析</span>
          <span class="alert-time">{{ currentDate }}</span>
        </div>
        <div class="alert-content" v-if="aiAlertContent">
          <p class="alert-text">{{ aiAlertContent }}</p>
        </div>
        <div class="alert-content" v-else>
          <p class="alert-loading">正在生成价格分析...</p>
        </div>
        <div class="alert-footer">
          <span class="view-more">点击查看详情和设置预警 →</span>
        </div>
      </div>

      <!-- 今日价格概览 -->
      <div class="price-overview" v-if="todayPrices.length > 0">
        <div class="overview-card avg-price">
          <div class="overview-icon">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="#4A7C59">
              <path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-7 14l-5-5 1.41-1.41L12 14.17l4.59-4.58L18 11l-6 6z"/>
            </svg>
          </div>
          <div class="overview-content">
            <div class="overview-label">市场均价</div>
            <div class="overview-value">¥{{ calcAvg() }}</div>
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
            <div class="overview-value" style="color: #f56c6c;">¥{{ calcMax().price }}</div>
            <div class="overview-market">{{ calcMax().market }}</div>
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
            <div class="overview-value" style="color: #67c23a;">¥{{ calcMin().price }}</div>
            <div class="overview-market">{{ calcMin().market }}</div>
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
    </div>

    <!-- 商品推荐 -->
    <div class="products-section">
      <div class="section-header">
        <h2 class="page-title">热门商品</h2>
        <span class="more-link" @click="$router.push('/products')">
          查看更多 →
        </span>
      </div>

      <div v-if="loading" class="loading-state">
        <el-icon class="is-loading"><Loading /></el-icon>
        <p>加载中...</p>
      </div>

      <div v-else-if="hotProducts.length > 0" class="product-grid">
        <div
          v-for="product in hotProducts"
          :key="product.id"
          class="neumorphic-card product-card"
          @click="$router.push(`/product/${product.id}`)"
        >
          <div class="product-image">
            <img :src="getProductImage(product)" :alt="product.name" />
            <span class="product-tag">{{ product.categoryName || '商品' }}</span>
          </div>
          <div class="product-info">
            <h4 class="product-name">{{ product.name }}</h4>
            <div class="product-bottom">
              <span class="product-price">
                <span class="price-symbol">¥</span>
                {{ formatPrice(product.price) }}
              </span>
              <span class="product-sales">销量 {{ product.sales || 0 }}</span>
            </div>
          </div>
        </div>
      </div>

      <div v-else class="empty-state">
        <div class="empty-icon">📦</div>
        <p class="empty-text">暂无商品</p>
        <p class="empty-hint">敬请期待更多优质商品上线</p>
      </div>
    </div>

    <!-- 茉莉花收购专栏 -->
    <div class="acquisition-section">
      <div class="section-header">
        <h2 class="page-title">🌸 茉莉花收购专栏</h2>
        <span class="more-link" @click="$router.push('/acquisition')">
          查看更多 →
        </span>
      </div>
      <p class="section-desc">花农直销渠道，减少中间环节，直接增加收入</p>

      <div v-if="acquisitionLoading" class="loading-state small">
        <el-icon class="is-loading"><Loading /></el-icon>
      </div>

      <div v-else-if="acquisitionDemands.length > 0" class="acquisition-grid">
        <div
          v-for="demand in acquisitionDemands"
          :key="demand.id"
          class="acquisition-card neumorphic-card"
          @click="$router.push(`/acquisition/detail/${demand.id}`)"
        >
          <div class="acquisition-header">
            <span class="shop-avatar">{{ getFirstChar(demand.shopName) }}</span>
            <div class="shop-info">
              <span class="shop-name">{{ demand.shopName }}</span>
              <span class="demand-date">{{ formatAcquisitionDate(demand.demandDate) }}</span>
            </div>
          </div>
          <h4 class="acquisition-title">{{ demand.title }}</h4>
          <div class="acquisition-price">
            <span class="price-icon">💵</span>
            <span class="price-text">{{ demand.priceMin }} - {{ demand.priceMax }} 元/{{ demand.unit }}</span>
          </div>
          <div class="acquisition-quantity">
            <span>剩余 {{ demand.quantityRemaining }} {{ demand.unit }}</span>
            <el-progress
              :percentage="getAcquisitionProgress(demand)"
              :stroke-width="4"
              :show-text="false"
              color="#4A7C59"
              style="flex: 1; margin: 0 10px;"
            />
          </div>
          <div class="acquisition-location">
            <span>📍</span>
            <span class="location-text">{{ demand.marketAddress }}</span>
          </div>
        </div>
      </div>

      <div v-else class="empty-state small">
        <div class="empty-icon">🌸</div>
        <p class="empty-text">暂无收购需求</p>
        <p class="empty-hint">敬请期待收购商发布需求</p>
      </div>

      <div class="quick-actions">
        <el-button type="primary" @click="$router.push('/acquisition')">
          查看收购需求
        </el-button>
        <el-button @click="$router.push('/acquisition/my-records')">
          我的收购记录
        </el-button>
      </div>
    </div>

    <!-- 分类导航 -->
    <div class="categories-section">
      <h2 class="page-title">商品分类</h2>
      <div class="category-grid">
        <div
          v-for="category in categories"
          :key="category.id"
          class="neumorphic-card category-card"
          @click="$router.push({ path: '/products', query: { category: category.id } })"
        >
          <span class="category-icon">{{ category.icon }}</span>
          <span class="category-name">{{ category.name }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { Loading } from '@element-plus/icons-vue'
import { getPriceOverview, getPriceTrend, getHotProducts, getTodayPrices } from '@/api'
import { getActiveAcquisitionDemands } from '@/api/acquisition'
import { normalizeUploadUrl } from '@/utils/imageUrl'
import { priceData } from '@/data/prices'

const PREFERRED_MARKET = '横州茉莉花交易市场'

const currentDate = computed(() => {
  const now = new Date()
  return `${now.getMonth() + 1}月${now.getDate()}日 ${now.getHours()}:${String(now.getMinutes()).padStart(2, '0')}`
})

const aiAlertContent = ref('')

const currentPrice = computed(() => {
  const item = priceOverview.value.find(p => Number(p.id) === 1)
  return item ? item.currentPrice : 0
})

const getWeatherData = async () => {
  try {
    const weather = await callWeatherAPI('横州')
    return weather
  } catch (e) {
    console.error('获取天气失败:', e)
    return { weather: '晴', temp: 25 }
  }
}

const callWeatherAPI = async (city) => {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({ weather: '晴转多云', temp: 22 })
    }, 500)
  })
}

const loadAIAlert = async () => {
  try {
    const weatherData = await getWeatherData()
    
    // 等待价格数据加载，最多等待5秒
    let retries = 50
    while (todayPrices.value.length === 0 && retries > 0) {
      await new Promise(resolve => setTimeout(resolve, 100))
      retries--
    }
    
    // 使用当前市场均价，确保与下方卡片显示一致
    const displayAvgPrice = calcAvg();
    
    const recentPrices = trendSeries.value[1]?.slice(-7).map(t => Number(t.price) || 0) || []

    let analysis = ''
    if (recentPrices.length >= 2) {
      const lastPrice = recentPrices[recentPrices.length - 1]
      const firstPrice = recentPrices[0]
      const change = firstPrice > 0 ? ((lastPrice - firstPrice) / firstPrice * 100).toFixed(1) : 0

      if (change > 5) {
        analysis = `📈 价格上涨趋势明显，近7天涨幅达${change}%。当前市场均价¥${displayAvgPrice}/斤，建议适量采购。`
      } else if (change < -5) {
        analysis = `📉 价格呈下跌趋势，近7天下跌${Math.abs(change)}%。当前市场均价¥${displayAvgPrice}/斤，可考虑逢低买入。`
      } else {
        analysis = `📊 价格走势平稳，波动在正常范围内。当前市场均价¥${displayAvgPrice}/斤，建议关注后续走势。`
      }
    } else {
      analysis = `当前茉莉花市场均价为¥${displayAvgPrice}/斤，天气状况：${weatherData.weather}（${weatherData.temp}°C），建议密切关注价格动态。`
    }

    aiAlertContent.value = analysis
  } catch (e) {
    console.error('生成AI预警失败:', e)
    aiAlertContent.value = '价格分析生成中，请稍后刷新页面...'
  }
}

// 图表相关
const trendChartRef = ref(null)
let trendChart = null

const loading = ref(false)
const acquisitionLoading = ref(false)
const acquisitionDemands = ref([])
const hotProducts = ref([])
const priceOverview = ref([])
const staticTrendInit = () => ({
  1: priceData.fresh.trend.map((t) => ({ fullDate: t.date, date: t.date, price: t.price }))
})
const trendSeries = ref(staticTrendInit())

const categories = ref([
  { id: 1, name: '茉莉花茶', icon: '🍵' },
  { id: 2, name: '茉莉花盆栽', icon: '🌱' },
  { id: 3, name: '茉莉花制品', icon: '🎁' },
  { id: 4, name: '茉莉花苗', icon: '🌿' }
])

const banners = ref([
  {
    title: '横州茉莉花 · 花香四溢',
    desc: '正宗横州茉莉花，新鲜采摘，冷链配送',
    btnText: '立即购买',
    link: '/products',
    bg: 'linear-gradient(135deg, #4A7C59 0%, #6B9B7A 100%)'
  },
  {
    title: '茉莉花茶 · 香气浓郁',
    desc: '传统工艺窨制，九次花香，茶香交融',
    btnText: '选购花茶',
    link: '/products?category=1',
    bg: 'linear-gradient(135deg, #D4A574 0%, #E8C9A0 100%)'
  },
  {
    title: '茉莉文创 · 精美礼品',
    desc: '手工定制，独特设计，送礼佳品',
    btnText: '查看详情',
    link: '/products?category=3',
    bg: 'linear-gradient(135deg, #9B8AA6 0%, #B8A8C8 100%)'
  }
])

const loadHotProducts = async () => {
  loading.value = true
  try {
    const res = await getHotProducts(8)
    if (res.code === 200 && res.data) {
      hotProducts.value = res.data
    } else {
      hotProducts.value = []
    }
  } catch (error) {
    console.error('加载商品失败:', error)
    hotProducts.value = []
  } finally {
    loading.value = false
  }
}

const loadAcquisitionDemands = async () => {
  acquisitionLoading.value = true
  try {
    const res = await getActiveAcquisitionDemands()
    if (res.code === 200 && res.data) {
      acquisitionDemands.value = res.data.slice(0, 3)
    } else {
      acquisitionDemands.value = []
    }
  } catch (error) {
    console.error('加载收购需求失败:', error)
    acquisitionDemands.value = []
  } finally {
    acquisitionLoading.value = false
  }
}

const formatAcquisitionDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getMonth() + 1}月${d.getDate()}日`
}

const getFirstChar = (str) => {
  if (!str || str.length === 0) return '收'
  return str.charAt(0)
}

const getAcquisitionProgress = (demand) => {
  if (!demand.quantityNeeded) return 100
  const remaining = Number(demand.quantityRemaining || 0)
  const needed = Number(demand.quantityNeeded || 0)
  if (needed === 0) return 100
  return Math.round(((needed - remaining) / needed) * 100)
}

const fallbackOverview = () => [
  {
    id: 1,
    name: '茉莉鲜花',
    icon: '🌸',
    currentPrice: priceData.fresh.currentPrice,
    unit: priceData.fresh.unit,
    change: priceData.fresh.change,
    avgPrice: priceData.fresh.avgPrice
  }
]

const normalizeOverviewRow = (row) => ({
  ...row,
  id: Number(row.id),
  currentPrice: Number(row.currentPrice ?? 0),
  avgPrice: Number(row.avgPrice ?? row.currentPrice ?? 0),
  change: Number(row.change ?? 0)
})

const todayPrices = ref([])

const calcAvg = () => {
  if (!todayPrices.value.length) return '0.00'
  const sum = todayPrices.value.reduce((s, i) => s + (parseFloat(i.avgPrice || i.minPrice) || 0), 0)
  return (sum / todayPrices.value.length).toFixed(2)
}
const calcMax = () => {
  if (!todayPrices.value.length) return { price: '0.00', market: '' }
  const item = todayPrices.value.reduce((a, b) => parseFloat(a.maxPrice) > parseFloat(b.maxPrice) ? a : b)
  return { price: parseFloat(item.maxPrice).toFixed(2), market: item.market }
}
const calcMin = () => {
  if (!todayPrices.value.length) return { price: '0.00', market: '' }
  const item = todayPrices.value.reduce((a, b) => parseFloat(a.minPrice) < parseFloat(b.minPrice) ? a : b)
  return { price: parseFloat(item.minPrice).toFixed(2), market: item.market }
}

const loadPriceData = async () => {
  try {
    const res = await getPriceOverview()
    if (res.code === 200 && Array.isArray(res.data) && res.data.length > 0) {
      priceOverview.value = res.data.map(normalizeOverviewRow)
    } else {
      priceOverview.value = fallbackOverview()
    }
  } catch (error) {
    console.error('加载价格数据失败:', error)
    priceOverview.value = fallbackOverview()
  }
}

const loadTodayPrices = async () => {
  try {
    const res = await getTodayPrices()
    if (res.code === 200 && res.data) {
      todayPrices.value = res.data
    }
  } catch (e) {
    console.error('加载今日价格失败:', e)
  }
}

const applyStaticTrend = () => {
  trendSeries.value = {
    1: priceData.fresh.trend.map((t) => ({ fullDate: t.date, date: t.date, price: t.price }))
  }
}

const loadTrendSeries = async () => {
  try {
    const cats = [1]
    const results = await Promise.all(
      cats.map((c) => getPriceTrend({ category: c, days: 30 }))
    )
    const next = { 1: [] }
    let anyOk = false
    cats.forEach((c, i) => {
      const res = results[i]
      if (res.code !== 200 || !Array.isArray(res.data) || res.data.length === 0) {
        return
      }
      anyOk = true
      let rows = res.data.filter((r) => r.market === PREFERRED_MARKET)
      if (!rows.length) {
        rows = res.data
      }
      const byDay = new Map()
      for (const r of rows) {
        const raw = r.recordDate
        const day = typeof raw === 'string' ? raw.substring(0, 10) : String(raw)
        const prev = byDay.get(day)
        const val = Number(r.price)
        if (prev == null) {
          byDay.set(day, val)
        } else {
          byDay.set(day, (prev + val) / 2)
        }
      }
      const sorted = [...byDay.keys()].sort()
      next[c] = sorted.map((fd) => ({
        fullDate: fd,
        date: fd.length >= 10 ? fd.slice(5) : fd,
        price: Math.round(byDay.get(fd) * 100) / 100
      }))
    })
    if (anyOk && next[1].length > 0) {
      trendSeries.value = next
    } else {
      applyStaticTrend()
    }
  } catch (e) {
    console.error('加载价格走势失败:', e)
    applyStaticTrend()
  }
}

const getAvgPrice = (categoryId) => {
  const item = priceOverview.value.find((p) => Number(p.id) === Number(categoryId))
  if (item && item.avgPrice != null) {
    return Number(item.avgPrice)
  }
  return 0
}

const num = (v) => Number(v ?? 0)
const priceAlerts = computed(() =>
  priceOverview.value.filter((p) => Math.abs(num(p.change)) >= 3)
)

// 图片相关
const getProductImage = (product) => {
  const images = parseImages(product.images)
  if (images.length > 0 && images[0]) {
    return normalizeUploadUrl(images[0]) || 'https://cube.elemecdn.com/6/94/4d3ea53c084bad6931a56d5158a48jpeg.jpeg'
  }
  return 'https://cube.elemecdn.com/6/94/4d3ea53c084bad6931a56d5158a48jpeg.jpeg'
}

const parseImages = (images) => {
  if (!images) return []
  if (Array.isArray(images)) return images
  try {
    return JSON.parse(images)
  } catch {
    return []
  }
}

const formatPrice = (price) => {
  if (!price && price !== 0) return '0.00'
  return parseFloat(price).toFixed(2)
}

// 图表配色
const chartColors = {
  primary: '#4A7C59',
  primaryLight: '#6b9b7a',
  primaryLighter: '#8dbf9e',
  grid: '#f0f0f0',
  text: '#666666',
  textLight: '#999999'
}

// 初始化价格趋势图表
const initTrendChart = () => {
  if (!trendChartRef.value) return

  trendChart = echarts.init(trendChartRef.value)
  updateTrendChart()
}

const updateTrendChart = () => {
  if (!trendChart) return

  const s1 = trendSeries.value[1] || []
  const s2 = trendSeries.value[2] || []
  const s3 = trendSeries.value[3] || []
  const m1 = new Map(s1.map(p => [p.fullDate, p.price]))
  const m2 = new Map(s2.map(p => [p.fullDate, p.price]))
  const m3 = new Map(s3.map(p => [p.fullDate, p.price]))
  const allDays = [...new Set([...m1.keys(), ...m2.keys(), ...m3.keys()])].sort()

  const mk = (m, hex) => ({
    name: '茉莉鲜花',
    type: 'line',
    data: allDays.map(d => m.get(d) ?? null),
    smooth: 0.5,
    connectNulls: true,
    showSymbol: allDays.length <= 20,
    symbol: 'circle',
    symbolSize: 7,
    lineStyle: {
      width: 3,
      color: hex,
      shadowColor: 'rgba(74, 124, 89, 0.35)',
      shadowBlur: 10,
      shadowOffsetY: 4
    },
    itemStyle: {
      color: hex,
      borderColor: '#fff',
      borderWidth: 2,
      shadowColor: 'rgba(74, 124, 89, 0.3)',
      shadowBlur: 8
    },
    areaStyle: {
      color: {
        type: 'linear',
        x: 0, y: 0, x2: 0, y2: 1,
        colorStops: [
          { offset: 0, color: 'rgba(74, 124, 89, 0.2)' },
          { offset: 1, color: 'rgba(74, 124, 89, 0.02)' }
        ]
      }
    },
    emphasis: {
      scale: true
    }
  })

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
          if (p.value != null) {
            result += `<div style="display: flex; align-items: center; gap: 8px; margin: 4px 0;">
              <span style="display: inline-block; width: 10px; height: 10px; border-radius: 50%; background: ${p.color};"></span>
              <span style="flex: 1;">${p.seriesName}</span>
              <span style="font-weight: 700; color: ${p.color};">¥${p.value}</span>
              <span style="color: #999; font-size: 12px;">元/斤</span>
            </div>`
          }
        })
        return result
      }
    },
    legend: {
      data: ['茉莉鲜花'],
      bottom: 0,
      textStyle: { color: chartColors.text, fontSize: 12 }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: allDays.map(d => d.length >= 10 ? d.slice(5) : d),
      axisLine: { lineStyle: { color: '#e0e0e0' } },
      axisTick: { show: false },
      axisLabel: {
        color: chartColors.text,
        fontSize: 11,
        formatter: (val) => val
      }
    },
    yAxis: {
      type: 'value',
      name: '价格（元/斤）',
      nameTextStyle: { color: chartColors.text, fontSize: 11 },
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: chartColors.text, fontSize: 11 },
      splitLine: { lineStyle: { color: chartColors.grid, type: 'dashed' } },
      min: (val) => Math.floor(val.min * 0.95),
      max: (val) => Math.ceil(val.max * 1.05)
    },
    series: [mk(m1, chartColors.primary)]
  }

  trendChart.setOption(option, true)
}

// 窗口调整
const handleResize = () => {
  if (trendChart) trendChart.resize()
}

onMounted(() => {
  loadHotProducts()
  loadPriceData()
  loadTodayPrices()
  loadTrendSeries()
  loadAIAlert()
  loadAcquisitionDemands()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.home-page {
  padding: 0;
}

.banner-section {
  margin-bottom: 40px;
}

.banner-item {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.banner-content {
  text-align: center;
}

.banner-content h2 {
  font-size: 42px;
  margin-bottom: 16px;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
}

.banner-content p {
  font-size: 18px;
  margin-bottom: 24px;
  opacity: 0.9;
}

.banner-content .btn-primary {
  padding: 14px 36px;
  font-size: 16px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-subtitle {
  font-size: 18px;
  color: #666;
  margin-bottom: 16px;
}

.more-link {
  color: #4A7C59;
  cursor: pointer;
  font-size: 14px;
  transition: color 0.3s;
}

.more-link:hover {
  color: #5a9469;
}

.price-monitor-section {
  margin-bottom: 50px;
}

.price-alerts {
  margin-bottom: 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.price-alert-item {
  border-radius: 12px;
}

.price-monitor-section .page-title {
  position: relative;
  padding-left: 14px;
}

.price-monitor-section .page-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 22px;
  background: #4a7c59;
  border-radius: 2px;
}

.price-alert-card {
  background: linear-gradient(135deg, #fff9e6 0%, #fff 100%);
  border: 1px solid #ffe58f;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.price-alert-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(255, 193, 7, 0.2);
}

.alert-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.alert-icon {
  font-size: 24px;
  display: flex;
  align-items: center;
}

.alert-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  flex: 1;
}

.alert-time {
  font-size: 12px;
  color: #999;
}

.alert-content {
  padding: 12px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 8px;
  margin-bottom: 12px;
}

.alert-text {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin: 0;
}

.alert-loading {
  font-size: 14px;
  color: #999;
  margin: 0;
}

.alert-footer {
  text-align: right;
}

.view-more {
  font-size: 13px;
  color: #4A7C59;
}

/* 今日价格概览 - 与价格详情页一致 */
.price-overview {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-top: 16px;
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
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

@media (max-width: 1024px) {
  .price-overview {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .price-overview {
    grid-template-columns: 1fr;
  }
}


.price-trend-preview {
  margin-top: 30px;
  padding: 24px;
  background: white;
  border-radius: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(0, 0, 0, 0.04);
}

.trend-chart {
  width: 100%;
}

.chart-container {
  height: 300px;
  width: 100%;
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

.empty-state {
  text-align: center;
  padding: 60px 20px;
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

.products-section {
  margin-bottom: 50px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

.product-card {
  cursor: pointer;
  padding: 0;
  overflow: hidden;
  transition: all 0.3s ease;
  border-radius: 20px;
}

.product-card:hover {
  transform: translateY(-5px);
}

.product-image {
  position: relative;
  height: 200px;
  overflow: hidden;
  border-radius: 20px 20px 0 0;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.product-card:hover .product-image img {
  transform: scale(1.1);
}

.product-tag {
  position: absolute;
  top: 12px;
  left: 12px;
  background: rgba(74, 124, 89, 0.9);
  color: white;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
}

.product-info {
  padding: 16px;
}

.product-name {
  font-size: 14px;
  color: #333;
  margin-bottom: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.product-price {
  color: #e74c3c;
  font-size: 18px;
  font-weight: bold;
}

.product-price .price-symbol {
  font-size: 12px;
}

.product-sales {
  color: #999;
  font-size: 12px;
}

.acquisition-section {
  margin-bottom: 50px;
}

.acquisition-section .page-title {
  position: relative;
  padding-left: 14px;
}

.acquisition-section .page-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 22px;
  background: #4a7c59;
  border-radius: 2px;
}

.section-desc {
  font-size: 14px;
  color: #999;
  margin-bottom: 20px;
}

.acquisition-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.acquisition-card {
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s;
  border-radius: 16px;
}

.acquisition-card:hover {
  transform: translateY(-4px);
}

.acquisition-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.shop-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #4A7C59 0%, #6B9B7A 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: bold;
}

.shop-info {
  display: flex;
  flex-direction: column;
}

.shop-name {
  font-size: 13px;
  font-weight: 600;
  color: #333;
}

.demand-date {
  font-size: 11px;
  color: #999;
}

.acquisition-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.acquisition-price {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 10px;
}

.price-icon {
  font-size: 16px;
}

.price-text {
  font-size: 14px;
  font-weight: bold;
  color: #e74c3c;
}

.acquisition-quantity {
  display: flex;
  align-items: center;
  font-size: 12px;
  color: #666;
  margin-bottom: 8px;
}

.acquisition-location {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #999;
}

.location-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.quick-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
}

.quick-actions .el-button {
  padding: 12px 24px;
  border-radius: 20px;
}

.categories-section {
  margin-bottom: 50px;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

.category-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  cursor: pointer;
  transition: transform 0.3s;
  border-radius: 20px;
}

.category-card:hover {
  transform: translateY(-5px);
}

.category-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.category-name {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

@media (max-width: 1024px) {
  .product-grid {
    grid-template-columns: repeat(3, 1fr);
  }
  
  .category-grid {
    grid-template-columns: repeat(3, 1fr);
  }
  
  .acquisition-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .chart-container {
    height: 250px;
  }
}

@media (max-width: 768px) {
  .banner-section {
    margin-bottom: 25px;
  }
  
  .el-carousel {
    height: 280px !important;
  }
  
  .banner-content h2 {
    font-size: 24px;
    padding: 0 15px;
  }

  .banner-content p {
    font-size: 13px;
    padding: 0 20px;
  }

  .banner-content .btn-primary {
    padding: 10px 24px;
    font-size: 14px;
  }
  
  .product-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
  
  .product-image {
    height: 140px;
  }
  
  .product-info {
    padding: 10px;
  }
  
  .product-name {
    font-size: 13px;
    -webkit-line-clamp: 2;
    white-space: normal;
    height: auto;
  }
  
  .product-price {
    font-size: 15px;
  }
  
  .category-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
  
  .category-card {
    padding: 20px;
  }
  
  .category-icon {
    font-size: 36px;
    margin-bottom: 10px;
  }
  
  .category-name {
    font-size: 14px;
  }
  
  .acquisition-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  
  .acquisition-card {
    padding: 14px;
  }
  
  .price-monitor-section {
    margin-bottom: 30px;
  }
  
  .price-trend-preview {
    padding: 15px;
    margin-top: 20px;
  }
  
  .chart-container {
    height: 220px;
  }
  
  .section-header {
    margin-bottom: 16px;
  }
  
  .section-header .page-title {
    font-size: 18px;
  }
  
  .quick-actions {
    flex-direction: column;
    gap: 10px;
  }
  
  .quick-actions .el-button {
    width: 100%;
  }
  
  .products-section,
  .acquisition-section,
  .categories-section {
    margin-bottom: 30px;
  }
  
  .alert-header {
    flex-wrap: wrap;
  }
  
  .alert-content {
    padding: 10px;
  }
}

@media (max-width: 480px) {
  .el-carousel {
    height: 220px !important;
  }
  
  .banner-content h2 {
    font-size: 20px;
  }

  .banner-content p {
    font-size: 12px;
    margin-bottom: 16px;
  }

  .banner-content .btn-primary {
    padding: 8px 20px;
    font-size: 13px;
  }
  
  .product-grid {
    gap: 10px;
  }
  
  .product-image {
    height: 110px;
  }
  
  .product-info {
    padding: 8px;
  }
  
  .product-tag {
    font-size: 10px;
    padding: 2px 8px;
  }
  
  .product-bottom {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
  
  .category-card {
    padding: 15px;
  }
  
  .category-icon {
    font-size: 30px;
  }
  
  .acquisition-header {
    margin-bottom: 8px;
  }
  
  .shop-avatar {
    width: 30px;
    height: 30px;
    font-size: 12px;
  }
  
  .chart-container {
    height: 200px;
  }
}</style>
