# /harness-changelog：变更日志——每条都可追溯到原始变更卡

> 命令深度拆解 · 第 36 篇 · 约 9000 字 · 7 个 SVG 图

---

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 300" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_c0" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_c0"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="300" fill="url(#bg_c0)" rx="10"/>
  <text x="400" y="28" text-anchor="middle" fill="#e2e8f0" font-size="26" font-weight="700">/harness-changelog：变更日志，条条可追溯</text>
  <rect x="60" y="55" width="320" height="95" rx="10" fill="#a855f7" opacity="0.12" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_c0)"/>
  <text x="220" y="80" text-anchor="middle" fill="#d8b4fe" font-size="14" font-weight="700">Keep a Changelog 风格</text>
  <text x="220" y="104" text-anchor="middle" fill="#94a3b8" font-size="11">按语义化版本归类 · 按类型分组</text>
  <text x="220" y="126" text-anchor="middle" fill="#64748b" font-size="10">Added/Changed/Fixed/Removed…</text>
  <rect x="420" y="55" width="320" height="95" rx="10" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_c0)"/>
  <text x="580" y="80" text-anchor="middle" fill="#86efac" font-size="14" font-weight="700">每个条目都能追溯</text>
  <text x="580" y="104" text-anchor="middle" fill="#94a3b8" font-size="11">条目链接 review/verify 留档</text>
  <text x="580" y="126" text-anchor="middle" fill="#64748b" font-size="10">不写模糊的\"若干优化\"</text>
  <text x="400" y="185" text-anchor="middle" fill="#475569" font-size="13">四步流程：收集 → 归类 → 落档 → 收口</text>
  <rect x="60" y="205" width="160" height="45" rx="8" fill="#3b82f6" opacity="0.10" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_c0)"/>
  <text x="140" y="234" text-anchor="middle" fill="#93c5fd" font-size="11">① 收集</text>
  <rect x="240" y="205" width="160" height="45" rx="8" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_c0)"/>
  <text x="320" y="234" text-anchor="middle" fill="#fde68a" font-size="11">② 归类</text>
  <rect x="420" y="205" width="160" height="45" rx="8" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_c0)"/>
  <text x="500" y="234" text-anchor="middle" fill="#86efac" font-size="11">③ 落档</text>
  <rect x="600" y="205" width="160" height="45" rx="8" fill="#ef4444" opacity="0.10" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_c0)"/>
  <text x="680" y="234" text-anchor="middle" fill="#fca5a5" font-size="11">④ 收口</text>
  <text x="400" y="280" text-anchor="middle" fill="#64748b" font-size="10">核心价值：不写模糊的\"若干优化\"——每条带 change ID，禁止无出处条目</text>
