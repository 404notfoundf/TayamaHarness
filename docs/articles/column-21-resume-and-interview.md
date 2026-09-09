# 如何把 Harness 脚手架写进简历 + 50 道经典面试题

> 星球专栏 · 简历突围 · 面试通关
>
> 很多人花三个月把项目做出来，却只用三行字把它写进简历，然后在面试里被问得哑口无言。
> 这篇文章不讲空话，直接给你：**① 这套脚手架项目怎么包装进简历；② 面试官最可能追问的 50 道题及答题要点。** 图文并茂，配合 10+ 张流程图，一次讲透。

---

## 目录

- 第一部分：这个项目为什么值得写进简历
- 第二部分：简历包装的三层写法
- 第三部分：面试官会深挖的三个故事（迁移 / 架构体检 / 软闸门）
- 第四部分：50 道本项目专属面试题（8 大方向分类）
- 第五部分：面试答题框架与行动清单

---

## 第一部分：这个项目为什么值得写进简历

### 1.1 项目定位：它到底值钱在哪

很多同学手里明明有一个"含金量不低"的项目，却不知道它值钱在哪。写简历的第一步，不是"写"，而是**给项目定位**。

tayama-harness-skills 这套脚手架，本质上不是"一个业务系统"，而是一套**AI 时代软件开发方法论的基础设施**。它解决的核心问题是：

> **当 AI 开始大规模写代码，如何保证代码质量、可追溯、可验证、可控？**

这套脚手架的答案是：**人类设计约束、AI 写代码、机器验证**。它把"需求分析 → 编码 → 测试 → 评审 → CI → 部署"组织成一条 6 阶段流水线，并用 30+ 个技能（Skill）把这条流水线落到任意项目里。

```svg
<svg xmlns="http://www.w3.org/2000/svg" width="980" height="360" viewBox="0 0 980 360">
    <defs>
        <linearGradient id="bgA" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
        </linearGradient>
        <marker id="arA" markerWidth="12" markerHeight="12" refX="10" refY="6" orient="auto">
            <path d="M 0 0 L 12 6 L 0 12 z" fill="#475569"/>
        </marker>
    </defs>
    <rect width="980" height="360" fill="url(#bgA)"/>
    <text x="490" y="32" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">普通项目 vs 工程基础设施：简历含金量对比</text>

    <!-- 左：普通业务项目 -->
    <rect x="40" y="55" width="420" height="240" rx="12" fill="#64748b" opacity="0.08" stroke="#64748b" stroke-width="1.5"/>
    <text x="250" y="84" text-anchor="middle" fill="#94a3b8" font-size="15" font-weight="700">✗ 普通业务项目</text>
    <line x1="70" y1="96" x2="430" y2="96" stroke="#64748b" stroke-width="0.6" opacity="0.3"/>
    <text x="70" y="124" fill="#94a3b8" font-size="12">技术栈：Vue3 + Spring Boot + MySQL</text>
    <text x="70" y="148" fill="#94a3b8" font-size="12">职责：写增删改查、调第三方接口</text>
    <text x="70" y="172" fill="#94a3b8" font-size="12">产出：一个"能用"的系统</text>
    <text x="70" y="196" fill="#94a3b8" font-size="12">面试官视角：谁都会写，无差异</text>
    <text x="70" y="220" fill="#94a3b8" font-size="12">简历写法："负责 xx 模块开发"</text>
    <text x="70" y="258" fill="#f87171" font-size="12">⚡ 价值：可替代 · 无壁垒</text>

    <line x1="470" y1="175" x2="520" y2="175" stroke="#475569" stroke-width="3" marker-end="url(#arA)"/>

    <!-- 右：工程基础设施 -->
    <rect x="520" y="55" width="420" height="240" rx="12" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="2"/>
    <text x="730" y="84" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">✓ 工程基础设施</text>
    <line x1="550" y1="96" x2="910" y2="96" stroke="#38bdf8" stroke-width="0.6" opacity="0.3"/>
    <text x="550" y="124" fill="#94a3b8" font-size="12">技术栈：方法论 + 多语言技能包 + AI Agent</text>
    <text x="550" y="148" fill="#94a3b8" font-size="12">职责：约束 AI 产出，定义"怎么做"的规则</text>
    <text x="550" y="172" fill="#94a3b8" font-size="12">产出：6 阶段流水线 + 39 技能包 + 文档体系</text>
    <text x="550" y="196" fill="#94a3b8" font-size="12">面试官视角：有架构思维、有方法论</text>
    <text x="550" y="220" fill="#94a3b8" font-size="12">简历写法："设计 AI 协作开发流水线"</text>
    <text x="550" y="258" fill="#4ade80" font-size="12">⚡ 价值：稀缺 · 有壁垒 · 有故事</text>

    <text x="490" y="325" text-anchor="middle" fill="#475569" font-size="16">面试官筛的不是"你会不会写 CRUD"，而是"你有没有工程思维和架构能力"</text>
    <text x="490" y="346" text-anchor="middle" fill="#475569" font-size="11">→ 同样的代码量，定位不同，简历含金量天差地别</text>
</svg>
```

### 1.2 先搞清楚：这个项目到底是什么（低水平认知 vs 高水平认知）

写简历前，先问自己一个致命问题：**你眼里这个项目是什么，面试官眼里它又是什么？** 同一个项目，两种认知，写出来的简历是两个世界的东西。

**低水平认知（执行者视角）**

> 这是一个脚手架项目，用 Markdown 和 Python 脚本写了一些规则，支持多种语言，让 AI 写代码的时候按规范来，还配了一些 CI 检查。

**高水平认知（架构师视角）**

> 这是一套「AI 时代软件开发方法论的基础设施」：把"需求分析 → 编码 → 测试 → 评审 → CI → 部署"组织成一条由**变更状态机**驱动的 6 阶段流水线，用**双轴评审**（Spec 需求匹配 + Standards 规范合规）和**六段机器门禁**把 AI 产出钉进可验证轨道；用**参数化分层覆盖**（语言基础参数块 + 框架差异块，52 个占位符）一处改动全语言生效，覆盖 6 语言 120+ 框架；沉淀 39 个技能包 + 2W+ 行文档，并通过**软闸门**让存量债"只减不增"地渐进收敛。

**区别**：低水平在说"你做了什么"；高水平在说"你怎么设计了一套系统"。前者是执行者视角，后者是资深工程师 / 架构师视角——对于 3 年以上岗位，面试官默认你必须是后者。

### 1.3 四层台阶：面试官站在哪一层看你

同样的项目，面试官的评价是分层的。你站在「功能层面」写，面试官站在「有没有架构感」看，中间隔着四层台阶：

| 层级 | 面试官在想什么 | 本项目的落点 |
|---|---|---|
| 功能层面（做了什么） | 你会不会写代码 | 写了技能规则、检测/渲染脚本、CI 配置 |
| 架构层面（怎么设计的） | 你会不会设计系统 | 状态机驱动流水线、参数化分层覆盖、两级检测 + 询问兜底、双轴评审 |
| 工程层面（怎么保证质量） | 你会不会做工程治理 | 六段门禁（编译→静态→竞态→架构约束→测试覆盖率→安全）、覆盖率 ≥80%、变更留档 |
| 生产层面（怎么上线运行） | 你能不能扛生产 | 39 技能包一键安装、AI 可关降级、软闸门存量迁移、文档可追溯 |

> 💡 简历面试最稀缺的，是能在后三层自圆其说的人。每写一个技术点，先问它落在哪一层——如果只能落在"功能层"，面试官 5 秒就划走了。

### 1.4 一句话对比：同样的工作量，为什么含金量不同

**一句话总结为什么值钱**：这是一个"能讲出方法论、能画出架构图、能经得起追问"的项目——而简历面试最稀缺的恰恰是这种项目。

### 1.5 项目履历的"完整样本"（含难点与亮点，可直接改写到简历）

简历的难点在于：**既要写"我做了什么"，更要写"这件事难在哪、我是怎么破的"**。下面三段样本都按「定位句 + 亮点机制 + 难点与解法 + 量化结果」四段式展开——面试官读到的是"一个有工程判断力的人"，而不是"一个会用工具的人"。

**样本 A：后端 / 架构方向（放在项目经历第一条）**

> **AI 协作开发脚手架 · 独立设计并落地（2024.06 – 至今）**
>
> 设计并落地"约束 AI 写代码"的工程方法基础设施：以 6 阶段流水线 + 机器门禁，将 AI 产出纳入可验证轨道。
> - **核心机制**：6 阶段流水线（需求→编码→测试→评审→CI→部署）由变更状态机驱动，人工可随时介入；双轴评审（Spec 需求匹配 + Standards 规范合规）0 严重问题才放行。
> - **难点① · 参数化**：5 种语言规范差异巨大，手写一套会爆炸。用"参数化分层覆盖"（语言基础参数块 + 框架差异块）+ 52 个占位符，实现"改 1 处、全语言生效"，渲染引擎保证确定性、幂等、安装秒级。
> - **难点② · 自动检测**：两级特征检测——先识别语言、再识别框架与构建工具，覆盖 120+ 框架；未匹配/多语言冲突一律询问用户，绝不静默猜（识别错误会沿链路放大，所以确定性优先）。
> - **结果**：39 个技能包落地、2W+ 行文档、npm 一键安装；技能按模板化/通用/组件/语言包四层治理，避免重复维护。

**样本 B：AI 工程方向（突出 LLM 应用 + Agent 编排）**

> **AI 协作开发脚手架 · 方法论 + LLM 应用设计（2024.06 – 至今）**
>
> 针对"AI 生成代码质量不可控"的行业痛点，落地"人类设计约束、AI 写代码、机器验证"方法论。
> - **LLM 应用**：PRD 解析用三层混合策略（模板匹配→LLM 解析→章节层级算法兜底），**AI 可关**——LLM 不可用时自动降级，不阻塞流水线；解析结果落库前人人可校验。
> - **Agent 编排**：Owner Agent 全权负责 Change（需求→编码→测试→评审→验证），沿 9 状态状态机推进、前置检查防跳步，关键节点停下请用户决策（AI 执行、人裁决）；SDD-TDD 强制"先写失败测试"，用测试约束 AI 生成方向。
> - **质量评测**：AI 产出经双轴评审 + CI 五段门禁（编译→lint→架构约束→竞态检测→覆盖率≥80%）双层校验，评估"AI 写得好不好"有硬指标。
> - **结果**：把"AI 写代码"从不可控变成"轨道内可验证"，覆盖 6 语言 120+ 框架。

**样本 C：全栈 / 前端方向（突出工程化 + 文档体系）**

> **PRD 解析平台 + AI 协作脚手架 · 前端全栈（2024.06 – 至今）**
>
> 开发 PRD 解析平台前端（Vue3 + TypeScript + Pinia），支持看板、校验面板、文件分片上传；同步建设 AI 协作开发脚手架。
> - **前端**：解析结果可视化、任务状态看板、交互式校验面板；长文件上传用分片 + 进度反馈，解决大文档解析卡顿。
> - **工程化**：组件化 + 状态管理 + 自动化测试；项目文档体系 2W+ 行、23 篇技术专栏，做到"代码之外的知识也可维护"。
> - **协作**：把方法论拆成 40+ 内联技能供 AI 与开发者共用，人机协同边界清楚——AI 写代码、机器验质量、人做决策。

> 💡 **为什么这样写有杀伤力**：每一段都埋了"面试钩子"——"参数化分层覆盖""不确定就询问""AI 可关""软闸门"。这些词面试官一听就知道你有真实工程经验，会顺着往下追；而你恰好都有对应的"难点-解法"故事可讲。

---

## 第二部分：简历包装的三层写法

### 2.1 第一层：一句话定位（35 字内讲清楚"你造了什么"）

简历项目第一条，应该是一句**定位句**，而不是技术栈清单。公式：

```
[目标用户] × [核心机制] × [可验证结果]
```

本项目的定位句可以这样写：

> **"设计并落地一套 AI 协作开发流水线：以 6 阶段门禁约束 AI 产出，覆盖 5 种语言、80+ 框架，30+ 可复用技能。"**

```svg
<svg xmlns="http://www.w3.org/2000/svg" width="980" height="340" viewBox="0 0 980 340">
  <defs>
    <linearGradient id="bgB" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arB" markerWidth="12" markerHeight="12" refX="10" refY="6" orient="auto">
      <path d="M 0 0 L 12 6 L 0 12 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="980" height="340" fill="url(#bgB)"/>
  <text x="490" y="30" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">一句话定位公式：目标 × 机制 × 结果</text>

  <!-- 三块 -->
  <rect x="40" y="55" width="270" height="150" rx="12" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="2"/>
  <text x="175" y="84" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">① 目标用户</text>
  <line x1="65" y1="96" x2="285" y2="96" stroke="#38bdf8" stroke-width="0.6" opacity="0.3"/>
  <text x="65" y="126" fill="#94a3b8" font-size="12">• 用 AI 写代码的开发者/团队</text>
  <text x="65" y="150" fill="#94a3b8" font-size="12">• 需要质量门禁的项目</text>
  <text x="65" y="174" fill="#94a3b8" font-size="12">• 想统一工程规范的组织</text>

  <rect x="355" y="55" width="270" height="150" rx="12" fill="#8b5cf6" opacity="0.12" stroke="#a78bfa" stroke-width="2"/>
  <text x="490" y="84" text-anchor="middle" fill="#c4b5fd" font-size="15" font-weight="700">② 核心机制</text>
  <line x1="380" y1="96" x2="600" y2="96" stroke="#a78bfa" stroke-width="0.6" opacity="0.3"/>
  <text x="380" y="126" fill="#94a3b8" font-size="12">• 6 阶段流水线（需求→部署）</text>
  <text x="380" y="150" fill="#94a3b8" font-size="12">• SDD-TDD + 变更状态机</text>
  <text x="380" y="174" fill="#94a3b8" font-size="12">• 门禁自动化（机器验证）</text>

  <rect x="670" y="55" width="270" height="150" rx="12" fill="#10b981" opacity="0.12" stroke="#34d399" stroke-width="2"/>
  <text x="805" y="84" text-anchor="middle" fill="#6ee7b7" font-size="15" font-weight="700">③ 可验证结果</text>
  <line x1="695" y1="96" x2="915" y2="96" stroke="#34d399" stroke-width="0.6" opacity="0.3"/>
  <text x="695" y="126" fill="#94a3b8" font-size="12">• 5 种语言 · 80+ 框架</text>
  <text x="695" y="150" fill="#94a3b8" font-size="12">• 39 个技能包</text>
  <text x="695" y="174" fill="#94a3b8" font-size="12">• 2W+ 行文档 · 23 篇专栏</text>

  <!-- 箭头 -->
  <line x1="315" y1="130" x2="350" y2="130" stroke="#475569" stroke-width="2" marker-end="url(#arB)"/>
  <line x1="630" y1="130" x2="665" y2="130" stroke="#475569" stroke-width="2" marker-end="url(#arB)"/>

  <!-- 底部成品 -->
  <rect x="140" y="230" width="700" height="55" rx="12" fill="#1e293b" stroke="#475569" stroke-width="1.5" stroke-dasharray="6,3"/>
  <text x="490" y="254" text-anchor="middle" fill="#e2e8f0" font-size="14" font-weight="700">定位句：设计并落地 AI 协作开发流水线，以 6 阶段门禁约束 AI 产出</text>
  <text x="490" y="274" text-anchor="middle" fill="#64748b" font-size="11">覆盖 6 语言 120+ 框架 · 30+ 可复用技能 · 全程变更留档可追溯</text>

  <text x="490" y="322" text-anchor="middle" fill="#475569" font-size="16">定位句决定简历第一行给面试官的"印象锚点"——务必出现"设计""落地""约束"这类动词</text>
</svg>
```

### 2.2 第二层：用 STAR 法则写每一条项目经历

简历里每一条"做了什么"，都要能回答面试官的一个潜在问题：**"这件事难在哪？你解决了什么？"**

STAR 法则是最稳妥的写法模板：

| 要素 | 问题 | 本项目示例 |
|---|---|---|
| **S** Situation | 当时面临什么背景/痛点？ | AI 写代码时代，代码质量不可控、不可追溯 |
| **T** Task | 你的任务是什么？ | 设计一套约束 AI 的方法论并落地为技能包 |
| **A** Action | 你具体做了什么？ | 拆解 6 阶段流水线，定义门禁，实现自动检测/渲染引擎 |
| **R** Result | 结果如何？可量化吗？ | 39 个技能包落地，覆盖 6 语言 120+ 框架，文档 2W+ 行 |

```svg
<svg xmlns="http://www.w3.org/2000/svg" width="980" height="420" viewBox="0 0 980 420">
  <defs>
    <linearGradient id="bgC" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arC" markerWidth="12" markerHeight="12" refX="10" refY="6" orient="auto">
      <path d="M 0 0 L 12 6 L 0 12 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="980" height="420" fill="url(#bgC)"/>
  <text x="490" y="30" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">STAR 法则：把"做了什么"升级为"解决了什么"</text>

  <!-- S -->
  <rect x="40" y="55" width="210" height="170" rx="12" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="2"/>
  <text x="145" y="84" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">S · Situation</text>
  <text x="145" y="104" text-anchor="middle" fill="#64748b" font-size="10">背景 / 痛点</text>
  <line x1="65" y1="116" x2="225" y2="116" stroke="#38bdf8" stroke-width="0.6" opacity="0.3"/>
  <text x="65" y="142" fill="#94a3b8" font-size="11">AI 写代码后质量</text>
  <text x="65" y="160" fill="#94a3b8" font-size="11">不可控、不可追溯</text>
  <text x="65" y="178" fill="#94a3b8" font-size="11">团队规范难统一</text>
  <text x="65" y="208" fill="#fde68a" font-size="11">★ 写清"为什么做"</text>

  <line x1="255" y1="140" x2="290" y2="140" stroke="#475569" stroke-width="2" marker-end="url(#arC)"/>

  <!-- T -->
  <rect x="295" y="55" width="210" height="170" rx="12" fill="#8b5cf6" opacity="0.12" stroke="#a78bfa" stroke-width="2"/>
  <text x="400" y="84" text-anchor="middle" fill="#c4b5fd" font-size="15" font-weight="700">T · Task</text>
  <text x="400" y="104" text-anchor="middle" fill="#64748b" font-size="10">你的任务</text>
  <line x1="320" y1="116" x2="480" y2="116" stroke="#a78bfa" stroke-width="0.6" opacity="0.3"/>
  <text x="320" y="142" fill="#94a3b8" font-size="11">设计约束 AI 的</text>
  <text x="320" y="160" fill="#94a3b8" font-size="11">方法论 + 落地工具</text>
  <text x="320" y="178" fill="#94a3b8" font-size="11">跨语言可复用</text>
  <text x="320" y="208" fill="#fde68a" font-size="11">★ 你的职责边界</text>

  <line x1="510" y1="140" x2="545" y2="140" stroke="#475569" stroke-width="2" marker-end="url(#arC)"/>

  <!-- A -->
  <rect x="550" y="55" width="210" height="170" rx="12" fill="#f59e0b" opacity="0.12" stroke="#fbbf24" stroke-width="2"/>
  <text x="655" y="84" text-anchor="middle" fill="#fcd34d" font-size="15" font-weight="700">A · Action</text>
  <text x="655" y="104" text-anchor="middle" fill="#64748b" font-size="10">你做了什么</text>
  <line x1="575" y1="116" x2="735" y2="116" stroke="#fbbf24" stroke-width="0.6" opacity="0.3"/>
  <text x="575" y="142" fill="#94a3b8" font-size="11">拆 6 阶段流水线</text>
  <text x="575" y="160" fill="#94a3b8" font-size="11">定义 5 条规则 + 门禁</text>
  <text x="575" y="178" fill="#94a3b8" font-size="11">实现检测/渲染引擎</text>
  <text x="575" y="208" fill="#fde68a" font-size="11">★ 具体动作动词</text>

  <line x1="765" y1="140" x2="800" y2="140" stroke="#475569" stroke-width="2" marker-end="url(#arC)"/>

  <!-- R -->
  <rect x="805" y="55" width="135" height="170" rx="12" fill="#10b981" opacity="0.12" stroke="#34d399" stroke-width="2"/>
  <text x="872" y="84" text-anchor="middle" fill="#6ee7b7" font-size="15" font-weight="700">R</text>
  <text x="872" y="104" text-anchor="middle" fill="#64748b" font-size="10">Result</text>
  <line x1="820" y1="116" x2="925" y2="116" stroke="#34d399" stroke-width="0.6" opacity="0.3"/>
  <text x="822" y="142" fill="#94a3b8" font-size="10">39 技能包</text>
  <text x="822" y="160" fill="#94a3b8" font-size="10">6 语言</text>
  <text x="822" y="178" fill="#94a3b8" font-size="10">120+ 框架</text>
  <text x="822" y="208" fill="#fde68a" font-size="11">★ 可量化</text>

  <!-- 底部：好坏对比 -->
  <text x="490" y="260" text-anchor="middle" fill="#e2e8f0" font-size="15" font-weight="700">简历写法对比</text>
  <rect x="60" y="275" width="420" height="60" rx="10" fill="#ef4444" opacity="0.08" stroke="#ef4444" stroke-width="1.5"/>
  <text x="80" y="298" fill="#f87171" font-size="12" font-weight="700">✗ 差</text>
  <text x="110" y="298" fill="#94a3b8" font-size="12">"开发了一个脚手架，用了 Markdown 和脚本，支持多语言"</text>
  <text x="80" y="322" fill="#64748b" font-size="11">→ 无痛点、无职责、无结果，面试官无从追问</text>
  <rect x="500" y="275" width="420" height="60" rx="10" fill="#10b981" opacity="0.10" stroke="#34d399" stroke-width="1.5"/>
  <text x="520" y="298" fill="#4ade80" font-size="12" font-weight="700">✓ 好</text>
  <text x="550" y="298" fill="#94a3b8" font-size="12">"针对 AI 写代码质量失控，设计 6 阶段流水线约束 AI 产出，落地 39 个技能包覆盖 6 语言"</text>
  <text x="520" y="322" fill="#64748b" font-size="11">→ 痛点清晰、动作具体、结果可量化，追问空间大</text>

  <text x="490" y="365" text-anchor="middle" fill="#475569" font-size="16">面试官其实希望你能展开讲——"差"写法没给他展开的抓手，"好"写法处处是抓手</text>
  <text x="490" y="386" text-anchor="middle" fill="#475569" font-size="16">但注意：简历要"留白"，把 30% 的关键细节留给面试时讲，别一次写完</text>
  <text x="490" y="407" text-anchor="middle" fill="#475569" font-size="16">面试引导话术："这个项目我做了 X，其中最复杂的是 Y，当时我用 Z 方案解决了 W"</text>
</svg>
```

### 2.3 1-5 年 vs 5-10 年：两套完整简历模板（直接抄）

同一套脚手架，**1-5 年写"我负责了什么"，5-10 年写"我设计并主导了什么"**。两套模板字段都不一样——5-10 年版多了「架构设计」和「工程治理」两个板块，这正是面试官想确认的能力边界。以下模板已按模板《如何写简历》的字段结构整理，可直接改项目名套用。

#### 2.3.1 1-5 年经验版（证明你能独立负责一个模块）

```
项目名称
AI 协作开发脚手架（Harness Engineering）

项目简介
面向 AI 写代码场景的工程化脚手架：把 6 阶段开发流水线（需求→编码→测试→评审→CI→部署）落地为可复用技能包，约束 AI 产出质量。用 Markdown 定义规则、Python 实现检测与渲染引擎。

技术栈
Markdown / Python / Shell / 6 语言技能包（Java / Go / Python / TS 等）/ CI 流水线 / 单元测试

核心职责
· 独立开发语言自动检测模块：两级特征检测（先识别语言、再识别框架/构建工具），覆盖 120+ 框架；未匹配/多语言时询问用户兜底，绝不静默猜。
· 参与参数化渲染引擎开发：设计"语言基础参数块 + 框架差异块"分层覆盖，52 个占位符一处替换，改 1 处全语言生效。
· 实现双轴评审脚本（Spec 需求匹配 + Standards 规范合规），任一严重问题即退回，产出评审记录留档。
· 编写单元测试，核心逻辑覆盖率 ≥80%，参与 CI 六段门禁配置与维护。
· 沉淀 2W+ 行文档，覆盖 6 语言 120+ 框架的安装与使用。

项目成果
· 39 个技能包从 0 到 1 落地，npm 一键安装，安装秒级完成
· 覆盖 5 种语言、80+ 框架，棕地项目迁移三阶段平稳落地
· 全模块测试通过，CI 六段门禁稳定运行
```

#### 2.3.2 5-10 年经验版（证明你在"设计系统"和"治理工程"）

```
项目名称
企业级 AI 协作开发基础设施（AI Engineering Scaffold）

项目定位
面向"AI 大规模写代码"时代的企业级工程方法基础设施：以变更状态机 + 六段机器门禁 + 双轴评审，将 AI 产出纳入可验证、可追溯、可回滚的轨道；参数化分层覆盖 6 语言 120+ 框架，软闸门支撑存量系统渐进迁移。

技术栈
Markdown / Python / Shell / 6 语言技能包 / GitHub Actions / ArchUnit（架构约束）/ JaCoCo（覆盖率门禁）/ 渲染与检测引擎

架构设计
· 状态机驱动的变更流水线：draft → analyzing → coding → testing → reviewing → ci → verifying → done 九状态推进，前置检查防跳步，关键节点停下请用户裁决（AI 执行、人决策）。
· 参数化分层覆盖模型：语言基础参数块（不变）+ 框架差异块（变）+ 52 个占位符，把"6 语言 × 120+ 框架"的维护收敛为"改 1 处"。
· 两级特征检测引擎：先识别语言、再识别框架/构建工具；未匹配/多语言一律询问用户，识别不确定就停，防止错误沿链路放大。
· 双轴评审机制：Spec 轴（需求匹配）+ Standards 轴（规范合规）并行独立评审，0 严重问题才放行，review.md 留档可追溯。

核心职责
· 设计并主导 6 阶段流水线 + 变更状态机，定义"AI 写代码、机器验证、人裁决"的人机协同边界。
· 设计参数化渲染引擎，保证确定性渲染、幂等、安装秒级；LLM 仅用于语义理解（PRD 三层解析：模板→LLM→算法兜底），AI 可关、不阻塞主链路。
· 设计软闸门存量迁移策略：baseline 存量"只减不增" + 新增 0 violation + 安全类例外立即修，三阶段（摸底→套壳→收敛）落地。

工程治理
· 六段 CI 门禁：编译 → 静态分析 → 竞态检测 → 架构约束 → 单元测试+覆盖率 → 安全扫描，全绿方可合入。
· 技能包四层治理（模板/通用/组件/语言包），39 个技能包避免重复维护；变更全程留档（C-NNN：change.md / review.md / verify.md）支持断点续传与复盘。

项目成果
· 构建 39 技能包 + 2W+ 行文档的完整工程体系，覆盖 6 语言 120+ 框架
· 六段门禁 CI 自动化守护，核心逻辑覆盖率 ≥80%，架构约束 0 违规
· 棕地项目软闸门迁移平稳落地，存量债"只减不增"消化
```

#### 2.3.3 同一模块的两种写法（1-5 vs 5-10 对比）

| 模块 | 1-5 年写法（做了什么） | 5-10 年写法（设计了什么） |
|---|---|---|
| 参数化引擎 | 实现模板渲染脚本，用占位符替换生成多语言代码 | 设计"基础块+差异块"分层覆盖模型，把 6 语言×120+ 框架的维护收敛为改 1 处，确定性/幂等/秒级安装 |
| 自动检测 | 写了个脚本识别项目语言/框架 | 设计两级特征检测引擎（先语言后框架），未匹配/多语言询问兜底，识别不确定就停防止错误放大 |
| 软闸门 | 给 CI 加了个检查规则 | 设计 baseline 存量"只减不增"+ 新增 0 violation 的迁移策略，三阶段渐进收敛 |
| 双轴评审 | 人工 review AI 产出 | 设计 Spec/Standards 双轴独立评审机制，0 严重问题放行，review.md 留档 |

**面试官最容易追的三个方向**：
- "检测识别错了会怎样？" → 答案：两级检测 + 询问兜底，识别不确定就停；识别错误会沿链路放大，所以确定性优先
- "软闸门会不会让存量债永远还不完？" → 答案：只减不增 + 新增 0 violation，欠债不再增加，存量在正常开发中顺带消化
- "AI 可关是怎么做到的？" → 答案：LLM 只做语义理解，核心链路用模板/算法兜底，LLM 挂了流水线不断

### 2.4 亮点 / 难点怎么写：四段式"挑战模板"

简历上的每个亮点，本质上都是**"一个机制 + 一个难点 + 一个解法 + 一个数字"**。普通简历写机制，高手简历写"机制背后我踩过的坑"。下面四段式模板，每一段都让面试官想追问：

```
[机制/动作] + [难点/挑战] + [解法/关键决策] + [量化结果]
```

**结合本项目，现成的 4 个"高分亮点"（直接抄进简历）：**

1. **参数化分层覆盖**——难点：5 种语言规范差异大，手写模板会爆炸；解法：语言基础参数块 → 框架差异块 + 52 个占位符，改 1 处全语言生效；结果：渲染确定性 / 幂等 / 安装秒级。
2. **自动检测引擎**——难点：120+ 框架怎么识别才能不误判；解法：两级特征检测（先识别语言、再识别框架/构建工具）、未匹配/多语言不静默猜（询问用户兜底）；结果：识别不确定就询问，绝不静默猜。
3. **软闸门**——难点：存量代码违规上千条，一刀切硬门禁会堵死业务；解法：baseline 存量"只减不减" + 新增 0 violation + 安全类例外立即修；结果：三阶段迁移平稳落地。
4. **双轴评审**——难点：怎么客观评估"AI 写得对不对"；解法：Spec 需求匹配 + Standards 规范合规两轴并行，0 严重问题才放行；结果：质量不依赖个人自觉。

**挖深一层的"难点台词"（被追问时用）：**
- "这个方案我一开始也想简单了……"（讲第一版为什么不行）
- "真正的难点不是实现，而是取舍……"（讲权衡过程）
- "我们当时在 A 和 B 之间纠结，最后选了 A，因为……"（讲决策依据）

> 💡 **为什么这是"深度"**：面试官听过的 90% 简历都是"我用了什么技术"；而你的四段式讲的是"我为什么这么设计、踩过什么坑、怎么权衡"——**这才是工程师和工具人的分水岭**。

### 2.5 第三层：按岗位定制"技术亮点矩阵"

同一套项目，投不同岗位，**主角和配角要换**。下面这张表直接抄：

```svg
<svg xmlns="http://www.w3.org/2000/svg" width="980" height="460" viewBox="0 0 980 460">
  <defs>
    <linearGradient id="bgD" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
  </defs>
  <rect width="980" height="460" fill="url(#bgD)"/>
  <text x="490" y="30" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">岗位 × 亮点矩阵：同一项目，三套讲法</text>

  <!-- 表头 -->
  <rect x="40" y="50" width="180" height="40" rx="8" fill="#1e293b" stroke="#475569" stroke-width="1.2"/>
  <text x="130" y="75" text-anchor="middle" fill="#e2e8f0" font-size="13" font-weight="700">投递岗位</text>
  <rect x="230" y="50" width="350" height="40" rx="8" fill="#1e293b" stroke="#475569" stroke-width="1.2"/>
  <text x="405" y="75" text-anchor="middle" fill="#e2e8f0" font-size="13" font-weight="700">主推亮点（放大讲）</text>
  <rect x="590" y="50" width="350" height="40" rx="8" fill="#1e293b" stroke="#475569" stroke-width="1.2"/>
  <text x="765" y="75" text-anchor="middle" fill="#e2e8f0" font-size="13" font-weight="700">隐含亮点（被问再讲）</text>

  <!-- 后端岗 -->
  <rect x="40" y="100" width="180" height="90" rx="8" fill="#0ea5e9" opacity="0.10" stroke="#38bdf8" stroke-width="1.2"/>
  <text x="130" y="128" text-anchor="middle" fill="#7dd3fc" font-size="13" font-weight="700">后端 / 架构岗</text>
  <text x="130" y="150" text-anchor="middle" fill="#64748b" font-size="10">Java / Go / Python</text>
  <rect x="230" y="100" width="350" height="90" rx="8" fill="#0ea5e9" opacity="0.06" stroke="#38bdf8" stroke-width="1"/>
  <text x="250" y="124" fill="#94a3b8" font-size="11">• 6 阶段流水线的架构设计</text>
  <text x="250" y="144" fill="#94a3b8" font-size="11">• 变更状态机（可跳步校验）</text>
  <text x="250" y="164" fill="#94a3b8" font-size="11">• 门禁自动化 + 构建工具链</text>
  <rect x="590" y="100" width="350" height="90" rx="8" fill="#0ea5e9" opacity="0.04"/>
  <text x="610" y="124" fill="#64748b" font-size="11">• SDD-TDD 方法论</text>
  <text x="610" y="144" fill="#64748b" font-size="11">• 并发/运行时可靠性规则</text>
  <text x="610" y="164" fill="#64748b" font-size="11">• 插件/模板机制</text>

  <!-- AI 工程岗 -->
  <rect x="40" y="200" width="180" height="90" rx="8" fill="#8b5cf6" opacity="0.10" stroke="#a78bfa" stroke-width="1.2"/>
  <text x="130" y="228" text-anchor="middle" fill="#c4b5fd" font-size="13" font-weight="700">AI 工程岗</text>
  <text x="130" y="250" text-anchor="middle" fill="#64748b" font-size="10">Agent / RAG / LLM</text>
  <rect x="230" y="200" width="350" height="90" rx="8" fill="#8b5cf6" opacity="0.06" stroke="#a78bfa" stroke-width="1"/>
  <text x="250" y="224" fill="#94a3b8" font-size="11">• AI 写代码 + 人机协同协议</text>
  <text x="250" y="244" fill="#94a3b8" font-size="11">• LLM 解析 PRD（模板/LLM/章节）</text>
  <text x="250" y="264" fill="#94a3b8" font-size="11">• AI 评测/双轴评审（Spec+规范）</text>
  <rect x="590" y="200" width="350" height="90" rx="8" fill="#8b5cf6" opacity="0.04"/>
  <text x="610" y="224" fill="#64748b" font-size="11">• 提示词即代码（版本化）</text>
  <text x="610" y="244" fill="#64748b" font-size="11">• AI 幻觉防御（机器验证）</text>
  <text x="610" y="264" fill="#64748b" font-size="11">• 上下文交接（handoff）</text>

  <!-- 前端/全栈岗 -->
  <rect x="40" y="300" width="180" height="90" rx="8" fill="#10b981" opacity="0.10" stroke="#34d399" stroke-width="1.2"/>
  <text x="130" y="328" text-anchor="middle" fill="#6ee7b7" font-size="13" font-weight="700">前端 / 全栈岗</text>
  <text x="130" y="350" text-anchor="middle" fill="#64748b" font-size="10">Vue / React</text>
  <rect x="230" y="300" width="350" height="90" rx="8" fill="#10b981" opacity="0.06" stroke="#34d399" stroke-width="1"/>
  <text x="250" y="324" fill="#94a3b8" font-size="11">• PRD 解析平台前端（Vue3+TS+Pinia）</text>
  <text x="250" y="344" fill="#94a3b8" font-size="11">• 看板 / 校验面板 / 分片上传</text>
  <text x="250" y="364" fill="#94a3b8" font-size="11">• 文档体系（2W+ 行）</text>
  <rect x="590" y="300" width="350" height="90" rx="8" fill="#10b981" opacity="0.04"/>
  <text x="610" y="324" fill="#64748b" font-size="11">• 组件化 / 状态管理</text>
  <text x="610" y="344" fill="#64748b" font-size="11">• 自动化测试体系</text>
  <text x="610" y="364" fill="#64748b" font-size="11">• 工程规范沉淀</text>

  <text x="490" y="425" text-anchor="middle" fill="#475569" font-size="16">原则：主推亮点写进简历前 3 行，隐含亮点留着面试被追问时"惊喜揭晓"</text>
  <text x="490" y="446" text-anchor="middle" fill="#475569" font-size="16">面试官追问你"擅长什么"，你只需要沿着主推亮点走，其余当作加分彩蛋</text>
</svg>
```

### 2.6 把"难点"写进简历的 3 个高阶姿势（让面试官主动追问）

除了四段式模板，还有 3 个"暗线"技巧能让简历在筛选中脱颖而出——它们专治"看着挺好，但面试官不知道该问什么"的尴尬：

**姿势 1：留一个"钩子数字"，但不解释**
> 例："覆盖 6 语言 120+ 框架，识别拿不准就停下来询问用户"。不写"怎么做到的"，让面试官忍不住问"检测怎么保证不识别错"。

**姿势 2：主动写"采过的坑"，而不是只写成果**
> 例："参数化分层覆盖方案，第一版用复制粘贴实现，6 语言同步改 6 处——重构为分层覆盖后，改 1 处全语言生效"。坑 = 真实，重构 = 成长，这条自带故事性。

**姿势 3：写清"边界"和"取舍"，显得有判断力**
> 例："软闸门：存量违规只减不减（允许历史债存在），新增代码 0 violation（硬门槛），安全类例外立即修"。写出"我允许什么、不允许什么"，直接展示架构师视角。

> 💡 **核心心法**：简历不是"项目说明书"，是"面试官追问的导航图"。你预埋的每个钩子，都是把面试引向你准备好的故事——**引导面试官往你最强的方向问**。

### 2.7 简历避坑：这 6 个雷区最容易把项目写废

有同学说"我写了呀，为什么没有面试？"——大概率踩了下面的坑。对照自查，把简历里的雷排掉：

**雷区 1：只写技术栈，不写解决什么**
> ❌ "使用 Vue3 + Spring Boot + MySQL 开发"
> ✅ "为 AI 写代码质量失控问题，设计了可自动检测的 6 阶段流水线"

**雷区 2：动词太弱，全是"参与""协助"**
> ❌ "参与开发了脚手架"
> ✅ "设计并落地了脚手架"——主动动词才有分量

**雷区 3：量化数字随手编，经不起追问**
> ❌ "大幅提升了开发效率"
> ✅ "覆盖 5 种语言、80+ 框架、39 个技能包"——每个数字都能展开讲

**雷区 4：简历里写满了，面试没得讲**
> ❌ 把 100% 细节全写在简历上
> ✅ 留 30% 的关键细节（如"三层混合解析"的取舍）到面试再讲，制造"惊喜感"

**雷区 5：没有"故事钩子"**
> ❌ 全部是结果陈述
> ✅ 埋 3 个钩子："这里我做了个关键取舍""这里我遇到过坑""这是我最有成就感的模块"

