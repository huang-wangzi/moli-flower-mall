<template>
  <div class="acquirer-messages-page">
    <!-- 左侧：会话列表 -->
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
        :key="'conv_' + conv.userId"
        class="conv-item"
        :class="{ active: activeConv && activeConv.userId === conv.userId, unread: conv.unread > 0 }"
        @click="selectConversation(conv)"
      >
        <div class="conv-avatar">
          <img :src="normalizeAvatar(conv.avatar) || defaultAvatar" />
          <span v-if="conv.unread > 0" class="unread-badge">{{ conv.unread > 99 ? '99+' : conv.unread }}</span>
        </div>
        <div class="conv-info">
          <div class="conv-name">{{ conv.username }}</div>
          <div class="conv-last-msg">{{ conv.lastMessage }}</div>
        </div>
        <div class="conv-time">{{ formatTime(conv.lastTime) }}</div>
      </div>
    </div>

    <!-- 右侧：聊天窗口 -->
    <div class="chat-panel">
      <div v-if="activeConv" class="chat-main">
        <!-- 聊天头部 -->
        <div class="chat-header">
          <img :src="normalizeAvatar(activeConv.avatar) || defaultAvatar" class="chat-avatar" />
          <span class="chat-name">{{ activeConv.username }}</span>
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
              :class="{ own: msg.fromUserId === currentUserId }"
            >
              <img
                v-if="msg.fromUserId !== currentUserId"
                :src="normalizeAvatar(msg.avatar) || defaultAvatar"
                class="msg-avatar"
              />
              <div class="message-bubble">
                <div class="message-content">{{ msg.content }}</div>
                <div class="message-time">{{ formatMsgTime(msg.createTime) }}</div>
              </div>
              <img
                v-if="msg.fromUserId === currentUserId"
                :src="userAvatar"
                class="msg-avatar"
              />
            </div>
            <div v-if="chatMessages.length === 0" class="empty-msgs">
              <p>暂无消息记录，开始对话吧</p>
            </div>
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="chat-input-area">
          <el-input
            v-model="inputMessage"
            type="textarea"
            :rows="2"
            placeholder="输入消息..."
            @keydown.enter.ctrl="sendMessage"
          />
          <el-button type="primary" @click="sendMessage" :loading="sending">
            发送
          </el-button>
        </div>
      </div>

      <!-- 未选中会话 -->
      <div v-else class="chat-empty">
        <div class="empty-icon">💬</div>
        <p>选择一个消息开始对话</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getMessageList, markMessageRead, sendChatMessage, getChatHistory } from '@/api/index.js'

const userStore = useUserStore()
const currentUserId = computed(() => userStore.userInfo?.id)

const loading = ref(false)
const msgLoading = ref(false)
const sending = ref(false)
const conversations = ref([])
const chatMessages = ref([])
const activeConv = ref(null)
const inputMessage = ref('')
const messagesContainer = ref(null)

const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
const userAvatar = computed(() => userStore.userInfo?.avatar || defaultAvatar)

const normalizeAvatar = (avatar) => {
  if (!avatar) return null
  if (avatar.startsWith('http')) return avatar
  if (avatar.startsWith('/uploads') || avatar.startsWith('/upload')) {
    return `http://localhost:8080${avatar}`
  }
  return avatar
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  if (date.toDateString() === now.toDateString()) {
    return `${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`
  }
  return `${date.getMonth() + 1}/${date.getDate()}`
}

const formatMsgTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`
}

const loadMessages = async () => {
  loading.value = true
  try {
    const userId = userStore.userInfo?.id
    if (!userId) {
      conversations.value = []
      return
    }
    const res = await getMessageList(userId)
    if (res.code === 200 && res.data) {
      // 按发送者分组，生成会话列表
      const convMap = new Map()
      for (const msg of res.data) {
        // 跳过系统消息（没有发送者的）
        if (!msg.senderId) continue

        const key = msg.senderId
        if (!convMap.has(key)) {
          convMap.set(key, {
            userId: msg.senderId,
            username: msg.senderName || '用户' + msg.senderId,
            avatar: msg.senderAvatar,
            lastMessage: msg.content,
            lastTime: msg.createTime,
            unread: msg.isRead === 0 ? 1 : 0
          })
        } else {
          const conv = convMap.get(key)
          conv.unread += (msg.isRead === 0 ? 1 : 0)
          if (new Date(msg.createTime) > new Date(conv.lastTime)) {
            conv.lastMessage = msg.content
            conv.lastTime = msg.createTime
          }
        }
      }
      conversations.value = Array.from(convMap.values())
        .sort((a, b) => new Date(b.lastTime) - new Date(a.lastTime))
    } else {
      conversations.value = []
    }
  } catch (error) {
    console.error('获取消息失败:', error)
    conversations.value = []
  } finally {
    loading.value = false
  }
}

const selectConversation = async (conv) => {
  activeConv.value = conv
  msgLoading.value = true
  chatMessages.value = []

  try {
    // 加载聊天记录
    const res = await getChatHistory(conv.userId)
    if (res.code === 200 && res.data) {
      chatMessages.value = res.data
    }

    // 标记消息已读
    const userId = userStore.userInfo?.id
    const messagesRes = await getMessageList(userId)
    if (messagesRes.code === 200 && messagesRes.data) {
      for (const msg of messagesRes.data) {
        if (msg.senderId === conv.userId && msg.isRead === 0) {
          await markMessageRead(msg.id).catch(() => {})
        }
      }
    }
    conv.unread = 0

    // 滚动到底部
    await nextTick()
    scrollToBottom()
  } catch (error) {
    console.error('加载聊天记录失败:', error)
  } finally {
    msgLoading.value = false
  }
}

const sendMessage = async () => {
  if (!inputMessage.value.trim() || !activeConv.value) return

  sending.value = true
  try {
    const res = await sendChatMessage(activeConv.value.userId, inputMessage.value.trim())

    if (res.code === 200) {
      // 添加到聊天列表
      chatMessages.value.push({
        id: res.data?.id || Date.now(),
        fromUserId: currentUserId.value,
        toUserId: activeConv.value.userId,
        content: inputMessage.value.trim(),
        createTime: new Date().toISOString(),
        avatar: userAvatar.value
      })
      inputMessage.value = ''

      // 更新会话列表
      activeConv.value.lastMessage = inputMessage.value.trim() || ''
      activeConv.value.lastTime = new Date().toISOString()

      await nextTick()
      scrollToBottom()
    } else {
      ElMessage.error(res.message || '发送失败')
    }
  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error('发送失败，请重试')
  } finally {
    sending.value = false
  }
}

const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

onMounted(() => {
  loadMessages()
})
</script>

<style scoped>
.acquirer-messages-page {
  display: flex;
  height: calc(100vh - 120px);
  background: #f5f5f5;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

/* 会话列表 */
.conversation-list {
  width: 300px;
  background: white;
  border-right: 1px solid #eee;
  display: flex;
  flex-direction: column;
}

.conv-header {
  padding: 16px;
  border-bottom: 1px solid #eee;
}

.conv-header h3 {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.loading-state {
  padding: 40px;
  text-align: center;
  color: #999;
}

.empty-conv {
  padding: 40px 16px;
  text-align: center;
  color: #999;
}

.conv-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  cursor: pointer;
  border-bottom: 1px solid #f5f5f5;
  transition: background 0.2s;
}

.conv-item:hover {
  background: #f8f8f8;
}

.conv-item.active {
  background: #e8f5e9;
}

.conv-item.unread {
  background: #f0fff0;
}

.conv-avatar {
  position: relative;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  overflow: visible;
  flex-shrink: 0;
}

.conv-avatar img {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
}

.unread-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  background: #f56c6c;
  color: white;
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 10px;
  min-width: 18px;
  text-align: center;
}

.conv-info {
  flex: 1;
  margin-left: 12px;
  overflow: hidden;
}

.conv-name {
  font-weight: 600;
  color: #333;
  font-size: 14px;
  margin-bottom: 4px;
}

.conv-last-msg {
  font-size: 12px;
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.conv-time {
  font-size: 11px;
  color: #bbb;
  flex-shrink: 0;
}

/* 聊天面板 */
.chat-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #f9f9f9;
}

.chat-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #999;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.chat-header {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  background: white;
  border-bottom: 1px solid #eee;
}

.chat-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  margin-right: 12px;
}

.chat-name {
  font-weight: 600;
  font-size: 16px;
  color: #333;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.empty-msgs {
  text-align: center;
  padding: 40px;
  color: #999;
}

.message-row {
  display: flex;
  align-items: flex-end;
  margin-bottom: 16px;
}

.message-row.own {
  flex-direction: row-reverse;
}

.msg-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  margin: 0 8px;
  flex-shrink: 0;
}

.message-bubble {
  max-width: 70%;
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
  word-break: break-word;
}

.message-time {
  font-size: 10px;
  color: #999;
  margin-top: 4px;
  text-align: right;
}

.message-row.own .message-time {
  color: rgba(255, 255, 255, 0.7);
}

.chat-input-area {
  display: flex;
  gap: 12px;
  padding: 12px 20px;
  background: white;
  border-top: 1px solid #eee;
}

.chat-input-area .el-textarea {
  flex: 1;
}

.chat-input-area .el-button {
  height: 60px;
  padding: 0 24px;
}

@media (max-width: 768px) {
  .acquirer-messages-page {
    flex-direction: column;
  }

  .conversation-list {
    width: 100%;
    height: 40%;
  }

  .chat-panel {
    height: 60%;
  }
}
</style>