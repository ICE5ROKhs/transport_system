<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth.js'

const router = useRouter()
const authStore = useAuthStore()

// 页面状态
const isLogin = ref(true)
const loginBy = ref('username')
const countdown = ref(0)
const successMessage = ref('')
let timer = null

// 表单数据
const loginForm = ref({
  identifier: '', // 用于用户名或邮箱
  password: ''
})

const registerForm = ref({
  username: '',
  password: '',
  email: '',
  verificationCode: '',
  userName: '',
  phone: '',
  age: null,
  sex: ''
})

/**
 * 处理登录
 */
const handleLogin = async () => {
  try {
    authStore.clearError()
    
    const loginData = {
      [loginBy.value]: loginForm.value.identifier,
      password: loginForm.value.password
    }

    if (loginBy.value === 'username') {
      await authStore.loginByUsername(loginData)
    } else {
      await authStore.loginByEmail(loginData)
    }

    // 登录成功，跳转到主页
    router.push('/main')
  } catch (error) {
    // 错误已在store中处理
    console.error('登录失败:', error)
  }
}

/**
 * 处理注册
 */
const handleRegister = async () => {
  try {
    authStore.clearError()
    successMessage.value = ''
    
    const response = await authStore.register(registerForm.value)
    
    // 注册成功
    successMessage.value = response
    
    // 2秒后切换到登录页面
    setTimeout(() => {
      switchToLogin()
      successMessage.value = ''
    }, 2000)
    
  } catch (error) {
    // 错误已在store中处理
    console.error('注册失败:', error)
  }
}

/**
 * 获取验证码
 */
const getVerificationCode = async () => {
  if (countdown.value > 0 || !registerForm.value.email) return

  try {
    authStore.clearError()
    
    const response = await authStore.sendVerificationCode(registerForm.value.email)
    
    // 显示成功消息
    successMessage.value = response
    
    // 开始60秒倒计时
    countdown.value = 60
    timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
    
    // 3秒后清除成功消息
    setTimeout(() => {
      successMessage.value = ''
    }, 3000)
    
  } catch (error) {
    // 错误已在store中处理
    console.error('发送验证码失败:', error)
  }
}

/**
 * 切换到注册页面
 */
const switchToRegister = () => {
  isLogin.value = false
  authStore.clearError()
  successMessage.value = ''
}

/**
 * 切换到登录页面
 */
const switchToLogin = () => {
  isLogin.value = true
  authStore.clearError()
  successMessage.value = ''
}

/**
 * 检查用户是否已登录
 */
onMounted(() => {
  if (authStore.isAuthenticated) {
    router.push('/main')
  }
})

/**
 * 清理定时器
 */
onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})
</script>

