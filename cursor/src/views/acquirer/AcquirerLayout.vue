<template>
  <div class="acquirer-layout">
    <!-- 侧边栏 -->
    <aside class="acquirer-sidebar">
      <div class="acquirer-logo">
        <span class="logo-icon">🌺</span>
        <span class="logo-text">收购商中心</span>
      </div>

      <nav class="acquirer-nav">
        <!-- 仪表盘 -->
        <router-link to="/acquirer" class="nav-item" :class="{ active: $route.path === '/acquirer' }">
          <span class="nav-icon">📊</span>
          <span class="nav-text">数据统计</span>
        </router-link>

        <!-- 收购需求管理 -->
        <router-link to="/acquirer/acquisition" class="nav-item" :class="{ active: $route.path.startsWith('/acquirer/acquisition') }">
          <span class="nav-icon">🌸</span>
          <span class="nav-text">收购需求管理</span>
        </router-link>

        <!-- 收购订单 -->
        <router-link to="/acquirer/orders" class="nav-item" :class="{ active: $route.path.startsWith('/acquirer/orders') }">
          <span class="nav-icon">📋</span>
          <span class="nav-text">收购订单</span>
        </router-link>

        <!-- 消息中心 -->
        <router-link to="/acquirer/messages" class="nav-item" :class="{ active: $route.path.startsWith('/acquirer/messages') }">
          <span class="nav-icon">💬</span>
          <span class="nav-text">消息中心</span>
        </router-link>
      </nav>

      <!-- 退出按钮 -->
      <div class="acquirer-footer">
        <div class="back-home" @click="handleLogout">
          <span>🚪</span>
          <span>退出登录</span>
        </div>
      </div>
    </aside>

    <!-- 右侧内容 -->
    <div class="acquirer-main">
      <!-- 顶部栏 -->
      <header class="acquirer-header">
        <div class="header-left">
          <h2>{{ pageTitle }}</h2>
        </div>
        <div class="header-right">
          <el-button size="small" @click="openAddressDialog" style="margin-right:8px;">
            📍 地址管理
          </el-button>
          <div class="acquirer-info">
            <img :src="normalizeUploadUrl(userStore.userInfo?.avatar) || defaultAvatar" alt="avatar" class="acquirer-avatar" />
            <span class="acquirer-name">{{ userStore.userInfo?.username || '收购商' }}</span>
          </div>
          <el-button type="danger" size="small" @click="handleLogout">退出登录</el-button>
        </div>
      </header>

      <!-- 内容区 -->
      <main class="acquirer-content">
        <router-view v-slot="{ Component }">
          <component :is="Component" />
        </router-view>
      </main>
    </div>

    <!-- 地址管理弹框 -->
    <el-dialog v-model="showAddressDialog" title="地址管理" width="520px">
      <div class="addr-list" v-if="addresses.length > 0">
        <div v-for="(addr, i) in addresses" :key="i" class="addr-item">
          <div class="addr-info">
            <div class="addr-name">{{ addr.contactName }} <span class="addr-phone">{{ addr.contactPhone }}</span></div>
            <div class="addr-market">📍 {{ addr.marketAddress }}</div>
          </div>
          <el-button type="danger" size="small" text @click="deleteAddress(addr.id)">删除</el-button>
        </div>
      </div>
      <div v-else class="addr-empty">暂无常用地址，请添加</div>

      <el-divider>添加新地址</el-divider>
      <el-form :model="addrForm" label-width="80px" size="small">
        <el-form-item label="市场地址">
          <el-input v-model="addrForm.marketAddress" placeholder="如：横州茉莉花交易市场" />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="addrForm.contactName" placeholder="请输入联系人姓名" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="addrForm.contactPhone" placeholder="请输入联系电话" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddressDialog = false">关闭</el-button>
        <el-button type="primary" @click="saveAddress">保存地址</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { normalizeUploadUrl } from '@/utils/imageUrl'
import { ElMessage } from 'element-plus'
import {
  getAcquirerAddressList,
  addAcquirerAddress,
  deleteAcquirerAddress
} from '@/api'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'

