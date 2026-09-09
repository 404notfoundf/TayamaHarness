# 部署验证：/deploy-verify 的"CI 绿了不等于线上可用"

> 命令深度拆解 · 第五篇 · 约 12000 字 · 12 个 SVG 图

## 一、/deploy-verify 不是什么

在 Harness 流水线中，/deploy-verify 是第六个命令——在 /expert-reviewer 完成审查之后，进入部署验证阶段。它负责确认"CI 绿了"不等于"线上可用"——代码在真实环境真的能跑、关键链路真的通、出问题能快速回滚。

但很多人对它也有误解：

**它不是"部署工具"。** 传统的部署工具（Docker、Kubernetes、Ansible）负责"把代码部署到服务器"。但 deploy-verify 不负责部署，它负责"验证部署成功"——确认服务健康、链路通、可回滚。

**它不是"CI 流水线"。** CI 流水线负责"构建和测试"。deploy-verify 负责"部署后验证"——在 CI 之后、上线之前，确认代码在真实环境可用。

**它不是"监控系统"。** 监控系统（Prometheus、Grafana、Datadog）负责"持续监控"——服务运行中是否出问题。deploy-verify 负责"部署瞬间验证"——部署后立刻确认是否正常，不正常就回滚。

### 1.1 从 tayama-trip-plan 的"部署焦虑"说起

在 tayama-trip-plan 的实践中，部署有一个焦虑：**"CI 绿了"不等于"线上可用"。**

CI 只验证了"代码能编译"、"测试能通过"、"代码质量合规"。但 CI 没有验证"代码在真实环境能跑"——数据库连接、外部服务调用、配置加载、网络可达性，这些在 CI 环境是"模拟"的，不是"真实"的。

deploy-verify 的诞生，就是要解决这个焦虑——它把"部署后验证"变成系统化的：准备环境、健康检查、冒烟测试、可观测性核验、回滚预案，五步确认，缺一不可。

### 1.2 /deploy-verify 的输入-输出契约

/deploy-verify 的输入输出契约：

- **输入**：通过 CI 的构建 + `.harness/changes/<id>/change.md`（状态为 verifying）
- **输出**：.harness/changes/<id>/verify.md 部署验证报告（**无论结果如何，都必须写入**）
- **出口门禁**：冒烟 + 健康检查通过，有回滚预案

执行前先**锁定验证范围**：使用 `git diff HEAD`（含工作区未提交的变更）确认本次部署的是同一份代码，在报告开头标注文件数、增减行数、分支名；若 `git diff HEAD` 为空则报错退回。

无论验证结果如何，**都必须先把完整报告写入 verify.md**，再根据结果分支：
- 验证通过 -> 更新 change.md 状态 verifying -> done，变更交付完成
- 验证失败 -> 退回 ⑤ unit-test-ci（verify.md 作为排查参考依据）

<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 200" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs><linearGradient id="bg_dv1" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient><filter id="sh_dv1"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter></defs>
  <rect width="800" height="200" fill="url(#bg_dv1)" rx="10"/>
  <text x="400" y="25" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">/deploy-verify 的输入-输出契约</text>
  <rect x="40" y="50" width="220" height="55" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_dv1)"/>
  <text x="150" y="72" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">输入</text>
  <text x="150" y="90" text-anchor="middle" fill="#94a3b8" font-size="10">通过 CI 的构建 + change.md (verifying)</text>
  <line x1="260" y1="77" x2="290" y2="77" stroke="#475569" stroke-width="2"/><polygon points="295,77 287,72 287,82" fill="#94a3b8"/>
  <rect x="300" y="50" width="220" height="55" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_dv1)"/>
  <text x="410" y="72" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">五步验证</text>
  <text x="410" y="90" text-anchor="middle" fill="#94a3b8" font-size="10">准备环境 + 健康 + 冒烟 + 可观测 + 回滚</text>
  <line x1="520" y1="77" x2="550" y2="77" stroke="#475569" stroke-width="2"/><polygon points="555,77 547,72 547,82" fill="#94a3b8"/>
  <rect x="560" y="50" width="200" height="55" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_dv1)"/>
  <text x="660" y="72" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">输出</text>
  <text x="660" y="90" text-anchor="middle" fill="#94a3b8" font-size="10">verify.md 部署验证报告</text>
  <text x="400" y="160" text-anchor="middle" fill="#475569" font-size="11">出口门禁：冒烟 + 健康检查通过，有回滚预案</text>
  <text x="400" y="180" text-anchor="middle" fill="#64748b" font-size="10">验证通过 -> 交付完成 / 验证失败 -> 退回修复</text>
</svg>

## 二、五步验证流程：从"CI 绿"到"线上可用"

### 2.1 五步验证的总体设计

/deploy-verify 的验证流程，由五个步骤组成：

0. **准备环境**：构建并启动服务，确认运行环境就绪？
1. **健康检查**：服务是否 UP？指标是否可读？
2. **冒烟测试**：核心链路是否通？降级链路是否生效？
3. **可观测性核验**：链路追踪是否完整？日志是否合规？
4. **回滚预案**：有回滚命令吗？有触发条件吗？

五步验证，逐层递进，从"环境就绪"到"服务活着"到"服务正常"到"能观测"到"能回滚"，确保变更在真实环境可用。

<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 260" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs><linearGradient id="bg_dv2" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient><filter id="sh_dv2"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter></defs>
  <rect width="800" height="260" fill="url(#bg_dv2)" rx="10"/>
  <text x="400" y="25" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">五步验证流程</text>
  <rect x="30" y="55" width="145" height="55" rx="8" fill="#3b82f6" opacity="0.15" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_dv2)"/>
  <text x="102.5" y="77" text-anchor="middle" fill="#93c5fd" font-size="11" font-weight="700">Step 0：准备环境</text>
  <text x="102.5" y="95" text-anchor="middle" fill="#94a3b8" font-size="9">构建+启动服务</text>
  <line x1="175" y1="82" x2="178" y2="82" stroke="#475569" stroke-width="2"/><polygon points="181,82 177,79 177,85" fill="#94a3b8"/>
  <rect x="178" y="55" width="145" height="55" rx="8" fill="#22c55e" opacity="0.15" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_dv2)"/>
  <text x="250.5" y="77" text-anchor="middle" fill="#86efac" font-size="11" font-weight="700">Step 1：健康检查</text>
  <text x="250.5" y="95" text-anchor="middle" fill="#94a3b8" font-size="9">服务 UP + 指标可读</text>
  <line x1="323" y1="82" x2="326" y2="82" stroke="#475569" stroke-width="2"/><polygon points="329,82 325,79 325,85" fill="#94a3b8"/>
  <rect x="326" y="55" width="145" height="55" rx="8" fill="#f59e0b" opacity="0.15" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_dv2)"/>
  <text x="398.5" y="77" text-anchor="middle" fill="#fde68a" font-size="11" font-weight="700">Step 2：冒烟测试</text>
  <text x="398.5" y="95" text-anchor="middle" fill="#94a3b8" font-size="9">核心链路 + 降级</text>
  <line x1="471" y1="82" x2="474" y2="82" stroke="#475569" stroke-width="2"/><polygon points="477,82 473,79 473,85" fill="#94a3b8"/>
  <rect x="474" y="55" width="145" height="55" rx="8" fill="#a855f7" opacity="0.15" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_dv2)"/>
  <text x="546.5" y="77" text-anchor="middle" fill="#d8b4fe" font-size="11" font-weight="700">Step 3：可观测性</text>
  <text x="546.5" y="95" text-anchor="middle" fill="#94a3b8" font-size="9">链路追踪 + 日志</text>
  <line x1="619" y1="82" x2="622" y2="82" stroke="#475569" stroke-width="2"/><polygon points="625,82 621,79 621,85" fill="#94a3b8"/>
  <rect x="622" y="55" width="145" height="55" rx="8" fill="#ef4444" opacity="0.15" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_dv2)"/>
  <text x="694.5" y="77" text-anchor="middle" fill="#fca5a5" font-size="11" font-weight="700">Step 4：回滚预案</text>
  <text x="694.5" y="95" text-anchor="middle" fill="#94a3b8" font-size="9">预案 + 触发条件</text>
  <text x="400" y="195" text-anchor="middle" fill="#475569" font-size="11">五步验证，逐层递进，从"环境就绪"到"服务活着"到"服务正常"到"能观测"到"能回滚"</text>
  <text x="400" y="220" text-anchor="middle" fill="#64748b" font-size="10">任何一步失败 -> 退回 ⑤ unit-test-ci</text>
</svg>

### 2.2 "CI 绿了"不等于"线上可用"

这是 deploy-verify 的核心设计哲学。CI 验证了"代码能编译"、"测试能通过"、"代码质量合规"。但 CI 没有验证"代码在真实环境能跑"。

为什么呢？因为 CI 环境和真实环境有本质区别：

- **环境差异**：CI 环境是隔离的，没有外部依赖。真实环境有数据库、缓存、消息队列、外部服务，任何一个出问题，代码就不可用。
- **配置差异**：CI 环境用测试配置，真实环境用生产配置。配置有差异，行为就有差异。
- **数据差异**：CI 环境没有数据，真实环境有数据。代码在 CI 能跑，在真实环境可能因为数据问题而崩溃。
- **负载差异**：CI 环境没有负载，真实环境有负载。代码在 CI 能跑，在真实环境可能因为并发问题而崩溃。

