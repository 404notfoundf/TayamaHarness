# 从需求到测试的 1:1 转换：/unit-test-write 的测试矩阵工程

> 命令深度拆解 · 第三篇 · 约 10000 字 · 10 个 SVG 图

## 一、/unit-test-write 不是什么

在 Harness 流水线中，unit-test-write 是第三个命令——在 /coding-skill 完成编码之后，进入测试补全阶段。它负责把需求卡片中的验收标准、边界情况、降级路径，系统地转换成可执行的测试用例。

但很多人对它也有误解：

**它不是"写测试的 AI 工具"。** 市面上有很多测试生成工具，它们的特点是扫描代码，自动生成测试。但 unit-test-write 不是这样工作的——它不扫描代码，它读取需求卡片。

**它不是"覆盖率收割机"。** 生成一堆 assertNotNull 来凑覆盖率，不是 unit-test-write 的风格。它的目标不是覆盖率数字好看，而是每条 AC、每个边界、每条降级路径都真实成立。

**它不是"/coding-skill 的补丁"。** 有人以为 coding-skill 写代码，unit-test-write 负责补测试。这不对——coding-skill 已经用 TDD 为核心逻辑写了测试，unit-test-write 负责的是非核心逻辑的补全：边界情况、降级路径、回归测试。

### 1.1 从 huazai-trip-plan 的"测试焦虑"说起

在 huazai-trip-plan 的实践中，测试有一个"焦虑"：**不知道测够了没有。**

程序员写完代码，跑一遍测试，全绿。但"全绿"不等于"测够了"——可能只测了 happy path，边界情况、降级路径全都没测。但谁也不知道哪里没测。

这个"焦虑"的根源是：测试是"拍脑袋"写的，没有一个"清单"告诉你"该测哪些"。

unit-test-write 的诞生，就是要解决这个"焦虑"——它把测试从"拍脑袋"变成"按清单"：从需求卡片提取 AC、边界、降级路径，生成一个"测试矩阵"，然后逐项测试。测完了，清单就划完了，焦虑就消失了。


### 1.3 测试矩阵 vs 测试驱动开发

unit-test-write 和 TDD（测试驱动开发）的关系是"互补"而非"替代"：

- **TDD 负责"写代码之前"**：红灯-绿灯-重构，确保核心逻辑正确
- **unit-test-write 负责"写代码之后"**：补全边界、降级、回归，确保测试完整

这个分工的本质是"不同阶段需要不同类型的测试"：TDD 适合"核心逻辑"的快速迭代，unit-test-write 适合"非核心逻辑"的系统补全。两者结合，才是"完整的测试覆盖"。

### 1.4 测试的"三层抽象"

unit-test-write 的测试矩阵，有三层抽象：

1. **需求层**：change.md 里的 AC、边界、降级路径——这是"业务语言"
2. **测试点层**：需求映射成的测试点清单——这是"测试语言"
3. **测试用例层**：测试点落地的具体代码——这是"代码语言"

三层抽象的价值在于"解耦"：需求变了，改测试点清单；测试点清单变了，改测试用例。每一步都是"可追溯"的——从测试用例回溯到测试点，再回溯到需求。

<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 240" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_ut9" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_ut9"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="240" fill="url(#bg_ut9)" rx="10"/>
  <text x="400" y="25" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">测试三层抽象</text>
  <rect x="60" y="55" width="200" height="70" rx="8" fill="#3b82f6" opacity="0.15" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_ut9)"/>
  <text x="160" y="80" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">需求层</text>
  <text x="160" y="100" text-anchor="middle" fill="#94a3b8" font-size="10">change.md / AC / 边界</text>
  <line x1="260" y1="90" x2="300" y2="90" stroke="#475569" stroke-width="2"/>
  <polygon points="305,90 297,85 297,95" fill="#94a3b8"/>
  <rect x="310" y="55" width="200" height="70" rx="8" fill="#f59e0b" opacity="0.15" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_ut9)"/>
  <text x="410" y="80" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">测试点层</text>
  <text x="410" y="100" text-anchor="middle" fill="#94a3b8" font-size="10">测试点清单</text>
  <line x1="510" y1="90" x2="550" y2="90" stroke="#475569" stroke-width="2"/>
  <polygon points="555,90 547,85 547,95" fill="#94a3b8"/>
  <rect x="560" y="55" width="200" height="70" rx="8" fill="#22c55e" opacity="0.15" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_ut9)"/>
  <text x="660" y="80" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">测试用例层</text>
  <text x="660" y="100" text-anchor="middle" fill="#94a3b8" font-size="10">具体代码</text>
  <text x="400" y="180" text-anchor="middle" fill="#475569" font-size="11">三层可追溯：用例 -> 测试点 -> 需求</text>
  <text x="400" y="205" text-anchor="middle" fill="#64748b" font-size="10">需求变了，改测试点清单；测试点变了，改测试用例</text>
</svg>

### 1.5 测试的"输出"价值

unit-test-write 的测试，不只是"质量保障"，还有"输出价值"：

- **文档价值**：可读的测试 = 可执行的文档
- **设计价值**：不可测试的代码 = 设计有问题
- **安全价值**：测试覆盖全 = 重构有底气

这个"输出价值"的视角，把测试从"成本中心"变成"价值中心"——测试不是"写完了就完了"，而是"持续产出价值"。

### 1.2 /unit-test-write 的"输入-输出"契约

/unit-test-write 的输入输出契约非常清晰：

- **输入**：实现代码 + change.md（需求卡片）
- **输出**：补全的测试用例 + 更新后的 change.md（状态变为 reviewing）
- **约束**：不修改实现代码、不覆盖 coding-skill 已写的测试、不为覆盖率而测

这个契约定义了 unit-test-write 的"职责边界"——它不碰实现代码，只负责"让测试覆盖得更完整"。

<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 200" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_ut1" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_ut1"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="200" fill="url(#bg_ut1)" rx="10"/>
  <text x="400" y="25" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">/unit-test-write 的输入-输出契约</text>
  <rect x="40" y="50" width="220" height="55" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_ut1)"/>
  <text x="150" y="72" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">输入</text>
  <text x="150" y="90" text-anchor="middle" fill="#94a3b8" font-size="10">实现代码 + change.md</text>
  <line x1="260" y1="77" x2="290" y2="77" stroke="#475569" stroke-width="2"/>
  <polygon points="295,77 287,72 287,82" fill="#94a3b8"/>
  <rect x="300" y="50" width="220" height="55" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_ut1)"/>
  <text x="410" y="72" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">处理</text>
  <text x="410" y="90" text-anchor="middle" fill="#94a3b8" font-size="10">测试矩阵 + 分层测试</text>
  <line x1="520" y1="77" x2="550" y2="77" stroke="#475569" stroke-width="2"/>
  <polygon points="555,77 547,72 547,82" fill="#94a3b8"/>
  <rect x="560" y="50" width="200" height="55" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_ut1)"/>
  <text x="660" y="72" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">输出</text>
  <text x="660" y="90" text-anchor="middle" fill="#94a3b8" font-size="10">补全的测试用例</text>
  <text x="400" y="160" text-anchor="middle" fill="#475569" font-size="11">核心指标：覆盖率 >=80%</text>
  <text x="400" y="180" text-anchor="middle" fill="#64748b" font-size="10">状态变更：change.md testing -> reviewing</text>
</svg>

## 二、测试矩阵：从需求到测试的 1:1 映射

### 2.1 测试矩阵的核心思想

unit-test-write 的核心工具是"测试矩阵"。它的思想是：**把需求卡片中的每一条 AC、每一个边界、每一条降级路径，都映射到一个测试用例。**

这个"1:1 映射"是 unit-test-write 的灵魂——它不是"生成尽量多的测试"，而是"确保每条需求都有测试"。

### 2.2 从 change.md 提取测试点

测试矩阵的第一步，是从 change.md 提取"测试点"。change.md 里有什么，就应该测什么：

- **AC（验收标准）**：每条 AC 至少一个测试
- **边界情况**：空/零值、极值、并发、重复执行
- **降级路径**：依赖故障时的回退逻辑（超时、限频、API 不可用）
- **回归测试**：已修复 bug、历史故障

这个"提取"的过程，是"需求到测试"的第一次转换——需求是"Business 语言"，测试点是"Test 语言"。

