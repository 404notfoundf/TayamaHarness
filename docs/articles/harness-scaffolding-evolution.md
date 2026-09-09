# 我是如何用 19 轮需求拷问，炼成跨语言 Harness 脚手架

> 人类设计约束，AI 编写代码，机器验证质量。
> —— 这篇文章讲的是：这句信条背后的工程制品，是如何一步步从某个项目的私有目录，长成一个可分发、可迁移、覆盖五种语言的脚手架。

## 写在前面

这篇文章的素材不是想象，而是一段真实发生的演进史。它的起点是一个再普通不过的需求："我想把当前项目 `.harness` 目录下的所有内容，改造为一个开箱即用的命令行工具"。终点是一个公开仓库：`tayama-harness-skills`——支持 Java / Python / Go / Rust / Frontend 五种语言、80+ 个框架差异块、51 个参数键、30+ 个可执行技能的跨语言工程脚手架。

中间隔了 19 轮需求讨论、10 个 git 里程碑、3 个主版本。我把这整个过程拆成十个章节讲给你听——不是为了展示结果有多漂亮，而是想让你看到：**一个"通用工具"是怎么从"一次性代码"里长出来的**。哪些判断让项目少走了弯路，哪些设计在三个月后被证明是正确的，又有哪些教训是在第 N 轮重构时才明白的。

在开始之前，先交代这篇文章的"数据来源"——它不来自任何人的回忆，而来自仓库自身的记录：10 个 git 提交（从 `29ff0aa Initial commit` 到 `b6dcbd4` 支持 Rust/PHP 与技能抽离）、4 个 CHANGELOG 版本条目（1.0.0 → 1.1.0 → 1.2.0 → 1.3.0 → 未发布的重构条目）、一份 2333 行的编排脚本 `apply-harness/SKILL.md`、以及 26 个技能目录与 6 篇语言文档。文中的每一个数字，你都可以在仓库里找到对应物。这本身就是一个信号：**一个成熟的项目，连自己的演进历史都要能被核查**。

为了让你在阅读时能实时对账，先把 19 条提示词与章节的映射关系摆在这里——每章开头也会标出所对应的提示词：

| 提示词 | 核心诉求 | 对应章节 |
|--------|---------|---------|
| #1 | 参考 grill-me，改造为开箱即用工具，兼容 Go/Python 等跨语言，四维度规范 | 第一章 缘起 |
| #2 | 你遗漏了 agents/owner.md，这是灵魂 | 第二章 灵魂 |
| #3 | 基于上述方案实现项目 | 第三章 第一版落地 |
| #4 | 参考 skills.docx 检查可完善之处 | 第四章 对照检查 |
| #5 | grill-me 改名 harness-me，吸收 mattpocock 精华，不删减现有功能 | 第四章 对照检查 |
| #6 | 技能之间能否关联（change → review → verify） | 第五章 技能关联 |
| #7 | harness-xxx/rules 重复，抽离公共部分到 harness-core | 第六章 两轮抽离 |
| #8 | mattpocock/skills/engineering 还有哪些可吸收 | 第七章 精华吸收 |
| #9 | 按优先级实现精华 skills，与现有 skills 整合关联 | 第七章 精华吸收 |
| #10 | 每个语言下的 skills 是否需抽离为公共 skills | 第六章 两轮抽离 |
| #11 | 核心公共逻辑抽离到模板 | 第六章 两轮抽离 |
| #12 | 不限工具（codex/claudecode/reasonix 等）+ install_skill 一键安装 | 第八章 工具无关 |
| #13 | 并行开发时只输入命令不行 | 第九章 并行开发 |
| #14 | 多 change 时如何处理哪个 change | 第五章 / 第八章 |
| #15 | 拓展业界主流框架识别 | 第十章 框架扩张 |
| #16 | 框架清单扩张 + 语言包 skill 未同步更新问题 | 第十章 框架扩张 |
| #17 | 新增 Rust / PHP 及主流框架 | 第十章 框架扩张 |
| #18 | 语言包流水线技能与 core 重复，能否删除（仅讨论） | 第十一章 方案 A |
| #19 | 按方案 A 实施清理 | 第十一章 方案 A |


```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 900 320" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_tl" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_tl"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="900" height="320" fill="url(#bg_tl)" rx="10"/>
  <text x="450" y="30" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">演进时间线：从 .harness 到跨语言脚手架</text>
  <line x1="60" y1="140" x2="840" y2="140" stroke="#334155" stroke-width="2"/>
  <polygon points="845,140 835,134 835,146" fill="#64748b"/>
  <g>
    <circle cx="105" cy="140" r="7" fill="#38bdf8"/>
    <text x="105" y="120" text-anchor="middle" fill="#e2e8f0" font-size="11">08-05</text>
    <text x="105" y="172" text-anchor="middle" fill="#94a3b8" font-size="10">Initial commit</text>
    <text x="105" y="188" text-anchor="middle" fill="#64748b" font-size="10">空仓库</text>
  </g>
  <g>
    <circle cx="220" cy="140" r="7" fill="#38bdf8"/>
    <text x="220" y="120" text-anchor="middle" fill="#e2e8f0" font-size="11">08-06</text>
    <text x="220" y="172" text-anchor="middle" fill="#94a3b8" font-size="10">feat: 初版</text>
    <text x="220" y="188" text-anchor="middle" fill="#64748b" font-size="10">单语言试点</text>
  </g>
  <g>
    <circle cx="335" cy="140" r="7" fill="#38bdf8"/>
    <text x="335" y="120" text-anchor="middle" fill="#e2e8f0" font-size="11">08-07</text>
    <text x="335" y="172" text-anchor="middle" fill="#94a3b8" font-size="10">吸收 mattpocock 精华</text>
    <text x="335" y="188" text-anchor="middle" fill="#64748b" font-size="10">技能抽离开始</text>
  </g>
  <g>
    <circle cx="450" cy="140" r="7" fill="#38bdf8"/>
    <text x="450" y="120" text-anchor="middle" fill="#e2e8f0" font-size="11">08-10</text>
    <text x="450" y="172" text-anchor="middle" fill="#94a3b8" font-size="10">AI 工具识别</text>
    <text x="450" y="188" text-anchor="middle" fill="#64748b" font-size="10">install-skill 一键安装</text>
  </g>
  <g>
    <circle cx="565" cy="140" r="7" fill="#38bdf8"/>
    <text x="565" y="120" text-anchor="middle" fill="#e2e8f0" font-size="11">08-11</text>
    <text x="565" y="172" text-anchor="middle" fill="#94a3b8" font-size="10">v1.2.0 框架扩张</text>
    <text x="565" y="188" text-anchor="middle" fill="#64748b" font-size="10">30+ 框架识别</text>
  </g>
  <g>
    <circle cx="680" cy="140" r="7" fill="#38bdf8"/>
    <text x="680" y="120" text-anchor="middle" fill="#e2e8f0" font-size="11">08-14</text>
    <text x="680" y="172" text-anchor="middle" fill="#94a3b8" font-size="10">v1.3.0 Rust/PHP</text>
    <text x="680" y="188" text-anchor="middle" fill="#64748b" font-size="10">新增 Rust/PHP 生态</text>
  </g>
  <g>
    <circle cx="795" cy="140" r="7" fill="#3b82f6"/>
    <text x="795" y="120" text-anchor="middle" fill="#e2e8f0" font-size="11">Unreleased</text>
    <text x="795" y="172" text-anchor="middle" fill="#94a3b8" font-size="10">方案 A 抽离方案</text>
    <text x="795" y="188" text-anchor="middle" fill="#64748b" font-size="10">10 技能收敛 core</text>
  </g>
  <text x="450" y="240" text-anchor="middle" fill="#c7d2fe" font-size="12">10 个 git 里程碑 · 3 个主版本 · 19 轮需求讨论</text>
  <text x="450" y="265" text-anchor="middle" fill="#94a3b8" font-size="11">每一轮"能不能再抽一层？"最终沉淀为：参数表驱动、单一事实源、模板渲染</text>
  <text x="450" y="295" text-anchor="middle" fill="#475569" font-size="11">6 语言 · 111 框架差异块 · 51 参数键 · 30+ 技能 · 2333 行编排</text>
</svg>
```

## 一、缘起：当"一次性目录"想变成"一键工具"

> 🎯 **对应提示词 #1**：参考 grill-me 的一键安装体验，把 `.harness` 改造为可分发工具，必须兼容 Go、Python 等跨语言项目；Java/Python/Golang 规范均需包含 SDD-TDD 模式、编码规范、工程结构、开发流程规划四个维度

故事的开始，是 tayama-trip-plan 这个项目。它有一套运行良好的 `.harness` 目录：里面有 rules（SDD-TDD 模式、编码规范、工程结构、开发流程规范）、有 agents/owner.md，还有一个接一个的技能。这套东西在项目里已经证明了价值——AI 写代码、机器验证质量，流水线跑得顺。

但"好用"恰恰是麻烦的开始。任何一个在项目里沉淀过规范的人都会遇到同样的三连问：

1. **第二个项目怎么办？** 把 `.harness` 整个复制过去？那以后改规范要改 N 份。
2. **换个语言怎么办？** Java 的规范里全是 Maven、Spring Boot 的味道，一个 Go 项目怎么用？
3. **别人怎么用？** 总不能让人去读一个私有项目的目录结构，再手工搬文件。

于是有了最初的需求原型，其中带着一个具体的参照物：grill-me（mattpocock/skills 的经典用法）——通过 `npx skills@latest add mattpocock/skills` 一键安装，在项目根目录执行 `/grill-me` 即可应用规范。用户想要的，就是 Harness 规范也能以类似方式分发与安装，并且"必须兼容 Go、Python 等跨语言项目"。

这句话里藏着整篇文章最重要的一个判断：**"开箱即用"不是打包问题，是抽象问题**。你没法把 Java 项目的规范"打包"给 Go 项目用——除非你先想清楚：哪些是 Java 的，哪些是 Harness 的。这个问题的答案，决定了后面所有架构走向。

那么 grill-me 到底神奇在哪？拆开看，它的本质是一个"SKILL.md 体系"：技能是一份带 frontmatter（name、description 等元信息）的 Markdown 文件，通过 `npx skills@latest add` 一键拉取到项目，AI 工具原生识别这些文件，把斜杠命令注册进自己的能力清单。用户不需要理解"技能文件放哪、怎么配"，只需要知道一个命令名。

这个机制的启示是：**技能的"形态"本身就是协议**。SKILL.md 不是给人看的文档，是给 AI 工具看的"可执行文件"——name 决定斜杠命令名，description 决定 AI 何时调用它，正文决定它执行什么。mattpocock 把"给 AI 写提示词"升级成了"给 AI 分发技能"，而 Harness 要做的，是把这套协议**工程化**：不是分发一个孤立的技能，而是分发一整条带参数的流水线。

> 核心收获：迁移一个"工程制品"到其他项目，本质是把它从"项目私货"里**剥离出通用内核**。剥离得越早，后面每一轮扩展的成本越低。

## 二、灵魂：owner.md 差点被漏掉

> 🎯 **对应提示词 #2**："还有一个关键的你遗漏了，那就是 `agents/owner.md`，这是灵魂啊，请重新给出新的实施方案"

第一版讨论方案时，有一个致命的遗漏：所有人都在讨论"规则"和"技能"怎么抽离，却没人提 `agents/owner.md`。直到有人点破——"**这是灵魂啊**"。

owner.md 是什么？它是"应用负责人智能体"的定义：你是谁、你在这个项目里怎么工作、你遵循什么工作方式。它不是一份配置，而是一段**人格与工作协议**。6 阶段流水线（harnessing → coding-skill → unit-test-write → expert-reviewer → unit-test-ci → deploy-verify）不是凭空跑的，是 Owner Agent 编排的；规则不是冷冰冰的文本，是 Owner Agent 承诺遵守的纪律。

这个"点破"改变了整个方案的结构：抽离的不只是 rules 和 skills，还有**承载它们的人设**。后来的实现里，owner.md 变成了参数化模板，躺在 `harness-core/templates/agents/` 下，里面是 `{{LANGUAGE}}`、`{{PROJECT_DESC}}` 之类的占位符，由 apply-harness 在安装时渲染成目标项目专属的 owner.md。一个 Java 项目装出来的 Owner Agent，和一个 Python 项目装出来的，**人格相同、语言不同、工具不同**。

owner.md 的重要性，在最终版的 apply-harness 里以"流程位置"的形式固定了下来：它排在**十步工作流的第三步**，仅次于检测（Step 1）和读项目名（Step 2）。完整的工作流是：

1. **Step 1 检测项目语言与框架** —— 扫描特征文件，识别语言与框架组合
2. **Step 2 读取项目名称** —— 从包名、模块名等推断项目身份
3. **Step 3 渲染 owner.md** —— 灵魂生成，人设落地
4. **Step 4 复制规则文件** —— 4 条通用 + 2 条语言特有
5. **Step 5 渲染技能模板 + 复制技能文件** —— 10 个模板渲染 + 语言包专属技能
6. **Step 5.5 注册技能到当前 AI 工具** —— 把渲染出的技能挂进工具能力清单
7. **Step 6 初始化变更追踪** —— changes 目录与 change.md 模板
8. **Step 7 初始化领域知识库** —— wiki 模板（业务模型/接口协议/数据模型/架构决策）
9. **Step 7.1 初始化共享语言上下文** —— CONTEXT.md，项目术语词典
10. **Step 8 输出项目摘要卡片** —— 告诉用户"你的项目被装成了什么样"

注意两个"内嵌步骤"（5.5 与 7.1）——它们是后面被"夹带"进去的，恰好说明这套流程是**长出来的**，不是一次设计出来的：技能注册是工具无关性的产物，共享语言上下文是领域建模的产物。灵魂排在第三位，但它定义了后面所有步骤的"人格基调"。

