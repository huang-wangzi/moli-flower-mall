<template>
  <!-- 天气小组件根容器，点击跳转到天气详情页 -->
  <div class="weather-widget" @click="$router.push('/weather')">
    <!-- 简化显示模式：显示天气图标、温度、城市和日期 -->
    <div class="weather-simple" v-if="!showDetail">
      <span class="weather-icon">{{ weatherIcon }}</span>
      <span class="weather-temp">{{ currentTemp }}°C</span>
      <span class="weather-city">横州</span>
      <span class="weather-date">{{ currentDate }}</span>
    </div>

    <!-- 详细信息模式：点击展开后显示更详细的天气数据 -->
    <div class="weather-detail" v-else @click.stop>
      <!-- 详情头部：标题和关闭按钮 -->
      <div class="detail-header">
        <span class="detail-title">横州天气</span>
        <span class="detail-close" @click="showDetail = false">×</span>
      </div>

      <!-- 当前天气主要信息 -->
      <div class="detail-current">
        <div class="current-main">
          <span class="current-icon">{{ weatherIcon }}</span>
          <span class="current-temp">{{ currentTemp }}°C</span>
        </div>
        <div class="current-info">
          <span class="current-text">{{ currentWeather }}</span>
          <span class="current-wind">{{ windDir }} {{ windSpeed }}km/h</span>
        </div>
      </div>

      <!-- 天气统计数据：湿度、降雨量、气压 -->
      <div class="detail-stats">
        <div class="stat-item">
          <span class="stat-label">湿度</span>
          <span class="stat-value">{{ humidity }}%</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">降雨量</span>
          <span class="stat-value">{{ precipitation }}mm</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">气压</span>
          <span class="stat-value">{{ pressure }}hPa</span>
        </div>
      </div>

      <!-- 空气质量显示区域 -->
      <div class="air-quality" v-if="airData">
        <span class="air-label">空气质量：</span>
        <span class="air-level" :class="'level-' + airLevel">{{ airLevel }}</span>
        <span class="air-aqi">AQI {{ airAqi }}</span>
      </div>

      <!-- 未来天气预报区域 -->
      <div class="forecast" v-if="forecastData.length > 0">
        <div class="forecast-title">未来5天预报</div>
        <div class="forecast-list">
          <!-- 遍历显示每天的预报 -->
          <div v-for="(day, i) in forecastData" :key="i" class="forecast-item">
            <span class="forecast-day">{{ i === 0 ? '今天' : i === 1 ? '明天' : getWeekDay(day.fxDate) }}</span>
            <span class="forecast-icon">{{ getWeatherEmoji(day.textDay) }}</span>
            <span class="forecast-temp">{{ day.tempMin }}° / {{ day.tempMax }}°</span>
          </div>
        </div>
      </div>

      <!-- 底部提示：点击查看更多 -->
      <div class="detail-footer">
        <span class="detail-hint">点击查看更多天气详情</span>
      </div>
    </div>
  </div>
</template>

<script setup>
// ==================== Vue3 组合式API引入 ====================
import { ref, computed, onMounted } from 'vue'

// ==================== API接口引入 ====================
// 引入天气相关的API接口函数
import { getNowWeather, getWeatherForecast, getAirQuality } from '@/api'

// ==================== 响应式数据定义 ====================
// showDetail: 控制详细天气信息的显示/隐藏
const showDetail = ref(false)
// weatherData: 存储当前天气数据
const weatherData = ref(null)
// forecastData: 存储未来天气预报数据
const forecastData = ref([])
// airData: 存储空气质量数据
const airData = ref(null)
// loading: 防止重复请求的加载状态标志
const loading = ref(false)

// ==================== 计算属性：当前日期显示 ====================
// computed: 用于计算基于响应式数据的派生值
const currentDate = computed(() => {
  const now = new Date()
  const month = now.getMonth() + 1
  const day = now.getDate()
  const weekDay = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'][now.getDay()]
  return `${month}月${day}日 ${weekDay}`
})

