# /resolving-merge-conflicts：让冲突解决讲原则

> 命令深度拆解 · 第 25 篇 · 约 8500 字 · 7 个 SVG 图

---

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 300" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_m0" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_m0"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="300" fill="url(#bg_m0)" rx="10"/>
  <text x="400" y="28" text-anchor="middle" fill="#e2e8f0" font-size="26" font-weight="700">/resolving-merge-conflicts：让冲突解决讲原则</text>
  <rect x="60" y="55" width="280" height="95" rx="10" fill="#ef4444" opacity="0.12" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_m0)"/>
  <text x="200" y="80" text-anchor="middle" fill="#fca5a5" font-size="14" font-weight="700">看状态 → 找一手来源</text>
  <text x="200" y="104" text-anchor="middle" fill="#94a3b8" font-size="11">理解每个改动为什么发生</text>
  <text x="200" y="126" text-anchor="middle" fill="#64748b" font-size="10">读 commit message / PR / issue</text>
  <rect x="460" y="55" width="280" height="95" rx="10" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_m0)"/>
  <text x="600" y="80" text-anchor="middle" fill="#86efac" font-size="14" font-weight="700">逐个解决 → 跑检查 → 完成</text>
  <text x="600" y="104" text-anchor="middle" fill="#94a3b8" font-size="11">保留双方意图 · 不发明新行为</text>
  <text x="600" y="126" text-anchor="middle" fill="#64748b" font-size="10">绝不 --abort · 修复合并副产物</text>
  <text x="400" y="185" text-anchor="middle" fill="#475569" font-size="13">五步流程，四句核心原则</text>
  <text x="120" y="215" text-anchor="middle" fill="#94a3b8" font-size="11">① 保留双方意图</text>
  <text x="320" y="215" text-anchor="middle" fill="#94a3b8" font-size="11">② 不发明新行为</text>
  <text x="515" y="215" text-anchor="middle" fill="#94a3b8" font-size="11">③ 绝不一跑了之</text>
  <text x="695" y="215" text-anchor="middle" fill="#94a3b8" font-size="11">④ 修复合并副产物</text>
  <text x="400" y="252" text-anchor="middle" fill="#64748b" font-size="11">核心洞察：冲突不是\"选一边\"的剪刀，是\"两个意图都要兑现\"的缝合</text>
  <text x="400" y="278" text-anchor="middle" fill="#475569" font-size="10">始终解决，绝不 --abort——除非人类明确要求</text>
