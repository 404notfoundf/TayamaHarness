package com.huazai.prd.ingestion.model.document;

/**
 * 文档更新请求。
 */
public class DocumentUpdateRequest {
    private String content;
    private String changeLog;

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getChangeLog() { return changeLog; }
    public void setChangeLog(String changeLog) { this.changeLog = changeLog; }
}