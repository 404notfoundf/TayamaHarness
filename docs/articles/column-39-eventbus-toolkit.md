# /eventbus-toolkit：事件总线封装——没有追踪的事件，等于没发过

> 命令深度拆解 · 第 39 篇 · 约 8000 字 · 7 个 SVG 图

---

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 300" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_e0" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_e0"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="300" fill="url(#bg_e0)" rx="10"/>
  <text x="400" y="28" text-anchor="middle" fill="#e2e8f0" font-size="26" font-weight="700">/eventbus-toolkit：事件总线五件套</text>
  <rect x="40" y="55" width="140" height="90" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_e0)"/>
  <text x="110" y="80" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">sync</text>
  <text x="110" y="104" text-anchor="middle" fill="#64748b" font-size="9">同步事件总线</text>
  <text x="110" y="126" text-anchor="middle" fill="#64748b" font-size="8">同线程发布消费</text>
  <rect x="200" y="55" width="140" height="90" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_e0)"/>
  <text x="270" y="80" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">async</text>
  <text x="270" y="104" text-anchor="middle" fill="#64748b" font-size="9">异步事件总线</text>
  <text x="270" y="126" text-anchor="middle" fill="#64748b" font-size="8">线程池消费 + 重试</text>
  <rect x="360" y="55" width="140" height="90" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_e0)"/>
  <text x="430" y="80" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">transactional</text>
  <text x="430" y="104" text-anchor="middle" fill="#64748b" font-size="9">事务事件总线</text>
  <text x="430" y="126" text-anchor="middle" fill="#64748b" font-size="8">提交后才发布</text>
  <rect x="520" y="55" width="140" height="90" rx="8" fill="#a855f7" opacity="0.12" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_e0)"/>
  <text x="590" y="80" text-anchor="middle" fill="#d8b4fe" font-size="12" font-weight="700">trace</text>
  <text x="590" y="104" text-anchor="middle" fill="#64748b" font-size="9">事件追踪</text>
  <text x="590" y="126" text-anchor="middle" fill="#64748b" font-size="8">发布/消费/失败记录</text>
  <rect x="680" y="55" width="100" height="90" rx="8" fill="#ef4444" opacity="0.12" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_e0)"/>
  <text x="730" y="80" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">retry</text>
  <text x="730" y="104" text-anchor="middle" fill="#64748b" font-size="9">失败重试</text>
  <text x="730" y="126" text-anchor="middle" fill="#64748b" font-size="8">定时扫描重发</text>
  <text x="400" y="185" text-anchor="middle" fill="#475569" font-size="13">铁律：禁止在业务代码中直接用 ApplicationEventPublisher 而不经过封装层</text>
  <rect x="60" y="205" width="160" height="45" rx="8" fill="#3b82f6" opacity="0.10" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_e0)"/>
  <text x="140" y="234" text-anchor="middle" fill="#93c5fd" font-size="11">① 确定目标</text>
  <rect x="240" y="205" width="160" height="45" rx="8" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_e0)"/>
  <text x="320" y="234" text-anchor="middle" fill="#86efac" font-size="11">② 生成代码</text>
  <rect x="420" y="205" width="160" height="45" rx="8" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_e0)"/>
  <text x="500" y="234" text-anchor="middle" fill="#fde68a" font-size="11">③ 生成测试</text>
  <rect x="600" y="205" width="160" height="45" rx="8" fill="#a855f7" opacity="0.10" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_e0)"/>
  <text x="680" y="234" text-anchor="middle" fill="#d8b4fe" font-size="11">④ 验证</text>
  <text x="400" y="280" text-anchor="middle" fill="#64748b" font-size="10\">事务事件提交后发 · 异步要有界线程池 · 失败必重试 · 全部可追踪</text>