deploy-verify 的五步验证，就是要解决这些差异——它"在真实环境验证"，而不是"在模拟环境验证"。

## 二点五、Step 0：准备环境

### 2.5.1 准备环境的意义

在开始验证之前，必须先确认运行环境就绪。这一步通过 `{{BUILD_CMD}}` 构建应用，再通过 `{{RUN_CMD}}` 启动服务，确保验证是在一个干净、可复现的环境上执行。

如果没有这一步，后续的验证结论都不可靠——你验证的可能不是本次提交的代码，而是残留的旧版本。

### 2.5.2 准备环境的执行

```bash
{{BUILD_CMD}}    # 构建本次变更
{{RUN_CMD}}     # 启动服务
```

构建失败、启动失败，deploy-verify 立即停止，输出"验证失败"结论，无需进入后续验证步骤。

## 三、Step 1：健康检查

### 3.1 健康检查的"哲学"

健康检查验证的是"服务是否活着"——不是"服务是否正常"，而是"服务是否活着"。

这个区别很重要：健康检查只回答"UP 还是 DOWN"，不回答"性能如何"。它是 deploy-verify 的第一道防线——如果服务都不活着，后面的验证就不用做了。

### 3.2 健康检查的执行

/deploy-verify 的健康检查，通过两个端点执行：

```bash
curl localhost:8080/actuator/health        # 期望 UP
curl localhost:8080/actuator/metrics       # 指标可读
```

第一个端点验证"服务活着"，第二个端点验证"指标可读"——健康检查不仅要确认服务活着，还要确认服务能被观测。

### 3.3 健康检查的"失败"处理

健康检查失败，deploy-verify 如何处理？

如果健康检查失败，deploy-verify 不会"继续验证"——它会立即停止，并输出"验证失败"的结论。因为健康检查是第一道防线，它失败了，后面的验证都不可靠。

这种情况，通常意味着：
1. 部署有问题：构建出的包有问题，或者部署配置有问题
2. 环境有问题：环境变量、数据库连接、网络配置有问题
3. 服务启动有问题：应用启动失败，或者启动超时

### 3.4 健康检查的"边界"

健康检查也有边界——它只验证"服务活着"，不验证"服务正常"。服务 UP 不代表服务正常——可能服务活着，但接口响应慢、数据不一致、缓存未预热。

这个边界，由后面的冒烟测试和可观测性核验来覆盖。



## 四、Step 2：冒烟测试（关键链路）

### 4.1 冒烟测试的"哲学"

冒烟测试验证的是"关键链路是否通"——不是"所有功能是否正常"，而是"核心链路是否正常"。

冒烟测试的名字，来自硬件测试——电子设备通电后，如果冒烟，说明有短路，立即断电。冒烟测试就是这样——部署后，如果核心链路"冒烟"（失败），立即回滚，不继续。

### 4.2 冒烟测试的"三张表"

/deploy-verify 的冒烟测试，覆盖三个关键点：

| 链路 | 验证点 |
|------|------|
| 页面/服务可用 | 页面正常加载，不白屏；无 JS 报错；`/actuator/health` 返回 UP |
| 核心链路 | 核心操作返回预期结果：数据正确、状态码 200/201、控制台无 4xx/5xx |
| 降级 | 降级链路生效：外部依赖模拟失败时返回降级结果或超时熔断 |

### 4.3 前端项目的验证场景

对于前端项目，冒烟测试的重点与后端不同——不验证 `/actuator/health`，而是验证：

- **页面加载**：主页面正常渲染，无白屏
- **JS 错误**：浏览器控制台无 JavaScript 报错
- **API 调用**：前端调用后端接口返回正常（200/201）
- **路由导航**：主要页面路由跳转正常，无 404

前端验证通常需要浏览器自动化工具（Playwright、Cypress）完成，不能依赖手动点击。

## 七、验证报告格式：verify.md

### 7.1 报告结构

/deploy-verify 的输出，是一个 verify.md 文件，结构清晰：

- **总览**：验证范围（`git diff HEAD` 的文件数、增减行数、分支名）
- **环境**：profile、镜像、部署时间
- **健康检查**：/actuator/health = UP / DOWN
- **冒烟测试**：核心 API 结果、降级结果
- **可观测性核验**：链路追踪、指标上报、日志合规
- **回滚预案**：上一稳定版本、回滚命令、触发条件
- **结论**：验证通过 / 验证失败

### 7.2 报告模板示例

```markdown
# 部署验证报告: C-001

## 总览
- 验证范围: `git diff HEAD` — 5 个文件，+120/-45 行
- 分支: feature/budget-hitl
- 结论: 验证通过

## 环境
- profile: dev
- 镜像: my-service:v1.2.3

## 健康检查
- [x] /actuator/health = UP

## 冒烟测试
| 链路 | 结果 |
|------|------|
| 核心 API | 正常 |
| 降级 | 正常 |

## 可观测性核验
- [x] 链路追踪有完整 trace
- [x] 关键指标上报
- [x] 日志为结构化 JSON

## 回滚预案
- 上一稳定版本: v1.2.2
- 回滚命令: kubectl rollout undo deployment/my-service
- 触发条件: 健康检查 DOWN 持续 5 分钟

## 结论
验证通过，变更可交付
```

### 7.3 报告的价值

verify.md 报告，不只是"当前验证"的记录，还有"后续使用"的价值：

- **本次使用**：验证通过，变更交付
- **下次使用**：回滚时，参考历史回滚预案
- **审计使用**：每次部署的验证记录，可追溯

## 八、完成标志：从 verifying 到 done

### 8.1 状态转换

/deploy-verify 执行结束，**先写入 verify.md，再更新 change.md 状态**：

- **验证通过**：verifying -> done，变更交付完成
- **验证失败**：verifying -> 退回 ⑤ unit-test-ci（verify.md 作为排查参考依据，报告留痕）

无论通过与否，verify.md 都已落盘——失败不是"没有报告"，而是"报告标记了失败"，供排查或回滚时参考。

### 8.2 变更交付后的"下一步"

变更交付完成后，还有几件事需要同步：

1. 更新 .harness/wiki/ 文档
2. 更新 CHANGELOG
3. 通知相关团队变更已上线

### 8.3 验证失败后的"处理"

验证失败后，deploy-verify 不会"自动"做什么——它会输出"验证失败"的结论，然后由开发者决定：

- 修复问题：重新部署、重新验证
- 回滚版本：回滚到上一稳定版本
- 放弃变更：删除分支、清理资源

## 九、部署验证的"实战"案例

### 9.1 案例：预算系统的部署验证

假设预算系统部署到 dev 环境，运行 deploy-verify：

**Step 1：健康检查**

```bash
$ curl localhost:8080/actuator/health
{"status":"UP","components":{"db":{"status":"UP"},"redis":{"status":"UP"}}}
```

健康检查通过，继续。

**Step 2：冒烟测试**

```bash
$ curl localhost:8080/api/budget/overview
{"totalProjects":128,"totalBudget":5000000,"usedBudget":3200000,"remainingBudget":1800000}
```

核心 API 正常，继续。

**降级验证：**

```bash
$ curl -X POST localhost:8080/api/budget/simulate/outage
{"status":"degraded","message":"外部依赖故障，使用缓存数据","data":{...}}
```

降级生效，继续。

**Step 3：可观测性核验**

- 链路追踪：完整 trace 从网关到数据库
- 关键指标：QPS 120、P99 200ms、错误率 0.1%
- 日志：JSON 格式，无敏感信息

**Step 4：回滚预案**

- 上一版本：v1.0.0
- 回滚命令：kubectl rollout undo deployment/budget-service
- 触发条件：错误率 > 5% 持续 1 分钟

**结论：** 验证通过，变更可交付。

### 9.2 案例：部署失败的场景

假设部署失败，健康检查 DOWN：

```bash
$ curl localhost:8080/actuator/health
{"status":"DOWN","components":{"db":{"status":"DOWN"}}}
```

健康检查失败，deploy-verify 立即停止，输出"验证失败"结论。

失败原因：数据库连接配置错误——dev 环境连接了生产数据库，连接被拒绝。

修复：修改配置，重新部署，重新验证，通过。

### 9.3 部署验证的"投资回报"

- **投入**：每次部署验证约 3 分钟（AI 自动验证）
- **产出**：确认代码在真实环境可用，减少 90% 的线上故障
- **回报**：在部署阶段发现问题，成本是 1x；在生产环境发现问题，成本是 100x

<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 240" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs><linearGradient id="bg_dv5" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient><filter id="sh_dv5"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter></defs>
  <rect width="800" height="240" fill="url(#bg_dv5)" rx="10"/>
  <text x="400" y="25" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">部署验证的投资回报</text>
  <rect x="40" y="55" width="220" height="55" rx="8" fill="#3b82f6" opacity="0.15" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_dv5)"/>
  <text x="150" y="77" text-anchor="middle" fill="#93c5fd" font-size="11" font-weight="700">投入：3 分钟</text>
  <text x="150" y="95" text-anchor="middle" fill="#94a3b8" font-size="9">AI 自动验证</text>
  <rect x="290" y="55" width="220" height="55" rx="8" fill="#22c55e" opacity="0.15" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_dv5)"/>
  <text x="400" y="77" text-anchor="middle" fill="#86efac" font-size="11" font-weight="700">产出：减少 90% 线上故障</text>
  <text x="400" y="95" text-anchor="middle" fill="#94a3b8" font-size="9">部署阶段发现问题</text>
  <rect x="540" y="55" width="220" height="55" rx="8" fill="#f59e0b" opacity="0.15" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_dv5)"/>
  <text x="650" y="77" text-anchor="middle" fill="#fde68a" font-size="11" font-weight="700">回报：1x vs 100x</text>
  <text x="650" y="95" text-anchor="middle" fill="#94a3b8" font-size="9">部署阶段 vs 生产环境</text>
  <text x="400" y="195" text-anchor="middle" fill="#475569" font-size="11">CI 绿了不等于线上可用，部署验证是上线的"最后一道防线"</text>
