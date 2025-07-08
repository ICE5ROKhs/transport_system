<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'; // 导入 useRouter

const router = useRouter()
// 控制当前显示的是登录表单还是注册表单
const isLogin = ref(true)

// 登录表单的数据
const loginForm = ref({
  username: '',
  password: ''
})

// 注册表单的数据
const registerForm = ref({
  name: '',
  username: '',
  password: '',
  email: '',
  phone: '',
  code: ''
})

// 处理登录逻辑
const handleLogin = () => {
    console.log('登录信息:', loginForm.value)
  // 在这里添加跳转逻辑
  router.push('/main')
}

// 处理注册逻辑
const handleRegister = () => {
  console.log('处理注册:', registerForm.value)
  // 在这里添加实际的注册请求逻辑
  alert('注册成功！(模拟)')
}

// 获取验证码的逻辑
const getVerificationCode = () => {
  console.log('获取验证码，手机号:', registerForm.value.phone)
  // 在这里添加调用后端接口发送验证码的逻辑
  alert('验证码已发送 (模拟)')
}
</script>

<template>
  <div class="login-container">
    <div class="login-card">
      <div class="form-container">
        <!-- 登录表单 -->
        <form v-if="isLogin" @submit.prevent="handleLogin">
          <h2>用户登录</h2>
          <div class="input-group">
            <input type="text" v-model="loginForm.username" placeholder="用户名" required>
          </div>
          <div class="input-group">
            <input type="password" v-model="loginForm.password" placeholder="密码" required>
          </div>
          <button type="submit" class="btn">登录</button>
          <p class="switch-form">
            没有账户? <a href="#" @click.prevent="isLogin = false">立即注册</a>
          </p>
        </form>

        <!-- 注册表单 -->
        <form v-else @submit.prevent="handleRegister">
          <h2>用户注册</h2>
          <div class="input-group">
            <input type="text" v-model="registerForm.name" placeholder="姓名" required>
          </div>
          <div class="input-group">
            <input type="text" v-model="registerForm.username" placeholder="用户名" required>
          </div>
          <div class="input-group">
            <input type="password" v-model="registerForm.password" placeholder="密码" required>
          </div>
          <div class="input-group">
            <input type="email" v-model="registerForm.email" placeholder="邮箱" required>
          </div>
          <div class="input-group">
            <input type="tel" v-model="registerForm.phone" placeholder="电话号码" required>
          </div>
          <div class="input-group verification-code">
            <input type="text" v-model="registerForm.code" placeholder="验证码" required>
            <button type="button" @click="getVerificationCode" class="btn-code">获取验证码</button>
          </div>
          <button type="submit" class="btn">注册</button>
          <p class="switch-form">
            已有账户? <a href="#" @click.prevent="isLogin = true">立即登录</a>
          </p>
        </form>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  width: 100vw;
  background-image: url('@/assets/background.png');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

.login-card {
  background-color: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  padding: 2rem;
  border-radius: 10px;
  box-shadow: 0 4px 30px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 400px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  text-align: center; /* 确保内容居中 */
}

h2 {
  text-align: center;
  margin-bottom: 1.5rem;
  color: #333;
}

.input-group {
  margin-bottom: 1rem;
}

input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ccc;
  border-radius: 4px;
  box-sizing: border-box;
}

.btn {
  width: 100%;
  padding: 0.75rem;
  border: none;
  border-radius: 4px;
  background-color: #007bff;
  color: white;
  font-size: 1rem;
  cursor: pointer;
  transition: background-color 0.3s;
}

.btn:hover {
  background-color: #0056b3;
}

.switch-form {
  text-align: center;
  margin-top: 1rem;
}

.switch-form a {
  color: #007bff;
  text-decoration: none;
}

.verification-code {
  display: flex;
}

.verification-code input {
  flex-grow: 1;
  border-top-right-radius: 0;
  border-bottom-right-radius: 0;
}

.btn-code {
  padding: 0.75rem;
  border: 1px solid #ccc;
  border-left: none;
  background-color: #f0f0f0;
  cursor: pointer;
  border-top-right-radius: 4px;
  border-bottom-right-radius: 4px;
}
</style>