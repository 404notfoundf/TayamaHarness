// ============================================================
// Mock 数据 — 供 store 在 useMock=true 时使用
// ============================================================

import type {
  PrdIngestResponse,
  DocumentSummary,
  IngestionProgress,
  RequirementEntity,
  DataEntity,
  InterfaceProtocol,
  ArchitectureDecision,
} from '@/models/prd'
import type { WikiDocument } from '@/models/document'
import type { ChangeSummary, ChangeDetail } from '@/models/change'
import type { Template } from '@/models/template'
import type { PipelineStatus, ChangeLogEntry, TraceNode } from '@/models/pipeline'

// ---- 常量 ----
export const MOCK_INGESTION_ID = 'ing-20260820-001'
export const MOCK_CHANGE_ID = 'C-001'
export const MOCK_TEMPLATE_ID_BM = 'tpl-bm-001'
export const MOCK_TEMPLATE_ID_DM = 'tpl-dm-001'
export const MOCK_TEMPLATE_ID_IP = 'tpl-ip-001'
export const MOCK_TEMPLATE_ID_AD = 'tpl-ad-001'

// ---- 模拟 PRD 原文 ----
export const MOCK_PRD_CONTENT = `# 用户登录模块 PRD

## 1. 概述
用户登录模块是系统的核心入口，支持手机号密码登录和微信扫码登录。

## 2. 功能需求

### 2.1 手机号密码登录
用户可以使用已注册的手机号和密码进行登录。
- 输入校验：手机号格式校验、密码非空校验
- 错误处理：登录失败提示"手机号或密码错误"
- 连续 5 次失败后，账号锁定 30 分钟

### 2.2 微信扫码登录
用户可以通过微信扫码快速登录。
- 生成临时二维码，有效期 5 分钟
- 扫码后自动登录，无需额外操作
- 首次扫码需绑定手机号

### 2.3 忘记密码
用户可以通过手机号验证码重置密码。
- 发送短信验证码，有效期 3 分钟
- 新密码需符合密码复杂度要求（8-20 位，含字母和数字）

## 3. 数据实体
- User: id, phone, password_hash, wechat_openid, status, created_at, updated_at
- Session: id, user_id, token, refresh_token, expires_at, created_at
- LoginLog: id, user_id, login_type, ip, device, status, created_at

## 4. 接口
- POST /api/v1/auth/login 用户登录
- POST /api/v1/auth/login/wechat 微信登录
- POST /api/v1/auth/refresh 刷新 token
- POST /api/v1/auth/forgot-password 忘记密码
- POST /api/v1/auth/logout 退出登录

## 5. 非功能需求
- 登录接口限流：每分钟 10 次/IP
- 密码使用 bcrypt 加密存储
- 会话有效期 7 天，可配置
- 所有登录操作记录审计日志
`

// ---- Mock 需求实体 ----
export const MOCK_REQUIREMENTS: RequirementEntity[] = [
  { id: 'REQ-001', description: '用户手机号密码登录', priority: 'P0', relatedEntities: ['User', 'Session'], sourceParagraph: '用户可以使用已注册的手机号和密码进行登录', notes: '' },
  { id: 'REQ-002', description: '微信扫码登录', priority: 'P0', relatedEntities: ['User', 'Wechat'], sourceParagraph: '用户可以通过微信扫码快速登录', notes: '需绑定手机号' },
  { id: 'REQ-003', description: '忘记密码重置', priority: 'P1', relatedEntities: ['User'], sourceParagraph: '用户可以通过手机号验证码重置密码', notes: '' },
  { id: 'REQ-004', description: '登录失败锁定', priority: 'P1', relatedEntities: ['User', 'LoginLog'], sourceParagraph: '连续 5 次失败后，账号锁定 30 分钟', notes: '安全策略' },
  { id: 'REQ-005', description: '登录审计日志', priority: 'P2', relatedEntities: ['LoginLog'], sourceParagraph: '所有登录操作记录审计日志', notes: '非功能需求' },
  { id: 'REQ-006', description: '登录接口限流', priority: 'P2', relatedEntities: ['Session'], sourceParagraph: '登录接口限流：每分钟 10 次/IP', notes: '安全策略' },
]

