<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { usePrdStore } from '@/stores/prd'
import { useDocumentStore } from '@/stores/document'
import { useChangeStore } from '@/stores/change'
import type { WikiDocument } from '@/models/document'

const route = useRoute()
const router = useRouter()
const prdStore = usePrdStore()
const docStore = useDocumentStore()
const changeStore = useChangeStore()

const activeTab = ref<string>('business-model')
const editingDoc = ref<WikiDocument | null>(null)
const editContent = ref('')
const editLog = ref('')
const showEditor = ref(false)
const comment = ref('')
const approvalLoading = ref(false)
const generating = ref(false)
const reparsing = ref(false)

// 当前 ingestionId（复用 onMounted 的查找逻辑）
const currentIngestionId = computed(() => {
  return (route.params.ingestionId as string) ||
    (route.query.ingestionId as string) ||
    prdStore.ingestionId ||
    ''
})

// 文档列表
const docList = computed(() => {
  if (!docStore.documents.length && prdStore.result?.documents) {
    return prdStore.result.documents
  }
  return docStore.documents.map((d) => ({
    docId: d.docId,
    type: d.type,
    title: d.title,
    status: d.status,
    version: d.version,
    updatedAt: d.updatedAt,
  }))
})

// 当前文档
const currentDoc = computed(() => {
  return docStore.documents.find((d) => d.type === activeTab.value) || null
})

// 当前文档的原始 PRD 段落
const originalText = computed(() => {
  return currentDoc.value?.originalPrdContent || ''
})

// 所有文档是否都已 approved
const allApproved = computed(() => {
  if (!docStore.documents.length) return false
  return docStore.documents.every((d) => d.status === 'approved')
})

// 切换文档
const switchTab = (type: string) => {
  activeTab.value = type
  closeEditor()
}

// 打开编辑器
const openEditor = (doc: WikiDocument) => {
  editingDoc.value = doc
  editContent.value = doc.content
  editLog.value = ''
  showEditor.value = true
}

// 关闭编辑器
const closeEditor = () => {
  showEditor.value = false
  editingDoc.value = null
  editContent.value = ''
  editLog.value = ''
}

// 保存编辑
const saveEdit = async () => {
  if (!editingDoc.value) return
  await docStore.updateDocument(editingDoc.value.docId, editContent.value, editLog.value)
  closeEditor()
}

// 审批文档
const handleApproval = async (action: 'approve' | 'reject') => {
  if (!currentDoc.value) return
  approvalLoading.value = true
  await docStore.approveDocument(currentDoc.value.docId, action, comment.value)
  comment.value = ''
  approvalLoading.value = false
}

// 重新解析（清空当前结果后重新走完整解析管线）
const handleReparse = async () => {
  const id = currentIngestionId.value
  if (!id) {
    alert('缺少 ingestionId，无法重新解析')
    return
  }
  if (!window.confirm('重新解析将清空当前所有解析结果，确定继续？')) return
  reparsing.value = true
  try {
    await prdStore.reparse(id)
    if (prdStore.error) {
      alert('重新解析失败: ' + prdStore.error)
      return
    }
    // 重新加载文档列表
    await docStore.loadDocuments(id)
    // 重置到第一个文档 tab
    if (docStore.documents.length > 0) {
      activeTab.value = docStore.documents[0].type
    }
  } catch (e) {
    alert('重新解析失败: ' + (e as Error).message)
  } finally {
    reparsing.value = false
  }
}

// 生成 change.md 并进入流水线（审核全部通过后调用）
// 记录「当前 ingestionId 已生成的 changeId」，仅同一次解析内复用，
// 避免重新解析 PRD 后误跳到旧 change（旧 change 可能已完成，导致页面显示全部完成）
let generatedMap: Record<string, string | undefined> = {}
const generateAndGoPipeline = async () => {
  // 兼容三种来源：路径参数（旧格式）→ query 参数 → store（从 PRD 导入页跳转）
  const ingestionId =
    (route.params.ingestionId as string) ||
    (route.query.ingestionId as string) ||
    prdStore.ingestionId
  if (!ingestionId) {
    alert('缺少 ingestionId，无法生成 change.md')
    return
  }
  generating.value = true
  try {
    // 同一 PRD（同一 ingestionId）重复点击时复用已生成的 change，避免重复创建
    const hit = generatedMap[ingestionId]
    if (hit) {
      router.push(`/project/${route.params.projectId}/pipeline/${hit}`)
      return
    }
    const docIds = docStore.documents.filter((d) => d.status === 'approved').map((d) => d.docId)
    await changeStore.createChange({ ingestionId, docIds })
    if (changeStore.error) {
      alert('生成 change.md 失败: ' + changeStore.error)
      return
    }
    const changeId = changeStore.currentChange?.changeId
    if (!changeId) {
      alert('生成 change.md 失败: 未返回 changeId')
      return
    }
    generatedMap[ingestionId] = changeId
    router.push(`/project/${route.params.projectId}/pipeline/${changeId}`)
  } finally {
    generating.value = false
  }
}