</svg>
```

## 一、/resolving-merge-conflicts 解决的是什么问题

### 1.1 冲突不是事故，是常态

在多人协作的仓库里，merge/rebase 冲突不是"出事了"，而是**工作的常态**。两条分支各自修改了同一处代码，git 无法自动合并——这是版本控制的正常产物，不是 bug。

但处理冲突的方式，决定了冲突是"一天的麻烦"还是"一周的灾难"：

- **没原则地解决**：看哪个 hunk 顺眼选哪个，或者干脆 `git checkout --ours` 整文件覆盖——结果把对方的改动静默丢掉，bug 在两周后才浮出水面，且没人知道是谁丢的。
- **有原则地解决**：先理解双方各自的意图，再尽量同时保留；保留不了的，选符合 merge 目标的那个并记录权衡——合并后跑自动化检查，确保没有留下合并副产物。

`/resolving-merge-conflicts` 是后者的执行手册。它有五步流程和四句核心原则，把"解决冲突"从"手忙脚乱地选边"变成"有条不紊地缝合"。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 240" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_m1" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_m1"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="240" fill="url(#bg_m1)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">两种解决方式：从选边到缝合</text>
  <rect x="40" y="50" width="350" height="130" rx="10" fill="#ef4444" opacity="0.08" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_m1)"/>
  <text x="215" y="74" text-anchor="middle" fill="#fca5a5" font-size="13" font-weight="700">没原则：剪刀思维</text>
  <text x="215" y="100" text-anchor="middle" fill="#94a3b8" font-size="10">看哪个顺眼选哪个</text>
  <text x="215" y="120" text-anchor="middle" fill="#94a3b8" font-size="10">--ours 整文件覆盖</text>
  <text x="215" y="140" text-anchor="middle" fill="#94a3b8" font-size="10">对方改动静默丢失</text>
  <text x="215" y="164" text-anchor="middle" fill="#64748b" font-size="9">bug 两周后浮现，无人可追</text>
  <rect x="420" y="50" width="340" height="130" rx="10" fill="#22c55e" opacity="0.08" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_m1)"/>
  <text x="590" y="74" text-anchor="middle" fill="#86efac" font-size="13" font-weight="700">有原则：缝合思维</text>
  <text x="590" y="100" text-anchor="middle" fill="#94a3b8" font-size="10">先理解双方意图</text>
  <text x="590" y="120" text-anchor="middle" fill="#94a3b8" font-size="10">尽量同时保留两边</text>
  <text x="590" y="140" text-anchor="middle" fill="#94a3b8" font-size="10">无法兼得时记录权衡</text>
  <text x="590" y="164" text-anchor="middle" fill="#64748b" font-size="9">合并后跑自动检查，无副产物</text>
  <text x="400" y="210" text-anchor="middle" fill="#475569" font-size="11">冲突解决的质量差异 = 未来 bug 的存量差异</text>
  <text x="400" y="230" text-anchor="middle" fill="#64748b" font-size="10">剪刀快一时，缝合省一世</text>
</svg>
```

### 1.2 冲突解决的真正目标

在动手改第一个 hunk 之前，必须想清楚目标。`/resolving-merge-conflicts` 的目标是：**两条分支的意图同时兑现，且不引入任何新行为**。

这个目标有三层含义：

1. **意图优先**：冲突双方的改动都是有人认真做的，都有存在的理由。解决的目标不是"少写几行"，而是"两边都别白做"。
2. **不发明**：冲突解决不是写新代码的机会。如果解决过程"顺手"加了重构、改了命名、动了无关逻辑——这就越界了。冲突解决的产出应该是"两边的代码共存"，而不是"我的新版本"。
3. **无副产物**：合并本身可能破坏编译、破坏测试、破坏格式（比如两个人都格式化了同一个文件）。这一步不在冲突 hunk 里，但它是合并的副作用，必须通过自动化检查发现并修复。

## 二、五步流程全景

`/resolving-merge-conflicts` 的执行是五步，顺序不能乱——后一步依赖前一步的信息：

