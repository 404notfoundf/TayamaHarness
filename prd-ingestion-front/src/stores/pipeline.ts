import { defineStore } from 'pinia'
import { ref } from 'vue'
import { useMock } from '@/config'
import * as api from '@/services/pipeline'
import type { PipelineStatus, ChangeLogEntry, TraceNode } from '@/models/pipeline'
import { MOCK_PIPELINE_STATUS, MOCK_CHANGE_LOGS, MOCK_TRACE_TREE } from '@/constants/mock'

export const usePipelineStore = defineStore('pipeline', () => {
  const pipelineStatus = ref<PipelineStatus | null>(null)
  const changeLogs = ref<ChangeLogEntry[]>([])
  const traceTree = ref<TraceNode | null>(null)
  const loading = ref(false)
  const error = ref('')

  // ① 获取流水线状态
  const fetchPipelineStatus = async (changeId: string) => {
    loading.value = true
    error.value = ''
    if (useMock) {
      pipelineStatus.value = MOCK_PIPELINE_STATUS
      loading.value = false
      return
    }
    try {
      pipelineStatus.value = await api.getPipelineStatus(changeId)
    } catch (e) {
      error.value = (e as Error).message
    } finally {
      loading.value = false
    }
  }

  // ② 获取变更日志
  const fetchChangeLogs = async (changeId: string) => {
    if (useMock) {
      changeLogs.value = MOCK_CHANGE_LOGS
      return
    }
    try {
      changeLogs.value = await api.getChangeLogs(changeId)
    } catch (e) {
      error.value = (e as Error).message
    }
  }

  // ③ 推动流水线
  const advancePipeline = async (changeId: string, stage: string) => {
    error.value = ''
    if (useMock) {
      if (pipelineStatus.value) {
        pipelineStatus.value = {
          ...pipelineStatus.value,
          currentStage: stage as PipelineStatus['currentStage'],
          stages: pipelineStatus.value.stages.map((s) => ({
            ...s,
            status: s.stage === stage ? 'active' : s.status === 'completed' ? 'completed' : s.status,
          })),
          progress: Math.min(pipelineStatus.value.progress + 15, 100),
        }
      }
      return
    }
    try {
      pipelineStatus.value = await api.advancePipeline(changeId, stage)
    } catch (e) {
      error.value = (e as Error).message
      throw e
    }
  }

  // ④ 获取追溯树
  const fetchTraceTree = async (reqId: string) => {
    if (useMock) {
      traceTree.value = MOCK_TRACE_TREE
      return
    }
    try {
      traceTree.value = await api.getRequirementTrace(reqId)
    } catch (e) {
      error.value = (e as Error).message
    }
  }

  return {
    pipelineStatus,
    changeLogs,
    traceTree,
    loading,
    error,
    fetchPipelineStatus,
    fetchChangeLogs,
    advancePipeline,
    fetchTraceTree,
  }
})