<template>
  <div class="profile-page">
    <!-- 用户信息卡片 -->
    <div class="neumorphic-card user-card" v-if="userInfo">
      <div class="user-info">
        <img :src="normalizeUploadUrl(userInfo.avatar) || defaultAvatar" class="user-avatar" />
        <div class="user-detail">
          <h3 class="user-name">{{ userInfo.username }}</h3>
          <span class="user-level">{{ userInfo.memberLevel }}</span>
        </div>
      </div>
      <div class="user-stats">
        <div class="stat-item">
          <span class="stat-value">{{ userInfo.memberPoints }}</span>
          <span class="stat-label">积分</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">¥{{ userInfo.balance }}</span>
          <span class="stat-label">余额</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">{{ orderStore.totalOrderCount }}</span>
          <span class="stat-label">订单</span>
        </div>
      </div>
    </div>

    <!-- 订单快捷入口 -->
    <div class="neumorphic-card order-quick">
      <div class="order-header">
        <h3>我的订单</h3>
        <span class="more-link" @click="$router.push('/orders')">全部订单 →</span>
      </div>
      <div class="order-navs">
        <div class="order-nav" @click="$router.push('/orders?status=pending')">
          <el-badge :value="orderStore.pendingPaymentCount" :hidden="orderStore.pendingPaymentCount === 0">
            <span class="nav-icon">💳</span>
          </el-badge>
          <span class="nav-label">待付款</span>
        </div>
        <div class="order-nav" @click="$router.push('/orders?status=shipped')">
          <el-badge :value="orderStore.pendingReceiveCount" :hidden="orderStore.pendingReceiveCount === 0">
            <span class="nav-icon">📦</span>
          </el-badge>
          <span class="nav-label">待收货</span>
        </div>
        <div class="order-nav" @click="$router.push('/orders?status=completed')">
          <span class="nav-icon">✅</span>
          <span class="nav-label">已完成</span>
        </div>
        <div class="order-nav" @click="$router.push('/after-sales')">
          <el-badge :value="orderStore.refundingCount" :hidden="orderStore.refundingCount === 0">
            <span class="nav-icon">🔄</span>
          </el-badge>
          <span class="nav-label">售后</span>
        </div>
      </div>
    </div>

    <!-- 功能菜单 -->
    <div class="neumorphic-card menu-section">
      <h3 class="menu-title">我的资产</h3>
      <div class="menu-grid">
        <div class="menu-item" @click="$router.push('/history')">
          <span class="menu-icon">📜</span>
          <span class="menu-label">浏览历史</span>
        </div>
        <div class="menu-item" @click="$router.push('/reviews')">
          <span class="menu-icon">⭐</span>
          <span class="menu-label">我的评价</span>
        </div>
      </div>

      <h3 class="menu-title">常用设置</h3>
      <div class="menu-list">
        <div class="menu-list-item" @click="$router.push('/address')">
          <span class="menu-icon">📍</span>
          <span class="menu-label">收货地址</span>
          <span class="arrow">→</span>
        </div>
        <div class="menu-list-item" @click="showEditDialog = true">
          <span class="menu-icon">👤</span>
          <span class="menu-label">个人信息</span>
          <span class="arrow">→</span>
        </div>
        <div class="menu-list-item" @click="$router.push('/messages')">
          <span class="menu-icon">💬</span>
          <span class="menu-label">消息中心</span>
          <span class="arrow">→</span>
        </div>
        <div class="menu-list-item danger" @click="handleCancelAccount">
          <span class="menu-icon">🚪</span>
          <span class="menu-label">注销账号</span>
          <span class="arrow">→</span>
        </div>
      </div>
    </div>

    <!-- 编辑个人信息弹窗 -->
    <el-dialog v-model="showEditDialog" title="编辑个人信息" width="480px" :close-on-click-modal="false" destroy-on-close>
      <div class="edit-profile-content">
        <!-- 头像 -->
        <div class="avatar-edit-wrap">
          <img :src="editForm.avatar" class="avatar-preview" />
          <div class="avatar-actions">
            <el-button size="small" @click="triggerAvatarUpload" :loading="avatarUploading">更换头像</el-button>
            <input id="avatar-file-input" type="file" accept="image/*" style="display:none" @change="handleAvatarChange" />
            <p class="avatar-tip">支持 JPG、PNG，建议 200x200</p>
          </div>
        </div>
        <!-- 表单 -->
        <el-form :model="editForm" label-width="80px" style="margin-top:20px">
          <el-form-item label="昵称">
            <el-input v-model="editForm.nickname" placeholder="请输入昵称（选填）" maxlength="20" />
          </el-form-item>
          <el-form-item label="性别">
            <el-radio-group v-model="editForm.gender">
              <el-radio :value="0">保密</el-radio>
              <el-radio :value="1">男</el-radio>
              <el-radio :value="2">女</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="editForm.phone" placeholder="请输入手机号" maxlength="11" />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="editForm.email" placeholder="请输入邮箱（选填）" />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="submitEdit" :loading="editLoading">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, reactive, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useOrderStore } from '@/stores/order'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserInfo, updateUserInfo, cancelAccount, uploadImage } from '@/api'
