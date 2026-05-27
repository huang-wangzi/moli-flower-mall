<template>
  <div class="admin-prices">
    <el-tabs v-model="activeTab" type="border-card" class="custom-tabs">
      <!-- 市场管理 -->
      <el-tab-pane label="市场管理" name="market">
        <div class="tab-content">
          <el-card class="market-card">
            <template #header>
              <div class="card-header">
                <span class="card-title">市场列表</span>
                <el-button type="primary" @click="showMarketDialog = true; editMarket = null">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="white" style="margin-right: 6px; vertical-align: middle;">
                    <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/>
                  </svg>
                  添加市场
                </el-button>
              </div>
            </template>

            <el-table :data="markets" border v-loading="loading" :header-cell-style="{ background: '#f5f7fa', color: '#333' }">
              <el-table-column prop="name" label="市场名称" width="180" />
              <el-table-column prop="address" label="地址" min-width="200" />
              <el-table-column prop="province" label="省份" width="100" />
              <el-table-column prop="city" label="城市" width="100" />
              <el-table-column prop="contact" label="联系人" width="100" />
              <el-table-column prop="phone" label="电话" width="130" />
              <el-table-column prop="sort" label="排序" width="80" />
              <el-table-column label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small" effect="light">
                    {{ row.status === 1 ? '启用' : '禁用' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="220" fixed="right">
                <template #default="{ row }">
                  <el-button type="primary" size="small" link @click="editMarket = row; showMarketDialog = true">编辑</el-button>
                  <el-button :type="row.status === 1 ? 'warning' : 'success'" size="small" link @click="toggleMarketStatus(row)">
                    {{ row.status === 1 ? '停用' : '启用' }}
                  </el-button>
                  <el-button type="danger" size="small" link @click="handleDeleteMarket(row.id)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </div>
      </el-tab-pane>

      <!-- 价格监测（整合价格录入） -->
      <el-tab-pane label="价格监测" name="monitor">
        <div class="tab-content">
          <!-- 价格录入表单 -->
          <el-card class="price-form-card">
            <template #header>
              <div class="card-header">
                <span class="card-title">录入价格</span>
                <span class="form-tip">选择市场并录入当日最低价和最高价，数据将实时更新到图表</span>
              </div>
            </template>
            <el-form :model="priceForm" inline class="price-form-inline">
              <el-form-item label="市场">
                <el-select v-model="priceForm.market" placeholder="请选择市场" style="width: 200px;" @change="onMarketChange">
                  <el-option v-for="m in markets" :key="m.id" :label="m.name" :value="m.name" />
                </el-select>
              </el-form-item>
              <el-form-item label="最低价">
                <el-input-number v-model="priceForm.minPrice" :min="0" :precision="2" style="width: 110px;" />
                <span style="margin-left: 8px; color: #999;">元/斤</span>
              </el-form-item>
              <el-form-item label="最高价">
                <el-input-number v-model="priceForm.maxPrice" :min="0" :precision="2" style="width: 110px;" />
                <span style="margin-left: 8px; color: #999;">元/斤</span>
              </el-form-item>
              <el-form-item label="日期">
                <el-date-picker v-model="priceForm.recordDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 150px;" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="addPrice" :loading="submitting" icon="Check">录入</el-button>
              </el-form-item>
            </el-form>
          </el-card>

          <!-- 价格走势图表 -->
          <el-card class="monitor-chart-card">
            <template #header>
              <div class="card-header">
                <span class="card-title">茉莉鲜花价格走势</span>
                <div class="chart-controls">
                  <el-select v-model="selectedChartMarket" placeholder="全部市场" clearable style="width: 160px; margin-right: 12px;" @change="loadMonitorChart">
                    <el-option v-for="m in markets" :key="m.id" :label="m.name" :value="m.name" />
                  </el-select>
                  <el-date-picker
                    v-model="chartDate"
                    type="date"
                    placeholder="选择历史日期"
                    value-format="YYYY-MM-DD"
                    :clearable="true"
                    style="width: 160px;"
                    @change="loadMonitorChart"
                  />
                </div>
              </div>
            </template>
            <div class="chart-container-large" ref="monitorChartRef"></div>
          </el-card>

          <!-- 今日录入记录 -->
          <el-card class="today-records-card">
            <template #header>
              <div class="card-header">
                <span class="card-title">今日录入记录</span>
                <span class="record-count">共 {{ todayRecords.length }} 条</span>
              </div>
            </template>
            <el-table :data="todayRecords" border size="small" v-loading="monitorLoading"
              :header-cell-style="{ background: '#f5f7fa', color: '#333' }">
              <el-table-column prop="market" label="市场" width="150" />
              <el-table-column prop="minPrice" label="最低价" width="100" align="center">
                <template #default="{ row }">
                  <span class="price-min">¥{{ row.minPrice }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="maxPrice" label="最高价" width="100" align="center">
                <template #default="{ row }">
                  <span class="price-max">¥{{ row.maxPrice }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="recordDate" label="录入日期" width="130" align="center" />
              <el-table-column prop="createTime" label="录入时间" min-width="160" />
              <el-table-column label="操作" width="130" align="center">
                <template #default="{ row }">
                  <el-button type="primary" size="small" link @click="editPrice(row)">编辑</el-button>
                  <el-button type="danger" size="small" link @click="deletePrice(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </div>
      </el-tab-pane>

      <!-- 历史记录 -->
      <el-tab-pane label="历史记录" name="list">
        <div class="tab-content">
          <el-card class="price-list-card">
            <template #header>
              <div class="card-header">
                <span class="card-title">价格列表</span>
                <div class="filter-controls">
                  <el-select v-model="filterMarket" placeholder="筛选市场" clearable style="width: 160px; margin-right: 10px;">
                    <el-option v-for="m in markets" :key="m.id" :label="m.name" :value="m.name" />
                  </el-select>
                  <el-select v-model="filterCategory" placeholder="筛选品种" clearable style="width: 130px; margin-right: 10px;" @change="loadPrices">
                    <el-option label="茉莉鲜花" :value="1" />
                  </el-select>
                  <el-button @click="loadPrices" icon="Refresh">刷新</el-button>
                </div>
              </div>
            </template>
            <el-table :data="prices" border v-loading="loading" :header-cell-style="{ background: '#f5f7fa', color: '#333' }">
              <el-table-column prop="market" label="市场" width="180" />
              <el-table-column prop="category" label="品种" width="100">
                <template #default="{ row }">茉莉鲜花</template>
              </el-table-column>
              <el-table-column prop="minPrice" label="最低价" width="100" align="center">
                <template #default="{ row }">
                  <span class="price-min">¥{{ row.minPrice }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="maxPrice" label="最高价" width="100" align="center">
                <template #default="{ row }">
                  <span class="price-max">¥{{ row.maxPrice }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="recordDate" label="日期" width="130" />
              <el-table-column prop="createTime" label="录入时间" width="170" />
              <el-table-column label="操作" width="150" fixed="right">
                <template #default="{ row }">
                  <el-button type="primary" size="small" link @click="editPrice(row)">编辑</el-button>
                  <el-button type="danger" size="small" link @click="deletePrice(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>

            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :total="total"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next"
              style="margin-top: 20px; justify-content: flex-end;"
              @size-change="loadPrices"
              @current-change="loadPrices"
            />
          </el-card>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 市场编辑弹窗 -->
    <el-dialog v-model="showMarketDialog" :title="editMarket ? '编辑市场' : '添加市场'" width="600px" :close-on-click-modal="false">
      <el-form :model="marketForm" :rules="marketRules" ref="marketFormRef" label-width="100px">
        <el-form-item label="市场名称" prop="name">
          <el-input v-model="marketForm.name" placeholder="请输入市场名称" />
        </el-form-item>
        <el-form-item label="市场地址" prop="address">
          <el-input v-model="marketForm.address" placeholder="请输入详细地址" />
        </el-form-item>
        <el-form-item label="省份" prop="province">
          <el-input v-model="marketForm.province" placeholder="请输入省份" />
        </el-form-item>
        <el-form-item label="城市" prop="city">
          <el-input v-model="marketForm.city" placeholder="请输入城市" />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="marketForm.contact" placeholder="请输入联系人" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="marketForm.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="marketForm.sort" :min="0" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="marketForm.description" type="textarea" :rows="3" placeholder="请输入市场描述" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="marketForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showMarketDialog = false">取消</el-button>
        <el-button type="primary" @click="saveMarket" :loading="submitting">保存</el-button>
      </template>
    </el-dialog>

    <!-- 价格编辑弹窗 -->
    <el-dialog v-model="editPriceDialog" title="编辑价格" width="450px" :close-on-click-modal="false">
      <el-form :model="editPriceForm" label-width="80px">
        <el-form-item label="市场">
          <el-input v-model="editPriceForm.market" disabled />
        </el-form-item>
        <el-form-item label="品种">
          <el-input value="茉莉鲜花" disabled />
        </el-form-item>
        <el-form-item label="价格">
          <el-input-number v-model="editPriceForm.price" :min="0" :precision="2" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="单位">
          <el-input value="元/斤" disabled />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editPriceDialog = false">取消</el-button>
        <el-button type="primary" @click="savePrice" :loading="submitting">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import {
  getAllMarkets, addMarket, updateMarket, deleteMarket,
  getPriceList, addPriceRecord, updatePriceRecord, deletePriceRecord,
  getLatestPrices, getMultiMarketTrend, getAllPriceMarkets
} from '@/api'

const activeTab = ref('monitor')
const loading = ref(false)
const submitting = ref(false)
const monitorLoading = ref(false)

const markets = ref([])
const prices = ref([])
const todayRecords = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const filterMarket = ref('')
const filterCategory = ref(null)

// 价格监测相关
const chartDays = ref(7)
const chartDate = ref('')
const selectedChartMarket = ref('')
const monitorData = ref([])
const monitorMarkets = ref([])
const monitorChartRef = ref(null)
let monitorChart = null

const categoryMap = {
  1: '茉莉鲜花',
  2: '茉莉花茶',
  3: '茉莉盆栽',
  4: '茉莉花苗'
}

const getCategoryName = (category) => {
  return categoryMap[category] || '未知'
}

// 图表配色
const chartColors = {
  primary: '#4A7C59',
  primaryLight: '#6b9b7a',
  grid: '#f0f0f0',
  text: '#666666'
}

const showMarketDialog = ref(false)
const editMarket = ref(null)
const marketFormRef = ref(null)
const marketForm = reactive({
  name: '',
  address: '',
  province: '',
  city: '',
  contact: '',
  phone: '',
  sort: 0,
  description: '',
  status: 1
})

const marketRules = {
  name: [{ required: true, message: '请输入市场名称', trigger: 'blur' }],
  province: [{ required: true, message: '请输入省份', trigger: 'blur' }],
  city: [{ required: true, message: '请输入城市', trigger: 'blur' }]
}

const priceForm = reactive({
  market: '',
  category: 1,
  unit: '元/斤',
  minPrice: 0,
  maxPrice: 0,
  recordDate: new Date().toISOString().split('T')[0]
})

const editPriceDialog = ref(false)
const editPriceForm = reactive({
  id: 0,
  market: '',
  price: 0
})

const loadMarkets = async () => {
  loading.value = true
  try {
    const res = await getAllMarkets()
    if (res.code === 200) {
      markets.value = res.data || []
    }
  } catch (error) {
    console.error('加载市场列表失败', error)
  } finally {
    loading.value = false
  }
}

const saveMarket = async () => {
  if (!marketFormRef.value) return

  try {
    await marketFormRef.value.validate()
    submitting.value = true

    const res = editMarket.value
      ? await updateMarket(marketForm)
      : await addMarket(marketForm)

    if (res.code === 200) {
      ElMessage.success(editMarket.value ? '更新成功' : '添加成功')
      showMarketDialog.value = false
      loadMarkets()
      resetMarketForm()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    console.error('保存市场失败', error)
  } finally {
    submitting.value = false
  }
}

const handleDeleteMarket = (id) => {
  ElMessageBox.confirm('确定要删除该市场吗？删除后将无法恢复！', '提示', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    const res = await deleteMarket(id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadMarkets()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  }).catch(() => {})
}

const toggleMarketStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '启用' : '停用'

  try {
    const res = await updateMarket({ ...row, status: newStatus })
    if (res.code === 200) {
      ElMessage.success(`市场已${action}`)
      loadMarkets()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    console.error('切换市场状态失败:', error)
    ElMessage.error('操作失败')
  }
}

const resetMarketForm = () => {
  Object.assign(marketForm, {
    name: '',
    address: '',
    province: '',
    city: '',
    contact: '',
    phone: '',
    sort: 0,
    description: '',
    status: 1
  })
  editMarket.value = null
}

const onMarketChange = () => {
  // 可以在这里做一些联动操作
}

const addPrice = async () => {
  if (!priceForm.market || !priceForm.minPrice || !priceForm.maxPrice || !priceForm.recordDate) {
    ElMessage.warning('请填写完整信息（市场、最低价、最高价、日期）')
    return
  }
  if (priceForm.minPrice > priceForm.maxPrice) {
    ElMessage.warning('最低价不能大于最高价')
    return
  }

  submitting.value = true
  try {
    const res = await addPriceRecord({
      market: priceForm.market,
      category: priceForm.category,
      unit: priceForm.unit,
      minPrice: priceForm.minPrice,
      maxPrice: priceForm.maxPrice,
      recordDate: priceForm.recordDate
    })

    if (res && res.code === 200) {
      ElMessage.success('价格录入成功')
      priceForm.minPrice = 0
      priceForm.maxPrice = 0
      loadPrices()
      loadTodayRecords()
      loadMonitorChart()
      loadMonitorTable()
    } else if (res) {
      ElMessage.error(res.message || '录入失败')
    }
  } catch (error) {
    console.error('录入请求异常:', error)
    // INSERT可能已成功，刷新数据
    loadPrices()
    loadTodayRecords()
    loadMonitorChart()
    loadMonitorTable()
  } finally {
    submitting.value = false
  }
}

const loadPrices = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      category: filterCategory.value,
      market: filterMarket.value || undefined
    }
    const res = await getPriceList(params)
    if (res.code === 200) {
      prices.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('加载价格列表失败', error)
  } finally {
    loading.value = false
  }
}

const editPrice = (row) => {
  editPriceForm.id = row.id
  editPriceForm.market = row.market
  editPriceForm.price = row.price
  editPriceDialog.value = true
}

const savePrice = async () => {
  submitting.value = true
  try {
    const res = await updatePriceRecord(editPriceForm.id, {
      category: 1,
      price: editPriceForm.price,
      unit: '元/斤'
    })

    if (res.code === 200) {
      ElMessage.success('更新成功')
      editPriceDialog.value = false
      loadPrices()
      loadChartData()
    } else {
      ElMessage.error(res.message || '更新失败')
    }
  } catch (error) {
    ElMessage.error('更新失败')
  } finally {
    submitting.value = false
  }
}

const deletePrice = (row) => {
  ElMessageBox.confirm('确定要删除该价格记录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    const res = await deletePriceRecord(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadPrices()
      loadTodayRecords()
      loadMonitorChart()
      loadMonitorTable()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  }).catch(() => {})
}

// 价格监测图表
const loadMonitorChart = async () => {
  if (!monitorChartRef.value) return

  await nextTick()
  await new Promise(r => setTimeout(r, 100))

  const el = monitorChartRef.value
  if (!el || el.clientWidth === 0 || el.clientHeight === 0) {
    return
  }

  if (!monitorChart) {
    monitorChart = echarts.init(el)
  }

  try {
    // 如果选了历史日期，计算从那天到今天的天数
    let queryDays = chartDays.value
    if (chartDate.value) {
      const selected = new Date(chartDate.value)
      const today = new Date()
      const diff = Math.ceil((today - selected) / (1000 * 60 * 60 * 24))
      queryDays = Math.max(diff + 1, 1)
    }

    const res = await getMultiMarketTrend({ days: queryDays, category: 1 })

    if (res.code === 200) {
      const trendData = res.data || []

      // 按市场分组
      const marketGroups = {}

      trendData.forEach(p => {
        const marketName = p.market
        if (!marketGroups[marketName]) {
          marketGroups[marketName] = {}
        }
        const date = typeof p.recordDate === 'string' ? p.recordDate.substring(0, 10) : String(p.recordDate)
        marketGroups[marketName][date] = {
          maxPrice: parseFloat(p.maxPrice) || 0,
          minPrice: parseFloat(p.minPrice) || 0
        }
      })

      // 生成日期范围：选了历史日期只显示那一天，否则近7天
      const dates = []
      if (chartDate.value) {
        dates.push(chartDate.value)
      } else {
        const today = new Date()
        for (let i = chartDays.value - 1; i >= 0; i--) {
          const d = new Date(today)
          d.setDate(d.getDate() - i)
          dates.push(d.toISOString().split('T')[0])
        }
      }

      // 获取年份
      const yearStr = dates[0].substring(0, 4) + '年'

      const markets = Object.keys(marketGroups)

      // 筛选市场
      const filteredMarkets = selectedChartMarket.value
        ? markets.filter(m => m === selectedChartMarket.value)
        : markets

      if (filteredMarkets.length === 0) {
        monitorChart.setOption({
          graphic: [{
            type: 'text',
            left: 'center',
            top: 'middle',
            style: {
              text: '暂无价格数据，请先录入价格信息',
              fontSize: 14,
              fill: '#999',
              textAlign: 'center'
            }
          }]
        })
        return
      }

      // 市场颜色配置 - 鲜艳颜色
      const marketColors = [
        '#e74c3c', '#3498db', '#27ae60', '#9b59b6',
        '#f39c12', '#1abc9c', '#e91e63', '#00bcd4'
      ]

      // 计算柱子宽度 - 根据市场数量和时间点数量动态调整
      const marketCount = filteredMarkets.length
      const dateCount = dates.length
      // 每个市场的柱子宽度（加宽）
      const barWidthRatio = Math.min(0.8, 0.6 / marketCount)
      // 两个市场柱子之间的间隙
      const barGapBetweenMarkets = barWidthRatio * 0.3
      // 每天之间留的空隙（用categoryGap）
      const categoryGapRatio = 0.5 // 50%的空隙留给日期之间

      // 构建 series - 每个市场每天一根柱子
      const series = []

      filteredMarkets.forEach((market, marketIndex) => {
        const color = marketColors[marketIndex % marketColors.length]
        const marketData = marketGroups[market] || {}

        // 最低价柱子 - 统一紫色
        series.push({
          name: `${market}`,
          type: 'bar',
          stack: `${market}`,
          data: dates.map(date => {
            const d = marketData[date]
            return d?.minPrice > 0 ? d.minPrice : 0
          }),
          barWidth: `${barWidthRatio * 100}%`,
          barGap: `${barGapBetweenMarkets * 100}%`,
          itemStyle: {
            color: '#9B8AA6',
            borderRadius: [0, 0, 0, 0]
          },
          emphasis: {
            itemStyle: {
              color: '#9B8AA6',
              shadowBlur: 6,
              shadowColor: 'rgba(0,0,0,0.2)'
            }
          }
        })

        // 最高价与最低价的差值，叠加在最低价上方，按市场分颜色
        series.push({
          name: `${market}`,
          type: 'bar',
          stack: `${market}`,
          data: dates.map(date => {
            const d = marketData[date]
            if (!d?.maxPrice || d.maxPrice <= 0) return 0
            return Math.max(0, d.maxPrice - (d.minPrice || 0))
          }),
          barWidth: `${barWidthRatio * 100}%`,
          barGap: `${barGapBetweenMarkets * 100}%`,
          itemStyle: {
            color: color,
            borderRadius: [4, 4, 0, 0]
          },
          emphasis: {
            itemStyle: {
              color: color,
              shadowBlur: 6,
              shadowColor: 'rgba(0,0,0,0.2)'
            }
          }
        })

      })

      // X轴标签格式化
      const xAxisData = dates.map(date => {
        const d = new Date(date)
        return `${d.getMonth() + 1}/${d.getDate()}`
      })

      monitorChart.setOption({
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'shadow' },
          backgroundColor: 'rgba(255, 255, 255, 0.98)',
          borderColor: '#4A7C59',
          borderWidth: 1,
          padding: [12, 16],
          textStyle: { color: '#333' },
          enterable: true,
          extraCssText: 'max-height: 260px; overflow-y: auto; pointer-events: auto;',
          formatter: function(params) {
            if (!params || params.length === 0) return ''
            const dateIdx = params[0].dataIndex
            const date = dates[dateIdx]
            let html = `<div style="font-weight:bold;margin-bottom:8px;font-size:13px;">${date}</div>`

            // 按市场分组显示（同名series按颜色区分最低价/最高价）
            const marketData = {}
            params.forEach(p => {
              const market = p.seriesName
              if (!marketData[market]) {
                marketData[market] = { min: 0, range: 0 }
              }
              // 紫色是最低价，其他颜色是最高价差值
              if (p.color === '#9B8AA6') {
                marketData[market].min = p.value
              } else {
                marketData[market].range = p.value
              }
            })

            Object.keys(marketData).sort().forEach((market) => {
              const d = marketData[market]
              const marketIdx = filteredMarkets.indexOf(market)
              const c = marketColors[marketIdx % marketColors.length]

              html += `<div style="margin:6px 0;padding:4px 0;border-bottom:1px solid #eee;">
                <div style="display:flex;align-items:center;gap:6px;margin-bottom:4px;">
                  <span style="display:inline-block;width:10px;height:10px;border-radius:2px;background:${c};"></span>
                  <span style="font-weight:600;color:#333;">${market}</span>
                </div>
                <div style="display:flex;justify-content:space-between;gap:20px;padding-left:16px;">
                  <span style="color:#9B8AA6;">最低价</span>
                  <span style="font-weight:600;">¥${d.min.toFixed(2)}</span>
                </div>
                <div style="display:flex;justify-content:space-between;gap:20px;padding-left:16px;">
                  <span style="color:${c};">最高价</span>
                  <span style="font-weight:600;">¥${(d.min + d.range).toFixed(2)}</span>
                </div>
              </div>`
            })

            return html
          }
        },
        legend: {
          data: filteredMarkets.map((market, idx) => ({
            name: market,
            itemStyle: { color: marketColors[idx % marketColors.length] }
          })),
          formatter: (name) => name,
          orient: 'vertical',
          right: 5,
          top: 10,
          textStyle: { color: '#666', fontSize: 11 },
          itemWidth: 14,
          itemHeight: 10,
          itemGap: 6,
          tooltip: { show: true, trigger: 'item' },
          type: 'scroll',
          selectedMode: 'multiple',
          selected: (() => {
            const s = {}
            filteredMarkets.forEach(m => { s[m] = true })
            return s
          })()
        },
        grid: {
          left: '3%',
          right: '12%',
          bottom: '10%',
          top: 50,
          containLabel: true
        },
        xAxis: {
          type: 'category',
          boundaryGap: true,
          data: xAxisData,
          axisLine: { lineStyle: { color: '#e0e0e0' } },
          axisTick: { show: false },
          axisLabel: {
            color: '#666',
            fontSize: 11,
            interval: 0
          },
          categoryGap: '50%'
        },
        yAxis: {
          type: 'value',
          name: `${yearStr}  价格（元/斤）`,
          nameTextStyle: {
            color: '#666',
            fontSize: 11,
            width: 120,
            padding: [0, 0, 0, 10]
          },
          axisLine: { show: false },
          axisTick: { show: false },
          axisLabel: { color: '#666', fontSize: 11, padding: [0, 5, 0, 0] },
          splitLine: { lineStyle: { color: '#f0f0f0', type: 'dashed' } },
          min: 0,
          max: 50,
          interval: 5
        },
        series: series
      }, true)

      // 图例点击：点击某市场只显示该市场，再次点击恢复全部
      monitorChart.off('legendselectchanged')
      monitorChart.on('legendselectchanged', (params) => {
        const clickedMarket = params.name
        const allMarkets = filteredMarkets

        if (selectedChartMarket.value === clickedMarket) {
          // 再次点击同一市场，恢复全部
          selectedChartMarket.value = ''
          const newSelected = {}
          allMarkets.forEach(m => { newSelected[m] = true })
          monitorChart.setOption({ legend: { selected: newSelected } })
        } else {
          // 点击新市场，只显示该市场
          selectedChartMarket.value = clickedMarket
          const newSelected = {}
          allMarkets.forEach(m => { newSelected[m] = (m === clickedMarket) })
          monitorChart.setOption({ legend: { selected: newSelected } })
        }
      })
      
      // 双击图表任意位置恢复显示全部市场
      monitorChart.getZr().off('dblclick')
      monitorChart.getZr().on('dblclick', () => {
        selectedChartMarket.value = ''
        loadMonitorChart()
      })
    } else {
      monitorChart.setOption({
        graphic: [{
          type: 'text',
          left: 'center',
          top: 'middle',
          style: {
            text: '暂无价格数据',
            fontSize: 14,
            fill: '#999',
            textAlign: 'center'
          }
        }]
      })
    }
  } catch (error) {
    console.error('加载图表数据失败', error)
  }
}

// 重置监测筛选
const resetMonitorFilter = () => {
  selectedChartMarket.value = ''
  loadMonitorChart()
}

// 获取本地时间日期字符串（兼容中国时区）
const getLocalDateString = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 加载今日录入记录
const loadTodayRecords = async () => {
  try {
    const today = getLocalDateString()
    const res = await getPriceList({ pageNum: 1, pageSize: 100, category: 1, startDate: today, endDate: today })
    if (res.code === 200) {
      todayRecords.value = res.data.records || []
    }
  } catch (error) {
    console.error('加载今日录入记录失败', error)
  }
}

// 表格行样式
const tableRowClassName = ({ row, rowIndex }) => {
  return rowIndex % 2 === 0 ? 'even-row' : 'odd-row'
}

// 加载最近录入记录（兼容旧代码）
const loadPriceRecords = async () => {
  try {
    const res = await getPriceList({ pageNum: 1, pageSize: 20, category: 1 })
    if (res.code === 200) {
      todayRecords.value = res.data.records || []
    }
  } catch (error) {
    console.error('加载录入记录失败', error)
  }
}

const handleResize = () => {
  if (monitorChart) {
    monitorChart.resize()
  }
}

onMounted(() => {
  loadMarkets()
  loadPrices()
  loadTodayRecords()
  setTimeout(() => {
    loadMonitorChart()
  }, 300)
  window.addEventListener('resize', handleResize)

  // 图表容器内阻止页面滚动
  setTimeout(() => {
    const chartContainers = document.querySelectorAll('.chart-container-large')
    chartContainers.forEach(container => {
      container.addEventListener('wheel', (e) => {
        e.stopPropagation()
      }, { passive: false })
    })
  }, 500)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (monitorChart) {
    monitorChart.dispose()
  }
})
</script>
<style scoped>
/* 页面根容器：整个价格管理页面的最外层包裹元素 */
.admin-prices {
  margin: 0;
  padding: 16px;
  width: 100%;
  min-height: calc(100vh - 60px);
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  gap: 16px;
  background: #f5f7fa;
}

/* 统一内容包装器：页面通用内容容器，统一约束宽度 */
.page-wrapper {
  width: 100%;
  max-width: 100%;
}

/* Tabs 容器：自定义标签页整体外层样式 */
.custom-tabs {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  background: white;
  border: 1px solid #e4e7ed;
  width: 100%;
  flex: 1;
  display: flex;
  flex-direction: column;
}

/* 标签页内容区域：Tab切换后显示的内容区域 */
.tab-content {
  padding: 16px;
  background: #f5f7fa;
  flex: 1;
}

/* 卡片头部：页面里所有卡片的标题栏容器 */
.card-header {
  display: flex;     /* 弹性布局，左右分布元素 */
  justify-content: space-between; /* 左右两端对齐，中间留白 */
  align-items: center; /* 垂直方向居中对齐 */
  flex-wrap: wrap;   /* 空间不足时自动换行 */
  gap: 10px;         /* 内部元素间距10px */
}

/* 卡片标题：卡片顶部的标题文字 */
.card-title {
  font-size: 15px;   /* 字体大小15px */
  font-weight: 600;  /* 字体加粗（半粗体） */
  color: #1a1a2e;    /* 字体颜色：深灰近黑 */
}

/* 价格录入表单提示 */
.form-tip {
  font-size: 12px;
  color: #999;
  font-weight: normal;
}

/* 价格录入表单样式 */
.price-form-inline {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  align-items: flex-end;
}

.price-form-inline .el-form-item {
  margin-bottom: 0;
}

/* 价格录入卡片 */
.price-form-card {
  margin-bottom: 12px;
  border-left: 4px solid #4A7C59;
}

/* 今日录入记录卡片 */
.today-records-card {
  border-left: 4px solid #E6A23C;
}

/* 筛选控件区域：搜索、下拉、按钮等筛选组件的容器 */
.filter-controls {
  display: flex;     /* 横向排列 */
  align-items: center; /* 垂直居中 */
  gap: 12px;         /* 控件之间间距12px */
  flex-wrap: wrap;   /* 空间不足自动换行 */
}

/* 时间选择器区域：日期选择器容器 */
.time-selector {
  display: flex;     /* 横向排列 */
  align-items: center; /* 垂直居中 */
}

/* 监控图表卡片 */
.monitor-chart-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  margin-bottom: 12px;
  border-radius: 8px;
}

.monitor-chart-card :deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
}

