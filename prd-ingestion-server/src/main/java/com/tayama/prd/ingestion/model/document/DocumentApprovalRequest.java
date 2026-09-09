package com.tayama.prd.ingestion.model.document;

/**
 * 文档审批请求。
 */
public class DocumentApprovalRequest {
    private String action;   // approve | reject
    private String comment;

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}