/**
 * 市场简报API
 * 功能：调用后端市场简报接口
 */
import { api } from './client.js'

// 生成日报
export const generateDailyBriefing = () => api.post('/briefing/generate/daily')

// 获取简报列表（分页）
export const getBriefingList = (params) => api.get('/briefing/list', { params })

// 获取简报详情
export const getBriefingDetail = (id) => api.get(`/briefing/${id}`)

// 获取最近简报
export const getRecentBriefings = (limit) => api.get('/briefing/recent', { params: { limit } })

// 删除简报
export const deleteBriefing = (id) => api.delete(`/briefing/${id}`)

// 发布简报
export const publishBriefing = (id) => api.put(`/briefing/${id}/publish`)
