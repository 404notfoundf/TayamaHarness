# /harness-status：项目的“进度仪表盘”

> 命令深度拆解 · 第 26 篇 · 约 8500 字 · 7 个 SVG 图

---

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 300" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_s0" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_s0"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="300" fill="url(#bg_s0)" rx="10"/>
  <text x="400" y="28" text-anchor="middle" fill="#e2e8f0" font-size="26" font-weight="700">/harness-status：项目的“进度仪表盘”</text>
  <rect x="60" y="55" width="320" height="95" rx="10" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_s0)"/>
  <text x="220" y="80" text-anchor="middle" fill="#93c5fd" font-size="14" font-weight="700">只读聚合总览</text>
  <text x="220" y="104" text-anchor="middle" fill="#94a3b8" font-size="11">扫描 .harness/changes/*/change.md</text>
  <text x="220" y="126" text-anchor="middle" fill="#64748b" font-size="10">绝不修改任何 change 文件</text>
  <rect x="420" y="55" width="320" height="95" rx="10" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_s0)"/>
  <text x="580" y="80" text-anchor="middle" fill="#86efac" font-size="14" font-weight="700">回答三个问题</text>
  <text x="580" y="104" text-anchor="middle" fill="#94a3b8" font-size="11">做到哪了 · 哪些卡着 · 下一步做什么</text>
  <text x="580" y="126" text-anchor="middle" fill="#64748b" font-size="10">总览表 + 阶段分布 + 待办建议</text>
  <text x="400" y="185" text-anchor="middle" fill="#475569" font-size="13">三块输出，各司其职</text>
  <rect x="70" y="205" width="200" height="45" rx="8" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_s0)"/>
  <text x="170" y="234" text-anchor="middle" fill="#fde68a" font-size="12">📊 总览表（逐个 change）</text>
  <rect x="300" y="205" width="200" height="45" rx="8" fill="#a855f7" opacity="0.10" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_s0)"/>
  <text x="400" y="234" text-anchor="middle" fill="#d8b4fe" font-size="12">阶段分布（条形图）</text>
  <rect x="530" y="205" width="200" height="45" rx="8" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_s0)"/>
  <text x="630" y="234" text-anchor="middle" fill="#86efac" font-size="12">待办建议（下一步命令）</text>
  <text x="400" y="280" text-anchor="middle" fill="#64748b" font-size="10">“现在做到哪了” → 一秒钟回答——这是仪表盘存在的全部理由</text>
</svg>
```

## 一、/harness-status 解决的是什么问题

### 1.1 多 change 并行时的“进度黑箱”

当项目只有一个 change 时，大家都很清楚进度：做到哪了、卡在哪，聊两句就知道了。

但 Harness 流水线的现实是**多 change 并行**：需求分析中的、编码中的、测试中的、评审中的、等部署的、已经完成的，同时存在。这时候“项目进度”成了一个黑箱：

- “C-003 现在怎么样了？”——要打开 change.md 看 status 字段；
- “有哪些 change 卡着没动？”——要翻所有 change 文件才知道；
- “下一步该做什么？”——要知道每个阶段对应的命令；
- “有没有异常的？”——要逐个解析 frontmatter 才发现哪个坏了。

`/harness-status` 把这一切压缩成一个只读命令：扫码、聚合、分类、输出。它回答三个问题：**做到哪了、哪些被卡着、下一步该做什么。**

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 240" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_s1" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_s1"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="240" fill="url(#bg_s1)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">进度黑箱 vs 进度仪表盘</text>
  <rect x="40" y="50" width="350" height="130" rx="10" fill="#ef4444" opacity="0.08" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_s1)"/>
  <text x="215" y="74" text-anchor="middle" fill="#fca5a5" font-size="13" font-weight="700">没有 status：进度黑箱</text>
  <text x="215" y="102" text-anchor="middle" fill="#94a3b8" font-size="10">C-003 怎么样了？→ 逐个翻文件</text>
  <text x="215" y="124" text-anchor="middle" fill="#94a3b8" font-size="10">哪些卡着？→ 不知道</text>
  <text x="215" y="146" text-anchor="middle" fill="#94a3b8" font-size="10">下一步做什么？→ 靠记忆</text>
  <text x="215" y="170" text-anchor="middle" fill="#64748b" font-size="9">信息分散，全靠人肉聚合</text>
  <rect x="420" y="50" width="340" height="130" rx="10" fill="#22c55e" opacity="0.08" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_s1)"/>
  <text x="590" y="74" text-anchor="middle" fill="#86efac" font-size="13" font-weight="700">有 status：进度仪表盘</text>
  <text x="590" y="102" text-anchor="middle" fill="#94a3b8" font-size="10">一条命令 → 总览表 + 条形图</text>
  <text x="590" y="124" text-anchor="middle" fill="#94a3b8" font-size="10">卡住的、异常的、孤立的全部显形</text>
  <text x="590" y="146" text-anchor="middle" fill="#94a3b8" font-size="10">每个 change 的下一步命令直接给出</text>
  <text x="590" y="170" text-anchor="middle" fill="#64748b" font-size="9">信息聚合，一秒钟看全貌</text>
  <text x="400" y="210" text-anchor="middle" fill="#475569" font-size="11">仪表盘的价值 = 把“翻 20 个文件才知道”变成“一秒钟看到全部”</text>
  <text x="400" y="230" text-anchor="middle" fill="#64748b" font-size="10">它不产生新信息，它让已有信息可见——可见性是治理的第一步</text>
</svg>
```

### 1.2 为什么必须是“只读”

`/harness-status` 的定位里最关键的一个词是**只读**：

> 这是只读的聚合总览技能，不修改任何 change 状态。状态迁移由流水线技能（coding-skill / unit-test-write / expert-reviewer / unit-test-ci / deploy-verify）负责。

这个设计是刻意的。为什么？

1. **职责单一**：状态迁移是"生产"动作，由各阶段技能在执行完自己的工作后写入；status 是"消费"动作，只负责读出来展示。生产者与消费者分离，状态才不会被到处乱改。
2. **状态只有一个真相源**：change.md 的 frontmatter。如果 status 又读又写，某个"顺手"的改动就可能让状态和实际不符——比如把 `coding` 改成了 `done`，但代码根本没写完。
3. **只读命令可以被安全地反复执行**：随时跑、跑多少次都无害。这让它适合作为"状态检查"的例行动作——就像仪表盘，看一百次也不会改变发动机。

### 1.3 与流水线的关系：总览是"眼"，流水线是"手"

把 `/harness-status` 放进整体看：它提供**眼睛**（现在在哪），流水线技能提供**手**（把它推进到下一阶段）。

- 眼睛看：C-005 在 `reviewing`。
- 手做：`/expert-reviewer C-005`，通过后 status → `ci`。
- 眼睛再看：C-005 在 `ci`，下一步 `/unit-test-ci`。

眼睛不干预手，手不需要眼睛提醒才知道下一步——但**没有眼睛，手就不知道此刻该抓哪件事**。状态总览的价值是"调度决策"：在所有进行中的 change 里，先推进哪个、哪个被卡着需要人介入。

## 二、执行流程：扫描 → 解析 → 分类

### 2.1 Step 1：扫描变更卡

`/harness-status` 的第一步是扫描 `.harness/changes/*/change.md` 下的所有变更卡：

- 列出所有 change.md 文件；
- 跳过 `_relations.json`、`_graph.md`、`TECH-DEBT.md` 等非变更卡文件（这些是索引与附属产物，不是 change 本身）；
- 若无任何 change.md → 输出：`📭 尚无变更记录。运行 /harnessing 创建第一个需求变更。`

这个"空状态"输出很重要——它不只是说"没有文件"，而是给出了**下一步动作**：用 `/harnessing` 创建第一个 change。空仓库不是终点，是起点。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 220" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_s2" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_s2"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="220" fill="url(#bg_s2)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">扫描范围：哪些算 change，哪些不算</text>
  <rect x="40" y="52" width="350" height="100" rx="10" fill="#22c55e" opacity="0.08" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_s2)"/>
  <text x="215" y="76" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">✅ 扫描</text>
  <text x="215" y="102" text-anchor="middle" fill="#94a3b8" font-size="10">.harness/changes/*/change.md</text>
  <text x="215" y="124" text-anchor="middle" fill="#64748b" font-size="9">每个 change 一张卡，frontmatter 里的 status 是真相源</text>
  <rect x="420" y="52" width="340" height="100" rx="10" fill="#ef4444" opacity="0.08" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_s2)"/>
  <text x="590" y="76" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">✗ 跳过</text>
  <text x="590" y="102" text-anchor="middle" fill="#94a3b8" font-size="10">_relations.json · _graph.md · TECH-DEBT.md</text>
  <text x="590" y="124" text-anchor="middle" fill="#64748b" font-size="9">索引与附属产物，不是 change 本体</text>
  <text x="400" y="178" text-anchor="middle" fill="#475569" font-size="11">空仓库的输出不是“无”，是“下一步”：运行 /harnessing 创建第一个需求变更</text>
  <text x="400" y="202" text-anchor="middle" fill="#64748b" font-size="10">空状态 = 起点，不是死胡同——总览永远给出行动方向</text>
</svg>
```

