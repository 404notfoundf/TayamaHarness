# 第十七章 · 自定义 Skill：把领域知识封装成 AI 的能力

> 一个 Skill 的本质，是一份"如何应对某类任务"的操作手册。
> 它不替 AI 思考，而是给 AI 装上"经验"——把团队踩过的坑、沉淀的规范、验证过的流程，变成可重复调用的能力。
>
> —— 项目信条：**知识不沉淀就是负债，沉淀成 Skill 才是资产。**

---

## 一、Skill 是什么

### 1.1 从"提示词"到"技能"

AI 协作的演进，本质上是"如何把经验传给 AI"的演进：

| 阶段 | 方式 | 问题 |
|------|------|------|
| 第一代 | 聊天里手写提示词 | 每次都要重新写，随用随丢 |
| 第二代 | 项目文档（AGENTS.md / 规则文件） | 会被读，但 AI 不知道"何时用" |
| 第三代 | Skill / 斜杠命令 | 按需调用、结构化、可复用、可版本化 |

**Skill = 结构化的操作手册 + 触发入口**。

### 1.2 SKILL.md 的真实结构

原文档给了一个简化骨架，但真实的 `SKILL.md` 要丰富得多。以流水线技能 `coding-skill` 为例，它的完整结构是：

```markdown
---
name: coding-skill{{LANG_TAG}}
stage: ② 编码实现
description: 按已确认的需求卡与编码规范，小步实现可编译的 {{LANGUAGE}} 代码
---

# 编码实现技能（coding-skill）— {{LANGUAGE}} 版

> **流水线阶段**: ② 第二步
> **输入**: 已审批的 `.harness/changes/<id>/change.md`
> **出口门禁**: 失败测试先存在且已转绿 · `{{BUILD_CMD}}` 通过 · 符合编码规范

### 核心规则（一句话摘要）
> 先写失败测试 → 最小实现通过 → 测试保护下重构。一次只做一个循环。

## 1. 职责
你是 AI 程序员。按已审批的规格卡精确实现，不越界、不自作主张。

## 2. 前置检查           ← 状态机：确保当前上下文合法
1. 定位目标 change（扫描 change.md，过滤 status: coding）
2. 加载规则文件 + 相关 wiki
3. 缺前置 → 退回 ① harnessing

## 3. 工作流程           ← Step 序列，每步有输入→动作→输出
### Step 1: 构造实现上下文
### Step 2: 确认实现位置
### Step 3: TDD 小循环（Red → Green → Refactor）

## 4. 约束               ← ❌ 禁止 / ✅ 必须
- ❌ 禁止跳过失败测试直接写核心逻辑
- ✅ 破坏性操作必须用 --dry-run 预览

## 5. 异常处理           ← 不是所有事都顺利
- 设计偏差 → 停下，给 2-3 个替代方案
- 术语冲突 → 调用 domain-modeling 技能

## 6. 完成标志           ← 状态流转 + 进入下一步
更新 change.md 状态 coding → testing，进入 ③ 单测编写。
```

一个合格的 `SKILL.md` 有 **七个组成部分**，每个都有明确职责：

| 部分 | 作用 | 缺失的后果 |
|------|------|-----------|
| frontmatter | 声明身份（名字、阶段、触发条件） | AI 不知道何时调用、在流水线哪里 |
| 一句话信条 | 压缩整个技能的灵魂到一句话 | AI 抓不住重点，把流程当选项 |
| 职责定义 | "你是谁、做什么、不做什么" | AI 角色漂移，越界发挥 |
| 前置检查 | 验证上下文合法才开工 | 在错误的状态下执行，产生垃圾 |
| 工作流程 | Step 序列，每步有输入→动作→输出 | AI 自由发挥，步骤随意 |
| 约束/门禁 | ❌ 禁止 + ✅ 必须 + ⚠️ 注意 | 无安全边界，自由发挥 |
| 完成标志 | 怎么知道自己做完了 + 状态流转 | 做不完就停，或做完不知道进下一步 |

### 1.3 frontmatter 的字段

对全仓库 SKILL.md 做一次实际统计，frontmatter 只出现了**四种字段**，而且使用频率悬殊：

| 字段 | 出现频率 | 作用 | 设计建议 |
|------|---------|------|---------|
| `name` | **100%**（所有技能） | 斜杠命令名 | 小写、连字符连接，如 `coding-skill` |
| `description` | **100%**（所有技能） | AI 自动调用的判断依据 | 场景-动作式，说清"何时用、解决什么" |
| `stage` | **大多数**（流水线 + 工具箱技能） | 流水线阶段标识 | 罗马序号 + 阶段名，如 `② 编码实现` |
| `disable-model-invocation` | **仅 4 个**（`apply-harness`、`arch-review`、`handoff`、`harness-me`） | 是否禁止 AI 自动调用 | 仅副作用大、应强制手动触发的技能设 `true` |

> **`name` 和 `description` 是必填项**——没有它们 AI 既不知道怎么调用、也不知道何时调用。`stage` 是大多数技能的选择项，`disable-model-invocation` 是极少数技能的例外项。

> **`stage` 为什么重要**：流水线技能通过 `stage` 字段声明自己在开发流程中的位置。`expert-reviewer` 标注 `④ 专家评审`，意味着它的前置是 `③ unit-test-write`，出口是 `⑤ unit-test-ci`。AI 读了 `stage` 就知道整个流水线的拓扑，不需要额外的流程图。工具箱技能则统一标注 `stage: 组件封装`，表明它们不参与流水线顺序，但会被 `expert-reviewer` 按代码特征自动加载。

> **`disable-model-invocation` 何时用**：全仓库只有 4 个技能设了它——`apply-harness`（会改写整个项目结构）、`arch-review`（会全量扫描代码库）、`handoff`（会压缩上下文写文件）、`harness-me`（是交互式对话入口）。共同点是**副作用大、应由人主动发起**。大多数技能（包括 `coding-skill`、`expert-reviewer`）不设这个字段，因为它们可以由流水线自动推进、或由其他技能按条件触发。

对于**参数化模板技能**，`name` 和 `description` 里的 `{{PLACEHOLDER}}` 会在渲染时被替换：

```yaml
# 模板源码（harness-core/skills/coding-skill/SKILL.md）
name: coding-skill{{LANG_TAG}}
description: 按已确认的需求卡与编码规范，小步实现可编译的 {{LANGUAGE}} 代码

# 渲染后（.harness/skills/golang/coding-skill/SKILL.md）
name: coding-skill-golang
description: 按已确认的需求卡与编码规范，小步实现可编译的 Go 代码
```

> `{{LANG_TAG}}` → `-golang`、`{{LANGUAGE}}` → `Go`、`{{BUILD_CMD}}` → `go build ./...`。参数化机制让一份模板服务五种语言，详见 §2.3。

### 1.4 四种技能类型与结构差异

不是所有 `SKILL.md` 长一个样。这个代码库里实际存在**四种技能类型**，它们的结构有显著差异：