</svg>
```

## 一、/harness-changelog 解决的是什么问题

### 1.1 CHANGELOG 是给"人"看的发布说明书

版本号是机器的（tag、registry），CHANGELOG 是**给人看的**——用户、下游团队、未来的自己。看 CHANGELOG 的人想知道三件事：这个版本改了什么？为什么改？影响我什么？

一份烂 CHANGELOG 长这样：

```
## [0.4.0] - 2026-08-31
- 若干优化
- bug 修复
- 功能增强
```

看完等于没看——"若干优化"是什么优化？"bug 修复"修了什么？影响我的接口吗？`/harness-changelog` 就是来消灭这种"幽灵日志"的：把 `status: done` 的 change 汇总进 `CHANGELOG.md`，按语义化版本归类、按类型分组、条目链接到对应的 review.md/verify.md 留档。

它的定位是**发布的前置整理**（`/harness-ship` 的 Step 3 专用），也可独立运行。核心价值一句话：**每个条目都能追溯到原始变更卡，不写模糊的"若干优化"**。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 240" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_c1" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617\"/></linearGradient>
    <filter id="sh_c1"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="240" fill="url(#bg_c1)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700\">\"若干优化\" vs 可追溯条目</text>
  <rect x="40" y="50" width="330" height="110" rx="8" fill="#ef4444" opacity="0.10" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_c1)"/>
  <text x="205" y="76" text-anchor="middle" fill="#fca5a5" font-size="13" font-weight="700\">幽灵日志 ❌</text>
  <text x="205" y="102" text-anchor="middle" fill="#94a3b8" font-size="10\">若干优化 / bug 修复 / 功能增强</text>
  <text x="205" y="124" text-anchor="middle" fill="#64748b" font-size="8\">看完等于没看——无出处、无详情、无影响</text>
  <text x="205" y="146" text-anchor="middle" fill="#64748b" font-size="8\">读者无法判断\"影响我什么\"</text>
  <rect x="420" y="50" width="340" height="110" rx="8" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_c1)"/>
  <text x="590" y="76" text-anchor="middle" fill="#86efac" font-size="13" font-weight="700\">可追溯条目 ✅</text>
  <text x="590" y="102" text-anchor="middle" fill="#94a3b8" font-size="10\">新增订单状态机（[C-001](…)）</text>
  <text x="590" y="124" text-anchor="middle" fill="#64748b" font-size="8\">每条带 change ID + review/verify 链接</text>
  <text x="590" y="146" text-anchor="middle" fill="#64748b" font-size="8\">想看详情点链接直达原始变更卡</text>
  <text x="400" y="204" text-anchor="middle" fill="#475569" font-size="11\">CHANGELOG 的读者有权知道\"改了什么、为什么、影响我什么\"</text>
  <text x="400" y="230" text-anchor="middle" fill="#64748b" font-size="10\">幽灵日志让读者无从判断——可追溯条目让读者自己去看</text>
</svg>
```

### 1.2 为什么条目必须带 change ID

`/harness-changelog` 最强调的一条纪律：**每条含 change ID，禁止无出处条目**。

想象半年后：线上出了个 ISSUE，"某个接口行为变了"。排查的人去看 CHANGELOG——如果条目是"若干优化"，线索在这里断掉；如果条目是"更新了订单查询行为（[C-007](.harness/changes/C-007/change.md) · [review](.harness/changes/C-007/review.md)）"，排查的人点进去就能看到：需求卡、评审记录、验证报告——**整条证据链都在**。

change ID 是"变更"与"日志"之间的索引键。没有它，CHANGELOG 只是一串字符串；有了它，CHANGELOG 变成证据链的入口。

### 1.3 与 harness-ship 的关系：组织者 vs 发射者

| 维度 | harness-changelog | harness-ship |
|------|------------------|-------------|
| 负责 | 整理变更条目 | 版本号 + tag + 触发 |
| 时机 | ship 的 Step 3 | 发布全流程 |
| 决策 | 不替发布做版本决策 | 版本号由它与人类确定 |

changelog 是"整理者"——它负责把变更写清楚；ship 是"发射者"——它负责把版本定下来、推出去。**changelog 不替发布做版本决策**：版本号是 `harness-ship` 与人类共同确定的，本技能只整理条目。这个边界清晰，两者不越界。

## 二、前置：三个小检查

1. **用 `/harness-status` 列出 `status: done` 的 change**——只有 done 的 change 才进 CHANGELOG，进行中的不算数；
2. **读取 `CHANGELOG.md` 现有头部**（确认当前版本与格式约定）——在既有格式上续写，不重起炉灶；
3. **无 CHANGELOG.md → 按模板创建**（Keep a Changelog 风格）。

前置的用意：**收集的边界 = done 状态，格式的基准 = 现有头部**。不 done 的 change 写"已完成"条目是编造；无视现有格式重写是破坏。前置检查让 changelog 始终\"接着写\"，而不是\"重写\"。

## 三、模板：Keep a Changelog 风格

无 CHANGELOG.md 时按以下模板创建：

