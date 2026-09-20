# ORM 与库表映射（Python）

> **库表列命名**: `.harness/rules/数据库命名规范.md` §1–3。  
> **应用层**: 见 `编码规范.md`（snake_case 为主）。

- SQLAlchemy / Django ORM：模型属性名建议与库列一致（snake_case），或通过 `Column('created_at')` / `db_column` 显式映射。
- 定稿对照写在 `.harness/wiki/数据模型.md`「ORM 映射」表。