### 2.2 Step 2：解析与分类

逐个解析每个 change.md 的 frontmatter，提取 `status:` 字段与 `slug:`，然后按 status 分组：

| 分组 | status 值 |
|------|----------|
| 🚧 进行中 | `analyzing` / `coding` / `testing` / `reviewing` / `ci` / `verifying` |
| ⏸ 草稿 | `draft` |
| ✅ 已完成 | `done` |
| ❌ 异常 | 无法解析 JSON/frontmatter，或 status 值未知 |

这个分组的巧妙在于**用一个统一的"红黄绿"心智模型**把 9 种状态收敛成 4 类：在跑的（进行中）、没启动的（草稿）、跑完的（已完成）、坏了的（异常）。

- 进行中 = 需要盯的；
- 草稿 = 需要启动或清理的；
- 已完成 = 归档可查的；
- 异常 = 需要立刻处理的（解析失败 ≠ 忽略——下面会讲）。

### 2.3 Step 3：异常必须显式列出，绝不静默跳过

`/harness-status` 对异常的处理有一条硬纪律：**无法解析的 change 不能静默跳过**。

- 某 change.md 无法解析 → 列入 ❌ 异常，并输出文件路径与解析错误；
- status 值未知（不在列表内）→ 列入 ❌ 异常，标注"未知状态: <值>"。

