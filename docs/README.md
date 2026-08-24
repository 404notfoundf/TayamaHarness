# Harness Engineering 技能包文档

> 开箱即用的 Harness Engineering 跨语言开发流水线技能包 — 文档中心
>
> 本仓库同时包含 **PRD 智能解析平台**（Harness Flow）的完整参考实现，该平台是 Harness Engineering 方法论在 PRD 文档处理领域的落地实践。

## 目录

- [Harness Engineering 总览](harness-overview.md) — 核心概念
- [Owner Agent](owner-agent.md) — 应用负责人智能体
- [SDD-TDD 方法论](sdd-tdd.md) — 规格驱动 + 测试驱动
- [6 阶段流水线](6-stage-pipeline.md) — 从需求到交付
- [变更管理](change-management.md) — 状态机与文档模板
- [语言规范](languages/) — 各语言编码标准

## 核心概念

| 文档 | 说明 |
|------|------|
| [Harness Engineering 总览](harness-overview.md) | 什么是 Harness、核心哲学、6 阶段流水线概览 |
| [Owner Agent](owner-agent.md) | 应用负责人智能体——流水线的编排者 |
| [SDD-TDD 方法论](sdd-tdd.md) | 规格驱动 + 测试驱动——三层关系与工作模式 |
| [6 阶段流水线](6-stage-pipeline.md) | 从需求到交付的完整流水线，阶段门禁与状态机 |
| [变更管理](change-management.md) | 变更状态机、文档模板、追踪机制 |

## 流水线技能

| 阶段 | 技能 | 文档 |
|------|------|------|
| ① 需求分析 | `harnessing` | [文档](skills/harnessing.md) |
| ② 编码实现 | `coding-skill` | [文档](skills/coding-skill.md) |
| ③ 单元测试编写 | `unit-test-write` | [文档](skills/unit-test-write.md) |
| ④ 专家评审 | `expert-reviewer` | [文档](skills/expert-reviewer.md) |
| ⑤ CI 门禁 | `unit-test-ci` | [文档](skills/unit-test-ci.md) |
| ⑥ 部署验证 | `deploy-verify` | [文档](skills/deploy-verify.md) |

## 辅助技能

| 技能 | 用途 | 文档 |
|------|------|------|
| `harness-me` | 需求打磨入口 | [文档](skills/harness-me.md) |
| `harnessing` | 需求拷问引擎 | [文档](skills/harnessing.md) |
| `diagnosing-bugs` | Bug 诊断 | [文档](skills/diagnosing-bugs.md) |
| `handoff` | 上下文交接 | [文档](skills/handoff.md) |
| `arch-review` | 架构体检 | [文档](skills/arch-review.md) |
| `domain-modeling` | 领域语言维护 | [文档](skills/domain-modeling.md) |
| `research` | 外部事实查证 | [文档](skills/research.md) |
| `resolving-merge-conflicts` | 解决合并冲突 | [文档](skills/resolving-merge-conflicts.md) |

## 初始化技能

| 技能 | 用途 | 文档 |
|------|------|------|
| `apply-harness` | 一键应用 Harness 开发规范 | [文档](skills/apply-harness.md) |
| `install-skill` | 注册技能到当前 AI 工具 | [文档](skills/install-skill.md) |

## 工具包技能（自动注入）

| 技能 | 说明 |
|------|------|
| `database-migration-toolkit` | 数据库迁移模板与回滚 |
| `eventbus-toolkit` | 事件总线封装 |
| `excel-toolkit` | Excel 导入导出 |
| `http-client-toolkit` | HTTP 客户端连接池与重试 |
| `k8s-release-toolkit` | K8s 发布与灰度 |
| `kafka-toolkit` | Kafka 生产消费封装 |
| `logging-toolkit` | 日志脱敏与 MDC traceId |
| `oss-toolkit` | 对象存储统一接口 |
| `performance-toolkit` | 性能诊断工具 |
| `redis-cache-wrapper` | 多级缓存防护 |
| `rocketmq-toolkit` | RocketMQ 事务消息 |
| `scheduler-toolkit` | 分布式调度 |
| `security-toolkit` | 安全脱敏与鉴权 |

详见 [工具包技能总览](skills/toolkit-skills.md) | Java 框架专属技能（`java-code-review`、`spring-api-convention`、`mybatis-toolkit`、`openfeign-toolkit`）

## 语言规范

| 语言 | 基线规范 | 文档 |
|------|---------|------|
| Java | 阿里巴巴 Java 开发手册 + Spring Boot | [文档](languages/java.md) |
| Python | PEP 8 + Google Style + Flask/FastAPI | [文档](languages/python.md) |
| Go | Go Code Review + Uber Style + go-zero/Gin | [文档](languages/golang.md) |
| Rust | Rustfmt + Clippy 严格模式 + Axum/Actix Web | [文档](languages/rust.md) |
| PHP | PSR-12 + PHPStan level 8 + Laravel/ThinkPHP | [文档](languages/php.md) |
| Frontend | Vue 3 + TypeScript 严格模式 + Vite | [文档](languages/frontend.md) |

---

## PRD 智能解析平台（Harness Flow）

本仓库包含 **PRD 智能解析平台** 的完整参考实现，作为 Harness Engineering 方法论在 PRD 文档解析领域的落地实践。

### 架构设计

| 文档 | 说明 |
|------|------|
| [系统架构图](arch/01-项目架构设计.svg) | 前后端架构、服务分层、外部依赖 |
| [4 文档拆分算法](arch/02-4文档拆分核心算法设计.svg) | 三层混合解析策略：模板 → LLM → 章节算法 |
| [Change 算法](arch/03-change核心算法设计.svg) | ChangeComposer 合成器 + 流水线生命周期 |
| [4 文档拆分算法详解](4-document-split-algorithm.md) | 章节树构建、分类器加权评分、提取器实现细节 |
| [Change 算法详解](change-core-algorithm.md) | ChangeComposer 8 模块、流水线推进、看板映射 |
| [平台架构详解](prd-platform-architecture.md) | 架构设计文档（文字版） |

### 使用指南

| 文档 | 说明 |
|------|------|
| [使用教程](prd-usage-tutorial.md) | 图文并茂的完整使用教程（2000+ 字） |
| [API 接口文档](prd-platform-api.md) | 后端 REST API 完整参考 |
| [变更管理（PRD 平台）](change-management-prd.md) | PRD 平台的变更管理、流水线、看板机制 |

### 流程截图

系统各页面截图见 [`docs/flow/`](flow/) 目录，共 17 张截图覆盖所有核心页面。