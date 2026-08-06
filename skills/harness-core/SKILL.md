---
name: harness-core
description: Harness 核心骨架模板 — 参数化 owner.md、changes 模板、wiki 模板（被 apply-harness 技能调用）
---

# Harness 核心骨架

本技能提供跨语言通用的 Harness 骨架模板，包括：

- `agents/owner.md` — 参数化的 Owner Agent 定义（含 `{{LANGUAGE}}` 等占位符）
- `changes/_TEMPLATE/` — 变更追踪模板（change.md / review.md / verify.md）
- `wiki/` — 领域知识文档模板（业务模型 / 接口协议 / 数据模型 / 架构决策）

被 `apply-harness` 技能在初始化项目时调用。