```markdown
# Changelog

本项目所有值得记录的变更。

## [Unreleased]

## [0.2.0] - <date>
### Added
- <功能>（[C-0NN](.harness/changes/C-0NN/change.md) · [review](.harness/changes/C-0NN/review.md)）

### Changed
- <变更>（[C-0NN](…)）

### Fixed
- <修复>（[C-0NN](…)）

### Removed / Deprecated / Security
- …
```

模板的三个设计要点：

1. **`[Unreleased]` 段**——正在积累、尚未发布的条目先放这里，发布时收口成版本小节。这保持了"日志持续在写"的节奏：不需要每个 change 都发布一次，条目先堆在 Unreleased，发布时一次性收口；
2. **类型分组（Added/Changed/Fixed/Removed/Deprecated/Security）**——Keep a Changelog 的标准分组，读者按类型扫读：想找修复就看 Fixed，想找破坏就看 Removed；
3. **条目尾部链接**（change.md + review）——从日志直达留档的"证据链入口"，这是与普通 CHANGELOG 的最大区别。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 280" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_c2" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_c2"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="280" fill="url(#bg_c2)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">模板三要素</text>
  <rect x="40" y="50" width="220" height="100" rx="8" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_c2)"/>
  <text x="150" y="76" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">[Unreleased] 段</text>
  <text x="150" y="100" text-anchor="middle" fill="#94a3b8" font-size="9">未发布条目先堆这</text>
  <text x="150" y="122" text-anchor="middle" fill="#64748b" font-size="8">发布时收口成版本小节</text>
  <text x="150" y="140" text-anchor="middle" fill="#64748b" font-size="8">日志持续在写</text>
  <rect x="290" y="50" width="220" height="100" rx="8" fill="#3b82f6" opacity="0.10" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_c2)"/>
  <text x="400" y="76" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">类型分组</text>
  <text x="400" y="100" text-anchor="middle" fill="#94a3b8" font-size="9">Added/Changed/Fixed…</text>
  <text x="400" y="122" text-anchor="middle" fill="#64748b" font-size="8">按类型扫读</text>
  <text x="400" y="140" text-anchor="middle" fill="#64748b" font-size="8">看修复→Fixed，看破坏→Removed</text>
  <rect x="540" y="50" width="220" height="100" rx="8" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_c2)"/>
  <text x="650" y="76" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">条目尾部链接</text>
  <text x="650" y="100" text-anchor="middle" fill="#94a3b8" font-size="9">(change.md · review)</text>
  <text x="650" y="122" text-anchor="middle" fill="#64748b" font-size="8">证据链入口</text>
  <text x="650" y="140" text-anchor="middle" fill="#64748b" font-size="8">日志→留档直达</text>
  <text x="400" y="208" text-anchor="middle" fill="#475569" font-size="11">Unreleased 保证节奏，分组保证可扫读，链接保证可追溯</text>
  <text x="400" y="240" text-anchor="middle" fill="#64748b" font-size="10">这是与普通 CHANGELOG 的最大区别：日志不只是"记录"，是"入口"</text>
  <text x="400" y="264" text-anchor="middle" fill="#64748b" font-size="10">想深挖的人一点链接就到原始变更卡</text>
