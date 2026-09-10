---
name: apply-harness
description: 自动检测项目语言与框架（Java / Python / Go / Rust / Frontend，支持 Spring Boot / Quarkus / Django / Gin / Axum / React / Vue 等主流框架）并应用 Harness 开发规范体系
disable-model-invocation: true
---

# Apply Harness — 一键应用 Harness 开发规范体系

> 在任意项目根目录执行此命令，自动检测项目语言、框架与构建工具，生成 `.harness/` 目录（含 Owner Agent 定义、规则、技能、变更追踪、领域知识库）。

---

## 工作流程

### Step 1: 检测项目语言与框架

扫描项目根目录，按优先级检测。先识别语言，再从语言特征中识别具体框架和构建工具。

#### Java 检测

| 检测特征 | 框架 | 构建工具 | 版本 |
|---------|------|---------|------|
| `pom.xml` + `spring-boot-starter-parent` 或 `spring-boot-maven-plugin` | Spring Boot | Maven | 读取 Spring Boot 版本 |
| `pom.xml` + `quarkus-maven-plugin` 或 `quarkus` BOM | Quarkus | Maven | 读取 Quarkus 版本 |
| `pom.xml` + `micronaut-parent` 或 `micronaut-maven-plugin` | Micronaut | Maven | 读取 Micronaut 版本 |
| `pom.xml` + `vertx` 依赖 | Vert.x | Maven | 读取 Vert.x 版本 |
| `pom.xml` + `dropwizard` 核心依赖 | Dropwizard | Maven | 读取 Dropwizard 版本 |
| `pom.xml` + `spring-webmvc`（无 spring-boot-starter） | Spring MVC | Maven | 读取 Spring 版本 |
| `pom.xml` + `dubbo`（`dubbo-spring-boot-starter` 等） | Dubbo | Maven | 读取 Dubbo 版本 |
| `pom.xml` + `spring-cloud-alibaba` 依赖 | Spring Cloud Alibaba | Maven | 读取版本 |
| `pom.xml` + `spring-ai-alibaba` 依赖 | Spring AI Alibaba | Maven | 读取版本 |
| `pom.xml` + `spring-ai` 依赖（无 alibaba） | Spring AI | Maven | 读取 Spring AI 版本 |
| `pom.xml` + `agentscope` 依赖 | AgentScope Java | Maven | 读取版本 |
| `pom.xml` + `langchain4j` 依赖 | LangChain4j | Maven | 读取版本 |
| `pom.xml` + `semantic-kernel` 依赖 | Semantic Kernel | Maven | 读取版本 |
| `pom.xml` + `genkit` 依赖 | Genkit Java | Maven | 读取版本 |
| `pom.xml`（通用，未匹配以上） | 询问用户 | Maven | 读取 Java 版本 |
| `build.gradle(.kts)` + `spring-boot` plugin | Spring Boot | Gradle | 读取 Spring Boot 版本 |
| `build.gradle(.kts)` + `quarkus` plugin | Quarkus | Gradle | 读取 Quarkus 版本 |
| `build.gradle(.kts)` + `micronaut` plugin | Micronaut | Gradle | 读取 Micronaut 版本 |
| `build.gradle(.kts)`（通用，未匹配以上） | 询问用户 | Gradle | Gradle 版本 |

#### Python 检测

| 检测特征 | 框架 | 构建工具 | 版本 |
|---------|------|---------|------|
| `pyproject.toml` + `django` 依赖 | Django | pip/poetry/uv | 读取 Python 版本 |
| `pyproject.toml` + `fastapi` 依赖 | FastAPI | pip/poetry/uv | 读取 Python 版本 |
| `pyproject.toml` + `flask` 依赖 | Flask | pip/poetry/uv | 读取 Python 版本 |
| `pyproject.toml`（通用，未匹配以上） | 询问用户 | pip/poetry/uv | 读取 Python 版本 |
| `requirements.txt` + `django` | Django | pip | 推断 Python 3.x |
| `requirements.txt` + `fastapi` | FastAPI | pip | 推断 Python 3.x |
| `requirements.txt` + `flask` | Flask | pip | 推断 Python 3.x |
| `pyproject.toml` / `requirements.txt` + `tornado` | Tornado | pip/poetry/uv | 推断/读取 Python 版本 |
| `requirements.txt` + `tensorflow` | TensorFlow | pip | 推断 Python 3.x |
| `requirements.txt` + `torch` | PyTorch | pip | 推断 Python 3.x |
| `requirements.txt` + `keras` | Keras | pip | 推断 Python 3.x |
| `requirements.txt` + `scikit-learn` | scikit-learn | pip | 推断 Python 3.x |
| `requirements.txt` + `xgboost` | XGBoost | pip | 推断 Python 3.x |
| `requirements.txt` + `transformers` | Hugging Face Transformers | pip | 推断 Python 3.x |
| `requirements.txt` + `langchain` | LangChain | pip | 推断 Python 3.x |
| `requirements.txt` + `langgraph` | LangGraph | pip | 推断 Python 3.x |
| `requirements.txt` + `crewai` | CrewAI | pip | 推断 Python 3.x |
| `requirements.txt` + `pydantic-ai` | PydanticAI | pip | 推断 Python 3.x |
| `requirements.txt` + `smolagents` | SmolAgents | pip | 推断 Python 3.x |
| `requirements.txt` + `openai-agents` | OpenAI Agents SDK | pip | 推断 Python 3.x |
| `requirements.txt`（通用，未匹配） | 询问用户 | pip | 推断 Python 3.x |
| `setup.py` / `setup.cfg` | 询问用户 | pip | 推断 Python 3.x |
| `manage.py` + `django` 已安装 | Django | pip | 推断 Python 3.x |

#### Go 检测

| 检测特征 | 框架 | 构建工具 | 版本 |
|---------|------|---------|------|
| `go.mod` + `gin` import | Gin | go mod | 读取 Go 版本 |
| `go.mod` + `go-zero` 依赖 | go-zero | go mod | 读取 Go 版本 |
| `go.mod` + `echo` 依赖 | Echo | go mod | 读取 Go 版本 |
| `go.mod` + `fiber` 依赖 | Fiber | go mod | 读取 Go 版本 |
| `go.mod` + `chi` 依赖 | Chi | go mod | 读取 Go 版本 |
| `go.mod` + `beego` 依赖 | Beego | go mod | 读取 Go 版本 |
| `go.mod` + `go-kit` 依赖 | Go-Kit | go mod | 读取 Go 版本 |
| `go.mod` + `kratos` 依赖 | Go-Kratos | go mod | 读取 Go 版本 |
| `go.mod` + `gorilla/mux` 依赖 | Gorilla Mux | go mod | 读取 Go 版本 |
| `go.mod` + `kitex` 依赖 | Kitex | go mod | 读取 Go 版本 |
| `go.mod` + `hertz` 依赖 | Hertz | go mod | 读取 Go 版本 |
| `go.mod` + `iris` 依赖 | Iris | go mod | 读取 Go 版本 |
| `go.mod` + `macaron` 依赖 | Macaron | go mod | 读取 Go 版本 |
| `go.mod` + `tango` 依赖 | Tango | go mod | 读取 Go 版本 |
| `go.mod` + `goframe` 依赖 | goframe | go mod | 读取 Go 版本 |
| `go.mod` + `langchaingo` 依赖 | LangChainGo | go mod | 读取 Go 版本 |
| `go.mod` + `adk-go` 依赖 | Google ADK-Go | go mod | 读取 Go 版本 |
| `go.mod` + `cloudwego/eino` 依赖 | eino（cloudwego） | go mod | 读取 Go 版本 |
| `go.mod` + `trpc-agent` 依赖 | tRPC-Agent-Go | go mod | 读取 Go 版本 |
| `go.mod` + `genkit` 依赖 | Firebase Genkit | go mod | 读取 Go 版本 |
| `go.mod` + `anyi` 依赖 | Anyi | go mod | 读取 Go 版本 |
| `go.mod`（通用，未匹配以上） | 询问用户 | go mod | 读取 Go 版本 |

#### Frontend 检测

| 检测特征 | 框架 | 构建工具 | 版本 |
|---------|------|---------|------|
| `package.json` + `vue` + `vite` 或 `vite.config.ts` | Vue 3 + Vite | npm/pnpm/yarn | 读取 Vue 版本 |
| `package.json` + `vue` + `vue.config.js` | Vue 3 + Vue CLI | npm/pnpm/yarn | 读取 Vue 版本 |
| `package.json` + `nuxt` 或 `nuxt.config.ts` | Nuxt | npm/pnpm/yarn | 读取 Nuxt 版本 |
| `package.json` + `react` + `next` 或 `next.config` | Next.js | npm/pnpm/yarn | 读取 Next.js 版本 |
| `package.json` + `react` + `vite` | React + Vite | npm/pnpm/yarn | 读取 React 版本 |
| `package.json` + `react` + `react-scripts`（CRA） | React + CRA | npm/pnpm/yarn | 读取 React 版本 |
| `package.json` + `@angular/core` 或 `angular.json` | Angular | npm/pnpm/yarn | 读取 Angular 版本 |
| `package.json` + `svelte` + `vite` | Svelte + Vite | npm/pnpm/yarn | 读取 Svelte 版本 |
| `package.json`（含前端框架，未匹配以上） | 询问用户 | npm/pnpm/yarn | 读取 Node 版本 |
| `package.json`（仅有 `vite`）+ `index.html` + 无前端框架 | 纯 Vite | npm/pnpm/yarn | 推断为纯 Vite 项目 |

#### Rust 检测

| 检测特征 | 框架 | 构建工具 | 版本 |
|---------|------|---------|------|
| `Cargo.toml` + `axum` 依赖 | Axum | cargo | 读取 workspace/package 版本 |
| `Cargo.toml` + `actix-web` 依赖 | Actix Web | cargo | 读取版本 |
| `Cargo.toml` + `rocket` 依赖 | Rocket | cargo | 读取版本 |
| `Cargo.toml` + `warp` 依赖 | Warp | cargo | 读取版本 |
| `Cargo.toml` + `poem` 依赖 | Poem | cargo | 读取版本 |
| `Cargo.toml` + `loco-rs` 依赖 | Loco | cargo | 读取版本 |
| `Cargo.toml` + `salvo` 依赖 | Salvo | cargo | 读取版本 |
| `Cargo.toml` + `candle-core` 依赖 | Candle | cargo | 读取版本 |
| `Cargo.toml` + `burn` 依赖 | Burn | cargo | 读取版本 |
| `Cargo.toml` + `tch` 依赖 | tch-rs | cargo | 读取版本 |
| `Cargo.toml` + `ort` 依赖 | ort | cargo | 读取版本 |
| `Cargo.toml` + `rlx-models` 依赖 | rlx-models | cargo | 读取版本 |
| `Cargo.toml` + `adk` / `google-adk` 依赖 | ADK-Rust | cargo | 读取版本 |
| `Cargo.toml` + `blockcell` 依赖 | Blockcell | cargo | 读取版本 |
| `Cargo.toml` + `vllm` 依赖 | vLLM（Rust） | cargo | 读取版本 |
| `Cargo.toml` + `tauri` 依赖 | Tauri | cargo | 读取版本 |
| `Cargo.toml` + `iced` 依赖 | Iced | cargo | 读取版本 |
| `Cargo.toml` + `egui` / `eframe` 依赖 | egui | cargo | 读取版本 |
| `Cargo.toml` + `dioxus` 依赖 | Dioxus | cargo | 读取版本 |
| `Cargo.toml`（通用，未匹配以上） | 询问用户 | cargo | 读取 rust-toolchain / Cargo.toml edition |

#### 多语言项目处理

同时有前端+后端（如 `package.json` + `pom.xml` / `go.mod` / `pyproject.toml` / `Cargo.toml`）：询问用户选择当前焦点语言。

#### 无法识别

未匹配任何检测特征：询问用户手动指定语言、框架和构建工具。

---

### Step 2: 读取项目名称

| 语言 | 读取方式 |
|------|---------|
| Java (Maven) | `pom.xml` → `<artifactId>` 或 `<name>` |
| Java (Gradle) | `settings.gradle(.kts)` → `rootProject.name` 或目录名 |
| Go | `go.mod` → `module` 后的模块名 |
| Rust | `Cargo.toml` → `[package] name` 或目录名 |
| Python | `pyproject.toml` → `[project] name` 或目录名 |
| Frontend | `package.json` → `name` 字段或目录名 |

### Step 3: 渲染 owner.md（灵魂生成）

将 `harness-core/templates/agents/owner.md` 参数化渲染。根据 Step 1 检测到的（语言、框架、构建工具）三元组，从下文「参数表」中选择对应的框架参数块。对于未匹配的框架（用户手动指定的），询问用户确认各参数值。

