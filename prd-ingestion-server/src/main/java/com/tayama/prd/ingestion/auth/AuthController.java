package com.tayama.prd.ingestion.auth;

import com.tayama.prd.ingestion.model.auth.LoginRequest;
import com.tayama.prd.ingestion.model.auth.LoginResponse;
import com.tayama.prd.ingestion.model.auth.ProjectMember;
import com.tayama.prd.ingestion.model.auth.RegisterRequest;
import com.tayama.prd.ingestion.model.auth.User;
import com.tayama.prd.ingestion.model.response.BaseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 认证 REST 控制器：登录、注册、获取当前用户。
 *
 * <p>端点前缀：/api/v1/auth （由全局 context-path 决定）</p>
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * POST /api/v1/auth/login - 用户登录。
     */
    @PostMapping("/login")
    public ResponseEntity<BaseResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(BaseResponse.ok(response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(BaseResponse.error("AUTH_FAILED", e.getMessage()));
        }
    }

    /**
     * POST /api/v1/auth/register - 用户注册。
     */
    @PostMapping("/register")
    public ResponseEntity<BaseResponse<LoginResponse>> register(@RequestBody RegisterRequest request) {
        try {
            LoginResponse response = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(BaseResponse.ok("注册成功", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error("INVALID_REQUEST", e.getMessage()));
        }
    }

    /**
     * GET /api/v1/auth/me - 获取当前登录用户信息。
     */
    @GetMapping("/me")
    public ResponseEntity<BaseResponse<User>> me(@CurrentUser String userId) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(BaseResponse.error("UNAUTHORIZED", "未登录"));
        }
        try {
            User user = authService.getUserById(userId);
            return ResponseEntity.ok(BaseResponse.ok(user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(BaseResponse.error("NOT_FOUND", e.getMessage()));
        }
    }

    // ======================== 项目成员管理 ========================

    /**
     * GET /api/v1/auth/projects/{projectId}/members - 获取项目成员列表。
     */
    @GetMapping("/projects/{projectId}/members")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BaseResponse<List<ProjectMember>>> listMembers(@PathVariable String projectId) {
        return ResponseEntity.ok(BaseResponse.ok(authService.getProjectMembers(projectId)));
    }

    /**
     * POST /api/v1/auth/projects/{projectId}/members - 添加项目成员。
     */
    @PostMapping("/projects/{projectId}/members")
    @PreAuthorize("isAuthenticated() && @authz.hasProjectRole('owner', 'maintainer')")
    public ResponseEntity<BaseResponse<Void>> addMember(
            @PathVariable String projectId, @RequestBody Map<String, String> body) {
        try {
            String userId = body.get("userId");
            String role = body.getOrDefault("role", "contributor");
            if (userId == null || userId.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(BaseResponse.error("INVALID_REQUEST", "userId 不能为空"));
            }
            authService.addProjectMember(projectId, userId, role);
            return ResponseEntity.ok(BaseResponse.ok("添加成功", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error("INVALID_REQUEST", e.getMessage()));
        }
    }

    /**
     * PUT /api/v1/auth/projects/{projectId}/members/{userId} - 更新项目成员角色。
     */
    @PutMapping("/projects/{projectId}/members/{userId}")
    @PreAuthorize("isAuthenticated() && @authz.hasProjectRole('owner')")
    public ResponseEntity<BaseResponse<Void>> updateMemberRole(
            @PathVariable String projectId, @PathVariable String userId, @RequestBody Map<String, String> body) {
        try {
            String role = body.get("role");
            if (role == null || role.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(BaseResponse.error("INVALID_REQUEST", "role 不能为空"));
            }
            authService.updateProjectMemberRole(projectId, userId, role);
            return ResponseEntity.ok(BaseResponse.ok("更新成功", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error("INVALID_REQUEST", e.getMessage()));
        }
    }

    /**
     * DELETE /api/v1/auth/projects/{projectId}/members/{userId} - 移除项目成员。
     */
    @DeleteMapping("/projects/{projectId}/members/{userId}")
    @PreAuthorize("isAuthenticated() && @authz.hasProjectRole('owner')")
    public ResponseEntity<BaseResponse<Void>> removeMember(
            @PathVariable String projectId, @PathVariable String userId) {
        try {
            authService.removeProjectMember(projectId, userId);
            return ResponseEntity.ok(BaseResponse.ok("移除成功", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error("INVALID_REQUEST", e.getMessage()));
        }
    }

    /**
     * GET /api/v1/auth/users - 获取所有用户列表（管理员添加成员时选择）。
     */
    @GetMapping("/users")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BaseResponse<List<User>>> listUsers() {
        return ResponseEntity.ok(BaseResponse.ok(authService.listAllUsers()));
    }

    // ======================== 用户管理（管理员） ========================

    /**
     * PUT /api/v1/auth/users/{userId}/disable - 禁用用户。
     */
    @PutMapping("/users/{userId}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<Void>> disableUser(@PathVariable String userId) {
        try {
            authService.disableUser(userId);
            return ResponseEntity.ok(BaseResponse.ok("用户已禁用", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error("INVALID_REQUEST", e.getMessage()));
        }
    }

    /**
     * PUT /api/v1/auth/users/{userId}/enable - 启用用户。
     */
    @PutMapping("/users/{userId}/enable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<Void>> enableUser(@PathVariable String userId) {
        try {
            authService.enableUser(userId);
            return ResponseEntity.ok(BaseResponse.ok("用户已启用", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error("INVALID_REQUEST", e.getMessage()));
        }
    }

    /**
     * PUT /api/v1/auth/users/{userId}/role - 更新用户角色。
     */
    @PutMapping("/users/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<Void>> updateUserRole(
            @PathVariable String userId, @RequestBody Map<String, String> body) {
        try {
            String role = body.get("role");
            if (role == null || role.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(BaseResponse.error("INVALID_REQUEST", "role 不能为空"));
            }
            authService.updateUserRole(userId, role);
            return ResponseEntity.ok(BaseResponse.ok("角色已更新", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error("INVALID_REQUEST", e.getMessage()));
        }
    }

    /**
     * DELETE /api/v1/auth/users/{userId} - 删除用户。
     */
    @DeleteMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<Void>> deleteUser(@PathVariable String userId) {
        try {
            authService.deleteUser(userId);
            return ResponseEntity.ok(BaseResponse.ok("用户已删除", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error("INVALID_REQUEST", e.getMessage()));
        }
    }
}