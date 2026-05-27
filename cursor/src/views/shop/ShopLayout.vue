<template>
  <div class="shop-layout">
    <!-- 移动端顶部栏 -->
    <header class="shop-header-mobile">
      <button class="menu-toggle" @click="toggleSidebar">
        <span></span>
        <span></span>
        <span></span>
      </button>
      <div class="mobile-title">{{ pageTitle }}</div>
      <div class="mobile-actions">
        <el-button type="danger" size="small" @click="handleLogout">退出</el-button>
      </div>
    </header>

    <!-- 侧边栏 -->
    <aside class="shop-sidebar">
      <div class="shop-logo">
        <span class="logo-icon">{{ isAcquirerType ? '🌺' : '🌸' }}</span>
        <span class="logo-text">{{ isAcquirerType ? '收购商中心' : '商家中心' }}</span>
      </div>

      <nav class="shop-nav">
        <!-- 收购商菜单（无需资质审核） -->
        <template v-if="isAcquirerType">
          <router-link to="/shop" class="nav-item" :class="{ active: $route.path === '/shop' }" @click="closeSidebar">
            <span class="nav-icon">📊</span>
            <span class="nav-text">数据统计</span>
          </router-link>
          <router-link to="/shop/acquisition" class="nav-item" :class="{ active: $route.path.startsWith('/shop/acquisition') }" @click="closeSidebar">
            <span class="nav-icon">🌸</span>
            <span class="nav-text">收购需求管理</span>
          </router-link>
          <router-link to="/shop/orders" class="nav-item" :class="{ active: $route.path.startsWith('/shop/orders') }" @click="closeSidebar">
            <span class="nav-icon">🛒</span>
            <span class="nav-text">订单管理</span>
          </router-link>
          <router-link to="/shop/messages" class="nav-item" :class="{ active: $route.path.startsWith('/shop/messages') }" @click="closeSidebar">
            <span class="nav-icon">💬</span>
            <span class="nav-text">消息中心</span>
          </router-link>
        </template>

        <!-- 商家端：资质未通过时显示资质提示，不显示其他菜单 -->
        <template v-if="!isAcquirerType && !isShopQualified">
          <div class="qualification-notice">
            <div class="notice-icon">
              <span v-if="qualificationStatus === 1">⏳</span>
              <span v-else-if="qualificationStatus === 3">❌</span>
              <span v-else>📋</span>
            </div>
            <div class="notice-content">
              <h4 v-if="qualificationStatus === 1">资质审核中</h4>
              <h4 v-else-if="qualificationStatus === 3">资质审核未通过</h4>
              <h4 v-else>请先提交资质材料</h4>
              <p v-if="qualificationStatus === 1">您的材料正在审核，请耐心等待</p>
              <p v-else-if="qualificationStatus === 3">请重新上传资质材料</p>
              <p v-else>完成认证后解锁全部功能</p>
            </div>
            <router-link to="/shop/qualification" class="notice-btn">
              {{ qualificationStatus === 1 ? '查看详情' : '去认证' }}
            </router-link>
          </div>
        </template>

        <!-- 商家端：资质审核通过后显示全部功能 -->
        <template v-if="!isAcquirerType && isShopQualified">
          <router-link to="/shop" class="nav-item" :class="{ active: $route.path === '/shop' }">
            <span class="nav-icon">📊</span>
            <span class="nav-text">仪表盘</span>
          </router-link>
          <router-link to="/shop/products" class="nav-item" :class="{ active: $route.path.startsWith('/shop/products') }">
            <span class="nav-icon">📦</span>
            <span class="nav-text">商品管理</span>
          </router-link>
          <router-link to="/shop/orders" class="nav-item" :class="{ active: $route.path.startsWith('/shop/orders') }">
            <span class="nav-icon">🛒</span>
            <span class="nav-text">订单管理</span>
          </router-link>
          <router-link to="/shop/refunds" class="nav-item" :class="{ active: $route.path.startsWith('/shop/refunds') }">
            <span class="nav-icon">🔧</span>
            <span class="nav-text">售后管理</span>
          </router-link>
          <router-link to="/shop/messages" class="nav-item" :class="{ active: $route.path.startsWith('/shop/messages') }">
            <span class="nav-icon">💬</span>
            <span class="nav-text">消息中心</span>
          </router-link>
          <router-link to="/shop/reviews" class="nav-item" :class="{ active: $route.path.startsWith('/shop/reviews') }">
            <span class="nav-icon">⭐</span>
            <span class="nav-text">评价管理</span>
          </router-link>
          <router-link to="/shop/qualification" class="nav-item" :class="{ active: $route.path.startsWith('/shop/qualification') }">
            <span class="nav-icon">📋</span>
            <span class="nav-text">资质查看</span>
          </router-link>
        </template>
      </nav>
    </aside>

    <!-- 遮罩层 -->
    <div class="sidebar-overlay" :class="{ active: sidebarOpen }" @click="closeSidebar"></div>

    <!-- 右侧内容 -->
    <div class="shop-main">
      <header class="shop-header">
        <div class="header-left">
          <h2>{{ pageTitle }}</h2>
        </div>
        <div class="header-right">
          <div class="shop-info">
            <img :src="normalizeUploadUrl(userInfo?.avatar) || defaultAvatar" alt="avatar" class="shop-avatar" />
            <span class="shop-name">{{ userInfo?.username || (isAcquirerType ? '收购商' : '商家') }}</span>
          </div>
          <el-button type="danger" size="small" @click="handleLogout">退出登录</el-button>
        </div>
      </header>

      <main class="shop-content">
        <!-- 商家资质未通过时：只显示资质申请页面 -->
        <div v-if="!isAcquirerType && !isShopQualified && !$route.path.startsWith('/shop/qualification')" class="qualification-redirect">
          <div class="redirect-content">
            <div class="redirect-icon">🔒</div>
            <h3>需要完成资质认证</h3>
            <p v-if="qualificationStatus === 1">您的资质材料正在审核中，请耐心等待...</p>
            <p v-else-if="qualificationStatus === 3">您的资质审核未通过，请重新上传</p>
            <p v-else>请先提交资质材料以解锁商家功能</p>
            <el-button type="primary" @click="$router.push('/shop/qualification')">
              {{ qualificationStatus === 1 ? '查看详情' : '立即认证' }}
            </el-button>
          </div>
        </div>

        <router-view v-else v-slot="{ Component }">
          <component :is="Component" />
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { normalizeUploadUrl } from '@/utils/imageUrl'

