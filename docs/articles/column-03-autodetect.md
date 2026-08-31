# 自动检测：让 AI 一眼认出你的技术栈

> 核心思想系列 · 第三篇 · 约 8000 字 · 6 个 SVG 图

---

## 一、从"手工配置"到"自动识别"：一场效率革命

### 1.1 huazai-trip-plan 的"手动告白"困境

在参数化系统（第二篇）解决了"命令在哪"的问题之后，一个更棘手的问题浮现了：**AI 怎么知道这个项目用的是什么技术栈？**

在 huazai-trip-plan 中，答案很简单——因为只有一个项目，只需要一段固定的描述："你是一个 Java 后端工程师，使用 Spring Boot 3.x，Maven 构建，JUnit 5 测试..."

但 Harness 要服务任意语言、任意框架的项目。当用户在一个新项目目录中安装 Harness 时，AI 必须"自己"弄清楚：**这是 Java 还是 Python？是 Spring Boot 还是 FastAPI？用 Maven 还是 pip？**

如果让用户手动回答，就回到了"硬编码"的困境——每个项目都要重新配置，而且容易出错。如果让 AI 盲目猜测，就会"自作主张"地选错技术栈。

自动检测系统解决的，正是这个问题：**让 AI 通过分析项目文件，自动识别技术栈，无需用户手动配置。**

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 260" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_a1" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_a1"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="250" fill="url(#bg_a1)" rx="10"/>
  <text x="400" y="25" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">从"手动告白"到"自动识别"</text>
  <rect x="30" y="50" width="360" height="80" rx="10" fill="#ef4444" opacity="0.12" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_a1)"/>
  <text x="210" y="72" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">手动配置时代</text>
  <text x="210" y="92" text-anchor="middle" fill="#94a3b8" font-size="10">用户手动告诉 AI 技术栈</text>
  <text x="210" y="110" text-anchor="middle" fill="#64748b" font-size="9">每个项目都要重新配置，易出错</text>
  <rect x="410" y="50" width="360" height="80" rx="10" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_a1)"/>
  <text x="590" y="72" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">自动识别时代</text>
  <text x="590" y="92" text-anchor="middle" fill="#94a3b8" font-size="10">AI 通过分析项目文件自动识别</text>
  <text x="590" y="110" text-anchor="middle" fill="#64748b" font-size="9">零配置，开箱即用，不易出错</text>
  <line x1="390" y1="90" x2="410" y2="90" stroke="#475569" stroke-width="2" stroke-dasharray="5,3"/>
  <text x="400" y="82" text-anchor="middle" fill="#f59e0b" font-size="22" font-weight="700">→</text>
  <text x="400" y="175" text-anchor="middle" fill="#475569" font-size="11">自动检测的核心：让 AI 通过分析项目文件，自动识别技术栈，无需用户手动配置</text>
  <text x="400" y="195" text-anchor="middle" fill="#94a3b8" font-size="10">这是"参数化"和"框架支持"之间的桥梁——检测到框架，才能加载对应的参数块</text>
  <text x="400" y="220" text-anchor="middle" fill="#64748b" font-size="10">检测是安装的第一步，也是渲染技能文件的前提</text>
