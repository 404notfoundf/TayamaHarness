package com.tayama.prd.ingestion.model.pipeline;

import java.util.List;

/**
 * 流水线状态。
 */
public class PipelineStatus {
    private String changeId;
    private String currentStage;
    private List<StageInfo> stages;
    private int progress;

    public String getChangeId() { return changeId; }
    public void setChangeId(String changeId) { this.changeId = changeId; }
    public String getCurrentStage() { return currentStage; }
    public void setCurrentStage(String currentStage) { this.currentStage = currentStage; }
    public List<StageInfo> getStages() { return stages; }
    public void setStages(List<StageInfo> stages) { this.stages = stages; }
    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }

    public static class StageInfo {
        private String stage;
        private String status;    // pending | active | completed | failed | skipped
        private String startedAt;
        private String completedAt;

        public String getStage() { return stage; }
        public void setStage(String stage) { this.stage = stage; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getStartedAt() { return startedAt; }
        public void setStartedAt(String startedAt) { this.startedAt = startedAt; }
        public String getCompletedAt() { return completedAt; }
        public void setCompletedAt(String completedAt) { this.completedAt = completedAt; }
    }
}