# /harness-retro：这个迭代做得怎么样——用真实留档回答

> 命令深度拆解 · 第 34 篇 · 约 9000 字 · 7 个 SVG 图

---

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 300" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_t0" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_t0"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="300" fill="url(#bg_t0)" rx="10"/>
  <text x="400" y="28" text-anchor="middle" fill="#e2e8f0" font-size="26" font-weight="700">/harness-retro：这个迭代做得怎么样</text>
  <rect x="60" y="55" width="320" height="95" rx="10" fill="#a855f7" opacity="0.12" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_t0)"/>
  <text x="220" y="80" text-anchor="middle" fill="#d8b4fe" font-size="14" font-weight="700">只读聚合技能</text>
  <text x="220" y="104" text-anchor="middle" fill="#94a3b8" font-size="11">读取历史留档 · 不改任何状态</text>
  <text x="220" y="126" text-anchor="middle" fill="#64748b" font-size="10">change.md / review / verify / 质量报告</text>
  <rect x="420" y="55" width="320" height="95" rx="10" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_t0)"/>
  <text x="580" y="80" text-anchor="middle" fill="#86efac" font-size="14" font-weight="700">趋势报告 + 改进项</text>
  <text x="580" y="104" text-anchor="middle" fill="#94a3b8" font-size="11">阶段分布 · 打回率 · 覆盖率 · 技术债</text>
  <text x="580" y="126" text-anchor="middle" fill="#64748b" font-size="10">回答\"这个迭代我们做得怎么样\"</text>
  <text x="400" y="185" text-anchor="middle" fill="#475569" font-size="13">四步流程：界定时间窗 → 聚合指标 → 出报告 → 转化</text>
  <rect x="60" y="205" width="160" height="45" rx="8" fill="#3b82f6" opacity="0.10" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_t0)"/>
  <text x="140" y="234" text-anchor="middle" fill="#93c5fd" font-size="11">① 界定时间窗</text>
  <rect x="240" y="205" width="160" height="45" rx="8" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_t0)"/>
  <text x="320" y="234" text-anchor="middle" fill="#fde68a" font-size="11">② 聚合指标</text>
  <rect x="420" y="205" width="160" height="45" rx="8" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_t0)"/>
  <text x="500" y="234" text-anchor="middle" fill="#86efac" font-size="11">③ 出报告</text>
  <rect x="600" y="205" width="160" height="45" rx="8" fill="#ef4444" opacity="0.10" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_t0)"/>
  <text x="680" y="234" text-anchor="middle" fill="#fca5a5" font-size="11">④ 转化改进项</text>
  <text x="400" y="280" text-anchor="middle" fill="#64748b" font-size="10">三条纪律：数字来自真实留档 · 结论带证据 · 复盘为了行动</text>
