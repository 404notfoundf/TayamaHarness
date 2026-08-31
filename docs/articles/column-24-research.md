# /research：把"查一下"变成可追溯的调研资产

> 命令深度拆解 · 第 24 篇 · 约 8500 字 · 7 个 SVG 图

---

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 300" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_r0" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_r0"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="300" fill="url(#bg_r0)" rx="10"/>
  <text x="400" y="28" text-anchor="middle" fill="#e2e8f0" font-size="26" font-weight="700">/research：把"查一下"变成可追溯的调研资产</text>
  <rect x="70" y="55" width="300" height="100" rx="10" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_r0)"/>
  <text x="220" y="82" text-anchor="middle" fill="#93c5fd" font-size="14" font-weight="700">后台 agent 调研</text>
  <text x="220" y="106" text-anchor="middle" fill="#94a3b8" font-size="11">主 agent 继续工作，不阻塞</text>
  <text x="220" y="128" text-anchor="middle" fill="#64748b" font-size="10">把阅读工作委派出去</text>
  <rect x="430" y="55" width="300" height="100" rx="10" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_r0)"/>
  <text x="580" y="82" text-anchor="middle" fill="#86efac" font-size="14" font-weight="700">只查一手来源</text>
  <text x="580" y="106" text-anchor="middle" fill="#94a3b8" font-size="11">官方文档 · 源码 · 规格 · 第一方 API</text>
  <text x="580" y="128" text-anchor="middle" fill="#64748b" font-size="10">每个断言追回到拥有它的来源</text>
  <rect x="70" y="180" width="660" height="55" rx="8" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_r0)"/>
  <text x="400" y="203" text-anchor="middle" fill="#fde68a" font-size="13" font-weight="700">产出：单个 Markdown 文件，落进 .harness/wiki/research/</text>
  <text x="400" y="224" text-anchor="middle" fill="#94a3b8" font-size="10">research/YYYY-MM-DD-<topic>.md · 每个断言有来源引用</text>
  <text x="400" y="262" text-anchor="middle" fill="#64748b" font-size="11">核心洞察：调研不是"记在脑子里"，是落成可复用的仓库资产</text>
  <text x="400" y="284" text-anchor="middle" fill="#475569" font-size="10">一次调研 = 一次沉淀 —— 相同的"查一下"不需要问第二遍</text>
</svg>
```

## 一、/research 解决的是什么问题

### 1.1 "查一下"这个动作的三个浪费

在开发中，"查一下"是最频繁、最不被当回事的动作：

- "查一下 Go 的 `context.WithTimeout` 会不会自动取消子 context。"
- "查一下这个 API 的限流参数到底叫什么。"
- "查一下 Postgres 的 `ON CONFLICT` 对部分索引的支持。"

每次"查一下"都有三个隐藏浪费：

1. **时间浪费**：查的过程要打开文档、找位置、确认版本、消化信息——如果由主 agent 亲自做，主流程就被打断了。
2. **重複浪费**：查完就忘。两周后同一个问题，又要重新查一遍——因为没人把结论记下来。
3. **信任浪费**：如果信息来源是二手转述（博客、问答、AI 复述），你永远不知道它针对的是哪个版本、哪个配置——结论不可信。

`/research` 一次解决这三个浪费：**后台查**（不阻塞主流程）、**落盘**（结论成为资产）、**一手来源**（结论可信可追溯）。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 240" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_r1" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_r1"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="240" fill="url(#bg_r1)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">"查一下"的三个浪费 → /research 的三个解法</text>
  <rect x="40" y="50" width="230" height="110" rx="10" fill="#ef4444" opacity="0.10" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_r1)"/>
  <text x="155" y="74" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">时间浪费</text>
  <text x="155" y="98" text-anchor="middle" fill="#94a3b8" font-size="10">查文档打断主流程</text>
  <text x="155" y="118" text-anchor="middle" fill="#64748b" font-size="9">→ 后台 agent，主流程不阻塞</text>
  <rect x="290" y="50" width="230" height="110" rx="10" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_r1)"/>
  <text x="405" y="74" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">重复浪费</text>
  <text x="405" y="98" text-anchor="middle" fill="#94a3b8" font-size="10">查完就忘，两周后再查</text>
  <text x="405" y="118" text-anchor="middle" fill="#64748b" font-size="9">→ 落盘为仓库资产</text>
  <rect x="540" y="50" width="220" height="110" rx="10" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_r1)"/>
  <text x="650" y="74" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">信任浪费</text>
  <text x="650" y="98" text-anchor="middle" fill="#94a3b8" font-size="10">二手转述不可信</text>
  <text x="650" y="118" text-anchor="middle" fill="#64748b" font-size="9">→ 只查一手来源 + 引用</text>
  <text x="400" y="190" text-anchor="middle" fill="#475569" font-size="11">三个解法对应三种浪费——技能的每个设计点都打在一个具体的痛点上</text>
  <text x="400" y="214" text-anchor="middle" fill="#64748b" font-size="10">没有落盘的调研 = 没有发生的调研</text>
</svg>
```

