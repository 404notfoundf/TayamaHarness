<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { usePipelineStore } from '@/stores/pipeline'
import { useChangeStore } from '@/stores/change'
import { useMock } from '@/config'
import { MOCK_KANBAN_STATS, MOCK_CHANGE_DETAIL, MOCK_CHANGE_LOGS } from '@/constants/mock'
import * as pipelineApi from '@/services/pipeline'
import { useDocumentStore } from '@/stores/document'
import * as docApi from '@/services/document'
import { confirmCandidate } from '@/services/prd'
import type { ChangeSummary, ChangeDetail } from '@/models/change'
import type { ChangeLogEntry } from '@/models/pipeline'
import type { WikiDocument } from '@/models/document'

const router = useRouter()
const route = useRoute()
const pipelineStore = usePipelineStore()
const changeStore = useChangeStore()

const projectId = computed(() => route.params.projectId as string)

// 看板统计（从后端获取，后端返回裸 Record，包装成与 mock 一致的 { columns } 形状）
const stats = ref<{ columns: Record<string, { count: number; label: string }> }>(MOCK_KANBAN_STATS)
const statsLoading = ref(false)

// Change 列表 → 看板卡片
const changes = ref<ChangeSummary[]>([])

// 每页显示的卡片数量
const PAGE_SIZE = 10

// 每列当前页码
const columnPages = ref<Record<string, number>>({})

const getColumnPage = (colKey: string) => columnPages.value[colKey] || 0
const setColumnPage = (colKey: string, page: number) => { columnPages.value[colKey] = page }

const prevPage = (colKey: string) => {
  const p = getColumnPage(colKey)
  if (p > 0) setColumnPage(colKey, p - 1)
}

const nextPage = (colKey: string, totalItems: number) => {
  const p = getColumnPage(colKey)
  if ((p + 1) * PAGE_SIZE < totalItems) setColumnPage(colKey, p + 1)
}

const paginatedItems = (colKey: string, items: ChangeSummary[]) => {
  const page = getColumnPage(colKey)
  return items.slice(page * PAGE_SIZE, (page + 1) * PAGE_SIZE)
}

const totalPages = (totalItems: number) => Math.max(1, Math.ceil(totalItems / PAGE_SIZE))

// 状态映射：流水线状态 → 看板列（coding/testing/verifying 归入 design_review 以正确展示）
const statusToColumn: Record<string, string> = {
  drafting: 'prd_imported',
  reviewing: 'document_review',
  approved: 'design_review',
  coding: 'design_review',
  testing: 'design_review',
  verifying: 'design_review',
  completed: 'completed',
}

// 看板列定义（仅保留原始列：PRD 导入/文档校验/设计评审/已完成）
const columns = computed(() => {
  const colMap: Record<string, { label: string; items: ChangeSummary[] }> = {
    prd_imported: { label: 'PRD 导入', items: [] },
    document_review: { label: '文档校验', items: [] },
    design_review: { label: '设计评审', items: [] },
    completed: { label: '已完成', items: [] },
  }
  changes.value.forEach((ch) => {
    const colKey = statusToColumn[ch.status] || 'prd_imported'
    const col = colMap[colKey]
    if (col) {
      col.items.push(ch)
    }
  })
  return Object.entries(colMap).map(([key, val]) => ({
    key,
    ...val,
    count: val.items.length,
  }))
})

const statusLabel = (status: string) => {
  const map: Record<string, string> = {
    drafting: '草案', reviewing: '评审中', approved: '已批准',
    coding: '开发中', testing: '测试中', verifying: '验证中', completed: '已完成',
  }
  return map[status] || status
}

const openPipeline = (changeId: string) => {
  router.push(`/project/${projectId.value}/pipeline/${changeId}`)
}

// ---- 卡片弹窗（操作日志 + 跳转流水线） ----
const showModal = ref(false)
const selectedChange = ref<ChangeSummary | null>(null)
const changeLogs = ref<ChangeLogEntry[]>([])
const modalLoading = ref(false)