// ---- Mock 数据实体 ----
export const MOCK_DATA_ENTITIES: DataEntity[] = [
  {
    name: 'User', description: '系统用户',
    attributes: [
      { name: 'id', type: 'string', description: '用户唯一标识' },
      { name: 'phone', type: 'string', description: '手机号' },
      { name: 'password_hash', type: 'string', description: 'bcrypt 加密密码' },
      { name: 'wechat_openid', type: 'string', description: '微信 OpenID' },
      { name: 'status', type: 'enum', description: '账号状态: active/locked/disabled' },
      { name: 'created_at', type: 'datetime', description: '创建时间' },
      { name: 'updated_at', type: 'datetime', description: '更新时间' },
    ],
    relations: [
      { target: 'Session', type: '1:N', description: '一个用户可以有多个会话' },
      { target: 'LoginLog', type: '1:N', description: '一个用户有多条登录日志' },
    ],
  },
  {
    name: 'Session', description: '用户会话',
    attributes: [
      { name: 'id', type: 'string', description: '会话 ID' },
      { name: 'user_id', type: 'string', description: '用户 ID' },
      { name: 'token', type: 'string', description: 'JWT Access Token' },
      { name: 'refresh_token', type: 'string', description: '刷新 Token' },
      { name: 'expires_at', type: 'datetime', description: '过期时间' },
    ],
    relations: [{ target: 'User', type: 'N:1', description: '属于一个用户' }],
  },
  {
    name: 'LoginLog', description: '登录日志',
    attributes: [
      { name: 'id', type: 'string', description: '日志 ID' },
      { name: 'user_id', type: 'string', description: '用户 ID' },
      { name: 'login_type', type: 'enum', description: '登录方式: password/wechat' },
      { name: 'ip', type: 'string', description: '登录 IP' },
      { name: 'device', type: 'string', description: '设备信息' },
      { name: 'status', type: 'enum', description: '状态: success/failed' },
      { name: 'created_at', type: 'datetime', description: '登录时间' },
    ],
    relations: [{ target: 'User', type: 'N:1', description: '属于一个用户' }],
  },
]

// ---- Mock 接口协议 ----
export const MOCK_INTERFACES: InterfaceProtocol[] = [
  { method: 'POST', path: '/api/v1/auth/login', summary: '用户登录', requestBody: '{ phone, password }', responseBody: '{ token, refreshToken, user }', notes: '限流 10次/分钟/IP' },
  { method: 'POST', path: '/api/v1/auth/login/wechat', summary: '微信扫码登录', requestBody: '{ code }', responseBody: '{ token, refreshToken, user, needBindPhone }', notes: '首次扫码需绑定手机号' },
  { method: 'POST', path: '/api/v1/auth/refresh', summary: '刷新 Token', requestBody: '{ refreshToken }', responseBody: '{ token, refreshToken }', notes: '旧 token 立即失效' },
  { method: 'POST', path: '/api/v1/auth/forgot-password', summary: '忘记密码', requestBody: '{ phone, verificationCode, newPassword }', responseBody: '{ ok }', notes: '验证码有效期 3 分钟' },
  { method: 'POST', path: '/api/v1/auth/logout', summary: '退出登录', requestBody: '', responseBody: '{ ok }', notes: '清除服务端会话' },
]

// ---- Mock 架构决策 ----
export const MOCK_ARCH_DECISIONS: ArchitectureDecision[] = [
  { id: 'AD-001', title: '采用 JWT 作为认证方案', context: '需要无状态认证，支持分布式部署', decision: '采用 JWT + Refresh Token 方案，Access Token 有效期 15 分钟，Refresh Token 有效期 7 天', consequences: ['需要实现 token 刷新机制', 'token 过期时间需谨慎设置', '服务端需维护 token 黑名单'], status: 'accepted' },
  { id: 'AD-002', title: '密码使用 bcrypt 加密存储', context: '密码安全存储是基本要求', decision: '使用 bcrypt 加盐哈希，成本因子 10', consequences: ['登录性能受影响（约 100ms 验证时间）', '不可逆加密，无法找回密码'], status: 'accepted' },
  { id: 'AD-003', title: '登录失败锁定策略', context: '防止暴力破解', decision: '连续 5 次失败锁定 30 分钟，支持管理员手动解锁', consequences: ['需要计数器持久化', '存在 DOS 风险'], status: 'accepted' },
]

