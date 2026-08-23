<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()

const isLogin = ref(true)
const username = ref('')
const password = ref('')
const displayName = ref('')
const email = ref('')
const loading = ref(false)
const errorMsg = ref('')
const showPassword = ref(false)

async function handleSubmit() {
  errorMsg.value = ''
  if (!username.value || !password.value) {
    errorMsg.value = '请输入用户名和密码'
    return
  }
  loading.value = true
  try {
    if (isLogin.value) {
      await auth.login({ username: username.value, password: password.value })
    } else {
      if (password.value.length < 6) {
        errorMsg.value = '密码不能少于6位'
        loading.value = false
        return
      }
      await auth.register({
        username: username.value,
        password: password.value,
        displayName: displayName.value || undefined,
        email: email.value || undefined,
      })
    }
    // 登录成功，跳转到项目列表
    router.push('/projects')
  } catch (e: any) {
    errorMsg.value = e.message || '操作失败'
  } finally {
    loading.value = false
  }
}

function toggleMode() {
  isLogin.value = !isLogin.value
  errorMsg.value = ''
}
</script>

<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-header">
        <div class="logo">⚡</div>
        <h1>Harness Flow</h1>
        <p class="subtitle">PRD 前置转换系统</p>
      </div>

      <form class="login-form" @submit.prevent="handleSubmit">
        <div class="form-group">
          <label>用户名</label>
          <input
            v-model="username"
            class="input"
            type="text"
            placeholder="输入用户名"
            autocomplete="username"
          />
        </div>

        <div class="form-group password-group">
          <label>密码</label>
          <div class="password-wrapper">
            <input
              v-model="password"
              class="input"
              :type="showPassword ? 'text' : 'password'"
              placeholder="输入密码"
              autocomplete="current-password"
            />
            <button type="button" class="eye-btn" @click="showPassword = !showPassword" :title="showPassword ? '隐藏密码' : '显示密码'">
              {{ showPassword ? '🙈' : '👁️' }}
            </button>
          </div>
        </div>

        <div v-if="!isLogin" class="form-group">
          <label>显示名称（可选）</label>
          <input
            v-model="displayName"
            class="input"
            type="text"
            placeholder="输入显示名称"
          />
        </div>

        <div v-if="!isLogin" class="form-group">
          <label>邮箱（可选）</label>
          <input
            v-model="email"
            class="input"
            type="email"
            placeholder="输入邮箱"
          />
        </div>

        <div v-if="errorMsg" class="error-msg">{{ errorMsg }}</div>

        <button class="btn btn-primary btn-block" :disabled="loading">
          {{ loading ? '处理中...' : isLogin ? '登录' : '注册' }}
        </button>
      </form>

      <div class="login-footer">
        <span>{{ isLogin ? '还没有账号？' : '已有账号？' }}</span>
        <a class="link" @click="toggleMode">{{ isLogin ? '立即注册' : '去登录' }}</a>
      </div>

      <div class="demo-hint">
        <p>演示账号: <code>admin</code> / <code>admin123</code></p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.login-card {
  width: 400px;
  max-width: 90vw;
  background: #fff;
  border-radius: 16px;
  padding: 40px 36px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.logo {
  font-size: 48px;
  margin-bottom: 8px;
}

.login-header h1 {
  font-size: 24px;
  font-weight: 700;
  margin: 0;
  color: #1a1a2e;
}

.subtitle {
  font-size: 14px;
  color: #666;
  margin: 4px 0 0;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.password-group {
  position: relative;
}

.password-wrapper {
  display: flex;
  align-items: center;
  position: relative;
}

.password-wrapper .input {
  flex: 1;
  padding-right: 40px;
}

.eye-btn {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  cursor: pointer;
  font-size: 18px;
  padding: 4px;
  line-height: 1;
  opacity: 0.6;
  transition: opacity 0.15s;
}

.eye-btn:hover {
  opacity: 1;
}

.form-group label {
  font-size: 13px;
  font-weight: 600;
  color: #374151;
}

.input {
  padding: 10px 14px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
}

.input:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.error-msg {
  background: #fef2f2;
  color: #dc2626;
  padding: 8px 12px;
  border-radius: 8px;
  font-size: 13px;
  text-align: center;
}

.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-primary {
  background: #667eea;
  color: #fff;
}

.btn-primary:hover {
  background: #5a6fd6;
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-block {
  width: 100%;
  padding: 12px;
}

.login-footer {
  text-align: center;
  margin-top: 20px;
  font-size: 13px;
  color: #666;
}

.link {
  color: #667eea;
  cursor: pointer;
  font-weight: 600;
  margin-left: 4px;
}

.link:hover {
  text-decoration: underline;
}

.demo-hint {
  margin-top: 16px;
  padding: 10px;
  background: #f3f4f6;
  border-radius: 8px;
  text-align: center;
  font-size: 12px;
  color: #888;
}

.demo-hint code {
  background: #e5e7eb;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
}
</style>