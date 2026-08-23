import { get, put, post } from '@/utils/request'
import type { WikiDocument, DocumentApprovalResponse, DocumentApprovalRequest } from '@/models/document'
import type { DocumentSummary } from '@/models/prd'
import type { BaseResponse } from '@/models/base'

// ① 获取文档列表
export const getDocuments = async (ingestionId: string) => {
  const res = await get<BaseResponse<DocumentSummary[]>>('/api/v1/documents', { params: { ingestionId } })
  return res.data ?? []
}

// ② 获取文档详情
export const getDocument = async (docId: string) => {
  const res = await get<BaseResponse<WikiDocument>>(`/api/v1/documents/${docId}`)
  if (!res.data) throw new Error('文档不存在')
  return res.data
}

// ③ 更新文档内容
export const updateDocument = async (docId: string, content: string, changeLog = '') => {
  const res = await put<BaseResponse<WikiDocument>>(`/api/v1/documents/${docId}`, { body: { content, changeLog } as unknown as Record<string, unknown> })
  if (!res.data) throw new Error('更新失败')
  return res.data
}

// ④ 审批文档
export const approveDocument = async (docId: string, action: 'approve' | 'reject', comment = '') => {
  const res = await post<BaseResponse<DocumentApprovalResponse>>(`/api/v1/documents/${docId}/approval`, { body: { action, comment } as unknown as Record<string, unknown> })
  if (!res.data) throw new Error('审批失败')
  return res.data
}