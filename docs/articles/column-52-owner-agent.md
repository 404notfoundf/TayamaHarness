# Owner Agent：应用负责人智能体——把"项目身份、规则、流程、上下文"持久化给 AI

> 思想类专栏 · 第 52 篇 · 约 9000 字 · 6 个 SVG 图

---

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 300" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_o1" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_o1"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="300" fill="url(#bg_o1)" rx="10"/>
  <text x="400" y="28" text-anchor="middle" fill="#e2e8f0" font-size="26" font-weight="700">Owner Agent：应用负责人智能体</text>
  <rect x="40" y="55" width="340" height="70" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_o1)"/>
  <text x="210" y="77" text-anchor="middle" fill="#93c5fd" font-size="13" font-weight="700">身份持有</text>
  <text x="210" y="103" text-anchor="middle" fill="#94a3b8" font-size="9">项目名 · 语言 · 构建 · 测试 · 规范</text>
  <rect x="420" y="55" width="340" height="70" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_o1)"/>
  <text x="590" y="77" text-anchor="middle" fill="#86efac" font-size="13" font-weight="700">规则裁决</text>
  <text x="590" y="103" text-anchor="middle" fill="#94a3b8" font-size="9">5 条规则在每个阶段对照校验</text>
  <rect x="40" y="145" width="340" height="70" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_o1)"/>
  <text x="210" y="167" text-anchor="middle" fill="#fde68a" font-size="13" font-weight="700">流水线编排</text>
  <text x="210" y="193" text-anchor="middle" fill="#94a3b8" font-size="9">6 阶段流水线调度者，不可跳步</text>
  <rect x="420" y="145" width="340" height="70" rx="8" fill="#a855f7" opacity="0.12" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_o1)"/>
  <text x="590" y="167" text-anchor="middle" fill="#d8b4fe" font-size="13" font-weight="700">上下文维护</text>
  <text x="590" y="193" text-anchor="middle" fill="#94a3b8" font-size="9">CONTEXT.md 共享语言机制</text>
  <text x="400" y="255" text-anchor="middle" fill="#64748b" font-size="11">定义文件：.harness/agents/owner.md · 由 apply-harness 初始化时渲染</text>
  <text x="400" y="280" text-anchor="middle" fill="#475569" font-size="10">它是"应用负责人"的 AI 化身</text>
</svg>
```

## 一、问题：每次对话都从零开始

### 1.1 普通 AI 助手的三个致命缺陷

普通 AI 助手在项目里工作的方式，有一个根本性问题：**它没有记忆，也没有身份**。

每一次新对话，AI 都要重新回答三个问题：

1. **这是什么项目？**——项目名、语言、技术栈、构建工具，每次都要重新介绍；
2. **规则是什么？**——编码规范、架构约束、流程纪律，对话一长就记不住；
3. **到哪一步了？**——需求理清没有、测试写了没有、评审过没过，全靠上下文里翻。

这三个缺陷带来三个后果：**身份每次重建、规则随上下文消失、流程靠人提醒**。AI 写得越多，越容易跑偏——因为它记不住"这个项目到底怎么回事"。

### 1.2 Owner Agent：把"应用负责人"固化下来

**Owner Agent** 是 Harness 体系的核心——它是"应用负责人"的 AI 化身，持有项目身份、规则、技能和上下文，编排整个 6 阶段流水线。

它不是一段即兴的提示词，而是一个**持久化的实体**：

- 定义文件位于 `.harness/agents/owner.md`；
- 由 `apply-harness` 技能在初始化时根据项目参数**自动渲染生成**；
- 每次对话开始时加载，全程持有身份、规则、流程、上下文。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 280" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_o2" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_o2"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="280" fill="url(#bg_o2)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">普通助手 vs Owner Agent</text>
  <rect x="40" y="50" width="340" height="95" rx="8" fill="#ef4444" opacity="0.12" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_o2)"/>
  <text x="210" y="72" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">普通 AI 助手</text>
  <text x="210" y="96" text-anchor="middle" fill="#94a3b8" font-size="9">身份：通用，每次对话重新建立</text>
  <text x="210" y="116" text-anchor="middle" fill="#94a3b8" font-size="9">规则：随上下文消失</text>
  <text x="210" y="136" text-anchor="middle" fill="#94a3b8" font-size="9">流程：无固定流程</text>
  <rect x="420" y="50" width="340" height="95" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_o2)"/>
  <text x="590" y="72" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">Owner Agent</text>
  <text x="590" y="96" text-anchor="middle" fill="#94a3b8" font-size="9">身份：持有项目身份，持久化</text>
  <text x="590" y="116" text-anchor="middle" fill="#94a3b8" font-size="9">规则：5 条规则存 .harness/rules/</text>
  <text x="590" y="136" text-anchor="middle" fill="#94a3b8" font-size="9">流程：6 阶段流水线，不可跳步</text>
  <rect x="40" y="165" width="720" height="60" rx="8" fill="#475569" opacity="0.22" stroke="#94a3b8" stroke-width="1.5" filter="url(#sh_o2)"/>
  <text x="400" y="190" text-anchor="middle" fill="#cbd5e1" font-size="11" font-weight="700">追溯：变更卡 + 评审报告 + 验证报告（而非聊天记录）</text>
  <text x="400" y="214" text-anchor="middle" fill="#94a3b8" font-size="9">术语：CONTEXT.md 持久积累（而非每次重新定义）</text>
  <text x="400" y="260" text-anchor="middle" fill="#475569" font-size="10">Owner Agent 把"人肉记忆"变成"仓库里的文件"</text>
</svg>
```

