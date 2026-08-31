# /harness-quality：质量闸门，人签字才算数

> 命令深度拆解 · 第 28 篇 · 约 9500 字 · 8 个 SVG 图

---

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 300" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_q0" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_q0"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="300" fill="url(#bg_q0)" rx="10"/>
  <text x="400" y="28" text-anchor="middle" fill="#e2e8f0" font-size="26" font-weight="700">/harness-quality：质量闸门，人签字才算数</text>
  <rect x="60" y="55" width="320" height="95" rx="10" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_q0)"/>
  <text x="220" y="80" text-anchor="middle" fill="#fde68a" font-size="14" font-weight="700">跑真实 check</text>
  <text x="220" y="104" text-anchor="middle" fill="#94a3b8" font-size="11">用量具量，不用眼睛估</text>
  <text x="220" y="126" text-anchor="middle" fill="#64748b" font-size="10">AI 只整理报告，不参与判定</text>
  <rect x="420" y="55" width="320" height="95" rx="10" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_q0)"/>
  <text x="580" y="80" text-anchor="middle" fill="#93c5fd" font-size="14" font-weight="700">对照质量目标 + 人签字放行</text>
  <text x="580" y="104" text-anchor="middle" fill="#94a3b8" font-size="11">quality-targets.yml 是目标值</text>
  <text x="580" y="126" text-anchor="middle" fill="#64748b" font-size="10">放行由人，AI 绝不代签</text>
  <text x="400" y="185" text-anchor="middle" fill="#475569" font-size="13">四步流程，两条铁律</text>
  <rect x="60" y="205" width="220" height="45" rx="8" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_q0)"/>
  <text x="170" y="234" text-anchor="middle" fill="#86efac" font-size="12">真实数据说话</text>
  <rect x="300" y="205" width="220" height="45" rx="8" fill="#ef4444" opacity="0.10" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_q0)"/>
  <text x="410" y="234" text-anchor="middle" fill="#fca5a5" font-size="12">量不到就如实写\"未测量\"</text>
  <rect x="540" y="205" width="220" height="45" rx="8" fill="#a855f7" opacity="0.10" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_q0)"/>
  <text x="650" y="234" text-anchor="middle" fill="#d8b4fe" font-size="12">人签字才算放行</text>
  <text x="400" y="280" text-anchor="middle" fill="#64748b" font-size="10">核心洞察：质量不能\"感觉达标\"，要\"数据达标 + 人签字\"双保险</text>
</svg>
```

## 一、/harness-quality 解决的是什么问题

### 1.1 "质量达标了吗"——最危险的问题

在流水线里，这个问题迟早会被问到。而它的危险在于：**"达标"这个词，人人都有自己的定义**。

- 开发者说：测试写了，编译过了，达标。
- 测试说：主干流程都跑通了，达标。
- 管理者说：覆盖率看起来不错，达标。

三个"达标"用的完全是不同的尺子。如果放行依赖"感觉"，那质量就变成了"谁嗓门大谁说了算"。

`/harness-quality` 把这个问题**从主观辩论变成客观对账**：一切判定来自真实数据——覆盖率数字来自覆盖率工具、E2E 覆盖来自 flow→test 映射 + 真跑结果、lint 来自退出码。**AI 只负责把这些数据整理成报告，不参与"达标与否"的判定，放行由人。**

一句话：**质量不靠感觉，靠量具；放行不靠 AI，靠人。**

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 240" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_q1" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_q1"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="240" fill="url(#bg_q1)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">主观判断 vs 客观对账</text>
  <rect x="40" y="50" width="350" height="130" rx="10" fill="#ef4444" opacity="0.08" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_q1)"/>
  <text x="215" y="74" text-anchor="middle" fill="#fca5a5" font-size="13" font-weight="700">\"我看了一下代码，挺全的\"</text>
  <text x="215" y="102" text-anchor="middle" fill="#94a3b8" font-size="10">靠眼睛，不靠量具</text>
  <text x="215" y="124" text-anchor="middle" fill="#94a3b8" font-size="10">每个人定义不同的\"达标\"</text>
  <text x="215" y="146" text-anchor="middle" fill="#94a3b8" font-size="10">谁嗓门大谁说了算</text>
  <text x="215" y="170" text-anchor="middle" fill="#64748b" font-size="9">主观辩论：质量没有真相</text>
  <rect x="420" y="50" width="340" height="130" rx="10" fill="#22c55e" opacity="0.08" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_q1)"/>
  <text x="590" y="74" text-anchor="middle" fill="#86efac" font-size="13" font-weight="700">覆盖率 82.3% · lint 0 · flow→test 全覆盖</text>
  <text x="590" y="102" text-anchor="middle" fill="#94a3b8" font-size="10">数字来自量具，可复核</text>
  <text x="590" y="124" text-anchor="middle" fill="#94a3b8" font-size="10">对着目标逐项对账</text>
  <text x="590" y="146" text-anchor="middle" fill="#94a3b8" font-size="10">🔴/🟢 一栏一栏摆事实</text>
  <text x="590" y="170" text-anchor="middle" fill="#64748b" font-size="9">客观对账：达标与否可检验</text>
  <text x="400" y="210" text-anchor="middle" fill="#475569" font-size="11">质量 = 可测量的指标 + 可核对的报告 + 人签字放行</text>
  <text x="400" y="230" text-anchor="middle" fill="#64748b" font-size="10">AI 的角色：量尺寸、摆数据——而不是下结论</text>
</svg>
```

