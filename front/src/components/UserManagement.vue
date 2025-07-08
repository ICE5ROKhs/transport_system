<template>
  <div class="user-management-container">
    <h2>个人信息</h2>
    <div v-if="loading" class="loading-tip">正在加载用户信息...</div>
    <div v-else class="user-info-panel">
      <div class="info-item">
        <span class="label">用户名:</span>
        <span class="value">{{ user.username }}</span>
      </div>
      <div class="info-item">
        <span class="label">邮箱:</span>
        <span class="value">{{ user.email }}</span>
      </div>
      <div class="info-item">
        <span class="label">用户角色:</span>
        <span class="value">{{ user.role }}</span>
      </div>
      <div class="info-item">
        <span class="label">注册日期:</span>
        <span class="value">{{ user.joinDate }}</span>
      </div>
      <button @click="logout" class="logout-button">退出登录</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';

const loading = ref(true);
const user = ref({});

// 模拟从后端获取用户信息
const fetchUserInfo = () => {
  // 假设我们从 localStorage 获取了 token，然后去请求后端
  // const token = localStorage.getItem('user-token');
  // if (!token) { /* 处理未登录情况 */ return; }

  // 模拟API调用
  setTimeout(() => {
    user.value = {
      username: 'Admin',
      email: 'admin@example.com',
      role: '系统管理员',
      joinDate: '2023-10-01',
    };
    loading.value = false;
  }, 1000); // 模拟1秒的网络延迟
};

const logout = () => {
  // 模拟退出登录
  // 1. 清除本地存储的 token
  // localStorage.removeItem('user-token');
  // 2. 跳转到登录页
  // router.push('/login');
  alert('已成功退出登录！');
};

onMounted(() => {
  fetchUserInfo();
});
</script>

<style scoped>
.user-management-container {
  padding: 2rem;
  color: #333; /* 将文字颜色改为深色以提高可读性 */
  background-color: rgba(255, 255, 255, 0.9); /* 提供一个浅色背景 */
  border-radius: 8px;
  max-width: 600px;
  margin: 2rem auto;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

h2 {
  text-align: center;
  margin-bottom: 2rem;
  color: #1a1a1a;
}

.loading-tip {
  text-align: center;
  color: #666;
}

.user-info-panel {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: 0.8rem;
  background-color: #f9f9f9;
  border-radius: 4px;
  border-left: 4px solid #409EFF;
}

.label {
  font-weight: bold;
  color: #555;
}

.value {
  color: #333;
}

.logout-button {
  margin-top: 1.5rem;
  padding: 0.8rem;
  width: 100%;
  background-color: #F56C6C;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1rem;
  transition: background-color 0.3s ease;
}

.logout-button:hover {
  background-color: #d9534f;
}
</style>