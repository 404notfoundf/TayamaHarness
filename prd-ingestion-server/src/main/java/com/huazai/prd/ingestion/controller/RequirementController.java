package com.huazai.prd.ingestion.controller;

import com.huazai.prd.ingestion.model.pipeline.TraceNode;
import com.huazai.prd.ingestion.model.response.BaseResponse;
import com.huazai.prd.ingestion.service.PipelineService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 需求追溯 REST 控制器。
 */
@RestController
@RequestMapping("/requirements")
public class RequirementController {

    private final PipelineService pipelineService;

    public RequirementController(PipelineService pipelineService) {
        this.pipelineService = pipelineService;
    }

    /**
     * GET /api/v1/requirements/{reqId}/trace - 获取需求追溯树。
     */
    @GetMapping("/{reqId}/trace")
    @PreAuthorize("isAuthenticated() && @authz.isProjectMember()")
    public ResponseEntity<BaseResponse<TraceNode>> getRequirementTrace(@PathVariable String reqId) {
        return ResponseEntity.ok(BaseResponse.ok(pipelineService.getRequirementTrace(reqId)));
    }
}