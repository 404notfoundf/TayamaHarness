# /harness-refactor：改结构，不改行为——每个小步都绿了才前进

> 命令深度拆解 · 第 33 篇 · 约 9000 字 · 7 个 SVG 图

---

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 300" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_r0" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_r0"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="300" fill="url(#bg_r0)" rx="10"/>
  <text x="400" y="28" text-anchor="middle" fill="#e2e8f0" font-size="26" font-weight="700\">/harness-refactor：改结构，不改行为</text>
  <rect x="60" y="55" width="320" height="95" rx="10" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_r0)"/>
  <text x="220" y="80" text-anchor="middle" fill="#fde68a" font-size="14" font-weight="700">行为不变的约束</text>
  <text x="220" y="104" text-anchor="middle" fill="#94a3b8" font-size="11">可读性 · 职责划分 · 重复消除</text>
  <text x="220" y="126" text-anchor="middle" fill="#64748b" font-size="10\">命名 · 模块边界</text>
  <rect x="420" y="55" width="320" height="95" rx="10" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_r0)"/>
  <text x="580" y="80" text-anchor="middle" fill="#86efac" font-size="14" font-weight="700">测试保护 + 小步进行</text>
  <text x="580" y="104" text-anchor="middle" fill="#94a3b8" font-size="11">测试先转绿，每步绿了才前进</text>
  <text x="580" y="126" text-anchor="middle" fill="#64748b" font-size="10">没有保护 → 先补特征测试</text>
  <text x="400" y="185" text-anchor="middle" fill="#475569" font-size="13">核心规则（一句话）</text>
  <rect x="80" y="205" width="640" height="50" rx="8" fill="#1e293b" stroke="#475569" stroke-width="1.5" filter="url(#sh_r0)"/>
  <text x="400" y="237" text-anchor="middle" fill="#e2e8f0" font-size="13">重构不改变行为——测试先转绿，每步绿了才前进。没有测试保护的代码，先补特征测试再动手。</text>
  <text x="400" y="284" text-anchor="middle" fill="#64748b" font-size="10\">三道闸门：行为基线 → 小步循环 → 验证行为不变</text>
</svg>
```

## 一、/harness-refactor 解决的是什么问题

### 1.1 结构烂，功能对，怎么办

代码库最常见的困境：功能是对的，但结构烂——函数五百行、职责混在一起、重复代码遍地、命名不知所云。直接改吧，怕把好的改坏了；不改吧，每次加功能都像在雷区里走路。

`/harness-refactor` 就是为这种场景准备的：在**行为不变的约束**下改善代码结构——可读性、职责划分、重复消除、命名、模块边界。

它的核心规则一句话概括：**重构不改变行为——测试先转绿，每步绿了才前进。没有测试保护的代码，先补特征测试（characterization test）再动手。**

这句话拆开是三个硬规定：

1. **行为不变**——重构的输出和输入行为必须完全一致；
2. **测试保护**——没有测试的代码不许动，先写特征测试建立基线；
3. **小步前进**——每次只改一小步，每步都验证绿了再走下一步。

### 1.2 特征测试：没有保护先建保护

重构最大的风险是"**改着改着行为悄悄变了**"。怎么知道行为变没变？对照物只能是测试。但现实是：你要重构的烂代码，往往恰恰没有测试（有测试可能也不至于烂成这样）。

对策就是特征测试（characterization test）：**用当前输出/行为固化为断言**——不管代码写得对不对，先把"它现在实际干什么"用测试记录下来，跑绿，作为行为基线。特征测试测试的不是"应该怎样"，而是"现在怎样"。它是重构的**安全带**：有了它，重构过程中任何行为偏移都会被测试当场抓住。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 260" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_r1" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_r1"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="260" fill="url(#bg_r1)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">特征测试：先建行为基线，再动结构</text>
  <rect x="40" y="50" width="330" height="60" rx="8" fill="#1e293b" stroke="#334155"/>
  <text x="205" y="72" text-anchor="middle" fill="#94a3b8" font-size="10">现状：烂代码 + 无测试</text>
  <text x="205" y="96" text-anchor="middle" fill="#64748b" font-size="9">直接重构 = 在雷区里走路</text>
  <rect x="420" y="50" width="340" height="60" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_r1)"/>
  <text x="590" y="72" text-anchor="middle" fill="#86efac" font-size="11" font-weight="700">先写特征测试</text>
  <text x="590" y="96" text-anchor="middle" fill="#94a3b8" font-size="9\">把\"现在实际干什么\"固化为断言</text>
  <rect x="40" y="135" width="330" height="60" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_r1)"/>
  <text x="205" y="157" text-anchor="middle" fill="#86efac" font-size="11" font-weight="700">跑绿 = 行为基线</text>
  <text x="205" y="181" text-anchor="middle" fill="#94a3b8" font-size="9\">测试的不是\"应该怎样\"，是\"现在怎样\"</text>
  <rect x="420" y="135" width="340" height="60" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_r1)"/>
  <text x="590" y="157" text-anchor="middle" fill="#93c5fd" font-size="11" font-weight="700">重构开始</text>
  <text x="590" y="181" text-anchor="middle" fill="#94a3b8" font-size="9">每次行为偏移，测试当场抓住</text>
  <text x="400" y="236" text-anchor="middle" fill="#475569" font-size="11">安全带原理：没有基线，改到行为变了你也不知道</text>
</svg>
```