### 1.2 为什么必须"一手来源"

`/research` 最硬的纪律是：**只查一手来源——官方文档、源码、规格、第一方 API——不是对它们的二手转述**。每个断言都追回到拥有它的来源。

为什么这么严格？因为二手转述的问题不在"转述者心怀恶意"，而在**信息在传播中必然失真**：

| 传播层 | 失真来源 |
|--------|---------|
| 官方文档 → 博客 | 博主可能基于旧版本，忘了标注版本号 |
| 博客 → 问答/笔记 | 上下文被裁剪，"在 X 前提下成立"变成"普遍成立" |
| 问答 → AI 训练语料 | 多条互相矛盾的二手信息被混合平均，得出"最像"但不是"最对"的答案 |

一手来源的权威性可验证：文档或源码本身就在那里，任何人都能对着它复核。二手来源的权威性不可验证：你只能选择信或不信。

**纪律细则**：每个断言必须能追回到来源。这不是要求每个句子都带链接（太沉重），而是要求"如果被追问'你怎么知道的'，你能指出那个一手来源"。

### 1.3 与普通"搜答案"的区别

`/research` 不是普通的"搜索然后回答"。它有三个独有特征：

1. **后台执行**：启动一个后台 agent 做调研，主 agent 继续工作不阻塞。调研完成时，主 agent 才收到摘要。
2. **产物落盘**：调研结果写进单个 Markdown 文件，保存到仓库的 `.harness/wiki/research/`。它成为项目资产，下次同样的问题直接读文件。
3. **引用可追溯**：每个断言有来源引用。读者可以顺着引用回到一手材料复核。

这三条合起来，把"查一下"从一次性动作升级成了**可积累的知识基础设施**。

## 二、调研的执行方式：后台线程与不阻塞

### 2.1 委派模型：主 agent 的工作方式

`/research` 的委派模型很明确：**主 agent 负责"继续干正事"，后台 agent 负责"查证"**。

当主 agent 遇到"需要查证外部事实"的场景（API 是否支持某能力、某依赖的正确用法、某规范的权威解释），它不会停下来自己查，而是：

