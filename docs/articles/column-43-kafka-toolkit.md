# /kafka-toolkit：Kafka 封装——手动提交、幂等消费、三次重试进 DLQ，一条都不能少

> 命令深度拆解 · 第 43 篇 · 约 8000 字 · 7 个 SVG 图

---

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 300" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_ka0" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_ka0"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="300" fill="url(#bg_ka0)" rx="10"/>
  <text x="400" y="28" text-anchor="middle" fill="#e2e8f0" font-size="26" font-weight="700">/kafka-toolkit：消息消费的完整防线</text>
  <rect x="40" y="55" width="160" height="90" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_ka0)"/>
  <text x="120" y="80" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">consumer</text>
  <text x="120" y="104" text-anchor="middle" fill="#64748b" font-size="9">手动提交 offset</text>
  <text x="120" y="126" text-anchor="middle" fill="#64748b" font-size="8">幂等过滤 + 重试</text>
  <rect x="220" y="55" width="160" height="90" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_ka0)"/>
  <text x="300" y="80" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">producer</text>
  <text x="300" y="104" text-anchor="middle" fill="#64748b" font-size="9">acks=all + 重试</text>
  <text x="300" y="126" text-anchor="middle" fill="#64748b" font-size="8">失败落盘 + 定时重扫</text>
  <rect x="400" y="55" width="160" height="90" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_ka0)"/>
  <text x="480" y="80" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">dlq</text>
  <text x="480" y="104" text-anchor="middle" fill="#64748b" font-size="9">死信队列兜底</text>
  <text x="480" y="126" text-anchor="middle" fill="#64748b" font-size="8">结构完整可重投</text>
  <rect x="580" y="55" width="180" height="90" rx="8" fill="#a855f7" opacity="0.12" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_ka0)"/>
  <text x="670" y="80" text-anchor="middle" fill="#d8b4fe" font-size="12" font-weight="700">monitor</text>
  <text x="670" y="104" text-anchor="middle" fill="#64748b" font-size="9">Lag 监控 + 告警</text>
  <text x="670" y="126" text-anchor="middle" fill="#64748b" font-size="8">Lag>10000 Warning</text>
  <text x="400" y="185" text-anchor="middle" fill="#475569" font-size="13">铁律：禁止手动消费 Kafka 消息而不经过封装层</text>
  <rect x="60" y="205" width="160" height="45" rx="8" fill="#3b82f6" opacity="0.10" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_ka0)"/>
  <text x="140" y="234" text-anchor="middle" fill="#93c5fd" font-size="11">① 确定目标</text>
  <rect x="240" y="205" width="160" height="45" rx="8" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_ka0)"/>
  <text x="320" y="234" text-anchor="middle" fill="#86efac" font-size="11">② 生成代码</text>
  <rect x="420" y="205" width="160" height="45" rx="8" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_ka0)"/>
  <text x="500" y="234" text-anchor="middle" fill="#fde68a" font-size="11">③ 测试钉死</text>
  <rect x="600" y="205" width="160" height="45" rx="8" fill="#a855f7" opacity="0.10" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_ka0)"/>
  <text x="680" y="234" text-anchor="middle" fill="#d8b4fe" font-size="11">④ 验证</text>
  <text x="400" y="286" text-anchor="middle" fill="#64748b" font-size="10">Topic 创建归运维 · 客户端版本不擅自引入 · 反序列化由用户指定</text>
