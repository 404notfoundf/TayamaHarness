package com.tayama.prd.ingestion.controller;

import com.tayama.prd.ingestion.model.pipeline.*;
import com.tayama.prd.ingestion.model.response.BaseResponse;
import com.tayama.prd.ingestion.service.PipelineService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 流水线管理 REST 控制器。
 *
 * 端点对照前端 API 契约 §4。
 */
@RestController
@RequestMapping("/pipeline")
public class PipelineController {

    private final PipelineService service;

    public PipelineController(PipelineService service) {
        this.service = service;
    }

    /**
     * GET /api/v1/pipeline/{changeId} - 获取流水线状态。
     */
    @GetMapping("/{changeId}")
    @PreAuthorize("isAuthenticated() && @authz.isProjectMember()")
    public ResponseEntity<BaseResponse<PipelineStatus>> getPipelineStatus(@PathVariable String changeId) {
        PipelineStatus status = service.getPipelineStatus(changeId);
        if (status == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(BaseResponse.error("NOT_FOUND", "流水线不存在"));
        }
        return ResponseEntity.ok(BaseResponse.ok(status));
    }

    /**
     * GET /api/v1/pipeline/{changeId}/logs - 获取变更日志。
     */
    @GetMapping("/{changeId}/logs")
    @PreAuthorize("isAuthenticated() && @authz.isProjectMember()")
    public ResponseEntity<BaseResponse<List<ChangeLogEntry>>> getChangeLogs(@PathVariable String changeId) {
        return ResponseEntity.ok(BaseResponse.ok(service.getChangeLogs(changeId)));
    }

    /**
     * POST /api/v1/pipeline/{changeId}/advance - 推动流水线。
     */
    @PostMapping("/{changeId}/advance")
    @PreAuthorize("isAuthenticated() && @authz.hasProjectRole('owner', 'maintainer')")
    public ResponseEntity<BaseResponse<PipelineStatus>> advancePipeline(
            @PathVariable String changeId, @RequestBody AdvancePipelineRequest request) {
        PipelineStatus status = service.advancePipeline(changeId, request.getStage());
        return ResponseEntity.ok(BaseResponse.ok(status));
    }

    /**
     * GET /api/v1/requirements/{reqId}/trace - 获取需求追溯树。
     * 注意：此端点 path 前缀为 /requirements，但映射在 PipelineController 中
     * 为了保持清晰，放在此控制器中。
     */
    @GetMapping("/../requirements/{reqId}/trace")
    @PreAuthorize("isAuthenticated() && @authz.isProjectMember()")
    public ResponseEntity<BaseResponse<TraceNode>> getRequirementTrace(@PathVariable String reqId) {
        return ResponseEntity.ok(BaseResponse.ok(service.getRequirementTrace(reqId)));
    }

    /**
     * GET /api/v1/kanban/stats - 获取看板统计。
     */
    @GetMapping("/../kanban/stats")
    @PreAuthorize("isAuthenticated() && @authz.isProjectMember()")
    public ResponseEntity<BaseResponse<KanbanStats>> getKanbanStats() {
        return ResponseEntity.ok(BaseResponse.ok(service.getKanbanStats()));
    }
}