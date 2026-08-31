# /rocketmq-toolkit：消息六件套——生产者、消费者、事务、顺序、延迟、DLQ，一次封装全包

> 命令深度拆解 · 第 48 篇 · 约 8000 字 · 6 个 SVG 图

---

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 300" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_m0" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_m0"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="300" fill="url(#bg_m0)" rx="10"/>
  <text x="400" y="28" text-anchor="middle" fill="#e2e8f0" font-size="26" font-weight="700">/rocketmq-toolkit：消息六件套</text>
  <rect x="30" y="55" width="140" height="95" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_m0)"/>
  <text x="100" y="80" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">producer</text>
  <text x="100" y="104" text-anchor="middle" fill="#64748b" font-size="9">同步/异步/单向</text>
  <text x="100" y="126" text-anchor="middle" fill="#64748b" font-size="8">失败自动重试 3 次</text>
  <rect x="187" y="55" width="140" height="95" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_m0)"/>
  <text x="257" y="80" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">consumer</text>
  <text x="257" y="104" text-anchor="middle" fill="#64748b" font-size="9">幂等 + 重试 + DLQ</text>
  <text x="257" y="126" text-anchor="middle" fill="#64748b" font-size="8">手动提交消费进度</text>
  <rect x="344" y="55" width="140" height="95" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_m0)"/>
  <text x="414" y="80" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">transaction</text>
  <text x="414" y="104" text-anchor="middle" fill="#64748b" font-size="9">半消息 + 反查</text>
  <text x="414" y="126" text-anchor="middle" fill="#64748b" font-size="8">60s 间隔 · 最多 15 次</text>
  <rect x="501" y="55" width="140" height="95" rx="8" fill="#a855f7" opacity="0.12" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_m0)"/>
  <text x="571" y="80" text-anchor="middle" fill="#d8b4fe" font-size="12" font-weight="700">order</text>
  <text x="571" y="104" text-anchor="middle" fill="#64748b" font-size="9">顺序消息</text>
  <text x="571" y="126" text-anchor="middle" fill="#64748b" font-size="8">队列选择器取模</text>
  <rect x="658" y="55" width="112" height="95" rx="8" fill="#ef4444" opacity="0.12" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_m0)"/>
  <text x="714" y="80" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">delay</text>
  <text x="714" y="104" text-anchor="middle" fill="#64748b" font-size="9">延迟消息</text>
  <text x="714" y="126" text-anchor="middle" fill="#64748b" font-size="8">枚举禁魔法数字</text>
  <text x="400" y="185" text-anchor="middle" fill="#475569" font-size="13">铁律：禁止直接操作 RocketMQ 客户端而不经过封装层</text>
  <rect x="60" y="205" width="160" height="45" rx="8" fill="#3b82f6" opacity="0.10" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_m0)"/>
  <text x="140" y="234" text-anchor="middle" fill="#93c5fd" font-size="11">① 确定生成目标</text>
  <rect x="240" y="205" width="160" height="45" rx="8" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_m0)"/>
  <text x="320" y="234" text-anchor="middle" fill="#86efac" font-size="11">② 生成代码</text>
  <rect x="420" y="205" width="160" height="45" rx="8" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_m0)"/>
  <text x="500" y="234" text-anchor="middle" fill="#fde68a" font-size="11">③ 测试钉死</text>
  <rect x="600" y="205" width="160" height="45" rx="8" fill="#a855f7" opacity="0.10" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_m0)"/>
  <text x="680" y="234" text-anchor="middle" fill="#d8b4fe" font-size="11">④ 验证</text>
  <text x="400" y="286" text-anchor="middle" fill="#64748b" font-size="10">Topic/Group 归运维 · 序列化方式由用户定 · 客户端版本不擅引</text>
