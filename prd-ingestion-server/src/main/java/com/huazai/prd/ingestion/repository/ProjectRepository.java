package com.huazai.prd.ingestion.repository;

import com.huazai.prd.ingestion.model.project.Project;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 项目数据访问层。
 */
@Repository
public class ProjectRepository {

    private final JdbcTemplate jdbc;

    private final RowMapper<Project> mapper = (rs, row) -> {
        Project p = new Project();
        p.setId(rs.getLong("id"));
        p.setProjectId(rs.getString("project_id"));
        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setLanguageId(rs.getLong("language_id"));
        p.setLanguageName(rs.getString("language_name"));
        p.setStatus(rs.getString("status"));
        p.setCreatedAt(rs.getString("created_at"));
        p.setUpdatedAt(rs.getString("updated_at"));
        return p;
    };

    public ProjectRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** 查询所有项目（含框架列表） */
    public List<Project> findAll() {
        List<Project> list = jdbc.query("""
            SELECT p.*, l.name AS language_name
            FROM prd_project p
            LEFT JOIN prd_language l ON p.language_id = l.id
            WHERE p.status = 'active'
            ORDER BY p.created_at DESC
            """, mapper);
        for (Project p : list) {
            loadFrameworks(p);
        }
        return list;
    }

    /** 按 ID 查询（含框架列表） */
    public Project findByProjectId(String projectId) {
        List<Project> list = jdbc.query("""
            SELECT p.*, l.name AS language_name
            FROM prd_project p
            LEFT JOIN prd_language l ON p.language_id = l.id
            WHERE p.project_id = ?
            """, mapper, projectId);
        if (list.isEmpty()) return null;
        Project p = list.get(0);
        loadFrameworks(p);
        return p;
    }

    /** 加载项目的框架列表 */
    private void loadFrameworks(Project p) {
        List<Long> fids = jdbc.queryForList(
            "SELECT framework_id FROM prd_project_framework WHERE project_id = ? ORDER BY framework_id",
            Long.class, p.getProjectId());
        p.setFrameworkIds(fids);

        if (!fids.isEmpty()) {
            String placeholders = String.join(",", fids.stream().map(v -> "?").toArray(String[]::new));
            // 参数需要重复传入：IN 子句用一次，FIELD 子句用一次
            Object[] params = new Object[fids.size() * 2];
            for (int i = 0; i < fids.size(); i++) {
                params[i] = fids.get(i);
                params[i + fids.size()] = fids.get(i);
            }
            List<String> fnames = jdbc.queryForList(
                "SELECT name FROM prd_framework WHERE id IN (" + placeholders + ") ORDER BY FIELD(id," + placeholders + ")",
                String.class, params);
            p.setFrameworkNames(fnames);
        } else {
            p.setFrameworkNames(List.of());
        }
    }

    /** 插入项目 */
    public void insert(String projectId, String name, String description, Long languageId) {
        jdbc.update("""
            INSERT INTO prd_project (project_id, name, description, language_id, status)
            VALUES (?,?,?,?, 'active')
            """, projectId, name, description, languageId);
    }

    /** 保存项目-框架关联 */
    public void saveProjectFrameworks(String projectId, List<Long> frameworkIds) {
        jdbc.update("DELETE FROM prd_project_framework WHERE project_id = ?", projectId);
        if (frameworkIds != null && !frameworkIds.isEmpty()) {
            for (Long fid : frameworkIds) {
                jdbc.update("INSERT INTO prd_project_framework (project_id, framework_id) VALUES (?,?)",
                        projectId, fid);
            }
        }
    }

    /** 更新项目基本信息 */
    public void update(String projectId, String name, String description, Long languageId, String status) {
        jdbc.update("""
            UPDATE prd_project SET name=?, description=?, language_id=?, status=?
            WHERE project_id=?
            """, name, description, languageId, status, projectId);
    }

    /** 删除项目（归档） */
    public void archive(String projectId) {
        jdbc.update("UPDATE prd_project SET status='archived' WHERE project_id=?", projectId);
    }

    /** 生成项目 ID */
    public String generateProjectId() {
        String ts = new java.text.SimpleDateFormat("yyyyMMddHHmmss").format(new java.util.Date());
        int rand = (int)(Math.random() * 9000) + 1000;
        return "proj-" + ts + "-" + rand;
    }
}