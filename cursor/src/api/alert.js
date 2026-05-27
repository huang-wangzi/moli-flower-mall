/**
 * 价格预警API
 * 功能：调用后端价格预警接口
 */
import { api } from './client.js'

// 智能分析并生成预警
export const analyzeAlerts = () => api.post('/alert/analyze')

// 获取预警列表（分页）
export const getAlertList = (params) => api.get('/alert/list', { params })

// 获取预警统计
export const getAlertStats = () => api.get('/alert/stats')

// 获取未读预警数量
export const getUnreadCount = () => api.get('/alert/unread/count')

// 获取最近预警
export const getRecentAlerts = (limit) => api.get('/alert/recent', { params: { limit } })

// 获取预警详情
export const getAlertDetail = (id) => api.get(`/alert/${id}`)

// 标记已读
export const markAsRead = (id) => api.put(`/alert/${id}/read`)

// 标记已处理
export const markAsHandled = (id, handleBy, remark) => api.put('/alert/${id}/handle', null, {
  params: { handleBy, remark }
})

// 删除预警
export const deleteAlert = (id) => api.delete(`/alert/${id}`)