| 参数 | 说明 |
|------|------|
| `{{PROJECT_NAME}}` | 项目名称（从 Step 2 读取） |
| `{{LANGUAGE}}` | `Java` / `Python` / `Go` / `Rust` / `Frontend` |
| `{{LANGUAGE_DESC}}` | 语言技术栈描述（见下文参数表，按检测到的框架选择） |
| `{{LANGUAGE_RUNTIME}}` | 运行时版本 |
| `{{FRAMEWORK_VER}}` | 框架版本（从检测到的框架参数块读取） |
| `{{BUILD_TOOL}}` | 构建工具（从检测到的框架参数块读取） |
| `{{TEST_FRAMEWORK}}` | 测试框架（从检测到的框架参数块读取） |
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
├── 变更定位规则.md       ← 来自 harness-core（跨语言通用）
├── 知识库治理规则.md     ← 来自 harness-core（跨语言通用）
├── 运行时可靠性.md       ← 来自 harness-core（后端）/ 来自 harness-front（前端特有）
├── 编码规范.md           ← 来自 harness-{lang}（语言特有）
└── 工程结构.md           ← 来自 harness-{lang}（语言特有）
```

> **注意**: 对于后端项目（Java/Python/Go/Rust），`运行时可靠性.md` 来自通用版；对于前端项目，`运行时可靠性.md` 来自 `harness-front`（前端特有版本，会覆盖通用版）。

### Step 5: 渲染技能模板 + 复制技能文件

1. **渲染模板化技能**：从 `harness-core/skills/` 读取 10 个流水线技能模板 SKILL.md，替换其中的 `{{PLACEHOLDER}}` 为语言参数表中对应框架参数块的值，写入 `.harness/skills/{lang}/`（唯一事实源，清单见下方「模板化技能清单」）
2. **复制语言包专属技能**：若 `harness-{lang}/skills/` 下存在**专属技能**（语言/框架特有、core 无模板，如 java 的 `java-code-review`、`spring-api-convention`、`mybatis-toolkit`、`openfeign-toolkit`），将其目录复制到 `.harness/skills/{lang}/`，并替换其中的 `{{PLACEHOLDER}}`（流水线技能一律以 core 模板渲染为准，语言包不再提供同名覆写）
3. **复制跨语言通用技能**：从 `harness-core/skills/` 复制以下技能到 `.harness/skills/common/`（纯通用，无需渲染）：
    - `domain-modeling` — 领域语言维护
    - `research` — 外部事实查证
    - `resolving-merge-conflicts` — 合并冲突解决
    - `redis-cache-wrapper` — 多级缓存封装
    - `database-migration-toolkit` — 数据库迁移工具
    - `kafka-toolkit` — Kafka 工具封装
    - `k8s-release-toolkit` — K8s 发布工具
    - `performance-toolkit` — 性能诊断工具
    - `security-toolkit` — 安全工具封装
    - `rocketmq-toolkit` — RocketMQ 工具封装
    - `http-client-toolkit` — HTTP 客户端工具封装
    - `logging-toolkit` — 日志工具封装
    - `scheduler-toolkit` — 分布式调度工具封装
    - `oss-toolkit` — 对象存储工具封装
    - `excel-toolkit` — Excel 工具封装
    - `eventbus-toolkit` — 事件总线工具封装
    - `harness-status` — 变更状态总览（聚合 changes 进度/待办）
    - `harness-loop-run` — 自主收敛循环引擎（编码→测试→评审到 ci）
    - `harness-quality` — 质量闸门与报告（真实 check + flow→test + 人签字）
    - `harness-relate` — 变更关系链管理（六类关系 + impact 影响分析）
    - `harness-e2e` — 端到端测试（主链路/降级链路）
    - `harness-db-design` — 数据库设计（表结构/索引/迁移设计）
    - `harness-api-mock` — API Mock 数据生成
    - `harness-refactor` — 重构（行为不变、测试保护、小步）
    - `harness-retro` — 迭代复盘（历史趋势报告）
    - `harness-ship` — 发布工作流（版本/tag/CI/CD，留人确认）
    - `harness-changelog` — 变更日志（可追溯条目）
    - `harness-standard` — 规范库管理（新增/修订/归档/审批）
    - `legacy-bootstrap` — 老系统冷启动（submodule 关联 src/、清洗 SKILL、采访稿）
    - `knowledge-health` — 知识健康检查（重复/冲突/失效链接/无来源结论）

4. **清理残留技能目录**：删除 `.harness/skills/{lang}/` 下在本仓库中已不存在的旧技能目录（如旧版本语言包中的 `agents/`、旧版专属技能等），确保 `.harness/skills/` 仅包含当前渲染的技能，无残留文件干扰。

**模板化技能清单**（10 个流水线技能的核心逻辑均来自 `harness-core/skills/` 模板，语言差异通过 `{{PLACEHOLDER}}` 表达；语言包不再维护同名副本）：

| 技能 | 模板位置 |
|------|---------|
| `arch-review` | `harness-core/skills/arch-review/` |
| `coding-skill` | `harness-core/skills/coding-skill/` |
| `deploy-verify` | `harness-core/skills/deploy-verify/` |
| `diagnosing-bugs` | `harness-core/skills/diagnosing-bugs/` |
| `expert-reviewer` | `harness-core/skills/expert-reviewer/` |
| `handoff` | `harness-core/skills/handoff/` |
| `harness-me` | `harness-core/skills/harness-me/` |
| `harnessing` | `harness-core/skills/harnessing/` |
| `unit-test-ci` | `harness-core/skills/unit-test-ci/` |
| `unit-test-write` | `harness-core/skills/unit-test-write/` |

**技能参数说明**——渲染时替换模板中的 `{{PLACEHOLDER}}`：

| 参数 | 说明                                                                                       |
|------|------------------------------------------------------------------------------------------|
| `{{LANG_TAG}}` | 技能名称后缀，如 `-python`、`-java`、`-golang`、`-rust`、`-front` |                                            |
| `{{HARNESS_ME_NAME}}` | harness-me 的 `name`（不含 `/`：`harness-me` / `harness-me-python` / `harness-me-golang` / `harness-me-rust` / `harness-me-front`） |
| `{{HARNESSING_CMD}}` | harnessing 的 `name`（不含 `/`：`harnessing`、`harnessing-python`） |
| `{{BUILD_CMD}}` | 编译命令，如 `mvn compile`、`go build ./...`                                                    |
| `{{TEST_CMD}}` | 测试命令                                                                                     |
| `{{LINT_CMD}}` | 代码规范检查命令                                                                                 |
| `{{DEV_CMD}}` | 开发服务器启动命令                                                                                |
| `{{DOCSTYLE}}` | 文档注释风格，如 `Javadoc`、`docstring`、`JSDoc`                                                   |
| `{{FILE_LIMIT}}` | 单文件行数上限                                                                                  |
| `{{ARCH_LAYER}}` | 架构依赖方向描述                                                                                 |
| `{{TEST_NAMING}}` | 测试命名规范                                                                                   |
| `{{MOCK_LIB}}` | Mock 库                                                                                   |
| `{{COV_CMD}}` | 覆盖率检查命令                                                                                  |
| `{{DEBUG_TOOL}}` | 调试工具                                                                                     |
| `{{ARCH_REVIEW_CMD}}` | 架构审查技能 `name`（不含 `/`：`arch-review`、`arch-review-python`） |
| `{{LANGUAGE}}` | 语言名，如 `Java`、`Python`、`Go`、`Rust`、`Frontend`                                  |
| `{{TEST_FRAMEWORK}}` | 测试框架，如 `JUnit 5 + Mockito`                                                          |
| `{{LINT_TOOL}}` | 代码规范检查工具，如 `Checkstyle + PMD`                                                      |
| `{{RACE_DETECT_ARG}}` | 竞态检测参数（无竞态检测能力的语言为 `空`）                                                    |
| `{{VET_CMD}}` | 静态分析命令，如 `go vet ./...`                                                              |
| `{{ASSERT_LIB}}` | 断言库，如 `AssertJ`                                                                          |
| `{{ARCH_TEST_CMD}}` | 架构约束检查命令                                                                              |
| `{{SECURITY_CMD}}` | 安全扫描命令                                                                                |
| `{{INTEGRATION_CMD}}` | 集成测试命令                                                                                |
| `{{RUN_CMD}}` | 启动服务命令                                                                                |
| `{{HEALTH_CHECK_CMD}}` | 健康检查命令                                                                                |
| `{{METRICS_ENDPOINT}}` | 指标端点                                                                                  |
| `{{DEP_CMD}}` | 依赖管理/编译前置命令，如 `mvn dependency:tree`                                                  |

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
    ├── resolving-merge-conflicts/ # 解决 git merge/rebase 冲突
    ├── redis-cache-wrapper/       # 多级缓存封装（穿透/击穿/雪崩防护 + 分布式锁）
    ├── database-migration-toolkit/ # 数据库迁移工具封装（迁移/回滚/数据回填）
    ├── kafka-toolkit/             # Kafka 工具封装（消费者/DLQ/生产者/Lag 监控）
    ├── k8s-release-toolkit/       # K8s 发布工具封装（Deployment/HPA/探针/灰度/回滚）
    ├── performance-toolkit/       # 性能诊断工具封装（火焰图/GC/慢SQL/线程 dump）
    └── security-toolkit/          # 安全工具封装（脱敏/加密/输入校验/鉴权/日志脱敏）
    ├── rocketmq-toolkit/          # RocketMQ 工具封装（事务消息/顺序消息/DLQ）
    ├── http-client-toolkit/       # HTTP 客户端工具封装（连接池/超时/重试/熔断）
    ├── logging-toolkit/           # 日志工具封装（traceId/MDC/脱敏/动态级别）
    ├── scheduler-toolkit/         # 分布式调度工具封装（分布式锁/幂等/补偿）
    ├── oss-toolkit/               # 对象存储工具封装（统一接口/分片上传/预签名）
    ├── excel-toolkit/             # Excel 工具封装（模板导出/大数据量/导入校验）
    └── eventbus-toolkit/          # 事件总线工具封装（同步/异步/事务事件）
    ├── harness-status/            # 变更状态总览（聚合 changes 进度/待办）
    ├── harness-loop-run/          # 自主收敛循环引擎（编码→测试→评审到 ci）
    ├── harness-quality/           # 质量闸门与报告（真实 check + flow→test + 人签字）
    ├── harness-relate/            # 变更关系链管理（六类关系 + impact 影响分析）
    ├── harness-e2e/               # 端到端测试（主链路/降级链路）
    ├── harness-db-design/         # 数据库设计（表结构/索引/约束/迁移设计）
    ├── harness-api-mock/          # API Mock 数据生成（忠于契约）
    ├── harness-refactor/          # 重构（行为不变、测试保护、小步）
    ├── harness-retro/             # 迭代复盘（历史留档聚合趋势报告）
    ├── harness-ship/              # 发布工作流（版本/tag/CI/CD，留人确认）
    ├── harness-changelog/         # 变更日志（可追溯条目）
    ├── harness-standard/          # 规范库管理（新增/修订/归档/审批）
    ├── legacy-bootstrap/          # 老系统冷启动（submodule 关联 src/、清洗 SKILL、采访稿）
    └── knowledge-health/          # 知识健康检查（重复/冲突/失效链接/无来源结论）
```

### Step 5.5: 检测当前 AI 工具并生成技能目录

> 将 `.harness/skills/` 下的技能注册到当前 AI 工具可识别的技能目录，使 `/harnessing`、`/harness-me` 等斜杠命令立即可用。

**本步骤是必需步骤，非可选。** 若检测失败或目录不存在，**必须创建目录**并注册技能。

#### 5.5.1 检测当前 AI 工具

按优先级检测当前运行的 AI 工具。检测顺序：**先检查环境变量（精确），再检查目录存在（模糊）。**

| 优先级 | 检测条件 | 推断工具 | 目标技能目录 |
|--------|---------|---------|-------------|
| 1 | `REASONIX` 环境变量存在 或 `.reasonix/` 目录存在 | **Reasonix** | `.reasonix/skills/` |
| 2 | `CLAUDE_CODE` 环境变量存在 或 `.claude/` 目录存在 | **Claude Code** | `.claude/skills/` |
| 3 | `CLINE` 环境变量存在 或 `.cline/` 目录存在 | **Cline / Roo Code** | `.cline/skills/` |
| 4 | `.cursor/` 目录存在 | **Cursor** | `.cursor/skills/` |
| 5 | `OPENAI_API_KEY` 环境变量存在 或 `.codex/` 目录存在 | **Codex (OpenAI)** | `.codex/skills/` |
| 6 | `.qoder/` 目录存在 | **Qoder** | `.qoder/skills/` |
| 7 | `.vscode/` 目录存在（VS Code 1.98+） | **VS Code Agent Skills** | `.vscode/agent-skills/` |
| 8 | `.windsurf/` 目录存在 | **Windsurf** | `.windsurf/workflows/` |
| 9 | `.continue/` 目录存在 | **Continue.dev** | `.continue/skills/` |
| 10 | `.github/` 目录存在 | **GitHub Copilot** | `.github/skills/` |
| 11 | `.opencode/` 目录存在 | **OpenCode** | `.opencode/commands/` |
| 12 | `.trae/` 目录存在 | **Trae** | `.trae/rules/` |
| 13 | `.codebuddy/` 目录存在 | **CodeBuddy** | `.codebuddy/` |
| 14 | `.lingma/` 目录存在 | **通义灵码** | `.lingma/` |
| 15 | `.codegeex/` 目录存在 | **CodeGeeX** | `.codegeex/` |
| 16 | `.tabnine/` 目录存在 | **Tabnine** | `.tabnine/guidelines/` |
| 17 | `.cody/` 目录存在 | **Sourcegraph Cody** | `.cody/` |
| 18 | `.mars/` 目录存在 | **MarsCode** | `.mars/` |

> **检测逻辑**：① 按上表顺序检测；② 优先选择环境变量匹配的工具（精确匹配）；③ 若环境变量未匹配，按优先级检测目录是否存在；④ 选择第一个匹配的，确定目标技能目录。

#### 5.5.2 创建目标目录（若不存在）

确定目标技能目录后，若该目录不存在，**必须创建**：

```bash
# 例如检测到 Reasonix，目标目录为 .reasonix/skills/
mkdir -p .reasonix/skills/
# 或检测到 Claude Code，目标目录为 .claude/skills/
mkdir -p .claude/skills/
# 其他工具同理，确保目标技能目录存在
```

> **关键**：不要因为目录不存在就跳过——创建它。安装引擎的职责之一是"为当前 AI 工具准备好技能基础设施"。

#### 5.5.3 扫描技能来源

扫描 `.harness/skills/` 下所有含 `SKILL.md` 的技能目录：

- 语言特有技能：`.harness/skills/{lang}/`（如 `harnessing`、`coding-skill`、`unit-test-write` 等）
- 跨语言通用技能：`.harness/skills/common/`（如 `domain-modeling`、`research`、`resolving-merge-conflicts` 等）

#### 5.5.4 复制安装技能

对每个含 `SKILL.md` 的技能目录 `<skill-name>`：

1. 完整复制到 `<目标技能目录>/<skill-name>/`（保持目录结构和所有文件）
2. 保持 `SKILL.md` 的 frontmatter `name` 不变——斜杠命令名即来自它
3. 若目标已存在同名技能，**覆盖**（或询问用户是否覆盖）

#### 5.5.5 输出安装摘要

```
╔══════════════════════════════════════════╗
║   ✅ 技能已安装到 <工具名>               ║
╠══════════════════════════════════════════╣
║  目标目录: .reasonix/skills/             ║
║  已安装:   N 个技能                      ║
║  /harnessing        ✅ 可调用            ║
║  /harness-me        ✅ 可调用            ║
║  /coding-skill      ✅ 可调用            ║
║  /unit-test-write   ✅ 可调用            ║
║  /expert-reviewer   ✅ 可调用            ║
║  /unit-test-ci      ✅ 可调用            ║
║  /deploy-verify     ✅ 可调用            ║
║  /diagnosing-bugs   ✅ 可调用            ║
║  /arch-review       ✅ 可调用            ║
║  /handoff           ✅ 可调用            ║
║  /domain-modeling   ✅ 可调用            ║
║  ...                                    ║
╚══════════════════════════════════════════╝
```

> 若自动注册成功，技能命令立即可用。若全部失败（如所有工具目录均不存在且无法创建），在 Step 8 的输出卡片末尾提示用户手动执行 `/install-skill`。

### Step 6: 初始化变更追踪

从 `harness-core/templates/changes/` 复制到 `.harness/changes/`：

```
.harness/changes/
└── _TEMPLATE/
    ├── change.md      ← 变更卡模板
    ├── review.md      ← 评审报告模板
    ├── verify.md      ← 部署验证报告模板（含动态事实核验 + traceId/断言回填）
    └── archive.md     ← 归档 + 知识回流清单模板
```

### Step 6.1: 初始化机器可读迭代协议（可选增强）

从 `harness-core/templates/iterations/` 复制到 `.harness/iterations/`：

```
.harness/iterations/
└── _TEMPLATE/
    ├── prd.md          ← 需求 + 验收标准（REQ/AC）
    ├── solution.md     ← 技术方案（REQ→任务/观测点）
    └── test/
        ├── design.md   ← 测试设计（AC→测试方法）
        ├── cases.md    ← 测试用例（TC）
        └── report.md   ← 测试报告（真实证据）
```

> 复杂需求可将单文件 `change.md` 扩展为多文件迭代协议，使阶段产物可被机器对账。归档与知识回流统一落在 `.harness/changes/<id>/archive.md`（不随迭代协议拆分）。实例化由需求阶段（`harnessing`）把 `_TEMPLATE/` 复制为 `.harness/iterations/<ITER-ID>/`，并在 `change.md` 的 `iteration:` 字段回填（阶段间接口门禁见 `rules/开发流程规范.md §2.2`）。

### Step 7: 初始化领域知识库

从 `harness-core/templates/wiki/` 复制到 `.harness/wiki/`，从 `harness-core/templates/tech/` 复制到 `.harness/tech/`：

```
.harness/wiki/
├── _TEMPLATE/知识文件模板.md   ← 业务知识模板（frontmatter: title/category/tags/status/version/source）
├── 业务模型.md     ← 业务结构与关系模板，待补充
├── 接口协议.md     ← 接口契约模板，待补充
├── 数据模型.md     ← 数据模型模板，待补充
├── 架构决策.md     ← ADR 记录模板，待补充
└── ADR-FORMAT.md   ← ADR 编写规范（什么值得写 / 怎么写）

.harness/tech/
└── _TEMPLATE/链路模板.md       ← 技术上下文模板（链路/观测方式/废弃状态，含 wiki_ref）
```

> 分层纪律见 `rules/知识库治理规则.md`：wiki 存业务知识、tech 存技术上下文，不复制代码，事实来源（source）必填、链接用相对路径。

### Step 7.1: 初始化共享语言上下文

从 `harness-core/templates/CONTEXT.md` 复制到 `.harness/CONTEXT.md`，从 `harness-core/templates/CONTEXT-FORMAT.md` 复制到 `.harness/CONTEXT-FORMAT.md`：

```
.harness/
├── CONTEXT.md          ← 领域语言术语表（活词典）
└── CONTEXT-FORMAT.md   ← CONTEXT.md 编写规范
```

> **CONTEXT.md 的作用**：AI 与人类之间的共享语言机制，记录项目特有的领域术语、缩写、决策、约定。由 `domain-modeling` 技能在对话中主动维护——术语敲定后**当场写**，不批量累积。每次对话结束时，AI 应检查是否有新术语/决策需要补充。
> **CONTEXT-FORMAT.md 的作用**：定义 CONTEXT.md 的编写规范（只存术语、要有主见、定义克制、只收项目特有概念）。

### Step 7.2: 初始化动态事实来源与公共能力依赖声明

从 `harness-core/templates/动态事实来源.md` 复制到 `.harness/动态事实来源.md`，从 `harness-core/templates/skill-dependencies.json` 复制到 `.harness/skill-dependencies.json`：

```
.harness/
├── 动态事实来源.md          ← 动态事实（工作项/环境/日志/DB/配置）经 MCP/CLI 连接清单
└── skill-dependencies.json  ← 公共能力依赖声明（来源 + 兼容版本，声明而非复制）
```

> **动态事实来源**：稳定上下文进 Git，动态事实从权威系统实时查询，两者在 Agent 执行时汇合。
> **skill-dependencies.json**：公共能力声明依赖而非复制进项目，便于统一升级（三层结构见 `rules/开发流程规范.md §1.1`）。

### Step 8: 输出项目摘要卡片

