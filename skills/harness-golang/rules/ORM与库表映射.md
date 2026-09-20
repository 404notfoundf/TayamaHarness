# ORM 与库表映射（Go）

> **库表列命名**: `.harness/rules/数据库命名规范.md` §1–3。

- 结构体导出字段 PascalCase，库列用 struct tag：`db:"created_at"`、`json:"createdAt"` 按 API 约定区分。
- GORM：默认 snake 列名；自定义用 `column:` tag。
- 对照登记在 `.harness/wiki/数据模型.md`「ORM 映射」表。
