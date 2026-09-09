# Red→Green→Refactor：/coding-skill 的 AI 编码小循环

> 命令深度拆解 · 第二篇 · 约 10000 字 · 10 个 SVG 图

## 一、/coding-skill 不是什么

在 Harness 的流水线中，/coding-skill 是"第二个命令"——在 /harnessing 完成需求分析后，进入编码阶段。但很多人对 coding-skill 有误解：

**它不是"写代码的 AI 工具"。** 市面上有很多 AI 写代码的工具——GitHub Copilot、Cursor、Windsurf——它们的特点是"你说一句，AI 写一段"。但 coding-skill 不是这样工作的。

**它不是"自动编程的按钮"。** 用户输入"帮我写一个订单系统"，coding-skill 不会直接输出 1000 行代码。它把需求分解成一个个小的"垂直切片"，每个切片只做一件事，但做完整。

**它不是"替代程序员的机器"。** coding-skill 的目标不是让程序员失业，而是让程序员的"编码模式"更加规范——从"凭感觉写代码"变成"按 TDD 小循环写代码"。

### 1.1 从 tayama-trip-plan 的"复制粘贴困局"说起

tayama-trip-plan 是 Harness 的"前身"。在 tayama-trip-plan 中，/coding-skill 的雏形是一个"手动执行"的过程：用户用 Claude Code 写代码，但每次都要手动加载需求卡片、手动检查编码规范、手动执行 TDD 循环。

这个"手动"过程的问题在于：

1. **一致性差**：同一个用户，今天写的代码和明天写的代码，风格可能完全不同
2. **遗漏多**：需求卡片中的验收标准，经常被"忘记"——写完了代码才发现"哦，还有这个 AC 没实现"
3. **回退难**：写了一半发现需求理解错了，前面 100 行代码全白写

coding-skill 的诞生，就是要把这个"手动"过程变成"自动"——让 AI 自动读取需求卡片、自动执行 TDD 循环、自动检查编码规范。

### 1.2 /coding-skill 的"输入-输出"契约

/coding-skill 的输入输出契约非常清晰：

- **输入**：`.harness/changes/<id>/change.md`（需求卡片，状态为 coding）
- **输出**：可编译的实现代码 + 更新后的 change.md（状态变为 testing）
- **约束**：不修改需求卡片的内容、不实现需求外的功能、不引入未评审的依赖

这个契约定义了 coding-skill 的"职责边界"——它不是"万能写代码工具"，而是"严格按需求规格执行的编码工具"。

<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 200" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_cs1" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_cs1"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="200" fill="url(#bg_cs1)" rx="10"/>
  <text x="400" y="25" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">/coding-skill 的输入-输出契约</text>
  <rect x="40" y="50" width="220" height="55" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_cs1)"/>
  <text x="150" y="72" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">输入</text>
  <text x="150" y="90" text-anchor="middle" fill="#94a3b8" font-size="10">change.md 需求卡片</text>
  <line x1="260" y1="77" x2="290" y2="77" stroke="#475569" stroke-width="2"/>
  <polygon points="295,77 287,72 287,82" fill="#94a3b8"/>
  <rect x="300" y="50" width="220" height="55" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_cs1)"/>
  <text x="410" y="72" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">处理</text>
  <text x="410" y="90" text-anchor="middle" fill="#94a3b8" font-size="10">TDD 小循环+编码规范</text>
  <line x1="520" y1="77" x2="550" y2="77" stroke="#475569" stroke-width="2"/>
  <polygon points="555,77 547,72 547,82" fill="#94a3b8"/>
  <rect x="560" y="50" width="200" height="55" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_cs1)"/>
  <text x="660" y="72" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">输出</text>
  <text x="660" y="90" text-anchor="middle" fill="#94a3b8" font-size="10">可编译的实现代码</text>
  <text x="400" y="160" text-anchor="middle" fill="#475569" font-size="11">不越界：只实现需求卡片中的内容，不引入额外功能</text>
  <text x="400" y="180" text-anchor="middle" fill="#64748b" font-size="10">状态变更：需求卡片从 coding → testing</text>
</svg>



### 1.3 coding-skill 的"进化"：从"手动"到"自动"

tayama-trip-plan 的 coding-skill 是"手动"的。用户需要自己：加载需求卡片、检查编码规范、执行 TDD 循环。这个过程依赖用户"记得"这些步骤。

而 Harness 的 coding-skill 是"自动"的。用户只需要输入 `/coding-skill`，AI 就会自动：读取需求卡片、加载编码规范、执行 TDD 循环。

这个"从手动到自动"的进化，是 coding-skill 的核心价值：**它把"用户记得做"变成了"AI 自动做"。**

### 1.4 coding-skill 的"边界"：它不做什么

coding-skill 的"边界"很清晰，它不做什么：

- **不写需求**：需求分析是 /harnessing 的工作
- **不写非核心测试**：测试补全是 /unit-test-write 的工作
- **不做代码审查**：代码审查是 /expert-reviewer 的工作
- **不做部署验证**：部署验证是 /deploy-verify 的工作

这个"边界"保证了 coding-skill 的"专注"——它只做一件事，但把这件事做到极致。


### 1.5 coding-skill 的"定位"：流水线的"生产者"

在 Harness 流水线中，coding-skill 扮演"生产者"的角色——它把需求卡片"生产"成代码。但这个"生产"不是"一次性的"，而是"增量式的"——每次调用只生产一个功能点。

这个"增量式生产"的设计，保证了流水线的"稳定性"——每个功能点都经过测试、检查、规范验证，才会进入下一阶段。

### 1.6 coding-skill 的"门槛"：入口检查

coding-skill 不是"随便就能调用"的，它有严格的入口检查：

```text
入口检查清单：
1. change.md 是否存在？
2. change.md 状态是否为 coding？
3. 编码规范是否已加载？
4. 工程结构是否已确认？

如果任一未通过 → 退回 /harnessing
```

这个入口检查，保证了 coding-skill 的"输入质量"——只有高质量的需求，才能进入高质量的编码。

### 1.7 coding-skill 的"不替代"原则

coding-skill 有一个核心原则：**不替代人的判断**。

当 AI 遇到"不确定"的情况时，coding-skill 不会"替人做决定"，而是"请人做决定"。这个"不替代"原则，保证了"人始终在决策链中"——AI 是工具，不是决策者。

这个原则的设计背景是：在 tayama-trip-plan 的实践中，发现 AI "自作主张"是最大的问题来源。AI 以为"这个参数应该这样设"，但实际需求是"那样设"。所以 coding-skill 的"不替代"原则，就是从实践中总结出来的"血的教训"。

### 1.8 从"编码工具"到"工程伙伴"的进化

coding-skill 代表了 AI 编码工具的一个"进化方向"：从"编码工具"到"工程伙伴"。

"编码工具"只负责"写代码"，而"工程伙伴"负责"管代码"——管需求、管质量、管规范、管流程。这个"进化"的核心，是让 AI 从"被动执行"变成"主动管理"。

这个"主动管理"的能力，是 coding-skill 与其他 AI 编码工具的核心区别——其他工具是"你让它写，它写"；coding-skill 是"它知道该写什么、怎么写、写完了还需要做什么"。

## 二、Red→Green→Refactor：AI 编码的小循环

### 2.1 TDD 的核心思想：先写失败测试

TDD（Test-Driven Development）的核心思想是：**在写实现代码之前，先写测试代码。** 这个测试是"失败的"——因为还没有实现代码。然后写最少量的实现代码让这个测试通过。最后，在测试的保护下重构代码。

这个"Red→Green→Refactor"循环，是 TDD 的"心跳"——每循环一次，就完成一个功能点。

但 AI 做 TDD 有一个天然的优势：**AI 不需要"编译等待"——它可以在几秒内完成 Red→Green→Refactor 的完整循环。**

