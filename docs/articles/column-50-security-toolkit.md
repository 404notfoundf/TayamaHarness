# /security-toolkit：安全六件套——脱敏、加密、校验、鉴权、日志脱敏、安全配置，每个项目的必选项

> 命令深度拆解 · 第 50 篇 · 约 8000 字 · 6 个 SVG 图

---

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 300" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_sec0" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_sec0"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="300" fill="url(#bg_sec0)" rx="10"/>
  <text x="400" y="28" text-anchor="middle" fill="#e2e8f0" font-size="26" font-weight="700">/security-toolkit：安全六件套</text>
  <rect x="30" y="55" width="120" height="95" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_sec0)"/>
  <text x="90" y="80" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">desensitize</text>
  <text x="90" y="104" text-anchor="middle" fill="#64748b" font-size="9">四种内置规则</text>
  <text x="90" y="126" text-anchor="middle" fill="#64748b" font-size="8">手机/邮箱/证件/卡</text>
  <rect x="167" y="55" width="120" height="95" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_sec0)"/>
  <text x="227" y="80" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">crypto</text>
  <text x="227" y="104" text-anchor="middle" fill="#64748b" font-size="9">AES-256-GCM</text>
  <text x="227" y="126" text-anchor="middle" fill="#64748b" font-size="8">BCrypt 密码哈希</text>
  <rect x="304" y="55" width="120" height="95" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_sec0)"/>
  <text x="364" y="80" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">validation</text>
  <text x="364" y="104" text-anchor="middle" fill="#64748b" font-size="9">入口统一拦截</text>
  <text x="364" y="126" text-anchor="middle" fill="#64748b" font-size="8">XSS/SQL 注入/参数</text>
  <rect x="441" y="55" width="120" height="95" rx="8" fill="#a855f7" opacity="0.12" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_sec0)"/>
  <text x="501" y="80" text-anchor="middle" fill="#d8b4fe" font-size="12" font-weight="700">auth</text>
  <text x="501" y="104" text-anchor="middle" fill="#64748b" font-size="9">中间件统一鉴权</text>
  <text x="501" y="126" text-anchor="middle" fill="#64748b" font-size="8">Token/角色/权限</text>
  <rect x="578" y="55" width="100" height="95" rx="8" fill="#ef4444" opacity="0.12" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_sec0)"/>
  <text x="628" y="80" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">audit</text>
  <text x="628" y="104" text-anchor="middle" fill="#64748b" font-size="9">日志脱敏</text>
  <text x="628" y="126" text-anchor="middle" fill="#64748b" font-size="8">输出层拦截</text>
  <rect x="695" y="55" width="85" height="95" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_sec0)"/>
  <text x="737" y="80" text-anchor="middle" fill="#fde68a" font-size="10" font-weight="700">config</text>
  <text x="737" y="104" text-anchor="middle" fill="#64748b" font-size="9">CORS/限流</text>
  <text x="737" y="126" text-anchor="middle" fill="#64748b" font-size="8">安全响应头</text>
  <text x="400" y="185" text-anchor="middle" fill="#475569" font-size="13">铁律：禁止在业务代码中手动处理敏感信息脱敏和鉴权逻辑</text>
  <rect x="60" y="205" width="160" height="45" rx="8" fill="#3b82f6" opacity="0.10" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_sec0)"/>
  <text x="140" y="234" text-anchor="middle" fill="#93c5fd" font-size="11">① 确定生成目标</text>
  <rect x="240" y="205" width="160" height="45" rx="8" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_sec0)"/>
  <text x="320" y="234" text-anchor="middle" fill="#86efac" font-size="11">② 生成代码</text>
  <rect x="420" y="205" width="160" height="45" rx="8" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_sec0)"/>
  <text x="500" y="234" text-anchor="middle" fill="#fde68a" font-size="11">③ 测试钉死</text>
  <rect x="600" y="205" width="160" height="45" rx="8" fill="#a855f7" opacity="0.10" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_sec0)"/>
  <text x="680" y="234" text-anchor="middle" fill="#d8b4fe" font-size="11">④ 验证</text>
  <text x="400" y="286" text-anchor="middle" fill="#64748b" font-size="10">业务鉴权规则归 Controller · 证书密钥归安全团队 · 已有配置不覆盖</text>
