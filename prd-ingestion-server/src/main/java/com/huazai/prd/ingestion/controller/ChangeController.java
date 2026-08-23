package com.huazai.prd.ingestion.controller;

import com.huazai.prd.ingestion.model.change.ChangeCreateRequest;
import com.huazai.prd.ingestion.model.change.ChangeDetail;
import com.huazai.prd.ingestion.model.change.ChangeSummary;
import com.huazai.prd.ingestion.model.response.BaseResponse;
import com.huazai.prd.ingestion.service.ChangeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Change 管理 REST 控制器。
 *
 * 端点对照前端 API 契约 §3。
 */
@RestController
@RequestMapping("/changes")
public class ChangeController {

    private final ChangeService service;

    public ChangeController(ChangeService service) {
        this.service = service;
    }

    /**
     * POST /api/v1/changes - 生成 Change。
     */
    @PostMapping
    @PreAuthorize("isAuthenticated() && @authz.hasProjectRole('owner', 'maintainer', 'contributor')")
    public ResponseEntity<BaseResponse<ChangeDetail>> createChange(@RequestBody ChangeCreateRequest request) {
        if (request.getIngestionId() == null || request.getIngestionId().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error("INVALID_REQUEST", "ingestionId 不能为空"));
        }
        ChangeDetail result = service.createChange(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.ok(result));
    }

    /**
     * GET /api/v1/changes?page=0&size=20 - 获取 Change 列表。
     */
    @GetMapping
    @PreAuthorize("isAuthenticated() && @authz.isProjectMember()")
    public ResponseEntity<BaseResponse<List<ChangeSummary>>> getChanges(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(BaseResponse.ok(service.getChanges(page, size)));
    }

    /**
     * GET /api/v1/changes/{changeId} - 获取 Change 详情。
     */
    @GetMapping("/{changeId}")
    @PreAuthorize("isAuthenticated() && @authz.isProjectMember()")
    public ResponseEntity<BaseResponse<ChangeDetail>> getChangeDetail(@PathVariable String changeId) {
        ChangeDetail detail = service.getChangeDetail(changeId);
        if (detail == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(BaseResponse.error("NOT_FOUND", "Change 不存在"));
        }
        return ResponseEntity.ok(BaseResponse.ok(detail));
    }

    /**
     * PUT /api/v1/changes/{changeId} - 手动更新 Change 内容（人工编辑后保存）。
     */
    @PutMapping("/{changeId}")
    @PreAuthorize("isAuthenticated() && @authz.hasProjectRole('owner', 'maintainer', 'contributor')")
    public ResponseEntity<BaseResponse<ChangeDetail>> updateChangeContent(
            @PathVariable String changeId, @RequestBody Map<String, String> body) {
        String content = body.get("content");
        if (content == null) {
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error("INVALID_REQUEST", "content 不能为空"));
        }
        ChangeDetail detail = service.updateChangeContent(changeId, content);
        if (detail == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(BaseResponse.error("NOT_FOUND", "Change 不存在"));
        }
        return ResponseEntity.ok(BaseResponse.ok(detail));
    }
}