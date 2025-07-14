<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const isLogin = ref(true)

// 登录方式：'username' 或 'email'
const loginBy = ref('username')

const loginForm = ref({
  identifier: '', // 用于用户名或邮箱
  password: ''
})

const registerForm = ref({
  username: '',
  password: '',
  email: '',
  verificationCode: ''
})

const countdown = ref(0)
let timer = null

// 发送验证码
const getVerificationCode = async () => {
  if (countdown.value > 0) return

  if (!registerForm.value.email) {
    alert('请输入邮箱地址')
    return
  }

  try {
    const response = await fetch('/api/auth/send-code', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ email: registerForm.value.email })
    })

    const resultText = await response.text()

    if (response.ok) {
      alert(resultText)
      // 开始60秒倒计时
      countdown.value = 60
      timer = setInterval(() => {
        countdown.value--
        if (countdown.value <= 0) {
          clearInterval(timer)
        }
      }, 1000)
    } else {
      alert(`发送失败: ${resultText}`)
    }
  } catch (error) {
    console.error('发送验证码时出错:', error)
    alert('发送验证码时发生网络错误')
  }
}

// 处理登录
const handleLogin = async () => {
  const url = loginBy.value === 'username' ? '/api/auth/login_by_username' : '/api/auth/login'
  const payload = loginBy.value === 'username'
    ? { username: loginForm.value.identifier, password: loginForm.value.password }
    : { email: loginForm.value.identifier, password: loginForm.value.password }

  try {
    const response = await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(payload)
    })

    if (response.ok) {
      const data = await response.json()
      localStorage.setItem('token', data.token)
      localStorage.setItem('username', data.username)
      router.push('/main')
    } else {
      const errorText = await response.text()
      alert(`登录失败: ${errorText}`)
    }
  } catch (error) {
    console.error('登录时出错:', error)
    alert('登录时发生网络错误')
  }
}

// 处理注册
const handleRegister = async () => {
  try {
    const response = await fetch('/api/auth/register', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(registerForm.value)
    })

    const resultText = await response.text()

    if (response.ok) {
      alert(resultText)
      isLogin.value = true // 注册成功后切换到登录页
    } else {
      alert(`注册失败: ${resultText}`)
    }
  } catch (error) {
    console.error('注册时出错:', error)
    alert('注册时发生网络错误')
  }
}

</script>

<template>
  <div class="login-container">
    <div class="login-card">
      <div class="form-container">
        <!-- 登录表单 -->
        <form v-if="isLogin" @submit.prevent="handleLogin">
          <h2>用户登录</h2>
          <div class="login-toggle">
            <label>
              <input type="radio" v-model="loginBy" value="username"> 用户名
            </label>
            <label>
              <input type="radio" v-model="loginBy" value="email"> 邮箱
            </label>
          </div>
          <div class="input-group">
            <input type="text" v-model="loginForm.identifier" :placeholder="loginBy === 'username' ? '用户名' : '邮箱'" required>
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
            <input type="text" v-model="registerForm.username" placeholder="用户名 (长度3-20)" required>
          </div>
          <div class="input-group">
            <input type="password" v-model="registerForm.password" placeholder="密码 (不少于6位)" required>
          </div>
          <div class="input-group">
            <input type="email" v-model="registerForm.email" placeholder="邮箱" required>
          </div>
          <div class="input-group verification-code">
            <input type="text" v-model="registerForm.verificationCode" placeholder="验证码" required>
            <button type="button" @click="getVerificationCode" class="btn-code" :disabled="countdown > 0">
              {{ countdown > 0 ? `${countdown}秒后重试` : '获取验证码' }}
            </button>
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
  white-space: nowrap;
}

.btn-code:disabled {
  background-color: #e9ecef;
  cursor: not-allowed;
}
</style>