// ---- Mock 文档摘要 ----
export const MOCK_DOC_SUMMARIES: DocumentSummary[] = [
  { docId: 'doc-bm-001', type: 'business-model', title: '业务模型 - 用户登录模块', status: 'draft', version: 1, updatedAt: '2026-08-20T10:00:00Z' },
  { docId: 'doc-dm-001', type: 'data-model', title: '数据模型 - 用户登录模块', status: 'draft', version: 1, updatedAt: '2026-08-20T10:00:00Z' },
  { docId: 'doc-ip-001', type: 'interface-protocol', title: '接口协议 - 用户登录模块', status: 'draft', version: 1, updatedAt: '2026-08-20T10:00:00Z' },
  { docId: 'doc-ad-001', type: 'architecture-decision', title: '架构决策 - 用户登录模块', status: 'draft', version: 1, updatedAt: '2026-08-20T10:00:00Z' },
]

// ---- Mock 解析结果 ----
export const MOCK_INGEST_RESULT: PrdIngestResponse = {
  ingestionId: MOCK_INGESTION_ID,
  status: 'completed',
  title: '用户登录模块 PRD',
  requirements: MOCK_REQUIREMENTS,
  dataEntities: MOCK_DATA_ENTITIES,
  interfaces: MOCK_INTERFACES,
  architectureDecisions: MOCK_ARCH_DECISIONS,
  documents: MOCK_DOC_SUMMARIES,
}

// ---- Mock 解析进度 ----
export const MOCK_INGEST_PROGRESS: IngestionProgress = {
  ingestionId: MOCK_INGESTION_ID,
  status: 'completed',
  progress: 100,
  message: '解析完成',
}

// ---- Mock Wiki 文档内容 ----
export const MOCK_BUSINESS_MODEL_CONTENT = `# 业务模型 - 用户登录模块

## 需求列表
| ID | 描述 | 优先级 | 关联实体 | 备注 |
|----|------|--------|---------|------|
| REQ-001 | 用户手机号密码登录 | P0 | User, Session | |
| REQ-002 | 微信扫码登录 | P0 | User, Wechat | 需绑定手机号 |
| REQ-003 | 忘记密码重置 | P1 | User | 验证码重置 |
| REQ-004 | 登录失败锁定 | P1 | User, LoginLog | 安全策略 |
| REQ-005 | 登录审计日志 | P2 | LoginLog | 非功能需求 |
| REQ-006 | 登录接口限流 | P2 | Session | 安全策略 |

## 用户故事
- 作为用户，我想要使用手机号和密码登录，以便快速访问系统
- 作为用户，我想要使用微信扫码登录，以便无需记住密码
- 作为用户，我忘记密码时可以通过手机号验证码重置，以便恢复正常使用

## 业务流程
\`\`\`mermaid
flowchart LR
  A[打开登录页] --> B{选择登录方式}
  B --> C[手机号密码登录]
  B --> D[微信扫码登录]
  C --> E{验证}
  D --> E
  E -->|成功| F[进入首页]
  E -->|失败| G[错误提示]
  G --> A
\`\`\`

## 验收标准
- AC-001: 用户输入正确手机号和密码，登录成功跳转首页
- AC-002: 用户输入错误密码，提示"手机号或密码错误"
- AC-003: 连续 5 次登录失败，账号锁定 30 分钟
- AC-004: 微信扫码成功后自动登录
- AC-005: 微信首次扫码需绑定手机号
`

