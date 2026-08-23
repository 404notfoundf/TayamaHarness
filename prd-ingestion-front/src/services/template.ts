import { get, put, post } from '@/utils/request'
import type { Template, TemplateCreateRequest, TemplateUpdateRequest } from '@/models/template'
import type { BaseResponse } from '@/models/base'

// ① 获取模板列表
export const getTemplates = async () => {
  const res = await get<BaseResponse<Template[]>>('/api/v1/templates')
  return res.data ?? []
}

// ② 获取模板详情
export const getTemplate = async (templateId: string) => {
  const res = await get<BaseResponse<Template>>(`/api/v1/templates/${templateId}`)
  if (!res.data) throw new Error('模板不存在')
  return res.data
}

// ③ 更新模板
export const updateTemplate = async (templateId: string, payload: TemplateUpdateRequest) => {
  const res = await put<BaseResponse<Template>>(`/api/v1/templates/${templateId}`, { body: payload as unknown as Record<string, unknown> })
  if (!res.data) throw new Error('更新失败')
  return res.data
}

// ④ 新建模板
export const createTemplate = async (payload: TemplateCreateRequest) => {
  const res = await post<BaseResponse<Template>>('/api/v1/templates', { body: payload as unknown as Record<string, unknown> })
  if (!res.data) throw new Error('创建失败')
  return res.data
}