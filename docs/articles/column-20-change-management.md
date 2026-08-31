# 第二十章 · Change Management：多 Change 场景下的上下文锁定

> 一次只做一个变更，是理想；同时推进十个变更，是现实。
> 上下文锁定要解决的，就是"AI 面对多个变更时，凭什么知道自己此刻该做哪个、不该碰哪个"。
>
> —— 项目信条：**变更必须可追溯、可验证、可回滚；多变更共存时，上下文必须锁定，禁止模糊选择。**

---

## 一、为什么需要 Change Management

### 1.1 项目是流变的，上下文是易失的

人类团队管理变更靠 JIRA、PR、任务看板。AI 协作同样需要变更管理——否则：

```
无变更管理的灾难：
  ① 两个 Change 同时改同一个文件 → 互相覆盖
  ② AI 凭"上次对话记忆"工作 → 会话一断，上下文全丢
  ③ 改到一半的代码没人知道改到哪了 → 无法验收、无法回滚
  ④ review 时不知道"这次改了什么" → 评审失效
```

### 1.2 Change 的本质：一个可交付、可验证、可回滚的工作单元

```
Change = 最小可交付单元
  ├─ 有唯一 id（C-NNN）
  ├─ 有明确目标（用户故事 + 非目标）
  ├─ 有验收标准（AC 清单）
  ├─ 有边界（影响面 + 契约影响）
  ├─ 有生命周期（status：analyzing → coding → testing → reviewing → ci → verifying → done）
  └─ 有独立目录（.harness/changes/C-NNN/）
```

**一个仓库同时可能有 5-10 个 Change 在流转**——它们有的在编码、有的在等评审、有的卡在 CI。这时 AI 每次工作都必须回答同一个问题：

> **我现在做的是哪个 Change？我能不能碰这个文件？**

---

## 二、Change 的物理结构

### 2.1 目录即状态

```svg
<svg xmlns="http://www.w3.org/2000/svg" width="960" height="420" viewBox="0 0 960 420">
  <defs>
    <linearGradient id="bg20" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arr20" markerWidth="10" markerHeight="10" refX="9" refY="5" orient="auto">
      <path d="M 0 0 L 10 5 L 0 10 z" fill="#64748b"/>
    </marker>
  </defs>
  <rect width="960" height="420" fill="url(#bg20)"/>
  <text x="480" y="34" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">.harness/changes/：每个 Change 一个目录</text>

  <text x="80" y="80" fill="#7dd3fc" font-size="14" font-weight="700">.harness/changes/</text>

  <rect x="80" y="100" width="360" height="56" rx="8" fill="#0ea5e9" opacity="0.1" stroke="#38bdf8" stroke-width="1"/>
  <text x="100" y="122" fill="#e2e8f0" font-size="12" font-weight="700">2026-08-01-user-login/</text>
  <text x="100" y="142" fill="#94a3b8" font-size="10">change.md · review.md · verify.md —— status: coding</text>

  <rect x="80" y="166" width="360" height="56" rx="8" fill="#8b5cf6" opacity="0.1" stroke="#a78bfa" stroke-width="1"/>
  <text x="100" y="188" fill="#e2e8f0" font-size="12" font-weight="700">2026-08-01-order-refactor/</text>
  <text x="100" y="208" fill="#94a3b8" font-size="10">change.md —— status: testing</text>

  <rect x="80" y="232" width="360" height="56" rx="8" fill="#f59e0b" opacity="0.1" stroke="#fbbf24" stroke-width="1"/>
  <text x="100" y="254" fill="#e2e8f0" font-size="12" font-weight="700">2026-08-02-upgrade-common-lib/</text>
  <text x="100" y="274" fill="#94a3b8" font-size="10">change.md · review.md —— status: reviewing</text>

  <rect x="80" y="298" width="360" height="56" rx="8" fill="#10b981" opacity="0.1" stroke="#34d399" stroke-width="1"/>
  <text x="100" y="320" fill="#e2e8f0" font-size="12" font-weight="700">2026-08-03-metrics-endpoint/</text>
  <text x="100" y="340" fill="#94a3b8" font-size="10">change.md · verify.md —— status: verifying</text>

  <!-- 右侧：change.md 内容 -->
  <rect x="500" y="70" width="420" height="300" rx="12" fill="#1e293b" opacity="0.6" stroke="#334155" stroke-width="1"/>
  <text x="525" y="100" fill="#e2e8f0" font-size="14" font-weight="700">change.md（规格真相源）</text>
  <text x="525" y="128" fill="#94a3b8" font-size="11">id: C-014        ← 唯一标识</text>
  <text x="525" y="150" fill="#94a3b8" font-size="11">status: coding    ← 生命周期状态</text>
  <text x="525" y="172" fill="#94a3b8" font-size="11">--------------------</text>
  <text x="525" y="194" fill="#c4b5fd" font-size="11">用户故事 / 非目标 / AC</text>
  <text x="525" y="216" fill="#c4b5fd" font-size="11">边界情况 / 非功能需求</text>
  <text x="525" y="238" fill="#c4b5fd" font-size="11">设计约束 / 契约影响 / 影响面</text>
  <text x="525" y="260" fill="#c4b5fd" font-size="11">测试策略</text>
  <text x="525" y="290" fill="#64748b" font-size="10">所有技能只消费当前 status 匹配的</text>
  <text x="525" y="310" fill="#64748b" font-size="10">change —— 这就是上下文锁定的载体</text>
  <text x="525" y="340" fill="#fbbf24" font-size="11">变更定位规则：多候选时必须请示用户</text>
  <text x="525" y="360" fill="#fbbf24" font-size="11">禁止默认取第一个 / 随机选 / 合并处理</text>

  <text x="480" y="408" text-anchor="middle" fill="#64748b" font-size="16">目录即状态：看 .harness/changes/ 就知道项目在忙什么</text>
</svg>
```

