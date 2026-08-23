<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { usePipelineStore } from '@/stores/pipeline'
import { useChangeStore } from '@/stores/change'
import { useDocumentStore } from '@/stores/document'
import type { WikiDocument } from '@/models/document'
import { MOCK_PIPELINE_STATUS, MOCK_CHANGE_LOGS, MOCK_CHANGE_DETAIL } from '@/constants/mock'

const route = useRoute()
const router = useRouter()
const pipelineStore = usePipelineStore()
const changeStore = useChangeStore()
const docStore = useDocumentStore()

const projectId = computed(() => route.params.projectId as string)

const pipelineStatus = ref(MOCK_PIPELINE_STATUS)
const changeLogs = ref(MOCK_CHANGE_LOGS)
const changeDetail = ref(MOCK_CHANGE_DETAIL)
const advancing = ref(false)
const noChange = ref(false)
// 解析后的真实 changeId（路由未携带时取项目内第一个 Change）
let currentChangeId = ''

// 3 个有效阶段 + completed 作为最终状态
const activeStages = ['drafting', 'reviewing', 'approved']

const stageLabels: Record<string, string> = {
  drafting: '草案',
  reviewing: '评审',
  approved: '已批准',
  completed: '已完成',
}

const stageIcons: Record<string, string> = {
  drafting: '📝',
  reviewing: '🔍',
  approved: '✅',
  completed: '🎉',
}

const currentStage = computed(() => pipelineStatus.value?.currentStage || 'drafting')

const stageStatus = (stage: string) => {
  const s = pipelineStatus.value?.stages.find((st) => st.stage === stage)
  return s?.status || 'pending'
}

const isActive = (stage: string) => stage === currentStage.value
const isCompleted = (stage: string) => stageStatus(stage) === 'completed'
const isPending = (stage: string) => stageStatus(stage) === 'pending'

const progressPercent = computed(() => {
  const stages = pipelineStatus.value?.stages || []
  const completed = stages.filter((s) => s.status === 'completed').length
  // 总阶段数取 activeStages，completed 不计入进度条
  const total = activeStages.length
  return Math.round((completed / total) * 100)
})

// 一键复制 change.md 内容的状态与处理
const copied = ref(false)
let copyTimer: number | undefined

// 编辑 change.md 的状态
const editing = ref(false)
const editContent = ref('')
const saving = ref(false)

const copyContent = async () => {
  const text = changeDetail.value?.content
  if (!text) return
  try {
    await navigator.clipboard.writeText(text)
  } catch {
    // 降级方案：兼容非安全上下文（如 http 非 localhost）下 clipboard API 不可用
    const ta = document.createElement('textarea')
    ta.value = text
    ta.style.position = 'fixed'
    ta.style.opacity = '0'
    document.body.appendChild(ta)
    ta.select()
    document.execCommand('copy')
    document.body.removeChild(ta)
  }
  copied.value = true
  if (copyTimer !== undefined) window.clearTimeout(copyTimer)
  copyTimer = window.setTimeout(() => {
    copied.value = false
  }, 2000)
}

// 开始编辑（把当前内容填入编辑器）
const startEdit = () => {
  editContent.value = changeDetail.value?.content || ''
  editing.value = true
}

// 取消编辑，还原内容
const cancelEdit = () => {
  editing.value = false
  editContent.value = ''
}

// 保存编辑的 change.md 内容
const saveEdit = async () => {
  const changeId = currentChangeId
  if (!changeId || !editContent.value) return
  saving.value = true
  try {
    await changeStore.updateChange(changeId, editContent.value)
    changeDetail.value = changeStore.currentChange || changeDetail.value
    editing.value = false
  } catch (e) {
    alert('保存失败: ' + (e as Error).message)
  } finally {
    saving.value = false
  }
}

onUnmounted(() => {
  if (copyTimer !== undefined) window.clearTimeout(copyTimer)
})

