<template>
  <!-- 商家端价格预警页面 -->
  <div class="shop-price-alert-page">
    <div class="page-header">
      <h1 class="page-title">🔔 价格预警</h1>
      <p class="page-desc">设置价格提醒，价格变动时自动通知</p>
    </div>

    <!-- AI价格分析卡片 -->
    <div class="ai-analysis-card">
      <div class="card-header">
        <span class="card-icon">🤖</span>
        <span class="card-title">AI智能分析</span>
        <span class="card-time">{{ currentDate }}</span>
      </div>
      <div class="card-body" v-if="aiAnalysis">
        <p class="analysis-text">{{ aiAnalysis }}</p>
      </div>
      <div class="card-body" v-else>
        <p class="loading-text">正在生成分析...</p>
      </div>
    </div>

    <!-- 预警设置 -->
    <div class="section">
      <h2 class="section-title">设置价格提醒</h2>
      <el-card class="alert-form-card">
        <el-form :model="alertForm" inline>
          <el-form-item label="提醒类型">
            <el-select v-model="alertForm.type" style="width: 130px;">
              <el-option label="低于时提醒" :value="1" />
              <el-option label="高于时提醒" :value="2" />
              <el-option label="两者都提醒" :value="3" />
            </el-select>
          </el-form-item>
          <el-form-item label="目标价格">
            <el-input-number v-model="alertForm.targetPrice" :min="0" :precision="2" style="width: 130px;" />
            <span class="unit-label">元/斤</span>
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="alertForm.remark" placeholder="选填" style="width: 160px;" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="addAlert" :loading="submitting">添加预警</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <!-- 我的预警列表 -->
    <div class="section">
      <h2 class="section-title">我的预警设置</h2>
      <el-card class="alert-list-card">
        <template #header>
          <div class="list-header">
            <span>共 {{ myAlerts.length }} 条预警</span>
            <el-button size="small" @click="loadAlerts">刷新</el-button>
          </div>
        </template>
        
        <div v-if="myAlerts.length > 0" class="alert-list">
          <div v-for="(alert, index) in myAlerts" :key="index" class="alert-item">
            <div class="alert-info">
              <el-tag :type="alert.type === 1 ? 'success' : alert.type === 2 ? 'danger' : 'warning'" size="small">
                {{ alert.type === 1 ? '低于' : alert.type === 2 ? '高于' : '双向' }}
              </el-tag>
              <span class="alert-price">¥{{ alert.targetPrice }}</span>
              <span class="alert-unit">元/斤</span>
              <span v-if="alert.remark" class="alert-remark">{{ alert.remark }}</span>
            </div>
            <div class="alert-meta">
              <el-tag :type="alert.triggered ? 'danger' : 'success'" size="small">
                {{ alert.triggered ? '已触发' : '监控中' }}
              </el-tag>
              <span class="create-time">{{ alert.createTime }}</span>
            </div>
            <div class="alert-actions">
              <el-button type="danger" size="small" link @click="deleteAlert(index)">删除</el-button>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无预警设置，添加预警后价格变动时将收到提醒" />
      </el-card>
    </div>

    <!-- 价格统计 -->
    <div class="section">
      <h2 class="section-title">当前市场行情</h2>
      <el-row :gutter="16">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-label">市场均价</div>
            <div class="stat-value">¥{{ avgPrice }}</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-label">最高价格</div>
            <div class="stat-value">¥{{ maxPrice }}</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-label">最低价格</div>
            <div class="stat-value">¥{{ minPrice }}</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-label">报价市场</div>
            <div class="stat-value">{{ marketCount }} 个</div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getTodayPrices, getMultiMarketTrend } from '@/api'

// 当前日期
const currentDate = computed(() => {
  const now = new Date()
  return `${now.getMonth() + 1}月${now.getDate()}日 ${now.getHours()}:${String(now.getMinutes()).padStart(2, '0')}`
})

// AI分析内容
const aiAnalysis = ref('')

// 预警表单
const alertForm = ref({
  type: 1,
  targetPrice: 0,
  remark: ''
})

// 我的预警列表
const myAlerts = ref([])

// 提交状态
const submitting = ref(false)

// 价格数据
const priceData = ref([])
const trendData = ref([])

// 统计数据
const avgPrice = computed(() => {
  if (!priceData.value.length) return '0.00'
  const sum = priceData.value.reduce((acc, item) => acc + (parseFloat(item.price) || 0), 0)
  return (sum / priceData.value.length).toFixed(2)
})

const maxPrice = computed(() => {
  if (!priceData.value.length) return '0.00'
  const max = Math.max(...priceData.value.map(item => parseFloat(item.price) || 0))
  return max.toFixed(2)
})

const minPrice = computed(() => {
  if (!priceData.value.length) return '0.00'
  const min = Math.min(...priceData.value.map(item => parseFloat(item.price) || 0))
  return min.toFixed(2)
})

