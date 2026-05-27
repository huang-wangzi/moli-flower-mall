import axios from 'axios'
import { ElMessage } from 'element-plus'

/**
 * 独立 axios 实例，供所有 API 模块共用
 * baseURL 不带 /api（由 vite proxy 添加 /api 前缀并转发到后端）
 */
export const api = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器 - 添加Token
api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    // 添加用户ID到请求头
    const userInfo = JSON.parse(localStorage.getItem('moli_mall_current_user') || '{}')
    if (userInfo?.userInfo?.id) {
      config.headers['X-User-Id'] = userInfo.userInfo.id
      // 收购商接口需要 X-Shop-Id，对于商家/收购商，shopId = userId
      config.headers['X-Shop-Id'] = userInfo.userInfo.id
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器 - 处理错误
api.interceptors.response.use(
  response => {
    // 确保返回的是后端返回的数据，而不是 undefined
    if (response && response.data !== undefined) {
      return response.data
    }
    // 如果响应数据为空，返回一个默认对象
    return { code: response?.status || 200, message: '操作成功', data: null }
  },
  error => {
    // 处理网络错误（后端未启动等）
    if (!error.response) {
      ElMessage.error('服务器连接失败，请检查后端服务是否启动')
      return Promise.reject(new Error('服务器连接失败'))
    }

    // 获取后端返回的错误信息
    const errorData = error.response.data
    let errorMessage = null
    
    if (errorData && typeof errorData === 'object') {
      errorMessage = errorData.message || errorData.msg || errorData.error
    }
    
    // HTTP状态码处理
    switch (error.response.status) {
      case 401:
        localStorage.removeItem('token')
        window.location.href = '/login'
        break
      case 403:
      case 404:
        // 静默处理，返回包含错误信息的对象
        return { 
          code: error.response.status, 
          message: errorMessage || '请求被拒绝', 
          data: null 
        }
      case 400:
      case 500:
        // 显示后端返回的错误消息
        if (errorMessage) {
          ElMessage.error(errorMessage)
        }
        // 返回包含错误信息的对象
        return { 
          code: error.response.status, 
          message: errorMessage || '操作失败', 
          data: null 
        }
      case 504:
        // 网关超时，静默处理
        return { code: 504, message: '请求超时，请稍后重试', data: null }
      default:
        if (errorMessage) {
          ElMessage.error(errorMessage)
        }
    }
    return Promise.reject(error)
  }
)

export default api