```svg
<svg xmlns="http://www.w3.org/2000/svg" width="920" height="360" viewBox="0 0 920 360">
  <defs>
    <linearGradient id="bg17b" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
  </defs>
  <rect width="920" height="360" fill="url(#bg17b)"/>
  <text x="460" y="30" text-anchor="middle" fill="#e2e8f0" font-size="16" font-weight="700">四种技能类型的结构差异</text>

  <!-- 流水线技能 -->
  <rect x="30" y="50" width="210" height="290" rx="10" fill="#0ea5e9" opacity="0.08" stroke="#38bdf8" stroke-width="1.5"/>
  <text x="135" y="78" text-anchor="middle" fill="#7dd3fc" font-size="13" font-weight="700">流水线技能</text>
  <text x="135" y="98" text-anchor="middle" fill="#64748b" font-size="10">coding-skill / expert-reviewer</text>
  <text x="40" y="125" fill="#94a3b8" font-size="10">✓ stage 字段（流水线位置）</text>
  <text x="40" y="145" fill="#94a3b8" font-size="10">✓ 前置检查（状态机）</text>
  <text x="40" y="165" fill="#94a3b8" font-size="10">✓ 出口门禁</text>
  <text x="40" y="185" fill="#94a3b8" font-size="10">✓ 完成标志→状态流转</text>
  <text x="40" y="205" fill="#94a3b8" font-size="10">✓ {{PLACEHOLDER}} 模板化</text>
  <text x="40" y="235" fill="#fde68a" font-size="10">→ 生成代码 + 测试</text>
  <text x="40" y="255" fill="#fde68a" font-size="10">→ 推动流水线前进</text>
  <text x="40" y="280" fill="#64748b" font-size="9">部署在 .harness/skills/{lang}/</text>
  <text x="40" y="300" fill="#64748b" font-size="9">由 apply-harness 渲染生成</text>
  <text x="40" y="325" fill="#38bdf8" font-size="10" font-weight="700">10 个</text>

  <!-- 工具箱技能 -->
  <rect x="265" y="50" width="210" height="290" rx="10" fill="#8b5cf6" opacity="0.08" stroke="#a78bfa" stroke-width="1.5"/>
  <text x="370" y="78" text-anchor="middle" fill="#c4b5fd" font-size="13" font-weight="700">工具箱技能</text>
  <text x="370" y="98" text-anchor="middle" fill="#64748b" font-size="10">redis-cache-wrapper / logging-toolkit</text>
  <text x="275" y="125" fill="#94a3b8" font-size="10">✓ 触发方式表（独立+自动）</text>
  <text x="275" y="145" fill="#94a3b8" font-size="10">✓ 子命令（cache/lock/all）</text>
  <text x="275" y="165" fill="#94a3b8" font-size="10">✓ 输出文件清单</text>
  <text x="275" y="185" fill="#94a3b8" font-size="10">✓ 质量门禁（组件级）</text>
  <text x="275" y="205" fill="#94a3b8" font-size="10">✓ 约束（不做什么）</text>
  <text x="275" y="235" fill="#fde68a" font-size="10">→ 生成基础设施代码</text>
  <text x="275" y="255" fill="#fde68a" font-size="10">→ 被 expert-reviewer 自动加载</text>
  <text x="275" y="280" fill="#64748b" font-size="9">部署在 .harness/skills/common/</text>
  <text x="275" y="300" fill="#64748b" font-size="9">跨语言通用，不参数化</text>
  <text x="275" y="325" fill="#a78bfa" font-size="10" font-weight="700">12 个</text>

  <!-- 辅助技能 -->
  <rect x="500" y="50" width="210" height="290" rx="10" fill="#f59e0b" opacity="0.08" stroke="#fbbf24" stroke-width="1.5"/>
  <text x="605" y="78" text-anchor="middle" fill="#fde68a" font-size="13" font-weight="700">辅助技能</text>
  <text x="605" y="98" text-anchor="middle" fill="#64748b" font-size="10">harnessing / diagnosing-bugs / handoff</text>
  <text x="510" y="125" fill="#94a3b8" font-size="10">✓ 部分设 disable-model</text>
  <text x="510" y="145" fill="#94a3b8" font-size="10">✓ 问题库 / Phase 序列</text>
  <text x="510" y="165" fill="#94a3b8" font-size="10">✓ 输出物（卡片/报告/文档）</text>
  <text x="510" y="185" fill="#94a3b8" font-size="10">✓ {{PLACEHOLDER}} 模板化</text>
  <text x="510" y="205" fill="#94a3b8" font-size="10">✓ 与流水线技能联动</text>
  <text x="510" y="235" fill="#fde68a" font-size="10">→ 打磨需求 / 诊断 Bug</text>
  <text x="510" y="255" fill="#fde68a" font-size="10">→ 上下文交接</text>
  <text x="510" y="280" fill="#64748b" font-size="9">部署在 .harness/skills/{lang}/</text>
  <text x="510" y="300" fill="#64748b" font-size="9">多由用户主动发起</text>
  <text x="510" y="325" fill="#fbbf24" font-size="10" font-weight="700">5 个</text>

  <!-- 通用技能 -->
  <rect x="735" y="50" width="175" height="290" rx="10" fill="#10b981" opacity="0.08" stroke="#34d399" stroke-width="1.5"/>
  <text x="822" y="78" text-anchor="middle" fill="#6ee7b7" font-size="13" font-weight="700">通用技能</text>
  <text x="822" y="98" text-anchor="middle" fill="#64748b" font-size="10">domain-modeling / research</text>
  <text x="745" y="125" fill="#94a3b8" font-size="10">✓ 无 {{PLACEHOLDER}}</text>
  <text x="745" y="145" fill="#94a3b8" font-size="10">✓ 无 stage</text>
  <text x="745" y="165" fill="#94a3b8" font-size="10">✓ 精简结构</text>
  <text x="745" y="185" fill="#94a3b8" font-size="10">✓ 主动动作描述</text>
  <text x="745" y="205" fill="#94a3b8" font-size="10">✓ 完成标志</text>
  <text x="745" y="235" fill="#fde68a" font-size="10">→ 维护领域语言</text>
  <text x="745" y="255" fill="#fde68a" font-size="10">→ 外部事实查证</text>
  <text x="745" y="280" fill="#64748b" font-size="9">部署在 .harness/skills/common/</text>
  <text x="745" y="300" fill="#64748b" font-size="9">跨语言直接复制</text>
  <text x="745" y="325" fill="#34d399" font-size="10" font-weight="700">3 个</text>
</svg>
```

四种类型的差异对照：

| 维度 | 流水线技能 | 工具箱技能 | 辅助技能 | 通用技能 |
|------|-----------|-----------|---------|---------|
| 代表 | `coding-skill` | `redis-cache-wrapper` | `harnessing` | `domain-modeling` |
| `stage` | ✅ 有 | ✅ `组件封装` | ✅ 有 | ❌ 无 |
| 模板化 | ✅ `{{PLACEHOLDER}}` | ❌ 纯通用 | ✅ `{{PLACEHOLDER}}` | ❌ 无占位符 |
| 状态机 | ✅ 前置检查 + 流转 | ❌ 无 | ❌ 无 | ❌ 无 |
| 触发 | 流水线自动推进 | 独立命令 + 自动检测 | 多由用户主动发起 | 被其他技能调用 |
| 产出 | 代码 + 测试 | 基础设施代码 | 卡片 / 报告 / 文档 | CONTEXT.md / ADR / wiki |
| 数量 | 10 个 | 12 个 | 5 个 | 3 个 |

> **设计启示**：写 Skill 前先想清楚它属于哪一类——流水线技能需要状态机和门禁，工具箱技能需要触发方式表和文件清单，辅助技能需要问题库或 Phase 序列，通用技能要足够精简。用错结构会让 AI 执行时找不到关键信息。

---

## 二、什么值得做成 Skill

### 2.1 判断标准：三个"以上"

不是任何知识都值得封装成 Skill。判断标准：**一个以上的人、在一个以上的项目、遇到一个以上的重复问题**。

| 领域知识 | 是否值得 | 理由 |
|---------|---------|------|
| 团队代码规范 | ✅ | 每个项目都要守，AI 需要随时知道 |
| 部署检查清单 | ✅ | 每次发布都要过一遍 |
| 一次性的 hack | ❌ | 用一次就扔，封装反而增加维护负担 |
| 与产品强耦合的业务知识 | ⚠️ | 沉淀到 CONTEXT.md（领域模型）更合适 |
| 需要人判断的任务 | ⚠️ | 让 Skill 给"判断框架"，不要给"结论" |

### 2.2 Skill 与规则文件、领域模型的分工

