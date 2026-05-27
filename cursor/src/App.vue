<template>
  <div class="app-container">
    <!-- 顶部导航栏 - 登录和注册页面隐藏 -->
    <header class="app-header" v-if="!isAuthPage">
      <div class="header-content">
        <!-- Logo -->
        <div class="logo" @click="$router.push('/')">
          <span class="logo-icon">🌸</span>
          <span class="logo-text">横州茉莉花</span>
        </div>

        <!-- 搜索框 - PC和平板显示 -->
        <div class="search-box">
          <input
            type="text"
            class="neumorphic-input search-input"
            placeholder="搜索茉莉花商品..."
            v-model="searchKeyword"
            @keyup.enter="handleSearch"
          />
          <button class="neumorphic-btn search-btn" @click="handleSearch">
            <el-icon><Search /></el-icon>
          </button>
        </div>

        <!-- 导航图标 - PC和平板显示 -->
        <div class="header-actions">
          <!-- 天气组件 -->
          <WeatherWidget class="hidden-xs-only" />

          <!-- 登录/登出 -->
          <div class="action-item" v-if="!isLoggedIn" @click="$router.push('/login')">
            <span class="action-icon">🔑</span>
            <span class="action-label">登录</span>
          </div>
          <div class="action-item" v-else @click="handleLogout">
            <span class="action-icon">🚪</span>
            <span class="action-label">登出</span>
          </div>

          <!-- 购物车 -->
          <div class="action-item" @click="$router.push('/cart')">
            <el-badge :value="cartCount" :hidden="cartCount === 0" class="badge-item">
              <span class="action-icon">🛒</span>
            </el-badge>
            <span class="action-label">购物车</span>
          </div>

          <!-- 消息中心 -->
          <div class="action-item" @click="$router.push('/messages')">
            <el-badge :value="unreadMessageCount" :hidden="unreadMessageCount === 0" class="badge-item">
              <span class="action-icon">💬</span>
            </el-badge>
            <span class="action-label">消息</span>
          </div>

          <!-- 个人中心 -->
          <div class="action-item" @click="$router.push('/profile')">
            <span class="action-icon">👤</span>
            <span class="action-label">我的</span>
          </div>
        </div>

        <!-- 移动端汉堡菜单按钮 -->
        <button class="mobile-menu-btn" @click="toggleMobileMenu" v-if="isLoggedIn">
          <span></span>
          <span></span>
          <span></span>
        </button>
      </div>

      <!-- 移动端搜索框 -->
      <div class="mobile-search-box" v-if="!isAuthPage">
        <input
          type="text"
          class="search-input-mobile"
          placeholder="搜索商品..."
          v-model="searchKeyword"
          @keyup.enter="handleSearch"
        />
        <button class="search-btn-mobile" @click="handleSearch">
          <el-icon><Search /></el-icon>
        </button>
      </div>
    </header>

    <!-- 移动端侧边导航菜单 -->
    <div class="mobile-overlay" :class="{ active: mobileMenuOpen }" @click="closeMobileMenu"></div>
    <nav class="mobile-nav" :class="{ active: mobileMenuOpen }">
      <div class="mobile-nav-header">
        <div class="mobile-nav-user" v-if="isLoggedIn">
          <span class="user-avatar">👤</span>
          <span class="user-name">{{ userStore.userInfo?.username || '用户' }}</span>
        </div>
        <button class="mobile-nav-close" @click="closeMobileMenu">
          <el-icon><Close /></el-icon>
        </button>
      </div>
      <div class="mobile-nav-list">
        <div class="mobile-nav-item" @click="goTo('/'); closeMobileMenu()">
          <span class="nav-icon">🏠</span>
          <span>首页</span>
        </div>
        <div class="mobile-nav-item" @click="goTo('/products'); closeMobileMenu()">
          <span class="nav-icon">🛍️</span>
          <span>商品列表</span>
        </div>
        <div class="mobile-nav-item" @click="goTo('/acquisitions'); closeMobileMenu()">
          <span class="nav-icon">📋</span>
          <span>收购专栏</span>
        </div>
        <div class="mobile-nav-item" @click="goTo('/cart'); closeMobileMenu()">
          <span class="nav-icon">🛒</span>
          <span>购物车</span>
          <span class="nav-badge" v-if="cartCount > 0">{{ cartCount }}</span>
        </div>
        <div class="mobile-nav-item" @click="goTo('/messages'); closeMobileMenu()">
          <span class="nav-icon">💬</span>
          <span>消息</span>
          <span class="nav-badge" v-if="unreadMessageCount > 0">{{ unreadMessageCount }}</span>
        </div>
        <div class="mobile-nav-divider"></div>
        <div class="mobile-nav-item" @click="goTo('/profile'); closeMobileMenu()">
          <span class="nav-icon">👤</span>
          <span>个人中心</span>
        </div>
        <div class="mobile-nav-item" @click="goTo('/orders'); closeMobileMenu()">
          <span class="nav-icon">📦</span>
          <span>我的订单</span>
        </div>
        <div class="mobile-nav-item" @click="goTo('/addresses'); closeMobileMenu()">
          <span class="nav-icon">📍</span>
          <span>收货地址</span>
        </div>
        <div class="mobile-nav-item" @click="goTo('/acquisition-records'); closeMobileMenu()">
          <span class="nav-icon">📝</span>
          <span>供货记录</span>
        </div>
        <div class="mobile-nav-divider"></div>
        <div class="mobile-nav-item" @click="goTo('/chat'); closeMobileMenu()">
          <span class="nav-icon">🤖</span>
          <span>AI助手</span>
        </div>
        <div class="mobile-nav-item" @click="goTo('/weather'); closeMobileMenu()">
          <span class="nav-icon">🌤️</span>
          <span>天气查询</span>
        </div>
        <div class="mobile-nav-item" @click="goTo('/prices'); closeMobileMenu()">
          <span class="nav-icon">📊</span>
          <span>价格监测</span>
        </div>
        <div class="mobile-nav-divider"></div>
        <div class="mobile-nav-item" @click="handleLogout" v-if="isLoggedIn">
          <span class="nav-icon">🚪</span>
          <span>退出登录</span>
        </div>
        <div class="mobile-nav-item" @click="goTo('/login'); closeMobileMenu()" v-if="!isLoggedIn">
          <span class="nav-icon">🔑</span>
          <span>登录</span>
        </div>
      </div>
    </nav>

    <!-- 主体内容 -->
    <main class="app-main">
      <router-view v-slot="{ Component, route }">
        <component
          :is="Component"
          :key="route.fullPath"
          @error="handleChildError"
        />
      </router-view>
    </main>

    <!-- 底部信息 - 登录和注册页面隐藏 -->
    <footer class="app-footer" v-if="!isAuthPage">
      <div class="footer-content">
        <p>© 2026 横州茉莉花商城 - 传承茉莉花香 · 品质保障</p>
      </div>
    </footer>

    <!-- 全局AI助手 - 仅用户端显示 -->
    <AIAssistant v-if="!isAuthPage" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useCartStore } from './stores/cart'