## 二、身份持有：知道"我是谁"

Owner Agent 持有的第一样东西是身份。它知道自己服务的应用到底是什么：

- 项目名称
- 语言和技术栈
- 构建工具和测试框架
- 代码规范和架构约束工具

这些信息不是对话里临时说的，而是写在 `.harness/agents/owner.md` 里的**参数化模板渲染结果**。`apply-harness` 技能检测项目语言后，将模板中的占位符替换为实际值：

| 参数 | 示例（Java） |
|------|-------------|
| `{{PROJECT_NAME}}` | `my-app` |
| `{{LANGUAGE}}` | `Java` |
| `{{LANGUAGE_DESC}}` | `Java、Spring Boot、多模块 Maven 架构` |
| `{{LANGUAGE_RUNTIME}}` | `JDK 21 LTS` |
| `{{FRAMEWORK_VER}}` | `Spring Boot 3.x+` |
| `{{BUILD_TOOL}}` | `Maven 3.9+` |
| `{{TEST_FRAMEWORK}}` | `JUnit 5 + Mockito + AssertJ` |
| `{{COV_TOOL}}` | `JaCoCo` |
| `{{LINT_TOOL}}` | `Checkstyle + PMD + SpotBugs` |
| `{{ARCH_TEST_TOOL}}` | `ArchUnit` |
| `{{DB_ACCESS}}` | `MyBatis-Plus / JPA + Flyway` |

**为什么身份必须参数化？** 因为 Owner Agent 要为任何语言的项目工作。Java 有 Maven、JUnit、JaCoCo；Go 有 go test、golangci-lint；Python 有 pytest、ruff。同一个模板，渲染出各自语言的 Owner Agent——**身份不是写死的，是按项目渲染的**。

## 三、规则裁决：知道"什么能做"

### 3.1 持有 5 条规则

Owner Agent 持有全部 5 条规则，在流水线每个阶段对照规则校验输出：

| 规则 | 校验内容 |
|------|---------|
| 编码规范 | 编码规范是否遵守？ |
| 工程结构 | 工程结构是否合规？ |
| SDD-TDD 模式 | 是否遵循规格驱动/测试驱动？ |
| 开发流程 | 开发流程是否跳步？ |
| 运行时可靠性 | 运行时可靠性是否满足？ |

五条规则覆盖了开发的全维度：**怎么写（规范）、放哪（结构）、怎么证明（SDD-TDD）、怎么走（流程）、怎么稳（可靠性）**。

### 3.2 规则为什么持久化存储？

