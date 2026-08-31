# 双轴审查：/expert-reviewer 的严格代码审查流程

> 命令深度拆解 · 第四篇 · 约 10000 字 · 10 个 SVG 图

## 一、/expert-reviewer 不是什么

在 Harness 流水线中，/expert-reviewer 是第四个命令——在 /unit-test-write 完成测试补全之后，进入专家评审阶段。它负责对代码进行双轴并行审查：Spec 轴检查是否做对了事，Standards 轴检查是否合规做事。

但很多人对它也有误解：

**它不是代码审查工具。** 传统的代码审查工具（SonarQube、CodeClimate）只检查代码质量——圈复杂度、重复代码、未使用变量。但 expert-reviewer 不只看代码质量，它看需求是否被实现——这是功能审查和质量审查的区别。

**它不是 QA 替代品。** QA 做的是端到端验证——用户故事、场景测试、集成测试。expert-reviewer 做的是代码级审查——代码是否满足需求、是否符合规范。两者职责不同，互相补充。

**它不是自动批准。** 0 个严重问题不等于自动批准。expert-reviewer 的审查报告是建议，最终决策权在开发者手里。

### 1.1 从 huazai-trip-plan 的评审焦虑说起

在 huazai-trip-plan 的实践中，代码评审有一个焦虑：**评审不够系统。**

传统评审的问题是：评审的质量取决于评审人的经验。经验丰富的评审人，能发现需求没实现的问题；经验不足的评审人，只能看命名规范。评审结果不统一，每次评审的深度不一样。

expert-reviewer 的诞生，就是要解决这个焦虑——它把评审变成系统化的：Spec 轴负责需求匹配，Standards 轴负责规范合规。两轴独立运行，互不干扰，最终汇总报告。

### 1.2 /expert-reviewer 的输入-输出契约

/expert-reviewer 的输入输出契约非常清晰：

- **输入**：代码 + 单元测试 + change.md + .harness/rules/
- **输出**：.harness/changes/<id>/review.md 审查报告（**无论结果如何，都必须写入**）
- **出口门禁**：0 个严重问题方可放行

执行前先**锁定审查范围**：使用 `git diff HEAD`（含工作区未提交的变更）作为审查对象，在报告开头标注文件数、增减行数、分支名；若 `git diff HEAD` 为空则报错退回，绝不在含糊的"变更范围"上审查。

无论审查结果如何，**都必须先把完整报告写入 review.md**，再根据结果分支：
- 有严重问题 -> 退回编码实现修复（review.md 作为修复参考依据）
- 0 个严重问题 -> 更新 change.md 状态 reviewing -> ci，进入 CI 门禁


<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 200" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs><linearGradient id="bg_er1" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient><filter id="sh_er1"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter></defs>
  <rect width="800" height="200" fill="url(#bg_er1)" rx="10"/>
  <text x="400" y="25" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">/expert-reviewer 的输入-输出契约</text>
  <rect x="40" y="50" width="220" height="55" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_er1)"/>
  <text x="150" y="72" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">输入</text>
  <text x="150" y="90" text-anchor="middle" fill="#94a3b8" font-size="10">代码 + 测试 + change.md</text>
  <line x1="260" y1="77" x2="290" y2="77" stroke="#475569" stroke-width="2"/><polygon points="295,77 287,72 287,82" fill="#94a3b8"/>
  <rect x="300" y="50" width="220" height="55" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_er1)"/>
  <text x="410" y="72" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">双轴审查</text>
  <text x="410" y="90" text-anchor="middle" fill="#94a3b8" font-size="10">Spec 轴 + Standards 轴</text>
  <line x1="520" y1="77" x2="550" y2="77" stroke="#475569" stroke-width="2"/><polygon points="555,77 547,72 547,82" fill="#94a3b8"/>
  <rect x="560" y="50" width="200" height="55" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_er1)"/>
  <text x="660" y="72" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">输出</text>
  <text x="660" y="90" text-anchor="middle" fill="#94a3b8" font-size="10">review.md 审查报告</text>
  <text x="400" y="160" text-anchor="middle" fill="#475569" font-size="11">出口门禁：0 个严重问题方可放行</text>
  <text x="400" y="180" text-anchor="middle" fill="#64748b" font-size="10">有严重问题 -> 退回编码修复</text>
</svg>

## 二、双轴并行架构：为什么是两轴而不是一轴

### 2.1 传统审查的单轴陷阱

传统代码审查，通常只有一个轴：评审人看代码是否符合规范。但符合规范不等于做对了事——代码可能完全符合规范，但实现了错误的功能。

expert-reviewer 的双轴架构，就是为了解决这个单轴陷阱：两个轴独立运行，独立报告，防止一个轴掩盖另一个轴。

### 2.2 两轴独立的意义

两轴独立的意义在于：

- 代码完全合规范但实现了错的东西 -> Standards 过 Spec 挂
- 代码照做了需求但破坏了约定 -> Spec 过 Standards 挂

这个独立的价值在于不掩盖——如果只有一个轴，要么规范掩盖需求（代码规范但功能错了），要么需求掩盖规范（功能对了但代码质量差）。

### 2.3 双轴并行子智能体架构

expert-reviewer 使用双轴并行子智能体架构：Spec 轴和 Standards 轴由独立子智能体运行，最终汇总报告。两轴之间不合并、不排序，独立报告。


<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 260" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs><linearGradient id="bg_er2" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient><filter id="sh_er2"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter></defs>
  <rect width="800" height="260" fill="url(#bg_er2)" rx="10"/>
  <text x="400" y="25" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">双轴并行子智能体架构</text>
  <rect x="100" y="55" width="250" height="90" rx="8" fill="#3b82f6" opacity="0.15" stroke="#3b82f6" stroke-width="2" filter="url(#sh_er2)"/>
  <text x="225" y="80" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">Spec 轴</text>
  <text x="225" y="100" text-anchor="middle" fill="#94a3b8" font-size="10">对照 change.md 检查需求匹配</text>
  <text x="225" y="118" text-anchor="middle" fill="#94a3b8" font-size="10">功能完整性 + SDD 合规</text>
  <rect x="450" y="55" width="250" height="90" rx="8" fill="#f59e0b" opacity="0.15" stroke="#f59e0b" stroke-width="2" filter="url(#sh_er2)"/>
  <text x="575" y="80" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">Standards 轴</text>
  <text x="575" y="100" text-anchor="middle" fill="#94a3b8" font-size="10">对照 .harness/rules/ 检查合规</text>
  <text x="575" y="118" text-anchor="middle" fill="#94a3b8" font-size="10">架构 + 编码 + 代码质量 + 安全</text>
  <line x1="350" y1="100" x2="450" y2="100" stroke="#475569" stroke-width="2"/><text x="400" y="95" text-anchor="middle" fill="#64748b" font-size="10">并行</text>
  <rect x="225" y="175" width="350" height="55" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_er2)"/>
  <text x="400" y="197" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">汇总报告</text>
  <text x="400" y="215" text-anchor="middle" fill="#94a3b8" font-size="10">两轴独立报告，不合并、不排序</text>
  <line x1="225" y1="145" x2="225" y2="175" stroke="#3b82f6" stroke-width="2"/><line x1="575" y1="145" x2="575" y2="175" stroke="#f59e0b" stroke-width="2"/>
</svg>


## 三、Spec 轴：需求匹配

### 3.1 功能完整性（对照 change.md）

Spec 轴的第一维度是功能完整性——代码是否实现了 change.md 里所有的 AC？

- 实现了所有 AC？
- 处理了所有边界情况？
- 有 scope creep（未要求的代码/功能）？
- 实现与设计约束一致？

这个维度的价值在于需求对齐——代码写完了，但需求里的每件事都做了吗？

### 3.2 SDD 合规

Spec 轴的第二维度是 SDD 合规——change.md 是否足够构成规格真相源？

- change.md 是否足够构成规格真相源？
- 实现是否严格对齐 change.md，而非擅自扩 scope？

