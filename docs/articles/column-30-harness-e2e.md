# /harness-e2e：整条链路，真跑一遍才算数

> 命令深度拆解 · 第 30 篇 · 约 9000 字 · 7 个 SVG 图

---

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 300" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_e0" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_e0"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="300" fill="url(#bg_e0)" rx="10"/>
  <text x="400" y="28" text-anchor="middle" fill="#e2e8f0" font-size="26" font-weight="700">/harness-e2e：整条链路，真跑一遍才算数</text>
  <rect x="60" y="55" width="320" height="95" rx="10" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_e0)"/>
  <text x="220" y="80" text-anchor="middle" fill="#86efac" font-size="14" font-weight="700">从用户入口出发 穿全程</text>
  <text x="220" y="104" text-anchor="middle" fill="#94a3b8" font-size="11">接口 → 服务 → 存储 → 消息</text>
  <text x="220" y="126" text-anchor="middle" fill="#64748b" font-size="10">主链路 + 降级链路都要验</text>
  <rect x="420" y="55" width="320" height="95" rx="10" fill="#ef4444" opacity="0.12" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_e0)"/>
  <text x="580" y="80" text-anchor="middle" fill="#fca5a5" font-size="14" font-weight="700">真跑才算数</text>
  <text x="580" y="104" text-anchor="middle" fill="#94a3b8" font-size="11">记录真实退出码与输出</text>
  <text x="580" y="126" text-anchor="middle" fill="#64748b" font-size="10">禁止\"我看了一下应该没问题\"</text>
  <text x="400" y="185" text-anchor="middle" fill="#475569" font-size="13">四步流程，三条铁律</text>
  <rect x="60" y="205" width="220" height="45" rx="8" fill="#3b82f6" opacity="0.10" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_e0)"/>
  <text x="170" y="234" text-anchor="middle" fill="#93c5fd" font-size="12">① 提取链路</text>
  <rect x="300" y="205" width="220" height="45" rx="8" fill="#a855f7" opacity="0.10" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_e0)"/>
  <text x="410" y="234" text-anchor="middle" fill="#d8b4fe" font-size="12">② 生成用例 → ③ 真跑</text>
  <rect x="540" y="205" width="220" height="45" rx="8" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_e0)"/>
  <text x="650" y="234" text-anchor="middle" fill="#fde68a" font-size="12">④ 结论与状态流转</text>
  <text x="400" y="280" text-anchor="middle" fill="#64748b" font-size="10">核心洞察：单测证明\"模块内正确\"，E2E 证明\"串起来正确\"——两者缺一不可</text>
</svg>
```

## 一、/harness-e2e 解决的是什么问题

### 1.1 单测全绿，上线还是炸

单元测试是流水线的地基：覆盖率 80%、全部绿、lint 干净——听起来可以上线了。然后呢？

现实里经常出现这样的场景：

- 下单模块单测全绿——但下单后扣库存的消息没发出去；
- 扣库存模块单测全绿——但库存更新和订单状态更新发生在两个事务里，一半成功一半失败；
- 出账单模块单测全绿——但账单服务依赖的接口协议改了，下游没同步。

每个模块内部都是对的，**串起来就错**。因为跨模块的错误不在任何单个模块的"管辖范围"里——它们在模块与模块的接缝处。

`/harness-e2e` 存在的目的：**从用户入口出发，穿过真实接口/服务/存储，验证整条业务链路真的走通**。它补上的正是单测的空缺——单测验证"模块内正确"，E2E 验证"跨模块/跨服务串起来正确"。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 260" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_e1" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_e1"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="260" fill="url(#bg_e1)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">接缝处的 bug：模块都对，串起来错</text>
  <rect x="40" y="55" width="140" height="70" rx="8" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_e1)"/>
  <text x="110" y="79" text-anchor="middle" fill="#86efac" font-size="11" font-weight="700">下单模块</text>
  <text x="110" y="101" text-anchor="middle" fill="#94a3b8" font-size="9">单测 🟢</text>
  <text x="110" y="117" text-anchor="middle" fill="#64748b" font-size="8">消息没发出去</text>
  <rect x="240" y="55" width="140" height="70" rx="8" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_e1)"/>
  <text x="310" y="79" text-anchor="middle" fill="#86efac" font-size="11" font-weight="700">扣库存模块</text>
  <text x="310" y="101" text-anchor="middle" fill="#94a3b8" font-size="9">单测 🟢</text>
  <text x="310" y="117" text-anchor="middle" fill="#64748b" font-size="8">跨事务半失败</text>
  <rect x="440" y="55" width="140" height="70" rx="8" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_e1)"/>
  <text x="510" y="79" text-anchor="middle" fill="#86efac" font-size="11" font-weight="700">出账单模块</text>
  <text x="510" y="101" text-anchor="middle" fill="#94a3b8" font-size="9">单测 🟢</text>
  <text x="510" y="117" text-anchor="middle" fill="#64748b" font-size="8">协议已改未同步</text>
  <text x="110" y="165" text-anchor="middle" fill="#475569" font-size="12">🔴</text>
  <text x="400" y="165" text-anchor="middle" fill="#475569" font-size="12">接缝处：谁都不负责</text>
  <text x="690" y="165" text-anchor="middle" fill="#475569" font-size="12">🔴</text>
  <text x="400" y="196" text-anchor="middle" fill="#475569" font-size="11">单测管\"模块内\"，E2E 管\"接缝处\"——这是互补关系，不是重叠</text>
  <text x="400" y="224" text-anchor="middle" fill="#64748b" font-size="10">cross-module 错误只在串起来的那一刻显形</text>
  <text x="400" y="246" text-anchor="middle" fill="#64748b" font-size="10">E2E = 把\"串起来\"变成一次显式的、有证据的验证</text>
</svg>
```

