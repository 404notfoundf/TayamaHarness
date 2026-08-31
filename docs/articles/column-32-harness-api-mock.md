# /harness-api-mock：接口先定好，数据先 mock 出来

> 命令深度拆解 · 第 32 篇 · 约 9000 字 · 7 个 SVG 图

---

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 300" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_m0" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_m0"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="300" fill="url(#bg_m0)" rx="10"/>
  <text x="400" y="28" text-anchor="middle" fill="#e2e8f0" font-size="26" font-weight="700">/harness-api-mock：接口先定好，数据先 mock 出来</text>
  <rect x="60" y="55" width="320" height="95" rx="10" fill="#a855f7" opacity="0.12" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_m0)"/>
  <text x="220" y="80" text-anchor="middle" fill="#d8b4fe" font-size="14" font-weight="700">契约固定的工具</text>
  <text x="220" y="104" text-anchor="middle" fill="#94a3b8" font-size="11">后端未就绪 → 前端先行开发</text>
  <text x="220" y="126" text-anchor="middle" fill="#64748b" font-size="10">联调不阻塞 · 测试可先跑</text>
  <rect x="420" y="55" width="320" height="95" rx="10" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_m0)"/>
  <text x="580" y="80" text-anchor="middle" fill="#86efac" font-size="14" font-weight="700">忠于契约</text>
  <text x="580" y="104" text-anchor="middle" fill="#94a3b8" font-size="11">字段名/类型/枚举/错误码一致</text>
  <text x="580" y="126" text-anchor="middle" fill="#64748b" font-size="10">否则假数据反而误导</text>
  <text x="400" y="185" text-anchor="middle" fill="#475569" font-size="13">四步流程：提取契约 → 生成 mock → 落地服务 → 校验</text>
  <rect x="60" y="205" width="160" height="45" rx="8" fill="#3b82f6" opacity="0.10" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_m0)"/>
  <text x="140" y="234" text-anchor="middle" fill="#93c5fd" font-size="11">① 提取契约</text>
  <rect x="240" y="205" width="160" height="45" rx="8" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_m0)"/>
  <text x="320" y="234" text-anchor="middle" fill="#fde68a" font-size="11">② 生成 mock 数据</text>
  <rect x="420" y="205" width="160" height="45" rx="8" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_m0)"/>
  <text x="500" y="234" text-anchor="middle" fill="#86efac" font-size="11">③ 落地 mock 服务</text>
  <rect x="600" y="205" width="160" height="45" rx="8" fill="#ef4444" opacity="0.10" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_m0)"/>
  <text x="680" y="234" text-anchor="middle" fill="#fca5a5" font-size="11">④ 校验</text>
  <text x="400" y="280" text-anchor="middle" fill="#64748b" font-size="10">五分支数据：成功/空/边界/错误/鉴权——不只 mock 成功分支</text>
