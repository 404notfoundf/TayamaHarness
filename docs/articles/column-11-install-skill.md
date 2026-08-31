# install-skill：技能安装到 AI 工具的设计哲学

> 核心思想系列 · 第五篇 · 约 10000 字 · 4 个 SVG 图

---

## 一、引言：技能装好了，但 AI 不认？

在前几篇文章中，我们讨论了工程纪律、参数化、自动检测和 apply-harness 安装引擎。apply-harness 把技能文件复制到了 `.harness/skills/` 目录，一切看起来都就绪了。

但当你打开 AI 编辑器，输入 `/harnessing`，屏幕显示：

```
Unknown command: /harnessing
```

**技能装好了，但 AI 工具不认识。**

这不是 bug，而是设计鸿沟。`apply-harness` 负责把技能文件放到项目目录，但各 AI 工具有自己的"技能注册机制"——它们不会自动扫描 `.harness/` 目录。

`install-skill` 要解决的就是这个鸿沟：**把技能从 `.harness/skills/` 安装到 AI 工具真正扫描的技能目录，让斜杠命令立即可用。**

这道鸿沟远比看起来复杂。目前市面上有 19 种以上 AI 工具，每种工具的检测方式、目录结构、技能格式都不相同。有的原生支持 `SKILL.md` 格式，有的只认自己的规则文件，有的需要手动配置。

`install-skill` 的设计哲学，就是在这 19 种工具的异质生态中，找到一个"统一安装协议"。

---

```svg
<svg xmlns="http://www.w3.org/2000/svg" width="800" height="340" viewBox="0 0 800 340">
    <defs>
        <!-- 背景渐变 -->
        <linearGradient id="bg" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stop-color="#0b1220"/>
            <stop offset="100%" stop-color="#111a2e"/>
        </linearGradient>
        <!-- 箭头标记 -->
        <marker id="arrow" markerWidth="10" markerHeight="10" refX="9" refY="5" orient="auto">
            <path d="M 0 0 L 10 5 L 0 10 z" fill="#475569"/>
        </marker>
        <!-- 发光阴影 -->
        <filter id="glow">
            <feDropShadow dx="0" dy="0" stdDeviation="4" flood-color="#38bdf8" flood-opacity="0.2"/>
        </filter>
    </defs>

    <!-- 背景 -->
    <rect width="800" height="340" fill="url(#bg)" rx="12"/>

    <!-- 标题 -->
    <text x="400" y="34" text-anchor="middle" fill="#e2e8f0" font-size="17" font-weight="700" font-family="sans-serif">技能安装的鸿沟</text>

    <!-- ===== 左侧：apply-harness ===== -->
    <rect x="40" y="56" width="200" height="96" rx="10" fill="#0ea5e9" opacity="0.08" stroke="#38bdf8" stroke-width="1.5"/>
    <text x="140" y="84" text-anchor="middle" fill="#7dd3fc" font-size="14" font-weight="700" font-family="monospace">apply-harness</text>
    <text x="140" y="108" text-anchor="middle" fill="#94a3b8" font-size="11" font-family="sans-serif">技能文件 → .harness/skills/</text>
    <text x="140" y="130" text-anchor="middle" fill="#64748b" font-size="11" font-family="sans-serif">项目级存储</text>

    <!-- ===== 红色 X ===== -->
    <text x="280" y="106" text-anchor="middle" fill="#ef4444" font-size="22" font-weight="700">✕</text>

    <!-- ===== 中间：AI 工具 ===== -->
    <rect x="320" y="56" width="200" height="96" rx="10" fill="#f59e0b" opacity="0.08" stroke="#fbbf24" stroke-width="1.5"/>
    <text x="420" y="84" text-anchor="middle" fill="#fde68a" font-size="14" font-weight="700" font-family="sans-serif">AI 工具</text>
    <text x="420" y="108" text-anchor="middle" fill="#94a3b8" font-size="11" font-family="sans-serif">扫描自己的技能目录</text>
    <text x="420" y="130" text-anchor="middle" fill="#64748b" font-size="11" font-family="sans-serif">.claude/ .cursor/ .reasonix/ ...</text>

    <!-- ===== 红色 X ===== -->
    <text x="560" y="106" text-anchor="middle" fill="#ef4444" font-size="22" font-weight="700">✕</text>

    <!-- ===== 右侧：Unknown ===== -->
    <rect x="600" y="56" width="160" height="96" rx="10" fill="#ef4444" opacity="0.08" stroke="#ef4444" stroke-width="1.5"/>
    <text x="680" y="88" text-anchor="middle" fill="#fca5a5" font-size="14" font-weight="700" font-family="monospace">/harnessing</text>
    <text x="680" y="118" text-anchor="middle" fill="#f87171" font-size="12" font-family="monospace">Unknown command</text>

    <!-- ===== 虚线箭头汇聚 ===== -->
    <line x1="140" y1="160" x2="400" y2="190" stroke="#475569" stroke-width="1.5" stroke-dasharray="5 3" marker-end="url(#arrow)"/>
    <line x1="420" y1="160" x2="400" y2="190" stroke="#475569" stroke-width="1.5" stroke-dasharray="5 3" marker-end="url(#arrow)"/>

    <!-- ===== 底部桥梁：install-skill ===== -->
    <rect x="300" y="200" width="200" height="36" rx="18" fill="#34d399" opacity="0.12" stroke="#34d399" stroke-width="1.5" filter="url(#glow)"/>
    <text x="400" y="224" text-anchor="middle" fill="#6ee7b7" font-size="14" font-weight="700" font-family="monospace">install-skill</text>

    <!-- ===== 底部说明文字 ===== -->
    <text x="400" y="268" text-anchor="middle" fill="#475569" font-size="12" font-family="sans-serif">在两者之间架起桥梁</text>

    <!-- ===== 底部装饰线 ===== -->
    <line x1="80" y1="290" x2="720" y2="290" stroke="#1e293b" stroke-width="1"/>
</svg>
```

**图 1：技能安装的鸿沟。apply-harness 把技能放到 `.harness/skills/`，但 AI 工具不扫描这个目录。install-skill 在两者之间架起桥梁。**

---

## 二、困境：19 个工具，19 种安装方式

### 2.1 为什么不是"统一目录"？

你可能会问：为什么不让所有 AI 工具都扫描 `.harness/skills/`？

答案很简单：**我们控制不了 AI 工具的设计。**

Reasonix 原生支持 `.reasonix/skills/`，Claude Code 扫描 `.claude/skills/`，Cursor 读取 `.cursor/skills/`，Cline 兼容 `.claude/skills/`，Windsurf 使用 `.windsurf/workflows/`，OpenCode 需要 `.opencode/commands/`，Tabnine 要求 `.tabnine/guidelines/`……每个工具都有自己的"技能注册协议"。

这不是生态分裂，而是"各扫门前雪"的正常结果。每个 AI 工具都希望用户在自己的目录下配置技能，以便于版本管理和工具发现。

### 2.2 兼容性分级

面对 19 个工具，`install-skill` 的做法不是"一刀切"，而是**分级兼容**：

