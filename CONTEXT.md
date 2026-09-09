# 项目术语表（CONTEXT.md）

> 本技能包的共享上下文，记录项目特有的术语、约定、目录结构。
> 供 AI 在对话和开发中保持一致的语义理解。

## 项目概述

**tayama-harness-skills** 是一套开箱即用的 Harness Engineering 跨语言开发流水线技能包，支持 Java / Python / Go / Rust / Frontend 五种语言，涵盖 Spring Boot、Spring Cloud Alibaba、Spring MVC、Quarkus、Micronaut、Vert.x、Dropwizard、Dubbo、Spring AI、Spring AI Alibaba、AgentScope Java、LangChain4j、Semantic Kernel、Genkit Java、Django、FastAPI、Flask、Tornado、TensorFlow、PyTorch、Keras、scikit-learn、XGBoost、Hugging Face Transformers、LangChain、LangGraph、CrewAI、PydanticAI、SmolAgents、OpenAI Agents SDK、Gin、go-zero、Echo、Fiber、Chi、Beego、Go-Kit、Go-Kratos、Gorilla Mux、Kitex、Hertz、Iris、Macaron、Tango、GoFrame、LangChainGo、Google ADK-Go、cloudwego eino、tRPC-Agent-Go、Firebase Genkit、Anyi、Axum、Actix Web、Rocket、Warp、Poem、Loco、Salvo、Tauri、Iced、egui、Dioxus、Candle、Burn、tch-rs、ort、rlx-models、ADK-Rust、Blockcell、vLLM、React、Vue、Angular、Svelte、Next.js、Nuxt 等 80+ 主流框架和 Maven、Gradle、pip、poetry、uv、go mod、npm、pnpm、yarn、Vite、Webpack、Angular CLI、cargo 等主流构建工具。

## 术语表

| 术语 | 含义 | 备注 |
|------|------|------|
| Harness | 人类设计约束、AI 写代码、机器验证的开发方法论 | |
| Owner Agent | 应用负责人智能体，编排 6 阶段流水线 | 模板在 `skills/harness-core/templates/agents/owner.md` |
| SDD-TDD | 规格驱动开发 + 测试驱动开发 | 先写规格 → 测试 → 代码 |
| AC | 验收条件（Acceptance Criteria） | 每条 AC 对应一个测试 |
| 垂直切片 | 一次只做一个 Red-Green-Refactor 循环，不批量 | |
| 战争迷雾 | 只规划当前层，下一层做标记，迷雾层不碰 | |

## 目录结构

```
tayama-harness-skills/
├── .claude-plugin/          # Claude Code 插件配置
│   ├── plugin.json
│   └── marketplace.json
├── scripts/                 # 工具脚本
│   ├── list-skills.sh
│   └── sync-version.mjs
├── skills/
│   ├── apply-harness/       # 入口技能（/apply-harness）
│   ├── install-skill/       # 技能注册（/install-skill）
│   ├── harness-core/        # 核心骨架模板 + 10 个流水线技能模板（唯一事实源）
│   ├── harness-front/       # 前端语言规范包
│   ├── harness-golang/      # Go 语言规范包
│   ├── harness-java/        # Java 语言规范包
│   ├── harness-python/      # Python 语言规范包
│   └── harness-rust/        # Rust 语言规范包
├── CONTEXT.md
├── CHANGELOG.md
├── LICENSE
├── README.md
└── package.json
```

## 语言支持

| 语言 | 目录 | 支持框架 | 构建工具 |
|------|------|---------|---------|
| Java | `skills/harness-java/` | Spring Boot / Spring Cloud Alibaba / Spring MVC / Quarkus / Micronaut / Vert.x / Dropwizard / Dubbo / Spring AI / LangChain4j / Semantic Kernel / AgentScope / Genkit | Maven / Gradle |
| Python | `skills/harness-python/` | Django / FastAPI / Flask / Tornado / TensorFlow / PyTorch / Keras / scikit-learn / XGBoost / LangChain / LangGraph / CrewAI / PydanticAI / Hugging Face Transformers / OpenAI Agents SDK | pip / Poetry / uv |
| Go | `skills/harness-golang/` | Gin / go-zero / Echo / Fiber / Chi / Beego / Go-Kit / Go-Kratos / Gorilla Mux / Kitex / Hertz / Iris / GoFrame / LangChainGo / eino / ADK-Go / Genkit / Anyi | go mod |
| Rust | `skills/harness-rust/` | Axum / Actix Web / Rocket / Warp / Poem / Loco / Salvo / Tauri / Iced / egui / Dioxus / Candle / Burn / tch-rs / ort / rlx-models / ADK-Rust / Blockcell / vLLM | cargo |
| Frontend | `skills/harness-front/` | Vue 3 / React / Angular / Svelte / Next.js / Nuxt | Vite / Webpack / Angular CLI |

## 6 阶段流水线

```
① harnessing  →  ② coding-skill  →  ③ unit-test-write  →  ④ expert-reviewer  →  ⑤ unit-test-ci  →  ⑥ deploy-verify
```

---

> **使用方式**: AI 在对话开始时优先读取此文件以快速建立上下文。
> **技能注册**: 执行 `/apply-harness` 后，运行 `/install-skill` 将 `.harness/skills/` 下的技能注册到当前 AI 工具（支持 19+ 主流工具：reasonix / claude-code / cline / cursor / codex / qoder / trae / codebuddy / lingma / windsurf / copilot 等）可识别的技能目录，使 `/harnessing`、`/harness-me` 等斜杠命令可用。
> 发现新术语或决策时，应同步更新此文件。