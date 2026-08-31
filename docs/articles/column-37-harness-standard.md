# /harness-standard：规范库——只收"达成共识"的约定，重大约束留人审批

> 命令深度拆解 · 第 37 篇 · 约 9000 字 · 7 个 SVG 图

---

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 300" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_t0" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_t0"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="300" fill="url(#bg_t0)" rx="10"/>
  <text x="400" y="28" text-anchor="middle" fill="#e2e8f0" font-size="26" font-weight="700">/harness-standard：规范库，只收共识</text>
  <rect x="60" y="55" width="320" height="95" rx="10" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_t0)"/>
  <text x="220" y="80" text-anchor="middle" fill="#fde68a" font-size="14" font-weight="700">四类规范文件</text>
  <text x="220" y="104" text-anchor="middle" fill="#94a3b8" font-size="11">编码规范 / 变更定位 / 运维审计 / AGENTS</text>
  <text x="220" y="126" text-anchor="middle" fill="#64748b" font-size="10">沉淀团队"怎么做"的约定</text>
  <rect x="420" y="55" width="320" height="95" rx="10" fill="#ef4444" opacity="0.12" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_t0)"/>
  <text x="580" y="80" text-anchor="middle" fill="#fca5a5" font-size="14" font-weight="700">重大约束留人审批</text>
  <text x="580" y="104" text-anchor="middle" fill="#94a3b8" font-size="11">技术选型 / 架构约束 / 破坏性改动</text>
  <text x="580" y="126" text-anchor="middle" fill="#64748b" font-size="10">写入前必须人类确认</text>
  <text x="400" y="185" text-anchor="middle" fill="#475569" font-size="13">定位：能查、能改、知道边界，比"新写一套规范"重要</text>
  <rect x="60" y="205" width="160" height="45" rx="8" fill="#3b82f6" opacity="0.10" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_t0)"/>
  <text x="140" y="234" text-anchor="middle" fill="#93c5fd" font-size="11">① 定位/扩充</text>
  <rect x="240" y="205" width="160" height="45" rx="8" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_t0)"/>
  <text x="320" y="234" text-anchor="middle" fill="#86efac" font-size="11">② 评估约束</text>
  <rect x="420" y="205" width="160" height="45" rx="8" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_t0)"/>
  <text x="500" y="234" text-anchor="middle" fill="#fde68a" font-size="11">③ 写入留痕</text>
  <rect x="600" y="205" width="160" height="45" rx="8" fill="#ef4444" opacity="0.10" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_t0)"/>
  <text x="680" y="234" text-anchor="middle" fill="#fca5a5" font-size="11">④ 引用告知</text>
  <text x="400" y="280" text-anchor="middle" fill="#64748b" font-size="10">纪律：只收共识 · 重大约束人审批 · 改动留痕 · 避免规范膨胀</text>
