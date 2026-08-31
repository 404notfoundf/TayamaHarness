# /harness-db-design：数据模型，先想清楚再落库

> 命令深度拆解 · 第 31 篇 · 约 9000 字 · 7 个 SVG 图

---

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 300" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_d0" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_d0"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="300" fill="url(#bg_d0)" rx="10"/>
  <text x="400" y="28" text-anchor="middle" fill="#e2e8f0" font-size="26" font-weight="700">/harness-db-design：数据模型，先想清楚再落库</text>
  <rect x="60" y="55" width="320" height="95" rx="10" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_d0)"/>
  <text x="220" y="80" text-anchor="middle" fill="#93c5fd" font-size="14" font-weight="700">设计（不是执行）</text>
  <text x="220" y="104" text-anchor="middle" fill="#94a3b8" font-size="11">实体 → 表结构 → 索引 → 约束 → 迁移</text>
  <text x="220" y="126" text-anchor="middle" fill="#64748b" font-size="10">沉淀进 wiki/数据模型.md</text>
  <rect x="420" y="55" width="320" height="95" rx="10" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_d0)"/>
  <text x="580" y="80" text-anchor="middle" fill="#fde68a" font-size="14" font-weight="700">与 toolkit 分工</text>
  <text x="580" y="104" text-anchor="middle" fill="#94a3b8" font-size="11">设计归本技能，执行归</text>
  <text x="580" y="126" text-anchor="middle" fill="#94a3b8" font-size="11">database-migration-toolkit</text>
  <text x="400" y="185" text-anchor="middle" fill="#475569" font-size="13">四步流程：识别实体 → 表结构 → 变更评估 → 落盘</text>
  <rect x="60" y="205" width="160" height="45" rx="8" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_d0)"/>
  <text x="140" y="234" text-anchor="middle" fill="#86efac" font-size="11">① 识别实体</text>
  <rect x="240" y="205" width="160" height="45" rx="8" fill="#3b82f6" opacity="0.10" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_d0)"/>
  <text x="320" y="234" text-anchor="middle" fill="#93c5fd" font-size="11">② 表结构设计</text>
  <rect x="420" y="205" width="160" height="45" rx="8" fill="#ef4444" opacity="0.10" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_d0)"/>
  <text x="500" y="234" text-anchor="middle" fill="#fca5a5" font-size="11">③ 变更评估</text>
  <rect x="600" y="205" width="160" height="45" rx="8" fill="#a855f7" opacity="0.10" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_d0)"/>
  <text x="680" y="234" text-anchor="middle" fill="#d8b4fe" font-size="11">④ 落盘</text>
  <text x="400" y="280" text-anchor="middle" fill="#64748b" font-size="10">核心铁律：破坏性变更必须先评审、可回滚、避开高峰——禁止直接 DROP</text>
