/**
 * 天气API - 前端调用后端天气接口
 * 
 * 后端接口地址（Controller 路径）：
 *   /api/weather/now → 实时天气
 *   /api/weather/forecast 或 /api/weather/3d → 3天预报
 *   /api/weather/air → 空气质量
 *   /api/weather/comprehensive → 综合天气（含种植建议）
 * 
 * 注意：client.js 的 baseURL 已配置为 http://localhost:8080/api
 * 所有请求路径不需要手动添加 /api 前缀
 */
import { api } from './client.js'

/**
 * 获取实时天气
 * 后端接口: GET /api/weather/now
 */
export function getNowWeather() {
  return api.get('/weather/now').then(res => {
    if (res.code === 200) {
      return {
        code: 200,
        data: res.data
      }
    }
    return { code: 500, message: res.message || '获取天气失败' }
  }).catch(err => {
    console.error('获取天气失败:', err)
    return { code: 500, message: err.message }
  })
}

/**
 * 获取天气预报（3天）
 * 后端接口: GET /api/weather/forecast
 */
export function getWeatherForecast() {
  return api.get('/weather/forecast').then(res => {
    if (res.code === 200) {
      return {
        code: 200,
        data: res.data
      }
    }
    return { code: 500, message: res.message || '获取预报失败' }
  }).catch(err => {
    console.error('获取预报失败:', err)
    return { code: 500, message: err.message }
  })
}

/**
 * 获取空气质量
 * 后端接口: GET /api/weather/air
 */
export function getAirQuality() {
  return api.get('/weather/air').then(res => {
    if (res.code === 200) {
      return {
        code: 200,
        data: res.data
      }
    }
    return { code: 500, message: res.message || '获取空气质量失败' }
  }).catch(err => {
    console.error('获取空气质量失败:', err)
    return { code: 500, message: err.message }
  })
}

/**
 * 获取综合天气信息
 * 后端接口: GET /api/weather/comprehensive
 */
export function getComprehensiveWeather() {
  return api.get('/weather/comprehensive').then(res => {
    if (res.code === 200) {
      return {
        code: 200,
        data: res.data
      }
    }
    return { code: 500, message: res.message || '获取天气失败' }
  }).catch(err => {
    console.error('获取综合天气失败:', err)
    return { code: 500, message: err.message }
  })
}