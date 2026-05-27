/**
 * 茉莉花收购专栏 API
 * 连接花农与收购商，提供直销渠道
 * 
 * 注意：client.js 的 baseURL 已配置为 http://localhost:8080/api
 * 所有请求路径不需要手动添加 /api 前缀
 */
import { api as request } from './client'

// ==================== 收购商端 API ====================

/**
 * 获取收购商的需求列表
 */
export function getMerchantAcquisitionDemands(page = 1, size = 10) {
  return request.get('/acquisition/merchant/demands', { params: { page, size } })
}

export const getMerchantDemands = getMerchantAcquisitionDemands

/**
 * 获取今日需求数量
 */
export function getTodayDemandCount() {
  return request.get('/acquisition/merchant/demands/today/count')
}

/**
 * 创建收购需求
 */
export function createMerchantDemand(data) {
  return request.post('/acquisition/merchant/demand', data)
}

export const createDemand = createMerchantDemand

/**
 * 更新收购需求
 */
export function updateMerchantDemand(id, data) {
  return request.put(`/acquisition/merchant/demand/${id}`, data)
}

export const updateDemand = updateMerchantDemand

/**
 * 删除收购需求
 */
export function deleteMerchantDemand(id) {
  return request.delete(`/acquisition/merchant/demand/${id}`)
}

export const deleteDemand = deleteMerchantDemand

/**
 * 下架收购需求
 */
export function offShelfDemand(id) {
  return request.put(`/acquisition/merchant/demand/${id}/offshelf`)
}

/**
 * 获取某需求的所有申请列表
 */
export function getDemandApplications(demandId, page = 1, size = 10) {
  return request.get(`/acquisition/merchant/application/${demandId}`, { params: { page, size } })
}

/**
 * 同意申请
 */
export function agreeApplication(id) {
  return request.put(`/acquisition/merchant/application/${id}/agree`)
}

/**
 * 拒绝申请
 */
export function rejectApplication(id, reason) {
  return request.put(`/acquisition/merchant/application/${id}/reject`, { reason })
}

/**
 * 完成订单（线下验货后）
 */
export function completeDelivery(id, data) {
  return request.put(`/acquisition/order/${id}/complete`, data)
}

export const completeOrder = completeDelivery

/**
 * 获取收购订单列表
 */
export function getMerchantOrders(page = 1, size = 10, status) {
  return request.get('/acquisition/merchant/orders', { params: { page, size, status } })
}

/**
 * 获取收购商统计信息（今日收购量、今日收购额、待处理订单、累计收购量）
 */
export function getMerchantStats() {
  return request.get('/acquisition/merchant/stats')
}

/**
 * 获取收购商每日统计数据（用于图表）
 */
export function getMerchantDailyStats(startDate, endDate) {
  return request.get('/acquisition/merchant/stats/daily', {
    params: { startDate, endDate }
  })
}

// ==================== 用户端 API ====================

/**
 * 获取收购需求列表
 */
export function getAcquisitionDemands(page = 1, size = 10) {
  return request.get('/acquisition/demands', { params: { page, size } })
}

/**
 * 获取所有活跃的收购需求
 */
export function getActiveAcquisitionDemands() {
  return request.get('/acquisition/demands/active')
}

/**
 * 获取收购需求详情
 */
export function getAcquisitionDemandDetail(id) {
  return request.get(`/acquisition/demand/${id}`)
}

/**
 * 检查用户是否已申请过某需求
 */
export function checkDemandApplied(demandId) {
  return request.get(`/acquisition/application/check/${demandId}`)
}

/**
 * 创建收购申请
 */
export function createAcquisitionApplication(data) {
  return request.post('/acquisition/application', data)
}

/**
 * 获取我的申请记录列表
 */
export function getMyAcquisitionApplications(page = 1, size = 10) {
  return request.get('/acquisition/application/my', { params: { page, size } })
}

/**
 * 取消我的申请
 */
export function cancelAcquisitionApplication(id) {
  return request.put(`/acquisition/application/${id}/cancel`)
}

/**
 * 修改我的申请
 */
export function updateAcquisitionApplication(id, data) {
  return request.put(`/acquisition/application/${id}`, data)
}

/**
 * 获取我的收购统计信息
 */
export function getMyAcquisitionStats() {
  return request.get('/acquisition/application/my/stats')
}

/**
 * 获取我的每日收购统计
 */
export function getMyDailyAcquisitionStats(startDate, endDate) {
  return request.get('/acquisition/application/my/stats/daily', {
    params: { startDate, endDate }
  })
}

/**
 * 获取我的收购订单列表
 */
export function getMyAcquisitionOrders(page = 1, size = 10, status) {
  return request.get('/acquisition/order/my', { params: { page, size, status } })
}

/**
 * 获取我的收入报表统计
 */
export function getMyIncomeStats(type = 'daily') {
  return request.get('/acquisition/income/stats', { params: { type } })
}

/**
 * 获取我的收入明细列表
 */
export function getMyIncomeDetails(page = 1, size = 10, startDate, endDate) {
  return request.get('/acquisition/income/details', {
    params: { page, size, startDate, endDate }
  })
}

// ==================== 管理员端 API ====================

/**
 * 管理员获取所有收购需求
 */
export function getAllAcquisitionDemands(status) {
  const params = {}
  if (status !== undefined && status !== null) {
    params.status = status
  }
  return request.get('/acquisition/admin/demands', { params })
}

/**
 * 管理员下架违规收购需求
 */
export function adminOffShelfDemand(id) {
  return request.put(`/acquisition/admin/demand/${id}/offshelf`)
}

/**
 * 管理员删除收购需求
 */
export function adminDeleteDemand(id) {
  return request.delete(`/acquisition/admin/demand/${id}`)
}

/**
 * 获取收购价格统计（最低价、最高价）
 */
export function getAcquisitionPriceStats() {
  return request.get('/acquisition/admin/price-stats')
}

/**
 * 获取每日价格变化趋势
 */
export function getAcquisitionPriceTrend(days = 7) {
  return request.get('/acquisition/admin/price-trend', { params: { days } })
}

/**
 * 管理员获取所有收购申请（订单）
 */
export function getAllAcquisitionApplications(page = 1, size = 20, status) {
  const params = { page, size }
  if (status !== undefined && status !== null) {
    params.status = status
  }
  return request.get('/acquisition/admin/applications', { params })
}

/**
 * 获取收购商待处理申请数量
 */
export function getPendingApplicationCount() {
  return request.get('/acquisition/merchant/application/pending/count')
}

// ==================== 用户端价格统计 API ====================

/**
 * 获取收购商价格统计（从收购需求数据获取）
 */
export function getAcquirerPriceStats() {
  return request.get('/acquisition/price-stats')
}

/**
 * 获取收购商每日价格趋势
 */
export function getAcquirerPriceTrend(days = 30) {
  return request.get('/acquisition/price-trend', { params: { days } })
}
