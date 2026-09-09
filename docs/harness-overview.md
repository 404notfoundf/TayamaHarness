# Harness Engineering 总览

## 什么是 Harness？

**Harness** 是一个**人类设计约束、AI 写代码、机器验证**的开发方法论。它的名字来自"缰绳"——人类通过规则和约束来引导 AI，而不是让 AI 自由发挥。

### 核心哲学

```
人类设计约束          AI 沿流水线生产          机器验证每一步
     ↓                      ↓                      ↓
  .harness/rules/     .harness/skills/       阶段门禁 + CI
```

三个核心原则：

1. **人类设计约束** — 人类定义"怎么做"的规则（编码规范、工程结构、开发流程），AI 在规则框架内执行
2. **AI 写代码** — AI 按照流水线阶段依次完成需求分析、编码、测试、审查、部署
3. **机器验证** — 每个阶段都有出口门禁，不满足条件不能进入下一阶段

### 为什么要用 Harness？

| 问题 | Harness 的解法 |
|------|---------------|
| AI 生成的代码质量不稳定 | 5 条规则强制约束编码规范、工程结构、SDD-TDD |
| AI 经常"想当然"地增加需求外功能 | 严格按 change.md 规格实现，禁止越界 |
| 缺乏可追溯性 | 全程变更留档，change.md 状态机驱动 |
| 测试覆盖率不够 | 强制 TDD，先写失败测试再做实现，覆盖率 ≥80% |
| 代码审查流于形式 | 专家评审技能逐项检查，0 个严重问题才放行 |
| 上下文丢失 | 变更追踪 + 交接文档 + 共享术语表 |

## 核心组件

| 组件 | 说明 |
|------|------|
| **Owner Agent** | 应用负责人智能体，编排 6 阶段流水线，定义项目身份 |
| **Rules（规则）** | 5 条规则：编码规范、工程结构、SDD-TDD 模式、开发流程、运行时可靠性 |
| **Skills（技能）** | 9 个技能：6 个流水线技能 + 3 个辅助技能 |
| **Changes（变更）** | 变更卡 + 评审报告 + 验证报告，状态机驱动 |
| **Wiki（知识库）** | 业务模型 + 接口协议 + 数据模型 + 架构决策 |
| **CONTEXT.md** | 共享语言机制，AI 与人类间的术语表 |

## 6 阶段流水线

```
① harnessing  →  ② coding-skill  →  ③ unit-test-write  →  ④ expert-reviewer  →  ⑤ unit-test-ci  →  ⑥ deploy-verify
```

详见 [6 阶段流水线](6-stage-pipeline.md) 文档。

## 支持的语言

| 语言 | 基线规范 | 测试框架 | 代码规范 |
|------|---------|---------|---------|
| **Java** | Alibaba Java 手册 | JUnit 5 + Mockito | Checkstyle + PMD |
| **Python** | PEP 8 + Google Style | pytest + pytest-mock | flake8 + mypy + black |
| **Go** | Go Code Review + Uber Style | go test + testify | golangci-lint + go vet |
| **Frontend** | Vue 3 + TypeScript 严格模式 | Vitest + @vue/test-utils | ESLint + Prettier + vue-tsc |

## 快速开始

```bash
# 在任意项目根目录执行
npx skills@latest add https://github.com/404notfoundf/TayamaHarness.git

# 在 AI 对话中键入
/apply-harness
```

AI 自动检测项目语言，生成 `.harness/` 目录并应用完整规范体系。

## 参考实现：PRD 智能解析平台

本仓库包含一个完整的参考实现——**PRD 智能解析平台（Harness Flow）**，展示了 Harness Engineering 方法论在 PRD 文档处理领域的落地。

### 架构亮点

| 维度 | 说明 |
|------|------|
| **前端** | Vue 3 + TypeScript + Pinia，7 个页面组件覆盖全部业务流程 |
| **后端** | Spring Boot 3.4 + JDK 21，12 个 REST 控制器 + 8 个业务服务 |
| **解析引擎** | 三层混合策略：模板匹配 → LLM 解析 → 章节层级算法（兜底） |
| **Change 合成** | 8 个正交模块从 4 文档合成 Change.md，宁缺毋滥 |
| **可开关组件** | AI 和 MinIO 可通过配置关闭，系统以降级模式运行 |
| **看板** | 4 列看板 + 分页 + 操作日志时间线 + 文档详情子弹窗 |

### 相关文档

- [PRD 平台架构详解](prd-platform-architecture.md)
- [PRD 平台变更管理](change-management-prd.md)
- [PRD 平台 API 文档](prd-platform-api.md)
- [使用教程](prd-usage-tutorial.md)
- [架构图](arch/01-项目架构设计.svg)
- [4 文档拆分算法](arch/02-4文档拆分核心算法设计.svg)
- [Change 算法](arch/03-change核心算法设计.svg)