规则不是写在 Owner Agent 的提示词里，而是持久存储在 `.harness/rules/` 目录下。

普通助手的问题是"规则随上下文消失"——对话一长，规则被挤出上下文；换一个对话，规则彻底丢失。Owner Agent 把规则放进仓库：

- **每次对话都从磁盘加载**，不依赖上下文长度；
- **每个阶段都对照校验**，不依赖记忆；
- **规则可被 review**，变更规则走版本控制。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 300" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_o3" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_o3"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="300" fill="url(#bg_o3)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">5 条规则 · 每个阶段对照校验</text>
  <rect x="60" y="50" width="200" height="55" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_o3)"/>
  <text x="160" y="72" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">编码规范</text>
  <text x="160" y="94" text-anchor="middle" fill="#94a3b8" font-size="9">怎么写</text>
  <rect x="300" y="50" width="200" height="55" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_o3)"/>
  <text x="400" y="72" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">工程结构</text>
  <text x="400" y="94" text-anchor="middle" fill="#94a3b8" font-size="9">放哪</text>
  <rect x="540" y="50" width="200" height="55" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_o3)"/>
  <text x="640" y="72" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">SDD-TDD 模式</text>
  <text x="640" y="94" text-anchor="middle" fill="#94a3b8" font-size="9">怎么证明</text>
  <rect x="60" y="125" width="200" height="55" rx="8" fill="#a855f7" opacity="0.12" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_o3)"/>
  <text x="160" y="147" text-anchor="middle" fill="#d8b4fe" font-size="12" font-weight="700">开发流程</text>
  <text x="160" y="169" text-anchor="middle" fill="#94a3b8" font-size="9">怎么走</text>
  <rect x="300" y="125" width="200" height="55" rx="8" fill="#ef4444" opacity="0.12" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_o3)"/>
  <text x="400" y="147" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">运行时可靠性</text>
  <text x="400" y="169" text-anchor="middle" fill="#94a3b8" font-size="9">怎么稳</text>
  <rect x="540" y="125" width="200" height="55" rx="8" fill="#475569" opacity="0.25" stroke="#94a3b8" stroke-width="1.5" filter="url(#sh_o3)"/>
  <text x="640" y="147" text-anchor="middle" fill="#cbd5e1" font-size="12" font-weight="700">.harness/rules/</text>
  <text x="640" y="169" text-anchor="middle" fill="#94a3b8" font-size="9">持久化存储</text>
  <text x="400" y="222" text-anchor="middle" fill="#475569" font-size="11">每个阶段：harnessing → coding → testing → reviewing → ci → verifying</text>
  <text x="400" y="246" text-anchor="middle" fill="#64748b" font-size="10">都对照规则校验输出，违规即停</text>
  <text x="400" y="284" text-anchor="middle" fill="#475569" font-size="10">规则进仓库 = 可加载、可对照、可评审</text>
</svg>
```

## 四、流水线编排：知道"下一步做什么"

### 4.1 6 阶段流水线的调度者

Owner Agent 是 6 阶段流水线的调度者。用户一句话需求进来，它按流水线逐步推进：

```
你 → 一句话需求 → Owner Agent
                    ↓
             ① harnessing  →  规格卡
                    ↓
             ② coding-skill      →  实现代码
                    ↓
             ③ unit-test-write   →  测试套件
                    ↓
             ④ expert-reviewer   →  评审报告
                    ↓
             ⑤ unit-test-ci      →  门禁结果
                    ↓
             ⑥ deploy-verify     →  验证报告
                    ↓
              交付
