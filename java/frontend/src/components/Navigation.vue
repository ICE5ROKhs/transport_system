<template>
  <div class="navigation-view">
    <div class="left-panel">
      <div class="input-section">
        <h3>智能路线规划</h3>
        
        <!-- 起终点输入 -->
        <div class="location-inputs">
          <div class="input-group">
            <label>起点</label>
            <input 
              type="text" 
              v-model="startPoint" 
              placeholder="请输入起点地址或点击地图选择"
              :disabled="loading"
            >
            <small v-if="startCoords.lat">{{ formatCoords(startCoords) }}</small>
          </div>
          
          <div class="input-group">
            <label>终点</label>
            <input 
              type="text" 
              v-model="endPoint" 
              placeholder="请输入终点地址或点击地图选择"
              :disabled="loading"
            >
            <small v-if="endCoords.lat">{{ formatCoords(endCoords) }}</small>
          </div>
          
          <!-- 路线类型选择 -->
          <div class="input-group">
            <label>路线偏好</label>
            <select v-model="routeType" :disabled="loading">
              <option value="fastest">最快路线</option>
              <option value="shortest">最短路线</option>
              <option value="avoidingTraffic">避堵路线</option>
            </select>
          </div>
          
          <button 
            @click="planRoute" 
            class="plan-button" 
            :disabled="!canPlan || loading"
          >
            {{ loading ? '规划中...' : '开始规划' }}
          </button>
          
          <button 
            v-if="routes.length > 0" 
            @click="clearRoutes" 
            class="clear-button"
          >
            清除路线
          </button>
        </div>
      </div>
      
      <!-- 路线结果展示 -->
      <div class="routes-section">
        <h4>路线方案</h4>
        
        <!-- 加载状态 -->
        <div v-if="loading" class="loading-container">
          <p>正在为您规划最佳路线...</p>
        </div>
        
        <!-- 路线列表 -->
        <div v-else-if="routes.length > 0" class="route-list">
          <div 
            v-for="(route, index) in routes" 
            :key="index" 
            class="route-item"
            :class="{ active: selectedRoute === index }"
            @click="selectRoute(index)"
          >
            <div class="route-header">
              <h5>{{ getRouteTypeName(route.routeType) }}</h5>
              <span class="route-badge">方案{{ index + 1 }}</span>
            </div>
            <div class="route-details">
              <div class="detail-item">
                <span class="icon">📏</span>
                <span>{{ route.totalDistance.toFixed(2) }} 公里</span>
              </div>
              <div class="detail-item">
                <span class="icon">⏱️</span>
                <span>{{ route.totalTime.toFixed(0) }} 分钟</span>
              </div>
              <div class="detail-item">
                <span class="icon">🚗</span>
                <span>{{ route.points.length }} 个节点</span>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 空状态 -->
        <div v-else class="empty-state">
          <p>请设置起终点后开始路线规划</p>
        </div>
      </div>

      <!-- 音乐播放器 -->
      <div class="music-section">
        <MusicPlayer />
      </div>
    </div>
    
    <!-- 地图容器 -->
    <div id="navigation-container" class="map-container"></div>
    
    <!-- 地图操作提示 -->
    <div class="map-instructions">
      <p>💡 点击地图设置起点/终点：先点击设为起点，再点击设为终点</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import AMapLoader from '@amap/amap-jsapi-loader'
import { routeAPI } from '../services/api.js'
import MusicPlayer from './MusicPlayer.vue'
import { handleApiError, showSuccess, handleMapError } from '../utils/errorHandler.js'

// 地图相关
let map = null
let currentPolylines = []
let startMarker = null
let endMarker = null

// 响应式数据
const startPoint = ref('')
const endPoint = ref('')
const startCoords = ref({ lat: null, lng: null })
const endCoords = ref({ lat: null, lng: null })
const routeType = ref('fastest')
const routes = ref([])
const selectedRoute = ref(-1)
const loading = ref(false)
const isSettingStart = ref(true) // true: 设置起点, false: 设置终点

// 计算属性
const canPlan = computed(() => {
  return startCoords.value.lat && startCoords.value.lng && 
         endCoords.value.lat && endCoords.value.lng
})

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
    plugins: ['AMap.Polyline', 'AMap.Marker'],
  }).then((AMap) => {
    map = new AMap.Map("navigation-container", {
      viewMode: "3D",
      zoom: 12,
      center: [116.397428, 39.90923], // 北京
    })

    // 地图点击事件
    map.on('click', (e) => {
      const { lng, lat } = e.lnglat
      
      if (isSettingStart.value) {
        setStartPoint(lng, lat)
        isSettingStart.value = false
      } else {
        setEndPoint(lng, lat)
        isSettingStart.value = true
      }
    })

    console.log('导航地图初始化完成')
  }).catch(e => {
    console.error("地图加载失败：", e)
    handleMapError(e)
  })
})

