/**
 * 价格监测API
 * 功能：提供前端调用的价格监测相关接口
 * 
 * 注意：client.js 的 baseURL 已配置为 http://localhost:8080/api
 * 所有请求路径不需要手动添加 /api 前缀
 */
import { api } from './client.js'

// 获取今日最新价格
export const getTodayPrices = () => api.get('/price/today')

// 获取多市场茉莉花价格趋势
export const getMultiMarketTrend = (params) => api.get('/price/multi-market-trend', { params })

// 获取单市场茉莉花价格走势
export const getSingleMarketTrend = (params) => api.get('/price/single-market-trend', { params })

// 获取价格概览
export const getPriceOverview = () => api.get('/price/overview')

// 获取最新价格
export const getLatestPrices = () => api.get('/price/latest')

// 获取指定市场的最新价格
export const getLatestPricesByMarket = (market) => api.get(`/price/latest/${market}`)

// 获取价格趋势
export const getPriceTrend = (params) => api.get('/price/trend', { params })

// 获取指定市场的价格趋势
export const getMarketPriceTrend = (market, params) => api.get(`/price/trend/${market}`, { params })

// 获取市场价格对比
export const getMarketPrices = (category) => api.get('/price/market', { params: { category } })

// 获取价格统计数据
export const getPriceStats = (params) => api.get('/price/stats', { params })

// 获取价格记录列表
export const getPriceList = (params) => api.get('/price/list', { params })

// 获取所有市场列表（从价格记录）
export const getAllPriceMarkets = () => api.get('/price/markets')

// 添加价格记录
export const addPriceRecord = (data) => api.post('/price', data)

// 更新价格记录
export const updatePriceRecord = (id, data) => api.put(`/price/${id}`, data)

// 删除价格记录
export const deletePriceRecord = (id) => api.delete(`/price/${id}`)

// 批量删除价格记录
export const batchDeletePriceRecords = (ids) => api.delete('/price/batch', { data: ids })

// 市场管理API

// 获取市场列表
export const getMarketList = () => api.get('/market/list')

// 获取所有市场
export const getAllMarkets = () => api.get('/market/all')

// 获取市场详情
export const getMarketDetail = (id) => api.get(`/market/${id}`)

// 添加市场
export const addMarket = (data) => api.post('/market', data)

// 更新市场
export const updateMarket = (data) => api.put('/market', data)

// 删除市场
export const deleteMarket = (id) => api.delete(`/market/${id}`)

// 批量删除市场
export const batchDeleteMarkets = (ids) => api.delete('/market/batch', { data: ids })

// 按城市获取市场
export const getMarketsByCity = (city) => api.get(`/market/city/${city}`)

// 按省份获取市场
export const getMarketsByProvince = (province) => api.get(`/market/province/${province}`)

// 按日期获取价格（最高价和最低价）
export const getPriceByDate = (params) => api.get('/price/by-date', { params })