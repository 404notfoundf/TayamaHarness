# /database-migration-toolkit：数据库迁移——没有回滚脚本，就不许执行

> 命令深度拆解 · 第 38 篇 · 约 8000 字 · 7 个 SVG 图

---

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 300" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_d0" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_d0"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="300" fill="url(#bg_d0)" rx="10"/>
  <text x="400" y="28" text-anchor="middle" fill="#e2e8f0" font-size="26" font-weight="700">/database-migration-toolkit：迁移三件套</text>
  <rect x="60" y="55" width="320" height="95" rx="10" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_d0)"/>
  <text x="220" y="80" text-anchor="middle" fill="#93c5fd" font-size="14" font-weight="700">正向迁移 V{version}</text>
  <text x="220" y="104" text-anchor="middle" fill="#94a3b8" font-size="11">DDL + DML，向前兼容</text>
  <text x="220" y="126" text-anchor="middle" fill="#64748b" font-size="10">新增列允许 NULL · 大表走 Online DDL</text>
  <rect x="420" y="55" width="320" height="95" rx="10" fill="#ef4444" opacity="0.12" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_d0)"/>
  <text x="580" y="80" text-anchor="middle" fill="#fca5a5" font-size="14" font-weight="700">回滚脚本 R{version}</text>
  <text x="580" y="104" text-anchor="middle" fill="#94a3b8" font-size="11">与正向一一对应，同版本号</text>
  <text x="580" y="126" text-anchor="middle" fill="#64748b" font-size="10">回滚数据影响：文件头注明</text>
  <text x="400" y="185" text-anchor="middle" fill="#475569" font-size="13">铁律：禁止在无回滚脚本的情况下执行迁移</text>
  <rect x="60" y="205" width="160" height="45" rx="8" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_d0)"/>
  <text x="140" y="234" text-anchor="middle" fill="#86efac" font-size="11">① 解析需求</text>
  <rect x="240" y="205" width="160" height="45" rx="8" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_d0)"/>
  <text x="320" y="234" text-anchor="middle" fill="#fde68a" font-size="11">② 生成脚本</text>
  <rect x="420" y="205" width="160" height="45" rx="8" fill="#a855f7" opacity="0.10" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_d0)"/>
  <text x="500" y="234" text-anchor="middle" fill="#d8b4fe" font-size="11">③ 兼容清单</text>
  <rect x="600" y="205" width="160" height="45" rx="8" fill="#3b82f6" opacity="0.10" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_d0)"/>
  <text x="680" y="234" text-anchor="middle" fill="#93c5fd" font-size="11">④ 验证</text>
  <text x="400" y="280" text-anchor="middle" fill="#64748b" font-size="10">双脚本成对存在 · 幂等可重入 · 脚本只生成不自动执行</text>
