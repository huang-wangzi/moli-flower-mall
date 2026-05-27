import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'

// localStorage keys
const USERS_KEY = 'moli_mall_users'
const CURRENT_USER_KEY = 'moli_mall_current_user'
const SHOP_DATA_PREFIX = 'moli_mall_shop_'
const ADMIN_DATA_KEY = 'moli_mall_admin_data'

// 获取本地存储的用户列表
const getUsers = () => {
  const users = localStorage.getItem(USERS_KEY)
  return users ? JSON.parse(users) : []
}

// 保存用户列表到本地
const saveUsers = (users) => {
  localStorage.setItem(USERS_KEY, JSON.stringify(users))
}

// 获取当前用户数据
const getCurrentUserData = () => {
  const data = localStorage.getItem(CURRENT_USER_KEY)
  return data ? JSON.parse(data) : null
}

// 保存当前用户数据
const saveCurrentUserData = (data) => {
  localStorage.setItem(CURRENT_USER_KEY, JSON.stringify(data))
}

// 清除所有登录相关数据
const clearLoginData = () => {
  localStorage.removeItem(CURRENT_USER_KEY)
  // 注意：不清除 token，token 由 Login.vue 管理
}

export const useUserStore = defineStore('user', () => {
  // 从localStorage初始化用户信息
  const currentUserData = getCurrentUserData()

  // 用户信息
  const userInfo = ref(currentUserData?.userInfo || null)

  // 登录状态
  const isLoggedIn = ref(!!currentUserData?.userInfo)

  // 用户购物车
  const cart = ref(currentUserData?.cart || [])

  // 用户订单
  const orders = ref(currentUserData?.orders || [])

  // 用户消息
  const messages = ref(currentUserData?.messages || [])

  // 用户地址
  const addresses = ref(currentUserData?.addresses || [])

  // 用户优惠券
  const coupons = ref(currentUserData?.coupons || [])

  // 浏览历史
  const browseHistory = ref(currentUserData?.browseHistory || [])

  // 用户评价
  const reviews = ref(currentUserData?.reviews || [])

  // 监听数据变化，保存到localStorage
  watch([userInfo, cart, orders, messages, addresses, coupons, browseHistory, reviews], () => {
    if (isLoggedIn.value && userInfo.value) {
      saveCurrentUserData({
        userInfo: userInfo.value,
        cart: cart.value,
        orders: orders.value,
        messages: messages.value,
        addresses: addresses.value,
        coupons: coupons.value,
        browseHistory: browseHistory.value,
        reviews: reviews.value
      })
    }
  }, { deep: true })

  // 注册新用户
  const register = async (userData) => {
    try {
      const { register: registerApi } = await import('@/api')
      const res = await registerApi({
        username: userData.username,
        password: userData.password,
        phone: userData.phone,
        role: userData.role,
        shopName: userData.shopName || '',
        shopCategory: userData.shopCategory || '',
        merchantName: userData.merchantName || '',
        contactPhone: userData.contactPhone || '',
        marketAddress: userData.marketAddress || ''
      })
      if (res.code !== 200) {
        return { success: false, message: res.message || '注册失败' }
      }
      return { success: true, message: res.message || '注册成功' }
    } catch (e) {
      console.error('注册请求失败', e)
      return { success: false, message: '服务器连接失败，请检查后端是否启动' }
    }
  }

  // 登录
  const login = async (data) => {
    // 清除旧用户数据（保留token，由Login.vue管理）
    clearLoginData()

    // 构建用户信息
    const loginRole = data.role || 'user'
    
    const newUserInfo = {
      id: data.id,
      username: data.username,
      nickname: data.nickname || '',
      avatar: data.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
      gender: data.gender ?? 0,
      phone: data.phone || '',
      email: data.email || '',
      role: loginRole,
      roleNum: data.roleNum || 1,
      shopType: data.shopType || null,
      balance: data.balance || 0,
      memberPoints: data.memberPoints || 0,
      memberLevel: data.memberLevel || '普通会员',
      createdAt: data.createdAt || new Date().toISOString().split('T')[0],
      shopName: data.shopName || '',
      shopCategory: data.shopCategory || '',
      shopQualificationStatus: data.shopQualificationStatus ?? 0,
      specialty: data.specialty || '',
      experience: data.experience || 0,
      bio: data.bio || ''
    }

    // 更新状态
    userInfo.value = newUserInfo
    isLoggedIn.value = true

    // 保存到localStorage
    saveCurrentUserData({
      userInfo: newUserInfo,
      cart: [],
      orders: [],
      messages: [],
      addresses: [],
      coupons: [],
      browseHistory: [],
      reviews: []
    })

    // 同步到用户列表
    const users = getUsers()
    const userIndex = users.findIndex(u => u.id === data.id)
    if (userIndex > -1) {
      users[userIndex] = { ...users[userIndex], ...newUserInfo }
    } else {
      users.push({ ...newUserInfo, password: '' })
    }
    saveUsers(users)

    // 加载角色相关数据
    loadUserData(data.id, loginRole)

    return { success: true, user: userInfo.value }
  }

  // 加载指定用户的数据
  const loadUserData = (userId, role = 'user') => {
    if (role === 'admin') {
      const adminData = localStorage.getItem(ADMIN_DATA_KEY)
      if (adminData) {
        const data = JSON.parse(adminData)
        messages.value = data.messages || []
      } else {
        messages.value = []
      }
      cart.value = []
      orders.value = []
      addresses.value = []
      coupons.value = []
      browseHistory.value = []
      reviews.value = []
      return
    }

    if (role === 'shop' || role === 'acquirer') {
      const key = role === 'acquirer' ? `moli_mall_acquirer_${userId}` : `${SHOP_DATA_PREFIX}${userId}`
      const shopData = localStorage.getItem(key)
      if (shopData) {
        const data = JSON.parse(shopData)
        messages.value = data.messages || []
      } else {
        messages.value = []
      }
      cart.value = []
      orders.value = []
      addresses.value = []
      coupons.value = []
      browseHistory.value = []
      reviews.value = []
      return
    }

    // 普通用户数据
    const storedData = localStorage.getItem(`moli_mall_user_${userId}`)
    if (storedData) {
      const data = JSON.parse(storedData)
      cart.value = data.cart || []
      orders.value = data.orders || []
      messages.value = data.messages || []
      addresses.value = data.addresses || []
      coupons.value = data.coupons || []
      browseHistory.value = data.browseHistory || []
      reviews.value = data.reviews || []
    }
  }

  // 保存用户数据
  const saveUserData = () => {
    if (!userInfo.value) return

    saveCurrentUserData({
      userInfo: userInfo.value,
      cart: cart.value,
      orders: orders.value,
      messages: messages.value,
      addresses: addresses.value,
      coupons: coupons.value,
      browseHistory: browseHistory.value,
      reviews: reviews.value
    })
  }

  // 登出 - 彻底清除所有登录数据
  const logout = () => {
    // 清除所有相关数据
    clearLoginData()

    // 重置状态
    userInfo.value = null
    isLoggedIn.value = false
    cart.value = []
    orders.value = []
    messages.value = []
    addresses.value = []
    coupons.value = []
    browseHistory.value = []
    reviews.value = []
  }

  // 更新用户信息
  const updateUserInfo = (data) => {
    if (userInfo.value) {
      userInfo.value = { ...userInfo.value, ...data }
    }
  }

  // 添加到购物车
  const addToCart = (product) => {
    const existingItem = cart.value.find(item => item.id === product.id)
    if (existingItem) {
      existingItem.quantity += 1
    } else {
      cart.value.push({
        id: product.id,
        name: product.name,
        price: product.price,
        image: product.image,
        quantity: 1
      })
    }
  }

  // 从购物车移除
  const removeFromCart = (productId) => {
    const index = cart.value.findIndex(item => item.id === productId)
    if (index > -1) {
      cart.value.splice(index, 1)
    }
  }

  // 清空购物车
  const clearCart = () => {
    cart.value = []
  }

  // 添加订单
  const addOrder = (order) => {
    orders.value.unshift(order)
  }

  // 添加消息
  const addMessage = (message) => {
    messages.value.unshift(message)
  }

  // 添加地址
  const addAddress = (address) => {
    addresses.value.push(address)
  }

  // 删除地址
  const removeAddress = (addressId) => {
    const index = addresses.value.findIndex(a => a.id === addressId)
    if (index > -1) {
      addresses.value.splice(index, 1)
    }
  }

  // 添加浏览历史
  const addBrowseHistory = (product) => {
    const index = browseHistory.value.findIndex(p => p.id === product.id)
    if (index > -1) {
      browseHistory.value.splice(index, 1)
    }
    browseHistory.value.unshift({
      id: product.id,
      name: product.name,
      price: product.price,
      image: product.image,
      browseTime: new Date().toISOString()
    })
    if (browseHistory.value.length > 50) {
      browseHistory.value.pop()
    }
  }

  // 添加评价
  const addReview = (review) => {
    reviews.value.unshift(review)
  }

  // 删除评价
  const removeReview = (reviewId) => {
    const index = reviews.value.findIndex(r => r.id === reviewId)
    if (index > -1) {
      reviews.value.splice(index, 1)
    }
  }

  // 计算属性
  const cartCount = computed(() => {
    return cart.value.reduce((sum, item) => sum + item.quantity, 0)
  })

  const cartTotal = computed(() => {
    return cart.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
  })

  const unreadMessageCount = computed(() => {
    return messages.value.filter(m => !m.read).length
  })

  // 检查是否是管理员
  const isAdmin = computed(() => {
    const role = userInfo.value?.role
    return role === 'admin' || role === 4
  })

  // 检查是否是商家
  const isShop = computed(() => {
    const role = userInfo.value?.role
    return role === 'shop' || role === 2
  })

  // 检查是否是收购商
  const isAcquirer = computed(() => {
    const role = userInfo.value?.role
    return role === 'acquirer' || (role === 2 && userInfo.value?.shopType === 2)
  })

  return {
    userInfo,
    isLoggedIn,
    cart,
    orders,
    messages,
    addresses,
    coupons,
    browseHistory,
    reviews,
    cartCount,
    cartTotal,
    unreadMessageCount,
    isAdmin,
    isShop,
    isAcquirer,
    register,
    login,
    logout,
    updateUserInfo,
    addToCart,
    removeFromCart,
    clearCart,
    addOrder,
    addMessage,
    addAddress,
    removeAddress,
    addBrowseHistory,
    addReview,
    removeReview,
    saveUserData,
    loadUserData
  }
})
