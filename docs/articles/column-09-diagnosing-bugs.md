# 严谨诊断：/diagnosing-bugs 的 6 阶段诊断流程

> 命令深度拆解 · 第六篇 · 约 12000 字 · 12 个 SVG 图

## 一、/diagnosing-bugs 不是什么

在 Harness 流水线中，/diagnosing-bugs 是一个"反常规"的命令——它不在流水线的主线流程中，而是在"调试"阶段。当用户报告"这个出错/崩溃/性能慢"时，diagnosing-bugs 接手，用 6 阶段诊断流程严谨地定位和修复 Bug。

但很多人对它也有误解：

**它不是"调试工具"。** 传统的调试工具（GDB、IntelliJ Debugger、Chrome DevTools）负责"交互式调试"——设置断点、单步执行、查看变量。但 diagnosing-bugs 不负责"交互式调试"，它负责"系统化诊断"——建立反馈循环、最小化复现、生成假设、探测验证。

**它不是"错误监控"。** 错误监控系统（Sentry、Datadog、ELK）负责"捕捉错误"。但 diagnosing-bugs 负责"定位根因"——错误监控告诉你"什么错了"，diagnosing-bugs 告诉你"为什么错了"。

**它不是"热修复"。** 热修复是"快速修复上线"。但 diagnosing-bugs 是"严谨诊断"——不跳到"猜原因"阶段，不跳过反馈循环，不忽视回归测试。

### 1.1 从 huazai-trip-plan 的"调试焦虑"说起

在 huazai-trip-plan 的实践中，调试有一个焦虑：**"猜原因"比"找原因"多。**

传统调试的问题是：看到 Bug，第一反应是"猜"——"可能是缓存问题"、"可能是数据库挂了"、"可能是并发冲突"。猜完，改代码，部署，看结果。如果没修好，再猜。

这个"猜"的过程，浪费了大量时间——因为"猜"没有"反馈循环"，"猜"的结果无法验证。

diagnosing-bugs 的诞生，就是要解决这个焦虑——它把调试变成"系统化的"：先建立反馈循环，再复现最小化，再生成假设，再探测验证，再修复回归，最后清理分析。六步走完，Bug 无处可逃。

### 1.2 /diagnosing-bugs 的输入-输出契约

/diagnosing-bugs 的输入输出契约：

- **输入**：Bug 描述 + 代码 + 环境 + 错误信息
- **输出**：修复的代码 + 回归测试 + 事后分析报告
- **核心原则**：在建立稳定复现的反馈循环之前，禁止跳到"猜原因"阶段

<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 200" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs><linearGradient id="bg_db1" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient><filter id="sh_db1"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter></defs>
  <rect width="800" height="200" fill="url(#bg_db1)" rx="10"/>
  <text x="400" y="25" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">/diagnosing-bugs 的输入-输出契约</text>
  <rect x="40" y="50" width="220" height="55" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_db1)"/>
  <text x="150" y="72" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">输入</text>
  <text x="150" y="90" text-anchor="middle" fill="#94a3b8" font-size="10">Bug 描述 + 代码 + 环境</text>
  <line x1="260" y1="77" x2="290" y2="77" stroke="#475569" stroke-width="2"/><polygon points="295,77 287,72 287,82" fill="#94a3b8"/>
  <rect x="300" y="50" width="220" height="55" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_db1)"/>
  <text x="410" y="72" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">6 阶段诊断</text>
  <text x="410" y="90" text-anchor="middle" fill="#94a3b8" font-size="10">反馈循环 -> 最小化 -> 假设 -> 探测 -> 修复 -> 清理</text>
  <line x1="520" y1="77" x2="550" y2="77" stroke="#475569" stroke-width="2"/><polygon points="555,77 547,72 547,82" fill="#94a3b8"/>
  <rect x="560" y="50" width="200" height="55" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_db1)"/>
  <text x="660" y="72" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">输出</text>
  <text x="660" y="90" text-anchor="middle" fill="#94a3b8" font-size="10">修复代码 + 回归测试</text>
  <text x="400" y="160" text-anchor="middle" fill="#475569" font-size="11">核心原则：建立反馈循环之前，禁止跳到"猜原因"阶段</text>
</svg>

## 二、6 阶段诊断流程总览

### 2.1 六阶段的总体设计

/diagnosing-bugs 的诊断流程，由六个阶段组成：

1. **Phase 1：建立反馈循环** —— 核心阶段，完成 90% 的工作
2. **Phase 2：复现 + 最小化** —— 缩小到最小复现场景
3. **Phase 3：假设（3-5 个排序）** —— 生成可证伪的假设列表
4. **Phase 4：探测** —— 一次只改一个变量，验证假设
5. **Phase 5：修复 + 回归测试** —— 先写回归测试再修复
6. **Phase 6：清理 + 事后分析** —— 清理探针，分析根因

六阶段，从"复现"到"修复"到"复盘"，是完整的诊断流程。

<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 300" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs><linearGradient id="bg_db2" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient><filter id="sh_db2"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter></defs>
  <rect width="800" height="300" fill="url(#bg_db2)" rx="10"/>
  <text x="400" y="25" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">6 阶段诊断流程</text>
  <rect x="30" y="55" width="110" height="80" rx="8" fill="#ef4444" opacity="0.15" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_db2)"/>
  <text x="85" y="80" text-anchor="middle" fill="#fca5a5" font-size="10" font-weight="700">Phase 1</text>
  <text x="85" y="98" text-anchor="middle" fill="#94a3b8" font-size="9">建立反馈</text>
  <text x="85" y="114" text-anchor="middle" fill="#94a3b8" font-size="9">循环</text>
  <line x1="140" y1="95" x2="155" y2="95" stroke="#475569" stroke-width="2"/><polygon points="160,95 154,91 154,99" fill="#94a3b8"/>
  <rect x="165" y="55" width="110" height="80" rx="8" fill="#f59e0b" opacity="0.15" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_db2)"/>
  <text x="220" y="80" text-anchor="middle" fill="#fde68a" font-size="10" font-weight="700">Phase 2</text>
  <text x="220" y="98" text-anchor="middle" fill="#94a3b8" font-size="9">复现 +</text>
  <text x="220" y="114" text-anchor="middle" fill="#94a3b8" font-size="9">最小化</text>
  <line x1="275" y1="95" x2="290" y2="95" stroke="#475569" stroke-width="2"/><polygon points="295,95 289,91 289,99" fill="#94a3b8"/>
  <rect x="300" y="55" width="110" height="80" rx="8" fill="#3b82f6" opacity="0.15" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_db2)"/>
  <text x="355" y="80" text-anchor="middle" fill="#93c5fd" font-size="10" font-weight="700">Phase 3</text>
  <text x="355" y="98" text-anchor="middle" fill="#94a3b8" font-size="9">假设</text>
  <text x="355" y="114" text-anchor="middle" fill="#94a3b8" font-size="9">3-5 个排序</text>
  <line x1="410" y1="95" x2="425" y2="95" stroke="#475569" stroke-width="2"/><polygon points="430,95 424,91 424,99" fill="#94a3b8"/>
  <rect x="435" y="55" width="110" height="80" rx="8" fill="#a855f7" opacity="0.15" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_db2)"/>
  <text x="490" y="80" text-anchor="middle" fill="#d8b4fe" font-size="10" font-weight="700">Phase 4</text>
  <text x="490" y="98" text-anchor="middle" fill="#94a3b8" font-size="9">探测</text>
  <text x="490" y="114" text-anchor="middle" fill="#94a3b8" font-size="9">一次一个变量</text>
  <line x1="545" y1="95" x2="560" y2="95" stroke="#475569" stroke-width="2"/><polygon points="565,95 559,91 559,99" fill="#94a3b8"/>
  <rect x="570" y="55" width="110" height="80" rx="8" fill="#22c55e" opacity="0.15" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_db2)"/>
  <text x="625" y="80" text-anchor="middle" fill="#86efac" font-size="10" font-weight="700">Phase 5</text>
  <text x="625" y="98" text-anchor="middle" fill="#94a3b8" font-size="9">修复 +</text>
  <text x="625" y="114" text-anchor="middle" fill="#94a3b8" font-size="9">回归测试</text>
  <line x1="680" y1="95" x2="695" y2="95" stroke="#475569" stroke-width="2"/><polygon points="700,95 694,91 694,99" fill="#94a3b8"/>
  <rect x="705" y="55" width="80" height="80" rx="8" fill="#64748b" opacity="0.15" stroke="#64748b" stroke-width="1.5" filter="url(#sh_db2)"/>
  <text x="745" y="80" text-anchor="middle" fill="#cbd5e1" font-size="10" font-weight="700">Phase 6</text>
  <text x="745" y="98" text-anchor="middle" fill="#94a3b8" font-size="9">清理</text>
  <text x="745" y="114" text-anchor="middle" fill="#94a3b8" font-size="9">+ 分析</text>
  <text x="400" y="220" text-anchor="middle" fill="#475569" font-size="11">从"复现"到"修复"到"复盘"，六阶段完整诊断流程</text>
  <text x="400" y="250" text-anchor="middle" fill="#ef4444" font-size="11">Phase 1 是核心，完成 90% 的工作</text>
  <text x="400" y="275" text-anchor="middle" fill="#64748b" font-size="10">禁止在没有反馈循环的情况下进入 Phase 2</text>
</svg>

### 2.2 为什么"反馈循环"是核心

diagnosing-bugs 最核心的设计哲学是：**在建立稳定复现的反馈循环之前，禁止跳到"猜原因"阶段。**

这个哲学，源于一个深刻的认知：没有反馈循环，你无法验证"是否修好了"。你猜了一个原因，改了代码，部署了，但 Bug 还在。因为你没有反馈循环——你不知道"改代码是否真的修好了"，你不知道"Bug 是否还在"。

反馈循环，就是"红/绿信号"——一个能明确捕捉到当前 Bug 的测试。有反馈循环，你就能快速验证"改了代码，Bug 消失了吗？"没有反馈循环，你只能"猜"。



## 三、Phase 1：建立反馈循环（核心 90%）

### 3.1 反馈循环的"哲学"