这个维度的价值在于范围控制——需求变了，改 change.md，而不是偷偷在代码里改。


### 3.3 Spec 轴审查的常见陷阱

Spec 轴审查，有一个常见的陷阱：**只看代码，不看需求。**

有些审查人，看到代码就习惯性地看代码质量——命名、结构、注释。但 Spec 轴的核心是需求匹配——代码是否实现了 change.md 里所有的 AC。如果只看代码质量，就漏掉了需求没实现这个最重要的问题。

expert-reviewer 的 Spec 轴，强制审查人先看 change.md，再看代码——先建立需求的真相源，再对照代码检查。这个顺序，是 Spec 轴审查的关键。

### 3.4 Spec 轴审查的四问

Spec 轴审查，可以浓缩为四个问题：

1. **做了吗**：所有 AC 都实现了吗？
2. **做对了吗**：实现与需求一致吗？
3. **多做吗**：有没有 scope creep？
4. **做全吗**：边界情况都处理了吗？

这四个问题，是 Spec 轴审查的灵魂——它确保审查人从需求出发，而不是从代码出发。

<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 260" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs><linearGradient id="bg_er7" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient><filter id="sh_er7"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter></defs>
  <rect width="800" height="260" fill="url(#bg_er7)" rx="10"/>
  <text x="400" y="25" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">审查维度的优先级金字塔</text>
  <polygon points="400,50 320,90 480,90" fill="#ef4444" opacity="0.25" stroke="#ef4444" stroke-width="1.5"/>
  <text x="400" y="82" text-anchor="middle" fill="#fca5a5" font-size="11" font-weight="700">严重（必须修复）</text>
  <polygon points="400,90 270,150 530,150" fill="#f59e0b" opacity="0.2" stroke="#f59e0b" stroke-width="1.5"/>
  <text x="400" y="135" text-anchor="middle" fill="#fde68a" font-size="11" font-weight="700">建议（推荐修复）</text>
  <polygon points="400,150 200,230 600,230" fill="#22c55e" opacity="0.15" stroke="#22c55e" stroke-width="1.5"/>
  <text x="400" y="205" text-anchor="middle" fill="#86efac" font-size="11" font-weight="700">通过（无问题）</text>
  <text x="400" y="250" text-anchor="middle" fill="#64748b" font-size="10">严重问题优先处理，建议改进排期处理，通过项持续保持</text>
</svg>


### 3.5 Spec 轴与 SDD 的深度关系

Spec 轴审查与 SDD（Spec-driven Development）有深度关系。SDD 的核心是 change.md 是规格真相源——代码是对 change.md 的实现，测试是对 change.md 的验证，审查是对 change.md 的核对。

Spec 轴审查，就是 SDD 的最后一公里——change.md 定义了需求，coding-skill 实现了需求，unit-test-write 验证了需求，expert-reviewer 核对实现是否与需求一致。

这个核对的价值在于完整性——每个环节都在对答案，确保需求-实现-测试-审查的闭环。

## 四、Standards 轴：规范合规

### 4.1 架构合规（对照工程结构.md）

Standards 轴的第一个维度是架构合规：

- 模块间仅通过接口通信？
- 依赖方向正确（common <- service <- server）？
- 新架构决策是否补了 ADR？

这个维度的价值在于架构不腐化——每次提交都检查架构，防止架构偏离从一个小的依赖倒置开始。

### 4.2 编码规范（对照编码规范.md）

Standards 轴的第二个维度是编码规范：

- 命名、Javadoc 合规？
- 无硬编码密钥 / 魔法值？
- LLM/外部调用有超时+重试+限频+降级？
- 异常处理完整（不吞、不空 catch）？

这个维度的价值在于代码可读——编码规范不是文风，而是契约。

### 4.3 代码质量

Standards 轴的第三个维度是代码质量：

- 方法 <=50 行 / 文件 <=500 行 / 圈复杂度 <=10？
- 无未使用 import/变量/方法？
- 无重复代码块？
- 日志级别合适？

这个维度的价值在于代码健康——代码质量是可维护性的基础。


### 4.5 Standards 轴审查的"边界"

Standards 轴审查，也有边界。它不审查：

- 业务逻辑是否正确（那是 Spec 轴的事）
- 性能是否达标（那是 CI 门禁的事）
- 部署是否顺利（那是 deploy-verify 的事）

这个边界，是 Standards 轴审查的"职责范围"——它只负责"规范合规"，不负责"功能正确"和"非功能需求"。

### 4.6 Standards 轴审查的"自检"

Standards 轴审查，在正式审查之前，会先做"自检"：

- 仓库的 .harness/rules/ 是否存在？
- 工程结构.md 是否定义了架构约束？
- 编码规范.md 是否定义了编码规范？
- 安全规范.md 是否定义了安全要求？

如果这些规范文件不存在，Standards 轴审查就无法进行。这个自检，确保了审查的"前提条件"——没有规范，就没有合规审查。

### 4.4 安全

Standards 轴的第四个维度是安全：

- 外部输入已校验？
- 日志无敏感信息（Token/密码/隐私）？

这个维度的价值在于安全底线——安全漏洞往往从一个未校验的输入开始。


<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 280" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs><linearGradient id="bg_er3" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient><filter id="sh_er3"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter></defs>
  <rect width="800" height="280" fill="url(#bg_er3)" rx="10"/>
  <text x="400" y="25" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">10 个审查维度</text>
  <rect x="40" y="50" width="220" height="55" rx="8" fill="#3b82f6" opacity="0.15" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_er3)"/>
  <text x="150" y="72" text-anchor="middle" fill="#93c5fd" font-size="11" font-weight="700">Spec 轴</text>
  <text x="150" y="90" text-anchor="middle" fill="#94a3b8" font-size="9">功能完整性 + SDD 合规</text>
  <rect x="290" y="50" width="220" height="55" rx="8" fill="#f59e0b" opacity="0.15" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_er3)"/>
  <text x="400" y="72" text-anchor="middle" fill="#fde68a" font-size="11" font-weight="700">Standards 轴</text>
  <text x="400" y="90" text-anchor="middle" fill="#94a3b8" font-size="9">架构 + 编码 + 质量 + 安全</text>
  <rect x="540" y="50" width="220" height="55" rx="8" fill="#a855f7" opacity="0.15" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_er3)"/>
  <text x="650" y="72" text-anchor="middle" fill="#d8b4fe" font-size="11" font-weight="700">公共维度</text>
  <text x="650" y="90" text-anchor="middle" fill="#94a3b8" font-size="9">测试 + TDD + 流程 + 领域语言</text>
  <text x="400" y="130" text-anchor="middle" fill="#475569" font-size="11">10 个维度逐项检查，每项标记 严重/建议/通过</text>
  <rect x="40" y="175" width="720" height="85" rx="8" fill="#1e293b" opacity="0.5" stroke="#334155" stroke-width="1"/>
  <text x="400" y="198" text-anchor="middle" fill="#94a3b8" font-size="11">审查维度清单</text>
  <text x="400" y="218" text-anchor="middle" fill="#64748b" font-size="10">1.功能完整性 2.SDD合规 3.架构合规 4.编码规范 5.代码质量</text>
  <text x="400" y="238" text-anchor="middle" fill="#64748b" font-size="10">6.安全 7.测试质量 8.TDD合规 9.流程合规 10.领域语言一致性</text>
</svg>


## 五、代码味道基线：Fowler 味道的可能性判断

### 5.1 代码味道 vs 硬违规

expert-reviewer 引入了一个独特的设计：**代码味道基线**，源自 Fowler 的《Refactoring》第三章。

代码味道是可能性判断而非硬违规——有味道不代表代码错了，只是可能有问题。expert-reviewer 对味道审查有明确的规则：仓库规范优先于味道基线；味道是标签不是硬违规；工具已能查的跳过。

这个规则的价值在于务实——味道是诊断工具，不是必须修正的清单。

### 5.2 12 种代码味道

