import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import PipelineView from './PipelineView.vue'
import type { WikiDocument } from '@/models/document'

// ---- 复现场景 ----
// 流水线详情页底部「📑 关联文档」：点击 4 个技术文档行 → 弹窗展示对应文档内容；
// 「查看看板（定位当前变更）→」→ 带 query.focus 跳转看板。
const routerPush = vi.fn()

vi.mock('vue-router', () => ({
  useRoute: () => ({ params: { projectId: 'p1', changeId: 'C-001' } }),
  useRouter: () => ({ push: routerPush }),
}))

vi.mock('@/stores/pipeline', () => ({
  usePipelineStore: () => ({
    pipelineStatus: null,
    changeLogs: [],
    loading: false,
    error: '',
    fetchPipelineStatus: vi.fn(),
    fetchChangeLogs: vi.fn(),
    advancePipeline: vi.fn(),
  }),
}))

const changeDetailFixture = {
  changeId: 'C-001',
  title: '用户登录模块实现',
  status: 'drafting',
  content: '## Change 内容',
  requirementIds: ['REQ-001'],
  documentRefs: [
    { type: 'business-model', docId: 'doc-bm-001', title: '业务模型', status: 'approved' },
    { type: 'data-model', docId: 'doc-dm-001', title: '数据模型', status: 'draft' },
    { type: 'interface-protocol', docId: 'doc-ip-001', title: '接口协议', status: 'draft' },
    { type: 'architecture-decision', docId: 'doc-ad-001', title: '架构决策', status: 'draft' },
  ],
  createdAt: '2026-08-20T10:00:00Z',
  updatedAt: '2026-08-20T10:00:00Z',
}

vi.mock('@/stores/change', () => ({
  useChangeStore: () => ({
    changes: [],
    currentChange: changeDetailFixture,
    loading: false,
    error: '',
    createChange: vi.fn(),
    loadChanges: vi.fn(),
    fetchChange: vi.fn(),
    updateChange: vi.fn(),
  }),
}))

// 文档 store mock：通过 hoisted 容器在用例内控制 currentDoc 与 fetchDocument 行为
const docHolder = vi.hoisted(() => ({
  doc: null as WikiDocument | null,
  fetchImpl: null as null | ((docId: string) => void),
  fetchDocument: vi.fn(async (docId: string) => {
    docHolder.fetchImpl?.(docId)
  }),
}))

vi.mock('@/stores/document', () => ({
  useDocumentStore: () => ({
    get currentDoc() {
      return docHolder.doc
    },
    documents: [],
    summaries: [],
    loading: false,
    error: '',
    loadDocuments: vi.fn(),
    fetchDocument: docHolder.fetchDocument,
    updateDocument: vi.fn(),
    approveDocument: vi.fn(),
  }),
}))

beforeEach(() => {
  routerPush.mockClear()
  docHolder.doc = null
  docHolder.fetchImpl = (docId) => {
    docHolder.doc = {
      docId,
      type: 'business-model',
      title: '业务模型 - 用户登录模块',
      status: 'approved',
      version: 3,
      content: '# 业务模型内容\n\n## 用户故事\n- 用户可以通过手机号密码登录',
      originalPrdContent: '## 功能需求',
      changeLog: '',
      createdBy: 'LLM',
      updatedAt: '2026-08-20T12:00:00Z',
      ingestionId: 'i1',
    }
  }
})

const mountView = async () => {
  const wrapper = mount(PipelineView)
  await flushPromises()
  return wrapper
}

describe('PipelineView — 📑 关联文档', () => {
  it('点击技术文档行应弹窗展示对应文档内容', async () => {
    const wrapper = await mountView()

    // 找到「业务模型」文档行并点击
    const bmRow = wrapper
      .findAll('div[class*="cursor-pointer"]')
      .find((r) => r.text().includes('业务模型'))
    expect(bmRow).toBeTruthy()
    await bmRow!.trigger('click')
    await flushPromises()

    // 弹窗出现，展示文档标题、元信息与 Markdown 原文
    expect(wrapper.text()).toContain('业务模型 - 用户登录模块')
    expect(wrapper.text()).toContain('v3')
    expect(wrapper.text()).toContain('# 业务模型内容')
    expect(wrapper.text()).toContain('用户可以通过手机号密码登录')
    // 弹窗内不应再展示「查看看板」按钮（点击行不应跳转看板）
    expect(routerPush).not.toHaveBeenCalled()
  })

  it('点击「查看看板（定位当前变更）」应带 focus 参数跳转看板', async () => {
    const wrapper = await mountView()

    const btn = wrapper
      .findAll('button')
      .find((b) => b.text().includes('查看看板（定位当前变更）'))
    expect(btn).toBeTruthy()
    await btn!.trigger('click')

    expect(routerPush).toHaveBeenCalledWith({
      path: '/project/p1/kanban',
      query: { focus: 'C-001' },
    })
  })

  it('点击弹窗遮罩可关闭弹窗', async () => {
    const wrapper = await mountView()

    const bmRow = wrapper
      .findAll('div[class*="cursor-pointer"]')
      .find((r) => r.text().includes('业务模型'))
    await bmRow!.trigger('click')
    await flushPromises()
    expect(wrapper.text()).toContain('# 业务模型内容')

    // 点击遮罩（@click.self）
    const overlay = wrapper.find('div.fixed.inset-0')
    expect(overlay.exists()).toBe(true)
    await overlay.trigger('click')
    await flushPromises()
    expect(wrapper.text()).not.toContain('# 业务模型内容')
  })
})