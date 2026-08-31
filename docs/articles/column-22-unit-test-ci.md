# /unit-test-ci：机械化门禁的 6 步流水线

> 命令深度拆解 · 第 22 篇 · 约 9000 字 · 8 个 SVG 图

---

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 300" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_u0" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_u0"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="300" fill="url(#bg_u0)" rx="10"/>
  <text x="400" y="28" text-anchor="middle" fill="#e2e8f0" font-size="26" font-weight="700">/unit-test-ci：机械化门禁的 6 步流水线</text>
  <rect x="60" y="55" width="210" height="60" rx="8" fill="#ef4444" opacity="0.12" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_u0)"/>
  <text x="165" y="80" text-anchor="middle" fill="#fca5a5" font-size="13" font-weight="700">① 编译检查</text>
  <text x="165" y="100" text-anchor="middle" fill="#94a3b8" font-size="10">0 error 才放行</text>
  <rect x="300" y="55" width="210" height="60" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_u0)"/>
  <text x="405" y="80" text-anchor="middle" fill="#fde68a" font-size="13" font-weight="700">②③④ 静态·竞态·架构</text>
  <text x="405" y="100" text-anchor="middle" fill="#94a3b8" font-size="10">lint / race / arch</text>
  <rect x="540" y="55" width="210" height="60" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_u0)"/>
  <text x="645" y="80" text-anchor="middle" fill="#86efac" font-size="13" font-weight="700">⑤⑥ 测试+安全</text>
  <text x="645" y="100" text-anchor="middle" fill="#94a3b8" font-size="10">覆盖率≥80% / 0 命中</text>
  <line x1="270" y1="85" x2="290" y2="85" stroke="#475569" stroke-width="2"/>
  <polygon points="296,85 288,80 288,90" fill="#94a3b8"/>
  <line x1="510" y1="85" x2="530" y2="85" stroke="#475569" stroke-width="2"/>
  <polygon points="536,85 528,80 528,90" fill="#94a3b8"/>
  <rect x="60" y="150" width="690" height="60" rx="8" fill="#3b82f6" opacity="0.10" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_u0)"/>
  <text x="405" y="175" text-anchor="middle" fill="#93c5fd" font-size="13" font-weight="700">任一检查失败 = 红灯，禁止放行</text>
  <text x="405" y="195" text-anchor="middle" fill="#94a3b8" font-size="10">"机械化执行"是门禁可信的前提——不靠人记，靠机器验</text>
  <text x="405" y="240" text-anchor="middle" fill="#475569" font-size="11">输入：status=ci 的变更卡 → 输出：status=verifying</text>
  <text x="405" y="265" text-anchor="middle" fill="#64748b" font-size="10">编译 → 静态分析 → 竞态检测 → 架构约束 → 测试+覆盖率 → 安全扫描</text>
  <text x="405" y="288" text-anchor="middle" fill="#475569" font-size="10">红灯不可协商，是质量门禁与"走过场"的本质区别</text>
</svg>
```

## 一、/unit-test-ci 解决的是什么问题

### 1.1 质量不是"感觉"，是"门禁"

在 Harness 流水线中，`/expert-reviewer` 完成的是**人审代码**，`/unit-test-write` 完成的是**写测试**。但测试写完了、人审通过了，就代表代码能上线吗？

不一定。因为还有六类问题，是人工审查和随手跑一次测试**都覆盖不到**的：

| 问题类型 | 人工审查能看到吗 | 随手跑一次测试能发现吗 |
|---------|----------------|---------------------|
| 代码编译错误（改完一处，另一处引用断了） | 难 | 也许 |
| 风格/告警积累（未用变量、死代码、复杂度超标） | 会漏 | 不会 |
| 数据竞争（多协程/多线程并发写同一变量） | **几乎不可能** | 单次运行很难触发 |
| 架构腐化（Controller 直接调 DAO、分层越级） | 会漏 | 不会 |
| 覆盖率不达标（核心逻辑没测到） | 难判断 | 不会 |
| 安全红线（硬编码密钥、危险 DDL、破坏性删除） | 难 | 不会 |

这六类问题的共性是什么？**它们需要"机械化"地去验，而不是"看一眼"。**

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 280" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_u1" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_u1"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="280" fill="url(#bg_u1)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="24" font-weight="700">靠人记 vs 靠机器验</text>
  <rect x="50" y="50" width="340" height="150" rx="10" fill="#ef4444" opacity="0.08" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_u1)"/>
  <text x="220" y="76" text-anchor="middle" fill="#fca5a5" font-size="13" font-weight="700">靠人记（"我检查过了"）</text>
  <text x="220" y="100" text-anchor="middle" fill="#94a3b8" font-size="11">· 依赖编码者的记忆与自觉</text>
  <text x="220" y="120" text-anchor="middle" fill="#94a3b8" font-size="11">· 每次都能漏掉不同的问题</text>
  <text x="220" y="140" text-anchor="middle" fill="#94a3b8" font-size="11">· 结果不可复现，无法追溯</text>
  <text x="220" y="160" text-anchor="middle" fill="#94a3b8" font-size="11">· 团队越大，口径越分散</text>
  <text x="220" y="184" text-anchor="middle" fill="#64748b" font-size="10">→ 质量靠"运气"</text>
  <rect x="420" y="50" width="340" height="150" rx="10" fill="#22c55e" opacity="0.08" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_u1)"/>
  <text x="590" y="76" text-anchor="middle" fill="#86efac" font-size="13" font-weight="700">靠机器验（/unit-test-ci）</text>
  <text x="590" y="100" text-anchor="middle" fill="#94a3b8" font-size="11">· 同一套命令，跑一万次结果一致</text>
  <text x="590" y="120" text-anchor="middle" fill="#94a3b8" font-size="11">· 每个 check 都有原始输出留档</text>
  <text x="590" y="140" text-anchor="middle" fill="#94a3b8" font-size="11">· 结果可复现、可审计、可追溯</text>
  <text x="590" y="160" text-anchor="middle" fill="#94a3b8" font-size="11">· 团队统一口径，无死角</text>
  <text x="590" y="184" text-anchor="middle" fill="#64748b" font-size="10">→ 质量靠"体系"</text>
  <text x="400" y="228" text-anchor="middle" fill="#475569" font-size="11">核心洞察：能机械化的检查，就不该靠人。人的注意力留给机械验不出来的事（设计、取舍、边界）</text>
  <text x="400" y="252" text-anchor="middle" fill="#64748b" font-size="10">/unit-test-ci 的使命：把"应该机械化"的部分全部机械化</text>
</svg>
```

