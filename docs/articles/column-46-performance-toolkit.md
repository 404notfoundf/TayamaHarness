# /performance-toolkit：性能诊断五连——诊断脚本、火焰图、GC、慢 SQL、线程 dump，先采集后建议

> 命令深度拆解 · 第 46 篇 · 约 8000 字 · 6 个 SVG 图

---

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 300" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_p0" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_p0"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="300" fill="url(#bg_p0)" rx="10"/>
  <text x="400" y="28" text-anchor="middle" fill="#e2e8f0" font-size="26" font-weight="700">/performance-toolkit：性能诊断五连</text>
  <rect x="30" y="55" width="140" height="95" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_p0)"/>
  <text x="100" y="80" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">diagnose</text>
  <text x="100" y="104" text-anchor="middle" fill="#64748b" font-size="9">一键诊断</text>
  <text x="100" y="126" text-anchor="middle" fill="#64748b" font-size="8">按语言自动选工具</text>
  <rect x="187" y="55" width="140" height="95" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_p0)"/>
  <text x="257" y="80" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">flame</text>
  <text x="257" y="104" text-anchor="middle" fill="#64748b" font-size="9">火焰图</text>
  <text x="257" y="126" text-anchor="middle" fill="#64748b" font-size="8">CPU/alloc/lock</text>
  <rect x="344" y="55" width="140" height="95" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_p0)"/>
  <text x="414" y="80" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">gc</text>
  <text x="414" y="104" text-anchor="middle" fill="#64748b" font-size="9">GC 分析</text>
  <text x="414" y="126" text-anchor="middle" fill="#64748b" font-size="8">频率/STW/堆使用率</text>
  <rect x="501" y="55" width="140" height="95" rx="8" fill="#a855f7" opacity="0.12" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_p0)"/>
  <text x="571" y="80" text-anchor="middle" fill="#d8b4fe" font-size="12" font-weight="700">slow-sql</text>
  <text x="571" y="104" text-anchor="middle" fill="#64748b" font-size="9">慢 SQL</text>
  <text x="571" y="126" text-anchor="middle" fill="#64748b" font-size="8">EXPLAIN + 索引建议</text>
  <rect x="658" y="55" width="112" height="95" rx="8" fill="#ef4444" opacity="0.12" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_p0)"/>
  <text x="714" y="80" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">thread</text>
  <text x="714" y="104" text-anchor="middle" fill="#64748b" font-size="9">线程 dump</text>
  <text x="714" y="126" text-anchor="middle" fill="#64748b" font-size="8">死锁/锁竞争</text>
  <text x="400" y="185" text-anchor="middle" fill="#475569" font-size="13">铁律：禁止在无数据支撑的情况下给出性能优化建议</text>
  <rect x="60" y="205" width="160" height="45" rx="8" fill="#3b82f6" opacity="0.10" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_p0)"/>
  <text x="140" y="234" text-anchor="middle" fill="#93c5fd" font-size="11">① 确定诊断目标</text>
  <rect x="240" y="205" width="160" height="45" rx="8" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_p0)"/>
  <text x="320" y="234" text-anchor="middle" fill="#86efac" font-size="11">② 生成诊断脚本</text>
  <rect x="420" y="205" width="160" height="45" rx="8" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_p0)"/>
  <text x="500" y="234" text-anchor="middle" fill="#fde68a" font-size="11">③ 验证</text>
  <rect x="600" y="205" width="160" height="45" rx="8" fill="#a855f7" opacity="0.10" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_p0)"/>
  <text x="680" y="234" text-anchor="middle" fill="#d8b4fe" font-size="11">④ 采集数据</text>
  <text x="400" y="286" text-anchor="middle" fill="#64748b" font-size="10">脚本只诊断不自动执行 · 生产配置不碰 · 框架调优参数由架构师定</text>
