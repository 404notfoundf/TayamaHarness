---
name: harness-java
description: Java 语言规范包 — 编码规范、工程结构（SDD-TDD/开发流程/运行时可靠性共享自 harness-core）
---

# Harness Java — Java 语言规范包

本包为 Java 项目提供完整的 Harness 开发规范体系，基于：

- **基线框架**: Spring Boot / Spring MVC / Quarkus / Micronaut / Vert.x / Dropwizard / Dubbo / Spring Cloud Alibaba / Spring AI / Spring AI Alibaba / AgentScope Java / LangChain4j / Semantic Kernel / Genkit Java（JDK 21 LTS）
- **构建工具**: Maven 3.9+ / Gradle 8+
- **测试框架**: JUnit 5 + Mockito + AssertJ
- **代码规范**: 阿里巴巴 Java 开发手册 + Checkstyle + PMD + SpotBugs
- **架构约束**: ArchUnit

包含 rules（2 个语言特有 + 3 个通用来自 harness-core）和 skills（14 个），与 `apply-harness` 入口技能配合使用。

### 流水线技能（9 个）
| 技能 | 阶段 | 说明 |
|------|------|------|
| `harnessing` | ① 需求打磨 | 需求拷问引擎，打磨方案 |
| `coding-skill` | ② 编码实现 | 按需求卡/规范实现可编译代码 |
| `unit-test-write` | ③ 单元测试 | 核心逻辑覆盖率 ≥80% |
| `expert-reviewer` | ④ 专家评审 | 双轴审查，0 严重问题放行 |
| `unit-test-ci` | ⑤ CI 门禁 | 静态分析+竞态检测+全量测试 |
| `deploy-verify` | ⑥ 部署验证 | 冒烟/健康检查/关键链路验证 |
| `arch-review` | 非流水线 | 架构体检，发现摩擦点 |
| `diagnosing-bugs` | 辅助 | Bug 诊断，稳定复现后分析 |
| `handoff` | 辅助 | 上下文压缩，交接文档 |

### 领域专家技能（4 个，由 expert-reviewer 混合加载，也支持独立 `/` 命令）
| 技能 | 说明 |
|------|------|
| `java-code-review` | Java 并发编程工具封装（线程池工厂/锁/重试/限流/异步） |
| `spring-api-convention` | Spring API 规范封装（统一响应/异常处理/幂等/分页/traceId） |
| `mybatis-toolkit` | MyBatis 工具封装（分页/乐观锁/逻辑删除/自动填充/数据权限/批量） |
| `openfeign-toolkit` | OpenFeign 工具封装（超时/错误解码器/重试/拦截器/traceId/熔断） |

### 跨语言技能（来自 harness-core，全语言共享）
| 技能 | 说明 |
|------|------|
| `redis-cache-wrapper` | 多级缓存封装（穿透/击穿/雪崩/分布式锁/热 key） |
| `database-migration-toolkit` | 数据库迁移工具封装（迁移/回滚/数据回填） |
| `kafka-toolkit` | Kafka 工具封装（消费者/DLQ/生产者/Lag 监控） |
| `k8s-release-toolkit` | K8s 发布工具封装（Deployment/HPA/探针/灰度/回滚） |
| `performance-toolkit` | 性能诊断工具封装（火焰图/GC/慢SQL/线程 dump） |
| `security-toolkit` | 安全工具封装（脱敏/加密/输入校验/鉴权/日志脱敏） |
| `rocketmq-toolkit` | RocketMQ 工具封装（事务消息/顺序消息/延迟消息/DLQ） |
| `http-client-toolkit` | HTTP 客户端工具封装（连接池/超时/重试/熔断/traceId） |
| `logging-toolkit` | 日志工具封装（traceId/MDC/脱敏/动态级别/文件策略） |
| `scheduler-toolkit` | 分布式调度工具封装（分布式锁/幂等/重试/补偿/手动触发） |
| `oss-toolkit` | 对象存储工具封装（统一接口/分片上传/预签名/图片处理/CDN） |
| `excel-toolkit` | Excel 工具封装（模板导出/大数据量分批/导入校验/动态列） |
| `eventbus-toolkit` | 事件总线工具封装（同步/异步/事务事件/事件追踪） |

> **辅助技能**: `/harness-me`（需求打磨）、`domain-modeling`（领域语言维护）、`research`（外部事实查证）、`resolving-merge-conflicts`（合并冲突解决）