</svg>
```

### 1.2 检测的对象：两类核心"信号源"

AI 如何"认出"一个项目？答案是：**读取项目中的关键文件（信号源）。** 每个项目都留下了"指纹"，通过这些指纹可以推断技术栈。

自动检测系统主要分析以下两类核心信号源：

| 信号源 | 示例 | 揭示的信息 |
|--------|------|-----------|
| 构建文件 | pom.xml、build.gradle、go.mod、requirements.txt、Cargo.toml、composer.json | 语言、构建工具、框架依赖 |
| 项目描述 | package.json、pyproject.toml | 项目名、框架、脚本命令 |

这两类信号源覆盖了 6 种语言（Java、Python、Go、Rust、PHP、Frontend）的主流框架检测需求。此外，在极少数无标准构建文件的场景下，AI 代理也可以参考源码特征（如 `manage.py` 暗示 Django）或目录结构来辅助判断，但这不是检测系统的主要路径。**检测的核心是"信号源"的确定性**——构建文件和项目描述文件是标准化的、可预测的，因此是检测的基石。

### 1.3 检测的"优先级"：从强信号到弱信号

并不是所有信号源同等可靠。`pom.xml` 明确告诉你"这是 Maven 项目"，而 README 中的一句"我们用了 Spring"可能不可靠。

因此，自动检测遵循**优先级顺序**：先检查构建文件（高可信），再检查项目描述文件（中可信），最后在无标准文件时参考源码特征。这个优先级确保了检测的**确定性**——不会因为一行注释就误判技术栈。

> 实际检测规则以扁平列表的形式定义在 `apply-harness` 技能中，按语言分组（Java → Python → Go → Frontend → Rust → PHP），每组内按常见框架优先排列。AI 代理按序检查每个规则，命中即判定。这并不是一个严格的三级优先级系统，而是一个"按可信度排列"的规则列表。

## 二、检测规则库：60+ 框架的"识别指纹"

### 2.1 检测规则的四要素

每个框架的检测规则由四个要素组成：

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 240" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_a2" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_a2"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="240" fill="url(#bg_a2)" rx="10"/>
  <text x="400" y="25" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">一个检测规则的四个要素</text>
  <rect x="30" y="50" width="170" height="70" rx="10" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_a2)"/>
  <text x="115" y="72" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">① 文件匹配</text>
  <text x="115" y="92" text-anchor="middle" fill="#94a3b8" font-size="10">匹配哪个文件</text>
  <text x="115" y="108" text-anchor="middle" fill="#64748b" font-size="9">如 pom.xml</text>
  <rect x="215" y="50" width="170" height="70" rx="10" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_a2)"/>
  <text x="300" y="72" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">② 内容匹配</text>
  <text x="300" y="92" text-anchor="middle" fill="#94a3b8" font-size="10">匹配文件内容</text>
  <text x="300" y="108" text-anchor="middle" fill="#64748b" font-size="9">如 spring-boot-starter</text>
  <rect x="400" y="50" width="170" height="70" rx="10" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_a2)"/>
  <text x="485" y="72" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">③ 框架判定</text>
  <text x="485" y="92" text-anchor="middle" fill="#94a3b8" font-size="10">命中 → 判定框架</text>
  <text x="485" y="108" text-anchor="middle" fill="#64748b" font-size="9">如 Spring Boot</text>
  <rect x="585" y="50" width="185" height="70" rx="10" fill="#a855f7" opacity="0.12" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_a2)"/>
  <text x="677" y="72" text-anchor="middle" fill="#d8b4fe" font-size="12" font-weight="700">④ 参数关联</text>
  <text x="677" y="92" text-anchor="middle" fill="#94a3b8" font-size="10">关联参数块</text>
  <text x="677" y="108" text-anchor="middle" fill="#64748b" font-size="9">加载 {{TEST_CMD}} 等</text>
  <text x="400" y="165" text-anchor="middle" fill="#475569" font-size="11">一条完整的检测规则 = 文件匹配 + 内容匹配 → 框架判定 → 参数关联</text>
  <text x="400" y="190" text-anchor="middle" fill="#94a3b8" font-size="10">检测规则库是一个映射表：项目文件模式 → 框架 → 参数块</text>
  <text x="400" y="210" text-anchor="middle" fill="#64748b" font-size="9">60+ 框架对应 60+ 条规则，每条规则独立维护、可增删</text>
</svg>
```

以 Java + Spring Boot 为例，检测规则是：
- 文件匹配：pom.xml 或 build.gradle
- 内容匹配：包含 "spring-boot-starter" 依赖
- 框架判定：Spring Boot
- 参数关联：加载 Spring Boot 差异参数块### 2.2 多语言检测的"分诊"逻辑

当检测到多种语言的痕迹时（比如 `pom.xml` 和 `requirements.txt` 同时存在），系统需要"分诊"——判断哪个语言是主语言。

分诊逻辑遵循以下优先级：
1. **构建文件唯一性**：一个项目通常只有一个主导构建文件
2. **目录结构**：src/main/java 表明 Java，src/ 下的 .py 文件表明 Python
3. **依赖关系**：如果 requirements.txt 引用了 pom.xml 无法提供的库，可能是多语言项目

**关键原则**：当检测到多语言时，系统**绝不自动替用户选择**——而是输出检测到的语言列表，让用户确认。这遵循了"不替用户做决定"的 Harness 原则。

### 2.3 检测的"误判"防护

自动检测最怕的是"误判"——把 A 框架误认为 B 框架。防护策略包括：
- **构建文件优先**：只有标准的构建文件才作为首要判定依据，避免误判
- **多信号交叉验证**：如果 pom.xml 和 package.json 都指向 Java，则高度可信
- **冲突时询问**：当不同信号指向不同框架时，不武断选择，而是询问用户

这些策略让自动检测在"快"和"准"之间取得平衡。

## 三、检测流程：从"识别"到"落地"

### 3.1 检测的完整流程

