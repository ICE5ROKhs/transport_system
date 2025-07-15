<template>
  <div class="map-query-view">
    <div id="map-query-container" class="map-container"></div>
    
    <!-- 图层控制面板 -->
    <div class="layer-control">
      <h4>数据图层</h4>
      
      <!-- 热力图控制 -->
      <div class="control-item">
        <input 
          type="checkbox" 
          id="heatmap-toggle" 
          v-model="showHeatmap" 
          @change="toggleHeatmap"
          :disabled="loading"
        >
        <label for="heatmap-toggle">交通热力图</label>
        <span v-if="loading && loadingType === 'heatmap'" class="loading-indicator">加载中...</span>
      </div>
      
      <!-- 拥挤度控制 -->
      <div class="control-item">
        <input 
          type="checkbox" 
          id="congestion-toggle" 
          v-model="showCongestion" 
          @change="toggleCongestion"
          :disabled="loading"
        >
        <label for="congestion-toggle">拥挤度分析</label>
        <span v-if="loading && loadingType === 'congestion'" class="loading-indicator">加载中...</span>
      </div>
      
      <!-- 刷新按钮 -->
      <div class="control-item">
        <button @click="refreshData" class="refresh-btn" :disabled="loading">
          {{ loading ? '刷新中...' : '刷新数据' }}
        </button>
      </div>
      
      <!-- 数据更新时间 -->
      <div v-if="lastUpdated" class="update-time">
        <small>更新: {{ formatTime(lastUpdated) }}</small>
      </div>
    </div>
    
    <!-- 拥挤度统计面板 -->
    <div v-if="congestionStats && showCongestion" class="congestion-stats">
      <h4>拥挤度统计</h4>
      <div class="stats-grid">
        <div 
          v-for="region in congestionStats.regions" 
          :key="region.level" 
          class="stat-item"
          :class="getCongestionClass(region.level)"
        >
          <span class="level">{{ region.level }}</span>
          <span class="value">{{ region.value.toFixed(1) }}%</span>
        </div>
      </div>
      <div class="update-info">
        <small>数据时间: {{ congestionStats.updateTime }}</small>
      </div>
    </div>
    
    <!-- AI助手面板 -->
    <div class="ai-assistant">
      <div class="ai-header">
        <h4>🤖 AI智能助手</h4>
        <div class="ai-role-selector">
          <select v-model="selectedRole" @change="changeRole" class="role-select">
            <option value="traffic">交通专家</option>
            <option value="tourist">旅游向导</option>
            <option value="business">商务顾问</option>
            <option value="student">学生助手</option>
            <option value="elderly">老年关怀</option>
          </select>
        </div>
      </div>
      
      <!-- 对话区域 -->
      <div class="chat-container">
        <div class="chat-messages" ref="chatContainer">
          <div 
            v-for="(message, index) in chatMessages" 
            :key="index" 
            class="message"
            :class="message.type"
          >
            <div class="message-avatar">
              {{ message.type === 'user' ? '👤' : '🤖' }}
            </div>
            <div class="message-content">
              <div class="message-text">{{ message.text }}</div>
              <div class="message-time">{{ formatMessageTime(message.time) }}</div>
            </div>
          </div>
        </div>
        
        <!-- 输入区域 -->
        <div class="chat-input">
          <input 
            v-model="userInput" 
            @keyup.enter="sendMessage"
            placeholder="输入您的问题..."
            class="message-input"
            :disabled="aiLoading"
          >
          <button 
            @click="sendMessage" 
            class="send-btn"
            :disabled="!userInput.trim() || aiLoading"
          >
            {{ aiLoading ? '发送中...' : '发送' }}
          </button>
        </div>
      </div>
      
      <!-- 快捷问题 -->
      <div class="quick-questions">
        <h5>💡 快捷问题</h5>
        <div class="question-buttons">
          <button 
            v-for="question in getQuickQuestions()" 
            :key="question"
            @click="askQuickQuestion(question)"
            class="quick-btn"
            :disabled="aiLoading"
          >
            {{ question }}
          </button>
        </div>
      </div>
    </div>
    
    <!-- 错误提示 -->
    <div v-if="error" class="error-toast">
      <span>{{ error }}</span>
      <button @click="clearError" class="close-btn">×</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import AMapLoader from '@amap/amap-jsapi-loader'
import { dataAPI, aiAPI } from '../services/api.js'

// 地图相关
let map = null
let heatmap = null
let infoWindow = null
let congestionMarkers = []

