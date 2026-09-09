# 第十八章 · 支持一个新框架

> Harness 支持一个新框架的本质，不是"再写一套规则"，而是**把新框架的差异填进已有的参数表**。
>
> 但"填参数表"有个前置条件：**这个框架属于的语言包必须已经存在**。如果语言包存在，改 5-50 行参数表即可；如果语言包不存在，得先造语言包，再填参数表。
>
> 本文档只解决一件事：**把一个未被 Harness 支持的框架接进来**。
>
> —— 项目信条：**支持一个框架的成本，应当与框架的差异成正比，而不是与已有框架数量成正比。**

---

## 一、一个目标，两个分支

支持一个新框架，第一步永远是问自己一个问题：

> **这个框架属于的语言包，Harness 已经支持了吗？**

目前 Harness 支持 6 个语言包：

```
skills/harness-java/       skills/harness-golang/      skills/harness-python/
skills/harness-rust/       skills/harness-front/
```

| 判断结果 | 分支 | 改动范围 | 耗时 |
|---------|------|---------|------|
| **语言包已存在** | 分支 A：同语言内新增框架 | 只改参数表 + 检测表 | 15-30 分钟 |
| **语言包不存在** | 分支 B：新增语言 + 新增框架 | 造语言包 + 改参数表 + 检测表 | 3-5 小时 |

**两条分支的最终产物完全一样**——都是让这个框架能被 `apply-harness` 自动检测并渲染出正确的约束文件。区别只是路径长短。

### 1.1 几个典型场景的判断

| 你手上的情况 | 判定 | 分支 |
|-------------|------|------|
| 项目用 Go，新上 Fiber（Go 已有） | 语言包已存在 | **A** |
| 项目用 Java，新上 Quarkus（Java 已有） | 语言包已存在 | **A** |
| 项目用 TypeScript，新上 NestJS（TS 尚无语言包） | 语言包不存在 | **B** |
| 项目用 Lua，新上 OpenResty（Lua 尚无语言包） | 语言包不存在 | **B** |
| 项目用 Go，新上 Fresh（Go 已有但 Fresh 未列） | 语言包已存在，框架未列 | **A** |

**原则：判断的是"语言包存不存在"，不是"框架支不支持"。** 框架未列只是缺了一行参数，语言包缺失才是结构性工作。

---

## 二、分支 A：同语言内新增框架（15-30 分钟）

**场景**：`harness-golang` 已支持 Gin / go-zero / Echo / Fiber / Chi … 现要新增 **Fresh**（一个新兴的 Go Web 框架）。

### 2.1 五步实操

#### Step 1：把框架加入检测表

打开 `skills/apply-harness/SKILL.md`，在「Go 检测」表中追加一行：

```markdown
#### Go 检测
...
| `go.mod` + `fresh` import | Fresh | go mod | 读取 Go 版本 |
| `go.mod` + `anyi` 依赖 | Anyi | go mod | 读取 Go 版本 |
```

**检测特征必须能唯一识别该框架**。Fresh 的特征是 `go.mod` 里包含 `github.com/saltyshiomaru/fresh`。

> **经验**：多个框架共享同一个依赖文件时（Go 全是 `go.mod`，Java 全是 `pom.xml`），靠 import / dependency 名称区分。Python 靠 `pyproject.toml` 或 `requirements.txt` 里的依赖名。

#### Step 2：写框架参数块（只写差异）

在 apply-harness 参数表的 Go 区段，找「Go — Kratos」块之后、`### Go 基础参数（通用）` 之前，追加：

```markdown
### Go — Fresh

| 参数 | 值 |
|------|-----|
| `{{LANGUAGE_DESC}}` | Go、Fresh、边缘渲染全栈 Web 框架 |
| `{{FRAMEWORK_VER}}` | Fresh 0.1.x |
| `{{DEV_CMD}}` | `go run main.go` |
| `{{ARCH_LAYER}}` | 分层 route → handler → component → store，依赖单向 |
| `{{DB_ACCESS}}` | sqlx / GORM（Fresh 不强制 ORM，项目自选） |
```

**只写 5 行**。其他参数全部继承自「Go 基础参数（通用）」块——`{{BUILD_CMD}}` = `go build ./...`、`{{TEST_CMD}}` = `go test ./...`、`{{LINT_CMD}}` = `golangci-lint run`、`{{RACE_DETECT_ARG}}` = `-race` 等，都不需要重复写。

