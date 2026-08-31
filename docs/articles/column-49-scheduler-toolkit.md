# /scheduler-toolkit：分布式调度五件套——锁、幂等、重试、执行记录、控制台，定时任务不再裸奔

> 命令深度拆解 · 第 49 篇 · 约 8000 字 · 6 个 SVG 图

---

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 300" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_s0" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_s0"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="300" fill="url(#bg_s0)" rx="10"/>
  <text x="400" y="28" text-anchor="middle" fill="#e2e8f0" font-size="26" font-weight="700">/scheduler-toolkit：分布式调度五件套</text>
  <rect x="30" y="55" width="140" height="95" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_s0)"/>
  <text x="100" y="80" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">task</text>
  <text x="100" y="104" text-anchor="middle" fill="#64748b" font-size="9">任务基类模板方法</text>
  <text x="100" y="126" text-anchor="middle" fill="#64748b" font-size="8">锁→幂等→执行→记录</text>
  <rect x="187" y="55" width="140" height="95" rx="8" fill="#ef4444" opacity="0.12" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_s0)"/>
  <text x="257" y="80" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">lock</text>
  <text x="257" y="104" text-anchor="middle" fill="#64748b" font-size="9">分布式锁调度</text>
  <text x="257" y="126" text-anchor="middle" fill="#64748b" font-size="8">SETNX + TTL/行锁</text>
  <rect x="344" y="55" width="140" height="95" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_s0)"/>
  <text x="414" y="80" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">retry</text>
  <text x="414" y="104" text-anchor="middle" fill="#64748b" font-size="9">失败重试 + 补偿</text>
  <text x="414" y="126" text-anchor="middle" fill="#64748b" font-size="8">3 次 · 30s · 5 分钟扫描</text>
  <rect x="501" y="55" width="140" height="95" rx="8" fill="#a855f7" opacity="0.12" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_s0)"/>
  <text x="571" y="80" text-anchor="middle" fill="#d8b4fe" font-size="12" font-weight="700">console</text>
  <text x="571" y="104" text-anchor="middle" fill="#64748b" font-size="9">手动触发/暂停/恢复</text>
  <text x="571" y="126" text-anchor="middle" fill="#64748b" font-size="8">POST API 指挥</text>
  <rect x="658" y="55" width="112" height="95" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_s0)"/>
  <text x="714" y="80" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">记录</text>
  <text x="714" y="104" text-anchor="middle" fill="#64748b" font-size="9">持久化落库</text>
  <text x="714" y="126" text-anchor="middle" fill="#64748b" font-size="8">可排查可审计</text>
  <text x="400" y="185" text-anchor="middle" fill="#475569" font-size="13">铁律：禁止在无分布式锁保护的情况下执行定时任务</text>
  <rect x="60" y="205" width="160" height="45" rx="8" fill="#3b82f6" opacity="0.10" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_s0)"/>
  <text x="140" y="234" text-anchor="middle" fill="#93c5fd" font-size="11">① 确定生成目标</text>
  <rect x="240" y="205" width="160" height="45" rx="8" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_s0)"/>
  <text x="320" y="234" text-anchor="middle" fill="#86efac" font-size="11">② 生成代码</text>
  <rect x="420" y="205" width="160" height="45" rx="8" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_s0)"/>
  <text x="500" y="234" text-anchor="middle" fill="#fde68a" font-size="11">③ 测试钉死</text>
  <rect x="600" y="205" width="160" height="45" rx="8" fill="#a855f7" opacity="0.10" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_s0)"/>
  <text x="680" y="234" text-anchor="middle" fill="#d8b4fe" font-size="11">④ 验证</text>
  <text x="400" y="286" text-anchor="middle" fill="#64748b" font-size="10">管理台归调度中心 · 前端 WEB 界面归前端 · 版本不擅引</text>
