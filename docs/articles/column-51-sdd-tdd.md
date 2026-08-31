# SDD-TDD 方法论：先写规格、再写测试、后写代码——三层模式把"猜"变成"证明"

> 思想类专栏 · 第 51 篇 · 约 9000 字 · 6 个 SVG 图

---

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 320" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_t1" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_t1"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="320" fill="url(#bg_t1)" rx="10"/>
  <text x="400" y="30" text-anchor="middle" fill="#e2e8f0" font-size="26" font-weight="700">SDD-TDD 方法论：三层模式</text>
  <rect x="140" y="55" width="520" height="68" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_t1)"/>
  <text x="400" y="78" text-anchor="middle" fill="#93c5fd" font-size="14" font-weight="700">SDD 规格驱动开发</text>
  <text x="400" y="102" text-anchor="middle" fill="#94a3b8" font-size="10">定义"做什么" · 规格真相源是 change.md</text>
  <text x="400" y="148" text-anchor="middle" fill="#475569" font-size="14">▼</text>
  <rect x="140" y="165" width="520" height="68" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_t1)"/>
  <text x="400" y="188" text-anchor="middle" fill="#86efac" font-size="14" font-weight="700">TDD 测试驱动开发</text>
  <text x="400" y="212" text-anchor="middle" fill="#94a3b8" font-size="10">定义"怎么写对" · Red → Green → Refactor</text>
  <text x="400" y="258" text-anchor="middle" fill="#475569" font-size="14">▼</text>
  <rect x="140" y="275" width="520" height="32" rx="8" fill="#a855f7" opacity="0.12" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_t1)"/>
  <text x="400" y="297" text-anchor="middle" fill="#d8b4fe" font-size="13" font-weight="700">Harness 约束体系：怎么被约束、验证、留档、收口</text>
</svg>
```

## 一、为什么需要一套方法论

### 1.1 "AI 写代码"最大的坑是太快

传统的开发流程有一条隐含假设：**人先想清楚，再让机器执行**。AI 时代这个假设被打破了——AI 想得快、写得快，快到"还没想清楚就开始写"。

没有方法论约束时，AI 开发必然落入三个坑：

1. **猜需求**——一句话意图直接变成代码，做出来的东西不是用户要的；
2. **测不知道测什么**——代码写完才补测试，测试覆盖的是"代码做了什么"，不是"需求要求什么"；
3. **评审没有基准**——评审员不知道该拿什么对照，只能说"看起来没问题"。

三个坑的共同根源是：**把"做什么"和"怎么做"混在一起了**。

### 1.2 三层模式：层层递进，各司其职

Harness 体系用三层模式解决问题，层层递进：

| 层 | 回答的问题 | 真相源 |
|----|-----------|--------|
| SDD（规格驱动开发） | 做什么 | `change.md`（规格真相源） |
| TDD（测试驱动开发） | 怎么写对 | 失败测试 → 最小实现 → 重构 |
| Harness（约束体系） | 怎么被约束、验证、留档、收口 | `.harness/rules/` + 流水线 |

每一层都在回答上一层的"下一步"：SDD 定义清楚"做什么"，TDD 才谈得上"怎么写对"；TDD 把行为钉死在测试里，Harness 才能客观验证、留档、收口。

**SDD-TDD 方法论不是两条独立的方法，而是一条链**：规格 → 测试 → 实现 → 约束，缺一环，后面全是空中楼阁。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 260" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_t2" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_t2"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="260" fill="url(#bg_t2)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">完整链条：从业务模型到收口</text>
  <rect x="40" y="50" width="220" height="70" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_t2)"/>
  <text x="150" y="72" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">业务模型/接口协议/数据模型</text>
  <text x="150" y="96" text-anchor="middle" fill="#94a3b8" font-size="9">SDD 层 · 规格的输入</text>
  <rect x="40" y="140" width="220" height="70" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_t2)"/>
  <text x="150" y="162" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">change.md 规格真相源</text>
  <text x="150" y="186" text-anchor="middle" fill="#94a3b8" font-size="9">AC + 边界 + 约束 + 测试策略</text>
  <rect x="295" y="50" width="170" height="70" rx="8" fill="#ef4444" opacity="0.12" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_t2)"/>
  <text x="380" y="72" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">Red 失败测试</text>
  <text x="380" y="96" text-anchor="middle" fill="#94a3b8" font-size="9">先证明需求可测</text>
  <rect x="295" y="140" width="170" height="70" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_t2)"/>
  <text x="380" y="162" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">Green 最小实现</text>
  <text x="380" y="186" text-anchor="middle" fill="#94a3b8" font-size="9">刚好够用，不猜未来</text>
  <rect x="500" y="50" width="260" height="70" rx="8" fill="#a855f7" opacity="0.12" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_t2)"/>
  <text x="630" y="72" text-anchor="middle" fill="#d8b4fe" font-size="12" font-weight="700">Refactor 测试保护下重构</text>
  <text x="630" y="96" text-anchor="middle" fill="#94a3b8" font-size="9">改设计，不改行为</text>
  <rect x="500" y="140" width="260" height="70" rx="8" fill="#475569" opacity="0.25" stroke="#94a3b8" stroke-width="1.5" filter="url(#sh_t2)"/>
  <text x="630" y="162" text-anchor="middle" fill="#cbd5e1" font-size="12" font-weight="700">Review / CI / Verify 收口</text>
  <text x="630" y="186" text-anchor="middle" fill="#94a3b8" font-size="9">Harness 层 · 客观验证 + 留档</text>
  <text x="400" y="244" text-anchor="middle" fill="#475569" font-size="10">缺一环，后面全是空中楼阁</text>
</svg>
```

