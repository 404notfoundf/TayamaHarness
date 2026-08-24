package com.huazai.prd.ingestion.service;

import com.huazai.prd.ingestion.config.AiProperties;
import com.huazai.prd.ingestion.model.prd.*;
import com.huazai.prd.ingestion.repository.PrdIngestionRepository;
import com.huazai.prd.ingestion.repository.TemplateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * PRD 导入与解析服务。
 *
 * <p>支持两种模式：</p>
 * <ul>
 *   <li>直接传入 content 文本（原有方式）</li>
 *   <li>通过 fileMd5 从 MinIO 读取文件内容（分块上传方式）</li>
 * </ul>
 */
@Service
public class PrdIngestionService {

    private static final Logger LOG = LoggerFactory.getLogger(PrdIngestionService.class);

    private static final com.fasterxml.jackson.databind.ObjectMapper OBJECT_MAPPER = new com.fasterxml.jackson.databind.ObjectMapper();

    private final PrdIngestionRepository repo;
    private final FileUploadService fileUploadService;
    private final AiProperties aiProperties;
    private final DomainTemplateRegistry templateRegistry;
    private final Optional<AiService> aiService;
    private final TemplateRepository templateRepository;

    public PrdIngestionService(PrdIngestionRepository repo, FileUploadService fileUploadService,
                               AiProperties aiProperties,
                               DomainTemplateRegistry templateRegistry,
                               @Autowired(required = false) AiService aiService,
                               TemplateRepository templateRepository) {
        this.repo = repo;
        this.fileUploadService = fileUploadService;
        this.aiProperties = aiProperties;
        this.templateRegistry = templateRegistry;
        this.aiService = Optional.ofNullable(aiService);
        this.templateRepository = templateRepository;
    }

    /**
     * 直接上传 PRD 文本内容并解析。
     */
    public PrdIngestResponse ingest(PrdIngestRequest request) {
        long startMs = System.currentTimeMillis();
        String projectId = request.getProjectId();
        if (projectId == null || projectId.isBlank()) {
            throw new IllegalArgumentException("请选择所属项目");
        }

        LOG.info("[ingest] 收到解析请求: projectId={}, title={}, format={}, contentLength={}, fileMd5={}",
                projectId, request.getTitle(), request.getFormat(),
                request.getContent() == null ? 0 : request.getContent().length(),
                request.getFileMd5());

        String ingestionId = generateIngestionId();
        String title = request.getTitle() != null ? request.getTitle() : "未命名 PRD";
        String format = request.getFormat() != null ? request.getFormat() : "markdown";

        // 如果传了 fileMd5，从 MinIO 读取文件内容
        String content = request.getContent();
        String fileMd5 = request.getFileMd5();
        if (fileMd5 != null && !fileMd5.isBlank()) {
            content = fileUploadService.readFileContent(fileMd5);
            // 从 MinIO 读取文件名
            String minioFileName = fileUploadService.getFileName(fileMd5);
            if (minioFileName != null && (title == null || title.isBlank() || "未命名 PRD".equals(title))) {
                title = minioFileName;
            }
        }

        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("PRD 内容不能为空");
        }

        // 创建导入记录
        repo.insertIngestion(projectId, ingestionId, title, content, format, request.getSourceUrl(), "pending");

        // 将原始 PRD 内容持久化到 MinIO 备份（无论来源是文本粘贴还是文件上传）
        fileUploadService.savePrdContent(ingestionId, title, content);

        // 解析 PRD 内容
        try {
            simulateParsing(projectId, ingestionId, content, title);
        } catch (Exception e) {
            LOG.error("[parse] 解析异常: ingestionId={}, error={}", ingestionId, e.getMessage(), e);
            // 更新状态为失败，但继续返回已解析的结果
            repo.updateStatus(ingestionId, "failed", 0, "解析异常: " + e.getMessage());
        }

        PrdIngestResponse result = getIngestionResult(ingestionId);