```
╔══════════════════════════════════════════╗
║   ✅ Harness 规范已应用到 <项目名>       ║
╠══════════════════════════════════════════╣
║  语言:   Java / Python / Go / Rust / Frontend        ║
║  框架:   Spring Boot 3.x / Django 5.x    ║
║  规则:   5 个已就绪（3 通用 + 2 语言特有）   ║
║  技能:   12 个已就绪（9 语言特有 + 3 通用）║
║  Owner:  已就绪                          ║
╠══════════════════════════════════════════╣
║  下一步: 注册技能到当前工具              ║
║  /install-skill                          ║
║  然后创建你的第一个变更:                  ║
║  /harnessing 一句话描述需求            ║
╚══════════════════════════════════════════╝
```

## 参数表

> 按 Step 1 检测到的（语言、框架、构建工具）三元组，选择对应的框架参数块。未在此列出的框架（用户手动指定）询问用户确认参数。

> **命令真跑验证纪律（test-setup）**：参数表中的 `{{BUILD_CMD}}` / `{{TEST_CMD}}` / `{{COV_CMD}}` / `{{LINT_CMD}}` / `{{ARCH_TEST_CMD}}` / `{{SECURITY_CMD}}` / `{{INTEGRATION_CMD}}` 等命令，渲染前必须**逐条真跑一次，看到真实退出码**才可落盘到渲染后的技能中；跑不通的命令**如实留空或标注"未验证"**，不得猜命令、不得把猜测当事实。若目标项目尚无代码可跑（全新项目），则命令先以参数表默认值落盘，但必须在首个 change 进入 ② 编码 / ⑤ CI 门禁时由 `coding-skill` / `unit-test-ci` **真跑验证一次**，验证不通过即退回修正命令——这正是 `harness-quality` 的「量不到就写未测量」同一条纪律：**命令真跑过才算数，没跑过就不算**。

### Java — Spring Boot + Maven（默认）

| 参数 | 值                                           |
|------|---------------------------------------------|
| `{{LANGUAGE}}` | `Java`                                      |
| `{{LANGUAGE_DESC}}` | Java、Spring Boot、多模块 Maven 架构               |
| `{{LANGUAGE_RUNTIME}}` | JDK 21 LTS                                  |
| `{{FRAMEWORK_VER}}` | Spring Boot 3.x+                            |
| `{{BUILD_TOOL}}` | Maven 3.9+                                  |
| `{{TEST_FRAMEWORK}}` | JUnit 5 + Mockito + AssertJ                 |
| `{{COV_TOOL}}` | JaCoCo (核心逻辑 ≥80%)                          |
| `{{LINT_TOOL}}` | Checkstyle + PMD + SpotBugs                 |
| `{{ARCH_TEST_TOOL}}` | ArchUnit                                    |
| `{{DB_ACCESS}}` | MyBatis-Plus / JPA + Flyway                 |
| `{{LANG_TAG}}` | `-java`                                     |
| `{{HARNESS_ME_NAME}}` | `harness-me`                               |
| `{{HARNESSING_CMD}}` | `harnessing`                               |
| `{{BUILD_CMD}}` | `mvn compile`                               |
| `{{TEST_CMD}}` | `mvn test`                                  |
| `{{LINT_CMD}}` | `mvn checkstyle:check`                      |
| `{{DEV_CMD}}` | `mvn spring-boot:run`                       |
| `{{DOCSTYLE}}` | `Javadoc`                                   |
| `{{FILE_LIMIT}}` | `500`                                       |
| `{{ARCH_LAYER}}` | 模块间仅通过接口通信，依赖方向 `common ← service ← server` |
| `{{TEST_NAMING}}` | `method_should_x_when_y`                    |
| `{{MOCK_LIB}}` | `Mockito`                                   |
| `{{COV_CMD}}` | `mvn jacoco:report`                         |
| `{{DEBUG_TOOL}}` | 调试器 / `jdb`                                 |
| `{{ARCH_REVIEW_CMD}}` | `arch-review`                              |
| `{{ARCH_TEST_CMD}}` | 架构约束测试命令                                    |
| `{{SECURITY_CMD}}` | 安全扫描命令                                      |
| `{{INTEGRATION_CMD}}` | 集成测试命令                                      |
| `{{RUN_CMD}}` | 启动服务命令                                      |
| `{{HEALTH_CHECK_CMD}}` | 健康检查命令                                      |
| `{{HEALTH_ENDPOINT}}` | 健康检查端点                                      |
| `{{METRICS_ENDPOINT}}` | 指标端点                                        |
| `{{VET_CMD}}` | 静态分析命令（实际合并到 LINT_CMD）                      |
| `{{DEP_CMD}}` | 依赖管理命令：`mvn dependency:tree`                |
| `{{ORM_TOOL}}` | ORM 框架：MyBatis-Plus / JPA                   |
| `{{TYPE_CHECK_TOOL}}` | 类型检查工具：javac（编译时检查）                         |
| `{{TYPE_CHECK_CMD}}` | 类型检查命令：`{{BUILD_CMD}}`                      |
| `{{ASSERT_LIB}}` | 断言库：AssertJ                                 |
| `{{RACE_DETECT_ARG}}` | 竞态检测参数：空                                    |

### Java — Spring Boot + Gradle

仅 `{{BUILD_TOOL}}`、`{{BUILD_CMD}}`、`{{TEST_CMD}}`、`{{LINT_CMD}}`、`{{DEV_CMD}}`、`{{COV_CMD}}` 与 Maven 版不同：

| 参数 | 值 |
|------|-----|
| `{{LANGUAGE_DESC}}` | Java、Spring Boot、多模块 Gradle 架构 |
| `{{BUILD_TOOL}}` | Gradle 8.x（Gradle Wrapper） |
| `{{BUILD_CMD}}` | `./gradlew build` |
| `{{TEST_CMD}}` | `./gradlew test` |
| `{{LINT_CMD}}` | `./gradlew checkstyleMain` |
| `{{DEV_CMD}}` | `./gradlew bootRun` |
| `{{COV_CMD}}` | `./gradlew jacocoTestReport` |

### Java — Quarkus + Maven

| 参数 | 值                                                  |
|------|----------------------------------------------------|
| `{{LANGUAGE}}` | `Java`                                             |
| `{{LANGUAGE_DESC}}` | Java、Quarkus、GraalVM 原生可执行                         |
| `{{LANGUAGE_RUNTIME}}` | JDK 21 LTS                                         |
| `{{FRAMEWORK_VER}}` | Quarkus 3.x                                        |
| `{{BUILD_TOOL}}` | Maven 3.9+                                         |
| `{{TEST_FRAMEWORK}}` | JUnit 5 + Mockito + AssertJ                        |
| `{{COV_TOOL}}` | JaCoCo (核心逻辑 ≥80%)                                 |
| `{{LINT_TOOL}}` | Checkstyle + PMD + SpotBugs                        |
| `{{ARCH_TEST_TOOL}}` | ArchUnit                                           |
| `{{DB_ACCESS}}` | Hibernate Panache / JPA + Flyway                   |
| `{{LANG_TAG}}` | `-java`                                            |
| `{{HARNESS_ME_NAME}}` | `harness-me`                                      |
| `{{HARNESSING_CMD}}` | `harnessing`                                      |
| `{{BUILD_CMD}}` | `mvn compile`                                      |
| `{{TEST_CMD}}` | `mvn test`                                         |
| `{{LINT_CMD}}` | `mvn checkstyle:check`                             |
| `{{DEV_CMD}}` | `mvn quarkus:dev`                                  |
| `{{DOCSTYLE}}` | `Javadoc`                                          |
| `{{FILE_LIMIT}}` | `500`                                              |
| `{{ARCH_LAYER}}` | 分层 `entity → repository → service → resource`，依赖单向 |
| `{{TEST_NAMING}}` | `method_should_x_when_y`                           |
| `{{MOCK_LIB}}` | `Mockito`                                          |
| `{{COV_CMD}}` | `mvn jacoco:report`                                |
| `{{DEBUG_TOOL}}` | 调试器 / `jdb`                                        |
| `{{ARCH_REVIEW_CMD}}` | `arch-review`                                     |

### Java — Quarkus + Gradle

| 参数 | 值 |
|------|-----|
| `{{LANGUAGE_DESC}}` | Java、Quarkus、GraalVM 原生可执行 |
| `{{BUILD_TOOL}}` | Gradle 8.x（Gradle Wrapper） |
| `{{BUILD_CMD}}` | `./gradlew build` |
| `{{TEST_CMD}}` | `./gradlew test` |
| `{{LINT_CMD}}` | `./gradlew checkstyleMain` |
| `{{DEV_CMD}}` | `./gradlew quarkusDev` |
| `{{COV_CMD}}` | `./gradlew jacocoTestReport` |

### Java — Micronaut + Maven

| 参数 | 值                                       |
|------|-----------------------------------------|
| `{{LANGUAGE}}` | `Java`                                  |
| `{{LANGUAGE_DESC}}` | Java、Micronaut、低内存云原生框架                 |
| `{{LANGUAGE_RUNTIME}}` | JDK 21 LTS                              |
| `{{FRAMEWORK_VER}}` | Micronaut 4.x                           |
| `{{BUILD_TOOL}}` | Maven 3.9+                              |
| `{{TEST_FRAMEWORK}}` | JUnit 5 + Mockito                       |
| `{{COV_TOOL}}` | JaCoCo (核心逻辑 ≥80%)                      |
| `{{LINT_TOOL}}` | Checkstyle + PMD + SpotBugs             |
| `{{ARCH_TEST_TOOL}}` | ArchUnit                                |
| `{{DB_ACCESS}}` | Micronaut Data + Flyway                 |
| `{{LANG_TAG}}` | `-java`                                 |
| `{{HARNESS_ME_NAME}}` | `harness-me`                           |
| `{{HARNESSING_CMD}}` | `harnessing`                           |
| `{{BUILD_CMD}}` | `mvn compile`                           |
| `{{TEST_CMD}}` | `mvn test`                              |
| `{{LINT_CMD}}` | `mvn checkstyle:check`                  |
| `{{DEV_CMD}}` | `mvn mn:run`                            |
| `{{DOCSTYLE}}` | `Javadoc`                               |
| `{{FILE_LIMIT}}` | `500`                                   |
| `{{ARCH_LAYER}}` | 分层 `domain → service → controller`，依赖单向 |
| `{{TEST_NAMING}}` | `method_should_x_when_y`                |
| `{{MOCK_LIB}}` | `Mockito`                               |
| `{{COV_CMD}}` | `mvn jacoco:report`                     |
| `{{DEBUG_TOOL}}` | 调试器 / `jdb`                             |
| `{{ARCH_REVIEW_CMD}}` | `arch-review`                          |

### Java — Vert.x + Maven

| 参数 | 值                               |
|------|---------------------------------|
| `{{LANGUAGE}}` | `Java`                          |
| `{{LANGUAGE_DESC}}` | Java、Vert.x、响应式事件驱动框架           |
| `{{LANGUAGE_RUNTIME}}` | JDK 21 LTS                      |
| `{{FRAMEWORK_VER}}` | Vert.x 4.x                      |
| `{{BUILD_TOOL}}` | Maven 3.9+                      |
| `{{TEST_FRAMEWORK}}` | JUnit 5 + Mockito + Vert.x Test |
| `{{COV_TOOL}}` | JaCoCo (核心逻辑 ≥80%)              |
| `{{LINT_TOOL}}` | Checkstyle + PMD + SpotBugs     |
| `{{ARCH_TEST_TOOL}}` | ArchUnit                        |
| `{{DB_ACCESS}}` | Vert.x JDBC/MySQL 客户端           |
| `{{LANG_TAG}}` | `-java`                         |
| `{{HARNESS_ME_NAME}}` | `harness-me`                   |
| `{{HARNESSING_CMD}}` | `harnessing`                   |
| `{{BUILD_CMD}}` | `mvn compile`                   |
| `{{TEST_CMD}}` | `mvn test`                      |
| `{{LINT_CMD}}` | `mvn checkstyle:check`          |
| `{{DEV_CMD}}` | `mvn vertx:run`                 |
| `{{DOCSTYLE}}` | `Javadoc`                       |
| `{{FILE_LIMIT}}` | `500`                           |
| `{{ARCH_LAYER}}` | Verticle 间通过事件总线通信，禁止直接耦合       |
| `{{TEST_NAMING}}` | `method_should_x_when_y`        |
| `{{MOCK_LIB}}` | `Mockito`                       |
| `{{COV_CMD}}` | `mvn jacoco:report`             |
| `{{DEBUG_TOOL}}` | 调试器 / `jdb`                     |
| `{{ARCH_REVIEW_CMD}}` | `arch-review`                  |

### Java — Dropwizard + Maven

| 参数 | 值                                          |
|------|--------------------------------------------|
| `{{LANGUAGE}}` | `Java`                                     |
| `{{LANGUAGE_DESC}}` | Java、Dropwizard、轻量 REST 服务                 |
| `{{LANGUAGE_RUNTIME}}` | JDK 21 LTS                                 |
| `{{FRAMEWORK_VER}}` | Dropwizard 4.x                             |
| `{{BUILD_TOOL}}` | Maven 3.9+                                 |
| `{{TEST_FRAMEWORK}}` | JUnit 5 + Mockito + AssertJ                |
| `{{COV_TOOL}}` | JaCoCo (核心逻辑 ≥80%)                         |
| `{{LINT_TOOL}}` | Checkstyle + PMD + SpotBugs                |
| `{{ARCH_TEST_TOOL}}` | ArchUnit                                   |
| `{{DB_ACCESS}}` | JDBI / Hibernate + Flyway                  |
| `{{LANG_TAG}}` | `-java`                                    |
| `{{HARNESS_ME_NAME}}` | `harness-me`                              |
| `{{HARNESSING_CMD}}` | `harnessing`                              |
| `{{BUILD_CMD}}` | `mvn compile`                              |
| `{{TEST_CMD}}` | `mvn test`                                 |
| `{{LINT_CMD}}` | `mvn checkstyle:check`                     |
| `{{DEV_CMD}}` | `java -jar target/*.jar server config.yml` |
| `{{DOCSTYLE}}` | `Javadoc`                                  |
| `{{FILE_LIMIT}}` | `500`                                      |
| `{{ARCH_LAYER}}` | 分层 `domain → service → resource`，依赖单向      |
| `{{TEST_NAMING}}` | `method_should_x_when_y`                   |
| `{{MOCK_LIB}}` | `Mockito`                                  |
| `{{COV_CMD}}` | `mvn jacoco:report`                        |
| `{{DEBUG_TOOL}}` | 调试器 / `jdb`                                |
| `{{ARCH_REVIEW_CMD}}` | `arch-review`                             |

### Java — Spring MVC + Maven（经典 Servlet 架构）

| 参数 | 值                                                  |
|------|----------------------------------------------------|
| `{{LANGUAGE}}` | `Java`                                             |
| `{{LANGUAGE_DESC}}` | Java、Spring MVC、经典 Servlet 分层架构                    |
| `{{LANGUAGE_RUNTIME}}` | JDK 21 LTS                                         |
| `{{FRAMEWORK_VER}}` | Spring 6.x Web MVC                                 |
| `{{BUILD_TOOL}}` | Maven 3.9+                                         |
| `{{TEST_FRAMEWORK}}` | JUnit 5 + Mockito + AssertJ                        |
| `{{COV_TOOL}}` | JaCoCo (核心逻辑 ≥80%)                                 |
| `{{LINT_TOOL}}` | Checkstyle + PMD + SpotBugs                        |
| `{{ARCH_TEST_TOOL}}` | ArchUnit                                           |
| `{{DB_ACCESS}}` | MyBatis-Plus / JPA + Flyway                        |
| `{{LANG_TAG}}` | `-java`                                            |
| `{{HARNESS_ME_NAME}}` | `harness-me`                                      |
| `{{HARNESSING_CMD}}` | `harnessing`                                      |
| `{{BUILD_CMD}}` | `mvn compile`                                      |
| `{{TEST_CMD}}` | `mvn test`                                         |
| `{{LINT_CMD}}` | `mvn checkstyle:check`                             |
| `{{DEV_CMD}}` | `mvn tomcat7:run` / 部署到外部 Servlet 容器               |
| `{{DOCSTYLE}}` | `Javadoc`                                          |
| `{{FILE_LIMIT}}` | `500`                                              |
| `{{ARCH_LAYER}}` | 分层 `controller → service → mapper/repository`，依赖单向 |
| `{{TEST_NAMING}}` | `method_should_x_when_y`                           |
| `{{MOCK_LIB}}` | `Mockito`                                          |
| `{{COV_CMD}}` | `mvn jacoco:report`                                |
| `{{DEBUG_TOOL}}` | 调试器 / `jdb`                                        |
| `{{ARCH_REVIEW_CMD}}` | `arch-review`                                     |

---

### Python — FastAPI

