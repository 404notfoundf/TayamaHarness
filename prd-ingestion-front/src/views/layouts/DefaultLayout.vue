<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { listProjects } from '@/services/project'
import type { Project } from '@/models/project'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const sidebarCollapsed = ref(false)
const projects = ref<Project[]>([])

const projectId = computed(() => route.params.projectId as string | undefined)

const currentProject = computed(() => {
  if (!projectId.value) return null
  return projects.value.find(p => p.projectId === projectId.value) || null
})

const navItems = computed(() => {
  const pid = projectId.value
  if (!pid) return []
  return [
    { name: 'prd-import', icon: '📄', label: 'PRD 导入', path: `/project/${pid}/prd/import` },
    { name: 'review', icon: '✓', label: '校验面板', path: `/project/${pid}/review` },
    { name: 'kanban', icon: '📋', label: '需求看板', path: `/project/${pid}/kanban` },
    { name: 'templates', icon: '📝', label: '模板管理', path: `/project/${pid}/templates` },
    { name: 'pipeline', icon: '⚙️', label: '流水线', path: `/project/${pid}/pipeline` },
    { name: 'members', icon: '👥', label: '成员管理', path: `/project/${pid}/members` },
  ]
})

const isActive = (name: string) => {
  const current = String(route.name ?? '')
  return current === name || current.startsWith(name)
}

async function loadProjects() {
  try {
    const res = await listProjects()
    projects.value = res?.data || []
  } catch (e) {
    console.error('加载项目列表失败', e)
  }
}

function switchProject(pid: string) {
  router.push(`/project/${pid}/prd/import`)
}

function goBackToProjects() {
  router.push('/projects')
}

function handleLogout() {
  auth.logout()
  router.push('/login')
}

onMounted(loadProjects)
</script>

<template>
  <div class="layout">
    <!-- 侧边栏 -->
    <aside class="sidebar" :class="{ collapsed: sidebarCollapsed }">
      <div class="sidebar-header">
        <div class="logo" @click="goBackToProjects" style="cursor:pointer">
          <span class="logo-icon">⚡</span>
          <span v-if="!sidebarCollapsed" class="logo-text">Harness Flow</span>
        </div>
        <button class="toggle-btn" @click="sidebarCollapsed = !sidebarCollapsed">
          {{ sidebarCollapsed ? '▶' : '◀' }}
        </button>
      </div>

      <!-- 项目选择器 -->
      <div class="project-selector" v-if="!sidebarCollapsed">
        <div class="selector-label">当前项目</div>
        <select
          class="project-select"
          :value="projectId"
          @change="(e) => switchProject((e.target as HTMLSelectElement).value)"
        >
          <option value="" disabled>-- 选择项目 --</option>
          <option v-for="p in projects" :key="p.projectId" :value="p.projectId">
            {{ p.name }}
          </option>
        </select>
        <div class="current-project-info" v-if="currentProject">
          <span class="lang-badge">{{ currentProject.languageName }}</span>
          <span v-if="currentProject.frameworkNames?.length" class="fw-badge">{{ currentProject.frameworkNames.join(', ') }}</span>
        </div>
      </div>

      <!-- 导航 -->
      <nav class="sidebar-nav">
        <a
          v-for="item in navItems"
          :key="item.name"
          class="nav-item"
          :class="{ active: isActive(item.name) }"
          @click="router.push(item.path)"
        >
          <span class="nav-icon">{{ item.icon }}</span>
          <span v-if="!sidebarCollapsed" class="nav-label">{{ item.label }}</span>
        </a>
      </nav>

      <div class="sidebar-footer" v-if="!sidebarCollapsed">
        <a class="back-link" @click="goBackToProjects">← 项目列表</a>
        <div class="version">v0.1.0</div>
      </div>
    </aside>

    <!-- 主内容区 -->
    <div class="main-area">
      <header class="topbar">
        <div class="topbar-left">
          <h2 class="page-title" v-if="route.meta?.title">{{ route.meta.title }}</h2>
          <h2 class="page-title" v-else-if="currentProject">{{ currentProject.name }}</h2>
        </div>
        <div class="topbar-right">
          <div class="user-info">
            <span class="user-avatar">👤</span>
            <span class="user-name">{{ auth.displayName }}</span>
          </div>
          <button class="logout-btn" @click="handleLogout" title="退出登录">🚪</button>
        </div>
      </header>
      <main class="content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<style scoped>
.layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

/* ---- 侧边栏 ---- */
.sidebar {
  width: 220px;
  background: #1e293b;
  color: #cbd5e1;
  display: flex;
  flex-direction: column;
  transition: width 0.2s ease;
  flex-shrink: 0;
}
.sidebar.collapsed {
  width: 60px;
}
.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 14px;
  border-bottom: 1px solid #334155;
}
.logo {
  display: flex;
  align-items: center;
  gap: 8px;
}
.logo-icon {
  font-size: 22px;
}
.logo-text {
  font-size: 15px;
  font-weight: 700;
  color: #f1f5f9;
  white-space: nowrap;
}
.toggle-btn {
  background: none;
  border: none;
  color: #64748b;
  cursor: pointer;
  font-size: 12px;
  padding: 4px;
}
.toggle-btn:hover {
  color: #f1f5f9;
}

/* ---- 项目选择器 ---- */
.project-selector {
  padding: 12px 14px;
  border-bottom: 1px solid #334155;
}
.selector-label {
  font-size: 11px;
  color: #64748b;
  margin-bottom: 6px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
.project-select {
  width: 100%;
  padding: 6px 8px;
  border: 1px solid #475569;
  border-radius: 6px;
  background: #334155;
  color: #e2e8f0;
  font-size: 13px;
  outline: none;
  cursor: pointer;
}
.project-select:focus {
  border-color: #3b82f6;
}
.current-project-info {
  display: flex;
  gap: 4px;
  margin-top: 6px;
}
.lang-badge, .fw-badge {
  font-size: 11px;
  padding: 1px 6px;
  border-radius: 4px;
  background: #374151;
  color: #94a3b8;
}

/* ---- 导航 ---- */
.sidebar-nav {
  flex: 1;
  padding: 12px 8px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 13px;
  color: #94a3b8;
  text-decoration: none;
  transition: all 0.12s;
  white-space: nowrap;
}
.nav-item:hover {
  background: #334155;
  color: #e2e8f0;
}
.nav-item.active {
  background: #2563eb;
  color: #fff;
}
.nav-icon {
  font-size: 16px;
  width: 24px;
  text-align: center;
  flex-shrink: 0;
}
.nav-label {
  font-size: 13px;
  font-weight: 500;
}

/* ---- 底部 ---- */
.sidebar-footer {
  padding: 12px 16px;
  border-top: 1px solid #334155;
}
.back-link {
  display: block;
  font-size: 12px;
  color: #64748b;
  cursor: pointer;
  margin-bottom: 4px;
}
.back-link:hover {
  color: #e2e8f0;
}
.version {
  font-size: 11px;
  color: #475569;
}

/* ---- 主内容区 ---- */
.main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-width: 0;
}
.topbar {
  height: 52px;
  background: #fff;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  flex-shrink: 0;
}
.page-title {
  font-size: 16px;
  font-weight: 700;
  color: #1e293b;
}
.topbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  background: #f8fafc;
  border-radius: 8px;
}
.user-avatar {
  font-size: 16px;
}
.user-name {
  font-size: 13px;
  font-weight: 500;
  color: #374151;
}
.logout-btn {
  background: none;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 4px 8px;
  cursor: pointer;
  font-size: 16px;
  transition: all 0.12s;
}
.logout-btn:hover {
  background: #fef2f2;
  border-color: #fca5a5;
}
.content {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}
</style>