Owner Agent 的职责，文档里写了四条，每一道都对应脚手架的一个子系统：**身份持有**（记住项目是什么、用什么语言、遵守什么规范，对应参数表与 rules）、**规则裁决**（每个阶段对照规则校验输出，对应 6 条规则的执行）、**流水线编排**（从一句话需求到交付的门卫，对应 6 阶段技能链）、**上下文维护**（维护 CONTEXT.md 共享语言机制，保证多轮对话术语一致，对应 Step 7.1）。换句话说，**owner.md 不是一份"自我介绍"，而是一门"宪法"**——它不是被阅读的，是被执行的。这正是"灵魂"二字的准确含义。

把这一层关系画成图，就是 Owner Agent 的架构全景：一条人格居中，四根职责线接向四个子系统，六阶段流水线在底部由它编排执行：

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 900 460" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_soul" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_soul"><feDropShadow dx="0" dy="3" stdDeviation="4" flood-color="#000" flood-opacity="0.35"/></filter>
  </defs>
  <rect width="900" height="460" fill="url(#bg_soul)" rx="10"/>
  <text x="450" y="30" text-anchor="middle" fill="#e2e8f0" font-size="26" font-weight="700">OwnerAgent 架构：一个灵魂 · 四个子系统 · 六阶段流水线</text>
  <rect x="60" y="52" width="780" height="64" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#0ea5e9" stroke-width="1.5" filter="url(#sh_soul)"/>
  <text x="450" y="80" text-anchor="middle" fill="#7dd3fc" font-size="17" font-weight="700">Owner Agent · 应用负责人智能体（owner.md）</text>
  <text x="450" y="103" text-anchor="middle" fill="#94a3b8" font-size="11.5">人格与工作协议 · 参数化模板（{{LANGUAGE}} / {{PROJECT_DESC}}）→ apply-harness Step 3 渲染落地</text>
  <line x1="450" y1="118" x2="450" y2="166" stroke="#64748b" stroke-width="2"/>
  <g>
    <line x1="450" y1="166" x2="153" y2="166" stroke="#38bdf8" stroke-width="2"/>
    <line x1="153" y1="166" x2="153" y2="190" stroke="#38bdf8" stroke-width="2"/>
    <polygon points="153,200 148,192 158,192" fill="#38bdf8"/>
    <line x1="450" y1="166" x2="351" y2="166" stroke="#f59e0b" stroke-width="2"/>
    <line x1="351" y1="166" x2="351" y2="190" stroke="#f59e0b" stroke-width="2"/>
    <polygon points="351,200 346,192 356,192" fill="#f59e0b"/>
    <line x1="450" y1="166" x2="549" y2="166" stroke="#22c55e" stroke-width="2"/>
    <line x1="549" y1="166" x2="549" y2="190" stroke="#22c55e" stroke-width="2"/>
    <polygon points="549,200 544,192 554,192" fill="#22c55e"/>
    <line x1="450" y1="166" x2="747" y2="166" stroke="#8b5cf6" stroke-width="2"/>
    <line x1="747" y1="166" x2="747" y2="190" stroke="#8b5cf6" stroke-width="2"/>
    <polygon points="747,200 742,192 752,192" fill="#8b5cf6"/>
  </g>
  <g>
    <rect x="60" y="200" width="186" height="104" rx="10" fill="#1e293b" stroke="#38bdf8" stroke-width="1.25" filter="url(#sh_soul)"/>
    <text x="153" y="226" text-anchor="middle" fill="#7dd3fc" font-size="14" font-weight="700">01 · 身份持有</text>
    <text x="153" y="250" text-anchor="middle" fill="#cbd5e1" font-size="11">记住项目是什么、用什么语言</text>
    <text x="153" y="268" text-anchor="middle" fill="#cbd5e1" font-size="11">遵守什么规范</text>
    <text x="153" y="289" text-anchor="middle" fill="#64748b" font-size="10.5">→ 参数表 + rules</text>
  </g>
  <g>
    <rect x="258" y="200" width="186" height="104" rx="10" fill="#1e293b" stroke="#f59e0b" stroke-width="1.25" filter="url(#sh_soul)"/>
    <text x="351" y="226" text-anchor="middle" fill="#fde68a" font-size="14" font-weight="700">02 · 规则裁决</text>
    <text x="351" y="250" text-anchor="middle" fill="#cbd5e1" font-size="11">每个阶段对照规则校验输出</text>
    <text x="351" y="268" text-anchor="middle" fill="#cbd5e1" font-size="11">输出不达标即驳回</text>
    <text x="351" y="289" text-anchor="middle" fill="#64748b" font-size="10.5">→ 6 条规则的执行</text>
  </g>
  <g>
    <rect x="456" y="200" width="186" height="104" rx="10" fill="#1e293b" stroke="#22c55e" stroke-width="1.25" filter="url(#sh_soul)"/>
    <text x="549" y="226" text-anchor="middle" fill="#86efac" font-size="14" font-weight="700">03 · 流水线编排</text>
    <text x="549" y="250" text-anchor="middle" fill="#cbd5e1" font-size="11">从一句话需求到交付</text>
    <text x="549" y="268" text-anchor="middle" fill="#cbd5e1" font-size="11">六阶段的唯一门卫</text>
    <text x="549" y="289" text-anchor="middle" fill="#64748b" font-size="10.5">→ 6 阶段技能链</text>
  </g>
  <g>
    <rect x="654" y="200" width="186" height="104" rx="10" fill="#1e293b" stroke="#8b5cf6" stroke-width="1.25" filter="url(#sh_soul)"/>
    <text x="747" y="226" text-anchor="middle" fill="#c4b5fd" font-size="14" font-weight="700">04 · 上下文维护</text>
    <text x="747" y="250" text-anchor="middle" fill="#cbd5e1" font-size="11">维护 CONTEXT.md 术语词典</text>
    <text x="747" y="268" text-anchor="middle" fill="#cbd5e1" font-size="11">多轮对话不失真</text>
    <text x="747" y="289" text-anchor="middle" fill="#64748b" font-size="10.5">→ Step 7.1 共享语言</text>
  </g>
  <line x1="549" y1="304" x2="549" y2="330" stroke="#22c55e" stroke-width="2"/>
  <polygon points="549,334 544,328 554,328" fill="#22c55e"/>
  <text x="450" y="322" text-anchor="middle" fill="#c7d2fe" font-size="12">六阶段流水线 · Owner Agent 编排（一句话需求 → 交付）</text>
  <g font-size="11" fill="#e2e8f0">
    <rect x="60" y="334" width="120" height="44" rx="8" fill="#1e293b" stroke="#334155" filter="url(#sh_soul)"/><text x="120" y="360" text-anchor="middle">harnessing</text>
    <rect x="192" y="334" width="120" height="44" rx="8" fill="#1e293b" stroke="#334155" filter="url(#sh_soul)"/><text x="252" y="360" text-anchor="middle">coding-skill</text>
    <rect x="324" y="334" width="120" height="44" rx="8" fill="#1e293b" stroke="#334155" filter="url(#sh_soul)"/><text x="384" y="360" text-anchor="middle">unit-test-write</text>
    <rect x="456" y="334" width="120" height="44" rx="8" fill="#1e293b" stroke="#334155" filter="url(#sh_soul)"/><text x="516" y="360" text-anchor="middle">expert-reviewer</text>
    <rect x="588" y="334" width="120" height="44" rx="8" fill="#1e293b" stroke="#334155" filter="url(#sh_soul)"/><text x="648" y="360" text-anchor="middle">unit-test-ci</text>
    <rect x="720" y="334" width="120" height="44" rx="8" fill="#1e293b" stroke="#334155" filter="url(#sh_soul)"/><text x="780" y="360" text-anchor="middle">deploy-verify</text>
  </g>
  <g fill="#64748b">
    <line x1="181" y1="356" x2="191" y2="356" stroke="#64748b" stroke-width="2"/><polygon points="192,356 186,352 186,360" fill="#64748b"/>
    <line x1="313" y1="356" x2="323" y2="356" stroke="#64748b" stroke-width="2"/><polygon points="324,356 318,352 318,360" fill="#64748b"/>
    <line x1="445" y1="356" x2="455" y2="356" stroke="#64748b" stroke-width="2"/><polygon points="456,356 450,352 450,360" fill="#64748b"/>
    <line x1="577" y1="356" x2="587" y2="356" stroke="#64748b" stroke-width="2"/><polygon points="588,356 582,352 582,360" fill="#64748b"/>
    <line x1="709" y1="356" x2="719" y2="356" stroke="#64748b" stroke-width="2"/><polygon points="720,356 714,352 714,360" fill="#64748b"/>
  </g>
  <text x="450" y="408" text-anchor="middle" fill="#c7d2fe" font-size="12">owner.md 不是文档，是流程里始终在场的坐标系：身份不失忆 · 规则不失范 · 术语不失真</text>
  <text x="450" y="432" text-anchor="middle" fill="#64748b" font-size="11">6 个阶段技能 · 4 条职责 · 十步工作流中排在 Step 3（灵魂生成 · 人设落地）</text>
</svg>
```

> 核心收获：抽离任何"规范系统"时，先找"灵魂文件"——那个定义了系统如何自我运行的制品。漏掉它，抽出来的只是一堆没有主人的规则。

## 三、第一版落地：目录结构的分水岭

> 🎯 **对应提示词 #3**："请基于上述讨论以及实施方案进行实现这个项目"

方案确认后，第一版实现（git 里程碑 `20dc733 feat: 初版`）确立了一个直到今天都没变过的核心结构：

```
skills/
├── apply-harness/     # 编排入口：检测语言/框架 → 渲染参数 → 生成 .harness
├── harness-core/      # 跨语言通用骨架：规则、模板、通用技能
├── harness-java/      # Java 语言包
├── harness-python/    # Python 语言包
├── harness-golang/    # Go 语言包
└── harness-front/     # 前端语言包
```

这个结构暗含了第一个大判断：**"语言"是第一级差异维度**。脚手架不搞"一套配置到处跑"，而是给每种语言一个"包"——语言包内部是同构的（同样的 rules 目录、同样的技能命名、同样的占位符），语言之间的差异被压缩到参数表里，而不是散落在文件副本里。

同时确立了「三大支柱」的分工：

1. **参数表**——语言与框架差异的"唯一事实源"。Java 的构建命令、Python 的测试框架、Go 的竞态检测参数，全部以键值对形式集中定义。
2. **模板**——用占位符书写的技能文件。`{{BUILD_CMD}}`、`{{TEST_FRAMEWORK}}`、`{{MOCK_LIB}}`……渲染时一次性替换。
3. **渲染器**——apply-harness 本身就是一篇 2333 行的 SKILL.md，它不"复制粘贴"，它"检测 → 读参数表 → 渲染模板 → 注册技能"。

"检测"这一步值得多写两行，因为它是整个参数化机制的前置齿轮。检测的本质是**把项目文件翻译成参数**：扫描根目录的特征文件（`pom.xml` → Java + Maven、`build.gradle` → Java + Gradle、`go.mod` → Go、`requirements.txt` → Python、`Cargo.toml` → Rust + Cargo、`composer.json` → PHP + Composer、`package.json` → Node.js……），命中即锁定语言，再深挖出框架与构建工具，最终得到一个"语言 × 框架 × 构建工具"的三元组。检测表从第一版的"每种语言 10-20 条规则"，增长到方案 A 后的 "每种 11-35 条、六语言合计 134 条"。因为检测与参数块是一一映射的，**检测规则每加一条，参数表就必须加一个对应的块**——这个"检测 ↔ 参数"的同步约束，是后文所有框架扩张工作的总纲。

第一版只支持少数几种框架、一种语言（Java）。但它验证了一件最重要的事：**"改一行参数，而不是改一份文件"是可行的**。

第一版的"资产清单"（记录在 CHANGELOG 1.0.0 里）值得原样列出来，因为它清楚地标出了种子包含了什么：

- 支持 Java / Python / Go / Frontend **四种语言**
- 核心方法论：SDD-TDD、6 阶段流水线
- 5 条规则（编码规范、工程结构、开发流程、运行时可靠性、SDD-TDD 模式）
- 9 个技能（6 流水线 + 3 辅助）
- **变更状态机 + 模板系统**
- **共享语言机制（CONTEXT.md）**

注意最后两条——它们在第一版就埋下了。"变更状态机"长成了后来的 change → review → verify 三件套；"CONTEXT.md 共享语言机制"长成了 Step 7.1 的领域词典。**第一批种子决定了这棵树的形状**：它不是"规则库"，而是"状态机 + 上下文 + 流水线"。正是这个形状，让后面所有关于"关联"和"并行"的诉求都有地方安放。

第一版的六个阶段，后来长成了整个脚手架的骨架，一路走到今天——六个阶段、六个技能、六道出口门禁，严格按序，不可跳步：

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 900 330" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_pl" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
  </defs>
  <rect width="900" height="330" fill="url(#bg_pl)" rx="10"/>
  <text x="450" y="34" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">6 阶段流水线：六阶段 × 六技能 × 六门禁</text>
  <g font-size="12" text-anchor="middle">
    <rect x="40" y="80" width="120" height="52" rx="8" fill="#0ea5e9" opacity="0.85"/>
    <text x="100" y="101" fill="#fff" font-weight="700">① harnessing</text>
    <text x="100" y="119" fill="#e0f2fe">规格构建</text>
    <rect x="193" y="80" width="120" height="52" rx="8" fill="#14b8a6" opacity="0.85"/>
    <text x="253" y="101" fill="#fff" font-weight="700">② coding-skill</text>
    <text x="253" y="119" fill="#ccfbf1">测试先行实现</text>
    <rect x="346" y="80" width="120" height="52" rx="8" fill="#8b5cf6" opacity="0.85"/>
    <text x="406" y="101" fill="#fff" font-weight="700">③ unit-test-write</text>
    <text x="406" y="119" fill="#ede9fe">测试完善</text>
    <rect x="499" y="80" width="120" height="52" rx="8" fill="#f59e0b" opacity="0.85"/>
    <text x="559" y="101" fill="#fff" font-weight="700">④ expert-reviewer</text>
    <text x="559" y="119" fill="#fef3c7">专家评审</text>
    <rect x="652" y="80" width="120" height="52" rx="8" fill="#ef4444" opacity="0.85"/>
    <text x="712" y="101" fill="#fff" font-weight="700">⑤ unit-test-ci</text>
    <text x="712" y="119" fill="#fee2e2">CI 门禁</text>
    <rect x="790" y="80" width="90" height="52" rx="8" fill="#3b82f6" opacity="0.85"/>
    <text x="835" y="101" fill="#fff" font-weight="700">⑥ deploy</text>
    <text x="835" y="119" fill="#dbeafe">部署验证</text>
  </g>
  <g stroke="#64748b" stroke-width="1.6">
    <line x1="160" y1="106" x2="193" y2="106"/>
    <line x1="313" y1="106" x2="346" y2="106"/>
    <line x1="466" y1="106" x2="499" y2="106"/>
    <line x1="619" y1="106" x2="652" y2="106"/>
    <line x1="772" y1="106" x2="790" y2="106"/>
  </g>
  <g font-size="10.5" fill="#94a3b8">
    <text x="100" y="170" text-anchor="middle">出口：规格明确 · AC 可测</text>
    <text x="100" y="184" text-anchor="middle">≥3 边界情况 · 策略清晰</text>
    <text x="253" y="170" text-anchor="middle">出口：失败测试先存在并转绿</text>
    <text x="253" y="184" text-anchor="middle">构建 / lint 通过</text>
    <text x="406" y="170" text-anchor="middle">出口：核心逻辑覆盖率 ≥80%</text>
    <text x="406" y="184" text-anchor="middle">覆盖全部 AC 与边界</text>
    <text x="559" y="170" text-anchor="middle">出口：0 个严重问题</text>
    <text x="559" y="184" text-anchor="middle">Spec 轴 + 标准轴双轴</text>
    <text x="712" y="170" text-anchor="middle">出口：编译 → 静态分析 →</text>
    <text x="712" y="184" text-anchor="middle">架构约束 → 测试 → 安全扫描</text>
    <text x="835" y="170" text-anchor="middle">出口：冒烟 + 健康检查</text>
    <text x="835" y="184" text-anchor="middle">有回滚预案</text>
  </g>
  <text x="450" y="225" text-anchor="middle" fill="#c7d2fe" font-size="12">严格按序，不可跳步——每阶段消费上一阶段产出，通过出口门禁才能进入下一阶段</text>
  <text x="450" y="255" text-anchor="middle" fill="#64748b" font-size="11">Owner Agent（owner.md）编排全程 · 每阶段对应 .harness/skills/ 下一个技能</text>
  <text x="450" y="290" text-anchor="middle" fill="#94a3b8" font-size="11">另有 handoff / diagnosing-bugs / domain-modeling 等辅助闭环，随时可切入</text>
</svg>
```