Phase 1 是整个技能的核心。diagnosing-bugs 的设计者认为：如果你有一个能稳定复现 Bug 的"红/绿信号"，你已经完成了 90% 的工作。

为什么是 90%？因为"复现 Bug"是调试中最困难的部分。一旦你有了稳定的复现方式，定位根因、修复代码、验证修复，都变得简单。

### 3.2 构建反馈循环的 8 种方式

构建反馈循环的方式，按优先级排列：

1. **失败测试**：在能触达 Bug 的接缝处写单元/集成/E2E 测试
2. **Curl / HTTP 脚本**：针对运行中的 dev server
3. **CLI 调用**：带 fixture 输入，比对 stdout 与已知正确快照
4. **Playwright 无头浏览器脚本**：驱动 UI，断言 DOM/console/network
5. **重放录制的 trace**：保存真实请求/事件日志到磁盘，隔离重放
6. **临时测试桩**：启动最小子系统，单函数调用跑 Bug 路径
7. **二分法测试桩**：自动化"启动状态 X → 检查 → 重复"以便 git bisect run
8. **对比测试**：同一输入跑旧版 vs 新版，对比输出

### 3.3 反馈循环的三条原则

构建反馈循环时，有三条核心原则：

1. **要有侵略性，要有创造力，拒绝放弃。** 花不成比例的时间在这里。反馈循环是调试的"命门"，不能轻易放弃。

2. **收紧循环**：让它更快（缓存 setup，跳过无关初始化）、信号更清晰（断言具体症状，不是"没崩溃"）、更确定（固定时间，随机种子，隔离文件系统）。

3. **非确定性 Bug**：目标不是干净的复现，而是更高的复现率。循环跑 100 次，并行化，加压力，缩小时间窗口。50% 的 flake 是可调试的，1% 不是。

### 3.4 如果实在无法建立反馈循环

如果各种方法都试过了，仍然无法建立反馈循环，diagnosing-bugs 不会"硬闯"——它会停下来，明确说出尝试了什么，并向用户请求：

(a) 能复现的环境访问权限
(b) 保存的 artifact（HAR 文件、日志 dump、core dump、带时间戳的录屏）
(c) 在线上加临时探针的权限

**禁止在没有反馈循环的情况下进入 Phase 2。**

### 3.5 Phase 1 完成条件

Phase 1 完成时，必须满足五个条件：

- 有一个可复现的命令——脚本路径、测试调用、curl——已至少执行过一次并看到输出
- 这个命令能捕捉用户报告的具体症状（不是附近的另一个错误）
- 它是确定性的（非确定 Bug：有足够高的复现率）
- 它是快速的（秒级，不是分钟级）
- 它是可自动化运行的

<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 260" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs><linearGradient id="bg_db3" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient><filter id="sh_db3"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter></defs>
  <rect width="800" height="260" fill="url(#bg_db3)" rx="10"/>
  <text x="400" y="25" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">反馈循环构建优先级</text>
  <rect x="40" y="55" width="720" height="30" rx="6" fill="#22c55e" opacity="0.15" stroke="#22c55e" stroke-width="1" filter="url(#sh_db3)"/>
  <text x="400" y="75" text-anchor="middle" fill="#86efac" font-size="10" font-weight="700">优先：失败测试 > Curl > CLI > Playwright</text>
  <rect x="40" y="95" width="720" height="30" rx="6" fill="#3b82f6" opacity="0.15" stroke="#3b82f6" stroke-width="1" filter="url(#sh_db3)"/>
  <text x="400" y="115" text-anchor="middle" fill="#93c5fd" font-size="10" font-weight="700">中优：重放 trace > 临时测试桩 > 二分法</text>
  <rect x="40" y="135" width="720" height="30" rx="6" fill="#64748b" opacity="0.15" stroke="#64748b" stroke-width="1" filter="url(#sh_db3)"/>
  <text x="400" y="155" text-anchor="middle" fill="#cbd5e1" font-size="10" font-weight="700">最后：对比测试</text>
  <text x="400" y="195" text-anchor="middle" fill="#475569" font-size="11">反馈循环三条原则：侵略性、收紧循环、非确定 Bug 追求更高复现率</text>
  <text x="400" y="225" text-anchor="middle" fill="#ef4444" font-size="11">禁止在没有反馈循环的情况下进入 Phase 2</text>
</svg>

## 四、Phase 2：复现 + 最小化

### 4.1 最小化的"哲学"

Phase 2 的核心是"最小化"——把复现条件缩小到最小场景，逐个削减输入、调用方、配置、数据、步骤，每次削减后重新运行，只保留对 Bug 必需的要素。

为什么需要最小化？因为"最小化"是"定位根因"的前提。一个复杂的场景，可能包含多个因素——缓存、数据库、并发、网络。只有把场景缩小到最小，才能知道"哪个因素是根因"。

### 4.2 最小化的方法

最小化的方法，是"逐削减"：

1. 削减输入：去掉无关参数，只保留必要参数
2. 削减调用方：去掉无关调用，只保留必要调用
3. 削减配置：去掉无关配置，只保留必要配置
4. 削减数据：去掉无关数据，只保留必要数据
5. 削减步骤：去掉无关步骤，只保留必要步骤

每次削减，都重新运行反馈循环，确认 Bug 仍然存在。如果 Bug 消失了，说明削减的要素是 Bug 必需的，不能削减。

### 4.3 最小化的完成条件

Phase 2 的完成条件：**移除任何一个剩余要素都会让 Bug 消失。**

这个条件，确保你找到了"最小化"的复现场景——不多不少，刚好能复现 Bug。这个场景，就是"定位根因"的基础。

## 五、Phase 3：假设（3-5 个排序）

### 5.1 假设的"哲学"

Phase 3 的核心是"生成假设"——在测试任何假设之前，生成 3-5 个排序的假设。

为什么是 3-5 个？因为单一假设会让你锚定第一个合理的想法。你看到 Bug，第一个想法是"可能是 X 问题"，然后你就只验证 X，不去验证 Y、Z。如果 X 不是根因，你就浪费了时间。

### 5.2 假设的"可证伪性"

每个假设必须是"可证伪的"——说明它做出的预测。

格式：**"如果 <X> 是原因，那么 <改变 Y> 将让 Bug 消失 / <改变 Z> 将让 Bug 更严重。"**

这个格式，让假设从"猜测"变成"可验证的预测"。不是"可能是缓存问题"，而是"如果缓存数据过期是原因，那么清除缓存后 Bug 将消失"。

### 5.3 假设的"排序"

生成假设后，需要排序——向用户展示排序列表，再开始测试。用户通常有领域知识可以快速重排。

排序的标准：
1. **可能性**：哪个假设最可能？
2. **验证成本**：哪个假设最容易验证？
3. **影响范围**：哪个假设如果正确，影响最大？

### 5.4 假设的"案例"

假设一个预算系统 Bug：预算超额超过 15% 但未触发 HITL。

假设列表：
1. 逻辑错误：条件判断写反了，<= 15% 触发而不是 > 15%（可能性高，验证成本低）
2. 配置问题：HITL 阈值配置为 20% 而不是 15%（可能性中，验证成本低）
3. 数据问题：budgetMap 中存储的预算值不准确，导致计算错误（可能性中，验证成本高）
4. 并发问题：并发更新 budgetMap 导致数据不一致（可能性低，验证成本高）



## 六、Phase 4：探测

### 6.1 探测的"哲学"

Phase 4 的核心是"验证假设"——每个探测必须对应 Phase 3 的特定预测。一次只改一个变量。

为什么"一次只改一个变量"？因为改了多个变量，你不知道"哪个变量导致了变化"。这个原则，是"科学方法"在调试中的应用——控制变量，排除干扰，锁定根因。

### 6.2 探测的工具偏好

工具偏好：**调试器/REPL 检查 > 定向日志 > "全量日志再 grep"**

调试器/REPL 检查是最优的——你可以查看变量、执行表达式、验证假设。定向日志是次优的——你只能看到你打印的信息。全量日志再 grep 是最差的——信息太多，干扰太多。

### 6.3 探测的"唯一标签"原则

每个调试日志打唯一标签，如 `[DEBUG-a4f2]`，结束时一次性清理。

这个原则，解决了"调试日志留下"的问题。唯一标签，让日志搜索变得简单——grep 就能找到所有调试日志。结束时一次性清理，防止调试日志留在代码中。

### 6.4 探测的"完成条件"

Phase 4 的完成条件：找到根因。

- 通过探测，验证了假设
- 确认了 Bug 的根本原因
- 记录了根因和验证过程

<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 200" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs><linearGradient id="bg_db4" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient><filter id="sh_db4"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter></defs>
  <rect width="800" height="200" fill="url(#bg_db4)" rx="10"/>
  <text x="400" y="25" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">探测工具偏好</text>
  <rect x="40" y="55" width="220" height="55" rx="8" fill="#22c55e" opacity="0.15" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_db4)"/>
  <text x="150" y="77" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">调试器/REPL</text>
  <text x="150" y="95" text-anchor="middle" fill="#94a3b8" font-size="10">最优：可查看变量、执行表达式</text>
  <line x1="260" y1="82" x2="280" y2="82" stroke="#475569" stroke-width="2"/><polygon points="285,82 279,78 279,86" fill="#94a3b8"/>
  <rect x="290" y="55" width="220" height="55" rx="8" fill="#f59e0b" opacity="0.15" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_db4)"/>
  <text x="400" y="77" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">定向日志</text>
  <text x="400" y="95" text-anchor="middle" fill="#94a3b8" font-size="10">次优：只能看到打印的信息</text>
  <line x1="510" y1="82" x2="530" y2="82" stroke="#475569" stroke-width="2"/><polygon points="535,82 529,78 529,86" fill="#94a3b8"/>
  <rect x="540" y="55" width="220" height="55" rx="8" fill="#ef4444" opacity="0.15" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_db4)"/>
  <text x="650" y="77" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">全量日志再 grep</text>
  <text x="650" y="95" text-anchor="middle" fill="#94a3b8" font-size="10">最差：信息太多，干扰太多</text>
  <text x="400" y="160" text-anchor="middle" fill="#475569" font-size="11">原则：每个调试日志打唯一标签，如 [DEBUG-a4f2]，结束时一次性清理</text>
