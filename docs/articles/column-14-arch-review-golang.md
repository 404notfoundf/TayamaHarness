# 第十四章 · 定期体检：arch-review-golang 的设计哲学

> 代码库像人体——平时感觉不到问题，直到某天突然跑不动了。
> 架构体检的价值，不在于"体检那天"发现了什么，而在于让每次小改都站在"知道哪里脆弱"的地基上。
>
> —— 项目信条：**架构不是画出来的，是长出来的；但长歪了，需要有人定期把它扶正。**

---

## 一、为什么需要"架构体检"？

### 1.1 代码库的"熵增定律"

任何一个代码库，只要有人在上面持续工作，就会不可避免地走向混乱。这不是开发者的能力问题，而是软件演化的自然规律：

| 熵增信号 | 早期表现 | 晚期表现 |
|---------|---------|---------|
| 依赖方向混乱 | controller 偶尔直接 new service | 循环依赖、包级 import 环 |
| 分层被穿透 | handler 里写 SQL | 业务逻辑散落在 UI 层与工具类 |
| 公共代码膨胀 | utils 慢慢变多 | 7000 行的 utils.go，谁都往里塞 |
| 魔术数字 | 魔法数出现在业务代码 | 常量散落各处，改一处漏三处 |
| 测试渐行渐远 | 只有核心模块有测试 | 新代码无测试，旧的测试被跳过 |

这些现象有一个共同特征：**它们都不是某一个 commit 造成的**。每一个单独的提交看起来都合理——加一个字段、抽一个函数、调一个接口——但累积起来，架构就在无人察觉中腐化了。

### 1.2 为什么需要"定期"而不是"一次性"

一次性清理架构问题，最大的风险是**大重构**。而大重构有三个致命问题：

1. **风险不可控**：改动面越大，回归风险越高，测试覆盖越难保障
2. **业务不等待**：大规模重构往往会被业务优先级打断，最后留下一半的烂摊子
3. **人心会散**：重构周期太长，团队看不到收益，热情消耗殆尽

所以 Harness 的设计哲学是：**架构维护应该是"定期体检"而不是"急救手术"**。

```svg
<svg xmlns="http://www.w3.org/2000/svg" width="780" height="280" viewBox="0 0 780 280">
  <defs>
    <linearGradient id="bg14a" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arr14a" markerWidth="10" markerHeight="10" refX="9" refY="5" orient="auto">
      <path d="M 0 0 L 10 5 L 0 10 z" fill="#64748b"/>
    </marker>
  </defs>
  <rect width="780" height="280" fill="url(#bg14a)"/>
  <text x="390" y="28" text-anchor="middle" fill="#e2e8f0" font-size="16" font-weight="700">架构腐化的时间曲线（复杂度 vs 时间）</text>

  <!-- 坐标轴 -->
  <line x1="60" y1="230" x2="740" y2="230" stroke="#475569" stroke-width="2" marker-end="url(#arr14a)"/>
  <line x1="60" y1="230" x2="60" y2="50" stroke="#475569" stroke-width="2"/>
  <text x="40" y="145" fill="#94a3b8" font-size="12" transform="rotate(-90 40 145)">复杂度</text>
  <text x="750" y="238" fill="#94a3b8" font-size="12">时间</text>

  <!-- 不做体检的曲线（红色，更陡） -->
  <path d="M 80 220 Q 200 200 350 150 Q 500 90 600 60" fill="none" stroke="#f43f5e" stroke-width="2.5" stroke-dasharray="6 3"/>
  <text x="460" y="80" fill="#f43f5e" font-size="12">不做体检的曲线</text>
  <circle cx="350" cy="150" r="4" fill="#f43f5e"/>

  <!-- 做体检的曲线（绿色，平缓） -->
  <path d="M 80 220 Q 200 190 300 170 L 350 160 L 450 140 L 500 135 L 600 125 L 700 120" fill="none" stroke="#34d399" stroke-width="2.5"/>
  <text x="460" y="115" fill="#34d399" font-size="12">做体检的曲线</text>

  <!-- 体检点 1-3 -->
  <circle cx="300" cy="170" r="5" fill="#fbbf24"/>
  <text x="295" y="192" fill="#fbbf24" font-size="10">体检1</text>
  <circle cx="450" cy="140" r="5" fill="#fbbf24"/>
  <text x="445" y="162" fill="#fbbf24" font-size="10">体检2</text>
  <circle cx="600" cy="125" r="5" fill="#fbbf24"/>
  <text x="595" y="147" fill="#fbbf24" font-size="10">体检3</text>

  <text x="390" y="265" text-anchor="middle" fill="#64748b" font-size="11">每次体检像是打了一次"疫苗"，把腐化压回低位</text>
</svg>
```