export const MOCK_DATA_MODEL_CONTENT = `# 数据模型 - 用户登录模块

## 实体关系图
\`\`\`mermaid
erDiagram
  User ||--o{ Session : has
  User ||--o{ LoginLog : records
  Session {
    string id PK
    string user_id FK
    string token
    string refresh_token
    datetime expires_at
  }
  LoginLog {
    string id PK
    string user_id FK
    string login_type
    string ip
    string device
    string status
    datetime created_at
  }
\`\`\`

## 实体定义
### User
| 字段 | 类型 | 说明 | 约束 |
|------|------|------|------|
| id | string | 用户唯一标识 | UUID, PK |
| phone | string | 手机号 | UNIQUE, NOT NULL |
| password_hash | string | bcrypt 加密密码 | NOT NULL |
| wechat_openid | string | 微信 OpenID | 可为空 |
| status | enum | 账号状态 | active/locked/disabled |
| created_at | datetime | 创建时间 | NOT NULL |
| updated_at | datetime | 更新时间 | NOT NULL |
`

export const MOCK_INTERFACE_CONTENT = `# 接口协议 - 用户登录模块

## 接口列表
### POST /api/v1/auth/login
**说明**: 用户手机号密码登录
**请求体**:
\`\`\`json
{
  "phone": "13800138000",
  "password": "encrypted_password"
}
\`\`\`
**响应**:
\`\`\`json
{
  "token": "jwt_access_token",
  "refreshToken": "jwt_refresh_token",
  "user": {
    "id": "user-001",
    "phone": "13800138000",
    "status": "active"
  }
}
\`\`\`
**限流**: 10次/分钟/IP
`

export const MOCK_ARCH_DECISION_CONTENT = `# 架构决策 - 用户登录模块

## AD-001: 采用 JWT 作为认证方案
- **状态**: accepted
- **上下文**: 需要无状态认证，支持分布式部署
- **决策**: 采用 JWT + Refresh Token 方案
- **后果**:
  - 需要实现 token 刷新机制
  - token 过期时间需谨慎设置
  - 服务端需维护 token 黑名单

## AD-002: 密码使用 bcrypt 加密存储
- **状态**: accepted
- **上下文**: 密码安全存储是基本要求
- **决策**: 使用 bcrypt 加盐哈希，成本因子 10
- **后果**:
  - 登录性能受影响（约 100ms 验证时间）
  - 不可逆加密，无法找回密码
`

export const MOCK_WIKI_DOCUMENTS: WikiDocument[] = [
  { docId: 'doc-bm-001', type: 'business-model', title: '业务模型 - 用户登录模块', status: 'draft', version: 1, content: MOCK_BUSINESS_MODEL_CONTENT, originalPrdContent: '## 功能需求\n### 2.1 手机号密码登录\n用户可以使用已注册的手机号和密码进行登录', changeLog: '', createdBy: 'LLM', updatedAt: '2026-08-20T10:00:00Z', ingestionId: MOCK_INGESTION_ID },
  { docId: 'doc-dm-001', type: 'data-model', title: '数据模型 - 用户登录模块', status: 'draft', version: 1, content: MOCK_DATA_MODEL_CONTENT, originalPrdContent: '## 3. 数据实体\nUser: id, phone, password_hash...', changeLog: '', createdBy: 'LLM', updatedAt: '2026-08-20T10:00:00Z', ingestionId: MOCK_INGESTION_ID },
  { docId: 'doc-ip-001', type: 'interface-protocol', title: '接口协议 - 用户登录模块', status: 'draft', version: 1, content: MOCK_INTERFACE_CONTENT, originalPrdContent: '## 4. 接口\n- POST /api/v1/auth/login 用户登录', changeLog: '', createdBy: 'LLM', updatedAt: '2026-08-20T10:00:00Z', ingestionId: MOCK_INGESTION_ID },
  { docId: 'doc-ad-001', type: 'architecture-decision', title: '架构决策 - 用户登录模块', status: 'draft', version: 1, content: MOCK_ARCH_DECISION_CONTENT, originalPrdContent: '## 5. 非功能需求\n- 密码使用 bcrypt 加密存储', changeLog: '', createdBy: 'LLM', updatedAt: '2026-08-20T10:00:00Z', ingestionId: MOCK_INGESTION_ID },
]