// 响应式数据
const showHeatmap = ref(false)
const showCongestion = ref(false)
const loading = ref(false)
const loadingType = ref('')
const error = ref(null)
const lastUpdated = ref(null)

// 数据
const heatmapData = ref([])
const congestionStats = ref(null)

// AI助手相关
const selectedRole = ref('traffic')
const chatMessages = ref([])
const userInput = ref('')
const aiLoading = ref(false)
const chatContainer = ref(null)

// 配置安全密钥
window._AMapSecurityConfig = {
  securityJsCode: 'aceef7681b1b9bcfe0b886af40c120f1',
}

/**
 * 初始化地图
 */
onMounted(() => {
  AMapLoader.load({
    key: "2bec7f50935812fcf29c98e01c419f02",
    version: "2.0",
    plugins: ['AMap.HeatMap', 'AMap.InfoWindow', 'AMap.Marker'],
  }).then((AMap) => {
    map = new AMap.Map("map-query-container", {
      viewMode: "3D",
      zoom: 12,
      center: [116.397428, 39.90923], // 北京
    })

    // 初始化信息窗体
    infoWindow = new AMap.InfoWindow({
      offset: new AMap.Pixel(0, -30),
      closeWhenClickMap: true
    })

    // 初始化热力图
    heatmap = new AMap.HeatMap(map, {
      radius: 30,
      opacity: [0, 0.8],
      gradient: {
        0.4: 'blue',
        0.6: 'cyan',
        0.7: 'lime',
        0.8: 'yellow',
        1.0: 'red'
      }
    })

    // 地图点击事件
    map.on('click', (e) => {
      const { lng, lat } = e.lnglat
      showLocationInfo(lng, lat)
    })

    console.log('地图初始化完成')
  }).catch(e => {
    console.error("地图加载失败：", e)
    error.value = '地图加载失败，请刷新页面重试'
  })
  
  // 初始化AI助手
  const config = roleConfigs[selectedRole.value]
  addMessage('assistant', config.greeting)
})

/**
 * 获取热力图数据
 */
const fetchHeatmapData = async () => {
  loading.value = true
  loadingType.value = 'heatmap'
  error.value = null
  
  try {
    const response = await dataAPI.getHeatmapData()
    heatmapData.value = response.data.map(point => ({
      lng: point.lng,
      lat: point.lat,
      count: Math.round(point.intensity * 100) // 将强度转换为热力图所需的count值
    }))
    
    console.log('热力图数据获取成功:', heatmapData.value)
    lastUpdated.value = new Date()
    
  } catch (err) {
    error.value = '获取热力图数据失败: ' + (err.message || '网络错误')
    console.error('获取热力图数据失败:', err)
    showHeatmap.value = false
  } finally {
    loading.value = false
    loadingType.value = ''
  }
}

/**
 * 获取拥挤度数据
 */
const fetchCongestionData = async () => {
  loading.value = true
  loadingType.value = 'congestion'
  error.value = null
  
  try {
    const response = await dataAPI.getCongestionData()
    congestionStats.value = response.data
    
    console.log('拥挤度数据获取成功:', congestionStats.value)
    lastUpdated.value = new Date()
    
  } catch (err) {
    error.value = '获取拥挤度数据失败: ' + (err.message || '网络错误')
    console.error('获取拥挤度数据失败:', err)
    showCongestion.value = false
  } finally {
    loading.value = false
    loadingType.value = ''
  }
}

/**
 * 切换热力图显示
 */
const toggleHeatmap = async () => {
  if (showHeatmap.value) {
    if (heatmapData.value.length === 0) {
      await fetchHeatmapData()
    }
    
    if (heatmapData.value.length > 0) {
      const maxCount = Math.max(...heatmapData.value.map(item => item.count))
      heatmap.setDataSet({ 
        data: heatmapData.value, 
        max: maxCount 
      })
      heatmap.show()
    }
  } else {
    heatmap.hide()
  }
}

/**
 * 切换拥挤度显示
 */
const toggleCongestion = async () => {
  if (showCongestion.value) {
    if (!congestionStats.value) {
      await fetchCongestionData()
    }
    showCongestionMarkers()
  } else {
    hideCongestionMarkers()
  }
}

/**
 * 显示拥挤度标记
 */