### 1.2 与 unit-test-write 的互补

`/harness-e2e` 与 `/unit-test-write` 是**互补**而非替代：

| 维度 | unit-test-write（单测） | harness-e2e（端到端） |
|------|----------------------|---------------------|
| 验证对象 | 模块内的函数/类 | 跨模块/跨服务链路 |
| 入口 | 函数调用 | 用户入口（接口/UI/消息） |
| 运行环境 | 纯内存，可注入 mock | 真实服务 + 存储 + 下游 |
| 回答的问题 | "这段逻辑对吗" | "整条业务流走通了吗" |
| 失败定位 | 精确到函数 | 精确到链路环节 |

在 `/harness-quality` 的 flow→test 映射矩阵里，这两者各司其职：**关键业务流的直接测试可由 E2E 承担**——单测覆盖"行"，E2E 覆盖"流"。你的业务流（flow）必须有直接测试（test），而 E2E 正是"业务流级"测试的主力。

### 1.3 主链路与降级链路：都要测

`/harness-e2e` 提取链路时，不只测"开心路径"：

- **主链路**：业务的核心路径——"下单 → 扣库存 → 发消息 → 出账单"完整走通；
- **降级链路**：异常/降级路径——"库存不足时下单被拒"或"扣库存失败时订单回滚"。

降级链路常常被忽略，但它是线上事故的高发区。主链路测的是"正常世界"，降级链路测的是"世界出问题时系统还守得住"——两者都是"业务流"的一部分，都要有 E2E 用例。

## 二、前置：环境与契约就位

执行 E2E 有两个前置，缺一不可：

1. **服务可运行**：按语言包 `deploy-verify` 技能的命令段读取启动命令，启动 dev 环境（或已有测试环境）。E2E 是"真跑"，所以服务必须真的在跑——从这个意义上，E2E 的前置就是 deploy-verify 的产出（一个可用的环境）。
2. **契约已加载**：读取 `.harness/wiki/接口协议.md` + `数据模型.md`，理解链路中的契约。E2E 用例里的请求/响应都要对照契约来写——你不知道接口长什么样，就没法验证链路走没走通。

前置的用意：**E2E 不是"理想世界里的推演"，是"真实环境里的事实"**。环境没起来、契约没搞清，写出来的"用例"只能是纸面推演——那正是 E2E 要消灭的东西。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 220" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_e2" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_e2"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="220" fill="url(#bg_e2)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">两个前置：环境真在跑 + 契约已加载</text>
  <rect x="40" y="50" width="340" height="110" rx="10" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_e2)"/>
  <text x="210" y="76" text-anchor="middle" fill="#86efac" font-size="13" font-weight="700">① 服务可运行</text>
  <text x="210" y="102" text-anchor="middle" fill="#94a3b8" font-size="10">deploy-verify 命令段里读启动命令</text>
  <text x="210" y="124" text-anchor="middle" fill="#94a3b8" font-size="10">dev 环境启动（或测试环境）</text>
  <text x="210" y="146" text-anchor="middle" fill="#64748b" font-size="9\">E2E 的前置 = deploy-verify 的产出</text>
  <rect x="420" y="50" width="340" height="110" rx="10" fill="#3b82f6" opacity="0.10" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_e2)"/>
  <text x="590" y="76" text-anchor="middle" fill="#93c5fd" font-size="13" font-weight="700">② 契约已加载</text>
  <text x="590" y="102" text-anchor="middle" fill="#94a3b8" font-size="10">接口协议.md + 数据模型.md</text>
  <text x="590" y="124" text-anchor="middle" fill="#94a3b8" font-size="10">请求/响应对照契约来写</text>
  <text x="590" y="146" text-anchor="middle" fill="#64748b" font-size="9">不懂契约就无从验证\"走没走通\"</text>
  <text x="400" y="196" text-anchor="middle" fill="#475569" font-size="11">E2E 不是推演，是真实环境里的事实——前置不全，用例只是纸面设想</text>