1. Mysterious Name：函数/变量名是否揭示意图？想不到好名字意味着设计不清
2. Duplicated Code：相同逻辑形状出现在多处 -> 提取共享部分
3. Feature Envy：方法取别人数据多于自己数据 -> 把方法移到它羡慕的数据上
4. Data Clumps：相同字段/参数总一起出现 -> 打包成新类型
5. Primitive Obsession：原始类型代表领域概念 -> 给概念一个专有类型
6. Repeated Switches：同一类型的 switch/if 链重复 -> 多态或映射表
7. Shotgun Surgery：一个逻辑改动散落多处 -> 聚集到同一模块
8. Divergent Change：一个文件因多种原因被改 -> 拆分为单一职责
9. Speculative Generality：为未来需求提前加的抽象 -> 删掉，直到真有需求
10. Message Chains：长链 a.b().c().d() -> 在第一个对象上隐藏路径
11. Middle Man：类/函数只是转发 -> 砍掉，直接调目标
12. Refused Bequest：子类大部分继承被重写/忽略 -> 放弃继承，用组合


<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 240" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs><linearGradient id="bg_er4" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient><filter id="sh_er4"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter></defs>
  <rect width="800" height="240" fill="url(#bg_er4)" rx="10"/>
  <text x="400" y="25" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">代码味道 vs 硬违规</text>
  <rect x="40" y="55" width="350" height="130" rx="8" fill="#f59e0b" opacity="0.1" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_er4)"/>
  <text x="215" y="78" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">代码味道（可能性判断）</text>
  <text x="215" y="100" text-anchor="middle" fill="#94a3b8" font-size="10">Mysterious Name / Duplicated Code</text>
  <text x="215" y="118" text-anchor="middle" fill="#94a3b8" font-size="10">Primitive Obsession / Shotgun Surgery</text>
  <text x="215" y="136" text-anchor="middle" fill="#94a3b8" font-size="10">Speculative Generality / Refused Bequest</text>
  <text x="215" y="154" text-anchor="middle" fill="#94a3b8" font-size="10">Message Chains / Middle Man</text>
  <text x="215" y="172" text-anchor="middle" fill="#94a3b8" font-size="10">Feature Envy / Data Clumps / Repeated Switches</text>
  <rect x="410" y="55" width="350" height="130" rx="8" fill="#ef4444" opacity="0.1" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_er4)"/>
  <text x="585" y="78" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">硬违规（必须修复）</text>
  <text x="585" y="100" text-anchor="middle" fill="#94a3b8" font-size="10">功能缺失（AC 未实现）</text>
  <text x="585" y="118" text-anchor="middle" fill="#94a3b8" font-size="10">安全漏洞（未校验输入）</text>
  <text x="585" y="136" text-anchor="middle" fill="#94a3b8" font-size="10">架构腐化（依赖倒置）</text>
  <text x="585" y="154" text-anchor="middle" fill="#94a3b8" font-size="10">硬编码密钥 / 空 catch</text>
  <text x="585" y="172" text-anchor="middle" fill="#94a3b8" font-size="10">降级逻辑缺失</text>
</svg>



### 5.3 味道审查的规则

expert-reviewer 对味道审查有明确的规则：

1. **仓库规范优先于味道基线**：仓库有自己的规范，味道基线只是参考
2. **味道是标签不是硬违规**：有味道不代表代码错了，只是可能有问题
3. **工具已能查的跳过**：重复代码、圈复杂度等，工具（SonarQube 等）已能查，审查人跳过

这个规则的价值在于务实——味道是诊断工具，不是必须修正的清单。审查人用味道做可能性判断，再结合上下文做最终判断。

### 5.4 味道审查的检查点

每种味道，expert-reviewer 都有明确的检查点：

- Mysterious Name：函数/变量名是否揭示意图？想不到好名字意味着设计不清
- Duplicated Code：相同逻辑形状出现在多处 -> 提取共享部分
- Feature Envy：方法取别人数据多于自己数据 -> 把方法移到它羡慕的数据上
- Data Clumps：相同字段/参数总一起出现 -> 打包成新类型
- Primitive Obsession：原始类型代表领域概念 -> 给概念一个专有类型
- Repeated Switches：同一类型的 switch/if 链重复 -> 多态或映射表
- Shotgun Surgery：一个逻辑改动散落多处 -> 聚集到同一模块
- Divergent Change：一个文件因多种原因被改 -> 拆分为单一职责
- Speculative Generality：为未来需求提前加的抽象 -> 删掉，直到真有需求
- Message Chains：长链 a.b().c().d() -> 在第一个对象上隐藏路径
- Middle Man：类/函数只是转发 -> 砍掉，直接调目标
- Refused Bequest：子类大部分继承被重写/忽略 -> 放弃继承，用组合

这些检查点，让味道审查从感觉变成清单——审查人对照检查点，逐项判断。

### 5.5 味道审查的案例

假设有一段代码：

```java
public class BudgetAgent {
    private final Map<String, Object> budgetMap = new HashMap<>();
    // ...
}
```

审查人会判断：Map<String, Object> 是 Primitive Obsession——预算是领域概念，但用 Map 和 Object 表示，丢失了类型安全。建议引入 Budget 类型。

这个案例展示了味道审查的价值——它不只是发现问题，而是给出改进方向。

## 六、公共维度：跨越两轴的审查

### 6.1 测试质量

公共维度的第一项是测试质量：

- 覆盖所有 AC 与边界？
- 测了降级逻辑？
- 核心覆盖率 >=80%？
- Mock 合理（不 Mock 自己写的类）？

### 6.2 TDD 合规

公共维度的第二项是 TDD 合规：

- 核心业务逻辑是否体现测试先行？
- 测试是否覆盖 AC / 边界 / 降级路径？

### 6.3 流程合规

公共维度的第三项是流程合规：

- 变更范围与 change.md 一致？
- 无偷偷夹带的额外变更？

### 6.4 领域语言一致性

公共维度的第四项是领域语言一致性：

- 代码中的术语与 .harness/CONTEXT.md 一致？
- 新引入的领域概念是否已在 CONTEXT.md 中定义？

这个维度的价值在于语言统一——团队用同一套术语沟通，代码、文档、讨论都用同一套词。


<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 240" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs><linearGradient id="bg_er5" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient><filter id="sh_er5"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter></defs>
  <rect width="800" height="240" fill="url(#bg_er5)" rx="10"/>
  <text x="400" y="25" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">公共维度：跨越两轴的审查</text>
  <rect x="40" y="55" width="170" height="70" rx="8" fill="#22c55e" opacity="0.15" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_er5)"/>
  <text x="125" y="80" text-anchor="middle" fill="#86efac" font-size="11" font-weight="700">测试质量</text>
  <text x="125" y="100" text-anchor="middle" fill="#94a3b8" font-size="9">AC/边界/降级已覆盖</text>
  <rect x="230" y="55" width="170" height="70" rx="8" fill="#3b82f6" opacity="0.15" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_er5)"/>
  <text x="315" y="80" text-anchor="middle" fill="#93c5fd" font-size="11" font-weight="700">TDD 合规</text>
  <text x="315" y="100" text-anchor="middle" fill="#94a3b8" font-size="9">测试先行</text>
  <rect x="420" y="55" width="170" height="70" rx="8" fill="#f59e0b" opacity="0.15" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_er5)"/>
  <text x="505" y="80" text-anchor="middle" fill="#fde68a" font-size="11" font-weight="700">流程合规</text>
  <text x="505" y="100" text-anchor="middle" fill="#94a3b8" font-size="9">变更范围一致</text>
  <rect x="610" y="55" width="170" height="70" rx="8" fill="#a855f7" opacity="0.15" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_er5)"/>
  <text x="695" y="80" text-anchor="middle" fill="#d8b4fe" font-size="11" font-weight="700">领域语言</text>
  <text x="695" y="100" text-anchor="middle" fill="#94a3b8" font-size="9">术语一致性</text>
  <text x="400" y="190" text-anchor="middle" fill="#475569" font-size="11">公共维度跨越两轴，确保测试、TDD、流程、语言都达标</text>
</svg>