### 2.2 为什么 AI 需要"一次只做一个"循环

人类程序员做 TDD 时，最大的挑战是"耐心"——写一个测试、跑一下、发现失败、写点代码、再跑一下、通过……这个过程很慢，所以人类程序员倾向于"批量做"——一下写 5 个测试，然后一下写 5 个实现。

但 AI 不一样。AI 做 TDD 时，最大的挑战是"精确性"——如果 AI 批量写 5 个测试，这 5 个测试可能基于"同一个假设"，而这个假设可能是错的。一旦发现假设错了，5 个测试全部要重写。

所以 coding-skill 的"一次只做一个"循环，不是"速度"问题，而是"质量"问题——每个测试都是一颗"示踪弹"，它在告诉你上一个决策的对错，然后引导你到下一个决策。

### 2.3 垂直切片原则：为什么"不批量"

垂直切片（Vertical Slice）是 coding-skill 的核心原则。它的意思是：**一次只做一个功能点，从界面到数据层的完整实现**，而不是"先写完所有界面，再写完所有逻辑，再写完所有数据层"。

为什么"不批量"？原因有三：

1. **反馈延迟**：批量写代码，意味着你要等到"所有代码写完"才能得到反馈。如果第一块代码就写错了，后面所有代码都是白写
2. **假设累积**：批量写代码时，你的"假设"会累积——第一块代码的假设、第二块代码的假设……一旦发现第一个假设错了，后面所有假设都要推翻
3. **重构困难**：批量写代码，代码耦合度高，重构成本大

垂直切片解决了这三个问题：每次只做一个小循环，立即得到反馈，假设不累积，重构成本低。

### 2.4 从"示踪弹"到"引导下一个决策"

每个测试都是一颗"示踪弹"——它在告诉你上一个决策的对错，然后引导你到下一个决策。

比如，你写了一个测试"should_create_order"，然后写了一个最小实现让它通过。这个通过的测试告诉你：订单创建的逻辑是对的，你可以继续下一个功能点了。

但如果测试失败了，失败的测试告诉你：订单创建的逻辑有问题，你需要回到"设计"阶段，重新思考"订单创建"的 API 设计。

这个"示踪弹"机制，让 coding-skill 的"编码"过程变成了"逐步逼近"的过程——每次循环都更接近最终的正确实现。

<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 260" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_cs2" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_cs2"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="260" fill="url(#bg_cs2)" rx="10"/>
  <text x="400" y="25" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">Red→Green→Refactor 循环</text>
  <rect x="60" y="55" width="200" height="55" rx="8" fill="#ef4444" opacity="0.15" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_cs2)"/>
  <text x="160" y="77" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">Red（写失败测试）</text>
  <text x="160" y="95" text-anchor="middle" fill="#94a3b8" font-size="10">先写一个测试，应该是红的</text>
  <line x1="260" y1="82" x2="300" y2="82" stroke="#475569" stroke-width="2"/>
  <polygon points="305,82 297,77 297,87" fill="#94a3b8"/>
  <rect x="310" y="55" width="200" height="55" rx="8" fill="#22c55e" opacity="0.15" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_cs2)"/>
  <text x="410" y="77" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">Green（最小实现）</text>
  <text x="410" y="95" text-anchor="middle" fill="#94a3b8" font-size="10">写最少代码让测试通过</text>
  <line x1="510" y1="82" x2="550" y2="82" stroke="#475569" stroke-width="2"/>
  <polygon points="555,82 547,77 547,87" fill="#94a3b8"/>
  <rect x="560" y="55" width="200" height="55" rx="8" fill="#3b82f6" opacity="0.15" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_cs2)"/>
  <text x="660" y="77" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">Refactor（重构）</text>
  <text x="660" y="95" text-anchor="middle" fill="#94a3b8" font-size="10">测试保护下安全重构</text>
  <line x1="660" y1="110" x2="660" y2="140" stroke="#475569" stroke-width="1.5"/>
  <polygon points="660,145 655,138 665,138" fill="#94a3b8"/>
  <rect x="310" y="150" width="200" height="40" rx="6" fill="#64748b" opacity="0.15" stroke="#64748b" stroke-width="1"/>
  <text x="410" y="174" text-anchor="middle" fill="#94a3b8" font-size="11">下一个AC→下一个Red</text>
  <text x="400" y="220" text-anchor="middle" fill="#475569" font-size="11">垂直切片：一次只做一个功能点，不批量</text>
  <text x="400" y="242" text-anchor="middle" fill="#64748b" font-size="10">每个测试是示踪弹，引导下一个决策</text>
</svg>



### 2.5 TDD 的"心理安全"价值

TDD 还有一个隐藏价值：**心理安全**。

当程序员知道自己写的代码有测试保护时，他们更愿意重构——因为测试会告诉他们"改错了"。而没有测试保护时，程序员不敢重构——因为不知道"改了什么"。

这个"心理安全"的价值，在 AI 编码中更加重要。AI 没有"心理"——但 AI 的"质量保证"来自于测试。如果 AI 写的代码没有测试，用户不敢信任 AI 的代码。如果 AI 写的代码有测试保护，用户敢信任。

所以，TDD 不仅是"质量工具"，也是"信任工具"——它让用户信任 AI 的代码。

### 2.6 TDD 与"测试驱动设计"

TDD 还有一个"副作用"：它强制了"好的 API 设计"。

因为先写测试，所以 API 必须"可测试"——方法签名必须清晰、参数必须明确、返回值必须可验证。这个"可测试性"的要求，天然地保证了 API 的"清晰性"——如果 API 不清晰，测试写不出来。

这个"测试驱动设计"的价值，是 TDD 的"隐藏红利"——它不只是"测试"，更是"设计"。


### 2.7 从"基础 TDD"到"AI TDD"的进化

传统 TDD 和 AI TDD 有一个关键区别：

```text
传统 TDD：
写测试（5分钟）→ 跑测试（30秒）→ 写实现（5分钟）→ 跑测试（30秒）→ 重构（3分钟）

AI TDD：
写测试（1秒）→ 写实现（1秒）→ 自检（1秒）→ 下一个循环
```

AI TDD 的"编译时间"是零——AI 不需要等待编译，可以在几秒内完成一个完整循环。这个"零编译时间"的优势，让 AI TDD 的"节奏"比人类 TDD 快 10 倍。

但快 10 倍不等于好 10 倍。AI TDD 的挑战在于"质量"——AI 生成的测试可能"看起来对"但"实际上不对"。所以 coding-skill 的"实时自检"机制就是在弥补这个"质量"挑战。

### 2.8 TDD 的"示踪弹"：从测试到实现的逐层逼近

每个测试都是一颗"示踪弹"，它在告诉你：

1. 第一颗弹（第一个测试）：API 设计是否正确？
2. 第二颗弹（第二个测试）：边界条件是否覆盖？
3. 第三颗弹（第三个测试）：业务逻辑是否完整？

如果第一颗弹偏了（API 设计错了），后面两颗弹也会偏。但好在"一次只做一颗"——发现第一颗弹偏了，只需要调整第一颗，不需要重写全部。

这个"逐层逼近"的机制，是 coding-skill 的"精确性"来源——它让 AI 的编码过程从"猜"变成了"验证"。




### 2.9 TDD 的"投资回报"计算

有些人会说：TDD 太慢了——先写测试再写实现，比直接写实现多花一倍时间。

这其实是一个"投资回报"的问题。TDD 的"投资"是"写测试的时间"，"回报"是"减少返工的时间"。在 AI 编码中，这个"投资回报"更加明显：

- 直接写实现：10 分钟写完，但可能返工 3 次（每次 5 分钟），总时间 25 分钟
- TDD 先写测试：5 分钟写测试 + 5 分钟写实现 = 10 分钟，返工 0 次，总时间 10 分钟