</svg>
```

## 一、/rocketmq-toolkit 解决的是什么问题

### 1.1 裸用 RocketMQ 客户端的事故清单

RocketMQ 的客户端 API 功能强大，但裸用几乎必然踩坑：

1. **发消息没封装**：每个业务自己 new Producer、自己拼 Topic——换环境要改全项目，发送失败没人管；
2. **消费丢了**：自动提交消费进度，业务处理到一半崩溃——消息"看似消费成功"实际没处理，数据丢了；
3. **重复消费**：没有幂等过滤，网络重试、消费重投——同一笔订单被处理两次，库存扣两次；
4. **事务消息没反查**：半消息发出去了，本地事务执行结果没上报——要么消息永远悬着，要么超时回滚，两边数据不一致；
5. **顺序消息乱序**：生产者随机选队列、消费者并行消费——同一个订单的消息被并发处理，顺序全乱；
6. **延迟级别用魔法数字**：`setDelayTimeLevel(4)` 躺在代码里，没人知道 4 是几秒。

`/rocketmq-toolkit` 为项目生成 RocketMQ 消息中间件基础设施代码：生产者封装、消费者封装（幂等/重试/DLQ）、事务消息（半消息+反查）、顺序消息、延迟消息。**禁止直接操作 RocketMQ 客户端而不经过封装层**。

### 1.2 六个子命令，一套消息体系

| 子命令 | 组件 | 检测条件 |
|--------|------|---------|
| `producer` | ProducerWrapper | 检测到 `MQProducer` / `DefaultMQProducer` |
| `consumer` | ConsumerWrapper + 幂等 + DLQ | 检测到 `MQPushConsumer` / `DefaultMQPushConsumer` |
| `transaction` | 事务消息 | 检测到 `TransactionMQProducer` / 事务相关逻辑 |
| `order` | 顺序消息 | 检测到顺序消费需求（队列选择器） |
| `delay` | 延迟消息 | 检测到延迟消息需求 |
| `all`（默认） | 全部组件 | 首次引入 RocketMQ |

六个子命令覆盖消息全链路：**producer 管发，consumer 管收，transaction 管两边一致，order 管不乱序，delay 管定时，DLQ 管兜底**。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 260" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_m1" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_m1"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="260" fill="url(#bg_m1)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">消息全链路六环</text>
  <rect x="40" y="50" width="340" height="70" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_m1)"/>
  <text x="210" y="72" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">发：三种模式自动重试</text>
  <text x="210" y="98" text-anchor="middle" fill="#94a3b8" font-size="9">同步 send / 异步 asyncSend / 单向 sendOneway</text>
  <rect x="420" y="50" width="340" height="70" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_m1)"/>
  <text x="590" y="72" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">收：幂等+重试+DLQ</text>
  <text x="590" y="98" text-anchor="middle" fill="#94a3b8" font-size="9">手动提交 · 去重表/布隆 · 16 次后入 DLQ</text>
  <rect x="40" y="140" width="340" height="70" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_m1)"/>
  <text x="210" y="162" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">准：事务半消息+反查</text>
  <text x="210" y="188" text-anchor="middle" fill="#94a3b8" font-size="9">同步/异步/单向 · 本地事务结果决策</text>
  <rect x="420" y="140" width="340" height="70" rx="8" fill="#a855f7" opacity="0.12" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_m1)"/>
  <text x="590" y="162" text-anchor="middle" fill="#d8b4fe" font-size="12" font-weight="700">序：队列选择器取模</text>
  <text x="590" y="188" text-anchor="middle" fill="#94a3b8" font-size="9">同一业务 ID 同一队列 · 单队列顺序消费</text>
  <text x="400" y="242" text-anchor="middle" fill="#475569" font-size="10">消息要发得出去、收得稳、两边一致、顺序不乱、定时准</text>
</svg>
```

### 1.3 为什么"消费者必须手动提交 + 幂等"

消息消费的"至少一次"语义决定了：**网络重试、消费重投是常态，重复是必然的**。

- 自动提交进度 → 业务处理到一半崩溃，进度已提交，消息丢了；
- 手动提交 → 处理成功才提交，崩溃了下次重投；
- 重投必然重复 → 必须有幂等过滤（去重表/布隆过滤器），同一消息处理一次；
- 重试 16 次还失败 → 入 DLQ（`%DLQ%{consumerGroup}`），兜底不丢。

手动提交 + 幂等 + DLQ 是消费端的三件套：**不丢、不重、有兜底**。

## 二、触发方式与前置

**触发方式**两种：

- 独立命令 `/rocketmq-toolkit [producer|consumer|transaction|order|delay|all]`；
- 自动加载：coding-skill 阶段检测到 `RocketMQ` / `MQProducer` / `MQPushConsumer` / `@RocketMQListener` 时。

**前置条件**：

1. 已识别技术栈（Java RocketMQ-Client / Python rocketmq-client / Go rocketmq-client）；
2. 已确定目标包路径（按语言规范）；
3. 已确定 NameServer 地址或 ACK 地址；
4. 用户未指定 → 生成全部组件（`all`）。