> 核心收获：跨语言脚手架的第一版不需要支持很多语言，但必须支持"语言差异可参数化"。架构的种子在第一版就埋对了，后面只是浇水和长枝干。

## 四、对照检查与三轮对齐：grill-me → harness-me

> 🎯 **对应提示词 #4 + #5**：参考 skills.docx 检查可完善之处；命令行改名 `harness-me`、吸收 mattpocock 精华，且"不能删减当前实现的功能"

实现初版后，老板（用户）丢来一份 skills.docx："检查当前项目实现有没有可以完善的地方"。这一轮检查不是走过场，它产出了几条改变路线的结论：

- **命名对齐**：grill-me 这个名字过于"个人化"，我们的技能统一改为 `harness-me`/`harnessing` 体系，与 6 阶段流水线的语汇一致。
- **功能不删减**：整合 mattpocock/skills 精华的前提是"不能删减当前实现的功能"。这意味着吸收是"加法"，不是"替换"。
- **入口体验**：一键安装后要能在 AI 对话里直接 `/apply-harness`，所有后续技能自动进入流水线，而不是让用户手动翻文档找命令。

这一轮还顺手做了"同步引用"的清理：早期技能名 `request-analysis` 全部改为 `harnessing`（git 里程碑 `5e5a871`）。这类小事单独不值得写一章，但它代表了一个长期原则：**命名是架构的一部分，语汇不统一，Agent 就学不会**。

把"检查 → 三轮对齐 → 清理 → 重构"四件事放在一起看，会发现它们其实是在做同一件事——**语汇统一**：

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 900 380" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_c4" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_c4"><feDropShadow dx="0" dy="3" stdDeviation="4" flood-color="#000" flood-opacity="0.35"/></filter>
  </defs>
  <rect width="900" height="380" fill="url(#bg_c4)" rx="10"/>
  <text x="450" y="32" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">第四轮：对照检查 → 三轮对齐 → 1.1.0 参数块化</text>
  <g font-size="10.5" text-anchor="middle">
    <rect x="24" y="64" width="160" height="88" rx="8" fill="#1e293b" stroke="#475569"/>
    <text x="104" y="88" fill="#e2e8f0" font-weight="700">检查输入</text>
    <text x="104" y="110" fill="#94a3b8">skills.docx</text>
    <text x="104" y="126" fill="#94a3b8">mattpocock 精华</text>
    <text x="104" y="142" fill="#94a3b8">对照现有实现</text>
    <rect x="196" y="64" width="160" height="88" rx="8" fill="#1e293b" stroke="#0ea5e9"/>
    <text x="276" y="88" fill="#7dd3fc" font-weight="700">对齐① 命名</text>
    <text x="276" y="110" fill="#94a3b8">grill-me → harness-me</text>
    <text x="276" y="126" fill="#94a3b8">request-analysis → harnessing</text>
    <text x="276" y="142" fill="#64748b">语汇与流水线一致</text>
    <rect x="368" y="64" width="160" height="88" rx="8" fill="#1e293b" stroke="#14b8a6"/>
    <text x="448" y="88" fill="#5eead4" font-weight="700">对齐② 功能</text>
    <text x="448" y="110" fill="#94a3b8">吸收 mattpocock 精华</text>
    <text x="448" y="126" fill="#94a3b8">前提：不删减现有功能</text>
    <text x="448" y="142" fill="#64748b">吸收 = 加法，非替换</text>
    <rect x="540" y="64" width="160" height="88" rx="8" fill="#1e293b" stroke="#f59e0b"/>
    <text x="620" y="88" fill="#fde68a" font-weight="700">对齐③ 入口</text>
    <text x="620" y="110" fill="#94a3b8">一键安装后</text>
    <text x="620" y="126" fill="#94a3b8">/apply-harness 直达流水线</text>
    <text x="620" y="142" fill="#64748b">不靠手翻文档</text>
    <rect x="712" y="64" width="164" height="88" rx="8" fill="#1e293b" stroke="#8b5cf6"/>
    <text x="794" y="88" fill="#c4b5fd" font-weight="700">清理</text>
    <text x="794" y="110" fill="#94a3b8">同名 skills 同步引用</text>
    <text x="794" y="126" fill="#94a3b8">git 5e5a871</text>
    <text x="794" y="142" fill="#64748b">一处改名，全链生效</text>
  </g>
  <line x1="450" y1="156" x2="450" y2="180" stroke="#334155" stroke-width="2"/>
  <polygon points="450,186 444,178 456,178" fill="#64748b"/>
  <rect x="120" y="196" width="660" height="96" rx="10" fill="#0ea5e9" opacity="0.10" stroke="#0ea5e9" stroke-width="1.5" filter="url(#sh_c4)"/>
  <text x="450" y="222" text-anchor="middle" fill="#7dd3fc" font-size="14" font-weight="700">1.1.0 架构重构：参数表一张大表 → 按框架独立的参数块</text>
  <text x="450" y="246" text-anchor="middle" fill="#94a3b8" font-size="11">检测表新增 40+ 识别规则：Spring MVC / Quarkus / Micronaut / Vert.x / Django / Echo / Fiber / Chi / Next.js ...</text>
  <text x="450" y="268" text-anchor="middle" fill="#94a3b8" font-size="11">检测到具体框架 → 自动选择对应参数块 → coding-skill 角色描述按框架动态渲染</text>
  <text x="450" y="312" text-anchor="middle" fill="#c7d2fe" font-size="12">四项动作看似各异，本质同一件事：命名、语汇、能力、结构在向一个口径收敛</text>
  <text x="450" y="344" text-anchor="middle" fill="#64748b" font-size="11">为后续的"模板化"与"方案 A"埋下同一颗种子：语言与框架差异必须可参数化</text>
</svg>
```

紧接着，1.1.0 完成了这个项目第一次真正的"架构级"重构：**参数表从"一张大表"拆成"按框架独立的参数块"**。此前参数表是"整体一张表"——所有语言的参数混在一起；1.1.0 之后，每个框架组合（如 Java — Spring Boot + Maven、Python — Django、Go — Gin）拥有独立参数块，apply-harness 检测到具体框架后自动选择对应参数块。配套地：检测表新增 40+ 识别规则，覆盖 Spring MVC、Quarkus、Micronaut、Vert.x、Django、Echo、Fiber、Chi、React、Angular、Svelte、Next.js 等主流框架；coding-skill 的角色描述也实现了参数化，能按检测到的框架动态渲染。

这次重构的动机很朴素：框架一多，"一张表选参数"就变成了"一张表里找参数"。**参数块的粒度对齐到框架组合，检测的粒度才能对齐到框架组合**——检测与参数是一一映射的两端，这个对齐原则从此贯穿了项目的一生。

把这次升级画成流程图，就是"一张大表 → 框架独立参数块"的切换：左边是升级前的痛，右边是升级后的结构，底部是这次重构同步落地的三步：

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 900 440" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_c4b" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_c4b"><feDropShadow dx="0" dy="3" stdDeviation="4" flood-color="#000" flood-opacity="0.35"/></filter>
  </defs>
  <rect width="900" height="440" fill="url(#bg_c4b)" rx="10"/>
  <text x="450" y="32" text-anchor="middle" fill="#e2e8f0" font-size="26" font-weight="700">1.1.0 参数表升级：一张大表 → 框架独立参数块</text>
  <rect x="40" y="70" width="340" height="150" rx="10" fill="#1e293b" stroke="#ef4444" stroke-width="1.25" filter="url(#sh_c4b)"/>
  <text x="210" y="98" text-anchor="middle" fill="#fca5a5" font-size="15" font-weight="700">升级前 · 参数表 = 一张大表</text>
  <text x="210" y="124" text-anchor="middle" fill="#cbd5e1" font-size="11.5">所有语言、所有框架的参数混在一起</text>
  <text x="210" y="144" text-anchor="middle" fill="#cbd5e1" font-size="11.5">框架一多：「选参数」变成「找参数」</text>
  <text x="210" y="164" text-anchor="middle" fill="#cbd5e1" font-size="11.5">差异没有边界 · 增删改牵一发动全身</text>
  <text x="210" y="188" text-anchor="middle" fill="#64748b" font-size="10.5">维护成本随框架数量线性膨胀</text>
  <line x1="385" y1="145" x2="515" y2="145" stroke="#f59e0b" stroke-width="2"/>
  <polygon points="520,145 512,140 512,150" fill="#f59e0b"/>
  <text x="450" y="172" text-anchor="middle" fill="#fde68a" font-size="11">1.1.0 重构 · 参数拆分 + 40+ 检测规则</text>
  <rect x="520" y="70" width="340" height="150" rx="10" fill="#1e293b" stroke="#22c55e" stroke-width="1.25" filter="url(#sh_c4b)"/>
  <text x="690" y="98" text-anchor="middle" fill="#86efac" font-size="15" font-weight="700">升级后 · 按框架独立的参数块</text>
  <text x="690" y="124" text-anchor="middle" fill="#cbd5e1" font-size="11.5">每个框架组合一个块：Java-Spring Boot+Maven</text>
  <text x="690" y="144" text-anchor="middle" fill="#cbd5e1" font-size="11.5">Python-Django / Go-Gin / Frontend-Next.js ...</text>
  <text x="690" y="164" text-anchor="middle" fill="#cbd5e1" font-size="11.5">检测到哪个框架组合，就取哪块参数</text>
  <text x="690" y="188" text-anchor="middle" fill="#64748b" font-size="10.5">差异有了边界 → 增删改局部化</text>
  <line x1="450" y1="220" x2="450" y2="267" stroke="#f59e0b" stroke-width="2"/>
  <polygon points="450,270 444,263 456,263" fill="#f59e0b"/>
  <g>
    <rect x="60" y="270" width="240" height="68" rx="8" fill="#1e293b" stroke="#334155" filter="url(#sh_c4b)"/>
    <text x="180" y="292" text-anchor="middle" fill="#7dd3fc" font-size="14" font-weight="700">STEP 1 · 检测识别</text>
    <text x="180" y="316" text-anchor="middle" fill="#94a3b8" font-size="11">语言 + 框架组合判定</text>
    <text x="180" y="330" text-anchor="middle" fill="#94a3b8" font-size="11">检测表 40+ 识别规则命中</text>
  </g>
  <g>
    <rect x="330" y="270" width="240" height="68" rx="8" fill="#1e293b" stroke="#334155" filter="url(#sh_c4b)"/>
    <text x="450" y="292" text-anchor="middle" fill="#fde68a" font-size="14" font-weight="700">STEP 2 · 选择参数块</text>
    <text x="450" y="316" text-anchor="middle" fill="#94a3b8" font-size="11">自动匹配对应框架参数块</text>
    <text x="450" y="330" text-anchor="middle" fill="#94a3b8" font-size="11">检测与参数一一映射</text>
  </g>
  <g>
    <rect x="600" y="270" width="240" height="68" rx="8" fill="#1e293b" stroke="#334155" filter="url(#sh_c4b)"/>
    <text x="720" y="292" text-anchor="middle" fill="#86efac" font-size="14" font-weight="700">STEP 3 · 渲染落地</text>
    <text x="720" y="316" text-anchor="middle" fill="#94a3b8" font-size="11">coding-skill 角色描述</text>
    <text x="720" y="330" text-anchor="middle" fill="#94a3b8" font-size="11">按检测到的框架动态渲染</text>
  </g>
  <line x1="301" y1="304" x2="329" y2="304" stroke="#64748b" stroke-width="2"/>
  <polygon points="330,304 324,300 324,308" fill="#64748b"/>
  <line x1="571" y1="304" x2="599" y2="304" stroke="#64748b" stroke-width="2"/>
  <polygon points="600,304 594,300 594,308" fill="#64748b"/>
  <text x="450" y="376" text-anchor="middle" fill="#c7d2fe" font-size="12">检测与参数是一一映射的两端：检测粒度对齐框架组合，参数粒度才能对齐框架组合</text>
  <text x="450" y="400" text-anchor="middle" fill="#64748b" font-size="11">参数块 · 检测规则 · 渲染参数化三处同步落地 —— 为后续模板化与方案 A 埋下同一颗种子</text>
</svg>
```