</svg>
```

## 一、/database-migration-toolkit 解决的是什么问题

### 1.1 迁移是"改了没法回头"的最高危操作

业务代码改错了可以 git revert，配置改错了可以立即回滚。但**数据库迁移改错了，数据可能已经变了**——列删了数据没了、类型改了值被截断、NOT NULL 约束加上了存量数据全是 NULL。迁移一旦执行，数据库就进入了新状态，回不去旧状态，除非你提前准备了回滚脚本。

`/database-migration-toolkit` 就是给项目的数据库结构变更生成全套脚本：正向迁移 SQL、回滚 SQL、数据回填脚本、兼容性检查清单。它的核心铁律只有一条：**禁止在无回滚脚本的情况下执行迁移**。

### 1.2 为什么"脚本只生成，不自动执行"

技能里明确写着：**不擅自执行迁移（脚本只生成，不自动执行）**。这不是保守，是取舍的艺术：

- 生成脚本是"写代码"的范畴——AI 擅长，错了也好改；
- 执行迁移是"动线上数据"的范畴——波及真实数据，必须有人的审批、窗口期、观察环节；
- 拆开两者，AI 负责把脚本写好、把检查清单列全，人类负责挑时机执行——各干各擅长的。

### 1.3 为什么需要三件套：正向 + 回滚 + 回填

一次完整的迁移，几乎总是三类脚本的组合：

| 脚本 | 作用 | 什么时候需要 |
|------|------|-------------|
| `V{version}__{description}.sql` | 正向迁移，把 schema 从旧状态推到新状态 | 每次结构变更 |
| `R{version}__{description}.sql` | 回滚迁移，把 schema 从新状态拉回旧状态 | 每次正向迁移 |
| `backfill/V{version}__backfill_{description}.sql` | 数据回填，补写存量数据 | 新增 NOT NULL 列、数据类型变化、重命名列 |

正向是"前进的地图"，回滚是"后退的安全带"，回填是"地上的坑"——三者缺一不可。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 240" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_d1" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_d1"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="240" fill="url(#bg_d1)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">三件套缺一不可</text>
  <circle cx="210" cy="130" r="70" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_d1)"/>
  <text x="210" y="118" text-anchor="middle" fill="#93c5fd" font-size="13" font-weight="700">正向迁移</text>
  <text x="210" y="142" text-anchor="middle" fill="#64748b" font-size="10">前进的地图</text>
  <text x="210" y="160" text-anchor="middle" fill="#64748b" font-size="9">V{version}__xxx.sql</text>
  <circle cx="400" cy="130" r="70" fill="#ef4444" opacity="0.12" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_d1)"/>
  <text x="400" y="118" text-anchor="middle" fill="#fca5a5" font-size="13" font-weight="700">回滚脚本</text>
  <text x="400" y="142" text-anchor="middle" fill="#64748b" font-size="10">后退的安全带</text>
  <text x="400" y="160" text-anchor="middle" fill="#64748b" font-size="9">R{version}__xxx.sql</text>
  <circle cx="590" cy="130" r="70" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_d1)"/>
  <text x="590" y="118" text-anchor="middle" fill="#fde68a" font-size="13" font-weight="700">数据回填</text>
  <text x="590" y="142" text-anchor="middle" fill="#64748b" font-size="10">地上的坑</text>
  <text x="590" y="160" text-anchor="middle" fill="#64748b" font-size="9">backfill/</text>
  <text x="400" y="225" text-anchor="middle" fill="#475569" font-size="10">缺回滚 = 出事回不了头；缺回填 = 新约束砸垮存量数据</text>
</svg>
```

### 1.4 与 harness-db-design 的关系：设计先行，迁移落地

- `harness-db-design` 负责**表结构设计**——字段、索引、约束、ER 关系，是"图纸"；
- `database-migration-toolkit` 负责**变更落地**——把设计变成 V/R/backfill 脚本，是"施工"。

设计改完成，要用迁移把线上 schema 推进到设计态；迁移里涉及新表/新索引时，也要回来检查设计约束（比如索引是否冗余）。**图纸定了才施工，施工时按图回查**——两者互为上下游。

## 二、四步执行流程

### Step 1: 解析迁移需求

从用户输入或 change.md 中提取四件事：

1. **变更类型**：DDL（新增表/列/索引、修改列、删除列）还是 DML（数据回填/数据修正）；
2. **涉及对象**：表名、列名、数据类型、约束——迁移脚本的"语法主语"，一个都不能含糊；
3. **数据影响**：是否影响已有数据？新增 NOT NULL 列必须带 DEFAULT 值，否则存量行全部非法；
4. **规模判断**：大表操作（>1000 万行）必须走 Online DDL 工具，直接 ALTER 会锁表、吃 IO、拖垮线上。