### 1.2 为什么"机械化"是门禁可信的前提

一个门禁如果由人"看一眼"执行，它就不是门禁——因为"人看一眼"的结果**不可复现**。同样的代码，今天看没问题，明天看可能就有问题；A 看没问题，B 看就有问题。

机械化的含义有三层：

1. **同一输入 → 同一输出**：`go vet ./...` 今天跑和明天跑，结果一致（代码不变的前提下）。这是"门禁"与"走过场"的分界线。
2. **输出可留档**：每条检查命令的原始输出都能被记录、被追溯。红灯不是"感觉不对"，而是"这条命令退出码非零、输出在留档里"。
3. **口径归一**：整个团队跑的是同一套命令，不存在"我觉得不需要跑竞态检测"这种分歧。

这就是 `/unit-test-ci` 的第一性原理：**把约束从"靠人记"变成"靠机器验"。**

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 260" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_u2" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_u2"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="260" fill="url(#bg_u2)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">机械化的三层可信</text>
  <rect x="50" y="50" width="220" height="120" rx="10" fill="#3b82f6" opacity="0.10" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_u2)"/>
  <text x="160" y="78" text-anchor="middle" fill="#93c5fd" font-size="13" font-weight="700">可复现</text>
  <text x="160" y="100" text-anchor="middle" fill="#94a3b8" font-size="10">同一命令、同一代码</text>
  <text x="160" y="118" text-anchor="middle" fill="#94a3b8" font-size="10">结果永远一致</text>
  <text x="160" y="140" text-anchor="middle" fill="#64748b" font-size="10">不依赖执行者是谁</text>
  <rect x="300" y="50" width="200" height="120" rx="10" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_u2)"/>
  <text x="400" y="78" text-anchor="middle" fill="#fde68a" font-size="13" font-weight="700">可留档</text>
  <text x="400" y="100" text-anchor="middle" fill="#94a3b8" font-size="10">退出码 + 原始输出</text>
  <text x="400" y="118" text-anchor="middle" fill="#94a3b8" font-size="10">全部记录在案</text>
  <text x="400" y="140" text-anchor="middle" fill="#64748b" font-size="10">红灯有据可查</text>
  <rect x="530" y="50" width="220" height="120" rx="10" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_u2)"/>
  <text x="640" y="78" text-anchor="middle" fill="#86efac" font-size="13" font-weight="700">口径归一</text>
  <text x="640" y="100" text-anchor="middle" fill="#94a3b8" font-size="10">全团队同一套命令</text>
  <text x="640" y="118" text-anchor="middle" fill="#94a3b8" font-size="10">无"个人检查习惯"</text>
  <text x="640" y="140" text-anchor="middle" fill="#64748b" font-size="10">口径即体系</text>
  <text x="400" y="200" text-anchor="middle" fill="#475569" font-size="11">三层缺一不可：不可复现则不可信，不可留档则不可审，口径不一则不可比</text>
  <text x="400" y="228" text-anchor="middle" fill="#64748b" font-size="10">/unit-test-ci 的输出不是"报告"，而是"证据"</text>