```
Step 1  看当前状态        → 我在哪？卡在哪？哪些文件冲突？
Step 2  找一手来源        → 每处改动为什么发生？原始意图是什么？
Step 3  逐个解决 hunk     → 保留双方意图；不兼容时选符合目标的一方并记录
Step 4  跑自动化检查      → typecheck → tests → format；修复合并破坏的东西
Step 5  完成 merge/rebase → stage + commit；rebase 则继续直到全部完成
```

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 300" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_m2" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_m2"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="300" fill="url(#bg_m2)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">五步流程：信息逐层积累</text>
  <rect x="40" y="50" width="130" height="80" rx="8" fill="#3b82f6" opacity="0.10" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_m2)"/>
  <text x="105" y="76" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">① 看状态</text>
  <text x="105" y="98" text-anchor="middle" fill="#94a3b8" font-size="9">在哪·卡哪·哪些文件</text>
  <text x="105" y="116" text-anchor="middle" fill="#64748b" font-size="9">merge/rebase 状态</text>
  <line x1="170" y1="90" x2="200" y2="90" stroke="#475569" stroke-width="2"/>
  <polygon points="206,90 198,85 198,95" fill="#94a3b8"/>
  <rect x="215" y="50" width="130" height="80" rx="8" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_m2)"/>
  <text x="280" y="76" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">② 找来源</text>
  <text x="280" y="98" text-anchor="middle" fill="#94a3b8" font-size="9">为什么改·什么意图</text>
  <text x="280" y="116" text-anchor="middle" fill="#64748b" font-size="9">commit / PR / issue</text>
  <line x1="345" y1="90" x2="375" y2="90" stroke="#475569" stroke-width="2"/>
  <polygon points="381,90 373,85 373,95" fill="#94a3b8"/>
  <rect x="390" y="50" width="130" height="80" rx="8" fill="#a855f7" opacity="0.10" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_m2)"/>
  <text x="455" y="76" text-anchor="middle" fill="#d8b4fe" font-size="12" font-weight="700">③ 解决</text>
  <text x="455" y="98" text-anchor="middle" fill="#94a3b8" font-size="9">逐个 hunk 缝合</text>
  <text x="455" y="116" text-anchor="middle" fill="#64748b" font-size="9">记录权衡</text>
  <line x1="520" y1="90" x2="550" y2="90" stroke="#475569" stroke-width="2"/>
  <polygon points="556,90 548,85 548,95" fill="#94a3b8"/>
  <rect x="565" y="50" width="130" height="80" rx="8" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_m2)"/>
  <text x="630" y="76" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">④ 跑检查</text>
  <text x="630" y="98" text-anchor="middle" fill="#94a3b8" font-size="9">typecheck→test→format</text>
  <text x="630" y="116" text-anchor="middle" fill="#64748b" font-size="9">修复合并副产物</text>
  <line x1="695" y1="90" x2="720" y2="90" stroke="#475569" stroke-width="2"/>
  <polygon points="726,90 718,85 718,95" fill="#94a3b8"/>
  <rect x="335" y="165" width="130" height="60" rx="8" fill="#ef4444" opacity="0.10" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_m2)"/>
  <text x="400" y="188" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">⑤ 完成</text>
  <text x="400" y="208" text-anchor="middle" fill="#94a3b8" font-size="9">stage + commit</text>
  <text x="400" y="228" text-anchor="middle" fill="#64748b" font-size="9">rebase：继续直到全部完成</text>
  <text x="400" y="262" text-anchor="middle" fill="#475569" font-size="11">每步都建立在前一步的信息上——跳步 = 在信息不全时做决策</text>
  <text x="400" y="285" text-anchor="middle" fill="#64748b" font-size="10">特别是 ②③ 必须先于 ④⑤：不理解的冲突，跑检查也救不回来</text>
