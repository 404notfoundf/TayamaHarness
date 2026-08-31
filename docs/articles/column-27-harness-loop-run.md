# /harness-loop-run：让收敛段自己跑起来

> 命令深度拆解 · 第 27 篇 · 约 9500 字 · 8 个 SVG 图

---

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 300" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_l0" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_l0"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="300" fill="url(#bg_l0)" rx="10"/>
  <text x="400" y="28" text-anchor="middle" fill="#e2e8f0" font-size="26" font-weight="700">/harness-loop-run：让收敛段自己跑起来</text>
  <rect x="60" y="55" width="320" height="95" rx="10" fill="#a855f7" opacity="0.12" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_l0)"/>
  <text x="220" y="80" text-anchor="middle" fill="#d8b4fe" font-size="14" font-weight="700">自主收敛循环引擎</text>
  <text x="220" y="104" text-anchor="middle" fill="#94a3b8" font-size="11">coding → testing → reviewing 自动推进</text>
  <text x="220" y="126" text-anchor="middle" fill="#64748b" font-size="10">从当前状态出发，直到评审通过或被 blocked</text>
  <rect x="420" y="55" width="320" height="95" rx="10" fill="#ef4444" opacity="0.12" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_l0)"/>
  <text x="580" y="80" text-anchor="middle" fill="#fca5a5" font-size="14" font-weight="700">⛔ 终态即 ci</text>
  <text x="580" y="104" text-anchor="middle" fill="#94a3b8" font-size="11">到 ci 即成功停机，交人工</text>
  <text x="580" y="126" text-anchor="middle" fill="#64748b" font-size="10">绝不自动进入 CI / 部署 / 发布</text>
  <text x="400" y="185" text-anchor="middle" fill="#475569" font-size="13">循环三要素：映射表 · 派发纪律 · 停机信号</text>
  <rect x="60" y="205" width="220" height="45" rx="8" fill="#3b82f6" opacity="0.10" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_l0)"/>
  <text x="170" y="234" text-anchor="middle" fill="#93c5fd" font-size="12">状态 → 技能 映射表</text>
  <rect x="300" y="205" width="220" height="45" rx="8" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_l0)"/>
  <text x="410" y="234" text-anchor="middle" fill="#86efac" font-size="12">fresh subagent 派发</text>
  <rect x="540" y="205" width="220" height="45" rx="8" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_l0)"/>
  <text x="650" y="234" text-anchor="middle" fill="#fde68a" font-size="12"><<<HARNESS>>> 停机信号</text>
  <text x="400" y="280" text-anchor="middle" fill="#64748b" font-size="10">核心洞察：把\"机械收敛\"与\"人类判断\"切开——机器跑收敛，人做决策</text>