/**
 * 设置起点
 */
const setStartPoint = (lng, lat) => {
  startCoords.value = { lng, lat }
  startPoint.value = `起点: ${lng.toFixed(4)}, ${lat.toFixed(4)}`
  
  // 清除旧的起点标记
  if (startMarker) {
    startMarker.setMap(null)
  }
  
  // 创建起点标记
  startMarker = new AMap.Marker({
    position: [lng, lat],
    content: createMarkerContent('起', '#4CAF50'),
    offset: new AMap.Pixel(-15, -15)
  })
  startMarker.setMap(map)
  
  console.log('起点已设置:', lng, lat)
}

/**
 * 设置终点
 */
const setEndPoint = (lng, lat) => {
  endCoords.value = { lng, lat }
  endPoint.value = `终点: ${lng.toFixed(4)}, ${lat.toFixed(4)}`
  
  // 清除旧的终点标记
  if (endMarker) {
    endMarker.setMap(null)
  }
  
  // 创建终点标记
  endMarker = new AMap.Marker({
    position: [lng, lat],
    content: createMarkerContent('终', '#F44336'),
    offset: new AMap.Pixel(-15, -15)
  })
  endMarker.setMap(map)
  
  console.log('终点已设置:', lng, lat)
}

/**
 * 创建标记内容
 */
const createMarkerContent = (text, color) => {
  return `
    <div style="
      width: 30px;
      height: 30px;
      background-color: ${color};
      border: 2px solid white;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-weight: bold;
      font-size: 14px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.3);
    ">${text}</div>
  `
}

/**
 * 规划路线
 */
const planRoute = async () => {
  if (!canPlan.value) return
  
  loading.value = true
  
  try {
    const routeRequest = {
      startLat: startCoords.value.lat,
      startLng: startCoords.value.lng,
      endLat: endCoords.value.lat,
      endLng: endCoords.value.lng,
      routeType: routeType.value
    }
    
    console.log('发送路线规划请求:', routeRequest)
    
    const response = await routeAPI.planRoute(routeRequest)
    
    console.log('路线规划响应:', response)
    
    // 将单个路线结果转换为数组格式，以便扩展支持多个方案
    routes.value = [response]
    selectedRoute.value = 0
    
    // 在地图上绘制路线
    drawRoute(response, 0)
    
    // 显示成功消息
    showSuccess('路线规划成功', `已为您规划${getRouteTypeName(routeType.value)}，总距离${response.totalDistance.toFixed(2)}公里`)
    
  } catch (err) {
    handleApiError(err, '路线规划')
  } finally {
    loading.value = false
  }
}

/**
 * 选择路线
 */
const selectRoute = (index) => {
  if (selectedRoute.value === index) return
  
  selectedRoute.value = index
  clearRouteLines()
  drawRoute(routes.value[index], index)
}

/**
 * 在地图上绘制路线
 */
const drawRoute = (route, index) => {
  if (!route || !route.points || !map) return
  
  // 创建路径点数组
  const path = route.points.map(point => [point.lng, point.lat])
  
  // 创建折线
  const polyline = new AMap.Polyline({
    path: path,
    strokeColor: getRouteColor(index),
    strokeWeight: 5,
    strokeOpacity: 0.8,
    strokeStyle: 'solid'
  })
  
  polyline.setMap(map)
  currentPolylines.push(polyline)
  
  // 调整地图视野以包含整个路线
  if (path.length > 0) {
    const bounds = new AMap.Bounds()
    path.forEach(point => {
      bounds.extend(point)
    })
    map.setBounds(bounds, false, [50, 50, 50, 50])
  }
}

/**
 * 获取路线颜色
 */
const getRouteColor = (index) => {
  const colors = ['#2196F3', '#4CAF50', '#FF9800', '#9C27B0']
  return colors[index % colors.length]
}

/**
 * 清除所有路线
 */
const clearRoutes = () => {
  routes.value = []
  selectedRoute.value = -1
  clearRouteLines()
  showSuccess('路线已清除', '地图上的路线标记已清除')
}