</svg>

## 七、Phase 5：修复 + 回归测试

### 7.1 修复的"哲学"

Phase 5 的核心是"先写回归测试，再修复"——但前提是有正确的接缝。

为什么先写回归测试？因为回归测试是"验证"的保障。你修复了 Bug，但怎么知道"真的修好了"？回归测试就是答案——先写失败的测试，看它失败，然后修复，看它通过。

### 7.2 正确的接缝

如果找不到正确的接缝，这本身就是发现——代码架构阻止了 Bug 被锁定。

什么是"正确的接缝"？就是测试能模拟真实 Bug 模式的接缝。如果代码耦合过深、依赖过重、测试接缝不存在，那么"找不到正确的接缝"本身就是发现——说明代码架构有问题，需要交给 /arch-review 处理。

### 7.3 修复的 5 步流程

有正确接缝时，修复流程是：

1. 将最小化复现转为失败测试
2. 看它失败（确认测试能捕捉 Bug）
3. 应用修复（修复代码）
4. 看它通过（确认修复有效）
5. 用原始（未最小化的）场景重新运行 Phase 1 反馈循环（确认修复不影响其他场景）

### 7.4 修复的"完成条件"

Phase 5 的完成条件：修复代码 + 回归测试通过。

- 回归测试通过（确认修复有效）
- 原始场景不再复现（确认修复不影响其他场景）
- 修复代码已提交（commit message 包含正确的假设）

<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 220" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs><linearGradient id="bg_db5" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient><filter id="sh_db5"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter></defs>
  <rect width="800" height="220" fill="url(#bg_db5)" rx="10"/>
  <text x="400" y="25" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">修复 5 步流程</text>
  <rect x="30" y="55" width="120" height="55" rx="8" fill="#ef4444" opacity="0.15" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_db5)"/>
  <text x="90" y="77" text-anchor="middle" fill="#fca5a5" font-size="10" font-weight="700">第1步：转入</text>
  <text x="90" y="95" text-anchor="middle" fill="#94a3b8" font-size="9">失败测试</text>
  <line x1="150" y1="82" x2="170" y2="82" stroke="#475569" stroke-width="2"/><polygon points="175,82 169,78 169,86" fill="#94a3b8"/>
  <rect x="180" y="55" width="120" height="55" rx="8" fill="#f59e0b" opacity="0.15" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_db5)"/>
  <text x="240" y="77" text-anchor="middle" fill="#fde68a" font-size="10" font-weight="700">第2步：看它</text>
  <text x="240" y="95" text-anchor="middle" fill="#94a3b8" font-size="9">失败</text>
  <line x1="300" y1="82" x2="320" y2="82" stroke="#475569" stroke-width="2"/><polygon points="325,82 319,78 319,86" fill="#94a3b8"/>
  <rect x="330" y="55" width="120" height="55" rx="8" fill="#3b82f6" opacity="0.15" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_db5)"/>
  <text x="390" y="77" text-anchor="middle" fill="#93c5fd" font-size="10" font-weight="700">第3步：应用</text>
  <text x="390" y="95" text-anchor="middle" fill="#94a3b8" font-size="9">修复</text>
  <line x1="450" y1="82" x2="470" y2="82" stroke="#475569" stroke-width="2"/><polygon points="475,82 469,78 469,86" fill="#94a3b8"/>
  <rect x="480" y="55" width="120" height="55" rx="8" fill="#22c55e" opacity="0.15" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_db5)"/>
  <text x="540" y="77" text-anchor="middle" fill="#86efac" font-size="10" font-weight="700">第4步：看它</text>
  <text x="540" y="95" text-anchor="middle" fill="#94a3b8" font-size="9">通过</text>
  <line x1="600" y1="82" x2="620" y2="82" stroke="#475569" stroke-width="2"/><polygon points="625,82 619,78 619,86" fill="#94a3b8"/>
  <rect x="630" y="55" width="140" height="55" rx="8" fill="#a855f7" opacity="0.15" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_db5)"/>
  <text x="700" y="77" text-anchor="middle" fill="#d8b4fe" font-size="10" font-weight="700">第5步：原始</text>
  <text x="700" y="95" text-anchor="middle" fill="#94a3b8" font-size="9">场景验证</text>
  <text x="400" y="175" text-anchor="middle" fill="#475569" font-size="11">先写回归测试，再修复；找不到正确接缝，这本身就是发现</text>
</svg>

## 八、Phase 6：清理 + 事后分析

### 8.1 清理的"清单"

Phase 6 的第一步是"清理"——确保调试探针不会留在代码中：

- 原始场景不再复现（重新运行 Phase 1 循环）
- 回归测试通过（或记录找不到正确接缝）
- 所有 [DEBUG-...] 探针已移除
- 一次性原型已删除
- 正确的假设写入 commit message

### 8.2 事后分析的"关键问题"

清理完成后，diagnosing-bugs 会问一个关键问题：**什么能阻止这个 Bug？**

如果答案是架构变更（没有好的测试接缝、耦合过深），交给 /arch-review 技能处理。

这个"关键问题"，是 diagnosing-bugs 的"升华"——不只是修复 Bug，还要防止 Bug 再次出现。如果架构问题导致了 Bug，那么修复 Bug 是不够的，还要修复架构。

### 8.3 事后分析的"价值"

事后分析的价值，不是"复盘"，而是"预防"：

- 找到 Bug 的根因，不是"谁写的代码"，而是"为什么代码会有 Bug"——测试不足？架构问题？需求不清？
- 找到防止 Bug 再次出现的方法，不是"下次注意"，而是"系统化改进"——完善测试？优化架构？明确需求？
- 把事后分析的结果，转化为团队的知识——写进文档、写进流程、写进文化



## 九、实战案例：从"报告"到"修复"的完整旅程

### 9.1 案例背景

假设一个在线支付系统的 Bug 报告：**"用户支付成功后，订单状态没有更新为'已支付'，一直停留在'待支付'。"**

这是一个典型的 Bug 报告。现在，用 /diagnosing-bugs 的 6 阶段诊断流程，走一遍完整旅程。

### 9.2 Phase 1：建立反馈循环（案例）

面对这个 Bug 报告，第一步是建立反馈循环——找到一个能稳定复现 Bug 的"红/绿信号"。

**方案 1：失败测试。** 在支付回调的接缝处写测试。支付回调是"用户支付成功后，系统更新订单状态"的入口。在这里写测试，能触达 Bug。

```java
@Test
void shouldUpdateOrderStatusToPaidWhenPaymentCallbackReceived() {
    OrderService orderService = new OrderService();
    PaymentCallback callback = new PaymentCallback("order-123", "PAID", 100.00);

    orderService.handlePaymentCallback(callback);

    assertEquals("PAID", orderService.getOrder("order-123").getStatus());
}
```

**运行结果：测试失败。** 订单状态没有更新为 PAID。这个失败的测试，就是反馈循环——它稳定复现了 Bug。

**可行性检查：**
- 可复现的命令：有（测试命令）
- 捕捉具体症状：是（订单状态没有更新）
- 确定性：是（每次运行都失败）
- 快速：是（秒级）
- 可自动化：是（CI 可运行）

Phase 1 完成。这个测试就是"红/绿信号"。

### 9.3 Phase 2：复现 + 最小化（案例）

最小化阶段，目标是缩小到最小复现场景。

**削减输入：** 去掉多余的参数，只保留必要参数。测试中只有 order-123、PAID、100.00 三个参数，已经很精简。

**削减调用方：** 去掉无关调用。测试只调用 handlePaymentCallback，没有其他调用。

**削减配置：** 去掉无关配置。测试没有依赖外部配置。

**削减数据：** 去掉无关数据。测试只有一个订单，没有其他数据。

**削减步骤：** 去掉无关步骤。测试只有一步：调用 handlePaymentCallback，然后断言。

最小化完成：测试已经是最小场景。移除任何一个剩余要素都会让 Bug 消失——比如，如果不调用 handlePaymentCallback，订单状态就不会变化，Bug 也不会出现。

### 9.4 Phase 3：假设（案例）

现在，生成 3-5 个排序的假设：

**假设 1：** 如果回调处理逻辑中，订单状态更新被条件判断拦截了，那么检查条件判断后，Bug 将消失。

```java
// 假设代码：条件判断可能有问题
if (callback.getStatus().equals("PAID")) {
    order.setStatus("PAID");  // 这行可能没执行
}
```

**假设 2：** 如果回调处理逻辑中，订单状态更新被异常吞掉了，那么查看异常处理后，Bug 将消失。

**假设 3：** 如果回调处理逻辑中，订单状态更新被事务回滚了，那么查看事务配置后，Bug 将消失。

**假设 4：** 如果回调处理逻辑中，订单状态更新被并发冲突覆盖了，那么查看并发控制后，Bug 将消失。

**排序：** 假设 1 可能性最高（条件判断是最常见的 Bug 来源），验证成本最低。假设 2、3 次之。假设 4 可能性最低。

### 9.5 Phase 4：探测（案例）

现在，验证假设。一次只改一个变量。

**探测假设 1：** 在条件判断处加一个调试日志，验证条件判断是否执行。

```java
if (callback.getStatus().equals("PAID")) {
    System.out.println("[DEBUG-a4f2] status is PAID, updating order");
    order.setStatus("PAID");
} else {
    System.out.println("[DEBUG-a4f2] status is not PAID: " + callback.getStatus());
}
```

**运行结果：** 没有输出 "[DEBUG-a4f2] status is PAID"。

结论：条件判断没有进入 PAID 分支。为什么？callback.getStatus() 不是 "PAID"。

**深挖：** 打印 callback.getStatus() 的值，发现是 "paid"（小写）而不是 "PAID"（大写）。

**根因确认：** 回调处理逻辑中，状态比较使用了大小写敏感的 equals，但回调传来的状态是小写 "paid"。这就是 Bug 的根因。

