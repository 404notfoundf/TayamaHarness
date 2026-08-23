// 基础响应数据格式
export type BaseResponse<T> = {
  code: string
  message: string
  data: T
}

// 统一错误响应结构
export type ErrorResponse = {
  code: string
  message: string
  traceId?: string
}

// 分页请求
export type PageRequest = {
  page?: number
  size?: number
  sort?: string
}

// 分页响应
export type PageResponse<T> = {
  content: T[]
  totalElements: number
  totalPages: number
  page: number
  size: number
}