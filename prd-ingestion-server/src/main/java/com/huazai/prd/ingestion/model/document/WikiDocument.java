package com.huazai.prd.ingestion.model.document;

import com.huazai.prd.ingestion.model.prd.ArchitectureDecision;
import com.huazai.prd.ingestion.model.prd.DataEntity;
import com.huazai.prd.ingestion.model.prd.InterfaceProtocol;
import com.huazai.prd.ingestion.model.prd.RequirementEntity;

import java.util.List;

/**
 * Wiki 文档详情。
 */
public class WikiDocument {
    private String docId;
    private String type;
    private String title;
    private String status;
    private int version;
    private String content;
    private String originalPrdContent;
    private String changeLog;
    private String createdBy;
    private String updatedAt;
    private String ingestionId;
    private String projectId;

    // ---- LLM 提取结果（可能为空：LLM 未启用或失败时为空列表）----
    private String parseSource;
    private String parseSourceLabel;
    private String parseErrorMessage;
    private List<RequirementEntity> requirements;
    private List<DataEntity> dataEntities;
    private List<InterfaceProtocol> interfaces;
    private List<ArchitectureDecision> archDecisions;
    /** LLM 解析结果（旁路存储，仅用于左侧"LLM 提取结果"展示；LLM 未启用/失败时为 null） */
    private Object llmExtraction;

    public String getDocId() { return docId; }
    public void setDocId(String docId) { this.docId = docId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getOriginalPrdContent() { return originalPrdContent; }
    public void setOriginalPrdContent(String originalPrdContent) { this.originalPrdContent = originalPrdContent; }
    public String getChangeLog() { return changeLog; }
    public void setChangeLog(String changeLog) { this.changeLog = changeLog; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    public String getIngestionId() { return ingestionId; }
    public void setIngestionId(String ingestionId) { this.ingestionId = ingestionId; }
    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }

    public String getParseSource() { return parseSource; }
    public void setParseSource(String parseSource) { this.parseSource = parseSource; }
    public String getParseSourceLabel() { return parseSourceLabel; }
    public void setParseSourceLabel(String parseSourceLabel) { this.parseSourceLabel = parseSourceLabel; }
    public String getParseErrorMessage() { return parseErrorMessage; }
    public void setParseErrorMessage(String parseErrorMessage) { this.parseErrorMessage = parseErrorMessage; }
    public List<RequirementEntity> getRequirements() { return requirements; }
    public void setRequirements(List<RequirementEntity> requirements) { this.requirements = requirements; }
    public List<DataEntity> getDataEntities() { return dataEntities; }
    public void setDataEntities(List<DataEntity> dataEntities) { this.dataEntities = dataEntities; }
    public List<InterfaceProtocol> getInterfaces() { return interfaces; }
    public void setInterfaces(List<InterfaceProtocol> interfaces) { this.interfaces = interfaces; }
    public List<ArchitectureDecision> getArchDecisions() { return archDecisions; }
    public void setArchDecisions(List<ArchitectureDecision> archDecisions) { this.archDecisions = archDecisions; }

    public Object getLlmExtraction() { return llmExtraction; }
    public void setLlmExtraction(Object llmExtraction) { this.llmExtraction = llmExtraction; }
}