const showCongestionMarkers = () => {
  if (!congestionStats.value || !map) return
  
  // 清除已有标记
  hideCongestionMarkers()
  
  // 模拟在地图上显示拥挤度区域标记
  const regions = [
    { level: '畅通', lng: 116.39, lat: 39.90, value: 25.5 },
    { level: '缓慢', lng: 116.41, lat: 39.91, value: 35.2 },
    { level: '拥堵', lng: 116.40, lat: 39.89, value: 28.8 },
    { level: '严重拥堵', lng: 116.42, lat: 39.92, value: 10.5 }
  ]
  
  regions.forEach(region => {
    const marker = new AMap.Marker({
      position: [region.lng, region.lat],
      content: createCongestionMarkerContent(region),
      offset: new AMap.Pixel(-15, -15)
    })
    
    marker.on('click', () => {
      showRegionInfo(region)
    })
    
    marker.setMap(map)
    congestionMarkers.push(marker)
  })
}

/**
 * 隐藏拥挤度标记
 */
const hideCongestionMarkers = () => {
  congestionMarkers.forEach(marker => {
    marker.setMap(null)
  })
  congestionMarkers = []
}

/**
 * 创建拥挤度标记内容
 */
const createCongestionMarkerContent = (region) => {
  const colorClass = getCongestionClass(region.level)
  return `
    <div class="congestion-marker ${colorClass}">
      <span class="marker-text">${region.level}</span>
    </div>
  `
}

/**
 * 显示区域信息
 */
const showRegionInfo = (region) => {
  const content = `
    <div class="region-info">
      <h4>区域拥挤度</h4>
      <p><strong>级别:</strong> <span class="${getCongestionClass(region.level)}">${region.level}</span></p>
      <p><strong>占比:</strong> ${region.value}%</p>
      <p><strong>位置:</strong> ${region.lng.toFixed(4)}, ${region.lat.toFixed(4)}</p>
    </div>
  `
  
  infoWindow.setContent(content)
  infoWindow.open(map, [region.lng, region.lat])
}

/**
 * 显示位置信息
 */
const showLocationInfo = (lng, lat) => {
  const content = `
    <div class="location-info">
      <h4>位置信息</h4>
      <p><strong>经度:</strong> ${lng.toFixed(6)}</p>
      <p><strong>纬度:</strong> ${lat.toFixed(6)}</p>
      <p><small>点击地图其他位置查看更多信息</small></p>
    </div>
  `
  
  infoWindow.setContent(content)
  infoWindow.open(map, [lng, lat])
}

/**
 * 刷新所有数据
 */
const refreshData = async () => {
  const promises = []
  
  if (showHeatmap.value) {
    promises.push(fetchHeatmapData())
  }
  
  if (showCongestion.value) {
    promises.push(fetchCongestionData())
  }
  
  if (promises.length > 0) {
    await Promise.allSettled(promises)
    
    // 重新应用显示状态
    if (showHeatmap.value) {
      toggleHeatmap()
    }
    if (showCongestion.value) {
      showCongestionMarkers()
    }
  }
}

/**
 * 获取拥挤程度对应的CSS类
 */
const getCongestionClass = (level) => {
  switch (level) {
    case '畅通': return 'smooth'
    case '缓慢': return 'slow'
    case '拥堵': return 'busy'
    case '严重拥堵': return 'severe'
    default: return 'unknown'
  }
}

/**
 * 格式化时间
 */
const formatTime = (date) => {
  if (!date) return ''
  return date.toLocaleTimeString('zh-CN', { 
    hour: '2-digit', 
    minute: '2-digit' 
  })
}

/**
 * 清除错误信息
 */
const clearError = () => {
  error.value = null
}

/**
 * AI助手相关方法
 */

// 角色配置
const roleConfigs = {
  traffic: {
    name: '交通专家',
    greeting: '您好！我是交通专家，可以为您分析交通状况、提供路线建议和出行时间预测。请问有什么可以帮助您的吗？',
    quickQuestions: [
      '当前交通状况如何？',
      '推荐最佳出行路线',
      '预测到达时间',
      '避开拥堵路段'
    ]
  },
  tourist: {
    name: '旅游向导',
    greeting: '您好！我是旅游向导，可以为您推荐景点、规划旅游路线和提供出行建议。请问您想去哪里游玩呢？',
    quickQuestions: [
      '推荐热门景点',
      '规划一日游路线',
      '交通出行建议',
      '景点周边信息'
    ]
  },
  business: {
    name: '商务顾问',
    greeting: '您好！我是商务顾问，可以为您提供商务出行建议、会议地点推荐和交通时间规划。请问有什么商务需求吗？',
    quickQuestions: [
      '商务出行建议',
      '会议地点推荐',
      '交通时间规划',
      '商务区信息'
    ]
  },
  student: {
    name: '学生助手',
    greeting: '您好！我是学生助手，可以为您提供校园周边信息、学习地点推荐和出行建议。请问需要什么帮助吗？',
    quickQuestions: [
      '校园周边信息',
      '学习地点推荐',
      '出行安全建议',
      '学生优惠信息'
    ]
  },
  elderly: {
    name: '老年关怀',
    greeting: '您好！我是老年关怀助手，可以为您提供适合老年人的出行建议、无障碍设施信息和安全出行指导。请问需要什么帮助吗？',
    quickQuestions: [
      '无障碍设施查询',
      '安全出行建议',
      '适合老年人的路线',
      '医疗设施位置'
    ]
  }
}

