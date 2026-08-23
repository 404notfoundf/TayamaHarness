package com.huazai.prd.ingestion.model.prd;

import java.util.List;

/**
 * PRD 解析响应。
 */
public class PrdIngestResponse {
    private String ingestionId;
    private String status;       // pending | parsing | extracting | generating | completed | failed
    private String title;
    private String parseSource;  // template | llm | section_hierarchy
    private List<RequirementEntity> requirements;
    private List<DataEntity> dataEntities;
    private List<InterfaceProtocol> interfaces;
    private List<ArchitectureDecision> architectureDecisions;
    private List<DocumentSummary> documents;

    public String getIngestionId() { return ingestionId; }
    public void setIngestionId(String ingestionId) { this.ingestionId = ingestionId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getParseSource() { return parseSource; }
    public void setParseSource(String parseSource) { this.parseSource = parseSource; }
    public List<RequirementEntity> getRequirements() { return requirements; }
    public void setRequirements(List<RequirementEntity> requirements) { this.requirements = requirements; }
    public List<DataEntity> getDataEntities() { return dataEntities; }
    public void setDataEntities(List<DataEntity> dataEntities) { this.dataEntities = dataEntities; }
    public List<InterfaceProtocol> getInterfaces() { return interfaces; }
    public void setInterfaces(List<InterfaceProtocol> interfaces) { this.interfaces = interfaces; }
    public List<ArchitectureDecision> getArchitectureDecisions() { return architectureDecisions; }
    public void setArchitectureDecisions(List<ArchitectureDecision> architectureDecisions) { this.architectureDecisions = architectureDecisions; }
    public List<DocumentSummary> getDocuments() { return documents; }
    public void setDocuments(List<DocumentSummary> documents) { this.documents = documents; }
}