const handleAdvance = async () => {
  const allStages = ['drafting', 'reviewing', 'approved', 'completed']
  const idx = allStages.indexOf(currentStage.value)
  if (idx < allStages.length - 1) {
    const nextStage = allStages[idx + 1]
    if (!nextStage) return
    const changeId = currentChangeId || 'C-001'
    advancing.value = true
    try {
      await pipelineStore.advancePipeline(changeId, nextStage)
      // 以服务端最新状态刷新左侧流水线进度
      pipelineStatus.value = pipelineStore.pipelineStatus || MOCK_PIPELINE_STATUS
      // 重新拉取变更日志（后端已写入"流水线推进至"记录）
      await pipelineStore.fetchChangeLogs(changeId)
      changeLogs.value = pipelineStore.changeLogs.length > 0 ? pipelineStore.changeLogs : MOCK_CHANGE_LOGS
    } catch (e) {
      // 推进失败：重新拉取服务端状态（后端可能已部分推进），并明确提示，不再静默回退到旧状态
      await pipelineStore.fetchPipelineStatus(changeId)
      pipelineStatus.value = pipelineStore.pipelineStatus || MOCK_PIPELINE_STATUS
      alert(`流水线推进失败: ${(e as Error).message}`)
    } finally {
      advancing.value = false
    }
  }
}

const docStatusBadge = (status: string) => {
  const map: Record<string, string> = {
    draft: 'badge badge-draft',
    in_review: 'badge badge-review',
    approved: 'badge badge-approved',
    rejected: 'badge badge-rejected',
  }
  return map[status] || 'badge badge-draft'
}

const docTypeLabel: Record<string, string> = {
  'business-model': '业务模型',
  'data-model': '数据模型',
  'interface-protocol': '接口协议',
  'architecture-decision': '架构决策',
}

const goToKanban = () => {
  router.push(`/project/${projectId.value}/kanban`)
}

// 跳转需求看板并定位当前变更对应的卡片
const goToKanbanFocused = () => {
  router.push({
    path: `/project/${projectId.value}/kanban`,
    query: currentChangeId ? { focus: currentChangeId } : undefined,
  })
}

// 关联文档弹窗：展示 4 个技术文档的内容
const docModalVisible = ref(false)
const docModalLoading = ref(false)
const docModalDoc = ref<WikiDocument | null>(null)

const openDocument = async (ref: { docId: string; title: string; type: string; status: string }) => {
  docModalVisible.value = true
  docModalLoading.value = true
  docModalDoc.value = null
  await docStore.fetchDocument(ref.docId)
  docModalLoading.value = false
  docModalDoc.value = docStore.currentDoc
}

const closeDocument = () => {
  docModalVisible.value = false
  docModalLoading.value = false
  docModalDoc.value = null
}