</svg>
```

## 一、/harness-api-mock 解决的是什么问题

### 1.1 前后端并行的最大障碍：后端没就绪

项目迭代里最常见的阻塞：前端要开发下单页面，但下单接口后端还没实现。于是前端的开发节奏被后端的排期绑架——**串行开发，整体变慢**。

`/harness-api-mock` 打破这个串行：按 `.harness/wiki/接口协议.md`（或 change.md 的「契约影响 · REST」）**生成 mock 数据与 mock 服务**，让前端可以先行开发、联调不阻塞、测试可以先把链路跑通。

它的本质不是"造假数据"，而是"**把契约从后端实现的进度里解耦出来**"：接口协议是团队的约定，一旦约定好，任何一方都可以基于这个约定推进——后端写实现，前端消费 mock，测试跑链路。

### 1.2 忠于契约：mock 的第一纪律

mock 数据有一个致命陷阱：**自由发挥**。字段名差一个字母、类型差一点点、枚举值对不上——前端按 mock 开发完，联调时发现全不对，等于开发了个寂寞。

所以本技能的第一纪律是：**mock 数据必须忠于接口协议（字段名/类型/枚举/错误码一致），否则联调时假数据反而误导**。

mock 的作用是"预演真实接口"，模拟得越像越好。mock 的正确打开方式：`接口协议.md` 是唯一的真源，mock 数据是它的忠实投影——投影歪了，预演就变成了误导。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 240" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_m1" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_m1"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="240" fill="url(#bg_m1)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">解释并行：契约把双方解耦</text>
  <rect x="40" y="50" width="340" height="60" rx="8" fill="#1e293b" stroke="#334155"/>
  <text x="210" y="72" text-anchor="middle" fill="#94a3b8" font-size="10">真源：.harness/wiki/接口协议.md</text>
  <text x="210" y="96" text-anchor="middle" fill="#64748b" font-size="9\">路径/方法/请求/响应/错误码/鉴权</text>
  <rect x="60" y="135" width="150" height="65" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_m1)"/>
  <text x="135" y="158" text-anchor="middle" fill="#86efac" font-size="11" font-weight="700">前端</text>
  <text x="135" y="180" text-anchor="middle" fill="#94a3b8" font-size="9">消费 mock 先行开发</text>
  <rect x="310" y="135" width="180" height="65" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_m1)"/>
  <text x="400" y="158" text-anchor="middle" fill="#fde68a" font-size="11" font-weight="700">后端</text>
  <text x="400" y="180" text-anchor="middle" fill="#94a3b8" font-size="9">按契约写真实实现</text>
  <rect x="590" y="135" width="150" height="65" rx="8" fill="#a855f7" opacity="0.12" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_m1)"/>
  <text x="665" y="158" text-anchor="middle" fill="#d8b4fe" font-size="11" font-weight="700">测试</text>
  <text x="665" y="180" text-anchor="middle" fill="#94a3b8" font-size="9">mock 把链路跑通</text>
  <text x="400" y="226" text-anchor="middle" fill="#64748b" font-size="10\">后端实现进度不再是前端的阻塞——契约一旦定好，三方各推各的</text>
</svg>
```

### 1.3 mock 不只在联调期有用

mock 的生命力不止于"后端没就绪"那一刻：

- **E2E/集成测试需要稳定的测试数据**：脱离真实下游依赖——mock 让测试不 flaky、不依赖外部环境；
- **异常分支的演练**：真实环境很难触发"鉴权失败""库存超卖"，mock 可以稳定复现任何分支让前端/测试覆盖到。

所以 mock 不是临时工，它是测试体系中"可控性"的来源：**真实环境不可控，mock 让每种分支变得可控可复现**。

## 二、前置：契约来源与接口清单

执行前置两条：

1. **定位接口来源**：`.harness/wiki/接口协议.md`（首选）或 change.md 的「契约影响 · REST」——契约在哪儿，mock 就忠于哪儿；
2. **确认接口清单**：路径、方法、请求/响应结构、错误码、鉴权方式——每个要 mock 的接口，这五项都得齐。

清单的意义：mock 是全量覆盖还是抽样覆盖，取决于清单齐不齐。**没列进清单的接口就是没 mock 的接口**——清单是 mock 工作的"验收边界"。

## 三、四步流程：契约 → mock 数据 → mock 服务 → 校验

### Step 1: 提取契约

列出需要 mock 的接口，对每个接口记录：

- 路径 + 方法
- 请求参数（query/path/body）与校验规则
- 响应结构（成功/失败/异常分支）+ 状态码
- 依赖的外部调用（是否需 mock 下游）

提取契约的质量决定 mock 的保真度：**契约记漏一个字段，mock 就少一个字段，联调就多一个坑**。所以提取时对照 `接口协议.md` 逐条核对，不凭记忆、不凭猜测。

### Step 2: 生成 mock 数据（五分支）

对每个接口生成**多种形态**的 mock 响应：

