# /redis-cache-wrapper：多级缓存四防——穿透、击穿、雪崩、热 key，分布式锁也包了

> 命令深度拆解 · 第 47 篇 · 约 8000 字 · 6 个 SVG 图

---

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 300" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_r0" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_r0"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="300" fill="url(#bg_r0)" rx="10"/>
  <text x="400" y="28" text-anchor="middle" fill="#e2e8f0" font-size="26" font-weight="700">/redis-cache-wrapper：多级缓存 + 分布式锁</text>
  <rect x="40" y="55" width="150" height="95" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_r0)"/>
  <text x="115" y="80" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">cache</text>
  <text x="115" y="104" text-anchor="middle" fill="#64748b" font-size="9">多级缓存 L1→L2→L3</text>
  <text x="115" y="126" text-anchor="middle" fill="#64748b" font-size="8">穿透/击穿/雪崩防护</text>
  <rect x="240" y="55" width="150" height="95" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_r0)"/>
  <text x="315" y="80" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">lock</text>
  <text x="315" y="104" text-anchor="middle" fill="#64748b" font-size="9">分布式锁</text>
  <text x="315" y="126" text-anchor="middle" fill="#64748b" font-size="8">SETNX + Lua + 看门狗</text>
  <rect x="440" y="55" width="150" height="95" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_r0)"/>
  <text x="515" y="80" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">热 key 探测</text>
  <text x="515" y="104" text-anchor="middle" fill="#64748b" font-size="9">滑动窗口频率统计</text>
  <text x="515" y="126" text-anchor="middle" fill="#64748b" font-size="8">超阈值自动降级 L1</text>
  <rect x="640" y="55" width="130" height="95" rx="8" fill="#a855f7" opacity="0.12" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_r0)"/>
  <text x="705" y="80" text-anchor="middle" fill="#d8b4fe" font-size="12" font-weight="700">布隆过滤</text>
  <text x="705" y="104" text-anchor="middle" fill="#64748b" font-size="9">穿透前置拦截</text>
  <text x="705" y="126" text-anchor="middle" fill="#64748b" font-size="8">配置容量不硬编</text>
  <text x="400" y="185" text-anchor="middle" fill="#475569" font-size="13">铁律：禁止直接操作 Redis 客户端（get/set）而不经过缓存服务层</text>
  <rect x="60" y="205" width="160" height="45" rx="8" fill="#3b82f6" opacity="0.10" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_r0)"/>
  <text x="140" y="234" text-anchor="middle" fill="#93c5fd" font-size="11">① 确定生成目标</text>
  <rect x="240" y="205" width="160" height="45" rx="8" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_r0)"/>
  <text x="320" y="234" text-anchor="middle" fill="#86efac" font-size="11">② 生成代码</text>
  <rect x="420" y="205" width="160" height="45" rx="8" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_r0)"/>
  <text x="500" y="234" text-anchor="middle" fill="#fde68a" font-size="11">③ 测试钉死</text>
  <rect x="600" y="205" width="160" height="45" rx="8" fill="#a855f7" opacity="0.10" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_r0)"/>
  <text x="680" y="234" text-anchor="middle" fill="#d8b4fe" font-size="11">④ 验证</text>
  <text x="400" y="286" text-anchor="middle" fill="#64748b" font-size="10">缓存是加速器，防的是三种最常见的缓存杀手事故</text>