</svg>
```

## 一、/performance-toolkit 解决的是什么问题

### 1.1 裸靠"经验"调优的四类事故

性能问题的排查，最容易犯的错不是"诊断不出来"，而是**没有数据就下结论**：

1. **凭感觉优化**："接口慢肯定是 SQL 的问题"——实际一看火焰图，热点全在序列化；
2. **反复重启试错**："重启一下看看还慢不慢"——现场没了，证据没了，下次还是猜；
3. **工具不成体系**：Java 用 jstack，Python 用 py-spy，Go 用 pprof——每个都要现场查命令、查参数，查完还要手工拼分析；
4. **排查没留档**：修完就忘，同类问题下次从头再来——没有诊断报告，就没有知识沉淀。

`/performance-toolkit` 为项目生成性能诊断工具集：一键诊断脚本、火焰图采集脚本、GC 分析工具、慢 SQL 分析模板、线程 dump 分析模板。**禁止在无数据支撑的情况下给出性能优化建议**。

### 1.2 五个子命令，覆盖症状到工具

| 子命令 | 工具 | 对应症状 |
|--------|------|---------|
| `diagnose` | 一键诊断脚本 | 通用性能问题 |
| `flame` | 火焰图采集脚本 | CPU 高、分配热点、锁竞争 |
| `gc` | GC 分析工具 | 内存泄漏、GC 频繁、STW 时间长 |
| `slow-sql` | 慢 SQL 分析模板 | 接口响应慢、DB 负载高 |
| `thread` | 线程 dump 分析模板 | 线程 hang、死锁、连接池耗尽 |
| `all`（默认） | 全部工具 | 首次引入性能诊断能力 |

五个子命令是一套**症状→工具→脚本→分析**的完整映射：先看症状是什么，再选对应工具，工具生成对应脚本，脚本跑完输出分析结论——全程不用现场查文档、不用手工拼命令。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 260" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_p1" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_p1"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="260" fill="url(#bg_p1)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">症状 → 工具 → 脚本 → 分析</text>
  <rect x="40" y="50" width="340" height="70" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_p1)"/>
  <text x="210" y="72" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">CPU 高 / 分配热点 / 锁竞争</text>
  <text x="210" y="98" text-anchor="middle" fill="#94a3b8" font-size="9">flame · async-profiler / py-spy / pprof</text>
  <rect x="420" y="50" width="340" height="70" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_p1)"/>
  <text x="590" y="72" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">内存泄漏 / GC 频繁 / STW 长</text>
  <text x="590" y="98" text-anchor="middle" fill="#94a3b8" font-size="9">gc · GC 日志 + 关键指标对比健康阈值</text>
  <rect x="40" y="140" width="340" height="70" rx="8" fill="#a855f7" opacity="0.12" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_p1)"/>
  <text x="210" y="162" text-anchor="middle" fill="#d8b4fe" font-size="12" font-weight="700">接口慢 / DB 负载高</text>
  <text x="210" y="188" text-anchor="middle" fill="#94a3b8" font-size="9">slow-sql · EXPLAIN ANALYZE + 索引建议</text>
  <rect x="420" y="140" width="340" height="70" rx="8" fill="#ef4444" opacity="0.12" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_p1)"/>
  <text x="590" y="162" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">线程 hang / 死锁 / 池耗尽</text>
  <text x="590" y="188" text-anchor="middle" fill="#94a3b8" font-size="9">thread · 3 次 dump 间隔 5s + 状态分布</text>
  <text x="400" y="242" text-anchor="middle" fill="#475569" font-size="10">先有数据，再有结论——没有数据支撑的建议都是猜测</text>
</svg>
```

### 1.3 为什么"无数据不下结论"是铁律

性能问题有三个显著特点：**偶发、瞬时、多因**。

- **偶发**：白天好好的，晚上高峰才出现——凭感觉优化正好错过现场；
- **瞬时**：STW 一秒就过去了，等想起来抓现场早就没了——必须脚本化采集；
- **多因**：慢可能是 SQL、可能是 GC、可能是锁、可能是网络——没有火焰图和 GC 日志，根本定位不到真凶。

所以 `diagnose.sh` 的设计是"**一键采集全部数据**"：检测目标语言自动选工具，采集所有数据到 `{timestamp}/` 目录，输出采集摘要——现场先保下来，分析慢慢做。