const openModal = async (change: ChangeSummary) => {
  selectedChange.value = change
  showModal.value = true
  modalLoading.value = true
  // 加载 change 详情（用于弹窗展示）
  await changeStore.fetchChange(change.changeId)
  // 加载操作日志
  if (useMock) {
    changeLogs.value = MOCK_CHANGE_LOGS
  } else {
    try {
      changeLogs.value = await pipelineApi.getChangeLogs(change.changeId)
    } catch {
      changeLogs.value = []
    }
  }
  modalLoading.value = false
}

const closeModal = () => {
  showModal.value = false
  selectedChange.value = null
  changeLogs.value = []
}

const goToPipeline = (changeId: string) => {
  closeModal()
  openPipeline(changeId)
}

// ---- 文档详情弹窗（子弹窗：关闭父 → 打开子 → 关闭子 → 恢复父） ----
const docStore = useDocumentStore()
const docModalVisible = ref(false)
const docModalLoading = ref(false)
const docModalDoc = ref<WikiDocument | null>(null)
const confirming = ref(false)

const docIcon = (type?: string) =>
  type === 'business-model' ? '📋' : type === 'data-model' ? '💾' : type === 'interface-protocol' ? '🔌' : '🏗️'

const docTypeLabel: Record<string, string> = {
  'business-model': '业务模型',
  'data-model': '数据模型',
  'interface-protocol': '接口协议',
  'architecture-decision': '架构决策',
}

const docStatusBadge = (status: string) => {
  const map: Record<string, string> = {
    draft: 'bg-slate-100 text-slate-500',
    in_review: 'bg-amber-50 text-amber-600',
    approved: 'bg-green-50 text-green-600',
    rejected: 'bg-red-50 text-red-500',
  }
  return map[status] || 'bg-slate-100 text-slate-500'
}

const docStatusText = (status?: string) =>
  status === 'approved' ? '已批准' : status === 'draft' ? '草稿' : status === 'rejected' ? '已拒绝' : (status || '')

const openDocModal = async (ref: { docId: string; title: string; type: string; status: string }) => {
  showModal.value = false
  docModalLoading.value = true
  docModalDoc.value = null
  await docStore.fetchDocument(ref.docId)
  docModalLoading.value = false
  docModalDoc.value = docStore.currentDoc
  docModalVisible.value = true
}

const closeDocModal = () => {
  docModalVisible.value = false
  docModalDoc.value = null
  showModal.value = true
}

const docModalParseSourceLabel = computed(() => {
  if (docModalDoc.value?.parseSourceLabel) return docModalDoc.value.parseSourceLabel
  const ps = docModalDoc.value?.parseSource
  if (ps === 'llm') return 'LLM 提取结果'
  if (ps === 'template') return '模板匹配结果'
  if (ps === 'keyword_fallback') return '关键词兜底算法'
  return '章节层级算法结果'
})

const docModalParseSourceBadge = computed(() => {
  const ps = docModalDoc.value?.parseSource
  if (ps === 'llm') return { text: 'LLM 提取结果', cls: 'bg-indigo-50 text-indigo-600' }
  if (ps === 'template') return { text: '模板匹配结果', cls: 'bg-blue-50 text-blue-600' }
  if (ps === 'keyword_fallback') return { text: '关键词兜底算法', cls: 'bg-amber-50 text-amber-600' }
  return { text: '章节层级算法结果', cls: 'bg-amber-50 text-amber-600' }
})

const docModalExtraction = computed(() => {
  const t = docModalDoc.value?.type
  const llm = docModalDoc.value?.llmExtraction as any
  const pick = (llmKey: string, dbArr: any[] | undefined) => {
    const llmItems = llm?.[llmKey]
    return Array.isArray(llmItems) && llmItems.length ? llmItems : (dbArr || [])
  }
  if (t === 'data-model') return { label: '数据实体', items: pick('dataEntities', docModalDoc.value?.dataEntities) }
  if (t === 'interface-protocol') return { label: '接口协议', items: pick('interfaces', docModalDoc.value?.interfaces) }
  if (t === 'architecture-decision') return { label: '架构决策', items: pick('archDecisions', docModalDoc.value?.archDecisions) }
  return { label: '需求', items: pick('requirements', docModalDoc.value?.requirements) }
})