前置的用意：**消息封装生成的是"按你的集群的代码"**——不知道用 Java/Python/Go、不知道 NameServer 地址，生成的封装连配置都写不对。

## 三、四步执行流程

### Step 1: 确定生成目标

按子命令或检测条件确定生成范围：

| 子命令 | 组件 | 检测条件 |
|--------|------|---------|
| `producer` | ProducerWrapper | 检测到 `MQProducer` / `DefaultMQProducer` |
| `consumer` | ConsumerWrapper + 幂等 + DLQ | 检测到 `MQPushConsumer` / `DefaultMQPushConsumer` |
| `transaction` | 事务消息 | 检测到 `TransactionMQProducer` / 事务相关逻辑 |
| `order` | 顺序消息 | 检测到顺序消费需求（队列选择器） |
| `delay` | 延迟消息 | 检测到延迟消息需求 |
| `all`（默认） | 全部组件 | 首次引入 RocketMQ |

Step 1 是"按需裁剪"：只有生产没消费？生成 producer。需要事务一致性？生成 transaction。首次引入？生成 all。**缺什么补什么，检测条件同时是体检**——裸 DefaultMQProducer、自动提交、魔法数字延迟级别，都会被照出来。

### Step 2: 生成代码文件

以 Java 示例，生成 `{basePackage}/rocketmq/` 包：

```
{basePackage}/rocketmq/
├── producer/
│   ├── ProducerWrapper.java          — 生产者封装（同步/异步/单向）
│   ├── TransactionProducer.java      — 事务消息生产者（半消息+反查）
│   └── OrderProducer.java            — 顺序消息生产者（队列选择器）
├── consumer/
│   ├── ConsumerWrapper.java          — 消费者封装（幂等过滤 + 重试 + DLQ）
│   ├── OrderConsumer.java            — 顺序消息消费者（单队列消费）
│   └── BroadcastConsumer.java        — 广播模式消费者
├── transaction/
│   ├── TransactionListener.java      — 事务反查监听器接口
│   └── TransactionChecker.java       — 反查实现（检查本地事务状态）
├── dlq/
│   ├── DeadLetterQueue.java          — 死信队列处理
│   └── DqlRepublish.java             — DLQ 重新投递（定时重扫）
├── config/
│   ├── RocketMQConfig.java           — 配置（NameServer/线程数/超时）
│   └── RocketMQConstant.java         — 常量（Topic/Tag/消费者组）
└── monitor/
    └── RocketMQMonitor.java          — 消息积压监控
```

**每个组件的关键实现约束**——本技能的灵魂就在这：

- **`ProducerWrapper`**：支持同步（send）、异步（asyncSend）、单向（sendOneway）三种模式，发送失败自动重试（retries=3），发送结果回调日志——**三种模式按需选，失败自动重试**；
- **事务消息**：发送半消息 → 执行本地事务 → 根据本地事务结果提交/回滚消息 → 反查机制（transactionCheckListener），反查间隔 60s，最大反查次数 15——**半消息悬着不提交，反查最多 15 次兜底**；
- **顺序消息**：生产者使用 `MessageQueueSelector`（按业务 ID 取模），消费者使用 `MessageListenerOrderly`（单队列顺序消费），消费失败本地重试（suspendCurrentQueueTimeMillis=1000）——**同一业务 ID 永远同一队列，顺序不乱**；
- **延迟消息**：延迟级别（1s/5s/10s/30s/1m/2m/3m/4m/5m/6m/7m/8m/9m/10m/20m/30m/1h/2h），封装为枚举，禁止直接使用 `message.setDelayTimeLevel(int)` 的魔法数字——**延迟级别有名字，不靠魔法数字猜**；
- **`ConsumerWrapper`**：手动提交消费进度，幂等过滤（去重表/布隆过滤器），消费失败重试次数 16 次后入 DLQ，DLQ Topic 命名 `%DLQ%{consumerGroup}`——**不丢、不重、有兜底**。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 260" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_m2" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_m2"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="260" fill="url(#bg_m2)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">最容易搞错的两个链接</text>
  <rect x="40" y="50" width="340" height="95" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_m2)"/>
  <text x="210" y="72" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">事务消息：半消息 → 反查</text>
  <text x="210" y="96" text-anchor="middle" fill="#94a3b8" font-size="9">半消息发出 → 本地事务 → 提交/回滚</text>
  <text x="210" y="116" text-anchor="middle" fill="#94a3b8" font-size="9">反查 60s 一次 · 最多 15 次</text>
  <text x="210" y="136" text-anchor="middle" fill="#64748b" font-size="8">无反查 → 悬着 → 超时回滚</text>
  <rect x="420" y="50" width="340" height="95" rx="8" fill="#a855f7" opacity="0.12" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_m2)"/>
  <text x="590" y="72" text-anchor="middle" fill="#d8b4fe" font-size="12" font-weight="700">顺序消息：队列选择器</text>
  <text x="590" y="96" text-anchor="middle" fill="#94a3b8" font-size="9">业务 ID 取模 → 同一队列</text>
  <text x="590" y="116" text-anchor="middle" fill="#94a3b8" font-size="9">MessageListenerOrderly 单队列</text>
  <text x="590" y="136" text-anchor="middle" fill="#64748b" font-size="8">随机选队列 → 乱序</text>
  <rect x="120" y="175" width="560" height="60" rx="8" fill="#ef4444" opacity="0.12" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_m2)"/>
  <text x="400" y="200" text-anchor="middle" fill="#fca5a5" font-size="11" font-weight="700">延迟级别魔法数字 = 没人知道 4 是几秒</text>
  <text x="400" y="224" text-anchor="middle" fill="#94a3b8" font-size="9">枚举封装，禁止 setDelayTimeLevel(int) 直接裸用</text>
