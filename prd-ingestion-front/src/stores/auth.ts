import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { loginApi, registerApi, getCurrentUser } from '@/services/auth'
import type { LoginRequest, RegisterRequest, LoginResponse, User } from '@/models/auth'

const TOKEN_KEY = 'prd_auth_token'
const USER_KEY = 'prd_auth_user'

export const useAuthStore = defineStore('auth', () => {
  // 从 localStorage 恢复状态
  const token = ref<string | null>(localStorage.getItem(TOKEN_KEY))
  const user = ref<User | null>(loadUser())

  function loadUser(): User | null {
    const raw = localStorage.getItem(USER_KEY)
    if (!raw) return null
    try {
      return JSON.parse(raw) as User
    } catch {
      return null
    }
  }

  const isLoggedIn = computed(() => !!token.value && !!user.value)
  const displayName = computed(() => user.value?.displayName || user.value?.username || '')
  const userId = computed(() => user.value?.userId || '')

  /** 登录 */
  async function login(data: LoginRequest): Promise<LoginResponse> {
    const res = await loginApi(data)
    if (res.code !== 'OK') {
      throw new Error(res.message || '登录失败')
    }
    const loginResp = res.data
    saveToken(loginResp.token)
    // 获取用户信息
    await fetchUser()
    return loginResp
  }

  /** 注册 */
  async function register(data: RegisterRequest): Promise<LoginResponse> {
    const res = await registerApi(data)
    if (res.code !== 'OK') {
      throw new Error(res.message || '注册失败')
    }
    const loginResp = res.data
    saveToken(loginResp.token)
    // 获取用户信息
    await fetchUser()
    return loginResp
  }

  /** 获取当前用户信息 */
  async function fetchUser() {
    try {
      const res = await getCurrentUser()
      if (res.code === 'OK') {
        user.value = res.data
        localStorage.setItem(USER_KEY, JSON.stringify(res.data))
      }
    } catch (e) {
      console.error('获取用户信息失败', e)
    }
  }

  /** 保存 token */
  function saveToken(newToken: string) {
    token.value = newToken
    localStorage.setItem(TOKEN_KEY, newToken)
  }

  /** 登出 */
  function logout() {
    token.value = null
    user.value = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  }

  return {
    token,
    user,
    isLoggedIn,
    displayName,
    userId,
    login,
    register,
    fetchUser,
    logout,
  }
})