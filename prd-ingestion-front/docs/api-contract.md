# Harness Flow — API 契约文档

## 概述

本文档定义 Harness Flow（PRD Ingestion Platform）的前后端交互接口协议。
所有接口遵循 RESTful 风格，统一返回 `BaseResponse<T>` 格式。

## 通用约定

### 基础路径

```
/api/v1
```

### 鉴权

暂未实现用户认证，后续版本增加。

### 响应格式

```json
{
  "code": "OK",
  "message": "success",
  "data": {}
}
```

### 错误响应

```json
{
  "code": "PRD_PARSE_ERROR",
  "message": "PRD 解析失败，请检查文档格式",
  "traceId": "trace-xxx"
}
```

---

## 1. PRD 导入与解析

### 1.1 上传并解析 PRD

```
POST /api/v1/prd/ingest
Content-Type: application/json

{
  "content": "PRD 文档全文...",
  "format": "markdown",
  "title": "用户登录模块 PRD",
  "sourceUrl": "https://confluence.xxx/..."
}
```

**响应（202 异步）**：

```json
{
  "code": "OK",
  "message": "accepted",
  "data": {
    "ingestionId": "ing-20260820-001",
    "status": "pending",
    "title": "用户登录模块 PRD",
    "requirements": [],
    "dataEntities": [],
    "interfaces": [],
    "architectureDecisions": [],
    "documents": []
  }
}
```

### 1.2 查询解析进度

```
GET /api/v1/prd/ingest/{ingestionId}/progress
```

**响应**：

```json
{
  "code": "OK",
  "message": "success",
  "data": {
    "ingestionId": "ing-20260820-001",
    "status": "extracting",
    "progress": 65,
    "message": "正在提取接口协议..."
  }
}
```

### 1.3 获取解析结果

```
GET /api/v1/prd/ingest/{ingestionId}
```

**响应**：

```json
{
  "code": "OK",
  "message": "success",
  "data": {
    "ingestionId": "ing-20260820-001",
    "status": "completed",
    "title": "用户登录模块 PRD",
    "requirements": [
      {
        "id": "REQ-001",
        "description": "用户需要能够使用手机号和密码登录",
        "priority": "P0",
        "relatedEntities": ["User", "Session"],
        "sourceParagraph": "用户可以通过手机号和密码登录系统...",
        "notes": ""
      }
    ],
    "dataEntities": [
      {
        "name": "User",
        "description": "系统用户",
        "attributes": [
          { "name": "id", "type": "string", "description": "用户唯一标识" },
          { "name": "phone", "type": "string", "description": "手机号" },
          { "name": "password", "type": "string", "description": "加密密码" }
        ],
        "relations": [
          { "target": "Session", "type": "1:N", "description": "一个用户可以有多个会话" }
        ]
      }
    ],
    "interfaces": [
      {
        "method": "POST",
        "path": "/api/v1/auth/login",
        "summary": "用户登录",
        "requestBody": "{ phone: string, password: string }",
        "responseBody": "{ token: string, user: User }"
      }
    ],
    "architectureDecisions": [
      {
        "id": "AD-001",
        "title": "使用 JWT 作为认证方案",
        "context": "需要无状态认证，支持分布式部署",
        "decision": "采用 JWT + Refresh Token 方案",
        "consequences": ["需要实现 token 刷新机制", "token 过期时间需谨慎设置"],
        "status": "accepted"
      }
    ],
    "documents": [
      {
        "docId": "doc-bm-001",
        "type": "business-model",
        "title": "业务模型 - 用户登录模块",
        "status": "draft",
        "version": 1,
        "updatedAt": "2026-08-20T10:00:00Z"
      }
    ]
  }
}
```

### 1.4 重新解析

```
POST /api/v1/prd/ingest/{ingestionId}/reparse
```

---

## 2. 文档管理

### 2.1 获取文档列表

```
GET /api/v1/documents?ingestionId=ing-20260820-001
```

### 2.2 获取文档详情

```
GET /api/v1/documents/{docId}
```

### 2.3 更新文档内容

```
PUT /api/v1/documents/{docId}
Content-Type: application/json

{
  "content": "更新后的 Markdown 内容...",
  "changeLog": "修复了用户故事描述"
}
```

### 2.4 审批文档

```
POST /api/v1/documents/{docId}/approval
Content-Type: application/json

{
  "action": "approve",
  "comment": "内容准确，同意发布"
}
```

**响应**：

```json
{
  "code": "OK",
  "message": "success",
  "data": {
    "docId": "doc-bm-001",
    "status": "approved",
    "comment": "内容准确，同意发布"
  }
}
```

---

## 3. Change 管理

### 3.1 生成 change.md

```
POST /api/v1/changes
Content-Type: application/json

{
  "ingestionId": "ing-20260820-001",
  "docIds": ["doc-bm-001", "doc-dm-001", "doc-ip-001", "doc-ad-001"],
  "title": "用户登录模块实现"
}
```

### 3.2 获取 change 列表

```
GET /api/v1/changes?page=0&size=20
```

### 3.3 获取 change 详情

```
GET /api/v1/changes/{changeId}
```

---

## 4. 需求管理

### 4.1 获取需求列表（看板）

```
GET /api/v1/requirements?status=all
```

### 4.2 获取需求追溯树

```
GET /api/v1/requirements/{reqId}/trace
```

**响应**：

```json
{
  "code": "OK",
  "message": "success",
  "data": {
    "id": "REQ-001",
    "label": "用户登录功能",
    "type": "requirement",
    "children": [
      {
        "id": "doc-bm-001",
        "label": "business-model.md",
        "type": "document",
        "status": "approved",
        "children": [
          {
            "id": "C-001",
            "label": "Change: 用户登录模块",
            "type": "change",
            "status": "coding",
            "children": [
              { "id": "commit-001", "label": "feat: login API", "type": "commit", "url": "..." },
              { "id": "PR-001", "label": "PR #42", "type": "pr", "status": "reviewing", "url": "..." }
            ]
          }
        ]
      }
    ]
  }
}
```

---

## 5. 模板管理

### 5.1 获取模板列表

```
GET /api/v1/templates
```

### 5.2 获取模板详情

```
GET /api/v1/templates/{templateId}
```

### 5.3 更新模板

```
PUT /api/v1/templates/{templateId}
Content-Type: application/json

{
  "content": "模板内容...",
  "changeLog": "新增用户故事章节"
}
```

### 5.4 新建模板

```
POST /api/v1/templates
Content-Type: application/json

{
  "type": "business-model",
  "name": "业务模型模板",
  "description": "标准业务模型模板",
  "content": "# {{MODEL_NAME}}\n\n..."
}
```

---

## 6. 流水线

### 6.1 获取流水线状态

```
GET /api/v1/pipeline/{changeId}
```

### 6.2 获取变更日志

```
GET /api/v1/pipeline/{changeId}/logs
```

### 6.3 推动流水线

```
POST /api/v1/pipeline/{changeId}/advance
Content-Type: application/json

{
  "stage": "coding"
}
```

---

## 7. 看板统计

### 7.1 获取看板统计

```
GET /api/v1/kanban/stats
```

**响应**：

```json
{
  "code": "OK",
  "message": "success",
  "data": {
    "columns": {
      "prd_imported": { "count": 5, "label": "PRD 导入" },
      "document_review": { "count": 3, "label": "文档校验" },
      "design_review": { "count": 2, "label": "设计评审" },
      "coding": { "count": 4, "label": "编码中" },
      "testing": { "count": 1, "label": "测试中" },
      "completed": { "count": 6, "label": "已完成" }
    }
  }
}
```