这是参数化设计的兑现：**框架差异真的只在 5-7 个参数**。

#### Step 3：本机校验命令

```bash
go version
golangci-lint version
go build ./...
go test ./...
```

#### Step 4：更新语言包 SKILL.md

在 `skills/harness-golang/SKILL.md` 的基线框架清单中加一项：

```markdown
- **基线框架**: Gin / go-zero / Echo / Fiber / Chi / ... / Fresh / ...
```

#### Step 5：端到端验收

在 Fresh 项目根目录跑：

```bash
/apply-harness    # 应自动识别为 Go + Fresh
```

检查渲染结果：

| 验收项 | 期望 |
|--------|------|
| Owner Agent 中 `{{LANGUAGE_DESC}}` | "Go、Fresh、边缘渲染全栈 Web 框架" |
| `.harness/rules/编码规范.md` | Go 版（来自 harness-golang） |
| `.harness/skills/golang/coding-skill/` | 存在，命令为 `go build ./...` |
| 残留 `{{...}}` 占位符 | **零个** |

---

### 2.2 Gin vs Fresh 参数差异一览

用这张表回答"到底改了哪些参数"：

| 参数 | Gin（已有） | Fresh（新增） | 变化 |
|------|-----------|---------------|------|
| `{{LANGUAGE_DESC}}` | Go、Gin、高性能 HTTP 框架 | Go、Fresh、边缘渲染全栈 Web 框架 | ✅ |
| `{{FRAMEWORK_VER}}` | Gin 1.10+ | Fresh 0.1.x | ✅ |
| `{{DEV_CMD}}` | `go run ./cmd/server` | `go run main.go` | ✅ |
| `{{ARCH_LAYER}}` | handler → service → repository | route → handler → component → store | ✅ |
| `{{DB_ACCESS}}` | GORM / sqlx + golang-migrate | sqlx / GORM（自选） | ✅ |
| `{{BUILD_CMD}}` / `{{TEST_CMD}}` / `{{LINT_CMD}}` / 其他 32 个参数 | … | … | ❌ 全部继承 |

**总改动行数：5 行参数 + 1 行检测表 + 1 行 SKILL.md = 7 行。**

---

## 三、分支 B：新增语言 + 新增框架（3-5 小时）

**场景**：要支持 **NestJS**（TypeScript 框架）。Harness 目前没有 `harness-typescript` 语言包，所以必须先造语言包，再在语言包内挂 NestJS 框架。

### 3.1 先理解"语言包"到底是个什么

语言包不是一个文件，而是一套**语言级约束**。以 `harness-golang` 为例：

```
skills/harness-golang/
├── SKILL.md                          ← 语言包元信息（frontmatter + 基线框架清单）
└── rules/
    ├── 工程结构.md                    ← Go 的目录骨架（go.mod、cmd/、internal/）
    └── 编码规范.md                    ← Go 的命名/注释/并发/错误处理规范
```

完整的 Java 语言包额外有专属技能：

```
skills/harness-java/
├── SKILL.md
├── rules/
│   ├── 工程结构.md
│   └── 编码规范.md
└── skills/                            ← Java 独有：4 个语言专属技能
    ├── java-code-review/
    ├── spring-api-convention/
    ├── mybatis-toolkit/
    └── openfeign-toolkit/
```

**关键事实**：
- 语言包最小集合 = `SKILL.md` + `rules/工程结构.md` + `rules/编码规范.md`（3 个文件）。
- **流水线技能（coding-skill / unit-test-write / expert-reviewer 等 10 个）语言包不维护**——它们在 `harness-core/skills/`，由 `apply-harness` 渲染时根据语言参数替换 `{{PLACEHOLDER}}`。
- **前端语言包 `harness-front` 额外有 `rules/运行时可靠性.md`**——前端的运行时可靠性（性能/安全/降级）与后端差异大，后端复用 harness-core 通用版。

### 3.2 八步实操（TypeScript + NestJS 完整走一遍）

#### Step 1：复制语言包骨架

```bash
cp -r skills/harness-golang skills/harness-typescript
```

选 `harness-golang` 做参照，因为它是结构最简洁的完整语言包（2 个规则文件，无专属技能）。

#### Step 2：改写 `SKILL.md`

改 frontmatter 和基线框架清单：