</svg>
```

## 一、/security-toolkit 解决的是什么问题

### 1.1 裸写安全逻辑的七宗罪

安全是"每个项目必选项"——但裸写安全逻辑，几乎必然犯以下错误：

1. **脱敏靠手动**：每个业务代码里自己 `phone.replace(3, 7, "****")`——规则不统一，换个掩码改全项目，漏一处泄漏一处；
2. **密码用 MD5**：`MessageDigest.getInstance("MD5")` 一行搞定——彩虹表一查就破，等保评审直接打回；
3. **加密用 ECB**：`AES/ECB/PKCS5Padding`——同样的明文加密结果一样，模式本身有安全缺陷；
4. **校验散落**：每个方法自己校验参数——有的漏了，有的误杀了，规则不统一；
5. **鉴权逐 Controller 写**：每个 Controller 自己解析 Token、自己查权限——有的忘了写，接口裸奔；
6. **日志泄漏敏感信息**：日志框架直接打印对象 toString——手机号、身份证、银行卡全进了日志，脱敏无处可查；
7. **CORS 全开放**：`Access-Control-Allow-Origin: *`——跨域随便打，表单劫持等着你。

`/security-toolkit` 为项目生成安全基础设施代码：脱敏工具、加密工具、输入校验框架、鉴权中间件、日志脱敏、安全配置。**禁止在业务代码中手动处理敏感信息脱敏和鉴权逻辑**。

### 1.2 六个子命令，一道安全防线

| 子命令 | 组件 | 检测条件 |
|--------|------|---------|
| `desensitize` | 脱敏工具 | 日志/API 响应中包含敏感字段 |
| `crypto` | 加密工具 | 需要存储密码/Token/密钥 |
| `validation` | 输入校验框架 | 存在外部输入未校验 |
| `auth` | 鉴权中间件 | 存在需要鉴权的 API 接口 |
| `audit` | 日志脱敏 + 安全配置 | 无安全配置或日志泄漏敏感信息 |
| `all`（默认） | 全部组件 | 首次引入安全基础设施 |

六个子命令覆盖安全全场景：**desensitize 防泄漏，crypto 防破解，validation 防注入，auth 防越权，audit 防留痕，config 防裸奔**。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 260" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_sec1" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_sec1"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="260" fill="url(#bg_sec1)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">六件套 = 六道防线</text>
  <rect x="40" y="50" width="340" height="70" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_sec1)"/>
  <text x="210" y="72" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">防泄漏：脱敏工具</text>
  <text x="210" y="98" text-anchor="middle" fill="#94a3b8" font-size="9">手机/邮箱/证件/卡 · 自定义保留位+掩码</text>
  <rect x="420" y="50" width="340" height="70" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_sec1)"/>
  <text x="590" y="72" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">防破解：加密工具</text>
  <text x="590" y="98" text-anchor="middle" fill="#94a3b8" font-size="9">AES-256-GCM · RSA-2048-OAEP · BCrypt</text>
  <rect x="40" y="140" width="340" height="70" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_sec1)"/>
  <text x="210" y="162" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">防注入：入口校验</text>
  <text x="210" y="188" text-anchor="middle" fill="#94a3b8" font-size="9">XSS 转义 · SQL 注入校验 · 参数白名单</text>
  <rect x="420" y="140" width="340" height="70" rx="8" fill="#a855f7" opacity="0.12" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_sec1)"/>
  <text x="590" y="162" text-anchor="middle" fill="#d8b4fe" font-size="12" font-weight="700">防越权：中间件鉴权</text>
  <text x="590" y="188" text-anchor="middle" fill="#94a3b8" font-size="9">Token 校验 · 角色/权限 · 注入用户上下文</text>
  <text x="400" y="242" text-anchor="middle" fill="#475569" font-size="10">安全不是某个接口的事，是所有接口的默认前提</text>
</svg>
```

### 1.3 为什么"安全必须统一拦截"

安全逻辑有一个共同特点：**必须无条件执行**——不能靠每个开发记得写。

- 脱敏：不统一 → 漏一处泄漏一处；
- 校验：不统一 → 有的接口裸奔；
- 鉴权：不统一 → 忘写就是越权；
- 日志脱敏：不统一 → 日志就是泄漏源。

所以安全组件**统一在入口/输出层拦截**：XSS 过滤器在请求入口、AuthMiddleware 在路由层、LogDesensitizer 在日志输出层——**业务代码根本不用（也不许）自己处理，规则由安全层统一执行**。

## 二、触发方式与前置

**触发方式**两种：

