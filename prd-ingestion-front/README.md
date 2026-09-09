# Harness Flow — PRD Ingestion Platform

> 面向产品经理与架构师的 PRD 前置转换工具——将 PRD 文档自动解析为结构化 wiki 文档，经人工校验后生成 change.md，进入 6 阶段流水线。

## 技术栈

| 类别 | 技术 |
|------|------|
| 框架 | Vue 3 + TypeScript |
| 构建 | Vite 8 |
| 样式 | Tailwind CSS v4 |
| 状态管理 | Pinia |
| 路由 | Vue Router 5 |
| 测试 | Vitest + vue-test-utils |

## 项目结构

```
prd-ingestion-front/
├── src/
│   ├── config/          # 全局配置（apiPrefix、useMock 开关等）
│   ├── constants/       # Mock 数据（完整的前端演示数据）
│   ├── models/          # 类型定义（base / prd / document / change / template / pipeline）
│   ├── services/        # API 服务层（prd / document / change / template / pipeline）
│   ├── stores/          # Pinia 状态管理（prd / document / change / template / pipeline）
│   ├── utils/           # 工具库（request.ts：统一 HTTP 请求封装）
│   ├── router/          # 路由配置
│   ├── components/      # 通用组件
│   ├── views/
│   │   ├── layouts/     # DefaultLayout（侧边栏 + 顶栏）
│   │   └── pages/       # 5 个功能页面
│   └── assets/styles/   # 全局样式
└── docs/
    └── api-contract.md  # 前后端 API 契约文档
```

## 5 个功能页面

| 页面 | 路径 | 目标用户 | 功能 |
|------|------|---------|------|
| PRD 导入 | `/prd/import` | 产品经理 | 拖拽上传/粘贴 PRD → LLM 解析 → 展示提取结果 |
| 校验面板 | `/review/:ingestionId` | 架构师/开发者 | 左右对比 PRD 原文 vs LLM 提取结果 → 行内编辑 → 审批 |
| 需求看板 | `/kanban` | 产品经理/管理者 | Kanban 视图 + 需求追溯树 |
| 模板管理 | `/templates` | 架构师 | 管理 4 类文档模板（业务模型/数据模型/接口协议/架构决策） |
| 流水线详情 | `/pipeline/:changeId` | 开发者 | 流水线进度条 + 关联文档 + 变更日志 + 验收标准 |

## 核心流程

```
PRD 上传 → LLM 解析 → 提取实体 → 生成 draft 文档 → 人工校验 → 审批通过 → 生成 change.md → 6 阶段流水线
```

## 开发

前后端一起起的完整步骤见仓库文档：[docs/prd-local-start.md](../docs/prd-local-start.md)。

```bash
# 安装依赖
npm install

# 启动开发服务器（默认端口 5174，/api 代理到 localhost:8080）
npm run dev

# 类型检查
npm run type-check

# 生产构建
npm run build

# 运行测试
npm test
```

## Mock 模式

`src/config/index.ts` 中 **`useMock` 当前为 `false`**，开发时需同时启动 `prd-ingestion-server`。

若改为 `true`，则走 `src/constants/mock.ts`，无需后端即可点页面演示。

## 前后端交互

完整 API 契约见 [docs/api-contract.md](docs/api-contract.md)。主要接口分组：

- **PRD 导入与解析**: `POST /api/v1/prd/ingest`, `GET /api/v1/prd/ingest/{id}`
- **文档管理**: `GET/PUT /api/v1/documents/{docId}`, `POST /api/v1/documents/{docId}/approval`
- **Change 管理**: `POST /api/v1/changes`, `GET /api/v1/changes/{changeId}`
- **模板管理**: `GET/PUT/POST /api/v1/templates`
- **流水线**: `GET /api/v1/pipeline/{changeId}`, `POST /api/v1/pipeline/{changeId}/advance`
- **需求追溯**: `GET /api/v1/requirements/{reqId}/trace`
- **看板统计**: `GET /api/v1/kanban/stats`