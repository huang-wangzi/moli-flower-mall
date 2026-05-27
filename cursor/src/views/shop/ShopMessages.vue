<template>
  <div class="shop-messages-page">
    <!-- 左侧：会话列表-->
    <div class="conversation-list">
      <div class="conv-header">
        <h3>消息中心</h3>
      </div>
      <div v-if="loading" class="loading-state">
        <el-icon class="is-loading"><Loading /></el-icon>
      </div>
      <div v-else-if="conversations.length === 0" class="empty-conv">
        <p>暂无消息</p>
      </div>
      <div
        v-else
        v-for="conv in conversations"
        :key="conv.type === 'broadcast' ? 'broadcast_' + conv.msgId : 'chat_' + conv.userId"
        class="conv-item"
        :class="{ active: isActiveConv(conv), unread: conv.unread > 0 }"
        @click="selectConversation(conv)"
      >
        <!-- 系统消息/广播：显示喇叭图标-->
        <div class="conv-avatar" v-if="conv.type === 'broadcast'">
          <div class="broadcast-avatar">📢</div>
          <span v-if="conv.unread > 0" class="unread-badge">{{ conv.unread > 99 ? '99+' : conv.unread }}</span>
        </div>
        <!-- 普通聊天-->
        <div class="conv-avatar" v-else>
          <img :src="normalizeAvatar(conv.avatar) || defaultAvatar" />
          <span v-if="conv.unread > 0" class="unread-badge">{{ conv.unread > 99 ? '99+' : conv.unread }}</span>
        </div>
        <div class="conv-info">
          <div class="conv-name">
            <span v-if="conv.type === 'broadcast'">📢 {{ conv.title || '系统消息' }}</span>
            <span v-else>{{ conv.username }}</span>
          </div>
          <div class="conv-last-msg">{{ conv.lastMessage }}</div>
        </div>
        <div class="conv-time">{{ formatTime(conv.lastTime) }}</div>
      </div>
    </div>

    <!-- 右侧：聊天窗口-->
    <div class="chat-panel">
      <!-- 选中会话 -->
      <div v-if="activeConv" class="chat-main">
        <!-- 聊天头部 -->
        <div class="chat-header">
          <img v-if="activeConv.type !== 'broadcast'" :src="normalizeAvatar(activeConv.avatar) || defaultAvatar" class="chat-avatar" />
          <div v-else class="broadcast-header-icon">📢</div>
          <span class="chat-name">
            <span v-if="activeConv.type === 'broadcast'">{{ activeConv.title || '系统消息' }}</span>
            <span v-else>{{ activeConv.username }}</span>
          </span>
        </div>

        <!-- 消息区域 -->
        <div class="chat-messages" ref="messagesContainer">
          <div v-if="msgLoading" class="loading-state">
            <el-icon class="is-loading"><Loading /></el-icon>
          </div>
          <div v-else>
            <div
              v-for="msg in chatMessages"
              :key="msg.id"
              class="message-row"
              :class="{ own: msg.fromUserId === currentShopId }"
            >
              <!-- 系统消息（管理员发送的原始广播）居中显示 -->
              <div v-if="msg.isSystem" class="system-message-bubble">
                <div class="system-message-title">{{ msg.title || '系统消息' }}</div>
                <div class="system-message-content">{{ msg.content }}</div>
                <div class="system-message-time">{{ formatMsgTime(msg.createTime) }}</div>
              </div>
              <!-- 普通消息：左右气泡 -->
              <template v-else>
                <img
                  v-if="msg.fromUserId !== currentShopId"
                  :src="normalizeAvatar(msg.avatar) || defaultAvatar"
                  class="msg-avatar"
                />
                <div class="message-bubble">
                  <div class="message-content">{{ msg.content }}</div>
                  <div class="message-time">{{ formatMsgTime(msg.createTime) }}</div>
                </div>
                <img
                  v-if="msg.fromUserId === currentShopId"
                  :src="shopAvatar"
                  class="msg-avatar"
                />
              </template>
            </div>
            <div v-if="chatMessages.length === 0" class="empty-msgs">
              <p>暂无消息记录，选择一个会话开始</p>
            </div>
          </div>
        </div>

        <!-- 输入区域（普通聊天可输入，广播只显示提示）-->
        <div class="chat-input-area" v-if="activeConv.type !== 'broadcast'">
          <input
            type="text"
            v-model="inputMessage"
            placeholder="输入消息..."
            @keyup.enter="sendMessage"
          />
          <button class="send-btn" @click="sendMessage" :disabled="sending">
            {{ sending ? '发送中...' : '发送' }}
          </button>
        </div>
        <!-- 广播会话：商家回复-->
        <div class="chat-input-area" v-else>
          <input
            type="text"
            v-model="broadcastReply"
            placeholder="输入回复内容..."
            @keyup.enter="sendBroadcastReply"
          />
          <button class="send-btn" @click="sendBroadcastReply" :disabled="sending">
            {{ sending ? '发送中...' : '回复' }}
          </button>
        </div>
      </div>

      <!-- 未选中会话 -->
      <div v-else class="no-conv-selected">
        <div class="no-conv-icon">💬</div>
        <p>选择一个会话开始</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { getChatConversations, getChatHistory, sendChatMessage, markChatConversationRead, getBroadcastHistory, sendBroadcastReply as apiSendBroadcastReply } from '@/api'