Step 1 的产出不是一句话，而是一个**结构化的迁移要素表**——类型是什么、动哪张表、改哪些字段、影响多大。这个表是一切后续产物（脚本、清单、验证）的唯一输入源：**解析不清，后面全歪**。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 250" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_d2" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_d2"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="250" fill="url(#bg_d2)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">Step 1：四要素解析</text>
  <rect x="40" y="50" width="340" height="70" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_d2)"/>
  <text x="210" y="72" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">① 变更类型</text>
  <text x="210" y="98" text-anchor="middle" fill="#64748b" font-size="9">DDL or DML？新增/修改/删除？</text>
  <rect x="420" y="50" width="340" height="70" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_d2)"/>
  <text x="590" y="72" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">② 涉及对象</text>
  <text x="590" y="98" text-anchor="middle" fill="#64748b" font-size="9">表/列/数据类型/约束一个不落</text>
  <rect x="40" y="140" width="340" height="70" rx="8" fill="#ef4444" opacity="0.12" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_d2)"/>
  <text x="210" y="162" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">③ 数据影响</text>
  <text x="210" y="188" text-anchor="middle" fill="#64748b" font-size="9">NOT NULL 新列必须带 DEFAULT</text>
  <rect x="420" y="140" width="340" height="70" rx="8" fill="#a855f7" opacity="0.12" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_d2)"/>
  <text x="590" y="162" text-anchor="middle" fill="#d8b4fe" font-size="12" font-weight="700">④ 规模判断</text>
  <text x="590" y="188" text-anchor="middle" fill="#64748b" font-size="9">>1000 万行 → Online DDL 工具</text>
  <text x="400" y="240" text-anchor="middle" fill="#475569" font-size="10">产出 = 结构化迁移要素表，一切后续产物的唯一输入源</text>
</svg>
```

### Step 2: 生成迁移脚本

按"正向 + 回滚 + 回填"三件套生成，版本号从当前最大版本号递增：

```
migrations/
├── V{version}__{description}.sql              — 正向迁移
├── R{version}__{description}.sql              — 回滚迁移
└── backfill/
    └── V{version}__backfill_{description}.sql  — 数据回填（如需）
```

**正向迁移模板约束**——每条都是血泪教训的结晶：

- **新增列**：`ALTER TABLE ADD COLUMN ... AFTER ...`，允许 NULL（向前兼容）——新代码先跑，旧代码还要兼容一段；
- **新增索引**：`ADD INDEX ... ALGORITHM=INPLACE, LOCK=NONE`——不锁表，不阻塞读写；
- **重命名列**：先 ADD 新列 → UPDATE 数据 → DROP 旧列（兼容期）——一步到位的 RENAME 会让运行中的旧代码瞬间崩；
- **删除列**：先标记废弃，观察一个周期再删除（或先 DROP 索引，列保留）——删列不是删线，要等确定无人引用；
- **大表操作**：注释中注明使用 gh-ost / pt-online-schema-change——直接在表上加锁式 ALTER 是生产事故制造机。

**回滚脚本模板约束**：

- 与正向迁移一一对应，同一个版本号——正向升到 V3，回滚就必须能降到 V3 之前；
- 文件头注释明确说明回滚后的数据影响（如"回滚会丢失 payment_method 数据"）——执行者有权事先知道代价；
- 大表回滚同样需要 Online 方式。

**数据回填脚本约束**：

- 分批执行（每批 1000 条，间隔 100ms）——一次性 UPDATE 千万行会拖垮数据库；
- 游标分页（非 OFFSET 分页）——深分页性能灾难，游标按上次的位置继续；
- 幂等：WHERE 条件确保可重入——同一脚本执行两次，结果一致；
- 有进度日志（每批打印当前进度）——大表回填可能跑很久，要有可见性。

### Step 3: 生成兼容性检查清单

根据迁移内容自动生成有动因的检查项：

```
## 兼容性检查清单

### DDL 检查
- [ ] 新增 NOT NULL 列 → 是否有 DEFAULT 值？
- [ ] 列类型变更 → 是否兼容（varchar 扩长安全，缩长会截断）？
- [ ] 重命名列/表 → 是否保留了旧名（兼容期）？
- [ ] 删除列/表 → 是否确认无引用（先废弃再删除）？
- [ ] 大表 ALTER → 是否使用 ONLINE DDL（gh-ost / pt-online）？

### 索引检查
- [ ] 新增索引 → 是否评估了写入性能影响？
- [ ] 新增索引 → 是否与已有索引冗余？
- [ ] 索引字段顺序 → 是否匹配查询（最左前缀）？