</svg>
```

## 一、/eventbus-toolkit 解决的是什么问题

### 1.1 事件驱动最危险的三个洞

事件驱动架构让模块解耦、让扩展优雅，但新手直接上手 `ApplicationEventPublisher.publishEvent()` 会掉进三个洞：

1. **事务洞**：在事务中间发事件，消费方立刻执行——事务一回头回滚了，事件已经发出去了，"我听到订单创建了就通知物流"，可订单其实没建成功。**事件在事务提交后才该发**；
2. **追踪洞**：事件像箭一样射出去，发没发出去、谁消费了、消费成功了没有——一概没有记录。出了问题连"这个事件到底发没发"都答不上来；
3. **重试洞**：消费方崩了，事件就丢了。没有重试机制的事件总线，等于"发出去就不管了"。

`/eventbus-toolkit` 就是来补这三个洞的：生成事件总线基础设施（同步/异步/事务事件）、事件追踪（发布/消费/失败记录 + 监控）、失败重试机制。**所有事件发布必须经过 EventBus 封装层**，不允许业务代码裸发。

### 1.2 五种总线，各管一头

| 子命令 | 组件 | 用在什么场景 |
|--------|------|-------------|
| `sync` | 同步事件总线 | 同一事务内的事件处理（如更新库存后同步扣减） |
| `async` | 异步事件总线 | 事件处理耗时，需要异步（如发通知） |
| `transactional` | 事务事件总线 | 事件必须在事务提交后才发布（如订单创建成功通知） |
| `trace` | 事件追踪 | 发布/消费/失败无处可查（生产必备） |
| `retry` | 失败重试 | 消费失败不能丢事件 |

**事务事件是其中最容易错的**：在 `@Transactional` 方法中发布事件，事务提交后统一消费，事务回滚则事件不发布（用 `TransactionSynchronizationManager` 注册回调）。这五个组件组合起来，就是一套完整的事件驱动基础设施。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 250" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_e1" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_e1"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="250" fill="url(#bg_e1)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">另一个视角：五个组件 = 三个洞的补丁</text>
  <rect x="40" y="50" width="340" height="70" rx="8" fill="#ef4444" opacity="0.12" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_e1)"/>
  <text x="210" y="72" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">洞① 事务洞</text>
  <text x="210" y="98" text-anchor="middle" fill="#64748b" font-size="9">→ 补丁：transactional（提交后才发）</text>
  <rect x="40" y="140" width="340" height="70" rx="8" fill="#a855f7" opacity="0.12" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_e1)"/>
  <text x="210" y="162" text-anchor="middle" fill="#d8b4fe" font-size="12" font-weight="700">洞② 追踪洞</text>
  <text x="210" y="188" text-anchor="middle" fill="#64748b" font-size="9">→ 补丁：trace（发布/消费/失败全记录）</text>
  <rect x="420" y="50" width="340" height="70" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_e1)"/>
  <text x="590" y="72" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">洞③ 重试洞</text>
  <text x="590" y="98" text-anchor="middle" fill="#64748b" font-size="9">→ 补丁：retry（失败重发）+ async（有界线程池）</text>
  <rect x="420" y="140" width="340" height="70" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_e1)"/>
  <text x="590" y="162" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">基础件：sync + core</text>
  <text x="590" y="188" text-anchor="middle" fill="#64748b" font-size="9">DomainEvent 基类带 eventId/聚合根/时间戳/来源</text>
  <text x="400" y="240" text-anchor="middle" fill="#475569" font-size="10">五个子命令不是摆设——每个都对应事件驱动的一个真实事故</text>
</svg>
```

### 1.3 与 message-queue toolkit 的边界：进程内 vs 跨服务

- `eventbus-toolkit` 管**进程内事件**（Spring Events / Guava EventBus / Python PyPubSub）——发事件的人和消费的人在同一进程里；
- `kafka-toolkit` / `rocketmq-toolkit` 管**跨服务消息**——事件要发到另一个服务去。

判断标准很简单：**消费方在本进程里，用 eventbus；消费方在另一个服务，用 MQ**。两者是兄弟不是替代，eventbus 也绝不生成消息队列集成——那是 Kafka/RocketMQ toolkit 的活。

## 二、触发方式与前置

**触发方式**两种：