</svg>
```

## 一、/harness-loop-run 解决的是什么问题

### 1.1 收敛段是"机械劳动"，不该占人时间

一个 change 从需求批准到可以发布，要经过一条流水线。其中有一段特别"机械"：

```
coding（编码实现）→ testing（单测编写）→ reviewing（专家评审）
```

这段路径上每一步的输入输出都是确定的：
- `coding` 状态的 change → 跑 `/coding-skill <id>` → 成功后 status 变成 `testing`；
- `testing` 状态的 change → 跑 `/unit-test-write <id>` → 成功后 status 变成 `reviewing`；
- `reviewing` 状态的 change → 跑 `/expert-reviewer <id>` → 0 严重问题后 status 变成 `ci`。

每一步都是"执行一个技能 → 看状态是否前进 → 前进则进入下一步"。**这个循环毫无智力含量**——它只需要按表索骥。可如果让人类手动执行，每一轮都要：打开 change 看状态 → 想该跑哪个技能 → 跑 → 看结果 → 再打开看状态……一轮五分钟，一个 change 五轮，人类的时间就这么被机械劳动吃掉了。

`/harness-loop-run` 存在的全部理由：**把这段机械收敛烧成自主循环**——人类只需要说一句"把这个 change 跑到底"，引擎就自动推进，直到评审通过（`ci`）或被 blocked 停下交还人类。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 260" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_l1" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_l1"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="260" fill="url(#bg_l1)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">人工逐轮 vs 自主循环</text>
  <rect x="40" y="50" width="350" height="150" rx="10" fill="#ef4444" opacity="0.08" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_l1)"/>
  <text x="215" y="74" text-anchor="middle" fill="#fca5a5" font-size="13" font-weight="700">人工逐轮（机械劳动）</text>
  <text x="215" y="102" text-anchor="middle" fill="#94a3b8" font-size="10">打开 change 看状态</text>
  <text x="215" y="122" text-anchor="middle" fill="#94a3b8" font-size="10">想该跑哪个技能</text>
  <text x="215" y="142" text-anchor="middle" fill="#94a3b8" font-size="10">跑完再看结果</text>
  <text x="215" y="162" text-anchor="middle" fill="#94a3b8" font-size="10">循环 ×N 轮</text>
  <text x="215" y="186" text-anchor="middle" fill="#64748b" font-size="9">一轮五分钟 × 五轮 = 人肉浪费</text>
  <rect x="420" y="50" width="340" height="150" rx="10" fill="#22c55e" opacity="0.08" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_l1)"/>
  <text x="590" y="74" text-anchor="middle" fill="#86efac" font-size="13" font-weight="700">自主循环（引擎代跑）</text>
  <text x="590" y="102" text-anchor="middle" fill="#94a3b8" font-size="10">说一句\"跑到底\"</text>
  <text x="590" y="122" text-anchor="middle" fill="#94a3b8" font-size="10">引擎按映射表自动推进</text>
  <text x="590" y="142" text-anchor="middle" fill="#94a3b8" font-size="10">到 ci 停机 or blocked 交还</text>
  <text x="590" y="162" text-anchor="middle" fill="#94a3b8" font-size="10">人只处理停机信号</text>
  <text x="590" y="186" text-anchor="middle" fill="#64748b" font-size="9">人从执行者变成决策者</text>
  <text x="400" y="228" text-anchor="middle" fill="#475569" font-size="11">收敛段 = 机械劳动；loop-run = 把机械劳动自动化，人只处理例外</text>
  <text x="400" y="250" text-anchor="middle" fill="#64748b" font-size="10">判断哪些要自动跑、哪些必须留人——这正是本技能的设计边界</text>
</svg>
```

### 1.2 边界在哪里：什么能自动，什么必须留人

`/harness-loop-run` 最精妙的设计不是"能自动"，而是**"自动到哪里为止"**。它的边界是刻意画出来的：

**自动范围（机械收敛段）**：`coding` → `testing` → `reviewing`。这三步的输入输出确定、判断标准明确（测试写没写、评审过没过），可以无人值守。

**绝不自动（不可逆·外发操作）**：CI 门禁、部署、发布——这些归属 `/unit-test-ci`、`/deploy-verify`、`/harness-ship`，**永远留人**。原因：

- 它们是不可逆操作（发布出去就收不回来）；
- 它们是外发操作（影响真实用户/生产环境）；
- 它们的失败成本远高于收敛段的任何一步。

**不自动（需求/设计层）**：PRD/设计的关键取舍不在本引擎范围内，归属 `harnessing` 和人工。因为"这个需求到底怎么做"是智力劳动，不是机械劳动。

用一句话概括：**机器跑机械收敛，人做判断决策。** 引擎的终态就是 `ci`——到达 `ci` 即成功停机，把 change 交还人类，由人决定是否继续 `/unit-test-ci` → `/deploy-verify`。这个"到 ci 就停"的设计是本技能的灵魂：**它能自动，但它知道自己的边界在哪。**

## 二、循环算法：从当前状态出发

### 2.1 输入与初始选择

`/harness-loop-run` 的输入是 change `<id>`，可选 `--max-steps`（迭代上限，缺省 **4**）。

- **未指定 `<id>`** → 扫描 `.harness/changes/*/change.md`：
  - `status` 在 `[coding, testing, reviewing]` 中的 change **恰好 1 个** → 自动选中；
  - **0 个** → 报错停机（没有可推进的 change，无从跑起）；
  - **≥2 个** → 停下列出候选，请人类选择（引擎不替人决定先做哪个——调度顺序可以有人为优先级）。
- **指定 `<id>`** → 校验状态在 `[coding, testing, reviewing]`，否则报错停机（不在收敛段的 change 不属于本引擎的管辖范围）。

