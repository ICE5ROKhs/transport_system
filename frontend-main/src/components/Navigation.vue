<template>
  <div class="navigation-view">
    <!-- 地图容器 -->
    <div id="navigation-container" class="map-container"></div>
    
    <!-- 右侧操作面板（原left-panel内容） -->
    <div class="right-panel card">
      <div class="input-section">
        <h3>智能路线规划</h3>
        <!-- 起终点输入等内容保持不变 -->
        <div class="location-inputs">
          <div class="input-group">
            <label>起点</label>
            <input 
              type="text" 
              v-model="startPoint" 
              placeholder="请输入起点地址或点击地图选择"
              :disabled="loading"
              class="input"
            >
            <small v-if="startCoords.lat" class="coord-display">{{ formatCoords(startCoords) }}</small>
          </div>
          <div class="input-group">
            <label>终点</label>
            <input 
              type="text" 
              v-model="endPoint" 
              placeholder="请输入终点地址或点击地图选择"
              :disabled="loading"
              class="input"
            >
            <small v-if="endCoords.lat" class="coord-display">{{ formatCoords(endCoords) }}</small>
          </div>
          <div class="input-group">
            <label>路线偏好</label>
            <select v-model="routeType" :disabled="loading" class="input">
              <option value="fastest">最快路线</option>
              <option value="shortest">最短路线</option>
              <option value="avoidingTraffic">避堵路线</option>
            </select>
          </div>
          <div class="button-group">
            <button 
              @click="planRoute" 
              class="plan-button btn" 
              :disabled="!canPlan || loading"
            >
              {{ loading ? '规划中...' : '开始规划' }}
            </button>
            <button 
              v-if="routes.length > 0" 
              @click="clearRoutes" 
              class="clear-button btn"
            >
              清除路线
            </button>
          </div>
        </div>
      </div>
      <div class="routes-section">
        <h4>路线方案</h4>
        <div v-if="loading" class="loading-container">
          <p>正在为您规划最佳路线...</p>
        </div>
        <div v-else-if="routes.length > 0" class="route-list">
          <div 
            v-for="(route, index) in routes" 
            :key="index" 
            class="route-item card"
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
        <div v-else class="empty-state">
          <p>请设置起终点后开始路线规划</p>
        </div>
      </div>
    </div>
    <!-- 地图操作提示 -->
    <div class="map-instructions card">
      <p>💡 点击地图设置起点/终点：先点击设为起点，再点击设为终点</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import AMapLoader from '@amap/amap-jsapi-loader'