### 1.3 体检的心理学：为什么人会抗拒

抗拒体检的原因很现实：

- **"我很忙"**：业务需求堆成山，架构体检看起来不产生直接收益
- **"我知道有问题"**：但不知道问题有多大，下意识回避
- **"改了会不会出 bug"**：对变更的恐惧，尤其是没有测试保护的老代码

arch-review 存在的意义，就是**把"体检"变成一件低成本、可重复、有产出物的事情**。它不要求你立刻重构，只要求你"看见"。

---

## 二、arch-review 的定位：不是"重构工具"

### 2.1 在 Harness 六阶段流水线中的位置

```svg
<svg xmlns="http://www.w3.org/2000/svg" width="680" height="240" viewBox="0 0 680 240">
  <defs>
    <linearGradient id="bg14b" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arr14b" markerWidth="10" markerHeight="10" refX="9" refY="5" orient="auto">
      <path d="M 0 0 L 10 5 L 0 10 z" fill="#64748b"/>
    </marker>
  </defs>
  <rect width="680" height="240" fill="url(#bg14b)"/>
  <text x="340" y="28" text-anchor="middle" fill="#e2e8f0" font-size="15" font-weight="700">六阶段流水线中的 arch-review 定位</text>

  <text x="340" y="60" text-anchor="middle" fill="#94a3b8" font-size="11">人类意图</text>
  <line x1="340" y1="65" x2="340" y2="78" stroke="#475569" stroke-width="1.5" marker-end="url(#arr14b)"/>

  <rect x="60" y="82" width="560" height="80" rx="10" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="2"/>
  <text x="340" y="108" text-anchor="middle" fill="#7dd3fc" font-size="14" font-weight="700">六阶段流水线（一个变更的生命周期）</text>
  <text x="340" y="134" text-anchor="middle" fill="#94a3b8" font-size="12">① harnessing → ② coding-skill → ③ unit-test</text>
  <text x="340" y="154" text-anchor="middle" fill="#94a3b8" font-size="12">→ ④ expert-reviewer → ⑤ ci → ⑥ deploy-verify</text>

  <line x1="340" y1="162" x2="340" y2="178" stroke="#475569" stroke-width="1.5" marker-end="url(#arr14b)"/>
  <text x="350" y="192" fill="#64748b" font-size="10">按需调用（不影响流水线主流程）</text>

  <rect x="240" y="198" width="200" height="34" rx="17" fill="#f59e0b" opacity="0.15" stroke="#fbbf24" stroke-width="1.5"/>
  <text x="340" y="220" text-anchor="middle" fill="#fde68a" font-size="13" font-weight="700">arch-review（架构体检）</text>
</svg>
```

arch-review 是**辅助技能**，不在六阶段主链路上。它的调用时机是：

| 时机 | 场景 |
|------|------|
| 迭代开始前 | 新需求涉及老模块，先体检一下该模块 |
| 架构评审前 | 要评审一个跨模块设计，先了解现状 |
| 代码库交接时 | 新成员接手，先看架构全貌 |
| CI 发现架构违规 | 规则被绕过，需要系统性梳理 |
| 季度架构评审 | 固定节奏的架构回顾 |

### 2.2 与 expert-reviewer 的区别

这是新手最容易混淆的一对：

| 维度 | expert-reviewer | arch-review |
|------|----------------|-----------|
| 对象 | 单个变更（diff） | 整个代码库（现状） |
| 频率 | 每次变更必做 | 定期/按需 |
| 产出 | 评审意见（合不合规） | 架构报告（现状 + 改进方案） |
| 时间尺度 | 一个 PR 的时间 | 一个迭代/季度的视角 |
| 侧重点 | 这个改动对不对 | 这个代码库健不健康 |

简单说：**expert-reviewer 体检一个人，arch-review 体检一个家族。**

### 2.3 架构体检的本质是"读代码"

很多团队对架构"体检"的理解是跑一个工具（如 ArchUnit、golangci-lint），出一份报告。但 arch-review 的哲学是：

> **工具只能发现"规则的违反"，发现不了"设计的腐化"。**

比如 `gofmt` 能发现格式问题，但发现不了"这个 controller 里为什么有 800 行业务逻辑"。arch-review 的核心动作是**带着问题去读代码**，而不是跑一遍工具。

---

## 三、体检的五维度模型

arch-review 从五个维度扫描代码库，每个维度回答一个核心问题：