</svg>
```

## 一、/scheduler-toolkit 解决的是什么问题

### 1.1 裸用 @Scheduled 的事故清单

定时任务看似最简单——一个注解搞定。但分布式环境下裸用，几乎必然踩坑：

1. **重复执行**：服务部署多副本，`@Scheduled` 在每个副本上各跑一遍——报表任务、对账任务、清理任务全被重复执行；
2. **任务没锁**：无分布式锁保护，两个节点同时消费同一批数据——订单重复推送、库存重复扣减；
3. **失败没人管**：任务跑到一半抛异常，什么都没留下——下次照跑，错误永远在；
4. **失败没补偿**：任务失败就失败，没有重试没有补偿——该处理的数据永远漏着，只能靠人工手动重跑；
5. **执行像黑盒**：没有执行记录，不知道任务跑了多久、是成功还是失败、上次跑是什么时候——排查问题全靠猜。

`/scheduler-toolkit` 为项目生成分布式任务调度基础设施代码：分布式锁调度（防止重复执行）、任务幂等（去重表）、失败重试（最大次数+补偿）、执行记录（开始/结束/耗时/结果）、手动触发/暂停/恢复 API。**禁止在无分布式锁保护的情况下执行定时任务**。

### 1.2 五个子命令，一套调度体系

| 子命令 | 组件 | 检测条件 |
|--------|------|---------|
| `task` | 任务基类 | 存在 `@Scheduled` 或 cron 表达式 |
| `lock` | 分布式锁调度 | 定时任务无锁保护 |
| `retry` | 失败重试+补偿 | 任务无失败处理 |
| `console` | 调度控制台 API | 无法手动触发/暂停任务 |
| `all`（默认） | 全部组件 | 首次引入调度框架 |

五个子命令覆盖调度全生命周期：**task 定骨架，lock 防重复，retry 保不丢，console 可指挥，记录可排查**。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 260" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_s1" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_s1"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="260" fill="url(#bg_s1)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">调度任务五问五环</text>
  <rect x="40" y="50" width="340" height="70" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_s1)"/>
  <text x="210" y="72" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">会重复吗：锁</text>
  <text x="210" y="98" text-anchor="middle" fill="#94a3b8" font-size="9">SETNX 抢锁 · 抢不到就跳过并记录被跳过</text>
  <rect x="420" y="50" width="340" height="70" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_s1)"/>
  <text x="590" y="72" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">会重放吗：幂等</text>
  <text x="590" y="98" text-anchor="middle" fill="#94a3b8" font-size="9">执行前查去重表 · 已执行则跳过</text>
  <rect x="40" y="140" width="340" height="70" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_s1)"/>
  <text x="210" y="162" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">会丢吗：重试+补偿</text>
  <text x="210" y="188" text-anchor="middle" fill="#94a3b8" font-size="9">最大 3 次 · 30s 间隔 · 补偿任务每 5 分钟扫</text>
  <rect x="420" y="140" width="340" height="70" rx="8" fill="#a855f7" opacity="0.12" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_s1)"/>
  <text x="590" y="162" text-anchor="middle" fill="#d8b4fe" font-size="12" font-weight="700">查得到吗：记录</text>
  <text x="590" y="188" text-anchor="middle" fill="#94a3b8" font-size="9">task_execution_record 落库 · 结果/耗时/异常</text>
  <text x="400" y="242" text-anchor="middle" fill="#475569" font-size="10">定时任务不是"到点跑一下"，而是"每次跑都有据可依"</text>
</svg>
```

### 1.3 为什么"任务必须走 AbstractTask"

裸 `@Scheduled` 只有一个动作：到点执行。而一个可靠的调度任务需要五件事：**抢锁、查幂等、记开始、执行、记结果**。

- 抢锁防重复执行；
- 查幂等防重放；
- 记开始/结束/耗时/结果——出问题有据可查；
- 异常处理——失败进入重试和补偿通道。

这五件事不该在每个业务任务里各写一遍——`AbstractTask` 用模板方法统一封装：子类只实现 `execute(TaskContext ctx)`，锁、幂等、记录、异常处理全是基类的事。

## 二、触发方式与前置

**触发方式**两种：

- 独立命令 `/scheduler-toolkit [task|lock|retry|console|all]`；
- 自动加载：coding-skill 阶段检测到 `@Scheduled` / `@SchedulerLock` / `cron` / `CronJob` / `robfig/cron` 时。

**前置条件**：