- 独立命令 `/eventbus-toolkit [sync|async|transactional|trace|all]`；
- 自动加载：coding-skill 阶段检测到 `ApplicationEventPublisher` / `@EventListener` / `EventBus` / `go-eventbus` 等符号时——**看到裸发事件，自动补封装**。

**前置条件**：

1. 已识别技术栈 + 事件框架（Spring Events / Guava EventBus / Go eventbus / Python PyPubSub）；
2. 已确定目标包路径（按语言规范）；已确定已定义的事件类型列表;
3. 用户未指定 → 生成全部组件（`all`）。

前置的用意：**连框架和事件类型都没确定的生成，是猜**——猜出来的代码没人敢用。技术栈定 → 包路径定 → 事件类型定，三样齐了才动手。

## 三、四步执行流程

### Step 1: 确定生成目标

按子命令或检测条件确定要生成哪些组件：

| 子命令 | 组件 | 检测条件 |
|--------|------|---------|
| `sync` | 同步事件总线 | 存在事件发布但无异步需求 |
| `async` | 异步事件总线 | 事件处理耗时，需要异步 |
| `transactional` | 事务事件 | 事件需要在事务提交后才发布 |
| `trace` | 事件追踪 | 事件发布/消费无记录 |
| `all`（默认） | 全部组件 | 首次引入事件驱动架构 |

Step 1 是"按需裁剪"：不是每次都生成全部，而是**缺什么补什么**——已有同步总线只差异步？只生成 async。首次引入？生成 all。裁剪的依据是检测条件，不是用户的随口要求。

### Step 2: 生成代码文件

以 Java 示例，生成 `{basePackage}/eventbus/` 包：

```
{basePackage}/eventbus/
├── core/        EventBus 接口 / DomainEvent 基类 / EventPriority 枚举
├── bus/         SyncEventBus / AsyncEventBus / TransactionalEventBus
├── annotation/  EventHandler 注解 / TransactionalEvent 注解
├── store/       EventRecord 实体 / EventRecordRepository / EventTracer
├── retry/       EventRetryStrategy / EventRetryProcessor
└── config/      EventBusConfig（线程池/重试/开关）
```

**每个组件的关键实现约束**——这是本技能的灵魂：

- **`SyncEventBus`**：同一线程同步发布和消费，适用于同一事务内的事件处理（如更新库存后同步扣减），消费异常抛出终止当前事务——同步总线就该"同生共死"，消费失败不能让业务假装成功；
- **`AsyncEventBus`**：线程池异步消费，线程池大小可配置，使用 `@Async` 或自定义线程池，消费异常记录日志并触发重试——**禁止无界线程池**，否则一个事件风暴就拖垮整台机器；
- **`TransactionalEventBus`**：在 `@Transactional` 事务方法中发布事件，事务提交后统一消费，事务回滚则事件不发布（使用 `TransactionSynchronizationManager` 注册回调）——这是"订单创建成功"类事件的标准姿势；
- **`DomainEvent`**：基类字段 eventId（UUID）、aggregateRootId（聚合根ID）、occurredAt（时间戳）、source（发布来源）——每个事件都有身份、有来处、有时间；
- **事件追踪**：记录事件发布（事件ID/类型/状态/发布时间）、消费（事件ID/消费者/消费时间/结果）、失败（失败原因/重试次数），事件记录持久化到数据库——**没有追踪的事件，等于没发过**。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 260" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_e2" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_e2"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="260" fill="url(#bg_e2)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">四种总线的"性格"</text>
  <rect x="40" y="50" width="340" height="75" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_e2)"/>
  <text x="210" y="70" text-anchor="middle" fill="#93c5fd" font-size="11" font-weight="700">Sync：同生共死</text>
  <text x="210" y="92" text-anchor="middle" fill="#94a3b8" font-size="9">同线程发布消费，异常向上抛终止事务</text>
  <text x="210" y="112" text-anchor="middle" fill="#64748b" font-size="8">适合：库存扣减这种必须立刻成的事</text>
  <rect x="420" y="50" width="340" height="75" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_e2)"/>
  <text x="590" y="70" text-anchor="middle" fill="#86efac" font-size="11" font-weight="700">Async：隔离消化</text>
  <text x="590" y="92" text-anchor="middle" fill="#94a3b8" font-size="9">有界线程池消费，异常记录 + 触发重试</text>
  <text x="590" y="112" text-anchor="middle" fill="#64748b" font-size="8">适合：发通知这种慢且可后置的事</text>
  <rect x="40" y="145" width="340" height="75" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_e2)"/>
  <text x="210" y="165" text-anchor="middle" fill="#fde68a" font-size="11" font-weight="700">Transactional：提交才出声</text>
  <text x="210" y="187" text-anchor="middle" fill="#94a3b8" font-size="9">事务提交后统一发布，回滚则不发布</text>
  <text x="210" y="207" text-anchor="middle" fill="#64748b" font-size="8">适合：订单创建成功这类的"事实宣告"</text>
  <text x="420" y="145" width="340" height="75" rx="8" fill="#a855f7" opacity="0.12" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_e2)"/>
  <text x="590" y="165" text-anchor="middle" fill="#d8b4fe" font-size="11" font-weight="700">Trace + Retry：全程留痕</text>
  <text x="590" y="187" text-anchor="middle" fill="#94a3b8" font-size="9">发布/消费/失败持久化，失败定时重发</text>
  <text x="590" y="207" text-anchor="middle" fill="#64748b" font-size="8">出问题能查到"事件到底发没发、谁处理了"</text>
  <text x="400" y="248" text-anchor="middle" fill="#475569" font-size="10">选哪种总线取决于"这件事等不等得起、反不反悔得了"</text>