为什么禁止静默跳过？因为在治理语境里，**静默 = 缺席**。一个解析失败的 change.md 意味着：它没有进入任何统计、没有待办建议、永远不被推进——它成了"进度黑洞"。你若跳过它，它就永远消失；你若列出它，至少有人知道"有个 change 坏了，需要修"。

异常处理还有一个隐性收益：**它是 change 卡格式正确性的哨兵**。如果 status 总是能正常解析，说明流水线技能写 frontmatter 的约定被执行得很好；如果频繁出现未知状态，说明有技能在写非标准值——这本身就是治理问题。

## 三、三块输出：总览表、阶段分布、待办建议

### 3.1 总览表：逐个 change 的"点名册"

第一块输出是总览表，格式是机器可读的一行一个 change：

```
📊 变更总览：N 个（进行中 X · 草稿 Y · 已完成 Z · 异常 W）
C-001 <slug> — 🚧 <状态徽标> <标题首句>
C-002 <slug> — ✅ done <标题首句>
```

要点：

- **按 id 升序**排列——稳定的顺序让"上次看到 C-004 在哪、这次看到它到哪了"成为可能；
- 每个 change 都带状态徽标——一眼区分分类；
- 只显示标题首句——总览不是细节，细节在各 change.md 里。

总览表回答的是"**有哪些 change 存在、各在什么状态**"。