| 级别 | 工具 | 安装方式 | 斜杠命令 |
|------|------|---------|---------|
| 原生 | Reasonix、Claude Code、Cline、Cursor、Codex、Qoder | 直接复制技能目录（含 SKILL.md）到 `<工具>/skills/<技能名>/` | 立即可用 |
| 原生 | VS Code Agent Skills（1.98+） | 复制到 `.vscode/agent-skills/` | 部分支持 |
| 部分兼容 | Amazon Q、Continue.dev、GitHub Copilot | 通过间接方式支持 | 需额外配置 |
| 需转换 | Windsurf、OpenCode、Trae、Tabnine | 转换为对应格式 | 格式不同 |
| 待确认 | CodeBuddy、通义灵码、CodeGeeX、Cody、MarsCode | 尝试复制，提示用户 | 不确定 |

**原生兼容**的工具占 7 个，它们都支持（或兼容）`SKILL.md` 格式。`install-skill` 只需复制技能目录即可。

**部分兼容**的工具需要额外步骤。比如 Amazon Q 需要通过 VS Code Agent Skills 间接兼容，Continue.dev 需要在 `config.yaml` 中注册技能路径。

**需转换**的工具使用完全不同的格式。Windsurf 的 workflow 格式、OpenCode 的独立命令格式、Tabnine 的 `guidelines.md` 格式——这些都需要格式转换，不能简单复制。

**待确认**的工具是兼容性尚未验证的。`install-skill` 对它们的策略是"尽量尝试，失败则提示用户手动配置"。

---

```svg
<svg xmlns="http://www.w3.org/2000/svg" width="800" height="300" viewBox="0 0 800 300">
  <defs>
    <linearGradient id="bg11b" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
  </defs>
  <rect width="800" height="300" fill="url(#bg11b)"/>
  <text x="400" y="28" text-anchor="middle" fill="#e2e8f0" font-size="16" font-weight="700">19 个 AI 工具的兼容性分级</text>
  <text x="130" y="58" text-anchor="middle" fill="#34d399" font-size="13" font-weight="700">原生（7 个）</text>
  <rect x="20" y="68" width="220" height="80" rx="8" fill="#10b981" opacity="0.08" stroke="#34d399" stroke-width="1"/>
  <text x="130" y="92" text-anchor="middle" fill="#94a3b8" font-size="11">Reasonix / Claude Code / Cline</text>
  <text x="130" y="112" text-anchor="middle" fill="#94a3b8" font-size="11">Cursor / Codex / Qoder</text>
  <text x="130" y="132" text-anchor="middle" fill="#94a3b8" font-size="11">VS Code Agent Skills</text>
  <text x="400" y="58" text-anchor="middle" fill="#fbbf24" font-size="13" font-weight="700">部分兼容（3 个）</text>
  <rect x="290" y="68" width="220" height="80" rx="8" fill="#f59e0b" opacity="0.08" stroke="#fbbf24" stroke-width="1"/>
  <text x="400" y="92" text-anchor="middle" fill="#94a3b8" font-size="11">Amazon Q</text>
  <text x="400" y="112" text-anchor="middle" fill="#94a3b8" font-size="11">Continue.dev / GitHub Copilot</text>
  <text x="400" y="132" text-anchor="middle" fill="#94a3b8" font-size="11">(需间接配置)</text>
  <text x="670" y="58" text-anchor="middle" fill="#f43f5e" font-size="13" font-weight="700">待确认（5 个）</text>
  <rect x="560" y="68" width="220" height="80" rx="8" fill="#f43f5e" opacity="0.08" stroke="#f43f5e" stroke-width="1"/>
  <text x="670" y="92" text-anchor="middle" fill="#94a3b8" font-size="11">CodeBuddy / 通义灵码</text>
  <text x="670" y="112" text-anchor="middle" fill="#94a3b8" font-size="11">CodeGeeX / Cody / MarsCode</text>
  <text x="670" y="132" text-anchor="middle" fill="#94a3b8" font-size="11">(路径待确认)</text>
  <text x="130" y="175" text-anchor="middle" fill="#fbbf24" font-size="13" font-weight="700">需转换（4 个）</text>
  <rect x="20" y="185" width="220" height="80" rx="8" fill="#f59e0b" opacity="0.08" stroke="#fbbf24" stroke-width="1"/>
  <text x="130" y="210" text-anchor="middle" fill="#94a3b8" font-size="11">Windsurf -> .windsurf/workflows/</text>
  <text x="130" y="230" text-anchor="middle" fill="#94a3b8" font-size="11">OpenCode -> .opencode/commands/</text>
  <text x="130" y="250" text-anchor="middle" fill="#94a3b8" font-size="11">Trae / Tabnine -> 独立格式</text>
  <text x="400" y="175" text-anchor="middle" fill="#94a3b8" font-size="13">检测策略</text>
  <rect x="290" y="185" width="220" height="80" rx="8" fill="#0ea5e9" opacity="0.08" stroke="#38bdf8" stroke-width="1"/>
  <text x="400" y="210" text-anchor="middle" fill="#94a3b8" font-size="11">按优先级检测原生工具</text>
  <text x="400" y="230" text-anchor="middle" fill="#94a3b8" font-size="11">未匹配则尝试部分兼容</text>
  <text x="400" y="250" text-anchor="middle" fill="#94a3b8" font-size="11">最后回退到手动配置提示</text>
  <text x="670" y="175" text-anchor="middle" fill="#64748b" font-size="13">关键原则</text>
  <rect x="560" y="185" width="220" height="80" rx="8" fill="#1e293b" stroke="#475569" stroke-width="1"/>
  <text x="670" y="210" text-anchor="middle" fill="#94a3b8" font-size="11">不猜测、不跨工具安装</text>
  <text x="670" y="230" text-anchor="middle" fill="#94a3b8" font-size="11">目录不存在则创建</text>
  <text x="670" y="250" text-anchor="middle" fill="#94a3b8" font-size="11">不修改原始 .harness 内容</text>
</svg>
```

**图 2：19 个 AI 工具的兼容性分级。每个级别有对应的安装策略。**

---

## 三、检测策略：如何确定当前 AI 工具？

### 3.1 优先级检测

`install-skill` 面临一个根本问题：**它不知道用户在哪个 AI 工具中运行它。**

这个问题没有完美的解决方案，因为不同 AI 工具的运行环境高度相似——都是终端、都是 shell。`install-skill` 无法通过进程名或环境变量精确判断当前工具。

所以它采用"优先级检测"策略：按兼容性从高到低检测目录，**优先选择原生支持 SKILL.md 的工具**，选择第一个匹配的目录。

检测顺序：

1. **Reasonix**：检测 `.reasonix/` 目录或 `REASONIX` 环境变量
2. **Claude Code**：检测 `.claude/` 目录或 `CLAUDE_CODE` 环境变量
3. **Cline / Roo Code**：检测 `.cline/` 或 `.clinerules/` 目录
4. **Cursor**：检测 `.cursor/` 目录
5. **Codex (OpenAI)**：检测 `.codex/` 目录或 `OPENAI_API_KEY` 环境变量
6. **Qoder**：检测 `.qoder/` 目录
7. **VS Code Agent Skills**：检测 `.vscode/` 目录（VS Code 1.98+）
8. ……

### 3.2 为什么按这个顺序？

顺序不是随机的，而是基于**兼容性等级**和**生态成熟度**：

- **Reasonix 和 Claude Code 排在最前**，因为它们原生支持 SKILL.md，且安装流程最简单——直接复制即可。
- **Cline 排在第三**，虽然它原生兼容，但生态相对年轻。
- **Cursor 排在第四**，它支持 `.cursor/skills/` 但需要版本配合。
- **VS Code Agent Skills 排在后面**，因为它需要 VS Code 1.98+ 版本，且 Agent Skills 是实验性功能。