<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 240" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs><linearGradient id="bg_db6" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient><filter id="sh_db6"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter></defs>
  <rect width="800" height="240" fill="url(#bg_db6)" rx="10"/>
  <text x="400" y="25" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">根因定位：大小写敏感比较</text>
  <rect x="60" y="55" width="300" height="55" rx="8" fill="#ef4444" opacity="0.15" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_db6)"/>
  <text x="210" y="77" text-anchor="middle" fill="#fca5a5" font-size="11" font-weight="700">回调传来</text>
  <text x="210" y="95" text-anchor="middle" fill="#94a3b8" font-size="9">"paid"（小写）</text>
  <line x1="360" y1="82" x2="380" y2="82" stroke="#475569" stroke-width="2"/>
  <text x="400" y="78" text-anchor="middle" fill="#f59e0b" font-size="11" font-weight="700">equals()</text>
  <text x="400" y="94" text-anchor="middle" fill="#94a3b8" font-size="9">大小写敏感</text>
  <line x1="420" y1="82" x2="440" y2="82" stroke="#475569" stroke-width="2"/><polygon points="445,82 439,78 439,86" fill="#94a3b8"/>
  <rect x="450" y="55" width="300" height="55" rx="8" fill="#3b82f6" opacity="0.15" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_db6)"/>
  <text x="600" y="77" text-anchor="middle" fill="#93c5fd" font-size="11" font-weight="700">代码比较</text>
  <text x="600" y="95" text-anchor="middle" fill="#94a3b8" font-size="9">"PAID"（大写）→ 不相等 → Bug</text>
  <text x="400" y="170" text-anchor="middle" fill="#475569" font-size="11">根因：equals 大小写敏感 vs 回调状态小写</text>
  <text x="400" y="200" text-anchor="middle" fill="#22c55e" font-size="11">修复：使用 equalsIgnoreCase 或统一大小写</text>
</svg>

### 9.6 Phase 5：修复 + 回归测试（案例）

现在，修复 Bug。先写回归测试，再修复。

**回归测试**（先写，确认失败）：

```java
@Test
void shouldUpdateOrderStatusToPaidWhenStatusIsLowercase() {
    OrderService orderService = new OrderService();
    PaymentCallback callback = new PaymentCallback("order-456", "paid", 200.00);

    orderService.handlePaymentCallback(callback);

    assertEquals("PAID", orderService.getOrder("order-456").getStatus());
}
```

**运行结果：测试失败。** 确认回归测试能捕捉 Bug。

**应用修复**：

```java
if ("PAID".equalsIgnoreCase(callback.getStatus())) {
    order.setStatus("PAID");
}
```

**运行结果：测试通过。** 确认修复有效。

**原始场景验证**：重新运行 Phase 1 的失败测试，确认通过。修复不影响其他场景。

### 9.7 Phase 6：清理 + 事后分析（案例）

**清理：**
- 原始场景不再复现：重新运行 Phase 1 循环，通过
- 回归测试通过：大小写测试通过
- 所有 [DEBUG-...] 探针已移除：删除调试日志
- 一次性原型已删除：无
- 正确的假设写入 commit message："fix: 修复支付回调状态大小写敏感比较导致的订单状态不更新"

**事后分析：** 什么能阻止这个 Bug？

答案是：**统一的枚举映射**。如果回调状态使用枚举（PAID、PENDING、FAILED），而不是字符串比较，那么大小写问题就不会发生。这是架构改进——但当前代码没有这个抽象，需要交给 /arch-review 评估。

## 十、/diagnosing-bugs 的设计哲学

### 10.1 "反馈循环优先"哲学

diagnosing-bugs 最核心的设计哲学，是"反馈循环优先"。

**"在建立稳定复现的反馈循环之前，禁止跳到'猜原因'阶段。"** 这句话，是 diagnosing-bugs 的灵魂。

为什么反馈循环优先？因为：
1. **验证**：没有反馈循环，无法验证"是否修好了"
2. **定位**：没有反馈循环，无法定位"根因在哪"
3. **效率**：没有反馈循环，调试就是"猜"，猜的效率极低

### 10.2 "假设驱动"哲学

diagnosing-bugs 的第二个设计哲学，是"假设驱动"。

3-5 个排序的假设，每个假设必须可证伪。这个哲学，防止了"锚定效应"——单一假设会让你锚定第一个合理的想法。

### 10.3 "一次只改一个变量"哲学

diagnosing-bugs 的第三个设计哲学，是"一次只改一个变量"。

这个哲学，是科学方法在调试中的应用——控制变量，排除干扰，锁定根因。

### 10.4 "先写回归测试再修复"哲学

diagnosing-bugs 的第四个设计哲学，是"先写回归测试再修复"。

这个哲学，确保了"修复有效"——先写失败的测试，看它失败，然后修复，看它通过。没有回归测试的修复，是"不可验证的修复"。

### 10.5 "找不到正确接缝本身就是发现"哲学

diagnosing-bugs 的第五个设计哲学，是"找不到正确接缝本身就是发现"。

如果代码架构阻止了 Bug 被锁定（没有测试接缝、耦合过深），那么"找不到正确的接缝"本身就是发现——说明代码架构有问题。这个发现，要交给 /arch-review 处理。

### 10.6 "什么能阻止这个 Bug"哲学

diagnosing-bugs 的第六个设计哲学，是"什么能阻止这个 Bug"。

调试的终点，不是"修复 Bug"，而是"防止 Bug"。修复 Bug 是"治标"，防止 Bug 是"治本"。这个哲学，让 diagnosing-bugs 从"修复者"变成"预防者"。



## 十一、诊断的"反模式"：什么不该做

### 11.1 反模式 1：不建反馈循环，直接猜原因

这是最常见的反模式。看到 Bug，第一反应是"猜"——"可能是缓存问题"、"可能是数据库挂了"、"可能是并发冲突"。猜完，改代码，部署，看结果。如果没修好，再猜。

**后果：** 浪费大量时间。猜的原因，90% 都不对。猜完改完，Bug 还在，但时间已经花了。

**正确做法：** 先建立反馈循环——找到能稳定复现 Bug 的"红/绿信号"。花 80% 的时间在这里，花 20% 的时间修复。

### 11.2 反模式 2：一次改多个变量

看到 Bug，改了 A，又改了 B，又改了 C，然后部署，看结果。如果 Bug 消失了，不知道是 A、B、C 哪个修好的。如果 Bug 还在，也不知道是哪个改错了。

**后果：** 无法定位根因。改了多个变量，不知道"哪个变量导致了变化"。

**正确做法：** 一次只改一个变量。改完，运行反馈循环，看结果。如果 Bug 消失，找到根因。如果 Bug 还在，继续改下一个。

### 11.3 反模式 3：不写回归测试，直接改代码

看到 Bug，改了代码，然后手动验证——"看起来没问题了"。但手动验证不可靠——你不知道"是否真的修好了"。

**后果：** 修复可能"没有修好"。手动验证不可靠，回归测试才能验证。

**正确做法：** 先写回归测试，再修复。先写失败的测试，看它失败，然后修复，看它通过。

### 11.4 反模式 4：不清理调试探针

在代码中加了调试日志，修复后忘记删除。调试日志留在代码中，生产环境输出"垃圾"日志。

**后果：** 生产环境日志被污染，调试日志干扰正常日志。

**正确做法：** 每个调试日志打唯一标签，如 [DEBUG-a4f2]，结束时一次性清理。

### 11.5 反模式 5：不做事后分析

修复 Bug，提交代码，关闭 issue。但 Bug 下次还会出现——因为"什么能阻止这个 Bug"的问题没有回答。

**后果：** Bug 反复出现。团队陷入"修复 Bug → 再次出现 → 再次修复"的循环。

**正确做法：** 修复后，问"什么能阻止这个 Bug"。如果答案是架构变更，交给 /arch-review 处理。

<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 280" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs><linearGradient id="bg_db7" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient><filter id="sh_db7"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter></defs>
  <rect width="800" height="280" fill="url(#bg_db7)" rx="10"/>
  <text x="400" y="25" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">诊断 5 种反模式</text>
  <rect x="30" y="55" width="340" height="42" rx="6" fill="#ef4444" opacity="0.15" stroke="#ef4444" stroke-width="1" filter="url(#sh_db7)"/>
  <text x="200" y="72" text-anchor="middle" fill="#fca5a5" font-size="10" font-weight="700">反模式 1：不建反馈循环，直接猜原因</text>
  <text x="200" y="86" text-anchor="middle" fill="#64748b" font-size="8">→ 90% 的猜测都不对，浪费大量时间</text>
  <rect x="430" y="55" width="340" height="42" rx="6" fill="#ef4444" opacity="0.15" stroke="#ef4444" stroke-width="1" filter="url(#sh_db7)"/>
  <text x="600" y="72" text-anchor="middle" fill="#fca5a5" font-size="10" font-weight="700">反模式 2：一次改多个变量</text>
  <text x="600" y="86" text-anchor="middle" fill="#64748b" font-size="8">→ 无法定位根因，不知道哪个变量导致</text>
  <rect x="30" y="110" width="340" height="42" rx="6" fill="#ef4444" opacity="0.15" stroke="#ef4444" stroke-width="1" filter="url(#sh_db7)"/>
  <text x="200" y="127" text-anchor="middle" fill="#fca5a5" font-size="10" font-weight="700">反模式 3：不写回归测试，直接改代码</text>
  <text x="200" y="141" text-anchor="middle" fill="#64748b" font-size="8">→ 修复不可验证，手动验证不可靠</text>
  <rect x="430" y="110" width="340" height="42" rx="6" fill="#ef4444" opacity="0.15" stroke="#ef4444" stroke-width="1" filter="url(#sh_db7)"/>
  <text x="600" y="127" text-anchor="middle" fill="#fca5a5" font-size="10" font-weight="700">反模式 4：不清理调试探针</text>
  <text x="600" y="141" text-anchor="middle" fill="#64748b" font-size="8">→ 生产环境日志被污染</text>
  <rect x="200" y="165" width="400" height="42" rx="6" fill="#ef4444" opacity="0.15" stroke="#ef4444" stroke-width="1" filter="url(#sh_db7)"/>
  <text x="400" y="182" text-anchor="middle" fill="#fca5a5" font-size="10" font-weight="700">反模式 5：不做事后分析</text>
  <text x="400" y="196" text-anchor="middle" fill="#64748b" font-size="8">→ Bug 反复出现，陷入"修复→再次出现"循环</text>
  <text x="400" y="245" text-anchor="middle" fill="#22c55e" font-size="11">正确做法：反馈循环 → 一次一个变量 → 先写回归测试 → 清理探针 → 事后分析</text>