```

每个阶段的产出物都有明确的落点：规格卡、实现代码、测试套件、评审报告、门禁结果、验证报告——**六步六产出，步步有留档**。

### 4.2 为什么编排必须由 Agent 来做？

把"下一步做什么"交给 Owner Agent 而非用户在对话里指挥，有三个理由：

1. **流程一致性**——同样的需求，今天走和明天走，走的是同一条流水线，产出同样的中间物；
2. **抗遗忘**——对话进行到第 20 轮，AI 不会忘记这是 coding 阶段还是 testing 阶段，因为阶段由流水线状态机驱动，不靠上下文提醒；
3. **抗跳步**——评审没过不能进 CI，CI 没绿不能部署——跳步被流水线拦截，而不是靠"记得"。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 320" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_o4" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_o4"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="320" fill="url(#bg_o4)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">6 阶段流水线：六步六产出</text>
  <rect x="30" y="50" width="115" height="150" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_o4)"/>
  <text x="87" y="75" text-anchor="middle" fill="#93c5fd" font-size="11" font-weight="700">harnessing</text>
  <text x="87" y="98" text-anchor="middle" fill="#94a3b8" font-size="9">规格卡</text>
  <text x="87" y="118" text-anchor="middle" fill="#64748b" font-size="8">AC+边界</text>
  <text x="87" y="138" text-anchor="middle" fill="#64748b" font-size="8">+约束+策略</text>
  <rect x="155" y="50" width="115" height="150" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_o4)"/>
  <text x="212" y="75" text-anchor="middle" fill="#86efac" font-size="11" font-weight="700">coding-skill</text>
  <text x="212" y="98" text-anchor="middle" fill="#94a3b8" font-size="9">实现代码</text>
  <text x="212" y="118" text-anchor="middle" fill="#64748b" font-size="8">失败测试</text>
  <text x="212" y="138" text-anchor="middle" fill="#64748b" font-size="8">+最小实现</text>
  <rect x="280" y="50" width="115" height="150" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_o4)"/>
  <text x="337" y="75" text-anchor="middle" fill="#fde68a" font-size="11" font-weight="700">unit-test-write</text>
  <text x="337" y="98" text-anchor="middle" fill="#94a3b8" font-size="9">测试套件</text>
  <text x="337" y="118" text-anchor="middle" fill="#64748b" font-size="8">覆盖率≥80%</text>
  <rect x="405" y="50" width="115" height="150" rx="8" fill="#a855f7" opacity="0.12" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_o4)"/>
  <text x="462" y="75" text-anchor="middle" fill="#d8b4fe" font-size="11" font-weight="700">expert-reviewer</text>
  <text x="462" y="98" text-anchor="middle" fill="#94a3b8" font-size="9">评审报告</text>
  <text x="462" y="118" text-anchor="middle" fill="#64748b" font-size="8">0 个🔴放行</text>
  <rect x="530" y="50" width="115" height="150" rx="8" fill="#ef4444" opacity="0.12" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_o4)"/>
  <text x="587" y="75" text-anchor="middle" fill="#fca5a5" font-size="11" font-weight="700">unit-test-ci</text>
  <text x="587" y="98" text-anchor="middle" fill="#94a3b8" font-size="9">门禁结果</text>
  <text x="587" y="118" text-anchor="middle" fill="#64748b" font-size="8">全绿</text>
  <rect x="655" y="50" width="115" height="150" rx="8" fill="#0ea5e9" opacity="0.12" stroke="#0ea5e9" stroke-width="1.5" filter="url(#sh_o4)"/>
  <text x="712" y="75" text-anchor="middle" fill="#7dd3fc" font-size="11" font-weight="700">deploy-verify</text>
  <text x="712" y="98" text-anchor="middle" fill="#94a3b8" font-size="9">验证报告</text>
  <text x="712" y="118" text-anchor="middle" fill="#64748b" font-size="8">冒烟+回滚</text>
  <text x="400" y="240" text-anchor="middle" fill="#475569" font-size="12">六步六产出，步步有留档，不可跳步</text>
  <rect x="150" y="258" width="500" height="40" rx="8" fill="#1e293b" opacity="0.9" stroke="#64748b" stroke-width="1.5" filter="url(#sh_o4)"/>
  <text x="400" y="283" text-anchor="middle" fill="#94a3b8" font-size="10">状态机：drafting → reviewing → approved → coding → testing → reviewing → ci → verifying → done</text>
</svg>
```

## 五、上下文维护：知道"术语是什么"

### 5.1 CONTEXT.md：共享语言机制