// ==================== 计算属性：当前温度 ====================
const currentTemp = computed(() => {
  return weatherData.value?.temp || '--'
})

// ==================== 计算属性：当前天气状况文字 ====================
const currentWeather = computed(() => {
  return weatherData.value?.text || '--'
})

// ==================== 计算属性：湿度 ====================
const humidity = computed(() => {
  return weatherData.value?.humidity || '--'
})

// ==================== 计算属性：降雨量 ====================
const precipitation = computed(() => {
  return weatherData.value?.precip || '0'
})

// ==================== 计算属性：气压 ====================
const pressure = computed(() => {
  return weatherData.value?.pressure || '--'
})

// ==================== 计算属性：风向 ====================
const windDir = computed(() => {
  return weatherData.value?.windDir || '--'
})

// ==================== 计算属性：风速 ====================
const windSpeed = computed(() => {
  return weatherData.value?.windSpeed || '--'
})

// ==================== 计算属性：天气图标 ====================
// 根据天气文字获取对应的emoji图标
const weatherIcon = computed(() => {
  return getWeatherEmoji(currentWeather.value)
})

// ==================== 计算属性：空气质量AQI ====================
const airAqi = computed(() => {
  return airData.value?.aqi || '--'
})

// ==================== 计算属性：空气质量等级 ====================
const airLevel = computed(() => {
  const level = airData.value?.category || '未知'
  return level
})

/**
 * 加载天气数据
 * 优先调用后端接口获取天气数据（后端调用和风API）
 * 避免前端直接调用被限流
 */
const loadWeather = async () => {
  // 如果正在加载，直接返回，防止重复请求
  if (loading.value) return
  loading.value = true

  try {
    // 调用后端天气接口获取实时天气
    const res = await getNowWeather()
    if (res.code === 200 && res.data) {
      weatherData.value = res.data
    } else {
      // 后端返回错误，设置为空对象避免页面崩溃
      weatherData.value = {}
    }

    // 获取未来天气预报
    const forecastRes = await getWeatherForecast()
    if (forecastRes.code === 200 && forecastRes.data) {
      // 兼容不同的返回数据格式
      const daily = forecastRes.data.daily || forecastRes.data
      // 取前5天的预报数据
      forecastData.value = (Array.isArray(daily) ? daily : []).slice(0, 5)
    } else {
      forecastData.value = []
    }

    // 获取空气质量数据
    const airRes = await getAirQuality()
    if (airRes.code === 200 && airRes.data) {
      airData.value = airRes.data
    }
  } catch (error) {
    console.error('加载天气数据失败:', error)
    // 确保数据不会为null，避免页面报错
    if (!weatherData.value) weatherData.value = {}
    if (!forecastData.value) forecastData.value = []
  } finally {
    // 无论成功失败，都要重置加载状态
    loading.value = false
  }
}

/**
 * 获取星期几
 * 根据日期字符串返回对应的星期
 * @param dateStr 日期字符串，格式：YYYY-MM-DD
 * @returns 星期几（如"周一"）
 */
const getWeekDay = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const days = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return days[date.getDay()]
}

/**
 * 获取天气Emoji图标
 * 根据天气文字描述返回对应的emoji表情
 * @param weather 天气描述文字
 * @returns 对应的emoji字符串
 */
const getWeatherEmoji = (weather) => {
  // 如果没有天气数据，返回默认图标
  if (!weather) return '🌤️'
  const w = weather.toLowerCase()
  // 根据天气关键词匹配对应的emoji
  if (w.includes('晴')) return '☀️'       // 晴天
  if (w.includes('多云')) return '⛅'      // 多云
  if (w.includes('阴')) return '☁️'        // 阴天
  if (w.includes('雨')) return '🌧️'      // 雨天
  if (w.includes('雪')) return '❄️'       // 雪天
  if (w.includes('雷')) return '⛈️'      // 雷暴
  if (w.includes('雾') || w.includes('霾')) return '🌫️'  // 雾霾
  if (w.includes('风')) return '💨'       // 大风
  return '🌤️'  // 默认天气图标
}