**雷区 6：一份简历投所有岗位**
> ❌ 投后端也写前端亮点、投 AI 岗只写 CRUD
> ✅ 按 2.4 的矩阵，为每个岗位换"主角"

> 💡 **自查口诀**：把简历给一个不懂技术的人看，如果他能在 30 秒内说出"你造了什么、解决了什么、结果怎么样"——这份简历就合格了。

---

## 第三部分：面试官会深挖的三个故事（迁移 / 架构体检 / 软闸门）

写进简历只是第一步。面试官一定会围绕项目**深挖 3~5 层**，如果你答不到"机制层"，简历写得再好也会翻车。这套脚手架天然自带三个"经得起深挖"的故事——把它们练熟，就是面试里的王牌。

### 3.1 故事一：棕地迁移的三个阶段（被问"你遇到的最难问题"）

> 常见问法："讲一个你解决过的最难的问题 / 一次有挑战的技术决策。"

这个问题 90% 的候选人都答成"我调了个 bug"，而你可以讲一个**有方法论、有阶段、有取舍**的故事：

```svg
<svg xmlns="http://www.w3.org/2000/svg" width="980" height="330" viewBox="0 0 980 330">
  <defs>
    <linearGradient id="bgE" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arE" markerWidth="12" markerHeight="12" refX="10" refY="6" orient="auto">
      <path d="M 0 0 L 12 6 L 0 12 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="980" height="330" fill="url(#bgE)"/>
  <text x="490" y="30" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">棕地迁移三阶段：从"哪里有债"到"债务还清"</text>

  <!-- 阶段一 -->
  <rect x="40" y="55" width="270" height="200" rx="12" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="2"/>
  <text x="175" y="84" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">① 摸底阶段</text>
  <text x="175" y="104" text-anchor="middle" fill="#64748b" font-size="10">1-2 周 · "哪里有债"</text>
  <line x1="65" y1="116" x2="285" y2="116" stroke="#38bdf8" stroke-width="0.8" opacity="0.4"/>
  <text x="65" y="142" fill="#94a3b8" font-size="12">• lint 违规热力图</text>
  <text x="65" y="164" fill="#94a3b8" font-size="12">• 测试覆盖率基线</text>
  <text x="65" y="186" fill="#94a3b8" font-size="12">• arch-review 架构扫描</text>
  <text x="65" y="208" fill="#94a3b8" font-size="12">• domain-modeling 敲术语</text>
  <text x="65" y="238" fill="#fde68a" font-size="11">产出：baseline.md 报告</text>

  <line x1="315" y1="155" x2="355" y2="155" stroke="#475569" stroke-width="3" marker-end="url(#arE)"/>

  <!-- 阶段二 -->
  <rect x="360" y="55" width="270" height="200" rx="12" fill="#8b5cf6" opacity="0.12" stroke="#a78bfa" stroke-width="2"/>
  <text x="495" y="84" text-anchor="middle" fill="#c4b5fd" font-size="15" font-weight="700">② 套壳阶段</text>
  <text x="495" y="104" text-anchor="middle" fill="#64748b" font-size="10">2-4 周 · "先入轨"</text>
  <line x1="385" y1="116" x2="605" y2="116" stroke="#a78bfa" stroke-width="0.8" opacity="0.4"/>
  <text x="385" y="142" fill="#94a3b8" font-size="12">• apply-harness 注入约束</text>
  <text x="385" y="164" fill="#94a3b8" font-size="12">• install-skill 注册命令</text>
  <text x="385" y="186" fill="#94a3b8" font-size="12">• 软闸门：违规不增加</text>
  <text x="385" y="208" fill="#94a3b8" font-size="12">• 新增代码先入轨</text>
  <text x="385" y="238" fill="#fde68a" font-size="11">产出：.harness/ 完整骨架</text>

  <line x1="635" y1="155" x2="675" y2="155" stroke="#475569" stroke-width="3" marker-end="url(#arE)"/>

  <!-- 阶段三 -->
  <rect x="680" y="55" width="270" height="200" rx="12" fill="#10b981" opacity="0.12" stroke="#34d399" stroke-width="2"/>
  <text x="815" y="84" text-anchor="middle" fill="#6ee7b7" font-size="15" font-weight="700">③ 收敛阶段</text>
  <text x="815" y="104" text-anchor="middle" fill="#64748b" font-size="10">1-3 个月 · "还清债"</text>
  <line x1="705" y1="116" x2="925" y2="116" stroke="#34d399" stroke-width="0.8" opacity="0.4"/>
  <text x="705" y="142" fill="#94a3b8" font-size="12">• 安全债立即修</text>
  <text x="705" y="164" fill="#94a3b8" font-size="12">• 逐模块改造（六阶段流水线）</text>
  <text x="705" y="186" fill="#94a3b8" font-size="12">• 闸门渐进收紧</text>
  <text x="705" y="208" fill="#94a3b8" font-size="12">• 违规清零</text>
  <text x="705" y="238" fill="#fde68a" font-size="11">产出：基线指标达成</text>

  <text x="490" y="290" text-anchor="middle" fill="#475569" font-size="16">迁移不是"一次改造"，是"一个收敛过程"——这句话本身就是面试官想听的工程判断</text>
  <text x="490" y="312" text-anchor="middle" fill="#475569" font-size="16">讲法口诀：先讲痛点(存量很乱) → 再讲策略(先摸底再套壳) → 最后讲取舍(安全债无缓冲)</text>
</svg>
```

**答题框架（30 秒版）**：
1. **痛点**：存量项目 5 万行代码，0 测试，lint 违规上千条——直接套规则会全线红灯。
2. **策略**：我不做"一刀切重写"，而是分三阶段——先摸底建档（数据说话），再套壳入轨（软闸门先跑），最后逐模块收敛。
3. **取舍**：安全债必须立即修，其他债排队还——因为 CI 安全扫描是**全仓库扫描**，不分新旧代码。
4. **结果**：存量违规不增加，新增代码全部入轨，覆盖率从 0 提到目标值。

### 3.2 故事二：用 arch-review 扫描架构摩擦（被问"你怎么保证架构质量"）

> 常见问法："代码架构怎么维护？ / 你怎么发现架构问题？"

这套脚手架的答案很具体：**架构不是靠人评审，而是靠工具定期"体检"**。

```svg
<svg xmlns="http://www.w3.org/2000/svg" width="980" height="340" viewBox="0 0 980 340">
  <defs>
    <linearGradient id="bgF" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arF" markerWidth="12" markerHeight="12" refX="10" refY="6" orient="auto">
      <path d="M 0 0 L 12 6 L 0 12 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="980" height="340" fill="url(#bgF)"/>
  <text x="490" y="30" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">arch-review 架构体检：扫描 → 报告 → 打磨 → 再扫描</text>

  <!-- 扫描 -->
  <rect x="40" y="55" width="190" height="130" rx="12" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="2"/>
  <text x="135" y="84" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">① 扫描</text>
  <line x1="60" y1="96" x2="210" y2="96" stroke="#38bdf8" stroke-width="0.6" opacity="0.3"/>
  <text x="65" y="120" fill="#94a3b8" font-size="12">• 找浅模块（无深度）</text>
  <text x="65" y="142" fill="#94a3b8" font-size="12">• 找循环依赖</text>
  <text x="65" y="164" fill="#94a3b8" font-size="12">• 找耦合过重</text>

  <line x1="235" y1="120" x2="270" y2="120" stroke="#475569" stroke-width="3" marker-end="url(#arF)"/>

  <!-- 报告 -->
  <rect x="275" y="55" width="190" height="130" rx="12" fill="#8b5cf6" opacity="0.12" stroke="#a78bfa" stroke-width="2"/>
  <text x="370" y="84" text-anchor="middle" fill="#c4b5fd" font-size="15" font-weight="700">② 生成报告</text>
  <line x1="295" y1="96" x2="445" y2="96" stroke="#a78bfa" stroke-width="0.6" opacity="0.3"/>
  <text x="300" y="120" fill="#94a3b8" font-size="12">• Mermaid 架构图</text>
  <text x="300" y="142" fill="#94a3b8" font-size="12">• 摩擦点清单</text>
  <text x="300" y="164" fill="#94a3b8" font-size="12">• 按严重度分级</text>

  <line x1="470" y1="120" x2="505" y2="120" stroke="#475569" stroke-width="3" marker-end="url(#arF)"/>

  <!-- 打磨 -->
  <rect x="510" y="55" width="190" height="130" rx="12" fill="#f59e0b" opacity="0.12" stroke="#fbbf24" stroke-width="2"/>
  <text x="605" y="84" text-anchor="middle" fill="#fcd34d" font-size="15" font-weight="700">③ 逐一打磨</text>
  <line x1="530" y1="96" x2="680" y2="96" stroke="#fbbf24" stroke-width="0.6" opacity="0.3"/>
  <text x="535" y="120" fill="#94a3b8" font-size="12">• 每个摩擦点一个改动</text>
  <text x="535" y="142" fill="#94a3b8" font-size="12">• 走六阶段流水线</text>
  <text x="535" y="164" fill="#94a3b8" font-size="12">• 留档到 changes/</text>

  <line x1="705" y1="120" x2="740" y2="120" stroke="#475569" stroke-width="3" marker-end="url(#arF)"/>

  <!-- 再扫描 -->
  <rect x="745" y="55" width="195" height="130" rx="12" fill="#10b981" opacity="0.12" stroke="#34d399" stroke-width="2"/>
  <text x="842" y="84" text-anchor="middle" fill="#6ee7b7" font-size="15" font-weight="700">④ 再扫描</text>
  <line x1="765" y1="96" x2="920" y2="96" stroke="#34d399" stroke-width="0.6" opacity="0.3"/>
  <text x="770" y="120" fill="#94a3b8" font-size="12">• 对比上次报告</text>
  <text x="770" y="142" fill="#94a3b8" font-size="12">• 验证摩擦点减少</text>
  <text x="770" y="164" fill="#94a3b8" font-size="12">• 形成"体检闭环"</text>

  <!-- 回环箭头 -->
  <path d="M 842 185 Q 842 240 490 240 Q 135 240 135 185" fill="none" stroke="#475569" stroke-width="2" stroke-dasharray="6,4" marker-end="url(#arF)"/>
  <text x="490" y="258" text-anchor="middle" fill="#64748b" font-size="12">闭环：每次体检 → 打磨 → 再体检，架构摩擦持续下降</text>

  <text x="490" y="300" text-anchor="middle" fill="#475569" font-size="16">面试加分点：强调"架构是持续演进的，不是一次性设计"，并用工具把这件事自动化</text>
  <text x="490" y="320" text-anchor="middle" fill="#475569" font-size="16">类比话术："就像人定期体检——不能等生病了才去医院，架构也要每月体检一次"</text>
</svg>
```

**答题框架**：把 arch-review 讲成"**架构的 CI**"——不靠人盯着，靠扫描器定期跑，报告留档、逐项打磨、闭环收敛。面试官听到"闭环"两个字就会眼睛一亮。

### 3.3 故事三：软闸门——存量违规的"缓冲区"设计（被问"怎么平衡规范与效率"）

> 常见问法："如果存量代码全是坏味道，你怎么落地规范？ / 你怎么说服团队配合？"

这是这套脚手架**最值得讲的设计**，因为它展示了你懂**工程现实**——不是理想化地"全部推倒重来"。

```svg
<svg xmlns="http://www.w3.org/2000/svg" width="980" height="360" viewBox="0 0 980 360">
  <defs>
    <linearGradient id="bgG" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arG" markerWidth="12" markerHeight="12" refX="10" refY="6" orient="auto">
      <path d="M 0 0 L 12 6 L 0 12 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="980" height="360" fill="url(#bgG)"/>
  <text x="490" y="30" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">软闸门设计：对新增代码严格，对存量代码"只增不减"</text>

  <!-- 左侧：存量 -->
  <rect x="40" y="55" width="430" height="200" rx="12" fill="#f59e0b" opacity="0.10" stroke="#fbbf24" stroke-width="1.5"/>
  <text x="255" y="84" text-anchor="middle" fill="#fcd34d" font-size="15" font-weight="700">存量代码（历史债）</text>
  <line x1="60" y1="96" x2="450" y2="96" stroke="#fbbf24" stroke-width="0.6" opacity="0.3"/>
  <text x="60" y="124" fill="#94a3b8" font-size="12">• lint：违规数不增加（baseline 比对）</text>
  <text x="60" y="148" fill="#94a3b8" font-size="12">• 覆盖率：不降低（基线比对）</text>
  <text x="60" y="172" fill="#94a3b8" font-size="12">• 架构约束：违规不增加</text>
  <text x="60" y="196" fill="#94a3b8" font-size="12">• 存量违规：登记 → 排队改造</text>
  <text x="60" y="230" fill="#fde68a" font-size="12">✅ 允许"旧债存在但被记录"</text>

  <!-- 右侧：新增 -->
  <rect x="510" y="55" width="430" height="200" rx="12" fill="#10b981" opacity="0.10" stroke="#34d399" stroke-width="1.5"/>
  <text x="725" y="84" text-anchor="middle" fill="#6ee7b7" font-size="15" font-weight="700">新增代码（增量）</text>
  <line x1="530" y1="96" x2="920" y2="96" stroke="#34d399" stroke-width="0.6" opacity="0.3"/>
  <text x="530" y="124" fill="#94a3b8" font-size="12">• lint：0 violation（硬门禁）</text>
  <text x="530" y="148" fill="#94a3b8" font-size="12">• 覆盖率：核心逻辑 ≥80%</text>
  <text x="530" y="172" fill="#94a3b8" font-size="12">• 安全扫描：全仓库，立即修</text>
  <text x="530" y="196" fill="#94a3b8" font-size="12">• 测试：先写失败测试再实现</text>
  <text x="530" y="230" fill="#fde68a" font-size="12">🚫 新增代码不允许带债合入</text>

  <!-- 中间隔离带 -->
  <rect x="290" y="130" width="160" height="40" rx="20" fill="#1e293b" stroke="#475569" stroke-width="1.5"/>
  <text x="370" y="154" text-anchor="middle" fill="#e2e8f0" font-size="13" font-weight="700">软闸门</text>

  <!-- 底部：安全债 -->
  <rect x="150" y="270" width="680" height="50" rx="10" fill="#ef4444" opacity="0.08" stroke="#ef4444" stroke-width="1.5"/>
  <text x="170" y="293" fill="#f87171" font-size="13" font-weight="700">例外：安全债没有缓冲区</text>
  <text x="340" y="293" fill="#94a3b8" font-size="12">硬编码密钥 / 危险 SQL / 无鉴权接口 → 立即修，因为安全扫描全仓库执行</text>
  <text x="490" y="312" fill="#64748b" font-size="16">核心洞察：不是"不治存量"，而是"给存量一段还债时间"，同时坚决保护增量</text>

  <text x="490" y="342" text-anchor="middle" fill="#475569" font-size="16">面试官听到"对新增严格、对存量渐进、安全无缓冲"——就知道你是真的做过工程的人</text>
</svg>
```

**答题框架**：
1. **矛盾**：存量上千条违规不可能一夜清零，硬闸门会堵死团队。
2. **设计**：分作用域——新增代码硬门禁（0 violation），存量代码"违规数不增加"（baseline 比对），覆盖率"不降低"。
3. **例外**：安全债无缓冲区，立即修。
4. **哲学**：这不是纵容旧债，而是**用时间换空间**——先让团队适应"有标尺"，再逐级收紧（软 → 半硬 → 硬）。

> 💡 **面试官最爱的三句话**：
> 1. "我用数据说话，先摸底再动手"（数据驱动）
> 2. "我不追求一步到位，而是渐进收敛"（工程现实）
> 3. "安全是红线，其他可以排队"（优先级判断）

### 3.4 追问防御链：三个故事被追问时怎么接

面试官听完你的故事，一定会往下追。预先把"追问链"准备好，你就能从"被问倒"变成"引导提问"。

```
故事一（迁移）的追问链：
  追问1 "迁移完覆盖率提到多少？"      → 答：从 0 提到核心逻辑 ≥80%，给具体数字
  追问2 "lint 上千条违规你怎么排优先级？" → 答：按"严重度 × 影响面"打分，安全类最先
  追问3 "你为什么不直接重写？"          → 答：5 万行存量，重写成本高、风险大、业务不能停
  追问4 "迁移过程中业务还在迭代吗？"    → 答：在。所以用软闸门，新增代码先入轨，存量排队还

故事二（架构体检）的追问链：
  追问1 "体检扫描具体扫什么？"          → 答：浅模块 / 循环依赖 / 耦合度，输出 Mermaid 图
  追问2 "谁负责改？改坏了怎么办？"      → 答：每个摩擦点一个 Change，走流水线，可回滚
  追问3 "多久扫一次？"                  → 答：计划任务定时扫，代码变更时增量扫
  追问4 "你如何证明摩擦在减少？"        → 答：两次报告的摩擦点数量对比，留档可查

故事三（软闸门）的追问链：
  追问1 "存量违规到底还还不还？"        → 答：还，登记造册、定级排队、逐周消化
  追问2 "团队不配合怎么办？"            → 答：先数据说话（展示违规热力图），再给缓冲期
  追问3 "覆盖率基线是多少？谁定的？"    → 答：摸底实测 + 团队共识，写入 baseline.md
  追问4 "你怎么防止有人改基线作弊？"    → 答：基线文件进版本库 + 变更走评审，改基线要留痕
```

**关键心法**：每答一个追问，都主动抛一个"钩子"（如"这里还有个设计细节"），把面试节奏握在自己手里。

---

## 第四部分：50 道"本项目专属"经典面试题（8 大方向分类）

> ⚠️ **重点提示**：这一部分的 50 道题**不是通用八股（如 B+ 树、死锁、Redis）**，而是围绕本项目真实机制设计的**项目专属经典问题**——面试官看到这份简历，追问的一定是"参数化分层覆盖""软闸门 baseline""变更状态机防跳步"这些只有你项目里才有的东西。每题都有**考察意图**与**答题要点**，全部基于本仓库真实实现。

**怎么用这 50 道题（先花 1 分钟看这个）**：

这 50 道题不是让你背的，而是给你一张"查漏补缺地图"。建议这样用：

1. **第一遍（通读）**：从头到尾看一遍，标出"完全不会"和"能说但没把握"的题——**会暴露你对自己项目的盲区**，这是最珍贵的。
2. **第二遍（分优先级）**：先啃高频追问题（Q1 方法论、Q6 双轴评审、Q9 参数化、Q16 检测、Q22 软闸门、Q29 状态机、Q35 评审闭环、Q41 PRD 解析），这些是面试官的"第一落点"。
3. **第三遍（实战演练）**：每道题**开口讲一遍**，录下来回听——你会发现"脑子里会"和"说出来"差很远。
4. **最后一招（面试前夜）**：只复习"答题要点"那部分，因为它既是你的答案，也是你的差异化记忆点。

> 💡 **一个提醒**：这些问题都没有"标准答案"，考察的是**你对你自己的项目理解得有多深**。答得好坏不取决于背得多熟，而取决于你真正动手做过多少个细节。

```svg
<svg xmlns="http://www.w3.org/2000/svg" width="980" height="420" viewBox="0 0 980 420">
  <defs>
    <linearGradient id="bgH" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
  </defs>
  <rect width="980" height="420" fill="url(#bgH)"/>
  <text x="490" y="30" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">50 道面试题全景：8 大方向 × 分布</text>

  <!-- 8 个分类块 -->
  <rect x="40" y="50" width="210" height="62" rx="10" fill="#0ea5e9" opacity="0.14" stroke="#38bdf8" stroke-width="1.5"/>
  <text x="145" y="76" text-anchor="middle" fill="#7dd3fc" font-size="16" font-weight="700">8 题</text>
  <text x="145" y="98" text-anchor="middle" fill="#94a3b8" font-size="12">① 方法论与流水线</text>

  <rect x="260" y="50" width="210" height="62" rx="10" fill="#8b5cf6" opacity="0.14" stroke="#a78bfa" stroke-width="1.5"/>
  <text x="365" y="76" text-anchor="middle" fill="#c4b5fd" font-size="16" font-weight="700">7 题</text>
  <text x="365" y="98" text-anchor="middle" fill="#94a3b8" font-size="12">② 参数化渲染引擎</text>

  <rect x="480" y="50" width="210" height="62" rx="10" fill="#f59e0b" opacity="0.14" stroke="#fbbf24" stroke-width="1.5"/>
  <text x="585" y="76" text-anchor="middle" fill="#fcd34d" font-size="16" font-weight="700">6 题</text>
  <text x="585" y="98" text-anchor="middle" fill="#94a3b8" font-size="12">③ 自动检测引擎</text>

  <rect x="700" y="50" width="240" height="62" rx="10" fill="#10b981" opacity="0.14" stroke="#34d399" stroke-width="1.5"/>
  <text x="820" y="76" text-anchor="middle" fill="#6ee7b7" font-size="16" font-weight="700">7 题</text>
  <text x="820" y="98" text-anchor="middle" fill="#94a3b8" font-size="12">④ 软闸门与迁移</text>

  <rect x="40" y="122" width="210" height="62" rx="10" fill="#ef4444" opacity="0.14" stroke="#f87171" stroke-width="1.5"/>
  <text x="145" y="148" text-anchor="middle" fill="#fca5a5" font-size="16" font-weight="700">7 题</text>
  <text x="145" y="170" text-anchor="middle" fill="#94a3b8" font-size="12">⑤ 变更状态机</text>

  <rect x="260" y="122" width="210" height="62" rx="10" fill="#0ea5e9" opacity="0.14" stroke="#38bdf8" stroke-width="1.5"/>
  <text x="365" y="148" text-anchor="middle" fill="#7dd3fc" font-size="16" font-weight="700">6 题</text>
  <text x="365" y="170" text-anchor="middle" fill="#94a3b8" font-size="12">⑥ LLM 与 AI Agent</text>

  <rect x="480" y="122" width="210" height="62" rx="10" fill="#8b5cf6" opacity="0.14" stroke="#a78bfa" stroke-width="1.5"/>
  <text x="585" y="148" text-anchor="middle" fill="#c4b5fd" font-size="16" font-weight="700">5 题</text>
  <text x="585" y="170" text-anchor="middle" fill="#94a3b8" font-size="12">⑦ 工程化扩展</text>

  <rect x="700" y="122" width="240" height="62" rx="10" fill="#f59e0b" opacity="0.14" stroke="#fbbf24" stroke-width="1.5"/>
  <text x="820" y="148" text-anchor="middle" fill="#fcd34d" font-size="16" font-weight="700">4 题</text>
  <text x="820" y="170" text-anchor="middle" fill="#94a3b8" font-size="12">⑧ 复盘与行为</text>

  <!-- 合计 -->
  <rect x="40" y="210" width="900" height="70" rx="12" fill="#1e293b" stroke="#475569" stroke-width="1.5"/>
  <text x="490" y="238" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">合计 50 题</text>
  <text x="490" y="262" text-anchor="middle" fill="#64748b" font-size="12">8 + 7 + 6 + 7 + 7 + 6 + 5 + 4 = 50</text>

  <!-- 底部建议 -->
  <text x="490" y="315" text-anchor="middle" fill="#e2e8f0" font-size="15" font-weight="700">建议复习节奏</text>
  <rect x="60" y="330" width="260" height="45" rx="8" fill="#0ea5e9" opacity="0.10" stroke="#38bdf8" stroke-width="1"/>
  <text x="190" y="350" text-anchor="middle" fill="#7dd3fc" font-size="12">第一周：①-④ 机制主干（28 题）</text>
  <text x="190" y="367" text-anchor="middle" fill="#64748b" font-size="10">先吃透"为什么这么设计"</text>
  <rect x="360" y="330" width="260" height="45" rx="8" fill="#8b5cf6" opacity="0.10" stroke="#a78bfa" stroke-width="1"/>
  <text x="490" y="350" text-anchor="middle" fill="#c4b5fd" font-size="12">第二周：⑤-⑥ 状态机与 LLM（13 题）</text>
  <text x="490" y="367" text-anchor="middle" fill="#64748b" font-size="10">重机制、重权衡</text>
  <rect x="660" y="330" width="260" height="45" rx="8" fill="#10b981" opacity="0.10" stroke="#34d399" stroke-width="1"/>
  <text x="790" y="350" text-anchor="middle" fill="#6ee7b7" font-size="12">第三周：⑦-⑧ 工程化 + 复盘（9 题）</text>
  <text x="790" y="367" text-anchor="middle" fill="#64748b" font-size="10">练表达、练讲故事</text>

  <text x="490" y="405" text-anchor="middle" fill="#475569" font-size="16">建议：每道题都先自己口答一遍，再对照要点修正——"能说出来"和"会做"是两回事</text>
</svg>
```



### 4.1 方向一：方法论与流水线（8 题）

---

**Q1. 这套项目的核心方法论是什么？一句话讲清“你造了什么”？（⚡项目定位必考 · 完整展开）**

**🎯 面试官想考什么**

这是面试的第一问，也是后续一切追问的“总开关”。它考的不是技术细节，而是你能不能把整个项目**抽象成一句话**——能抽象，说明你想清楚过；不能，说明你只是照着别人做了一遍。面试官会用你对这一问的回答决定后面往哪个方向追问。

**🧭 答题框架（PREP）**

- **Point（结论先行）**：九个字——“人类设计约束、AI 写代码、机器验证”。
- **Reason（为什么这么设计）**：AI 写代码最大的问题是质量不稳定、不可追溯，靠人盯着盯不过来，必须把“约束”和“验证”变成基础设施，而不是依赖个人自觉。
- **Example（落地举证）**：把“需求→编码→测试→评审→CI→部署”组织成 6 阶段流水线，每阶段都有机器可判定的出口门禁；再用 39 个技能包（SKILL.md）把方法论落到 5 种语言、80+ 框架上。
- **Point（回扣升华）**：所以这不是一个工具，而是一套 AI 时代软件开发的基础设施。



<svg xmlns="http://www.w3.org/2000/svg" width="980" height="420" viewBox="0 0 980 482.0">
  <defs>
    <linearGradient id="bgq1" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arq1" markerWidth="9" markerHeight="9" refX="8" refY="4.5" orient="auto">
      <path d="M 0 0 L 9 4.5 L 0 9 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="980" height="482.0" fill="url(#bgq1)"/>
  <text x="490" y="42" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">图 1 · 方法论核心：把经验变成可安装的基础设施</text>
  <text x="490" y="464" text-anchor="middle" fill="#475569" font-size="16">同样的代码量，定位不同，简历含金量天差地别</text>
<rect x="40" y="104" width="427" height="128" rx="10" fill="#475569" opacity="0.12" stroke="#64748b" stroke-width="1.6"/>
<text x="253.5" y="131" text-anchor="middle" fill="#cbd5e1" font-size="15" font-weight="700">普通业务项目</text>
<text x="253.5" y="156" text-anchor="middle" fill="#94a3b8" font-size="11">CRUD · 调接口 · 无沉淀</text>
<text x="253.5" y="174" text-anchor="middle" fill="#94a3b8" font-size="11">价值：可替代</text>
<line x1="467" y1="168.0" x2="513" y2="168.0" stroke="#475569" stroke-width="3" marker-end="url(#arq1)"/>
<rect x="513" y="104" width="427" height="128" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="726.5" y="131" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">工程基础设施</text>
<text x="726.5" y="156" text-anchor="middle" fill="#94a3b8" font-size="11">方法论+技能包+门禁+Agent</text>
<text x="726.5" y="174" text-anchor="middle" fill="#94a3b8" font-size="11">价值：稀缺·有壁垒·有故事</text>
<rect x="40" y="316" width="900" height="128" rx="10" fill="#10b981" opacity="0.12" stroke="#34d399" stroke-width="1.6"/>
<text x="490.0" y="343" text-anchor="middle" fill="#6ee7b7" font-size="15" font-weight="700">一句话定位</text>
<text x="490.0" y="368" text-anchor="middle" fill="#94a3b8" font-size="11">目标 × 机制 × 结果</text>
<text x="490.0" y="386" text-anchor="middle" fill="#94a3b8" font-size="11">把隐性经验工程化</text>
<line x1="253.5" y1="232" x2="340.0" y2="316" stroke="#475569" stroke-width="3" marker-end="url(#arq1)"/>
<line x1="726.5" y1="232" x2="640.0" y2="316" stroke="#475569" stroke-width="3" marker-end="url(#arq1)"/>
</svg>

**🗣️ 参考回答示范**

“我做的是一个 AI 协作开发脚手架——Harness，核心方法论可以概括成九个字：**人类设计约束、AI 写代码、机器验证**。人负责定义‘怎么做的规则’，AI 沿着 6 阶段流水线生产代码，机器在每个阶段出口用门禁验证。具体展开：我把需求分析、编码、测试、评审、CI、部署六件事，各自做成了一个可复用的技能包，每个阶段都有一个输出物和一个门禁——比如编码阶段要求‘先写失败测试并转绿’，评审阶段要求‘0 个严重问题才放行’，核心逻辑覆盖率要求 80% 以上。整套流水线覆盖 5 种语言、80+ 主流框架，任何项目装上就能用。为什么必须这么设计？因为 AI 生成代码的质量如果不靠约束和验证兜底，就不可控、不可追溯。所以本质上我做的是一个 AI 时代的软件开发基础设施，而不是某个具体工具。”

**🌟 加分点**

- 主动点出 Harness 名字的来源：缰绳——“人类通过规则引导 AI，而不是让 AI 自由发挥”。
- 用“门禁”把三个原则串成闭环：约束（规则）→ 生产（流水线）→ 验证（门禁），说明三者不是并列而是咬合。

**🛡️ 追问防御链**

- **追问“为什么是这 6 个阶段，不是 5 个或 7 个？”** → 因为每个阶段都有一个机器可判定的门禁和一份可验证的产出：规格卡、代码、测试报告、评审报告、CI 结果、验证报告。多一段没有新门禁，少一段就漏掉一个质量关口。
- **追问“AI、人、机器三者的职责边界在哪？”** → 人做两件事：定规则、关键节点裁决（评审、冲突选择）；AI 在规则轨道内生产；机器做验证——“做没做对”不由任何人说了算，只由门禁说了算。

---

**Q2. 6 阶段流水线是哪 6 个阶段？为什么是这 6 个？（流水线基础）**

**🎯 面试官想考什么**：确认你不只是会背“需求、编码、测试”，而是真理解每个阶段“干什么、产出什么、卡什么”。

**🧭 答题框架（PREP）**
- **P**：6 阶段 = ① 需求分析（harnessing）→ ② 编码实现（coding-skill）→ ③ 测试完善（unit-test-write）→ ④ 专家评审（expert-reviewer）→ ⑤ CI 门禁（unit-test-ci）→ ⑥ 部署验证（deploy-verify）。
- **R**：划分依据是“每个阶段都必须有一个机器可判定的出口门禁 + 一份可验证的产出”。
- **E**：举例——② 的门禁是“失败测试先存在并转绿、构建/lint 通过”；⑤ 是“六段检查全绿”。
- **P**：所以阶段数不是拍脑袋，是“质量关口”的数量决定的。



<svg xmlns="http://www.w3.org/2000/svg" width="980" height="320" viewBox="0 0 980 320">
  <defs>
    <linearGradient id="bgq2" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arq2" markerWidth="9" markerHeight="9" refX="8" refY="4.5" orient="auto">
      <path d="M 0 0 L 9 4.5 L 0 9 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="980" height="320" fill="url(#bgq2)"/>
  <text x="490" y="42" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">图 2 · 6 阶段流水线：每阶段有门禁有产出</text>
  <text x="490" y="292" text-anchor="middle" fill="#475569" font-size="16">严格按序、不可跳步</text>
<rect x="40" y="112" width="111" height="118" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="95.5" y="139" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">需求分析</text>
<text x="95.5" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">产出规格卡</text>
<text x="95.5" y="182" text-anchor="middle" fill="#94a3b8" font-size="11">门禁:AC可测</text>
<line x1="151" y1="171.0" x2="197" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq2)"/>
<rect x="197" y="112" width="111" height="118" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="252.5" y="139" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">编码实现</text>
<text x="252.5" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">产出代码</text>
<text x="252.5" y="182" text-anchor="middle" fill="#94a3b8" font-size="11">门禁:测试转绿</text>
<line x1="308" y1="171.0" x2="354" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq2)"/>
<rect x="354" y="112" width="111" height="118" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="409.5" y="139" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">测试完善</text>
<text x="409.5" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">产出测试</text>
<text x="409.5" y="182" text-anchor="middle" fill="#94a3b8" font-size="11">门禁:覆盖率≥80%</text>
<line x1="465" y1="171.0" x2="511" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq2)"/>
<rect x="511" y="112" width="111" height="118" rx="10" fill="#8b5cf6" opacity="0.12" stroke="#a78bfa" stroke-width="1.6"/>
<text x="566.5" y="139" text-anchor="middle" fill="#c4b5fd" font-size="15" font-weight="700">专家评审</text>
<text x="566.5" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">产出 review.md</text>
<text x="566.5" y="182" text-anchor="middle" fill="#94a3b8" font-size="11">门禁:0 严重</text>
<line x1="622" y1="171.0" x2="668" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq2)"/>
<rect x="668" y="112" width="111" height="118" rx="10" fill="#8b5cf6" opacity="0.12" stroke="#a78bfa" stroke-width="1.6"/>
<text x="723.5" y="139" text-anchor="middle" fill="#c4b5fd" font-size="15" font-weight="700">CI 门禁</text>
<text x="723.5" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">六段机械化</text>
<text x="723.5" y="182" text-anchor="middle" fill="#94a3b8" font-size="11">门禁:全绿</text>
<line x1="779" y1="171.0" x2="825" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq2)"/>
<rect x="825" y="112" width="111" height="118" rx="10" fill="#10b981" opacity="0.12" stroke="#34d399" stroke-width="1.6"/>
<text x="880.5" y="139" text-anchor="middle" fill="#6ee7b7" font-size="15" font-weight="700">部署验证</text>
<text x="880.5" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">产出验证报告</text>
<text x="880.5" y="182" text-anchor="middle" fill="#94a3b8" font-size="11">门禁:健康检查</text>
</svg>

**🗣️ 参考回答示范**：“6 个阶段：需求分析、编码实现、测试完善、专家评审、CI 门禁、部署验证，每阶段对应一个技能。划分依据是每个阶段结束都要有一个机器能判定的门禁和一份产出——需求分析产出规格卡，门禁是 AC 可测试、至少 3 个边界情况；编码产出代码，门禁是失败测试转绿加构建通过；评审产出 review.md，门禁是 0 个严重问题；再往后是 CI 报告和验证报告。流水线严格按序、不可跳步。”

**🌟 加分点**：把“阶段名 = 技能名”说出来（harnessing、coding-skill…），证明管理过真实体系；补一句“⑥ 通过才叫交付，验证失败有回滚预案”。

**🛡️ 追问防御链**：追问“测试没写够会怎样？” → ③ 的出口门禁是“核心逻辑覆盖率 ≥80% 且覆盖全部 AC 与边界”，不达标根本进不了评审，不需要人提醒。

---

**Q3. 什么是 Skill（技能）？技能体系是怎么分层的？（概念辨析）**

**🎯 面试官想考什么**：考你对“技能”这个核心抽象的理解——技能不是文档，而是可复用、可测试、可按需加载的执行单元。

**🧭 答题框架（PREP）**
- **P**：Skill 是一份可复用的 playbook，告诉 AI“这个任务怎么做”，带固定前置检查、工作流、门禁判定和完成标志。
- **R**：因为任务分三种性质，技能必须分层：逻辑通用、工具不同、逻辑本身绑死某种语言。
- **E**：本仓库 39 个 SKILL.md 分四类——模板化技能（5 个，渲染进各语言包）、跨语言通用技能（3 个，直接复制）、跨语言封装组件（13 个，按代码特征按需加载）、语言包专属技能（如 java-code-review）。
- **P**：分层原则一句话——**逻辑通用 → 模板化；工具不同 → 占位符化；逻辑本身不同 → 语言包专属**。



<svg xmlns="http://www.w3.org/2000/svg" width="980" height="320" viewBox="0 0 980 320">
  <defs>
    <linearGradient id="bgq3" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arq3" markerWidth="9" markerHeight="9" refX="8" refY="4.5" orient="auto">
      <path d="M 0 0 L 9 4.5 L 0 9 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="980" height="320" fill="url(#bgq3)"/>
  <text x="490" y="42" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">图 3 · Skill 四层治理：逻辑通用就模板化</text>
  <text x="490" y="292" text-anchor="middle" fill="#475569" font-size="16">原则：通用→模板化，语言特有→放语言包</text>
<rect x="40" y="112" width="190" height="118" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="135.0" y="139" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">模板化技能</text>
<text x="135.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">coding/unit-test 等 5 个</text>
<text x="135.0" y="182" text-anchor="middle" fill="#94a3b8" font-size="11">安装时渲染进各语言</text>
<line x1="230" y1="171.0" x2="276" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq3)"/>
<rect x="276" y="112" width="190" height="118" rx="10" fill="#8b5cf6" opacity="0.12" stroke="#a78bfa" stroke-width="1.6"/>
<text x="371.0" y="139" text-anchor="middle" fill="#c4b5fd" font-size="15" font-weight="700">跨语言通用</text>
<text x="371.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">domain-modeling 等 3 个</text>
<text x="371.0" y="182" text-anchor="middle" fill="#94a3b8" font-size="11">直接复制</text>
<line x1="466" y1="171.0" x2="512" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq3)"/>
<rect x="512" y="112" width="190" height="118" rx="10" fill="#f59e0b" opacity="0.12" stroke="#fbbf24" stroke-width="1.6"/>
<text x="607.0" y="139" text-anchor="middle" fill="#fcd34d" font-size="15" font-weight="700">组件封装</text>
<text x="607.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">redis-cache 等 13 个</text>
<text x="607.0" y="182" text-anchor="middle" fill="#94a3b8" font-size="11">按代码特征按需加载</text>
<line x1="702" y1="171.0" x2="748" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq3)"/>
<rect x="748" y="112" width="190" height="118" rx="10" fill="#10b981" opacity="0.12" stroke="#34d399" stroke-width="1.6"/>
<text x="843.0" y="139" text-anchor="middle" fill="#6ee7b7" font-size="15" font-weight="700">语言包专属</text>
<text x="843.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">spring-api-convention</text>
<text x="843.0" y="182" text-anchor="middle" fill="#94a3b8" font-size="11">语言特有，无法模板化</text>
</svg>

