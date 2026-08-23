// ============================================================
// PRD 文档解析相关类型
// ============================================================

/** 文档状态 */
export type DocumentStatus = 'draft' | 'in_review' | 'approved' | 'rejected'

/** 文档类型 */
export type DocumentType = 'business-model' | 'data-model' | 'interface-protocol' | 'architecture-decision'

/** PRD 文档格式 */
export type PrdFormat = 'markdown' | 'docx' | 'pdf' | 'txt' | 'confluence'

/** 解析状态 */
export type IngestionStatus = 'pending' | 'parsing' | 'extracting' | 'generating' | 'completed' | 'failed'

/** 解析出的需求实体 */
export type RequirementEntity = {
  id: string
  description: string
  priority: 'P0' | 'P1' | 'P2' | 'P3'
  relatedEntities: string[]
  sourceParagraph: string // 原始 PRD 段落引用
  notes?: string
}

/** 数据实体（领域对象） */
export type DataEntity = {
  name: string
  description: string
  attributes: { name: string; type: string; description?: string }[]
  relations: { target: string; type: string; description?: string }[]
}

/** 接口协议 */
export type InterfaceProtocol = {
  method: 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH'
  path: string
  summary: string
  requestBody?: string
  responseBody?: string
  notes?: string
}

/** 架构决策 */
export type ArchitectureDecision = {
  id: string
  title: string
  context: string
  decision: string
  consequences: string[]
  status: 'proposed' | 'accepted' | 'deprecated' | 'superseded'
}

/** PRD 解析请求 */
export type PrdIngestRequest = {
  projectId: string    // 所属项目 ID
  content?: string
  fileMd5?: string    // MinIO 文件 MD5（分块上传完成后使用）
  format: PrdFormat
  title?: string
  sourceUrl?: string
}

/** 解析来源 */
export type ParseSource = 'template' | 'llm' | 'section_hierarchy'

/** PRD 解析响应 */
export type PrdIngestResponse = {
  ingestionId: string
  status: IngestionStatus
  title: string
  parseSource?: ParseSource
  requirements: RequirementEntity[]
  dataEntities: DataEntity[]
  interfaces: InterfaceProtocol[]
  architectureDecisions: ArchitectureDecision[]
  documents: DocumentSummary[]
}

/** 解析进度 */
export type IngestionProgress = {
  ingestionId: string
  status: IngestionStatus
  progress: number // 0-100
  message?: string
}

/** 文档摘要 */
export type DocumentSummary = {
  docId: string
  type: DocumentType
  title: string
  status: DocumentStatus
  version: number
  updatedAt: string
}