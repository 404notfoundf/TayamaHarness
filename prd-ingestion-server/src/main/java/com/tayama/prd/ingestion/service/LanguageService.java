package com.tayama.prd.ingestion.service;

import com.tayama.prd.ingestion.model.project.Language;
import com.tayama.prd.ingestion.repository.LanguageRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 语言字典服务。
 */
@Service
public class LanguageService {

    private final LanguageRepository repo;

    public LanguageService(LanguageRepository repo) {
        this.repo = repo;
    }

    public List<Language> listLanguages() {
        return repo.findAll();
    }
}