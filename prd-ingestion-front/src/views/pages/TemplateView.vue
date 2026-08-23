<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useTemplateStore } from '@/stores/template'
import type { Template } from '@/models/template'
import { MOCK_TEMPLATES } from '@/constants/mock'

const templateStore = useTemplateStore()

const activeTemplateId = ref<string | null>(null)
const editContent = ref('')
const editLog = ref('')
const showEditor = ref(false)
const showPreview = ref(false)
const showNewForm = ref(false)
const newTemplateName = ref('')
const newTemplateType = ref<string>('business-model')
const newTemplateContent = ref('')
const newTemplateDescription = ref('')
const creating = ref(false)

const templates = computed(() => templateStore.templates)

const activeTemplate = computed(() => {
  if (!activeTemplateId.value) return null
  return templateStore.templates.find((t) => t.templateId === activeTemplateId.value) || null
})

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

const selectTemplate = (tpl: Template) => {
  activeTemplateId.value = tpl.templateId
  showEditor.value = false
  showPreview.value = false
  templateStore.fetchTemplate(tpl.templateId)
}

const openEditor = () => {
  if (!activeTemplate.value) return
  editContent.value = activeTemplate.value.content
  editLog.value = ''
  showEditor.value = true
  showPreview.value = false
}

const closeEditor = () => {
  showEditor.value = false
  editContent.value = ''
  editLog.value = ''
}

const saveTemplate = async () => {
  if (!activeTemplate.value) return
  await templateStore.updateTemplate(activeTemplate.value.templateId, {
    content: editContent.value,
    changeLog: editLog.value,
  })
  closeEditor()
}

const togglePreview = () => {
  showPreview.value = !showPreview.value
  if (showPreview.value) {
    showEditor.value = false
  }
}

const openNewForm = () => {
  showNewForm.value = true
  newTemplateName.value = ''
  newTemplateType.value = 'business-model'
  newTemplateContent.value = ''
  newTemplateDescription.value = ''
}

const createNewTemplate = async () => {
  if (!newTemplateName.value.trim()) return
  creating.value = true
  try {
    await templateStore.createTemplate({
      type: newTemplateType.value as Template['type'],
      name: newTemplateName.value,
      description: newTemplateDescription.value,
      content: newTemplateContent.value,
    })
    showNewForm.value = false
  } finally {
    creating.value = false
  }
}

onMounted(() => {
  templateStore.loadTemplates()
})
</script>