TDD 的"投资回报率"是正的——投入的时间，被返工的减少抵消了。
## 三、/coding-skill 的工作流程

### 3.1 前置检查：确认需求已经就绪

/coding-skill 的"入口门禁"是：change.md 存在且状态为 coding。如果 change.md 不存在，或者状态不是 coding，说明需求分析还没有完成，coding-skill 会"退回"到 /harnessing 阶段。

这个前置检查保证了"需求先于编码"——不会出现"还没想清楚就写代码"的情况。

### 3.2 构造实现上下文

在开始编码之前，coding-skill 会构造一个完整的"实现上下文"：

1. 需求摘要 + 验收标准 + 边界情况 + 设计约束
2. 契约影响分析（当前变更会影响到哪些模块、哪些接口）
3. 测试策略（核心业务逻辑用 TDD，非核心逻辑用后补测试）
4. 涉及文件列表（哪些文件需要修改、哪些文件需要新建）
5. 相关规则约束（从 `.harness/rules/` 加载编码规范、工程结构等）

这个上下文是 coding-skill 的"思维框架"——它不是一个"从零开始"的 AI，而是一个"带着完整上下文"的 AI。

### 3.3 确认实现位置

在"写代码"之前，coding-skill 会先确认"在哪里写代码"：

- 对照 `.harness/rules/工程结构.md` 确定模块、包、类的归属
- 优先复用已有公共模块（避免重复造轮子）
- 判断是"新建文件"还是"修改已有文件"

这个步骤看似简单，但实际价值很大——它避免了"文件放错位置"的问题。

### 3.4 TDD 小循环的详细执行

这是 coding-skill 的"核心步骤"。它的执行过程是：

1. 从 change.md 提取第一个 AC（验收标准）
2. 写一个失败测试（Red）——只写一个，不批量
3. 写最小实现让这个测试通过（Green）——只写刚好的代码，不多写
4. 在测试保护下重构（Refactor）
5. 小步编译通过，确认代码可运行
6. 提取下一个 AC，重复 1-5

关键原则是"只写一个测试"——不是"写一个测试类"，而是"写一个测试方法"。一个测试方法对应一个 AC，一个 Red→Green→Refactor 循环解决一个 AC。

### 3.5 编码顺序：从外到内

TDD 小循环是"内层"循环，coding-skill 还有一个"外层"编码顺序：

1. **数据模型**（如有）：先定义数据结构，再写业务逻辑
2. **接口/抽象**：先定义接口契约，再写实现
3. **失败测试先行**：核心业务逻辑用 TDD
4. **最小实现**：写刚好让测试通过的代码
5. **配置/装配**：最后写配置代码和装配代码

### 3.6 实时自检与编码规范

在每个 TDD 循环结束后，coding-skill 会执行"实时自检"：

- 命名是否符合规范
- 是否有硬编码的密钥、魔法值
- 方法是否超过 50 行、文件是否超过 500 行、圈复杂度是否超过 10
- LLM/外部调用是否有超时+重试+限频+降级
- 异常处理是否合规

<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 280" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_cs3" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_cs3"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="280" fill="url(#bg_cs3)" rx="10"/>
  <text x="400" y="25" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">/coding-skill 的完整工作流程</text>
  <rect x="40" y="50" width="720" height="30" rx="6" fill="#3b82f6" opacity="0.15" stroke="#3b82f6" stroke-width="1.5"/>
  <text x="400" y="69" text-anchor="middle" fill="#93c5fd" font-size="11" font-weight="700">Step 1：前置检查（change.md 状态为 coding）</text>
  <line x1="400" y1="80" x2="400" y2="95" stroke="#475569" stroke-width="1.5"/>
  <polygon points="400,100 397,93 403,93" fill="#94a3b8"/>
  <rect x="40" y="105" width="720" height="30" rx="6" fill="#22c55e" opacity="0.15" stroke="#22c55e" stroke-width="1.5"/>
  <text x="400" y="124" text-anchor="middle" fill="#86efac" font-size="11" font-weight="700">Step 2：构造实现上下文（需求+契约+约束+文件）</text>
  <line x1="400" y1="135" x2="400" y2="150" stroke="#475569" stroke-width="1.5"/>
  <polygon points="400,155 397,148 403,148" fill="#94a3b8"/>
  <rect x="40" y="160" width="720" height="30" rx="6" fill="#f59e0b" opacity="0.15" stroke="#f59e0b" stroke-width="1.5"/>
  <text x="400" y="179" text-anchor="middle" fill="#fde68a" font-size="11" font-weight="700">Step 3：TDD 小循环（Red→Green→Refactor 逐个 AC）</text>
  <line x1="400" y1="190" x2="400" y2="205" stroke="#475569" stroke-width="1.5"/>
  <polygon points="400,210 397,203 403,203" fill="#94a3b8"/>
  <rect x="40" y="215" width="720" height="30" rx="6" fill="#a855f7" opacity="0.15" stroke="#a855f7" stroke-width="1.5"/>
  <text x="400" y="234" text-anchor="middle" fill="#d8b4fe" font-size="11" font-weight="700">Step 4：实时自检+编码规范检查+状态更新</text>
  <text x="400" y="265" text-anchor="middle" fill="#475569" font-size="11">每个子步骤可编译，为后续一 task 一 commit 做准备</text>
</svg>


### 3.7 前置检查的"快速失败"设计

前置检查是 coding-skill 的"第一道防线"——如果需求没有就绪，它不会浪费时间写代码，而是"快速失败"。

这个"快速失败"的设计，确保了资源的有效利用：与其花 10 分钟写代码后发现需求不明确，不如花 10 秒检查需求不明确然后停止。

### 3.8 实时自检的"自动化"价值

实时自检是 coding-skill 的"第二道防线"——在每个 TDD 循环结束后，自动检查代码质量。

这个"自动化"的价值在于：它把"代码审查"从"事后"变成了"事中"——在代码写出来的那一刻，就检查它的质量，而不是等代码写完了再检查。


### 3.9 "确认实现位置"的复用原则

coding-skill 的"确认实现位置"有一个重要原则：**优先复用已有公共模块。**

如果项目里已经有一个"金额计算工具类"，coding-skill 不会新建一个"金额计算类"，而是复用已有的。这个"复用"原则避免了"重复造轮子"——这是传统开发中常见的问题。

### 3.10 "编码顺序"的稳定性思考

为什么 coding-skill 要"先数据模型，后配置"？

因为数据模型是"最稳定"的——它定义了系统的"骨架"，不容易变化。而配置是"最容易变化"的——环境不同、端口不同、参数不同。

先写稳定的，后写容易变的，这个顺序保证了：如果配置改了，数据模型不用改；如果数据模型要改，那说明需求本身变了。


### 3.11 实现上下文的"工程价值"

实现上下文不只是"给 AI 看的"，它还有工程价值：

```text
实现上下文文件：
- .harness/changes/<id>/context.md
- 包含：需求摘要、AC 列表、涉及文件、依赖关系
- 作用：让 AI 和人类"共享同一个上下文"
```

这个"共享上下文"的机制，保证了"AI 知道的和人类知道的一样多"——不会出现"AI 不知道某个约束"的情况。

### 3.12 编码顺序的"案例"：从数据模型到配置

来看一个具体的编码顺序示例：

```text
1. 数据模型：Order.java（定义订单数据结构）
2. 接口：OrderService.java（定义订单服务接口）
3. 实现：OrderServiceImpl.java（实现 TDD 循环）
4. 配置：application.yml（配置数据库连接）
```

这个顺序保证了"先稳定后可变"——数据模型和接口是稳定的，实现和配置是相对容易变化的。
## 四、/coding-skill 的异常处理

### 4.1 设计偏差

实现过程中，coding-skill 可能发现"需求卡片中的假设不成立"。此时 coding-skill 不会"自作主张"修改需求，而是"停下来"：描述"假设 X 但实际 Y"，给 2-3 个替代方案，等人类决策。

### 4.2 依赖阻塞

如果外部资源不可用，coding-skill 的处理方式是：写 Mock/Stub 实现核心逻辑，在 change.md 登记"替换 Mock"任务，然后继续。

### 4.3 领域术语冲突

如果实现时发现代码中的术语与 `.harness/CONTEXT.md` 不一致，coding-skill 会调用 `domain-modeling` 技能精化术语、更新 CONTEXT.md。

<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 240" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_cs4" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_cs4"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="240" fill="url(#bg_cs4)" rx="10"/>
  <text x="400" y="25" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">/coding-skill 的异常处理决策树</text>
  <rect x="40" y="55" width="220" height="55" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_cs4)"/>
  <text x="150" y="77" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">设计偏差</text>
  <text x="150" y="95" text-anchor="middle" fill="#94a3b8" font-size="10">停下来，给方案，请人类决策</text>
  <line x1="260" y1="82" x2="290" y2="82" stroke="#475569" stroke-width="2"/>
  <polygon points="295,82 287,77 287,87" fill="#94a3b8"/>
  <rect x="300" y="55" width="220" height="55" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_cs4)"/>
  <text x="410" y="77" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">依赖阻塞</text>
  <text x="410" y="95" text-anchor="middle" fill="#94a3b8" font-size="10">写 Mock 继续，登记替换任务</text>
  <line x1="520" y1="82" x2="550" y2="82" stroke="#475569" stroke-width="2"/>
  <polygon points="555,82 547,77 547,87" fill="#94a3b8"/>
  <rect x="560" y="55" width="200" height="55" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_cs4)"/>
  <text x="660" y="77" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">术语冲突</text>
  <text x="660" y="95" text-anchor="middle" fill="#94a3b8" font-size="10">调用 domain-modeling</text>
  <text x="400" y="175" text-anchor="middle" fill="#475569" font-size="11">三种异常共享同一原则：不跳过、不假设、不沉默</text>
  <text x="400" y="200" text-anchor="middle" fill="#64748b" font-size="10">AI 发现问题时，不是自己解决，而是通知人类</text>
