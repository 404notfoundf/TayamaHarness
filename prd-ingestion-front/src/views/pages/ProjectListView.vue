<template>
  <div class="project-list-page">
    <header class="page-header">
      <h1>项目管理</h1>
      <div class="header-actions">
        <button class="btn btn-secondary" @click="router.push('/users')">👥 团队用户管理</button>
        <button class="btn btn-primary" @click="onOpenCreate">+ 新建项目</button>
        <span class="user-info">
          <span class="user-name">{{ auth.displayName }}</span>
          <button class="btn btn-ghost logout-btn" @click="handleLogout" title="退出登录">🚪 退出</button>
        </span>
      </div>
    </header>

    <!-- 项目列表 -->
    <div class="project-grid" v-if="projects.length > 0">
      <div
        class="project-card"
        v-for="p in projects"
        :key="p.projectId"
        @click="enterProject(p.projectId)"
      >
        <div class="card-header">
          <span class="project-name">{{ p.name }}</span>
          <span class="badge" :class="p.languageName?.toLowerCase()">{{ p.languageName }}</span>
        </div>
        <div class="card-body">
          <p v-if="p.description" class="desc">{{ p.description }}</p>
          <p v-else class="desc muted">暂无描述</p>
          <div class="meta">
            <span class="meta-item" v-if="p.frameworkNames?.length">框架: {{ p.frameworkNames.join(', ') }}</span>
            <span class="meta-item">创建: {{ formatDate(p.createdAt) }}</span>
          </div>
        </div>
      </div>
    </div>

    <div v-else-if="!loading" class="empty-state">
      <div class="empty-icon">📁</div>
      <h3>还没有项目</h3>
      <p>创建第一个项目，开始 PRD 前置转换之旅</p>
      <button class="btn btn-primary" @click="onOpenCreate">新建项目</button>
    </div>

    <div v-if="loading" class="loading">加载中...</div>

    <!-- 创建项目弹窗 -->
    <div class="modal-overlay" v-if="showCreate" @click.self="showCreate = false">
      <div class="modal">
        <h3>新建项目</h3>
        <div class="form-group">
          <label>项目名称 *</label>
          <input v-model="form.name" class="input" placeholder="输入项目名称" />
        </div>
        <div class="form-group">
          <label>项目描述</label>
          <textarea v-model="form.description" class="input" rows="2" placeholder="可选描述"></textarea>
        </div>
        <div class="form-group">
          <label>编程语言 *</label>
          <select v-model.number="form.languageId" class="input" @change="onLanguageChange">
            <option :value="0">-- 请选择 --</option>
            <option v-for="l in languages" :key="l.id" :value="l.id">{{ l.name }}</option>
          </select>
        </div>
        <div class="form-group" v-if="form.languageId && frameworks.length > 0">
          <label>框架（可多选）</label>
          <div class="checkbox-group">
            <label class="checkbox-item" v-for="f in frameworks" :key="f.id">
              <input
                type="checkbox"
                :value="f.id"
                v-model="form.frameworkIds"
              />
              <span>{{ f.name }}</span>
            </label>
          </div>
        </div>
        <div class="form-group" v-if="users.length > 0">
          <label>项目成员（可选，默认为创建者）</label>
          <label class="checkbox-item select-all">
            <input
              type="checkbox"
              :checked="allSelected"
              @change="toggleSelectAll"
            />
            <span class="select-all-label">全选（{{ users.length }} 人）</span>
          </label>
          <div class="checkbox-group">
            <label class="checkbox-item" v-for="u in users" :key="u.userId">
              <input
                type="checkbox"
                :value="u.userId"
                v-model="form.memberIds"
              />
              <span>{{ u.displayName }} (@{{ u.username }})</span>
            </label>
          </div>
        </div>
        <div class="form-actions">
          <button class="btn btn-ghost" @click="showCreate = false">取消</button>
          <button class="btn btn-primary" :disabled="!form.name || !form.languageId" @click="onCreate">
            创建
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { listProjects, createProject } from '@/services/project'
import type { Project } from '@/models/project'
import type { ProjectRequest } from '@/models/project'
import { listLanguages, listFrameworks } from '@/services/language'
import type { Language, Framework } from '@/models/project'
import { listAllUsers } from '@/services/member'
import type { User } from '@/models/auth'

const router = useRouter()
const auth = useAuthStore()
const projects = ref<Project[]>([])
const languages = ref<Language[]>([])
const frameworks = ref<Framework[]>([])
const users = ref<User[]>([])
const loading = ref(false)
const showCreate = ref(false)

