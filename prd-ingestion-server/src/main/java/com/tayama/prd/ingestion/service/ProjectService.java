package com.tayama.prd.ingestion.service;

import com.tayama.prd.ingestion.auth.AuthRepository;
import com.tayama.prd.ingestion.model.project.Project;
import com.tayama.prd.ingestion.model.project.ProjectRequest;
import com.tayama.prd.ingestion.repository.ProjectRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 项目管理服务。
 */
@Service
public class ProjectService {

    private final ProjectRepository repo;
    private final AuthRepository authRepository;

    public ProjectService(ProjectRepository repo, AuthRepository authRepository) {
        this.repo = repo;
        this.authRepository = authRepository;
    }

    public List<Project> listProjects() {
        return repo.findAll();
    }

    public Project getProject(String projectId) {
        Project p = repo.findByProjectId(projectId);
        if (p == null) {
            throw new IllegalArgumentException("项目不存在: " + projectId);
        }
        return p;
    }

    @Transactional
    public Project createProject(ProjectRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("项目名称不能为空");
        }
        if (request.getLanguageId() == null) {
            throw new IllegalArgumentException("请选择编程语言");
        }
        String projectId = repo.generateProjectId();
        repo.insert(projectId, request.getName(), request.getDescription(),
                request.getLanguageId());
        // 保存多框架关联
        if (request.getFrameworkIds() != null && !request.getFrameworkIds().isEmpty()) {
            repo.saveProjectFrameworks(projectId, request.getFrameworkIds());
        }
        // 添加创建者为 owner
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof String userId) {
            authRepository.addProjectMember(projectId, userId, "owner");
        }
        // 添加指定的其他成员
        if (request.getMemberIds() != null) {
            for (String memberId : request.getMemberIds()) {
                if (auth == null || !memberId.equals(auth.getPrincipal())) {
                    authRepository.addProjectMember(projectId, memberId, "contributor");
                }
            }
        }
        return repo.findByProjectId(projectId);
    }

    @Transactional
    public Project updateProject(String projectId, ProjectRequest request) {
        Project existing = repo.findByProjectId(projectId);
        if (existing == null) {
            throw new IllegalArgumentException("项目不存在: " + projectId);
        }
        String name = request.getName() != null ? request.getName() : existing.getName();
        String description = request.getDescription() != null ? request.getDescription() : existing.getDescription();
        Long languageId = request.getLanguageId() != null ? request.getLanguageId() : existing.getLanguageId();
        String status = request.getStatus() != null ? request.getStatus() : existing.getStatus();

        repo.update(projectId, name, description, languageId, status);

        // 更新框架关联
        if (request.getFrameworkIds() != null) {
            repo.saveProjectFrameworks(projectId, request.getFrameworkIds());
        }

        return repo.findByProjectId(projectId);
    }

    public void archiveProject(String projectId) {
        Project existing = repo.findByProjectId(projectId);
        if (existing == null) {
            throw new IllegalArgumentException("项目不存在: " + projectId);
        }
        repo.archive(projectId);
    }
}