## 二、SDD——规格驱动开发

### 2.1 核心原则：先写规格，再写代码

SDD（Specification-Driven Development）的核心只有一句话：**先写规格，再写代码**。

规格的真相源是 `change.md`。它不是一份"需求文档"，而是一份**可执行的规格卡**，包含四类内容：

| 内容 | 作用 | 可验证性 |
|------|------|---------|
| **AC（验收条件）** | 每条 AC 对应一个或多个测试 | 每条 AC 可测 |
| **边界情况** | 至少 3 个边界情况 | 每个边界有测试 |
| **设计约束** | 技术选型、架构限制 | 可对照检查 |
| **测试策略** | 哪些测、哪些不测、怎么测 | 可执行 |

为什么是"真相源"？因为在后续每一个阶段——编码、测试、评审、CI、部署——**都以 change.md 为唯一基准**。评审对照 change.md 逐项检查，测试覆盖 change.md 的每条 AC，部署验证 change.md 的验收条件。任何偏离都是 Bug，不是"灵活处理"。

### 2.2 为什么先写规格？——可追溯性的起点

| 没有规格 | 有规格 |
|---------|-------|
| AI 猜需求，容易做错 | 规格明确，减少误解 |
| 测试不知道测什么 | 每条 AC 对应一个测试 |
| 评审没有基准 | 评审对照 change.md 逐项检查 |
| 上线后不知道是否满足需求 | 验收条件明确可验证 |

这四行对照的深层逻辑是：**没有规格，开发的所有后续活动都失去锚点**。测试没有锚点就会"测自己写的代码"，评审没有锚点就会"评看起来的代码"，上线没有锚点就会"验证想象中的需求"。

规格的本质是把"需求"从人脑中的模糊想法，变成仓库里的**可检查文档**——从这一刻起，开发才从"聊天"变成"工程"。

## 三、TDD——测试驱动开发

### 3.1 Red → Green → Refactor 循环

TDD 的核心是那个著名的三色循环：

**Red（写失败测试）**：

- 在接缝处写一个反映需求的小测试；
- 运行测试：确认它失败（RED）；
- 测试描述的是行为，不是实现。

**Green（最小实现）**：

- 写刚好够让测试通过的最小代码；
- 运行测试：确认它通过（GREEN）；
- 不要猜未来的需求，不要过度设计。

**Refactor（重构）**：