import { useMessageStore } from './stores/message'
import { useUserStore } from './stores/user'
import { Search, Close } from '@element-plus/icons-vue'
import WeatherWidget from './components/WeatherWidget.vue'
import AIAssistant from './components/AIAssistant.vue'

const router = useRouter()
const route = useRoute()
const cartStore = useCartStore()
const messageStore = useMessageStore()
const userStore = useUserStore()

const searchKeyword = ref('')
const initialized = ref(false)
const mobileMenuOpen = ref(false)

// 判断是否为登录/注册页面或后台管理页面
const isAuthPage = computed(() => {
  const path = route.path
  return path === '/login' || path === '/register' ||
         path.startsWith('/admin') || path.startsWith('/shop') || path.startsWith('/acquirer')
})

// 登录状态
const isLoggedIn = computed(() => userStore.isLoggedIn)

// 购物车数量
const cartCount = computed(() => userStore.cartCount)

// 未读消息数量
const unreadMessageCount = computed(() => messageStore.unreadCount)

// 初始化检查：根据用户角色重定向
const checkInitialRoute = () => {
  const whiteList = ['/login', '/register']
  if (whiteList.includes(route.path)) {
    initialized.value = true
    return
  }

  const storedState = getStoredLoginState()
  if (storedState.isLoggedIn) {
    if (route.path === '/' && storedState.role === 'shop') {
      router.replace('/shop')
      return
    }
    if (route.path === '/' && storedState.role === 'acquirer') {
      router.replace('/acquirer')
      return
    }
    if (route.path === '/' && storedState.role === 'admin') {
      router.replace('/admin')
      return
    }
  }

  initialized.value = true
}