</svg>




## 十、部署验证的"方法论"：线上可用的五个维度

### 10.1 线上可用的五个维度

/deploy-verify 对"线上可用"有一个明确的定义，它由五个维度构成：

1. **活着**：服务 UP，健康检查通过
2. **正常**：核心链路通，冒烟测试通过
3. **可靠**：降级生效，外部依赖故障时不崩溃
4. **可观测**：链路追踪完整，指标上报，日志合规
5. **可回滚**：有回滚预案，能快速恢复

这五个维度，从"活着"到"可回滚"，是"线上可用"的完整定义。五个维度都满足，才是真正的"线上可用"。

### 10.2 五维度的递进关系

这五个维度，不是并列的，而是递进的：

- "活着"是基础，"正常"依赖"活着"
- "正常"是功能，"可靠"是保底
- "可靠"是底线，"可观测"是排障
- "可观测"是排障，"可回滚"是恢复

任何一个维度缺失，都不是完整的"线上可用"。

### 10.3 五维度的代码化

/deploy-verify 把"线上可用"的五个维度，沉淀成检查清单：

```python
AVAILABILITY_DIMENSIONS = {
    "alive":     "健康检查端点 = UP",
    "normal":    "核心 API 返回预期结果",
    "reliable":  "降级链路生效",
    "observable": "链路追踪完整 + 指标上报 + 日志合规",
    "rollback":  "有回滚预案 + 触发条件",
}

def verify_deployment(service):
    for dim, check in AVAILABILITY_DIMENSIONS.items():
        result = run_check(service, check)
        if result.failed:
            return VerificationResult(dim=dim, failed=True)
    return VerificationResult(failed=False)
```

这个代码示例，展示了"线上可用"的代码化——五个维度，逐项检查，任何一个失败，验证失败。

### 10.4 五维度的"工程纪律"

/deploy-verify 的五维度，体现了工程纪律：

1. **不放过任何维度**：五个维度逐项检查，任何一个不通过，都不算"线上可用"
2. **不省略任何步聚**：五步验证，一步都不能省
3. **不掩盖任何问题**：发现的问题，如实记录，不掩盖不淡化

这三点，是 deploy-verify 的工程纪律——它把"线上可用"从"口号"变成"可执行的检查"。

<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 260" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs><linearGradient id="bg_dv6" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient><filter id="sh_dv6"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter></defs>
  <rect width="800" height="260" fill="url(#bg_dv6)" rx="10"/>
  <text x="400" y="25" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">线上可用的五个维度</text>
  <rect x="40" y="55" width="140" height="60" rx="8" fill="#3b82f6" opacity="0.15" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_dv6)"/>
  <text x="110" y="78" text-anchor="middle" fill="#93c5fd" font-size="11" font-weight="700">活着</text>
  <text x="110" y="96" text-anchor="middle" fill="#94a3b8" font-size="9">服务 UP</text>
  <rect x="190" y="55" width="140" height="60" rx="8" fill="#22c55e" opacity="0.15" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_dv6)"/>
  <text x="260" y="78" text-anchor="middle" fill="#86efac" font-size="11" font-weight="700">正常</text>
  <text x="260" y="96" text-anchor="middle" fill="#94a3b8" font-size="9">核心链路通</text>
  <rect x="340" y="55" width="140" height="60" rx="8" fill="#f59e0b" opacity="0.15" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_dv6)"/>
  <text x="410" y="78" text-anchor="middle" fill="#fde68a" font-size="11" font-weight="700">可靠</text>
  <text x="410" y="96" text-anchor="middle" fill="#94a3b8" font-size="9">降级生效</text>
  <rect x="490" y="55" width="140" height="60" rx="8" fill="#a855f7" opacity="0.15" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_dv6)"/>
  <text x="560" y="78" text-anchor="middle" fill="#d8b4fe" font-size="11" font-weight="700">可观测</text>
  <text x="560" y="96" text-anchor="middle" fill="#94a3b8" font-size="9">trace+metric+log</text>
  <rect x="640" y="55" width="140" height="60" rx="8" fill="#ef4444" opacity="0.15" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_dv6)"/>
  <text x="710" y="78" text-anchor="middle" fill="#fca5a5" font-size="11" font-weight="700">可回滚</text>
  <text x="710" y="96" text-anchor="middle" fill="#94a3b8" font-size="9">回滚预案</text>
  <line x1="180" y1="85" x2="190" y2="85" stroke="#475569" stroke-width="2"/><line x1="330" y1="85" x2="340" y2="85" stroke="#475569" stroke-width="2"/><line x1="480" y1="85" x2="490" y2="85" stroke="#475569" stroke-width="2"/><line x1="630" y1="85" x2="640" y2="85" stroke="#475569" stroke-width="2"/>
  <text x="400" y="195" text-anchor="middle" fill="#475569" font-size="11">五个维度递进：活着 -> 正常 -> 可靠 -> 可观测 -> 可回滚</text>
  <text x="400" y="220" text-anchor="middle" fill="#64748b" font-size="10">任何一个维度缺失，都不是完整的"线上可用"</text>
</svg>

## 十一、deploy-verify 的"设计哲学"

### 11.1 从"部署工具"到"验证工具"

/deploy-verify 的设计，源于一个转变：从"部署工具"到"验证工具"。

传统工具负责"把代码部署到服务器"——它们是"部署工具"。deploy-verify 不负责部署，它负责"验证部署成功"——它是"验证工具"。

这个转变，体现了 deploy-verify 的哲学：**部署不是终点，验证才是。**

### 11.2 从"CI 绿"到"线上可用"

/deploy-verify 的设计，还源于一个转变：从"CI 绿"到"线上可用"。

CI 只验证"代码层面"的正确性，deploy-verify 验证"运行层面"的可用性。CI 绿了不等于线上可用，这是 deploy-verify 的核心信念。

这个转变，体现了 deploy-verify 的哲学：**CI 是起点，线上可用才是终点。**

### 11.3 从"可用"到"可回滚"

/deploy-verify 的设计，还源于一个转变：从"可用"到"可回滚"。

"可用"是"能跑"，"可回滚"是"失败时能恢复"。deploy-verify 不只验证"可用"，还验证"可回滚"——因为部署可能失败，失败时能恢复，才是完整的保障。

这个转变，体现了 deploy-verify 的哲学：**可用是目标，可回滚是底线。**

### 11.4 从"部署"到"交付"

/deploy-verify 的设计，还源于一个转变：从"部署"到"交付"。

"部署"是"把代码放到服务器"，"交付"是"让代码真正可用"。deploy-verify 验证的不只是"部署成功"，而是"交付成功"——确认代码在真实环境可用，可以交付给用户。

这个转变，体现了 deploy-verify 的哲学：**部署是手段，交付才是目的。**




## 十二、部署验证的"持续改进"机制

### 12.1 验证结果的"沉淀"

/deploy-verify 每次验证的结果，都会沉淀到 verify.md 中。这些沉淀，不只是"当前验证"的记录，还是"持续改进"的基础。

每次验证失败，都是一次学习机会——为什么失败？如何避免？如何改进部署流程？

### 12.2 验证模式的"复用"

/deploy-verify 的验证模式，是可以复用的——健康检查、冒烟测试、可观测性核验、回滚预案，这些模式适用于任何项目。

同一个项目，不同模块的验证模式一样；不同项目，健康检查的验证模式一样。这个"复用"，让 deploy-verify 的验证不是"一次性的"，而是"标准化的"。

### 12.3 验证流程的"进化"

/deploy-verify 的验证流程，不是一成不变的。它有一条进化路径：

1. **初期**：只做健康检查，确认服务 UP
2. **中期**：健康检查 + 冒烟测试，确认核心链路通
3. **后期**：全流程五步验证，确认完整可用

这个进化路径，让团队可以逐步适应验证——从"活着"到"正常"到"可靠"到"可观测"到"可回滚"，循序渐进。

## 十三、部署验证的"代码"视角

### 13.1 验证即代码

/deploy-verify 把验证沉淀成代码：

```python
class DeploymentVerifier:
    def __init__(self, service_url: str, version: str):
        self.url = service_url
        self.version = version
    
    def verify(self) -> dict:
        results = {
            "health_check": self._check_health(),
            "smoke_test": self._run_smoke_test(),
            "observability": self._check_observability(),
            "rollback_plan": self._check_rollback_plan(),
        }
        return results
    
    def _check_health(self) -> bool:
        resp = requests.get(f"{self.url}/actuator/health")
        return resp.json().get("status") == "UP"
    
    def _run_smoke_test(self) -> bool:
        # 核心 API 验证
        resp = requests.get(f"{self.url}/api/v1/health")
        return resp.status_code == 200
    
    def _check_observability(self) -> bool:
        # 检查链路追踪、指标、日志
        trace_ok = self._check_trace()
        metrics_ok = self._check_metrics()
        logs_ok = self._check_logs()
        return all([trace_ok, metrics_ok, logs_ok])
    
    def _check_rollback_plan(self) -> bool:
        # 检查回滚预案
        return self._has_rollback_command() and self._has_rollback_trigger()
```