import { routeAPI } from '../services/api.js'
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
    plugins: ['AMap.Polyline', 'AMap.Marker', 'AMap.InfoWindow'],
  }).then((AMap) => {
    map = new AMap.Map("navigation-container", {
      viewMode: "3D",
      zoom: 12,
      center: [116.397428, 39.90923], // 北京
    })

    // 初始化信息窗体
    const infoWindow = new AMap.InfoWindow({
      offset: new AMap.Pixel(0, -30),
      closeWhenClickMap: true
    })

    // 地图点击显示经纬度
    map.on('click', (e) => {
      const { lng, lat } = e.lnglat
      infoWindow.setContent(`<div style='font-size:14px;'>经度: ${lng.toFixed(6)}<br>纬度: ${lat.toFixed(6)}</div>`)
      infoWindow.open(map, [lng, lat])
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
  
  // 创建新的起点标记
  startMarker = new AMap.Marker({
    position: [lng, lat],
    icon: '//a.amap.com/jsapi_demos/static/demo-center/icons/poi-marker-red.png',
    title: '起点'
  })
  
  startMarker.setMap(map)
  showSuccess('起点设置成功', `起点坐标: ${lng.toFixed(4)}, ${lat.toFixed(4)}`)
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
  
  // 创建新的终点标记
  endMarker = new AMap.Marker({
    position: [lng, lat],
    icon: '//a.amap.com/jsapi_demos/static/demo-center/icons/poi-marker-default.png',
    title: '终点'
  })
  
  endMarker.setMap(map)
  showSuccess('终点设置成功', `终点坐标: ${lng.toFixed(4)}, ${lat.toFixed(4)}`)
}

/**
 * 规划路线
 */
const planRoute = async () => {
  if (!canPlan.value) return
  
  loading.value = true
  
  try {
    const response = await routeAPI.planRoute({
      start: {
        latitude: startCoords.value.lat,
        longitude: startCoords.value.lng
      },
      end: {
        latitude: endCoords.value.lat,
        longitude: endCoords.value.lng
      },
      routeType: routeType.value
    })
    
    routes.value = response.data
    
    if (routes.value.length > 0) {
      selectRoute(0) // 默认选择第一条路线
      showSuccess('路线规划成功', `找到 ${routes.value.length} 条可选路线`)
    } else {
      showSuccess('规划完成', '未找到合适的路线，请调整起终点位置')
    }
    
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
  selectedRoute.value = index
  const route = routes.value[index]
  
  if (route && route.points) {
    displayRoute(route)
  }
}

/**
 * 在地图上显示路线
 */
const displayRoute = (route) => {
  // 清除之前的路线
  clearPolylines()
  
  // 创建路线折线
  const path = route.points.map(point => [point.longitude, point.latitude])
  
  const polyline = new AMap.Polyline({
    path: path,
    borderWeight: 2,
    strokeColor: "#3366FF",
    strokeOpacity: 0.8,
    strokeWeight: 6,
    strokeStyle: "solid"
  })
  
  polyline.setMap(map)
  currentPolylines.push(polyline)
  
  // 调整地图视野以包含整条路线
  map.setFitView([polyline])
}

/**
 * 清除地图上的路线
 */
const clearPolylines = () => {
  currentPolylines.forEach(polyline => {
    polyline.setMap(null)
  })
  currentPolylines = []
}

/**
 * 清除所有路线
 */
const clearRoutes = () => {
  routes.value = []
  selectedRoute.value = -1
  clearPolylines()
  showSuccess('路线已清除', '所有路线数据已清理')
}

/**
 * 获取路线类型名称
 */
const getRouteTypeName = (type) => {
  const typeNames = {
    fastest: '最快路线',
    shortest: '最短路线',
    avoidingTraffic: '避堵路线'
  }
  return typeNames[type] || '推荐路线'
}

/**
 * 格式化坐标显示
 */
const formatCoords = (coords) => {
  if (!coords.lat || !coords.lng) return ''
  return `${coords.lng.toFixed(4)}, ${coords.lat.toFixed(4)}`
}

/**
 * 组件卸载时清理
 */
onUnmounted(() => {
  if (map) {
    map.destroy()
  }
})
</script>

<style scoped>
.navigation-view {
  background: var(--color-bg-primary);
  color: var(--color-text-primary);
  min-height: 100vh;
  display: flex;
  flex-direction: row;
  width: 100%;
  height: 100%;
  position: relative;
}

.map-container {
  flex: 1;
  position: relative;
  min-width: 0;
  min-height: 100vh;
}

.right-panel {
  width: 400px;
  background: var(--color-bg-primary);
  border-left: 1px solid var(--color-border-primary);
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  padding: 20px;
  box-shadow: -2px 0 8px rgba(0,0,0,0.04);
  z-index: 10;
}

.input-section {
  margin-bottom: 30px;
}

.input-section h3 {
  margin: 0 0 20px 0;
  color: var(--color-text-primary);
  font-size: 18px;
  font-weight: 600;
}

.location-inputs {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.input-group label {
  color: var(--color-text-secondary);
  font-weight: 500;
  font-size: 14px;
}

.coord-display {
  color: var(--color-text-muted);
  font-size: 12px;
  font-style: italic;
}

.button-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 8px;
}

.plan-button {
  background: var(--color-primary);
  color: white;
  border: 1px solid var(--color-primary);
}

.plan-button:hover:not(:disabled) {
  background: var(--color-primary-hover);
  border-color: var(--color-primary-hover);
}

.plan-button:disabled {
  background: var(--color-bg-tertiary);
  color: var(--color-text-muted);
  border-color: var(--color-border-primary);
  cursor: not-allowed;
}

.clear-button {
  background: var(--color-bg-secondary);
  color: var(--color-danger);
  border: 1px solid var(--color-danger);
}

.clear-button:hover {
  background: var(--color-danger);
  color: white;
}

.routes-section {
  flex: 1;
  padding: 20px;
  background: var(--color-bg-secondary);
  border-radius: 12px;
  border: 1px solid var(--color-border-primary);
}

.routes-section h4 {
  margin: 0 0 16px 0;
  color: var(--color-text-primary);
  font-size: 16px;
  font-weight: 600;
}

.loading-container {
  text-align: center;
  padding: 40px 20px;
  color: var(--color-text-muted);
}

.route-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.route-item {
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s;
  background: var(--color-bg-primary);
  border: 1px solid var(--color-border-primary);
}

.route-item:hover {
  border-color: var(--color-border-hover);
  background: var(--color-bg-secondary);
}

.route-item.active {
  border-color: var(--color-primary);
  background: color-mix(in srgb, var(--color-primary) 5%, var(--color-bg-primary));
}

.route-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.route-header h5 {
  margin: 0;
  color: var(--color-text-primary);
  font-size: 14px;
  font-weight: 600;
}

.route-badge {
  background: var(--color-primary);
  color: white;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
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
  color: var(--color-text-secondary);
  font-size: 13px;
}

.detail-item .icon {
  width: 16px;
  font-size: 12px;
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: var(--color-text-muted);
}

.map-instructions {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  padding: 12px 20px;
  background: var(--color-bg-overlay);
  z-index: 100;
  text-align: center;
  backdrop-filter: blur(8px);
}

.map-instructions p {
  margin: 0;
  color: var(--color-text-secondary);
  font-size: 14px;
}

/* 响应式调整 */
@media (max-width: 1024px) {
  .navigation-view {
    flex-direction: column;
  }
  
  .right-panel {
    width: 100%;
    max-height: 350px;
    border-left: none;
    border-bottom: 1px solid var(--color-border-primary);
  }
  
  .routes-section {
    max-height: 200px;
    overflow-y: auto;
  }
}

@media (max-width: 768px) {
  .right-panel {
    padding: 16px;
    max-height: 300px;
  }
  
  .input-section h3 {
    font-size: 16px;
  }
  
  .location-inputs {
    gap: 16px;
  }
  
  .map-instructions {
    left: 10px;
    right: 10px;
    transform: none;
    padding: 8px 12px;
  }
  
  .map-instructions p {
    font-size: 12px;
  }
}
</style>