这个初始选择的处理很讲究：**能自动选就自动选（1 个），不能就交还（≥2 个）**。它既不浪费人类时间（只有一个候选时不问），也不替人做优先级决策（多个候选时必问）。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 300" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_l2" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_l2"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="300" fill="url(#bg_l2)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">初始选择：能自动就自动，不能就问人</text>
  <rect x="40" y="55" width="150" height="70" rx="10" fill="#3b82f6" opacity="0.10" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_l2)"/>
  <text x="115" y="80" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">扫收敛段</text>
  <text x="115" y="104" text-anchor="middle" fill="#94a3b8" font-size="9">coding/testing/reviewing</text>
  <line x1="190" y1="90" x2="220" y2="90" stroke="#475569" stroke-width="2"/>
  <polygon points="226,90 218,85 218,95" fill="#94a3b8"/>
  <rect x="235" y="45" width="150" height="90" rx="10" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_l2)"/>
  <text x="310" y="70" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">恰好 1 个</text>
  <text x="310" y="94" text-anchor="middle" fill="#94a3b8" font-size="9">自动选中</text>
  <text x="310" y="118" text-anchor="middle" fill="#64748b" font-size="9">不问人，直接跑</text>
  <rect x="235" y="160" width="150" height="90" rx="10" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_l2)"/>
  <text x="310" y="185" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">≥2 个</text>
  <text x="310" y="209" text-anchor="middle" fill="#94a3b8" font-size="9">停下列候选</text>
  <text x="310" y="233" text-anchor="middle" fill="#64748b" font-size="9">人挑优先级</text>
  <rect x="600" y="55" width="150" height="70" rx="10" fill="#ef4444" opacity="0.10" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_l2)"/>
  <text x="675" y="80" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">0 个</text>
  <text x="675" y="104" text-anchor="middle" fill="#94a3b8" font-size="9">报错停机</text>
  <text x="400" y="248" text-anchor="middle" fill="#475569" font-size="11">指定 <id> 时：状态必须在收敛段，否则报错——不在管辖范围的 change 不碰</text>
  <text x="400" y="280" text-anchor="middle" fill="#64748b" font-size="10">自动选不是替人决策，是在没有决策空间时省掉提问</text>
</svg>
```

### 2.2 主循环：状态驱动的派发

选定 change 后进入主循环，算法如下：

```
当前 status = change.md frontmatter 的 status 字段
steps = 0
loop:
  if status == ci:           → 成功停机 <<<HARNESS done stage=ci>>>
  if status not in 收敛段:    → blocked 停机（见 §5）
  next_skill = 映射表[status]
  steps += 1
  if steps > max_steps:      → 预算耗尽，停机交还人类
  派发 next_skill → fresh subagent（带 --autonomous）
  重读 change.md 的 status
  if status 未前进（stuck）:  → stuck-stop，停机交还人类