const marketCount = computed(() => priceData.value.length)

// 加载价格数据
const loadPriceData = async () => {
  try {
    const res = await getTodayPrices()
    if (res.code === 200 && res.data) {
      priceData.value = res.data
    }
  } catch (error) {
    console.error('加载价格数据失败:', error)
  }
}

// 加载趋势数据
const loadTrendData = async () => {
  try {
    const res = await getMultiMarketTrend({ days: 7 })
    if (res.code === 200 && res.data) {
      trendData.value = res.data
    }
  } catch (error) {
    console.error('加载趋势数据失败:', error)
  }
}

// 加载我的预警
const loadAlerts = () => {
  const saved = localStorage.getItem('shopPriceAlerts')
  if (saved) {
    myAlerts.value = JSON.parse(saved)
  }
}

// 添加预警
const addAlert = () => {
  if (!alertForm.value.targetPrice || alertForm.value.targetPrice <= 0) {
    ElMessage.warning('请输入有效的目标价格')
    return
  }
  
  const newAlert = {
    type: alertForm.value.type,
    targetPrice: alertForm.value.targetPrice,
    remark: alertForm.value.remark,
    triggered: false,
    createTime: new Date().toLocaleString()
  }
  
  myAlerts.value.push(newAlert)
  localStorage.setItem('shopPriceAlerts', JSON.stringify(myAlerts.value))
  
  alertForm.value = { type: 1, targetPrice: 0, remark: '' }
  ElMessage.success('预警设置成功')
}

// 删除预警
const deleteAlert = (index) => {
  myAlerts.value.splice(index, 1)
  localStorage.setItem('shopPriceAlerts', JSON.stringify(myAlerts.value))
  ElMessage.success('已删除预警')
}

// 生成AI分析
const generateAIAnalysis = () => {
  const avg = parseFloat(avgPrice.value)
  const max = parseFloat(maxPrice.value)
  const min = parseFloat(minPrice.value)
  
  if (!priceData.value.length) {
    aiAnalysis.value = '暂无价格数据，请等待管理员录入'
    return
  }
  
  // 简单的分析逻辑
  const range = max - min
  const rangePercent = ((range / avg) * 100).toFixed(1)
  
  let analysis = `📊 今日市场行情分析\n\n`
  analysis += `• 市场均价：¥${avg}/斤\n`
  analysis += `• 价格区间：¥${min} - ¥${max}/斤\n`
  analysis += `• 市场波动：${rangePercent}%\n\n`
  
  if (rangePercent < 5) {
    analysis += `💡 今日市场价格稳定，波动较小，建议按需采购。`
  } else if (rangePercent < 10) {
    analysis += `💡 市场存在一定波动，建议关注价格走势，把握采购时机。`
  } else {
    analysis += `💡 市场波动较大，建议谨慎采购，关注后续走势。`
  }
  
  aiAnalysis.value = analysis
}

// 初始化
onMounted(() => {
  loadPriceData()
  loadTrendData()
  loadAlerts()
  
  // 生成AI分析
  setTimeout(() => {
    generateAIAnalysis()
  }, 500)
})
</script>

<style scoped>
.shop-price-alert-page {
  padding: 24px;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  color: #333;
  margin-bottom: 8px;
}

.page-desc {
  font-size: 14px;
  color: #999;
}

/* AI分析卡片 */
.ai-analysis-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 20px;
  color: #fff;
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.card-icon {
  font-size: 24px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  flex: 1;
}

.card-time {
  font-size: 12px;
  opacity: 0.8;
}

.card-body {
  background: rgba(255, 255, 255, 0.15);
  border-radius: 8px;
  padding: 16px;
}

.analysis-text {
  font-size: 14px;
  line-height: 1.8;
  white-space: pre-line;
  margin: 0;
}

.loading-text {
  font-size: 14px;
  opacity: 0.8;
  margin: 0;
}

/* 区块样式 */
.section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 18px;
  color: #333;
  margin-bottom: 16px;
}

/* 预警表单 */
.alert-form-card {
  margin-bottom: 0;
}

.unit-label {
  margin-left: 8px;
  color: #999;
}

/* 预警列表 */
.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.alert-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.alert-item {
  display: flex;
  align-items: center;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  gap: 16px;
}

.alert-info {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
}

.alert-price {
  font-size: 20px;
  font-weight: bold;
  color: #4A7C59;
}

.alert-unit {
  font-size: 12px;
  color: #999;
}

.alert-remark {
  font-size: 13px;
  color: #666;
  padding-left: 12px;
  border-left: 1px solid #ddd;
}

.alert-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}

.create-time {
  font-size: 11px;
  color: #999;
}

.alert-actions {
  min-width: 50px;
}

/* 统计卡片 */
.stat-card {
  text-align: center;
}

.stat-label {
  font-size: 13px;
  color: #666;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #4A7C59;
}
</style>