1. 已识别技术栈+调度框架（Spring @Scheduled / Quartz / XXL-Job / Python APScheduler / Go robfig/cron）；
2. 已确定目标包路径（按语言规范）；
3. 已确定任务列表（至少一个任务名称+执行逻辑）；
4. 已确定分布式锁实现（Redis / 数据库 / Zookeeper）；
5. 用户未指定 → 生成全部组件（`all`）。

前置的用意：**调度封装生成的是"按你的调度框架和锁实现的代码"**——不知道用 @Scheduled 还是 XXL-Job、不知道锁用 Redis 还是数据库，生成的基类连框架 API 都调不对。

## 三、四步执行流程

### Step 1: 确定生成目标

按子命令或检测条件确定生成范围：

| 子命令 | 组件 | 检测条件 |
|--------|------|---------|
| `task` | 任务基类 | 存在 `@Scheduled` 或 cron 表达式 |
| `lock` | 分布式锁调度 | 定时任务无锁保护 |
| `retry` | 失败重试+补偿 | 任务无失败处理 |
| `console` | 调度控制台 API | 无法手动触发/暂停任务 |
| `all`（默认） | 全部组件 | 首次引入调度框架 |

Step 1 是"按需裁剪"：只有任务没锁？生成 lock。任务失败没人管？生成 retry。无法手动控制？生成 console。首次引入？生成 all。**缺什么补什么，检测条件同时是体检**——裸 @Scheduled、无锁保护、无失败处理，都会被照出来。

### Step 2: 生成代码文件

以 Java 示例，生成 `{basePackage}/scheduler/` 包：

```
{basePackage}/scheduler/
├── core/
│   ├── AbstractTask.java              — 任务基类（模板方法：锁→执行→记录→异常处理）
│   ├── TaskContext.java               — 任务上下文（任务ID/参数/开始时间/重试次数）
│   └── TaskResult.java                — 执行结果（成功/失败/跳过/耗时）
├── lock/
│   ├── DistributedTaskLock.java       — 任务分布式锁接口
│   ├── RedisTaskLock.java             — Redis 任务锁实现（SETNX + TTL）
│   └── DatabaseTaskLock.java          — 数据库任务锁实现（行锁）
├── store/
│   ├── TaskExecutionRecord.java       — 执行记录实体（任务名/开始/结束/耗时/结果/异常信息）
│   ├── TaskExecutionRepository.java   — 执行记录存储
│   └── TaskIdempotentChecker.java     — 任务幂等检查（去重表）
├── retry/
│   ├── TaskRetryStrategy.java         — 重试策略（最大次数/间隔/补偿窗口）
│   └── CompensationTask.java          — 补偿任务（扫描失败记录重新执行）
├── console/
│   ├── TaskController.java            — 调度控制台 API
│   └── TaskManageService.java         — 任务管理服务
└── config/
    └── SchedulerConfig.java           — 调度配置（线程池大小/锁超时/重试次数）
```

**每个组件的关键实现约束**——本技能的灵魂就在这：

- **`AbstractTask`**：模板方法——获取锁（失败则跳过/记录"被跳过"）→ 执行前检查幂等（已执行则跳过）→ 记录开始时间 → 执行业务逻辑 → 记录结束时间/耗时/结果 → 释放锁，子类只实现 `execute(TaskContext ctx)` 方法——**骨架统一，业务只填 execute**；
- **`RedisTaskLock`**：SETNX key `task:{taskName}` value `{instanceId}` PX 30000，锁超时自动释放，防止任务挂掉锁不释放——**锁有超时，任务死了锁不死**；
- **执行记录**：持久化到数据库（`task_execution_record` 表），字段：task_name / start_time / end_time / duration_ms / result / error_message / instance_id——**每次运行都有痕可查**；
- **重试策略**：最大重试次数 3，间隔 30s，补偿任务每 5 分钟扫描失败记录重新执行，补偿次数上限 5——**失败不沉底，补偿有上限**；
- **手动触发 API**：`POST /api/scheduler/{taskName}/trigger` 忽略锁和 cron 表达式立即执行，`POST /api/scheduler/{taskName}/pause` 暂停（不执行但标记为暂停），`POST /api/scheduler/{taskName}/resume` 恢复——**人能指挥，机器照样跑**。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 260" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_s2" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_s2"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="260" fill="url(#bg_s2)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">AbstractTask 模板方法五步</text>
  <rect x="40" y="50" width="340" height="70" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_s2)"/>
  <text x="210" y="72" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">① 抢锁</text>
  <text x="210" y="98" text-anchor="middle" fill="#94a3b8" font-size="9">失败则跳过并记录被跳过</text>
  <rect x="420" y="50" width="340" height="70" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_s2)"/>
  <text x="590" y="72" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">② 查幂等</text>
  <text x="590" y="98" text-anchor="middle" fill="#94a3b8" font-size="9">已执行则跳过</text>
  <rect x="40" y="140" width="340" height="70" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_s2)"/>
  <text x="210" y="162" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">③ 记开始 + execute()</text>
  <text x="210" y="188" text-anchor="middle" fill="#94a3b8" font-size="9">子类只实现 execute(TaskContext)</text>
  <rect x="420" y="140" width="340" height="70" rx="8" fill="#a855f7" opacity="0.12" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_s2)"/>
  <text x="590" y="162" text-anchor="middle" fill="#d8b4fe" font-size="12" font-weight="700">④ 记结果 + 释放锁</text>
  <text x="590" y="188" text-anchor="middle" fill="#94a3b8" font-size="9">结束/耗时/结果/异常落库</text>
  <text x="400" y="252" text-anchor="middle" fill="#475569" font-size="10">骨架统一五件事，业务只填一件事</text>