**🗣️ 参考回答示范**：“Skill 是可复用的 playbook，告诉 AI 一个任务怎么做，包含前置检查、固定工作流和完成标志，是个能独立执行的最小闭环。本项目 39 个技能包分四层：模板化技能（coding-skill、unit-test-write 等 5 个），安装时渲染进每种语言；跨语言通用技能（domain-modeling、research 等 3 个），直接复制；跨语言封装组件（redis-cache-wrapper、kafka-toolkit 等 13 个），按代码特征按需加载；语言包专属技能（harness-java 下的 spring-api-convention 等），因为依赖 Spring 注解，根本没法模板化，直接放语言包里。分层的依据就是那条原则：逻辑通用就模板化，工具不同就占位符化，逻辑本身是某语言特有的就放语言包。”

**🌟 加分点**：用具体例子讲边界判断——“redis-cache-wrapper 的缓存逻辑跨语言通用所以放 core；spring-api-convention 依赖 Spring 注解所以放 harness-java”。

**🛡️ 追问防御链**：追问“新技能放 core 还是语言包，怎么定？” → 三问自检：逻辑跨语言通用吗（core）？只是命令/工具不同吗（占位符化）？逻辑本身绑死某语言吗（语言包）？三问之外还有评审把关。

---

**Q4. “人类设计约束”具体约束了什么？（约束体系）**

**🎯 面试官想考什么**：很多人只会说“我让 AI 写代码”，这道题看你有没有把“约束”设计成一个体系，而不是几条零散提示。

**🧭 答题框架（PREP）**
- **P**：5 条规则——编码规范、工程结构、SDD-TDD 模式、开发流程、运行时可靠性。
- **R**：每种约束对应一类 AI 失控风险：命名/魔法值、依赖方向、凭空实现不写测试、跳步、并发/事务/日志隐患。
- **E**：落地形式是 `.harness/rules/` 下的规则文件，Owner Agent 在每个阶段结束时逐条对照校验。
- **P**：关键认知——**约束不是限制 AI，而是给 AI 确定性**。



<svg xmlns="http://www.w3.org/2000/svg" width="980" height="320" viewBox="0 0 980 320">
  <defs>
    <linearGradient id="bgq4" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arq4" markerWidth="9" markerHeight="9" refX="8" refY="4.5" orient="auto">
      <path d="M 0 0 L 9 4.5 L 0 9 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="980" height="320" fill="url(#bgq4)"/>
  <text x="490" y="42" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">图 4 · 人类设计约束：5 条规则</text>
  <text x="490" y="292" text-anchor="middle" fill="#475569" font-size="16">不在 README 当口号，落在 .harness/rules/</text>
<rect x="40" y="112" width="143" height="118" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="111.5" y="139" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">编码规范</text>
<text x="111.5" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">命名 · 硬编码</text>
<text x="111.5" y="182" text-anchor="middle" fill="#94a3b8" font-size="11">异常处理</text>
<line x1="183" y1="171.0" x2="229" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq4)"/>
<rect x="229" y="112" width="143" height="118" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="300.5" y="139" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">工程结构</text>
<text x="300.5" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">目录 · 依赖方向</text>
<line x1="372" y1="171.0" x2="418" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq4)"/>
<rect x="418" y="112" width="143" height="118" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="489.5" y="139" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">SDD-TDD</text>
<text x="489.5" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">先规格</text>
<text x="489.5" y="182" text-anchor="middle" fill="#94a3b8" font-size="11">先失败测试</text>
<line x1="561" y1="171.0" x2="607" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq4)"/>
<rect x="607" y="112" width="143" height="118" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="678.5" y="139" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">开发流程</text>
<text x="678.5" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">状态机 · 不跳步</text>
<line x1="750" y1="171.0" x2="796" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq4)"/>
<rect x="796" y="112" width="143" height="118" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="867.5" y="139" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">运行时可靠性</text>
<text x="867.5" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">并发 · 事务 · 日志</text>
</svg>

**🗣️ 参考回答示范**：“5 条规则：编码规范约束命名、硬编码、异常处理；工程结构约束目录和依赖方向；SDD-TDD 约束先写规格、先写失败测试；开发流程约束状态机、不许跳步；运行时可靠性约束并发、事务、日志这些生产级细节。它们不在 README 里当口号，而是落在 `.harness/rules/` 下的规则文件，Owner Agent 在每个流水线阶段对照校验。我理解约束的本质不是限制 AI，而是**给 AI 确定性**——约束越明确，AI 发挥越稳定，人越省心。”

**🌟 加分点**：点出“规则文件也是渲染产物，跟着语言/框架参数走”——约束体系本身参数化，换语言约束自动跟着换。

**🛡️ 追问防御链**：追问“规则会不会太死，限制 AI 创造力？” → 约束管的是质量底线和一致性，不管方案选择；新技术走 ADR（架构决策记录）流程，约束本身可演进。

---

**Q5. “机器验证”验证什么？门禁体系是怎么设计的？（门禁体系）**

**🎯 面试官想考什么**：验证是“机器”部分的实体，考你是否理解“质量不靠人自觉”这句话的工程含义。

**🧭 答题框架（PREP）**
- **P**：验证 = 每阶段出口门禁 + CI 六段机械化门禁。
- **R**：人眼审查会疲劳、会漏、会妥协；机器验证把质量底线变成“不满足就不放行”的硬事实。
- **E**：CI 六段——编译 → 静态分析 → 竞态检测 → 架构约束 → 单元测试+覆盖率（≥80%）→ 安全扫描；任一红灯按失败类型退回编码或测试阶段。
- **P**：门禁的意义正是“让质量不依赖个人自觉”。



<svg xmlns="http://www.w3.org/2000/svg" width="980" height="420" viewBox="0 0 980 482.0">
  <defs>
    <linearGradient id="bgq5" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arq5" markerWidth="9" markerHeight="9" refX="8" refY="4.5" orient="auto">
      <path d="M 0 0 L 9 4.5 L 0 9 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="980" height="482.0" fill="url(#bgq5)"/>
  <text x="490" y="42" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">图 5 · 机器验证：阶段出口 + CI 六段</text>
  <text x="490" y="464" text-anchor="middle" fill="#475569" font-size="16">核心：质量不依赖个人自觉</text>
<rect x="40" y="104" width="427" height="128" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="253.5" y="131" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">阶段出口门禁</text>
<text x="253.5" y="156" text-anchor="middle" fill="#94a3b8" font-size="11">失败测试转绿</text>
<text x="253.5" y="174" text-anchor="middle" fill="#94a3b8" font-size="11">覆盖率≥80%</text>
<line x1="467" y1="168.0" x2="513" y2="168.0" stroke="#475569" stroke-width="3" marker-end="url(#arq5)"/>
<rect x="513" y="104" width="427" height="128" rx="10" fill="#8b5cf6" opacity="0.12" stroke="#a78bfa" stroke-width="1.6"/>
<text x="726.5" y="131" text-anchor="middle" fill="#c4b5fd" font-size="15" font-weight="700">CI 六段流水线</text>
<text x="726.5" y="156" text-anchor="middle" fill="#94a3b8" font-size="11">编译→静态→竞态</text>
<text x="726.5" y="174" text-anchor="middle" fill="#94a3b8" font-size="11">→架构→单测→安全</text>
<rect x="40" y="316" width="900" height="128" rx="10" fill="#ef4444" opacity="0.12" stroke="#f87171" stroke-width="1.6"/>
<text x="490.0" y="343" text-anchor="middle" fill="#fca5a5" font-size="15" font-weight="700">红灯处理</text>
<text x="490.0" y="368" text-anchor="middle" fill="#94a3b8" font-size="11">按失败类型退回对应阶段</text>
<line x1="253.5" y1="232" x2="340.0" y2="316" stroke="#475569" stroke-width="3" marker-end="url(#arq5)"/>
<line x1="726.5" y1="232" x2="640.0" y2="316" stroke="#475569" stroke-width="3" marker-end="url(#arq5)"/>
</svg>

**🗣️ 参考回答示范**：“‘机器验证’分两层：一是每个阶段的出口门禁，比如编码阶段要求失败测试转绿、构建通过，测试阶段要求核心覆盖率 ≥80%；二是 CI 阶段把门禁合并成一条机械化流水线：编译、静态分析、竞态检测、架构约束、单元测试加覆盖率、安全扫描六段，任何一段失败就红灯，按失败类型退回对应阶段补。这套设计想解决的核心问题只有一个：**质量不依赖个人自觉**——人盯会漏、会妥协，机器不会。”

**🌟 加分点**：逐个说清门禁挡什么——竞态检测挡并发 bug、架构约束挡分层腐化、安全扫描含硬编码凭据和危险 DDL 扫描，每个门禁对应一类真实事故。

**🛡️ 追问防御链**：追问“门禁全绿就一定没问题？” → 不，门禁只覆盖可机械化判定的质量维度；语义层（需求对不对、设计合不合理）交给双轴评审，两者互补。

---

**Q6. 双轴评审是哪两轴？为什么必须 0 严重问题才放行？（⚡评审机制必考 · 完整展开）**

**🎯 面试官想考什么**：“需求匹配”和“规范合规”是两类完全不同的事，考你是否理解为什么必须分开查、分开出结论——很多团队评审流于形式，根因就是把这两件事混在了一起。

**🧭 答题框架（PREP）**
- **P**：两轴 = **Spec 轴（需求匹配）** + **Standards 轴（规范合规）**，由两个独立子智能体并行开查、分开出报告。
- **R**：因为“做对了需求但代码不规范”和“代码规范但没实现需求”都不能算完成，混在一起查，总有一个轴被另一个轴掩盖。
- **E**：Spec 轴对照 change.md 逐条核对 AC、边界、scope creep；Standards 轴对照 `.harness/rules/` 查架构、编码规范、代码质量、安全（共 10 个维度）。
- **P**：所以放行条件不是“平均分合格”，而是两轴都 0 个 🔴 严重问题。



<svg xmlns="http://www.w3.org/2000/svg" width="980" height="420" viewBox="0 0 980 482.0">
  <defs>
    <linearGradient id="bgq6" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arq6" markerWidth="9" markerHeight="9" refX="8" refY="4.5" orient="auto">
      <path d="M 0 0 L 9 4.5 L 0 9 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="980" height="482.0" fill="url(#bgq6)"/>
  <text x="490" y="42" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">图 6 · 双轴评审：为什么必须两轴都 0 严重问题</text>
  <text x="490" y="464" text-anchor="middle" fill="#475569" font-size="16">面试官最爱追问"两轴冲突怎么办"——答案：按严重度定级，🔴 优先于 🟡，两轴不中和</text>
<rect x="40" y="104" width="269" height="128" rx="10" fill="#475569" opacity="0.12" stroke="#64748b" stroke-width="1.6"/>
<text x="174.5" y="131" text-anchor="middle" fill="#cbd5e1" font-size="15" font-weight="700">待评审代码</text>
<text x="174.5" y="156" text-anchor="middle" fill="#94a3b8" font-size="11">来自 coding 阶段</text>
<line x1="309" y1="168.0" x2="355" y2="168.0" stroke="#475569" stroke-width="3" marker-end="url(#arq6)"/>
<rect x="355" y="104" width="269" height="128" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="489.5" y="131" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">Spec 轴</text>
<text x="489.5" y="156" text-anchor="middle" fill="#94a3b8" font-size="11">做没做对需求</text>
<text x="489.5" y="174" text-anchor="middle" fill="#94a3b8" font-size="11">对照 AC 逐条核</text>
<line x1="624" y1="168.0" x2="670" y2="168.0" stroke="#475569" stroke-width="3" marker-end="url(#arq6)"/>
<rect x="670" y="104" width="269" height="128" rx="10" fill="#8b5cf6" opacity="0.12" stroke="#a78bfa" stroke-width="1.6"/>
<text x="804.5" y="131" text-anchor="middle" fill="#c4b5fd" font-size="15" font-weight="700">Standards 轴</text>
<text x="804.5" y="156" text-anchor="middle" fill="#94a3b8" font-size="11">合不合规范</text>
<text x="804.5" y="174" text-anchor="middle" fill="#94a3b8" font-size="11">架构/编码/安全</text>
<rect x="40" y="316" width="427" height="128" rx="10" fill="#475569" opacity="0.12" stroke="#64748b" stroke-width="1.6"/>
<text x="253.5" y="343" text-anchor="middle" fill="#cbd5e1" font-size="15" font-weight="700">两轴独立报告</text>
<text x="253.5" y="368" text-anchor="middle" fill="#94a3b8" font-size="11">不合并不排序</text>
<line x1="467" y1="380.0" x2="513" y2="380.0" stroke="#475569" stroke-width="3" marker-end="url(#arq6)"/>
<rect x="513" y="316" width="427" height="128" rx="10" fill="#f59e0b" opacity="0.12" stroke="#fbbf24" stroke-width="1.6"/>
<text x="726.5" y="343" text-anchor="middle" fill="#fcd34d" font-size="15" font-weight="700">判定：0 个严重问题</text>
<text x="726.5" y="368" text-anchor="middle" fill="#94a3b8" font-size="11">放行 → CI</text>
<text x="726.5" y="386" text-anchor="middle" fill="#94a3b8" font-size="11">任一严重 → 退回编码</text>
<path d="M 489.5 232 V 270 H 182.3 V 316" fill="none" stroke="#475569" stroke-width="3" marker-end="url(#arq6)"/>
<path d="M 804.5 232 V 292 H 324.7 V 316" fill="none" stroke="#475569" stroke-width="3" marker-end="url(#arq6)"/>
</svg>

**🗣️ 参考回答示范**：“双轴评审是这套体系里我最得意的一个设计。它把代码审查拆成两个独立的轴：Spec 轴查‘是否做对了事’——对照 change.md 逐条核对 AC 全实现了没有、边界处理了没有、有没有超出需求乱加功能；Standards 轴查‘是否合规做事’——对照规则文件查架构分层、编码规范、代码质量、安全。两轴由**独立的子智能体并行运行、分开出报告**，不合并、不排序。为什么必须这样？因为‘代码完全合规范但实现错了需求’和‘需求实现对了但代码一塌糊涂’，混在一起查就会被对方掩盖。所以放行条件是硬性的：两轴都必须 0 个 🔴 严重问题，任何一轴有严重问题就退回编码阶段，review.md 作为修复依据。”

**🌟 加分点**：补充 🔴 严重问题的具体类型（功能缺失/scope creep、安全漏洞、架构腐化）与 🟡 建议的区别；提一句“评审时会按代码特征自动加载领域技能补充检查细则”，展示体系深度。

**🛡️ 追问防御链**：
- **追问“评审是 AI 在做，可信吗？”** → 两轴是独立子智能体 + 固定检查清单，AI 只负责执行清单和引用证据，不是自由发挥打分；review.md 留档可追溯、可人工复核。
- **追问“评审通过后进入哪？”** → 0 个 🔴 就把 change.md 状态推进到 ci，进入 CI 门禁；有 🔴 退回编码阶段修复。

---

**Q7. 变更状态机在流水线里起什么作用？（状态机基础）**

**🎯 面试官想考什么**：检验你理解的流程是不是“可执行、可验证”的，而不是嘴上说说的纪律。

**🧭 答题框架（PREP）**
- **P**：变更（Change）从需求到部署有 9 个合法状态（drafting → reviewing → approved → coding → testing → reviewing → ci → verifying → done），状态机保证**不可跳步**。
- **R**：跳步的后果是“没写测试就声称完成、没评审就上线”，质量问题全部后置。
- **E**：每个技能启动时先做前置检查：扫描 `.harness/changes/*/change.md`，只处理处于自己对应状态的 change，找不到就报错退回上一阶段。
- **P**：状态机把“流程纪律”变成“机器强制”，AI 只是执行者，关键节点由人裁决。



<svg xmlns="http://www.w3.org/2000/svg" width="980" height="320" viewBox="0 0 980 320">
  <defs>
    <linearGradient id="bgq7" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arq7" markerWidth="9" markerHeight="9" refX="8" refY="4.5" orient="auto">
      <path d="M 0 0 L 9 4.5 L 0 9 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="980" height="320" fill="url(#bgq7)"/>
  <text x="490" y="42" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">图 7 · 状态机在流水线里的作用：防跳步</text>
  <text x="490" y="292" text-anchor="middle" fill="#475569" font-size="16">AI 只是执行者，不是决策者</text>
<rect x="40" y="112" width="190" height="118" rx="10" fill="#475569" opacity="0.12" stroke="#64748b" stroke-width="1.6"/>
<text x="135.0" y="139" text-anchor="middle" fill="#cbd5e1" font-size="15" font-weight="700">技能启动</text>
<text x="135.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">coding-skill 等</text>
<line x1="230" y1="171.0" x2="276" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq7)"/>
<rect x="276" y="112" width="190" height="118" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="371.0" y="139" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">前置检查</text>
<text x="371.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">扫描变更目录</text>
<line x1="466" y1="171.0" x2="512" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq7)"/>
<rect x="512" y="112" width="190" height="118" rx="10" fill="#8b5cf6" opacity="0.12" stroke="#a78bfa" stroke-width="1.6"/>
<text x="607.0" y="139" text-anchor="middle" fill="#c4b5fd" font-size="15" font-weight="700">只认匹配状态</text>
<text x="607.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">status=coding</text>
<line x1="702" y1="171.0" x2="748" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq7)"/>
<rect x="748" y="112" width="190" height="118" rx="10" fill="#ef4444" opacity="0.12" stroke="#f87171" stroke-width="1.6"/>
<text x="843.0" y="139" text-anchor="middle" fill="#fca5a5" font-size="15" font-weight="700">找不到→报错</text>
<text x="843.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">退回需求分析</text>
</svg>

**🗣️ 参考回答示范**：“状态机把‘流程纪律’变成了代码级的强制约束。每个 Change 有 9 个状态：drafting、reviewing、approved、coding、testing、reviewing、ci、verifying、done，注意 reviewing 出现两次——先评审规格，再评审代码。它的作用就四个字：**防跳步**。实现上不是靠 AI 自觉，而是每个技能启动时先做前置检查：扫描变更目录，只处理状态匹配的 change，比如 coding-skill 只认 status=coding 的变更，找不到就报错退回需求分析。所以 AI 在这个体系里只是执行者，不是决策者，真正的决策发生在人工评审和冲突确认这些节点上。”

**🌟 加分点**：指出“reviewing 两次”的细节（规格评审 vs 代码评审）；补一句“多个待处理变更时，技能会停下列出候选清单请用户选择，不擅自挑一个”。

**🛡️ 追问防御链**：追问“变更多时怎么定位目标？” → 按变更定位规则：用户指定 id 就校验状态；恰好 1 个自动选中；0 个报错退回；多个就停下来让用户选。

---

**Q8. 为什么说这个项目“能讲出方法论”？（价值升华）**

**🎯 面试官想考什么**：这道题决定面试官对你是“会用工具的人”还是“能抽象问题、能设计系统的人”的最后判断。

**🧭 答题框架（PREP）**
- **P**：它把“隐性经验”变成了“显性基础设施”。
- **R**：老工程师的规范、门禁、流程原本在人脑里——人离职经验就带走、团队越大越不一致；变成技能包后，可安装、可测试、可追溯。
- **E**：39 个 SKILL.md + 规则文件 + 变更留档，任意项目跑一遍 apply-harness 就能装上整套方法论。
- **P**：这体现的不是会用工具，而是**把经验工程化**的能力。



<svg xmlns="http://www.w3.org/2000/svg" width="980" height="320" viewBox="0 0 980 320">
  <defs>
    <linearGradient id="bgq8" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arq8" markerWidth="9" markerHeight="9" refX="8" refY="4.5" orient="auto">
      <path d="M 0 0 L 9 4.5 L 0 9 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="980" height="320" fill="url(#bgq8)"/>
  <text x="490" y="42" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">图 8 · 为什么能讲出方法论：隐性经验工程化</text>
  <text x="490" y="292" text-anchor="middle" fill="#475569" font-size="16">人走了经验带不走 → 装到任意项目上</text>
<rect x="40" y="112" width="190" height="118" rx="10" fill="#475569" opacity="0.12" stroke="#64748b" stroke-width="1.6"/>
<text x="135.0" y="139" text-anchor="middle" fill="#cbd5e1" font-size="15" font-weight="700">隐性经验</text>
<text x="135.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">老工程师脑子里的规范</text>
<line x1="230" y1="171.0" x2="276" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq8)"/>
<rect x="276" y="112" width="190" height="118" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="371.0" y="139" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">显性化</text>
<text x="371.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">规则/技能/门禁/留档</text>
<line x1="466" y1="171.0" x2="512" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq8)"/>
<rect x="512" y="112" width="190" height="118" rx="10" fill="#8b5cf6" opacity="0.12" stroke="#a78bfa" stroke-width="1.6"/>
<text x="607.0" y="139" text-anchor="middle" fill="#c4b5fd" font-size="15" font-weight="700">可安装基础设施</text>
<text x="607.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">任意项目装上即用</text>
<line x1="702" y1="171.0" x2="748" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq8)"/>
<rect x="748" y="112" width="190" height="118" rx="10" fill="#10b981" opacity="0.12" stroke="#34d399" stroke-width="1.6"/>
<text x="843.0" y="139" text-anchor="middle" fill="#6ee7b7" font-size="15" font-weight="700">不依赖个人自觉</text>
<text x="843.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">依赖可测试的体系</text>
</svg>

**🗣️ 参考回答示范**：“我认为这个项目最值钱的地方，是把‘隐性经验’工程化了。老工程师脑子里的那套规范——怎么命名、怎么分层、怎么评审、门禁卡什么——本来是隐性知识，人走了就带走，人多了就不一致。Harness 把这套东西变成了显性的、可安装的基础设施：规则文件、技能包、门禁、变更留档，任意项目装上就能复用。对团队来说，方法论不再依赖某个人的自觉，而是依赖一套可测试的体系。对我自己来说，这证明我做的不只是‘调了一个好用的工具’，而是‘把经验抽象成了系统’——这正是面试里最想看到的素质。”

**🌟 加分点**：用数据收尾——“一套技能包覆盖 6 语言、120+ 框架，说明这套抽象经得起规模检验”。

**🛡️ 追问防御链**：追问“方法论真能跨团队复制吗？团队不认怎么办？” → 复制的是机制不是文化：门禁和数据是客观的，落地用软闸门给存量留缓冲（见 Q22），机制先行、逐步收敛。

---

### 4.2 方向二：参数化与模板渲染引擎（7 题）

---

**Q9. 参数化是怎么做到“改 1 处、全语言生效”的？（⚡最亮机制 · 必考 · 完整展开）**

**🎯 面试官想考什么**：这是全项目最容易被追问的机制。面试官想确认：你说“一套模板服务 5 种语言”是真的，还是演示稿——他会追问“那语言差异怎么办”“新框架怎么接”。

**🧭 答题框架（PREP）**
- **P**：参数表是两层结构——**语言级“基础参数（通用）”块 + 框架“差异块”**，共 52 个占位符；渲染时合并：**最终参数 = 基础参数块（全量） + 框架参数块（覆盖差异）**。
- **R**：同一个方法论要落到 6 语言、120+ 框架，如果把公共和差异混在一起写，每加一种语言/框架就要复制一整份；分开后公共部分只维护一份，差异部分只写真正不同的 5-15 个。
- **E**：比如 `{{BUILD_CMD}}` 在 Go 基础块里是 `go build ./...`，Gin 框架块不重复写；而 `{{RACE_DETECT_ARG}}` = `-race` 直接继承。技能模板里永远不写死语言，只写占位符。
- **P**：所以新增一个框架只需要填一个 5 行的差异块，核心引擎零改动——这就是“改 1 处、全语言生效”的来源。



<svg xmlns="http://www.w3.org/2000/svg" width="980" height="420" viewBox="0 0 980 482.0">
  <defs>
    <linearGradient id="bgq9" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arq9" markerWidth="9" markerHeight="9" refX="8" refY="4.5" orient="auto">
      <path d="M 0 0 L 9 4.5 L 0 9 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="980" height="482.0" fill="url(#bgq9)"/>
  <text x="490" y="42" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">图 9 · 参数化：改 1 处、全语言生效</text>
  <text x="490" y="464" text-anchor="middle" fill="#475569" font-size="16">52 个占位符、6 语言、120+ 框架，维护量只有"一份公共 + 每框架一份差异"</text>
<rect x="40" y="104" width="427" height="128" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="253.5" y="131" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">基础参数块(通用)</text>
<text x="253.5" y="156" text-anchor="middle" fill="#94a3b8" font-size="11">约 30 个占位符</text>
<text x="253.5" y="174" text-anchor="middle" fill="#94a3b8" font-size="11">{{BUILD_CMD}} 等</text>
<line x1="467" y1="168.0" x2="513" y2="168.0" stroke="#475569" stroke-width="3" marker-end="url(#arq9)"/>
<rect x="513" y="104" width="427" height="128" rx="10" fill="#8b5cf6" opacity="0.12" stroke="#a78bfa" stroke-width="1.6"/>
<text x="726.5" y="131" text-anchor="middle" fill="#c4b5fd" font-size="15" font-weight="700">框架差异块(覆盖)</text>
<text x="726.5" y="156" text-anchor="middle" fill="#94a3b8" font-size="11">只写差异 5-15 个</text>
<text x="726.5" y="174" text-anchor="middle" fill="#94a3b8" font-size="11">Gin / go-zero</text>
<rect x="40" y="316" width="269" height="128" rx="10" fill="#f59e0b" opacity="0.12" stroke="#fbbf24" stroke-width="1.6"/>
<text x="174.5" y="343" text-anchor="middle" fill="#fcd34d" font-size="15" font-weight="700">合并规则</text>
<text x="174.5" y="368" text-anchor="middle" fill="#94a3b8" font-size="11">最终=基础全量+框架覆盖</text>
<line x1="309" y1="380.0" x2="355" y2="380.0" stroke="#475569" stroke-width="3" marker-end="url(#arq9)"/>
<rect x="355" y="316" width="269" height="128" rx="10" fill="#10b981" opacity="0.12" stroke="#34d399" stroke-width="1.6"/>
<text x="489.5" y="343" text-anchor="middle" fill="#6ee7b7" font-size="15" font-weight="700">模板 {{占位符}}</text>
<text x="489.5" y="368" text-anchor="middle" fill="#94a3b8" font-size="11">纯字符串替换</text>
<text x="489.5" y="386" text-anchor="middle" fill="#94a3b8" font-size="11">无任何具体语言</text>
<line x1="624" y1="380.0" x2="670" y2="380.0" stroke="#475569" stroke-width="3" marker-end="url(#arq9)"/>
<rect x="670" y="316" width="269" height="128" rx="10" fill="#475569" opacity="0.12" stroke="#64748b" stroke-width="1.6"/>
<text x="804.5" y="343" text-anchor="middle" fill="#cbd5e1" font-size="15" font-weight="700">产物</text>
<text x="804.5" y="368" text-anchor="middle" fill="#94a3b8" font-size="11">owner.md · 技能模板</text>
<path d="M 253.5 232 V 270 H 129.7 V 316" fill="none" stroke="#475569" stroke-width="3" marker-end="url(#arq9)"/>
<path d="M 726.5 232 V 292 H 219.3 V 316" fill="none" stroke="#475569" stroke-width="3" marker-end="url(#arq9)"/>
</svg>

**🗣️ 参考回答示范**：“核心是参数表和占位符两层结构。apply-harness 的参数表里，每种语言先有一个‘基础参数（通用）’块，50 多个占位符里约 30 个在这层定好，比如 Go 的 `{{BUILD_CMD}}`=`go build ./...`、`{{TEST_CMD}}`=`go test ./...`；然后每种框架一个‘差异块’，只写和基础块不同的 5-15 个参数，比如 Gin 和 go-zero 只有框架相关的那几项不同。渲染时的合并规则很简单：**最终参数 = 基础块全量 + 框架块覆盖差异**，框架块重复写基础块的参数反而是错的，会被覆盖。所有技能模板里写的都是 `{{占位符}}`，不出现任何具体语言。所以加一个新框架，只需要在参数表加一个差异块填 5 行，加一种新语言才需要再造一个语言包——而且是一次性成本。这套设计让 6 语言 120+ 框架的维护成本收敛到了‘只维护差异’。”

**🌟 加分点**：
- 讲出 52 个占位符的 7 大类（身份、运行时、工程命令、工具与库、约束阈值、技能命令后缀、其他），证明不是拍脑袋定的。
- 说出验收标准：渲染后**不允许残留任何 `{{...}}` 占位符**——机器可判定的完成条件。

**🛡️ 追问防御链**：
- **追问“为什么是参数化而不是复制粘贴？”** → 第一版试过复制粘贴，6 种语言同步改 6 处，改一次漏一次；参数化后公共部分只维护一份，差异收敛到参数表。
- **追问“框架块和基础块冲突以谁为准？”** → 框架块覆盖基础块——越具体优先级越高，类似 CSS 层叠或配置中心的按环境覆盖；所以框架块里重复写基础参数是 bug，会被静默覆盖。

---

**Q10. 52 个占位符是怎么分类的？解决了什么问题？（参数化细节）**

**🎯 面试官想考什么**：看你是“用过参数化”还是“设计过参数化”——分类方式反映你对语言差异的理解。

**🧭 答题框架（PREP）**
- **P**：52 个占位符按 7 类组织：身份、运行时、工程命令、工具与库、约束阈值、技能命令后缀、其他。
- **R**：占位符把“规则模板”和“具体语言”解耦——模板永远不写死语言，语言差异全部收敛到占位符取值。
- **E**：`{{PROJECT_NAME}}`/`{{LANG_TAG}}`（身份）、`{{BUILD_CMD}}`/`{{LINT_CMD}}`（工程命令）、`{{FILE_LIMIT}}`（约束阈值）……
- **P**：分类的价值是让“加一种语言要动哪些占位符”一目了然。



<svg xmlns="http://www.w3.org/2000/svg" width="980" height="320" viewBox="0 0 980 320">
  <defs>
    <linearGradient id="bgq10" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arq10" markerWidth="9" markerHeight="9" refX="8" refY="4.5" orient="auto">
      <path d="M 0 0 L 9 4.5 L 0 9 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="980" height="320" fill="url(#bgq10)"/>
  <text x="490" y="42" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">图 10 · 52 个占位符的分类</text>
  <text x="490" y="292" text-anchor="middle" fill="#475569" font-size="16">模板只写 {{占位符}}，语言差异全收敛到参数表取值</text>
<rect x="40" y="112" width="143" height="118" rx="10" fill="#475569" opacity="0.12" stroke="#64748b" stroke-width="1.6"/>
<text x="111.5" y="139" text-anchor="middle" fill="#cbd5e1" font-size="15" font-weight="700">身份类</text>
<text x="111.5" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">{{PROJECT_NAME}}</text>
<text x="111.5" y="182" text-anchor="middle" fill="#94a3b8" font-size="11">{{LANGUAGE}}</text>
<line x1="183" y1="171.0" x2="229" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq10)"/>
<rect x="229" y="112" width="143" height="118" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="300.5" y="139" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">运行时类</text>
<text x="300.5" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">{{LANGUAGE_RUNTIME}}</text>
<line x1="372" y1="171.0" x2="418" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq10)"/>
<rect x="418" y="112" width="143" height="118" rx="10" fill="#8b5cf6" opacity="0.12" stroke="#a78bfa" stroke-width="1.6"/>
<text x="489.5" y="139" text-anchor="middle" fill="#c4b5fd" font-size="15" font-weight="700">工程命令类</text>
<text x="489.5" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">{{BUILD_CMD}}</text>
<text x="489.5" y="182" text-anchor="middle" fill="#94a3b8" font-size="11">{{TEST_CMD}}</text>
<line x1="561" y1="171.0" x2="607" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq10)"/>
<rect x="607" y="112" width="143" height="118" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="678.5" y="139" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">工具与库类</text>
<text x="678.5" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">{{TEST_FRAMEWORK}}</text>
<line x1="750" y1="171.0" x2="796" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq10)"/>
<rect x="796" y="112" width="143" height="118" rx="10" fill="#10b981" opacity="0.12" stroke="#34d399" stroke-width="1.6"/>
<text x="867.5" y="139" text-anchor="middle" fill="#6ee7b7" font-size="15" font-weight="700">约束阈值类</text>
<text x="867.5" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">{{FILE_LIMIT}}</text>
<text x="867.5" y="182" text-anchor="middle" fill="#94a3b8" font-size="11">其他</text>
</svg>

**🗣️ 参考回答示范**：“占位符一共 42 个，分 7 类：身份类比如 `{{PROJECT_NAME}}`、`{{LANGUAGE}}`；运行时类比如 `{{LANGUAGE_RUNTIME}}`、`{{RACE_DETECT_ARG}}`；工程命令类最多，`{{BUILD_CMD}}`、`{{TEST_CMD}}`、`{{LINT_CMD}}`、`{{COV_CMD}}` 这些；工具与库类比如 `{{TEST_FRAMEWORK}}`、`{{MOCK_LIB}}`；约束阈值类比如 `{{FILE_LIMIT}}`；还有技能命令后缀类和其他。它们解决的核心问题是：技能模板里永远只写 `{{占位符}}`，不出现任何具体语言——语言差异全部收敛到参数表的取值上。分类还有个好处：评估‘加一种语言要动哪些东西’时，按类别过一遍就行。”

**🌟 加分点**：举一个跨语言对比——同一个 `{{TEST_CMD}}`，Go 是 `go test ./...`，Java 是 `mvn test`；模板一行没改，渲染出来的技能完全不同。

**🛡️ 追问防御链**：追问“会不会忘了填某个占位符？” → 不会漏到用户手里——渲染后有机器验收：扫描产物，残留 `{{...}}` 计数必须为零，否则安装流程直接失败。

把上面的"骨架"串起来，给你一场完整的模拟面试。建议你对照录音练一遍：

**Q11. 渲染引擎怎么保证“确定性”？为什么确定性重要？（可靠性）**

**🎯 面试官想考什么**：AI 项目里“确定性”是稀缺词，这道题考你有没有把“该确定的环节坚决不走 AI”想清楚。

**🧭 答题框架（PREP）**
- **P**：渲染是纯字符串模板替换：同一模板 + 同一参数 → 每次结果完全一致，无随机、无 LLM 参与。
- **R**：因为渲染产物会被门禁、基线比对、版本库留档反复使用——结果不稳定，一切比对都失去意义。
- **E**：项目扫描 + 模板渲染 + 规则复制 = 纯文件操作，所以装一个项目是秒级的。
- **P**：确定性是可测试、可复现、可缓存的前提；需要理解语义的地方才交给 LLM，两条路严格分开。



<svg xmlns="http://www.w3.org/2000/svg" width="980" height="320" viewBox="0 0 980 320">
  <defs>
    <linearGradient id="bgq11" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arq11" markerWidth="9" markerHeight="9" refX="8" refY="4.5" orient="auto">
      <path d="M 0 0 L 9 4.5 L 0 9 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="980" height="320" fill="url(#bgq11)"/>
  <text x="490" y="42" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">图 11 · 渲染引擎怎么保证确定性：不调 LLM</text>
  <text x="490" y="292" text-anchor="middle" fill="#475569" font-size="16">需要确定性的环节坚决不走 AI，需要理解语义的环节才用 LLM</text>
<rect x="40" y="112" width="190" height="118" rx="10" fill="#475569" opacity="0.12" stroke="#64748b" stroke-width="1.6"/>
<text x="135.0" y="139" text-anchor="middle" fill="#cbd5e1" font-size="15" font-weight="700">模板 + 参数</text>
<text x="135.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">同一组输入</text>
<line x1="230" y1="171.0" x2="276" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq11)"/>
<rect x="276" y="112" width="190" height="118" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="371.0" y="139" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">纯字符串替换</text>
<text x="371.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">无外部依赖</text>
<line x1="466" y1="171.0" x2="512" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq11)"/>
<rect x="512" y="112" width="190" height="118" rx="10" fill="#8b5cf6" opacity="0.12" stroke="#a78bfa" stroke-width="1.6"/>
<text x="607.0" y="139" text-anchor="middle" fill="#c4b5fd" font-size="15" font-weight="700">100 次结果一致</text>
<text x="607.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">可验证可比对</text>
<line x1="702" y1="171.0" x2="748" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq11)"/>
<rect x="748" y="112" width="190" height="118" rx="10" fill="#10b981" opacity="0.12" stroke="#34d399" stroke-width="1.6"/>
<text x="843.0" y="139" text-anchor="middle" fill="#6ee7b7" font-size="15" font-weight="700">秒级完成</text>
<text x="843.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">无网络无推理</text>
</svg>

**🗣️ 参考回答示范**：“渲染引擎刻意不调 LLM，就是纯字符串模板替换——同一份模板加同一组参数，一百次渲染结果完全一样。为什么这么较真？因为渲染产物会被反复验证：门禁要跑、基线要比对、产物要进版本库，如果渲染结果不稳定，一切比对都失去意义。而且纯模板渲染没有任何外部依赖，装一个项目就是扫描文件、替换占位符、复制规则，所以是秒级的。我的原则是：**需要确定性的环节坚决不走 AI，需要理解语义的环节才用 LLM**，两条路严格分开。”

**🌟 加分点**：点出“渲染=确定性路径、解析/评审=LLM 路径”的架构边界——AI 用得克制，恰恰是可靠性的一部分。

**🛡️ 追问防御链**：追问“渲染完全不用 AI，会不会太傻？” → 渲染的任务是“按参数精确产出”，没有语义判断需求；硬上 LLM 反而引入不稳定和成本。

---

**Q12. 参数化怎么保证“幂等”？重复执行会出问题吗？（幂等与可重入）**

**🎯 面试官想考什么**：考你对“可重复执行”这类工程素养的理解——方案演示可以跑一次，生产工具必须能跑一万次。

**🧭 答题框架（PREP）**
- **P**：幂等来自“确定性渲染 + 覆盖语义”两件事。
- **R**：同一模板同一参数渲染两次结果字节级一致；对已存在的产物直接覆盖而不是新增。
- **E**：Change 重新生成 = 覆盖旧内容、不创建新 ChangeId（PRD 平台 `/changes/{id}/regenerate`）；技能/规则重复安装不产生重复文件。
- **P**：所以重复执行是安全的——这是可被门禁和 CI 反复调用的前提。



