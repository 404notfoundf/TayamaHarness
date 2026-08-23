package com.huazai.prd.ingestion.controller;

import com.huazai.prd.ingestion.model.project.Language;
import com.huazai.prd.ingestion.model.response.BaseResponse;
import com.huazai.prd.ingestion.service.LanguageService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 语言字典 REST 控制器（只读）。
 */
@RestController
@RequestMapping("/languages")
public class LanguageController {

    private final LanguageService service;

    public LanguageController(LanguageService service) {
        this.service = service;
    }

    /** GET /api/v1/languages - 语言列表 */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BaseResponse<List<Language>>> listLanguages() {
        return ResponseEntity.ok(BaseResponse.ok(service.listLanguages()));
    }
}