const candidateTypeOf = (docType: string) => {
  switch (docType) {
    case 'business-model': return 'requirement'
    case 'data-model': return 'entity'
    case 'interface-protocol': return 'interface'
    case 'architecture-decision': return 'decision'
    default: return ''
  }
}

const candidateIdOf = (docType: string, item: any) => {
  switch (docType) {
    case 'business-model': return String(item?.id ?? '')
    case 'data-model': return String(item?.name ?? '')
    case 'interface-protocol': return `${item?.method}|${item?.path}`
    case 'architecture-decision': return String(item?.id ?? '')
    default: return ''
  }
}

const isPendingCandidate = (docType: string, item: any) => {
  if (docType === 'architecture-decision') {
    return (item?.status || 'proposed') === 'proposed'
  }
  return (item?.candidateStatus || '') === 'proposed'
}

const isConfirmable = (docType: string, item: any) =>
  isPendingCandidate(docType, item) && candidateIdOf(docType, item) !== ''

const badgeOf = (docType: string, item: any) => {
  if (docType === 'architecture-decision') {
    if (item?.status === 'accepted') return { kind: 'ok', label: '✅ 已确认' }
    if (item?.status === 'proposed' && !!item?.id) return { kind: 'warn', label: '⚠️ 规则推导待确认' }
    return { kind: 'muted', label: '🧠 提取建议' }
  }
  if (item?.candidateStatus === 'confirmed') return { kind: 'ok', label: '✅ 原文提取' }
  if (item?.candidateStatus === 'proposed') return { kind: 'warn', label: '⚠️ 规则推导待确认' }
  return { kind: 'muted', label: '🧠 提取建议' }
}

const badgeClass = (docType: string, item: any) => {
  const kind = badgeOf(docType, item).kind
  if (kind === 'warn') return 'bg-amber-50 text-amber-700 text-xs px-2 py-0.5 rounded'
  if (kind === 'ok') return 'bg-green-50 text-green-700 text-xs px-2 py-0.5 rounded'
  return 'bg-slate-50 text-slate-400 text-xs px-2 py-0.5 rounded'
}

const badgeLabel = (docType: string, item: any) => badgeOf(docType, item).label

const confirmItem = async (item: any) => {
  const doc = docModalDoc.value
  if (!doc?.ingestionId || !doc.type) {
    alert('无法确认：缺少 ingestionId')
    return
  }
  const type = candidateTypeOf(doc.type)
  const cid = candidateIdOf(doc.type, item)
  if (!type || !cid) {
    alert('无法确认：缺少条目标识')
    return
  }
  confirming.value = true
  try {
    await confirmCandidate(doc.ingestionId, type, cid)
    await docStore.fetchDocument(doc.docId)
    docModalDoc.value = docStore.currentDoc
  } catch (e) {
    alert('确认失败: ' + (e as Error).message)
  } finally {
    confirming.value = false
  }
}

const logTypeLabel = (type: string) => {
  const map: Record<string, string> = {
    status_change: '状态变更',
    approval: '审批通过',
    rejection: '驳回',
    comment: '备注',
    commit: '提交',
    pr: 'PR',
  }
  return map[type] || type
}

const logTypeClass = (type: string) => {
  const map: Record<string, string> = {
    status_change: 'badge badge-info',
    approval: 'badge badge-success',
    rejection: 'badge badge-error',
    comment: 'badge',
    commit: 'badge',
    pr: 'badge',
  }
  return map[type] || 'badge'
}