```markdown
---
name: harness-typescript
description: TypeScript 语言规范包 — 编码规范、工程结构
---

# Harness TypeScript — TypeScript 语言规范包

- **基线框架**: NestJS / Elysia / Hono / Fastify / Express
- **运行时**: Node.js 20 LTS+
- **测试框架**: Vitest + Testing Library
- **代码规范**: ESLint + Prettier + TypeScript 严格模式
- **依赖管理**: npm / pnpm / yarn

包含 rules（2 个语言特有 + 3 个通用来自 harness-core）。
流水线技能由 apply-harness 从 harness-core/skills/ 模板渲染。
```

#### Step 3：改写 `rules/工程结构.md`

Go 的目录骨架（go.mod / cmd/ / internal/ / common/）全部替换为 TypeScript 的：

```markdown
## 1. 顶层目录结构（TypeScript 项目）

<project-root>/
├── CLAUDE.md
├── package.json / pnpm-lock.yaml
├── tsconfig.json / tsconfig.build.json
├── .editorconfig
│
├── .harness/
│   ├── agents/
│   ├── rules/
│   ├── skills/
│   ├── wiki/
│   └── changes/
│
├── src/
│   ├── controller/      # HTTP 入口
│   ├── service/         # 业务逻辑层
│   ├── repository/      # 数据访问层
│   ├── dto/             # 数据传输对象
│   ├── entity/          # 领域实体
│   ├── common/          # 公共工具
│   └── main.ts          # 应用入口
│
├── test/
├── scripts/
└── docs/
```

#### Step 4：改写 `rules/编码规范.md`

把 Go 的语言级规范替换为 TypeScript 的。关键差异：

| Go 规范 | → TypeScript 规范 |
|--------|------------------|
| 接口首字母大写导出 | interface / type 用 PascalCase |
| error 必须处理 | 使用 `Result<T, E>` 或 try/catch，禁止吞异常 |
| goroutine 不持有可变状态 | 禁止闭包捕获可变引用做共享状态 |
| context 传超时 | AbortSignal 或 RxJS timeout |
| `go vet` 静态分析 | `tsc --noEmit` 类型检查 |
| 函数 ≤ 50 行 | 函数 ≤ 80 行（TS 表达力更弱，放宽） |

#### Step 5：在 apply-harness 加检测表

在 `skills/apply-harness/SKILL.md` 加一个「TypeScript 检测」小节：

```markdown
#### TypeScript 检测

| 检测特征 | 框架 | 构建工具 | 版本 |
|---------|------|---------|------|
| `package.json` + `@nestjs/core` 依赖 | NestJS | pnpm/npm/yarn | 读取 NestJS 版本 |
| `package.json` + `elysia` 依赖 | Elysia | pnpm/npm/yarn | 读取 Elysia 版本 |
| `package.json` + `hono` 依赖 | Hono | pnpm/npm/yarn | 读取 Hono 版本 |
| `package.json` + `fastify` 依赖（无以上框架） | Fastify | pnpm/npm/yarn | 读取 Fastify 版本 |
| `package.json`（通用，未匹配） | 询问用户 | pnpm/npm/yarn | 读取 Node 版本 |
```

#### Step 6：写参数表——先写语言基础块

```markdown
### TypeScript 基础参数（通用）

| 参数 | 值 |
|------|-----|
| `{{LANGUAGE}}` | `TypeScript` |
| `{{LANGUAGE_RUNTIME}}` | Node.js 20 LTS+ |
| `{{BUILD_TOOL}}` | pnpm / npm |
| `{{TEST_FRAMEWORK}}` | Vitest + Testing Library |
| `{{COV_TOOL}}` | Vitest -coverage (核心逻辑 ≥80%) |
| `{{LINT_TOOL}}` | ESLint + Prettier |
| `{{LANG_TAG}}` | `-typescript` |
| `{{HARNESS_ME_NAME}}` | `/harness-me-typescript` |
| `{{HARNESSING_CMD}}` | `/harnessing-typescript` |
| `{{BUILD_CMD}}` | `pnpm build` |
| `{{TEST_CMD}}` | `pnpm test` |
| `{{LINT_CMD}}` | `pnpm lint` |
| `{{COV_CMD}}` | `pnpm test -- --coverage` |
| `{{DOCSTYLE}}` | TSDoc / JSDoc |
| `{{FILE_LIMIT}}` | `400` |
| `{{TEST_NAMING}}` | `describe/it` + `should_x_when_y` |
| `{{DEBUG_TOOL}}` | Node.js Inspector / `node --inspect` |
| `{{TYPE_CHECK_CMD}}` | `pnpm typecheck`（`tsc --noEmit`） |
| `{{ASSERT_LIB}}` | `vitest expect` / `chai` |
| `{{RACE_DETECT_ARG}}` | 空（单线程语言，无竞态检测） |
| `{{DEP_CMD}}` | `pnpm install --frozen-lockfile` |
| `{{ORM_TOOL}}` | Prisma / TypeORM / Drizzle |
```

