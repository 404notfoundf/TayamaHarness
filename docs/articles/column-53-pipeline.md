# 6 阶段流水线：从一句话意图到交付——严格按序、不可跳步、步步留档

> 思想类专栏 · 第 53 篇 · 约 9000 字 · 6 个 SVG 图

---

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 340" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_p1" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_p1"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="340" fill="url(#bg_p1)" rx="10"/>
  <text x="400" y="28" text-anchor="middle" fill="#e2e8f0" font-size="24" font-weight="700">6 阶段流水线</text>
  <text x="400" y="50" text-anchor="middle" fill="#64748b" font-size="11">人类意图 → 交付 · 每一步都对照 .harness/rules/ 校验</text>
  <rect x="25" y="70" width="115" height="140" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_p1)"/>
  <text x="82" y="95" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">① harnessing</text>
  <text x="82" y="118" text-anchor="middle" fill="#94a3b8" font-size="9">规格构建</text>
  <text x="82" y="138" text-anchor="middle" fill="#94a3b8" font-size="8">规格卡+AC+边界</text>
  <text x="82" y="158" text-anchor="middle" fill="#94a3b8" font-size="8">+设计约束+测试策略</text>
  <rect x="150" y="70" width="115" height="140" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_p1)"/>
  <text x="207" y="95" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">② coding-skill</text>
  <text x="207" y="118" text-anchor="middle" fill="#94a3b8" font-size="9">测试先行实现</text>
  <text x="207" y="138" text-anchor="middle" fill="#94a3b8" font-size="8">失败测试+最小实现</text>
  <rect x="275" y="70" width="115" height="140" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_p1)"/>
  <text x="332" y="95" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">③ unit-test-write</text>
  <text x="332" y="118" text-anchor="middle" fill="#94a3b8" font-size="9">测试完善</text>
  <text x="332" y="138" text-anchor="middle" fill="#94a3b8" font-size="8">覆盖率≥80%</text>
  <rect x="400" y="70" width="115" height="140" rx="8" fill="#a855f7" opacity="0.12" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_p1)"/>
  <text x="457" y="95" text-anchor="middle" fill="#d8b4fe" font-size="12" font-weight="700">④ expert-reviewer</text>
  <text x="457" y="118" text-anchor="middle" fill="#94a3b8" font-size="9">专家评审</text>
  <text x="457" y="138" text-anchor="middle" fill="#94a3b8" font-size="8">0 个🔴才放行</text>
  <rect x="525" y="70" width="115" height="140" rx="8" fill="#ef4444" opacity="0.12" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_p1)"/>
  <text x="582" y="95" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">⑤ unit-test-ci</text>
  <text x="582" y="118" text-anchor="middle" fill="#94a3b8" font-size="9">CI 门禁</text>
  <text x="582" y="138" text-anchor="middle" fill="#94a3b8" font-size="8">四道门禁全绿</text>
  <rect x="650" y="70" width="115" height="140" rx="8" fill="#0ea5e9" opacity="0.12" stroke="#0ea5e9" stroke-width="1.5" filter="url(#sh_p1)"/>
  <text x="707" y="95" text-anchor="middle" fill="#7dd3fc" font-size="12" font-weight="700">⑥ deploy-verify</text>
  <text x="707" y="118" text-anchor="middle" fill="#94a3b8" font-size="9">部署验证</text>
  <text x="707" y="138" text-anchor="middle" fill="#94a3b8" font-size="8">冒烟+回滚预案</text>
  <text x="400" y="245" text-anchor="middle" fill="#475569" font-size="12">每个阶段对应 .harness/skills/ 下的一个技能，严格按序，不可跳步</text>
  <rect x="100" y="262" width="600" height="55" rx="8" fill="#1e293b" opacity="0.9" stroke="#64748b" stroke-width="1.5" filter="url(#sh_p1)"/>
  <text x="400" y="285" text-anchor="middle" fill="#94a3b8" font-size="10">.harness/changes/&lt;id&gt;/ 全程留档 · 状态机驱动</text>
  <text x="400" y="305" text-anchor="middle" fill="#64748b" font-size="9">每步的输入输出都有明确的落点和门禁</text>