1. 把问题包装成明确的调研任务；
2. 启动后台 agent 执行；
3. **继续自己的主线工作**（编码、评审、需求分析）；
4. 后台 agent 完成时，把摘要交给主 agent，主 agent 决定如何使用结论。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 260" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_r2" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_r2"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="260" fill="url(#bg_r2)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">委派模型：主线程不阻塞</text>
  <rect x="40" y="55" width="200" height="90" rx="10" fill="#3b82f6" opacity="0.10" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_r2)"/>
  <text x="140" y="80" text-anchor="middle" fill="#93c5fd" font-size="13" font-weight="700">主 agent</text>
  <text x="140" y="104" text-anchor="middle" fill="#94a3b8" font-size="10">遇到外部事实盲点</text>
  <text x="140" y="124" text-anchor="middle" fill="#64748b" font-size="9">不亲自查，委派出去</text>
  <line x1="240" y1="80" x2="280" y2="80" stroke="#475569" stroke-width="2"/>
  <polygon points="286,80 278,75 278,85" fill="#94a3b8"/>
  <rect x="295" y="55" width="210" height="90" rx="10" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_r2)"/>
  <text x="400" y="80" text-anchor="middle" fill="#fde68a" font-size="13" font-weight="700">后台 agent</text>
  <text x="400" y="104" text-anchor="middle" fill="#94a3b8" font-size="10">只查一手来源</text>
  <text x="400" y="124" text-anchor="middle" fill="#64748b" font-size="9">写 Markdown + 引用来源</text>
  <line x1="505" y1="80" x2="545" y2="80" stroke="#475569" stroke-width="2"/>
  <polygon points="551,80 543,75 543,85" fill="#94a3b8"/>
  <rect x="560" y="55" width="200" height="90" rx="10" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_r2)"/>
  <text x="660" y="80" text-anchor="middle" fill="#86efac" font-size="13" font-weight="700">主 agent 继续</text>
  <text x="660" y="104" text-anchor="middle" fill="#94a3b8" font-size="10">编码/评审/需求</text>
  <text x="660" y="124" text-anchor="middle" fill="#64748b" font-size="9">不被调研阻塞</text>
  <rect x="40" y="160" width="720" height="55" rx="8" fill="#a855f7" opacity="0.10" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_r2)"/>
  <text x="400" y="182" text-anchor="middle" fill="#d8b4fe" font-size="12" font-weight="700">调研完成后</text>
  <text x="400" y="203" text-anchor="middle" fill="#94a3b8" font-size="10">后台 agent 把发现写进 .harness/wiki/research/ 的 Markdown 文件，主 agent 收到摘要</text>
  <text x="400" y="240" text-anchor="middle" fill="#64748b" font-size="10">"查证"和"做事"是两条并行轨道——这是 /research 与\"搜完再干\"的本质区别</text>
</svg>
```

### 2.2 为什么值得"后台"

有人会质疑：启动一个后台 agent 的成本，比直接查一下还高吧？

关键在**次数**。单次确实略高，但：

- 调研任务通常不是"查一个参数"，而是"确认一个 API 的完整能力边界"——这本身就要读好几页文档，成本不低。
- 主 agent 的暂停成本很高：上下文切换、恢复现场、重新进入状态。让主流程停下来等 10 分钟查证，损失远大于"继续干活"。
- 产物是复用的：一次调研写的 Markdown，全仓库的人（和 agent）都能再读。成本摊到多次使用上，越来越便宜。

**后台委派的收益 = 不阻塞的主流程 + 可复用的产物**。这两者都是"查一下就完"永远给不了的。

### 2.3 任务的输入质量决定调研质量

后台 agent 拿到的调研任务描述，直接决定产出质量。委派时的任务包装要注意三点：

1. **问题要具体**：不是"调研 Go context 的取消机制"，而是"确认 context.WithTimeout 创建的 context 在超时后，其子 context 是否也会同时取消，以及取消传播的文档出处"。问题越具体，agent 越能直奔一手来源。
2. **明确需要的产出**：告诉 agent 要写几个断言、用哪种格式。比如"每个 API 事实给出：能力、参数签名、版本要求、官方文档链接"。
3. **指定来源类型**：明确"只接受官方文档/源码，不接受博客转述"。

## 三、落点：调研结果进仓库

### 3.1 标准落点与惰性创建

`/research` 的调研结果有固定落点：`.harness/wiki/research/` 下，按日期命名：

```
.harness/wiki/research/YYYY-MM-DD-<topic>.md
```

例如 `2026-04-12-postgres-on-conflict-partial-index.md`。

若无 `.harness/wiki/research/` 目录，**惰性创建**——第一次调研需要时创建一个。这和 harness 整体的"不做事则不建物"哲学一致：目录存在 = 内容存在 = 有人在调研。

### 3.2 为什么落进仓库

把调研结果放进仓库而不是聊天记录/个人笔记，有三个理由：

1. **共享**：全团队都能读。别人遇到同一个问题，`grep research/` 就找到了，不用重新查。
2. **版本化**：调研结论跟着代码走。代码升级后如果 API 行为变了，调研文档的日期和内容能告诉你"当时是基于哪个版本查的"。
3. **追责**：每个断言有来源引用。将来结论被质疑（"这个 API 根本不是这么用的"），可以回到调研文档核实"当时依据是什么"。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 240" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_r3" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_r3"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="240" fill="url(#bg_r3)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">为什么落进仓库：三个理由</text>
  <rect x="40" y="52" width="230" height="120" rx="10" fill="#3b82f6" opacity="0.10" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_r3)"/>
  <text x="155" y="76" text-anchor="middle" fill="#93c5fd" font-size="13" font-weight="700">① 共享</text>
  <text x="155" y="102" text-anchor="middle" fill="#94a3b8" font-size="10">全团队可读可 grep</text>
  <text x="155" y="122" text-anchor="middle" fill="#94a3b8" font-size="10">同一问题不再重复查</text>
  <text x="155" y="148" text-anchor="middle" fill="#64748b" font-size="9">调研结论 = 团队公共知识</text>
  <rect x="290" y="52" width="230" height="120" rx="10" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_r3)"/>
  <text x="405" y="76" text-anchor="middle" fill="#fde68a" font-size="13" font-weight="700">② 版本化</text>
  <text x="405" y="102" text-anchor="middle" fill="#94a3b8" font-size="10">结论跟着代码走</text>
  <text x="405" y="122" text-anchor="middle" fill="#94a3b8" font-size="10">日期标注\"基于哪版\"</text>
  <text x="405" y="148" text-anchor="middle" fill="#64748b" font-size="9">API 变了能追溯差异</text>
  <rect x="540" y="52" width="220" height="120" rx="10" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_r3)"/>
  <text x="650" y="76" text-anchor="middle" fill="#86efac" font-size="13" font-weight="700">③ 追责</text>
  <text x="650" y="102" text-anchor="middle" fill="#94a3b8" font-size="10">每个断言有来源引用</text>
  <text x="650" y="122" text-anchor="middle" fill="#94a3b8" font-size="10">质疑时可回到依据</text>
  <text x="650" y="148" text-anchor="middle" fill="#64748b" font-size="9">结论可被验证、可被推翻</text>
  <text x="400" y="202" text-anchor="middle" fill="#475569" font-size="11">落进仓库的调研 = 资产；留在对话里的调研 = 过眼云烟</text>
  <text x="400" y="224" text-anchor="middle" fill="#64748b" font-size="10">如果结论不值得进仓库，说明这个问题不值得调研</text>
</svg>
```

