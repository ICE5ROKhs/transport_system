<template>
    <div class="map-query-view">
      <div id="map-query-container" class="map-container"></div>
      <div class="layer-control">
        <h4>图层控制</h4>
        <div>
          <input type="checkbox" id="heatmap-toggle" v-model="showHeatmap" @change="toggleHeatmap">
          <label for="heatmap-toggle">热力图</label>
        </div>
        <div>
          <input type="checkbox" id="congestion-toggle" disabled>
          <label for="congestion-toggle">街区拥挤度 (开发中)</label>
        </div>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted, onUnmounted } from 'vue';
  import AMapLoader from '@amap/amap-jsapi-loader';
  
  let map = null;
  let heatmap = null;
  const showHeatmap = ref(false);
  
  // 模拟的热力图数据
  const heatmapData = [
      { lng: 116.39, lat: 39.9, count: 100 },
      { lng: 116.4, lat: 39.9, count: 120 },
      { lng: 116.41, lat: 39.91, count: 80 },
      { lng: 116.38, lat: 39.89, count: 90 },
      { lng: 116.42, lat: 39.92, count: 150 },
      { lng: 116.4, lat: 39.91, count: 130 },
  ];
  
  // 配置安全密钥
  window._AMapSecurityConfig = {
    securityJsCode: 'aceef7681b1b9bcfe0b886af40c120f1',
  }
  
  onMounted(() => {
    AMapLoader.load({
      key: "2bec7f50935812fcf29c98e01c419f02", // 您的 Key
      version: "2.0",
      plugins: ['AMap.HeatMap'], // 加载热力图插件
    }).then((AMap) => {
      map = new AMap.Map("map-query-container", {
        viewMode: "3D",
        zoom: 11,
        center: [116.397428, 39.90923], // 北京
      });
  
      // 初始化热力图
      heatmap = new AMap.HeatMap(map, {
          radius: 25, //给定半径
          opacity: [0, 0.8]
      });
  
    }).catch(e => {
      console.error("地图加载失败：", e);
    });
  });
  
  const toggleHeatmap = () => {
    if (showHeatmap.value) {
      heatmap.setDataSet({ data: heatmapData, max: 150 });
      heatmap.show();
    } else {
      heatmap.hide();
    }
  };
  
  onUnmounted(() => {
    map?.destroy();
  });
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
    bottom: 20px;
    right: 20px;
    background: rgba(255, 255, 255, 0.8);
    padding: 10px 20px;
    border-radius: 5px;
    box-shadow: 0 2px 6px rgba(0,0,0,.3);
    z-index: 10;
  }
  
  .layer-control h4 {
    margin: 0 0 10px 0;
  }
  
  .layer-control div {
    margin-bottom: 5px;
  }
  </style>