</svg>
```

## 一、为什么要有一条流水线

### 1.1 没有流程的 AI 协作是什么样

在没有流水线约束时，AI 协作的典型轨迹是这样的：

1. 用户说一句话需求；
2. AI 直接开始写代码；
3. 写完用户发现理解偏了；
4. AI 重写，再偏一点；
5. 代码能跑了，但没有测试，没有评审，没有门禁；
6. 上线后出问题，谁都不知道问题出在哪一步。

这个轨迹的问题不在 AI 的能力，而在**流程的缺失**：**需求没理清就开始写，测试没写就认为完成，评审没做就敢上线**。

### 1.2 流水线的本质：把质量责任从"记忆"交给"流程"

6 阶段流水线要解决的是：**怎么让每一步都做对、可验证、可追溯**。

它的答案不是靠人记得，而是靠流程卡住——

| 想跳过的 | 被什么拦住 |
|---------|-----------|
| 不写规格直接写代码 | 阶段 ① 没产出规格卡，无法进入 ② |
| 不写测试就说完成 | 阶段 ③ 覆盖率不达标，无法进入 ④ |
| 评审有问题照样上线 | 阶段 ④ 有 🔴 即打回修改，不放行 |
| CI 没跑照样部署 | 阶段 ⑤ 门禁不全绿，无法进入 ⑥ |

**流水线的每一步都是一个检查点，检查不过，流程不走**——质量责任从"AI 记得要自律"变成"流程保证不可能跳过"。

## 二、阶段总览：六步六产出

| 阶段 | 技能 | 输入 | 产出 | 出口门禁 | 核心原则 |
|------|------|------|------|---------|---------|
| ① 需求分析 | `harnessing` | 一句话意图 | 规格卡 + AC + 设计约束 + 测试策略 | 规格明确、AC 可测试、≥3 边界、测试策略清晰 | 先搞清楚做什么 |
| ② 编码实现 | `coding-skill` | 已确认的规格卡 | 失败测试 + 最小实现 | 失败测试先存在并转绿、构建/lint 通过 | 先写失败测试，垂直切片 |
| ③ 单元测试 | `unit-test-write` | 实现代码 + change.md | 完整测试套件 | 测试通过、覆盖率 ≥80%、覆盖全部 AC 与边界 | 每条 AC 至少一个测试 |
| ④ 专家评审 | `expert-reviewer` | 代码 + 单元测试 | review.md | 0 个 🔴 严重问题 | 逐项审查，0 个 🔴 才放行 |
| ⑤ CI 门禁 | `unit-test-ci` | 通过评审的变更 | 门禁结果报告 | 所有 CI 阶段全绿 | 机械化执行 |
| ⑥ 部署验证 | `deploy-verify` | 通过 CI 的产物 | 验证报告 | 主链路验证通过、回滚预案就绪 | 部署后验证，坏了能回滚 |

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 300" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_p2" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_p2"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="300" fill="url(#bg_p2)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">六步六产出 · 步步有门禁</text>
  <rect x="40" y="50" width="340" height="65" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_p2)"/>
  <text x="210" y="72" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">① 需求分析 → 规格卡</text>
  <text x="210" y="98" text-anchor="middle" fill="#94a3b8" font-size="9">门禁：AC 可测试 · ≥3 边界 · 测试策略清晰</text>
  <rect x="420" y="50" width="340" height="65" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_p2)"/>
  <text x="590" y="72" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">② 编码实现 → 最小实现</text>
  <text x="590" y="98" text-anchor="middle" fill="#94a3b8" font-size="9">门禁：失败测试先存在并转绿 · 构建/lint 通过</text>
  <rect x="40" y="130" width="340" height="65" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_p2)"/>
  <text x="210" y="152" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">③ 单元测试 → 测试套件</text>
  <text x="210" y="178" text-anchor="middle" fill="#94a3b8" font-size="9">门禁：覆盖率 ≥80% · 全部 AC 与边界</text>
  <rect x="420" y="130" width="340" height="65" rx="8" fill="#a855f7" opacity="0.12" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_p2)"/>
  <text x="590" y="152" text-anchor="middle" fill="#d8b4fe" font-size="12" font-weight="700">④ 专家评审 → review.md</text>
  <text x="590" y="178" text-anchor="middle" fill="#94a3b8" font-size="9">门禁：0 个 🔴 严重问题</text>
  <rect x="40" y="210" width="340" height="65" rx="8" fill="#ef4444" opacity="0.12" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_p2)"/>
  <text x="210" y="232" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">⑤ CI 门禁 → 门禁报告</text>
  <text x="210" y="258" text-anchor="middle" fill="#94a3b8" font-size="9">门禁：编译/静态/架构/测试 全绿</text>
  <rect x="420" y="210" width="340" height="65" rx="8" fill="#0ea5e9" opacity="0.12" stroke="#0ea5e9" stroke-width="1.5" filter="url(#sh_p2)"/>
  <text x="590" y="232" text-anchor="middle" fill="#7dd3fc" font-size="12" font-weight="700">⑥ 部署验证 → 验证报告</text>
  <text x="590" y="258" text-anchor="middle" fill="#94a3b8" font-size="9">门禁：主链路验证通过 · 回滚预案就绪</text>
</svg>
```