</svg>
```

### 1.3 一个真实场景：为什么"随手跑一次测试"不够

想象你是一个 Go 开发者，改了订单模块的并发逻辑。你"随手跑了 `go test ./...`"，全绿，提交了。

但你的同事在 review 时发现：`cancelOrder` 和 `expireOrder` 两个协程在并发写 `order.Status`——数据竞争。你随手跑的那次测试，恰好没触发这个 race；而 `go test -race ./...` 能稳定地在第一次运行就报出来。

这个场景说明：**测试全绿 ≠ 代码没问题**。它只能说明"我写的测试通过了"，不能说明"代码没有竞态、没有架构问题、没有安全红线"。后者需要的是**专门的机械化检查**，而不是"再多跑一次测试"。

## 二、为什么是"6 步"而不是"3 步"或"10 步"

### 2.1 六步的分类逻辑

`/unit-test-ci` 的流水线是六步，每一步解决一个**独立的错误类型**：

```
stage-1  编译检查        → 代码根本跑不起来的问题（断引用、语法错）
stage-2  静态分析        → 代码风格/潜在 bug（未用变量、复杂度、重复）
stage-3  竞态检测        → 并发正确性（数据竞争、死锁信号）
stage-4  架构约束        → 模块/分层方向（依赖倒置、越级调用）
stage-5  单元测试+覆盖率  → 行为正确性 + 测试充分性（≥80%）
stage-6  安全扫描        → 红线类（硬编码密钥、危险 DDL/DML、破坏性删除）
```

为什么是这个顺序？**因为每一步都在"更快、更便宜"地发现问题，且前置步骤失败时，后续步骤没有意义**：

1. 编译都过不了 → 跑测试没有意义（代码跑不起来）；所以编译必须第一。
2. 静态分析不过 → 代码质量差，直接进入测试会放大问题；所以静态第二。
3. 竞态检测 → 在测试**之前**跑，因为带 race 检测的测试更慢；而且数据竞争是"测试跑绿也发现不了"的，必须专门验。
4. 架构约束 → 依赖方向错了，测试再绿也是"绿在错误的结构上"，架构问题越早发现越便宜。
5. 测试+覆盖率 → 是行为正确性的最终验证，放最后是因为它最慢、最权威。
6. 安全扫描 → 是"红线"检查，独立于一切功能正确性——即使全部功能正确，硬编码密钥也是必须拦下的红线。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 320" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_u3" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_u3"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="320" fill="url(#bg_u3)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">6 步流水线：每一步拦一类错误</text>
  <rect x="40" y="48" width="110" height="190" rx="8" fill="#ef4444" opacity="0.10" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_u3)"/>
  <text x="95" y="70" text-anchor="middle" fill="#fca5a5" font-size="11" font-weight="700">① 编译</text>
  <text x="95" y="90" text-anchor="middle" fill="#94a3b8" font-size="9">断引用</text>
  <text x="95" y="106" text-anchor="middle" fill="#94a3b8" font-size="9">语法错</text>
  <text x="95" y="150" text-anchor="middle" fill="#64748b" font-size="9">最快最便宜</text>
  <text x="95" y="168" text-anchor="middle" fill="#64748b" font-size="9">前置不过，</text>
  <text x="95" y="184" text-anchor="middle" fill="#64748b" font-size="9">后续无意义</text>
  <rect x="165" y="48" width="110" height="190" rx="8" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_u3)"/>
  <text x="220" y="70" text-anchor="middle" fill="#fde68a" font-size="11" font-weight="700">② 静态</text>
  <text x="220" y="90" text-anchor="middle" fill="#94a3b8" font-size="9">lint/风格</text>
  <text x="220" y="106" text-anchor="middle" fill="#94a3b8" font-size="9">复杂度</text>
  <text x="220" y="122" text-anchor="middle" fill="#94a3b8" font-size="9">重复代码</text>
  <text x="220" y="168" text-anchor="middle" fill="#64748b" font-size="9">质量差的代码</text>
  <text x="220" y="184" text-anchor="middle" fill="#64748b" font-size="9">放大后续问题</text>
  <rect x="290" y="48" width="110" height="190" rx="8" fill="#a855f7" opacity="0.10" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_u3)"/>
  <text x="345" y="70" text-anchor="middle" fill="#d8b4fe" font-size="11" font-weight="700">③ 竞态</text>
  <text x="345" y="90" text-anchor="middle" fill="#94a3b8" font-size="9">数据竞争</text>
  <text x="345" y="106" text-anchor="middle" fill="#94a3b8" font-size="9">并发正确性</text>
  <text x="345" y="168" text-anchor="middle" fill="#64748b" font-size="9">测试绿也发现不了</text>
  <text x="345" y="184" text-anchor="middle" fill="#64748b" font-size="9">必须专门验</text>
  <rect x="415" y="48" width="110" height="190" rx="8" fill="#3b82f6" opacity="0.10" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_u3)"/>
  <text x="470" y="70" text-anchor="middle" fill="#93c5fd" font-size="11" font-weight="700">④ 架构</text>
  <text x="470" y="90" text-anchor="middle" fill="#94a3b8" font-size="9">分层越级</text>
  <text x="470" y="106" text-anchor="middle" fill="#94a3b8" font-size="9">依赖方向</text>
  <text x="470" y="168" text-anchor="middle" fill="#64748b" font-size="9">越早发现越便宜</text>
  <text x="470" y="184" text-anchor="middle" fill="#64748b" font-size="9">测试绿在错结构上</text>
  <rect x="540" y="48" width="110" height="190" rx="8" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_u3)"/>
  <text x="595" y="70" text-anchor="middle" fill="#86efac" font-size="11" font-weight="700">⑤ 测试</text>
  <text x="595" y="90" text-anchor="middle" fill="#94a3b8" font-size="9">行为正确性</text>
  <text x="595" y="106" text-anchor="middle" fill="#94a3b8" font-size="9">覆盖率≥80%</text>
  <text x="595" y="168" text-anchor="middle" fill="#64748b" font-size="9">最慢最权威</text>
  <text x="595" y="184" text-anchor="middle" fill="#64748b" font-size="9">最终验证</text>
  <rect x="665" y="48" width="100" height="190" rx="8" fill="#ef4444" opacity="0.10" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_u3)"/>
  <text x="715" y="70" text-anchor="middle" fill="#fca5a5" font-size="11" font-weight="700">⑥ 安全</text>
  <text x="715" y="90" text-anchor="middle" fill="#94a3b8" font-size="9">硬编码密钥</text>
  <text x="715" y="106" text-anchor="middle" fill="#94a3b8" font-size="9">危险 DDL/DML</text>
  <text x="715" y="122" text-anchor="middle" fill="#94a3b8" font-size="9">破坏性删除</text>
  <text x="715" y="168" text-anchor="middle" fill="#64748b" font-size="9">红线，独立于</text>
  <text x="715" y="184" text-anchor="middle" fill="#64748b" font-size="9">功能正确性</text>
  <text x="400" y="268" text-anchor="middle" fill="#475569" font-size="11">顺序逻辑：更快更便宜的先跑；前置失败时后续无意义；红线独立兜底</text>
  <text x="400" y="292" text-anchor="middle" fill="#64748b" font-size="10">这是"为什么前置"的完整论证——不是六步平铺，是有依赖链的六步</text>
</svg>
```