// focus 定位：从流水线详情页「📑 关联文档 → 查看看板（定位当前变更）」跳转而来
const focusChangeId = computed(() => (route.query.focus as string) || '')
const focusHit = computed(() => changes.value.some((c) => c.changeId === focusChangeId.value))

const isFocused = (changeId: string) => changeId === focusChangeId.value

// 取消定位：清除 URL 上的 focus 参数
const clearFocus = () => {
  router.replace({ path: `/project/${projectId.value}/kanban` })
}

// 加载数据
onMounted(async () => {
  // 1. 加载看板统计
  statsLoading.value = true
  try {
    const data = await pipelineApi.getKanbanStats()
    if (data) stats.value = { columns: data }
  } catch {
    // 保持 mock
  }
  statsLoading.value = false

  // 2. 加载 Change 列表作为看板卡片
  await changeStore.loadChanges()
  changes.value = changeStore.changes.length > 0 ? changeStore.changes : []

  // 3. focus 定位：滚动到目标变更卡片并高亮
  if (focusChangeId.value) {
    await nextTick()
    document
      .querySelector(`[data-change-id="${focusChangeId.value}"]`)
      ?.scrollIntoView?.({ behavior: 'smooth', block: 'center' })
  }
})
</script>

<template>
  <div class="kanban-page">
    <div class="page-section flex items-center justify-between">
      <div>
        <h1 class="text-xl font-bold">需求看板</h1>
        <p class="text-sm text-slate-500 mt-1">PRD 变更的完整流转追踪</p>
      </div>
      <div class="text-xs text-slate-400">
        共 {{ changes.length }} 个变更
      </div>
    </div>

    <!-- 统计概览 -->
    <div class="grid grid-cols-4 gap-3 mb-6">
      <div v-for="(col, key) in stats.columns" :key="key" class="card text-center py-3">
        <div class="text-xl font-bold text-slate-800">{{ col.count }}</div>
        <div class="text-xs text-slate-500">{{ col.label }}</div>
      </div>
    </div>

    <!-- 定位提示：从流水线详情「关联文档」跳转而来 -->
    <div v-if="focusHit" class="card mb-6 border-blue-300 bg-blue-50">
      <div class="card-body flex items-center justify-between">
        <div class="text-sm text-blue-700">
          📍 已定位到变更 <strong class="font-mono">{{ focusChangeId }}</strong>
        </div>
        <button class="text-xs text-blue-600 hover:underline" @click="clearFocus">✕ 取消定位</button>
      </div>
    </div>

    <!-- 看板 -->
    <div class="kanban-board">
      <div v-for="col in columns" :key="col.key" class="kanban-column">
        <div class="column-header">
          <div class="flex items-center justify-between">
            <span>{{ col.label }}</span>
            <span class="text-xs font-normal text-slate-400">{{ col.count }}</span>
          </div>
        </div>
        <div
          v-for="item in paginatedItems(col.key, col.items)"
          :key="item.changeId"
          class="kanban-card"
          :class="isFocused(item.changeId) ? 'kanban-card-focus' : ''"
          :data-change-id="item.changeId"
          @click="openModal(item)"
        >
          <div class="flex items-start justify-between gap-2">
            <div class="card-title">{{ item.title }}</div>
            <span
              v-if="isFocused(item.changeId)"
              class="text-[11px] shrink-0 rounded px-1.5 py-0.5 text-white bg-green-600"
            >📍 当前变更</span>
          </div>
          <div class="card-meta flex items-center gap-2 mt-2">
            <span class="text-xs px-1.5 py-0.5 rounded bg-slate-100 text-slate-600">{{ statusLabel(item.status) }}</span>
            <span class="ml-auto text-xs text-slate-400">{{ item.createdAt?.slice(0, 10) }}</span>
          </div>
        </div>
        <div v-if="!col.items.length" class="text-xs text-slate-400 text-center py-4">
          暂无变更
        </div>
        <!-- 分页 -->
        <div v-if="col.items.length > PAGE_SIZE" class="flex items-center justify-center gap-2 py-2 border-t border-slate-100 mt-1">
          <button
            class="text-xs px-2 py-1 rounded hover:bg-slate-100 disabled:opacity-30 disabled:cursor-not-allowed"
            :disabled="getColumnPage(col.key) === 0"
            @click="prevPage(col.key)"
          >‹ 上一页</button>
          <span class="text-xs text-slate-400">
            {{ getColumnPage(col.key) + 1 }} / {{ totalPages(col.items.length) }}
          </span>
          <button
            class="text-xs px-2 py-1 rounded hover:bg-slate-100 disabled:opacity-30 disabled:cursor-not-allowed"
            :disabled="(getColumnPage(col.key) + 1) * PAGE_SIZE >= col.items.length"
            @click="nextPage(col.key, col.items.length)"
          >下一页 ›</button>
        </div>
      </div>
    </div>
  </div>

  <!-- 卡片弹窗：操作日志 + 跳转流水线 -->
  <Teleport to="body">
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-container">
        <div class="modal-header">
          <div class="flex items-center gap-2">
            <span class="text-lg">📋</span>
            <div>
              <div class="font-semibold text-slate-800">{{ selectedChange?.title }}</div>
              <div class="text-xs text-slate-400 font-mono">{{ selectedChange?.changeId }}</div>
            </div>
          </div>
          <button class="modal-close" @click="closeModal">✕</button>
        </div>

        <div class="modal-body">
          <div v-if="modalLoading" class="flex items-center justify-center py-8 text-sm text-slate-400">
            ⏳ 加载中...
          </div>

          <template v-else>
            <!-- 基本信息 -->
            <div class="mb-4">
              <div class="text-xs font-semibold text-slate-500 uppercase tracking-wider mb-2">基本信息</div>
              <div class="grid grid-cols-2 gap-3 text-sm">
                <div>
                  <span class="text-slate-400">状态 </span>
                  <span class="ml-2 px-1.5 py-0.5 rounded text-xs bg-slate-100 text-slate-600">{{ statusLabel(selectedChange?.status || '') }}</span>
                </div>
                <div>
                  <span class="text-slate-400">创建时间 </span>
                  <span class="ml-2 text-slate-600">{{ selectedChange?.createdAt?.slice(0, 10) }}</span>
                </div>
              </div>
            </div>

            <!-- 关联文档 -->
            <div v-if="selectedChange?.documentRefs?.length" class="mb-4">
              <div class="text-xs font-semibold text-slate-500 uppercase tracking-wider mb-2">关联文档</div>
              <div class="flex flex-wrap gap-2">
                <button
                  v-for="ref in selectedChange.documentRefs"
                  :key="ref.docId"
                  class="text-xs px-2 py-1 rounded bg-slate-50 text-slate-600 border border-slate-200 hover:bg-indigo-50 hover:border-indigo-300 hover:text-indigo-600 transition-colors cursor-pointer"
                  @click="openDocModal(ref)"
                >{{ docIcon(ref.type) }} {{ ref.title }}</button>
              </div>
            </div>

            <!-- 操作日志 -->
            <div>
              <div class="text-xs font-semibold text-slate-500 uppercase tracking-wider mb-2">操作日志</div>
              <div v-if="changeLogs.length === 0" class="text-sm text-slate-400 py-4 text-center">暂无操作日志</div>
              <div v-else class="log-list">
                <div v-for="log in changeLogs" :key="log.id" class="log-item">
                  <div class="log-dot"></div>
                  <div class="log-content">
                    <div class="flex items-center gap-2">
                      <span :class="logTypeClass(log.type)">{{ logTypeLabel(log.type) }}</span>
                      <span class="text-xs text-slate-400">{{ log.actor }}</span>
                    </div>
                    <div class="text-sm text-slate-700 mt-0.5">{{ log.message }}</div>
                    <div v-if="log.detail" class="text-xs text-slate-400 mt-0.5">{{ log.detail }}</div>
                    <div class="text-xs text-slate-300 mt-0.5">{{ log.createdAt }}</div>
                  </div>
                </div>
              </div>
            </div>
          </template>
        </div>

        <div class="modal-footer">
          <button class="btn btn-outline btn-sm" @click="closeModal">关闭</button>
          <button class="btn btn-primary btn-sm" @click="goToPipeline(selectedChange?.changeId || '')">
            ➡️ 跳转到流水线
          </button>
        </div>
      </div>
    </div>
  </Teleport>

  <!-- 文档详情弹窗（子弹窗：关闭父 → 打开子 → 关闭子 → 恢复父，与流水线弹窗一致） -->
  <Teleport to="body">
    <Transition name="modal-fade">
      <div v-if="docModalVisible" class="modal-overlay" @click.self="closeDocModal">
        <div class="modal-panel">
          <div class="modal-header">
            <div class="flex items-center gap-3">
              <span class="text-xl">{{ docIcon(docModalDoc?.type) }}</span>
              <div>
                <div class="text-lg font-bold text-slate-800">{{ docModalDoc?.title || docTypeLabel[docModalDoc?.type || ''] || '文档' }}</div>
                <div class="text-xs text-slate-400">{{ docModalDoc?.docId }}</div>
              </div>
            </div>
            <button class="modal-close" @click="closeDocModal">✕</button>
          </div>

          <div class="modal-body">
            <div v-if="docModalLoading" class="text-sm text-slate-400 py-10 text-center bg-slate-50 rounded-lg">⏳ 文档内容加载中...</div>

            <template v-else>
              <!-- 文档元信息 -->
              <div class="grid grid-cols-3 gap-6">
                <div>
                  <label class="text-xs font-semibold text-slate-500 uppercase tracking-wider">文档类型</label>
                  <div class="mt-1 text-sm text-slate-700">{{ docTypeLabel[docModalDoc?.type || ''] || docModalDoc?.type || '-' }}</div>
                </div>
                <div>
                  <label class="text-xs font-semibold text-slate-500 uppercase tracking-wider">版本</label>
                  <div class="mt-1 text-sm text-slate-700">v{{ docModalDoc?.version ?? '-' }}</div>
                </div>
                <div>
                  <label class="text-xs font-semibold text-slate-500 uppercase tracking-wider">更新时间</label>
                  <div class="mt-1 text-sm text-slate-700">{{ docModalDoc?.updatedAt ? new Date(docModalDoc.updatedAt).toLocaleString() : '-' }}</div>
                </div>
              </div>

              <div class="flex items-center gap-2 text-xs text-slate-500">
                <span>解析来源</span>
                <span v-if="docModalDoc" class="badge" :class="docModalParseSourceBadge.cls">{{ docModalParseSourceBadge.text }}</span>
                <span v-if="docModalDoc?.parseErrorMessage" class="badge badge-error" :title="docModalDoc.parseErrorMessage">LLM 失败</span>
                <span class="ml-auto">{{ docModalDoc?.content?.length || 0 }} 字符（按模板渲染）</span>
              </div>
              <p v-if="docModalDoc?.parseErrorMessage" class="text-xs text-amber-600">⚠️ LLM 解析失败（{{ docModalDoc.parseErrorMessage }}），当前展示按模板渲染的兜底结果</p>

              <!-- 左右对照：左 = PRD 原文 | 右 = 提取结果 -->
              <div class="grid grid-cols-1 xl:grid-cols-5 gap-4">
                <!-- 左列：PRD 原文 -->
                <div class="xl:col-span-2">
                  <div class="card h-full">
                    <div class="card-header">📜 PRD 原文</div>
                    <div class="card-body extraction-scroll whitespace-pre-wrap">{{ docModalDoc?.originalPrdContent || '（无对应原文段落）' }}</div>
                  </div>
                </div>

                <!-- 右列：提取结果 -->
                <div class="xl:col-span-3">
                  <div v-if="docModalExtraction.items.length" class="card h-full border-indigo-200">
                    <div class="card-header bg-indigo-50">🧠 {{ docModalExtraction.label }} — {{ docModalParseSourceLabel }}（{{ docModalExtraction.items.length }} 条）</div>
                    <div class="card-body extraction-scroll">
                      <template v-if="docModalDoc?.type === 'data-model'">
                        <div v-for="(e, i) in (docModalExtraction.items as any[])" :key="i" class="mb-3 pb-3 border-b border-slate-100 last:border-0">
                          <div class="font-semibold text-indigo-700">▣ {{ e.name }}</div>
                          <div class="flex items-center gap-2 mt-1">
                            <span :class="badgeClass(docModalDoc?.type || '', e)">{{ badgeLabel(docModalDoc?.type || '', e) }}</span>
                            <button v-if="isConfirmable(docModalDoc?.type || '', e)" class="btn btn-success btn-xs" :disabled="confirming" @click="confirmItem(e)">确认</button>
                          </div>
                          <div v-if="e.description" class="text-sm text-slate-600 mt-0.5">{{ e.description }}</div>
                          <div v-if="e.attributes && e.attributes.length" class="mt-1 text-xs">
                            <span v-for="(a, ai) in e.attributes" :key="ai" class="inline-block bg-slate-100 rounded px-1.5 py-0.5 mr-1 mb-1">{{ a.name }}: {{ a.type }}</span>
                          </div>
                        </div>
                      </template>
                      <template v-else-if="docModalDoc?.type === 'interface-protocol'">
                        <div v-for="(it, i) in (docModalExtraction.items as any[])" :key="i" class="mb-2 pb-2 border-b border-slate-100 last:border-0">
                          <span class="font-mono text-sm font-bold text-indigo-700">{{ it.method }}</span>
                          <span class="font-mono text-sm text-slate-800 ml-1">{{ it.path }}</span>
                          <div class="flex items-center gap-2 mt-1">
                            <span :class="badgeClass(docModalDoc?.type || '', it)">{{ badgeLabel(docModalDoc?.type || '', it) }}</span>
                            <button v-if="isConfirmable(docModalDoc?.type || '', it)" class="btn btn-success btn-xs" :disabled="confirming" @click="confirmItem(it)">确认</button>
                          </div>
                          <div v-if="it.summary" class="text-xs text-slate-500 mt-0.5">{{ it.summary }}</div>
                        </div>
                      </template>
                      <template v-else-if="docModalDoc?.type === 'architecture-decision'">
                        <div v-for="(a, i) in (docModalExtraction.items as any[])" :key="i" class="mb-2 pb-2 border-b border-slate-100 last:border-0">
                          <div class="font-semibold text-indigo-700 text-sm">📌 {{ a.title }}</div>
                          <div class="flex items-center gap-2 mt-1">
                            <span :class="badgeClass(docModalDoc?.type || '', a)">{{ badgeLabel(docModalDoc?.type || '', a) }}</span>
                            <button v-if="isConfirmable(docModalDoc?.type || '', a)" class="btn btn-success btn-xs" :disabled="confirming" @click="confirmItem(a)">确认</button>
                          </div>
                          <div v-if="a.decision" class="text-xs text-slate-600 mt-0.5">{{ a.decision }}</div>
                        </div>
                      </template>
                      <template v-else>
                        <div v-for="(r, i) in (docModalExtraction.items as any[])" :key="i" class="mb-2 pb-2 border-b border-slate-100 last:border-0">
                          <div class="text-sm text-slate-700"><span class="font-mono text-xs text-slate-400 mr-1">{{ r.id }}</span>{{ r.description }}</div>
                          <div class="flex items-center gap-2 mt-1">
                            <span :class="badgeClass(docModalDoc?.type || '', r)">{{ badgeLabel(docModalDoc?.type || '', r) }}</span>
                            <button v-if="isConfirmable(docModalDoc?.type || '', r)" class="btn btn-success btn-xs" :disabled="confirming" @click="confirmItem(r)">确认</button>
                          </div>
                        </div>
                      </template>
                    </div>
                  </div>
                  <div v-else class="card h-full border-slate-200">
                    <div class="card-header bg-slate-50">📄 按模板渲染：{{ docModalParseSourceLabel }}</div>
                    <div class="card-body extraction-scroll whitespace-pre-wrap">{{ docModalDoc?.content || '（暂无内容）' }}</div>
                  </div>
                </div>
              </div>
            </template>
          </div>

          <div class="modal-footer">
            <button class="btn btn-ghost" @click="closeDocModal">关闭</button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
