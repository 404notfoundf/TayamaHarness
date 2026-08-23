import { defineStore } from 'pinia'
import { ref } from 'vue'
import { useMock } from '@/config'
import * as api from '@/services/template'
import type { Template, TemplateCreateRequest, TemplateUpdateRequest } from '@/models/template'
import { MOCK_TEMPLATES } from '@/constants/mock'

export const useTemplateStore = defineStore('template', () => {
  const templates = ref<Template[]>([])
  const currentTemplate = ref<Template | null>(null)
  const loading = ref(false)
  const error = ref('')

  // ① 加载模板列表
  const loadTemplates = async () => {
    loading.value = true
    error.value = ''
    if (useMock) {
      templates.value = MOCK_TEMPLATES
      loading.value = false
      return
    }
    try {
      templates.value = await api.getTemplates()
    } catch (e) {
      error.value = (e as Error).message
    } finally {
      loading.value = false
    }
  }

  // ② 获取模板详情
  const fetchTemplate = async (templateId: string) => {
    if (useMock) {
      currentTemplate.value = MOCK_TEMPLATES.find((t) => t.templateId === templateId) || null
      return
    }
    try {
      currentTemplate.value = await api.getTemplate(templateId)
    } catch (e) {
      error.value = (e as Error).message
    }
  }

  // ③ 更新模板
  const updateTemplate = async (templateId: string, payload: TemplateUpdateRequest) => {
    error.value = ''
    if (useMock) {
      const idx = templates.value.findIndex((t) => t.templateId === templateId)
      if (idx >= 0) {
        const tpl = templates.value[idx]!
        templates.value[idx] = { ...tpl, ...payload, version: tpl.version + 1 }
      }
      if (currentTemplate.value) {
        currentTemplate.value = { ...currentTemplate.value, ...payload, version: currentTemplate.value.version + 1 }
      }
      return
    }
    try {
      const updated = await api.updateTemplate(templateId, payload)
      const idx = templates.value.findIndex((t) => t.templateId === templateId)
      if (idx >= 0) templates.value[idx] = updated
      currentTemplate.value = updated
    } catch (e) {
      error.value = (e as Error).message
    }
  }

  // ④ 新建模板
  const createTemplate = async (payload: TemplateCreateRequest) => {
    error.value = ''
    if (useMock) {
      const newTpl: Template = {
        templateId: `tpl-${Date.now()}`,
        ...payload,
        version: 1,
        versions: [],
        updatedAt: new Date().toISOString(),
        updatedBy: '当前用户',
      }
      templates.value.push(newTpl)
      currentTemplate.value = newTpl
      return
    }
    try {
      const created = await api.createTemplate(payload)
      templates.value.push(created)
      currentTemplate.value = created
    } catch (e) {
      error.value = (e as Error).message
    }
  }

  return {
    templates,
    currentTemplate,
    loading,
    error,
    loadTemplates,
    fetchTemplate,
    updateTemplate,
    createTemplate,
  }
})