</svg>

## 十二、诊断的"效率"：从"小时"到"分钟"

### 12.1 反馈循环的效率

反馈循环建立得好，诊断效率能提升 10 倍以上。

**没有反馈循环：** 猜原因 → 改代码 → 部署 → 手动验证 → 猜另一个原因 → 再改 → 再部署 → 再验证。一个循环，1 小时。

**有反馈循环：** 运行反馈循环 → 验证假设 → 确认根因 → 修复 → 回归测试。一个循环，5 分钟。

效率提升 12 倍。这就是反馈循环的价值。

### 12.2 假设排序的效率

假设排序得好，诊断效率能翻倍。

**没有排序：** 验证第一个假设（可能不对）→ 验证第二个假设 → 验证第三个假设。最坏情况，验证 3 个假设才找到根因。

**有排序：** 验证最可能的假设（大概率正确）→ 找到根因。平均情况，验证 1.5 个假设找到根因。

效率翻倍。这就是假设排序的价值。

### 12.3 回归测试的效率

回归测试写得好，修复效率能再翻倍。

**没有回归测试：** 修复 → 手动验证（可能遗漏）→ 部署 → 出问题 → 重新修复。一个循环，30 分钟。

**有回归测试：** 写回归测试（5 分钟）→ 修复（5 分钟）→ 验证（10 秒）→ 部署。一个循环，10 分钟。

效率提升 3 倍。这就是回归测试的价值。

### 12.4 诊断效率的"总账"

综合来看，/diagnosing-bugs 的 6 阶段诊断流程，能让诊断效率提升 10 倍以上：

- 反馈循环：12 倍效率提升
- 假设排序：2 倍效率提升
- 回归测试：3 倍效率提升
- 总效率提升：10-20 倍

## 十三、/diagnosing-bugs 与 /unit-test-write 的配合

### 13.1 两个命令的"分工"

/diagnosing-bugs 和 /unit-test-write 是两个不同的命令，但它们是"互补"的：

- /diagnosing-bugs 负责"诊断"——找到 Bug 的根因，修复 Bug
- /unit-test-write 负责"测试"——编写测试，覆盖需求

diagnosing-bugs 的回归测试，就是 unit-test-write 的输入。diagnosing-bugs 找到了 Bug，修复了 Bug，然后需要 unit-test-write 来补充测试——确保 Bug 不再出现。

### 13.2 从"诊断"到"测试"

diagnosing-bugs 的 Phase 5（修复 + 回归测试）中，写回归测试时，需要用到 unit-test-write 的测试编写能力：

- 测试矩阵：从需求到测试的 1:1 映射
- 分层测试：不同的测试，不同的目标
- Mock 原则：测"逻辑"而非测"Mock"
- 覆盖率：不是数字，是承诺

### 13.3 从"诊断"到"审查"

diagnosing-bugs 的 Phase 6（事后分析）中，如果发现"架构问题"（没有测试接缝、耦合过深），需要交给 /arch-review 处理。这是 diagnosing-bugs 和 arch-review 的配合。

## 十四、总结：从"诊断"到"预防"

### 14.1 诊断的"六阶段"回顾

/diagnosing-bugs 的 6 阶段诊断流程，是完整的诊断流程：

1. Phase 1：建立反馈循环——完成 90% 的工作
2. Phase 2：复现 + 最小化——缩小到最小复现场景
3. Phase 3：假设（3-5 个排序）——生成可证伪的假设列表
4. Phase 4：探测——一次只改一个变量，验证假设
5. Phase 5：修复 + 回归测试——先写回归测试再修复
6. Phase 6：清理 + 事后分析——清理探针，分析根因

### 14.2 诊断的"六个哲学"

1. "反馈循环优先"——建立反馈循环之前，禁止跳到"猜原因"阶段
2. "假设驱动"——3-5 个排序的假设，每个假设必须可证伪
3. "一次只改一个变量"——控制变量，排除干扰，锁定根因
4. "先写回归测试再修复"——没有回归测试的修复，是不可验证的修复
5. "找不到正确接缝本身就是发现"——如果代码架构阻止了 Bug 被锁定，这是架构问题
6. "什么能阻止这个 Bug"——修复 Bug 是"治标"，防止 Bug 是"治本"

### 14.3 从"诊断"到"预防"

diagnosing-bugs 的终极目标，不是"修复 Bug"，而是"防止 Bug"。

修复 Bug 是"治标"——你修好了这个 Bug，但同样的 Bug 可能在其他地方出现。防止 Bug 是"治本"——你找到了 Bug 的根因，改进了架构、完善了测试、明确了需求，让 Bug 不再出现。

从"诊断"到"预防"，是 diagnosing-bugs 的升华。也是 Harness 工程纪律的终极目标。

<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 200" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs><linearGradient id="bg_db8" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient><filter id="sh_db8"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter></defs>
  <rect width="800" height="200" fill="url(#bg_db8)" rx="10"/>
  <text x="400" y="25" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">从"诊断"到"预防"</text>
  <rect x="60" y="55" width="300" height="55" rx="8" fill="#ef4444" opacity="0.15" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_db8)"/>
  <text x="210" y="77" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">治标：修复 Bug</text>
  <text x="210" y="95" text-anchor="middle" fill="#94a3b8" font-size="9">修好了这个 Bug，但下次还会出现</text>
  <line x1="360" y1="82" x2="440" y2="82" stroke="#475569" stroke-width="2"/><polygon points="445,82 437,78 437,86" fill="#94a3b8"/>
  <rect x="450" y="55" width="300" height="55" rx="8" fill="#22c55e" opacity="0.15" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_db8)"/>
  <text x="600" y="77" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">治本：防止 Bug</text>
  <text x="600" y="95" text-anchor="middle" fill="#94a3b8" font-size="9">改进了架构，让 Bug 不再出现</text>
  <text x="400" y="165" text-anchor="middle" fill="#475569" font-size="11">六个哲学，让 diagnosing-bugs 从"修复者"变成"预防者"</text>
</svg>

---

*本文是 Harness 专栏系列命令深度拆解的第 6 篇。下一篇：[质量门禁：/unit-test-ci 的 CI 全量质量门禁](./column-22-unit-test-ci.md)*



## 十五、/diagnosing-bugs 与 /harnessing 的配合

### 15.1 从"需求"到"诊断"

在 Harness 流水线中，/harnessing 和 /diagnosing-bugs 是两个端点：

- /harnessing 在"起点"——需求拷问，明确"做什么"
- /diagnosing-bugs 在"终点"——调试诊断，解决"哪里错了"

但这两个命令有一个共同点：**"一次只问一个问题"**。

/harnessing 一次只问一个问题，引导用户明确需求。diagnosing-bugs 一次只改一个变量，避免调试干扰。这个"一次一个"的原则，贯穿了 Harness 的整个设计哲学。

### 15.2 从"诊断"到"需求"

diagnosing-bugs 修复 Bug 后，有时会发现"需求不明确"导致 Bug。比如，支付回调状态大小写问题，本质是"需求没有明确指定的取值范围"。

这时候，需要回到 /harnessing——明确需求，补充用例，完善测试。

## 十六、/diagnosing-bugs 的"非确定性 Bug"处理

### 16.1 非确定性 Bug 的挑战

非确定性 Bug（flaky bugs）是最难调试的 Bug。它们的特点是：

- 不稳定的复现（有时出现，有时不出现）
- 难以定位（没有稳定的"红/绿信号"）
- 难以修复（修复可能"没有效果"）

传统调试方法，面对非确定性 Bug，几乎无能为力。

### 16.2 非确定性 Bug 的处理策略

diagnosing-bugs 处理非确定性 Bug 的策略是：

1. **目标不是干净的复现，而是更高的复现率**。循环跑 100 次，并行化，加压力，缩小时间窗口。

2. **50% 的 flake 是可调试的，1% 不是**。不要放弃，也不要强求。如果复现率 < 1%，可以考虑"先写防御性代码，再观察"。

3. **使用"二分法"缩小范围**。如果 Bug 在两个已知状态之间出现，自动化"启动状态 X → 检查 → 重复"以便 git bisect run。

4. **使用"对比测试"**。同一输入跑旧版 vs 新版，对比输出。如果旧版稳定，新版 flaky，说明是"新代码引入的"。

### 16.3 非确定性 Bug 的案例

```java
// 非确定性 Bug 案例：并发更新问题
public class BudgetService {
    private Map<String, Double> budgetMap = new HashMap<>(); // 非线程安全

    public void updateBudget(String userId, double amount) {
        // 没有同步，并发时可能丢失更新
        Double current = budgetMap.get(userId);
        if (current == null) {
            budgetMap.put(userId, amount);
        } else {
            budgetMap.put(userId, current + amount);
        }
    }
}
```

**诊断方法：** 编写并发测试，循环执行 100 次，检查是否出现数据不一致。

```java
@Test
void shouldHandleConcurrentBudgetUpdates() throws InterruptedException {
    BudgetService service = new BudgetService();
    int threadCount = 10;
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    CountDownLatch latch = new CountDownLatch(threadCount);

    for (int i = 0; i < threadCount; i++) {
        executor.submit(() -> {
            service.updateBudget("user-1", 100.0);
            latch.countDown();
        });
    }
    latch.await();
    executor.shutdown();

    // 预期：10 次更新，每次 100，总共 1000
    // 实际：可能少于 1000，因为 HashMap 不是线程安全的
    System.out.println("Final balance: " + service.getBudget("user-1"));
}
```

**修复：** 使用 ConcurrentHashMap 或同步块。

```java
private Map<String, Double> budgetMap = new ConcurrentHashMap<>();
```

## 十七、/diagnosing-bugs 的"调试日志"最佳实践

### 17.1 唯一标签原则

