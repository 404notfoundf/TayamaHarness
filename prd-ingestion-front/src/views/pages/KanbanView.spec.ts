import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import KanbanView from './KanbanView.vue'

// ---- 复现场景 ----
// 从流水线详情页「📑 关联文档 → 查看看板（定位当前变更）」跳转而来，
// URL 携带 ?focus=C-001：应高亮定位 C-001 卡片，并可通过「✕ 取消定位」清除。
const routerPush = vi.fn()
const routerReplace = vi.fn()
let focusQuery: Record<string, string> = { focus: 'C-001' }

vi.mock('vue-router', () => ({
  useRoute: () => ({ params: { projectId: 'p1' }, query: focusQuery }),
  useRouter: () => ({ push: routerPush, replace: routerReplace }),
}))

vi.mock('@/stores/pipeline', () => ({
  usePipelineStore: () => ({
    pipelineStatus: null,
    stats: {},
    loading: false,
    error: '',
    loadKanbanStats: vi.fn(),
  }),
}))

const changeSummary = (changeId: string, title: string, status: string) => ({
  changeId,
  title,
  status,
  requirementIds: ['REQ-001'],
  documentRefs: [],
  createdAt: '2026-08-20T10:00:00Z',
  updatedAt: '2026-08-20T10:00:00Z',
})

vi.mock('@/stores/change', () => ({
  useChangeStore: () => ({
    changes: [
      changeSummary('C-001', '用户登录模块实现', 'drafting'),
      changeSummary('C-002', '订单模块实现', 'reviewing'),
    ],
    currentChange: null,
    loading: false,
    error: '',
    createChange: vi.fn(),
    loadChanges: vi.fn(),
    fetchChange: vi.fn(),
    updateChange: vi.fn(),
  }),
}))

vi.mock('@/services/pipeline', () => ({
  getKanbanStats: vi.fn().mockResolvedValue({
    prd_imported: { count: 1, label: 'PRD 导入' },
    document_review: { count: 1, label: '文档校验' },
    design_review: { count: 0, label: '设计评审' },
    completed: { count: 0, label: '已完成' },
  }),
}))

beforeEach(() => {
  routerPush.mockClear()
  routerReplace.mockClear()
  focusQuery = { focus: 'C-001' }
})

describe('KanbanView — ?focus 定位高亮', () => {
  it('带 focus 参数时，对应变更卡片应高亮并显示定位提示', async () => {
    const wrapper = mount(KanbanView)
    await flushPromises()

    const focusedCard = wrapper.find('[data-change-id="C-001"]')
    const otherCard = wrapper.find('[data-change-id="C-002"]')

    expect(focusedCard.exists()).toBe(true)
    expect(focusedCard.classes()).toContain('kanban-card-focus')
    expect(otherCard.classes()).not.toContain('kanban-card-focus')

    // 定位卡片带有「📍 当前变更」角标，其他卡片不带
    expect(focusedCard.text()).toContain('📍 当前变更')
    expect(otherCard.text()).not.toContain('📍 当前变更')

    // 定位提示条可见
    expect(wrapper.text()).toContain('已定位到变更')
    expect(wrapper.text()).toContain('C-001')
  })

  it('点击「✕ 取消定位」应清除 query 中的 focus 参数', async () => {
    const wrapper = mount(KanbanView)
    await flushPromises()

    const clearBtn = wrapper.findAll('button').find((b) => b.text().includes('取消定位'))
    expect(clearBtn).toBeTruthy()
    await clearBtn!.trigger('click')

    expect(routerReplace).toHaveBeenCalledWith({
      path: '/project/p1/kanban',
    })
  })

  it('不带 focus 参数时，不应高亮任何卡片、不显示定位提示', async () => {
    focusQuery = {}
    const wrapper = mount(KanbanView)
    await flushPromises()

    expect(wrapper.find('[data-change-id="C-001"]').classes()).not.toContain('kanban-card-focus')
    expect(wrapper.find('[data-change-id="C-002"]').classes()).not.toContain('kanban-card-focus')
    expect(wrapper.text()).not.toContain('已定位到变更')
  })
})