### 2.2 为什么不收敛成"3 步"或"1 条命令"

有人会问：为什么不把六步合成一条命令，或者干脆只跑 `go test ./...`？

因为**错误类型不同，失败信息就不同，退回路径也不同**。看门禁判定表就明白：

| 检查项 | 通过标准 | 失败处理（退回谁） |
|--------|---------|------------------|
| 编译 | 0 error | 退回 ② 编码 |
| 静态分析 | 0 violation | 退回 ② 编码 |
| 竞态检测 | 0 data race | 退回 ②（严重） |
| 架构约束 | 全部通过 | 退回 ②（架构腐化，严重） |
| 单元测试 | 0 failed | 退回 ② / ③ |
| 覆盖率 | 核心 ≥80% | 退回 ③ 补测试 |
| 安全扫描 | 0 命中 | 退回 ②（安全红线） |

六步之所以必须分开，是因为它们**失败时的处置完全不同**：

- 编译失败 → 是"代码本身有问题" → 退回编码阶段改代码。
- 覆盖率不达标 → 代码可能没错，是"测试没写够" → 退回测试阶段补测试。
- 安全红线 → 是最严重的一类 → 无论其他全绿，必须退回。

**如果合成一条命令，你就失去了"该退回谁"的信息。** 门禁的意义不仅是"拦住"，更是"拦住后正确地指路"。

## 三、门禁判定表：红灯是如何"不可协商"的

### 3.1 整张判定表就是门禁的"宪法"

`/unit-test-ci` 把每一步的"通过标准"写成一张可被机器执行的判定表，而不是一段散文。这张表是门禁的**宪法**——它规定了"什么算过、什么算不过、不过了退回谁"。

判定表的关键列有三个：**检查项、通过标准、失败处置**。这三列缺一不可：