> 核心收获："借鉴"比"照抄"难得多。照抄是把别人的文件搬进你的仓库；借鉴是把别人的思想翻译成你的架构语汇。翻译的前提是先有自己的语汇。

## 五、技能关联：把 change → review → verify 串成一条链

> 🎯 **对应提示词 #6 + #14**："新增的这几个技能能否跟之前的技能关联？比如一次开发涉及 change → review → verify"；"实际项目下 change 列表有多个时，怎么确定处理哪个 change"

第六轮讨论抛出了一个尖锐的问题："新增的这几个技能，能否跟之前的技能关联呢？比如原先的一次开发涉及 change → review → verify。"

这个问题戳中了第一批"技术栈"类脚手架的软肋：**每个技能都是孤岛**。`/coding-skill` 写完了代码，`/expert-reviewer` 不知道要审什么；`/unit-test-write` 写了测试，`/unit-test-ci` 不知道跑哪些用例。技能再多，串不起来就只是"一堆命令"。

Harness 的解法是引入**变更上下文（Changes）**：`templates/changes/_TEMPLATE/` 下是一组配套模板——`change.md`（变更声明）、`review.md`（评审记录）、`verify.md`（验证记录）。一次开发 = 一张 change 卡片，技能的输入输出都挂在这张卡片上：

- `/coding-skill` 读 change.md 的 AC（验收标准）列表，逐个实现
- `/expert-reviewer` 对照 change.md 做"Spec 匹配 + 规范合规"双轴评审
- `/unit-test-ci` 针对 change 波及范围跑全量门禁
- `/handoff` 切换上下文时，把 change.md 进度写成交接文档

这套"上下文对象"的设计，是脚手架从"命令集合"进化为"流水线系统"的分水岭。更妙的是，change 卡片是一台**状态机**驱动的：变更从"创建 → 分析 → 实现 → 评审 → 验证 → 完成"逐状态流转，每个技能只在特定状态下消费它。状态机带来的第一个好处是**防跳步**——AI 想从"实现"直接跳到"验证"？抱歉，状态没到。第二个好处是**全程留档**——`.harness/changes/<id>/` 目录下，change.md / review.md / verify.md 三件套记录了每一次流转，三个月后回看，一次开发从头到尾的决策路径一目了然。后来"并行开发"（第十三轮问题）的答案也在这里：每个 change 是一张独立的卡片、独立的状态机，多张卡片同时流转互不干扰，命令只需问"你要处理哪张卡"。后来并行开发的问题（"多个 change 怎么选"、"怎么确定处理哪一个"）之所以能解决，靠的也是这张卡片——命令不再问"做什么"，而是问"做哪张 change 卡"。

change 卡片不是一个空壳，`templates/changes/_TEMPLATE/change.md` 的骨架一眼就能看出它"为谁设计"：frontmatter 里是 `id: C-NNN`、`slug`、`status`（从 `analyzing` 起跳）、`created`；正文里是"用户故事"（作为&lt;角色&gt;，我想要&lt;功能&gt;，以便&lt;价值&gt;）、"非目标（Out of Scope）"——**明确写下不做什么**，这是对抗 AI 过度实现的第一个防线——然后是"验收标准 AC-1/2/3"和"边界情况"。这份骨架把"规格"从对话里捞出来、钉在文件上：任何技能读它，就能拿到一次开发的全部契约。

状态机的完整流转（记录在 6 阶段流水线文档里）是：`drafting → reviewing → approved → coding → testing → reviewing → ci → verifying → done`。注意 `reviewing` 出现了两次——第一次审**规格**（approved 才允许进入实现），第二次审**代码**（0 个严重问题才允许进 CI）。配合状态机的消费规则：`/coding-skill` 只认 `status: coding` 的 change——恰好 1 个自动选中，0 个报错退回 ① harnessing，≥2 个**列出候选清单、停下请用户选择**。这条规则被写成技能里的硬约束，AI 不得擅自默认取第一个：

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 900 340" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_cs" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
  </defs>
  <rect width="900" height="340" fill="url(#bg_cs)" rx="10"/>
  <text x="450" y="34" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">change 状态机：一次开发的一张卡片</text>
  <g font-size="11.5" text-anchor="middle">
    <rect x="30" y="80" width="92" height="44" rx="7" fill="#475569"/><text x="76" y="106" fill="#e2e8f0">drafting</text>
    <rect x="140" y="80" width="92" height="44" rx="7" fill="#7c3aed"/><text x="186" y="106" fill="#ede9fe">reviewing</text>
    <rect x="250" y="80" width="92" height="44" rx="7" fill="#0e7490"/><text x="296" y="106" fill="#cffafe">approved</text>
    <rect x="360" y="80" width="92" height="44" rx="7" fill="#14b8a6"/><text x="406" y="106" fill="#ccfbf1">coding</text>
    <rect x="470" y="80" width="92" height="44" rx="7" fill="#8b5cf6"/><text x="516" y="106" fill="#ede9fe">testing</text>
    <rect x="580" y="80" width="92" height="44" rx="7" fill="#7c3aed"/><text x="626" y="106" fill="#ede9fe">reviewing</text>
    <rect x="690" y="80" width="92" height="44" rx="7" fill="#ef4444"/><text x="736" y="106" fill="#fee2e2">ci</text>
    <rect x="795" y="80" width="86" height="44" rx="7" fill="#3b82f6"/><text x="838" y="106" fill="#dbeafe">verifying→done</text>
  </g>
  <g stroke="#64748b" stroke-width="1.5">
    <line x1="122" y1="102" x2="140" y2="102"/>
    <line x1="232" y1="102" x2="250" y2="102"/>
    <line x1="342" y1="102" x2="360" y2="102"/>
    <line x1="452" y1="102" x2="470" y2="102"/>
    <line x1="562" y1="102" x2="580" y2="102"/>
    <line x1="672" y1="102" x2="690" y2="102"/>
    <line x1="782" y1="102" x2="795" y2="102"/>
  </g>
  <g font-size="10.5" fill="#94a3b8">
    <text x="76" y="165" text-anchor="middle">harnessing 产出规格</text>
    <text x="186" y="165" text-anchor="middle">第一次评审：审规格</text>
    <text x="186" y="180" text-anchor="middle">通过才 approved</text>
    <text x="296" y="165" text-anchor="middle">coding-skill 消费</text>
    <text x="296" y="180" text-anchor="middle">读 AC 逐条实现</text>
    <text x="406" y="165" text-anchor="middle">unit-test-write</text>
    <text x="406" y="180" text-anchor="middle">覆盖率 ≥80%</text>
    <text x="516" y="165" text-anchor="middle">expert-reviewer</text>
    <text x="516" y="180" text-anchor="middle">双轴评审 0 严重</text>
    <text x="626" y="165" text-anchor="middle">第二次评审：审代码</text>
    <text x="736" y="165" text-anchor="middle">unit-test-ci 门禁全绿</text>
    <text x="838" y="165" text-anchor="middle">deploy-verify</text>
    <text x="838" y="180" text-anchor="middle">冒烟+健康检查</text>
  </g>
  <text x="450" y="225" text-anchor="middle" fill="#c7d2fe" font-size="12">同级技能分工：coding-skill / expert-reviewer / unit-test-ci 都挂在同一张 change 卡片上</text>
  <text x="450" y="255" text-anchor="middle" fill="#64748b" font-size="11">状态不到，消费技能不启动（防跳步）；每个 change 独立状态机，并行开发互不干扰</text>
  <text x="450" y="290" text-anchor="middle" fill="#94a3b8" font-size="11">多 change 时：列出候选清单（id + 标题 + 摘要）请用户选择，AI 不得擅自取第一个</text>
</svg>
```

评审与验证的产物同样模板化：`review.md` 输出"Spec 轴报告（需求匹配）"+ "标准轴报告（规范合规）"双轴报告，问题分 🔴 严重 / 🟡 建议 / 🟢 通过三级，🔴 必须修复才放行；`verify.md` 则输出环境（profile / 镜像）、部署拓扑（Mermaid 图）、健康检查端点、冒烟测试清单、回滚预案。三件套合起来，一次开发的"契约 → 实现 → 评审 → 验证"全链路都有据可查——这也是"机器验证质量"这句话最落地的形态：质量不是评审时的口头结论，而是 change 卡片与三份报告里的结构化记录。

> 核心收获：技能不是越多越好，是"上下文共享"越多越好。一个共享的变更对象，比十次口头交接都管用。

## 六、两轮抽离：rules 进 core，skills 进模板

> 🎯 **对应提示词 #7 + #10 + #11**：rules 抽离公共部分到 harness-core；每个语言下的 skills 是否需抽离为公共；"核心公共逻辑抽离到模板"

接下来是这个项目反复出现的主题词：**抽离**。它至少发生了四次，我挑最关键的两轮讲。

**第一轮：rules 的公共抽离。** 有人问："harness-xxx/rules 的内容是否有重复，可以抽离公共部分到 harness-core？"答案是肯定的。对比六种语言的 rules，发现：SDD-TDD 模式、变更定位、开发流程规范、运行时可靠性这四条是**跨语言不变式**——Java 和 Go 的 SDD-TDD 循环完全一样，只是测试命令不同；而工程结构、编码规范是**语言绑定的**——Java 的分层、Go 的包组织没法通用。于是规则集被拆成"4 条通用 + 2 条语言特有"的 6 条结构，通用的进 `harness-core/rules/`，语言特有的留在各语言包 `rules/`。

**第二轮：技能的模板抽离。** 问题更进一步："每个语言下的 skills 是否还需要抽离为公共的 skills？"这次抽到了最核心的地方。对照六种语言的技能文件，差异分布非常有规律：

- **大体相同的部分**（流程、步骤、检查清单、门禁逻辑）——占 80%
- **语言绑定的部分**（构建命令、测试框架、lint 工具、代码样例）——占 20%

于是决定：把 10 个流水线/辅助技能全部改造成 harness-core 里的**参数化模板**，语言特有部分一律用 `{{占位符}}` 表达；语言包里的技能文件删除。语言差异只存在于两处：**参数表**（命令与工具的选择）和 **rules**（工程结构与编码规范）。模板占位符从最初的几个，最终收敛为 29 个核心占位符，由参数表 51 个参数键取值覆盖。

以 Java 为例，`### Java — Spring Boot + Maven（默认）` 参数块把 30+ 个键一次定义到位：`{{LANGUAGE}}=Java`、`{{TEST_FRAMEWORK}}=JUnit 5 + Mockito + AssertJ`、`{{COV_TOOL}}=JaCoCo（核心逻辑 ≥80%）`、`{{LINT_TOOL}}=Checkstyle + PMD + SpotBugs`、`{{ARCH_TEST_TOOL}}=ArchUnit`、`{{LANG_TAG}}=-java`、`{{HARNESS_ME_NAME}}=/harness-me`、`{{BUILD_CMD}}=mvn compile`。模板 frontmatter 里的 `name: coding-skill{{LANG_TAG}}` 渲染后就是 `coding-skill-java`——同一份定义在六种语言下渲染出 `coding-skill-java / -python / -golang / -rust / -php / -front` 六个技能。更有意思的是，参数值本身也可以是占位符：`{{TYPE_CHECK_CMD}}` 的值就指向 `{{BUILD_CMD}}`（"类型检查即编译检查"），渲染时嵌套替换。**模板负责"长什么样"，参数表负责"是什么"**——这是整个参数化得以成立的二分法：

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 900 360" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_pr" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
  </defs>
  <rect width="900" height="360" fill="url(#bg_pr)" rx="10"/>
  <text x="450" y="34" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">参数化渲染：模板 × 参数表 → 渲染器 → 语言包技能</text>
  <g font-size="11" text-anchor="middle">
    <rect x="30" y="70" width="190" height="120" rx="8" fill="#1e293b" stroke="#334155"/>
    <text x="125" y="94" fill="#e2e8f0" font-weight="700">模板（harness-core）</text>
    <text x="125" y="118" fill="#94a3b8">10 个技能 × 29 占位符</text>
    <text x="125" y="140" fill="#7dd3fc">name: coding-skill{{LANG_TAG}}</text>
    <text x="125" y="158" fill="#7dd3fc">{{TEST_FRAMEWORK}} 测试</text>
    <text x="125" y="176" fill="#7dd3fc">{{BUILD_CMD}} 通过</text>
    <rect x="340" y="70" width="230" height="120" rx="8" fill="#1e293b" stroke="#334155"/>
    <text x="455" y="94" fill="#e2e8f0" font-weight="700">参数表（apply-harness）</text>
    <text x="455" y="118" fill="#94a3b8">6 基础块 + 111 差异块</text>
    <text x="455" y="140" fill="#fbbf24">{{LANG_TAG}} → -java</text>
    <text x="455" y="158" fill="#fbbf24">{{TEST_FRAMEWORK}} → JUnit5+Mockito</text>
    <text x="455" y="176" fill="#fbbf24">{{BUILD_CMD}} → mvn compile</text>
    <rect x="680" y="70" width="190" height="120" rx="8" fill="#1e293b" stroke="#334155"/>
    <text x="775" y="94" fill="#e2e8f0" font-weight="700">产物（.harness/skills/）</text>
    <text x="775" y="118" fill="#94a3b8">6 语言 × 10 技能渲染版</text>
    <text x="775" y="140" fill="#86efac">coding-skill-java</text>
    <text x="775" y="158" fill="#86efac">coding-skill-golang</text>
    <text x="775" y="176" fill="#86efac">coding-skill-rust ...</text>
    <text x="280" y="150" fill="#94a3b8" font-size="56">×</text>
    <line x1="570" y1="130" x2="672" y2="130" stroke="#94a3b8" stroke-width="2"/>
    <polygon points="680,130 672,126 672,134" fill="#94a3b8"/>
  </g>
  <g font-size="10.5" fill="#94a3b8">
    <text x="125" y="222" text-anchor="middle">模板里不出现任何语言字样</text>
    <text x="125" y="236" text-anchor="middle">差异全部走占位符</text>
    <text x="455" y="222" text-anchor="middle">按 Step 1 检测出的三元组</text>
    <text x="455" y="236" text-anchor="middle">（语言×框架×构建工具）选块</text>
    <text x="775" y="222" text-anchor="middle">渲染后写入各语言包</text>
    <text x="775" y="236" text-anchor="middle">注册到当前 AI 工具</text>
  </g>
  <text x="450" y="280" text-anchor="middle" fill="#c7d2fe" font-size="12">嵌套替换：{{TYPE_CHECK_CMD}} 的值指向 {{BUILD_CMD}}，渲染器递归展开直到无占位符</text>
  <text x="450" y="310" text-anchor="middle" fill="#64748b" font-size="11">占位符共 29 个 · 参数键共 51 个 · 每轮用脚本核对"全覆盖、零缺失"</text>
  <text x="450" y="338" text-anchor="middle" fill="#94a3b8" font-size="11">改模板一处 → 六语言同步生效；改参数一处 → 一个语言包生效</text>