// 文档类型标签
const typeLabel = (type: string) => {
  const map: Record<string, string> = {
    'business-model': '业务模型',
    'data-model': '数据模型',
    'interface-protocol': '接口协议',
    'architecture-decision': '架构决策',
  }
  return map[type] || type
}

const typeIcon = (type: string) => {
  const map: Record<string, string> = {
    'business-model': '📋',
    'data-model': '💾',
    'interface-protocol': '🔌',
    'architecture-decision': '🏗️',
  }
  return map[type] || '📄'
}

const parseSourceLabel = computed(() => {
  // 优先用后端返回的文档级解析来源（每个文档随接口返回），再回退到文档/ingest 的 parseSource
  const fromDoc = currentDoc.value?.parseSourceLabel
  if (fromDoc) return fromDoc
  const source = currentDoc.value?.parseSource || prdStore.result?.parseSource
  const map: Record<string, string> = {
    template: '模板匹配结果',
    llm: 'LLM 提取结果',
    section_hierarchy: '章节层级算法结果',
    keyword_fallback: '关键词匹配结果',
  }
  return map[source || ''] || '提取结果'
})

const parseSourceBadge = computed(() => {
  const source = currentDoc.value?.parseSource || prdStore.result?.parseSource || ''
  const map: Record<string, { text: string; cls: string }> = {
    llm: { text: 'LLM 提取', cls: 'badge-llm' },
    template: { text: '模板匹配', cls: 'badge-template' },
    section_hierarchy: { text: '章节算法', cls: 'badge-fallback' },
    keyword_fallback: { text: '关键词匹配', cls: 'badge-fallback' },
  }
  return map[source] || { text: '提取', cls: 'badge-fallback' }
})

// 当前文档对应的 LLM 提取结果（结构化数据，LLM 成功时非空）
// 只要 doc.llmExtraction 旁路数据存在即优先展示（LLM 原始提取结果，最完整），
// 再回退到 doc.* 字段（确定性解析结果）。
// 注意：不依赖 parseSource === 'llm' 判断——LLM 部分成功时 parseSource 可能是
// section_hierarchy，但 llmExtraction 里仍可能有完整数据，此时也应展示。
const llmExtraction = computed<{ label: string; items: unknown[] } | null>(() => {
  const doc = currentDoc.value
  if (!doc) return null
  const llm = doc.llmExtraction as any
  const pick = (llmKey: string, dbArr: unknown[] | undefined) => {
    const llmItems = llm?.[llmKey]
    const items = Array.isArray(llmItems) && llmItems.length ? llmItems : (dbArr || [])
    return items.length ? items : null
  }
  switch (doc.type) {
    case 'business-model': {
      const items = pick('requirements', doc.requirements)
      return items ? { label: '需求', items } : null
    }
    case 'data-model': {
      const items = pick('dataEntities', doc.dataEntities)
      return items ? { label: '实体', items } : null
    }
    case 'interface-protocol': {
      const items = pick('interfaces', doc.interfaces)
      return items ? { label: '接口', items } : null
    }
    case 'architecture-decision': {
      const items = pick('archDecisions', doc.archDecisions)
      return items ? { label: '架构决策', items } : null
    }
    default:
      return null
  }
})

const statusBadge = (status: string) => {
  const map: Record<string, string> = {
    draft: 'badge badge-draft',
    in_review: 'badge badge-review',
    approved: 'badge badge-approved',
    rejected: 'badge badge-rejected',
  }
  return map[status] || 'badge badge-draft'
}

onMounted(async () => {
  const ingestionId =
    (route.params.ingestionId as string) ||
    (route.query.ingestionId as string) ||
    prdStore.ingestionId
  if (ingestionId) {
    await prdStore.fetchResult(ingestionId)
    await docStore.loadDocuments(ingestionId)
  }
})
</script>

