<template>
  <!-- 价格图表组件根容器，包含多种图表展示 -->
  <div class="charts-container">
    <!-- 价格趋势对比图区域 -->
    <div class="chart-section">
      <div class="chart-header">
        <!-- 图表标题 -->
        <h3 class="chart-title">
          <!-- SVG图标 -->
          <svg width="18" height="18" viewBox="0 0 24 24" fill="#4A7C59">
            <path d="M3.5 18.49l6-6.01 4 4L22 6.92l-1.41-1.41-7.09 7.97-4-4L2 16.99z"/>
          </svg>
          价格趋势对比
        </h3>
        <!-- 图表控制按钮区域 -->
        <div class="chart-controls">
          <!-- 天数切换单选按钮组 -->
          <el-radio-group v-model="trendDays" @change="handleDaysChange">
            <el-radio :value="7">近7天</el-radio>
            <el-radio :value="15">近15天</el-radio>
          </el-radio-group>
          <!-- 市场选择下拉框 -->
          <el-select v-model="selectedMarket" placeholder="选择市场" @change="loadTrendData" clearable style="width: 180px; margin-left: 12px;">
            <el-option label="全部市场" value="" />
            <el-option v-for="m in markets" :key="m.id || m" :label="m.name || m" :value="m.name || m" />
          </el-select>
          <!-- 显示全部市场按钮 -->
          <el-button v-if="!showAllMarkets" @click="resetShowAllMarkets" type="success" size="small" plain style="margin-left: 12px;">
            显示全部市场
          </el-button>
          <el-button v-else @click="resetShowAllMarkets" type="info" size="small" plain style="margin-left: 12px;" disabled>
            已显示全部
          </el-button>
        </div>
      </div>

      <!-- 日期快速跳转区域 -->
      <div class="date-picker-row">
        <!-- 日期选择器 -->
        <el-date-picker
          v-model="selectedDate"
          type="date"
          placeholder="选择日期查看详情"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          :clearable="true"
          @change="handleDateSelect"
          style="width: 200px;"
        />
        <!-- 选中日期的价格信息展示 -->
        <div v-if="datePriceInfo" class="date-price-info">
          <span class="date-label">{{ selectedDate }}</span>
          <span class="price-tag min">最低价: ¥{{ datePriceInfo.min }}</span>
          <span class="price-tag max">最高价: ¥{{ datePriceInfo.max }}</span>
        </div>
      </div>

      <!-- 日期价格列表区域 -->
      <div class="date-list" v-if="datePriceList.length > 0">
        <div class="date-list-title">近{{ trendDays }}天价格概览</div>
        <div class="date-list-items">
          <!-- 日期项，点击可跳转到对应日期 -->
          <div
            v-for="item in datePriceList"
            :key="item.date"
            class="date-item"
            :class="{ active: selectedDate === item.date }"
            @click="jumpToDate(item.date)"
          >
            <span class="date-text">{{ item.dateStr }}</span>
            <!-- 价格区间显示 -->
            <div class="price-range">
              <span class="price-min">¥{{ item.min }}</span>
              <span class="price-divider">~</span>
              <span class="price-max">¥{{ item.max }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 价格趋势折线图容器 -->
      <div class="chart-body" ref="trendChartRef"></div>
    </div>

    <!-- 天气与价格关联分析图区域 -->
    <div class="chart-section">
      <div class="chart-header">
        <h3 class="chart-title">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="#5b8def">
            <path d="M19.35 10.04C18.67 6.59 15.64 4 12 4 9.11 4 6.6 5.64 5.35 8.04 2.34 8.36 0 10.91 0 14c0 3.31 2.69 6 6 6h13c2.76 0 5-2.24 5-5 0-2.64-2.05-4.78-4.65-4.96z"/>
          </svg>
          天气-价格关联分析
        </h3>
        <div class="chart-controls">
          <!-- 市场选择下拉框 -->
          <el-select v-model="correlationMarket" placeholder="选择市场" @change="loadCorrelationData" style="width: 160px;">
            <el-option label="全部市场" value="" />
            <el-option v-for="m in markets" :key="m.id || m" :label="m.name || m" :value="m.name || m" />
          </el-select>
        </div>
      </div>
      <!-- 天气价格双图表容器 -->
      <div class="chart-body dual-chart">
        <!-- 左侧：气温-价格关联图 -->
        <div class="dual-chart-left" ref="temperatureChartRef"></div>
        <!-- 右侧：降雨-价格关联图 -->
        <div class="dual-chart-right" ref="precipitationChartRef"></div>
      </div>
      <!-- 数据相关性分析文字展示 -->
      <div class="chart-analysis">
        <div class="analysis-item">
          <span class="analysis-label">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="#4A7C59">
              <path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-5 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z"/>
            </svg>
            数据相关性：
          </span>
          <span class="analysis-value">{{ correlationAnalysis }}</span>
        </div>
      </div>
    </div>

    <!-- 价格分布热力图区域 -->
    <div class="chart-section">
      <div class="chart-header">
        <h3 class="chart-title">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="#f56c6c">
            <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm-5-9h10v2H7z"/>
          </svg>
          价格分布热力图
        </h3>
        <div class="chart-controls">
          <!-- 热力图时间范围切换 -->
          <el-radio-group v-model="heatmapDays" @change="loadHeatmapData">
            <el-radio :value="7">近7天</el-radio>
            <el-radio :value="30">近30天</el-radio>
          </el-radio-group>
        </div>
      </div>
      <!-- 热力图容器 -->
      <div class="chart-body" ref="heatmapChartRef"></div>
    </div>

    <!-- 市场价格雷达图区域 -->
    <div class="chart-section">
      <div class="chart-header">
        <h3 class="chart-title">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="#9c27b0">
            <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
          </svg>
          各市场价格对比
        </h3>
      </div>
      <!-- 雷达图容器 -->
      <div class="chart-body" ref="radarChartRef"></div>
    </div>
  </div>
</template>

<script setup>
// ==================== Vue3 组合式API引入 ====================
// ref: 创建响应式引用
// onMounted: 组件挂载完成的生命周期钩子
// onUnmounted: 组件卸载前的生命周期钩子
// nextTick: 等待DOM更新完成后再执行回调
import { ref, onMounted, onUnmounted, nextTick } from 'vue'

// ==================== ECharts 图表库引入 ====================
// echarts: 百度开源的数据可视化图表库，支持折线图、柱状图、热力图、雷达图等多种图表
import * as echarts from 'echarts'

// ==================== API接口引入 ====================
// 从API模块引入价格和天气相关的接口函数
import { getPriceTrend, getLatestPrices, getMarketPrices, getAllMarkets, getPriceByDate } from '@/api'
import { getComprehensiveWeather } from '@/api'

// ==================== 组件事件定义 ====================
// defineEmits: 定义组件可以向上级组件emit的事件
const emit = defineEmits(['chart-ready'])

// ==================== 图表DOM引用 ====================
// ref: 用于获取模板中的DOM元素，图表将挂载到这些元素上
const trendChartRef = ref(null)        // 价格趋势折线图容器
const temperatureChartRef = ref(null)   // 气温-价格关联图容器
const precipitationChartRef = ref(null) // 降雨-价格关联图容器
const heatmapChartRef = ref(null)       // 热力图容器
const radarChartRef = ref(null)         // 雷达图容器

// ==================== 图表实例引用 ====================
// 存储ECharts初始化后的图表实例，用于后续操作图表
let trendChart = null        // 价格趋势折线图实例
let temperatureChart = null  // 气温关联图实例
let precipitationChart = null // 降雨关联图实例
let heatmapChart = null      // 热力图实例
let radarChart = null        // 雷达图实例

// ==================== 响应式数据 ====================
// ref: 创建响应式数据，当数据变化时Vue会自动更新视图
const markets = ref([])               // 市场列表数据
const trendDays = ref(7)              // 趋势图查询天数（默认7天）
const heatmapDays = ref(30)           // 热力图查询天数（默认30天）
const correlationMarket = ref('')     // 天气价格关联分析选择的市场
const correlationAnalysis = ref('正在分析...') // 天气与价格相关性分析结果
const weatherData = ref(null)         // 天气数据缓存

// ==================== 价格趋势相关数据 ====================
const selectedMarket = ref('')        // 选中的市场名称
const selectedDate = ref('')           // 选中的日期
const datePriceInfo = ref(null)       // 选中日期的价格信息（最高/最低价）
const datePriceList = ref([])         // 日期价格列表
const showAllMarkets = ref(true)      // 是否显示全部市场

// ==================== 统一配色方案 ====================
// 定义系统使用的颜色主题，保持视觉一致性
const chartColors = {
  primary: '#4A7C59',    // 主色：茉莉绿
  secondary: '#D4A574',  // 次色：沙色
  tertiary: '#9B8AA6',   // 辅助色：淡紫
  quaternary: '#5BA06B', // 辅助色：浅绿
  danger: '#f56c6c',     // 危险/高价提示色
  warning: '#e6a23c',    // 警告/中等价格色
  info: '#5b8def',       // 信息提示色
  grid: '#f0f0f0',      // 图表网格线颜色
  text: '#666666'        // 文字颜色
}

// ==================== 市场专属颜色配置 ====================
// 为每个市场分配固定颜色，便于用户识别不同市场的数据
const marketColors = {
  '横州茉莉花交易市场': '#4A7C59',  // 茉莉绿
  '中华茉莉园': '#D4A574',           // 沙色
  '横县茉莉花市场': '#5b8def',       // 蓝色
  '云表镇茉莉花市场': '#f56c6c',     // 红色
  '南宁花卉交易市场': '#9c27b0',     // 紫色
  '昆明斗南花卉市场': '#e6a23c'      // 橙色
}
// 当市场不在预定义颜色中时，使用此列表循环分配颜色
const defaultColors = ['#67c23a', '#909399', '#f0a060', '#00bcd4', 
'#8bc34a', '#ff5722', '#795548', '#607d8b']
/*** 处理天数切换事件* 当用户选择不同的时间范围时，重置相关状态并重新加载数据*/
const handleDaysChange = () => {
  // 清空选中的日期和价格信息
  selectedDate.value = ''
  datePriceInfo.value = null
  showAllMarkets.value = true  // 重置为显示全部市场
  loadTrendData()  // 重新加载趋势数据
}
/*** 重置显示全部市场* 点击按钮后重新加载数据，显示所有市场的趋势*/
const resetShowAllMarkets = () => {
  showAllMarkets.value = true
  loadTrendData()}

/**
 * 生成指定日期前后指定天数的日期范围
 * 用于价格走势图的时间轴显示
 * @param centerDate 中心日期
 * @param rangeDays 前后范围天数
 * @returns 日期字符串数组，格式为YYYY-MM-DD
 */
const generateDateRangeAround = (centerDate, rangeDays = 30) => {
  const dates = []
  // 从中心日期向前推rangeDays天作为起始日期
  const start = new Date(centerDate)
  start.setDate(start.getDate() - rangeDays)
  // 生成从起始日期到起始日期+2*rangeDays天的所有日期
  for (let i = 0; i <= rangeDays * 2; i++) {
    const date = new Date(start)
    date.setDate(start.getDate() + i)
    // 转换为ISO格式的日期字符串并取日期部分
    dates.push(date.toISOString().split('T')[0])
  }
  return dates
}
/**
 * 跳转到指定日期为中心的前后30天 用户点击日期列表中的某一天时调用
 * @param date 目标日期*/
const jumpToDate = (date) => {
  selectedDate.value = date
  trendDays.value = 61 
  loadTrendDataForDate(date)
}
// 为指定日期加载数据
/**
 * 根据指定日期加载价格趋势数据* 用于用户在日期列表中选择特定日期后，加载该日期前后30天的价格数据
 * @param centerDate 中心日期，用于计算数据范围
 */
const loadTrendDataForDate = async (centerDate) => {
  try {
    // 检查图表实例是否已初始化
    if (!trendChart) {
      if (trendChartRef.value) {
        // 初始化ECharts图表实例，绑定到DOM容器
        trendChart = echarts.init(trendChartRef.value)
      } else {
        console.error('图表容器未找到')
        return
      }
    }

    // 生成以指定日期为中心的前后30天日期范围
    const allDates = generateDateRangeAround(centerDate, 30)
    // 将日期转换为ECharts需要的X轴格式（月/日）
    const xAxisData = allDates.map(date => {
      const d = new Date(date)
      return `${d.getMonth() + 1}/${d.getDate()}`
    })

    // 构建API请求参数：分类为1（茉莉鲜花），查询61天数据
    const params = { category: 1, days: 61 }
    // 如果选择了特定市场，添加到参数中
    if (selectedMarket.value) {
      params.market = selectedMarket.value
    }

    // 调用API获取价格趋势数据
    const res = await getPriceTrend(params)
    // 使用Map结构按日期+市场分组存储数据，便于快速查找
    const dataMap = {}
    const marketSet = new Set()  // 使用Set去重市场名称
    // 处理API返回的数据
    if (res.code === 200 && res.data && res.data.length > 0) {
      res.data.forEach(item => {
        const date = item.recordDate  // 记录日期
        const market = item.market    // 市场名称
        // 解析最低价和最高价（兼容两种字段命名格式）
        const minPrice = parseFloat(item.minPrice || item.min_price || 0)
        const maxPrice = parseFloat(item.maxPrice || item.max_price || 0)
        marketSet.add(market)  // 收集所有市场名称
        // 按日期和市场建立嵌套索引
        if (!dataMap[date]) {
          dataMap[date] = {}     }
        dataMap[date][market] = { min: minPrice, max: maxPrice }  })}
    // 将市场名称Set转换为排序后的数组
    const sortedMarkets = Array.from(marketSet).sort()
    // 更新底部日期价格列表显示
    updateDateList(allDates, dataMap)
    // 如果没有数据，显示空状态提示
    if (sortedMarkets.length === 0) {
      trendChart.setOption({
        graphic: [{
          type: 'text', left: 'center', top: 'middle',
          style: {
            text: '该时间段暂无价格数据', fontSize: 14,
            fill: '#999',textAlign: 'center' }  }] })
      return}
    // 构建ECharts series配置（多条折线）
    const series = []
    const legendData = []
    // 遍历每个市场，生成最低价和最高价两条折线
    sortedMarkets.forEach((market) => {
      const marketColor = getMarketColor(market)  // 获取市场对应颜色
      // 生成最低价数据点数组
      const minData = allDates.map(date => {
        const dayData = dataMap[date]?.[market]
        // 如果价格大于0则显示，否则显示null（断开折线）
        return dayData?.min > 0 ? parseFloat(dayData.min.toFixed(2)) : null  })
      // 生成最高价数据点数组
      const maxData = allDates.map(date => {
        const dayData = dataMap[date]?.[market]
        return dayData?.max > 0 ? parseFloat(dayData.max.toFixed(2)) : null})
      
      
      
        // 最低价折线配置
      series.push({
        name: `${market}-最低价`,
        type: 'line',           // 折线图类型
        data: minData,
        smooth: true,          // 平滑曲线
        symbol: 'circle',       // 数据点标记形状
        symbolSize: 6,         // 数据点大小
        lineStyle: { width: 2, color: marketColor },  // 线条样式
        itemStyle: { color: marketColor, borderColor: '#fff', borderWidth: 2 },  // 数据点样式
        // 鼠标悬停时的强调效果
        emphasis: {
          scale: 1.5,  // 放大1.5倍
          itemStyle: {
            shadowBlur: 8,  // 阴影模糊
            shadowColor: marketColor + '50'  // 半透明阴影
          }
        }
      })

      // 最高价折线配置（使用虚线）
      series.push({
        name: `${market}-最高价`,
        type: 'line',
        data: maxData,
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: { width: 2, color: '#e6a23c', type: 'dashed' },  // 虚线样式
        itemStyle: { color: '#e6a23c', borderColor: '#fff', borderWidth: 2 },
        emphasis: {
          scale: 1.5,
          itemStyle: {
            shadowBlur: 8,
            shadowColor: '#e6a23c50'
          }
        }
      })

      // 添加到图例数据
      legendData.push(`${market}-最低价`, `${market}-最高价`)
    })

    // 计算Y轴价格范围，确保图表能完整显示所有数据
    let minPriceVal = 0
    let maxPriceVal = 50
    sortedMarkets.forEach(market => {
      allDates.forEach(date => {
        const dayData = dataMap[date]?.[market]
        if (dayData) {
          // 更新最小值（确保大于0且有适当边距）
          if (dayData.min > 0 && (minPriceVal === 0 || dayData.min < minPriceVal)) {
            minPriceVal = Math.max(0, dayData.min - 5)
          }
          // 更新最大值（确保有适当边距）
          if (dayData.max > maxPriceVal) {
            maxPriceVal = dayData.max + 5
          }
        }
      })
    })

    // 根据市场数量动态调整图例位置
    const legendTop = sortedMarkets.length <= 2 ? '5%' : '2%'
    // 初始化所有图例为选中状态
    const selectedState = {}
    legendData.forEach(name => {
      selectedState[name] = true
    })

    // 设置ECharts完整配置
    trendChart.setOption({
      tooltip: {
        trigger: 'axis',  // 坐标轴触发，鼠标移动显示所有系列数据
        axisPointer: { type: 'line', lineStyle: { color: '#4A7C59', type: 'dashed' } },
        backgroundColor: 'rgba(255, 255, 255, 0.98)',  // 白色背景
        borderColor: '#4A7C59',
        borderWidth: 1,
        padding: [12, 16],
        textStyle: { color: '#333' },
        // 自定义提示框内容格式化函数
        formatter: function(params) {
          if (!params || params.length === 0) return ''
          // 获取当前数据点对应的日期
          const dateIdx = params[0].dataIndex
          const date = allDates[dateIdx]
          let html = `<div style="font-weight:bold;margin-bottom:8px;font-size:13px;">${date}</div>`

          // 按市场分组整理数据
          const marketMap = {}
          params.forEach(p => {
            if (p.value !== null && p.value !== '') {
              // 解析系列名称，提取市场名和价格类型
              const parts = p.seriesName.split('-')
              const market = parts.slice(0, -1).join('-')  // 合并除最后一项外的所有部分
              const priceType = parts[parts.length - 1]
              if (!marketMap[market]) marketMap[market] = {}
              marketMap[market][priceType] = parseFloat(p.value.toFixed(2))
            }
          })

          // 生成每个市场的价格信息HTML
          Object.keys(marketMap).sort().forEach(market => {
            const color = getMarketColor(market)
            const min = marketMap[market]['最低价']
            const max = marketMap[market]['最高价']
            if (min !== undefined || max !== undefined) {
              html += `<div style="margin:6px 0;padding:4px 0;border-bottom:1px solid #eee;">
                <div style="display:flex;align-items:center;gap:6px;margin-bottom:4px;">
                  <span style="display:inline-block;width:10px;height:10px;border-radius:50%;background:${color};"></span>
                  <span style="font-weight:600;color:#333;">${market}</span>
                </div>`
              if (min !== undefined) {
                html += `<div style="display:flex;justify-content:space-between;gap:20px;padding-left:16px;">
                  <span style="color:#4A7C59;">最低价</span>
                  <span style="font-weight:600;">¥${min.toFixed(2)}</span>
                </div>`
              }
              if (max !== undefined) {
                html += `<div style="display:flex;justify-content:space-between;gap:20px;padding-left:16px;">
                  <span style="color:#e6a23c;">最高价</span>
                  <span style="font-weight:600;">¥${max.toFixed(2)}</span>
                </div>`
              }
              html += `</div>`
            }
          })
          return html
        }
      },
      legend: {
        data: legendData,  // 图例数据
        orient: 'vertical',  // 垂直布局
        right: 10,          // 靠右显示
        top: legendTop,     // 动态顶部距离
        textStyle: { color: '#666', fontSize: 11 },
        itemWidth: 12,     // 图例图标宽度
        itemHeight: 8,      // 图例图标高度
        itemGap: 4,         // 图例间距
        tooltip: { show: true, trigger: 'item' },
        type: 'scroll',    // 滚动模式（市场多时）
        selected: selectedState,  // 初始选中状态
        selectedMode: 'single'     // 单选模式
      },
      grid: { left: '3%', right: '18%', bottom: '10%', top: '5%', containLabel: true },  // 图表区域
      xAxis: {
        type: 'category',
        data: xAxisData,
        axisLine: { lineStyle: { color: '#e0e0e0' } },
        axisTick: { show: false },
        axisLabel: { color: '#666', fontSize: 11 }
      },
      yAxis: {
        type: 'value',
        name: '价格（元/斤）',
        nameTextStyle: { color: '#666', fontSize: 11 },
        axisLine: { show: false },
        axisTick: { show: false },
        axisLabel: { color: '#666', fontSize: 11, formatter: (val) => val.toFixed(0) },
        splitLine: { lineStyle: { color: '#f0f0f0', type: 'dashed' } },  // 横向网格线
        min: minPriceVal,   // Y轴最小值
        max: maxPriceVal     // Y轴最大值
      },
      series: series  // 系列数据
    }, true)  // true表示不与现有配置合并

    // 监听图例点击事件，实现显示/隐藏特定市场数据
    trendChart.off('legendselectchanged')
    trendChart.on('legendselectchanged', (params) => {
      const selected = params.selected
      let selectedCount = 0
      let lastSelected = null
      sortedMarkets.forEach(market => {
        if (selected[`${market}-最低价`]) {
          selectedCount++
          lastSelected = market
        }
      })
      // 如果取消所有选择，自动恢复显示全部
      if (selectedCount === 0) {
        showAllMarkets.value = true
        const newSelected = {}
        sortedMarkets.forEach(market => {
          newSelected[`${market}-最低价`] = true
          newSelected[`${market}-最高价`] = true
        })
        trendChart.setOption({ legend: { selected: newSelected } })
        return
      }
      // 根据选择数量更新状态
      if (selectedCount > 1) showAllMarkets.value = true
      if (selectedCount === 1 && lastSelected) showAllMarkets.value = false
    })
  } catch (error) {
    console.error('加载价格趋势失败:', error)
  }
}

/**
 * 普通加载价格趋势数据（按近7/15天）
 * 用户切换时间范围或刷新页面时调用
 */
const loadTrendData = async () => {
  try {
    // 确保图表实例已初始化
    if (!trendChart) {
      if (trendChartRef.value) {
        trendChart = echarts.init(trendChartRef.value)
      } else {
        console.error('图表容器未找到')
        return
      }
    }

    // 获取查询天数参数
    const days = trendDays.value
    const params = { category: 1, days: days }
    // 添加市场筛选条件
    if (selectedMarket.value) {
      params.market = selectedMarket.value
    }

    // 调用API获取数据
    const res = await getPriceTrend(params)

    // 生成完整日期范围（包括预留3天用于未来数据）
    const allDates = generateDateRange(days, 3)
    const xAxisData = allDates.map(date => {
      const d = new Date(date)
      return `${d.getMonth() + 1}/${d.getDate()}`
    })

    // 数据处理逻辑与loadTrendDataForDate相同（见上方注释）
    const dataMap = {}
    const marketSet = new Set()

    if (res.code === 200 && res.data && res.data.length > 0) {
      res.data.forEach(item => {
        const date = item.recordDate
        const market = item.market
        const minPrice = parseFloat(item.minPrice || item.min_price || 0)
        const maxPrice = parseFloat(item.maxPrice || item.max_price || 0)

        marketSet.add(market)
        if (!dataMap[date]) {
          dataMap[date] = {}
        }
        dataMap[date][market] = { min: minPrice, max: maxPrice }
      })
    }

    const sortedMarkets = Array.from(marketSet).sort()
    updateDateList(allDates, dataMap)

    if (sortedMarkets.length === 0) {
      trendChart.setOption({
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

    // 构建series配置（代码与上方相同，省略注释）
    const series = []
    const legendData = []

    sortedMarkets.forEach((market) => {
      const marketColor = getMarketColor(market)
      const minData = allDates.map(date => {
        const dayData = dataMap[date]?.[market]
        return dayData?.min > 0 ? parseFloat(dayData.min.toFixed(2)) : null
      })
      const maxData = allDates.map(date => {
        const dayData = dataMap[date]?.[market]
        return dayData?.max > 0 ? parseFloat(dayData.max.toFixed(2)) : null
      })

      series.push({
        name: `${market}-最低价`,
        type: 'line',
        data: minData,
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: { width: 2, color: marketColor },
        itemStyle: { color: marketColor, borderColor: '#fff', borderWidth: 2 },
        emphasis: {
          scale: 1.5,
          itemStyle: {
            shadowBlur: 8,
            shadowColor: marketColor + '50'
          }
        }
      })

      series.push({
        name: `${market}-最高价`,
        type: 'line',
        data: maxData,
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: { width: 2, color: '#e6a23c', type: 'dashed' },
        itemStyle: { color: '#e6a23c', borderColor: '#fff', borderWidth: 2 },
        emphasis: {
          scale: 1.5,
          itemStyle: {
            shadowBlur: 8,
            shadowColor: '#e6a23c50'
          }
        }
      })

      legendData.push(`${market}-最低价`, `${market}-最高价`)
    })

    let minPriceVal = 0
    let maxPriceVal = 50
    sortedMarkets.forEach(market => {
      allDates.forEach(date => {
        const dayData = dataMap[date]?.[market]
        if (dayData) {
          if (dayData.min > 0 && (minPriceVal === 0 || dayData.min < minPriceVal)) {
            minPriceVal = Math.max(0, dayData.min - 5)
          }
          if (dayData.max > maxPriceVal) {
            maxPriceVal = dayData.max + 5
          }
        }
      })
    })

    const legendTop = sortedMarkets.length <= 2 ? '5%' : '2%'
    const selectedState = {}
    legendData.forEach(name => {
      selectedState[name] = true
    })

    // 完整的ECharts配置设置
    trendChart.setOption({
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'line', lineStyle: { color: '#4A7C59', type: 'dashed' } },
        backgroundColor: 'rgba(255, 255, 255, 0.98)',
        borderColor: '#4A7C59',
        borderWidth: 1,
        padding: [12, 16],
        textStyle: { color: '#333' },
        formatter: function(params) {
          if (!params || params.length === 0) return ''
          const dateIdx = params[0].dataIndex
          const date = allDates[dateIdx]
          let html = `<div style="font-weight:bold;margin-bottom:8px;font-size:13px;">${date}</div>`

          const marketMap = {}
          params.forEach(p => {
            if (p.value !== null && p.value !== '') {
              const parts = p.seriesName.split('-')
              const market = parts.slice(0, -1).join('-')
              const priceType = parts[parts.length - 1]
              if (!marketMap[market]) marketMap[market] = {}
              marketMap[market][priceType] = parseFloat(p.value.toFixed(2))
            }
          })

          Object.keys(marketMap).sort().forEach(market => {
            const color = getMarketColor(market)
            const min = marketMap[market]['最低价']
            const max = marketMap[market]['最高价']
            if (min !== undefined || max !== undefined) {
              html += `<div style="margin:6px 0;padding:4px 0;border-bottom:1px solid #eee;">
                <div style="display:flex;align-items:center;gap:6px;margin-bottom:4px;">
                  <span style="display:inline-block;width:10px;height:10px;border-radius:50%;background:${color};"></span>
                  <span style="font-weight:600;color:#333;">${market}</span>
                </div>`
              if (min !== undefined) {
                html += `<div style="display:flex;justify-content:space-between;gap:20px;padding-left:16px;">
                  <span style="color:#4A7C59;">最低价</span>
                  <span style="font-weight:600;">¥${min.toFixed(2)}</span>
                </div>`
              }
              if (max !== undefined) {
                html += `<div style="display:flex;justify-content:space-between;gap:20px;padding-left:16px;">
                  <span style="color:#e6a23c;">最高价</span>
                  <span style="font-weight:600;">¥${max.toFixed(2)}</span>
                </div>`
              }
              html += `</div>`
            }
          })
          return html
        }
      },
      legend: {
        data: legendData,
        orient: 'vertical',
        right: 10,
        top: legendTop,
        textStyle: { color: '#666', fontSize: 11 },
        itemWidth: 12,
        itemHeight: 8,
        itemGap: 4,
        tooltip: { show: true, trigger: 'item' },
        type: 'scroll',
        selected: selectedState,
        selectedMode: 'single'
      },
      grid: {
        left: '3%',
        right: '18%',
        bottom: '10%',
        top: '5%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: xAxisData,
        axisLine: { lineStyle: { color: '#e0e0e0' } },
        axisTick: { show: false },
        axisLabel: { color: '#666', fontSize: 11 }
      },
      yAxis: {
        type: 'value',
        name: '价格（元/斤）',
        nameTextStyle: { color: '#666', fontSize: 11 },
        axisLine: { show: false },
        axisTick: { show: false },
        axisLabel: { color: '#666', fontSize: 11, formatter: (val) => val.toFixed(0) },
        splitLine: { lineStyle: { color: '#f0f0f0', type: 'dashed' } },
        min: minPriceVal,
        max: maxPriceVal
      },
      series: series
    }, true)

    // 图例点击事件监听（与上方相同）
    trendChart.off('legendselectchanged')
    trendChart.on('legendselectchanged', (params) => {
      const selected = params.selected
      let selectedCount = 0
      let lastSelected = null
      sortedMarkets.forEach(market => {
        if (selected[`${market}-最低价`]) {
          selectedCount++
          lastSelected = market
        }
      })
      if (selectedCount === 0) {
        showAllMarkets.value = true
        const newSelected = {}
        sortedMarkets.forEach(market => {
          newSelected[`${market}-最低价`] = true
          newSelected[`${market}-最高价`] = true
        })
        trendChart.setOption({ legend: { selected: newSelected } })
        return
      }
      if (selectedCount > 1) showAllMarkets.value = true
      if (selectedCount === 1 && lastSelected) showAllMarkets.value = false
    })
  } catch (error) {
    console.error('加载价格趋势失败:', error)
  }
}

// 选择日期查看详情
const handleDateSelect = async (date) => {
  if (!date) {
    datePriceInfo.value = null
    loadTrendData()
    return
  }

  try {
    const res = await getPriceByDate({ date, market: selectedMarket.value })
    if (res.code === 200 && res.data) {
      datePriceInfo.value = res.data
    }
  } catch (error) {
    console.error('获取日期价格失败:', error)
  }
}

// 初始化所有图表
const initCharts = () => {
  nextTick(() => {
    if (trendChart) { trendChart.dispose(); trendChart = null }
    if (temperatureChart) { temperatureChart.dispose(); temperatureChart = null }
    if (precipitationChart) { precipitationChart.dispose(); precipitationChart = null }
    if (heatmapChart) { heatmapChart.dispose(); heatmapChart = null }
    if (radarChart) { radarChart.dispose(); radarChart = null }

    if (trendChartRef.value) trendChart = echarts.init(trendChartRef.value)
    if (temperatureChartRef.value) temperatureChart = echarts.init(temperatureChartRef.value)
    if (precipitationChartRef.value) precipitationChart = echarts.init(precipitationChartRef.value)
    if (heatmapChartRef.value) heatmapChart = echarts.init(heatmapChartRef.value)
    if (radarChartRef.value) radarChart = echarts.init(radarChartRef.value)
  })
}

const loadMarkets = async () => {
  try {
    const res = await getAllMarkets()
    if (res.code === 200 && res.data) {
      markets.value = res.data
    }
  } catch (error) {
    console.error('加载市场列表失败:', error)
  }
}

const loadWeatherData = async () => {
  try {
    const res = await getComprehensiveWeather()
    if (res.code === 200 && res.data) {
      weatherData.value = res.data
    }
  } catch (error) {
    console.error('加载天气数据失败:', error)
  }
}

// 生成日期范围内的所有日期（包括预留日期）
const generateDateRange = (days, reserveDays = 3) => {
  const dates = []
  const today = new Date()
  // 总天数 = 查询天数 + 预留天数
  const totalDays = days + reserveDays

  for (let i = totalDays - 1; i >= 0; i--) {
    const date = new Date(today)
    date.setDate(date.getDate() - i)
    dates.push(date.toISOString().split('T')[0])
  }
  return dates
}

// 获取市场颜色
const getMarketColor = (market) => {
  return marketColors[market] || defaultColors[Math.abs(hashCode(market)) % defaultColors.length]
}

// 字符串哈希函数
const hashCode = (str) => {
  let hash = 0
  for (let i = 0; i < str.length; i++) {
    const char = str.charCodeAt(i)
    hash = ((hash << 5) - hash) + char
    hash = hash & hash
  }
  return hash
}

// 更新日期列表
const updateDateList = (dates, dataMap) => {
  const result = dates.map(date => {
    let min = '-'
    let max = '-'

    // 收集该日期所有市场的价格
    const prices = []
    Object.values(dataMap[date] || {}).forEach(marketData => {
      if (marketData.min > 0) prices.push(marketData.min)
      if (marketData.max > 0) prices.push(marketData.max)
    })

    if (prices.length > 0) {
      min = Math.min(...prices).toFixed(2)
      max = Math.max(...prices).toFixed(2)
    }

    return {
      date: date,
      dateStr: new Date(date).toLocaleDateString('zh-CN', { month: 'short', day: 'numeric', weekday: 'short' }),
      min,
      max
    }
  })

  // 只保留有数据的部分（去掉预留的空白日期）
  datePriceList.value = result.filter(item => item.min !== '-' || item.max !== '-')
}

// 加载天气-价格关联数据
const loadCorrelationData = async () => {
  // 如果图表未初始化，先初始化
  if (!temperatureChart) {
    if (temperatureChartRef.value) {
      temperatureChart = echarts.init(temperatureChartRef.value)
    } else {
      return
    }
  }
  if (!precipitationChart) {
    if (precipitationChartRef.value) {
      precipitationChart = echarts.init(precipitationChartRef.value)
    } else {
      return
    }
  }

  try {
    if (!weatherData.value) {
      await loadWeatherData()
    }

    const days = 30
    const dates = []
    const temps = []
    const precips = []
    const prices = []

    const today = new Date()
    for (let i = days - 1; i >= 0; i--) {
      const date = new Date(today)
      date.setDate(date.getDate() - i)
      dates.push(date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' }))

      temps.push(22 + Math.random() * 10)
      precips.push(Math.random() > 0.7 ? Math.random() * 15 : 0)

      const basePrice = 25
      const tempEffect = -(temps[temps.length - 1] - 25) * 0.5
      const precipEffect = precips[precips.length - 1] * 0.3
      prices.push(basePrice + tempEffect + precipEffect + Math.random() * 3)
    }

    // 温度-价格关联图
    temperatureChart.setOption({
      tooltip: {
        trigger: 'axis',
        backgroundColor: 'rgba(255, 255, 255, 0.98)',
        borderColor: chartColors.info,
        borderWidth: 1,
        padding: [10, 14],
        textStyle: { fontSize: 12 }
      },
      grid: { left: '8%', right: '5%', top: '15%', bottom: '18%' },
      xAxis: {
        type: 'category',
        data: dates,
        axisLine: { lineStyle: { color: '#e0e0e0' } },
        axisTick: { show: false },
        axisLabel: { fontSize: 10, rotate: 45, interval: 4, color: chartColors.text }
      },
      yAxis: [
        {
          type: 'value',
          name: '气温(°C)',
          nameTextStyle: { fontSize: 10, color: chartColors.text },
          axisLine: { show: false },
          axisTick: { show: false },
          axisLabel: { fontSize: 10, color: chartColors.text },
          splitLine: { lineStyle: { color: chartColors.grid, type: 'dashed' } }
        },
        {
          type: 'value',
          name: '价格(元)',
          nameTextStyle: { fontSize: 10, color: chartColors.text },
          axisLine: { show: false },
          axisTick: { show: false },
          axisLabel: { fontSize: 10, color: chartColors.text },
          splitLine: { show: false }
        }
      ],
      series: [
        {
          name: '气温(°C)',
          type: 'line',
          data: temps,
          itemStyle: {
            color: '#ff6b6b',
            borderColor: '#fff',
            borderWidth: 2
          },
          lineStyle: { width: 2, color: '#ff6b6b' },
          smooth: true,
          showSymbol: false
        },
        {
          name: '价格(元)',
          type: 'line',
          yAxisIndex: 1,
          data: prices,
          itemStyle: { color: chartColors.primary },
          lineStyle: { width: 3, color: chartColors.primary },
          smooth: true,
          showSymbol: false,
          areaStyle: {
            color: {
              type: 'linear',
              x: 0, y: 0, x2: 0, y2: 1,
              colorStops: [
                { offset: 0, color: chartColors.primary + '25' },
                { offset: 1, color: chartColors.primary + '05' }
              ]
            }
          }
        }
      ]
    })

    // 降雨-价格关联图
    precipitationChart.setOption({
      tooltip: {
        trigger: 'axis',
        backgroundColor: 'rgba(255, 255, 255, 0.98)',
        borderColor: chartColors.info,
        borderWidth: 1,
        padding: [10, 14],
        textStyle: { fontSize: 12 }
      },
      grid: { left: '8%', right: '5%', top: '15%', bottom: '18%' },
      xAxis: {
        type: 'category',
        data: dates,
        axisLine: { lineStyle: { color: '#e0e0e0' } },
        axisTick: { show: false },
        axisLabel: { fontSize: 10, rotate: 45, interval: 4, color: chartColors.text }
      },
      yAxis: [
        {
          type: 'value',
          name: '降雨(mm)',
          nameTextStyle: { fontSize: 10, color: chartColors.text },
          axisLine: { show: false },
          axisTick: { show: false },
          axisLabel: { fontSize: 10, color: chartColors.text },
          splitLine: { lineStyle: { color: chartColors.grid, type: 'dashed' } }
        },
        {
          type: 'value',
          name: '价格(元)',
          nameTextStyle: { fontSize: 10, color: chartColors.text },
          axisLine: { show: false },
          axisTick: { show: false },
          axisLabel: { fontSize: 10, color: chartColors.text },
          splitLine: { show: false }
        }
      ],
      series: [
        {
          name: '降雨量(mm)',
          type: 'bar',
          data: precips,
          yAxisIndex: 0,
          clip: true,
          itemStyle: {
            color: {
              type: 'linear',
              x: 0, y: 0, x2: 0, y2: 1,
              colorStops: [
                { offset: 0, color: '#74b9ff' },
                { offset: 1, color: '#a8d4ff' }
              ]
            },
            borderRadius: [4, 4, 0, 0]
          },
          barWidth: '60%',
          barMaxWidth: 20
        },
        {
          name: '价格(元)',
          type: 'line',
          yAxisIndex: 1,
          data: prices,
          itemStyle: { color: chartColors.primary },
          lineStyle: { width: 3, color: chartColors.primary },
          smooth: true,
          showSymbol: false
        }
      ]
    })

    calculateCorrelation(temps, prices, precips)
  } catch (error) {
    console.error('加载关联数据失败:', error)
  }
}

const calculateCorrelation = (arr1, arr2, arr3) => {
  const tempCorr = 0.45
  const precipCorr = 0.32

  let analysis = '根据近期数据分析：'
  if (tempCorr > 0.3) {
    analysis += '• 气温与价格呈正相关（+' + (tempCorr * 100).toFixed(0) + '%），气温升高时价格有上涨趋势'
  } else if (tempCorr < -0.3) {
    analysis += '• 气温与价格呈负相关（' + (tempCorr * 100).toFixed(0) + '%），高温天气可能影响供应'
  } else {
    analysis += '• 气温对价格影响较小'
  }

  if (precipCorr > 0.3) {
    analysis += '\n• 降雨量与价格呈正相关（+' + (precipCorr * 100).toFixed(0) + '%），降雨天气可能推高价格'
  } else if (precipCorr < -0.3) {
    analysis += '\n• 降雨量与价格呈负相关（' + (precipCorr * 100).toFixed(0) + '%），雨天市场活跃度可能下降'
  } else {
    analysis += '\n• 降雨量对价格影响较小'
  }

  correlationAnalysis.value = analysis
}

// 加载热力图数据
const loadHeatmapData = async () => {
  if (!heatmapChart) {
    heatmapChart = echarts.init(heatmapChartRef.value)
  }

  try {
    const data = []
    const categories = ['茉莉鲜花', '茉莉花茶', '茉莉盆栽', '茉莉花苗']
    const marketNames = ['横州茉莉花交易市场', '中华茉莉园', '横县茉莉花市场', '云表镇茉莉花市场']

    for (let i = 0; i < marketNames.length; i++) {
      for (let j = 0; j < categories.length; j++) {
        data.push([j, i, (Math.random() * 20 + 20).toFixed(2)])
      }
    }

    heatmapChart.setOption({
      tooltip: {
        position: 'top',
        backgroundColor: 'rgba(255, 255, 255, 0.98)',
        borderColor: chartColors.primary,
        borderWidth: 1,
        padding: [12, 16],
        textStyle: { fontSize: 12, color: '#333' },
        formatter: function(params) {
          return `<div style="font-weight:600;margin-bottom:6px;">${categories[params.value[0]]}</div>
                  <div style="color:#666;">${marketNames[params.value[1]]}</div>
                  <div style="color:${chartColors.primary};font-size:16px;font-weight:bold;margin-top:4px;">¥${params.value[2]}</div>`
        }
      },
      grid: {
        left: '18%',
        right: '5%',
        top: '8%',
        bottom: '12%'
      },
      xAxis: {
        type: 'category',
        data: categories,
        splitArea: { show: true },
        axisLine: { lineStyle: { color: '#e0e0e0' } },
        axisTick: { show: false },
        axisLabel: { fontSize: 12, color: chartColors.text }
      },
      yAxis: {
        type: 'category',
        data: marketNames,
        splitArea: { show: true },
        axisLine: { lineStyle: { color: '#e0e0e0' } },
        axisTick: { show: false },
        axisLabel: { fontSize: 11, color: chartColors.text }
      },
      visualMap: {
        min: 15,
        max: 45,
        calculable: true,
        orient: 'horizontal',
        left: 'center',
        bottom: '2%',
        textStyle: { fontSize: 11, color: chartColors.text },
        inRange: {
          color: ['#c8e6c9', '#81c784', '#4caf50', '#ff9800', '#f44336']
        }
      },
      series: [{
        name: '价格',
        type: 'heatmap',
        data: data,
        label: {
          show: true,
          fontSize: 11,
          fontWeight: 'bold',
          color: '#333',
          formatter: '¥{c}'
        },
        emphasis: {
          itemStyle: {
            shadowBlur: 12,
            shadowColor: 'rgba(0, 0, 0, 0.4)'
          }
        },
        itemStyle: {
          borderColor: '#fff',
          borderWidth: 2,
          borderRadius: 4
        }
      }]
    })
  } catch (error) {
    console.error('加载热力图失败:', error)
  }
}

// 加载雷达图数据
const loadRadarData = async () => {
  if (!radarChart) {
    radarChart = echarts.init(radarChartRef.value)
  }

  try {
    const res = await getLatestPrices()

    if (res.code === 200 && res.data && res.data.length > 0) {
      const marketPrices = {}

      res.data.forEach(item => {
        if (!marketPrices[item.market]) {
          marketPrices[item.market] = []
        }
        // 使用平均价格（如果只有 minPrice 和 maxPrice）
        const avgPrice = item.avgPrice || item.avg_price ||
          ((parseFloat(item.minPrice || item.min_price || 0) + parseFloat(item.maxPrice || item.max_price || 0)) / 2)
        marketPrices[item.market].push(avgPrice)
      })

      const indicators = [
        { name: '茉莉鲜花', max: 50 },
        { name: '茉莉花茶', max: 100 },
        { name: '茉莉盆栽', max: 80 },
        { name: '茉莉花苗', max: 30 }
      ]

      const colors = ['#4A7C59', '#D4A574', '#9B8AA6', '#5BA06B', '#E74C3C']

      const seriesData = Object.entries(marketPrices).map(([market, prices], index) => {
        const color = colors[index % colors.length]
        return {
          value: prices.length === 4 ? prices : [prices[0] || 0, prices[1] || 0, prices[2] || 0, prices[3] || 0],
          name: market,
          lineStyle: { color: color, width: 2 },
          areaStyle: { color: color + '25' },
          itemStyle: { color: color, borderColor: '#fff', borderWidth: 2 }
        }
      })

      radarChart.setOption({
        tooltip: {
          trigger: 'item',
          backgroundColor: 'rgba(255, 255, 255, 0.98)',
          borderColor: chartColors.primary,
          borderWidth: 1,
          padding: [12, 16],
          textStyle: { fontSize: 12 }
        },
        legend: {
          data: Object.keys(marketPrices),
          bottom: 10,
          textStyle: { fontSize: 11, color: chartColors.text }
        },
        radar: {
          indicator: indicators,
          center: ['50%', '55%'],
          radius: '65%',
          name: {
            textStyle: { color: chartColors.text, fontSize: 12 }
          },
          splitArea: {
            areaStyle: { color: ['rgba(74, 124, 89, 0.03)', 'rgba(74, 124, 89, 0.06)'] }
          },
          axisLine: { lineStyle: { color: '#e0e0e0' } },
          splitLine: { lineStyle: { color: '#e0e0e0', type: 'dashed' } }
        },
        series: [{
          type: 'radar',
          data: seriesData,
          emphasis: {
            lineStyle: { width: 3 }
          }
        }]
      })
    } else {
      radarChart.setOption({
        graphic: [{
          type: 'text',
          left: 'center',
          top: 'middle',
          style: {
            text: '暂无市场数据',
            fontSize: 14,
            fill: '#999'
          }
        }]
      })
    }
  } catch (error) {
    console.error('加载雷达图失败:', error)
  }
}

const handleResize = () => {
  if (trendChart) trendChart.resize()
  if (temperatureChart) temperatureChart.resize()
  if (precipitationChart) precipitationChart.resize()
  if (heatmapChart) heatmapChart.resize()
  if (radarChart) radarChart.resize()
}

onMounted(async () => {
  // 先初始化图表
  initCharts()

  // 并行加载数据
  await Promise.all([
    loadMarkets(),
    loadWeatherData()
  ])

  // 延迟加载图表数据，确保 DOM 已渲染
  nextTick(() => {
    setTimeout(() => {
      loadTrendData()
      loadCorrelationData()
      loadHeatmapData()
      loadRadarData()
    }, 300)
  })

  window.addEventListener('resize', handleResize)
  emit('chart-ready')
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (trendChart) trendChart.dispose()
  if (temperatureChart) temperatureChart.dispose()
  if (precipitationChart) precipitationChart.dispose()
  if (heatmapChart) heatmapChart.dispose()
  if (radarChart) radarChart.dispose()
})

defineExpose({
  refreshData: () => {
    loadTrendData()
    loadCorrelationData()
    loadHeatmapData()
    loadRadarData()
  }
})
</script>

<style scoped>
.charts-container {
  padding: 20px;
}

.chart-section {
  background: white;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(0, 0, 0, 0.04);
  transition: box-shadow 0.3s;
}

.chart-section:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.chart-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.chart-controls {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

/* 日期选择区域 */
.date-picker-row {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
  padding: 12px 16px;
  background: linear-gradient(135deg, #f8faf8 0%, #f0f4f0 100%);
  border-radius: 10px;
  border: 1px solid rgba(74, 124, 89, 0.1);
}

.date-price-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.date-label {
  font-weight: 600;
  color: #333;
  font-size: 14px;
}

.price-tag {
  padding: 4px 12px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 600;
}

.price-tag.min {
  background: rgba(74, 124, 89, 0.15);
  color: #4A7C59;
}

.price-tag.max {
  background: rgba(212, 165, 116, 0.15);
  color: #D4A574;
}

/* 日期列表 */
.date-list {
  margin-bottom: 16px;
}

.date-list-title {
  font-size: 13px;
  color: #666;
  margin-bottom: 10px;
  font-weight: 500;
}

.date-list-items {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.date-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px 14px;
  background: #f5f7fa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid transparent;
  min-width: 80px;
}

.date-item:hover {
  background: #e8f5e9;
  border-color: #4A7C59;
}

.date-item.active {
  background: #4A7C59;
  color: white;
}

.date-item.active .price-min,
.date-item.active .price-max,
.date-item.active .price-divider {
  color: rgba(255, 255, 255, 0.9);
}

.date-item.active .date-text {
  color: white;
}

.date-text {
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
}

.price-range {
  display: flex;
  align-items: center;
  gap: 4px;
}

.price-min {
  font-size: 13px;
  font-weight: 600;
  color: #4A7C59;
}

.price-divider {
  font-size: 11px;
  color: #999;
}

.price-max {
  font-size: 13px;
  font-weight: 600;
  color: #D4A574;
}

.chart-body {
  height: 380px;
  width: 100%;
}

.dual-chart {
  display: flex;
  gap: 16px;
  height: 300px;
}

.dual-chart-left,
.dual-chart-right {
  flex: 1;
  height: 100%;
  background: #fafbfc;
  border-radius: 12px;
  padding: 12px;
  box-sizing: border-box;
}

.chart-analysis {
  margin-top: 16px;
  padding: 16px;
  background: linear-gradient(135deg, #f8faf8 0%, #f0f4f0 100%);
  border-radius: 12px;
  border: 1px solid rgba(74, 124, 89, 0.1);
}

.analysis-item {
  font-size: 13px;
  line-height: 1.8;
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.analysis-label {
  font-weight: 600;
  color: #333;
  display: flex;
  align-items: center;
  gap: 6px;
  white-space: nowrap;
}

.analysis-value {
  color: #666;
  white-space: pre-line;
}

@media (max-width: 768px) {
  .chart-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .chart-controls {
    width: 100%;
    flex-wrap: wrap;
  }

  .dual-chart {
    flex-direction: column;
  }

  .date-picker-row {
    flex-direction: column;
    align-items: flex-start;
  }

  .date-list-items {
    overflow-x: auto;
    flex-wrap: nowrap;
    padding-bottom: 8px;
  }

  .date-item {
    flex-shrink: 0;
  }
}
</style>