<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 260" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_ut2" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_ut2"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="260" fill="url(#bg_ut2)" rx="10"/>
  <text x="400" y="25" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">从需求到测试的 1:1 映射</text>
  <rect x="40" y="50" width="170" height="55" rx="8" fill="#3b82f6" opacity="0.15" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_ut2)"/>
  <text x="125" y="72" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">AC 验收标准</text>
  <text x="125" y="90" text-anchor="middle" fill="#94a3b8" font-size="10">happy path</text>
  <line x1="210" y1="77" x2="240" y2="77" stroke="#475569" stroke-width="2"/>
  <polygon points="245,77 237,72 237,82" fill="#94a3b8"/>
  <rect x="250" y="50" width="170" height="55" rx="8" fill="#22c55e" opacity="0.15" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_ut2)"/>
  <text x="335" y="72" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">边界情况</text>
  <text x="335" y="90" text-anchor="middle" fill="#94a3b8" font-size="10">空值/极值/并发</text>
  <line x1="420" y1="77" x2="450" y2="77" stroke="#475569" stroke-width="2"/>
  <polygon points="455,77 447,72 447,82" fill="#94a3b8"/>
  <rect x="460" y="50" width="170" height="55" rx="8" fill="#f59e0b" opacity="0.15" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_ut2)"/>
  <text x="545" y="72" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">降级路径</text>
  <text x="545" y="90" text-anchor="middle" fill="#94a3b8" font-size="10">依赖故障回退</text>
  <line x1="630" y1="77" x2="660" y2="77" stroke="#475569" stroke-width="2"/>
  <polygon points="665,77 657,72 657,82" fill="#94a3b8"/>
  <rect x="670" y="50" width="110" height="55" rx="8" fill="#a855f7" opacity="0.15" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_ut2)"/>
  <text x="725" y="72" text-anchor="middle" fill="#d8b4fe" font-size="12" font-weight="700">回归</text>
  <text x="725" y="90" text-anchor="middle" fill="#94a3b8" font-size="10">历史故障</text>
  <text x="400" y="165" text-anchor="middle" fill="#475569" font-size="11">每条需求 -> 至少一个测试用例</text>
  <text x="400" y="190" text-anchor="middle" fill="#64748b" font-size="10">测试矩阵是需求完整性的清单</text>
  <text x="400" y="230" text-anchor="middle" fill="#94a3b8" font-size="11">测试矩阵 = 需求卡片 -> 测试点的 1:1 映射</text>
</svg>


### 2.4 测试矩阵的"颗粒度"

unit-test-write 的测试矩阵，有一个"颗粒度"问题：**一个测试点应该多大？**

答案是：**一个测试点 = 一个独立的"行为"**。比如：

- "创建订单时返回订单号"是一个行为——一个测试点
- "创建订单时验证库存"是另一个行为——另一个测试点
- "创建订单时发送通知"是第三个行为——第三个测试点

这个"颗粒度"的价值在于"独立"——每个测试点独立，一个测试失败不影响其他测试。如果两个行为放在一个测试点里，一个失败你就不知道"是哪个行为有问题"。

### 2.5 测试矩阵的"版本管理"

unit-test-write 的测试矩阵，作为一个"清单"文件，存放在 .harness 目录下：

```
.harness/changes/2024-01-01-feature-001/
  change.md        # 需求卡片
  test-matrix.md   # 测试矩阵清单
  tests/           # 测试用例
```

这个"版本管理"的价值在于"可追溯"——每次需求变更，测试矩阵都跟着变。看到测试矩阵，就知道"这个版本测了哪些"。

### 2.6 测试矩阵的"自动化"价值

unit-test-write 的核心突破，是"测试矩阵的自动化"——不需要手动维护，而是从 change.md 自动生成。

传统开发中，测试矩阵是"手动维护的 Excel 表格"——没人维护，很快就过期了。unit-test-write 把它变成"自动生成的清单"——change.md 变了，测试矩阵就变了。

这个"自动化"的价值在于"零维护"——开发者不需要"记得"更新测试矩阵，因为它是自动生成的。

### 2.7 测试矩阵的"完整性"检查

unit-test-write 有一个"完整性检查"步骤：**测试矩阵是否覆盖了所有测试点？**

```
完整性检查：
[OK] AC1 已覆盖 -> should_trigger_HITL
[OK] AC2 已覆盖 -> should_not_trigger_HITL
[OK] 边界已覆盖（边界值 15%）
[OK] 降级已覆盖（地图 API 不可用）
=> 完整度：100%
```

如果完整性不足，unit-test-write 不会进入"编写"阶段，而是先"补充测试点"。这个"完整性检查"的本质是"门禁前置"——在写代码之前，先确认"该测的都列了"。

### 2.3 测试矩阵的"无遗漏"承诺

测试矩阵的核心价值是"无遗漏"——它保证"需求里提到的每件事都有测试"。

在传统开发中，"遗漏"是测试最大的敌人。程序员写测试时，凭"记忆"和"感觉"，很容易漏掉某些边界情况。测试矩阵解决了这个问题——它不是靠"记忆"，而是靠"清单"。清单上有什么，就测什么；清单划完了，就测完了。

这个"无遗漏"承诺，是 unit-test-write 的第一价值。

## 三、分层测试：不同的测试，不同的目标

### 3.1 四层测试体系

unit-test-write 把测试分为四层，每一层的目标不同：

1. **Spec Tests**：验证每条 AC 的 happy path / 核心行为
2. **Boundary Tests**：验证空/零值、极值、并发、重复执行
3. **Failure/Fallback Tests**：验证依赖故障时的降级逻辑
4. **Regression Tests**：验证已修复 bug / 历史故障回归

这个"分层"不是"为了分层而分层"，而是"为了目标而分层"——每层测试回答一个不同的问题。

### 3.2 Spec Tests：核心行为是否正确

Spec Tests 回答的问题是："这个功能的核心行为是否正确？"

它对应 change.md 里的每一条 AC。比如订单创建功能的 AC 是"创建订单成功时返回订单号"，Spec Test 就是"should_create_order_successfully"——验证创建订单后订单号不为空。

Spec Tests 是"最基础"的测试——它验证"功能没有做错"。

### 3.3 Boundary Tests：边界是否被覆盖

Boundary Tests 回答的问题是："边界情况是否被正确处理？"

它验证空/零值、极值、并发、重复执行等边界。比如订单创建的空商品列表、超长商品名称、重复创建订单等。

Boundary Tests 是"最容易遗漏"的测试——因为"happy path 正确"不代表"边界正确"。

### 3.4 Failure/Fallback Tests：降级是否真实

Failure/Fallback Tests 是 unit-test-write 的"特色"——它验证**降级逻辑**。

在 AI 应用（如 huazai-trip-plan）中，降级逻辑非常重要：当 LLM 超时、API 不可用、token 超限时，系统要"优雅降级"——返回离线估算、触发人工干预（HITL）、走缓存等。

unit-test-write 的规则是：**降级逻辑必须实测，不满足于"应该能处理"。** 也就是说不只是"代码里写了降级"，而是"真的用测试验证了降级真的生效"。

<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 260" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_ut3" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_ut3"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="260" fill="url(#bg_ut3)" rx="10"/>
  <text x="400" y="25" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">四层测试体系</text>
  <rect x="40" y="55" width="160" height="90" rx="8" fill="#22c55e" opacity="0.15" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_ut3)"/>
  <text x="120" y="80" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">Spec Tests</text>
  <text x="120" y="100" text-anchor="middle" fill="#94a3b8" font-size="10">核心行为</text>
  <text x="120" y="118" text-anchor="middle" fill="#94a3b8" font-size="10">每条 AC 一个</text>
  <rect x="220" y="55" width="160" height="90" rx="8" fill="#3b82f6" opacity="0.15" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_ut3)"/>
  <text x="300" y="80" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">Boundary</text>
  <text x="300" y="100" text-anchor="middle" fill="#94a3b8" font-size="10">边界情况</text>
  <text x="300" y="118" text-anchor="middle" fill="#94a3b8" font-size="10">空值/极值/并发</text>
  <rect x="400" y="55" width="160" height="90" rx="8" fill="#f59e0b" opacity="0.2" stroke="#f59e0b" stroke-width="2" filter="url(#sh_ut3)"/>
  <text x="480" y="80" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">Fallback</text>
  <text x="480" y="100" text-anchor="middle" fill="#94a3b8" font-size="10">降级逻辑</text>
  <text x="480" y="118" text-anchor="middle" fill="#94a3b8" font-size="10">超时/API 故障</text>
  <rect x="580" y="55" width="180" height="90" rx="8" fill="#a855f7" opacity="0.15" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_ut3)"/>
  <text x="670" y="80" text-anchor="middle" fill="#d8b4fe" font-size="12" font-weight="700">Regression</text>
  <text x="670" y="100" text-anchor="middle" fill="#94a3b8" font-size="10">回归测试</text>
  <text x="670" y="118" text-anchor="middle" fill="#94a3b8" font-size="10">历史故障</text>
  <text x="400" y="195" text-anchor="middle" fill="#475569" font-size="11">每层测试回答一个不同的问题</text>
  <text x="400" y="230" text-anchor="middle" fill="#64748b" font-size="10">Fallback 是特色：降级逻辑必须实测</text>