- 只有检查项没有标准 → 无法机械化，走到哪算哪。
- 只有标准没有处置 → 红灯了也不知道该怎么办。
- 只有处置没有标准 → 处置对象都不明确。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 240" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_u5" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_u5"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="240" fill="url(#bg_u5)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">门禁判定表三要素</text>
  <rect x="60" y="50" width="210" height="110" rx="10" fill="#3b82f6" opacity="0.10" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_u5)"/>
  <text x="165" y="76" text-anchor="middle" fill="#93c5fd" font-size="13" font-weight="700">检查项</text>
  <text x="165" y="98" text-anchor="middle" fill="#94a3b8" font-size="10">编译/静态/竞态/架构</text>
  <text x="165" y="116" text-anchor="middle" fill="#94a3b8" font-size="10">测试/覆盖率/安全</text>
  <text x="165" y="142" text-anchor="middle" fill="#64748b" font-size="10">缺了它：不知道该验什么</text>
  <rect x="300" y="50" width="200" height="110" rx="10" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_u5)"/>
  <text x="400" y="76" text-anchor="middle" fill="#fde68a" font-size="13" font-weight="700">通过标准</text>
  <text x="400" y="98" text-anchor="middle" fill="#94a3b8" font-size="10">0 error / 0 violation</text>
  <text x="400" y="116" text-anchor="middle" fill="#94a3b8" font-size="10">覆盖率 ≥80% / 0 命中</text>
  <text x="400" y="142" text-anchor="middle" fill="#64748b" font-size="10">缺了它：无法机械化</text>
  <rect x="530" y="50" width="210" height="110" rx="10" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_u5)"/>
  <text x="635" y="76" text-anchor="middle" fill="#86efac" font-size="13" font-weight="700">失败处置</text>
  <text x="635" y="98" text-anchor="middle" fill="#94a3b8" font-size="10">退回 ② 编码 / 退回 ③ 测试</text>
  <text x="635" y="116" text-anchor="middle" fill="#94a3b8" font-size="10">安全红线上报</text>
  <text x="635" y="142" text-anchor="middle" fill="#64748b" font-size="10">缺了它：红灯不知怎么办</text>
  <text x="400" y="196" text-anchor="middle" fill="#475569" font-size="11">三要素齐备，门禁才从"建议"变成"体系"</text>
  <text x="400" y="220" text-anchor="middle" fill="#64748b" font-size="10">/unit-test-ci 的判定逻辑完全是机械可执行的——这正是它可信的原因</text>
</svg>
```

### 3.2 红灯不可协商的具体含义

"红灯不可协商"是老生常谈，但 `/unit-test-ci` 把它落实成了三条可操作的规则：

**规则一：红灯即不过，不解释。** 不能用"这个 lint 告警是历史遗留"来解释掉一个红灯。红灯 = 不过，没有解释通道。

**规则二：红灯即退回，不修补带病提交。** 编译失败退编码、覆盖率不足退测试、安全红线单独上报。红灯不是"改一行继续跑"，而是"回到正确的阶段重新来"。

**规则三：红绿状态以机器输出为准，不以 Agent 汇报为准。** 判定不看"Agent 说自己检查过了"，而看命令的真实退出码与留档。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 200" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_u6" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_u6"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="200" fill="url(#bg_u6)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="20" font-weight="700">红灯不可协商的三条规则</text>
  <rect x="40" y="50" width="230" height="80" rx="10" fill="#ef4444" opacity="0.10" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_u6)"/>
  <text x="155" y="74" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">① 红灯即不过</text>
  <text x="155" y="98" text-anchor="middle" fill="#94a3b8" font-size="10">没有解释通道，没有"特殊情况"</text>
  <text x="155" y="116" text-anchor="middle" fill="#64748b" font-size="9">过 = 已通过；不过 = 已退回</text>
  <rect x="290" y="50" width="230" height="80" rx="10" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_u6)"/>
  <text x="405" y="74" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">② 红灯即退回</text>
  <text x="405" y="98" text-anchor="middle" fill="#94a3b8" font-size="10">编译→② / 覆盖率→③ / 安全→上报</text>
  <text x="405" y="116" text-anchor="middle" fill="#64748b" font-size="9">回到正确阶段重新来</text>
  <rect x="540" y="50" width="220" height="80" rx="10" fill="#3b82f6" opacity="0.10" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_u6)"/>
  <text x="650" y="74" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">③ 以机器输出为准</text>
  <text x="650" y="98" text-anchor="middle" fill="#94a3b8" font-size="10">真实退出码 + 留档为凭</text>
  <text x="650" y="116" text-anchor="middle" fill="#64748b" font-size="9">不信"Agent 说检查过了"</text>
  <text x="400" y="170" text-anchor="middle" fill="#475569" font-size="11">三条规则的共同内核：门禁的权威来自"可验证的机器输出"，不来自任何人的自觉</text>
</svg>
```

### 3.3 失败处置的"退回路径"细节

门禁判定表里最关键的设计，是**每条失败都有明确的退回路径**。我们用一张图看清"红灯后去哪"：