### 1.2 与 /unit-test-ci 的分工：一个机械化执行，一个客观判定

`/harness-quality` 的定位必须放在与 `/unit-test-ci` 的分工里理解：

- **`/unit-test-ci`** 是**机械化执行**全量门禁：编译 / 静态分析 / 架构约束 / 测试 / 安全，一次性跑完，红绿分明，该修的修。
- **`/harness-quality`** 在其之上做**客观判定 + 可追溯 + 人签字**：跑真实 check 收集数据 → 对照质量目标 → 出可核对的报告 → 请人签字放行。

换句话说，`unit-test-ci` 回答"**机器全绿了吗**"，`harness-quality` 回答"**质量达标了吗、能不能放行**"。前者是执行，后者是判定 + 批准——**判定需要目标（quality-targets.yml），批准需要人（签字）**，这两件事是 unit-test-ci 不做的。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 220" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_q2" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_q2"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="220" fill="url(#bg_q2)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">unit-test-ci vs harness-quality</text>
  <rect x="40" y="50" width="340" height="110" rx="10" fill="#3b82f6" opacity="0.10" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_q2)"/>
  <text x="210" y="76" text-anchor="middle" fill="#93c5fd" font-size="13" font-weight="700">/unit-test-ci</text>
  <text x="210" y="102" text-anchor="middle" fill="#94a3b8" font-size="10">机械化执行全量门禁</text>
  <text x="210" y="124" text-anchor="middle" fill="#94a3b8" font-size="10">编译/静态分析/架构/测试/安全</text>
  <text x="210" y="146" text-anchor="middle" fill="#64748b" font-size="9">回答：机器全绿了吗</text>
  <rect x="420" y="50" width="340" height="110" rx="10" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_q2)"/>
  <text x="590" y="76" text-anchor="middle" fill="#fde68a" font-size="13" font-weight="700">/harness-quality</text>
  <text x="590" y="102" text-anchor="middle" fill="#94a3b8" font-size="10">客观判定 + 可追溯 + 人签字</text>
  <text x="590" y="124" text-anchor="middle" fill="#94a3b8" font-size="10">对照目标 · 出报告 · 请人放行</text>
  <text x="590" y="146" text-anchor="middle" fill="#64748b" font-size="9">回答：质量达标了吗、能放行吗</text>
  <text x="400" y="190" text-anchor="middle" fill="#475569" font-size="11">前者是执行（跑门禁），后者是判定+批准（定目标、要签字）</text>
  <text x="400" y="212" text-anchor="middle" fill="#64748b" font-size="10">判定的两个前提——目标文件 + 签字人——正是 quality 独有的</text>