</svg>

### 3.5 Regression Tests：历史是否重现

Regression Tests 回答的问题是："已修复的 bug 会不会复发？"

它验证已修复 bug、历史故障。每次修复一个 bug，就写一个回归测试，防止"修好了又坏"。

回归测试是"质量保障"的最后一道防线——它保证"不会倒退"。


### 3.6 测试优先级的"三色法则"

unit-test-write 有一个"三色法则"来判断测试优先级：

- **红色**：降级路径（Fallback）——必须测，因为 AI 应用的核心风险在降级
- **黄色**：边界情况（Boundary）——应该测，因为"边界"是 bug 高发区
- **绿色**：核心行为（Spec）——基础测，但编码时已由 TDD 覆盖

这个"三色法则"的价值在于"优先级分配"——时间和资源有限时，先测红色，再测黄色，最后看绿色。

```
测试优先级规则：
红色（必须测）：降级逻辑、超时处理、异常回退
黄色（应该测）：空值、极值、重复、并发
绿色（基础测）：核心行为、happy path
```

### 3.7 测试的"分层运行"策略

unit-test-write 的测试是"分层运行"的，不是一次全跑：

- **开发阶段**：只跑 Spec + Boundary，快速反馈
- **提交阶段**：跑全部四层，确保完整覆盖
- **发版阶段**：跑全部测试 + 集成测试，确保完整质量

这个"分层运行"策略，让测试的"反馈速度"和"覆盖深度"达到平衡。开发阶段追求"快"，发版阶段追求"全"。

```
测试运行策略：
- 开发阶段：{{TEST_CMD}} --filter=spec,boundary（秒级）
- 提交阶段：{{TEST_CMD}} --all（分钟级）
- 发版阶段：{{TEST_CMD}} + {{INTEGRATION_CMD}}（十分钟级）
```


### 3.9 测试的"依赖注入"技巧

unit-test-write 强调"依赖注入"——被测类的外部依赖，通过构造器或 setter 注入，而不是直接 new。

为什么？因为"可测试性"要求"可替换"——如果把依赖写死在代码里（new BaiduMapTool()），测试就无法替换成 Mock。

```
// 不可测试：依赖写死
class RouteService {
    private final BaiduMapTool mapTool = new BaiduMapTool(); // 无法替换
}

// 可测试：依赖注入
class RouteService {
    private final BaiduMapTool mapTool; // 构造器注入
    RouteService(BaiduMapTool mapTool) {
        this.mapTool = mapTool;
    }
}
```

这个"依赖注入"技巧，是"可测试性设计"的基础——好的设计，天然可测试。

### 3.10 测试的"语义"解读

unit-test-write 的测试，有一个"语义"维度——测试不只是"验证行为"，还"表达意图"。

一个好的测试，应该"讲述一个故事"：在什么条件下（Arrange），做了什么（Act），期望什么（Assert）。读者看到测试，就能"理解需求"。

这个"语义"维度的价值在于"沟通"——测试是团队之间的"契约"，它告诉每个人"这个功能应该怎么工作"。

```
// 语义清晰的测试
@Test
void should_trigger_HITL_when_budget_overrun_exceeds_15_percent() {
    // 故事：预算超支超过 15% 时
    TripPlan plan = planWithCost(11500);
    // 系统会触发人工干预
    BudgetCheckResult result = budgetAgent.check(plan);
    // 以确保成本可控
    assertThat(result.requiresHumanIntervention()).isTrue();
}
```

### 3.8 测试的"副作用"检测

unit-test-write 在编写测试时，还会检测"测试的副作用"——测试是否修改了外部状态、是否污染了数据库、是否改变了全局配置。

为什么？因为"有副作用的测试"是"不可靠的测试"——这次跑通过，下次跑失败，因为环境变了。unit-test-write 的规则是：测试应该"无副作用"——跑完测试，环境应该和跑之前一样。

```
// 有副作用的测试（不推荐）
@Test
void should_create_order() {
    orderService.create(order);  // 副作用：插入了数据库
    // 没有清理
}

// 无副作用的测试（推荐）
@Test
void should_create_order() {
    orderService.create(order);  // 使用内存数据库
    assertThat(result.getOrderId()).isNotNull();
    // 自动回滚事务
}
```

## 四、编写测试：从"清单"到"代码"

### 4.1 测试命名规范

unit-test-write 有严格的测试命名规范：

- should_<期望>_when_<条件>：如 should_trigger_HITL_when_budget_overrun_exceeds_15_percent
- 或 测试方法名_场景：如 createOrder_emptyList

这个命名规范的价值在于"可读性"——看到测试名，就知道"测的是什么"。当测试失败时，测试名直接告诉你"哪里出了问题"。

### 4.2 单一断言意图

unit-test-write 强调"单一断言意图"：每个测试只验证一件事，Arrange-Act-Assert 三段清晰。

这个原则的价值在于"定位"——测试失败时，你能立刻知道"是哪个功能点失败了"。如果测试有多个断言，失败时你不知道"是哪个断言挂了"。

### 4.3 Arrange-Act-Assert 三段式

每个测试都遵循 Arrange-Act-Assert 三段式：

1. **Arrange**：准备测试数据、Mock 依赖
2. **Act**：调用被测方法
3. **Assert**：验证结果

这个三段式的价值在于"清晰"——测试的"准备、执行、验证"三阶段一目了然。


### 4.4 测试的"可维护性"原则

unit-test-write 强调测试的"可维护性"——测试也是代码，也要被维护。

可维护的测试，应该做到：避免重复数据准备、提取公共 Setup 方法、使用工厂方法创建测试数据、避免硬编码常量。

```
// 可维护的测试
private TripPlan planWithCost(int cost) {
    return TripPlan.builder()
        .totalCost(cost)
        .destination("北京")
        .days(3)
        .build();
}

@Test
void should_trigger_HITL_when_budget_overrun_exceeds_15_percent() {
    TripPlan plan = planWithCost(11500);
    BudgetCheckResult result = budgetAgent.check(plan);
    assertThat(result.requiresHumanIntervention()).isTrue();
}
```

### 4.5 测试的"异常路径"测试

unit-test-write 特别强调"异常路径"的测试——不是"正常流程"，而是"异常流程"。

异常路径包括：参数校验失败、依赖服务超时、数据库连接断开、配置加载失败、状态机冲突。这些"异常路径"在传统开发中经常被忽略，但 AI 应用中尤为重要——因为 AI 的"不确定性"比传统软件更高。

### 4.6 测试的"并发"场景

unit-test-write 的测试还覆盖"并发场景"——多个线程同时调用同一个方法，是否线程安全？

```
// 并发测试
@Test
void should_handle_concurrent_requests() throws InterruptedException {
    ExecutorService executor = Executors.newFixedThreadPool(5);
    List<Future<Boolean>> futures = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
        futures.add(executor.submit(() -> orderService.create(order)));
    }
    // 所有线程都应成功
    for (Future<Boolean> f : futures) {
        assertThat(f.get()).isTrue();
    }
}
```

并发测试的价值在于"发现偶发问题"——单线程测试没问题，并发测试可能发现"竞态条件"。

## 五、Mock 原则：测"逻辑"而非测"Mock"

### 5.1 Mock 什么，不 Mock 什么

unit-test-write 的 Mock 原则非常关键：

