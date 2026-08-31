# /domain-modeling：把术语钉在纸上

> 命令深度拆解 · 第 23 篇 · 约 9000 字 · 8 个 SVG 图

---

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 300" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_d0" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_d0"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="300" fill="url(#bg_d0)" rx="10"/>
  <text x="400" y="28" text-anchor="middle" fill="#e2e8f0" font-size="26" font-weight="700">/domain-modeling：把术语钉在纸上</text>
  <rect x="70" y="55" width="300" height="100" rx="10" fill="#a855f7" opacity="0.12" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_d0)"/>
  <text x="220" y="82" text-anchor="middle" fill="#d8b4fe" font-size="14" font-weight="700">CONTEXT.md — 领域活词典</text>
  <text x="220" y="106" text-anchor="middle" fill="#94a3b8" font-size="11">术语敲定当场写入 · 零实现细节</text>
  <text x="220" y="128" text-anchor="middle" fill="#64748b" font-size="10">只当术语表，不当规格仓</text>
  <rect x="430" y="55" width="300" height="100" rx="10" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_d0)"/>
  <text x="580" y="82" text-anchor="middle" fill="#93c5fd" font-size="14" font-weight="700">ADR — 架构决策记录</text>
  <text x="580" y="106" text-anchor="middle" fill="#94a3b8" font-size="11">难逆转 + 脱离上下文会惊讶 + 真实权衡</text>
  <text x="580" y="128" text-anchor="middle" fill="#64748b" font-size="10">三条件全满足才写 · 克制提供</text>
  <text x="400" y="185" text-anchor="middle" fill="#475569" font-size="13">四个主动动作，贯穿整个会话</text>
  <text x="120" y="215" text-anchor="middle" fill="#94a3b8" font-size="11">① 对照词表挑战术语</text>
  <text x="320" y="215" text-anchor="middle" fill="#94a3b8" font-size="11">② 精化模糊语言</text>
  <text x="515" y="215" text-anchor="middle" fill="#94a3b8" font-size="11">③ 场景压测领域关系</text>
  <text x="695" y="215" text-anchor="middle" fill="#94a3b8" font-size="11">④ 与代码交叉验证</text>
  <text x="400" y="252" text-anchor="middle" fill="#64748b" font-size="11">核心洞察：术语敲定的「当下」是最便宜的时机——过了就再也补不回来</text>
  <text x="400" y="278" text-anchor="middle" fill="#475569" font-size="10">本技能用于「改变模型」，不是「消费模型」</text>
</svg>
```

## 一、/domain-modeling 解决的是什么问题

### 1.1 术语漂移：软件项目的隐形税

先问一个问题：你的项目里，"客户"这个词出现过几次不同的含义？

- 产品文档里的"客户" = 下单的公司；
- 数据库表 `user` = 登录账号；
- 代码里的 `Customer` = 收款对象；
- 客服口中的"客户" = 打过电话的人。

同一个词，四个含义。每一次沟通，人们都在心里默默做一次"翻译"；每一次翻译，都有出错的可能。这种成本不在预算表里，但它真实存在——它叫**术语漂移**。

`/domain-modeling` 要消灭的就是它。做法不是开会培训，而是**把术语钉在纸上**：项目里放一份"活词典"（CONTEXT.md），每个词的权威含义都写下来，谁有疑问谁去查，谁用错谁被纠正。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 260" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_d1" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_d1"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="260" fill="url(#bg_d1)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">同一个词，四种含义：术语漂移</text>
  <rect x="40" y="50" width="170" height="90" rx="10" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_d1)"/>
  <text x="125" y="74" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">产品文档</text>
  <text x="125" y="100" text-anchor="middle" fill="#94a3b8" font-size="10">"客户" = 下单的公司</text>
  <text x="125" y="120" text-anchor="middle" fill="#64748b" font-size="9">业务视角</text>
  <rect x="230" y="50" width="170" height="90" rx="10" fill="#3b82f6" opacity="0.10" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_d1)"/>
  <text x="315" y="74" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">数据库</text>
  <text x="315" y="100" text-anchor="middle" fill="#94a3b8" font-size="10">user = 登录账号</text>
  <text x="315" y="120" text-anchor="middle" fill="#64748b" font-size="9">存储视角</text>
  <rect x="420" y="50" width="170" height="90" rx="10" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_d1)"/>
  <text x="505" y="74" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">代码</text>
  <text x="505" y="100" text-anchor="middle" fill="#94a3b8" font-size="10">Customer = 收款对象</text>
  <text x="505" y="120" text-anchor="middle" fill="#64748b" font-size="9">实现视角</text>
  <rect x="610" y="50" width="150" height="90" rx="10" fill="#ef4444" opacity="0.10" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_d1)"/>
  <text x="685" y="74" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">客服</text>
  <text x="685" y="100" text-anchor="middle" fill="#94a3b8" font-size="10">"客户" = 打过电话的人</text>
  <text x="685" y="120" text-anchor="middle" fill="#64748b" font-size="9">沟通视角</text>
  <text x="400" y="175" text-anchor="middle" fill="#475569" font-size="11">每次沟通都要心理翻译 → 每次翻译都可能出错 → 出错成本 = 返工 + 返工 + 返工</text>
  <text x="400" y="200" text-anchor="middle" fill="#fca5a5" font-size="11">/domain-modeling 的答案：一个词只有一种权威含义，写在 CONTEXT.md 里</text>
  <text x="400" y="228" text-anchor="middle" fill="#64748b" font-size="10">不是培训、不是约定，是"谁用错谁被纠正，谁有疑问谁去查"的活词典</text>
</svg>
```

