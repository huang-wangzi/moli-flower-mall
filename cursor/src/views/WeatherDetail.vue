<template>
  <div class="weather-page">
    <!-- 顶部天气概览 -->
    <div class="weather-hero" v-if="weatherData.now">
      <div class="hero-left">
        <div class="location">
          <span class="location-icon">📍</span>
          <span class="location-name">{{ locationName }}</span>
        </div>
        <div class="current-temp">
          <span class="temp-value">{{ weatherData.now.temp || '--' }}</span>
          <span class="temp-unit">°C</span>
        </div>
        <div class="current-text">{{ weatherData.now.text || '未知' }}</div>
        <div class="current-feels">
          体感温度 {{ weatherData.now.feelsLike || '--' }}°C
        </div>
      </div>
      <div class="hero-right">
        <span class="weather-icon-large">{{ getWeatherEmoji(weatherData.now.text) }}</span>
      </div>
    </div>

    <!-- 实时天气详情 -->
    <div class="weather-cards">
      <div class="weather-card">
        <div class="card-header">
          <span class="card-icon">🌡️</span>
          <span class="card-title">温度</span>
        </div>
        <div class="card-body">
          <div class="detail-row">
            <span class="label">当前温度</span>
            <span class="value">{{ weatherData.now.temp || '--' }}°C</span>
          </div>
          <div class="detail-row">
            <span class="label">体感温度</span>
            <span class="value">{{ weatherData.now.feelsLike || '--' }}°C</span>
          </div>
          <div class="detail-row" v-if="weatherData.now.dew || weatherData.now.dew === 0">
            <span class="label">露点温度</span>
            <span class="value">{{ weatherData.now.dew }}°C</span>
          </div>
        </div>
      </div>

      <div class="weather-card">
        <div class="card-header">
          <span class="card-icon">💧</span>
          <span class="card-title">湿度</span>
        </div>
        <div class="card-body">
          <div class="detail-row">
            <span class="label">相对湿度</span>
            <span class="value">{{ weatherData.now.humidity || '--' }}%</span>
          </div>
          <div class="detail-row" v-if="weatherData.now.precip || weatherData.now.precip === 0">
            <span class="label">小时降水量</span>
            <span class="value">{{ weatherData.now.precip }}mm</span>
          </div>
        </div>
      </div>

      <div class="weather-card">
        <div class="card-header">
          <span class="card-icon">💨</span>
          <span class="card-title">风力</span>
        </div>
        <div class="card-body">
          <div class="detail-row">
            <span class="label">风向</span>
            <span class="value">{{ weatherData.now.windDir || '--' }}</span>
          </div>
          <div class="detail-row">
            <span class="label">风速</span>
            <span class="value">{{ weatherData.now.windSpeed || '--' }} km/h</span>
          </div>
          <div class="detail-row" v-if="weatherData.now.windGust">
            <span class="label">阵风</span>
            <span class="value">{{ weatherData.now.windGust }} km/h</span>
          </div>
        </div>
      </div>

      <div class="weather-card">
        <div class="card-header">
          <span class="card-icon">🌅</span>
          <span class="card-title">其他</span>
        </div>
        <div class="card-body">
          <div class="detail-row" v-if="weatherData.now.pressure">
            <span class="label">气压</span>
            <span class="value">{{ weatherData.now.pressure }} hPa</span>
          </div>
          <div class="detail-row" v-if="weatherData.now.vis">
            <span class="label">能见度</span>
            <span class="value">{{ weatherData.now.vis }} km</span>
          </div>
          <div class="detail-row" v-if="weatherData.now.cloud">
            <span class="label">云量</span>
            <span class="value">{{ weatherData.now.cloud }}%</span>
          </div>
          <div class="detail-row" v-if="weatherData.now.uvIndex">
            <span class="label">紫外线指数</span>
            <span class="value">{{ getUVLevel(weatherData.now.uvIndex) }} ({{ weatherData.now.uvIndex }})</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 逐小时预报 -->
    <div class="section" v-if="hourlyData.length > 0">
      <h2 class="section-title">逐小时预报</h2>
      <div class="hourly-scroll">
        <div 
          v-for="(hour, index) in hourlyData" 
          :key="index"
          class="hourly-item"
        >
          <div class="hour-time">{{ formatHour(hour.fxTime) }}</div>
          <div class="hour-icon">{{ getWeatherEmoji(hour.text) }}</div>
          <div class="hour-temp">{{ hour.temp }}°</div>
          <div class="hour-precip" v-if="hour.precip > 0">{{ hour.precip }}mm</div>
        </div>
      </div>
    </div>

    <!-- 每日预报 -->
    <div class="section" v-if="weatherData.forecast && weatherData.forecast.length > 0">
      <h2 class="section-title">天气预报</h2>
      <div class="forecast-list">
        <div 
          v-for="(day, index) in weatherData.forecast" 
          :key="index"
          class="forecast-item"
        >
          <div class="forecast-date">
            <span class="date-day">{{ getDayName(index, day.fxDate) }}</span>
            <span class="date-text">{{ day.fxDate }}</span>
          </div>
          <div class="forecast-sunrise" v-if="day.sunrise">
            <span>🌅 {{ day.sunrise }}</span>
            <span class="sunset">🌇 {{ day.sunset }}</span>
          </div>
          <div class="forecast-weather">
            <span class="weather-icon">{{ getWeatherEmoji(day.textDay) }}</span>
            <span class="weather-text">{{ day.textDay }}</span>
          </div>
          <div class="forecast-temp">
            <span class="temp-high">{{ day.tempMax }}°</span>
            <div class="temp-bar">
              <div class="temp-fill" :style="{ width: getTempPercent(day.tempMin, day.tempMax) + '%' }"></div>
            </div>
            <span class="temp-low">{{ day.tempMin }}°</span>
          </div>
          <div class="forecast-detail">
            <span v-if="day.humidity">💧 {{ day.humidity }}%</span>
            <span v-if="day.windDirDay">{{ getWindDir(day.windDirDay) }} {{ day.windSpeedDay }}km/h</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 空气质量 -->
    <div class="section" v-if="airData">
      <h2 class="section-title">空气质量</h2>
      <div class="air-card">
        <div class="air-main">
          <div class="air-aqi" :class="'aqi-' + getAqiLevel(airData.aqi)">
            {{ airData.aqi || '--' }}
          </div>
          <div class="air-level">{{ airData.category || '未知' }}</div>
        </div>
        <div class="air-details">
          <div class="air-item">
            <span class="air-label">PM2.5</span>
            <span class="air-value">{{ airData.pm2p5 || '--' }} μg/m³</span>
          </div>
          <div class="air-item">
            <span class="air-label">PM10</span>
            <span class="air-value">{{ airData.pm10 || '--' }} μg/m³</span>
          </div>
          <div class="air-item">
            <span class="air-label">SO₂</span>
            <span class="air-value">{{ airData.so2 || '--' }} μg/m³</span>
          </div>
          <div class="air-item">
            <span class="air-label">NO₂</span>
            <span class="air-value">{{ airData.no2 || '--' }} μg/m³</span>
          </div>
          <div class="air-item">
            <span class="air-label">CO</span>
            <span class="air-value">{{ airData.co || '--' }} mg/m³</span>
          </div>
          <div class="air-item">
            <span class="air-label">O₃</span>
            <span class="air-value">{{ airData.o3 || '--' }} μg/m³</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 天气预警 -->
    <div class="section" v-if="warningData && warningData.length > 0">
      <h2 class="section-title">天气预警</h2>
      <div class="warning-list">
        <div 
          v-for="(warn, index) in warningData" 
          :key="index"
          class="warning-item"
          :class="'warning-' + getWarningLevel(warn.level)"
        >
          <div class="warning-header">
            <span class="warning-type">{{ warn.typeName || '天气预警' }}</span>
            <span class="warning-level">{{ warn.level || '未知' }}级</span>
          </div>
          <div class="warning-text">{{ warn.text }}</div>
          <div class="warning-time">{{ warn.publishTime }}</div>
        </div>
      </div>
    </div>

    <!-- 茉莉花种植影响 -->
    <div class="section" v-if="weatherData.impact">
      <h2 class="section-title">🌱 茉莉花种植影响分析</h2>
      <div class="impact-card">
        <div class="impact-header">
          <div class="impact-score" :class="'score-' + weatherData.impact.level">
            <span class="score-value">{{ weatherData.impact.score || '--' }}</span>
            <span class="score-label">分</span>
          </div>
          <div class="impact-level" :class="'level-' + weatherData.impact.level">
            {{ weatherData.impact.level || '未知' }}
          </div>
        </div>
        <div class="impact-analysis">{{ weatherData.impact.analysis }}</div>
      </div>
    </div>

    <!-- 价格走势预测（新增） -->
    <div class="section" v-if="weatherData.impact && weatherData.impact.pricePrediction">
      <h2 class="section-title">💰 价格走势预测</h2>
      <div class="price-card">
        <div class="price-header">
          <div class="price-trend" :class="getPriceTrendClass(weatherData.impact.priceScore)">
            <span class="trend-icon">{{ getPriceTrendIcon(weatherData.impact.priceScore) }}</span>
            <span class="trend-text">{{ weatherData.impact.priceTrend || '分析中...' }}</span>
          </div>
        </div>
        <div class="price-prediction">{{ weatherData.impact.pricePrediction }}</div>
        <div class="price-factor">
          <div class="factor-label">综合影响指数</div>
          <div class="factor-bar">
            <div class="factor-fill" 
              :style="{ width: (weatherData.impact.priceScore || 50) + '%', background: getPriceBarColor(weatherData.impact.priceScore) }">
            </div>
          </div>
          <div class="factor-value">{{ weatherData.impact.priceScore || 50 }}/100</div>
        </div>
      </div>
    </div>

    <!-- 更新信息 -->
    <div class="update-info">
      数据更新时间：{{ weatherData.updateTime || new Date().toLocaleString('zh-CN') }}
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getComprehensiveWeather } from '@/api'