</svg>
```

### 1.3 两条铁律：真实数据 + 量不到就写"未测量"

`/harness-quality` 的立身之本，浓缩成两条铁律：

**铁律一：一切判定来自客观数据。**

- 覆盖率数字来自覆盖率工具；
- E2E 覆盖来自 flow→test 映射 + 真跑结果；
- lint 来自退出码。

**铁律二：量不到就如实写"未测量"，绝不编造。**

⛔ 绝不允许出现的行为：

- 用"我看了一下代码，测试挺全的"这类判断替代真实数据；
- 用上一次的结果冒充本次；
- 命令没跑通却按通过处理；
- 覆盖率没测量却写一个数字；
- **量不到就如实写"未测量"**——这与"无命令可跑 → 该项不判定"是同一条纪律。

为什么"写'未测量'"和"写'通过'"一样重要？因为**"未测量"是诚实的红灯，"编一个数"是危险的绿灯**。编一个"覆盖率 95%"的假数字，等于给坏代码发通行证；而"未测量"至少让人知道这个维度没有得到保证。虚假的数据比没有数据更糟——它会直接骗过签字的人。

## 二、前置：两份真源文件

执行 `/harness-quality` 前，先确认两份"真源"是否就位——它们定义了**怎么量**和**目标是多少**。

| 文件 | 作用 | 缺失时 |
|------|------|--------|
| `.harness/rules/开发流程规范.md` + 语言包 `unit-test-ci` 技能内的命令段 | **怎么量**（命令：TEST_CMD / COV_CMD / LINT_CMD / ARCH_TEST_CMD / SECURITY_CMD 等，已按语言渲染） | 中止，提示先跑 `/apply-harness` 或 `/unit-test-ci` 确认命令可用 |
| `.harness/quality-targets.yml` | **目标值**（覆盖率阈值、lint 阈值、flow→test 覆盖要求） | 提示创建（见 §2.1），或使用默认目标 |

这里的架构逻辑是**命令与目标分离**：

- **命令文件管"怎么量"**——度量方式由开发流程规范和语言包定义，保证每个项目用量具的方式一致、可复现；
- **目标文件管"量多少算达标"**——阈值由项目团队拍板，不同项目可以有不同的质量标准（核心库要 80%，工具脚本可以放宽）。

两者都缺失时中止并提示先跑 `/apply-harness`——因为**没有量具和质量标准，任何"质量报告"都是空中楼阁**。

### 2.1 quality-targets.yml：目标值由人拍板

```yaml
# .harness/quality-targets.yml
core_coverage: 0.80        # 核心逻辑覆盖率阈值
total_coverage: 0.60       # 全量覆盖率阈值（可选）
lint_violations: 0         # lint 违规数上限
flow_test_required: true   # 每条业务流必须有对应测试
danger_ddl_dml: 0          # 危险 DDL/DML 命中数上限（必须为 0）
hardcoded_secrets: [redacted]   # 硬编码凭据命中数上限（必须为 0）
```

首次运行本技能且该文件不存在 → 用默认值创建，并**提示人类确认目标**。

关键纪律：**目标调整属人类决策，AI 不得自行放宽**。如果覆盖率是 60% 而目标要求 80%，AI 的职责是如实标红，不是"通融一下"把目标改成 60%。目标的权威在人类手里——这也是"判断力归属人类"哲学的延伸：**AI 可以量，不可以定标准**。

我注意到上面的 yaml 示例里我故意写了一个中文键，这是我测试时的不严谨。实际模板中所有键都是英文（同前面质量目标描述）——**任何键都必须来自模板，报告和实际执行使用同一套配置**，避免"目标定义"和"测量字段"漂移成两套语言。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 240" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_q3" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_q3"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="240" fill="url(#bg_q3)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">怎么量 vs 目标是多少：两件事分开管</text>
  <rect x="40" y="50" width="340" height="110" rx="10" fill="#3b82f6" opacity="0.10" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_q3)"/>
  <text x="210" y="76" text-anchor="middle" fill="#93c5fd" font-size="13" font-weight="700">命令文件（开发流程规范）</text>
  <text x="210" y="102" text-anchor="middle" fill="#94a3b8" font-size="10">管"怎么量"——量具统一</text>
  <text x="210" y="124" text-anchor="middle" fill="#94a3b8" font-size="10">TEST_CMD/COV_CMD/LINT_CMD</text>
  <text x="210" y="146" text-anchor="middle" fill="#64748b" font-size="9">按语言渲染，可复现</text>
  <rect x="420" y="50" width="340" height="110" rx="10" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_q3)"/>
  <text x="590" y="76" text-anchor="middle" fill="#fde68a" font-size="13" font-weight="700">质量目标文件 quality-targets.yml</text>
  <text x="590" y="102" text-anchor="middle" fill="#94a3b8" font-size="10">管"量多少算达标"——阈值人定</text>
  <text x="590" y="124" text-anchor="middle" fill="#94a3b8" font-size="10">core_coverage / lint 上限</text>
  <text x="590" y="146" text-anchor="middle" fill="#64748b" font-size="9">不同项目可不同标准</text>
  <text x="400" y="190" text-anchor="middle" fill="#475569" font-size="11">量具统一，标准可异；两者缺一，报告无根</text>
  <text x="400" y="214" text-anchor="middle" fill="#64748b" font-size="10">目标调整是人的决策——AI 只量，不定标准</text>
</svg>
```