### 1.3 重构不是"顺手优化"

`/harness-refactor` 与普通改代码的本质区别：**重构是独立于功能变更的专项活动**。它出现在三个典型时机：

- `coding-skill` 完成功能后需要清理结构（Red-Green-Refactor 的第三段）；
- `expert-reviewer` 标注出"结构问题但行为正确"的问题；
- `diagnosing-bugs` 发现代码难读/难改导致排查困难；
- 技术债条目（`TECH-DEBT.md` 中的 P2 异味）。

每个时机都是一个"结构专项"——不在功能开发过程中顺手做，而是作为独立任务执行。这个隔离非常关键：**功能变更和结构重构混在一起，行为变化来源就说不清了**。

## 二、前置：行为保护是重构的第一纪律

动手前的第一步永远是对行为基线的确认：

1. **检查现有测试**：目标代码是否有测试覆盖？
   - **有** → 先跑绿，作为基线；
   - **无/覆盖不足** → 先写**特征测试**（characterization test）：用当前输出/行为固化为断言，跑绿。**禁止**在无保护下直接重构。
2. **记录基线**：`git stash` 后对比或直接 `git diff`，确认重构前后行为一致。

这两个前置把重构从"凭感觉改"变成"有对照的改"：**有基线，行为变了测试会叫；没基线，行为变了你永远不知道**。前置的灵魂是"没有保护不许动刀"——这是重构的第一纪律，比任何手法都重要。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 230" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_r2" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_r2"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="230" fill="url(#bg_r2)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">前置：有测试跑绿当基线，没测试先补特征测试</text>
  <rect x="40" y="50" width="340" height="110" rx="8" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_r2)"/>
  <text x="210" y="76" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">有测试覆盖</text>
  <text x="210" y="102" text-anchor="middle" fill="#94a3b8" font-size="9">先跑绿 → 作为基线</text>
  <text x="210" y="124" text-anchor="middle" fill="#64748b" font-size="8">正常路径</text>
  <text x="210" y="146" text-anchor="middle" fill="#64748b" font-size="8">直接开工</text>
  <rect x="420" y="50" width="340" height="110" rx="8" fill="#ef4444" opacity="0.10" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_r2)"/>
  <text x="590" y="76" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">无/覆盖不足</text>
  <text x="590" y="102" text-anchor="middle" fill="#94a3b8" font-size="9">先写特征测试固化现状</text>
  <text x="590" y="124" text-anchor="middle" fill="#64748b" font-size="8">跑绿 → 建立基线</text>
  <text x="590" y="146" text-anchor="middle" fill="#64748b" font-size="8">禁止无保护直接重构</text>
  <text x="400" y="192" text-anchor="middle" fill="#475569" font-size="11">记录基线：git stash 对比 / git diff——确认重构前后行为一致</text>
  <text x="400" y="216" text-anchor="middle" fill="#64748b" font-size="10\">没有保护不许动刀：这是第一纪律，比任何重构手法都重要</text>