/* 图表容器：所有ECharts/图表渲染区域 */
.chart-container {
  height: 300px;
  width: 100%;
}

/* 大图表容器 */
.chart-container-large {
  flex: 1;
  min-height: 600px;
  width: 100%;
}

/* 最高价/最低价样式 */
.price-max {
  color: #E74C3C;
  font-weight: 600;
}

.price-min {
  color: #4A7C59;
  font-weight: 600;
}

/* 监控图表包裹层：监控页面的图表容器 */
.monitor-chart-wrapper {
  width: 100%;        /* 宽度100% */
  height: 320px;      /* 高度320px */
  /* 背景淡色渐变 */
  background: linear-gradient(135deg, #fafbfc 0%, #f5f9f5 100%);
  border-radius: 8px; /* 圆角8px */
  padding: 12px;     /* 内边距12px */
  box-sizing: border-box; /* 内边距不撑开盒子 */
}

/* 记录数量文字：列表下方的总记录数提示 */
.record-count {
  font-size: 12px;    /* 小字体12px */
  color: #999;        /* 浅灰色，弱化显示 */
}

/* 价格高亮文字：重点显示的价格数值 */
.price-highlight {
  color: #4A7C59;     /* 墨绿色，突出价格 */
  font-weight: 700;   /* 加粗 */
  font-size: 15px;    /* 字体15px */
}

/* 价格单位文字：价格后面的单位（元/吨等） */
.price-unit-text {
  color: #999;        /* 浅灰色 */
  font-size: 12px;    /* 小字体 */
  margin-left: 4px;  /* 左边距4px，和价格隔开 */
}

/* ---------- Element Plus 深度修改样式（作用于子组件） ---------- */
/* 深度修改：Tabs头部 */
:deep(.el-tabs__header) {
  border-radius: 8px 8px 0 0; /* 顶部左右圆角，底部无圆角 */
}

/* 深度修改：Tabs导航栏底部横线 */
:deep(.el-tabs__nav-wrap::after) {
  height: 1px; /* 底部边框高度1px */
}

/* 深度修改：每一个Tab选项卡文字 */
:deep(.el-tabs__item) {
  font-size: 14px;   /* 字体14px */
  height: 44px;      /* 选项卡高度44px */
  line-height: 44px; /* 行高等于高度，文字垂直居中 */
}

/* 深度修改：Tab内容区域背景 */
:deep(.el-tabs__content) {
  background: #f5f7fa; /* 浅灰色背景 */
}

/* 深度修改：表格整体样式 */
:deep(.el-table) {
  border-radius: 8px; /* 表格圆角8px */
  overflow: hidden;   /* 隐藏超出部分，让圆角生效 */
}

/* 深度修改：表格表头文字 */
:deep(.el-table th) {
  font-weight: 600;  /* 表头文字加粗 */
}
</style>