</svg>
```

## 一、/harness-retro 解决的是什么问题

### 1.1 "这个迭代做得怎么样"——凭感觉还是凭数据

迭代结束，团队坐进会议室，主持人问："这个迭代做得怎么样？"最常见的回答是——"还行""挺忙的""感觉质量不错"。

感觉是复盘最大的敌人。感觉会把"打回率 40%"记成"偶尔被打回"，把"覆盖率从 80% 降到 55%"记成"覆盖率还行"。**复盘必须建立在数据上**，而数据来自哪里？来自流水线留下的留档。

`/harness-retro` 就是干这个的：读取 `.harness/changes/` 的历史留档（change.md 状态变化 / review.md / verify.md / quality-report.md / TECH-DEBT.md），聚合出**迭代质量趋势报告**，回答"这个迭代/阶段我们做得怎么样"。

它的定位是**只读聚合技能**：只产报告，不改任何状态。报告用于——迭代回顾、把趋势问题转化为改进项（可登记为技术债或新 change）。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 240" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_t1" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_t1"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="240" fill="url(#bg_t1)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">感觉 vs 数据</text>
  <rect x="40" y="50" width="330" height="110" rx="8" fill="#ef4444" opacity="0.10" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_t1)"/>
  <text x="205" y="76" text-anchor="middle" fill="#fca5a5" font-size="13" font-weight="700">凭感觉复盘</text>
  <text x="205" y="102" text-anchor="middle" fill="#94a3b8" font-size="10\">\"还行\"\"挺忙\"\"感觉不错\"</text>
  <text x="205" y="124" text-anchor="middle" fill="#64748b" font-size="8">忽略 40% 打回率</text>
  <text x="205" y="146" text-anchor="middle" fill="#64748b" font-size="8\">掩盖 80%→55% 覆盖率滑坡</text>
  <rect x="420" y="50" width="340" height="110" rx="8" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_t1)"/>
  <text x="590" y="76" text-anchor="middle" fill="#86efac" font-size="13" font-weight="700">凭留档复盘</text>
  <text x="590" y="102" text-anchor="middle" fill="#94a3b8" font-size="10\">打回率 / 覆盖率 / 技术债增减</text>
  <text x="590" y="124" text-anchor="middle" fill="#64748b" font-size="8\">每个结论都有数字撑腰</text>
  <text x="590" y="146" text-anchor="middle" fill="#64748b" font-size="8\">趋势问题当场转化为改进项</text>
  <text x="400" y="196" text-anchor="middle" fill="#475569" font-size="11\">感觉把数据记错，数据把感觉纠正</text>
  <text x="400" y="222" text-anchor="middle" fill="#64748b" font-size="10\">复盘不是忆苦思甜会，是数据驱动的趋势诊断</text>
</svg>
```

### 1.2 复盘与状态总览的分工：一个看时点，一个看趋势

`/harness-status` 与 `/harness-retro` 一个看"现在"，一个看"过去"：

| 维度 | harness-status | harness-retro |
|------|---------------|---------------|
| 时间 | 当前时点 | 一个时间窗 |
| 视角 | 进行中的 change | 已结束的迭代 |
| 输出 | 待办建议 | 趋势报告 + 改进项 |
| 用途 | 调度下一步 | 迭代回顾 / 定位瓶颈 |

status 回答"现在该推进哪个"，retro 回答"这个迭代做得怎么样"。一个管当下行动，一个管周期反思——两者都是治理层，但一个向前看，一个向后看。

## 二、输入源：复盘的"原材料"

复盘的质量取决于留档的质量。`/harness-retro` 的输入源有六类：

| 数据源 | 提取内容 |
|--------|---------|
| `.harness/changes/*/change.md` | 状态流转（id/status/created）、打回痕迹 |
| `.harness/changes/<id>/review.md` | 评审结论、🔴/🟡 问题分布 |
| `.harness/changes/<id>/verify.md` | 部署验证结论、回滚记录 |
| `.harness/changes/<id>/quality-report.md` | 覆盖率、flow→test 映射、放行签字 |
| `.harness/changes/TECH-DEBT.md` | 技术债条目与优先级 |
| git 历史 | 提交节奏、commit message 含 change ID 的比例 |

这六类输入源的存在本身就在强调一件事：**留档不是流程负担，是复盘的数据资产**。review.md 不只是评审那一刻的证据，它还是三个月后复盘时"评审问题分布"的统计来源。每个 change 的每个产物，都在为未来的复盘积累数据——认真留档，才有认真的复盘。

git 历史这个输入源特别有意思：它统计"commit message 含 change ID 的比例"——这是追溯链完整性的信号。比例低，说明提交和 change 之间断了线，将来查"这个提交是哪个 change 引入的"无从下手。

## 三、四步流程：时间窗 → 聚合 → 报告 → 转化

### Step 1: 界定时间窗

按 `$ARGUMENTS` 或对话上下文确定复盘范围：`last-iteration`（近 N 个 change）/ 指定 id 区间 / 指定日期段。无参数 → **默认最近 10 个 change**。

时间窗是复盘的"取样范围"，决定了报告说的是哪段时间的事：

