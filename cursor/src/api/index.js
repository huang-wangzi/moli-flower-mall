// 从 client.js 导入 api 实例
import { api } from './client.js'

// 导出 api 实例供全局使用
export { api }
export default api

// ==================== 认证相关 ====================

// 健康检查 - 测试后端连接
export const healthCheck = () => api.get('/auth/health')

// 登录（query + body 均带 loginType，避免部分环境下 query 丢失导致管理员被当成普通用户校验）
export const login = (data, loginType = 'user') =>
  api.post('/auth/login', { ...data, loginType }, { params: { loginType } })

// 注册（roleStr: user/shop/acquirer）
export const register = (data) => api.post('/auth/register', {
  username: data.username,
  password: data.password,
  phone: data.phone,
  roleStr: data.role,
  shopName: data.shopName || '',
  shopCategory: data.shopCategory || '',
  merchantName: data.merchantName || ''
})

// 获取用户信息
export const getUserInfo = () => api.get('/auth/info')

// 根据ID获取用户信息（用于聊天等场景）
export const getUserInfoById = (userId) => api.get(`/auth/user/${userId}`)

// 退出登录
export const logout = () => api.post('/auth/logout')

// ==================== 商品相关 ====================

// 获取商品列表
export const getProductList = (params) => api.get('/product/list', { params })

// 获取商品详情
export const getProductDetail = (id) => api.get(`/product/${id}`)

// 获取商家商品列表
export const getShopProducts = (shopId) => api.get(`/product/shop/${shopId}`)

// 获取热门商品
export const getHotProducts = (limit = 10) => api.get('/product/hot', { params: { limit } })

// 新增商品
export const addProduct = (data) => api.post('/product', data)

// 更新商品
export const updateProduct = (data) => api.put('/product', data)

// 更新商品状态
export const updateProductStatus = (id, status) => api.put(`/product/${id}/status`, null, { params: { status } })

// 删除商品
export const deleteProduct = (id) => api.delete(`/product/${id}`)

// ==================== 订单相关 ====================

// 获取用户订单列表
export const getUserOrders = (params) => api.get('/order/user/list', { params })

// 获取商家订单列表（带商品明细）
export const getShopOrders = (params) => api.get('/order/shop/list-with-items', { params })

// ==================== 用户个人中心 ====================
// 获取用户详情（根据用户ID）
export const getUserDetailById = (userId) => api.get('/user/info', { params: { userId } })
export const updateUserInfo = (userId, data) => api.put('/user/update', null, { params: { userId, ...data } })
export const cancelAccount = (userId) => api.delete(`/user/cancel`, { params: { userId } })

// 获取订单详情
export const getOrderDetail = (id) => api.get(`/order/${id}`)

// 创建订单
export const createOrder = (data) => api.post('/order/create', data)

// 支付订单（可传递收货信息）
export const payOrder = (id, payType, receiverName, receiverPhone, receiverAddress, remark) => {
  const params = { payType }
  if (receiverName) params.receiverName = receiverName
  if (receiverPhone) params.receiverPhone = receiverPhone
  if (receiverAddress) params.receiverAddress = receiverAddress
  if (remark) params.remark = remark
  return api.put(`/order/${id}/pay`, null, { params })
}

// 商家发货
export const shipOrder = (id, remark) => api.put(`/order/${id}/ship`, null, { params: { remark } })

// 用户确认收货
export const receiveOrder = (id) => api.put(`/order/${id}/receive`)

// 取消订单
export const cancelOrder = (id, remark) => api.put(`/order/${id}/cancel`, null, { params: { remark } })

// ==================== 评价相关 ====================

// 获取商品评价列表
export const getProductReviews = (productId) => api.get(`/review/product/${productId}`)

// 获取用户评价列表
export const getUserReviews = (userId) => api.get(`/review/user/${userId}`)

// ==================== 售后相关 ====================

// 获取商家售后列表
export const getShopRefunds = (shopId) => api.get('/refund/shop/list', { params: { shopId } })

// 获取用户售后列表
export const getUserRefunds = (userId) => api.get('/refund/user/list', { params: { userId } })

// 创建售后申请
export const createRefund = (data) => api.post('/refund/create', data)

// 客诉 / 客服介入（同步管理员端「交易投诉」）
export const createComplaint = (data) => api.post('/complaint/create', data)

// 处理售后申请（商家端）
export const processRefund = (id, status, remark, userId, shopId) =>
  api.put(`/refund/shop/${id}/process`, null, { params: { status, remark, userId, shopId } })

// 取消售后申请（用户端）
export const cancelRefund = (id) => api.post(`/refund/${id}/cancel`)

// 再次申诉（用户端：重新创建售后并申诉）
export const reAppealRefund = (data) => api.post('/refund/re-appeal', data)

// 添加评价
export const addReview = (data) => api.post('/review', data)

// 商家回复评价
export const replyReview = (id, reply) => api.put(`/review/${id}/reply`, reply, {
  headers: { 'Content-Type': 'text/plain' }
})

// 获取商家所有评价
export const getShopReviews = (shopId) => api.get(`/review/shop/${shopId}`)

// ==================== 管理员相关 ====================

// 获取用户列表
export const getAdminUsers = (params) => api.get('/admin/users', { params })

// 获取商家列表
export const getAdminShops = (params) => api.get('/admin/shops', { params })

// 审核商家通过
export const approveShop = (id) => api.put(`/admin/shop/${id}/approve`)

// 审核商家拒绝
export const rejectShop = (id, reason) => api.put(`/admin/shop/${id}/reject`, reason, {
  headers: { 'Content-Type': 'text/plain' }
})

// 修改用户状态
export const updateUserStatus = (id, status) => api.put(`/admin/user/${id}/status`, null, { params: { status } })