const locationName = '广西横州'
const weatherData = ref({
  now: { temp: '--', text: '加载中...', feelsLike: '--', windDir: '--', windSpeed: '--', humidity: '--', pressure: '--', precip: '--' },
  forecast: [],
  impact: null,
  updateTime: ''
})
const hourlyData = ref([])
const airData = ref(null)
const warningData = ref([])

const loading = ref(false)

// 加载天气数据 - 统一调用后端综合接口
const loadWeather = async () => {
  loading.value = true
  try {
    const res = await getComprehensiveWeather()
    if (res.code === 200 && res.data) {
      // 从后端综合接口获取所有数据
      const data = res.data
      
      // 实时天气 - 确保 now 存在
      const nowWeather = data.now || data
      
      weatherData.value = {
        now: nowWeather || { temp: '--', text: '暂无数据', feelsLike: '--' },
        forecast: data.forecast?.daily || data.forecast || [],
        impact: data.impact,
        updateTime: nowWeather?.updateTime || data.updateTime || ''
      }
      
      // 空气质量（从综合接口获取）
      airData.value = data.air || null
      
      // 逐小时数据（如果有的话，否则使用空数组）
      hourlyData.value = data.hourly || []
      
      // 预警数据（如果有的话）
      warningData.value = data.warning || []
    } else {
      // 后端返回错误，设置为默认值
      weatherData.value = {
        now: { temp: '--', text: '数据加载中...', feelsLike: '--' },
        forecast: [],
        impact: null,
        updateTime: ''
      }
      console.warn('天气数据加载失败:', res.message)
    }
  } catch (error) {
    console.error('加载天气数据失败:', error)
    // 发生错误时设置为默认值，避免页面崩溃
    weatherData.value = {
      now: { temp: '--', text: '数据加载失败', feelsLike: '--' },
      forecast: [],
      impact: null,
      updateTime: ''
    }
  } finally {
    loading.value = false
  }
}

