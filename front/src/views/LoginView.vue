<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const isLogin = ref(true);

// --- 数据模型 ---
const loginForm = ref({
  email: '',
  password: '',
});

const registerForm = ref({
  username: '',
  password: '',
  email: '',
  verificationCode: '',
});

// --- 验证码相关状态 ---
const isCodeSent = ref(false);
const countdown = ref(60);
let timer = null;

// --- 方法 ---

// 登录处理
const handleLogin = () => {
  console.log('发起登录请求:', loginForm.value);
  // 模拟API调用
  // 实际应用中，这里会是一个 fetch 或 axios 请求
  // fetch('/api/auth/login', { method: 'POST', body: JSON.stringify(loginForm.value) })
  alert(`登录成功（模拟），Token: fake-jwt-token, 用户名: ${loginForm.value.email}`);
  router.push('/main'); // 登录成功后跳转
};

// 注册处理
const handleRegister = () => {
  console.log('发起注册请求:', registerForm.value);
  // 模拟API调用
  alert('注册成功（模拟）');
  isLogin.value = true; // 注册成功后切换到登录界面
};

// 发送验证码
const sendVerificationCode = () => {
  if (!registerForm.value.email) {
    alert('请输入邮箱地址');
    return;
  }
  console.log('向邮箱发送验证码:', registerForm.value.email);
  // 模拟API调用
  alert('验证码已发送至您的邮箱（模拟），请注意查收');

  // 启动60秒倒计时
  isCodeSent.value = true;
  countdown.value = 60;
  timer = setInterval(() => {
    if (countdown.value > 0) {
      countdown.value--;
    } else {
      clearInterval(timer);
      isCodeSent.value = false;
    }
  }, 1000);
};

// 切换表单时重置状态
const switchForm = (targetIsLogin) => {
  isLogin.value = targetIsLogin;
  // 清理可能正在运行的计时器
  if (timer) {
    clearInterval(timer);
  }
  isCodeSent.value = false;
  // 重置表单数据
  loginForm.value = { email: '', password: '' };
  registerForm.value = { username: '', password: '', email: '', verificationCode: '' };
};

</script>

<template>
  <div class="login-container">
    <div class="login-card">
      <div class="form-container">
        <!-- 登录表单 -->
        <form v-if="isLogin" @submit.prevent="handleLogin">
          <h2>用户登录</h2>
          <div class="input-group">
            <input type="email" v-model="loginForm.email" placeholder="邮箱" required>
          </div>
          <div class="input-group">
            <input type="password" v-model="loginForm.password" placeholder="密码" required>
          </div>
          <button type="submit" class="btn">登录</button>
          <p class="switch-form">
            没有账户? <a href="#" @click.prevent="switchForm(false)">立即注册</a>
          </p>
        </form>

        <!-- 注册表单 -->
        <form v-else @submit.prevent="handleRegister">
          <h2>用户注册</h2>
          <div class="input-group">
            <input type="text" v-model="registerForm.username" placeholder="用户名 (长度3-20)" required>
          </div>
           <div class="input-group">
            <input type="email" v-model="registerForm.email" placeholder="邮箱" required>
          </div>
          <div class="input-group">
            <input type="password" v-model="registerForm.password" placeholder="密码 (不少于6位)" required>
          </div>
          <div class="input-group verification-code">
            <input type="text" v-model="registerForm.verificationCode" placeholder="验证码" required>
            <button type="button" @click="sendVerificationCode" class="btn-code" :disabled="isCodeSent">
              {{ isCodeSent ? `${countdown}秒后重试` : '获取验证码' }}
            </button>
          </div>
          <button type="submit" class="btn">注册</button>
          <p class="switch-form">
            已有账户? <a href="#" @click.prevent="switchForm(true)">立即登录</a>
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
  background-image: url('@/assets/background.png'); /* 请确保背景图片路径正确 */
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
  color: #fff; /* 调整为白色以适应深色背景 */
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
  background-color: rgba(255, 255, 255, 0.8);
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
  color: #f0f0f0;
}

.switch-form a {
  color: #87cefa; /* 淡蓝色链接 */
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
  white-space: nowrap; /* 防止文字换行 */
}

.btn-code:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}
</style>