### 3.3 匹配现有约定

落点还要注意：**匹配仓库里已有的此类笔记位置，遵循现有约定**。如果仓库已经有 `docs/research/` 或 `.harness/wiki/知识库/` 之类的约定，就放进约定位置而不是强行建新目录。若无约定、也不存在合理路径，才用默认位置并**说明放哪了**——让读者（和将来的 agent）不用猜。

## 四、调研的"一手来源"实践

### 4.1 什么算一手来源

一手来源不是"能找到的最原始资料"，而是"拥有该事实权威性的来源"。判断标准：**断言的主张者是谁，就到谁那里去核实**。

| 要确认的事实 | 一手来源 | 二手转述（拒绝） |
|-------------|---------|----------------|
| Go context 的取消语义 | Go 官方文档 + 源码 | 博客《Go context 详解》 |
| 某 API 的参数限制 | 该 API 官方文档 | Stack Overflow 回答 |
| 某库的版本兼容要求 | 该库 CHANGELOG / release notes | 第三方对比文章 |
| 某规范的条款含义 | 规范原文 | 解读该规范的教程 |

判断"谁拥有一手事实"有个简单办法：**如果这个来源错了，谁会被打脸**。官方文档里写错 context 语义，Go 团队会被纠正；博客里写错，博主可能根本不知道。事实的权威性天然属于它的"发布者"，不属于"评论者"。

### 4.2 引用的粒度：断言级

调研文档里的引用是**断言级**的：每个关键断言后面跟它的来源。不是整篇末尾甩一堆链接，而是"这句话出自这个来源"。

```
## context.WithTimeout 的取消传播

- **断言**：WithTimeout 创建的 context 在超时后，其所有子 context 同时收到取消信号。
- **来源**：Go 官方文档 context 包（https://pkg.go.dev/context），"WithCancel" 段落；源码 cancelCtx.Done() 实现（go/src/context/context.go）。
```

断言级引用的价值：读者可以**逐条验证**。如果发现某条断言错了，可以精确地指出"第 3 条断言依据不足"，而不是笼统地说"这篇调研不靠谱"。

### 4.3 版本与环境的标注

一手来源在**特定版本**下才成立。调研文档必须标注：