</svg>
```

## 四、四步执行流程

### Step 1: 收集

- 从 `git log` 或 change 目录列出本次窗口内 done 的 change；
- 每个 change 读取 `change.md` 标题 + 摘要，决定归类（Added/Changed/Fixed/Removed/Deprecated/Security）。

收集的"窗口"由发布范围决定：本次发布覆盖哪些 change，就收集哪些。归类是 Step 1 的副产物：读标题+摘要时顺手判断类型——新增功能 → Added；行为变更 → Changed；bug 修复 → Fixed；删了东西 → Removed；标记弃用 → Deprecated；安全问题 → Security。

### Step 2: 写入 [Unreleased]

- 条目追加到 `[Unreleased]` 对应类型分组下，格式：`- <摘要>（[C-0NN](.harness/changes/C-0NN/change.md) · [review](.harness/changes/C-0NN/review.md)）`；
- 莫写进已发布版本小节。

### Step 3: 检查格式

- 条目是否都带 ID 链接？类型是否正确？摘要是否忠于 change.md？
- 有疑问的条目回查 change.md，不凭空补内容。

Step 3 是质量闸：**条目要么有出处，要么回查补出处，绝不凭空写**。"不编造"在这里的具体含义——摘要忠于 change.md：change.md 说"重构了订单模块"，条目就不能写成"新增了订单模块"。

### Step 4: 发布时收口

- 发布时（`/harness-ship` 触发前）把 `[Unreleased]` 内容收口为新版本小节（如 `## [0.2.0] - <date>`）；
- 清空 `[Unreleased]`，等待下一轮。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 250" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_c3" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_c3"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="250" fill="url(#bg_c3)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">四步流程</text>
  <rect x="60" y="50" width="140" height="95" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_c3)"/>
  <text x="130" y="74" text-anchor="middle" fill="#93c5fd" font-size="11" font-weight="700">① 收集</text>
  <text x="130" y="98" text-anchor="middle" fill="#94a3b8" font-size="9">列 done 的 change</text>
  <text x="130" y="118" text-anchor="middle" fill="#64748b" font-size="8">git log / change 目录</text>
  <text x="130" y="136" text-anchor="middle" fill="#64748b" font-size="8">读摘要定归类</text>
  <rect x="230" y="50" width="150" height="95" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_c3)"/>
  <text x="305" y="74" text-anchor="middle" fill="#fde68a" font-size="11" font-weight="700">② 写入 Unreleased</text>
  <text x="305" y="98" text-anchor="middle" fill="#94a3b8" font-size="9">追加到对应分组</text>
  <text x="305" y="118" text-anchor="middle" fill="#64748b" font-size="8">格式带 ID + 链接</text>
  <text x="305" y="136" text-anchor="middle" fill="#64748b" font-size="8">不写进已发布小节</text>
  <rect x="410" y="50" width="150" height="95" rx="8" fill="#ef4444" opacity="0.12" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_c3)"/>
  <text x="485" y="74" text-anchor="middle" fill="#fca5a5" font-size="11" font-weight="700">③ 检查格式</text>
  <text x="485" y="98" text-anchor="middle" fill="#94a3b8" font-size="9">ID 链接齐不齐</text>
  <text x="485" y="118" text-anchor="middle" fill="#64748b" font-size="8">类型对不对</text>
  <text x="485" y="136" text-anchor="middle" fill="#64748b" font-size="8">摘要忠于原文？</text>
  <rect x="590" y="50" width="160" height="95" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_c3)"/>
  <text x="670" y="74" text-anchor="middle" fill="#86efac" font-size="11" font-weight="700">④ 发布时收口</text>
  <text x="670" y="98" text-anchor="middle" fill="#94a3b8" font-size="9">Unreleased → 版本小节</text>
  <text x="670" y="118" text-anchor="middle" fill="#64748b" font-size="8">清空等待下一轮</text>
  <text x="670" y="136" text-anchor="middle" fill="#64748b" font-size="8">ship 触发前完成</text>
  <text x="400" y="190" text-anchor="middle" fill="#475569" font-size="11">Step 3 是关键闸：有疑问回查 change.md，绝不凭空补内容</text>
  <text x="400" y="220" text-anchor="middle" fill="#64748b" font-size="10">"不编造" = 摘要忠于 change.md，一句不夸大、不缩小、不换词</text>