### 6.5 公共维度的跨越意义

公共维度跨越两轴，它的意义在于：**有些问题，不属于任何一轴，但必须被审查。**

测试质量、TDD 合规、流程合规、领域语言一致性——这些问题，既不是需求匹配（Spec 轴），也不是规范合规（Standards 轴），但它们是代码质量的重要组成部分。

expert-reviewer 的公共维度，就是覆盖这些两轴之外的问题——确保审查是完整的，没有遗漏。

### 6.6 领域语言一致性：被忽视的审查维度

领域语言一致性（DDD Ubiquitous Language），是最容易被忽视的审查维度。它检查：代码中的术语与 .harness/CONTEXT.md 一致吗？

在传统开发中，术语不一致是隐形杀手——代码里叫 budgetCheck，文档里叫 budgetAudit，讨论里叫 budgetReview。同一个概念，三个名字，团队沟通成本飙升。

expert-reviewer 的领域语言一致性审查，就是解决这个问题——它检查代码术语、文档术语、讨论术语是否一致，不一致就标记为建议改进。

## 七、审查报告格式：review.md

### 7.1 报告结构

expert-reviewer 的输出是一个 review.md 文件，结构清晰：

- **总览**：审查范围（`git diff HEAD` 的文件数、增减行数、分支名）、严重问题数、建议改进数、通过项
- **Spec 轴报告**：需求匹配相关的严重和建议
- **Standards 轴报告**：规范合规相关的严重和建议
- **测试质量**：测试相关的严重和建议
- **结论**：一句话结论

### 7.2 严重程度标准

expert-reviewer 的严重程度分三级：

- **严重**：功能异常 / 安全漏洞 / 架构腐化 —— 必须修复
- **建议**：不立即出错但影响可维护/性能 —— 推荐修复
- **通过**：无问题



<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 240" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs><linearGradient id="bg_er6" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient><filter id="sh_er6"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter></defs>
  <rect width="800" height="240" fill="url(#bg_er6)" rx="10"/>
  <text x="400" y="25" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">expert-reviewer 审查流程</text>
  <rect x="40" y="55" width="130" height="55" rx="8" fill="#3b82f6" opacity="0.15" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_er6)"/>
  <text x="105" y="77" text-anchor="middle" fill="#93c5fd" font-size="11" font-weight="700">读取 change.md</text>
  <text x="105" y="95" text-anchor="middle" fill="#94a3b8" font-size="9">提取 AC 清单</text>
  <line x1="170" y1="82" x2="210" y2="82" stroke="#475569" stroke-width="2"/><polygon points="215,82 207,77 207,87" fill="#94a3b8"/>
  <rect x="220" y="55" width="130" height="55" rx="8" fill="#22c55e" opacity="0.15" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_er6)"/>
  <text x="285" y="77" text-anchor="middle" fill="#86efac" font-size="11" font-weight="700">Spec 轴审查</text>
  <text x="285" y="95" text-anchor="middle" fill="#94a3b8" font-size="9">功能完整性</text>
  <line x1="350" y1="82" x2="390" y2="82" stroke="#475569" stroke-width="2"/><polygon points="395,82 387,77 387,87" fill="#94a3b8"/>
  <rect x="400" y="55" width="130" height="55" rx="8" fill="#f59e0b" opacity="0.15" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_er6)"/>
  <text x="465" y="77" text-anchor="middle" fill="#fde68a" font-size="11" font-weight="700">Standards 轴</text>
  <text x="465" y="95" text-anchor="middle" fill="#94a3b8" font-size="9">规范合规</text>
  <line x1="530" y1="82" x2="570" y2="82" stroke="#475569" stroke-width="2"/><polygon points="575,82 567,77 567,87" fill="#94a3b8"/>
  <rect x="580" y="55" width="180" height="55" rx="8" fill="#a855f7" opacity="0.15" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_er6)"/>
  <text x="670" y="77" text-anchor="middle" fill="#d8b4fe" font-size="11" font-weight="700">汇总报告</text>
  <text x="670" y="95" text-anchor="middle" fill="#94a3b8" font-size="9">review.md</text>
  <line x1="400" y1="110" x2="400" y2="150" stroke="#475569" stroke-width="2"/><polygon points="400,155 395,147 405,147" fill="#94a3b8"/>
  <rect x="280" y="160" width="240" height="55" rx="8" fill="#ef4444" opacity="0.12" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_er6)"/>
  <text x="400" y="182" text-anchor="middle" fill="#fca5a5" font-size="11" font-weight="700">有严重问题？</text>
  <text x="400" y="200" text-anchor="middle" fill="#94a3b8" font-size="9">0 个 -> CI / 有 -> 退回编码</text>
</svg>


### 7.4 审查流程的完整性

expert-reviewer 的审查流程，是一个从"锁定审查范围"到"生成 review.md"的完整闭环：

0. 前置检查：锁定审查范围（`git diff HEAD`，含未提交变更），为空则报错退回
1. 读取 change.md，提取 AC 清单
2. Spec 轴独立审查：功能完整性 + SDD 合规
3. Standards 轴独立审查：架构 + 编码 + 质量 + 安全
4. 公共维度审查：测试 + TDD + 流程 + 领域语言
5. 汇总报告，**无论结果如何都写入 review.md**（⛔ 强制规则）
6. 判断出口门禁：0 个严重问题 -> CI / 有 -> 退回编码（review.md 已落盘，作为修复依据）

这个流程的关键，是每一步都有明确的产出——审查范围、AC 清单、Spec 报告、Standards 报告、汇总报告。每一步都能被验证，没有模糊地带。

### 7.5 审查报告的"真相源"原则

expert-reviewer 有一个重要的原则：**审查报告是真相源。**

审查报告记录了每次审查的完整结果——什么查了、什么没查、什么有问题、什么没问题。这个报告的价值不在于"一次审查"，而在于"历史记录"——下次审查时，可以回头看"上次的审查结果"。

这个原则，让 expert-reviewer 的审查不是"一次性的"，而是"可追溯的"。

## 八、出口门禁：0 个严重问题的意义

### 8.1 为什么是 0 个严重问题

expert-reviewer 的出口门禁是 0 个严重问题。这个门禁的意义在于：**质量不是累积的，质量是底线的。**

有一个严重问题，代码就不能进入 CI。这个门禁的严格性，保证了质量底线——没有被忽视的严重问题。在传统开发中，严重问题往往因为"时间紧"被放行，导致问题积累到生产环境才被发现。expert-reviewer 的 0 个严重问题门禁，就是杜绝这种情况。

### 8.2 有严重问题 -> 退回，在源头修复

有严重问题，expert-reviewer 会退回编码实现修复。这个退回的意义在于：**问题在源头修复，而不是在后期排查。**

在编码阶段修复一个严重问题，成本是 1x；在 CI 阶段发现，成本是 10x；在生产环境发现，成本是 100x。expert-reviewer 的退回机制，就是把问题消灭在源头。

### 8.3 0 个严重问题 -> 进入 CI

0 个严重问题，expert-reviewer 更新 change.md 状态 reviewing -> ci，进入 CI 门禁。这个流转的意义在于：**流水线是连续的，每个阶段都有明确的产出和门禁。**


### 8.4 出口门禁的多米诺效应

0 个严重问题的出口门禁，有多米诺效应：

- expert-reviewer 门禁严格 -> 编码阶段更认真（因为知道会被审查）
- 编码阶段更认真 -> 严重问题更少
- 严重问题更少 -> CI 更顺畅
- CI 更顺畅 -> 发布更快

这个多米诺效应，是门禁的间接价值——它不只是检查，更是威慑。

### 8.5 门禁与信任的关系

expert-reviewer 的门禁，与信任有微妙的关系：

- 门禁严格，团队信任通过审查的代码——因为知道审查是认真的
- 门禁松散，团队不信任通过审查的代码——因为知道审查是走形式的

这个信任关系，是门禁的长期价值——门禁不是限制，而是信任的基础。

### 8.6 门禁的成本考量

门禁不是免费的，它有成本：

