<template>
    <div class="main-view">
      <div class="left-menu">
        <div class="menu-header">
          <h3>交通系统</h3>
        </div>
        <div class="menu-items">
          <router-link to="/main/home" class="menu-item">首页</router-link> 
          <router-link to="/main/navigation" class="menu-item">导航</router-link>
          <router-link to="/main/map-query" class="menu-item">地图查询</router-link>
          <router-link to="/main/user-management" class="menu-item">用户管理</router-link>
        </div>
        <div class="menu-footer">
          <button @click="logout" class="logout-button">退出登录</button>
        </div>
      </div>
      <div class="content-area">
        <router-view></router-view>
      </div>
    </div>
  </template>
  
  <script setup>
  import { useRouter } from 'vue-router';
  
  const router = useRouter();
  
  const logout = () => {
    // 这里可以添加清理 token 等退出登录的逻辑
    console.log('用户退出登录');
    router.push('/');
  };
  </script>
  
  <style scoped>
  .main-view {
    display: flex;
    width: 100vw;
    height: 100vh;
    background-image: url('@/assets/background.webp'); /* 修正了背景图路径 */
    background-size: cover;
    background-position: center;
  }
  
  .left-menu {
    width: 220px;
    background-color: rgba(0, 0, 0, 0.4);
    backdrop-filter: blur(10px);
    display: flex;
    flex-direction: column;
    padding: 20px;
    color: white;
    border-right: 1px solid rgba(255, 255, 255, 0.2);
  }
  
  .menu-header h3 {
    text-align: center;
    font-size: 24px;
    margin-bottom: 40px;
  }
  
  .menu-items {
    flex-grow: 1;
  }
  
  .menu-item {
    display: block;
    padding: 15px 20px;
    color: white;
    text-decoration: none;
    border-radius: 8px;
    margin-bottom: 10px;
    transition: background-color 0.3s;
  }
  
  .menu-item:hover,
  .router-link-active {
    background-color: rgba(255, 255, 255, 0.2);
  }
  
  .menu-footer {
    margin-top: auto;
  }
  
  .logout-button {
    width: 100%;
    padding: 15px;
    background-color: rgba(255, 82, 82, 0.7);
    border: none;
    color: white;
    border-radius: 8px;
    cursor: pointer;
    transition: background-color 0.3s;
  }
  
  .logout-button:hover {
    background-color: rgba(255, 82, 82, 1);
  }
  
  .content-area {
    flex-grow: 1;
    padding: 0; /* 内容区域不留白 */
    overflow: auto; /* 如果内容溢出则显示滚动条 */
  }
  </style>