import { normalizeUploadUrl } from '@/utils/imageUrl'

const userStore = useUserStore()
const orderStore = useOrderStore()
const userInfo = computed(() => userStore.userInfo)
const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'

// ============ 个人中心编辑 ============
const showEditDialog = ref(false)
const editForm = reactive({
  nickname: '',
  avatar: '',
  gender: 0,
  phone: '',
  email: ''
})
const editLoading = ref(false)
const avatarUploading = ref(false)

const openEditDialog = async () => {
  const uid = userStore.userInfo?.id
  if (!uid) { ElMessage.warning('请先登录'); return }
  // 从后端拉最新数据
  try {
    const res = await getUserInfo(uid)
    if (res.code === 200 && res.data) {
      const d = res.data
      editForm.nickname = d.nickname || d.username || ''
      editForm.avatar = d.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
      editForm.gender = d.gender ?? 0
      editForm.phone = d.phone || ''
      editForm.email = d.email || ''
    } else {
      editForm.nickname = userStore.userInfo?.nickname || userStore.userInfo?.username || ''
      editForm.avatar = userStore.userInfo?.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
      editForm.gender = userStore.userInfo?.gender ?? 0
      editForm.phone = userStore.userInfo?.phone || ''
      editForm.email = userStore.userInfo?.email || ''
    }
  } catch {
    editForm.nickname = userStore.userInfo?.nickname || userStore.userInfo?.username || ''
    editForm.avatar = userStore.userInfo?.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
    editForm.gender = userStore.userInfo?.gender ?? 0
    editForm.phone = userStore.userInfo?.phone || ''
    editForm.email = userStore.userInfo?.email || ''
  }
  showEditDialog.value = true
}

// 点击头像触发上传
const triggerAvatarUpload = () => {
  document.getElementById('avatar-file-input')?.click()
}

const handleAvatarChange = async (e) => {
  const file = e.target.files?.[0]
  if (!file) return
  if (file.size > 5 * 1024 * 1024) { ElMessage.warning('图片大小不能超过5MB'); return }
  avatarUploading.value = true
  try {
    // 使用压缩工具压缩头像图片
    const { compressImage } = await import('@/utils/imageCompress')
    const base64 = await compressImage(file, {
      maxWidth: 500,
      maxHeight: 500,
      maxSizeMB: 0.5,
      quality: 0.85
    })
    // 直接使用 Base64 作为头像
    editForm.avatar = base64
    ElMessage.success('头像上传成功')
  } catch (err) {
    ElMessage.error(err.message || '头像上传失败，请重试')
  } finally {
    avatarUploading.value = false
  }
}

const submitEdit = async () => {
  const uid = userStore.userInfo?.id
  if (!uid) { ElMessage.warning('请先登录'); return }
  if (editForm.nickname && editForm.nickname.trim().length > 20) {
    ElMessage.warning('昵称不能超过20字'); return
  }
  if (editForm.phone && !/^1[3-9]\d{9}$/.test(editForm.phone)) {
    ElMessage.warning('手机号格式不正确'); return
  }
  if (editForm.email && !/^[\w.-]+@[\w.-]+\.\w+$/.test(editForm.email)) {
    ElMessage.warning('邮箱格式不正确'); return
  }
  editLoading.value = true
  try {
    const res = await updateUserInfo(uid, {
      nickname: editForm.nickname,
      avatar: editForm.avatar,
      gender: editForm.gender,
      phone: editForm.phone,
      email: editForm.email
    })
    if (res.code === 200) {
      // 更新本地 store
      userStore.userInfo.value = {
        ...userStore.userInfo.value,
        nickname: editForm.nickname,
        avatar: editForm.avatar,
        gender: editForm.gender,
        phone: editForm.phone,
        email: editForm.email
      }
      // 同步保存到 localStorage
      const localData = JSON.parse(localStorage.getItem('moli_mall_current_user') || '{}')
      localData.userInfo = { ...localData.userInfo, nickname: editForm.nickname, avatar: editForm.avatar, phone: editForm.phone, email: editForm.email }
      localStorage.setItem('moli_mall_current_user', JSON.stringify(localData))
      ElMessage.success('修改成功')
      showEditDialog.value = false
    } else {
      ElMessage.error(res.message || '修改失败')
    }
  } catch { ElMessage.error('修改失败，请重试') }
  finally { editLoading.value = false }
}