- **Mock 外部依赖**：LLM、第三方 API、数据库、缓存
- **禁止 Mock 自己写的业务类**：那样测的是 Mock，不是逻辑

这个原则解决了 AI 测试的一个常见陷阱：**Mock 了自己写的类，测试就"失真"了**——你测的是"Mock 的行为"，不是"真实代码的行为"。

### 5.2 Stub：可控的故障场景

unit-test-write 用 Stub 提供可控的故障场景：超时、异常、空返回。

比如，测试"地图 API 不可用时返回离线估算"：

```
@Test
void should_return_offline_estimate_when_map_api_unavailable() {
    // Arrange: 让地图 API 抛超时
    when(baiduMapTool.distance(any(), any())).thenThrow(new TimeoutException());
    // Act: 调用路线规划
    RouteResult result = routeService.plan(req);
    // Assert: 走降级，返回离线估算
    assertThat(result.isFallback()).isTrue();
}
```

这个 Stub 的价值在于"可控"——它让"故障场景"可复现、可验证。

### 5.3 Mock 的"失真"陷阱

Mock 的"失真"陷阱是：**Mock 太完美，测试太天真。**

真实的外部依赖会超时、会抛异常、会返回脏数据。如果 Mock 总是"正常返回"，测试就测不到降级逻辑。所以 unit-test-write 强调"用 Stub 制造故障"，让测试"touch"到真实的降级路径。


### 5.4 Mock 的三层工具箱

unit-test-write 的 Mock 工具箱分三层，每一层解决不同的问题：

1. **Stub（桩）**：提供预定义返回值，解决"依赖不可用"的问题
2. **Mock（模拟）**：验证调用次数与参数，解决"依赖被正确调用"的问题
3. **Spy（间谍）**：包裹真实对象，解决"既要真实行为又要观测"的问题

这三层不是"越多越好"，而是"按需使用"——Stub 用于数据准备，Mock 用于行为验证，Spy 用于混合场景。unit-test-write 的规则是：能用 Stub 就不用 Mock，能用真实对象就不用 Stub。

<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 240" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_ut7" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_ut7"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="240" fill="url(#bg_ut7)" rx="10"/>
  <text x="400" y="25" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">Mock 三层工具箱</text>
  <rect x="40" y="55" width="220" height="90" rx="8" fill="#22c55e" opacity="0.15" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_ut7)"/>
  <text x="150" y="80" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">Stub 桩</text>
  <text x="150" y="100" text-anchor="middle" fill="#94a3b8" font-size="10">预定义返回值</text>
  <text x="150" y="118" text-anchor="middle" fill="#94a3b8" font-size="10">解决"依赖不可用"</text>
  <rect x="290" y="55" width="220" height="90" rx="8" fill="#3b82f6" opacity="0.15" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_ut7)"/>
  <text x="400" y="80" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">Mock 模拟</text>
  <text x="400" y="100" text-anchor="middle" fill="#94a3b8" font-size="10">验证调用次数与参数</text>
  <text x="400" y="118" text-anchor="middle" fill="#94a3b8" font-size="10">解决"调用正确性"</text>
  <rect x="540" y="55" width="220" height="90" rx="8" fill="#a855f7" opacity="0.15" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_ut7)"/>
  <text x="650" y="80" text-anchor="middle" fill="#d8b4fe" font-size="12" font-weight="700">Spy 间谍</text>
  <text x="650" y="100" text-anchor="middle" fill="#94a3b8" font-size="10">包裹真实对象</text>
  <text x="650" y="118" text-anchor="middle" fill="#94a3b8" font-size="10">真实行为 + 观测</text>
  <text x="400" y="195" text-anchor="middle" fill="#475569" font-size="11">规则：能用 Stub 就不用 Mock，能用真实对象就不用 Stub</text>
</svg>

### 5.5 测试隔离：不依赖"顺序"

unit-test-write 强调"测试隔离"——每个测试独立运行，不依赖其他测试的执行顺序。

为什么？因为"有顺序依赖的测试"是脆弱的：一旦某个测试改了执行顺序，其他测试就挂了。调试这种"顺序性失败"非常痛苦——不是你的代码错了，是测试的顺序错了。

单元测试的隔离原则：每个测试自己准备数据、自己清理数据，不共享可变状态。这样即使并行执行，测试也稳定。

### 5.6 测试即文档

unit-test-write 有一个很有意思的观点：**测试是最好的文档。**

为什么？因为代码注释会过期，但测试不会——测试挂了，代码就错了。一个"可读"的测试，直接告诉你"这个函数应该做什么、在什么条件下做什么"。

所以 unit-test-write 的测试命名规范，本质上是"文档规范"：should_trigger_HITL_when_budget_overrun_exceeds_15_percent，这句英文本身就是功能说明。


### 5.7 Mock 的"组合"使用

unit-test-write 的测试，经常需要"组合 Mock"——同时替换多个依赖。

比如测试"路线规划"，需要同时 Mock：地图 API、LLM 规划器、缓存、费用计算器。这时，unit-test-write 会用一个"测试上下文"集中管理所有 Mock。

```
// 组合 Mock 的测试上下文
@Test
void should_plan_route_with_all_dependencies() {
    // 一次性替换所有依赖
    when(mapTool.distance(any(), any())).thenReturn(10);
    when(llmPlanner.plan(any())).thenReturn(planResult);
    when(cache.get(any())).thenReturn(null);
    when(costCalculator.calculate(any())).thenReturn(500);

    // 调用被测方法
    TripPlan result = routeService.plan(req);

    // 验证结果
    assertThat(result.getTotalCost()).isEqualTo(500);
}
```

这个"组合 Mock"的价值在于"可控"——所有外部因素都被固定，测试只关注"被测逻辑"是否正确。

### 5.8 Mock 的"过度"陷阱

unit-test-write 提醒一个"过度"陷阱：**Mock 太多，测试就"失真"了。**

如果测试 Mock 了 10 个依赖，那测试实际上在验证"Mock 的行为"，而不是"真实代码的行为"。真实代码的 Bug，可能被 Mock "掩盖"了。

unit-test-write 的规则是：**Mock 越少越好。** 能集成测试的，就不 Mock；能真实对象的，就不替换。Mock 只用于"外部依赖"（LLM、API、数据库），不用于"内部逻辑"。

```
Mock 使用原则：
- 只 Mock 外部依赖：LLM、API、数据库、缓存
- 不 Mock 内部逻辑：业务类、工具类
- Mock 越少越好：能集成就不 Mock
- 降级路径必须实测：Mock 是为了"制造故障"
```

## 六、覆盖率：不是数字，是承诺

### 6.1 覆盖率 >=80% 的规则

unit-test-write 的覆盖率规则是：核心逻辑覆盖率 >=80%。

这个 80% 不是拍脑袋定出来的，而是工程实践的经验值——80% 是一个"够用且不内耗"的平衡点。低于 80%，核心逻辑可能没测透；高于 80%，投入产出比下降。

### 6.2 覆盖率不足 -> 补测试，而非降低标准

unit-test-write 有一条铁律：**覆盖率不足，补测试，而非降低标准。**

覆盖率不达标，说明"有代码没测到"——可能是边界没测、降级没测、某条分支没测。正确的做法是"找到没测的代码，补上测试"，而不是"把覆盖率标准从 80% 降到 70%"。

### 6.3 纯 getter/setter/配置类的豁免

unit-test-write 也承认"有的代码不值得测"——纯 getter/setter、配置类，可以豁免。

这个豁免的价值在于"务实"——不是所有代码都需要测试，机械性的代码（getter/setter）测试价值低，可以跳过。但"降级逻辑"这种"关键代码"必须测。