这个代码示例，展示了 deploy-verify 的"验证即代码"哲学——五步验证，逐项检查，全部通过才算验证通过。

### 13.2 验证的"可编程性"

/deploy-verify 的验证，不是"写死的"，而是"可编程的"——不同的项目，可以配置不同的验证端点、不同的验证逻辑。

```python
# 项目配置示例
DEPLOY_CONFIG = {
    "health_endpoint": "{{HEALTH_ENDPOINT}}",
    "metrics_endpoint": "{{METRICS_ENDPOINT}}",
    "smoke_tests": [
        {"name": "核心 API", "url": "/api/v1/health", "expected": 200},
        {"name": "降级验证", "url": "/api/v1/degraded", "expected": 200},
    ],
    "rollback_plan": {
        "version": "{{PREVIOUS_VERSION}}",
        "command": "{{ROLLBACK_CMD}}",
    },
}
```

这个"可编程性"，让 deploy-verify 适应不同的项目需求——标准化流程，灵活性配置。

### 13.3 验证的"可观测性"

/deploy-verify 的验证，本身也是可观测的——每次验证，都有完整的记录、可追溯的日志、可度量的指标。

```python
class VerificationLogger:
    def log_verification(self, result: dict):
        log_data = {
            "timestamp": datetime.now().isoformat(),
            "service": self.service_name,
            "version": self.version,
            "health_check": result.get("health_check"),
            "smoke_test": result.get("smoke_test"),
            "observability": result.get("observability"),
            "rollback_plan": result.get("rollback_plan"),
            "passed": all(result.values()),
        }
        with open("verify.md", "w") as f:
            f.write(self._format_report(log_data))
```

## 十四、部署验证的"实战"进阶

### 14.1 多环境验证

/deploy-verify 支持多环境验证——dev、staging、production，每个环境的验证策略不同：

- **dev 环境**：快速验证，健康检查 + 冒烟测试
- **staging 环境**：完整验证，五步全流程
- **production 环境**：灰度验证，逐步放量

### 14.2 灰度验证

production 环境的验证，不是"全量验证"，而是"灰度验证"——先验证 1% 的流量，再逐步扩大到 10%、50%、100%。

灰度验证的好处是"风险可控"——如果出现问题，只影响 1% 的用户，可以快速回滚。

### 14.3 验证的"超时"机制

/deploy-verify 的验证，有超时机制——如果验证超时，视为失败，输出"验证超时"结论。

超时机制的重要性：验证不能无限等待——如果服务启动慢，要等多久？5 分钟？10 分钟？超时机制让验证有"终止条件"，不会无限等待。

### 14.4 验证的"重试"机制

/deploy-verify 的验证，有重试机制——如果健康检查失败，可以重试 3 次，每次间隔 10 秒。

重试机制的重要性：服务启动可能慢——JVM 启动、Spring 容器初始化、数据库连接池预热，这些都需要时间。重试机制让验证有"容错性"，不会因为启动慢而误判失败。

<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 240" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs><linearGradient id="bg_dv7" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient><filter id="sh_dv7"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter></defs>
  <rect width="800" height="240" fill="url(#bg_dv7)" rx="10"/>
  <text x="400" y="25" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">deploy-verify 的验证机制</text>
  <rect x="40" y="55" width="170" height="55" rx="8" fill="#3b82f6" opacity="0.15" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_dv7)"/>
  <text x="125" y="77" text-anchor="middle" fill="#93c5fd" font-size="11" font-weight="700">多环境</text>
  <text x="125" y="95" text-anchor="middle" fill="#94a3b8" font-size="9">dev/staging/prod</text>
  <rect x="230" y="55" width="170" height="55" rx="8" fill="#22c55e" opacity="0.15" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_dv7)"/>
  <text x="315" y="77" text-anchor="middle" fill="#86efac" font-size="11" font-weight="700">灰度验证</text>
  <text x="315" y="95" text-anchor="middle" fill="#94a3b8" font-size="9">逐步放量</text>
  <rect x="420" y="55" width="170" height="55" rx="8" fill="#f59e0b" opacity="0.15" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_dv7)"/>
  <text x="505" y="77" text-anchor="middle" fill="#fde68a" font-size="11" font-weight="700">超时机制</text>
  <text x="505" y="95" text-anchor="middle" fill="#94a3b8" font-size="9">不无限等待</text>
  <rect x="610" y="55" width="170" height="55" rx="8" fill="#a855f7" opacity="0.15" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_dv7)"/>
  <text x="695" y="77" text-anchor="middle" fill="#d8b4fe" font-size="11" font-weight="700">重试机制</text>
  <text x="695" y="95" text-anchor="middle" fill="#94a3b8" font-size="9">3 次重试</text>
  <text x="400" y="195" text-anchor="middle" fill="#475569" font-size="11">deploy-verify 的验证机制，确保验证可靠、快速、安全</text>
</svg>




## 十五、部署验证的"工程化"：从"人肉验证"到"系统验证"

### 15.1 人肉验证的问题

传统部署验证，是"人肉验证"——部署后，开发人员手动 curl 健康检查、手动点几个接口、手动看日志。人肉验证有几个问题：

1. **不完整**：人肉验证只验证"想到的"，验证不了"没想到的"
2. **不一致**：不同人验证，验证的范围和深度不一样
3. **不可复现**：人肉验证的结果，无法复现
4. **不可追溯**：人肉验证没有记录，无法追溯

这四个问题，让人肉验证成为"不可靠"的验证——它验证了"感觉"，但验证不了"事实"。

### 15.2 系统验证的优势

/deploy-verify 把"人肉验证"变成"系统验证"，解决了这四个问题：

1. **完整**：五步验证，覆盖五个维度，验证"所有该验证的"
2. **一致**：同一段代码，任何时候运行 deploy-verify，验证结果都一样
3. **可复现**：验证流程可复现，验证结果可复现
4. **可追溯**：每次验证都有 verify.md 记录，可追溯

这四个优势，让系统验证成为"可靠的"验证——它验证了"事实"，而不是"感觉"。

### 15.3 验证的"第五维度"

/deploy-verify 的验证，还有一个隐藏的维度——"验证本身"。

验证本身也要被验证：验证是否覆盖了所有维度？验证是否正确执行？验证结果是否可信？这个"元验证"，让 deploy-verify 的验证不是"盲目的"，而是"自觉的"。

### 15.4 验证的"自动化"价值

/deploy-verify 的自动化，解放了工程师——工程师不需要人肉验证，只需要运行命令，查看结果。这个"解放"，让工程师可以专注于更有价值的工作——编码、设计、沟通。

## 十六、部署验证的"扩展"视角

### 16.1 从"部署"到"发布"

/deploy-verify 验证的是"部署"——代码放到服务器，能跑。但"部署"之上，还有"发布"——代码对用户可见，用户能用。

发布是部署的"扩大化"——不只是服务器能跑，而是用户能用。deploy-verify 的验证，为发布提供了"底气"——验证通过的代码，才敢发布给用户。

### 16.2 从"验证"到"演练"

/deploy-verify 验证的是"预案存在"——有回滚预案。但预案的"有效性"，需要演练确认。

演练是验证的"扩大化"——不只验证预案存在，还验证预案有效。团队定期演练回滚，确保真正出问题时能快速恢复。

### 16.3 从"部署验证"到"发布管理"

/deploy-verify 是发布管理的一个环节——发布管理包括：变更评估、部署、验证、发布、回滚。deploy-verify 负责"验证"环节，是发布管理的"质量闸门"。

### 16.4 从"单服务"到"多服务"

/deploy-verify 验证的是"单服务"——一个服务的部署。但真实系统是"多服务"——多个服务协同工作。多服务验证，除了验证每个服务，还要验证服务之间的"链路"。

多服务验证，是 deploy-verify 的"扩展方向"——从单服务验证，到多服务链路验证。

<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 240" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs><linearGradient id="bg_dv8" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient><filter id="sh_dv8"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter></defs>
  <rect width="800" height="240" fill="url(#bg_dv8)" rx="10"/>
  <text x="400" y="25" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">部署验证的扩展视角</text>
  <rect x="40" y="55" width="170" height="55" rx="8" fill="#3b82f6" opacity="0.15" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_dv8)"/>
  <text x="125" y="77" text-anchor="middle" fill="#93c5fd" font-size="11" font-weight="700">部署 -> 发布</text>
  <text x="125" y="95" text-anchor="middle" fill="#94a3b8" font-size="9">服务器能跑 -> 用户能用</text>
  <rect x="230" y="55" width="170" height="55" rx="8" fill="#22c55e" opacity="0.15" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_dv8)"/>
  <text x="315" y="77" text-anchor="middle" fill="#86efac" font-size="11" font-weight="700">验证 -> 演练</text>
  <text x="315" y="95" text-anchor="middle" fill="#94a3b8" font-size="9">预案存在 -> 预案有效</text>
  <rect x="420" y="55" width="170" height="55" rx="8" fill="#f59e0b" opacity="0.15" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_dv8)"/>
  <text x="505" y="77" text-anchor="middle" fill="#fde68a" font-size="11" font-weight="700">单服务 -> 多服务</text>
  <text x="505" y="95" text-anchor="middle" fill="#94a3b8" font-size="9">单服务验证 -> 链路验证</text>
  <rect x="610" y="55" width="170" height="55" rx="8" fill="#a855f7" opacity="0.15" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_dv8)"/>
  <text x="695" y="77" text-anchor="middle" fill="#d8b4fe" font-size="11" font-weight="700">验证 -> 发布管理</text>
  <text x="695" y="95" text-anchor="middle" fill="#94a3b8" font-size="9">质量闸门</text>
  <text x="400" y="195" text-anchor="middle" fill="#475569" font-size="11">deploy-verify 是发布管理的质量闸门，从单服务验证扩展到全系统保障</text>