### 1.2 术语的"成本曲线"是陡峭上升的

为什么必须在术语敲定的"当下"处理，而不是累积到月底统一整理？

因为术语模糊的成本曲线是**陡峭上升**的：

| 时机 | 修正一个术语的成本 | 说明 |
|------|------------------|------|
| 讨论中（当下） | 1 分钟 | 改一句话的事 |
| 写进文档后 | 30 分钟 | 要翻文档、改文档 |
| 写进代码后 | 半天 | 变量名、类型、接口全要改 |
| 上线后 | 数天 | 数据迁移、兼容逻辑、客服话术 |

成本上升了三个数量级。这就是 `domain-modeling` 强调**"当场更新"**的原因：术语敲定的当下，是整条时间线上修正成本最低的点。过了这个点，每拖延一天，修正成本就涨一截。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 260" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_d2" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_d2"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="260" fill="url(#bg_d2)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">术语修正成本：随时间陡峭上升</text>
  <g fill="none" stroke="#a855f7" stroke-width="3">
    <path d="M 80 200 C 160 195, 220 180, 320 140 S 560 70, 720 30" />
  </g>
  <g fill="#a855f7"><circle cx="80" cy="200" r="6"/><circle cx="320" cy="140" r="6"/><circle cx="560" cy="70" r="6"/><circle cx="720" cy="30" r="6"/></g>
  <text x="80" y="222" text-anchor="middle" fill="#94a3b8" font-size="10">讨论中 · 1分钟</text>
  <text x="320" y="162" text-anchor="middle" fill="#94a3b8" font-size="10">写进文档 · 30分钟</text>
  <text x="560" y="92" text-anchor="middle" fill="#94a3b8" font-size="10">写进代码 · 半天</text>
  <text x="720" y="48" text-anchor="middle" fill="#94a3b8" font-size="10">上线后 · 数天</text>
  <text x="400" y="252" text-anchor="middle" fill="#64748b" font-size="10">三个数量级的成本差异 → "当场"不是风格偏好，是成本决策</text>
</svg>
```

### 1.3 但它不是"记单词"

这里必须澄清一个常见的误解：把术语写进 CONTEXT.md，然后照做，这不叫 domain-modeling。

`/domain-modeling` 的定位是**"改变模型"，而不是"消费模型"**。它的动作都是主动的：

- **挑战**：发现词表定义和实际用法冲突，当场指出。
- **精化**：发现模糊的、过载的词，提出更精确的规范词。
- **压测**：用具体场景逼领域关系露出边界。
- **交叉验证**：拿代码和术语对质。

"读 CONTEXT.md 取词"是任何技能都能做的一行习惯；而**主动质疑术语、发明边界场景、在当下修正模型**——这才是本技能存在的理由。

## 二、四个主动动作：会话里的"领域警察"

### 2.1 动作一：对照词表挑战术语

当用户（或代码、或文档）使用的术语与 CONTEXT.md 已有语言冲突时，`domain-modeling` 的第一反应是**立即指出**，而不是默默接受。

典型对话：

> 用户：订单取消后，把退款发给客户。
> 模型（对照词表）：你的词表里 "cancellation" 定义为"取消订单且不产生退款"，但你这里说的是"取消并退款"——到底是哪个？如果两者都存在，我们需要为"取消并退款"定一个新词，而不是复用 cancellation。

为什么要这样"较真"？因为**同一个词表里一个词只能有一个含义**。如果"cancellation"同时表示"取消不退钱"和"取消退钱"，那么代码里 `cancelOrder` 的行为究竟是哪个，就得靠猜。靠猜的业务逻辑，是 bug 的温床。

关键纪律：**挑战要基于词表，不是基于个人偏好**。指出冲突时引用 CONTEXT.md 的现有定义，让争论落在"文档 vs 用法"上，而不是"我 vs 你"上。

### 2.2 动作二：精化模糊语言

术语冲突是"词对不上词"；模糊语言是"词本身含义不清"。比如用户说 "account"——它到底指 Customer（付款方）还是 User（登录者）？

`domain-modeling` 会提出一个精确的规范词：

> 你说 "account"——你指的是 Customer 还是 User？两者是不同的东西。Customer 是付款方，User 是登录者。一个 Customer 可以对应多个 User。

精化的本质是**把过载的词拆分**。一个词承载了两个概念，就制造了两处歧义。拆开后，每个词只有一个概念，歧义归零。

精化的对象不只是名词，也包括动词：用户说"处理订单"，"处理"包含"校验、冻结、扣款、发货"四件事——"处理"一词过载，应当拆成四个精确动作。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 240" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_d3" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_d3"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="240" fill="url(#bg_d3)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">动作二：把过载的词拆开</text>
  <rect x="320" y="50" width="160" height="50" rx="10" fill="#ef4444" opacity="0.12" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_d3)"/>
  <text x="400" y="72" text-anchor="middle" fill="#fca5a5" font-size="13" font-weight="700">"处理订单"</text>
  <text x="400" y="92" text-anchor="middle" fill="#64748b" font-size="9">一个词装四件事 → 歧义</text>
  <line x1="400" y1="100" x2="400" y2="125" stroke="#475569" stroke-width="2"/>
  <polygon points="400,131 395,123 405,123" fill="#94a3b8"/>
  <rect x="70" y="140" width="150" height="50" rx="8" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_d3)"/>
  <text x="145" y="162" text-anchor="middle" fill="#86efac" font-size="12">校验订单</text>
  <text x="145" y="180" text-anchor="middle" fill="#64748b" font-size="9">validate</text>
  <rect x="250" y="140" width="150" height="50" rx="8" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_d3)"/>
  <text x="325" y="162" text-anchor="middle" fill="#86efac" font-size="12">冻结库存</text>
  <text x="325" y="180" text-anchor="middle" fill="#64748b" font-size="9">reserve</text>
  <rect x="430" y="140" width="150" height="50" rx="8" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_d3)"/>
  <text x="505" y="162" text-anchor="middle" fill="#86efac" font-size="12">扣款</text>
  <text x="505" y="180" text-anchor="middle" fill="#64748b" font-size="9">charge</text>
  <rect x="610" y="140" width="150" height="50" rx="8" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_d3)"/>
  <text x="685" y="162" text-anchor="middle" fill="#86efac" font-size="12">发货</text>
  <text x="685" y="180" text-anchor="middle" fill="#64748b" font-size="9">ship</text>
  <text x="400" y="220" text-anchor="middle" fill="#64748b" font-size="10">拆开后每个词只有一个概念——歧义归零，代码与沟通对齐</text>
</svg>
```

### 2.3 动作三：用具体场景压测领域关系

领域关系的"真面目"只在边界上显现。`domain-modeling` 会主动**编造能触及边界的场景**，逼用户在概念边界上精确。

比如领域关系是"Order 属于 Customer"。压测问题可以是：

- 一个 Order 可以同时属于两个 Customer 吗？（客户合并场景）
- Customer 被删除后，历史 Order 怎么办？（数据生命周期）
- Order 的价格是下单时锁定，还是付款时重算？（时间边界）

这些问题不是为了刁难，而是为了**逼出关系定义中缺失的条款**。如果 "Order 属于 Customer" 没有回答上述任何一个问题，它就不是定义，是口号。

压测的价值在于**在写代码之前发现领域漏洞**。写代码后发现关系错了，改的是数据库表 + 服务逻辑 + 历史数据；写代码之前发现，改的只是一句话。

### 2.4 动作四：与代码交叉验证

术语不只是文档里对（文档可以自洽地错），还要和**代码对得上**。当用户描述某机制如何工作时，`domain-modeling` 会检查代码是否同意。

> 用户：这个系统支持部分取消订单。
> 模型（查代码）：你的代码在 cancelOrder 里取消整个 Order 的所有行项目——和你说的"部分取消"矛盾。哪个是对的？

这种验证是双向的：文档错了就改文档，代码错了就改代码。但无论改哪边，**术语表都必须先行**——因为术语表是"公约"，代码和文档都向它看齐。

交叉验证还有个习惯性动作：**发现代码用词与 CONTEXT.md 不一致时，不是默默容忍，而是记录为待修正项**，交给后续的 `expert-reviewer` 或 `coding-skill` 处理。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 220" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_d3b" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_d3b"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="220" fill="url(#bg_d3b)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">四个主动动作：会话中的"领域警察"</text>
  <rect x="40" y="55" width="170" height="95" rx="10" fill="#ef4444" opacity="0.10" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_d3b)"/>
  <text x="125" y="80" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">① 挑战术语</text>
  <text x="125" y="104" text-anchor="middle" fill="#94a3b8" font-size="10">用法 vs 词表冲突</text>
  <text x="125" y="124" text-anchor="middle" fill="#64748b" font-size="9">基于词表，非个人偏好</text>
  <rect x="230" y="55" width="170" height="95" rx="10" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_d3b)"/>
  <text x="315" y="80" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">② 精化语言</text>
  <text x="315" y="104" text-anchor="middle" fill="#94a3b8" font-size="10">过载词拆为精确词</text>
  <text x="315" y="124" text-anchor="middle" fill="#64748b" font-size="9">一词一概念，歧义归零</text>
  <rect x="420" y="55" width="170" height="95" rx="10" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_d3b)"/>
  <text x="505" y="80" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">③ 压测关系</text>
  <text x="505" y="104" text-anchor="middle" fill="#94a3b8" font-size="10">边界场景逼出定义</text>
  <text x="505" y="124" text-anchor="middle" fill="#64748b" font-size="9">写码前发现领域漏洞</text>
  <rect x="610" y="55" width="150" height="95" rx="10" fill="#3b82f6" opacity="0.10" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_d3b)"/>
  <text x="685" y="80" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">④ 交叉验证</text>
  <text x="685" y="104" text-anchor="middle" fill="#94a3b8" font-size="10">代码 vs 术语对质</text>
  <text x="685" y="124" text-anchor="middle" fill="#64748b" font-size="9">矛盾必须表面化</text>
  <text x="400" y="180" text-anchor="middle" fill="#475569" font-size="11">四个动作的共同点：发现问题当场指出，不等待、不积累、不默认"以后再改"</text>
  <text x="400" y="203" text-anchor="middle" fill="#64748b" font-size="10">被动记录 ≠ domain-modeling；主动质疑才是</text>