</svg>
```

## 一、/harness-db-design 解决的是什么问题

### 1.1 数据库设计是最贵的返工

写错一个函数，改一行代码；写错一个表结构，牵动的是整个服务的数据访问层、接口协议、迁移脚本、线上存量数据——**数据库设计是最贵的返工**。

`/harness-db-design` 的价值就是把这个"贵"的环节前置：在设计阶段就把表结构、索引、约束、迁移想清楚，而不是写代码写到一半发现表缺字段、接口返回要联表、存量数据要回填。

它从 `.harness/changes/<id>/change.md` 的需求与验收标准出发，产出：**实体 → 表结构 → 索引 → 约束 → 迁移脚本**，并把设计沉淀到 `.harness/wiki/数据模型.md`。设计不是一次性产出——它是 wiki 里持续演进的部分，每个 change 都在上面加一笔。

### 1.2 设计与执行的分离：db-design 与 database-migration-toolkit

本技能与 `database-migration-toolkit` 的分工是设计的核心思想：

| 维度 | harness-db-design（设计） | database-migration-toolkit（执行） |
|------|--------------------------|-----------------------------------|
| 负责 | 表结构该长什么样 | 把设计落地为可回滚的迁移脚本并执行 |
| 产出 | 设计文档 + 迁移文件 | 已执行的迁移 + 回滚能力 |
| 回答 | "模型对不对" | "迁移跑没跑成、能不能回滚" |

两者配合：本技能产出设计后，迁移脚本通常也一并生成（或交给 toolkit 落地）——**设计是决策，执行是事实**：设计错了改文档便宜，执行错了要处理线上数据。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 240" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_d1" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_d1"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="240" fill="url(#bg_d1)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">设计与执行的分离</text>
  <rect x="40" y="50" width="330" height="120" rx="10" fill="#3b82f6" opacity="0.10" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_d1)"/>
  <text x="205" y="76" text-anchor="middle" fill="#93c5fd" font-size="13" font-weight="700">/harness-db-design（设计层）</text>
  <text x="205" y="102" text-anchor="middle" fill="#94a3b8" font-size="10">决策：表结构/索引/约束/迁移文件</text>
  <text x="205" y="124" text-anchor="middle" fill="#94a3b8" font-size="10">沉淀：wiki/数据模型.md</text>
  <text x="205" y="146" text-anchor="middle" fill="#64748b" font-size="9">错了改文档便宜</text>
  <rect x="420" y="50" width="340" height="120" rx="10" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_d1)"/>
  <text x="590" y="76" text-anchor="middle" fill="#fde68a" font-size="13" font-weight="700">database-migration-toolkit（执行层）</text>
  <text x="590" y="102" text-anchor="middle" fill="#94a3b8" font-size="10">事实：迁移脚本已落地并执行</text>
  <text x="590" y="124" text-anchor="middle" fill="#94a3b8" font-size="10">能力：可回滚的 up/down</text>
  <text x="590" y="146" text-anchor="middle" fill="#64748b" font-size="9">错了要处理线上数据</text>
  <text x="400" y="200" text-anchor="middle" fill="#475569" font-size="11\">\"设计是决策，执行是事实\"——把两件事分给两个技能，各管各的责任</text>
  <text x="400" y="226" text-anchor="middle" fill="#64748b" font-size="10">设计产出后迁移脚本通常一并生成，或交给 toolkit 落地</text>
</svg>
```

### 1.3 为什么需要设计文档沉淀

设计文档不是流程包袱，而是"**团队共享的数据库心智模型**"。你接手一个三个月没人碰的表时，看 `数据模型.md` 的 erDiagram 一眼就懂；没有它，你得连查 N 个迁移文件才能拼出当前结构。这就是 wiki 沉淀与一次性设计的区别——**它服务的不只是写代码的当下，还有读代码的未来**。

## 二、前置：知道存量，才能谈增量

执行前置：

1. 按 `.harness/rules/变更定位规则.md` 定位目标 change（`status: analyzing` 或 `coding`）——设计发生在实现之前，所以 change 还在分析或编码阶段；
2. 加载 `.harness/wiki/数据模型.md`（若存在）——理解**存量模型**，新设计必须与之兼容。这个前置是"兼容性"的保障：没有存量视图，你设计的新表可能和旧表字段命名冲突、关系断裂；
3. 读取 change.md 的「验收标准」「边界情况」「契约影响 · 数据模型」。

一句话：**先看清存量，再动增量**。数据库设计最忌讳闭门造车——设计完发现和存量模型对不上，返工成本是双份的。

## 三、四步流程：实体 → 表结构 → 变更评估 → 落盘

### Step 1: 识别实体

从 AC 与用户故事提取实体（名词），确定实体间关系（1:1 / 1:N / M:N）与基数。

实体提取是朴素但关键的一步：**AC 里的名词就是实体的候选**。"用户下单，订单包含多个订单项"——用户、订单、订单项是实体；"一个订单包含多个订单项"是 1:N；"一个用户可以下多个订单"是 1:N。识别实体时问三个问题：