```svg
<svg xmlns="http://www.w3.org/2000/svg" width="960" height="420" viewBox="0 0 960 420">
  <defs>
    <linearGradient id="bg14" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/>
      <stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <linearGradient id="c141" x1="0" y1="0" x2="1" y2="1">
      <stop offset="0%" stop-color="#38bdf8"/><stop offset="100%" stop-color="#818cf8"/>
    </linearGradient>
    <filter id="sh14"><feDropShadow dx="0" dy="2" stdDeviation="4" flood-opacity="0.35"/></filter>
  </defs>
  <rect width="960" height="420" fill="url(#bg14)"/>
  <text x="480" y="36" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">架构体检五维度模型</text>

  <rect x="40" y="80" width="170" height="90" rx="12" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.5" filter="url(#sh14)"/>
  <text x="125" y="110" text-anchor="middle" fill="#7dd3fc" font-size="17" font-weight="700">① 结构</text>
  <text x="125" y="132" text-anchor="middle" fill="#94a3b8" font-size="10">目录/分层/依赖方向</text>
  <text x="125" y="150" text-anchor="middle" fill="#64748b" font-size="9">包结构健康吗？</text>

  <rect x="235" y="80" width="170" height="90" rx="12" fill="#8b5cf6" opacity="0.12" stroke="#a78bfa" stroke-width="1.5" filter="url(#sh14)"/>
  <text x="320" y="110" text-anchor="middle" fill="#c4b5fd" font-size="17" font-weight="700">② 依赖</text>
  <text x="320" y="132" text-anchor="middle" fill="#94a3b8" font-size="10">耦合/循环/上帝依赖</text>
  <text x="320" y="150" text-anchor="middle" fill="#64748b" font-size="9">谁在依赖谁？</text>

  <rect x="430" y="80" width="170" height="90" rx="12" fill="#10b981" opacity="0.12" stroke="#34d399" stroke-width="1.5" filter="url(#sh14)"/>
  <text x="515" y="110" text-anchor="middle" fill="#6ee7b7" font-size="17" font-weight="700">③ 边界</text>
  <text x="515" y="132" text-anchor="middle" fill="#94a3b8" font-size="10">模型/接口/防腐层</text>
  <text x="515" y="150" text-anchor="middle" fill="#64748b" font-size="9">边界清晰吗？</text>

  <rect x="625" y="80" width="170" height="90" rx="12" fill="#f59e0b" opacity="0.12" stroke="#fbbf24" stroke-width="1.5" filter="url(#sh14)"/>
  <text x="710" y="110" text-anchor="middle" fill="#fde68a" font-size="17" font-weight="700">④ 治理</text>
  <text x="710" y="132" text-anchor="middle" fill="#94a3b8" font-size="10">规则/工具/门禁</text>
  <text x="710" y="150" text-anchor="middle" fill="#64748b" font-size="9">规则落地了吗？</text>

  <rect x="820" y="80" width="110" height="90" rx="12" fill="#f43f5e" opacity="0.12" stroke="#fb7185" stroke-width="1.5" filter="url(#sh14)"/>
  <text x="875" y="110" text-anchor="middle" fill="#fda4af" font-size="17" font-weight="700">⑤ 细节</text>
  <text x="875" y="132" text-anchor="middle" fill="#94a3b8" font-size="10">命名/长度/复杂度</text>
  <text x="875" y="150" text-anchor="middle" fill="#64748b" font-size="9">代码卫生？</text>

  <line x1="125" y1="170" x2="125" y2="230" stroke="#334155" stroke-width="2"/>
  <line x1="320" y1="170" x2="320" y2="230" stroke="#334155" stroke-width="2"/>
  <line x1="515" y1="170" x2="515" y2="230" stroke="#334155" stroke-width="2"/>
  <line x1="710" y1="170" x2="710" y2="230" stroke="#334155" stroke-width="2"/>
  <line x1="875" y1="170" x2="875" y2="230" stroke="#334155" stroke-width="2"/>

  <rect x="40" y="230" width="890" height="150" rx="12" fill="#1e293b" opacity="0.6" stroke="#334155" stroke-width="1"/>
  <text x="485" y="258" text-anchor="middle" fill="#e2e8f0" font-size="15" font-weight="700">体检报告（输出物）</text>
  <text x="70" y="290" fill="#94a3b8" font-size="11">🔴 严重问题 —— 立即处理（违反铁律 / 循环依赖 / 架构穿透）</text>
  <text x="70" y="312" fill="#94a3b8" font-size="11">🟡 摩擦点 —— 纳入计划（过度耦合 / 职责模糊 / 重复代码）</text>
  <text x="70" y="334" fill="#94a3b8" font-size="11">🟢 健康项 —— 保持现状（边界清晰 / 规则落地 / 测试覆盖）</text>
  <text x="70" y="360" fill="#64748b" font-size="11">每个问题：位置 + 现象 + 影响 + 改进方案 + 建议优先级 —— 可直接转成 change.md</text>
</svg>
```

