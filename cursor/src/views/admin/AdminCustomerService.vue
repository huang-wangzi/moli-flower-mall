<template>
  <div class="admin-customer-service">
    <div class="service-header">
      <h2>客服中心</h2>
      <p class="subtitle">处理用户通过商品详情页「联系客服」发来的消息</p>
    </div>

    <div class="service-container">
      <!-- 左侧：会话列表 -->
      <div class="conversation-list">
        <div class="list-header">
          <span>用户会话</span>
          <span class="unread-badge" v-if="totalUnread > 0">{{ totalUnread }}</span>
        </div>
        <div class="conversations" v-loading="loadingConversations">
          <div v-if="conversations.length === 0 && !loadingConversations" class="empty-list">
            <p>暂无用户咨询</p>
          </div>
          <div
            v-for="conv in conversations"
            :key="conv.userId"
            class="conversation-item"
            :class="{ active: selectedUserId === conv.userId, unread: conv.unread > 0 }"
            @click="selectConversation(conv)"
          >
            <img :src="getAvatar(conv.avatar)" class="conv-avatar" />
            <div class="conv-info">
              <div class="conv-header">
                <span class="conv-name">{{ conv.username || '用户' + conv.userId }}</span>
                <span class="conv-time">{{ formatTime(conv.lastTime) }}</span>
              </div>
              <div class="conv-preview">
                <span class="last-message">{{ conv.lastMessage }}</span>
                <span v-if="conv.unread > 0" class="unread-count">{{ conv.unread > 99 ? '99+' : conv.unread }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：聊天详情 -->
      <div class="chat-detail">
        <div v-if="!selectedUserId" class="no-selection">
          <div class="empty-chat">
            <span class="icon">💬</span>
            <p>请选择左侧用户会话</p>
            <p class="tip">用户可在商品详情页点击「联系客服」与您沟通</p>
          </div>
        </div>
        <template v-else>
          <div class="chat-header">
            <div class="user-info">
              <img :src="getAvatar(currentConversation?.avatar)" class="header-avatar" />
              <div>
                <div class="user-name">{{ currentConversation?.username || '用户' + selectedUserId }}</div>
                <div class="user-tip" v-if="currentConversation?.productId">咨询商品 #{{ currentConversation.productId }}</div>
              </div>
            </div>
          </div>

          <div class="messages-area" ref="messagesArea">
            <div v-if="messages.length === 0" class="no-messages">
              <p>暂无消息记录</p>
            </div>
            <div v-for="msg in messages" :key="msg.id" class="message-item" :class="msg.fromUserId === adminId ? 'mine' : 'theirs'">
              <img v-if="msg.fromUserId !== adminId" :src="getAvatar(msg.fromAvatar)" class="msg-avatar" />
              <div class="message-bubble">
                <div class="message-content">{{ msg.content }}</div>
                <div class="message-time">{{ formatMessageTime(msg.createTime) }}</div>
              </div>
              <img v-if="msg.fromUserId === adminId" :src="getAvatar(msg.toAvatar)" class="msg-avatar" />
            </div>
          </div>

          <div class="input-area">
            <el-input
              v-model="inputMessage"
              type="textarea"
              :rows="2"
              placeholder="输入回复内容..."
              @keydown.enter.ctrl="sendMessage"
            />
            <el-button type="primary" :disabled="!inputMessage.trim()" @click="sendMessage" :loading="sending">
              发送
            </el-button>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getAdminChatConversations, getAdminChatHistory, adminSendChatMessage, adminMarkChatRead } from '@/api'
import { normalizeUploadUrl } from '@/utils/imageUrl'

const loadingConversations = ref(false)
const loadingMessages = ref(false)
const sending = ref(false)
const conversations = ref([])
const messages = ref([])
const selectedUserId = ref(null)
const inputMessage = ref('')
const messagesArea = ref(null)
const adminId = ref(null)

// 当前选中的会话
const currentConversation = computed(() => {
  return conversations.value.find(c => c.userId === selectedUserId.value)
})

// 总未读数
const totalUnread = computed(() => {
  return conversations.value.reduce((sum, c) => sum + (c.unread || 0), 0)
})

const getAvatar = (avatar) => {
  if (!avatar) return 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
  return normalizeUploadUrl(avatar) || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'
  return date.toLocaleDateString('zh-CN')
}

const formatMessageTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

// 加载会话列表
const loadConversations = async () => {
  loadingConversations.value = true
  try {
    const res = await getAdminChatConversations()
    if (res.code === 200) {
      conversations.value = res.data || []
    } else {
      ElMessage.error(res.message || '加载会话列表失败')
    }
  } catch (e) {
    console.error('加载会话列表失败:', e)
    ElMessage.error('加载会话列表失败')
  } finally {
    loadingConversations.value = false
  }
}

// 选择会话
const selectConversation = async (conv) => {
  selectedUserId.value = conv.userId
  inputMessage.value = ''
  
  // 标记已读
  if (conv.unread > 0) {
    try {
      await adminMarkChatRead(conv.userId)
      conv.unread = 0
    } catch (e) {
      console.error('标记已读失败:', e)
    }
  }
  
  await loadMessages()
}

