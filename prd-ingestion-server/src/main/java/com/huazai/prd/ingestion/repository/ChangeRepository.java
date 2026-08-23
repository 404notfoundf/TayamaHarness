package com.huazai.prd.ingestion.repository;

import com.huazai.prd.ingestion.model.change.ChangeDetail;
import com.huazai.prd.ingestion.model.change.ChangeSummary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Change 数据访问层。
 */
@Repository
public class ChangeRepository {

    private final JdbcTemplate jdbc;

    public ChangeRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void insertChange(String changeId, String title, String status, String content, String ingestionId,
                              String requirementIds, String dependencies, String projectId) {
        jdbc.update("INSERT INTO prd_change (change_id, title, status, content, ingestion_id, requirement_ids, dependencies, project_id) VALUES (?,?,?,?,?,?,?,?)",
                changeId, title, status, content, ingestionId, requirementIds, dependencies, projectId);
    }

    public void updateChangeStatus(String changeId, String status) {
        jdbc.update("UPDATE prd_change SET status=? WHERE change_id=?", status, changeId);
    }

    public void updateChangeContent(String changeId, String content) {
        jdbc.update("UPDATE prd_change SET content=? WHERE change_id=?", content, changeId);
    }

    public List<ChangeSummary> findChanges(int page, int size, String projectId) {
        int offset = page * size;
        if (projectId != null) {
            return jdbc.query(
                    "SELECT change_id, title, status, requirement_ids, created_at, updated_at FROM prd_change WHERE project_id=? ORDER BY created_at DESC LIMIT ? OFFSET ?",
                    (rs, row) -> {
                        ChangeSummary s = new ChangeSummary();
                        s.setChangeId(rs.getString("change_id"));
                        s.setTitle(rs.getString("title"));
                        s.setStatus(rs.getString("status"));
                        s.setCreatedAt(rs.getTimestamp("created_at").toInstant().toString());
                        s.setUpdatedAt(rs.getTimestamp("updated_at").toInstant().toString());
                        return s;
                    }, projectId, size, offset);
        }
        return jdbc.query(
                "SELECT change_id, title, status, requirement_ids, created_at, updated_at FROM prd_change ORDER BY created_at DESC LIMIT ? OFFSET ?",
                (rs, row) -> {
                    ChangeSummary s = new ChangeSummary();
                    s.setChangeId(rs.getString("change_id"));
                    s.setTitle(rs.getString("title"));
                    s.setStatus(rs.getString("status"));
                    s.setCreatedAt(rs.getTimestamp("created_at").toInstant().toString());
                    s.setUpdatedAt(rs.getTimestamp("updated_at").toInstant().toString());
                    return s;
                }, size, offset);
    }

    public ChangeDetail findChangeDetail(String changeId) {
        List<ChangeDetail> results = jdbc.query(
                "SELECT change_id, title, status, content, requirement_ids, dependencies, created_at, updated_at FROM prd_change WHERE change_id=?",
                (rs, row) -> {
                    ChangeDetail d = new ChangeDetail();
                    d.setChangeId(rs.getString("change_id"));
                    d.setTitle(rs.getString("title"));
                    d.setStatus(rs.getString("status"));
                    d.setContent(rs.getString("content"));
                    d.setCreatedAt(rs.getTimestamp("created_at").toInstant().toString());
                    d.setUpdatedAt(rs.getTimestamp("updated_at").toInstant().toString());
                    return d;
                }, changeId);
        return results.isEmpty() ? null : results.get(0);
    }

    public String findChangeIdByIngestionId(String ingestionId) {
        List<String> results = jdbc.query(
                "SELECT change_id FROM prd_change WHERE ingestion_id=? LIMIT 1",
                (rs, row) -> rs.getString("change_id"), ingestionId);
        return results.isEmpty() ? null : results.get(0);
    }

    // ---- Document Refs ----
    public void insertDocumentRef(String changeId, String docId, String docType, String docTitle, String docStatus, String projectId) {
        jdbc.update("INSERT INTO prd_change_document_ref (change_id, doc_id, doc_type, doc_title, doc_status, project_id) VALUES (?,?,?,?,?,?)",
                changeId, docId, docType, docTitle, docStatus, projectId);
    }

    public List<ChangeSummary.DocumentRef> findDocumentRefs(String changeId) {
        return jdbc.query(
                "SELECT doc_type, doc_id, doc_title, doc_status FROM prd_change_document_ref WHERE change_id=?",
                (rs, row) -> {
                    ChangeSummary.DocumentRef ref = new ChangeSummary.DocumentRef();
                    ref.setType(rs.getString("doc_type"));
                    ref.setDocId(rs.getString("doc_id"));
                    ref.setTitle(rs.getString("doc_title"));
                    ref.setStatus(rs.getString("doc_status"));
                    return ref;
                }, changeId);
    }

    // ---- Acceptance Criteria ----
    public void insertAcceptanceCriteria(String changeId, String acId, String description, String projectId) {
        jdbc.update("INSERT INTO prd_change_acceptance_criteria (change_id, ac_id, description, project_id) VALUES (?,?,?,?)",
                changeId, acId, description, projectId);
    }

    public List<ChangeDetail.AcceptanceCriteria> findAcceptanceCriteria(String changeId) {
        return jdbc.query(
                "SELECT ac_id, description FROM prd_change_acceptance_criteria WHERE change_id=?",
                (rs, row) -> {
                    ChangeDetail.AcceptanceCriteria ac = new ChangeDetail.AcceptanceCriteria();
                    ac.setId(rs.getString("ac_id"));
                    ac.setDescription(rs.getString("description"));
                    return ac;
                }, changeId);
    }
}