## 三、六个阶段的逐一拆解

### ① 需求分析 — `harnessing`

| 属性 | 说明 |
|------|------|
| **输入** | 一句话意图 |
| **产出** | 规格卡 + 验收标准 + 设计约束 + 测试策略 |
| **出口门禁** | 规格明确、AC 可测试、≥3 边界情况、测试策略清晰 |
| **核心原则** | 先搞清楚"做什么"，再想"怎么做" |

这一阶段回答的是最贵的问题：**需求到底是什么**。一句话意图进入，规格卡出来——AC 可测试、边界 ≥3、测试策略清晰。它是整个流水线的地基：规格错了，后面五步全错；规格对了，后面五步有基准。

### ② 编码实现 — `coding-skill`

| 属性 | 说明 |
|------|------|
| **输入** | 已确认的规格卡 |
| **产出** | 失败测试 + 最小实现 |
| **出口门禁** | 失败测试先存在并转绿、构建/lint 通过、符合编码规范 |
| **核心原则** | 先写失败测试，再做最小实现，垂直切片 |

这一阶段回答"怎么把规格变成代码"。铁律是**先写失败测试**——测试先于实现存在，证明需求可测；然后最小实现让它转绿，不猜未来需求。垂直切片保证每一个循环都是完整的 Red-Green-Refactor。

### ③ 单元测试编写 — `unit-test-write`

| 属性 | 说明 |
|------|------|
| **输入** | ② 阶段的实现代码 + change.md |
| **产出** | 完整测试套件 |
| **出口门禁** | 测试通过、核心逻辑覆盖率 ≥80%、覆盖全部 AC 与边界 |
| **核心原则** | 每条 AC 至少一个测试，每条降级路径一个专门测试 |

这一阶段把"测试先行"补成"测试完整"。② 阶段有核心路径的失败测试，③ 阶段补全边界、降级路径、异常分支——覆盖率 ≥80% 是硬门槛，覆盖全部 AC 与边界是硬要求。测试不是点缀，是需求的证明。

### ④ 专家评审 — `expert-reviewer`

| 属性 | 说明 |
|------|------|
| **输入** | 代码 + 单元测试 |
| **产出** | `.harness/changes/<id>/review.md` |
| **出口门禁** | 0 个 🔴 严重问题 |
| **核心原则** | 不手下留情，逐项审查，0 个 🔴 才放行 |