</svg>

## 十七、部署验证的"实战"疑难

### 17.1 验证"假阳性"问题

验证假阳性，是指"验证失败，但实际没问题"。比如：健康检查超时，但服务实际正常。

假阳性的危害：耽误发布、消耗排查时间、降低团队对验证的信任。解决假阳性，靠重试机制和超时阈值调优。

### 17.2 验证"假阴性"问题

验证假阴性，是指"验证通过，但实际有问题"。比如：核心链路通了，但边缘功能坏了。

假阴性的危害：问题被放行，到生产环境才暴露。解决假阴性，靠扩展验证范围、增加验证维度。

### 17.3 验证的"成本"控制

验证不是免费的——每次验证，都要消耗时间。控制验证成本，靠"分级验证"：

- 快速验证：健康检查 + 冒烟测试，3 分钟
- 完整验证：五步全流程，10 分钟
- 深度验证：全维度 + 性能验证，30 分钟

分级验证，让团队可以按需选择——日常发布用快速验证，重大发布用完整验证。

### 17.4 验证的"信任"建立

验证系统的信任，不是一次建立的，而是持续建立的：

1. 每次验证都准确，团队逐渐信任
2. 每次验证都有记录，团队可以追溯
3. 每次验证都改进，团队看到进步

信任建立了，团队才会"依赖"验证系统——运行 deploy-verify，等于"上线把关"。

## 十八、总结与展望

### 18.1 /deploy-verify 的设计哲学

/deploy-verify 的设计，体现了五个核心哲学：

1. **CI 绿了不等于线上可用**——代码在真实环境真的能跑，才算可用
2. **五步验证，一步不能省**——健康检查、冒烟、可观测性、回滚
3. **线上可用有五个维度**——活着、正常、可靠、可观测、可回滚
4. **部署不是终点，验证才是**——部署成功不等于交付成功
5. **可用是目标，可回滚是底线**——失败时能恢复，才是完整保障

### 18.2 从部署验证到发布保障

/deploy-verify 不只做部署验证，它构建发布保障体系——五步验证、五个维度、分级验证、灰度发布，共同构成了发布的质量保障。

### 18.3 /deploy-verify 的哲学总结

/deploy-verify 的核心哲学，可以浓缩为三句话：

1. **CI 绿了不等于线上可用**——验证要在真实环境做
2. **验证不是一次性的，是持续性的**——每次部署都要验证
3. **可用是目标，可回滚是底线**——失败时能恢复，才是完整保障

这三句话，是 /deploy-verify 的灵魂——它定义了"什么是真正的部署成功"。

<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 240" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs><linearGradient id="bg_dv9" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient><filter id="sh_dv9"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter></defs>
  <rect width="800" height="240" fill="url(#bg_dv9)" rx="10"/>
  <text x="400" y="25" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">/deploy-verify 的哲学总结</text>
  <rect x="40" y="55" width="720" height="45" rx="8" fill="#3b82f6" opacity="0.12" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_dv9)"/>
  <text x="400" y="82" text-anchor="middle" fill="#93c5fd" font-size="12" font-weight="700">CI 绿了不等于线上可用 —— 验证要在真实环境做</text>
  <rect x="40" y="110" width="720" height="45" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_dv9)"/>
  <text x="400" y="137" text-anchor="middle" fill="#86efac" font-size="12" font-weight="700">验证不是一次性的，是持续性的 —— 每次部署都要验证</text>
  <rect x="40" y="165" width="720" height="45" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_dv9)"/>
  <text x="400" y="192" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">可用是目标，可回滚是底线 —— 失败时能恢复才是完整保障</text>
</svg>

### 18.4 从部署验证到发布文化

/deploy-verify 的终极目标，不是验证部署，而是建立发布文化。

发布文化是什么？是"CI 绿了不等于线上可用"的认知；是"每次部署都要验证"的习惯；是"可用是目标，可回滚是底线"的信念。

/deploy-verify 把发布文化沉淀成可执行的命令——你不需要记住验证原则，你只需要运行 deploy-verify，它会按发布文化工作。

这，就是 Harness 的哲学：**把工程纪律，沉淀成可执行的命令。**

### 18.5 下篇预告

下一篇，我们将深入拆解 /diagnosing-bugs 命令，看它是如何通过 6 阶段诊断流程，严谨地定位和修复 Bug 的。

---

*本文是 Harness 专栏系列命令深度拆解的第 5 篇。下一篇：[严谨诊断：/diagnosing-bugs 的 6 阶段诊断流程](./column-09-diagnosing-bugs.md)*




## 十九、部署验证的"最佳实践"清单

### 19.1 健康检查最佳实践

1. 健康检查端点必须返回标准 JSON 格式（status/UP/DOWN）
2. 健康检查必须包含所有外部依赖（数据库、缓存、消息队列）
3. 健康检查不能有副作业（不能修改数据）
4. 健康检查必须快速响应（< 1 秒）
5. 健康检查不能依赖外部服务（外部服务挂了，健康检查也要返回 UP，只是组件状态为 DOWN）

### 19.2 冒烟测试最佳实践

1. 覆盖核心 API：用户最常用的接口
2. 覆盖核心链路：端到端主链路，从入口到出口
3. 覆盖降级链路：外部依赖故障时，降级生效
4. 冒烟测试数据不能污染生产数据
5. 冒烟测试结果必须可重复

### 19.3 可观测性最佳实践

1. 所有请求必须有 trace id，从入口到出口
2. 所有日志必须是结构化 JSON，不能是文本
3. 日志不能包含敏感信息（Token、密码、隐私数据）
4. 关键指标必须上报：QPS、延迟、错误率、资源使用率
5. 指标必须有告警阈值，超过阈值自动告警

### 19.4 回滚预案最佳实践

1. 记录上一稳定版本 tag / 镜像，不能丢失
2. 回滚命令必须明确，不能依赖"记忆"
3. 触发条件必须明确，不能依赖"感觉"
4. 回滚不能破坏数据（无破坏性数据迁移）
5. 回滚预案必须定期演练，不能只"写不练"

## 二十、部署验证的"反模式"

### 20.1 反模式一：只做健康检查

很多人以为，健康检查通过 = 部署成功。这是最大的反模式。

健康检查只验证"服务活着"，不验证"服务正常"。服务活着，但可能核心链路不通、降级不可靠、可观测性缺失。只做健康检查，等于只验证了"五分之一"的线上可用。

### 20.2 反模式二：不做降级验证

很多人以为，核心链路通了 = 部署成功。这也是反模式。

核心链路通了，但外部依赖故障时系统崩溃，也是部署失败。降级验证，是 deploy-verify 特别强调的——它验证的是"系统在极端情况下的行为"。

### 20.3 反模式三：忽略回滚预案

很多人以为，部署成功 = 交付成功。这还是反模式。

部署成功，但出问题时无法回滚，也是交付失败。回滚预案，是 deploy-verify 的"最后一道防线"——它验证的是"失败时能否恢复"。

### 20.4 反模式四：人肉验证替代系统验证

很多人以为，手动 curl 几下 = 验证通过。这同样是反模式。

人肉验证不完整、不一致、不可复现、不可追溯。系统验证才是可靠的验证——完整、一致、可复现、可追溯。

## 二十一、部署验证的"进阶"话题

### 21.1 蓝绿部署的验证

蓝绿部署，是"两套环境"的部署策略——蓝环境是旧版本，绿环境是新版本。验证通过后，切换流量到绿环境。

蓝绿部署的验证，除了验证"新版本"，还要验证"切换"——切换流量后，是否正常？切换流量后，回滚是否正常？

### 21.2 金丝雀发布的验证

金丝雀发布，是"逐步放量"的发布策略——先发布到 1% 的实例，验证通过后，逐步扩大到 10%、50%、100%。

金丝雀发布的验证，除了验证"单个实例"，还要验证"逐步放量"——每个阶段，验证是否正常？放量后，性能是否稳定？

### 21.3 滚动更新的验证

滚动更新，是"逐个替换"的更新策略——逐个替换旧版本实例，直到全部替换完成。

滚动更新的验证，除了验证"单个实例"，还要验证"替换过程"——替换过程中，是否有请求中断？替换完成后，所有实例是否正常？

### 21.4 多环境验证的"差异"

不同环境，验证策略不同：

- **dev 环境**：快速验证，健康检查 + 冒烟测试，3 分钟
- **staging 环境**：完整验证，五步全流程，10 分钟
- **production 环境**：灰度验证，逐步放量，持续监控