<svg xmlns="http://www.w3.org/2000/svg" width="980" height="320" viewBox="0 0 980 320">
  <defs>
    <linearGradient id="bgq12" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arq12" markerWidth="9" markerHeight="9" refX="8" refY="4.5" orient="auto">
      <path d="M 0 0 L 9 4.5 L 0 9 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="980" height="320" fill="url(#bgq12)"/>
  <text x="490" y="42" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">图 12 · 幂等：确定性 + 覆盖语义</text>
  <text x="490" y="292" text-anchor="middle" fill="#475569" font-size="16">CI 反复执行、门禁反复调用，不制造重复产物</text>
<rect x="40" y="112" width="190" height="118" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="135.0" y="139" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">渲染确定性</text>
<text x="135.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">同输入同输出</text>
<line x1="230" y1="171.0" x2="276" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq12)"/>
<rect x="276" y="112" width="190" height="118" rx="10" fill="#8b5cf6" opacity="0.12" stroke="#a78bfa" stroke-width="1.6"/>
<text x="371.0" y="139" text-anchor="middle" fill="#c4b5fd" font-size="15" font-weight="700">覆盖语义</text>
<text x="371.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">覆盖而不是新建</text>
<text x="371.0" y="182" text-anchor="middle" fill="#94a3b8" font-size="11">ChangeId 不变</text>
<line x1="466" y1="171.0" x2="512" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq12)"/>
<rect x="512" y="112" width="190" height="118" rx="10" fill="#f59e0b" opacity="0.12" stroke="#fbbf24" stroke-width="1.6"/>
<text x="607.0" y="139" text-anchor="middle" fill="#fcd34d" font-size="15" font-weight="700">CI 反复执行</text>
<text x="607.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">门禁反复调用</text>
<line x1="702" y1="171.0" x2="748" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq12)"/>
<rect x="748" y="112" width="190" height="118" rx="10" fill="#10b981" opacity="0.12" stroke="#34d399" stroke-width="1.6"/>
<text x="843.0" y="139" text-anchor="middle" fill="#6ee7b7" font-size="15" font-weight="700">无重复产物</text>
<text x="843.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">多跑一次无副作用</text>
</svg>

**🗣️ 参考回答示范**：“幂等靠两件事保证：一是渲染本身确定性，同一输入重复渲染结果完全一致；二是覆盖语义，对已存在的产物是覆盖而不是新建。举个具体例子，PRD 平台里 Change 重新生成时，无论生成多少次，ChangeId 不变，新内容覆盖旧内容——它遵循的是‘幂等覆盖’而不是‘append’。这样整套流程可以被 CI 反复执行、被门禁反复调用，不会因为多跑一次就制造出一堆重复产物。”

**🌟 加分点**：说出“幂等覆盖”这个术语（覆盖、不创建新 ChangeId），并把“重新解析/重新生成”接口（`/reparse`、`/regenerate`）作为例证。

**🛡️ 追问防御链**：追问“如果用户改了产物又重跑，改动会丢吗？” → 覆盖语义是明确的——重跑覆盖的是“生成物”，用户手工改动要么走版本控制留痕，要么视为升级渲染的输入。

---

**Q13. 如果要新增一种全新语言（比如 Swift），完整接入流程是什么？（可扩展性）**

**🎯 面试官想考什么**：这是“脚手架能不能长大”的试金石——考你是否把接入路径设计成了“步骤清单”，而不是每次全靠手改。

**🧭 答题框架（PREP）**
- **P**：四步——① 检测表加语言段 ② 造语言包（frontmatter + 基线框架清单） ③ 参数表加“基础参数（通用）”块 ④ 渲染回归验收。
- **R**：每一步都对应“检测→参数→渲染→验证”链路里的一个环节，缺一环就会断链。
- **E**：造语言包时只写该语言特有的规则（编码规范/工程结构），SDD-TDD、开发流程、运行时可靠性从 harness-core 共享；回归验收跑一遍 apply-harness，确认无残留 `{{...}}`。
- **P**：这是一次性成本，之后加框架都走“5 行参数表”的轻量路径。



<svg xmlns="http://www.w3.org/2000/svg" width="980" height="320" viewBox="0 0 980 320">
  <defs>
    <linearGradient id="bgq13" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arq13" markerWidth="9" markerHeight="9" refX="8" refY="4.5" orient="auto">
      <path d="M 0 0 L 9 4.5 L 0 9 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="980" height="320" fill="url(#bgq13)"/>
  <text x="490" y="42" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">图 13 · 新增一种全新语言的四步</text>
  <text x="490" y="292" text-anchor="middle" fill="#475569" font-size="16">核心引擎一行代码都不用改</text>
<rect x="40" y="112" width="190" height="118" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="135.0" y="139" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">① 检测表加段</text>
<text x="135.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">Swift 特征文件映射</text>
<line x1="230" y1="171.0" x2="276" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq13)"/>
<rect x="276" y="112" width="190" height="118" rx="10" fill="#8b5cf6" opacity="0.12" stroke="#a78bfa" stroke-width="1.6"/>
<text x="371.0" y="139" text-anchor="middle" fill="#c4b5fd" font-size="15" font-weight="700">② 造语言包</text>
<text x="371.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">语言特有规范</text>
<text x="371.0" y="182" text-anchor="middle" fill="#94a3b8" font-size="11">通用规则继承</text>
<line x1="466" y1="171.0" x2="512" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq13)"/>
<rect x="512" y="112" width="190" height="118" rx="10" fill="#f59e0b" opacity="0.12" stroke="#fbbf24" stroke-width="1.6"/>
<text x="607.0" y="139" text-anchor="middle" fill="#fcd34d" font-size="15" font-weight="700">③ 填参数块</text>
<text x="607.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">基础参数块</text>
<text x="607.0" y="182" text-anchor="middle" fill="#94a3b8" font-size="11">{{BUILD_CMD}} 取值</text>
<line x1="702" y1="171.0" x2="748" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq13)"/>
<rect x="748" y="112" width="190" height="118" rx="10" fill="#10b981" opacity="0.12" stroke="#34d399" stroke-width="1.6"/>
<text x="843.0" y="139" text-anchor="middle" fill="#6ee7b7" font-size="15" font-weight="700">④ 回归验收</text>
<text x="843.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">零占位符残留</text>
<text x="843.0" y="182" text-anchor="middle" fill="#94a3b8" font-size="11">门禁命令可跑通</text>
</svg>

**🗣️ 参考回答示范**：“加一种全新语言分四步。第一步，在 apply-harness 的检测表里加一个 Swift 段，定义特征文件映射：`.xcodeproj`/`.swift` 对应什么框架、什么构建工具。第二步，造一个 `harness-swift` 语言包，里面是语言特有的编码规范和工程结构，而 SDD-TDD、开发流程、运行时可靠性这些通用规则直接从 harness-core 继承，不用重写。第三步，在参数表里加一个‘Swift 基础参数（通用）’块，把 `{{BUILD_CMD}}`、`{{TEST_CMD}}` 这些占位符的 Swift 取值填上。第四步，回归验收：跑一遍 apply-harness，检查渲染出来的产物里没有残留占位符、门禁命令能跑通。四步走完，整个流水线对 Swift 就生效了——核心引擎一行代码都不用改。”

**🌟 加分点**：强调“通用规则共享自 core、语言特有规则才放进语言包”——这是语言包设计的关键，否则每个语言包都会失控膨胀。

**🛡️ 追问防御链**：追问“能不能不造检测规则，先支持语言？” → 可以，但链路会断：检测不到语言就无法确定参数表，渲染就没有输入；所以四步的顺序不是随意的。

---

**Q14. 基础参数块和框架差异块冲突时，以谁为准？（参数优先级）**

**🎯 面试官想考什么**：考你是否真正理解“分层覆盖”的语义，而不是只会用模板。

**🧭 答题框架（PREP）**
- **P**：框架差异块优先——**最终参数 = 基础块全量 + 框架块覆盖差异**。
- **R**：这和配置中心的“按环境覆盖”、CSS 层叠是同一个思想：越具体越优先。
- **E**：所以在框架块里重复写基础块已有的参数是 bug——会被静默覆盖，等于白写。
- **P**：这条规则让“公共只维护一份”成为可能，否则差异块就得全量复制。



<svg xmlns="http://www.w3.org/2000/svg" width="980" height="320" viewBox="0 0 980 320">
  <defs>
    <linearGradient id="bgq14" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arq14" markerWidth="9" markerHeight="9" refX="8" refY="4.5" orient="auto">
      <path d="M 0 0 L 9 4.5 L 0 9 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="980" height="320" fill="url(#bgq14)"/>
  <text x="490" y="42" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">图 14 · 基础块 vs 框架块：谁优先</text>
  <text x="490" y="292" text-anchor="middle" fill="#475569" font-size="16">和配置中心按环境覆盖、CSS 层叠同一思想——越具体优先级越高</text>
<rect x="40" y="112" width="190" height="118" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="135.0" y="139" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">基础块全量</text>
<text x="135.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">全部生效</text>
<line x1="230" y1="171.0" x2="276" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq14)"/>
<rect x="276" y="112" width="190" height="118" rx="10" fill="#8b5cf6" opacity="0.12" stroke="#a78bfa" stroke-width="1.6"/>
<text x="371.0" y="139" text-anchor="middle" fill="#c4b5fd" font-size="15" font-weight="700">框架块覆盖差异</text>
<text x="371.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">只覆盖真正不同的</text>
<line x1="466" y1="171.0" x2="512" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq14)"/>
<rect x="512" y="112" width="190" height="118" rx="10" fill="#f59e0b" opacity="0.12" stroke="#fbbf24" stroke-width="1.6"/>
<text x="607.0" y="139" text-anchor="middle" fill="#fcd34d" font-size="15" font-weight="700">最终参数</text>
<text x="607.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">基础 + 框架覆盖</text>
<line x1="702" y1="171.0" x2="748" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq14)"/>
<rect x="748" y="112" width="190" height="118" rx="10" fill="#10b981" opacity="0.12" stroke="#34d399" stroke-width="1.6"/>
<text x="843.0" y="139" text-anchor="middle" fill="#6ee7b7" font-size="15" font-weight="700">隐含约定</text>
<text x="843.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">框架块不重复写基础块</text>
</svg>

**🗣️ 参考回答示范**：“框架差异块优先。合并规则就一条：最终参数 = 基础块全量 + 框架块覆盖差异。基础块里定义的参数全部生效，框架块只覆盖真正不同的字段。这和配置中心的按环境覆盖、CSS 的层叠都是同一个思想——越具体优先级越高。反过来还有一个隐含约定：框架块里不该重复写基础块的参数，因为会被覆盖，等于写了也没用，还可能造成‘改了基础块但框架块残留旧值’的坑。”

**🌟 加分点**：主动说出“重复写 = bug”，展示你不仅知道规则，还知道规则的边界和坑。

**🛡️ 追问防御链**：追问“怎么防止有人误在框架块重复写？” → 渲染时可以加校验：框架块与基础块同名的参数如果值相同，告警提示冗余；值不同才允许作为覆盖。

---

**Q15. apply-harness 装一个项目为什么是秒级的？（性能与开销）**

**🎯 面试官想考什么**：看你对系统开销有没有概念——是不是所有环节都理所当然地认为要“跑模型”。

**🧭 答题框架（PREP）**
- **P**：因为整个安装过程全是本地文件操作：扫描特征文件 → 读项目名 → 渲染模板 → 复制规则与技能 → 建变更目录。
- **R**：没有任何一步需要网络请求或模型推理，所以开销是毫秒到秒级，而不是分钟级。
- **E**：对比：PRD 平台的“AI 解析”才走 LLM（秒级到分钟级），且平台支持关闭 AI 以降级模式运行。
- **P**：把性能问题转化为“哪些环节该用 LLM”的架构问题——能用确定性的绝不用 LLM。



<svg xmlns="http://www.w3.org/2000/svg" width="980" height="320" viewBox="0 0 980 320">
  <defs>
    <linearGradient id="bgq15" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arq15" markerWidth="9" markerHeight="9" refX="8" refY="4.5" orient="auto">
      <path d="M 0 0 L 9 4.5 L 0 9 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="980" height="320" fill="url(#bgq15)"/>
  <text x="490" y="42" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">图 15 · 为什么安装是秒级：全程本地文件操作</text>
  <text x="490" y="292" text-anchor="middle" fill="#475569" font-size="16">没有一步需要网络请求或模型推理</text>
<rect x="40" y="112" width="143" height="118" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="111.5" y="139" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">扫描特征文件</text>
<text x="111.5" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">识别语言框架</text>
<line x1="183" y1="171.0" x2="229" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq15)"/>
<rect x="229" y="112" width="143" height="118" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="300.5" y="139" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">读项目名</text>
<text x="300.5" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">渲染 owner.md</text>
<line x1="372" y1="171.0" x2="418" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq15)"/>
<rect x="418" y="112" width="143" height="118" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="489.5" y="139" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">复制规则</text>
<text x="489.5" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">rules/ 注入</text>
<line x1="561" y1="171.0" x2="607" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq15)"/>
<rect x="607" y="112" width="143" height="118" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="678.5" y="139" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">渲染技能模板</text>
<text x="678.5" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">占位符替换</text>
<line x1="750" y1="171.0" x2="796" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq15)"/>
<rect x="796" y="112" width="143" height="118" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="867.5" y="139" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">建变更目录</text>
<text x="867.5" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">.harness/changes/</text>
</svg>

**🗣️ 参考回答示范**：“因为安装过程从头到尾都是本地文件操作：扫描特征文件、读项目名、渲染 owner.md、复制规则、渲染技能模板、建变更目录——没有一步需要网络请求或模型推理，所以是秒级的。设计上我有个原则：**能确定性解决的绝不用 LLM**。AI 很贵、很慢、还不稳定，它只应该出现在真正需要理解语义的地方，比如 PRD 解析、代码评审；模板渲染、检测这类有明确规则的环节，用确定性算法又快又稳。”

**🌟 加分点**：引出平台的“可开关组件”设计——AI 和 MinIO 可通过配置关闭，系统降级模式照常运行，证明“LLM 是可选件”不是口号。

**🛡️ 追问防御链**：追问“如果检测要覆盖 120+ 框架，扫描会变慢吗？” → 不会，检测是查文件特征+读依赖清单，复杂度与文件数线性相关，与框架数量无关；真正的开销在渲染产物体积，与项目规模无关。

---

### 4.3 方向三：自动检测引擎（6 题）

**Q16. 自动检测引擎是怎么识别项目语言和框架的？（⚡最亮机制 · 必考 · 完整展开）**

**🎯 面试官想考什么**：检测是“一键安装”体验的起点，也是最容易翻车的环节——面试官会追问“识别错了怎么办”“多语言项目怎么办”，考你的兜底设计。

**🧭 答题框架（PREP）**
- **P**：两级检测 + 特征表匹配：**先识别语言，再从语言特征中识别具体框架和构建工具**，按优先级扫描项目根目录。
- **R**：为什么两级？因为“语言”决定规则和技能体系，“框架”决定命令和参数差异——一步到位容易误判，分开判更稳。
- **E**：Java 检测表发现 `pom.xml` + spring-boot-starter-web 依赖 → Spring Boot + Maven；发现 `pom.xml` 但没匹配到任何框架 → 询问用户。每张表最后一行都是“询问用户”兑底。
- **P**：检测不是“猜”——**识别不出来就停下来问，绝不静默猜**，这是整套参数化能跑对的前提。



<svg xmlns="http://www.w3.org/2000/svg" width="980" height="450" viewBox="0 0 980 458.0">
  <defs>
    <linearGradient id="bgq16" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arq16" markerWidth="9" markerHeight="9" refX="8" refY="4.5" orient="auto">
      <path d="M 0 0 L 9 4.5 L 0 9 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="980" height="458.0" fill="url(#bgq16)"/>
  <text x="490" y="42" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">图 16 · 自动检测：两级识别 + 匹配分支 + 询问兜底</text>
  <text x="490" y="440" text-anchor="middle" fill="#475569" font-size="16">检测是整条链路的第一块多米诺：识别错了，参数、渲染、安装全部白装</text>
  <rect x="40" y="94" width="280" height="112" rx="10" fill="#475569" opacity="0.12" stroke="#64748b" stroke-width="1.6"/>
  <text x="180" y="121" text-anchor="middle" fill="#cbd5e1" font-size="15" font-weight="700">扫描根目录</text>
  <text x="180" y="146" text-anchor="middle" fill="#94a3b8" font-size="11">pom.xml / go.mod</text>
  <text x="180" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">package.json</text>
  <line x1="320" y1="150" x2="350" y2="150" stroke="#475569" stroke-width="3" marker-end="url(#arq16)"/>
  <rect x="350" y="94" width="280" height="112" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
  <text x="490" y="121" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">识别语言</text>
  <text x="490" y="146" text-anchor="middle" fill="#94a3b8" font-size="11">按特征优先级判定</text>
  <line x1="630" y1="150" x2="660" y2="150" stroke="#475569" stroke-width="3" marker-end="url(#arq16)"/>
  <rect x="660" y="94" width="280" height="112" rx="10" fill="#8b5cf6" opacity="0.12" stroke="#a78bfa" stroke-width="1.6"/>
  <text x="800" y="121" text-anchor="middle" fill="#c4b5fd" font-size="15" font-weight="700">识别框架</text>
  <text x="800" y="146" text-anchor="middle" fill="#94a3b8" font-size="11">120+ 框架特征表</text>
  <text x="800" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">依赖内容叠加判断</text>
  <line x1="800" y1="206" x2="800" y2="242" stroke="#475569" stroke-width="3" marker-end="url(#arq16)"/>
  <rect x="660" y="242" width="280" height="50" rx="10" fill="#f59e0b" opacity="0.12" stroke="#fbbf24" stroke-width="1.6"/>
  <text x="800" y="274" text-anchor="middle" fill="#fcd34d" font-size="15" font-weight="700">匹配？</text>
  <line x1="800" y1="292" x2="726" y2="330" stroke="#10b981" stroke-width="3" marker-end="url(#arq16)"/>
  <text x="785" y="318" text-anchor="middle" fill="#6ee7b7" font-size="12" font-weight="700">匹配</text>
  <path d="M 800 292 V 322 H 253 V 330" fill="none" stroke="#f87171" stroke-width="3" marker-end="url(#arq16)"/>
  <text x="526" y="315" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">未匹配</text>
  <rect x="40" y="330" width="427" height="90" rx="10" fill="#f59e0b" opacity="0.12" stroke="#fbbf24" stroke-width="1.6"/>
  <text x="253" y="357" text-anchor="middle" fill="#fcd34d" font-size="15" font-weight="700">询问用户：手动指定</text>
  <text x="253" y="382" text-anchor="middle" fill="#94a3b8" font-size="11">绝不静默猜</text>
  <line x1="467" y1="375" x2="513" y2="375" stroke="#475569" stroke-width="3" marker-end="url(#arq16)"/>
  <rect x="513" y="330" width="427" height="90" rx="10" fill="#10b981" opacity="0.12" stroke="#34d399" stroke-width="1.6"/>
  <text x="726" y="357" text-anchor="middle" fill="#6ee7b7" font-size="15" font-weight="700">输出</text>
  <text x="726" y="382" text-anchor="middle" fill="#94a3b8" font-size="11">语言 + 框架 + 构建工具</text>
</svg>


**🗣️ 参考回答示范**：“检测引擎是两级结构。第一步先识别语言：扫描项目根目录的特征文件，比如 `pom.xml`、`go.mod`、`Cargo.toml`、`package.json`、`pyproject.toml`，按优先级判定语言。第二步在语言内识别框架和构建工具：每种语言维护一张检测特征表，比如 Java 表里，`pom.xml` 带 spring-boot-starter-web 依赖判为 Spring Boot + Maven；`package.json` 里能看到 Vue/React/Next.js 等前端框架。关键设计是**兑底**：每张表最后一行都是‘未匹配 → 询问用户’，比如发现 `pom.xml` 但一个已知框架都对不上，就停下来让用户手动指定，而不是猜一个装上去。全部框架加起来支持 80+ 种主流框架。这样设计是因为检测结果决定后面整套参数渲染，识别错了全部白装，所以宁可多问一次，也不能静默猜。”

**🌟 加分点**：
- 点出“检测是参数化的前置”——检测结果直接决定选哪组参数表（基础块+框架块），链路是“检测 → 参数 → 渲染”。
- 说出“先语言后框架”的两级顺序本身就是降低误判的手段。

**🛡️ 追问防御链**：
- **追问“识别错了会发生什么？”** → 比识别不出更糟：参数表选错，渲染出一套错误命令的技能，装完跑不通。所以设计上“不确定就询问”，把错误拦截在源头。
- **追问“版本信息从哪来？”** → 从依赖清单读实际版本，比如 `Cargo.toml` 的依赖版本、`pom.xml` 的 Java 版本，不猜不写死。

---

**Q17. 怎么避免检测引擎识别错误？（防误判设计）**

**🎯 面试官想考什么**：面试官想听的不是“我们测过没问题”，而是你在系统层面做了什么来限制错误的可能性。

**🧭 答题框架（PREP）**
- **P**：三层防线——① 两级检测，先语言后框架，缩小判定范围；② 特征文件 + 依赖内容双重校验；③ 未匹配/多语言一律询问用户。
- **R**：检测错误的代价是整套参数化渲染跑偏，源头必须最保守。
- **E**：每张检测表最后一行都是“询问用户”；多语言项目会停下来让用户选焦点语言。
- **P**：结论——**识别精度靠“宁可问、不硬猜”兑底，而不是靠更复杂的猜测算法**。



<svg xmlns="http://www.w3.org/2000/svg" width="980" height="320" viewBox="0 0 980 320">
  <defs>
    <linearGradient id="bgq17" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arq17" markerWidth="9" markerHeight="9" refX="8" refY="4.5" orient="auto">
      <path d="M 0 0 L 9 4.5 L 0 9 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="980" height="320" fill="url(#bgq17)"/>
  <text x="490" y="42" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">图 17 · 避免检测误判：三个层面</text>
  <text x="490" y="292" text-anchor="middle" fill="#475569" font-size="16">识别精度靠"不确定就不猜"这个纪律保住的</text>
<rect x="40" y="112" width="190" height="118" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="135.0" y="139" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">两级检测</text>
<text x="135.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">先语言后框架</text>
<text x="135.0" y="182" text-anchor="middle" fill="#94a3b8" font-size="11">判定范围小</text>
<line x1="230" y1="171.0" x2="276" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq17)"/>
<rect x="276" y="112" width="190" height="118" rx="10" fill="#8b5cf6" opacity="0.12" stroke="#a78bfa" stroke-width="1.6"/>
<text x="371.0" y="139" text-anchor="middle" fill="#c4b5fd" font-size="15" font-weight="700">叠加依赖内容</text>
<text x="371.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">package.json 只是入口</text>
<text x="371.0" y="182" text-anchor="middle" fill="#94a3b8" font-size="11">看依赖里是 Vue 还是 React</text>
<line x1="466" y1="171.0" x2="512" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq17)"/>
<rect x="512" y="112" width="190" height="118" rx="10" fill="#f59e0b" opacity="0.12" stroke="#fbbf24" stroke-width="1.6"/>
<text x="607.0" y="139" text-anchor="middle" fill="#fcd34d" font-size="15" font-weight="700">询问兜底</text>
<text x="607.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">不确定就问</text>
<text x="607.0" y="182" text-anchor="middle" fill="#94a3b8" font-size="11">多语言也问</text>
<line x1="702" y1="171.0" x2="748" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq17)"/>
<rect x="748" y="112" width="190" height="118" rx="10" fill="#10b981" opacity="0.12" stroke="#34d399" stroke-width="1.6"/>
<text x="843.0" y="139" text-anchor="middle" fill="#6ee7b7" font-size="15" font-weight="700">精度</text>
<text x="843.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">不靠更复杂算法</text>
<text x="843.0" y="182" text-anchor="middle" fill="#94a3b8" font-size="11">靠不猜</text>
</svg>

**🗣️ 参考回答示范**：“避免误判我从三个层面下手。第一层，检测本身分两级：先识别语言，再从语言特征里识别框架，每一步判定范围都很小，不容易混。第二层，判定不只靠文件名，还叠加依赖内容——`package.json` 只是前端项目的入口，具体是 Vue 还是 React 要看依赖里有什么。第三层，也是最重要的兑底：**所有不确定的路径都停下来询问用户**——语言文件存在但框架对不上，问；一个项目同时有前端和后端，问；完全识别不了，问。识别精度不是靠更复杂的算法猜出来的，是靠‘不确定就不猜’这个纪律保住的。”

**🌟 加分点**：点出“询问”不是体验退步，而是参数化系统唯一正确的兑底——错误的参数比没有参数更危险。

**🛡️ 追问防御链**：追问“询问用户会不会显得工具不智能？” → 恰恰相反：确定性系统里“诚实承认不知道”比“自信地错”更可靠，这也是可测试性的前提。

---

**Q18. 一个项目同时有前端和后端，检测引擎怎么办？（多语言项目）**

**🎯 面试官想考什么**：真实项目大多是前后端混在一个仓库，考你是否考虑过真实世界的复杂性。

**🧭 答题框架（PREP）**
- **P**：检测到多语言时**不静默选一个**，列出候选让用户指定当前焦点语言。
- **R**：因为一套 `.harness/` 是一次初始化，必须对准一个焦点语言——选错会让规则、命令全部错位。
- **E**：检测表设计上就支持“多语言并存 → 询问用户”，把选择权交回人。
- **P**：多语言处理原则：**一次只服务一个焦点语言，剩下的交给用户决策。**



<svg xmlns="http://www.w3.org/2000/svg" width="980" height="320" viewBox="0 0 980 320">
  <defs>
    <linearGradient id="bgq18" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arq18" markerWidth="9" markerHeight="9" refX="8" refY="4.5" orient="auto">
      <path d="M 0 0 L 9 4.5 L 0 9 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="980" height="320" fill="url(#bgq18)"/>
  <text x="490" y="42" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">图 18 · 前后端混仓：把选择权交回用户</text>
  <text x="490" y="292" text-anchor="middle" fill="#475569" font-size="16">一次询问换整套体系的正确性</text>
<rect x="40" y="112" width="190" height="118" rx="10" fill="#475569" opacity="0.12" stroke="#64748b" stroke-width="1.6"/>
<text x="135.0" y="139" text-anchor="middle" fill="#cbd5e1" font-size="15" font-weight="700">检测到多语言</text>
<text x="135.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">前端+后端同时存在</text>
<line x1="230" y1="171.0" x2="276" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq18)"/>
<rect x="276" y="112" width="190" height="118" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="371.0" y="139" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">不静默选一个</text>
<text x="371.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">列出所有候选</text>
<line x1="466" y1="171.0" x2="512" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq18)"/>
<rect x="512" y="112" width="190" height="118" rx="10" fill="#8b5cf6" opacity="0.12" stroke="#a78bfa" stroke-width="1.6"/>
<text x="607.0" y="139" text-anchor="middle" fill="#c4b5fd" font-size="15" font-weight="700">用户指定焦点语言</text>
<text x="607.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">生成对应一套 .harness</text>
<line x1="702" y1="171.0" x2="748" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq18)"/>
<rect x="748" y="112" width="190" height="118" rx="10" fill="#10b981" opacity="0.12" stroke="#34d399" stroke-width="1.6"/>
<text x="843.0" y="139" text-anchor="middle" fill="#6ee7b7" font-size="15" font-weight="700">装对</text>
<text x="843.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">规则/技能/命令全对</text>
</svg>

**🗣️ 参考回答示范**：“前后端混仓是我设计检测时第一件考虑到的真实场景。做法是：同时检测到多种语言时，工具不静默选一个，而是把检测到的候选列出来，让用户指定当前要聚焦哪个语言。原因很直接：一次初始化只生成一套 `.harness/`，规则、技能、命令全都围绕一个焦点语言展开，如果我替你赌一个，很可能赌错——前端项目装了后端规则，命令全跑不通。把选择权交回用户，一次询问换整套体系的正确性，这笔交易很划算。”

**🌟 加分点**：提到“焦点语言”这个概念，并说明“一次只服务一个焦点”是刻意约束，而不是能力不足。

**🛡️ 追问防御链**：追问“同一个仓库想同时管前后端怎么办？” → 当前设计一次初始化聚焦一种语言；两种语言都要管，可以分别在子目录初始化两套 `.harness/`，因为 `.harness/` 是目录级隔离的。

---

**Q19. 检测为什么宁可多问用户，也不静默猜？（质量权衡）**

**🎯 面试官想考什么**：考你的“调性判断”——很多工程师认为“识别率高=牛”，但这道题考你是否理解错误代价的不对称性。

**🧭 答题框架（PREP）**
- **P**：因为“识别错”和“识别不出”的代价严重不对称：识别错 → 后续全部渲染跟着错；识别不出 → 只是一次询问。
- **R**：检测结果是确定性的源头，错一次全盘皆错，而询问的代价只是一次交互。
- **E**：所以设计上用“0 误判 + 询问兑底”代替“高识别率 + 静默猜测”。
- **P**：结论——**在低成本高后果的环节，问；在高成本低后果的环节，才考虑自动化。**



<svg xmlns="http://www.w3.org/2000/svg" width="980" height="320" viewBox="0 0 980 320">
  <defs>
    <linearGradient id="bgq19" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arq19" markerWidth="9" markerHeight="9" refX="8" refY="4.5" orient="auto">
      <path d="M 0 0 L 9 4.5 L 0 9 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="980" height="320" fill="url(#bgq19)"/>
  <text x="490" y="42" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">图 19 · 为什么宁可多问：错猜代价 &gt;&gt; 多问代价</text>
  <text x="490" y="292" text-anchor="middle" fill="#475569" font-size="16">错误沿 检测→参数→渲染→执行 链路放大，源头绝对保守</text>
<rect x="40" y="112" width="427" height="118" rx="10" fill="#ef4444" opacity="0.12" stroke="#f87171" stroke-width="1.6"/>
<text x="253.5" y="139" text-anchor="middle" fill="#fca5a5" font-size="15" font-weight="700">错猜代价</text>
<text x="253.5" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">一次摧毁后续所有环节</text>
<text x="253.5" y="182" text-anchor="middle" fill="#94a3b8" font-size="11">用户装错还得重来</text>
<line x1="467" y1="171.0" x2="513" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq19)"/>
<rect x="513" y="112" width="427" height="118" rx="10" fill="#10b981" opacity="0.12" stroke="#34d399" stroke-width="1.6"/>
<text x="726.5" y="139" text-anchor="middle" fill="#6ee7b7" font-size="15" font-weight="700">多问代价</text>
<text x="726.5" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">用户花几秒点一下</text>
<text x="726.5" y="182" text-anchor="middle" fill="#94a3b8" font-size="11">换取整套体系正确</text>
</svg>

**🗣️ 参考回答示范**：“我算过一笔账：识别错的代价是一次性摧毁后面所有环节——参数表选错、模板渲染错、技能命令错，用户装完才发现全错，重新装等于重来；而识别不出、多问一句的代价，只是用户花几秒点一下。一笔账算下来，结论非常清楚：**宁可多问，不可错猜**。这不是偷懒，是对错误代价的不对称性做了权衡——检测环节的错误会沿着‘检测 → 参数 → 渲染 → 执行’链路放大，所以在链路源头，我选择绝对保守。”

**🌟 加分点**：主动说出“错误沿链路放大”这个系统性判断——面试官会记住你思考问题的方式，而不是具体数字。

**🛡️ 追问防御链**：追问“那识别率指标还重要吗？” → 重要，但指标要定义对：我们优化的是“确定识别”（有把握才下结论）的比例，而不是“总识别率”——把不确定的果断交给询问，两者不冲突。

---

**Q20. 检测引擎怎么测试和验收？（质量保障）**

**🎯 面试官想考什么**：考你有没有把“检测”当成可测试的软件来对待，而不是一把梭的脚本。

**🧭 答题框架（PREP）**
- **P**：用“特征样例回归 + 全链路验收”两条腿。
- **R**：检测表是规则集合，规则必须可被样例回归——每个语言每个框架一族样例，跑一遍检测断言结果。
- **E**：每新增一个框架，进参数表的同时进样例库；验收跑一遍 apply-harness，确认渲染产物**零残留占位符**、门禁命令可执行。
- **P**：验收标准必须是机器可判定的，否则“支持了”等于“没支持”。



<svg xmlns="http://www.w3.org/2000/svg" width="980" height="420" viewBox="0 0 980 482.0">
  <defs>
    <linearGradient id="bgq20" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arq20" markerWidth="9" markerHeight="9" refX="8" refY="4.5" orient="auto">
      <path d="M 0 0 L 9 4.5 L 0 9 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="980" height="482.0" fill="url(#bgq20)"/>
  <text x="490" y="42" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">图 20 · 检测引擎怎么测试验收：两层</text>
  <text x="490" y="464" text-anchor="middle" fill="#475569" font-size="16">两层测试都挂在 CI 里：改检测表之后，不会悄悄破坏其它语言的识别</text>
<rect x="40" y="104" width="427" height="128" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="253.5" y="131" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">规则级回归</text>
<text x="253.5" y="156" text-anchor="middle" fill="#94a3b8" font-size="11">每种语言/框架特征样例</text>
<text x="253.5" y="174" text-anchor="middle" fill="#94a3b8" font-size="11">断言 语言+框架 识别正确</text>
<line x1="467" y1="168.0" x2="513" y2="168.0" stroke="#475569" stroke-width="3" marker-end="url(#arq20)"/>
<rect x="513" y="104" width="427" height="128" rx="10" fill="#8b5cf6" opacity="0.12" stroke="#a78bfa" stroke-width="1.6"/>
<text x="726.5" y="131" text-anchor="middle" fill="#c4b5fd" font-size="15" font-weight="700">全链路验收</text>
<text x="726.5" y="156" text-anchor="middle" fill="#94a3b8" font-size="11">apply-harness 真跑一遍</text>
<text x="726.5" y="174" text-anchor="middle" fill="#94a3b8" font-size="11">拿真实项目验</text>
<rect x="40" y="316" width="900" height="128" rx="10" fill="#10b981" opacity="0.12" stroke="#34d399" stroke-width="1.6"/>
<text x="490.0" y="343" text-anchor="middle" fill="#6ee7b7" font-size="15" font-weight="700">三项检查</text>
<text x="490.0" y="368" text-anchor="middle" fill="#94a3b8" font-size="11">零占位符残留</text>
<text x="490.0" y="386" text-anchor="middle" fill="#94a3b8" font-size="11">参数落到命令</text>
<text x="490.0" y="404" text-anchor="middle" fill="#94a3b8" font-size="11">变更目录创建</text>
<line x1="253.5" y1="232" x2="340.0" y2="316" stroke="#475569" stroke-width="3" marker-end="url(#arq20)"/>
<line x1="726.5" y1="232" x2="640.0" y2="316" stroke="#475569" stroke-width="3" marker-end="url(#arq20)"/>
</svg>

**🗣️ 参考回答示范**：“检测引擎的测试分两层。第一层是规则级回归：为每种语言、每个框架维护一组特征样例，比如目录里放一个伪造的 `go.mod` + Gin 依赖样例，跑检测，断言输出必须是 Go + Gin，不得误判。第二层是全链路验收：新增一个语言或框架后，真实跑一遍 apply-harness，检查三件事——渲染产物里 `{{占位符}}` 残留必须为零；语言/框架参数确实落到了技能命令里；变更追踪目录正常创建。验收标准全部机器可判定，因为‘支持了某框架’如果不落到可验证的产出上，就等于没支持。”

**🌟 加分点**：把“无残留占位符”再次作为验收红线（与参数化呼应），说明检测与渲染是一条链路上的两个环节，共同验收。

**🛡️ 追问防御链**：追问“样例库会不会越维护越重？” → 每条样例就是“特征文件 + 期望结果”两行，成本极低；而且它同时是文档——新框架怎么识别，看样例就知道。

---

**Q21. 检测引擎和参数化是什么关系？（机制串联）**

**🎯 面试官想考什么**：考你能不能把两个机制串成一条链路讲清楚——面试官最怕听到“每个模块都会，连起来不会”。

**🧭 答题框架（PREP）**
- **P**：检测是参数化的**前置输入**：检测结果 → 决定语言/框架 → 从参数表选出“基础块 + 框架块” → 渲染。
- **R**：没有检测，参数化就缺输入，只能手动选模板（第一版就是这样，选错就全错）；没有参数化，检测结果无处落地。
- **E**：检测出 Go + Gin → 选 Go 基础参数块 + Gin 差异块 → owner.md 和技能模板出炉。
- **P**：两者合起来才是“一键装对”——检测负责对，参数化负责省。



<svg xmlns="http://www.w3.org/2000/svg" width="980" height="320" viewBox="0 0 980 320">
  <defs>
    <linearGradient id="bgq21" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arq21" markerWidth="9" markerHeight="9" refX="8" refY="4.5" orient="auto">
      <path d="M 0 0 L 9 4.5 L 0 9 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="980" height="320" fill="url(#bgq21)"/>
  <text x="490" y="42" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">图 21 · 检测引擎和参数化是什么关系：串联</text>
  <text x="490" y="292" text-anchor="middle" fill="#475569" font-size="16">检测负责"对"，参数化负责"省"，合起来才是一键安装</text>
<rect x="40" y="112" width="190" height="118" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="135.0" y="139" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">检测引擎</text>
<text x="135.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">这个项目是什么</text>
<text x="135.0" y="182" text-anchor="middle" fill="#94a3b8" font-size="11">语言/框架/构建</text>
<line x1="230" y1="171.0" x2="276" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq21)"/>
<rect x="276" y="112" width="190" height="118" rx="10" fill="#8b5cf6" opacity="0.12" stroke="#a78bfa" stroke-width="1.6"/>
<text x="371.0" y="139" text-anchor="middle" fill="#c4b5fd" font-size="15" font-weight="700">输出参数值</text>
<text x="371.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">52 个占位符填值</text>
<line x1="466" y1="171.0" x2="512" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq21)"/>
<rect x="512" y="112" width="190" height="118" rx="10" fill="#10b981" opacity="0.12" stroke="#34d399" stroke-width="1.6"/>
<text x="607.0" y="139" text-anchor="middle" fill="#6ee7b7" font-size="15" font-weight="700">参数化渲染</text>
<text x="607.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">owner.md · 技能模板</text>
<text x="607.0" y="182" text-anchor="middle" fill="#94a3b8" font-size="11">规则文件</text>
<line x1="702" y1="171.0" x2="748" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq21)"/>
<rect x="748" y="112" width="190" height="118" rx="10" fill="#f59e0b" opacity="0.12" stroke="#fbbf24" stroke-width="1.6"/>
<text x="843.0" y="139" text-anchor="middle" fill="#fcd34d" font-size="15" font-weight="700">一键安装</text>
<text x="843.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">秒级 · 无残留</text>
</svg>

**🗣️ 参考回答示范**：“检测和参数化是一条链路的上下两端。检测负责回答‘这个项目是什么’——语言、框架、构建工具；参数化负责回答‘怎么把这个语言的项目装配出来’——把 52 个占位符填上对应语言的值。流程上：检测结果先定语言和框架，然后从参数表里选出‘基础参数（通用）块 + 框架差异块’，合并后就是渲染参数，owner.md、技能模板全部由它渲染出来。没有检测，参数化就没有输入，只能手动选模板，这也是我第一版的做法，经常选错；没有参数化，检测结果也无处落地。所以它俩必须成对出现——**检测负责‘对’，参数化负责‘省’**，合起来才是真正的一键安装。”