</svg>
```

### Step 3: 生成测试

每个核心组件配测试，测试要求紧扣实现约束（使用 Mock Redis/嵌入式数据库）：

- **任务基类**：验证模板方法流程（锁检查→幂等检查→执行→记录→释放锁）——**每一步都按顺序走，必须有测试证明**；
- **分布式锁**：验证加锁/释放/超时自动释放/并发加锁失败跳过——**并发抢锁只有一个赢家、任务挂了锁会超时释放，必须有测试证明**；
- **执行记录**：验证记录完整性、耗时统计——**每次运行都有记录，必须有测试证明**；
- **补偿任务**：验证扫描失败记录、重新执行、补偿次数上限——**失败不沉底、补偿有上限，必须有测试证明**。

测试的作用是把五道纪律**钉死**：模板方法、分布式锁、超时释放、记录落库、补偿兜底——纪律靠测试保证，不靠 review 保证。

### Step 4: 验证

1. 编译通过 + 测试通过；
2. 检查清单逐项确认：
   - [ ] 所有定时任务经过 `AbstractTask` 封装（非裸 `@Scheduled`）
   - [ ] 分布式锁已实现（非仅依赖单机锁）
   - [ ] 锁超时自动释放（防任务挂掉锁不释放）
   - [ ] 执行记录持久化到数据库
   - [ ] 失败任务有补偿机制（非手动重跑）
   - [ ] 手动触发 API 可用

六条清单对应六道红线复查：**全部走基类、分布式有锁、锁有超时、记录落库、失败有补偿、人工可指挥**——六项全过，调度基础设施才算合格。

## 四、质量门禁：哪些必须做，哪些绝对不能做

**✅ 必须做**：

- 编译通过 + 测试通过；
- 所有定时任务经过 `AbstractTask` 封装（非裸 `@Scheduled`）；
- 分布式锁已实现（非仅依赖单机锁）；
- 锁超时自动释放（防任务挂掉锁不释放）；
- 执行记录持久化到数据库；
- 失败任务有补偿机制（非手动重跑）；
- 手动触发 API 可用。

**❌ 绝对不能做**：

- 禁止裸 `@Scheduled` 无锁保护（多副本重复执行）；
- 禁止任务锁无超时时间（会永久锁死）；
- 禁止任务失败无补偿机制（会丢失数据）；
- 禁止执行记录无持久化（无法排查问题）。

门禁的逻辑是"**调度的贞操观**"：

- **不能重**——必须有分布式锁，多副本也不重复执行；
- **不能死**——锁必须有超时，任务挂了锁自动释放；
- **不能丢**——失败必须有补偿，数据不漏；
- **不能盲**——必须有执行记录，排查有据可查。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 240" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_s3" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_s3"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="240" fill="url(#bg_s3)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">四条红线 = 四类事故</text>
  <rect x="40" y="50" width="340" height="70" rx="8" fill="#ef4444" opacity="0.12" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_s3)"/>
  <text x="210" y="72" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">禁裸 @Scheduled：重复执行</text>
  <text x="210" y="98" text-anchor="middle" fill="#64748b" font-size="9">多副本各跑一遍</text>
  <rect x="420" y="50" width="340" height="70" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_s3)"/>
  <text x="590" y="72" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">禁无超时锁：永久锁死</text>
  <text x="590" y="98" text-anchor="middle" fill="#64748b" font-size="9">任务挂了锁不释放</text>
  <rect x="40" y="140" width="340" height="70" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_s3)"/>
  <text x="210" y="162" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">禁无补偿：数据丢失</text>
  <text x="210" y="188" text-anchor="middle" fill="#64748b" font-size="9">失败的任务永远漏着</text>
  <rect x="420" y="140" width="340" height="70" rx="8" fill="#a855f7" opacity="0.12" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_s3)"/>
  <text x="590" y="162" text-anchor="middle" fill="#d8b4fe" font-size="12" font-weight="700">禁无记录：无法排查</text>
  <text x="590" y="188" text-anchor="middle" fill="#64748b" font-size="9">跑了什么全靠猜</text>
  <text x="400" y="232" text-anchor="middle" fill="#475569" font-size="10">定时任务是后台的守夜人，守夜人不能打盹、不能装睡、不能失忆</text>
</svg>
```