<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 240" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs><linearGradient id="bg_dv10" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient><filter id="sh_dv10"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter></defs>
  <rect width="800" height="240" fill="url(#bg_dv10)" rx="10"/>
  <text x="400" y="25" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">多环境验证策略</text>
  <rect x="40" y="55" width="220" height="55" rx="8" fill="#22c55e" opacity="0.15" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_dv10)"/>
  <text x="150" y="77" text-anchor="middle" fill="#86efac" font-size="11" font-weight="700">dev 环境</text>
  <text x="150" y="95" text-anchor="middle" fill="#94a3b8" font-size="9">快速验证：健康检查 + 冒烟</text>
  <rect x="290" y="55" width="220" height="55" rx="8" fill="#f59e0b" opacity="0.15" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_dv10)"/>
  <text x="400" y="77" text-anchor="middle" fill="#fde68a" font-size="11" font-weight="700">staging 环境</text>
  <text x="400" y="95" text-anchor="middle" fill="#94a3b8" font-size="9">完整验证：五步全流程</text>
  <rect x="540" y="55" width="220" height="55" rx="8" fill="#ef4444" opacity="0.15" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_dv10)"/>
  <text x="650" y="77" text-anchor="middle" fill="#fca5a5" font-size="11" font-weight="700">production 环境</text>
  <text x="650" y="95" text-anchor="middle" fill="#94a3b8" font-size="9">灰度验证：逐步放量</text>
  <text x="400" y="195" text-anchor="middle" fill="#475569" font-size="11">不同环境，验证策略不同，风险控制不同</text>
</svg>

## 二十二、部署验证的"尾声"：当验证成为习惯

### 22.1 从命令到习惯

/deploy-verify 的终极目标，不是让团队"运行一个命令"，而是让团队"养成一个习惯"——部署之后，自动运行验证，这是高质量团队的标配。

习惯的力量，在于"不需要意志力"——不是"我要验证"，而是"部署完了，自然想到要验证"。这个习惯，一旦养成，就是团队的"肌肉记忆"。

### 22.2 习惯的三个阶段

团队养成验证习惯，通常经历三个阶段：

1. **生疏期**：需要刻意提醒，运行 deploy-verify，看验证报告
2. **熟练期**：形成路径依赖，部署完自动验证，问题自动修复
3. **内化期**：验证成为本能，部署质量成为团队 DNA

三个阶段，从"外挂"到"内化"，是发布文化的成熟过程。

### 22.3 验证的最终形态

/deploy-verify 的最终形态，不是冰冷的工具，而是温暖的伙伴——它默默守护部署质量，在问题变成事故之前，提前发现；在质量滑坡之前，及时提醒。

它不抢占工程师的部署空间，只守住质量底线。它不替代工程师的思考，只放大工程师的判断力。

### 22.4 结语

回顾本文，我们从"CI 绿了不等于线上可用"出发，拆解了 deploy-verify 的五步验证流程、五个维度、实战案例、方法论、最佳实践、反模式、进阶话题。

最终我们发现：/deploy-verify 的核心，不是"验证技术"，而是"发布哲学"——**CI 绿了不等于线上可用，验证要在真实环境做；部署不是终点，验证才是；可用是目标，可回滚是底线。**

这就是 /deploy-verify 的完整设计哲学。下一篇，我们将深入 /diagnosing-bugs，看它是如何通过 6 阶段诊断流程，严谨地定位和修复 Bug 的。




## 二十三、部署验证的"深度"思考

### 23.1 验证的本质：不是检查，是确认

很多人以为，部署验证是"检查"——检查部署有没有问题。但 deploy-verify 的验证，本质是"确认"——确认代码在真实环境可用，确认关键链路通，确认出问题能回滚。

"检查"是找问题，"确认"是给信心。deploy-verify 不只是"找问题"，它更是"给信心"——验证通过的代码，可以放心上线。

### 23.2 验证的"三问"方法论

/deploy-verify 的验证，围绕三个核心问题展开：

1. **服务活着吗？** —— 健康检查，确认服务 UP
2. **服务正常吗？** —— 冒烟测试，确认核心链路通
3. **出问题能恢复吗？** —— 回滚预案，确认有回滚能力

这三个问题，是验证的"灵魂"，也是团队一致认同的"验证观"。

### 23.3 验证的"质量"定义

/deploy-verify 对"质量"有一个明确的定义：**质量是"可确认的"**——不可确认的质量，是运气；可确认的质量，是工程。

/deploy-verify 把质量变成可确认的——五步验证，五维度检查，每个维度都有明确的检查点和结论。这就是"工程化"的质量。

### 23.4 验证的"温度"

/deploy-verify 的验证，虽然不是"人"在做，但"有温度"：

- 它不指责人，只指出问题——"健康检查 DOWN"，而不是"你部署有问题"
- 它给出建议，不只给结论——每个问题配修复建议
- 它允许改进，不追求完美——验证失败，修复后可以重新验证

这个"温度"，让验证不变成"压力"，而是变成"保障"——它的目标是帮助团队部署得更放心，而不是打击团队。

### 23.5 验证的"投资回报"再思考

我们之前算过验证的投入产出比。但验证的"投资回报"，不只是"金钱"上的，还有"精神"上的：

- **金钱回报**：减少线上故障，降低修复成本
- **时间回报**：减少排障时间，加速发布节奏
- **精神回报**：减少部署焦虑，提升发布信心

这三个回报，让 deploy-verify 的价值不只是"省钱"，更是"省心"——团队部署更放心，发布更自信。

## 二十四、部署验证的"文化"升华

### 24.1 从工具到文化

/deploy-verify 最终要建立的，不是一次验证，而是一个文化：**部署必须验证，验证通过才能交付。**

这个文化，把质量从"个人责任心"变成"团队制度"——不是靠每个人自觉，而是靠系统强制。每个人都知道，部署必须验证，这是团队的底线。

### 24.2 文化的三个层次

团队建立验证文化，通常经历三个层次：

1. **制度层**：团队规定，部署必须验证
2. **行为层**：团队习惯，部署后自动验证
3. **信念层**：团队信念，验证是质量的保障

三个层次，从"规定"到"习惯"到"信念"，是验证文化的成熟过程。

### 24.3 验证的"最终形态"

/deploy-verify 的最终形态，不是工具，而是制度——它把"部署验证"变成团队的规定，把"五步验证"变成团队的习惯，把"0 个严重问题"变成团队的信念。

### 24.4 最后的升华

回到文章开头的问题：/deploy-verify 到底是什么？

它不是部署工具，不是 CI 流水线，不是监控系统。它是——**工程师的"上线把关人"，是团队的"质量守门员"，是工程纪律在部署层的"最后一道防线"。**

<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 240" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs><linearGradient id="bg_dv11" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient><filter id="sh_dv11"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter></defs>
  <rect width="800" height="240" fill="url(#bg_dv11)" rx="10"/>
  <text x="400" y="25" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">验证文化的三个层次</text>
  <rect x="40" y="55" width="220" height="55" rx="8" fill="#3b82f6" opacity="0.15" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_dv11)"/>
  <text x="150" y="77" text-anchor="middle" fill="#93c5fd" font-size="11" font-weight="700">制度层</text>
  <text x="150" y="95" text-anchor="middle" fill="#94a3b8" font-size="9">团队规定，部署必须验证</text>
  <rect x="290" y="55" width="220" height="55" rx="8" fill="#22c55e" opacity="0.15" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_dv11)"/>
  <text x="400" y="77" text-anchor="middle" fill="#86efac" font-size="11" font-weight="700">行为层</text>
  <text x="400" y="95" text-anchor="middle" fill="#94a3b8" font-size="9">团队习惯，部署后自动验证</text>
  <rect x="540" y="55" width="220" height="55" rx="8" fill="#f59e0b" opacity="0.15" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_dv11)"/>
  <text x="650" y="77" text-anchor="middle" fill="#fde68a" font-size="11" font-weight="700">信念层</text>
  <text x="650" y="95" text-anchor="middle" fill="#94a3b8" font-size="9">团队信念，验证是质量的保障</text>
  <text x="400" y="195" text-anchor="middle" fill="#475569" font-size="11">从规定到习惯到信念，是验证文化的成熟过程</text>
</svg>

## 二十五、部署验证的"未来"方向

### 25.1 从"部署验证"到"持续验证"

未来的方向，是从"部署验证"到"持续验证"——不只是部署时验证，运行时也持续验证。

持续验证的好处：发现"渐行偏航"的问题——部署时正常，但运行一段时间后，因为配置变化、数据变化、外部依赖变化，服务变得不正常。持续验证，能及时发现这些问题。

### 25.2 从"人工触发"到"自动触发"

未来的方向，是从"人工触发"到"自动触发"——部署完成后，自动触发验证，不需要人工干预。

自动触发的好处：减少"忘记验证"的风险——部署完成后，自动验证，自动输出报告，自动决定是否通过。

### 25.3 从"单服务"到"全链路"

未来的方向，是从"单服务验证"到"全链路验证"——不只是验证单个服务，而是验证完整的调用链。

全链路验证的好处：发现"服务间问题"——单个服务正常，但服务间调用有问题。全链路验证，能及时发现这些问题。

### 25.4 从"验证"到"预测"

未来的方向，是从"验证"到"预测"——不只是验证当前状态，而是预测未来趋势。

预测的好处：在问题发生之前，提前发现——根据历史数据，预测本次部署的风险。预测到风险，可以提前采取措施。




