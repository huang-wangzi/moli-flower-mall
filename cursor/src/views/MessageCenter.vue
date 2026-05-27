<template>
  <div class="message-center-page">
    <h1 class="page-title">消息中心</h1>

    <!-- 消息分类标签 -->
    <div class="message-tabs">
      <div
        class="message-tab"
        :class="{ active: activeTab === 'all' }"
        @click="activeTab = 'all'"
      >
        <span class="tab-icon">📋</span>
        <span>全部消息</span>
        <span v-if="totalUnread > 0" class="tab-badge">{{ totalUnread }}</span>
      </div>
      <div
        class="message-tab"
        :class="{ active: activeTab === 'order' }"
        @click="activeTab = 'order'"
      >
        <span class="tab-icon">📦</span>
        <span>订单通知</span>
        <span v-if="orderUnread > 0" class="tab-badge">{{ orderUnread }}</span>
      </div>
      <div
        class="message-tab"
        :class="{ active: activeTab === 'review' }"
        @click="activeTab = 'review'"
      >
        <span class="tab-icon">⭐</span>
        <span>评价回复</span>
        <span v-if="reviewUnread > 0" class="tab-badge">{{ reviewUnread }}</span>
      </div>
      <div
        class="message-tab"
        :class="{ active: activeTab === 'chat' }"
        @click="switchToChat"
      >
        <span class="tab-icon">💬</span>
        <span>聊天记录</span>
        <span v-if="chatUnreadCount > 0" class="tab-badge">{{ chatUnreadCount }}</span>
      </div>
    </div>

    <!-- 全部/订单通知/评价回复消息列表 -->
    <div v-if="activeTab !== 'chat'" class="message-list">
      <div
        v-for="msg in filteredMessages"
        :key="msg.id"
        class="neumorphic-card message-item"
        :class="{ unread: msg.isRead === 0 }"
        @click="handleMessageClick(msg)"
      >
        <div class="message-icon">
          {{ msg.icon }}
        </div>
        <div class="message-content">
          <div class="message-header">
            <span class="message-title">{{ msg.title }}</span>
            <span class="message-time">{{ formatTime(msg.createTime) }}</span>
          </div>
          <p class="message-text">{{ msg.content }}</p>
        </div>
        <div v-if="msg.isRead === 0" class="unread-dot"></div>
        <button class="delete-btn" @click.stop="deleteMessage(msg.id)">×</button>
      </div>

      <div v-if="filteredMessages.length === 0" class="empty-state">
        <div class="empty-icon">📭</div>
        <p class="empty-text">暂无消息</p>
      </div>

      <!-- 全部已读按钮 -->
      <div v-if="currentUnread > 0" class="action-bar">
        <button class="neumorphic-btn-sm" @click="markAllAsRead">全部标记为已读</button>
      </div>
    </div>

    <!-- 聊天会话列表 -->
    <div v-if="activeTab === 'chat'" class="chat-list">
      <div
        v-for="conv in chatConversations"
        :key="conv.userId"
        class="neumorphic-card chat-item"
        :class="{ unread: conv.unread > 0 }"
        @click="navigateToChat(conv)"
      >
        <div class="conv-avatar">
          <img :src="normalizeUploadUrl(conv.avatar) || defaultAvatar" />
          <span v-if="conv.unread > 0" class="unread-badge">{{ conv.unread > 99 ? '99+' : conv.unread }}</span>
        </div>
        <div class="chat-info">
          <div class="chat-header">
            <span class="chat-name">{{ conv.username }}</span>
            <span class="chat-time">{{ formatTime(conv.lastTime) }}</span>
          </div>
          <p class="chat-preview">{{ conv.lastMessage }}</p>
        </div>
        <span v-if="conv.unread > 0" class="unread-badge">{{ conv.unread > 99 ? '99+' : conv.unread }}</span>
      </div>

      <div v-if="chatLoading" class="loading-state">
        <el-icon class="is-loading"><Loading /></el-icon>
        <p>加载中...</p>
      </div>

      <div v-if="!chatLoading && chatConversations.length === 0" class="empty-state">
        <div class="empty-icon">💬</div>
        <p class="empty-text">暂无聊天记录</p>
        <p class="hint">在商品详情页点击"沟通商家"即可开始聊天</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { useMessageStore } from '@/stores/message'
import { useUserStore } from '@/stores/user'
import { getChatConversations, getChatUnreadCount } from '@/api'
import { normalizeUploadUrl } from '@/utils/imageUrl'

const router = useRouter()
const messageStore = useMessageStore()
const userStore = useUserStore()