</svg>
```

## 三、文件结构：活词典和决策记录

### 3.1 CONTEXT.md：领域活词典

多数项目是单上下文，文件结构很简单：

```
.harness/
├── CONTEXT.md          ← 领域语言（活词典）
├── CONTEXT-FORMAT.md   ← 编写规范
└── wiki/
    ├── 架构决策.md      ← ADR 列表入口
    └── ADR-FORMAT.md   ← ADR 编写规范
```

CONTEXT.md 是**术语表**——每个词条一句话定义，仅此而已。它**完全不含实现细节**：不写字段名、不写 SQL、不写方法签名。它也不当规格、草稿或实现决策仓库。

为什么严格限定"只有术语"？因为**内容越杂，维护成本越高**。如果 CONTEXT.md 里混进实现细节，它就变成了"什么都有所以什么都不权威"的四不像；术语的定义会被淹没，大家在需要查词时懒得翻了。克制，是活词典能活下去的前提。

术语敲定后**当场**写进 CONTEXT.md，遵循 `CONTEXT-FORMAT.md`，**不批量累积**。当场写的理由前面说过：成本曲线。批量累积的坏处还有一个：**等你月底来整理，你早忘了当时为什么这么定义**。"当下"写，写的是鲜活的理由。

### 3.2 惰性创建：不预建空文件

`/domain-modeling` 的另一条纪律是**惰性创建**：只有有内容可写时才建文件。

- 首个术语敲定时才建 CONTEXT.md，而不是项目一开始就预建一个空文件。
- 第一个 ADR 真正需要时才建对应条目，而不是先建一堆空模板。

为什么？因为**空文件是"装饰性债务"**。项目里躺着一个空的 CONTEXT.md，会给人一种"领域语言已经治理过了"的错觉——而实际上什么也没定义。惰性创建保证：**文件存在 = 内容存在 = 已经有人在维护**。这也契合 harness 流水线整体的"不做事则不建物"哲学。

### 3.3 多上下文：CONTEXT-MAP.md

如果项目复杂到存在多个子域（比如订单域、库存域、支付域各自有自己的术语体系），根目录会出现 `CONTEXT-MAP.md`，它映射各上下文所在位置与关系——哪些词在这个上下文里是什么含义。多上下文里，"客户"在支付域和客服域可以有不同含义，但**上下文边界**要画清楚：同一个词跨上下文含义不同是允许的，但必须在同一个上下文内保持一致。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 230" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_d4" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_d4"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="230" fill="url(#bg_d4)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">文件结构：惰性创建的三件套</text>
  <rect x="60" y="52" width="210" height="105" rx="10" fill="#a855f7" opacity="0.10" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_d4)"/>
  <text x="165" y="78" text-anchor="middle" fill="#d8b4fe" font-size="13" font-weight="700">CONTEXT.md</text>
  <text x="165" y="102" text-anchor="middle" fill="#94a3b8" font-size="10">领域活词典 · 术语表</text>
  <text x="165" y="122" text-anchor="middle" fill="#64748b" font-size="9">首个术语敲定时才建</text>
  <text x="165" y="142" text-anchor="middle" fill="#64748b" font-size="9">零实现细节</text>
  <rect x="300" y="52" width="200" height="105" rx="10" fill="#3b82f6" opacity="0.10" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_d4)"/>
  <text x="400" y="78" text-anchor="middle" fill="#93c5fd" font-size="13" font-weight="700">ADR（wiki/）</text>
  <text x="400" y="102" text-anchor="middle" fill="#94a3b8" font-size="10">架构决策 · 三条件</text>
  <text x="400" y="122" text-anchor="middle" fill="#64748b" font-size="9">首个 ADR 需要时才建</text>
  <text x="400" y="142" text-anchor="middle" fill="#64748b" font-size="9">克制记录</text>
  <rect x="530" y="52" width="210" height="105" rx="10" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_d4)"/>
  <text x="635" y="78" text-anchor="middle" fill="#86efac" font-size="13" font-weight="700">CONTEXT-MAP.md</text>
  <text x="635" y="102" text-anchor="middle" fill="#94a3b8" font-size="10">多上下文映射（可选）</text>
  <text x="635" y="122" text-anchor="middle" fill="#64748b" font-size="9">子域各自术语体系时出现</text>
  <text x="635" y="142" text-anchor="middle" fill="#64748b" font-size="9">跨上下文允许同词异义</text>
  <text x="400" y="190" text-anchor="middle" fill="#475569" font-size="11">惰性创建：文件存在 = 内容存在 = 有人在维护。空文件 = 装饰性债务</text>
  <text x="400" y="212" text-anchor="middle" fill="#64748b" font-size="10">CONTEXT 贪婪、ADR 克制、MAP 按需——三层各守其职</text>
</svg>
```