const router = useRouter()
const route = useRoute()
const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'

const userInfo = ref(null)
const qualificationStatus = ref(null)
const sidebarOpen = ref(false)

const toggleSidebar = () => {
  sidebarOpen.value = !sidebarOpen.value
  if (sidebarOpen.value) {
    document.body.style.overflow = 'hidden'
  } else {
    document.body.style.overflow = ''
  }
}

const closeSidebar = () => {
  sidebarOpen.value = false
  document.body.style.overflow = ''
}

// 监听用户信息变化
const loadUserInfo = () => {
  try {
    const data = JSON.parse(localStorage.getItem('moli_mall_current_user') || '{}')
    userInfo.value = data.userInfo
    if (userInfo.value?.shopQualificationStatus !== undefined) {
      qualificationStatus.value = userInfo.value.shopQualificationStatus
    }
  } catch { userInfo.value = null }
}

onMounted(() => {
  loadUserInfo()
})

watch(() => localStorage.getItem('moli_mall_current_user'), () => {
  loadUserInfo()
  closeSidebar()
}, { deep: true })

// 路由变化时关闭侧边栏
watch(() => route.path, () => {
  closeSidebar()
})

// 判断是否为收购商
const isAcquirerType = computed(() => {
  return userInfo.value?.role === 5 || userInfo.value?.role === 'acquirer' ||
         (userInfo.value?.role === 2 && userInfo.value?.shopType === 2)
})