自动检测是 Harness 安装流程的**第一步**（Step 1），它的完整流程如下：

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 420" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_a3" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_a3"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="420" fill="url(#bg_a3)" rx="10"/>
  <text x="400" y="25" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">自动检测的完整流程（Step 1）</text>
  <rect x="290" y="40" width="220" height="40" rx="8" fill="#3b82f6" opacity="0.15" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_a3)"/>
  <text x="400" y="64" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">扫描项目信号源</text>
  <line x1="400" y1="80" x2="400" y2="100" stroke="#475569" stroke-width="2"/>
  <polygon points="400,105 395,95 405,95" fill="#94a3b8"/>
  <rect x="290" y="110" width="220" height="40" rx="8" fill="#06b6d4" opacity="0.15" stroke="#06b6d4" stroke-width="1.5" filter="url(#sh_a3)"/>
  <text x="400" y="134" text-anchor="middle" fill="#67e8f9" font-size="12" font-weight="700">匹配检测规则库</text>
  <line x1="400" y1="150" x2="400" y2="170" stroke="#475569" stroke-width="2"/>
  <polygon points="400,175 395,165 405,165" fill="#94a3b8"/>
  <rect x="290" y="180" width="220" height="40" rx="8" fill="#f59e0b" opacity="0.15" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_a3)"/>
  <text x="400" y="204" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">判定语言 + 框架</text>
  <line x1="400" y1="220" x2="400" y2="240" stroke="#475569" stroke-width="2"/>
  <rect x="200" y="250" width="140" height="40" rx="8" fill="#ef4444" opacity="0.12" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_a3)"/>
  <text x="270" y="274" text-anchor="middle" fill="#fca5a5" font-size="11" font-weight="700">多语言？</text>
  <line x1="365" y1="240" x2="400" y2="240" stroke="#475569" stroke-width="2"/>
  <line x1="400" y1="240" x2="400" y2="250" stroke="#475569" stroke-width="2"/>
  <line x1="270" y1="250" x2="270" y2="260" stroke="#475569" stroke-width="2"/>
  <rect x="460" y="250" width="140" height="40" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_a3)"/>
  <text x="530" y="274" text-anchor="middle" fill="#86efac" font-size="11" font-weight="700">单一语言</text>
  <line x1="270" y1="290" x2="270" y2="310" stroke="#475569" stroke-width="2"/>
  <polygon points="270,315 265,305 275,305" fill="#94a3b8"/>
  <rect x="170" y="320" width="200" height="40" rx="8" fill="#ef4444" opacity="0.12" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_a3)"/>
  <text x="270" y="344" text-anchor="middle" fill="#fca5a5" font-size="11" font-weight="700">询问用户确认</text>
  <line x1="530" y1="290" x2="530" y2="310" stroke="#475569" stroke-width="2"/>
  <polygon points="530,315 525,305 535,305" fill="#94a3b8"/>
  <rect x="430" y="320" width="200" height="40" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_a3)"/>
  <text x="530" y="344" text-anchor="middle" fill="#86efac" font-size="11" font-weight="700">加载参数块</text>
  <line x1="530" y1="360" x2="530" y2="380" stroke="#475569" stroke-width="2"/>
  <polygon points="530,385 525,375 535,375" fill="#94a3b8"/>
  <rect x="430" y="385" width="200" height="30" rx="8" fill="#3b82f6" opacity="0.15" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_a3)"/>
  <text x="530" y="404" text-anchor="middle" fill="#93c5fd" font-size="11" font-weight="700">渲染技能文件</text>
