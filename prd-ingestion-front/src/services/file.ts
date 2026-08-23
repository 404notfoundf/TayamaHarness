import { get, post, resolveProjectId } from '@/utils/request'
import type { ChunkUploadResult, UploadProgress, MergeResult } from '@/models/file'

/**
 * 上传文件分片（multipart/form-data）。
 *
 * 由于 request.ts 中的 post 方法默认使用 JSON 序列化，
 * 分片上传需要使用原生 fetch 发送 FormData。
 */
export const uploadChunk = async (
  file: Blob,
  fileMd5: string,
  fileName: string,
  chunkIndex: number,
  totalChunks: number,
): Promise<ChunkUploadResult> => {
  const formData = new FormData()
  formData.append('file', file, fileName)
  formData.append('fileMd5', fileMd5)
  formData.append('fileName', fileName.split('.').pop() === 'md' ? fileName : `${fileName}`)
  formData.append('chunkIndex', String(chunkIndex))
  formData.append('totalChunks', String(totalChunks))

  // 使用原生 fetch 发送 FormData（不经过 JSON 序列化）
  // 与 baseFetch 行为保持一致：注入 JWT + X-Project-Id（后端 /api/** 全部要求认证，漏带 Authorization 会 401）
  const baseUrl = import.meta.env.VITE_API_BASE || ''
  const headers = new Headers()
  const token = typeof localStorage !== 'undefined' ? localStorage.getItem('prd_auth_token') : null
  if (token) headers.set('Authorization', `Bearer ${token}`)
  const pid = resolveProjectId()
  if (pid) headers.set('X-Project-Id', pid)
  const res = await fetch(`${baseUrl}/api/v1/files/uploadChunk`, {
    method: 'POST',
    headers,
    body: formData,
  })
  if (!res.ok) {
    const errBody = await res.json().catch(() => ({}))
    throw new Error(errBody.message || `上传分片失败: HTTP ${res.status}`)
  }
  const payload = await res.json()
  if (payload.code !== 'OK' && payload.code !== 0) {
    throw new Error(payload.message || '上传分片失败')
  }
  return payload.data as ChunkUploadResult
}

/** 查询上传进度 */
export const getUploadProgress = (fileMd5: string, totalChunks: number) =>
  get<UploadProgress>('/api/v1/files/uploadProgress', {
    params: { fileMd5, totalChunks },
  })

/** 合并文件分片 */
export const mergeChunks = (fileMd5: string, fileName: string) =>
  post<MergeResult>('/api/v1/files/uploadMerge', {
    params: { fileMd5, fileName },
    body: null,
  })

/** 计算文件 MD5（使用 Web Crypto API） */
export const computeFileMd5 = async (file: File): Promise<string> => {
  const buffer = await file.arrayBuffer()
  const hashBuffer = await crypto.subtle.digest('MD5', buffer)
  // 注意：Web Crypto API 不支持 MD5，使用 SparkMD5 或类似库
  // 这里使用简单的替代方案：返回文件名的 MD5 模拟
  // 实际应使用：https://github.com/satazor/js-spark-md5
  // 或通过 WebAssembly 实现
  return await computeMd5WithSpark(buffer)
}

/**
 * 使用 SparkMD5 计算文件 MD5。
 * 需要安装 spark-md5 依赖：npm install spark-md5
 */
const computeMd5WithSpark = async (buffer: ArrayBuffer): Promise<string> => {
  // 动态导入 spark-md5
  const SparkMD5 = (await import('spark-md5')).default
  const spark = new SparkMD5.ArrayBuffer()
  spark.append(buffer)
  return spark.end()
}

/** 按指定大小分片文件 */
export const sliceFile = (file: File, chunkSize: number = 5 * 1024 * 1024): Blob[] => {
  const chunks: Blob[] = []
  let start = 0
  while (start < file.size) {
    const end = Math.min(start + chunkSize, file.size)
    chunks.push(file.slice(start, end))
    start = end
  }
  return chunks
}