</svg>
```

## 一、/kafka-toolkit 解决的是什么问题

### 1.1 裸写 Kafka 消费的五宗罪

Kafka 的客户端 API 给了你 `poll()` 和 `send()`，但**把消息消费做成可靠的事故预防**，封装层一个都不能少：

1. **自动提交 offset**：`enable.auto.commit=true` 是默认陷阱——**消息还没处理完，offset 已经提交了**。进程一挂，消息就丢了；
2. **重复消费无幂等**：offset 提交了但处理超时、消费者重启，消息会再发一次——没有幂等过滤，重复下单、重复扣款；
3. **失败无重试、无兜底**：业务异常不处理，消息要么一直卡在 poll 里，要么直接丢弃——既没有重试，也没有死信队列兜底；
4. **生产者发完不管**：`acks=0`/`acks=1` 或发送失败不记录——消息丢了都不知道；
5. **Lag 无人监控**：消费者挂了或慢如蜗牛，Lag 疯狂增长，业务发现时已经积压了几个小时。

`/kafka-toolkit` 生成 Kafka 消息中间件基础设施代码：消费者封装（幂等消费/重试/DLQ）、生产者封装（确认/重试）、Lag 监控。**禁止手动消费 Kafka 消息而不经过封装层**。

### 1.2 四个子命令，一条完整的防线

| 子命令 | 组件 | 检测条件 |
|--------|------|---------|
| `consumer` | ConsumerWrapper + 幂等过滤 | 出现 `@KafkaListener` / `consumer.poll()` |
| `producer` | ProducerWrapper | 出现 `kafkaTemplate.send()` / `producer.send()` |
| `dlq` | 死信队列 + 重试 Topic | 消费者无异常处理或 DLQ |
| `monitor` | Lag 监控 + 告警 | 无消费者监控指标 |
| `all`（默认） | 全部组件 | 首次引入 Kafka |

四个子命令构成一条完整防线：**consumer 保证"消费可靠"（幂等 + 重试），producer 保证"发送可靠"（确认 + 重试），dlq 保证"绝不丢消息"（兜底 + 可重投），monitor 保证"积压看得见"（Lag + 告警）**。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 260" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_ka1" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_ka1"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="260" fill="url(#bg_ka1)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">消息全生命周期防线</text>
  <rect x="40" y="50" width="160" height="70" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_ka1)"/>
  <text x="120" y="70" text-anchor="middle" fill="#93c5fd" font-size="11" font-weight="700">producer</text>
  <text x="120" y="96" text-anchor="middle" fill="#94a3b8" font-size="9">发送可靠</text>
  <text x="120" y="116" text-anchor="middle" fill="#64748b" font-size="8">acks=all · 失败落盘</text>
  <rect x="220" y="50" width="160" height="70" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_ka1)"/>
  <text x="300" y="70" text-anchor="middle" fill="#86efac" font-size="11" font-weight="700">consumer</text>
  <text x="300" y="96" text-anchor="middle" fill="#94a3b8" font-size="9">消费可靠</text>
  <text x="300" y="116" text-anchor="middle" fill="#64748b" font-size="8">手动提交 · 幂等过滤</text>
  <rect x="400" y="50" width="160" height="70" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_ka1)"/>
  <text x="480" y="70" text-anchor="middle" fill="#fde68a" font-size="11" font-weight="700">dlq</text>
  <text x="480" y="96" text-anchor="middle" fill="#94a3b8" font-size="9">绝不丢消息</text>
  <text x="480" y="116" text-anchor="middle" fill="#64748b" font-size="8">三次失败入死信</text>
  <rect x="580" y="50" width="180" height="70" rx="8" fill="#a855f7" opacity="0.12" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_ka1)"/>
  <text x="670" y="70" text-anchor="middle" fill="#d8b4fe" font-size="11" font-weight="700">monitor</text>
  <text x="670" y="96" text-anchor="middle" fill="#94a3b8" font-size="9">积压看得见</text>
  <text x="670" y="116" text-anchor="middle" fill="#64748b" font-size="8">Lag 指标 + 告警规则</text>
  <rect x="60" y="140" width="680" height="55" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_ka1)"/>
  <text x="400" y="160" text-anchor="middle" fill="#fde68a" font-size="11" font-weight="700">重试阶梯：首次失败 → retry-1（延迟 10s）→ retry-2（延迟 60s）→ DLQ</text>
  <text x="400" y="184" text-anchor="middle" fill="#94a3b8" font-size="9">重试有上限，上限之外有兜底——不会无限重试拖垮系统</text>
  <text x="400" y="240" text-anchor="middle" fill="#475569" font-size="10">每一环都防一类事故：丢消息、重复扣款、积压不发现</text>
</svg>
```

### 1.3 为什么"重试必须有上限，上限之外必须进 DLQ"

这是消息系统最微妙的地方：

- **无限重试**会拖垮消费线程、积压 Topic、拖慢所有消息——一条坏消息毁掉整条消费链路；
- **直接丢弃**等于悄悄丢钱——重试了几次还是失败的消息，往往是需要人工介入的（比如数据格式变了、下游接口坏了）；
- **DLQ 是两者之间的缓冲**：重试 N 次仍失败，进死信队列，业务可以稍后排查、修复、重新投递。**消息的终点不是"丢掉"，而是"留档可查"**。

### 1.4 为什么"禁止自动提交 offset"