<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 220" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs><linearGradient id="bg_ut4" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient><filter id="sh_ut4"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter></defs>
  <rect width="800" height="220" fill="url(#bg_ut4)" rx="10"/>
  <text x="400" y="25" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">覆盖率核验流程</text>
  <rect x="40" y="55" width="200" height="55" rx="8" fill="#3b82f6" opacity="0.15" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_ut4)"/>
  <text x="140" y="77" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">跑覆盖率工具</text>
  <text x="140" y="95" text-anchor="middle" fill="#94a3b8" font-size="10">COV_TOOL</text>
  <line x1="240" y1="82" x2="270" y2="82" stroke="#475569" stroke-width="2"/>
  <polygon points="275,82 267,77 267,87" fill="#94a3b8"/>
  <rect x="280" y="55" width="200" height="55" rx="8" fill="#f59e0b" opacity="0.15" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_ut4)"/>
  <text x="380" y="77" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">>=80%？</text>
  <text x="380" y="95" text-anchor="middle" fill="#94a3b8" font-size="10">核心逻辑覆盖</text>
  <line x1="480" y1="82" x2="510" y2="82" stroke="#475569" stroke-width="2"/>
  <polygon points="515,82 507,77 507,87" fill="#94a3b8"/>
  <rect x="520" y="55" width="240" height="55" rx="8" fill="#22c55e" opacity="0.15" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_ut4)"/>
  <text x="640" y="77" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">不足 -> 补测试</text>
  <text x="640" y="95" text-anchor="middle" fill="#94a3b8" font-size="10">而非降低标准</text>
  <text x="400" y="175" text-anchor="middle" fill="#475569" font-size="11">覆盖率是承诺，不是数字</text>
</svg>


### 6.4 覆盖率工具的选择

unit-test-write 的覆盖率工具，根据语言和框架自动选择：

- **Java Spring Boot**：JaCoCo
- **Python FastAPI**：pytest-cov
- **Go Gin**：go test -cover
- **前端**：vitest --coverage / jest --coverage

这个"自动选择"的价值在于"零配置"——开发者不需要知道覆盖率工具怎么装，unit-test-write 会自动装好、自动跑、自动汇报。

### 6.5 覆盖率报告的"可读性"

unit-test-write 的覆盖率报告，不只是"数字"，还有"可读性"：

```
覆盖率报告：
- 整体覆盖率：85%（达标）
- 核心逻辑覆盖率：92%（优秀）
- 未覆盖分支：UserService.getUser() 第 47-52 行
- 建议：补充 getUser 的空返回值测试
```

这个"可读性"的价值在于"可行动"——看到报告，就知道"哪里需要补测试"。

### 6.6 覆盖率的"动态阈值"

unit-test-write 支持"动态阈值"——覆盖率标准不是固定的，而是根据不同模块调整：

- 核心业务逻辑：>=85%
- 工具类/辅助类：>=70%
- 配置类/常量类：豁免

这个"动态阈值"的价值在于"精准"——不是"一刀切"的 80%，而是"核心从严、辅助从宽"。

```
覆盖率动态阈值：
- 核心业务逻辑：>=85%（严格）
- 服务层/工具类：>=70%（适中）
- 配置类/常量类：豁免（跳过）
```

### 6.7 覆盖率的"持续追踪"

unit-test-write 的最后一个覆盖特性是"持续追踪"——每次跑测试，覆盖率结果都记录到 .harness/coverage/history.md：

```
| 日期 | 覆盖率 | 核心覆盖率 | 变化 |
|------|--------|------------|------|
| 2024-01-01 | 82% | 90% | - |
| 2024-01-02 | 83% | 91% | +1% |
| 2024-01-03 | 81% | 88% | -3% |
```

持续追踪的价值在于"趋势可见"——覆盖率不是"今天达标就行"，而是"持续不下降"。

## 七、诊断辅助：当测试遇到"非确定性"

### 7.1 测试遇到"不稳定复现"的 Bug

有时候，测试会遇到"无法稳定复现的 Bug"或"非确定性故障"——这次跑失败，下次跑通过。这种"间歇性故障"是最难调试的。

unit-test-write 的处理方式是：**建议运行 /diagnosing-bugs 进行 6 阶段诊断。**

### 7.2 诊断结果的记录

诊断结果记录到 .harness/changes/<id>/diagnosis.md。这个记录的价值在于"沉淀"——诊断过程、根因分析、修复方案，都沉淀下来，供后续参考。

这个"诊断辅助"机制，是 unit-test-write 的"兜底"——当测试本身遇到问题时，它不慌，而是"求助"于专门的诊断命令。


### 7.3 诊断的六阶段流程

当测试遇到"非确定性故障"时，unit-test-write 建议运行 /diagnosing-bugs 进行六阶段诊断：

1. **复现**：建立稳定复现的反馈循环
2. **定位**：通过二分定位到最小复现用例
3. **根因**：分析根因，区分"环境问题"和"代码问题"
4. **修复**：最小化修复，不引入新问题
5. **验证**：修复后跑全部测试，确认"修好了且没破坏"
6. **沉淀**：把诊断过程记录到 diagnosis.md

这个"六阶段"的价值在于"不慌"——遇到间歇性故障，不是"瞎猜"，而是"按流程走"。

```
诊断六阶段：
1. 复现：稳定复现的反馈循环
2. 定位：二分定位最小复现
3. 根因：区分环境与代码
4. 修复：最小化修复
5. 验证：跑全部测试
6. 沉淀：记录到 diagnosis.md
```

### 7.4 测试的"重试"策略

unit-test-write 对"非确定性测试"有一个处理策略：**不盲目重试，而是标记并分析。**

- 首次失败：标记为"疑似非确定性"
- 再次失败：进入诊断流程
- 连续失败：阻断 CI，必须修复

这个"重试策略"的价值在于"不过度容忍"——如果测试总是"这次失败下次通过"，那说明测试本身有问题（或者代码有竞态），必须修，不能"重试掩盖"。

```
测试重试策略：
- 首次失败：标记"疑似非确定性"
- 再次失败：进入诊断
- 连续失败：阻断 CI，必须修复
```

### 7.5 测试的"环境隔离"

unit-test-write 强调测试的"环境隔离"——测试不应该依赖"外部环境"：

- 不依赖真实数据库（用内存数据库）
- 不依赖真实 LLM（用 Mock）
- 不依赖真实网络（用本地服务）
- 不依赖真实时间（用固定时钟）

这个"环境隔离"的价值在于"可重复"——测试在任何机器上、任何时候跑，结果都一样。

```
环境隔离要求：
- 数据库：内存数据库 / H2 / SQLite
- LLM：Mock / 固定响应
- 网络：本地服务 / WireMock
- 时间：固定时钟 / 注入 Clock
```

## 八、测试质量红线：什么不能做

### 8.1 四种禁止

unit-test-write 有明确的"测试质量红线"：

1. **禁止只测 happy path**：边界靠注释"应该能处理"是不行的
2. **禁止断言空泛**：assertNotNull 当主断言是不够的
3. **禁止测试间相互依赖**：有顺序耦合的测试是脆弱的
4. **禁止 Mock 自己写的类**：测的是 Mock 不是逻辑

### 8.2 三种必须

同时，unit-test-write 有"三种必须"：

1. **每条 AC 至少一个测试**
2. **每个边界情况至少一个测试**
3. **降级逻辑有专门测试**

这"四条禁止 + 三条必须"，是 unit-test-write 的"行为准则"——它定义了"什么测试是合格的"。

<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 220" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs><linearGradient id="bg_ut5" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient><filter id="sh_ut5"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter></defs>
  <rect width="800" height="220" fill="url(#bg_ut5)" rx="10"/>
  <text x="400" y="25" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">测试质量红线</text>
  <rect x="40" y="55" width="350" height="110" rx="8" fill="#ef4444" opacity="0.1" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_ut5)"/>
  <text x="215" y="78" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">禁止</text>
  <text x="215" y="100" text-anchor="middle" fill="#94a3b8" font-size="10">只测 happy path</text>
  <text x="215" y="118" text-anchor="middle" fill="#94a3b8" font-size="10">断言空泛</text>
  <text x="215" y="136" text-anchor="middle" fill="#94a3b8" font-size="10">测试相互依赖</text>
  <text x="215" y="154" text-anchor="middle" fill="#94a3b8" font-size="10">Mock 自己写的类</text>
  <rect x="410" y="55" width="350" height="110" rx="8" fill="#22c55e" opacity="0.1" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_ut5)"/>
  <text x="585" y="78" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">必须</text>
  <text x="585" y="100" text-anchor="middle" fill="#94a3b8" font-size="10">每条 AC 至少一个测试</text>
  <text x="585" y="118" text-anchor="middle" fill="#94a3b8" font-size="10">每个边界至少一个测试</text>
  <text x="585" y="136" text-anchor="middle" fill="#94a3b8" font-size="10">降级逻辑有专业测试</text>
</svg>


### 8.3 测试的"经济账"

