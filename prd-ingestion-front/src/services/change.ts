import { get, post, put } from '@/utils/request'
import type { ChangeSummary, ChangeDetail, ChangeCreateRequest } from '@/models/change'
import type { BaseResponse } from '@/models/base'

// ① 生成 change.md
export const createChange = async (payload: ChangeCreateRequest) => {
  const res = await post<BaseResponse<ChangeDetail>>('/api/v1/changes', { body: payload as unknown as Record<string, unknown> })
  if (!res.data) throw new Error('创建失败')
  return res.data
}

// ② 获取 change 列表
export const getChanges = async (page = 0, size = 20) => {
  const res = await get<BaseResponse<ChangeSummary[]>>('/api/v1/changes', { params: { page, size } })
  return res.data ?? []
}

// ③ 获取 change 详情
export const getChangeDetail = async (changeId: string) => {
  const res = await get<BaseResponse<ChangeDetail>>(`/api/v1/changes/${changeId}`)
  if (!res.data) throw new Error('变更不存在')
  return res.data
}

// ④ 更新 change 内容（人工编辑后保存）
export const updateChange = async (changeId: string, content: string) => {
  const res = await put<BaseResponse<ChangeDetail>>(`/api/v1/changes/${changeId}`, { body: { content } })
  if (!res.data) throw new Error('更新失败')
  return res.data
}