## 二、触发方式与前置

**触发方式**两种：

- 独立命令 `/performance-toolkit [diagnose|flame|gc|slow-sql|thread|all]`；
- 自动加载：diagnosing-bugs 阶段检测到性能相关异常（超时/OOM/CPU 高）时。

**前置条件**：

1. 已确定目标进程 PID 或服务名——脚本要对准进程采集；
2. 已确定应用的运行时（JVM / Python / Go / Node.js）——选对采集工具；
3. 已确定诊断目标（CPU 高 / 内存泄漏 / 高延迟 / 低吞吐）——选对子命令；
4. 用户未指定 → 生成全部组件（`all`）。

前置的用意：**诊断工具是"对着进程开火"的**——不知道 PID、不知道运行时、不知道目标，脚本连采集谁都不知道。

## 三、四步执行流程

### Step 1: 确定诊断目标

按症状选子命令：

| 子命令 | 工具 | 对应症状 |
|--------|------|---------|
| `diagnose` | 一键诊断脚本 | 通用性能问题 |
| `flame` | 火焰图采集脚本 | CPU 高、分配热点、锁竞争 |
| `gc` | GC 分析工具 | 内存泄漏、GC 频繁、STW 时间长 |
| `slow-sql` | 慢 SQL 分析模板 | 接口响应慢、DB 负载高 |
| `thread` | 线程 dump 分析模板 | 线程 hang、死锁、连接池耗尽 |
| `all`（默认） | 全部工具 | 首次引入性能诊断能力 |

Step 1 是"症状对准工具"：接口慢别急着看火焰图，先 slow-sql；内存涨别瞎猜泄漏，先 gc。**症状不对准，工具全是白跑**。

### Step 2: 生成诊断脚本

生成 `scripts/` 目录：

```
scripts/
├── diagnose.sh                  — 一键诊断入口（采集所有数据）
├── flame-graph/
│   ├── collect-flame-java.sh    — Java async-profiler 采集
│   ├── collect-flame-python.sh  — Python py-spy 采集
│   ├── collect-flame-go.sh      — Go pprof 采集
│   └── analyze-flame.sh         — 火焰图分析指南
├── gc/
│   ├── collect-gc-logs.sh       — GC 日志采集
│   ├── analyze-gc.sh            — GC 分析脚本（提取关键指标）
│   └── gc-params.sh             — GC 参数模板（按堆大小生成推荐配置）
├── slow-sql/
│   ├── config-slow-query.sh     — 慢查询日志配置
│   ├── analyze-slow-sql.sh      — 慢 SQL 分析（提取执行计划）
│   └── index-finder.sh          — 索引建议生成
├── thread/
│   ├── dump-thread.sh           — 线程 dump 采集（3 次，间隔 5s）
│   └── analyze-thread.sh        — 线程 dump 分析（死锁检测/锁竞争/线程状态分布）
└── report/
    └── template.md              — 诊断报告模板
```

**每个脚本的约束**——本技能的灵魂就在这：