Owner Agent 维护 `.harness/CONTEXT.md`，这是项目的**共享语言机制**，确保多轮对话中术语一致。

在软件开发里，术语不一致是隐形杀手：

- 今天叫"订单"，明天叫"交易"——代码里两套命名，改需求时发现是同一个东西；
- AI 说"验收条件"，业务说"通过标准"——评审时对不上，谁也没错，就是各说各话；
- 新对话开始，AI 重新发明一套术语——上次的产物和这次的产物没法对接。

CONTEXT.md 把术语钉死：**项目里"订单"就是订单，AC 就是 AC，change 就是 change**——每次对话加载，术语从文件来，不从记忆来。

### 5.2 为什么术语要持久积累？

普通助手的术语是"每次重新定义"——第 1 轮定义一次，第 50 轮可能已经漂移。Owner Agent 的术语是"CONTEXT.md 持久积累"：

- 新术语进入 CONTEXT.md，旧术语被修正也在 CONTEXT.md；
- 术语的演化全程可追溯（git 历史）；
- 换对话、换人、换 AI，术语不变——**项目语言属于仓库，不属于某次对话**。

## 六、Owner Agent 的生产：参数化模板渲染

### 6.1 从模板到实例

Owner Agent 不是手写的，而是由 `apply-harness` 技能在初始化时自动生成。生成过程：

1. `apply-harness` 检测项目语言和技术栈；
2. 读取 Owner Agent 模板（含 `{{PLACEHOLDER}}`）；
3. 将占位符替换为实际参数值；
4. 渲染生成 `.harness/agents/owner.md`。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 260" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_o5" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_o5"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="260" fill="url(#bg_o5)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">参数化模板渲染</text>
  <rect x="40" y="55" width="160" height="95" rx="8" fill="#1e293b" opacity="0.9" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_o5)"/>
  <text x="120" y="80" text-anchor="middle" fill="#e2e8f0" font-size="11" font-weight="700">模板</text>
  <text x="120" y="104" text-anchor="middle" fill="#94a3b8" font-size="9">{{LANGUAGE}}</text>
  <text x="120" y="124" text-anchor="middle" fill="#94a3b8" font-size="9">{{BUILD_TOOL}}</text>
  <text x="120" y="144" text-anchor="middle" fill="#94a3b8" font-size="9">{{TEST_FRAMEWORK}}</text>
  <text x="230" y="102" text-anchor="middle" fill="#64748b" font-size="14">→</text>
  <rect x="260" y="55" width="160" height="95" rx="8" fill="#1e293b" opacity="0.9" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_o5)"/>
  <text x="340" y="80" text-anchor="middle" fill="#e2e8f0" font-size="11" font-weight="700">检测</text>
  <text x="340" y="104" text-anchor="middle" fill="#94a3b8" font-size="9">pom.xml → Java</text>
  <text x="340" y="124" text-anchor="middle" fill="#94a3b8" font-size="9">go.mod → Go</text>
  <text x="340" y="144" text-anchor="middle" fill="#94a3b8" font-size="9">pyproject → Python</text>
  <text x="450" y="102" text-anchor="middle" fill="#64748b" font-size="14">→</text>
  <rect x="480" y="55" width="280" height="95" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_o5)"/>
  <text x="620" y="80" text-anchor="middle" fill="#86efac" font-size="11" font-weight="700">渲染生成 owner.md</text>
  <text x="620" y="104" text-anchor="middle" fill="#94a3b8" font-size="9">.harness/agents/owner.md</text>
  <text x="620" y="124" text-anchor="middle" fill="#94a3b8" font-size="9">Java → Maven + JUnit 5 + JaCoCo</text>
  <text x="620" y="144" text-anchor="middle" fill="#94a3b8" font-size="9">Go → go test + golangci-lint</text>
  <text x="400" y="196" text-anchor="middle" fill="#475569" font-size="11">同一套模板 → 不同语言的 Owner Agent</text>
  <text x="400" y="238" text-anchor="middle" fill="#64748b" font-size="9">身份是渲染的结果，不是手写的产物</text>
