import { get, post, put, del } from '../utils/request'
import type { BaseResponse } from '../models/base'
import type { ProjectMember, User } from '../models/auth'

/** 获取项目成员列表 */
export function listProjectMembers(projectId: string) {
  return get<BaseResponse<ProjectMember[]>>(`/api/v1/auth/projects/${projectId}/members`)
}

/** 添加项目成员 */
export function addProjectMember(projectId: string, userId: string, role: string) {
  return post<BaseResponse<void>>(`/api/v1/auth/projects/${projectId}/members`, {
    body: { userId, role },
  })
}

/** 更新项目成员角色 */
export function updateProjectMemberRole(projectId: string, userId: string, role: string) {
  return put<BaseResponse<void>>(`/api/v1/auth/projects/${projectId}/members/${userId}`, {
    body: { role },
  })
}

/** 移除项目成员 */
export function removeProjectMember(projectId: string, userId: string) {
  return del<BaseResponse<void>>(`/api/v1/auth/projects/${projectId}/members/${userId}`)
}

/** 获取所有用户列表 */
export function listAllUsers() {
  return get<BaseResponse<User[]>>('/api/v1/auth/users')
}

/** 禁用用户 */
export function disableUserApi(userId: string) {
  return put<BaseResponse<void>>(`/api/v1/auth/users/${userId}/disable`)
}

/** 启用用户 */
export function enableUserApi(userId: string) {
  return put<BaseResponse<void>>(`/api/v1/auth/users/${userId}/enable`)
}

/** 更新用户角色 */
export function updateUserRoleApi(userId: string, role: string) {
  return put<BaseResponse<void>>(`/api/v1/auth/users/${userId}/role`, { body: { role } })
}

/** 删除用户 */
export function deleteUserApi(userId: string) {
  return del<BaseResponse<void>>(`/api/v1/auth/users/${userId}`)
}