package com.tayama.prd.ingestion.model.auth;

/**
 * 项目成员视图模型，包含用户信息。
 */
public class ProjectMember {

    private String userId;
    private String username;
    private String displayName;
    private String email;
    private String role;
    private String createdAt;

    public ProjectMember() {}

    public ProjectMember(String userId, String username, String displayName, String email, String role, String createdAt) {
        this.userId = userId;
        this.username = username;
        this.displayName = displayName;
        this.email = email;
        this.role = role;
        this.createdAt = createdAt;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}