| 分支 | 示例数据 | 用途 |
|------|---------|------|
| 正常成功 | <符合契约的完整响应> | 主流程 |
| 空数据 | <空列表/空对象> | 空态 UI |
| 边界值 | <最大/最小/超长/特殊字符> | 边界校验 |
| 错误分支 | <业务错误码 + 错误信息> | 错误处理 |
| 鉴权失败 | <401/403 响应> | 权限拦截 |

五分支是 mock 数据设计的骨架，每个分支对应前端的一种页面状态：

- **正常成功** → 主流程页面能跑通；
- **空数据** → 列表为空的空态 UI 有数据可测；
- **边界值** → 输入校验/截断逻辑有数据可测；
- **错误分支** → 错误提示/重试逻辑有数据可测；
- **鉴权失败** → 401/403 跳登录逻辑有数据可测。

只 mock 成功分支是最常见的偷懒：前端把主流程做完了，一遇错误分支全靠猜——而错误分支恰恰是前端代码里最容易崩的地方。**分支齐全，前端才测得到**。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 300" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_m2" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_m2"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="300" fill="url(#bg_m2)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">五分支数据：每个分支对应一种页面状态</text>
  <rect x="40" y="45" width="220" height="55" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_m2)"/>
  <text x="150" y="67" text-anchor="middle" fill="#86efac" font-size="11" font-weight="700">① 正常成功</text>
  <text x="150" y="88" text-anchor="middle" fill="#64748b" font-size="9">主流程页面</text>
  <rect x="290" y="45" width="220" height="55" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_m2)"/>
  <text x="400" y="67" text-anchor="middle" fill="#93c5fd" font-size="11" font-weight="700">② 空数据</text>
  <text x="400" y="88" text-anchor="middle" fill="#64748b" font-size="9">空态 UI</text>
  <rect x="540" y="45" width="220" height="55" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_m2)"/>
  <text x="650" y="67" text-anchor="middle" fill="#fde68a" font-size="11" font-weight="700">③ 边界值</text>
  <text x="650" y="88" text-anchor="middle" fill="#64748b" font-size="9">校验/截断逻辑</text>
  <rect x="40" y="120" width="220" height="55" rx="8" fill="#ef4444" opacity="0.12" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_m2)"/>
  <text x="150" y="142" text-anchor="middle" fill="#fca5a5" font-size="11" font-weight="700">④ 错误分支</text>
  <text x="150" y="163" text-anchor="middle" fill="#64748b" font-size="9">错误提示/重试逻辑</text>
  <rect x="290" y="120" width="220" height="55" rx="8" fill="#a855f7" opacity="0.12" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_m2)"/>
  <text x="400" y="142" text-anchor="middle" fill="#d8b4fe" font-size="11" font-weight="700">⑤ 鉴权失败</text>
  <text x="400" y="163" text-anchor="middle" fill="#64748b" font-size="9">401/403 跳登录</text>
  <text x="400" y="216" text-anchor="middle" fill="#475569" font-size="11">只 mock 成功分支 = 最危险的偷懒</text>
  <text x="400" y="246" text-anchor="middle" fill="#64748b" font-size="10">错误分支是前端最易崩的地方——没数据可测，全靠猜</text>
  <text x="400" y="270" text-anchor="middle" fill="#64748b" font-size="10">分支齐全，前端才测得到：每个分支都有对应的页面状态</text>