### 3.4 惰性创建与"当场更新"的关系

有人会问：既然文件要惰性创建，为什么不干脆"攒一批术语一起写"？

这两个原则不矛盾，反而互补：

- **惰性创建**解决"文件何时诞生"：不预建空文件，避免装饰性债务。
- **当场更新**解决"内容何时写入"：文件一旦诞生，每次术语敲定**当场**写入，不批量累积。

所以正确流程是：讨论中敲定第一个术语 → 建 CONTEXT.md → 写入该术语 → 之后每敲定一个，当场追加。文件从诞生第一天起就是活的、准确的。

## 四、ADR：克制地记录架构决策

### 4.1 只有三个条件全满足，才建议创建 ADR

ADR（Architecture Decision Record）记录"为什么这么设计"。但 `domain-modeling` 对 ADR 的态度是**克制**——广告满天飞的 ADR 等于没有 ADR。

只有当三个条件**全都满足**才建议创建 ADR：

| 条件 | 判断问题 | 反面例子（不满足） |
|------|---------|------------------|
| ① 难逆转 | 改主意成本高吗？ | "临时起个名"——随时能改 |
| ② 脱离上下文会惊讶 | 未来读者会疑惑"为什么这么搞"吗？ | "选了标准的 HTTP 库"——不惊讶 |
| ③ 真实权衡 | 有真正替代方案且选了有特定理由吗？ | "顺手选的"——没有权衡 |