// 获取所有商品（管理员）
export const getAdminProducts = (params) => api.get('/admin/products', { params })

// 获取待审核商品
export const getAdminPendingProducts = () => api.get('/admin/products/pending')

// 审核商品通过
export const approveProduct = (id) => api.put(`/admin/product/${id}/approve`)

// 审核商品拒绝
export const rejectProduct = (id) => api.put(`/admin/product/${id}/reject`)

// 下架商品
export const offlineProduct = (id) => api.put(`/admin/product/${id}/offline`)

// 获取所有评价（管理员）
export const getAdminReviews = () => api.get('/admin/reviews')

// 删除评价
export const deleteReview = (id) => api.delete(`/admin/review/${id}`)

// 获取统计数据
export const getAdminStats = () => api.get('/admin/stats')

// 获取用户增长趋势
export const getUserGrowthTrend = () => api.get('/admin/user-growth-trend')

// 获取商品趋势
export const getProductTrend = () => api.get('/admin/product-trend')

// ==================== 收货地址 ====================
export const getAddressList = () => api.get('/address/list')

export const addAddress = (data) => api.post('/address/add', data)

export const updateAddress = (data) => api.put('/address/update', data)

export const deleteAddress = (id) => api.delete(`/address/${id}`)

export const setDefaultAddress = (id) => api.put(`/address/default/${id}`)

export const getAdminComplaints = () => api.get('/admin/complaints')

export const handleAdminComplaint = (id, status, remark) =>
  api.put(`/admin/complaints/${id}`, null, { params: { status, remark: remark || '' } })

// ==================== 收购商地址管理 ====================
export const getAcquirerAddressList = () => api.get('/acquirer/address/list')
export const addAcquirerAddress = (data) => api.post('/acquirer/address/add', data)
export const updateAcquirerAddress = (data) => api.put('/acquirer/address/update', data)
export const deleteAcquirerAddress = (id) => api.delete(`/acquirer/address/${id}`)
export const setAcquirerDefaultAddress = (id) => api.put(`/acquirer/address/default/${id}`)

// ==================== 消息相关 ====================

// 获取消息列表（带发送者信息）
export const getMessageList = (userId) => api.get('/message/list', { params: { userId } })

// 获取未读数量
export const getUnreadCount = (userId) => api.get('/message/unread', { params: { userId } })

// 标记已读
export const markMessageRead = (id) => api.put(`/message/${id}/read`)

// 全部标记已读
export const markAllMessageRead = (userId) => api.put('/message/readAll', null, { params: { userId } })

// 删除消息
export const deleteMessage = (id) => api.delete(`/message/${id}`)

// 价格监测API - 从price.js导入
export * from './price'

// 商家资质API - 从shop.js导入
export * from './shop'

// 天气API - 从weather.js导入
export * from './weather'

// 价格预警API - 从alert.js导入
export * from './alert'

// 市场简报API - 从briefing.js导入
export * from './briefing'

// ==================== 文件上传相关 ====================

// 上传单张图片
export const uploadImage = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  // 走代理：vite proxy 配置了 /uploads → localhost:8080/uploads
  return api.post('/upload/image', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 批量上传图片
export const uploadImages = (files) => {
  const formData = new FormData()
  files.forEach(file => {
    formData.append('files', file)
  })
  return api.post('/upload/images', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// ==================== 聊天相关 ====================

// 平台客服（管理员）用户 ID
export const getSupportAdminId = () => api.get('/chat/support-admin-id')

// 获取与用户的聊天历史
export const getChatHistory = (toUserId) => api.get(`/chat/history/${toUserId}`)

// 获取广播消息对话（含原消息+回复）
export const getBroadcastHistory = (msgId) => api.get(`/chat/broadcast/${msgId}`)

// 商家回复广播消息
export const sendBroadcastReply = (msgId, content) =>
  api.post('/chat/broadcast-reply', null, { params: { msgId, content } })

// 获取与商品商家的聊天历史
export const getChatHistoryByProduct = (productId) => api.get(`/chat/product/${productId}`)

// 发送消息（无商品上下文时不传 productId，避免空字符串导致后端类型转换异常）
export const sendChatMessage = (toUserId, content, productId) => {
  const params = { toUserId, content }
  if (productId != null && productId !== '') {
    params.productId = productId
  }
  return api.post('/chat/send', null, { params })
}

// 获取会话列表
export const getChatConversations = () => api.get('/chat/conversations')

// 获取未读消息数
export const getChatUnreadCount = () => api.get('/chat/unread')

// 标记与指定用户的聊天消息为已读
export const markChatConversationRead = (fromUserId, toUserId) => 
  api.put('/chat/mark-read', null, { params: { fromUserId, toUserId } })

// ==================== 管理员客服消息 ====================
// 获取管理员收到的用户聊天会话列表
export const getAdminChatConversations = () => api.get('/chat/admin/conversations')

// 获取与指定用户的聊天记录（管理员视角）
export const getAdminChatHistory = (userId) => api.get(`/chat/admin/history/${userId}`)

// 管理员发送消息给用户
export const adminSendChatMessage = (toUserId, content, productId) => {
  const params = { toUserId, content }
  if (productId != null && productId !== '') {
    params.productId = productId
  }
  return api.post('/chat/admin/send', null, { params })
}

// 管理员标记与用户的所有消息为已读
export const adminMarkChatRead = (userId) => api.put(`/chat/admin/mark-read/${userId}`)

// ==================== 管理员系统消息 ====================
export const getAdminSystemMessages = () => api.get('/admin/system-messages')
export const sendAdminSystemMessage = (scope, title, content) =>
  api.post('/admin/system-message', null, { params: { scope, title, content } })
export const deleteAdminSystemMessage = (id) => api.delete(`/admin/system-message/${id}`)