| 参数 | 值                                         |
|------|-------------------------------------------|
| `{{LANGUAGE}}` | `Python`                                  |
| `{{LANGUAGE_DESC}}` | Python、FastAPI、异步 API 服务                  |
| `{{LANGUAGE_RUNTIME}}` | Python 3.11+                              |
| `{{FRAMEWORK_VER}}` | FastAPI 0.110+                            |
| `{{BUILD_TOOL}}` | pip + virtualenv / poetry / uv            |
| `{{TEST_FRAMEWORK}}` | pytest + pytest-mock                      |
| `{{COV_TOOL}}` | pytest-cov (核心逻辑 ≥80%)                    |
| `{{LINT_TOOL}}` | flake8 + mypy + black + isort             |
| `{{ARCH_TEST_TOOL}}` | 自定义 import-lint 检查                        |
| `{{DB_ACCESS}}` | SQLAlchemy + Alembic                      |
| `{{LANG_TAG}}` | `-python`                                 |
| `{{HARNESS_ME_NAME}}` | `harness-me-python`                      |
| `{{HARNESSING_CMD}}` | `harnessing-python`                      |
| `{{BUILD_CMD}}` | `python -m compileall .`                  |
| `{{TEST_CMD}}` | `python -m pytest`                        |
| `{{LINT_CMD}}` | `flake8 .`                                |
| `{{DEV_CMD}}` | `uvicorn main:app --reload`               |
| `{{DOCSTYLE}}` | `docstring`                               |
| `{{FILE_LIMIT}}` | `800`                                     |
| `{{ARCH_LAYER}}` | 依赖方向 `router → handler → service → model` |
| `{{TEST_NAMING}}` | `test_x_when_y`                           |
| `{{MOCK_LIB}}` | `pytest-mock`                             |
| `{{COV_CMD}}` | `pytest --cov`                            |
| `{{DEBUG_TOOL}}` | `pdb` / `breakpoint()` / `ipdb`           |
| `{{ARCH_REVIEW_CMD}}` | `arch-review-python`                     |
| `{{ARCH_TEST_CMD}}` | 架构约束测试命令                                  |
| `{{SECURITY_CMD}}` | 安全扫描命令                                    |
| `{{INTEGRATION_CMD}}` | 集成测试命令                                    |
| `{{RUN_CMD}}` | 启动服务命令                                    |
| `{{HEALTH_CHECK_CMD}}` | 健康检查命令                                    |
| `{{HEALTH_ENDPOINT}}` | 健康检查端点                                    |
| `{{METRICS_ENDPOINT}}` | 指标端点                                      |
| `{{VET_CMD}}` | 静态分析命令：`mypy`                             |
| `{{DEP_CMD}}` | 依赖管理命令：`pip install` / `poetry add`       |
| `{{ORM_TOOL}}` | ORM 框架：SQLAlchemy                         |
| `{{TYPE_CHECK_TOOL}}` | 类型检查工具：mypy                               |
| `{{TYPE_CHECK_CMD}}` | 类型检查命令：`mypy`                             |
| `{{FORMAT_TOOL}}` | 格式化工具：black                               |
| `{{FORMAT_CHECK_CMD}}` | 格式检查命令：`black --check .`                  |
| `{{METRICS_CHECK_CMD}}` | 指标检查命令：`curl {{METRICS_ENDPOINT}}`        |
| `{{ASSERT_LIB}}` | 断言库：`assert`                              |
| `{{RACE_DETECT_ARG}}` | 竞态检测参数：空                                  |

### Python — Flask

| 参数 | 值                                                   |
|------|-----------------------------------------------------|
| `{{LANGUAGE_DESC}}` | Python、Flask、轻量模板化 Web 服务                           |
| `{{FRAMEWORK_VER}}` | Flask 3.x                                           |
| `{{LANGUAGE_RUNTIME}}` | Python 3.11+                                        |
| `{{BUILD_TOOL}}` | pip + virtualenv / poetry / uv                      |
| `{{TEST_FRAMEWORK}}` | pytest + pytest-mock                                |
| `{{COV_TOOL}}` | pytest-cov (核心逻辑 ≥80%)                              |
| `{{LINT_TOOL}}` | flake8 + mypy + black + isort                       |
| `{{ARCH_TEST_TOOL}}` | 自定义 import-lint 检查                                  |
| `{{DB_ACCESS}}` | SQLAlchemy + Flask-Migrate（Alembic）                 |
| `{{LANG_TAG}}` | `-python`                                           |
| `{{HARNESS_ME_NAME}}` | `harness-me-python`                                |
| `{{HARNESSING_CMD}}` | `harnessing-python`                                |
| `{{BUILD_CMD}}` | `python -m compileall .`                            |
| `{{TEST_CMD}}` | `python -m pytest`                                  |
| `{{LINT_CMD}}` | `flake8 .`                                          |
| `{{DEV_CMD}}` | `flask --app <app> run --debug`                     |
| `{{DOCSTYLE}}` | `docstring`                                         |
| `{{FILE_LIMIT}}` | `800`                                               |
| `{{ARCH_LAYER}}` | 依赖方向 `blueprint/router → handler → service → model` |
| `{{TEST_NAMING}}` | `test_x_when_y`                                     |
| `{{MOCK_LIB}}` | `pytest-mock`                                       |
| `{{COV_CMD}}` | `pytest --cov`                                      |
| `{{DEBUG_TOOL}}` | `pdb` / `breakpoint()` / `ipdb`                     |
| `{{ARCH_REVIEW_CMD}}` | `arch-review-python`                               |

### Python — Django

| 参数 | 值                                           |
|------|---------------------------------------------|
| `{{LANGUAGE}}` | `Python`                                    |
| `{{LANGUAGE_DESC}}` | Python、Django、全栈 MTV 框架（自带 ORM/Admin/迁移）    |
| `{{LANGUAGE_RUNTIME}}` | Python 3.11+                                |
| `{{FRAMEWORK_VER}}` | Django 5.x                                  |
| `{{BUILD_TOOL}}` | pip + virtualenv / poetry / uv              |
| `{{TEST_FRAMEWORK}}` | Django TestCase + pytest-django             |
| `{{COV_TOOL}}` | pytest-cov (核心逻辑 ≥80%)                      |
| `{{LINT_TOOL}}` | flake8 + mypy + black + isort               |
| `{{ARCH_TEST_TOOL}}` | 自定义 import-lint 检查                          |
| `{{DB_ACCESS}}` | Django ORM + 内置迁移（makemigrations）           |
| `{{LANG_TAG}}` | `-python`                                   |
| `{{HARNESS_ME_NAME}}` | `harness-me-python`                        |
| `{{HARNESSING_CMD}}` | `harnessing-python`                        |
| `{{BUILD_CMD}}` | `python -m compileall .`                    |
| `{{TEST_CMD}}` | `python manage.py test`                     |
| `{{LINT_CMD}}` | `flake8 .`                                  |
| `{{DEV_CMD}}` | `python manage.py runserver`                |
| `{{DOCSTYLE}}` | `docstring`                                 |
| `{{FILE_LIMIT}}` | `800`                                       |
| `{{ARCH_LAYER}}` | MTV 分层：依赖方向 `views → services/models → ORM` |
| `{{TEST_NAMING}}` | `test_x_when_y`                             |
| `{{MOCK_LIB}}` | `pytest-mock` / `unittest.mock`             |
| `{{COV_CMD}}` | `pytest --cov`                              |
| `{{DEBUG_TOOL}}` | `pdb` / `django-debug-toolbar`              |
| `{{ARCH_REVIEW_CMD}}` | `arch-review-python`                       |

---

### Go — Gin

| 参数 | 值                                       |
|------|-----------------------------------------|
| `{{LANGUAGE}}` | `Go`                                    |
| `{{LANGUAGE_DESC}}` | Go、Gin、高性能 HTTP 框架                      |
| `{{LANGUAGE_RUNTIME}}` | Go 1.22+                                |
| `{{FRAMEWORK_VER}}` | Gin 1.10+                               |
| `{{BUILD_TOOL}}` | go mod                                  |
| `{{TEST_FRAMEWORK}}` | go test + testify                       |
| `{{COV_TOOL}}` | go test -cover (核心逻辑 ≥80%)              |
| `{{LINT_TOOL}}` | golangci-lint (go vet + staticcheck)    |
| `{{ARCH_TEST_TOOL}}` | goimports + 自定义架构检查                     |
| `{{DB_ACCESS}}` | GORM / sqlx + golang-migrate            |
| `{{LANG_TAG}}` | `-golang`                               |
| `{{HARNESS_ME_NAME}}` | `harness-me-golang`                    |
| `{{HARNESSING_CMD}}` | `harnessing-golang`                    |
| `{{BUILD_CMD}}` | `go build ./...`                        |
| `{{TEST_CMD}}` | `go test ./...`                         |
| `{{LINT_CMD}}` | `golangci-lint run`                     |
| `{{DEV_CMD}}` | `go run ./cmd/server`                   |
| `{{DOCSTYLE}}` | Go 注释                                   |
| `{{FILE_LIMIT}}` | `800`                                   |
| `{{ARCH_LAYER}}` | 分层 `handler → service → repository`，依赖单向 |
| `{{TEST_NAMING}}` | `TestX_WhenY`                           |
| `{{MOCK_LIB}}` | `gomock` / `testify`                    |
| `{{COV_CMD}}` | `go test -cover`                        |
| `{{DEBUG_TOOL}}` | `delve`                                 |
| `{{ARCH_REVIEW_CMD}}` | `arch-review`                          |
| `{{ARCH_TEST_CMD}}` | 架构约束测试命令                                |
| `{{SECURITY_CMD}}` | 安全扫描命令                                  |
| `{{INTEGRATION_CMD}}` | 集成测试命令                                  |
| `{{RUN_CMD}}` | 启动服务命令                                  |
| `{{HEALTH_CHECK_CMD}}` | 健康检查命令                                  |
| `{{HEALTH_ENDPOINT}}` | 健康检查端点                                  |
| `{{METRICS_ENDPOINT}}` | 指标端点                                    |
| `{{VET_CMD}}` | 静态分析命令：`go vet ./...`                   |
| `{{DEP_CMD}}` | 依赖管理命令：`go mod tidy`                    |
| `{{ORM_TOOL}}` | ORM 框架：GORM / sqlx                      |
| `{{TYPE_CHECK_TOOL}}` | 类型检查工具：go vet                           |
| `{{TYPE_CHECK_CMD}}` | 类型检查命令：`go vet ./...`                   |
| `{{ASSERT_LIB}}` | 断言库：testify                             |
| `{{RACE_DETECT_ARG}}` | 竞态检测参数：`-race`                          |
| `{{HTTP_MOCK_UTIL}}` | HTTP Mock 工具：`httptest.Server`          |
| `{{FRAMEWORK_NAME}}` | 框架名称（从 {{LANGUAGE_DESC}} 提取）           |

### Go — go-zero

| 参数 | 值                                                |
|------|--------------------------------------------------|
| `{{LANGUAGE_DESC}}` | Go、go-zero、一体化微服务框架（API/RPC）                     |
| `{{FRAMEWORK_VER}}` | go-zero 1.9.x                                    |
| `{{LANGUAGE_RUNTIME}}` | Go 1.22+                                         |
| `{{BUILD_TOOL}}` | go mod + goctl                                   |
| `{{TEST_FRAMEWORK}}` | go test + testify                                |
| `{{COV_TOOL}}` | go test -cover (核心逻辑 ≥80%)                       |
| `{{LINT_TOOL}}` | golangci-lint (go vet + staticcheck)             |
| `{{ARCH_TEST_TOOL}}` | goimports + 自定义架构检查                              |
| `{{DB_ACCESS}}` | GORM + goctl model                               |
| `{{LANG_TAG}}` | `-golang`                                        |
| `{{HARNESS_ME_NAME}}` | `harness-me-golang`                             |
| `{{HARNESSING_CMD}}` | `harnessing-golang`                             |
| `{{BUILD_CMD}}` | `goctl api go` + `go build ./...`                |
| `{{TEST_CMD}}` | `go test ./...`                                  |
| `{{LINT_CMD}}` | `golangci-lint run`                              |
| `{{DEV_CMD}}` | `go run <service>.go`                            |
| `{{DOCSTYLE}}` | Go 注释                                            |
| `{{FILE_LIMIT}}` | `800`                                            |
| `{{ARCH_LAYER}}` | 服务间仅通过 RPC 通信，依赖方向 `common → models → rpc → api` |
| `{{TEST_NAMING}}` | `TestX_WhenY`                                    |
| `{{MOCK_LIB}}` | `gomock` / `testify` / `goctl`                   |
| `{{COV_CMD}}` | `go test -cover`                                 |
| `{{DEBUG_TOOL}}` | `delve`                                          |
| `{{ARCH_REVIEW_CMD}}` | `arch-review`                                   |

### Go — Echo

| 参数 | 值                                        |
|------|------------------------------------------|
| `{{LANGUAGE_DESC}}` | Go、Echo、极简高性能 HTTP 框架                    |
| `{{FRAMEWORK_VER}}` | Echo v4                                  |
| `{{LANGUAGE_RUNTIME}}` | Go 1.22+                                 |
| `{{BUILD_TOOL}}` | go mod                                   |
| `{{TEST_FRAMEWORK}}` | go test + testify                        |
| `{{COV_TOOL}}` | go test -cover (核心逻辑 ≥80%)               |
| `{{LINT_TOOL}}` | golangci-lint (go vet + staticcheck)     |
| `{{ARCH_TEST_TOOL}}` | goimports + 自定义架构检查                      |
| `{{DB_ACCESS}}` | GORM / sqlx + golang-migrate             |
| `{{LANG_TAG}}` | `-golang`                                |
| `{{HARNESS_ME_NAME}}` | `harness-me-golang`                     |
| `{{HARNESSING_CMD}}` | `harnessing-golang`                     |
| `{{BUILD_CMD}}` | `go build ./...`                         |
| `{{TEST_CMD}}` | `go test ./...`                          |
| `{{LINT_CMD}}` | `golangci-lint run`                      |
| `{{DEV_CMD}}` | `go run ./cmd/server`                    |
| `{{DOCSTYLE}}` | Go 注释                                    |
| `{{FILE_LIMIT}}` | `800`                                    |
| `{{ARCH_LAYER}}` | 分层 `handler → service → repository`，依赖单向 |
| `{{TEST_NAMING}}` | `TestX_WhenY`                            |
| `{{MOCK_LIB}}` | `gomock` / `testify`                     |
| `{{COV_CMD}}` | `go test -cover`                         |
| `{{DEBUG_TOOL}}` | `delve`                                  |
| `{{ARCH_REVIEW_CMD}}` | `arch-review`                           |

### Go — Fiber

| 参数 | 值                                        |
|------|------------------------------------------|
| `{{LANGUAGE_DESC}}` | Go、Fiber、基于 fasthttp 的极速 Web 框架          |
| `{{FRAMEWORK_VER}}` | Fiber v2                                 |
| `{{LANGUAGE_RUNTIME}}` | Go 1.22+                                 |
| `{{BUILD_TOOL}}` | go mod                                   |
| `{{TEST_FRAMEWORK}}` | go test + testify                        |
| `{{COV_TOOL}}` | go test -cover (核心逻辑 ≥80%)               |
| `{{LINT_TOOL}}` | golangci-lint (go vet + staticcheck)     |
| `{{ARCH_TEST_TOOL}}` | goimports + 自定义架构检查                      |
| `{{DB_ACCESS}}` | GORM / sqlx + golang-migrate             |
| `{{LANG_TAG}}` | `-golang`                                |
| `{{HARNESS_ME_NAME}}` | `harness-me-golang`                     |
| `{{HARNESSING_CMD}}` | `harnessing-golang`                     |
| `{{BUILD_CMD}}` | `go build ./...`                         |
| `{{TEST_CMD}}` | `go test ./...`                          |
| `{{LINT_CMD}}` | `golangci-lint run`                      |
| `{{DEV_CMD}}` | `go run ./cmd/server`                    |
| `{{DOCSTYLE}}` | Go 注释                                    |
| `{{FILE_LIMIT}}` | `800`                                    |
| `{{ARCH_LAYER}}` | 分层 `handler → service → repository`，依赖单向 |
| `{{TEST_NAMING}}` | `TestX_WhenY`                            |
| `{{MOCK_LIB}}` | `gomock` / `testify`                     |
| `{{COV_CMD}}` | `go test -cover`                         |
| `{{DEBUG_TOOL}}` | `delve`                                  |
| `{{ARCH_REVIEW_CMD}}` | `arch-review`                           |

### Go — Chi