// 天气图标
const getWeatherEmoji = (weather) => {
  if (!weather) return '🌤️'
  const w = weather.toLowerCase()
  if (w.includes('晴')) return '☀️'
  if (w.includes('多云') && !w.includes('阴')) return '⛅'
  if (w.includes('阴') || w.includes('蔽')) return '☁️'
  if (w.includes('小雨')) return '🌧️'
  if (w.includes('中雨')) return '🌧️'
  if (w.includes('大雨') || w.includes('暴雨')) return '⛈️'
  if (w.includes('雷')) return '⛈️'
  if (w.includes('雪')) return '❄️'
  if (w.includes('雾') || w.includes('霾')) return '🌫️'
  if (w.includes('沙') || w.includes('尘')) return '🌪️'
  return '🌤️'
}

// 格式化小时
const formatHour = (time) => {
  if (!time) return '--'
  const hour = new Date(time).getHours()
  return hour + ':00'
}

// 获取星期几
const getDayName = (index, date) => {
  if (index === 0) return '今天'
  if (index === 1) return '明天'
  if (!date) return ''
  const days = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return days[new Date(date).getDay()] || ''
}

// 温度百分比（用于显示温度条）
const getTempPercent = (min, max) => {
  const minVal = parseInt(min) || 0
  const maxVal = parseInt(max) || 40
  const range = maxVal - minVal
  if (range <= 0) return 50
  return ((parseInt(max) - minVal) / range) * 100
}

