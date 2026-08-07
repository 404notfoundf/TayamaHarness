---
name: apply-harness
description: 自动检测项目语言并应用 Harness 开发规范体系（Java / Python / Go / Frontend）
disable-model-invocation: true
---

# Apply Harness — 一键应用 Harness 开发规范体系

> 在任意项目根目录执行此命令，自动检测项目语言，生成 `.harness/` 目录（含 Owner Agent 定义、规则、技能、变更追踪、领域知识库）。

---

## 工作流程

### Step 1: 检测项目语言

扫描项目根目录，按优先级检测：

| 检测文件 | 语言 | 版本 |
|---------|------|------|
| `pom.xml`（含 `<parent>` 或 `<groupId>`） | **Java** | 读取 Java 版本 / Spring Boot 版本 |
| `go.mod` | **Go** | 读取 Go 版本 |
| `pyproject.toml` | **Python** | 读取 Python 版本 |
| `requirements.txt` | **Python** | 推断 Python 3.x |
| `setup.py` / `setup.cfg` | **Python** | 推断 Python 3.x |
| `package.json`（含 `vue` 或 `@vitejs` 或 `vite` 依赖） + `vite.config.ts` | **Frontend** | 读取 Vue 版本 / 构建工具 |
| `package.json`（含 `vue` 依赖） + `vue.config.js` | **Frontend** | 读取 Vue 版本 / Vue CLI |
| `package.json`（含 `vite` 依赖） + `index.html` + 无 `vue` 依赖 | **Frontend** | 推断为纯 Vite 前端项目 |

**多语言项目**（如同时有前端+后端）：询问用户选择当前焦点语言。

**无法识别**：询问用户手动指定语言。

### Step 2: 读取项目名称

| 语言 | 读取方式 |
|------|---------|
| Java | `pom.xml` → `<artifactId>` 或 `<name>` |
| Go | `go.mod` → `module` 后的模块名 |
| Python | `pyproject.toml` → `[project] name` 或目录名 |
| Frontend | `package.json` → `name` 字段或目录名 |

### Step 3: 渲染 owner.md（灵魂生成）

将 `harness-core/templates/agents/owner.md` 参数化渲染：

| 参数 | 说明 |
|------|------|
| `{{PROJECT_NAME}}` | 项目名称（从 Step 2 读取） |
| `{{LANGUAGE}}` | `Java` / `Python` / `Go` / `Frontend` |
| `{{LANGUAGE_DESC}}` | 语言技术栈描述（见下文参数表） |
| `{{LANGUAGE_RUNTIME}}` | 运行时版本 |
| `{{FRAMEWORK_VER}}` | 框架版本 |
| `{{BUILD_TOOL}}` | 构建工具 |
| `{{TEST_FRAMEWORK}}` | 测试框架 |
| `{{COV_TOOL}}` | 覆盖率工具 |
| `{{LINT_TOOL}}` | 代码规范检查工具 |
| `{{ARCH_TEST_TOOL}}` | 架构约束守护工具 |
| `{{DB_ACCESS}}` | 数据库访问方式 |

### Step 4: 复制规则文件

1. 从 `harness-core/rules/` 复制通用规则到 `.harness/rules/`
2. 从 `harness-{lang}/rules/` 复制语言特有规则到 `.harness/rules/`（同名覆盖）

```
.harness/rules/
├── SDD-TDD模式.md       ← 来自 harness-core（跨语言通用）
├── 开发流程规范.md       ← 来自 harness-core（跨语言通用）
├── 运行时可靠性.md       ← 来自 harness-core（后端）/ 来自 harness-front（前端特有）
├── 编码规范.md           ← 来自 harness-{lang}（语言特有）
└── 工程结构.md           ← 来自 harness-{lang}（语言特有）
```

> **注意**: 对于后端项目（Java/Python/Go），`运行时可靠性.md` 来自通用版；对于前端项目，`运行时可靠性.md` 来自 `harness-front`（前端特有版本，会覆盖通用版）。

### Step 5: 渲染技能模板 + 复制技能文件

1. **渲染模板化技能**：从 `harness-core/skills/` 读取模板 SKILL.md，替换其中的 `{{PLACEHOLDER}}` 为语言参数表中的值，写入 `.harness/skills/{lang}/`
2. **复制语言特有技能**：从 `harness-{lang}/skills/` 复制全部技能目录到 `.harness/skills/{lang}/`（同名覆盖渲染文件，语言特有内容优先）
3. **复制跨语言通用技能**：从 `harness-core/skills/` 复制 `domain-modeling`、`research`、`resolving-merge-conflicts` 到 `.harness/skills/common/`（纯通用，无需渲染）

