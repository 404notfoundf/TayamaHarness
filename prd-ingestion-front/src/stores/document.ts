import { defineStore } from 'pinia'
import { ref } from 'vue'
import { useMock } from '@/config'
import * as api from '@/services/document'
import type { WikiDocument } from '@/models/document'
import type { DocumentSummary, DocumentStatus } from '@/models/prd'
import { MOCK_WIKI_DOCUMENTS, MOCK_DOC_SUMMARIES } from '@/constants/mock'

export const useDocumentStore = defineStore('document', () => {
  const documents = ref<WikiDocument[]>([])
  const summaries = ref<DocumentSummary[]>([])
  const currentDoc = ref<WikiDocument | null>(null)
  const loading = ref(false)
  const error = ref('')

  // ① 加载文档列表
  const loadDocuments = async (ingestionId: string) => {
    loading.value = true
    error.value = ''
    if (useMock) {
      summaries.value = MOCK_DOC_SUMMARIES
      documents.value = MOCK_WIKI_DOCUMENTS
      loading.value = false
      return
    }
    try {
      summaries.value = await api.getDocuments(ingestionId)
      // 加载每个文档详情（单个失败不阻断整体，避免卡在 Promise.all）
      const docs = await Promise.all(
        summaries.value.map(async (s) => {
          try {
            return await api.getDocument(s.docId)
          } catch (e) {
            console.warn('[documentStore] 获取文档详情失败', s.docId, e)
            return null
          }
        }),
      )
      documents.value = docs.filter((d): d is NonNullable<typeof d> => d !== null)
    } catch (e) {
      error.value = (e as Error).message
    } finally {
      loading.value = false
    }
  }

  // ② 获取文档详情
  const fetchDocument = async (docId: string) => {
    if (useMock) {
      currentDoc.value = MOCK_WIKI_DOCUMENTS.find((d) => d.docId === docId) || null
      return
    }
    try {
      currentDoc.value = await api.getDocument(docId)
    } catch (e) {
      error.value = (e as Error).message
    }
  }

  // ③ 更新文档
  const updateDocument = async (docId: string, content: string, changeLog = '') => {
    error.value = ''
    if (useMock) {
      const idx = documents.value.findIndex((d) => d.docId === docId)
      if (idx >= 0) {
        const doc = documents.value[idx]!
        documents.value[idx] = { ...doc, content, changeLog, version: doc.version + 1 }
      }
      if (currentDoc.value?.docId === docId && currentDoc.value) {
        currentDoc.value = { ...currentDoc.value, content, changeLog, version: currentDoc.value.version + 1 }
      }
      return
    }
    try {
      const updated = await api.updateDocument(docId, content, changeLog)
      const idx = documents.value.findIndex((d) => d.docId === docId)
      if (idx >= 0) documents.value[idx] = updated
      currentDoc.value = updated
    } catch (e) {
      error.value = (e as Error).message
    }
  }

  // ④ 审批文档
  const approveDocument = async (docId: string, action: 'approve' | 'reject', comment = '') => {
    error.value = ''
    if (useMock) {
      const newStatus: DocumentStatus = action === 'approve' ? 'approved' : 'rejected'
      const idx = documents.value.findIndex((d) => d.docId === docId)
      if (idx >= 0) {
        const doc = documents.value[idx]!
        documents.value[idx] = { ...doc, status: newStatus }
      }
      const sIdx = summaries.value.findIndex((s) => s.docId === docId)
      if (sIdx >= 0) {
        const s = summaries.value[sIdx]!
        summaries.value[sIdx] = { ...s, status: newStatus }
      }
      if (currentDoc.value?.docId === docId) {
        currentDoc.value = { ...currentDoc.value, status: newStatus }
      }
      return
    }
    try {
      const resp = await api.approveDocument(docId, action, comment)
      // 用后端返回的最新状态同步本地，审批通过/驳回后立即反映到界面
      const newStatus: DocumentStatus = resp.status
      const idx = documents.value.findIndex((d) => d.docId === docId)
      if (idx >= 0) documents.value[idx] = { ...documents.value[idx]!, status: newStatus }
      const sIdx = summaries.value.findIndex((s) => s.docId === docId)
      if (sIdx >= 0) summaries.value[sIdx] = { ...summaries.value[sIdx]!, status: newStatus }
      if (currentDoc.value?.docId === docId) {
        currentDoc.value = { ...currentDoc.value, status: newStatus }
      }
    } catch (e) {
      error.value = (e as Error).message
    }
  }

  return {
    documents,
    summaries,
    currentDoc,
    loading,
    error,
    loadDocuments,
    fetchDocument,
    updateDocument,
    approveDocument,
  }
})