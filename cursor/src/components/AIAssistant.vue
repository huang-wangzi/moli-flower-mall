<template>
  <Teleport to="body">
    <!-- AI助手悬浮按钮 -->
    <!-- 只在登录后显示 -->
    <div 
      class="ai-assistant-btn" 
      v-if="!isOpen && isLoggedIn" 
      :style="btnPositionStyle"
      @click="handleBtnClick"
    >
      <div class="ai-icon" @click.stop="openAssistant">
        <svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor">
          <path d="M12 2a2 2 0 0 1 2 2c0 .74-.4 1.39-1 1.73V7h1a7 7 0 0 1 7 7h1a1 1 0 0 1 1 1v3a1 1 0 0 1-1 1h-1v1a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-1H2a1 1 0 0 1-1-1v-3a1 1 0 0 1 1-1h1a7 7 0 0 1 7-7h1V5.73c-.6-.34-1-.99-1-1.73a2 2 0 0 1 2-2M7.5 13A2.5 2.5 0 0 0 5 15.5A2.5 2.5 0 0 0 7.5 18a2.5 2.5 0 0 0 2.5-2.5A2.5 2.5 0 0 0 7.5 13m9 0a2.5 2.5 0 0 0-2.5 2.5a2.5 2.5 0 0 0 2.5 2.5a2.5 2.5 0 0 0 2.5-2.5a2.5 2.5 0 0 0-2.5-2.5z"/>
        </svg>
      </div>
      <span class="ai-label" @click.stop="openAssistant">AI助手</span>
      <div class="drag-hint" @mousedown.stop="startDragBtn" @touchstart.stop="startDragBtn">⋮⋮</div>
    </div>

    <!-- AI助手主界面 -->
    <!-- 只在登录后且打开时显示 -->
    <div 
      v-if="isOpen && isLoggedIn"
      class="ai-assistant" 
      :style="panelPositionStyle"
    >
      <div class="ai-header" @mousedown="startDragPanel" @touchstart="startDragPanel">
        <div class="drag-area">
          <div class="drag-dots" @mousedown.stop @touchstart.stop>⋮⋮</div>
        </div>
        <div class="ai-title">
          <span class="ai-avatar">🤖</span>
          <div class="ai-info">
            <span class="ai-name">茉莉花AI助手</span>
            <span class="ai-status">在线</span>
          </div>
        </div>
        <div class="ai-actions">
          <button class="ai-action-btn" @click="switchMode('chat')" :class="{ active: mode === 'chat' }" title="智能对话">
            💬
          </button>
          <button class="ai-action-btn" @click="switchMode('price')" :class="{ active: mode === 'price' }" title="价格查询">
            📊
          </button>
          <button class="ai-action-btn" @click="switchMode('weather')" :class="{ active: mode === 'weather' }" title="天气查询">
            🌤️
          </button>
          <button class="ai-action-btn" @click="switchMode('report')" :class="{ active: mode === 'report' }" title="市场简报">
            📰
          </button>
        </div>
        <button class="ai-close" @click="closeAssistant">×</button>
      </div>

      <div class="ai-messages" ref="messagesContainer">
        <div v-if="messages.length === 0" class="ai-welcome">
          <div class="welcome-icon">🌸</div>
          <div class="welcome-title">您好！我是茉莉花AI助手</div>
          <div class="welcome-desc">我可以帮您：</div>
          <div class="welcome-items">
            <div class="welcome-item" @click="quickAsk('当前茉莉花价格走势如何？')">
              <span>📈</span> 查询价格走势
            </div>
            <div class="welcome-item" @click="quickAsk('横州今天天气怎么样？')">
              <span>🌤️</span> 查询天气预报
            </div>
            <div class="welcome-item" @click="quickAsk('生成今日市场简报')">
              <span>📰</span> 生成市场简报
            </div>
            <div class="welcome-item" @click="quickAsk('茉莉花种植技术有哪些？')">
              <span>🌱</span> 种植技术问答
            </div>
            <div class="welcome-item" @click="quickAsk('有哪些价格预警信息？')">
              <span>⚠️</span> 价格预警查询
            </div>
            <div class="welcome-item" @click="quickAsk('天气对茉莉花价格有什么影响？')">
              <span>🔍</span> 天气价格分析
            </div>
          </div>
        </div>

        <div v-for="(msg, index) in messages" :key="index" 
             class="message" 
             :class="{ 'message-user': msg.role === 'user', 'message-ai': msg.role === 'assistant' }">
          <div class="message-avatar">
            {{ msg.role === 'user' ? '👤' : '🤖' }}
          </div>
          <div class="message-content">
            <div class="message-text" v-html="formatMessage(msg.content)"></div>
            <div class="message-time">{{ msg.time }}</div>
          </div>
        </div>

        <div v-if="isTyping" class="message message-ai">
          <div class="message-avatar">🤖</div>
          <div class="message-content">
            <div class="message-typing">
              <span></span><span></span><span></span>
            </div>
          </div>
        </div>
      </div>

      <!-- 免责声明 -->
      <div class="ai-disclaimer">
        <span class="disclaimer-icon">⚠️</span>
        本内容由AI生成，仅供参考，不构成投资或交易建议
      </div>

      <div class="ai-input">
        <input 
          type="text" 
          v-model="inputText" 
          placeholder="输入您的问题..."
          @keyup.enter="sendMessage"
        />
        <button class="send-btn" @click="sendMessage" :disabled="!inputText.trim() || isTyping">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
            <path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z"/>
          </svg>
        </button>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref, computed, nextTick, onMounted, onUnmounted, watch } from 'vue'
