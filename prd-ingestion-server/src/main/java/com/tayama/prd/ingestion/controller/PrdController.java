package com.tayama.prd.ingestion.controller;

import com.tayama.prd.ingestion.config.ProjectContext;
import com.tayama.prd.ingestion.model.prd.ConfirmCandidateRequest;
import com.tayama.prd.ingestion.model.prd.IngestionProgress;
import com.tayama.prd.ingestion.model.prd.PrdIngestRequest;
import com.tayama.prd.ingestion.model.prd.PrdIngestResponse;
import com.tayama.prd.ingestion.model.response.BaseResponse;
import com.tayama.prd.ingestion.service.PrdIngestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * PRD 导入与解析 REST 控制器。
 *
 * 端点对照前端 API 契约 §1。
 */
@RestController
@RequestMapping("/prd")
public class PrdController {

    private static final Logger LOG = LoggerFactory.getLogger(PrdController.class);

    private final PrdIngestionService service;

    public PrdController(PrdIngestionService service) {
        this.service = service;
    }

    /**
     * POST /api/v1/prd/ingest - 上传并解析 PRD。
     *
     * <p>支持两种方式：</p>
     * <ul>
     *   <li>直接传 content（文本内容）</li>
     *   <li>传 fileMd5（从 MinIO 读取已上传文件内容）</li>
     * </ul>
     */
    @PostMapping("/ingest")
    @PreAuthorize("isAuthenticated() && @authz.hasProjectRole('owner', 'maintainer', 'contributor')")
    public ResponseEntity<BaseResponse<PrdIngestResponse>> ingest(@RequestBody PrdIngestRequest request) {
        long startMs = System.currentTimeMillis();
        boolean hasContent = request.getContent() != null && !request.getContent().isBlank();
        boolean hasFileMd5 = request.getFileMd5() != null && !request.getFileMd5().isBlank();
        if (!hasContent && !hasFileMd5) {
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error("INVALID_REQUEST", "PRD 内容或 fileMd5 不能为空，请至少提供一项"));
        }
        // projectId 兜底：body 未传时从请求头 X-Project-Id（前端自动注入）获取
        if (request.getProjectId() == null || request.getProjectId().isBlank()) {
            request.setProjectId(ProjectContext.get());
        }
        if (request.getProjectId() == null || request.getProjectId().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error("INVALID_REQUEST", "projectId 不能为空，请先选择项目"));
        }
        LOG.info("[ingest-api] 请求进入: projectId={}, title={}, hasContent={}, contentLength={}, fileMd5={}",
                request.getProjectId(), request.getTitle(), hasContent,
                request.getContent() == null ? 0 : request.getContent().length(), request.getFileMd5());
        try {
            PrdIngestResponse result = service.ingest(request);
            LOG.info("[ingest-api] 返回成功: ingestionId={}, status={}, 耗时={}ms",
                    result.getIngestionId(), result.getStatus(), System.currentTimeMillis() - startMs);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(BaseResponse.ok("accepted", result));
        } catch (IllegalArgumentException e) {
            LOG.warn("[ingest-api] 参数错误: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error("INVALID_REQUEST", e.getMessage()));
        } catch (Exception e) {
            LOG.error("[ingest-api] 解析异常: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * GET /api/v1/prd/ingest/{ingestionId}/progress - 查询解析进度。
     */
    @GetMapping("/ingest/{ingestionId}/progress")
    @PreAuthorize("isAuthenticated() && @authz.isProjectMember()")
    public ResponseEntity<BaseResponse<IngestionProgress>> getProgress(@PathVariable String ingestionId) {
        IngestionProgress progress = service.getProgress(ingestionId);
        if (progress == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(BaseResponse.error("NOT_FOUND", "解析记录不存在"));
        }
        return ResponseEntity.ok(BaseResponse.ok(progress));
    }

    /**
     * POST /api/v1/prd/ingest/{ingestionId}/confirm - 人工确认候选条目（proposed → confirmed / accepted）。
     */
    @PostMapping("/ingest/{ingestionId}/confirm")
    @PreAuthorize("isAuthenticated() && @authz.hasProjectRole('owner', 'maintainer', 'contributor')")
    public ResponseEntity<BaseResponse<Integer>> confirmCandidate(@PathVariable String ingestionId,
                                                                   @RequestBody ConfirmCandidateRequest request) {
        if (request == null || request.getType() == null || request.getType().isBlank()
                || request.getId() == null || request.getId().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error("INVALID_REQUEST", "type 与 id 不能为空"));
        }
        int updated = service.confirmCandidate(ingestionId, request.getType(), request.getId());
        if (updated <= 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(BaseResponse.error("NOT_FOUND", "未找到候选条目或确认失败"));
        }
        LOG.info("[confirm-api] ingestionId={}, type={}, id={}, updated={}",
                ingestionId, request.getType(), request.getId(), updated);
        return ResponseEntity.ok(BaseResponse.ok(updated));
    }

    /**
     * GET /api/v1/prd/ingest/{ingestionId} - 获取解析结果。
     */
    @GetMapping("/ingest/{ingestionId}")
    @PreAuthorize("isAuthenticated() && @authz.isProjectMember()")
    public ResponseEntity<BaseResponse<PrdIngestResponse>> getResult(@PathVariable String ingestionId) {
        PrdIngestResponse result = service.getIngestionResult(ingestionId);
        if (result == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(BaseResponse.error("NOT_FOUND", "解析结果不存在"));
        }
        return ResponseEntity.ok(BaseResponse.ok(result));
    }

    /**
     * POST /api/v1/prd/ingest/{ingestionId}/reparse - 重新解析。
     */
    @PostMapping("/ingest/{ingestionId}/reparse")
    @PreAuthorize("isAuthenticated() && @authz.hasProjectRole('owner', 'maintainer')")
    public ResponseEntity<BaseResponse<PrdIngestResponse>> reparse(@PathVariable String ingestionId) {
        PrdIngestResponse result = service.reparse(ingestionId);
        if (result == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(BaseResponse.error("NOT_FOUND", "解析记录不存在"));
        }
        return ResponseEntity.ok(BaseResponse.ok("正在重新解析...", result));
    }
}