        LOG.info("[ingest] 解析完成: ingestionId={}, status={}, requirements={}, dataEntities={}, interfaces={}, archDecisions={}, documents={}, 耗时={}ms",
                ingestionId, result.getStatus(),
                result.getRequirements() == null ? 0 : result.getRequirements().size(),
                result.getDataEntities() == null ? 0 : result.getDataEntities().size(),
                result.getInterfaces() == null ? 0 : result.getInterfaces().size(),
                result.getArchitectureDecisions() == null ? 0 : result.getArchitectureDecisions().size(),
                result.getDocuments() == null ? 0 : result.getDocuments().size(),
                System.currentTimeMillis() - startMs);
        return result;
    }

    /**
     * 生成全局唯一的导入记录 ID：秒级时间戳 + UUID 片段。
     *
     * <p>旧实现使用进程内静态计数器（重启即归零）+ 秒级时间戳，导致不同进程在同一秒内
     * 可能生成相同的 ingestionId，进而使 REQ-xxx/AD-xxx/doc-xxx 业务 ID 碰撞，
     * 触发 uk_req_id / uk_ad_id / uk_doc_id 唯一索引冲突。</p>
     */
    static String generateIngestionId() {
        String ts = Instant.now().toString().replace("-", "").replace(":", "").replace(".", "");
        if (ts.length() > 15) {
            ts = ts.substring(0, 15);
        }
        return "ing-" + ts + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * 由 ingestionId 推导稳定且唯一的业务 ID 键（去掉 "ing-" 前缀）。
     *
     * <p>业务 ID（REQ-/AD-/doc-）一律基于完整 ingestionId 生成；旧实现只取 ingestionId
     * 后 6 位（秒末两位 + 进程内计数器），跨进程重启后极易重复。</p>
     */
    static String idKey(String ingestionId) {
        return ingestionId != null && ingestionId.startsWith("ing-")
                ? ingestionId.substring("ing-".length())
                : ingestionId;
    }

    /**
     * 模拟解析逻辑：将 PRD 内容按段落解析为需求、实体、接口、决策。
     */
    private void simulateParsing(String projectId, String ingestionId, String content, String title) {
        repo.updateStatus(ingestionId, "parsing", 10, "正在解析文档结构...");

        // 按空行分割段落
        String[] paragraphs = content.split("\\n\\s*\\n");
        List<String> paraList = Arrays.stream(paragraphs)
                .map(String::trim)
                .filter(p -> !p.isEmpty())
                .collect(Collectors.toList());
        LOG.info("[parse] ingestionId={}, 待解析段落数={}, 内容长度={}", ingestionId, paraList.size(), content.length());

        String key = idKey(ingestionId);

        // ---- 混合解析（Hybrid）: 模板 -> LLM -> 章节层级算法 ----
        // 旧实现：content 长度超过 short-content-threshold(默认200) 时跳过模板+LLM，
        // 直接关键词回退——导致长 PRD（几乎全部真实 PRD）永远提不出数据模型/接口协议。
        // 现在始终走模板→LLM→章节层级算法，超长内容在调用 LLM 时截断（见 truncateForLlm）。
        boolean useEnhanced = true;
        // LLM 是否解析成功：成功时旁路存结果并标记 llm，但文档渲染仍走确定性章节算法
        boolean llmSucceeded = false;

        if (useEnhanced) {
            LOG.info("[parse] ingestionId={}, 启动增强解析(模板→LLM, 内容长度={})", ingestionId, content.length());

            // 第一步：尝试模板匹配
            PrdParseResult templateResult = templateRegistry.match(content);
            if (templateResult != null) {
                LOG.info("[parse] ingestionId={}, 模板匹配成功, 写入数据库", ingestionId);
                writeParseResult(projectId, ingestionId, key, templateResult);
                repo.updateStatus(ingestionId, "generating", 90, "正在生成文档...");
                generateDocuments(projectId, ingestionId, title);
                repo.updateStatus(ingestionId, "completed", 100, "解析完成");
                LOG.info("[parse] ingestionId={}, 模板解析完成, status=completed", ingestionId);
                return;
            }

            // 第二步：尝试 LLM 解析
            if (aiProperties.isEnabled()) {
                repo.updateStatus(ingestionId, "extracting", 40, "正在调用 AI 分析...");
                LOG.info("[parse] ingestionId={}, 模板未命中, 尝试 LLM 解析", ingestionId);
                PrdParseResult llmResult = null;
                try {
                    // 直接传完整 PRD 内容给 LLM，不做截断——base64 图片占 93% 内容，截断后只剩 2600 字符
                    // LLM 需要看到完整上下文才能准确提取需求
                    llmResult = aiService.map(s -> s.parse(content)).orElse(null);
                } catch (RuntimeException e) {
                    // LLM 网络超时/服务不可用：必须兜底到章节层级算法，
                    // 否则 reparse 会先在 deleteParsedData 删光数据后中断，导致拆分结果永久丢失。
                    LOG.warn("[parse] ingestionId={}, LLM 调用异常, 回退到章节层级算法: {}", ingestionId, e.getMessage());
                    repo.updateParseMeta(ingestionId, "section_hierarchy", "LLM 调用异常：" + e.getMessage());
                }
                if (llmResult != null) {
                    // LLM 成功：结果仅旁路保存供前端左侧“LLM 提取结果”展示，
                    // 不写入 4 张结构化表、不参与文档渲染——4 份文档内容始终由确定性章节算法 + 模板生成，保证 LLM 成败结果严格一致。
                    LOG.info("[parse] ingestionId={}, LLM 解析成功(仅用于左侧展示, 不参与文档渲染)", ingestionId);
                    saveLlmResult(ingestionId, llmResult);
                    llmSucceeded = true;
                } else {
                    // LLM 调用异常或未返回有效结果：仅在未成功时标记回退章节算法，
                    // 避免 LLM 成功后 parse_source 被误覆盖为 section_hierarchy。
                    LOG.warn("[parse] ingestionId={}, LLM 解析失败, 回退到章节层级算法", ingestionId);
                    String aiError = aiService.map(AiService::getLastError).orElse(null);
                    repo.updateParseMeta(ingestionId, "section_hierarchy",
                            "LLM 解析未返回有效结果：" + (aiError != null && !aiError.isBlank() ? aiError : "未知原因"));
                }
            } else {
                LOG.info("[parse] ingestionId={}, LLM 未启用, 走章节层级算法", ingestionId);
            }
        }

        // ---- 第三步：章节树流水线（确定性解析；LLM 成败与否都执行，保证四份文档可完整拆解） ----
        repo.updateStatus(ingestionId, "parsing", 15, "正在章节树解析...");
        // 解析来源标记：LLM 成功时标记 llm（供前端展示旁路的 LLM 提取结果）；失败/未启用标记 section_hierarchy。
        repo.updateParseSource(ingestionId, llmSucceeded ? "llm" : "section_hierarchy");
        if (llmSucceeded) {
            repo.updateParseMeta(ingestionId, "llm", null);
        }

        // 章节树 → 4 个正交生成器（业务模型/数据模型/接口协议/架构决策）
        java.util.List<SectionNode> tree = PrdSectionParser.parse(paraList);
        ParseOutcome outcome = new ParseOutcome();
        outcome.requirements.addAll(RequirementExtractor.extract(tree));
        outcome.dataEntities.addAll(DataEntityExtractor.extract(tree));
        outcome.interfaces.addAll(InterfaceExtractor.extract(tree, outcome.dataEntities));
        outcome.archDecisions.addAll(ArchDecisionExtractor.extract(tree));

        repo.updateStatus(ingestionId, "extracting", 40, "正在落库提取结果...");
        // 需求（业务模型）：同一特性章节聚合为一条需求，避免段落直抄碎片
        int reqOrder = 0;
        int reqCounter = 0;
        for (RequirementEntity req : outcome.requirements) {
            reqCounter++;
            String reqId = "REQ-" + key + "-" + String.format("%03d", reqCounter);
            repo.insertRequirement(projectId, ingestionId, reqId, truncate(req.getDescription(), 500),
                    req.getPriority(), "[]", req.getSourceParagraph(), req.getNotes(), reqOrder++,
                    req.getCandidateStatus());
        }
        LOG.info("[parse] ingestionId={}, 需求解析完成: {} 条", ingestionId, reqCounter);

        // 数据实体（数据模型）：数据模型章节 confirmed + 业务候选 proposed
        int entityOrder = 0;
        for (DataEntity e : outcome.dataEntities) {
            long entityId = repo.insertDataEntity(projectId, ingestionId, e.getName(),
                    truncate(e.getDescription(), 500), entityOrder++, e.getSourceParagraph(), e.getCandidateStatus());
            int attrOrder = 0;
            for (DataEntity.Attribute attr : (e.getAttributes() == null ? Collections.<DataEntity.Attribute>emptyList() : e.getAttributes())) {
                repo.insertEntityAttribute(entityId, attr.getName(), attr.getType(), attr.getDescription(), attrOrder++);
            }
            int relOrder = 0;
            for (DataEntity.Relation rel : (e.getRelations() == null ? Collections.<DataEntity.Relation>emptyList() : e.getRelations())) {
                repo.insertEntityRelation(entityId, rel.getTarget(), rel.getType(), rel.getDescription(), relOrder++);
            }
        }
        LOG.info("[parse] ingestionId={}, 数据实体解析完成: {} 个", ingestionId, entityOrder);

        // 接口协议：接口章节 confirmed + 实体 CRUD 推导候选 proposed
        repo.updateStatus(ingestionId, "extracting", 60, "正在提取接口协议...");
        int ifOrder = 0;
        for (InterfaceProtocol ip : outcome.interfaces) {
            repo.insertInterface(projectId, ingestionId, ip.getMethod(), ip.getPath(), truncate(ip.getSummary(), 200),
                    ip.getRequestBody(), ip.getResponseBody(), ip.getNotes(), ifOrder++, ip.getSourceParagraph(),
                    ip.getCandidateStatus());
        }
        LOG.info("[parse] ingestionId={}, 接口解析完成: {} 个", ingestionId, ifOrder);

        // 架构决策：非功能需求章节推导为候选 ADR（proposed），避免空模板
        repo.updateStatus(ingestionId, "generating", 80, "正在生成架构决策...");
        int adCounter = 0;
        for (ArchitectureDecision ad : outcome.archDecisions) {
            adCounter++;
            String adId = "AD-" + key + "-" + String.format("%03d", adCounter);
            repo.insertArchDecision(projectId, ingestionId, adId, ad.getTitle(),
                    truncate(ad.getContext(), 300), truncate(ad.getDecision(), 500),
                    toJsonArray(ad.getConsequences()), ad.getStatus(), adCounter - 1, ad.getSourceParagraph());
        }
        LOG.info("[parse] ingestionId={}, 架构决策解析完成: {} 条", ingestionId, adCounter);

        // 生成文档
        generateDocuments(projectId, ingestionId, title);

        repo.updateStatus(ingestionId, "completed", 100, "解析完成");
        LOG.info("[parse] ingestionId={}, 解析全部完成, status=completed", ingestionId);
    }

    private static String toJsonArray(java.util.List<String> items) {
        if (items == null || items.isEmpty()) {
            return "[]";
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(items);
        } catch (Exception ex) {
            return "[]";
        }
    }

    /**
     * 生成四类文档，内容基于已解析的真实数据。
     */
    private void generateDocuments(String projectId, String ingestionId, String title) {
        String baseTitle = title != null ? title : "PRD";
        String key = idKey(ingestionId);

        // 查询已解析的数据
        List<RequirementEntity> requirements = repo.findRequirements(ingestionId);
        List<DataEntity> dataEntities = repo.findDataEntities(ingestionId);
        List<InterfaceProtocol> interfaces = repo.findInterfaces(ingestionId);
        List<ArchitectureDecision> archDecisions = repo.findArchDecisions(ingestionId);

        // 1. Business Model
        renderAndInsertDocument(projectId, "doc-bm-" + key, ingestionId, "business-model",
                baseTitle + " - 业务模型", baseTitle,
                requirements, dataEntities, interfaces, archDecisions,
                collectOriginalParagraphs(ingestionId, "business-model"));

        // 2. Data Model
        String dmOriginal = collectOriginalParagraphs(ingestionId, "data-model");
        if (dmOriginal.isEmpty() && !dataEntities.isEmpty()) {
            dmOriginal = reverseMatchParagraphs(ingestionId, "data-model", dataEntities, null, null);
        }
        renderAndInsertDocument(projectId, "doc-dm-" + key, ingestionId, "data-model",
                baseTitle + " - 数据模型", baseTitle,
                requirements, dataEntities, interfaces, archDecisions, dmOriginal);

        // 3. Interface Protocol
        String ipOriginal = collectOriginalParagraphs(ingestionId, "interface-protocol");
        if (ipOriginal.isEmpty() && !interfaces.isEmpty()) {
            ipOriginal = reverseMatchParagraphs(ingestionId, "interface-protocol", null, interfaces, null);
        }
        renderAndInsertDocument(projectId, "doc-ip-" + key, ingestionId, "interface-protocol",
                baseTitle + " - 接口协议", baseTitle,
                requirements, dataEntities, interfaces, archDecisions, ipOriginal);

        // 4. Architecture Decision
        String adOriginal = collectOriginalParagraphs(ingestionId, "architecture-decision");
        if (adOriginal.isEmpty() && !archDecisions.isEmpty()) {
            adOriginal = reverseMatchParagraphs(ingestionId, "architecture-decision", null, null, archDecisions);
        }
        renderAndInsertDocument(projectId, "doc-ad-" + key, ingestionId, "architecture-decision",
                baseTitle + " - 架构决策", baseTitle,
                requirements, dataEntities, interfaces, archDecisions, adOriginal);
    }

    /**
     * 读取数据库模板（项目级优先、全局兕底）并渲染单份拆分文档后入库；模板缺失或渲染异常时回退硬编码生成。
     */
    private void renderAndInsertDocument(String projectId, String docId, String ingestionId, String type,
                                         String docTitle, String baseTitle,
                                         List<RequirementEntity> requirements, List<DataEntity> dataEntities,
                                         List<InterfaceProtocol> interfaces, List<ArchitectureDecision> archDecisions,
                                         String originalPrdContent) {
        String template = templateRepository.findContentByType(projectId, type);
        String content = renderDocumentFromTemplate(type, baseTitle, template,
                requirements, dataEntities, interfaces, archDecisions);
        repo.insertDocument(projectId, docId, ingestionId, type, docTitle, "draft", 1, content, originalPrdContent);
    }

    /**
     * 以「模板管理」中的模板为骨架渲染拆分文档。<p>
     *
     * 填充约定（占位符优先，章节名语义匹配兑底，两者都支持）：
     * <ol>
     *   <li>模板含标准占位符 {{REQUIREMENTS}}/{{ENTITIES}}/{{INTERFACES}}/{{DECISIONS}}/{{TITLE}}
     *       时，直接替换为解析出的数据块，模板可自由摆放数据位置；</li>
     *   <li>无占位符时，扫描模板二级标题（## xxx），按文档类型的数据语义匹配章节并填充；</li>
     *   <li>未被命中的章节保留模板原文占位（说明、表格等）；</li>
     *   <li>无模板或渲染异常时，回退原硬编码逻辑生成（保证兼容旧库与降级可用）。</li>
     * </ol>
     */
    static String renderDocumentFromTemplate(String type, String baseTitle, String template,
                                              List<RequirementEntity> requirements, List<DataEntity> dataEntities,
                                              List<InterfaceProtocol> interfaces, List<ArchitectureDecision> archDecisions) {
        if (template == null || template.isBlank()) {
            return hardcodedDocument(type, baseTitle, requirements, dataEntities, interfaces, archDecisions);
        }
        try {
            String reqBlock = buildRequirementsBlock(requirements);
            String entityBlock = buildEntitiesBlock(dataEntities);
            String ifaceBlock = buildInterfacesBlock(interfaces);
            String decisionBlock = buildDecisionsBlock(archDecisions);

            String rendered = template;
            // 模板首行 H1 统一为「baseTitle - 类型名」，与文档 title 字段保持一致
            rendered = replaceTemplateTitle(rendered, baseTitle, type);
             boolean hasPlaceholder = rendered.contains("{{REQUIREMENTS}}") || rendered.contains("{{ENTITIES}}")
                    || rendered.contains("{{INTERFACES}}") || rendered.contains("{{DECISIONS}}")
                    || rendered.contains("{{TITLE}}");
            if (hasPlaceholder) {
                return stripTemplatePlaceholders(rendered.replace("{{TITLE}}", baseTitle)
                        .replace("{{REQUIREMENTS}}", reqBlock)
                        .replace("{{ENTITIES}}", entityBlock)
                        .replace("{{INTERFACES}}", ifaceBlock)
                        .replace("{{DECISIONS}}", decisionBlock));
            }

            // 章节名语义匹配填充
            String[] lines = rendered.split("\n");
            StringBuilder out = new StringBuilder();
            boolean reqPlaced = false, entityPlaced = false, ifacePlaced = false, decisionPlaced = false;
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                if (line.startsWith("## ")) {
                    String matchedBlock = null;
                    String section = line.substring(3);
                    if (!reqPlaced && matchesSection("requirements", type, section)) { matchedBlock = reqBlock; reqPlaced = true; }
                    else if (!entityPlaced && matchesSection("entities", type, section)) { matchedBlock = entityBlock; entityPlaced = true; }
                    else if (!ifacePlaced && matchesSection("interfaces", type, section)) { matchedBlock = ifaceBlock; ifacePlaced = true; }
                    else if (!decisionPlaced && matchesSection("decisions", type, section)) { matchedBlock = decisionBlock; decisionPlaced = true; }
                    out.append(line).append("\n\n");
                    if (matchedBlock != null) {
                        out.append(matchedBlock).append("\n\n");
                        // 命中数据章节：跳过模板原有正文行（表格/占位说明），避免占位残留
                        while (i + 1 < lines.length && !lines[i + 1].startsWith("#")) {
                            i++;
                        }
                    }
                } else {
                    out.append(line).append("\n");
                }
            }
            // 模板中没有可填充的数据章节时，把解析出的数据块追加到对应类型文档末尾，
            // 保证拆分数据不丢失。各数据块只进入自己的文档类型，避免需求混入数据模型等。
            if (!reqPlaced && type.equals("business-model") && !reqBlock.isBlank()) out.append("\n## 需求列表\n\n").append(reqBlock).append("\n\n");
            if (!entityPlaced && type.equals("data-model") && !entityBlock.isBlank()) out.append("\n## 实体关系\n\n").append(entityBlock).append("\n\n");
            if (!ifacePlaced && type.equals("interface-protocol") && !ifaceBlock.isBlank()) out.append("\n## 接口列表\n\n").append(ifaceBlock).append("\n\n");
            if (!decisionPlaced && type.equals("architecture-decision") && !decisionBlock.isBlank()) out.append("\n## 决策记录\n\n").append(decisionBlock).append("\n\n");
            return stripTemplatePlaceholders(out.toString().trim());
        } catch (Exception e) {
            LOG.warn("[generateDocuments] 模板渲染异常({}), 回退硬编码生成: {}", type, e.getMessage());
            return hardcodedDocument(type, baseTitle, requirements, dataEntities, interfaces, archDecisions);
        }
    }

    /**
     * 清理模板渲染后的填写说明占位文字（如 {@code <填写说明>}、{@code <明确模板/系统职责边界>} 等），
     * 以及连续空行，确保最终文档干净、无模板残留提示。
     */
    static String stripTemplatePlaceholders(String rendered) {
        if (rendered == null || rendered.isBlank()) return rendered;
        String result = rendered;
        // 1) 去掉中文占位文字：<...> 格式，内容包含中文
        result = result.replaceAll("<[^>]*[\\u4e00-\\u9fff][^>]*>", "").trim();
        // 2a) 去掉花括号占位文字：{...} 格式（如 {1-3 句话：上下文是什么}）
        result = result.replaceAll("\\{[^}]*[\\u4e00-\\u9fff][^}]*\\}", "").trim();
        // 2b) 去掉英文占位（如 <entity_name>），但保留标准 Markdown 表格分隔符
        result = result.replaceAll("<([^>]*)>", "").trim();
        // 3) 合并连续空行（≥3 个换行 → 2 个）
        result = result.replaceAll("\\n{3,}", "\n\n");
        return result;
    }

    /** 文档类型 → 中文名称（与 title 字段的命名一致）。 */
    static String documentTypeName(String type) {
        switch (type) {
            case "business-model": return "业务模型";
            case "data-model": return "数据模型";
            case "interface-protocol": return "接口协议";
            case "architecture-decision": return "架构决策";
            default: return type;
        }
    }

    /**
     * 把模板首行 H1 替换为「baseTitle - 类型名」；模板无 H1 时在开头补一行。
     * 保证文档内容标题与 document.title 字段一致（前端列表/详情展示不出现标题错位）。
     */
    static String replaceTemplateTitle(String template, String baseTitle, String type) {
        String typeName = documentTypeName(type);
        int idx = template.indexOf("\n");
        if (idx > 0 && template.startsWith("# ")) {
            return "# " + baseTitle + " - " + typeName + template.substring(idx);
        }
        if (template.startsWith("# ")) {
            return "# " + baseTitle + " - " + typeName;
        }
        return "# " + baseTitle + " - " + typeName + "\n\n" + template;
    }

    /**
     * 判断某个二级标题章节是否应填充指定类型的数据。<p>
     * 关键词必须是强信号短语，避免模板导语误命中。
     */
    static boolean matchesSection(String dataType, String docType, String section) {
        switch (dataType) {
            case "requirements":
                return containsAny(section, "需求", "需求列表", "业务需求", "用户故事", "User Story",
                        "功能摘要", "功能概述", "功能模块", "功能点", "产品概述", "Feature");
            case "entities":
                return docType.equals("data-model") && containsAny(section, "实体", "实体关系", "模型",
                        "Entity", "数据模型", "数据结构", "数据字典", "表结构",
                        "字段", "主键", "外键", "E-R", "ER 图", "数据表", "Schema");
            case "interfaces":
                return docType.equals("interface-protocol") && containsAny(section, "接口", "API", "端点",
                        "HTTP", "REST", "请求参数", "请求方式", "请求体", "响应体", "响应码", "报文", "URL");
            case "decisions":
                return docType.equals("architecture-decision") && containsAny(section, "架构", "决策", "ADR",
                        "技术选型", "系统设计", "非功能需求", "性能需求", "高可用", "Architecture");
            default:
                return false;
        }
    }

    private static boolean isProposed(String candidateStatus) {
        return "proposed".equals(candidateStatus);
    }

    /** 需求列表 → Markdown 数据块（模板填充用）。 */
    static String buildRequirementsBlock(List<RequirementEntity> requirements) {
        if (requirements == null || requirements.isEmpty()) return "";
        StringBuilder b = new StringBuilder();
        for (RequirementEntity req : requirements) {
            b.append("### ").append(req.getId()).append(" [").append(req.getPriority()).append("]");
            if (isProposed(req.getCandidateStatus())) {
                b.append(" ⚠️ 候选（规则推导，需人工确认）");
            }
            b.append("\n\n");
            b.append(normalizeNewlines(req.getDescription())).append("\n\n");
            if (req.getSourceParagraph() != null && !req.getSourceParagraph().isEmpty()) {
                b.append("> **来源**: ").append(normalizeNewlines(truncate(req.getSourceParagraph(), 200))).append("\n\n");
            }
        }
        return b.toString().trim();
    }

    /** 数据实体 → Markdown 数据块（模板填充用）。 */
    static String buildEntitiesBlock(List<DataEntity> dataEntities) {
        if (dataEntities == null || dataEntities.isEmpty()) return "";
        StringBuilder b = new StringBuilder();
        for (DataEntity entity : dataEntities) {
            b.append("### ").append(normalizeNewlines(entity.getName()));
            if (isProposed(entity.getCandidateStatus())) {
                b.append(" ⚠️ 候选（规则推导，需人工确认）");
            }
            b.append("\n\n");
            b.append(normalizeNewlines(entity.getDescription())).append("\n\n");
            if (entity.getSourceParagraph() != null && !entity.getSourceParagraph().isEmpty()) {
                b.append("> **来源**: ").append(normalizeNewlines(truncate(entity.getSourceParagraph(), 200))).append("\n\n");
            }
            if (entity.getAttributes() != null && !entity.getAttributes().isEmpty()) {
                b.append("**属性**:\n\n| 属性名 | 类型 | 描述 |\n|--------|------|------|\n");
                for (DataEntity.Attribute attr : entity.getAttributes()) {
                    b.append("| ").append(escapeTableCell(normalizeNewlines(attr.getName())))
                            .append(" | ").append(escapeTableCell(normalizeNewlines(attr.getType())))
                            .append(" | ").append(escapeTableCell(normalizeNewlines(attr.getDescription()))).append(" |\n");
                }
                b.append("\n");
            }
            if (entity.getRelations() != null && !entity.getRelations().isEmpty()) {
                b.append("**关系**:\n\n| 目标实体 | 关系类型 | 描述 |\n|----------|----------|------|\n");
                for (DataEntity.Relation rel : entity.getRelations()) {
                    b.append("| ").append(escapeTableCell(normalizeNewlines(rel.getTarget())))
                            .append(" | ").append(escapeTableCell(normalizeNewlines(rel.getType())))
                            .append(" | ").append(escapeTableCell(normalizeNewlines(rel.getDescription()))).append(" |\n");
                }
                b.append("\n");
            }
        }
        return b.toString().trim();
    }

    /** 接口定义 → Markdown 数据块（模板填充用）。 */
    static String buildInterfacesBlock(List<InterfaceProtocol> interfaces) {
        if (interfaces == null || interfaces.isEmpty()) return "";
        StringBuilder b = new StringBuilder();
        for (InterfaceProtocol iface : interfaces) {
            b.append("### ").append(iface.getMethod()).append(" ").append(normalizeNewlines(iface.getPath()));
            if (isProposed(iface.getCandidateStatus())) {
                b.append(" ⚠️ 候选（规则推导，需人工确认）");
            }
            b.append("\n\n");
            b.append(normalizeNewlines(nullSafe(iface.getSummary()))).append("\n\n");
            if (iface.getSourceParagraph() != null && !iface.getSourceParagraph().isEmpty()) {
                b.append("> **来源**: ").append(normalizeNewlines(truncate(iface.getSourceParagraph(), 200))).append("\n\n");
            }
            if (iface.getRequestBody() != null && !iface.getRequestBody().isEmpty()) {
                b.append("**请求体**:\n\n").append(safeCodeBlock(normalizeNewlines(iface.getRequestBody()))).append("\n\n");
            }
            if (iface.getResponseBody() != null && !iface.getResponseBody().isEmpty()) {
                b.append("**响应体**:\n\n").append(safeCodeBlock(normalizeNewlines(iface.getResponseBody()))).append("\n\n");
            }
        }
        return b.toString().trim();
    }

    /** 架构决策 → Markdown 数据块（模板填充用）。 */
    static String buildDecisionsBlock(List<ArchitectureDecision> archDecisions) {
        if (archDecisions == null || archDecisions.isEmpty()) return "";
        StringBuilder b = new StringBuilder();
        for (ArchitectureDecision arch : archDecisions) {
            b.append("### ").append(arch.getId()).append(": ").append(nullSafe(arch.getTitle())).append("\n\n");
            String statusLabel = nullSafe(arch.getStatus());
            if ("proposed".equals(arch.getStatus())) {
                statusLabel += " ⚠️ 候选（规则推导，需人工确认）";
            } else if ("accepted".equals(arch.getStatus())) {
                statusLabel += " ✅ 已确认";
            }
            b.append("**状态**: ").append(normalizeNewlines(statusLabel)).append("\n\n");
            if (arch.getContext() != null && !arch.getContext().isEmpty()) {
                b.append("**背景**:\n\n").append(normalizeNewlines(arch.getContext())).append("\n\n");
            }
            b.append("**决策**:\n\n").append(normalizeNewlines(nullSafe(arch.getDecision()))).append("\n\n");
            if (arch.getSourceParagraph() != null && !arch.getSourceParagraph().isEmpty()) {
                b.append("> **来源**: ").append(normalizeNewlines(truncate(arch.getSourceParagraph(), 200))).append("\n\n");
            }
            if (arch.getConsequences() != null && !arch.getConsequences().isEmpty()) {
                b.append("**影响**:\n\n");
                for (String c : arch.getConsequences()) {
                    appendListItem(b, "- ", c, "");
                }
                b.append("\n");
            }
        }
        return b.toString().trim();
    }

    /**
     * 原硬编码文档生成逻辑（无模板/模板渲染异常时的兜底），保持历史行为。
     */
    static String hardcodedDocument(String type, String baseTitle,
                                            List<RequirementEntity> requirements, List<DataEntity> dataEntities,
                                            List<InterfaceProtocol> interfaces, List<ArchitectureDecision> archDecisions) {
        switch (type) {
            case "data-model":
                return "# " + baseTitle + " - 数据模型\n\n## 实体关系\n\n" + buildEntitiesBlock(dataEntities);
            case "interface-protocol":
                return "# " + baseTitle + " - 接口协议\n\n## 接口列表\n\n" + buildInterfacesBlock(interfaces);
            case "architecture-decision":
                return "# " + baseTitle + " - 架构决策\n\n## 决策记录\n\n" + buildDecisionsBlock(archDecisions);
            case "business-model":
            default:
                return "# " + baseTitle + " - 业务模型\n\n## 概述\n\n本文档基于 PRD 自动生成，包含以下业务需求。\n\n## 需求列表\n\n"
                        + buildRequirementsBlock(requirements);
        }
    }

    /**
     * 判断 PRD 段落是否属于某类文档的原始依据（关键词回退解析与原文拆分共用）。
     *
     * <p>关键词必须是强信号短语而非常用泛词，避免模板导语误命中：
     * 例如「大致响应时间」不再命中接口协议（不用单字"响应"/"请求"）、
     * 「架构设计人员」不再命中架构决策（"架构"仅在架构相关词表中按优先级判定）、
     * 业务模型不再因单字"需求"/"功能"把整份文档吞掉。类型间有优先级
     * （data-model &gt; interface-protocol &gt; architecture-decision &gt; business-model），
     * 同一段落只归属一个类型。</p>
     */
    static boolean matchesDocType(String para, String docType) {
        return SectionTypeClassifier.matchesDocType(para, docType);
    }

    /** 判断文本是否包含任意一个关键词。 */
    private static boolean containsAny(String text, String... keywords) {
        return SectionTypeClassifier.containsAny(text, keywords);
    }

    // ========== 章节层级 + 加权关键词评分分类（词表与评分逻辑统一下沉到 SectionTypeClassifier） ==========

    /**
     * 对一段文本计算四类文档的加权关键词评分，返回评分最高的类型及分数。
     * @param text 待评分的文本（通常是标题行）
     * @return 类型名 + 分数，若所有类型均为 0 分则返回 ["business-model", 0]
     */
    static Map.Entry<String, Integer> scoreDocType(String text) {
        return SectionTypeClassifier.scoreDocType(text);
    }

    /**
     * 检测标题的层级深度。
     * @return 1=H1, 2=H2, 3=H3, 4=H4+, 0=非标题
     */
    static int detectHeadingLevel(String para) {
        if (para == null || para.isBlank()) return 0;
        String t = para.trim();
        // Markdown 标题
        if (t.startsWith("#")) {
            int level = 1;
            for (int i = 1; i < t.length() && t.charAt(i) == '#'; i++) level++;
            return Math.min(level, 6);
        }
        // 编号标题：1.2.3 格式 或 5.1 格式
        if (t.matches("^\\d+(\\.\\d+)+\\s+.*")) {
            String[] parts = t.split("\\s+")[0].split("\\.");
            return Math.min(parts.length, 6);
        }
        // 编号标题：1. 格式
        if (t.matches("^\\d+\\.\\s+.*")) return 1;
        // 中文序号：一、二、…
        if (t.matches("^[一二三四五六七八九十]+、.*")) return 1;
        // 中文序号：（一）（二）…
        if (t.matches("^（[一二三四五六七八九十]+）.*")) return 1;
        // 数字编号：1、1．等
        if (t.matches("^\\d+[、．]\\s*.*")) return 1;
        return 0;
    }

    /**
     * 章节层级感知的段落分类，替代旧的 {@link #classifyParagraphsBySection(List)}。
     *
     * <p>算法：
     * <ol>
     *   <li>识别每个段落的标题层级（H1-H6），构建标题树；</li>
     *   <li>对每个标题用加权关键词评分（{@link #scoreDocType(String)}）计算最佳类型；</li>
     *   <li>子标题的评分低于阈值（5分）时，继承父标题的类型；</li>
     *   <li>非标题段落继承最近一个标题的类型。</li>
     * </ol>
     *
     * @param paragraphs 段落列表（已按空行分割）
     * @return 每个段落对应的文档类型数组
     */
    static String[] classifyBySectionHierarchy(List<String> paragraphs) {
        int n = paragraphs.size();
        String[] owners = new String[n];
        // 栈：每层标题的类型，用于父继承
        java.util.ArrayDeque<String> typeStack = new java.util.ArrayDeque<>();
        typeStack.push("business-model"); // 根节点默认业务模型

        for (int i = 0; i < n; i++) {
            String para = paragraphs.get(i);
            int level = detectHeadingLevel(para);
            if (level > 0) {
                // 是标题行
                Map.Entry<String, Integer> scored = scoreDocType(para);
                String type = scored.getKey();
                int score = scored.getValue();
                // 子标题评分低于阈值时，继承父标题的类型
                if (score < 5 && typeStack.size() > 1) {
                    type = typeStack.peek();
                }
                // 弹出栈顶到当前层级
                while (typeStack.size() > level) {
                    typeStack.pop();
                }
                typeStack.push(type);
                owners[i] = type;
            } else {
                // 非标题段落：继承最近一个标题的类型
                owners[i] = typeStack.peek();
            }
        }
        return owners;
    }

    /**
     * 按优先级返回段落最能归属的文档类型；无任何特征时返回 null。
     * 优先级：data-model &gt; interface-protocol &gt; architecture-decision &gt; business-model。
     */
    static String bestDocType(String para) {
        return SectionTypeClassifier.bestDocType(para);
    }

    /**
     * 判断段落是否为章节标题（Markdown 标题 / 编号标题 / 中文序号标题）。
     */
    static boolean isHeading(String para) {
        String t = para == null ? "" : para.trim();
        if (t.isEmpty()) return false;
        // Markdown 标题：# ~ ######
        if (t.startsWith("#")) return true;
        // 编号标题：1、1.2、1.2.3 开头
        if (t.matches("^\\d+(\\.\\d+){0,3}\\s+.*")) return true;
        // 中文序号标题：一、二、…；（一）（二）…；1．、1、
        if (t.matches("^[一二三四五六七八九十]+、.*")) return true;
        if (t.matches("^（[一二三四五六七八九十]+）.*")) return true;
        return false;
    }

    /**
     * 收集某类文档对应的原始 PRD 段落（单次扫描，每段唯一归属，跨文档类型不重复）：
     *
     * <ol>
     *   <li>章节级归属：标题行按优先级（data-model &gt; interface-protocol &gt;
     *       architecture-decision &gt; business-model）判定归属类型，标题命中该类型时
     *       整个章节（直到下一个标题）归入；标题无任何特征时兜底归业务模型（主文档收纳器）；</li>
     *   <li>段落级兜底：章节外零散段落按同样优先级逐段匹配，无特征时归业务模型；
     *       base64 图片数据视为业务模型正文，避免子串（如 "HTTP"/"REST"）误命中其他类型。</li>
     * </ol>
     *
     * <p>与旧实现的关键差异：不再用宽泛子串（"需求"/"功能"/"响应"/"架构"）逐类独立扫描
     * （各自维护 consumed，同一段落会被多个类型重复收入），而是单次扫描为每个段落
     * 计算唯一归属，同一段落只进一份文档：例如模板导语「[如果产品对性能要特殊需求…响应时间…]」
     * 随"性能需求"章节归架构决策，不再误入接口协议；"产品特性"整章（含"架构设计人员"导语）
     * 归业务模型，不再误入架构决策；业务模型也不再因"需求/功能"宽泛命中而吞下整份文档。</p>
     *
     * <p>某类型无任何命中时返回空字符串（校验面板显示"无对应原文段落"）。</p>
     */
    private String collectOriginalParagraphs(String ingestionId, String docType) {
        String content = repo.findContent(ingestionId);
        // 不按关键词切分，所有文档都拿整篇 PRD 原文
        // 差异化靠模板渲染（提取结果）实现，PRD原文只提供完整上下文
        return content != null ? content : "";
    }

    /**
     * Phase 3 反向匹配：当章节归属（collectOriginalParagraphs）取不到原文段落时，
     * 用提取出的实体名/接口路径/架构决策关键词在 PRD 原文段落中做反向检索。
     * 优先匹配含实体名/接口路径的段落；无精确匹配时回退到关键词匹配。
     */
    private String reverseMatchParagraphs(String ingestionId, String docType,
                                           List<DataEntity> entities,
                                           List<InterfaceProtocol> interfaces,
                                           List<ArchitectureDecision> decisions) {
        String content = repo.findContent(ingestionId);
        if (content == null || content.isBlank()) return "";
        List<String> paragraphs = Arrays.stream(content.split("\\n\\s*\\n"))
                .map(String::trim)
                .filter(p -> !p.isEmpty())
                .collect(Collectors.toList());

        // 从提取数据中收集关键词
        Set<String> keywords = new HashSet<>();
        switch (docType) {
            case "data-model":
                if (entities != null) {
                    for (DataEntity e : entities) {
                        keywords.add(e.getName());
                        if (e.getAttributes() != null) {
                            for (DataEntity.Attribute a : e.getAttributes()) {
                                keywords.add(a.getName());
                            }
                        }
                    }
                }
                keywords.addAll(Arrays.asList("实体", "字段", "属性", "表", "状态", "数据字典", "数据结构", "ER图"));
                break;
            case "interface-protocol":
                if (interfaces != null) {
                    for (InterfaceProtocol i : interfaces) {
                        keywords.add(i.getPath());
                        keywords.add(i.getMethod());
                        if (i.getSummary() != null && !i.getSummary().isBlank()) {
                            String s = i.getSummary().length() > 20 ? i.getSummary().substring(0, 20) : i.getSummary();
                            keywords.add(s);
                        }
                    }
                }
                keywords.addAll(Arrays.asList("接口", "API", "请求", "响应", "参数", "返回", "提交", "查询", "HTTP", "REST"));
                break;
            case "architecture-decision":
                if (decisions != null) {
                    for (ArchitectureDecision d : decisions) {
                        if (d.getTitle() != null && !d.getTitle().isBlank()) {
                            keywords.add(d.getTitle());
                        }
                    }
                }
                keywords.addAll(Arrays.asList("架构", "技术选型", "决策", "方案", "设计", "性能", "安全", "高可用", "扩展性"));
                break;
            default:
                return "";
        }

        // 对每个段落打分：匹配关键词越多，越可能是该类型的段落
        StringBuilder sb = new StringBuilder();
        for (String para : paragraphs) {
            if (isSkipParagraph(para)) continue;
            String lower = para.toLowerCase();
            int score = 0;
            for (String kw : keywords) {
                if (lower.contains(kw.toLowerCase())) {
                    // 精确匹配实体名/路径加分更高
                    score += kw.length() > 3 ? 3 : 1;
                }
            }
            // 阈值：至少匹配到一个非单字词
            if (score >= 3) {
                sb.append(para).append("\n\n");
            }
        }
        return sb.toString().trim();
    }

    /**
     * 超长 PRD 交给 LLM 前截断，避免超出上下文/token 预算。
     * 先去除 base64 图片（经常占几千字符导致真实需求被截断），再截断到极限值。
     */
    private static String truncateForLlm(String content) {
        if (content == null || content.isBlank()) return content;
        // 1) 去除 data:image/...;base64,... 图片数据（经常占 2000-5000+ 字符）
        // 注意：从 data:image/ 匹配到第一个 ) 为止（即 markdown 图片URL的结尾）
        String cleaned = content.replaceAll("data:image/[^;]+;base64[^)]+\\)", "").trim();
        // 2) 去除普通 inline 图片标记
        cleaned = cleaned.replaceAll("!\\[.*?\\]\\([^)]+\\)", "").trim();
        // 3) 去除纯 HTML 图片标签
        cleaned = cleaned.replaceAll("<img[^>]+>", "").trim();

        final int MAX_LLM_CHARS = 15_000;
        if (cleaned.length() <= MAX_LLM_CHARS) {
            return cleaned;
        }
        return cleaned.substring(0, MAX_LLM_CHARS) + "\n\n（内容过长，以下已省略，请仅基于以上内容提取）";
    }

    private static String nullSafe(String s) {
        return s != null ? s : "";
    }

    /**
     * 获取解析进度。
     */
    public IngestionProgress getProgress(String ingestionId) {
        return repo.findProgress(ingestionId);
    }

    /**
     * 获取完整解析结果。
     */
    public PrdIngestResponse getIngestionResult(String ingestionId) {
        IngestionProgress progress = repo.findProgress(ingestionId);
        if (progress == null) return null;

        PrdIngestResponse response = new PrdIngestResponse();
        response.setIngestionId(ingestionId);
        response.setStatus(progress.getStatus());
        response.setTitle(repo.findTitle(ingestionId));
        response.setParseSource(repo.findParseSource(ingestionId));
        response.setRequirements(repo.findRequirements(ingestionId));
        response.setDataEntities(repo.findDataEntities(ingestionId));
        response.setInterfaces(repo.findInterfaces(ingestionId));
        response.setArchitectureDecisions(repo.findArchDecisions(ingestionId));
        response.setDocuments(repo.findDocumentSummaries(ingestionId));
        return response;
    }

    /**
     * 人工确认一条候选条目：proposed → confirmed / accepted，返回受影响行数。
     */
    public int confirmCandidate(String ingestionId, String type, String id) {
        return repo.confirmCandidate(ingestionId, type, id);
    }

    /**
     * 重新解析。
     */
    public PrdIngestResponse reparse(String ingestionId) {
        // 以 prd_ingestion 记录为准（不依赖 doc-bm 文档是否存在）：
        // 若上次解析因 LLM 超时中断（deleteParsedData 已删除文档但生成失败），
        // 仍能基于完整原文与 projectId 重新解析，保证可恢复。
        String projectId = repo.findProjectId(ingestionId);
        String fullContent = repo.findContent(ingestionId);
        String title = repo.findTitle(ingestionId);
        if (projectId == null || projectId.isBlank()) {
            // 兼容历史数据：project_id 缺失时回退到业务模型文档记录
            com.huazai.prd.ingestion.model.document.WikiDocument doc = repo.findDocument("doc-bm-" + idKey(ingestionId));
            if (doc == null && ingestionId != null && ingestionId.length() > 6) {
                doc = repo.findDocument("doc-bm-" + ingestionId.substring(ingestionId.length() - 6));
            }
            if (doc == null) return null;
            projectId = doc.getProjectId();
            if (fullContent == null || fullContent.isBlank()) {
                fullContent = doc.getOriginalPrdContent() != null ? doc.getOriginalPrdContent() : "";
            }
        }
        if (projectId == null || projectId.isBlank()) return null;

        // 清空该导入记录下的旧解析数据，再重新解析，保证幂等（避免 REQ-xxx 唯一索引冲突）
        repo.deleteParsedData(ingestionId);
        repo.updateStatus(ingestionId, "pending", 0, "重新解析中...");
        // 重新解析必须用完整 PRD 原文，不能用拆分后的文档原文（doc-bm 只含业务模型章节，
        // 用它重解析会丢掉数据模型/接口协议等其余内容）
        simulateParsing(projectId, ingestionId, fullContent, title);
        return getIngestionResult(ingestionId);
    }