/* ---- 弹窗遮罩 ---- */
.modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.5);
  padding: 2rem;
}

/* ---- 弹窗容器（与流水线一致） ---- */
.modal-panel {
  background: #fff;
  border-radius: 12px;
  width: 100%;
  max-width: 1200px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

/* 父弹窗（需求看板详情），保持较小尺寸 */
.modal-container {
  background: #fff;
  border-radius: 12px;
  width: 100%;
  max-width: 520px;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.modal-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: 24px 40px;
  border-bottom: 1px solid #e2e8f0;
}

.modal-close {
  background: none;
  border: none;
  font-size: 18px;
  color: #94a3b8;
  cursor: pointer;
  padding: 4px;
  line-height: 1;
}

.modal-close:hover {
  color: #475569;
}

.modal-body {
  flex: 1;
  overflow-y: auto;
  padding: 32px 40px;
}

.modal-body > * + * {
  margin-top: 24px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding: 20px 40px;
  border-top: 1px solid #e2e8f0;
}

/* ---- 弹窗动画 ---- */
.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.15s ease;
}
.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}

/* ---- 日志时间线 ---- */
.log-list {
  position: relative;
}

.log-item {
  display: flex;
  gap: 12px;
  padding-bottom: 16px;
  position: relative;
}

.log-item:not(:last-child)::before {
  content: '';
  position: absolute;
  left: 5px;
  top: 14px;
  bottom: 0;
  width: 2px;
  background: #e2e8f0;
}