/**
 * 切换AI角色
 */
const changeRole = () => {
  const config = roleConfigs[selectedRole.value]
  addMessage('assistant', `已切换到${config.name}模式。${config.greeting}`)
}

/**
 * 获取当前角色的快捷问题
 */
const getQuickQuestions = () => {
  return roleConfigs[selectedRole.value]?.quickQuestions || []
}

/**
 * 添加消息到对话
 */
const addMessage = (type, text) => {
  chatMessages.value.push({
    type,
    text,
    time: new Date()
  })
  
  // 滚动到底部
  nextTick(() => {
    if (chatContainer.value) {
      chatContainer.value.scrollTop = chatContainer.value.scrollHeight
    }
  })
}

/**
 * 发送消息
 */
const sendMessage = async () => {
  const message = userInput.value.trim()
  if (!message || aiLoading.value) return
  
  // 添加用户消息
  addMessage('user', message)
  userInput.value = ''
  
  // 显示AI正在输入
  aiLoading.value = true
  
  try {
    // 模拟AI响应
    const response = await generateAIResponse(message)
    addMessage('assistant', response)
  } catch (err) {
    addMessage('assistant', '抱歉，我现在无法回答您的问题，请稍后再试。')
    console.error('AI响应生成失败:', err)
  } finally {
    aiLoading.value = false
  }
}

/**
 * 生成AI响应
 */
const generateAIResponse = async (message) => {
  try {
    // 尝试调用后端AI API
    const response = await aiAPI.sendMessage({
      message,
      role: selectedRole.value,
      context: {
        heatmapData: heatmapData.value.length > 0,
        congestionData: congestionStats.value !== null,
        currentTime: new Date().toISOString()
      }
    })
    
    return response.response || response.message || '抱歉，我现在无法回答您的问题。'
  } catch (error) {
    console.warn('AI API调用失败，使用本地响应:', error)
    
    // 如果API调用失败，使用本地响应
    const role = roleConfigs[selectedRole.value]
    const lowerMessage = message.toLowerCase()
    
    // 根据角色和消息内容生成响应
    switch (selectedRole.value) {
      case 'traffic':
        if (lowerMessage.includes('交通') || lowerMessage.includes('拥堵')) {
          return '根据当前数据分析，主要道路通行状况良好，建议避开早晚高峰时段出行。您可以使用热力图查看实时交通状况。'
        } else if (lowerMessage.includes('路线') || lowerMessage.includes('路径')) {
          return '我可以为您推荐最优路线。请告诉我您的起点和终点，我会结合实时交通数据为您规划最佳路径。'
        } else if (lowerMessage.includes('时间') || lowerMessage.includes('到达')) {
          return '到达时间会根据当前交通状况动态调整。建议您查看实时路况，并预留一些缓冲时间。'
        }
        break
        
      case 'tourist':
        if (lowerMessage.includes('景点') || lowerMessage.includes('游玩')) {
          return '我推荐您访问故宫、天安门广场、颐和园等著名景点。建议提前查看交通状况，合理安排游览时间。'
        } else if (lowerMessage.includes('路线') || lowerMessage.includes('规划')) {
          return '我可以为您规划一日游路线。请告诉我您的兴趣偏好和可用时间，我会为您制定详细的游览计划。'
        }
        break
        
      case 'business':
        if (lowerMessage.includes('商务') || lowerMessage.includes('会议')) {
          return '商务区主要集中在CBD、金融街等区域。建议提前规划路线，避开交通高峰，确保准时到达。'
        } else if (lowerMessage.includes('推荐') || lowerMessage.includes('地点')) {
          return '我推荐您考虑国贸、三里屯、望京等商务区，这些区域交通便利，配套设施完善。'
        }
        break
        
      case 'student':
        if (lowerMessage.includes('校园') || lowerMessage.includes('学习')) {
          return '校园周边通常有图书馆、咖啡厅等学习场所。建议选择交通便利、环境安静的地点进行学习。'
        } else if (lowerMessage.includes('安全') || lowerMessage.includes('出行')) {
          return '学生出行要注意安全，建议结伴而行，选择安全的交通方式，避免夜间单独出行。'
        }
        break
        
      case 'elderly':
        if (lowerMessage.includes('无障碍') || lowerMessage.includes('设施')) {
          return '主要公共场所都设有无障碍设施，包括轮椅通道、电梯等。建议提前了解目的地设施情况。'
        } else if (lowerMessage.includes('安全') || lowerMessage.includes('出行')) {
          return '老年人出行建议选择平坦道路，避开拥挤路段，必要时可以寻求他人帮助。建议携带紧急联系信息。'
        }
        break
    }
    
    // 默认响应
    return `作为${role.name}，我会根据您的需求提供专业建议。请详细描述您的问题，我会尽力帮助您。`
  }
}