- 这个名词有没有独立生命周期？（有 → 实体；只是属性 → 字段）
- 它和其他实体什么关系、基数多少？
- 关系是否需要单独的表（M:N 通常需要中间表）？

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 260" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_d2" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_d2"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="260" fill="url(#bg_d2)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700\">Step 1：从 AC 提取实体</text>
  <rect x="40" y="55" width="360" height="60" rx="8" fill="#1e293b" stroke="#334155"/>
  <text x="220" y="78" text-anchor="middle" fill="#94a3b8" font-size="10">\"用户下单，订单包含多个订单项\"</text>
  <text x="220" y="100" text-anchor="middle" fill="#64748b" font-size="9">AC 原文：名词即候选实体</text>
  <rect x="130" y="135" width="140" height="60" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_d2)"/>
  <text x="200" y="158" text-anchor="middle" fill="#93c5fd" font-size="11" font-weight="700">用户 user</text>
  <text x="200" y="180" text-anchor="middle" fill="#64748b" font-size="9">1 ─── N 订单</text>
  <rect x="330" y="135" width="140" height="60" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_d2)"/>
  <text x="400" y="158" text-anchor="middle" fill="#86efac" font-size="11" font-weight="700">订单 order</text>
  <text x="400" y="180" text-anchor="middle" fill="#64748b" font-size="9">1 ─── N 订单项</text>
  <rect x="530" y="135" width="140" height="60" rx="8" fill="#a855f7" opacity="0.12" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_d2)"/>
  <text x="600" y="158" text-anchor="middle" fill="#d8b4fe" font-size="11" font-weight="700">订单项 order_item</text>
  <text x="600" y="180" text-anchor="middle" fill="#64748b" font-size="9\">M:N → 需要中间表</text>
  <text x="400" y="230" text-anchor="middle" fill="#475569" font-size="11\">三个问题：有独立生命周期？什么关系/基数？M:N 要不要中间表？</text>
</svg>
```

### Step 2: 表结构设计

对每个实体输出表结构：

| 字段 | 类型 | 约束（PK/FK/UNIQUE/NOT NULL） | 默认 | 说明 |
|------|------|-------------------------------|------|------|
| id | <DB 主键类型> | PK | auto | 主键 |
| <字段> | <类型> | <约束> | <默认> | <说明> |

设计要点共五条，每条都是避免返工的纪律：

**① 主键**：优先自增/雪花/UUID（按项目约定，见 编码规范.md），**禁止用业务字段做主键**。业务字段（订单号、手机号）会变、会重复、会被外部引用——用业务字段做主键，日后改它等于牵一发动全身；代理主键（自增/雪花/UUID）与业务解耦，业务随便改。

**② 索引**：查询条件列加索引；组合索引遵循最左前缀；禁止无索引的全表扫（除非表极小）。索引设计要对着查询路径来：WHERE 里最常用的列进索引，组合索引把最常过滤的列放最左。全表扫在几百万行上就是灾难——索引不是优化的点缀，是查询的底线。

**③ 约束**：业务不变式用 CHECK/UNIQUE 表达；外键按项目约定（如 MyBatis-Plus/JPA 生态常约定**逻辑外键 + 应用层校验**）。约束是把业务规则下沉到数据库层的最后防线：应用层校验可能漏，约束不会。但外键的选择要跟项目生态一致——ORM 生态常嫌物理外键拖性能，用逻辑外键+应用层校验，那就双方约定齐整，别一半物理一半逻辑。

**④ 软删除**：如项目约定 `deleted` 字段，全表统一。软删除的好处是可恢复、可审计；代价是每个查询都要带 `deleted = 0`。要软删就全表统一约定，不要有的表有有的表没有——不一致才是灾难。

**⑤ 时间字段**：`created_at` / `updated_at` 统一命名与默认值。两个时间字段是每个表的"出生证明"和"体检报告"——排查数据问题、做时间维度统计都靠它。统一命名让任何表的结构都长一个样，降低心智负担。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 300" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_d3" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_d3"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="300" fill="url(#bg_d3)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">设计要点五条</text>
  <rect x="40" y="45" width="220" height="50" rx="8" fill="#3b82f6" opacity="0.10" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_d3)"/>
  <text x="150" y="68" text-anchor="middle" fill="#93c5fd" font-size="11" font-weight="700">① 主键</text>
  <text x="150" y="86" text-anchor="middle" fill="#64748b" font-size="8">禁业务字段做主键</text>
  <rect x="290" y="45" width="220" height="50" rx="8" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_d3)"/>
  <text x="400" y="68" text-anchor="middle" fill="#86efac" font-size="11" font-weight="700">② 索引</text>
  <text x="400" y="86" text-anchor="middle" fill="#64748b" font-size="8">查询列加索引·最左前缀</text>
  <rect x="540" y="45" width="220" height="50" rx="8" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_d3)"/>
  <text x="650" y="68" text-anchor="middle" fill="#fde68a" font-size="11" font-weight="700">③ 约束</text>
  <text x="650" y="86" text-anchor="middle" fill="#64748b" font-size="8">CHECK/UNIQUE·外键按约定</text>
  <rect x="40" y="115" width="220" height="50" rx="8" fill="#a855f7" opacity="0.10" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_d3)"/>
  <text x="150" y="138" text-anchor="middle" fill="#d8b4fe" font-size="11" font-weight="700">④ 软删除</text>
  <text x="150" y="156" text-anchor="middle" fill="#64748b" font-size="8">项目约定就全表统一</text>
  <rect x="290" y="115" width="220" height="50" rx="8" fill="#ef4444" opacity="0.10" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_d3)"/>
  <text x="400" y="138" text-anchor="middle" fill="#fca5a5" font-size="11" font-weight="700">⑤ 时间字段</text>
  <text x="400" y="156" text-anchor="middle" fill="#64748b" font-size="8">created_at/updated_at 统一</text>
  <text x="400" y="215" text-anchor="middle" fill="#475569" font-size="11">每条都对应一种"返工形态"</text>
  <text x="400" y="245" text-anchor="middle" fill="#64748b" font-size="10">业务主键日后必改 · 无索引查询必慢 · 约束不统一必漏</text>
  <text x="400" y="268" text-anchor="middle" fill="#64748b" font-size="10">软删不一致必乱 · 时间字段不统一排查必懵</text>
</svg>
```