// ---- Mock Change ----
export const MOCK_CHANGE_SUMMARIES: ChangeSummary[] = [
  { changeId: 'C-001', title: '用户登录模块实现', status: 'drafting', requirementIds: ['REQ-001', 'REQ-002', 'REQ-003', 'REQ-004', 'REQ-005', 'REQ-006'], documentRefs: [
    { type: 'business-model', docId: 'doc-bm-001', title: '业务模型', status: 'draft' },
    { type: 'data-model', docId: 'doc-dm-001', title: '数据模型', status: 'draft' },
    { type: 'interface-protocol', docId: 'doc-ip-001', title: '接口协议', status: 'draft' },
    { type: 'architecture-decision', docId: 'doc-ad-001', title: '架构决策', status: 'draft' },
  ], createdAt: '2026-08-20T10:00:00Z', updatedAt: '2026-08-20T10:00:00Z' },
]

export const MOCK_CHANGE_DETAIL: ChangeDetail = {
  changeId: 'C-001',
  title: '用户登录模块实现',
  status: 'drafting',
  content: `# Change: C-001
## 标题
用户登录模块实现

## 关联文档
- business-model.md (approved)
- data-model.md (approved)
- interface-protocol.md (approved)
- architecture-decision.md (approved)

## 验收标准
- AC-001: 用户输入正确手机号和密码，登录成功跳转首页
- AC-002: 用户输入错误密码，提示"手机号或密码错误"
- AC-003: 连续 5 次登录失败，账号锁定 30 分钟
- AC-004: 微信扫码成功后自动登录

## 依赖
无（首个 change）
`,
  requirementIds: ['REQ-001', 'REQ-002', 'REQ-003', 'REQ-004'],
  documentRefs: [
    { type: 'business-model', docId: 'doc-bm-001', title: '业务模型', status: 'approved' },
    { type: 'data-model', docId: 'doc-dm-001', title: '数据模型', status: 'approved' },
    { type: 'interface-protocol', docId: 'doc-ip-001', title: '接口协议', status: 'approved' },
    { type: 'architecture-decision', docId: 'doc-ad-001', title: '架构决策', status: 'approved' },
  ],
  acceptanceCriteria: [
    { id: 'AC-001', description: '用户输入正确手机号和密码，登录成功跳转首页' },
    { id: 'AC-002', description: '用户输入错误密码，提示"手机号或密码错误"' },
    { id: 'AC-003', description: '连续 5 次登录失败，账号锁定 30 分钟' },
    { id: 'AC-004', description: '微信扫码成功后自动登录' },
  ],
  dependencies: [],
  createdAt: '2026-08-20T10:00:00Z',
  updatedAt: '2026-08-20T10:00:00Z',
}

// ---- Mock 模板 ----
export const MOCK_TEMPLATES: Template[] = [
  {
    templateId: MOCK_TEMPLATE_ID_BM, type: 'business-model', name: '业务模型模板', description: '标准业务模型模板，包含需求列表、用户故事、业务流程、验收标准',
    content: '# {{MODEL_NAME}}\n\n## 需求列表\n| ID | 描述 | 优先级 | 关联实体 | 备注 |\n|----|------|--------|---------|------|\n{{REQUIREMENTS_TABLE}}\n\n## 用户故事\n{{USER_STORIES}}\n\n## 业务流程\n```mermaid\nflowchart LR\n  {{FLOWCHART}}\n```\n\n## 验收标准\n{{ACCEPTANCE_CRITERIA}}',
    version: 2, versions: [], updatedAt: '2026-08-20T10:00:00Z', updatedBy: '架构师-张三',
  },
  {
    templateId: MOCK_TEMPLATE_ID_DM, type: 'data-model', name: '数据模型模板', description: '标准数据模型模板，包含实体关系图和实体定义',
    content: '# {{MODEL_NAME}}\n\n## 实体关系图\n```mermaid\nerDiagram\n  {{ER_DIAGRAM}}\n```\n\n## 实体定义\n{{ENTITY_DEFINITIONS}}',
    version: 1, versions: [], updatedAt: '2026-08-20T10:00:00Z', updatedBy: '架构师-张三',
  },
  {
    templateId: MOCK_TEMPLATE_ID_IP, type: 'interface-protocol', name: '接口协议模板', description: '标准接口协议模板，包含接口列表、请求/响应定义',
    content: '# {{MODEL_NAME}}\n\n## 接口列表\n{{INTERFACE_LIST}}',
    version: 1, versions: [], updatedAt: '2026-08-20T10:00:00Z', updatedBy: '架构师-张三',
  },
  {
    templateId: MOCK_TEMPLATE_ID_AD, type: 'architecture-decision', name: '架构决策模板', description: '标准架构决策模板，包含 ADR 格式',
    content: '# {{MODEL_NAME}}\n\n{{ARCHITECTURE_DECISIONS}}',
    version: 1, versions: [], updatedAt: '2026-08-20T10:00:00Z', updatedBy: '架构师-张三',
  },
]