| 参数 | 值                                        |
|------|------------------------------------------|
| `{{LANGUAGE_DESC}}` | Go、Chi、轻量标准库风格路由器                        |
| `{{FRAMEWORK_VER}}` | chi v5                                   |
| `{{LANGUAGE_RUNTIME}}` | Go 1.22+                                 |
| `{{BUILD_TOOL}}` | go mod                                   |
| `{{TEST_FRAMEWORK}}` | go test + testify                        |
| `{{COV_TOOL}}` | go test -cover (核心逻辑 ≥80%)               |
| `{{LINT_TOOL}}` | golangci-lint (go vet + staticcheck)     |
| `{{ARCH_TEST_TOOL}}` | goimports + 自定义架构检查                      |
| `{{DB_ACCESS}}` | GORM / sqlx + golang-migrate             |
| `{{LANG_TAG}}` | `-golang`                                |
| `{{HARNESS_ME_NAME}}` | `harness-me-golang`                     |
| `{{HARNESSING_CMD}}` | `harnessing-golang`                     |
| `{{BUILD_CMD}}` | `go build ./...`                         |
| `{{TEST_CMD}}` | `go test ./...`                          |
| `{{LINT_CMD}}` | `golangci-lint run`                      |
| `{{DEV_CMD}}` | `go run ./cmd/server`                    |
| `{{DOCSTYLE}}` | Go 注释                                    |
| `{{FILE_LIMIT}}` | `800`                                    |
| `{{ARCH_LAYER}}` | 分层 `handler → service → repository`，依赖单向 |
| `{{TEST_NAMING}}` | `TestX_WhenY`                            |
| `{{MOCK_LIB}}` | `gomock` / `testify`                     |
| `{{COV_CMD}}` | `go test -cover`                         |
| `{{DEBUG_TOOL}}` | `delve`                                  |
| `{{ARCH_REVIEW_CMD}}` | `arch-review`                           |

---

### Frontend 基础参数（通用）

| 参数 | 值                                                                  |
|------|--------------------------------------------------------------------|
| `{{LANGUAGE}}` | `Frontend`                                                         |
| `{{LANG_TAG}}` | `-front`                                                           |
| `{{HARNESS_ME_NAME}}` | `harness-me-front`                                                |
| `{{HARNESSING_CMD}}` | `harnessing-front`                                                |
| `{{ARCH_REVIEW_CMD}}` | `arch-review-front`                                               |
| `{{DOCSTYLE}}` | JSDoc / TSDoc                                                      |
| `{{FILE_LIMIT}}` | `400`                                                              |
| `{{TEST_NAMING}}` | component/function 描述名                                             |
| `{{RACE_DETECT_ARG}}` | 竞态检测参数：空                                                         |
| `{{ARCH_TEST_CMD}}` | 架构约束测试命令                                                       |
| `{{ASSERT_LIB}}` | 断言库：来自测试框架（如 expect/assertThat/assertSame）                                      |
| `{{DEP_CMD}}` | 依赖管理命令：`npm install`                                             |
| `{{HEALTH_CHECK_CMD}}` | 健康检查命令                                                         |
| `{{INTEGRATION_CMD}}` | 集成测试命令                                                         |
| `{{METRICS_ENDPOINT}}` | 指标端点                                                           |
| `{{RUN_CMD}}` | 启动服务命令                                                         |
| `{{SECURITY_CMD}}` | 安全扫描命令                                                         |
| `{{VET_CMD}}` | 静态分析命令：`eslint` / `vue-tsc`                                     |

### Frontend — Vue 3 + Vite

| 参数 | 值                                                                  |
|------|--------------------------------------------------------------------|
| `{{LANGUAGE}}` | `Frontend`                                                         |
| `{{LANGUAGE_DESC}}` | Vue 3、Vite、Pinia、Vue Router、TypeScript                             |
| `{{LANGUAGE_RUNTIME}}` | Node 20+                                                           |
| `{{FRAMEWORK_VER}}` | Vue 3.x+ / Vite 5.x+                                               |
| `{{BUILD_TOOL}}` | npm/pnpm/yarn                                                      |
| `{{TEST_FRAMEWORK}}` | Vitest + @vue/test-utils                                           |
| `{{COV_TOOL}}` | c8 / istanbul (核心逻辑 ≥80%)                                          |
| `{{LINT_TOOL}}` | ESLint + Prettier + vue-tsc                                        |
| `{{ARCH_TEST_TOOL}}` | ESLint import 规则 + 自定义检查                                           |
| `{{DB_ACCESS}}` | Pinia store + Axios 封装                                             |
| `{{LANG_TAG}}` | `-front`                                                           |
| `{{HARNESS_ME_NAME}}` | `harness-me-front`                                                |
| `{{HARNESSING_CMD}}` | `harnessing-front`                                                |
| `{{BUILD_CMD}}` | `npm run build`                                                    |
| `{{TEST_CMD}}` | `npm run test`                                                     |
| `{{LINT_CMD}}` | `npm run lint`                                                     |
| `{{DEV_CMD}}` | `npm run dev`                                                      |
| `{{DOCSTYLE}}` | JSDoc / TSDoc                                                      |
| `{{FILE_LIMIT}}` | `400`                                                              |
| `{{ARCH_LAYER}}` | 依赖方向 `views → components → stores → services/api → models → utils` |
| `{{TEST_NAMING}}` | component/function 描述名                                             |
| `{{MOCK_LIB}}` | `vitest` mock / msw                                                |
| `{{COV_CMD}}` | `npx vitest --coverage`                                            |
| `{{DEBUG_TOOL}}` | 浏览器 DevTools / vue-devtools                                        |
| `{{ARCH_REVIEW_CMD}}` | `arch-review-front`                                               |
| `{{ARCH_TEST_CMD}}` | 架构约束测试命令                                                           |
| `{{SECURITY_CMD}}` | 安全扫描命令                                                             |
| `{{INTEGRATION_CMD}}` | 集成测试命令                                                             |
| `{{RUN_CMD}}` | 启动服务命令                                                             |
| `{{HEALTH_CHECK_CMD}}` | 健康检查命令                                                             |
| `{{HEALTH_ENDPOINT}}` | 健康检查端点                                                             |
| `{{METRICS_ENDPOINT}}` | 指标端点                                                               |
| `{{VET_CMD}}` | 静态分析命令：`eslint --fix`                                              |
| `{{DEP_CMD}}` | 依赖管理命令：`npm install`                                               |
| `{{ORM_TOOL}}` | ORM 框架：N/A                                                         |
| `{{TYPE_CHECK_TOOL}}` | 类型检查工具：`vue-tsc` / `tsc`                                           |
| `{{TYPE_CHECK_CMD}}` | 类型检查命令：`{{TYPE_CHECK_TOOL}} --noEmit`                              |
| `{{FORMAT_TOOL}}` | 格式化工具：Prettier                                                     |
| `{{ASSERT_LIB}}` | 断言库：来自测试框架（如 expect/assertThat/assertSame）                                          |
| `{{RACE_DETECT_ARG}}` | 竞态检测参数：空                                                           |
| `{{DEVTOOLS_TOOL}}` | DevTools：Vue Devtools / React DevTools / Angular DevTools          |
| `{{ENV_LIB}}` | 测试环境库：`jsdom`                                                      |
| `{{UTIL_LIB}}` | 测试工具库：`@vue/test-utils` / `@testing-library/react`                 |
| `{{STATE_MGMT_LIB}}` | 状态管理库：Pinia / Redux / NgRx / Zustand                               |
| `{{API_FILE}}` | API 层文件：`request.ts`                                               |
| `{{METRICS_CHECK_CMD}}` | 指标检查命令：N/A                                                         |
| `{{ROLLBACK_CMD}}` | 回滚命令：`npm run rollback`                                            |

### Frontend — React + Vite

| 参数 | 值                                                                  |
|------|--------------------------------------------------------------------|
| `{{LANGUAGE_DESC}}` | React、Vite、TypeScript                                              |
| `{{FRAMEWORK_VER}}` | React 18/19 + Vite 5.x+                                            |
| `{{LANGUAGE_RUNTIME}}` | Node 20+                                                           |
| `{{BUILD_TOOL}}` | npm/pnpm/yarn                                                      |
| `{{TEST_FRAMEWORK}}` | Vitest + React Testing Library                                     |
| `{{COV_TOOL}}` | c8 / istanbul (核心逻辑 ≥80%)                                          |
| `{{LINT_TOOL}}` | ESLint + Prettier + typescript-eslint                              |
| `{{ARCH_TEST_TOOL}}` | ESLint import 规则 + 自定义检查                                           |
| `{{DB_ACCESS}}` | Redux / Zustand + Axios 封装                                         |
| `{{LANG_TAG}}` | `-front`                                                           |
| `{{HARNESS_ME_NAME}}` | `harness-me-front`                                                |
| `{{HARNESSING_CMD}}` | `harnessing-front`                                                |
| `{{BUILD_CMD}}` | `npm run build`                                                    |
| `{{TEST_CMD}}` | `npm run test`                                                     |
| `{{LINT_CMD}}` | `npm run lint`                                                     |
| `{{DEV_CMD}}` | `npm run dev`                                                      |
| `{{DOCSTYLE}}` | JSDoc / TSDoc                                                      |
| `{{FILE_LIMIT}}` | `400`                                                              |
| `{{ARCH_LAYER}}` | 依赖方向 `pages → components → stores → services/api → models → utils` |
| `{{TEST_NAMING}}` | component/function 描述名                                             |
| `{{MOCK_LIB}}` | `vitest` mock / msw                                                |
| `{{COV_CMD}}` | `npx vitest --coverage`                                            |
| `{{DEBUG_TOOL}}` | 浏览器 DevTools / react-devtools                                      |
| `{{ARCH_REVIEW_CMD}}` | `arch-review-front`                                               |

### Frontend — Next.js

| 参数 | 值                                                              |
|------|----------------------------------------------------------------|
| `{{LANGUAGE_DESC}}` | React、Next.js、全栈框架（App Router + RSC）                           |
| `{{FRAMEWORK_VER}}` | Next.js 15.x                                                   |
| `{{LANGUAGE_RUNTIME}}` | Node 20+                                                       |
| `{{BUILD_TOOL}}` | npm/pnpm/yarn                                                  |
| `{{TEST_FRAMEWORK}}` | Vitest/Jest + React Testing Library                            |
| `{{COV_TOOL}}` | c8 / istanbul (核心逻辑 ≥80%)                                      |
| `{{LINT_TOOL}}` | ESLint + Prettier + typescript-eslint                          |
| `{{ARCH_TEST_TOOL}}` | ESLint import 规则 + 自定义检查                                       |
| `{{DB_ACCESS}}` | Server Actions / Prisma + Axios 封装                             |
| `{{LANG_TAG}}` | `-front`                                                       |
| `{{HARNESS_ME_NAME}}` | `harness-me-front`                                            |
| `{{HARNESSING_CMD}}` | `harnessing-front`                                            |
| `{{BUILD_CMD}}` | `npm run build`                                                |
| `{{TEST_CMD}}` | `npm run test`                                                 |
| `{{LINT_CMD}}` | `npm run lint`                                                 |
| `{{DEV_CMD}}` | `npm run dev`                                                  |
| `{{DOCSTYLE}}` | JSDoc / TSDoc                                                  |
| `{{FILE_LIMIT}}` | `400`                                                          |
| `{{ARCH_LAYER}}` | 依赖方向 `app(route) → components → lib/services → models → utils` |
| `{{TEST_NAMING}}` | component/function 描述名                                         |
| `{{MOCK_LIB}}` | `vitest` mock / msw                                            |
| `{{COV_CMD}}` | `npx vitest --coverage`                                        |
| `{{DEBUG_TOOL}}` | 浏览器 DevTools / react-devtools                                  |
| `{{ARCH_REVIEW_CMD}}` | `arch-review-front`                                           |

### Frontend — Vue 3 + Vue CLI（Webpack）

| 参数 | 值                                                                  |
|------|--------------------------------------------------------------------|
| `{{LANGUAGE_DESC}}` | Vue 3、Vue CLI、Webpack                                              |
| `{{FRAMEWORK_VER}}` | Vue 3.x + Vue CLI 5.x                                              |
| `{{LANGUAGE_RUNTIME}}` | Node 20+                                                           |
| `{{BUILD_TOOL}}` | npm/pnpm/yarn                                                      |
| `{{TEST_FRAMEWORK}}` | Jest + @vue/test-utils                                             |
| `{{COV_TOOL}}` | istanbul (核心逻辑 ≥80%)                                               |
| `{{LINT_TOOL}}` | ESLint + Prettier                                                  |
| `{{ARCH_TEST_TOOL}}` | ESLint import 规则 + 自定义检查                                           |
| `{{DB_ACCESS}}` | Pinia store + Axios 封装                                             |
| `{{LANG_TAG}}` | `-front`                                                           |
| `{{HARNESS_ME_NAME}}` | `harness-me-front`                                                |
| `{{HARNESSING_CMD}}` | `harnessing-front`                                                |
| `{{BUILD_CMD}}` | `npm run build`                                                    |
| `{{TEST_CMD}}` | `npm run test`                                                     |
| `{{LINT_CMD}}` | `npm run lint`                                                     |
| `{{DEV_CMD}}` | `npm run serve`                                                    |
| `{{DOCSTYLE}}` | JSDoc / TSDoc                                                      |
| `{{FILE_LIMIT}}` | `400`                                                              |
| `{{ARCH_LAYER}}` | 依赖方向 `views → components → stores → services/api → models → utils` |
| `{{TEST_NAMING}}` | component/function 描述名                                             |
| `{{MOCK_LIB}}` | Jest mock / msw                                                    |
| `{{COV_CMD}}` | `npx jest --coverage`                                              |
| `{{DEBUG_TOOL}}` | 浏览器 DevTools / vue-devtools                                        |
| `{{ARCH_REVIEW_CMD}}` | `arch-review-front`                                               |

### Frontend — Angular

| 参数 | 值                                                   |
|------|-----------------------------------------------------|
| `{{LANGUAGE_DESC}}` | Angular、Angular CLI、TypeScript                      |
| `{{FRAMEWORK_VER}}` | Angular 18+                                         |
| `{{LANGUAGE_RUNTIME}}` | Node 20+                                            |
| `{{BUILD_TOOL}}` | npm/pnpm/yarn                                       |
| `{{TEST_FRAMEWORK}}` | Jasmine + Karma / Jest                              |
| `{{COV_TOOL}}` | Karma 覆盖率 / istanbul (核心逻辑 ≥80%)                    |
| `{{LINT_TOOL}}` | ESLint + Prettier                                   |
| `{{ARCH_TEST_TOOL}}` | ESLint import 规则 + 自定义检查                            |
| `{{DB_ACCESS}}` | NgRx / Signal + Axios 封装                            |
| `{{LANG_TAG}}` | `-front`                                            |
| `{{HARNESS_ME_NAME}}` | `harness-me-front`                                 |
| `{{HARNESSING_CMD}}` | `harnessing-front`                                 |
| `{{BUILD_CMD}}` | `npm run build`                                     |
| `{{TEST_CMD}}` | `npm run test`                                      |
| `{{LINT_CMD}}` | `npm run lint`                                      |
| `{{DEV_CMD}}` | `npm run start`                                     |
| `{{DOCSTYLE}}` | JSDoc / TSDoc                                       |
| `{{FILE_LIMIT}}` | `400`                                               |
| `{{ARCH_LAYER}}` | 依赖方向 `components → services → models → utils`，模块化分层 |
| `{{TEST_NAMING}}` | component/function 描述名                              |
| `{{MOCK_LIB}}` | Jasmine spies / Jest mock                           |
| `{{COV_CMD}}` | `npx ng test --code-coverage`                       |
| `{{DEBUG_TOOL}}` | 浏览器 DevTools / Angular DevTools                     |
| `{{ARCH_REVIEW_CMD}}` | `arch-review-front`                                |

### Frontend — Svelte + Vite

| 参数 | 值                                                                  |
|------|--------------------------------------------------------------------|
| `{{LANGUAGE_DESC}}` | Svelte、Vite、TypeScript                                             |
| `{{FRAMEWORK_VER}}` | Svelte 5.x + Vite 5.x+                                             |
| `{{LANGUAGE_RUNTIME}}` | Node 20+                                                           |
| `{{BUILD_TOOL}}` | npm/pnpm/yarn                                                      |
| `{{TEST_FRAMEWORK}}` | Vitest + @testing-library/svelte                                   |
| `{{COV_TOOL}}` | c8 / istanbul (核心逻辑 ≥80%)                                          |
| `{{LINT_TOOL}}` | ESLint + Prettier + svelte-check                                   |
| `{{ARCH_TEST_TOOL}}` | ESLint import 规则 + 自定义检查                                           |
| `{{DB_ACCESS}}` | Svelte stores + Axios 封装                                           |
| `{{LANG_TAG}}` | `-front`                                                           |
| `{{HARNESS_ME_NAME}}` | `harness-me-front`                                                |
| `{{HARNESSING_CMD}}` | `harnessing-front`                                                |
| `{{BUILD_CMD}}` | `npm run build`                                                    |
| `{{TEST_CMD}}` | `npm run test`                                                     |
| `{{LINT_CMD}}` | `npm run lint`                                                     |
| `{{DEV_CMD}}` | `npm run dev`                                                      |
| `{{DOCSTYLE}}` | JSDoc / TSDoc                                                      |
| `{{FILE_LIMIT}}` | `400`                                                              |
| `{{ARCH_LAYER}}` | 依赖方向 `pages → components → stores → services/api → models → utils` |
| `{{TEST_NAMING}}` | component/function 描述名                                             |
| `{{MOCK_LIB}}` | `vitest` mock / msw                                                |
| `{{COV_CMD}}` | `npx vitest --coverage`                                            |
| `{{DEBUG_TOOL}}` | 浏览器 DevTools / Svelte DevTools                                     |
| `{{ARCH_REVIEW_CMD}}` | `arch-review-front`                                               |