import axios from 'axios'
import { useUserStore } from '@/stores/user'
import { getNowWeather, getWeatherForecast, getComprehensiveWeather } from '@/api'
import { getPriceOverview, getPriceTrend, getPriceStats } from '@/api'
import { getRecentAlerts } from '@/api'

const userStore = useUserStore()

const isOpen = ref(false)
const isLoggedIn = computed(() => userStore.isLoggedIn && !!userStore.userInfo)
const mode = ref('chat')
const messages = ref([])
const inputText = ref('')
const isTyping = ref(false)
const messagesContainer = ref(null)

// 监听登录状态变化，加载用户聊天记录
watch(isLoggedIn, (loggedIn) => {
  if (loggedIn) {
    loadUserMessages()
  } else {
    messages.value = []
  }
})

// 加载用户聊天记录
const loadUserMessages = () => {
  const userId = userStore.userInfo?.id
  if (!userId) return
  
  const key = `ai_messages_${userId}`
  const stored = localStorage.getItem(key)
  if (stored) {
    messages.value = JSON.parse(stored)
  }
}

// 保存用户聊天记录
const saveUserMessages = () => {
  const userId = userStore.userInfo?.id
  if (!userId) return
  
  const key = `ai_messages_${userId}`
  localStorage.setItem(key, JSON.stringify(messages.value))
}

// DeepSeek API 配置
const DEEPSEEK_API_URL = 'https://api.deepseek.com/chat/completions'
const DEEPSEEK_API_KEY = '***DEEPSEEK_API_KEY_REMOVED***'

// 拖拽状态 - 按钮
const btnPosition = ref({ x: null, y: null })
const isDraggingBtn = ref(false)
const hasDraggedBtn = ref(false)
const dragStartBtn = ref({ x: 0, y: 0 })

// 拖拽状态 - 面板
const panelPosition = ref({ x: null, y: null })
const isDraggingPanel = ref(false)
const dragStartPanel = ref({ x: 0, y: 0, left: 0, top: 0 })

// 计算位置样式 - 按钮
const btnPositionStyle = computed(() => {
  const style = {}
  if (btnPosition.value.x !== null) {
    style.right = 'auto'
    style.left = btnPosition.value.x + 'px'
  }
  if (btnPosition.value.y !== null) {
    style.bottom = 'auto'
    style.top = btnPosition.value.y + 'px'
  }
  return style
})

// 计算位置样式 - 面板
const panelPositionStyle = computed(() => {
  const style = {}
  if (panelPosition.value.x !== null) {
    style.right = 'auto'
    style.left = panelPosition.value.x + 'px'
  }
  if (panelPosition.value.y !== null) {
    style.bottom = 'auto'
    style.top = panelPosition.value.y + 'px'
  }
  return style
})

// 开始拖拽按钮
const startDragBtn = (e) => {
  if (isOpen.value) return
  e.preventDefault()
  isDraggingBtn.value = true
  hasDraggedBtn.value = false
  const clientX = e.type === 'touchstart' ? e.touches[0].clientX : e.clientX
  const clientY = e.type === 'touchstart' ? e.touches[0].clientY : e.clientY
  
  dragStartBtn.value = {
    x: clientX,
    y: clientY,
    startX: btnPosition.value.x,
    startY: btnPosition.value.y
  }
  
  document.addEventListener('mousemove', onDragBtn)
  document.addEventListener('mouseup', stopDragBtn)
  document.addEventListener('touchmove', onDragBtn)
  document.addEventListener('touchend', stopDragBtn)
}

