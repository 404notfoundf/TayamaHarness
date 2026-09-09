package com.tayama.prd.ingestion.model.pipeline;

/**
 * 变更日志条目。
 */
public class ChangeLogEntry {
    private String id;
    private String type;     // status_change | approval | rejection | comment | commit | pr
    private String message;
    private String detail;
    private String actor;
    private String createdAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
    public String getActor() { return actor; }
    public void setActor(String actor) { this.actor = actor; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}