**注意**：`{{RACE_DETECT_ARG}}` = 空，因为 TypeScript 是单线程语言，没有竞态检测概念。这也是参数化的体现——不是每个语言都需要每个参数。

#### Step 7：写参数表——再写 NestJS 框架块

```markdown
### TypeScript — NestJS

| 参数 | 值 |
|------|-----|
| `{{LANGUAGE_DESC}}` | TypeScript、NestJS、模块化企业级框架 |
| `{{FRAMEWORK_VER}}` | NestJS 10.x+ |
| `{{DEV_CMD}}` | `pnpm start:dev` |
| `{{ARCH_LAYER}}` | 分层 controller → service → repository，通过依赖注入（DI）解耦 |
| `{{DB_ACCESS}}` | Prisma / TypeORM + migration |
```

如果后续要在 TypeScript 内再挂 Elysia / Hono，这里就是**分支 A** 的路子——只写差异参数，5 行搞定。

#### Step 8：端到端验收

在 NestJS 项目根目录跑：

```bash
/apply-harness     # 应自动识别为 TypeScript + NestJS
/install-skill     # 注册技能到当前 AI 工具
```

检查渲染结果（同分支 A 的验收表）。

---

### 3.3 Go 语言包 → TypeScript 语言包的改动量

| 文件 | Go（参照） | TypeScript（新增） | 改动量 |
|------|----------|------------------|--------|
| `SKILL.md` | 20 行 | 20 行 | 全部重写 |
| `rules/工程结构.md` | 300 行 | 300 行 | 全部重写 |
| `rules/编码规范.md` | 400 行 | 400 行 | 全部重写 |
| apply-harness 参数表 | 已有 Go 块 | 新增 TS 基础块（~20 行）+ NestJS 框架块（5 行） | 新增 ~25 行 |
| apply-harness 检测表 | 已有 Go 表 | 新增 TS 检测表（~6 行） | 新增 6 行 |

**总改动量：约 740 行（3 个语言包文件）+ 31 行（参数表 + 检测表）。**

---

## 四、两个分支的本质差异

把两个分支放一起看，差异一目了然：

| 维度 | 分支 A：同语言新增框架 | 分支 B：新增语言 + 框架 |
|------|---------------------|---------------------|
| **语言包** | 已存在，复用 | 不存在，从零复制改造 |
| **改动文件数** | 2-3 个（参数表 + 检测表 + SKILL.md） | 4-5 个（语言包 3 个 + 参数表 + 检测表） |
| **改动行数** | 7-50 行 | 700-1200 行 |
| **最耗时环节** | 写参数块（3 分钟） | 改写编码规范（1-2 小时） |
| **风险** | 参数值填错 | 规则骨架里藏着语言假设（技术债） |
| **后续在该语言内加框架** | 无后续 | 第一次成本高，之后同分支 A |

**关键洞察**：分支 B 的高昂成本是一次性的。TypeScript 语言包造好之后，再挂 Elysia / Hono / Fastify 都是分支 A——每个只填 5 行参数表。

---

## 五、参数表结构详解

无论走哪个分支，最终都要写参数表。这里讲清楚参数表的工作原理。

### 5.1 两层结构：基础块 + 框架块

每个语言在 apply-harness 参数表里有两层：

```
### Go 基础参数（通用）          ← 第一层：语言级共享参数（42 个中约 30 个）
### Go — Gin                    ← 第二层：框架差异（只列不同的 5-15 个）
### Go — go-zero
### Go — Echo
### Go — Fresh                  ← 新增的在这里
```

`apply-harness` 渲染时的合并策略：

```
最终参数 = 基础参数块（全量） + 框架参数块（覆盖差异）
```