// ============ 注销账号 ============
const handleCancelAccount = () => {
  ElMessageBox.confirm(
    '注销账号将清空所有个人数据，此操作不可恢复！\n确定要注销当前账号吗？',
    '注销账号',
    { confirmButtonText: '确定注销', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    const uid = userStore.userInfo?.id
    if (!uid) { ElMessage.warning('请先登录'); return }
    try {
      const res = await cancelAccount(uid)
      if (res.code === 200) {
        ElMessage.success('账号已注销，欢迎下次使用！')
        userStore.logout()
        router.push('/login')
      } else {
        ElMessage.error(res.message || '注销失败')
      }
    } catch { ElMessage.error('注销失败，请重试') }
  }).catch(() => {})
}

// 监听打开编辑弹窗
const router = useRouter()
watch(showEditDialog, (val) => { if (val) openEditDialog() })
</script>

<style scoped>
.profile-page {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

/* 用户信息卡片 */
.user-card {
  padding: 30px;
  margin-bottom: 24px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 24px;
}

.user-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  box-shadow: 4px 4px 8px #b6b9ba, -4px -4px 8px #fafafd;
}

.user-detail {
  flex: 1;
}

.user-name {
  font-size: 22px;
  font-weight: bold;
  margin-bottom: 8px;
}

.user-level {
  display: inline-block;
  padding: 4px 12px;
  background: linear-gradient(145deg, #FFD700, #FFA500);
  color: #333;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}

.user-stats {
  display: flex;
  justify-content: space-around;
  padding-top: 20px;
  border-top: 1px solid #ddd;
}

.stat-item {
  text-align: center;
}

.stat-value {
  display: block;
  font-size: 20px;
  font-weight: bold;
  color: #4A7C59;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 13px;
  color: #999;
}

/* 订单快捷入口 */
.order-quick {
  padding: 24px;
  margin-bottom: 24px;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.order-header h3 {
  font-size: 18px;
  font-weight: 600;
}

.more-link {
  color: #4A7C59;
  font-size: 14px;
  cursor: pointer;
}

.order-navs {
  display: flex;
  justify-content: space-around;
}

.order-nav {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
}

.nav-icon {
  font-size: 28px;
  margin-bottom: 8px;
}

.nav-label {
  font-size: 13px;
  color: #666;
}

/* 功能菜单 */
.menu-section {
  padding: 24px;
}

.menu-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #333;
}

.menu-grid {
  display: flex;
  justify-content: space-around;
  margin-bottom: 30px;
}

.menu-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  transition: transform 0.3s;
}

.menu-item:hover {
  transform: translateY(-3px);
}

.menu-icon {
  font-size: 28px;
  margin-bottom: 8px;
}

.menu-label {
  font-size: 13px;
  color: #666;
}

.menu-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.menu-list-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-radius: 12px;
  cursor: pointer;
  transition: background 0.3s;
}

.menu-list-item:hover {
  background: rgba(74, 124, 89, 0.1);
}

.menu-list-item .menu-icon {
  font-size: 20px;
  margin-bottom: 0;
}

.menu-list-item .menu-label {
  flex: 1;
  font-size: 15px;
  color: #333;
}

.arrow {
  color: #999;
}

/* 注销危险按钮 */
.menu-list-item.danger {
  color: #E6A23C;
}
.menu-list-item.danger .menu-label {
  color: #E6A23C;
}
.menu-list-item.danger:hover {
  background: rgba(230, 162, 60, 0.1);
}

/* 编辑个人信息弹窗 */
.edit-profile-content {
  padding: 0 8px;
}
.avatar-edit-wrap {
  display: flex;
  align-items: center;
  gap: 20px;
}
.avatar-preview {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  box-shadow: 4px 4px 8px #b6b9ba, -4px -4px 8px #fafafd;
  flex-shrink: 0;
}
.avatar-actions {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.avatar-tip {
  font-size: 12px;
  color: #999;
  margin: 0;
}
</style>