### 3.1 维度一：结构（Structure）

**核心问题：代码的组织方式是否支撑当前规模的演进？**

体检动作：
- 目录层级是否与业务域对齐，还是按技术分层堆叠？
- 是否出现"上帝目录"（一个目录里塞了所有东西）？
- 新功能进代码库时，是"找不到应该放哪"还是"很自然地知道放哪"？

在 Go 项目中，经典的结构问题包括：

| 信号 | 具体表现 | 影响 |
|------|---------|------|
| `internal/` 滥用 | 所有包都塞进 internal，外部依赖无法注入 | 无法测试、无法扩展 |
| 包名是 `utils` | `package utils` 收集了 50 个无关函数 | 隐式耦合，改动范围失控 |
| 目录按技术分层 | `handlers/`、`models/`、`services/` 平铺 | 业务域被肢解 |
| 循环目录引用 | `pkg/a` 引用 `pkg/b`，`pkg/b` 引用 `pkg/a` | 编译期能过，架构已碎 |

### 3.2 维度二：依赖（Dependency）

**核心问题：谁依赖谁？这种依赖关系健康吗？**

这是最能体现"架构腐化"的维度。健康的依赖应该满足**依赖倒置原则**：高层模块不依赖低层实现，都依赖抽象。

体检动作：
- 用 `go list -deps` 或类似工具画出依赖图
- 标记"上帝依赖"：被超过 20 个包引用的包
- 查找循环依赖和"中心辐射"式的依赖结构

```svg
<svg xmlns="http://www.w3.org/2000/svg" width="680" height="240" viewBox="0 0 680 240">
  <defs>
    <linearGradient id="bg14c" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arr14c" markerWidth="10" markerHeight="10" refX="9" refY="5" orient="auto">
      <path d="M 0 0 L 10 5 L 0 10 z" fill="#64748b"/>
    </marker>
    <marker id="arr14red" markerWidth="10" markerHeight="10" refX="9" refY="5" orient="auto">
      <path d="M 0 0 L 10 5 L 0 10 z" fill="#f43f5e"/>
    </marker>
  </defs>
  <rect width="680" height="240" fill="url(#bg14c)"/>

  <!-- 左侧：健康 -->
  <text x="170" y="28" text-anchor="middle" fill="#34d399" font-size="14" font-weight="700">健康：单向依赖树</text>
  <rect x="80" y="40" width="120" height="28" rx="6" fill="#1e293b" stroke="#475569"/>
  <text x="140" y="59" text-anchor="middle" fill="#e2e8f0" font-size="12">handler</text>
  <line x1="140" y1="68" x2="140" y2="82" stroke="#475569" stroke-width="1.5" marker-end="url(#arr14c)"/>
  <rect x="80" y="85" width="120" height="28" rx="6" fill="#1e293b" stroke="#475569"/>
  <text x="140" y="104" text-anchor="middle" fill="#e2e8f0" font-size="12">service</text>
  <line x1="140" y1="113" x2="140" y2="127" stroke="#475569" stroke-width="1.5" marker-end="url(#arr14c)"/>
  <rect x="80" y="130" width="120" height="28" rx="6" fill="#1e293b" stroke="#475569"/>
  <text x="140" y="149" text-anchor="middle" fill="#e2e8f0" font-size="12">dao</text>
  <line x1="140" y1="158" x2="140" y2="172" stroke="#475569" stroke-width="1.5" marker-end="url(#arr14c)"/>
  <rect x="80" y="175" width="120" height="28" rx="6" fill="#1e293b" stroke="#475569"/>
  <text x="140" y="194" text-anchor="middle" fill="#e2e8f0" font-size="12">db</text>
  <text x="140" y="222" text-anchor="middle" fill="#64748b" font-size="10">腐败：任何一环出现反向箭头</text>

  <!-- 右侧：环 -->
  <text x="510" y="28" text-anchor="middle" fill="#fbbf24" font-size="14" font-weight="700">⚠️ 有环 → 立即修复</text>
  <rect x="420" y="40" width="120" height="28" rx="6" fill="#1e293b" stroke="#475569"/>
  <text x="480" y="59" text-anchor="middle" fill="#e2e8f0" font-size="12">handler</text>
  <line x1="480" y1="68" x2="480" y2="82" stroke="#475569" stroke-width="1.5" marker-end="url(#arr14c)"/>
  <rect x="420" y="85" width="120" height="28" rx="6" fill="#1e293b" stroke="#475569"/>
  <text x="480" y="104" text-anchor="middle" fill="#e2e8f0" font-size="12">service A</text>
  <rect x="420" y="130" width="120" height="28" rx="6" fill="#1e293b" stroke="#475569"/>
  <text x="480" y="149" text-anchor="middle" fill="#e2e8f0" font-size="12">service B</text>
  <line x1="480" y1="158" x2="480" y2="172" stroke="#475569" stroke-width="1.5" marker-end="url(#arr14c)"/>
  <rect x="420" y="175" width="120" height="28" rx="6" fill="#1e293b" stroke="#475569"/>
  <text x="480" y="194" text-anchor="middle" fill="#e2e8f0" font-size="12">dao</text>

  <!-- 环标注 -->
  <path d="M 540 100 Q 600 100 600 145 Q 600 185 540 175" fill="none" stroke="#f43f5e" stroke-width="2" marker-end="url(#arr14red)"/>
  <text x="610" y="142" fill="#f43f5e" font-size="10">环！</text>
  <line x1="480" y1="113" x2="600" y2="145" stroke="#f43f5e" stroke-width="1.5" stroke-dasharray="4 2" marker-end="url(#arr14red)"/>
  <text x="600" y="110" fill="#f43f5e" font-size="10">反向引用</text>

  <text x="480" y="222" text-anchor="middle" fill="#f43f5e" font-size="10">❌ 环出现 → 立即修复</text>
</svg>
```