### Step 3: 变更评估（对存量表）

若改动存量表，列出**兼容性清单**：

| 存量表 | 变更类型（加字段/改类型/拆表/归档） | 是否破坏性 | 迁移策略 |
|--------|----------------------------------|-----------|---------|
| <表> | <变更> | 🟢 兼容 / 🔴 破坏 | <先加字段后回填 / 新表+切换+删旧> |

兼容性评估的精髓是**区分变更的破坏性**：

- 🟢 **兼容变更**：加字段（带默认值）、加索引、加约束——存量数据不受影响，可以先加后回填；
- 🔴 **破坏性变更**：删列、改类型、改约束——存量数据可能丢失或语义变化，必须走评审 + 回滚预案 + 避开高峰。

**铁律：破坏性变更（删列/改类型/改约束）必须先评审、可回滚、避开高峰，禁止直接 `DROP`。**

这条铁律把"数据库操作"和"普通代码操作"区分开了：代码改错可以秒回滚，`DROP` 了列就是回不来的。所以破坏性变更的迁移策略宁可绕远路：先加新字段 → 双写/回填 → 切换读 → 确认稳定 → 再删旧（"新表+切换+删旧"三段式），也不要一步到位删掉存量。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 240" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_d4" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_d4"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="240" fill="url(#bg_d4)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">变更评估：先分清是否破坏</text>
  <rect x="40" y="50" width="330" height="110" rx="10" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_d4)"/>
  <text x="205" y="76" text-anchor="middle" fill="#86efac" font-size="13" font-weight="700">🟢 兼容变更</text>
  <text x="205" y="102" text-anchor="middle" fill="#94a3b8" font-size="10">加字段(带默认)/加索引/加约束</text>
  <text x="205" y="124" text-anchor="middle" fill="#94a3b8" font-size="10">存量不受影响 → 先加后回填</text>
  <text x="205" y="148" text-anchor="middle" fill="#64748b" font-size="9">低频风险</text>
  <rect x="420" y="50" width="330" height="110" rx="10" fill="#ef4444" opacity="0.10" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_d4)"/>
  <text x="585" y="76" text-anchor="middle" fill="#fca5a5" font-size="13" font-weight="700">🔴 破坏性变更</text>
  <text x="585" y="102" text-anchor="middle" fill="#94a3b8" font-size="10">删列/改类型/改约束</text>
  <text x="585" y="124" text-anchor="middle" fill="#94a3b8" font-size="10">先评审·可回滚·避开高峰</text>
  <text x="585" y="148" text-anchor="middle" fill="#64748b" font-size="9">禁止直接 DROP</text>
  <text x="400" y="196" text-anchor="middle" fill="#475569" font-size="11">迁移策略绕远路指南：新表+切换+删旧，三段式降风险</text>
  <text x="400" y="222" text-anchor="middle" fill="#64748b" font-size="10">代码改错可秒回，DROP 了列回不来——数据库操作永远多留一条退路</text>
