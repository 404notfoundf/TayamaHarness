import { get, post } from '@/utils/request'
import type { PrdIngestRequest, PrdIngestResponse, IngestionProgress } from '@/models/prd'
import type { BaseResponse } from '@/models/base'

// ① 上传并解析 PRD
export const ingestPrd = async (payload: PrdIngestRequest) => {
  const res = await post<BaseResponse<PrdIngestResponse>>('/api/v1/prd/ingest', { body: payload as unknown as Record<string, unknown> })
  if (!res.data) throw new Error('解析失败')
  return res.data
}

// ② 查询解析进度
export const getIngestionProgress = async (ingestionId: string) => {
  const res = await get<BaseResponse<IngestionProgress>>(`/api/v1/prd/ingest/${ingestionId}/progress`)
  if (!res.data) throw new Error('进度信息不存在')
  return res.data
}

// ③ 获取解析结果
export const getIngestionResult = async (ingestionId: string) => {
  const res = await get<BaseResponse<PrdIngestResponse>>(`/api/v1/prd/ingest/${ingestionId}`)
  if (!res.data) throw new Error('解析结果不存在')
  return res.data
}

// ④ 重新解析
export const reparsePrd = async (ingestionId: string) => {
  const res = await post<BaseResponse<PrdIngestResponse>>(`/api/v1/prd/ingest/${ingestionId}/reparse`)
  if (!res.data) throw new Error('重新解析失败')
  return res.data
}

// ⑤ 人工确认候选条目（proposed → confirmed / accepted）
// type ∈ requirement | entity | interface | decision
// id 分别为 req_id / entity_name / 「method|path」/ ad_id
export const confirmCandidate = async (ingestionId: string, type: string, id: string) => {
  const res = await post<BaseResponse<number>>(`/api/v1/prd/ingest/${ingestionId}/confirm`, {
    body: { type, id },
  })
  if (!res.data) throw new Error('确认失败')
  return res.data
}