## 三、执行流程：五步，步步有据

### Step 1: 锁定范围

按 `.harness/rules/变更定位规则.md` 定位目标 change（`status: ci` 或人工指定任意 change）。若为全项目质量体检 → 使用 `git diff` 或指定版本区间作为范围，**报告开头标注范围**。

为什么要标注范围？因为"覆盖率 80%"在"本次 change 的 diff" 和"全项目"两个口径下是完全不同的含义。范围不标，报告无法被复核——复核的人不知道这个 80% 是哪个范围的 80%。

### Step 2: 跑真实 check（机械执行）

逐项执行命令真源里的命令，**每一步都记录真实输出**：

- 覆盖命令（如 `go test -cover ./...`）→ 记录覆盖率数字；
- 静态分析 / lint → 记录违规数（来自退出码与输出）；
- 架构约束检查 → 记录命中数；
- 安全扫描（硬编码凭据、危险 DDL/DML）→ 记录命中数。

机械执行的意思是：**命令怎么输的就怎么记**，不做"我觉得这个数字合理所以保留"的加工。

### Step 3: flow→test 映射对账

把 PRD 的业务流（flow）与自动化测试（test）逐个对账：

- 读取 PRD / change 的 AC（验收标准）与业务流定义；
- 列出该流对应的测试（测试名 + 断言了什么）；
- 逐条核对：**每条业务流是否都有对应测试覆盖**；
- 漂移（某条 AC 没有测试、或测试测的不是 AC 描述的行为）→ 标 🔴 红灯。

这是 `/harness-quality` 最有"领域sense"的一步——机器检查只能告诉你"这段代码被测试覆盖了多少行"，flow→test 对账告诉你"**业务上承诺的行为，测试真的验证了吗**"。行覆盖率 90% 但核心业务流程没测，这个 90% 是虚的。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 280" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_q4" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_q4"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="280" fill="url(#bg_q4)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">flow→test 对账：业务承诺 vs 测试验证</text>
  <text x="170" y="60" text-anchor="middle" fill="#94a3b8" font-size="12">PRD 业务流（FLOW）</text>
  <text x="630" y="60" text-anchor="middle" fill="#94a3b8" font-size="12">自动化测试（TEST）</text>
  <rect x="60" y="78" width="220" height="40" rx="8" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_q4)"/>
  <text x="170" y="102" text-anchor="middle" fill="#fde68a" font-size="11">AC1 下单后库存锁定</text>
  <rect x="520" y="78" width="220" height="40" rx="8" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_q4)"/>
  <text x="630" y="102" text-anchor="middle" fill="#86efac" font-size="11">TestOrderReserveStock ✅</text>
  <rect x="60" y="132" width="220" height="40" rx="8" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_q4)"/>
  <text x="170" y="156" text-anchor="middle" fill="#fde68a" font-size="11">AC2 超时自动释放</text>
  <rect x="520" y="132" width="220" height="40" rx="8" fill="#ef4444" opacity="0.10" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_q4)"/>
  <text x="630" y="156" text-anchor="middle" fill="#fca5a5" font-size="11">无对应测试 🔴 漂移</text>
  <line x1="280" y1="98" x2="520" y2="98" stroke="#22c55e" stroke-width="2" stroke-dasharray="6 4"/>
  <line x1="280" y1="152" x2="520" y2="152" stroke="#ef4444" stroke-width="2" stroke-dasharray="6 4"/>
  <text x="400" y="225" text-anchor="middle" fill="#475569" font-size="11">行覆盖率 90% 但 AC2 没人测——这个 90% 是虚的</text>
  <text x="400" y="252" text-anchor="middle" fill="#64748b" font-size="10">机器检查看"行"，flow→test 对账看"业务承诺"——两层都要过</text>