</svg>
```

## 五、四条纪律：CHANGELOG 的底线

- **每条含 change ID，禁止无出处条目**——这是追溯信仰的底线；
- **面向使用者**：写"用户/调用方能感知"的变更，不堆内部噪音——"重构了数据访问层"是内部噪音，"订单查询接口返回新增字段 x"是用户可感知的变更；
- **不替发布做版本决策**：版本号由 `/harness-ship` 与人类确定，本技能只整理条目；
- **不编造**：未完成的 change 不写"已完成"条目；条目内容忠于 change.md 摘要。

四条纪律可以归成一句话：**CHANGELOG 是给读者看的真实记录，不是给流程看的表格**。

- "禁止无出处"保证真实可查；
- "面向使用者"保证读者有用；
- "不替版本决策"保证边界清晰；
- "不编造"保证内容可信。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 280" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_c4" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_c4"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="280" fill="url(#bg_c4)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">四条纪律</text>
  <rect x="40" y="50" width="340" height="70" rx="8" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_c4)"/>
  <text x="210" y="72" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">① 每条含 change ID</text>
  <text x="210" y="98" text-anchor="middle" fill="#64748b" font-size="9">禁止无出处条目——追溯信仰的底线</text>
  <rect x="420" y="50" width="340" height="70" rx="8" fill="#3b82f6" opacity="0.10" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_c4)"/>
  <text x="590" y="72" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">② 面向使用者</text>
  <text x="590" y="98" text-anchor="middle" fill="#64748b" font-size="9">写用户可感知的变更，不堆内部噪音</text>
  <rect x="40" y="140" width="340" height="70" rx="8" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_c4)"/>
  <text x="210" y="162" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">③ 不替发布做版本决策</text>
  <text x="210" y="188" text-anchor="middle" fill="#64748b" font-size="9">版本由 ship + 人类确定，这里只整理条目</text>
  <rect x="420" y="140" width="340" height="70" rx="8" fill="#ef4444" opacity="0.10" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_c4)"/>
  <text x="590" y="162" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">④ 不编造</text>
  <text x="590" y="188" text-anchor="middle" fill="#64748b" font-size="9">未完成不写已完成，内容忠于 change.md</text>
  <text x="400" y="250" text-anchor="middle" fill="#475569" font-size="11">一句话：CHANGELOG 是给读者看的真实记录，不是给流程看的表格</text>
</svg>
```

## 六、与相邻技能的分工

| 场景 | 归属 |
|------|------|
| 变更条目整理 | **本技能**（`/harness-changelog`） |
| 版本号决策 + tag | `/harness-ship` |
| 未发布 change 清单 | `/harness-status` |

- **harness-ship**：版本号决策 + tag + 触发——changelog 是它的 Step 3 专用整理器，ship 是 changelog 内容的"发布出口"；
- **harness-status**：未发布 change 清单——changelog 收集 done change 时调用它；
- 三者构成发布整理的链路：**status 列清单 → changelog 写条目 → ship 定版本发出去**。

## 七、完成标志

`/harness-changelog` 的完成标志有三个：

1. **已 done change 全部归入 CHANGELOG 条目**（带 ID 链接）——没有漏网的 change，也没有无出处的条目；
2. **条目类型归类正确，无编造、无内部噪音**——Added/Changed/Fixed 分得对，摘要忠于原文，只写使用者能感知的；
3. **`[Unreleased]` 已更新**（或本次条目已收口为版本小节）——日志保持在"持续可写"的状态，或已为发布收口。

三个标志对应三问：**齐了吗**（无遗漏）？**对吗**（归类/忠实）？**就绪了吗**（可继续积累或已收口）？——三个都答"是"，CHANGELOG 才算合格。

## 八、写在最后

`/harness-changelog` 的全部设计，浓缩成四句话：

1. **CHANGELOG 是发布说明书，写给读者看**——用户、下游、未来的自己。
2. **每条带 change ID，禁止"若干优化"**——日志是证据链的入口，不是一串无出处的字符串。
3. **面向使用者，不堆内部噪音**——内部重构不值得写，接口变化才值得写。
4. **不替发布做版本决策，不编造任何条目**——边界清晰，内容诚实。

一句话记住它：**/harness-changelog 是发行版的"出版编辑"——它把已完成的 change 按 Keep a Changelog 规范整理成可追溯、可扫读、可信赖的变更日志，让"这次发布改了什么"永远有据可查、有点可点。**