### 回滚检查
- [ ] 回滚脚本 → 是否已准备？
- [ ] 回滚后数据 → 是否可恢复？
- [ ] 应用兼容 → 新旧代码是否同时兼容新旧 schema？
```

清单的价值不是"打勾"，而是**强制思考**：

- "NOT NULL 列有 DEFAULT 吗"——逼你确认存量数据的命运；
- "varchar 缩长会截断"——逼你想清楚类型收缩的代价；
- "新旧代码同时兼容吗"——逼你把部署时序想明白：先发布新代码还是先执行迁移？顺序错了会事故。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 250" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_d3" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_d3"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="250" fill="url(#bg_d3)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">兼容清单三类检查</text>
  <rect x="40" y="50" width="220" height="120" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_d3)"/>
  <text x="150" y="76" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">DDL 检查</text>
  <text x="150" y="102" text-anchor="middle" fill="#94a3b8" font-size="9">DEFAULT / 类型兼容</text>
  <text x="150" y="124" text-anchor="middle" fill="#64748b" font-size="8">旧名保留 / 无引用确认</text>
  <text x="150" y="146" text-anchor="middle" fill="#64748b" font-size="8">大表走 Online DDL</text>
  <rect x="290" y="50" width="220" height="120" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_d3)"/>
  <text x="400" y="76" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">索引检查</text>
  <text x="400" y="102" text-anchor="middle" fill="#94a3b8" font-size="9">写入影响 / 冗余索引</text>
  <text x="400" y="124" text-anchor="middle" fill="#64748b" font-size="8">最左前缀匹配</text>
  <text x="400" y="146" text-anchor="middle" fill="#64748b" font-size="8">索引不是免费的</text>
  <rect x="540" y="50" width="220" height="120" rx="8" fill="#ef4444" opacity="0.12" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_d3)"/>
  <text x="650" y="76" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">回滚检查</text>
  <text x="650" y="102" text-anchor="middle" fill="#94a3b8" font-size="9">脚本已备 / 数据可恢复</text>
  <text x="650" y="124" text-anchor="middle" fill="#64748b" font-size="8">新旧代码兼容新旧 schema</text>
  <text x="650" y="146" text-anchor="middle" fill="#64748b" font-size="8">部署时序想清楚</text>
  <text x="400" y="220" text-anchor="middle" fill="#475569" font-size="10">清单的价值是强制思考，不是打勾——逼你把数据命运和部署时序想明白</text>
</svg>
```

### Step 4: 验证

四步铁打验证：

1. **正向迁移 SQL 语法正确**：`mysql -e "source V{version}__xxx.sql"`——语法错了，执行就是事故；
2. **回滚脚本与正向迁移对应**：版本号一致，操作为逆向——正向 ADD 的，回滚必须 DROP；
3. **数据回填 SQL 可重入**：同一脚本执行两次结果一致——重试不会产生脏数据；
4. **检查清单逐项确认**：每一项都过一遍，不跳过。

验证是整个流程的"最后一道闸"：脚本生成得再漂亮，语法错、不可重入，都是废纸。验证通过，才敢进入人工执行环节。

## 三、质量门禁：哪些必须做，哪些绝对不能做

**✅ 必须做**：

- 正向迁移 + 回滚脚本成对存在；
- 正向迁移向前兼容（新增列允许 NULL，不删除已有字段）；
- 数据回填脚本幂等可重入；
- 大表操作使用 Online DDL 工具。

**❌ 绝对不能做**：

- 禁止无回滚脚本的迁移；
- 禁止 NOT NULL 列无 DEFAULT 值；
- 禁止大表直接 ALTER（无 Online DDL 注释）。