- `last-iteration`：跟着迭代走——说"这个迭代"自然用迭代边界；
- 指定 id 区间：针对某批 change 专项复盘；
- 指定日期段：按日历时间复盘（比如想看看某个月的质量走势）；
- 无参数默认最近 10 个 change：给出即时可用的基线。

时间窗界定本身值得诚实：**范围定成什么样，结论就覆盖什么样**。默认最近 10 个 change 给出的报告，只能说明"最近 10 个 change 做得怎么样"，不能推广成"这个季度做得怎么样"。

### Step 2: 聚合指标

对时间窗内的 change 计算六类指标：

| 指标 | 计算方式 | 信号 |
|------|---------|------|
| 交付数/打回数 | done 数 vs 被打回次数 | 打回率高 → 需求/设计阶段质量差 |
| 各阶段停留 | 每个 change 在 analyzing/coding/reviewing 停留时间 | 某阶段集中卡住 → 瓶颈 |
| 评审问题分布 | review.md 中 🔴/🟡 问题的类型归类 | 同类问题反复 → 系统性问题 |
| 覆盖率趋势 | quality-report.md 的覆盖率数字 | 下降 → 测试保护在退化 |
| 技术债变化 | TECH-DEBT.md 增删 | 债只增不减 → 熵失控 |
| 提交质量 | commit message 含 change ID 的比例 | 低 → 追溯断裂 |

每个指标都带"信号"列——指标不是数字陈列，是**诊断探针**：

- **打回率高** → 问题出在需求/设计阶段——代码写得再快，需求没想清楚就会被评审打回；
- **某阶段集中卡住** → 流水线瓶颈——比如 analyzing 停留普遍很长，说明需求阶段是瓶颈；
- **同类问题反复** → 系统性问题——评审老是标"命名混乱"，这不是单个代码问题，是规范缺失问题；
- **覆盖率下降** → 测试保护在退化——没人写测试了，或测试写得太敷衍；
- **债只增不减** → 熵失控——改进项永远在登记，永远没偿还；
- **追溯断裂** → commit 和 change 脱钩，将来无法回答"这是哪个 change 引入的"。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 300" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_t2" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_t2"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="300" fill="url(#bg_t2)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">六类指标：每个都是诊断探针</text>
  <rect x="40" y="45" width="240" height="55" rx="8" fill="#ef4444" opacity="0.10" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_t2)"/>
  <text x="160" y="67" text-anchor="middle" fill="#fca5a5" font-size="11" font-weight="700">交付数/打回数</text>
  <text x="160" y="88" text-anchor="middle" fill="#64748b" font-size="9">打回率高 → 需求/设计质量差</text>
  <rect x="300" y="45" width="240" height="55" rx="8" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_t2)"/>
  <text x="420" y="67" text-anchor="middle" fill="#fde68a" font-size="11" font-weight="700">各阶段停留</text>
  <text x="420" y="88" text-anchor="middle" fill="#64748b" font-size="9">某阶段集中卡 → 瓶颈</text>
  <rect x="560" y="45" width="200" height="55" rx="8" fill="#a855f7" opacity="0.10" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_t2)"/>
  <text x="660" y="67" text-anchor="middle" fill="#d8b4fe" font-size="11" font-weight="700">评审问题分布</text>
  <text x="660" y="88" text-anchor="middle" fill="#64748b" font-size="9">同类反复 → 系统性问题</text>
  <rect x="40" y="120" width="240" height="55" rx="8" fill="#3b82f6" opacity="0.10" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_t2)"/>
  <text x="160" y="142" text-anchor="middle" fill="#93c5fd" font-size="11" font-weight="700">覆盖率趋势</text>
  <text x="160" y="163" text-anchor="middle" fill="#64748b" font-size="9">下降 → 测试保护在退化</text>
  <rect x="300" y="120" width="240" height="55" rx="8" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_t2)"/>
  <text x="420" y="142" text-anchor="middle" fill="#86efac" font-size="11" font-weight="700">技术债变化</text>
  <text x="420" y="163" text-anchor="middle" fill="#64748b" font-size="9">债只增不减 → 熵失控</text>
  <rect x="560" y="120" width="200" height="55" rx="8" fill="#ef4444" opacity="0.10" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_t2)"/>
  <text x="660" y="142" text-anchor="middle" fill="#fca5a5" font-size="11" font-weight="700">提交质量</text>
  <text x="660" y="163" text-anchor="middle" fill="#64748b" font-size="9">追溯断裂信号</text>
  <text x="400" y="220" text-anchor="middle" fill="#475569" font-size="11">指标不是数字陈列，是诊断：每个"信号"都是一条行动线索</text>
  <text x="400" y="250" text-anchor="middle" fill="#64748b" font-size="10">打回率高别怪代码，去看需求阶段；覆盖降别怪工具，去看测试态度</text>
  <text x="400" y="274" text-anchor="middle" fill="#64748b" font-size="10">数字自己不会说话，信号告诉你怎么听</text>