```

核心思想是**状态驱动**：循环体不是"按固定计划执行"，而是每轮读取最新的 status，据此决定下一个动作。状态变了，动作就变——`reviewing` 被打回 `coding` 时，下一轮自动重新执行 `/coding-skill`，这是收敛回路的自然部分。

### 2.3 阶段映射表

| 当前 status | 派发技能 | 成功后新 status |
|-------------|---------|----------------|
| `coding` | `/coding-skill <id>` | `testing` |
| `testing` | `/unit-test-write <id>` | `reviewing` |
| `reviewing` | `/expert-reviewer <id>` | `ci`（0 严重问题） |

这张表是引擎的"宪法"——把三态与三技能一一锁定，执行时没有模糊地带。它同时定义了"什么算前进"：技能执行完，status 变成表里的"成功后新 status"才算前进。

## 三、派发纪律：fresh subagent + 状态真相源

循环引擎的"执行单元"是 subagent——但怎么派发，有严格纪律。

### 3.1 每个 stage 派发给一个 fresh subagent

`/harness-loop-run` 每轮迭代都**派发一个全新的 subagent** 去执行对应技能，而不是让循环主进程自己累积上下文。为什么？

- **上下文隔离**：coding 阶段的代码细节、testing 阶段的测试细节，不该污染后续 review 阶段"冷静看全局"的视角。fresh subagent 每次从零加载所需文件，天然隔离。
- **防止上下文污染累积**：长循环中，主进程的上下文会越堆越厚（每轮都夹带旧的系统输出），既慢又容易让后续判断受早期信息的锚定。fresh subagent 每次只看当前该看的。
- **出错隔离**：某个 stage 的 subagent 把环境搞乱了（装错了包、改了不该改的文件），影响被限制在一个 subagent 内，主循环的调度逻辑不受牵连。

### 3.2 完整上下文指针，而非内容注入

派发 subagent 时，**不把文件内容塞进循环主进程**，而是带上上下文指针：

- `.harness/changes/<id>/change.md`
- 相关 `.harness/wiki/*`
- `.harness/rules/`

让 subagent 自己按需加载。这样主进程保持轻量，subagent 又拿到了完整的领域上下文。

### 3.3 --autonomous：不暂停、不询问

每个 subagent 的任务描述用 `<技能名> <id>`，并声明 `--autonomous`：**不暂停、不询问、只按技能流程执行到出口门禁**。

这是"自主"二字的技术实现：如果每个 stage 都停下来问人"我可以开始吗"，那循环就不是循环，是连问五次的审批流。`--autonomous` 把"沿着技能流程走到底"授权给了 subagent，把暂停权只留给引擎（blocked/stuck/预算耗尽时才交还人类）。

### 3.4 重读状态，不信口头汇报

这是循环引擎最关键的一条纪律：**subagent 返回后，重读 change.md 的 status，而不是信任其口头汇报。**

- 状态字段才是真相源（`git diff` 验证 status 行确实变了）；
- subagent 可能"觉得"自己完成了，但没写状态、写错了状态、或者中途失败——口头汇报不可靠，文件状态才是事实。

这条纪律把"我认为完成了"变成"状态字段证明完成了"——和 `/harness-quality` 的"一切判定来自客观数据"是同一条治理哲学的两次落地。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 260" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_l3" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_l3"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="260" fill="url(#bg_l3)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">派发纪律：四道闸门</text>
  <rect x="40" y="52" width="170" height="90" rx="10" fill="#a855f7" opacity="0.10" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_l3)"/>
  <text x="125" y="76" text-anchor="middle" fill="#d8b4fe" font-size="12" font-weight="700">① fresh subagent</text>
  <text x="125" y="100" text-anchor="middle" fill="#94a3b8" font-size="9">上下文隔离</text>
  <text x="125" y="118" text-anchor="middle" fill="#64748b" font-size="9">不累积污染</text>
  <rect x="230" y="52" width="170" height="90" rx="10" fill="#3b82f6" opacity="0.10" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_l3)"/>
  <text x="315" y="76" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">② 上下文指针</text>
  <text x="315" y="100" text-anchor="middle" fill="#94a3b8" font-size="9">给路径不给内容</text>
  <text x="315" y="118" text-anchor="middle" fill="#64748b" font-size="9">主进程保持轻量</text>
  <rect x="420" y="52" width="170" height="90" rx="10" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_l3)"/>
  <text x="505" y="76" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">③ --autonomous</text>
  <text x="505" y="100" text-anchor="middle" fill="#94a3b8" font-size="9">不暂停不询问</text>
  <text x="505" y="118" text-anchor="middle" fill="#64748b" font-size="9">只跑流程到门禁</text>
  <rect x="610" y="52" width="150" height="90" rx="10" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_l3)"/>
  <text x="685" y="76" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">④ 重读状态</text>
  <text x="685" y="100" text-anchor="middle" fill="#94a3b8" font-size="9">不信口头汇报</text>
  <text x="685" y="118" text-anchor="middle" fill="#64748b" font-size="9">状态字段是真相</text>
  <text x="400" y="180" text-anchor="middle" fill="#475569" font-size="11">四道闸门共同保证：循环可靠、隔离、可验证、不打扰</text>
  <text x="400" y="206" text-anchor="middle" fill="#64748b" font-size="10">特に④："我认为完成了" ≠ "状态字段证明完成了"</text>
  <text x="400" y="232" text-anchor="middle" fill="#64748b" font-size="10">git diff 验证 status 行确实变了——可信度校验不可省</text>
</svg>
```

## 四、停机信号：机器可读的交接单

### 4.1 五种停机信号

循环引擎每次迭代结束都输出**一行机器可读信号**，供外层 bash 循环 / CI 解析。这是 engine 与外界（人或脚本）的标准化接口：

```
<<<HARNESS next=<coding|testing|reviewing> step=<n>/<max_steps> status=<当前status>>>>
<<<HARNESS done stage=ci>>>                          # 成功停机：评审通过，交人工 /unit-test-ci
<<<HARNESS blocked reason="<原因>">>>                # 被阻塞：需要人类决策
<<<HARNESS stuck  stage=<status> attempt=<n>>>       # 同阶段无进展，stuck-stop
<<<HARNESS budget-exhausted steps=<n> max=<max_steps>>>  # 预算耗尽
```

五种信号对应五种结局：

| 信号 | 含义 | 谁负责下一步 |
|------|------|------------|
| `next` | 正常迭代中，将继续推进 | 引擎继续 |
| `done` | 到达 ci，收敛段完成 | **人类**（决定是否 /unit-test-ci） |
| `blocked` | 需要人类决策/审批 | **人类** |
| `stuck` | 同阶段无进展 | **人类** |
| `budget-exhausted` | 迭代预算用尽 | **人类** |

注意一个不对称：**只有 `next` 是引擎自己继续，其余四个信号都把控制权交还人类**。这不是巧合——循环引擎的设计哲学是：**引擎只在"确定能做"的路上自动，一旦出现任何"不确定"，就停机交人**。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 300" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_l4" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_l4"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="300" fill="url(#bg_l4)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">五信号：四种交还人类，一种引擎继续</text>
  <rect x="40" y="55" width="220" height="70" rx="10" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_l4)"/>
  <text x="150" y="80" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">next -- 继续</text>
  <text x="150" y="106" text-anchor="middle" fill="#64748b" font-size="9">引擎自己继续迭代</text>
  <rect x="290" y="55" width="220" height="70" rx="10" fill="#93c5fd" opacity="0.14" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_l4)"/>
  <text x="400" y="80" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">done -- 交人类</text>
  <text x="400" y="106" text-anchor="middle" fill="#64748b" font-size="9">到达 ci，下一步由人决定</text>
  <rect x="540" y="55" width="220" height="70" rx="10" fill="#fde68a" opacity="0.14" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_l4)"/>
  <text x="650" y="80" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">blocked -- 交人类</text>
  <text x="650" y="106" text-anchor="middle" fill="#64748b" font-size="9">需要人类决策/审批</text>
  <rect x="130" y="150" width="220" height="70" rx="10" fill="#fca5a5" opacity="0.14" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_l4)"/>
  <text x="240" y="175" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">stuck -- 交人类</text>
  <text x="240" y="201" text-anchor="middle" fill="#64748b" font-size="9">同阶段无进展</text>
  <rect x="450" y="150" width="220" height="70" rx="10" fill="#d8b4fe" opacity="0.14" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_l4)"/>
  <text x="560" y="175" text-anchor="middle" fill="#d8b4fe" font-size="12" font-weight="700">budget-exhausted -- 交人类</text>
  <text x="560" y="201" text-anchor="middle" fill="#64748b" font-size="9">迭代预算用尽</text>
  <text x="400" y="252" text-anchor="middle" fill="#475569" font-size="11">对称性：只有 next 由引擎继续，其余全交还人类</text>
  <text x="400" y="280" text-anchor="middle" fill="#64748b" font-size="10">引擎只在"确定能做"的路上自动，一旦"不确定"就停机交人</text>
</svg>
```

### 4.2 为什么需要机器可读的格式

`<<<HARNESS ...>>>` 的格式不是给人看的装饰，而是**为外层自动化准备的**：

- bash 循环可以用一行 `grep '<<<HARNESS done'` 判断是否成功收尾；
- CI 可以把 `blocked` 映射为失败通知，把 `done` 映射为继续下游管道；
- 人工值班脚本可以批量盯多个 change 的循环，只在收到非 `next` 信号时告警。

这正是"AI 输出的标准化"：人类可读的总结是附带的，**机器可解析的状态码才是契约**。

## 五、blocked / stuck / 预算耗尽：三种交还人的姿势

引擎把"需要人"的情况分成三种，交还的方式各自不同、判断阈值各自明确。

### 5.1 blocked：需要人类决策

**判定**：subagent 返回"缺前置 / 规格不明确 / 需人类审批 / 发现需人类决策的架构问题"等无法由引擎自行消解的信号 → 立即停机交还人类，输出 `reason`。

典型场景：

- 编码时发现需求描述有歧义，无法自行决定实现方向；
- 测试时发现 AC 与实现矛盾，不知道该跟哪边；
- 评审时发现设计有架构级问题，需要人拍板；
- 某个前置 change 还没 done，while 当前 change 依赖它。

**blocked 的本质**：引擎遇到了"需要判断力"的问题——而判断力不在引擎的授权范围内。停机不是失败，是**正确地识别出自己没有权限继续**。

### 5.2 stuck：同阶段无进展

**判定**：同一 `status` 连续 **2** 次迭代未前进（技能正常执行完但 status 未变，或测试一直红）→ 停机交还人类，输出 `stage` 与 `attempt`。

典型场景：

- `/coding-skill` 执行完，但 status 仍是 `coding`（技能声称完成却没推进状态）；
- 测试连续两次全红，修复陷入原地打转；
- 评审连续两次打回，修改没有实质进展。

**stuck 的本质**：引擎在"能自动推进"的路上空转——它不卡在判断力上，而卡在"执行没有收敛"上。连续 2 次相同状态不前进，说明继续自动跑大概率也是同样的空转，所以停机给人类看现场。

### 5.3 打回回路：不是 stuck，但有上限

一个重要的区分：`reviewing` 打回 `coding` 时 status 会**回退**（reviewing → coding）——这是**正常的收敛回路**，不算 stuck。评审发现代码问题 → 打回 → 重新编码 → 重新测试 → 重新评审，这是收敛段设计好的自愈机制，引擎要支持而不是报警。

但同一打回回路累计 > 2 次仍未到 `ci` → 停机交人类判断：**是不是需求/设计本身有问题**。

为什么打回要设上限？因为"评审打回"是收敛段的正常机制，但如果同一个 change 反复被同一类问题打回，那问题很可能不在代码质量，而在需求定义——此时继续循环只是在浪费算力，应该让人类重新审视需求。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 260" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_l5" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_l5"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="260" fill="url(#bg_l5)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">三种交还：判据各不同</text>
  <rect x="40" y="50" width="230" height="100" rx="10" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_l5)"/>
  <text x="155" y="76" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">blocked</text>
  <text x="155" y="102" text-anchor="middle" fill="#94a3b8" font-size="10">缺前置/规格不明/需审批</text>
  <text x="155" y="126" text-anchor="middle" fill="#64748b" font-size="9">需要判断力 → 交人</text>
  <rect x="290" y="50" width="230" height="100" rx="10" fill="#ef4444" opacity="0.10" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_l5)"/>
  <text x="405" y="76" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">stuck</text>
  <text x="405" y="102" text-anchor="middle" fill="#94a3b8" font-size="10">同 status 连续 2 次未前进</text>
  <text x="405" y="126" text-anchor="middle" fill="#64748b" font-size="9">执行空转 → 交人</text>
  <rect x="540" y="50" width="220" height="100" rx="10" fill="#a855f7" opacity="0.10" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_l5)"/>
  <text x="650" y="76" text-anchor="middle" fill="#d8b4fe" font-size="12" font-weight="700">打回回路＞2</text>
  <text x="650" y="102" text-anchor="middle" fill="#94a3b8" font-size="10">reviewing↔coding 反复</text>
  <text x="650" y="126" text-anchor="middle" fill="#64748b" font-size="9">疑需求设计问题 → 交人</text>
  <text x="400" y="190" text-anchor="middle" fill="#475569" font-size="11">打回本身不是 stuck——它是收敛自愈机制，但要设上限</text>
  <text x="400" y="214" text-anchor="middle" fill="#64748b" font-size="10">同一 change 反复被同一类问题打回 → 问题可能在需求定义</text>
  <text x="400" y="238" text-anchor="middle" fill="#64748b" font-size="10">停止循环是负责任的收敛，不是逃避</text>
</svg>
```

### 5.4 预算耗尽：国际象棋里的"先手限时"

`--max-steps` 缺省 4，超过即输出 `budget-exhausted` 停机。预算的意义：**任何循环都不允许无限消耗资源**。

- 对算力：每次迭代都要派发 subagent、跑检查，成本递增；
- 对时间：无人值守任务不该无限挂机；
- 对心态：明确的预算让"该停了"有了硬标准，而不是"再试一次看运气"。

预算耗尽不是失败——它和 blocked/stuck 一样，是引擎的**正常出口**。只是它的触发原因不是"判断力不足"或"执行空转"，而是"资源配额用完"。

## 六、与其余技能的边界：谁的事归谁办

`/harness-loop-run` 的边界表把"什么事归谁"钉死，防止引擎越界：

| 场景 | 归属 |
|------|------|
| 需求/设计关键取舍 | `harnessing` + 人工，**不在本引擎** |
| CI 门禁 / 部署 / 发布 | `/unit-test-ci` `/deploy-verify` `/harness-ship`，**留人**，不在本引擎 |
| 状态总览 | `/harness-status`（只读） |
| 变更间影响 | `/harness-relate impact <id>` |

这张表的作用是**防越权**：

- **向上（需求）不碰**：引擎只跑"已经批准的需求"的收敛段；需求本身的分析、取舍、审批属于 `harnessing` 和人类。
- **向下（发布）不碰**：引擎到 `ci` 就停，绝不自动 CI/部署/发布——这些是不可逆·外发操作。
- **横向（同工具族）分工**：看状态用 `status`，看影响用 `relate`，引擎只负责"推进"。

## 七、完成标志：怎样算"跑完了"

`/harness-loop-run` 的完成标志分成功与停机两类：

1. **成功停机**：`reviewing` 通过，`status: ci`，已输出 `<<<HARNESS done stage=ci>>>` 并提示交人工。
2. **受阻停机**：被 blocked / stuck / 预算耗尽停机，已输出对应机器可读信号与人类可读原因。
3. **全程未触碰** `ci → verifying → done` 及发布链路——这是本技能最重要的"负向完成标志"：跑完了，但没越界。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 230" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_l6" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_l6"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="230" fill="url(#bg_l6)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">完成标志：成功停机 or 受阻停机，都不越界</text>
  <rect x="40" y="50" width="230" height="110" rx="10" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_l6)"/>
  <text x="155" y="76" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">① 成功停机</text>
  <text x="155" y="102" text-anchor="middle" fill="#94a3b8" font-size="10">status: ci + done 信号</text>
  <text x="155" y="124" text-anchor="middle" fill="#64748b" font-size="9">交人决定/unit-test-ci</text>
  <rect x="290" y="50" width="230" height="110" rx="10" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_l6)"/>
  <text x="405" y="76" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">② 受阻停机</text>
  <text x="405" y="102" text-anchor="middle" fill="#94a3b8" font-size="10">blocked/stuck/budget</text>
  <text x="405" y="124" text-anchor="middle" fill="#64748b" font-size="9">信号 + 人类可读原因</text>
  <rect x="540" y="50" width="220" height="110" rx="10" fill="#ef4444" opacity="0.10" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_l6)"/>
  <text x="650" y="76" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">③ 全程未越界</text>
  <text x="650" y="102" text-anchor="middle" fill="#94a3b8" font-size="10">不碰 ci→verifying→done</text>
  <text x="650" y="124" text-anchor="middle" fill="#64748b" font-size="9">发布链路永远留人</text>
  <text x="400" y="190" text-anchor="middle" fill="#475569" font-size="11">"跑完了"里最重的一条：全程未触碰发布链路</text>
  <text x="400" y="214" text-anchor="middle" fill="#64748b" font-size="10">负向完成标志——没做不该做的事，也是完成</text>
</svg>
```

## 八、写在最后

`/harness-loop-run` 的全部设计，浓缩成四句话：

1. **边界先于能力**——能自动的范围（收敛段）刻得很窄，不能自动的（发布链路）刻得很死。引擎的灵魂不是"能跑多远"，而是"知道在哪停"。
2. **状态是唯一真相**——重读 change.md 的 status，不信任何口头汇报；状态变了才算前进。
3. **停机是正常出口**——done / blocked / stuck / budget-exhausted 全是设计好的出口，引擎用它把"不确定"交还人类。
4. **信号要机器可读**——`<<<HARNESS ...>>>` 是引擎与外界的契约，让 bash/CI 能自动化地处理循环结果。

一句话记住它：**/harness-loop-run 是收敛段的无人驾驶——它只开确定的路，遇到任何不确定就靠边停车，把方向盘交回人类。**