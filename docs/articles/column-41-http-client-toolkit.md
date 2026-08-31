# /http-client-toolkit：HTTP 客户端封装——重试只给幂等请求，熔断必须有半开

> 命令深度拆解 · 第 41 篇 · 约 8000 字 · 7 个 SVG 图

---

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 300" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_h0" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_h0"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="300" fill="url(#bg_h0)" rx="10"/>
  <text x="400" y="28" text-anchor="middle" fill="#e2e8f0" font-size="26" font-weight="700">/http-client-toolkit：HTTP 客户端五件套</text>
  <rect x="40" y="55" width="140" height="90" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_h0)"/>
  <text x="110" y="80" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">pool</text>
  <text x="110" y="104" text-anchor="middle" fill="#64748b" font-size="9">连接池管理</text>
  <text x="110" y="126" text-anchor="middle" fill="#64748b" font-size="8">200 连接 / 超时三件套</text>
  <rect x="200" y="55" width="140" height="90" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_h0)"/>
  <text x="270" y="80" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">retry</text>
  <text x="270" y="104" text-anchor="middle" fill="#64748b" font-size="9">指数退避重试</text>
  <text x="270" y="126" text-anchor="middle" fill="#64748b" font-size="8">仅幂等请求重试</text>
  <rect x="360" y="55" width="140" height="90" rx="8" fill="#ef4444" opacity="0.12" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_h0)"/>
  <text x="430" y="80" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">circuit</text>
  <text x="430" y="104" text-anchor="middle" fill="#64748b" font-size="9">熔断降级</text>
  <text x="430" y="126" text-anchor="middle" fill="#64748b" font-size="8">滑动窗口 + 半开试探</text>
  <rect x="520" y="55" width="140" height="90" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_h0)"/>
  <text x="590" y="80" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">loadbalance</text>
  <text x="590" y="104" text-anchor="middle" fill="#64748b" font-size="9">负载均衡</text>
  <text x="590" y="126" text-anchor="middle" fill="#64748b" font-size="8">轮询 / 加权 / 服务发现</text>
  <rect x="680" y="55" width="100" height="90" rx="8" fill="#a855f7" opacity="0.12" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_h0)"/>
  <text x="730" y="80" text-anchor="middle" fill="#d8b4fe" font-size="12" font-weight="700">trace</text>
  <text x="730" y="104" text-anchor="middle" fill="#64748b" font-size="9">traceId 透传</text>
  <text x="730" y="126" text-anchor="middle" fill="#64748b" font-size="8">X-Trace-Id 自动注入</text>
  <text x="400" y="185" text-anchor="middle" fill="#475569" font-size="13">铁律：禁止在业务代码中直接创建 HTTP 客户端而不经过封装层</text>
  <rect x="60" y="205" width="160" height="45" rx="8" fill="#3b82f6" opacity="0.10" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_h0)"/>
  <text x="140" y="234" text-anchor="middle" fill="#93c5fd" font-size="11">① 确定目标</text>
  <rect x="240" y="205" width="160" height="45" rx="8" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_h0)"/>
  <text x="320" y="234" text-anchor="middle" fill="#86efac" font-size="11">② 生成代码</text>
  <rect x="420" y="205" width="160" height="45" rx="8" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_h0)"/>
  <text x="500" y="234" text-anchor="middle" fill="#fde68a" font-size="11">③ 生成测试</text>
  <rect x="600" y="205" width="160" height="45" rx="8" fill="#a855f7" opacity="0.10" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_h0)"/>
  <text x="680" y="234" text-anchor="middle" fill="#d8b4fe" font-size="11">④ 验证</text>
  <text x="400" y="280" text-anchor="middle" fill="#64748b" font-size="10">重试仅幂等 · 熔断有半开 · traceId 自动透传 · 超时三件套全配</text>
