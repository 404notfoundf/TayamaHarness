package com.tayama.prd.ingestion.repository;

import com.tayama.prd.ingestion.model.template.Template;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

/**
 * 模板数据访问层。
 */
@Repository
public class TemplateRepository {

    private final JdbcTemplate jdbc;

    public TemplateRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Template> findAll() {
        return jdbc.query(
                "SELECT template_id, type, name, description, content, version, updated_by, updated_at FROM prd_template ORDER BY type, name",
                (rs, row) -> mapTemplate(rs));
    }

    /**
     * 按文档类型读取拆分用的模板内容：项目级模板优先，全局模板兜底。
     *
     * <p>PRD 拆分（生成 4 类文档）必须从数据库读取模板，使模板管理真正生效：
     * 管理员在模板管理页编辑的内容决定拆分产物的章节骨架。</p>
     *
     * @param projectId 项目 ID（区别于全局模板）
     * @param type      文档类型（business-model/data-model/interface-protocol/architecture-decision）
     * @return 模板内容；无任何可用模板时返回 null
     */
    public String findContentByType(String projectId, String type) {
        // 1. 项目级模板优先（按最新更新取第一条）
        List<String> projectTemplates = jdbc.query(
                "SELECT content FROM prd_project_template WHERE project_id=? AND type=? ORDER BY updated_at DESC, template_id DESC LIMIT 1",
                (rs, row) -> rs.getString("content"), projectId, type);
        if (!projectTemplates.isEmpty() && projectTemplates.get(0) != null && !projectTemplates.get(0).isBlank()) {
            return projectTemplates.get(0);
        }
        // 2. 全局模板兜底（按最新更新取第一条）
        List<String> globalTemplates = jdbc.query(
                "SELECT content FROM prd_template WHERE type=? ORDER BY updated_at DESC, template_id DESC LIMIT 1",
                (rs, row) -> rs.getString("content"), type);
        if (!globalTemplates.isEmpty() && globalTemplates.get(0) != null && !globalTemplates.get(0).isBlank()) {
            return globalTemplates.get(0);
        }
        return null;
    }

    public Template findById(String templateId) {
        List<Template> results = jdbc.query(
                "SELECT template_id, type, name, description, content, version, updated_by, updated_at FROM prd_template WHERE template_id=?",
                (rs, row) -> mapTemplate(rs), templateId);
        return results.isEmpty() ? null : results.get(0);
    }

    public void insert(String templateId, String type, String name, String description, String content, String updatedBy) {
        jdbc.update("INSERT INTO prd_template (template_id, type, name, description, content, version, updated_by) VALUES (?,?,?,?,?,1,?)",
                templateId, type, name, description, content, updatedBy);
    }

    public void update(String templateId, String content, String changeLog, String updatedBy) {
        // 保存版本历史
        jdbc.update("INSERT INTO prd_template_version (template_id, version, content, change_log, updated_by) " +
                        "SELECT template_id, version, content, ?, ? FROM prd_template WHERE template_id=?",
                changeLog, updatedBy, templateId);
        // 更新模板
        jdbc.update("UPDATE prd_template SET content=?, version=version+1, updated_by=? WHERE template_id=?",
                content, updatedBy, templateId);
    }

    public List<Template.TemplateVersion> findVersions(String templateId) {
        return jdbc.query(
                "SELECT version, content, change_log, updated_by, created_at FROM prd_template_version WHERE template_id=? ORDER BY version DESC",
                (rs, row) -> {
                    Template.TemplateVersion v = new Template.TemplateVersion();
                    v.setVersion(rs.getInt("version"));
                    v.setContent(rs.getString("content"));
                    v.setChangeLog(rs.getString("change_log"));
                    v.setUpdatedBy(rs.getString("updated_by"));
                    Timestamp ts = rs.getTimestamp("created_at");
                    v.setUpdatedAt(ts != null ? ts.toInstant().toString() : null);
                    return v;
                }, templateId);
    }

    private Template mapTemplate(java.sql.ResultSet rs) throws java.sql.SQLException {
        Template t = new Template();
        t.setTemplateId(rs.getString("template_id"));
        t.setType(rs.getString("type"));
        t.setName(rs.getString("name"));
        t.setDescription(rs.getString("description"));
        t.setContent(rs.getString("content"));
        t.setVersion(rs.getInt("version"));
        t.setUpdatedBy(rs.getString("updated_by"));
        Timestamp ts = rs.getTimestamp("updated_at");
        t.setUpdatedAt(ts != null ? ts.toInstant().toString() : null);
        return t;
    }
}