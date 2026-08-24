# PRD 智能解析平台 API 接口文档

> 本文档描述 PRD 智能解析平台（Harness Flow）后端 REST API。
>
> 基础路径：`/api/v1`

## 认证

### AuthController

所有 API 路径前缀：`/api/v1/auth`

| 方法 | 路径 | 说明 | 请求体 | 响应 |
|------|------|------|--------|------|
| POST | `/auth/login` | 用户登录 | `{ username, password }` | `{ token, user }` |
| POST | `/auth/register` | 用户注册 | `{ username, password, email }` | `{ token, user }` |
| GET | `/auth/me` | 获取当前用户信息 | — | `{ id, username, role }` |
| GET | `/auth/projects/{projectId}/members` | 项目成员列表 | — | `[{ id, username, role }]` |
| POST | `/auth/projects/{projectId}/members` | 添加项目成员 | `{ userId, role }` | `{ id, userId, role }` |
| PUT | `/auth/projects/{projectId}/members/{userId}` | 更新成员角色 | `{ role }` | `{ id, userId, role }` |
| DELETE | `/auth/projects/{projectId}/members/{userId}` | 移除成员 | — | 204 |
| GET | `/auth/users` | 用户列表（管理员） | — | `[{ id, username, role, enabled }]` |
| PUT | `/auth/users/{userId}/disable` | 禁用用户 | — | 200 |
| PUT | `/auth/users/{userId}/enable` | 启用用户 | — | 200 |
| PUT | `/auth/users/{userId}/role` | 更新用户角色 | `{ role }` | 200 |
| DELETE | `/auth/users/{userId}` | 删除用户 | — | 204 |

## PRD 文档导入

### PrdController

路径前缀：`/api/v1/prd`

| 方法 | 路径 | 说明 | 请求体 | 响应 |
|------|------|------|--------|------|
| POST | `/prd/ingest` | 上传并解析 PRD | `{ content 或 fileMd5, projectId, title }` | `{ ingestionId }` |
| GET | `/prd/ingest/{ingestionId}/progress` | 查询解析进度 | — | `{ status, progress }` |
| POST | `/prd/ingest/{ingestionId}/confirm` | 确认候选条目 | `{ itemIds }` | `{ confirmed }` |
| GET | `/prd/ingest/{ingestionId}` | 获取解析结果 | — | `{ ingestion, documents }` |
| POST | `/prd/ingest/{ingestionId}/reparse` | 重新解析（覆盖） | — | `{ ingestionId }` |

## 4 篇文档管理

### DocumentController

路径前缀：`/api/v1/documents`

| 方法 | 路径 | 说明 | 参数 | 响应 |
|------|------|------|------|------|
| GET | `/documents` | 文档列表 | `?ingestionId=xxx`（可选） | `[{ id, type, title, status }]` |
| GET | `/documents/{docId}` | 文档详情 | — | `{ id, type, title, content, status, version }` |
| PUT | `/documents/{docId}` | 编辑文档内容 | `{ content }` | `{ id, updatedAt }` |
| POST | `/documents/{docId}/approval` | 审批文档 | `{ approved, comment }` | `{ id, status }` |

## Change 管理

### ChangeController

路径前缀：`/api/v1/changes`

| 方法 | 路径 | 说明 | 请求体/参数 | 响应 |
|------|------|------|------------|------|
| POST | `/changes` | 创建 Change（从 4 文档合成） | `{ ingestionId }` | `{ changeId, title }` |
| GET | `/changes` | Change 列表 | `?page=0&size=20` | `{ content: [...], totalPages, totalElements }` |
| GET | `/changes/{changeId}` | Change 详情 | — | `{ changeId, title, content, status, documentRefs, acceptanceCriteria }` |
| PUT | `/changes/{changeId}` | 编辑 Change 内容 | `{ content }` | `{ changeId, updatedAt }` |
| POST | `/changes/{changeId}/regenerate` | 重新生成 Change（覆盖） | — | `{ changeId, content }` |

## 流水线

### PipelineController

路径前缀：`/api/v1/pipeline`

| 方法 | 路径 | 说明 | 响应 |
|------|------|------|------|
| GET | `/pipeline/{changeId}` | 获取流水线状态 | `{ changeId, currentStage, stages: [...] }` |
| GET | `/pipeline/{changeId}/logs` | 获取操作日志 | `[{ action, operator, detail, createdAt }]` |
| POST | `/pipeline/{changeId}/advance` | 推进到下一阶段 | `{ changeId, newStage }` |
| GET | `/requirements/{reqId}/trace` | 需求追溯树 | `{ tree }` |
| GET | `/kanban/stats` | 看板统计数据 | `{ columns: [...] }` |

## 看板

### KanbanController

路径前缀：`/api/v1/kanban`

| 方法 | 路径 | 说明 | 响应 |
|------|------|------|------|
| GET | `/kanban/stats` | 看板统计（按列分组） | `{ columns: [{ key, label, items: [...], count }] }` |

## 文件上传

### FileUploadController

路径前缀：`/api/v1/files`

| 方法 | 路径 | 说明 | 请求体 | 响应 |
|------|------|------|--------|------|
| POST | `/files/uploadChunk` | 上传分片 | `multipart: file, fileMd5, chunkIndex, totalChunks` | `{ received }` |
| POST | `/files/uploadMerge` | 合并分片 | `{ fileMd5, fileName, totalChunks }` | `{ fileId, fileMd5 }` |
| GET | `/files/uploadProgress` | 查询上传进度 | `?fileMd5=&totalChunks=` | `{ uploaded: [index, ...], progress }` |

> 注意：MinIO 未启用时，文件上传接口返回 503 `{ code: "MINIO_DISABLED" }`。

## 项目 / 语言 / 框架 / 模板

### ProjectController

路径前缀：`/api/v1/projects`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/projects` | 项目列表 |
| GET | `/projects/{projectId}` | 项目详情 |
| POST | `/projects` | 创建项目 |
| PUT | `/projects/{projectId}` | 更新项目 |
| DELETE | `/projects/{projectId}` | 删除项目 |

### LanguageController / FrameworkController

路径前缀：`/api/v1/languages`、`/api/v1/frameworks`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/languages` | 语言列表 |
| GET | `/frameworks` | 框架列表（`?languageId=` 可选） |

### TemplateController

路径前缀：`/api/v1/templates`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/templates` | 模板列表 |
| GET | `/templates/{templateId}` | 模板详情 |
| POST | `/templates` | 创建模板 |
| PUT | `/templates/{templateId}` | 更新模板 |

## 数据模型

### 请求 / 响应格式

所有请求和响应均为 JSON 格式。成功响应示例：

```json
{
  "changeId": "CHG-20260824-001",
  "title": "用户登录功能",
  "content": "## 用户故事\n...",
  "status": "drafting"
}
```

错误响应格式：

```json
{
  "code": "MINIO_DISABLED",
  "message": "MinIO 未启用，无法执行文件操作"
}
```

### HTTP 状态码

| 状态码 | 说明 |
|--------|------|
| 200 | 成功 |
| 201 | 创建成功 |
| 204 | 删除成功（无内容） |
| 400 | 参数错误 |
| 401 | 未认证 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 503 | 服务不可用（组件未启用） |