</svg>
```

## 一、/redis-cache-wrapper 解决的是什么问题

### 1.1 缓存的三座大山 + 一个隐雷

裸用 Redis `get/set` 做缓存，业务上几乎必然撞上三类事故——加上一个热 key 隐雷：

1. **缓存穿透**：请求的 key 在 DB 里也不存在——缓存查不到、DB 也查不到，每次请求都打到 DB，恶意刷一个不存在的 ID，DB 直接被打穿；
2. **缓存击穿**：某个热点 key 过期瞬间，大量请求同时穿透——所有请求一起冲 DB，DB 瞬间过载；
3. **缓存雪崩**：大量 key 同一时间过期——一批请求同时穿透，DB 压力骤增；
4. **热 key**：某个 key 访问量巨大，单 Redis 节点扛不住，反而成为瓶颈。

`/redis-cache-wrapper` 为项目生成多级缓存基础设施代码：缓存服务（L1 本地 + L2 Redis + L3 DB）、分布式锁、热 key 探测。**禁止直接操作 Redis 客户端（get/set）而不经过缓存服务层**。

### 1.2 核心三组件

| 子命令 | 组件 | 生成文件数 |
|--------|------|-----------|
| `cache` | MultiLevelCacheService | 3~5 个文件 |
| `lock` | DistributedLock | 2~3 个文件 |
| `all`（默认） | 全部组件 | 5~8 个文件 |

三个组件对应三个战场：**CacheService 管"数据怎么缓存"，DistributedLock 管"并发怎么互斥"，HotKeyDetector 管"热点怎么降级"**。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 260" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_r1" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_r1"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="260" fill="url(#bg_r1)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">三座大山 + 一个隐雷</text>
  <rect x="40" y="50" width="340" height="70" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_r1)"/>
  <text x="210" y="72" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">穿透：DB 里也没有的 key</text>
  <text x="210" y="98" text-anchor="middle" fill="#94a3b8" font-size="9">空值缓存 30s + 布隆过滤器前置拦截</text>
  <rect x="420" y="50" width="340" height="70" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_r1)"/>
  <text x="590" y="72" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">击穿：热点 key 过期瞬间</text>
  <text x="590" y="98" text-anchor="middle" fill="#94a3b8" font-size="9">SETNX 互斥锁 · 永不过期+异步刷新</text>
  <rect x="40" y="140" width="340" height="70" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_r1)"/>
  <text x="210" y="162" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">雪崩：大量 key 同时过期</text>
  <text x="210" y="188" text-anchor="middle" fill="#94a3b8" font-size="9">TTL 随机偏移 · L1 不依赖 Redis · 熔断降级</text>
  <rect x="420" y="140" width="340" height="70" rx="8" fill="#ef4444" opacity="0.12" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_r1)"/>
  <text x="590" y="162" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">热 key：单个 key 访问爆炸</text>
  <text x="590" y="188" text-anchor="middle" fill="#94a3b8" font-size="9">滑动窗口统计 · 超阈值自动降级到 L1</text>
  <text x="400" y="242" text-anchor="middle" fill="#475569" font-size="10">防的是事故，省的是 DB，保的是稳定</text>
</svg>
```

### 1.3 为什么"缓存必须经过 CacheService"

缓存的三大防线（防穿透、防击穿、防雪崩）**不是 get/set 两个方法能表达的**：

- 穿透防线要求"查 DB 前先查布隆、查不到缓存空值"——裸 get/set 做不到；
- 击穿防线要求"过期瞬间互斥重建"——裸 get/set 做不到；
- 雪崩防线要求"TTL 随机偏移、L1 兜底"——裸 get/set 做不到。

所以业务代码里出现直接 `redis.get(key)`、`redis.set(key, value)`，等于绕过了全部防线——**红线就设在封装层：所有缓存操作经过 `CacheService`**。

## 二、触发方式与前置

**触发方式**两种：

- 独立命令 `/redis-cache-wrapper [cache|lock|all]`；
- 自动加载：coding-skill 阶段检测到 `Redis` / `Memcached` / 缓存操作代码时。

**前置条件**：

1. 已识别技术栈（Java/Python/Go 等）；
2. 已确定目标包路径（按语言规范：Java `{basePackage}.cache`，Python `{app}/cache/`，Go `internal/cache/`）；
3. 已确定缓存中间件类型（Redis/Memcached/本地缓存）；
4. 用户未指定 → 生成全部组件（`all`）。

前置的用意：**多级缓存封装生成的是"按你的缓存中间件的代码"**——不知道用 Redis 还是 Memcached、不知道包路径，生成的封装连配置都写不对。

## 三、四步执行流程

### Step 1: 确定生成目标

按子命令或检测条件确定生成范围：

| 子命令 | 组件 | 生成文件数 |
|--------|------|-----------|
| `cache` | MultiLevelCacheService | 3~5 个文件 |
| `lock` | DistributedLock | 2~3 个文件 |
| `all`（默认） | 全部组件 | 5~8 个文件 |

Step 1 是"按需裁剪"：只要缓存不要锁？生成 cache。只要分布式锁？生成 lock。首次引入？生成 all。**缺什么补什么，检测条件同时是体检**——裸 Redis get/set、无锁保护，都会被照出来。

### Step 2: 生成代码文件

以 Java 示例，生成 `{basePackage}/cache/` 包：

```
{basePackage}/cache/
├── CacheService.java           — 缓存服务接口（get/set/delete/getWithLoader）
├── MultiLevelCacheService.java — 多级缓存实现（L1→L2→L3 逐级穿透）
├── BloomFilter.java            — 布隆过滤器（穿透防护）
├── DistributedLock.java        — 分布式锁接口
├── RedisDistributedLock.java   — Redis 分布式锁实现
├── HotKeyDetector.java         — 热 key 探测（本地频率统计 + 自动降级）
└── CacheConfig.java            — 缓存配置（TTL/L1 上限/布隆过滤器容量）
```

**每个组件的关键实现约束**——本技能的灵魂就在这：