`enable.auto.commit=true` 的意思是：**poll() 返回一批消息后，offset 就被提交了**。此时这批消息**还没被处理**——如果处理到一半进程崩溃，重启后这批消息不会再收到，数据就丢了。

正确姿势是**手动提交 offset**：处理完一批消息、落库成功后再 commit——"处理成功才提交，没处理完就不算数"。这是 at-least-once 语义的根基，配合幂等过滤（去重表/布隆过滤器/业务幂等），实现"**至少一次 + 幂等 = 恰好一次**"。

## 二、触发方式与前置

**触发方式**两种：

- 独立命令 `/kafka-toolkit [consumer|producer|dlq|monitor|all]`；
- 自动加载：coding-skill 阶段检测到 `Kafka` / `Consumer` / `Producer` / `@KafkaListener` 时。

**前置条件**：

1. 已识别技术栈（Java Spring Kafka / Go sarama-cluster / Python kafka-python 等）；
2. 已确定目标包路径（按语言规范）；
3. 已确定 Topic 名称和消息格式；
4. 用户未指定 → 生成全部组件（`all`）。

前置的用意：**封装层生成的是"按你的技术栈和消息格式定制的代码"**——不知道 Topic 长什么样、消息是什么格式，生成的封装就是空壳。

## 三、四步执行流程

### Step 1: 确定生成目标

按子命令或检测条件确定要生成哪些组件：

| 子命令 | 组件 | 检测条件 |
|--------|------|---------|
| `consumer` | ConsumerWrapper + 幂等过滤 | 出现 `@KafkaListener` / `consumer.poll()` |
| `producer` | ProducerWrapper | 出现 `kafkaTemplate.send()` / `producer.send()` |
| `dlq` | 死信队列 + 重试 Topic | 消费者无异常处理或 DLQ |
| `monitor` | Lag 监控 + 告警 | 无消费者监控指标 |
| `all`（默认） | 全部组件 | 首次引入 Kafka |

Step 1 是"按需裁剪"：已有消费者缺 DLQ？只生成 dlq。首次引入 Kafka？生成 all。**缺什么补什么；检测条件同时是体检**——裸 `@KafkaListener`、裸 `send()`、无监控指标，都会被照出来。

### Step 2: 生成代码文件

以 Java 示例，生成 `{basePackage}/kafka/` 包：

```
{basePackage}/kafka/
├── consumer/
│   ├── ConsumerWrapper.java         — 消费者封装（幂等过滤 + 重试 + DLQ）
│   ├── IdempotentConsumer.java      — 幂等消费处理器接口
│   └── RetryableConsumer.java       — 可重试消费者（重试 Topic 路由）
├── producer/
│   ├── ProducerWrapper.java         — 生产者封装（acks=all + 重试 + 回调）
│   └── TransactionalProducer.java   — 事务生产者（批量发送原子性）
├── dlq/
│   ├── DeadLetterQueue.java         — 死信队列处理
│   └── DqlMessage.java              — DLQ 消息结构
└── monitor/
    ├── LagMonitor.java              — Lag 监控（指标暴露）
    └── KafkaHealthIndicator.java    — 健康检查
```

**每个组件的关键实现约束**——本技能的灵魂就在这：