- 独立命令 `/security-toolkit [desensitize|crypto|validation|auth|audit|all]`；
- 自动加载：coding-skill 阶段**始终加载**——安全组件是每个项目的必选项，不管项目里有没有显式安全代码。

**前置条件**：

1. 已识别技术栈；
2. 已确定目标包路径（按语言规范）；
3. 已确定安全需求等级（标准/高安全/合规要求）；
4. 用户未指定 → 生成全部组件（`all`）。

前置的用意：**安全组件生成的是"按你的需求等级的代码"**——标准级够用的 AES-GCM/BCrypt，安全级加密升级，合规级补审计——不知道需求等级，生成的组件要么过度要么不足。

## 三、四步执行流程

### Step 1: 确定生成目标

按安全需求等级和检测条件确定生成范围：

| 子命令 | 组件 | 检测条件 |
|--------|------|---------|
| `desensitize` | 脱敏工具 | 日志/API 响应中包含敏感字段 |
| `crypto` | 加密工具 | 需要存储密码/Token/密钥 |
| `validation` | 输入校验框架 | 存在外部输入未校验 |
| `auth` | 鉴权中间件 | 存在需要鉴权的 API 接口 |
| `audit` | 日志脱敏 + 安全配置 | 无安全配置或日志泄漏敏感信息 |
| `all`（默认） | 全部组件 | 首次引入安全基础设施 |

Step 1 是"按需裁剪"：日志里有敏感字段？生成 desensitize + audit。要存密码？生成 crypto。有外部接口？生成 validation + auth。首次引入？生成 all。**缺什么补什么，检测条件同时是体检**——裸写脱敏、MD5 密码、ECB 加密、CORS 全开放，都会被照出来。

### Step 2: 生成代码文件

以 Java 示例，生成 `{basePackage}/security/` 包：

```
{basePackage}/security/
├── DesensitizeUtil.java          — 脱敏工具
├── CryptoUtil.java               — 加密工具
├── validation/
│   ├── XssFilter.java            — XSS 过滤器
│   ├── SqlInjectionValidator.java
│   └── ParamValidator.java       — 参数校验器
├── auth/
│   ├── AuthMiddleware.java       — 鉴权中间件
│   ├── RequireAuth.java          — 鉴权注解
│   └── TokenUtil.java            — Token 工具
├── audit/
│   ├── LogDesensitizer.java      — 日志脱敏器
│   └── SecurityConfig.java       — 安全配置
└── config/
    └── application-security.yml
```

**每个组件的关键实现约束**——本技能的灵魂就在这：

- **`DesensitizeUtil`**：手机号/邮箱/身份证/银行卡四种内置规则，支持自定义保留位和掩码字符——**四种主流，规则统一**；
- **`CryptoUtil`**：AES-256-GCM（带认证），RSA-2048-OAEP，密码哈希用 BCrypt（cost >= 10 或 Argon2id），**严禁使用 MD5/SHA1/DES/ECB**——**算法选型是红线，弱算法一票否决**；
- **XSS 过滤器**：在请求入口处统一拦截，对输入做 HTML 转义（< -> &lt;，> -> &gt;），放过已有安全框架（如 Spring 的防 XSS）时跳过——**入口拦截，不误杀已有防线**；
- **参数校验器**：字符串长度上限、禁止字符集、数字范围、文件上传白名单、URL 协议白名单、分页参数上限——**规则可配，白名单思维**；
- **`AuthMiddleware`**：在路由层统一拦截，从请求头提取 Token，校验签名+有效期，校验角色/权限，注入用户信息到上下文，不依赖每个 Controller 自己写鉴权逻辑——**鉴权中间件统一，Controller 只写业务**；
- **`LogDesensitizer`**：在日志输出层统一拦截（日志框架的 Layout/Formatter 扩展），配置化脱敏规则（哪些字段+哪种规则），不在业务代码中手动脱敏——**日志脱敏在出口，配置化不硬编**；
- **`SecurityConfig`**：CORS（禁止开放所有域，限定来源+方法+max-age）、RateLimit（默认 100/s）、安全响应头（XSS-Protection/Content-Type-Options/Frame-Options/HSTS）——**CORS 白名单、限流、响应头三件套**。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 260" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_sec2" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_sec2"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="260" fill="url(#bg_sec2)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">最容易搞错的两个红线</text>
  <rect x="40" y="50" width="340" height="95" rx="8" fill="#ef4444" opacity="0.12" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_sec2)"/>
  <text x="210" y="72" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">密码弱算法：MD5/SHA1/DES</text>
  <text x="210" y="96" text-anchor="middle" fill="#94a3b8" font-size="9">彩虹表一查就破</text>
  <text x="210" y="116" text-anchor="middle" fill="#94a3b8" font-size="9">必须 BCrypt cost>=10 / Argon2id</text>
  <text x="210" y="136" text-anchor="middle" fill="#64748b" font-size="8">等保评审直接打回</text>
  <rect x="420" y="50" width="340" height="95" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_sec2)"/>
  <text x="590" y="72" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">加密 ECB 模式 / CORS 全开放</text>
  <text x="590" y="96" text-anchor="middle" fill="#94a3b8" font-size="9">同样的明文同样的密文</text>
  <text x="590" y="116" text-anchor="middle" fill="#94a3b8" font-size="9">Access-Control-Allow-Origin: *</text>
  <text x="590" y="136" text-anchor="middle" fill="#64748b" font-size="8">一个是缺陷模式，一个是裸奔</text>
  <rect x="120" y="175" width="560" height="60" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_sec2)"/>
  <text x="400" y="200" text-anchor="middle" fill="#93c5fd" font-size="11" font-weight="700">鉴权/脱敏/校验必须统一拦截</text>
  <text x="400" y="224" text-anchor="middle" fill="#94a3b8" font-size="9">入口/输出层统一执行，业务代码不手动处理</text>