**🌟 加分点**：用“手动选模板经常选错”的第一版经历做对比，展示演进思维——这类“从 V1 到 V2 的 why”是面试官最爱听的故事。

**🛡️ 追问防御链**：追问“如果检测说 Go、参数化了 Java，会怎样？” → 不会发生：框架差异块和语言基础块是一体的、以检测结果为准；真出现不一致（比如用户改了参数表），渲染验收的零残留占位符检查也会优先暴露。

---

### 4.4 方向四：软闸门与存量迁移（7 题）

**Q22. 软闸门（Soft Gate）是什么？为什么不用硬门禁一步到位？（⚡最亮设计 · 必考 · 完整展开）**

**🎯 面试官想考什么**：这是最能拉开差距的一道题——它考的不是技术，而是你对“约束条件建模”的理解：理想方案 vs 现实方案为什么不同。

**🧭 答题框架（PREP）**
- **P**：软闸门 = **存量违规允许存在但只减不增，新增代码 0 violation（硬约束）**，用 baseline 做比对闸门。
- **R**：硬门禁一步清零的代价：老项目几千条存量违规，一条条改完才能继续开发，业务停摆几周，团队必然抵制；软闸门把“存量债”和“新增债”分开治理。
- **E**：摸底产出 baseline.md（存量违规清单/热力图/热点模块）；套壳期 CI 只拦“新增违规”——lint 跑完与 baseline 比对，新增 0 条才放行，存量只减不增。
- **P**：所以软闸门不是妥协，而是对约束条件的正确建模：**时间、风险、业务三者平衡**——禁增量、化存量。



<svg xmlns="http://www.w3.org/2000/svg" width="980" height="420" viewBox="0 0 980 482.0">
  <defs>
    <linearGradient id="bgq22" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arq22" markerWidth="9" markerHeight="9" refX="8" refY="4.5" orient="auto">
      <path d="M 0 0 L 9 4.5 L 0 9 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="980" height="482.0" fill="url(#bgq22)"/>
  <text x="490" y="42" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">图 22 · 软闸门：存量只减不增，新增强制零违规</text>
  <text x="490" y="464" text-anchor="middle" fill="#475569" font-size="16">约束先于改造生效：先立规则，存量债在正常开发里顺带消化掉</text>
<rect x="40" y="104" width="427" height="128" rx="10" fill="#f59e0b" opacity="0.12" stroke="#fbbf24" stroke-width="1.6"/>
<text x="253.5" y="131" text-anchor="middle" fill="#fcd34d" font-size="15" font-weight="700">存量债 baseline.md</text>
<text x="253.5" y="156" text-anchor="middle" fill="#94a3b8" font-size="11">允许存在 · 只减不增</text>
<text x="253.5" y="174" text-anchor="middle" fill="#94a3b8" font-size="11">lint 1,234 条 · Top10 文件</text>
<line x1="467" y1="168.0" x2="513" y2="168.0" stroke="#475569" stroke-width="3" marker-end="url(#arq22)"/>
<rect x="513" y="104" width="427" height="128" rx="10" fill="#10b981" opacity="0.12" stroke="#34d399" stroke-width="1.6"/>
<text x="726.5" y="131" text-anchor="middle" fill="#6ee7b7" font-size="15" font-weight="700">新增代码</text>
<text x="726.5" y="156" text-anchor="middle" fill="#94a3b8" font-size="11">强制 0 违规</text>
<text x="726.5" y="174" text-anchor="middle" fill="#94a3b8" font-size="11">不新增机械债</text>
<rect x="40" y="316" width="427" height="128" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="253.5" y="343" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">CI 增量闸门</text>
<text x="253.5" y="368" text-anchor="middle" fill="#94a3b8" font-size="11">lint vs baseline 比对</text>
<text x="253.5" y="386" text-anchor="middle" fill="#94a3b8" font-size="11">新增=0 · 存量只降</text>
<line x1="467" y1="380.0" x2="513" y2="380.0" stroke="#475569" stroke-width="3" marker-end="url(#arq22)"/>
<rect x="513" y="316" width="427" height="128" rx="10" fill="#8b5cf6" opacity="0.12" stroke="#a78bfa" stroke-width="1.6"/>
<text x="726.5" y="343" text-anchor="middle" fill="#c4b5fd" font-size="15" font-weight="700">收敛</text>
<text x="726.5" y="368" text-anchor="middle" fill="#94a3b8" font-size="11">热点模块优先 · 逐批消化</text>
<text x="726.5" y="386" text-anchor="middle" fill="#94a3b8" font-size="11">质量曲线只升不降</text>
<path d="M 253.5 232 V 270 H 182.3 V 316" fill="none" stroke="#475569" stroke-width="3" marker-end="url(#arq22)"/>
<path d="M 726.5 232 V 292 H 324.7 V 316" fill="none" stroke="#475569" stroke-width="3" marker-end="url(#arq22)"/>
</svg>

**🗣️ 参考回答示范**：“软闸门解决的是棕地项目引入质量体系时的核心矛盾：存量违规几千条，硬门禁一步清零，团队整个月只能改债不能做业务，必然反弹。我的设计是分两个作用域：**存量允许存在、只减不增；新增强制零违规**。落地三步：第一步摸底，用 arch-review 扫描全库，产出 baseline.md——存量违规清单、违规最多的 10 个文件热力图、近 90 天变更最频繁的热点模块；第二步套壳，apply-harness 注入约束体系但不改业务代码，CI 里加一条‘增量闸门’：lint 结果和 baseline 比对，**新增违规数必须为 0**，存量违规数只允许下降不允许上升；第三步收敛，存量债在正常开发中逐批消化。全程业务不停摆，质量曲线只升不降。所以软闸门不是对质量的妥协，而是对约束条件的正确建模——一次性清债要支付‘停摆’的代价，软闸门把这笔代价摊平了。”

**🌟 加分点**：
- 说出“基线报告 = 迁移的合同”——摸底报告里的数字就是团队和管理层都认账的基线，后面所有验收都以它为锚。
- 强调“存量只减不增”是用数据管理存量债（每个月 lint 总数必须下降），而不是“有空再改”。

**🛡️ 追问防御链**：
- **追问“存量一直不清怎么办？”** → 软闸门不是放任：基线总违规数被监控，只允许下降；再配合收敛阶段的目标（比如 lint 违规数从 1,234 降到 200 以下），存量债是排期消化而不是无限期挂账。
- **追问“新增 0 violation 怎么保证不误伤？”** → 闸门比对的是“本次变更新增的违规”而不是全量结果——只有你这次提交引入的违规才被拦，历史违规不背锅，所以不会误伤开发。

---

**Q23. 迁移分哪三个阶段？为什么是这个顺序？（迁移节奏）**

**🎯 面试官想考什么**：考你有没有“先搞清楚再动手”的工程直觉——很多改造死在“没摸清现状就开干”。

**🧭 答题框架（PREP）**
- **P**：三阶段 = **摸底 → 套壳 → 收敛**。
- **R**：顺序背后的逻辑：先有数据（摸底）、再有约束（套壳）、最后才谈降解（收敛）——没有基线无法证明改善。
- **E**：摸底产出基线报告；套壳只注入规则不改代码；收敛按基线目标逐月消化存量。
- **P**：三阶段的划分依据是“每个阶段有独立产出物和验收锚点”，不是拍脑袋分期。



<svg xmlns="http://www.w3.org/2000/svg" width="980" height="320" viewBox="0 0 980 320">
  <defs>
    <linearGradient id="bgq23" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arq23" markerWidth="9" markerHeight="9" refX="8" refY="4.5" orient="auto">
      <path d="M 0 0 L 9 4.5 L 0 9 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="980" height="320" fill="url(#bgq23)"/>
  <text x="490" y="42" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">图 23 · 迁移三阶段：摸底 → 套壳 → 收敛</text>
  <text x="490" y="292" text-anchor="middle" fill="#475569" font-size="16">顺序不能换：先有数据，再有约束，最后降债</text>
<rect x="40" y="112" width="269" height="118" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="174.5" y="139" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">摸底</text>
<text x="174.5" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">量化存量债</text>
<text x="174.5" y="182" text-anchor="middle" fill="#94a3b8" font-size="11">产出 baseline</text>
<line x1="309" y1="171.0" x2="355" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq23)"/>
<rect x="355" y="112" width="269" height="118" rx="10" fill="#8b5cf6" opacity="0.12" stroke="#a78bfa" stroke-width="1.6"/>
<text x="489.5" y="139" text-anchor="middle" fill="#c4b5fd" font-size="15" font-weight="700">套壳</text>
<text x="489.5" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">注入规则/技能/CI</text>
<text x="489.5" y="182" text-anchor="middle" fill="#94a3b8" font-size="11">业务代码一行不动</text>
<line x1="624" y1="171.0" x2="670" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq23)"/>
<rect x="670" y="112" width="269" height="118" rx="10" fill="#10b981" opacity="0.12" stroke="#34d399" stroke-width="1.6"/>
<text x="804.5" y="139" text-anchor="middle" fill="#6ee7b7" font-size="15" font-weight="700">收敛</text>
<text x="804.5" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">存量只减不增</text>
<text x="804.5" y="182" text-anchor="middle" fill="#94a3b8" font-size="11">热点模块优先</text>
</svg>

**🗣️ 参考回答示范**：“三个阶段：摸底、套壳、收敛。摸底的目标是回答‘哪里有债、债多大’——用 arch-review 跑全库扫描，产出基线报告，包括存量违规清单、违规最多的文件热力图、变更最频繁的热点模块。套壳的目标是‘立规矩不改代码’——apply-harness 注入约束体系和 CI 增量闸门，业务代码一行不动，从这一天起新增违规为零。收敛的目标是‘还债有节奏’——存量债排期逐批消化，对照基线定目标，比如 lint 违规数降 80%。这个顺序不能乱，因为**没有摸底就没有基线，没有基线就无法证明收敛**——先数据、再约束、最后降解。”

**🌟 加分点**：点出“摸底阶段 arch-review 只生成报告、不推进改造”，避免在还没共识时就开始改架构。

**🛡️ 追问防御链**：追问“三个阶段的时长怎么定？” → 摸底看扫描规模和人工确认时间；套壳通常最短（纯注入）；收敛看存量债总量和团队节奏，按月设里程碑。

---

**Q24. baseline.md 里有什么？它为什么是“迁移的合同”？（基线设计）**

**🎯 面试官想考什么**：看你是真把基线当作管理工具，还是只是做一个报告交差。

**🧭 答题框架（PREP）**
- **P**：baseline.md = 存量违规的“快照 + 分布 + 热点”，具体包括：基线数据、违规热力图（Top10 文件）、热点模块（近 90 天变更最频繁）、禁区与风险。
- **R**：没有基线的迁移没法验收——“改善了”需要数字来证明。
- **E**：基线报告里的目标值（如 lint 违规 <200、零测试包 0 个）就是收敛阶段的验收条件。
- **P**：所以它是迁移的合同：**管理层认这个数字，开发按这个数字还债，验收比这个数字**。



<svg xmlns="http://www.w3.org/2000/svg" width="980" height="320" viewBox="0 0 980 320">
  <defs>
    <linearGradient id="bgq24" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arq24" markerWidth="9" markerHeight="9" refX="8" refY="4.5" orient="auto">
      <path d="M 0 0 L 9 4.5 L 0 9 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="980" height="320" fill="url(#bgq24)"/>
  <text x="490" y="42" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">图 24 · baseline.md：迁移的合同</text>
  <text x="490" y="292" text-anchor="middle" fill="#475569" font-size="16">数字下降=改善，数字不动=没改善，全程可追溯</text>
<rect x="40" y="112" width="190" height="118" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="135.0" y="139" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">基线数据</text>
<text x="135.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">lint/测试/文档</text>
<text x="135.0" y="182" text-anchor="middle" fill="#94a3b8" font-size="11">三组量化数字</text>
<line x1="230" y1="171.0" x2="276" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq24)"/>
<rect x="276" y="112" width="190" height="118" rx="10" fill="#8b5cf6" opacity="0.12" stroke="#a78bfa" stroke-width="1.6"/>
<text x="371.0" y="139" text-anchor="middle" fill="#c4b5fd" font-size="15" font-weight="700">热力图</text>
<text x="371.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">问题分布一目了然</text>
<line x1="466" y1="171.0" x2="512" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq24)"/>
<rect x="512" y="112" width="190" height="118" rx="10" fill="#f59e0b" opacity="0.12" stroke="#fbbf24" stroke-width="1.6"/>
<text x="607.0" y="139" text-anchor="middle" fill="#fcd34d" font-size="15" font-weight="700">Top 热点模块</text>
<text x="607.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">优先处理的清单</text>
<line x1="702" y1="171.0" x2="748" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq24)"/>
<rect x="748" y="112" width="190" height="118" rx="10" fill="#ef4444" opacity="0.12" stroke="#f87171" stroke-width="1.6"/>
<text x="843.0" y="139" text-anchor="middle" fill="#fca5a5" font-size="15" font-weight="700">禁区与风险</text>
<text x="843.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">先做什么·后做什么</text>
<text x="843.0" y="182" text-anchor="middle" fill="#94a3b8" font-size="11">红线不能碰</text>
</svg>

**🗣️ 参考回答示范**：“baseline.md 是存量违规的完整快照，我把它当成迁移的合同。里面四部分：一是基线数据，各质量维度当前的数字，比如 lint 违规 1,234 条、零测试包 11 个；二是热力图，违规最多的 10 个文件——这就是还债的优先队列；三是热点模块，近 90 天变更最频繁的代码区域——这里优先级最高，因为每天都在被改；四是禁区与风险。为什么叫合同？因为迁移最怕‘没有锚点’：团队说在改善，管理层说看不出，最后变成口水战。有了 baseline，改善是‘数字下降’，没改善是‘数字不动’，验收全部有据可依。”

**🌟 加分点**：说“热点模块优先”背后的理由——变更最频繁的地方就是新违规的主要来源，守住它等于守住了增量闸门。

**🛡️ 追问防御链**：追问“基线多久更新一次？” → 基线是快照不是活文档：存量债消化到一定程度（如降 50%）或重大架构调整后重新摸一次底、刷新基线。

---

**Q25. 套壳阶段为什么强调“不改代码”？（改造纪律）**

**🎯 面试官想考什么**：考你能不能忍住“顺手改一下”的冲动——这是迁移项目失败最常见的原因。

**🧭 答题框架（PREP）**
- **P**：套壳阶段的目标是“先把规则和闸门立起来”，业务代码一行不动。
- **R**：因为一旦边注入边改代码，违规数就分不清是注入引入的还是顺手改出来的——基线失真，增量闸门失效。
- **E**：注入的是规则文件、技能、CI 配置，零业务改动；从这一天起新增违规由机器兑底。
- **P**：纪律的本质是**保持变量的单一**——一次只改一件事，才能知道什么在起作用。



<svg xmlns="http://www.w3.org/2000/svg" width="980" height="420" viewBox="0 0 980 482.0">
  <defs>
    <linearGradient id="bgq25" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arq25" markerWidth="9" markerHeight="9" refX="8" refY="4.5" orient="auto">
      <path d="M 0 0 L 9 4.5 L 0 9 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="980" height="482.0" fill="url(#bgq25)"/>
  <text x="490" y="42" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">图 25 · 套壳为什么强调不改代码：保持变量单一</text>
  <text x="490" y="464" text-anchor="middle" fill="#475569" font-size="16">套壳阶段动代码，等于"规则效果"和"业务改动"两个变量同时变——问题说不清是谁造成的</text>
<rect x="40" y="104" width="427" height="128" rx="10" fill="#10b981" opacity="0.12" stroke="#34d399" stroke-width="1.6"/>
<text x="253.5" y="131" text-anchor="middle" fill="#6ee7b7" font-size="15" font-weight="700">只注入规则</text>
<text x="253.5" y="156" text-anchor="middle" fill="#94a3b8" font-size="11">规则+技能+CI 配置</text>
<text x="253.5" y="174" text-anchor="middle" fill="#94a3b8" font-size="11">业务代码一行不动</text>
<line x1="467" y1="168.0" x2="513" y2="168.0" stroke="#475569" stroke-width="3" marker-end="url(#arq25)"/>
<rect x="513" y="104" width="427" height="128" rx="10" fill="#ef4444" opacity="0.12" stroke="#f87171" stroke-width="1.6"/>
<text x="726.5" y="131" text-anchor="middle" fill="#fca5a5" font-size="15" font-weight="700">边注入边改码</text>
<text x="726.5" y="156" text-anchor="middle" fill="#94a3b8" font-size="11">存量数变了说不清</text>
<text x="726.5" y="174" text-anchor="middle" fill="#94a3b8" font-size="11">基线失真</text>
<rect x="40" y="316" width="900" height="128" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="490.0" y="343" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">增量闸门</text>
<text x="490.0" y="368" text-anchor="middle" fill="#94a3b8" font-size="11">约束先于改造生效</text>
<text x="490.0" y="386" text-anchor="middle" fill="#94a3b8" font-size="11">新增违规被机器拦截</text>
<line x1="253.5" y1="232" x2="340.0" y2="316" stroke="#475569" stroke-width="3" marker-end="url(#arq25)"/>
<line x1="726.5" y1="232" x2="640.0" y2="316" stroke="#475569" stroke-width="3" marker-end="url(#arq25)"/>
</svg>

**🗣️ 参考回答示范**：“套壳阶段强调不改代码，是为了**保持变量的单一**。如果你边注入规则边改代码，那存量违规数变了，说不清是符合新规则的正常减少，还是顺手引入的新违规——基线失真，增量闸门就失效了。所以这个阶段的纪律是：注入的只有规则文件、技能包、CI 配置，业务代码一行不动。从注入完成那一刻起，CI 开始拿 baseline 比对，新增违规被机器拦截——约束先于改造生效。”

**🌟 加分点**：把“不改代码”上升为“变量单一”的科学方法（一次只改一个变量才能归因），而不是简单的工作纪律。

**🛡️ 追问防御链**：追问“套壳后存量违规真的会自己下降吗？” → 不会自己降，但会在正常开发中被顺带清理：开发经过的地方顺手修掉——这正是“热点模块优先”把清理引导到变更最密集区域的原因。

---

**Q26. 仅靠 CI 增量闸门就能守住新增代码质量吗？（守门短板）**

**🎯 面试官想考什么**：考你是否知道自己方案的天花板——主动说短板比被问出来好一万倍。

**🧭 答题框架（PREP）**
- **P**：不能，增量闸门守住的是“规则维度”，但规则之外的问题它管不了。
- **R**：lint/静态分析只能抓“机械可判定的违规”，抓不到“逻辑错误、需求偏差、架构腐化”这类语义问题。
- **E**：所以方案是“门禁 + 双轴评审”双保险：CI 拦规则，双轴评审拦语义；增量闸门负责“不新增机械债”，评审负责“不新增语义债”。
- **P**：结论——**增量闸门是底线守护者，不是质量的全部**；承认这一点，方案反而更完整。



<svg xmlns="http://www.w3.org/2000/svg" width="980" height="420" viewBox="0 0 980 482.0">
  <defs>
    <linearGradient id="bgq26" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arq26" markerWidth="9" markerHeight="9" refX="8" refY="4.5" orient="auto">
      <path d="M 0 0 L 9 4.5 L 0 9 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="980" height="482.0" fill="url(#bgq26)"/>
  <text x="490" y="42" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">图 26 · 增量闸门的短板：双保险</text>
  <text x="490" y="464" text-anchor="middle" fill="#475569" font-size="16">分工的第一原则：机器能自动判定的交给闸门，判不了的交给人（双轴评审）</text>
<rect x="40" y="104" width="427" height="128" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="253.5" y="131" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">CI 增量闸门</text>
<text x="253.5" y="156" text-anchor="middle" fill="#94a3b8" font-size="11">拦机械可判定违规</text>
<text x="253.5" y="174" text-anchor="middle" fill="#94a3b8" font-size="11">命名/依赖/竞态</text>
<line x1="467" y1="168.0" x2="513" y2="168.0" stroke="#475569" stroke-width="3" marker-end="url(#arq26)"/>
<rect x="513" y="104" width="427" height="128" rx="10" fill="#8b5cf6" opacity="0.12" stroke="#a78bfa" stroke-width="1.6"/>
<text x="726.5" y="131" text-anchor="middle" fill="#c4b5fd" font-size="15" font-weight="700">双轴评审</text>
<text x="726.5" y="156" text-anchor="middle" fill="#94a3b8" font-size="11">拦语义问题</text>
<text x="726.5" y="174" text-anchor="middle" fill="#94a3b8" font-size="11">逻辑错误/需求偏差</text>
<rect x="40" y="316" width="900" height="128" rx="10" fill="#10b981" opacity="0.12" stroke="#34d399" stroke-width="1.6"/>
<text x="490.0" y="343" text-anchor="middle" fill="#6ee7b7" font-size="15" font-weight="700">双保险</text>
<text x="490.0" y="368" text-anchor="middle" fill="#94a3b8" font-size="11">不新增机械债 + 不新增语义债</text>
<line x1="253.5" y1="232" x2="340.0" y2="316" stroke="#475569" stroke-width="3" marker-end="url(#arq26)"/>
<line x1="726.5" y1="232" x2="640.0" y2="316" stroke="#475569" stroke-width="3" marker-end="url(#arq26)"/>
</svg>

**🗣️ 参考回答示范**：“不能，我不能说增量闸门守住了新增质量，它只守住了规则维度。lint、静态分析这一类闸门只能抓机械可判定的违规：命名、圈复杂度、依赖方向、竞态、危险调用——它抓不到逻辑错误、需求偏差这种语义问题。所以我的方案是双保险：**门禁管规则，双轴评审管语义**。CI 增量闸门保证‘不新增机械债’，双轴评审保证‘不新增语义债’，两者覆盖的是不同维度。主动说出这条短板，其实是在告诉面试官：我知道每个机制的能力边界。”

**🌟 加分点**：主动把“能力边界”讲清楚——“机械可判定”与“语义判断”的区分，正是整套质量体系的分工逻辑。

**🛡️ 追问防御链**：追问“那语义问题被评审漏掉怎么办？” → 双轴评审有 10 个固定维度 + 按代码特征加载的领域检查细则，且 review.md 留档可追溯；漏网的最后一道防线是部署验证（冒烟/健康检查/关键链路）。

---

**Q27. 摸底阶段具体摸什么？为什么说“没有数据的摸底是拍脑袋”？（摸底方法）**

**🎯 面试官想考什么**：看你会不会“用数据说话”——迁移项目里最怕“我感觉我们质量还行”。

**🧭 答题框架（PREP）**
- **P**：摸三类东西——工具链现状（用什么工具、有没有门禁）、产出物现状（测试、文档、lint 结果）、架构健康度（arch-review 扫描出的摩擦信号）。
- **R**：因为这些是“说得清、可量化”的债，而“代码风格差”“可维护性低”这类感觉无法作为基线。
- **E**：摸底产出基线报告——违规数、Top10 文件、热点模块、零测试包数量，全部是数字。
- **P**：没有数字的摸底只能叫“感觉”，感觉不能当合同、不能验收、不能比较。



<svg xmlns="http://www.w3.org/2000/svg" width="980" height="320" viewBox="0 0 980 320">
  <defs>
    <linearGradient id="bgq27" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arq27" markerWidth="9" markerHeight="9" refX="8" refY="4.5" orient="auto">
      <path d="M 0 0 L 9 4.5 L 0 9 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="980" height="320" fill="url(#bgq27)"/>
  <text x="490" y="42" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">图 27 · 摸底摸什么：三类可量化现状</text>
  <text x="490" y="292" text-anchor="middle" fill="#475569" font-size="16">没有数据的摸底是拍脑袋</text>
<rect x="40" y="112" width="190" height="118" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="135.0" y="139" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">工具链现状</text>
<text x="135.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">用什么工具</text>
<text x="135.0" y="182" text-anchor="middle" fill="#94a3b8" font-size="11">有没有门禁</text>
<line x1="230" y1="171.0" x2="276" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq27)"/>
<rect x="276" y="112" width="190" height="118" rx="10" fill="#8b5cf6" opacity="0.12" stroke="#a78bfa" stroke-width="1.6"/>
<text x="371.0" y="139" text-anchor="middle" fill="#c4b5fd" font-size="15" font-weight="700">产出物现状</text>
<text x="371.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">测试/文档/lint 结果</text>
<line x1="466" y1="171.0" x2="512" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq27)"/>
<rect x="512" y="112" width="190" height="118" rx="10" fill="#f59e0b" opacity="0.12" stroke="#fbbf24" stroke-width="1.6"/>
<text x="607.0" y="139" text-anchor="middle" fill="#fcd34d" font-size="15" font-weight="700">架构健康度</text>
<text x="607.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">arch-review 摩擦信号</text>
<line x1="702" y1="171.0" x2="748" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq27)"/>
<rect x="748" y="112" width="190" height="118" rx="10" fill="#10b981" opacity="0.12" stroke="#34d399" stroke-width="1.6"/>
<text x="843.0" y="139" text-anchor="middle" fill="#6ee7b7" font-size="15" font-weight="700">基线报告</text>
<text x="843.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">全部数字说话</text>
</svg>

**🗣️ 参考回答示范**：“摸底摸三类东西。第一类，工具链现状：项目现在用什么构建、lint、测试工具，有没有门禁在跑——没有的话说明过去完全是靠自觉。第二类，产出物现状：测试覆盖率是多少、哪些包完全没有测试、lint 违规总数是多少。第三类，架构健康度：用 arch-review 扫描全库，看有没有循环依赖、越层调用这些摩擦信号。三类摸完汇总成基线报告。为什么强调数据？因为**感觉不能当基线**——‘代码风格差’没法验收，‘1,234 条违规’可以；数字可以当合同、可以比较、可以证明收敛。没有数据的摸底就是拍脑袋。”

**🌟 加分点**：点出“摸”的对象是可量化的债，并强调基线报告是与团队和管理层对齐的工具，不只是给自己的备忘。

**🛡️ 追问防御链**：追问“摸底要多细？” → 细到能支撑三个决策：存量债总量（要不要软闸门）、还债优先级（Top10 文件排序）、风险区（哪里禁改）——摸不到这个粒度，就继续摸。

---

**Q28. 软闸门和“零容忍”什么关系？什么场景下必须升级为硬闸门？（闸门演进）**

**🎯 面试官想考什么**：考你的是“何时该妥协、何时不该妥协”的判断力——好的工程师知道规则的适用范围。

**🧭 答题框架（PREP）**
- **P**：软闸门是对“存量”的治理策略，对“新增”从来都是零容忍——两者不矛盾。
- **R**：软 vs 硬不是质量标准的取舍，而是“存量债消化方式”的取舍：软闸门=边开发边消化，硬闸门=先清后再开发。
- **E**：安全漏洞、密钥泄露这类问题必须硬闸门（发现即阻断）；而编码风格类违规适合软闸门逐步消化。
- **P**：升级原则——**风险等级决定闸门硬度**：会出事故的用硬闸门，影响体验的用软闸门。



<svg xmlns="http://www.w3.org/2000/svg" width="980" height="420" viewBox="0 0 980 482.0">
  <defs>
    <linearGradient id="bgq28" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arq28" markerWidth="9" markerHeight="9" refX="8" refY="4.5" orient="auto">
      <path d="M 0 0 L 9 4.5 L 0 9 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="980" height="482.0" fill="url(#bgq28)"/>
  <text x="490" y="42" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">图 28 · 闸门硬度：风险等级决定</text>
  <text x="490" y="464" text-anchor="middle" fill="#475569" font-size="16">会出事故的用硬闸门，影响体验的用软闸门</text>
<rect x="40" y="104" width="427" height="128" rx="10" fill="#ef4444" opacity="0.12" stroke="#f87171" stroke-width="1.6"/>
<text x="253.5" y="131" text-anchor="middle" fill="#fca5a5" font-size="15" font-weight="700">事故级</text>
<text x="253.5" y="156" text-anchor="middle" fill="#94a3b8" font-size="11">密钥/危险DDL/安全漏洞</text>
<text x="253.5" y="174" text-anchor="middle" fill="#94a3b8" font-size="11">硬闸门：发现即阻断</text>
<line x1="467" y1="168.0" x2="513" y2="168.0" stroke="#475569" stroke-width="3" marker-end="url(#arq28)"/>
<rect x="513" y="104" width="427" height="128" rx="10" fill="#f59e0b" opacity="0.12" stroke="#fbbf24" stroke-width="1.6"/>
<text x="726.5" y="131" text-anchor="middle" fill="#fcd34d" font-size="15" font-weight="700">体验级</text>
<text x="726.5" y="156" text-anchor="middle" fill="#94a3b8" font-size="11">命名/圈复杂度超限</text>
<text x="726.5" y="174" text-anchor="middle" fill="#94a3b8" font-size="11">软闸门：逐步消化</text>
<rect x="40" y="316" width="900" height="128" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="490.0" y="343" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">升级原则</text>
<text x="490.0" y="368" text-anchor="middle" fill="#94a3b8" font-size="11">风险等级决定闸门硬度</text>
<line x1="253.5" y1="232" x2="340.0" y2="316" stroke="#475569" stroke-width="3" marker-end="url(#arq28)"/>
<line x1="726.5" y1="232" x2="640.0" y2="316" stroke="#475569" stroke-width="3" marker-end="url(#arq28)"/>
</svg>

**🗣️ 参考回答示范**：“软闸门和零容忍不冲突，它俩的边界在‘存量 vs 新增’：对新增代码，软闸门从来都是零容忍；对存量代码，才谈得上软和硬。真正要判断的是另一件事——**不同性质的违规，闸门硬度应该不同**。比如硬编码密钥、危险 DDL、已知安全漏洞，这类是事故级风险，必须硬闸门：发现就阻断，一刻都不能忍；而命名风格、圈复杂度超限这类，属于影响可维护性但不至于出事的问题，适合软闸门：允许存量存在、逐步消化。所以我的升级原则是：**风险等级决定闸门硬度**——会出事故的用硬闸门，影响体验的用软闸门。”

**🌟 加分点**：给出“事故级 vs 体验级”的二分，并各举一个具体例子（密钥=事故级、命名=体验级），展示分级治理思维。

**🛡️ 追问防御链**：追问“软闸门运行中发现存量里有安全漏洞怎么办？” → 立刻升级：存量漏洞不享受“只减不增”待遇，按事故处理立即修复——软闸门不等于对安全妥协。

---

### 4.5 方向五：变更状态机与评审（7 题）

**Q29. 变更状态机的完整状态流是什么样的？为什么 reviewing 会出现两次？（⚡状态机必考 · 完整展开）**

**🎯 面试官想考什么**：这是“流程到底跑没跑通过”的验金石——面试官会从你答的状态细节判断你是背了流程图，还是真写过状态推进逻辑。

**🧭 答题框架（PREP）**
- **P**：完整链路是 9 个状态：**drafting → reviewing → approved → coding → testing → reviewing → ci → verifying → done**，其中 reviewing 出现两次。
- **R**：两次 reviewing 不是笔误——第一次评审“规格”（需求对不对），第二次评审“代码”（实现对不对），这是两种完全不同的评审对象。
- **E**：规格评审在需求分析阶段（对应 drafting/reviewing/approved），代码评审在开发阶段（对应 coding/testing 后的 reviewing）；评审通过进 ci，部署验证过进 done。
- **P**：所以状态机不是一条直线，而是一条“先审规格、再审代码、最后机器门禁”的严谨链路。



<svg xmlns="http://www.w3.org/2000/svg" width="980" height="350" viewBox="0 0 980 382.0">
  <defs>
    <linearGradient id="bgq29" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arq29" markerWidth="9" markerHeight="9" refX="8" refY="4.5" orient="auto">
      <path d="M 0 0 L 9 4.5 L 0 9 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="980" height="382.0" fill="url(#bgq29)"/>
  <text x="490" y="42" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">图 29 · 变更状态机：9 个状态，reviewing 出现两次</text>
  <text x="490" y="364" text-anchor="middle" fill="#475569" font-size="16">同一状态出现两次不是错误——它是"先过规格关、再过代码关"两道关的体现</text>
<rect x="40" y="108" width="143" height="92" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="111.5" y="135" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">drafting</text>
<text x="111.5" y="160" text-anchor="middle" fill="#94a3b8" font-size="11">起草规格</text>
<line x1="183" y1="154.0" x2="229" y2="154.0" stroke="#475569" stroke-width="3" marker-end="url(#arq29)"/>
<rect x="229" y="108" width="143" height="92" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="300.5" y="135" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">reviewing ①</text>
<text x="300.5" y="160" text-anchor="middle" fill="#94a3b8" font-size="11">规格评审</text>
<line x1="372" y1="154.0" x2="418" y2="154.0" stroke="#475569" stroke-width="3" marker-end="url(#arq29)"/>
<rect x="418" y="108" width="143" height="92" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="489.5" y="135" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">approved</text>
<text x="489.5" y="160" text-anchor="middle" fill="#94a3b8" font-size="11">规格通过</text>
<line x1="561" y1="154.0" x2="607" y2="154.0" stroke="#475569" stroke-width="3" marker-end="url(#arq29)"/>
<rect x="607" y="108" width="143" height="92" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="678.5" y="135" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">coding</text>
<text x="678.5" y="160" text-anchor="middle" fill="#94a3b8" font-size="11">写代码</text>
<line x1="750" y1="154.0" x2="796" y2="154.0" stroke="#475569" stroke-width="3" marker-end="url(#arq29)"/>
<rect x="796" y="108" width="143" height="92" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="867.5" y="135" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">testing</text>
<text x="867.5" y="160" text-anchor="middle" fill="#94a3b8" font-size="11">写测试</text>
<rect x="40" y="252" width="190" height="92" rx="10" fill="#8b5cf6" opacity="0.12" stroke="#a78bfa" stroke-width="1.6"/>
<text x="135.0" y="279" text-anchor="middle" fill="#c4b5fd" font-size="15" font-weight="700">reviewing ②</text>
<text x="135.0" y="304" text-anchor="middle" fill="#94a3b8" font-size="11">代码评审</text>
<line x1="230" y1="298.0" x2="276" y2="298.0" stroke="#475569" stroke-width="3" marker-end="url(#arq29)"/>
<rect x="276" y="252" width="190" height="92" rx="10" fill="#8b5cf6" opacity="0.12" stroke="#a78bfa" stroke-width="1.6"/>
<text x="371.0" y="279" text-anchor="middle" fill="#c4b5fd" font-size="15" font-weight="700">ci</text>
<text x="371.0" y="304" text-anchor="middle" fill="#94a3b8" font-size="11">机器门禁</text>
<line x1="466" y1="298.0" x2="512" y2="298.0" stroke="#475569" stroke-width="3" marker-end="url(#arq29)"/>
<rect x="512" y="252" width="190" height="92" rx="10" fill="#8b5cf6" opacity="0.12" stroke="#a78bfa" stroke-width="1.6"/>
<text x="607.0" y="279" text-anchor="middle" fill="#c4b5fd" font-size="15" font-weight="700">verifying</text>
<text x="607.0" y="304" text-anchor="middle" fill="#94a3b8" font-size="11">部署验证</text>
<line x1="702" y1="298.0" x2="748" y2="298.0" stroke="#475569" stroke-width="3" marker-end="url(#arq29)"/>
<rect x="748" y="252" width="190" height="92" rx="10" fill="#10b981" opacity="0.12" stroke="#34d399" stroke-width="1.6"/>
<text x="843.0" y="279" text-anchor="middle" fill="#6ee7b7" font-size="15" font-weight="700">done</text>
<text x="843.0" y="304" text-anchor="middle" fill="#94a3b8" font-size="11">完成</text>
<path d="M 867.5 200 V 226 H 135 V 252" fill="none" stroke="#475569" stroke-width="3" marker-end="url(#arq29)"/>
</svg>

**🗣️ 参考回答示范**：“变更状态机一共 9 个状态：drafting、reviewing、approved、coding、testing、reviewing、ci、verifying、done。有个细节值得说：**reviewing 出现了两次**——第一次是规格评审，在需求分析阶段，评审的是 change.md 这份规格，‘需求对不对、边界全不全’；第二次是代码评审，在编码测试之后，评审的是实现，‘代码合不合规范、需求实现全了没有’。两轮评审对象完全不同，所以状态重复出现不是笔误，而是设计。整体归属上：drafting/reviewing/approved 属于需求分析，coding/testing 属于开发，第二个 reviewing 属于评审，ci 是门禁，verifying 是部署验证，done 才交付。进阶：每个阶段都有出口门禁，比如规格评审要求 AC 可测试、边界至少 3 个；代码评审要求 0 严重问题。”

**🌟 加分点**：
- 指出状态与阶段的映射（9 状态归入 6 阶段），证明你对两层模型都清楚。
- 说出“规格可以先变”——状态机不是单向冻结，需求有变更就回到 drafting 重新评审，这体现演进思维。

**🛡️ 追问防御链**：
- **追问“规格评审不过会怎样？”** → 状态停在 reviewing，change.md 继续打磨；只有 approved 才允许进入编码，AI 拿不到 approved 状态就不开工。
- **追问“部署验证失败呢？”** → verifying 状态不推进，验证报告留档，按预案回滚；修复后重新走验证，不会假装做过。

---

**Q30. “防跳步”具体是怎么实现的？AI 想跳步会怎样？（防跳步实现）**

**🎯 面试官想考什么**：考“流程强制”的实现细节——AI 会不会不理状态机直接写代码？靠什么约束？

**🧭 答题框架（PREP）**
- **P**：防跳步靠**前置检查 + 状态过滤**：每个技能启动时先扫描变更目录，只处理处于自己对应状态的 change，不匹配就报错。
- **R**：难点是 AI 的自觉不可靠，必须把“流程纪律”变成“找不到可处理对象”的硬性报错。
- **E**：coding-skill 只认 status=coding 的变更——状态没到 coding 就找不到对象，直接报错退回需求分析，而不是“顺便写了算了”。
- **P**：所以防跳步的本质是**把流程约束翻译成 AI 无法绕过的前置条件**。



<svg xmlns="http://www.w3.org/2000/svg" width="980" height="320" viewBox="0 0 980 320">
  <defs>
    <linearGradient id="bgq30" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arq30" markerWidth="9" markerHeight="9" refX="8" refY="4.5" orient="auto">
      <path d="M 0 0 L 9 4.5 L 0 9 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="980" height="320" fill="url(#bgq30)"/>
  <text x="490" y="42" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">图 30 · 状态机怎么防跳步：AI 想跳也跳不了</text>
  <text x="490" y="292" text-anchor="middle" fill="#475569" font-size="16">AI 想跳步也跳不了：它根本找不到可以下手的东西</text>
<rect x="40" y="112" width="190" height="118" rx="10" fill="#475569" opacity="0.12" stroke="#64748b" stroke-width="1.6"/>
<text x="135.0" y="139" text-anchor="middle" fill="#cbd5e1" font-size="15" font-weight="700">技能启动</text>
<text x="135.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">coding 只认 status=coding</text>
<line x1="230" y1="171.0" x2="276" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq30)"/>
<rect x="276" y="112" width="190" height="118" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="371.0" y="139" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">前置检查</text>
<text x="371.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">扫描变更目录</text>
<line x1="466" y1="171.0" x2="512" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq30)"/>
<rect x="512" y="112" width="190" height="118" rx="10" fill="#ef4444" opacity="0.12" stroke="#f87171" stroke-width="1.6"/>
<text x="607.0" y="139" text-anchor="middle" fill="#fca5a5" font-size="15" font-weight="700">状态不匹配</text>
<text x="607.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">找不到可下手对象</text>
<line x1="702" y1="171.0" x2="748" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq30)"/>
<rect x="748" y="112" width="190" height="118" rx="10" fill="#f59e0b" opacity="0.12" stroke="#fbbf24" stroke-width="1.6"/>
<text x="843.0" y="139" text-anchor="middle" fill="#fcd34d" font-size="15" font-weight="700">报错退回</text>
<text x="843.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">请先完成需求分析</text>
</svg>