</svg>
```

## 一、/http-client-toolkit 解决的是什么问题

### 1.1 裸 HTTP 客户端的五个隐患

每个微服务都在调别人，但大多数团队直接 `new RestTemplate()` / `requests.get()` 就用——这背后藏着五个隐患：

1. **连接失控**：没连接池或默认配置，每请求建连/断连，高并发下端口耗尽、TIME_WAIT 堆积；
2. **超时缺失**：只配了 connectTimeout 没配 readTimeout——下游慢响应能挂住你的线程 30 秒；
3. **重试盲目**：下游抖动就重试——POST 重试可能造成重复下单；
4. **熔断缺失**：下游挂了，你的服务也跟着挂——没有熔断就是雪崩；
5. **链路断裂**：traceId 不传递，出了事故跨服务查不了日志。

`/http-client-toolkit` 生成 HTTP 客户端基础设施：连接池管理、超时配置（connect/read/write）、重试策略（指数退避+最大次数）、熔断降级、负载均衡、traceId 透传。**禁止在业务代码中直接创建 HTTP 客户端而不经过封装层**。

### 1.2 五个子命令，从连接到链路

| 子命令 | 组件 | 用在什么场景 |
|--------|------|-------------|
| `pool` | 连接池配置 | 无连接池或使用默认配置 |
| `retry` | 重试策略 | 外部调用无重试 |
| `circuit` | 熔断降级 | 外部调用无熔断保护 |
| `loadbalance` | 负载均衡 | 多实例调用无负载均衡 |
| `all`（默认） | 全部组件 | 首次引入 HTTP 客户端 |

层层递进的关系：**pool 管'连得上'，retry 管'抖得稳'，circuit 管'挂得住'，loadbalance 管'分得匀'，trace 管'查得清'**——五个组件合起来，才是一个生产级的 HTTP 客户端。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 250" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_h1" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_h1"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="250" fill="url(#bg_h1)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">五个组件的五层防线</text>
  <rect x="40" y="50" width="130" height="120" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_h1)"/>
  <text x="105" y="76" text-anchor="middle" fill="#93c5fd" font-size="11" font-weight="700">pool</text>
  <text x="105" y="100" text-anchor="middle" fill="#94a3b8" font-size="9">连得上</text>
  <text x="105" y="122" text-anchor="middle" fill="#64748b" font-size="8">连接复用</text>
  <text x="105" y="140" text-anchor="middle" fill="#64748b" font-size="8">空闲回收</text>
  <text x="105" y="158" text-anchor="middle" fill="#64748b" font-size="8">超时三件套</text>
  <rect x="190" y="50" width="130" height="120" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_h1)"/>
  <text x="255" y="76" text-anchor="middle" fill="#86efac" font-size="11" font-weight="700">retry</text>
  <text x="255" y="100" text-anchor="middle" fill="#94a3b8" font-size="9">抖得稳</text>
  <text x="255" y="122" text-anchor="middle" fill="#64748b" font-size="8">指数退避</text>
  <text x="255" y="140" text-anchor="middle" fill="#64748b" font-size="8">仅幂等重试</text>
  <text x="255" y="158" text-anchor="middle" fill="#64748b" font-size="8">最大次数上限</text>
  <rect x="340" y="50" width="130" height="120" rx="8" fill="#ef4444" opacity="0.12" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_h1)"/>
  <text x="405" y="76" text-anchor="middle" fill="#fca5a5" font-size="11" font-weight="700">circuit</text>
  <text x="405" y="100" text-anchor="middle" fill="#94a3b8" font-size="9">挂得住</text>
  <text x="405" y="122" text-anchor="middle" fill="#64748b" font-size="8">滑动窗口统计</text>
  <text x="405" y="140" text-anchor="middle" fill="#64748b" font-size="8">半开试探恢复</text>
  <rect x="490" y="50" width="130" height="120" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_h1)"/>
  <text x="555" y="76" text-anchor="middle" fill="#fde68a" font-size="11" font-weight="700">loadbalance</text>
  <text x="555" y="100" text-anchor="middle" fill="#94a3b8" font-size="9">分得匀</text>
  <text x="555" y="122" text-anchor="middle" fill="#64748b" font-size="8">轮询 / 加权</text>
  <text x="555" y="140" text-anchor="middle" fill="#64748b" font-size="8">服务发现</text>
  <rect x="640" y="50" width="120" height="120" rx="8" fill="#a855f7" opacity="0.12" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_h1)"/>
  <text x="700" y="76" text-anchor="middle" fill="#d8b4fe" font-size="11" font-weight="700">trace</text>
  <text x="700" y="100" text-anchor="middle" fill="#94a3b8" font-size="9">查得清</text>
  <text x="700" y="122" text-anchor="middle" fill="#64748b" font-size="8">traceId 注入</text>
  <text x="700" y="140" text-anchor="middle" fill="#64748b" font-size="8">下游透传</text>
  <text x="400" y="220" text-anchor="middle" fill="#475569" font-size="10">五层防线连起来，才是生产级 HTTP 客户端的完整形态</text>
</svg>
```