### 2.2 change.md 的字段清单

每个字段都是为"锁定"服务的：

| 字段 | 作用 | 锁定什么 |
|------|------|---------|
| `id: C-NNN` | 唯一标识 | 全局可引用 |
| `slug` | 语义短名 | 人可读的引用 |
| `status` | 生命周期状态 | 阶段匹配的唯一依据 |
| 用户故事 | 目标（谁/要什么/为什么） | 定义"完成的含义" |
| 非目标（Out of Scope） | 明确**不做什么** | 防止范围蔓延 |
| 验收标准（AC） | 可验证断言 | 验收的真相源 |
| 边界情况 | 异常行为 | 测试的边界 |
| 非功能需求 | 性能/可靠性/安全 | 隐藏约束 |
| 设计约束 | 技术限制 | 不得越界 |
| 契约影响 | API/数据模型变更 | 影响面 |
| 影响面 | 模块/外部 API/wiki | 改动边界 |
| 测试策略 | 先写失败测试等 | 测试方向 |

### 2.3 变更三件套：change.md / review.md / verify.md

一个 Change 目录里不只有 change.md。每个 Change 完整生命周期会沉淀**三份文档**，分别在三个阶段产生，各自承担不同职责——这就是变更三件套：

```
.harness/changes/C-NNN/
  ├── change.md    ← ① harnessing 产出，全程是规格真相源
  ├── review.md    ← ④ expert-reviewer 产出，双轴评审报告
  └── verify.md   ← ⑥ deploy-verify 产出，部署验证与回滚预案
```

| 文档 | 产生阶段 | 谁产出 | 核心字段 | 锁定什么 |
|------|---------|--------|---------|---------|
| `change.md` | ① 需求分析 | harnessing | id / status / 用户故事 / 非目标 / AC / 边界 / 影响面 | 规格真相源，所有技能消费的锁定主体 |
| `review.md` | ④ 专家评审 | expert-reviewer | 🔴严重/🟡建议/🟢通过 / Spec轴+Standards轴 | 评审是否 0 严重问题，记录留档可追溯 |
| `verify.md` | ⑥ 部署验证 | deploy-verify | 部署拓扑 / 健康检查 / 冒烟 / 回滚预案 | 验证变更是否真正可交付，回滚是否可行 |

**为什么是三份而不是一份？** 因为三个阶段关注的问题完全不同：change.md 回答"要做什么"，review.md 回答"做对了吗"，verify.md 回答"上线后真的行吗"。把它们塞进一个文件，既臃肿又无法分阶段消费——评审时不需要看部署拓扑，部署时不需要重读 AC 清单。

### 2.4 review.md：双轴评审报告的真实结构

`review.md` 是 expert-reviewer 技能产出的评审记录，它不是自由文本，而是严格的双轴结构化报告——这与本系列"双轴评审"章节的设计一致：

```markdown
# 📋 评审报告: C-NNN

## 总览
- 审查文件: N 个
- 🔴 严重问题: N（必须修复）
- 🟡 建议改进: N（推荐修复）
- 🟢 通过项: N

## Spec 轴报告（需求匹配）
> 对照 change.md 检查代码是否做了该做的事，且没做不该做的事。
### 🔴 严重问题
### 1. <标题>
- 文件: <路径>:<行号>
- 问题: <描述>
- 违反: <change.md 条目>      ← 锁定回规格
- 修复建议: <可操作方案>

## Standards 轴报告（规范合规）
> 对照 .harness/rules/ 和代码质量基线检查代码是否合规。

## 测试质量

## 结论
<一句话结论>
```

**review.md 的锁定价值**：每个 🔴 严重问题必须标明"违反了 change.md 的哪一条"——评审不是主观判断，而是拿规格去对照代码。如果一条严重问题写不出对应的 change.md 条目，说明它要么是 scope creep（规格里没有却做了），要么是规格遗漏（该写的没写）。

### 2.5 verify.md：部署验证与回滚预案

`verify.md` 是 deploy-verify 技能产出的交付验证报告。它回答"这个变更真的能上线、上线后真的健康、出问题能回滚"三个问题：