### 3.3 维度三：边界（Boundary）

**核心问题：领域模型是否保持纯粹？基础设施是否被隔离在外？**

在 Go 项目中，"边界"通常指：

- **领域模型层**不应该 import 任何基础设施（数据库 driver、HTTP 库、文件系统）
- **接口定义**应该放在消费方，而不是实现方（Go 的惯例：`consumer-side interface`）
- **防腐层（Anti-Corruption Layer）**是否存在：外部系统模型变化时，是否有一条缓冲带？

体检动作：
- 抽查几个核心模型文件，看 import 列表里有没有"不该出现的东西"
- 检查 handler 是否直接建连数据库、service 是否直接操作文件
- 检查错误类型是否泄漏到 API 层（原始 error 直接返回给客户端）

### 3.4 维度四：治理（Governance）

**核心问题：规则写了，落地了吗？**

Harness 体系里有一整套规则文件（编码规范、工程结构、运行时可靠性）。治理维度的体检就是检查：**这些规则是否真正被机械地守护，还是只是文档？**

| 治理层次 | 检查内容 | 工具示例 |
|---------|---------|---------|
| 格式层 | gofmt / goimports 是否强制 | `go vet`、CI 检查 |
| 静态层 | staticcheck / golangci-lint 规则是否启用 | golangci-lint |
| 架构层 | 依赖方向是否被 ArchUnit 守护 | ArchUnit（Java） |
| 流程层 | 六阶段流水线是否被遵守 | 变更卡状态检查 |

没有工具守护的规则，等于没有规则——这是体检报告中优先级最高的治理问题。

### 3.5 维度五：细节（Detail）

**核心问题：每个函数、每个命名是否"卫生"？**

细节维度的体检不是逐行 review，而是采样检查：

- 命名：`GetUser` vs `fetchUserDataFromDB`（动词 + 对象 + 来源，超过 3 段说明职责不清）
- 函数长度：超过 80 行的函数，是否值得提取？
- 圈复杂度：`gocyclo` 结果超过 15 的函数
- 重复代码：`goreporter` 或人工抽查出的重复块

细节维度的问题通常不致命，但它们是**早期熵增的信号**——细节开始腐化时，结构腐化通常也不远了。

---

## 四、体检的流程：从"扫"到"报"