</svg>
```

### Step 4: 落盘

设计完成后进入落盘，三件事：

1. **更新 `.harness/wiki/数据模型.md`**：实体关系图（Mermaid erDiagram）+ 表结构 + 变更记录。erDiagram 给人看全局关系，表结构给人看细节，变更记录留下演进轨迹——三个视角合起来才是完整的模型文档；
2. **生成迁移脚本**（或调用 `database-migration-toolkit` 落地）：命名含版本号与变更 ID（如 `V<c>__C-0NN_<desc>.sql`），含 `up` 与 `down`（回滚）——迁移文件带 change ID，让"哪个表谁改的"可追溯，`down` 脚本保证任何一步都能回头；
3. **在 change.md 的「契约影响 · 数据模型」补充本次模型变更摘要**——把设计决策带回 change 卡，让评审/回溯时能在 change.md 里看到数据层影响了什么。

## 四、自检清单

落盘前的最后一道闸——把设计过一遍清单：

- [ ] 每个实体有明确主键，索引覆盖查询路径
- [ ] 无破坏性变更未标注/未评审
- [ ] 迁移脚本含回滚（down）
- [ ] `数据模型.md` 已同步，无"代码改完了文档还是旧的"

自检清单的价值在最后一条：**"代码改完了文档还是旧的"是数据库文档最常见的死法**。设计文档只有在 change 完成时仍与代码同步，它才有下一次被信任的资格；文档一旧，后面所有人都不再去看它，模型心智就断了。

## 五、与相邻技能的分工

| 场景 | 归属 |
|------|------|
| 数据模型设计 | **本技能**（`/harness-db-design`） |
| 迁移脚本执行/回滚 | `database-migration-toolkit` |
| 数据访问层编码 | `/coding-skill` |
| 评审数据模型 | `/expert-reviewer` |

- **database-migration-toolkit**：设计的上游/执行搭档——本技能决定"长什么样"，toolkit 负责"落地"；
- **coding-skill**：数据访问层编码（DAO/Mapper）消费设计产出——表设计好了，编码才有根据；
- **expert-reviewer**：评审数据模型——评审时带着 `数据模型.md` 和迁移脚本，看设计是否自洽、是否遗漏约束。

## 六、完成标志

`/harness-db-design` 的完成标志有三个：

1. **表结构/索引/约束已设计并自检通过**——设计本身完成且过了一遍自检清单；
2. **`数据模型.md` 已更新**（含 erDiagram 与变更记录）——设计沉淀进了共享心智；
3. **迁移脚本已生成**（含回滚），或已交 `database-migration-toolkit` 落地——设计有了可执行的载体。

三个标志对应三层承诺：想清楚了（设计已自检）、记下来了（wiki 已同步）、能落地了（迁移脚本含 up/down）。**缺任何一个，设计都是"半成品"——尤其迁移脚本，没有 down 的设计等于把退路提前烧了。**

## 七、写在最后

`/harness-db-design` 的全部设计，浓缩成四句话：

1. **设计是最贵的返工，所以前置**——表结构想清楚再写代码，比写一半返工便宜一个数量级。
2. **设计与执行分离**——设计归 db-design（决策），执行归 toolkit（事实），错了各承担各的代价。
3. **记入 wiki，才叫完成**——erDiagram + 表结构 + 变更记录，让未来的人打开 `数据模型.md` 就懂全部。
4. **破坏性变更永远留退路**——评审、可回滚、避开高峰，禁止直接 DROP。

一句话记住它：**/harness-db-design 是数据模型的"设计院"——它把表结构、索引、约束与迁移在设计阶段想清楚并沉淀进 wiki，让"数据库是最贵的返工"变成"数据库是最稳的资产"。**