**🗣️ 参考回答示范**：“防跳步不是靠在提示词里写‘请你按流程走’，而是把它变成硬性的前置检查。每个技能启动第一件事就是扫描变更目录，按状态过滤出能处理的对象：coding-skill 只认 status=coding 的 change，expert-reviewer 只认 status=reviewing 的 change。如果当前没有任何 change 处于这个状态，技能就直接报错——‘找不到处于 coding 状态的变更，请先完成需求分析’，而不是自作主张去干上一阶段的活。所以 AI 想跳步也跳不了：它根本找不到可以下手的东西。这是把流程纪律翻译成了 AI 无法绕过的条件。”

**🌟 加分点**：给出“定位规则”的三个分支——用户指定 id（校验状态）、恰好 1 个（自动选中）、0 个/多个（报错或停下让用户选），完整展示前置检查逻辑。

**🛡️ 追问防御链**：追问“用户命令 AI 跳过评审直接上线呢？” → 技术上 AI 可以生成内容，但状态机不认：ci/verifying 阶段的前置检查会发现 review.md 不存在/状态不对，机器门禁拒绝放行——人不能“命令”违反门禁，只能先走完流程。

---

**Q31. 状态机和 6 阶段流水线是什么关系？（两层模型）**

**🎯 面试官想考什么**：很多方案二选一：要么有流水线没状态机，要么反之。考你是否理解两层各自解决什么问题。

**🧭 答题框架（PREP）**
- **P**：流水线定义“做什么”（阶段与门禁），状态机定义“做到哪一步、能否推进”（变更的合法生命周期）——一个是任务视图，一个是状态视图。
- **R**：只有流水线没有状态机，变更无法定位进度；只有状态机没有流水线，状态没有内容。
- **E**：阶段给出产出物（change.md→代码→review.md→CI 报告→verify.md），状态机保证这些产出物按序产生、不被跳过。
- **P**：一句话——**流水线是内容的生产线，状态机是内容的质量关卡**。



<svg xmlns="http://www.w3.org/2000/svg" width="980" height="420" viewBox="0 0 980 482.0">
  <defs>
    <linearGradient id="bgq31" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arq31" markerWidth="9" markerHeight="9" refX="8" refY="4.5" orient="auto">
      <path d="M 0 0 L 9 4.5 L 0 9 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="980" height="482.0" fill="url(#bgq31)"/>
  <text x="490" y="42" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">图 31 · 流水线与状态机：同一个流程的两个视图</text>
  <text x="490" y="464" text-anchor="middle" fill="#475569" font-size="16">面试话术：流水线是引擎负责生产，状态机是刹车负责守门</text>
<rect x="40" y="104" width="427" height="128" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="253.5" y="131" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">流水线(任务视图)</text>
<text x="253.5" y="156" text-anchor="middle" fill="#94a3b8" font-size="11">干什么 · 产出 · 门禁</text>
<line x1="467" y1="168.0" x2="513" y2="168.0" stroke="#475569" stroke-width="3" marker-end="url(#arq31)"/>
<rect x="513" y="104" width="427" height="128" rx="10" fill="#8b5cf6" opacity="0.12" stroke="#a78bfa" stroke-width="1.6"/>
<text x="726.5" y="131" text-anchor="middle" fill="#c4b5fd" font-size="15" font-weight="700">状态机(状态视图)</text>
<text x="726.5" y="156" text-anchor="middle" fill="#94a3b8" font-size="11">到哪了 · 能否推进</text>
<rect x="40" y="316" width="900" height="128" rx="10" fill="#10b981" opacity="0.12" stroke="#34d399" stroke-width="1.6"/>
<text x="490.0" y="343" text-anchor="middle" fill="#6ee7b7" font-size="15" font-weight="700">合起来</text>
<text x="490.0" y="368" text-anchor="middle" fill="#94a3b8" font-size="11">流水线生产内容 · 状态机守关卡</text>
<line x1="253.5" y1="232" x2="340.0" y2="316" stroke="#475569" stroke-width="3" marker-end="url(#arq31)"/>
<line x1="726.5" y1="232" x2="640.0" y2="316" stroke="#475569" stroke-width="3" marker-end="url(#arq31)"/>
</svg>

**🗣️ 参考回答示范**：“流水线和状态机是同一个流程的两个视图。流水线是任务视图：需求分析、编码、测试、评审、CI、验证，每阶段干什么、产出什么、门禁是什么。状态机是状态视图：一个具体变更现在到哪了、能不能往下一步推。为什么需要两个？因为只有流水线没有状态机，你没法回答‘这个需求做到哪了’，变更没有进度锚点；只有状态机没有流水线，状态就只是空壳。两者合起来：流水线负责生产内容，状态机负责守质量关卡——状态不合法，内容再多也进不了下一个阶段。”

**🌟 加分点**：用“视图”类比（任务视图/状态视图），展示系统设计里的“正交建模”思维。

**🛡️ 追问防御链**：追问“为什么不用一张大表记录所有状态？” → 状态机是“状态图”（显式定义合法转移），大表是“状态列表”（无法表达转移合法性）；状态机把“能不能从 A 到 B”编码成了规则，这是防跳步的基础。

---

**Q32. 这套体系里，人的角色是什么？哪些节点必须人来决策？（人机协同）**

**🎯 面试官想考什么**：这套体系叫“人机协同”，面试官会追问“AI 这么强，人还有什么用”——考你对‘机器边界’的清醒认识。

**🧭 答题框架（PREP）**
- **P**：人的职责分三类：**定规则、关键节点裁决、处理机器解决不了的事**。
- **R**：因为确定性系统只能解“规则明确”的问题，价值判断（需求合理吗）、风险判断（这单敢上线吗）必须留给人。
- **E**：具体节点——规格评审（需求对不对）、多变更冲突选择、规格有歧义时的讨论、PRD 平台的人工校验；AI 在这些节点“停下请用户决策”，而不是替人决定。
- **P**：所以这套体系的定位是“** AI 执行、人裁决**”——机器越强大，关键决策的归属越要明确。



<svg xmlns="http://www.w3.org/2000/svg" width="980" height="420" viewBox="0 0 980 482.0">
  <defs>
    <linearGradient id="bgq32" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arq32" markerWidth="9" markerHeight="9" refX="8" refY="4.5" orient="auto">
      <path d="M 0 0 L 9 4.5 L 0 9 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="980" height="482.0" fill="url(#bgq32)"/>
  <text x="490" y="42" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">图 32 · 人机协同：AI 执行，人裁决</text>
  <text x="490" y="464" text-anchor="middle" fill="#475569" font-size="16">"AI 停下问人"不是缺陷是设计：决策权留在人手里，AI 才敢放手跑</text>
<rect x="40" y="104" width="269" height="128" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="174.5" y="131" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">人定规则</text>
<text x="174.5" y="156" text-anchor="middle" fill="#94a3b8" font-size="11">5 条规则 · 门禁阈值</text>
<text x="174.5" y="174" text-anchor="middle" fill="#94a3b8" font-size="11">评审标准</text>
<line x1="309" y1="168.0" x2="355" y2="168.0" stroke="#475569" stroke-width="3" marker-end="url(#arq32)"/>
<rect x="355" y="104" width="269" height="128" rx="10" fill="#8b5cf6" opacity="0.12" stroke="#a78bfa" stroke-width="1.6"/>
<text x="489.5" y="131" text-anchor="middle" fill="#c4b5fd" font-size="15" font-weight="700">人裁决关键节点</text>
<text x="489.5" y="156" text-anchor="middle" fill="#94a3b8" font-size="11">规格拍板 · 冲突选择</text>
<text x="489.5" y="174" text-anchor="middle" fill="#94a3b8" font-size="11">风险决策</text>
<line x1="624" y1="168.0" x2="670" y2="168.0" stroke="#475569" stroke-width="3" marker-end="url(#arq32)"/>
<rect x="670" y="104" width="269" height="128" rx="10" fill="#f59e0b" opacity="0.12" stroke="#fbbf24" stroke-width="1.6"/>
<text x="804.5" y="131" text-anchor="middle" fill="#fcd34d" font-size="15" font-weight="700">人处理歧义</text>
<text x="804.5" y="156" text-anchor="middle" fill="#94a3b8" font-size="11">需求澄清对话</text>
<rect x="40" y="316" width="900" height="128" rx="10" fill="#10b981" opacity="0.12" stroke="#34d399" stroke-width="1.6"/>
<text x="490.0" y="343" text-anchor="middle" fill="#6ee7b7" font-size="15" font-weight="700">AI 执行</text>
<text x="490.0" y="368" text-anchor="middle" fill="#94a3b8" font-size="11">按规则推进 · 停下请用户决策</text>
<line x1="174.5" y1="232" x2="265.0" y2="316" stroke="#475569" stroke-width="3" marker-end="url(#arq32)"/>
<line x1="489.5" y1="232" x2="490.0" y2="316" stroke="#475569" stroke-width="3" marker-end="url(#arq32)"/>
<line x1="804.5" y1="232" x2="715.0" y2="316" stroke="#475569" stroke-width="3" marker-end="url(#arq32)"/>
</svg>

**🗣️ 参考回答示范**：“我的原则是‘**AI 执行、人裁决**’。人在这套体系里管三件事：第一，定规则——5 条规则、门禁阈值、评审标准都是人定的，AI 只是执行规则；第二，关键节点裁决——规格评审拍板需求对不对、多变更冲突时选择先做哪个、风险决策敢不敢上线，这些节点 AI 都会明确‘停下请用户决策’，不会替你拍板；第三，处理机器解决不了的事——规格有歧义时人和 AI 对话澄清（harnessing 就是一问一答的拷问式对话）。为什么必须留给人？因为确定性系统只能解‘规则明确’的问题，需求合理性和风险归谁这种价值判断，机器不该做。”

**🌟 加分点**：列举具体的“停下请用户”场景（≥2 个变更、检测多语言、规格歧义），证明这不是口号而是真实存在的代码分支。

**🛡️ 追问防御链**：追问“那 AI 岂不是处处受限，效率在哪？” → 效率在别处：AI 包揽‘按规则生产’（写代码、写测试、跑门禁、出报告），这些占工作量 80%，人只出现在 20% 的决策节点上——所以是效率与控制的平衡。

---

**Q33. 一份完整的 change.md 长什么样？为什么称它为“规格真相源”？（规格设计）**

**🎯 面试官想考什么**：很多团队的需求文档写了没人看，考你是否理解“规格”为什么必须是可执行、可追溯到代码的东西。

**🧭 答题框架（PREP）**
- **P**：change.md 七要素：标题、描述、状态、AC（验收条件）、边界（≥3）、设计约束、测试策略、技术备忘录。
- **R**：它叫“真相源”是因为后续所有环节都只对它负责：编码对着 AC 写、测试对着 AC 写、评审对着 AC 核。
- **E**：评审时 Spec 轴逐条勾 AC——每一条要么通过要么给出证据失败，不存在“差不多”。
- **P**：真相源的意义——**消除歧义：全流程只认一份规格，而不是口头需求或聊天记录**。



<svg xmlns="http://www.w3.org/2000/svg" width="980" height="320" viewBox="0 0 980 320">
  <defs>
    <linearGradient id="bgq33" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arq33" markerWidth="9" markerHeight="9" refX="8" refY="4.5" orient="auto">
      <path d="M 0 0 L 9 4.5 L 0 9 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="980" height="320" fill="url(#bgq33)"/>
  <text x="490" y="42" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">图 33 · change.md：全程只认这一份真相源</text>
  <text x="490" y="292" text-anchor="middle" fill="#475569" font-size="16">全流程只认这一份规格，不认口头和零散笔记</text>
<rect x="40" y="112" width="190" height="118" rx="10" fill="#475569" opacity="0.12" stroke="#64748b" stroke-width="1.6"/>
<text x="135.0" y="139" text-anchor="middle" fill="#cbd5e1" font-size="15" font-weight="700">标题</text>
<text x="135.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">一句话讲清变更</text>
<line x1="230" y1="171.0" x2="276" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq33)"/>
<rect x="276" y="112" width="190" height="118" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="371.0" y="139" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">AC + 边界</text>
<text x="371.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">3 个以上边界情况</text>
<text x="371.0" y="182" text-anchor="middle" fill="#94a3b8" font-size="11">AC 可测试</text>
<line x1="466" y1="171.0" x2="512" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq33)"/>
<rect x="512" y="112" width="190" height="118" rx="10" fill="#8b5cf6" opacity="0.12" stroke="#a78bfa" stroke-width="1.6"/>
<text x="607.0" y="139" text-anchor="middle" fill="#c4b5fd" font-size="15" font-weight="700">设计约束</text>
<text x="607.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">改哪些文件 · 测试策略</text>
<line x1="702" y1="171.0" x2="748" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq33)"/>
<rect x="748" y="112" width="190" height="118" rx="10" fill="#10b981" opacity="0.12" stroke="#34d399" stroke-width="1.6"/>
<text x="843.0" y="139" text-anchor="middle" fill="#6ee7b7" font-size="15" font-weight="700">备忘录</text>
<text x="843.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">先做什么后做什么</text>
</svg>

**🗣️ 参考回答示范**：“change.md 是每个变更的规格卡片，包含：标题和描述、当前状态、AC 验收条件、至少 3 个边界情况、设计约束、测试策略、技术备忘录。为什么我强调它是‘规格真相源’？因为整条流水线都只对它负责：编码阶段对着 AC 写‘让 AC 变成失败测试再转绿’，测试阶段对着 AC 和边界写测试，评审阶段 Spec 轴逐条核 AC 全实现了没有。换句话说，**全流程只认这一份规格**，口头需求、聊天记录、邮件都不算数——这就把‘需求理解不一致’这个最常见的协作事故从机制上消灭了。”

**🌟 加分点**：说“AC 先转测试”这条闭环——规格不是给人看的文档，是直接转成机器能跑的测试，这是它区别于普通需求文档的本质。

**🛡️ 追问防御链**：追问“需求变了怎么办？” → 不硬改代码：回到需求分析改 change.md、状态回 drafting 重新评审，评审过了再改实现——变更是规格先变，代码跟着变。

---

**Q34. 双轴评审的 10 个检查维度是怎么设计的？（评审清单设计）**

**🎯 面试官想考什么**：考评审清单的“设计”而不是罗列——为什么是这 10 个维度，它们内部怎么分组。

**🧭 答题框架（PREP）**
- **P**：10 个维度分三组：**Spec 轴（需求匹配）+ Standards 轴（规范合规）+ 公共维度**。
- **R**：分组原因是检查对象的性质不同——Spec 轴对需求负责，Standards 轴对规则负责，公共维度对工程质量负责。
- **E**：Spec 轴：功能完整性、SDD 合规；Standards 轴：架构合规、编码规范、代码质量、安全；公共：测试质量、TDD 合规、流程合规、领域语言一致性。
- **P**：清单设计原则——**每个维度都可对照规则/产物给出“通过/问题+证据”**，禁止空泛评价。



<svg xmlns="http://www.w3.org/2000/svg" width="980" height="420" viewBox="0 0 980 482.0">
  <defs>
    <linearGradient id="bgq34" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arq34" markerWidth="9" markerHeight="9" refX="8" refY="4.5" orient="auto">
      <path d="M 0 0 L 9 4.5 L 0 9 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="980" height="482.0" fill="url(#bgq34)"/>
  <text x="490" y="42" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">图 34 · 评审清单：10 个固定维度</text>
  <text x="490" y="464" text-anchor="middle" fill="#475569" font-size="16">清单固定 = 口径一致：这次和上次、这个人和那个人，评审结论可对比</text>
<rect x="40" y="104" width="269" height="128" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="174.5" y="131" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">Spec 轴 2 项</text>
<text x="174.5" y="156" text-anchor="middle" fill="#94a3b8" font-size="11">功能完整性 · SDD 合规</text>
<line x1="309" y1="168.0" x2="355" y2="168.0" stroke="#475569" stroke-width="3" marker-end="url(#arq34)"/>
<rect x="355" y="104" width="269" height="128" rx="10" fill="#8b5cf6" opacity="0.12" stroke="#a78bfa" stroke-width="1.6"/>
<text x="489.5" y="131" text-anchor="middle" fill="#c4b5fd" font-size="15" font-weight="700">Standards 轴 4 项</text>
<text x="489.5" y="156" text-anchor="middle" fill="#94a3b8" font-size="11">架构 · 编码 · 质量 · 安全</text>
<line x1="624" y1="168.0" x2="670" y2="168.0" stroke="#475569" stroke-width="3" marker-end="url(#arq34)"/>
<rect x="670" y="104" width="269" height="128" rx="10" fill="#475569" opacity="0.12" stroke="#64748b" stroke-width="1.6"/>
<text x="804.5" y="131" text-anchor="middle" fill="#cbd5e1" font-size="15" font-weight="700">公共 4 项</text>
<text x="804.5" y="156" text-anchor="middle" fill="#94a3b8" font-size="11">测试 · TDD · 流程 · 领域语言</text>
<rect x="40" y="316" width="900" height="128" rx="10" fill="#10b981" opacity="0.12" stroke="#34d399" stroke-width="1.6"/>
<text x="490.0" y="343" text-anchor="middle" fill="#6ee7b7" font-size="15" font-weight="700">输出要求</text>
<text x="490.0" y="368" text-anchor="middle" fill="#94a3b8" font-size="11">通过 或 问题+证据</text>
<text x="490.0" y="386" text-anchor="middle" fill="#94a3b8" font-size="11">不留模糊地带</text>
<line x1="174.5" y1="232" x2="265.0" y2="316" stroke="#475569" stroke-width="3" marker-end="url(#arq34)"/>
<line x1="489.5" y1="232" x2="490.0" y2="316" stroke="#475569" stroke-width="3" marker-end="url(#arq34)"/>
<line x1="804.5" y1="232" x2="715.0" y2="316" stroke="#475569" stroke-width="3" marker-end="url(#arq34)"/>
</svg>

**🗣️ 参考回答示范**：“评审清单 10 个维度，我一共分三组。Spec 轴 2 个维度：功能完整性（对照 AC 逐条核）和 SDD 合规（有没有遵守先规格后编码）；Standards 轴 4 个维度：架构合规、编码规范、代码质量（10 个代码味道基线）、安全；公共维度 4 个：测试质量（覆盖率/断言有效性）、TDD 合规、流程合规（有没有跳步）、领域语言一致性（有没有用对统一的术语）。设计原则就一条：**每个维度必须能输出‘通过，或问题+证据’**，不允许写‘整体还行’这种空泛评价——评价必须落实到具体的行号和规则条文，这样评审结果才可复核、可追责。”

**🌟 加分点**：点出“领域语言一致性”这个维度——它来自 domain-modeling（统一术语表），评审会查代码里的术语和 CONTEXT.md 是否一致，这体现了“质量不只是代码层面”。

**🛡️ 追问防御链**：追问“Standards 轴会不会漏掉某个领域的特有规则？” → 会，所以有混合模式：评审时检测代码特征自动加载领域技能（redis-cache-wrapper、security-toolkit 等）作为维度 4-6 的补充细则——通用维度打底，领域细则增强。

---

**Q35. review.md 评审报告怎么产出、怎么归档、怎么驱动退回？（⚡评审机制必考 · 完整展开）**

**🎯 面试官想考什么**：评审的价值不在“评”，而在“评完之后发生什么”——考你有没有把评审结果闭环到流程里，而不是评完就完。

**🧭 答题框架（PREP）**
- **P**：评审产出 review.md，由双轴分开报告（两份独立结论）合并归档；问题分级 🔴 严重 / 🟡 建议。
- **R**：闭环的关键：**评审结论不是参考意见，而是驱动状态转移的硬条件**。
- **E**：0 个 🔴 → change 状态推进到 ci；有 🔴 → 退回编码阶段，review.md 就是修复任务单；🟡 记录但不阻塞。
- **P**：所以评审是“**评、档、推**”三件事——结论如实记录，问题带着证据，状态按结论推进。



<svg xmlns="http://www.w3.org/2000/svg" width="980" height="420" viewBox="0 0 980 482.0">
  <defs>
    <linearGradient id="bgq35" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arq35" markerWidth="9" markerHeight="9" refX="8" refY="4.5" orient="auto">
      <path d="M 0 0 L 9 4.5 L 0 9 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="980" height="482.0" fill="url(#bgq35)"/>
  <text x="490" y="42" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">图 35 · 评审闭环：评、档、推三件事</text>
  <text x="490" y="464" text-anchor="middle" fill="#475569" font-size="16">闭环的关键在"档"：没有带证据的 review.md，评审等于没评、没追溯</text>
<rect x="40" y="104" width="269" height="128" rx="10" fill="#475569" opacity="0.12" stroke="#64748b" stroke-width="1.6"/>
<text x="174.5" y="131" text-anchor="middle" fill="#cbd5e1" font-size="15" font-weight="700">进入评审</text>
<text x="174.5" y="156" text-anchor="middle" fill="#94a3b8" font-size="11">status=reviewing</text>
<line x1="309" y1="168.0" x2="355" y2="168.0" stroke="#475569" stroke-width="3" marker-end="url(#arq35)"/>
<rect x="355" y="104" width="269" height="128" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="489.5" y="131" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">Spec 报告</text>
<text x="489.5" y="156" text-anchor="middle" fill="#94a3b8" font-size="11">问题+证据</text>
<line x1="624" y1="168.0" x2="670" y2="168.0" stroke="#475569" stroke-width="3" marker-end="url(#arq35)"/>
<rect x="670" y="104" width="269" height="128" rx="10" fill="#8b5cf6" opacity="0.12" stroke="#a78bfa" stroke-width="1.6"/>
<text x="804.5" y="131" text-anchor="middle" fill="#c4b5fd" font-size="15" font-weight="700">Standards 报告</text>
<text x="804.5" y="156" text-anchor="middle" fill="#94a3b8" font-size="11">问题+证据</text>
<rect x="40" y="316" width="269" height="128" rx="10" fill="#f59e0b" opacity="0.12" stroke="#fbbf24" stroke-width="1.6"/>
<text x="174.5" y="343" text-anchor="middle" fill="#fcd34d" font-size="15" font-weight="700">判定</text>
<text x="174.5" y="368" text-anchor="middle" fill="#94a3b8" font-size="11">0 严重 → 推进 ci</text>
<text x="174.5" y="386" text-anchor="middle" fill="#94a3b8" font-size="11">有严重 → 退回 coding</text>
<line x1="309" y1="380.0" x2="355" y2="380.0" stroke="#475569" stroke-width="3" marker-end="url(#arq35)"/>
<rect x="355" y="316" width="269" height="128" rx="10" fill="#ef4444" opacity="0.12" stroke="#f87171" stroke-width="1.6"/>
<text x="489.5" y="343" text-anchor="middle" fill="#fca5a5" font-size="15" font-weight="700">review.md</text>
<text x="489.5" y="368" text-anchor="middle" fill="#94a3b8" font-size="11">修复任务单</text>
<text x="489.5" y="386" text-anchor="middle" fill="#94a3b8" font-size="11">逐条对应修什么</text>
<line x1="624" y1="380.0" x2="670" y2="380.0" stroke="#475569" stroke-width="3" marker-end="url(#arq35)"/>
<rect x="670" y="316" width="269" height="128" rx="10" fill="#10b981" opacity="0.12" stroke="#34d399" stroke-width="1.6"/>
<text x="804.5" y="343" text-anchor="middle" fill="#6ee7b7" font-size="15" font-weight="700">归档留档</text>
<text x="804.5" y="368" text-anchor="middle" fill="#94a3b8" font-size="11">复盘可答"当时为什么改"</text>
<path d="M 489.5 232 V 270 H 129.7 V 316" fill="none" stroke="#475569" stroke-width="3" marker-end="url(#arq35)"/>
<path d="M 804.5 232 V 292 H 219.3 V 316" fill="none" stroke="#475569" stroke-width="3" marker-end="url(#arq35)"/>
</svg>

**🗣️ 参考回答示范**：“评审的产出是 review.md，结构上是双轴分开的两份报告：Spec 轴报告 + Standards 轴报告，各自列问题，不合并不排序——这是为了不让一个轴的问题被另一个轴掩盖。问题分级两种：🔴 严重（功能缺失、scope creep、安全漏洞、架构腐化），🟡 建议。闭环是这样：评审结论直接驱动状态转移——两轴都 0 个 🔴，change 状态从 reviewing 推进到 ci，进入机器门禁；只要任一轴有 🔴，就退回编码阶段，review.md 就是修复任务单，逐条对应代码修什么；🟡 记入报告不阻塞放行。而且 review.md 会归档留档，复盘时能回答‘当时为什么这么改’。所以评审是评、档、推三件事：结论如实记，问题带证据，状态按结论走。”

**🌟 加分点**：
- 说出“双轴报告不合并”的目的（防掩盖），呼应 Q6——两处答案互相咬合，展现体系自洽。
- 说出 🔴 的具体类型（功能缺失/scope creep/安全漏洞/架构腐化），避免只背“严重”两个字。

**🛡️ 追问防御链**：
- **追问“评审通过但上生产出问题，谁负责？”** → 责任在流程而非个人：review.md 留档了当时通过的依据，问题是门禁没覆盖的维度暴露了，退回补充门禁或维度——这正好体现“可追溯”。
- **追问“评审会不会只走过场？”** → 有防走过场机制：评审必须引用证据（行号+规则），🟡 与 🔴 分级，且结果驱动状态迁移——走不了过场，走场会直接体现在状态停滞上。

---

### 4.6 方向六：LLM 应用与 AI Agent（6 题）

**Q36. Owner Agent 是什么？它和普通 AI 助手的本质区别是什么？（Agent 设计）**

**🎯 面试官想考什么**：现在人人都在做 Agent，考你是不是真理解“Agent vs 助手”的分界——不是会调工具就叫 Agent。

**🧭 答题框架（PREP）**
- **P**：Owner Agent 是“应用负责人”的 AI 化身——持有项目身份、规则、技能、上下文，编排 6 阶段流水线。
- **R**：和普通助手的区别在五个维度：**身份**（代表这个项目）、**规则**（持有项目规则）、**流程**（按状态机走）、**追溯**（产物留档）、**术语**（统一领域语言）。
- **E**：它由 apply-harness 根据检测出的语言/框架**参数化渲染生成**（`.harness/agents/owner.md`），不是通用大模型默认人格。
- **P**：一句话——**普通助手是“会答问题”，Owner Agent 是“替项目干活”**：前者有问才动，后者有责任、有流程、有产出。



<svg xmlns="http://www.w3.org/2000/svg" width="980" height="320" viewBox="0 0 980 320">
  <defs>
    <linearGradient id="bgq36" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arq36" markerWidth="9" markerHeight="9" refX="8" refY="4.5" orient="auto">
      <path d="M 0 0 L 9 4.5 L 0 9 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="980" height="320" fill="url(#bgq36)"/>
  <text x="490" y="42" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">图 36 · Owner Agent 五维：替项目干活</text>
  <text x="490" y="292" text-anchor="middle" fill="#475569" font-size="16">普通助手有问才答，Owner Agent 替项目干活</text>
<rect x="40" y="112" width="143" height="118" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="111.5" y="139" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">身份</text>
<text x="111.5" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">owner.md 知道项目是干什么的</text>
<line x1="183" y1="171.0" x2="229" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq36)"/>
<rect x="229" y="112" width="143" height="118" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="300.5" y="139" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">规则</text>
<text x="300.5" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">rules/ 知道什么能做</text>
<line x1="372" y1="171.0" x2="418" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq36)"/>
<rect x="418" y="112" width="143" height="118" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="489.5" y="139" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">流程</text>
<text x="489.5" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">状态机知道该干什么</text>
<line x1="561" y1="171.0" x2="607" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq36)"/>
<rect x="607" y="112" width="143" height="118" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="678.5" y="139" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">追溯</text>
<text x="678.5" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">changes/ 知道改过什么</text>
<line x1="750" y1="171.0" x2="796" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq36)"/>
<rect x="796" y="112" width="143" height="118" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="867.5" y="139" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">术语</text>
<text x="867.5" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">领域语言不跑偏</text>
</svg>

**🗣️ 参考回答示范**：“Owner Agent 是‘应用负责人’的 AI 化身，它是被安装出来的：apply-harness 检测出项目语言框架后，用参数渲染生成 `.harness/agents/owner.md`，这把项目的身份、规则、技能清单、上下文全部固化进去。和普通 AI 助手的本质区别我归纳成五个维度：身份——它知道自己是这个项目的负责人，不是万能助手；规则——它持有 `.harness/rules/` 的五条规则，所有产出都必须对齐；流程——它按变更状态机推进，不跳步；追溯——每个阶段都留档（change.md/review.md/verify.md），可复盘；术语——它统一使用项目的领域语言，不会随口换词。普通助手是‘有问才答’，Owner Agent 是‘替项目干活’——有目标、有约束、有产出、有记录。”

**🌟 加分点**：强调“被参数化渲染生成”这个细节——Owner Agent 不是写死的人格，而是随项目变化的配置，这决定了它能复制到任意项目。

**🛡️ 追问防御链**：追问“换个项目，Owner Agent 会变吗？” → 会，整个 `.harness/` 都是渲染产物：换语言参数表就变，Owner Agent 的规则和技能也跟着变——这正是它能落地的原因。

---

**Q37. Agent 的上下文是怎么管理的？（上下文工程）**

**🎯 面试官想考什么**：Agent 工程的核心难点是上下文——考你有没有把“什么时候加载什么”当成一个设计问题，而不是把所有内容都塞给模型。

**🧭 答题框架（PREP）**
- **P**：按需加载、阶段切片：每个阶段只加载对应技能 + 必要产物，不把全部上下文一次灌入。
- **R**：模型上下文有上限，全量灌入既浪费又稀释注意力——上下文质量比数量重要。
- **E**：coding-skill 阶段加载变更目录 + 编码规范 + 对应技能；评审阶段加载 change.md + review 维度；产物各自落盘，跨阶段只传引用。
- **P**：上下文管理原则——**最小必要上下文 + 产物留档**：一次只给模型做这件事所需的信息。



<svg xmlns="http://www.w3.org/2000/svg" width="980" height="320" viewBox="0 0 980 320">
  <defs>
    <linearGradient id="bgq37" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arq37" markerWidth="9" markerHeight="9" refX="8" refY="4.5" orient="auto">
      <path d="M 0 0 L 9 4.5 L 0 9 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="980" height="320" fill="url(#bgq37)"/>
  <text x="490" y="42" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">图 37 · 上下文管理：省 token 的四板斧</text>
  <text x="490" y="292" text-anchor="middle" fill="#475569" font-size="16">不把整个技能库塞进上下文</text>
<rect x="40" y="112" width="190" height="118" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="135.0" y="139" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">按需加载</text>
<text x="135.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">用哪个技能装哪个</text>
<line x1="230" y1="171.0" x2="276" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq37)"/>
<rect x="276" y="112" width="190" height="118" rx="10" fill="#8b5cf6" opacity="0.12" stroke="#a78bfa" stroke-width="1.6"/>
<text x="371.0" y="139" text-anchor="middle" fill="#c4b5fd" font-size="15" font-weight="700">阶段切片</text>
<text x="371.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">只看本阶段所需</text>
<line x1="466" y1="171.0" x2="512" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq37)"/>
<rect x="512" y="112" width="190" height="118" rx="10" fill="#f59e0b" opacity="0.12" stroke="#fbbf24" stroke-width="1.6"/>
<text x="607.0" y="139" text-anchor="middle" fill="#fcd34d" font-size="15" font-weight="700">最小集</text>
<text x="607.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">只传必要文件</text>
<line x1="702" y1="171.0" x2="748" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq37)"/>
<rect x="748" y="112" width="190" height="118" rx="10" fill="#10b981" opacity="0.12" stroke="#34d399" stroke-width="1.6"/>
<text x="843.0" y="139" text-anchor="middle" fill="#6ee7b7" font-size="15" font-weight="700">只传引用</text>
<text x="843.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">跨阶段不重复喂</text>
</svg>

**🗣️ 参考回答示范**：“上下文管理的核心是按需加载、阶段切片。模型上下文是稀缺资源，全量灌入不仅浪费，还会稀释模型对关键信息的注意力。我的做法是：每个阶段只加载完成本阶段任务所需的最小集——编码阶段加载对应 change.md 和编码规范，评审阶段加载 change.md 和评审清单，测试阶段加载 AC 和边界；各阶段产物独立落盘（change.md、review.md、verify.md），跨阶段只传引用不传全文。这样每个阶段的模型看到的都是‘此刻该看的’。”

**🌟 加分点**：点出“产物落盘 = 上下文的外部化”——把上下文从模型窗口转移到文件系统，窗口只留当下，这是多步骤 Agent 工程的标准解。

**🛡️ 追问防御链**：追问“落盘会不会信息不同步？” → 变更状态机保证任意时刻只有唯一合法的‘当前产物’（change.md 是真相源），读它永远是最新且正确的；人工改动也走同一份文件。

---

**Q38. 这套项目里，LLM 用在哪些环节？哪些环节坚决不用？（LLM 边界）**

**🎯 面试官想考什么**：考“该用 AI 的地方用 AI、不该用 AI 的地方绝不用”——这是 AI 工程面试的高频观察点。

**🧭 答题框架（PREP）**
- **P**：用 LLM 的：需求澄清（harnessing 拷问式对话）、PRD 解析、代码生成、评审执行、部署验证脚本解读；不用的：模板渲染、检测、状态推进、门禁判定。
- **R**：分界线是**“是否需要理解语义”**：要理解语义才值得付 LLM 的代价（慢/贵/不稳定）；纯规则问题用确定性算法，快、稳、可测试。
- **E**：渲染是确定性的（纯模板替换）、状态推进是硬逻辑、门禁是脚本判定——它们要是用 LLM，等于把可靠的东西变得不可靠。
- **P**：所以我的原则是——**LLM 是系统的“语义引擎”，不是“万能执行器”**。



