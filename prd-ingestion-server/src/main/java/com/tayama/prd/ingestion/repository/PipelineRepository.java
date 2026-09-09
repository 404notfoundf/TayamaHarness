package com.tayama.prd.ingestion.repository;

import com.tayama.prd.ingestion.model.pipeline.ChangeLogEntry;
import com.tayama.prd.ingestion.model.pipeline.PipelineStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

/**
 * 流水线数据访问层。
 */
@Repository
public class PipelineRepository {

    private final JdbcTemplate jdbc;

    public PipelineRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ---- Pipeline Stages ----
    public void initStages(String changeId, String projectId) {
        String[] stages = {"drafting", "reviewing", "approved", "completed"};
        for (String stage : stages) {
            jdbc.update("INSERT IGNORE INTO prd_pipeline_stage (change_id, stage, status, project_id) VALUES (?,?,?,?)",
                    changeId, stage, "pending", projectId);
        }
        // 第一阶段设为 active
        jdbc.update("UPDATE prd_pipeline_stage SET status='active', started_at=NOW() WHERE change_id=? AND stage='drafting'",
                changeId);
    }

    public List<PipelineStatus.StageInfo> findStages(String changeId) {
        return jdbc.query(
                "SELECT stage, status, started_at, completed_at FROM prd_pipeline_stage WHERE change_id=? ORDER BY FIELD(stage, 'drafting','reviewing','approved','completed')",
                (rs, row) -> {
                    PipelineStatus.StageInfo s = new PipelineStatus.StageInfo();
                    s.setStage(rs.getString("stage"));
                    s.setStatus(rs.getString("status"));
                    Timestamp started = rs.getTimestamp("started_at");
                    if (started != null) s.setStartedAt(started.toInstant().toString());
                    Timestamp completed = rs.getTimestamp("completed_at");
                    if (completed != null) s.setCompletedAt(completed.toInstant().toString());
                    return s;
                }, changeId);
    }

    public void advanceStage(String changeId, String stage) {
        // 完成当前活跃阶段
        jdbc.update("UPDATE prd_pipeline_stage SET status='completed', completed_at=NOW() WHERE change_id=? AND status='active'",
                changeId);
        // 激活目标阶段
        jdbc.update("UPDATE prd_pipeline_stage SET status='active', started_at=NOW() WHERE change_id=? AND stage=?",
                changeId, stage);
    }

    // ---- Change Logs ----
    public void insertLog(String changeId, String type, String message, String detail, String actor, String projectId) {
        if (projectId == null || projectId.isBlank()) {
            // 防御：X-Project-Id 请求头缺失时从 Change 记录反查，避免 project_id 非空约束报错
            List<String> ids = jdbc.query("SELECT project_id FROM prd_change WHERE change_id=?",
                    (rs, row) -> rs.getString(1), changeId);
            projectId = ids.isEmpty() ? null : ids.get(0);
        }
        jdbc.update("INSERT INTO prd_change_log (change_id, log_type, message, detail, actor, project_id) VALUES (?,?,?,?,?,?)",
                changeId, type, message, detail, actor, projectId);
    }

    public List<ChangeLogEntry> findLogs(String changeId) {
        return jdbc.query(
                "SELECT id, log_type, message, detail, actor, created_at FROM prd_change_log WHERE change_id=? ORDER BY created_at DESC",
                (rs, row) -> {
                    ChangeLogEntry e = new ChangeLogEntry();
                    e.setId(String.valueOf(rs.getLong("id")));
                    e.setType(rs.getString("log_type"));
                    e.setMessage(rs.getString("message"));
                    e.setDetail(rs.getString("detail"));
                    e.setActor(rs.getString("actor"));
                    e.setCreatedAt(rs.getTimestamp("created_at").toInstant().toString());
                    return e;
                }, changeId);
    }

    // ---- Kanban Stats ----
    public int countByStatus(String status, String projectId) {
        if (projectId != null) {
            Integer count = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM prd_change WHERE status=? AND project_id=?", Integer.class, status, projectId);
            return count != null ? count : 0;
        }
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM prd_change WHERE status=?", Integer.class, status);
        return count != null ? count : 0;
    }
}