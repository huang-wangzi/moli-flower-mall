<template>
  <div class="admin-layout">
    <!-- 移动端顶部栏 -->
    <header class="admin-header-mobile">
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
    <aside class="admin-sidebar" :class="{ active: sidebarOpen }">
      <div class="admin-logo">
        <span class="logo-icon">⚙️</span>
        <span class="logo-text">管理中心</span>
      </div>
      <nav class="admin-nav">
        <router-link to="/admin" class="nav-item" :class="{ active: $route.path === '/admin' }" @click="closeSidebar">
          <span class="nav-icon">📊</span>
          <span class="nav-text">系统概览</span>
        </router-link>
        <router-link to="/admin/users" class="nav-item" :class="{ active: $route.path.startsWith('/admin/users') }" @click="closeSidebar">
          <span class="nav-icon">👥</span>
          <span class="nav-text">用户管理</span>
        </router-link>
        <router-link to="/admin/shops" class="nav-item" :class="{ active: $route.path.startsWith('/admin/shops') }" @click="closeSidebar">
          <span class="nav-icon">🏪</span>
          <span class="nav-text">商家审核</span>
        </router-link>
        <router-link to="/admin/qualifications" class="nav-item" :class="{ active: $route.path.startsWith('/admin/qualifications') }" @click="closeSidebar">
          <span class="nav-icon">📋</span>
          <span class="nav-text">资质核验</span>
        </router-link>
        <router-link to="/admin/products" class="nav-item" :class="{ active: $route.path.startsWith('/admin/products') }" @click="closeSidebar">
          <span class="nav-icon">📦</span>
          <span class="nav-text">商品审核</span>
        </router-link>
        <router-link to="/admin/acquisition" class="nav-item" :class="{ active: $route.path.startsWith('/admin/acquisition') }" @click="closeSidebar">
          <span class="nav-icon">🌸</span>
          <span class="nav-text">收购管理</span>
        </router-link>
        <router-link to="/admin/prices" class="nav-item" :class="{ active: $route.path.startsWith('/admin/prices') }" @click="closeSidebar">
          <span class="nav-icon">💹</span>
          <span class="nav-text">价格监测</span>
        </router-link>
        <router-link to="/admin/complaints" class="nav-item" :class="{ active: $route.path.startsWith('/admin/complaints') }" @click="closeSidebar">
          <span class="nav-icon">⚠️</span>
          <span class="nav-text">交易投诉</span>
        </router-link>
        <router-link to="/admin/messages" class="nav-item" :class="{ active: $route.path.startsWith('/admin/messages') }" @click="closeSidebar">
          <span class="nav-icon">📢</span>
          <span class="nav-text">系统消息</span>
        </router-link>
        <router-link to="/admin/service" class="nav-item" :class="{ active: $route.path.startsWith('/admin/service') }" @click="closeSidebar">
          <span class="nav-icon">💬</span>
          <span class="nav-text">客服中心</span>
        </router-link>
      </nav>
      <div class="admin-footer">
      
      </div>
    </aside>

    <!-- 遮罩层 -->
    <div class="sidebar-overlay" :class="{ active: sidebarOpen }" @click="closeSidebar"></div>

    <!-- 右侧内容 -->
    <div class="admin-main">
      <!-- PC端顶部栏 -->
      <header class="admin-header">
        <div class="header-left">
          <h2>{{ pageTitle }}</h2>
        </div>
        <div class="header-right">
          <div class="admin-info">
            <span class="admin-name">管理员</span>
          </div>
          <el-button type="danger" size="small" @click="handleLogout">退出登录</el-button>
        </div>
      </header>

      <!-- 内容区 -->
      <main class="admin-content">
        <div class="content-wrapper">
          <router-view v-slot="{ Component }">
            <component :is="Component" />
          </router-view>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const sidebarOpen = ref(false)

const pageTitle = computed(() => {
  return route.meta?.title || '管理员后台'
})

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

const handleLogout = () => {
  closeSidebar()
  userStore.logout()
  router.push('/login')
}

// 路由变化时关闭侧边栏
watch(() => route.path, () => {
  closeSidebar()
})
</script>

<style scoped>
/* ================================================
   管理员后台布局 - 统一设计
   ================================================ */

.admin-layout {
  display: flex;
  min-height: 100vh;
  background: #f0f2f5;
}

/* ========== 侧边栏 ========== */
.admin-sidebar {
  width: 220px;
  min-width: 220px;
  flex-shrink: 0;
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%);
  color: white;
  display: flex;
  flex-direction: column;
  height: 100vh;
  position: fixed;
  left: 0;
  top: 0;
  z-index: 1001;
  transition: transform 0.3s ease;
  box-shadow: 2px 0 10px rgba(0, 0, 0, 0.1);
}

.admin-logo {
  height: 64px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(255, 255, 255, 0.02);
}

.logo-icon {
  font-size: 24px;
}

.logo-text {
  font-size: 16px;
  font-weight: 600;
  color: white;
  letter-spacing: 1px;
}

.admin-nav {
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
  background: linear-gradient(90deg, #e6a23c 0%, #f5b84e 100%);
  color: white;
  font-weight: 500;
  box-shadow: 0 2px 8px rgba(230, 162, 60, 0.3);
}

.nav-icon {
  font-size: 18px;
  margin-right: 12px;
  width: 20px;
  text-align: center;
}

.nav-text {
  font-size: 14px;
}

.admin-footer {
  padding: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}

.back-home {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: rgba(255, 255, 255, 0.65);
  cursor: pointer;
  padding: 12px;
  border-radius: 8px;
  transition: all 0.3s;
  font-size: 14px;
}

.back-home:hover {
  background: rgba(255, 255, 255, 0.08);
  color: white;
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
.admin-main,
.shop-main,
.acquirer-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #f0f2f5;
  margin-left: 200px;
  min-height: 100vh;
}

.admin-header,
.shop-header,
.acquirer-header {
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

.admin-info,
.shop-info,
.acquirer-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.admin-name,
.shop-name,
.acquirer-name {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

.shop-avatar {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  border: 2px solid #e3e5e7;
}

.acquirer-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
}

/* 内容区域 - 统一内边距 */
.admin-content,
.shop-content,
.acquirer-content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}

/* 统一的内容包装器 */
.content-wrapper {
  max-width: 100%;
  width: 100%;
}

/* ========== 统一页面组件样式 ========== */

/* 页面容器 - 主内容包装 */
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

/* ========== 移动端顶部栏 ========== */
.admin-header-mobile {
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
  transition: all 0.3s ease;
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
  .admin-sidebar {
    transform: translateX(-100%);
  }
  
  .admin-sidebar.active {
    transform: translateX(0);
  }
  
  .sidebar-overlay {
    display: block;
  }
  
  .admin-main {
    margin-left: 0;
  }
  
  .admin-header {
    display: none;
  }
  
  .admin-header-mobile {
    display: flex;
  }
  
  .admin-content {
    padding: 16px;
  }
  
  .custom-tabs {
    border-radius: 8px;
  }
  
  .tab-content {
    padding: 12px;
  }
}

@media (max-width: 576px) {
  .nav-text {
    font-size: 13px;
  }
  
  .nav-item {
    height: 44px;
    padding: 0 15px;
  }
  
  .logo-text {
    font-size: 15px;
  }
  
  .admin-logo {
    padding: 0 15px;
    height: 56px;
  }
  
  .admin-content {
    padding: 12px;
  }
  
  .mobile-title {
    font-size: 14px;
  }
  
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
  
  .custom-tabs {
    border-radius: 6px;
  }
  
  .tab-content {
    padding: 8px;
  }
}
</style>
