package com.huazai.prd.ingestion.controller;

import com.huazai.prd.ingestion.model.project.Project;
import com.huazai.prd.ingestion.model.project.ProjectRequest;
import com.huazai.prd.ingestion.model.response.BaseResponse;
import com.huazai.prd.ingestion.service.ProjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 项目管理 REST 控制器。
 */
@RestController
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService service;

    public ProjectController(ProjectService service) {
        this.service = service;
    }

    /** GET /api/v1/projects - 项目列表 */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BaseResponse<List<Project>>> listProjects() {
        return ResponseEntity.ok(BaseResponse.ok(service.listProjects()));
    }

    /** GET /api/v1/projects/{projectId} - 项目详情 */
    @GetMapping("/{projectId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BaseResponse<Project>> getProject(@PathVariable String projectId) {
        try {
            return ResponseEntity.ok(BaseResponse.ok(service.getProject(projectId)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(BaseResponse.error("NOT_FOUND", e.getMessage()));
        }
    }

    /** POST /api/v1/projects - 创建项目 */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BaseResponse<Project>> createProject(@RequestBody ProjectRequest request) {
        try {
            Project project = service.createProject(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(BaseResponse.ok("created", project));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error("INVALID_REQUEST", e.getMessage()));
        }
    }

    /** PUT /api/v1/projects/{projectId} - 更新项目 */
    @PutMapping("/{projectId}")
    @PreAuthorize("isAuthenticated() && @authz.hasProjectRole('owner', 'maintainer')")
    public ResponseEntity<BaseResponse<Project>> updateProject(
            @PathVariable String projectId, @RequestBody ProjectRequest request) {
        try {
            Project project = service.updateProject(projectId, request);
            return ResponseEntity.ok(BaseResponse.ok(project));
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("不存在")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(BaseResponse.error("NOT_FOUND", e.getMessage()));
            }
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error("INVALID_REQUEST", e.getMessage()));
        }
    }

    /** DELETE /api/v1/projects/{projectId} - 归档项目 */
    @DeleteMapping("/{projectId}")
    @PreAuthorize("isAuthenticated() && @authz.hasProjectRole('owner')")
    public ResponseEntity<BaseResponse<Void>> archiveProject(@PathVariable String projectId) {
        try {
            service.archiveProject(projectId);
            return ResponseEntity.ok(BaseResponse.ok("archived", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(BaseResponse.error("NOT_FOUND", e.getMessage()));
        }
    }
}