```svg
<svg xmlns="http://www.w3.org/2000/svg" width="960" height="300" viewBox="0 0 960 300">
  <defs>
    <linearGradient id="bg14b" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arr14b" markerWidth="10" markerHeight="10" refX="9" refY="5" orient="auto">
      <path d="M 0 0 L 10 5 L 0 10 z" fill="#64748b"/>
    </marker>
  </defs>
  <rect width="960" height="300" fill="url(#bg14b)"/>
  <text x="480" y="34" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">architecture review 四步流程</text>

  <rect x="30" y="90" width="190" height="130" rx="12" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.5"/>
  <text x="125" y="118" text-anchor="middle" fill="#7dd3fc" font-size="16" font-weight="700">Step 1 · 扫描</text>
  <text x="125" y="142" text-anchor="middle" fill="#94a3b8" font-size="11">遍历代码库结构</text>
  <text x="125" y="162" text-anchor="middle" fill="#94a3b8" font-size="11">读关键入口与核心包</text>
  <text x="125" y="182" text-anchor="middle" fill="#94a3b8" font-size="11">收集编译/测试结果</text>
  <text x="125" y="202" text-anchor="middle" fill="#64748b" font-size="10">输入：repository</text>

  <line x1="220" y1="155" x2="258" y2="155" stroke="#475569" stroke-width="2" marker-end="url(#arr14b)"/>

  <rect x="268" y="90" width="190" height="130" rx="12" fill="#8b5cf6" opacity="0.12" stroke="#a78bfa" stroke-width="1.5"/>
  <text x="363" y="118" text-anchor="middle" fill="#c4b5fd" font-size="16" font-weight="700">Step 2 · 诊断</text>
  <text x="363" y="142" text-anchor="middle" fill="#94a3b8" font-size="11">对照五维度模型</text>
  <text x="363" y="162" text-anchor="middle" fill="#94a3b8" font-size="11">标记摩擦点与违规</text>
  <text x="363" y="182" text-anchor="middle" fill="#94a3b8" font-size="11">评估影响与优先级</text>
  <text x="363" y="202" text-anchor="middle" fill="#64748b" font-size="10">核心：读代码 + 判断</text>

  <line x1="458" y1="155" x2="496" y2="155" stroke="#475569" stroke-width="2" marker-end="url(#arr14b)"/>

  <rect x="506" y="90" width="190" height="130" rx="12" fill="#10b981" opacity="0.12" stroke="#34d399" stroke-width="1.5"/>
  <text x="601" y="118" text-anchor="middle" fill="#6ee7b7" font-size="16" font-weight="700">Step 3 · 报告</text>
  <text x="601" y="142" text-anchor="middle" fill="#94a3b8" font-size="11">输出体检报告</text>
  <text x="601" y="162" text-anchor="middle" fill="#94a3b8" font-size="11">问题分级（🔴🟡🟢）</text>
  <text x="601" y="182" text-anchor="middle" fill="#94a3b8" font-size="11">给改进方案与优先级</text>
  <text x="601" y="202" text-anchor="middle" fill="#64748b" font-size="10">输出：arch report</text>

  <line x1="696" y1="155" x2="734" y2="155" stroke="#475569" stroke-width="2" marker-end="url(#arr14b)"/>

  <rect x="744" y="90" width="190" height="130" rx="12" fill="#f59e0b" opacity="0.12" stroke="#fbbf24" stroke-width="1.5"/>
  <text x="839" y="118" text-anchor="middle" fill="#fde68a" font-size="16" font-weight="700">Step 4 · 跟进</text>
  <text x="839" y="142" text-anchor="middle" fill="#94a3b8" font-size="11">问题转 change.md</text>
  <text x="839" y="162" text-anchor="middle" fill="#94a3b8" font-size="11">纳入迭代计划</text>
  <text x="839" y="182" text-anchor="middle" fill="#94a3b8" font-size="11">下次体检复查闭环</text>
  <text x="839" y="202" text-anchor="middle" fill="#64748b" font-size="10">输出：change cards</text>

  <text x="480" y="272" text-anchor="middle" fill="#64748b" font-size="11">体检报告的终点不是报告本身，而是"转成变更卡"——否则体检就是一场自嗨</text>
</svg>
```

### 4.1 Step 1 · 扫描：先摸清地形

扫描阶段的原则是**广覆盖、浅深入**：

1. 遍历目录树，标注每个目录的职责
2. 读取每个包的 `doc.go`（如果有）或包注释
3. 找到"入口文件"（main.go、server 注册处、路由表）
4. 记录 import 关系——不用完整依赖图，抓住主干即可

扫描的**禁忌**是试图读完每一行代码。arch-review 的时间预算里，**扫描只占 20%**，其余 80% 用于"带着疑问深入"。

### 4.2 Step 2 · 诊断：带着五维度问题深挖

诊断阶段，对每个可疑点问三个问题：

1. **这是什么问题？**（现象描述，不带评价）
2. **它影响了谁？**（哪个模块、哪个团队、哪个变更经常被它拖累）
3. **它为什么会存在？**（是历史遗留、是没人管、还是设计时就没想清楚）

这个"为什么"很重要——**多数架构问题不是"错误"，而是"权衡的遗留"**。诊断时要区分：

| 类型 | 特征 | 处理 |
|------|------|------|
| 决策失误 | 当时就没想清楚 | 重新设计 |
| 权衡遗留 | 当时为了赶进度/兼容做了取舍 | 记录上下文，等条件成熟再还债 |
| 熵增积累 | 无人在意，日积月累 | 制定清理计划 |
| 规则缺失 | 没有规则或规则没落地 | 补规则 + 补工具守护 |