// 判断商家资质是否已通过
const isShopQualified = computed(() => {
  // 收购商不需要资质
  if (isAcquirerType.value) return true
  return userInfo.value?.shopQualificationStatus === 2
})

const pageTitle = computed(() => {
  return route.meta?.title || (isAcquirerType.value ? '收购商中心' : '商家中心')
})

const handleLogout = () => {
  localStorage.removeItem('moli_mall_current_user')
  localStorage.removeItem('token')
  router.push('/login')
}
</script>

<style scoped>
/* ================================================
   商家后台布局 - 统一设计
   ================================================ */

.shop-layout { 
  display: flex; 
  min-height: 100vh; 
  width: 100%; 
  background: #f0f2f5;
}

/* ========== 侧边栏 ========== */
.shop-sidebar { 
  width: 220px; 
  min-width: 220px; 
  flex-shrink: 0; 
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%);
  color: white; 
  display: flex; 
  flex-direction: column; 
  position: fixed;
  left: 0;
  top: 0;
  height: 100vh;
  box-shadow: 2px 0 10px rgba(0, 0, 0, 0.1);
}

.shop-logo { 
  height: 64px;
  padding: 0 20px; 
  display: flex; 
  align-items: center; 
  gap: 12px; 
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(255, 255, 255, 0.02);
}

.logo-icon { font-size: 24px; }

.logo-text { 
  font-size: 16px; 
  font-weight: 600; 
  color: white;
  letter-spacing: 1px;
}

.shop-nav { 
  flex: 1; 
  padding: 12px 0; 
  overflow-y: auto; 
}

.nav-item { 
  display: flex; 
  align-items: center; 
  height: 48px;
  padding: 0 20px;
  margin: 2px 8px;
  color: rgba(255, 255, 255, 0.65); 
  text-decoration: none; 
  transition: all 0.3s; 
  cursor: pointer; 
  border-radius: 8px;
  font-size: 14px;
}

.nav-item:hover { 
  background: rgba(255, 255, 255, 0.08); 
  color: white; 
}

