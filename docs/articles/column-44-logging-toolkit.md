# /logging-toolkit：日志四件套——traceId 自动注入、脱敏统一拦截、动态级别、文件策略

> 命令深度拆解 · 第 44 篇 · 约 8000 字 · 7 个 SVG 图

---

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 300" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_l0" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_l0"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="300" fill="url(#bg_l0)" rx="10"/>
  <text x="400" y="28" text-anchor="middle" fill="#e2e8f0" font-size="26" font-weight="700">/logging-toolkit：日志四件套</text>
  <rect x="40" y="55" width="160" height="90" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_l0)"/>
  <text x="120" y="80" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">trace</text>
  <text x="120" y="104" text-anchor="middle" fill="#64748b" font-size="9">MDC traceId 自动注入</text>
  <text x="120" y="126" text-anchor="middle" fill="#64748b" font-size="8">子线程继承 · 响应回传</text>
  <rect x="220" y="55" width="160" height="90" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_l0)"/>
  <text x="300" y="80" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">desensitize</text>
  <text x="300" y="104" text-anchor="middle" fill="#64748b" font-size="9">配置化脱敏规则</text>
  <text x="300" y="126" text-anchor="middle" fill="#64748b" font-size="8">输出层统一拦截</text>
  <rect x="400" y="55" width="160" height="90" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_l0)"/>
  <text x="480" y="80" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">dynamic</text>
  <text x="480" y="104" text-anchor="middle" fill="#64748b" font-size="9">动态日志级别 API</text>
  <text x="480" y="126" text-anchor="middle" fill="#64748b" font-size="8">重启后保留</text>
  <rect x="580" y="55" width="180" height="90" rx="8" fill="#a855f7" opacity="0.12" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_l0)"/>
  <text x="670" y="80" text-anchor="middle" fill="#d8b4fe" font-size="12" font-weight="700">strategy</text>
  <text x="670" y="104" text-anchor="middle" fill="#64748b" font-size="9">滚动/压缩/保留</text>
  <text x="670" y="126" text-anchor="middle" fill="#64748b" font-size="8">500MB · 30天 · 10GB</text>
  <text x="400" y="185" text-anchor="middle" fill="#475569" font-size="13">铁律：禁止在业务代码中手动处理日志脱敏和 traceId 传递</text>
  <rect x="60" y="205" width="160" height="45" rx="8" fill="#3b82f6" opacity="0.10" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_l0)"/>
  <text x="140" y="234" text-anchor="middle" fill="#93c5fd" font-size="11">① 确定目标</text>
  <rect x="240" y="205" width="160" height="45" rx="8" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_l0)"/>
  <text x="320" y="234" text-anchor="middle" fill="#86efac" font-size="11">② 生成代码</text>
  <rect x="420" y="205" width="160" height="45" rx="8" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_l0)"/>
  <text x="500" y="234" text-anchor="middle" fill="#fde68a" font-size="11">③ 测试钉死</text>
  <rect x="600" y="205" width="160" height="45" rx="8" fill="#a855f7" opacity="0.10" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_l0)"/>
  <text x="680" y="234" text-anchor="middle" fill="#d8b4fe" font-size="11">④ 验证</text>
  <text x="400" y="286" text-anchor="middle" fill="#64748b" font-size="10">告警规则归监控 · 采集配置归运维 · 只新增不覆盖</text>
