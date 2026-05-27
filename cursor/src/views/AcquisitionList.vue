<template>
  <div class="acquisition-list-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">茉莉花收购专栏</h1>
      <p class="page-subtitle">花农直销渠道，减少中间环节，增加收入</p>
    </div>

    <!-- 收购商收购价格统计 -->
    <div class="price-stats-section">
      <div class="section-header">
        <h2 class="section-title">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="#e6a23c" style="vertical-align: middle; margin-right: 6px;">
            <path d="M16 6l2.29 2.29-4.88 4.88-4-4L2 16.59 3.41 18l6-6 4 4 6.3-6.29L22 12V6z"/>
          </svg>
          收购商收购价格统计
        </h2>
        <span class="section-desc">横州茉莉鲜花收购商发布的最高最低收购价格参考</span>
      </div>

      <!-- 收购价格概览 -->
      <div class="acquisition-overview" v-if="priceStats">
        <div class="overview-card acq-max">
          <div class="overview-icon">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="#f56c6c">
              <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z"/>
            </svg>
          </div>
          <div class="overview-content">
            <div class="overview-label">最高收购价</div>
            <div class="overview-value" style="color: #f56c6c;">¥{{ priceStats.maxPrice || '0.00' }}</div>
            <div class="overview-market">{{ priceStats.maxMarket || '-' }}</div>
          </div>
        </div>
        <div class="overview-card acq-min">
          <div class="overview-icon">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="#67c23a">
              <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z"/>
            </svg>
          </div>
          <div class="overview-content">
            <div class="overview-label">最低收购价</div>
            <div class="overview-value" style="color: #67c23a;">¥{{ priceStats.minPrice || '0.00' }}</div>
            <div class="overview-market">{{ priceStats.minMarket || '-' }}</div>
          </div>
        </div>
        <div class="overview-card acq-avg">
          <div class="overview-icon">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="#4A7C59">
              <path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-7 14l-5-5 1.41-1.41L12 14.17l4.59-4.58L18 11l-6 6z"/>
            </svg>
          </div>
          <div class="overview-content">
            <div class="overview-label">平均收购价</div>
            <div class="overview-value">¥{{ priceStats.avgPrice || '0.00' }}</div>
            <div class="overview-unit">元/斤</div>
          </div>
        </div>
        <div class="overview-card acq-count">
          <div class="overview-icon">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="#9c27b0">
              <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z"/>
            </svg>
          </div>
          <div class="overview-content">
            <div class="overview-label">收购商家数</div>
            <div class="overview-value" style="color: #9c27b0;">{{ priceStats.merchantCount || 0 }}</div>
            <div class="overview-unit">家</div>
          </div>
        </div>
      </div>

      <!-- 价格走势图 -->
      <div class="acquisition-chart" ref="acquisitionChartRef"></div>

      <!-- 收购商报价对比图表 -->
      <div class="acquisition-bar-section">
        <h3 class="bar-chart-title">收购商报价对比</h3>
        <div class="bar-chart-container" ref="merchantBarChartRef"></div>
      </div>

      <!-- 收购商报价列表 -->
      <div class="acquisition-list-container" v-if="demands.length > 0">
        <div class="acquisition-list-header">
          <span class="col-merchant">收购商</span>
          <span class="col-price-range">收购价格范围</span>
          <span class="col-quantity">需求量</span>
          <span class="col-expire">有效期至</span>
          <span class="col-action">操作</span>
        </div>
        <div class="acquisition-list-body">
          <div v-for="(item, index) in demands" :key="index" class="acquisition-row">
            <span class="col-merchant">
              <span class="merchant-avatar">{{ item.shopName ? item.shopName.charAt(0) : '商' }}</span>
              <span class="merchant-name">{{ item.shopName || item.title || '收购商' }}</span>
            </span>
            <span class="col-price-range">
              <span class="price-range">
                <span class="price-min">¥{{ item.priceMin || '0' }}</span>
                <span class="price-divider"> ~ </span>
                <span class="price-max">¥{{ item.priceMax || '0' }}</span>
              </span>
              <span class="price-unit-small">元/斤</span>
            </span>
            <span class="col-quantity">
              <span class="quantity-value">{{ item.quantityRemaining || 0 }}</span>
              <span class="quantity-unit">斤</span>
            </span>
            <span class="col-expire">{{ formatExpireDate(item.demandDate) }}</span>
            <span class="col-action">
              <el-button type="primary" size="small" text @click="goToDetail(item)">查看详情</el-button>
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- 统计概览 -->
    <div class="stats-overview" v-if="stats">
      <div class="stat-card">
        <span class="stat-icon">💰</span>
        <div class="stat-info">
          <span class="stat-value">¥{{ formatNumber(stats.totalAmount) }}</span>
          <span class="stat-label">累计收入</span>
        </div>
      </div>
      <div class="stat-card">
        <span class="stat-icon">📦</span>
        <div class="stat-info">
          <span class="stat-value">{{ formatNumber(stats.totalQuantity) }} 斤</span>
          <span class="stat-label">累计交货</span>
        </div>
      </div>
      <div class="stat-card" @click="$router.push('/acquisition/my-records')">
        <span class="stat-icon">📋</span>
        <div class="stat-info">
          <span class="stat-value">{{ stats.completedCount + stats.pendingCount }}</span>
          <span class="stat-label">我的申请</span>
        </div>
      </div>
    </div>

    <!-- 加载状态和需求列表 -->
    <template v-if="loading">
      <div class="loading-state">
        <el-icon class="is-loading"><Loading /></el-icon>
        <p>加载中...</p>
      </div>
    </template>
    <template v-else>
      <!-- 收购需求列表 -->
      <div class="demand-list-section">
        <div class="section-title" style="margin-top: 30px; margin-bottom: 20px;">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="#4A7C59" style="vertical-align: middle; margin-right: 6px;">
            <path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-5 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z"/>
          </svg>
          收购需求列表
        </div>

        <div class="demand-list" v-if="demands.length > 0">
          <div
            v-for="demand in demands"
            :key="demand.id + '-card'"
            class="demand-card neumorphic-card"
            @click="goToDetail(demand)"
          >
        <!-- 卡片头部 -->
        <div class="demand-header">
          <div class="shop-info">
            <span class="shop-avatar">{{ demand.shopName?.charAt(0) || '收' }}</span>
            <span class="shop-name">{{ demand.shopName }}</span>
          </div>
          <span class="demand-date">{{ formatDate(demand.demandDate) }}</span>
        </div>

        <!-- 卡片内容 -->
        <div class="demand-content">
          <h3 class="demand-title">{{ demand.title }}</h3>

          <!-- 价格区间 -->
          <div class="demand-price">
            <span class="price-icon">💵</span>
            <span class="price-range">
              {{ demand.priceMin }} - {{ demand.priceMax }} 元/{{ demand.unit }}
            </span>
          </div>

          <!-- 需求量 -->
          <div class="demand-quantity">
            <div class="quantity-info">
              <span>剩余收购量</span>
              <span class="quantity-value">{{ demand.quantityRemaining }} {{ demand.unit }}</span>
            </div>
            <el-progress
              :percentage="getProgress(demand)"
              :stroke-width="8"
              :show-text="false"
              color="#4A7C59"
            />
            <div class="quantity-text">
              已收 {{ subtract(demand.quantityNeeded, demand.quantityRemaining) }} / 计划 {{ demand.quantityNeeded }} {{ demand.unit }}
            </div>
          </div>

          <!-- 市场地址 -->
          <div class="demand-location">
            <span class="location-icon">📍</span>
            <span class="location-text">{{ demand.marketAddress }}</span>
          </div>

          <!-- 联系方式 -->
          <div class="demand-contact">
            <span class="contact-icon">📞</span>
            <span class="contact-text">{{ demand.contactPhone }}</span>
          </div>
        </div>

        <!-- 卡片底部 -->
        <div class="demand-footer">
          <span class="apply-btn" @click.stop="applyForDemand(demand)">
            申请供货
          </span>
        </div>
      </div>
      </div>
      <!-- 空状态 -->
      <div v-if="demands.length === 0" class="empty-state">
        <div class="empty-icon">🌸</div>
        <p class="empty-text">暂无收购需求</p>
        <p class="empty-hint">敬请期待收购商发布的需求</p>
      </div>
      </div>
    </template>

    <!-- 分页 -->
    <div class="pagination-wrapper" v-if="total > pageSize">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="loadDemands"
      />
    </div>

    <!-- 申请对话框 -->
    <el-dialog
      v-model="applyDialogVisible"
      title="申请供货"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="applyFormRef"
        :model="applyForm"
        :rules="applyRules"
        label-width="100px"
      >
        <el-form-item label="收购需求">
          <span class="demand-summary">{{ currentDemand?.title }}</span>
        </el-form-item>

        <el-form-item label="收购商">
          <span>{{ currentDemand?.shopName }}</span>
        </el-form-item>

        <el-form-item label="收购价格">
          <span class="price-text">
            {{ currentDemand?.priceMin }} - {{ currentDemand?.priceMax }} 元/{{ currentDemand?.unit }}
          </span>
        </el-form-item>

        <el-form-item label="茉莉花斤数" prop="quantity">
          <el-input-number
            v-model="applyForm.quantity"
            :min="1"
            :max="currentDemand?.quantityRemaining || 999"
            :step="1"
            :precision="0"
            style="width: 100%"
          />
          <span class="form-tip">当前需求剩余 {{ currentDemand?.quantityRemaining }} 斤</span>
        </el-form-item>

        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="applyForm.contactPhone" placeholder="请输入联系电话" />
        </el-form-item>

        <el-form-item label="茉莉花照片" prop="photoUrls">
          <div class="photo-upload">
            <el-upload
              action="#"
              :http-request="uploadPhoto"
              :before-upload="beforePhotoUpload"
              list-type="picture-card"
              :file-list="photoList"
              :on-remove="handlePhotoRemove"
              :on-error="handleUploadError"
            >
              <el-icon><Plus /></el-icon>
            </el-upload>
            <div class="upload-tip">上传今日采摘的茉莉花照片（最多3张）</div>
          </div>
        </el-form-item>

        <el-form-item label="备注说明">
          <el-input
            v-model="applyForm.remark"
            type="textarea"
            :rows="3"
            placeholder="可填写茉莉花品种、质量等信息"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="applyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitApplication" :loading="submitting">
          提交申请
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading, Plus } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getAcquisitionDemands, getMyAcquisitionStats, checkDemandApplied, createAcquisitionApplication, getAcquirerPriceStats, getAcquirerPriceTrend } from '@/api/acquisition'
import { uploadImage } from '@/api'