```markdown
# ✅ 部署验证报告: C-NNN

## 环境
- profile: dev / 镜像: …

## 部署拓扑（Mermaid）
graph LR
    GW[网关] --> SVC[服务]
    SVC --> DB[(数据库)]

## 健康检查
- [x] 健康检查端点 = UP

## 冒烟测试
| 链路 | 结果 |
| 主链路 | 🟢 |
| 降级链路 | 🟢 |

## 回滚预案
- 上一稳定版本: <tag>
- 回滚命令: <cmd>
- 触发条件: <…>

## 结论
✅ 验证通过，变更可交付 / ❌ 失败，退回 …
```

**verify.md 的锁定价值**：回滚预案是"变更可交付"的硬性前提——没有回滚命令和触发条件的变更，不准标记 `done`。这把"可回滚"从口号变成了磁盘上的可验证字段。

---

## 三、上下文锁定：多 Change 场景的核心机制

### 3.1 什么是"上下文锁定"

**上下文锁定 = 每次工作开始时，先确定"当前 Change 是谁"，然后把 AI 的注意力锁定在这个 Change 的规格上，禁止跨 Change 操作。**

```
  工作的上下文来源（优先级从高到低）：
  ① 当前 change.md（本次变更的目标 + AC + 边界）  ← 锁定主体
  ② CONTEXT.md（领域模型，跨变更稳定）             ← 稳定背景
  ③ rules/（工程纪律，跨变更稳定）                 ← 稳定背景
  ④ 会话历史（仅辅助，不当作真相源）               ← 易失，只参考
```

**关键：change.md 是易变的（每个 Change 一份），CONTEXT.md 和 rules/ 是稳定的。锁定的重点是"锁定在正确的 change.md 上"，而不是"锁定在对话记忆里"。** 会话一断，靠 change.md 找回上下文。

### 3.2 迭代替换 vs 并行共存

```svg
<svg xmlns="http://www.w3.org/2000/svg" width="720" height="170" viewBox="0 0 720 170">
  <defs>
    <linearGradient id="bg20a" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
  </defs>
  <rect width="720" height="170" fill="url(#bg20a)"/>
  <text x="180" y="28" text-anchor="middle" fill="#7dd3fc" font-size="14" font-weight="700">串行（理想，小项目）</text>
  <text x="540" y="28" text-anchor="middle" fill="#fbbf24" font-size="14" font-weight="700">并行（现实，大型项目）</text>

  <rect x="30" y="42" width="300" height="95" rx="10" fill="#0ea5e9" opacity="0.1" stroke="#38bdf8" stroke-width="1.5"/>
  <text x="180" y="70" text-anchor="middle" fill="#e2e8f0" font-size="13">Change A → B → C</text>
  <text x="180" y="96" text-anchor="middle" fill="#94a3b8" font-size="12">一次只做一个</text>
  <text x="180" y="118" text-anchor="middle" fill="#94a3b8" font-size="12">上下文天然单一</text>
  <text x="180" y="152" text-anchor="middle" fill="#34d399" font-size="12">无需锁定</text>

  <rect x="390" y="42" width="300" height="95" rx="10" fill="#f59e0b" opacity="0.1" stroke="#fbbf24" stroke-width="1.5"/>
  <text x="540" y="70" text-anchor="middle" fill="#e2e8f0" font-size="13">Change A ║ B ║ C ║ D</text>
  <text x="540" y="96" text-anchor="middle" fill="#94a3b8" font-size="12">同时流转，各在不同阶段</text>
  <text x="540" y="118" text-anchor="middle" fill="#94a3b8" font-size="12">上下文必须显式锁定</text>
  <text x="540" y="152" text-anchor="middle" fill="#f43f5e" font-size="12">锁定是刚需</text>
</svg>
```

**为什么并行时会混乱？** 因为多个 change.md 都躺在 `.harness/changes/` 里，AI 如果不去读定位规则，就可能：

```
❌ 把 Change B 的代码改在 Change A 的目录下
❌ 把只属于 Change A 的文件，当成 Change C 的影响面
❌ review 时评审了错误的 diff
❌ 合并时把未完成 Change 的代码带进去
```

### 3.3 锁定机制：变更定位规则（代码实证）

源码 `skills/harness-core/rules/变更定位规则.md` 定义了硬规则，这正是上下文锁定的具体代码：

```
步骤 1: 是否已显式指定 <id>？
  ├─ 是（如 /coding-skill C-002）
  │   └─ 校验该 change 的 status 匹配当前阶段 → 匹配则用它，否则报错
  └─ 否 → 步骤 2

步骤 2: 扫描候选：列出 status 匹配当前阶段的 change

步骤 3: 数量判断
  ├─ 恰好 1 个 → 锁定它，开始工作
  ├─ 0 个       → 报错"无符合条件的 change"，退回上一阶段，不得继续
  └─ ≥ 2 个     → 步骤 4

步骤 4: 多候选处理（≥2 个）—— 上下文锁定最关键的规则
  1. 列出所有候选（id + 标题 + 状态 + 摘要首句）
  2. 停下，请用户指定要处理哪个 change
  3. 未得到明确选择前，禁止继续执行
```

**这条规则的价值**：AI 永不猜测。多候选时它宁可停下来问，也不会自作主张——因为"猜错 Change"比"慢一步"昂贵得多。

### 3.4 锁定后的"夹具"：status 与阶段的严格对应