// 拖拽按钮中
const onDragBtn = (e) => {
  if (!isDraggingBtn.value) return
  e.preventDefault()
  const clientX = e.type === 'touchmove' ? e.touches[0].clientX : e.clientX
  const clientY = e.type === 'touchmove' ? e.touches[0].clientY : e.clientY
  
  const deltaX = clientX - dragStartBtn.value.x
  const deltaY = clientY - dragStartBtn.value.y
  
  // 如果移动超过5px，认为是拖拽
  if (Math.abs(deltaX) > 5 || Math.abs(deltaY) > 5) {
    hasDraggedBtn.value = true
  }
  
  let newX = dragStartBtn.value.startX !== null 
    ? dragStartBtn.value.startX + deltaX 
    : window.innerWidth - 140 + deltaX
  let newY = dragStartBtn.value.startY !== null 
    ? dragStartBtn.value.startY + deltaY 
    : window.innerHeight - 120 + deltaY
  
  // 边界限制
  newX = Math.max(0, Math.min(newX, window.innerWidth - 120))
  newY = Math.max(0, Math.min(newY, window.innerHeight - 60))
  
  btnPosition.value = { x: newX, y: newY }
}

// 停止拖拽按钮
const stopDragBtn = () => {
  isDraggingBtn.value = false
  setTimeout(() => {
    hasDraggedBtn.value = false
  }, 50)
  document.removeEventListener('mousemove', onDragBtn)
  document.removeEventListener('mouseup', stopDragBtn)
  document.removeEventListener('touchmove', onDragBtn)
  document.removeEventListener('touchend', stopDragBtn)
}

// 点击打开助手（仅在非拖拽时触发）
const handleBtnClick = () => {
  if (!hasDraggedBtn.value) {
    openAssistant()
  }
}

// 开始拖拽面板
const startDragPanel = (e) => {
  e.preventDefault()
  isDraggingPanel.value = true
  const clientX = e.type === 'touchstart' ? e.touches[0].clientX : e.clientX
  const clientY = e.type === 'touchstart' ? e.touches[0].clientY : e.clientY
  
  // 初始化面板位置（如果还没设置）
  if (panelPosition.value.x === null) {
    panelPosition.value.x = window.innerWidth - 400
  }
  if (panelPosition.value.y === null) {
    panelPosition.value.y = window.innerHeight - 570
  }
  
  dragStartPanel.value = {
    x: clientX,
    y: clientY,
    left: panelPosition.value.x,
    top: panelPosition.value.y
  }
  
  document.addEventListener('mousemove', onDragPanel)
  document.addEventListener('mouseup', stopDragPanel)
  document.addEventListener('touchmove', onDragPanel)
  document.addEventListener('touchend', stopDragPanel)
}

// 拖拽面板中
const onDragPanel = (e) => {
  if (!isDraggingPanel.value) return
  e.preventDefault()
  const clientX = e.type === 'touchmove' ? e.touches[0].clientX : e.clientX
  const clientY = e.type === 'touchmove' ? e.touches[0].clientY : e.clientY
  
  const deltaX = clientX - dragStartPanel.value.x
  const deltaY = clientY - dragStartPanel.value.y
  
  let newX = dragStartPanel.value.left + deltaX
  let newY = dragStartPanel.value.top + deltaY
  
  // 边界限制（考虑面板大小）
  newX = Math.max(0, Math.min(newX, window.innerWidth - 380))
  newY = Math.max(0, Math.min(newY, window.innerHeight - 550))
  
  panelPosition.value = { x: newX, y: newY }
}

// 停止拖拽面板
const stopDragPanel = () => {
  isDraggingPanel.value = false
  document.removeEventListener('mousemove', onDragPanel)
  document.removeEventListener('mouseup', stopDragPanel)
  document.removeEventListener('touchmove', onDragPanel)
  document.removeEventListener('touchend', stopDragPanel)
}

// 打开助手
const openAssistant = () => {
  isOpen.value = true
  loadUserMessages()
  if (messages.value.length === 0) {
    addWelcomeMessage()
  }
}

// 关闭助手
const closeAssistant = () => {
  isOpen.value = false
  saveUserMessages()
}

// 切换模式
const switchMode = (newMode) => {
  mode.value = newMode
  if (newMode === 'price') {
    quickAsk('查询当前茉莉花各品类价格')
  } else if (newMode === 'weather') {
    quickAsk('查询横州当前天气和种植建议')
  } else if (newMode === 'report') {
    quickAsk('生成今日市场简报')
  }
}