<svg xmlns="http://www.w3.org/2000/svg" width="980" height="420" viewBox="0 0 980 482.0">
  <defs>
    <linearGradient id="bgq38" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arq38" markerWidth="9" markerHeight="9" refX="8" refY="4.5" orient="auto">
      <path d="M 0 0 L 9 4.5 L 0 9 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="980" height="482.0" fill="url(#bgq38)"/>
  <text x="490" y="42" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">图 38 · LLM 边界：语义理解用，确定性不用</text>
  <text x="490" y="464" text-anchor="middle" fill="#475569" font-size="16">选择标准可直接答面试：凡是不确定性会变成 bug 的地方，都不碰 LLM</text>
<rect x="40" y="104" width="427" height="128" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="253.5" y="131" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">用 LLM</text>
<text x="253.5" y="156" text-anchor="middle" fill="#94a3b8" font-size="11">需求澄清 · PRD 解析</text>
<text x="253.5" y="174" text-anchor="middle" fill="#94a3b8" font-size="11">代码生成 · 评审</text>
<line x1="467" y1="168.0" x2="513" y2="168.0" stroke="#475569" stroke-width="3" marker-end="url(#arq38)"/>
<rect x="513" y="104" width="427" height="128" rx="10" fill="#ef4444" opacity="0.12" stroke="#f87171" stroke-width="1.6"/>
<text x="726.5" y="131" text-anchor="middle" fill="#fca5a5" font-size="15" font-weight="700">不用 LLM</text>
<text x="726.5" y="156" text-anchor="middle" fill="#94a3b8" font-size="11">模板渲染 · 自动检测</text>
<text x="726.5" y="174" text-anchor="middle" fill="#94a3b8" font-size="11">状态机 · 门禁</text>
<rect x="40" y="316" width="900" height="128" rx="10" fill="#f59e0b" opacity="0.12" stroke="#fbbf24" stroke-width="1.6"/>
<text x="490.0" y="343" text-anchor="middle" fill="#fcd34d" font-size="15" font-weight="700">原则</text>
<text x="490.0" y="368" text-anchor="middle" fill="#94a3b8" font-size="11">需要理解语义才用 LLM</text>
<text x="490.0" y="386" text-anchor="middle" fill="#94a3b8" font-size="11">需要确定性坚决不用</text>
<line x1="253.5" y1="232" x2="340.0" y2="316" stroke="#475569" stroke-width="3" marker-end="url(#arq38)"/>
<line x1="726.5" y1="232" x2="640.0" y2="316" stroke="#475569" stroke-width="3" marker-end="url(#arq38)"/>
</svg>

**🗣️ 参考回答示范**：“我把 LLM 的用法画了一条很清楚的分界线：**需要理解语义的环节用，有确定答案的环节坚决不用**。用的地方：需求澄清（harnessing 是一问一答的拷问式对话，需要理解人在说什么）、PRD 解析（从自然语言抽结构化）、代码生成（写实现）、评审（读代码找问题）。不用的地方：模板渲染（纯字符串替换）、语言/框架检测（查特征表）、状态机推进（硬逻辑）、门禁判定（跑脚本）。为什么？因为 LLM 慢、贵、不稳定——把它用在有确定答案的地方，等于把可靠的东西变得不可靠，还要为它买单。所以我说 LLM 是系统的‘语义引擎’，不是万能执行器。”

**🌟 加分点**：用“两条路径”总结（确定性路径 vs 语义路径），并说出二者的接口边界（渲染产出的规则文件 → LLM 执行的输入），展示架构的清爽。

**🛡️ 追问防御链**：追问“评审这种高质量要求的工作，AI 能胜任吗？” → 评审不是‘让 AI 自由打分’，是让 AI 执行固定检查清单并引用证据——重要的是清单和证据，不是 AI 的聪明；而且结果可人工复核。

---

**Q39. 为什么 PRD 解析平台要把 AI 做成“可关闭”的？（降级设计）**

**🎯 面试官想考什么**：考你对“依赖 AI 的系统”的工程认知——是否理解 AI 是不可控的外部依赖，必须留后路。

**🧭 答题框架（PREP）**
- **P**：AI（LLM 解析）和 MinIO（对象存储）都是可开关组件，关闭后平台走降级模式，功能不瘫痪。
- **R**：因为 AI 服务可能超时、限流、涨价、停服，把核心链路绑死在外部依赖上是事故源头。
- **E**：AI 关闭后，PRD 解析落到“章节层级算法”分支，仍是可用的解析路径；模板匹配本来就不依赖 AI。
- **P**：设计原则——**AI 是增强件，不是必需品**；主链路必须能在没有 AI 时运行。



<svg xmlns="http://www.w3.org/2000/svg" width="980" height="420" viewBox="0 0 980 482.0">
  <defs>
    <linearGradient id="bgq39" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arq39" markerWidth="9" markerHeight="9" refX="8" refY="4.5" orient="auto">
      <path d="M 0 0 L 9 4.5 L 0 9 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="980" height="482.0" fill="url(#bgq39)"/>
  <text x="490" y="42" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">图 39 · AI 可关闭：降级设计</text>
  <text x="490" y="464" text-anchor="middle" fill="#475569" font-size="16">面试答"AI 挂了怎么办"：平台不是 AI 的壳，核心流程永远有非 AI 兜底</text>
<rect x="40" y="104" width="427" height="128" rx="10" fill="#475569" opacity="0.12" stroke="#64748b" stroke-width="1.6"/>
<text x="253.5" y="131" text-anchor="middle" fill="#cbd5e1" font-size="15" font-weight="700">AI 解析</text>
<text x="253.5" y="156" text-anchor="middle" fill="#94a3b8" font-size="11">可关闭 · 可降级</text>
<text x="253.5" y="174" text-anchor="middle" fill="#94a3b8" font-size="11">超时/限流自动切换</text>
<line x1="467" y1="168.0" x2="513" y2="168.0" stroke="#475569" stroke-width="3" marker-end="url(#arq39)"/>
<rect x="513" y="104" width="427" height="128" rx="10" fill="#f59e0b" opacity="0.12" stroke="#fbbf24" stroke-width="1.6"/>
<text x="726.5" y="131" text-anchor="middle" fill="#fcd34d" font-size="15" font-weight="700">章节层级算法</text>
<text x="726.5" y="156" text-anchor="middle" fill="#94a3b8" font-size="11">兜底分支</text>
<text x="726.5" y="174" text-anchor="middle" fill="#94a3b8" font-size="11">准确率略低但稳</text>
<rect x="40" y="316" width="900" height="128" rx="10" fill="#10b981" opacity="0.12" stroke="#34d399" stroke-width="1.6"/>
<text x="490.0" y="343" text-anchor="middle" fill="#6ee7b7" font-size="15" font-weight="700">核心链路</text>
<text x="490.0" y="368" text-anchor="middle" fill="#94a3b8" font-size="11">拆文档/分章节/出 Change 不断</text>
<line x1="253.5" y1="232" x2="340.0" y2="316" stroke="#475569" stroke-width="3" marker-end="url(#arq39)"/>
<line x1="726.5" y1="232" x2="640.0" y2="316" stroke="#475569" stroke-width="3" marker-end="url(#arq39)"/>
</svg>

**🗣️ 参考回答示范**：“把 AI 做成可关闭，是因为我把它当成‘外部依赖’来看待：它会超时、限流、涨价，甚至停服。如果把核心链路绑死在 AI 上，AI 挂了平台就瘫了。所以设计上 AI 解析和 MinIO 都是可开关组件：AI 关闭后，解析走三层策略里的‘章节层级算法’分支，虽然不如 LLM 解析智能，但核心功能——拆文档、分章节、出 Change——依然可用。三层混合策略本身就是为了这个：模板匹配不依赖 AI，是第一层；LLM 解析是增强，第二层；算法兜底，第三层。平台的价值不建立在 AI 的可用性之上，这就是降级设计的意义。”

**🌟 加分点**：说出“三层混合”本身就是“按 AI 可用性分级”的结构——每降一层功能少一点但不会断，这比二值思维（有 AI/没 AI）更精细。

**🛡️ 追问防御链**：追问“降级模式质量会不会太低？” → 会降，但降的不是“可用性”而是“解析质量”；模板匹配+算法分支对结构规范的 PRD 文档仍然准确，真正费劲的是野文档——这正是 LLM 该上的场景。

---

**Q40. ChangeComposer 合成 Change 时，为什么“推导不出就返回空值，从不编造”？（确定性设计）**

**🎯 面试官想考什么**：考你面对“信息不全”时的系统设计立场——AI 项目里“编造”是头号事故源，看你会不会用结构性手段封死它。

**🧭 答题框架（PREP）**
- **P**：ChangeComposer 是纯工具类，8 个正交模块分别独立推导，推导不出就返回空值（null/空列表），**绝不编造**。
- **R**：因为编造的内容没有出处，进入规格后无人知道它从哪来，评审无法核对——一条无出处的 AC 比缺失的 AC 更危险。
- **E**：每个合成条目都带出处（REQ 编号/接口路径/架构决策标题），空值意味着“该模块本次不给结论”，宁可缺失也不造假。
- **P**：所以这里的设计哲学是**宁缺毋滥**：规格可以暂时不全，但绝不能有假。



<svg xmlns="http://www.w3.org/2000/svg" width="980" height="320" viewBox="0 0 980 320">
  <defs>
    <linearGradient id="bgq40" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arq40" markerWidth="9" markerHeight="9" refX="8" refY="4.5" orient="auto">
      <path d="M 0 0 L 9 4.5 L 0 9 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="980" height="320" fill="url(#bgq40)"/>
  <text x="490" y="42" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">图 40 · ChangeComposer：推导不出就返回空值</text>
  <text x="490" y="292" text-anchor="middle" fill="#475569" font-size="16">从不编造，每条目带出处</text>
<rect x="40" y="112" width="190" height="118" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="135.0" y="139" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">8 正交模块</text>
<text x="135.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">功能/技术/测试/操作</text>
<line x1="230" y1="171.0" x2="276" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq40)"/>
<rect x="276" y="112" width="190" height="118" rx="10" fill="#ef4444" opacity="0.12" stroke="#f87171" stroke-width="1.6"/>
<text x="371.0" y="139" text-anchor="middle" fill="#fca5a5" font-size="15" font-weight="700">推导不出</text>
<text x="371.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">返回空值</text>
<line x1="466" y1="171.0" x2="512" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq40)"/>
<rect x="512" y="112" width="190" height="118" rx="10" fill="#10b981" opacity="0.12" stroke="#34d399" stroke-width="1.6"/>
<text x="607.0" y="139" text-anchor="middle" fill="#6ee7b7" font-size="15" font-weight="700">不编造</text>
<text x="607.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">宁缺毋滥</text>
<line x1="702" y1="171.0" x2="748" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq40)"/>
<rect x="748" y="112" width="190" height="118" rx="10" fill="#8b5cf6" opacity="0.12" stroke="#a78bfa" stroke-width="1.6"/>
<text x="843.0" y="139" text-anchor="middle" fill="#c4b5fd" font-size="15" font-weight="700">带出处进 Change</text>
<text x="843.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">每字段可追溯</text>
</svg>

**🗣️ 参考回答示范**：“ChangeComposer 把 Change 的合成拆成 8 个正交模块，每个模块独立推导自己的那部分内容。设计上有一条铁律：**推导不出就返回空值，绝不编造**。为什么这么极端？因为编造的内容没有出处——它是模型自己脑补的，进了 Change 之后没人知道它依据什么，评审时无法核对，等于在规格里埋了一颗无引信的雷。相比之下，返回空值只是‘这一块暂时没结论’，至少诚实，可以人工补。为了让这条纪律可执行，每个合成条目都带出处：需求条目引 REQ 编号，接口条目引接口路径，架构条目引决策标题——有出处才进 Change，没出处就留空。这是‘宁缺毋滥’：规格可以暂时不全，但不能有假。”

**🌟 加分点**：把“带出处”和“空值不编造”连起来讲——出处机制让“不编造”可验证，而不是只靠模型自觉。

**🛡️ 追问防御链**：追问“8 个模块正交是什么意思？” → 模块之间互不依赖、各管一段（需求/接口/边界/测试策略等），任何一个模块失败不影响其他模块产出——正交让失败局部化，也让产物可追溯。

---

**Q41. PRD 解析平台的“三层混合策略”是怎么设计的？（⚡最亮机制 · 必考 · 完整展开）**

**🎯 面试官想考什么**：这是“把 LLM 用在生产”的完整案例——面试官想听的不是“我们调了 API”，而是你如何把“模型不可靠”变成一个可治理的问题。

**🧭 答题框架（PREP）**
- **P**：三层按序回退：**模板匹配 → LLM 解析 → 章节层级算法（兜底）**，每一层失败/不准就降一层。
- **R**：为什么三层？因为 PRD 文档有两类——规范模板产出的（机器可解析）和野文档（只能靠语义），单一策略必然两头不讨好。
- **E**：模板匹配最准最快（构造型文档命中率最高）；命中不了进 LLM 语义解析；LLM 不可用或超时才落到算法兜底，保证功能不断。
- **P**：三层设计的本质——**把文档质量映射到解析成本与可靠性**：越规范的文档越便宜越可靠，越野的文档代价越高。



<svg xmlns="http://www.w3.org/2000/svg" width="980" height="420" viewBox="0 0 980 482.0">
  <defs>
    <linearGradient id="bgq41" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arq41" markerWidth="9" markerHeight="9" refX="8" refY="4.5" orient="auto">
      <path d="M 0 0 L 9 4.5 L 0 9 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="980" height="482.0" fill="url(#bgq41)"/>
  <text x="490" y="42" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">图 41 · PRD 三层混合解析：越规范越便宜</text>
  <text x="490" y="464" text-anchor="middle" fill="#475569" font-size="16">三层路径 = 三档成本：模板最便宜、LLM 次之、算法兜底最稳，越规范越靠前</text>
<rect x="40" y="104" width="190" height="128" rx="10" fill="#475569" opacity="0.12" stroke="#64748b" stroke-width="1.6"/>
<text x="135.0" y="131" text-anchor="middle" fill="#cbd5e1" font-size="15" font-weight="700">PRD 文档</text>
<text x="135.0" y="156" text-anchor="middle" fill="#94a3b8" font-size="11">质量参差不齐</text>
<line x1="230" y1="168.0" x2="276" y2="168.0" stroke="#475569" stroke-width="3" marker-end="url(#arq41)"/>
<rect x="276" y="104" width="190" height="128" rx="10" fill="#10b981" opacity="0.12" stroke="#34d399" stroke-width="1.6"/>
<text x="371.0" y="131" text-anchor="middle" fill="#6ee7b7" font-size="15" font-weight="700">① 模板匹配</text>
<text x="371.0" y="156" text-anchor="middle" fill="#94a3b8" font-size="11">标准模板→直接解析</text>
<text x="371.0" y="174" text-anchor="middle" fill="#94a3b8" font-size="11">最快最准 · 零 AI</text>
<line x1="466" y1="168.0" x2="512" y2="168.0" stroke="#475569" stroke-width="3" marker-end="url(#arq41)"/>
<rect x="512" y="104" width="190" height="128" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="607.0" y="131" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">② LLM 语义解析</text>
<text x="607.0" y="156" text-anchor="middle" fill="#94a3b8" font-size="11">结构不标准→LLM</text>
<text x="607.0" y="174" text-anchor="middle" fill="#94a3b8" font-size="11">抽结构化字段</text>
<line x1="702" y1="168.0" x2="748" y2="168.0" stroke="#475569" stroke-width="3" marker-end="url(#arq41)"/>
<rect x="748" y="104" width="190" height="128" rx="10" fill="#f59e0b" opacity="0.12" stroke="#fbbf24" stroke-width="1.6"/>
<text x="843.0" y="131" text-anchor="middle" fill="#fcd34d" font-size="15" font-weight="700">③ 章节算法兜底</text>
<text x="843.0" y="156" text-anchor="middle" fill="#94a3b8" font-size="11">LLM 不可用→算法</text>
<text x="843.0" y="174" text-anchor="middle" fill="#94a3b8" font-size="11">保证链路不断</text>
<rect x="40" y="316" width="427" height="128" rx="10" fill="#8b5cf6" opacity="0.12" stroke="#a78bfa" stroke-width="1.6"/>
<text x="253.5" y="343" text-anchor="middle" fill="#c4b5fd" font-size="15" font-weight="700">4 文档拆分</text>
<text x="253.5" y="368" text-anchor="middle" fill="#94a3b8" font-size="11">功能/技术/测试/操作</text>
<text x="253.5" y="386" text-anchor="middle" fill="#94a3b8" font-size="11">加权评分定归属</text>
<line x1="467" y1="380.0" x2="513" y2="380.0" stroke="#475569" stroke-width="3" marker-end="url(#arq41)"/>
<rect x="513" y="316" width="427" height="128" rx="10" fill="#475569" opacity="0.12" stroke="#64748b" stroke-width="1.6"/>
<text x="726.5" y="343" text-anchor="middle" fill="#cbd5e1" font-size="15" font-weight="700">Change 合成</text>
<text x="726.5" y="368" text-anchor="middle" fill="#94a3b8" font-size="11">8 模块 · 带出处</text>
<path d="M 843 232 V 270 H 253.5 V 316" fill="none" stroke="#475569" stroke-width="3" marker-end="url(#arq41)"/>
</svg>

**🗣️ 参考回答示范**：“PRD 解析平台处理的是一个很现实的问题：文档质量参差不齐。所以我设计了三层混合策略，按序回退。第一层**模板匹配**：先用文档结构和关键词特征做判定——如果文档来自标准 PRD 模板，直接按模板字段解析，这一层最快最准，零 AI 成本。第二层**LLM 语义解析**：模板匹配不中的，说明文档结构不标准，交给 LLM 理解语义抽结构化字段。第三层**章节层级算法兜底**：LLM 不可用（超时/限流/关闭）时，退到基于章节标题层级和关键词的算法解析——准确率不如前两层，但保证核心链路不断。平台还配套了‘4 文档拆分’：用加权评分算法把解析结果分到功能/技术/测试/操作四类文档，每个文档类型一组关键词权重，得分最高的类型获胜。这个三层设计最得意的地方是：**文档越规范，走的路径越便宜越可靠；文档越野，代价越高但兜得住**——把不可控的 AI 风险，收敛成了可控的成本分级。”

**🌟 加分点**：
- 说出后端的工程细节（Spring Boot 3.4 + JDK21、12 个 REST 控制器 + 8 个业务服务、前端 Vue3+TS+Pinia 7 页面），证明是真实现不是 PPT。
- 点出“三层回退”与“AI 可关闭”是同一个降级哲学的两次落地。

**🛡️ 追问防御链**：
- **追问“模板匹配的准确率怎么保证？”** → 模板匹配是确定性规则（结构+关键词），可单测；而且命中与否有明确判定条件，不命中就降级，不会把误匹配当成功。
- **追问“LLM 解析的结果可信吗？”** → 解析输出全部落到可人工校验的界面（看板/文档详情），平台强调“人人可校验”；LLM 只负责提稿，人负责定稿。

---

### 4.7 方向七：工程化与可扩展（5 题）

**Q42. 支持了哪 5 种语言？为什么是这 5 种？（语言包矩阵）**

**🎯 面试官想考什么**：考你“选型”的依据——是拍脑袋凑数，还是按工程逻辑挑选。

**🧭 答题框架（PREP）**
- **P**：5 种：Golang、Java、Python、Rust、Frontend（Vue3+Vite/React/Next.js/Angular/Svelte/Nuxt 等）。
- **R**：判断“值不值得单独做语言包”的两个标准：① 市场主流（装的人多）；② 工程形态差异大（构建/测试/规范体系完全不同，模板化共担不了）。
- **E**：Go 的构建测试范式与 Java 完全不同，必须独立语言包；而同为前端，Vue/React/Next 只是框架差异，走“框架差异块”就能覆盖，不用分别造包。
- **P**：所以语言包矩阵的扩张逻辑是——**差异到“构建范式”级别才造包，差异只在“框架”级别就填参数**。



<svg xmlns="http://www.w3.org/2000/svg" width="980" height="420" viewBox="0 0 980 482.0">
  <defs>
    <linearGradient id="bgq42" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arq42" markerWidth="9" markerHeight="9" refX="8" refY="4.5" orient="auto">
      <path d="M 0 0 L 9 4.5 L 0 9 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="980" height="482.0" fill="url(#bgq42)"/>
  <text x="490" y="42" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">图 42 · 为什么是这 5 种语言</text>
  <text x="490" y="464" text-anchor="middle" fill="#475569" font-size="16">造包 vs 填参的区别就是"重写还是配置"：范式不同才造包，框架不同只填参</text>
<rect x="40" y="104" width="427" height="128" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="253.5" y="131" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">5 种语言</text>
<text x="253.5" y="156" text-anchor="middle" fill="#94a3b8" font-size="11">Go · Java · Python</text>
<text x="253.5" y="174" text-anchor="middle" fill="#94a3b8" font-size="11">Rust · Frontend</text>
<line x1="467" y1="168.0" x2="513" y2="168.0" stroke="#475569" stroke-width="3" marker-end="url(#arq42)"/>
<rect x="513" y="104" width="427" height="128" rx="10" fill="#8b5cf6" opacity="0.12" stroke="#a78bfa" stroke-width="1.6"/>
<text x="726.5" y="131" text-anchor="middle" fill="#c4b5fd" font-size="15" font-weight="700">框架差异</text>
<text x="726.5" y="156" text-anchor="middle" fill="#94a3b8" font-size="11">120+ 框架用参数区分</text>
<text x="726.5" y="174" text-anchor="middle" fill="#94a3b8" font-size="11">Vue/React 只需差异块</text>
<rect x="40" y="316" width="900" height="128" rx="10" fill="#10b981" opacity="0.12" stroke="#34d399" stroke-width="1.6"/>
<text x="490.0" y="343" text-anchor="middle" fill="#6ee7b7" font-size="15" font-weight="700">两个标准</text>
<text x="490.0" y="368" text-anchor="middle" fill="#94a3b8" font-size="11">真实用户 + 范式级差异</text>
<text x="490.0" y="386" text-anchor="middle" fill="#94a3b8" font-size="11">前端是同一工程形态</text>
<line x1="253.5" y1="232" x2="340.0" y2="316" stroke="#475569" stroke-width="3" marker-end="url(#arq42)"/>
<line x1="726.5" y1="232" x2="640.0" y2="316" stroke="#475569" stroke-width="3" marker-end="url(#arq42)"/>
</svg>

**🗣️ 参考回答示范**：“目前 5 种：Golang、Java、Python、Rust 和 Frontend。为什么是这几种不是拍脑袋——两个标准：一是有真实用户（市场主流），二是**工程形态差异足够大**，大到一个模板包不住。Java 的 Maven/Gradle、构建-测试-QA 流程跟 Go 的 go build/go test 体系完全是两套范式，所以必须各自独立包；而前端的 Vue、React、Next.js，本质是同一套前端工程形态（npm 生态、Vite/Webpack 构建）下的框架差异，所以只需要在参数表里用‘框架差异块’区分，不用给每个框架单独造包。这个划分的收益是：语言包数量收敛到 5，而框架覆盖能做到 80+——靠‘范式级差异造包、框架级差异填参数’的分层。”

**🌟 加分点**：主动说出“Frontend 算一类语言包”的取舍（前端生态统一为 npm 工程形态），展示你对包边界的思考。

**🛡️ 追问防御链**：追问“Rust 用户少，为什么也做了？” → 不看用户数看范式差异：Rust 的 cargo 体系、所有权模型、测试范式与其它 5 种完全不同，不造包就永远支持不了；Rust 是‘范式差异’的典型。

---

**Q43. 技能仓库怎么维护：改一处、全语言生效是怎么做到的？（版本管理）**

**🎯 面试官想考什么**：多语言仓库最怕“改 6 次、漏 3 次”——考你有没有想过公共代码的同步问题。

**🧭 答题框架（PREP）**
- **P**：靠“公共部分只存一份 + 渲染时分发”：公共技能/规则放 harness-core，只维护一份；语言包只存差异；apply-harness 安装时把模板技能渲染进目标项目。
- **R**：如果公共逻辑在 6 个语言包里各存一份，改一次要同步 6 处，必然漏——这是复制粘贴方案的死穴。
- **E**：SDD-TDD、开发流程、运行时可靠性这些规则在 harness-core 只有一份；dreaming差异只发生在参数表。
- **P**：同步问题的答案不是“记得同步”，而是**结构上就不存在第二份**。



<svg xmlns="http://www.w3.org/2000/svg" width="980" height="320" viewBox="0 0 980 320">
  <defs>
    <linearGradient id="bgq43" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arq43" markerWidth="9" markerHeight="9" refX="8" refY="4.5" orient="auto">
      <path d="M 0 0 L 9 4.5 L 0 9 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="980" height="320" fill="url(#bgq43)"/>
  <text x="490" y="42" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">图 43 · 公共只存一份：改一处全语言生效</text>
  <text x="490" y="292" text-anchor="middle" fill="#475569" font-size="16">正确答案不是"记得同步"，而是从结构上消灭多份</text>
<rect x="40" y="112" width="190" height="118" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="135.0" y="139" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">harness-core 公共一份</text>
<text x="135.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">SDD-TDD/流程/可靠性</text>
<line x1="230" y1="171.0" x2="276" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq43)"/>
<rect x="276" y="112" width="190" height="118" rx="10" fill="#8b5cf6" opacity="0.12" stroke="#a78bfa" stroke-width="1.6"/>
<text x="371.0" y="139" text-anchor="middle" fill="#c4b5fd" font-size="15" font-weight="700">安装时渲染分发</text>
<text x="371.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">apply-harness 按语言渲染</text>
<line x1="466" y1="171.0" x2="512" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq43)"/>
<rect x="512" y="112" width="190" height="118" rx="10" fill="#10b981" opacity="0.12" stroke="#34d399" stroke-width="1.6"/>
<text x="607.0" y="139" text-anchor="middle" fill="#6ee7b7" font-size="15" font-weight="700">语言包只留差异</text>
<text x="607.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">语言特有部分</text>
<line x1="702" y1="171.0" x2="748" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq43)"/>
<rect x="748" y="112" width="190" height="118" rx="10" fill="#f59e0b" opacity="0.12" stroke="#fbbf24" stroke-width="1.6"/>
<text x="843.0" y="139" text-anchor="middle" fill="#fcd34d" font-size="15" font-weight="700">改一次全局生效</text>
<text x="843.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">不需要六次编辑六次评审</text>
</svg>

**🗣️ 参考回答示范**：“改一处全语言生效，靠的是‘公共只存一份’的结构，而不是‘记得同步六份’。公共的东西——SDD-TDD 规则、开发流程、运行时可靠性、模板化技能——全部只放在 harness-core，全仓库只有一份；语言包和用户项目里不存在它的副本。install 时由 apply-harness 把公共技能模板渲染成各语言的版本。这样改公共逻辑一次就好，语言包只维护语言特有部分。这也解释了为什么我们不做复制粘贴——复制粘贴的问题不是‘容易漏’，而是‘结构上存在多份’，每次改动都是六次编辑六次评审；现在结构上就只有一份，同步问题从根上消失了。”

**🌟 加分点**：把“避免重复”上升为“结构决策”——问题的正确答案不是流程（记得同步），而是结构（不存在多份）。

**🛡️ 追问防御链**：追问“渲染出去的副本被用户改了，怎么同步新版？” → 渲染产物在项目 `.harness/` 里，升级时 apply-harness 重新渲染覆盖即可；用户自定义改动分开存放，不阻断覆盖。

---

**Q44. 39 个技能包是怎么管理的？为什么强调每个技能“单一职责”？（工程治理）**

**🎯 面试官想考什么**：考你对“技能库”这种特殊代码库的治理意识——不是写完就完事，要能长期维护。

**🧭 答题框架（PREP）**
- **P**：三层治理：模板化技能（5）、通用技能（3）、跨语言组件（13）+ 语言包专属技能，共 39 个 SKILL.md。
- **R**：单一职责的必要性：技能是“按任务装载”的，职责混在一起，一个任务加载的技能就会携带无关内容——浪费上下文、增加冲突面。
- **E**：写日志、缓存封装、安全扫描拆成独立组件技能，代码里检测到对应特征才加载，其余场景不产生负担。
- **P**：所以技能库的管理原则——**小而专、按需载、可测试**：每个技能是一份完整可测的 playbook，而不是一个文档合集。



<svg xmlns="http://www.w3.org/2000/svg" width="980" height="420" viewBox="0 0 980 482.0">
  <defs>
    <linearGradient id="bgq44" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arq44" markerWidth="9" markerHeight="9" refX="8" refY="4.5" orient="auto">
      <path d="M 0 0 L 9 4.5 L 0 9 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="980" height="482.0" fill="url(#bgq44)"/>
  <text x="490" y="42" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">图 44 · 39 个技能包 + 单一职责</text>
  <text x="490" y="464" text-anchor="middle" fill="#475569" font-size="16">一技能一闭环的代价是数量多，换来按任务精确装载，不把无关技能塞进上下文</text>
<rect x="40" y="104" width="427" height="128" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="253.5" y="131" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">四层结构</text>
<text x="253.5" y="156" text-anchor="middle" fill="#94a3b8" font-size="11">模板化5 · 通用3</text>
<text x="253.5" y="174" text-anchor="middle" fill="#94a3b8" font-size="11">组件13 · 语言包专属</text>
<line x1="467" y1="168.0" x2="513" y2="168.0" stroke="#475569" stroke-width="3" marker-end="url(#arq44)"/>
<rect x="513" y="104" width="427" height="128" rx="10" fill="#8b5cf6" opacity="0.12" stroke="#a78bfa" stroke-width="1.6"/>
<text x="726.5" y="131" text-anchor="middle" fill="#c4b5fd" font-size="15" font-weight="700">单一职责</text>
<text x="726.5" y="156" text-anchor="middle" fill="#94a3b8" font-size="11">一技能只闭环一个任务</text>
<text x="726.5" y="174" text-anchor="middle" fill="#94a3b8" font-size="11">编码/测试/评审各是各</text>
<rect x="40" y="316" width="900" height="128" rx="10" fill="#10b981" opacity="0.12" stroke="#34d399" stroke-width="1.6"/>
<text x="490.0" y="343" text-anchor="middle" fill="#6ee7b7" font-size="15" font-weight="700">按任务装载</text>
<text x="490.0" y="368" text-anchor="middle" fill="#94a3b8" font-size="11">出现 redis 特征才加载 redis 技能</text>
<text x="490.0" y="386" text-anchor="middle" fill="#94a3b8" font-size="11">不出现就不加载</text>
<line x1="253.5" y1="232" x2="340.0" y2="316" stroke="#475569" stroke-width="3" marker-end="url(#arq44)"/>
<line x1="726.5" y1="232" x2="640.0" y2="316" stroke="#475569" stroke-width="3" marker-end="url(#arq44)"/>
</svg>

**🗣️ 参考回答示范**：“技能库一共 39 个 SKILL.md，分四层：模板化技能 5 个（渲染进每种语言）、跨语言通用技能 3 个（直接复制）、跨语言封装组件 13 个（按代码特征按需加载）、语言包专属技能若干（比如 Java 的 spring-api-convention）。每个技能都强调单一职责——一个技能只闭环一个任务：编码、写测试、评审、迁移、诊断 bug 各是各的。为什么？因为技能是**按任务装载**的：职责混在一起，做一件事就要加载一堆无关内容，既浪费上下文又增加规则冲突面。把这些拆成独立组件后，代码里出现 redis 特征才加载 redis 封装技能，没出现就不加载——零负担。”

**🌟 加分点**：用“按需加载”解释组件技能的装载逻辑（检测代码特征触发），展示“39 个技能如何不稀释任何一次对话”。

**🛡️ 追问防御链**：追问“技能会不会相互矛盾？” → 有可能，但治理住了：通用规则只有一份（在 core）、语言特有规则在语言包、技能间职责正交；真冲突就改规则本身而非打补丁。

---

**Q45. 一套 Harness 是怎么装进一个项目的？完整流程是什么？（上手体验）**

**🎯 面试官想考什么**：考“第一公里”的体验设计——工具再好，装不上就没人用，面试官会看你对上手路径的打磨。

**🧭 答题框架（PREP）**
- **P**：两条命令：`npx skills@latest add` 安装技能包，然后运行 apply-harness 完成六步装配。
- **R**：体验原则：**零配置启动 + 全程可确认**——检测和渲染自动完成，拿不准就询问。
- **E**：六步：扫描特征文件识别语言框架（120+ 框架）→ 读项目名 → 渲染 Owner Agent → 复制规则 → 渲染技能模板 → 创建变更追踪目录。
- **P**：装完之后，项目就有了身份、规则、技能、流程四件套，Owner Agent 立刻可以开始协作。



<svg xmlns="http://www.w3.org/2000/svg" width="980" height="320" viewBox="0 0 980 320">
  <defs>
    <linearGradient id="bgq45" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arq45" markerWidth="9" markerHeight="9" refX="8" refY="4.5" orient="auto">
      <path d="M 0 0 L 9 4.5 L 0 9 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="980" height="320" fill="url(#bgq45)"/>
  <text x="490" y="42" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">图 45 · 一套 Harness 装进项目的完整流程</text>
  <text x="490" y="292" text-anchor="middle" fill="#475569" font-size="16">零配置启动，全程秒级，不碰业务代码</text>
<rect x="40" y="112" width="111" height="118" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="95.5" y="139" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">npx 装包</text>
<text x="95.5" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">skills@latest add</text>
<line x1="151" y1="171.0" x2="197" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq45)"/>
<rect x="197" y="112" width="111" height="118" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="252.5" y="139" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">扫描识别</text>
<text x="252.5" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">语言/框架 120+</text>
<line x1="308" y1="171.0" x2="354" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq45)"/>
<rect x="354" y="112" width="111" height="118" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="409.5" y="139" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">渲染 Owner</text>
<text x="409.5" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">owner.md</text>
<line x1="465" y1="171.0" x2="511" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq45)"/>
<rect x="511" y="112" width="111" height="118" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="566.5" y="139" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">复制规则</text>
<text x="566.5" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">rules/</text>
<line x1="622" y1="171.0" x2="668" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq45)"/>
<rect x="668" y="112" width="111" height="118" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="723.5" y="139" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">渲染技能</text>
<text x="723.5" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">技能模板</text>
<line x1="779" y1="171.0" x2="825" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq45)"/>
<rect x="825" y="112" width="111" height="118" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="880.5" y="139" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">建变更目录</text>
<text x="880.5" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">.harness/changes/</text>
</svg>

**🗣️ 参考回答示范**：“上手体验我打磨的原则是零配置启动：第一条命令 `npx skills@latest add <仓库地址>` 把技能包装进项目环境；然后运行 apply-harness，它自动完成六步：扫描项目根目录识别语言和框架（支持 120+ 框架，拿不准会问你）→ 读项目名 → 参数渲染生成 Owner Agent → 复制规则文件 → 渲染流水线技能模板 → 创建 `.harness/changes/` 变更目录。整个安装过程纯本地文件操作，秒级完成，不碰业务代码。装完项目就有了四件套：身份（owner.md）、规则（rules/）、技能（skills/）、流程（changes/），Owner Agent 立刻进入工作状态。失败的兜底也是明确的：检测不到就往询问用户走，不让用户面对一个装错的项目。”

**🌟 加分点**：强调“不碰业务代码”——安装只生成 `.harness/` 目录，代码零改动，这降低了试错门槛，是能推广的关键。

**🛡️ 追问防御链**：追问“装完之后代码还是得自己写吗？” → 不用：变更流程启动后，Owner Agent 沿流水线跑——需求澄清、编码、测试、评审、门禁自动推进，人只在决策节点介入。

---

**Q46. 这套体系还能往哪些方向扩展？（扩展性展望）**

**🎯 面试官想考什么**：考你的“边界感”——既要展示产品还能长大，又要说清扩展不是无限堆功能。

**🧭 答题框架（PREP）**
- **P**：三个扩展方向：① 广度——新语言/新框架（检测表+语言包+参数块，流程已固化）；② 深度——新的领域组件技能（按需加载，第三层拉通业务领域规则）；③ 平台化——对接 CI 平台、IDE 插件、多项目组合管理。
- **R**：扩展判断标准是“是否复用现有机制”——能复用就走参数化，不能复用就评估造新层的成本。
- **E**：新增框架=加 5 行参数块；新领域=造组件技能；平台化=把技能库变成服务。
- **P**：所以我的扩展观是——**机制已经具备生长性，剩下的是按需生长，不是重构**。



<svg xmlns="http://www.w3.org/2000/svg" width="980" height="320" viewBox="0 0 980 320">
  <defs>
    <linearGradient id="bgq46" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arq46" markerWidth="9" markerHeight="9" refX="8" refY="4.5" orient="auto">
      <path d="M 0 0 L 9 4.5 L 0 9 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="980" height="320" fill="url(#bgq46)"/>
  <text x="490" y="42" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">图 46 · 三个扩展方向：顺着机制长</text>
  <text x="490" y="292" text-anchor="middle" fill="#475569" font-size="16">判断标准：能不能复用现有机制</text>
<rect x="40" y="112" width="269" height="118" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="174.5" y="139" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">广度</text>
<text x="174.5" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">新语言/新框架</text>
<text x="174.5" y="182" text-anchor="middle" fill="#94a3b8" font-size="11">固定四步流程</text>
<line x1="309" y1="171.0" x2="355" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq46)"/>
<rect x="355" y="112" width="269" height="118" rx="10" fill="#8b5cf6" opacity="0.12" stroke="#a78bfa" stroke-width="1.6"/>
<text x="489.5" y="139" text-anchor="middle" fill="#c4b5fd" font-size="15" font-weight="700">深度</text>
<text x="489.5" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">领域组件技能</text>
<text x="489.5" y="182" text-anchor="middle" fill="#94a3b8" font-size="11">如中间件规范</text>
<line x1="624" y1="171.0" x2="670" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq46)"/>
<rect x="670" y="112" width="269" height="118" rx="10" fill="#10b981" opacity="0.12" stroke="#34d399" stroke-width="1.6"/>
<text x="804.5" y="139" text-anchor="middle" fill="#6ee7b7" font-size="15" font-weight="700">平台化</text>
<text x="804.5" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">远程门禁/IDE 插件</text>
<text x="804.5" y="182" text-anchor="middle" fill="#94a3b8" font-size="11">多项目看板</text>
</svg>

**🗣️ 参考回答示范**：“扩展我分三个方向。广度：新语言、新框架——这条路已经固定成流程：检测表加段、造语言包、填参数块、回归验收，新增一个框架只要 5 行参数。深度：新的领域组件技能——比如团队有自己的中间件规范，做成类似 redis-cache-wrapper 的组件技能，按代码特征按需加载，把领域规则揉进评审和编码。平台化：把技能库从‘本地安装包’升级成服务——对接 Jenkins/GitLab CI 做远程门禁、做 IDE 插件在编辑器内跑流水线、做多项目的变更总览。每个方向我的判断标准都一样：**能不能复用现有机制**。能复用就顺着长，不能复用就先评估新层的成本——机制本身已经具备了生长性，后面是按需生长而不是推倒重来。”

**🌟 加分点**：用“复用现有机制”作为扩展的决策标准，展示你既看到可能性，又控制了疯狂加功能的风险。

**🛡️ 追问防御链**：追问“平台化会不会很重？从哪开始？” → 从‘读取侧’做起：先让平台能读多个项目的 `.harness/` 出报表（零侵入），再逐步加执行侧能力——先读后写，避免一上来就背上权限和安全的重壳。

---

### 4.8 方向八：项目复盘与行为（4 题）

**Q47. 这个项目你最得意的设计是什么？（⚡复盘必考 · 结构化重点）**

**🎯 面试官想考什么**：这道题不考技术，考“自我认知”——你能不能分清‘我做了什么’和‘什么值得骄傲’。面试官会根据你的答案判断你的品味和深度。

**🧭 答题框架（PREP）**
- **P**：候选不止一个，按场景挑：① **参数化+检测引擎**（一键装对，最硬核）；② **双轴评审**（解决评审流于形式，最有普适价值）；③ **软闸门**（最懂业务现实）。
- **R**：我通常先讲双轴评审——因为它解决的“评审最后都变成走过场”是每个团队都痛的通用问题，评委立刻有共鸣。
- **E**：双轴独立并行、0 严重问题放行、review.md 驱动退回——讲完这个，评委对整套体系的信任就建立了。
- **P**：收尾落到方法论——“我最得意的不是某个机制，而是这套体系让质量不依赖个人自觉”。



