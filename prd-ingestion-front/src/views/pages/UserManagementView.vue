<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { listAllUsers, disableUserApi, enableUserApi, updateUserRoleApi, deleteUserApi } from '@/services/member'
import { registerApi } from '@/services/auth'
import type { User } from '@/models/auth'

const auth = useAuthStore()
const users = ref<User[]>([])
const loading = ref(false)
const errorMsg = ref('')
const successMsg = ref('')

// 添加用户弹窗
const showAddDialog = ref(false)
const addForm = ref({ username: '', password: '', displayName: '', email: '', role: 'DEVELOPER' })
const adding = ref(false)

// 角色编辑
const editingRole = ref<string | null>(null)

const roleOptions = [
  { value: 'ADMIN', label: '管理员' },
  { value: 'PROJECT_MANAGER', label: '项目经理' },
  { value: 'DEVELOPER', label: '开发者' },
  { value: 'VIEWER', label: '观察者' },
]

const roleLabelMap: Record<string, string> = {
  ADMIN: '管理员',
  PROJECT_MANAGER: '项目经理',
  DEVELOPER: '开发者',
  VIEWER: '观察者',
}

function getRoleLabel(role: string) {
  return roleLabelMap[role] || role
}

async function loadUsers() {
  loading.value = true
  errorMsg.value = ''
  try {
    const res = await listAllUsers()
    users.value = res?.data || []
  } catch (e: any) {
    errorMsg.value = e.message || '加载用户列表失败'
  } finally {
    loading.value = false
  }
}

async function handleToggleStatus(user: User) {
  const action = user.status === 'active' ? '禁用' : '启用'
  if (!confirm(`确定要${action}用户 ${user.displayName} 吗？`)) return
  errorMsg.value = ''
  try {
    if (user.status === 'active') {
      await disableUserApi(user.userId)
    } else {
      await enableUserApi(user.userId)
    }
    successMsg.value = `用户已${action}`
    await loadUsers()
  } catch (e: any) {
    errorMsg.value = e.message || '操作失败'
  }
}

async function handleChangeRole(user: User, role: string) {
  editingRole.value = null
  errorMsg.value = ''
  try {
    await updateUserRoleApi(user.userId, role)
    successMsg.value = '角色已更新'
    await loadUsers()
  } catch (e: any) {
    errorMsg.value = e.message || '更新失败'
  }
}

async function handleDeleteUser(user: User) {
  if (!confirm(`确定要删除用户 ${user.displayName} 吗？\n此操作不可恢复！`)) return
  errorMsg.value = ''
  try {
    await deleteUserApi(user.userId)
    successMsg.value = '用户已删除'
    await loadUsers()
  } catch (e: any) {
    errorMsg.value = e.message || '删除失败'
  }
}

async function handleAddUser() {
  if (!addForm.value.username || !addForm.value.password) return
  adding.value = true
  errorMsg.value = ''
  try {
    await registerApi({ ...addForm.value })
    successMsg.value = '用户创建成功'
    showAddDialog.value = false
    addForm.value = { username: '', password: '', displayName: '', email: '', role: 'DEVELOPER' }
    await loadUsers()
  } catch (e: any) {
    errorMsg.value = e.message || '创建失败'
  } finally {
    adding.value = false
  }
}

function getStatusLabel(status: string) {
  return status === 'active' ? '正常' : '已禁用'
}

onMounted(loadUsers)
</script>