<template>
  <div class="login-container">
    <div class="login-card">
      <div class="form-container">
        <!-- 登录表单 -->
        <form v-if="isLogin" @submit.prevent="handleLogin">
          <h2>用户登录</h2>
          
          <!-- 登录方式选择 -->
          <div class="login-toggle">
            <label>
              <input type="radio" v-model="loginBy" value="username"> 用户名
            </label>
            <label>
              <input type="radio" v-model="loginBy" value="email"> 邮箱
            </label>
          </div>
          
          <!-- 错误信息显示 -->
          <div v-if="authStore.error" class="error-message">
            {{ authStore.error }}
          </div>
          
          <div class="input-group">
            <input 
              type="text" 
              v-model="loginForm.identifier" 
              :placeholder="loginBy === 'username' ? '用户名' : '邮箱'" 
              required
              :disabled="authStore.loading"
            >
          </div>
          <div class="input-group">
            <input 
              type="password" 
              v-model="loginForm.password" 
              placeholder="密码" 
              required
              :disabled="authStore.loading"
            >
          </div>
          <button type="submit" class="btn" :disabled="authStore.loading">
            {{ authStore.loading ? '登录中...' : '登录' }}
          </button>
          <p class="switch-form">
            没有账户? <a href="#" @click.prevent="switchToRegister">立即注册</a>
          </p>
        </form>

        <!-- 注册表单 -->
        <form v-else @submit.prevent="handleRegister">
          <h2>用户注册</h2>
          
          <!-- 错误信息显示 -->
          <div v-if="authStore.error" class="error-message">
            {{ authStore.error }}
          </div>
          
          <!-- 成功信息显示 -->
          <div v-if="successMessage" class="success-message">
            {{ successMessage }}
          </div>
          
          <div class="input-group">
            <input 
              type="text" 
              v-model="registerForm.username" 
              placeholder="用户名 (长度3-20)" 
              required
              :disabled="authStore.loading"
            >
          </div>
          <div class="input-group">
            <input 
              type="password" 
              v-model="registerForm.password" 
              placeholder="密码 (不少于6位)" 
              required
              :disabled="authStore.loading"
            >
          </div>
          <div class="input-group">
            <input 
              type="email" 
              v-model="registerForm.email" 
              placeholder="邮箱" 
              required
              :disabled="authStore.loading"
            >
          </div>
          <div class="input-group verification-code">
            <input 
              type="text" 
              v-model="registerForm.verificationCode" 
              placeholder="验证码" 
              required
              :disabled="authStore.loading"
            >
            <button 
              type="button" 
              @click="getVerificationCode" 
              class="btn-code" 
              :disabled="countdown > 0 || authStore.loading || !registerForm.email"
            >
              {{ countdown > 0 ? `${countdown}秒后重试` : '获取验证码' }}
            </button>
          </div>
          
          <!-- 个人信息字段 -->
          <div class="input-group">
            <input 
              type="text" 
              v-model="registerForm.userName" 
              placeholder="真实姓名 (2-10个字符)" 
              required
              :disabled="authStore.loading"
            >
          </div>
          <div class="input-group">
            <input 
              type="tel" 
              v-model="registerForm.phone" 
              placeholder="手机号 (11位数字)" 
              required
              :disabled="authStore.loading"
            >
          </div>
          <div class="input-group">
            <input 
              type="number" 
              v-model="registerForm.age" 
              placeholder="年龄 (1-150)" 
              min="1" 
              max="150"
              required
              :disabled="authStore.loading"
            >
          </div>
          <div class="input-group">
            <select 
              v-model="registerForm.sex" 
              required
              :disabled="authStore.loading"
              class="form-select"
            >
              <option value="">请选择性别</option>
              <option value="男">男</option>
              <option value="女">女</option>
              <option value="其他">其他</option>
            </select>
          </div>
          
          <button type="submit" class="btn" :disabled="authStore.loading">
            {{ authStore.loading ? '注册中...' : '注册' }}
          </button>
          <p class="switch-form">
            已有账户? <a href="#" @click.prevent="switchToLogin">立即登录</a>
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
  text-align: center;
}

h2 {
  text-align: center;
  margin-bottom: 1.5rem;
  color: #333;
}

.login-toggle {
  display: flex;
  justify-content: center;
  margin-bottom: 1rem;
  gap: 1rem;
}

.login-toggle label {
  cursor: pointer;
  color: #333;
  display: flex;
  align-items: center;
  gap: 0.5rem;
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
  transition: border-color 0.3s;
}

input:focus {
  outline: none;
  border-color: #007bff;
}

input:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
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

.btn:hover:not(:disabled) {
  background-color: #0056b3;
}

.btn:disabled {
  background-color: #6c757d;
  cursor: not-allowed;
}

.switch-form {
  text-align: center;
  margin-top: 1rem;
  color: #333;
}

.switch-form a {
  color: #007bff;
  text-decoration: none;
}

.switch-form a:hover {
  text-decoration: underline;
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
  white-space: nowrap;
  transition: background-color 0.3s;
}

.form-select {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ccc;
  border-radius: 4px;
  box-sizing: border-box;
  transition: border-color 0.3s;
  background-color: white;
}

.form-select:focus {
  outline: none;
  border-color: #007bff;
}

.form-select:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

.error-message {
  background-color: #f8d7da;
  color: #721c24;
  padding: 0.75rem;
  border-radius: 4px;
  margin-bottom: 1rem;
  border: 1px solid #f5c6cb;
}

.success-message {
  background-color: #d4edda;
  color: #155724;
  padding: 0.75rem;
  border-radius: 4px;
  margin-bottom: 1rem;
  border: 1px solid #c3e6cb;
}

.btn-code:hover:not(:disabled) {
  background-color: #e9ecef;
}

.btn-code:disabled {
  background-color: #e9ecef;
  cursor: not-allowed;
  color: #6c757d;
}

.error-message {
  background-color: #f8d7da;
  color: #721c24;
  padding: 0.75rem;
  border-radius: 4px;
  margin-bottom: 1rem;
  border: 1px solid #f5c6cb;
}

.success-message {
  background-color: #d4edda;
  color: #155724;
  padding: 0.75rem;
  border-radius: 4px;
  margin-bottom: 1rem;
  border: 1px solid #c3e6cb;
}
</style>