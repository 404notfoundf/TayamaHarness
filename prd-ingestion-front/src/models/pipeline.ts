// ============================================================
// 流水线相关类型
// ============================================================

import type { ChangeStatus } from './change'

/** 流水线阶段 */
export type PipelineStage = 'drafting' | 'reviewing' | 'approved' | 'completed'

/** 流水线阶段定义 */
export type StageDefinition = {
  stage: PipelineStage
  label: string
  description: string
}

/** 流水线状态 */
export type PipelineStatus = {
  changeId: string
  currentStage: PipelineStage
  stages: {
    stage: PipelineStage
    status: 'pending' | 'active' | 'completed' | 'failed' | 'skipped'
    startedAt?: string
    completedAt?: string
  }[]
  progress: number // 0-100
}

/** 变更日志条目 */
export type ChangeLogEntry = {
  id: string
  type: 'status_change' | 'approval' | 'rejection' | 'comment' | 'commit' | 'pr'
  message: string
  detail?: string
  actor: string
  createdAt: string
}

/** 需求追溯节点 */
export type TraceNode = {
  id: string
  type: 'requirement' | 'document' | 'change' | 'commit' | 'pr' | 'deploy'
  label: string
  status?: string
  children?: TraceNode[]
  url?: string
}