- **`MultiLevelCacheService`**：L1 本地缓存（Caffeine/Guava/LRU）+ L2 Redis + L3 DB 回填，逐级穿透——**本地先挡，Redis 兜底，DB 最后**；
- **穿透防护**：DB 查不到→缓存空值（短 TTL 30s）+ 布隆过滤器前置拦截——**不存在也缓存，恶意 ID 打不动 DB**；
- **击穿防护**：互斥锁（SETNX）只让一个请求重建缓存 + 永不过期+异步刷新——**过期瞬间一个请求进 DB，其余等/拿旧值**；
- **雪崩防护**：TTL 加随机偏移 + L1 不依赖 Redis（Redis 挂了本地缓存仍可用）+ 熔断降级——**过期错峰，Redis 挂了也能扛**；
- **`DistributedLock`**：加锁 SET NX PX，释放 Lua 脚本，看门狗续期，可重入——**锁要能续、能好好地放，if-else 放锁是事故**；
- **`HotKeyDetector`**：本地滑动窗口统计 key 访问频率，超过阈值自动降级到 L1——**热点 key 就地消化，不再冲击 Redis**。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 260" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_r2" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_r2"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="260" fill="url(#bg_r2)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">六组件 三防线 一条链路</text>
  <rect x="40" y="50" width="340" height="70" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_r2)"/>
  <text x="210" y="72" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">L1 本地 → L2 Redis → L3 DB</text>
  <text x="210" y="98" text-anchor="middle" fill="#94a3b8" font-size="9">逐级穿透，DB 是最后一道防线</text>
  <rect x="420" y="50" width="340" height="70" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_r2)"/>
  <text x="590" y="72" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">布隆前置 + 空值缓存</text>
  <text x="590" y="98" text-anchor="middle" fill="#94a3b8" font-size="9">拦截在查 DB 之前，不存在的 key 打不到 DB</text>
  <rect x="40" y="140" width="340" height="70" rx="8" fill="#ef4444" opacity="0.12" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_r2)"/>
  <text x="210" y="162" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">SETNX 互斥 + 永不过期刷新</text>
  <text x="210" y="188" text-anchor="middle" fill="#94a3b8" font-size="9">过期瞬间只放一个请求进 DB</text>
  <rect x="420" y="140" width="340" height="70" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_r2)"/>
  <text x="590" y="162" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">TTL 随机偏移 + L1 兜底</text>
  <text x="590" y="188" text-anchor="middle" fill="#94a3b8" font-size="9">Redis 挂了本地缓存仍可用</text>
  <text x="400" y="252" text-anchor="middle" fill="#475569" font-size="10">防线是设计出来的，不是 get/set 写出来的</text>
</svg>
```

### Step 3: 生成测试

每个核心组件配测试，测试要求紧扣实现约束（使用 Mock/嵌入式 Redis）：

- **缓存穿透**：验证空值缓存 + 布隆过滤器拦截——**不存在的 key 打不到 DB，必须有测试证明**；
- **缓存击穿**：验证并发下只有一个请求重建缓存——**并发 N 个请求，只有 1 个进 DB，必须有测试证明**；
- **分布式锁**：验证加锁/释放/看门狗续期/可重入——**锁不会丢、不会死、能重入，必须有测试证明**；
- **热 key 探测**：验证频率统计阈值触发自动降级——**热点一到阈值就地消化，必须有测试证明**。

测试的作用是把三道防线**钉死**：防穿透、防击穿、防雪崩、防热 key——防线靠测试保证，不靠 review 保证。

### Step 4: 验证

1. 编译通过 + 测试通过；
2. 检查清单逐项确认：
   - [ ] 缓存操作全部经过 `CacheService`，无直接 Redis 客户端操作
   - [ ] 布隆过滤器已前置部署（非穿透后才查布隆）
   - [ ] TTL 包含随机偏移（非固定值）
   - [ ] 分布式锁释放使用 Lua 脚本
   - [ ] 热 key 探测有阈值配置（非硬编码）

五条清单对应五道红线复查：**全部走封装、布隆在前、TTL 错峰、Lua 放锁、阈值可配**——五项全过，多级缓存基础设施才算合格。

## 四、质量门禁：哪些必须做，哪些绝对不能做

**✅ 必须做**：

- 编译通过 + 测试通过；
- 所有缓存操作经过 `CacheService` 封装；
- 穿透/击穿/雪崩三种防护均已实现；
- 分布式锁释放使用 Lua 脚本；
- TTL 带有随机偏移。

**❌ 绝对不能做**：

- 禁止在业务代码中直接操作 Redis 客户端（get/set 绕过封装层）；
- 禁止缓存没有 TTL（永不过期又不刷新 = 脏数据常驻）；
- 禁止分布式锁释放用 `if-else` 代替 Lua（不是原子的，会误删别人刚拿到的锁）。

门禁的逻辑是"**缓存的贞操观**"：

- **不能裸**——get/set 必须走封装，裸写绕过的全是防线；
- **不能死**——缓存必须有 TTL/刷新机制，永不过期又不刷新是事故；
- **不能错**——放锁必须原子（Lua），if-else 放锁会误删他人锁。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 240" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_r3" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_r3"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="240" fill="url(#bg_r3)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">三条红线 = 三类事故</text>
  <rect x="40" y="50" width="340" height="70" rx="8" fill="#ef4444" opacity="0.12" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_r3)"/>
  <text x="210" y="72" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">禁裸 get/set：防线绕过</text>
  <text x="210" y="98" text-anchor="middle" fill="#64748b" font-size="9">穿透/击穿/雪崩全裸奔</text>
  <rect x="420" y="50" width="340" height="70" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_r3)"/>
  <text x="590" y="72" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">禁无 TTL：脏数据常驻</text>
  <text x="590" y="98" text-anchor="middle" fill="#64748b" font-size="9">永不过期又不刷新 = 事故</text>
  <rect x="40" y="140" width="340" height="70" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_r3)"/>
  <text x="210" y="162" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">禁 if-else 放锁：误删锁</text>
  <text x="210" y="188" text-anchor="middle" fill="#64748b" font-size="9">必须 Lua 原子释放</text>
  <rect x="420" y="140" width="340" height="70" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_r3)"/>
  <text x="590" y="162" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">禁硬编码阈值：改不动</text>
  <text x="590" y="188" text-anchor="middle" fill="#64748b" font-size="9">热 key 阈值必须可配</text>
  <text x="400" y="232" text-anchor="middle" fill="#475569" font-size="10">缓存是加速器，但防线一破就是放大器</text>
</svg>
```

