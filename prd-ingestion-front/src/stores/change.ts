import { defineStore } from 'pinia'
import { ref } from 'vue'
import { useMock } from '@/config'
import * as api from '@/services/change'
import type { ChangeSummary, ChangeDetail, ChangeCreateRequest } from '@/models/change'
import { MOCK_CHANGE_SUMMARIES, MOCK_CHANGE_DETAIL } from '@/constants/mock'

export const useChangeStore = defineStore('change', () => {
  const changes = ref<ChangeSummary[]>([])
  const currentChange = ref<ChangeDetail | null>(null)
  const loading = ref(false)
  const error = ref('')

  // ① 生成 change
  const createChange = async (payload: ChangeCreateRequest) => {
    error.value = ''
    loading.value = true
    if (useMock) {
      await new Promise((r) => setTimeout(r, 500))
      currentChange.value = MOCK_CHANGE_DETAIL
      changes.value = MOCK_CHANGE_SUMMARIES
      loading.value = false
      return
    }
    try {
      const detail = await api.createChange(payload)
      currentChange.value = detail
      // 追加到列表头部，不覆盖旧数据
      const newSummary: ChangeSummary = {
        changeId: detail.changeId,
        title: detail.title,
        status: detail.status,
        requirementIds: detail.requirementIds,
        documentRefs: detail.documentRefs,
        createdAt: detail.createdAt,
        updatedAt: detail.updatedAt,
      }
      changes.value = [newSummary, ...changes.value]
    } catch (e) {
      error.value = (e as Error).message
    } finally {
      loading.value = false
    }
  }

  // ② 获取 change 列表
  const loadChanges = async (page = 0, size = 20) => {
    if (useMock) {
      changes.value = MOCK_CHANGE_SUMMARIES
      return
    }
    try {
      changes.value = await api.getChanges(page, size)
    } catch (e) {
      error.value = (e as Error).message
    }
  }

  // ③ 获取 change 详情
  const fetchChange = async (changeId: string) => {
    if (useMock) {
      currentChange.value = MOCK_CHANGE_DETAIL
      return
    }
    try {
      currentChange.value = await api.getChangeDetail(changeId)
    } catch (e) {
      error.value = (e as Error).message
    }
  }

  // ④ 更新 change 内容（人工编辑后保存）
  const updateChange = async (changeId: string, content: string) => {
    error.value = ''
    if (useMock) {
      if (currentChange.value) {
        currentChange.value = { ...currentChange.value, content }
      }
      return
    }
    try {
      currentChange.value = await api.updateChange(changeId, content)
    } catch (e) {
      error.value = (e as Error).message
    }
  }

  // ⑤ 重新生成 change 内容（覆盖已有，不新建 changeId）
  const regenerateChange = async (changeId: string) => {
    error.value = ''
    loading.value = true
    if (useMock) {
      await new Promise((r) => setTimeout(r, 500))
      // mock 模拟重新生成：更新 content 时间戳
      if (currentChange.value) {
        currentChange.value = {
          ...currentChange.value,
          updatedAt: new Date().toISOString(),
        }
      }
      loading.value = false
      return
    }
    try {
      currentChange.value = await api.regenerateChange(changeId)
    } catch (e) {
      error.value = (e as Error).message
      throw e
    } finally {
      loading.value = false
    }
  }

  return {
    changes,
    currentChange,
    loading,
    error,
    createChange,
    loadChanges,
    fetchChange,
    updateChange,
    regenerateChange,
  }
})