</svg>
```

## 一、/harness-standard 解决的是什么问题

### 1.1 规范散落在哪，团队就乱在哪

每个团队都有一堆"应该这么做"的约定：代码风格、表命名、错误码规范、发版流程、安全红线。但约定的存在形式五花八门——有人记在文档里，有人记在脑子里，有人记在 PR 评论里，更多人默认"大家应该都知道"。

规范散落，就必然产生三类问题：

1. **找不到**——新成员问"我们怎么命名接口"，没人能指一个地方说"看这里"；
2. **不一致**——老成员按旧的来，新成员按新的来，代码风格四分五裂；
3. **没权威**——规范从没被"正式"认可，任何人不遵守都不觉得自己在违反什么。

`/harness-standard` 就是给这些约定一个正式的家：**沉淀团队规范到 `.harness/rules/`（或项目根 AGENTS.md），与 `harnessing` 的需求分析、`coding-skill` 的编码执行形成闭环**。

### 1.2 规范库的四类文件

`.harness/rules/` 下有四类规范文件，各有职责：

| 文件 | 内容 | 谁消费 |
|------|------|--------|
| `编码规范.md` | 语言风格、命名、测试约定 | coding-skill / unit-test-write |
| `变更定位规则.md` | change 命名、状态定义、流程 | harnessing / status / loop-run |
| `运维审计规则.md` | 日志、审计、异常检测要求 | deploy-verify / diagnose |
| AGENTS.md（项目根） | 当前会话的全局工作约束 | 所有技能的默认上下文 |

四类文件覆盖了流水线的四个侧面：**怎么写代码**（编码规范）、**怎么管变更**（变更定位规则）、**怎么守生产**（运维审计规则）、**怎么干活**（AGENTS.md）。本技能维护这几类文件的"增改查"——它的定位是**规范库的管理员**。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 250" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_t1" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_t1"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="250" fill="url(#bg_t1)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">四类规范文件</text>
  <rect x="40" y="50" width="340" height="60" rx="8" fill="#3b82f6" opacity="0.10" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_t1)"/>
  <text x="210" y="72" text-anchor="middle" fill="#93c5fd" font-size="11" font-weight="700">编码规范.md</text>
  <text x="210" y="94" text-anchor="middle" fill="#64748b" font-size="9">语言风格/命名/测试 → coding-skill 消费</text>
  <rect x="420" y="50" width="340" height="60" rx="8" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_t1)"/>
  <text x="590" y="72" text-anchor="middle" fill="#fde68a" font-size="11" font-weight="700">变更定位规则.md</text>
  <text x="590" y="94" text-anchor="middle" fill="#64748b" font-size="9">change 命名/状态/流程 → harnessing 消费</text>
  <rect x="40" y="130" width="340" height="60" rx="8" fill="#ef4444" opacity="0.10" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_t1)"/>
  <text x="210" y="152" text-anchor="middle" fill="#fca5a5" font-size="11" font-weight="700">运维审计规则.md</text>
  <text x="210" y="174" text-anchor="middle" fill="#64748b" font-size="9">日志/审计/异常 → deploy-verify 消费</text>
  <rect x="420" y="130" width="340" height="60" rx="8" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_t1)"/>
  <text x="590" y="152" text-anchor="middle" fill="#86efac" font-size="11" font-weight="700">AGENTS.md（项目根）</text>
  <text x="590" y="174" text-anchor="middle" fill="#64748b" font-size="9">全局工作约束 → 所有技能默认上下文</text>
  <text x="400" y="232" text-anchor="middle" fill="#64748b" font-size="10">四类文件覆盖：怎么写代码 / 怎么管变更 / 怎么守生产 / 怎么干活</text>
</svg>
```

### 1.3 与引用它的技能的关系

规范的"消费方"就是流水线的各个技能：

- `coding-skill` 读了 `编码规范.md` 才知道"命名用驼峰还是下划线"；
- `harnessing` 按 `变更定位规则.md` 生成 change 卡；
- `expert-reviewer` 按 `编码规范.md` 判问题；
- `harness-standard` 不直接消费这些规范——它维护它们。

所以本技能是"规范的规范"：其他技能按规定写代码，本技能负责规定本身怎么被管理——**能查、能改、知道边界**。

## 二、变更的三个来源

什么情况下会调用 `/harness-standard` 改规范？三个来源：

1. **实现中发现规范缺失/不适用**：`coding-skill` 实现时发现"规范没写这个场景"或"规范与语言包冲突"——这是最常见的来源；
2. **评审标注意见**：`expert-reviewer` 标注"命名规范未定义"——评审暴露的规范空白；
3. **用户直接要求/架构决定**：人类指定要更新某条规范——最有分量的来源。

这三个来源的优先级不同：用户要求 > 评审意见 > 实现发现。**用户直接要求意味着人类已经拍板**，几乎不需要再走审批；而实现中发现和评审意见，本质是"提议"——要过完评估流程才能写进规范库。

## 三、四步执行流程

### Step 1: 定位与扩充

按 `$ARGUMENTS` 或上下文定位目标：

- 指定某条规范 → 直接定位到对应文件；
- 未指定 → 列出 `.harness/rules/` 现有文件索引 + 简述 + 最近变更，供用户选择。

Step 1 的原则是"**先定位，后动笔**"：规范库的首要价值是"可查"——每次改之前先确认"这条约定现在在哪、已有规范怎么说"。盲目新写最危险：很可能你准备新写的规范，原有文件里已经有类似条目，只是你没找到——重复且矛盾的规范比没有规范更糟。