所以框架块**只写与基础块不同的参数**，重复写是错误的——会被基础块覆盖。

### 5.2 42 个占位符分类

| 类别 | 占位符举例 | 数量 |
|------|----------|------|
| **身份** | `{{PROJECT_NAME}}` `{{LANGUAGE}}` `{{LANGUAGE_DESC}}` `{{FRAMEWORK_VER}}` | 4 |
| **运行时** | `{{LANGUAGE_RUNTIME}}` `{{BUILD_TOOL}}` `{{HEALTH_ENDPOINT}}` `{{METRICS_ENDPOINT}}` `{{RACE_DETECT_ARG}}` | 5 |
| **工程命令** | `{{BUILD_CMD}}` `{{TEST_CMD}}` `{{LINT_CMD}}` `{{DEV_CMD}}` `{{COV_CMD}}` `{{VET_CMD}}` `{{DEP_CMD}}` `{{TYPE_CHECK_CMD}}` | 8 |
| **工具与库** | `{{TEST_FRAMEWORK}}` `{{MOCK_LIB}}` `{{ASSERT_LIB}}` `{{DEBUG_TOOL}}` `{{LINT_TOOL}}` `{{ORM_TOOL}}` `{{ARCH_TEST_TOOL}}` | 7 |
| **约束阈值** | `{{FILE_LIMIT}}` `{{ARCH_LAYER}}` `{{TEST_NAMING}}` `{{DOCSTYLE}}` | 4 |
| **技能命令后缀** | `{{LANG_TAG}}` `{{HARNESS_ME_NAME}}` `{{HARNESSING_CMD}}` `{{ARCH_REVIEW_CMD}}` | 4 |
| **其他** | `{{DB_ACCESS}}` `{{COV_TOOL}}` `{{RUN_CMD}}` `{{SECURITY_CMD}}` `{{INTEGRATION_CMD}}` `{{ARCH_TEST_CMD}}` `{{HEALTH_CHECK_CMD}}` `{{HTTP_MOCK_UTIL}}` `{{FRAMEWORK_NAME}}` `{{TYPE_CHECK_TOOL}}` | 10 |
| **合计** | | **42 个** |

> **不要写死数字**：42 个是当前版本的数量，随版本演化。以 `skills/apply-harness/SKILL.md` 参数表实际列出的为准。

### 5.3 框架块常见的 5-7 个差异参数

新增框架时，通常只需要覆盖这几个：

```markdown
{{LANGUAGE_DESC}}     ← 框架全称描述
{{FRAMEWORK_VER}}     ← 框架版本
{{DEV_CMD}}           ← 开发服务器启动命令
{{ARCH_LAYER}}        ← 架构分层方向
{{DB_ACCESS}}         ← 数据库访问方式
{{TEST_NAMING}}       ← （可选）测试命名规范
{{HARNESSING_CMD}}    ← （可选）语言专属命令后缀
```

其他 30+ 个参数全部继承基础块，**不要重复写**。

---

## 六、常见阻力与解决方案

### 6.1 参数表写错 → 全链路渲染错误

**症状**：渲染出的 Owner Agent 里出现 `{{FRAMEWORK_VER}}` 未替换。

**根因**：参数表里没有该框架块，或占位符拼写错误（`{{FRAMEWORK_VER}}` ≠ `{{FRAMEWORK_VERSION}}`）。

**解决**：跑一遍 `apply-harness`，检查渲染产物中残留 `{{...}}` 的数量。对比已支持的框架块，确认所有参数名完全匹配。

### 6.2 检测表漏了框架 → 无法自动识别

**症状**：`/apply-harness` 提示"无法识别框架，请手动指定"。

**根因**：检测表里没有匹配该框架特征的行。

**解决**：在对应语言的检测表中加一行。特征要能唯一识别——Go 靠 go.mod 的 import 路径，Java 靠 pom.xml 的 dependency，Python 靠 `requirements.txt` 的包名。

### 6.3 规则骨架里的语言假设 → 新语言被旧规则污染

**症状**：新增 TypeScript 语言包后，某条通用规则写了"go.mod 必须存在"。

**根因**：harness-core 的规则骨架含硬编码语言假设——这是最大的技术债。

**解决**：把硬编码改为参数化：

