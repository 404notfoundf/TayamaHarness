package com.huazai.prd.ingestion.repository;

import com.huazai.prd.ingestion.model.prd.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

/**
 * PRD 导入记录数据访问层。
 */
@Repository
public class PrdIngestionRepository {

    private final JdbcTemplate jdbc;
    private static final Logger log = LoggerFactory.getLogger(PrdIngestionRepository.class);

    public PrdIngestionRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void insertIngestion(String projectId, String ingestionId, String title, String content, String format, String sourceUrl, String status) {
        jdbc.update("INSERT INTO prd_ingestion (project_id, ingestion_id, title, content, format, source_url, status, progress) VALUES (?,?,?,?,?,?,?,0)",
                projectId, ingestionId, title, content, format, sourceUrl, status);
    }

    public void updateStatus(String ingestionId, String status, int progress, String progressMsg) {
        jdbc.update("UPDATE prd_ingestion SET status=?, progress=?, progress_msg=? WHERE ingestion_id=?",
                status, progress, progressMsg, ingestionId);
    }

    public void updateError(String ingestionId, String errorMessage) {
        jdbc.update("UPDATE prd_ingestion SET status='failed', error_message=? WHERE ingestion_id=?",
                errorMessage, ingestionId);
    }

    public void updateErrorWithSource(String ingestionId, String errorMessage, String parseSource) {
        try {
            jdbc.update("UPDATE prd_ingestion SET status='failed', error_message=?, parse_source=? WHERE ingestion_id=?",
                    errorMessage, parseSource, ingestionId);
        } catch (Exception e) {
            // 兼容旧表
            updateError(ingestionId, errorMessage);
        }
    }

    public IngestionProgress findProgress(String ingestionId) {
        List<IngestionProgress> results = jdbc.query(
                "SELECT ingestion_id, status, progress, progress_msg FROM prd_ingestion WHERE ingestion_id=?",
                (rs, row) -> {
                    IngestionProgress p = new IngestionProgress();
                    p.setIngestionId(rs.getString("ingestion_id"));
                    p.setStatus(rs.getString("status"));
                    p.setProgress(rs.getInt("progress"));
                    p.setMessage(rs.getString("progress_msg"));
                    return p;
                }, ingestionId);
        return results.isEmpty() ? null : results.get(0);
    }

    public String findTitle(String ingestionId) {
        List<String> results = jdbc.query("SELECT title FROM prd_ingestion WHERE ingestion_id=?",
                (rs, row) -> rs.getString("title"), ingestionId);
        return results.isEmpty() ? "" : results.get(0);
    }