这个顺序反映了"**最小配置原则**"：优先选择不需要用户额外配置的工具，把需要手动配置的放在后面。

### 3.3 目录不存在怎么办？

这是 `install-skill` 解决过的**最隐蔽的 bug**之一。

很多项目从未生成过 AI 工具的技能目录。比如一个项目可能使用 Reasonix，但 `.reasonix/skills/` 目录从未被创建过——因为没有任何工具自动创建它。

早期的 `install-skill` 会检测到 `.reasonix/` 目录存在，然后尝试安装技能到 `.reasonix/skills/`——但目录不存在，安装失败。

**修复很简单：`mkdir -p`。**

```
检测到目标目录 -> 若不存在，创建它 -> 安装技能
```

这个修复看似微不足道，但它揭示了一个更深层的设计原则：**不要假设环境已经就绪，主动创建缺失的依赖。**

---

```svg
<svg xmlns="http://www.w3.org/2000/svg" width="760" height="200" viewBox="0 0 760 200">
  <defs>
    <linearGradient id="bg11c" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arr11c" markerWidth="12" markerHeight="12" refX="10" refY="6" orient="auto">
      <path d="M 0 0 L 12 6 L 0 12 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="760" height="200" fill="url(#bg11c)"/>
  <text x="380" y="28" text-anchor="middle" fill="#e2e8f0" font-size="16" font-weight="700">检测流程：优先级 + 目录创建</text>
  <rect x="30" y="50" width="200" height="48" rx="8" fill="#0ea5e9" opacity="0.1" stroke="#38bdf8" stroke-width="1.5"/>
  <text x="130" y="78" text-anchor="middle" fill="#7dd3fc" font-size="13" font-weight="700">扫描工具目录</text>
  <line x1="230" y1="74" x2="280" y2="74" stroke="#475569" stroke-width="2" marker-end="url(#arr11c)"/>
  <rect x="290" y="50" width="180" height="48" rx="8" fill="#f59e0b" opacity="0.1" stroke="#fbbf24" stroke-width="1.5"/>
  <text x="380" y="78" text-anchor="middle" fill="#fde68a" font-size="13" font-weight="700">目录存在？</text>
  <text x="380" y="120" text-anchor="middle" fill="#94a3b8" font-size="11">否</text>
  <line x1="380" y1="98" x2="380" y2="108" stroke="#475569" stroke-width="1.5"/>
  <line x1="380" y1="108" x2="380" y2="132" stroke="#475569" stroke-width="1.5" marker-end="url(#arr11c)"/>
  <rect x="290" y="135" width="180" height="48" rx="8" fill="#34d399" opacity="0.1" stroke="#34d399" stroke-width="1.5"/>
  <text x="380" y="163" text-anchor="middle" fill="#6ee7b7" font-size="13" font-weight="700">mkdir -p 创建</text>
  <line x1="470" y1="74" x2="530" y2="74" stroke="#475569" stroke-width="2" marker-end="url(#arr11c)"/>
  <rect x="540" y="50" width="180" height="48" rx="8" fill="#34d399" opacity="0.1" stroke="#34d399" stroke-width="1.5"/>
  <text x="630" y="78" text-anchor="middle" fill="#6ee7b7" font-size="13" font-weight="700">安装技能</text>
</svg>
```

**图 3：检测流程。目录不存在时自动创建，不依赖环境就绪。**

---

## 四、安装流程：从检测到注册

### 4.1 四步安装

`install-skill` 的安装流程是"检测 -> 定位 -> 安装 -> 摘要"四个步骤：

**Step 1：检测当前 AI 工具**

按优先级检测 19 个工具，选择第一个匹配的目录。这一步确定"安装到哪里"。

**Step 2：定位技能来源**

扫描 `.harness/skills/` 目录，收集所有包含 `SKILL.md` 的技能目录。结构如下：

```
.harness/skills/
+-- java/
|   +-- harnessing/
|   +-- coding-skill/
|   +-- unit-test-write/
|   +-- expert-reviewer/
|   +-- harness-me/
|   +-- arch-review/
+-- golang/
|   +-- ...
+-- python/
|   +-- ...
+-- rust/
|   +-- ...
+-- php/
|   +-- ...
+-- front/
|   +-- ...
+-- common/
    +-- domain-modeling/
    +-- research/
    +-- resolving-merge-conflicts/
    +-- handoff/
```

技能分为两类：**语言特有技能**（每个语言包一套）和**跨语言通用技能**（所有语言共享）。至于哪些语言包进入 `.harness/skills/`，是上游 apply-harness 在渲染时决定的（见 4.3 节），install-skill 只是照单全收。

**Step 3：创建目标目录并安装技能**

首先确保目标目录存在（`mkdir -p`），然后对每个技能：

- **原生工具**：完整复制技能目录到 `<工具目录>/<技能名>/`
- **非原生工具**：尝试格式转换，或提示用户手动配置

这一步的关键是"**保持 SKILL.md 的 frontmatter 不变**"。斜杠命令名来自 SKILL.md 的 `name` 字段，改变它会导致命令名变化。

**Step 4：输出安装摘要**

安装完成后，输出一个清晰的摘要，告诉用户哪些技能已安装、哪些命令可用：

```
+---------------------------------------------+
|  技能已安装到 Reasonix                       |
+---------------------------------------------+
|  目标目录: .reasonix/skills/                 |
|  已安装:   12 个技能                         |
|  /harnessing        可调用                   |
|  /harness-me        可调用                   |
|  /coding-skill      可调用                   |
|  /unit-test-write   可调用                   |
|  /expert-reviewer   可调用                   |
|  ...                                         |
+---------------------------------------------+
```

这个摘要既是"安装确认"，也是"功能清单"——用户一眼就能知道哪些命令可用。

---