| 阶段 | 技能 | 只消费 status |
|------|------|--------------|
| 需求/分析 | harnessing | `analyzing` |
| ② 编码实现 | `coding-skill` | `coding` |
| ③ 单测编写 | `unit-test-write` | `testing` |
| ④ 专家评审 | `expert-reviewer` | `reviewing` |
| ⑤ CI 门禁 | `unit-test-ci` | `ci` |
| ⑥ 部署验证 | `deploy-verify` | `verifying` |

**status 是锁的"钥匙孔"**——只有钥匙孔匹配的 change 才会被 AI 拾取，其余全部忽略。这从机制上防止了"做错 Change"。

### 3.5 完整状态机：draft 到 done 的全路径（含打回回路）

变更状态机不是单向直线，它有 8 个正向状态 + 1 个占位状态 + 打回回路。`开发流程规范.md` 的定义是：

```
draft → analyzing → coding → testing → reviewing → ci → verifying → done
                                                  └─(打回)─┘
```

| 状态 | 含义 | 谁推进到下一状态 |
|------|------|----------------|
| `draft` | 占位变更卡：只写标题和一句话目标，还没进入流水线 | 人类确认后 → `analyzing` |
| `analyzing` | harnessing 正在构建规格卡 | 人类审批 → `coding`（默认不得擅越） |
| `coding` | coding-skill 正在实现 | Owner Agent → `testing` |
| `testing` | unit-test-write 正在补测试 | Owner Agent → `reviewing` |
| `reviewing` | expert-reviewer 正在双轴评审 | 0 严重 → `ci`；有严重问题 → 打回 |
| `ci` | CI 门禁全量检查 | Owner Agent → `verifying` |
| `verifying` | deploy-verify 部署验证 | 验证通过 → `done`；失败 → 退回 |
| `done` | 交付完成，回写 changes/ 状态 | — |

**三个关键规则**：

1. **`draft` 是占位，不是进入流水线**——它只写标题和一句话目标，还没有 AC、没有影响面。任何技能都不会消费 `draft` 状态的 change（没有 status 匹配的技能）。
2. **`analyzing → coding` 必须人类审批**——这是状态机里唯一需要人类推进的节点。默认规则：没有人类审批，不得从 `analyzing` 进入 `coding`。其他状态切换由 Owner Agent 自主完成。
3. **打回不是跳到开头**——评审/CI/验证打回时，按问题性质回退到对应上游状态（评审发现需求不清 → `analyzing`；发现实现有 bug → `coding`），并在相关文件记录原因，不是一切重来。

### 3.6 状态机与锁定机制的配合

状态机给 Change "生命周期"，变更定位规则给 Change "锁定机制"，两者配合的完整图景：

```
状态机回答：“这个 Change 现在在哪个阶段？”
变更定位规则回答：“AI 现在应该锁定哪个 Change？”

两者交集 = AI 永远知道：自己在哪个阶段、做哪个 Change、什么算完成。
```

**状态机是"时钟"，定位规则是"锁"**：时钟推进决定哪个 status 活跃，锁决定 AI 只能干那个 status 的 Change。如果时钟走错了（比如 `analyzing` 没审批就跳 `coding`），锁也会拒绝——因为变更定位规则会校验 status 是否匹配当前阶段。

---

## 四、多 Change 的完整工作循环

### 4.1 一次规范的多 Change 协作

```svg
<svg xmlns="http://www.w3.org/2000/svg" width="720" height="140" viewBox="0 0 720 140">
  <defs>
    <linearGradient id="bg20b" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
  </defs>
  <rect width="720" height="140" fill="url(#bg20b)"/>
  <text x="360" y="22" text-anchor="middle" fill="#94a3b8" font-size="11">时间线（两个 Change 并行）</text>

  <rect x="60" y="32" width="620" height="42" rx="8" fill="#0ea5e9" opacity="0.1" stroke="#38bdf8" stroke-width="1"/>
  <text x="80" y="55" fill="#7dd3fc" font-size="12" font-weight="700">C-014 user-login</text>
  <text x="260" y="55" fill="#94a3b8" font-size="11">analyzing → coding → testing → reviewing → ci → done</text>

  <rect x="60" y="82" width="620" height="42" rx="8" fill="#f59e0b" opacity="0.1" stroke="#fbbf24" stroke-width="1"/>
  <text x="80" y="105" fill="#fde68a" font-size="12" font-weight="700">C-015 order-refactor</text>
  <text x="260" y="105" fill="#94a3b8" font-size="11">analyzing → coding → testing → ...</text>
</svg>
```

### 4.2 锁定的"影响面隔离"

每个 change.md 里有**影响面**字段。锁定的语义包括：

```
✅ 可碰：change.md 影响面列出的文件
✅ 可读：CONTEXT.md / rules/ / wiki / 其他 change 的 change.md（只读）
❌ 不可碰：其他 Change 影响面内的文件
❌ 不可改：本 Change 影响面之外的文件
```

**"只读队友的规格，不写队友的地盘"**——这是并行协作的边界纪律。

### 4.3 锁定的解除

Change 完成（status → done）后：

- 锁定自动解除（该 change 不再匹配任何阶段的 status）
- review.md / verify.md 归档
- 影响面从"活跃"状态变为"历史"