这一阶段引入**独立视角**。写代码的 AI 和评审的 AI 是两码事——写的人容易对隐藏假设熟视无睹，评审的人逐项对照规格卡和规范。双轴并行：Spec 需求匹配 + Standards 规范合规，任一维度出现 🔴，打回修改。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 240" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_p3" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_p3"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="240" fill="url(#bg_p3)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">评审双轴：Spec × Standards</text>
  <rect x="100" y="55" width="250" height="65" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_p3)"/>
  <text x="225" y="80" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">Spec 需求匹配</text>
  <text x="225" y="104" text-anchor="middle" fill="#94a3b8" font-size="9">每条 AC 是否都被实现？</text>
  <rect x="450" y="55" width="250" height="65" rx="8" fill="#a855f7" opacity="0.12" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_p3)"/>
  <text x="575" y="80" text-anchor="middle" fill="#d8b4fe" font-size="12" font-weight="700">Standards 规范合规</text>
  <text x="575" y="104" text-anchor="middle" fill="#94a3b8" font-size="9">编码规范/工程结构是否守约？</text>
  <text x="400" y="160" text-anchor="middle" fill="#fca5a5" font-size="13" font-weight="700">任一轴出现 🔴 → 打回修改 → 重新评审</text>
  <text x="400" y="190" text-anchor="middle" fill="#94a3b8" font-size="10">0 个 🔴 才放行，评审才有意义</text>
  <text x="400" y="218" text-anchor="middle" fill="#64748b" font-size="9">写的人看不到的隐藏假设，评审的人逐项找到</text>
</svg>
```

### ⑤ CI 门禁 — `unit-test-ci`

| 属性 | 说明 |
|------|------|
| **输入** | 通过 ④ 评审的完整变更 |
| **产出** | 门禁结果报告 |
| **出口门禁** | 所有 CI 阶段全绿 |
| **核心原则** | 机械化执行：编译 → 静态分析 → 架构约束 → 测试 → 安全扫描 |

这一阶段回答"怎么客观证明没问题"。评审有主观成分，CI 没有——编译结果、静态分析告警、架构约束违规、测试失败，都是不可争辩的客观数据。门禁是四道：编译、静态分析、架构约束、全量测试。机械化执行意味着：**同样的变更，任何人在任何时候跑，结果都一样**。

### ⑥ 部署验证 — `deploy-verify`

| 属性 | 说明 |
|------|------|
| **输入** | 通过 ⑤ 门禁的构建产物 |
| **产出** | 部署验证报告 |
| **出口门禁** | 主链路冒烟通过、健康检查通过、回滚预案就绪 |
| **核心原则** | 部署后必须验证，坏了必须能回滚 |

这一阶段回答"上线后真的能用吗"。部署 ≠ 完成，部署 + 验证 = 完成。主链路冒烟、健康检查、关键链路验证——全过才算交付；任何一步失败，回滚预案立即执行。

## 四、为什么"严格按序、不可跳步"是铁律

### 4.1 每阶段的产出是下一阶段的输入

流水线各阶段之间不是平行关系，而是**依赖关系**：

- 规格卡是编码的输入——没有规格卡，编码无从对照；
- 失败测试是实现的输入——没有失败测试，实现没有目标；
- 代码是测试的输入——没有代码，测试不知道测什么；
- 代码+测试是评审的输入——没有这些，评审无米下锅；
- 通过评审的变更是 CI 的输入——没有评审，CI 验证的是未确认的东西；
- 通过 CI 的产物是部署的输入——没有 CI，部署的是未验证的东西。

**跳步的本质是让后面的阶段在"未确认的输入"上工作**——评审没过的代码上 CI，等于把检查交给机器却跳过了人的把关；CI 没跑就部署，等于把未验证的产物推上线。跳步不是省时间，是把风险往后挪。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 260" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_p4" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_p4"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="260" fill="url(#bg_p4)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">依赖链：前一步的产出是后一步的输入</text>
  <rect x="30" y="55" width="110" height="55" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_p4)"/>
  <text x="85" y="77" text-anchor="middle" fill="#93c5fd" font-size="11" font-weight="700">规格卡</text>
  <text x="85" y="99" text-anchor="middle" fill="#64748b" font-size="8">① 产出</text>
  <text x="145" y="82" text-anchor="middle" fill="#64748b" font-size="12">→</text>
  <rect x="152" y="55" width="110" height="55" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_p4)"/>
  <text x="207" y="77" text-anchor="middle" fill="#86efac" font-size="11" font-weight="700">最小实现</text>
  <text x="207" y="99" text-anchor="middle" fill="#64748b" font-size="8">② 产出</text>
  <text x="267" y="82" text-anchor="middle" fill="#64748b" font-size="12">→</text>
  <rect x="274" y="55" width="110" height="55" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_p4)"/>
  <text x="329" y="77" text-anchor="middle" fill="#fde68a" font-size="11" font-weight="700">测试套件</text>
  <text x="329" y="99" text-anchor="middle" fill="#64748b" font-size="8">③ 产出</text>
  <text x="389" y="82" text-anchor="middle" fill="#64748b" font-size="12">→</text>
  <rect x="396" y="55" width="110" height="55" rx="8" fill="#a855f7" opacity="0.12" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_p4)"/>
  <text x="451" y="77" text-anchor="middle" fill="#d8b4fe" font-size="11" font-weight="700">review.md</text>
  <text x="451" y="99" text-anchor="middle" fill="#64748b" font-size="8">④ 产出</text>
  <text x="511" y="82" text-anchor="middle" fill="#64748b" font-size="12">→</text>
  <rect x="518" y="55" width="110" height="55" rx="8" fill="#ef4444" opacity="0.12" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_p4)"/>
  <text x="573" y="77" text-anchor="middle" fill="#fca5a5" font-size="11" font-weight="700">门禁报告</text>
  <text x="573" y="99" text-anchor="middle" fill="#64748b" font-size="8">⑤ 产出</text>
  <text x="633" y="82" text-anchor="middle" fill="#64748b" font-size="12">→</text>
  <rect x="640" y="55" width="130" height="55" rx="8" fill="#0ea5e9" opacity="0.12" stroke="#0ea5e9" stroke-width="1.5" filter="url(#sh_p4)"/>
  <text x="705" y="77" text-anchor="middle" fill="#7dd3fc" font-size="11" font-weight="700">验证报告</text>
  <text x="705" y="99" text-anchor="middle" fill="#64748b" font-size="8">⑥ 产出 → 交付</text>
  <rect x="120" y="140" width="560" height="55" rx="8" fill="#ef4444" opacity="0.12" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_p4)"/>
  <text x="400" y="165" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">跳步 = 在未确认的输入上工作 = 把风险往后挪</text>
  <text x="400" y="187" text-anchor="middle" fill="#94a3b8" font-size="9">评审没过的代码上 CI = 跳过人为把关；CI 没跑就部署 = 未验证产物上线</text>
  <text x="400" y="228" text-anchor="middle" fill="#475569" font-size="10">门禁不是阻碍速度，是避免"返工一整条链"</text>
</svg>
```