</svg>
```

## 一、/logging-toolkit 解决的是什么问题

### 1.1 日志用不好，事故查不了

日志是系统唯一的"事后真相"，但裸写日志基础设施，几乎必然踩这些坑：

1. **traceId 靠手动传**：每个方法都要把 traceId 当参数传来传去——漏传一处，链路就断了；排查一个跨服务问题，要在十几个服务里 grep 半天；
2. **敏感信息明文落日志**：手机号、身份证、Token 直接打印——日志被同事看到、被采集系统收集、被泄漏到第三方……**泄密事故往往不是数据库被拖库，而是日志里躺着明文密码**；
3. **日志级别改不了**：线上排查问题需要 DEBUG 日志，只能改配置重启应用——重启一次，现场就没了；
4. **日志文件无策略**：不滚动、不压缩、不限制——一个月后 /var/log 被日志打满，磁盘满导致整个服务崩掉。

`/logging-toolkit` 生成日志基础设施代码：MDC traceId 自动注入、日志脱敏（配置化规则+统一拦截）、动态日志级别 API、日志文件策略（滚动/压缩/保留）、链路日志格式化。**禁止在业务代码中手动处理日志脱敏和 traceId 传递**。

### 1.2 四个子命令，各守一道

| 子命令 | 组件 | 检测条件 |
|--------|------|---------|
| `trace` | traceId 自动注入 | 无 traceId 或手动传递 |
| `desensitize` | 日志脱敏 | 日志输出中包含明文敏感字段 |
| `dynamic` | 动态日志级别 | 无动态日志级别切换能力 |
| `strategy` | 日志文件策略 | 无日志文件配置或使用默认配置 |
| `all`（默认） | 全部组件 | 首次引入日志基础设施 |

四个子命令各守一道：**trace 保证链路可查，desensitize 保证机密不泄，dynamic 保证排查不断线，strategy 保证磁盘不被打爆**。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 260" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_l1" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_l1"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="260" fill="url(#bg_l1)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">日志四件套，各守一道</text>
  <rect x="40" y="50" width="340" height="70" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_l1)"/>
  <text x="210" y="72" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">trace：链路可查</text>
  <text x="210" y="98" text-anchor="middle" fill="#94a3b8" font-size="9">MDC 自动注入 · 线程池传递 · 响应头回传</text>
  <rect x="420" y="50" width="340" height="70" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_l1)"/>
  <text x="590" y="72" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">desensitize：机密不泄</text>
  <text x="590" y="98" text-anchor="middle" fill="#94a3b8" font-size="9">配置化规则 · 输出层统一拦截</text>
  <rect x="40" y="140" width="340" height="70" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_l1)"/>
  <text x="210" y="162" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">dynamic：排查不断线</text>
  <text x="210" y="188" text-anchor="middle" fill="#94a3b8" font-size="9">REST API 改级别 · 重启后保留</text>
  <rect x="420" y="140" width="340" height="70" rx="8" fill="#a855f7" opacity="0.12" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_l1)"/>
  <text x="590" y="162" text-anchor="middle" fill="#d8b4fe" font-size="12" font-weight="700">strategy：磁盘不爆</text>
  <text x="590" y="188" text-anchor="middle" fill="#94a3b8" font-size="9">按大小滚动 · gz 压缩 · 30 天保留</text>
  <text x="400" y="240" text-anchor="middle" fill="#475569" font-size="10">日志不是越多越好，是"该有的都有、不该有的绝不出现"</text>
</svg>
```

### 1.3 为什么"禁止在业务代码中手动脱敏"

手动脱敏（`user.getPhone().replace(...)`）有三宗罪：

1. **漏一处就泄一处**——每个打印点都要记得脱敏，总有遗漏；
2. **规则不一致**——两个开发写的脱敏规则不一样，日志里手机号一会儿 `138****1234` 一会儿 `138***1234`；
3. **没法改**——要新增一个敏感字段，得把所有打印点翻一遍。

正确姿势是在**日志输出层统一拦截**（日志框架的 Layout/Formatter 扩展），配置化规则（字段名+规则类型）——**业务代码摸不到脱敏逻辑，敏感字段永远到不了日志**。

## 二、触发方式与前置

**触发方式**两种：

- 独立命令 `/logging-toolkit [trace|desensitize|dynamic|strategy|all]`；
- 自动加载：coding-skill 阶段**始终加载**（日志是所有项目的必选项）。

**前置条件**：

1. 已识别技术栈 + 日志框架（Java Logback/Log4j2 / Python logging / Go zap）；
2. 已确定目标包路径（按语言规范）；
3. 已确定敏感字段列表（至少包含手机号/邮箱/身份证/银行卡/Token/密码）；
4. 用户未指定 → 生成全部组件（`all`）。