</svg>
```

一个具体的"漂移"例子可以说明为什么必须删：同一份 `coding-skill`，早期语言包版本里写着"error 是否被正确处理，不吞不忽略""对照工程结构.md 确定服务、包、文件归属"，而 core 模板版本里写的是"相关 rules 约束""超时+重试+限频+降级""common/ 已有公共库"——两边各有一些对方没有的句子。这种差异不是有意设计的，是**两边被不同的人在不同时间修改过**的痕迹。语言包从"渲染产物"悄悄变成了"半渲染产物"：既不是模板的忠实拷贝，也不是独立的事实源，夹在中间，谁改都心里发慌。删掉语言包副本、把所有句子收敛进 core 模板（语言特有针对性的句子下沉到语言包 rules），才让"谁持有这句话"重新变得唯一。

除了 10 个模板化技能，harness-core 还沉淀了 **16 个"直接复制"技能**——它们不随语言变化，安装时原样拷贝给任何项目：

- **3 个通用技能**：`domain-modeling`（领域模型维护）、`research`（高信任来源调研）、`resolving-merge-conflicts`（合并冲突解决）
- **13 个封装组件**：`redis-cache-wrapper`（多级缓存）、`database-migration-toolkit`（迁移/回滚）、`kafka-toolkit`（消费者/DLQ）、`rocketmq-toolkit`（事务消息）、`http-client-toolkit`（连接池/重试/熔断）、`logging-toolkit`（traceId/脱敏）、`scheduler-toolkit`（分布式调度）、`oss-toolkit`（对象存储）、`excel-toolkit`（模板导出）、`eventbus-toolkit`（事件总线）、`performance-toolkit`（性能诊断）、`security-toolkit`（安全封装）、`k8s-release-toolkit`（K8s 发布）

这 13 个组件揭示了一个更深的抽离维度：**除了"语言差异"，还有"场景差异"**。前者决定了要不要参数化（要），后者决定了要不要保留多份（不要——中间件封装是跨语言通用的，直接复制即可）。两轮抽离合在一起，harness-core 的 26 个技能目录终于有了清晰的分类学：10 模板（参数化渲染）+ 3 通用（直拷）+ 13 组件（直拷）。

把这一分类学画成图，就是两轮抽离后的技能全景：三栏总览上方是"语言差异 → 参数化"与"场景差异 → 直拷"的分叉，下方展开 3 个通用技能的详情与 13 个封装组件的功能分组：

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 900 480" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
    <defs>
        <linearGradient id="bg_sd" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
        <filter id="sh_sd"><feDropShadow dx="0" dy="3" stdDeviation="4" flood-color="#000" flood-opacity="0.35"/></filter>
    </defs>
    <rect width="900" height="480" fill="url(#bg_sd)" rx="10"/>
    <text x="450" y="32" text-anchor="middle" fill="#e2e8f0" font-size="26" font-weight="700">两轮抽离后 · harness-core 技能分类</text>

    <!-- 三栏总览 -->
    <g>
        <rect x="40" y="70" width="250" height="100" rx="10" fill="#1e293b" stroke="#0ea5e9" stroke-width="1.25" filter="url(#sh_sd)"/>
        <text x="165" y="100" text-anchor="middle" fill="#7dd3fc" font-size="16" font-weight="700">10 参数化模板</text>
        <text x="165" y="122" text-anchor="middle" fill="#94a3b8" font-size="11">语言差异 → 参数表渲染</text>
        <text x="165" y="145" text-anchor="middle" fill="#64748b" font-size="10.5">6 流水线 + 4 辅助技能</text>

        <rect x="325" y="70" width="250" height="100" rx="10" fill="#1e293b" stroke="#8b5cf6" stroke-width="1.25" filter="url(#sh_sd)"/>
        <text x="450" y="100" text-anchor="middle" fill="#c4b5fd" font-size="16" font-weight="700">3 通用技能</text>
        <text x="450" y="122" text-anchor="middle" fill="#94a3b8" font-size="11">场景通用 → 直接复制</text>
        <text x="450" y="145" text-anchor="middle" fill="#64748b" font-size="10.5">来自 mattpocock 精华吸收</text>

        <rect x="610" y="70" width="250" height="100" rx="10" fill="#1e293b" stroke="#22c55e" stroke-width="1.25" filter="url(#sh_sd)"/>
        <text x="735" y="100" text-anchor="middle" fill="#86efac" font-size="16" font-weight="700">13 封装组件</text>
        <text x="735" y="122" text-anchor="middle" fill="#94a3b8" font-size="11">场景差异 → 直接复制</text>
        <text x="735" y="145" text-anchor="middle" fill="#64748b" font-size="10.5">中间件/运维/开发辅助</text>
    </g>

    <!-- 箭头 -->
    <line x1="291" y1="120" x2="324" y2="120" stroke="#64748b" stroke-width="2"/>
    <polygon points="325,120 319,116 319,124" fill="#64748b"/>
    <text x="308" y="138" text-anchor="middle" fill="#64748b" font-size="10.5">语言</text>
    <text x="308" y="148" text-anchor="middle" fill="#64748b" font-size="10.5">差异</text>

    <line x1="576" y1="120" x2="609" y2="120" stroke="#64748b" stroke-width="2"/>
    <polygon points="610,120 604,116 604,124" fill="#64748b"/>
    <text x="593" y="138" text-anchor="middle" fill="#64748b" font-size="10.5">场景</text>
    <text x="593" y="148" text-anchor="middle" fill="#64748b" font-size="10.5">差异</text>

    <!-- 分隔线 -->
    <line x1="40" y1="190" x2="860" y2="190" stroke="#334155" stroke-width="1" stroke-dasharray="4,4"/>

    <!-- 3 通用技能详情 -->
    <g>
        <rect x="40" y="210" width="280" height="50" rx="7" fill="#1e293b" stroke="#334155" filter="url(#sh_sd)"/>
        <text x="180" y="232" text-anchor="middle" fill="#c4b5fd" font-size="13" font-weight="700">domain-modeling</text>
        <text x="180" y="252" text-anchor="middle" fill="#94a3b8" font-size="10.5">领域模型维护 · 与 owner.md Wiki 模板打通</text>

        <rect x="40" y="268" width="280" height="50" rx="7" fill="#1e293b" stroke="#334155" filter="url(#sh_sd)"/>
        <text x="180" y="290" text-anchor="middle" fill="#c4b5fd" font-size="13" font-weight="700">research</text>
        <text x="180" y="310" text-anchor="middle" fill="#94a3b8" font-size="10.5">高信任一手来源调研 · 需求分析的事实底座</text>

        <rect x="40" y="326" width="280" height="50" rx="7" fill="#1e293b" stroke="#334155" filter="url(#sh_sd)"/>
        <text x="180" y="348" text-anchor="middle" fill="#c4b5fd" font-size="13" font-weight="700">resolving-merge-conflicts</text>
        <text x="180" y="368" text-anchor="middle" fill="#94a3b8" font-size="10.5">合并冲突解决 · 接入 change → review → verify 链</text>
    </g>

    <!-- 13 封装组件 · 按功能分组 -->
    <g>
        <rect x="360" y="210" width="240" height="90" rx="8" fill="#1e293b" stroke="#334155" filter="url(#sh_sd)"/>
        <text x="480" y="234" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">数据与存储</text>
        <text x="480" y="256" text-anchor="middle" fill="#cbd5e1" font-size="10">· redis-cache-wrapper（多级缓存）</text>
        <text x="480" y="272" text-anchor="middle" fill="#cbd5e1" font-size="10">· database-migration-toolkit（迁移/回滚）</text>
        <text x="480" y="288" text-anchor="middle" fill="#cbd5e1" font-size="10">· oss-toolkit（对象存储）</text>

        <rect x="620" y="210" width="240" height="90" rx="8" fill="#1e293b" stroke="#334155" filter="url(#sh_sd)"/>
        <text x="740" y="234" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">消息与通信</text>
        <text x="740" y="256" text-anchor="middle" fill="#cbd5e1" font-size="10">· kafka-toolkit（消费者/DLQ）</text>
        <text x="740" y="272" text-anchor="middle" fill="#cbd5e1" font-size="10">· rocketmq-toolkit（事务消息）</text>
        <text x="740" y="288" text-anchor="middle" fill="#cbd5e1" font-size="10">· eventbus-toolkit（事件总线）</text>

        <rect x="360" y="316" width="240" height="90" rx="8" fill="#1e293b" stroke="#334155" filter="url(#sh_sd)"/>
        <text x="480" y="340" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">运维与基础</text>
        <text x="480" y="362" text-anchor="middle" fill="#cbd5e1" font-size="10">· scheduler-toolkit（分布式调度）</text>
        <text x="480" y="378" text-anchor="middle" fill="#cbd5e1" font-size="10">· k8s-release-toolkit（K8s 发布）</text>
        <text x="480" y="394" text-anchor="middle" fill="#cbd5e1" font-size="10">· logging-toolkit（traceId/脱敏）</text>

        <rect x="620" y="316" width="240" height="90" rx="8" fill="#1e293b" stroke="#334155" filter="url(#sh_sd)"/>
        <text x="740" y="340" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">开发辅助</text>
        <text x="740" y="362" text-anchor="middle" fill="#cbd5e1" font-size="10">· http-client-toolkit（连接池/重试/熔断）</text>
        <text x="740" y="378" text-anchor="middle" fill="#cbd5e1" font-size="10">· excel-toolkit（模板导出）</text>
        <text x="740" y="394" text-anchor="middle" fill="#cbd5e1" font-size="10">· performance-toolkit（性能诊断）</text>
    </g>

    <text x="450" y="440" text-anchor="middle" fill="#c7d2fe" font-size="12">10 模板承载语言差异（参数化渲染） · 3 通用 + 13 组件承载场景差异（直接复制）</text>
    <text x="450" y="462" text-anchor="middle" fill="#64748b" font-size="11">harness-core 26 个技能目录 · 两轮抽离确定的分类</text>
</svg>
```

> 核心收获：抽离的判断标准只有一个——**这段内容是否随"语言"变化**。会变的进参数表，不会变的进模板。抽离不是目标，可维护性才是，但抽离是达成它的唯一路径。

## 七、吸收 mattpocock 的精华：五个技能的"增值"

> 🎯 **对应提示词 #8 + #9**：调研 `mattpocock/skills/tree/v1.2.3/skills/engineering` 还有哪些可吸收；"按建议的先后顺序实现精华 skills，目标就是能和之前的 skills 进行整合关联"

第八轮讨论来了个"抄作业"需求：`mattpocock/skills/tree/v1.2.3/skills/engineering` 里还有哪些可以吸收？git 提交 `9ef39fc` 记录了答案——**五个技能**被吸收并完成与现有流水线的整合：

- `domain-modeling` — 领域模型维护（Ubiquitous Language 字典），与 owner.md 的 Wiki 模板打通
- `research` — 高信任一手来源调研，结果落进仓库，作为需求分析的"事实底座"
- `resolving-merge-conflicts` — 合并冲突解决，接入 change → review → verify 链的收尾
- `diagnosing-bugs` — 严谨的 Bug 诊断流程（稳定复现 → 根因分析），对接 6 阶段流水线的任意环节
- `handoff` — 上下文交接，配合 change.md 实现"换对话不丢进度"

这一轮吸收的关键词是"**整合关联**"（原文要求："目标就是能和之前的 skills 进行整合关联"）。五个技能不是平铺在仓库里，而是各自找到了与现有流水线的钩子：diagnosing-bugs 挂在"遇到 Bug 时"，handoff 挂在"切换上下文时"，domain-modeling 挂在"沉淀领域知识时"。脚手架的价值主张也从"6 阶段流水线"扩展为"流水线 + 辅助闭环"。