- **查证的版本**：Go 1.21？Postgres 15？
- **环境前提**：Linux？集群配置？
- **查证日期**：2026-04-12（写在文件名里）。

这三点保证了结论的**有效期可见**：读者看到"基于 Go 1.21 查证"，就知道 Go 1.25 环境下需要复核。版本不对的调研结论，是披着信任外衣的误导。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 230" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_r4" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_r4"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="230" fill="url(#bg_r4)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">一手来源的"谁有打脸权"判断法</text>
  <rect x="60" y="52" width="330" height="110" rx="10" fill="#ef4444" opacity="0.08" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_r4)"/>
  <text x="225" y="76" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">二手转述：错了没人负责</text>
  <text x="225" y="102" text-anchor="middle" fill="#94a3b8" font-size="10">博客写错 context 语义 → 无人被打脸</text>
  <text x="225" y="122" text-anchor="middle" fill="#94a3b8" font-size="10">信息失去"责任主体"</text>
  <text x="225" y="144" text-anchor="middle" fill="#64748b" font-size="9">你只能选择信或不信</text>
  <rect x="420" y="52" width="320" height="110" rx="10" fill="#22c55e" opacity="0.08" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_r4)"/>
  <text x="580" y="76" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">一手来源：写错会被纠正</text>
  <text x="580" y="102" text-anchor="middle" fill="#94a3b8" font-size="10">官方文档写错 → Go 团队被打脸</text>
  <text x="580" y="122" text-anchor="middle" fill="#94a3b8" font-size="10">事实与"发布者"绑定</text>
  <text x="580" y="144" text-anchor="middle" fill="#64748b" font-size="9">可验证、可回溯、可纠正</text>
  <text x="400" y="194" text-anchor="middle" fill="#475569" font-size="11">权威性天然属于"发布者"，不属于"评论者"</text>
  <text x="400" y="216" text-anchor="middle" fill="#64748b" font-size="10">断言级引用 + 版本标注 → 结论的信任是可以逐条审计的</text>
</svg>
```

## 五、与流水线技能的联动

### 5.1 三个典型触发场景

`/research` 在流水线的三个关键时刻被调用：

| 触发场景 | 动作 |
|---------|------|
| `harnessing` 需要确认外部 API/库是否支持某能力 | 调用本技能查证 |
| `coding-skill` 不确定某依赖的正确用法 | 调用本技能查一手文档 |
| `expert-reviewer` 需要确认某规范条款的权威解释 | 调用本技能查证 |

三个场景的共同点：**都是"即将基于某个外部事实做决策"**。harnessing 要确认 API 能力才能定方案；coding 要确认依赖用法才能写代码；reviewer 要确认规范条款才能下结论。决策依赖事实，事实必须查证。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 240" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_r5" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_r5"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="240" fill="url(#bg_r5)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">三个触发场景：决策前的查证</text>
  <rect x="40" y="52" width="230" height="110" rx="10" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_r5)"/>
  <text x="155" y="76" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">harnessing</text>
  <text x="155" y="102" text-anchor="middle" fill="#94a3b8" font-size="10">外部 API 是否支持某能力</text>
  <text x="155" y="124" text-anchor="middle" fill="#64748b" font-size="9">定方案前先确认可行性</text>
  <rect x="290" y="52" width="230" height="110" rx="10" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_r5)"/>
  <text x="405" y="76" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">coding-skill</text>
  <text x="405" y="102" text-anchor="middle" fill="#94a3b8" font-size="10">依赖的正确用法</text>
  <text x="405" y="124" text-anchor="middle" fill="#64748b" font-size="9">写代码前查一手文档</text>
  <rect x="540" y="52" width="220" height="110" rx="10" fill="#3b82f6" opacity="0.10" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_r5)"/>
  <text x="650" y="76" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">expert-reviewer</text>
  <text x="650" y="102" text-anchor="middle" fill="#94a3b8" font-size="10">规范条款的权威解释</text>
  <text x="650" y="124" text-anchor="middle" fill="#64748b" font-size="9">下结论前确认依据</text>
  <text x="400" y="196" text-anchor="middle" fill="#475569" font-size="11">共同点：都是"即将基于外部事实做决策"——决策依赖事实，事实必须查证</text>
  <text x="400" y="220" text-anchor="middle" fill="#64748b" font-size="10">查证是决策的"前置输入"，不是"事后补充"</text>
</svg>
```