</svg>
```

流程的关键分支是"多语言还是单一语言"：
- **单一语言**：直接加载该语言的参数块，渲染技能文件
- **多语言**：**绝不自动选择**，而是列出检测到的语言，询问用户确认

### 3.2 检测的输出：一份"技术栈报告"

检测完成后，AI 会输出一份**技术栈报告**，类似：
```
📋 检测到技术栈：
- 语言：Java
- 构建工具：Maven
- 框架：Spring Boot 3.x
- 测试框架：JUnit 5 + Mockito
- 覆盖率：JaCoCo
- 代码规范：Checkstyle
- 架构：Controller - Service - Repository
```
这份报告不仅让用户确认检测结果，也作为后续所有技能文件的"上下文"。

### 3.3 检测的"可覆盖性"：手动修正

自动检测不是"不可修正"的。如果检测结果有误（比如项目使用了非常规的目录结构），用户可以**手动覆盖**检测结果。覆盖方式有两种：
- **在检测时指定**：用户直接在检测过程中告知 AI 正确的语言和框架，AI 据此加载对应的参数块
- **在检测时修正**：用户看到技术栈报告后指出错误，AI 重新加载正确的参数块

这种"自动 + 手动兜底"的设计，让自动检测系统既高效又可靠。

## 四、检测规则库的"扩展"：框架识别指纹详解

### 4.1 Java 框架的识别指纹

Java 生态的框架识别主要依赖 `pom.xml` 和 `build.gradle` 中的依赖：

| 框架 | 识别指纹（依赖关键字） |
|------|----------------------|
| Spring Boot | spring-boot-starter |
| Dubbo | dubbo |
| Spring Cloud Alibaba | spring-cloud-alibaba |
| Spring AI | spring-ai |
| Spring AI Alibaba | spring-ai-alibaba-starter |
| AgentScope Java | agentscope-java |
| LangChain4j | langchain4j |
| Semantic Kernel | semantic-kernel |
| Genkit Java | genkit |
| Quarkus | quarkus-core |
| Micronaut | micronaut-core |
| Vert.x | vertx-core |

其中 Spring AI 与 Spring AI Alibaba 的区分需要更精细的匹配——Spring AI Alibaba 的依赖中包含 `spring-ai-alibaba-starter`，而 Spring AI 没有。这种"父子依赖"的区分考验了检测规则的精确性。

### 4.2 Python 框架的识别指纹

Python 框架的识别主要依赖 `requirements.txt`、`pyproject.toml` 和 `Pipfile`：

| 框架 | 识别指纹（依赖关键字） |
|------|----------------------|
| FastAPI | fastapi |
| Flask | flask |
| Django | django |
| TensorFlow | tensorflow |
| PyTorch | torch |
| LangChain | langchain |
| LangGraph | langgraph |
| CrewAI | crewai |
| PydanticAI | pydantic-ai |
| Hugging Face | transformers |
| Tornado | tornado |

有趣的是，Python 的 AI 框架识别还需要考虑依赖的组合。例如，`langgraph` 往往与 `langchain` 一起出现，而 `crewai` 也常与 `langchain` 搭配。系统需要能区分"主导框架"和"辅助依赖"。

### 4.3 Go 框架的识别指纹

Go 框架的识别主要依赖 `go.mod` 中的依赖路径：

| 框架 | 识别指纹（依赖模块） |
|------|---------------------|
| Gin | gin-gonic/gin |
| go-zero | zeromicro/go-zero |
| Echo | labstack/echo |
| Fiber | gofiber/fiber |
| Chi | go-chi/chi |
| Kitex | cloudwego/kitex |
| Hertz | cloudwego/hertz |
| LangChainGo | tmc/langchaingo |
| GoFrame | gogf/gf |
| Beego | beego |
| Go-Kit | go-kit/kit |
| Gorilla Mux | gorilla/mux |

Go 的识别有个特点：依赖路径本身就包含了完整的模块名，因此识别非常精确，几乎不会误判。

### 4.4 Frontend 框架的识别指纹

Frontend 框架的识别主要依赖 `package.json` 中的 dependencies：

| 框架 | 识别指纹（依赖关键字） |
|------|----------------------|
| Vue 3 | vue |
| React | react |
| Angular | @angular/core |
| Svelte | svelte |
| Next.js | next |
| Nuxt | nuxt |
| Vite | vite |

Frontend 的识别有个独特挑战：**一个项目可能同时使用 Vue + Vite + TypeScript**。系统需要识别"主框架"（Vue）+ "构建工具"（Vite）+ "语言变体"（TS），生成一个复合的技术栈描述。

### 4.5 Rust 框架的识别指纹

Rust 框架的识别主要依赖 `Cargo.toml` 中的依赖路径：

| 框架 | 识别指纹（依赖模块） |
|------|---------------------|
| Axum | axum |
| Actix Web | actix-web |
| Rocket | rocket |
| Warp | warp |
| Poem | poem |
| Loco | loco-rs |
| Salvo | salvo |
| Candle | candle-core |
| Burn | burn |
| tch-rs | tch |
| ort | ort |
| Tauri | tauri |
| Iced | iced |
| egui | egui / eframe |
| Dioxus | dioxus |

Rust 的识别有个特点：`Cargo.toml` 是唯一的构建文件，依赖路径直接对应框架名，因此识别非常精确。由于 Rust 的异步运行时（tokio / async-std）和构建工具（cargo）相对统一，检测的重点在于框架而非工具链。

### 4.6 PHP 框架的识别指纹

PHP 框架的识别主要依赖 `composer.json` 中的依赖：

| 框架 | 识别指纹（依赖关键字） |
|------|----------------------|
| Laravel | laravel/framework |
| Laravel AI SDK | laravel-ai / laravel-ai/sdk |
| ThinkPHP | topthink/framework |
| Hyperf | hyperf/framework |
| Yii 2 | yiisoft/yii2 |
| Yii 3 | yiisoft/yii |
| Symfony 2 | symfony/symfony |
| CodeIgniter 4 | codeigniter4/framework |
| Slim | slim/slim |
| CakePHP | cakephp/cakephp |
| Phalcon | phalcon/cphalcon |
| Drupal | drupal/core |
| Craft CMS | craftcms/cms |
| October CMS | october/october |
| OpenCart | opencart/opencart |
| GravCMS | getgrav/grav |
| Workerman | workerman/workerman |
| webman | workerman/webman-framework |
| Swoole | swoole / ext-swoole |
| Yaf | yaf（php 扩展） |

PHP 的识别有个独特挑战：**框架数量多且命名风格不统一**。有的用 `vendor/package` 格式（`laravel/framework`），有的直接用单一名（`slim/slim`），还有的依赖 PHP 扩展（`phalcon`、`yaf`）。系统需要同时处理 Composer 依赖和 PHP 扩展两种信号源。

### 4.7 识别指纹的"组合与冲突"

当一个项目同时命中多个框架规则时（比如 Java + Spring Boot + Dubbo），系统需要处理组合与冲突：
- **组合**：Spring Boot + Dubbo 是合法组合（一个 Web 框架 + 一个 RPC 框架），系统加载两个框架的参数块，按优先级合并
- **冲突**：Spring Boot vs Quarkus 都命中了（不太可能），系统会交叉验证信号源，选择更可信的那个

## 五、自动检测的"设计哲学"与"协同"

### 5.1 为什么"不自动选择"？

Harness 最特殊的设计哲学之一，是**多语言检测时绝不自动选择**。这看似"不便"，实则是深思熟虑的决定：
1. **尊重用户决定**：技术栈是项目的核心决策，不应由 AI 代劳
2. **避免"想当然"**：AI 猜测的语言可能不是项目实际使用的语言
3. **减少误配置**：选错语言会加载错误的参数块，后果严重

这个原则体现了 Harness 的定位：**AI 是脚手架，不是决策者。**

### 5.2 检测的"确定性"与"智能性"

自动检测在"确定性"和"智能性"之间寻求平衡：
- **确定性**：检测规则是明确、可预测的（pom.xml 命中 → Java），不会因为 AI 的"自由发挥"而误判
- **智能性**：系统能处理复杂场景（多技术栈、父子依赖、框架组合检测）

这两个特性看似矛盾，实则是分层实现的：**规则的匹配是确定性的，规则的组合与优先级判断是智能的。**

### 5.3 检测 → 参数化 → 渲染的完整链路

自动检测不是孤立的，它是整个 Harness 系统的**入口**。完整的链路是：检测 → 加载参数块 → 渲染技能文件 → 工程就绪。

检测是"入口"，参数化是"核心"，渲染是"落地"。三个环节缺一不可：检测错了，参数就错；参数错了，技能就错。每一步都输出"状态报告"，让用户随时知道"现在发生了什么"。

## 七、案例：从"空目录"到"技术栈就绪"的完整过程

### 7.1 场景一：Spring Boot 项目

假设用户在 `/my-app` 目录下有一个 Spring Boot 项目，第一次安装 Harness。自动检测的完整过程如下：

**第 1 步：扫描信号源**
AI 发现以下文件：`pom.xml`（构建文件）、`src/main/java/**/*.java`（源码目录）、`.gitignore`（版本控制）。

**第 2 步：匹配规则库**
读取 `pom.xml`，发现 `spring-boot-starter-web` 依赖，匹配 Spring Boot 框架规则。

**第 3 步：判定结果**
- 语言：Java
- 构建工具：Maven（pom.xml 存在）
- 框架：Spring Boot
- 测试框架：JUnit 5（从依赖判断）

**第 4 步：加载参数块**
加载 Java 基础参数 + Spring Boot 差异参数块，得到完整的参数表。

**第 5 步：渲染技能文件**
将所有 32 个技能文件中的 `{{PLACEHOLDER}}` 替换为参数表中的实际值。

**第 6 步：输出技术栈报告**
确认无误后，Harness 就绪。

### 7.2 场景二：多语言项目

假设项目同时包含 `pom.xml` 和 `requirements.txt`（一个 Java 服务 + 一个 Python 训练脚本）。

自动检测会：
1. 检测到 Java（pom.xml）和 Python（requirements.txt）
2. **不自动选择**，而是列出两个语言
3. 询问用户："检测到多语言（Java + Python），请确认主语言？"

用户确认后，系统按确认的语言加载参数块。这个"询问"不是麻烦，而是尊重用户的决定权——这是 Harness 的核心原则之一。

### 7.3 场景三：AI 框架项目

假设项目是 Python + LangChain + LangGraph 的 AI 应用。

自动检测会：
1. 检测到 `requirements.txt` 中包含 `langchain` 和 `langgraph`
2. 匹配到 LangChain 框架规则（langchain + langgraph 依赖在同一行规则中覆盖）
3. 加载 Python 基础参数 + LangChain 差异参数块

这种一次匹配的方式让系统能识别复合框架——LangChain 和 LangGraph 的参数差异在同一个差异参数块中定义。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 240" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_a5" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_a5"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="240" fill="url(#bg_a5)" rx="10"/>
  <text x="400" y="25" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">一次检测：从文件到框架的完整匹配</text>
  <rect x="30" y="50" width="240" height="55" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_a5)"/>
  <text x="150" y="72" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">读取项目文件</text>
  <text x="150" y="90" text-anchor="middle" fill="#94a3b8" font-size="10">requirements.txt</text>
  <line x1="270" y1="77" x2="300" y2="77" stroke="#475569" stroke-width="2"/>
  <polygon points="305,77 297,72 297,82" fill="#94a3b8"/>
  <rect x="310" y="50" width="240" height="55" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_a5)"/>
  <text x="430" y="72" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">匹配检测规则表</text>
  <text x="430" y="90" text-anchor="middle" fill="#94a3b8" font-size="10">langchain + langgraph 依赖</text>
  <line x1="550" y1="77" x2="580" y2="77" stroke="#475569" stroke-width="2"/>
  <polygon points="585,77 577,72 577,82" fill="#94a3b8"/>
  <rect x="590" y="50" width="180" height="55" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_a5)"/>
  <text x="680" y="72" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">判定框架 + 加载参数</text>
  <text x="680" y="90" text-anchor="middle" fill="#94a3b8" font-size="10">LangChain 参数块</text>
  <text x="400" y="145" text-anchor="middle" fill="#475569" font-size="11">一次检测即可完成从文件到框架的完整匹配——无需多轮递归</text>
  <text x="400" y="165" text-anchor="middle" fill="#94a3b8" font-size="10">检测规则表是扁平的，每条规则包含完整的"文件特征 → 框架 + 构建工具"映射</text>
</svg>
```