### 4.3 Step 3 · 报告：输出的不是"批评"是"地图"

体检报告的措辞极其重要。如果报告充满"这里写得不好"的批评，团队会用防御心态对待它；如果报告是一张"现状地图 + 改进路线图"，团队会感激它。

报告结构：

```markdown
# 架构体检报告 — <项目名>（<日期>）

## 总览
- 健康评分：72/100（结构 78 · 依赖 65 · 边界 70 · 治理 80 · 细节 68）
- 🔴 严重问题：2 个 ｜ 🟡 摩擦点：9 个 ｜ 🟢 健康项：14 个

## 🔴 严重问题（建议 1 个迭代内处理）
### 1. <位置> — <问题一句话>
- 现象：...
- 影响：...
- 改进方案：...
- 建议变更：change-<id> 的雏形

## 🟡 摩擦点（建议纳入季度计划）
...

## 🟢 健康项（保持现状）
...

## 下轮重点关注
- 验收：上面 🟡 中有几个在下次体检时降为 🟢？
```

### 4.4 Step 4 · 跟进：从报告到变更卡

体检报告的价值，最后要靠"跟进"兑现。**每个 🔴 问题都应当产出一张 change.md**，具备：

- 清晰的 AC（怎么算修复了）
- 优先级（阻塞 / 高 / 中 / 低）
- 责任人（owner）
- 时间窗口（哪个迭代）

否则体检只是"看了一次病，没开药"。

---

## 五、与 Harness 规则的联动

### 5.1 体检是规则的"体检"

Harness 体系里，规则（rules/）是静态的、机械的。但规则本身也会过时：

- 规则 A 已经不适用于新架构，但还在 CI 里跑
- 规则 B 是拍脑袋写的，从来没人验证过
- 规则 C 覆盖了 90% 的场景，但缺了关键 10%

arch-review 的治理维度，本质上是对规则的反省：**规则还在守护正确的东西吗？**

```svg
<svg xmlns="http://www.w3.org/2000/svg" width="620" height="180" viewBox="0 0 620 180">
  <defs>
    <linearGradient id="bg14d" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arr14d" markerWidth="10" markerHeight="10" refX="9" refY="5" orient="auto">
      <path d="M 0 0 L 10 5 L 0 10 z" fill="#64748b"/>
    </marker>
  </defs>
  <rect width="620" height="180" fill="url(#bg14d)"/>
  <text x="155" y="28" text-anchor="middle" fill="#94a3b8" font-size="13">规则文件（静态）</text>
  <text x="435" y="28" text-anchor="middle" fill="#94a3b8" font-size="13">体检（动态）</text>

  <rect x="50" y="42" width="210" height="110" rx="10" fill="#1e293b" stroke="#475569"/>
  <text x="155" y="68" text-anchor="middle" fill="#e2e8f0" font-size="12">编码规范.md</text>
  <text x="155" y="92" text-anchor="middle" fill="#e2e8f0" font-size="12">工程结构.md</text>
  <text x="155" y="116" text-anchor="middle" fill="#e2e8f0" font-size="12">可靠性规范.md</text>
  <text x="155" y="140" text-anchor="middle" fill="#64748b" font-size="10">一致性校验</text>

  <line x1="260" y1="80" x2="340" y2="80" stroke="#475569" stroke-width="2" marker-end="url(#arr14d)"/>
  <text x="300" y="72" text-anchor="middle" fill="#64748b" font-size="10">校验</text>

  <rect x="350" y="42" width="220" height="110" rx="10" fill="#1e293b" stroke="#475569"/>
  <text x="460" y="68" text-anchor="middle" fill="#e2e8f0" font-size="12">实际代码是否遵守？</text>
  <text x="460" y="92" text-anchor="middle" fill="#e2e8f0" font-size="12">规则是否过时？</text>
  <text x="460" y="116" text-anchor="middle" fill="#e2e8f0" font-size="12">规则是否有工具守护？</text>
  <text x="460" y="140" text-anchor="middle" fill="#64748b" font-size="10">缺口分析</text>

  <path d="M 460 150 Q 460 170 310 170 Q 160 170 160 150" fill="none" stroke="#fbbf24" stroke-width="1.5" stroke-dasharray="4 2" marker-end="url(#arr14d)"/>
  <text x="310" y="166" text-anchor="middle" fill="#fbbf24" font-size="10">修订规则</text>
</svg>
```

### 5.2 体检发现的"规则缺口"怎么补