</svg>
```

### Step 3: 落地 mock 服务

按项目可用能力选择方式（参考语言包已渲染技能中的 mock 工具——即 apply-harness 参数表 `HTTP_MOCK_UTIL` 所对应的实际工具）：

1. **内嵌 mock server**：轻量脚本/服务（如 mock 框架），按契约文件返回数据——适合联调期集中 mock；
2. **mock 数据文件**：JSON/fixture 文件，供前端直接 import 或测试加载——适合纯数据消费场景；
3. **网关/代理层 mock**：在开发网关配置 mock 路由——适合前后端已联调基础设施的场景。

三种方式不互斥，按项目实际能力选：有 mock 框架就用内嵌 server，前端要静态数据就用数据文件，有网关就配代理。产出 `.harness/changes/<id>/api-mock.md`：**接口 → mock 数据文件的映射表 + 启动方式**——映射表让人知道哪个接口去哪找 mock 数据，启动方式让人能把 mock 服务跑起来。

### Step 4: 校验

mock 做完不是终点，要验证它真的可用：

- **mock 响应能通过契约校验**（字段/类型/枚举）——数据与契约一致性可机械验证；
- **启动 mock 服务后，至少一个真实请求能打到并返回预期数据（真跑一次）**——mock 服务不是摆设，真打一次才知道它活着；
- 更新「契约影响 · REST」或接口协议文档，标注接口 mock 状态（`mock` / `real`）——防混淆的关键一步。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 240" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_m3" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_m3"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="240" fill="url(#bg_m3)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">Step 4：三项校验</text>
  <rect x="40" y="50" width="220" height="90" rx="8" fill="#3b82f6" opacity="0.10" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_m3)"/>
  <text x="150" y="74" text-anchor="middle" fill="#93c5fd" font-size="11" font-weight="700">① 契约校验</text>
  <text x="150" y="98" text-anchor="middle" fill="#94a3b8" font-size="9">字段/类型/枚举可机械验证</text>
  <text x="150" y="120" text-anchor="middle" fill="#64748b" font-size="8">一致性有据可查</text>
  <rect x="290" y="50" width="220" height="90" rx="8" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_m3)"/>
  <text x="400" y="74" text-anchor="middle" fill="#86efac" font-size="11" font-weight="700">② 真跑一次</text>
  <text x="400" y="98" text-anchor="middle" fill="#94a3b8" font-size="9">至少一个真实请求打中</text>
  <text x="400" y="120" text-anchor="middle" fill="#64748b" font-size="8">mock 活着不活着，打了才知道</text>
  <rect x="540" y="50" width="220" height="90" rx="8" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_m3)"/>
  <text x="650" y="74" text-anchor="middle" fill="#fde68a" font-size="11" font-weight="700">③ 标注状态</text>
  <text x="650" y="98" text-anchor="middle" fill="#94a3b8" font-size="9">接口标注 mock / real</text>
  <text x="650" y="120" text-anchor="middle" fill="#64748b" font-size="8">防止误以为接口已实现</text>
  <text x="400" y="190" text-anchor="middle" fill="#475569" font-size="11">三项校验 = 契约一致 + 活着 + 不混淆</text>
  <text x="400" y="220" text-anchor="middle" fill="#64748b" font-size="10">"mock 状态标注"是最后一道防呆：文档里分不清，联调必踩坑</text>
</svg>
```

## 四、四条纪律：mock 的可信度底线

### 4.1 忠于契约

**mock 数据不得与接口协议矛盾，否则联调必踩坑。**

契约是真源，mock 是投影。投影歪了，前端按歪的 mock 开发出的代码，联调时全错——mock 从"帮助"变成了"误导"。忠于契约的意思是逐字对照：字段名、类型、枚举值、错误码，一个都不自由发挥。

### 4.2 分支齐全

**不只 mock 成功分支，错误/边界分支也要覆盖（前端才测得到）。**

单一分支的 mock 是最廉价也最危险的——前端只测得到主流程，错误处理全是空白。五分支数据保证了前端的每个状态都有数据可测。

### 4.3 可复现

**随机数据带种子（seed），测试不 flaky。**

mock 数据最大的敌人是随机性：每次请求返回不同数据，测试时而这个 flaky 时而不 flaky，无法定位。固定 seed 让随机数据"可复现"——同一次请求永远返回同一份数据，测试才可靠。

### 4.4 标注状态

**mock 与 real 接口在文档中明确区分，防止误以为接口已实现。**

