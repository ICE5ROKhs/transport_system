<template>
    <div class="navigation-view">
      <div class="left-panel">
        <div class="input-section">
          <h3>路线规划</h3>
          <input type="text" v-model="startPoint" placeholder="输入起点">
          <input type="text" v-model="endPoint" placeholder="输入终点">
          <button @click="queryRoutes">查询路线</button>
        </div>
        <div class="results-section">
          <!-- 根据是否已查询显示不同内容 -->
          <div v-if="!hasSearched">
            <h4>历史记录</h4>
            <ul v-if="history.length > 0" class="history-list">
              <li v-for="(item, index) in history" :key="index" @click="applyHistory(item)">
                <p><strong>起点:</strong> {{ item.start }}</p>
                <p><strong>终点:</strong> {{ item.end }}</p>
              </li>
            </ul>
            <p v-else>暂无历史记录。</p>
          </div>
          <div v-else>
            <h4>推荐路线</h4>
            <ul v-if="routes.length > 0" class="routes-list">
              <li v-for="(route, index) in routes" :key="index" @click="drawRoute(route)">
                <p><strong>{{ route.name }}</strong></p>
                <p>距离: {{ route.distance }} | 预计时间: {{ route.duration }}</p>
              </li>
            </ul>
            <p v-else>未能查询到相关路线。</p>
          </div>
        </div>
      </div>
      <div id="navigation-container" class="map-container"></div>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted, onUnmounted } from 'vue';
  import AMapLoader from '@amap/amap-jsapi-loader';
  
  let map = null;
  let currentPolyline = null;
  
  const startPoint = ref('');
  const endPoint = ref('');
  const routes = ref([]);
  const hasSearched = ref(false); // 标记是否已执行查询
  
  // 模拟的历史记录数据
  const history = ref([
    { start: '北京西站', end: '故宫博物院' },
    { start: '首都国际机场', end: '三里屯' },
    { start: '颐和园', end: '清华大学' },
  ]);
  
  // 配置安全密钥
  window._AMapSecurityConfig = {
    securityJsCode: 'aceef7681b1b9bcfe0b886af40c120f1',
  }
  
  onMounted(() => {
    AMapLoader.load({
      key: "2bec7f50935812fcf29c98e01c419f02", // 您的 Key
      version: "2.0",
      plugins: ['AMap.Polyline'], // 加载 Polyline 插件
    }).then((AMap) => {
      map = new AMap.Map("navigation-container", {
        viewMode: "3D",
        zoom: 11, // 调整缩放级别以适应北京
        center: [116.397428, 39.90923], // 设置中心点为北京
      });
    }).catch(e => {
      console.error("地图加载失败：", e);
    });
  });
  
  const queryRoutes = () => {
    // 模拟后端交互
    if (startPoint.value && endPoint.value) {
      // 模拟的路线数据
      const mockRoutes = [
        {
          name: '最短路径',
          distance: '15公里',
          duration: '30分钟',
          path: [[116.397428, 39.90923], [116.405285, 39.90923], [116.405285, 39.91923]]
        },
        {
          name: '人流量最少路径',
          distance: '18公里',
          duration: '45分钟',
          path: [[116.397428, 39.90923], [116.387428, 39.90923], [116.387428, 39.89923]]
        }
      ];
      routes.value = mockRoutes;
      hasSearched.value = true; // 标记已查询
      // 默认绘制第一条推荐路线
      if(routes.value.length > 0) {
        drawRoute(routes.value[0]);
      }
    } else {
      alert('请输入起点和终点');
    }
  };

  const applyHistory = (item) => {
    startPoint.value = item.start;
    endPoint.value = item.end;
    // 点击历史记录后立即查询并显示路线
    queryRoutes();
  };
  
  const drawRoute = (route) => {
    if (currentPolyline) {
      map.remove(currentPolyline); // 移除上一条路线
    }
  
    currentPolyline = new window.AMap.Polyline({
      path: route.path,
      strokeColor: "#3366FF", 
      strokeOpacity: 1,
      strokeWeight: 6,
      strokeStyle: "solid",
    });
  
    map.add(currentPolyline);
    map.setFitView(); // 自动调整视野以包含路线
  };
  
  onUnmounted(() => {
    map?.destroy();
  });
  </script>
  
  <style scoped>
  .navigation-view {
    display: flex;
    width: 100%;
    height: 100%;
  }
  
  .left-panel {
    width: 25%;
    min-width: 300px;
    height: 100%;
    padding: 20px;
    box-sizing: border-box;
    display: flex;
    flex-direction: column;
    background-color: rgba(255, 255, 255, 0.9);
    border-right: 1px solid #ddd;
  }
  
  .input-section {
    margin-bottom: 20px;
  }
  
  .input-section h3 {
    margin-top: 0;
    color: #333;
  }
  
  .input-section input {
    width: 100%;
    padding: 10px;
    margin-bottom: 10px;
    box-sizing: border-box;
    border: 1px solid #ccc;
    border-radius: 4px;
  }
  
  .input-section button {
    width: 100%;
    padding: 12px;
    background-color: #409EFF;
    color: white;
    border: none;
    cursor: pointer;
    border-radius: 4px;
    font-size: 16px;
  }
  
  .results-section {
    flex-grow: 1;
    overflow-y: auto;
  }

  .results-section h4 {
    color: #555;
    border-bottom: 2px solid #eee;
    padding-bottom: 10px;
    margin-top: 0;
  }
  
  .results-section ul {
    list-style: none;
    padding: 0;
    margin: 0;
  }
  
  .results-section li {
    padding: 15px;
    border-bottom: 1px solid #f0f0f0;
    cursor: pointer;
    transition: background-color 0.2s;
  }
  
  .results-section li:hover {
    background-color: #f5f5f5;
  }

  .history-list li p {
    margin: 5px 0;
    color: #666;
  }

  .routes-list li p {
    margin: 5px 0;
    color: #333;
  }
  
  .map-container {
    width: 75%;
    height: 100%;
  }
  </style>