// 添加欢迎消息
const addWelcomeMessage = () => {
  messages.value.push({
    role: 'assistant',
    content: '您好！我是茉莉花AI助手，可以帮您查询价格、天气、市场简报等信息，也可以回答关于茉莉花种植、交易等方面的问题。请问有什么可以帮到您？',
    time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  })
}

// 快捷提问
const quickAsk = (question) => {
  inputText.value = question
  sendMessage()
}

// 发送消息
const sendMessage = async () => {
  const text = inputText.value.trim()
  if (!text || isTyping.value) return

  // 添加用户消息
  messages.value.push({
    role: 'user',
    content: text,
    time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  })

  inputText.value = ''
  isTyping.value = true
  scrollToBottom()

  try {
    // 分析问题类型
    const response = await analyzeAndRespond(text)
    
    // 添加AI回复
    messages.value.push({
      role: 'assistant',
      content: response,
      time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    })
  } catch (error) {
    console.error('AI响应错误:', error)
    messages.value.push({
      role: 'assistant',
      content: '抱歉，我现在遇到了一些问题，无法及时回复您。请稍后再试，或者您可以直接联系客服获取帮助。',
      time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    })
  } finally {
    isTyping.value = false
    scrollToBottom()
    saveUserMessages()
  }
}

// 分析问题并智能回复
const analyzeAndRespond = async (question) => {
  const q = question.toLowerCase()

  // 天气相关问题
  if (q.includes('天气') || q.includes('气温') || q.includes('下雨') || q.includes('湿度')) {
    return await handleWeatherQuestion(q)
  }

  // 价格相关问题
  if (q.includes('价格') || q.includes('涨') || q.includes('跌') || q.includes('报价')) {
    return await handlePriceQuestion(q)
  }

  // 预警相关问题
  if (q.includes('预警') || q.includes('警告') || q.includes('风险')) {
    return await handleAlertQuestion()
  }

  // 简报相关问题
  if (q.includes('简报') || q.includes('报告') || q.includes('日报') || q.includes('市场分析')) {
    return await handleBriefingQuestion()
  }

  // 种植技术问题
  if (q.includes('种植') || q.includes('培育') || q.includes('养殖') || q.includes('栽培')) {
    return await handlePlantingQuestion(q)
  }

  // 通用对话，使用DeepSeek API
  return await callDeepSeek(question)
}

// 处理天气问题
const handleWeatherQuestion = async (q) => {
  try {
    const res = await getComprehensiveWeather()
    if (res.code === 200 && res.data) {
      const now = res.data.now || {}
      const impact = res.data.impact || {}
      const forecast = res.data.forecast?.daily || res.data.forecast || []

      let response = `当前横州天气情况：\n`
      response += `天气：${now.text || '未知'}\n`
      response += `气温：${now.temp || '--'}°C（体感 ${now.feelsLike || '--'}°C）\n`
      response += `湿度：${now.humidity || '--'}%\n`
      response += `风力：${now.windDir || ''} ${now.windSpeed || '--'}km/h\n`
      response += `降雨量：${now.precip || 0}mm\n\n`

      if (impact.analysis) {
        response += `【茉莉花种植影响分析】\n`
        response += `种植指数：${impact.level || '--'}（${impact.score || '--'}分）\n`
        response += `${impact.analysis}\n`
      }

      // 价格预测（新增）
      if (impact.pricePrediction) {
        response += `\n【价格走势预测】\n`
        response += `预测走势：${impact.priceTrend || '分析中'}\n`
        response += `${impact.pricePrediction}\n`
      }

      if (q.includes('未来') || q.includes('预报') || q.includes('明天')) {
        response += `\n【天气预报】\n`
        forecast.slice(0, 3).forEach((day, i) => {
          const dayName = i === 0 ? '今天' : i === 1 ? '明天' : '后天'
          const weatherText = day.textDay || day.textNight || '未知'
          const tempMin = day.tempMin || '--'
          const tempMax = day.tempMax || '--'
          response += `${dayName}：${weatherText}，${tempMin}° ~ ${tempMax}°C\n`
        })
      }

      return response
    }
  } catch (error) {
    console.error('获取天气失败:', error)
  }
  return '抱歉，暂时无法获取天气数据，请稍后再试。'
}

