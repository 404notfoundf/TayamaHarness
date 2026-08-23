import { createRouter, createWebHistory } from 'vue-router'
import type { RouteLocationNormalized } from 'vue-router'
import DefaultLayout from '@/views/layouts/DefaultLayout.vue'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/projects',
    },
    // 登录页
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/pages/LoginView.vue'),
    },
    // 项目列表页（未选择项目时的默认页）
    {
      path: '/projects',
      name: 'projects',
      meta: { requiresAuth: true },
      component: () => import('@/views/pages/ProjectListView.vue'),
    },
    // 全局用户管理（管理员）
    {
      path: '/users',
      name: 'users',
      meta: { requiresAuth: true },
      component: () => import('@/views/pages/UserManagementView.vue'),
    },
    // 项目上下文下的所有页面
    {
      path: '/project/:projectId',
      component: DefaultLayout,
      meta: { requiresAuth: true },
      children: [
        { path: '', redirect: (to: { params: Record<string, string | string[] | undefined> }) => ({ path: `/project/${to.params.projectId}/prd/import` }) },
        // 页面一：PRD 导入页
        {
          path: 'prd/import',
          name: 'prd-import',
          component: () => import('@/views/pages/PrdImportView.vue'),
        },
        // 页面二：校验面板
        {
          path: 'review',
          name: 'review',
          component: () => import('@/views/pages/ReviewView.vue'),
        },
        // 页面三：需求看板
        {
          path: 'kanban',
          name: 'kanban',
          component: () => import('@/views/pages/KanbanView.vue'),
        },
        // 页面四：模板管理
        {
          path: 'templates',
          name: 'templates',
          component: () => import('@/views/pages/TemplateView.vue'),
        },
        // 页面五：流水线详情
        {
          path: 'pipeline/:changeId?',
          name: 'pipeline',
          component: () => import('@/views/pages/PipelineView.vue'),
        },
        // 页面六：成员管理
        {
          path: 'members',
          name: 'members',
          component: () => import('@/views/pages/MemberView.vue'),
        },
      ],
    },
  ],
})

// 路由守卫：未登录跳转到登录页
router.beforeEach((to: RouteLocationNormalized, _from: RouteLocationNormalized, next: (arg?: unknown) => void) => {
  if (to.meta.requiresAuth) {
    const auth = useAuthStore()
    if (!auth.isLoggedIn) {
      next({ name: 'login', query: { redirect: to.fullPath } })
      return
    }
  }
  // 已登录用户访问登录页则跳转回项目列表
  if (to.name === 'login') {
    const auth = useAuthStore()
    if (auth.isLoggedIn) {
      next({ name: 'projects' })
      return
    }
  }
  next()
})

export default router