## 五、四条约束：边界在哪里

- **不生成缓存监控面板代码**——只预留指标暴露接口（监控面板归监控团队）；
- **不引入未确认的缓存中间件依赖**——项目中已存在的优先复用；
- **不擅自决定 L1 缓存库**——用项目已有库，无则用推荐但不强制；
- **不修改已有业务代码**——只新增缓存基础设施，建议用户逐步迁移。

边界一句话：**本技能管"缓存怎么封装、锁怎么做对"，不碰"监控面板、依赖选型、业务改造"**。

## 六、与相邻技能的分工

| 场景 | 归属 |
|------|------|
| 多级缓存/分布式锁/热 key | **本技能**（`/redis-cache-wrapper`） |
| 缓存 Redis 的读写缓存设计 | `harness-db-design`（缓存表/索引设计） |
| 消息链路中的缓存一致性 | `kafka-toolkit` / `rocketmq-toolkit`（发布订阅） |

- **本技能 vs harness-db-design**：redis-cache-wrapper 管"数据怎么缓存"（L1/L2/L3 与防线）；harness-db-design 管"数据怎么存"（表/索引/约束）——一个管缓存，一个管库表；
- **本技能 vs 消息组件**：缓存更新靠消息驱动时，发布订阅是消息组件的事，本技能只负责缓存侧的一致性处理。

## 七、完成标志

`/redis-cache-wrapper` 的完成标志有三个：

1. **目标组件已生成**：cache/lock 按需产出——该有的都有；
2. **测试通过 + 检查清单全过**：五条红线逐项确认——不是"写了"，是"验证了"；
3. **业务代码不再直接操作 Redis 客户端**：所有缓存操作经过 `CacheService`，分布式锁经过 `DistributedLock`——纪律已生效。

三个标志对应三问：**补齐了吗**（组件）？**钉死了吗**（测试+清单）？**生效了吗**（不再裸写）？——三个都答"是"，多级缓存封装才算真正落地。

## 八、写在最后

`/redis-cache-wrapper` 的全部设计，浓缩成四句话：

1. **L1→L2→L3 逐级穿透**——本地先挡，Redis 兜底，DB 最后，逐级减负。
2. **三道防线一体设计**——布隆防穿透、互斥防击穿、TTL 错峰防雪崩。
3. **锁要原子**——SETNX 加锁、Lua 释放、看门狗续期，if-else 放锁是事故。
4. **热 key 就地消化**——滑动窗口探测，超阈值自动降级到 L1。

一句话记住它：**/redis-cache-wrapper 是缓存的"四防管家"——L1/L2/L3 多级缓存、布隆防穿透、SETNX 防击穿、TTL 错峰防雪崩、滑动窗口防热 key、Lua 原子锁，用"不裸写、不无 TTL、不 if-else 放锁、不硬编阈值"四条纪律，让缓存既快又稳、防线不破。**