### Frontend — Nuxt

| 参数 | 值                                                            |
|------|--------------------------------------------------------------|
| `{{LANGUAGE_DESC}}` | Vue、Nuxt、全栈框架（SSR/SSG + 自动导入）                                |
| `{{FRAMEWORK_VER}}` | Nuxt 4.x                                                     |
| `{{LANGUAGE_RUNTIME}}` | Node 20+                                                     |
| `{{BUILD_TOOL}}` | npm/pnpm/yarn                                                |
| `{{TEST_FRAMEWORK}}` | Vitest + @vue/test-utils                                     |
| `{{COV_TOOL}}` | c8 / istanbul (核心逻辑 ≥80%)                                    |
| `{{LINT_TOOL}}` | ESLint + Prettier + vue-tsc                                  |
| `{{ARCH_TEST_TOOL}}` | ESLint import 规则 + 自定义检查                                     |
| `{{DB_ACCESS}}` | Pinia store + Nitro API + Axios 封装                           |
| `{{LANG_TAG}}` | `-front`                                                     |
| `{{HARNESS_ME_NAME}}` | `harness-me-front`                                          |
| `{{HARNESSING_CMD}}` | `harnessing-front`                                          |
| `{{BUILD_CMD}}` | `npm run build`                                              |
| `{{TEST_CMD}}` | `npm run test`                                               |
| `{{LINT_CMD}}` | `npm run lint`                                               |
| `{{DEV_CMD}}` | `npm run dev`                                                |
| `{{DOCSTYLE}}` | JSDoc / TSDoc                                                |
| `{{FILE_LIMIT}}` | `400`                                                        |
| `{{ARCH_LAYER}}` | 依赖方向 `pages → components → composables → server/api → utils` |
| `{{TEST_NAMING}}` | component/function 描述名                                       |
| `{{MOCK_LIB}}` | `vitest` mock / msw                                          |
| `{{COV_CMD}}` | `npx vitest --coverage`                                      |
| `{{DEBUG_TOOL}}` | 浏览器 DevTools / vue-devtools                                  |
| `{{ARCH_REVIEW_CMD}}` | `arch-review-front`                                         |

### Frontend — React + CRA（Webpack）

| 参数 | 值                                                                  |
|------|--------------------------------------------------------------------|
| `{{LANGUAGE_DESC}}` | React、Create React App、Webpack                                     |
| `{{FRAMEWORK_VER}}` | React 18 + CRA 5.x                                                 |
| `{{LANGUAGE_RUNTIME}}` | Node 20+                                                           |
| `{{BUILD_TOOL}}` | npm/pnpm/yarn                                                      |
| `{{TEST_FRAMEWORK}}` | Jest + React Testing Library                                       |
| `{{COV_TOOL}}` | istanbul (核心逻辑 ≥80%)                                               |
| `{{LINT_TOOL}}` | ESLint + Prettier                                                  |
| `{{ARCH_TEST_TOOL}}` | ESLint import 规则 + 自定义检查                                           |
| `{{DB_ACCESS}}` | Redux / Zustand + Axios 封装                                         |
| `{{LANG_TAG}}` | `-front`                                                           |
| `{{HARNESS_ME_NAME}}` | `harness-me-front`                                                |
| `{{HARNESSING_CMD}}` | `harnessing-front`                                                |
| `{{BUILD_CMD}}` | `npm run build`                                                    |
| `{{TEST_CMD}}` | `npm run test`                                                     |
| `{{LINT_CMD}}` | `npm run lint`                                                     |
| `{{DEV_CMD}}` | `npm run start`                                                    |
| `{{DOCSTYLE}}` | JSDoc / TSDoc                                                      |
| `{{FILE_LIMIT}}` | `400`                                                              |
| `{{ARCH_LAYER}}` | 依赖方向 `pages → components → stores → services/api → models → utils` |
| `{{TEST_NAMING}}` | component/function 描述名                                             |
| `{{MOCK_LIB}}` | Jest mock / msw                                                    |
| `{{COV_CMD}}` | `npx react-scripts test --coverage`                                |
| `{{DEBUG_TOOL}}` | 浏览器 DevTools / react-devtools                                      |
| `{{ARCH_REVIEW_CMD}}` | `arch-review-front`                                               |

### Frontend — 纯 Vite（无前端框架）

| 参数 | 值                               |
|------|---------------------------------|
| `{{LANGUAGE_DESC}}` | 纯 Vite、TypeScript               |
| `{{FRAMEWORK_VER}}` | Vite 5.x+                       |
| `{{LANGUAGE_RUNTIME}}` | Node 20+                        |
| `{{BUILD_TOOL}}` | npm/pnpm/yarn                   |
| `{{TEST_FRAMEWORK}}` | Vitest                          |
| `{{COV_TOOL}}` | c8 / istanbul (核心逻辑 ≥80%)       |
| `{{LINT_TOOL}}` | ESLint + Prettier               |
| `{{ARCH_TEST_TOOL}}` | ESLint import 规则 + 自定义检查        |
| `{{DB_ACCESS}}` | 无特定状态管理                         |
| `{{LANG_TAG}}` | `-front`                        |
| `{{HARNESS_ME_NAME}}` | `harness-me-front`             |
| `{{HARNESSING_CMD}}` | `harnessing-front`             |
| `{{BUILD_CMD}}` | `npm run build`                 |
| `{{TEST_CMD}}` | `npm run test`                  |
| `{{LINT_CMD}}` | `npm run lint`                  |
| `{{DEV_CMD}}` | `npm run dev`                   |
| `{{DOCSTYLE}}` | JSDoc / TSDoc                   |
| `{{FILE_LIMIT}}` | `400`                           |
| `{{ARCH_LAYER}}` | 依赖方向 `src → components → utils` |
| `{{TEST_NAMING}}` | function 描述名                    |
| `{{MOCK_LIB}}` | `vitest` mock                   |
| `{{COV_CMD}}` | `npx vitest --coverage`         |
| `{{DEBUG_TOOL}}` | 浏览器 DevTools                    |
| `{{ARCH_REVIEW_CMD}}` | `arch-review-front`            |

### 参数块使用说明（差异化）

> 以下参数块采用**差异化**写法：每个语言先给出「基础参数」，各框架块只列出与基础不同的参数（未列出的继承基础）。ML/AI/LLM 框架无传统 Web 分层与数据库访问，ARCH_LAYER/DB_ACCESS 改为其适用含义。

### Java 基础参数（JDK 21 + Maven Web 家族通用）

| 参数 | 值                            |
|------|------------------------------|
| `{{LANGUAGE}}` | `Java`                       |
| `{{LANGUAGE_RUNTIME}}` | JDK 21 LTS                   |
| `{{TEST_FRAMEWORK}}` | JUnit 5 + Mockito + AssertJ  |
| `{{COV_TOOL}}` | JaCoCo (核心逻辑 ≥80%)           |
| `{{LINT_TOOL}}` | Checkstyle + PMD + SpotBugs  |
| `{{ARCH_TEST_TOOL}}` | ArchUnit                     |
| `{{LANG_TAG}}` | `-java`                      |
| `{{HARNESS_ME_NAME}}` | `harness-me`                |
| `{{HARNESSING_CMD}}` | `harnessing`                |
| `{{BUILD_CMD}}` | `mvn compile`                |
| `{{TEST_CMD}}` | `mvn test`                   |
| `{{LINT_CMD}}` | `mvn checkstyle:check`       |
| `{{COV_CMD}}` | `mvn jacoco:report`          |
| `{{DOCSTYLE}}` | `Javadoc`                    |
| `{{FILE_LIMIT}}` | `500`                        |
| `{{TEST_NAMING}}` | `method_should_x_when_y`     |
| `{{MOCK_LIB}}` | `Mockito`                    |
| `{{DEBUG_TOOL}}` | 调试器 / `jdb`                  |
| `{{ARCH_REVIEW_CMD}}` | `arch-review`               |
| `{{ARCH_TEST_CMD}}` | 架构约束测试命令                     |
| `{{SECURITY_CMD}}` | 安全扫描命令                       |
| `{{INTEGRATION_CMD}}` | 集成测试命令                       |
| `{{RUN_CMD}}` | 启动服务命令                       |
| `{{HEALTH_CHECK_CMD}}` | 健康检查命令                       |
| `{{HEALTH_ENDPOINT}}` | 健康检查端点                       |
| `{{METRICS_ENDPOINT}}` | 指标端点                         |
| `{{VET_CMD}}` | 静态分析命令（实际合并到 LINT_CMD）       |
| `{{DEP_CMD}}` | 依赖管理命令：`mvn dependency:tree` |
| `{{ORM_TOOL}}` | ORM 框架：MyBatis-Plus / JPA    |
| `{{TYPE_CHECK_TOOL}}` | 类型检查工具：javac（编译时检查）          |
| `{{TYPE_CHECK_CMD}}` | 类型检查命令：`{{BUILD_CMD}}`       |
| `{{ASSERT_LIB}}` | 断言库：AssertJ                  |
| `{{RACE_DETECT_ARG}}` | 竞态检测参数：空                     |
| `{{ARCH_LAYER}}` | 模块间仅通过接口通信，依赖方向 `common ← service ← server` |

### Java — Dubbo（微服务 RPC）

| 参数 | 值 |
|------|-----|
| `{{LANGUAGE_DESC}}` | Java、Dubbo、微服务 RPC 架构 |
| `{{FRAMEWORK_VER}}` | Dubbo 3.x |
| `{{DEV_CMD}}` | `mvn dubbo:run` / 启动 Provider |
| `{{ARCH_LAYER}}` | Provider/Consumer 通过 RPC 接口通信，禁止直接依赖实现 |
| `{{DB_ACCESS}}` | MyBatis-Plus / JPA + Flyway |

### Java — Spring Cloud Alibaba

| 参数 | 值 |
|------|-----|
| `{{LANGUAGE_DESC}}` | Java、Spring Cloud Alibaba、微服务生态（Nacos/Sentinel/Seata） |
| `{{FRAMEWORK_VER}}` | Spring Cloud 2023.x + Alibaba |
| `{{DEV_CMD}}` | `mvn spring-boot:run` |
| `{{ARCH_LAYER}}` | 服务间通过 OpenFeign/RestTemplate 通信，网关 → 微服务 → 注册中心 |
| `{{DB_ACCESS}}` | MyBatis-Plus / JPA + Flyway |

### Java — Spring AI（LLM 应用）

| 参数 | 值 |
|------|-----|
| `{{LANGUAGE_DESC}}` | Java、Spring AI、LLM/RAG 应用框架 |
| `{{FRAMEWORK_VER}}` | Spring AI 1.0.x |
| `{{DEV_CMD}}` | `mvn spring-boot:run` |
| `{{ARCH_LAYER}}` | Controller → Service → AI Client（绑定 Model/Vector Store） |
| `{{DB_ACCESS}}` | 向量库（PgVector/Redis） + 元数据存储 |

### Java — Spring AI Alibaba

| 参数 | 值 |
|------|-----|
| `{{LANGUAGE_DESC}}` | Java、Spring AI Alibaba、阿里云百炼 LLM 应用 |
| `{{FRAMEWORK_VER}}` | Spring AI Alibaba 1.0.x |
| `{{DEV_CMD}}` | `mvn spring-boot:run` |
| `{{ARCH_LAYER}}` | Controller → Service → AI Client（百炼 Model + Vector Store） |
| `{{DB_ACCESS}}` | 向量库 + 元数据存储 |

### Java — AgentScope Java（多 Agent）

| 参数 | 值 |
|------|-----|
| `{{LANGUAGE_DESC}}` | Java、AgentScope、多 Agent 协同框架 |
| `{{FRAMEWORK_VER}}` | AgentScope Java |
| `{{DEV_CMD}}` | `mvn spring-boot:run` |
| `{{ARCH_LAYER}}` | Agent → Tool → LLM，Agent 间通过消息通信 |
| `{{DB_ACCESS}}` | 向量库 / 会话存储 |

### Java — LangChain4j（LLM 编排）

| 参数 | 值 |
|------|-----|
| `{{LANGUAGE_DESC}}` | Java、LangChain4j、LLM 编排与 RAG 框架 |
| `{{FRAMEWORK_VER}}` | langchain4j 1.x |
| `{{DEV_CMD}}` | `mvn spring-boot:run` |
| `{{ARCH_LAYER}}` | 分层 controller → service → langchain4j(chain/agent/tool) |
| `{{DB_ACCESS}}` | 向量库（PgVector/OpenSearch） + 嵌入式存储 |

### Java — Semantic Kernel（AI 编排）

| 参数 | 值 |
|------|-----|
| `{{LANGUAGE_DESC}}` | Java、Semantic Kernel、AI 编排与技能框架 |
| `{{FRAMEWORK_VER}}` | Semantic Kernel Java 版 |
| `{{DEV_CMD}}` | `mvn spring-boot:run` |
| `{{ARCH_LAYER}}` | Kernel → Plugin → Connector，插件化组织 |
| `{{DB_ACCESS}}` | 向量库 / 内存存储 |

### Java — Genkit Java（AI 后端）

| 参数 | 值 |
|------|-----|
| `{{LANGUAGE_DESC}}` | Java、Firebase Genkit、AI 后端工具链 |
| `{{FRAMEWORK_VER}}` | Genkit Java 版 |
| `{{DEV_CMD}}` | `mvn spring-boot:run` |
| `{{ARCH_LAYER}}` | Flow → Tool → LLM，Flow 编排 AI 逻辑 |
| `{{DB_ACCESS}}` | 向量库 / 会话存储 |

---

### Python 基础参数（通用）

| 参数 | 值                                   |
|------|-------------------------------------|
| `{{LANGUAGE}}` | `Python`                            |
| `{{LANGUAGE_RUNTIME}}` | Python 3.11+                        |
| `{{COV_TOOL}}` | pytest-cov (核心逻辑 ≥80%)              |
| `{{LINT_TOOL}}` | flake8 + mypy + black + isort       |
| `{{LANG_TAG}}` | `-python`                           |
| `{{HARNESS_ME_NAME}}` | `harness-me-python`                |
| `{{HARNESSING_CMD}}` | `harnessing-python`                |
| `{{BUILD_CMD}}` | `python -m compileall .`            |
| `{{TEST_CMD}}` | `python -m pytest`                  |
| `{{LINT_CMD}}` | `flake8 .`                          |
| `{{COV_CMD}}` | `pytest --cov`                      |
| `{{DOCSTYLE}}` | `docstring`                         |
| `{{FILE_LIMIT}}` | `800`                               |
| `{{TEST_NAMING}}` | `test_x_when_y`                     |
| `{{DEBUG_TOOL}}` | `pdb` / `breakpoint()` / `ipdb`     |
| `{{ARCH_REVIEW_CMD}}` | `arch-review-python`               |
| `{{ARCH_TEST_CMD}}` | 架构约束测试命令                            |
| `{{SECURITY_CMD}}` | 安全扫描命令                              |
| `{{INTEGRATION_CMD}}` | 集成测试命令                              |
| `{{RUN_CMD}}` | 启动服务命令                              |
| `{{HEALTH_CHECK_CMD}}` | 健康检查命令                              |
| `{{HEALTH_ENDPOINT}}` | 健康检查端点                              |
| `{{METRICS_ENDPOINT}}` | 指标端点                                |
| `{{VET_CMD}}` | 静态分析命令：`mypy`                       |
| `{{DEP_CMD}}` | 依赖管理命令：`pip install` / `poetry add` |
| `{{ORM_TOOL}}` | ORM 框架：SQLAlchemy                   |
| `{{TYPE_CHECK_TOOL}}` | 类型检查工具：mypy                         |
| `{{TYPE_CHECK_CMD}}` | 类型检查命令：`mypy`                       |
| `{{FORMAT_TOOL}}` | 格式化工具：black                         |
| `{{FORMAT_CHECK_CMD}}` | 格式检查命令：`black --check .`            |
| `{{METRICS_CHECK_CMD}}` | 指标检查命令：`curl {{METRICS_ENDPOINT}}`  |
| `{{ASSERT_LIB}}` | 断言库：`assert`                        |
| `{{RACE_DETECT_ARG}}` | 竞态检测参数：空                            |
| `{{TEST_FRAMEWORK}}` | pytest + pytest-mock                      |
| `{{MOCK_LIB}}` | `pytest-mock`                             |
| `{{ARCH_LAYER}}` | 依赖方向 `router → service → model`（框架块可覆盖） |

