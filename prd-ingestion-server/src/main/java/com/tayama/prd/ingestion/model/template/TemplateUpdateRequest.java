package com.tayama.prd.ingestion.model.template;

/**
 * 模板更新请求。
 */
public class TemplateUpdateRequest {
    private String content;
    private String changeLog;

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getChangeLog() { return changeLog; }
    public void setChangeLog(String changeLog) { this.changeLog = changeLog; }
}