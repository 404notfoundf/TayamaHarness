/** 编程语言 */
export interface Language {
  id: number
  name: string
  slug: string
  sortOrder: number
}

/** 框架 */
export interface Framework {
  id: number
  languageId: number
  name: string
  slug: string
  sortOrder: number
}

/** 项目 */
export interface Project {
  id: number
  projectId: string
  name: string
  description: string
  languageId: number
  languageName: string
  frameworkIds: number[]
  frameworkNames: string[]
  status: string
  createdAt: string
  updatedAt: string
}

/** 创建/更新项目请求 */
export interface ProjectRequest {
  name: string
  description?: string
  languageId: number
  frameworkIds?: number[]
  status?: string
  /** 创建项目时初始添加的成员 userId 列表 */
  memberIds?: string[]
}