const router = useRouter()

// ==================== 数据定义 ====================

const loading = ref(false)
const demands = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 统计信息
const stats = ref(null)

// 收购商价格统计
const priceStats = ref(null)
const acquisitionChartRef = ref(null)
const merchantBarChartRef = ref(null)
let acquisitionChart = null
let merchantBarChart = null

// 图表配色
const chartColors = {
  primary: '#4A7C59',
  primaryLight: '#6b9b7a',
  primaryLighter: '#8dbf9e',
  grid: '#f0f0f0',
  text: '#666666',
  textLight: '#999999'
}

// 申请对话框
const applyDialogVisible = ref(false)
const submitting = ref(false)
const currentDemand = ref(null)
const applyFormRef = ref(null)
const applyForm = ref({
  quantity: 10,
  contactPhone: '',
  photoUrls: '',
  remark: ''
})
const photoList = ref([])

const applyRules = {
  quantity: [
    { required: true, message: '请输入茉莉花斤数', trigger: 'blur' }
  ],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

// ==================== 方法 ====================

/**
 * 加载收购需求列表
 */
const loadDemands = async () => {
  loading.value = true
  try {
    const res = await getAcquisitionDemands(currentPage.value, pageSize.value)
    if (res.code === 200 && res.data) {
      demands.value = res.data.records || res.data || []
      total.value = res.data.total || demands.value.length
      // 加载完成后更新条形图
      setTimeout(() => {
        loadMerchantBarChart()
      }, 100)
    } else {
      demands.value = []
    }
  } catch (error) {
    console.error('加载收购需求失败:', error)
    demands.value = []
  } finally {
    loading.value = false
  }
}

/**
 * 加载统计信息
 */
const loadStats = async () => {
  try {
    const res = await getMyAcquisitionStats()
    if (res.code === 200) {
      stats.value = res.data
    }
  } catch (error) {
    console.error('加载统计信息失败:', error)
  }
}

/**
 * 加载收购商价格统计
 */
const loadPriceStats = async () => {
  try {
    const res = await getAcquirerPriceStats()
    if (res.code === 200) {
      priceStats.value = res.data
    }
  } catch (error) {
    console.error('加载收购商价格统计失败:', error)
  }
}

/**
 * 加载价格趋势图
 */
const loadPriceTrendChart = async () => {
  if (!acquisitionChartRef.value) return
  await nextTick()

  if (!acquisitionChart) {
    acquisitionChart = echarts.init(acquisitionChartRef.value)
  }

  try {
    const res = await getAcquirerPriceTrend(30)
    if (res.code !== 200 || !res.data) {
      // 无数据时显示空状态
      acquisitionChart.setOption({
        title: { text: '暂无价格趋势数据', left: 'center', top: 'center', textStyle: { color: '#999', fontSize: 14 } },
        xAxis: { show: false },
        yAxis: { show: false }
      })
      return
    }

    const data = res.data
    const dates = data.map(item => String(item.date).substring(5)) // MM-DD格式
    const fullDates = data.map(item => item.date)
    const minPrices = data.map(item => {
      const val = item.minPrice || item.priceMin || item.price_min || 0
      return val ? parseFloat(val) : null
    })
    const maxPrices = data.map(item => {
      const val = item.maxPrice || item.priceMax || item.price_max || 0
      return val ? parseFloat(val) : null
    })
    const avgPrices = data.map(item => {
      const minVal = item.minPrice || item.priceMin || item.price_min || 0
      const maxVal = item.maxPrice || item.priceMax || item.price_max || 0
      if (minVal && maxVal) {
        return ((parseFloat(minVal) + parseFloat(maxVal)) / 2).toFixed(2)
      }
      return null
    })

    acquisitionChart.setOption({
      tooltip: {
        trigger: 'axis',
        backgroundColor: 'rgba(255,255,255,0.98)',
        borderColor: '#e6a23c',
        borderWidth: 1,
        padding: [12, 16],
        textStyle: { color: '#333' },
        formatter(params) {
          let r = `<div style="font-weight:600;margin-bottom:8px;">${fullDates[params[0].dataIndex]}</div>`
          params.forEach(p => {
            if (p.value != null) {
              r += `<div style="display:flex;align-items:center;gap:8px;margin:4px 0;">
                <span style="display:inline-block;width:10px;height:10px;border-radius:50%;background:${p.color};"></span>
                <span style="flex:1;color:#666;">${p.seriesName}</span>
                <span style="font-weight:700;color:${p.color};">¥${p.value}</span>
              </div>`
            }
          })
          return r
        }
      },
      legend: {
        data: ['最高价', '最低价', '均价'],
        bottom: 10,
        textStyle: { color: '#666', fontSize: 12 }
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '18%',
        top: '10%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: dates,
        axisLine: { lineStyle: { color: '#e0e0e0' } },
        axisTick: { show: false },
        axisLabel: { color: '#666', fontSize: 11, rotate: 45, interval: Math.max(0, Math.floor(dates.length / 8) - 1) }
      },
      yAxis: {
        type: 'value',
        name: '元/斤',
        axisLine: { show: false },
        axisTick: { show: false },
        axisLabel: { color: '#666' },
        splitLine: { lineStyle: { color: '#f0f0f0', type: 'dashed' } }
      },
      series: [
        {
          name: '最高价',
          type: 'line',
          smooth: 0.4,
          symbol: 'circle',
          symbolSize: 6,
          data: maxPrices,
          itemStyle: { color: '#f56c6c', borderColor: '#fff', borderWidth: 2 },
          lineStyle: { width: 3, color: '#f56c6c' },
          areaStyle: {
            color: {
              type: 'linear',
              x: 0, y: 0, x2: 0, y2: 1,
              colorStops: [
                { offset: 0, color: 'rgba(245,108,108,0.2)' },
                { offset: 1, color: 'rgba(245,108,108,0.02)' }
              ]
            }
          }
        },
        {
          name: '最低价',
          type: 'line',
          smooth: 0.4,
          symbol: 'circle',
          symbolSize: 6,
          data: minPrices,
          itemStyle: { color: '#67c23a', borderColor: '#fff', borderWidth: 2 },
          lineStyle: { width: 3, color: '#67c23a' },
          areaStyle: {
            color: {
              type: 'linear',
              x: 0, y: 0, x2: 0, y2: 1,
              colorStops: [
                { offset: 0, color: 'rgba(103,194,58,0.2)' },
                { offset: 1, color: 'rgba(103,194,58,0.02)' }
              ]
            }
          }
        },
        {
          name: '均价',
          type: 'line',
          smooth: 0.5,
          symbol: 'diamond',
          symbolSize: 7,
          data: avgPrices,
          itemStyle: { color: '#e6a23c', borderColor: '#fff', borderWidth: 2 },
          lineStyle: { width: 3, color: '#e6a23c' }
        }
      ]
    }, true)
  } catch (error) {
    console.error('加载收购价格走势图失败:', error)
  }
}

/**
 * 加载收购商报价对比条形图
 */
const loadMerchantBarChart = async () => {
  if (!merchantBarChartRef.value) return
  await nextTick()

  if (!merchantBarChart) {
    merchantBarChart = echarts.init(merchantBarChartRef.value)
  }

  const list = demands.value
  if (!list || list.length === 0) {
    merchantBarChart.setOption({
      title: { text: '暂无收购商报价数据', left: 'center', top: 'center', textStyle: { color: '#999', fontSize: 14 } },
      xAxis: { show: false },
      yAxis: { show: false }
    })
    return
  }

  // 取前10个收购商
  const topDemands = list.slice(0, 10)
  const merchants = topDemands.map(d => d.shopName || d.title || '收购商')
  
  // 兼容处理：尝试多种可能的字段名
  const maxPrices = topDemands.map(d => {
    const val = d.maxPrice || d.priceMax || d.price_max || d.price_maximum || d.max || 0
    return parseFloat(val) || 0
  })
  const minPrices = topDemands.map(d => {
    const val = d.minPrice || d.priceMin || d.price_min || d.price_minimum || d.min || 0
    return parseFloat(val) || 0
  })

  merchantBarChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      backgroundColor: 'rgba(255, 255, 255, 0.98)',
      borderColor: '#e6a23c',
      borderWidth: 1,
      padding: [12, 16],
      textStyle: { color: '#333' },
      formatter: function(params) {
        const merchant = params[0].name
        let result = '<div style="font-weight:600;margin-bottom:8px;">' + merchant + '</div>'
        params.forEach(p => {
          const color = p.seriesName === '最高价' ? '#f56c6c' : '#67c23a'
          result += '<div style="display:flex;align-items:center;gap:8px;margin:4px 0;">'
          result += '<span style="display:inline-block;width:10px;height:10px;border-radius:2px;background:' + color + ';"></span>'
          result += '<span style="flex:1;color:#666;">' + p.seriesName + '</span>'
          result += '<span style="font-weight:bold;color:' + color + ';">¥' + p.value + '</span>'
          result += '</div>'
        })
        return result
      }
    },
    legend: {
      data: ['最高价', '最低价'],
      bottom: 10,
      textStyle: { color: chartColors.text, fontSize: 12 }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '18%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'value',
      name: '价格（元）',
      nameTextStyle: { color: chartColors.text, fontSize: 11 },
      axisLine: { lineStyle: { color: '#e0e0e0' } },
      axisTick: { show: false },
      axisLabel: { color: chartColors.text },
      splitLine: { lineStyle: { color: chartColors.grid, type: 'dashed' } }
    },
    yAxis: {
      type: 'category',
      data: merchants,
      axisLine: { lineStyle: { color: '#e0e0e0' } },
      axisTick: { show: false },
      axisLabel: {
        color: chartColors.text,
        fontSize: 12,
        width: 80,
        overflow: 'truncate'
      }
    },
    series: [
      {
        name: '最高价',
        type: 'bar',
        data: maxPrices,
        barWidth: 12,
        itemStyle: {
          color: {
            type: 'linear',
            x: 0, y: 0, x2: 1, y2: 0,
            colorStops: [
              { offset: 0, color: '#f56c6c' },
              { offset: 1, color: '#ff9a9a' }
            ]
          },
          borderRadius: [0, 4, 4, 0]
        },
        label: {
          show: true,
          position: 'right',
          color: '#f56c6c',
          fontSize: 12,
          fontWeight: 'bold',
          formatter: '{c}'
        }
      },
      {
        name: '最低价',
        type: 'bar',
        data: minPrices,
        barWidth: 12,
        itemStyle: {
          color: {
            type: 'linear',
            x: 0, y: 0, x2: 1, y2: 0,
            colorStops: [
              { offset: 0, color: '#67c23a' },
              { offset: 1, color: '#95d57a' }
            ]
          },
          borderRadius: [0, 4, 4, 0]
        },
        label: {
          show: true,
          position: 'right',
          color: '#67c23a',
          fontSize: 12,
          fontWeight: 'bold',
          formatter: '{c}'
        }
      }
    ]
  }, true)
}