/**
 * 快捷问题
 */
const askQuickQuestion = (question) => {
  userInput.value = question
  sendMessage()
}

/**
 * 格式化消息时间
 */
const formatMessageTime = (date) => {
  return date.toLocaleTimeString('zh-CN', { 
    hour: '2-digit', 
    minute: '2-digit' 
  })
}

/**
 * 组件卸载时清理资源
 */
onUnmounted(() => {
  if (map) {
    hideCongestionMarkers()
    map.destroy()
  }
})
</script>

<style scoped>
.map-query-view {
  position: relative;
  width: 100%;
  height: 100%;
}

.map-container {
  width: 100%;
  height: 100%;
}

.layer-control {
  position: absolute;
  top: 20px;
  right: 20px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.15);
  z-index: 10;
  min-width: 200px;
}

.layer-control h4 {
  margin: 0 0 15px 0;
  color: #333;
  font-size: 1.1rem;
  border-bottom: 2px solid #007bff;
  padding-bottom: 5px;
}

.control-item {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  gap: 8px;
}

.control-item input[type="checkbox"] {
  width: 16px;
  height: 16px;
}

.control-item label {
  color: #333;
  font-size: 0.9rem;
  cursor: pointer;
  user-select: none;
}

.loading-indicator {
  color: #007bff;
  font-size: 0.8rem;
  font-style: italic;
}

.refresh-btn {
  width: 100%;
  padding: 8px 12px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: background-color 0.3s;
}

.refresh-btn:hover:not(:disabled) {
  background-color: #0056b3;
}

.refresh-btn:disabled {
  background-color: #6c757d;
  cursor: not-allowed;
}

.update-time {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #eee;
  text-align: center;
}

.update-time small {
  color: #666;
  font-size: 0.8rem;
}

.congestion-stats {
  position: absolute;
  bottom: 20px;
  left: 20px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.15);
  z-index: 10;
  min-width: 250px;
}

/* AI助手样式 */
.ai-assistant {
  position: absolute;
  top: 20px;
  left: 20px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.15);
  z-index: 10;
  width: 350px;
  max-height: 600px;
  display: flex;
  flex-direction: column;
}

.ai-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 2px solid #007bff;
}

.ai-header h4 {
  margin: 0;
  color: #333;
  font-size: 1.1rem;
}

.role-select {
  padding: 5px 10px;
  border: 1px solid #ddd;
  border-radius: 5px;
  background: white;
  font-size: 0.9rem;
  cursor: pointer;
}

.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 300px;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
  background: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 10px;
  max-height: 250px;
}

.message {
  display: flex;
  margin-bottom: 15px;
  gap: 10px;
}

.message.user {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  background: #e9ecef;
  flex-shrink: 0;
}

.message.user .message-avatar {
  background: #007bff;
  color: white;
}

.message-content {
  flex: 1;
  max-width: 70%;
}

.message.user .message-content {
  text-align: right;
}