```svg
<svg xmlns="http://www.w3.org/2000/svg" width="860" height="200" viewBox="0 0 860 200">
  <defs>
    <linearGradient id="bg17" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
  </defs>
  <rect width="860" height="200" fill="url(#bg17)"/>
  <text x="140" y="28" text-anchor="middle" fill="#7dd3fc" font-size="13" font-weight="700">rules/（规则文件）</text>
  <text x="430" y="28" text-anchor="middle" fill="#c4b5fd" font-size="13" font-weight="700">CONTEXT.md（领域模型）</text>
  <text x="720" y="28" text-anchor="middle" fill="#6ee7b7" font-size="13" font-weight="700">Skill（技能）</text>

  <rect x="40" y="42" width="200" height="115" rx="10" fill="#0ea5e9" opacity="0.1" stroke="#38bdf8" stroke-width="1.5"/>
  <text x="140" y="70" text-anchor="middle" fill="#e2e8f0" font-size="12">静态约束</text>
  <text x="140" y="94" text-anchor="middle" fill="#94a3b8" font-size="11">编码规范</text>
  <text x="140" y="116" text-anchor="middle" fill="#94a3b8" font-size="11">工程结构</text>
  <text x="140" y="138" text-anchor="middle" fill="#94a3b8" font-size="11">可靠性要求</text>
  <text x="140" y="160" text-anchor="middle" fill="#64748b" font-size="10">"必须/禁止"</text>

  <text x="140" y="188" text-anchor="middle" fill="#64748b" font-size="11">守规矩</text>

  <rect x="320" y="42" width="220" height="115" rx="10" fill="#8b5cf6" opacity="0.1" stroke="#a78bfa" stroke-width="1.5"/>
  <text x="430" y="70" text-anchor="middle" fill="#e2e8f0" font-size="12">术语与架构认知</text>
  <text x="430" y="94" text-anchor="middle" fill="#94a3b8" font-size="11">User / Order 定义</text>
  <text x="430" y="116" text-anchor="middle" fill="#94a3b8" font-size="11">领域规则</text>
  <text x="430" y="138" text-anchor="middle" fill="#94a3b8" font-size="11">架构决策</text>
  <text x="430" y="160" text-anchor="middle" fill="#64748b" font-size="10">"是什么"</text>

  <text x="430" y="188" text-anchor="middle" fill="#64748b" font-size="11">知背景</text>

  <rect x="620" y="42" width="200" height="115" rx="10" fill="#10b981" opacity="0.1" stroke="#34d399" stroke-width="1.5"/>
  <text x="720" y="70" text-anchor="middle" fill="#e2e8f0" font-size="12">操作流程与经验</text>
  <text x="720" y="94" text-anchor="middle" fill="#94a3b8" font-size="11">如何写单测</text>
  <text x="720" y="116" text-anchor="middle" fill="#94a3b8" font-size="11">如何做架构体检</text>
  <text x="720" y="138" text-anchor="middle" fill="#94a3b8" font-size="11">如何交接</text>
  <text x="720" y="160" text-anchor="middle" fill="#64748b" font-size="10">"怎么做"</text>

  <text x="720" y="188" text-anchor="middle" fill="#64748b" font-size="11">会干活</text>
</svg>
```

**一个完整的 Harness 项目，三者缺一不可。** 更关键的是——三者之间有**引用关系**，不是各自独立：

- **规则文件是单一真相源**。`coding-skill` 的工作流程 Step 2 写的是"对照 `工程结构.md` 确定模块归属"，而不是把工程结构抄一遍。Skill 引用规则，不复制规则。
- **CONTEXT.md 是领域语言的活词典**。`domain-modeling` 技能负责"改变"它，其他技能负责"消费"它。`coding-skill` 发现代码术语与 CONTEXT.md 矛盾时，调用 `domain-modeling` 修正——而不是自己改。
- **Skill 是"怎么做"的操作手册**。它引用前两者，把它们变成可执行的流程。

> **反例**：如果 `coding-skill` 把编码规范抄进自己的 SKILL.md，规则文件改了但 Skill 没改——两边漂移，AI 执行时到底听谁的？正确做法是 Skill 里写"符合 `.harness/rules/编码规范.md`"，让规则文件保持唯一真相源地位。

### 2.3 参数化模板：一份源码，多语言复用

这是这个代码库最精巧的设计之一。10 个流水线技能的**核心逻辑是跨语言通用的**（先写失败测试、双轴审查、6 阶段 Bug 诊断……），差异只在命令和工具名上。如果把它们为每种语言写一份，五种语言 × 10 个技能 = 50 份 SKILL.md，维护噩梦。

**解决方案：模板 + `{{PLACEHOLDER}}`**。一份模板用占位符表达语言差异，`apply-harness` 在初始化项目时按检测结果替换占位符：

```yaml
# 模板源码（harness-core/skills/coding-skill/SKILL.md）
name: coding-skill{{LANG_TAG}}
description: 按已确认的需求卡与编码规范，小步实现可编译的 {{LANGUAGE}} 代码
---
> 出口门禁: 失败测试先存在且已转绿 · `{{BUILD_CMD}}` 通过

# 渲染为 Go 版（.harness/skills/golang/coding-skill/SKILL.md）
name: coding-skill-golang
description: 按已确认的需求卡与编码规范，小步实现可编译的 Go 代码
---
> 出口门禁: 失败测试先存在且已转绿 · `go build ./...` 通过

# 渲染为 Java 版（.harness/skills/java/coding-skill/SKILL.md）
name: coding-skill-java
description: 按已确认的需求卡与编码规范，小步实现可编译的 Java 代码
---
> 出口门禁: 失败测试先存在且已转绿 · `mvn compile` 通过
```

完整的占位符表（节选）：

| 占位符 | Go 值 | Java 值 | 说明 |
|--------|-------|---------|------|
| `{{LANG_TAG}}` | `-golang` | `-java` | 技能名后缀，保证斜杠命令不冲突 |
| `{{LANGUAGE}}` | `Go` | `Java` | 出现在 description 和正文 |
| `{{BUILD_CMD}}` | `go build ./...` | `mvn compile` | 编译命令 |
| `{{TEST_CMD}}` | `go test ./...` | `mvn test` | 测试命令 |
| `{{LINT_CMD}}` | `golangci-lint run` | `checkstyle:check` | 规范检查 |
| `{{DOCSTYLE}}` | `godoc` | `Javadoc` | 文档注释风格 |
| `{{TEST_FRAMEWORK}}` | `go test + testify` | `JUnit 5 + Mockito` | 测试框架 |
| `{{RACE_DETECT_ARG}}` | `-race` | `空` | 竞态检测参数 |

> **设计原则**：**逻辑通用 → 模板化**；**工具不同 → 占位符化**；**逻辑本身不同 → 语言包专属技能**。例如 `spring-api-convention` 是 Java 专属（依赖 Spring 注解），不做成模板，直接放在 `harness-java/skills/` 里。而 `redis-cache-wrapper` 的缓存逻辑跨语言通用，放在 `harness-core/skills/common/` 直接复制。

---

## 三、写一个 Skill 的五个步骤

### 3.1 Step 1：定义"何时用"

先回答：**用户说哪句话、遇到什么场景，应该触发这个 Skill？**

这一步决定 `description` 的写法。好的 description 是"场景-动作"式——对照真实代码里的写法：

```
❌ "加密解密工具"                    ← 太抽象，AI 不知道何时调用
✅ "当需要为用户的密码、API Key 做加解密时使用（提供 AES/RSA 封装与规范）"
```

真实代码里的合格 description 例子：

| 技能 | description | 为什么好 |
|------|------------|---------|
| `coding-skill` | "按已确认的需求卡与编码规范，小步实现可编译的代码" | 说了前置（已确认的需求卡）+ 动作（小步实现） |
| `diagnosing-bugs` | "严谨的 Bug 诊断流程——先建立稳定复现的反馈循环，再分析根因。**适用于当用户报告'这个出错/崩溃/性能慢'时**" | 给了触发场景（用户报告 Bug）+ 核心方法论 |
| `expert-reviewer` | "双轴并行代码审查（Spec 需求匹配 + Standards 规范合规），0 个严重问题方可放行" | 说了方法（双轴）+ 放行标准（0 严重） |
| `domain-modeling` | "主动构建和维护项目的领域模型。当用户要敲定领域术语、记录架构决策，或其他技能需要维护领域模型时使用" | 说了"何时用"（敲定术语/记录决策/被其他技能调用） |

> **模式提炼**：好的 description = **触发场景 + 核心动作 + 关键约束**。"`diagnosing-bugs`" 的 description 三者俱全——"当用户报告 Bug 时"（场景）+"先建立反馈循环再分析根因"（动作）+"稳定复现"（约束）。

### 3.2 Step 2：提炼流程

把"老手做这事的心智过程"拆成 Step。每个 Step 遵循**输入 → 动作 → 输出**。但真实代码告诉我们，流程之前还有一个常被忽略的关键部分——**前置检查**。

流水线技能的 `coding-skill`、`unit-test-write`、`expert-reviewer`、`deploy-verify` 全部以"前置检查"开头，它的作用是**状态机守卫**——确保在合法上下文里才开工：

