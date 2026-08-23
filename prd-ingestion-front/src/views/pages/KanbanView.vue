<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { usePipelineStore } from '@/stores/pipeline'
import { useChangeStore } from '@/stores/change'
import { MOCK_KANBAN_STATS } from '@/constants/mock'
import * as pipelineApi from '@/services/pipeline'
import type { ChangeSummary } from '@/models/change'

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

// 状态映射：流水线状态 → 看板列
const statusToColumn: Record<string, string> = {
  drafting: 'prd_imported',
  reviewing: 'document_review',
  approved: 'design_review',
  completed: 'completed',
}

// 看板列定义
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
  const map: Record<string, string> = { drafting: '草案', reviewing: '评审中', approved: '已批准', completed: '已完成' }
  return map[status] || status
}

const openPipeline = (changeId: string) => {
  router.push(`/project/${projectId.value}/pipeline/${changeId}`)
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
          v-for="item in col.items"
          :key="item.changeId"
          class="kanban-card"
          :class="isFocused(item.changeId) ? 'kanban-card-focus' : ''"
          :data-change-id="item.changeId"
          @click="openPipeline(item.changeId)"
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
      </div>
    </div>
  </div>
</template>