| 红灯来源 | 退回阶段 | 对应命令 | 重新跑前的前置 |
|---------|---------|---------|--------------|
| stage-1 编译失败 | ② 编码 | `coding-skill` | 修复断引用/语法 |
| stage-2 静态失败 | ② 编码 | `coding-skill` | 修 lint / 降复杂度 |
| stage-3 竞态失败 | ③ 测试 或 ② | `unit-test-write` | 加锁/重设计并发 |
| stage-4 架构失败 | ② 编码 | `coding-skill` | 调整依赖方向 |
| stage-5 测试失败 | ③ 测试 | `unit-test-write` | 修断言/补场景 |
| stage-6 覆盖率不足 | ③ 测试 | `unit-test-write` | 补核心逻辑用例 |
| stage-7 安全命中 | ② 编码（严重） | `coding-skill` | 去掉密钥/危险操作 |

**注意**：覆盖率不足退回的是 **③ 测试阶段**（写测试），而不是 ②（改代码）——因为代码没错，是测试没写够。这个区分，恰恰是"红灯不可协商"里最容易被忽略、也最有价值的部分：**门禁不只告诉你"不行"，还告诉你"为什么不行、去哪修"。**

## 四、与流水线的衔接：⑤ 在整条流水线中的位置

`/unit-test-ci` 不是孤立的一条命令，而是流水线中的一个**阶段闸门**。要理解它，必须把它放回整条流水线里看：

```
需求（analyzing）→ 编码（coding）→ 单测（testing）→ 评审（reviewing）→ [unit-test-ci：ci] → 部署验证（verifying）→ done
```

它的前后各有明确契约：

- **前置**：change 的 `status` 必须是 `ci`，且已通过 `/expert-reviewer`（0 严重问题）。如果还在 `reviewing`，`/unit-test-ci` 不应接手。
- **后置**：全绿后将 `status` 置为 `verifying`，交 `/deploy-verify` 做部署冒烟与健康检查。
- **输入**：`change.md`（知道要验证什么范围）+ 语言产品的门禁命令表（知道怎么验）。
- **输出**：质量报告 + 真实命令留档 + 状态迁移。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 240" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_u7" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_u7"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="240" fill="url(#bg_u7)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="20" font-weight="700">/unit-test-ci 在流水线中的前后契约</text>
  <rect x="40" y="50" width="150" height="60" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_u7)"/>
  <text x="115" y="74" text-anchor="middle" fill="#fde68a" font-size="12">reviewing → ci</text>
  <text x="115" y="94" text-anchor="middle" fill="#94a3b8" font-size="9">expert-reviewer 0严重</text>
  <line x1="190" y1="80" x2="225" y2="80" stroke="#475569" stroke-width="2"/>
  <polygon points="231,80 223,75 223,85" fill="#94a3b8"/>
  <rect x="240" y="45" width="180" height="70" rx="8" fill="#22c55e" opacity="0.14" stroke="#22c55e" stroke-width="2" filter="url(#sh_u7)"/>
  <text x="330" y="66" text-anchor="middle" fill="#86efac" font-size="13" font-weight="700">/unit-test-ci</text>
  <text x="330" y="86" text-anchor="middle" fill="#94a3b8" font-size="9">6 步全绿 → status=verifying</text>
  <text x="330" y="102" text-anchor="middle" fill="#fca5a5" font-size="9">任一红灯 → 退回对应阶段</text>
  <line x1="420" y1="80" x2="455" y2="80" stroke="#475569" stroke-width="2"/>
  <polygon points="461,80 453,75 453,85" fill="#94a3b8"/>
  <rect x="470" y="50" width="150" height="60" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_u7)"/>
  <text x="545" y="74" text-anchor="middle" fill="#93c5fd" font-size="12">verifying</text>
  <text x="545" y="94" text-anchor="middle" fill="#94a3b8" font-size="9">deploy-verify 冒烟</text>
  <rect x="640" y="50" width="120" height="60" rx="8" fill="#64748b" opacity="0.12" stroke="#64748b" stroke-width="1.5" filter="url(#sh_u7)"/>
  <text x="700" y="74" text-anchor="middle" fill="#cbd5e1" font-size="12">done</text>
  <text x="700" y="94" text-anchor="middle" fill="#94a3b8" font-size="9">完成归档</text>
  <text x="400" y="150" text-anchor="middle" fill="#475569" font-size="11">输入：change.md + 语言门禁命令表 · 输出：质量报告 + 命令留档 + 状态迁移</text>
  <text x="400" y="178" text-anchor="middle" fill="#64748b" font-size="10">门禁不审"做得对不对"——审查在 reviewing 已做完；门禁验"代码有没有硬伤"</text>
  <text x="400" y="202" text-anchor="middle" fill="#64748b" font-size="10">分工：reviewing 管"该做的事做了没"，unit-test-ci 管"做出来的东西能不能跑"</text>
  <text x="400" y="226" text-anchor="middle" fill="#475569" font-size="10">只有人审 + 机器验都通过，才配进入 verifying——缺一不可</text>