### 7.4 检测结果的"复用"与"重新检测"

检测结果不是"一次性"的。生成的参数表会被持久化到 `.harness/` 目录，后续每次对话都复用这份参数表，无需重新检测。这带来两个好处：
- **快**：后续对话无需重新扫描项目
- **稳**：参数表一旦生成，保持不变，技能行为稳定

当项目发生变化时（比如从 Maven 切换到 Gradle），用户可以触发"重新检测"，系统会重新扫描并更新参数表。重新检测遵循 Harness 的原则：**如果 `.harness/` 已存在，先询问用户是否覆盖**，绝不静默覆盖用户的配置。

### 7.5 检测系统的"可演进性"

检测规则库不是一成不变的。随着新框架的出现，规则库需要持续扩展：
- 新增一个框架 = 新增一条检测规则（文件匹配 + 内容匹配）+ 一个差异参数块
- 规则库是"插件式"的，可以独立增删，不影响核心系统

这种可演进性，让自动检测系统能"永远跟上"主流框架的发展。

## 八、自动检测的未来方向

### 8.1 AI 自主检测

目前的检测系统依赖"预定义的规则表"——约 128 条检测规则（6 种语言）对应 111 个框架差异块。未来的方向是让 AI 自主检测：当 AI 遇到一个未知的框架时，它可以通过分析项目文件（import 语句、依赖声明、配置模式）来推断框架类型，而无需预定义的规则。