### 5.2 查证结果怎么回流

调研完成后，结果沿两条路回流：

1. **摘要回主 agent**：后台 agent 把核心结论压缩成摘要交给主 agent，主 agent 基于摘要做决策。摘要里通常含"关键断言 + 来源链接"，主 agent 不必重读全文。
2. **全文进仓库**：完整调研文档落到 `.harness/wiki/research/`。后续任何技能（包括人）需要时直接读文件，不必重新调研。

这两条路配合，保证了"决策有依据、依据可复用"——主 agent 的决策不是拍脑袋，后续的复用不是重新查。

## 六、完成标志

`/research` 的完成标志有三条：

1. **调研结果已写入 `.harness/wiki/research/` 下的 Markdown**——不是停留在对话里，是文件真实存在。
2. **每个断言有来源引用**——不是"我记得官方文档说过"，是每个断言后面对应一个可回溯的一手来源。
3. **主 agent 已收到摘要**——不只是后台查完了，是结论真正回到了决策者手里。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 200" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_r6" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_r6"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="200" fill="url(#bg_r6)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">三个完成标志</text>
  <rect x="40" y="52" width="230" height="90" rx="10" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_r6)"/>
  <text x="155" y="76" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">① 已落盘</text>
  <text x="155" y="100" text-anchor="middle" fill="#94a3b8" font-size="10">research/YYYY-MM-DD-topic.md</text>
  <text x="155" y="120" text-anchor="middle" fill="#64748b" font-size="9">惰性创建，匹配现有约定</text>
  <rect x="290" y="52" width="230" height="90" rx="10" fill="#3b82f6" opacity="0.10" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_r6)"/>
  <text x="405" y="76" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">② 断言有来源</text>
  <text x="405" y="100" text-anchor="middle" fill="#94a3b8" font-size="10">每条断言可回溯一手来源</text>
  <text x="405" y="120" text-anchor="middle" fill="#64748b" font-size="9">版本 + 日期 + 链接</text>
  <rect x="540" y="52" width="220" height="90" rx="10" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_r6)"/>
  <text x="650" y="76" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">③ 摘要回流</text>
  <text x="650" y="100" text-anchor="middle" fill="#94a3b8" font-size="10">主 agent 已收到摘要</text>
  <text x="650" y="120" text-anchor="middle" fill="#64748b" font-size="9">决策者拿到了结论</text>
  <text x="400" y="176" text-anchor="middle" fill="#64748b" font-size="10">三条缺一不可：没落盘 = 白查；没引用 = 不可信；没回流 = 白查给谁看？</text>
</svg>
```

三条标志的设计刻意闭合了调研的价值回路：**查证（后台）→ 落盘（资产）→ 回流（决策）**。断掉任何一环，调研就退化成"查了一下"。

## 七、与相邻能力的边界

### 7.1 vs 普通对话问答

主 agent 在对话里直接回答问题，可能依赖内部知识或二手信息；`/research` 必须查证一手来源并落盘。区别不在"是否联网"，而在**"断言是否有可回溯依据、结论是否进仓库"**。

### 7.2 vs 需求调研

需求调研（用户调研、竞品调研）问的是"人想要什么"；`/research` 问的是"客观事实是什么"。前者没有"一手来源"概念（人的需求就是它自己），后者严格依赖一手来源。落点也不同：需求调研结果进需求文档，`/research` 结果进 `.harness/wiki/research/`。

### 7.3 vs handoff

`handoff` 是会话级交接：压缩对话上下文给下一个会话。`/research` 是知识级沉淀：把外部事实查证落成项目资产。handoff 面向"续接"，research 面向"复用"。

## 八、写在最后

`/research` 的全部设计浓缩成三句话：

1. **查证不阻塞做事**——后台 agent 跑调研，主 agent 继续干活，两条轨道并行。
2. **事实只信一手**——决策建立在一手来源上，每个断言可回溯、可审计、可纠正。
3. **结论必须落盘**——调研的价值不在"查过了"，而在"查完的东西别人还能用"。

一句话记住它：**/research 是把"查一下"升级成"查证并沉淀"的引擎——它不是消灭提问，而是让同样的提问只发生一次。**