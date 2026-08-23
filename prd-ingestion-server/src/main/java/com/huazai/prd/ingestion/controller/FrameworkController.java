package com.huazai.prd.ingestion.controller;

import com.huazai.prd.ingestion.model.project.Framework;
import com.huazai.prd.ingestion.model.response.BaseResponse;
import com.huazai.prd.ingestion.service.FrameworkService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 框架字典 REST 控制器（只读，按语言过滤）。
 */
@RestController
@RequestMapping("/frameworks")
public class FrameworkController {

    private final FrameworkService service;

    public FrameworkController(FrameworkService service) {
        this.service = service;
    }

    /** GET /api/v1/frameworks?languageId=1 - 框架列表 */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BaseResponse<List<Framework>>> listFrameworks(
            @RequestParam(required = false) Long languageId) {
        return ResponseEntity.ok(BaseResponse.ok(service.listFrameworks(languageId)));
    }
}