// 加载聊天记录
const loadMessages = async () => {
  if (!selectedUserId.value) return
  
  loadingMessages.value = true
  try {
    const res = await getAdminChatHistory(selectedUserId.value)
    if (res.code === 200) {
      messages.value = (res.data || []).map(msg => ({
        ...msg,
        fromAvatar: msg.fromUserId === adminId.value ? null : null,
        toAvatar: msg.fromUserId === adminId.value ? null : null
      }))
      await nextTick()
      scrollToBottom()
    } else {
      ElMessage.error(res.message || '加载消息记录失败')
    }
  } catch (e) {
    console.error('加载消息记录失败:', e)
    ElMessage.error('加载消息记录失败')
  } finally {
    loadingMessages.value = false
  }
}

// 发送消息
const sendMessage = async () => {
  if (!inputMessage.value.trim() || !selectedUserId.value) return
  
  sending.value = true
  try {
    const res = await adminSendChatMessage(
      selectedUserId.value,
      inputMessage.value.trim(),
      currentConversation.value?.productId
    )
    if (res.code === 200) {
      inputMessage.value = ''
      await loadMessages()
    } else {
      ElMessage.error(res.message || '发送失败')
    }
  } catch (e) {
    console.error('发送消息失败:', e)
    ElMessage.error('发送消息失败')
  } finally {
    sending.value = false
  }
}

// 滚动到底部
const scrollToBottom = () => {
  if (messagesArea.value) {
    messagesArea.value.scrollTop = messagesArea.value.scrollHeight
  }
}

// 定时刷新会话列表
let refreshTimer = null

onMounted(() => {
  // 获取当前管理员ID
  const userData = JSON.parse(localStorage.getItem('moli_mall_current_user') || '{}')
  adminId.value = userData.userInfo?.id
  
  loadConversations()
  
  // 每30秒刷新会话列表
  refreshTimer = setInterval(() => {
    if (document.visibilityState === 'visible') {
      loadConversations()
    }
  }, 30000)
})

// 组件卸载时清理
import { onUnmounted } from 'vue'
onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})

// 选中的用户变化时自动加载消息
watch(selectedUserId, (newVal) => {
  if (newVal) {
    loadMessages()
  }
})
</script>

<style scoped>
.admin-customer-service {
  height: calc(100vh - 140px);
  display: flex;
  flex-direction: column;
}

.service-header {
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 12px;
  margin-bottom: 20px;
}

.service-header h2 {
  margin: 0 0 5px 0;
  font-size: 20px;
}

.subtitle {
  margin: 0;
  font-size: 14px;
  opacity: 0.9;
}

.service-container {
  display: flex;
  flex: 1;
  gap: 20px;
  min-height: 0;
}

/* 会话列表 */
.conversation-list {
  width: 320px;
  background: white;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.list-header {
  padding: 16px;
  border-bottom: 1px solid #eee;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
}

.unread-badge {
  background: #f56c6c;
  color: white;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: normal;
}

.conversations {
  flex: 1;
  overflow-y: auto;
}

.empty-list {
  padding: 40px;
  text-align: center;
  color: #999;
}

.conversation-item {
  display: flex;
  padding: 12px 16px;
  cursor: pointer;
  transition: background 0.2s;
  border-bottom: 1px solid #f5f5f5;
}

.conversation-item:hover {
  background: #f5f7fa;
}

.conversation-item.active {
  background: #ecf5ff;
}

.conversation-item.unread {
  background: #fef0f0;
}

.conv-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  margin-right: 12px;
  object-fit: cover;
}

.conv-info {
  flex: 1;
  min-width: 0;
}

.conv-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.conv-name {
  font-weight: 500;
  color: #333;
}

.conv-time {
  font-size: 12px;
  color: #999;
}

.conv-preview {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.last-message {
  font-size: 13px;
  color: #666;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 150px;
}

.unread-count {
  background: #f56c6c;
  color: white;
  padding: 2px 6px;
  border-radius: 10px;
  font-size: 11px;
  min-width: 18px;
  text-align: center;
}

/* 聊天详情 */
.chat-detail {
  flex: 1;
  background: white;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  min-height: 0;
}

.no-selection {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.empty-chat {
  text-align: center;
  color: #999;
}

.empty-chat .icon {
  font-size: 64px;
  display: block;
  margin-bottom: 16px;
}

.empty-chat p {
  margin: 8px 0;
}

.empty-chat .tip {
  font-size: 13px;
  color: #bbb;
}

.chat-header {
  padding: 16px;
  border-bottom: 1px solid #eee;
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.user-name {
  font-weight: 600;
  color: #333;
}

.user-tip {
  font-size: 12px;
  color: #999;
}

.messages-area {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.no-messages {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
}

.message-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  max-width: 70%;
}

.message-item.mine {
  align-self: flex-end;
  flex-direction: row;
}

.message-item.theirs {
  align-self: flex-start;
  flex-direction: row;
}

.msg-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.message-bubble {
  padding: 10px 14px;
  border-radius: 12px;
  max-width: 100%;
}

.message-item.mine .message-bubble {
  background: #409eff;
  color: white;
  border-bottom-right-radius: 4px;
}

.message-item.theirs .message-bubble {
  background: #f5f7fa;
  color: #333;
  border-bottom-left-radius: 4px;
}

.message-content {
  word-break: break-word;
  line-height: 1.5;
}

.message-time {
  font-size: 11px;
  margin-top: 4px;
  opacity: 0.7;
  text-align: right;
}

.input-area {
  padding: 16px;
  border-top: 1px solid #eee;
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.input-area .el-textarea {
  flex: 1;
}

.input-area .el-button {
  height: 68px;
}
</style>