// 获取风向简写
const getWindDir = (dir) => {
  const dirMap = {
    '北': 'N', '东北': 'NE', '东': 'E', '东南': 'SE',
    '南': 'S', '西南': 'SW', '西': 'W', '西北': 'NW'
  }
  return dirMap[dir] || dir || ''
}

// AQI等级
const getAqiLevel = (aqi) => {
  if (!aqi) return 'unknown'
  if (aqi <= 50) return 'good'
  if (aqi <= 100) return 'moderate'
  if (aqi <= 150) return 'unhealthy-sens'
  if (aqi <= 200) return 'unhealthy'
  if (aqi <= 300) return 'very-unhealthy'
  return 'hazardous'
}

// 紫外线等级
const getUVLevel = (uv) => {
  if (!uv) return '未知'
  if (uv <= 2) return '最弱'
  if (uv <= 4) return '弱'
  if (uv <= 6) return '中等'
  if (uv <= 8) return '强'
  if (uv <= 10) return '很强'
  return '极强'
}

// 预警等级
const getWarningLevel = (level) => {
  if (!level) return 'unknown'
  const l = level.toLowerCase()
  if (l.includes('蓝')) return 'blue'
  if (l.includes('黄')) return 'yellow'
  if (l.includes('橙')) return 'orange'
  if (l.includes('红')) return 'red'
  return 'unknown'
}

// 价格走势相关函数
const getPriceTrendClass = (score) => {
  if (!score) return 'trend-normal'
  if (score >= 75) return 'trend-rise-high'
  if (score >= 60) return 'trend-rise'
  if (score >= 40) return 'trend-normal'
  if (score >= 25) return 'trend-fall'
  return 'trend-fall-high'
}

const getPriceTrendIcon = (score) => {
  if (!score) return '❓'
  if (score >= 75) return '📈'
  if (score >= 60) return '↗️'
  if (score >= 40) return '➡️'
  if (score >= 25) return '↘️'
  return '📉'
}

const getPriceBarColor = (score) => {
  if (!score) return '#999'
  if (score >= 75) return '#e74c3c'
  if (score >= 60) return '#f39c12'
  if (score >= 40) return '#27ae60'
  if (score >= 25) return '#3498db'
  return '#9b59b6'
}

// 生活指数图标
const getIndexIcon = (type) => {
  const iconMap = {
    '1': '🚗', // 洗车
    '2': '👕', // 穿衣
    '3': '💊', // 感冒
    '4': '🏃', // 运动
    '5': '🌂', // 雨伞
    '6': '☀️', // 紫外线
    '7': '😷', // 空气污染
    '8': '🎨', // 化妆
    '9': '❄️', // 冰冻
    '10': '⚡', // 闪电
  }
  return iconMap[type] || '📋'
}

