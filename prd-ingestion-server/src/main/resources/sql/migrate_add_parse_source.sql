-- =============================================================================
-- 迁移：为 prd_ingestion 表增加 parse_source 列（幂等）
-- 适用：mysql 5.7+ / 8.0+，已执行 schema.sql 旧版的存量数据库
-- 执行方式：mysql -u<user> -p < harness_prd_ingestion < migrate_add_parse_source.sql
-- =============================================================================

USE harness_prd_ingestion;

DROP PROCEDURE IF EXISTS add_parse_source_if_not_exists;
DELIMITER //
CREATE PROCEDURE add_parse_source_if_not_exists()
BEGIN
    IF NOT EXISTS (
        SELECT * FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'prd_ingestion'
        AND COLUMN_NAME = 'parse_source'
    ) THEN
        ALTER TABLE prd_ingestion
            ADD COLUMN parse_source VARCHAR(32) NOT NULL DEFAULT 'keyword_fallback'
            COMMENT '解析来源（template/llm/keyword_fallback）'
            AFTER progress_msg;
    END IF;
END//
DELIMITER ;
CALL add_parse_source_if_not_exists();
DROP PROCEDURE IF EXISTS add_parse_source_if_not_exists;

-- 验证
SELECT column_name, column_type, column_default
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'prd_ingestion'
  AND COLUMN_NAME = 'parse_source';