### 4.2 状态机驱动：变更卡全程留档

流水线的每一步都落在 `.harness/changes/<id>/` 下，状态机驱动流转。变更从创建到完成，经历一系列状态：

- 规格确认前：drafting（草拟）→ reviewing（评审规格）→ approved（确认）；
- 编码测试阶段：coding（编码）→ testing（测试）；
- 评审阶段：reviewing（代码评审）→ 打回循环；
- 门禁部署阶段：ci（门禁）→ verifying（部署验证）→ done（完成）。

**留档的意义**：任何一次变更，都能从 `.harness/changes/<id>/` 里看到它走过的每一步——规格卡、代码、测试、评审报告、门禁结果、验证报告都在。三个月后回看，能精确回答"这个功能当初怎么做的"。

## 五、与 SDD-TDD 和 Owner Agent 的关系

### 5.1 流水线是方法论的执行环境

| 理念 | 在流水线里的落点 |
|------|----------------|
| SDD（先写规格） | 阶段 ① harnessing 产出规格卡 |
| TDD（先写测试） | 阶段 ② coding-skill 先写失败测试 |
| 测试完善 | 阶段 ③ unit-test-write 覆盖率 ≥80% |
| 独立把关 | 阶段 ④ expert-reviewer 0 个 🔴 放行 |
| 客观验证 | 阶段 ⑤ unit-test-ci 全绿门禁 |
| 部署验证 | 阶段 ⑥ deploy-verify 冒烟+回滚 |

SDD-TDD 回答"做什么、怎么写对、怎么证明"，流水线回答"按什么顺序做完这一切"。**方法论给流程提供标准，流程给方法论提供骨架**。

### 5.2 Owner Agent 是流水线的调度者

Owner Agent 持有身份、规则、上下文，在流水线每个阶段扮演调度者和检查者：