</svg>



### 4.4 异常处理的"三不"原则

coding-skill 的异常处理遵循"三不"原则：

1. **不跳过**：遇到异常不跳过，必须停下来处理
2. **不假设**：遇到问题不假设"AI 自己可以解决"，而是"请人类决策"
3. **不沉默**：遇到问题不沉默，必须记录到 change.md 中

这个"三不"原则，保证了异常的"可见性"——用户不会"莫名其妙"地发现代码有问题，而是"明明白白"地知道"这里有问题"。


### 4.5 异常处理的"记录"价值

coding-skill 异常处理中，有一个容易被忽略的价值：**记录**。

每次异常处理（设计偏差、依赖阻塞、术语冲突），都会被记录到 change.md 中。这个"记录"的价值在于：

- 用户可以看到"AI 在编码过程中遇到了什么问题"
- 后续的代码审查者可以看到"这个决策是怎么做出的"
- 后续的维护者可以看到"为什么这里用了 Mock"

这个"记录"让 coding-skill 的"思维过程"变得可见——它不是"黑盒"地写代码，而是"透明"地写代码。

### 4.6 异常处理的"三思"原则

coding-skill 遇到异常时，会"三思"：

1. 一思：这个异常是"真的"还是"假的"？——确认异常是否真实存在
2. 二思：这个异常是否需要"停下来"？——判断异常的影响范围
3. 三思：这个异常是否需要"记录"？——判断异常是否值得记录

这个"三思"原则，保证了异常处理的"精确性"——不会每个小问题都停下来，也不会放过真正的问题。
## 五、/coding-skill 与流水线的联动

### 5.1 在流水线中的位置

/coding-skill 是 Harness 流水线的"第二个命令"——在 /harnessing 之后，在 /unit-test-write 之前。

### 5.2 状态管理

/coding-skill 执行过程中，change.md 的状态会变化：
- coding（开始编码）
- testing（编码完成）
- 如果发现需求偏差，回退到 analyzing

### 5.3 与 /unit-test-write 的协作

/coding-skill 负责核心业务逻辑的 TDD 测试，/unit-test-write 负责非核心逻辑的测试补全。

### 5.4 与 /expert-reviewer 的协作

实时自检发现"表面问题"，代码审查发现"深层问题"。

<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 220" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_cs5" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_cs5"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="220" fill="url(#bg_cs5)" rx="10"/>
  <text x="400" y="25" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">/coding-skill 在流水线中的位置</text>
  <rect x="60" y="50" width="150" height="55" rx="8" fill="#3b82f6" opacity="0.15" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_cs5)"/>
  <text x="135" y="72" text-anchor="middle" fill="#93c5fd" font-size="11" font-weight="700">/harnessing</text>
  <text x="135" y="90" text-anchor="middle" fill="#94a3b8" font-size="10">需求分析</text>
  <line x1="210" y1="77" x2="240" y2="77" stroke="#475569" stroke-width="2"/>
  <polygon points="245,77 237,72 237,82" fill="#94a3b8"/>
  <rect x="250" y="50" width="150" height="55" rx="8" fill="#22c55e" opacity="0.2" stroke="#22c55e" stroke-width="2" filter="url(#sh_cs5)"/>
  <text x="325" y="72" text-anchor="middle" fill="#86efac" font-size="11" font-weight="700">/coding-skill</text>
  <text x="325" y="90" text-anchor="middle" fill="#94a3b8" font-size="10">编码实现</text>
  <line x1="400" y1="77" x2="430" y2="77" stroke="#475569" stroke-width="2"/>
  <polygon points="435,77 427,72 427,82" fill="#94a3b8"/>
  <rect x="440" y="50" width="150" height="55" rx="8" fill="#f59e0b" opacity="0.15" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_cs5)"/>
  <text x="515" y="72" text-anchor="middle" fill="#fde68a" font-size="11" font-weight="700">/unit-test-write</text>
  <text x="515" y="90" text-anchor="middle" fill="#94a3b8" font-size="10">测试补全</text>
  <line x1="590" y1="77" x2="620" y2="77" stroke="#475569" stroke-width="2"/>
  <polygon points="625,77 617,72 617,82" fill="#94a3b8"/>
  <rect x="630" y="50" width="130" height="55" rx="8" fill="#a855f7" opacity="0.15" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_cs5)"/>
  <text x="695" y="72" text-anchor="middle" fill="#d8b4fe" font-size="11" font-weight="700">/reviewer</text>
  <text x="695" y="90" text-anchor="middle" fill="#94a3b8" font-size="10">代码审查</text>
  <text x="400" y="160" text-anchor="middle" fill="#475569" font-size="11">状态链路：coding→testing→reviewing→verifying</text>
  <text x="400" y="185" text-anchor="middle" fill="#64748b" font-size="10">需求卡片是流水线的核心资产，状态变化记录进度</text>
</svg>



### 5.5 状态管理的完整生命周期

下面是一个完整的 change.md 状态流转示例：

```markdown
# 变更卡

| 状态 | 说明 |
|------|------|
| analyzing | /harnessing 需求分析中 |
| coding | /coding-skill 编码中 |
| testing | /unit-test-write 测试补全中 |
| reviewing | /expert-reviewer 代码审查中 |
| verifying | /unit-test-ci 门禁验证中 |
| deployed | /deploy-verify 部署验证通过 |
```

这个状态流转示例展示了哈ness 流水线的"进度可视化"——每个阶段都有明确的状态，用户随时可以查看进度。

### 5.6 与 /unit-test-write 的"分工"细节

/coding-skill 和 /unit-test-write 的分工，可以精确到"哪些测试"：