.log-dot {
  width: 12px;
  height: 12px;
  min-width: 12px;
  border-radius: 50%;
  background: #cbd5e1;
  margin-top: 4px;
  z-index: 1;
}

.log-content {
  flex: 1;
  min-width: 0;
}

/* ---- 徽章（与流水线一致） ---- */
.badge {
  display: inline-block;
  font-size: 11px;
  line-height: 1;
  padding: 3px 8px;
  border-radius: 9999px;
  font-weight: 600;
}
.badge-llm {
  background: #dbeafe;
  color: #1d4ed8;
}
.badge-template {
  background: #dcfce7;
  color: #15803d;
}
.badge-fallback {
  background: #fef3c7;
  color: #b45309;
}
.badge-error {
  background: #fee2e2;
  color: #b91c1c;
}
.badge-info {
  background: #dbeafe;
  color: #2563eb;
}
.badge-success {
  background: #dcfce7;
  color: #16a34a;
}

/* ---- 候选条目徽标（与流水线一致） ---- */
.cand-badge {
  display: inline-block;
  font-size: 11px;
  line-height: 1;
  padding: 3px 8px;
  border-radius: 9999px;
  font-weight: 600;
  background: #fef3c7;
  color: #b45309;
}
.cand-badge-ok {
  display: inline-block;
  font-size: 11px;
  line-height: 1;
  padding: 3px 6px;
  border-radius: 9999px;
  font-weight: 600;
  background: #dcfce7;
  color: #15803d;
}
.cand-badge-muted {
  display: inline-block;
  font-size: 11px;
  line-height: 1;
  padding: 3px 8px;
  border-radius: 9999px;
  font-weight: 600;
  background: #e2e8f0;
  color: #475569;
}

.btn-xs {
  font-size: 11px;
  line-height: 1;
  padding: 4px 8px;
}

.extraction-scroll {
  max-height: 480px;
  overflow-y: auto;
}
</style>
