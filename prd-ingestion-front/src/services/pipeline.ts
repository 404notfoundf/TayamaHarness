import { get, post } from '@/utils/request'
import type { PipelineStatus, ChangeLogEntry, TraceNode } from '@/models/pipeline'
import type { BaseResponse } from '@/models/base'

// ① 获取流水线状态
export const getPipelineStatus = async (changeId: string) => {
  const res = await get<BaseResponse<PipelineStatus>>(`/api/v1/pipeline/${changeId}`)
  if (!res.data) throw new Error('流水线不存在')
  return res.data
}

// ② 获取变更日志
export const getChangeLogs = async (changeId: string) => {
  const res = await get<BaseResponse<ChangeLogEntry[]>>(`/api/v1/pipeline/${changeId}/logs`)
  return res.data ?? []
}

// ③ 推动流水线
export const advancePipeline = async (changeId: string, stage: string) => {
  const res = await post<BaseResponse<PipelineStatus>>(`/api/v1/pipeline/${changeId}/advance`, { body: { stage } as unknown as Record<string, unknown> })
  if (!res.data) throw new Error('操作失败')
  return res.data
}

// ④ 获取追溯树
export const getRequirementTrace = async (reqId: string) => {
  const res = await get<BaseResponse<TraceNode>>(`/api/v1/requirements/${reqId}/trace`)
  if (!res.data) throw new Error('追溯信息不存在')
  return res.data
}

// ⑤ 获取看板统计
export const getKanbanStats = async () => {
  const res = await get<BaseResponse<Record<string, { count: number; label: string }>>>('/api/v1/kanban/stats')
  return res.data ?? {}
}