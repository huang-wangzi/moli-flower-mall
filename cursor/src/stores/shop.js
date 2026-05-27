import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'
import { useUserStore } from './user'
import { getShopProducts, getShopOrders, addProduct, deleteProduct as apiDeleteProduct, updateProductStatus } from '@/api'

export const useShopStore = defineStore('shop', () => {
  const userStore = useUserStore()

  const products = ref([])
  const shopOrders = ref([])
  const messages = ref([])
  const stats = ref({
    totalOrders: 0,
    totalSales: 0,
    totalProfit: 0,
    totalProducts: 0,
    pendingOrders: 0,
    pendingShip: 0,
    visitors: 0
  })

  // 从后端加载商家数据（商品列表、订单列表）
  const loadShopData = async (shopId) => {
    if (!shopId) return
    try {
      // 加载商家商品
      const prodRes = await getShopProducts(shopId)
      if (prodRes.code === 200) {
        products.value = prodRes.data || []
      }
      // 加载商家订单（/order/shop/list-with-items 返回 List<Map>）
      const orderRes = await getShopOrders({ shopId })
      if (orderRes.code === 200) {
        // 后端返回 List<Map>，直接用 data 数组
        shopOrders.value = Array.isArray(orderRes.data) ? orderRes.data : []
      }
    } catch (e) {
      console.error('加载商家数据失败', e)
    }
    updateStats()
    saveShopData()
  }

  const saveShopData = () => {
    if (userStore.userInfo?.role === 'shop') {
      const data = {
        products: products.value,
        shopOrders: shopOrders.value,
        messages: messages.value,
        stats: stats.value
      }
      localStorage.setItem(`moli_mall_shop_${userStore.userInfo.id}`, JSON.stringify(data))
    }
  }

  // 计算统计数据
  const updateStats = () => {
    const completedOrders = shopOrders.value.filter(o => o.status === 3)
    stats.value.totalOrders = shopOrders.value.length
    stats.value.totalSales = completedOrders.reduce((sum, o) => sum + Number(o.actualAmount || 0), 0)
    stats.value.totalProfit = completedOrders.reduce((sum, o) => sum + Number(o.actualAmount || 0) * 0.2, 0)
    stats.value.totalProducts = products.value.filter(p => p.status === 1).length
    stats.value.pendingOrders = shopOrders.value.filter(o => o.status === 0).length
    stats.value.pendingShip = shopOrders.value.filter(o => o.status === 1).length
  }

  // 获取每周销售额数据
  const getWeeklySales = () => {
    const days = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    const values = [0, 0, 0, 0, 0, 0, 0]
    const now = new Date()
    shopOrders.value.forEach(order => {
      if (order.status === 3 && order.createTime) {
        const orderDate = new Date(order.createTime)
        const daysDiff = Math.floor((now - orderDate) / (1000 * 60 * 60 * 24))
        if (daysDiff >= 0 && daysDiff < 7) {
          const dayIndex = (orderDate.getDay() + 6) % 7
          values[dayIndex] += Number(order.actualAmount || 0)
        }
      }
    })
    return { days, values }
  }

  // 获取每周利润数据
  const getWeeklyProfit = () => {
    const days = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    const values = [0, 0, 0, 0, 0, 0, 0]
    const now = new Date()
    shopOrders.value.forEach(order => {
      if (order.status === 3 && order.createTime) {
        const orderDate = new Date(order.createTime)
        const daysDiff = Math.floor((now - orderDate) / (1000 * 60 * 60 * 24))
        if (daysDiff >= 0 && daysDiff < 7) {
          const dayIndex = (orderDate.getDay() + 6) % 7
          values[dayIndex] += Number(order.actualAmount || 0) * 0.2
        }
      }
    })
    return { days, values }
  }

  const addProduct = (product) => {
    const newProduct = { id: Date.now(), ...product, sales: 0, status: 1, createTime: new Date().toLocaleString() }
    products.value.unshift(newProduct)
    updateStats()
    saveShopData()
    return newProduct
  }

  const updateProduct = (productId, updates) => {
    const index = products.value.findIndex(p => p.id === productId)
    if (index > -1) { products.value[index] = { ...products.value[index], ...updates }; saveShopData() }
  }

  const deleteProduct = async (productId) => {
    try {
      const res = await apiDeleteProduct(productId)
      if (res.code === 200) {
        products.value = products.value.filter(p => p.id !== productId)
        updateStats()
      } else {
        throw new Error(res.message)
      }
    } catch (e) {
      console.error('删除商品失败', e)
      throw e
    }
  }

  // 切换商品状态（商家自主操作）
  const toggleProductStatus = async (productId) => {
    const p = products.value.find(x => x.id === productId)
    if (!p) return

    try {
      // 已上架(status=1) → 下架(status=2)，无需审核
      if (p.status === 1) {
        const res = await updateProductStatus(p.id, 2)
        if (res.code === 200) {
          p.status = 2
          saveShopData()
        }
      }
      // 已下架(status=2) → 重新提交审核(status=0)，需要管理员审核
      else if (p.status === 2) {
        const res = await updateProductStatus(p.id, 0)
        if (res.code === 200) {
          p.status = 0
          saveShopData()
        }
      }
      // 待审核(status=0) → 无操作（已经在审核中）
    } catch (e) {
      console.error('操作商品状态失败', e)
      throw e
    }
  }

  const getProductById = (productId) => products.value.find(p => p.id === productId)

  const addOrder = (order) => {
    shopOrders.value.unshift(order)
    updateStats()
    saveShopData()
  }

  const updateOrderStatus = (orderId, status, statusText) => {
    const o = shopOrders.value.find(x => x.id === orderId)
    if (o) { o.status = status; if (statusText) o.statusText = statusText; if (status === 2) o.shipTime = new Date().toLocaleString(); updateStats(); saveShopData() }
  }

  const getOrderById = (orderId) => shopOrders.value.find(o => o.id === orderId)
  const getOrders = (status = 'all') => status === 'all' ? shopOrders.value : shopOrders.value.filter(o => o.status === status)

  const addMessage = (msg) => {
    messages.value.unshift({ id: Date.now(), ...msg, time: new Date().toLocaleString(), read: false })
    saveShopData()
  }

  const markAsRead = (msgId) => {
    const m = messages.value.find(x => x.id === msgId)
    if (m) { m.read = true; saveShopData() }
  }

  const deleteMessage = (msgId) => {
    messages.value = messages.value.filter(m => m.id !== msgId)
    saveShopData()
  }

  // 监听用户登录状态
  watch(() => userStore.userInfo, (newUser) => {
    if (newUser?.role === 'shop') {
      loadShopData(newUser.id)
    } else {
      products.value = []
      shopOrders.value = []
      messages.value = []
      stats.value = { totalOrders: 0, totalSales: 0, totalProfit: 0, totalProducts: 0, pendingOrders: 0, pendingShip: 0, visitors: 0 }
    }
  }, { deep: true, immediate: true })

  const unreadCount = computed(() => messages.value.filter(m => !m.read).length)
  const pendingOrdersCount = computed(() => shopOrders.value.filter(o => o.status === 0 || o.status === 1).length)
  const lowStockCount = computed(() => products.value.filter(p => p.status === 1 && p.stock < 10).length)

  return {
    products, shopOrders, messages, stats,
    unreadCount, pendingOrdersCount, lowStockCount,
    loadShopData, saveShopData, addProduct, updateProduct, deleteProduct,
    toggleProductStatus, getProductById, addOrder, updateOrderStatus,
    getOrderById, getOrders, updateStats, addMessage, markAsRead, deleteMessage,
    getWeeklySales, getWeeklyProfit
  }
})