前置的用意：**日志是照妖镜，前提是镜面得先擦对**——先确定技术栈和敏感字段清单，生成的代码才能直接贴合项目。

## 三、四步执行流程

### Step 1: 确定生成目标

按子命令或检测条件确定要生成哪些组件：

| 子命令 | 组件 | 检测条件 |
|--------|------|---------|
| `trace` | traceId 自动注入 | 无 traceId 或手动传递 |
| `desensitize` | 日志脱敏 | 日志输出中包含明文敏感字段 |
| `dynamic` | 动态日志级别 | 无动态日志级别切换能力 |
| `strategy` | 日志文件策略 | 无日志文件配置或使用默认配置 |
| `all`（默认） | 全部组件 | 首次引入日志基础设施 |

Step 1 是"按需裁剪"：已有 trace 只缺脱敏？生成 desensitize。首次引入？生成 all。**缺什么补什么，检测条件同时是体检**——无 traceId、明文敏感字段、无文件策略，都会照出来。

### Step 2: 生成代码文件

以 Java 示例，生成 `{basePackage}/logging/` 包：

```
{basePackage}/logging/
├── trace/
│   ├── TraceIdFilter.java            — Servlet Filter traceId 注入（从请求头提取或生成）
│   ├── TraceIdInterceptor.java       — Feign/RestTemplate 拦截器（自动透传）
│   └── TraceIdMDCInjector.java       — 线程池 MDC 上下文传递（装饰器模式）
├── desensitize/
│   ├── LogDesensitizer.java          — 日志脱敏器（配置化规则）
│   ├── DesensitizeRule.java          — 脱敏规则定义（字段名+规则类型）
│   └── SensitiveFields.java          — 默认敏感字段列表
├── dynamic/
│   ├── LogLevelController.java       — 动态日志级别 API（REST 接口）
│   └── LogLevelConfig.java           — 日志级别持久化配置
├── strategy/
│   ├── logback-spring.xml            — Logback 配置文件模板
│   └── LogFileStrategy.java          — 文件策略（滚动/压缩/保留天数）
└── config/
    └── LoggingAutoConfig.java        — 自动配置（确保所有组件生效）
```

**每个组件的关键实现约束**——本技能的灵魂就在这：

- **`TraceIdFilter`**：从请求头 `X-Trace-Id` 提取，不存在则 UUID 生成，注入 MDC 上下文 `traceId`，响应头中回传，请求结束后从 MDC 清除（防止内存泄漏）——**链路由入口自动拉起，请求结束自动清场**；
- **`TraceIdMDCInjector`**：线程池提交任务时自动传递 MDC 上下文（包装 Runnable/Callable），子线程池继承父线程 traceId——**异步代码不丢 trace**；
- **`LogDesensitizer`**：在日志输出层统一拦截（日志框架的 Layout/Formatter 扩展），配置化规则（字段名+规则类型），不在业务代码中手动脱敏——**敏感字段在出口处统一拦截**；
- **脱敏规则**：手机号 `138****1234`、邮箱 `user***@example.com`、身份证 `110********1234`、银行卡 `6222****1234`、密码 `******`、Token `tok_***`——**六类规则开箱即用**；
- **`LogLevelController`**：提供 REST 接口 `GET/PUT /actuator/loggers/{package}` 查看和修改日志级别，支持持久化（重启后保留）——**排查问题不用重启**；
- **日志文件策略**：按大小滚动（maxFileSize=500MB），保留最近 30 天，总大小不超过 10GB，压缩归档（.gz），异步写日志（不阻塞业务线程）——**磁盘有上限，性能不拖累**。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 270" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_l2" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_l2"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="270" fill="url(#bg_l2)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">最容易出事的两个环节</text>
  <rect x="40" y="50" width="340" height="95" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_l2)"/>
  <text x="210" y="72" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">traceId：自动，别手动</text>
  <text x="210" y="96" text-anchor="middle" fill="#94a3b8" font-size="9">入口 Filter 注入 MDC</text>
  <text x="210" y="116" text-anchor="middle" fill="#94a3b8" font-size="9">线程池包装 Runable/Callable</text>
  <text x="210" y="136" text-anchor="middle" fill="#64748b" font-size="8">请求结束从 MDC 清除</text>
  <rect x="420" y="50" width="340" height="95" rx="8" fill="#ef4444" opacity="0.12" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_l2)"/>
  <text x="590" y="72" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">脱敏：出口拦截，别逐点打</text>
  <text x="590" y="96" text-anchor="middle" fill="#94a3b8" font-size="9">手机号 138****1234</text>
  <text x="590" y="116" text-anchor="middle" fill="#94a3b8" font-size="9">邮箱 user***@example.com</text>
  <text x="590" y="136" text-anchor="middle" fill="#64748b" font-size="8">密码 ****** · Token tok_***</text>
  <rect x="120" y="175" width="560" height="60" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_l2)"/>
  <text x="400" y="200" text-anchor="middle" fill="#fde68a" font-size="11" font-weight="700">手动脱敏三宗罪：漏一处泄一处 · 规则不一致 · 没法改</text>
  <text x="400" y="224" text-anchor="middle" fill="#94a3b8" font-size="9">配置化 + 统一拦截，敏感字段永远到不了日志</text>