// 处理价格问题
const handlePriceQuestion = async (q) => {
  try {
    const overviewRes = await getPriceOverview()
    const statsRes = await getPriceStats({})

    let response = `【横州茉莉花价格概览】\n\n`

    if (overviewRes.code === 200 && overviewRes.data) {
      const categories = overviewRes.data
      categories.forEach(cat => {
        const change = cat.change || 0
        const changeIcon = change >= 0 ? '↑' : '↓'
        const changeClass = change >= 0 ? '上涨' : '下跌'
        response += `${cat.icon || ''} ${cat.name || '未知品类'}：¥${cat.currentPrice || '0'} ${cat.unit || ''}\n`
        response += `   较昨日${changeClass}${Math.abs(change).toFixed(2)}% ${changeIcon}\n\n`
      })
    }

    if (statsRes.code === 200 && statsRes.data) {
      const stats = statsRes.data
      response += `【近30天统计】\n`
      response += `均价：¥${stats.avgPrice || '0'}\n`
      response += `最高价：¥${stats.maxPrice || '0'}\n`
      response += `最低价：¥${stats.minPrice || '0'}\n`
    }

    if (q.includes('走势') || q.includes('趋势')) {
      response += `\n如需查看详细价格走势图，请访问价格详情页面。`
    }

    return response
  } catch (error) {
    console.error('获取价格失败:', error)
  }
  return '抱歉，暂时无法获取价格数据，请稍后再试。'
}

// 处理预警问题 - 根据天气预测茉莉花价格
const handleAlertQuestion = async () => {
  try {
    // 获取天气数据和价格数据
    const [weatherRes, priceRes] = await Promise.all([
      getComprehensiveWeather(),
      getPriceOverview()
    ])
    
    let response = `【茉莉花价格预警分析】\n\n`

    if (weatherRes.code === 200 && weatherRes.data) {
      const now = weatherRes.data.now || {}
      const impact = weatherRes.data.impact || {}
      const forecast = weatherRes.data.forecast?.daily || weatherRes.data.forecast || []

      // 天气影响分析
      response += `【当前天气情况】\n`
      response += `天气：${now.text || '未知'}\n`
      response += `气温：${now.temp || '--'}°C\n`
      response += `湿度：${now.humidity || '--'}%\n`
      response += `风力：${now.windDir || ''} ${now.windSpeed || '--'}km/h\n\n`

      // 价格预测（直接使用后端计算的结果）
      if (impact.pricePrediction) {
        response += `【价格走势预测】\n`
        response += `预测走势：${impact.priceTrend || '分析中'}\n`
        response += `${impact.pricePrediction}\n\n`
      }

      // 种植影响分析
      if (impact.analysis) {
        response += `【种植影响分析】\n`
        response += `种植指数：${impact.level || '--'}（${impact.score || '--'}分）\n`
        response += `${impact.analysis}\n\n`
      }

      // 获取当前茉莉鲜花价格
      let currentPrice = 0
      if (priceRes.code === 200 && priceRes.data) {
        const jasminePrice = priceRes.data.find(p => p.id === 1 || p.name?.includes('鲜花'))
        if (jasminePrice) {
          currentPrice = Number(jasminePrice.currentPrice || 0)
        }
      }

      // 交易建议
      response += `【交易建议】\n`
      if (impact.priceScore >= 75) {
        response += `价格上涨趋势明显，建议提前采购或适量囤货\n`
      } else if (impact.priceScore <= 25) {
        response += `价格可能下跌，建议观望或按需采购\n`
      } else {
        response += `价格预计平稳，可正常交易\n`
      }

      if (currentPrice > 0) {
        response += `\n当前茉莉鲜花参考价：¥${currentPrice.toFixed(2)}/斤\n`
      }

      response += `\n⚠️ 本预测仅供参考，实际价格受多种因素影响\n`
      
    } else {
      response += `暂时无法获取天气数据，无法进行价格预测分析。\n`
    }
  } catch (error) {
    console.error('获取预警失败:', error)
    return '抱歉，暂时无法获取预警信息，请稍后再试。'
  }
}

