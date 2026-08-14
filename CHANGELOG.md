# Changelog

## Unreleased

- **重构：语言包技能单一事实源（方案 A）**:
  - 6 个语言包（java/python/golang/rust/php/front）内的 10 个流水线/辅助技能全部收敛到 `harness-core/skills/` 参数化模板，语言包不再维护同名副本，消除 60 份重复文件与内容漂移
  - `harness-core/skills/` 新增 5 个参数化模板：`arch-review`、`deploy-verify`、`expert-reviewer`、`harnessing`、`unit-test-ci`
  - `apply-harness` Step 5 改为「渲染 core 模板 + 复制语言包专属技能」：10 个技能全部由 `harness-core/skills/` 渲染，语言差异通过参数表占位符表达；java 语言包仅保留 4 个框架专属技能（`java-code-review`、`spring-api-convention`、`mybatis-toolkit`、`openfeign-toolkit`）
  - 参数表新增「Frontend 基础参数（通用）」块，并补齐 Java/Python/Go/Rust/PHP 基础参数块缺失键（ARCH_LAYER、TEST_FRAMEWORK、MOCK_LIB 等），6 语言 × 111 个框架块对 29 个模板占位符全覆盖、零缺失
  - 语言特有内容（部署命令、架构审查关注点、自检项等）下沉到各语言包 `rules/`

## 1.3.0 (2025-08-14)

- **新增 Rust 语言支持**:
  - 新增 `skills/harness-rust/` 语言包：基础参数 + 10 个技能 + rules
  - Web 框架: Axum、Actix Web、Rocket、Warp、Poem、Loco、Salvo
  - AI/深度学习: Candle、Burn、tch-rs、ort、rlx-models、ADK-Rust、Blockcell、vLLM
  - 桌面/UI: Tauri、Iced、egui、Dioxus
- **新增 PHP 语言支持**:
  - 新增 `skills/harness-php/` 语言包：基础参数 + 10 个技能 + rules
  - Web/网络框架: Laravel、ThinkPHP、Hyperf、Yii / Yii 3、Workerman、webman、CodeIgniter (CI) / Slim、Phalcon、CakePHP、Symfony2、Yaf、Swoole
  - CMS/电商: WordPress、WooCommerce、Drupal、Joomla、phpcms、dedecms、discuz、Craft CMS、October CMS、OpenCart、GravCMS
  - AI/LLM: Laravel AI SDK、Neuron AI、LLPhant、Prism、PocketFlow PHP、Cognesy Instructor PHP、Papiai
- 参数表新增 Rust/PHP 基础参数 + 各框架差异块
- 检测表新增 Rust（Cargo.toml / 19 框架）与 PHP（composer.json / 29+ 框架）识别规则
- 文档与元数据同步更新：README、CONTEXT、docs/languages/{rust,php}.md、plugin.json、marketplace.json、package.json

## 1.2.0 (2025-08-11)

- **新增 30+ 框架/构建工具识别**:
  - Java: Dubbo、Spring Cloud Alibaba、Spring AI、Spring AI Alibaba、AgentScope Java、LangChain4j、Semantic Kernel、Genkit Java
  - Python: Tornado、TensorFlow、PyTorch、Keras、scikit-learn、XGBoost、Hugging Face Transformers、LangChain、LangGraph、CrewAI、PydanticAI、SmolAgents、OpenAI Agents SDK
  - Go: Beego、Go-Kit、Go-Kratos、Gorilla Mux、Kitex、Hertz、Iris、Macaron、Tango、GoFrame、LangChainGo、Google ADK-Go、cloudwego/eino、tRPC-Agent-Go、Firebase Genkit、Anyi
- 参数表新增差异化参数块（基础参数 + 差异块），50+ 框架参数块在手
- 语言包内部技能全部参数化（8 个核心技能 × 4 语言），使用 {{PLACEHOLDER}} 动态渲染
- 语言文档全面更新，覆盖所有框架
- 检测表新增 50+ 识别规则

## 1.1.0 (2025-08-11)

- 大幅扩展框架/构建工具识别支持
- **Java**: 新增 Spring MVC、Quarkus、Micronaut、Vert.x、Dropwizard 框架 + Gradle 构建工具
- **Python**: 新增 Django 全栈框架 + uv 构建工具
- **Go**: 新增 Echo、Fiber、Chi 框架
- **Frontend**: 新增 React、Angular、Svelte、Next.js、Nuxt 框架 + Webpack/Angular CLI 构建工具
- 参数表重构为按框架独立参数块，自动选择对应参数
- 检测表新增 40+ 识别规则，覆盖主流框架和构建工具
- 语言文档全面更新，列出各语言全部支持框架
- coding-skill 角色描述参数化，支持按检测到的框架动态渲染

## 1.0.0 (2025-06-01)

- 初始发布
- 支持 Java / Python / Go / Frontend 四种语言
- 核心 Harness Engineering 方法论：SDD-TDD、6 阶段流水线
- 包含 5 条规则（编码规范、工程结构、开发流程、运行时可靠性、SDD-TDD 模式）
- 包含 9 个技能（6 流水线 + 3 辅助）
- 变更状态机 + 模板系统
- 共享语言机制（CONTEXT.md）