    public String findProjectId(String ingestionId) {
        List<String> results = jdbc.query("SELECT project_id FROM prd_ingestion WHERE ingestion_id=?",
                (rs, row) -> rs.getString("project_id"), ingestionId);
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * 记录解析来源与失败原因（不改变 status）。
     * 用于 LLM 失败回退后，仍保留 completed 状态，但把失败原因暴露给前端/用户排查。
     */
    public void updateParseMeta(String ingestionId, String parseSource, String errorMessage) {
        try {
            jdbc.update("UPDATE prd_ingestion SET parse_source=?, error_message=? WHERE ingestion_id=?",
                    parseSource, errorMessage, ingestionId);
        } catch (Exception e) {
            log.warn("[repo] updateParseMeta failed for {}: {}", ingestionId, e.getMessage());
        }
    }

    public String findParseSource(String ingestionId) {
        try {
            List<String> results = jdbc.query("SELECT parse_source FROM prd_ingestion WHERE ingestion_id=?",
                    (rs, row) -> rs.getString("parse_source"), ingestionId);
            return results.isEmpty() ? "section_hierarchy" : results.get(0);
        } catch (Exception e) {
            // 兼容旧表：parse_source 列不存在时返回默认值
            return "section_hierarchy";
        }
    }

    public String findErrorMessage(String ingestionId) {
        List<String> results = jdbc.query("SELECT error_message FROM prd_ingestion WHERE ingestion_id=?",
                (rs, row) -> rs.getString("error_message"), ingestionId);
        return results.isEmpty() ? null : results.get(0);
    }

    public String findContent(String ingestionId) {
        List<String> results = jdbc.query("SELECT content FROM prd_ingestion WHERE ingestion_id=?",
                (rs, row) -> rs.getString("content"), ingestionId);
        return results.isEmpty() ? "" : results.get(0);
    }

    public void updateParseSource(String ingestionId, String parseSource) {
        try {
            jdbc.update("UPDATE prd_ingestion SET parse_source=? WHERE ingestion_id=?",
                    parseSource, ingestionId);
        } catch (Exception e) {
            // 兼容旧表：parse_source 列不存在时静默忽略
            log.warn("updateParseSource failed (column may not exist yet): {}", e.getMessage());
        }
    }

    // ---- Requirements ----
    public void insertRequirement(String projectId, String ingestionId, String reqId, String description, String priority,
                                   String relatedEntities, String sourceParagraph, String notes, int sortOrder) {
        insertRequirement(projectId, ingestionId, reqId, description, priority, relatedEntities, sourceParagraph, notes, sortOrder, "confirmed");
    }

    public void insertRequirement(String projectId, String ingestionId, String reqId, String description, String priority,
                                   String relatedEntities, String sourceParagraph, String notes, int sortOrder,
                                   String candidateStatus) {
        jdbc.update("INSERT INTO prd_requirement (project_id, ingestion_id, req_id, description, priority, related_entities, source_paragraph, notes, sort_order, candidate_status) VALUES (?,?,?,?,?,?,?,?,?,?)",
                projectId, ingestionId, reqId, description, priority, relatedEntities, sourceParagraph, notes, sortOrder, candidateStatus);
    }

    /**
     * 清理某条导入记录下的全部解析产物（重新解析前调用，保证幂等）。
     */
    public void deleteParsedData(String ingestionId) {
        jdbc.update("DELETE FROM prd_data_entity_attribute WHERE entity_id IN (SELECT id FROM prd_data_entity WHERE ingestion_id=?)", ingestionId);
        jdbc.update("DELETE FROM prd_data_entity_relation WHERE entity_id IN (SELECT id FROM prd_data_entity WHERE ingestion_id=?)", ingestionId);
        jdbc.update("DELETE FROM prd_data_entity WHERE ingestion_id=?", ingestionId);
        jdbc.update("DELETE FROM prd_requirement WHERE ingestion_id=?", ingestionId);
        jdbc.update("DELETE FROM prd_interface WHERE ingestion_id=?", ingestionId);
        jdbc.update("DELETE FROM prd_architecture_decision WHERE ingestion_id=?", ingestionId);
        jdbc.update("DELETE FROM prd_document WHERE ingestion_id=?", ingestionId);
        jdbc.update("DELETE FROM prd_llm_result WHERE ingestion_id=?", ingestionId);
    }

    /** 保存 LLM 解析结果（旁路存储，仅用于前端左侧"LLM 提取结果"展示；同一 ingestion 重解析时覆盖）。 */
    public void insertLlmResult(String ingestionId, String payloadJson) {
        jdbc.update("INSERT INTO prd_llm_result (ingestion_id, payload_json) VALUES (?, ?) " +
                        "ON DUPLICATE KEY UPDATE payload_json = VALUES(payload_json)",
                ingestionId, payloadJson);
    }

    /** 读取 LLM 解析结果 JSON；无记录返回 null。 */
    public String findLlmResult(String ingestionId) {
        List<String> results = jdbc.query("SELECT payload_json FROM prd_llm_result WHERE ingestion_id=?",
                (rs, row) -> rs.getString("payload_json"), ingestionId);
        return results.isEmpty() ? null : results.get(0);
    }

    public List<RequirementEntity> findRequirements(String ingestionId) {
        return jdbc.query(
                "SELECT req_id, description, priority, related_entities, source_paragraph, notes, candidate_status FROM prd_requirement WHERE ingestion_id=? ORDER BY sort_order",
                (rs, row) -> {
                    RequirementEntity r = new RequirementEntity();
                    r.setId(rs.getString("req_id"));
                    r.setDescription(rs.getString("description"));
                    r.setPriority(rs.getString("priority"));
                    // JSON array stored as string
                    r.setSourceParagraph(rs.getString("source_paragraph"));
                    r.setNotes(rs.getString("notes"));
                    r.setCandidateStatus(rs.getString("candidate_status"));
                    return r;
                }, ingestionId);
    }

    // ---- Data Entities ----
    public long insertDataEntity(String projectId, String ingestionId, String name, String description, int sortOrder) {
        return insertDataEntity(projectId, ingestionId, name, description, sortOrder, null);
    }

    public long insertDataEntity(String projectId, String ingestionId, String name, String description, int sortOrder, String sourceParagraph) {
        return insertDataEntity(projectId, ingestionId, name, description, sortOrder, sourceParagraph, "confirmed");
    }

    public long insertDataEntity(String projectId, String ingestionId, String name, String description, int sortOrder,
                                 String sourceParagraph, String candidateStatus) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO prd_data_entity (project_id, ingestion_id, entity_name, description, source_paragraph, sort_order, candidate_status) VALUES (?,?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, projectId);
            ps.setString(2, ingestionId);
            ps.setString(3, name);
            ps.setString(4, description);
            ps.setString(5, sourceParagraph);
            ps.setInt(6, sortOrder);
            ps.setString(7, candidateStatus);
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public void insertEntityAttribute(long entityId, String name, String type, String description, int sortOrder) {
        jdbc.update("INSERT INTO prd_data_entity_attribute (entity_id, attr_name, attr_type, description, sort_order) VALUES (?,?,?,?,?)",
                entityId, name, type, description, sortOrder);
    }

    public void insertEntityRelation(long entityId, String target, String relationType, String description, int sortOrder) {
        jdbc.update("INSERT INTO prd_data_entity_relation (entity_id, target_entity, relation_type, description, sort_order) VALUES (?,?,?,?,?)",
                entityId, target, relationType, description, sortOrder);
    }

    public List<DataEntity> findDataEntities(String ingestionId) {
        return jdbc.query(
                "SELECT id, entity_name, description, source_paragraph, candidate_status FROM prd_data_entity WHERE ingestion_id=? ORDER BY sort_order",
                (rs, row) -> {
                    DataEntity e = new DataEntity();
                    e.setName(rs.getString("entity_name"));
                    e.setDescription(rs.getString("description"));
                    e.setSourceParagraph(rs.getString("source_paragraph"));
                    e.setCandidateStatus(rs.getString("candidate_status"));
                    long entityId = rs.getLong("id");
                    e.setAttributes(findAttributes(entityId));
                    e.setRelations(findRelations(entityId));
                    return e;
                }, ingestionId);
    }

    private List<DataEntity.Attribute> findAttributes(long entityId) {
        return jdbc.query(
                "SELECT attr_name, attr_type, description FROM prd_data_entity_attribute WHERE entity_id=? ORDER BY sort_order",
                (rs, row) -> {
                    DataEntity.Attribute a = new DataEntity.Attribute();
                    a.setName(rs.getString("attr_name"));
                    a.setType(rs.getString("attr_type"));
                    a.setDescription(rs.getString("description"));
                    return a;
                }, entityId);
    }

    private List<DataEntity.Relation> findRelations(long entityId) {
        return jdbc.query(
                "SELECT target_entity, relation_type, description FROM prd_data_entity_relation WHERE entity_id=? ORDER BY sort_order",
                (rs, row) -> {
                    DataEntity.Relation r = new DataEntity.Relation();
                    r.setTarget(rs.getString("target_entity"));
                    r.setType(rs.getString("relation_type"));
                    r.setDescription(rs.getString("description"));
                    return r;
                }, entityId);
    }

    // ---- Interfaces ----
    public void insertInterface(String projectId, String ingestionId, String method, String path, String summary,
                                 String requestBody, String responseBody, String notes, int sortOrder) {
        insertInterface(projectId, ingestionId, method, path, summary, requestBody, responseBody, notes, sortOrder, null);
    }

    public void insertInterface(String projectId, String ingestionId, String method, String path, String summary,
                                 String requestBody, String responseBody, String notes, int sortOrder, String sourceParagraph) {
        insertInterface(projectId, ingestionId, method, path, summary, requestBody, responseBody, notes, sortOrder, sourceParagraph, "confirmed");
    }

    public void insertInterface(String projectId, String ingestionId, String method, String path, String summary,
                                 String requestBody, String responseBody, String notes, int sortOrder, String sourceParagraph,
                                 String candidateStatus) {
        jdbc.update("INSERT INTO prd_interface (project_id, ingestion_id, http_method, path, summary, request_body, response_body, notes, source_paragraph, sort_order, candidate_status) VALUES (?,?,?,?,?,?,?,?,?,?,?)",
                projectId, ingestionId, method, path, summary, requestBody, responseBody, notes, sourceParagraph, sortOrder, candidateStatus);
    }

    public List<InterfaceProtocol> findInterfaces(String ingestionId) {
        return jdbc.query(
                "SELECT http_method, path, summary, request_body, response_body, notes, source_paragraph, candidate_status FROM prd_interface WHERE ingestion_id=? ORDER BY sort_order",
                (rs, row) -> {
                    InterfaceProtocol i = new InterfaceProtocol();
                    i.setMethod(rs.getString("http_method"));
                    i.setPath(rs.getString("path"));
                    i.setSummary(rs.getString("summary"));
                    i.setRequestBody(rs.getString("request_body"));
                    i.setResponseBody(rs.getString("response_body"));
                    i.setNotes(rs.getString("notes"));
                    i.setSourceParagraph(rs.getString("source_paragraph"));
                    i.setCandidateStatus(rs.getString("candidate_status"));
                    return i;
                }, ingestionId);
    }

    // ---- Candidate confirmation ----

    /**
     * 人工确认一条候选条目：proposed → confirmed。
     * type ∈ requirement | entity | interface | decision；
     * id 分别为 req_id / entity_name / method|path / ad_id（接口用复合键）。
     * 返回受影响行数。
     */
    public int confirmCandidate(String ingestionId, String type, String id) {
        switch (type) {
            case "requirement":
                return jdbc.update("UPDATE prd_requirement SET candidate_status='confirmed' WHERE ingestion_id=? AND req_id=?",
                        ingestionId, id);
            case "entity":
                return jdbc.update("UPDATE prd_data_entity SET candidate_status='confirmed' WHERE ingestion_id=? AND entity_name=?",
                        ingestionId, id);
            case "interface":
                String[] parts = id.split("\\|", 2);
                if (parts.length < 2) return 0;
                return jdbc.update("UPDATE prd_interface SET candidate_status='confirmed' WHERE ingestion_id=? AND http_method=? AND path=?",
                        ingestionId, parts[0], parts[1]);
            case "decision":
                return jdbc.update("UPDATE prd_architecture_decision SET status='accepted' WHERE ingestion_id=? AND ad_id=?",
                        ingestionId, id);
            default:
                return 0;
        }
    }

    // ---- Architecture Decisions ----
    public void insertArchDecision(String projectId, String ingestionId, String adId, String title, String context,
                                    String decision, String consequences, String status, int sortOrder) {
        insertArchDecision(projectId, ingestionId, adId, title, context, decision, consequences, status, sortOrder, null);
    }

    public void insertArchDecision(String projectId, String ingestionId, String adId, String title, String context,
                                    String decision, String consequences, String status, int sortOrder, String sourceParagraph) {
        jdbc.update("INSERT INTO prd_architecture_decision (project_id, ingestion_id, ad_id, title, context, decision, consequences, status, source_paragraph, sort_order) VALUES (?,?,?,?,?,?,?,?,?,?)",
                projectId, ingestionId, adId, title, context, decision, consequences, status, sourceParagraph, sortOrder);
    }

    public List<ArchitectureDecision> findArchDecisions(String ingestionId) {
        return jdbc.query(
                "SELECT ad_id, title, context, decision, consequences, status, source_paragraph FROM prd_architecture_decision WHERE ingestion_id=? ORDER BY sort_order",
                (rs, row) -> {
                    ArchitectureDecision ad = new ArchitectureDecision();
                    ad.setId(rs.getString("ad_id"));
                    ad.setTitle(rs.getString("title"));
                    ad.setContext(rs.getString("context"));
                    ad.setDecision(rs.getString("decision"));
                    ad.setStatus(rs.getString("status"));
                    ad.setSourceParagraph(rs.getString("source_paragraph"));
                    return ad;
                }, ingestionId);
    }

    // ---- Documents ----
    public void insertDocument(String projectId, String docId, String ingestionId, String type, String title, String status, int version, String content, String originalPrdContent) {
        jdbc.update("INSERT INTO prd_document (project_id, doc_id, ingestion_id, type, title, status, version, content, original_prd_content) VALUES (?,?,?,?,?,?,?,?,?)",
                projectId, docId, ingestionId, type, title, status, version, content, originalPrdContent);
    }

    public List<DocumentSummary> findDocumentSummaries(String ingestionId) {
        return jdbc.query(
                "SELECT doc_id, type, title, status, version, updated_at FROM prd_document WHERE ingestion_id=?",
                (rs, row) -> {
                    DocumentSummary d = new DocumentSummary();
                    d.setDocId(rs.getString("doc_id"));
                    d.setType(rs.getString("type"));
                    d.setTitle(rs.getString("title"));
                    d.setStatus(rs.getString("status"));
                    d.setVersion(rs.getInt("version"));
                    d.setUpdatedAt(rs.getTimestamp("updated_at").toInstant().toString());
                    return d;
                }, ingestionId);
    }

    /** 按项目查询文档列表（ingestionId 为空时使用，文档基于项目维度展开） */
    public List<DocumentSummary> findDocumentSummariesByProject(String projectId) {
        return jdbc.query(
                "SELECT doc_id, type, title, status, version, updated_at FROM prd_document WHERE project_id=? ORDER BY updated_at DESC",
                (rs, row) -> {
                    DocumentSummary d = new DocumentSummary();
                    d.setDocId(rs.getString("doc_id"));
                    d.setType(rs.getString("type"));
                    d.setTitle(rs.getString("title"));
                    d.setStatus(rs.getString("status"));
                    d.setVersion(rs.getInt("version"));
                    d.setUpdatedAt(rs.getTimestamp("updated_at").toInstant().toString());
                    return d;
                }, projectId);
    }

    public com.huazai.prd.ingestion.model.document.WikiDocument findDocument(String docId) {
        List<com.huazai.prd.ingestion.model.document.WikiDocument> results = jdbc.query(
                "SELECT doc_id, project_id, ingestion_id, type, title, status, version, content, original_prd_content, change_log, created_by, updated_at FROM prd_document WHERE doc_id=?",
                (rs, row) -> {
                    com.huazai.prd.ingestion.model.document.WikiDocument doc = new com.huazai.prd.ingestion.model.document.WikiDocument();
                    doc.setDocId(rs.getString("doc_id"));
                    doc.setProjectId(rs.getString("project_id"));
                    doc.setIngestionId(rs.getString("ingestion_id"));
                    doc.setType(rs.getString("type"));
                    doc.setTitle(rs.getString("title"));
                    doc.setStatus(rs.getString("status"));
                    doc.setVersion(rs.getInt("version"));
                    doc.setContent(rs.getString("content"));
                    doc.setOriginalPrdContent(rs.getString("original_prd_content"));
                    doc.setChangeLog(rs.getString("change_log"));
                    doc.setCreatedBy(rs.getString("created_by"));
                    Timestamp ts = rs.getTimestamp("updated_at");
                    doc.setUpdatedAt(ts != null ? ts.toInstant().toString() : null);
                    return doc;
                }, docId);
        return results.isEmpty() ? null : results.get(0);
    }

    public void updateDocument(String docId, String content, String changeLog) {
        jdbc.update("UPDATE prd_document SET content=?, change_log=?, version=version+1 WHERE doc_id=?",
                content, changeLog, docId);
    }

    public void updateDocumentStatus(String docId, String status) {
        jdbc.update("UPDATE prd_document SET status=? WHERE doc_id=?", status, docId);
    }

    // ---- Document Approval ----
    public void insertApproval(String projectId, String docId, String action, String comment, String approvedBy) {
        jdbc.update("INSERT INTO prd_document_approval (project_id, doc_id, action, comment, approved_by) VALUES (?,?,?,?,?)",
                projectId, docId, action, comment, approvedBy);
    }
}