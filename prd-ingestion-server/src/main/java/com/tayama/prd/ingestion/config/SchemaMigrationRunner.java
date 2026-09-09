package com.tayama.prd.ingestion.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 启动时执行的幂等数据库迁移。
 *
 * <p>背景：schema.sql 中曾用 MySQL 存储过程 + DELIMITER 做旧表加列迁移，
 * 但 Spring 的 ScriptUtils 不支持 DELIMITER 语法，这些语句在启动时必然失败
 * （被 continue-on-error 吞掉），导致旧库升级后缺失列、新代码读写报错。
 * 迁移统一在 Java 侧启动时执行：先查 information_schema.COLUMNS，
 * 列不存在才 ALTER TABLE ADD COLUMN（与 schema.sql 中的定义保持一致）。</p>
 */
@Component
public class SchemaMigrationRunner implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(SchemaMigrationRunner.class);

    private final JdbcTemplate jdbc;

    public SchemaMigrationRunner(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void run(ApplicationArguments args) {
        Map<String, String> migrations = new LinkedHashMap<>();
        // 与 schema.sql 中 prd_ingestion 表定义保持一致
        migrations.put("parse_source",
                "ALTER TABLE prd_ingestion ADD COLUMN parse_source VARCHAR(32) NOT NULL DEFAULT 'keyword_fallback' COMMENT '解析来源（template/llm/keyword_fallback）' AFTER progress_msg");
        migrations.put("source_paragraph_entity",
                "ALTER TABLE prd_data_entity ADD COLUMN source_paragraph TEXT NULL COMMENT '来源 PRD 段落' AFTER description");
        migrations.put("source_paragraph_interface",
                "ALTER TABLE prd_interface ADD COLUMN source_paragraph TEXT NULL COMMENT '来源 PRD 段落' AFTER notes");
        migrations.put("source_paragraph_ad",
                "ALTER TABLE prd_architecture_decision ADD COLUMN source_paragraph TEXT NULL COMMENT '来源 PRD 段落' AFTER status");
        migrations.put("candidate_status_requirement",
                "ALTER TABLE prd_requirement ADD COLUMN candidate_status VARCHAR(16) NOT NULL DEFAULT 'confirmed' COMMENT '候选状态（confirmed=原文提取/proposed=规则推导待确认）' AFTER notes");
        migrations.put("candidate_status_entity",
                "ALTER TABLE prd_data_entity ADD COLUMN candidate_status VARCHAR(16) NOT NULL DEFAULT 'confirmed' COMMENT '候选状态（confirmed=原文提取/proposed=规则推导待确认）' AFTER source_paragraph");
        migrations.put("candidate_status_interface",
                "ALTER TABLE prd_interface ADD COLUMN candidate_status VARCHAR(16) NOT NULL DEFAULT 'confirmed' COMMENT '候选状态（confirmed=原文提取/proposed=规则推导待确认）' AFTER source_paragraph");

        apply("prd_ingestion", "parse_source", migrations.get("parse_source"));
        apply("prd_data_entity", "source_paragraph", migrations.get("source_paragraph_entity"));
        apply("prd_interface", "source_paragraph", migrations.get("source_paragraph_interface"));
        apply("prd_architecture_decision", "source_paragraph", migrations.get("source_paragraph_ad"));
        apply("prd_requirement", "candidate_status", migrations.get("candidate_status_requirement"));
        apply("prd_data_entity", "candidate_status", migrations.get("candidate_status_entity"));
        apply("prd_interface", "candidate_status", migrations.get("candidate_status_interface"));
    }

    /**
     * 幂等迁移：information_schema 中不存在目标列时才执行 ALTER TABLE。
     */
    private void apply(String table, String column, String alterSql) {
        try {
            List<Integer> exists = jdbc.query(
                    "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?",
                    (rs, row) -> rs.getInt(1), table, column);
            boolean columnExists = !exists.isEmpty() && exists.get(0) > 0;
            if (columnExists) {
                LOG.info("[migration] 列已存在，跳过: {}.{}", table, column);
                return;
            }
            jdbc.execute(alterSql);
            LOG.info("[migration] 迁移完成: {}", alterSql);
        } catch (Exception e) {
            // 表不存在（首次建库前）或权限不足时不应阻断启动，仅记录日志
            LOG.warn("[migration] 迁移跳过（表不存在或执行失败）: {}.{}, error={}", table, column, e.getMessage());
        }
    }
}