### 4.4 锁定背后的人机协同：5 个必须暂停请示的触发点

上下文锁定解决的是"AI 做哪个 Change"，但锁定之后，AI 在执行中还有一套人机协同纪律——哪些事 AI 可以自己做，哪些必须停下来请人。`开发流程规范.md` 定义了**5 个必须暂停请示的触发点**：

```
触发点 1：需求模糊到无法确定实现方式
触发点 2：设计方案在实现时被证伪
触发点 3：需要新增依赖 / 改 public API / 改架构
触发点 4：发现安全风险
触发点 5：触碰生产配置或密钥
```

**为什么锁定之后还要人机协同？** 因为锁定保证 AI 做对了 Change，但不保证 AI 在 Change 内部不越界。AI 可以自主写代码、跑测试、改文档，但遇到这 5 个触发点，必须停下请示——它们都涉及"超出当前规格授权"的决策：

- **需求模糊**：规格不够清楚，需要人类澄清，不是 AI 猜。
- **方案证伪**：设计约束在实现中发现行不通，需要人类重新决策。
- **改 API/架构**：这是契约变更，影响面超出当前 change.md 的影响面定义。
- **安全风险**：这是必须立即暴露的问题，不允许 AI 自行处理。
- **生产配置/密钥**：这是人类的地盘，AI 不得触碰。

**与锁定的关系**：锁定是"做对 Change"的保障，人机协同是"在 Change 内部做对决策"的保障。两者叠加，AI 既不会跨 Change 越界，也不会在 Change 内部越权。

### 4.5 特殊场景：流水线可以裁剪，但 Change 不能省

不是所有变更都要走完整 6 阶段。`开发流程规范.md` 定义了 4 种裁剪场景：

| 场景 | 可简化什么 | 不可省什么 |
|------|-----------|------------|
| **Hotfix（紧急修复）** | 可简化 ①，但必须最小化变更卡（修复描述 + AC） | ④ 评审 + ⑤ CI 不可省 |
| **Spike（技术探索）** | 可跳过完整 ①②③ | 探索结论必须沉淀为 ADR 或 wiki 更新 |
| **棕地修改（改已有功能）** | 先更新对应 wiki/changes，再走 ②~⑥ | 不得直接改代码 |
| **纯文档变更** | 可跳过 ②③⑤⑥ | 必须走 ① 需求卡 + ④ 评审 |

**裁剪原则**：可以裁剪阶段，但**不能裁剪 Change 本身**——即使 Hotfix 也要有最小变更卡，即使 Spike 也要有沉淀产出。没有 Change 的变更就是失管变更：无法追溯、无法回滚、无法验收。

> 💡 **裁剪 ≠ 免除**：裁剪的是阶段数量，不是变更纪律。被裁剪的场景反而更要用影响面锁死边界——Hotfix 不做影响面分析，就可能修一个 bug 带出三个 bug。

---

## 五、代码证据：模板与规则的配合

### 5.1 模板定义"长什么样"

`changes/_TEMPLATE/change.md` 定义了规格的骨架（frontmatter + 章节）：

```markdown
---
id: C-NNN
slug: <slug>
status: analyzing
created: <date>
---

# C-NNN <标题>
## 用户故事 / ## 非目标 / ## 验收标准（AC）
## 边界情况 / ## 非功能需求 / ## 设计约束
## 契约影响 / ## 影响面 / ## 测试策略
```

### 5.2 规则定义"怎么用"

`rules/变更定位规则.md` 定义了消费方式（谁在哪个阶段、匹配哪个 status、多候选怎么办）。

**模板 + 规则 = 上下文锁定的完整实现**：

```svg
<svg xmlns="http://www.w3.org/2000/svg" width="680" height="150" viewBox="0 0 680 150">
  <defs>
    <linearGradient id="bg20c" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arr20c" markerWidth="12" markerHeight="12" refX="10" refY="6" orient="auto">
      <path d="M 0 0 L 12 6 L 0 12 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="680" height="150" fill="url(#bg20c)"/>
  <text x="110" y="24" text-anchor="middle" fill="#7dd3fc" font-size="13" font-weight="700">模板（静态）</text>
  <text x="340" y="24" text-anchor="middle" fill="#fbbf24" font-size="13" font-weight="700">规则（动态）</text>
  <text x="570" y="24" text-anchor="middle" fill="#34d399" font-size="13" font-weight="700">结果</text>

  <rect x="20" y="36" width="180" height="90" rx="10" fill="#0ea5e9" opacity="0.1" stroke="#38bdf8" stroke-width="1.5"/>
  <text x="110" y="62" text-anchor="middle" fill="#e2e8f0" font-size="12">change.md</text>
  <text x="110" y="86" text-anchor="middle" fill="#94a3b8" font-size="11">数据结构</text>
  <text x="110" y="108" text-anchor="middle" fill="#94a3b8" font-size="11">status 字段</text>

  <line x1="200" y1="80" x2="250" y2="80" stroke="#475569" stroke-width="2.5" marker-end="url(#arr20c)"/>

  <rect x="260" y="36" width="180" height="90" rx="10" fill="#f59e0b" opacity="0.1" stroke="#fbbf24" stroke-width="1.5"/>
  <text x="350" y="62" text-anchor="middle" fill="#e2e8f0" font-size="12">变更定位规则</text>
  <text x="350" y="86" text-anchor="middle" fill="#94a3b8" font-size="11">阶段↔status</text>
  <text x="350" y="108" text-anchor="middle" fill="#94a3b8" font-size="11">多候选请示</text>

  <line x1="440" y1="80" x2="490" y2="80" stroke="#475569" stroke-width="2.5" marker-end="url(#arr20c)"/>

  <rect x="500" y="36" width="160" height="90" rx="10" fill="#10b981" opacity="0.1" stroke="#34d399" stroke-width="1.5"/>
  <text x="580" y="62" text-anchor="middle" fill="#e2e8f0" font-size="12">AI 总是锁定在</text>
  <text x="580" y="86" text-anchor="middle" fill="#94a3b8" font-size="11">正确且唯一的</text>
  <text x="580" y="108" text-anchor="middle" fill="#94a3b8" font-size="11">Change 上干活</text>
</svg>
```