</svg>
```

### 4.1 与 `harness-quality` 的分工

质量验收在流水线中有两个密切相关的角色，容易混淆：

- **`unit-test-ci`（本技能）**：**机械化执行**全部门禁——它回答"有没有硬伤"（能不能编译、有没有竞态、覆盖率够不够）。它不判"业务对不对"。
- **`harness-quality`**：在机械化执行之上做**客观判定 + 可追溯 + 人签字**——它把 unit-test-ci 的真实数据整理成报告、做 flow→test 映射对账，最后**由人类签字放行**。

一句话区分：**unit-test-ci 负责"机器能不能跑"，harness-quality 负责"跑出来的证据能不能放行"**。前者是执行，后者是判定。

### 4.2 状态机的"停机交人"原则

`/unit-test-ci` 是**自动化**的，但它不是"无人值守到底"的。它遵循整条流水线的"关键节点停机交人"原则：

- 执行门禁：机器跑，Agent 驱动——**这步可以全自动**。
- 红灯退回：退回对应阶段，交给下一轮循环——自动。
- 全绿放行：`status → verifying`，交 `/deploy-verify`——**部署是外发操作，永远留人**。

所以 `/unit-test-ci` 的"自动化"边界很清晰：**自动化限于"验证"，不延伸到"部署发布"**。验证全绿 ≠ 可以自动发布。

## 五、实战记录：一次被拦下的提交（含修复闭环）

### 5.1 场景：佣金结算模块改造

假设一个 change（`C-23`）改造佣金结算模块的并发处理。Agent 按流程执行 `/unit-test-ci C-23`。

**第一步：编译（stage-1）**

```bash
$ go build ./...
# 输出空，退出码 0 → 编译通过
[PASS] stage-1 编译检查: 0 error
```

**第二步：静态分析（stage-2）**

```bash
$ go vet ./... && golangci-lint run
# 输出：internal/settlement/calc.go:120:14: shadow: declaration of "err" shadows declaration at line 118
#        internal/settlement/calc.go:56:6: gocyclo: cyclomatic complexity 12 > 10
[FAIL] stage-2 静态分析: 2 violation
```

红灯了。但注意：这不是"哦不行"，判定表告诉 Agent **退回 ② 编码阶段**，因为这是代码质量问题。

**第三步：修复回到 ②**

Agent 回到编码阶段（`coding-skill`），修掉 `err` 变量遮蔽和复杂度超标。然后**重新从编译开始跑**——因为门禁是无状态的，修复后必须从头验一遍，不能"只补跑静态"。

**第四步：竞态（stage-3）**

```bash
$ go test -race ./...
# 输出：WARNING: DATA RACE
#        Write at 0x00c000... by goroutine 42: internal/settlement/calc.go:88
#        Previous write ... by goroutine 17: internal/settlement/calc.go:88
[FAIL] stage-3 竞态检测: 1 data race
```

这正是"测试绿也发现不了"的那类问题——数据竞争在 `go test`（不带 race）下可能永远不暴露。判定表：**退回 ②（严重）**，因为并发正确性是结构问题，不是补个用例能解决的。

**第五步：加锁修复 → 重新全跑**

在共享写点加锁（或改为无共享设计）后，重新跑全六步：编译 → 静态 → 竞态 → 架构 → 测试覆盖率 → 安全。

**第六步：覆盖率（stage-5 后半）**

```bash
$ go test ./... -coverprofile=cover.out && go tool cover -func=cover.out | tail -1
# total: 76.3%
[FAIL] stage-5 覆盖率: total 76.3% < 80%
```

判定表：**退回 ③ 测试阶段补测试**——因为这一次代码没错（功能测试全过），是**测试没写够**。Agent 去 `unit-test-write`，补了佣金边界场景的用例，覆盖率到 82.5%。

**第七步：全绿 → 放行**

```
[PASS] stage-1 编译检查: 0 error
[PASS] stage-2 静态分析: 0 violation
[PASS] stage-3 竞态检测: 0 data race
[PASS] stage-4 架构约束: 全部通过
[PASS] stage-5 单元测试: 0 failed · 覆盖率 82.5% (≥80%)
[PASS] stage-6 安全扫描: 0 命中
→ status: ci → verifying（交 /deploy-verify）
```

这个案例的价值不在"最后绿了"，而在**它展示了一条被红灯拦下、又正确地退回到对应阶段修复的完整闭环**：编译错回编码、覆盖率不足回测试、竞态作为严重问题单独处理——每一步都是判定表在"指路"，Agent 只是执行者。

## 六、设计哲学：为什么"不自动修复"

有人会问：既然门禁能发现问题，为什么不让 Agent 直接自动修复，一步到位？

`/unit-test-ci` 的设计选择是：**发现问题 → 退回阶段，而不是现场自动修复**。理由有三：

**1. 修复的"知识边界"不同。** 编译错误可以机械改，但数据竞争怎么修（加锁？无锁设计？）是设计决策；覆盖率不够补哪些用例是测试策略决策。这些决策不属于"门禁"，属于"编码"和"测试"阶段。把决策权和上下文流转留给对应阶段，是职责划分。

**2. 修复的"上下文"不同。** 门禁阶段携带的上下文是"验证口径"（命令、标准、留档）；而修复需要"需求 + 设计 + 代码全貌"。在门禁现场修复，等于在缺上下文的场景做需要上下文的决策——质量必然下降。

**3. 退回是流程的一部分。** 退回 ②/③ 不是"失败"，而是流水线的正常回流。流水线本来就要支持"评审打回编码"，门禁退回只是把这种回流延伸到机器检查。拒绝回流 = 拒绝在源头修问题 = 带病前行。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 240" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_u8" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_u8"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="240" fill="url(#bg_u8)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">为什么"不自动修复"：三条理由</text>
  <rect x="40" y="52" width="230" height="120" rx="10" fill="#3b82f6" opacity="0.10" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_u8)"/>
  <text x="155" y="76" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">① 知识边界不同</text>
  <text x="155" y="100" text-anchor="middle" fill="#94a3b8" font-size="10">怎么修数据竞争是设计决策</text>
  <text x="155" y="118" text-anchor="middle" fill="#94a3b8" font-size="10">补哪些用例是测试策略决策</text>
  <text x="155" y="142" text-anchor="middle" fill="#64748b" font-size="9">不属于门禁，属于编码/测试阶段</text>
  <rect x="290" y="52" width="230" height="120" rx="10" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_u8)"/>
  <text x="405" y="76" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">② 上下文不同</text>
  <text x="405" y="100" text-anchor="middle" fill="#94a3b8" font-size="10">门禁只带"验证口径"</text>
  <text x="405" y="118" text-anchor="middle" fill="#94a3b8" font-size="10">修复需要"需求+设计+代码全貌"</text>
  <text x="405" y="142" text-anchor="middle" fill="#64748b" font-size="9">缺上下文做决策，质量必降</text>
  <rect x="540" y="52" width="220" height="120" rx="10" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_u8)"/>
  <text x="650" y="76" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">③ 退回是流程的一部分</text>
  <text x="650" y="100" text-anchor="middle" fill="#94a3b8" font-size="10">退回 = 回流转接，不是失败</text>
  <text x="650" y="118" text-anchor="middle" fill="#94a3b8" font-size="10">拒绝回流 = 带病前行</text>
  <text x="650" y="142" text-anchor="middle" fill="#64748b" font-size="9">与"评审打回编码"同构</text>
  <text x="400" y="204" text-anchor="middle" fill="#475569" font-size="11">门禁的价值 = 发现问题 + 正确指路；修复的价值 = 在正确的阶段做正确的决策</text>
  <text x="400" y="226" text-anchor="middle" fill="#64748b" font-size="10">两者分开，才能各自做到极致</text>
</svg>
```