</svg>
```

### Step 3: 生成测试

每个核心组件配测试，测试要求紧扣实现约束（使用 Mock/Embedded RocketMQ）：

- **生产者**：验证同步/异步/单向三种发送模式，验证重试机制——**发送失败自动重试 3 次，必须有测试证明**；
- **消费者**：验证幂等过滤、消息重试、DLQ 入队——**同一消息不重处理、失败 16 次入 DLQ，必须有测试证明**；
- **事务消息**：验证半消息发送、本地事务执行回调、反查机制——**半消息发出的消息，反查能查到本地事务结果，必须有测试证明**；
- **DLQ 重投**：验证定时重扫死信队列重新投递——**死信不烂在队列里，必须有测试证明**。

测试的作用是把六道纪律**钉死**：三种模式、幂等、重试、DLQ、事务反查、顺序不乱——纪律靠测试保证，不靠 review 保证。

### Step 4: 验证

1. 编译通过 + 测试通过；
2. 检查清单逐项确认：
   - [ ] 所有生产者经过 `ProducerWrapper` 封装
   - [ ] 所有消费者经过 `ConsumerWrapper` 封装
   - [ ] 事务消息有反查实现（非空 `TransactionListener`）
   - [ ] 顺序消息使用 `MessageQueueSelector`（非随机选择队列）
   - [ ] 延迟消息使用枚举（非 `setDelayTimeLevel` 魔法数字）
   - [ ] 幂等过滤已实现（去重表/布隆过滤器）

六条清单对应六道红线复查：**进出封装、事务有反查、顺序有选择器、延迟有枚举、消费有幂等**——六项全过，消息基础设施才算合格。

## 四、质量门禁：哪些必须做，哪些绝对不能做

**✅ 必须做**：

- 编译通过 + 测试通过；
- 所有生产者经过 `ProducerWrapper` 封装；
- 所有消费者经过 `ConsumerWrapper` 封装；
- 事务消息有反查实现（非空 `TransactionListener`）；
- 顺序消息使用 `MessageQueueSelector`（非随机选择队列）；
- 延迟消息使用枚举（非 `setDelayTimeLevel` 魔法数字）；
- 幂等过滤已实现（去重表/布隆过滤器）。

**❌ 绝对不能做**：

- 禁止直接使用 `setDelayTimeLevel` 魔法数字（应使用延迟级别枚举）；
- 禁止事务消息无反查（半消息无反查会超时回滚）；
- 禁止顺序消息使用随机队列选择（会乱序）；
- 禁止自动提交消费进度（应手动提交，防止丢消息）。

门禁的逻辑是"**消息的贞操观**"：

- **不能裸**——生产消费必须走封装，裸写绕过规范；
- **不能悬**——半消息必须能反查，悬着的消息是隐患；
- **不能乱**——顺序语义必须靠选择器，随机选队列会乱序；
- **不能丢**——消费进度必须手动提交，自动提交会丢消息。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 240" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_m3" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_m3"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="240" fill="url(#bg_m3)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">四条红线 = 四类事故</text>
  <rect x="40" y="50" width="340" height="70" rx="8" fill="#ef4444" opacity="0.12" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_m3)"/>
  <text x="210" y="72" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">禁魔法数字：没人懂</text>
  <text x="210" y="98" text-anchor="middle" fill="#64748b" font-size="9">延迟级别必须用枚举</text>
  <rect x="420" y="50" width="340" height="70" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_m3)"/>
  <text x="590" y="72" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">禁无反查：半消息悬着</text>
  <text x="590" y="98" text-anchor="middle" fill="#64748b" font-size="9">超时回滚 = 两边不一致</text>
  <rect x="40" y="140" width="340" height="70" rx="8" fill="#a855f7" opacity="0.12" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_m3)"/>
  <text x="210" y="162" text-anchor="middle" fill="#d8b4fe" font-size="12" font-weight="700">禁随机队列：必然乱序</text>
  <text x="210" y="188" text-anchor="middle" fill="#64748b" font-size="9">顺序语义必须有选择器</text>
  <rect x="420" y="140" width="340" height="70" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_m3)"/>
  <text x="590" y="162" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">禁自动提交：消息丢失</text>
  <text x="590" y="188" text-anchor="middle" fill="#64748b" font-size="9">手动提交 · 幂等过滤 · DLQ 兜底</text>
  <text x="400" y="232" text-anchor="middle" fill="#475569" font-size="10">消息是分布式系统的血管，血管不能堵、不能漏、不能乱流</text>
</svg>
```