- **`diagnose.sh`**：检测目标语言自动选择对应工具，采集所有数据到 `{timestamp}/` 目录，输出采集摘要——**按语言自动选工具，现场先保下来**；
- **`collect-flame-*.sh`**：支持 CPU/alloc/lock 三种事件，采样时长可配置（默认 60s），输出 SVG 文件——**火焰图三事件，热点看得见**；
- **`analyze-gc.sh`**：提取 Young GC 频率、Full GC 频率、STW 时间、堆使用率、分配速率，与健康阈值对比——**指标要对标，才知健康与否**；
- **`analyze-slow-sql.sh`**：对每条慢 SQL 执行 EXPLAIN ANALYZE，标记 type=ALL（全表扫描）、Extra=Using filesort/Using temporary——**执行计划说话，索引建议不猜**；
- **`analyze-thread.sh`**：检测死锁、统计线程状态分布（RUNNABLE/BLOCKED/WAITING/TIMED_WAITING）、标记锁竞争热点——**死锁一把抓，池耗尽看得穿**。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 260" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_p2" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_p2"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="260" fill="url(#bg_p2)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">脚本五大关键设计</text>
  <rect x="40" y="50" width="340" height="70" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_p2)"/>
  <text x="210" y="72" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">自动选语言工具</text>
  <text x="210" y="98" text-anchor="middle" fill="#64748b" font-size="9">JVM→async-profiler · Python→py-spy · Go→pprof</text>
  <rect x="420" y="50" width="340" height="70" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_p2)"/>
  <text x="590" y="72" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">三事件火焰图</text>
  <text x="590" y="98" text-anchor="middle" fill="#64748b" font-size="9">CPU / alloc / lock · 采样时长可配</text>
  <rect x="40" y="140" width="340" height="70" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_p2)"/>
  <text x="210" y="162" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">GC 指标对比健康阈值</text>
  <text x="210" y="188" text-anchor="middle" fill="#64748b" font-size="9">Young/Full 频率 · STW · 堆使用率 · 分配速率</text>
  <rect x="420" y="140" width="340" height="70" rx="8" fill="#a855f7" opacity="0.12" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_p2)"/>
  <text x="590" y="162" text-anchor="middle" fill="#d8b4fe" font-size="12" font-weight="700">慢 SQL 关联执行计划</text>
  <text x="590" y="188" text-anchor="middle" fill="#64748b" font-size="9">type=ALL · Using filesort/temporary · 索引建议</text>
  <text x="400" y="258" text-anchor="middle" fill="#475569" font-size="10">脚本生成≠问题解决——采集完数据，分析才有依据</text>
</svg>
```

### Step 3: 验证

1. 脚本语法正确：`bash -n scripts/diagnose.sh`；
2. 检查清单逐项确认：
   - [ ] 诊断脚本覆盖目标环境的运行时（JVM/Python/Go/Node.js）
   - [ ] 火焰图脚本支持 CPU/alloc/lock 三种事件
   - [ ] GC 分析脚本输出了关键指标（频率/停顿/吞吐量/分配速率）
   - [ ] 慢 SQL 分析脚本关联了执行计划
   - [ ] 线程 dump 分析脚本包含死锁检测

五条清单对应五道红线复查：**运行时全覆盖、三事件火焰图、GC 有指标、SQL 有计划、dump 查死锁**——五项全过，诊断工具集才算合格。

### Step 4: 采集数据

脚本生成完不是终点，**跑起来采集数据才是**：

- 用 `diagnose.sh` 一键采集全部数据到 `{timestamp}/` 目录；
- 用 `analyze-*.sh` 对采集的数据做分析，输出结论；
- 用 `report/template.md` 把结论落成诊断报告留档。

Step 4 是最容易被跳过的——但**没有数据，前面的脚本全是摆设**。诊断报告模板存在的意义，就是让每次排查都有据可查、有档可续。

## 四、质量门禁：哪些必须做，哪些绝对不能做

**✅ 必须做**：

- 所有脚本通过 `bash -n` 语法校验；
- 诊断脚本覆盖目标语言（JVM/Python/Go/Node.js）；
- 火焰图支持 CPU/alloc/lock 三种事件；
- GC 分析输出关键指标并对比健康阈值；
- 慢 SQL 分析关联执行计划；
- 线程 dump 分析包含死锁检测。

**❌ 绝对不能做**：

- 禁止无数据采集直接给出优化建议（先诊断后结论）；
- 禁止在诊断脚本中写入硬编码路径（使用参数传递）。

门禁的逻辑是"**诊断的贞操观**"：

- **先数据后结论**——没有采集就没有发言权，猜测式的优化建议都是耍流氓；
- **参数化不硬编**——脚本要在任意环境跑，路径写死就废了。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 240" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_p3" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_p3"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="240" fill="url(#bg_p3)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">性能诊断三条边界</text>
  <rect x="40" y="50" width="340" height="70" rx="8" fill="#ef4444" opacity="0.12" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_p3)"/>
  <text x="210" y="72" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">不碰生产配置</text>
  <text x="210" y="98" text-anchor="middle" fill="#64748b" font-size="9">只生成诊断脚本，不自动执行</text>
  <rect x="420" y="50" width="340" height="70" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_p3)"/>
  <text x="590" y="72" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">不定框架调优参数</text>
  <text x="590" y="98" text-anchor="middle" fill="#64748b" font-size="9">线程池/连接数由架构师决定</text>
  <rect x="40" y="140" width="340" height="70" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_p3)"/>
  <text x="210" y="162" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">不擅自装系统工具</text>
  <text x="210" y="188" text-anchor="middle" fill="#64748b" font-size="9">async-profiler 由用户自行安装</text>
  <rect x="420" y="140" width="340" height="70" rx="8" fill="#a855f7" opacity="0.12" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_p3)"/>
  <text x="590" y="162" text-anchor="middle" fill="#d8b4fe" font-size="12" font-weight="700">不做压测基准</text>
  <text x="590" y="188" text-anchor="middle" fill="#64748b" font-size="9">基准测试由压测团队负责</text>
  <text x="400" y="232" text-anchor="middle" fill="#475569" font-size="10">诊断是发现问题的，不是替你调整和做决定的</text>
</svg>
```