- 在测试保护下重构代码；
- 运行测试：确认仍然通过；
- 改善设计，不改变行为。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 320" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_t3" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_t3"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="320" fill="url(#bg_t3)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">Red → Green → Refactor 循环</text>
  <rect x="40" y="55" width="220" height="100" rx="8" fill="#ef4444" opacity="0.12" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_t3)"/>
  <text x="150" y="80" text-anchor="middle" fill="#fca5a5" font-size="14" font-weight="700">Red 失败测试</text>
  <text x="150" y="106" text-anchor="middle" fill="#94a3b8" font-size="9">在接缝处写小测试</text>
  <text x="150" y="126" text-anchor="middle" fill="#94a3b8" font-size="9">确认失败（RED）</text>
  <text x="150" y="146" text-anchor="middle" fill="#64748b" font-size="8">测试描述行为，不是实现</text>
  <text x="290" y="105" text-anchor="middle" fill="#64748b" font-size="14">→</text>
  <rect x="310" y="55" width="220" height="100" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_t3)"/>
  <text x="420" y="80" text-anchor="middle" fill="#86efac" font-size="14" font-weight="700">Green 最小实现</text>
  <text x="420" y="106" text-anchor="middle" fill="#94a3b8" font-size="9">刚好够通过的最小代码</text>
  <text x="420" y="126" text-anchor="middle" fill="#94a3b8" font-size="9">确认通过（GREEN）</text>
  <text x="420" y="146" text-anchor="middle" fill="#64748b" font-size="8">不猜未来，不过度设计</text>
  <text x="560" y="105" text-anchor="middle" fill="#64748b" font-size="14">→</text>
  <rect x="580" y="55" width="180" height="100" rx="8" fill="#a855f7" opacity="0.12" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_t3)"/>
  <text x="670" y="80" text-anchor="middle" fill="#d8b4fe" font-size="14" font-weight="700">Refactor 重构</text>
  <text x="670" y="106" text-anchor="middle" fill="#94a3b8" font-size="9">测试保护下重构</text>
  <text x="670" y="126" text-anchor="middle" fill="#94a3b8" font-size="9">确认仍然通过</text>
  <text x="670" y="146" text-anchor="middle" fill="#64748b" font-size="8">改善设计，不改行为</text>
  <rect x="250" y="185" width="300" height="50" rx="8" fill="#0ea5e9" opacity="0.12" stroke="#0ea5e9" stroke-width="1.5" filter="url(#sh_t3)"/>
  <text x="400" y="210" text-anchor="middle" fill="#7dd3fc" font-size="12" font-weight="700">循环往复 → 下一个行为</text>
  <text x="400" y="228" text-anchor="middle" fill="#94a3b8" font-size="9">一次只做一个循环，垂直切片</text>
  <rect x="80" y="255" width="640" height="40" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_t3)"/>
  <text x="400" y="279" text-anchor="middle" fill="#fde68a" font-size="11" font-weight="700">核心规则：先写失败测试 → 最小实现通过 → 测试保护下重构</text>