// 生活指数名称
const getIndexName = (type) => {
  const nameMap = {
    '1': '洗车指数',
    '2': '穿衣指数',
    '3': '感冒指数',
    '4': '运动指数',
    '5': '雨伞指数',
    '6': '紫外线指数',
    '7': '空气污染扩散条件',
    '8': '化妆指数',
    '9': '冰冻指数',
    '10': '雷暴指数',
  }
  return nameMap[type] || '未知指数'
}

onMounted(() => {
  loadWeather()
})
</script>

<style scoped>
.weather-page {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

/* 顶部天气概览 */
.weather-hero {
  background: linear-gradient(135deg, #4A7C59 0%, #5BA06B 100%);
  border-radius: 20px;
  padding: 30px;
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.hero-left {
  flex: 1;
}

.location {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 16px;
  opacity: 0.9;
  margin-bottom: 10px;
}

.current-temp {
  display: flex;
  align-items: flex-start;
}

.temp-value {
  font-size: 72px;
  font-weight: 300;
  line-height: 1;
}

.temp-unit {
  font-size: 24px;
  margin-top: 10px;
}

.current-text {
  font-size: 20px;
  margin-top: 8px;
  opacity: 0.9;
}

.current-feels {
  font-size: 14px;
  opacity: 0.8;
  margin-top: 4px;
}

.hero-right {
  font-size: 100px;
}

.weather-icon-large {
  filter: drop-shadow(0 4px 8px rgba(0,0,0,0.2));
}

/* 天气卡片网格 */
.weather-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.weather-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #eee;
}

.card-icon {
  font-size: 24px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.card-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.detail-row .label {
  font-size: 14px;
  color: #666;
}

.detail-row .value {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

/* Section */
.section {
  margin-bottom: 30px;
}

.section-title {
  font-size: 18px;
  color: #333;
  margin-bottom: 16px;
  padding-left: 12px;
  border-left: 4px solid #4A7C59;
}

/* 逐小时预报 */
.hourly-scroll {
  display: flex;
  gap: 12px;
  overflow-x: auto;
  padding: 10px 0;
  -webkit-overflow-scrolling: touch;
}

.hourly-item {
  flex-shrink: 0;
  width: 70px;
  text-align: center;
  padding: 16px 8px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.hour-time {
  font-size: 12px;
  color: #666;
  margin-bottom: 8px;
}

.hour-icon {
  font-size: 28px;
  margin-bottom: 8px;
}

.hour-temp {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.hour-precip {
  font-size: 11px;
  color: #4A7C59;
  margin-top: 4px;
}

/* 每日预报 */
.forecast-list {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
}

.forecast-item {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
  gap: 16px;
}

.forecast-item:last-child {
  border-bottom: none;
}

.forecast-date {
  width: 80px;
  flex-shrink: 0;
}

.date-day {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.date-text {
  display: block;
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}

.forecast-sunrise {
  width: 100px;
  font-size: 11px;
  color: #666;
  flex-shrink: 0;
}

.sunset {
  margin-left: 12px;
}

.forecast-weather {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100px;
  flex-shrink: 0;
}

.forecast-weather .weather-icon {
  font-size: 28px;
}

.forecast-weather .weather-text {
  font-size: 14px;
  color: #666;
}

.forecast-temp {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
}

.temp-high {
  font-size: 14px;
  font-weight: 600;
  color: #e74c3c;
  width: 36px;
}

.temp-low {
  font-size: 14px;
  color: #27ae60;
  width: 36px;
}

.temp-bar {
  flex: 1;
  height: 4px;
  background: linear-gradient(to right, #27ae60, #e74c3c);
  border-radius: 2px;
}

.temp-fill {
  height: 100%;
  background: transparent;
}

.forecast-detail {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 12px;
  color: #666;
  min-width: 140px;
}

/* 空气质量 */
.air-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
  display: flex;
  gap: 30px;
  align-items: center;
}

.air-main {
  text-align: center;
}

.air-aqi {
  font-size: 56px;
  font-weight: bold;
}

.aqi-good { color: #4CAF50; }
.aqi-moderate { color: #FFEB3B; }
.aqi-unhealthy-sens { color: #FF9800; }
.aqi-unhealthy { color: #f44336; }
.aqi-very-unhealthy { color: #9C27B0; }
.aqi-hazardous { color: #800000; }
.aqi-unknown { color: #999; }

.air-level {
  font-size: 16px;
  color: #666;
  margin-top: 4px;
}

.air-details {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.air-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.air-label {
  font-size: 12px;
  color: #999;
}

.air-value {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

/* 天气预警 */
.warning-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.warning-item {
  background: white;
  border-radius: 12px;
  padding: 16px;
  border-left: 4px solid;
}

.warning-blue { border-color: #2196F3; }
.warning-yellow { border-color: #FFC107; }
.warning-orange { border-color: #FF9800; }
.warning-red { border-color: #f44336; }
.warning-unknown { border-color: #999; }

.warning-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.warning-type {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.warning-level {
  font-size: 14px;
  font-weight: 600;
  padding: 2px 10px;
  border-radius: 4px;
  background: #f0f0f0;
}

.warning-text {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
}

.warning-time {
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}

/* 茉莉花种植影响 */
.impact-card {
  background: linear-gradient(135deg, #e8f5e9, #c8e6c9);
  border-radius: 16px;
  padding: 24px;
}

.impact-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.impact-score {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.score-value {
  font-size: 48px;
  font-weight: bold;
  color: #2e7d32;
}

.score-label {
  font-size: 18px;
  color: #2e7d32;
}

.impact-level {
  font-size: 20px;
  font-weight: 600;
  padding: 6px 20px;
  border-radius: 20px;
}

.level-优 { background: #2e7d32; color: white; }
.level-良 { background: #558b2f; color: white; }
.level-中 { background: #f9a825; color: white; }
.level-差 { background: #c62828; color: white; }

.impact-analysis {
  font-size: 15px;
  color: #333;
  line-height: 1.8;
}

/* 生活指数 */
.index-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 12px;
}

.index-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.index-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.index-name {
  font-size: 14px;
  color: #666;
  margin-bottom: 4px;
}

.index-level {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

/* 更新信息 */
.update-info {
  text-align: center;
  font-size: 12px;
  color: #999;
  padding: 20px;
}

/* 价格走势预测 */
.price-card {
  background: linear-gradient(135deg, #fff8e1, #ffecb3);
  border-radius: 16px;
  padding: 24px;
  border: 1px solid #ffe082;
}

.price-header {
  margin-bottom: 16px;
}

.price-trend {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border-radius: 20px;
  font-weight: 600;
}

.trend-icon {
  font-size: 24px;
}

.trend-text {
  font-size: 18px;
}

.price-trend.trend-rise-high {
  background: #ffebee;
  color: #c62828;
}

.price-trend.trend-rise {
  background: #fff3e0;
  color: #e65100;
}

.price-trend.trend-normal {
  background: #e8f5e9;
  color: #2e7d32;
}

.price-trend.trend-fall {
  background: #e3f2fd;
  color: #1565c0;
}

.price-trend.trend-fall-high {
  background: #f3e5f5;
  color: #7b1fa2;
}

.price-prediction {
  font-size: 15px;
  color: #333;
  line-height: 1.8;
  margin-bottom: 16px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 8px;
}

.price-factor {
  display: flex;
  align-items: center;
  gap: 12px;
}

.factor-label {
  font-size: 14px;
  color: #666;
  white-space: nowrap;
}

.factor-bar {
  flex: 1;
  height: 8px;
  background: #e0e0e0;
  border-radius: 4px;
  overflow: hidden;
}

.factor-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.3s ease;
}

.factor-value {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  min-width: 50px;
  text-align: right;
}

/* 响应式 */
@media (max-width: 768px) {
  .weather-hero {
    padding: 20px;
  }
  
  .temp-value {
    font-size: 56px;
  }
  
  .hero-right {
    font-size: 72px;
  }
  
  .weather-cards {
    grid-template-columns: 1fr 1fr;
  }
  
  .air-card {
    flex-direction: column;
    text-align: center;
  }
  
  .air-details {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .forecast-item {
    flex-wrap: wrap;
  }
  
  .forecast-sunrise,
  .forecast-detail {
    width: 100%;
    flex-direction: row;
    justify-content: space-around;
  }
}
</style>