## 五、四条约束：边界在哪里

- **不修改生产环境配置**——只生成诊断脚本，不自动执行（诊断是采集，不是改动）；
- **不生成特定框架的调优参数**（如 Spring 线程池配置、Tomcat 连接数等，这些由架构师决定）；
- **不擅自安装系统级工具**（如 `async-profiler` 需用户自行安装，脚本只提供链接和安装说明）；
- **不生成性能基准测试代码**——只生成诊断工具，基准测试由压测团队负责。

边界一句话：**本技能管"问题怎么发现"，不碰"配置怎么改、参数怎么定、工具怎么装、压测怎么做"**。

## 六、与相邻技能的分工

| 场景 | 归属 |
|------|------|
| 超时/OOM/CPU 高的诊断采集与脚本 | **本技能**（`/performance-toolkit`） |
| 性能问题的根因推导与修复链路 | `diagnosing-bugs`（byRootCause） |
| 慢 SQL 的索引落地 | `harness-db-design`（索引/约束设计） |

- **本技能 vs diagnosing-bugs**：performance-toolkit 把"证据"采下来（火焰图、GC 日志、慢 SQL、线程 dump）；diagnosing-bugs 把"结论"推出来（根因分析、修复验证）——一个造证据，一个做侦探；
- **本技能 vs harness-db-design**：slow-sql 分析发现全表扫描；`harness-db-design` 负责把索引设计好、落地对——一个发现问题，一个解决问题。

## 七、完成标志

`/performance-toolkit` 的完成标志有三个：

1. **目标工具集已生成**：diagnose/flame/gc/slow-sql/thread 按需产出——该有的都有；
2. **脚本验证通过 + 检查清单全过**：bash -n 校验 + 五条红线逐项确认——不是"写了"，是"验证了"；
3. **无数据不下结论的纪律已生效**：优化建议均有采集数据支撑，且有诊断报告留档——不是"猜了"，是"测了"。

三个标志对应三问：**齐了吗**（工具集）？**验了吗**（脚本+清单）？**测了吗**（数据支撑）？——三个都答"是"，性能诊断能力才算真正落地。

## 八、写在最后

`/performance-toolkit` 的全部设计，浓缩成四句话：

1. **先采集后建议**——没有数据支撑的优化建议都是猜测，现场先保下来。
2. **症状对准工具**——CPU 高看火焰图，内存涨看 GC，接口慢看慢 SQL，线程卡看 dump。
3. **脚本参数化**——诊断脚本不硬编码路径，任何环境都能跑。
4. **报告留档**——每次排查落成诊断报告，同类问题不重来。

一句话记住它：**/performance-toolkit 是性能诊断的"五件套取证仪"——一键诊断脚本、三事件火焰图、GC 指标分析、慢 SQL 执行计划、线程死锁检测，用"不采集不给建议、不对症不乱跑、不硬编不越界、不留档不算完"四条纪律，让每个性能问题都有据可查、有结论可依。**