</svg>
```

### Step 3: 生成测试

每个核心组件配测试，测试要求紧扣实现约束：

- **同步事件**：验证同一线程发布消费、消费异常终止；
- **异步事件**：验证线程池消费、异常日志记录；
- **事务事件**：验证事务提交后发布、事务回滚不发布（这是最容易逃逸的约束，必须用测试钉死）；
- **事件追踪**：验证发布/消费/失败记录完整性。

### Step 4: 验证

1. 编译通过 + 测试通过；
2. 检查清单逐项确认：
   - [ ] 所有事件发布经过 `EventBus` 封装（非直接 `publishEvent()`）
   - [ ] 事务事件在事务提交后发布（非事务中发布）
   - [ ] 异步事件有线程池配置（非无界线程池）
   - [ ] 事件记录已持久化
   - [ ] 失败事件有重试机制

这五条检查清单就是五个洞的"复查"：**封装（无裸发）、事务（提交后发）、有界（不无界）、留痕（持久化）、重试（不丢失）**——五项全过，事件基础设施才算合格。

## 四、质量门禁：哪些必须做，哪些绝对不能做

**✅ 必须做**：

- 所有事件发布经过 `EventBus` 封装（非直接 `publishEvent()`）；
- 事务事件在事务提交后发布；
- 异步事件有界线程池；
- 事件记录持久化；
- 失败事件有重试机制。

**❌ 绝对不能做**：

- 禁止直接使用 `ApplicationEventPublisher.publishEvent()`（应经过 EventBus 封装）；
- 禁止事务事件在事务中直接发布（会因事务回滚导致事件误发）；
- 禁止异步事件无界线程池（会耗尽系统资源）；
- 禁止事件失败无重试（会丢失事件处理）。

门禁的逻辑是"**事件可以晚到，但不能错到和丢到**"：同步失败可以抛异常终止，异步失败可以重试，事务回滚可以不发——唯独"事务中裸发"和"失败即丢"是两条红线，因为前者造成**假消息**（订阅方以为发生了，其实没发生），后者造成**真丢失**（该发生的没发生）。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 250" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_e3" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_e3"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="250" fill="url(#bg_e3)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">事件可以晚到，但不能错到和丢到</text>
  <rect x="40" y="50" width="340" height="70" rx="8" fill="#ef4444" opacity="0.12" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_e3)"/>
  <text x="210" y="72" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">错到 = 假消息 ❌</text>
  <text x="210" y="98" text-anchor="middle" fill="#64748b" font-size="9">事务中裸发事件，回滚后订阅方已在处理</text>
  <rect x="420" y="50" width="340" height="70" rx="8" fill="#ef4444" opacity="0.12" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_e3)"/>
  <text x="590" y="72" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">丢到 = 真丢失 ❌</text>
  <text x="590" y="98" text-anchor="middle" fill="#64748b" font-size="9">失败无重试，消费崩了事件就没了</text>
  <rect x="40" y="140" width="340" height="70" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_e3)"/>
  <text x="210" y="162" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">晚到 = 可接受 ✅</text>
  <text x="210" y="188" text-anchor="middle" fill="#64748b" font-size="9">异步 + 重试，事情最终一定会做</text>
  <rect x="420" y="140" width="340" height="70" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_e3)"/>
  <text x="590" y="162" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">留痕 = 可复盘 ✅</text>
  <text x="590" y="188" text-anchor="middle" fill="#64748b" font-size="9">发布/消费/失败全记录，出问题查得到</text>
  <text x="400" y="240" text-anchor="middle" fill="#475569" font-size="10">四条红线 = 假消息、真丢失、资源耗尽、无处可查</text>
</svg>
```

