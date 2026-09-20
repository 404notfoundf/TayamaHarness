# ORM 与库表映射（Rust）

> **库表列命名**: `.harness/rules/数据库命名规范.md` §1–3。

- SQLx / Diesel：Rust 字段 snake_case 常见，与库列一致或通过 `#[sqlx(rename = "created_at")]` 等映射。
- 对照登记在 `.harness/wiki/数据模型.md`「ORM 映射」表。
