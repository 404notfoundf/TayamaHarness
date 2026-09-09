package com.tayama.prd.ingestion.controller;

import com.tayama.prd.ingestion.config.ProjectContext;
import com.tayama.prd.ingestion.model.document.DocumentApprovalRequest;
import com.tayama.prd.ingestion.model.document.DocumentApprovalResponse;
import com.tayama.prd.ingestion.model.document.DocumentUpdateRequest;
import com.tayama.prd.ingestion.model.document.WikiDocument;
import com.tayama.prd.ingestion.model.prd.DocumentSummary;
import com.tayama.prd.ingestion.model.response.BaseResponse;
import com.tayama.prd.ingestion.service.DocumentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文档管理 REST 控制器。
 *
 * 端点对照前端 API 契约 §2。
 */
@RestController
@RequestMapping("/documents")
public class DocumentController {

    private final DocumentService service;

    public DocumentController(DocumentService service) {
        this.service = service;
    }

    /**
     * GET /api/v1/documents?ingestionId=xxx - 获取文档列表。
     * <p>ingestionId 可选：为空时按当前项目（X-Project-Id）返回项目全部文档。</p>
     */
    @GetMapping
    @PreAuthorize("isAuthenticated() && @authz.isProjectMember()")
    public ResponseEntity<BaseResponse<List<DocumentSummary>>> getDocuments(
            @RequestParam(value = "ingestionId", required = false) String ingestionId) {
        return ResponseEntity.ok(BaseResponse.ok(service.getDocuments(ingestionId, ProjectContext.get())));
    }

    /**
     * GET /api/v1/documents/{docId} - 获取文档详情。
     */
    @GetMapping("/{docId}")
    @PreAuthorize("isAuthenticated() && @authz.isProjectMember()")
    public ResponseEntity<BaseResponse<WikiDocument>> getDocument(@PathVariable String docId) {
        WikiDocument doc = service.getDocument(docId);
        if (doc == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(BaseResponse.error("NOT_FOUND", "文档不存在"));
        }
        return ResponseEntity.ok(BaseResponse.ok(doc));
    }

    /**
     * PUT /api/v1/documents/{docId} - 更新文档内容。
     */
    @PutMapping("/{docId}")
    @PreAuthorize("isAuthenticated() && @authz.hasProjectRole('owner', 'maintainer')")
    public ResponseEntity<BaseResponse<WikiDocument>> updateDocument(
            @PathVariable String docId, @RequestBody DocumentUpdateRequest request) {
        WikiDocument doc = service.updateDocument(docId, request.getContent(), request.getChangeLog());
        return ResponseEntity.ok(BaseResponse.ok(doc));
    }

    /**
     * POST /api/v1/documents/{docId}/approval - 审批文档。
     */
    @PostMapping("/{docId}/approval")
    @PreAuthorize("isAuthenticated() && @authz.hasProjectRole('owner', 'maintainer')")
    public ResponseEntity<BaseResponse<DocumentApprovalResponse>> approveDocument(
            @PathVariable String docId, @RequestBody DocumentApprovalRequest request) {
        DocumentApprovalResponse result = service.approveDocument(docId, request.getAction(), request.getComment());
        return ResponseEntity.ok(BaseResponse.ok(result));
    }
}