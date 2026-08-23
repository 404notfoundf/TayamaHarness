import { describe, it, expect, vi, beforeEach } from 'vitest'
import { ref } from 'vue'
import { mount, flushPromises } from '@vue/test-utils'
import ReviewView from './ReviewView.vue'
import type { WikiDocument } from '@/models/document'

// ---- 复现场景 ----
// 用户通过侧边栏「校验面板」菜单（URL 不带 ingestionId）进入页面，
// 且 prdStore 内存态丢失（刷新 / 重新解析后 ingest 失败），
// 但 docStore 残留上一轮解析的已通过文档 → 按钮可用，点击后 ingestionId 为空 → alert。
const routerPush = vi.fn()

vi.mock('vue-router', () => ({
  useRoute: () => ({ name: 'review', params: { projectId: 'p1', ingestionId: '' }, query: {} }),
  useRouter: () => ({ push: routerPush }),
}))

vi.mock('@/stores/prd', () => ({
  usePrdStore: () => ({
    ingestionId: '',
    title: '',
    status: 'completed',
    progress: 100,
    result: null,
    loading: false,
    error: '',
    ingest: vi.fn(),
    fetchProgress: vi.fn(),
    fetchResult: vi.fn(),
    reset: vi.fn(),
  }),
}))

const approvedDoc = (docId: string, type: string, ingestionId: string): WikiDocument => ({
  docId,
  type: type as WikiDocument['type'],
  title: `${type} 文档`,
  status: 'approved',
  version: 1,
  content: '## 内容',
  originalPrdContent: '## 原文',
  changeLog: '',
  createdBy: 'LLM',
  updatedAt: '2026-08-20T10:00:00Z',
  ingestionId,
})

vi.mock('@/stores/document', () => ({
  useDocumentStore: () => ({
    documents: [approvedDoc('doc-bm-001', 'business-model', ''), approvedDoc('doc-dm-001', 'data-model', '')],
    summaries: [],
    currentDoc: null,
    loading: false,
    error: '',
    loadDocuments: vi.fn(),
    fetchDocument: vi.fn(),
    updateDocument: vi.fn(),
    approveDocument: vi.fn(),
  }),
}))

vi.mock('@/stores/change', () => ({
  useChangeStore: () => ({
    changes: [],
    currentChange: null,
    loading: false,
    error: '',
    createChange: vi.fn(),
    loadChanges: vi.fn(),
    fetchChange: vi.fn(),
    updateChange: vi.fn(),
  }),
}))

describe('ReviewView — 缺少 ingestionId 时生成 change', () => {
  beforeEach(() => {
    routerPush.mockClear()
  })

  it('点击「生成 change.md 并进入流水线」应报错「缺少 ingestionId」，不能进入流水线', async () => {
    const alertSpy = vi.spyOn(window, 'alert').mockImplementation(() => {})
    const wrapper = mount(ReviewView)
    await flushPromises()

    // 文档列表有残留已通过文档 → 「生成 change」按钮可见
    const generateBtn = wrapper
      .findAll('button')
      .find((b) => b.text().includes('生成 change.md 并进入流水线'))
    expect(generateBtn).toBeTruthy()

    await generateBtn!.trigger('click')
    await flushPromises()

    expect(alertSpy).toHaveBeenCalledWith('缺少 ingestionId，无法生成 change.md')
    expect(routerPush).not.toHaveBeenCalled()

    alertSpy.mockRestore()
  })
})