- 编排：一句话需求进来，按阶段逐步推进，产出落位到 `.harness/changes/<id>/`；
- 裁决：每个阶段对照 `.harness/rules/` 校验输出，违规即停；
- 上下文：术语、规则、身份全程持有，不随对话丢失。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 260" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_p5" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_p5"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="260" fill="url(#bg_p5)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">Owner Agent 与流水线的关系</text>
  <rect x="330" y="50" width="140" height="55" rx="8" fill="#a855f7" opacity="0.12" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_p5)"/>
  <text x="400" y="73" text-anchor="middle" fill="#d8b4fe" font-size="12" font-weight="700">Owner Agent</text>
  <text x="400" y="95" text-anchor="middle" fill="#94a3b8" font-size="9">调度者 + 检查者</text>
  <text x="400" y="135" text-anchor="middle" fill="#64748b" font-size="12">▼ 编排 ▼</text>
  <rect x="60" y="155" width="100" height="45" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_p5)"/>
  <text x="110" y="182" text-anchor="middle" fill="#93c5fd" font-size="11" font-weight="700">harnessing</text>
  <rect x="170" y="155" width="100" height="45" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_p5)"/>
  <text x="220" y="182" text-anchor="middle" fill="#86efac" font-size="11" font-weight="700">coding-skill</text>
  <rect x="280" y="155" width="100" height="45" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_p5)"/>
  <text x="330" y="182" text-anchor="middle" fill="#fde68a" font-size="11" font-weight="700">test-write</text>
  <rect x="390" y="155" width="100" height="45" rx="8" fill="#a855f7" opacity="0.12" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_p5)"/>
  <text x="440" y="182" text-anchor="middle" fill="#d8b4fe" font-size="11" font-weight="700">reviewer</text>
  <rect x="500" y="155" width="100" height="45" rx="8" fill="#ef4444" opacity="0.12" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_p5)"/>
  <text x="550" y="182" text-anchor="middle" fill="#fca5a5" font-size="11" font-weight="700">unit-test-ci</text>
  <rect x="610" y="155" width="100" height="45" rx="8" fill="#0ea5e9" opacity="0.12" stroke="#0ea5e9" stroke-width="1.5" filter="url(#sh_p5)"/>
  <text x="660" y="182" text-anchor="middle" fill="#7dd3fc" font-size="11" font-weight="700">deploy-verify</text>
  <text x="400" y="235" text-anchor="middle" fill="#475569" font-size="10">身份/规则/上下文全程持有 · 每一步对照规则校验 · 违规即停</text>
</svg>
```

## 六、完成标志与边界

### 6.1 完成标志

一条变更真正走完流水线，必须满足：

1. **六阶段全走通、无跳步**——从 harnessing 到 deploy-verify，每阶段有产出、有门禁、有留档；
2. **`.harness/changes/<id>/` 留档完整**——规格卡、代码、测试、评审报告、门禁结果、验证报告都在；
3. **门禁全绿且真实**——评审 0 个 🔴、CI 全绿、部署冒烟通过；
4. **回滚预案就绪**——不可逆操作的每一步都有回滚方案。

### 6.2 边界：流水线不管什么

- 不规定具体语言/框架——任何语言都能映射到这 6 步；
- 不规定测试框架——JUnit、pytest、go test 皆可，门禁只看纪律；
- 不替代业务决策——规格要用户确认，放行要按流程；
- 流水线管"质量流程"，不管"业务怎么设计"。

## 七、写在最后

6 阶段流水线的全部设计，浓缩成四句话：

1. **先理清后动手**——harnessing 产出规格卡，AC 可测试、边界 ≥3。
2. **先测试后实现**——coding-skill 失败测试先行，最小实现转绿。
3. **先评审后门禁**——expert-reviewer 0 个 🔴 放行，unit-test-ci 机械化全绿。
4. **先验证后交付**——deploy-verify 冒烟+健康检查+回滚预案，坏了能回滚。

一句话记住它：**6 阶段流水线是开发质量的"六道闸门"——harnessing 定规格、coding-skill 写实现、unit-test-write 补测试、expert-reviewer 把关评审、unit-test-ci 跑门禁、deploy-verify 验部署，严格按序、不可跳步、步步留档，让每一次变更从"一句话意图"变成"可验证、可追溯、可回滚的交付"。**