- 审查时间成本：每次审查需要时间
- 退回修复成本：严重问题退回，需要重新编码
- 团队心理成本：门禁严格，团队可能焦虑

但 expert-reviewer 认为：门禁的成本，远小于问题后期的成本。在编码阶段修复一个严重问题，成本是 1x；在生产环境发现，成本是 100x。门禁的成本，是最便宜的保险。

<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 240" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs><linearGradient id="bg_er8" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient><filter id="sh_er8"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter></defs>
  <rect width="800" height="240" fill="url(#bg_er8)" rx="10"/>
  <text x="400" y="25" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">审查报告的流转路径</text>
  <rect x="40" y="55" width="150" height="55" rx="8" fill="#3b82f6" opacity="0.15" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_er8)"/>
  <text x="115" y="77" text-anchor="middle" fill="#93c5fd" font-size="11" font-weight="700">review.md</text>
  <text x="115" y="95" text-anchor="middle" fill="#94a3b8" font-size="9">审查结果</text>
  <line x1="190" y1="82" x2="230" y2="82" stroke="#475569" stroke-width="2"/><polygon points="235,82 227,77 227,87" fill="#94a3b8"/>
  <rect x="240" y="55" width="150" height="55" rx="8" fill="#f59e0b" opacity="0.15" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_er8)"/>
  <text x="315" y="77" text-anchor="middle" fill="#fde68a" font-size="11" font-weight="700">有严重问题？</text>
  <text x="315" y="95" text-anchor="middle" fill="#94a3b8" font-size="9">判断门禁</text>
  <line x1="390" y1="82" x2="430" y2="82" stroke="#475569" stroke-width="2"/><polygon points="435,82 427,77 427,87" fill="#94a3b8"/>
  <rect x="440" y="55" width="150" height="55" rx="8" fill="#ef4444" opacity="0.15" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_er8)"/>
  <text x="515" y="77" text-anchor="middle" fill="#fca5a5" font-size="11" font-weight="700">退回编码</text>
  <text x="515" y="95" text-anchor="middle" fill="#94a3b8" font-size="9">修复严重问题</text>
  <line x1="590" y1="82" x2="630" y2="82" stroke="#475569" stroke-width="2"/><polygon points="635,82 627,77 627,87" fill="#94a3b8"/>
  <rect x="640" y="55" width="130" height="55" rx="8" fill="#22c55e" opacity="0.15" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_er8)"/>
  <text x="705" y="77" text-anchor="middle" fill="#86efac" font-size="11" font-weight="700">进入 CI</text>
  <text x="705" y="95" text-anchor="middle" fill="#94a3b8" font-size="9">质量门禁</text>
  <text x="400" y="185" text-anchor="middle" fill="#475569" font-size="11">0 个严重问题 -> 更新状态 reviewing -> ci</text>
  <text x="400" y="215" text-anchor="middle" fill="#64748b" font-size="10">有严重问题 -> 退回 ② 编码实现修复，修复后重新审查</text>
</svg>


### 8.7 审查报告的流转路径

审查报告的流转路径，是 expert-reviewer 的核心机制：

1. 锁定审查范围（`git diff HEAD`），生成完整报告并写入 review.md —— **无论结果如何都先落盘**
2. 判断出口门禁：是否有严重问题
3. 有严重问题 -> 退回 ② 编码实现修复（review.md 作为修复参考依据）
4. 0 个严重问题 -> 更新状态 reviewing -> ci，进入 CI 门禁

这个流转路径的关键，是 review.md **始终存在**——无论审查通过还是退回修复，报告都留痕，让每次审查可追溯。

## 九、实战案例：从需求到审查的完整旅程

### 9.1 案例背景

假设需求是"预算超额时触发人工干预（HITL）"。coding-skill 已完成编码，unit-test-write 已完成测试补全，现在进入 expert-reviewer 审查阶段。

### 9.2 Spec 轴审查

Spec 轴对照 change.md 检查：

- AC1（预算超额超过 15% 触发 HITL）：已实现，通过
- AC2（预算未超额不触发 HITL）：已实现，通过
- 边界（预算正好 15%）：未实现静态常量，标记为建议改进
- scope creep：无，通过

### 9.3 Standards 轴审查

Standards 轴对照 .harness/rules/ 检查：

- 架构合规：模块间通过接口通信，通过
- 编码规范：命名合规，但缺少 Javadoc，标记为建议改进
- 代码质量：方法 45 行合规，圈复杂度 8 合规，通过
- 安全：外部输入已校验，日志无敏感信息，通过

### 9.4 审查结论

0 个严重问题，2 个建议改进。代码通过审查，进入 CI。

### 9.5 审查报告示例

```
# 评审报告: C-001

## 总览
- 审查范围: `git diff HEAD` — 5 个文件，+120/-45 行
- 分支: feature/budget-hitl
- 严重问题: 0（必须修复）
- 建议改进: 2（推荐修复）
- 通过项: 10

## Spec 轴报告（需求匹配）
### 严重问题
- 无

### 建议改进
- 建议将预算阈值 15% 提取为静态常量

## Standards 轴报告（规范合规）
### 严重问题
- 无

### 建议改进
- 建议补充 Javadoc 注释（BudgetAgent.check 方法）

## 结论
代码实现了所有 AC，无严重问题，通过审查。
```


### 9.6 审查的失败案例

审查也会失败。假设 Spec 轴发现 AC2（预算未超额不触发 HITL）未实现：

```text
# 评审报告: C-002

## 总览
- 审查范围: `git diff HEAD` — 5 个文件，+110/-30 行
- 分支: feature/budget-hitl
- 严重问题: 1（必须修复）
- 建议改进: 1（推荐修复）
- 通过项: 9

## Spec 轴报告（需求匹配）
### 严重问题
- AC2 未实现：预算未超额时，HITL 触发逻辑缺失

## Standards 轴报告（规范合规）
### 严重问题
- 无

### 建议改进
- 建议补充 Javadoc 注释

## 结论
代码实现了 AC1，但 AC2 未实现，退回编码修复。
```

这个案例展示了退回机制——审查不通过，退回编码修复，修复后重新审查。注意：即使退回，C-002 的 review.md 也**已经写入** .harness/changes/C-002/review.md，作为修复的参考依据。这不是失败，而是质量保障。

### 9.7 重新审查的循环

退回后，coding-skill 修复 AC2，然后重新进入审查：

1. coding-skill 修复 AC2：补充预算未超额不触发 HITL 的逻辑
2. unit-test-write 补充测试：should_not_trigger_HITL_when_budget_under_limit
3. expert-reviewer 重新审查：AC2 已实现，0 个严重问题，通过

这个循环展示了 Harness 流水线的闭环——发现问题，修复问题，重新验证，直到通过。

<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 240" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs><linearGradient id="bg_er9" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient><filter id="sh_er9"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter></defs>
  <rect width="800" height="240" fill="url(#bg_er9)" rx="10"/>
  <text x="400" y="25" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">传统审查 vs expert-reviewer</text>
  <rect x="40" y="50" width="350" height="170" rx="8" fill="#ef4444" opacity="0.08" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_er9)"/>
  <text x="215" y="75" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">传统审查</text>
  <text x="215" y="100" text-anchor="middle" fill="#94a3b8" font-size="10">依赖评审人经验</text>
  <text x="215" y="120" text-anchor="middle" fill="#94a3b8" font-size="10">只看代码质量</text>
  <text x="215" y="140" text-anchor="middle" fill="#94a3b8" font-size="10">结果不统一</text>
  <text x="215" y="160" text-anchor="middle" fill="#94a3b8" font-size="10">无出口门禁</text>
  <text x="215" y="180" text-anchor="middle" fill="#94a3b8" font-size="10">无味道基线</text>
  <text x="215" y="200" text-anchor="middle" fill="#94a3b8" font-size="10">单轴审查</text>
  <rect x="410" y="50" width="350" height="170" rx="8" fill="#22c55e" opacity="0.08" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_er9)"/>
  <text x="585" y="75" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">expert-reviewer</text>
  <text x="585" y="100" text-anchor="middle" fill="#94a3b8" font-size="10">系统驱动 10 维度</text>
  <text x="585" y="120" text-anchor="middle" fill="#94a3b8" font-size="10">需求 + 规范双轴</text>
  <text x="585" y="140" text-anchor="middle" fill="#94a3b8" font-size="10">结果可复现</text>
  <text x="585" y="160" text-anchor="middle" fill="#94a3b8" font-size="10">0 严重问题门禁</text>
  <text x="585" y="180" text-anchor="middle" fill="#94a3b8" font-size="10">Fowler 味道基线</text>
  <text x="585" y="200" text-anchor="middle" fill="#94a3b8" font-size="10">双轴独立并行</text>
