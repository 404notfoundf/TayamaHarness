# 工程纪律即架构：拆解跨语言 Harness 脚手架的设计哲学

> 人类设计约束，AI 编写代码，机器验证质量。
> *—— Harness Engineering 的信条*

## 目录

- [一、前言](#一前言)
- [二、为什么要开发这个脚手架](#二为什么要开发这个脚手架)
- [三、项目整体架构设计](#三项目整体架构设计)
- [四、项目核心设计](#四项目核心设计)
- [五、模块核心设计](#五模块核心设计)
- [六、每个命令背后的核心设计](#六每个命令背后的核心设计)
- [七、多语言 / 框架识别支持](#七多语言--框架识别支持)
- [八、多种 AI 工具识别、注册与安装技能包](#八多种-ai-工具识别注册与安装技能包)
- [九、结语](#九结语)

---

## 一、前言

2026 年，AI Agent 真正进入落地爆发期。会使用大模型、调 API 的工程师很多，但真正能把「多 Agent 协同」「自主决策」「Skills 技能封装」「工程化约束」和「可交付代码」结合起来的人并不多。谁能把这几件事打通，谁就不再只是「只会接模型的人」，而是在向「Agent 架构师」进行升级。

下面我就结合正在落地的一套跨语言 Harness Engineering 脚手架（huazai-harness-skills），手把手拆开讲清楚：一套从 Java 专属 Harness 中脱胎、能约束 AI 在工程纪律内交付可维护代码的跨语言基础设施，到底应该怎么从 0 到 1 设计、分模块研发、逐步交付。

项目的核心定位是：

> **跨语言 Harness Engineering 脚手架**——Java / Python / Go / Rust / PHP / Frontend 六种语言、111 框架差异块自动识别，SDD-TDD 思想 + Owner Agent 智能编排 + 10 个模板化流水线技能，让「人类设计约束、AI 写代码、机器验证」在任何技术栈上开箱即用。

我会分成两大部分来讲：

- **整体篇**：先看清这套脚手架的总架构、方法论、工程约束和流水线
- **分模块篇**：再把每个模块拆开，讲它为什么存在、怎么设计、如何协同

这篇文章不是一篇"怎么配置 .harness"的教程，也不是"如何用 AI 写代码"的指南，而是一篇**工程决策记录**（Architecture Decision Record）——记录每个设计取舍背后的"为什么这样设计，而不是那样设计"。

> 如果你正在用 AI 写代码，并且觉得"AI 写的东西质量不可控"——这篇文章就是为你写的。

---

## 二、为什么要开发这个脚手架

### 2.1 真实起源：huazai-trip-plan 的"Java 专属 Harness"

这个脚手架不是凭空设计出来的。它的起源，是一个叫 **huazai-trip-plan** 的 Java 项目——**它本身就带着一套完整的 v1 版 Harness 工程规范**。

在从零搭建 huazai-trip-plan（AgentScope 自主旅游规划项目）的过程中，harness 规范与代码是同步生长的。目标从一开始就很明确：**让 AI 在工程约束下写代码，而不是自由发挥。**

这套规范由四部分组成：

1. **一套工程规则集**：`.harness/rules/` 下的 6 条规则——4 条通用（SDD-TDD 模式 / 变更定位 / 开发流程规范 / 运行时可靠性）+ 2 条语言特有（工程结构 / 编码规范）
2. **一组开发技能**：`.harness/skills/` 下 30 个可执行技能——`{lang}/` 内 10 个模板化流水线技能（harnessing / harness-me → coding-skill → unit-test-write → expert-reviewer → unit-test-ci → deploy-verify，另含 arch-review / diagnosing-bugs / handoff）由参数表渲染生成，`common/` 内 16 个跨语言通用技能，把"需求引导 → 编码实现 → 单元测试 → 专家评审 → CI 门禁 → 部署验证"的流水线变成了可执行的斜杠命令
3. **一个变更追踪体系**：`.harness/changes/` 下 18 个变更（C-001 ~ C-018），每个变更都有 change.md / review.md / verify.md，需求卡片、审查报告、验证结果全程可追溯
4. **一个领域知识库**：`.harness/wiki/` 下的业务模型、接口协议、数据模型、架构决策，沉淀多智能体系统的共享知识

这套体系在 huazai-trip-plan 上完整交付了一个 AgentScope 多智能体项目，AI 写代码的质量稳定了、代码风格统一了、需求与变更全程留档。**但它有一个根本性的问题：它只对 Java 有效——`mvn` 构建、JUnit 测试、Spring Boot 结构全部写死。**

### 2.2 问题暴露：换一种语言，整套规范就失效

huazai-trip-plan 验证了 harness 方法论的价值，但当我们把同样的工程纪律用到 Go、Python、前端项目上时，发现根本复用不了：

- `coding-skill` 里写死了 `mvn compile` 和 `mvn test`
- 代码规范检查用的是 `checkstyle`
- 测试框架用的是 `JUnit 5`
- 构建工具用的是 `Maven`
- 依赖管理用的是 `pom.xml`

这些对于 Go、Python、前端项目来说完全是"外星语言"。需求分析、编码、审查、门禁这些流程在每个语言的项目上都同样需要，但**承载它们的技能却和 Java 生态焊死在了一起**。

我们面临着两个选择：

1. **为每种语言重写一套技能**——复制 huazai-trip-plan 的结构，把 Java 特定的命令换成对应语言的命令
2. **把技能参数化**——让同一套技能根据检测到的语言/框架，自动使用不同的命令

如果选 1，意味着每增加一个语言（Python、Go、前端、Rust……），就要重写一套。而且维护 4 套技能，改了 Java 的规则还要手动同步到其他语言的版本。

**选 1 是死路。选 2 是唯一出路。**

但为什么参数化可行？我们重新拆解了 huazai-trip-plan 中的 harness 文件，一个关键结构浮现出来：

- 大约 60% 的规则是**跨语言通用**的：测试覆盖率门禁、编码规范约束、代码审查流程、门禁卡点
- 只有 40% 是**语言/框架特定**的：构建工具（Maven/Gradle）、测试框架（JUnit/Mockito）、风格检查（Checkstyle/SpotBugs）

也就是说，**核心的工程纪律是跨语言的，只是"用什么工具执行"不同。** 那为什么整套方案却只能用于 Java？答案藏在实现方式里：那些"通用规则"在技能文件中被硬编码了——"测试覆盖率 ≥ 80%" 这条规则听起来是通用的，但它的实现（`mvn test -Dcoverage`）却绑死了 Java + Maven。

于是解法浮出水面：**把"规则"和"实现"解耦。**

- 规则本身是跨语言的："测试覆盖率 ≥ 80%"
- 实现是语言/框架绑定的：Java 用 `mvn test`，Python 用 `pytest --cov`，Go 用 `go test -cover`

这个解耦，就是 Harness 脚手架参数化机制的原型。

### 2.3 核心挑战：从"硬编码"到"参数化"

把技能参数化，听起来简单——把 `mvn compile` 换成 `{{BUILD_CMD}}` 就行了。但实际拆解后，发现需要参数化的不仅仅是"命令"：

| 需要参数化的东西 | Java 的值 | Go 的值 |
|----------------|----------|--------|
| 构建命令 | `mvn compile` | `go build ./...` |
| 测试命令 | `mvn test` | `go test ./...` |
| 测试框架 | `JUnit 5` | `testing` |
| 代码规范 | `checkstyle` | `go vet` + `staticcheck` |
| 架构约束 | `archunit` | `go tool` + 包规则 |
| 构建工具 | `Maven` / `Gradle` | `go mod` |
| 依赖管理 | `pom.xml` | `go.mod` |
| 覆盖率检查 | `jacoco` | `go test -cover` |
| 目录结构 | `src/main/java/` | `cmd/` / `internal/` |

这不仅仅是"把命令换成变量"的问题——**不同语言的项目结构、构建流程、测试范式、代码规范工具都完全不同。**

### 2.4 参数化之后：还要自动检测

参数化解决了"同一套技能跑不同语言"的问题，但它带来了一个新问题：

> 用户需要手动告诉系统"我的项目是 Java + Spring Boot + Maven"

这不够好。一个好的脚手架应该**自动检测**项目使用了什么语言和框架，然后自动匹配对应的参数。

于是，我们开发了**检测机制**——通过扫描项目根目录的特征文件（`pom.xml` → Java + Maven、`build.gradle` → Java + Gradle、`go.mod` → Go、`requirements.txt` → Python、`package.json` → Node.js、`Cargo.toml` → Rust + Cargo、`composer.json` → PHP + Composer……），自动识别语言和框架。

检测的粒度从"语言"细化到了"框架"：

- **Java**：Spring Boot + Maven、Spring Boot + Gradle、Spring MVC、Quarkus、Micronaut、Dubbo、Spring Cloud Alibaba……
- **Python**：FastAPI + pip、FastAPI + poetry、Flask、Django、PyTorch、LangChain、CrewAI……
- **Go**：Gin + go mod、go-zero、Fiber、Echo、Chi、Gorilla Mux、Kitex、Hertz……
- **Rust**：Axum、Actix Web、Rocket、Warp、Poem、Loco、Tauri……
- **PHP**：Laravel、ThinkPHP、Symfony、Hyperf、Swoole、WordPress、GravCMS……
- **Frontend**：Vue + Vite、React + Vite、Angular、Next.js、Nuxt.js……

每种组合对应一套不同的参数。当参数化表达到 51 个参数键时，这套系统才能说"真正支持了跨语言"。

### 2.5 工具适配：从"只支持 Claude Code"到"支持 19+ 工具"

参数化解决了"跨语言"的问题，但还有一个问题：**huazai-trip-plan 的技能是基于 Claude Code 的 Skill 机制写的。**

如果用户用的是 Cursor、Cline、Windsurf、Reasonix 或其他 AI 工具，怎么办？

这意味着我们需要一个**安装适配器**——让用户无论使用什么 AI 工具，都能安装和运行 Harness 技能。

于是我们开发了 `install-skill` 命令，它能够：

1. 检测当前使用的 AI 工具（从交互方式、环境变量、配置文件特征推断）
2. 根据工具的格式要求，将技能文件转换成对应格式
3. 安装到工具的正确位置（Cursorrules、CLAUDE.md、MCP 配置等）
4. 验证安装是否成功

目前支持 19+ 个 AI 工具的适配安装。

### 2.6 从"Java 专属"到"基础设施"

回头看，huazai-trip-plan 中的那套 Harness，是一个"刚好能用的原型"。它证明了"工程纪律 → AI 代码质量"这个因果链是成立的，但它的作用域被限制在 Java 生态内。

现在这个脚手架，是从**那个原型**中解耦出来的通用版本：

- **一套核心规则** → 跨语言通用的工程纪律
- **参数化机制** → 同一套规则适配不同语言/框架
- **自动检测** → 不需要用户手动配置
- **工具适配器** → 不绑定 AI 工具
- **可扩展性** → 新增框架只需要新增检测规则 + 参数块

**这是从"一个 Java 项目的好工具"到"所有语言的基础设施"的跨越。**

---

## 三、项目整体架构设计

### 3.1 一种新的分层视角

传统软件分层的目的是"关注点分离"（Separation of Concerns）。但 Harness 的目标不是分离"业务逻辑层"，而是分离**"决策的可信度"**。

思考一个关键洞察：规则、技能、参数、模板，它们有一个共同点——**它们的可信度不同，因此必须放在不同的物理位置，由不同的更新流程管控**。

- **跨语言通用规则**可信度最高：它们体现的是普适工程真理，不该因语言而变。放在 `harness-core/rules/`。
- **语言包规则**可信度次之：它们体现的是某个语言的惯用法，只在特定技术栈内稳定。放在 `harness-{lang}/rules/`。
- **参数**可信度最低：它们是具体框架、具体工具链的"事实值"，变化最频繁。放在 `apply-harness/SKILL.md` 的参数表里。

物理位置的差异，反映了**"什么该由谁掌控"的不变式**。这是 Harness 架构的第一条不变式。

整个脚手架采用**四层架构**：**入口层 → 核心骨架层 → 语言包层 → 运行时层**。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1200 920" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <radialGradient id="bgg" cx="50%" cy="0%" r="120%">
      <stop offset="0%" stop-color="#1e293b"/>
      <stop offset="100%" stop-color="#020617"/>
    </radialGradient>
    <linearGradient id="entry" x1="0" y1="0" x2="1" y2="1">
      <stop offset="0%" stop-color="#818cf8"/><stop offset="100%" stop-color="#6366f1"/>
    </linearGradient>
    <linearGradient id="core" x1="0" y1="0" x2="1" y2="1">
      <stop offset="0%" stop-color="#38bdf8"/><stop offset="100%" stop-color="#0ea5e9"/>
    </linearGradient>
    <linearGradient id="lang" x1="0" y1="0" x2="1" y2="1">
      <stop offset="0%" stop-color="#34d399"/><stop offset="100%" stop-color="#10b981"/>
    </linearGradient>
    <linearGradient id="runtime" x1="0" y1="0" x2="1" y2="1">
      <stop offset="0%" stop-color="#fbbf24"/><stop offset="100%" stop-color="#f97316"/>
    </linearGradient>
    <filter id="sh" x="-20%" y="-20%" width="140%" height="140%">
      <feDropShadow dx="0" dy="6" stdDeviation="8" flood-color="#000" flood-opacity="0.45"/>
    </filter>
    <filter id="glow">
      <feDropShadow dx="0" dy="0" stdDeviation="6" flood-opacity="0.4"/>
    </filter>
  </defs>

  <rect width="1200" height="920" fill="url(#bgg)"/>
  <rect width="1200" height="6" fill="url(#entry)"/>

  <text x="600" y="44" text-anchor="middle" fill="#f1f5f9" font-size="28" font-weight="700" letter-spacing="2">HARNESS 四层架构 · 按"决策可信度"分层</text>
  <text x="600" y="66" text-anchor="middle" fill="#64748b" font-size="13">关注点分离的核心是分层决策的可信度，而非分层业务逻辑</text>

  <!-- ========== 入口层 ========== -->
  <g>
    <rect x="70" y="88" width="1060" height="140" rx="14" fill="#1e1b4b" stroke="#818cf8" stroke-width="1.5" opacity="0.95" filter="url(#sh)"/>
    <text x="92" y="118" fill="#c7d2fe" font-size="14" font-weight="700">入口层 · Entry Layer</text>
    <text x="92" y="136" fill="#94a3b8" font-size="11" letter-spacing="1">TRUST: 编排者 — 唯一面向用户的操作入口</text>

    <rect x="110" y="150" width="320" height="58" rx="10" fill="url(#entry)" filter="url(#sh)"/>
    <text x="270" y="174" text-anchor="middle" fill="#fff" font-size="15" font-weight="700">/apply-harness</text>
    <text x="270" y="194" text-anchor="middle" fill="#e0e7ff" font-size="11">Step1-8 · 检测→渲染→复制→注册</text>

    <rect x="455" y="150" width="320" height="58" rx="10" fill="url(#entry)" filter="url(#sh)"/>
    <text x="615" y="174" text-anchor="middle" fill="#fff" font-size="15" font-weight="700">/install-skill</text>
    <text x="615" y="194" text-anchor="middle" fill="#e0e7ff" font-size="11">检测工具→适配→安装技能</text>

    <rect x="800" y="150" width="300" height="58" rx="10" fill="#312e81" stroke="#818cf8" stroke-width="1" filter="url(#sh)"/>
    <text x="950" y="174" text-anchor="middle" fill="#e0e7ff" font-size="13" font-weight="700">检测表 + 参数表</text>
    <text x="950" y="194" text-anchor="middle" fill="#94a3b8" font-size="11">6 语言 · 111 框架差异 · 51 参数键 · 三级继承</text>
  </g>
  <line x1="600" y1="228" x2="600" y2="252" stroke="#6366f1" stroke-width="2" stroke-dasharray="6,4"/>
  <polygon points="600,256 594,246 606,246" fill="#6366f1"/>

  <!-- ========== 核心骨架层 ========== -->
  <g>
    <rect x="70" y="260" width="1060" height="160" rx="14" fill="#0c1a2b" stroke="#38bdf8" stroke-width="1.5" opacity="0.95" filter="url(#sh)"/>
    <text x="92" y="290" fill="#bae6fd" font-size="14" font-weight="700">核心骨架层 · Core Layer</text>
    <text x="92" y="308" fill="#94a3b8" font-size="11" letter-spacing="1">TRUST: 最高 — 普适工程真理，版本管控最严格 · harness-core/</text>

    <rect x="110" y="320" width="320" height="84" rx="10" fill="url(#core)" filter="url(#sh)"/>
    <text x="270" y="344" text-anchor="middle" fill="#fff" font-size="14" font-weight="700">📋 Rules · 通用规则</text>
    <text x="270" y="364" text-anchor="middle" fill="#e0f2fe" font-size="11">SDD-TDD / 开发流程 / 运行时可靠性</text>
    <text x="270" y="382" text-anchor="middle" fill="#7dd3fc" font-size="11">4 条 · 跨语言 · 不变式</text>

    <rect x="455" y="320" width="320" height="84" rx="10" fill="url(#core)" filter="url(#sh)"/>
    <text x="615" y="344" text-anchor="middle" fill="#fff" font-size="14" font-weight="700">⚙️ Skills · 核心技能</text>
    <text x="615" y="364" text-anchor="middle" fill="#e0f2fe" font-size="11">2D 流程 / 工程规范 / 技术巡检</text>
    <text x="615" y="382" text-anchor="middle" fill="#7dd3fc" font-size="11">owner · skill.md · 5.3 · 7.4</text>

    <rect x="800" y="320" width="300" height="84" rx="10" fill="url(#core)" filter="url(#sh)"/>
    <text x="950" y="344" text-anchor="middle" fill="#fff" font-size="14" font-weight="700">🧩 Templates · 模板</text>
    <text x="950" y="364" text-anchor="middle" fill="#e0f2fe" font-size="11">owner.md / change.md / wiki</text>
    <text x="950" y="382" text-anchor="middle" fill="#7dd3fc" font-size="11">参数化 · {{PLACEHOLDER}}</text>
  </g>
  <line x1="600" y1="420" x2="600" y2="444" stroke="#0ea5e9" stroke-width="2" stroke-dasharray="6,4"/>
  <polygon points="600,448 594,438 606,438" fill="#0ea5e9"/>

  <!-- ========== 语言包层 ========== -->
  <g>
    <rect x="70" y="452" width="1060" height="175" rx="14" fill="#052e1c" stroke="#34d399" stroke-width="1.5" opacity="0.95" filter="url(#sh)"/>
    <text x="92" y="482" fill="#a7f3d0" font-size="14" font-weight="700">语言包层 · Language Layer</text>
    <text x="92" y="500" fill="#94a3b8" font-size="11" letter-spacing="1">TRUST: 中 — 语言惯用法，随技术栈稳定 · harness-{lang}/</text>

    <rect x="110" y="515" width="240" height="96" rx="10" fill="url(#lang)" filter="url(#sh)"/>
    <text x="230" y="540" text-anchor="middle" fill="#fff" font-size="20" font-weight="700">☕ Java</text>
    <text x="230" y="562" text-anchor="middle" fill="#d1fae5" font-size="11">14 框架 · Maven/Gradle</text>
    <text x="230" y="580" text-anchor="middle" fill="#a7f3d0" font-size="11">JUnit5 · Checkstyle · ArchUnit</text>
    <text x="230" y="598" text-anchor="middle" fill="#a7f3d0" font-size="11">9 skills · 2 特有规则</text>

    <rect x="370" y="515" width="240" height="96" rx="10" fill="url(#lang)" filter="url(#sh)"/>
    <text x="490" y="540" text-anchor="middle" fill="#fff" font-size="20" font-weight="700">🐍 Python</text>
    <text x="490" y="562" text-anchor="middle" fill="#d1fae5" font-size="11">16 框架 · pip/poetry/uv</text>
    <text x="490" y="580" text-anchor="middle" fill="#a7f3d0" font-size="11">pytest · flake8 · mypy</text>
    <text x="490" y="598" text-anchor="middle" fill="#a7f3d0" font-size="11">9 skills · 2 特有规则</text>

    <rect x="630" y="515" width="240" height="96" rx="10" fill="url(#lang)" filter="url(#sh)"/>
    <text x="750" y="540" text-anchor="middle" fill="#fff" font-size="20" font-weight="700">🔵 Go</text>
    <text x="750" y="562" text-anchor="middle" fill="#d1fae5" font-size="11">20 框架 · go mod</text>
    <text x="750" y="580" text-anchor="middle" fill="#a7f3d0" font-size="11">go test · golangci-lint</text>
    <text x="750" y="598" text-anchor="middle" fill="#a7f3d0" font-size="11">9 skills · 2 特有规则</text>

    <rect x="890" y="515" width="220" height="96" rx="10" fill="url(#lang)" filter="url(#sh)"/>
    <text x="1000" y="540" text-anchor="middle" fill="#fff" font-size="20" font-weight="700">🟩 Frontend</text>
    <text x="1000" y="562" text-anchor="middle" fill="#d1fae5" font-size="11">9 框架 · npm/pnpm/yarn</text>
    <text x="1000" y="580" text-anchor="middle" fill="#a7f3d0" font-size="11">Vitest · ESLint · Prettier</text>
    <text x="1000" y="598" text-anchor="middle" fill="#a7f3d0" font-size="11">9 skills · 3 特有规则</text>
  </g>
  <line x1="600" y1="627" x2="600" y2="651" stroke="#10b981" stroke-width="2" stroke-dasharray="6,4"/>
  <polygon points="600,655 594,645 606,645" fill="#10b981"/>

  <!-- ========== 运行时层 ========== -->
  <g>
    <rect x="70" y="660" width="1060" height="150" rx="14" fill="#1c1204" stroke="#fbbf24" stroke-width="1.5" opacity="0.95" filter="url(#sh)"/>
    <text x="92" y="690" fill="#fde68a" font-size="14" font-weight="700">运行时层 · Runtime Layer</text>
    <text x="92" y="708" fill="#94a3b8" font-size="11" letter-spacing="1">TRUST: 落地 — 工程纪律最终生效的地方</text>

    <rect x="110" y="722" width="330" height="72" rx="10" fill="url(#runtime)" filter="url(#sh)"/>
    <text x="275" y="748" text-anchor="middle" fill="#fff" font-size="14" font-weight="700">📁 .harness/ 工作区</text>
    <text x="275" y="768" text-anchor="middle" fill="#fef3c7" font-size="11">agents/ rules/ skills/ changes/ wiki/ CONTEXT</text>
    <text x="275" y="784" text-anchor="middle" fill="#fde68a" font-size="11">工程规范的单一载体（SSOT）</text>

    <rect x="460" y="722" width="330" height="72" rx="10" fill="url(#runtime)" filter="url(#sh)"/>
    <text x="625" y="748" text-anchor="middle" fill="#fff" font-size="14" font-weight="700">🤖 AI 工具</text>
    <text x="625" y="768" text-anchor="middle" fill="#fef3c7" font-size="11">Reasonix·Claude·Cursor·Cline·Windsurf</text>
    <text x="625" y="784" text-anchor="middle" fill="#fde68a" font-size="11">19+ 工具 · SKILL.md / 自定义适配</text>

    <rect x="810" y="722" width="290" height="72" rx="10" fill="#451a03" stroke="#fbbf24" stroke-width="1" filter="url(#sh)"/>
    <text x="955" y="748" text-anchor="middle" fill="#fef3c7" font-size="14" font-weight="700">🔄 CI/CD 流水线</text>
    <text x="955" y="768" text-anchor="middle" fill="#fde68a" font-size="11">自动化测试 · 静态分析 · 覆盖率</text>
    <text x="955" y="784" text-anchor="middle" fill="#fde68a" font-size="11">安全扫描 · 部署验证</text>
  </g>

  <text x="600" y="862" text-anchor="middle" fill="#475569" font-size="12">一条不变式贯穿四层：分层按"决策可信度"而非业务逻辑 —— 可信度越高，越靠近核心层，更新越受控</text>
  <text x="600" y="884" text-anchor="middle" fill="#334155" font-size="11">entry 编排 → core/templates 提供 → lang 覆盖差异 → runtime 落地生效</text>
</svg>
```

### 3.2 核心组件：工程纪律的六块基石

整个脚手架由六大组件构成，每个组件在 `.harness/` 目录中都有对应的物理位置：

| 组件 | 目录 | 职责 | 可信度不变式 |
|------|------|------|-------------|
| **Owner Agent** | `.harness/agents/owner.md` | 应用的"人格设定"——身份、使命、决策边界 | AI 初始化时必读，是"AI 认识自己"的真相源 |
| **Rules（规则）** | `.harness/rules/` | 5 条不可协商约束 | 写入后禁止跳过/修改/降级 |
| **Skills（技能）** | `.harness/skills/` | 30 个可执行技能，斜杠命令驱动 | 命令与 AI 工具的注册一一对应 |
| **Changes（变更）** | `.harness/changes/` | change.md / review.md / verify.md | 每次变更必须有可审计轨迹 |
| **Wiki（知识库）** | `.harness/wiki/` | 业务模型、ADR、数据模型、接口协议 | 承接需求分析产生的共享知识 |
| **CONTEXT.md** | `.harness/CONTEXT.md` | 共享语言词典（Ubiquitous Language） | 所有规则/技能引用术语的权威定义源 |

### 3.3 数据流：从检测到生效的五步定律

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 980 700" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg2" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/>
    </linearGradient>
    <linearGradient id="step" x1="0" y1="0" x2="1" y2="0">
      <stop offset="0%" stop-color="#312e81"/><stop offset="100%" stop-color="#4338ca"/>
    </linearGradient>
    <linearGradient id="stepA" x1="0" y1="0" x2="1" y2="0">
      <stop offset="0%" stop-color="#065f46"/><stop offset="100%" stop-color="#047857"/>
    </linearGradient>
    <filter id="sh2" x="-20%" y="-20%" width="140%" height="140%">
      <feDropShadow dx="0" dy="5" stdDeviation="7" flood-color="#000" flood-opacity="0.4"/>
    </filter>
  </defs>

  <rect width="980" height="700" fill="url(#bg2)"/>
  <text x="490" y="38" text-anchor="middle" fill="#f1f5f9" font-size="28" font-weight="700" letter-spacing="1">apply-harness 数据流：检测 → 渲染 → 复制 → 注册 → 生效</text>

  <!-- Step1 -->
  <rect x="290" y="58" width="400" height="58" rx="29" fill="url(#stepA)" filter="url(#sh2)"/>
  <text x="490" y="82" text-anchor="middle" fill="#fff" font-size="14" font-weight="700">Step 1 · 检测语言与框架</text>
  <text x="490" y="102" text-anchor="middle" fill="#d1fae5" font-size="11">扫描根目录 → 匹配检测表 → 输出（语言,框架,构建）三元组</text>
  <line x1="490" y1="116" x2="490" y2="146" stroke="#34d399" stroke-width="2"/>
  <polygon points="490,152 483,141 497,141" fill="#34d399"/>

  <!-- Step2 -->
  <rect x="290" y="156" width="400" height="58" rx="10" fill="url(#step)" filter="url(#sh2)"/>
  <text x="490" y="181" text-anchor="middle" fill="#fff" font-size="14" font-weight="700">Step 2 · 读取项目名称</text>
  <text x="490" y="201" text-anchor="middle" fill="#c7d2fe" font-size="11">从 pom.xml / go.mod / pyproject.toml / package.json 读取 {{PROJECT_NAME}}</text>
  <line x1="490" y1="214" x2="490" y2="244" stroke="#6366f1" stroke-width="2"/>
  <polygon points="490,250 483,239 497,239" fill="#6366f1"/>

  <!-- Step3 -->
  <rect x="170" y="254" width="640" height="58" rx="10" fill="url(#step)" filter="url(#sh2)"/>
  <text x="490" y="279" text-anchor="middle" fill="#fff" font-size="14" font-weight="700">Step 3 · 参数表三级继承 → 渲染 owner.md</text>
  <text x="490" y="299" text-anchor="middle" fill="#c7d2fe" font-size="11">语言基础 ← 框架差异 ← 构建工具差异 · 51 参数键一次性替换</text>
  <line x1="490" y1="312" x2="490" y2="342" stroke="#6366f1" stroke-width="2"/>
  <polygon points="490,348 483,337 497,337" fill="#6366f1"/>

  <!-- Step4 -->
  <rect x="170" y="352" width="640" height="58" rx="10" fill="url(#step)" filter="url(#sh2)"/>
  <text x="490" y="377" text-anchor="middle" fill="#fff" font-size="14" font-weight="700">Step 4 · 复制规则</text>
  <text x="490" y="397" text-anchor="middle" fill="#c7d2fe" font-size="11">3 条通用规则（core/rules）+ 语言特有规则（harness-{lang}/rules）→ .harness/rules/</text>
  <line x1="490" y1="410" x2="490" y2="440" stroke="#6366f1" stroke-width="2"/>
  <polygon points="490,446 483,435 497,435" fill="#6366f1"/>

  <!-- Step5 -->
  <rect x="170" y="450" width="640" height="58" rx="10" fill="url(#step)" filter="url(#sh2)"/>
  <text x="490" y="475" text-anchor="middle" fill="#fff" font-size="14" font-weight="700">Step 5 · 渲染技能模板 + 复制</text>
  <text x="490" y="495" text-anchor="middle" fill="#c7d2fe" font-size="11">10 个模板技能（读→替换{{}}→写）+ 16 个通用技能（直拷）</text>
  <line x1="490" y1="508" x2="490" y2="538" stroke="#6366f1" stroke-width="2"/>
  <polygon points="490,544 483,533 497,533" fill="#6366f1"/>

  <!-- Step5.5 -->
  <rect x="170" y="548" width="640" height="58" rx="10" fill="#7c2d12" stroke="#fb923c" stroke-width="1" filter="url(#sh2)"/>
  <text x="490" y="573" text-anchor="middle" fill="#fed7aa" font-size="14" font-weight="700">Step 5.5 · 注册技能到 AI 工具</text>
  <text x="490" y="593" text-anchor="middle" fill="#fdba74" font-size="11">检测当前工具 → 复制到对应技能目录 → 斜杠命令立即可用</text>
  <line x1="490" y1="606" x2="490" y2="632" stroke="#f97316" stroke-width="2"/>

  <!-- End -->
  <rect x="290" y="636" width="400" height="50" rx="25" fill="url(#stepA)" filter="url(#sh2)"/>
  <text x="490" y="667" text-anchor="middle" fill="#fff" font-size="15" font-weight="700">✅ 安装完成 → 输出项目摘要卡片</text>
</svg>
```

**五步定律背后的不变式**：**每降低一层可信度，就引入一次"渲染"或"复制"**——任何跨层的通信都不能越过渲染层直接硬编码。这让参数表成为唯一的"事实源"（Single Source of Truth），任何框架差异都收敛在参数表中，而不是散落在 30 个技能文件里。

---
---

## 四、项目核心设计

### 4.1 Owner Agent：让 AI 认识"自己是谁"

在进入技术细节之前，先理解一个根本问题：**AI 在接手一个项目时，它知道自己是谁吗？**

默认情况下，AI 是一个"万能的通用助手"——它不知道自己是这个项目的负责人，不知道这个项目的使命，不知道什么该自己做决定、什么该请示人类。**Owner Agent 的存在，就是为了回答这三个问题。**

`owner.md` 不是一段代码，而是一份被设计成 AI"人格设定"的 Markdown 文档。AI 在初始化项目后，第一件事就是读取它，从而"认识自己"。

**身份与使命**：
```
我是 [项目名称] 的 Owner Agent。
我的使命是：在 Harness 约束下，高效、高质量地驱动 6 阶段流水线，
交付可维护、可验证、可演化的软件。
```

**七大核心职责**（这是 Owner 的"责任边界"）：
1. 维护项目核心文档（CONTEXT.md、Wiki、Change 记录）——**这是知识资产的看护人**
2. 确保 AI 的所有产出符合 6 条规则——**这是纪律的执行者**
3. 驱动 6 阶段流水线，不跳步、不降级——**这是流程的守护者**
4. 垂直切片，一次只做一个 Red-Green-Refactor 循环——**这是迭代的节奏器**
5. 战争迷雾原则：只规划当前层，下一层标记不展开——**这是认知的边界者**
6. 变更管理——每一步创建/更新 change.md——**这是可追溯性的维护者**
7. 质量门禁——不满足门禁条件不进入下一阶段——**这是质量的守门人**

**决策边界**（用"授权级别"而非功能划分来定义）：
- 🟢 **可自主决策**：代码实现细节、文件组织、方法命名、测试用例设计
- 🟡 **必须请示人类**：架构方案变更、依赖引入、异常处理策略、边界条件
- 🔴 **必须停下**：不理解需求、发现冲突约束、某阶段门禁未通过

**为什么用"授权级别"定义边界？** 因为功能的边界（"我可以改 Controller"）无法穷举，而授权的级别（"多大范围的决策我能自己拍板"）是有限的、可判定的。这是 Owner Agent 设计的不变式：**决策边界按授权级别划分，而非按功能列表划分。**

### 4.2 6 阶段流水线：从模糊需求到可部署产物的状态机

流水线是整个脚手架的核心。它不是一份"流程说明书"，而是一个**有明确状态转移规则的状态机**。每个阶段有一个"入口条件"和"出口门禁"，不满足门禁就不能进入下一阶段。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1180 520" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg3" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/>
    </linearGradient>
    <linearGradient id="s1" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#6366f1"/><stop offset="100%" stop-color="#4f46e5"/>
    </linearGradient>
    <linearGradient id="s2" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0ea5e9"/><stop offset="100%" stop-color="#0284c7"/>
    </linearGradient>
    <linearGradient id="s3" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#10b981"/><stop offset="100%" stop-color="#059669"/>
    </linearGradient>
    <linearGradient id="s4" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#f59e0b"/><stop offset="100%" stop-color="#d97706"/>
    </linearGradient>
    <linearGradient id="s5" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#ef4444"/><stop offset="100%" stop-color="#dc2626"/>
    </linearGradient>
    <linearGradient id="s6" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#8b5cf6"/><stop offset="100%" stop-color="#7c3aed"/>
    </linearGradient>
    <filter id="sh3" x="-20%" y="-20%" width="140%" height="140%">
      <feDropShadow dx="0" dy="5" stdDeviation="6" flood-color="#000" flood-opacity="0.4"/>
    </filter>
  </defs>

  <rect width="1180" height="520" fill="url(#bg3)"/>
  <text x="590" y="36" text-anchor="middle" fill="#f1f5f9" font-size="28" font-weight="700" letter-spacing="1">6 阶段流水线 · 有明确门禁的状态机</text>

  <!-- 阶段节点 -->
  <g font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
    <rect x="40" y="150" width="165" height="90" rx="12" fill="url(#s1)" filter="url(#sh3)"/>
    <text x="122" y="178" text-anchor="middle" fill="#fff" font-size="26" font-weight="700">①</text>
    <text x="122" y="200" text-anchor="middle" fill="#e0e7ff" font-size="13" font-weight="700">需求分析</text>
    <text x="122" y="220" text-anchor="middle" fill="#c7d2fe" font-size="10">/harnessing</text>

    <rect x="245" y="150" width="165" height="90" rx="12" fill="url(#s2)" filter="url(#sh3)"/>
    <text x="327" y="178" text-anchor="middle" fill="#fff" font-size="26" font-weight="700">②</text>
    <text x="327" y="200" text-anchor="middle" fill="#e0f2fe" font-size="13" font-weight="700">编码实现</text>
    <text x="327" y="220" text-anchor="middle" fill="#bae6fd" font-size="10">/coding-skill</text>

    <rect x="450" y="150" width="165" height="90" rx="12" fill="url(#s3)" filter="url(#sh3)"/>
    <text x="532" y="178" text-anchor="middle" fill="#fff" font-size="26" font-weight="700">③</text>
    <text x="532" y="200" text-anchor="middle" fill="#d1fae5" font-size="13" font-weight="700">单测编写</text>
    <text x="532" y="220" text-anchor="middle" fill="#a7f3d0" font-size="10">/unit-test-write</text>

    <rect x="655" y="150" width="165" height="90" rx="12" fill="url(#s4)" filter="url(#sh3)"/>
    <text x="737" y="178" text-anchor="middle" fill="#fff" font-size="26" font-weight="700">④</text>
    <text x="737" y="200" text-anchor="middle" fill="#fef3c7" font-size="13" font-weight="700">专家评审</text>
    <text x="737" y="220" text-anchor="middle" fill="#fde68a" font-size="10">/expert-reviewer</text>

    <rect x="860" y="150" width="165" height="90" rx="12" fill="url(#s5)" filter="url(#sh3)"/>
    <text x="942" y="178" text-anchor="middle" fill="#fff" font-size="26" font-weight="700">⑤</text>
    <text x="942" y="200" text-anchor="middle" fill="#fee2e2" font-size="13" font-weight="700">CI 门禁</text>
    <text x="942" y="220" text-anchor="middle" fill="#fecaca" font-size="10">/unit-test-ci</text>

    <rect x="975" y="150" width="165" height="90" rx="12" fill="url(#s6)" filter="url(#sh3)"/>
    <text x="1057" y="178" text-anchor="middle" fill="#fff" font-size="26" font-weight="700">⑥</text>
    <text x="1057" y="200" text-anchor="middle" fill="#ede9fe" font-size="13" font-weight="700">部署验证</text>
    <text x="1057" y="220" text-anchor="middle" fill="#ddd6fe" font-size="10">/deploy-verify</text>

    <!-- 箭头 -->
    <line x1="205" y1="195" x2="242" y2="195" stroke="#475569" stroke-width="2.5"/>
    <polygon points="248,195 238,189 238,201" fill="#94a3b8"/>
    <line x1="410" y1="195" x2="447" y2="195" stroke="#475569" stroke-width="2.5"/>
    <polygon points="453,195 443,189 443,201" fill="#94a3b8"/>
    <line x1="615" y1="195" x2="652" y2="195" stroke="#475569" stroke-width="2.5"/>
    <polygon points="658,195 648,189 648,201" fill="#94a3b8"/>
    <line x1="820" y1="195" x2="857" y2="195" stroke="#475569" stroke-width="2.5"/>
    <polygon points="863,195 853,189 853,201" fill="#94a3b8"/>
    <line x1="1025" y1="195" x2="1062" y2="195" stroke="#475569" stroke-width="2.5"/>
    <polygon points="1068,195 1058,189 1058,201" fill="#94a3b8"/>
  </g>

  <!-- 门禁 / 回退连接 -->
  <g font-size="11">
    <path d="M 122 240 L 122 300 L 530 300 L 530 240" fill="none" stroke="#f59e0b" stroke-width="1.5" stroke-dasharray="5,4"/>
    <text x="326" y="320" text-anchor="middle" fill="#fbbf24">门禁未过 → 回退到上一阶段，不允许跳过</text>

    <!-- 出口门禁面板 -->
    <rect x="40" y="360" width="1100" height="120" rx="12" fill="#0f172a" stroke="#334155" stroke-width="1"/>
    <text x="60" y="386" fill="#94a3b8" font-size="12" font-weight="700">出口门禁（不满足 → 红灯，禁止进入下一阶段）</text>

    <text x="122" y="415" text-anchor="middle" fill="#e0e7ff" font-size="11" font-weight="700">规格明确</text>
    <text x="122" y="432" text-anchor="middle" fill="#94a3b8" font-size="10">AC 可测 · ≥3 边界</text>

    <text x="327" y="415" text-anchor="middle" fill="#bae6fd" font-size="11" font-weight="700">测试全绿</text>
    <text x="327" y="432" text-anchor="middle" fill="#94a3b8" font-size="10">先失败测试→实现→重构</text>

    <text x="532" y="415" text-anchor="middle" fill="#a7f3d0" font-size="11" font-weight="700">覆盖率 ≥80%</text>
    <text x="532" y="432" text-anchor="middle" fill="#94a3b8" font-size="10">覆盖全部 AC 与边界</text>

    <text x="737" y="415" text-anchor="middle" fill="#fde68a" font-size="11" font-weight="700">0 个 🔴</text>
    <text x="737" y="432" text-anchor="middle" fill="#94a3b8" font-size="10">Spec + Standards 双轴</text>

    <text x="942" y="415" text-anchor="middle" fill="#fecaca" font-size="11" font-weight="700">全链路绿</text>
    <text x="942" y="432" text-anchor="middle" fill="#94a3b8" font-size="10">编译/Lint/架构/竞态/覆盖</text>

    <text x="1057" y="415" text-anchor="middle" fill="#ddd6fe" font-size="11" font-weight="700">健康 + 回滚</text>
    <text x="1057" y="432" text-anchor="middle" fill="#94a3b8" font-size="10">"CI 绿" ≠ "线上可用"</text>
  </g>

  <text x="590" y="500" text-anchor="middle" fill="#475569" font-size="12">状态机不变式：状态只能 forward 推进或回退到紧邻上一阶段，禁止跨阶段跳转</text>
</svg>
```

### 4.3 参数化模板机制：如何用 117 个参数块覆盖 111 框架差异

这是脚手架最核心的工程创新。表面上看它解决的是"跨语言、跨框架"的复用问题，但深层的设计洞察是：

> **工具链的差异是"组合"的，不是"枚举"的。**

111 框架差异 × 多个构建工具 = 数百种组合。如果为每种组合写一份完整的技能文件，那就是数百 × 30 文件 = 上万个文件，不可维护。

但如果我们把"语言的差异"、"框架的差异"、"构建工具的差异"拆成三个**正交维度**，那么组合数就从"乘法"退化为"加法"——这就是参数化三段继承的数学基础。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1000 560" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg4" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/>
    </linearGradient>
    <linearGradient id="l1" x1="0" y1="0" x2="1" y2="0">
      <stop offset="0%" stop-color="#6366f1"/><stop offset="100%" stop-color="#8b5cf6"/>
    </linearGradient>
    <linearGradient id="l2" x1="0" y1="0" x2="1" y2="0">
      <stop offset="0%" stop-color="#0ea5e9"/><stop offset="100%" stop-color="#06b6d4"/>
    </linearGradient>
    <linearGradient id="l3" x1="0" y1="0" x2="1" y2="0">
      <stop offset="0%" stop-color="#34d399"/><stop offset="100%" stop-color="#10b981"/>
    </linearGradient>
    <filter id="sh4" x="-20%" y="-20%" width="140%" height="140%">
      <feDropShadow dx="0" dy="5" stdDeviation="6" flood-color="#000" flood-opacity="0.4"/>
    </filter>
  </defs>

  <rect width="1000" height="560" fill="url(#bg4)"/>
  <text x="500" y="36" text-anchor="middle" fill="#f1f5f9" font-size="28" font-weight="700" letter-spacing="1">参数化三段继承 · 将组合数从乘法退化为加法</text>

  <!-- 第一层 -->
  <rect x="60" y="60" width="880" height="90" rx="12" fill="#1e1b4b" stroke="#818cf8" stroke-width="1.5" filter="url(#sh4)"/>
  <text x="80" y="88" fill="#c7d2fe" font-size="13" font-weight="700">① 语言基础参数 · Language Base</text>
  <text x="80" y="108" fill="#94a3b8" font-size="11" font-family="monospace">LANGUAGE=Java · RUNTIME=JDK21 · BUILD_CMD=mvn build · TEST_CMD=mvn test</text>
  <text x="80" y="126" fill="#94a3b8" font-size="11" font-family="monospace">LINT_CMD=mvn checkstyle · COV=mvn jacoco · TYPE=mvn compile</text>
  <text x="80" y="142" fill="#64748b" font-size="10">所有 Java 项目共享 · 6 种语言各一份</text>

  <line x1="500" y1="150" x2="500" y2="174" stroke="#6366f1" stroke-width="2"/>
  <polygon points="500,180 493,169 507,169" fill="#6366f1"/>

  <!-- 第二层 -->
  <rect x="60" y="184" width="880" height="90" rx="12" fill="#0c1a2b" stroke="#38bdf8" stroke-width="1.5" filter="url(#sh4)"/>
  <text x="80" y="212" fill="#bae6fd" font-size="13" font-weight="700">② 框架差异参数 · Framework Diff</text>
  <text x="80" y="232" fill="#94a3b8" font-size="11" font-family="monospace">Spring Boot: DEV_CMD=mvn spring-boot:run · ARCH Ctrl→Svc→Repo</text>
  <text x="80" y="250" fill="#94a3b8" font-size="11" font-family="monospace">Dubbo: DEV_CMD=mvn dubbo:run · ARCH Provider→Consumer · DB=Zookeeper</text>
  <text x="80" y="266" fill="#64748b" font-size="10">只写与基础不同的值 · 相同的继承 · ML/LLM 框架重定义 ARCH/DB 语义</text>

  <line x1="500" y1="274" x2="500" y2="298" stroke="#0ea5e9" stroke-width="2"/>
  <polygon points="500,304 493,293 507,293" fill="#0ea5e9"/>

  <!-- 第三层 -->
  <rect x="60" y="308" width="880" height="90" rx="12" fill="#052e1c" stroke="#34d399" stroke-width="1.5" filter="url(#sh4)"/>
  <text x="80" y="336" fill="#6ee7b7" font-size="13" font-weight="700">③ 构建工具差异参数 · Toolchain Diff</text>
  <text x="80" y="356" fill="#94a3b8" font-size="11" font-family="monospace">Maven: BUILD_CMD=mvn build · Gradle: BUILD_CMD=./gradlew build</text>
  <text x="80" y="374" fill="#94a3b8" font-size="11" font-family="monospace">pip: pip install · poetry: poetry install · uv: uv sync</text>
  <text x="80" y="390" fill="#64748b" font-size="10">只覆盖命令类占位符 · 下层覆盖上层的同名参数</text>

  <line x1="500" y1="398" x2="500" y2="422" stroke="#10b981" stroke-width="2"/>
  <polygon points="500,428 493,417 507,417" fill="#10b981"/>

  <!-- 汇合 -->
  <rect x="200" y="434" width="600" height="80" rx="14" fill="url(#l1)" filter="url(#sh4)"/>
  <text x="500" y="462" text-anchor="middle" fill="#fff" font-size="15" font-weight="700">渲染合并 → 最终参数表</text>
  <text x="500" y="486" text-anchor="middle" fill="#e0e7ff" font-size="12">51 参数键 → 一次替换到 owner.md 与 30 个技能文件</text>
  <text x="500" y="503" text-anchor="middle" fill="#c7d2fe" font-size="11">6 语言 × 111 框架差异 ⇋ 仅 117 个参数块</text>

  <text x="500" y="540" text-anchor="middle" fill="#475569" font-size="12">不变式：任何框架差异都必须收敛在参数表中，禁止散落到技能文件硬编码</text>
</svg>
```

**为什么 ML/AI/LLM 框架要"重定义语义"？**

LangChain、CrewAI、TensorFlow 这类 AI 框架没有传统的 Web 分层（Controller/Service/Repository）和关系型数据库访问。所以 `ARCH_LAYER` 和 `DB_ACCESS` 这两个占位符在 AI 框架下被赋予新的含义：

- `ARCH_LAYER`：`Agent → Tool → LLM` 而非 `Controller → Service → Repository`
- `DB_ACCESS`：`向量库 + 会话存储` 而非 `MyBatis-Plus + MySQL`

这体现了参数化机制的一个关键设计原则：**占位符是语义槽位，不是固定模板**。同一个占位符在不同框架下可以承载不同的领域含义，这让一套机制能同时覆盖 Web 框架和 AI/LLM 框架。

### 4.4 约束体系：5 条不可协商的规则

约束体系是整个脚手架"纪律"的载体。它分为**跨语言通用规则**和**语言特有规则**两个层次。

**通用规则**（来自 `harness-core/rules/`，3 条，体现普适工程真理）：
1. **SDD-TDD 模式** — 先写规格（Spec-Driven），再写测试，再写代码。change.md 是规格的真相源（SSOT）。
2. **开发流程规范** — 6 阶段流水线的操作细节、变更状态机、人机协同协议。
3. **运行时可靠性** — 外部依赖四要素（timeout / retry / rate limit / fallback）、traceId 传播、缓存退化策略、回滚基线。

**语言特有规则**（来自语言包 `rules/`，2 条，体现语言惯用法）：
4. **编码规范** — 命名风格、文件结构、注释规范、代码质量标准（Java 阿里巴巴规约 + Checkstyle；Python PEP 8 + flake8 + mypy + black；Go go vet + staticcheck）。
5. **工程结构** — 目录层级、包/模块划分、依赖方向约束（Go "依赖单向"、Java "service 层禁止依赖实现"）。

**Frontend 的特例**：为什么 Frontend 有 3 条特有规则？因为前端的运行时可靠性关注点（浏览器兼容性、Bundle 体积、水合性能）与后端完全不同，所以通用规则 3 在 Frontend 被语言特有规则覆盖。这体现了约束体系的另一个不变式：**规则可以被语言包覆盖，但覆盖必须显式声明，而非默默替换。**

> **关于变更定位规则**：`harness-core/rules/变更定位规则.md` 是开发流程规范在"多变更并存"场景下的细则文件（不属于独立的第 6 条规则），专门回答"十八个 change 并存时，AI 该做哪一个"——见 4.5 节。

### 4.5 多 Change 如何选择：从"候选清单"到"依赖 DAG"

6 阶段流水线回答的是"**一个** change 怎么走完"，但真实项目里从来不止一个 change。以 huazai-trip-plan 为例，`.harness/changes/` 下就有 **18 个 change 并存**（C-001 ~ C-018）。这时候真正的问题来了：**面对一堆 change，AI 凭什么知道"现在该做哪一个"？**

这是变更选择（Change Selection）问题，脚手架把它拆成两个层次来解决：技能层的**变更定位规则**，和项目层的**变更清单路线图（ROADMAP）**。

#### 4.5.1 技能层：变更定位规则（Change Selection Rule）

`harness-core/rules/变更定位规则.md` 的作用域覆盖所有消费 change.md 的技能（② coding-skill ~ ⑥ deploy-verify），它的核心原则只有一句话：

> **Change 定位必须明确，禁止模糊选择。多候选时停下请示，不自作主张。**

每个技能开始工作前，按以下流程锁定目标 change：

```
步骤 1: 是否已显式指定 <id>？
  ├─ 是（如 /coding-skill C-007）
  │    └─ 校验该 change 的 status 是否匹配当前阶段 → 匹配则使用，否则报错
  └─ 否 → 进入步骤 2

步骤 2: 扫描候选 change
  列出 .harness/changes/*/change.md 中 status 匹配当前阶段要求的 change

步骤 3: 数量判断
  ├─ 恰好 1 个 → 使用该 change
  ├─ 0 个       → 报错"无符合条件的 change"，退回上一阶段
  └─ ≥ 2 个     → 列出候选清单（id + 标题 + 摘要），停下请用户选择
                    未得到明确选择前，禁止继续执行
```

"匹配当前阶段"不是模糊的，而是由 change 的 **status 字段**机械决定的——每个阶段技能只认一个 status：

| 阶段 | 技能 | 匹配的 status |
|------|------|--------------|
| ② 编码实现 | `coding-skill` | `coding` |
| ③ 单测编写 | `unit-test-write` | `testing` |
| ④ 专家评审 | `expert-reviewer` | `reviewing` |
| ⑤ CI 门禁 | `unit-test-ci` | `ci` |
| ⑥ 部署验证 | `deploy-verify` | `verifying` |

规则同时列出了一组**禁止行为**，堵死一切"偷懒选"的路径：

- ❌ 多候选时默认取第一个（必须请示）
- ❌ 多候选时随机选一个
- ❌ 多候选时合并处理
- ❌ 无候选时跳过前置检查继续执行
- ❌ 忽略 status 校验直接使用任意 change

**设计哲学**：为什么 AI 不能自己挑一个最靠前的？因为"选哪个 change"本质是**价值判断**（业务优先级、依赖顺序、风险），不是 AI 能可靠推断的；而 status 是**事实判断**，机械可判定。脚手架把事实判断交给规则，把价值判断交还人类——这和 4.1 的授权级别（🟢 可自主 / 🟡 必请示 / 🔴 必停）一脉相承：多候选选择永远落在 🟡。

#### 4.5.2 项目层：变更清单路线图（ROADMAP）

定位规则解决"这个技能此刻做哪个"，但"项目下一步推进哪个 change"还需要项目级编排。脚手架在每个项目的 `.harness/changes/ROADMAP.md` 维护一份**变更清单路线图**：

> 它是 `.harness/changes/` 的清单索引与依赖地图，供 Owner Agent 断点续传与人类审批使用。单卡真相源仍是各自的 `change.md`，ROADMAP 只做导航、阶段分组与依赖编排，不重复定义需求。

ROADMAP 的核心价值是把 18 个平行的 change 变成一张**可推理的地图**，包含三样东西：

**① 阶段分组（Phase）**——按交付次序把 change 分组，天然形成"先做地基、再做基座、再做业务"的顺序：

```
Phase 0 工程地基      C-001(骨架) · C-002(门禁) · C-003(环境) · C-004(公共模型)
Phase 1 契约与协同基座  C-005(A2A 基座/统一治理)
Phase 2 五专业 Agent    C-006(XHS) · C-007(Route) · C-008(Itinerary) · C-009(Budget)
Phase 3 编排与对外      C-010(Supervisor) · C-011(Server REST)
Phase 4 端到端/体验     C-012(E2E) · C-013(前端) · C-014(PDF 导出)
Phase 5 增强与加固      C-015(记忆/RAG) · C-016(可观测/可靠性)
```

**② 依赖 DAG（change 级）**——箭头指向被依赖方，必须无环：`C-001 ─▶ C-002, C-003, C-004`，`C-004 ─▶ C-005, C-006, C-007, C-008, C-009, C-011, C-014`……由它可以直接算出**关键路径**：

```
C-001 → C-004 → C-005 → C-006/07/08/09 → C-010 → C-011 → C-012 → C-016
```

**③ 状态总览与审批门禁**——每个 change 的当前 status 汇总在一张表里，一眼看清谁在 analyzing、谁在 reviewing；同时标注人类审批节点（如"进入 coding 前必须审批"）。Owner Agent 每次接手项目，读一遍 ROADMAP 就能**断点续传**：知道项目整体到哪了、下一步推哪个 change、哪个候选在等人类拍板。

#### 4.5.3 两层机制如何协作

技能层与项目层不是两套独立系统，而是同一件事的两个视角：

| 场景 | 走哪层 | 谁裁决 |
|------|--------|--------|
| 技能被调用，候选 ≥ 2 | 变更定位规则（步骤 3） | 人类（停下请示） |
| 项目断点续传，推下一个 | ROADMAP（依赖 DAG + 关键路径） | Owner Agent（按图推进）+ 人类审批 |
| 一个 change 内部推进 | 6 阶段流水线 status 机 | 门禁（机器） |

**不变式**：无论哪一层，"选择"的裁决权都在人类手中——定位规则靠"多候选必请示"强制，ROADMAP 靠"审批门禁"强制。AI 可以执行选择、可以推理依赖，但**不能替人类决定"做什么"**。这就是多 change 场景下的工程纪律。

---

## 五、模块核心设计

### 5.1 apply-harness：脚手架的中枢编辑器

`skills/apply-harness/SKILL.md` 是整个脚手架最复杂的单个文件，也是唯一的"中枢编辑器"。它不是一份说明文档，而一个**可执行的、有状态的安装协议**：

- 8 个主步骤 + 2 个内嵌子步骤（Step 1-8，含 Step 5.5 技能注册与 Step 7.1 共享语言上下文）
- 6 种语言的检测表（每种 11-35 条规则）
- 111 个框架差异的参数块定义
- 51 参数键的完整定义
- 硬性约束（禁止修改已存在 `.harness/`、禁止多语言擅自选择、禁止跳过 Step 1）

**检测表的设计原则：精确优先，模糊兜底。** 检测规则按匹配精确度从高到低排列：

```
pom.xml + spring-boot-starter  → Spring Boot（精确）
pom.xml + spring-webmvc        → Spring MVC（精确）
pom.xml + dubbo 依赖           → Dubbo（精确）
pom.xml + spring-ai            → Spring AI（精确）
...
pom.xml（通用，未匹配以上）      → 询问用户（兜底）
```

**为什么需要"兜底询问"？** 因为在检测到"有 pom.xml 但没有匹配到任何已知框架"时，直接臆测框架是危险的。**宁可多问一次，也不愿给用户装错一套参数。** 这是 apply-harness 设计的第一条失败原则：**当不确定性超过阈值时，把决策权交还给人类。**

### 5.2 语言包：差异的隔离舱

每个语言包（`harness-java/`、`harness-python/`、`harness-golang/`、`harness-rust/`、`harness-php/`、`harness-front/`）只承载语言特有内容：

```
harness-{lang}/
├── SKILL.md          # 语言包入口描述（列出该语言支持的框架/构建工具/规范）
├── rules/            # 语言特有规则
│   ├── 编码规范.md
│   └── 工程结构.md
└── skills/           # 框架专属技能（core 无模板，仅部分语言有）
    ├── java-code-review/SKILL.md        # 仅 Java
    ├── spring-api-convention/SKILL.md   # 仅 Java
    ├── mybatis-toolkit/SKILL.md         # 仅 Java
    └── openfeign-toolkit/SKILL.md       # 仅 Java
```

**语言包的核心设计原则：同构化 + 参数化。** 六个语言包的结构**完全同构**——同样的子目录、同样的 rules。唯一的差异是 `{{LANG_TAG}}`（`-java`、`-python`、`-golang`、`-rust`、`-php`、`-front`）和参数表里的具体命令值。所有流水线技能的模板已收敛到 `harness-core/skills/`，语言包不再维护同名副本。

同构化的价值在于：**维护者只需要理解一个语言包的结构，就能维护全部六个。** 这极大降低了脚手架本身的维护成本——这是"meta-scaffolding"（脚手架的脚手架）层面的设计。

### 5.3 install-skill：技能注册的适配器模式

`/install-skill` 是一个独立的技能注册入口，负责把 `.harness/skills/` 下的技能注册到当前 AI 工具可识别的目录中，让斜杠命令立即可用。

它本质上是**适配器模式（Adapter Pattern）**的实践：统一接口（"把技能装到工具里"）背后，是对不同工具不同格式的适配。

**检测流程**：按优先级检测当前 AI 工具 → 识别其技能目录 → 确保兼容性。

**为什么不只支持 Reasonix？** 因为脚手架的核心价值是"用户已经用某个工具，就能用这套工程纪律"。如果用户主力工具是 Claude Code，却要求它切换到 Reasonix 才能用 Harness，那这个脚手架就没有价值了。**适配用户已用的工具，而不是要求用户迁就工具。** 这是 install-skill 的核心设计理念。

**约束**：
- ❌ 不修改原 `.harness/skills/` 下的内容
- ❌ 不把 install-skill 自身当技能安装
- ❌ 只安装到检测到的工具目录
- ✅ 已存在则覆盖，并提示用户

---

## 六、每个命令背后的设计哲学

这一章，我们逐个拆解每个斜杠命令背后的设计思考。它们不是孤立的工具，而是同一个状态机的不同状态转换器。

### 6.1 /harnessing：需求分析的"一次一个问题"

这是流水线的第一站，也是整个脚手架中最重要的一站。因为**80% 的 Bug 可以追溯到"需求没有充分理解就开始写代码"**。

**核心设计：一次只问一个问题。** 采用决策树驱动的串行提问——AI 每一步只输出一个问题，用户回答后，AI 根据回答推进到下一个节点。这是"信息增益最大化"的策略。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 780 400" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_6" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <linearGradient id="bl" x1="0" y1="0" x2="1" y2="0"><stop offset="0%" stop-color="#6366f1"/><stop offset="100%" stop-color="#8b5cf6"/></linearGradient>
    <linearGradient id="gn" x1="0" y1="0" x2="1" y2="0"><stop offset="0%" stop-color="#10b981"/><stop offset="100%" stop-color="#34d399"/></linearGradient>
    <filter id="sh"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="780" height="400" fill="url(#bg_6)" rx="10"/>
  <text x="390" y="30" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">/harnessing · 一次只问一个问题的决策树</text>
  <rect x="300" y="48" width="180" height="36" rx="18" fill="url(#bl)" filter="url(#sh)"/>
  <text x="390" y="71" text-anchor="middle" fill="#fff" font-size="12" font-weight="700">用户输入需求</text>
  <line x1="390" y1="84" x2="390" y2="106" stroke="#6366f1" stroke-width="1.5"/><polygon points="390,111 385,103 395,103" fill="#6366f1"/>
  <rect x="80" y="115" width="620" height="40" rx="8" fill="#1e1b4b" stroke="#818cf8" stroke-width="1" filter="url(#sh)"/>
  <text x="390" y="133" text-anchor="middle" fill="#c7d2fe" font-size="11" font-weight="700">Q1: 这个需求的目标是什么？期望解决什么问题？</text>
  <text x="390" y="148" text-anchor="middle" fill="#94a3b8" font-size="10">用户回答 → 推进</text>
  <line x1="390" y1="155" x2="390" y2="173" stroke="#6366f1" stroke-width="1.5"/><polygon points="390,178 385,170 395,170" fill="#6366f1"/>
  <rect x="130" y="182" width="520" height="40" rx="8" fill="#1e1b4b" stroke="#818cf8" stroke-width="1" filter="url(#sh)"/>
  <text x="390" y="200" text-anchor="middle" fill="#c7d2fe" font-size="11" font-weight="700">Q2: 有哪些功能点需要实现？列出可测试的 AC</text>
  <text x="390" y="215" text-anchor="middle" fill="#94a3b8" font-size="10">用户回答 → 推进</text>
  <line x1="390" y1="222" x2="390" y2="240" stroke="#6366f1" stroke-width="1.5"/><polygon points="390,245 385,237 395,237" fill="#6366f1"/>
  <rect x="130" y="249" width="520" height="40" rx="8" fill="#1e1b4b" stroke="#818cf8" stroke-width="1" filter="url(#sh)"/>
  <text x="390" y="267" text-anchor="middle" fill="#c7d2fe" font-size="11" font-weight="700">Q3: 有哪些边界条件？异常情况怎么处理？</text>
  <text x="390" y="282" text-anchor="middle" fill="#94a3b8" font-size="10">用户回答 → 推进</text>
  <line x1="390" y1="289" x2="390" y2="310" stroke="#6366f1" stroke-width="1.5"/><polygon points="390,315 385,307 395,307" fill="#6366f1"/>
  <rect x="180" y="320" width="420" height="50" rx="12" fill="url(#gn)" filter="url(#sh)"/>
  <text x="390" y="344" text-anchor="middle" fill="#fff" font-size="12" font-weight="700">输出：需求总结卡片</text>
  <text x="390" y="362" text-anchor="middle" fill="#d1fae5" font-size="10">项目/目标/AC 列表/≥3 边界条件/变更状态</text>
</svg>
```

**最终输出**：需求总结卡片，包含项目名称、目标、可用约束、AC 列表（每个 AC 附带可测试性说明）、边界条件（≥3 个）、变更状态（`analyzing`）。

### 6.2 /coding-skill：Red-Green-Refactor 小循环

**核心设计：每个循环严格遵循 TDD 三阶段，一次只做一个 AC 的垂直切片。**

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 400" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_c" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh2"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="400" fill="url(#bg_c)" rx="10"/>
  <text x="400" y="30" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">/coding-skill · Red → Green → Refactor 小循环</text>
  <rect x="40" y="60" width="220" height="120" rx="12" fill="#450a0a" stroke="#ef4444" stroke-width="2" filter="url(#sh2)"/>
  <circle cx="80" cy="90" r="18" fill="#ef4444"/><text x="80" y="96" text-anchor="middle" fill="#fff" font-size="14" font-weight="700">1</text>
  <text x="150" y="95" text-anchor="middle" fill="#fca5a5" font-size="13" font-weight="700">RED</text>
  <text x="150" y="115" text-anchor="middle" fill="#fecaca" font-size="11">读 change.md + 当前 AC</text>
  <text x="150" y="133" text-anchor="middle" fill="#fca5a5" font-size="11">先写编译失败的测试</text>
  <text x="150" y="151" text-anchor="middle" fill="#fecaca" font-size="11">不修改实现代码</text>
  <text x="150" y="169" text-anchor="middle" fill="#f87171" font-size="10">→ 测试失败（期望）</text>
  <line x1="260" y1="120" x2="290" y2="120" stroke="#475569" stroke-width="2"/>
  <polygon points="295,120 287,115 287,125" fill="#94a3b8"/>
  <rect x="300" y="60" width="220" height="120" rx="12" fill="#052e16" stroke="#22c55e" stroke-width="2" filter="url(#sh2)"/>
  <circle cx="340" cy="90" r="18" fill="#22c55e"/><text x="340" y="96" text-anchor="middle" fill="#fff" font-size="14" font-weight="700">2</text>
  <text x="410" y="95" text-anchor="middle" fill="#86efac" font-size="13" font-weight="700">GREEN</text>
  <text x="410" y="115" text-anchor="middle" fill="#bbf7d0" font-size="11">编写最小实现代码</text>
  <text x="410" y="133" text-anchor="middle" fill="#86efac" font-size="11">只做"让测试通过"的事</text>
  <text x="410" y="151" text-anchor="middle" fill="#bbf7d0" font-size="11">不做额外优化</text>
  <text x="410" y="169" text-anchor="middle" fill="#4ade80" font-size="10">→ 测试通过（绿色）</text>
  <line x1="520" y1="120" x2="550" y2="120" stroke="#475569" stroke-width="2"/>
  <polygon points="555,120 547,115 547,125" fill="#94a3b8"/>
  <rect x="560" y="60" width="200" height="120" rx="12" fill="#1c1917" stroke="#f59e0b" stroke-width="2" filter="url(#sh2)"/>
  <circle cx="600" cy="90" r="18" fill="#f59e0b"/><text x="600" y="96" text-anchor="middle" fill="#fff" font-size="14" font-weight="700">3</text>
  <text x="660" y="95" text-anchor="middle" fill="#fde68a" font-size="13" font-weight="700">REFACTOR</text>
  <text x="660" y="115" text-anchor="middle" fill="#fef3c7" font-size="11">在测试保护下重构</text>
  <text x="660" y="133" text-anchor="middle" fill="#fde68a" font-size="11">优化结构/命名</text>
  <text x="660" y="151" text-anchor="middle" fill="#fef3c7" font-size="11">不改变外部行为</text>
  <text x="660" y="169" text-anchor="middle" fill="#fbbf24" font-size="10">→ 测试依然通过</text>
  <path d="M 660 180 L 660 250 L 150 250 L 150 185" fill="none" stroke="#f59e0b" stroke-width="1.5" stroke-dasharray="5,3"/>
  <polygon points="150,180 145,190 155,190" fill="#f59e0b"/>
  <rect x="80" y="270" width="640" height="60" rx="10" fill="#0f172a" stroke="#334155" stroke-width="1"/>
  <text x="400" y="293" text-anchor="middle" fill="#94a3b8" font-size="11" font-weight="700">关键约束</text>
  <text x="400" y="314" text-anchor="middle" fill="#64748b" font-size="10">方法≤50行 · 文件≤500行 · 圈复杂度≤10 · 只Mock外部依赖 · 垂直切片不批量</text>
  <text x="400" y="370" text-anchor="middle" fill="#475569" font-size="11">每个循环对应一个 AC · 多个 AC 重复此循环 · 全部通过后进入下一阶段</text>
</svg>
```

**为什么"垂直切片"？** 一次只做"一个 AC 的完整垂直切片"（从 Controller 到 Repository），更快暴露"数据流是否打通"的问题；横向切片（先写所有 Controller）会延迟集成风险的暴露。

### 6.3 /unit-test-write：4 层测试矩阵

**核心设计：每个 AC 对应 4 层测试矩阵，而非 Happy Path 测试。**

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 380" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_u" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh3"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="380" fill="url(#bg_u)" rx="10"/>
  <text x="400" y="30" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">/unit-test-write · 4 层测试矩阵</text>
  <text x="400" y="50" text-anchor="middle" fill="#64748b" font-size="11">每个 AC 生成 4 个测试，而非 1 个</text>
  <rect x="30" y="70" width="170" height="120" rx="10" fill="#0ea5e9" opacity="0.15" stroke="#0ea5e9" stroke-width="1.5" filter="url(#sh3)"/>
  <rect x="30" y="70" width="170" height="32" rx="10" fill="#0ea5e9"/>
  <text x="115" y="91" text-anchor="middle" fill="#fff" font-size="12" font-weight="700">Spec Test</text>
  <text x="115" y="118" text-anchor="middle" fill="#e0f2fe" font-size="11">正向功能验证</text>
  <text x="115" y="136" text-anchor="middle" fill="#94a3b8" font-size="10">输入正常参数</text>
  <text x="115" y="152" text-anchor="middle" fill="#94a3b8" font-size="10">验证正确结果</text>
  <text x="115" y="170" text-anchor="middle" fill="#64748b" font-size="9">"应该发生的事"</text>
  <rect x="220" y="70" width="170" height="120" rx="10" fill="#f59e0b" opacity="0.15" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh3)"/>
  <rect x="220" y="70" width="170" height="32" rx="10" fill="#f59e0b"/>
  <text x="305" y="91" text-anchor="middle" fill="#fff" font-size="12" font-weight="700">Boundary</text>
  <text x="305" y="118" text-anchor="middle" fill="#fef3c7" font-size="11">边界条件验证</text>
  <text x="305" y="136" text-anchor="middle" fill="#94a3b8" font-size="10">空输入 / 极值</text>
  <text x="305" y="152" text-anchor="middle" fill="#94a3b8" font-size="10">并发 / 大数据</text>
  <text x="305" y="170" text-anchor="middle" fill="#64748b" font-size="9">"极端输入不崩"</text>
  <rect x="410" y="70" width="170" height="120" rx="10" fill="#ef4444" opacity="0.15" stroke="#ef4444" stroke-width="1.5" filter="url(#sh3)"/>
  <rect x="410" y="70" width="170" height="32" rx="10" fill="#ef4444"/>
  <text x="495" y="91" text-anchor="middle" fill="#fff" font-size="12" font-weight="700">Failure</text>
  <text x="495" y="118" text-anchor="middle" fill="#fecaca" font-size="11">异常路径验证</text>
  <text x="495" y="136" text-anchor="middle" fill="#94a3b8" font-size="10">外部依赖失败</text>
  <text x="495" y="152" text-anchor="middle" fill="#94a3b8" font-size="10">验证优雅降级</text>
  <text x="495" y="170" text-anchor="middle" fill="#64748b" font-size="9">"挂了也有处理"</text>
  <rect x="600" y="70" width="170" height="120" rx="10" fill="#8b5cf6" opacity="0.15" stroke="#8b5cf6" stroke-width="1.5" filter="url(#sh3)"/>
  <rect x="600" y="70" width="170" height="32" rx="10" fill="#8b5cf6"/>
  <text x="685" y="91" text-anchor="middle" fill="#fff" font-size="12" font-weight="700">Regression</text>
  <text x="685" y="118" text-anchor="middle" fill="#e9d5ff" font-size="11">防御性验证</text>
  <text x="685" y="136" text-anchor="middle" fill="#94a3b8" font-size="10">已修 Bug 不复发</text>
  <text x="685" y="152" text-anchor="middle" fill="#94a3b8" font-size="10">历史场景不破</text>
  <text x="685" y="170" text-anchor="middle" fill="#64748b" font-size="9">"修了的不再修"</text>
  <rect x="80" y="210" width="640" height="50" rx="10" fill="#0f172a" stroke="#334155" stroke-width="1"/>
  <text x="400" y="233" text-anchor="middle" fill="#94a3b8" font-size="11" font-weight="700">命名规范：should_&lt;期望&gt;_when_&lt;条件&gt;</text>
  <text x="400" y="252" text-anchor="middle" fill="#64748b" font-size="10">示例：should_throw_exception_when_order_is_null · 测试即活的规格文档</text>
  <rect x="80" y="275" width="640" height="50" rx="10" fill="#052e16" stroke="#22c55e" stroke-width="1"/>
  <text x="400" y="298" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">覆盖率门禁：核心逻辑 ≥ 80%</text>
  <text x="400" y="316" text-anchor="middle" fill="#64748b" font-size="10">Happy Path 只覆盖 25% 的代码路径 · 4 层矩阵强制跳出舒适区</text>
  <text x="400" y="360" text-anchor="middle" fill="#475569" font-size="11">"测试是活的规格" —— 测试即文档</text>
</svg>
```

**为什么拒绝只测 Happy Path？** 因为"功能正确"只覆盖了 25% 的代码路径。真正的软件可靠性来自边界条件、失败路径和回归。**4 层矩阵强制 AI 跳出"功能正确"的舒适区。**
### 6.4 /expert-reviewer：双轴并行评审

**核心设计：同时启动 Spec 轴和 Standards 轴两条独立评审线，达到各自的门禁才能放行。**

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 340" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_r" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh4"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="340" fill="url(#bg_r)" rx="10"/>
  <text x="400" y="30" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">/expert-reviewer · 双轴并行评审</text>
  <rect x="40" y="55" width="350" height="130" rx="10" fill="#0c1a2b" stroke="#38bdf8" stroke-width="1.5" filter="url(#sh4)"/>
  <rect x="40" y="55" width="350" height="32" rx="10" fill="#0ea5e9"/>
  <text x="215" y="76" text-anchor="middle" fill="#fff" font-size="12" font-weight="700">Spec 轴 · 功能完整性</text>
  <text x="215" y="102" text-anchor="middle" fill="#bae6fd" font-size="11">对照 change.md 检查</text>
  <text x="215" y="122" text-anchor="middle" fill="#94a3b8" font-size="10">AC 是否全部覆盖</text>
  <text x="215" y="140" text-anchor="middle" fill="#94a3b8" font-size="10">边界条件是否测试</text>
  <text x="215" y="158" text-anchor="middle" fill="#94a3b8" font-size="10">测试是否反映规格</text>
  <text x="215" y="176" text-anchor="middle" fill="#7dd3fc" font-size="10">出口：功能完整</text>
  <rect x="420" y="55" width="350" height="130" rx="10" fill="#1c1917" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh4)"/>
  <rect x="420" y="55" width="350" height="32" rx="10" fill="#f59e0b"/>
  <text x="595" y="76" text-anchor="middle" fill="#fff" font-size="12" font-weight="700">Standards 轴 · 规范合规</text>
  <text x="595" y="102" text-anchor="middle" fill="#fde68a" font-size="11">架构约束检查</text>
  <text x="595" y="122" text-anchor="middle" fill="#94a3b8" font-size="10">依赖方向 / 包结构</text>
  <text x="595" y="140" text-anchor="middle" fill="#94a3b8" font-size="10">编码规范 / 圈复杂度</text>
  <text x="595" y="158" text-anchor="middle" fill="#94a3b8" font-size="10">异常处理 / 日志</text>
  <text x="595" y="176" text-anchor="middle" fill="#fbbf24" font-size="10">出口：规范合规</text>
  <line x1="215" y1="185" x2="215" y2="215" stroke="#0ea5e9" stroke-width="1.5"/>
  <line x1="595" y1="185" x2="595" y2="215" stroke="#f59e0b" stroke-width="1.5"/>
  <line x1="215" y1="215" x2="595" y2="215" stroke="#64748b" stroke-width="1.5"/>
  <line x1="400" y1="215" x2="400" y2="235" stroke="#64748b" stroke-width="1.5"/>
  <polygon points="400,242 394,233 406,233" fill="#64748b"/>
  <rect x="160" y="248" width="480" height="50" rx="12" fill="#052e16" stroke="#22c55e" stroke-width="1.5" filter="url(#sh4)"/>
  <text x="400" y="272" text-anchor="middle" fill="#86efac" font-size="13" font-weight="700">门禁：0 个严重问题才放行</text>
  <text x="400" y="290" text-anchor="middle" fill="#64748b" font-size="10">建议问题可接受但需记录 · 双轴都绿才进入下一阶段</text>
  <text x="400" y="328" text-anchor="middle" fill="#475569" font-size="11">功能正确性 + 规范合规性 = 正交维度 · 双轴互不干扰</text>
</svg>
```

**为什么双轴并行？** 功能正确性和规范合规性是正交的——一个代码可能"功能完全正确但严重违反工程规范"，也可能"完全规范但功能漏了三分之二"。合并成一条轴会互相干扰。

### 6.5 /unit-test-ci：机械化门禁

**核心设计：机械化执行，消灭人工判断空间——同样的输入永远得到同样的输出。**

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 340" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_ci" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh5"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="340" fill="url(#bg_ci)" rx="10"/>
  <text x="400" y="30" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">/unit-test-ci · 机械化门禁流水线</text>
  <text x="400" y="50" text-anchor="middle" fill="#64748b" font-size="11">任一失败即红灯 · 红灯不允许被解释或豁免</text>
  <rect x="30" y="75" width="105" height="50" rx="8" fill="#ef4444" filter="url(#sh5)"/>
  <text x="82" y="98" text-anchor="middle" fill="#fff" font-size="11" font-weight="700">1. 编译</text>
  <text x="82" y="115" text-anchor="middle" fill="#fecaca" font-size="9">编译检查</text>
  <line x1="135" y1="100" x2="150" y2="100" stroke="#475569" stroke-width="1.5"/>
  <polygon points="153,100 147,96 147,104" fill="#94a3b8"/>
  <rect x="158" y="75" width="105" height="50" rx="8" fill="#f97316" filter="url(#sh5)"/>
  <text x="210" y="98" text-anchor="middle" fill="#fff" font-size="11" font-weight="700">2. Lint</text>
  <text x="210" y="115" text-anchor="middle" fill="#fed7aa" font-size="9">静态分析</text>
  <line x1="263" y1="100" x2="278" y2="100" stroke="#475569" stroke-width="1.5"/>
  <polygon points="281,100 275,96 275,104" fill="#94a3b8"/>
  <rect x="286" y="75" width="105" height="50" rx="8" fill="#f59e0b" filter="url(#sh5)"/>
  <text x="338" y="98" text-anchor="middle" fill="#fff" font-size="11" font-weight="700">3. 架构</text>
  <text x="338" y="115" text-anchor="middle" fill="#fef3c7" font-size="9">约束检查</text>
  <line x1="391" y1="100" x2="406" y2="100" stroke="#475569" stroke-width="1.5"/>
  <polygon points="409,100 403,96 403,104" fill="#94a3b8"/>
  <rect x="414" y="75" width="105" height="50" rx="8" fill="#0ea5e9" filter="url(#sh5)"/>
  <text x="466" y="98" text-anchor="middle" fill="#fff" font-size="11" font-weight="700">4. 竞态</text>
  <text x="466" y="115" text-anchor="middle" fill="#e0f2fe" font-size="9">竞态检测</text>
  <line x1="519" y1="100" x2="534" y2="100" stroke="#475569" stroke-width="1.5"/>
  <polygon points="537,100 531,96 531,104" fill="#94a3b8"/>
  <rect x="542" y="75" width="105" height="50" rx="8" fill="#8b5cf6" filter="url(#sh5)"/>
  <text x="594" y="98" text-anchor="middle" fill="#fff" font-size="11" font-weight="700">5. 测试</text>
  <text x="594" y="115" text-anchor="middle" fill="#e9d5ff" font-size="9">全量测试</text>
  <line x1="647" y1="100" x2="662" y2="100" stroke="#475569" stroke-width="1.5"/>
  <polygon points="665,100 659,96 659,104" fill="#94a3b8"/>
  <rect x="670" y="75" width="105" height="50" rx="8" fill="#22c55e" filter="url(#sh5)"/>
  <text x="722" y="98" text-anchor="middle" fill="#fff" font-size="11" font-weight="700">6. 覆盖</text>
  <text x="722" y="115" text-anchor="middle" fill="#bbf7d0" font-size="9">覆盖率检查</text>
  <rect x="200" y="150" width="400" height="50" rx="12" fill="#450a0a" stroke="#ef4444" stroke-width="1.5" filter="url(#sh5)"/>
  <text x="400" y="174" text-anchor="middle" fill="#fca5a5" font-size="13" font-weight="700">任一失败 → 红灯 · 禁止进入下一阶段</text>
  <text x="400" y="192" text-anchor="middle" fill="#64748b" font-size="10">失败原因记录到 review.md · 修复后重新运行</text>
  <line x1="400" y1="200" x2="400" y2="220" stroke="#ef4444" stroke-width="1.5"/>
  <polygon points="400,226 394,217 406,217" fill="#ef4444"/>
  <rect x="200" y="232" width="400" height="50" rx="12" fill="#052e16" stroke="#22c55e" stroke-width="1.5" filter="url(#sh5)"/>
  <text x="400" y="256" text-anchor="middle" fill="#86efac" font-size="13" font-weight="700">全部通过 → 绿灯 · 进入部署验证</text>
  <text x="400" y="274" text-anchor="middle" fill="#64748b" font-size="10">安全扫描作为附加检查 · 扫描结果记录到 verify.md</text>
  <text x="400" y="325" text-anchor="middle" fill="#475569" font-size="11">机械化 = 确定性 · 人工判断在 CI 阶段是可靠性最差的环节</text>
</svg>
```

### 6.6 /deploy-verify："CI 绿" ≠ "线上可用"

**核心设计：把部署验证与 CI 分离，因为两者验证的是不同的问题。**

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 340" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_d" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh6"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="340" fill="url(#bg_d)" rx="10"/>
  <text x="400" y="30" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">/deploy-verify · CI 绿 ≠ 线上可用</text>
  <text x="400" y="50" text-anchor="middle" fill="#64748b" font-size="11">CI 验证工程规范 · 部署验证线上可用性</text>
  <rect x="20" y="70" width="240" height="50" rx="8" fill="#0f172a" stroke="#334155" stroke-width="1" filter="url(#sh6)"/>
  <text x="140" y="93" text-anchor="middle" fill="#e2e8f0" font-size="12" font-weight="700">1. 健康检查</text>
  <text x="140" y="110" text-anchor="middle" fill="#94a3b8" font-size="10">/health /ready 端点返回 200</text>
  <line x1="260" y1="95" x2="285" y2="95" stroke="#475569" stroke-width="1.5"/>
  <polygon points="290,95 282,90 282,100" fill="#94a3b8"/>
  <rect x="295" y="70" width="240" height="50" rx="8" fill="#0f172a" stroke="#334155" stroke-width="1" filter="url(#sh6)"/>
  <text x="415" y="93" text-anchor="middle" fill="#e2e8f0" font-size="12" font-weight="700">2. 关键链路</text>
  <text x="415" y="110" text-anchor="middle" fill="#94a3b8" font-size="10">核心业务流程返回正确</text>
  <line x1="535" y1="95" x2="560" y2="95" stroke="#475569" stroke-width="1.5"/>
  <polygon points="565,95 557,90 557,100" fill="#94a3b8"/>
  <rect x="570" y="70" width="210" height="50" rx="8" fill="#0f172a" stroke="#334155" stroke-width="1" filter="url(#sh6)"/>
  <text x="675" y="93" text-anchor="middle" fill="#e2e8f0" font-size="12" font-weight="700">3. 安全扫描</text>
  <text x="675" y="110" text-anchor="middle" fill="#94a3b8" font-size="10">端口/依赖/配置</text>
  <line x1="400" y1="120" x2="400" y2="145" stroke="#475569" stroke-width="1.5"/>
  <polygon points="400,150 394,142 406,142" fill="#94a3b8"/>
  <rect x="50" y="155" width="700" height="50" rx="10" fill="#1e1b4b" stroke="#818cf8" stroke-width="1" filter="url(#sh6)"/>
  <text x="400" y="178" text-anchor="middle" fill="#c7d2fe" font-size="12" font-weight="700">验证结果写入 verify.md 并存档到 .harness/verify/</text>
  <text x="400" y="196" text-anchor="middle" fill="#94a3b8" font-size="10">每次部署的验证结果形成历史记录 · 可追溯每一次部署的健康状况</text>
  <line x1="400" y1="205" x2="400" y2="225" stroke="#475569" stroke-width="1.5"/>
  <polygon points="400,230 394,222 406,222" fill="#94a3b8"/>
  <rect x="250" y="235" width="300" height="50" rx="12" fill="#052e16" stroke="#22c55e" stroke-width="1.5" filter="url(#sh6)"/>
  <text x="400" y="259" text-anchor="middle" fill="#86efac" font-size="13" font-weight="700">积累部署历史 · 持续优化</text>
  <text x="400" y="277" text-anchor="middle" fill="#64748b" font-size="10">通过历史数据发现"哪些模块容易出问题"</text>
  <text x="400" y="325" text-anchor="middle" fill="#475569" font-size="11">部署验证 = 用生产环境的标准验证代码 · 不是用单元测试的标准</text>
</svg>
```

**为什么分离？** 因为 CI 验证的是"代码是否满足工程规范"，部署验证验证的是"代码是否真的能在生产环境跑起来"。一个编译通过、测试全绿的应用，在线上可能因配置错误、依赖超时、内存不足等各种原因无法启动。部署验证就是那道最后的防线。
### 6.7 辅助命令的设计哲学

除了 6 个核心流水线命令，还有 5 个辅助命令构成完整的脚手架生态。

#### 6.7.1 /domain-modeling：领域模型的"活文档"

**核心设计：把领域模型视为"活的"——使用过程中不断更新，而非一次性产出的文档。**

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 780 300" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_dm" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh7"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="780" height="300" fill="url(#bg_dm)" rx="10"/>
  <text x="390" y="30" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">/domain-modeling · 活的领域模型</text>
  <rect x="40" y="55" width="220" height="60" rx="10" fill="#0ea5e9" opacity="0.15" stroke="#0ea5e9" stroke-width="1.5" filter="url(#sh7)"/>
  <text x="150" y="80" text-anchor="middle" fill="#7dd3fc" font-size="12" font-weight="700">① 提取领域术语</text>
  <text x="150" y="100" text-anchor="middle" fill="#94a3b8" font-size="10">从代码和对话中提取</text>
  <line x1="260" y1="85" x2="280" y2="85" stroke="#475569" stroke-width="1.5"/>
  <polygon points="285,85 277,80 277,90" fill="#94a3b8"/>
  <rect x="290" y="55" width="220" height="60" rx="10" fill="#8b5cf6" opacity="0.15" stroke="#8b5cf6" stroke-width="1.5" filter="url(#sh7)"/>
  <text x="400" y="80" text-anchor="middle" fill="#c4b5fd" font-size="12" font-weight="700">② 记录定义</text>
  <text x="400" y="100" text-anchor="middle" fill="#94a3b8" font-size="10">术语+定义+上下文</text>
  <line x1="510" y1="85" x2="530" y2="85" stroke="#475569" stroke-width="1.5"/>
  <polygon points="535,85 527,80 527,90" fill="#94a3b8"/>
  <rect x="540" y="55" width="200" height="60" rx="10" fill="#10b981" opacity="0.15" stroke="#10b981" stroke-width="1.5" filter="url(#sh7)"/>
  <text x="640" y="80" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">③ 持续更新</text>
  <text x="640" y="100" text-anchor="middle" fill="#94a3b8" font-size="10">每次使用都刷新</text>
  <line x1="390" y1="115" x2="390" y2="140" stroke="#475569" stroke-width="1.5"/>
  <polygon points="390,145 384,137 396,137" fill="#94a3b8"/>
  <rect x="190" y="150" width="400" height="50" rx="10" fill="#0f172a" stroke="#334155" stroke-width="1" filter="url(#sh7)"/>
  <text x="390" y="173" text-anchor="middle" fill="#e2e8f0" font-size="12" font-weight="700">模型存储在 .harness/domain-model.md</text>
  <text x="390" y="190" text-anchor="middle" fill="#64748b" font-size="10">与代码一起版本控制 · 永不掉队</text>
  <line x1="390" y1="200" x2="390" y2="220" stroke="#475569" stroke-width="1.5"/>
  <polygon points="390,225 384,217 396,217" fill="#94a3b8"/>
  <rect x="140" y="230" width="500" height="40" rx="10" fill="#1c1917" stroke="#f59e0b" stroke-width="1" filter="url(#sh7)"/>
  <text x="390" y="250" text-anchor="middle" fill="#fde68a" font-size="11" font-weight="700">"一致的语言是所有软件项目长期健康的前提"</text>
  <text x="390" y="263" text-anchor="middle" fill="#64748b" font-size="9">没有领域模型，每个开发者都在用自己的语言描述同一个概念</text>
</svg>
```

**为什么是"活的"而不是"设计的"？** 传统软件工程中，领域模型在项目初期由架构师一次性产出，然后随着代码演化逐渐失活。这里反其道而行之：模型在**使用过程中持续更新**，每次调用都刷新。它和代码放在一起版本控制，永不掉队。

#### 6.7.2 /diagnosing-bugs：假设驱动的侦探

**核心设计：先建立"稳定复现"的反馈循环，再分析根因。不假设，不跳过复现步骤。**

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 780 300" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_diag" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh8"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="780" height="300" fill="url(#bg_diag)" rx="10"/>
  <text x="390" y="30" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">/diagnosing-bugs · 假设驱动的侦探流程</text>
  <rect x="30" y="55" width="140" height="60" rx="8" fill="#ef4444" opacity="0.15" stroke="#ef4444" stroke-width="1.5" filter="url(#sh8)"/>
  <text x="100" y="80" text-anchor="middle" fill="#fca5a5" font-size="11" font-weight="700">1. 建立复现</text>
  <text x="100" y="98" text-anchor="middle" fill="#94a3b8" font-size="9">稳定复现 Bug</text>
  <line x1="170" y1="85" x2="185" y2="85" stroke="#475569" stroke-width="1.5"/>
  <polygon points="190,85 182,80 182,90" fill="#94a3b8"/>
  <rect x="195" y="55" width="140" height="60" rx="8" fill="#f59e0b" opacity="0.15" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh8)"/>
  <text x="265" y="80" text-anchor="middle" fill="#fde68a" font-size="11" font-weight="700">2. 提出假设</text>
  <text x="265" y="98" text-anchor="middle" fill="#94a3b8" font-size="9">根因候选列表</text>
  <line x1="335" y1="85" x2="350" y2="85" stroke="#475569" stroke-width="1.5"/>
  <polygon points="355,85 347,80 347,90" fill="#94a3b8"/>
  <rect x="360" y="55" width="140" height="60" rx="8" fill="#0ea5e9" opacity="0.15" stroke="#0ea5e9" stroke-width="1.5" filter="url(#sh8)"/>
  <text x="430" y="80" text-anchor="middle" fill="#7dd3fc" font-size="11" font-weight="700">3. 验证假设</text>
  <text x="430" y="98" text-anchor="middle" fill="#94a3b8" font-size="9">逐一排除</text>
  <line x1="500" y1="85" x2="515" y2="85" stroke="#475569" stroke-width="1.5"/>
  <polygon points="520,85 512,80 512,90" fill="#94a3b8"/>
  <rect x="525" y="55" width="140" height="60" rx="8" fill="#22c55e" opacity="0.15" stroke="#22c55e" stroke-width="1.5" filter="url(#sh8)"/>
  <text x="595" y="80" text-anchor="middle" fill="#86efac" font-size="11" font-weight="700">4. 修复验证</text>
  <text x="595" y="98" text-anchor="middle" fill="#94a3b8" font-size="9">修复后跑回归</text>
  <path d="M 595 115 L 595 180 L 100 180 L 100 118" fill="none" stroke="#f59e0b" stroke-width="1.5" stroke-dasharray="5,3"/>
  <polygon points="100,113 95,123 105,123" fill="#f59e0b"/>
  <rect x="80" y="195" width="620" height="50" rx="10" fill="#0f172a" stroke="#334155" stroke-width="1" filter="url(#sh8)"/>
  <text x="390" y="218" text-anchor="middle" fill="#e2e8f0" font-size="12" font-weight="700">黄金法则：不假设，不跳过复现步骤</text>
  <text x="390" y="236" text-anchor="middle" fill="#64748b" font-size="10">不能稳定复现的 Bug 无法被确认修复 · 复现不是可选项</text>
  <text x="390" y="288" text-anchor="middle" fill="#475569" font-size="11">"在没有建立稳定复现之前，任何根因分析都是猜测"</text>
</svg>
```

**黄金法则**：不能稳定复现的 Bug 无法被确认修复。复现不是可选项。这个命令的独特之处在于：它强制 AI 先写一个**复现测试**（Red），然后做根因分析，再修 Bug（Green），最后跑回归。这和 coding-skill 的 Red-Green-Refactor 循环一脉相承。

#### 6.7.3 /arch-review-golang：架构的"定期体检"

**核心设计：定期扫描代码库，发现架构摩擦点，生成可视化报告。**

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 780 260" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_ar" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh9"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="780" height="260" fill="url(#bg_ar)" rx="10"/>
  <text x="390" y="30" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">/arch-review-golang · 架构定期体检</text>
  <rect x="40" y="55" width="220" height="60" rx="10" fill="#0ea5e9" opacity="0.15" stroke="#0ea5e9" stroke-width="1.5" filter="url(#sh9)"/>
  <text x="150" y="80" text-anchor="middle" fill="#7dd3fc" font-size="12" font-weight="700">扫描代码库</text>
  <text x="150" y="100" text-anchor="middle" fill="#94a3b8" font-size="10">分析依赖/包结构/圈复杂度</text>
  <line x1="260" y1="85" x2="280" y2="85" stroke="#475569" stroke-width="1.5"/>
  <polygon points="285,85 277,80 277,90" fill="#94a3b8"/>
  <rect x="290" y="55" width="220" height="60" rx="10" fill="#f59e0b" opacity="0.15" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh9)"/>
  <text x="400" y="80" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">发现摩擦点</text>
  <text x="400" y="100" text-anchor="middle" fill="#94a3b8" font-size="10">循环依赖/包边界/数据流</text>
  <line x1="510" y1="85" x2="530" y2="85" stroke="#475569" stroke-width="1.5"/>
  <polygon points="535,85 527,80 527,90" fill="#94a3b8"/>
  <rect x="540" y="55" width="200" height="60" rx="10" fill="#22c55e" opacity="0.15" stroke="#22c55e" stroke-width="1.5" filter="url(#sh9)"/>
  <text x="640" y="80" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">生成报告</text>
  <text x="640" y="100" text-anchor="middle" fill="#94a3b8" font-size="10">可视化改进建议</text>
  <line x1="390" y1="115" x2="390" y2="140" stroke="#475569" stroke-width="1.5"/>
  <polygon points="390,145 384,137 396,137" fill="#94a3b8"/>
  <rect x="90" y="150" width="600" height="50" rx="10" fill="#0f172a" stroke="#334155" stroke-width="1" filter="url(#sh9)"/>
  <text x="390" y="173" text-anchor="middle" fill="#e2e8f0" font-size="12" font-weight="700">输出：架构健康报告 + 可视化 SVG 图 + 改进优先级清单</text>
  <text x="390" y="190" text-anchor="middle" fill="#64748b" font-size="10">每个摩擦点标注严重度 · 给出具体改进方案</text>
  <text x="390" y="250" text-anchor="middle" fill="#475569" font-size="11">"架构不是在会议室里设计的，而是在代码里演化的"</text>
</svg>
```

**为什么需要"架构体检"而不是"架构设计"？** 因为架构不是一次性设计出来的，而是在代码演化过程中逐渐形成的。定期体检能发现"架构债"——那些在快速迭代中积累的、需要偿还的设计债务。

#### 6.7.4 /handoff-golang：上下文的"序列化"

**核心设计：将当前对话上下文压缩成可被另一个对话无缝续接的结构化文档。** 这是脚手架的"跨会话协作"机制。当对话太长、上下文溢出时，调用此命令将当前项目状态、进展、待办事项打包成交接文档，在新对话中加载即可无缝续接。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 780 200" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_h" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh10"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="780" height="200" fill="url(#bg_h)" rx="10"/>
  <text x="390" y="30" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">/handoff-golang · 上下文的序列化</text>
  <rect x="30" y="55" width="220" height="60" rx="10" fill="#0ea5e9" opacity="0.15" stroke="#0ea5e9" stroke-width="1.5" filter="url(#sh10)"/>
  <text x="140" y="80" text-anchor="middle" fill="#7dd3fc" font-size="12" font-weight="700">① 压缩当前上下文</text>
  <text x="140" y="100" text-anchor="middle" fill="#94a3b8" font-size="10">项目状态+进展+待办</text>
  <line x1="250" y1="85" x2="270" y2="85" stroke="#475569" stroke-width="1.5"/>
  <polygon points="275,85 267,80 267,90" fill="#94a3b8"/>
  <rect x="280" y="55" width="220" height="60" rx="10" fill="#8b5cf6" opacity="0.15" stroke="#8b5cf6" stroke-width="1.5" filter="url(#sh10)"/>
  <text x="390" y="80" text-anchor="middle" fill="#c4b5fd" font-size="12" font-weight="700">② 生成交接文档</text>
  <text x="390" y="100" text-anchor="middle" fill="#94a3b8" font-size="10">结构化 + 可执行</text>
  <line x1="500" y1="85" x2="520" y2="85" stroke="#475569" stroke-width="1.5"/>
  <polygon points="525,85 517,80 517,90" fill="#94a3b8"/>
  <rect x="530" y="55" width="220" height="60" rx="10" fill="#22c55e" opacity="0.15" stroke="#22c55e" stroke-width="1.5" filter="url(#sh10)"/>
  <text x="640" y="80" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">③ 新对话加载</text>
  <text x="640" y="100" text-anchor="middle" fill="#94a3b8" font-size="10">无缝续接工作</text>
  <line x1="390" y1="115" x2="390" y2="140" stroke="#475569" stroke-width="1.5"/>
  <polygon points="390,145 384,137 396,137" fill="#94a3b8"/>
  <rect x="90" y="150" width="600" height="36" rx="10" fill="#0f172a" stroke="#334155" stroke-width="1" filter="url(#sh10)"/>
  <text x="390" y="173" text-anchor="middle" fill="#e2e8f0" font-size="12" font-weight="700">"跨会话协作 · 上下文永不丢失"</text>
</svg>
```

#### 6.7.5 /harness-me-golang：灵魂拷问

**核心设计：一场"苏格拉底式"的对话，一次只问一个问题，帮助用户打磨需求、方案或设计。**

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 780 260" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_hm" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh11"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="780" height="260" fill="url(#bg_hm)" rx="10"/>
  <text x="390" y="30" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">/harness-me-golang · 苏格拉底式灵魂拷问</text>
  <rect x="40" y="55" width="140" height="60" rx="8" fill="#6366f1" opacity="0.15" stroke="#6366f1" stroke-width="1.5" filter="url(#sh11)"/>
  <text x="110" y="80" text-anchor="middle" fill="#a5b4fc" font-size="11" font-weight="700">用户提出想法</text>
  <text x="110" y="98" text-anchor="middle" fill="#94a3b8" font-size="9">需求/方案/设计</text>
  <line x1="180" y1="85" x2="200" y2="85" stroke="#475569" stroke-width="1.5"/>
  <polygon points="205,85 197,80 197,90" fill="#94a3b8"/>
  <rect x="210" y="55" width="180" height="60" rx="8" fill="#f59e0b" opacity="0.15" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh11)"/>
  <text x="300" y="80" text-anchor="middle" fill="#fde68a" font-size="11" font-weight="700">AI 一次问一个问题</text>
  <text x="300" y="98" text-anchor="middle" fill="#94a3b8" font-size="9">针对性灵魂拷问</text>
  <line x1="390" y1="85" x2="410" y2="85" stroke="#475569" stroke-width="1.5"/>
  <polygon points="415,85 407,80 407,90" fill="#94a3b8"/>
  <rect x="420" y="55" width="180" height="60" rx="8" fill="#0ea5e9" opacity="0.15" stroke="#0ea5e9" stroke-width="1.5" filter="url(#sh11)"/>
  <text x="510" y="80" text-anchor="middle" fill="#7dd3fc" font-size="11" font-weight="700">用户回答</text>
  <text x="510" y="98" text-anchor="middle" fill="#94a3b8" font-size="9">给出思考结果</text>
  <line x1="600" y1="85" x2="620" y2="85" stroke="#475569" stroke-width="1.5"/>
  <polygon points="625,85 617,80 617,90" fill="#94a3b8"/>
  <rect x="630" y="55" width="120" height="60" rx="8" fill="#22c55e" opacity="0.15" stroke="#22c55e" stroke-width="1.5" filter="url(#sh11)"/>
  <text x="690" y="80" text-anchor="middle" fill="#86efac" font-size="11" font-weight="700">打磨完成</text>
  <text x="690" y="98" text-anchor="middle" fill="#94a3b8" font-size="9">方案成熟</text>
  <path d="M 690 115 L 690 170 L 300 170 L 300 118" fill="none" stroke="#6366f1" stroke-width="1.5" stroke-dasharray="5,3"/>
  <polygon points="300,113 295,123 305,123" fill="#6366f1"/>
  <rect x="80" y="185" width="620" height="50" rx="10" fill="#0f172a" stroke="#334155" stroke-width="1" filter="url(#sh11)"/>
  <text x="390" y="208" text-anchor="middle" fill="#e2e8f0" font-size="12" font-weight="700">"不要问 AI 能做什么 · 问 AI 应该问什么"</text>
  <text x="390" y="226" text-anchor="middle" fill="#64748b" font-size="10">适合在开始任何工作之前使用 · 避免"做正确的事"之前先"正确地做事"</text>
</svg>
```

**适合场景**：在开始任何工作之前使用。避免"做正确的事"之前先"正确地做事"——先确认方向，再优化速度。

---

### 6.8 命令之间的协作关系

6 个核心命令 + 5 个辅助命令不是孤立的，它们构成一个完整的生命周期：

| 阶段 | 命令 | 输入 | 输出 |
|------|------|------|------|
| 需求分析 | /harnessing | 用户描述 | 需求总结卡片 |
| 敲定方案 | /harness-me-golang | 需求卡片 | 方案确认 |
| 领域建模 | /domain-modeling | 术语/代码 | 领域模型 |
| 编码实现 | /coding-skill | change.md + AC | 实现代码 + 测试 |
| 代码审查 | /expert-reviewer | 实现代码 | 审查报告 |
| 质量门禁 | /unit-test-ci | 代码 + 测试 | 通过/失败 |
| 部署验证 | /deploy-verify | 部署版本 | 验证报告 |
| Bug 诊断 | /diagnosing-bugs | Bug 描述 | 修复代码 |
| 架构体检 | /arch-review-golang | 代码库 | 架构报告 |
| 交接 | /handoff-golang | 当前上下文 | 交接文档 |

**核心思想**：不同命令关注不同维度的"风险"：

- **/harnessing**：管理"需求风险"——做正确的事
- **/coding-skill**：管理"实现风险"——正确地做事
- **/unit-test-write**：管理"质量风险"——做对的事
- **/expert-reviewer**：管理"规范风险"——做好的事
- **/unit-test-ci**：管理"回归风险"——持续做对
- **/deploy-verify**：管理"上线风险"——真的能跑

每个命令只关注一个维度，但组合起来覆盖了软件交付的全生命周期风险。

## 七、多语言 / 框架识别支持

### 7.1 识别机制的定位：脚手架的"哨兵"

多语言多框架识别是整个脚手架的"哨兵"——它决定了项目"是什么语言、什么框架、什么构建工具"，进而决定了后续所有参数的选择。识别错了，后面的一切参数化都是错的。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1000 720" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg5" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/>
    </linearGradient>
    <linearGradient id="d1" x1="0" y1="0" x2="1" y2="0">
      <stop offset="0%" stop-color="#6366f1"/><stop offset="100%" stop-color="#8b5cf6"/>
    </linearGradient>
    <linearGradient id="d2" x1="0" y1="0" x2="1" y2="0">
      <stop offset="0%" stop-color="#0ea5e9"/><stop offset="100%" stop-color="#06b6d4"/>
    </linearGradient>
    <linearGradient id="d3" x1="0" y1="0" x2="1" y2="0">
      <stop offset="0%" stop-color="#34d399"/><stop offset="100%" stop-color="#10b981"/>
    </linearGradient>
    <linearGradient id="d4" x1="0" y1="0" x2="1" y2="0">
      <stop offset="0%" stop-color="#f59e0b"/><stop offset="100%" stop-color="#f97316"/>
    </linearGradient>
    <filter id="sh5" x="-20%" y="-20%" width="140%" height="140%">
      <feDropShadow dx="0" dy="5" stdDeviation="6" flood-color="#000" flood-opacity="0.4"/>
    </filter>
  </defs>

  <rect width="1000" height="720" fill="url(#bg5)"/>
  <text x="500" y="38" text-anchor="middle" fill="#f1f5f9" font-size="28" font-weight="700" letter-spacing="1">多语言识别 · 精确优先，模糊兜底，多语言不擅选</text>

  <!-- 开始 -->
  <rect x="300" y="60" width="400" height="46" rx="23" fill="url(#d1)" filter="url(#sh5)"/>
  <text x="500" y="88" text-anchor="middle" fill="#fff" font-size="14" font-weight="700">扫描项目根目录检测信号文件</text>

  <line x1="500" y1="106" x2="500" y2="132" stroke="#6366f1" stroke-width="2"/>
  <polygon points="500,138 493,127 507,127" fill="#6366f1"/>

  <!-- 语言分支 -->
  <rect x="60" y="142" width="200" height="52" rx="10" fill="url(#d3)" filter="url(#sh5)"/>
  <text x="160" y="166" text-anchor="middle" fill="#fff" font-size="13" font-weight="700">pom.xml → Java</text>
  <text x="160" y="184" text-anchor="middle" fill="#d1fae5" font-size="10">build.gradle → Java</text>

  <rect x="285" y="142" width="200" height="52" rx="10" fill="url(#d3)" filter="url(#sh5)"/>
  <text x="385" y="166" text-anchor="middle" fill="#fff" font-size="13" font-weight="700">pyproj.toml → Python</text>
  <text x="385" y="184" text-anchor="middle" fill="#d1fae5" font-size="10">requirements.txt → Python</text>

  <rect x="510" y="142" width="200" height="52" rx="10" fill="url(#d3)" filter="url(#sh5)"/>
  <text x="610" y="166" text-anchor="middle" fill="#fff" font-size="13" font-weight="700">go.mod → Go</text>
  <text x="610" y="184" text-anchor="middle" fill="#d1fae5" font-size="10">go.sum → Go</text>

  <rect x="735" y="142" width="190" height="52" rx="10" fill="url(#d3)" filter="url(#sh5)"/>
  <text x="830" y="166" text-anchor="middle" fill="#fff" font-size="13" font-weight="700">package.json → Frontend</text>
  <text x="830" y="184" text-anchor="middle" fill="#d1fae5" font-size="10">vue/react 配置 → Frontend</text>

  <!-- 多语言分支 -->
  <rect x="60" y="210" width="880" height="44" rx="10" fill="#7c2d12" stroke="#fb923c" stroke-width="1.5" filter="url(#sh5)"/>
  <text x="500" y="228" text-anchor="middle" fill="#fed7aa" font-size="13" font-weight="700">⚠️ 检测到多个语言信号</text>
  <text x="500" y="246" text-anchor="middle" fill="#fdba74" font-size="11">→ 禁止擅自选择 · 必须询问用户确认主语言</text>

  <line x1="500" y1="254" x2="500" y2="292" stroke="#f97316" stroke-width="2" stroke-dasharray="6,4"/>
  <polygon points="500,298 493,287 507,287" fill="#f97316"/>

  <!-- 框架匹配 -->
  <rect x="160" y="302" width="680" height="54" rx="10" fill="#0c1a2b" stroke="#38bdf8" stroke-width="1.5" filter="url(#sh5)"/>
  <text x="500" y="328" text-anchor="middle" fill="#bae6fd" font-size="13" font-weight="700">按精确度优先级遍历框架检测表</text>
  <text x="500" y="348" text-anchor="middle" fill="#94a3b8" font-size="11">pom.xml + spring-boot-starter → Spring Boot · pom.xml + dubbo → Dubbo · pom.xml → 询问</text>

  <line x1="500" y1="356" x2="500" y2="384" stroke="#0ea5e9" stroke-width="2"/>
  <polygon points="500,390 493,379 507,379" fill="#0ea5e9"/>

  <!-- 确定性 vs 兜底 -->
  <rect x="120" y="394" width="360" height="90" rx="12" fill="url(#d2)" filter="url(#sh5)"/>
  <text x="300" y="422" text-anchor="middle" fill="#fff" font-size="14" font-weight="700">✅ 高置信度命中</text>
  <text x="300" y="444" text-anchor="middle" fill="#e0f2fe" font-size="11">明确匹配到某个框架 →</text>
  <text x="300" y="462" text-anchor="middle" fill="#bae6fd" font-size="11">直接采用其参数块</text>

  <rect x="520" y="394" width="360" height="90" rx="12" fill="url(#d4)" filter="url(#sh5)"/>
  <text x="700" y="422" text-anchor="middle" fill="#fff" font-size="14" font-weight="700">❓ 低置信度 / 未命中</text>
  <text x="700" y="444" text-anchor="middle" fill="#fef3c7" font-size="11">存在构建文件但框架未知 →</text>
  <text x="700" y="462" text-anchor="middle" fill="#fde68a" font-size="11">询问用户 · 不臆测</text>

  <line x1="300" y1="484" x2="300" y2="512" stroke="#0ea5e9" stroke-width="2"/>
  <polygon points="300,518 293,507 307,507" fill="#0ea5e9"/>
  <line x1="700" y1="484" x2="700" y2="512" stroke="#f97316" stroke-width="2"/>
  <polygon points="700,518 693,507 707,507" fill="#f97316"/>

  <!-- 输出 -->
  <rect x="200" y="522" width="600" height="60" rx="14" fill="url(#d1)" filter="url(#sh5)"/>
  <text x="500" y="550" text-anchor="middle" fill="#fff" font-size="15" font-weight="700">输出（语言, 框架, 构建工具）三元组</text>
  <text x="500" y="570" text-anchor="middle" fill="#e0e7ff" font-size="12">如 (Java, Spring Boot, Maven) → 查询参数块 → 渲染</text>

  <!-- 不变式 -->
  <rect x="200" y="600" width="600" height="60" rx="14" fill="#0f172a" stroke="#334155" stroke-width="1"/>
  <text x="500" y="628" text-anchor="middle" fill="#94a3b8" font-size="12" font-weight="700">识别不变式</text>
  <text x="500" y="648" text-anchor="middle" fill="#64748b" font-size="11">①精确优先 ②多语言不擅自选择 ③低置信度必询问 ④禁止跳过 Step 1 硬编码默认语言</text>
</svg>
```

### 7.2 识别机制的四个不变式

1. **精确优先**：检测规则按匹配精确度降序排列，最确定的规则最先匹配。
2. **多语言不擅选**：如果检测到多个语言信号，**禁止**擅自选择，必须询问用户确认主语言。
3. **低置信度必询问**：存在构建文件但框架未知时，宁可多问一次，也不臆测。
4. **禁止跳过 Step 1**：禁止跳过检测步骤直接采用默认语言——检测是整个参数化的地基。

这四个不变式共同保证了**"识别结果的确定性"**——识别错了，一切参数化都是错的，所以识别环节宁可保守，不可激进。

### 7.3 检测表示例（部分）

**Java 检测表**（按优先级排列）：

| 检测信号 | 识别框架 | 构建工具 |
|---------|---------|---------|
| `pom.xml` + `spring-boot-starter` | Spring Boot | Maven |
| `pom.xml` + `spring-webmvc` | Spring MVC | Maven |
| `pom.xml` + `dubbo` 依赖 | Dubbo | Maven |
| `pom.xml` + `spring-cloud-alibaba` / `nacos-client` | Spring Cloud Alibaba | Maven |
| `pom.xml` + `spring-ai` | Spring AI | Maven |
| `pom.xml` + `langchain4j` | LangChain4j | Maven |
| `pom.xml` + `quarkus` plugin | Quarkus | Maven |
| `build.gradle.kts` + `spring-boot` plugin | Spring Boot | Gradle |

**Python 检测表**（按优先级排列）：

| 检测信号 | 识别框架 | 构建工具 |
|---------|---------|---------|
| `requirements.txt` + `django` | Django | pip |
| `requirements.txt` + `fastapi` | FastAPI | pip |
| `requirements.txt` + `flask` | Flask | pip |
| `requirements.txt` + `tensorflow` | TensorFlow | pip |
| `requirements.txt` + `torch` | PyTorch | pip |
| `requirements.txt` + `langchain` | LangChain | pip |
| `pyproject.toml`（通用） | 询问用户 | pip/poetry/uv |

**Go 检测表**（按优先级排列）：

| 检测信号 | 识别框架 | 构建工具 |
|---------|---------|---------|
| `go.mod` + `gin` | Gin | go mod |
| `go.mod` + `go-zero` | go-zero | go mod |
| `go.mod` + `echo` | Echo | go mod |
| `go.mod` + `kitex` | Kitex | go mod |
| `go.mod` + `goframe` | GoFrame | go mod |
| `go.mod` + `langchaingo` | LangChainGo | go mod |
| `go.mod`（通用） | 询问用户 | go mod |

---

## 八、多 AI 工具识别、注册与安装技能包

### 8.1 兼容性的本质：格式适配

不同的 AI 编程工具，技能/知识/能力的安装位置和格式完全不同。这是"工具无关性"目标下最直接的挑战：

- **Reasonix**：`.reasonix/skills/`，原生支持 SKILL.md
- **Claude Code**：`.claude/skills/`，原生支持 SKILL.md
- **Cursor**：`.cursor/skills/`，原生支持 SKILL.md
- **Cline / Roo Code**：`.cline/` 或 `.claude/skills/`
- **Windsurf**：`.windsurf/workflows/`，格式不同
- **Continue.dev**：`.continue/skills/`，格式不同
- **GitHub Copilot**：`.github/skills/`
- **通义灵码**：`.lingma/`，格式不同

**核心设计决策：只在"适配"层做格式转换，不在"核心"层做。** 核心技能文件始终维护为 SKILL.md 标准格式，`/install-skill` 只在复制时按目标工具做格式适配。这保证了核心维护成本最低，同时兼容性最高。

### 8.2 安装流程

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1000 520" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg6" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/>
    </linearGradient>
    <linearGradient id="a1" x1="0" y1="0" x2="1" y2="0">
      <stop offset="0%" stop-color="#6366f1"/><stop offset="100%" stop-color="#8b5cf6"/>
    </linearGradient>
    <linearGradient id="a2" x1="0" y1="0" x2="1" y2="0">
      <stop offset="0%" stop-color="#0ea5e9"/><stop offset="100%" stop-color="#06b6d4"/>
    </linearGradient>
    <linearGradient id="a3" x1="0" y1="0" x2="1" y2="0">
      <stop offset="0%" stop-color="#34d399"/><stop offset="100%" stop-color="#10b981"/>
    </linearGradient>
    <filter id="sh6" x="-20%" y="-20%" width="140%" height="140%">
      <feDropShadow dx="0" dy="5" stdDeviation="6" flood-color="#000" flood-opacity="0.4"/>
    </filter>
  </defs>

  <rect width="1000" height="520" fill="url(#bg6)"/>
  <text x="500" y="38" text-anchor="middle" fill="#f1f5f9" font-size="28" font-weight="700" letter-spacing="1">install-skill 安装流程 · 适配器模式</text>

  <rect x="300" y="60" width="400" height="46" rx="23" fill="url(#a1)" filter="url(#sh6)"/>
  <text x="500" y="88" text-anchor="middle" fill="#fff" font-size="14" font-weight="700">检测当前 AI 工具</text>
  <line x1="500" y1="106" x2="500" y2="132" stroke="#6366f1" stroke-width="2"/>
  <polygon points="500,138 493,127 507,127" fill="#6366f1"/>

  <rect x="150" y="142" width="700" height="56" rx="10" fill="#0c1a2b" stroke="#38bdf8" stroke-width="1.5" filter="url(#sh6)"/>
  <text x="500" y="168" text-anchor="middle" fill="#bae6fd" font-size="13" font-weight="700">识别技能安装目录 + 格式兼容性检查</text>
  <text x="500" y="188" text-anchor="middle" fill="#94a3b8" font-size="11">按优先级: Reasonix → Claude → Cursor → Cline → Windsurf → Copilot → 通义灵码</text>

  <line x1="500" y1="198" x2="500" y2="226" stroke="#0ea5e9" stroke-width="2"/>
  <polygon points="500,232 493,221 507,221" fill="#0ea5e9"/>

  <!-- 分支 -->
  <rect x="120" y="236" width="360" height="80" rx="12" fill="url(#a3)" filter="url(#sh6)"/>
  <text x="300" y="264" text-anchor="middle" fill="#fff" font-size="14" font-weight="700">✅ 原生支持 SKILL.md</text>
  <text x="300" y="286" text-anchor="middle" fill="#d1fae5" font-size="11">直接复制技能文件到</text>
  <text x="300" y="304" text-anchor="middle" fill="#a7f3d0" font-size="11">对应技能目录</text>

  <rect x="520" y="236" width="360" height="80" rx="12" fill="url(#a2)" filter="url(#sh6)"/>
  <text x="700" y="264" text-anchor="middle" fill="#fff" font-size="14" font-weight="700">🔄 格式不同</text>
  <text x="700" y="286" text-anchor="middle" fill="#e0f2fe" font-size="11">在复制时做格式适配转换</text>
  <text x="700" y="304" text-anchor="middle" fill="#bae6fd" font-size="11">SKILL.md → 工具原生格式</text>

  <line x1="300" y1="316" x2="300" y2="344" stroke="#10b981" stroke-width="2"/>
  <polygon points="300,350 293,339 307,339" fill="#10b981"/>
  <line x1="700" y1="316" x2="700" y2="344" stroke="#0ea5e9" stroke-width="2"/>
  <polygon points="700,350 693,339 707,339" fill="#0ea5e9"/>

  <rect x="200" y="354" width="600" height="46" rx="23" fill="url(#a3)" filter="url(#sh6)"/>
  <text x="500" y="382" text-anchor="middle" fill="#fff" font-size="14" font-weight="700">✅ 安装完成 → 斜杠命令立即可用</text>

  <rect x="200" y="420" width="600" height="60" rx="12" fill="#0f172a" stroke="#334155" stroke-width="1"/>
  <text x="500" y="446" text-anchor="middle" fill="#94a3b8" font-size="12" font-weight="700">安装约束</text>
  <text x="500" y="466" text-anchor="middle" fill="#64748b" font-size="11">不修改原 .harness/skills/ · 不装 install-skill 自身 · 只装到检测到的工具 · 已存在则覆盖并提示</text>
</svg>
```

### 8.3 支持的 AI 工具清单（按检测优先级）

| 优先级 | AI 工具 | 检测信号 | 技能目录 | 格式 |
|--------|---------|---------|---------|------|
| 1 | Reasonix | `.reasonix/` | `.reasonix/skills/` | SKILL.md |
| 2 | Claude Code | `.claude/` | `.claude/skills/` | SKILL.md |
| 3 | Cursor | `.cursor/` | `.cursor/skills/` | SKILL.md |
| 4 | Cline / Roo Code | `.cline/` | `.cline/` 或 `.claude/skills/` | SKILL.md |
| 5 | Codex | `.codex/` | `.codex/skills/` | SKILL.md |
| 6 | Qoder | `.qoder/` | `.qoder/skills/` | SKILL.md |
| 7 | VS Code Agent Skills | `.vscode/agent-skills/` | `.vscode/agent-skills/` | 标准化 |
| 8 | Windsurf | `.windsurf/` | `.windsurf/workflows/` | Workflow |
| 9 | Continue.dev | `.continue/` | `.continue/skills/` | 自定义 |
| 10 | GitHub Copilot | `.github/` | `.github/skills/` | 标准化 |
| 11 | OpenCode | `.opencode/` | `.opencode/skills/` | 自定义 |
| 12 | 通义灵码 | `.lingma/` | `.lingma/` | 自定义 |
| 13 | CodeGeeX | `.codegeex/` | `.codegeex/` | 自定义 |
| 14 | Tabnine | `.tabnine/` | `.tabnine/` | 自定义 |
| 15+ | 其他工具 | 兼容检测 | 按工具约定 | 适配 |

**安装逻辑三步**：
1. **检测工具版本与兼容性**：检查是否支持 SKILL.md → 不支持的尝试格式转换 → 转换失败则输出手动安装指引。
2. **复制技能文件**：遍历 `.harness/skills/` 逐个复制 → 保持目录结构 → 已存在则询问是否覆盖。
3. **验证安装结果**：检查技能文件完整性 → 尝试调用验证可用性 → 输出安装摘要。

---
---

## 九、结语

### 9.1 从"脚手架"到"工程纪律基础设施"

回到文章开头的问题：**如何让 AI 像一个训练有素的工程师那样去工作？**

我们给出的答案不是更好的提示词，而是**工程纪律基础设施**——一套把"纪律"变成"可执行基础设施"的体系。

提示词是易变的、不可复用的、依赖具体对话上下文的。而基础设施是结构化的、可复用的、不依赖具体工具的。这个脚手架把 20 年软件开发沉淀的工程纪律，变成了 6 条规则、10 个模板化流水线技能、16 个跨语言通用技能、4 个框架专属技能、51 参数键、111 框架差异检测表、19+ AI 工具适配器，覆盖了从需求分析到部署验证的完整闭环。

### 9.2 回顾：贯穿全文的不变式

如果只让你记住这篇文章的一个东西，请记住**"不变式"（Invariant）**这个概念。我们为每一个核心机制都定义了它成立的前提：

| 机制 | 不变式 |
|------|--------|
| **四层架构** | 分层按"决策可信度"而非业务逻辑 |
| **参数化机制** | 任何框架差异必须收敛在参数表中 |
| **识别机制** | 精确优先、多语言不擅选、低置信度必询问 |
| **流水线** | 状态只能前移或回退到紧邻阶段，禁止跨阶段跳转 |
| **约束体系** | 规则可被语言包覆盖，但覆盖必须显式声明 |
| **变更选择** | "选哪个 change"是价值判断，裁决权在人类——多候选必请示、审批门禁强制 |
| **CI 门禁** | 门禁是硬约束，不是软建议 |
| **部署验证** | "CI 绿" ≠ "线上可用" |

**不变式是工程纪律的"力学定律"**——它们定义了"什么情况下系统是正确的"。没有不变式的流程只是"建议"，有不变式的流程才是"纪律"。

### 9.3 项目数据一览

| 维度 | 数据 |
|------|------|
| 支持语言 | 6（Java / Python / Go / Rust / PHP / Frontend） |
| 框架差异块 | 111 |
| 支持构建工具 | 10+ |
| 支持 AI 工具 | 19+ |
| 可执行技能 | 30 个（10 模板 + 16 通用 + 4 专属） |
| 参数键 | 51 |
| 规则文件 | 6 条（4 通用 + 2 语言特有） |
| 检测表 | 6 种 × 11-35 条规则 |
| 参数块 | 117 个（6 基础 + 111 差异） |
| 架构层级 | 4 层 |
| 核心文件 | apply-harness/SKILL.md（2300+ 行） |

### 9.4 未来演进

这个脚手架不是一个"做完就完"的项目，它在持续演进：

- **检测表持续扩展**：随着新框架和构建工具出现持续更新。Go 已支持 21 框架差异块，Java 16，Python 16，Frontend 9，Rust 19，PHP 30。
- **AI 工具兼容性**：随着新 AI 编程工具出现，安装适配器持续添加。
- **参数化深度**：从 51 参数键扩展到更多维度（部署平台、云服务商、日志系统等）。
- **领域建模**：`/domain-modeling` 技能持续增强，支持更丰富的 ADR 格式。

### 9.5 写给 AI 时代的工程师

如果你正在使用 AI 编程工具，却觉得"AI 写的代码质量不稳定"——不要责怪 AI，而是问自己：

> **我有没有给 AI 设定清晰的约束？有没有给它一个可执行的流程？有没有一个自动化的门禁来验证它的产出？**

如果答案是否定的，那么试试这个方向。它不是一个银弹，但它是让"AI 写代码"这件事变得**可预测、可控制、可重复**的工程化路径。

**真正的生产力提升，不是来自 AI 写得有多快，而是来自团队不花时间在修 Bug 上。**

> 人类设计约束，AI 编写代码，机器验证质量。
> 这就是 Harness Engineering。

---

*本文档由 Harness 团队撰写，作为跨语言 Harness 脚手架的设计架构记录（ADR）。*

*欢迎 star / fork / PR 共建。*

---