<svg xmlns="http://www.w3.org/2000/svg" width="980" height="420" viewBox="0 0 980 482.0">
  <defs>
    <linearGradient id="bgq47" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arq47" markerWidth="9" markerHeight="9" refX="8" refY="4.5" orient="auto">
      <path d="M 0 0 L 9 4.5 L 0 9 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="980" height="482.0" fill="url(#bgq47)"/>
  <text x="490" y="42" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">图 47 · 最得意设计：双轴评审</text>
  <text x="490" y="464" text-anchor="middle" fill="#475569" font-size="16">面试先讲"双轴评审"打动人心，参数化留作被追问时的硬核彩蛋</text>
<rect x="40" y="104" width="269" height="128" rx="10" fill="#ef4444" opacity="0.12" stroke="#f87171" stroke-width="1.6"/>
<text x="174.5" y="131" text-anchor="middle" fill="#fca5a5" font-size="15" font-weight="700">痛点</text>
<text x="174.5" y="156" text-anchor="middle" fill="#94a3b8" font-size="11">评审走过场 · 说不清</text>
<text x="174.5" y="174" text-anchor="middle" fill="#94a3b8" font-size="11">问题混在一起</text>
<line x1="309" y1="168.0" x2="355" y2="168.0" stroke="#475569" stroke-width="3" marker-end="url(#arq47)"/>
<rect x="355" y="104" width="269" height="128" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="489.5" y="131" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">设计</text>
<text x="489.5" y="156" text-anchor="middle" fill="#94a3b8" font-size="11">双轴独立 · 分开报告</text>
<text x="489.5" y="174" text-anchor="middle" fill="#94a3b8" font-size="11">Spec + Standards</text>
<line x1="624" y1="168.0" x2="670" y2="168.0" stroke="#475569" stroke-width="3" marker-end="url(#arq47)"/>
<rect x="670" y="104" width="269" height="128" rx="10" fill="#f59e0b" opacity="0.12" stroke="#fbbf24" stroke-width="1.6"/>
<text x="804.5" y="131" text-anchor="middle" fill="#fcd34d" font-size="15" font-weight="700">硬性放行</text>
<text x="804.5" y="156" text-anchor="middle" fill="#94a3b8" font-size="11">两轴都 0 严重问题</text>
<rect x="40" y="316" width="427" height="128" rx="10" fill="#10b981" opacity="0.12" stroke="#34d399" stroke-width="1.6"/>
<text x="253.5" y="343" text-anchor="middle" fill="#6ee7b7" font-size="15" font-weight="700">落地效果</text>
<text x="253.5" y="368" text-anchor="middle" fill="#94a3b8" font-size="11">有问题退回编码</text>
<text x="253.5" y="386" text-anchor="middle" fill="#94a3b8" font-size="11">带证据归档</text>
<line x1="467" y1="380.0" x2="513" y2="380.0" stroke="#475569" stroke-width="3" marker-end="url(#arq47)"/>
<rect x="513" y="316" width="427" height="128" rx="10" fill="#8b5cf6" opacity="0.12" stroke="#a78bfa" stroke-width="1.6"/>
<text x="726.5" y="343" text-anchor="middle" fill="#c4b5fd" font-size="15" font-weight="700">被追问时</text>
<text x="726.5" y="368" text-anchor="middle" fill="#94a3b8" font-size="11">再讲参数化: 52 占位符</text>
<text x="726.5" y="386" text-anchor="middle" fill="#94a3b8" font-size="11">120+ 框架更硬核</text>
<path d="M 489.5 232 V 270 H 253.5 V 316" fill="none" stroke="#475569" stroke-width="3" marker-end="url(#arq47)"/>
</svg>

**🗣️ 参考回答示范**：“我会选**双轴评审**，原因是它解决的痛点最普遍。我之前在团队里见过无数次评审走过场：reviewer 没时间细看、问题混在一起说不清、最后‘通过’了事。我的设计是把评审拆成相互独立的两个轴——Spec 轴（需求匹配）和 Standards 轴（规范合规），两个子智能体并行开查、分开出报告：查‘做没做对需求’和查‘合不合规范’是两件事，合并就互相掩盖。放行条件是硬性的：两轴都 0 个严重问题。评审报告带证据归档，有问题就退回编码阶段，review.md 就是修复依据。如果评委追问技术含量，我再说参数化：50 多个占位符、两层参数表、检测引擎识别 120+ 框架——那部分更硬核。但论‘给团队带来的改变’，双轴评审是我最得意的。”

**🌟 加分点**：给出“多候选+按场景选择”的策略——这本身就是一种面试技巧：先讲有共鸣的，被追问硬核再补参数化。

**🛡️ 追问防御链**：追问“参数化不是更复杂吗，为什么不选它？” → 复杂度不等于价值：参数化解决的是‘维护成本’，双轴评审解决的是‘质量信任’；对评委和团队，后者更能打动——技术深度是加分项，机制价值是必答项。

---

**Q48. 项目里最大的坑是什么？你是怎么爬出来的？（复盘故事）**

**🎯 面试官想考什么**：考真实性与成长性——只会报喜没有故事的可信度低；这道题的关键是“爬出来的过程”而不是“坑有多深”。

**🧭 答题框架（PREP）**
- **P**：挑一个真实的故事：**模板化技能换成参数化之前，同步 6 种语言规则改一次漏一次**。
- **R**：这个坑的根因是“复制粘贴结构”：公共逻辑在 6 份语言包里各有一份，改一处要同步六处，漏了还发现不了。
- **E**：爬出来的过程分三步：先统计‘改一次漏几处’暴露问题规模 → 重构为‘公共只存一份+参数渲染分发’ → 加回归验收（无残留占位符），让问题从结构上消失。
- **P**：结尾提炼——“这个坑教会我：多语言项目的同步问题，正确答案不是流程而是结构”。



<svg xmlns="http://www.w3.org/2000/svg" width="980" height="320" viewBox="0 0 980 320">
  <defs>
    <linearGradient id="bgq48" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arq48" markerWidth="9" markerHeight="9" refX="8" refY="4.5" orient="auto">
      <path d="M 0 0 L 9 4.5 L 0 9 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="980" height="320" fill="url(#bgq48)"/>
  <text x="490" y="42" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">图 48 · 最大的坑：复制粘贴的教训</text>
  <text x="490" y="292" text-anchor="middle" fill="#475569" font-size="16">从"记得同步"到"结构上消灭多份"</text>
<rect x="40" y="112" width="190" height="118" rx="10" fill="#ef4444" opacity="0.12" stroke="#f87171" stroke-width="1.6"/>
<text x="135.0" y="139" text-anchor="middle" fill="#fca5a5" font-size="15" font-weight="700">复制粘贴方案</text>
<text x="135.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">一套技能复制 6 份</text>
<text x="135.0" y="182" text-anchor="middle" fill="#94a3b8" font-size="11">各改各的</text>
<line x1="230" y1="171.0" x2="276" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq48)"/>
<rect x="276" y="112" width="190" height="118" rx="10" fill="#ef4444" opacity="0.12" stroke="#f87171" stroke-width="1.6"/>
<text x="371.0" y="139" text-anchor="middle" fill="#fca5a5" font-size="15" font-weight="700">公共变更漏同步</text>
<text x="371.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">改一次漏一次</text>
<text x="371.0" y="182" text-anchor="middle" fill="#94a3b8" font-size="11">最惨漏 2 处</text>
<line x1="466" y1="171.0" x2="512" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq48)"/>
<rect x="512" y="112" width="190" height="118" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="607.0" y="139" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">重构</text>
<text x="607.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">公共只存一份</text>
<text x="607.0" y="182" text-anchor="middle" fill="#94a3b8" font-size="11">安装时渲染分发</text>
<line x1="702" y1="171.0" x2="748" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq48)"/>
<rect x="748" y="112" width="190" height="118" rx="10" fill="#10b981" opacity="0.12" stroke="#34d399" stroke-width="1.6"/>
<text x="843.0" y="139" text-anchor="middle" fill="#6ee7b7" font-size="15" font-weight="700">回归验收</text>
<text x="843.0" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">零残留占位符</text>
<text x="843.0" y="182" text-anchor="middle" fill="#94a3b8" font-size="11">漏改直接被拦</text>
</svg>

**🗣️ 参考回答示范**：“最大的坑是模板化技能之前的复制粘贴方案。最早我做多语言支持，是把一套技能复制到 6 种语言包里再各改各的——结果公共规则一变，要同步 6 处，改一次漏一次，最惨的一次 Go 包漏了 2 处，用户装完才发现命令是旧的。爬坑分三步：第一步，我统计了一下，一个公共变更平均要改 6-8 处，且没有机器能发现漏改——问题规模摆在那里；第二步，把它重构成‘公共只存一份、安装时渲染分发’，语言包只留差异；第三步，加回归验收——渲染产物零残留占位符，漏改直接被拦。这个坑教会我的不只是技术，而是：**多版本同步问题，正确答案不是‘记得同步’，而是从结构上消灭多份**——这成了我后来所有设计的第一原则。”

**🌟 加分点**：用“先量化问题再给方案”的故事线——统计漏改次数这一步，让故事从“我摔了一跤”升格为“我用数据驱动了重构”。

**🛡️ 追问防御链**：追问“重构花了多久？中途业务受影响吗？” → 重构只动技能仓库不动业务代码，大约一轮迭代完成；因为渲染是确定性的，回归测试跑通即验证完成，业务零影响——这也反证了早期复制粘贴方案的脆弱。

---

**Q49. 如果再给你三个月，你优先做什么？（规划与优先级）**

**🎯 面试官想考什么**：考优先级判断——面试官怕听到‘全都要做’的贪心答案，也怕听到‘不知道做什么’的空洞答案。

**🧭 答题框架（PREP）**
- **P**：三件事按序：① 补测试与加固——从技术债最重的 PRD 解析平台开始（解析/合成算法层补单测）；② 落地验证——找真实团队跑棕地迁移试点，收集软闸门数据；③ 平台化成 MVP——先做读取侧多项目看板。
- **R**：顺序逻辑：先还债（内功）再验证（价值）最后放大（规模）——没有测试的平台不敢让团队真用，没有试点数据的平台化是空中楼阁。
- **E**：三件事分别对应“可信度→价值证明→规模复制”三段。
- **P**：收尾——“三个月我选的是把‘能用’变成‘敢用、好用、爱用’，而不是堆功能”。



<svg xmlns="http://www.w3.org/2000/svg" width="980" height="320" viewBox="0 0 980 320">
  <defs>
    <linearGradient id="bgq49" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arq49" markerWidth="9" markerHeight="9" refX="8" refY="4.5" orient="auto">
      <path d="M 0 0 L 9 4.5 L 0 9 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="980" height="320" fill="url(#bgq49)"/>
  <text x="490" y="42" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">图 49 · 再给三个月：先还债、再试点、后规模</text>
  <text x="490" y="292" text-anchor="middle" fill="#475569" font-size="16">把"能用"变成"敢用、好用、爱用"</text>
<rect x="40" y="112" width="269" height="118" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="174.5" y="139" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">① 补测试加固</text>
<text x="174.5" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">PRD 解析平台先补</text>
<text x="174.5" y="182" text-anchor="middle" fill="#94a3b8" font-size="11">算法层单测</text>
<line x1="309" y1="171.0" x2="355" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq49)"/>
<rect x="355" y="112" width="269" height="118" rx="10" fill="#8b5cf6" opacity="0.12" stroke="#a78bfa" stroke-width="1.6"/>
<text x="489.5" y="139" text-anchor="middle" fill="#c4b5fd" font-size="15" font-weight="700">② 落地验证</text>
<text x="489.5" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">真实团队棕地试点</text>
<text x="489.5" y="182" text-anchor="middle" fill="#94a3b8" font-size="11">跑出 baseline/增量/收敛</text>
<line x1="624" y1="171.0" x2="670" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq49)"/>
<rect x="670" y="112" width="269" height="118" rx="10" fill="#10b981" opacity="0.12" stroke="#34d399" stroke-width="1.6"/>
<text x="804.5" y="139" text-anchor="middle" fill="#6ee7b7" font-size="15" font-weight="700">③ 平台化 MVP</text>
<text x="804.5" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">只读看板</text>
<text x="804.5" y="182" text-anchor="middle" fill="#94a3b8" font-size="11">零侵入低风险</text>
</svg>

**🗣️ 参考回答示范**：“三个月我排三件事。第一，**补测试加固**——从技术债最重的 PRD 解析平台开始，解析、4 文档拆分、合成器这些算法层先补上单元测试，把‘演示能跑’变成‘测过才敢信’。第二，**落地验证**——找 1 个真实团队做棕地迁移试点，跑完整软闸门流程，把 baseline、增量闸门、收敛曲线这三组数据跑出来——这些数据比任何演示都值钱，也是平台化最需要的背书。第三，**平台化为 MVP**——先做‘读取侧’：一个能汇总多个项目 `.harness/` 变更状态和多语言技能覆盖的只读看板，零侵入、低风险。三件事是一段链：**先还债换来可信，再试点换来数据，最后用数据做规模**——三个月我选的是把‘能用’变成‘敢用、好用、爱用’，而不是堆功能。”

**🌟 加分点**：三件事互相咬合（测试→试点→平台）而不是并列清单，展示规划上的因果链思维。

**🛡️ 追问防御链**：追问“为什么不先做最亮眼的平台化？” → 因为平台化最大风险是‘没有真实使用数据时的过度设计’；先试点拿到基线数据和真实反馈，平台才知道该接什么——先有用户再建平台，而不是建了平台找用户。

---

**Q50. 用一句话总结这个项目，你会怎么说？（收尾升华）**

**🎯 面试官想考什么**：整场面试的“最后一帧”——这句话要让评委带着‘这人想清楚了’的印象结束。

**🧭 答题框架（PREP）**
- **P**：一句话版本：“**人类设计约束、AI 写代码、机器验证**——我造的是 AI 时代的软件开发基础设施。”
- **R**：它同时回答了三个问题：人的角色（定规则）、AI 的角色（执行）、质量的归宿（机器门禁），正好覆盖评委对你的所有疑问。
- **E**：如果需要再加半句，落到这句话的份量上——“它不是工具，是把老工程师的经验变成可安装、可验证、可传承的基础设施。”
- **P**：最后留一个开放钩子：“这套体系已经被 5 种语言、80+ 框架的安装量验证过边界——下一步是让它服务真实团队。”



<svg xmlns="http://www.w3.org/2000/svg" width="980" height="320" viewBox="0 0 980 320">
  <defs>
    <linearGradient id="bgq50" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arq50" markerWidth="9" markerHeight="9" refX="8" refY="4.5" orient="auto">
      <path d="M 0 0 L 9 4.5 L 0 9 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="980" height="320" fill="url(#bgq50)"/>
  <text x="490" y="42" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">图 50 · 一句话总结：人类设计约束，AI 写代码，机器验证</text>
  <text x="490" y="292" text-anchor="middle" fill="#475569" font-size="16">可安装 · 可测试 · 可追溯的软件开发基础设施</text>
<rect x="40" y="112" width="269" height="118" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.6"/>
<text x="174.5" y="139" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">人类设计约束</text>
<text x="174.5" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">规则/门禁/流程</text>
<line x1="309" y1="171.0" x2="355" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq50)"/>
<rect x="355" y="112" width="269" height="118" rx="10" fill="#8b5cf6" opacity="0.12" stroke="#a78bfa" stroke-width="1.6"/>
<text x="489.5" y="139" text-anchor="middle" fill="#c4b5fd" font-size="15" font-weight="700">AI 写代码</text>
<text x="489.5" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">按约束执行</text>
<line x1="624" y1="171.0" x2="670" y2="171.0" stroke="#475569" stroke-width="3" marker-end="url(#arq50)"/>
<rect x="670" y="112" width="269" height="118" rx="10" fill="#10b981" opacity="0.12" stroke="#34d399" stroke-width="1.6"/>
<text x="804.5" y="139" text-anchor="middle" fill="#6ee7b7" font-size="15" font-weight="700">机器验证</text>
<text x="804.5" y="164" text-anchor="middle" fill="#94a3b8" font-size="11">六段门禁兜底</text>
</svg>

**🗣️ 参考回答示范**：“一句话：**人类设计约束，AI 写代码，机器验证**——我做的是一个 AI 时代的软件开发基础设施。它把老工程师脑子里的隐性经验——规范、门禁、流程——变成了可安装、可测试、可追溯的显性基础设施，任何项目装上就能用；5 种语言、80+ 框架的覆盖面说明这套抽象经得起规模检验，而软闸门的设计让它能走进存量业务，而不是只能在绿地项目里演示。如果要给这个项目一个定位，它不是某个工具，而是我给‘AI 协作开发’这个时代问题提交的一份工程答案。”

**🌟 加分点**：把“6 语言 120+ 框架”和“软闸门能进棕地”两个最硬的点织进结语——前者证明广度，后者证明务实，一句话同时立住两个印象。

**🛡️ 追问防御链**：追问“如果一定要说一个最大的局限呢？” → 诚实说：确定性链路很强，但‘语义环节’（评审、解析）的可靠性依赖模型能力和评测体系，下一步的评测数据集是我最想补的东西——主动说局限并带上补救方向，比嘴硬加分。

---

### 4.9 模拟面试实录：一场 30 分钟的完整对话

建议你对照录音练一遍：

```
【面试官】先做个自我介绍。
【候选人】我主要做后端开发，最近一年专注 AI 工程方向。我最拿得出手的
          项目是一个 AI 协作开发脚手架——它解决的是"AI 写代码质量不可控"
          这个行业问题，我把"需求到部署"组织成 6 阶段流水线，用机器门禁
          约束 AI 产出。落地了 39 个技能包，覆盖 5 种语言 80+ 框架。

【面试官】你这个项目解决的核心问题是什么？
【候选人】核心问题是：当 AI 开始大规模写代码，质量和可追溯性谁保证？
          传统靠 code review，但 AI 生成量太大，人盯不过来。我的方案是
          "人类设计约束、AI 写代码、机器验证"——让机器当质检员。

【面试官】那你怎么保证 AI 真的按约束来？
【候选人】三层保障：① 先写失败测试，AC 先转成测试，AI 必须让测试通过；
          ② 机器门禁，编译、静态分析、竞态检测、架构约束、单元测试+覆盖率、
          安全扫描六道闸；
          ③ 专家双轴评审，Spec 匹配度 + 规范合规度，0 严重问题才放行。

【面试官】遇到的最大的技术难点是什么？
【候选人】最难的是跨语言模板渲染。6 种语言规范差异很大，如果每种语言
          写一套规则，维护成本爆炸。我用了"参数化分层覆盖"：语言基础参数
          块管通用的，框架差异块管语言特有的，合并时框架块覆盖基础块。
          这样新增一种语言，只写差异块，不动公共部分。

【面试官】如果你的新方案推广时，老代码全是坏味道，怎么办？
【候选人】这就是我设计"软闸门"的初衷。存量代码允许违规数不增加（先建档），
          新增代码 0 violation（硬门禁），安全类问题例外——立即修。然后
          分三阶段收敛：摸底 → 套壳 → 收敛。团队既有标尺，又不被堵死。

【面试官】你最大的优势是什么？
【候选人】我觉得是"把方法论变成基础设施"。很多人知道要写测试、要规范，
          但我是真的把它做成了可一键安装、可复用的工程产物，不依赖个人
          自觉——这恰好是 AI 时代最稀缺的工程能力。

【面试官】你有什么想问我的？
【候选人】想请教一下，咱们团队目前在做 AI 工程化的过程中，最头疼的
          问题是什么？我很好奇实际落地的挑战。
```

**这段实录的三个要点**：① 每个答案都用 PREP 结构；② 主动抛出"软闸门""参数化分层覆盖"等钩子引导追问；③ 收尾问题显得你真正关心团队。把这段背熟 80%，你已经是"面霸"预备役。

---

## 第五部分：面试答题框架与行动清单

### 5.1 万能答题框架：结构 = 高度（让面试官"听不懂也要觉得你行"）

技术面试最重要的不是"全对"，而是**结构化表达**。同一个知识点，会答和答得好，差距就在框架上。

```svg
<svg xmlns="http://www.w3.org/2000/svg" width="980" height="380" viewBox="0 0 980 380">
  <defs>
    <linearGradient id="bgI" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arI" markerWidth="12" markerHeight="12" refX="10" refY="6" orient="auto">
      <path d="M 0 0 L 12 6 L 0 12 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="980" height="380" fill="url(#bgI)"/>
  <text x="490" y="30" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">万能答题框架 PREP：观点 → 理由 → 例子 → 收束</text>

  <!-- 四个阶段 -->
  <rect x="40" y="55" width="210" height="150" rx="12" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="2"/>
  <text x="145" y="84" text-anchor="middle" fill="#7dd3fc" font-size="16" font-weight="700">P · Point</text>
  <text x="145" y="106" text-anchor="middle" fill="#64748b" font-size="10">先亮观点</text>
  <line x1="65" y1="118" x2="225" y2="118" stroke="#38bdf8" stroke-width="0.6" opacity="0.3"/>
  <text x="70" y="142" fill="#94a3b8" font-size="11">"我的结论是：分三层"</text>
  <text x="70" y="162" fill="#94a3b8" font-size="11">一句话先给结论</text>
  <text x="70" y="182" fill="#fde68a" font-size="11">★ 别让面试官猜</text>

  <line x1="255" y1="130" x2="290" y2="130" stroke="#475569" stroke-width="3" marker-end="url(#arI)"/>

  <rect x="295" y="55" width="210" height="150" rx="12" fill="#8b5cf6" opacity="0.12" stroke="#a78bfa" stroke-width="2"/>
  <text x="400" y="84" text-anchor="middle" fill="#c4b5fd" font-size="16" font-weight="700">R · Reason</text>
  <text x="400" y="106" text-anchor="middle" fill="#64748b" font-size="10">讲清理由</text>
  <line x1="320" y1="118" x2="480" y2="118" stroke="#a78bfa" stroke-width="0.6" opacity="0.3"/>
  <text x="325" y="142" fill="#94a3b8" font-size="11">"因为模板快但脆</text>
  <text x="325" y="162" fill="#94a3b8" font-size="11">LLM 灵活但会幻觉"</text>
  <text x="325" y="182" fill="#fde68a" font-size="11">★ 讲清"为什么"</text>

  <line x1="510" y1="130" x2="545" y2="130" stroke="#475569" stroke-width="3" marker-end="url(#arI)"/>

  <rect x="550" y="55" width="210" height="150" rx="12" fill="#f59e0b" opacity="0.12" stroke="#fbbf24" stroke-width="2"/>
  <text x="655" y="84" text-anchor="middle" fill="#fcd34d" font-size="16" font-weight="700">E · Example</text>
  <text x="655" y="106" text-anchor="middle" fill="#64748b" font-size="10">给例子</text>
  <line x1="575" y1="118" x2="735" y2="118" stroke="#fbbf24" stroke-width="0.6" opacity="0.3"/>
  <text x="580" y="142" fill="#94a3b8" font-size="11">"我项目里就用了三层</text>
  <text x="580" y="162" fill="#94a3b8" font-size="11">模板→LLM→章节算法"</text>
  <text x="580" y="182" fill="#fde68a" font-size="11">★ 一定带项目</text>

  <line x1="765" y1="130" x2="800" y2="130" stroke="#475569" stroke-width="3" marker-end="url(#arI)"/>

  <rect x="805" y="55" width="135" height="150" rx="12" fill="#10b981" opacity="0.12" stroke="#34d399" stroke-width="2"/>
  <text x="872" y="84" text-anchor="middle" fill="#6ee7b7" font-size="16" font-weight="700">P · Point</text>
  <text x="872" y="106" text-anchor="middle" fill="#64748b" font-size="10">回扣收束</text>
  <line x1="820" y1="118" x2="925" y2="118" stroke="#34d399" stroke-width="0.6" opacity="0.3"/>
  <text x="822" y="142" fill="#94a3b8" font-size="10">"所以选混合</text>
  <text x="822" y="162" fill="#94a3b8" font-size="10">策略最稳"</text>
  <text x="822" y="182" fill="#fde68a" font-size="11">★ 收个尾</text>

  <!-- 底部：话术模板 -->
  <rect x="80" y="225" width="820" height="60" rx="10" fill="#1e293b" stroke="#475569" stroke-width="1.5"/>
  <text x="100" y="250" fill="#e2e8f0" font-size="13" font-weight="700">话术模板：</text>
  <text x="220" y="250" fill="#94a3b8" font-size="12">"我的结论是 X（观点）。之所以这样，是因为 A/B/C（理由）。比如在我项目里就……（例子）。所以综合来看选 X 最合适（收束）。"</text>
  <text x="100" y="274" fill="#64748b" font-size="11">→ 哪怕答案不完美，结构化表达也能让面试官看到你的"思路清晰度"</text>

  <text x="490" y="325" text-anchor="middle" fill="#475569" font-size="16">加一条"面试防坑"：答不出时不要沉默，说"我目前对这个了解有限，但我猜是……因为……"</text>
  <text x="490" y="348" text-anchor="middle" fill="#475569" font-size="16">面试官更在意你的思考路径，而不是标准答案本身</text>
  <text x="490" y="368" text-anchor="middle" fill="#475569" font-size="16">结合本项目：Q41（PRD 三层解析）就是用 PREP 框架答的——你可以直接套用</text>
</svg>
```

**技术题的 PREP 应用示例（拿 Q41 演示）**：
- **P**：PRD 解析，我会用"模板 + LLM + 章节算法"三层混合，按序回退。
- **R**：模板快但脆（换个写法就失效）、LLM 灵活但可能幻觉、章节算法稳定但精度低——单用任何一层都有明显缺陷。
- **E**：在我的项目里，三层按序回退，AI 还可关闭降级到后两层。
- **P**：所以混合策略 + 可降级，是准确率与稳定性之间的最优解。

### 5.2 简历到面试的"闭环清单"（收藏这 7 条）

最后，给你一份可以直接执行的行动清单。把这份清单打出来，贴在工位前：

```svg
<svg xmlns="http://www.w3.org/2000/svg" width="980" height="400" viewBox="0 0 980 400">
  <defs>
    <linearGradient id="bgJ" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arJ" markerWidth="12" markerHeight="12" refX="10" refY="6" orient="auto">
      <path d="M 0 0 L 12 6 L 0 12 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="980" height="400" fill="url(#bgJ)"/>
  <text x="490" y="30" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">简历 → 面试 行动清单（7 步闭环）</text>

  <!-- 7 个步骤，环形排列 -->
  <rect x="40" y="55" width="250" height="52" rx="10" fill="#0ea5e9" opacity="0.14" stroke="#38bdf8" stroke-width="1.5"/>
  <text x="60" y="78" fill="#7dd3fc" font-size="14" font-weight="700">1. 写定位句</text>
  <text x="60" y="97" fill="#94a3b8" font-size="11">35 字内讲清目标×机制×结果</text>

  <rect x="365" y="55" width="250" height="52" rx="10" fill="#8b5cf6" opacity="0.14" stroke="#a78bfa" stroke-width="1.5"/>
  <text x="385" y="78" fill="#c4b5fd" font-size="14" font-weight="700">2. 按岗位改亮点</text>
  <text x="385" y="97" fill="#94a3b8" font-size="11">后端/AI/全栈三套主角</text>

  <rect x="690" y="55" width="250" height="52" rx="10" fill="#f59e0b" opacity="0.14" stroke="#fbbf24" stroke-width="1.5"/>
  <text x="710" y="78" fill="#fcd34d" font-size="14" font-weight="700">3. 准备 3 个故事</text>
  <text x="710" y="97" fill="#94a3b8" font-size="11">迁移 / 架构体检 / 软闸门</text>

  <rect x="40" y="130" width="250" height="52" rx="10" fill="#ef4444" opacity="0.14" stroke="#f87171" stroke-width="1.5"/>
  <text x="60" y="153" fill="#fca5a5" font-size="14" font-weight="700">4. 背熟 5 道"项目必考题"</text>
  <text x="60" y="172" fill="#94a3b8" font-size="11">Q1 / Q9 / Q16 / Q22 / Q29 / Q41</text>

  <rect x="365" y="130" width="250" height="52" rx="10" fill="#10b981" opacity="0.14" stroke="#34d399" stroke-width="1.5"/>
  <text x="385" y="153" fill="#6ee7b7" font-size="14" font-weight="700">5. 练 PREP 框架</text>
  <text x="385" y="172" fill="#94a3b8" font-size="11">每个技术题先结论后理由</text>

  <rect x="690" y="130" width="250" height="52" rx="10" fill="#0ea5e9" opacity="0.14" stroke="#38bdf8" stroke-width="1.5"/>
  <text x="710" y="153" fill="#7dd3fc" font-size="14" font-weight="700">6. 录音自测</text>
  <text x="710" y="172" fill="#94a3b8" font-size="11">口答一遍，听自己的表达</text>

  <!-- 底部大框 -->
  <rect x="240" y="210" width="500" height="70" rx="12" fill="#1e293b" stroke="#38bdf8" stroke-width="2"/>
  <text x="490" y="240" text-anchor="middle" fill="#e2e8f0" font-size="16" font-weight="700">7. 面试后复盘</text>
  <text x="490" y="262" text-anchor="middle" fill="#94a3b8" font-size="12">没答好的题 → 补短板 → 再战，形成"面一轮强一轮"的飞轮</text>

  <!-- 箭头环 -->
  <path d="M 165 165 L 165 200 L 490 200" fill="none" stroke="#475569" stroke-width="2" marker-end="url(#arJ)"/>
  <path d="M 490 200 L 490 285 L 165 285 L 165 112" fill="none" stroke="#475569" stroke-width="2" stroke-dasharray="6,4"/>

  <text x="490" y="320" text-anchor="middle" fill="#475569" font-size="16">记住：简历是"门"，面试是"关"。门要让人想进来，关要让人不想走。</text>
  <text x="490" y="345" text-anchor="middle" fill="#475569" font-size="16">你的项目足够好——你要做的，只是学会把它讲好。</text>
  <text x="490" y="370" text-anchor="middle" fill="#475569" font-size="16">下一期预告：如何把这个项目讲成一个"让人记住你"的故事（叙事版）</text>
</svg>
```

### 5.3 面试前 24 小时：临门一脚清单

面试前的最后一天，不要再看新的技术题了——**回归"稳"**。按这份清单过一遍：

**T-24h（前一天晚上）**
- [ ] 重读一遍你的简历，确保每个词都能展开讲 2 分钟
- [ ] 把 50 道题里"结合本项目"的部分全部过一遍（这是你的差异化）
- [ ] 准备好 2 个"万能问题"问面试官（Q50 的模板）
- [ ] 晚上 11 点前睡——面试状态比临阵磨枪重要 10 倍

**T-1h（面试前 1 小时）**
- [ ] 把 3 个故事（迁移/架构体检/软闸门）各口述一遍，控制在 90 秒内
- [ ] 复习"一句话定位句"（这是开场白）
- [ ] 看一遍 PREP 框架图（5.1 那张）
- [ ] 深呼吸：你已经准备得比 90% 的候选人充分了

**面试中**
- [ ] 前 2 分钟主动讲"定位句 + 亮点数字"，掌握节奏
- [ ] 每个技术题先给结论（P），再展开理由和例子（R + E）
- [ ] 答不出时：不沉默，说"我目前了解有限，但我的思路是……"
- [ ] 最后反问环节：一定问，且问"团队最头疼的问题"这类高质量问题

> 💡 **心态提示**：面试是"筛选匹配"而不是"审判"。你的目标不是让所有人满意，而是**让对的团队看到你的价值**。把每一次面试当成一次"双向了解"，你会从容很多。

### 5.4 最容易翻车的 5 个点（实战血泪总结）

上面讲的是"怎么答对"，这一节讲"怎么不踩坑"。以下 5 个点，是绝大多数候选人在面试现场最容易翻车的地方，提前排雷：

**翻车点 1：简历上写了，但答不上细节**

这是最致命的。你简历写"参数化分层覆盖"，面试官追问"两层参数块怎么合并、谁覆盖谁？"你答不上来，整份简历的可信度都会崩。**解法**：简历上每一个技术名词，都要能展开讲 3 分钟——对照本文第三部分的"追问防御链"提前演练。

**翻车点 2：一上来就背项目，像念稿**

面试官问"介绍一下你的项目"，你从第一个字背到最后一个字，中间不停顿、不互动。**解法**：只讲 60 秒定位句 + 亮点数字，然后停一下，说"您想先了解哪部分？"——把节奏交给面试官，显得自信又从容。

**翻车点 3：项目题答不到点上，全程绕圈**

面试官问"你们怎么保证 AI 写出来的代码没问题"，你从"我们用了很多工具"开始讲起。**解法**：用 PREP 先给结论"双轴评审 + CI 五段门禁，用硬指标说话"，再展开。先结论后理由，永远不跑偏。

**翻车点 4：暴露"没做过、只是看过"**

面试官追问得深一点，你就支支吾吾。**解法**：只讲自己真正做过的（本项目足够撑场），没做过的就大方承认"这部分我了解不深，但我可以讲下我的理解"。诚实 + 思路，远胜硬撑。

**翻车点 5：最后反问环节问了个"掉价"问题**

"咱们加班多吗？""薪资能到多少？"这类问题会瞬间拉低印象分。**解法**：用 Q50 的高质量问法——关心团队痛点、岗位目标、成长机制。这既体现你的格局，也帮你判断这个团队值不值得去。

> 💡 **记住**：面试翻车大多不是因为"不会"，而是因为"没准备到位"或"表达没结构"。这两件事，本文都已经帮你准备好了。

### 5.5 最后说三句掏心窝的话

1. **简历不是"写出来的"，是"做出来的"。** 你现在手里这套脚手架，已经从"做"的角度赢了大多数人——剩下的是把它的价值翻译成面试官能秒懂的语言。
2. **别背题，要背"骨架"。** 50 道题给你的是骨架和要点，面试时用自己的话组织。背标准答案会露怯，讲"你的理解"才稳。
3. **这个项目最大的竞争力，是"新鲜"。** 2025 年面试官见到最多的简历是"电商 + 秒杀 + 双十一"，而你拿出的是"AI 协作开发方法论基础设施"——**差异化本身就是最大的加分项**。

祝上岸。🚀

---

## 附录：简历投出去之后的 3 件事（别让好项目输在最后一步）

很多人以为"写完简历、面完试"就结束了。其实**投递阶段和谈薪阶段**同样决定成败。这一节补全最后一公里。

### 附 1：投递渠道怎么选

- **内推 > Boss/拉勾 > 官网**：内推简历优先被看到，且能跳过第一轮 HR 初筛。如果你有认识的学长/前同事在这个项目方向，一定优先内推。
- **垂直社区**：掘金、InfoQ、公众号的"急招"贴，往往比平台效率更高——尤其 AI 工程这类新方向，垂直渠道需求更真实。
- **海投 vs 精投**：建议"80% 精投 + 20% 海投"。精投指针对每个公司微调"岗位版本"，海投只是为了练手和保底。**别把海投的反馈当真**，那不代表你的真实水平。

### 附 2：投出去后 3 天没回音，怎么办

- **3 天**：正常，招聘流程普遍偏慢，不要焦虑。
- **1 周**：礼貌跟进一次——"您好，上周投递了 XX 岗位，想了解一下进展"。注意一次就够，别天天催。
- **被拒**：主动问一句"方便告知是哪方面不匹配吗？"——这是免费的改进反馈，问到的答案记下来，下轮改进。

> 💡 **心态**：投简历是"概率游戏"。你的目标是让"进面率"从 10% 提到 30%，而不是指望一次投中。每多一份反馈，你就离上岸近一步。

### 附 3：谈薪的 3 个原则（应届/转行同样适用）

1. **先别主动报数字**：让 HR 先出价。你报低了吃亏，报高了可能直接出局。
2. **用"市场区间"报价**：被追问时，说"我看同类岗位市场区间是 18-25k，我期望在合理范围"，而不是报死一个数。
3. **谈的是"总包"不是"月薪"**：月薪 + 年终 + 股票/期权 + 福利折算，才是一年的真实收入。别只看 base。

**应届生特别提醒**：校招薪资相对标准化，不要过于纠结单点差距，**平台和成长空间比第一份薪资重要得多**——用本项目冲一个能持续打磨技术栈的团队，三年后的回报远大于多出的几千块。

### 附 4：三分钟自我介绍模板（直接照着练）

很多面试从"请先做个自我介绍"开始，而这个开场 3 分钟，往往决定了面试官接下来的提问方向。给你一份能直接背的模板，把节奏牢牢抓在自己手里：

**第一分钟 · 定位句（我是谁 + 我解决过什么难题）**

> 面试官你好，我主要做后端开发，最近一年专注 AI 工程方向。我最拿得出手的项目，是一套 AI 协作开发脚手架——它解决的是"AI 大规模写代码后，质量谁保证"这个行业级痛点。我把"需求到部署"组织成 6 阶段流水线，用机器门禁约束 AI 产出，最终落地了 30+ 可复用技能，覆盖 5 种语言、80+ 框架。

**第二分钟 · 一个亮点故事（让面试官记住你）**

> 这里面我最想讲的是"棕地迁移"的实践。存量项目代码量大、没有测试、lint 违规上千条，直接套新规范会全线红灯。我的做法是分三阶段：先摸底建档、再用软闸门让存量违规"只减不增"、最后逐模块收敛。安全类问题例外，立即修。这个设计让我学会了——工程规范不是一纸命令，而是需要"缓冲期"的渐进式变革。

**第三分钟 · 关联岗位（为什么这个岗位适合我）**

> 我关注到贵司在招 XX 方向，恰好和我项目里沉淀的方法论高度契合：既要有工程质量意识，又要有把 AI 能力工程化的落地经验。所以我很期待有机会在真实业务场景里，把"约束 AI、机器验证"这套思路继续深化。

**三个小贴士**：① 全程控制在 3 分钟内，别超时；② 语速放慢 20%，停顿比赶场更显自信；③ 结尾一定"关联到岗位"，让面试官顺着你的方向提问，而不是自己乱猜。

### 附 5：校招 / 转行 / 在职，三种人群的差异化打法

同一个项目，不同人群的用法完全不同。对号入座，别用错力：

**如果你是校招生（没有工作经验）**

> 项目就是你简历的"工作经验"。把本文第二部分的"完整样本"写细、写深，让它撑起整页简历的 60%。
> - 重点：体现"你一个人完成了从需求到部署的全链路"，展示独立交付能力
> - 加分：把项目的 Wiki、流程图、架构图打包进作品集/个人网站，给面试官一个链接
> - 注意：校招面试官最喜欢追问细节，所以第四部分的方向①方法论、方向④软闸门、方向⑤状态机要优先啃熟——这三块最容易从"你项目里怎么落地的"被深挖

**如果你是转行者（比如测试/运维转开发）**

> 项目是你"跨过来的决心"的最佳证明。转行面试官最怕的是"你没有真正写过代码"。
> - 重点：讲清楚你**为什么做、怎么学会的、踩过什么坑**——转行者最能打动人的就是"学习力 + 执行力"
> - 加分：把 arch-review、软闸门这些"工程思维"讲出来，证明你不只会写代码，还懂工程
> - 注意：面试官可能会挑战"你是不是只看教程没实操"，准备好用具体的文件/命令/报错经历来回应

**如果你是在职人员（用项目冲涨薪/跳槽）**

> 项目是你"技术成长"的证据，重点不是"做什么"，而是"你解决了什么新问题"。
> - 重点：突出这个项目和你当前岗位的**差异化**——AI 工程能力正是市场稀缺点，这就是你谈薪的筹码
> - 加分：讲一讲"如果放到公司里，这套方法论能解决什么真实问题"，体现业务视角
> - 注意：在职面试官更关注"落地能力"，多讲数据（覆盖率、违规数下降、门禁拦截了多少问题）

**一句话总结**：校招拼"潜力 + 完整"，转行拼"决心 + 学习力"，在职拼"稀缺 + 落地"。同一个脚手架，三种讲法，条条通向上岸。

---

*本文基于 tayama-harness-skills 脚手架项目撰写，项目细节以仓库内文档为准。文中 50 道题覆盖技术面 / 项目面 / 行为面三大环节，可按需裁剪组合使用。*
