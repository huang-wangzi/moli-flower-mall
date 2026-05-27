import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getMessageList, markMessageRead, markAllMessageRead, deleteMessage as apiDeleteMessage } from '@/api'

// 类型数字 → 字符串别名
const TYPE_MAP = {
  'all': null,
  'logistics': 2,   // 订单通知
  'system': 1,      // 系统通知
  'chat': 5,        // 聊天消息
  'review': 3       // 评价提醒
}

export const useMessageStore = defineStore('message', () => {
  const messages = ref([])

  // 从后端加载真实消息
  const loadMessages = async (userId) => {
    try {
      const res = await getMessageList(userId)
      if (res.code === 200) {
        messages.value = res.data || []
      }
    } catch (e) {
      console.error('加载消息失败', e)
      messages.value = []
    }
  }

  // 未读数量
  const unreadCount = computed(() => {
    return messages.value.filter(msg => msg.isRead === 0 || msg.isRead === false).length
  })

  // 按类型筛选
  const getMessagesByType = (type) => {
    const targetType = TYPE_MAP[type] ?? null
    if (targetType === null) return messages.value
    return messages.value.filter(msg => msg.type === targetType)
  }

  // 标记单条已读
  const markAsRead = async (id) => {
    try {
      await markMessageRead(id)
    } catch (e) {
      // ignore
    }
    const msg = messages.value.find(m => m.id === id)
    if (msg) msg.isRead = 1
  }

  // 全部标记已读
  const markAllAsRead = async (userId) => {
    try {
      await markAllMessageRead(userId)
    } catch (e) {
      // ignore
    }
    messages.value.forEach(msg => { msg.isRead = 1 })
  }

  // 删除消息
  const deleteMessage = async (id) => {
    try {
      await apiDeleteMessage(id)
    } catch (e) {
      // ignore
    }
    const idx = messages.value.findIndex(m => m.id === id)
    if (idx > -1) messages.value.splice(idx, 1)
  }

  return {
    messages,
    unreadCount,
    loadMessages,
    getMessagesByType,
    markAsRead,
    markAllAsRead,
    deleteMessage
  }
})