这需要 AI 具备更强的"框架推理"能力——能从源码中推断出"这个项目用了什么框架"。这部分能力已经在 Claude 的代码理解能力中有所体现，但尚未系统化。

### 8.2 动态检测

目前的检测是"静态"的——在安装时执行一次，之后不再变化。未来的方向是"动态检测"：AI 在每次对话时都会重新检测项目的技术栈，自动切换参数集。

这意味着：同一个项目目录，如果新增了一个 `requirements.txt` 包含 tensorflow，AI 会自动识别并切换到 TensorFlow 参数集，不需要重新安装 Harness。

### 8.3 检测 + 社区框架包

当社区贡献的框架包越来越多时，检测规则库也需要支持"社区贡献"。一个框架包包含：
- 检测规则（匹配模式）
- 差异参数块（参数值）
- 框架描述（角色描述）

社区贡献者只需写一个 `frameworks/spring-ai.md`，安装后自动注册到检测规则库中。

## 九、总结

自动检测系统是 Harness 的"第一道门"。它让 AI 从"不知道这个项目是什么"到"一眼认出技术栈"，从"需要用户手动配置"到"零配置开箱即用"。

约 128 条检测规则构成了一个庞大的"框架指纹库"，覆盖了 Java、Python、Go、Rust、PHP、Frontend 六大语言生态中的主流框架。每条规则独立维护、可扩展，让系统能持续跟进新技术的发展。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 230" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_a6" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_a6"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="230" fill="url(#bg_a6)" rx="10"/>
  <text x="400" y="25" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">三大核心的"三驾马车"</text>
  <rect x="40" y="55" width="220" height="70" rx="10" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_a6)"/>
  <text x="150" y="78" text-anchor="middle" fill="#93c5fd" font-size="13" font-weight="700">工程纪律（Why）</text>
  <text x="150" y="98" text-anchor="middle" fill="#94a3b8" font-size="10">为什么需要纪律</text>
  <text x="150" y="114" text-anchor="middle" fill="#64748b" font-size="9">第一篇</text>
  <rect x="290" y="55" width="220" height="70" rx="10" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_a6)"/>
  <text x="400" y="78" text-anchor="middle" fill="#86efac" font-size="13" font-weight="700">参数化（How）</text>
  <text x="400" y="98" text-anchor="middle" fill="#94a3b8" font-size="10">如何让纪律可配置</text>
  <text x="400" y="114" text-anchor="middle" fill="#64748b" font-size="9">第二篇</text>
  <rect x="540" y="55" width="220" height="70" rx="10" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_a6)"/>
  <text x="650" y="78" text-anchor="middle" fill="#fde68a" font-size="13" font-weight="700">自动检测（What）</text>
  <text x="650" y="98" text-anchor="middle" fill="#94a3b8" font-size="10">如何让配置自动适配</text>
  <text x="650" y="114" text-anchor="middle" fill="#64748b" font-size="9">第三篇</text>
  <text x="400" y="175" text-anchor="middle" fill="#475569" font-size="11">三驾马车缺一不可：Why 决定了方向，How 决定了方法，What 决定了落地</text>
  <text x="400" y="200" text-anchor="middle" fill="#94a3b8" font-size="10">它们共同构成了 Harness 的完整技术栈：打破语言壁垒，让 AI 成为真正的全栈工程师</text>