每个调试日志打唯一标签，如 [DEBUG-a4f2]。这个标签，是调试日志的"身份证"。

为什么需要唯一标签？因为：
1. 搜索方便：grep "[DEBUG-a4f2]" 就能找到所有调试日志
2. 清理方便：结束时 grep 并删除所有调试日志
3. 避免混淆：多个调试日志不会互相干扰

### 17.2 调试日志的"生命周期"

调试日志的生命周期：
1. 创建：加唯一标签，如 [DEBUG-a4f2]
2. 使用：在调试过程中使用，打印关键信息
3. 清理：在 Phase 6 中，一次性清理所有调试日志

### 17.3 调试日志的"最佳实践"

```java
// 最佳实践：调试日志 + 唯一标签
System.out.println("[DEBUG-a4f2] callback status: " + callback.getStatus());
System.out.println("[DEBUG-a4f2] order status before update: " + order.getStatus());

// 清理时：
// grep -r "\[DEBUG-" --include="*.java" . 找到所有调试日志
// 删除后，确认没有调试日志留下
```

<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 200" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs><linearGradient id="bg_db9" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient><filter id="sh_db9"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter></defs>
  <rect width="800" height="200" fill="url(#bg_db9)" rx="10"/>
  <text x="400" y="25" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">调试日志生命周期</text>
  <rect x="40" y="55" width="200" height="55" rx="8" fill="#22c55e" opacity="0.15" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_db9)"/>
  <text x="140" y="77" text-anchor="middle" fill="#86efac" font-size="11" font-weight="700">创建</text>
  <text x="140" y="95" text-anchor="middle" fill="#94a3b8" font-size="9">加唯一标签 [DEBUG-a4f2]</text>
  <line x1="240" y1="82" x2="280" y2="82" stroke="#475569" stroke-width="2"/><polygon points="285,82 279,78 279,86" fill="#94a3b8"/>
  <rect x="290" y="55" width="200" height="55" rx="8" fill="#3b82f6" opacity="0.15" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_db9)"/>
  <text x="390" y="77" text-anchor="middle" fill="#93c5fd" font-size="11" font-weight="700">使用</text>
  <text x="390" y="95" text-anchor="middle" fill="#94a3b8" font-size="9">调试过程中打印关键信息</text>
  <line x1="490" y1="82" x2="530" y2="82" stroke="#475569" stroke-width="2"/><polygon points="535,82 529,78 529,86" fill="#94a3b8"/>
  <rect x="540" y="55" width="220" height="55" rx="8" fill="#ef4444" opacity="0.15" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_db9)"/>
  <text x="650" y="77" text-anchor="middle" fill="#fca5a5" font-size="11" font-weight="700">清理</text>
  <text x="650" y="95" text-anchor="middle" fill="#94a3b8" font-size="9">Phase 6 一次性清理</text>
  <text x="400" y="165" text-anchor="middle" fill="#475569" font-size="11">原则：唯一标签 → 一次性使用 → 一次性清理，防止调试日志留在代码中</text>
</svg>

## 十八、结语：诊断的本质

### 18.1 诊断的本质

diagnosing-bugs 的本质，不是"修复 Bug"，而是"找到根因"。

找到根因，修复 Bug 是"顺便"的。找到根因，防止 Bug 再次出现是"根本"的。

### 18.2 诊断的"六阶段"的"哲学"

六阶段，其实是六个"哲学"：

1. 反馈循环优先：不要猜，要验证
2. 最小化：不要复杂，要简单
3. 假设驱动：不要单一，要多元
4. 一次一个变量：不要混乱，要清晰
5. 先写回归测试再修复：不要事后验证，要事前验证
6. 什么能阻止这个 Bug：不要治标，要治本

### 18.3 诊断的"最终"价值

diagnosing-bugs 的最终价值，不是"修复 Bug"，而是"让 Bug 不再出现"。

这个价值，是 /diagnosing-bugs 的"初心"。也是 Harness 工程纪律的"终极目标"——让 Bug 无处可逃，让代码质量系统化。

---

*本文是 Harness 专栏系列命令深度拆解的第 6 篇。下一篇：[质量门禁：/unit-test-ci 的 CI 全量质量门禁](./column-22-unit-test-ci.md)*



## 十九、更多实战案例

### 19.1 案例：内存泄漏诊断

**场景：** 一个定时任务处理订单，运行一段时间后 OOM。

**Phase 1 反馈循环：** 编写一个循环测试，每次处理 1000 个订单，检查内存使用。

```java
@Test
void shouldNotLeakMemoryWhenProcessingOrders() {
    OrderProcessor processor = new OrderProcessor();
    for (int i = 0; i < 10000; i++) {
        processor.processOrder(createTestOrder(i));
    }
    long memoryAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    System.out.println("Memory after 10k orders: " + memoryAfter);
    // 如果内存持续增长，说明有泄漏
}
```

**Phase 2 最小化：** 逐个削减处理步骤，发现"发送邮件"步骤内存泄漏。每次发送邮件，创建一个新的邮件对象，但对象没有被释放。

**Phase 3 假设：** 假设 1：邮件对象引用没有被释放。假设 2：邮件发送队列无限增长。假设 3：邮件附件缓存没有清理。

**Phase 4 探测：** 在邮件发送步骤加调试日志，检查邮件对象引用。发现邮件发送队列无限增长——队列没有设置上限。

**Phase 5 修复：** 使用有界队列（ArrayBlockingQueue），设置最大容量 1000。

**Phase 6 事后分析：** 什么能阻止这个 Bug？代码审查时应该检查队列是否设置上限。建议在代码审查清单中添加"队列大小"检查项。

### 19.2 案例：性能回归诊断

**场景：** 版本升级后，API 响应时间从 50ms 增长到 500ms。

**Phase 1 反馈循环：** 编写性能测试，比较新旧版本的响应时间。

```java
@Test
void shouldNotRegressApiResponseTime() {
    long start = System.currentTimeMillis();
    for (int i = 0; i < 100; i++) {
        apiClient.callApi("/orders");
    }
    long avg = (System.currentTimeMillis() - start) / 100;
    System.out.println("Average response time: " + avg + "ms");
    assertTrue(avg < 100, "Response time should be under 100ms");
}
```

**Phase 2 最小化：** 逐个削减 API 调用步骤，发现"数据库查询"步骤变慢。新版本中，数据库查询增加了 JOIN 操作。

**Phase 3 假设：** 假设 1：新版本增加了不必要的 JOIN。假设 2：数据库索引没有覆盖新查询。假设 3：数据库连接池配置不当。

**Phase 4 探测：** 检查 SQL 日志，发现新版本中，查询订单时 JOIN 了用户表，但用户表没有索引。

**Phase 5 修复：** 在用户表添加索引，优化 SQL 查询。

**Phase 6 事后分析：** 什么能阻止这个 Bug？性能测试应该在 CI 中自动运行，对比基准性能。建议在 CI 中增加性能回归测试。

<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 240" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs><linearGradient id="bg_db10" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient><filter id="sh_db10"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter></defs>
  <rect width="800" height="240" fill="url(#bg_db10)" rx="10"/>
  <text x="400" y="25" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">性能回归诊断流程</text>
  <rect x="40" y="55" width="160" height="55" rx="8" fill="#ef4444" opacity="0.15" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_db10)"/>
  <text x="120" y="77" text-anchor="middle" fill="#fca5a5" font-size="10" font-weight="700">发现性能回归</text>
  <text x="120" y="95" text-anchor="middle" fill="#94a3b8" font-size="9">响应时间 50ms → 500ms</text>
  <line x1="200" y1="82" x2="230" y2="82" stroke="#475569" stroke-width="2"/><polygon points="235,82 229,78 229,86" fill="#94a3b8"/>
  <rect x="240" y="55" width="160" height="55" rx="8" fill="#f59e0b" opacity="0.15" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_db10)"/>
  <text x="320" y="77" text-anchor="middle" fill="#fde68a" font-size="10" font-weight="700">建立反馈循环</text>
  <text x="320" y="95" text-anchor="middle" fill="#94a3b8" font-size="9">性能测试，比较新旧版本</text>
  <line x1="400" y1="82" x2="430" y2="82" stroke="#475569" stroke-width="2"/><polygon points="435,82 429,78 429,86" fill="#94a3b8"/>
  <rect x="440" y="55" width="160" height="55" rx="8" fill="#3b82f6" opacity="0.15" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_db10)"/>
  <text x="520" y="77" text-anchor="middle" fill="#93c5fd" font-size="10" font-weight="700">最小化定位</text>
  <text x="520" y="95" text-anchor="middle" fill="#94a3b8" font-size="9">数据库查询变慢</text>
  <line x1="600" y1="82" x2="630" y2="82" stroke="#475569" stroke-width="2"/><polygon points="635,82 629,78 629,86" fill="#94a3b8"/>
  <rect x="640" y="55" width="120" height="55" rx="8" fill="#22c55e" opacity="0.15" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_db10)"/>
  <text x="700" y="77" text-anchor="middle" fill="#86efac" font-size="10" font-weight="700">修复</text>
  <text x="700" y="95" text-anchor="middle" fill="#94a3b8" font-size="9">加索引 + 优化 SQL</text>
  <text x="400" y="175" text-anchor="middle" fill="#475569" font-size="11">关键：性能测试应在 CI 中自动运行，对比基准性能</text>
  <text x="400" y="205" text-anchor="middle" fill="#22c55e" font-size="11">事后分析：在 CI 中增加性能回归测试</text>
</svg>

## 二十、/diagnosing-bugs 的"局限"与"边界"

### 20.1 局限：反馈循环建立不了怎么办？

diagnosing-bugs 最核心的原则是"禁止在没有反馈循环的情况下进入 Phase 2"。但有时候，确实无法建立反馈循环。

这时候，diagnosing-bugs 的选择是"诚实告知"——明确说出尝试了什么，向用户请求资源支持。如果资源不可用，diagnosing-bugs 不会"硬闯"，而是"等待"。

### 20.2 局限：非确定性 Bug 怎么办？