```text
coding-skill 负责：
- 核心业务逻辑的 TDD 测试（每个 AC 一个测试）
- 白盒测试（了解内部实现）

unit-test-write 负责：
- 边界情况测试（空值、异常、极限值）
- 异常路径测试（依赖失败、超时）
- 集成测试（模块间协作）
```

这个分工保证了测试的完整覆盖——coding-skill 保证"核心逻辑测了"，unit-test-write 保证"边界和异常也测了"。

### 5.7 与 /harnessing 的"双向"协作

coding-skill 和 /harnessing 不只是"前后关系"，还有"双向协作"：

1. 如果编码过程中发现需求不明确，coding-skill 会"回退"到 /harnessing，更新需求卡片
2. 如果需求卡片更新（analyzing-updated），coding-skill 会"重新加载"需求卡片，调整编码方向

这个"双向协作"保证了"需求与编码的持续对齐"——不是"需求写完了就不能改"，而是"需求随时可以改，编码随时可以调整"。

### 5.8 与 /deploy-verify 的"最终"协作

coding-skill 和 /deploy-verify 的协作，是"最远"的——从"编码的开始"到"部署的结束"。

coding-skill 写的代码，最终会通过 /deploy-verify 部署到生产环境。所以 coding-skill 的"质量"直接影响部署的"成功率"。

这个"最远"的协作，让 coding-skill 的"质量意识"延伸到了"生产环境"——它不只是"写代码"，而是"写能上线的代码"。

## 六、/coding-skill 的设计哲学

### 6.1 为什么"先写失败测试"

它强制 AI 先思考"我要做什么"，再思考"怎么做"。先写测试，AI 必须先定义 API 的形状——方法签名、参数类型、返回值类型——然后才写实现。

### 6.2 为什么"不批量"

AI 的"批量"能力其实是一个陷阱。AI 可以在几秒内生成 100 行代码，但如果这 100 行代码有 10 个不同的问题，AI 很难一次性修复所有问题。

### 6.3 为什么"最小实现"

只写刚好让测试通过的代码，避免过度设计。AI 很容易"过度设计"——写了一个方法，顺便把"未来可能需要的功能"也实现了。

### 6.4 为什么"小步编译"

每个 TDD 循环结束后，都要确保代码可编译。这个原则保证了"错误不会累积"。

<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 240" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_cs6" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_cs6"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="240" fill="url(#bg_cs6)" rx="10"/>
  <text x="400" y="25" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">/coding-skill 的四大设计哲学</text>
  <rect x="40" y="55" width="170" height="70" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_cs6)"/>
  <text x="125" y="77" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">先写失败测试</text>
  <text x="125" y="95" text-anchor="middle" fill="#94a3b8" font-size="10">先定义 API 形状</text>
  <text x="125" y="112" text-anchor="middle" fill="#94a3b8" font-size="10">再写实现</text>
  <rect x="225" y="55" width="170" height="70" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_cs6)"/>
  <text x="310" y="77" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">不批量</text>
  <text x="310" y="95" text-anchor="middle" fill="#94a3b8" font-size="10">一次一个功能点</text>
  <text x="310" y="112" text-anchor="middle" fill="#94a3b8" font-size="10">假设不累积</text>
  <rect x="410" y="55" width="170" height="70" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_cs6)"/>
  <text x="495" y="77" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">最小实现</text>
  <text x="495" y="95" text-anchor="middle" fill="#94a3b8" font-size="10">只写刚好的代码</text>
  <text x="495" y="112" text-anchor="middle" fill="#94a3b8" font-size="10">避免过度设计</text>
  <rect x="595" y="55" width="170" height="70" rx="8" fill="#a855f7" opacity="0.12" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_cs6)"/>
  <text x="680" y="77" text-anchor="middle" fill="#d8b4fe" font-size="12" font-weight="700">小步编译</text>
  <text x="680" y="95" text-anchor="middle" fill="#94a3b8" font-size="10">每循环都编译</text>
  <text x="680" y="112" text-anchor="middle" fill="#94a3b8" font-size="10">错误不累积</text>
  <text x="400" y="180" text-anchor="middle" fill="#475569" font-size="11">四个哲学相互支撑：先测试→不批量→最小实现→小步编译</text>
  <text x="400" y="205" text-anchor="middle" fill="#64748b" font-size="10">共同目标：把 AI 的编码质量提高到工程级</text>
</svg>



### 6.5 "最小实现"的边界在哪里

有人会问："最小实现"到底有多小？它的边界在哪里？

答案：**刚好让测试通过，且不复用"未来可能需要的代码"。**

如果测试要求"返回订单号"，最小实现就是"返回 UUID"。不能因为"未来可能要订单号包含用户信息"，就把用户信息塞进去。

但如果测试要求"校验商品列表不为空"，最小实现就必须包含"校验逻辑"——因为它刚好让测试通过。

这个"刚好"的边界，是"最小实现"的精华——它既不过度设计，也不过早实现。

### 6.6 "小步编译"的工程价值

"小步编译"不只是"确保代码可编译"，它还有一个更深的工程价值：**它为"一 task 一 commit"做准备。**

在 coding-skill 中，每个 TDD 循环对应一个"task"，每个"task"完成后可以提交一个 commit。这个"一 task 一 commit"的实践，让代码的"历史"变得清晰——每个 commit 只做一件事，回退容易，审查容易。

这个实践，是"小步编译"的"延伸价值"——它从"编译"延伸到了"版本管理"。


### 6.7 "先写测试"的"心理安全"延伸

在 AI 编码中，"先写测试"还有一个"心理安全"的延伸价值：**它让用户敢信任 AI 的代码。**

用户不信任 AI 的代码，是因为"不知道 AI 写的对不对"。但如果 AI 写了测试，而且测试通过了，用户就可以"看测试就知道代码对不对"。

这个"信任"的建立，是 coding-skill 的"社会价值"——它让"人机协作"变得更加可信。

### 6.8 "最小实现"的"反直觉"价值

"最小实现"在直觉上似乎"不够好"——只写刚好让测试通过的代码，代码质量会不会太低？

但实际效果恰恰相反："最小实现"反而提高了代码质量。因为"只写刚好够的代码"，代码的"复杂度"最低——没有多余的逻辑、没有未使用的分支、没有过度设计的抽象。

这个"反直觉"的价值，是"最小实现"的精华：**不是"少写代码"，而是"不多写不需要的代码"。**

### 6.9 四大设计哲学的整体价值

四大设计哲学不是独立的，而是整体的——它们相互支撑，共同构成了 coding-skill 的质量体系：

先写测试决定了质量底线——没有测试就没有质量保证；不批量决定了质量节奏——一次只做一个，确保每个功能点都有质量；最小实现决定了质量纯度——不写多余的代码，降低复杂度；小步编译决定了质量验证——每步都验证，错误不累积。

这个整体的价值，大于四个部分的简单相加。

## 七、实战案例：订单创建功能

### 7.1 前置检查与上下文

change.md 已存在，状态为 coding，包含 3 个 AC：
1. 创建订单成功时返回订单号
2. 创建订单时校验商品列表不为空
3. 创建订单时计算总金额

### 7.2 TDD 循环 1：创建订单成功

Red：`should_create_order_successfully`——创建订单，断言订单号不为空
Green：`new Order(orderId: UUID.randomUUID().toString())`
Refactor：不需要

### 7.3 TDD 循环 2：校验商品列表

Red：`should_throw_when_items_empty`——传入空列表，期望异常
Green：添加 `if (request.getItems().isEmpty()) throw ...`
Refactor：不需要

### 7.4 TDD 循环 3：计算总金额

Red：`should_calculate_total_amount`——传入商品列表，断言总金额
Green：调用 `itemService.getPrice()` 求和
Refactor：提取"价格计算"为独立方法

### 7.5 案例的启示

这个案例展示了 coding-skill 的"节奏"：每个循环 3-5 分钟，3 个循环 15 分钟完成一个完整的订单创建功能。如果批量写，可能需要 30 分钟，而且质量更低。