```markdown
## 2. 前置检查

1. **定位目标 change**（按变更定位规则）：
   - 扫描 `.harness/changes/*/change.md`，过滤 `status: coding`
   - 用户已指定 `<id>` → 校验该 change 状态是否为 `coding`，否则报错
   - 恰好 1 个 → 自动选中
   - 0 个 → 报错：无处于 `coding` 状态的 change，退回 ① harnessing
   - ≥ 2 个 → **列出候选清单（id + 标题 + 摘要），停下请用户选择**，
            不得擅自默认取第一个
2. 加载上下文：规则文件 + 相关 wiki
3. 缺前置 → 退回 ① harnessing
```

这个模式有四个要点：

1. **状态过滤**：只处理指定状态的 change，防止在错误阶段执行
2. **零个报错退回**：没有处于正确状态的 change，说明流水线没走到这一步，报错并指明退回哪里
3. **多个不自动选**：≥2 个候选时**列出清单请用户选**，这是防止 AI 擅自做决策的安全阀门
4. **加载规则**：明确列出要读哪些规则文件，不靠 AI 自己猜

前置检查之后，才是真正的 Step 序列：

```
Step 1: 输入（需求卡 + 规则）→ 动作（构造上下文）→ 输出：实现上下文
Step 2: 输入（上下文）→ 动作（确认实现位置）→ 输出：模块/文件归属
Step 3: 输入（AC + 边界）→ 动作（TDD 小循环）→ 输出：代码 + 测试
```

> **为什么前置检查是灵魂**：没有它的 Skill，AI 可能在"没有需求卡"的情况下就开始编码，或者在"代码还没写"的情况下就开始审查——产出一堆幻觉。前置检查是"开工许可证"。

### 3.3 Step 3：写约束

约束是 Skill 的灵魂——**没有约束的 Skill 会自由发挥**。真实代码里的约束不是笼统的"不要做什么"，而是**三个等级**：

```markdown
## 4. 约束

- ❌ 禁止跳过失败测试直接写核心逻辑              ← 硬禁止：安全边界
- ❌ 禁止批量写所有测试再写实现                    ← 硬禁止：方法论红线
- ❌ 禁止引入未评审的依赖                         ← 硬禁止：架构红线
- ✅ 破坏性操作必须使用 --dry-run / -WhatIf 预览   ← 强制必须：质量门禁
- ✅ 涉及数据库变更必须先输出 SQL 让用户确认       ← 强制必须：人审关卡
- ✅ 每个公共符号有 godoc 注释                    ← 强制必须：规范要求
- ⚠️ 核心业务逻辑遵循 Red → Green → Refactor      ← 注意：默认路径，可偏离
```

三个等级的区别：

| 等级 | 符号 | 含义 | 违反后果 |
|------|------|------|---------|
| 硬禁止 | `❌ 禁止` | 安全/方法论红线，绝对不做 | 代码审查直接打回 |
| 强制必须 | `✅ 必须` | 质量门禁，必须满足才能算完成 | 出口门禁拦住 |
| 注意 | `⚠️` | 默认路径，有充分理由可偏离 | 需说明原因 |

工具箱技能的约束更具体，直接针对组件的实现细节：

```markdown
# redis-cache-wrapper 的约束
- ❌ 禁止在业务代码中直接操作 Redis 客户端   ← 必须经过 CacheService
- ❌ 禁止缓存没有 TTL                        ← 防内存泄漏
- ❌ 禁止分布式锁释放用 if-else 代替 Lua       ← 原子性要求
- ❌ 不生成缓存监控面板代码                    ← 职责边界
- ❌ 不修改已有业务代码                        ← 只新增不破坏
```

> **约束要具体到实现细节**。"`❌ 禁止分布式锁释放用 if-else 代替 Lua`"比"`❌ 注意并发安全`"有效得多——前者告诉 AI 具体不能用什么、要用什么替代；后者只是笼统提醒，AI 照样可能用 if-else。

### 3.4 Step 4：设计输出物与出口门禁

每个 Skill 应该有一个**可验证的输出物**（卡片、报告、清单）。但真实代码更进一步——流水线技能还有**出口门禁**，即"满足什么条件才算做完"：

```markdown
> **出口门禁**: 失败测试先存在且已转绿 · `go build ./...` 通过 · 符合编码规范
```

出口门禁与输出物的关系：

| 技能 | 输出物 | 出口门禁 | 完成后流转 |
|------|--------|---------|-----------|
| `coding-skill` | 代码 + 测试 | 失败测试先行 + 编译通过 | `coding → testing` |
| `unit-test-write` | 测试代码 | 覆盖率 ≥80% + 竞态检测通过 | `testing → reviewing` |
| `expert-reviewer` | `.harness/changes/<id>/review.md` | 0 个 🔴 严重问题 | `reviewing → ci` |
| `deploy-verify` | `.harness/changes/<id>/verify.md` | 冒烟 + 健康检查 + 回滚预案 | `verifying → done` |
| `harnessing` | 需求总结卡片 | AC 列出 + 边界覆盖 | `analyzing → coding` |

> **输出物 vs 出口门禁**：输出物是"产出什么"（review.md、verify.md），出口门禁是"产出要满足什么才算数"（0 个严重问题、覆盖率 ≥80%）。没有出口门禁的输出物只是"交了个差"，有门禁才有质量保证。

输出物还有一个设计原则：**要落地到文件，不要只留在对话里**。`expert-reviewer` 的约束写明"无论审查结果如何，**必须先完成**将完整报告写入 `.harness/changes/<id>/review.md`"——报告是落地的、可追溯的、下一个技能可以读取的。

### 3.5 Step 5：写验收/自检清单与状态流转

好的 Skill 末尾应有"我怎么知道自己做完了"。真实代码里的完成标志有两层：

**第一层：检查清单**（本技能内部的自检）：

```markdown
## 6. 完成标志

失败测试已先行、核心实现可编译且符合规范后，更新 `change.md`
状态 `coding → testing`，进入 ③ 单测编写。
```

```markdown
## 完成标志（diagnosing-bugs 的 Phase 6）
- [ ] 原始场景不再复现（重新运行 Phase 1 循环）
- [ ] 回归测试通过（或记录找不到正确接缝）
- [ ] 所有 [DEBUG-...] 探针已移除
- [ ] 正确的假设写入 commit message
```

**第二层：状态流转**（流水线级别的交接）：

```svg
<svg xmlns="http://www.w3.org/2000/svg" width="860" height="120" viewBox="0 0 860 120">
  <defs>
    <linearGradient id="bg17c" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arr17c" markerWidth="8" markerHeight="8" refX="7" refY="4" orient="auto">
      <path d="M 0 0 L 8 4 L 0 8 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="860" height="120" fill="url(#bg17c)"/>

  <rect x="20" y="40" width="100" height="40" rx="8" fill="#f59e0b" opacity="0.12" stroke="#fbbf24" stroke-width="1.5"/>
  <text x="70" y="58" text-anchor="middle" fill="#fde68a" font-size="10" font-weight="700">① harnessing</text>
  <text x="70" y="74" text-anchor="middle" fill="#94a3b8" font-size="9">需求拷问</text>
  <text x="70" y="98" text-anchor="middle" fill="#64748b" font-size="8">status: analyzing</text>

  <line x1="120" y1="60" x2="145" y2="60" stroke="#475569" stroke-width="1.5" marker-end="url(#arr17c)"/>

  <rect x="150" y="40" width="100" height="40" rx="8" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.5"/>
  <text x="200" y="58" text-anchor="middle" fill="#7dd3fc" font-size="10" font-weight="700">② coding-skill</text>
  <text x="200" y="74" text-anchor="middle" fill="#94a3b8" font-size="9">编码实现</text>
  <text x="200" y="98" text-anchor="middle" fill="#64748b" font-size="8">status: coding</text>

  <line x1="250" y1="60" x2="275" y2="60" stroke="#475569" stroke-width="1.5" marker-end="url(#arr17c)"/>

  <rect x="280" y="40" width="100" height="40" rx="8" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.5"/>
  <text x="330" y="58" text-anchor="middle" fill="#7dd3fc" font-size="10" font-weight="700">③ unit-test-write</text>
  <text x="330" y="74" text-anchor="middle" fill="#94a3b8" font-size="9">单测编写</text>
  <text x="330" y="98" text-anchor="middle" fill="#64748b" font-size="8">status: testing</text>

  <line x1="380" y1="60" x2="405" y2="60" stroke="#475569" stroke-width="1.5" marker-end="url(#arr17c)"/>

  <rect x="410" y="40" width="100" height="40" rx="8" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.5"/>
  <text x="460" y="58" text-anchor="middle" fill="#7dd3fc" font-size="10" font-weight="700">④ expert-reviewer</text>
  <text x="460" y="74" text-anchor="middle" fill="#94a3b8" font-size="9">专家评审</text>
  <text x="460" y="98" text-anchor="middle" fill="#64748b" font-size="8">status: reviewing</text>

  <line x1="510" y1="60" x2="535" y2="60" stroke="#475569" stroke-width="1.5" marker-end="url(#arr17c)"/>

  <rect x="540" y="40" width="100" height="40" rx="8" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.5"/>
  <text x="590" y="58" text-anchor="middle" fill="#7dd3fc" font-size="10" font-weight="700">⑤ unit-test-ci</text>
  <text x="590" y="74" text-anchor="middle" fill="#94a3b8" font-size="9">CI 门禁</text>
  <text x="590" y="98" text-anchor="middle" fill="#64748b" font-size="8">status: ci</text>

  <line x1="640" y1="60" x2="665" y2="60" stroke="#475569" stroke-width="1.5" marker-end="url(#arr17c)"/>

  <rect x="670" y="40" width="100" height="40" rx="8" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.5"/>
  <text x="720" y="58" text-anchor="middle" fill="#7dd3fc" font-size="10" font-weight="700">⑥ deploy-verify</text>
  <text x="720" y="74" text-anchor="middle" fill="#94a3b8" font-size="9">部署验证</text>
  <text x="720" y="98" text-anchor="middle" fill="#64748b" font-size="8">status: verifying</text>

  <line x1="770" y1="60" x2="795" y2="60" stroke="#475569" stroke-width="1.5" marker-end="url(#arr17c)"/>

  <rect x="800" y="40" width="50" height="40" rx="8" fill="#10b981" opacity="0.12" stroke="#34d399" stroke-width="1.5"/>
  <text x="825" y="58" text-anchor="middle" fill="#6ee7b7" font-size="10" font-weight="700">done</text>
  <text x="825" y="74" text-anchor="middle" fill="#94a3b8" font-size="9">交付</text>
  <text x="825" y="98" text-anchor="middle" fill="#64748b" font-size="8">status: done</text>
</svg>
```

每个流水线技能的"完成标志"都包含两件事：**更新 change.md 状态** + **进入下一阶段**。这让技能之间形成自动交接——`coding-skill` 做完不只是一个信号，而是把状态从 `coding` 改成 `testing`，下一个技能 `unit-test-write` 的前置检查自动发现它。

### 3.6 Step 6：设计联动

真实代码里的 Skill 不是孤岛，它们有明确的**联动关系**。设计 Skill 时要想清楚它跟谁接力：

```markdown
## 5. 异常处理（coding-skill）

### 设计偏差
实现发现需求卡假设不成立 → 停下，描述"假设 X 但实际 Y"，
给 2-3 个替代方案，等人类决策。

### 依赖阻塞
外部资源不可用 → 写 Mock/Stub 实现核心逻辑，
在 change.md 登记"替换 Mock"任务，继续。

### 领域术语冲突
实现时发现代码中的术语与 CONTEXT.md 不一致 →
调用 `domain-modeling` 技能：精化术语、更新 CONTEXT.md。

### 难以复现的 Bug
如果遇到无法稳定复现的 Bug → 建议运行 `/diagnosing-bugs`
进行 6 阶段诊断。
```

这个"异常处理"部分定义了三种联动：

| 异常场景 | 联动技能 | 联动方式 |
|---------|---------|---------|
| 术语冲突 | `domain-modeling` | 调用它修正 CONTEXT.md，然后继续编码 |
| 难以复现的 Bug | `diagnosing-bugs` | 建议用户调用，诊断结果写入 `diagnosis.md` |
| 架构阻碍 Bug 被锁定 | `arch-review` | `diagnosing-bugs` 的 Phase 6 交接给它 |

更精巧的联动是 `expert-reviewer` 的**领域技能自动加载**——它根据代码特征自动引入工具箱技能作为审查维度：

```markdown
## 领域技能加载（混合模式）

| 检测条件 | 加载技能 |
|---------|---------|
| RedisTemplate / Jedis 调用 | `redis-cache-wrapper`（缓存检查清单） |
| `V*__*.sql` / `migrations/*.sql` | `database-migration-toolkit`（迁移检查） |
| 消息队列生产者/消费者 | `kafka-toolkit` / `rocketmq-toolkit` |
| HTTP 客户端调用 | `http-client-toolkit`（连接池/超时/重试） |
| 日志配置/日志打印代码 | `logging-toolkit`（traceId/脱敏） |
| 安全敏感模式（密钥/注入/鉴权） | `security-toolkit`（脱敏/加密/校验） |
```

> **联动设计的本质**：Skill 不是独立的函数，而是有调用图的模块。设计时要回答三个问题——**谁会调用我**（上游）、**我会调用谁**（下游）、**什么条件下触发联动**（触发条件）。

---

## 四、安装与注册：让你的 Skill 生效

### 4.1 技能目录的结构

Skill 的"源"放在 `.harness/skills/`，分为语言特有和跨语言通用两区：

```
.harness/skills/
├── {lang}/                 ← 语言特有技能（流水线 + 辅助 + 语言专属）
│   ├── harnessing/SKILL.md
│   ├── coding-skill/SKILL.md      ← 模板渲染后的版本
│   ├── expert-reviewer/SKILL.md
│   ├── spring-api-convention/SKILL.md  ← 语言专属（非模板）
│   └── ...
└── common/                 ← 跨语言通用技能（工具箱 + 通用）
    ├── domain-modeling/SKILL.md
    ├── research/SKILL.md
    ├── redis-cache-wrapper/SKILL.md
    ├── logging-toolkit/SKILL.md
    └── ...
```

> **分区原则**：`{lang}/` 放需要参数化渲染的流水线技能和语言专属技能；`common/` 放跨语言通用的工具箱技能和通用技能。`apply-harness` 负责把模板渲染到 `{lang}/`，把通用技能直接复制到 `common/`。

### 4.2 为什么需要"安装"而不是"放在那里"

**不同 AI 工具扫描不同的技能目录**。这是国产工具时代最容易被忽视的坑：

```svg
<svg xmlns="http://www.w3.org/2000/svg" width="960" height="400" viewBox="0 0 960 400">
  <defs>
    <linearGradient id="bg17" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arr17" markerWidth="10" markerHeight="10" refX="9" refY="5" orient="auto">
      <path d="M 0 0 L 10 5 L 0 10 z" fill="#64748b"/>
    </marker>
  </defs>
  <rect width="960" height="400" fill="url(#bg17)"/>
  <text x="480" y="34" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">Skill 源码 → 工具目录的映射</text>

  <rect x="40" y="80" width="260" height="120" rx="12" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.5"/>
  <text x="170" y="108" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">Skill 源码（单一真相源）</text>
  <text x="170" y="135" text-anchor="middle" fill="#94a3b8" font-size="11">.harness/skills/{lang}/</text>
  <text x="170" y="155" text-anchor="middle" fill="#94a3b8" font-size="11">+ .harness/skills/common/</text>
  <text x="170" y="175" text-anchor="middle" fill="#94a3b8" font-size="11">/apply-harness 渲染生成</text>
  <text x="170" y="195" text-anchor="middle" fill="#64748b" font-size="10">拷贝/渲染后由 install-skill 注册</text>

  <line x1="300" y1="140" x2="340" y2="140" stroke="#475569" stroke-width="2" marker-end="url(#arr17)"/>

  <rect x="350" y="60" width="180" height="70" rx="10" fill="#8b5cf6" opacity="0.12" stroke="#a78bfa" stroke-width="1.5"/>
  <text x="440" y="88" text-anchor="middle" fill="#c4b5fd" font-size="13" font-weight="700">Reasonix</text>
  <text x="440" y="110" text-anchor="middle" fill="#94a3b8" font-size="10">.reasonix/skills/</text>

  <rect x="350" y="145" width="180" height="70" rx="10" fill="#8b5cf6" opacity="0.12" stroke="#a78bfa" stroke-width="1.5"/>
  <text x="440" y="173" text-anchor="middle" fill="#c4b5fd" font-size="13" font-weight="700">Claude Code</text>
  <text x="440" y="195" text-anchor="middle" fill="#94a3b8" font-size="10">.claude/skills/</text>

  <rect x="350" y="230" width="180" height="70" rx="10" fill="#8b5cf6" opacity="0.12" stroke="#a78bfa" stroke-width="1.5"/>
  <text x="440" y="258" text-anchor="middle" fill="#c4b5fd" font-size="13" font-weight="700">Cursor</text>
  <text x="440" y="280" text-anchor="middle" fill="#94a3b8" font-size="10">.cursor/skills/</text>

  <rect x="350" y="315" width="180" height="70" rx="10" fill="#8b5cf6" opacity="0.12" stroke="#a78bfa" stroke-width="1.5"/>
  <text x="440" y="343" text-anchor="middle" fill="#c4b5fd" font-size="13" font-weight="700">Codex</text>
  <text x="440" y="365" text-anchor="middle" fill="#94a3b8" font-size="10">.codex/skills/</text>

  <rect x="560" y="60" width="360" height="325" rx="12" fill="#1e293b" opacity="0.6" stroke="#334155" stroke-width="1"/>
  <text x="740" y="88" text-anchor="middle" fill="#e2e8f0" font-size="14" font-weight="700">非原生工具（部分兼容）</text>
  <text x="580" y="115" fill="#94a3b8" font-size="11">Windsurf → .windsurf/workflows/</text>
  <text x="580" y="140" fill="#94a3b8" font-size="11">OpenCode → .opencode/commands/</text>
  <text x="580" y="165" fill="#94a3b8" font-size="11">Trae → .trae/rules/</text>
  <text x="580" y="190" fill="#94a3b8" font-size="11">Tabnine → .tabnine/guidelines.md</text>
  <text x="580" y="215" fill="#94a3b8" font-size="11">Copilot → .github/skills/</text>
  <text x="580" y="240" fill="#94a3b8" font-size="11">Continue → .continue/skills/</text>
  <text x="580" y="275" fill="#fde68a" font-size="11">⚠️ 格式不统一：SKILL.md ≠ 各工具原生格式</text>
  <text x="580" y="300" fill="#fde68a" font-size="11">install-skill 负责：检测 → 创建目录 → 复制/转换</text>
  <text x="580" y="330" fill="#94a3b8" font-size="11">✅ 原生工具直接复制 SKILL.md</text>
  <text x="580" y="355" fill="#94a3b8" font-size="11">⚠️ 非原生工具尝试转换或提示手动配置</text>

  <text x="480" y="392" text-anchor="middle" fill="#64748b" font-size="10">// 同一个技能源码，映射到不同工具的目录 —— 这就是 install-skill 存在的原因</text>
</svg>
```

兼容性分四级（基于真实代码的 19+ 工具支持表）：

| 级别 | 工具 | 安装方式 | 斜杠命令 |
|------|------|---------|---------|
| 原生 | Reasonix、Claude Code、Cline、Cursor、Codex、Qoder | 直接复制 SKILL.md | 立即可用 |
| 标准 | VS Code Agent Skills（1.98+） | 复制到 `.vscode/agent-skills/` | 部分支持 |
| 需转换 | Windsurf、OpenCode、Trae、Tabnine | 转为对应格式 | 格式不同 |
| 待确认 | CodeBuddy、通义灵码、CodeGeeX、Cody、MarsCode | 尝试复制，提示用户 | 不确定 |

### 4.3 关键教训：目录不存在也要创建

**最常见的故障**：AI 检测到了项目（比如 Reasonix），但 `.reasonix/skills/` 目录从未被创建——斜杠命令全部无法识别。

install-skill 的处理原则：

```
检测到工具 → 目标目录不存在？ → mkdir -p 创建 → 复制技能 → 验证
```

**永远不要因为"目录不存在"就跳过安装**——目录不存在恰恰说明更需要安装。真实代码里 `apply-harness` 的 Step 5.5 明确标注："本步骤是必需步骤，非可选。若检测失败或目录不存在，**必须创建目录**并注册技能。"

---

## 五、一个完整示例：写一个"日志规范检查"Skill

用实际例子走一遍五个步骤（含联动），并对照真实代码的标准。

### 5.1 定义（frontmatter + 信条）

```markdown
---
name: log-audit
description: 当需要检查项目中日志的使用是否符合规范时使用（结构化字段、级别选择、敏感信息脱敏），生成分级报告。
disable-model-invocation: true  ← 可选：审计会读遍代码库，应手动触发
---

# 日志规范检查（log-audit）

> 一句话信条：**只审计，不改代码。每条问题带文件位置与行号，规则来源可追溯。**
```

对照真实代码的 `logging-toolkit` 技能——它的 description 是"日志工具封装——MDC traceId 自动注入、日志脱敏（配置化规则）、动态日志级别……"，场景和动作都说了。`log-audit` 的 description 也要做到这个密度。

> 注意：`disable-model-invocation: true` 是**可选字段**——全仓库只有 4 个技能用了它（`apply-harness`、`arch-review`、`handoff`、`harness-me`）。`log-audit` 设它的理由和 `arch-review` 一样：会全量扫描代码库、副作用大，应由人主动发起。而 `logging-toolkit` 不设这个字段，因为它可以在 `coding-skill` 阶段被自动检测加载。**不要默认加这个字段**——大多数技能应允许流水线自动推进或按条件触发。

### 5.2 前置检查 + 流程

```markdown
## 1. 前置检查

1. 确认规则文件存在：`.harness/rules/编码规范.md` 中有"日志规范"章节
   - 不存在 → 报错：规则未定义，先在规则文件中补充日志规范
2. 确认项目已识别技术栈（Java Logback / Python logging / Go zap）
   - 未识别 → 提示先运行 `/apply-harness`
3. 锁定审计范围：
   - 默认：全项目 `*.java` / `*.py` / `*.go` 源文件
   - 用户指定模块 → 只审计指定路径

## 2. 工作流程

### Step 1: 收集日志调用点
- 用 grep 找出所有 log.xxx / logger.xxx 调用，按文件分组
- 输出：调用点清单（文件 + 行号 + 级别 + 内容）

### Step 2: 对照规范逐项检查
| 规范 | 检查点 | 规则来源 |
|------|--------|---------|
| 结构化 | 是否使用 key=value 或 JSON 字段，而非裸字符串拼接 | 编码规范.md §日志 |
| 级别 | error 是否只用于真正出错（不含业务预期分支） | 编码规范.md §日志 |
| 脱敏 | password/token/手机号 是否被打码 | 运行时可靠性.md §脱敏 |
| 采样 | 高频打印的日志是否有限流/采样 | 运行时可靠性.md §可靠性 |

### Step 3: 输出报告
- 按 🔴 严重（泄漏敏感信息）/ 🟡 改进（级别不当）/ 🟢 健康 分级
- 每条带文件与行号，附修改建议
```

> 对照真实代码 `logging-toolkit` 的结构——它有"触发方式表"（独立命令 + 自动加载）、"前置条件"（技术栈已识别、包路径已确定）、"工作流程"（Step 1-4）。`log-audit` 借鉴了这个结构，但作为审计技能没有"输出文件清单"（它不生成代码），而是输出报告。

### 5.3 约束（三级）

```markdown
## 3. 约束

- ❌ 不直接改代码，只输出报告                    ← 硬禁止：职责边界
- ❌ 不做超出规则范围的"自作主张"审计              ← 硬禁止：防止幻觉规则
- ✅ 每条问题必须带文件位置与行号                  ← 强制必须：可追溯
- ✅ 规则来源标注：本检查对应规则文件的哪一条       ← 强制必须：可追溯
- ⚠️ 遇到规则未覆盖的情况，标注"规则缺口"而非跳过   ← 注意：不静默跳过
```

对照真实代码 `redis-cache-wrapper` 的约束——"`❌ 禁止分布式锁释放用 if-else 代替 Lua`"，具体到实现细节。`log-audit` 的约束同样具体到"带文件位置与行号""标注规则来源"。

### 5.4 输出物 + 完成标志

```markdown
## 4. 输出物

写入 `.harness/changes/<id>/log-audit.md`（若有活跃变更）
或 `.harness/wiki/log-audit-<timestamp>.md`（独立审计）：

# 日志规范审计报告

## 总览
- 审计范围: N 个文件，M 个日志调用点
- 严重: X | 改进: Y | 健康: Z

## 🔴 严重问题
| 文件 | 行号 | 问题 | 规则来源 | 建议 |
|------|------|------|---------|------|
| UserController.java | 42 | 日志输出明文手机号 | 运行时可靠性.md §脱敏 | 改用 LogDesensitizer |

## 🟡 改进建议
...

## 规则缺口
- 规范未定义"异步日志"要求 → 建议补充

## 5. 完成标志
- [ ] 全部调用点已检查
- [ ] 报告含位置 + 级别 + 建议 + 规则来源
- [ ] 规则缺口已记录，未静默跳过
```

对照真实代码 `expert-reviewer`——它的输出物是 `.harness/changes/<id>/review.md`，完成标志是"0 个 🔴 → 状态流转 reviewing → ci""有 🔴 → 退回 coding 修复"。`log-audit` 作为独立审计技能，没有状态流转（不是流水线技能），但同样要求报告落地到文件。

### 5.5 联动设计

```markdown
## 6. 联动

| 场景 | 联动技能 | 动作 |
|------|---------|------|
| 发现日志基础设施缺失（无 traceId/无脱敏） | `logging-toolkit` | 建议用户调用以生成基础设施代码 |
| 发现规则未覆盖的日志场景 | `domain-modeling` | 在 CONTEXT.md 记录"规则缺口" |
| 审计后需要修复 | `coding-skill` | 将报告作为变更卡输入，进入流水线 |
```

### 5.6 为什么这个示例"合格"

对照七个组成部分：

| 部分 | 是否做到 | 体现 |
|------|---------|------|
| frontmatter | ✅ | name + 场景式 description + 可选 disable-model-invocation |
| 一句话信条 | ✅ | "只审计，不改代码" |
| 职责定义 | ✅ | 隐含在信条里——审计者，不是修改者 |
| 前置检查 | ✅ | 验证规则文件存在、技术栈已识别、锁定范围 |
| 工作流程 | ✅ | 3 个 Step，每步有输入→动作→输出 |
| 约束 | ✅ | 三级（禁止/必须/注意），具体可执行 |
| 完成标志 | ✅ | 检查清单 + 报告落地到文件 |
| 联动 | ✅ | 与 logging-toolkit / domain-modeling / coding-skill 联动 |

---

## 六、Skill 的维护与演进

### 6.1 Skill 也是代码

Skill 应该像代码一样管理：

| 实践 | 说明 | 真实体现 |
|------|------|---------|
| 纳入 git | Skill 与代码同库版本化 | `skills/` 目录在仓库根目录 |
| 走评审 | 改 Skill 走变更流程 | Skill 改动影响所有 AI 协作，等同改接口 |
| 有测试 | 至少有一条"示例输入 → 期望输出"的验收 | 完成标志就是验收用例 |
| 记录变更 | 变更卡描述"改了哪个 Step、为什么" | `change.md` 追踪 Skill 变更 |
| 定期体检 | arch-review 的治理维度覆盖 Skill 本身 | `arch-review` 扫描摩擦信号 |

### 6.2 Skill 的版本策略

```
v1.0  初始版本，覆盖主路径
v1.1  补充边界：处理"规则未覆盖"的情况（如 log-audit 的"规则缺口"）
v2.0  重构：Step 拆分更细，输出物改版
```

Skill 的演进信号：**AI 频繁出错、用户频繁纠正、团队流程变了**。

真实代码里的演进痕迹——`expert-reviewer` 的"领域技能加载"机制就是演进的产物：最初只有固定的 10 维度审查，后来发现不同代码需要不同的领域检查，于是加入了"检测条件 → 加载技能"的混合模式。这是 v1.0 → v2.0 的演进。

### 6.3 何时删掉一个 Skill

- 流程已经废弃
- 被更通用的 Skill 覆盖
- 半年无人调用

删掉也是维护的一部分。**库存太多的 Skill 等于没有 Skill**——AI 选择时会迷失。

> 真实代码里 `apply-harness` 的 Step 5 第 4 点就是"清理残留技能目录"——"删除 `.harness/skills/{lang}/` 下在本仓库中已不存在的旧技能目录，确保仅包含当前渲染的技能，无残留文件干扰"。删除是安装流程的一部分。

---

## 七、常见反模式

### 7.1 反模式一：Skill 写成了论文

```
❌ 一个 3000 行的 SKILL.md，什么都讲了，AI 根本执行不过来
✅ 开头一句话信条，正文只有 Step + 约束 + 输出物
```

真实代码里最精简的技能 `harness-me` 只有 7 行——它的全部内容就是一个 frontmatter 加一句"Run a harnessing session"。它把复杂度委托给了 `harnessing` 技能，自己只做触发。这是"精简到极致"的例子。

而 `expert-reviewer` 是"复杂但有序"的例子——它有 200 行，但结构清晰：前置检查 → 领域加载 → 双轴设计 → Spec 轴 → Standards 轴 → 汇总 → 完成标志。每个部分有明确边界，AI 能按图索骥。

### 7.2 反模式二：没有约束，只有流程

没有"❌ 不做什么"的 Skill，AI 会：
- 擅自改代码（如果规范允许它改）
- 虚构不存在的工具
- 跳过验证直接给结论

**约束是 Skill 的安全边界。**

### 7.3 反模式三：description 写得太泛

```
❌ "代码质量工具"
✅ "当需要检查代码是否符合项目规范时使用，生成分级报告"
```

泛 description 会让 AI 在错误场景调用，或在正确场景忘记调用。真实代码里 `diagnosing-bugs` 的 description 是范例——它明确写了"**适用于当用户报告'这个出错/崩溃/性能慢'时**"，把触发场景嵌在 description 里，AI 一读就知道何时用。

### 7.4 反模式四：Skill 与规则重复

规则文件已经规定了"编码规范"，Skill 又抄了一遍——两边维护，必然漂移。**正确做法：Skill 引用规则文件，规则文件是单一真相源。**

真实代码的做法——`coding-skill` 的工作流程写的是"加载上下文：`.harness/rules/编码规范.md`"，而不是把编码规范抄进 SKILL.md。`expert-reviewer` 的 Standards 轴写的是"对照 `.harness/rules/` 和代码质量基线"，不是复制规则内容。

### 7.5 反模式五：没有前置检查，直接开工

```
❌ Skill 一上来就开始做事，不验证当前状态是否合法
✅ 先做前置检查：状态对不对？上下文全不全？有没有多个候选？
```

没有前置检查的 Skill 会在"没有需求卡"时就开始编码，在"代码还没写"时就开始审查。真实代码里所有流水线技能都以"前置检查"开头——它是"开工许可证"。

### 7.6 反模式六：没有联动，变成孤岛

```
❌ Skill 做完就完了，不告诉下一个技能"该你了"
✅ 完成标志里写明状态流转 + 异常处理里写明调用谁
```

---

## 八、install-skill 与 Skill 生态

### 8.1 三条路径：生成 → 安装 → 使用

原文档用一句话总结了生态，但真实代码揭示的是**三条路径**，对应不同场景：

```svg
<svg xmlns="http://www.w3.org/2000/svg" width="860" height="300" viewBox="0 0 860 300">
  <defs>
    <linearGradient id="bg17d" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arr17d" markerWidth="8" markerHeight="8" refX="7" refY="4" orient="auto">
      <path d="M 0 0 L 8 4 L 0 8 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="860" height="300" fill="url(#bg17d)"/>
  <text x="430" y="28" text-anchor="middle" fill="#e2e8f0" font-size="16" font-weight="700">Skill 生命周期的三条路径</text>

  <!-- 路径一：全自动 -->
  <rect x="30" y="50" width="800" height="70" rx="8" fill="#10b981" opacity="0.06" stroke="#34d399" stroke-width="1"/>
  <text x="50" y="75" fill="#6ee7b7" font-size="12" font-weight="700">路径一：全自动（新项目初始化）</text>
  <rect x="50" y="85" width="150" height="28" rx="6" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1"/>
  <text x="125" y="103" text-anchor="middle" fill="#7dd3fc" font-size="10">/apply-harness</text>
  <line x1="200" y1="99" x2="225" y2="99" stroke="#475569" stroke-width="1.5" marker-end="url(#arr17d)"/>
  <rect x="230" y="85" width="180" height="28" rx="6" fill="#8b5cf6" opacity="0.12" stroke="#a78bfa" stroke-width="1"/>
  <text x="320" y="103" text-anchor="middle" fill="#c4b5fd" font-size="10">检测语言→渲染模板→复制规则</text>
  <line x1="410" y1="99" x2="435" y2="99" stroke="#475569" stroke-width="1.5" marker-end="url(#arr17d)"/>
  <rect x="440" y="85" width="170" height="28" rx="6" fill="#8b5cf6" opacity="0.12" stroke="#a78bfa" stroke-width="1"/>
  <text x="525" y="103" text-anchor="middle" fill="#c4b5fd" font-size="10">检测工具→创建目录→注册</text>
  <line x1="610" y1="99" x2="635" y2="99" stroke="#475569" stroke-width="1.5" marker-end="url(#arr17d)"/>
  <rect x="640" y="85" width="170" height="28" rx="6" fill="#f59e0b" opacity="0.12" stroke="#fbbf24" stroke-width="1"/>
  <text x="725" y="103" text-anchor="middle" fill="#fde68a" font-size="10">斜杠命令立即可用</text>

  <!-- 路径二：半自动 -->
  <rect x="30" y="135" width="800" height="70" rx="8" fill="#0ea5e9" opacity="0.06" stroke="#38bdf8" stroke-width="1"/>
  <text x="50" y="160" fill="#7dd3fc" font-size="12" font-weight="700">路径二：半自动（已有 .harness/skills/）</text>
  <rect x="50" y="170" width="150" height="28" rx="6" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1"/>
  <text x="125" y="188" text-anchor="middle" fill="#7dd3fc" font-size="10">/install-skill</text>
  <line x1="200" y1="184" x2="225" y2="184" stroke="#475569" stroke-width="1.5" marker-end="url(#arr17d)"/>
  <rect x="230" y="170" width="180" height="28" rx="6" fill="#8b5cf6" opacity="0.12" stroke="#a78bfa" stroke-width="1"/>
  <text x="320" y="188" text-anchor="middle" fill="#c4b5fd" font-size="10">检测 AI 工具</text>
  <line x1="410" y1="184" x2="435" y2="184" stroke="#475569" stroke-width="1.5" marker-end="url(#arr17d)"/>
  <rect x="440" y="170" width="170" height="28" rx="6" fill="#8b5cf6" opacity="0.12" stroke="#a78bfa" stroke-width="1"/>
  <text x="525" y="188" text-anchor="middle" fill="#c4b5fd" font-size="10">创建目录→复制技能→验证</text>
  <line x1="610" y1="184" x2="635" y2="184" stroke="#475569" stroke-width="1.5" marker-end="url(#arr17d)"/>
  <rect x="640" y="170" width="170" height="28" rx="6" fill="#f59e0b" opacity="0.12" stroke="#fbbf24" stroke-width="1"/>
  <text x="725" y="188" text-anchor="middle" fill="#fde68a" font-size="10">斜杠命令可用</text>

  <!-- 路径三：手动 -->
  <rect x="30" y="220" width="800" height="70" rx="8" fill="#8b5cf6" opacity="0.06" stroke="#a78bfa" stroke-width="1"/>
  <text x="50" y="245" fill="#c4b5fd" font-size="12" font-weight="700">路径三：手动（自定义技能）</text>
  <rect x="50" y="255" width="150" height="28" rx="6" fill="#8b5cf6" opacity="0.12" stroke="#a78bfa" stroke-width="1"/>
  <text x="125" y="273" text-anchor="middle" fill="#c4b5fd" font-size="10">手写 SKILL.md</text>
  <line x1="200" y1="269" x2="225" y2="269" stroke="#475569" stroke-width="1.5" marker-end="url(#arr17d)"/>
  <rect x="230" y="255" width="180" height="28" rx="6" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1"/>
  <text x="320" y="273" text-anchor="middle" fill="#7dd3fc" font-size="10">放入 .harness/skills/</text>
  <line x1="410" y1="269" x2="435" y2="269" stroke="#475569" stroke-width="1.5" marker-end="url(#arr17d)"/>
  <rect x="440" y="255" width="170" height="28" rx="6" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1"/>
  <text x="525" y="273" text-anchor="middle" fill="#7dd3fc" font-size="10">/install-skill 注册</text>
  <line x1="610" y1="269" x2="635" y2="269" stroke="#475569" stroke-width="1.5" marker-end="url(#arr17d)"/>
  <rect x="640" y="255" width="170" height="28" rx="6" fill="#f59e0b" opacity="0.12" stroke="#fbbf24" stroke-width="1"/>
  <text x="725" y="273" text-anchor="middle" fill="#fde68a" font-size="10">/my-skill 可调用</text>
</svg>
```

| 路径 | 命令 | 场景 | 做了什么 |
|------|------|------|---------|
| 全自动 | `/apply-harness` | 新项目，从零开始 | 检测语言→渲染模板→复制规则→检测工具→创建目录→注册 |
| 半自动 | `/install-skill` | 已有 `.harness/skills/`，换了 AI 工具 | 检测工具→创建目录→复制技能→验证 |
| 手动 | 手写 + `/install-skill` | 自定义技能（如 `log-audit`） | 手写 SKILL.md→放入目录→install-skill 注册 |

### 8.2 多工具共存

一个项目可能同时被多个 AI 工具使用（有人用 Reasonix，有人用 Claude Code）。install-skill 的原则是**只装到检测到的工具**，不跨工具安装，避免污染。

真实代码的检测逻辑是**按优先级顺序**的——先检查环境变量（精确），再检查目录存在（模糊），选择第一个匹配的：

```
优先级 1: REASONIX 环境变量 或 .reasonix/ 目录 → .reasonix/skills/
优先级 2: CLAUDE_CODE 环境变量 或 .claude/ 目录 → .claude/skills/
优先级 3: CLINE 环境变量 或 .cline/ 目录 → .cline/skills/
...
```

> **设计意图**：环境变量优先于目录存在——因为目录可能被其他工具的配置创建（误检测），而环境变量是工具自己声明的身份。这是"精确优于模糊"的检测原则。

### 8.3 Skill 格式的兼容未来

AI 工具的技能格式正在趋同（都在靠拢 SKILL.md），但今天仍是"战国时代"。**把源码维护成标准 SKILL.md，让 install-skill 去适配各工具**——这是面向未来的正确姿势。

> 真实代码里 `install-skill` 对原生工具（Reasonix、Claude Code、Cursor 等）直接复制 SKILL.md；对非原生工具（Windsurf、OpenCode、Trae）尝试转换或提示手动配置。**源码只有一份，适配逻辑全在 install-skill 里**——如果明天某工具改了格式，只改 install-skill，不动源码。

---

## 九、信条回顾

- **Skill = 结构化的操作手册 + 触发入口**：知识沉淀成可调用能力
- **四个部分不可省**：frontmatter + 信条 + 前置检查 + 工作流程 + 约束 + 完成标志，缺一不可
- **四种类型选对结构**：流水线技能要状态机，工具箱技能要触发表，辅助技能要问题库，通用技能要精简
- **参数化模板让一份源码服务多语言**：逻辑通用→模板化，工具不同→占位符化
- **三个"以上"决定是否值得封装**：人、项目、问题都重复才值得
- **约束是 Skill 的灵魂**：三级约束（禁止/必须/注意）是安全边界
- **前置检查是开工许可证**：状态机守卫防止在错误上下文执行
- **出口门禁 + 状态流转 = 质量保证 + 自动交接**：输出物落地到文件，门禁过了才进下一步
- **Skill 不是孤岛**：设计时要想清楚联动——谁调用我、我调用谁、什么条件触发
- **规则文件负责"必须"，CONTEXT.md 负责"是什么"，Skill 负责"怎么做"**：Skill 引用规则，不复制规则

下一篇，我们聊聊**如何新增一个框架支持**——把 gofmt 一键扩展到任意语言、任意框架。
