package com.huazai.prd.ingestion.model.prd;

/**
 * 解析进度。
 */
public class IngestionProgress {
    private String ingestionId;
    private String status;    // pending | parsing | extracting | generating | completed | failed
    private int progress;     // 0-100
    private String message;

    public String getIngestionId() { return ingestionId; }
    public void setIngestionId(String ingestionId) { this.ingestionId = ingestionId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}