<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 300" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_cs7" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_cs7"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="300" fill="url(#bg_cs7)" rx="10"/>
  <text x="400" y="25" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">订单创建案例的 TDD 循环过程</text>
  <rect x="40" y="50" width="220" height="55" rx="8" fill="#ef4444" opacity="0.15" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_cs7)"/>
  <text x="150" y="72" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">循环 1：创建订单</text>
  <text x="150" y="90" text-anchor="middle" fill="#94a3b8" font-size="10">AC1：订单号不为空</text>
  <line x1="260" y1="77" x2="290" y2="77" stroke="#475569" stroke-width="2"/>
  <polygon points="295,77 287,72 287,82" fill="#94a3b8"/>
  <rect x="300" y="50" width="220" height="55" rx="8" fill="#ef4444" opacity="0.15" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_cs7)"/>
  <text x="410" y="72" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">循环 2：校验列表</text>
  <text x="410" y="90" text-anchor="middle" fill="#94a3b8" font-size="10">AC2：空列表抛异常</text>
  <line x1="520" y1="77" x2="550" y2="77" stroke="#475569" stroke-width="2"/>
  <polygon points="555,77 547,72 547,82" fill="#94a3b8"/>
  <rect x="560" y="50" width="200" height="55" rx="8" fill="#ef4444" opacity="0.15" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_cs7)"/>
  <text x="660" y="72" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">循环 3：计算金额</text>
  <text x="660" y="90" text-anchor="middle" fill="#94a3b8" font-size="10">AC3：总金额正确</text>
  <line x1="660" y1="105" x2="660" y2="130" stroke="#475569" stroke-width="1.5"/>
  <polygon points="660,135 655,128 665,128" fill="#94a3b8"/>
  <rect x="300" y="140" width="220" height="40" rx="6" fill="#22c55e" opacity="0.15" stroke="#22c55e" stroke-width="1.5"/>
  <text x="410" y="164" text-anchor="middle" fill="#86efac" font-size="11" font-weight="700">全部通过，状态→testing</text>
  <text x="400" y="215" text-anchor="middle" fill="#475569" font-size="11">每个循环 3-5 分钟，3 个循环 15 分钟完成</text>
  <text x="400" y="240" text-anchor="middle" fill="#64748b" font-size="10">vs 批量写代码：30 分钟，且质量更低</text>
</svg>



下面是创建订单功能的完整代码示例：

```java
// 订单服务
@Service
public class OrderService {
    private final ItemService itemService;

    public OrderService(ItemService itemService) {
        this.itemService = itemService;
    }

    public Order createOrder(OrderRequest request) {
        if (request.getItems().isEmpty()) {
            throw new IllegalArgumentException("商品列表不能为空");
        }
        double total = request.getItems().stream()
            .mapToDouble(item -> itemService.getPrice(item))
            .sum();
        return new Order(orderId: UUID.randomUUID().toString(), totalAmount: total);
    }
}

// 订单测试
@SpringBootTest
class OrderServiceTest {
    @MockBean
    private ItemService itemService;

    @Autowired
    private OrderService orderService;

    @Test
    void should_create_order_successfully() {
        OrderRequest request = new OrderRequest(List.of("item1", "item2"));
        Order order = orderService.createOrder(request);
        assertNotNull(order.getOrderId());
    }

    @Test
    void should_throw_when_items_empty() {
        OrderRequest request = new OrderRequest(emptyList());
        assertThrows(IllegalArgumentException.class, () -> {
            orderService.createOrder(request);
        });
    }

    @Test
    void should_calculate_total_amount() {
        when(itemService.getPrice("item1")).thenReturn(40.0);
        when(itemService.getPrice("item2")).thenReturn(60.0);
        OrderRequest request = new OrderRequest(List.of("item1", "item2"));
        Order order = orderService.createOrder(request);
        assertEquals(100.0, order.getTotalAmount());
    }
}
```

这个代码示例展示了 coding-skill 的三个 TDD 循环的最终产出：每个 AC 对应一个测试方法，每个测试方法对应一个实现逻辑。


### 7.6 案例的"反事实"推演

让我们做一个"反事实"推演：如果不用 TDD，而是让 AI 直接写订单创建功能，会发生什么？

**场景一：直接写代码**

```java
public Order createOrder(List<String> items) {
    return new Order(orderId: UUID.randomUUID().toString());
}
```

这段代码看起来"没问题"——它返回了一个订单。但它缺少了：商品列表为空的校验、总金额的计算、商品价格的获取。

**场景二：TDD 一次只做一个**

第一个测试帮助发现"创建订单"这个最小功能；第二个测试帮助发现"校验商品列表"；第三个测试帮助发现"计算总金额"。三个测试，三个功能，每个都验证了。

这个推演揭示了 TDD 的核心价值：**它让 AI 的"思考"和"验证"同步进行——每写一个功能，就验证一个功能。**

### 7.7 案例的"需求-测试-实现"映射表

| AC | 测试方法 | 实现逻辑 |
|----|---------|---------|
| AC1：订单号不为空 | should_create_order_successfully | new Order(orderId: UUID.randomUUID()) |
| AC2：空列表抛异常 | should_throw_when_items_empty | if (items.isEmpty()) throw |
| AC3：总金额正确 | should_calculate_total_amount | items.stream().sum() |

这个映射表展示了"需求到测试到实现"的 1:1 对应关系——每个 AC 有一个测试，每个测试有一个实现。

### 7.8 案例的"扩展"：如果需求更复杂

如果订单功能更复杂——比如需要考虑折扣、会员价、运费——coding-skill 的处理方式是：

1. 先实现"基础订单"（当前 3 个 AC）
2. 再实现"折扣"（新增 AC）
3. 再实现"运费"（新增 AC）

每个新功能都是一个"新的 TDD 循环"，不会影响已有的功能。这个"增量"特性，保证了代码的"稳定性"——新功能不会破坏旧功能。
## 八、/coding-skill 的"完成标志"

### 8.1 出口门禁

/coding-skill 的"出口门禁"是：
1. 失败测试已先行——所有核心业务逻辑都有测试保护
2. 核心实现可编译——代码没有语法错误
3. 符合编码规范——通过了实时自检
4. 状态更新——change.md 从 coding 变为 testing

### 8.2 与 /harnessing 的对比

| 维度 | /harnessing | /coding-skill |
|------|------------|--------------|
| 目标 | 把模糊需求变成可执行的 AC | 把需求卡片变成可编译的代码 |
| 方法 | 决策树对话 | TDD 小循环 |
| 输出 | 需求卡片（change.md） | 可编译的实现代码 |
| 节奏 | 一次一个问题 | 一次一个 TDD 循环 |

<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 220" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_cs8" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_cs8"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="220" fill="url(#bg_cs8)" rx="10"/>
  <text x="400" y="25" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">/coding-skill 的出口门禁</text>
  <rect x="40" y="55" width="170" height="55" rx="8" fill="#22c55e" opacity="0.15" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_cs8)"/>
  <text x="125" y="77" text-anchor="middle" fill="#86efac" font-size="11" font-weight="700">失败测试已先行</text>
  <text x="125" y="95" text-anchor="middle" fill="#94a3b8" font-size="10">核心逻辑有测试保护</text>
  <rect x="225" y="55" width="170" height="55" rx="8" fill="#3b82f6" opacity="0.15" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_cs8)"/>
  <text x="310" y="77" text-anchor="middle" fill="#93c5fd" font-size="11" font-weight="700">代码可编译</text>
  <text x="310" y="95" text-anchor="middle" fill="#94a3b8" font-size="10">无语法错误</text>
  <rect x="410" y="55" width="170" height="55" rx="8" fill="#f59e0b" opacity="0.15" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_cs8)"/>
  <text x="495" y="77" text-anchor="middle" fill="#fde68a" font-size="11" font-weight="700">符合编码规范</text>
  <text x="495" y="95" text-anchor="middle" fill="#94a3b8" font-size="10">通过实时自检</text>
  <rect x="595" y="55" width="170" height="55" rx="8" fill="#a855f7" opacity="0.15" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_cs8)"/>
  <text x="680" y="77" text-anchor="middle" fill="#d8b4fe" font-size="11" font-weight="700">状态更新</text>
  <text x="680" y="95" text-anchor="middle" fill="#94a3b8" font-size="10">coding→testing</text>
  <text x="400" y="175" text-anchor="middle" fill="#475569" font-size="11">四个门禁全部通过，才允许进入下一阶段</text>
  <text x="400" y="200" text-anchor="middle" fill="#64748b" font-size="10">门禁是硬约束——不通过就不能继续</text>