### Step 2: 评估约束性质

增量写入前评估：这条约束是**轻量约定**还是**重大约束**？

| 性质 | 特征 | 处理 |
|------|------|------|
| 🟢 轻量约定 | 命名风格、提交格式、局部编码习惯 | 直接写入 |
| 🔴 重大约束 | 技术选型、架构约束、破坏性改动 | **留人审批** |

这个分类是规范管理的分水岭：

- **轻量约定**（命名、提交格式、局部习惯）——影响小、好改，直接写入即可；
- **重大约束**（技术选型、架构约束、破坏性改动）——影响全局、难撤销，**写入必须人类确认**。

"技术选型"尤其要警惕：选型决定整个项目的走向，写进规范库后所有技能都会遵守——一旦写错，等于全项目跟着错；架构约束决定代码的骨架，破坏性改动影响存量代码——更不能让 AI 单独拍板。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 240" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_t2" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_t2"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="240" fill="url(#bg_t2)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">Step 2：先分轻重大，再决定要不要审批</text>
  <rect x="40" y="50" width="330" height="120" rx="8" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_t2)"/>
  <text x="205" y="76" text-anchor="middle" fill="#86efac" font-size="13" font-weight="700">🟢 轻量约定</text>
  <text x="205" y="102" text-anchor="middle" fill="#94a3b8" font-size="10">命名风格/提交格式/局部习惯</text>
  <text x="205" y="124" text-anchor="middle" fill="#94a3b8" font-size="10">影响小、好改</text>
  <text x="205" y="146" text-anchor="middle" fill="#64748b" font-size="9">直接写入 ✅</text>
  <rect x="420" y="50" width="340" height="120" rx="8" fill="#ef4444" opacity="0.10" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_t2)"/>
  <text x="590" y="76" text-anchor="middle" fill="#fca5a5" font-size="13" font-weight="700">🔴 重大约束</text>
  <text x="590" y="102" text-anchor="middle" fill="#94a3b8" font-size="10">技术选型/架构约束/破坏性改动</text>
  <text x="590" y="124" text-anchor="middle" fill="#94a3b8" font-size="10">影响全局、难撤销</text>
  <text x="590" y="146" text-anchor="middle" fill="#64748b" font-size="9">写入必须人类确认 👤</text>
  <text x="400" y="206" text-anchor="middle" fill="#475569" font-size="11">选型写错 = 全项目跟着错；架构约束写错 = 存量代码跟着错</text>