const getStoredLoginState = () => {
  try {
    const data = localStorage.getItem('moli_mall_current_user')
    if (data) {
      const parsed = JSON.parse(data)
      return {
        isLoggedIn: !!parsed?.userInfo,
        role: parsed?.userInfo?.role
      }
    }
  } catch (e) {
    console.error('读取登录状态失败', e)
  }
  return { isLoggedIn: false, role: null }
}

// 搜索处理
const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push({ path: '/products', query: { search: searchKeyword.value } })
  }
}

// 登出处理
const handleLogout = () => {
  userStore.logout()
  mobileMenuOpen.value = false
  router.push('/login')
}

// 路由跳转
const goTo = (path) => {
  router.push(path)
}

// 切换移动端菜单
const toggleMobileMenu = () => {
  mobileMenuOpen.value = !mobileMenuOpen.value
  if (mobileMenuOpen.value) {
    document.body.style.overflow = 'hidden'
  } else {
    document.body.style.overflow = ''
  }
}

// 关闭移动端菜单
const closeMobileMenu = () => {
  mobileMenuOpen.value = false
  document.body.style.overflow = ''
}

// 子组件渲染错误兜底
const handleChildError = (err) => {
  console.error('子组件渲染错误:', err)
}

// 组件挂载时检查初始路由
onMounted(() => {
  checkInitialRoute()
})

// 监听路由变化，重新检查
watch(() => route.path, () => {
  if (initialized.value) {
    checkInitialRoute()
  }
  closeMobileMenu()
})

// 组件卸载时关闭菜单
onUnmounted(() => {
  document.body.style.overflow = ''
})
</script>

<style scoped>
.app-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #e3e5e7;
}

/* ========== 顶部导航 ========== */
.app-header {
  position: sticky;
  top: 0;
  z-index: 1000;
  background: #e3e5e7;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

/* Logo */
.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  transition: transform 0.3s ease;
  flex-shrink: 0;
}

.logo:hover {
  transform: scale(1.05);
}

.logo-icon {
  font-size: 28px;
}

.logo-text {
  font-size: 18px;
  font-weight: bold;
  color: #4A7C59;
}

/* 搜索框 */
.search-box {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
  max-width: 450px;
  margin: 0 30px;
}

.search-input {
  flex: 1;
  height: 40px;
  font-size: 14px;
  border: none;
  border-radius: 20px;
  background: #fff;
  box-shadow: inset 2px 2px 6px #b6b9ba, inset -2px -2px 6px #fafafd;
}

.search-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

/* 导航图标 */
.header-actions {
  display: flex;
  align-items: center;
  gap: 20px;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  transition: transform 0.3s ease;
  padding: 5px 8px;
  border-radius: 8px;
}

.action-item:hover {
  transform: translateY(-2px);
  background: rgba(74, 124, 89, 0.1);
}

.action-icon {
  font-size: 22px;
}

.action-label {
  font-size: 11px;
  color: #666;
  margin-top: 4px;
}