## 五、四条约束：边界在哪里

- **不生成 Topic/Group 创建脚本**——由运维或控制台管理（消息的"容器"归运维）；
- **不修改已有 RocketMQ 客户端代码**——只新增封装层，覆盖别人的代码等于破坏别人的稳定性；
- **不引入未确认的 RocketMQ 客户端版本**——项目中已有的优先复用；
- **不擅自决定消息序列化方式**——JSON/Protobuf 由用户指定。

边界一句话：**本技能管"消息怎么收发、怎么保证一致与顺序"，不碰"Topic 怎么建、客户端版本怎么升、消息怎么序列化"**。

## 六、与相邻技能的分工

| 场景 | 归属 |
|------|------|
| 消息生产/消费/事务/顺序/延迟/DLQ | **本技能**（`/rocketmq-toolkit`） |
| Kafka 消息封装 | `kafka-toolkit`（同族 API） |
| 消息链路 traceId 透传 | `logging-toolkit`（MDC trace 注入） |

- **本技能 vs kafka-toolkit**：两套都是消息组件，但 API 族不同——RocketMQ 重事务/顺序/延迟消息，Kafka 重吞吐/流式/积压监控；选哪个由架构决策，本技能只把选定那套封装对；
- **本技能 vs logging-toolkit**：消息在链路里带 traceId，logging-toolkit 负责把 traceId 记进日志——一个在消息里传，一个在日志里记，配合成完整链路。

## 七、完成标志

`/rocketmq-toolkit` 的完成标志有三个：

1. **目标组件已生成**：producer/consumer/transaction/order/delay 按需产出——该有的都有；
2. **测试通过 + 检查清单全过**：六条红线逐项确认——不是"写了"，是"验证了"；
3. **业务代码不再直接操作 RocketMQ 客户端**：所有生产消费经过 Wrapper，消费手动提交+幂等——纪律已生效。

三个标志对应三问：**补齐了吗**（组件）？**钉死了吗**（测试+清单）？**生效了吗**（不再裸写）？——三个都答"是"，消息基础设施才算真正落地。

## 八、写在最后

`/rocketmq-toolkit` 的全部设计，浓缩成四句话：

1. **进出两端都封装**——ProducerWrapper 管发、ConsumerWrapper 管收，业务不碰裸客户端。
2. **一致性靠反查**——半消息+本地事务+反查，两边要么都成要么都滚。
3. **顺序靠选择器**——业务 ID 取模选队列，同一单永远同队列。
4. **兜底靠 DLQ**——消费失败 16 次入死信，定时重扫不烂队。

一句话记住它：**/rocketmq-toolkit 是消息的"六件套管家"——生产三种模式封装、消费幂等重试手工提交、事务半消息反查、顺序队列选择器、延迟级别枚举、DLQ 兜底重投，用"不裸写、不悬着、不乱序、不丢消息"四条纪律，让消息发得出去、收得稳、两边一致、顺序不乱。**