// 根据天气预测茉莉花价格变化
const predictPriceFromWeather = (now, forecast) => {
  let change = 0
  const reasons = []
  
  // 温度影响
  const temp = parseFloat(now.temp) || 25
  if (temp < 15) {
    change -= 2
    reasons.push('气温偏低(<15°C)，茉莉花生长缓慢，供应减少，价格可能上涨')
  } else if (temp < 20) {
    change += 0.5
    reasons.push('气温适宜，茉莉花正常生长')
  } else if (temp > 35) {
    change -= 1.5
    reasons.push('高温天气(>35°C)可能影响花苞质量')
  } else if (temp >= 25 && temp <= 33) {
    change += 1
    reasons.push('温度适宜(25-33°C)，茉莉花生长良好，品质优良')
  }
  
  // 天气状况影响
  const weather = now.text || ''
  const weatherCode = now.code || ''
  
  if (weather.includes('雨') || weather.includes('雷')) {
    change -= 3
    reasons.push('雨天采摘困难，供应量大幅减少，价格预计上涨')
  } else if (weather.includes('阴') || weather.includes('多云')) {
    change -= 0.5
    reasons.push('阴天光照不足，可能影响茉莉花香精油含量')
  } else if (weather.includes('晴')) {
    change += 1.5
    reasons.push('晴天采摘质量最佳，市场供应充足')
  }
  
  // 湿度影响
  const humidity = parseFloat(now.humidity) || 50
  if (humidity < 40) {
    change -= 1
    reasons.push('空气干燥，影响茉莉花开放度')
  } else if (humidity > 85) {
    change -= 1.5
    reasons.push('湿度过大易导致病虫害，品质可能下降')
  } else if (humidity >= 60 && humidity <= 80) {
    change += 0.5
    reasons.push('湿度适宜，茉莉花开放度好')
  }
  
  // 风力影响
  const windSpeed = parseFloat(now.windSpeed) || 0
  if (windSpeed > 20) {
    change -= 1
    reasons.push('风力较强，可能损伤花苞')
  }
  
  // 未来天气影响（如果能获取到预报）
  if (forecast && forecast.length > 0) {
    const tomorrow = forecast.find(d => d.fxDate !== getTodayStr())
    if (tomorrow) {
      if (tomorrow.textDay?.includes('雨')) {
        change += 1
        reasons.push('明日预报有雨，今日采购需求可能增加')
      }
    }
  }
  
  // 限制变化范围
  change = Math.max(-15, Math.min(15, change))
  
  // 计算价格区间（基于典型茉莉花价格 15-30元/斤）
  const basePrice = 22
  const changeAmount = basePrice * (change / 100)
  const min = basePrice + changeAmount - 3
  const max = basePrice + changeAmount + 3
  
  return {
    change: change,
    min: Math.max(10, min),
    max: Math.max(15, max),
    reason: reasons.length > 0 ? reasons.join('\n   ') : '天气条件总体良好，价格预计平稳'
  }
}

// 获取今天的日期字符串
const getTodayStr = () => {
  const now = new Date()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  return `${now.getFullYear()}-${month}-${day}`
}

// 处理简报问题
const handleBriefingQuestion = async () => {
  return `【市场简报功能】\n\n`
    + `我可以为您提供以下市场简报服务：\n\n`
    + `1. 每日市场行情分析\n`
    + `2. 价格走势回顾与预测\n`
    + `3. 天气对市场的影响分析\n`
    + `4. 交易建议与风险提示\n\n`
    + `请告诉我您需要：\n`
    + `- "生成今日市场简报"\n`
    + `- "查看本周价格走势"\n`
    + `- "分析近期市场变化"\n\n`
    + `或者您可以直接联系管理员生成最新简报。`
}

// 处理种植问题
const handlePlantingQuestion = async (q) => {
  // 本地知识库回答常见问题
  const plantingKnowledge = {
    '种植': `【茉莉花种植技术要点】

1. **土壤选择**：茉莉花喜酸性土壤，pH值5-6为宜，土层深厚、疏松肥沃。

2. **温度要求**：最适生长温度25-35°C，低于10°C停止生长，低于0°C可能受冻害。

3. **光照需求**：喜阳光充足，每天至少6小时光照，光照不足影响开花。

4. **水分管理**：
   - 生长期保持土壤湿润
   - 夏季高温早晚浇水
   - 雨季注意排水防涝
   - 忌积水

5. **施肥要点**：
   - 基肥：腐熟有机肥为主
   - 追肥：花前增施磷钾肥
   - 每7-10天施一次稀薄液肥

6. **修剪技术**：
   - 春季重剪促新枝
   - 花后及时剪除残花
   - 冬季轻剪保树形

7. **病虫害防治**：
   - 常见虫害：介壳虫、红蜘蛛
   - 常见病害：白绢病、叶斑病
   - 预防为主，定期喷药`,
    '浇水': `【茉莉花浇水指南】

1. **季节调整**：
   - 春季：2-3天浇一次
   - 夏季：早晚各浇一次，高温时喷雾增湿
   - 秋季：1-2天浇一次
   - 冬季：控制浇水，5-7天浇一次

2. **判断方法**：
   - 表土发白即需浇水
   - 用手指插入土中2-3cm感受湿度
   - 叶片萎蔫时及时补水

3. **注意事项**：
   - 浇水要浇透，避免浇"半截水"
   - 避免正午高温时浇水
   - 雨后及时排水
   - 使用微酸性水更好`,
    '施肥': `【茉莉花施肥指南】

1. **基肥**：春季每株施腐熟有机肥2-3公斤

2. **追肥方案**：
   - 3-4月：氮肥为主，促进新枝生长
   - 5-6月：磷钾肥为主，促进花芽分化
   - 7-8月：复合肥为主，保持开花
   - 9月后：停肥，准备越冬

3. **叶面施肥**：
   - 0.2%磷酸二氢钾溶液
   - 花期喷施，提高品质

4. **注意事项**：
   - 薄肥勤施，避免浓肥
   - 施肥后及时浇水
   - 雨天不施肥
   - 休眠期停肥`,
    '修剪': `【茉莉花修剪技术】

1. **春季修剪**（3月）：
   - 重剪，保留枝条10-15cm
   - 剪除枯枝、弱枝
   - 促进新枝萌发

2. **花后修剪**：
   - 及时剪除残花
   - 保留花下2-3对叶片
   - 促进二次开花

3. **秋季修剪**（10月）：
   - 轻剪，整理树形
   - 剪除徒长枝
   - 减少养分消耗

4. **冬季修剪**：
   - 一般不修剪
   - 仅剪除病虫枝

5. **注意事项**：
   - 使用锋利工具
   - 剪口要平滑
   - 伤口涂保护剂`
  }

  for (const [key, answer] of Object.entries(plantingKnowledge)) {
    if (q.includes(key)) {
      return answer
    }
  }

  return await callDeepSeek(question)
}