### 5.3 违反锁定的四种行为（禁止清单，源码实证）

```
❌ 多候选时默认取第一个        → 可能做错 Change
❌ 多候选时随机选一个          → 完全不可预测
❌ 多候选时合并处理            → 把两个 Change 搅在一起
❌ 无候选时跳过前置检查继续执行 → 没有规格也硬做
```

**这四条禁止规则，是"上下文锁定"最锋利的牙齿**——没有后果的规则只是建议。

---

## 六、多 Change 锁定的工程实践

### 6.1 实践一：先在对话中确认 Change

用户说"接着做订单模块"时，AI 的正确反应：

```
❌ 直接开始写代码（假设是唯一 Change）
✅ "当前有 C-014（user-login, coding）和 C-015（order-refactor, testing）
    两个 Change。你说的'订单模块'，是指继续 C-015 吗？"
```

**先确认锁定目标，再动手。** 这也正是 `变更定位规则.md` 步骤 4 的要求。

### 6.2 实践二：每个技能的入口都要锁定

```
coding-skill   → 只消费 status=coding 的 Change
unit-test-write → 只消费 status=testing 的 Change
expert-reviewer → 只消费 status=reviewing 的 Change
unit-test-ci   → 只消费 status=ci 的 Change
deploy-verify  → 只消费 status=verifying 的 Change
```

入口即锁，阶段即界。

### 6.3 实践三：上下文断点续传

会话丢失后的恢复路径（全部来自磁盘，不靠记忆）：

```
1. ls .harness/changes/*/change.md  → 看所有 Change 及 status
2. 用户说"继续" → 按锁定规则确认目标 Change
3. 读该 change.md → 恢复规格上下文
4. 读 review.md / verify.md → 恢复进度
5. 继续对应阶段的技能
```

**change.md 就是 AI 的"记忆外置"**——会话可以丢，规格不能丢。

### 6.4 实践四：变更粒度控制

```svg
<svg xmlns="http://www.w3.org/2000/svg" width="720" height="150" viewBox="0 0 720 150">
  <defs>
    <linearGradient id="bg20d" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
  </defs>
  <rect width="720" height="150" fill="url(#bg20d)"/>
  <text x="180" y="28" text-anchor="middle" fill="#f43f5e" font-size="14" font-weight="700">Change 太大（坏）</text>
  <text x="540" y="28" text-anchor="middle" fill="#f43f5e" font-size="14" font-weight="700">Change 太小（坏）</text>

  <rect x="40" y="42" width="280" height="80" rx="10" fill="#f43f5e" opacity="0.1" stroke="#f43f5e" stroke-width="1.5"/>
  <text x="180" y="68" text-anchor="middle" fill="#e2e8f0" font-size="12">"重构整个用户模块"</text>
  <text x="180" y="92" text-anchor="middle" fill="#94a3b8" font-size="11">影响面=全仓库</text>
  <text x="180" y="112" text-anchor="middle" fill="#94a3b8" font-size="11">永远完不成</text>

  <rect x="400" y="42" width="280" height="80" rx="10" fill="#f43f5e" opacity="0.1" stroke="#f43f5e" stroke-width="1.5"/>
  <text x="540" y="68" text-anchor="middle" fill="#e2e8f0" font-size="12">"把日志改成英文"</text>
  <text x="540" y="92" text-anchor="middle" fill="#94a3b8" font-size="11">无法独立验收</text>
  <text x="540" y="112" text-anchor="middle" fill="#94a3b8" font-size="11">变更泛滥</text>

  <text x="360" y="140" text-anchor="middle" fill="#fbbf24" font-size="12">✅ 建议粒度：可独立验收、可独立回滚、通常 1-2 天完成</text>
</svg>
```

### 6.5 实践五：用影响面做冲突预警

两个 Change 影响面重叠时（比如都改 `internal/user/service.go`）：

```
冲突预警流程：
1. AI 扫描当前活跃 Change 的影响面
2. 发现重叠 → 主动提示："C-014 与 C-015 都涉及 user/service.go"
3. 建议：调整粒度 / 排队串行 / 明确所有权
4. 未解决前，不开始编码
```

