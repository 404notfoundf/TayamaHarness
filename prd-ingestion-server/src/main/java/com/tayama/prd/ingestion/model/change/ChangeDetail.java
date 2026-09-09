package com.tayama.prd.ingestion.model.change;

import java.util.List;

/**
 * Change 详情。
 */
public class ChangeDetail {
    private String changeId;
    private String title;
    private String status;
    private String content;
    private List<String> requirementIds;
    private List<ChangeSummary.DocumentRef> documentRefs;
    private List<AcceptanceCriteria> acceptanceCriteria;
    private List<String> dependencies;
    private String createdAt;
    private String updatedAt;

    public String getChangeId() { return changeId; }
    public void setChangeId(String changeId) { this.changeId = changeId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public List<String> getRequirementIds() { return requirementIds; }
    public void setRequirementIds(List<String> requirementIds) { this.requirementIds = requirementIds; }
    public List<ChangeSummary.DocumentRef> getDocumentRefs() { return documentRefs; }
    public void setDocumentRefs(List<ChangeSummary.DocumentRef> documentRefs) { this.documentRefs = documentRefs; }
    public List<AcceptanceCriteria> getAcceptanceCriteria() { return acceptanceCriteria; }
    public void setAcceptanceCriteria(List<AcceptanceCriteria> acceptanceCriteria) { this.acceptanceCriteria = acceptanceCriteria; }
    public List<String> getDependencies() { return dependencies; }
    public void setDependencies(List<String> dependencies) { this.dependencies = dependencies; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public static class AcceptanceCriteria {
        private String id;
        private String description;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}