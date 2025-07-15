<template>
  <div class="home-container">
    <div class="welcome-banner">
      <h1>欢迎使用智能交通系统！</h1>
      <p>在这里，您可以一览全局，轻松管理交通信息。</p>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <p class="loading-text">正在加载统计数据...</p>
    </div>

    <!-- 错误状态 -->
    <div v-else-if="error" class="error-container">
      <p class="error-text">{{ error }}</p>
      <button @click="fetchDashboardData" class="retry-button">重新加载</button>
    </div>

    <!-- 统计数据展示 -->
    <div v-else class="metrics-grid">
      <div class="metric-card">
        <h2>交通拥挤度</h2>
        <p class="metric-value" :class="getCongestionClass(dashboardData?.congestionLevel)">
          {{ dashboardData?.congestionLevel || '暂无数据' }}
        </p>
        <p class="metric-desc">当前城市整体交通状况</p>
      </div>
      <div class="metric-card">
        <h2>拥堵路段</h2>
        <p class="metric-value">
          {{ dashboardData?.congestedSections ?? '--' }} 
          <span class="metric-unit">条</span>
        </p>
        <p class="metric-desc">需要关注的拥堵路段数量</p>
      </div>
      <div class="metric-card">
        <h2>系统状态</h2>
        <p class="metric-value" style="color: #67c23a;">良好</p>
        <p class="metric-desc">所有服务均正常运行</p>
      </div>
    </div>

    <!-- 数据更新时间 -->
    <div v-if="dashboardData && lastUpdated" class="update-info">
      <p>数据更新时间: {{ formatTime(lastUpdated) }}</p>
      <button @click="fetchDashboardData" class="refresh-button" :disabled="loading">
        {{ loading ? '更新中...' : '手动刷新' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { dataAPI } from '../services/api.js'

// 响应式数据
const dashboardData = ref(null)
const loading = ref(true)
const error = ref(null)
const lastUpdated = ref(null)

// 自动刷新定时器
let refreshTimer = null

/**
 * 获取仪表盘数据
 */
const fetchDashboardData = async () => {
  loading.value = true
  error.value = null
  
  try {
    const response = await dataAPI.getDashboardStats()
    dashboardData.value = response.data
    lastUpdated.value = new Date()
    console.log('仪表盘数据获取成功:', response)
  } catch (err) {
    error.value = err.message || '获取数据失败，请稍后重试'
    console.error('获取仪表盘数据失败:', err)
  } finally {
    loading.value = false
  }
}

/**
 * 根据拥挤程度返回对应的CSS类
 */
const getCongestionClass = (level) => {
  switch (level) {
    case '畅通':
      return 'congestion-smooth'
    case '缓慢':
      return 'congestion-slow'
    case '拥堵':
      return 'congestion-busy'
    case '严重拥堵':
      return 'congestion-severe'
    default:
      return 'congestion-unknown'
  }
}

/**
 * 格式化时间显示
 */
const formatTime = (date) => {
  if (!date) return '未知'
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

/**
 * 组件挂载时获取数据并设置自动刷新
 */
onMounted(() => {
  fetchDashboardData()
  
  // 设置5分钟自动刷新
  refreshTimer = setInterval(() => {
    fetchDashboardData()
  }, 5 * 60 * 1000) // 5分钟
})

/**
 * 组件卸载时清除定时器
 */
onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})
</script>

<style scoped>
.home-container {
  padding: 40px;
  color: #fff;
  height: 100%;
  overflow-y: auto;
  background-color: transparent;
  background-image: url('@/assets/background-1.png');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

.welcome-banner {
  text-align: center;
  margin-bottom: 40px;
}

.welcome-banner h1 {
  font-size: 2.5rem;
  margin-bottom: 1rem;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.5);
}

.welcome-banner p {
  font-size: 1.2rem;
  opacity: 0.9;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.5);
}

.loading-container, .error-container {
  text-align: center;
  margin: 60px 0;
}

.loading-text {
  font-size: 1.2rem;
  color: rgba(255, 255, 255, 0.8);
}

.error-text {
  font-size: 1.1rem;
  color: #ff6b6b;
  margin-bottom: 20px;
}

.retry-button, .refresh-button {
  background-color: rgba(0, 123, 255, 0.8);
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 1rem;
  transition: background-color 0.3s;
}

.retry-button:hover, .refresh-button:hover:not(:disabled) {
  background-color: rgba(0, 123, 255, 1);
}

.refresh-button:disabled {
  background-color: rgba(0, 123, 255, 0.5);
  cursor: not-allowed;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 30px;
  margin-bottom: 40px;
}

.metric-card {
  background-color: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border-radius: 15px;
  padding: 30px;
  text-align: center;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: transform 0.3s, box-shadow 0.3s;
}

.metric-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.2);
}

.metric-card h2 {
  font-size: 1.5rem;
  margin-bottom: 20px;
  color: rgba(255, 255, 255, 0.9);
}

.metric-value {
  font-size: 2.5rem;
  font-weight: bold;
  margin-bottom: 15px;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
}

.metric-unit {
  font-size: 1.2rem;
  font-weight: normal;
  opacity: 0.8;
}

.metric-desc {
  font-size: 1rem;
  opacity: 0.8;
  line-height: 1.4;
}

/* 拥挤程度颜色样式 */
.congestion-smooth {
  color: #67c23a; /* 绿色 - 畅通 */
}

.congestion-slow {
  color: #e6a23c; /* 橙色 - 缓慢 */
}

.congestion-busy {
  color: #f56c6c; /* 红色 - 拥堵 */
}

.congestion-severe {
  color: #ff4d4f; /* 深红色 - 严重拥堵 */
}

.congestion-unknown {
  color: #909399; /* 灰色 - 未知 */
}

.update-info {
  text-align: center;
  margin-top: 30px;
  padding: 20px;
  background-color: rgba(255, 255, 255, 0.1);
  border-radius: 10px;
  backdrop-filter: blur(5px);
}

.update-info p {
  margin-bottom: 15px;
  color: rgba(255, 255, 255, 0.8);
  font-size: 0.9rem;
}

@media (max-width: 768px) {
  .metrics-grid {
    grid-template-columns: 1fr;
    gap: 20px;
  }
  
  .metric-card {
    padding: 20px;
  }
  
  .metric-value {
    font-size: 2rem;
  }
  
  .welcome-banner h1 {
    font-size: 2rem;
  }
}
</style>