unit-test-write 背后有一笔"经济账"：测试不是成本，是投资。

- 写一个测试的成本：几分钟
- 测试防止的线上事故成本：几小时到几天
- 测试带来的重构信心：无价

这笔账算清楚后，unit-test-write 的"覆盖优先"就很合理了——核心逻辑 80% 的覆盖率，换来的是一年 365 天里每一天的"重构安全感"。

### 8.4 测试的"反馈速度"

unit-test-write 还关注"反馈速度"——测试跑得越快，开发循环越短。

- 单元测试：毫秒级，每次保存都跑
- 集成测试：秒级，每次提交都跑
- 端到端测试：分钟级，每次发版都跑

这个"分级反馈"的思想，是 unit-test-write 从 CI 中吸收的：快反馈在前，慢反馈在后，让开发者"尽早知道坏消息"。

<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 220" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_ut8" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_ut8"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="220" fill="url(#bg_ut8)" rx="10"/>
  <text x="400" y="25" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">测试反馈速度金字塔</text>
  <rect x="350" y="55" width="100" height="35" rx="6" fill="#22c55e" opacity="0.2" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_ut8)"/>
  <text x="400" y="77" text-anchor="middle" fill="#86efac" font-size="11" font-weight="700">单元测试</text>
  <text x="400" y="95" text-anchor="middle" fill="#94a3b8" font-size="9">毫秒级</text>
  <rect x="280" y="105" width="240" height="35" rx="6" fill="#f59e0b" opacity="0.2" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_ut8)"/>
  <text x="400" y="127" text-anchor="middle" fill="#fde68a" font-size="11" font-weight="700">集成测试</text>
  <text x="400" y="145" text-anchor="middle" fill="#94a3b8" font-size="9">秒级</text>
  <rect x="200" y="155" width="400" height="35" rx="6" fill="#ef4444" opacity="0.2" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_ut8)"/>
  <text x="400" y="177" text-anchor="middle" fill="#fca5a5" font-size="11" font-weight="700">端到端测试</text>
  <text x="400" y="195" text-anchor="middle" fill="#94a3b8" font-size="9">分钟级</text>
</svg>

### 8.5 测试的"文化"价值

unit-test-write 最后想说：测试是一种"文化"，不是一种"任务"。

在 huazai-trip-plan 的实践中，团队从"写完代码再补测试"到"先写测试再写代码"，是一种文化转变。TDD 不是"测试先行"，而是"思考先行"——先想清楚"什么行为是对的"，再动手写代码。

unit-test-write 是这个文化的"载体"——它把"测试文化"沉淀成"可执行的命令"，让每个加入团队的人都能快速进入状态。


### 8.6 测试的"团队"价值

测试矩阵不只是"个人工具"，也是"团队工具"。

当团队使用 unit-test-write 时，测试矩阵成为"共识"的载体：每个测试点代表一个"验收标准"，每个测试用例代表一个"质量承诺"。新成员加入团队时，看测试矩阵就知道"项目对质量的要求是什么"。

这个"团队"价值的核心是：**质量不是某个人的责任，而是团队的共识。**

### 8.7 测试的"持续改进"循环

unit-test-write 的最后一个设计，是"持续改进"循环：

1. 写测试 -> 跑测试 -> 绿了
2. 绿了 -> 发现缺了某个边界 -> 补测试点
3. 补测试点 -> 写测试 -> 绿了
4. 绿了 -> 发现覆盖率不够 -> 补测试
5. 补测试 -> 绿了 -> 进入评审

这个"持续改进"循环，让测试覆盖率"螺旋上升"——每次迭代，测试都"更完整一点"。

### 8.8 测试的"投资回报率"

unit-test-write 最后算一笔账：测试的"投资回报率"。

假设一个功能有 5 个 AC、3 个边界、2 条降级路径。写测试花 2 小时，但测试防止了 1 次线上事故（平均修复 4 小时、影响 100 个用户）。那么 ROI 就是 2 小时 vs 4 小时，400% 回报。

这个"投资回报率"的视角，把测试从"成本"变成"投资"——测试不是"写完了就没了"，而是"持续产生回报"。

## 九、实战案例：从需求到测试的完整旅程

### 9.1 案例背景

假设需求是"预算超额时触发人工干预（HITL）"。


### 9.2 测试矩阵的完整工作流

unit-test-write 的完整工作流，是一个从"需求卡片到测试通过"的闭环：

1. 读取 change.md 提取 AC、边界、降级路径，形成测试点清单
2. 生成测试矩阵，把测试点映射为具体的测试用例
3. 分层编写：Spec、Boundary、Fallback、Regression 四层逐层编写
4. 运行测试，定位失败用例
5. 核验覆盖率，确认核心逻辑 >=80%
6. 出口门禁：全绿达标 -> 更新状态，进入评审

这个工作流的关键，是每一步都有明确的产出——测试点清单、测试矩阵、绿了的测试、达标的覆盖率。每一步都能被验证，没有模糊地带。

<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 820 320" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_ut6" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_ut6"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="820" height="320" fill="url(#bg_ut6)" rx="10"/>
  <text x="410" y="25" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">unit-test-write 完整工作流</text>
  <rect x="40" y="55" width="150" height="55" rx="8" fill="#3b82f6" opacity="0.15" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_ut6)"/>
  <text x="115" y="77" text-anchor="middle" fill="#93c5fd" font-size="11" font-weight="700">读 change.md</text>
  <text x="115" y="95" text-anchor="middle" fill="#94a3b8" font-size="9">提取测试点</text>
  <line x1="190" y1="82" x2="230" y2="82" stroke="#475569" stroke-width="2"/>
  <polygon points="235,82 227,77 227,87" fill="#94a3b8"/>
  <rect x="240" y="55" width="150" height="55" rx="8" fill="#22c55e" opacity="0.15" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_ut6)"/>
  <text x="315" y="77" text-anchor="middle" fill="#86efac" font-size="11" font-weight="700">生成测试矩阵</text>
  <text x="315" y="95" text-anchor="middle" fill="#94a3b8" font-size="9">1:1 映射</text>
  <line x1="390" y1="82" x2="430" y2="82" stroke="#475569" stroke-width="2"/>
  <polygon points="435,82 427,77 427,87" fill="#94a3b8"/>
  <rect x="440" y="55" width="150" height="55" rx="8" fill="#f59e0b" opacity="0.15" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_ut6)"/>
  <text x="515" y="77" text-anchor="middle" fill="#fde68a" font-size="11" font-weight="700">分层编写测试</text>
  <text x="515" y="95" text-anchor="middle" fill="#94a3b8" font-size="9">四层逐一编写</text>
  <line x1="590" y1="82" x2="630" y2="82" stroke="#475569" stroke-width="2"/>
  <polygon points="635,82 427,77 427,87" fill="#94a3b8"/>
  <rect x="640" y="55" width="150" height="55" rx="8" fill="#a855f7" opacity="0.15" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_ut6)"/>
  <text x="715" y="77" text-anchor="middle" fill="#d8b4fe" font-size="11" font-weight="700">运行测试</text>
  <text x="715" y="95" text-anchor="middle" fill="#94a3b8" font-size="9">定位失败</text>
  <line x1="430" y1="110" x2="430" y2="150" stroke="#475569" stroke-width="2"/>
  <polygon points="430,155 425,147 435,147" fill="#94a3b8"/>
  <rect x="340" y="160" width="180" height="55" rx="8" fill="#ef4444" opacity="0.12" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_ut6)"/>
  <text x="430" y="182" text-anchor="middle" fill="#fca5a5" font-size="11" font-weight="700">核验覆盖率</text>
  <text x="430" y="200" text-anchor="middle" fill="#94a3b8" font-size="9">COV_TOOL >=80%</text>
  <line x1="430" y1="215" x2="430" y2="255" stroke="#475569" stroke-width="2"/>
  <polygon points="430,260 425,252 435,252" fill="#94a3b8"/>
  <rect x="340" y="265" width="180" height="45" rx="8" fill="#22c55e" opacity="0.15" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_ut6)"/>
  <text x="430" y="290" text-anchor="middle" fill="#86efac" font-size="11" font-weight="700">出口门禁 -> reviewing</text>
</svg>

### 9.3 测试矩阵的自检表

unit-test-write 在编写的间隙，会运行一个自检表，检查测试是否完整：