// ---- Hybrid 解析结果写入数据库 ----

    /**
     * 将模板或 LLM 解析的结构化结果写入数据库。
     * 写入后自动跳过后续的关键词匹配流程。
     */
    /**
     * 旁路保存 LLM 解析结果（仅用于前端左侧展示，不影响文档内容）。
     */
    private void saveLlmResult(String ingestionId, PrdParseResult result) {
        try {
            repo.insertLlmResult(ingestionId, OBJECT_MAPPER.writeValueAsString(result));
        } catch (Exception e) {
            LOG.warn("[parse] ingestionId={}, 保存 LLM 结果失败(不影响解析与文档生成): {}", ingestionId, e.getMessage());
        }
    }

    private void writeParseResult(String projectId, String ingestionId, String key, PrdParseResult result) {
        // 记录解析来源
        repo.updateParseSource(ingestionId, result.getParseSource() != null ? result.getParseSource() : "section_hierarchy");

        int reqOrder = 0;
        int reqCounter = 0;
        for (PrdParseResult.RequirementItem req : result.getRequirements()) {
            reqCounter++;
            // 需求 ID 一律基于 ingestionId 生成（REQ-{key}-{seq}），不信任模板/LLM 返回的固定 id。
            // 模板数据（DomainTemplateRegistry）与 LLM 输出都包含硬编码的 REQ-001/REQ-002，
            // 而 prd_requirement.uk_req_id 是全局唯一索引，直接使用会导致跨导入重复冲突。
            String reqId = "REQ-" + key + "-" + String.format("%03d", reqCounter);
            repo.insertRequirement(projectId, ingestionId, reqId,
                    req.getDescription() != null ? req.getDescription() : "",
                    req.getPriority() != null ? req.getPriority() : "P2",
                    "[]", "", "", reqOrder++);
        }

        int entityOrder = 0;
        for (PrdParseResult.EntityItem entity : result.getDataEntities()) {
            String entitySource = entity.getSourceSection() != null ? entity.getSourceSection() : "";
            long entityId = repo.insertDataEntity(projectId, ingestionId,
                    entity.getName() != null ? entity.getName() : "Entity" + entityOrder,
                    entity.getDescription() != null ? entity.getDescription() : "", entityOrder++, entitySource);
            int attrOrder = 0;
            for (PrdParseResult.EntityItem.AttributeItem attr : entity.getAttributes()) {
                repo.insertEntityAttribute(entityId,
                        attr.getName() != null ? attr.getName() : "field" + attrOrder,
                        attr.getType() != null ? attr.getType() : "String",
                        attr.getDescription() != null ? attr.getDescription() : "", attrOrder++);
            }
            int relOrder = 0;
            for (PrdParseResult.EntityItem.RelationItem rel : entity.getRelations()) {
                repo.insertEntityRelation(entityId,
                        rel.getTarget() != null ? rel.getTarget() : "",
                        rel.getType() != null ? rel.getType() : "ManyToOne",
                        rel.getDescription() != null ? rel.getDescription() : "",
                        relOrder++);
            }
        }

        int ifOrder = 0;
        for (PrdParseResult.InterfaceItem iface : result.getInterfaces()) {
            String ifaceSource = iface.getSourceSection() != null ? iface.getSourceSection() : "";
            repo.insertInterface(projectId, ingestionId,
                    iface.getMethod() != null ? iface.getMethod() : "GET",
                    iface.getPath() != null ? iface.getPath() : "/api/v1/unknown",
                    iface.getSummary() != null ? iface.getSummary() : "",
                    iface.getRequestBody() != null ? iface.getRequestBody() : "",
                    iface.getResponseBody() != null ? iface.getResponseBody() : "",
                    "", ifOrder++, ifaceSource);
        }

        int adCounter = 0;
        for (PrdParseResult.ArchDecisionItem ad : result.getArchDecisions()) {
            adCounter++;
            String adId = "AD-" + key + "-" + String.format("%03d", adCounter);
            String consequences = ad.getConsequences() != null
                    ? "[" + String.join(", ", ad.getConsequences()) + "]"
                    : "[]";
            String adSource = ad.getSourceSection() != null ? ad.getSourceSection() : "";
            repo.insertArchDecision(projectId, ingestionId, adId,
                    ad.getTitle() != null ? ad.getTitle() : "决策" + adCounter,
                    ad.getContext() != null ? ad.getContext() : "",
                    ad.getDecision() != null ? ad.getDecision() : "",
                    consequences,
                    ad.getStatus() != null ? ad.getStatus() : "proposed",
                    adCounter - 1, adSource);
        }

        LOG.info("[parse] ingestionId={}, 写入完成: {} 需求, {} 实体, {} 接口, {} 决策",
                ingestionId, reqCounter, entityOrder, ifOrder, adCounter);
    }

    // ---- Markdown formatting helpers ----

    /**
     * 归一化字符串中的字面换行符：将 "\\n" 替换为真正的换行符。
     * PRD 解析数据中可能混入字面反斜杠 n，需要先归一化再输出。
     */
    // ---- 章节感知分类 ----

    /**
     * 章节感知的段落分类（与 collectOriginalParagraphs 逻辑一致）。
     * 标题行决定整章归属，无特征标题兜底业务模型。
     */
    private static String[] classifyParagraphsBySection(java.util.List<String> paragraphs) {
        int n = paragraphs.size();
        String[] owners = new String[n];

        // 阶段一：章节级归属——标题行决定整章归属
        for (int i = 0; i < n; i++) {
            if (!isHeading(paragraphs.get(i))) {
                continue;
            }
            String heading = paragraphs.get(i);
            String type = bestDocType(heading);
            if (type == null) {
                type = "business-model";
            }
            owners[i] = type;
            // 同章节后续段落（直到下一个标题）归入同一类型
            for (int j = i + 1; j < n; j++) {
                if (isHeading(paragraphs.get(j))) {
                    break;
                }
                if (owners[j] == null && !isNoiseParagraph(paragraphs.get(j))) {
                    owners[j] = type;
                }
            }
        }

        // 阶段二：段落级兜底——章节外零散段落按优先级匹配
        for (int i = 0; i < n; i++) {
            if (owners[i] != null) continue;
            String para = paragraphs.get(i);
            if (para.contains("data:image/") || para.contains("base64,")) {
                owners[i] = "business-model";
                continue;
            }
            String type = bestDocType(para);
            owners[i] = type == null ? "business-model" : type;
        }
        return owners;
    }

    // ---- 增强型实体提取 ----

    /**
     * 从描述数据模型的文本中提取结构化实体信息。
     * 支持 Markdown 表格格式：| 属性名 | 类型 | 描述 |
     * 以及文本描述格式："实体名：包含字段A、字段B"
     */
    private static java.util.List<ExtractedEntity> extractEntitiesFromKeywords(String text) {
        java.util.List<ExtractedEntity> entities = new java.util.ArrayList<>();
        String[] lines = text.split("\n");

        // 尝试提取 Markdown 表格
        java.util.List<String[]> tableRows = extractTableRows(lines);
        if (!tableRows.isEmpty()) {
            String[] header = tableRows.get(0);
            boolean hasType = false;
            boolean hasAttr = false;
            for (String h : header) {
                String hLow = h.toLowerCase();
                if (hLow.contains("类型") || hLow.contains("type")) hasType = true;
                if (hLow.contains("属性") || hLow.contains("字段") || hLow.contains("name")) hasAttr = true;
            }
            if (hasAttr && hasType && tableRows.size() > 1) {
                String entityName = guessEntityNameBeforeTable(text.split("\n"), tableRows);
                // 未找到有意义的实体名（如 hash 回退 Entity485）或名字本身不具业务含义时不产出实体
                if (entityName != null && !entityName.startsWith("Entity") && isMeaningfulEntityName(entityName)) {
                    ExtractedEntity entity = new ExtractedEntity(entityName, "数据实体");
                    for (int i = 1; i < tableRows.size(); i++) {
                        String[] row = tableRows.get(i);
                        if (row.length >= 2) {
                            String attrName = row[0].trim();
                            String attrType = row.length >= 2 ? row[1].trim() : "String";
                            String attrDesc = row.length >= 3 ? row[2].trim() : "";
                            if (!attrName.matches("[-:]+")) {
                                entity.attributes.add(new ExtractedEntity.Attribute(attrName, attrType, attrDesc));
                            }
                        }
                    }
                    if (!entity.attributes.isEmpty()) {
                        entities.add(entity);
                    }
                }
            }
        }

        // 回退：尝试从文本中提取实体定义
        for (String[] candidate : extractEntityCandidatesFromSection(text)) {
            entities.add(new ExtractedEntity(candidate[0], candidate[1] == null ? "数据实体" : candidate[1]));
        }
        return entities;
    }

    /**
     * 从指定分段文本中提取候选实体（抽取“实体/数据字典”等结构化段落的真实业务名词）。
     * 返回 [name, description] 列表，提取不到返回空列表。
     */
    private static java.util.List<String[]> extractEntityCandidatesFromSection(String text) {
        java.util.List<String[]> result = new java.util.ArrayList<>();
        if (text == null || text.isBlank()) return result;
        java.util.Set<String> seen = new java.util.HashSet<>();
        // 优先匹配“实体名：描述”或“- 实体名：描述”模式
        java.util.regex.Pattern defPattern = java.util.regex.Pattern.compile(
                "(?:^|[\\r\\n])(?:-\\s*|[*]\\s*)?([\\u4e00-\\u9fa5A-Za-z0-9_]{2,30})[：:]\\s*([\\u4e00-\\u9fa5A-Za-z0-9_，,、（）()\\s]{2,120})");
        java.util.regex.Matcher m = defPattern.matcher(text);
        while (m.find() && result.size() < 8) {
            String name = m.group(1).trim();
            if (!isMeaningfulEntityName(name) || seen.contains(name)) continue;
            seen.add(name);
            String desc = m.group(2).trim();
            if (desc.length() > 120) desc = desc.substring(0, 120);
            result.add(new String[]{name, desc});
        }
        return result;
    }
    static java.util.List<String[]> extractTableRows(String[] lines) {
        java.util.List<String[]> rows = new java.util.ArrayList<>();
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("|") && line.endsWith("|")) {
                String[] cells = line.substring(1, line.length() - 1).split("\\|");
                for (int i = 0; i < cells.length; i++) {
                    cells[i] = cells[i].trim();
                }
                if (cells.length > 0 && cells[0].matches("[-:]+")) {
                    continue;
                }
                rows.add(cells);
            }
        }
        return rows;
    }

    /**
     * 尝试从表格前的段落中推断实体名。
     */
    static String guessEntityNameBeforeTable(String[] lines, java.util.List<String[]> tableRows) {
        int tableStart = -1;
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].trim().startsWith("|")) {
                tableStart = i;
                break;
            }
        }
        for (int i = (tableStart > 0 ? tableStart - 1 : 0); i >= 0 && i >= tableStart - 5; i--) {
            String line = lines[i].trim();
            if (!line.isEmpty() && !line.startsWith("#") && !line.startsWith("|")) {
                String candidate = line.replaceAll("[^a-zA-Z0-9_\\u4e00-\\u9fa5]", "").trim();
                if (candidate.length() > 1 && candidate.length() < 50 && isMeaningfulEntityName(candidate)) {
                    return candidate;
                }
            }
        }
        return "";
    }

    /**
     * 从文本中提取属性列表（如 "包含字段A、字段B、字段C"）。
     */
    private static java.util.List<ExtractedEntity.Attribute> extractAttributesFromText(String text) {
        java.util.List<ExtractedEntity.Attribute> attrs = new java.util.ArrayList<>();
        // 匹配 "字段A、字段B" 或 "字段A, 字段B"
        java.util.regex.Pattern attrListPattern = java.util.regex.Pattern.compile(
                "(字段|属性|包含|包括)[：:]\\s*([\\u4e00-\\u9fa5A-Za-z0-9_]+[、，,][\\u4e00-\\u9fa5A-Za-z0-9_、，, ]+)");
        java.util.regex.Matcher m = attrListPattern.matcher(text);
        if (m.find()) {
            String list = m.group(2);
            String[] parts = list.split("[、，,]");
            for (String part : parts) {
                String attrName = part.trim();
                if (!attrName.isEmpty()) {
                    attrs.add(new ExtractedEntity.Attribute(attrName, "String", ""));
                }
            }
        }
        // 匹配 "属性名: 类型" 模式
        java.util.regex.Pattern attrDefPattern = java.util.regex.Pattern.compile(
                "([\\u4e00-\\u9fa5A-Za-z0-9_]+)\\s*[:：]\\s*(String|Long|Integer|Boolean|DateTime|BigDecimal|Double|Float|Int|Text|Date|\\w+)");
        m = attrDefPattern.matcher(text);
        while (m.find()) {
            String name = m.group(1).trim();
            String type = m.group(2).trim();
            if (!name.equals("类型") && !name.equals("备注") && !name.equals("说明") && !name.equals("描述")) {
                attrs.add(new ExtractedEntity.Attribute(name, type, ""));
            }
        }
        return attrs;
    }

    /**
     * 从业务模型段落中推断可能的实体名。
     */
    private static String guessEntityNameFromRequirements(java.util.List<String> paragraphs, String[] owners) {
        for (int i = 0; i < paragraphs.size(); i++) {
            if ("business-model".equals(owners[i]) && isHeading(paragraphs.get(i))) {
                String heading = paragraphs.get(i).replaceAll("^#+\\s*", "").trim();
                if (!containsAny(heading, "概述", "简介", "摘要", "列表", "需求", "功能", "流程", "角色",
                        "目的", "背景", "范围", "目标", "总体", "结论", "后续", "附录", "术语", "版本",
                        "产品", "特性", "模块", "状态", "说明", "场景", "输入", "输出", "结构",
                        "组件", "定义", "补充", "示例", "示意", "优先级", "修订", "目录", "密级",
                        "附件", "风险", "性能", "监控", "兼容")) {
                    return heading.length() > 10 ? heading.substring(0, 10) : heading;
                }
            }
        }
        return "DefaultEntity";
    }

    /**
     * 判断兜底实体名是否有意义（避免把章节标题/通用词/占位名当成实体）。
     */
    private static boolean isMeaningfulEntityName(String name) {
        if (name == null) return false;
        String n = name.trim();
        if (n.isEmpty() || "DefaultEntity".equals(n)) return false;
        if (n.length() > 12) n = n.substring(0, 12);
        if (containsAny(n, "概述", "简介", "摘要", "列表", "需求", "功能", "流程", "角色",
                "目的", "背景", "范围", "目标", "总体", "结论", "后续", "附录", "术语", "版本",
                "产品", "特性", "模块", "状态", "说明", "场景", "输入", "输出", "结构",
                "组件", "定义", "补充", "示例", "示意", "优先级", "修订", "目录", "密级",
                "附件", "风险", "性能", "监控", "兼容", "属性", "字段", "实体", "关系",
                "接口", "请求", "参数", "响应", "内容", "描述", "部分", "文档", "文件",
                "提取", "解析", "评审", "展示", "生成", "识别", "拆分", "结果", "数据表",
                "tencent", "模板", "原文", "markdown", "截图", "页面", "来源", "段落",
                "核心", "模型", "业务", "全景", "边界", "规则", "领域", "仓储", "存储方案",
                "一、", "二、", "三、", "四、", "五、", "六、", "七、", "八、")) {
            return false;
        }
        // 拦截单个通用后缀词本身（如"系统""平台""信息"），不拦"教务系统"这种前有实词的名称
        if ("系统".equals(n) || "平台".equals(n) || "模块".equals(n) || "信息".equals(n)
                || "服务".equals(n) || "数据".equals(n) || "中心".equals(n) || "管理".equals(n)
                || "菜单".equals(n) || "界面".equals(n) || "页面".equals(n) || "配置".equals(n)
                || "属性".equals(n) || "字段".equals(n) || "实体".equals(n) || "部分".equals(n)
                || "功能模块".equals(n) || "产品名".equals(n) || "子系统".equals(n)
                || "核心模型".equals(n) || "模型".equals(n) || "核心".equals(n)) {
            return false;
        }
        // 至少 2 个有效字符，纯标点/序号不算
        String letters = n.replaceAll("[^\\u4e00-\\u9fa5a-zA-Z0-9]", "");
        return letters.length() >= 2;
    }

    /**
     * 从业务模型章节的真实内容中提取候选实体（如"新闻发布中心"），
     * 返回 [name, description] 列表，最多 6 个；提取不到返回空列表。
     * 用于 LLM 失败兜底时避免把 PRD 章节标题（如"产品特性"）当作实体。
     */
    private java.util.List<String[]> extractEntityCandidatesFromBusiness(
            java.util.List<String> paragraphs, String[] owners) {
        java.util.List<String[]> result = new java.util.ArrayList<>();
        java.util.Set<String> seen = new java.util.HashSet<>();
        java.util.regex.Pattern entityPattern = java.util.regex.Pattern.compile(
                "([\\u4e00-\\u9fa5A-Za-z]{2,8}(?:中心|系统|平台|模块|管理|信息|数据|服务|板块|菜单|区域))");
        for (int i = 0; i < paragraphs.size(); i++) {
            String para = paragraphs.get(i);
            if (!"business-model".equals(owners[i])) continue;
            if (para.trim().startsWith("#")) continue;               // 标题行跳过
            if (para.contains("data:image/")) continue;              // base64 图片跳过
            if (isSkipParagraph(para.trim()) || isNoiseParagraph(para.trim())) continue;
            java.util.regex.Matcher m = entityPattern.matcher(para);
            while (m.find() && result.size() < 6) {
                // 名称里的连接词（及/和/与/的…）后通常是另一业务词，只取前段，如"校园新闻及系统公告"→"校园新闻"
                String name = m.group(1).split("及|和|与|的|并|或|以及")[0].trim();
                if (!isMeaningfulEntityName(name) || seen.contains(name)) continue;
                seen.add(name);
                String desc = para.replaceAll("^#+\\s*", "").replaceAll("\\[([^\\]]*)\\]", "$1").trim();
                if (desc.length() > 200) desc = desc.substring(0, 200);
                result.add(new String[]{name, desc});
            }
        }
        return result;
    }

    /**
     * 从架构决策段落中提取有意义的标题。
     */
    private static String extractAdTitle(String para) {
        String[] lines = para.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("#")) {
                return trimmed.replaceAll("^#+\\s*", "").trim();
            }
        }
        String clean = para.replaceAll("[\\[\\]]", "").trim();
        return clean.length() > 50 ? clean.substring(0, 50) : clean;
    }

    /**
     * 从普通段落中提取架构决策标题：以关键词位置为中心取前半句作为标题。
     */
    private static String extractAdTitleFromParagraph(String para, int keywordPos) {
        String before = para.substring(0, Math.min(keywordPos + 20, para.length()));
        int sentStart = Math.max(0, before.lastIndexOf('。'));
        if (sentStart > 0) sentStart++;
        int sentEnd = Math.min(keywordPos + 20, para.length());
        String title = para.substring(sentStart, Math.min(sentEnd, para.length())).trim();
        title = title.replaceAll("[\\[\\]#]", "").trim();
        if (title.length() > 50) title = title.substring(0, 50);
        if (title.isEmpty()) {
            title = "架构决策" + (para.hashCode() & 0xFFFF);
        }
        return title;
    }

    // ---- 内部类：结构化提取结果 ----

    /**
     * 关键词回退解析中提取的实体（含属性和关系）。
     */
    public static class ExtractedEntity {
        String name;
        String description;
        java.util.List<Attribute> attributes = new java.util.ArrayList<>();
        java.util.List<Relation> relations = new java.util.ArrayList<>();

        ExtractedEntity(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public static class Attribute {
            String name; String type; String description;
            Attribute(String name, String type, String description) {
                this.name = name; this.type = type; this.description = description;
            }
        }

        public static class Relation {
            String target; String type; String description;
            Relation(String target, String type, String description) {
                this.target = target; this.type = type; this.description = description;
            }
        }
    }

    private static String normalizeNewlines(String s) {
        if (s == null) return "";
        return s.replace("\\n", "\n");
    }

    /**
     * 转义 Markdown 表格单元格中的管道符，防止表格列断裂。
     * 将单元格内的 | 替换为 \|（HTML 实体或反斜杠转义）。
     */
    private static String escapeTableCell(String s) {
        if (s == null) return "";
        return s.replace("|", "\\|");
    }

    /**
     * 安全地包裹代码围栏：如果内容包含三重反引号，改用更长的围栏（```` 或更多），
     * 防止围栏提前闭合导致的后续内容被吞掉。
     */
    private static String safeCodeBlock(String content) {
        if (content == null) return "";
        // 检测内容中出现的最大连续反引号层数
        int maxBackticks = 0;
        int cur = 0;
        for (int i = 0; i < content.length(); i++) {
            if (content.charAt(i) == '`') {
                cur++;
                maxBackticks = Math.max(maxBackticks, cur);
            } else {
                cur = 0;
            }
        }
        // 需要围栏长度 = max + 1（至少 3）
        int fenceLen = Math.max(maxBackticks + 1, 3);
        String fence = "`".repeat(fenceLen);
        return fence + "\n" + content + "\n" + fence;
    }

    /**
     * 安全地追加列表项：支持多行内容，每行自动缩进，防止列表断裂。
     */
    private static void appendListItem(StringBuilder sb, String prefix, String content, String indent) {
        String normalized = normalizeNewlines(content);
        String[] lines = normalized.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;
            if (i == 0) {
                sb.append(indent).append(prefix).append(line).append("\n");
            } else {
                sb.append(indent).append("  ").append(line).append("\n");
            }
        }
    }

    // ---- Helper methods ----
    private String detectPriority(String para) {
        if (para.contains("P0") || para.contains("紧急") || para.contains("关键")) return "P0";
        if (para.contains("P1") || para.contains("重要")) return "P1";
        if (para.contains("P2")) return "P2";
        return "P2";
    }

    private String extractEntityName(String para) {
        // 尝试从段落中提取实体名
        String[] lines = para.split("\\n");
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("#")) {
                // 从标题提取实体名：### 5.1 用户 → 用户
                String heading = line.replaceAll("^#+\\s*", "").trim();
                heading = heading.replaceAll("^[\\d\\.]+\\s*", "").trim();
                if (heading.length() > 1 && heading.length() < 50) {
                    return heading;
                }
                continue;
            }
            if (line.length() > 3 && line.length() < 50 && !line.contains(" ")) {
                return line.replaceAll("[^a-zA-Z0-9_\\u4e00-\\u9fa5]", "");
            }
        }
        return "Entity" + Math.abs(para.hashCode() % 1000);
    }

    private String detectHttpMethod(String para) {
        String upper = para.toUpperCase();
        if (upper.contains("POST") || upper.contains("创建") || upper.contains("新增")) return "POST";
        if (upper.contains("GET") || upper.contains("查询") || upper.contains("获取")) return "GET";
        if (upper.contains("PUT") || upper.contains("更新") || upper.contains("修改")) return "PUT";
        if (upper.contains("DELETE") || upper.contains("删除")) return "DELETE";
        return "GET";
    }

    private String extractPath(String para) {
        // 尝试从段落中提取 URL 路径
        int idx = para.indexOf("/api/");
        if (idx >= 0) {
            int end = para.indexOf(" ", idx);
            if (end < 0) end = para.length();
            return para.substring(idx, Math.min(end, idx + 100)).trim();
        }
        return "/api/v1/" + "endpoint" + Math.abs(para.hashCode() % 1000);
    }

    /**
     * 判断段落是否应跳过（标题行、模板占位符、TOC 目录项、HTML 锚点段落等无实际内容段落）。
     */
    private boolean isSkipParagraph(String para) {
        if (para.startsWith("#")) return true;                     // 标题行跳过
        if (para.contains("[请在此处")) return true;               // 模板占位符跳过
        if (para.startsWith("[") && para.contains("](#")) return true; // TOC 目录项（[标题](#anchor)）跳过
        if (para.startsWith("<a id=")) return true;                // HTML 锚点段落跳过（含尾部文字）
        if (para.contains("data:image/")) return true;             // base64 图片数据跳过（二进制可能含HTTP/架构等假阳性关键词）
        return false;
    }

    /** 封面/元信息标签（"标签：" 或 "标签：" 后跟空值/短值的行）。 */
    private static final String[] PRD_META_LABELS = {
            "文档版本号", "文档编号", "文档密级", "文档名称", "归属部门", "归属部门/项目", "产品名", "产品名称",
            "子系统名", "编写人", "编写日期", "修订记录", "版本号", "版本", "修订人", "修订日期", "修订描述",
            "生效日期", "发布日期", "创建人", "创建日期", "更新人", "更新日期", "密级", "编号", "状态：",
            "审核人", "批准人", "作者", "拟制", "评审人"
    };

    /**
     * 判断段落是否为解析噪声（封面元信息、水印、版本号、模板填空说明等无实际业务内容的段落）。
     * LLM 失败走关键词回退时，在需求/接口/决策提取前调用，避免垃圾内容混入拆分文档。
     */
    static boolean isNoiseParagraph(String para) {
        if (para == null) return true;
        String p = para.trim();
        if (p.isEmpty()) return true;
        // 1) 双下划线开头：封面/水印/修订记录表头行，含未闭合的模板标记行（__流程说明：__（用例图、流程图））
        if (p.startsWith("__") && p.length() <= 80) return true;
        // 2) 水印/版权/来源行
        if (p.contains("人人都是产品经理") || p.contains("woshipm")
                || p.contains("版权所有") || p.contains("©") || p.contains("copyright")
                || p.contains("内部资料") || p.contains("注意保密") || p.contains("相互转告")) {
            return true;
        }
        // 3) 版本号行：V 1.0 / V1.0 / v1.0.0 / 版本：1.0
        if (p.matches("^(?i)v\\s?\\d+(\\.\\d+){1,3}(\\s*$|.*)") && p.length() <= 20) return true;
        // 4) 元信息标签行："标签：" 后无值或值很短
        for (String label : PRD_META_LABELS) {
            if (p.startsWith(label + "：") || p.startsWith(label + ":")) {
                String value = p.substring(p.indexOf('：') >= 0 ? p.indexOf('：') + 1 : p.indexOf(':') + 1).trim();
                if (value.isEmpty() || value.length() <= 24) return true;
            }
        }
        // 5) PRD 模板填空说明句：以 [ 开头的引导/说明文字（docx 模板残留，真实需求不会以 [ 开头）
        if (p.startsWith("[")) return true;
        // 5b) 模板引导段：此节/本节/这一节 开头的说明文字
        if (p.startsWith("此节") || p.startsWith("本节") || p.startsWith("这一节")) return true;
        // 5c) 模板表格内嵌说明行：1. 播放区：播放区定义及功能说明；
        if (p.contains("定义及功能说明") || p.contains("定义及可执行操作说明")) return true;
        // 5d) docx 表格表头/单元格碎片（整行命中才过滤，避免误伤真实需求）
        if (p.length() <= 12 && containsAny(p,
                "用户角色", "用户描述", "功能模块", "主要功能点", "功能描述", "优先级",
                "功能点1", "功能点2", "功能点3", "功能点4",
                "风险", "可能性", "严重性", "应对策略", "可应对性", "状态转换图",
                "高", "中", "低")) {
            return true;
        }
        // 5e) 纯符号/序号残留行：]、1.、2. 等
        if (p.length() <= 10 && p.replaceAll("[\\]\\[\\s.。0-9、，,（）()]", "").isEmpty()) return true;
        return false;
    }

    /**
     * 清理需求描述：去掉 docx 表格单元格合并产生的孤立右括号 ] 及首尾空格。
     */
    static String cleanReqDescription(String para) {
        if (para == null) return "";
        String t = para.trim();
        while (t.endsWith("]") && t.length() > 1) {
            t = t.substring(0, t.length() - 1).trim();
        }
        return t;
    }

    private static String truncate(String text, int maxLen) {
        if (text == null) return "";
        return text.length() <= maxLen ? text : text.substring(0, maxLen);
    }
}
