<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { listProjectMembers, addProjectMember, updateProjectMemberRole, removeProjectMember, listAllUsers } from '@/services/member'
import type { ProjectMember, User } from '@/models/auth'

const route = useRoute()
const auth = useAuthStore()

const projectId = computed(() => route.params.projectId as string)
const members = ref<ProjectMember[]>([])
const allUsers = ref<User[]>([])
const loading = ref(false)
const errorMsg = ref('')
const successMsg = ref('')

// 添加成员弹窗
const showAddDialog = ref(false)
const newUserId = ref('')
const newRole = ref('contributor')
const adding = ref(false)

// 角色选项
const roleOptions = [
  { value: 'owner', label: '所有者', description: '完全控制项目，唯一可删除项目、管理成员角色' },
  { value: 'maintainer', label: '维护者', description: '管理项目内容与设置，审批文档，推进流水线' },
  { value: 'contributor', label: '贡献者', description: '创建和编辑 PRD、变更（Change）等业务内容' },
  { value: 'reader', label: '读者', description: '只读访问项目所有内容' },
]

// 角色详细权限说明（用于添加成员弹窗展示）
const roleDetails: Record<string, { summary: string; can: string[]; cannot: string[] }> = {
  owner: {
    summary: '项目最高权限，建议每个项目仅 1~2 人，通常是项目负责人。',
    can: ['管理项目（编辑、删除）', '管理成员（添加、移除、改角色）', '审批文档、推进流水线、管理模板', '创建/编辑 PRD、变更（Change）', '查看项目所有内容'],
    cannot: ['（无限制）'],
  },
  maintainer: {
    summary: '协助 owner 维护项目日常，可管理内容和成员，但不可删除项目。',
    can: ['管理项目内容（编辑基本信息）', '添加/移除成员（不可修改他人的 owner 角色）', '审批文档、推进流水线、管理模板', '创建/编辑 PRD、变更（Change）', '查看项目所有内容'],
    cannot: ['删除项目', '修改成员角色（仅 owner 可操作）'],
  },
  contributor: {
    summary: '一线业务人员，负责产出和迭代 PRD / 变更内容。',
    can: ['创建/编辑 PRD、变更（Change）', '查看并评论文档', '查看项目所有内容'],
    cannot: ['管理成员', '审批文档/推进流水线', '管理项目设置与模板'],
  },
  reader: {
    summary: '只读访客视角，适合需要了解项目但不参与产出的干系人。',
    can: ['查看项目所有内容（PRD、文档、看板、流水线状态）'],
    cannot: ['创建/编辑任何内容', '管理成员', '审批文档、推进流水线'],
  },
}

const currentUserRole = computed(() => {
  const m = members.value.find(m => m.userId === auth.userId)
  return m?.role || null
})

const canManage = computed(() => currentUserRole.value === 'owner' || currentUserRole.value === 'maintainer')
const canChangeRole = computed(() => currentUserRole.value === 'owner')

async function loadMembers() {
  loading.value = true
  errorMsg.value = ''
  try {
    const res = await listProjectMembers(projectId.value)
    members.value = res?.data || []
  } catch (e: any) {
    errorMsg.value = e.message || '加载成员列表失败'
  } finally {
    loading.value = false
  }
}

async function loadUsers() {
  try {
    const res = await listAllUsers()
    allUsers.value = res?.data || []
  } catch (e: any) {
    console.error('加载用户列表失败', e)
  }
}

const availableUsers = computed(() => {
  const memberIds = new Set(members.value.map(m => m.userId))
  return allUsers.value.filter(u => !memberIds.has(u.userId))
})

async function handleAddMember() {
  if (!newUserId.value) return
  adding.value = true
  errorMsg.value = ''
  try {
    await addProjectMember(projectId.value, newUserId.value, newRole.value)
    successMsg.value = '成员添加成功'
    showAddDialog.value = false
    newUserId.value = ''
    newRole.value = 'contributor'
    await loadMembers()
  } catch (e: any) {
    errorMsg.value = e.message || '添加失败'
  } finally {
    adding.value = false
  }
}

async function handleChangeRole(userId: string, role: string) {
  if (!canChangeRole.value) return
  errorMsg.value = ''
  try {
    await updateProjectMemberRole(projectId.value, userId, role)
    successMsg.value = '角色更新成功'
    await loadMembers()
  } catch (e: any) {
    errorMsg.value = e.message || '更新失败'
  }
}

async function handleRemoveMember(userId: string, displayName: string) {
  if (!confirm(`确定要移除成员 ${displayName} 吗？`)) return
  errorMsg.value = ''
  try {
    await removeProjectMember(projectId.value, userId)
    successMsg.value = '成员移除成功'
    await loadMembers()
  } catch (e: any) {
    errorMsg.value = e.message || '移除失败'
  }
}

function getRoleLabel(role: string) {
  return roleOptions.find(r => r.value === role)?.label || role
}

onMounted(async () => {
  await loadMembers()
  // 等待成员列表加载完成后，再判断是否加载用户列表
  // 因为 canManage 依赖 members，必须在 loadMembers 之后判断
  if (canManage.value) loadUsers()
})
</script>

