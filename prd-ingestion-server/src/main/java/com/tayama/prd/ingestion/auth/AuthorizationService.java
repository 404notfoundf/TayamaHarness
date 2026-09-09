package com.tayama.prd.ingestion.auth;

import com.tayama.prd.ingestion.config.ProjectContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 授权服务 — 作为 SpEL bean {@code @authz} 在 {@code @PreAuthorize} 中使用。
 *
 * <p>用法示例：</p>
 * <pre>{@code
 * // 检查当前用户是否拥有指定权限字符串
 * @PreAuthorize("@authz.hasPermission('prd:create')")
 *
 * // 检查当前用户在项目上下文中的角色是否满足要求
 * @PreAuthorize("@authz.hasProjectRole('maintainer')")
 * }</pre>
 */
@Component("authz")
public class AuthorizationService {

    private static final Logger LOG = LoggerFactory.getLogger(AuthorizationService.class);

    private final AuthRepository authRepository;

    public AuthorizationService(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    /** 获取当前认证用户 ID。 */
    public String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            return (String) auth.getPrincipal();
        }
        return null;
    }

    /**
     * 检查当前用户是否拥有指定权限。
     * 用于 {@code @PreAuthorize("@authz.hasPermission('prd:create')")}。
     */
    public boolean hasPermission(String permissionName) {
        String userId = getCurrentUserId();
        if (userId == null) return false;

        // 检查是否 ADMIN（拥有所有权限）
        if (authRepository.isAdmin(userId)) return true;

        return authRepository.userHasPermission(userId, permissionName);
    }

    /**
     * 检查当前用户在项目上下文中是否拥有指定项目角色。
     * 项目 ID 从 {@link ProjectContext} 获取。
     * 用于 {@code @PreAuthorize("@authz.hasProjectRole('maintainer')")}。
     *
     * @param requiredRoles 可接受的角色列表（如 "owner", "maintainer"）
     */
    public boolean hasProjectRole(String... requiredRoles) {
        String userId = getCurrentUserId();
        if (userId == null) return false;

        // ADMIN 自动拥有所有项目角色权限
        if (authRepository.isAdmin(userId)) return true;

        String projectId = ProjectContext.get();
        if (projectId == null || projectId.isBlank()) return false;

        String userRole = authRepository.getProjectMemberRole(projectId, userId);
        if (userRole == null) return false;

        for (String required : requiredRoles) {
            if (userRole.equals(required)) return true;
        }
        return false;
    }

    /**
     * 检查当前用户是否为指定项目的成员（至少 reader 角色）。
     * 用于 {@code @PreAuthorize("@authz.isProjectMember()")}。
     */
    public boolean isProjectMember() {
        String userId = getCurrentUserId();
        if (userId == null) return false;

        if (authRepository.isAdmin(userId)) return true;

        String projectId = ProjectContext.get();
        if (projectId == null || projectId.isBlank()) return false;

        return authRepository.isProjectMember(projectId, userId);
    }
}