```svg
<svg xmlns="http://www.w3.org/2000/svg" width="780" height="260" viewBox="0 0 780 260">
  <defs>
    <linearGradient id="bg11d" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arr11d" markerWidth="12" markerHeight="12" refX="10" refY="6" orient="auto">
      <path d="M 0 0 L 12 6 L 0 12 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="780" height="260" fill="url(#bg11d)"/>
  <text x="390" y="28" text-anchor="middle" fill="#e2e8f0" font-size="16" font-weight="700">四步安装流程</text>
  <rect x="30" y="50" width="150" height="100" rx="10" fill="#0ea5e9" opacity="0.1" stroke="#38bdf8" stroke-width="1.5"/>
  <text x="105" y="78" text-anchor="middle" fill="#7dd3fc" font-size="20" font-weight="700">1</text>
  <text x="105" y="102" text-anchor="middle" fill="#e2e8f0" font-size="12" font-weight="700">检测</text>
  <text x="105" y="124" text-anchor="middle" fill="#94a3b8" font-size="10">19 工具优先级</text>
  <text x="105" y="140" text-anchor="middle" fill="#94a3b8" font-size="10">目录不存在则创建</text>
  <line x1="180" y1="100" x2="220" y2="100" stroke="#475569" stroke-width="2.5" marker-end="url(#arr11d)"/>
  <rect x="230" y="50" width="150" height="100" rx="10" fill="#8b5cf6" opacity="0.1" stroke="#a78bfa" stroke-width="1.5"/>
  <text x="305" y="78" text-anchor="middle" fill="#c4b5fd" font-size="20" font-weight="700">2</text>
  <text x="305" y="102" text-anchor="middle" fill="#e2e8f0" font-size="12" font-weight="700">定位</text>
  <text x="305" y="124" text-anchor="middle" fill="#94a3b8" font-size="10">扫描 .harness/skills/</text>
  <text x="305" y="140" text-anchor="middle" fill="#94a3b8" font-size="10">收集 SKILL.md</text>
  <line x1="380" y1="100" x2="420" y2="100" stroke="#475569" stroke-width="2.5" marker-end="url(#arr11d)"/>
  <rect x="430" y="50" width="150" height="100" rx="10" fill="#f59e0b" opacity="0.1" stroke="#fbbf24" stroke-width="1.5"/>
  <text x="505" y="78" text-anchor="middle" fill="#fde68a" font-size="20" font-weight="700">3</text>
  <text x="505" y="102" text-anchor="middle" fill="#e2e8f0" font-size="12" font-weight="700">安装</text>
  <text x="505" y="124" text-anchor="middle" fill="#94a3b8" font-size="10">复制到工具目录</text>
  <text x="505" y="140" text-anchor="middle" fill="#94a3b8" font-size="10">格式转换（非原生）</text>
  <line x1="580" y1="100" x2="620" y2="100" stroke="#475569" stroke-width="2.5" marker-end="url(#arr11d)"/>
  <rect x="630" y="50" width="130" height="100" rx="10" fill="#34d399" opacity="0.1" stroke="#34d399" stroke-width="1.5"/>
  <text x="695" y="78" text-anchor="middle" fill="#6ee7b7" font-size="20" font-weight="700">4</text>
  <text x="695" y="102" text-anchor="middle" fill="#e2e8f0" font-size="12" font-weight="700">摘要</text>
  <text x="695" y="124" text-anchor="middle" fill="#94a3b8" font-size="10">安装摘要</text>
  <text x="695" y="140" text-anchor="middle" fill="#94a3b8" font-size="10">可用命令清单</text>
  <text x="390" y="195" text-anchor="middle" fill="#475569" font-size="11">// 每一步的输出都是下一步的输入</text>
  <rect x="200" y="210" width="380" height="36" rx="8" fill="#1e293b" stroke="#475569" stroke-width="1"/>
  <text x="390" y="234" text-anchor="middle" fill="#94a3b8" font-size="11">检测 -> 定位 -> 安装 -> 摘要：完整的安装闭环</text>
</svg>
```

**图 4：四步安装流程。每一步的输出都是下一步的输入，形成完整的安装闭环。**

---


## 四（续）：安装流程的深入细节

### 4.2 技能来源的结构化扫描

在定位技能来源时，`install-skill` 面对的是一个多语言、多技能的嵌套结构。`.harness/skills/` 目录的典型结构是：

```
.harness/skills/
+-- java/
|   +-- harnessing/
|   |   +-- SKILL.md
|   |   +-- rules/        # 技能关联的规则文件
|   +-- coding-skill/
|   |   +-- SKILL.md
|   |   +-- templates/    # 技能关联的模板
|   +-- unit-test-write/
|   +-- expert-reviewer/
|   +-- harness-me/
|   +-- arch-review/
+-- golang/               # 同上结构，但语言不同
|   +-- harnessing/
|   +-- coding-skill/
|   +-- ...
+-- python/
|   +-- ...
+-- common/
    +-- domain-modeling/  # 跨语言通用技能
    +-- research/
    +-- resolving-merge-conflicts/
    +-- handoff/
```

`install-skill` 的扫描逻辑是递归的：它遍历 `skills/` 下的所有子目录，查找包含 `SKILL.md` 的目录。每个包含 `SKILL.md` 的目录就是一个可安装的技能。

这个扫描逻辑看似简单，但有一个重要的设计决策：**完整复制整个技能目录，而非只复制 `SKILL.md`。** 原因在于技能可能带有关联文件（如 `rules/`、`templates/`），这些文件是技能运转的一部分。复制时连同目录一起落到 `<工具目录>/<技能名>/` 下，保持目录结构与源一致。

### 4.3 语言包筛选：在 apply-harness 阶段完成

需要澄清一个容易混淆的职责边界：**语言包的筛选不在 install-skill 阶段进行，而是在上游的 apply-harness 阶段完成。**

apply-harness 会检测项目构建文件（`pom.xml`→Java、`go.mod`→Go、`package.json`→Front、`Cargo.toml`→Rust、`pyproject.toml`→Python、`composer.json`→PHP），只把当前语言包对应的技能渲染进 `.harness/skills/{lang}/`，加上 `common/` 下的通用技能。也就是说，等到 install-skill 执行时，`.harness/skills/` 里已经只剩当前语言的技能了——install-skill 不再做二次过滤。

这个分工的依据是职责分离：apply-harness 负责"渲染哪些技能"（项目级、与语言相关），install-skill 负责"装到哪个工具"（工具级、与语言无关）。如果用户切换语言，需要重新运行 apply-harness 重新生成 `.harness/skills/`，再运行 install-skill 重新注册。

### 4.4 安装摘要的详细设计

安装摘要是用户与 `install-skill` 交互的最后一步，也是最重要的信息输出。它的设计遵循"一眼可知"原则：

```
+---------------------------------------------+
|  技能已安装到 Reasonix                       |
|  目标目录: .reasonix/skills/                 |
+---------------------------------------------+
|  /harnessing        [OK] 可调用              |
|  /harness-me        [OK] 可调用              |
|  /coding-skill      [OK] 可调用              |
|  /unit-test-write   [OK] 可调用              |
|  /expert-reviewer   [OK] 可调用              |
|  /arch-review       [OK] 可调用              |
|  /domain-modeling   [OK] 可调用              |
|  /research          [OK] 可调用              |
|  /handoff           [OK] 可调用              |
+---------------------------------------------+
|  已安装: 9 个技能      未安装: 0 个技能      |
+---------------------------------------------+
```

如果某个技能安装失败，摘要会显示具体的错误信息：

```
+---------------------------------------------+
|  /arch-review       [FAIL] 权限不足          |
|  /domain-modeling   [FAIL] 磁盘空间不足       |
+---------------------------------------------+
```

这个设计让用户一眼就能看到安装结果，不需要逐行阅读日志。

---

## 五（补充）：从安装摘要到即时可用

### 5.1 摘要是唯一的交付物

安装完成后，`install-skill` 不做独立的"验证步骤"——它直接输出安装摘要作为交付物。摘要列出目标工具、目标目录、已安装技能清单与可用的斜杠命令，是一份"一眼可知"的功能清单。如果某个技能复制失败，摘要会在对应行标出失败原因（如权限不足、目录不可写），而不生成独立的验证报告。

### 5.2 工具识别靠重启，不靠验证

AI 工具能否识别新安装的技能，不在 `install-skill` 的控制范围内——它无法探知另一个进程的技能加载状态。所以"功能级确认"退化为一句提示性指引：安装完成后提示用户重启会话或重新加载配置，以确保新斜杠命令被拾取。

---

## 六、目录创建：被低估的细节

### 6.1 为什么目录创建如此重要

在早期的版本中，`install-skill` 只复制文件到已存在的目录。如果目标目录不存在，它就会失败。