const activeTab = ref('all')
const chatConversations = ref([])
const chatLoading = ref(false)
const chatUnreadCount = ref(0)
const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'

// 消息图标映射
const TYPE_ICON = { 1: '🔔', 2: '📦', 3: '⭐', 4: '✅', 5: '💬' }

// 带图标的消息列表（聊天(type=5)按最新时间追加，合并展示）
const messagesWithIcon = computed(() => {
  // 1. 站内消息
  const sysMsgs = [...messageStore.messages].map(msg => ({
    ...msg,
    icon: TYPE_ICON[msg.type] || '📋'
  }))
  // 2. 聊天消息 → 转为站内信格式追加（每个会话只取最新一条）
  const chatByUser = {}
  chatConversations.value.forEach(conv => {
    chatByUser[conv.userId] = conv
  })
  const chatMsgs = Object.values(chatByUser)
    .filter(conv => conv.lastMessage)
    .map(conv => ({
      id: `chat-${conv.userId}`,
      type: 5,
      icon: '💬',
      title: `与「${conv.username}」的对话`,
      content: conv.lastMessage,
      createTime: conv.lastTime,
      isRead: conv.unread > 0 ? 0 : 1,
      senderId: conv.userId,
      relatedId: conv.productId || null
    }))
  // 3. 合并并按时间倒序
  const all = [...sysMsgs, ...chatMsgs]
  return all.sort((a, b) => {
    const timeA = new Date(a.createTime || 0).getTime()
    const timeB = new Date(b.createTime || 0).getTime()
    return timeB - timeA
  })
})

// 各标签未读数
const currentUnread = computed(() => {
  if (activeTab.value === 'all') return messageStore.unreadCount
  if (activeTab.value === 'order') return messageStore.messages.filter(m => m.type === 2 && m.isRead === 0).length
  if (activeTab.value === 'review') return messageStore.messages.filter(m => (m.type === 3 || m.type === 4) && m.isRead === 0).length
  return 0
})

const totalUnread = computed(() => messageStore.unreadCount)
const orderUnread = computed(() => messageStore.messages.filter(m => m.type === 2 && m.isRead === 0).length)
const reviewUnread = computed(() => messageStore.messages.filter(m => (m.type === 3 || m.type === 4) && m.isRead === 0).length)

// 筛选消息
const filteredMessages = computed(() => {
  const msgs = messagesWithIcon.value
  if (activeTab.value === 'all') return msgs
  if (activeTab.value === 'order') return msgs.filter(m => m.type === 2)
  if (activeTab.value === 'review') return msgs.filter(m => m.type === 3 || m.type === 4)
  return msgs
})

// 切换到聊天
const switchToChat = async () => {
  activeTab.value = 'chat'
  await loadChatConversations()
}

// 加载聊天会话
const loadChatConversations = async () => {
  chatLoading.value = true
  try {
    const [conversationsRes, unreadRes] = await Promise.all([
      getChatConversations(),
      getChatUnreadCount()
    ])
    if (conversationsRes.code === 200) {
      chatConversations.value = conversationsRes.data || []
    }
    if (unreadRes.code === 200) {
      chatUnreadCount.value = unreadRes.data || 0
    }
  } catch (error) {
    console.error('加载聊天会话失败', error)
  } finally {
    chatLoading.value = false
  }
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  const days = Math.floor(diff / 86400000)
  if (days === 0) return '今天'
  if (days === 1) return '昨天'
  if (days < 7) return `${days}天前`
  return date.toLocaleDateString('zh-CN')
}

// 点击消息 → 标记已读 + 跳转
const handleMessageClick = async (msg) => {
  // 聊天合成消息（id为字符串如"chat-5"）无法标记已读，直接跳转
  if (msg.isRead === 0 && !String(msg.id).startsWith('chat-')) {
    await messageStore.markAsRead(msg.id)
    // 强制刷新computed属性
    const index = messageStore.messages.findIndex(m => m.id === msg.id)
    if (index > -1) {
      messageStore.messages[index].isRead = 1
    }
  }
  if (msg.type === 2 && msg.relatedId) {
    router.push(`/order/${msg.relatedId}`)
  } else if (msg.type === 5 && msg.relatedId) {
    router.push(`/chat/${msg.senderId}?productId=${msg.relatedId}`)
  } else if ((msg.type === 3 || msg.type === 4) && msg.relatedId) {
    router.push(`/product/${msg.relatedId}`)
  }
}

// 跳转到聊天会话（区分普通商家聊天和客服广播会话）
const navigateToChat = (conv) => {
  if (conv.type === 'broadcast') {
    // 客服广播会话 → 跳转广播对话页面
    router.push(`/chat/broadcast/${conv.msgId}`)
  } else {
    // 普通商家聊天
    router.push(`/chat/${conv.userId}?productId=${conv.productId || ''}`)
  }
}

