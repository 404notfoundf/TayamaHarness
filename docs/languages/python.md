# Python 语言规范包

## 概述

Harness Python 语言规范包为 Python 项目提供完整的开发规范体系，基于 PEP 8 和 Google Python Style Guide。

## 基线

| 维度 | 选型 |
|------|------|
| 基线框架 | Python 3.11+ / Flask / FastAPI |
| 构建工具 | pip + virtualenv / Poetry |
| 测试框架 | pytest + pytest-mock |
| 覆盖率工具 | pytest-cov（核心逻辑覆盖率 ≥80%） |
| 代码规范 | flake8 + mypy + black + isort |
| 架构约束 | 自定义 import-lint 检查 |
| 安全扫描 | bandit + safety |
| 数据库 | SQLAlchemy + Alembic |

## 规则

| 规则 | 来源 |
|------|------|
| 编码规范 | `harness-python/rules/`（语言特有） |
| 工程结构 | `harness-python/rules/`（语言特有） |
| SDD-TDD 模式 | `harness-core/rules/`（跨语言通用） |
| 开发流程规范 | `harness-core/rules/`（跨语言通用） |
| 运行时可靠性 | `harness-core/rules/`（跨语言通用） |

### 编码规范要点

- 遵循 PEP 8 + Google Python Style Guide
- 类名：PascalCase
- 函数/方法：snake_case
- 常量：UPPER_SNAKE_CASE
- 文件 ≤500 行
- 函数 ≤30 行
- 类型注解：所有函数必须包含类型注解

### 工程结构要点

- 分层架构（`api` / `service` / `repository` / `model`）
- `api` 层只做请求解析和响应序列化
- `service` 层承担业务逻辑
- 依赖注入使用 FastAPI 的 `Depends` 或自定义容器
- 配置管理使用 `pydantic-settings`

## 技能

9 个技能（6 流水线 + 3 辅助），与语言无关的通用技能直接复用 `harness-core` 的模板。

## 适用项目

- FastAPI / Flask Web 服务
- 数据处理和 ETL 管道
- Python 3.11+ 项目