import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'

// 路由白名单 - 不需要登录就能访问
const whiteList = ['/login', '/register']

// 获取当前登录状态
const getLoginState = () => {
  try {
    const token = localStorage.getItem('token')
    const userData = localStorage.getItem('moli_mall_current_user')
    if (!token || !userData) {
      return { isLoggedIn: false, role: null }
    }
    const parsed = JSON.parse(userData)
    return {
      isLoggedIn: !!parsed?.userInfo,
      role: parsed?.userInfo?.role || parsed?.userInfo?.roleNum,
      shopType: parsed?.userInfo?.shopType,
      userId: parsed?.userInfo?.id
    }
  } catch (e) {
    return { isLoggedIn: false, role: null }
  }
}

// 获取用户角色
const getUserRole = () => {
  const state = getLoginState()
  if (!state.isLoggedIn) return null

  // 优先使用后端返回的 role 字符串
  const userDataStr = localStorage.getItem('moli_mall_current_user')
  if (userDataStr) {
    try {
      const parsed = JSON.parse(userDataStr)
      const role = parsed?.userInfo?.role
      // 如果 role 是字符串（后端返回的完整角色）
      if (role === 'admin') return 'admin'
      if (role === 'acquirer') return 'acquirer'
      if (role === 'shop') return 'shop'
      if (role === 'user') return 'user'
      if (role === 'expert') return 'expert'
    } catch (e) {}
  }

  // 兼容数字角色
  let role = state.role
  if (role === 1 || role === 'user') return 'user'
  if (role === 2 || role === 'shop') {
    // 商家需要检查是否是收购商
    if (state.shopType === 2) return 'acquirer'
    return 'shop'
  }
  if (role === 3 || role === 'expert') return 'expert'
  if (role === 4 || role === 'admin') return 'admin'

  return 'user'
}

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue'),
    meta: { title: '首页' }
  },
  {
    path: '/price-detail',
    name: 'PriceDetail',
    component: () => import('../views/PriceDetail.vue'),
    meta: { title: '价格详情' }
  },
  {
    path: '/weather',
    name: 'WeatherDetail',
    component: () => import('../views/WeatherDetail.vue'),
    meta: { title: '天气详情' }
  },
  {
    path: '/products',
    name: 'ProductList',
    component: () => import('../views/ProductList.vue'),
    meta: { title: '商品列表' }
  },
  {
    path: '/product/:id',
    name: 'ProductDetail',
    component: () => import('../views/ProductDetail.vue'),
    meta: { title: '商品详情' }
  },
  {
    path: '/cart',
    name: 'Cart',
    component: () => import('../views/Cart.vue'),
    meta: { title: '购物车', requiresAuth: true, roles: ['user'] }
  },
  {
    path: '/messages',
    name: 'MessageCenter',
    component: () => import('../views/MessageCenter.vue'),
    meta: { title: '消息中心', requiresAuth: true }
  },
  {
    path: '/chat/:id',
    name: 'Chat',
    component: () => import('../views/Chat.vue'),
    meta: { title: '聊天详情', requiresAuth: true }
  },
  {
    path: '/chat/broadcast/:msgId',
    name: 'ChatBroadcast',
    component: () => import('../views/ChatBroadcast.vue'),
    meta: { title: '广播消息', requiresAuth: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('../views/Profile.vue'),
    meta: { title: '个人中心', requiresAuth: true, roles: ['user'] }
  },
  {
    path: '/address',
    name: 'AddressManage',
    component: () => import('../views/AddressManage.vue'),
    meta: { title: '地址管理', requiresAuth: true, roles: ['user'] }
  },
  {
    path: '/orders',
    name: 'OrderList',
    component: () => import('../views/OrderList.vue'),
    meta: { title: '我的订单', requiresAuth: true, roles: ['user'] }
  },
  {
    path: '/order/:id',
    name: 'OrderDetail',
    component: () => import('../views/OrderDetail.vue'),
    meta: { title: '订单详情', requiresAuth: true }
  },
  {
    path: '/refund-form/:orderId',
    name: 'RefundForm',
    component: () => import('../views/RefundForm.vue'),
    meta: { title: '申请退换货', requiresAuth: true, roles: ['user'] }
  },
  {
    path: '/history',
    name: 'BrowseHistory',
    component: () => import('../views/BrowseHistory.vue'),
    meta: { title: '浏览历史', requiresAuth: true, roles: ['user'] }
  },
  {
    path: '/reviews',
    name: 'MyReviews',
    component: () => import('../views/MyReviews.vue'),
    meta: { title: '我的评价', requiresAuth: true, roles: ['user'] }
  },
  {
    path: '/after-sales',
    name: 'AfterSales',
    component: () => import('../views/AfterSales.vue'),
    meta: { title: '售后记录', requiresAuth: true, roles: ['user'] }
  },
  // 茉莉花收购专栏（用户端）
  {
    path: '/acquisition',
    name: 'AcquisitionList',
    component: () => import('../views/AcquisitionList.vue'),
    meta: { title: '收购专栏' }
  },
  {
    path: '/acquisition/detail/:id',
    name: 'AcquisitionDetail',
    component: () => import('../views/AcquisitionDetail.vue'),
    meta: { title: '收购需求详情', requiresAuth: true, roles: ['user'] }
  },
  {
    path: '/acquisition/my-records',
    name: 'AcquisitionRecords',
    component: () => import('../views/AcquisitionRecords.vue'),
    meta: { title: '我的收购记录', requiresAuth: true, roles: ['user'] }
  },
  // 商家后台
  {
    path: '/shop',
    name: 'ShopAdmin',
    component: () => import('../views/shop/ShopLayout.vue'),
    meta: { title: '商家后台', requiresAuth: true, roles: ['shop'] },
    children: [
      {
        path: '',
        name: 'ShopDashboard',
        component: () => import('../views/shop/ShopDashboard.vue'),
        meta: { title: '仪表盘' }
      },
      {
        path: 'products',
        name: 'ShopProducts',
        component: () => import('../views/shop/ShopProducts.vue'),
        meta: { title: '商品管理' }
      },
      {
        path: 'product/add',
        name: 'ShopProductAdd',
        component: () => import('../views/shop/ShopProductAdd.vue'),
        meta: { title: '发布商品' }
      },
      {
        path: 'product/edit/:id',
        name: 'ShopProductEdit',
        component: () => import('../views/shop/ShopProductEdit.vue'),
        meta: { title: '商品编辑' }
      },
      {
        path: 'orders',
        name: 'ShopOrders',
        component: () => import('../views/shop/ShopOrders.vue'),
        meta: { title: '订单管理' }
      },
      {
        path: 'refunds',
        name: 'ShopRefunds',
        component: () => import('../views/shop/ShopRefunds.vue'),
        meta: { title: '售后管理' }
      },
      {
        path: 'messages',
        name: 'ShopMessages',
        component: () => import('../views/shop/ShopMessages.vue'),
        meta: { title: '消息中心' }
      },
      {
        path: 'reviews',
        name: 'ShopReviews',
        component: () => import('../views/shop/ShopReviews.vue'),
        meta: { title: '评价管理' }
      },
      {
        path: 'qualification',
        name: 'ShopQualification',
        component: () => import('../views/shop/ShopQualification.vue'),
        meta: { title: '资质申请' }
      },
      {
        path: 'price-alert',
        name: 'ShopPriceAlert',
        component: () => import('../views/shop/ShopPriceAlert.vue'),
        meta: { title: '价格预警' }
      },
      {
        path: 'price',
        name: 'ShopPrice',
        component: () => import('../views/shop/ShopPrice.vue'),
        meta: { title: '价格监测' }
      },
      {
        path: 'acquisition',
        name: 'ShopAcquisition',
        component: () => import('../views/shop/ShopAcquisition.vue'),
        meta: { title: '收购需求管理' }
      }
    ]
  },
  // 收购商后台
  {
    path: '/acquirer',
    name: 'AcquirerLayout',
    component: () => import('../views/acquirer/AcquirerLayout.vue'),
    meta: { title: '收购商后台', requiresAuth: true, roles: ['acquirer'] },
    children: [
      {
        path: '',
        name: 'AcquirerDashboard',
        component: () => import('../views/acquirer/AcquirerDashboard.vue'),
        meta: { title: '数据统计' }
      },
      {
        path: 'acquisition',
        name: 'AcquirerAcquisition',
        component: () => import('../views/acquirer/AcquirerAcquisition.vue'),
        meta: { title: '收购需求管理' }
      },
      {
        path: 'orders',
        name: 'AcquirerOrders',
        component: () => import('../views/acquirer/AcquirerOrders.vue'),
        meta: { title: '收购订单' }
      },
      {
        path: 'messages',
        name: 'AcquirerMessages',
        component: () => import('../views/acquirer/AcquirerMessages.vue'),
        meta: { title: '消息中心' }
      }
    ]
  },
  // 管理员后台
  {
    path: '/admin',
    name: 'AdminLayout',
    component: () => import('../views/admin/AdminLayout.vue'),
    meta: { title: '管理员后台', requiresAuth: true, roles: ['admin'] },
    children: [
      {
        path: '',
        name: 'AdminDashboard',
        component: () => import('../views/admin/AdminDashboard.vue'),
        meta: { title: '系统概览' }
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('../views/admin/AdminUsers.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'shops',
        name: 'AdminShops',
        component: () => import('../views/admin/AdminShops.vue'),
        meta: { title: '商家审核' }
      },
      {
        path: 'qualifications',
        name: 'AdminQualifications',
        component: () => import('../views/admin/AdminQualifications.vue'),
        meta: { title: '资质核验' }
      },
      {
        path: 'products',
        name: 'AdminProducts',
        component: () => import('../views/admin/AdminProducts.vue'),
        meta: { title: '商品审核' }
      },
      {
        path: 'prices',
        name: 'AdminPrices',
        component: () => import('../views/admin/AdminPrices.vue'),
        meta: { title: '价格监测' }
      },
      {
        path: 'complaints',
        name: 'AdminComplaints',
        component: () => import('../views/admin/AdminComplaints.vue'),
        meta: { title: '交易投诉' }
      },
      {
        path: 'messages',
        name: 'AdminMessages',
        component: () => import('../views/admin/AdminMessages.vue'),
        meta: { title: '系统消息' }
      },
      {
        path: 'service',
        name: 'AdminCustomerService',
        component: () => import('../views/admin/AdminCustomerService.vue'),
        meta: { title: '客服中心' }
      },
      {
        path: 'acquisition',
        name: 'AdminAcquisition',
        component: () => import('../views/admin/AdminAcquisition.vue'),
        meta: { title: '收购管理' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = `${to.meta.title || '横州茉莉花商城'} - 茉莉花香，品质之选`

  // 白名单页面直接放行
  if (whiteList.includes(to.path)) {
    // 已登录用户访问登录页，根据角色跳转
    const state = getLoginState()
    if (state.isLoggedIn) {
      const role = getUserRole()
      if (role === 'admin') {
        next('/admin')
        return
      } else if (role === 'acquirer') {
        next('/acquirer')
        return
      } else if (role === 'shop') {
        next('/shop')
        return
      } else {
        next('/')
        return
      }
    }
    next()
    return
  }

  // 检查登录状态
  const state = getLoginState()
  const userRole = getUserRole()

  // 未登录：重定向到登录页
  if (!state.isLoggedIn) {
    next({ 
      path: '/login', 
      query: { redirect: to.fullPath } 
    })
    return
  }

  // 检查角色权限
  if (to.meta.roles && to.meta.roles.length > 0) {
    if (!to.meta.roles.includes(userRole)) {
      // 角色不匹配，跳转到对应角色首页
      ElMessage.warning('您没有权限访问该页面')
      if (userRole === 'admin') {
        next('/admin')
      } else if (userRole === 'acquirer') {
        next('/acquirer')
      } else if (userRole === 'shop') {
        next('/shop')
      } else {
        next('/')
      }
      return
    }
  }

  // 已登录用户访问首页，根据角色自动跳转
  if (state.isLoggedIn && to.path === '/') {
    if (userRole === 'admin') {
      next('/admin')
      return
    } else if (userRole === 'acquirer') {
      next('/acquirer')
      return
    } else if (userRole === 'shop') {
      next('/shop')
      return
    }
  }

  next()
})

export default router