</svg>
```

### Step 3: 出报告（写 .harness/retro/<日期>.md）

报告落盘到 `.harness/retro/<日期>.md`，结构四段：

```markdown
# 迭代复盘: <时间窗>

## 总览
- 交付 change: X 个（done）/ 进行中: Y
- 打回率: Z%（N 次打回 / M 次评审）
- 平均停留: 各阶段分布 <表或条形图>

## 趋势
<各指标随时间变化的关键观察，附真实数字>

## 问题定位
- 🔴 <严重趋势问题>（证据: <数字/文件>）
- 🟡 <次要问题>
- 🟢 <做得好、值得保持的>

## 改进项（可执行）
| 优先级 | 改进项 | 来源证据 | 登记方式 |
|--------|--------|---------|---------|
| P0 | <…> | <指标> | 新建 change / TECH-DEBT |
| P1 | <…> | <…> | <…> |
```

四段结构的用意：

- **总览**：一页看清迭代全貌——交付多少、打回多少、卡在哪；
- **趋势**：指标随时间的变化——单次数字会骗人，趋势不会（覆盖率一次低可能是统计口径问题，连续三次下降就是真问题）；
- **问题定位**：按严重度分级——🔴 严重问题必须带证据，🟡 次要问题记录在案，🟢 做得好的一定要写——复盘不全是批评，值得保持的也要点名；
- **改进项**：每条都可执行、带优先级、带来源证据、带登记方式。

### Step 4: 转化：复盘必须落到行动

报告写完不是终点——**复盘是为了行动**：

- 把可执行的改进项**当场登记**：写入 `TECH-DEBT.md`（按优先级）或建议新建 change；
- 系统性问题（如"每个 change 都在 reviewing 被打回"）→ 建议检查需求阶段是否充分（`/harnessing`）或补充设计阶段。

转化的两种去向：

1. **技术债登记**（TECH-DEBT.md）——适合"改进测试基建""补评审清单"这类持续投入项；
2. **新 change**——适合"修复某块具体代码""重组某个流程"这类可独立交付项。

复盘和行动之间最常断的环节就是这里：**报告写得漂亮，改进项从不登记，下个迭代原样重演**。`Step 4` 的"当场登记"治的就是这个病——改进项在报告里出现的同时，就必须有个落点（TECH-DEBT 条目或新 change）。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 260" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_t3" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_t3"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="260" fill="url(#bg_t3)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">Step 4：复盘必须落到行动</text>
  <rect x="40" y="50" width="330" height="100" rx="8" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_t3)"/>
  <text x="205" y="76" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">改进项 → TECH-DEBT.md</text>
  <text x="205" y="102" text-anchor="middle" fill="#94a3b8" font-size="9">持续投入项：测试基建/评审清单</text>
  <text x="205" y="124" text-anchor="middle" fill="#64748b" font-size="8">按优先级登记</text>
  <rect x="420" y="50" width="340" height="100" rx="8" fill="#3b82f6" opacity="0.10" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_t3)"/>
  <text x="590" y="76" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">改进项 → 新 change</text>
  <text x="590" y="102" text-anchor="middle" fill="#94a3b8" font-size="9">可独立交付项：修复代码/重组流程</text>
  <text x="590" y="124" text-anchor="middle" fill="#64748b" font-size="8">系统性问题回顾需求阶段</text>
  <text x="400" y="186" text-anchor="middle" fill="#475569" font-size="11">报告漂亮但改进项不登记 = 下个迭代原样重演</text>
  <text x="400" y="214" text-anchor="middle" fill="#64748b" font-size="10">"当场登记"治的就是"复盘与行动脱节"这个病</text>
  <text x="400" y="240" text-anchor="middle" fill="#64748b" font-size="10">复盘的价值 = 行动；没有行动的复盘只是一份归档的 PPT</text>
</svg>
```