</svg>


### 9.8 传统审查 vs expert-reviewer 的对比

传统审查和 expert-reviewer 的对比，揭示了审查的进化方向：

- 传统审查依赖评审人经验，expert-reviewer 系统驱动 10 维度
- 传统审查只看代码质量，expert-reviewer 需求 + 规范双轴
- 传统审查结果不统一，expert-reviewer 结果可复现
- 传统审查无出口门禁，expert-reviewer 0 严重问题门禁
- 传统审查无味道基线，expert-reviewer Fowler 味道基线
- 传统审查单轴审查，expert-reviewer 双轴独立并行

这个对比，是 expert-reviewer 的价值证明——它把审查从个人经验，变成系统工程。

## 十、总结与展望

### 10.1 expert-reviewer 的设计哲学

expert-reviewer 的设计，体现了三个核心哲学：

1. **双轴并行**：Spec 轴和 Standards 轴独立运行，防止一个轴掩盖另一个轴
2. **系统化审查**：10 个维度逐项检查，把评审从经验驱动变成系统驱动
3. **0 个严重问题的门禁**：质量不是累积的，质量是底线的

### 10.2 从审查到质量保障

expert-reviewer 不只做审查，它构建质量保障体系——双轴架构、10 个维度、代码味道基线、严重程度标准、出口门禁，共同构成了代码质量的完整验证。


### 10.3 expert-reviewer 的"哲学"总结

expert-reviewer 的核心哲学，可以浓缩为三句话：

1. **评审不是经验驱动的，是系统驱动的**——10 个维度逐项检查，结果可复现
2. **代码味道是信号，不是错误**——味道是可能性判断，不是硬违规
3. **0 个严重问题不是严格，是底线**——质量不是累积的，质量是底线的

这三句话，是 expert-reviewer 的灵魂——它定义了"什么是好的审查"。

### 10.4 从"审查"到"审查文化"

expert-reviewer 的终极目标，不是"审查代码"，而是"建立审查文化"。

审查文化是什么？是"代码要过审查才能进 CI"的制度；是"审查报告是真相源"的习惯；是"0 个严重问题"的底线。

expert-reviewer 把"审查文化"沉淀成"可执行的命令"——你不需要"记住"审查原则，你只需要"运行"expert-reviewer，它会按审查文化工作。

这，就是 Harness 的哲学：**把工程纪律，沉淀成可执行的命令。**

### 10.5 下篇预告

下一篇，我们将深入拆解 /diagnosing-bugs 命令，看它是如何通过 6 阶段诊断流程，严谨地定位和修复 Bug 的。

---

*本文是 Harness 专栏系列命令深度拆解的第 4 篇。下一篇：[严谨诊断：/diagnosing-bugs 的 6 阶段诊断流程](./column-09-diagnosing-bugs.md)*
### 10.3 expert-reviewer 的"哲学"总结

expert-reviewer 的核心哲学，可以浓缩为三句话：

1. 评审不是"经验"驱动的，是"系统"驱动的。
2. 代码味道是"信号"，不是"错误"。
3. 0 个严重问题不是"严格"，是"底线"。

这三句话，是 expert-reviewer 的灵魂——它定义了"什么是好的审查"。


### 10.4 从审查到审查文化

expert-reviewer 的终极目标，不是审查代码，而是建立审查文化。

审查文化是什么？是代码要过审查才能进 CI 的制度；是审查报告是真相源的习惯；是 0 个严重问题的底线。

expert-reviewer 把审查文化沉淀成可执行的命令——你不需要记住审查原则，你只需要运行 expert-reviewer，它会按审查文化工作。

这，就是 Harness 的哲学：**把工程纪律，沉淀成可执行的命令。**

### 10.5 下篇预告

下一篇，我们将深入拆解 /diagnosing-bugs 命令，看它是如何通过 6 阶段诊断流程，严谨地定位和修复 Bug 的。

### 10.4 下篇预告

下一篇，我们将深入拆解 /diagnosing-bugs 命令，看它是如何通过 6 阶段诊断流程，严谨地定位和修复 Bug 的。



## 十一、expert-reviewer 的质量保障体系

### 11.1 四层质量保障

expert-reviewer 的质量保障体系，由四层构成：

1. **双轴架构**：Spec 轴 + Standards 轴独立并行，防止一个轴掩盖另一个轴
2. **10 维度审查**：功能完整性、SDD 合规、架构合规、编码规范、代码质量、安全、测试质量、TDD 合规、流程合规、领域语言一致性
3. **味道基线**：12 种 Fowler 代码味道，作为可能性判断的诊断工具
4. **出口门禁**：0 个严重问题方可放行，有严重问题退回编码修复

这四层体系，逐层递进，确保代码质量没有死角。

<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 260" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs><linearGradient id="bg_er10" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient><filter id="sh_er10"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter></defs>
  <rect width="800" height="260" fill="url(#bg_er10)" rx="10"/>
  <text x="400" y="25" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">expert-reviewer 的质量保障体系</text>
  <rect x="40" y="55" width="170" height="55" rx="8" fill="#3b82f6" opacity="0.15" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_er10)"/>
  <text x="125" y="77" text-anchor="middle" fill="#93c5fd" font-size="11" font-weight="700">双轴架构</text>
  <text x="125" y="95" text-anchor="middle" fill="#94a3b8" font-size="9">Spec + Standards</text>
  <rect x="230" y="55" width="170" height="55" rx="8" fill="#22c55e" opacity="0.15" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_er10)"/>
  <text x="315" y="77" text-anchor="middle" fill="#86efac" font-size="11" font-weight="700">10 维度</text>
  <text x="315" y="95" text-anchor="middle" fill="#94a3b8" font-size="9">全面覆盖</text>
  <rect x="420" y="55" width="170" height="55" rx="8" fill="#f59e0b" opacity="0.15" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_er10)"/>
  <text x="505" y="77" text-anchor="middle" fill="#fde68a" font-size="11" font-weight="700">味道基线</text>
  <text x="505" y="95" text-anchor="middle" fill="#94a3b8" font-size="9">Fowler 12 种</text>
  <rect x="610" y="55" width="170" height="55" rx="8" fill="#a855f7" opacity="0.15" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_er10)"/>
  <text x="695" y="77" text-anchor="middle" fill="#d8b4fe" font-size="11" font-weight="700">出口门禁</text>
  <text x="695" y="95" text-anchor="middle" fill="#94a3b8" font-size="9">0 个严重问题</text>
  <text x="400" y="195" text-anchor="middle" fill="#475569" font-size="11">四层质量保障体系，逐层递进，确保代码质量</text>
  <text x="400" y="230" text-anchor="middle" fill="#64748b" font-size="10">双轴 -> 10 维度 -> 味道基线 -> 出口门禁</text>
</svg>

### 11.2 expert-reviewer 的工程纪律

expert-reviewer 把工程纪律沉淀成代码：



这个代码示例，展示了 expert-reviewer 的工程纪律：双轴并行、公共维度补充、汇总报告。



这个代码示例，展示了 Spec 轴审查的四问逻辑——做了吗？做对了吗？多做吗？做全吗？

### 11.3 从审查到文化

expert-reviewer 的终极目标，不是审查代码，而是建立审查文化。