- 每条 AC 是否有 Spec Test
- 边界情况是否有 Boundary Test
- 降级逻辑是否有 Fallback Test
- 已修复 bug 是否有 Regression Test
- 覆盖率是否 >=80%
- 是否有测试只测 happy path（禁止）
- 是否有空泛断言（禁止）
- 是否有测试间相互依赖（禁止）
- 是否 Mock 了自己写的类（禁止）

这个自检表的价值在于门禁前置——在进入 CI 之前，先自己检查一遍。它把质量红线从事后纠错变成事中自查。

### 9.2 提取测试点

从 change.md 提取测试点：

- **AC1**：预算超额超过 15% 时，触发 HITL
- **AC2**：预算未超额时，不触发 HITL
- **边界**：预算正好 15% 时，是否触发
- **降级**：地图 API 不可用时，返回离线估算

### 9.3 分层测试

Spec Tests：
- should_trigger_HITL_when_budget_overrun_exceeds_15_percent
- should_not_trigger_HITL_when_budget_under_limit

Boundary Tests：
- should_not_trigger_HITL_when_overrun_equals_15_percent（边界精确值）

Fallback Tests：
- should_return_offline_estimate_when_map_api_unavailable

### 9.4 编写测试

```
@Test
void should_trigger_HITL_when_budget_overrun_exceeds_15_percent() {
    // Arrange
    TripPlan plan = planWithCost(11500); // 超支 15%
    // Act
    BudgetCheckResult result = budgetAgent.check(plan);
    // Assert
    assertThat(result.requiresHumanIntervention()).isTrue();
}

@Test
void should_return_offline_estimate_when_map_api_unavailable() {
    // Arrange: 地图 API 抛超时
    when(baiduMapTool.distance(any(), any())).thenThrow(new TimeoutException());
    // Act: 调用路线规划
    RouteResult result = routeService.plan(req);
    // Assert: 走降级路径
    assertThat(result.isFallback()).isTrue();
}
```

### 9.5 覆盖率核验

跑覆盖率工具，核心逻辑覆盖率约 85%，达标。


### 9.7 测试的"完整度"评估

unit-test-write 在完成测试后，会做一个"完整度评估"：

```
测试完整度评估：
[OK] Spec Tests：2/2 条 AC 已覆盖
[OK] Boundary Tests：2/2 个边界已覆盖
[OK] Fallback Tests：1/1 条降级路径已覆盖
[OK] 覆盖率：85%（>=80%）
[OK] 质量红线：无违规
=> 评估结果：通过，进入评审
```

这个"完整度评估"的价值在于"透明"——测试完整不完整，不是"感觉"，而是"清单"。

### 9.8 测试的"重构"挑战

单元测试在"重构"场景下，会遇到一个挑战：**重构时，测试也要改。**

unit-test-write 的处理方式是：**重构不改测试逻辑，只改测试实现。** 即：重构后，测试的"期望结果"不变，只是"调用方式"变了。

```
// 重构前：直接调用 service
@Test
void should_create_order() {
    orderService.create(order);
}

// 重构后：通过 rest 接口调用
// 测试期望结果不变，只是调用方式变了
```

这个"逻辑不变"的原则，让测试在重构时"改得少、改得准"。

### 9.9 测试的"回归"守护

unit-test-write 的最后一个实践是"回归守护"——每次修复一个 bug，都写一个 Regression Test，防止"修好了又坏"。

回归测试的命名规范：test_<bug_id>_<描述>。如 test_47_should_not_trigger_HITL_when_overrun_equals_15_percent。

回归测试的价值在于"历史不重演"——一个 bug 修复后，回归测试就是"永久守护"：以后每次重构、每次改代码，回归测试都会自动验证"这个 bug 没有复发"。

```
// 回归测试示例：bug #47
@Test
void test_47_should_not_trigger_HITL_when_overrun_equals_15_percent() {
    // 边界值：超支正好 15%，不触发 HITL
    TripPlan plan = planWithCost(11500);
    BudgetCheckResult result = budgetAgent.check(plan);
    // 期望：不触发人工干预
    assertThat(result.requiresHumanIntervention()).isFalse();
}
```

### 9.6 出口门禁

测试全绿 + 覆盖率达标 -> 更新 change.md 状态 testing -> reviewing，进入专家评审。


### 9.10 测试的"全链路"视角

unit-test-write 的测试矩阵，最终要回答一个"全链路"问题：**这个需求从输入到输出，整条链路是否都被覆盖？**

- 输入层：参数校验、格式转换
- 逻辑层：业务规则、状态流转
- 输出层：结果组装、错误处理
- 依赖层：外部服务、LLM 调用
- 降级层：超时回退、异常处理

这个"全链路"视角的价值在于"无死角"——不是"测了核心逻辑就行"，而是"整条链路都测了"。

```
全链路覆盖检查：
[OK] 输入层：参数校验已覆盖
[OK] 逻辑层：业务规则已覆盖
[OK] 输出层：结果组装已覆盖
[OK] 依赖层：外部服务已 Mock
[OK] 降级层：超时回退已覆盖
=> 全链路无死角
```

### 9.11 测试的"编码风格"一致

unit-test-write 的测试代码，遵循和实现代码一样的编码规范：

- 命名规范：should_xxx_when_yyy
- 缩进规范：与实现代码一致
- 注释规范：关键逻辑有注释
- 格式规范：通过 {{FORMAT_CHECK_CMD}} 检查

这个"风格一致"的价值在于"可读"——测试代码和实现代码放在一起，看起来像"同一个团队写的"。

### 9.12 测试的"文档化"交付

unit-test-write 的最后一个交付物，是"测试文档"——不是"测试报告"，而是"测试说明"：

```
测试说明：
- 测试范围：预算超额 HITL 功能
- 测试矩阵：4 个测试点，8 个测试用例
- 覆盖率：85%（核心 92%）
- 运行方式：{{TEST_CMD}}
- 特殊说明：地图 API 已 Mock，降级路径已覆盖
```

这个"文档化"的价值在于"交接"——下一个开发者接手这个功能时，看测试说明就知道"测了什么、怎么跑、有什么坑"。

## 十、总结与展望

### 10.0 unit-test-write 的哲学总结

unit-test-write 的核心哲学，可以浓缩为三句话：

1. 测试从需求来，不是从代码来。
2. 降级必须实测，不满足于应该能处理。
3. 覆盖率是承诺，不是数字。

这三句话，是 unit-test-write 的灵魂——它定义了什么测试是好的。




### 6.8 覆盖率与"测试粒度"的平衡

unit-test-write 发现一个"悖论"：**覆盖率越高，测试粒度越细，维护成本越高。**

- 覆盖率 60%：核心逻辑测了，维护成本低
- 覆盖率 80%：核心 + 边界测了，维护成本适中
- 覆盖率 95%：几乎所有分支都测了，维护成本高

unit-test-write 的答案是"80% 是甜点"——它平衡了"覆盖深度"和"维护成本"。低于 80%，核心逻辑可能有漏洞；高于 80%，测试的边际价值下降，维护负担上升。

这个"甜点"不是拍脑袋，而是实践的经验值：80% 的覆盖率，既能"发现大多数 bug"，又不至于"为了覆盖而写一堆脆弱的测试"。

### 9.13 测试的"多语言"适配

unit-test-write 的一个亮点，是"多语言适配"——同样的测试矩阵，在不同语言中落地方式不同：

- **Java**：JUnit + Mockito + JaCoCo
- **Python**：pytest + unittest.mock + pytest-cov
- **Go**：go test + testify + go test -cover
- **前端**：Vitest + Jest + @testing-library

这个"多语言适配"的价值在于"心智统一"——开发者只学一次"测试矩阵"的思想，就能在不同语言中应用。语言不同，但"从需求到测试的 1:1 映射"是一致的。

```
不同语言的测试矩阵落地：
- Java: @Test + when().thenThrow() + assertThat()
- Python: @pytest.mark + monkeypatch + pytest.raises
- Go: func Test + mock.Mock + assert.True()
- 前端: it() + jest.fn() + expect().toBe()
```

### 9.14 测试的"边界"设计思维

unit-test-write 强调"边界设计思维"——测试边界，不只是"测试边界值"，而是"设计边界"。

