import { get } from '../utils/request'
import type { BaseResponse } from '../models/base'
import type { Language, Framework } from '../models/project'

/** 语言列表 */
export function listLanguages() {
  return get<BaseResponse<Language[]>>('/api/v1/languages')
}

/** 框架列表（可选按语言过滤） */
export function listFrameworks(languageId?: number) {
  const params = languageId ? { languageId } : undefined
  return get<BaseResponse<Framework[]>>('/api/v1/frameworks', { params })
}