</svg>
```

## 十、后记：核心思想系列的总结

三篇核心思想文章，从"观念"（工程纪律即架构）到"设计"（参数化）再到"落地"（自动检测），完整地拆解了 Harness 的技术架构。

- **第一篇**：工程纪律不是"约束"，而是"基础设施"。它让 AI 的行为可预测、可验证、可重复。
- **第二篇**：参数化不是"配置"，而是"抽象"。它让 32 个技能文件通过 38 个占位符统一驱动。
- **第三篇**：自动检测不是"猜测"，而是"识别"。它让 60+ 条规则精确匹配项目技术栈。

这三篇的核心思想，可以用一句话概括：**让 AI 成为"工程纪律的执行者"，而不是"工程细节的存储器"。**

在接下来的专栏文章中，我们将深入每个命令的设计哲学——从 `/harnessing` 到 `/coding-skill`，从 `/testing` 到 `/deploy`，每一篇都会拆解"为什么这个命令这么设计"以及"它背后的工程思考"。

### 7.6 自动检测的"性能"：检测速度

自动检测需要扫描项目文件，对于大项目，扫描速度是一个关键指标。检测系统的设计原则是：**构建文件优先，按序匹配。**

检测流程不是"扫描所有文件再匹配"，而是"逐个扫描，发现即返回"：
1. 先检查常见的构建文件（pom.xml、go.mod、requirements.txt等）
2. 一旦发现匹配，立即判定结果，不再继续检查其他语言分组
3. 如果所有构建文件检查后没有匹配，再参考项目描述文件

这种"懒惰扫描"策略，让检测速度与项目规模无关，只与"发现匹配的速度"有关。对于大部分项目，检测在 1-2 秒内完成。

### 7.7 检测的"精度"与"召回率"

自动检测的精度和召回率是衡量系统质量的关键指标：

- **精度（Precision）**：检测到的框架中，有多少是用户实际使用的框架？
- **召回率（Recall）**：用户实际使用的框架中，有多少被检测到了？

理想情况下，精度的目标是 100%（绝不误报），召回率的目标是 90%+（覆盖主流框架）。在 60+ 框架的检测规则库中，通过构建文件优先策略，精度达到了 99%+，召回率约 85%（部分小众框架无法被自动识别）。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 220" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_a7" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_a7"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="220" fill="url(#bg_a7)" rx="10"/>
  <text x="400" y="25" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">检测质量的"双指标"</text>
  <rect x="80" y="55" width="280" height="70" rx="10" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_a7)"/>
  <text x="220" y="78" text-anchor="middle" fill="#93c5fd" font-size="14" font-weight="700">精度 99%+</text>
  <text x="220" y="98" text-anchor="middle" fill="#94a3b8" font-size="10">绝不误报：检测到的框架一定是对的</text>
  <text x="220" y="114" text-anchor="middle" fill="#64748b" font-size="9">构建文件优先策略确保精度</text>
  <rect x="440" y="55" width="280" height="70" rx="10" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_a7)"/>
  <text x="580" y="78" text-anchor="middle" fill="#86efac" font-size="14" font-weight="700">召回率 ~85%</text>
  <text x="580" y="98" text-anchor="middle" fill="#94a3b8" font-size="10">覆盖主流框架，小众框架需手动指定</text>
  <text x="580" y="114" text-anchor="middle" fill="#64748b" font-size="9">手动覆盖 + 社区贡献提升召回率</text>
  <text x="400" y="170" text-anchor="middle" fill="#475569" font-size="11">精度优先于召回率——宁可"不检测"也不能"误检测"</text>
  <text x="400" y="195" text-anchor="middle" fill="#94a3b8" font-size="10">检测不到的框架，用户可以通过手动覆盖的方式指定，不影响精度</text>
</svg>
```

