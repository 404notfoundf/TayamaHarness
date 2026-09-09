package com.tayama.prd.ingestion.model.project;

import java.util.List;

/**
 * 项目。
 */
public class Project {
    private Long id;
    private String projectId;
    private String name;
    private String description;
    private Long languageId;
    private String languageName;
    private List<Long> frameworkIds;
    private List<String> frameworkNames;
    private String status;
    private String createdAt;
    private String updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getLanguageId() { return languageId; }
    public void setLanguageId(Long languageId) { this.languageId = languageId; }
    public String getLanguageName() { return languageName; }
    public void setLanguageName(String languageName) { this.languageName = languageName; }
    public List<Long> getFrameworkIds() { return frameworkIds; }
    public void setFrameworkIds(List<Long> frameworkIds) { this.frameworkIds = frameworkIds; }
    public List<String> getFrameworkNames() { return frameworkNames; }
    public void setFrameworkNames(List<String> frameworkNames) { this.frameworkNames = frameworkNames; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}