### 1.3 与 trace 相关技能的边界

- `http-client-toolkit` 的 trace 管**调用侧的 traceId 透传**——请求带上 `X-Trace-Id`，下游响应带回下游的 traceId 记日志；
- `logging-toolkit` 管**日志规范**——traceId 从 MDC 提取、日志格式统一；
- 两者配合：**logging 定义 traceId 怎么落到日志，http-client 定义 traceId 怎么跨服务传**。

## 二、触发方式与前置

**触发方式**两种：

- 独立命令 `/http-client-toolkit [pool|retry|circuit|loadbalance|all]`；
- 自动加载：coding-skill 阶段检测到 `HttpClient` / `RestTemplate` / `OkHttp` / `httpx` / `requests` / `net/http` 时——看到裸 HTTP 调用，自动补封装。

**前置条件**：

1. 已识别技术栈（Java RestTemplate/OkHttp/WebClient / Python httpx/requests / Go net/http/resty）；
2. 已确定目标包路径（按语言规范）；
3. 已确定下游服务列表（至少一个 URL 或服务名）——连调谁都不知道，客户端封装就是空中楼阁；
4. 用户未指定 → 生成全部组件（`all`）。

## 三、四步执行流程

### Step 1: 确定生成目标

按子命令或检测条件确定要生成哪些组件：

| 子命令 | 组件 | 检测条件 |
|--------|------|--------|
| `pool` | 连接池配置 | 无连接池或使用默认配置 |
| `retry` | 重试策略 | 外部调用无重试 |
| `circuit` | 熔断降级 | 外部调用无熔断保护 |
| `loadbalance` | 负载均衡 | 多实例调用无负载均衡 |
| `all`（默认） | 全部组件 | 首次引入 HTTP 客户端 |

Step 1 是"按需裁剪"：已有连接池只缺重试？生成 retry。首次引入？生成 all。**缺什么补什么，不重复造轮子**。

### Step 2: 生成代码文件

以 Java 示例，生成 `{basePackage}/http/` 包：

```
{basePackage}/http/
├── client/      HttpClientFactory / TraceIdInterceptor / LoggingInterceptor
├── retry/       RetryStrategy / ExponentialBackoffRetry / RetryableHttpClient
├── circuit/     CircuitBreaker / SlidingWindowBreaker / FallbackHandler
├── loadbalance/ LoadBalancer / RoundRobinBalancer / WeightedBalancer / ServiceDiscovery
└── config/      HttpClientConfig（连接池/超时/重试）
```

**每个组件的关键实现约束**——本技能的灵魂就在这：

