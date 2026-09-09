package com.tayama.prd.ingestion.model.document;

/**
 * 文档审批结果。
 */
public class DocumentApprovalResponse {
    private String docId;
    private String status;
    private String comment;

    public String getDocId() { return docId; }
    public void setDocId(String docId) { this.docId = docId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}