# 从 AI 辅助编程到 AI 驱动研发体系：Agent = Model + Harness

> 本文梳理研发方式的三段演进，并给出贯穿全文的公式：**Agent = Model + Harness**。
> 它是理解"为什么脚手架不是提示词合集，而是约束体系"的起点。

## 三段演进

### 1. AI 辅助编程：提升局部动作效率

靠人在提示词输入框里把「代码 + 要求」喂给 AI。业务规则每次都在提示词里重述，**新开一个会话又要重复输入一次**。AI 提高了单点动作的效率，但没有改变研发的形态。

### 2. Coding Agent：从生成内容走向执行任务

当 AI 能操作文件、代码、Git 和外部工具，它就不再只是"辅助"，而是能检索、修改文件并提交的执行者。此时：

```
Agent = Model + Harness
```

- **Model** 提供理解、推理、规划、生成能力。
- **Coding Agent Harness** 提供 Agent Loop、文件操作、工具使用、环境连接。

但通用 Coding Agent 缺三样东西：**业务知识、流程约束、完成标准**。所以 Agent 仍需要人反复补充背景，也可能把"命令执行成功"误判为"业务交付完成"。

### 3. 项目 Harness：让 AI 进入完整研发闭环

AI 驱动的研发体系，要在通用 Coding Agent 之上增加**项目 Harness**——把业务知识、源代码、项目规则、流程 Skill、质量门禁和验证证据放在同一工作空间，再通过 CLI/MCP 连接工作项、环境、配置、日志、数据库等动态事实。公式升级为：

```
业务研发 Agent = Model + Coding Agent 通用 Harness + 项目 Harness
```

此时产品、开发、测试在同一套体系里共同提出目标、约束与决策，人的重心从"逐步指导操作"转向"业务判断、方案选择、结果验收和高风险授权"。

## 与脚手架的关系

`tayama-harness-skills` 生成的 `.harness/` 就是「项目 Harness」的具体实例：

| 文档概念 | 脚手架对应 |
|---------|-----------|
| Coding Agent 通用 Harness | 宿主 AI 工具（文件/Git/工具调用能力） |
| 项目 Harness | `.harness/`（Owner Agent + Rules + Skills + Changes + Wiki） |
| 业务知识 / 技术上下文 | `.harness/wiki/` + `.harness/tech/` |
| 流程约束 / 完成标准 | `.harness/rules/` 的门禁与状态机 |
| 动态事实 | `.harness/动态事实来源.md`（MCP/CLI） |

**核心命题**：模型决定通用能力，项目 Harness 决定它在具体项目里能走多远。

> 关联：见《真正的瓶颈是上下文》《本地优先》《知识库保存什么》。
