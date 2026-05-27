import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'
import { useUserStore } from './user'
import { getAdminStats, getAdminUsers, getAdminShops, getAdminProducts, getAdminReviews } from '@/api'

export const useAdminStore = defineStore('admin', () => {
  const userStore = useUserStore()

  // 所有用户列表
  const allUsers = ref([])

  // 所有商家列表
  const allShops = ref([])

  // 所有商品列表
  const allProducts = ref([])

  // 所有评价列表
  const allReviews = ref([])

  // 系统统计
  const systemStats = ref({
    totalUsers: 0,
    totalShops: 0,
    totalProducts: 0,
    totalOrders: 0,
    totalSales: 0,
    totalBuyers: 0,
    pendingShops: 0,
    pendingProducts: 0,
    pendingComplaints: 0
  })

  // 加载统计数据
  const loadStats = async () => {
    try {
      const res = await getAdminStats()
      if (res.code === 200) {
        const data = res.data
        systemStats.value = {
          totalUsers: data.userCount || 0,
          totalShops: data.shopCount || 0,
          totalProducts: data.productCount || 0,
          totalOrders: 0,
          totalSales: 0,
          totalBuyers: data.acquirerCount || 0,
          pendingShops: data.pendingShopCount || 0,
          pendingProducts: data.pendingProductCount || 0,
          pendingComplaints: 0
        }
      }
    } catch (error) {
      console.error('加载统计数据失败', error)
    }
  }

  // 加载用户列表
  const loadUsers = async () => {
    try {
      const res = await getAdminUsers({ pageNum: 1, pageSize: 100 })
      if (res.code === 200) {
        allUsers.value = res.data.records || []
      }
    } catch (error) {
      console.error('加载用户列表失败', error)
    }
  }

  // 加载商家列表
  const loadShops = async () => {
    try {
      const res = await getAdminShops({ pageNum: 1, pageSize: 100 })
      if (res.code === 200) {
        allShops.value = res.data.records || []
      }
    } catch (error) {
      console.error('加载商家列表失败', error)
    }
  }

  // 加载商品列表
  const loadProducts = async () => {
    try {
      const res = await getAdminProducts({ pageNum: 1, pageSize: 100 })
      if (res.code === 200) {
        allProducts.value = res.data.records || []
      }
    } catch (error) {
      console.error('加载商品列表失败', error)
    }
  }

  // 加载评价列表
  const loadReviews = async () => {
    try {
      const res = await getAdminReviews()
      if (res.code === 200) {
        allReviews.value = res.data || []
      }
    } catch (error) {
      console.error('加载评价列表失败', error)
    }
  }

  // 加载管理员数据
  const loadAdminData = async () => {
    await loadStats()
    await loadUsers()
    await loadShops()
    await loadProducts()
    await loadReviews()
  }

  // 清空数据
  const clearData = () => {
    allUsers.value = []
    allShops.value = []
    allProducts.value = []
    allReviews.value = []
    systemStats.value = {
      totalUsers: 0,
      totalShops: 0,
      totalProducts: 0,
      totalOrders: 0,
      totalSales: 0,
      totalBuyers: 0,
      pendingShops: 0,
      pendingProducts: 0,
      pendingComplaints: 0
    }
  }

  // ====================== 【我把 watch 放到这里！】======================
  // 监听用户登录状态
  watch(() => userStore.userInfo, (newUser) => {
    if (newUser && newUser.role === 'admin') {
      loadAdminData()
    } else {
      // 登出时清空数据
      clearData()
    }
  }, { immediate: true })
  // ======================================================================

  // 更新系统统计
  const updateSystemStats = () => {
  }

  // 审核商家
  const approveShop = (shopId) => {
    const shop = allShops.value.find(s => s.id === shopId)
    if (shop) {
      shop.status = 2
      updateSystemStats()
    }
  }

  // 拒绝商家
  const rejectShop = (shopId) => {
    const shop = allShops.value.find(s => s.id === shopId)
    if (shop) {
      shop.status = 3
      updateSystemStats()
    }
  }

  // 审核商品
  const approveProduct = (productId) => {
    const product = allProducts.value.find(p => p.id === productId)
    if (product) {
      product.status = 1
      updateSystemStats()
    }
  }

  // 拒绝商品
  const rejectProduct = (productId) => {
    const product = allProducts.value.find(p => p.id === productId)
    if (product) {
      product.status = 2
      updateSystemStats()
    }
  }

  // 删除商品
  const deleteProduct = (productId) => {
    const index = allProducts.value.findIndex(p => p.id === productId)
    if (index > -1) {
      allProducts.value.splice(index, 1)
      updateSystemStats()
    }
  }

  // 禁用用户
  const disableUser = (userId) => {
    const user = allUsers.value.find(u => u.id === userId)
    if (user) {
      user.status = 0
    }
  }

  // 启用用户
  const enableUser = (userId) => {
    const user = allUsers.value.find(u => u.id === userId)
    if (user) {
      user.status = 1
    }
  }

  // 删除评价
  const deleteReview = (reviewId) => {
    const index = allReviews.value.findIndex(r => r.id === reviewId)
    if (index > -1) {
      allReviews.value.splice(index, 1)
    }
  }

  // 发送系统消息
  const sendSystemMessage = (msg) => {
    const users = JSON.parse(localStorage.getItem('moli_mall_users') || '[]')
    users.forEach(user => {
      const userData = JSON.parse(localStorage.getItem(`moli_mall_user_${user.id}`) || '{}')
      if (!userData.messages) userData.messages = []
      userData.messages.unshift({
        id: Date.now(),
        type: 'system',
        title: msg.title,
        content: msg.content,
        time: new Date().toLocaleString(),
        read: false
      })
      localStorage.setItem(`moli_mall_user_${user.id}`, JSON.stringify(userData))
    })
  }

  return {
    allUsers,
    allShops,
    allProducts,
    allReviews,
    systemStats,
    loadAdminData,
    loadStats,
    loadUsers,
    loadShops,
    loadProducts,
    loadReviews,
    updateSystemStats,
    clearData,
    approveShop,
    rejectShop,
    approveProduct,
    rejectProduct,
    deleteProduct,
    disableUser,
    enableUser,
    deleteReview,
    sendSystemMessage
  }
})