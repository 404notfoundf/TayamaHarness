# ORM 与库表映射（Java）

> **作用域**: Java 实体、MyBatis / MyBatis-Plus 与关系型库列的对应关系。  
> **库表列命名**: 以 `.harness/rules/数据库命名规范.md` §1–3 为准。  
> **状态**: 强制 · 与 `编码规范.md` 一并由 Checkstyle/PMD 与代码评审对照

---

## 1. 基本原则

- **库列**：snake_case（`sku_id`、`created_at`）；禁止在 DDL/迁移中使用 camelCase 列名。
- **实体字段**：lowerCamelCase（`skuId`、`createdAt`），与 `编码规范.md` 一致。布尔实体字段**不加** `is` 前缀（`deleted` 而非 `isDeleted`）；库列若为 `is_deleted` 用 `@TableField("is_deleted")`。
- MyBatis-Plus 默认**下划线转驼峰**；非常规列用 `@TableField("column_name")` 显式映射。
- 全项目选定一种时间字段实体命名，并与自动填充、逻辑删除插件配置**一致**（见 §2）。

---

## 2. 推荐对照（与 `数据库命名规范.md` 库列一致）

| 库列 | Java 实体字段（本项目默认） |
|------|----------------------------|
| `created_at` | `createdAt` |
| `updated_at` | `updatedAt` |
| `deleted` | `deleted` |
| `deleted_at` | `deletedAt`（若使用） |

- `@TableLogic`、`MetaObjectHandler` / 自动填充插件、`mybatis-toolkit` 的填充字段须使用上表实体侧名称。
- 遗留项目若已统一为 `createTime`/`updateTime`，在 wiki「命名约定」声明后全库沿用，**禁止混用** `createdAt` 与 `createTime`。

---

## 3. 与 wiki、编码

- 新表/新列：在 `.harness/wiki/数据模型.md` 的 ORM 映射表登记库列与实体字段。
- `/coding-skill` 写 Mapper、Wrapper、update 字段时，以 wiki + 实体为准，禁止仅照抄 change.md 未验证的字段名。