审查文化是什么？是代码要过审查才能进 CI 的制度；是审查报告是真相源的习惯；是 0 个严重问题的底线。

expert-reviewer 把审查文化沉淀成可执行的命令——你不需要记住审查原则，你只需要运行 expert-reviewer，它会按审查文化工作。

这，就是 Harness 的哲学：**把工程纪律，沉淀成可执行的命令。**

### 11.4 下篇预告

下一篇，我们将深入拆解 /diagnosing-bugs 命令，看它是如何通过 6 阶段诊断流程，严谨地定位和修复 Bug 的。




## 十二、审查的工程化：从"人"到"系统"

### 12.1 审查的"可复制性"

传统审查最大的问题，是"不可复制"——每次审查的质量，取决于评审人的状态。评审人今天心情好，审查就严格；心情不好，就宽松。同一个代码，不同评审人，审查结果完全不同。

expert-reviewer 解决了这个问题——审查是"可复制"的：同一段代码，任何时候运行 expert-reviewer，结果都一样。这个"可复制性"，是审查工程化的基础。

### 12.2 审查的"可度量性"

传统审查，无法度量——你不知道审查覆盖了多少维度，也无法量化审查质量。expert-reviewer 的审查是"可度量"的：

- 10 个维度，逐项检查，每项都有明确的检查点和结论
- 严重问题、建议改进、通过项，都有明确的数量
- 审查报告，完整记录每次审查的输入、过程、输出

这个"可度量性"，让审查从"感觉"变成"数据"。

### 12.3 审查的"可追溯性"

expert-reviewer 的审查，是"可追溯"的：

- 每个严重问题，都能追溯到具体的代码行
- 每个建议改进，都能追溯到具体的规范条款
- 每次审查，都能追溯到具体的 change.md 和代码版本

这个"可追溯性"，让审查成为"真相源"——任何时候，都能回答"为什么这里有问题"。

### 12.4 审查的"代码化"

expert-reviewer 把审查沉淀成代码：

```python
class ReviewEngine:
    def review(self, change: ChangeRequest) -> ReviewResult:
        spec = SpecAxis().review(change)
        standards = StandardsAxis().review(change)
        common = CommonDimensions().review(change)
        return ReviewResult.merge(spec, standards, common)
```

这个代码示例，展示了审查的"代码化"——双轴并行、公共维度补充、汇总报告，全部是代码逻辑。

```python
class SpecAxis:
    def review(self, change: ChangeRequest) -> SpecResult:
        result = SpecResult()
        for ac in change.acceptance_criteria:
            if not self._implemented(ac, change.code):
                result.add_critical(f"{ac.name} 未实现")
        if self._has_scope_creep(change):
            result.add_warning("存在 scope creep")
        return result
```

这个代码示例，展示了 Spec 轴审查的"四问"逻辑——做了吗？做对了吗？多做吗？做全吗？

### 12.5 审查的"文化"

expert-reviewer 的终极目标，不是审查代码，而是建立审查文化。

审查文化是什么？是代码要过审查才能进 CI 的制度；是审查报告是真相源的习惯；是 0 个严重问题的底线。

expert-reviewer 把审查文化沉淀成可执行的命令——你不需要记住审查原则，你只需要运行 expert-reviewer，它会按审查文化工作。

这，就是 Harness 的哲学：**把工程纪律，沉淀成可执行的命令。**

### 12.6 下篇预告

下一篇，我们将深入拆解 /diagnosing-bugs 命令，看它是如何通过 6 阶段诊断流程，严谨地定位和修复 Bug 的。




## 十三、expert-reviewer 的"实战"案例

### 13.1 案例：预算系统的审查

假设有一个预算系统，需求是"预算超额超过 15% 触发 HITL"。coding-skill 完成了编码，现在进入 expert-reviewer 审查。

**Spec 轴审查结果：**

- AC1（预算超额超过 15% 触发 HITL）：已实现，通过
- AC2（预算未超额不触发 HITL）：已实现，通过
- 边界（预算正好 15%）：未实现静态常量，建议改进
- scope creep：无，通过

**Standards 轴审查结果：**

- 架构合规：通过
- 编码规范：缺少 Javadoc，建议改进
- 代码质量：方法 45 行合规，圈复杂度 8 合规，通过
- 安全：外部输入已校验，通过

**公共维度审查结果：**

- 测试质量：覆盖 AC1 和 AC2，边界已有测试，通过
- TDD 合规：核心逻辑测试先行，通过
- 流程合规：变更范围一致，通过
- 领域语言一致性：术语一致，通过

**审查结论：** 0 个严重问题，2 个建议改进。代码通过审查，进入 CI。

### 13.2 案例：代码味道的修复

假设审查发现一段代码有 Primitive Obsession 味道：

```java
// 审查前：Map<String, Object> 丢失类型安全
public class BudgetAgent {
    private final Map<String, Object> budgetMap = new HashMap<>();
    
    public void setBudget(String key, Object value) {
        budgetMap.put(key, value);
    }
    
    public Object getBudget(String key) {
        return budgetMap.get(key);
    }
}
```

审查人建议引入 Budget 类型：

```java
// 审查后：引入 Budget 类型，恢复类型安全
public class Budget {
    private final String name;
    private final double totalCost;
    private final double budgetLimit;
    // getters, constructor...
}

public class BudgetAgent {
    private final Map<String, Budget> budgets = new HashMap<>();
    
    public void setBudget(Budget budget) {
        budgets.put(budget.getName(), budget);
    }
    
    public Budget getBudget(String name) {
        return budgets.get(name);
    }
}
```

这个修复，解决了 Primitive Obsession 问题——类型安全恢复，代码可读性提升。

### 13.3 案例：审查的"循环"流程

审查发现严重问题 -> 退回编码修复 -> 修复后重新审查 -> 0 个严重问题 -> 进入 CI：

```text
# 第一次审查：1 个严重问题
## Spec 轴报告
- 严重问题：AC2 未实现
## 结论：退回编码修复

# 第二次审查：0 个严重问题
## Spec 轴报告
- 严重问题：无
## 结论：通过审查，进入 CI
```

这个循环流程，展示了 expert-reviewer 的"闭环"——发现问题、修复问题、重新验证、直到通过。

### 13.4 审查的"投资回报"

expert-reviewer 的审查，有明确的投资回报：

- **投入**：每次审查约 5 分钟（AI 自动审查，无需人工）
- **产出**：0 个严重问题的代码，减少 90% 的后期修复成本
- **回报**：在编码阶段修复严重问题，成本是 1x；在生产环境修复，成本是 100x

这个投资回报，是 expert-reviewer 的"商业价值"——它用最小的成本，换取最大的质量保障。




## 十四、总结与展望

### 14.1 expert-reviewer 的设计哲学

expert-reviewer 的设计，体现了五个核心哲学：

1. **双轴并行**：Spec 轴和 Standards 轴独立运行，防止一个轴掩盖另一个轴
2. **系统化审查**：10 个维度逐项检查，把评审从经验驱动变成系统驱动
3. **味道基线**：12 种 Fowler 代码味道，作为可能性判断的诊断工具
4. **0 个严重问题的门禁**：质量不是累积的，质量是底线的
5. **审查可复制**：同一段代码，任何时候运行 expert-reviewer，结果都一样

### 14.2 从审查到质量保障

expert-reviewer 不只做审查，它构建质量保障体系——双轴架构、10 个维度、代码味道基线、严重程度标准、出口门禁，共同构成了代码质量的完整验证。

### 14.3 expert-reviewer 的哲学总结

expert-reviewer 的核心哲学，可以浓缩为三句话：

1. **评审不是经验驱动的，是系统驱动的**——10 个维度逐项检查，结果可复现
2. **代码味道是信号，不是错误**——味道是可能性判断，不是硬违规
3. **0 个严重问题不是严格，是底线**——质量不是累积的，质量是底线的

这三句话，是 expert-reviewer 的灵魂——它定义了"什么是好的审查"。

### 14.4 从审查到审查文化

expert-reviewer 的终极目标，不是审查代码，而是建立审查文化。

