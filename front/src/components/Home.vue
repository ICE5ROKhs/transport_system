<template>
    <div class="home-container">
      <div class="welcome-banner">
        <h1>欢迎使用智能交通系统！</h1>
        <p>在这里，您可以一览全局，轻松管理交通信息。</p>
      </div>
  
      <div class="metrics-grid">
        <div class="metric-card">
          <h2>实时交通指数</h2>
          <p class="metric-value">8.2 <span class="metric-unit">/ 10</span></p>
          <p class="metric-desc">当前城市交通状况良好</p>
        </div>
        <div class="metric-card">
          <h2>拥堵路段</h2>
          <p class="metric-value">12 <span class="metric-unit">条</span></p>
          <p class="metric-desc">主要集中在市中心区域</p>
        </div>
        <div class="metric-card">
          <h2>系统健康度</h2>
          <p class="metric-value" style="color: #67c23a;">良好</p>
          <p class="metric-desc">所有服务均正常运行</p>
        </div>
      </div>
  
      <!-- “快速操作”部分已被移除 -->
  
    </div>
  </template>
  
  <script setup>
  // 不再需要 useRouter，因为没有内部跳转了
  </script>
  
  <style scoped>
  .home-container {
    padding: 40px;
    color: #fff;
    height: 100%;
    overflow-y: auto;
    background-color: transparent; /* 确保背景透明 */
  }
  
  .welcome-banner {
    text-align: center;
    margin-bottom: 40px;
  }
  
  .welcome-banner h1 {
    font-size: 2.5rem;
    font-weight: 600;
    margin-bottom: 10px;
  }
  
  .welcome-banner p {
    font-size: 1.2rem;
    color: #ccc;
  }
  
  .metrics-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: 30px;
    margin-bottom: 40px;
  }
  
  .metric-card {
    background-color: rgba(255, 255, 255, 0.1);
    border-radius: 10px;
    padding: 25px;
    text-align: center;
    backdrop-filter: blur(10px);
    border: 1px solid rgba(255, 255, 255, 0.2);
  }
  
  .metric-card h2 {
    font-size: 1.2rem;
    margin-bottom: 15px;
    color: #eee;
  }
  
  .metric-value {
    font-size: 2.8rem;
    font-weight: bold;
    margin-bottom: 5px;
  }
  
  .metric-unit {
    font-size: 1.2rem;
    margin-left: 5px;
    color: #ccc;
  }
  
  .metric-desc {
    font-size: 0.9rem;
    color: #bbb;
  }
  </style>