**锁定不等于死锁**——锁定是明确"谁管哪块"，冲突时协调，而不是各改各的。

---

## 七、一次完整的多 Change 演示

### 7.1 场景

仓库有两个活跃 Change：

```
C-014  user-login        status: coding
C-015  metrics-endpoint  status: analyzing
```

### 7.2 演示：coding-skill 的锁定过程

```
用户：/coding-skill
AI  （读变更定位规则）：
  → 步骤 1：未显式指定 id
  → 步骤 2：扫描 status=coding 的候选 → 只有 C-014
  → 步骤 3：恰好 1 个 → 锁定 C-014
  → 读取 .harness/changes/2026-08-01-user-login/change.md
  → 开始编码，只碰影响面内的文件
  → 完成 coding 后，把 status 更新为 testing
```

### 7.3 演示：expert-reviewer 的锁定过程

```
用户：/expert-reviewer
AI  （读变更定位规则）：
  → 步骤 2：扫描 status=reviewing 的候选
     发现 C-014（testing→reviewing）与 C-015 都在 reviewing
  → 步骤 4：≥ 2 个 → 停下，列出两个候选，请示用户
  → 用户：评审 C-014
  → 锁定 C-014，读取 change.md + 代码 diff，开始评审
```

**注意：哪怕只有 0.1% 的歧义，也要请示**——这是规则的字面要求，也是工程上的正确选择。

---

## 八、失败案例复盘

### 8.1 案例：多候选默认取第一个

```
背景：C-016（重构支付模块）和 C-017（修支付 bug）同时处于 coding
事故：AI 默认取了 C-016，改了支付模块结构
结果：C-017 的修复被结构改动覆盖，线上支付异常 2 小时
教训：多候选必须请示；"修 bug" 不该在"重构"的 Change 里进行
```

### 8.2 案例：跨 Change 改文件

```
背景：C-018（升级日志库）影响面含 logging-toolkit
事故：C-019（加审计日志）顺手改了 logging-toolkit 的接口
结果：C-018 验收时发现接口变了，两个 Change 纠缠不清
教训：影响面外的文件，宁可请示也不要碰
```

### 8.3 案例：无候选还继续

```
背景：harnessing 还没产出任何 Change
事故：用户说"帮我加点测试"，AI 没有 change.md 也硬写
结果：测试没有规格依据，AC 对不上，白写
教训：没有候选就退回上一阶段，"没有规格就不写代码"
```

### 8.4 案例：analyzing 未审批就跳 coding

```
背景：C-020（加缓存层）刚由 harnessing 写完规格卡，status=analyzing
事故：用户说"赶紧写吧"，AI 没等人类审批就把 status 改成 coding 开工
结果：用户其实对缓存策略有异议（想用本地缓存而非 Redis），
      AI 已经按 Redis 写完了实现和测试，全部返工
教训：analyzing→coding 是状态机里唯一需要人类审批的节点，
      "赶紧写" 不等于"已审批"——AI 应回应"请确认规格后再开工"
```

**为什么这个节点要人类把关？** 因为 ① 需求分析阶段的产出是规格卡，它定义了"做什么"和"不做什么"。如果 AI 自己跳过审批开工，等于 AI 自己定义了需求范围——这与"人类是意图指挥官"的原则背道而驰。审批的本质是人类确认"这个规格就是我要的"，然后才授权 AI 去实现。

### 8.5 案例：评审打回后直接跳回 coding（该回 analyzing）

```
背景：C-021（订单退款）在 reviewing 阶段，expert-reviewer 发现
      Spec 轴严重问题：退款金额计算逻辑与 change.md 的 AC-2 不符
事故：AI 把 status 直接打回 coding，按评审意见改了计算逻辑
结果：改完后用户发现新逻辑不符合业务规则——
      根本问题是 AC-2 本身写错了（规格问题，不是实现问题）
教训：打回不是一律跳 coding，要按问题性质回退：
      - 需求不清 / AC 错误 → 回 analyzing（重写规格）
      - 实现 bug → 回 coding（修代码）
      - 测试遗漏 → 回 testing（补测试）
```

**打回的精准原则**：打回不是"重来"，是"回退到问题根源对应的阶段"。需求错回 analyzing，实现错回 coding，测试错回 testing——否则会把规格问题当实现问题修，越修越错。

### 8.6 案例：Hotfix 不做影响面分析

```
背景：线上支付超时，需要紧急修复
事故：为了快，直接跳过 ① 的影响面分析，只写了修复描述 + AC
结果：修复改了 payment/client.go 的超时参数，
      但该文件同时被对账模块依赖，修复后对账模块超时调优全失效
教训：Hotfix 可以简化 ①，但不能省影响面——
      Hotfix 反而更需要影响面分析，因为紧急修复改错文件的代价更大
```

**裁剪的边界**：特殊场景裁剪的是"阶段深度"，不是"变更纪律"。Hotfix 可以不写完整规格卡，但必须分析影响面；Spike 可以跳过编码和测试，但必须沉淀结论。裁剪了不该裁剪的东西，失管风险比正常变更更大。