import { useUserStore } from '@/stores/user'
import { normalizeUploadUrl } from '@/utils/imageUrl'

const userStore = useUserStore()

const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
const shopAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'

// 当前商家ID
const currentShopId = ref(null)

// 会话列表
const conversations = ref([])
const loading = ref(false)

// 消息列表
const chatMessages = ref([])
const msgLoading = ref(false)
const messagesContainer = ref(null)

// 输入
const inputMessage = ref('')
const broadcastReply = ref('')
const sending = ref(false)

// 当前选中的会话
const activeConv = ref(null)

// 定时刷新
let pollInterval = null

// 判断当前是否选中某会话
const isActiveConv = (conv) => {
  if (!activeConv.value) return false
  if (conv.type === 'broadcast') return activeConv.value.type === 'broadcast' && activeConv.value.msgId === conv.msgId
  return activeConv.value.type !== 'broadcast' && activeConv.value.userId === conv.userId
}

// 加载会话列表
const loadConversations = async () => {
  const shopId = userStore.userInfo?.id
  if (!shopId) return
  currentShopId.value = shopId

  try {
    const res = await getChatConversations()
    if (res.code === 200) {
      conversations.value = res.data || []
    }
  } catch (e) {
    console.error('加载会话列表失败', e)
  }
}

// 选中会话，加载聊天记录
const selectConversation = async (conv) => {
  activeConv.value = conv
  msgLoading.value = true
  const shopId = currentShopId.value
  try {
    if (conv.type === 'broadcast') {
      // 加载广播消息对话
      const res = await getBroadcastHistory(conv.msgId)
      if (res.code === 200) {
        chatMessages.value = res.data || []
        nextTick(() => { scrollToBottom() })
      }
    } else {
      // 加载普通聊天记录
      const res = await getChatHistory(conv.userId)
      if (res.code === 200) {
        chatMessages.value = res.data || []
        nextTick(() => { scrollToBottom() })
      }
      // 标记已读
      if (shopId) {
        try {
          await markChatConversationRead(conv.userId, shopId)
          conv.unread = 0
          await loadConversations()
          const refreshed = conversations.value.find(c => c.userId === conv.userId)
          if (refreshed) { activeConv.value = refreshed }
        } catch (e) { console.error('标记已读失败', e) }
      }
    }
  } catch (e) {
    console.error('加载聊天记录失败', e)
  } finally {
    msgLoading.value = false
  }
}

// 发送普通消息
const sendMessage = async () => {
  if (!inputMessage.value.trim() || !activeConv.value || activeConv.value.type === 'broadcast') return
  if (sending.value) return

  sending.value = true
  const text = inputMessage.value.trim()
  try {
    const res = await sendChatMessage(
      activeConv.value.userId,
      text,
      activeConv.value.productId || null
    )
    if (res.code === 200) {
      chatMessages.value.push(res.data)
      inputMessage.value = ''
      nextTick(() => { scrollToBottom() })
    } else {
      ElMessage.error(res.message || '发送失败')
    }
  } catch (e) {
    console.error('发送消息失败', e)
    ElMessage.error('发送失败，请重试')
  } finally {
    sending.value = false
  }
}

// 发送广播回复
const sendBroadcastReply = async () => {
  if (!broadcastReply.value.trim() || !activeConv.value || activeConv.value.type !== 'broadcast') return
  if (sending.value) return

  sending.value = true
  const text = broadcastReply.value.trim()
  try {
    const res = await apiSendBroadcastReply(activeConv.value.msgId, text)
    if (res.code === 200) {
      chatMessages.value.push(res.data)
      broadcastReply.value = ''
      nextTick(() => { scrollToBottom() })
      await loadConversations()
    } else {
      ElMessage.error(res.message || '回复失败')
    }
  } catch (e) {
    console.error('回复失败', e)
    ElMessage.error('回复失败，请重试')
  } finally {
    sending.value = false
  }
}