.badge-item :deep(.el-badge__content) {
  background: #e74c3c;
}

/* 移动端汉堡菜单按钮 */
.mobile-menu-btn {
  display: none;
  width: 40px;
  height: 40px;
  border: none;
  background: transparent;
  cursor: pointer;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 5px;
  padding: 8px;
}

.mobile-menu-btn span {
  display: block;
  width: 22px;
  height: 2px;
  background: #333;
  border-radius: 2px;
  transition: all 0.3s ease;
}

/* 移动端搜索框 */
.mobile-search-box {
  display: none;
  padding: 10px 15px;
  background: #fff;
  border-top: 1px solid #eee;
}

.search-input-mobile {
  flex: 1;
  height: 38px;
  padding: 0 15px;
  border: 1px solid #ddd;
  border-radius: 19px;
  font-size: 14px;
  outline: none;
}

.search-btn-mobile {
  width: 38px;
  height: 38px;
  border: none;
  background: #4A7C59;
  color: #fff;
  border-radius: 50%;
  cursor: pointer;
  margin-left: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* ========== 移动端侧边导航 ========== */
.mobile-overlay {
  display: none;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1999;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.mobile-overlay.active {
  opacity: 1;
}

.mobile-nav {
  position: fixed;
  top: 0;
  left: -280px;
  width: 280px;
  height: 100vh;
  background: #fff;
  z-index: 2000;
  transition: left 0.3s ease;
  overflow-y: auto;
  box-shadow: 4px 0 20px rgba(0, 0, 0, 0.15);
}

.mobile-nav.active {
  left: 0;
}

.mobile-nav-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 15px;
  background: linear-gradient(135deg, #4A7C59, #5a9469);
  color: #fff;
}

.mobile-nav-user {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  font-size: 32px;
}

.user-name {
  font-size: 16px;
  font-weight: 500;
}

.mobile-nav-close {
  width: 32px;
  height: 32px;
  border: none;
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.mobile-nav-list {
  padding: 10px 0;
}

.mobile-nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 18px;
  color: #333;
  cursor: pointer;
  transition: background 0.2s ease;
  position: relative;
}

.mobile-nav-item:hover {
  background: #f5f5f5;
}

.mobile-nav-item:active {
  background: #eee;
}

.nav-icon {
  font-size: 20px;
  width: 24px;
  text-align: center;
}

.mobile-nav-divider {
  height: 1px;
  background: #eee;
  margin: 8px 15px;
}

.nav-badge {
  position: absolute;
  right: 15px;
  background: #e74c3c;
  color: #fff;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 10px;
  font-weight: bold;
}

/* ========== 主体内容 ========== */
.app-main {
  flex: 1;
  max-width: 1400px;
  width: 100%;
  margin: 0 auto;
  padding: 20px;
}

/* ========== 底部 ========== */
.app-footer {
  background: #e3e5e7;
  padding: 20px;
  text-align: center;
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.05);
}

.footer-content p {
  color: #666;
  font-size: 13px;
}

/* ========== 响应式适配 ========== */
@media (max-width: 992px) {
  .header-content {
    padding: 0 15px;
  }
  
  .logo-text {
    font-size: 16px;
  }
  
  .search-box {
    max-width: 300px;
    margin: 0 20px;
  }
  
  .header-actions {
    gap: 12px;
  }
  
  .action-label {
    display: none;
  }
  
  .action-item {
    padding: 5px;
  }
}

@media (max-width: 768px) {
  .search-box,
  .header-actions {
    display: none;
  }
  
  .mobile-menu-btn {
    display: flex;
  }
  
  .mobile-search-box {
    display: flex;
  }
  
  .app-main {
    padding: 15px 10px;
  }
  
  .footer-content p {
    font-size: 12px;
  }
}

@media (max-width: 480px) {
  .header-content {
    height: 55px;
  }
  
  .logo-icon {
    font-size: 24px;
  }
  
  .logo-text {
    font-size: 15px;
  }
}
</style>
