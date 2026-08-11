# Changelog

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