这个 bug 的影响比看起来大得多。很多项目从未生成过 AI 工具的技能目录，因为：

- 项目是新的，还没有任何 AI 工具配置
- 用户换了 AI 工具，旧工具目录已删除
- 项目是从模板克隆的，模板不包含特定工具的配置

在这些情况下，`install-skill` 都会失败——不是因为技能安装有问题，而是因为目标目录不存在。

### 6.2 mkdir -p 的哲学

`mkdir -p` 是一个 Unix 命令，它的语义是"创建目录及其所有父目录，如果已存在则不做任何事"。

这个命令体现了"**防御性编程**"的哲学：不假设环境已经就绪，主动创建缺失的依赖。

在 `install-skill` 中，`mkdir -p` 的应用是：

```
1. 检测到目标工具（如 Reasonix）
2. 目标目录为 .reasonix/skills/
3. 检查 .reasonix/ 是否存在
   - 不存在 -> 创建 .reasonix/
4. 检查 .reasonix/skills/ 是否存在
   - 不存在 -> 创建 .reasonix/skills/
5. 复制技能文件到 .reasonix/skills/
```

这个流程确保即使 `.reasonix/` 目录也不存在，`install-skill` 也能正确创建完整的目录结构。

### 6.3 权限问题

目录创建可能遇到权限问题。如果项目目录位于系统保护区域（如 `/usr/local/` 或 `C:\Program Files`），`install-skill` 可能没有权限创建目录。

`install-skill` 的处理方式是：

1. 尝试创建目录
2. 如果失败，捕获错误信息
3. 输出清晰的错误提示，告诉用户需要哪些权限
4. 建议用户以管理员身份运行

这个处理方式的哲学是"**失败要明确**"：不要静默失败，不要让用户猜测问题出在哪里。

---

## 七、安装策略：不同工具的不同处理

### 7.1 原生工具：直接复制

对于 Reasonix、Claude Code、Cline、Cursor 等原生支持 SKILL.md 的工具，安装策略最简单：**直接复制技能目录到工具的技能目录。**

```python
# 伪代码
for skill in skills:
    src = f".harness/skills/{language}/{skill}/SKILL.md"
    dst = f"{tool_dir}/skills/{skill}/SKILL.md"
    mkdir_p(dst.parent)
    copy_file(src, dst)
```

这个策略的优势是简单、可靠、可验证。复制完成后，AI 工具下次启动时就能识别新技能。

### 7.2 部分兼容工具：需要额外配置

对于 Continue.dev、GitHub Copilot 等部分兼容的工具，直接复制可能不够。这些工具需要在配置文件中注册技能路径。

以 Continue.dev 为例：

```yaml
# .continue/config.yaml
skills:
  - path: .reasonix/skills/harnessing/SKILL.md
  - path: .reasonix/skills/harness-me/SKILL.md
  # ...
```

`install-skill` 对这些工具的处理方式是：先复制技能文件，然后尝试更新配置文件。如果配置文件不存在或格式不兼容，提示用户手动配置。

### 7.3 需转换工具：格式转换

对于 Windsurf、OpenCode、Trae 等使用不同格式的工具，`install-skill` 需要将 SKILL.md 转换为对应格式。

以 Windsurf 为例，它的 workflow 格式是：

```yaml
# .windsurf/workflows/harnessing.yaml
name: harnessing
description: 需求拷问引擎
steps:
  - prompt: |
      请开始一场"灵魂拷问"式的对话...
```

转换逻辑是：读取 SKILL.md 的 frontmatter 和 body，提取 `name`、`description` 和 `body` 字段，然后生成对应格式的配置文件。

这个转换过程是有损的——不是所有 SKILL.md 的功能都能在目标格式中完整表达。`install-skill` 会尽量保留关键信息，但对于不兼容的功能，会提示用户手动调整。

---

## 八、与 apply-harness 的集成

### 8.1 安装顺序

`install-skill` 和 `apply-harness` 的推荐使用顺序是：

1. 首次设置：`apply-harness` -> `install-skill`
2. 技能更新：`apply-harness` -> `install-skill`
3. 技能新增：`apply-harness` -> `install-skill`
4. 技能删除：`apply-harness` -> `install-skill`

每次 `apply-harness` 后都应该运行 `install-skill`，确保技能注册到 AI 工具。

### 8.2 自动化集成

在持续集成环境中，`install-skill` 可以作为 CI 流水线的一步。但需要注意：CI 环境通常没有 AI 工具，所以 `install-skill` 在 CI 中只会检测工具目录，检测不到就会跳过安装。

这个行为是符合预期的：CI 环境不需要 AI 技能，只有开发环境需要。

---

## 九、设计哲学总结

### 9.1 三个核心原则

`install-skill` 的设计哲学可以总结为三个核心原则：

1. **分级兼容**：面对 19 个工具的异质生态，不追求"一刀切"的统一方案，而是按兼容性等级制定不同的安装策略
2. **主动创建**：不假设环境已经就绪，目录不存在就创建，技能缺失就安装
3. **保守失败**：不猜测、不跨工具、不修改原始内容。失败时告诉用户精确的下一步

### 9.2 与生态的关系

`install-skill` 的存在，反映了 AI 技能生态的一个根本现状：**技能格式正在统一，但安装路径尚未统一。**

SKILL.md 格式正在被越来越多的 AI 工具原生支持，这是一个好的趋势。但每个工具仍然坚持自己的技能目录结构，导致安装路径的分裂。

`install-skill` 不是要统一安装路径，而是在路径分裂的现状下提供一个"智能桥接"——让用户只需运行一条命令，技能就能出现在正确的工具目录中。

### 9.3 未来方向

随着 AI 工具生态的发展，`install-skill` 的未来方向包括：

- **Watch 模式**：监控 `.harness/skills/` 的变化，自动同步到工具目录
- **多工具安装**：同时安装到多个检测到的 AI 工具
- **格式转换引擎**：对非原生工具，自动转换 SKILL.md 到对应格式

但核心原则不变：**让技能安装变得简单，让用户专注于工程纪律本身，而不是工具适配的琐事。**

---

> **本文是"核心思想系列"的第五篇。前四篇分别讨论了工程纪律（01）、参数化（02）、自动检测（03）和 apply-harness 安装引擎（04）。下一篇将讨论 SDD-TDD 方法论——如何让 AI 先写设计再写代码。**


### 5.1 安装 vs 注册

`install-skill` 和 `apply-harness` 经常被混淆。它们名字相似，但职责完全不同：

| 维度 | apply-harness | install-skill |
|------|-------------|--------------|
| 目标 | 把项目变成 Harness 项目 | 把技能注册到 AI 工具 |
| 输入 | 模板 + 参数表 | `.harness/skills/` 下的技能 |
| 输出 | `.harness/` 完整目录 | 工具目录下的技能 |
| 运行时机 | 项目初始化时一次 | 每次安装/更新技能时 |
| 重复运行 | 安全（覆盖旧文件） | 安全（覆盖旧技能） |
| 前置条件 | 无 | 必须先运行 apply-harness |

`apply-harness` 是"**安装引擎**"——它把模板渲染成具体的技能文件，放到 `.harness/skills/` 目录。这是"从无到有"的过程。

`install-skill` 是"**注册引擎**"——它把 `.harness/skills/` 中的技能注册到 AI 工具可识别的目录。这是"从有到可用"的过程。