</svg>
```

## 三、四步流程：提取 → 生成 → 执行 → 结论

### Step 1: 提取链路

从 change.md 的 AC + 影响面，列出需要验证的**主链路**与**降级链路**。每条链路包含三个要素：

- **入口动作**（一个）——用户触发点：某个接口调用 / UI 点击 / 消息到达；
- **关键中间步骤**——链路被接缝分割的骨架：A 模块 → 消息 → B 模块 → 存储；
- **期望终态**——链路跑完必须成立的事实：响应码、数据落库、消息发出。

提取链路的质量决定了 E2E 的质量：**链路漏了一条，那个链路就永远没人测**。所以要对着 AC 逐条核对，确保每条有跨模块/跨服务特征的 AC 都有一条对应的链路。

### Step 2: 生成用例（写 .harness/changes/<id>/e2e.md）

对每条链路输出用例卡：

```markdown
## 用例 E2E-<n>: <链路名>
- 前置: <数据/服务状态准备>
- 步骤:
  1. <入口动作（接口/UI/消息）>
  2. <中间步骤>
  3. <中间步骤>
- 期望终态:
  - <结果 1（响应/数据落库/消息发出）>
  - <结果 2>
- 验证方式: <真实调用 / 自动化脚本>
```

用例卡的三要素（前置/步骤/终态）刻意与链路骨架对齐：**前置回答"要准备什么"，步骤回答"怎么走"，终态回答"算走通"**。

### Step 3: 执行（真跑）

执行层是本技能铁律落地的地方：

- **优先自动化**：写成可执行脚本/测试，真跑并记录**真实退出码与输出**。自动化用语言包 `unit-test-write` 已渲染的测试框架与工具，或 curl/HTTP 脚本；
- **无法自动化的步骤**：列出精确手动操作步骤（点击顺序、输入值、API 参数），标注待人工执行——**诚实标注，不冒充已经跑了**；
- 执行后逐条核对期望终态，产出结果表：

| 用例 | 结果 | 证据（输出/退出码） |
|------|------|-------------------|
| E2E-1 主链路 | 🟢 / 🔴 | <真实输出摘要> |
| E2E-2 降级链路 | 🟢 / 🔴 | <真实输出摘要> |

结果表的"证据"列是 E2E 的灵魂：**没有证据的 🟢 等于没有跑**。证据可以是退出码 0、接口响应体、数据库里的行——只要是执行的真实痕迹。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 260" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_e3" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_e3"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="260" fill="url(#bg_e3)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">结果表：没有证据的 🟢 等于没跑</text>
  <rect x="60" y="50" width="180" height="36" rx="6" fill="#1e293b" stroke="#334155"/>
  <text x="150" y="73" text-anchor="middle" fill="#94a3b8" font-size="11">用例</text>
  <rect x="240" y="50" width="110" height="36" rx="6" fill="#1e293b" stroke="#334155"/>
  <text x="295" y="73" text-anchor="middle" fill="#94a3b8" font-size="11">结果</text>
  <rect x="350" y="50" width="390" height="36" rx="6" fill="#1e293b" stroke="#334155"/>
  <text x="545" y="73" text-anchor="middle" fill="#94a3b8" font-size="11">证据（输出/退出码）</text>
  <rect x="60" y="96" width="180" height="36" rx="6" fill="#0f172a"/>
  <text x="150" y="118" text-anchor="middle" fill="#e2e8f0" font-size="10">E2E-1 主链路</text>
  <rect x="240" y="96" width="110" height="36" rx="6" fill="#22c55e" opacity="0.15"/>
  <text x="295" y="118" text-anchor="middle" fill="#86efac" font-size="11">🟢</text>
  <rect x="350" y="96" width="390" height="36" rx="6" fill="#0f172a"/>
  <text x="545" y="118" text-anchor="middle" fill="#94a3b8" font-size="9">exit 0 · 200 OK · order row id=8821</text>
  <rect x="60" y="142" width="180" height="36" rx="6" fill="#0f172a"/>
  <text x="150" y="164" text-anchor="middle" fill="#e2e8f0" font-size="10">E2E-2 降级链路</text>
  <rect x="240" y="142" width="110" height="36" rx="6" fill="#ef4444" opacity="0.15"/>
  <text x="295" y="164" text-anchor="middle" fill="#fca5a5" font-size="11">🔴</text>
  <rect x="350" y="142" width="390" height="36" rx="6" fill="#0f172a"/>
  <text x="545" y="164" text-anchor="middle" fill="#94a3b8" font-size="9">500 · stock not reserved · trace=8f2a</text>
  <text x="400" y="216" text-anchor="middle" fill="#475569" font-size="11">证据 = 执行的真实痕迹（退出码/响应体/落库行）</text>
  <text x="400" y="242" text-anchor="middle" fill="#64748b" font-size="10">写不出证据的"通过"，等于没跑过</text>
</svg>
```