</svg>






### 8.3 出口门禁的"失败"处理

如果 coding-skill 的出口门禁没有通过，会发生什么？

```text
门禁失败 → 回到编码阶段 → 修复问题 → 重新检查 → 直到通过
```

这个"循环"保证了门禁的"严格性"——不通过就不能进入下一阶段。同时，它也保证了编码的"完整性"——每个 AC 都实现了、每个测试都通过了、每段代码都符合规范了，才能说"编码完成"。

### 8.4 从"编码完成"到"测试补全"的交接

coding-skill 完成编码后，会更新 change.md 的状态为 testing，并把"测试策略"和"已写的测试"交接给 /unit-test-write。这个"交接"保证了测试的连续性——unit-test-write 知道哪些测试已经写了、哪些还没写。

这个交接的示例：

```markdown
## 测试交接

已写测试（coding-skill）：
- should_create_order_successfully
- should_throw_when_items_empty
- should_calculate_total_amount

待补测试（unit-test-write）：
- 边界情况：null 商品列表
- 异常路径：itemService 不可用
- 集成测试：订单保存到数据库
```

这个交接机制，是 coding-skill 和 unit-test-write 的"协作桥梁"。

### 8.5 完成标志的"质量度量"

coding-skill 的完成标志，可以用"质量度量"来量化：

1. 测试覆盖率：核心逻辑覆盖率 >= 80%
2. 编译通过率：100%
3. 规范合规率：100%（无违规）
4. 需求覆盖率：100%（所有 AC 都实现了）

这个"质量度量"让 coding-skill 的"完成"不再是"感觉"，而是"数据"——用户可以看到具体的数字，知道"编码质量有多高"。

### 8.6 从"完成"到"开始"：coding-skill 的"接力棒"

coding-skill 完成编码后，会把"接力棒"交给 /unit-test-write。这个"接力棒"包含：

1. 已写的测试代码
2. 测试策略（哪些测了、哪些没测）
3. 编码过程中的决策记录
4. 领域术语的更新（如果有）

这个"接力棒"机制，保证了流水线的"连续性"——每个命令都知道"前一个命令做了什么"、"我该做什么"。


### 8.7 完成标志的"检查清单"示例

一个完整的 coding-skill 完成检查清单：

1. 所有 AC 都有对应的测试方法了吗？
2. 所有测试方法都通过了吗？
3. 代码是否可编译？
4. 是否符合编码规范？
5. 是否记录了编码过程中的决策？
6. 是否更新了领域术语（如果需要）？
7. change.md 状态是否已更新为 testing？

这个检查清单，是 coding-skill 的"质量控制"工具——每个问题都回答"是"，才能说"编码完成"。
## 九、/coding-skill 的"不可替代性"

### 9.1 AI 编码的"质量分层"

可以把 AI 编码的质量分为三个层次：

1. **第一层：代码生成**——AI 根据提示生成代码片段（Copilot 模式）
2. **第二层：代码补全**——AI 理解上下文，补全当前文件（Cursor 模式）
3. **第三层：代码工程**——AI 理解需求、执行 TDD、检查规范、管理状态（coding-skill 模式）

coding-skill 属于"第三层"——它不仅是"写代码"，而是"做工程"。

### 9.2 coding-skill 的"不可替代性"

市面上有很多 AI 写代码的工具，但 coding-skill 的"不可替代性"在于：

1. **需求驱动的编码**：所有代码都源自需求卡片，不会出现"AI 自己决定写什么"
2. **TDD 保障的质量**：核心业务逻辑先写测试，保证代码的可测试性
3. **规范约束的设计**：编码规范不是"建议"，而是"约束"——违规会被阻止
4. **流水线中的状态管理**：编码不是"终点"，而是"流水线的一个环节"

### 9.3 从"写代码"到"做工程"的进化

coding-skill 代表了 AI 编码的"进化方向"：从"写代码"到"做工程"。

"写代码"是"技术活"——把需求翻译成代码。而"做工程"是"管理活"——管理需求、管理质量、管理规范、管理流程。

coding-skill 让 AI 从"技术活"升级到了"管理活"——它不只是"写代码"，而是"管理整个编码过程"。

<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 240" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_cs9" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_cs9"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="240" fill="url(#bg_cs9)" rx="10"/>
  <text x="400" y="25" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">AI 编码质量的三个层次</text>
  <rect x="40" y="55" width="220" height="55" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_cs9)"/>
  <text x="150" y="77" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">第一层：代码生成</text>
  <text x="150" y="95" text-anchor="middle" fill="#94a3b8" font-size="10">Copilot 模式</text>
  <line x1="260" y1="82" x2="290" y2="82" stroke="#475569" stroke-width="2"/>
  <polygon points="295,82 287,77 287,87" fill="#94a3b8"/>
  <rect x="300" y="55" width="220" height="55" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_cs9)"/>
  <text x="410" y="77" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">第二层：代码补全</text>
  <text x="410" y="95" text-anchor="middle" fill="#94a3b8" font-size="10">Cursor 模式</text>
  <line x1="520" y1="82" x2="550" y2="82" stroke="#475569" stroke-width="2"/>
  <polygon points="555,82 547,77 547,87" fill="#94a3b8"/>
  <rect x="560" y="55" width="200" height="55" rx="8" fill="#f59e0b" opacity="0.2" stroke="#f59e0b" stroke-width="2" filter="url(#sh_cs9)"/>
  <text x="660" y="77" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">第三层：代码工程</text>
  <text x="660" y="95" text-anchor="middle" fill="#94a3b8" font-size="10">coding-skill 模式</text>
  <text x="400" y="175" text-anchor="middle" fill="#475569" font-size="11">从"写代码"到"做工程"的进化</text>
  <text x="400" y="200" text-anchor="middle" fill="#64748b" font-size="10">管理需求、管理质量、管理规范、管理流程</text>
</svg>
  


### 9.4 coding-skill 的"进化"对 AI 编码的启示

coding-skill 从"代码生成"到"代码工程"的进化，揭示了 AI 编码的一个趋势：**AI 正在从"工具"变成"同事"**。

"工具"的特点是：你告诉它做什么，它做什么。而"同事"的特点是：它理解你为什么要做，然后主动去做。

coding-skill 的"进化"方向，就是把 AI 从"工具"变成"同事"——它不只是"写代码"，而是"理解需求、执行 TDD、检查规范、管理状态"。

### 9.5 从"写代码"到"管代码"的转变

coding-skill 的价值，不仅在于"它写了多少代码"，更在于"它管理了多少代码"。它管的是"代码的质量"——通过 TDD 保证质量；管的是"代码的规范"——通过实时自检保证规范；管的是"代码的进度"——通过状态管理保证进度。

这个"管"字，是 coding-skill 最核心的价值。

### 9.6 coding-skill 的"反思"能力

coding-skill 还有一个常被忽略的能力：**反思**。

在每个 TDD 循环的 Refactor 阶段，coding-skill 会"反思"：这个实现是否合理？是否有更好的方式？是否引入了不必要的复杂度？

