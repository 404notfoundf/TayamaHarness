import { defineStore } from 'pinia'
import { ref } from 'vue'
import { useMock } from '@/config'
import * as api from '@/services/prd'
import type { PrdIngestRequest, PrdIngestResponse, IngestionProgress, RequirementEntity } from '@/models/prd'
import { MOCK_INGEST_RESULT, MOCK_INGEST_PROGRESS, MOCK_PRD_CONTENT } from '@/constants/mock'

export const usePrdStore = defineStore('prd', () => {
  const ingestionId = ref('')
  const title = ref('')
  const status = ref<IngestionProgress['status']>('pending')
  const progress = ref(0)
  const result = ref<PrdIngestResponse | null>(null)
  const loading = ref(false)
  const error = ref('')

  // ① 上传并解析 PRD
  const ingest = async (payload: PrdIngestRequest): Promise<string> => {
    error.value = ''
    loading.value = true
    title.value = payload.title || ''
    console.log('[prdStore.ingest] 开始', { title: payload.title, contentLength: payload.content?.length })

    if (useMock) {
      // 模拟异步解析过程
      status.value = 'parsing'
      progress.value = 10
      await new Promise((r) => setTimeout(r, 500))
      status.value = 'extracting'
      progress.value = 50
      await new Promise((r) => setTimeout(r, 500))
      status.value = 'generating'
      progress.value = 80
      await new Promise((r) => setTimeout(r, 500))
      status.value = 'completed'
      progress.value = 100
      ingestionId.value = MOCK_INGEST_RESULT.ingestionId
      result.value = MOCK_INGEST_RESULT
      loading.value = false
      return ingestionId.value
    }

    try {
      console.log('[prdStore.ingest] 即将调用 API', { projectId: payload.projectId, contentLength: payload.content?.length })
      // 竞争：API 请求 vs 60秒超时（后端为同步解析，长 PRD 需要更长时间）
      const res = await Promise.race([
        api.ingestPrd(payload),
        new Promise<PrdIngestResponse>((_, reject) =>
          setTimeout(() => reject(new Error('解析请求超时，请检查后端是否正常运行')), 60000),
        ),
      ])
      console.log('[prdStore.ingest] API 返回成功', { status: res?.status, ingestionId: res?.ingestionId })
      ingestionId.value = res?.ingestionId || ''
      status.value = res?.status || 'pending'
      result.value = res || null
      console.log('[prdStore.ingest] 成功', {
        ingestionId: res?.ingestionId,
        status: res?.status,
        requirements: res?.requirements?.length,
        dataEntities: res?.dataEntities?.length,
        documents: res?.documents?.length,
      })
      return res?.ingestionId
    } catch (e) {
      console.error('[prdStore.ingest] 捕获异常', (e as Error).message)
      error.value = (e as Error).message
      console.error('[prdStore.ingest] 失败', e)
      // 超时或失败时仍设置状态，避免前端卡死
      if (!status.value || status.value === 'pending') {
        status.value = 'failed'
      }
      throw e
    } finally {
      loading.value = false
      console.log('[prdStore.ingest] finally', { loading: loading.value, status: status.value })
      console.log('[prdStore.ingest] finally', { loading: loading.value, status: status.value })
    }
  }

  // ② 查询解析进度
  const fetchProgress = async (id: string) => {
    if (useMock) {
      status.value = MOCK_INGEST_PROGRESS.status
      progress.value = MOCK_INGEST_PROGRESS.progress
      return
    }
    try {
      const p = await api.getIngestionProgress(id)
      status.value = p?.status || 'pending'
      progress.value = p?.progress || 0
    } catch (e) {
      error.value = (e as Error).message
    }
  }

  // ③ 获取解析结果
  const fetchResult = async (id: string) => {
    if (useMock) {
      result.value = MOCK_INGEST_RESULT
      status.value = MOCK_INGEST_RESULT.status
      return
    }
    try {
      const res = await api.getIngestionResult(id)
      result.value = res || null
      status.value = res?.status || 'pending'
    } catch (e) {
      error.value = (e as Error).message
    }
  }

  // ⑤ 重新解析（复用已有 ingestionId，清空旧数据后重新走完整解析管线）
  const reparse = async (id: string) => {
    error.value = ''
    loading.value = true
    if (useMock) {
      // 模拟重新解析
      await new Promise((r) => setTimeout(r, 1000))
      result.value = MOCK_INGEST_RESULT
      status.value = 'completed'
      loading.value = false
      return
    }
    try {
      const res = await api.reparsePrd(id)
      result.value = res || null
      status.value = res?.status || 'pending'
    } catch (e) {
      error.value = (e as Error).message
      throw e
    } finally {
      loading.value = false
    }
  }

  // ④ 重置状态
  const reset = () => {
    ingestionId.value = ''
    title.value = ''
    status.value = 'pending'
    progress.value = 0
    result.value = null
    loading.value = false
    error.value = ''
  }

  return {
    ingestionId,
    title,
    status,
    progress,
    result,
    loading,
    error,
    ingest,
    fetchProgress,
    fetchResult,
    reparse,
    reset,
  }
})