### 5.2 为什么需要两个步骤？

为什么不把安装和注册合并成一个步骤？

原因在于职责分离：

- **安装是项目级别的操作**：它影响项目的 `.harness/` 目录，与项目配置、参数表、模板渲染相关。这是"项目工程纪律基础设施"的一部分。
- **注册是工具级别的操作**：它影响 AI 工具的技能目录，与工具检测、目录结构、格式兼容性相关。这是"AI 工具生态适配"的一部分。

把两者合并会导致一个"胖子脚本"——既要做参数化渲染，又要适配 19 种工具格式。这不仅难以维护，还会让用户产生不必要的依赖（每次改参数都要重新注册）。

### 5.3 互补关系

`apply-harness` 和 `install-skill` 形成互补关系：

```
apply-harness -> 渲染技能到 .harness/skills/
                      |
              install-skill -> 注册技能到 AI 工具
```

没有 `apply-harness`，`install-skill` 没有技能可安装。
没有 `install-skill`，`apply-harness` 安装的技能不被 AI 工具识别。

两者缺一不可。

---

## 六、约束：哪些事情不做

### 6.1 不修改原始 .harness 内容

`install-skill` 只复制，不移动，不修改。`.harness/skills/` 是"真相源"，工具目录是"副本"。这个区分很重要：

- 如果用户修改了 `.harness/skills/` 中的技能，重新运行 `install-skill` 会覆盖工具目录中的旧版本
- 如果用户删除了 `.harness/skills/` 中的某个技能，重新运行 `install-skill` 会从工具目录中移除它

### 6.2 不安装自身

`install-skill` 把自己排除在安装列表之外。原因很简单——它只需运行一次，不需要被 AI 工具作为斜杠命令注册。

### 6.3 不跨工具安装

`install-skill` 只安装到检测到的第一个工具目录，不会同时安装到多个工具。用户可以使用多个 AI 工具，但每次运行 `install-skill` 只安装到一个工具。

如果需要安装到多个工具，用户需要为每个工具分别运行 `install-skill`。

### 6.4 不猜测未检测到的工具

如果 `install-skill` 未能检测到任何 AI 工具，它不会猜测或随机选择一个目录。它会输出一条清晰的提示，告诉用户手动指定工具，或回退到 `npx skills@latest add <source>` 这类通用安装器。

这是刻意保守的设计。**猜测失败比失败更糟糕**——失败是明确的，猜测失败是隐式的，用户可能很久之后才发现技能没有安装到正确的工具。

---

## 七、设计决策背后的故事

### 7.1 为什么叫"install-skill"而不是"register-skill"

名字曾经是 `register-skill`，但后来改成了 `install-skill`。

原因在于语义：**安装（install）是"把东西放进去"，注册（register）是"告诉系统东西在哪"。** `install-skill` 确实把技能文件复制到了工具目录——这是"安装"而非"注册"。

"注册"更适合引用场景——比如告诉系统"技能在 `/path/to/skills/` 目录"，但不复制文件。但现实是大多数 AI 工具不支持外部引用，只能复制文件。

### 7.2 为什么要保留"update"语义

`install-skill` 可重复运行。每次运行都会覆盖工具目录中的旧技能。这意味着它同时也是一个"更新命令"。

为什么不叫 `update-skill`？因为"安装"包含了"首次安装"和"更新"两种语义。安装的前提是"技能不存在"，更新的前提是"技能已存在但版本过时"。`install-skill` 两者都支持——如果技能不存在，复制；如果技能已存在，覆盖。

---

## 八、安装后确认

### 8.1 安装后如何确认

安装完成后，`install-skill` 不做独立的验证轮次——安装与确认合并在"安装摘要"一步里。摘要本身即承担确认职能：它列出目标工具、目标目录、每个技能的复制结果（成功 / 失败及原因）。如果某技能复制失败，对应行直接标明原因；没有单独的"验证报告"。

### 8.2 失败怎么办

如果某技能复制失败，摘要会在对应行给出可操作的提示：

- 目录权限不足 -> 提示用户以管理员身份运行
- 文件被占用 -> 提示用户关闭相关工具
- 磁盘空间不足 -> 提示用户清理空间

这些错误信息是"可操作的"——用户看到后知道下一步该做什么。

---

## 九、总结

### 9.1 设计哲学回顾

`install-skill` 的设计哲学可以总结为三点：

1. **分级兼容**：面对 19 个工具的异质生态，不追求"一刀切"的统一方案，而是按兼容性等级制定不同的安装策略
2. **主动创建**：不假设环境已经就绪，目录不存在就创建，技能缺失就安装
3. **保守失败**：不猜测、不跨工具、不修改原始内容。失败时告诉用户精确的下一步

### 9.2 与生态的关系

`install-skill` 的存在，反映了 AI 技能生态的一个根本现状：**技能格式正在统一，但安装路径尚未统一。**

SKILL.md 格式正在被越来越多的 AI 工具原生支持，这是一个好的趋势。但每个工具仍然坚持自己的技能目录结构，导致安装路径的分裂。

`install-skill` 不是要统一安装路径，而是在路径分裂的现状下提供一个"智能桥接"——让用户只需运行一条命令，技能就能出现在正确的工具目录中。

### 9.3 未来方向

随着 AI 工具生态的发展，`install-skill` 的未来方向包括：

- **Watch 模式**：监控 `.harness/skills/` 的变化，自动同步到工具目录
- **多工具安装**：同时安装到多个检测到的 AI 工具
- **格式转换引擎**：对非原生工具，自动转换 SKILL.md 到对应格式

但核心原则不变：**让技能安装变得简单，让用户专注于工程纪律本身，而不是工具适配的琐事。**

---

> **本文是"核心思想系列"的第五篇。前四篇分别讨论了工程纪律（01）、参数化（02）、自动检测（03）和 apply-harness 安装引擎（04）。下一篇将讨论 SDD-TDD 方法论——如何让 AI 先写设计再写代码。**


## 十一、从安装摘要看交付物

前文五（补充）已说明，`install-skill` 不设独立的"验证轮次"，安装摘要即交付物。这里再从另一个角度补一句：摘要的诚实性体现在"不等同于成功"——如果 `.harness/skills/` 为空或某技能复制失败，摘要会如实标出，不会用一句笼统的"安装成功"掩盖。这个原则与六节的"失败要明确"一脉相承。

---

## 十二、实战案例

### 12.1 场景设定

假设一个开发团队正在使用一个 Java 微服务项目。项目结构如下：

```
my-service/
+-- pom.xml
+-- src/
+-- .harness/
|   +-- skills/
|       +-- java/
|       |   +-- harnessing/
|       |   +-- coding-skill/
|       |   +-- unit-test-write/
|       |   +-- expert-reviewer/
|       |   +-- harness-me/
|       |   +-- arch-review/
|       +-- common/
|           +-- domain-modeling/
|           +-- research/
+-- .gitignore
```

团队刚刚运行了 `apply-harness`，`.harness/skills/` 目录已经生成。但团队成员使用的是不同的 AI 工具。

### 12.2 不同工具的安装过程

对于 Cursor 用户，安装过程的示意输出如下（实际措辞以 SKILL.md 为准）：