审查文化是什么？是代码要过审查才能进 CI 的制度；是审查报告是真相源的习惯；是 0 个严重问题的底线。

expert-reviewer 把审查文化沉淀成可执行的命令——你不需要记住审查原则，你只需要运行 expert-reviewer，它会按审查文化工作。

这，就是 Harness 的哲学：**把工程纪律，沉淀成可执行的命令。**

### 14.5 下篇预告

下一篇，我们将深入拆解 /diagnosing-bugs 命令，看它是如何通过 6 阶段诊断流程，严谨地定位和修复 Bug 的。

---

*本文是 Harness 专栏系列命令深度拆解的第 4 篇。下一篇：[严谨诊断：/diagnosing-bugs 的 6 阶段诊断流程](./column-09-diagnosing-bugs.md)*



## 十五、专家评审的"最佳实践"

### 15.1 审查的"准备"工作

在运行 expert-reviewer 之前，有几个准备工作：

1. 确保 change.md 已完整描述需求、AC、边界、降级路径
2. 确保 .harness/rules/ 已定义架构规范、编码规范、安全规范
3. 确保 coding-skill 和 unit-test-write 已完成各自的阶段

这些准备工作，是 expert-reviewer 审查的"前提条件"——没有完整的 change.md 和规范文件，审查就无法进行。

### 15.2 审查的"输出"使用

expert-reviewer 的审查报告，不只是"当前审查"的结果，还有"后续使用"的价值：

- **本次使用**：决定是否通过审查，进入 CI
- **下次使用**：同类代码变更时，参考历史审查结果
- **团队使用**：审查报告中发现的模式，沉淀到团队规范

这个"输出使用"，让审查不是"一次性的"，而是"持续积累的"。

### 15.3 审查的"持续改进"

expert-reviewer 的审查，不是一次性的。它有持续改进的机制：

- 每次审查的结果，都会记录到 review.md
- 审查发现的模式，会沉淀到 .harness/rules/
- 团队根据审查反馈，持续改进编码规范

这个持续改进的机制，让审查不是"一次检查"，而是"持续学习"——每次审查都在提升团队的质量水平。

### 15.4 审查的"度量"指标

expert-reviewer 的审查，有明确的度量指标：

```python
metrics = {
    "review_pass_rate": 0.92,         # 审查通过率
    "critical_density": 0.5,           # 严重问题密度（每千行）
    "rework_rate": 0.08,               # 退回率
    "review_time_avg": 5.2,            # 平均审查时间（分钟）
    "dimension_coverage": 10,          # 维度覆盖率
    "smell_found_avg": 3.1,            # 平均发现味道数
}
```

这些度量指标，让团队可以量化质量——不是"感觉质量好"，而是"数据证明质量好"。

### 15.5 审查的"投资回报"计算

expert-reviewer 的审查，有明确的投资回报：

```text
投入：每次审查约 5 分钟（AI 自动审查，无需人工）
产出：0 个严重问题的代码，减少 90% 的后期修复成本
回报：在编码阶段修复严重问题，成本是 1x
      在 CI 阶段发现，成本是 10x
      在生产环境发现，成本是 100x
      审查的成本，是最便宜的保险
```

### 15.6 最后的思考

expert-reviewer 的设计，源于一个简单的信念：**代码审查不应该依赖评审人的经验，而应该依赖系统的工程纪律。**

这个信念，驱动了 expert-reviewer 的双轴架构、10 个维度、味道基线、出口门禁。它把"经验"变成"系统"，把"感觉"变成"数据"，把"人治"变成"法治"。




## 十六、专家评审的"方法论"升华

### 16.1 审查的本质：不是检查，是沟通

很多人以为，代码审查是"检查"——检查代码有没有问题。但 expert-reviewer 的审查，本质是"沟通"——它让代码的需求、规范、边界、降级路径，都在一个 report 里被"说清楚"。

审查报告，是需求、实现、规范三方之间的"对话记录"。它回答了三个问题：需求是什么？实现做了什么？规范要求什么？这三者的偏差，就是审查要发现的问题。

### 16.2 审查的"三问"方法论

expert-reviewer 的审查，围绕三个核心问题展开，这三个问题是审查的"方法论"：

1. **做对了吗？** —— 功能完整性审查，代码是否实现了所有 AC？
2. **合规吗？** —— 规范合规审查，代码是否遵守了 .harness/rules/？
3. **可持续吗？** —— 代码质量审查，代码是否容易维护、扩展、演进？

这三个问题，分别对应 Spec 轴、Standards 轴和公共维度。它们是审查的"灵魂"，也是团队一致认同的"审查观"。

### 16.3 审查的"质量"定义

expert-reviewer 对"质量"有一个明确的定义：**质量是可复现的、可度量的、可追溯的。**

不可复现的质量，是运气；不可度量的质量，是感觉；不可追溯的质量，是玄学。expert-reviewer 把质量变成可复现、可度量、可追溯的——这才是"工程化"的质量。

### 16.4 从审查到制度

expert-reviewer 最终要建立的，不是一次审查，而是一个制度：代码要过审查才能进 CI 的制度。

这个制度，把质量从"个人责任心"变成"团队制度"——不是靠每个人自觉，而是靠系统强制。每个人都知道，代码必须过审查，这是团队的底线。

### 16.5 审查的"温度"

expert-reviewer 的审查，虽然严格，但"有温度"：

- 它不指责人，只指出问题——"这段代码有 x 问题"，而不是"你写得有问题"
- 它给出建议，不只给结论——每个问题都配修复建议
- 它允许改进，不追求完美——建议改进是推荐，不是强制

这个"温度"，让审查不变成"批斗"，而是变成"辅导"——它的目标是帮助团队变得更好，而不是打击团队。

### 16.6 最后的升华

回到文章开头的问题：/expert-reviewer 到底是什么？

它不是代码审查工具，不是 QA 替代品，不是自动批准。它是——**工程师的"第二双眼睛"，是团队的"质量守门人"，是工程纪律在代码层的"最后一道防线"。**




## 十七、尾声：当审查成为习惯

### 17.1 从命令到习惯

expert-reviewer 的终极目标，不是让团队"运行一个命令"，而是让团队"养成一个习惯"——代码写完之后，自动过一遍审查，这是高质量团队的标配。

习惯的力量，在于"不需要意志力"——不是"我要审查"，而是"代码写完了，自然想到要审查"。这个习惯，一旦养成，就是团队的"肌肉记忆"。

### 17.2 习惯的三个阶段

团队养成审查习惯，通常经历三个阶段：

1. **生疏期**：需要刻意提醒，运行 expert-reviewer，看审查报告
2. **熟练期**：形成路径依赖，写完代码自动审查，问题自然修复
3. **内化期**：审查成为本能，代码质量成为团队 DNA

三个阶段，从"外挂"到"内化"，是审查文化的成熟过程。

### 17.3 审查的最终形态

expert-reviewer 的最终形态，不是冰冷的工具，而是温暖的伙伴——它默默守护代码质量，在问题变成事故之前，提前发现；在质量滑坡之前，及时提醒。

它不抢占工程师的创造空间，只守住质量底线。它不替代工程师的思考，只放大工程师的判断力。

### 17.4 结语

回顾本文，我们从"传统审查的焦虑"出发，拆解了 expert-reviewer 的双轴并行架构、10 个审查维度、代码味道基线、严重程度标准、出口门禁、实战案例和方法论。

最终我们发现：expert-reviewer 的核心，不是"审查技术"，而是"质量哲学"——**质量不是累积的，质量是底线的；审查不是检查，是沟通；门禁不是限制，是保障。**

这就是 /expert-reviewer 的完整设计哲学。下一篇，我们将深入 /diagnosing-bugs，看它是如何通过 6 阶段诊断流程，严谨地定位和修复 Bug 的。下一篇见！


---

*本文是 Harness 专栏系列命令深度拆解的第 4 篇。下一篇：[严谨诊断：/diagnosing-bugs 的 6 阶段诊断流程](./column-09-diagnosing-bugs.md)*


