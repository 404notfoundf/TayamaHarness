---
name: harness-front
description: Vue 前端语言规范包 — 编码规范、工程结构、运行时可靠性（SDD-TDD/开发流程共享自 harness-core）
---

# Harness Front — Vue 前端语言规范包

本包为 Vue 前端项目提供完整的 Harness 开发规范体系，基于：

- **基线框架**: Vue 3.x+ / Vite 8.x+
- **状态管理**: Pinia
- **路由**: Vue Router
- **UI 库**: Element Plus / Arco Design / Tailwind CSS（项目自选）
- **CSS 方案**: Tailwind CSS / Less / Sass（项目自选）
- **测试框架**: Vitest + @vue/test-utils + jsdom
- **代码规范**: ESLint + Prettier + TypeScript严格模式
- **HTTP 请求**: Axios / fetch 封装

包含 rules（3 个语言特有 + 2 个通用来自 harness-core）和 skills（9 个），与 `apply-harness` 入口技能配合使用。

> **辅助技能**: `/harness-me`（需求打磨）、`/diagnosing-bugs`（Bug 诊断）、`/handoff`（上下文交接）、`/arch-review`（架构体检）——在流水线各阶段按需调用。