</svg>
```

### Step 3: 生成测试

每个核心组件配测试，测试要求紧扣实现约束：

- **脱敏**：验证手机号/邮箱/身份证/银行卡四种规则，验证边界情况（空字符串/短字符串）——**空串不崩、短串不乱，必须有测试证明**；
- **加密**：验证 AES 加密解密一致性、RSA 加解密、BCrypt 哈希验证——**加密解密往返一致、BCrypt 能验不能解，必须有测试证明**；
- **XSS 过滤**：验证常见 XSS payload 被转义、验证合法 HTML 不被误杀——**恶意被挡、合法不误杀，必须有测试证明**；
- **参数校验**：验证长度上限、禁止字符、数字范围边界——**边界值都能拦住，必须有测试证明**；
- **鉴权**：验证无效 Token 返回 401、权限不足返回 403、有效 Token 通过——**三种状态各有归宿，必须有测试证明**。

测试的作用是把安全底线**钉死**：弱算法、ECB、漏校验、裸鉴权、日志泄漏——红线靠测试保证，不靠 review 保证。

### Step 4: 验证

1. 编译通过 + 测试通过；
2. 检查清单逐项确认：
   - [ ] 密码存储使用 BCrypt/Argon2（非 MD5/SHA1）
   - [ ] 对称加密使用 GCM 模式（非 ECB）
   - [ ] 日志中敏感信息已脱敏（手机号、邮箱、身份证、银行卡）
   - [ ] 输入校验在入口处统一拦截（非每个方法单独校验）
   - [ ] 鉴权逻辑在中间件中统一处理（非每个 Controller 单独写）
   - [ ] CORS 配置了白名单（非开放所有域）

六条清单对应六道红线复查：**强哈希、GCM 模式、日志脱敏、入口校验、中间件鉴权、CORS 白名单**——六项全过，安全基础设施才算合格。

## 四、质量门禁：哪些必须做，哪些绝对不能做

**✅ 必须做**：

- 编译通过 + 测试通过；
- 密码使用 BCrypt/Argon2；
- 对称加密使用 GCM 模式；
- 日志敏感信息已脱敏；
- 输入校验在入口处统一拦截；
- 鉴权在中间件统一处理；
- CORS 配置了白名单。

**❌ 绝对不能做**：

- 禁止使用 MD5/SHA1 存储密码；
- 禁止使用 ECB 加密模式；
- 禁止在业务代码中手动脱敏（应在输出层统一处理）；
- 禁止 CORS 开放所有域（`Access-Control-Allow-Origin: *`）。

门禁的逻辑是"**安全的贞操观**"：

- **不能弱**——密码哈希必须 BCrypt/Argon2，弱算法一票否决；
- **不能裸**——CORS 必须白名单，开放所有域等于裸奔；
- **不能散**——校验/鉴权/脱敏必须统一拦截，散落就会漏；
- **不能泄**——日志必须脱敏，泄漏就是事故。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 240" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_sec3" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_sec3"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="240" fill="url(#bg_sec3)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">四条红线 = 四类事故</text>
  <rect x="40" y="50" width="340" height="70" rx="8" fill="#ef4444" opacity="0.12" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_sec3)"/>
  <text x="210" y="72" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">禁弱算法：一破全破</text>
  <text x="210" y="98" text-anchor="middle" fill="#64748b" font-size="9">MD5/SHA1/DES/ECB 全禁</text>
  <rect x="420" y="50" width="340" height="70" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_sec3)"/>
  <text x="590" y="72" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">禁 CORS 全开：等于裸奔</text>
  <text x="590" y="98" text-anchor="middle" fill="#64748b" font-size="9">必须限定来源+方法+max-age</text>
  <rect x="40" y="140" width="340" height="70" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_sec3)"/>
  <text x="210" y="162" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">禁手动脱敏：规则不统一</text>
  <text x="210" y="188" text-anchor="middle" fill="#64748b" font-size="9">应在输出层统一拦截</text>
  <rect x="420" y="140" width="340" height="70" rx="8" fill="#a855f7" opacity="0.12" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_sec3)"/>
  <text x="590" y="162" text-anchor="middle" fill="#d8b4fe" font-size="12" font-weight="700">禁逐接口鉴权：忘写裸奔</text>
  <text x="590" y="188" text-anchor="middle" fill="#64748b" font-size="9">应在中间件统一处理</text>
  <text x="400" y="232" text-anchor="middle" fill="#475569" font-size="10">安全是默认前提，不是加分项</text>
</svg>
```