## 二十六、部署验证的"全景"回顾

### 26.1 从"一次验证"到"全景保障"

回顾全文，/deploy-verify 的价值，不只是"一次验证"，而是"全景保障"：

- **验证前**：确认输入（通过 CI 的构建 + change.md）
- **验证中**：五步验证（健康检查、冒烟、可观测性、回滚）
- **验证后**：输出报告（verify.md）、更新状态（verifying -> done）

这个"全景保障"，让 deploy-verify 不是"一次性的检查"，而是"完整的保障"。

### 26.2 从"单点"到"体系"

/deploy-verify 的价值，不只是"单点验证"，而是"体系保障"：

- **五步验证**：健康检查、冒烟测试、可观测性核验、回滚预案
- **五度可用**：活着、正常、可靠、可观测、可回滚
- **分级策略**：dev 快速验证、staging 完整验证、production 灰度验证

这个"体系保障"，让 deploy-verify 不是"零散的命令"，而是"完整的体系"。

### 26.3 从"工具"到"文化"

/deploy-verify 的价值，不只是"工具"，而是"文化"：

- **制度层**：部署必须验证，验证通过才能交付
- **行为层**：部署后自动验证，问题自动修复
- **信念层**：验证是质量的保障，可用是目标，可回滚是底线

这个"文化"，让 deploy-verify 不是"外挂的命令"，而是"内化的信念"。

### 26.4 从"命令"到"哲学"

/deploy-verify 的价值，最终是"哲学"：

1. **CI 绿了不等于线上可用**——验证要在真实环境做
2. **部署不是终点，验证才是**——部署成功不等于交付成功
3. **可用是目标，可回滚是底线**——失败时能恢复，才是完整保障

这三个哲学，是 /deploy-verify 的灵魂——它定义了"什么是真正的部署成功"。

## 二十七、部署验证的"尾声"：从 deploy-verify 到 diagnosing-bugs

### 27.1 两个命令的"接力"

/deploy-verify 和 /diagnosing-bugs，是流水线中的"前后接力"：

- /deploy-verify 负责"部署后验证"——确认代码在真实环境可用
- /diagnosing-bugs 负责"问题诊断"——代码出问题时，系统化定位

部署验证通过，交付完成。但如果运行中出问题，就进入诊断阶段——diagnosing-bugs 接手。

### 27.2 从"验证"到"诊断"的转变

从"验证"到"诊断"，是两种不同的思维方式：

- **验证思维**：确认"没问题"——健康检查 UP，核心链路通
- **诊断思维**：定位"有问题"——问题出在哪？根因是什么？

验证通过，不等于永远没问题。问题出现了，诊断就要开始。这是工程纪律的"两道防线"。

### 27.3 下篇预告

下一篇，我们将深入拆解 /diagnosing-bugs 命令，看它是如何通过 6 阶段诊断流程，严谨地定位和修复 Bug 的。

<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 240" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs><linearGradient id="bg_dv12" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient><filter id="sh_dv12"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter></defs>
  <rect width="800" height="240" fill="url(#bg_dv12)" rx="10"/>
  <text x="400" y="25" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">deploy-verify 与 diagnosing-bugs 的接力</text>
  <rect x="60" y="55" width="300" height="55" rx="8" fill="#22c55e" opacity="0.15" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_dv12)"/>
  <text x="210" y="77" text-anchor="middle" fill="#86efac" font-size="11" font-weight="700">/deploy-verify 部署验证</text>
  <text x="210" y="95" text-anchor="middle" fill="#94a3b8" font-size="9">确认没问题：健康检查 + 冒烟 + 回滚</text>
  <line x1="360" y1="82" x2="440" y2="82" stroke="#475569" stroke-width="2"/><polygon points="445,82 437,78 437,86" fill="#94a3b8"/>
  <rect x="450" y="55" width="300" height="55" rx="8" fill="#f59e0b" opacity="0.15" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_dv12)"/>
  <text x="600" y="77" text-anchor="middle" fill="#fde68a" font-size="11" font-weight="700">/diagnosing-bugs 问题诊断</text>
  <text x="600" y="95" text-anchor="middle" fill="#94a3b8" font-size="9">定位有问题：6 阶段诊断 + 根因分析</text>
  <text x="400" y="195" text-anchor="middle" fill="#475569" font-size="11">验证思维：确认没问题；诊断思维：定位有问题 —— 工程纪律的两道防线</text>
</svg>

---

*本文是 Harness 专栏系列命令深度拆解的第 5 篇。下一篇：[严谨诊断：/diagnosing-bugs 的 6 阶段诊断流程](./column-09-diagnosing-bugs.md)*




## 二十八、最后的思考：部署验证的"初心"

### 28.1 回到起点

我们在文章开头问了：/deploy-verify 到底是什么？

现在，我们有了答案。它不是部署工具，不是 CI 流水线，不是监控系统。它是工程师的"上线把关人"，是团队的"质量守门员"，是工程纪律在部署层的"最后一道防线"。

### 28.2 "CI 绿了不等于线上可用"的深层含义

这句话，是 deploy-verify 的核心信念。它的深层含义是：

**CI 验证的是"代码正确"，deploy-verify 验证的是"系统可用"。** 代码正确，是"静态"的——代码能编译、测试能通过、质量合规。系统可用，是"动态"的——服务能跑、链路能通、降级可靠、回滚有预案。

静态的正确，不等于动态的可用。这是 deploy-verify 存在的根本原因。

### 28.3 五步验证的"哲学"

五步验证，不只是"流程"，更是"哲学"：

- **准备环境**：先建好环境，再谈验证
- **健康检查**：先活着，再做事
- **冒烟测试**：先保核心，再保全面
- **可观测性**：先能观测，再能排障
- **回滚预案**：先有底线，再敢上线

这个"哲学"，让 deploy-verify 的验证不是"机械的流程"，而是"有深度的思考"。

### 28.4 五维度的"哲学"

五个维度，也不只是"维度"，更是"哲学"：

- **活着**：活着是基础，没有活着，一切免谈
- **正常**：正常是目标，服务活着，还要服务正常
- **可靠**：可靠是保障，正常时可靠，异常时也可靠
- **可观测**：可观测是排障，出问题能发现、能定位
- **可回滚**：可回滚是底线，出问题能恢复，才敢上线

这个"哲学"，让 deploy-verify 的维度不是"空洞的概念"，而是"可执行的检查"。

### 28.5 从 deploy-verify 到 Harness 工程纪律

/deploy-verify 是 Harness 流水线的第六步。它和前面的五个命令，共同构成了完整的工程纪律：

1. /harnessing：需求拷问，明确"做什么"
2. /coding-skill：编码实现，解决"怎么做"
3. /unit-test-write：测试补全，验证"做对了"
4. /expert-reviewer：专家评审，审查"做得好"
5. /unit-test-ci：CI 门禁，确保"质量过关"
6. /deploy-verify：部署验证，确认"上线可用"

每一步，都是工程纪律的一个环节。每一步，都是质量保障的一道防线。

### 28.6 最后的最后

我们相信，部署验证不是"成本"，而是"投资"。它投资的不是"验证的时间"，而是"上线的信心"。

当团队信任 deploy-verify，团队就敢频繁发布、快速迭代——因为知道，每次部署都有验证把关，每次验证通过都是"可上线"的信号。

这，就是 deploy-verify 的终极价值：**让发布成为习惯，让上线不再焦虑。**




## 二十九、部署验证的"实操"锦囊

### 29.1 快速开始清单

如果团队刚开始使用 deploy-verify，可以先从"最小可行验证"开始：

1. 确认健康检查端点存在且返回标准 JSON
2. 确认核心 API 可用（curl 一个关键接口）
3. 确认回滚命令可用（kubectl rollout undo / docker run）
4. 确认 verify.md 报告模板正确

这三个步骤，10 分钟可以完成。完成之后，团队就有了最基础的部署验证。

### 29.2 常见问题速查

**Q：健康检查返回 UP，但服务不可用？**
A：健康检查只验证"服务活着"，不验证"服务正常"。需要冒烟测试验证核心 API 是否可用。

**Q：冒烟测试通过，但生产环境出问题？**
A：冒烟测试只验证"关键链路"，不验证"所有功能"。生产环境出问题，需要 diagnosing-bugs 诊断。

**Q：回滚预案有，但回滚失败？**
A：回滚预案需要演练确认。deploy-verify 只验证"预案存在"，不验证"预案有效"。建议定期演练回滚。

**Q：验证报告太简略？**
A：验证报告可以扩展——增加自定义检查项、自定义验证逻辑。deploy-verify 支持可编程配置。

### 29.3 部署验证的"黄金法则"

四条黄金法则：

1. **健康检查必须快**：< 1 秒，不能依赖外部服务
2. **冒烟测试必须稳**：覆盖核心链路 + 降级链路
3. **可观测性必须全**：trace + metric + log，三件套齐全
4. **回滚预案必须实**：有命令、有触发、有演练




## 三十、结语：让验证成为习惯

### 30.1 习惯的意义

我们反复强调"让验证成为习惯"。为什么习惯如此重要？

因为"验证"不是一次性的动作，而是"持续的制度"。只有把验证变成习惯——部署后自动运行、问题自动处理、结果自动记录——验证才能真正发挥价值。

### 30.2 从"自动"到"自觉"

习惯的成熟，是从"自动"到"自觉"的过程：

