package com.tayama.prd.ingestion.auth;

import com.tayama.prd.ingestion.model.auth.LoginRequest;
import com.tayama.prd.ingestion.model.auth.LoginResponse;
import com.tayama.prd.ingestion.model.auth.ProjectMember;
import com.tayama.prd.ingestion.model.auth.RegisterRequest;
import com.tayama.prd.ingestion.model.auth.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * 认证与授权服务。
 */
@Service
public class AuthService {

    private static final Logger LOG = LoggerFactory.getLogger(AuthService.class);

    private final AuthRepository authRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AuthRepository authRepository,
                       JwtTokenProvider jwtTokenProvider,
                       PasswordEncoder passwordEncoder) {
        this.authRepository = authRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 用户登录。
     */
    public LoginResponse login(LoginRequest request) {
        if (request.getUsername() == null || request.getPassword() == null) {
            throw new IllegalArgumentException("用户名和密码不能为空");
        }

        User user = authRepository.findByUsername(request.getUsername());
        if (user == null) {
            throw new IllegalArgumentException("用户名或密码错误");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }

        List<String> roles = authRepository.findUserRoles(user.getUserId());
        String token = jwtTokenProvider.createToken(user.getUserId(), user.getUsername(), roles);

        LOG.info("用户登录成功: userId={}, username={}", user.getUserId(), user.getUsername());
        return new LoginResponse(token, user.getUserId(), user.getUsername(),
                user.getDisplayName(), user.getEmail(), roles);
    }

    /**
     * 用户注册。
     */
    @Transactional
    public LoginResponse register(RegisterRequest request) {
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new IllegalArgumentException("密码不能少于6位");
        }

        if (authRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("用户名已存在");
        }

        String userId = "user-" + UUID.randomUUID().toString().substring(0, 8);
        String passwordHash = passwordEncoder.encode(request.getPassword());

        authRepository.insertUser(userId, request.getUsername(), passwordHash,
                request.getDisplayName(), request.getEmail());

        // 根据请求中的角色分配，默认为 DEVELOPER
        String roleName = request.getRole() != null ? request.getRole().toUpperCase() : "DEVELOPER";
        String roleId = switch (roleName) {
            case "ADMIN" -> "role-admin";
            case "PROJECT_MANAGER" -> "role-pm";
            case "DEVELOPER" -> "role-dev";
            case "VIEWER" -> "role-viewer";
            default -> "role-dev";
        };
        authRepository.insertUserRole(userId, roleId);

        List<String> roles = List.of(roleName);
        String token = jwtTokenProvider.createToken(userId, request.getUsername(), roles);

        LOG.info("用户注册成功: userId={}, username={}", userId, request.getUsername());
        return new LoginResponse(token, userId, request.getUsername(),
                request.getDisplayName() != null ? request.getDisplayName() : request.getUsername(),
                request.getEmail(), roles);
    }

    /**
     * 获取用户信息（安全，不含密码）。
     */
    public User getUserById(String userId) {
        User user = authRepository.findByUserId(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        return User.safe(user);
    }

    // ======================== 项目成员管理 ========================

    public List<ProjectMember> getProjectMembers(String projectId) {
        return authRepository.listProjectMembers(projectId);
    }

    /**
     * 添加项目成员。
     */
    public void addProjectMember(String projectId, String userId, String role) {
        if (authRepository.isProjectMember(projectId, userId)) {
            throw new IllegalArgumentException("该用户已是项目成员");
        }
        authRepository.addProjectMember(projectId, userId, role);
        LOG.info("添加项目成员: projectId={}, userId={}, role={}", projectId, userId, role);
    }

    /**
     * 移除项目成员。
     */
    public void removeProjectMember(String projectId, String userId) {
        if (!authRepository.isProjectMember(projectId, userId)) {
            throw new IllegalArgumentException("该用户不是项目成员");
        }
        // 不允许移除最后一个 owner
        if ("owner".equals(authRepository.getProjectMemberRole(projectId, userId))) {
            long ownerCount = authRepository.listProjectMembers(projectId).stream()
                    .filter(m -> "owner".equals(m.getRole()))
                    .count();
            if (ownerCount <= 1) {
                throw new IllegalArgumentException("至少保留一个项目所有者(owner)");
            }
        }
        authRepository.removeProjectMember(projectId, userId);
        LOG.info("移除项目成员: projectId={}, userId={}", projectId, userId);
    }

    /**
     * 更新项目成员角色。
     */
    public void updateProjectMemberRole(String projectId, String userId, String role) {
        if (!authRepository.isProjectMember(projectId, userId)) {
            throw new IllegalArgumentException("该用户不是项目成员");
        }
        authRepository.addProjectMember(projectId, userId, role);
        LOG.info("更新项目成员角色: projectId={}, userId={}, role={}", projectId, userId, role);
    }

    /**
     * 获取所有用户列表（用于添加成员时选择）。
     */
    public List<User> listAllUsers() {
        return authRepository.listAllUsers().stream()
                .map(User::safe)
                .toList();
    }

    // ======================== 用户管理（管理员） ========================

    /**
     * 禁用用户。
     */
    public void disableUser(String userId) {
        User user = authRepository.findByUserId(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        authRepository.updateUserStatus(userId, "disabled");
        LOG.info("用户已禁用: userId={}", userId);
    }

    /**
     * 启用用户。
     */
    public void enableUser(String userId) {
        User user = authRepository.findByUserId(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        authRepository.updateUserStatus(userId, "active");
        LOG.info("用户已启用: userId={}", userId);
    }

    /**
     * 更新用户的全局角色。
     */
    public void updateUserRole(String userId, String roleName) {
        User user = authRepository.findByUserId(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        // 根据角色名查找 roleId
        String roleId = switch (roleName.toUpperCase()) {
            case "ADMIN" -> "role-admin";
            case "PROJECT_MANAGER" -> "role-pm";
            case "DEVELOPER" -> "role-dev";
            case "VIEWER" -> "role-viewer";
            default -> throw new IllegalArgumentException("无效的角色: " + roleName);
        };
        // 先清除所有角色，再分配新角色
        authRepository.clearUserRoles(userId);
        authRepository.insertUserRole(userId, roleId);
        LOG.info("用户角色已更新: userId={}, role={}", userId, roleName);
    }

    /**
     * 删除用户。
     */
    public void deleteUser(String userId) {
        User user = authRepository.findByUserId(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        authRepository.deleteUser(userId);
        LOG.info("用户已删除: userId={}", userId);
    }
}