**任一缺失，跳过 ADR。** 这不是偷懒，而是保护 ADR 的稀缺性：

- 如果每条决策都写 ADR，未来读者就分不清哪些是"深思熟虑的架构选择"，哪些是"日常决定"。**当一切都重要时，一切都不重要。**
- 写 ADR 本身有成本：思考替代方案、记录理由、维护更新。这个成本只值得花在难逆转的决策上。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 260" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_d5" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_d5"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="260" fill="url(#bg_d5)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">ADR 三条件（AND 关系）</text>
  <rect x="60" y="50" width="210" height="110" rx="10" fill="#ef4444" opacity="0.10" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_d5)"/>
  <text x="165" y="76" text-anchor="middle" fill="#fca5a5" font-size="13" font-weight="700">① 难逆转</text>
  <text x="165" y="100" text-anchor="middle" fill="#94a3b8" font-size="10">改主意成本高</text>
  <text x="165" y="140" text-anchor="middle" fill="#64748b" font-size="9">可随时改 → 跳过</text>
  <rect x="300" y="50" width="200" height="110" rx="10" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_d5)"/>
  <text x="400" y="76" text-anchor="middle" fill="#fde68a" font-size="13" font-weight="700">② 脱离上下文会惊讶</text>
  <text x="400" y="100" text-anchor="middle" fill="#94a3b8" font-size="10">未来读者会问"为什么"</text>
  <text x="400" y="140" text-anchor="middle" fill="#64748b" font-size="9">一眼看懂 → 跳过</text>
  <rect x="530" y="50" width="210" height="110" rx="10" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_d5)"/>
  <text x="635" y="76" text-anchor="middle" fill="#86efac" font-size="13" font-weight="700">③ 真实权衡</text>
  <text x="635" y="100" text-anchor="middle" fill="#94a3b8" font-size="10">有替代方案</text>
  <text x="635" y="140" text-anchor="middle" fill="#64748b" font-size="9">没权衡过 → 跳过</text>
  <text x="400" y="180" text-anchor="middle" fill="#475569" font-size="12">AND：三条件全满足才写 ADR</text>
  <text x="400" y="205" text-anchor="middle" fill="#64748b" font-size="11">任一缺失 → 跳过。克制，是为了保住 ADR 的稀缺性与可读性</text>
  <text x="400" y="232" text-anchor="middle" fill="#64748b" font-size="10">当一切都重要时，一切都不重要</text>