</svg>
```

### 3.2 垂直切片原则：一次一个循环

**一次只做一个 Red-Green-Refactor 循环。** 不要在同一个循环中处理多个行为。

| ✅ 正确做法 | ❌ 错误做法 |
|------------|------------|
| 写一个测试 → 让它通过 → 重构 → 下一个 | 先写所有测试 → 再写所有代码 → 最后重构 |
| 每个测试独立验证一个行为 | 一个测试验证多个行为 |
| 从 tracer bullet 开始，端到端打通一条路径 | 水平切片：所有层一起做，但只做一半 |

垂直切片的深层逻辑是**把风险切成小块**：每一块都有失败测试兜底，都独立可验证。如果一口气写完所有测试再写所有代码，测试验证的是"想象的行为"——你还没实现，怎么知道测试写对了？

## 四、三个反模式：一看就懂，一写就错

| 反模式 | 特征 | 后果 |
|--------|------|------|
| 实现耦合测试 | 重命名内部函数后测试崩溃 | 测试阻碍重构，失去价值 |
| 同义反复测试 | 期望值用代码相同方式计算得出 | 测试永远通过，什么也验证不了 |
| 水平切片 | 先批量写所有测试，再批量写代码 | 测试验证的是"想象的行为" |

### 4.1 实现耦合测试

**特征**：测试断言依赖实现细节——比如断言某个内部函数被调用、某个私有变量的值、某个中间状态。

**后果**：重命名内部函数后测试崩溃。测试本来应该保护重构，结果成了重构的阻力——重构时被迫改测试，改了测试，测试就失去"保护"的意义。

**正确姿势**：测试断言**外部可观察行为**，不触碰内部实现。行为的载体是公共 API 的输入输出，不是内部函数名。

### 4.2 同义反复测试

**特征**：期望值用与被测代码相同的方式计算得出。

**后果**：测试永远通过，什么也验证不了。典型例子：

```
// 被测代码
function total(items) { return items.reduce((s, x) => s + x.price, 0); }
// 测试
const expect = items.reduce((s, x) => s + x.price, 0);  // 同一算法
assert.equal(total(items), expect);  // 永远通过
```

**正确姿势**：期望值用**独立来源**计算——手写的字面常量、已知的数学结果、规格里写死的数字。

### 4.3 水平切片

**特征**：先批量写所有测试，再批量写所有代码，最后一次性重构。

**后果**：测试先写出来时，代码还不存在；等代码写完，测试已经"漂移"——它们验证的是当初想象的行为，而不是现在的行为。且中间没有反馈，错了就错一整批。

**正确姿势**：垂直切片——从 tracer bullet 开始，端到端打通一条路径，再逐步加厚。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 240" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_t4" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_t4"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="240" fill="url(#bg_t4)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">三个反模式：一看就懂，一写就错</text>
  <rect x="40" y="50" width="340" height="75" rx="8" fill="#ef4444" opacity="0.12" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_t4)"/>
  <text x="210" y="72" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">实现耦合测试</text>
  <text x="210" y="98" text-anchor="middle" fill="#94a3b8" font-size="9">断言内部实现 → 重构受阻</text>
  <rect x="420" y="50" width="340" height="75" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_t4)"/>
  <text x="590" y="72" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">同义反复测试</text>
  <text x="590" y="98" text-anchor="middle" fill="#94a3b8" font-size="9">期望值同一算法 → 永远通过</text>
  <rect x="40" y="145" width="340" height="75" rx="8" fill="#a855f7" opacity="0.12" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_t4)"/>
  <text x="210" y="167" text-anchor="middle" fill="#d8b4fe" font-size="12" font-weight="700">水平切片</text>
  <text x="210" y="193" text-anchor="middle" fill="#94a3b8" font-size="9">先全测试再全代码 → 验证想象的行为</text>
  <rect x="420" y="145" width="340" height="75" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_t4)"/>
  <text x="590" y="167" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">对应解法</text>
  <text x="590" y="193" text-anchor="middle" fill="#94a3b8" font-size="9">断言外部行为 · 独立期望值 · 垂直切片</text>
</svg>
```

## 五、什么时候用 Mock？——只在系统边界

Mock 只用于**系统边界**：

- 外部 API
- 时间（时钟）
- 随机数
- 文件系统（必要时）
- 数据库（必要时）

**不要 Mock 自己的模块。**

### 5.1 为什么只 Mock 边界？

Mock 的本质是"用一个替身代替真实依赖"。它有两个副作用：

1. **测试不再验证真实协作**——替身的行为由你定义，你定义的就是你期望的，测不出协作错误；
2. **测试与实现强耦合**——Mock 的调用方式暴露了实现细节，重构时 Mock 先碎。

所以 Mock 只留给**无法在测试里真实运行**的东西：外部 API（你控制不了网络）、时钟（你控制不了时间）、随机数（你控制不了概率）。

而自己的模块——**不要 Mock**。自己的模块能真实运行，就真实运行；耦合在一起的行为，就应该在集成测试里见真章。

### 5.2 Mock 边界判断清单

| 依赖 | 是否 Mock | 原因 |
|------|----------|------|
| 外部第三方 API | ✅ Mock | 网络不可控 |
| 系统时钟 | ✅ Mock | 时间不可控 |
| 随机数生成器 | ✅ Mock | 概率不可控 |
| 文件系统 | ⚠️ 必要时 | 本地可真实读写时优先真实 |
| 数据库 | ⚠️ 必要时 | 可用嵌入式/测试库时优先真实 |
| 自己项目的模块 | ❌ 不 Mock | 能真实运行，就要真实协作 |

