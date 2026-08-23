// ============================================================
// Wiki 文档相关类型
// ============================================================

import type { DocumentStatus, DocumentType } from './prd'

export type EntityAttribute = {
  name: string
  type: string
  description: string
}
export type EntityRelation = {
  target: string
  type: string
  description: string
}
export type DataEntity = {
  name: string
  description: string
  sourceParagraph?: string
  attributes?: EntityAttribute[]
  relations?: EntityRelation[]
}
export type RequirementEntity = {
  id: string
  description: string
  priority?: string
  relatedEntities?: string[]
  sourceParagraph?: string
  notes?: string
}
export type InterfaceProtocol = {
  method: string
  path: string
  summary?: string
  requestBody?: string
  responseBody?: string
  notes?: string
}
export type ArchitectureDecision = {
  id?: string
  title: string
  context?: string
  decision?: string
  consequences?: string[]
  status?: string
}

/** wiki 文档详情 */
export type WikiDocument = {
  docId: string
  type: DocumentType
  title: string
  status: DocumentStatus
  version: number
  content: string // Markdown 内容（已按模板渲染拆分）
  originalPrdContent: string // 对应的原始 PRD 段落
  changeLog: string
  createdBy: string
  updatedAt: string
  ingestionId: string
  projectId?: string
  // ---- LLM/解析提取结果（后端补充，可能为空：LLM 未启用或失败时为空列表）----
  parseSource?: string // llm | template | section_hierarchy | keyword_fallback
  parseSourceLabel?: string
  parseErrorMessage?: string
  requirements?: RequirementEntity[]
  dataEntities?: DataEntity[]
  interfaces?: InterfaceProtocol[]
  archDecisions?: ArchitectureDecision[]
  /** LLM 解析结果（旁路存储，仅用于左侧"LLM 提取结果"展示；LLM 未启用/失败时为 null） */
  llmExtraction?: {
    requirements?: RequirementEntity[]
    dataEntities?: DataEntity[]
    interfaces?: InterfaceProtocol[]
    archDecisions?: ArchitectureDecision[]
  } | null
}

/** 文档更新请求 */
export type DocumentUpdateRequest = {
  content: string
  changeLog: string
}

/** 文档审批请求 */
export type DocumentApprovalRequest = {
  action: 'approve' | 'reject'
  comment?: string
}

/** 文档审批结果 */
export type DocumentApprovalResponse = {
  docId: string
  status: DocumentStatus
  comment?: string
}