</svg>
```

### 4.2 一个满足条件的例子

**场景**：订单模块决定"支付成功后订单状态由 PENDING 直接变为 PAID，不经过 CONFIRMED 中间态"。

- ① 难逆转？是。改状态机要动数据库枚举、接口契约、对账逻辑。
- ② 脱离上下文会惊讶？是。未来读者会问"为什么没有 CONFIRMED？"。
- ③ 真实权衡？是。我们考虑过"CONFIRMED 中间态"，但为了简化支付回调幂等，决定跳过。

三条件全满足 → 写 ADR。ADR 里记录：背景（支付回调解耦）、选项 A（保留中间态）、选项 B（直接迁移）、选择与理由、以及"如果将来要回退"的信号。

### 4.3 一个不满足条件的例子

**场景**："订单列表接口用 GET /orders。"

- ① 难逆转？否。改接口路径很痛苦但可逆。
- ② 脱离上下文会惊讶？否。GET + 集合资源是标准 REST 习惯。
- ③ 真实权衡？否。没有认真对比替代方案。

三条件无一满足 → 不写 ADR。它只是个日常决定，记在接口文档里就够了。

这两个例子的对比，就是 ADR 纪律的全部：**不是"重要的事"写 ADR，而是"满足三条件的决策"写 ADR**——标准是客观的，不由当下的情绪决定。

## 五、与流水线技能的联动

`/domain-modeling` 不是一个孤立阶段，它像胶水一样粘在整条流水线的关键接缝上。技能表里定义了四个触发场景：

| 触发场景 | 动作 |
|---------|------|
| `harnessing` 遇到模糊术语 | 调用本技能精化并写 CONTEXT.md |
| `coding-skill` 发现代码与 CONTEXT.md 矛盾 | 调用本技能修正术语 |
| 做出满足 ADR 条件的设计决策 | 调用本技能写 ADR |
| `expert-reviewer` 发现代码与领域语言不一致 | 标注为问题项 |

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 240" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_d6" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_d6"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="240" fill="url(#bg_d6)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">domain-modeling 在流水线中的联动</text>
  <rect x="60" y="52" width="170" height="80" rx="10" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_d6)"/>
  <text x="145" y="76" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">harnessing</text>
  <text x="145" y="98" text-anchor="middle" fill="#94a3b8" font-size="10">模糊术语 → 精化写词典</text>
  <line x1="230" y1="92" x2="260" y2="92" stroke="#475569" stroke-width="2"/>
  <polygon points="266,92 258,87 258,97" fill="#94a3b8"/>
  <rect x="275" y="52" width="170" height="80" rx="10" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_d6)"/>
  <text x="360" y="76" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">coding-skill</text>
  <text x="360" y="98" text-anchor="middle" fill="#94a3b8" font-size="10">代码矛盾 → 修正术语</text>
  <line x1="445" y1="92" x2="475" y2="92" stroke="#475569" stroke-width="2"/>
  <polygon points="481,92 473,87 473,97" fill="#94a3b8"/>
  <rect x="490" y="52" width="250" height="80" rx="10" fill="#3b82f6" opacity="0.10" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_d6)"/>
  <text x="615" y="76" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">expert-reviewer</text>
  <text x="615" y="98" text-anchor="middle" fill="#94a3b8" font-size="10">术语不一致 → 标注问题项</text>
  <rect x="60" y="150" width="680" height="50" rx="8" fill="#a855f7" opacity="0.10" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_d6)"/>
  <text x="400" y="170" text-anchor="middle" fill="#d8b4fe" font-size="12" font-weight="700">设计决策（难逆转 + 惊讶 + 权衡）</text>
  <text x="400" y="190" text-anchor="middle" fill="#94a3b8" font-size="10">→ 调用本技能写 ADR → wiki/</text>
  <text x="400" y="222" text-anchor="middle" fill="#64748b" font-size="10">每个接缝都是一次"改变模型"的机会——术语治理渗透进需求、编码、评审全流程</text>
</svg>
```