<template>
  <div class="user-mgmt-page">
    <div class="page-header">
      <h2>用户管理</h2>
      <button class="btn btn-primary" @click="showAddDialog = true">+ 添加用户</button>
    </div>

    <div v-if="successMsg" class="alert alert-success">{{ successMsg }}</div>
    <div v-if="errorMsg" class="alert alert-error">{{ errorMsg }}</div>

    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="users.length === 0" class="empty-state">暂无用户</div>
    <div v-else class="user-table">
      <div class="table-header">
        <span class="col-user">用户</span>
        <span class="col-email">邮箱</span>
        <span class="col-status">状态</span>
        <span class="col-role">角色</span>
        <span class="col-actions">操作</span>
      </div>
      <div v-for="u in users" :key="u.userId" class="table-row">
        <div class="col-user">
          <span class="user-avatar">👤</span>
          <div class="user-info">
            <span class="user-name">{{ u.displayName }}</span>
            <span class="user-username">@{{ u.username }}</span>
          </div>
          <span v-if="u.userId === auth.userId" class="me-badge">我</span>
        </div>
        <div class="col-email">{{ u.email || '-' }}</div>
        <div class="col-status">
          <span class="status-badge" :class="u.status">{{ getStatusLabel(u.status) }}</span>
        </div>
        <div class="col-role">
          <select
            v-if="editingRole === u.userId"
            class="role-select"
            :value="u.roles?.[0] || 'DEVELOPER'"
            @change="(e) => handleChangeRole(u, (e.target as HTMLSelectElement).value)"
            @blur="editingRole = null"
          >
            <option v-for="opt in roleOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
          </select>
          <button
            v-else-if="u.userId !== auth.userId"
            class="btn btn-sm btn-secondary"
            @click="editingRole = u.userId"
          >
            {{ getRoleLabel(u.roles?.[0] || '未分配') }}
          </button>
          <span v-else class="role-text">{{ getRoleLabel(u.roles?.[0] || '未分配') }}</span>
        </div>
        <div class="col-actions">
          <button
            v-if="u.userId !== auth.userId"
            class="btn btn-sm"
            :class="u.status === 'active' ? 'btn-warning' : 'btn-success'"
            @click="handleToggleStatus(u)"
          >
            {{ u.status === 'active' ? '禁用' : '启用' }}
          </button>
          <button
            v-if="u.userId !== auth.userId"
            class="btn btn-sm btn-danger"
            @click="handleDeleteUser(u)"
          >
            删除
          </button>
        </div>
      </div>
    </div>

    <!-- 添加用户弹窗 -->
    <div v-if="showAddDialog" class="dialog-overlay" @click.self="showAddDialog = false">
      <div class="dialog">
        <div class="dialog-header">
          <h3>添加用户</h3>
          <button class="close-btn" @click="showAddDialog = false">✕</button>
        </div>
        <div class="dialog-body">
          <div class="form-group">
            <label>用户名 *</label>
            <input v-model="addForm.username" class="input" placeholder="登录用户名" />
          </div>
          <div class="form-group">
            <label>密码 *</label>
            <input v-model="addForm.password" class="input" type="password" placeholder="密码" />
          </div>
          <div class="form-group">
            <label>显示名称</label>
            <input v-model="addForm.displayName" class="input" placeholder="用户显示名称" />
          </div>
          <div class="form-group">
            <label>邮箱</label>
            <input v-model="addForm.email" class="input" placeholder="邮箱地址" />
          </div>
          <div class="form-group">
            <label>角色</label>
            <select v-model="addForm.role" class="input">
              <option v-for="opt in roleOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
            </select>
          </div>
        </div>
        <div class="dialog-footer">
          <button class="btn btn-secondary" @click="showAddDialog = false">取消</button>
          <button class="btn btn-primary" :disabled="adding || !addForm.username || !addForm.password" @click="handleAddUser">
            {{ adding ? '创建中...' : '确认创建' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.user-mgmt-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 24px;
}
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}
.page-header h2 { font-size: 20px; font-weight: 700; color: #1e293b; margin: 0; }
.alert { padding: 10px 14px; border-radius: 8px; font-size: 13px; margin-bottom: 16px; }
.alert-success { background: #f0fdf4; color: #166534; border: 1px solid #bbf7d0; }
.alert-error { background: #fef2f2; color: #991b1b; border: 1px solid #fecaca; }
.loading, .empty-state { text-align: center; color: #94a3b8; padding: 40px 0; font-size: 14px; }
.user-table { border: 1px solid #e2e8f0; border-radius: 10px; overflow: hidden; }
.table-header, .table-row {
  display: grid;
  grid-template-columns: 1fr 1fr 0.5fr 0.6fr 1fr;
  padding: 12px 16px;
  align-items: center;
  gap: 8px;
}
.table-header { background: #f8fafc; font-size: 12px; font-weight: 600; color: #64748b; text-transform: uppercase; border-bottom: 1px solid #e2e8f0; }
.table-row { font-size: 13px; color: #1e293b; border-bottom: 1px solid #f1f5f9; }
.table-row:last-child { border-bottom: none; }
.table-row:hover { background: #f8fafc; }
.col-user { display: flex; align-items: center; gap: 8px; }
.user-avatar { font-size: 20px; }
.user-info { display: flex; flex-direction: column; }
.user-name { font-weight: 600; font-size: 13px; }
.user-username { font-size: 11px; color: #94a3b8; }
.me-badge { font-size: 10px; background: #dbeafe; color: #1d4ed8; padding: 1px 6px; border-radius: 4px; font-weight: 600; }
.status-badge { font-size: 12px; padding: 2px 8px; border-radius: 6px; font-weight: 500; }
.status-badge.active { background: #dcfce7; color: #166534; }
.status-badge.disabled { background: #fef2f2; color: #991b1b; }
.col-role { display: flex; align-items: center; }
.role-select { padding: 4px 8px; border: 1px solid #d1d5db; border-radius: 6px; font-size: 12px; background: #fff; cursor: pointer; }
.role-select:focus { border-color: #3b82f6; outline: none; }
.role-text { font-size: 12px; color: #6b7280; }
.col-actions { display: flex; gap: 6px; }
.btn { padding: 6px 12px; border-radius: 6px; font-size: 12px; font-weight: 600; cursor: pointer; border: none; transition: all 0.12s; }
.btn-primary { background: #2563eb; color: #fff; }
.btn-primary:hover { background: #1d4ed8; }
.btn-primary:disabled { opacity: 0.5; cursor: not-allowed; }
.btn-sm { padding: 4px 10px; font-size: 12px; }
.btn-secondary { background: #f1f5f9; color: #374151; border: 1px solid #e2e8f0; }
.btn-secondary:hover { background: #e2e8f0; }
.btn-warning { background: #fef3c7; color: #92400e; border: 1px solid #fde68a; }
.btn-warning:hover { background: #fde68a; }
.btn-success { background: #dcfce7; color: #166534; border: 1px solid #bbf7d0; }
.btn-success:hover { background: #bbf7d0; }
.btn-danger { background: #fef2f2; color: #dc2626; border: 1px solid #fecaca; }
.btn-danger:hover { background: #fee2e2; }

/* 弹窗 */
.dialog-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,0.4);
  display: flex; align-items: center; justify-content: center;
  z-index: 1000;
}
.dialog {
  background: #fff; border-radius: 12px; width: 420px; max-width: 90vw;
  box-shadow: 0 20px 60px rgba(0,0,0,0.15);
}
.dialog-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 16px 20px; border-bottom: 1px solid #e2e8f0;
}
.dialog-header h3 { margin: 0; font-size: 16px; font-weight: 700; }
.close-btn { background: none; border: none; font-size: 18px; cursor: pointer; color: #94a3b8; padding: 4px; }
.close-btn:hover { color: #1e293b; }
.dialog-body { padding: 20px; }
.dialog-footer { display: flex; justify-content: flex-end; gap: 8px; padding: 12px 20px; border-top: 1px solid #e2e8f0; }
.form-group { margin-bottom: 14px; }
.form-group:last-child { margin-bottom: 0; }
.form-group label { display: block; font-size: 12px; font-weight: 600; color: #374151; margin-bottom: 6px; }
.form-group .input {
  width: 100%; padding: 8px 10px; border: 1px solid #d1d5db; border-radius: 8px;
  font-size: 13px; color: #1e293b; background: #fff; box-sizing: border-box;
}
.form-group .input:focus { border-color: #3b82f6; outline: none; }
</style>