</svg>
```

## 三、三步执行流程

### Step 1: 划定范围

一次只重构一个**小范围**（一个函数/一个类/一个模块）。列出要消除的异味与目标结构，不顺手改无关代码。

范围划定是重构的质量上限：**范围越小，风险越小，验证越精确**。一次重构五个函数，绿了也不知道是哪个函数的行为在测；一次只重构一个函数，出问题就是这一个函数的问题。

划范围的纪律是"**列异味，不顺手改**"：

- 划出目标范围后，把要消除的异味逐条列出（过长函数/重复代码/坏命名/上帝类…）；
- 画出目标结构（提取后长什么样）；
- **与范围无关的代码，再烂也不碰**——那是另一个 change 的事。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 240" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_r3" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_r3"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="240" fill="url(#bg_r3)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">Step 1：一次只重构一个小范围</text>
  <rect x="40" y="50" width="320" height="110" rx="10" fill="#3b82f6" opacity="0.10" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_r3)"/>
  <text x="200" y="76" text-anchor="middle" fill="#93c5fd" font-size="13" font-weight="700">划范围（一个函数/类/模块）</text>
  <text x="200" y="102" text-anchor="middle" fill="#94a3b8" font-size="10">列要消除的异味清单</text>
  <text x="200" y="124" text-anchor="middle" fill="#94a3b8" font-size="10">画目标结构</text>
  <text x="200" y="146" text-anchor="middle" fill="#64748b" font-size="9">范围越小，风险越可控</text>
  <rect x="420" y="50" width="340" height="110" rx="10" fill="#ef4444" opacity="0.10" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_r3)"/>
  <text x="590" y="76" text-anchor="middle" fill="#fca5a5" font-size="13" font-weight="700">不顺手改无关代码</text>
  <text x="590" y="102" text-anchor="middle" fill="#94a3b8" font-size="10">再烂也不碰——那是另一个 change</text>
  <text x="590" y="124" text-anchor="middle" fill="#94a3b8" font-size="10">scope creep 是重构的头号敌人</text>
  <text x="590" y="146" text-anchor="middle" fill="#64748b" font-size="9">隔离：行为变化来源才能说清</text>
  <text x="400" y="196" text-anchor="middle" fill="#475569" font-size="11">一次重构五个函数，绿了也不知道在测谁；一次一个，出问题就知道是谁</text>
  <text x="400" y="222" text-anchor="middle" fill="#64748b" font-size="10">跨越范围 = 失去验证的精确性</text>
</svg>
```

### Step 2: 小步重构循环

对每个小步：

1. **挑一种重构手法**：提取函数/提取变量/改名/拆分模块/合并重复/替换条件逻辑等（按项目 `编码规范.md`）；
2. **改代码**：只做这一步，不混入功能变更；
3. **跑测试**：该范围相关的测试必须全绿；
4. 测试红 → **立即回退这一步**（`git checkout` 该文件/撤销），不带着红测试继续；
5. 绿 → 提交（commit message 含 change ID 与重构手法）。

循环直到该范围重构完成。这就是重构的**最小步进循环**：改一小步 → 测 → 绿就提交 / 红就回退 → 下一步。

两个纪律值得强调：