// ---- Mock 流水线 ----
export const MOCK_PIPELINE_STATUS: PipelineStatus = {
  changeId: 'C-001',
  currentStage: 'drafting',
  stages: [
    { stage: 'drafting', status: 'active', startedAt: '2026-08-20T10:00:00Z' },
    { stage: 'reviewing', status: 'pending' },
    { stage: 'approved', status: 'pending' },
    { stage: 'completed', status: 'pending' },
  ],
  progress: 10,
}

export const MOCK_CHANGE_LOGS: ChangeLogEntry[] = [
  { id: 'log-001', type: 'status_change', message: '进入 drafting 阶段', detail: 'Change C-001 已创建', actor: '系统', createdAt: '2026-08-20T10:00:00Z' },
  { id: 'log-002', type: 'comment', message: '文档已全部生成，请架构师审核', detail: '4 份文档均为 draft 状态', actor: '系统', createdAt: '2026-08-20T10:01:00Z' },
]

// ---- Mock 追溯树 ----
export const MOCK_TRACE_TREE: TraceNode = {
  id: 'REQ-001',
  label: '用户登录功能',
  type: 'requirement',
  children: [
    {
      id: 'doc-bm-001', label: 'business-model.md', type: 'document', status: 'approved',
      children: [
        { id: 'C-001', label: 'Change: 用户登录模块', type: 'change', status: 'drafting', children: [
          { id: 'commit-001', label: 'commit: 3a2b1c (feat: login API)', type: 'commit' },
          { id: 'PR-001', label: 'PR #42 (reviewing)', type: 'pr', status: 'reviewing' },
        ]},
      ],
    },
    { id: 'doc-dm-001', label: 'data-model.md', type: 'document', status: 'approved' },
    { id: 'doc-ip-001', label: 'interface-protocol.md', type: 'document', status: 'draft' },
    { id: 'doc-ad-001', label: 'architecture-decision.md', type: 'document', status: 'approved' },
  ],
}

// ---- Mock 看板统计 ----
export const MOCK_KANBAN_STATS = {
  columns: {
    prd_imported: { count: 5, label: 'PRD 导入' },
    document_review: { count: 3, label: '文档校验' },
    design_review: { count: 2, label: '设计评审' },
    completed: { count: 6, label: '已完成' },
  },
}

// 看板 Mock 数据
export const MOCK_KANBAN_ITEMS = [
  { id: 'REQ-001', title: '用户登录功能', priority: 'P0', status: 'prd_imported', assignee: '张三', updatedAt: '2026-08-20' },
  { id: 'REQ-002', title: '微信扫码登录', priority: 'P0', status: 'document_review', assignee: '李四', updatedAt: '2026-08-19' },
  { id: 'REQ-003', title: '忘记密码重置', priority: 'P1', status: 'design_review', assignee: '王五', updatedAt: '2026-08-18' },
  { id: 'REQ-004', title: '登录失败锁定', priority: 'P1', status: 'coding', assignee: '张三', updatedAt: '2026-08-17' },
  { id: 'REQ-005', title: '登录审计日志', priority: 'P2', status: 'testing', assignee: '李四', updatedAt: '2026-08-16' },
  { id: 'REQ-006', title: '登录接口限流', priority: 'P2', status: 'completed', assignee: '王五', updatedAt: '2026-08-15' },
  { id: 'REQ-007', title: '密码加密存储', priority: 'P0', status: 'prd_imported', assignee: '张三', updatedAt: '2026-08-20' },
  { id: 'REQ-008', title: 'Token 刷新机制', priority: 'P1', status: 'document_review', assignee: '李四', updatedAt: '2026-08-19' },
]