</svg>
```

## 三、Step 1-2：动手前先把地图和史料看全

### 3.1 Step 1：看当前状态

冲突解决的第一步不是"打开第一个冲突文件"，而是**看全局**：

- **merge 还是 rebase？**——两者的完成方式不同：merge 是"合并到当前分支"，rebase 是"把提交逐个重放到目标分支上"。先确认操作类型。
- **当前状态**：`git status` 看哪些文件冲突、哪些已暂存、哪些未追踪。
- **git 历史**：`git log` 看冲突相关的提交，理解这个 merge/rebase 正在连接哪些历史。

这一步的信息全部来自"看"，不来自"猜"。状态没看清就动手，等于蒙眼拆弹。

### 3.2 Step 2：为每个冲突找一手来源

这是五步里最容易被跳过的，也是决定成败的一步。**为每个冲突找一手来源：深入理解每个改动为什么发生、原始意图是什么。**

一手来源包括：

| 来源 | 能提供什么 |
|------|-----------|
| commit message | 改动作者的意图声明 |
| PR 描述与讨论 | 改动背后的权衡与取舍 |
| 原始 issue/ticket | 改动要解决的业务问题 |
| 相邻代码的注释 | 实现层面的上下文 |

为什么要"找一手来源"而不是"看当前代码猜"？因为**当前代码只是结果，意图藏在历史里**。`order` 变量被重命名成了 `purchase`，光看冲突现场不知道哪边是对的；看了 commit message 才知道"上游统一术语，把 order 全部改名为 purchase"——那解决方向就定了：跟上游。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 220" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_m3" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_m3"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="220" fill="url(#bg_m3)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">一手来源：意图藏在历史里</text>
  <rect x="40" y="50" width="170" height="80" rx="10" fill="#3b82f6" opacity="0.10" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_m3)"/>
  <text x="125" y="74" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">commit message</text>
  <text x="125" y="98" text-anchor="middle" fill="#94a3b8" font-size="10">改动的意图声明</text>
  <text x="125" y="118" text-anchor="middle" fill="#64748b" font-size="9">"统一术语 order→purchase"</text>
  <rect x="230" y="50" width="170" height="80" rx="10" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_m3)"/>
  <text x="315" y="74" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">PR 讨论</text>
  <text x="315" y="98" text-anchor="middle" fill="#94a3b8" font-size="10">取舍与权衡</text>
  <text x="315" y="118" text-anchor="middle" fill="#64748b" font-size="9">"为什么选了 B 方案"</text>
  <rect x="420" y="50" width="170" height="80" rx="10" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_m3)"/>
  <text x="505" y="74" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">issue/ticket</text>
  <text x="505" y="98" text-anchor="middle" fill="#94a3b8" font-size="10">要解决的业务问题</text>
  <text x="505" y="118" text-anchor="middle" fill="#64748b" font-size="9">"修复超时重试死循环"</text>
  <rect x="610" y="50" width="150" height="80" rx="10" fill="#a855f7" opacity="0.10" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_m3)"/>
  <text x="685" y="74" text-anchor="middle" fill="#d8b4fe" font-size="12" font-weight="700">代码注释</text>
  <text x="685" y="98" text-anchor="middle" fill="#94a3b8" font-size="10">实现上下文</text>
  <text x="685" y="118" text-anchor="middle" fill="#64748b" font-size="9">"并发下必须加锁"</text>
  <text x="400" y="165" text-anchor="middle" fill="#475569" font-size="11">当前代码是结果，意图藏在历史里——不读史料就缝合，等于赌</text>
  <text x="400" y="195" text-anchor="middle" fill="#64748b" font-size="10">例子：order→purchase 重命名冲突，看了 commit 才知道"跟上游"才是对的</text>
</svg>
```

### 3.3 一手来源解决的经典困境

**困境**：`master` 把 `order.Status` 改成了 `order.State`（枚举值也变了），你的分支在 `order.Status` 上加了一段校验逻辑。冲突现场是："上游删了 Status、你还在用 Status"。

看一手来源后：上游 commit 说"Status 语义含混，拆成 State + PaymentState 两个字段"。那你的校验逻辑就该落到新的 `State` 上，而不是"把校验塞回 Status"。**理解了意图，冲突解决的答案自然浮现**；不理解意图，只能二选一赌运气。

## 四、Step 3：逐个解决每个 hunk

### 4.1 保留双方意图：缝合而不是选边

看完一手来源后，逐个解决每个 hunk。核心原则第一条：**尽可能保留双方意图**。

很多冲突并不是"非此即彼"：

- 一边加了一个新函数，另一边改了调用它的参数——可以两边都要。
- 一边重命名了变量，另一边在这一行加了注释——可以两边都要。
- 一边改了逻辑，另一边改了格式——可以两边都要。