## 六、SDD 与 TDD 如何衔接：从规格到测试的映射

### 6.1 change.md 里的四类内容全部落到测试

SDD 定义了"做什么"，TDD 定义了"怎么写对"，衔接的关键是**每一条规格都有测试承接**：

| change.md 内容 | TDD 承接方式 |
|----------------|-------------|
| 每条 AC | 一个或多个测试 |
| 边界情况 | 每个边界一个专门测试 |
| 设计约束 | 架构测试/静态检查（Harness 层兜底） |
| 测试策略 | 决定 Mock 边界与测试层级 |

"每条 AC 至少一个测试，每条降级路径一个专门测试"——这条铁律把 SDD 和 TDD 焊死在一起：**没有测试的 AC 是不完整的规格，没有 AC 的测试是脱缰的测试**。

### 6.2 核心规则（一条）

> **先写失败测试 → 最小实现通过 → 测试保护下重构。一次只做一个 Red-Green-Refactor 循环——垂直切片，不批量。**

这条规则是整个方法论的浓缩，拆开看有三个约束：

1. **顺序约束**：失败测试必须先于实现存在——先证明需求可测，再开始写；
2. **幅度约束**：最小实现——刚好够用，不猜未来需求，不过度设计；
3. **节奏约束**：一次一个循环，垂直切片——把风险切成小块，每块独立验证。

## 七、Harness 收口层：方法论如何在流水线里落地

SDD-TDD 是方法论，Harness 是它的执行环境。三层模式里，Harness 层负责"怎么被约束、验证、留档、收口"：