- **`HttpClientFactory`**：连接池最大连接数 200，每条路由最大 50，keep-alive 存活时间 60s，空闲连接回收 30s——连接池是性能的底座，数字不是随便定的；
- **超时三件套**：connectTimeout=5s（建立连接）、readTimeout=10s（等待响应）、writeTimeout=10s（发送数据），全部可配置——**三个超时必须全配**，只配 connect 是常见事故源；
- **`RetryStrategy`**：指数退避（base=1s，multiplier=2，maxRetries=3，maxDelay=10s），**仅对 GET/幂等请求重试**，POST/PUT/DELETE 不自动重试——重试非幂等 = 重复下单；
- **`CircuitBreaker`**：滑动窗口统计最近 100 次请求，失败率 > 50% 熔断 10s，半开后允许 3 次试探请求，成功则关闭——**熔断必须有半开**，否则熔断后永不恢复；
- **`TraceIdInterceptor`**：从 MDC 上下文提取 traceId，注入请求头 `X-Trace-Id`，响应中提取下游 traceId 记录日志——traceId 自动透传，不在业务代码里手写；
- **`LoggingInterceptor`**：记录请求方法/URL/耗时/状态码，响应体大于 10KB 截断，**不记录敏感字段**（Authorization/Token）——日志要全，但不能泄密。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 260" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_h2" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_h2"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="260" fill="url(#bg_h2)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">两组最容易记错的数字</text>
  <rect x="40" y="50" width="340" height="95" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_h2)"/>
  <text x="210" y="72" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">超时三件套（全配！）</text>
  <text x="210" y="96" text-anchor="middle" fill="#94a3b8" font-size="9">connect = 5s（建连）</text>
  <text x="210" y="116" text-anchor="middle" fill="#94a3b8" font-size="9">read = 10s（等响应）</text>
  <text x="210" y="136" text-anchor="middle" fill="#94a3b8" font-size="9">write = 10s（发数据）</text>
  <rect x="420" y="50" width="340" height="95" rx="8" fill="#ef4444" opacity="0.12" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_h2)"/>
  <text x="590" y="72" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">重试红线（幂等！）</text>
  <text x="590" y="96" text-anchor="middle" fill="#94a3b8" font-size="9">base=1s ×2 退避，最多 3 次</text>
  <text x="590" y="116" text-anchor="middle" fill="#94a3b8" font-size="9">最大延迟 10s</text>
  <text x="590" y="136" text-anchor="middle" fill="#94a3b8" font-size="9">仅 GET / 幂等请求重试</text>
  <text x="400" y="170" text-anchor="middle" fill="#ef4444" font-size="11" font-weight="700">POST/PUT/DELETE 不自动重试</text>
  <rect x="120" y="185" width="560" height="45" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_h2)"/>
  <text x="400" y="215" text-anchor="middle" fill="#fde68a" font-size="10">重试非幂等请求 = 用户付一次钱，你扣两次</text>
</svg>
```

### Step 3: 生成测试

每个核心组件配测试，测试要求紧扣实现约束（用 Mock HTTP 服务器测试）：

- **连接池**：验证连接池配置生效、连接复用、空闲回收；
- **重试**：验证指数退避时间间隔、最大重试次数、**非幂等请求不重试**（最容易逃逸的约束，必须用测试钉死）；
- **熔断**：验证滑动窗口统计、熔断状态切换、**半开试探逻辑**；
- **负载均衡**：验证轮询/加权分发、空实例列表处理。

### Step 4: 验证

1. 编译通过 + 测试通过；
2. 检查清单逐项确认：
   - [ ] 所有 HTTP 调用经过封装层（非直接 `new HttpClient()`）
   - [ ] 超时三件套全部配置（非只配了 connectTimeout）
   - [ ] 重试仅对幂等请求生效
   - [ ] 熔断器有半开试探机制（非熔断后永不恢复）
   - [ ] traceId 自动透传（非手动在业务代码中设置请求头）

这五条检查清单对应五个"红线复查"：**封装（无裸 new）、超时全配、重试幂等、熔断半开、trace 自动**——五项全过，HTTP 客户端才算合格。

## 四、质量门禁：哪些必须做，哪些绝对不能做

**✅ 必须做**：

- 所有 HTTP 调用经过封装层；
- 超时三件套全部配置；
- 重试仅对幂等请求生效；
- 熔断器有半开试探机制；
- traceId 自动透传。

**❌ 绝对不能做**：

- 禁止直接 `new HttpClient()` / `new RestTemplate()`（应经过封装层）；
- 禁止对非幂等请求自动重试（会重复扣款/重复下单）；
- 禁止无界重试（无限重试会拖垮下游和自己）；
- 禁止熔断后无半开试探（熔断后永不恢复，服务永久降级）；
- 禁止日志记录敏感字段（Authorization/Token）。

门禁的逻辑是"**调用别人，先护住自己**"：

- 重试幂等是钱的问题——重复扣款是安全事故；
- 熔断半开是恢复力的问题——没有半开，服务降级了就永远降级下去；
- 日志脱敏是合规的问题——token 进日志等于密码贴墙上；
- 超时全配是可用性的问题——没配 readTimeout，下游慢挂住你的线程。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 250" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_h3" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_h3"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="250" fill="url(#bg_h3)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">装配越全，防护越稳</text>
  <rect x="40" y="50" width="340" height="70" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_h3)"/>
  <text x="210" y="72" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">重试幂等 = 钱的问题</text>
  <text x="210" y="98" text-anchor="middle" fill="#64748b" font-size="9">POST 重试 = 重复下单/扣款</text>
  <rect x="420" y="50" width="340" height="70" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_h3)"/>
  <text x="590" y="72" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">熔断半开 = 恢复力的问题</text>
  <text x="590" y="98" text-anchor="middle" fill="#64748b" font-size="9">无半开 = 降级就永远降级</text>
  <rect x="40" y="140" width="340" height="70" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_h3)"/>
  <text x="210" y="162" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">日志脱敏 = 合规的问题</text>
  <text x="210" y="188" text-anchor="middle" fill="#64748b" font-size="9">token 进日志 = 密码贴墙上</text>
  <rect x="420" y="140" width="340" height="70" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_h3)"/>
  <text x="590" y="162" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">超时全配 = 可用性的问题</text>
  <text x="590" y="188" text-anchor="middle" fill="#64748b" font-size="9">只配 connect = 下游慢时线程挂死</text>
  <text x="400" y="240" text-anchor="middle" fill="#475569" font-size="10">调用别人之前，先把自己护住</text>
</svg>
```