### 5.1 为什么是"调用"而不是"自己顺手做"

注意联动表里用的词是"调用本技能"。这背后是一个刻意设计：

- `harnessing` 遇到模糊术语时，**不自己定义**，而是交给 domain-modeling 走"对照词表→精化→当场写入"的完整流程。
- `expert-reviewer` 发现术语不一致时，**不现场修改**，而是标注为问题项，交回流水线处理。

为什么要这样？因为**术语治理需要统一的纪律和格式**。如果每个技能都顺手定义术语，CONTEXT.md 就会变成各写各的、格式混乱的大杂烩。把"改变模型"的动作收拢到一个技能里，才能保证：词表格式统一、写入时机正确（当场）、处置路径一致（挑战/精化/压测/验证四步法）。

这就是"单一职责"在治理层面的体现：**消费模型的技能可以很多，改变模型的技能只有一个**。

## 六、完成标志：怎样算"干完了"

`/domain-modeling` 的完成标志有三条，全部满足才算一次完整的领域治理：

1. **术语敲定后 CONTEXT.md 已更新（当场，不批量）**——不是"记住待办"，是文件里真的多了词条。
2. **满足条件的架构决策已补 ADR**——不是"觉得可以写"，是三条件核对过、确实写了。
3. **代码与领域语言无矛盾**——不是"应该一致"，是交叉验证过、没有遗留矛盾。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 200" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_d7" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_d7"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="200" fill="url(#bg_d7)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">三个完成标志：都是可验证的</text>
  <rect x="40" y="52" width="230" height="90" rx="10" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_d7)"/>
  <text x="155" y="76" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">① CONTEXT.md 已更新</text>
  <text x="155" y="100" text-anchor="middle" fill="#94a3b8" font-size="10">词条真的在文件里</text>
  <text x="155" y="120" text-anchor="middle" fill="#64748b" font-size="9">当场写，不批量</text>
  <rect x="290" y="52" width="230" height="90" rx="10" fill="#3b82f6" opacity="0.10" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_d7)"/>
  <text x="405" y="76" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">② 满足条件者已补 ADR</text>
  <text x="405" y="100" text-anchor="middle" fill="#94a3b8" font-size="10">三条件核对过</text>
  <text x="405" y="120" text-anchor="middle" fill="#64748b" font-size="9">该写的写了，不该写的没写</text>
  <rect x="540" y="52" width="220" height="90" rx="10" fill="#a855f7" opacity="0.10" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_d7)"/>
  <text x="650" y="76" text-anchor="middle" fill="#d8b4fe" font-size="12" font-weight="700">③ 代码与语言无矛盾</text>
  <text x="650" y="100" text-anchor="middle" fill="#94a3b8" font-size="10">交叉验证过</text>
  <text x="650" y="120" text-anchor="middle" fill="#64748b" font-size="9">无遗留矛盾项</text>
  <text x="400" y="176" text-anchor="middle" fill="#64748b" font-size="10">三条都没有"感觉"成分——每条都指向一个可检查的产物</text>