## 五、四条约束：边界在哪里

- **不生成业务相关的鉴权逻辑**——如"管理员才能删除订单"这类业务规则由用户在 Controller 中实现（本技能管"谁有 Token 谁可信"，不管"谁能删单谁不能"）；
- **不引入未确认的加密库**——项目中已有的优先复用（如 Java 用 javax.crypto 而非 BouncyCastle）；
- **不修改已有的安全配置**——只新增安全组件，不覆盖已有配置；
- **不生成证书/密钥管理方案**——证书由安全团队或云平台管理。

边界一句话：**本技能管"安全基础设施怎么搭对"，不碰"业务权限规则、加密库选型、已有配置、证书密钥"**。

## 六、与相邻技能的分工

| 场景 | 归属 |
|------|------|
| 脱敏/加密/校验/鉴权/日志脱敏/安全配置 | **本技能**（`/security-toolkit`） |
| 日志链路安全（traceId 注入与脱敏配合） | `logging-toolkit`（MDC trace） |
| API 响应的敏感字段脱敏 | `http-client-toolkit`（响应处理侧） |

- **本技能 vs logging-toolkit**：security-toolkit 的 `LogDesensitizer` 管"日志里禁止出现什么"（敏感字段）；logging-toolkit 管"日志里必须带上什么"（traceId）——一个做减法，一个做加法，配合成既安全又可追踪的日志；
- **本技能 vs 业务鉴权**：接口级业务权限（谁能删订单）是业务规则，由用户在 Controller 实现——本技能到"身份可信/角色匹配"为止。

## 七、完成标志

`/security-toolkit` 的完成标志有三个：

1. **目标组件已生成**：desensitize/crypto/validation/auth/audit 按需产出——该有的都有；
2. **测试通过 + 检查清单全过**：六条红线逐项确认——不是"写了"，是"验证了"；
3. **业务代码不再手动脱敏/手动鉴权**：脱敏在输出层、鉴权在中间件、校验在入口——纪律已生效。

三个标志对应三问：**补齐了吗**（组件）？**钉死了吗**（测试+清单）？**生效了吗**（不再手动）？——三个都答"是"，安全基础设施才算真正落地。

## 八、写在最后

`/security-toolkit` 的全部设计，浓缩成四句话：

1. **算法选型是红线**——BCrypt/Argon2 存密码、AES-256-GCM 加密、RSA-2048-OAEP 非对称，弱算法一票否决。
2. **统一拦截是纪律**——XSS 在入口、鉴权在路由、日志脱敏在输出，业务代码不手动处理。
3. **黑名单思维变白名单**——CORS 限定来源、文件上传白名单、URL 协议白名单，默认可疑。
4. **安全是默认前提**——每个项目必装，不是有需求才补。

一句话记住它：**/security-toolkit 是安全的"六件套守门人"——脱敏四规则、CORS 白名单、强哈希强加密、入口校验、中间件鉴权、日志输出层脱敏，用"不能弱、不能裸、不能散、不能泄"四条纪律，让每个项目默认安全、不靠回忆。**