### 6.1 门禁的"权威"从哪来

最后回到本文的主题：门禁的权威，不来自"Agent 多认真"，而来自**"机器输出可验证、判定标准可执行、失败处置有路径"**这三件事。

- 可验证 → 退出码 + 留档。
- 可执行 → 判定表写死了通过标准。
- 有路径 → 每个红灯都指向一个退回阶段。

这三件事，正是 `harness-quality` 报告中"客观数据 → 达标判定 → 人签字"三段结构的底层支撑：**先把 data 做实（unit-test-ci），再把 judgment 做完（quality-report），最后把 authority 交给人（人签字）**。

## 七、写在最后

`/unit-test-ci` 看起来只是一条"跑测试的命令"，但它的设计浓缩了 harness 流水线对**质量门禁**的全部思考：

1. **能机械化的，就不要靠人**——编译、静态、竞态、架构、覆盖率、安全，全部交给确定性命令。
2. **判断要有依据，红灯要有路径**——判定表给了每个红灯一个"退回谁"的答案，而不是一句模糊的"不行"。
3. **自动化有边界**——验证可以放手让机器跑，但"放行签字"和"部署发布"永远留人。
4. **门禁之后还有门禁**——unit-test-ci 通过 ≠ 放行，还要 `harness-quality` 出报告、人签字，才进入 `verifying`。

一句话记住它：**/unit-test-ci 是把"质量靠感觉"变成"质量靠证据"的那道闸。** 它不负责替你想，它负责让"你想的都对"这件事可以被验证。