```markdown
❌ "检查 go.mod 是否存在"
✅ "检查 {{DEP_CMD}} 对应的依赖清单文件是否存在（go.mod / pom.xml / package.json / Cargo.toml / requirements.txt）"
```

**新增语言时顺手还掉这类技术债**，不要在骨架里继续堆砌语言假设。

### 6.4 测试约定不同 → 测试命名/断言库错配

| 语言 | 测试命名 | 断言库 |
|------|---------|--------|
| Java | `method_should_x_when_y` | AssertJ |
| Go | `TestX_WhenY` | testify |
| Python | `test_x_when_y` | pytest + assert |
| TypeScript | `describe/it` + `should_x_when_y` | Vitest expect |
| Rust | `test_x_when_y` | assert_eq! |

**解决**：差异参数化进 `{{TEST_NAMING}}`、`{{TEST_FRAMEWORK}}`、`{{ASSERT_LIB}}`，不要硬编码在规则骨架里。

---

## 七、一个完整案例：从 0 到支持 Fresh 框架

走一遍分支 A 的完整流程，展示每一步的真实改动：

```diff
# 1. apply-harness 检测表
  | `go.mod` + `anyi` 依赖 | Anyi | go mod | 读取 Go 版本 |
+ | `go.mod` + `fresh` import | Fresh | go mod | 读取 Go 版本 |

# 2. apply-harness 参数表（Go 区段，Kratos 块之后）
  ### Go — Go-Kit
  ...
+ ### Go — Fresh
+
+ | 参数 | 值 |
+ |------|-----|
+ | `{{LANGUAGE_DESC}}` | Go、Fresh、边缘渲染全栈 Web 框架 |
+ | `{{FRAMEWORK_VER}}` | Fresh 0.1.x |
+ | `{{DEV_CMD}}` | `go run main.go` |
+ | `{{ARCH_LAYER}}` | 分层 route → handler → component → store，依赖单向 |
+ | `{{DB_ACCESS}}` | sqlx / GORM（Fresh 不强制 ORM，项目自选） |

# 3. harness-golang/SKILL.md
- - **基线框架**: Gin / go-zero / Echo / ...
+ - **基线框架**: Gin / go-zero / Echo / ... / Fresh / ...
```

**总 diff：8 行增加，0 行删除。**

---

## 八、操作清单

### 分支 A：同语言内新增框架

```
□ 1. 确认语言包存在（如 skills/harness-golang/）
□ 2. 在 apply-harness 检测表加一行检测特征
□ 3. 在参数表写框架参数块（只写差异参数，5-15 行）
□ 4. 本机确认命令可执行（build/test/lint）
□ 5. 在语言包 SKILL.md 基线框架清单加一项
□ 6. 跑 /apply-harness 验收，确认无残留占位符
```

### 分支 B：新增语言 + 新增框架

```
□ 1. 选参照语言包（最简洁的，如 harness-golang）
□ 2. cp -r skills/harness-golang skills/harness-{newlang}
□ 3. 改写 SKILL.md（frontmatter + 基线框架清单）
□ 4. 改写 rules/工程结构.md（目录骨架）
□ 5. 改写 rules/编码规范.md（语言级规范）
□ 6. 在 apply-harness 加「新语言 检测」表
□ 7. 在参数表写基础参数块（语言级）
□ 8. 在参数表写框架参数块（框架差异，只写不同的）
□ 9. 如有专属技能，加 skills/{newlang}/skills/ 目录
□ 10. 跑 /apply-harness 验收，确认无残留占位符
□ 11. 跑 /install-skill 注册到新 AI 工具
```

---

## 九、信条回顾

- **支持一个新框架，先判断语言包存不存在**——存在走分支 A（填表），不存在走分支 B（造包 + 填表）
- **新增框架 = 填差异参数**：5-50 行，15-30 分钟
- **新增语言 = 复制语言包 + 重写规则**：3 个文件 700+ 行，3-5 小时
- **第一次造语言包贵，之后挂框架便宜**：分支 B 是一次性成本，分支 A 是边际成本
- **参数表是框架差异的单一真相源**：只填表，不复制规则
- **规则骨架的语言假设是最大技术债**：新增语言时顺手还掉
- **不要写死占位符数量**：以 apply-harness 参数表为准

下一篇，我们聊聊**迁移实战**——把一套没有纪律的存量代码库，迁移进 Harness 约束体系。