</svg>
```

### Step 4: 出可核对的报告

报告落盘 `.harness/changes/<id>/quality-report.md`，每项数据都标注**来源**（命令、输出、时间），让复核者可以重新执行验证。报告核心是质量门禁表：

```
## 质量门禁报告
范围: <git diff 范围 / change id>

| 检查项 | 实测 | 目标 | 状态 |
|--------|------|------|------|
| 单元覆盖率 | 82.3% | ≥80% | 🟢 |
| 全量覆盖率 | 55.1% | ≥60% | 🔴 |
| lint 违规 | 0 | 0 | 🟢 |
| flow→test 映射 | 4/5 AC 有直接测试 | 全覆盖 | 🔴 |
| 硬编码凭据 | 0 | 0 | 🟢 |
| 未测量项 | — | — | ⚪ 未测量（如实标注） |

## 达标判定
- 是否全部达标：✅ / ❌
- 未达标项清单 + 差距数据（不写"感觉不够好"这类主观话）
```

报告的"可核对"体现在三处：

1. **每条数据可溯源**——覆盖率来自哪个命令的输出，复核者能重跑验证；
2. **未达标项写差距数字**——"还差 4.9 个百分点"而非"还有提升空间"；
3. **未测量项显式标注**——诚实暴露盲区，而不是假装全绿。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 300" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_q5" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_q5"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="300" fill="url(#bg_q5)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">质量门禁报告：一张表说清全部</text>
  <rect x="60" y="50" width="150" height="36" rx="6" fill="#1e293b" stroke="#334155"/>
  <text x="135" y="73" text-anchor="middle" fill="#94a3b8" font-size="11">检查项</text>
  <rect x="210" y="50" width="120" height="36" rx="6" fill="#1e293b" stroke="#334155"/>
  <text x="270" y="73" text-anchor="middle" fill="#94a3b8" font-size="11">实测</text>
  <rect x="330" y="50" width="120" height="36" rx="6" fill="#1e293b" stroke="#334155"/>
  <text x="390" y="73" text-anchor="middle" fill="#94a3b8" font-size="11">目标</text>
  <rect x="450" y="50" width="120" height="36" rx="6" fill="#1e293b" stroke="#334155"/>
  <text x="510" y="73" text-anchor="middle" fill="#94a3b8" font-size="11">状态</text>
  <rect x="60" y="96" width="150" height="30" rx="6" fill="#0f172a"/>
  <text x="135" y="116" text-anchor="middle" fill="#e2e8f0" font-size="10">单元覆盖率</text>
  <rect x="210" y="96" width="120" height="30" rx="6" fill="#0f172a"/>
  <text x="270" y="116" text-anchor="middle" fill="#e2e8f0" font-size="10">82.3%</text>
  <rect x="330" y="96" width="120" height="30" rx="6" fill="#0f172a"/>
  <text x="390" y="116" text-anchor="middle" fill="#e2e8f0" font-size="10">≥80%</text>
  <rect x="450" y="96" width="120" height="30" rx="6" fill="#22c55e" opacity="0.15"/>
  <text x="510" y="116" text-anchor="middle" fill="#86efac" font-size="11">🟢</text>
  <rect x="60" y="132" width="150" height="30" rx="6" fill="#0f172a"/>
  <text x="135" y="152" text-anchor="middle" fill="#e2e8f0" font-size="10">flow→test 映射</text>
  <rect x="210" y="132" width="120" height="30" rx="6" fill="#0f172a"/>
  <text x="270" y="152" text-anchor="middle" fill="#e2e8f0" font-size="10">4/5</text>
  <rect x="330" y="132" width="120" height="30" rx="6" fill="#0f172a"/>
  <text x="390" y="152" text-anchor="middle" fill="#e2e8f0" font-size="10">全覆盖</text>
  <rect x="450" y="132" width="120" height="30" rx="6" fill="#ef4444" opacity="0.15"/>
  <text x="510" y="152" text-anchor="middle" fill="#fca5a5" font-size="11">🔴</text>
  <text x="400" y="210" text-anchor="middle" fill="#475569" font-size="11">每项数据可溯源 → 复核者可重跑验证</text>
  <text x="400" y="238" text-anchor="middle" fill="#64748b" font-size="10">未达标写差距数字，不写"感觉不够好"</text>
  <text x="400" y="262" text-anchor="middle" fill="#64748b" font-size="10">未测量项显式标注 ⚪，不假装全绿</text>
  <text x="400" y="286" text-anchor="middle" fill="#64748b" font-size="10">报告的角色：把"质量"做成可审计的凭证</text>
</svg>
```