## 五、四条约束：边界在哪里

- **不生成 XXL-Job 管理台配置**——由调度中心管理（管理台归调度中心）；
- **不修改已有的调度代码**——只新增调度基础设施，建议用户逐步迁移（覆盖别人的调度代码等于破坏别人的定时任务）；
- **不引入未确认的调度框架版本**——项目中已有的优先复用；
- **不生成 Cron 表达式的 WEB 界面**——由前端团队实现。

边界一句话：**本技能管"任务怎么跑得可靠"，不碰"管理台、迁移、版本、前端界面"**。

## 六、与相邻技能的分工

| 场景 | 归属 |
|------|------|
| 分布式任务调度基础设施 | **本技能**（`/scheduler-toolkit`） |
| 定时任务的分布式锁复用 | `redis-cache-wrapper`（DistributedLock） |
| 调度异常诊断与修复 | `diagnosing-bugs`（byRootCause） |

- **本技能 vs redis-cache-wrapper**：scheduler-toolkit 用锁来防重复调度；redis-cache-wrapper 的 `DistributedLock` 是通用锁，定时任务可以直接复用它——一个是任务专用，一个是通用可用；
- **本技能 vs diagnosing-bugs**：调度任务出问题（不跑/重复跑/死锁），diagnosing-bugs 负责根因推导，本技能负责把任务基础设施本身做对。

## 七、完成标志

`/scheduler-toolkit` 的完成标志有三个：

1. **目标组件已生成**：task/lock/retry/console 按需产出——该有的都有；
2. **测试通过 + 检查清单全过**：六条红线逐项确认——不是"写了"，是"验证了"；
3. **业务代码不再裸用 `@Scheduled`**：所有定时任务经过 `AbstractTask`，有锁、有幂等、有记录、有补偿——纪律已生效。

三个标志对应三问：**补齐了吗**（组件）？**钉死了吗**（测试+清单）？**生效了吗**（不再裸奔）？——三个都答"是"，分布式调度才算真正落地。

## 八、写在最后

`/scheduler-toolkit` 的全部设计，浓缩成四句话：

1. **骨架统一**——AbstractTask 模板方法管抢锁、幂等、记录、释放，业务只填 execute。
2. **锁要超时**——SETNX PX 30000，任务挂了锁自动释放，不永久锁死。
3. **失败有补偿**——重试 3 次、间隔 30s、补偿扫描每 5 分钟，数据不沉底。
4. **记录落库**——每次运行都有痕可查，排查不靠猜。

一句话记住它：**/scheduler-toolkit 是定时任务的"五件套守夜人"——任务基类模板方法、分布式锁调度、失败重试补偿、执行记录落库、手动触发暂停 API，用"不能重、不能死、不能丢、不能盲"四条纪律，让每个定时任务都跑得可靠、查得到、指挥得动。**