### Python — Tornado（异步 Web）

| 参数 | 值 |
|------|-----|
| `{{LANGUAGE_DESC}}` | Python、Tornado、异步非阻塞 Web 框架 |
| `{{FRAMEWORK_VER}}` | Tornado 6.x |
| `{{BUILD_TOOL}}` | pip / poetry / uv |
| `{{ARCH_TEST_TOOL}}` | 自定义 import-lint 检查 |
| `{{DB_ACCESS}}` | SQLAlchemy + Alembic |
| `{{DEV_CMD}}` | `python -m tornado.autoreload main.py` |
| `{{ARCH_LAYER}}` | 依赖方向 RequestHandler → service → model |
| `{{MOCK_LIB}}` | `pytest-mock` |

### Python — TensorFlow（深度学习）

| 参数 | 值 |
|------|-----|
| `{{LANGUAGE_DESC}}` | Python、TensorFlow、深度学习框架 |
| `{{FRAMEWORK_VER}}` | TensorFlow 2.x |
| `{{BUILD_TOOL}}` | pip / poetry / uv |
| `{{ARCH_TEST_TOOL}}` | 自定义 import-lint 检查 |
| `{{DB_ACCESS}}` | 数据集管道（tf.data）+ 模型检查点 |
| `{{DEV_CMD}}` | `python -m pytest`（训练脚本按需运行） |
| `{{ARCH_LAYER}}` | 数据加载 → 模型构建 → 训练/评估 → 服务导出 |
| `{{MOCK_LIB}}` | `pytest-mock` |

### Python — PyTorch（深度学习）

| 参数 | 值 |
|------|-----|
| `{{LANGUAGE_DESC}}` | Python、PyTorch、深度学习框架 |
| `{{FRAMEWORK_VER}}` | PyTorch 2.x |
| `{{BUILD_TOOL}}` | pip / poetry / uv |
| `{{ARCH_TEST_TOOL}}` | 自定义 import-lint 检查 |
| `{{DB_ACCESS}}` | DataLoader 数据集 + 模型检查点 |
| `{{DEV_CMD}}` | `python -m pytest` |
| `{{ARCH_LAYER}}` | 数据加载 → 模型构建 → 训练/评估 → 服务导出 |
| `{{MOCK_LIB}}` | `pytest-mock` |

### Python — Keras（高层 API）

| 参数 | 值 |
|------|-----|
| `{{LANGUAGE_DESC}}` | Python、Keras、高层深度学习 API |
| `{{FRAMEWORK_VER}}` | Keras 3.x |
| `{{BUILD_TOOL}}` | pip / poetry / uv |
| `{{ARCH_TEST_TOOL}}` | 自定义 import-lint 检查 |
| `{{DB_ACCESS}}` | 数据集 + 模型检查点 |
| `{{DEV_CMD}}` | `python -m pytest` |
| `{{ARCH_LAYER}}` | 数据加载 → 模型构建 → 训练/评估 |
| `{{MOCK_LIB}}` | `pytest-mock` |

### Python — scikit-learn（机器学习）

| 参数 | 值 |
|------|-----|
| `{{LANGUAGE_DESC}}` | Python、scikit-learn、经典机器学习库 |
| `{{FRAMEWORK_VER}}` | scikit-learn 1.x |
| `{{BUILD_TOOL}}` | pip / poetry / uv |
| `{{ARCH_TEST_TOOL}}` | 自定义 import-lint 检查 |
| `{{DB_ACCESS}}` | 数据集 + 模型持久化（joblib/pickle） |
| `{{DEV_CMD}}` | `python -m pytest` |
| `{{ARCH_LAYER}}` | 数据预处理 → 特征工程 → 建模 → 评估 |
| `{{MOCK_LIB}}` | `pytest-mock` |

### Python — XGBoost（梯度提升）

| 参数 | 值 |
|------|-----|
| `{{LANGUAGE_DESC}}` | Python、XGBoost、梯度提升树库 |
| `{{FRAMEWORK_VER}}` | XGBoost 2.x |
| `{{BUILD_TOOL}}` | pip / poetry / uv |
| `{{ARCH_TEST_TOOL}}` | 自定义 import-lint 检查 |
| `{{DB_ACCESS}}` | 数据集 + 模型持久化 |
| `{{DEV_CMD}}` | `python -m pytest` |
| `{{ARCH_LAYER}}` | 数据处理 → 建模 → 评估 |
| `{{MOCK_LIB}}` | `pytest-mock` |

### Python — Hugging Face Transformers（NLP）

| 参数 | 值 |
|------|-----|
| `{{LANGUAGE_DESC}}` | Python、Hugging Face Transformers、预训练模型库 |
| `{{FRAMEWORK_VER}}` | transformers 4.x |
| `{{BUILD_TOOL}}` | pip / poetry / uv |
| `{{ARCH_TEST_TOOL}}` | 自定义 import-lint 检查 |
| `{{DB_ACCESS}}` | 预训练模型 + 数据集（datasets） |
| `{{DEV_CMD}}` | `python -m pytest` |
| `{{ARCH_LAYER}}` | 数据加载 → 模型加载 → fine-tune → 推理 |
| `{{MOCK_LIB}}` | `pytest-mock` |

### Python — LangChain（LLM 应用）

| 参数 | 值 |
|------|-----|
| `{{LANGUAGE_DESC}}` | Python、LangChain、LLM 应用编排框架 |
| `{{FRAMEWORK_VER}}` | LangChain 0.3.x / 1.x |
| `{{BUILD_TOOL}}` | pip / poetry / uv |
| `{{ARCH_TEST_TOOL}}` | 自定义 import-lint 检查 |
| `{{DB_ACCESS}}` | 向量库（Chroma/Pinecone） + LangSmith 追踪 |
| `{{DEV_CMD}}` | `python -m pytest` |
| `{{ARCH_LAYER}}` | Chain → Model → Tool/Retriever，链式编排 |
| `{{MOCK_LIB}}` | `pytest-mock` |

### Python — LangGraph（Agent 图）

| 参数 | 值 |
|------|-----|
| `{{LANGUAGE_DESC}}` | Python、LangGraph、有状态多 Agent 图编排 |
| `{{FRAMEWORK_VER}}` | LangGraph 1.x |
| `{{BUILD_TOOL}}` | pip / poetry / uv |
| `{{ARCH_TEST_TOOL}}` | 自定义 import-lint 检查 |
| `{{DB_ACCESS}}` | 向量库 + 状态存储（Checkpointer） |
| `{{DEV_CMD}}` | `python -m pytest` |
| `{{ARCH_LAYER}}` | StateGraph 节点 → 边，Agent 状态机编排 |
| `{{MOCK_LIB}}` | `pytest-mock` |

### Python — CrewAI（Agent 团队）

| 参数 | 值 |
|------|-----|
| `{{LANGUAGE_DESC}}` | Python、CrewAI、多 Agent 协作框架 |
| `{{FRAMEWORK_VER}}` | CrewAI 0.x |
| `{{BUILD_TOOL}}` | pip / poetry / uv |
| `{{ARCH_TEST_TOOL}}` | 自定义 import-lint 检查 |
| `{{DB_ACCESS}}` | 向量库 + 任务/会话存储 |
| `{{DEV_CMD}}` | `python -m pytest` |
| `{{ARCH_LAYER}}` | Agent + Task + Crew/Process，角色化协作 |
| `{{MOCK_LIB}}` | `pytest-mock` |

### Python — PydanticAI（AI Agent）

| 参数 | 值 |
|------|-----|
| `{{LANGUAGE_DESC}}` | Python、PydanticAI、类型安全 AI Agent 框架 |
| `{{FRAMEWORK_VER}}` | pydantic-ai |
| `{{BUILD_TOOL}}` | pip / poetry / uv |
| `{{ARCH_TEST_TOOL}}` | 自定义 import-lint 检查 |
| `{{DB_ACCESS}}` | 向量库 / 会话存储 |
| `{{DEV_CMD}}` | `python -m pytest` |
| `{{ARCH_LAYER}}` | Agent → Tool → Model，Pydantic 结构化输出 |
| `{{MOCK_LIB}}` | `pytest-mock` |

### Python — SmolAgents（轻量 Agent）

| 参数 | 值 |
|------|-----|
| `{{LANGUAGE_DESC}}` | Python、SmolAgents、轻量 Agent 框架 |
| `{{FRAMEWORK_VER}}` | smolagents |
| `{{BUILD_TOOL}}` | pip / poetry / uv |
| `{{ARCH_TEST_TOOL}}` | 自定义 import-lint 检查 |
| `{{DB_ACCESS}}` | 向量库 / 会话存储 |
| `{{DEV_CMD}}` | `python -m pytest` |
| `{{ARCH_LAYER}}` | Agent → Tool → Model，轻量编排 |
| `{{MOCK_LIB}}` | `pytest-mock` |

### Python — OpenAI Agents SDK（Agent）

| 参数 | 值 |
|------|-----|
| `{{LANGUAGE_DESC}}` | Python、OpenAI Agents SDK、Agent 编排框架 |
| `{{FRAMEWORK_VER}}` | openai-agents |
| `{{BUILD_TOOL}}` | pip / poetry / uv |
| `{{ARCH_TEST_TOOL}}` | 自定义 import-lint 检查 |
| `{{DB_ACCESS}}` | 向量库 / 会话存储 |
| `{{DEV_CMD}}` | `python -m pytest` |
| `{{ARCH_LAYER}}` | Agent → Handoff → Guardrail → Tool，层级编排 |
| `{{MOCK_LIB}}` | `pytest-mock` |

---

### Go 基础参数（通用）

| 参数 | 值                                    |
|------|--------------------------------------|
| `{{LANGUAGE}}` | `Go`                                 |
| `{{LANGUAGE_RUNTIME}}` | Go 1.22+                             |
| `{{COV_TOOL}}` | go test -cover (核心逻辑 ≥80%)           |
| `{{LINT_TOOL}}` | golangci-lint (go vet + staticcheck) |
| `{{ARCH_TEST_TOOL}}` | goimports + 自定义架构检查                  |
| `{{LANG_TAG}}` | `-golang`                            |
| `{{HARNESS_ME_NAME}}` | `harness-me-golang`                 |
| `{{HARNESSING_CMD}}` | `harnessing-golang`                 |
| `{{BUILD_CMD}}` | `go build ./...`                     |
| `{{TEST_CMD}}` | `go test ./...`                      |
| `{{LINT_CMD}}` | `golangci-lint run`                  |
| `{{COV_CMD}}` | `go test -cover`                     |
| `{{DOCSTYLE}}` | Go 注释                                |
| `{{FILE_LIMIT}}` | `800`                                |
| `{{TEST_NAMING}}` | `TestX_WhenY`                        |
| `{{DEBUG_TOOL}}` | `delve`                              |
| `{{ARCH_REVIEW_CMD}}` | `arch-review`                       |
| `{{ARCH_TEST_CMD}}` | 架构约束测试命令                             |
| `{{SECURITY_CMD}}` | 安全扫描命令                               |
| `{{INTEGRATION_CMD}}` | 集成测试命令                               |
| `{{RUN_CMD}}` | 启动服务命令                               |
| `{{HEALTH_CHECK_CMD}}` | 健康检查命令                               |
| `{{HEALTH_ENDPOINT}}` | 健康检查端点                               |
| `{{METRICS_ENDPOINT}}` | 指标端点                                 |
| `{{VET_CMD}}` | 静态分析命令：`go vet ./...`                |
| `{{DEP_CMD}}` | 依赖管理命令：`go mod tidy`                 |
| `{{ORM_TOOL}}` | ORM 框架：GORM / sqlx                   |
| `{{TYPE_CHECK_TOOL}}` | 类型检查工具：go vet                        |
| `{{TYPE_CHECK_CMD}}` | 类型检查命令：`go vet ./...`                |
| `{{ASSERT_LIB}}` | 断言库：testify                          |
| `{{RACE_DETECT_ARG}}` | 竞态检测参数：`-race`                       |
| `{{HTTP_MOCK_UTIL}}` | HTTP Mock 工具：`httptest.Server`       |
| `{{FRAMEWORK_NAME}}` | 框架名称（从 {{LANGUAGE_DESC}} 提取）        |
| `{{TEST_FRAMEWORK}}` | go test + testify                       |
| `{{MOCK_LIB}}` | `gomock` / `testify`                    |
| `{{ARCH_LAYER}}` | 分层 `handler → service → repository`，依赖单向 |

### Go — Beego

| 参数 | 值 |
|------|-----|
| `{{LANGUAGE_DESC}}` | Go、Beego、全栈 Go Web 框架 |
| `{{FRAMEWORK_VER}}` | Beego 2.x |
| `{{DEV_CMD}}` | `go run main.go` |
| `{{ARCH_LAYER}}` | 分层 controller → models/services → ORM，依赖单向 |
| `{{DB_ACCESS}}` | Beego ORM + 迁移 |

### Go — Go-Kit

| 参数 | 值 |
|------|-----|
| `{{LANGUAGE_DESC}}` | Go、Go-Kit、微服务工具箱 |
| `{{FRAMEWORK_VER}}` | Go-Kit 0.13.x |
| `{{DEV_CMD}}` | `go run ./cmd/` |
| `{{ARCH_LAYER}}` | Transport → Endpoint → Service（Go-Kit 三层） |
| `{{DB_ACCESS}}` | GORM / sqlx + golang-migrate |

### Go — Go-Kratos

| 参数 | 值 |
|------|-----|
| `{{LANGUAGE_DESC}}` | Go、Go-Kratos、微服务框架 |
| `{{FRAMEWORK_VER}}` | Kratos v2 |
| `{{DEV_CMD}}` | `go run service-api/main.go` |
| `{{ARCH_LAYER}}` | 分层 api → service → biz → data（Kratos 标准） |
| `{{DB_ACCESS}}` | GORM + ent + golang-migrate |

### Go — Gorilla Mux

| 参数 | 值 |
|------|-----|
| `{{LANGUAGE_DESC}}` | Go、Gorilla Mux、标准库风格路由器 |
| `{{FRAMEWORK_VER}}` | gorilla/mux v1 |
| `{{DEV_CMD}}` | `go run ./cmd/server` |
| `{{ARCH_LAYER}}` | 分层 handler → service → repository，依赖单向 |
| `{{DB_ACCESS}}` | GORM / sqlx + golang-migrate |

### Go — Kitex（RPC）

| 参数 | 值 |
|------|-----|
| `{{LANGUAGE_DESC}}` | Go、Kitex、字节高性能 RPC 框架 |
| `{{FRAMEWORK_VER}}` | Kitex 0.x |
| `{{DEV_CMD}}` | `go run .` |
| `{{ARCH_LAYER}}` | Client/Server 通过 IDL 生成代码通信，禁止直接耦合 |
| `{{DB_ACCESS}}` | GORM / sqlx + golang-migrate |

### Go — Hertz（HTTP）

| 参数 | 值 |
|------|-----|
| `{{LANGUAGE_DESC}}` | Go、Hertz、字节高性能 HTTP 框架 |
| `{{FRAMEWORK_VER}}` | Hertz v0.x |
| `{{DEV_CMD}}` | `go run .` |
| `{{ARCH_LAYER}}` | 分层 handler → service → repository，依赖单向 |
| `{{DB_ACCESS}}` | GORM / sqlx + golang-migrate |

### Go — Iris

| 参数 | 值 |
|------|-----|
| `{{LANGUAGE_DESC}}` | Go、Iris、Web 框架 |
| `{{FRAMEWORK_VER}}` | Iris v12 |
| `{{DEV_CMD}}` | `go run main.go` |
| `{{ARCH_LAYER}}` | 分层 handler → service → repository，依赖单向 |
| `{{DB_ACCESS}}` | GORM / sqlx + golang-migrate |

### Go — Macaron

| 参数 | 值 |
|------|-----|
| `{{LANGUAGE_DESC}}` | Go、Macaron、轻量 Web 框架 |
| `{{FRAMEWORK_VER}}` | Macaron v1 |
| `{{DEV_CMD}}` | `go run main.go` |
| `{{ARCH_LAYER}}` | 分层 handler → service → repository，依赖单向 |
| `{{DB_ACCESS}}` | GORM / sqlx + golang-migrate |

### Go — Tango

| 参数 | 值 |
|------|-----|
| `{{LANGUAGE_DESC}}` | Go、Tango、轻量 Web 框架 |
| `{{FRAMEWORK_VER}}` | Tango |
| `{{DEV_CMD}}` | `go run main.go` |
| `{{ARCH_LAYER}}` | 分层 handler → service → repository，依赖单向 |
| `{{DB_ACCESS}}` | GORM / sqlx + golang-migrate |