这个"反思"能力，是 coding-skill 与其他 AI 编码工具的最大区别——其他工具"写完了就完了"，coding-skill "写完了还会想一想"。

### 9.7 从 coding-skill 看"工程思维"

coding-skill 的设计，体现了完整的"工程思维"：

```text
1. 边界意识：只做需求范围内的事
2. 质量意识：先测试后实现
3. 规范意识：实时自检
4. 状态意识：管理变更卡状态
5. 协作意识：与后续命令交接
```

这个"工程思维"，是 coding-skill 的"灵魂"——它不仅是"工具"，更是"工程方法论"。




### 9.8 coding-skill 的"边界"：它不替代人

最后，coding-skill 有一个"边界"：它不替代人。

coding-skill 的目标是"辅助"程序员——让程序员从"重复劳动"中解放出来，聚焦于"创造性的工作"。它不是"让程序员失业"，而是"让程序员更高效"。

这个"边界"意味着：coding-skill 永远不自己做重大决策——重大决策（比如"要不要改这个 API"、"要不要引入这个依赖"）必须由人类来做。

### 9.9 总结：/coding-skill 的"信条"

/coding-skill 的"信条"可以浓缩为一句话：

**先写测试，不批量，最小实现，小步编译。**

这 16 个字，是 coding-skill 的灵魂。
## 十、总结与展望

### 10.1 核心设计回顾

/coding-skill 的核心设计可以浓缩为五个关键词：

1. **TDD**：Red→Green→Refactor 的核心循环
2. **垂直切片**：一次只做一个功能点，不批量
3. **最小实现**：只写刚好让测试通过的代码
4. **小步编译**：每个循环都要确保代码可编译
5. **实时自检**：每个循环结束后检查编码规范

### 10.2 与市面上 AI 编码工具的对比

| 维度 | Copilot | Cursor | coding-skill |
|------|---------|--------|-------------|
| 输入 | 代码注释 | 自然语言提示 | 需求卡片 |
| 输出 | 代码片段 | 代码文件 | 可编译的完整实现 |
| 方法 | 补全/生成 | 对话式 | TDD 小循环 |
| 质量 | 无保证 | 用户控制 | 测试+规范双重保障 |
| 状态 | 无 | 无 | 流水线状态管理 |

<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 240" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_cs10" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_cs10"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="240" fill="url(#bg_cs10)" rx="10"/>
  <text x="400" y="25" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">/coding-skill vs 市面 AI 编码工具</text>
  <rect x="40" y="50" width="160" height="55" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_cs10)"/>
  <text x="120" y="72" text-anchor="middle" fill="#93c5fd" font-size="11" font-weight="700">Copilot</text>
  <text x="120" y="90" text-anchor="middle" fill="#94a3b8" font-size="10">代码片段补全</text>
  <line x1="200" y1="77" x2="230" y2="77" stroke="#475569" stroke-width="2"/>
  <polygon points="235,77 227,72 227,82" fill="#94a3b8"/>
  <rect x="240" y="50" width="160" height="55" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_cs10)"/>
  <text x="320" y="72" text-anchor="middle" fill="#86efac" font-size="11" font-weight="700">Cursor</text>
  <text x="320" y="90" text-anchor="middle" fill="#94a3b8" font-size="10">对话式代码生成</text>
  <line x1="400" y1="77" x2="430" y2="77" stroke="#475569" stroke-width="2"/>
  <polygon points="435,77 427,72 427,82" fill="#94a3b8"/>
  <rect x="440" y="50" width="160" height="55" rx="8" fill="#f59e0b" opacity="0.2" stroke="#f59e0b" stroke-width="2" filter="url(#sh_cs10)"/>
  <text x="520" y="72" text-anchor="middle" fill="#fde68a" font-size="11" font-weight="700">Windsurf</text>
  <text x="520" y="90" text-anchor="middle" fill="#94a3b8" font-size="10">Agent 模式编码</text>
  <line x1="600" y1="77" x2="630" y2="77" stroke="#475569" stroke-width="2"/>
  <polygon points="635,77 627,72 627,82" fill="#94a3b8"/>
  <rect x="640" y="50" width="140" height="55" rx="8" fill="#a855f7" opacity="0.2" stroke="#a855f7" stroke-width="2" filter="url(#sh_cs10)"/>
  <text x="710" y="72" text-anchor="middle" fill="#d8b4fe" font-size="11" font-weight="700">coding-skill</text>
  <text x="710" y="90" text-anchor="middle" fill="#94a3b8" font-size="10">TDD+工程管理</text>
  <text x="400" y="170" text-anchor="middle" fill="#475569" font-size="11">coding-skill 是唯一一个"工程级"的 AI 编码工具</text>
  <text x="400" y="195" text-anchor="middle" fill="#64748b" font-size="10">它不只是写代码，而是管理整个编码过程</text>
</svg>

### 10.3 写在最后

AI 编码的最终形态，不是"AI 替代程序员"，而是"AI 和程序员一起做工程"。而 /coding-skill，就是这场"工程合作"的起点。

### 10.4 从 coding-skill 看 AI 编码的未来

coding-skill 的设计，揭示了 AI 编码的"未来形态"：

1. **需求驱动的编码**：AI 不是"自己决定写什么"，而是"严格按需求规格写代码"
2. **质量内建的编码**：AI 不是"写完了再改"，而是"边写边测、边测边改"
3. **规范约束的编码**：AI 不是"无约束地写"，而是"按规范写"
4. **状态管理的编码**：AI 不是"一次性的写"，而是"可追溯的写"

这个"未来形态"的核心，是"工程化"——让 AI 从"写代码"升级到"做工程"。

### 10.5 写在最后：AI 编码的"三个不要"

最后，三个"不要"送给正在使用 AI 编码的你：

1. **不要相信 AI 写的第一版代码**——它可能"看起来对"，但"实际上不对"
2. **不要让 AI 批量写代码**——批量写导致的问题，比分批写更多
3. **不要跳过测试**——测试是 AI 编码的"安全网"，没有测试的 AI 代码不可信

记住这三个"不要"，你就能用好 AI 编码这个工具。


### 10.6 写在最后：AI 编码的"三问"

在使用 coding-skill 之前，问自己三个问题：

1. 需求清楚了吗？——如果需求不清楚，先 /harnessing
2. 测试写了吗？——如果测试没写，先写测试
3. 规范遵守了吗？——如果规范没遵守，先检查规范

这三个问题，是 coding-skill 的"使用指南"——问完这三个问题，你就知道该不该用 coding-skill 了。

下一篇文章，我们将深入拆解 /unit-test-write 命令，看它是如何把"需求卡片"变成"测试用例"的——这是"从需求到测试的 1:1 转换"。

---

*本文是 Harness 专栏系列的第 5 篇。下一篇：[从需求到测试用例的 1:1 转换：/unit-test-write 的测试编写哲学](./column-06-unit-test-write.md)



### 10.7 最后的话

/coding-skill 不是终点，而是起点。它代表了 AI 编码的工程化方向——让 AI 从写代码升级到做工程。下一站，/unit-test-write。


### 10.8 下篇预告

下一篇我们将深入拆解 /unit-test-write 命令，看它是如何把需求卡片中的验收标准，变成可执行的测试用例的。这是从需求到测试的 1:1 转换，也是测试驱动开发的完整闭环。


### 10.9 写在最后

/coding-skill 的设计告诉我们：AI 编码的终极形态，不是 AI 替代程序员，而是 AI 和程序员一起做工程。而做好工程的关键，就是 TDD 小循环——先写测试，不批量，最小实现，小步编译。


这 16 个字，是 AI 编码的工程纪律，也是 coding-skill 献给每一位 AI 编码者的礼物。


下一篇文章，我们继续深入拆解 /unit-test-write 命令，看测试是如何从需求卡片中诞生的。
*
