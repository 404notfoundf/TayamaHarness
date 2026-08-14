# Harness Engineering 技能包文档

> 开箱即用的 Harness Engineering 跨语言开发流水线技能包 — 文档中心

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

## 语言规范

| 语言 | 基线规范 | 文档 |
|------|---------|------|
| Java | 阿里巴巴 Java 开发手册 + Spring Boot | [文档](languages/java.md) |
| Python | PEP 8 + Google Style + Flask/FastAPI | [文档](languages/python.md) |
| Go | Go Code Review + Uber Style + go-zero/Gin | [文档](languages/golang.md) |
| Rust | Rustfmt + Clippy 严格模式 + Axum/Actix Web | [文档](languages/rust.md) |
| PHP | PSR-12 + PHPStan level 8 + Laravel/ThinkPHP | [文档](languages/php.md) |
| Frontend | Vue 3 + TypeScript 严格模式 + Vite | [文档](languages/frontend.md) |