好的代码，会主动"暴露边界"：参数校验、范围检查、状态机约束。坏的代码，边界"藏"在逻辑里，测试很难触达。

unit-test-write 的规则是：**如果代码的边界很难测，说明代码设计有问题。** 好的设计，天然可测——边界是"显式的"，不是"隐藏的"。

```
// 边界设计好：参数校验先行
TripPlan planFor(int budget) {
    if (budget < 0) throw new IllegalArgumentException();
    if (budget > 100000) throw new IllegalArgumentException();
    // 业务逻辑
}

// 边界藏起来：逻辑里嵌套检查（难测）
TripPlan planFor(int budget) {
    // 一大堆逻辑，边界藏在中间
}
```


### 9.16 测试的"交付"质量

unit-test-write 的测试交付，不只是"测试代码"，还有"测试说明"。

测试说明回答了三个问题：测了什么（测试范围）、怎么测的（测试方法）、结果如何（测试结果）。这三个问题，是"测试交付"的核心——它让下一个开发者能在 5 分钟内理解"这个测试做了什么"。

unit-test-write 的规则是：**测试说明和测试代码一样重要。** 测试代码是"可执行的"，测试说明是"可理解的"。两者缺一不可。

### 9.15 测试的"演进"路径

unit-test-write 总结了测试的"演进"路径：

1. **第 1 阶段：无测试。** 写完代码，靠手动验证。
2. **第 2 阶段：happy path 测试。** 只测正常流程。
3. **第 3 阶段：分层测试。** Spec + Boundary + Fallback + Regression。
4. **第 4 阶段：矩阵驱动。** 从需求卡片自动生成测试矩阵。

unit-test-write 的目标，是把团队从"第 2 阶段"带到"第 4 阶段"——从"凭感觉写测试"到"按矩阵写测试"。

这个"演进"路径，是所有工程团队的质量成长之路。unit-test-write 让这个路径"可复制"——不是"靠经验"，而是"靠命令"。

### 10.0 unit-test-write 的"哲学"总结

unit-test-write 的核心哲学，可以浓缩为三句话：

1. **测试从需求来，不是从代码来。** 测试矩阵把需求映射成测试点，确保"每条需求都有测试"。
2. **降级必须实测，不满足于"应该能处理"。** AI 应用的核心风险在降级，降级没测等于"裸奔"。
4. **覆盖率是承诺，不是数字。** 80% 的覆盖率，承诺的是"核心逻辑测透了"。

这三句话，是 unit-test-write 的"灵魂"——它定义了"什么测试是好的"。

### 10.1 unit-test-write 的设计哲学

unit-test-write 的设计，体现了三个核心哲学：

1. **需求驱动**：测试从需求卡片来，不是从代码来
2. **分层覆盖**：Spec、Boundary、Fallback、Regression 四层各司其职
3. **降级必测**：降级逻辑必须实测，不满足于"应该能处理"

### 10.2 从测试到质量保障

unit-test-write 不只写测试，它构建质量保障体系——测试矩阵、分层覆盖、覆盖率承诺、质量红线，共同构成了需求的完整验证。


### 10.4 unit-test-write 的"不可替代性"

有人会问："AI 不是能自动生成测试吗？为什么还需要 unit-test-write？"

答案是：**AI 生成测试是"无脑覆盖"，unit-test-write 是"需求驱动"。** AI 生成测试的问题是"不知道哪些是重点"——它可能生成 100 个测试，但核心逻辑一个都没测到。而 unit-test-write 从需求卡片出发，先问"需求是什么"，再测"需求是否成立"。

这个"不可替代性"的本质是：unit-test-write 是"思考工具"，不是"生成工具"。

### 10.5 从"测试"到"工程纪律"

unit-test-write 是 Harness 工程纪律的"第三道防线"：

1. **第一道防线**：/harnessing —— 需求拷问，确保"需求是对的"
2. **第二道防线**：/coding-skill —— 编码小循环，确保"代码是对的"
3. **第三道防线**：/unit-test-write —— 测试矩阵，确保"测试是完整的"

三道防线，逐层递进：需求 -> 实现 -> 验证。unit-test-write 是"验证"的最后一环——它保证"需求里提到的每件事，都有测试验证"。

<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 200" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_ut10" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_ut10"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="200" fill="url(#bg_ut10)" rx="10"/>
  <text x="400" y="25" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">三道防线</text>
  <rect x="40" y="55" width="220" height="55" rx="8" fill="#3b82f6" opacity="0.15" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_ut10)"/>
  <text x="150" y="77" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">第一道：需求拷问</text>
  <text x="150" y="95" text-anchor="middle" fill="#94a3b8" font-size="10">/harnessing</text>
  <line x1="260" y1="82" x2="290" y2="82" stroke="#475569" stroke-width="2"/>
  <polygon points="295,82 287,77 287,87" fill="#94a3b8"/>
  <rect x="300" y="55" width="220" height="55" rx="8" fill="#f59e0b" opacity="0.15" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_ut10)"/>
  <text x="410" y="77" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">第二道：编码小循环</text>
  <text x="410" y="95" text-anchor="middle" fill="#94a3b8" font-size="10">/coding-skill</text>
  <line x1="520" y1="82" x2="550" y2="82" stroke="#475569" stroke-width="2"/>
  <polygon points="555,82 547,77 547,87" fill="#94a3b8"/>
  <rect x="560" y="55" width="200" height="55" rx="8" fill="#22c55e" opacity="0.15" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_ut10)"/>
  <text x="660" y="77" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">第三道：测试矩阵</text>
  <text x="660" y="95" text-anchor="middle" fill="#94a3b8" font-size="10">/unit-test-write</text>
  <text x="400" y="170" text-anchor="middle" fill="#475569" font-size="11">需求 -> 实现 -> 验证：逐层递进，每一步都不可跳过</text>
</svg>

### 10.6 最后的话

/unit-test-write 不是终点，而是起点。它代表了 AI 编码的"工程化"方向——让 AI 从"写代码"升级到"做工程"。

当我们把测试从"写代码的附属品"变成"需求驱动的系统工程"，测试就不再是"负担"，而是"质量保障"。下一站，/expert-reviewer。


### 10.7 为什么是"测试矩阵"而不是"测试清单"

有人会问：为什么不叫"测试清单"，而叫"测试矩阵"？

因为"清单"是"一维"的——列出一堆测试点。而"矩阵"是"二维"的——横轴是测试点（AC、边界、降级、回归），纵轴是测试层（Spec、Boundary、Fallback、Regression）。每个"测试点"和"测试层"的交叉点，就是一个"测试用例"。

矩阵的价值在于"交叉检查"：AC 有没有对应的 Spec Test？边界有没有对应的 Boundary Test？降级有没有对应的 Fallback Test？矩阵的"格子"一眼可见，漏了哪个格子，一目了然。

```
测试矩阵（示意）：
          Spec  |  Boundary  |  Fallback  |  Regression
AC1        [x]   |    [x]     |    [-]     |    [-]
AC2        [x]   |    [x]     |    [-]     |    [x]
边界15%    [-]   |    [x]     |    [-]     |    [-]
降级API    [-]   |    [-]     |    [x]     |    [-]
```

看到矩阵，就知道"测试覆盖的全貌"——这是"清单"做不到的。

### 10.8 从"写测试"到"测试思维"

unit-test-write 的终极目标，不是"写更多测试"，而是"建立测试思维"。

测试思维是什么？是"写代码时就想'这个怎么测'"；是"改代码时就想'这会不会破坏什么'"；是"设计接口时就想'这个边界是什么'"。

unit-test-write 把"测试思维"沉淀成"可执行的命令"——你不需要"记住"测试原则，你只需要"运行"unit-test-write，它会按测试思维工作。

这，就是 Harness 的哲学：**把工程纪律，沉淀成可执行的命令。**

### 10.9 下篇预告

下一篇，我们将深入拆解 /expert-reviewer 命令，看它是如何从"规范"和"需求"两个维度，对代码进行双轴审查的。

### 10.3 下篇预告

下一篇，我们将深入拆解 /expert-reviewer 命令，看它是如何从规范和需求两个维度，对代码进行双轴审查的。

---

*本文是 Harness 专栏系列命令深度拆解的第 3 篇。下一篇：[双轴审查：/expert-reviewer 的严格代码审查流程](./column-07-expert-reviewer.md)*