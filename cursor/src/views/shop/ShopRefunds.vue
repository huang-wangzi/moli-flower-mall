<template>
  <div class="page-wrapper">
    <div class="page-header">
      <h2>售后管理</h2>
      <p class="subtitle">管理用户的售后申请，处理退款请求</p>
    </div>

    <!-- 统计卡片（可点击筛选） -->
    <div class="stats-grid">
      <div class="stat-card pending clickable" @click="filterStatus = 0">
        <div class="stat-icon">⏳</div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.pending }}</span>
          <span class="stat-label">待处理</span>
        </div>
      </div>
      <div class="stat-card processing clickable" @click="filterStatus = 1">
        <div class="stat-icon">🔄</div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.processing }}</span>
          <span class="stat-label">处理中</span>
        </div>
      </div>
      <div class="stat-card approved clickable" @click="filterStatus = 2">
        <div class="stat-icon">✅</div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.approved }}</span>
          <span class="stat-label">已同意</span>
        </div>
      </div>
      <div class="stat-card rejected clickable" @click="filterStatus = 3">
        <div class="stat-icon">❌</div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.rejected }}</span>
          <span class="stat-label">已拒绝</span>
        </div>
      </div>
    </div>

    <!-- 售后列表 -->
    <div class="table-container">
      <el-table :data="filteredList" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="订单号" width="120">
          <template #default="{ row }">
            <span>{{ row.orderId || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="售后类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.type === 0 ? 'warning' : 'info'" size="small">
              {{ row.type === 0 ? '仅退款' : '退货退款' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="退款金额" width="120">
          <template #default="{ row }">
            <span class="amount">¥{{ Number(row.amount || 0).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="退款原因" min-width="150">
          <template #default="{ row }">
            <span class="reason">{{ row.reason || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="详细说明" min-width="150">
          <template #default="{ row }">
            <span class="desc">{{ row.description || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="申请时间" width="160">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button type="success" size="small" @click="handleRefund(row, 2)">同意</el-button>
              <el-button type="danger" size="small" @click="handleRefund(row, 3)">拒绝</el-button>
            </template>
            <template v-else-if="row.status === 4">
              <span class="text-muted">已完成</span>
            </template>
            <template v-else>
              <span class="text-muted">已处理</span>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="!loading && refundList.length === 0" class="empty-state">
        <div class="empty-icon">📦</div>
        <p>暂无售后申请</p>
      </div>
    </div>

    <!-- 处理对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="400px">
      <div class="dialog-content">
        <p v-if="dialogAction === 2">确定同意此退款申请？退款金额：<strong>¥{{ currentRefund?.amount }}</strong></p>
        <p v-else>确定拒绝此退款申请？</p>
        <el-input
          v-model="dialogRemark"
          type="textarea"
          :rows="3"
          placeholder="请输入处理备注（可选）"
        />
      </div>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmHandle">确认{{ dialogAction === 2 ? '同意' : '拒绝' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getShopRefunds, processRefund } from '@/api'

const refundList = ref([])
const loading = ref(false)
const filterStatus = ref(null)
const dialogVisible = ref(false)
const dialogAction = ref(2)
const dialogRemark = ref('')
const currentRefund = ref(null)

// 统计数据
const stats = computed(() => {
  const list = refundList.value
  return {
    pending: list.filter(i => i.status === 0).length,
    processing: list.filter(i => i.status === 1).length,
    approved: list.filter(i => i.status === 2 || i.status === 4).length,
    rejected: list.filter(i => i.status === 3).length
  }
})

// 筛选后的列表
const filteredList = computed(() => {
  if (filterStatus.value === null) return refundList.value
  return refundList.value.filter(i => i.status === filterStatus.value)
})

// 加载售后列表
const loadRefunds = async () => {
  loading.value = true
  try {
    const userData = JSON.parse(localStorage.getItem('moli_mall_current_user') || '{}')
    const shopId = userData.userInfo?.id
    if (!shopId) {
      ElMessage.warning('无法获取商家信息')
      return
    }
    const res = await getShopRefunds(shopId)
    if (res.code === 200) {
      refundList.value = res.data || []
    } else {
      ElMessage.error(res.message || '加载失败')
    }
  } catch (e) {
    console.error('加载售后列表失败', e)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

// 状态文本
const getStatusText = (status) => {
  const texts = { 0: '待处理', 1: '处理中', 2: '已同意', 3: '已拒绝', 4: '已完成' }
  return texts[status] || '未知'
}

// 状态标签类型
const getStatusTagType = (status) => {
  const types = { 0: 'warning', 1: 'primary', 2: 'success', 3: 'danger', 4: 'success' }
  return types[status] || 'info'
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

// 处理退款
const handleRefund = (item, action) => {
  currentRefund.value = item
  dialogAction.value = action
  dialogRemark.value = ''
  dialogVisible.value = true
}

const dialogTitle = computed(() => {
  return dialogAction.value === 2 ? '同意退款' : '拒绝退款'
})

// 确认处理
const confirmHandle = async () => {
  if (!currentRefund.value) return
  try {
    const res = await processRefund(
      currentRefund.value.id,
      dialogAction.value,
      dialogRemark.value,
      currentRefund.value.userId,
      currentRefund.value.shopId
    )
    if (res.code === 200) {
      ElMessage.success(dialogAction.value === 2 ? '已同意退款' : '已拒绝退款')
      dialogVisible.value = false
      loadRefunds()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  loadRefunds()
})
</script>

<style scoped>
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.stat-card.clickable {
  cursor: pointer;
  transition: all 0.3s;
}

.stat-card.clickable:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  font-size: 32px;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #1a1a2e;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.stat-card.pending .stat-icon { color: #faad14; }
.stat-card.processing .stat-icon { color: #409eff; }
.stat-card.approved .stat-icon { color: #52c41a; }
.stat-card.rejected .stat-icon { color: #ff4d4f; }

.amount {
  color: #ff4d4f;
  font-weight: 600;
}

.reason, .desc {
  font-size: 13px;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: block;
  max-width: 200px;
}

.text-muted {
  color: #999;
  font-size: 13px;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}

.empty-icon {
  font-size: 60px;
  margin-bottom: 16px;
}

.dialog-content p {
  margin-bottom: 16px;
  font-size: 14px;
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