```
$ install-skill
[检测] 检测到 .cursor/ 目录
[创建] 创建 .cursor/skills/ 目录
[安装] 安装 harnessing/ 到 .cursor/skills/harnessing/
[安装] 安装 coding-skill/ 到 .cursor/skills/coding-skill/
[安装] 安装 unit-test-write/ 到 .cursor/skills/unit-test-write/
[安装] 安装 expert-reviewer/ 到 .cursor/skills/expert-reviewer/
[安装] 安装 harness-me/ 到 .cursor/skills/harness-me/
[安装] 安装 arch-review/ 到 .cursor/skills/arch-review/
[安装] 安装 domain-modeling/ 到 .cursor/skills/domain-modeling/
[安装] 安装 research/ 到 .cursor/skills/research/
[摘要] 技能已安装到 Cursor，8 个命令可用
```

> 注：上图为示意输出。SKILL.md 本体只规定了摘要的呈现格式，不强制逐行日志的措辞；`[验证]` 一类环节是早期设想过但未落地的，现已从流程中移除。

### 12.3 团队协作中的安装分工

在团队协作中，`install-skill` 的职责分工是：

- **架构师或技术负责人**：负责运行 `apply-harness` 初始化项目，配置参数表
- **每个开发者**：各自运行 `install-skill`，将技能安装到自己使用的 AI 工具

这种分工的好处是：每个开发者可以自由选择自己习惯的 AI 工具，而不影响团队的工程纪律统一性。

---

## 十三、常见问题与解答

### 13.1 安装后斜杠命令仍然不可用

如果安装后斜杠命令仍然不可用，可能的原因包括：

1. AI 工具需要重启才能识别新技能
2. 技能目录不是 AI 工具扫描的默认目录
3. AI 工具版本不支持自定义技能

解决方案是：重启 AI 工具，检查 AI 工具的文档确认技能目录位置，或者升级 AI 工具到支持自定义技能的版本。

### 13.2 安装到错误的位置

如果 `install-skill` 安装到了 `.claude/` 目录，但用户使用的是 Cursor，这是因为检测优先级的原因。如果 `.claude/` 和 `.cursor/` 都存在，`install-skill` 会优先选择 `.claude/`。

解决方案是：手动指定工具，或者删除不需要的目录。

### 13.3 技能安装后立即被覆盖

每次运行 `apply-harness` 后，已安装的技能都会被覆盖。这是预期行为，因为 `apply-harness` 会重新生成 `.harness/skills/` 目录，然后 `install-skill` 会覆盖工具目录中的旧技能。

解决方案是：在 `apply-harness` 之后运行 `install-skill`，确保技能是最新版本。

---

## 十四、未来方向

### 14.1 Watch 模式

Watch 模式是 `install-skill` 的未来方向之一。在 Watch 模式下，`install-skill` 会监控 `.harness/skills/` 目录的变化，当技能文件发生变化时，自动同步到工具目录。

这个功能对团队协作特别有用：当架构师更新了技能配置，团队成员不需要手动运行 `install-skill`，技能会自动更新。

### 14.2 多工具安装

目前 `install-skill` 只安装到检测到的第一个工具目录。未来版本可以支持同时安装到多个检测到的 AI 工具。

设计挑战在于：如何确定用户同时使用哪些工具？当检测到多个工具目录时，是全部安装还是让用户选择？

### 14.3 格式转换引擎

对非原生工具，`install-skill` 目前的做法是提示用户手动配置。未来版本可以自动转换 SKILL.md 到对应格式。

转换引擎的设计挑战在于：不同格式之间的功能映射不是一一对应的。有些功能在 SKILL.md 中有，但在目标格式中可能没有对应的表达方式。

---

## 十五、总结

### 15.1 三个核心原则

`install-skill` 的设计哲学可以总结为三个核心原则：

1. **分级兼容**：面对 19 个工具的异质生态，不追求一刀切的统一方案，而是按兼容性等级制定不同的安装策略
2. **主动创建**：不假设环境已经就绪，目录不存在就创建，技能缺失就安装
3. **保守失败**：不猜测、不跨工具、不修改原始内容。失败时告诉用户精确的下一步

### 15.2 与生态的关系

`install-skill` 的存在，反映了 AI 技能生态的一个根本现状：技能格式正在统一，但安装路径尚未统一。

SKILL.md 格式正在被越来越多的 AI 工具原生支持，这是一个好的趋势。但每个工具仍然坚持自己的技能目录结构，导致安装路径的分裂。

`install-skill` 不是要统一安装路径，而是在路径分裂的现状下提供一个智能桥接——让用户只需运行一条命令，技能就能出现在正确的工具目录中。

### 15.3 核心思想

三级兼容、主动创建目录、保守失败——这些设计决策背后的核心思想是：**让技能安装变得简单，让用户专注于工程纪律本身，而不是工具适配的琐事。**

当用户运行 `install-skill` 时，他们不需要知道 Reasonix 的技能目录在哪里，不需要知道 Cursor 的格式是什么，不需要知道如何配置 VS Code 的 Agent Skills。`install-skill` 替他们处理了这一切。

这就是 `install-skill` 的设计哲学：**把复杂留给实现，把简单留给用户。**

---

> **本文是"核心思想系列"的第五篇。前四篇分别讨论了工程纪律（01）、参数化（02）、自动检测（03）和 apply-harness 安装引擎（04）。下一篇将讨论 SDD-TDD 方法论——如何让 AI 先写设计再写代码。**


## 十六、边界情况与错误处理

### 16.1 空目录处理

当 `.harness/skills/` 目录为空时，`install-skill` 会输出：

```
[警告] .harness/skills/ 目录为空，没有技能可安装
[建议] 请先运行 apply-harness 生成技能文件
```

这个处理方式的设计原则是：不给用户一个"成功"的假象。如果没有任何技能可安装，`install-skill` 会明确告诉用户问题出在哪里。

### 16.2 重复安装

当多次运行 `install-skill` 时，技能文件会被覆盖。这是预期行为，因为 `install-skill` 的设计就是可重复运行的。

但有一个问题：如果用户手动修改了工具目录中的技能文件，重新运行 `install-skill` 会覆盖这些修改。`install-skill` 不会提示用户，因为 `.harness/skills/` 是真相源，工具目录是副本。

### 16.3 权限不足

当 `install-skill` 没有权限创建目录或复制文件时，会输出特定的错误信息：

```
[ERROR] 无法创建目录 .reasonix/skills/
[原因] 权限不足
[建议] 请以管理员身份运行，或检查目录权限设置
```

这个错误信息的格式是"三段式"：错误描述 + 原因分析 + 解决建议。用户不需要猜测问题出在哪里。

### 16.4 磁盘空间不足

当磁盘空间不足时，`install-skill` 会检测到复制失败，并输出：

```
[ERROR] 无法复制技能文件到 /home/user/.reasonix/skills/harnessing/SKILL.md
[原因] 磁盘空间不足
[建议] 请清理磁盘空间，至少需要 100MB 可用空间
```

## 十七、性能考虑

### 17.1 安装速度

`install-skill` 的安装速度取决于技能的数量和文件大小。在典型场景下（8-12 个技能），安装过程在 1-2 秒内完成。

如果技能数量很多（比如 50+ 个），安装时间可能增加到 5-10 秒。这个时间仍然在可接受范围内。

### 17.2 增量安装

`install-skill` 目前不支持增量安装——每次运行都会重新复制所有技能文件。未来版本可以支持增量安装，只复制发生变化的文件。