/**
 * 跳转详情页
 */
const goToDetail = (demand) => {
  router.push(`/acquisition/detail/${demand.id}`)
}

/**
 * 格式化有效期日期
 */
const formatExpireDate = (date) => {
  if (!date) return '-'
  const d = new Date(date)
  return `${d.getMonth() + 1}月${d.getDate()}日`
}

/**
 * 申请供货
 */
const applyForDemand = async (demand) => {
  // 检查是否已申请
  try {
    const checkRes = await checkDemandApplied(demand.id)
    if (checkRes.code === 200 && checkRes.data) {
      ElMessage.warning('您已申请过该收购需求，请勿重复申请')
      return
    }
  } catch (error) {
    console.error('检查申请状态失败:', error)
  }

  currentDemand.value = demand
  applyForm.value = {
    demandId: demand.id,
    quantity: 10,
    contactPhone: '',
    photoUrls: '',
    remark: ''
  }
  photoList.value = []
  applyDialogVisible.value = true
}

/**
 * 上传照片
 */
const uploadPhoto = async (options) => {
  const { file, onSuccess, onError } = options
  try {
    const res = await uploadImage(file)
    console.log('[上传图片] 响应:', res)
    if (res.code === 200) {
      const url = res.data || res.message || ''
      if (url) {
        photoList.value.push({ url, name: file.name, uid: file.uid })
        updatePhotoUrls()
        onSuccess({ url, ...res }, file)
      } else {
        onError(new Error('服务器返回的URL为空'))
      }
    } else {
      onError(new Error(res.message || '上传失败'))
    }
  } catch (error) {
    console.error('上传错误:', error)
    onError(error)
  }
}

const beforePhotoUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }
  if (photoList.value.length >= 3) {
    ElMessage.error('最多上传3张照片')
    return false
  }
  return true
}

const handlePhotoRemove = (file) => {
  const index = photoList.value.findIndex(f => f.uid === file.uid)
  if (index > -1) {
    photoList.value.splice(index, 1)
    updatePhotoUrls()
  }
}

const handleUploadError = (err, file) => {
  console.error('上传失败:', err)
  ElMessage.error('图片上传失败，请重试')
}

const updatePhotoUrls = () => {
  applyForm.value.photoUrls = JSON.stringify(photoList.value.map(p => p.url))
}

/**
 * 提交申请
 */
const submitApplication = async () => {
  try {
    await applyFormRef.value.validate()
  } catch {
    return
  }

  submitting.value = true
  try {
    const submitData = {
      demandId: currentDemand.value.id,
      quantity: applyForm.value.quantity,
      contactPhone: applyForm.value.contactPhone,
      photoUrls: applyForm.value.photoUrls,
      remark: applyForm.value.remark
    }
    console.log('[提交申请] 发送的数据:', submitData)
    
    const res = await createAcquisitionApplication(submitData)
    console.log('[提交申请] 响应:', res)

    if (res.code === 200) {
      ElMessage.success('申请提交成功，等待收购商审核')
      applyDialogVisible.value = false
      loadDemands()
    } else {
      ElMessage.error(res.message || '提交失败')
    }
  } catch (error) {
    console.error('[提交申请] 错误:', error)
    ElMessage.error(error.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

/**
 * 计算进度
 */
const getProgress = (demand) => {
  if (!demand.quantityNeeded) return 100
  const remaining = Number(demand.quantityRemaining || 0)
  const needed = Number(demand.quantityNeeded || 0)
  if (needed === 0) return 100
  return Math.round(((needed - remaining) / needed) * 100)
}

const subtract = (a, b) => {
  return (Number(a) - Number(b)).toFixed(1)
}

/**
 * 格式化数字
 */
const formatNumber = (value) => {
  if (!value) return '0'
  const num = Number(value)
  if (num >= 10000) {
    return (num / 10000).toFixed(2) + '万'
  }
  return num.toFixed(2)
}

/**
 * 格式化日期
 */
const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getMonth() + 1}月${d.getDate()}日`
}

/**
 * 窗口调整
 */
const handleResize = () => {
  if (acquisitionChart) acquisitionChart.resize()
  if (merchantBarChart) merchantBarChart.resize()
}

// ==================== 生命周期 ====================

onMounted(() => {
  loadDemands()
  loadStats()
  loadPriceStats()
  setTimeout(() => {
    loadPriceTrendChart()
  }, 300)
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (acquisitionChart) acquisitionChart.dispose()
  if (merchantBarChart) merchantBarChart.dispose()
})
</script>

<style scoped>
.acquisition-list-page {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

/* 页面标题 */
.page-header {
  text-align: center;
  margin-bottom: 30px;
}

.page-title {
  font-size: 28px;
  color: #333;
  margin-bottom: 8px;
}

.page-subtitle {
  font-size: 14px;
  color: #999;
}

/* 价格统计区域 */
.price-stats-section {
  background: linear-gradient(135deg, #fff 0%, #fefbf5 100%);
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(230, 162, 60, 0.2);
}

.section-header {
  margin-bottom: 20px;
}

.section-title {
  font-size: 17px;
  color: #1a1a2e;
  font-weight: 600;
  display: flex;
  align-items: center;
  margin: 0;
}

.section-desc {
  font-size: 13px;
  color: #888;
  margin-top: 4px;
  margin-left: 26px;
}

/* 收购价格概览 */
.acquisition-overview {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.overview-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  border: 1px solid #eee;
  transition: all 0.3s;
}

.overview-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
}

.overview-icon {
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  flex-shrink: 0;
}

.overview-card.acq-max .overview-icon { background: rgba(245, 108, 108, 0.1); }
.overview-card.acq-min .overview-icon { background: rgba(103, 194, 58, 0.1); }
.overview-card.acq-avg .overview-icon { background: rgba(74, 124, 89, 0.1); }
.overview-card.acq-count .overview-icon { background: rgba(147, 112, 219, 0.1); }

.overview-content {
  flex: 1;
  min-width: 0;
}

.overview-label {
  font-size: 13px;
  color: #888;
  margin-bottom: 4px;
  font-weight: 500;
}

.overview-value {
  font-size: 24px;
  font-weight: 700;
  color: #4A7C59;
  line-height: 1.2;
}

.overview-unit {
  font-size: 12px;
  color: #888;
}

.overview-market {
  font-size: 12px;
  color: #666;
  margin-top: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 图表 */
.acquisition-chart {
  width: 100%;
  height: 320px;
  background: #fafbfc;
  border-radius: 12px;
  padding: 16px;
  box-sizing: border-box;
  margin-bottom: 24px;
}

.acquisition-bar-section {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 24px;
  border: 1px solid #eee;
}

.bar-chart-title {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 16px;
}

.bar-chart-container {
  height: 300px;
  width: 100%;
}

/* 收购商报价列表 */
.acquisition-list-container {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #eee;
}

.acquisition-list-header {
  display: grid;
  grid-template-columns: 2fr 2fr 1fr 1.5fr 1fr;
  gap: 16px;
  padding: 14px 20px;
  background: linear-gradient(135deg, #e6a23c 0%, #f5a623 100%);
  color: #fff;
  font-size: 13px;
  font-weight: 600;
}

.acquisition-list-body {
  max-height: 400px;
  overflow-y: auto;
}

.acquisition-row {
  display: grid;
  grid-template-columns: 2fr 2fr 1fr 1.5fr 1fr;
  gap: 16px;
  padding: 16px 20px;
  align-items: center;
  border-bottom: 1px solid #f0f0f0;
  transition: all 0.2s;
}

.acquisition-row:hover {
  background: rgba(230, 162, 60, 0.04);
}

.acquisition-row:last-child {
  border-bottom: none;
}

.col-merchant {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 600;
  color: #333;
  font-size: 14px;
}

.merchant-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #e6a23c 0%, #f5a623 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  flex-shrink: 0;
}

.merchant-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.col-price-range {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.price-range {
  display: flex;
  align-items: center;
  gap: 4px;
}

.price-min {
  font-size: 16px;
  font-weight: bold;
  color: #67c23a;
}

.price-divider {
  color: #999;
  font-size: 12px;
}

.price-max {
  font-size: 16px;
  font-weight: bold;
  color: #f56c6c;
}

.price-unit-small {
  font-size: 12px;
  color: #888;
}

.col-quantity {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.quantity-value {
  font-size: 18px;
  font-weight: bold;
  color: #4A7C59;
}

.quantity-unit {
  font-size: 12px;
  color: #888;
}

.col-expire {
  font-size: 13px;
  color: #666;
}

/* 统计概览 */
.stats-overview {
  display: flex;
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px;
  background: #e3e5e7;
  border-radius: 16px;
  box-shadow: 4px 4px 8px #b6b9ba, -4px -4px 8px #fafafd;
  cursor: pointer;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-icon {
  font-size: 32px;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 20px;
  font-weight: bold;
  color: #4A7C59;
}

.stat-label {
  font-size: 12px;
  color: #999;
}

/* 加载状态 */
.loading-state {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}

.loading-state .el-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

/* 需求列表 */
.demand-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.demand-card {
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.demand-card:hover {
  transform: translateY(-4px);
  box-shadow: 12px 12px 20px #b6b9ba, -12px -12px 20px #fafafd;
}

/* 卡片头部 */
.demand-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #eee;
}

.shop-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.shop-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #4A7C59 0%, #6B9B7A 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: bold;
}

.shop-name {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.demand-date {
  font-size: 12px;
  color: #999;
}

/* 卡片内容 */
.demand-content {
  margin-bottom: 16px;
}

.demand-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}

.demand-price {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.price-icon {
  font-size: 18px;
}

.price-range {
  font-size: 16px;
  font-weight: bold;
  color: #e74c3c;
}

.demand-quantity {
  margin-bottom: 12px;
}

.quantity-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 14px;
  color: #666;
}

.quantity-value {
  font-weight: bold;
  color: #4A7C59;
}

.quantity-text {
  margin-top: 8px;
  font-size: 12px;
  color: #999;
}

.demand-location,
.demand-contact {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  font-size: 14px;
  color: #666;
}

.location-icon,
.contact-icon {
  font-size: 16px;
}

/* 卡片底部 */
.demand-footer {
  display: flex;
  justify-content: flex-end;
}

.apply-btn {
  padding: 10px 24px;
  background: linear-gradient(135deg, #4A7C59 0%, #6B9B7A 100%);
  color: white;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.apply-btn:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(74, 124, 89, 0.3);
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.empty-text {
  font-size: 18px;
  color: #666;
  margin-bottom: 8px;
}

.empty-hint {
  font-size: 14px;
  color: #999;
}

/* 分页 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

/* 申请对话框 */
.demand-summary,
.price-text {
  color: #4A7C59;
  font-weight: 600;
}

.form-tip {
  display: block;
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.photo-upload {
  width: 100%;
}

.upload-tip {
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}

/* 响应式 */
@media (max-width: 1024px) {
  .acquisition-overview {
    grid-template-columns: repeat(2, 1fr);
  }

  .acquisition-list-header,
  .acquisition-row {
    grid-template-columns: 2fr 2fr 1fr 1fr;
  }

  .col-expire {
    display: none;
  }
}

@media (max-width: 768px) {
  .acquisition-overview {
    grid-template-columns: 1fr;
  }

  .stats-overview {
    flex-direction: column;
  }

  .stat-card {
    padding: 16px;
  }

  .stat-value {
    font-size: 18px;
  }

  .acquisition-list-header {
    display: none;
  }

  .acquisition-row {
    display: flex;
    justify-content: space-between;
    padding: 16px;
    background: #fff;
    border-radius: 8px;
    margin-bottom: 8px;
    border: 1px solid #eee;
  }
}
</style>