// 调用DeepSeek API
const callDeepSeek = async (question) => {
  try {
    const context = `你是横州茉莉花商城的AI助手，专门回答关于茉莉花交易、价格、种植等方面的问题。
请用专业但易懂的方式回答用户的问题。
如果涉及投资建议，请提醒用户谨慎决策。

用户问题：${question}`

    const response = await axios.post(
      DEEPSEEK_API_URL,
      {
        model: 'deepseek-chat',
        messages: [
          {
            role: 'system',
            content: '你是横州茉莉花商城的AI助手，专门回答关于茉莉花交易、价格、种植等方面的问题。请用专业但易懂的方式回答。如果涉及投资建议，请提醒用户谨慎决策。'
          },
          {
            role: 'user',
            content: question
          }
        ],
        temperature: 0.7,
        max_tokens: 1000
      },
      {
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${DEEPSEEK_API_KEY}`
        },
        timeout: 30000
      }
    )

    if (response.data?.choices?.[0]?.message?.content) {
      return response.data.choices[0].message.content
    }
  } catch (error) {
    console.error('DeepSeek API错误:', error)
    
    // 返回友好的降级回复
    if (error.code === 'ECONNABORTED') {
      return '抱歉，AI服务响应超时，请稍后再试。'
    }
    if (error.response?.status === 401) {
      return '抱歉，AI服务认证失败，请联系管理员检查配置。'
    }
    if (error.response?.status === 429) {
      return '抱歉，AI服务请求过于频繁，请稍后再试。'
    }
  }

  return '抱歉，我现在无法回答这个问题。您可以尝试：\n1. 重新表述您的问题\n2. 点击快捷问题获取帮助\n3. 联系客服获取人工服务'
}

// 格式化消息
const formatMessage = (content) => {
  if (!content) return ''
  // 简单处理换行
  let formatted = content.replace(/\n/g, '<br>')
  // 处理列表
  formatted = formatted.replace(/^- /gm, '• ')
  formatted = formatted.replace(/^\d+\. /gm, (m) => m)
  return formatted
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}
</script>

<style scoped>
.ai-assistant-btn {
  position: fixed;
  right: 20px;
  bottom: 80px;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  padding-right: 12px;
  background: linear-gradient(135deg, #4A7C59, #5BA06B);
  color: white;
  border-radius: 30px;
  cursor: pointer;
  box-shadow: 0 4px 20px rgba(74, 124, 89, 0.4);
  z-index: 9999;
  transition: box-shadow 0.3s;
  user-select: none;
}

.ai-assistant-btn:hover {
  box-shadow: 0 6px 25px rgba(74, 124, 89, 0.5);
}

.ai-assistant-btn:active {
  cursor: grabbing;
}

.ai-icon {
  width: 24px;
  height: 24px;
}

.ai-label {
  font-size: 14px;
  font-weight: 600;
}

.drag-hint {
  font-size: 16px;
  opacity: 0.7;
  cursor: grab;
  margin-left: 4px;
}

.drag-hint:active {
  cursor: grabbing;
}

.ai-assistant {
  position: fixed;
  right: 20px;
  bottom: 20px;
  width: 380px;
  height: 550px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 10px 50px rgba(0, 0, 0, 0.2);
  display: flex;
  flex-direction: column;
  z-index: 9999;
  overflow: hidden;
}

.ai-header {
  display: flex;
  align-items: center;
  padding: 16px;
  background: linear-gradient(135deg, #4A7C59, #5BA06B);
  color: white;
  cursor: move;
}

.drag-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  margin-right: 8px;
  padding: 4px;
}

.drag-dots {
  font-size: 18px;
  opacity: 0.6;
  cursor: grab;
  letter-spacing: 2px;
  line-height: 1;
}

.drag-dots:active {
  cursor: grabbing;
}

.ai-title {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
}

.ai-avatar {
  font-size: 28px;
}

.ai-info {
  display: flex;
  flex-direction: column;
}

.ai-name {
  font-size: 16px;
  font-weight: 600;
}

.ai-status {
  font-size: 12px;
  opacity: 0.9;
}

.ai-status::before {
  content: '';
  display: inline-block;
  width: 8px;
  height: 8px;
  background: #4ade80;
  border-radius: 50%;
  margin-right: 4px;
}

.ai-actions {
  display: flex;
  gap: 6px;
  margin-right: 12px;
  pointer-events: auto;
}

.ai-action-btn {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.2);
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}

.ai-action-btn:hover,
.ai-action-btn.active {
  background: rgba(255, 255, 255, 0.4);
  transform: scale(1.1);
}

.ai-close {
  width: 28px;
  height: 28px;
  border: none;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  color: white;
  font-size: 20px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  pointer-events: auto;
}

.ai-close:hover {
  background: rgba(255, 255, 255, 0.3);
}

.ai-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #f5f7f9;
  pointer-events: auto;
}

.ai-welcome {
  text-align: center;
  padding: 30px 20px;
}

.welcome-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.welcome-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.welcome-desc {
  font-size: 14px;
  color: #666;
  margin-bottom: 16px;
}

.welcome-items {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
  pointer-events: auto;
}

.welcome-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: white;
  border-radius: 10px;
  cursor: pointer;
  font-size: 13px;
  color: #333;
  transition: all 0.2s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.welcome-item:hover {
  background: #4A7C59;
  color: white;
  transform: translateY(-2px);
}

.welcome-item span {
  font-size: 18px;
}

.message {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
}

.message-user {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #e3e5e7;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  flex-shrink: 0;
}

.message-user .message-avatar {
  background: #4A7C59;
  color: white;
}

.message-content {
  max-width: 75%;
}

.message-text {
  padding: 12px 16px;
  border-radius: 16px;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
}

.message-user .message-text {
  background: #4A7C59;
  color: white;
  border-bottom-right-radius: 4px;
}

.message-ai .message-text {
  background: white;
  color: #333;
  border-bottom-left-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.message-time {
  font-size: 11px;
  color: #999;
  margin-top: 4px;
  padding: 0 4px;
}

.message-user .message-time {
  text-align: right;
}

.message-typing {
  display: flex;
  gap: 4px;
  padding: 16px;
  background: white;
  border-radius: 16px;
  width: fit-content;
}

.message-typing span {
  width: 8px;
  height: 8px;
  background: #4A7C59;
  border-radius: 50%;
  animation: typing 1.4s infinite ease-in-out;
}

.message-typing span:nth-child(2) {
  animation-delay: 0.2s;
}

.message-typing span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
  }
  30% {
    transform: translateY(-8px);
  }
}

.ai-disclaimer {
  padding: 8px 16px;
  background: #fff3cd;
  font-size: 11px;
  color: #856404;
  display: flex;
  align-items: center;
  gap: 6px;
  pointer-events: auto;
}

.disclaimer-icon {
  font-size: 14px;
}

.ai-input {
  display: flex;
  gap: 10px;
  padding: 12px 16px;
  background: white;
  border-top: 1px solid #eee;
  pointer-events: auto;
}

.ai-input input {
  flex: 1;
  padding: 10px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 24px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
  pointer-events: auto;
}

.ai-input input:focus {
  border-color: #4A7C59;
}

.send-btn {
  width: 44px;
  height: 44px;
  border: none;
  border-radius: 50%;
  background: #4A7C59;
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.send-btn:hover:not(:disabled) {
  background: #5BA06B;
  transform: scale(1.05);
}

.send-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

/* 响应式 */
@media (max-width: 480px) {
  .ai-assistant {
    right: 10px;
    bottom: 10px;
    width: calc(100vw - 20px);
    height: calc(100vh - 100px);
  }

  .ai-assistant-btn {
    right: 10px;
    bottom: 60px;
  }
}
</style>