</svg>
```

### Step 3: 生成测试

每个核心组件配测试，测试要求紧扣实现约束（用 Mock MVC 测试）：

- **traceId**：验证请求入口注入、子线程传递、响应头回传、请求结束后清除——**四件事各测各的，尤其是"请求结束后清除"**（不测这个，内存泄漏没被发现）；
- **脱敏**：验证手机号/邮箱/身份证/银行卡/密码/Token 六种规则，验证边界情况——**明文必须一个都出不去**；
- **动态日志级别**：验证 GET/PUT 接口、级别持久化、重启后恢复。

测试的作用是把约束**钉死**：自动注入、统一拦截、持久化——红线靠测试保证，不靠 review 保证。

### Step 4: 验证

1. 编译通过 + 测试通过；
2. 检查清单逐项确认：
   - [ ] traceId 在请求入口自动注入（非手动生成）
   - [ ] 子线程自动传递 MDC 上下文
   - [ ] 日志脱敏在输出层统一拦截（非业务代码中手动脱敏）
   - [ ] 脱敏规则可配置（非硬编码）
   - [ ] 动态日志级别 API 可用
   - [ ] 日志文件滚动策略已配置

六条清单对应六道红线复查：**自动注入、线程传递、出口拦截、规则可配、级别可调、文件会滚**——六项全过，日志基础设施才算合格。

## 四、质量门禁：哪些必须做，哪些绝对不能做

**✅ 必须做**：

- traceId 在请求入口自动注入（非手动生成）；
- 子线程自动传递 MDC 上下文；
- 日志脱敏在输出层统一拦截（非业务代码中手动脱敏）；
- 脱敏规则可配置（非硬编码）；
- 动态日志级别 API 可用；
- 日志文件滚动策略已配置。

**❌ 绝对不能做**：

- 禁止在业务代码中手动传递 traceId（会漏链）；
- 禁止在业务代码中手动脱敏（应在日志输出层统一处理）；
- 禁止日志文件无滚动策略（会打满磁盘）；
- 禁止日志配置使用同步写（应使用异步 Appender）。

门禁的逻辑是"**日志的贞操观**"：

- **不能漏**——traceId 必须自动传递，漏一处链条就断；
- **不能泄**——敏感字段必须出口拦截，泄一个就是事故；
- **不能堵**——日志不能拖慢业务线程，必须异步写；
- **不能爆**——文件必须滚动，不然磁盘被打满。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 240" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_l3" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_l3"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="240" fill="url(#bg_l3)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">日志四条贞操观</text>
  <rect x="40" y="50" width="340" height="70" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_l3)"/>
  <text x="210" y="72" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">不能漏：trace 自动传</text>
  <text x="210" y="98" text-anchor="middle" fill="#64748b" font-size="9">漏一处链条就断</text>
  <rect x="420" y="50" width="340" height="70" rx="8" fill="#ef4444" opacity="0.12" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_l3)"/>
  <text x="590" y="72" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">不能泄：敏感出口拦截</text>
  <text x="590" y="98" text-anchor="middle" fill="#64748b" font-size="9">泄一个就是事故</text>
  <rect x="40" y="140" width="340" height="70" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_l3)"/>
  <text x="210" y="162" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">不能堵：异步写日志</text>
  <text x="210" y="188" text-anchor="middle" fill="#64748b" font-size="9">日志不拖累业务线程</text>
  <rect x="420" y="140" width="340" height="70" rx="8" fill="#a855f7" opacity="0.12" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_l3)"/>
  <text x="590" y="162" text-anchor="middle" fill="#d8b4fe" font-size="12" font-weight="700">不能爆：文件会滚动</text>
  <text x="590" y="188" text-anchor="middle" fill="#64748b" font-size="9">500MB 滚动 · 30 天 · 10GB 上限</text>
  <text x="400" y="240" text-anchor="middle" fill="#475569" font-size="10">日志是照妖镜，但镜面不能漏、不能泄、不能堵、不能爆</text>
</svg>
```

