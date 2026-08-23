// ============================================================
// 文件上传（MinIO 分块上传）相关类型
// ============================================================

/** 上传状态 */
export type UploadStatus = 'UPLOADING' | 'MERGING' | 'PARSING' | 'COMPLETED' | 'FAILED'

/** 分块上传结果 */
export type ChunkUploadResult = {
  fileMd5: string
  chunkIndex: number
  totalChunks: number
  size: number
}

/** 上传进度查询结果 */
export type UploadProgress = {
  fileMd5: string
  uploadedChunks: number[]
  totalChunks: number
  progress: number
  status: UploadStatus
}

/** 合并结果 */
export type MergeResult = {
  fileMd5: string
  fileName: string
  status: UploadStatus
  minioObject: string
  presignedUrl: string
}