</svg>
```

### 6.2 为什么是参数化而非多套模板？

参数化的好处是**单一真相源**：

- 规则变了，只改模板一处，所有语言的 Owner Agent 同步更新；
- 新语言接入，只需补充一份参数映射，不必重写整套规则；
- Agent 的行为逻辑（规则裁决、流水线编排、上下文维护）与语言无关，语言只影响参数值。

## 七、与普通 AI 助手的区别——一张表看懂

| 维度 | 普通 AI 助手 | Owner Agent |
|------|------------|-------------|
| 身份 | 通用，每次对话重新建立 | 持有项目身份，持久化 |
| 规则 | 随上下文消失 | 5 条规则持久存储在 `.harness/rules/` |
| 流程 | 无固定流程 | 6 阶段流水线，不可跳步 |
| 追溯 | 聊天记录 | 变更卡 + 评审报告 + 验证报告 |
| 术语 | 每次重新定义 | CONTEXT.md 持久积累 |

### 7.1 五个维度的本质差异

- **身份**：普通助手是"空的 AI"，Owner Agent 是"这个项目的 AI"——它知道项目叫什么、用什么语言、怎么构建、怎么测试；
- **规则**：普通助手的规则活不过对话，Owner Agent 的规则活在仓库里——对话结束，规则还在；
- **流程**：普通助手想到哪做到哪，Owner Agent 按流水线推进——评审没过不进 CI，CI 没绿不部署；
- **追溯**：普通助手的产出只存在于聊天记录里，Owner Agent 的产出是变更卡、评审报告、验证报告——**可回放、可审计**；
- **术语**：普通助手每次重新发明，Owner Agent 从 CONTEXT.md 加载——**项目语言是资产，不是临时约定**。

### 7.2 变化的本质：从"对话"到"工程"

普通助手的工作方式是"对话"：上下文决定一切，对话结束一切归零。Owner Agent 的工作方式是"工程"：**文件系统是记忆，规则是约束，流水线是流程，留档是审计**。

这不是给 AI 加了一层壳，而是改变了 AI 工作的基本单位——从"一轮对话"变成"一个变更"。变更卡驱动，状态机流转，每一步都有产出物落在仓库里——**AI 的工作从此可验证、可回放、可交接**。

## 八、完成标志与边界

### 8.1 完成标志

一个项目真正"拥有" Owner Agent，四件事必须成立：

1. **`.harness/agents/owner.md` 存在且参数正确**——身份渲染无误，语言/构建/测试/规范工具与实际项目一致；
2. **`.harness/rules/` 五条规则在册**——编码规范、工程结构、SDD-TDD、开发流程、运行时可靠性，每条都有明确校验内容；
3. **流水线可被编排**——新需求能走完 6 阶段且不跳步，每阶段有产出物留档；
4. **`.harness/CONTEXT.md` 有内容**——术语已初始化，多轮对话保持一致。

### 8.2 边界：Owner Agent 不管什么

- 不替用户做业务决策——规格要用户确认，验收标准要用户把关；
- 不生成业务鉴权规则——那是 Controller/业务层的事；
- 不替代人工评审——expert-reviewer 给出评审报告，是否放行仍按流程；
- Owner Agent 管"流程和纪律"，不管"业务怎么设计"。

## 九、写在最后

Owner Agent 的全部设计，浓缩成四句话：

1. **身份是渲染的不是手写的**——apply-harness 按项目参数生成 owner.md，任何语言都能接入。
2. **规则进仓库不进上下文**——5 条规则持久化，每个阶段对照校验，对话结束规则还在。
3. **流程靠状态机不靠记忆**——6 阶段流水线驱动，不可跳步，产出物步步留档。
4. **术语靠 CONTEXT.md 不靠约定**——共享语言机制，多轮对话术语一致。

一句话记住它：**Owner Agent 是项目的"AI 负责人"——身份参数化渲染、规则持久化存储、流水线状态机编排、上下文 CONTEXT.md 维护，把"项目是谁、规则是什么、走到哪一步、术语怎么叫"全部固化进仓库，让 AI 从"每次从零开始"变成"全程持有项目记忆"。**