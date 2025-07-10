<template>
    <div class="navigation-view">
      <div class="left-panel">
        <div class="input-section">
          <h3>路线规划</h3>
          <input type="text" v-model="startPoint" placeholder="输入起点">
          <input type="text" v-model="endPoint" placeholder="输入终点">
          <button @click="queryRoutes">查询路线</button>
        </div>
        <div class="routes-section">
          <h4>推荐路线</h4>
          <ul v-if="routes.length > 0">
            <li v-for="(route, index) in routes" :key="index" @click="drawRoute(route)">
              <p><strong>{{ route.name }}</strong></p>
              <p>距离: {{ route.distance }} | 预计时间: {{ route.duration }}</p>
            </li>
          </ul>
          <p v-else>请输入起终点查询路线。</p>
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
  
  // 模拟的路线数据
  const mockRoutes = [
    {
      name: '最短路径',
      distance: '15公里',
      duration: '30分钟',
      path: [
        [116.397428, 39.90923],
        [116.405285, 39.90923],
        [116.405285, 39.91923]
      ]
    },
    {
      name: '人流量最少路径',
      distance: '18公里',
      duration: '45分钟',
      path: [
          [116.397428, 39.90923], 
          [116.387428, 39.90923],
          [116.387428, 39.89923]
      ]
    }
  ];
  
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
      routes.value = mockRoutes;
    } else {
      alert('请输入起点和终点');
    }
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
    height: 100%;
    padding: 20px;
    box-sizing: border-box;
    display: flex;
    flex-direction: column;
    background-color: rgba(255, 255, 255, 0.8); /* 半透明背景，可以看到主背景图 */
    border-right: 1px solid #ccc;
  }
  
  .input-section {
    margin-bottom: 20px;
  }
  
  .input-section h3 {
    margin-top: 0;
  }
  
  .input-section input {
    width: 100%;
    padding: 8px;
    margin-bottom: 10px;
    box-sizing: border-box;
  }
  
  .input-section button {
    width: 100%;
    padding: 10px;
    background-color: #409EFF;
    color: white;
    border: none;
    cursor: pointer;
  }
  
  .routes-section {
    flex-grow: 1;
    overflow-y: auto;
  }
  
  .routes-section ul {
    list-style: none;
    padding: 0;
    margin: 0;
  }
  
  .routes-section li {
    padding: 10px;
    border-bottom: 1px solid #eee;
    cursor: pointer;
  }
  
  .routes-section li:hover {
    background-color: #f0f0f0;
  }
  
  .map-container {
    width: 75%;
    height: 100%;
  }
  </style>