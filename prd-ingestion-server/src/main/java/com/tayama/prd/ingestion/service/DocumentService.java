package com.tayama.prd.ingestion.service;

import com.tayama.prd.ingestion.model.document.DocumentApprovalResponse;
import com.tayama.prd.ingestion.model.document.WikiDocument;
import com.tayama.prd.ingestion.model.prd.DocumentSummary;
import com.tayama.prd.ingestion.repository.ChangeRepository;
import com.tayama.prd.ingestion.repository.PrdIngestionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 文档管理服务。
 */
@Service
public class DocumentService {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentService.class);

    private final PrdIngestionRepository repo;
    private final ChangeRepository changeRepo;
    private final PipelineService pipelineService;

    public DocumentService(PrdIngestionRepository repo, ChangeRepository changeRepo, PipelineService pipelineService) {
        this.repo = repo;
        this.changeRepo = changeRepo;
        this.pipelineService = pipelineService;
    }

    public List<DocumentSummary> getDocuments(String ingestionId, String projectId) {
        // ingestionId 为空时按项目维度返回该项目的全部文档
        if (ingestionId == null || ingestionId.isBlank()) {
            return repo.findDocumentSummariesByProject(projectId);
        }
        return repo.findDocumentSummaries(ingestionId);
    }

    public WikiDocument getDocument(String docId) {
        WikiDocument doc = repo.findDocument(docId);
        if (doc != null && doc.getIngestionId() != null && !doc.getIngestionId().isBlank()) {
            enrichWithExtraction(doc);
        }
        return doc;
    }

    /**
     * 为文档补充 LLM/解析提取结果与来源信息，供前端展示"提取结果 / PRD 原文"对照，
     * 以及严格按模板渲染后的最终内容。
     */
    private void enrichWithExtraction(WikiDocument doc) {
        String ingestionId = doc.getIngestionId();
        try {
            String parseSource = repo.findParseSource(ingestionId);
            doc.setParseSource(parseSource);
            doc.setParseSourceLabel(toParseSourceLabel(parseSource));
            doc.setParseErrorMessage(repo.findErrorMessage(ingestionId));
            doc.setRequirements(repo.findRequirements(ingestionId));
            doc.setDataEntities(repo.findDataEntities(ingestionId));
            doc.setInterfaces(repo.findInterfaces(ingestionId));
            doc.setArchDecisions(repo.findArchDecisions(ingestionId));
            // LLM 旁路结果（仅用于左侧"LLM 提取结果"展示；LLM 未启用/失败时为 null）
            doc.setLlmExtraction(parseLlmResult(ingestionId));
        } catch (Exception e) {
            LOG.warn("[getDocument] 补充提取结果失败 docId={}: {}", doc.getDocId(), e.getMessage());
        }
    }

    private static final com.fasterxml.jackson.databind.ObjectMapper OBJECT_MAPPER = new com.fasterxml.jackson.databind.ObjectMapper();

    private Object parseLlmResult(String ingestionId) {
        try {
            String json = repo.findLlmResult(ingestionId);
            if (json == null || json.isBlank()) return null;
            return OBJECT_MAPPER.readTree(json);
        } catch (Exception e) {
            LOG.warn("[getDocument] 读取 LLM 旁路结果失败 ingestionId={}: {}", ingestionId, e.getMessage());
            return null;
        }
    }

    private static String toParseSourceLabel(String parseSource) {
        if (parseSource == null) return "模板匹配结果";
        switch (parseSource) {
            case "llm": return "LLM 提取结果";
            case "template": return "模板匹配结果";
            case "section_hierarchy": return "章节层级算法结果";
            case "keyword_fallback": return "关键词匹配结果";
            default: return parseSource;
        }
    }

    public WikiDocument updateDocument(String docId, String content, String changeLog) {
        repo.updateDocument(docId, content, changeLog != null ? changeLog : "");
        return repo.findDocument(docId);
    }

    public DocumentApprovalResponse approveDocument(String docId, String action, String comment) {
        if (!"approve".equals(action) && !"reject".equals(action)) {
            throw new IllegalArgumentException("审批动作必须是 approve 或 reject");
        }
        // 查询文档获取 projectId
        com.tayama.prd.ingestion.model.document.WikiDocument doc = repo.findDocument(docId);
        if (doc == null) {
            throw new IllegalArgumentException("文档不存在: " + docId);
        }
        String newStatus = "approve".equals(action) ? "approved" : "rejected";
        repo.updateDocumentStatus(docId, newStatus);
        repo.insertApproval(doc.getProjectId(), docId, action, comment, "system");

        // 如果审批通过，自动推进关联 Change 的流水线
        if ("approve".equals(action)) {
            try {
                String ingestionId = doc.getIngestionId();
                if (ingestionId != null) {
                    String changeId = changeRepo.findChangeIdByIngestionId(ingestionId);
                    if (changeId != null) {
                        // 只在 drafting 状态时推进到 reviewing
                        var status = pipelineService.getPipelineStatus(changeId);
                        if (status != null && "drafting".equals(status.getCurrentStage())) {
                            pipelineService.advancePipeline(changeId, "reviewing");
                            LOG.info("[approveDocument] 文档审批通过，自动推进流水线: changeId={}, ingestionId={}", changeId, ingestionId);
                        }
                    }
                }
            } catch (Exception e) {
                LOG.warn("[approveDocument] 自动推进流水线失败: {}", e.getMessage());
            }
        }

        DocumentApprovalResponse resp = new DocumentApprovalResponse();
        resp.setDocId(docId);
        resp.setStatus(newStatus);
        resp.setComment(comment);
        return resp;
    }
}