// 头像标准化
const normalizeAvatar = (avatar) => {
  if (!avatar) return null
  return normalizeUploadUrl(avatar)
}

// 滚动到底部
const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// 格式化会话列表时间
const formatTime = (time) => {
  if (!time) return ''
  const d = new Date(time)
  const diff = Date.now() - d.getTime()
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`
  return d.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
}

// 格式化消息时间
const formatMsgTime = (time) => {
  if (!time) return ''
  const d = new Date(time)
  const diff = Date.now() - d.getTime()
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`
  return d.toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

onMounted(() => {
  loading.value = true
  loadConversations().finally(() => { loading.value = false })
  pollInterval = setInterval(loadConversations, 8000)
})

onUnmounted(() => {
  if (pollInterval) clearInterval(pollInterval)
})
</script>

<style scoped>
.shop-messages-page {
  display: flex;
  height: calc(100vh - 200px);
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0,0,0,0.05);
}

/* 会话列表 */
.conversation-list {
  width: 320px;
  border-right: 1px solid #eee;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.conv-header {
  padding: 16px 20px;
  border-bottom: 1px solid #eee;
}

.conv-header h3 {
  margin: 0;
  font-size: 16px;
  color: #333;
}

.conv-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  cursor: pointer;
  border-bottom: 1px solid #f5f5f5;
  transition: background 0.2s;
}

.conv-item:hover {
  background: #f9f9f9;
}

.conv-item.active {
  background: #e8f4ff;
  border-left: 3px solid #409eff;
}

.conv-item.unread {
  background: #fff8e6;
}

.conv-avatar {
  position: relative;
  flex-shrink: 0;
}

.conv-avatar img {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  object-fit: cover;
}

.broadcast-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: #fff3e0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
}

.unread-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  background: #e74c3c;
  color: white;
  border-radius: 10px;
  padding: 1px 6px;
  font-size: 11px;
  min-width: 18px;
  text-align: center;
}

.conv-info {
  flex: 1;
  min-width: 0;
}

.conv-name {
  font-weight: 600;
  font-size: 14px;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.conv-last-msg {
  font-size: 12px;
  color: #999;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-top: 4px;
}

.conv-time {
  font-size: 11px;
  color: #bbb;
  flex-shrink: 0;
}

/* 聊天窗口 */
.chat-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.no-conv-selected {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #999;
}

.no-conv-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-header {
  height: 60px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 20px;
  border-bottom: 1px solid #eee;
  background: #fafafa;
}

.chat-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
}

.broadcast-header-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #fff3e0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.chat-name {
  font-weight: 600;
  font-size: 16px;
  color: #333;
}

/* 消息列表 */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #f0f2f5;
}

.message-row {
  display: flex;
  align-items: flex-end;
  gap: 10px;
  margin-bottom: 12px;
}

.message-row.own {
  flex-direction: row-reverse;
}

.msg-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.message-bubble {
  max-width: 65%;
  padding: 10px 14px;
  border-radius: 12px;
  background: white;
  position: relative;
}

.message-row.own .message-bubble {
  background: #4A7C59;
  color: white;
}

.message-content {
  font-size: 14px;
  line-height: 1.5;
  word-break: break-all;
}

.message-time {
  font-size: 11px;
  color: #aaa;
  margin-top: 4px;
  text-align: right;
}

.message-row.own .message-time {
  color: rgba(255,255,255,0.6);
}

/* 系统消息气泡 */
.system-message-bubble {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px 20px;
  margin: 8px 0;
}

.system-message-title {
  font-size: 14px;
  font-weight: bold;
  color: #333;
  margin-bottom: 4px;
}

.system-message-content {
  font-size: 14px;
  color: #555;
  line-height: 1.5;
  text-align: center;
  background: #fff8e6;
  border: 1px solid #ffe58f;
  border-radius: 8px;
  padding: 12px 20px;
  max-width: 80%;
}

.system-message-time {
  font-size: 11px;
  color: #bbb;
  margin-top: 4px;
}

/* 输入区域 */
.chat-input-area {
  height: 60px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 16px;
  border-top: 1px solid #eee;
  background: white;
}

.chat-input-area input {
  flex: 1;
  height: 38px;
  border: 1px solid #ddd;
  border-radius: 20px;
  padding: 0 16px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
}

.chat-input-area input:focus {
  border-color: #4A7C59;
}

.send-btn {
  height: 38px;
  padding: 0 20px;
  background: #4A7C59;
  color: white;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.2s;
}

.send-btn:hover {
  background: #3d6b4a;
}

.send-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

/* 通用 */
.loading-state {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  color: #999;
}

.empty-conv, .empty-msgs {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  color: #999;
  font-size: 14px;
}
</style>
