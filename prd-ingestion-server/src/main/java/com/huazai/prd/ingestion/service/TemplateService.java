package com.huazai.prd.ingestion.service;

import com.huazai.prd.ingestion.model.template.Template;
import com.huazai.prd.ingestion.model.template.TemplateCreateRequest;
import com.huazai.prd.ingestion.model.template.TemplateUpdateRequest;
import com.huazai.prd.ingestion.repository.TemplateRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * 模板管理服务。
 */
@Service
public class TemplateService {

    private final TemplateRepository repo;

    public TemplateService(TemplateRepository repo) {
        this.repo = repo;
    }

    public List<Template> getTemplates() {
        return repo.findAll();
    }

    public Template getTemplate(String templateId) {
        Template template = repo.findById(templateId);
        if (template != null) {
            template.setVersions(repo.findVersions(templateId));
        }
        return template;
    }

    public Template createTemplate(TemplateCreateRequest request) {
        String templateId = "tpl-" + Instant.now().toString()
                .replace("-", "").replace(":", "").replace(".", "")
                + (int)(Math.random() * 1000);

        repo.insert(templateId, request.getType(), request.getName(),
                request.getDescription(), request.getContent(), "system");
        return repo.findById(templateId);
    }

    public Template updateTemplate(String templateId, TemplateUpdateRequest request) {
        repo.update(templateId, request.getContent(), request.getChangeLog(), "system");
        Template template = repo.findById(templateId);
        if (template != null) {
            template.setVersions(repo.findVersions(templateId));
        }
        return template;
    }
}