### 8.4 检测 + 多技术栈合并

一个项目同时使用多个框架的情况并不罕见。比如：
- Java + Spring Boot + Dubbo（微服务 + RPC）
- Python + FastAPI + LangChain（Web + LLM）
- Go + Gin + Kitex（HTTP + RPC）

未来的检测系统需要支持"多技术栈合并"：检测到多个框架时，自动合并它们的参数块，冲突时按优先级规则解决。

### 8.5 检测 + AI 自主参数化

最终极的形态是：**AI 通过检测结果，自主生成参数块。**

当 AI 检测到一个框架时，它不只是"加载预定义的参数块"，而是"根据检测结果，生成最适合这个项目的参数"。这意味着不同的 Spring Boot 项目，即使使用同一个框架，也可能有不同的参数值——因为 AI 会根据项目的具体配置（如使用了哪些模块、依赖的版本等）来微调参数。

### 9.1 自动检测的"核心贡献"

自动检测系统为 Harness 带来的核心贡献有三点：

1. **零配置体验**：用户不需要手动告诉 AI "我用什么技术栈"——AI 自己发现
2. **精确匹配**：60+ 条规则确保检测结果精确，不会误判
3. **可扩展**：新框架只需要新增一条检测规则 + 一个差异参数块

### 9.2 自动检测的"边界"：什么检测不到

自动检测也有其"边界"——一些情况无法通过检测规则自动识别：
- **自定义框架**：项目内部自研的框架，没有公开的构建文件痕迹
- **混合技术栈**：一个项目使用多种语言（如 Java + Python），但以某种语言为主
- **新型框架**：刚发布的框架，检测规则库中还没有对应的规则

这些"边界"情况，通过用户手动覆盖和社区贡献来解决。

### 10.1 三篇核心文章的关系

三篇核心思想文章，从"观念"（工程纪律即架构）到"设计"（参数化）再到"落地"（自动检测），完整地拆解了 Harness 的技术架构。它们不是独立的，而是递进的关系：

- **第一篇**设定了"为什么"——工程纪律不是约束，而是基础设施
- **第二篇**设定了"怎么做"——参数化让纪律可配置、可复用
- **第三篇**设定了"怎么落地"——自动检测让配置自动适配项目

### 10.2 核心思想总结

用一句话概括三篇核心思想：**让 AI 成为"工程纪律的执行者"，而不是"工程细节的存储器"。**

AI 不需要记住每个项目的测试命令、构建工具、代码规范——这些信息存储在参数表中。AI 只需要知道"如何执行测试命令"、"如何执行构建命令"——这些是通用的工程纪律。

在接下来的专栏文章中，我们将深入每个命令的设计哲学。从 `/harnessing` 到 `/coding-skill`，从 `/testing` 到 `/deploy`，每一篇都会拆解"为什么这个命令这么设计"以及"它背后的工程思考"。

---