<template>
  <div class="template-page">
    <div class="page-section flex items-center justify-between">
      <div>
        <h1 class="text-xl font-bold">模板管理</h1>
        <p class="text-sm text-slate-500 mt-1">管理 LLM 解析输出的模板格式</p>
      </div>
      <button class="btn btn-primary" @click="openNewForm">+ 新建模板</button>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <!-- 左侧：模板列表 -->
      <div class="card lg:col-span-1">
        <div class="card-header">📑 模板列表</div>
        <div class="card-body p-2">
          <div
            v-for="tpl in templates"
            :key="tpl.templateId"
            class="p-3 rounded-lg cursor-pointer mb-1 transition-colors"
            :class="activeTemplateId === tpl.templateId ? 'bg-blue-50 border border-blue-200' : 'hover:bg-slate-50 border border-transparent'"
            @click="selectTemplate(tpl)"
          >
            <div class="flex items-center gap-2">
              <span>{{ typeIcon(tpl.type) }}</span>
              <span class="text-sm font-medium">{{ tpl.name }}</span>
            </div>
            <div class="text-xs text-slate-500 mt-1">v{{ tpl.version }} · {{ tpl.updatedBy }}</div>
          </div>
        </div>
      </div>

      <!-- 右侧：编辑器 -->
      <div class="lg:col-span-2">
        <div v-if="!activeTemplate" class="empty-state">
          <div class="icon">📝</div>
          <div class="title">选择模板</div>
          <div class="desc">从左侧选择一个模板开始编辑</div>
        </div>

        <div v-else class="space-y-4">
          <!-- 模板信息 -->
          <div class="card">
            <div class="card-header">
              <span>{{ typeIcon(activeTemplate.type) }} {{ activeTemplate.name }}</span>
              <span class="ml-auto text-xs text-slate-400">v{{ activeTemplate.version }} · 最后修改: {{ new Date(activeTemplate.updatedAt).toLocaleString() }}</span>
            </div>
            <div class="card-body">
              <div class="text-sm text-slate-600">{{ activeTemplate.description }}</div>
              <div class="flex gap-2 mt-3">
                <button class="btn btn-primary btn-sm" @click="openEditor">✏️ 编辑</button>
                <button class="btn btn-ghost btn-sm" @click="togglePreview">
                  {{ showPreview ? '关闭预览' : '👁️ 预览' }}
                </button>
              </div>
            </div>
          </div>

          <!-- 预览视图 -->
          <div v-if="showPreview" class="card">
            <div class="card-header">👁️ 预览</div>
            <div class="card-body">
              <pre class="text-sm text-slate-700 whitespace-pre-wrap font-mono bg-slate-50 p-4 rounded-lg">{{ activeTemplate.content }}</pre>
            </div>
          </div>

          <!-- 编辑器视图 -->
          <div v-if="showEditor" class="card border-blue-300">
            <div class="card-header bg-blue-50">
              ✏️ 编辑模板 — {{ activeTemplate.name }}
              <button class="ml-auto text-slate-400 hover:text-slate-700" @click="closeEditor">✕</button>
            </div>
            <div class="card-body">
              <div class="mb-3">
                <label class="text-xs font-semibold text-slate-500 uppercase tracking-wider">修改说明</label>
                <input class="input mt-1" v-model="editLog" placeholder="简要描述本次修改..." />
              </div>
              <textarea
                class="input min-h-[400px] font-mono text-sm"
                v-model="editContent"
              ></textarea>
              <div class="flex gap-2 mt-3 justify-end">
                <button class="btn btn-ghost btn-sm" @click="closeEditor">取消</button>
                <button class="btn btn-primary btn-sm" @click="saveTemplate">保存模板</button>
              </div>
            </div>
          </div>

          <!-- 模板内容（只读） -->
          <div v-if="!showEditor && !showPreview" class="card">
            <div class="card-header">📄 模板内容</div>
            <div class="card-body">
              <pre class="text-sm text-slate-700 whitespace-pre-wrap font-mono bg-slate-50 p-4 rounded-lg max-h-[480px] overflow-y-auto">{{ activeTemplate.content }}</pre>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 新建模板弹窗 -->
    <Transition name="modal-fade">
      <div
        v-if="showNewForm"
        class="fixed inset-0 z-50 flex items-center justify-center bg-black/30"
        @click.self="showNewForm = false"
        @keydown.esc="showNewForm = false"
      >
        <div class="modal-panel w-[560px] max-w-[calc(100vw-32px)] rounded-xl bg-white shadow-xl">
          <!-- 头部 -->
          <div class="flex items-center justify-between modal-header border-b border-slate-200">
            <h3 class="text-[15px] font-semibold">+ 新建模板</h3>
            <button
              class="text-slate-400 hover:text-slate-700 text-lg leading-none px-1"
              @click="showNewForm = false"
              aria-label="关闭"
            >✕</button>
          </div>

          <!-- 表单主体 -->
          <div class="modal-body max-h-[70vh] overflow-y-auto">
            <!-- 模板名称 -->
            <div>
              <label class="text-xs font-semibold text-slate-500 uppercase tracking-wider">模板名称 <span class="text-red-500">*</span></label>
              <input class="input mt-1" v-model="newTemplateName" placeholder="如：业务模型模板" @keydown.enter="createNewTemplate" />
            </div>

            <!-- 文档类型 -->
            <div>
              <label class="text-xs font-semibold text-slate-500 uppercase tracking-wider">文档类型 <span class="text-red-500">*</span></label>
              <select class="input mt-1 cursor-pointer" v-model="newTemplateType">
                <option value="business-model">业务模型</option>
                <option value="data-model">数据模型</option>
                <option value="interface-protocol">接口协议</option>
                <option value="architecture-decision">架构决策</option>
                <option value="change-model">change开发清单</option>
              </select>
            </div>

            <!-- 描述 -->
            <div>
              <label class="text-xs font-semibold text-slate-500 uppercase tracking-wider">描述</label>
              <input class="input mt-1" v-model="newTemplateDescription" placeholder="简要描述模板用途" />
            </div>

            <!-- 模板内容 -->
            <div>
              <div class="flex items-center justify-between">
                <label class="text-xs font-semibold text-slate-500 uppercase tracking-wider">模板内容 <span class="text-red-500">*</span></label>
                <span v-pre class="text-xs text-slate-400">支持 {{VAR}} 变量占位符</span>
              </div>
              <textarea
                class="input mt-1 min-h-[180px] font-mono text-sm leading-relaxed"
                v-model="newTemplateContent"
                :placeholder="`# {{MODEL_NAME}}\n\n## 需求列表\n- {{REQ_DESC}}\n\n## 验收标准\n- [ ] {{AC_CRITERIA}}`"
              ></textarea>
            </div>
          </div>

          <!-- 底部操作 -->
          <div class="flex gap-2 justify-end modal-footer border-t border-slate-200">
            <button class="btn btn-ghost" @click="showNewForm = false">取消</button>
            <button
              class="btn btn-primary min-w-[72px]"
              :disabled="!newTemplateName.trim() || creating"
              @click="createNewTemplate"
            >
              <span v-if="creating" class="spinner spinner-sm"></span>
              {{ creating ? '创建中...' : '创建' }}
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
/* ---- 新建模板弹窗动画（仅淡入淡出，保持轻量） ---- */
.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.15s ease;
}
.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}

/* ---- 弹窗内边距（scoped CSS，不依赖 Tailwind 扫描） ---- */
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

/* 小号 spinner（配合创建按钮） */
.spinner-sm {
  width: 14px;
  height: 14px;
  border-width: 2px;
}
</style>
