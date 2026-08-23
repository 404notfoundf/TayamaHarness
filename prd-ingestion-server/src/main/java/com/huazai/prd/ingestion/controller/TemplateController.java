package com.huazai.prd.ingestion.controller;

import com.huazai.prd.ingestion.model.response.BaseResponse;
import com.huazai.prd.ingestion.model.template.Template;
import com.huazai.prd.ingestion.model.template.TemplateCreateRequest;
import com.huazai.prd.ingestion.model.template.TemplateUpdateRequest;
import com.huazai.prd.ingestion.service.TemplateService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 模板管理 REST 控制器。
 *
 * 端点对照前端 API 契约 §5。
 */
@RestController
@RequestMapping("/templates")
public class TemplateController {

    private final TemplateService service;

    public TemplateController(TemplateService service) {
        this.service = service;
    }

    /**
     * GET /api/v1/templates - 获取模板列表。
     */
    @GetMapping
    @PreAuthorize("isAuthenticated() && @authz.isProjectMember()")
    public ResponseEntity<BaseResponse<List<Template>>> getTemplates() {
        return ResponseEntity.ok(BaseResponse.ok(service.getTemplates()));
    }

    /**
     * GET /api/v1/templates/{templateId} - 获取模板详情。
     */
    @GetMapping("/{templateId}")
    @PreAuthorize("isAuthenticated() && @authz.isProjectMember()")
    public ResponseEntity<BaseResponse<Template>> getTemplate(@PathVariable String templateId) {
        Template template = service.getTemplate(templateId);
        if (template == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(BaseResponse.error("NOT_FOUND", "模板不存在"));
        }
        return ResponseEntity.ok(BaseResponse.ok(template));
    }

    /**
     * POST /api/v1/templates - 新建模板。
     */
    @PostMapping
    @PreAuthorize("isAuthenticated() && @authz.hasProjectRole('owner', 'maintainer')")
    public ResponseEntity<BaseResponse<Template>> createTemplate(@RequestBody TemplateCreateRequest request) {
        if (request.getContent() == null || request.getContent().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(BaseResponse.error("INVALID_REQUEST", "模板内容不能为空"));
        }
        Template result = service.createTemplate(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.ok(result));
    }

    /**
     * PUT /api/v1/templates/{templateId} - 更新模板。
     */
    @PutMapping("/{templateId}")
    @PreAuthorize("isAuthenticated() && @authz.hasProjectRole('owner', 'maintainer')")
    public ResponseEntity<BaseResponse<Template>> updateTemplate(
            @PathVariable String templateId, @RequestBody TemplateUpdateRequest request) {
        Template result = service.updateTemplate(templateId, request);
        return ResponseEntity.ok(BaseResponse.ok(result));
    }
}