<template>
  <div class="member-page">
    <div class="page-header">
      <h2>项目成员管理</h2>
      <button v-if="canManage" class="btn btn-primary" @click="showAddDialog = true">+ 添加成员</button>
    </div>

    <!-- 提示信息 -->
    <div v-if="successMsg" class="alert alert-success">{{ successMsg }}</div>
    <div v-if="errorMsg" class="alert alert-error">{{ errorMsg }}</div>

    <!-- 成员列表 -->
    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="members.length === 0" class="empty-state">暂无项目成员</div>
    <div v-else class="member-table">
      <div class="table-header">
        <span class="col-user">用户</span>
        <span class="col-email">邮箱</span>
        <span class="col-role">角色</span>
        <span class="col-joined">加入时间</span>
        <span v-if="canManage" class="col-actions">操作</span>
      </div>
      <div v-for="m in members" :key="m.userId" class="table-row">
        <div class="col-user">
          <span class="user-avatar">👤</span>
          <div class="user-info">
            <span class="user-name">{{ m.displayName }}</span>
            <span class="user-username">@{{ m.username }}</span>
          </div>
          <span v-if="m.userId === auth.userId" class="me-badge">我</span>
        </div>
        <div class="col-email">{{ m.email || '-' }}</div>
        <div class="col-role">
          <select
            v-if="canChangeRole && m.userId !== auth.userId"
            :value="m.role"
            @change="(e) => handleChangeRole(m.userId, (e.target as HTMLSelectElement).value)"
            class="role-select"
          >
            <option v-for="opt in roleOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
          </select>
          <span v-else class="role-badge" :class="m.role">{{ getRoleLabel(m.role) }}</span>
        </div>
        <div class="col-joined">{{ m.createdAt?.substring(0, 10) || '-' }}</div>
        <div v-if="canManage && m.userId !== auth.userId" class="col-actions">
          <button class="btn btn-danger btn-sm" @click="handleRemoveMember(m.userId, m.displayName)">移除</button>
        </div>
      </div>
    </div>

    <!-- 添加成员弹窗 -->
    <div v-if="showAddDialog" class="dialog-overlay" @click.self="showAddDialog = false">
      <div class="dialog">
        <div class="dialog-header">
          <h3>添加成员</h3>
          <button class="close-btn" @click="showAddDialog = false">✕</button>
        </div>
        <div class="dialog-body">
          <div class="form-group">
            <label>选择用户</label>
            <select v-model="newUserId" class="input">
              <option value="" disabled>-- 请选择用户 --</option>
              <option v-for="u in availableUsers" :key="u.userId" :value="u.userId">
                {{ u.displayName }} (@{{ u.username }})
              </option>
            </select>
            <div v-if="availableUsers.length === 0" class="hint">所有用户已是项目成员</div>
          </div>
          <div class="form-group">
            <label>角色</label>
            <select v-model="newRole" class="input">
              <option v-for="opt in roleOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
            </select>
          </div>
          <!-- 角色说明卡片 -->
          <div class="role-info-card">
            <div class="role-info-title">
              <span class="role-info-name">{{ roleOptions.find(r => r.value === newRole)?.label }}</span>
              <span class="role-info-badge" :class="newRole">{{ newRole }}</span>
            </div>
            <p class="role-info-summary">{{ roleDetails[newRole]?.summary }}</p>
            <div class="role-info-section">
              <div class="role-info-label can">✓ 可以</div>
              <ul class="role-info-list">
                <li v-for="(item, i) in roleDetails[newRole]?.can || []" :key="'can-' + i">{{ item }}</li>
              </ul>
            </div>
            <div v-if="(roleDetails[newRole]?.cannot || []).length > 0" class="role-info-section">
              <div class="role-info-label cannot">✗ 不可以</div>
              <ul class="role-info-list">
                <li v-for="(item, i) in roleDetails[newRole]?.cannot || []" :key="'cannot-' + i">{{ item }}</li>
              </ul>
            </div>
          </div>
        </div>
        <div class="dialog-footer">
          <button class="btn btn-secondary" @click="showAddDialog = false">取消</button>
          <button class="btn btn-primary" :disabled="adding || !newUserId" @click="handleAddMember">
            {{ adding ? '添加中...' : '确认添加' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.member-page {
  max-width: 960px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}
.page-header h2 {
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}

/* 提示 */
.alert {
  padding: 10px 14px;
  border-radius: 8px;
  font-size: 13px;
  margin-bottom: 16px;
}
.alert-success {
  background: #f0fdf4;
  color: #166534;
  border: 1px solid #bbf7d0;
}
.alert-error {
  background: #fef2f2;
  color: #991b1b;
  border: 1px solid #fecaca;
}

/* 加载 / 空状态 */
.loading, .empty-state {
  text-align: center;
  color: #94a3b8;
  padding: 40px 0;
  font-size: 14px;
}

/* 表格 */
.member-table {
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  overflow: hidden;
}
.table-header, .table-row {
  display: grid;
  grid-template-columns: 1fr 1.2fr 0.8fr 0.8fr 0.6fr;
  padding: 12px 16px;
  align-items: center;
  gap: 8px;
}
.table-header {
  background: #f8fafc;
  font-size: 12px;
  font-weight: 600;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  border-bottom: 1px solid #e2e8f0;
}
.table-row {
  font-size: 13px;
  color: #1e293b;
  border-bottom: 1px solid #f1f5f9;
  transition: background 0.1s;
}
.table-row:last-child {
  border-bottom: none;
}
.table-row:hover {
  background: #f8fafc;
}

/* 用户列 */
.col-user {
  display: flex;
  align-items: center;
  gap: 8px;
}
.user-avatar {
  font-size: 20px;
  flex-shrink: 0;
}
.user-info {
  display: flex;
  flex-direction: column;
}
.user-name {
  font-weight: 600;
  font-size: 13px;
}
.user-username {
  font-size: 11px;
  color: #94a3b8;
}
.me-badge {
  font-size: 10px;
  background: #dbeafe;
  color: #1d4ed8;
  padding: 1px 6px;
  border-radius: 4px;
  font-weight: 600;
}

/* 角色 */
.col-role {
  display: flex;
  align-items: center;
}
.role-select {
  padding: 4px 8px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 12px;
  background: #fff;
  color: #1e293b;
  cursor: pointer;
}
.role-select:focus {
  border-color: #3b82f6;
  outline: none;
}
.role-badge {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 6px;
  font-weight: 500;
}
.role-badge.owner {
  background: #fef3c7;
  color: #92400e;
}
.role-badge.maintainer {
  background: #dbeafe;
  color: #1e3a5f;
}
.role-badge.contributor {
  background: #e0e7ff;
  color: #3730a3;
}
.role-badge.reader {
  background: #f3f4f6;
  color: #6b7280;
}

/* 操作按钮 */
.btn-sm {
  padding: 4px 10px;
  font-size: 12px;
}

/* 弹窗 */
.dialog-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}
.dialog {
  background: #fff;
  border-radius: 12px;
  width: 440px;
  max-width: 90vw;
  box-shadow: 0 20px 60px rgba(0,0,0,0.15);
}
.dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #e2e8f0;
}
.dialog-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
}
.close-btn {
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
  color: #94a3b8;
  padding: 4px;
}
.close-btn:hover {
  color: #1e293b;
}
.dialog-body {
  padding: 20px;
}
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding: 12px 20px;
  border-top: 1px solid #e2e8f0;
}
.form-group {
  margin-bottom: 16px;
}
.form-group:last-child {
  margin-bottom: 0;
}
.form-group label {
  display: block;
  font-size: 12px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 6px;
}
.form-group .input {
  width: 100%;
  padding: 8px 10px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 13px;
  color: #1e293b;
  background: #fff;
  box-sizing: border-box;
}
.form-group .input:focus {
  border-color: #3b82f6;
  outline: none;
}
.hint {
  font-size: 11px;
  color: #94a3b8;
  margin-top: 4px;
}