这是最容易被忽略的一条。接口协议文档里如果 mock 和 real 混在一起，联调的人会以为"接口已实现"，结果连的是 mock——排查半天才发现。标注状态（`mock` / `real`）是文档层面的防混淆，成本极低、收益极大。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 300" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_m4" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_m4"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="300" fill="url(#bg_m4)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">四条纪律：mock 的可信度底线</text>
  <rect x="40" y="50" width="340" height="70" rx="8" fill="#3b82f6" opacity="0.10" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_m4)"/>
  <text x="210" y="72" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">① 忠于契约</text>
  <text x="210" y="98" text-anchor="middle" fill="#64748b" font-size="9">不矛盾，逐字对照，否则联调必踩坑</text>
  <rect x="420" y="50" width="340" height="70" rx="8" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_m4)"/>
  <text x="590" y="72" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">② 分支齐全</text>
  <text x="590" y="98" text-anchor="middle" fill="#64748b" font-size="9">错误/边界也覆盖，前端才测得到</text>
  <rect x="40" y="140" width="340" height="70" rx="8" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_m4)"/>
  <text x="210" y="162" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">③ 可复现</text>
  <text x="210" y="188" text-anchor="middle" fill="#64748b" font-size="9">随机数据带 seed，测试不 flaky</text>
  <rect x="420" y="140" width="340" height="70" rx="8" fill="#ef4444" opacity="0.10" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_m4)"/>
  <text x="590" y="162" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">④ 标注状态</text>
  <text x="590" y="188" text-anchor="middle" fill="#64748b" font-size="9">mock/real 分清楚，防误以为已实现</text>
  <text x="400" y="250" text-anchor="middle" fill="#475569" font-size="11">四条纪律对应四种 mock 常见死法：背约、单分支、随机、混淆</text>
  <text x="400" y="278" text-anchor="middle" fill="#64748b" font-size="10">守住底线，mock 就是帮手；破一条，mock 就变敌人</text>
</svg>
```

## 五、与相邻技能的分工

| 场景 | 归属 |
|------|------|
| API mock 数据生成 | **本技能**（`/harness-api-mock`） |
| 接口协议维护 | `domain-modeling` / wiki |
| E2E/集成测试用 mock | `/harness-e2e` + `harness-api-mock` 数据 |
| 后端编码 | `/coding-skill` |

- **domain-modeling / wiki**：接口协议的真源维护者——mock 忠于的契约来自它；契约变了，mock 数据要跟着重建；
- **harness-e2e**：E2E 的"稳定数据"来源——E2E 脱离真实下游依赖时，消费本技能的 mock 数据；
- **coding-skill**：后端实现——mock 是它的"先行替身"，后端实现完，mock 状态从 `mock` 切到 `real`。

## 六、完成标志

`/harness-api-mock` 的完成标志有三个：

1. **契约接口均已生成 mock 数据**（含成功/空/边界/错误/鉴权分支）——五分支全覆盖，不是抽样；
2. **mock 服务可启动，至少一个真实请求验证通过**——真跑了一次，服务是活的；
3. **`api-mock.md` 已落盘，接口 mock 状态已标注**——映射表和状态标注进了文档，可被后续消费。

三个标志对应：数据齐（五分支）→ 服务活（真跑过）→ 可交接（文档化）。**中间的"真跑一次"最容易跳——mock 数据生成完不验证，等于假设它活着。**

## 七、写在最后

`/harness-api-mock` 的全部设计，浓缩成四句话：

1. **并行开发的钥匙**——契约一旦定好，前端/后端/测试三方各推各的，谁都不等谁。
2. **忠于契约是第一纪律**——mock 是契约的忠实投影，投影歪了，预演变误导。
3. **五分支数据是标准**——成功/空/边界/错误/鉴权，每个分支都是前端的一个页面状态。
4. **活着、可复现、标注清楚**——真跑一次、带 seed、mock/real 分明。

一句话记住它：**/harness-api-mock 是契约固定的工具——它按接口协议生成五分支 mock 数据与服务，让"后端没就绪"不再阻塞前端开发、联调与测试，用忠于契约的假数据换取并行开发的真速度。**