判断能否"都要"的标准：**双方的改动是否作用于不同的关注点**。是 → 缝合；否 → 取舍。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 300" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_m4" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_m4"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="300" fill="url(#bg_m4)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">缝合 vs 取舍：判断标准</text>
  <rect x="40" y="50" width="350" height="140" rx="10" fill="#22c55e" opacity="0.08" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_m4)"/>
  <text x="215" y="74" text-anchor="middle" fill="#86efac" font-size="13" font-weight="700">缝合：双方作用于不同关注点</text>
  <text x="215" y="102" text-anchor="middle" fill="#94a3b8" font-size="10">一边加函数，一边改调用 → 都保留</text>
  <text x="215" y="124" text-anchor="middle" fill="#94a3b8" font-size="10">一边改逻辑，一边改格式 → 都保留</text>
  <text x="215" y="146" text-anchor="middle" fill="#94a3b8" font-size="10">一边重命名，一边加注释 → 都保留</text>
  <text x="215" y="172" text-anchor="middle" fill="#64748b" font-size="9">两个意图都能兑现 → 不要牺牲任何一边</text>
  <rect x="420" y="50" width="340" height="140" rx="10" fill="#ef4444" opacity="0.08" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_m4)"/>
  <text x="590" y="74" text-anchor="middle" fill="#fca5a5" font-size="13" font-weight="700">取舍：双方改的是同一件事且冲突</text>
  <text x="590" y="102" text-anchor="middle" fill="#94a3b8" font-size="10">同一变量被两边改成不同值</text>
  <text x="590" y="124" text-anchor="middle" fill="#94a3b8" font-size="10">同一逻辑被两边重写成两版</text>
  <text x="590" y="146" text-anchor="middle" fill="#94a3b8" font-size="10">→ 选符合 merge 目标的一方</text>
  <text x="590" y="172" text-anchor="middle" fill="#64748b" font-size="9">并记录：为什么选这一边、牺牲了什么</text>
  <text x="400" y="220" text-anchor="middle" fill="#475569" font-size="11">取舍时的记录 = 未来读者的救命稻草——不记录，就没有"权衡"，只有"随手"</text>
  <text x="400" y="248" text-anchor="middle" fill="#64748b" font-size="10">保留不了的那一边，请用注释/commit 说明它去哪了，别让它无声消失</text>
  <text x="400" y="280" text-anchor="middle" fill="#64748b" font-size="10">缝合优先、取舍次之、记录兜底</text>
</svg>
```

### 4.2 不兼容时：选符合目标的一方并记录权衡

当双方确实作用于同一件事且意图冲突（比如同一变量的两个新值），必须取舍。取舍的三条纪律：

1. **以 merge 目标为准**：如果是在把功能分支合回主干的路上，主干侧（或正在完成的功能侧）的意图通常优先——但必须结合 Step 2 的史料判断，不盲目"偏向主干"。
2. **记录权衡**：解决结果里/commit message 里写明"这里选择了 A 侧，因为 B 侧的意图 X 已被 A 侧的新结构覆盖"。这样未来有人发现丢了东西，能顺着记录找回。
3. **绝不静默丢弃**：最坏的解决不是"选错了边"，而是"丢了东西还没人知道"。

### 4.3 不发明新行为：解决 ≠ 重构

这一条需要反复强调：**冲突解决不引入需求外逻辑**。

冲突解决时的典型越界行为：

- "顺手"把冲突区域重构得更优雅；
- "顺手"统一了命名（不在本次 merge 范围内）；
- "顺手"修了个无关 bug；
- "顺手"加了防御性代码。

每次越界，都在把"合并两条分支"变成"合并且开发"。后果是：**冲突解决者的个人判断混进历史，review 时没人能分清哪些是合并、哪些是新代码**——而合并通常没有严格的 review 流程。

纪律：**hunk 内外只做让合并成立的最小改动**。要重构、要改命名、要修 bug，单独提 PR，别混进冲突解决。

## 五、Step 4-5：修复副产物并完成

### 5.1 Step 4：发现项目的自动化检查并运行

合并本身会留下**副产物**——不在任何冲突 hunk 里的破坏：

- 两个人都格式化过同一个文件 → 合并后格式混乱；
- 一边改了函数签名，另一边在别处调用 → 合并后编译失败（不一定在冲突区域）；
- 一边删了某个导出符号，另一边还在用 → 合并后测试失败。

副产物只能靠**自动化检查**发现。顺序通常是：**先 typecheck，再 tests，再 format**。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 240" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_m5" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_m5"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="240" fill="url(#bg_m5)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">合并副产物：不在 hunk 里的破坏</text>
  <rect x="40" y="52" width="230" height="110" rx="10" fill="#ef4444" opacity="0.10" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_m5)"/>
  <text x="155" y="76" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">格式混乱</text>
  <text x="155" y="102" text-anchor="middle" fill="#94a3b8" font-size="10">两方都格式化过同一文件</text>
  <text x="155" y="124" text-anchor="middle" fill="#64748b" font-size="9">→ format 检查发现</text>
  <rect x="290" y="52" width="230" height="110" rx="10" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_m5)"/>
  <text x="405" y="76" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">编译破坏</text>
  <text x="405" y="102" text-anchor="middle" fill="#94a3b8" font-size="10">签名改了，别处还按旧的调</text>
  <text x="405" y="124" text-anchor="middle" fill="#64748b" font-size="9">→ typecheck 发现</text>
  <rect x="540" y="52" width="220" height="110" rx="10" fill="#3b82f6" opacity="0.10" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_m5)"/>
  <text x="650" y="76" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">行为回归</text>
  <text x="650" y="102" text-anchor="middle" fill="#94a3b8" font-size="10">导出符号被删，调用方挂</text>
  <text x="650" y="124" text-anchor="middle" fill="#64748b" font-size="9">→ tests 发现</text>
  <text x="400" y="196" text-anchor="middle" fill="#475569" font-size="11">副产物只在合并后存在——"解决完 hunk 就算完"是最危险的错觉</text>
  <text x="400" y="222" text-anchor="middle" fill="#64748b" font-size="10">typecheck → tests → format，一步不能少</text>
</svg>
```