非确定性 Bug 是最难调试的 Bug。diagnosing-bugs 的策略是"追求更高的复现率"，而不是"追求完全的复现"。如果复现率 < 1%，可以选择"先写防御性代码，再观察"。

### 20.3 边界：与 /arch-review 的边界

diagnosing-bugs 和 arch-review 的边界是"架构问题"：

- 如果 Bug 的根因是代码逻辑错误，diagnosing-bugs 负责修复
- 如果 Bug 的根因是架构问题（没有测试接缝、耦合过深），交给 /arch-review 处理

## 二十一、最后的思考：诊断的"人文"

### 21.1 诊断的"心态"

诊断，不只是"技术"，更是"心态"。

- 耐心：反馈循环可能花 80% 的时间，但这是值得的
- 好奇心：看到 Bug，不是"烦"，而是"有意思"
- 严谨：什么能阻止这个 Bug？不是"下次注意"，而是"系统化改进"

### 21.2 诊断的"团队"

诊断，不只是"个人"，更是"团队"。

- Phase 3 的假设排序，要向用户展示——用户可能有领域知识
- Phase 6 的事后分析，要转化为团队知识——写进文档、写进流程
- 从"诊断"到"预防"，需要团队一起改进

### 21.3 诊断的"文化"

最终，诊断是一种"文化"——"找到根因，防止 Bug"的文化。

这个文化，让 diagnosing-bugs 从"修复者"变成"预防者"，从"个人"变成"团队"，从"技术"变成"文化"。

---

*本文是 Harness 专栏系列命令深度拆解的第 6 篇。

## 二十二、/diagnosing-bugs 的"数学"视角

### 22.1 诊断的"信息论"

从信息论的角度看，诊断是一个"信息获取"过程。

- 反馈循环：建立"信号源"——一个能稳定复现 Bug 的测试
- 最小化：缩小"不确定范围"——从"整个系统"到"一个函数"
- 假设：生成"候选原因"——3-5 个可能的根因
- 探测：验证"候选原因"——一次一个变量，排除干扰

每一步，都在减少不确定性。每一步，都在增加信息量。

### 22.2 诊断的"效率"数学

诊断的效率，可以用一个简单的公式表示：

**效率 = 信息量 / 时间**

- 反馈循环：信息量大（能验证"是否修好了"），但时间长（需要建立测试）
- 猜想：信息量小（不能验证"是否修好了"），但时间短（不需要建立测试）

diagnosing-bugs 选择了"先建立反馈循环"——因为"信息量"的收益，远远大于"时间"的成本。

<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 200" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs><linearGradient id="bg_db11" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient><filter id="sh_db11"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter></defs>
  <rect width="800" height="200" fill="url(#bg_db11)" rx="10"/>
  <text x="400" y="25" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">诊断的"信息论"视角</text>
  <rect x="40" y="55" width="160" height="55" rx="8" fill="#ef4444" opacity="0.15" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_db11)"/>
  <text x="120" y="77" text-anchor="middle" fill="#fca5a5" font-size="10" font-weight="700">反馈循环</text>
  <text x="120" y="95" text-anchor="middle" fill="#94a3b8" font-size="9">建立信号源</text>
  <line x1="200" y1="82" x2="230" y2="82" stroke="#475569" stroke-width="2"/><polygon points="235,82 229,78 229,86" fill="#94a3b8"/>
  <rect x="240" y="55" width="160" height="55" rx="8" fill="#f59e0b" opacity="0.15" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_db11)"/>
  <text x="320" y="77" text-anchor="middle" fill="#fde68a" font-size="10" font-weight="700">最小化</text>
  <text x="320" y="95" text-anchor="middle" fill="#94a3b8" font-size="9">缩小不确定范围</text>
  <line x1="400" y1="82" x2="430" y2="82" stroke="#475569" stroke-width="2"/><polygon points="435,82 429,78 429,86" fill="#94a3b8"/>
  <rect x="440" y="55" width="160" height="55" rx="8" fill="#3b82f6" opacity="0.15" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_db11)"/>
  <text x="520" y="77" text-anchor="middle" fill="#93c5fd" font-size="10" font-weight="700">假设 + 探测</text>
  <text x="520" y="95" text-anchor="middle" fill="#94a3b8" font-size="9">验证候选原因</text>
  <line x1="600" y1="82" x2="630" y2="82" stroke="#475569" stroke-width="2"/><polygon points="635,82 629,78 629,86" fill="#94a3b8"/>
  <rect x="640" y="55" width="120" height="55" rx="8" fill="#22c55e" opacity="0.15" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_db11)"/>
  <text x="700" y="77" text-anchor="middle" fill="#86efac" font-size="10" font-weight="700">修复</text>
  <text x="700" y="95" text-anchor="middle" fill="#94a3b8" font-size="9">信息量最大化</text>
  <text x="400" y="165" text-anchor="middle" fill="#475569" font-size="11">每一步减少不确定性，每一步增加信息量</text>
</svg>

## 二十三、/diagnosing-bugs 与 /handoff 的配合

### 23.1 从"诊断"到"交接"

在 Harness 流水线中，/handoff 负责"交接"——把当前对话的上下文压缩成交接文档，用于在另一个对话中无缝续接工作。

diagnosing-bugs 和 handoff 的配合是：诊断完成后，如果 Bug 需要用另一个会话处理，handoff 负责交接。

### 23.2 交接的内容

handoff 交接的内容包括：
- 诊断的阶段（Phase 1-6 的完成状态）
- 反馈循环（测试命令、脚本）
- 最小化复现场景
- 假设列表（排序）
- 探测结果
- 修复方案
- 事后分析结果

## 二十四、最后的总结

### 24.1 六阶段的价值

/diagnosing-bugs 的 6 阶段诊断流程，是严谨的 Bug 诊断流程。它的核心价值是：

1. **反馈循环优先**：不猜，要验证
2. **最小化**：不复杂，要简单
3. **假设驱动**：不单一，要多元
4. **一次一个变量**：不混乱，要清晰
5. **先写回归测试再修复**：不事后验证，要事前验证
6. **什么能阻止这个 Bug**：不治标，要治本

### 24.2 从"诊断"到"预防"

diagnosing-bugs 的终极目标，不是"修复 Bug"，而是"防止 Bug"。

这个目标，让 diagnosing-bugs 从"修复者"变成"预防者"，从"个人"变成"团队"，从"技术"变成"文化"。

### 24.3 下篇预告

下一篇，我们将进入质量门禁的世界——/unit-test-ci。它是 Harness 流水线的"第五步"，负责在 CI 中执行全量质量门禁。从静态分析到竞态检测，从架构约束到全量测试，/unit-test-ci 确保每个 PR 都通过严格的质量门禁。敬请期待。

---

*本文是 Harness 专栏系列命令深度拆解的第 6 篇。

## 二十五、诊断的"终极"问题

### 25.1 什么能阻止这个 Bug？

这是 diagnosing-bugs 的"终极"问题——修复 Bug 后，问"什么能阻止这个 Bug"。

这个问题的答案，决定了诊断的"深度"：

- 如果答案是"代码审查"，那么完善审查清单
- 如果答案是"测试覆盖"，那么补充测试用例
- 如果答案是"架构改进"，那么交给 /arch-review
- 如果答案是"需求明确"，那么回到 /harnessing

### 25.2 什么能阻止"诊断"本身？

第二层问题：什么能阻止"诊断"本身？

- 反馈循环建立不了：需要环境访问权限
- 假设无法验证：需要更多数据
- 修复无法验证：需要回归测试

这个问题的答案，决定了 diagnosing-bugs 的"边界"——在边界内，诊断有效；在边界外，需要其他工具。

### 25.3 从"诊断"到"工程纪律"

最终，diagnosing-bugs 的价值，不在于"修复了多少 Bug"，而在于"建立了什么文化"——

- 反馈循环的文化：用验证代替猜测
- 假设驱动的文化：用多元代替单一
- 基于证据的文化：用事实代替感觉
- 预防 Bug 的文化：用治本代替治标

这个"文化"，是 Harness 工程纪律的"最高境界"——让 Bug 无处可逃，让代码质量系统化，让工程纪律内化在团队文化中。

<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 200" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs><linearGradient id="bg_db12" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient><filter id="sh_db12"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter></defs>
  <rect width="800" height="200" fill="url(#bg_db12)" rx="10"/>
  <text x="400" y="25" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">诊断的"文化"</text>
  <rect x="40" y="55" width="340" height="42" rx="6" fill="#22c55e" opacity="0.15" stroke="#22c55e" stroke-width="1" filter="url(#sh_db12)"/>
  <text x="210" y="72" text-anchor="middle" fill="#86efac" font-size="10" font-weight="700">反馈循环的文化：用验证代替猜测</text>
  <text x="210" y="86" text-anchor="middle" fill="#64748b" font-size="8">不猜原因，先建反馈循环，再验证假设</text>
  <rect x="420" y="55" width="340" height="42" rx="6" fill="#3b82f6" opacity="0.15" stroke="#3b82f6" stroke-width="1" filter="url(#sh_db12)"/>
  <text x="590" y="72" text-anchor="middle" fill="#93c5fd" font-size="10" font-weight="700">假设驱动的文化：用多元代替单一</text>
  <text x="590" y="86" text-anchor="middle" fill="#64748b" font-size="8">3-5 个假设，每个假设必须可证伪</text>
  <rect x="40" y="110" width="340" height="42" rx="6" fill="#a855f7" opacity="0.15" stroke="#a855f7" stroke-width="1" filter="url(#sh_db12)"/>
  <text x="210" y="127" text-anchor="middle" fill="#d8b4fe" font-size="10" font-weight="700">基于证据的文化：用事实代替感觉</text>
  <text x="210" y="141" text-anchor="middle" fill="#64748b" font-size="8">一次只改一个变量，用证据验证假设</text>
  <rect x="420" y="110" width="340" height="42" rx="6" fill="#f59e0b" opacity="0.15" stroke="#f59e0b" stroke-width="1" filter="url(#sh_db12)"/>
  <text x="590" y="127" text-anchor="middle" fill="#fde68a" font-size="10" font-weight="700">预防 Bug 的文化：用治本代替治标</text>
  <text x="590" y="141" text-anchor="middle" fill="#64748b" font-size="8">修复后问"什么能阻止这个 Bug"</text>
  <text x="400" y="175" text-anchor="middle" fill="#475569" font-size="11">最终，diagnosing-bugs 的价值不在于"修复了多少 Bug"，而在于"建立了什么文化"</text>
