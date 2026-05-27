<template>
  <div class="chat-page">
    <!-- 聊天头部 -->
    <div class="chat-header">
      <button class="back-btn" @click="$router.back()">←</button>
      <div class="chat-title">
        <img :src="normalizeUploadUrl(chatData.avatar)" class="chat-avatar" />
        <span class="chat-name">{{ chatData.name }}</span>
      </div>
    </div>

    <!-- 聊天消息区域 -->
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
          <img
            v-if="msg.fromUserId !== currentUserId"
            :src="normalizeUploadUrl(chatData.avatar) || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'"
            class="msg-avatar"
          />
          <div class="message-bubble">
            <div class="message-content">{{ msg.content }}</div>
            <div class="message-time">{{ formatTime(msg.createTime) }}</div>
          </div>
          <img
            v-if="msg.fromUserId === currentUserId"
            :src="userAvatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'"
            class="msg-avatar own-avatar"
          />
        </div>
        <div v-if="!loading && messages.length === 0" class="empty-state">
          <p>暂无消息记录，开始聊天吧~</p>
        </div>
      </div>
    </div>

    <!-- 输入区域 -->
    <div class="chat-input-area">
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
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { getChatHistory, getChatHistoryByProduct, sendChatMessage, getProductDetail, getUserInfoById, markChatConversationRead, getSupportAdminId } from '@/api'
import { useUserStore } from '@/stores/user'
import { normalizeUploadUrl } from '@/utils/imageUrl'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 获取对方用户ID
const targetUserId = computed(() => {
  return Number(route.params.id) || null
})

// 当前用户ID
const currentUserId = computed(() => {
  return userStore.userInfo?.id || null
})

// 用户头像（响应式，修改后立即生效）
const userAvatar = computed(() => normalizeUploadUrl(userStore.userInfo?.avatar) || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png')

// 聊天数据（对方信息）
const chatData = ref({
  name: '聊天',
  avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
})

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

// 商品ID（如果有）
const productId = computed(() => {
  return route.query.productId ? Number(route.query.productId) : null
})

// 管理员ID缓存
let adminIdCache = null

// 加载聊天数据
const loadChat = async () => {
  if (!targetUserId.value || !currentUserId.value) {
    return
  }

  loading.value = true
  try {
    // 先获取管理员ID，用于判断当前聊天对象是商家还是客服
    if (adminIdCache === null) {
      try {
        const adminRes = await getSupportAdminId()
        if (adminRes.code === 200) {
          adminIdCache = adminRes.data
        }
      } catch (e) { /* ignore */ }
    }

    // 判断是联系商家还是联系客服
    const isContactingAdmin = adminIdCache !== null && targetUserId.value === adminIdCache
    chatData.value.name = isContactingAdmin ? '平台客服' : '商家'

    // 获取聊天历史
    // 管理员会话：通过 /history/{adminId} 单独拉取，不用商品维度
    // 商家会话：有 productId 时按商品维度拉取（只看该商品相关对话）
    if (productId.value && !isContactingAdmin) {
      const res = await getChatHistoryByProduct(productId.value)
      if (res.code === 200) {
        messages.value = res.data || []
      }
    } else {
      const res = await getChatHistory(targetUserId.value)
      if (res.code === 200) {
        messages.value = res.data || []
      }
    }

    // 如果有商品ID且是联系商家，获取商家名称
    if (productId.value && !isContactingAdmin) {
      try {
        const productRes = await getProductDetail(productId.value)
        if (productRes.code === 200 && productRes.data?.product) {
          chatData.value.name = productRes.data.product.shopName || '商家'
        }
      } catch (e) {
        console.error('获取商品信息失败', e)
      }
    }

    // 如果是联系客服，获取客服信息
    if (isContactingAdmin) {
      try {
        const userRes = await getUserInfoById(targetUserId.value)
        if (userRes.code === 200 && userRes.data) {
          chatData.value.name = '平台客服'
          chatData.value.avatar = normalizeUploadUrl(userRes.data.avatar) || chatData.value.avatar
        }
      } catch (e) {
        console.error('获取客服信息失败', e)
      }
    } else if (!productId.value) {
      // 非商品页面联系商家
      try {
        const userRes = await getUserInfoById(targetUserId.value)
        if (userRes.code === 200 && userRes.data) {
          chatData.value.name = userRes.data.username || '商家'
          chatData.value.avatar = normalizeUploadUrl(userRes.data.avatar) || chatData.value.avatar
        }
      } catch (e) {
        console.error('获取用户信息失败', e)
      }
    }

    // 加载完聊天记录后，标记与该用户的消息为已读
    await markConversationAsRead()
  } catch (error) {
    console.error('加载聊天失败', error)
    ElMessage.error('加载聊天记录失败')
  } finally {
    loading.value = false
    nextTick(() => {
      scrollToBottom()
    })
  }
}

// 标记与该用户的聊天消息为已读
const markConversationAsRead = async () => {
  if (!currentUserId.value || !targetUserId.value) return
  try {
    await markChatConversationRead(targetUserId.value, currentUserId.value)
    // 触发事件通知更新未读数
    window.dispatchEvent(new CustomEvent('chat-read', { detail: { userId: targetUserId.value } }))
  } catch (e) {
    console.error('标记已读失败', e)
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

// 发送消息
const sendMessage = async () => {
  if (!inputMessage.value.trim()) return
  if (!targetUserId.value) {
    ElMessage.warning('无法获取聊天对象')
    return
  }

  sending.value = true
  try {
    const res = await sendChatMessage(
      targetUserId.value,
      inputMessage.value.trim(),
      productId.value
    )
    
    if (res.code === 200) {
      inputMessage.value = ''
      messages.value.push(res.data)
      nextTick(() => {
        scrollToBottom()
      })
    } else {
      ElMessage.error(res.message || '发送失败')
    }
  } catch (error) {
    console.error('发送消息失败', error)
    ElMessage.error('发送失败，请重试')
  } finally {
    sending.value = false
  }
}

// 滚动到底部
const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

onMounted(() => {
  loadChat()
})
</script>

<style scoped>
.chat-page {
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

.chat-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
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
.msg-avatar.own-avatar {
  /* 自己的头像在气泡右侧，不需要额外样式 */
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