// 弹窗文档：解析来源徽标与提取结果（与校验模板页面一致的左右对照展示）
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
  if (ps === 'llm') return { text: 'LLM 提取结果', cls: 'badge badge-llm' }
  if (ps === 'template') return { text: '模板匹配结果', cls: 'badge badge-template' }
  if (ps === 'keyword_fallback') return { text: '关键词兜底算法', cls: 'badge badge-fallback' }
  return { text: '章节层级算法结果', cls: 'badge badge-fallback' }
})
const docModalExtraction = computed(() => {
  const t = docModalDoc.value?.type
  // 只要 llmExtraction 旁路结果存在即优先展示（LLM 原始提取结果，最完整），
  // 再回退到 doc.* 字段（确定性章节算法结果，与模板渲染数据一致）。
  // 不依赖 parseSource === 'llm' 判断——LLM 部分成功时 parseSource 可能是
  // section_hierarchy，但 llmExtraction 里仍可能有完整数据（与校验面板逻辑一致）。
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

// 文档类型图标 / 状态文案 helper（弹窗与列表行共用）
const docIcon = (type?: string) =>
  type === 'business-model' ? '📋' : type === 'data-model' ? '💾' : type === 'interface-protocol' ? '🔌' : '🏗️'

const docStatusText = (status?: string) =>
  status === 'approved' ? '已批准' : status === 'draft' ? '草稿' : status === 'rejected' ? '已拒绝' : (status || '')

onMounted(async () => {
  let changeId = route.params.changeId as string

  // 路由未携带 changeId（如从校验面板「进入流水线」直接跳转）时，
  // 取当前项目第一个 Change，避免展示 mock 数据
  if (!changeId) {
    await changeStore.loadChanges(0, 50)
    changeId = changeStore.changes[0]?.changeId || ''
  }

  if (!changeId) {
    noChange.value = true
    return
  }
  currentChangeId = changeId

  await pipelineStore.fetchPipelineStatus(changeId)
  pipelineStatus.value = pipelineStore.pipelineStatus || MOCK_PIPELINE_STATUS

  await pipelineStore.fetchChangeLogs(changeId)
  changeLogs.value = pipelineStore.changeLogs.length > 0 ? pipelineStore.changeLogs : MOCK_CHANGE_LOGS

  await changeStore.fetchChange(changeId)
  changeDetail.value = changeStore.currentChange || MOCK_CHANGE_DETAIL
})
</script>

<template>
  <!-- 项目下暂无 Change 时的空态（不再回退展示 mock 数据） -->
  <div v-if="noChange" class="empty-state mt-8">
    <div class="icon">🆕</div>
    <div class="title">暂无 Change</div>
    <div class="desc">请先在导入页面解析 PRD，系统将自动生成 Change 并进入流水线</div>
    <button class="btn btn-primary mt-4" @click="router.push(`/project/${projectId}/prd/import`)">去导入 PRD</button>
  </div>

  <div v-else class="pipeline-page">
    <div class="page-section flex items-center justify-between">
      <div>
        <h1 class="text-xl font-bold">流水线详情</h1>
        <p class="text-sm text-slate-500 mt-1">{{ changeDetail?.title || '用户登录模块实现' }}</p>
      </div>
      <button class="btn btn-outline text-sm" @click="goToKanban">← 返回看板</button>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <!-- 左侧：流水线进度 -->
      <div class="lg:col-span-2 space-y-6">
        <!-- 进度概览 -->
        <div class="card">
          <div class="card-header">
            <span>⚙️ 流水线进度</span>
            <span class="ml-auto text-sm text-slate-400">{{ stageLabels[currentStage] || currentStage }}</span>
          </div>
          <div class="card-body">
            <!-- 进度条 -->
            <div class="bar big mb-6">
              <i :style="{ width: progressPercent + '%' }"></i>
            </div>

            <!-- 阶段节点 -->
            <div class="stage-flow">
              <div
                v-for="(stage, idx) in pipelineStatus?.stages || []"
                :key="stage.stage"
                class="stage-node"
                :class="{
                  active: isActive(stage.stage),
                  completed: isCompleted(stage.stage),
                  pending: isPending(stage.stage),
                }"
              >
                <div class="stage-dot">
                  <span v-if="isCompleted(stage.stage)">✓</span>
                  <span v-else-if="isActive(stage.stage)">●</span>
                  <span v-else>○</span>
                </div>
                <div class="stage-label">{{ stageIcons[stage.stage] }} {{ stageLabels[stage.stage] }}</div>
                <div class="stage-time" v-if="stage.startedAt">
                  {{ new Date(stage.startedAt).toLocaleDateString() }}
                </div>
                <!-- 连接线 -->
                <div v-if="idx < (pipelineStatus?.stages?.length || 0) - 1" class="stage-connector" :class="{ completed: isCompleted(stage.stage) }"></div>
              </div>
            </div>

            <!-- 操作按钮 -->
            <div class="mt-6 pt-4 border-t border-slate-200 flex justify-between items-center">
              <div class="text-sm text-slate-500">
                当前阶段: <strong>{{ stageLabels[currentStage] }}</strong>
              </div>
              <button
                class="btn btn-primary"
                :disabled="currentStage === 'completed' || advancing"
                @click="handleAdvance"
              >
                {{ advancing ? '推进中...' : currentStage === 'approved' ? '🏁 标记完成' : '➡️ 推进到下一阶段' }}
              </button>
            </div>
          </div>
        </div>

        <!-- change.md 正文内容 -->
        <div class="card">
          <div class="card-header flex items-center justify-between">
            <span>📄 change.md 内容</span>
            <span class="flex items-center gap-3">
              <span v-if="changeDetail?.content" class="text-xs text-slate-400">{{ changeDetail.content.length }} 字符</span>
              <!-- 编辑按钮：drafting/reviewing 阶段可编辑 -->
              <button v-if="!editing && (currentStage === 'drafting' || currentStage === 'reviewing')" class="btn btn-ghost btn-sm" @click="startEdit">
                ✏️ 编辑
              </button>
              <button class="btn btn-ghost btn-sm" :disabled="!changeDetail?.content" @click="copyContent">
                {{ copied ? '✓ 已复制' : '📋 一键复制' }}
              </button>
            </span>
          </div>
          <div class="card-body">
            <!-- 编辑模式 -->
            <div v-if="editing" class="space-y-3">
              <textarea
                v-model="editContent"
                class="w-full text-sm font-mono border border-slate-300 rounded-lg p-4 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 resize-y min-h-[320px] max-h-[640px]"
                :disabled="saving"
              ></textarea>
              <div class="flex justify-end gap-2">
                <button class="btn btn-outline btn-sm" :disabled="saving" @click="cancelEdit">取消</button>
                <button class="btn btn-primary btn-sm" :disabled="saving" @click="saveEdit">
                  {{ saving ? '⏳ 保存中...' : '💾 保存' }}
                </button>
              </div>
            </div>
            <!-- 只读显示模式 -->
            <pre v-else-if="changeDetail?.content" class="text-sm text-slate-700 whitespace-pre-wrap font-mono bg-slate-50 p-4 rounded-lg max-h-[480px] overflow-y-auto">{{ changeDetail.content }}</pre>
            <div v-else class="text-sm text-slate-400 py-8 text-center">暂无 change.md 内容</div>
          </div>
        </div>

        <!-- 关联文档：点击文档行弹窗展示内容；「查看看板」跳转看板并定位当前变更 -->
        <div class="card">
          <div class="card-header flex items-center justify-between">
            <span>📑 关联文档</span>
            <button class="text-xs text-blue-600 hover:underline" @click="goToKanbanFocused">查看看板（定位当前变更）→</button>
          </div>
          <div class="card-body">
            <div class="space-y-2">
              <div
                v-for="ref in changeDetail?.documentRefs || []"
                :key="ref.docId"
                class="flex items-center gap-3 p-3 bg-slate-50 rounded-lg cursor-pointer hover:bg-slate-100 transition-colors"
                @click="openDocument(ref)"
                :title="`查看${docTypeLabel[ref.type] || ref.type}文档内容`"
              >
                <span class="text-lg">{{ docIcon(ref.type) }}</span>
                <div class="flex-1">
                  <div class="text-sm font-medium">{{ ref.title }}</div>
                  <div class="text-xs text-slate-500">{{ docTypeLabel[ref.type] || ref.type }}</div>
                </div>
                <span :class="docStatusBadge(ref.status)">{{ docStatusText(ref.status) }}</span>
                <span class="text-xs text-blue-500 whitespace-nowrap">查看内容 →</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 技术文档内容弹窗（与模板管理新建模板弹窗同一套视觉：modal-fade 动画 + modal-panel 分区 + footer 操作） -->
        <Transition name="modal-fade">
          <div
            v-if="docModalVisible"
            class="fixed inset-0 z-50 flex items-center justify-center bg-black/30"
            @click.self="closeDocument"
            @keydown.esc="closeDocument"
          >
            <div class="modal-panel flex flex-col w-[1280px] max-w-[calc(100vw-32px)] rounded-xl bg-white shadow-xl">
              <!-- 头部 -->
              <div class="flex items-center justify-between modal-header border-b border-slate-200">
                <div class="flex items-center gap-2 min-w-0">
                  <span class="text-lg">{{ docIcon(docModalDoc?.type) }}</span>
                  <h3 class="text-[15px] font-semibold truncate">{{ docModalDoc?.title || '文档内容' }}</h3>
                  <span v-if="docModalDoc" :class="docStatusBadge(docModalDoc.status)">{{ docStatusText(docModalDoc.status) }}</span>
                </div>
                <button
                  class="text-slate-400 hover:text-slate-700 text-lg leading-none px-1"
                  @click="closeDocument"
                  aria-label="关闭"
                >✕</button>
              </div>

              <!-- 主体：元信息 + 解析来源徽标 + 提取结果 / PRD 原文 左右对照（与校验模板一致） -->
              <div class="modal-body max-h-[calc(100vh-120px)] overflow-y-auto">
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

                <!-- 左右对照：左 = PRD 原文 | 右 = 提取结果/按模板渲染（二选一），总共保持 2 卡片 -->
                <div v-if="docModalLoading" class="text-sm text-slate-400 py-10 text-center bg-slate-50 rounded-lg">⏳ 文档内容加载中...</div>
                <div v-else class="grid grid-cols-1 xl:grid-cols-5 gap-4">
                  <!-- 左列：PRD 原文 -->
                  <div class="xl:col-span-2">
                    <div class="card h-full">
                      <div class="card-header">📜 PRD 原文</div>
                      <div class="card-body extraction-scroll whitespace-pre-wrap">{{ docModalDoc?.originalPrdContent || '（无对应原文段落）' }}</div>
                    </div>
                  </div>

                  <!-- 右列：提取结果卡（有数据时）；无数据时以按模板渲染文档兜底 -->
                  <div class="xl:col-span-3">
                    <div v-if="docModalExtraction.items.length" class="card h-full border-indigo-200">
                      <div class="card-header bg-indigo-50">🧠 {{ docModalExtraction.label }} — {{ docModalParseSourceLabel }}（{{ docModalExtraction.items.length }} 条）</div>
                      <div class="card-body extraction-scroll">
                        <template v-if="docModalDoc?.type === 'data-model'">
                          <div v-for="(e, i) in (docModalExtraction.items as any[])" :key="i" class="mb-3 pb-3 border-b border-slate-100 last:border-0">
                            <div class="font-semibold text-indigo-700">▣ {{ e.name }}</div>
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
                            <div v-if="it.summary" class="text-xs text-slate-500 mt-0.5">{{ it.summary }}</div>
                          </div>
                        </template>
                        <template v-else-if="docModalDoc?.type === 'architecture-decision'">
                          <div v-for="(a, i) in (docModalExtraction.items as any[])" :key="i" class="mb-2 pb-2 border-b border-slate-100 last:border-0">
                            <div class="font-semibold text-indigo-700 text-sm">📌 {{ a.title }}</div>
                            <div v-if="a.decision" class="text-xs text-slate-600 mt-0.5">{{ a.decision }}</div>
                          </div>
                        </template>
                        <template v-else>
                          <div v-for="(r, i) in (docModalExtraction.items as any[])" :key="i" class="mb-2 pb-2 border-b border-slate-100 last:border-0">
                            <div class="text-sm text-slate-700"><span class="font-mono text-xs text-slate-400 mr-1">{{ r.id }}</span>{{ r.description }}</div>
                          </div>
                        </template>
                      </div>
                    </div>
                    <!-- 无提取结果时，直接以按模板渲染的最终文档兜底展示（替代黄色空态提示） -->
                    <div v-else class="card h-full border-slate-200">
                      <div class="card-header bg-slate-50">📄 按模板渲染：{{ docModalParseSourceLabel }}</div>
                      <div class="card-body extraction-scroll whitespace-pre-wrap">{{ docModalDoc?.content || '（暂无内容）' }}</div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 底部操作 -->
              <div class="flex gap-2 justify-end modal-footer border-t border-slate-200">
                <button class="btn btn-ghost" @click="closeDocument">关闭</button>
              </div>
            </div>
          </div>
        </Transition>

        <!-- 验收标准（隐藏，保留数据） -->
        <!--
        <div class="card" v-if="false">
          <div class="card-header">✅ 验收标准</div>
          <div class="card-body">
            <div class="space-y-2">
              <div v-for="ac in changeDetail?.acceptanceCriteria || []" :key="ac.id" class="flex items-start gap-3 p-2">
                <span class="text-sm text-slate-400 mt-0.5">☐</span>
                <div>
                  <span class="text-xs font-mono text-slate-400">{{ ac.id }}:</span>
                  <span class="text-sm ml-2">{{ ac.description }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
        -->
      </div>

      <!-- 右侧：变更日志 -->
      <div class="lg:col-span-1 space-y-4">
        <div class="card">
          <div class="card-header">📋 变更日志</div>
          <div class="card-body p-0">
            <div class="divide-y divide-slate-100">
              <div
                v-for="log in changeLogs"
                :key="log.id"
                class="p-3 hover:bg-slate-50"
              >
                <div class="flex items-start gap-2">
                  <span class="text-sm mt-0.5">
                    {{ log.type === 'status_change' ? '⚡' : log.type === 'approval' ? '✅' : log.type === 'rejection' ? '❌' : log.type === 'comment' ? '💬' : log.type === 'commit' ? '💻' : '🔀' }}
                  </span>
                  <div class="flex-1 min-w-0">
                    <div class="text-sm">{{ log.message }}</div>
                    <div v-if="log.detail" class="text-xs text-slate-500 mt-0.5">{{ log.detail }}</div>
                    <div class="text-xs text-slate-400 mt-1">{{ new Date(log.createdAt).toLocaleString() }}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Change 信息 -->
        <div class="card">
          <div class="card-header">🆔 Change 信息</div>
          <div class="card-body text-sm space-y-2">
            <div class="flex justify-between">
              <span class="text-slate-500">ID</span>
              <span class="font-mono">{{ changeDetail?.changeId }}</span>
            </div>
            <div class="flex justify-between">
              <span class="text-slate-500">状态</span>
              <span class="badge" :class="changeDetail?.status === 'approved' ? 'badge-approved' : changeDetail?.status === 'reviewing' ? 'badge-review' : 'badge-draft'">{{ stageLabels[changeDetail?.status || ''] || changeDetail?.status }}</span>
            </div>
            <div class="flex justify-between">
              <span class="text-slate-500">需求数</span>
              <span>{{ changeDetail?.requirementIds?.length || 0 }}</span>
            </div>
            <div class="flex justify-between">
              <span class="text-slate-500">创建时间</span>
              <span class="text-xs">{{ changeDetail?.createdAt ? new Date(changeDetail.createdAt).toLocaleDateString() : '-' }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* ---- 文档内容弹窗（与模板管理「新建模板」弹窗保持同一套视觉） ---- */
.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.15s ease;
}
.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}

/* 弹窗内边距（scoped CSS，不依赖 Tailwind 扫描） */
.modal-header {
  padding: 24px 40px;
}
.modal-body {
  padding: 32px 40px;
}
.modal-body > * + * {
  margin-top: 24px;
}
.modal-footer {
  padding: 20px 40px;
}

.bar.big {
  height: 8px;
  background: #e2e8f0;
  border-radius: 4px;
  overflow: hidden;
}
.bar.big > i {
  display: block;
  height: 100%;
  background: #2563eb;
  border-radius: 4px;
  transition: width 0.5s ease;
}

/* 流水线阶段 */
.stage-flow {
  display: flex;
  align-items: flex-start;
  gap: 0;
  position: relative;
}
.stage-node {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
  position: relative;
  padding: 0 4px;
}
.stage-dot {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 700;
  margin-bottom: 6px;
  transition: all 0.2s;
}
.stage-node.active .stage-dot {
  background: #2563eb;
  color: #fff;
  box-shadow: 0 0 0 4px rgba(37, 99, 235, 0.2);
}
.stage-node.completed .stage-dot {
  background: #16a34a;
  color: #fff;
}
.stage-node.pending .stage-dot {
  background: #f1f5f9;
  color: #94a3b8;
  border: 2px solid #e2e8f0;
}
.stage-label {
  font-size: 11px;
  font-weight: 600;
  color: #64748b;
  text-align: center;
  white-space: nowrap;
}
.stage-node.active .stage-label {
  color: #2563eb;
  font-weight: 700;
}
.stage-node.completed .stage-label {
  color: #16a34a;
}
.stage-time {
  font-size: 10px;
  color: #94a3b8;
  margin-top: 2px;
}
.stage-connector {
  position: absolute;
  top: 15px;
  left: 50%;
  width: 100%;
  height: 2px;
  background: #e2e8f0;
  z-index: -1;
}
.stage-connector.completed {
  background: #16a34a;
}

/* ---- 弹窗左侧提取结果徽标与滚动（与校验模板页面一致） ---- */
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
.extraction-scroll {
  max-height: 480px;
  overflow-y: auto;
}
</style>