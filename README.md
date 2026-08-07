# huazai-harness-skills

> 开箱即用的 Harness Engineering 跨语言开发流水线技能包
>
> 参考 [mattpocock/skills](https://github.com/mattpocock/skills) 的 SKILL.md 体系开发

一键安装：`npx skills@latest add huazai/harness-skills`

## 什么是 Harness Engineering？

Harness 是一个**人类设计约束、AI 写代码、机器验证**的开发方法论。核心是 **Owner Agent**（应用负责人智能体），它编排 6 阶段流水线：

```
① request-analysis  →  ② coding-skill  →  ③ unit-test-write  →  ④ expert-reviewer  →  ⑤ unit-test-ci  →  ⑥ deploy-verify
```

## 快速开始

```bash
# 在任意项目根目录执行
npx skills@latest add huazaiteam/harness-skills

# 在 AI 对话中键入
/apply-harness
```

AI 自动检测项目语言（Java / Python / Go），生成 `.harness/` 目录：
- **Owner Agent** — 应用负责人智能体（灵魂，定义你是谁、怎么工作）
- **Rules** — 5 条规则（SDD-TDD / 编码规范 / 工程结构 / 开发流程 / 运行时可靠性）
- **Skills** — 14 个技能（6 流水线 + 3 通用辅助 + 3 场景辅助 + 2 新增）
- **Changes** — 变更追踪模板
- **Wiki** — 领域知识库模板
- **CONTEXT.md** — 共享语言机制（AI 与人类间的术语表）

## 支持的语言

| 语言 | 基线规范 | 测试框架 | 代码规范 | 结构规范 |
|------|---------|---------|---------|---------|
| **Java** | Alibaba Java 手册 | JUnit 5 + Mockito | Checkstyle + PMD | Maven 多模块 + Spring Boot |
| **Python** | PEP 8 + Google Style | pytest + pytest-mock | flake8 + mypy + black | Flask/FastAPI 分层架构 |
| **Go** | Go Code Review + Uber Style | go test + testify | golangci-lint + go vet | go-zero/Gin 微服务布局 |
| **Frontend** | Vue 3 + TypeScript 严格模式 | Vitest + @vue/test-utils | ESLint + Prettier + vue-tsc | Vite + Pinia + Vue Router |

## 技能清单

| 技能 | 阶段 | 一句话摘要 |
|------|------|-----------|
| `/harness-me` | 需求打磨 | 一场"灵魂拷问"式对话，帮你把模糊需求打磨到可落地 |
| `/harnessing` | 需求拷问引擎 | 一次只问一个问题，沿决策树推进，输出需求总结卡片 |
| `request-analysis` | ① 需求分析 | 规格构建、AC 可测试、边界 ≥3 |
| `coding-skill` | ② 编码实现 | 先写失败测试 → 最小实现 → 重构，垂直切片不批量 |
| `unit-test-write` | ③ 单测编写 | 每条 AC 一个测试，覆盖率 ≥80%，不测 happy path |
| `expert-reviewer` | ④ 专家评审 | 双轴评审（Spec + Standards），0 个 🔴 才放行 |
| `unit-test-ci` | ⑤ CI 门禁 | 机械化执行，任一检查失败即红灯 |
| `deploy-verify` | ⑥ 部署验证 | "CI 绿"≠"线上可用"，确认健康检查+链路+回滚 |
| `domain-modeling` | 通用辅助 | 主动维护领域模型，术语敲定当场写 CONTEXT.md / ADR |
| `research` | 通用辅助 | 对一手来源调研，结果落 wiki |
| `resolving-merge-conflicts` | 通用辅助 | 解决 git merge/rebase 冲突，保留双方意图 |
| `/diagnosing-bugs` | Bug 诊断 | 先建反馈循环再猜原因，6 阶段严谨流程 |
| `/handoff` | 上下文交接 | 压缩对话上下文为交接文档，无缝续接 |
| `/arch-review` | 架构体检 | 扫描浅模块，生成 Mermaid 报告，逐一打磨 |

## 核心设计理念

1. **Owner Agent 是灵魂** — 它定义"你是谁、怎么工作、怎么决策"
2. **方法论跨语言通用** — SDD-TDD、6 阶段流水线、变更状态机、人机协同协议
3. **技术栈语言特有** — 编码规范、工程结构、工具链按语言独立
4. **一键迁移** — 在任意项目执行 `/apply-harness` 即可应用完整规范
5. **垂直切片** — 一次只做一个 Red-Green-Refactor 循环，不批量
6. **战争迷雾** — 只规划当前层，下一层做标记，迷雾层不碰

## 项目结构

```
huazai-harness-skills/
├── .claude-plugin/
│   ├── plugin.json                # 插件清单
│   └── marketplace.json           # 插件市场元数据
├── docs/                          # 文档中心（📖 从这里开始学习）
│   ├── README.md                  # 文档索引
│   ├── harness-overview.md        # Harness Engineering 总览
│   ├── owner-agent.md             # Owner Agent 概念
│   ├── sdd-tdd.md                 # SDD-TDD 方法论
│   ├── 6-stage-pipeline.md        # 6 阶段流水线
│   ├── change-management.md       # 变更管理
│   ├── skills/                    # 各技能文档
│   └── languages/                 # 各语言规范文档
├── scripts/
│   ├── list-skills.sh             # 列出所有技能
│   └── sync-version.mjs           # 同步版本号
├── skills/
│   ├── apply-harness/             # ★ 入口技能（/apply-harness）
│   ├── harness-core/              # 核心骨架模板 + 通用技能
│   │   ├── templates/
│   │   │   ├── agents/owner.md    # Owner Agent 模板（参数化）
│   │   │   ├── changes/_TEMPLATE/ # 变更卡/评审/验证模板
│   │   │   ├── wiki/              # 领域知识库模板（含 ADR-FORMAT）
│   │   │   ├── CONTEXT.md         # 领域语言词典模板
│   │   │   └── CONTEXT-FORMAT.md  # CONTEXT.md 编写规范
│   │   └── skills/                # 技能模板 + 跨语言通用技能
│   │       ├── domain-modeling/   # 领域语言维护
│   │       ├── research/          # 外部事实查证
│   │       ├── resolving-merge-conflicts/ # 合并冲突解决
│   │       ├── harness-me/        # ⚙ 模板化技能（被 apply-harness 渲染）
│   │       ├── handoff/           # ⚙ 模板化技能
│   │       ├── diagnosing-bugs/   # ⚙ 模板化技能
│   │       ├── coding-skill/      # ⚙ 模板化技能
│   │       └── unit-test-write/   # ⚙ 模板化技能
│   ├── harness-java/              # Java 语言规范包
│   │   ├── rules/                 # 5 条规则
│   │   ├── skills/                # 9 个技能
│   ├── harness-python/            # Python 语言规范包（同上）
│   ├── harness-golang/            # Golang 语言规范包（同上）
│   └── harness-front/             # Frontend 语言规范包（同上）
├── CONTEXT.md                     # 项目共享上下文
├── CHANGELOG.md                   # 变更日志
├── LICENSE                        # MIT 许可证
├── README.md                      # 本文件
└── package.json                   # npm 包配置
```

## 学习路径

建议按以下顺序阅读文档：

1. **[Harness Engineering 总览](docs/harness-overview.md)** — 了解核心概念和哲学
2. **[Owner Agent](docs/owner-agent.md)** — 了解应用负责人智能体
3. **[SDD-TDD 方法论](docs/sdd-tdd.md)** — 了解规格驱动 + 测试驱动开发
4. **[6 阶段流水线](docs/6-stage-pipeline.md)** — 了解完整流水线
5. **[变更管理](docs/change-management.md)** — 了解变更状态机
6. **语言规范** — 选择你的语言：[Java](docs/languages/java.md) / [Python](docs/languages/python.md) / [Go](docs/languages/golang.md) / [Frontend](docs/languages/frontend.md)
7. **各技能文档** — 在 `docs/skills/` 目录下

> 📖 **完整文档中心**: [docs/README.md](docs/README.md)

## 许可证

MIT