/**
 * 清除路线线条
 */
const clearRouteLines = () => {
  currentPolylines.forEach(polyline => {
    polyline.setMap(null)
  })
  currentPolylines = []
}

/**
 * 获取路线类型显示名称
 */
const getRouteTypeName = (type) => {
  switch (type) {
    case 'fastest': return '最快路线'
    case 'shortest': return '最短路线'
    case 'avoidingTraffic': return '避堵路线'
    default: return '推荐路线'
  }
}

/**
 * 格式化坐标显示
 */
const formatCoords = (coords) => {
  if (!coords.lat || !coords.lng) return ''
  return `${coords.lng.toFixed(4)}, ${coords.lat.toFixed(4)}`
}

/**
 * 清除错误信息
 */
const clearError = () => {
  error.value = null
}

/**
 * 组件卸载时清理资源
 */
onUnmounted(() => {
  if (map) {
    clearRouteLines()
    if (startMarker) startMarker.setMap(null)
    if (endMarker) endMarker.setMap(null)
    map.destroy()
  }
})
</script>

<style scoped>
.navigation-view {
  display: flex;
  width: 100%;
  height: 100%;
  background-color: #f5f5f5;
}

.left-panel {
  width: 350px;
  background-color: white;
  border-right: 1px solid #e0e0e0;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}

.input-section {
  padding: 20px;
  border-bottom: 1px solid #e0e0e0;
}

.input-section h3 {
  margin: 0 0 20px 0;
  color: #333;
  font-size: 1.3rem;
}

.location-inputs {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.input-group label {
  font-weight: 500;
  color: #555;
  font-size: 0.9rem;
}

.input-group input,
.input-group select {
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 1rem;
  transition: border-color 0.3s;
}

.input-group input:focus,
.input-group select:focus {
  outline: none;
  border-color: #2196F3;
}

.input-group input:disabled,
.input-group select:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

.input-group small {
  color: #666;
  font-size: 0.8rem;
  font-style: italic;
}

.plan-button,
.clear-button {
  padding: 12px 20px;
  border: none;
  border-radius: 6px;
  font-size: 1rem;
  cursor: pointer;
  transition: background-color 0.3s;
}

.plan-button {
  background-color: #2196F3;
  color: white;
}

.plan-button:hover:not(:disabled) {
  background-color: #1976D2;
}

.plan-button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.clear-button {
  background-color: #f44336;
  color: white;
  margin-top: 10px;
}

.clear-button:hover {
  background-color: #d32f2f;
}

.routes-section {
  flex: 1;
  padding: 20px;
}

.routes-section h4 {
  margin: 0 0 15px 0;
  color: #333;
  font-size: 1.1rem;
}

.loading-container {
  text-align: center;
  padding: 40px 20px;
  color: #666;
}

.error-container {
  text-align: center;
  padding: 20px;
}

.error-text {
  color: #f44336;
  margin-bottom: 15px;
}

.retry-button {
  background-color: #2196F3;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
}

.retry-button:hover {
  background-color: #1976D2;
}

.route-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.route-item {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 15px;
  cursor: pointer;
  transition: all 0.3s;
}

.route-item:hover {
  border-color: #2196F3;
  box-shadow: 0 2px 8px rgba(33, 150, 243, 0.1);
}

.route-item.active {
  border-color: #2196F3;
  background-color: #e3f2fd;
}

.route-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.route-header h5 {
  margin: 0;
  color: #333;
  font-size: 1rem;
}

.route-badge {
  background-color: #2196F3;
  color: white;
  font-size: 0.8rem;
  padding: 2px 8px;
  border-radius: 12px;
}

.route-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.9rem;
  color: #666;
}

.detail-item .icon {
  font-size: 1rem;
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #999;
}

.music-section {
  padding: 20px;
  border-top: 1px solid #e0e0e0;
}

.map-container {
  flex: 1;
  position: relative;
}

.map-instructions {
  position: absolute;
  bottom: 20px;
  left: 370px;
  right: 20px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  padding: 12px 20px;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  z-index: 10;
}

.map-instructions p {
  margin: 0;
  color: #666;
  font-size: 0.9rem;
  text-align: center;
}

@media (max-width: 768px) {
  .navigation-view {
    flex-direction: column;
  }
  
  .left-panel {
    width: 100%;
    max-height: 50vh;
  }
  
  .map-instructions {
    left: 20px;
    position: fixed;
    bottom: 20px;
    right: 20px;
  }
}
</style>