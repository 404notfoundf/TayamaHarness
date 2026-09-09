package com.tayama.prd.ingestion.model.template;

import java.util.List;

/**
 * 模板详情。
 */
public class Template {
    private String templateId;
    private String type;
    private String name;
    private String description;
    private String content;
    private int version;
    private List<TemplateVersion> versions;
    private String updatedAt;
    private String updatedBy;

    public String getTemplateId() { return templateId; }
    public void setTemplateId(String templateId) { this.templateId = templateId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
    public List<TemplateVersion> getVersions() { return versions; }
    public void setVersions(List<TemplateVersion> versions) { this.versions = versions; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public static class TemplateVersion {
        private int version;
        private String content;
        private String updatedAt;
        private String updatedBy;
        private String changeLog;

        public int getVersion() { return version; }
        public void setVersion(int version) { this.version = version; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
        public String getUpdatedBy() { return updatedBy; }
        public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
        public String getChangeLog() { return changeLog; }
        public void setChangeLog(String changeLog) { this.changeLog = changeLog; }
    }
}