package com.huazai.prd.ingestion.model.prd;

/**
 * 文档摘要（用于 PRD 解析结果中的文档列表）。
 */
public class DocumentSummary {
    private String docId;
    private String type;      // business-model | data-model | interface-protocol | architecture-decision
    private String title;
    private String status;    // draft | in_review | approved | rejected
    private int version;
    private String updatedAt;

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
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}