- **一次只做一种手法**：提取函数就是提取函数，不顺便改名。混手法会让"哪一步导致行为变化"无法定位；
- **红就回退，绝不带病前进**：带着红测试继续改，等于在不知道基线是否成立的情况下叠新变化——红测试的每一步都不可信。回退成本极低（一行 git checkout），带病前进的代价可能是整个范围的返工。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 260" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_r4" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_r4"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="260" fill="url(#bg_r4)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">Step 2：小步重构循环</text>
  <rect x="40" y="50" width="170" height="55" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_r4)"/>
  <text x="125" y="72" text-anchor="middle" fill="#93c5fd" font-size="11" font-weight="700">① 挑一种手法</text>
  <text x="125" y="94" text-anchor="middle" fill="#64748b" font-size="8">提取/改名/拆分/合并</text>
  <rect x="230" y="50" width="170" height="55" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_r4)"/>
  <text x="315" y="72" text-anchor="middle" fill="#86efac" font-size="11" font-weight="700">② 改代码</text>
  <text x="315" y="94" text-anchor="middle" fill="#64748b" font-size="8">只做这一步不混功能</text>
  <rect x="420" y="50" width="170" height="55" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_r4)"/>
  <text x="505" y="72" text-anchor="middle" fill="#fde68a" font-size="11" font-weight="700">③ 跑测试</text>
  <text x="505" y="94" text-anchor="middle" fill="#64748b" font-size="8">相关测试必须全绿</text>
  <rect x="610" y="50" width="150" height="55" rx="8" fill="#a855f7" opacity="0.12" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_r4)"/>
  <text x="685" y="72" text-anchor="middle" fill="#d8b4fe" font-size="11" font-weight="700">④ 绿→提交</text>
  <text x="685" y="94" text-anchor="middle" fill="#64748b" font-size="8">红→立即回退</text>
  <text x="400" y="150" text-anchor="middle" fill="#475569" font-size="11">循环：直到该范围重构完成</text>
  <rect x="120" y="170" width="560" height="40" rx="8" fill="#ef4444" opacity="0.10" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_r4)"/>
  <text x="400" y="196" text-anchor="middle" fill="#fca5a5" font-size="10">测试红 → git checkout 该文件，不带着红测试继续</text>
  <text x="400" y="232" text-anchor="middle" fill="#64748b" font-size="10">"红就回退"是一行 git checkout 的成本，"带病前进"是整段返工的代价</text>
</svg>
```

### Step 3: 验证行为不变

小步循环跑完，最后还要过一次总闸——验证整个范围的**行为不变**：

- 跑目标范围全量相关测试 + 周边测试，确认全绿；
- 用 `git diff` 核对：重构不应改变任何对外接口签名、返回语义、日志/错误信息（除非显式约定）；
- 需要时跑 `/harness-quality` 的 flow→test 对账，确认业务流仍被覆盖。

三个验证维度层层递进：

1. **测试全绿**——行为的机器验证：测试说了算；
2. **diff 核对**——结构的肉眼验证：签名、语义、日志信息应原封不动；
3. **flow→test 对账**——业务流覆盖的验证：重构后业务流仍有直接测试在保护。

其中 diff 核对最容易被忽略：测试绿了不代表日志/错误信息没变——调用方可能依赖这些字符串。**重构的"行为不变"是彻底的：接口、语义、乃至日志，全都不能变。**

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 240" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_r5" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_r5"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="240" fill="url(#bg_r5)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">Step 3：三层验证"行为不变"</text>
  <rect x="40" y="50" width="220" height="100" rx="8" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_r5)"/>
  <text x="150" y="76" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">① 测试全绿</text>
  <text x="150" y="100" text-anchor="middle" fill="#94a3b8" font-size="9">行为的机器验证</text>
  <text x="150" y="122" text-anchor="middle" fill="#64748b" font-size="8">测试说了算</text>
  <text x="150" y="140" text-anchor="middle" fill="#64748b" font-size="8">相关+周边全跑</text>
  <rect x="290" y="50" width="220" height="100" rx="8" fill="#3b82f6" opacity="0.10" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_r5)"/>
  <text x="400" y="76" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">② diff 核对</text>
  <text x="400" y="100" text-anchor="middle" fill="#94a3b8" font-size="9">结构的肉眼验证</text>
  <text x="400" y="122" text-anchor="middle" fill="#64748b" font-size="8">签名/语义/日志原封不动</text>
  <text x="400" y="140" text-anchor="middle" fill="#64748b" font-size="8">最容易被忽略</text>
  <rect x="540" y="50" width="220" height="100" rx="8" fill="#a855f7" opacity="0.10" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_r5)"/>
  <text x="650" y="76" text-anchor="middle" fill="#d8b4fe" font-size="12" font-weight="700">③ flow→test 对账</text>
  <text x="650" y="100" text-anchor="middle" fill="#94a3b8" font-size="9">业务流覆盖验证</text>
  <text x="650" y="122" text-anchor="middle" fill="#64748b" font-size="8">重构后仍有直接测试</text>
  <text x="650" y="140" text-anchor="middle" fill="#64748b" font-size="8">harness-quality 联动</text>
  <text x="400" y="196" text-anchor="middle" fill="#475569" font-size="11">行为不变是彻底的：接口、语义、日志——全都不能变</text>
  <text x="400" y="222" text-anchor="middle" fill="#64748b" font-size="10">调用方可能依赖日志字符串，测试绿了不代表日志没变</text>
</svg>
```

