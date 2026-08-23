// ============================================================
// Change 管理相关类型
// ============================================================

import type { DocumentType } from './prd'

/** Change 状态 */
export type ChangeStatus = 'drafting' | 'reviewing' | 'approved' | 'completed'

/** Change 摘要 */
export type ChangeSummary = {
  changeId: string
  title: string
  status: ChangeStatus
  requirementIds: string[]
  documentRefs: { type: DocumentType; docId: string; title: string; status: string }[]
  createdAt: string
  updatedAt: string
}

/** Change 详情 */
export type ChangeDetail = {
  changeId: string
  title: string
  status: ChangeStatus
  content: string // Markdown 格式的 change.md
  requirementIds: string[]
  documentRefs: { type: DocumentType; docId: string; title: string; status: string }[]
  acceptanceCriteria: { id: string; description: string }[]
  dependencies: string[]
  createdAt: string
  updatedAt: string
}

/** Change 生成请求 */
export type ChangeCreateRequest = {
  ingestionId: string
  docIds: string[]
  title?: string
}