六个技能在流水线上的"挂接位置"一目了然——有的挂全程，有的挂定点：

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 900 380" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_c7" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_c7"><feDropShadow dx="0" dy="3" stdDeviation="4" flood-color="#000" flood-opacity="0.35"/></filter>
  </defs>
  <rect width="900" height="380" fill="url(#bg_c7)" rx="10"/>
  <text x="450" y="32" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">吸收后的挂接：五个辅助技能接入六阶段流水线</text>
  <g font-size="10.5" text-anchor="middle">
    <rect x="30" y="58" width="150" height="62" rx="8" fill="#1e293b" stroke="#22c55e"/>
    <text x="105" y="81" fill="#86efac" font-weight="700">/research</text>
    <text x="105" y="99" fill="#94a3b8">挂 ① 之前</text>
    <text x="105" y="113" fill="#94a3b8">需求的事实底座</text>
    <rect x="330" y="58" width="240" height="62" rx="8" fill="#1e293b" stroke="#f59e0b"/>
    <text x="450" y="81" fill="#fde68a" font-weight="700">/handoff</text>
    <text x="450" y="99" fill="#94a3b8">挂全程（任何阶段）</text>
    <text x="450" y="113" fill="#94a3b8">换对话 / 交接进度</text>
    <rect x="680" y="58" width="150" height="62" rx="8" fill="#1e293b" stroke="#a78bfa"/>
    <text x="755" y="81" fill="#c4b5fd" font-weight="700">/domain-modeling</text>
    <text x="755" y="99" fill="#94a3b8">挂 ① 培养阶段</text>
    <text x="755" y="113" fill="#94a3b8">沉淀领域术语</text>
  </g>
  <line x1="105" y1="120" x2="140" y2="146" stroke="#22c55e" stroke-width="1.8"/>
  <polygon points="146,152 134,148 144,140" fill="#22c55e"/>
  <line x1="755" y1="120" x2="640" y2="146" stroke="#a78bfa" stroke-width="1.8"/>
  <polygon points="634,152 646,148 636,140" fill="#a78bfa"/>
  <g font-size="11" text-anchor="middle">
    <rect x="110" y="150" width="113" height="52" rx="8" fill="#0ea5e9" opacity="0.85"/><text x="166" y="180" fill="#fff" font-weight="700">① 规格</text>
    <rect x="223" y="150" width="113" height="52" rx="8" fill="#14b8a6" opacity="0.85"/><text x="279" y="180" fill="#fff" font-weight="700">② 编码</text>
    <rect x="336" y="150" width="113" height="52" rx="8" fill="#8b5cf6" opacity="0.85"/><text x="392" y="180" fill="#fff" font-weight="700">③ 测试</text>
    <rect x="449" y="150" width="113" height="52" rx="8" fill="#f59e0b" opacity="0.85"/><text x="505" y="180" fill="#fff" font-weight="700">④ 评审</text>
    <rect x="562" y="150" width="113" height="52" rx="8" fill="#ef4444" opacity="0.85"/><text x="618" y="180" fill="#fff" font-weight="700">⑤ CI</text>
    <rect x="675" y="150" width="113" height="52" rx="8" fill="#3b82f6" opacity="0.85"/><text x="731" y="180" fill="#fff" font-weight="700">⑥ 部署</text>
  </g>
  <g font-size="10.5" text-anchor="middle">
    <rect x="250" y="262" width="200" height="62" rx="8" fill="#1e293b" stroke="#ef4444"/>
    <text x="350" y="285" fill="#fca5a5" font-weight="700">/diagnosing-bugs</text>
    <text x="350" y="303" fill="#94a3b8">挂 ③-⑥ 任意环节</text>
    <text x="350" y="317" fill="#94a3b8">遇 Bug 时切入</text>
    <rect x="620" y="262" width="220" height="62" rx="8" fill="#1e293b" stroke="#38bdf8"/>
    <text x="730" y="285" fill="#7dd3fc" font-weight="700">/resolving-merge-conflicts</text>
    <text x="730" y="303" fill="#94a3b8">挂 ⑥ 之后</text>
    <text x="730" y="317" fill="#94a3b8">合并 / rebase 冲突时</text>
  </g>
  <line x1="350" y1="262" x2="420" y2="204" stroke="#ef4444" stroke-width="1.8"/>
  <polygon points="424,198 412,202 422,210" fill="#ef4444"/>
  <line x1="730" y1="262" x2="730" y2="204" stroke="#38bdf8" stroke-width="1.8"/>
  <polygon points="730,198 724,206 736,206" fill="#38bdf8"/>
  <text x="450" y="352" text-anchor="middle" fill="#94a3b8" font-size="11">痛点先导的接入顺序：handoff（上下文贵）→ diagnosing-bugs（事故）→ domain-modeling（术语漂移）→ research / merge-conflicts（信息与合并瓶颈）</text>
</svg>
```

被吸收的技能也立刻进入同一套分类学：`handoff` 与 `diagnosing-bugs` 是模板化技能（含 `{{LANG_TAG}}`、`{{DEBUG_TOOL}}` 等占位符，随语言渲染）；`domain-modeling`、`research`、`resolving-merge-conflicts` 是跨语言通用技能（直接复制，任何语言项目原样拿到）。这保证了"新鲜血液"不会成为第二套事实源——**吸收不是把文件搬进仓库，而是把它们接进已有的生命周期管理**：模板、参数表、检测规则的任何一次演进，都会自动覆盖到这些新成员。

吸收的时机也很有讲究：这五个技能不是第一轮就全部进来的，而是"流水线先立住、痛点出现后再补"。order 大致是：先有 handoff（上下文太贵）、再有 diagnosing-bugs（生产事故需要标准化流程）、然后 domain-modeling（术语漂移开始伤人）、最后 research 与 resolving-merge-conflicts（信息与合并成为瓶颈）。每个技能被吸收时，仓库里都已经存在一个"它应该出现的位置"——这就是前面说的"整合关联"的底层逻辑：**不是引进技能，是给已有的流程补上缺的环节**。

> 核心收获：吸收外部技能时，先画"它应该挂在现有流程的哪个位置"，再动手。没有位置的技能，吸收进来只会增加噪音。

## 八、工具无关与一键安装：从"我的安装方式"到"你的安装方式"

> 🎯 **对应提示词 #12 + #13**："不限工具，要支持主流的 codex、claude-code、reasonix 等等"；"实现 install_skill 能力，我只需在目标项目执行 /install_skill 即可"；"如果是并行开发的话，只输入命令就不行了吧"

第十二轮的需求把格局拉大了：

> "1、不限工具，要支持主流的比如 codex、claude-code、reasonix 等等。2、我需要你去实现 install_skill 这个能力，而不是让我选择目标项目。我只需在目标项目执行 /install_skill 即可。"

这句话包含两个独立的设计决策，每一个都值得单独讲。

**决策一：AI 工具无关。** 很多脚手架是为某个特定 AI 工具写的（比如只为 Claude Code 写 CLAUDE 规则文件）。Harness 的 SKILL.md 体系天然是"工具无关"的——它靠的是对话中的斜杠命令和结构化文本，而不是某个工具的私有配置。项目专门实现"AI 工具识别"能力（git 里程碑 `556cd25`），在安装时探测当前正在用哪个工具，相应地调整技能注册与命令风格。19+ 种 AI 工具适配器由此而来。

**决策二：安装动作前置。** 原方案是"先选目标项目，再由我安装"；改进后是"在目标项目根目录执行 `/install_skill` 即可"。用户不需要理解仓库结构、不需要克隆、不需要配置——一个命令，全部搞定。这才是"开箱即用"的完整含义：**不仅使用要开箱，安装也要开箱**。

配套的还有一个工程化细节：**多 change 选择**（第十四轮）——`/coding-skill-xxx` 遇到多个变更时，先列出 change.md 清单让用户选择，而不是盲目处理。这个问题的答案，是前面第五章的"变更上下文"机制的延伸。

"工具无关"不是一句口号，它落进了 apply-harness 的 Step 5.5——**注册技能到当前 AI 工具**。这一步要回答的问题是：渲染出的技能文件，怎么让当前这个 AI 工具"认识"它们？不同工具的技能注册方式不同（有的读目录、有的认 frontmatter、有的需要导入命令），Step 5.5 的意义在于把"注册"这件事也参数化了：探测当前工具的能力模型（是否支持斜杠命令、是否支持技能导入、推理能力强弱），再选择对应的注册方式。仓库里 19+ 种 AI 工具适配器由此而来。

`install-skill` 的实现把"注册"拆成三步。**Step 1 检测当前 AI 工具**：按固定优先级探测特征目录或环境变量——`REASONIX`/`.reasonix/` → Reasonix，`CLAUDE_CODE`/`.claude/` → Claude Code，`OPENAI_API_KEY`/`.codex/` → Codex，`.cursor/` → Cursor，`.vscode/agent-skills/` → VS Code Agent Skills，`.github/` → Copilot，`.windsurf/` → Windsurf，`.opencode/` → OpenCode，`.tabnine/` → Tabnine……选择第一个命中的工具；探测表优先 ✅ 原生支持 SKILL.md 的工具，其次 ⚠️ 需要映射或间接兼容的工具，全部不命中则询问用户，或回退到 `npx skills` CLI 安装。**Step 2 定位技能来源**：扫描 `.harness/skills/` 下所有含 `SKILL.md` 的目录。**Step 3 写入目标目录**：Reasonix 认 `.reasonix/skills/`、Claude Code 认 `.claude/skills/`、VS Code 认 `.vscode/agent-skills/`、OpenCode 每命令一个文件落在 `.opencode/commands/`、Tabnine 用 `guidelines.md`……同一份 `.harness/`，在 19+ 种工具里各得其所：

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 900 340" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_is" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
  </defs>
  <rect width="900" height="340" fill="url(#bg_is)" rx="10"/>
  <text x="450" y="34" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">install-skill：一份 .harness，注册到 19+ 工具</text>
  <g font-size="11" text-anchor="middle">
    <rect x="30" y="70" width="150" height="120" rx="8" fill="#1e293b" stroke="#334155"/>
    <text x="105" y="94" fill="#e2e8f0" font-weight="700">目标项目</text>
    <text x="105" y="118" fill="#94a3b8">.harness/skills/</text>
    <text x="105" y="136" fill="#94a3b8">30 技能 × SKILL.md</text>
    <text x="105" y="154" fill="#7dd3fc">执行 /install-skill</text>
    <rect x="330" y="70" width="150" height="120" rx="8" fill="#0f172a" stroke="#7c3aed"/>
    <text x="405" y="94" fill="#e2e8f0" font-weight="700">install-skill</text>
    <text x="405" y="118" fill="#c4b5fd">Step 1 检测当前工具</text>
    <text x="405" y="136" fill="#c4b5fd">Step 2 定位技能来源</text>
    <text x="405" y="154" fill="#c4b5fd">Step 3 写入目标目录</text>
    <rect x="630" y="70" width="240" height="120" rx="8" fill="#1e293b" stroke="#334155"/>
    <text x="750" y="94" fill="#e2e8f0" font-weight="700">工具识别的技能目录</text>
    <text x="750" y="116" fill="#94a3b8" text-anchor="middle">Reasonix → .reasonix/skills/</text>
    <text x="750" y="132" fill="#94a3b8" text-anchor="middle">Claude Code → .claude/skills/</text>
    <text x="750" y="148" fill="#94a3b8" text-anchor="middle">Codex → .codex/skills/</text>
    <text x="750" y="164" fill="#94a3b8" text-anchor="middle">VS Code → .vscode/agent-skills/</text>
    <text x="750" y="180" fill="#94a3b8" text-anchor="middle">OpenCode → .opencode/commands/</text>
    <line x1="180" y1="130" x2="322" y2="130" stroke="#94a3b8" stroke-width="2"/>
    <polygon points="330,130 322,126 322,134" fill="#94a3b8"/>
    <line x1="480" y1="130" x2="622" y2="130" stroke="#94a3b8" stroke-width="2"/>
    <polygon points="630,130 622,126 622,134" fill="#94a3b8"/>
  </g>
  <g font-size="10.5" fill="#94a3b8">
    <text x="105" y="212" text-anchor="middle">apply-harness 负责复制</text>
    <text x="105" y="226" text-anchor="middle">（生成技能文件）</text>
    <text x="405" y="212" text-anchor="middle">优先原生 SKILL.md ✅</text>
    <text x="405" y="226" text-anchor="middle">间接兼容 ⚠️ 次之</text>
    <text x="405" y="240" text-anchor="middle">都不中 → 询问 / npx skills</text>
    <text x="750" y="212" text-anchor="middle">注册后 /harnessing、/harness-me</text>
    <text x="750" y="226" text-anchor="middle">等斜杠命令立即生效</text>
  </g>
  <text x="450" y="278" text-anchor="middle" fill="#c7d2fe" font-size="12">复制 ≠ 注册：apply-harness 把文件放进 .harness/skills/，install-skill 让当前工具真的会扫描它们</text>
  <text x="450" y="308" text-anchor="middle" fill="#94a3b8" font-size="11">换工具零成本：.harness/ 不重建，只需重跑一次注册动作</text>
</svg>
```

这个设计带来的一个副产品是：**技能体系与工具解耦后，换工具的成本趋近于零**。今天用 reasonix 跑流水线，明天切到 claude-code，`.harness/` 目录不需要重建——只有 Step 5.5 的"注册动作"需要重新执行一次。对一个要"分发给所有人"的脚手架来说，这可能是比功能本身更重要的事。

> 核心收获："工具无关"不是靠兼容层实现的，是靠"协议化"实现的——用通用的文本协议（SKILL.md + 斜杠命令 + change 卡片）而不是某个工具的私有配置。

## 九、并行开发：多 change 场景下的上下文锁定

> 🎯 **对应提示词 #13**："如果是并行开发的话，只输入命令就不行了吧"

第十二轮明确了"工具无关"和"install_skill 一键安装"，一切看起来都顺了——直到有人问了一个看似简单的问题：如果是并行开发的话，输入命令怎么确定该处理哪个 change？

这一问直接戳中了脚手架最薄弱的一环：**所有技能都假设"当前只有一个 change"**。让编码技能就执行编码，让评审技能就执行评审——但同一个项目完全可能同时有多个 change 在推进：一个在 coding，一个在 reviewing，一个在 drafting。如果用户输入 `/coding-skill`，系统怎么知道该处理哪一个？

答案回到第五章建立的 change 状态机，把它从"单线"升级为"多线"：`.harness/changes/` 目录下允许存在多个 change 目录，每个 change 维护自己的状态进度。技能的消费规则统一为三条：

1. 用户输入 `/coding-skill-xxx` 等技能命令后，系统扫描 `.harness/changes/*/change.md`，过滤出 `status: coding` 的 change
2. 根据结果数分支：
   - **0 个**：报错，退回 ① harnessing，没有可消费的 change
   - **1 个**：自动选中，锁定该 change，技能立即执行
   - **≥2 个**：列出候选清单，停下等待用户选择
