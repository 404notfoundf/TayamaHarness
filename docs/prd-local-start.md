# Harness Flow 本地启动（front + server）

本文记录 **`prd-ingestion-server` + `prd-ingestion-front`** 的本地启动方式。这是网页产品：浏览器操作 PRD 解析、校验、看板。页面流水线**不会**调用 Cursor 里的 `/coding-skill` 等技能。

页面操作见 [使用教程](prd-usage-tutorial.md)。

## 依赖

| 软件 | 要求 |
|------|------|
| JDK | 21 |
| Maven | 3.9+ |
| MySQL | 8.0，本机 `3306` |
| Node.js | 18+ |

MinIO、LLM **可选**。没有 MinIO 就关对象存储、用粘贴 Markdown；没有 API Key 就关 AI，仍走模板 + 章节算法。

## 1. 建库

```sql
CREATE DATABASE IF NOT EXISTS harness_prd_ingestion DEFAULT CHARSET utf8mb4;
```

## 2. 启动后端

默认 profile 为 `dev`（`SPRING_PROFILES_ACTIVE`，未设时即 `dev`）。数据源来自环境变量，未设则用 yaml 冒号后的默认值：

| 变量 | 未设置时 | 说明 |
|------|----------|------|
| `DB_HOST` | `127.0.0.1` | |
| `DB_PORT` | `3306` | |
| `DB_NAME` | `harness_prd_ingestion` | |
| `DB_USERNAME` | `root` | |
| `DB_PASSWORD` | 见 `application-dev.yml` 的 `${DB_PASSWORD:…}` | **必须与本机 MySQL 一致**，不一致就设环境变量 |
| `SERVER_PORT` | `8080` | |
| `MINIO_ENABLED` | yaml 默认为 `true` | 本地无 MinIO 时设为 `false` |
| `AI_ENABLED` | `false` | |

Spring Boot **不会**自动读取 `prd-ingestion-server/.env.example`。变量要注入 **启动 Java 的那个进程**。

**PowerShell**（与 `mvn` 同一窗口；关掉窗口后 `$env` 失效）：

```powershell
cd d:\go\working\my\study\harness-skills\tayama-harness-skills\prd-ingestion-server

$env:DB_PASSWORD = "你的MySQL密码"
$env:MINIO_ENABLED = "false"
$env:AI_ENABLED = "false"

mvn spring-boot:run
```

账号不是 `root` 时再设 `$env:DB_USERNAME = "实际用户"`。

也可打包后启动：

```powershell
mvn clean package -DskipTests
java -jar target\prd-ingestion-server-*.jar
```

健康检查：<http://localhost:8080/api/v1/actuator/health> 应返回 `UP`。首次启动会执行 `schema.sql` 建表（幂等）。

CMD 写法：`set DB_PASSWORD=你的密码` 后再 `mvn spring-boot:run`。IDE 运行则在 Run Configuration 的 Environment 里加同样的键。

## 3. 启动前端

另开一个终端。当前 `prd-ingestion-front/src/config/index.ts` 中 **`useMock = false`**，必须连真实后端。

```powershell
cd d:\go\working\my\study\harness-skills\tayama-harness-skills\prd-ingestion-front
npm install
npm run dev
```

- 开发页：**http://localhost:5174**（`vite.config.ts` 端口为 5174，不是 5173）
- `/api` 代理到 `http://localhost:8080`

可选：复制 `prd-ingestion-front/.env.example` 为 `.env`，按需改 `VITE_API_TIMEOUT`（PRD 同步解析较慢，默认 180 秒）。

## 4. 验证与页面流程

1. 打开 <http://localhost:5174>，应到登录页。
2. 注册第一个用户即为管理员。
3. 创建项目 → 进入项目 → **PRD 导入**（粘贴 Markdown）→ 解析完成后到 **校验面板**。
4. 四类文档确认后 **生成 Change** → **需求看板** / **流水线** 里查看或点「推进到下一阶段」（`drafting → reviewing → approved → completed`）。

要把 Change 拿去写代码：在流水线页复制 `change.md`，贴进业务仓库的 `.harness/changes/`，再到 Cursor 使用技能包。

## 5. 常见问题

| 现象 | 处理 |
|------|------|
| 前端能开但登录/导入失败 | 后端未起，或 `useMock` 为 false 却连不上 8080 |
| 后端报 Access denied | `$env:DB_PASSWORD` 与 MySQL 不一致；`dev` 配置会覆盖 `application.yml` 的数据源 |
| 上传文件失败 | 未起 MinIO：设 `MINIO_ENABLED=false`，改用粘贴文本 |
| 解析很浅 | 未开 LLM：设 `AI_ENABLED=true` 并配置 `AI_API_KEY` / `AI_API_URL` |