.message-text {
  background: white;
  padding: 8px 12px;
  border-radius: 15px;
  font-size: 0.9rem;
  line-height: 1.4;
  word-wrap: break-word;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.message.user .message-text {
  background: #007bff;
  color: white;
}

.message-time {
  font-size: 0.7rem;
  color: #666;
  margin-top: 4px;
}

.chat-input {
  display: flex;
  gap: 8px;
  margin-bottom: 15px;
}

.message-input {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 20px;
  font-size: 0.9rem;
  outline: none;
}

.message-input:focus {
  border-color: #007bff;
}

.send-btn {
  padding: 8px 16px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: background-color 0.3s;
}

.send-btn:hover:not(:disabled) {
  background: #0056b3;
}

.send-btn:disabled {
  background: #6c757d;
  cursor: not-allowed;
}

.quick-questions {
  border-top: 1px solid #eee;
  padding-top: 15px;
}

.quick-questions h5 {
  margin: 0 0 10px 0;
  color: #333;
  font-size: 0.9rem;
}

.question-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.quick-btn {
  padding: 4px 8px;
  background: #f8f9fa;
  border: 1px solid #dee2e6;
  border-radius: 12px;
  font-size: 0.8rem;
  cursor: pointer;
  transition: all 0.3s;
  white-space: nowrap;
}

.quick-btn:hover:not(:disabled) {
  background: #e9ecef;
  border-color: #adb5bd;
}

.quick-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.congestion-stats h4 {
  margin: 0 0 15px 0;
  color: #333;
  font-size: 1.1rem;
  border-bottom: 2px solid #28a745;
  padding-bottom: 5px;
}

.stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-bottom: 15px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  border-radius: 6px;
  font-size: 0.9rem;
  font-weight: 500;
}

.stat-item .level {
  font-weight: 600;
}

.stat-item .value {
  font-weight: 700;
}

.update-info {
  text-align: center;
  padding-top: 10px;
  border-top: 1px solid #eee;
}

.update-info small {
  color: #666;
  font-size: 0.8rem;
}

.error-toast {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: #f8d7da;
  color: #721c24;
  padding: 15px 20px;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.15);
  z-index: 1000;
  max-width: 300px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.close-btn {
  background: none;
  border: none;
  color: #721c24;
  font-size: 1.5rem;
  cursor: pointer;
  padding: 0;
  line-height: 1;
}

/* 拥挤程度颜色 */
.smooth {
  background-color: rgba(76, 175, 80, 0.2);
  color: #2e7d32;
}

.slow {
  background-color: rgba(255, 193, 7, 0.2);
  color: #f57c00;
}

.busy {
  background-color: rgba(255, 87, 34, 0.2);
  color: #d84315;
}

.severe {
  background-color: rgba(244, 67, 54, 0.2);
  color: #c62828;
}

.unknown {
  background-color: rgba(158, 158, 158, 0.2);
  color: #616161;
}

/* 全局样式 - 地图标记 */
:global(.congestion-marker) {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-weight: bold;
  font-size: 0.7rem;
  border: 2px solid white;
  box-shadow: 0 2px 8px rgba(0,0,0,0.3);
  transition: transform 0.2s;
}

:global(.congestion-marker:hover) {
  transform: scale(1.1);
}

:global(.congestion-marker.smooth) {
  background-color: #4caf50;
  color: white;
}

:global(.congestion-marker.slow) {
  background-color: #ffc107;
  color: #333;
}

:global(.congestion-marker.busy) {
  background-color: #ff5722;
  color: white;
}

:global(.congestion-marker.severe) {
  background-color: #f44336;
  color: white;
}

/* 信息窗体样式 */
:global(.region-info),
:global(.location-info) {
  background: #fff;
  padding: 15px;
  border-radius: 8px;
  max-width: 250px;
}

:global(.region-info h4),
:global(.location-info h4) {
  margin: 0 0 10px 0;
  color: #333;
  font-size: 1rem;
}

:global(.region-info p),
:global(.location-info p) {
  margin: 5px 0;
  font-size: 0.9rem;
  color: #666;
}

:global(.region-info .smooth) { color: #4caf50; font-weight: bold; }
:global(.region-info .slow) { color: #ffc107; font-weight: bold; }
:global(.region-info .busy) { color: #ff5722; font-weight: bold; }
:global(.region-info .severe) { color: #f44336; font-weight: bold; }

@media (max-width: 768px) {
  .layer-control,
  .congestion-stats,
  .ai-assistant {
    padding: 15px;
    min-width: 180px;
  }
  
  .ai-assistant {
    width: 300px;
    max-height: 500px;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .chat-messages {
    max-height: 200px;
  }
  
  .question-buttons {
    flex-direction: column;
  }
  
  .quick-btn {
    text-align: center;
  }
}
</style>