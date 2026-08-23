import { post, get } from '../utils/request'
import type { BaseResponse } from '../models/base'
import type { LoginRequest, RegisterRequest, LoginResponse, User } from '../models/auth'

/** 登录 */
export function loginApi(data: LoginRequest) {
  return post<BaseResponse<LoginResponse>>('/api/v1/auth/login', { body: data as unknown as Record<string, unknown> })
}

/** 注册 */
export function registerApi(data: RegisterRequest) {
  return post<BaseResponse<LoginResponse>>('/api/v1/auth/register', { body: data as unknown as Record<string, unknown> })
}

/** 获取当前用户 */
export function getCurrentUser() {
  return get<BaseResponse<User>>('/api/v1/auth/me')
}