// 地址管理
const showAddressDialog = ref(false)
const addresses = ref([])
const addrForm = ref({ id: null, marketAddress: '', contactName: '', contactPhone: '' })

// 加载收购商地址列表
const loadAddresses = async () => {
  try {
    const res = await getAcquirerAddressList()
    if (res.code === 200) {
      addresses.value = res.data || []
    } else {
      addresses.value = []
    }
  } catch (e) {
    addresses.value = []
  }
}

// 保存地址
const saveAddress = async () => {
  if (!addrForm.value.marketAddress && !addrForm.value.contactPhone) {
    ElMessage.warning('请至少填写市场地址或联系电话')
    return
  }
  try {
    const res = await addAcquirerAddress({
      marketAddress: addrForm.value.marketAddress,
      contactName: addrForm.value.contactName,
      contactPhone: addrForm.value.contactPhone
    })
    if (res.code === 200) {
      await loadAddresses()
      addrForm.value = { id: null, marketAddress: '', contactName: '', contactPhone: '' }
      ElMessage.success('地址已保存')
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (e) {
    ElMessage.error('保存失败，请重试')
  }
}

// 删除地址
const deleteAddress = async (id) => {
  try {
    const res = await deleteAcquirerAddress(id)
    if (res.code === 200) {
      await loadAddresses()
      ElMessage.success('删除成功')
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (e) {
    ElMessage.error('删除失败，请重试')
  }
}

// 打开地址弹框时加载数据
const openAddressDialog = () => {
  showAddressDialog.value = true
  loadAddresses()
}

const pageTitle = computed(() => {
  const titles = {
    '/acquirer': '数据统计',
    '/acquirer/acquisition': '收购需求管理',
    '/acquirer/orders': '收购订单',
    '/acquirer/messages': '消息中心'
  }
  return titles[route.path] || '收购商后台'
})

// 退出登录
const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}

// 组件挂载时加载地址数据
onMounted(() => {
  loadAddresses()
})
</script>

<style scoped>
/* ================================================
   收购商后台布局 - 统一设计
   ================================================ */

.acquirer-layout {
  display: flex;
  min-height: 100vh;
  width: 100%;
  background: #f0f2f5;
}

/* ========== 侧边栏 ========== */
.acquirer-sidebar {
  width: 220px;
  min-width: 220px;
  flex-shrink: 0;
  background: linear-gradient(180deg, #2d5a3d 0%, #1a3d2a 100%);
  color: white;
  display: flex;
  flex-direction: column;
  position: fixed;
  left: 0;
  top: 0;
  height: 100vh;
  box-shadow: 2px 0 10px rgba(0, 0, 0, 0.1);
}

.acquirer-logo {
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

.acquirer-nav {
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
  background: linear-gradient(90deg, #e8a045 0%, #f5b84e 100%);
  color: white;
  font-weight: 500;
  box-shadow: 0 2px 8px rgba(232, 160, 69, 0.3);
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

.acquirer-footer {
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

/* ========== 主内容区 ========== */
.acquirer-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #f0f2f5;
  margin-left: 220px;
  min-height: 100vh;
}

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

.acquirer-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.acquirer-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
}

.acquirer-name {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

.acquirer-content {
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

/* ========== 响应式 ========== */
@media (max-width: 992px) {
  .acquirer-sidebar {
    position: fixed;
    left: 0;
    top: 0;
    height: 100vh;
    z-index: 1001;
    transform: translateX(-100%);
    transition: transform 0.3s ease;
  }

  .acquirer-sidebar.active {
    transform: translateX(0);
  }

  .acquirer-main {
    margin-left: 0;
  }

  .acquirer-content {
    padding: 16px;
  }

  .sidebar-overlay {
    display: block;
  }
}

@media (max-width: 576px) {
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

.addr-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  border: 1px solid #eee;
  border-radius: 8px;
  margin-bottom: 8px;
}
.addr-name { font-weight: 600; color: #333; font-size: 14px; }
.addr-phone { color: #4A7C59; margin-left: 8px; font-size: 13px; }
.addr-market { color: #999; font-size: 12px; margin-top: 4px; }
.addr-empty { text-align: center; color: #aaa; padding: 20px 0; }
</style>