</svg>
```

### Step 3: 写入留痕

- 新增/更新规范文件，写明改动理由、影响范围、来源（哪个 change/谁提出）；
- 刚改的规范，下次对话前先读回——**对话惯例**保证规范与实际状态一致；
- 增量追加为主：规范库是"续写"的，不是"重写"的。

"写入留痕"解决的是规范的可信问题：没有来源的规范，没人知道它为什么存在，也就没人敢遵守。每条规范带上来源（change ID 或提出人），将来想改的时候，`harness-standard` 才能评估"这条规范是轻量约定还是重大约束、谁来拍板"。

**对话惯例**是这里的一个细节：AI 改完规范，下次会话开始时先读回规范文件，避免"上次的修改这次不知道"——规范库必须与真实状态同步，否则技能按旧规范干活，改了个寂寞。

### Step 4: 引用告知

- 若改动影响其他技能行为，在 AGENTS.md 或变更说明中标注"引用了哪些规范"；
- 让消费方知道：什么规范变了、影响哪个技能。

Step 4 是闭环的最后一步：**规范改了，消费方要知道**。`coding-skill` 等着读 `编码规范.md`，`harnessing` 等着读 `变更定位规则.md`——改完不告知，等于改了又没改。

## 四、四条纪律：规范库的底线

- **只收"达成共识"的约定**：拿不准、没讨论过的规范不写入——规范的权威来自共识，不来自文档本身；
- **重大约束留人审批**：技术选型、架构约束、破坏性改动必须人类确认，AI 不单独拍板；
- **改动留痕**：写明来源、理由、影响范围，可追溯；
- **避免规范膨胀**：只维护必要的规范，不把所有经验都塞进规范库。

这四条纪律归成一句话：**规范库是团队的契约，不是 AI 的备忘录**。

- "只收共识"保证权威——没人认同的规范写了也白写；
- "重大人审"保证安全——影响全局的决定不由 AI 单独做；
- "改动留痕"保证可信——每条规范都有出处；
- "避免膨胀"保证可维护——规范越多，遵守率越低。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 280" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_t3" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_t3"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="280" fill="url(#bg_t3)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">四条纪律</text>
  <rect x="40" y="50" width="340" height="70" rx="8" fill="#3b82f6" opacity="0.10" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_t3)"/>
  <text x="210" y="72" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">① 只收共识</text>
  <text x="210" y="98" text-anchor="middle" fill="#64748b" font-size="9">拿不准的不写入——权威来自共识</text>
  <rect x="420" y="50" width="340" height="70" rx="8" fill="#ef4444" opacity="0.10" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_t3)"/>
  <text x="590" y="72" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">② 重大约束留人审批</text>
  <text x="590" y="98" text-anchor="middle" fill="#64748b" font-size="9">选型/架构/破坏性改动必须人类确认</text>
  <rect x="40" y="140" width="340" height="70" rx="8" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_t3)"/>
  <text x="210" y="162" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">③ 改动留痕</text>
  <text x="210" y="188" text-anchor="middle" fill="#64748b" font-size="9">写明来源/理由/影响范围</text>
  <rect x="420" y="140" width="340" height="70" rx="8" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_t3)"/>
  <text x="590" y="162" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">④ 避免规范膨胀</text>
  <text x="590" y="188" text-anchor="middle" fill="#64748b" font-size="9">只维护必要的，不把经验全塞进去</text>
  <text x="400" y="250" text-anchor="middle" fill="#475569" font-size="11">一句话：规范库是团队的契约，不是 AI 的备忘录</text>
</svg>
```

## 五、与相邻技能的分工

| 场景 | 归属 |
|------|------|
| 规范增改查 | **本技能**（`/harness-standard`） |
| 按规范写代码 | `coding-skill` |
| 需求转 change 卡 | `harnessing` |
| 按规范评审 | `expert-reviewer` |

- **harness-standard vs coding-skill**：一个定规矩，一个守规矩——standard 保证规范存在且可查，coding-skill 保证代码按规范产出；
- **harness-standard vs harnessing**：一个管"怎么做"的约定，一个管"做什么"的需求——来源不同、归档位置不同（rules/ vs changes/）；
- **harness-standard vs expert-reviewer**：一个维护评判标准，一个使用评判标准——reviewer 判"合不合规"时，标准来自 standard 的规范库。

边界一句话：**本技能不写代码、不写需求、不判问题——它只管理"规范本身"**。

## 六、完成标志

`/harness-standard` 的完成标志有三个：

1. **规范文件已更新**（新增/修正/废止），改动带来源与理由——不是无据的改动；
2. **约束性质已评估**：重大约束留人审批，轻量约定直接写入——该请示的请示了，该直改的直改了；
3. **消费方已知晓**：影响到的技能/文件已在 AGENTS.md 或变更说明中标注引用——改完不是终点，让该知道的知道。

三个标志对应三问：**落地了吗**（文件确实改了）？**合规吗**（轻重评估对了）？**闭环了吗**（消费方知道了）？——三个都答"是"，规范管理才算合格。

## 七、写在最后

`/harness-standard` 的全部设计，浓缩成四句话：

1. **规范库是团队共识的正式家**——散落在 PR 评论和记忆里的约定，终于有个能指的地方。
2. **只收共识，重大约束留人审批**——AI 管"整理"，人类管"拍板"。
3. **改动留痕，避免膨胀**——每条规范可追溯，规范库不变成垃圾桶。
4. **改完告知消费方**——规范的生命力在于被执行，不在于被记录。

一句话记住它：**/harness-standard 是团队规范库的"管理员"——它把达成共识的约定沉淀成可查、可改、有边界的规则文件，让技术选型和架构约束这种重大决定永远留给人来拍板。**