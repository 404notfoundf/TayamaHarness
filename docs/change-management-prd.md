# PRD 平台变更管理（Harness Flow）

> 本文档描述 PRD 智能解析平台的变更管理机制，包括流水线状态机、数据库模型、ChangeComposer 合成算法和看板映射。

## 流水线状态机

每个 Change 通过 `prd_pipeline_stage` 表记录的阶段，驱动状态流转。状态机如下：

```
drafting ──▶ reviewing ──▶ approved ──▶ completed
   ↑              │              │
   └──────────────┴──────────────┘
   (可回退：重新生成/编辑)
```

| 阶段 | 说明 | 可否编辑 | 可否重新生成 |
|------|------|---------|------------|
| `drafting` | 草案阶段，Change 由 4 文档生成 | ✅ | ✅ |
| `reviewing` | 评审阶段，Change 内容冻结 | ✅ | ✅ |
| `approved` | 已批准，可进入编码 | ❌ | ❌ |
| `completed` | 已完成，变更交付 | ❌ | ❌ |

### 推进操作

- **Advance（推进）**：将 Change 从当前阶段推进到下一阶段
- **Regenerate（重新生成）**：在 `drafting`/`reviewing` 阶段，调用 ChangeComposer 重新从 4 文档合成 Change 内容（覆盖旧内容，不创建新 ChangeId）
- **Edit（编辑）**：在 `drafting`/`reviewing` 阶段，手动编辑 Change 内容

## 数据库模型

### 核心表

| 表名 | 说明 | 关键字段 |
|------|------|---------|
| `prd_change` | Change 主表 | `id`（自增主键）、`change_id`（业务唯一 ID）、`title`、`description`、`content`（Markdown 全文） |
| `prd_change_document_ref` | 关联文档 | `change_id`、`doc_id`、`doc_type`（business_model/data_model/interface_protocol/architecture_decision） |
| `prd_change_acceptance_criteria` | 验收标准 | `change_id`、`content`、`status`（pending/passed/failed） |
| `prd_pipeline_stage` | 流水线阶段记录 | `change_id`、`stage`（drafting/reviewing/approved/completed）、`operator`、`comment` |
| `prd_change_log` | 操作日志 | `change_id`、`action`、`operator`、`detail`、`created_at` |

### 核心关系

```
prd_ingestion (PRD 文档)
    │
    ▼
4 篇 Wiki 文档 ──▶ ChangeComposer ──▶ prd_change
                                           │
                                           ├── prd_change_document_ref (关联文档)
                                           ├── prd_change_acceptance_criteria (AC)
                                           ├── prd_pipeline_stage (阶段流水线)
                                           └── prd_change_log (操作日志)
```

## ChangeComposer 合成算法

ChangeComposer 是一个纯 Java 规则引擎，从 4 篇文档合成 Change.md 内容。包含 8 个正交合成模块：

| 模块 | 方法 | 输入来源 | 输出内容 |
|------|------|---------|---------|
| 用户故事 | `composeUserStory()` | 业务模型·需求列表 | 用户故事 + 总括句，噪音过滤 |
| 非目标 | `composeOutOfScope()` | PRD 原文 | 显式声明不在此次变更范围内的事项 |
| 验收标准 | `composeAcceptanceCriteria()` | 业务模型·需求描述 | 每条需求对应 1 条 AC |
| 边界情况 | `composeEdgeCases()` | 数据模型·字段约束 | 从字段类型/长度/枚举推导边界 |
| 非功能需求 | `composeNonFunctional()` | 架构决策 + 接口协议 | 性能/安全/可用性/可维护性 |
| 测试策略 | `composeTestStrategy()` | 接口协议 + 边界情况 | 单元/集成/端到端测试策略 |
| 任务拆解 | `composeTaskBreakdown()` | 数据模型·实体 + 需求 | 逐条任务（前端/后端/数据库） |
| 验收用例 | `composeTestCases()` + `composeProgress()` | 全部 4 文档 | 具体验收用例 + 流水线进度 |

### 设计原则

1. **正交独立**：每个模块的输出不依赖其他模块的输出，推导不出则返回空值
2. **噪音过滤**：自动过滤 base64 图片、纯表格行、数据 URI 等噪音内容
3. **可回溯**：每个推导条目附带出处（REQ 编号 / 接口路径 / 架构决策标题）
4. **宁缺毋滥**：推导不出的内容留空，不编造
5. **幂等覆盖**：重新生成覆盖旧内容，不创建新 ChangeId

## 看板映射

需求看板将 Change 的流水线状态映射到 4 列：

| 看板列 | 包含的流水线阶段 | 说明 |
|--------|---------------|------|
| 📋 PRD 导入 | `prd_imported`（导入后未生成 Change） | 新导入的 PRD 文档 |
| 📝 文档校验 | `reviewing` | Change 正在评审 |
| 🔍 设计评审 | `approved`、`coding`、`testing`、`verifying` | 已批准或在编码/测试/验证中 |
| ✅ 已完成 | `completed` | 变更已完成交付 |

> 注意：`coding` / `testing` / `verifying` 阶段在旧版本中存在，为兼容旧数据，它们被归入「设计评审」列展示，不会丢失数据。

### 看板分页

每列最多展示 10 张卡片，底部有分页控件（上一页 / 下一页），防止卡片过多时展示不全。

## 操作日志

每次对 Change 的操作（创建、推进、重新生成、编辑、批准）都会记录到 `prd_change_log` 表，在需求看板弹窗中展示为时间线，包含：

- 操作类型（`action`）
- 操作人（`operator`）
- 详细描述（`detail`）
- 操作时间（`created_at`）

## 关联文档

每个 Change 关联 4 篇 Wiki 文档：

- 📋 **业务模型**（`business_model`）
- 💾 **数据模型**（`data_model`）
- 🔌 **接口协议**（`interface_protocol`）
- 🏗️ **架构决策**（`architecture_decision`）

在需求看板弹窗中，关联文档可点击打开子弹窗查看详情，子弹窗关闭后恢复父弹窗。