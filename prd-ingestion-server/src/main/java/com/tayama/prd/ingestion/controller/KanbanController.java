package com.tayama.prd.ingestion.controller;

import com.tayama.prd.ingestion.model.pipeline.KanbanStats;
import com.tayama.prd.ingestion.model.response.BaseResponse;
import com.tayama.prd.ingestion.service.PipelineService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 看板统计 REST 控制器。
 */
@RestController
@RequestMapping("/kanban")
public class KanbanController {

    private final PipelineService pipelineService;

    public KanbanController(PipelineService pipelineService) {
        this.pipelineService = pipelineService;
    }

    /**
     * GET /api/v1/kanban/stats - 获取看板统计。
     */
    @GetMapping("/stats")
    @PreAuthorize("isAuthenticated() && @authz.isProjectMember()")
    public ResponseEntity<BaseResponse<KanbanStats>> getKanbanStats() {
        return ResponseEntity.ok(BaseResponse.ok(pipelineService.getKanbanStats()));
    }
}