### 17.3 并行安装

`install-skill` 目前是串行安装的——一次复制一个技能文件。未来版本可以支持并行安装，利用多核 CPU 的优势提高安装速度。

## 十八、与其他工具的对比

### 18.1 与 package manager 的对比

`install-skill` 类似于一个"包管理器"——它从 `.harness/skills/` 这个"仓库"安装技能到 AI 工具的"本地目录"。

但与 npm 或 pip 等包管理器不同，`install-skill` 不处理依赖关系，不管理版本，不提供卸载功能。它的职责范围更窄，但也更简单。

### 18.2 与 dotfile 管理器的对比

`install-skill` 也类似于 dotfile 管理器——它把配置文件复制到正确的位置。

但与 dotfile 管理器不同，`install-skill` 的目标位置是 AI 工具的技能目录，而不是用户的 home 目录。它的安装策略是针对 AI 工具生态设计的。

## 十九、结语

`install-skill` 是 Harness 技能体系中看似简单但实则关键的一环。它解决了一个"最后一公里"的问题——技能已经准备好了，但 AI 工具不认识它。

这个问题看似简单，但深入分析后发现它涉及 19 种工具的检测、4 级兼容性分级、目录创建、格式转换、安装摘要等多个方面。

`install-skill` 的设计哲学告诉我们：**在异质生态中，兼容性不是二元的（兼容或不兼容），而是分级的。** 对于不同的工具，采用不同的策略，而不是追求一个放之四海而皆准的解决方案。

当你下次运行 `install-skill` 并看到 `/harnessing` 命令可用时，背后是 19 种工具的检测优先级、4 级兼容性分级、目录创建的防御性编程、一步到位的安装摘要——这些设计决策共同保证了"一条命令，技能立即可用"的用户体验。

---

> **本文是"核心思想系列"的第五篇。前四篇分别讨论了工程纪律（01）、参数化（02）、自动检测（03）和 apply-harness 安装引擎（04）。下一篇将讨论 SDD-TDD 方法论——如何让 AI 先写设计再写代码。**


## 二十、核心设计决策回顾

### 20.1 为什么选择"复制"而不是"链接"

安装技能有两种方式：复制文件或创建符号链接。

`install-skill` 选择复制而不是链接，原因在于：

- **AI 工具可能不支持符号链接**：某些 AI 工具在启动时不会解析符号链接，导致技能不可用
- **符号链接在新环境中无效**：如果项目被克隆到新环境，符号链接可能指向不存在的路径
- **复制更可靠**：复制是文件系统最基础的操作，任何环境都支持

### 20.2 为什么"只安装当前语言"的职责不在 install-skill

如 4.3 节所述，"只保留当前语言包"这件事是 apply-harness 在渲染阶段完成的：它检测 `pom.xml`/`go.mod`/`package.json`/`Cargo.toml`/`pyproject.toml`/`composer.json` 等构建文件，只把当前语言包的技能写进 `.harness/skills/`。install-skill 拿到的是已经筛过的产物，不再二次过滤。

这个分工的依据是：用户不需要看到与自己项目无关的技能；而"判断项目是什么语言"属于项目级决策，归 apply-harness。如果用户切换语言，重新运行 apply-harness 重新生成 `.harness/skills/` 即可。

### 20.3 为什么选择"检测优先级"而不是"手动选择"

`install-skill` 采用自动检测优先级的方式确定工具，而不是让用户手动选择。

原因在于：用户通常不会在命令行中告诉 `install-skill` 他们使用什么 AI 工具——他们希望一切自动完成。

自动检测的优先级设计基于两个原则：

1. **兼容性优先**：原生支持 SKILL.md 的工具排在前面
2. **生态成熟度优先**：用户量大的工具排在前面

## 二十一、扩展阅读

### 21.1 相关文章

- **工程纪律即架构（01）**：讨论 Harness 项目的核心设计哲学
- **参数化：52 个占位符的设计取舍（02）**：讨论技能模板的参数化设计
- **自动检测：从 6 种语言到 19 个工具（03）**：讨论自动检测策略
- **apply-harness：安装引擎的设计哲学（04）**：讨论安装引擎的整体设计

### 21.2 相关代码文件

- `skills/install-skill/SKILL.md`：install-skill 技能的完整实现
- `skills/apply-harness/SKILL.md`：apply-harness 技能的完整实现
- `skills/harness-core/`：Harness 核心技能库

---

> **本文是"核心思想系列"的第五篇。前四篇分别讨论了工程纪律（01）、参数化（02）、自动检测（03）和 apply-harness 安装引擎（04）。下一篇将讨论 SDD-TDD 方法论——如何让 AI 先写设计再写代码。**


## 二十二、安装摘要的统计信息

`install-skill` 会在安装完成后输出安装摘要，帮助用户了解安装结果：

- 目标工具与目标目录
- 已安装的技能清单与对应的斜杠命令
- 复制失败的技能及原因（如有）
- 建议的下一步操作（如重启会话以刷新斜杠命令）

这些信息不仅是对安装结果的确认，也是对用户下一步操作的指引。用户不需要猜测安装是否成功，也不需要查找日志文件——所有信息都在安装摘要中一目了然。

## 二十三、作为平台工程的一部分

`install-skill` 不仅仅是一个工具，它是平台工程（Platform Engineering）的一部分。在平台工程的视角下，`install-skill` 提供了"自助服务"的能力——开发者可以自行安装和更新技能，而不需要等待平台团队的手动配置。

这种自助服务的能力是平台工程的核心价值之一：**让开发者能够自主地完成配置工作，减少对平台团队的依赖，同时保持配置的一致性和标准化。**

## 二十四、致谢

`install-skill` 的设计和实现离不开对 AI 工具生态的深入研究。感谢 Reasonix、Claude Code、Cline、Cursor、Codex、Qoder、VS Code 等工具的开发者，你们的优秀设计启发了 `install-skill` 的兼容性分级策略。

也要感谢所有早期用户，你们的反馈帮助我们发现并修复了目录创建、跨平台兼容性等关键问题。

---

> **本文是"核心思想系列"的第五篇。前四篇分别讨论了工程纪律（01）、参数化（02）、自动检测（03）和 apply-harness 安装引擎（04）。下一篇将讨论 SDD-TDD 方法论——如何让 AI 先写设计再写代码。**

## 二十五、写在最后

`install-skill` 这个技能本身的名字就体现了它的设计哲学——"install"（安装）意味着把技能放进去，而不是"register"（注册）意味着告诉系统在哪里。

当用户运行 `install-skill` 时，他们得到的是一个立即可用的技能集合。不需要手动配置，不需要修改文件，不需要重启服务。一条命令，一切就绪。

这就是 `install-skill` 的终极目标：**让技能安装变得无感，让用户忘记安装过程本身，只记得技能带来的效率提升。**

在 AI 工具生态快速发展的今天，`install-skill` 的策略可能随时需要调整。但核心原则不会变：分级兼容、主动创建、保守失败。这些原则不仅适用于技能安装，也适用于任何需要在异质生态中提供统一体验的系统设计。

---

> **本文是"核心思想系列"的第五篇。前四篇分别讨论了工程纪律（01）、参数化（02）、自动检测（03）和 apply-harness 安装引擎（04）。下一篇将讨论 SDD-TDD 方法论——如何让 AI 先写设计再写代码。**
