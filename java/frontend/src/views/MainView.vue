<template>
  <div class="main-view">
    <div class="left-menu">
      <div class="menu-header">
        <h3>交通系统</h3>
        <div class="user-info" v-if="authStore.user">
          <span class="welcome-text">欢迎, {{ authStore.currentUsername }}</span>
        </div>
      </div>
      <div class="menu-items">
        <router-link to="/main/home" class="menu-item">首页</router-link> 
        <router-link to="/main/navigation" class="menu-item">导航</router-link>
        <router-link to="/main/map-query" class="menu-item">地图查询</router-link>
        <router-link to="/main/user-management" class="menu-item">用户管理</router-link>
      </div>
      <div class="menu-footer">
        <button @click="handleLogout" class="logout-button" :disabled="authStore.loading">
          {{ authStore.loading ? '登出中...' : '退出登录' }}
        </button>
      </div>
    </div>
    <div class="content-area">
      <router-view></router-view>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth.js'

const router = useRouter()
const authStore = useAuthStore()

/**
 * 处理登出
 */
const handleLogout = async () => {
  try {
    await authStore.logout()
    // 登出成功，跳转到登录页
    router.push('/')
  } catch (error) {
    console.error('登出失败:', error)
    // 即使登出API失败，也要清除本地状态并跳转
    authStore.clearAuthData()
    router.push('/')
  }
}

/**
 * 组件挂载时获取用户信息
 */
onMounted(async () => {
  if (authStore.isAuthenticated && !authStore.user?.email) {
    try {
      await authStore.fetchUserInfo()
    } catch (error) {
      console.error('获取用户信息失败:', error)
      // 如果获取用户信息失败，可能token已过期，直接登出
      if (error.message.includes('token') || error.message.includes('401')) {
        handleLogout()
      }
    }
  }
})
</script>

<style scoped>
.main-view {
  display: flex;
  width: 100vw;
  height: 100vh;
  background-image: url('@/assets/background.png'); /* 修正了背景图路径 */
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
  margin-bottom: 20px;
}

.user-info {
  text-align: center;
  margin-bottom: 20px;
}

.welcome-text {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
  background-color: rgba(255, 255, 255, 0.1);
  padding: 8px 12px;
  border-radius: 6px;
  display: inline-block;
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

.logout-button:hover:not(:disabled) {
  background-color: rgba(255, 82, 82, 1);
}

.logout-button:disabled {
  background-color: rgba(255, 82, 82, 0.5);
  cursor: not-allowed;
}

.content-area {
  flex-grow: 1;
  padding: 0; /* 内容区域不留白 */
  overflow: auto; /* 如果内容溢出则显示滚动条 */
}
</style>