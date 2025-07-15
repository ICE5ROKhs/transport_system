<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth.js'
import { showSuccess, showError, handleApiError } from '../utils/errorHandler.js'

const router = useRouter()
const authStore = useAuthStore()

// 页面状态
const isLogin = ref(true)
const loginBy = ref('username')
const countdown = ref(0)
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

    // 登录成功提示
    showSuccess('登录成功', '欢迎回来！正在跳转到主页...')
    
    // 登录成功，跳转到主页
    router.push('/main')
  } catch (error) {
    // 使用新的错误处理
    handleApiError(error, '登录')
  }
}

/**
 * 处理注册
 */
const handleRegister = async () => {
  try {
    authStore.clearError()
    
    const response = await authStore.register(registerForm.value)
    
    // 注册成功提示
    showSuccess('注册成功', response || '账户创建成功，请登录')
    
    // 2秒后切换到登录页面
    setTimeout(() => {
      switchToLogin()
    }, 2000)
    
  } catch (error) {
    // 使用新的错误处理
    handleApiError(error, '注册')
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
    showSuccess('验证码已发送', response || '验证码已发送至您的邮箱，请注意查收')
    
    // 开始60秒倒计时
    countdown.value = 60
    timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
    
  } catch (error) {
    // 使用新的错误处理
    handleApiError(error, '发送验证码')
  }
}

/**
 * 切换到注册页面
 */
const switchToRegister = () => {
  isLogin.value = false
  authStore.clearError()
}

/**
 * 切换到登录页面
 */
const switchToLogin = () => {
  isLogin.value = true
  authStore.clearError()
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
          
          <!-- 当前登录方式提示 -->
          <div class="login-method-hint">
            <span class="method-indicator">
              {{ loginBy === 'username' ? '用户名登录' : '邮箱登录' }}
            </span>
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
          
          <!-- 登录按钮和切换按钮区域 -->
          <div class="login-actions">
            <!-- 切换登录方式按钮 -->
            <button 
              type="button" 
              class="btn-switch" 
              @click="loginBy = loginBy === 'username' ? 'email' : 'username'"
              :disabled="authStore.loading"
            >
              {{ loginBy === 'username' ? '邮箱登录' : '用户名登录' }}
            </button>
            
            <!-- 登录按钮 -->
            <button type="submit" class="btn-submit" :disabled="authStore.loading">
              {{ authStore.loading ? '登录中...' : '登录' }}
            </button>
            
            <!-- 注册账号按钮 -->
            <button 
              type="button" 
              class="btn-register" 
              @click="switchToRegister"
              :disabled="authStore.loading"
            >
              注册账号
            </button>
          </div>
        </form>

        <!-- 注册表单 -->
        <form v-else @submit.prevent="handleRegister">
          <h2>用户注册</h2>
          
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
          
          <!-- 返回登录按钮 -->
          <div class="register-actions">
            <button 
              type="button" 
              class="btn-back-login" 
              @click="switchToLogin"
              :disabled="authStore.loading"
            >
              返回登录
            </button>
          </div>
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

/* 登录方式提示 */
.login-method-hint {
  text-align: center;
  margin-bottom: 1rem;
}

.method-indicator {
  display: inline-block;
  padding: 0.5rem 1rem;
  background-color: #e3f2fd;
  color: #1976d2;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 500;
  border: 1px solid #bbdefb;
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

/* 登录操作按钮区域 */
.login-actions {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.btn-switch {
  flex: 1;
  padding: 0.75rem 0.5rem;
  border: 1px solid #007bff;
  border-radius: 4px;
  background-color: transparent;
  color: #007bff;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s;
  white-space: nowrap;
}

.btn-switch:hover:not(:disabled) {
  background-color: #007bff;
  color: white;
}

.btn-switch:disabled {
  border-color: #6c757d;
  color: #6c757d;
  cursor: not-allowed;
}

.btn-submit {
  flex: 2;
  padding: 0.75rem;
  border: none;
  border-radius: 4px;
  background-color: #007bff;
  color: white;
  font-size: 1rem;
  cursor: pointer;
  transition: background-color 0.3s;
}

.btn-submit:hover:not(:disabled) {
  background-color: #0056b3;
}

.btn-submit:disabled {
  background-color: #6c757d;
  cursor: not-allowed;
}

.btn-register {
  flex: 1;
  padding: 0.75rem 0.5rem;
  border: 1px solid #28a745;
  border-radius: 4px;
  background-color: transparent;
  color: #28a745;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s;
  white-space: nowrap;
}

.btn-register:hover:not(:disabled) {
  background-color: #28a745;
  color: white;
}

.btn-register:disabled {
  border-color: #6c757d;
  color: #6c757d;
  cursor: not-allowed;
}

/* 注册页面操作按钮 */
.register-actions {
  display: flex;
  justify-content: center;
  margin-top: 1rem;
}

.btn-back-login {
  padding: 0.75rem 1.5rem;
  border: 1px solid #6c757d;
  border-radius: 4px;
  background-color: transparent;
  color: #6c757d;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-back-login:hover:not(:disabled) {
  background-color: #6c757d;
  color: white;
}

.btn-back-login:disabled {
  opacity: 0.6;
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