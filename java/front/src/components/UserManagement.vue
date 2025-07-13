<template>
  <div class="user-management-container">
    <div class="user-card">
      <h2 class="title">用户中心</h2>
      <div v-if="loading" class="loading-text">正在加载用户信息...</div>
      <div v-if="!loading && user" class="user-info">
        <div class="info-item">
          <span class="label">用户名:</span>
          <span class="value">{{ user.username }}</span>
        </div>
        <div class="info-item">
          <span class="label">邮 箱:</span>
          <span class="value">{{ user.email }}</span>
        </div>
        <div class="info-item">
          <span class="label">角 色:</span>
          <span class="value">{{ user.role }}</span>
        </div>
        <div class="info-item">
          <span class="label">注册日期:</span>
          <span class="value">{{ user.registrationDate }}</span>
        </div>
        <button @click="editProfile" class="edit-button">修改信息</button>
      </div>
      <div v-if="!loading && !user" class="error-text">
        无法加载用户信息，请稍后重试。
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';

const user = ref(null);
const loading = ref(true);

// 模拟从API获取用户信息的函数
const fetchUserInfo = () => {
  loading.value = true;
  // 模拟网络请求延迟
  setTimeout(() => {
    // 模拟成功获取数据
    user.value = {
      username: 'AdminUser',
      email: 'admin@example.com',
      role: '管理员',
      registrationDate: '2023-10-01',
    };
    loading.value = false;
  }, 1000);
};

// 组件挂载后获取用户信息
onMounted(() => {
  fetchUserInfo();
});

// 修改信息按钮点击事件
const editProfile = () => {
  alert('即将进入用户信息修改页面！(功能开发中)');
  // 在这里可以添加路由跳转到编辑页面的逻辑
  // router.push('/user/edit');
};
</script>

<style scoped>
@import '@/assets/main.css'; /* 引入全局绿色主题样式 */

.user-management-container {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background-image: url('@/assets/background-2.png');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  padding: 20px;
  box-sizing: border-box;
}

.user-card {
  background: rgba(12, 44, 33, 0.7); /* 半透明深绿色背景 */
  padding: 40px;
  border-radius: 15px;
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.37);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.18);
  width: 100%;
  max-width: 500px;
  color: #e0e0e0; /* 浅色文字，提高可读性 */
}

.title {
  text-align: center;
  margin-bottom: 30px;
  font-size: 2rem;
  font-weight: bold;
  color: #42b983; /* 主题绿色 */
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: 20px; /* 增加项目之间的间距 */
}

.info-item {
  display: flex;
  font-size: 1.1rem;
  border-bottom: 1px solid rgba(66, 185, 131, 0.3);
  padding-bottom: 15px;
}

.label {
  font-weight: bold;
  color: #a3e6c5; /* 浅绿色标签 */
  width: 100px;
  flex-shrink: 0;
}

.value {
  color: #f0f0f0; /* 浅灰色值 */
}

.loading-text,
.error-text {
  text-align: center;
  font-size: 1.2rem;
  color: #a3e6c5;
  padding: 20px 0;
}

.edit-button {
  margin-top: 30px;
  padding: 12px 25px;
  border: 1px solid #42b983;
  background-color: transparent;
  color: #42b983;
  border-radius: 8px;
  cursor: pointer;
  font-size: 1rem;
  font-weight: bold;
  transition: background-color 0.3s, color 0.3s;
  align-self: center;
}

.edit-button:hover {
  background-color: #42b983;
  color: #1a1a1a;
}
</style>