**模板化技能清单**（以下技能的核心逻辑来自 `harness-core/skills/` 模板，语言包提供覆写）：

| 技能 | 模板位置 | 语言包覆写 |
|------|---------|-----------|
| `harness-me` | `harness-core/skills/harness-me/` | `harness-{lang}/skills/harness-me/` |
| `handoff` | `harness-core/skills/handoff/` | `harness-{lang}/skills/handoff/` |
| `diagnosing-bugs` | `harness-core/skills/diagnosing-bugs/` | `harness-{lang}/skills/diagnosing-bugs/` |
| `coding-skill` | `harness-core/skills/coding-skill/` | `harness-{lang}/skills/coding-skill/` |
| `unit-test-write` | `harness-core/skills/unit-test-write/` | `harness-{lang}/skills/unit-test-write/` |

**技能参数说明**——渲染时替换模板中的 `{{PLACEHOLDER}}`：

| 参数 | 说明 |
|------|------|
| `{{LANG_TAG}}` | 技能名称后缀，如 `-python`、`-java`、`-golang`、`-front` |
| `{{HARNESS_ME_NAME}}` | harness-me 技能名（`harness-me`/`harness-me-python`/`harness-me-golang`/`harness-me-front`） |
| `{{HARNESSING_CMD}}` | harnessing 命令，如 `/harnessing`、`/harnessing-python` |
| `{{BUILD_CMD}}` | 编译命令，如 `mvn compile`、`go build ./...` |
| `{{TEST_CMD}}` | 测试命令 |
| `{{LINT_CMD}}` | 代码规范检查命令 |
| `{{DEV_CMD}}` | 开发服务器启动命令 |
| `{{DOCSTYLE}}` | 文档注释风格，如 `Javadoc`、`docstring`、`JSDoc` |
| `{{FILE_LIMIT}}` | 单文件行数上限 |
| `{{FRAMEWORK_DESC}}` | 语言技术栈描述 |
| `{{ARCH_LAYER}}` | 架构依赖方向描述 |
| `{{TEST_NAMING}}` | 测试命名规范 |
| `{{MOCK_LIB}}` | Mock 库 |
| `{{COV_CMD}}` | 覆盖率检查命令 |
| `{{DEBUG_TOOL}}` | 调试工具 |
| `{{ARCH_REVIEW_CMD}}` | 架构审查命令，如 `/arch-review`、`/arch-review-python` |

```
.harness/skills/
├── {lang}/                # 语言特有技能
│   ├── coding-skill/      # 编码实现
│   ├── unit-test-write/   # 单测编写
│   ├── expert-reviewer/   # 专家评审
│   ├── unit-test-ci/      # CI 门禁
│   ├── deploy-verify/     # 部署验证
│   ├── harness-me/        # 需求打磨（可选辅助）
│   ├── harnessing/        # 需求拷问引擎（可选辅助）
│   ├── diagnosing-bugs/   # Bug 诊断（可选辅助）
│   ├── handoff/           # 上下文交接（可选辅助）
│   └── arch-review/       # 架构体检（可选辅助）
└── common/                # 跨语言通用技能
    ├── domain-modeling/           # 领域语言维护（术语敲定当场写 CONTEXT.md / ADR）
    ├── research/                  # 外部事实查证（一手来源，结果落 wiki）
    └── resolving-merge-conflicts/ # 解决 git merge/rebase 冲突
```

### Step 6: 初始化变更追踪

从 `harness-core/templates/changes/` 复制到 `.harness/changes/`：

```
.harness/changes/
└── _TEMPLATE/
    ├── change.md      ← 变更卡模板
    ├── review.md      ← 评审报告模板
    └── verify.md      ← 部署验证报告模板
```

### Step 7: 初始化领域知识库

从 `harness-core/templates/wiki/` 复制到 `.harness/wiki/`：

```
.harness/wiki/
├── 业务模型.md     ← 业务结构与关系模板，待补充
├── 接口协议.md     ← 接口契约模板，待补充
├── 数据模型.md     ← 数据模型模板，待补充
├── 架构决策.md     ← ADR 记录模板，待补充
└── ADR-FORMAT.md   ← ADR 编写规范（什么值得写 / 怎么写）
```

### Step 7.1: 初始化共享语言上下文

从 `harness-core/templates/CONTEXT.md` 复制到 `.harness/CONTEXT.md`，从 `harness-core/templates/CONTEXT-FORMAT.md` 复制到 `.harness/CONTEXT-FORMAT.md`：

```
.harness/
├── CONTEXT.md          ← 领域语言术语表（活词典）
└── CONTEXT-FORMAT.md   ← CONTEXT.md 编写规范
```