## 四、三条纪律

### 4.1 一切数字来自真实留档

**不编造覆盖率/打回率；留档缺失处如实标注"该阶段无记录"。**

复盘容易犯的错是"脑补数据"：覆盖率没查，写个 75%。编造的数据会直接污染决策——下个迭代拿假基线对比，全盘皆错。**留档缺失就写"无记录"**——缺失本身也是一条信号：说明那个阶段没留档，这比编个数诚实得多。

### 4.2 不留空泛结论

**每条结论必须带证据（数字或文件引用）。**

"测试质量有待提升"是空泛结论；"覆盖率从上迭代 78% 降至 61%（quality-report.md 数据），且 flow→test 映射缺失 3 条关键业务流"是带证据的结论。复盘报告不是感想集，是**证据链**——每条判断都能指回一个数字或一个文件。

### 4.3 复盘是为了行动

**报告末尾必须有可执行的改进项，不写成"都挺好"。**

"都挺好"的复盘是最贵的复盘——花了一小时，结论是没事。改进项可以少，但必须可执行、带优先级、有登记方式。列不出改进项的复盘，说明复盘本身没做透。

## 五、与相邻技能的分工

| 场景 | 归属 |
|------|------|
| 当前状态总览 | `/harness-status` |
| 历史趋势复盘 | **本技能**（`/harness-retro`） |
| 改进项落实 | 新建 change（`/harnessing`）或技术债登记 |

- **harness-status**：看当下的总览与待办——status 是"现在"，retro 是"过去"；
- **harnessing**：改进项落实为 change 时的需求打磨入口——复盘提出的改进项，交到 harnessing 变成正式 change；
- **TECH-DEBT.md**：技术债登记处——复盘发现的持续投入项，登记到这里排队偿还。

## 六、完成标志

`/harness-retro` 的完成标志有三个：

1. **报告已落盘** `.harness/retro/<日期>.md`，含总览/趋势/问题定位/改进项——四段结构齐全，可被查阅；
2. **数字均来自真实留档**，缺失已如实标注——数据可信，无脑补；
3. **改进项已登记**（TECH-DEBT.md 或新 change 建议）——复盘真的落到了行动。

三个标志层层递进：写出来了（报告落盘）→ 写对了（数字真实）→ 有用了（改进项登记）。**第三个标志是复盘的灵魂——改进项没登记，前两个做得再好也只是存档。**

## 七、写在最后

`/harness-retro` 的全部设计，浓缩成四句话：

1. **复盘凭数据，不凭感觉**——留档是数据资产，每个产物都在为复盘积累。
2. **指标是诊断探针，不是数字陈列**——每个指标都带信号，告诉你问题在哪。
3. **结论必须带证据**——没有数字/文件支撑的判断，不配写进报告。
4. **复盘为了行动**——改进项当场登记，绝不写成"都挺好"。

一句话记住它：**/harness-retro 是流水线的"体检科"——它把迭代的留档聚合为趋势报告，让"这个迭代做得怎么样"从感觉判断变成数据证据，并保证每个结论最终落成可执行的改进项。**