### Step 4: 结论与状态流转

- **全 🟢** → 更新 change.md 状态（`verifying → done`，若部署验证通过）；
- **有 🔴** → 如实记录失败点与复现信息，退回对应阶段（编码/集成），**不得按通过处理**。

注意"若部署验证通过"这个条件：E2E 通常与 `/deploy-verify` 联动——E2E 证明"业务链路走通"，deploy-verify 证明"部署本身健康"，两者一起才有资格把 change 置为 done。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 240" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_e4" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_e4"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="240" fill="url(#bg_e4)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">Step 4：全绿才前进，红必须退回</text>
  <rect x="40" y="50" width="340" height="120" rx="10" fill="#22c55e" opacity="0.08" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_e4)"/>
  <text x="210" y="76" text-anchor="middle" fill="#86efac" font-size="13" font-weight="700">全 🟢 → 前进</text>
  <text x="210" y="102" text-anchor="middle" fill="#94a3b8" font-size="10">主链路 + 降级链路全过</text>
  <text x="210" y="124" text-anchor="middle" fill="#94a3b8" font-size="10">部署验证通过 → verifying → done</text>
  <text x="210" y="146" text-anchor="middle" fill="#64748b" font-size="9">done 不是 E2E 单独说了算</text>
  <rect x="420" y="50" width="340" height="120" rx="10" fill="#ef4444" opacity="0.08" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_e4)"/>
  <text x="590" y="76" text-anchor="middle" fill="#fca5a5" font-size="13" font-weight="700">有 🔴 → 退回</text>
  <text x="590" y="102" text-anchor="middle" fill="#94a3b8" font-size="10">记录失败点 + 复现信息</text>
  <text x="590" y="124" text-anchor="middle" fill="#94a3b8" font-size="10">退回编码/集成，修复后再验</text>
  <text x="590" y="146" text-anchor="middle" fill="#64748b" font-size="9">不得"差不多"按通过处理</text>
  <text x="400" y="196" text-anchor="middle" fill="#475569" font-size="11">E2E 失败 = 红灯 = 退回修复——降标准放行是对整条流水线的背叛</text>
  <text x="400" y="222" text-anchor="middle" fill="#64748b" font-size="10">失败时留下复现信息：下一次修复的人需要"怎么复现"</text>
