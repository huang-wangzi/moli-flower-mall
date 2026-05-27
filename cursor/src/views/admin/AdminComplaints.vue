<template>
  <div class="admin-complaints">
    <div class="page-header">
      <h2>交易投诉</h2>
      <p class="subtitle">处理用户投诉商家、售后客服介入等交易纠纷</p>
    </div>

    <div class="table-container">
      <el-table :data="complaints" border v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column label="订单号" width="160">
          <template #default="{ row }">
            <span>{{ row.orderNo || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="订单金额" width="110">
          <template #default="{ row }">
            <span v-if="row.orderActualAmount">¥{{ row.orderActualAmount }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="来源" width="110">
          <template #default="{ row }">
            <span>{{ row.sourceType === 1 ? '售后客服介入' : '用户投诉' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="事由" min-width="180" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'warning' : row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 0 ? '待处理' : row.status === 1 ? '已处理' : '已驳回' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" width="160" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="viewDetail(row)">查看</el-button>

            <template v-if="row.status === 0">
              <!-- 售后介入显示退款/驳回 -->
              <template v-if="row.sourceType === 1">
                <el-button type="success" size="small" @click="handleComplaint(row, 1)">退款</el-button>
                <el-button type="danger" size="small" @click="handleComplaint(row, 2)">驳回</el-button>
              </template>
              <!-- 普通投诉显示处理/驳回 -->
              <template v-else>
                <el-button type="success" size="small" @click="handleComplaint(row, 1)">标记处理</el-button>
                <el-button type="danger" size="small" @click="handleComplaint(row, 2)">驳回</el-button>
              </template>
            </template>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="detailVisible" title="投诉详情" width="520px">
      <div v-if="currentComplaint" class="complaint-detail">
        <div class="detail-row">
          <span class="label">订单号：</span>
          <span>{{ currentComplaint.orderNo || '-' }}</span>
        </div>
        <div class="detail-row">
          <span class="label">订单金额：</span>
          <span v-if="currentComplaint.orderActualAmount">¥{{ currentComplaint.orderActualAmount }}</span>
          <span v-else>-</span>
        </div>
        <div class="detail-row">
          <span class="label">事由：</span>
          <span>{{ currentComplaint.reason }}</span>
        </div>
        <div class="detail-row">
          <span class="label">时间：</span>
          <span>{{ currentComplaint.createTime }}</span>
        </div>
        <div v-if="currentComplaint.orderId" class="detail-row">
          <span class="label">关联订单ID：</span>
          <span>{{ currentComplaint.orderId }}</span>
        </div>
        <div class="detail-row">
          <span class="label">详细描述：</span>
          <p>{{ currentComplaint.description }}</p>
        </div>
        <div v-if="currentComplaint.adminRemark" class="detail-row">
          <span class="label">管理员备注：</span>
          <span>{{ currentComplaint.adminRemark }}</span>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminComplaints, handleAdminComplaint } from '@/api'

const loading = ref(false)
const complaints = ref([])
const detailVisible = ref(false)
const currentComplaint = ref(null)

const loadComplaints = async () => {
  loading.value = true
  try {
    const res = await getAdminComplaints()
    if (res.code === 200) {
      complaints.value = res.data || []
    } else {
      complaints.value = []
      ElMessage.error(res.message || '加载失败')
    }
  } catch (e) {
    complaints.value = []
    ElMessage.error('加载客诉列表失败')
  } finally {
    loading.value = false
  }
}

const viewDetail = (complaint) => {
  currentComplaint.value = complaint
  detailVisible.value = true
}

const handleComplaint = async (row, status) => {
  let actionText = ''
  let promptTitle = ''
  
  if (status === 1) {
    actionText = row.sourceType === 1 ? '退款' : '已处理'
    promptTitle = '确认处理'
  } else {
    actionText = '驳回'
    promptTitle = '确认驳回'
  }

  try {
    const { value } = await ElMessageBox.prompt(
      `${actionText}说明（可选）`,
      promptTitle,
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPlaceholder: '备注',
        inputValue: row.adminRemark || ''
      }
    )
    const res = await handleAdminComplaint(row.id, status, value || '')
    if (res.code === 200) {
      ElMessage.success(`操作成功：${actionText}`)
      await loadComplaints()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

onMounted(() => {
  loadComplaints()
})
</script>

<style scoped>
.admin-complaints {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.complaint-detail {
  padding: 10px;
}

.detail-row {
  margin-bottom: 15px;
  display: flex;
  align-items: flex-start;
}

.detail-row .label {
  font-weight: 600;
  margin-right: 10px;
  color: #666;
  min-width: 90px;
}

.detail-row p {
  margin: 0;
  color: #333;
  line-height: 1.6;
  flex: 1;
}
</style>