/* 角色说明卡片 */
.role-info-card {
  margin-top: 10px;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 14px 16px;
  background: #f8fafc;
}
.role-info-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}
.role-info-name {
  font-size: 14px;
  font-weight: 700;
  color: #1e293b;
}
.role-info-badge {
  font-size: 10px;
  font-weight: 600;
  padding: 1px 8px;
  border-radius: 99px;
  background: #e2e8f0;
  color: #475569;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
.role-info-badge.owner { background: #fef3c7; color: #92400e; }
.role-info-badge.maintainer { background: #dbeafe; color: #1e3a5f; }
.role-info-badge.contributor { background: #e0e7ff; color: #3730a3; }
.role-info-badge.reader { background: #e2e8f0; color: #64748b; }
.role-info-summary {
  font-size: 12px;
  color: #64748b;
  margin: 0 0 10px 0;
  line-height: 1.5;
}
.role-info-section {
  margin-bottom: 8px;
}
.role-info-section:last-child {
  margin-bottom: 0;
}
.role-info-label {
  font-size: 11px;
  font-weight: 700;
  margin-bottom: 4px;
}
.role-info-label.can { color: #16a34a; }
.role-info-label.cannot { color: #dc2626; }
.role-info-list {
  margin: 0;
  padding-left: 18px;
  font-size: 12px;
  color: #334155;
  line-height: 1.6;
}
.role-info-list li {
  margin-bottom: 2px;
}

/* 通用按钮 */
.btn {
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  border: none;
  transition: all 0.12s;
}
.btn-primary {
  background: #2563eb;
  color: #fff;
}
.btn-primary:hover {
  background: #1d4ed8;
}
.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.btn-secondary {
  background: #f1f5f9;
  color: #374151;
  border: 1px solid #e2e8f0;
}
.btn-secondary:hover {
  background: #e2e8f0;
}
.btn-danger {
  background: #fef2f2;
  color: #dc2626;
  border: 1px solid #fecaca;
}
.btn-danger:hover {
  background: #fee2e2;
}
</style>