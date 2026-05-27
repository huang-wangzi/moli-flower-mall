<template>
  <div class="admin-messages">
    <div class="page-header">
      <h2>系统消息</h2>
      <p class="subtitle">向用户发送系统广播消息，查看商家回复</p>
    </div>

    <div class="toolbar">
      <el-button type="primary" @click="showSendDialog">发送系统消息</el-button>
    </div>

    <div class="table-container">
      <el-table :data="messages" border v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="title" label="消息标题" min-width="200" />
        <el-table-column prop="content" label="消息内容" min-width="280" show-overflow-tooltip />
        <el-table-column label="范围" width="100">
          <template #default="{ row }">
            <el-tag :type="scopeTagType(row.scope)" size="small">{{ scopeLabel(row.scope) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发送时间" width="160" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="info" size="small" @click="viewReplies(row)">查看回复</el-button>
            <el-button type="danger" size="small" @click="deleteMessage(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 发送广播消息 -->
    <el-dialog v-model="sendVisible" title="发送系统消息（广播）" width="500px">
      <div class="send-tip">
        <p>💡 广播消息只存一条记录，商家可在消息中心查看并回复。</p>
      </div>
      <el-form :model="sendForm" label-width="80px">
        <el-form-item label="发送范围">
          <el-select v-model="sendForm.scope" style="width: 100%;">
            <el-option label="全部用户" value="all" />
            <el-option label="普通用户" value="users" />
            <el-option label="商家" value="shops" />
          </el-select>
        </el-form-item>
        <el-form-item label="消息标题">
          <el-input v-model="sendForm.title" placeholder="请输入消息标题" />
        </el-form-item>
        <el-form-item label="消息内容">
          <el-input v-model="sendForm.content" type="textarea" :rows="4" placeholder="请输入消息内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="sendVisible = false">取消</el-button>
        <el-button type="primary" @click="sendMessage">发送</el-button>
      </template>
    </el-dialog>

    <!-- 查看回复弹窗 -->
    <el-dialog v-model="repliesVisible" :title="'回复查看：' + (selectedMsg?.title || '')" width="600px" destroy-on-close>
      <div class="replies-list" v-if="replies.length > 0">
        <div v-for="reply in replies" :key="reply.id" class="reply-item">
          <img :src="normalizeAvatar(reply.avatar)" class="reply-avatar" />
          <div class="reply-content">
            <div class="reply-header">
              <span class="reply-name">{{ reply.senderName }}</span>
              <span class="reply-time">{{ formatTime(reply.createTime) }}</span>
            </div>
            <div class="reply-text">{{ reply.content }}</div>
          </div>
        </div>
      </div>
      <div v-else class="empty-replies">
        <p>暂无回复</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAdminSystemMessages, sendAdminSystemMessage, deleteAdminSystemMessage, getBroadcastHistory } from '@/api'
import { normalizeUploadUrl } from '@/utils/imageUrl'

const loading = ref(false)
const sendVisible = ref(false)
const repliesVisible = ref(false)
const messages = ref([])
const selectedMsg = ref(null)
const replies = ref([])

const sendForm = reactive({
  scope: 'all',
  title: '',
  content: ''
})

const normalizeAvatar = (avatar) => {
  if (!avatar) return 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
  return normalizeUploadUrl(avatar) || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
}

const scopeLabel = (scope) => ({ all: '全部', users: '普通用户', shops: '商家' })[scope] || '全部'
const scopeTagType = (scope) => ({ all: 'primary', users: 'success', shops: 'warning' })[scope] || 'info'

const loadMessages = async () => {
  loading.value = true
  try {
    const res = await getAdminSystemMessages()
    if (res.code === 200) {
      messages.value = res.data || []
    }
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

const showSendDialog = () => {
  sendForm.title = ''
  sendForm.content = ''
  sendForm.scope = 'all'
  sendVisible.value = true
}

const sendMessage = async () => {
  if (!sendForm.title || !sendForm.content) {
    ElMessage.warning('请填写完整信息')
    return
  }
  try {
    const res = await sendAdminSystemMessage(sendForm.scope, sendForm.title, sendForm.content)
    if (res.code === 200) {
      ElMessage.success(res.message || '消息发送成功')
      sendVisible.value = false
      loadMessages()
    } else {
      ElMessage.error(res.message || '发送失败')
    }
  } catch (e) { ElMessage.error('发送失败，请重试') }
}

const viewReplies = async (msg) => {
  selectedMsg.value = msg
  repliesVisible.value = true
  try {
    const res = await getBroadcastHistory(msg.id)
    if (res.code === 200) {
      replies.value = (res.data || []).filter(item => !item.isSystem)
    }
  } catch (e) { console.error(e); replies.value = [] }
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

const deleteMessage = async (msg) => {
  try {
    const res = await deleteAdminSystemMessage(msg.id)
    if (res.code === 200) {
      messages.value = messages.value.filter(m => m.id !== msg.id)
      ElMessage.success('删除成功')
    }
  } catch (e) { ElMessage.error('删除失败') }
}

onMounted(() => {
  loadMessages()
})
</script>

<style scoped>
.admin-messages {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.send-tip {
  background: #f6ffed;
  border: 1px solid #b7eb8f;
  border-radius: 8px;
  padding: 12px 14px;
  margin-bottom: 16px;
  font-size: 13px;
  color: #52c41a;
}

.replies-list {
  max-height: 400px;
  overflow-y: auto;
}

.reply-item {
  display: flex;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.reply-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.reply-content {
  flex: 1;
}

.reply-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 4px;
}

.reply-name {
  font-weight: 600;
  font-size: 14px;
  color: #333;
}

.reply-time {
  font-size: 12px;
  color: #999;
}

.reply-text {
  font-size: 14px;
  color: #555;
  line-height: 1.5;
}

.empty-replies {
  text-align: center;
  padding: 30px;
  color: #999;
}
</style>