---

## 九、Change Management 与六阶段流水线

### 9.1 Change 是流水线的"工件"

六阶段流水线（analyzing → coding → testing → reviewing → ci → verifying）的全部输入输出都是 Change：

```
  ① 需求分析          /harnessing      → 产出 change.md（status: analyzing）
  ② 编码实现          /coding-skill    → 消费 change.md（status: coding）
  ③ 单测编写          /unit-test-write → 消费（status: testing）
  ④ 专家评审          /expert-reviewer → 消费（status: reviewing）
  ⑤ CI 门禁          /unit-test-ci    → 消费（status: ci）
  ⑥ 部署验证          /deploy-verify   → 消费（status: verifying）
                      ✅ 完成          → status: done
```

### 9.2 Change 是回滚的最小单元

```
回滚单元 = 一个 Change：
  git revert（该 Change 的提交集）
  + 校验影响面内的文件恢复
  + verify.md 记录回滚验证结果
```

**一个 Change 一个提交集，回滚就可定位。** 多个 Change 混在一个提交里，回滚就是噩梦。

### 9.3 Change 是上下文锁定的载体

把 3.1 和 9.1 连起来看：

```
六阶段流水线给了 Change"生命周期"
变更定位规则给了 Change"锁定机制"
change.md 给了 Change"规格真相源"
三者合一 = 多 Change 场景下，AI 永远知道：
  自己在哪个阶段、做哪个 Change、什么算完成
```

### 9.4 Change 与 handoff：断点续传的闭环

会话会断，这是 AI 协作的物理现实——上下文窗口满了、进程重启、换一个 AI 实例。Change Management 的最后一环是 **handoff 技能**：它把当前工作上下文压缩成一份交接文档，让下一个会话能直接续接。

handoff 技能的输入输出（源码 `skills/harness-core/skills/handoff/SKILL.md`）：

```
输入（收集上下文）：
  .harness/changes/<id>/change.md   → 当前变更规格
  .harness/changes/<id>/review.md   → 最近评审记录
  对话历史                            → 关键决策、未解决问题
  当前代码变更                        → 已修改未提交的代码
  lint / 测试结果                     → 最近运行状态

输出（写入交接文档）：
  .harness/changes/<id>/handoff.md
    ├── 当前状态（status + 进度）
    ├── 已完成的工作
    ├── 未完成的工作
    ├── 关键决策与原因
    ├── 未解决问题
    ├── 断点续接命令（编译/测试/lint）
    └── 下一步行动
```

**handoff 的锁定价值**：交接文档写在 Change 目录里（`.harness/changes/<id>/handoff.md`），不是写在一个全局位置。这保证了交接文档与它所属的 Change 绑定——新会话只要锁定到同一个 Change，就能读到对应的交接文档。如果交接文档写在一个全局位置，多 Change 场景下会混淆“这是哪个 Change 的交接”。

**三件套 + handoff = 完整的 Change 留档**：

```
.harness/changes/C-NNN/
  ├── change.md    ← 规格（① 产出，全程真相源）
  ├── review.md    ← 评审（④ 产出，双轴报告）
  ├── verify.md    ← 验证（⑥ 产出，回滚预案）
  └── handoff.md   ← 交接（阶段切换时产出，断点续传）
```

会话断 → handoff.md 存在 → 新会话读 handoff.md 续接 → 继续推进状态机。这个闭环让 AI 的“记忆”从"靠对话"变成了"靠磁盘"——磁盘上的文件不会因会话结束而消失。

---

## 十、信条回顾

- **一次只做一个变更，是理想；同时推进十个，是现实**——锁定是并行协作的刚需
- **变更必须可追溯、可验证、可回滚**——每个 Change 一个目录、一个提交集、一组 AC
- **上下文锁定，禁止模糊选择**——AI 永不猜测，多候选必须请示（源码 `变更定位规则.md`）
- **入口即锁，阶段即界**——每个技能只消费匹配 status 的 Change
- **会话可以丢，规格不能丢**——change.md 是 AI 的"记忆外置"
- **只读队友的规格，不写队友的地盘**——影响面隔离
- **锁定不等于死锁**——影响面冲突时协调，而不是各改各的
- **模板定结构，规则定使用**——两者配合才是完整的锁定实现
- **三件套留档：change.md 定规格、review.md 定评审、verify.md 定交付**——三个阶段三份文档，各管各的问题
- **状态机有打回回路，打回按问题性质精准回退**——需求错回 analyzing，实现错回 coding，不一律重头
- **裁剪阶段不裁剪纪律**——Hotfix 也要有影响面，Spike 也要有沉淀
- **handoff 让记忆从对话落到磁盘**——会话断、handoff.md 在，新会话续接

---

> 💡 至此，二十二章系列已覆盖：工程纪律（01-03）、参数化（04-07）、技能体系（08-12）、apply-harness 引擎（13）、六大核心技能（14-15）、灵魂拷问（16）、自定义 Skill（17）、新增框架（18）、迁移实战（19）、Change Management（20）。最后两章，我们将聊聊 SDD-TDD 方法论与 Owner Agent——把"AI 团队"的最后一个拼图放上。