发现规则缺口后，标准动作是：

1. **记入体检报告**的治理维度
2. **起草规则修订**（增量，不推翻）
3. **加上工具守护**：能机械化的必须机械化（ArchUnit、golangci-lint 配置等）
4. **走一次变更流程**：改规则也是变更，要过审查

### 5.3 体检与 CONTEXT.md 的联动

体检过程中敲定的术语、发现的领域概念，应当**当场写入 `.harness/CONTEXT.md`**——这符合项目"术语被敲定后当场写"的原则。例如体检发现"gateway 其实不是网关，而是聚合层"，就应该把这个认知固化下来。

---

## 六、Go 项目体检实战清单

以下是一份 arch-review 在 Go 项目中实际执行的检查清单模板：

### 6.1 结构维度
- [ ] 目录是否按业务域划分？`internal/` 下是否有清晰模块边界？
- [ ] 是否有 `utils`、`common`、`helper` 这类"垃圾桶包"？
- [ ] 包名是否简洁（`package user` 而非 `package user_service_impl`）？
- [ ] 导出符号是否克制（`Exported` vs `exported`）？

### 6.2 依赖维度
- [ ] 是否存在包循环依赖？（`go list -deps` 检查）
- [ ] 是否有包被过多依赖（"上帝包"）？
- [ ] 接口定义在消费方还是在实现方？
- [ ] 依赖注入是否清晰？还是用全局变量共享状态？

### 6.3 边界维度
- [ ] 领域包是否 import 了 DB driver / HTTP 库？
- [ ] 外部 API 的错误是否被转换为领域错误？
- [ ] 是否定义了防腐层挡住第三方 SDK 变更？
- [ ] 配置是显式注入还是包内读全局？

### 6.4 治理维度
- [ ] 每一条规则是否有对应的工具守护？
- [ ] CI 门禁是否真的执行（而不是手动执行）？
- [ ] 规则文件是否与代码同库版本化？
- [ ] 规则有"违反豁免"流程吗（避免规则被绕过就作废）？

### 6.5 细节维度
- [ ] 是否有长函数（>80 行）？
- [ ] 是否有高圈复杂度函数（>15）？
- [ ] 是否有明显的重复代码块？
- [ ] 命名是否表意？`GetXxx` 是否真的只做获取？

---

## 七、体检报告到变更：一个完整例子

假设体检发现：`internal/service/billing.go` 有 1200 行，其中包含支付逻辑、对账逻辑、通知逻辑，被 6 个模块引用。

| 维度 | 内容 |
|------|------|
| **现象** | billing.go 是"上帝服务"，业务逻辑堆叠，import 了 handler 层的东西 |
| **影响** | 每次改账单需求，都可能影响支付；测试难写；新人不敢碰 |
| **方案** | 拆成 billing（核心）、payment-adapter（支付适配）、reconciliation（对账）、notification（通知）四个包；handler 只依赖接口 |
| **优先级** | 🟡 高（不阻塞，但持续拖累） |
| **变更卡** | 创建 C-042，含 AC-1 四个包独立编译；AC-2 handler 只 import 接口；AC-3 拆包后测试全绿；AC-4 billing.go 行数 < 200 |

这张变更卡进入六阶段流水线，走完 coding → test → review → ci → verify，架构体检发现问题 → 变更流程解决问题 → 下次体检验收闭环。

---

## 八、体检的节奏：不是"越多越好"

最后的哲学提醒：**体检的节奏要匹配团队的消化能力。**

```
  体检频率       适合场景                风险
  ──────────    ──────────────────      ──────────────
  每次迭代       团队成熟、工具化程度高    消耗注意力
  每季度         主流节奏                问题累积一个季度
  每年一次       边缘团队                往往已经太晚
```

判断标准只有一个：**每轮体检的 🔴 问题是否都能在下一轮前清零？** 如果做不到，就放慢节奏；如果做得到，可以考虑加快。

> 体检不是为了"发现更多问题"，而是为了"让问题在变得致命之前被发现"。

---

## 九、信条回顾

- **架构是长出来的**：承认熵增是常态，不追求"一次性完美"
- **体检是低成本的看见**：让腐化在早期被发现，而不是晚期被急救
- **工具发现规则违反，人发现设计腐化**：读代码是体检的核心动作
- **报告是地图不是批评**：措辞决定团队是防御还是感激
- **报告终点是变更卡**：没有跟进的体检是自嗨
- **规则也要被体检**：静态规则 + 动态反省 = 健康治理

下一篇，我们聊聊 `handoff` —— 当对话上下文快满时，如何把整个工作状态"序列化"成一份交接文档，在另一个对话里无缝续接。