3. 锁定后，该 change 的上下文（change.md 中的 AC、描述、任务分解）成为技能执行的唯一输入——其他 change 虽然存在，但当前命令不感知它们

这条规则被编码为技能的硬约束，写在每个技能的 SKILL.md 里。AI 不得擅自默认取第一个，也不得跳过"列出清单"的步骤。这保证了并行开发时，每个命令都只操作一个被显式锁定的 change——不会出现"A 的编码改了 B 的代码"。

把这一机制画成图，就是"多 change 场景 → 消费规则 → 上下文锁定"的链路：

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 900 460" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_par" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_par"><feDropShadow dx="0" dy="3" stdDeviation="4" flood-color="#000" flood-opacity="0.35"/></filter>
  </defs>
  <rect width="900" height="460" fill="url(#bg_par)" rx="10"/>
  <text x="450" y="32" text-anchor="middle" fill="#e2e8f0" font-size="26" font-weight="700">并行开发：多 change 上下文锁定机制</text>
  <g>
    <rect x="50" y="70" width="240" height="100" rx="10" fill="#1e293b" stroke="#14b8a6" stroke-width="1.5" filter="url(#sh_par)"/>
    <text x="170" y="96" text-anchor="middle" fill="#e2e8f0" font-size="13" font-weight="700">change-a · 用户登录</text>
    <text x="170" y="118" text-anchor="middle" fill="#cbd5e1" font-size="10.5">⬡ 多模块 Spring Boot 项目</text>
    <text x="170" y="136" text-anchor="middle" fill="#cbd5e1" font-size="10.5">状态：coding</text>
    <text x="170" y="156" text-anchor="middle" fill="#14b8a6" font-size="10.5">▶ 当前技能可消费</text>
    <rect x="330" y="70" width="240" height="100" rx="10" fill="#1e293b" stroke="#7c3aed" stroke-width="1.5" filter="url(#sh_par)"/>
    <text x="450" y="96" text-anchor="middle" fill="#e2e8f0" font-size="13" font-weight="700">change-b · 下订单</text>
    <text x="450" y="118" text-anchor="middle" fill="#cbd5e1" font-size="10.5">⬡ 复杂业务逻辑</text>
    <text x="450" y="136" text-anchor="middle" fill="#cbd5e1" font-size="10.5">状态：drafting</text>
    <text x="450" y="156" text-anchor="middle" fill="#64748b" font-size="10.5">⏳ 待审批</text>
    <rect x="610" y="70" width="240" height="100" rx="10" fill="#1e293b" stroke="#0e7490" stroke-width="1.5" filter="url(#sh_par)"/>
    <text x="730" y="96" text-anchor="middle" fill="#e2e8f0" font-size="13" font-weight="700">change-c · 订单管理</text>
    <text x="730" y="118" text-anchor="middle" fill="#cbd5e1" font-size="10.5">⬡ 数据库交互</text>
    <text x="730" y="136" text-anchor="middle" fill="#cbd5e1" font-size="10.5">状态：reviewing</text>
    <text x="730" y="156" text-anchor="middle" fill="#64748b" font-size="10.5">⏳ 待评审</text>
  </g>
  <line x1="170" y1="170" x2="170" y2="208" stroke="#64748b" stroke-width="2"/>
  <line x1="450" y1="170" x2="450" y2="208" stroke="#64748b" stroke-width="2"/>
  <line x1="730" y1="170" x2="730" y2="208" stroke="#64748b" stroke-width="2"/>
  <line x1="50" y1="190" x2="850" y2="190" stroke="#334155" stroke-width="1" stroke-dasharray="4,4"/>
  <rect x="50" y="208" width="800" height="94" rx="10" fill="#1e293b" stroke="#8b5cf6" stroke-width="1.25" filter="url(#sh_par)"/>
  <text x="450" y="232" text-anchor="middle" fill="#c4b5fd" font-size="15" font-weight="700">消费规则：筛选 status:coding</text>
  <text x="450" y="254" text-anchor="middle" fill="#cbd5e1" font-size="11">0 个 → 报错，退回 ① harnessing，没有可消费的 change</text>
  <text x="450" y="274" text-anchor="middle" fill="#cbd5e1" font-size="11">1 个 → 自动选中，锁定该 change，技能立即执行</text>
  <text x="450" y="294" text-anchor="middle" fill="#cbd5e1" font-size="11">≥2 个 → 列出候选清单，停下等待用户选择</text>
  <line x1="450" y1="302" x2="450" y2="322" stroke="#8b5cf6" stroke-width="2"/>
  <polygon points="450,326 444,318 456,318" fill="#8b5cf6"/>
  <rect x="250" y="326" width="400" height="44" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#0ea5e9" stroke-width="1.25" filter="url(#sh_par)"/>
  <text x="450" y="354" text-anchor="middle" fill="#7dd3fc" font-size="14" font-weight="700">锁定所选 change → 对应流水线技能执行</text>
  <text x="450" y="400" text-anchor="middle" fill="#c7d2fe" font-size="12">每个命令只消费一个被显式锁定的 change · 多 change 不是问题，隐式操作才是</text>
  <text x="450" y="426" text-anchor="middle" fill="#64748b" font-size="11">.harness/changes/ 目录是唯一事实源 · 多 change 共享同一份规则与参数表</text>
</svg>
```

> 核心收获：并行开发不靠"并发锁"，靠"显式上下文锁定"——每个技能只消费一个被显式选中的 change。多 change 不是问题，隐式操作才是。

## 十、框架版图的两次扩张：从 30+ 到 111

> 🎯 **对应提示词 #15 + #16 + #17**：拓展业界主流框架识别（Java 的 Dubbo/Spring AI/LangChain4j、Python 的 PyTorch/LangGraph/PydanticAI、Go 的 Kitex/Hertz/GoFrame 等）；新增 Rust（Axum/Actix/Tauri 等）与 PHP（Laravel/ThinkPHP/Hyperf/Symfony 等）两大语言生态

第十五到第十七轮，是纯粹的"喂框架"阶段，但喂法很有讲究。

**第一波（v1.2.0）**：Java 要支持 Dubbo、Spring Cloud Alibaba、Spring AI、AgentScope Java、LangChain4j、Semantic Kernel……Python 要支持 TensorFlow、PyTorch、LangChain、LangGraph、CrewAI、PydanticAI、OpenAI Agents SDK、Tornado……Go 要支持 Beego、Go-Kit、Go-Kratos、Kitex、Hertz、GoFrame、LangChainGo、cloudwego/eino、Firebase Genkit……30+ 个框架/构建工具的识别规则与参数块一次补齐。关键约束是："你只更新了外围的 skill，语言包下的 skill 并没有同步更新"——**参数变了，模板也要跟着变**，两者必须处于同一版本。

**第二波（v1.3.0）**：新增两个语言生态——Rust（Axum、Actix Web、Rocket、Warp、Poem、Loco、Candle、Burn、Tauri、Iced、egui、Dioxus……）与 PHP（Laravel、ThinkPHP、Hyperf、Swoole、WordPress、Symfony2、Yaf、GravCMS、webman……），参数表、检测表、语言文档、元数据全部同步。到这一轮结束时，项目的版图是：**6 种语言 × 111 个框架差异块**，检测表从"每种语言 10-20 条规则"增长到"每种 11-35 条"，合计 134 条检测规则。

配合"加检测"，还有一条一直没提的机制让这门生意做得下去：参数块的三级继承。**语言基础（如 Java 基础参数）→ 框架差异（如 Dubbo）→ 构建工具差异（如 Maven/Gradle）**，每一层只写本层差异，渲染时逐层覆盖、合并成最终参数集。这意味着加一个新框架，90% 的参数从语言基础块继承，只需补齐框架特有的 10%——111 个框架差异块听着吓人，其实每个块都很薄。三级继承是"为什么参数化能在 6 语言 × 111 框架的规模下仍然可控"的技术答案，它与单一事实源是同一枚硬币的两面。

这里有一个值得所有做"规则引擎"的人记住的教训：**框架扩张的瓶颈不在"识别"，而在"参数维护"**。每加一个框架，就要回答同样的问题——用什么构建命令？用什么测试框架？用什么 mock 库？用什么 lint 工具？——并且答案要落进参数表，还要保证模板里的每一个占位符都被覆盖。111 个框架差异块、51 个参数键、29 个模板占位符之间的"全覆盖、零缺失"，不是运气，是每轮都用脚本核对出来的纪律。

把四个版本放在一起，能清楚地看到"喂框架"的节奏：

| 版本 | 语言 | 框架/差异块 | 技能/规则 | 里程碑事件 |
|------|------|------------|----------|-----------|
| 1.0.0 | 4 种 | 少量 | 9 技能 · 5 规则 | 种子落地 |
| 1.1.0 | 4 种 | 40+ 识别规则 | 参数块化 | 参数表架构重构 |
| 1.2.0 | 4 种 | 30+ 框架 | 8 技能 × 4 语言参数化 | 框架大扩张 |
| 1.3.0 | 6 种 | 111 框架差异块 | 10 技能 × 6 语言 | Rust/PHP 加入 |
| Unreleased | 6 种 | 111 框架差异块 | 30 技能·抽离收敛 | 方案 A 清理 |

到 v1.3.0 结束时，111 个框架差异块在各语言间的分布是：Java 16、Python 16、Go 21、Rust 19、PHP 30、Frontend 9。注意 PHP 以 30 个差异块居首——因为它同时覆盖 Web 框架、CMS/电商和 AI/LLM 三个生态（Laravel、ThinkPHP、Hyperf、Swoole 之外，还有 WordPress、WooCommerce、Drupal、GravCMS，乃至 Laravel AI SDK、LLPhant 等），是"一个语言包横跨三个生态"的极端案例。这个分布也解释了为什么 PHP 语言包是后期最难维护的一个——**生态多样性会直接转化为参数表负担**，而三级继承正是为这种负担准备的。差异块本身也是一张"生态分布图"：

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 900 380" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_c9" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_c9"><feDropShadow dx="0" dy="3" stdDeviation="4" flood-color="#000" flood-opacity="0.35"/></filter>
  </defs>
  <rect width="900" height="380" fill="url(#bg_c9)" rx="10"/>
  <text x="450" y="32" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">111 个框架差异块：六语言分布与三级继承</text>
  <g font-size="12">
    <text x="140" y="92" text-anchor="end" fill="#e2e8f0">Java</text>
    <text x="140" y="132" text-anchor="end" fill="#e2e8f0">Python</text>
    <text x="140" y="172" text-anchor="end" fill="#e2e8f0">Go</text>
    <text x="140" y="212" text-anchor="end" fill="#e2e8f0">Rust</text>
    <text x="140" y="252" text-anchor="end" fill="#e2e8f0">PHP</text>
    <text x="140" y="292" text-anchor="end" fill="#e2e8f0">Frontend</text>
  </g>
  <g>
    <rect x="160" y="72" width="176" height="24" rx="5" fill="#0ea5e9"/><text x="344" y="89" fill="#7dd3fc" font-size="11">16</text>
    <rect x="160" y="112" width="176" height="24" rx="5" fill="#14b8a6"/><text x="344" y="129" fill="#5eead4" font-size="11">16</text>
    <rect x="160" y="152" width="231" height="24" rx="5" fill="#8b5cf6"/><text x="399" y="169" fill="#c4b5fd" font-size="11">21</text>
    <rect x="160" y="192" width="209" height="24" rx="5" fill="#f59e0b"/><text x="377" y="209" fill="#fde68a" font-size="11">19</text>
    <rect x="160" y="232" width="330" height="24" rx="5" fill="#ef4444"/><text x="498" y="249" fill="#fca5a5" font-size="11">30</text>
    <rect x="160" y="272" width="99" height="24" rx="5" fill="#3b82f6"/><text x="267" y="289" fill="#93c5fd" font-size="11">9</text>
  </g>
  <text x="532" y="246" fill="#fca5a5" font-size="10.5">PHP 横跨 Web / CMS·电商 / AI·LLM 三生态</text>
  <text x="450" y="330" text-anchor="middle" fill="#c7d2fe" font-size="12">三级继承：语言基础（如 Java 基础参数）← 框架差异（如 Dubbo）← 构建工具差异（如 Maven/Gradle）</text>
  <text x="450" y="356" text-anchor="middle" fill="#64748b" font-size="11">每块只写本层差异，渲染时逐层覆盖合并 · 134 条检测规则（每种语言 11-35 条）</text>
</svg>
```

注意 1.2.0 的描述里有一句"语言包内部技能全部参数化（8 个核心技能 × 4 语言）"——它说明**参数化先于方案 A 发生**：早在 1.2.0，语言包里的技能文件就已经是"由参数渲染出来的产物"了。方案 A 不是发明参数化，而是把"渲染产物"和"事实源"彻底分离。这个时间差解释了为什么方案 A 敢删语言包技能——**删除的前提是它的信息已经完整转移到别处**。

> 核心收获：支持新框架的正确姿势是"加参数块 + 加检测规则 + 核对占位符覆盖"，而不是"复制一份技能文件改改"。前者是线性增长，后者是指数腐烂。

## 十一、终局：方案 A——抽离方案

> 🎯 **对应提示词 #18 + #19**："每个 harness-语言/skills/ 下的 6 大流水线跟 harness-core 大部分重复，能否删除掉，还能保证 /apply-harness 完整处理？请给出完整清理方案（仅讨论）"→"请按照方案 A 进行实施清理工作"

第十八轮，用户提出了整篇文章的高潮问题：

> "目前实现了 6 大语言的支持，每个 harness-语言/skills/ 下的 6 大流水线跟 harness-core/skills/ 下的大部分都是重复的，是否可以将语言下的删除掉，还可以保证 /apply-harness 注册&安装的时候可以完整处理呢？"

那时候摆在桌面上的其实有三个候选方案，删除的力度从保守到激进：