门禁的分水岭不是"技术难度"，而是"**能否反悔**"：能反悔的（脚本写错可以改）放行，不能反悔的（数据已被破坏）一律拦截。回滚脚本就是"反悔的钥匙"——没有钥匙，门就不许开。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 250" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_d4" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_d4"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="250" fill="url(#bg_d4)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">门禁的分水岭：能否反悔</text>
  <rect x="40" y="50" width="340" height="110" rx="8" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_d4)"/>
  <text x="210" y="76" text-anchor="middle" fill="#86efac" font-size="13" font-weight="700">能反悔 → 放行 ✅</text>
  <text x="210" y="102" text-anchor="middle" fill="#94a3b8" font-size="10">脚本写错可以改</text>
  <text x="210" y="124" text-anchor="middle" fill="#64748b" font-size="9">双脚本成对 · 向前兼容</text>
  <text x="210" y="146" text-anchor="middle" fill="#64748b" font-size="9">回填幂等 · 大表 Online DDL</text>
  <rect x="420" y="50" width="340" height="110" rx="8" fill="#ef4444" opacity="0.10" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_d4)"/>
  <text x="590" y="76" text-anchor="middle" fill="#fca5a5" font-size="13" font-weight="700">不能反悔 → 拦截 ❌</text>
  <text x="590" y="102" text-anchor="middle" fill="#94a3b8" font-size="10">数据破坏不可逆</text>
  <text x="590" y="124" text-anchor="middle" fill="#64748b" font-size="9">无回滚脚本的迁移</text>
  <text x="590" y="146" text-anchor="middle" fill="#64748b" font-size="9">NOT NULL 无 DEFAULT / 大表裸 ALTER</text>
  <text x="400" y="220" text-anchor="middle" fill="#475569" font-size="10">回滚脚本是"反悔的钥匙"——没有钥匙，门就不许开</text>
</svg>
```

## 四、四条约束：边界在哪里

- **不生成数据库无关的 SQL**——只为目标数据库类型（MySQL / PostgreSQL）生成，方言各不同；
- **不修改已有迁移脚本**——只新增版本号递增的迁移，历史脚本永不改动（改了历史 = 抹掉事实）；
- **不擅自执行迁移**——脚本只生成，不自动执行，执行是人的事；
- **不生成回填数据的具体业务逻辑**——只生成回填框架，业务逻辑由用户填写——AI 猜不出你的回填规则。

边界一句话：**本技能写"脚本"，不碰"数据决策"和"执行开关"**。

## 五、与相邻技能的分工

| 场景 | 归属 |
|------|------|
| 表结构设计 / ER / 约束 | `harness-db-design` |
| 迁移脚本生成 / 回滚 | **本技能**（`/database-migration-toolkit`） |
| 部署时执行迁移 | `k8s-release-toolkit`（发布流程的一部分） |

- **harness-db-design → 本技能**：设计改完，本技能把设计落地成迁移；
- **本技能 → k8s-release-toolkit**：迁移脚本在发布流程中被编排执行，本技能负责"生成"，发布工具负责"执行时机"。

## 六、完成标志

`/database-migration-toolkit` 的完成标志有三个：

1. **正向迁移 + 回滚脚本成对生成**，版本号一致、操作互逆——没有只进不出的迁移；
2. **数据回填脚本幂等可重入**（如需回填），有分批逻辑与进度日志——重跑不脏、跑起来看得见；
3. **兼容性检查清单生成并逐项确认**，验证四步全过——不是"写完了"，是"验证过了"。

三个标志对应三问：**有成对吗**（正向/回滚）？**可重入吗**（回填）？**验证过了吗**（清单+四步）？——三个都答"是"，迁移才允许进入人工执行环节。

## 七、写在最后

`/database-migration-toolkit` 的全部设计，浓缩成四句话：

1. **迁移是"改了没法回头"的最高危操作**——所以正向、回滚、回填三件套一个都不能少。
2. **禁止无回滚脚本的迁移**——回滚脚本是反悔的钥匙，没有钥匙不许开门。
3. **脚本只生成，不自动执行**——AI 写好地图和清单，人类挑时机开车。
4. **向前兼容是信仰**——新增列允许 NULL、重命名留兼容期、大表走 Online DDL。

一句话记住它：**/database-migration-toolkit 是数据库变更的"施工队"——它把结构设计落地成正向、回滚、回填三件套脚本和兼容性检查清单，用"没有回滚就不能执行"的铁律，让每一次 schema 变更都有进有退、可反悔、不塌方。**