**顺序有讲究**：typecheck 最便宜、发现问题最基础（编译不过，测试白跑）；tests 验证行为有没有被合并破坏；format 最表层。如果 typecheck 就红了，先修再往下——不带着编译错误跑测试。

### 5.2 Step 5：完成 merge/rebase

全部解决 + 检查通过后，进入收尾：

- **merge**：`git add` 所有文件，`git commit`——完成合并提交。
- **rebase**：`git add` + `git rebase --continue`——`rebase` 是**逐提交重放**，一次 `--continue` 可能只是解决了当前提交的冲突，后续提交可能还有新的冲突。**继续 rebase 直到所有 commit 都被 rebase**，不能停在一个"看起来完成了"的中间态。

### 5.3 绝不一跑了之

`/resolving-merge-conflicts` 最硬的一条纪律：**始终解决，绝不 `--abort`**——除非人类明确要求。

为什么？因为 `--abort` 意味着：

1. 放弃当前所有解决努力——之前的工作全部作废；
2. 冲突原封不动地留着，只是换个时间、换个 context 再来一次；
3. 更糟的是，`--abort` 常常被用来逃避"不理解的冲突"——冲突不会因为 abort 而消失。

`--abort` 的合法唯一场景：**人类明确要求放弃**（比如决定这个 merge 本身不该发生）。Agent 不该自己决定 abort——因为"放弃"是需求级决策（要不要合并），不是执行级决策（怎么解决冲突）。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 240" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_m6" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_m6"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="240" fill="url(#bg_m6)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">为什么绝不 --abort</text>
  <rect x="40" y="52" width="230" height="110" rx="10" fill="#ef4444" opacity="0.10" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_m6)"/>
  <text x="155" y="76" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">努力全部作废</text>
  <text x="155" y="102" text-anchor="middle" fill="#94a3b8" font-size="10">已解决的 hunk 白干</text>
  <text x="155" y="124" text-anchor="middle" fill="#64748b" font-size="9">过去的投入归零</text>
  <rect x="290" y="52" width="230" height="110" rx="10" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_m6)"/>
  <text x="405" y="76" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">问题不会消失</text>
  <text x="405" y="102" text-anchor="middle" fill="#94a3b8" font-size="10">换个时间换个 context 再来</text>
  <text x="405" y="124" text-anchor="middle" fill="#64748b" font-size="9">逃避不理解的冲突无意义</text>
  <rect x="540" y="52" width="220" height="110" rx="10" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_m6)"/>
  <text x="650" y="76" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">唯一合法场景</text>
  <text x="650" y="102" text-anchor="middle" fill="#94a3b8" font-size="10">人类明确要求放弃</text>
  <text x="650" y="124" text-anchor="middle" fill="#64748b" font-size="9">"要不要合并"是需求级决策</text>
  <text x="400" y="190" text-anchor="middle" fill="#475569" font-size="11">abort 是需求级决策（要不要合并），不是执行级决策（怎么解决冲突）</text>
  <text x="400" y="214" text-anchor="middle" fill="#64748b" font-size="10">Agent 不该替人类决定"放弃"——放弃权只属于人</text>
  <text x="400" y="232" text-anchor="middle" fill="#475569" font-size="10">始终解决，绝不 --abort，除非人类明确要求</text>