- **方案 A（抽离方案）**：10 个流水线技能全部收敛到 harness-core 参数化模板，从语言包删除；java 的 4 个框架专属技能保留在语言包。改动最大，但一劳永逸
- **方案 B（最小改动）**：只删 core 里已有模板的 5 个（coding-skill、harness-me、handoff、diagnosing-bugs、unit-test-write），另 5 个不敢动。风险最低，但漂移只解决了一半
- **方案 C（彻底清空）**：语言包 skills 目录全部清掉。最彻底，但 java 的 4 个框架专属技能无处安放，风险最高

最终选了 A。选择的理由值得展开：B 的"保守"其实并不安全——留着的 5 个副本照样漂移，只是把疼痛推迟；C 的"激进"忽略了一个事实——java 的专属技能（java-code-review、spring-api-convention、mybatis-toolkit、openfeign-toolkit）是真正语言绑定、永远不会参数化的内容，它们本来就不该被模板化。方案 A 不是"中间派"，而是**唯一同时满足"零重复"与"零信息丢失"的选项**：模板类内容回收进 core，专属类内容留在语言包，每一份信息都有唯一且合理的主人。

**重复从哪来的？** 这是历史遗留：早期语言包确实是"渲染产物"（core 模板渲染 → 复制到语言包 → 再复制到目标项目），但随着框架扩张和技能维护，语言包里的文件被反复手工调整，慢慢变成了"半渲染产物"——和 core 模板**内容漂移**了。60 份重复文件、六个地方维护同一件事，正是第三章埋下的"架构种子"在三个月后的代价：当时为支持多语言而做的"语言包复制"，在支持到第六种语言时变成了维护噩梦。

**为什么能删？** 因为经过第八到第九章的改造，语言差异已经被完整地参数化了：10 个流水线/辅助技能全部有 core 模板，占位符表达所有语言差异；语言特有内容（部署命令、架构审查关注点、自检项等）已下沉到各语言包 `rules/`。**信息没有丢失，只是换了存放位置**。验证方式是在六种语言 × 10 个技能上做"模拟渲染 → 逐 diff"，确认零信息丢失。

**方案 A 做了什么**：

1. `harness-core/skills/` 新增 5 个参数化模板：`arch-review`、`deploy-verify`、`expert-reviewer`、`harnessing`、`unit-test-ci`（补齐此前只有语言包、没有 core 模板的 5 个）
2. apply-harness Step 5 改为「渲染 core 模板 + 复制语言包专属技能」：10 个技能全部由 core 渲染，语言差异通过参数表占位符表达
3. 六种语言包删除 10 个同名技能目录，java 只保留 4 个框架专属技能（`java-code-review`、`spring-api-convention`、`mybatis-toolkit`、`openfeign-toolkit`）
4. 参数表补齐六语言基础参数块缺失键（ARCH_LAYER、TEST_FRAMEWORK、MOCK_LIB 等），6 语言 × 111 框架块对 29 个模板占位符全覆盖、零缺失

方案 A 后，语言包的长相发生了戏剧性的变化：python、golang、rust、php、front 五个语言包各只剩 3-4 个文件——一份 SKILL.md 入口描述 + 两份语言特有规则（编码规范、工程结构）——**skills 目录整个消失**；只有 java 语言包保留了 `skills/`，里面是 4 个框架专属技能。这种"不对称"不是偷懒，恰恰是分类学的诚实：**参数化把"大多数"收进了 core，剩下的就必然是"无法参数化"的部分**。每次安装时，apply-harness 的 Step 5 只做两件事：渲染 10 个 core 模板（语言差异由参数表注入）+ 复制 java 语言包的 4 个专属技能——六种语言，同一条渲染路径，物尽其用。

从此，仓库里任何一处技能逻辑**只有一个事实源**：模板在 core，差异在参数表，语言特有内容在语言包 rules。改动一处、六语言生效；新增一处、全链路同步。方案 A 的验收动作也成了项目的"例行体检"：渲染 → 比对 → 覆盖率核对 → 目录结构核对，四条命令跑完，漂移无处遁形。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 900 520" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_arch" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_arch"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="900" height="520" fill="url(#bg_arch)" rx="10"/>
  <text x="450" y="32" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">方案 A 之后的架构：抽离方案 + 渲染器</text>
  <rect x="60" y="60" width="780" height="86" rx="10" fill="#0ea5e9" opacity="0.10" stroke="#0ea5e9" stroke-width="1.5" filter="url(#sh_arch)"/>
  <text x="450" y="88" text-anchor="middle" fill="#7dd3fc" font-size="16" font-weight="700">apply-harness · 编排渲染器（2333 行 SKILL.md）</text>
  <text x="450" y="110" text-anchor="middle" fill="#94a3b8" font-size="12">检测语言/框架 → 读参数表 → 渲染 core 模板 → 复制语言包专属内容 → 注册技能</text>
  <text x="450" y="130" text-anchor="middle" fill="#64748b" font-size="11">Step 1-8（含 Step 5.5 技能注册、Step 7.1 共享语言上下文）</text>

  <rect x="60" y="188" width="250" height="130" rx="10" fill="#8b5cf6" opacity="0.10" stroke="#8b5cf6" stroke-width="1.5" filter="url(#sh_arch)"/>
  <text x="185" y="214" text-anchor="middle" fill="#c4b5fd" font-size="14" font-weight="700">harness-core 单一事实源</text>
  <text x="185" y="238" text-anchor="middle" fill="#94a3b8" font-size="11">10 个技能模板（参数化）</text>
  <text x="185" y="256" text-anchor="middle" fill="#94a3b8" font-size="11">16 个跨语言通用技能</text>
  <text x="185" y="274" text-anchor="middle" fill="#94a3b8" font-size="11">4 条通用规则 + owner.md 模板</text>
  <text x="185" y="292" text-anchor="middle" fill="#94a3b8" font-size="11">changes / wiki / CONTEXT 模板</text>
  <text x="185" y="310" text-anchor="middle" fill="#64748b" font-size="11">改一处 · 六语言生效</text>
  <rect x="360" y="188" width="250" height="130" rx="10" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_arch)"/>
  <text x="485" y="214" text-anchor="middle" fill="#fde68a" font-size="14" font-weight="700">参数表（差异事实源）</text>
  <text x="485" y="238" text-anchor="middle" fill="#94a3b8" font-size="11">51 参数键 · 117 参数块</text>
  <text x="485" y="256" text-anchor="middle" fill="#94a3b8" font-size="11">6 语言基础块 + 111 框架差异块</text>
  <text x="485" y="274" text-anchor="middle" fill="#94a3b8" font-size="11">29 个模板占位符全覆盖</text>
  <text x="485" y="292" text-anchor="middle" fill="#94a3b8" font-size="11">零缺失 · 脚本核对</text>
  <text x="485" y="310" text-anchor="middle" fill="#64748b" font-size="11">java/python/golang/rust/php/front</text>
  <rect x="660" y="188" width="180" height="130" rx="10" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_arch)"/>
  <text x="750" y="214" text-anchor="middle" fill="#86efac" font-size="14" font-weight="700">语言包（差异承载）</text>
  <text x="750" y="238" text-anchor="middle" fill="#94a3b8" font-size="11">2 条语言特有规则</text>
  <text x="750" y="256" text-anchor="middle" fill="#94a3b8" font-size="11">SKILL.md 入口描述</text>
  <text x="750" y="274" text-anchor="middle" fill="#94a3b8" font-size="11">java 另含 4 框架专属技能</text>
  <text x="750" y="292" text-anchor="middle" fill="#94a3b8" font-size="11">（其余语言无技能副本）</text>
  <text x="750" y="310" text-anchor="middle" fill="#64748b" font-size="11">零重复 · 零漂移</text>
  <line x1="185" y1="146" x2="185" y2="188" stroke="#334155" stroke-width="2"/>
  <polygon points="185,194 179,186 191,186" fill="#64748b"/>
  <line x1="485" y1="146" x2="485" y2="188" stroke="#334155" stroke-width="2"/>
  <polygon points="485,194 479,186 491,186" fill="#64748b"/>
  <line x1="750" y1="146" x2="750" y2="188" stroke="#334155" stroke-width="2"/>
  <polygon points="750,194 744,186 756,186" fill="#64748b"/>
  <line x1="450" y1="318" x2="450" y2="350" stroke="#334155" stroke-width="2"/>
  <polygon points="450,356 444,348 456,348" fill="#64748b"/>
  <rect x="60" y="364" width="780" height="110" rx="10" fill="#ef4444" opacity="0.10" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_arch)"/>
  <text x="450" y="394" text-anchor="middle" fill="#fca5a5" font-size="15" font-weight="700">产物 .harness/（目标项目内）</text>
  <text x="450" y="420" text-anchor="middle" fill="#94a3b8" font-size="12">owner.md（渲染后） + rules（4 通用 + 2 语言特有）</text>
  <text x="450" y="442" text-anchor="middle" fill="#94a3b8" font-size="12">skills：30 个可执行技能（10 模板渲染 + 16 通用 + 4 java 专属）</text>
  <text x="450" y="464" text-anchor="middle" fill="#94a3b8" font-size="12">changes / wiki / CONTEXT（模板渲染）</text>
  <text x="450" y="508" text-anchor="middle" fill="#475569" font-size="11">人类设计约束 → AI 编写代码 → 机器验证质量</text>
</svg>
```

## 终章：一万步之后，我们学到了什么

回头看这 19 轮讨论、10 个里程碑，值得沉淀的不只是架构，还有三条方法论。

**第一，抽象的时机：不是越早越好，是"第二次重复"时最好。** 第一次抽离（rules → core）发生在第四个语言包出现之前，靠的是前瞻；最后一次抽离（方案 A）发生在第六个语言包出现之后，靠的是痛感。如果一开始就把所有参数化做完，第一版根本跑不起来；如果一直不抽，第六种语言落地时维护成本会爆炸。脚手架的生命节奏，就是"喂功能 → 疼了 → 抽离"的循环。

**第二，参数化是双刃剑：它同时是"自由"和"约束"。** 51 个参数键让六种语言的差异可控，但每新增一个参数，就要维护 117 个参数块里的对应键。项目用两条纪律驯服了这把剑：**占位符全覆盖核对**（29 个模板占位符必须全部有值，缺失即报错）和**单一事实源**（技能逻辑只在 core 出现一次）。参数化的自由，必须用机械化的核对来买单。

**第三，脚手架的灵魂不是技术，是"人设"。** 从 owner.md 被点醒的那一刻起，这个项目的定位就清晰了：它不是"规则文件打包器"，而是一个**有性格的工程教练**——它要求 AI 遵守纪律，也承诺给 AI 信任的正确方式。框架会过时，参数会膨胀，但"人类设计约束、AI 编写代码、机器验证质量"这条信条，和承载它的 Owner Agent，才是整个脚手架真正不可抽离的内核。

**第四，警惕过度抽象——抽象是有保质期的。** 这篇文章通篇在讲抽离，但必须补一句反话：**不是所有重复都该立刻消除**。第一版只有一种语言、一个项目时，语言包复制是最务实的方案——抽象成参数表的成本远超收益；方案 A 之所以在第六种语言时才执行，是因为只有到那时重复才变成了漂移、漂移才变成了事故。判断抽离时机的信号只有一个：**修改是否开始需要同时改动多处**。一次改一个文件 = 复制是合理的；一次改六个文件 = 抽离的时候到了。这个项目最幸运的地方，是每次抽离都恰好发生在"痛感出现之后、积重难返之前"。

回到开篇的那句信条。仔细想想，这句口号本身也在悄悄演进：最早的版本可能只是"让 AI 写得更好"，后来变成了"人类设计约束，AI 编写代码，机器验证质量"——它把三个主体、三种职责、三层机制压缩进了十六个字。这就是这篇文章想让你带走的最内核的东西：**好的脚手架，是把一句正确的口号，翻译成几百个可以核查的细节；而好的演进，是让这些细节始终只有一个家。**

最后补一句关于"产品面"的观察。一个脚手架项目，其实有三张脸，缺一不可：**工程面**（skills/ 下的代码与模板，解决"怎么跑"）、**文档面**（docs/ 下的 6 阶段流水线、Owner Agent、SDD-TDD、变更管理、语言规范包，解决"怎么懂"）、**叙事面**（docs/articles/ 下的专栏文章，解决"为什么信"）。第一张脸决定它好不好用，第二张脸决定它好不好上手，第三张脸决定别人愿不愿意用它。很多开源项目死在第三张脸——工具很好，但没人讲得清它解决什么问题。

如果你正在做一件"想被别人用"的工具，这篇文章最值得带走的一句话是：**把"能不能抽离"当成默认问题，把"谁会是唯一事实源"当成默认答案**。抽离会迟到，但不会缺席——与其等第六种语言来逼你，不如在第二种语言出现时就问自己：这些重复，属于谁？

最后，把整个过程压缩成一张可复用的**行动清单**，任何"把项目私有制品做成通用工具"的尝试都可以照着走：

1. **盘点灵魂文件**——先找出定义系统如何自我运行的那个文件，它决定了抽离的边界
2. **建立差异维度**——问"什么随语言/场景/工具变化"，把差异显式列成表，而不是藏在文件副本里
3. **让差异参数化**——会变的写成占位符，不会变的收进模板，并在两层之间建立"全覆盖核对"
4. **保持检测与参数同步**——每加一个识别规则，必须同步加一个参数块，用脚本锁死这个约束
5. **用上下文对象串技能**——一张 change 卡片 + 状态机，让所有技能共享同一份"现在做到哪了"
6. **吸收外部沉淀**——但先找到"它应该挂在哪"，没有位置的技能不吸收
7. **单一事实源收尾**——当重复开始漂移，果断删副本，把信息收敛到唯一持有者，用渲染-比对验收

这七步走完，你的"一次性目录"就有资格叫"跨语言脚手架"了。Tayama 走了一万步，但每一步都只验证了一个问题：**价值是否仍然只存在于一个地方**。

*本文是 Harness 脚手架演进复盘。相关系列文章见 docs/articles/ 下 column-01 至 column-53（完整清单见 [column-series.md](./column-series.md)）与《harness-scaffolding-deep-dive》。*