### 3.2 阶段分布：条形图里的"瓶颈显影"

第二块输出是阶段分布，用条形图直观展示各阶段数量：

```
阶段分布：
🚧 analyzing  ██▁▁▁▁▁▁ 2
🚧 coding     ▁▁▁▁▁▁▁▁ 0
🚧 testing    ██████▁▁ 6
🚧 reviewing  ████▁▁▁▁ 4
🚧 ci         ▁▁▁▁▁▁▁▁ 0
🚧 verifying  █▁▁▁▁▁▁▁ 1
⏸ draft       ██▁▁▁▁▁▁ 2
✅ done       ████▁▁▁▁ 4
```

条形长度 ∝ 数量。它的价值不是"好看"，而是**瓶颈显影**：当某个阶段堆积了大量 change，它就露馅了——

- `testing` 堆积 6 个 → 测试环节是瓶颈，也许测试命令太慢、也许单测写不出来；
- `draft` 堆积 → 一堆需求没启动，可能有需求分析卡住了；
- `reviewing` 堆积 → 评审环节排队，也许评审太严或人不够。

阶段分布在总览表的"点名单"之上叠了一层"**队列视图**"：不只看单个 change，看整条流水线的吞吐均衡。这是调度型治理的核心视野。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 280" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_s3" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_s3"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="280" fill="url(#bg_s3)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">阶段分布：瓶颈显影</text>
  <text x="120" y="60" text-anchor="middle" fill="#94a3b8" font-size="11">analyzing</text>
  <rect x="200" y="50" width="40" height="16" fill="#f59e0b" opacity="0.6"/>
  <text x="620" y="63" text-anchor="middle" fill="#64748b" font-size="10">1 个</text>
  <text x="120" y="92" text-anchor="middle" fill="#94a3b8" font-size="11">coding</text>
  <rect x="200" y="82" width="40" height="16" fill="#f59e0b" opacity="0.6"/>
  <text x="620" y="95" text-anchor="middle" fill="#64748b" font-size="10">1 个</text>
  <text x="120" y="124" text-anchor="middle" fill="#fca5a5" font-size="11" font-weight="700">testing ⚠</text>
  <rect x="200" y="114" width="230" height="16" fill="#ef4444" opacity="0.7"/>
  <text x="620" y="127" text-anchor="middle" fill="#fca5a5" font-size="10">6 个 ← 瓶颈</text>
  <text x="120" y="156" text-anchor="middle" fill="#94a3b8" font-size="11">reviewing</text>
  <rect x="200" y="146" width="160" height="16" fill="#a855f7" opacity="0.6"/>
  <text x="620" y="159" text-anchor="middle" fill="#64748b" font-size="10">4 个</text>
  <text x="120" y="188" text-anchor="middle" fill="#94a3b8" font-size="11">ci</text>
  <rect x="200" y="178" width="20" height="16" fill="#3b82f6" opacity="0.6"/>
  <text x="620" y="191" text-anchor="middle" fill="#64748b" font-size="10">0 个</text>
  <text x="400" y="228" text-anchor="middle" fill="#475569" font-size="11">队列堆积在哪，瓶颈就在哪——条形图让问题区域自己"显影"</text>
  <text x="400" y="252" text-anchor="middle" fill="#64748b" font-size="10">testing 堆积 6 个 → 该查：测试命令太慢？单测写不出？还是 gate 过严？</text>
  <text x="400" y="272" text-anchor="middle" fill="#64748b" font-size="10">总览表看"点"，阶段分布看"队列"——两个视野叠起来才是调度视图</text>
</svg>
```

### 3.3 待办建议：每个 change 的"下一步命令"

第三块输出是待办建议——对每个进行中的 change，输出下一步应执行的命令：

| 当前 status | 下一步 | 建议命令 |
|-------------|--------|---------|
| `draft` | 需求未启动 | `/harnessing` 细化或删除 |
| `analyzing` | 需求卡待审批 | 提示人类审批后置为 `coding` |
| `coding` | 编码实现 | `/coding-skill <id>` |
| `testing` | 单测编写 | `/unit-test-write <id>` |
| `reviewing` | 专家评审 | `/expert-reviewer <id>` |
| `ci` | CI 门禁 | `/unit-test-ci <id>` |
| `verifying` | 部署验证 | `/deploy-verify <id>` |

输出格式：

```
C-003 → /coding-skill C-003  (订单状态机拆分)
C-005 → /unit-test-write C-005  (退款幂等)
C-007 → /expert-reviewer C-007  (消息重试改造)
```

这一块的妙处在于**它把"状态"翻译成了"动作"**。状态值本身（`coding`）是存量信息——它只说"现在在编码"；待办建议是增量信息——它说"你要执行 `/coding-skill C-003`"。对使用者（人或者外层调度脚本）来说，动作比状态有用得多：**状态告诉你"在哪"，待办告诉你"去哪"**。

还有一个细节：`analyzing` 状态的建议不是命令，而是"**提示人类审批后置为 coding**"——因为需求审批是人类决策，不是技能动作。总览在这里诚实地区分了"技能能推进的"与"必须人介入的"。

## 四、与相邻技能的分工

### 4.1 status 与 loop-run：一个看，一个跑

- `/harness-status` 是**只读总览**：看全貌、给建议，不推进任何 change。
- `/harness-loop-run` 是**执行引擎**：选定一个 change，自动跑完机械收敛段（coding → testing → reviewing）。

它们互补：status 负责"选哪个 change 推进"（调度决策），loop-run 负责"把选中的 change 推进"（执行）。status 看到 C-005 在 `testing`，接下来人可以选择丢给 loop-run 让它自动跑到 `ci`。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 220" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_s4" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_s4"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="220" fill="url(#bg_s4)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">status（看）与 loop-run（跑）</text>
  <rect x="40" y="50" width="330" height="110" rx="10" fill="#3b82f6" opacity="0.10" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_s4)"/>
  <text x="205" y="76" text-anchor="middle" fill="#93c5fd" font-size="13" font-weight="700">/harness-status</text>
  <text x="205" y="102" text-anchor="middle" fill="#94a3b8" font-size="10">只读 · 聚合 · 建议</text>
  <text x="205" y="124" text-anchor="middle" fill="#94a3b8" font-size="10">看全貌，选 next</text>
  <text x="205" y="146" text-anchor="middle" fill="#64748b" font-size="9">调度决策</text>
  <rect x="420" y="50" width="340" height="110" rx="10" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_s4)"/>
  <text x="590" y="76" text-anchor="middle" fill="#86efac" font-size="13" font-weight="700">/harness-loop-run</text>
  <text x="590" y="102" text-anchor="middle" fill="#94a3b8" font-size="10">执行 · 收敛 · 停机</text>
  <text x="590" y="124" text-anchor="middle" fill="#94a3b8" font-size="10">选定一个 change 自动推进</text>
  <text x="590" y="146" text-anchor="middle" fill="#64748b" font-size="9">执行推进</text>
  <text x="400" y="185" text-anchor="middle" fill="#475569" font-size="11">status 回答"推进哪个"，loop-run 完成"把它推进"——一选一做</text>
  <text x="400" y="207" text-anchor="middle" fill="#64748b" font-size="10">两者都不触碰 ci 之后的发布链路，那里永远留人</text>
</svg>
```

### 4.2 status 与流水线技能：状态迁移的"所有者"

`/harness-status` 明确不负责状态迁移——状态迁移的"所有者"是各阶段技能：

- `coding-skill` 完成 → status: `coding` → `testing`
- `unit-test-write` 完成 → `testing` → `reviewing`
- `expert-reviewer` 通过 → `reviewing` → `ci`
- `deploy-verify` 通过 → `verifying` → `done`

这个"所有权限定"是刻意为之：如果 status 也改、阶段技能也改，就会出现状态互相踩踏。只有**推进者写状态**，状态才能始终反映"上一个技能实际干完了什么"。

### 4.3 status 与 relate：进度视图与关系视图

- `harness-status` 看"**每个 change 到哪了**"（纵向：沿流水线的位置）；
- `harness-relate` 看"**change 之间怎么牵连**"（横向：change 之间的依赖/冲突）。

`status` 回答"有没有卡着"，`relate` 回答"改了 A 会不会崩 B"。调度时两个视图都要：先 status 看谁 ready，再 relate 看动了它会不会牵连别人。这也是 `harnessing` 分析影响面时调用 `impact` 的原因——**新需求不是孤岛，它落在关系网上**。

## 五、完成标志：怎样算"看完了"

`/harness-status` 的完成标志有三条，全部满足即算完成：

1. **已输出总览表 + 阶段分布 + 待办建议**——三块输出缺一不可。
2. **异常 change 已显式列出**（若有）——坏文件不静默消失。
3. **未修改任何 change 文件**——只读承诺兑现。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 210" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_s5" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_s5"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="210" fill="url(#bg_s5)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">完成标志：三块都出，一处没改</text>
  <rect x="40" y="52" width="230" height="90" rx="10" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_s5)"/>
  <text x="155" y="76" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">① 三块输出齐全</text>
  <text x="155" y="100" text-anchor="middle" fill="#94a3b8" font-size="10">总览表+分布+待办</text>
  <text x="155" y="120" text-anchor="middle" fill="#64748b" font-size="9">缺一块都不算完</text>
  <rect x="290" y="52" width="230" height="90" rx="10" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_s5)"/>
  <text x="405" y="76" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">② 异常显式列出</text>
  <text x="405" y="100" text-anchor="middle" fill="#94a3b8" font-size="10">坏文件不静默消失</text>
  <text x="405" y="120" text-anchor="middle" fill="#64748b" font-size="9">标注路径+解析错误</text>
  <rect x="540" y="52" width="220" height="90" rx="10" fill="#3b82f6" opacity="0.10" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_s5)"/>
  <text x="650" y="76" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">③ 未改任何 change</text>
  <text x="650" y="100" text-anchor="middle" fill="#94a3b8" font-size="10">只读承诺兑现</text>
  <text x="650" y="120" text-anchor="middle" fill="#64748b" font-size="9">仪盘表不改发动机</text>
  <text x="400" y="176" text-anchor="middle" fill="#475569" font-size="11">只读是所有治理视图的前提——能看不能改，才能放心地反复看</text>
  <text x="400" y="196" text-anchor="middle" fill="#64748b" font-size="10">完成标志全是可以机械验证的事实</text>
</svg>
```

## 六、写在最后

`/harness-status` 的全部设计，浓缩成四句话：

1. **只读是身份**——总览不产生新信息，它让已有信息可见；可反复执行而无副作用。
2. **三块输出各司其职**——总览表看点名，阶段分布看瓶颈，待办建议看下一步。
3. **状态是真相源**——推进者写状态，总览只读状态；异常的 change 显式列、不静默。
4. **兼容系统的人与机器**——输出既给人看（条形图、徽标），也给脚本解析（一行一个、待办命令可执行）。

一句话记住它：**/harness-status 是项目的仪表盘——它永远只看不动，把"现在做到哪了、卡在哪、下一步做什么"一秒钟摆在你面前。**