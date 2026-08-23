// ============================================================
// 模板管理相关类型
// ============================================================

import type { DocumentType } from './prd'

/** 模板版本 */
export type TemplateVersion = {
  version: number
  content: string
  updatedAt: string
  updatedBy: string
  changeLog: string
}

/** 模板详情 */
export type Template = {
  templateId: string
  type: DocumentType
  name: string
  description: string
  content: string // Markdown 模板，含 {{PLACEHOLDER}} 变量
  version: number
  versions: TemplateVersion[]
  updatedAt: string
  updatedBy: string
}

/** 模板创建请求 */
export type TemplateCreateRequest = {
  type: DocumentType
  name: string
  description: string
  content: string
}

/** 模板更新请求 */
export type TemplateUpdateRequest = {
  content: string
  changeLog: string
}