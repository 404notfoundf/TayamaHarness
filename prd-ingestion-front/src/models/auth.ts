/** 用户信息 */
export interface User {
  userId: string
  username: string
  displayName: string
  email: string
  avatarUrl: string | null
  status: string
  roles: string[]
  createdAt: string
  updatedAt: string
}

/** 登录请求 */
export interface LoginRequest {
  username: string
  password: string
}

/** 注册请求 */
export interface RegisterRequest {
  username: string
  password: string
  displayName?: string
  email?: string
  role?: string
}

/** 登录响应 */
export interface LoginResponse {
  token: string
  userId: string
  username: string
  displayName: string
  email: string
  roles: string[]
}

/** 项目成员 */
export interface ProjectMember {
  userId: string
  username: string
  displayName: string
  email: string
  role: string
  createdAt: string
}