### Step 5: 人签字放行

最后一步也是本技能与其他技能最大的不同：**把报告呈现给人类，请其明确签字**。

- **放行**：人类勾选"我（人类）确认以上数据属实，允许进入下一步"；
- **拒绝**：人类勾选"我拒绝放行"并给理由，按理由退回对应阶段（编码/补测试/改目标），并记录到报告。

**AI 不得代替人类勾选放行**。未获签字 → 状态保持在 `ci`，不进入 `verifying`。

这条约束为什么存在？因为"放行"是**责任转移**——签字意味着人类对"这份数据我核实过、这个质量我接受"负责。AI 可以代跑检查、代写报告，但**不能代担责任**。若 AI 自己就能放行，整个质量闸门就变成了"AI 自己检查自己"——那和人人都给自己写合格证没有区别。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 240" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_q6" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_q6"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="240" fill="url(#bg_q6)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">人签字：责任转移的一步</text>
  <rect x="60" y="50" width="330" height="120" rx="10" fill="#22c55e" opacity="0.08" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_q6)"/>
  <text x="225" y="76" text-anchor="middle" fill="#86efac" font-size="13" font-weight="700">AI 的权限边界</text>
  <text x="225" y="104" text-anchor="middle" fill="#94a3b8" font-size="10">✓ 跑检查 · 测覆盖 · 出报告</text>
  <text x="225" y="126" text-anchor="middle" fill="#94a3b8" font-size="10">✓ 标差距 · 标未测量</text>
  <text x="225" y="150" text-anchor="middle" fill="#64748b" font-size="9">✗ 不判达标 · 不代签字</text>
  <rect x="420" y="50" width="330" height="120" rx="10" fill="#3b82f6" opacity="0.08" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_q6)"/>
  <text x="585" y="76" text-anchor="middle" fill="#93c5fd" font-size="13" font-weight="700">放行 = 责任转移</text>
  <text x="585" y="104" text-anchor="middle" fill="#94a3b8" font-size="10">签字 = "数据我核实过、质量我接受"</text>
  <text x="585" y="126" text-anchor="middle" fill="#94a3b8" font-size="10">未签字 → 状态停在 ci，不进 verifying</text>
  <text x="585" y="150" text-anchor="middle" fill="#64748b" font-size="9">拒绝 → 按理由退回对应阶段并记录</text>
  <text x="400" y="196" text-anchor="middle" fill="#475569" font-size="11">AI 可以代跑检查、代写报告，但不能代担责任</text>
  <text x="400" y="220" text-anchor="middle" fill="#64748b" font-size="10">否则质量闸门 = AI 自己检查自己，无人负责</text>
