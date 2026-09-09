package com.tayama.prd.ingestion.model.project;

import java.util.List;

/**
 * 创建/更新项目请求。
 */
public class ProjectRequest {
    private String name;
    private String description;
    private Long languageId;
    private List<Long> frameworkIds;
    private String status;
    /** 创建项目时初始添加的成员 userId 列表（可选），默认添加创建者为 owner */
    private List<String> memberIds;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getLanguageId() { return languageId; }
    public void setLanguageId(Long languageId) { this.languageId = languageId; }
    public List<Long> getFrameworkIds() { return frameworkIds; }
    public void setFrameworkIds(List<Long> frameworkIds) { this.frameworkIds = frameworkIds; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<String> getMemberIds() { return memberIds; }
    public void setMemberIds(List<String> memberIds) { this.memberIds = memberIds; }
}