- **`ConsumerWrapper`**：手动提交 offset（处理完再提交），幂等过滤（去重表/布隆过滤器/业务幂等三选一），重试三次后入 DLQ——**处理成功才提交，没处理完就不算数**；
- **重试策略**：首次失败 → retry-1 Topic（延迟 10s），再次失败 → retry-2 Topic（延迟 60s），第三次 → DLQ——有上限、有阶梯、有兜底；
- **`ProducerWrapper`**：acks=all，retries=3，异步回调记录发送结果，发送失败记录到本地文件 + 定时重扫——**发不出去的消息落盘留证据，稍后重扫补救**；
- **`DeadLetterQueue`**：DLQ 消息结构包含 originalTopic / originalKey / originalValue / error / retryCount / failedAt / traceId——**死信可查因、可追溯、可重投**；
- **`LagMonitor`**：按 Topic×Partition 暴露 Lag 指标，对接 Prometheus/Grafana，告警规则：Lag>10000 → Warning，持续增长 5 分钟 → Critical——**积压到阈值立即报警，持续增长升级为严重**。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 270" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_ka2" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_ka2"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="270" fill="url(#bg_ka2)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">最关键的三个数字</text>
  <rect x="40" y="50" width="340" height="90" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_ka2)"/>
  <text x="210" y="70" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">消费：手动提交 offset</text>
  <text x="210" y="96" text-anchor="middle" fill="#94a3b8" font-size="9">处理成功才提交</text>
  <text x="210" y="116" text-anchor="middle" fill="#94a3b8" font-size="9">幂等三选一：去重表/布隆/业务幂等</text>
  <text x="210" y="136" text-anchor="middle" fill="#64748b" font-size="8">禁 enable.auto.commit=true</text>
  <rect x="420" y="50" width="340" height="90" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_ka2)"/>
  <text x="590" y="70" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">发送：acks=all + retries=3</text>
  <text x="590" y="96" text-anchor="middle" fill="#94a3b8" font-size="9">异步回调记录结果</text>
  <text x="590" y="116" text-anchor="middle" fill="#94a3b8" font-size="9">失败落盘 + 定时重扫</text>
  <text x="590" y="136" text-anchor="middle" fill="#64748b" font-size="8">禁 acks=0 / acks=1</text>
  <rect x="40" y="160" width="340" height="90" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_ka2)"/>
  <text x="210" y="180" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">重试：10s → 60s → DLQ</text>
  <text x="210" y="206" text-anchor="middle" fill="#94a3b8" font-size="9">首次失败 retry-1 延迟 10s</text>
  <text x="210" y="226" text-anchor="middle" fill="#94a3b8" font-size="9">二次失败 retry-2 延迟 60s</text>
  <text x="210" y="246" text-anchor="middle" fill="#64748b" font-size="8">第三次进 DLQ（有上限！）</text>
  <rect x="420" y="160" width="340" height="90" rx="8" fill="#a855f7" opacity="0.12" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_ka2)"/>
  <text x="590" y="180" text-anchor="middle" fill="#d8b4fe" font-size="12" font-weight="700">监控：Lag>10000 告警</text>
  <text x="590" y="206" text-anchor="middle" fill="#94a3b8" font-size="9">按 Topic×Partition 暴露</text>
  <text x="590" y="226" text-anchor="middle" fill="#94a3b8" font-size="9">对接 Prometheus/Grafana</text>
  <text x="590" y="246" text-anchor="middle" fill="#64748b" font-size="8">持续增长 5 分钟 → Critical</text>
</svg>
```

### Step 3: 生成测试

每个核心组件配测试，测试要求紧扣实现约束（用 Mock/Embedded Kafka）：

- **消费者**：验证幂等过滤（重复消息被跳过）、重试三次后入 DLQ、手动 offset 提交；
- **生产者**：验证发送确认、重试机制、回调记录；
- **DLQ**：验证消息结构完整、重新投递到原 Topic；
- **监控**：验证 Lag 指标暴露格式。

测试的作用是把约束**钉死**：手动提交、幂等过滤、重试上限、结构完整性——这些红线不靠 review 保证，靠测试保证。

### Step 4: 验证

1. 编译通过 + 测试通过；
2. 检查清单逐项确认：
   - [ ] 所有消费者经过 `ConsumerWrapper` 封装（非裸 `@KafkaListener`）
   - [ ] 所有生产者经过 `ProducerWrapper` 封装
   - [ ] 手动提交 offset（非自动提交）
   - [ ] 重试有上限（非无限重试）
   - [ ] DLQ 消息结构完整
   - [ ] Lag 监控已暴露

六条清单对应六道红线复查：**封装、手动提交、有上限、有兜底、可追溯、看得见**——六项全过，Kafka 基础设施才算合格。

## 四、质量门禁：哪些必须做，哪些绝对不能做

**✅ 必须做**：

- 编译通过 + 测试通过；
- 所有消费者经过封装层（非裸 `@KafkaListener`）；
- 手动提交 offset（非 `enable.auto.commit=true`）；
- 重试有上限（失败后进 DLQ）；
- 生产者 acks=all；
- Lag 监控已对接指标系统。

**❌ 绝对不能做**：

- 禁止自动提交 offset（会丢消息）；
- 禁止无限重试（无 DLQ，会拖垮消费链路）；
- 禁止生产者 acks=0 或 acks=1（消息可能丢）。

门禁的逻辑是"**可靠性三连**"：

- **消费端**：手动提交 + 幂等，保证 at-least-once 不丢、不重；
- **生产端**：acks=all + 落盘重扫，保证消息发出有确认、失败有证据；
- **链路端**：有上限的重试 + DLQ 兜底，保证坏消息不拖垮全链路、不悄悄消失。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 240" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_ka3" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_ka3"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="240" fill="url(#bg_ka3)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">可靠性三连</text>
  <rect x="40" y="50" width="340" height="70" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_ka3)"/>
  <text x="210" y="72" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">消费端：不丢、不重</text>
  <text x="210" y="98" text-anchor="middle" fill="#64748b" font-size="9">手动提交 + 幂等过滤</text>
  <rect x="420" y="50" width="340" height="70" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_ka3)"/>
  <text x="590" y="72" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">生产端：有确认、有证据</text>
  <text x="590" y="98" text-anchor="middle" fill="#64748b" font-size="9">acks=all + 失败落盘重扫</text>
  <rect x="40" y="140" width="340" height="70" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_ka3)"/>
  <text x="210" y="162" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">链路端：有上限、有兜底</text>
  <text x="210" y="188" text-anchor="middle" fill="#64748b" font-size="9">10s → 60s → DLQ</text>
  <rect x="420" y="140" width="340" height="70" rx="8" fill="#a855f7" opacity="0.12" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_ka3)"/>
  <text x="590" y="162" text-anchor="middle" fill="#d8b4fe" font-size="12" font-weight="700">可观测：积压看得见</text>
  <text x="590" y="188" text-anchor="middle" fill="#64748b" font-size="9">Lag 指标 + 告警规则</text>
  <text x="400" y="240" text-anchor="middle" fill="#475569" font-size="10">消息的终点不是"丢掉"，而是"留档可查"</text>
</svg>
```