## 五、四条约束：边界在哪里

- **不生成日志告警规则**——由监控团队在 Prometheus AlertManager 中配置；
- **不修改已有的日志配置**——只新增，不覆盖——动了别人的日志配置，等于动了别人的排查工具；
- **不引入未确认的日志框架版本**——项目中已有的优先复用；
- **不生成日志采集（Filebeat/Fluentd）配置**——由运维团队管理。

边界一句话：**本技能管"日志怎么写对"，不碰"日志怎么采、怎么告"**。

## 六、与相邻技能的分工

| 场景 | 归属 |
|------|------|
| traceId 注入与透传 | **本技能**（`/logging-toolkit`） |
| 服务间调用 trace 透传 | `http-client-toolkit`（TraceIdInterceptor） |
| 消息链路 traceId | `kafka-toolkit` / `rocketmq-toolkit`（DLQ/消息结构带 traceId） |

- **本技能 vs http-client-toolkit**：logging-toolkit 管 traceId 在**本地日志**里怎么注入、怎么清场；http-client-toolkit 的 TraceIdInterceptor 管 traceId 在**跨服务调用**时怎么透传——一个管日志上下文，一个管网关/客户端；
- **本技能 vs kafka/rocketmq-toolkit**：消息组件在消息结构里带 traceId，logging-toolkit 负责把它记进日志——一个在消息里传，一个在日志里记。

## 七、完成标志

`/logging-toolkit` 的完成标志有三个：

1. **目标组件已生成**：trace/desensitize/dynamic/strategy 按需产出——该有的都有；
2. **测试通过 + 检查清单全过**：六条红线逐项确认——不是"写了"，是"验证了"；
3. **业务代码不再手动传 traceId / 手动脱敏**：敏感字段在出口统一拦截（或已给出迁移建议）——纪律已生效。

三个标志对应三问：**补齐了吗**（组件）？**钉死了吗**（测试+清单）？**生效了吗**（不再手动）？——三个都答"是"，日志基础设施才算真正落地。

## 八、写在最后

`/logging-toolkit` 的全部设计，浓缩成四句话：

1. **traceId 自动注入，子线程自动继承**——链路是系统给的，不是代码传的。
2. **脱敏在输出层统一拦截**——敏感字段永远到不了日志，漏一处就是事故。
3. **动态级别 API 排查不用重启**——现场是跑出来的，重启一次现场就没了。
4. **文件策略：滚动、压缩、限总量**——日志可以是证据，但不能是磁盘杀手。

一句话记住它：**/logging-toolkit 是日志的"四件套管家"——它生成 MDC traceId 自动注入、配置化脱敏统一拦截、动态日志级别 API、滚动压缩的文件策略，用"不手动、不泄漏、可切换、有上限"四条纪律，让每一行日志都可追溯、不泄密、查得动、撑得住。**