> **CONTEXT.md 的作用**：AI 与人类之间的共享语言机制，记录项目特有的领域术语、缩写、决策、约定。由 `domain-modeling` 技能在对话中主动维护——术语敲定后**当场写**，不批量累积。每次对话结束时，AI 应检查是否有新术语/决策需要补充。
> **CONTEXT-FORMAT.md 的作用**：定义 CONTEXT.md 的编写规范（只存术语、要有主见、定义克制、只收项目特有概念）。

### Step 8: 输出项目摘要卡片

```
╔══════════════════════════════════════════╗
║   ✅ Harness 规范已应用到 <项目名>       ║
╠══════════════════════════════════════════╣
║  语言:   Java / Python / Go / Frontend   ║
║  框架:   Spring Boot 4.0.x / Flask 3.x   ║
║  规则:   5 个已就绪（3 通用 + 2 语言特有）   ║
║  技能:   12 个已就绪（9 语言特有 + 3 通用）║
║  Owner:  已就绪                          ║
╠══════════════════════════════════════════╣
║  下一步: 创建你的第一个变更              ║
║  /request-analysis "一句话描述需求"      ║
╚══════════════════════════════════════════╝
```

---

## 参数表

### Java 参数

| 参数 | 值 |
|------|-----|
| `{{LANGUAGE}}` | `Java` |
| `{{LANGUAGE_DESC}}` | Java、Spring Boot、多模块 Maven 架构 |
| `{{LANGUAGE_RUNTIME}}` | JDK 21 LTS |
| `{{FRAMEWORK_VER}}` | Spring Boot 3.x+ |
| `{{BUILD_TOOL}}` | Maven 3.9+ |
| `{{TEST_FRAMEWORK}}` | JUnit 5 + Mockito + AssertJ |
| `{{COV_TOOL}}` | JaCoCo (核心逻辑 ≥80%) |
| `{{LINT_TOOL}}` | Checkstyle + PMD + SpotBugs |
| `{{ARCH_TEST_TOOL}}` | ArchUnit |
| `{{DB_ACCESS}}` | MyBatis-Plus / JPA + Flyway |
| `{{LANG_TAG}}` | `-java` |
| `{{HARNESS_ME_NAME}}` | `harness-me` |
| `{{HARNESSING_CMD}}` | `/harnessing` |
| `{{BUILD_CMD}}` | `mvn compile` |
| `{{TEST_CMD}}` | `mvn test` |
| `{{LINT_CMD}}` | `mvn checkstyle:check` |
| `{{DEV_CMD}}` | `mvn spring-boot:run` |
| `{{DOCSTYLE}}` | `Javadoc` |
| `{{FILE_LIMIT}}` | `500` |
| `{{ARCH_LAYER}}` | 模块间仅通过接口通信，依赖方向 `common ← service ← server` |
| `{{TEST_NAMING}}` | `method_should_x_when_y` |
| `{{MOCK_LIB}}` | `Mockito` |
| `{{COV_CMD}}` | `mvn jacoco:report` |
| `{{DEBUG_TOOL}}` | 调试器 / `jdb` |
| `{{ARCH_REVIEW_CMD}}` | `/arch-review` |

### Python 参数

| 参数 | 值 |
|------|-----|
| `{{LANGUAGE}}` | `Python` |
| `{{LANGUAGE_DESC}}` | Python、Flask/FastAPI、SQLAlchemy |
| `{{LANGUAGE_RUNTIME}}` | Python 3.11+ |
| `{{FRAMEWORK_VER}}` | Flask 3.x / FastAPI 0.110+ |
| `{{BUILD_TOOL}}` | pip + virtualenv / poetry |
| `{{TEST_FRAMEWORK}}` | pytest + pytest-mock |
| `{{COV_TOOL}}` | pytest-cov (核心逻辑 ≥80%) |
| `{{LINT_TOOL}}` | flake8 + mypy + black + isort |
| `{{ARCH_TEST_TOOL}}` | 自定义 import-lint 检查 |
| `{{DB_ACCESS}}` | SQLAlchemy + Alembic |
| `{{LANG_TAG}}` | `-python` |
| `{{HARNESS_ME_NAME}}` | `harness-me-python` |
| `{{HARNESSING_CMD}}` | `/harnessing-python` |
| `{{BUILD_CMD}}` | `python -m compileall .` |
| `{{TEST_CMD}}` | `python -m pytest` |
| `{{LINT_CMD}}` | `flake8 .` |
| `{{DEV_CMD}}` | `flask run` / `uvicorn main:app` |
| `{{DOCSTYLE}}` | `docstring` |
| `{{FILE_LIMIT}}` | `800` |
| `{{ARCH_LAYER}}` | 依赖方向 `router → handler → service → model` |
| `{{TEST_NAMING}}` | `test_x_when_y` |
| `{{MOCK_LIB}}` | `pytest-mock` |
| `{{COV_CMD}}` | `pytest --cov` |
| `{{DEBUG_TOOL}}` | `pdb` / `breakpoint()` / `ipdb` |
| `{{ARCH_REVIEW_CMD}}` | `/arch-review-python` |