</svg>

---

*本文是 Harness 专栏系列命令深度拆解的第 6 篇。

## 二十六、诊断的"常见错误"速查表

### 26.1 反馈循环阶段

| 错误 | 后果 | 正确做法 |
|------|------|----------|
| 没有反馈循环就进入 Phase 2 | 无法验证修复 | 先建立反馈循环 |
| 反馈循环太慢 | 调试效率低 | 收紧循环，缓存 setup |
| 反馈循环不稳定 | 无法确认修复 | 固定时间、随机种子、隔离文件系统 |
| 反馈循环不捕捉具体症状 | 修复错误原因 | 断言具体症状，不是"没崩溃" |

### 26.2 最小化阶段

| 错误 | 后果 | 正确做法 |
|------|------|----------|
| 削减太多，Bug 消失 | 丢失 Bug 场景 | 逐个削减，每次重新运行 |
| 削减太少，场景复杂 | 无法定位根因 | 剪到最小，移除任何一个都会让 Bug 消失 |
| 没有削减直接假设 | 猜测太多 | 先最小化，再假设 |

### 26.3 假设阶段

| 错误 | 后果 | 正确做法 |
|------|------|----------|
| 只有一个假设 | 锚定第一个想法 | 生成 3-5 个假设 |
| 假设不可证伪 | 无法验证 | 每个假设必须说明它做出的预测 |
| 假设没有排序 | 浪费验证时间 | 排序后开始验证 |

### 26.4 探测阶段

| 错误 | 后果 | 正确做法 |
|------|------|----------|
| 一次改多个变量 | 无法定位根因 | 一次只改一个变量 |
| 使用全量日志再 grep | 信息太多干扰太多 | 使用调试器/REPL 或定向日志 |
| 调试日志没有唯一标签 | 清理困难 | 每个调试日志打唯一标签 |

### 26.5 修复阶段

| 错误 | 后果 | 正确做法 |
|------|------|----------|
| 没有回归测试就修复 | 修复不可验证 | 先写回归测试，再修复 |
| 找不到正确接缝 | 修复可能无效 | 这本身就是发现，交给 /arch-review |
| 修复后不验证原始场景 | 修复可能影响其他场景 | 用原始场景重新运行反馈循环 |

### 26.6 事后分析阶段

| 错误 | 后果 | 正确做法 |
|------|------|----------|
| 不清理调试探针 | 生产环境日志被污染 | 一次性清理所有调试日志 |
| 不做事后分析 | Bug 反复出现 | 问"什么能阻止这个 Bug" |
| 事后分析只针对个人 | 团队无改进 | 转化为团队知识 |

## 二十七、诊断的"10 倍效率"法则

### 27.1 法则 1：80/20 法则

80% 的时间建立反馈循环，20% 的时间修复 Bug。

反馈循环建立得好，修复 Bug 只需要 20% 的时间。反馈循环建立不好，修复 Bug 需要 80% 的时间——而且还不一定修得好。

### 27.2 法则 2：3-5 法则

生成 3-5 个假设，排序后开始验证。

单一假设容易锚定。3-5 个假设，覆盖了"最可能"的范围。排序后，从最可能的开始验证。

### 27.3 法则 3：一次一个变量法则

一次只改一个变量，验证后改下一个。

改了多个变量，不知道"哪个变量导致了变化"。一次一个变量，每个变量都能定位。

### 27.4 法则 4：先写回归测试法则

先写回归测试，再修复。没有回归测试的修复，是不可验证的修复。

### 27.5 法则 5：什么能阻止这个 Bug 法则

修复 Bug 后，问"什么能阻止这个 Bug"。这个问题的答案，决定了诊断的"深度"。

---

*本文是 Harness 专栏系列命令深度拆解的第 6 篇。

## 二十八、诊断的"团队实践"指南

### 28.1 诊断复盘

团队应该定期进行"诊断复盘"——回顾最近修复的 Bug，分析：

- 哪些 Bug 是"代码错误"？哪些是"架构问题"？
- 哪些 Bug 是"可以预防"的？哪些是"难以预防"的？
- 哪些反馈循环"建立得好"？哪些"建立得差"？
- 哪些假设"排序得好"？哪些"排序得差"？

诊断复盘的目的，不是"追责"，而是"改进"——让团队的诊断能力，一次次提升。

### 28.2 反馈循环的共享

团队应该共享"反馈循环"——建立一套公共的测试工具库，包含：

- 常用的测试桩（Mock 服务、测试客户端）
- 常用的测试数据（fixture、测试环境）
- 常用的测试脚本（curl、Playwright、性能测试）

共享反馈循环，让每个工程师都能快速建立"红/绿信号"，提高整个团队的诊断效率。

### 28.3 事后的"知识沉淀"

团队应该沉淀"事后分析"的知识——把"什么能阻止这个 Bug"的答案，转化为：

- 代码审查清单（新增检查项）
- 测试用例库（新增测试用例）
- 架构改进建议（交给 /arch-review）
- 需求澄清（回到 /harnessing）

知识沉淀，让团队的诊断能力，从"个人经验"变成"团队资产"。

### 28.4 诊断的"文化"建设

最终，团队应该建设"诊断文化"：

- 反馈循环的文化：用验证代替猜测
- 假设驱动的文化：用多元代替单一
- 基于证据的文化：用事实代替感觉
- 预防 Bug 的文化：用治本代替治标

这个"文化"，是 diagnosing-bugs 的终极目标，也是 Harness 工程纪律的"最高境界"。

## 二十九、最后的最后

### 29.1 诊断的"初心"

我们在文章开头问：/diagnosing-bugs 到底是什么？

现在，我们有了答案。它不是调试工具，不是错误监控，不是热修复。它是工程师的"严谨诊断"——在建立稳定复现的反馈循环之前，禁止跳到"猜原因"阶段。

### 29.2 诊断的"终点"

diagnosing-bugs 的终点，不是"修复 Bug"，而是"防止 Bug"。

从"修复"到"预防"，从"个人"到"团队"，从"技术"到"文化"——这是 diagnosing-bugs 的完整旅程。

### 29.3 一句话总结

/diagnosing-bugs 的完整哲学，可以浓缩成一句话：

**"在建立反馈循环之前禁止猜原因；一次只改一个变量；先写回归测试再修复；修复后问'什么能阻止这个 Bug'。"**

这就是 /diagnosing-bugs 的初心与终点。下一篇，我们进入质量门禁的世界——/unit-test-ci。期待与你继续这段工程纪律的旅程。

---

*本文是 Harness 专栏系列命令深度拆解的第 6 篇。

## 三十、从诊断到质量门禁

### 30.1 诊断的"终点"是质量门禁的"起点"

在 Harness 流水线中，diagnosing-bugs 是"调试"阶段，unit-test-ci 是"质量门禁"阶段。

诊断的"终点"是质量门禁的"起点"——diagnosing-bugs 修复 Bug 后，单元测试和回归测试需要在 CI 中自动运行，防止 Bug 再次出现。

### 30.2 诊断发现的"质量改进"

每次诊断，都会发现"质量改进机会"：

- 测试覆盖不足：需要补充测试
- 代码审查遗漏：需要完善审查清单
- 架构设计缺陷：需要交给 /arch-review
- 需求不明确：需要回到 /harnessing

这些"质量改进机会"，是诊断的"副产品"——但也是最有价值的"产出"。

### 30.3 诊断的"最终"价值

diagnosing-bugs 的最终价值，不是"修复 Bug"，而是"让 Bug 不再出现"。

从"修复"到"预防"，从"诊断"到"门禁"，从"个人"到"团队"——这是 diagnosing-bugs 的完整旅程。

```java
// 诊断的最终产物：一个防止 Bug 再次出现的回归测试
@Test
void shouldHandlePaymentCallbackStatusCaseInsensitively() {
    OrderService service = new OrderService();
    
    // 测试小写 paid
    PaymentCallback lowerCase = new PaymentCallback("order-1", "paid", 100.0);
    service.handlePaymentCallback(lowerCase);
    assertEquals("PAID", service.getOrder("order-1").getStatus());
    
    // 测试大写 PAID
    PaymentCallback upperCase = new PaymentCallback("order-2", "PAID", 200.0);
    service.handlePaymentCallback(upperCase);
    assertEquals("PAID", service.getOrder("order-2").getStatus());
    
    // 测试混合大小写 Paid
    PaymentCallback mixedCase = new PaymentCallback("order-3", "Paid", 300.0);
    service.handlePaymentCallback(mixedCase);
    assertEquals("PAID", service.getOrder("order-3").getStatus());
}
```

这个回归测试，就是诊断的"最终产物"——它确保 Bug 不会再次出现，确保工程质量得到保障。

---

*本文是 Harness 专栏系列命令深度拆解的第 6 篇。

### 30.4 诊断的"承诺"

diagnosing-bugs 做出三个承诺：

1. **在建立反馈循环之前，禁止跳到"猜原因"阶段**——这是它对"严谨"的承诺。
2. **一次只改一个变量，用证据验证假设**——这是它对"科学"的承诺。
3. **修复后问"什么能阻止这个 Bug"**——这是它对"预防"的承诺。

这三个承诺，是 /diagnosing-bugs 的"初心"，也是它区别于"猜原因"调试的根本所在。

### 30.5 别忘了"回头看"

最后，别忘了"回头看"——OpenAI 的"回头看"（reflection）原则，在诊断中同样重要：

- 花一分钟回顾：我学到了什么？
- 这个 Bug 的根因，让我对系统有了什么新理解？
- 下次遇到类似 Bug，我能更快定位吗？

"回头看"，让每次诊断都成为"成长"——让工程师的诊断能力，一次次提升。

---

*本文是 Harness 专栏系列命令深度拆解的第 6 篇。下一篇：[质量门禁：/unit-test-ci 的 CI 全量质量门禁](./column-22-unit-test-ci.md)*