## 五、四条约束：边界在哪里

- **不生成事件定义的具体业务逻辑**——只生成事件基础设施（发布怎么走、事务怎么控），事件里装什么数据是业务的事；
- **不修改已有的事件处理代码**——只新增封装层，建议用户逐步迁移——历史代码不动，新代码走新路；
- **不引入未确认的事件框架版本**——框架版本是架构决策，得先确认；
- **不生成消息队列集成**（Kafka/RocketMQ 由对应 toolkit 处理）——进程内事件和跨服务消息是两码事。

边界一句话：**本技能生成"事件怎么发、怎么管"的管道，不碰"事件里装什么"和"事件发去哪"**。

## 六、与相邻技能的分工

| 场景 | 归属 |
|------|------|
| 进程内事件总线封装 | **本技能**（`/eventbus-toolkit`） |
| 跨服务消息队列 | `kafka-toolkit` / `rocketmq-toolkit` |
| 事件消费逻辑编写 | `coding-skill`（按规范写处理函数） |

- **本技能 vs MQ toolkit**：进程内 vs 跨服务——消费方在本进程用 eventbus，在别的服务用 MQ；
- **本技能 vs coding-skill**：一个生成管道，一个写业务——管道先把"发布纪律"钉死，业务代码只关心业务。

## 七、完成标志

`/eventbus-toolkit` 的完成标志有三个：

1. **目标组件已生成**：Sync/Async/Transactional/Trace/Retry 按需产出，DomainEvent 基类带 eventId/聚合根/时间戳/来源——该补的洞都补上了；
2. **测试通过 + 检查清单全过**：编译绿、测试绿，封装/事务/有界/留痕/重试五项全过——不是"写了"，是"钉死了"；
3. **业务代码不再裸发事件**：所有事件发布经过 EventBus 封装层（或已给出迁移建议）——纪律已生效。

三个标志对应三问：**补齐了吗**（组件）？**钉死了吗**（测试+清单）？**生效了吗**（不再裸发）？——三个都答"是"，事件基础设施才算真正落地。

## 八、写在最后

`/eventbus-toolkit` 的全部设计，浓缩成四句话：

1. **事件驱动有三个洞：事务洞、追踪洞、重试洞**——本技能就是它们的补丁。
2. **事务事件提交后发，回滚不发**——不然你发出的是假消息。
3. **没有追踪的事件等于没发过**——发布/消费/失败，全都要有记录。
4. **异步必有界，失败必重试**——资源要保护，事件不能丢。

一句话记住它：**/eventbus-toolkit 是进程内事件驱动的"管道工"——它生成同步、异步、事务、追踪、重试五位一体的事件总线封装层，用"提交后才发、失败必重试、全程可追踪"三条纪律，让每一件域事件都发得对、不丢失、查得清。**