## 四、禁止事项：重构的红线清单

重构不是无约束的"改代码"，以下五件事是红线：

- ❌ **重构与功能变更混在一次提交里**（无法区分行为变化来源）——出 bug 时分不清是重构引入的还是功能引入的；
- ❌ **无测试保护直接大改**（"反正我看懂了"）——看懂了不代表没改歪，特征测试是唯一的客观对照；
- ❌ **顺手"优化"无关代码**（scope creep）——每个顺手改都可能引入新 bug，且没有任何测试为它背书；
- ❌ **一口气重构大范围后一次性验证**（失败难定位）——范围一大，红了都不知道是哪一步导致的；
- ❌ **为了重构引入未经评审的依赖或模式**——重构是"改善结构"不是"引入新东西"，新依赖/新模式要过评审。

这五条红线的共同点：**它们都让"行为变化来源"变得不可追溯**。重构的底线是"改了结构，行为一处不变"——任何让这个底线无法验证的做法都是红线。

## 五、与相邻技能的分工

| 场景 | 归属 |
|------|------|
| 功能编码 | `/coding-skill` |
| 补特征测试保护 | `/unit-test-write` |
| 评审重构结果 | `/expert-reviewer` |
| 重构执行 | **本技能**（`/harness-refactor`） |

- **coding-skill**：功能开发的执行者——它的 Red-Green-Refactor 的 Refactor 段正是本技能的用武之地；
- **unit-test-write**：特征测试的提供者——没有测试保护的代码，先请它补特征测试再动刀；
- **expert-reviewer**：重构结果的评审者——"结构问题但行为正确"的标注会触发本技能，重构完成后结构质量由它复核。

## 六、完成标志

`/harness-refactor` 的完成标志有三个：

1. **重构前后测试全绿，行为基线无破坏**——测试从开始到结束都绿，特征测试证明行为没变；
2. **每次提交只含单一重构手法，含 change ID**——提交粒度可追溯：一个提交一个手法，commit message 里带着 change ID；
3. **异味已消除/收窄，且无 scope creep**——目标异味处理完成，且没有顺手改动无关代码。

三个标志分别回答三个问题：**行为保住没（测试绿）？过程可追溯没（单手法提交）？范围守住没（无异味残留/无越界）？** 三个都答"是"，重构才算完成。

## 七、写在最后

`/harness-refactor` 的全部设计，浓缩成四句话：

1. **行为不变是铁律**——重构改善的是结构，不是行为；行为动一点，就不是重构而是改功能。
2. **没有保护不许动刀**——特征测试是安全带，先建基线再改结构。
3. **小步循环，红就回退**——一次一种手法，绿了提交，红了 checkout，绝不带病前进。
4. **范围是红线**——不混功能、不顺手改无关代码，重构的边界清清楚楚。

一句话记住它：**/harness-refactor 是代码的"结构手术师"——它在行为不变的铁律下，用特征测试作保护、小步循环作步法、红线清单作边界，让"改结构"成为一次可验证、可追溯、可回退的安全操作。**