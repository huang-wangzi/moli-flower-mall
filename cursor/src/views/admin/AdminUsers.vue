<template>
  <div class="admin-users">
    <div class="page-header">
      <h2>用户管理</h2>
      <p class="subtitle">管理系统用户，查看用户信息和账户状态</p>
    </div>

    <div class="status-tabs">
      <div class="status-tab" :class="{ active: statusFilter === '' }" @click="statusFilter = ''; loadUsers()">
        全部<span v-if="getStatusCount(null)" class="tab-count">{{ getStatusCount(null) }}</span>
      </div>
      <div class="status-tab" :class="{ active: statusFilter === 1 }" @click="statusFilter = 1; loadUsers()">
        正常<span v-if="getStatusCount(1)" class="tab-count success">{{ getStatusCount(1) }}</span>
      </div>
      <div class="status-tab" :class="{ active: statusFilter === 0 }" @click="statusFilter = 0; loadUsers()">
        禁用<span v-if="getStatusCount(0)" class="tab-count danger">{{ getStatusCount(0) }}</span>
      </div>
    </div>

    <div class="toolbar">
      <el-input v-model="searchKeyword" placeholder="搜索用户名/手机号" clearable @keyup.enter="loadUsers" />
      <el-button type="primary" @click="loadUsers">搜索</el-button>
    </div>

    <div class="table-container">
      <el-table :data="users" border v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column prop="email" label="邮箱" min-width="150" />
        <el-table-column label="角色" width="80">
          <template #default="{ row }">
            <el-tag :type="getRoleTagType(row.role)" size="small">{{ getRoleName(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="账户余额" width="100">
          <template #default="{ row }">¥{{ row.balance || 0 }}</template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" size="small">{{ getStatusName(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="160">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button :type="row.status === 1 ? 'danger' : 'success'" size="small" @click="toggleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="loadUsers"
        @current-change="loadUsers"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAdminUsers, updateUserStatus } from '@/api'

const loading = ref(false)
const searchKeyword = ref('')
const statusFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const allUsers = ref([])
const users = ref([])

const formatTime = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

const getRoleName = (role) => ({ 1: '用户', 2: '商家', 4: '管理员' })[role] || '未知'
const getRoleTagType = (role) => ({ 1: 'info', 2: 'warning', 4: 'danger' })[role] || 'info'
const getStatusName = (status) => ({ 1: '正常', 2: '已审核', 3: '已拒绝', 0: '禁用' })[status] || '未知'
const getStatusTagType = (status) => ({ 1: 'success', 2: 'success', 3: 'danger', 0: 'danger' })[status] || 'info'

const getStatusCount = (status) => {
  if (status === null) return allUsers.value.length
  return allUsers.value.filter(u => u.status === status).length
}

const loadUsers = async () => {
  loading.value = true
  try {
    const res = await getAdminUsers({
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      keyword: searchKeyword.value
    })
    if (res.code === 200) {
      allUsers.value = res.data.records || []
      total.value = res.data.total || 0
      users.value = statusFilter.value !== ''
        ? allUsers.value.filter(u => u.status === parseInt(statusFilter.value))
        : allUsers.value
    }
  } catch (error) { console.error('加载用户列表失败', error) }
  finally { loading.value = false }
}

const toggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    const res = await updateUserStatus(row.id, newStatus)
    if (res.code === 200) { row.status = newStatus; ElMessage.success(row.status === 1 ? '用户已启用' : '用户已禁用') }
    else ElMessage.error(res.message || '操作失败')
  } catch (error) { ElMessage.error('操作失败') }
}

onMounted(() => { loadUsers() })
</script>

<style scoped>
.admin-users {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 横向状态标签筛选 */
.status-tabs {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  padding: 16px 20px;
  background: #fafbfc;
  border-radius: 12px;
  flex-wrap: wrap;
}

.status-tab {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border-radius: 24px;
  background: white;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
  color: #666;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  border: 1px solid transparent;
  white-space: nowrap;
}

.status-tab:hover {
  background: #f0f0f0;
}

.status-tab.active {
  background: linear-gradient(135deg, #4A7C59, #3d6b4a);
  color: white;
  border-color: #4A7C59;
}

.tab-count {
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  background: #f0f0f0;
  color: #666;
}

.status-tab.active .tab-count {
  background: rgba(255, 255, 255, 0.25);
  color: white;
}

.tab-count.success { background: #e8f5e9; color: #4A7C59; }
.tab-count.warning { background: #fff3e0; color: #faad14; }
.tab-count.danger { background: #ffe0e0; color: #e74c3c; }

/* 搜索工具栏 */
.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

/* 表格容器 */
.table-container {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}
</style>