### Go — goframe

| 参数 | 值 |
|------|-----|
| `{{LANGUAGE_DESC}}` | Go、GoFrame、全栈 Web 开发框架 |
| `{{FRAMEWORK_VER}}` | GoFrame v2 |
| `{{DEV_CMD}}` | `go run main.go` |
| `{{ARCH_LAYER}}` | 分层 api → controller → logic → dao/model（GoFrame 标准） |
| `{{DB_ACCESS}}` | GoFrame ORM + 内置迁移 |

### Go — LangChainGo（LLM）

| 参数 | 值 |
|------|-----|
| `{{LANGUAGE_DESC}}` | Go、LangChainGo、LLM 编排框架 |
| `{{FRAMEWORK_VER}}` | LangChainGo |
| `{{DEV_CMD}}` | `go run ./cmd/` |
| `{{ARCH_LAYER}}` | Chain → Model → Tool/Retriever，链式编排 |
| `{{DB_ACCESS}}` | 向量库 + 会话存储 |

### Go — Google ADK-Go（Agent）

| 参数 | 值 |
|------|-----|
| `{{LANGUAGE_DESC}}` | Go、Google ADK-Go、Agent 开发套件 |
| `{{FRAMEWORK_VER}}` | ADK-Go |
| `{{DEV_CMD}}` | `go run ./cmd/` |
| `{{ARCH_LAYER}}` | Agent → Tool → Model，工具化编排 |
| `{{DB_ACCESS}}` | 向量库 / 会话存储 |

### Go — cloudwego/eino（Agent）

| 参数 | 值 |
|------|-----|
| `{{LANGUAGE_DESC}}` | Go、cloudwego eino、LLM 编排框架 |
| `{{FRAMEWORK_VER}}` | eino |
| `{{DEV_CMD}}` | `go run ./cmd/` |
| `{{ARCH_LAYER}}` | Graph 节点 → 边，Agent 编排 |
| `{{DB_ACCESS}}` | 向量库 / 会话存储 |

### Go — tRPC-Agent-Go（Agent）

| 参数 | 值 |
|------|-----|
| `{{LANGUAGE_DESC}}` | Go、tRPC-Agent-Go、Agent 框架 |
| `{{FRAMEWORK_VER}}` | tRPC-Agent-Go |
| `{{DEV_CMD}}` | `go run ./cmd/` |
| `{{ARCH_LAYER}}` | Agent → Tool → Model，tRPC 服务化 |
| `{{DB_ACCESS}}` | 向量库 / 会话存储 |

### Go — Firebase Genkit

| 参数 | 值 |
|------|-----|
| `{{LANGUAGE_DESC}}` | Go、Firebase Genkit、AI 后端工具链 |
| `{{FRAMEWORK_VER}}` | Genkit Go 版 |
| `{{DEV_CMD}}` | `go run ./cmd/` |
| `{{ARCH_LAYER}}` | Flow → Tool → LLM，Flow 编排 AI 逻辑 |
| `{{DB_ACCESS}}` | 向量库 / 会话存储 |

### Go — Anyi（LLM Agent）

| 参数 | 值 |
|------|-----|
| `{{LANGUAGE_DESC}}` | Go、Anyi、LLM Agent 框架 |
| `{{FRAMEWORK_VER}}` | Anyi |
| `{{DEV_CMD}}` | `go run ./cmd/` |
| `{{ARCH_LAYER}}` | Agent → Tool → LLM，工具化编排 |
| `{{DB_ACCESS}}` | 向量库 / 会话存储 |

### Rust 基础参数（通用）

| 参数 | 值                                      |
|------|----------------------------------------|
| `{{LANGUAGE}}` | `Rust`                                 |
| `{{LANGUAGE_RUNTIME}}` | Rust 1.75+（2021 edition）             |
| `{{COV_TOOL}}` | cargo llvm-cov (核心逻辑 ≥80%)             |
| `{{LINT_TOOL}}` | cargo clippy + rustfmt                 |
| `{{ARCH_TEST_TOOL}}` | cargo clippy 架构检查 + 自定义模块检查          |
| `{{LANG_TAG}}` | `-rust`                                |
| `{{HARNESS_ME_NAME}}` | `harness-me-rust`                     |
| `{{HARNESSING_CMD}}` | `harnessing-rust`                     |
| `{{BUILD_CMD}}` | `cargo build`                          |
| `{{TEST_CMD}}` | `cargo test`                           |
| `{{TEST_FRAMEWORK}}` | cargo test + rstest + mockall        |
| `{{LINT_CMD}}` | `cargo clippy -- -D warnings`          |
| `{{COV_CMD}}` | `cargo llvm-cov --workspace --summary-only` |
| `{{DOCSTYLE}}` | Rustdoc 注释                             |
| `{{FILE_LIMIT}}` | `600`                                  |
| `{{TEST_NAMING}}` | `fn test_x_when_y()`                   |
| `{{DEBUG_TOOL}}` | `rust-gdb` / `lldb`                    |
| `{{ARCH_REVIEW_CMD}}` | `arch-review-rust`                     |
| `{{ARCH_TEST_CMD}}` | 架构约束测试命令                               |
| `{{SECURITY_CMD}}` | 安全扫描命令：`cargo audit`                    |
| `{{INTEGRATION_CMD}}` | 集成测试命令：`cargo test --test`（tests/ 下集成） |
| `{{RUN_CMD}}` | 启动服务命令：`cargo run`                   |
| `{{HEALTH_CHECK_CMD}}` | 健康检查命令                               |
| `{{HEALTH_ENDPOINT}}` | 健康检查端点                               |
| `{{METRICS_ENDPOINT}}` | 指标端点                                 |
| `{{VET_CMD}}` | 静态分析命令：`cargo clippy`               |
| `{{DEP_CMD}}` | 依赖管理命令：`cargo add` / `cargo update` |
| `{{ORM_TOOL}}` | ORM 框架：SQLx / Diesel / SeaORM         |
| `{{TYPE_CHECK_TOOL}}` | 类型检查工具：cargo check（编译期强类型）        |
| `{{TYPE_CHECK_CMD}}` | 类型检查命令：`cargo check`                |
| `{{ASSERT_LIB}}` | 断言库：内置 `assert!` / rstest / proptest |
| `{{MOCK_LIB}}` | `mockall` / `wiremock`                 |
| `{{RACE_DETECT_ARG}}` | 竞态检测参数：`cargo test`（所有权/借用保证线程安全） |
| `{{HTTP_MOCK_UTIL}}` | HTTP Mock 工具：`httpmock` / `wiremock` |
| `{{FRAMEWORK_NAME}}` | 框架名称（从 {{LANGUAGE_DESC}} 提取）        |
| `{{ARCH_LAYER}}` | Router → Handler → Service，依赖单向 |

### Rust — Axum（Web 服务）

| 参数 | 值 |
|------|-----|
| `{{LANGUAGE_DESC}}` | Rust、Axum、异步 Web 框架（tokio 生态） |
| `{{FRAMEWORK_VER}}` | Axum 0.8+ |
| `{{DEV_CMD}}` | `cargo run` |
| `{{ARCH_LAYER}}` | Router → Handler → Service，依赖单向 |
| `{{DB_ACCESS}}` | SQLx / SeaORM + sqlx-cli 迁移 |

### Rust — Actix Web

| 参数 | 值 |
|------|-----|
| `{{LANGUAGE_DESC}}` | Rust、Actix Web、高性能 Web 框架 |
| `{{FRAMEWORK_VER}}` | Actix Web 4.x |
| `{{DEV_CMD}}` | `cargo run` |
| `{{ARCH_LAYER}}` | 路由 → Handler（Actor 可选）→ Service |
| `{{DB_ACCESS}}` | SQLx / Diesel |

### Rust — Rocket

| 参数 | 值 |
|------|-----|
| `{{LANGUAGE_DESC}}` | Rust、Rocket、类型安全 Web 框架 |
| `{{FRAMEWORK_VER}}` | Rocket 0.5.x |
| `{{DEV_CMD}}` | `cargo run` |
| `{{ARCH_LAYER}}` | 路由 → Controller → Service |
| `{{DB_ACCESS}}` | Diesel / SQLx（rocket_db_pools） |

### Rust — Warp

| 参数 | 值 |
|------|-----|
| `{{LANGUAGE_DESC}}` | Rust、Warp、Filter 组合式 Web 框架 |
| `{{FRAMEWORK_VER}}` | Warp 0.3.x |
| `{{DEV_CMD}}` | `cargo run` |
| `{{ARCH_LAYER}}` | Filter 组合 → Handler → Service |
| `{{DB_ACCESS}}` | SQLx / Diesel |

### Rust — Poem

| 参数 | 值 |
|------|-----|
| `{{LANGUAGE_DESC}}` | Rust、Poem、异步 Web 框架 |
| `{{FRAMEWORK_VER}}` | Poem 3.x |
| `{{DEV_CMD}}` | `cargo run` |
| `{{ARCH_LAYER}}` | Endpoint → Handler → Service |
| `{{DB_ACCESS}}` | SeaORM / SQLx |

### Rust — Loco（全栈 MVC）

| 参数 | 值 |
|------|-----|
| `{{LANGUAGE_DESC}}` | Rust、Loco、Rails 风格全栈 MVC 框架 |
| `{{FRAMEWORK_VER}}` | Loco（loco-rs） |
| `{{DEV_CMD}}` | `cargo loco start` |
| `{{ARCH_LAYER}}` | 分层 controller → service → model（Loco 标准） |
| `{{DB_ACCESS}}` | SeaORM（内置）+ 迁移 |

### Rust — Salvo

| 参数 | 值 |
|------|-----|
| `{{LANGUAGE_DESC}}` | Rust、Salvo、Web 框架 |
| `{{FRAMEWORK_VER}}` | Salvo 0.7x |
| `{{DEV_CMD}}` | `cargo run` |
| `{{ARCH_LAYER}}` | Handler → Service |
| `{{DB_ACCESS}}` | SeaORM / SQLx |

### Rust — Candle（深度学习）

| 参数 | 值 |
|------|-----|
| `{{LANGUAGE_DESC}}` | Rust、Candle、轻量深度学习框架（huggingface） |
| `{{FRAMEWORK_VER}}` | Candle |
| `{{DEV_CMD}}` | `cargo run --release` |
| `{{ARCH_LAYER}}` | 模型定义 → 张量运算 → 训练/推理 |
| `{{DB_ACCESS}}` | 权重/数据集文件 + 向量存储 |

### Rust — Burn（深度学习）

| 参数 | 值 |
|------|-----|
| `{{LANGUAGE_DESC}}` | Rust、Burn、深度学习框架 |
| `{{FRAMEWORK_VER}}` | Burn 0.15 |
| `{{DEV_CMD}}` | `cargo run --release` |
| `{{ARCH_LAYER}}` | Model Module → Backend → 训练循环 |
| `{{DB_ACCESS}}` | 权重/数据集文件 + 向量存储 |

### Rust — tch-rs（深度学习）

| 参数 | 值 |
|------|-----|
| `{{LANGUAGE_DESC}}` | Rust、tch-rs、libtorch 绑定深度学习 |
| `{{FRAMEWORK_VER}}` | tch-rs（libtorch） |
| `{{DEV_CMD}}` | `cargo run --release` |
| `{{ARCH_LAYER}}` | 张量 → 模型 → 训练/推理 |
| `{{DB_ACCESS}}` | 权重文件（safetensors / bin） |

### Rust — ort（ONNX 推理）

| 参数 | 值 |
|------|-----|
| `{{LANGUAGE_DESC}}` | Rust、ort、ONNX Runtime 推理 |
| `{{FRAMEWORK_VER}}` | ort 2.x |
| `{{DEV_CMD}}` | `cargo run --release` |
| `{{ARCH_LAYER}}` | ONNX 图 → Session 推理 |
| `{{DB_ACCESS}}` | 权重文件 + 数据管道 |

### Rust — rlx-models（LLM）

| 参数 | 值 |
|------|-----|
| `{{LANGUAGE_DESC}}` | Rust、rlx-models、本地 LLM 推理库（llama.cpp 后端） |
| `{{FRAMEWORK_VER}}` | rlx-models 0.3+ |
| `{{DEV_CMD}}` | `cargo run --release` |
| `{{ARCH_LAYER}}` | 模型仓库 → 量化 → 推理 |
| `{{DB_ACCESS}}` | 权重文件 + KV cache |

### Rust — ADK-Rust（Agent）

| 参数 | 值 |
|------|-----|
| `{{LANGUAGE_DESC}}` | Rust、ADK-Rust、Agent 开发套件 |
| `{{FRAMEWORK_VER}}` | ADK-Rust |
| `{{DEV_CMD}}` | `cargo run` |
| `{{ARCH_LAYER}}` | Agent → Tool → Model，工具化编排 |
| `{{DB_ACCESS}}` | 向量库 / 会话存储 |

### Rust — Blockcell（Agent）

| 参数 | 值 |
|------|-----|
| `{{LANGUAGE_DESC}}` | Rust、Blockcell、Agent 编排框架 |
| `{{FRAMEWORK_VER}}` | Blockcell |
| `{{DEV_CMD}}` | `cargo run` |
| `{{ARCH_LAYER}}` | Agent → Block/Cell 图 → Tool/Model 调用 |
| `{{DB_ACCESS}}` | 向量库 / 会话存储 |

### Rust — vLLM（LLM 推理服务）

| 参数 | 值 |
|------|-----|
| `{{LANGUAGE_DESC}}` | Rust、vLLM、LLM 推理服务 |
| `{{FRAMEWORK_VER}}` | vLLM（Rust 绑定） |
| `{{DEV_CMD}}` | `vllm serve`（Python/容器启动） |
| `{{ARCH_LAYER}}` | Engine 推理服务 → 量化/调度/批处理 |
| `{{DB_ACCESS}}` | KV cache + 权重存储 |

### Rust — Tauri（桌面）

| 参数 | 值 |
|------|-----|
| `{{LANGUAGE_DESC}}` | Rust、Tauri、跨平台桌面应用 |
| `{{FRAMEWORK_VER}}` | Tauri 2.x |
| `{{DEV_CMD}}` | `cargo tauri dev` |
| `{{ARCH_LAYER}}` | Rust Core（Commands）→ 前端 WebView 桥接 |
| `{{DB_ACCESS}}` | SQLx / rusqlite（本地） |

### Rust — Iced（GUI）

| 参数 | 值 |
|------|-----|
| `{{LANGUAGE_DESC}}` | Rust、Iced、Elm 架构 GUI |
| `{{FRAMEWORK_VER}}` | Iced 0.13 |
| `{{DEV_CMD}}` | `cargo run` |
| `{{ARCH_LAYER}}` | Model → Update → View（Elm 架构） |
| `{{DB_ACCESS}}` | 本地文件 / rusqlite |

### Rust — egui（即时 GUI）

| 参数 | 值 |
|------|-----|
| `{{LANGUAGE_DESC}}` | Rust、egui、即时模式 GUI |
| `{{FRAMEWORK_VER}}` | egui 0.29+ |
| `{{DEV_CMD}}` | `cargo run` |
| `{{ARCH_LAYER}}` | App → update() 绘制（即时模式） |
| `{{DB_ACCESS}}` | 本地文件 / rusqlite |

### Rust — Dioxus（跨平台 UI）

| 参数 | 值 |
|------|-----|
| `{{LANGUAGE_DESC}}` | Rust、Dioxus、跨平台 UI 框架（React 风格） |
| `{{FRAMEWORK_VER}}` | Dioxus 0.6 |
| `{{DEV_CMD}}` | `dx serve` / `cargo run` |
| `{{ARCH_LAYER}}` | 组件树 → 状态（hooks）→ 事件 |
| `{{DB_ACCESS}}` | 本地存储（rusqlite / localStorage） |

---

## 约束

- ❌ 禁止修改已存在的 `.harness/` 内容（除非用户明确要求覆盖）
- ❌ 禁止在检测到多语言时擅自选择
- ❌ 禁止跳过 Step 1 直接使用默认语言
- ❌ 禁止把 `HARNESSING_CMD` / `HARNESS_ME_NAME` / `ARCH_REVIEW_CMD` 写成带前导 `/` 的值（YAML `name:` 必须是字符串；工具会再加 `/`，写成 `/harnessing` 会变成 `//harnessing`）
- ❌ 禁止未加引号地把 `{{PLACEHOLDER}}` 写进 `name:`（`{{` 会被 YAML 解析成 map）
- ✅ 渲染 `name` 时保持 `"{{HARNESSING_CMD}}"` 这种引号形式，替换后仍是带引号的字符串
- ✅ 如果 `.harness/` 已存在，输出提示并询问是否覆盖
- ✅ 每个步骤完成后输出简要状态
---


