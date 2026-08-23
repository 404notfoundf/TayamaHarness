package com.huazai.prd.ingestion.auth;

import com.huazai.prd.ingestion.model.auth.ProjectMember;
import com.huazai.prd.ingestion.model.auth.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * 认证数据访问层：用户查询、角色权限检查。
 */
@Repository
public class AuthRepository {

    private final JdbcTemplate jdbc;

    public AuthRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ======================== 用户查询 ========================

    public User findByUsername(String username) {
        List<User> list = jdbc.query(
                "SELECT * FROM sys_user WHERE username = ? AND status = 'active'",
                new UserRowMapper(), username);
        return list.isEmpty() ? null : list.get(0);
    }

    public User findByUserId(String userId) {
        List<User> list = jdbc.query(
                "SELECT * FROM sys_user WHERE user_id = ?",
                new UserRowMapper(), userId);
        if (list.isEmpty()) return null;
        User u = list.get(0);
        u.setRoles(loadUserRoles(userId));
        return u;
    }

    public boolean existsByUsername(String username) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM sys_user WHERE username = ?",
                Integer.class, username);
        return count != null && count > 0;
    }

    public void insertUser(String userId, String username, String passwordHash,
                           String displayName, String email) {
        jdbc.update(
                "INSERT INTO sys_user (user_id, username, password_hash, display_name, email) VALUES (?, ?, ?, ?, ?)",
                userId, username, passwordHash,
                displayName != null ? displayName : username,
                email);
    }

    public void insertUserRole(String userId, String roleId) {
        jdbc.update(
                "INSERT INTO sys_user_role (user_id, role_id) VALUES (?, ?) ON DUPLICATE KEY UPDATE user_id = user_id",
                userId, roleId);
    }

    // ======================== 角色查询 ========================

    public List<String> findUserRoles(String userId) {
        return jdbc.queryForList(
                "SELECT r.name FROM sys_role r JOIN sys_user_role ur ON r.role_id = ur.role_id WHERE ur.user_id = ?",
                String.class, userId);
    }

    public boolean isAdmin(String userId) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM sys_user_role ur JOIN sys_role r ON ur.role_id = r.role_id " +
                        "WHERE ur.user_id = ? AND r.name = 'ADMIN'",
                Integer.class, userId);
        return count != null && count > 0;
    }

    // ======================== 权限检查 ========================

    public boolean userHasPermission(String userId, String permissionName) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM sys_user_role ur " +
                        "JOIN sys_role_permission rp ON ur.role_id = rp.role_id " +
                        "JOIN sys_permission p ON rp.permission_id = p.permission_id " +
                        "WHERE ur.user_id = ? AND p.name = ?",
                Integer.class, userId, permissionName);
        return count != null && count > 0;
    }

    // ======================== 项目成员查询 ========================

    public String getProjectMemberRole(String projectId, String userId) {
        List<String> roles = jdbc.queryForList(
                "SELECT role FROM sys_project_member WHERE project_id = ? AND user_id = ?",
                String.class, projectId, userId);
        return roles.isEmpty() ? null : roles.get(0);
    }

    public boolean isProjectMember(String projectId, String userId) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM sys_project_member WHERE project_id = ? AND user_id = ?",
                Integer.class, projectId, userId);
        return count != null && count > 0;
    }

    public void addProjectMember(String projectId, String userId, String role) {
        jdbc.update(
                "INSERT INTO sys_project_member (project_id, user_id, role) VALUES (?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE role = VALUES(role)",
                projectId, userId, role);
    }

    public void removeProjectMember(String projectId, String userId) {
        jdbc.update(
                "DELETE FROM sys_project_member WHERE project_id = ? AND user_id = ?",
                projectId, userId);
    }

    public List<ProjectMember> listProjectMembers(String projectId) {
        return jdbc.query(
                "SELECT pm.user_id, u.username, u.display_name, u.email, pm.role, pm.created_at " +
                        "FROM sys_project_member pm " +
                        "JOIN sys_user u ON pm.user_id = u.user_id " +
                        "WHERE pm.project_id = ? " +
                        "ORDER BY pm.created_at ASC",
                new ProjectMemberRowMapper(), projectId);
    }

    public List<User> listAllUsers() {
        List<User> users = jdbc.query(
                "SELECT * FROM sys_user ORDER BY username ASC",
                new UserRowMapper());
        // 为每个用户查询角色
        for (User u : users) {
            u.setRoles(loadUserRoles(u.getUserId()));
        }
        return users;
    }

    private List<String> loadUserRoles(String userId) {
        return jdbc.query(
                "SELECT r.name FROM sys_user_role ur JOIN sys_role r ON ur.role_id = r.role_id WHERE ur.user_id = ?",
                (rs, rowNum) -> rs.getString("name"),
                userId);
    }

    public void updateUserStatus(String userId, String status) {
        jdbc.update("UPDATE sys_user SET status = ? WHERE user_id = ?", status, userId);
    }

    public void removeUserRole(String userId, String roleId) {
        jdbc.update("DELETE FROM sys_user_role WHERE user_id = ? AND role_id = ?", userId, roleId);
    }

    public void clearUserRoles(String userId) {
        jdbc.update("DELETE FROM sys_user_role WHERE user_id = ?", userId);
    }

    public void deleteUser(String userId) {
        clearUserRoles(userId);
        jdbc.update("DELETE FROM sys_project_member WHERE user_id = ?", userId);
        jdbc.update("DELETE FROM sys_user WHERE user_id = ?", userId);
    }

    // ======================== RowMapper ========================

    private static class UserRowMapper implements RowMapper<User> {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User u = new User();
            u.setId(rs.getLong("id"));
            u.setUserId(rs.getString("user_id"));
            u.setUsername(rs.getString("username"));
            u.setPasswordHash(rs.getString("password_hash"));
            u.setDisplayName(rs.getString("display_name"));
            u.setEmail(rs.getString("email"));
            u.setAvatarUrl(rs.getString("avatar_url"));
            u.setStatus(rs.getString("status"));
            if (rs.getTimestamp("created_at") != null)
                u.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            if (rs.getTimestamp("updated_at") != null)
                u.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            return u;
        }
    }

    private static class ProjectMemberRowMapper implements RowMapper<ProjectMember> {
        @Override
        public ProjectMember mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new ProjectMember(
                    rs.getString("user_id"),
                    rs.getString("username"),
                    rs.getString("display_name"),
                    rs.getString("email"),
                    rs.getString("role"),
                    rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toString() : null
            );
        }
    }
}