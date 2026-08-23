import { apiPrefix, useMock } from '@/config'

// 全局请求超时（毫秒）。默认 180s：
// PRD 导入是同步等待全流程（模板→LLM→写库）完成才返回，LLM 调用可长达 60s（ai.timeout），
// 加上解析写入耗时，30s 默认值会误报超时。可通过环境变量 VITE_API_TIMEOUT 覆盖。
const TIME_OUT = Number(import.meta.env.VITE_API_TIMEOUT) || 180000

/** 从当前 URL（/project/{projectId}/...）解析项目 ID，实现所有接口自动携带 project_id */
export function resolveProjectId(): string | null {
  if (typeof window === 'undefined') return null
  const m = window.location.pathname.match(/\/project\/([^/]+)/)
  return m && m[1] ? decodeURIComponent(m[1]) : null
}

/** 从 localStorage 获取 JWT token */
function getToken(): string | null {
  return localStorage.getItem('prd_auth_token')
}

export type FetchOptionType = Omit<RequestInit, 'body'> & {
  params?: Record<string, string | number | boolean | null | undefined>
  body?: Record<string, unknown> | null
}

const baseFetch = async <T>(url: string, fetchOptions: FetchOptionType): Promise<T> => {
  const options: FetchOptionType = { method: 'GET', mode: 'cors', ...fetchOptions }
  const headers = new Headers(options.headers)

  // 全局注入 JWT token（如果存在）
  const token = getToken()
  if (token) {
    headers.set('Authorization', `Bearer ${token}`)
  }

  // 全局注入当前项目 ID（无需每个调用点手动传 projectId）
  const projectId = resolveProjectId()
  if (projectId) headers.set('X-Project-Id', projectId)

  const path = url.startsWith('/') ? url : `/${url}`
  let fullUrl = `${apiPrefix}${path}`
  const { method, params, body } = options

  if (method === 'GET' && params) {
    const search = new URLSearchParams()
    Object.entries(params).forEach(([k, v]) => {
      if (v != null) search.append(k, String(v))
    })
    const qs = search.toString()
    if (qs) fullUrl += (fullUrl.includes('?') ? '&' : '?') + qs
    delete options.params
  }

  const init: RequestInit = { method: options.method, mode: options.mode, headers }
  if (body && typeof body === 'object') {
    headers.set('Content-Type', 'application/json;charset=UTF-8')
    init.body = JSON.stringify(body)
  }

  const doFetch = fetch(fullUrl, init).then(async (resp) => {
    // 401 未登录 → 清除 token 并跳转登录页
    if (resp.status === 401) {
      localStorage.removeItem('prd_auth_token')
      localStorage.removeItem('prd_auth_user')
      const currentPath = window.location.pathname
      if (currentPath !== '/login') {
        window.location.href = `/login?redirect=${encodeURIComponent(currentPath)}`
      }
      throw new Error('未登录或登录已过期')
    }
    // 403 无权限
    if (resp.status === 403) {
      throw new Error('没有权限执行此操作')
    }

    const contentType = resp.headers.get('content-type')
    if (contentType && contentType.includes('application/json')) {
      return resp.json() as T
    }
    return resp.text() as unknown as T
  })

  // 超时处理
  return Promise.race([
    new Promise<T>((_, reject) => {
      setTimeout(() => reject(new Error('接口请求超时')), TIME_OUT)
    }),
    doFetch,
  ])
}

export const get = <T>(url: string, options: FetchOptionType = {}) =>
  baseFetch<T>(url, { ...options, method: 'GET' })

export const post = <T>(url: string, options: FetchOptionType = {}) =>
  baseFetch<T>(url, { ...options, method: 'POST' })

export const put = <T>(url: string, options: FetchOptionType = {}) =>
  baseFetch<T>(url, { ...options, method: 'PUT' })

export const del = <T>(url: string, options: FetchOptionType = {}) =>
  baseFetch<T>(url, { ...options, method: 'DELETE' })

export const delete_ = <T>(url: string, options: FetchOptionType = {}) =>
  baseFetch<T>(url, { ...options, method: 'DELETE' })