- **自动**：部署完成后，自动运行 deploy-verify，输出报告
- **自觉**：团队理解验证的价值，主动运行、主动检查、主动改进

从"自动"到"自觉"，是 deploy-verify 从"工具"到"文化"的转变。

### 30.3 一句话总结

/部署验证 · deploy-verify / 的完整哲学，可以浓缩成一句话：

**"CI 绿了不等于线上可用，部署不是终点，验证才是；可用是目标，可回滚是底线。"**

这就是 /deploy-verify 的初心与终点。下一篇，我们进入 /diagnosing-bugs，看它如何用 6 阶段诊断流程，严谨地定位和修复每一个 Bug。

下一篇见！




### 30.4 最后的话

部署验证的价值，不在于"验证"本身，而在于"验证"带来的信心。当团队知道每次部署都有 deploy-verify 把关，团队就敢频繁发布、快速迭代。这，就是 /deploy-verify 的终极价值。


---|
| 服务健康 | 健康检查端点 = UP |
| 核心 API | 端到端主链路返回预期结果 |
| 降级 | 模拟外部依赖故障，确认降级生效 |

这三张表，覆盖了"服务活着"、"功能正常"、"降级可靠"三个层次。

### 4.3 冒烟测试的执行

冒烟测试的执行，是"端到端"的——它不只测一个接口，而是测一条完整链路：

```text
用户请求 -> 网关 -> 核心服务 -> 数据库 -> 返回结果
```

这条链路，模拟了真实用户的核心操作。如果链路通了，说明核心功能可用；如果链路中断，说明部署有问题。

### 4.4 降级验证的"重要性"

冒烟测试里，降级验证是最容易被忽略的。很多人只测"核心链路通"，不测"降级链路通"。但降级验证恰恰是最重要的。

为什么？因为降级是"保底线"——当外部依赖故障时，系统不能崩溃，而要降级。如果降级不可靠，外部依赖一故障，整个系统就崩溃。

### 4.5 冒烟测试的"边界"

冒烟测试也有边界——它只验证"关键链路"，不验证"所有功能"。完整的功能验证，由 CI 的测试覆盖。冒烟测试是"快速验证"，不是"全面验证"。

<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 260" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs><linearGradient id="bg_dv3" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient><filter id="sh_dv3"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter></defs>
  <rect width="800" height="260" fill="url(#bg_dv3)" rx="10"/>
  <text x="400" y="25" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">冒烟测试的三张表</text>
  <rect x="40" y="55" width="210" height="75" rx="8" fill="#3b82f6" opacity="0.15" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_dv3)"/>
  <text x="145" y="80" text-anchor="middle" fill="#93c5fd" font-size="11" font-weight="700">服务健康</text>
  <text x="145" y="100" text-anchor="middle" fill="#94a3b8" font-size="9">健康检查端点 = UP</text>
  <text x="145" y="118" text-anchor="middle" fill="#94a3b8" font-size="9">服务活着</text>
  <rect x="295" y="55" width="210" height="75" rx="8" fill="#22c55e" opacity="0.15" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_dv3)"/>
  <text x="400" y="80" text-anchor="middle" fill="#86efac" font-size="11" font-weight="700">核心 API</text>
  <text x="400" y="100" text-anchor="middle" fill="#94a3b8" font-size="9">端到端主链路返回预期</text>
  <text x="400" y="118" text-anchor="middle" fill="#94a3b8" font-size="9">功能正常</text>
  <rect x="550" y="55" width="210" height="75" rx="8" fill="#f59e0b" opacity="0.15" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_dv3)"/>
  <text x="655" y="80" text-anchor="middle" fill="#fde68a" font-size="11" font-weight="700">降级</text>
  <text x="655" y="100" text-anchor="middle" fill="#94a3b8" font-size="9">模拟外部依赖故障</text>
  <text x="655" y="118" text-anchor="middle" fill="#94a3b8" font-size="9">降级可靠</text>
  <text x="400" y="195" text-anchor="middle" fill="#475569" font-size="11">覆盖"服务活着"、"功能正常"、"降级可靠"三个层次</text>
  <text x="400" y="220" text-anchor="middle" fill="#64748b" font-size="10">降级验证最容易忽略，却是保底线的关键</text>
</svg>

## 五、Step 3：可观测性核验

### 5.1 可观测性的"哲学"

可观测性核验验证的是"服务是否能被观测"——不是"服务是否正常"，而是"服务异常时能否被发现"。

可观测性包括三件事：链路追踪（trace）、指标（metrics）、日志（logs）。这三件事，是"排障"的基础——服务出问题时，靠它们定位问题。

### 5.2 可观测性核验的三项检查

/deploy-verify 的可观测性核验，检查三项：

1. **链路追踪有完整 trace**：请求的完整调用链，从入口到出口，都有记录
2. **关键指标上报**：QPS、延迟、错误率、资源使用率，都上报到监控系统
3. **日志为结构化 JSON，无敏感信息泄露**：日志可解析、可检索、无 Token/密码/隐私

### 5.3 结构化日志的"重要性"

结构化日志，是 deploy-verify 特别强调的。为什么？

因为结构化日志（JSON）可解析、可检索、可联动。而格式化日志（文本）不可解析，只能人肉找。结构化日志，是"可观测性"的基础——没有结构化日志，链路追踪和指标上报都失去意义。

### 5.4 可观测性核验的"边界"

可观测性核验也有边界——它只验证"部署时"的可观测性，不验证"运行中"的持续可观测性。持续可观测性，由监控系统覆盖。

## 六、Step 4：回滚预案

### 6.1 回滚预案的"哲学"

回滚预案验证的是"出问题时能否快速回滚"——不是"部署成功"，而是"失败时能否恢复"。

回滚是"保底线"——部署可能失败，但失败时能快速回滚，损失就有限。没有回滚预案，部署失败就是"灾难"。

### 6.2 回滚预案的三项检查

/deploy-verify 的回滚预案，检查三项：

1. **记录上一个稳定版本 tag / 镜像**：知道"回滚到哪里"
2. **明确回滚命令与触发条件**：知道"怎么回滚"和"何时回滚"
3. **确认无破坏性数据迁移**：回滚不会导致数据丢失

### 6.3 回滚的"触发条件"

回滚的触发条件，是 deploy-verify 特别强调的。仅仅"知道怎么回滚"不够，还要"知道何时回滚"。

触发条件通常包括：健康检查 DOWN、核心链路失败、错误率飙升、延迟剧增。任何一个触发，立即回滚。

### 6.4 回滚预案的"边界"

回滚预案也有边界——它只验证"有预案"，不验证"预案有效"。预案的有效性，需要演练确认。deploy-verify 的职责是"确认有预案"，演练是团队的持续工作。

<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 260" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs><linearGradient id="bg_dv4" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient><filter id="sh_dv4"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter></defs>
  <rect width="800" height="260" fill="url(#bg_dv4)" rx="10"/>
  <text x="400" y="25" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">回滚预案的三项检查</text>
  <rect x="40" y="55" width="220" height="75" rx="8" fill="#3b82f6" opacity="0.15" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_dv4)"/>
  <text x="150" y="80" text-anchor="middle" fill="#93c5fd" font-size="11" font-weight="700">回滚到哪</text>
  <text x="150" y="100" text-anchor="middle" fill="#94a3b8" font-size="9">上一稳定版本 tag / 镜像</text>
  <text x="150" y="118" text-anchor="middle" fill="#94a3b8" font-size="9">记录版本</text>
  <rect x="290" y="55" width="220" height="75" rx="8" fill="#22c55e" opacity="0.15" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_dv4)"/>
  <text x="400" y="80" text-anchor="middle" fill="#86efac" font-size="11" font-weight="700">怎么回滚</text>
  <text x="400" y="100" text-anchor="middle" fill="#94a3b8" font-size="9">明确回滚命令与触发条件</text>
  <text x="400" y="118" text-anchor="middle" fill="#94a3b8" font-size="9">命令 + 触发</text>
  <rect x="540" y="55" width="220" height="75" rx="8" fill="#f59e0b" opacity="0.15" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_dv4)"/>
  <text x="650" y="80" text-anchor="middle" fill="#fde68a" font-size="11" font-weight="700">回滚安全</text>
  <text x="650" y="100" text-anchor="middle" fill="#94a3b8" font-size="9">无破坏性数据迁移</text>
  <text x="650" y="118" text-anchor="middle" fill="#94a3b8" font-size="9">数据安全</text>
  <text x="400" y="195" text-anchor="middle" fill="#475569" font-size="11">部署可能失败，但失败时能快速回滚，损失就有限</text>
  <text x="400" y="220" text-anchor="middle" fill="#64748b" font-size="10">回滚是"保底线"，没有预案的部署是灾难</text>
</svg>

### 6.5 回滚的"代码"视角

/deploy-verify 的回滚，最终落地到具体的命令和触发条件：

```bash
# 回滚命令示例
kubectl rollout undo deployment/my-service   # 回滚到上一版本
# 或
docker run -d --name my-service-v1 my-image:v1  # 回滚到 v1 镜像

# 触发条件示例
# 1. 健康检查 DOWN 持续 5 分钟
# 2. 核心 API 错误率 > 5%
# 3. 端到端主链路失败
# 4. P99 延迟 > 3 秒
```

这个代码示例，展示了回滚预案的具体落地——回滚命令、触发条件、数据安全，三件事都明确。

