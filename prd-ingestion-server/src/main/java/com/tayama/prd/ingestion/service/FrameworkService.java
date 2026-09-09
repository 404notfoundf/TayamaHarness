package com.tayama.prd.ingestion.service;

import com.tayama.prd.ingestion.model.project.Framework;
import com.tayama.prd.ingestion.repository.FrameworkRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 框架字典服务。
 */
@Service
public class FrameworkService {

    private final FrameworkRepository repo;

    public FrameworkService(FrameworkRepository repo) {
        this.repo = repo;
    }

    public List<Framework> listFrameworks(Long languageId) {
        if (languageId != null) {
            return repo.findByLanguageId(languageId);
        }
        return repo.findAll();
    }
}