</svg>
```

## 四、与相邻技能边界

| 场景 | 归属 |
|------|------|
| 机械化跑全量门禁 | `/unit-test-ci` |
| 代码双轴评审（Spec/Standards） | `/expert-reviewer` |
| 质量判定 + 报告 + 人签字 | **本技能**（`/harness-quality`） |
| 测试地基（命令真跑验证） | `/harness-test-setup` 纪律（apply-harness 已内置） |

- **与 unit-test-ci**：它是执行者，跑门禁；本技能是判定者，出报告请人签字。本技能可以依赖它产出的绿，但"绿"不等于"放行"。
- **与 expert-reviewer**：它审代码（Spec 是否匹配需求、Standards 是否合规）；本技能审质量指标（覆盖率、lint、flow→test）。一个看代码内在质量，一个看外在度量——两个视角不重叠。
- **与 apply-harness**：命令真源地基由 apply-harness 内置的测试地基纪律保证——本技能假定"命令能跑"这个前提已由它垫好。

## 五、完成标志

`/harness-quality` 的完成标志：

1. **报告已落盘** `.harness/changes/<id>/quality-report.md`，全部数据来自真实运行；
2. **flow→test 映射已逐条对账**，漂移已红灯；
3. **已获得人类签字**（放行或拒绝），未代签。

注意第三条的"或拒绝"——被拒绝也是完成，因为**本技能的产出是"决策所需的证据"，不是"放行的结果"**。人类拒绝放行、给出理由，本技能就完成了它的使命：把质量事实摆清楚，把决定权交还人类。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 220" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_q7" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_q7"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="220" fill="url(#bg_q7)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">完成标志：被拒绝也是完成</text>
  <rect x="40" y="50" width="230" height="90" rx="10" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_q7)"/>
  <text x="155" y="76" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">① 报告落盘</text>
  <text x="155" y="100" text-anchor="middle" fill="#94a3b8" font-size="10">全部数据来自真实运行</text>
  <text x="155" y="120" text-anchor="middle" fill="#64748b" font-size="9">quality-report.md</text>
  <rect x="290" y="50" width="230" height="90" rx="10" fill="#3b82f6" opacity="0.10" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_q7)"/>
  <text x="405" y="76" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">② 对账完成</text>
  <text x="405" y="100" text-anchor="middle" fill="#94a3b8" font-size="10">flow→test 逐条对账</text>
  <text x="405" y="120" text-anchor="middle" fill="#64748b" font-size="9">漂移已红灯</text>
  <rect x="540" y="50" width="220" height="90" rx="10" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_q7)"/>
  <text x="650" y="76" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">③ 人已签字</text>
  <text x="650" y="100" text-anchor="middle" fill="#94a3b8" font-size="10">放行或拒绝都算</text>
  <text x="650" y="120" text-anchor="middle" fill="#64748b" font-size="9">AI 绝不代签</text>
  <text x="400" y="172" text-anchor="middle" fill="#475569" font-size="11">产出是"决策所需的证据"，不是"放行的结果"</text>
  <text x="400" y="196" text-anchor="middle" fill="#64748b" font-size="10">把质量事实摆清楚，把决定权交还人类——被拒绝一样是成功</text>
</svg>
```

## 六、写在最后

`/harness-quality` 的全部设计，浓缩成四句话：

1. **真实数据是唯一的语言**——覆盖率用工具量、lint 看退出码、flow→test 逐条对账；绝不用"感觉"冒充"测得"。
2. **量不到就写"未测量"**——诚实的红灯胜过编造的绿灯，假数据会骗过签字的人。
3. **AI 量，人定标**——目标值是团队拍板、人类可调；AI 只把数字摆出来，不代替人类决定 60% 就是合格。
4. **放行必须人签字**——签字是责任转移，AI 可以代跑、代写报告，不能代担责任。

一句话记住它：**/harness-quality 是质量闸门——它用真实数据把"质量达标了吗"变成可核对的对账表，然后把放行这支笔，永远留在人类手里。**