## 五、四条约束：边界在哪里

- **不生成 Topic 创建脚本**——Topic 由运维或 IaC 管理；
- **不修改已有的消费者/生产者代码**——只新增封装层，原代码重构到封装层——动了别人的消费逻辑，等于动别人的业务；
- **不引入未确认的 Kafka 客户端版本**——项目中已有的优先复用；
- **不擅自决定反序列化方式**（AVRO/JSON/Protobuf）——由用户指定——消息格式是契约，AI 不能替用户拍板。

边界一句话：**本技能管"消息怎么可靠地收发"，不碰"Topic 怎么建、格式怎么定"**。

## 六、与相邻技能的分工

| 场景 | 归属 |
|------|------|
| 消息消费/发送封装 | **本技能**（`/kafka-toolkit`） |
| 事务消息 / 顺序消息 | `rocketmq-toolkit`（RocketMQ 场景） |
| 消息链路追踪 | `logging-toolkit`（traceId 落日志规范） |

- **本技能 vs rocketmq-toolkit**：两者都是消息中间件封装，但目标系统不同——Kafka 主打高吞吐日志流，RocketMQ 主打事务消息/顺序消息；封装纪律一致（都禁止裸用客户端），API 按各自 SDK 生成；
- **本技能 vs logging-toolkit**：kafka-toolkit 的 DLQ 记录 traceId，logging-toolkit 负责 traceId 的注入与日志规范——一个在消息里传，一个在日志里记。

## 七、完成标志

`/kafka-toolkit` 的完成标志有三个：

1. **目标组件已生成**：consumer/producer/dlq/monitor 按需产出——该有的都有；
2. **测试通过 + 检查清单全过**：六条红线逐项确认——不是"写了"，是"验证了"；
3. **业务代码不再裸用 Kafka 客户端**：所有消费/发送经过封装层（或已给出迁移建议）——纪律已生效。

三个标志对应三问：**补齐了吗**（组件）？**钉死了吗**（测试+清单）？**生效了吗**（不再裸用）？——三个都答"是"，消息基础设施才算真正落地。

## 八、写在最后

`/kafka-toolkit` 的全部设计，浓缩成四句话：

1. **处理成功才提交 offset**——自动提交 = 消息还没处理就丢掉。
2. **重试必须有上限，上限之外进 DLQ**——**禁止无限重试**，一条坏消息不能拖垮整条链路。
3. **acks=all，失败落盘重扫**——消息发出要有确认，失败要有证据。
4. **Lag 超过阈值必须报警**——积压不可怕，看不到积压才可怕。

一句话记住它：**/kafka-toolkit 是 Kafka 消息链路的"可靠性四件套"——它生成手动提交+幂等过滤的消费者、acks=all+落盘重扫的生产者、结构完整可重投的 DLQ、对接指标系统的 Lag 监控，用"不丢、不重、有上限、看得见"四条纪律，让每一条消息都有始有终、有据可查。**