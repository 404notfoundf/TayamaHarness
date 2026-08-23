package com.huazai.prd.ingestion.model.prd;

import java.util.List;

/**
 * PRD 解析请求。
 */
public class PrdIngestRequest {
    private String projectId; // 所属项目 ID
    private String content;
    private String fileMd5;   // MinIO 文件 MD5（分块上传完成后使用）
    private String format;    // markdown | docx | pdf | txt | confluence
    private String title;
    private String sourceUrl;

    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getFileMd5() { return fileMd5; }
    public void setFileMd5(String fileMd5) { this.fileMd5 = fileMd5; }
    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }
}