<template>
  <div class="review-page">
    <div class="page-section">
      <h1 class="text-xl font-bold">校验面板</h1>
      <p class="text-sm text-slate-500 mt-1">对比解析结果与原始 PRD，逐文档确认</p>
    </div>

    <div v-if="!docStore.documents.length" class="empty-state">
      <div class="icon">📄</div>
      <div class="title">暂无文档</div>
      <div class="desc">请先导入 PRD 文档，解析完成后将在此显示</div>
    </div>

    <div v-else class="grid grid-cols-1 lg:grid-cols-4 gap-6">
      <!-- 左侧：文档列表 -->
      <div class="card lg:col-span-1">
        <div class="card-header">📑 文档列表</div>
        <div class="card-body p-2">
          <div
            v-for="doc in docList"
            :key="doc.docId"
            class="p-3 rounded-lg cursor-pointer mb-1 transition-colors"
            :class="activeTab === doc.type ? 'bg-blue-50 border border-blue-200' : 'hover:bg-slate-50 border border-transparent'"
            @click="switchTab(doc.type)"
          >
            <div class="flex items-center gap-2">
              <span>{{ typeIcon(doc.type) }}</span>
              <span class="text-sm font-medium">{{ typeLabel(doc.type) }}</span>
              <span :class="statusBadge(doc.status)" class="ml-auto">{{ doc.status }}</span>
            </div>
            <div class="text-xs text-slate-500 mt-1">v{{ doc.version }} · {{ new Date(doc.updatedAt).toLocaleDateString() }}</div>
          </div>
        </div>
      </div>

      <!-- 右侧：对比区 -->
      <div class="lg:col-span-3 space-y-4">
        <div v-if="currentDoc">
          <!-- 文档标题栏 -->
          <div class="flex items-center justify-between mb-3">
            <div class="flex flex-wrap items-center gap-2">
              <h3 class="font-semibold">{{ typeIcon(currentDoc.type) }} {{ currentDoc.title }}</h3>
              <span class="badge" :class="parseSourceBadge.cls">{{ parseSourceBadge.text }}</span>
              <span v-if="currentDoc.parseErrorMessage" class="badge badge-error" :title="currentDoc.parseErrorMessage">LLM 失败</span>
              <span :class="statusBadge(currentDoc.status)" class="inline-block">{{ currentDoc.status === 'draft' ? '待审核' : currentDoc.status === 'approved' ? '已通过' : currentDoc.status === 'rejected' ? '已驳回' : '审核中' }}</span>
            </div>
            <div class="flex gap-2">
              <button class="btn btn-ghost btn-sm" @click="openEditor(currentDoc)">✏️ 编辑文档</button>
              <button class="btn btn-outline btn-sm" :disabled="reparsing" @click="handleReparse">
                {{ reparsing ? '⏳ 重新解析中...' : '🔄 重新解析' }}
              </button>
            </div>
          </div>
          <p v-if="currentDoc.parseErrorMessage" class="text-xs text-amber-600 mb-2">
            ⚠️ LLM 解析失败（{{ currentDoc.parseErrorMessage }}），当前为按模板渲染的兜底结果
          </p>

          <!-- 编辑器弹窗 -->
          <div v-if="showEditor && editingDoc" class="card mb-4 border-blue-300">
            <div class="card-header bg-blue-50">
              ✏️ 编辑文档 — {{ editingDoc.title }}
              <button class="ml-auto text-slate-400 hover:text-slate-700" @click="closeEditor">✕</button>
            </div>
            <div class="card-body">
              <div class="mb-3">
                <label class="text-xs font-semibold text-slate-500 uppercase tracking-wider">修改说明</label>
                <input class="input mt-1" v-model="editLog" placeholder="简要描述本次修改..." />
              </div>
              <textarea
                class="input min-h-[360px] font-mono text-sm"
                v-model="editContent"
              ></textarea>
              <div class="flex gap-2 mt-3 justify-end">
                <button class="btn btn-ghost btn-sm" @click="closeEditor">取消</button>
                <button class="btn btn-primary btn-sm" @click="saveEdit">保存修改</button>
              </div>
            </div>
          </div>

          <!-- 校验区：左 = PRD 原文 | 右 = 提取结果/按模板渲染（二选一），总共保持 2 卡片 -->
          <div class="grid grid-cols-1 xl:grid-cols-5 gap-4">
            <!-- 左列：PRD 原文 -->
            <div class="xl:col-span-2">
              <div class="card h-full">
                <div class="card-header">📜 PRD 原文</div>
                <div class="card-body extraction-scroll whitespace-pre-wrap">{{ originalText || '（无对应原文段落）' }}</div>
              </div>
            </div>

            <!-- 右列：提取结果卡（有数据时）；无数据时以按模板渲染文档兜底 -->
            <div class="xl:col-span-3">
              <div v-if="llmExtraction && llmExtraction.items.length" class="card h-full border-indigo-200">
                <div class="card-header bg-indigo-50">🧠 {{ llmExtraction.label }} — {{ parseSourceLabel }}（{{ llmExtraction.items.length }} 条）</div>
                <div class="card-body extraction-scroll">
                  <template v-if="currentDoc.type === 'data-model'">
                    <div v-for="(e, i) in (llmExtraction.items as any[])" :key="i" class="mb-3 pb-3 border-b border-slate-100 last:border-0">
                      <div class="font-semibold text-indigo-700">▣ {{ e.name }}</div>
                      <div v-if="e.description" class="text-sm text-slate-600 mt-0.5">{{ e.description }}</div>
                      <div v-if="e.attributes && e.attributes.length" class="mt-1 text-xs">
                        <span v-for="(a, ai) in e.attributes" :key="ai" class="inline-block bg-slate-100 rounded px-1.5 py-0.5 mr-1 mb-1">{{ a.name }}: {{ a.type }}</span>
                      </div>
                    </div>
                  </template>
                  <template v-else-if="currentDoc.type === 'interface-protocol'">
                    <div v-for="(it, i) in (llmExtraction.items as any[])" :key="i" class="mb-2 pb-2 border-b border-slate-100 last:border-0">
                      <span class="font-mono text-sm font-bold text-indigo-700">{{ it.method }}</span>
                      <span class="font-mono text-sm text-slate-800 ml-1">{{ it.path }}</span>
                      <div v-if="it.summary" class="text-xs text-slate-500 mt-0.5">{{ it.summary }}</div>
                    </div>
                  </template>
                  <template v-else-if="currentDoc.type === 'architecture-decision'">
                    <div v-for="(a, i) in (llmExtraction.items as any[])" :key="i" class="mb-2 pb-2 border-b border-slate-100 last:border-0">
                      <div class="font-semibold text-indigo-700 text-sm">📌 {{ a.title }}</div>
                      <div v-if="a.decision" class="text-xs text-slate-600 mt-0.5">{{ a.decision }}</div>
                    </div>
                  </template>
                  <template v-else>
                    <div v-for="(r, i) in (llmExtraction.items as any[])" :key="i" class="mb-2 pb-2 border-b border-slate-100 last:border-0">
                      <div class="text-sm text-slate-700"><span class="font-mono text-xs text-slate-400 mr-1">{{ r.id }}</span>{{ r.description }}</div>
                    </div>
                  </template>
                </div>
              </div>
              <!-- 无提取结果时，直接以按模板渲染的最终文档兜底展示（替代黄色空态提示） -->
              <div v-else class="card h-full border-slate-200">
                <div class="card-header bg-slate-50">📄 按模板渲染：{{ parseSourceLabel }}</div>
                <div class="card-body extraction-scroll whitespace-pre-wrap">{{ currentDoc.content || '（暂无内容）' }}</div>
              </div>
            </div>
          </div>

          <!-- 审批操作栏 -->
          <div v-if="currentDoc.status !== 'approved'" class="card">
            <div class="card-body flex items-center gap-3">
              <input
                class="input flex-1"
                v-model="comment"
                placeholder="审批意见（可选）..."
              />
              <button
                class="btn btn-success"
                :disabled="approvalLoading"
                @click="handleApproval('approve')"
              >
                {{ approvalLoading ? '处理中...' : '✓ 通过' }}
              </button>
              <button
                class="btn btn-danger"
                :disabled="approvalLoading"
                @click="handleApproval('reject')"
              >
                ✕ 驳回
              </button>
            </div>
          </div>

          <!-- 已通过提示 -->
          <div v-else class="card border-green-200 bg-green-50">
            <div class="card-body text-green-700 text-sm flex items-center gap-2">
              ✅ 该文档已审核通过
            </div>
          </div>
        </div>

        <!-- 全局提示：所有文档已审批 -->
        <div v-if="allApproved" class="card border-blue-300 bg-blue-50">
          <div class="card-body">
            <div class="flex items-center justify-between">
              <div>
                <div class="font-semibold text-blue-700">🎉 所有文档已审核通过</div>
                <div class="text-sm text-blue-600 mt-1">可以生成 change.md 进入流水线</div>
              </div>
              <button
                class="btn btn-primary"
                :disabled="generating"
                @click="generateAndGoPipeline"
              >
                {{ generating ? '⏳ 生成 change.md...' : '➡️ 生成 change.md 并进入流水线' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<style scoped>
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
  max-height: 520px;
  overflow-y: auto;
}
</style>
