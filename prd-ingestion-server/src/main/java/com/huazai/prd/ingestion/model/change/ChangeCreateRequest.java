package com.huazai.prd.ingestion.model.change;

import java.util.List;

/**
 * Change 生成请求。
 */
public class ChangeCreateRequest {
    private String ingestionId;
    private List<String> docIds;
    private String title;
    /** 项目 ID：请求头 X-Project-Id 缺失时由调用方显式传入（如 ingest 自动创建 Change 场景） */
    private String projectId;

    public String getIngestionId() { return ingestionId; }
    public void setIngestionId(String ingestionId) { this.ingestionId = ingestionId; }
    public List<String> getDocIds() { return docIds; }
    public void setDocIds(List<String> docIds) { this.docIds = docIds; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }
}