| 环节 | 方法论要求 | Harness 承接 |
|------|-----------|-------------|
| 规格 | change.md 是真相源 | `harnessing` 产出规格卡 |
| 测试先行 | 失败测试先存在 | `coding-skill` 先写失败测试 |
| 测试完善 | 覆盖全部 AC 与边界 | `unit-test-write` 覆盖率 ≥80% |
| 评审 | 对照规格逐项检查 | `expert-reviewer` 0 个 🔴 放行 |
| CI | 客观验证 | `unit-test-ci` 全绿门禁 |
| 部署 | 验证验收条件 | `deploy-verify` 冒烟+回滚预案 |

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 300" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_t5" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_t5"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="300" fill="url(#bg_t5)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">三层模式在流水线中的落地</text>
  <rect x="40" y="50" width="90" height="38" rx="6" fill="#3b82f6" opacity="0.15" stroke="#3b82f6" stroke-width="1.5"/>
  <text x="85" y="75" text-anchor="middle" fill="#93c5fd" font-size="11" font-weight="700">SDD 层</text>
  <rect x="40" y="96" width="90" height="38" rx="6" fill="#22c55e" opacity="0.15" stroke="#22c55e" stroke-width="1.5"/>
  <text x="85" y="121" text-anchor="middle" fill="#86efac" font-size="11" font-weight="700">TDD 层</text>
  <rect x="40" y="142" width="90" height="38" rx="6" fill="#a855f7" opacity="0.15" stroke="#a855f7" stroke-width="1.5"/>
  <text x="85" y="167" text-anchor="middle" fill="#d8b4fe" font-size="11" font-weight="700">Harness 层</text>
  <rect x="150" y="50" width="110" height="130" rx="8" fill="#1e293b" opacity="0.9" stroke="#3b82f6" stroke-width="1.5"/>
  <text x="205" y="80" text-anchor="middle" fill="#e2e8f0" font-size="11" font-weight="700">harnessing</text>
  <text x="205" y="105" text-anchor="middle" fill="#94a3b8" font-size="8">规格卡</text>
  <text x="205" y="127" text-anchor="middle" fill="#94a3b8" font-size="8">AC + 边界</text>
  <text x="205" y="149" text-anchor="middle" fill="#94a3b8" font-size="8">+ 测试策略</text>
  <rect x="280" y="50" width="110" height="130" rx="8" fill="#1e293b" opacity="0.9" stroke="#22c55e" stroke-width="1.5"/>
  <text x="335" y="80" text-anchor="middle" fill="#e2e8f0" font-size="11" font-weight="700">coding-skill</text>
  <text x="335" y="105" text-anchor="middle" fill="#94a3b8" font-size="8">失败测试先行</text>
  <text x="335" y="127" text-anchor="middle" fill="#94a3b8" font-size="8">最小实现</text>
  <rect x="410" y="50" width="110" height="130" rx="8" fill="#1e293b" opacity="0.9" stroke="#22c55e" stroke-width="1.5"/>
  <text x="465" y="80" text-anchor="middle" fill="#e2e8f0" font-size="11" font-weight="700">unit-test-write</text>
  <text x="465" y="105" text-anchor="middle" fill="#94a3b8" font-size="8">覆盖率 ≥80%</text>
  <text x="465" y="127" text-anchor="middle" fill="#94a3b8" font-size="8">AC 全覆盖</text>
  <rect x="540" y="50" width="110" height="130" rx="8" fill="#1e293b" opacity="0.9" stroke="#3b82f6" stroke-width="1.5"/>
  <text x="595" y="80" text-anchor="middle" fill="#e2e8f0" font-size="11" font-weight="700">expert-reviewer</text>
  <text x="595" y="105" text-anchor="middle" fill="#94a3b8" font-size="8">对照 change.md</text>
  <text x="595" y="127" text-anchor="middle" fill="#94a3b8" font-size="8">0 个 🔴 放行</text>
  <rect x="670" y="50" width="110" height="130" rx="8" fill="#1e293b" opacity="0.9" stroke="#a855f7" stroke-width="1.5"/>
  <text x="725" y="80" text-anchor="middle" fill="#e2e8f0" font-size="11" font-weight="700">unit-test-ci</text>
  <text x="725" y="105" text-anchor="middle" fill="#94a3b8" font-size="8">全绿门禁</text>
  <text x="725" y="127" text-anchor="middle" fill="#94a3b8" font-size="8">deploy-verify</text>
  <text x="725" y="149" text-anchor="middle" fill="#94a3b8" font-size="8">冒烟+回滚</text>
  <text x="400" y="260" text-anchor="middle" fill="#475569" font-size="11">SDD 产出规格 → TDD 产出行为 → Harness 产出证据</text>
  <text x="400" y="286" text-anchor="middle" fill="#64748b" font-size="9">每一层都要"可验证、可留档、可追溯"</text>
</svg>
```

## 八、完成标志与边界

### 8.1 完成标志

一套变更真正遵循 SDD-TDD 方法论，三件事必须成立：

1. **change.md 存在且被全程引用**——规格是真相源，编码、测试、评审都对照它；
2. **测试先于实现且能独立验证需求**——每条 AC 有测试，每个边界有测试，测试是"需求的证明"不是"代码的复述"；
3. **流水线无跳步**——harnessing → coding-skill → unit-test-write → expert-reviewer → unit-test-ci → deploy-verify，每步有产出、有门禁、有留档。

### 8.2 边界：方法论不管什么

- 不规定具体测试框架——JUnit、pytest、Go test 都行，方法论文档只管纪律；
- 不规定代码风格——那是编码规范的事；
- 不规定 UI 设计——那是产品的事；
- 方法论只管一件事：**怎么让"做什么、怎么写对、怎么证明"形成闭环**。

## 九、写在最后

SDD-TDD 方法论的全部设计，浓缩成四句话：

1. **先规格后代码**——change.md 是真相源，AC 可测试、边界 ≥3、约束明确、策略清晰。
2. **先测试后实现**——Red 证明需求可测，Green 最小实现，Refactor 保护下改进。
3. **垂直切片**——一次一个循环，风险切成小块，错误早暴露。
4. **三层收口**——SDD 定做什么，TDD 定怎么写对，Harness 定怎么验证留档。

一句话记住它：**SDD-TDD 方法论是开发节奏的"三层指挥棒"——SDD 把"做什么"写进 change.md，TDD 用 Red-Green-Refactor 把"怎么写对"钉进测试，Harness 用流水线把"可验证、可留档、可追溯"落进仓库——先写规格、再写测试、后写代码，让开发从"猜"变成"证明"。**