</svg>
```

## 四、三条铁律：E2E 的价值观

### 4.1 真跑才算数

**E2E 用例必须真实执行并留证据，禁止"我看了一下应该没问题"替代。**

这条铁律针对的是最常见的偷懒形态：不跑，但凭"直觉"填一个 🟢。为什么直觉不可信？因为 E2E 验证的恰恰是直觉最容易错的地方——**接缝**。模块内的行为你可以推理，跨服务的行为只有跑了才知道。

"真跑"意味着：真实的请求打到真实的接口，穿过真实的存储，留下真实的痕迹。没有痕迹的通过，只是你希望它通过。

### 4.2 主链路必测

**每次 change 影响的主链路必须有 E2E 用例（或已有用例覆盖），不因"改动小"而跳过。**

"改动小"是对 E2E 最大的误解来源。改一个字段的类型，接口协议变了——单测看着全绿，但整个链路里所有消费这个字段的地方都可能错。正因为改动小，才更要真跑一遍主链路确认没破。**覆盖优先级：主链路永远是必测项，降级链路按影响面决定**。

### 4.3 失败如实报

**E2E 失败就是红灯，退回修复，不降低标准放行。**

红灯不可怕——红灯是 E2E 存在的意义（它抓到了 bug）。可怕的是把红灯涂成绿灯：降标准放行等于亲手销毁了 E2E 的所有价值。失败时就该承认失败，留下复现信息，退回修复，再跑一遍。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 300" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_e5" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_e5"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="300" fill="url(#bg_e5)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">三条铁律：价值观即行为准则</text>
  <rect x="40" y="50" width="220" height="100" rx="10" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_e5)"/>
  <text x="150" y="76" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">① 真跑才算数</text>
  <text x="150" y="100" text-anchor="middle" fill="#94a3b8" font-size="9">真实执行 + 留证据</text>
  <text x="150" y="120" text-anchor="middle" fill="#64748b" font-size="8">禁止"我看了一下应该没问题"</text>
  <text x="150" y="140" text-anchor="middle" fill="#64748b" font-size="8">没痕迹的通过=没跑过</text>
  <rect x="290" y="50" width="220" height="100" rx="10" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_e5)"/>
  <text x="400" y="76" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">② 主链路必测</text>
  <text x="400" y="100" text-anchor="middle" fill="#94a3b8" font-size="9">影响主链路必有用例</text>
  <text x="400" y="120" text-anchor="middle" fill="#64748b" font-size="8">不因"改动小"而跳过</text>
  <text x="400" y="140" text-anchor="middle" fill="#64748b" font-size="8">改动小 ≠ 影响小</text>
  <rect x="540" y="50" width="220" height="100" rx="10" fill="#ef4444" opacity="0.10" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_e5)"/>
  <text x="650" y="76" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">③ 失败如实报</text>
  <text x="650" y="100" text-anchor="middle" fill="#94a3b8" font-size="9">红灯 = 退回修复</text>
  <text x="650" y="120" text-anchor="middle" fill="#64748b" font-size="8">不降低标准放行</text>
  <text x="650" y="140" text-anchor="middle" fill="#64748b" font-size="8">涂绿 = 销毁 E2E 价值</text>
  <text x="400" y="195" text-anchor="middle" fill="#475569" font-size="11">铁律不是限制，是 E2E 可信赖的前提</text>
  <text x="400" y="225" text-anchor="middle" fill="#64748b" font-size="10">没有铁律的 E2E 会迅速退化成"填表仪式"</text>
  <text x="400" y="250" text-anchor="middle" fill="#64748b" font-size="10">真跑 → 全覆盖 → 如实报，三层交织才是可信赖</text>
  <text x="400" y="278" text-anchor="middle" fill="#64748b" font-size="10">红灯是 E2E 存在的意义：它抓到了 bug</text>
</svg>
```

## 五、与相邻技能的分工

| 场景 | 归属 |
|------|------|
| 单测 | `/unit-test-write` |
| 部署冒烟/健康检查 | `/deploy-verify` |
| 质量判定 + flow→test 对账 | `/harness-quality` |
| E2E 用例生成/执行 | **本技能**（`/harness-e2e`） |

- **unit-test-write**：模块内的函数级测试，E2E 的"地基"——E2E 通过但单测乱，代码照样难维护；
- **deploy-verify**：部署冒烟/健康检查，确认"服务起来了"；E2E 确认"业务链路走通了"——一个看部署，一个看业务；
- **harness-quality**：flow→test 映射矩阵会揭示"哪条业务流没有端到端覆盖"——这正是本技能的触发信号。E2E 的结果反过来也是 quality 判定质量的数据源。

## 六、完成标志

`/harness-e2e` 的完成标志有三个，缺一不可：

1. **用例卡已落盘** `.harness/changes/<id>/e2e.md`——链路提取与用例设计成了可追溯的文档；
2. **用例已执行并留真实证据**（退出码/输出），结果表已产出——真跑的证据进了结果表；
3. **主链路与降级链路均 🟢**（或 🔴 已如实记录并退回）——要么全过，要么如实退回，没有第三种状态。

这三个标志层层递进：落盘 → 执行 → 结论。落盘不执行是纸面工程，执行不留证据是自我安慰，全绿灯但其实是涂的就更糟。**做到第三步，前两步才有意义。**

## 七、写在最后

`/harness-e2e` 的全部设计，浓缩成四句话：

1. **单测管"模块内"，E2E 管"接缝处"**——跨服务的 bug 只会在串起来的那一刻显形，E2E 就是那一瞬间的显影剂。
2. **主链路必测，降级链路不放过**——正常世界和世界出问题时，都是业务流的一部分。
3. **真跑才算数，证据是灵魂**——没有退出码和输出的 🟢 等于没跑，直觉替代不了执行。
4. **红灯就退回，不降标准**——红灯是 E2E 的价值，涂绿是它的葬礼。

一句话记住它：**/harness-e2e 是流水线的"接缝探测器"——它从用户入口出发，穿过真实接口、服务与存储，用真实证据回答"整条业务流真的走通了吗"。**