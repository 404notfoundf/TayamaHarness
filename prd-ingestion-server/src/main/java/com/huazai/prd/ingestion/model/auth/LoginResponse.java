package com.huazai.prd.ingestion.model.auth;

import java.util.List;

/**
 * 登录响应 DTO：包含 token 和用户信息。
 */
public class LoginResponse {

    private String token;
    private String userId;
    private String username;
    private String displayName;
    private String email;
    private List<String> roles;

    public LoginResponse() {}

    public LoginResponse(String token, String userId, String username,
                         String displayName, String email, List<String> roles) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.displayName = displayName;
        this.email = email;
        this.roles = roles;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
}