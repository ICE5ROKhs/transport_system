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
  let infoWindow = null;
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

  // 模拟的景点数据库
  const scenicSpots = [
    {
      id: 'spot1',
      name: '故宫博物院',
      lng: 116.39747, 
      lat: 39.91514, 
      address: '北京市东城区景山前街4号',
      phone: '010-85007421',
      image: 'https://upload.wikimedia.org/wikipedia/commons/thumb/a/a7/Hall_of_Supreme_Harmony_in_Forbidden_City.jpg/320px-Hall_of_Supreme_Harmony_in_Forbidden_City.jpg'
    },
    {
      id: 'spot2',
      name: '天安门广场',
      lng: 116.3914, 
      lat: 39.9042, 
      address: '北京市东城区西长安街',
      phone: 'N/A',
      image: 'https://upload.wikimedia.org/wikipedia/commons/thumb/c/cc/Tiananmen_Gate_at_night.jpg/320px-Tiananmen_Gate_at_night.jpg'
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
      plugins: ['AMap.HeatMap', 'AMap.InfoWindow'], // 加载热力图和信息窗体插件
    }).then((AMap) => {
      map = new AMap.Map("map-query-container", {
        viewMode: "3D",
        zoom: 12, // 稍微放大一点
        center: [116.397428, 39.90923], // 北京
      });
  
      // 初始化信息窗体
      infoWindow = new AMap.InfoWindow({ 
          offset: new AMap.Pixel(0, -30),
          closeWhenClickMap: true // 点击地图其他区域时关闭信息窗体
      });
  
      // 绑定地图点击事件
      map.on('click', async (e) => {
          console.log(`用户点击了地图，经纬度: ${e.lnglat.getLng()}, ${e.lnglat.getLat()}`);
          
          // 模拟向后端发送请求
          const spotInfo = await fetchScenicSpotInfo(e.lnglat);
  
          if (spotInfo) {
              // 如果后端返回了景点信息，则显示信息窗体
              const content = createInfoWindowContent(spotInfo);
              infoWindow.setContent(content);
              infoWindow.open(map, e.lnglat);
          } else {
              // 如果不是景点，则关闭已打开的信息窗体
              infoWindow.close();
          }
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
  
  /**
   * 模拟后端API调用，检查点击的是否是景点
   * @param {AMap.LngLat} lnglat - 点击的经纬度
   * @returns {Promise<object|null>} - 返回景点信息或null
   */
  function fetchScenicSpotInfo(lnglat) {
      return new Promise(resolve => {
          // 模拟网络延迟
          setTimeout(() => {
              const clickedLng = lnglat.getLng();
              const clickedLat = lnglat.getLat();
              const tolerance = 0.01; // 设置一个经纬度容差范围
  
              const foundSpot = scenicSpots.find(spot => 
                  Math.abs(spot.lng - clickedLng) < tolerance &&
                  Math.abs(spot.lat - clickedLat) < tolerance
              );
  
              if (foundSpot) {
                  console.log('后端返回：找到景点', foundSpot.name);
                  resolve(foundSpot);
              } else {
                  console.log('后端返回：该位置不是景点');
                  resolve(null);
              }
          }, 500); // 模拟500ms延迟
      });
  }
  
  /**
   * 创建信息窗体的内容
   * @param {object} spot - 景点信息
   * @returns {string} - HTML字符串
   */
  function createInfoWindowContent(spot) {
      return `
          <div class="custom-info-window">
              <h4>${spot.name}</h4>
              <div class="info-content">
                  <img src="${spot.image}" alt="${spot.name}" />
                  <div class="info-details">
                      <p><strong>地址:</strong> ${spot.address}</p>
                      <p><strong>电话:</strong> ${spot.phone}</p>
                  </div>
              </div>
          </div>
      `;
  }
  
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

  /* 添加信息窗体的自定义样式 */
  :global(.custom-info-window) {
      background: #fff;
      padding: 15px;
      border-radius: 8px;
      box-shadow: 0 2px 10px rgba(0,0,0,0.2);
      width: 320px;
  }
  
  :global(.custom-info-window h4) {
      margin: 0 0 10px 0;
      font-size: 16px;
      color: #333;
  }
  
  :global(.custom-info-window .info-content) {
      display: flex;
      align-items: flex-start;
  }
  
  :global(.custom-info-window img) {
      width: 120px;
      height: 80px;
      object-fit: cover;
      border-radius: 4px;
      margin-right: 15px;
  }
  
  :global(.custom-info-window .info-details) {
      font-size: 13px;
      color: #666;
  }
  
  :global(.custom-info-window .info-details p) {
      margin: 0 0 5px 0;
  }
  
  /* 覆盖高德地图默认的关闭按钮样式 */
  :global(.amap-info-close) {
      top: 10px !important;
      right: 10px !important;
      color: #999;
  }
  </style>