</svg>
```

这三条标志的设计刻意避开了"我认为"：不是说"我认为术语没问题了"，而是说"我更新了文件、核对了条件、验证了代码"。**完成标志是对"动作完成"的确认，不是对"质量完美"的声明。**

## 七、与相邻技能的边界

`/domain-modeling` 容易和几个技能混淆，划清边界很重要：

### 7.1 vs `harnessing`（需求烤问）

- `harnessing` 在**需求层面**打磨：目标是什么、范围是什么、验收怎么定。它产生的是一张需求卡。
- `domain-modeling` 在**语言层面**治理：概念怎么命名、关系怎么定义、决策怎么记录。它产生的是 CONTEXT.md 与 ADR。

两者经常协作：harnessing 烤问需求时冒出模糊术语 → 交给 domain-modeling 精化。但它们是两个技能：一个管"要做什么"，一个管"怎么说它"。

### 7.2 vs `handoff`（交接）

- `handoff` 把当前对话压缩成交接文档，服务"新对话无缝续接"。
- `domain-modeling` 维护长期项目资产（词表/ADR），服务"所有对话都讲同一种语言"。

handoff 是**会话级**产物（用完即走），domain-modeling 是**项目级**资产（长期演进）。CONTEXT.md 比任何一份 handoff 都长寿。

### 7.3 vs `arch-review`（架构评审）

- `arch-review` 扫描代码库找架构摩擦点，输出可视化报告——它是**发现问题**的工具。
- `domain-modeling` 记录决策、维护语言——它是**固化决策**的工具。

arch-review 发现架构问题后，如果要记录"为什么当时这么设计"，就会用上 domain-modeling 的 ADR。一个找问题，一个留证据。

## 八、写在最后

`/domain-modeling` 的全部设计可以浓缩成四句话：

1. **术语是把项目粘在一起的水泥**——水泥裂了，高楼就会晃；术语糊了，系统就会崩。
2. **在成本最低的当下修正**——术语敲定的那 1 分钟，是整条时间线上最便宜的修正点。
3. **改变模型是主动的**——挑战、精化、压测、交叉验证，四个动作缺一不可；被动记词不是本技能。
4. **克制是长期主义的保护**——CONTEXT 只装术语、ADR 三条件把关、文件惰性创建——每一条克制都在保护这两个文件的可信度。

一句话记住它：**/domain-modeling 是项目的语言立法机构——它不管代码怎么写，只管"我们说的每一句话都只有一个意思"。** 有了它，所有人都说同一种语言；没有它，每个人都在自己的方言里写代码，然后互相听不懂。