</svg>
```

## 六、完成标志：怎样算"解决了"

`/resolving-merge-conflicts` 的完成标志有三条，全部满足才算真正解决：

1. **所有冲突 hunk 已解决**——没有遗留的 `<<<<<<<` 标记。这一步可以机械验证（搜索冲突标记）。
2. **变更已 stage 并 commit（或 rebase 完成）**——`git status` 干净、merge/rebase 状态已退出。
3. **自动化检查通过**——typecheck / tests / format 全绿，没有合并副产物。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 200" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_m7" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_m7"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="200" fill="url(#bg_m7)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">三个完成标志：都可机械验证</text>
  <rect x="40" y="52" width="230" height="90" rx="10" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_m7)"/>
  <text x="155" y="76" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">① hunk 全解决</text>
  <text x="155" y="100" text-anchor="middle" fill="#94a3b8" font-size="10">无 <&lt;<<<<< 残留标记</text>
  <text x="155" y="120" text-anchor="middle" fill="#64748b" font-size="9">grep 即可验证</text>
  <rect x="290" y="52" width="230" height="90" rx="10" fill="#3b82f6" opacity="0.10" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_m7)"/>
  <text x="405" y="76" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">② 已 stage + commit</text>
  <text x="405" y="100" text-anchor="middle" fill="#94a3b8" font-size="10">merge/rebase 状态退出</text>
  <text x="405" y="120" text-anchor="middle" fill="#64748b" font-size="9">git status 干净</text>
  <rect x="540" y="52" width="220" height="90" rx="10" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_m7)"/>
  <text x="650" y="76" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">③ 自动化检查通过</text>
  <text x="650" y="100" text-anchor="middle" fill="#94a3b8" font-size="10">typecheck/tests/format</text>
  <text x="650" y="120" text-anchor="middle" fill="#64748b" font-size="9">无合并副产物</text>
  <text x="400" y="176" text-anchor="middle" fill="#64748b" font-size="10">三条都指向可检查的事实——没有一条靠"我感觉"</text>
</svg>
```

## 七、写在最后

`/resolving-merge-conflicts` 的全部设计，浓缩成五句话：

1. **先看状态、再找史料、最后动手**——信息顺序决定解决质量。
2. **缝合优先于选边**——冲突双方都有意图，能同时兑现就别牺牲任何一边。
3. **取舍必须记录**——被牺牲的意图留下文字，未来的人才能找回来。
4. **不发明新行为**——冲突解决是合并，不是开发。hunk 之外的事，单独提 PR。
5. **绝不 --abort**——放弃是人类的决策，不是执行者的选项。

一句话记住它：**/resolving-merge-conflicts 是让"两条分支碰撞"变成"两个意图共存"的缝合师——它不选边站队，它让两边都不白干。**