// 删除消息
const deleteMessage = async (id) => {
  await messageStore.deleteMessage(id)
}

// 全部标记为已读
const markAllAsRead = async () => {
  const userId = userStore.userInfo?.id
  if (userId) {
    await messageStore.markAllAsRead(userId)
    ElMessage.success('已全部标记为已读')
  }
}

// 定时刷新
let refreshInterval = null

// 监听聊天已读事件
const handleChatRead = async () => {
  // 刷新聊天未读数
  try {
    const unreadRes = await getChatUnreadCount()
    if (unreadRes.code === 200) {
      chatUnreadCount.value = unreadRes.data || 0
    }
  } catch (e) {}
}

onMounted(async () => {
  const userId = userStore.userInfo?.id
  if (userId) {
    // 同时加载站内消息和聊天会话
    await Promise.all([
      messageStore.loadMessages(userId),
      loadChatConversations()
    ])
  }
  // 每15秒刷新一次消息
  refreshInterval = setInterval(async () => {
    if (userStore.userInfo?.id) {
      await Promise.all([
        messageStore.loadMessages(userStore.userInfo.id),
        loadChatConversations()
      ])
    }
  }, 15000)
  // 监听聊天已读事件
  window.addEventListener('chat-read', handleChatRead)
})

onUnmounted(() => {
  if (refreshInterval) clearInterval(refreshInterval)
  window.removeEventListener('chat-read', handleChatRead)
})
</script>

<style scoped>
.message-center-page {
  padding: 20px;
}

.message-tabs {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  overflow-x: auto;
  padding-bottom: 8px;
}

.message-tab {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  border-radius: 12px;
  background: #e3e5e7;
  box-shadow: 4px 4px 8px #b6b9ba, -4px -4px 8px #fafafd;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.3s;
}

.message-tab:hover {
  transform: translateY(-2px);
}

.message-tab.active {
  background: linear-gradient(145deg, #4A7C59, #3d6b4a);
  color: white;
}

.tab-icon { font-size: 18px; }

.tab-badge {
  background: #e74c3c;
  color: white;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  min-width: 20px;
  text-align: center;
}

.message-tab.active .tab-badge {
  background: rgba(255,255,255,0.3);
}

.message-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 20px;
  cursor: pointer;
  position: relative;
  transition: all 0.3s;
}

.message-item:hover {
  transform: translateX(5px);
}

.message-item.unread {
  background: linear-gradient(145deg, #eef6ee, #e3e5e7);
  border-left: 3px solid #4A7C59;
}

.message-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: #e3e5e7;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  flex-shrink: 0;
  box-shadow: inset 2px 2px 4px #b6b9ba, inset -2px -2px 4px #fafafd;
}

.message-content {
  flex: 1;
  min-width: 0;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.message-title {
  font-weight: 600;
  color: #333;
}

.message-time {
  font-size: 12px;
  color: #999;
}

.message-text {
  color: #666;
  font-size: 14px;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.unread-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #e74c3c;
  flex-shrink: 0;
  margin-top: 4px;
}

.delete-btn {
  position: absolute;
  top: 10px;
  right: 10px;
  width: 24px;
  height: 24px;
  border: none;
  background: transparent;
  color: #999;
  font-size: 18px;
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.3s;
}

.message-item:hover .delete-btn {
  opacity: 1;
}

.delete-btn:hover {
  color: #e74c3c;
}

.action-bar {
  margin-top: 24px;
  text-align: center;
}

.chat-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.chat-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.chat-item:hover {
  transform: translateX(5px);
}

.chat-item.unread {
  background: #f0f9eb;
  border-left: 3px solid #67c23a;
}

.conv-avatar {
  position: relative;
  flex-shrink: 0;
}

.conv-avatar img {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  object-fit: cover;
}

.chat-info {
  flex: 1;
  min-width: 0;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.chat-name {
  font-weight: 600;
  color: #333;
}

.chat-time {
  font-size: 12px;
  color: #999;
}

.chat-preview {
  color: #666;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.unread-badge {
  background: #e74c3c;
  color: white;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  flex-shrink: 0;
}

.empty-state {
  padding: 80px 20px;
  text-align: center;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.empty-text {
  font-size: 18px;
  color: #666;
  margin-bottom: 8px;
}

.hint {
  font-size: 14px;
  color: #999;
}

.loading-state {
  text-align: center;
  padding: 40px;
  color: #999;
}

.loading-state .el-icon {
  font-size: 32px;
  margin-bottom: 12px;
}
</style>
