<template>
  <div class="chat-broadcast-page">
    <!-- 聊天头部 -->
    <div class="chat-header">
      <button class="back-btn" @click="$router.back()">←</button>
      <div class="chat-title">
        <div class="broadcast-icon">📢</div>
        <span class="chat-name">{{ broadcastTitle || '系统消息' }}</span>
      </div>
    </div>

    <!-- 消息区域 -->
    <div class="chat-messages" ref="messagesContainer">
      <div v-if="loading" class="loading-state">
        <el-icon class="is-loading"><Loading /></el-icon>
        <p>加载中...</p>
      </div>
      <div v-else>
        <div
          v-for="msg in messages"
          :key="msg.id"
          class="message-row"
          :class="{ own: msg.fromUserId === currentUserId }"
        >
          <!-- 系统消息（管理员发送的原始广播）居中显示 -->
          <div v-if="msg.isSystem" class="system-message-bubble">
            <div class="system-message-title">{{ msg.title || '系统消息' }}</div>
            <div class="system-message-content">{{ msg.content }}</div>
            <div class="system-message-time">{{ formatTime(msg.createTime) }}</div>
          </div>
          <!-- 普通消息：左右气泡 -->
          <template v-else>
            <img
              v-if="msg.fromUserId !== currentUserId"
              :src="normalizeAvatar(msg.avatar) || defaultAvatar"
              class="msg-avatar"
            />
            <div class="message-bubble">
              <div class="message-sender" v-if="msg.senderName">{{ msg.senderName }}</div>
              <div class="message-content">{{ msg.content }}</div>
              <div class="message-time">{{ formatTime(msg.createTime) }}</div>
            </div>
            <img
              v-if="msg.fromUserId === currentUserId"
              :src="userAvatar || defaultAvatar"
              class="msg-avatar own-avatar"
            />
          </template>
        </div>
        <div v-if="!loading && messages.length === 0" class="empty-state">
          <p>暂无消息记录</p>
        </div>
      </div>
    </div>

    <!-- 输入区域 -->
    <div class="chat-input-area">
      <input
        type="text"
        v-model="inputMessage"
        placeholder="输入回复内容..."
        @keyup.enter="sendReply"
      />
      <button class="send-btn" @click="sendReply" :disabled="sending">
        {{ sending ? '发送中...' : '发送' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { getBroadcastHistory, sendBroadcastReply as apiSendBroadcastReply } from '@/api'
import { useUserStore } from '@/stores/user'
import { normalizeUploadUrl } from '@/utils/imageUrl'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'

// 广播消息ID
const msgId = computed(() => {
  return Number(route.params.msgId) || null
})

// 当前用户ID
const currentUserId = computed(() => {
  return userStore.userInfo?.id || null
})

// 用户头像
const userAvatar = computed(() => {
  return normalizeUploadUrl(userStore.userInfo?.avatar) || defaultAvatar
})

// 广播标题
const broadcastTitle = ref('')

// 消息列表
const messages = ref([])

// 输入的消息
const inputMessage = ref('')

// 加载状态
const loading = ref(false)

// 发送状态
const sending = ref(false)

// 消息容器引用
const messagesContainer = ref(null)

// 头像标准化
const normalizeAvatar = (avatar) => {
  if (!avatar) return null
  return normalizeUploadUrl(avatar)
}

// 加载广播消息
const loadBroadcast = async () => {
  if (!msgId.value) {
    ElMessage.error('消息ID无效')
    router.back()
    return
  }

  loading.value = true
  try {
    const res = await getBroadcastHistory(msgId.value)
    if (res.code === 200) {
      messages.value = res.data || []
      // 设置标题为第一条系统消息的标题
      const systemMsg = messages.value.find(m => m.isSystem)
      if (systemMsg?.title) {
        broadcastTitle.value = systemMsg.title
      }
      nextTick(() => { scrollToBottom() })
    } else {
      ElMessage.error(res.message || '加载消息失败')
    }
  } catch (error) {
    console.error('加载广播消息失败', error)
    ElMessage.error('加载消息失败')
  } finally {
    loading.value = false
  }
}

// 发送回复
const sendReply = async () => {
  if (!inputMessage.value.trim()) return
  if (sending.value) return

  sending.value = true
  const text = inputMessage.value.trim()
  try {
    const res = await apiSendBroadcastReply(msgId.value, text)
    if (res.code === 200) {
      messages.value.push(res.data)
      inputMessage.value = ''
      nextTick(() => { scrollToBottom() })
    } else {
      ElMessage.error(res.message || '回复失败')
    }
  } catch (error) {
    console.error('回复失败', error)
    ElMessage.error('回复失败，请重试')
  } finally {
    sending.value = false
  }
}

// 格式化时间显示
const formatTime = (time) => {
  if (!time) return ''
  const msgTime = new Date(time)
  const now = new Date()
  const diff = now - msgTime
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return msgTime.toLocaleString('zh-CN')
}

// 滚动到底部
const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// 定时刷新
let pollInterval = null

onMounted(() => {
  loadBroadcast()
  // 每10秒刷新一次
  pollInterval = setInterval(loadBroadcast, 10000)
})

onUnmounted(() => {
  if (pollInterval) clearInterval(pollInterval)
})
</script>

<style scoped>
.chat-broadcast-page {
  height: calc(100vh - 150px);
  display: flex;
  flex-direction: column;
  background: #e3e5e7;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 8px 8px 16px #b6b9ba, -8px -8px 16px #fafafd;
}

/* 聊天头部 */
.chat-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  background: #e3e5e7;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.back-btn {
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 50%;
  background: #e3e5e7;
  box-shadow: 3px 3px 6px #b6b9ba, -3px -3px 6px #fafafd;
  cursor: pointer;
  font-size: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.back-btn:hover {
  transform: translateX(-3px);
}

.chat-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.broadcast-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #fff3e0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.chat-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

/* 消息区域 */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.message-row.own {
  flex-direction: row-reverse;
}

.msg-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  flex-shrink: 0;
}

.message-bubble {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 16px;
  background: white;
  box-shadow: 2px 2px 8px rgba(0, 0, 0, 0.08);
}

.message-row.own .message-bubble {
  background: #4A7C59;
  color: white;
}

.message-sender {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}

.message-row.own .message-sender {
  color: rgba(255, 255, 255, 0.7);
}

.message-content {
  font-size: 14px;
  line-height: 1.5;
  word-break: break-word;
}

.message-time {
  font-size: 11px;
  color: #999;
  margin-top: 6px;
  text-align: right;
}

.message-row.own .message-time {
  color: rgba(255, 255, 255, 0.7);
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
  display: flex;
  gap: 12px;
  padding: 16px 20px;
  background: #e3e5e7;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
}

.chat-input-area input {
  flex: 1;
  padding: 12px 16px;
  border: none;
  border-radius: 25px;
  background: #e3e5e7;
  box-shadow: inset 3px 3px 6px #b6b9ba, inset -3px -3px 6px #fafafd;
  font-size: 14px;
  outline: none;
}

.send-btn {
  padding: 12px 24px;
  border: none;
  border-radius: 25px;
  background: linear-gradient(145deg, #4A7C59, #3d6b4a);
  color: white;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.send-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(74, 124, 89, 0.4);
}

.send-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

/* 加载状态 */
.loading-state {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}

.loading-state .el-icon {
  font-size: 32px;
  margin-bottom: 12px;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}
</style>
