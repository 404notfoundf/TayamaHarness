import { get, post, put, del as delRequest } from '../utils/request'
import type { BaseResponse } from '../models/base'
import type { Project, ProjectRequest } from '../models/project'

/** 项目列表 */
export function listProjects() {
  return get<BaseResponse<Project[]>>('/api/v1/projects')
}

/** 项目详情 */
export function getProject(projectId: string) {
  return get<BaseResponse<Project>>(`/api/v1/projects/${projectId}`)
}

/** 创建项目 */
export function createProject(data: ProjectRequest) {
  return post<BaseResponse<Project>>('/api/v1/projects', { body: data as unknown as Record<string, unknown> })
}

/** 更新项目 */
export function updateProject(projectId: string, data: ProjectRequest) {
  return put<BaseResponse<Project>>(`/api/v1/projects/${projectId}`, { body: data as unknown as Record<string, unknown> })
}

/** 归档项目 */
export function archiveProject(projectId: string) {
  return delRequest<BaseResponse<void>>(`/api/v1/projects/${projectId}`)
}