### Go 参数

| 参数 | 值 |
|------|-----|
| `{{LANGUAGE}}` | `Go` |
| `{{LANGUAGE_DESC}}` | Go、go-zero/Gin、gRPC 微服务架构 |
| `{{LANGUAGE_RUNTIME}}` | Go 1.22+ |
| `{{FRAMEWORK_VER}}` | go-zero 1.9.x / Gin 1.10+ |
| `{{BUILD_TOOL}}` | go mod |
| `{{TEST_FRAMEWORK}}` | go test + testify |
| `{{COV_TOOL}}` | go test -cover (核心逻辑 ≥80%) |
| `{{LINT_TOOL}}` | golangci-lint (go vet + staticcheck) |
| `{{ARCH_TEST_TOOL}}` | goimports + 自定义架构检查 |
| `{{DB_ACCESS}}` | GORM + goctl model |
| `{{LANG_TAG}}` | `-golang` |
| `{{HARNESS_ME_NAME}}` | `harness-me-golang` |
| `{{HARNESSING_CMD}}` | `/harnessing-golang` |
| `{{BUILD_CMD}}` | `go build ./...` |
| `{{TEST_CMD}}` | `go test ./...` |
| `{{LINT_CMD}}` | `golangci-lint run` |
| `{{DEV_CMD}}` | `go run` |
| `{{DOCSTYLE}}` | Go 注释 |
| `{{FILE_LIMIT}}` | `800` |
| `{{ARCH_LAYER}}` | 服务间仅通过 RPC 通信，依赖方向 `common → models → rpc → api` |
| `{{TEST_NAMING}}` | `TestX_WhenY` |
| `{{MOCK_LIB}}` | `gomock` / `testify` |
| `{{COV_CMD}}` | `go test -cover` |
| `{{DEBUG_TOOL}}` | `delve` |
| `{{ARCH_REVIEW_CMD}}` | `/arch-review` |

### Frontend 参数

| 参数 | 值 |
|------|-----|
| `{{LANGUAGE}}` | `Frontend` |
| `{{LANGUAGE_DESC}}` | Vue 3、Vite、Pinia、Vue Router、TypeScript |
| `{{LANGUAGE_RUNTIME}}` | Node.js 20+ |
| `{{FRAMEWORK_VER}}` | Vue 3.x+ / Vite 8.x+ |
| `{{BUILD_TOOL}}` | npm / pnpm / yarn |
| `{{TEST_FRAMEWORK}}` | Vitest + @vue/test-utils + jsdom |
| `{{COV_TOOL}}` | Vitest --coverage (核心逻辑 ≥80%) |
| `{{LINT_TOOL}}` | ESLint + Prettier + vue-tsc |
| `{{ARCH_TEST_TOOL}}` | ESLint 扁平配置 + 自定义 import 检查 |
| `{{DB_ACCESS}}` | API 请求（Axios 封装） |
| `{{LANG_TAG}}` | `-front` |
| `{{HARNESS_ME_NAME}}` | `harness-me-front` |
| `{{HARNESSING_CMD}}` | `/harnessing` |
| `{{BUILD_CMD}}` | `npm run build` |
| `{{TEST_CMD}}` | `npm run test:unit` |
| `{{LINT_CMD}}` | `npm run lint` |
| `{{DEV_CMD}}` | `npm run dev` |
| `{{DOCSTYLE}}` | `JSDoc` |
| `{{FILE_LIMIT}}` | `500` |
| `{{ARCH_LAYER}}` | 组件/模块放入正确位置，无循环依赖 |
| `{{TEST_NAMING}}` | `should_x_when_y` |
| `{{MOCK_LIB}}` | `vi.mock` |
| `{{COV_CMD}}` | `npm run coverage` |
| `{{DEBUG_TOOL}}` | DevTools / `console` |
| `{{ARCH_REVIEW_CMD}}` | `/arch-review` |

---

## 约束

- ❌ 禁止修改已存在的 `.harness/` 内容（除非用户明确要求覆盖）
- ❌ 禁止在检测到多语言时擅自选择
- ❌ 禁止跳过 Step 1 直接使用默认语言
- ✅ 如果 `.harness/` 已存在，输出提示并询问是否覆盖
- ✅ 每个步骤完成后输出简要状态