const allSelected = computed(() =>
  users.value.length > 0 && (form.value.memberIds?.length ?? 0) === users.value.length
)

function toggleSelectAll() {
  if (allSelected.value) {
    form.value.memberIds = []
  } else {
    form.value.memberIds = users.value.map(u => u.userId)
  }
}

const form = ref<ProjectRequest>({
  name: '',
  description: '',
  languageId: 0,
  frameworkIds: [],
  memberIds: [],
})

async function loadProjects() {
  loading.value = true
  try {
    const res = await listProjects()
    projects.value = res?.data || []
  } catch (e) {
    console.error('加载项目列表失败', e)
  } finally {
    loading.value = false
  }
}

async function loadLanguages() {
  try {
    const res = await listLanguages()
    languages.value = res?.data || []
  } catch (e) {
    console.error('加载语言列表失败', e)
  }
}

function onLanguageChange() {
  form.value.frameworkIds = []
  if (form.value.languageId) {
    listFrameworks(form.value.languageId).then(res => {
      frameworks.value = res?.data || []
    })
  } else {
    frameworks.value = []
  }
}

async function onCreate() {
  if (!form.value.name || !form.value.languageId) return
  try {
    await createProject(form.value)
    showCreate.value = false
    form.value = { name: '', description: '', languageId: 0, frameworkIds: [], memberIds: [] }
    await loadProjects()
  } catch (e) {
    console.error('创建项目失败', e)
  }
}

function onOpenCreate() {
  showCreate.value = true
  // 加载用户列表供选择成员
  listAllUsers().then(res => {
    users.value = res?.data || []
  }).catch(() => {})
}

function enterProject(projectId: string) {
  router.push(`/project/${projectId}/prd/import`)
}

function handleLogout() {
  auth.logout()
  router.push('/login')
}

function formatDate(dateStr: string) {
  if (!dateStr) return ''
  return dateStr.substring(0, 10)
}

onMounted(() => {
  loadProjects()
  loadLanguages()
})
</script>

<style scoped>
.project-list-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px 24px;
}
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}
.header-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}
.user-info {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding-left: 12px;
  border-left: 1px solid #e5e7eb;
  margin-left: 4px;
}
.user-name {
  font-size: 13px;
  color: #6b7280;
}
.logout-btn {
  font-size: 13px;
  color: #ef4444;
  cursor: pointer;
}
.page-header h1 { margin: 0; font-size: 24px; }
.project-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
}
.project-card {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
  transition: box-shadow 0.2s, border-color 0.2s;
}
.project-card:hover {
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  border-color: #3b82f6;
}
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}
.project-name { font-weight: 600; font-size: 16px; }
.badge {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  background: #e5e7eb;
  color: #374151;
}
.badge.java { background: #fef3c7; color: #92400e; }
.badge.python { background: #dbeafe; color: #1e40af; }
.badge.go { background: #d1fae5; color: #065f46; }
.badge.rust { background: #fce7f3; color: #9d174d; }
.card-body .desc { font-size: 14px; color: #6b7280; margin: 0 0 8px; }
.muted { color: #9ca3af; }
.meta { display: flex; gap: 16px; font-size: 12px; color: #9ca3af; }
.empty-state { text-align: center; padding: 80px 20px; }
.empty-icon { font-size: 48px; margin-bottom: 16px; }
.empty-state h3 { margin: 0 0 8px; }
.empty-state p { color: #6b7280; margin: 0 0 16px; }
.loading { text-align: center; padding: 40px; color: #6b7280; }
.modal-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,0.4);
  display: flex; align-items: center; justify-content: center;
  z-index: 100;
}
.modal {
  background: white; border-radius: 12px; padding: 24px;
  width: 480px; max-width: 90vw;
}
.modal h3 { margin: 0 0 16px; }
.form-group { margin-bottom: 12px; }
.form-group label { display: block; font-size: 13px; color: #374151; margin-bottom: 4px; }
.input {
  width: 100%; padding: 8px 12px; border: 1px solid #d1d5db;
  border-radius: 6px; font-size: 14px; box-sizing: border-box;
}
.checkbox-group {
  max-height: 200px;
  overflow-y: auto;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  padding: 8px;
}
.checkbox-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 0;
  font-size: 14px;
  cursor: pointer;
}
.checkbox-item input[type="checkbox"] {
  margin: 0;
}
.select-all {
  border-bottom: 1px solid #e5e7eb;
  padding-bottom: 8px;
  margin-bottom: 8px;
}
.select-all-label { font-weight: 600; color: #374151; }
.form-actions { display: flex; justify-content: flex-end; gap: 8px; margin-top: 16px; }
</style>