.nav-item.active { 
  background: linear-gradient(90deg, #4A7C59 0%, #5a9469 100%);
  color: white;
  font-weight: 500;
  box-shadow: 0 2px 8px rgba(74, 124, 89, 0.3);
}

.nav-icon { 
  font-size: 18px; 
  margin-right: 12px; 
  width: 20px;
  text-align: center;
}

.nav-text { font-size: 14px; }

/* 资质提示框 */
.qualification-notice {
  margin: 16px;
  padding: 20px;
  background: linear-gradient(135deg, #fff7e6 0%, #fff3d9 100%);
  border-radius: 12px;
  border: 1px solid #ffd591;
}

.notice-icon {
  font-size: 40px;
  text-align: center;
  margin-bottom: 12px;
}

.notice-content h4 {
  font-size: 15px;
  color: #d46b00;
  margin: 0 0 8px 0;
  text-align: center;
}

.notice-content p {
  font-size: 12px;
  color: #ad6800;
  margin: 0;
  text-align: center;
}

.notice-btn {
  display: block;
  margin-top: 16px;
  padding: 10px;
  background: #fa8c16;
  color: white;
  text-align: center;
  border-radius: 8px;
  text-decoration: none;
  font-size: 13px;
  transition: all 0.3s;
}

.notice-btn:hover {
  background: #e67300;
}

/* ========== 遮罩层 ========== */
.sidebar-overlay {
  display: none;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.sidebar-overlay.active {
  opacity: 1;
}

/* ========== 主内容区 ========== */
.shop-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #f0f2f5;
  margin-left: 220px;
  min-height: 100vh;
}

.shop-header {
  height: 60px;
  background: white;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  position: sticky;
  top: 0;
  z-index: 50;
  flex-shrink: 0;
}

.header-left h2 {
  margin: 0;
  font-size: 18px;
  color: #1a1a2e;
  font-weight: 600;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.shop-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.shop-avatar {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  border: 2px solid #e3e5e7;
}

.shop-name {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

.shop-content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}

/* ========== 统一页面组件样式 ========== */

/* 页面容器 */
.page-wrapper {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

/* 页面标题 */
.page-header {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.page-header h2 {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0 0 8px 0;
}

.page-header .subtitle {
  color: #888;
  font-size: 14px;
  margin: 0;
}

/* 统一统计卡片样式 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

/* 统一表格容器 */
.table-container {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  margin-top: 20px;
}

/* 横向状态标签筛选 */
.status-tabs {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  padding: 16px 20px;
  background: #fafbfc;
  border-radius: 12px;
  flex-wrap: wrap;
}

.status-tab {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border-radius: 24px;
  background: white;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
  color: #666;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  border: 1px solid transparent;
  white-space: nowrap;
}

.status-tab:hover {
  background: #f0f0f0;
}

.status-tab.active {
  background: linear-gradient(135deg, #4A7C59, #3d6b4a);
  color: white;
  border-color: #4A7C59;
}

.tab-count {
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  background: #f0f0f0;
  color: #666;
}

.status-tab.active .tab-count {
  background: rgba(255, 255, 255, 0.25);
  color: white;
}

.tab-count.success { background: #e8f5e9; color: #4A7C59; }
.tab-count.warning { background: #fff3e0; color: #faad14; }
.tab-count.danger { background: #ffe0e0; color: #e74c3c; }

/* 搜索工具栏 */
.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

/* 资质未通过时的重定向提示 */
.qualification-redirect {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 150px);
}

.redirect-content {
  text-align: center;
  padding: 60px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  max-width: 400px;
}

.redirect-icon {
  font-size: 80px;
  margin-bottom: 24px;
}

.redirect-content h3 {
  font-size: 22px;
  color: #333;
  margin: 0 0 12px 0;
}

.redirect-content p {
  font-size: 14px;
  color: #666;
  margin: 0 0 24px 0;
  line-height: 1.6;
}

/* ========== 移动端顶部栏 ========== */
.shop-header-mobile {
  display: none;
  height: 50px;
  background: white;
  align-items: center;
  justify-content: space-between;
  padding: 0 15px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  position: sticky;
  top: 0;
  z-index: 100;
}

.menu-toggle {
  width: 36px;
  height: 36px;
  border: none;
  background: transparent;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 5px;
  padding: 0;
}

.menu-toggle span {
  display: block;
  width: 20px;
  height: 2px;
  background: #333;
  border-radius: 2px;
}

.mobile-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.mobile-actions {
  display: flex;
  align-items: center;
}

/* ========== 响应式 ========== */
@media (max-width: 992px) {
  .shop-sidebar {
    position: fixed;
    left: 0;
    top: 0;
    height: 100vh;
    z-index: 1001;
    transform: translateX(-100%);
    transition: transform 0.3s ease;
  }
  
  .shop-sidebar.active {
    transform: translateX(0);
  }
  
  .shop-main {
    margin-left: 0;
  }
  
  .shop-content {
    padding: 16px;
  }
  
  .sidebar-overlay {
    display: block;
  }
}

@media (max-width: 576px) {
  .shop-logo {
    padding: 0 15px;
    height: 56px;
  }
  
  .logo-text {
    font-size: 15px;
  }
  
  .nav-item {
    height: 44px;
    padding: 0 15px;
  }
  
  .nav-text {
    font-size: 13px;
  }
  
  .shop-header {
    height: 50px;
  }
  
  .header-left h2 {
    font-size: 15px;
  }
  
  .shop-name {
    display: none;
  }
  
  .redirect-content {
    padding: 30px;
    margin: 15px;
  }
  
  .redirect-icon {
    font-size: 60px;
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  .status-tabs {
    padding: 12px;
    gap: 8px;
  }

  .status-tab {
    padding: 8px 14px;
    font-size: 13px;
  }
}
</style>