/**
 * 组件挂载时执行的初始化操作
 */
onMounted(() => {
  // 首次加载天气数据
  loadWeather()
  // 设置定时刷新：每10分钟自动刷新一次天气数据
  setInterval(loadWeather, 10 * 60 * 1000)
})
</script>

<style scoped>
.weather-widget {
  cursor: pointer;
  user-select: none;
}

.weather-simple {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 20px;
  transition: all 0.3s;
}

.weather-simple:hover {
  background: rgba(255, 255, 255, 1);
  transform: scale(1.02);
}

.weather-icon {
  font-size: 18px;
}

.weather-temp {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.weather-city {
  font-size: 12px;
  color: #666;
}

.weather-detail {
  position: absolute;
  top: 60px;
  right: 20px;
  width: 320px;
  background: linear-gradient(145deg, #ffffff, #f0f4f8);
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
  z-index: 2000;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.detail-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.detail-close {
  font-size: 24px;
  color: #999;
  cursor: pointer;
  line-height: 1;
}

.detail-close:hover {
  color: #333;
}

.detail-current {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.current-main {
  display: flex;
  align-items: center;
  gap: 8px;
}

.current-icon {
  font-size: 36px;
}

.current-temp {
  font-size: 32px;
  font-weight: bold;
  color: #333;
}

.current-info {
  text-align: right;
}

.current-text {
  display: block;
  font-size: 14px;
  color: #666;
}

.current-wind {
  display: block;
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.detail-stats {
  display: flex;
  justify-content: space-around;
  padding: 12px 0;
  border-top: 1px solid #eee;
  border-bottom: 1px solid #eee;
  margin-bottom: 12px;
}

.stat-item {
  text-align: center;
}

.stat-label {
  display: block;
  font-size: 12px;
  color: #999;
}

.stat-value {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-top: 4px;
}

.air-quality {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #f5f5f5;
  border-radius: 8px;
  margin-bottom: 12px;
}

.air-label {
  font-size: 12px;
  color: #666;
}

.air-level {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
}

.level-优 {
  background: #e8f5e9;
  color: #2e7d32;
}

.level-良 {
  background: #fff3e0;
  color: #ef6c00;
}

.level-中 {
  background: #fff8e1;
  color: #f9a825;
}

.level-差 {
  background: #ffebee;
  color: #c62828;
}

.level-未知 {
  background: #f5f5f5;
  color: #999;
}

.air-aqi {
  font-size: 12px;
  color: #666;
}

.planting-advice {
  padding: 12px;
  background: linear-gradient(135deg, #e8f5e9, #c8e6c9);
  border-radius: 12px;
  margin-bottom: 12px;
}

.advice-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.advice-icon {
  font-size: 20px;
}

.advice-title {
  font-size: 14px;
  font-weight: 600;
  color: #2e7d32;
}

.advice-score {
  margin-left: auto;
  padding: 2px 10px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 600;
}

.score-优 {
  background: #2e7d32;
  color: white;
}

.score-良 {
  background: #558b2f;
  color: white;
}

.score-中 {
  background: #f9a825;
  color: white;
}

.score-差 {
  background: #c62828;
  color: white;
}

.score--- {
  background: #999;
  color: white;
}

.advice-analysis {
  font-size: 13px;
  color: #333;
  line-height: 1.5;
  margin: 0;
}

.advice-suggestions {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px dashed rgba(0, 0, 0, 0.1);
}

.advice-suggestions p {
  font-size: 12px;
  color: #555;
  margin: 4px 0;
}

.forecast {
  margin-bottom: 12px;
}

.forecast-title {
  font-size: 13px;
  color: #666;
  margin-bottom: 8px;
}

.forecast-list {
  display: flex;
  justify-content: space-between;
}

.forecast-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.forecast-day {
  font-size: 11px;
  color: #999;
}

.forecast-icon {
  font-size: 20px;
}

.forecast-temp {
  font-size: 11px;
  color: #666;
}

.update-time {
  text-align: center;
  font-size: 11px;
  color: #999;
}
</style>