## 五、四条约束：边界在哪里

- **不实现具体业务 API 调用**——封装层管"请求怎么发"，业务 API 的路径/参数/header 由业务侧定义——AI 不知道你下游服务的接口契约；
- **不引入未确认的 HTTP 库版本**——库选型是架构决策，得先确认；
- **不修改已有的 HTTP 调用代码**——只新增封装层，建议用户逐步迁移；
- **不生成 WebSocket/gRPC 客户端**——那是别的协议的活。

边界一句话：**本技能管"HTTP 调用怎么防",不碰"调的是什么 API"**。

## 六、与相邻技能的分工

| 场景 | 归属 |
|------|------|
| HTTP 客户端封装 | **本技能**（`/http-client-toolkit`） |
| 远程服务日志追踪 | `logging-toolkit`（traceId 落日志规范） |
| API 联调契约 | `harness-api-mock`（Mock 服务） |

- **本技能 vs logging-toolkit**：http-client 管 traceId 怎么传，logging 管 traceId 怎么记——一个跨服务，一个在本地；
- **本技能 vs harness-api-mock**：开发期 Mock 服务模拟下游，生产期 http-client 安全调用下游——一个搭假的下游，一个防真的下游。

## 七、完成标志

`/http-client-toolkit` 的完成标志有三个：

1. **目标组件已生成**：连接池/超时/重试/熔断/负载均衡/trace 按需产出——该有的都有；
2. **测试通过 + 检查清单全过**：封装/超时全配/重试幂等/熔断半开/trace 自动五项全过——不是"写了"，是"验证了"；
3. **业务代码不再直接创建 HTTP 客户端**：所有调用经过封装层（或已给出迁移建议）——纪律已生效。

三个标志对应三问：**补齐了吗**（组件）？**钉死了吗**（测试+清单）？**生效了吗**（不再裸 new）？——三个都答"是"，HTTP 基础设施才算真正落地。

## 八、写在最后

`/http-client-toolkit` 的全部设计，浓缩成四句话：

1. **调用别人之前，先护住自己**——连接池、超时、重试、熔断、trace，一层都不能少。
2. **重试只给幂等请求**——POST 重试就是重复扣款，这是钱的事。
3. **熔断必须有半开**——没有半开，降级就是永久降级。
4. **traceId 自动透传，日志自动脱敏**——链路易查，机密不泄。

一句话记住它：**/http-client-toolkit 是服务间调用的"防护甲"——它生成连接池、超时、重试、熔断、负载均衡、traceId 透传六件套封装层，用"重试仅幂等、熔断必半开、trace 自动传"三条纪律，让每一笔外部调用都稳得住、死不了、查得清。**