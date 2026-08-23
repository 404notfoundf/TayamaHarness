package com.huazai.prd.ingestion.model.change;

import java.util.List;

/**
 * Change 摘要。
 */
public class ChangeSummary {
    private String changeId;
    private String title;
    private String status;
    private List<String> requirementIds;
    private List<DocumentRef> documentRefs;
    private String createdAt;
    private String updatedAt;

    public String getChangeId() { return changeId; }
    public void setChangeId(String changeId) { this.changeId = changeId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<String> getRequirementIds() { return requirementIds; }
    public void setRequirementIds(List<String> requirementIds) { this.requirementIds = requirementIds; }
    public List<DocumentRef> getDocumentRefs() { return documentRefs; }
    public void setDocumentRefs(List<DocumentRef> documentRefs) { this.documentRefs = documentRefs; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public static class DocumentRef {
        private String type;
        private String docId;
        private String title;
        private String status;

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getDocId() { return docId; }
        public void setDocId(String docId) { this.docId = docId; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}