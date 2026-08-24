package com.huazai.prd.ingestion.service;

import com.huazai.prd.ingestion.config.ProjectContext;
import com.huazai.prd.ingestion.model.change.ChangeCreateRequest;
import com.huazai.prd.ingestion.model.change.ChangeDetail;
import com.huazai.prd.ingestion.model.change.ChangeSummary;
import com.huazai.prd.ingestion.model.prd.ArchitectureDecision;
import com.huazai.prd.ingestion.model.prd.DataEntity;
import com.huazai.prd.ingestion.model.prd.DocumentSummary;
import com.huazai.prd.ingestion.model.prd.InterfaceProtocol;
import com.huazai.prd.ingestion.model.prd.RequirementEntity;
import com.huazai.prd.ingestion.repository.ChangeRepository;
import com.huazai.prd.ingestion.repository.PipelineRepository;
import com.huazai.prd.ingestion.repository.PrdIngestionRepository;
import com.huazai.prd.ingestion.repository.TemplateRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Change 管理服务。
 */
@Service
public class ChangeService {

    private final ChangeRepository changeRepo;
    private final PrdIngestionRepository prdRepo;
    private final PipelineRepository pipelineRepo;
    private final TemplateRepository templateRepo;

    public ChangeService(ChangeRepository changeRepo, PrdIngestionRepository prdRepo,
                         PipelineRepository pipelineRepo, TemplateRepository templateRepo) {
        this.changeRepo = changeRepo;
        this.prdRepo = prdRepo;
        this.pipelineRepo = pipelineRepo;
        this.templateRepo = templateRepo;
    }

    public ChangeDetail createChange(ChangeCreateRequest request) {
        // 项目 ID 解析：显式传入 > 请求头 X-Project-Id > ingestion 记录反查；均无则 "default"，保证永不为 null
        String projectId = resolveProjectId(request);

        String changeId = "chg-" + Instant.now().toString()
                .replace("-", "").replace(":", "").substring(0, 14)
                + "-" + (int)(Math.random() * 1000);

        String title = request.getTitle() != null && !request.getTitle().isBlank()
                ? request.getTitle()
                : prdRepo.findTitle(request.getIngestionId());
        if (title == null || title.isBlank()) {
            title = "Change: " + changeId;
        }

        // 收集文档引用
        List<DocumentSummary> docs = request.getDocIds() != null
                ? request.getDocIds().stream()
                    .map(docId -> {
                        var doc = prdRepo.findDocument(docId);
                        if (doc != null) {
                            DocumentSummary ds = new DocumentSummary();
                            ds.setDocId(doc.getDocId());
                            ds.setType(doc.getType());
                            ds.setTitle(doc.getTitle());
                            ds.setStatus(doc.getStatus());
                            return ds;
                        }
                        return null;
                    })
                    .filter(d -> d != null)
                    .collect(Collectors.toList())
                : List.of();

        // 生成 Change 内容（章节骨架对齐 skills/harness-core/templates/changes/_TEMPLATE/change.md，优先按模板管理中的 change-model 模板渲染）
        String content = buildChangeContent(changeId, title, request.getIngestionId(), docs, projectId);

        String requirementIds = "[]";
        String dependencies = "[]";

        // 保存（携带解析后的项目 ID）
        changeRepo.insertChange(changeId, title, "drafting", content,
                request.getIngestionId(), requirementIds, dependencies, projectId);

        // 保存文档引用
        for (DocumentSummary doc : docs) {
            changeRepo.insertDocumentRef(changeId, doc.getDocId(), doc.getType(), doc.getTitle(), doc.getStatus(), projectId);
        }

        // 初始化流水线阶段
        pipelineRepo.initStages(changeId, projectId);

        // 写入日志
        pipelineRepo.insertLog(changeId, "status_change", "Change 已创建，进入起草阶段", null, "system", projectId);

        return getChangeDetail(changeId);
    }

    /**
     * 解析项目 ID：显式传入 > 请求头 X-Project-Id > ingestion 记录反查；均无则 "default"（保证永不为 null）。
     */
    private String resolveProjectId(ChangeCreateRequest request) {
        if (request.getProjectId() != null && !request.getProjectId().isBlank()) {
            return request.getProjectId();
        }
        if (ProjectContext.get() != null && !ProjectContext.get().isBlank()) {
            return ProjectContext.get();
        }
        if (request.getIngestionId() != null) {
            String fromIngestion = prdRepo.findProjectId(request.getIngestionId());
            if (fromIngestion != null && !fromIngestion.isBlank()) {
                return fromIngestion;
            }
        }
        return "default";
    }

    /**
     * 生成 change.md 内容：<b>优先严格按照「模板管理」中的 change-model 模板渲染</b>，
     * 模板缺失/渲染异常时回退原硬编码章节骨架（保证兼容旧库与降级可用）。
     *
     * <p>模板驱动逻辑与 PRD 拆分文档（如 data-model 等 4 类模板）保持一致：
     * frontmatter/标题按真实 change 信息替换；「用户故事/设计约束/契约影响/影响面/
     * 非目标/验收标准/边界情况/非功能需求/测试策略」章节按模板标题识别并用 ChangeComposer
     * 从解析产物推导填充（推导不出保留模板占位，宁缺毋滥）；尾部追加
     * 「任务拆解/验收用例/流水线进度」扩展章节。</p>
     */
    private String buildChangeContent(String changeId, String title, String ingestionId, List<DocumentSummary> docs, String projectId) {
        String template = null;
        try {
            template = templateRepo.findContentByType(projectId, "change-model");
        } catch (Exception e) {
            // 模板读取失败不影响 change 创建，走回退逻辑
        }
        if (template != null && !template.isBlank()) {
            try {
                return renderChangeFromTemplate(template, changeId, title, ingestionId, docs);
            } catch (Exception e) {
                // 模板渲染异常时回退硬编码，保证 change.md 始终可生成
            }
        }
        return buildChangeContentHardcoded(changeId, title, ingestionId, docs);
    }

    /**
     * 以「模板管理」中的 change-model 模板为骨架渲染 change.md。
     */
    private String renderChangeFromTemplate(String template, String changeId, String title,
                                            String ingestionId, List<DocumentSummary> docs) {
        // 1) frontmatter 与 H1 标题行替换为真实 change 信息
        String rendered = template
                .replace("id: C-NNN", "id: " + changeId)
                .replace("slug: <slug>", "slug: " + changeId.toLowerCase())
                .replace("status: analyzing", "status: drafting")
                .replace("created: <date>", "created: " + LocalDate.now())
                .replaceFirst("(?m)^# .*$", "# " + changeId + " " + title);
        // 修复模板中存储的 \\n 字面量为实际换行
        rendered = rendered.replace("\\n", "\n");

        // 2) 预取解析产物一次（四类 + 原始 PRD），供各章节填充与尾部扩展章节共用
        List<RequirementEntity> requirements = prdRepo.findRequirements(ingestionId);
        List<InterfaceProtocol> interfaces = prdRepo.findInterfaces(ingestionId);
        List<DataEntity> entities = prdRepo.findDataEntities(ingestionId);
        List<ArchitectureDecision> archs = prdRepo.findArchDecisions(ingestionId);
        String original = prdRepo.findContent(ingestionId);
        List<String> edgeCases = ChangeComposer.composeEdgeCases(original, requirements, interfaces, entities);

        // 各章节推导内容（未推导章节值为空串，由模板保留占位，宁缺毋滥）
        java.util.Map<String, String> sections = new java.util.LinkedHashMap<>();
        sections.put("用户故事", ChangeComposer.composeUserStory(requirements, original));
        sections.put("设计约束", buildDesignConstraintsBlock(archs));
        sections.put("契约影响", buildContractImpactBlock(interfaces, entities));
        sections.put("影响面", buildImpactBlock(docs));
        sections.put("非目标", joinLines(ChangeComposer.extractOutOfScope(original, requirements)));
        sections.put("验收标准", joinLines(ChangeComposer.composeAcceptanceCriteria(requirements)));
        sections.put("边界情况", joinLines(edgeCases));
        sections.put("非功能需求", ChangeComposer.composeNfrTable(archs));
        sections.put("测试策略", ChangeComposer.composeTestStrategy(interfaces, entities, archs, edgeCases));

        // 2b) 标题推导：从 change 标题推导各章节内容。
        // 当标题命中已知域（如 oauth-login）时，推导内容优先覆盖 PRD 解析产物——
        // 因为 ingestionId 关联的 PRD 与 change 标题意图可能不同（例如用旧 PRD
        // 数据生成新 change），此时标题推导的内容比 PRD 产物更贴合 change 实际意图。
        // 未命中域（deriveFromTitle 返回空 Map）时完全不影响现有 PRD 产物填充逻辑。
        java.util.Map<String, String> derived = ChangeComposer.deriveFromTitle(title);
        if (!derived.isEmpty()) {
            for (java.util.Map.Entry<String, String> e : derived.entrySet()) {
                sections.put(e.getKey(), e.getValue());
            }
        }

        // 3) 按模板章节标题识别，用推导内容填充（未推导章节保留模板原文占位）
        String[] lines = rendered.split("\n");
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line.startsWith("## ")) {
                String section = line.substring(3);
                String block = null;
                for (java.util.Map.Entry<String, String> e : sections.entrySet()) {
                    if (section.contains(e.getKey())) {
                        block = e.getValue();
                        break;
                    }
                }
                out.append(line).append("\n\n");
                if (block != null && !block.isBlank()) {
                    out.append(block).append("\n\n");
                    // 命中数据章节：跳过模板原有示例/占位正文，避免占位残留
                    while (i + 1 < lines.length && !lines[i + 1].startsWith("#")) {
                        i++;
                    }
                }
            } else {
                out.append(line).append("\n");
            }
        }

        // 4) 扩展章节（模板通常没有）：任务拆解 / 验收用例 / 流水线进度
        appendExtraSections(out, entities, interfaces);

        return out.toString().trim() + "\n";
    }

    /** 列表条目化：每条 "- xxx"；空列表返回空串（由调用方保留模板占位）。 */
    private static String joinLines(List<String> items) {
        if (items == null || items.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (String item : items) {
            sb.append("- ").append(item).append("\n");
        }
        return sb.toString().trim();
    }

    /** 追加模板通常缺失的扩展章节：任务拆解 / 验收用例 / 流水线进度。 */
    private void appendExtraSections(StringBuilder out, List<DataEntity> entities, List<InterfaceProtocol> interfaces) {
        String tasks = ChangeComposer.composeTasks(entities, interfaces);
        String cases = ChangeComposer.composeAcceptanceCases(interfaces);
        if (!tasks.isBlank()) {
            out.append("\n## 任务拆解\n\n").append(tasks).append("\n\n");
        }
        if (!cases.isBlank()) {
            out.append("\n## 验收用例\n\n").append(cases).append("\n\n");
        }
        out.append("## 流水线进度\n\n").append(ChangeComposer.composePipelineProgress()).append("\n");
    }

    /** 设计约束章节：来自架构决策。 */
    private String buildDesignConstraintsBlock(List<ArchitectureDecision> archs) {
        StringBuilder sb = new StringBuilder();
        if (archs.isEmpty()) {
            sb.append("- 待补充\n");
        } else {
            for (ArchitectureDecision ad : archs) {
                String text = ad.getTitle();
                if (ad.getDecision() != null && !ad.getDecision().isBlank()) {
                    text = (text == null ? "" : text) + "\n" + ad.getDecision();
                }
                appendItem(sb, null, text, "", "");
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    /** 契约影响章节：REST 接口 + 数据模型。 */
    private String buildContractImpactBlock(List<InterfaceProtocol> interfaces, List<DataEntity> entities) {
        StringBuilder sb = new StringBuilder();
        sb.append("- REST:\n");
        if (interfaces.isEmpty()) {
            sb.append("  - 待补充\n");
        } else {
            for (InterfaceProtocol i : interfaces) {
                String text = i.getMethod() + " " + i.getPath();
                if (i.getSummary() != null && !i.getSummary().isBlank()) {
                    text = text + "\n" + i.getSummary();
                }
                appendItem(sb, null, text, "", "  ");
                sb.append("\n");
            }
        }
        sb.append("- 模块间通信: TBD\n");
        sb.append("- 数据模型:");
        if (entities.isEmpty()) {
            sb.append(" TBD\n");
        } else {
            sb.append("\n");
            for (DataEntity e : entities) {
                String text = e.getName();
                if (e.getDescription() != null && !e.getDescription().isBlank()) {
                    text = text + "\n" + e.getDescription();
                }
                appendItem(sb, null, text, "", "  ");
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    /** 影响面章节：涉及文档引用。 */
    private String buildImpactBlock(List<DocumentSummary> docs) {
        StringBuilder sb = new StringBuilder();
        sb.append("- 模块/服务: TBD\n");
        sb.append("- 外部 API: TBD\n");
        sb.append("- wiki:\n");
        if (docs.isEmpty()) {
            sb.append("  - 待补充\n");
        } else {
            for (DocumentSummary doc : docs) {
                sb.append("  - ").append(doc.getTitle()).append(" (").append(doc.getType()).append(")\n");
            }
        }
        return sb.toString();
    }

    /**
     * 回退实现：按参考模板（skills/harness-core/templates/changes/_TEMPLATE/change.md）的章节骨架
     * 生成 change.md 内容。仅在模板管理无 change-model 模板或渲染异常时使用。
     */
    private String buildChangeContentHardcoded(String changeId, String title, String ingestionId, List<DocumentSummary> docs) {
        StringBuilder sb = new StringBuilder();

        // frontmatter
        sb.append("---\n");
        sb.append("id: ").append(changeId).append("\n");
        sb.append("slug: ").append(changeId.toLowerCase()).append("\n");
        sb.append("status: drafting\n");
        sb.append("created: ").append(LocalDate.now()).append("\n");
        sb.append("---\n\n");

        sb.append("# ").append(changeId).append(" ").append(title).append("\n\n");

        // 标题推导：命中已知域时，推导内容优先于 PRD 产物（标题意图 ≠ PRD 数据）
        java.util.Map<String, String> derived = ChangeComposer.deriveFromTitle(title);
        boolean titleDerived = !derived.isEmpty();

        // 用户故事：标题推导优先，否则用 PRD 需求（ChangeComposer 过滤 base64 图片等噪音，并补总括句）
        List<RequirementEntity> requirements = prdRepo.findRequirements(ingestionId);
        String original = prdRepo.findContent(ingestionId);
        String story = titleDerived ? derived.getOrDefault("用户故事", "") : ChangeComposer.composeUserStory(requirements, original);
        sb.append("## 用户故事\n\n");
        if (story == null || story.isBlank()) {
            story = "- 待补充\n";
        }
        sb.append(story).append("\n\n");

        // 推导章节共用数据（一次拉取，供非目标/验收/边界/非功能/测试策略使用）
        List<InterfaceProtocol> interfaces = prdRepo.findInterfaces(ingestionId);
        List<DataEntity> entities = prdRepo.findDataEntities(ingestionId);
        List<ArchitectureDecision> archs = prdRepo.findArchDecisions(ingestionId);
        List<String> edgeCases = titleDerived
                ? java.util.Arrays.asList(derived.getOrDefault("边界情况", "").split("\n"))
                : ChangeComposer.composeEdgeCases(original, requirements, interfaces, entities);
        if (edgeCases.size() == 1 && edgeCases.get(0).isEmpty()) edgeCases = java.util.List.of();

        List<String> outOfScope = ChangeComposer.extractOutOfScope(original, requirements);
        sb.append("## 非目标（Out of Scope）\n\n");
        String derivedOos = derived.get("非目标");
        if (titleDerived && derivedOos != null) {
            sb.append(derivedOos).append("\n\n");
        } else if (outOfScope.isEmpty()) {
            sb.append("- 待补充\n\n");
        } else {
            sb.append(joinLines(outOfScope)).append("\n\n");
        }

        List<String> acs = titleDerived
                ? java.util.Arrays.asList(derived.getOrDefault("验收标准", "").split("\n"))
                : ChangeComposer.composeAcceptanceCriteria(requirements);
        if (acs.size() == 1 && acs.get(0).isEmpty()) acs = java.util.List.of();
        sb.append("## 验收标准\n\n");
        if (acs.isEmpty()) {
            sb.append("- AC-1: 待补充\n\n");
        } else {
            sb.append(joinLines(acs)).append("\n\n");
        }

        sb.append("## 边界情况\n\n");
        if (edgeCases.isEmpty()) {
            sb.append("- 待补充\n\n");
        } else {
            sb.append(joinLines(edgeCases)).append("\n\n");
        }

        String nfr = titleDerived ? derived.getOrDefault("非功能需求", "") : ChangeComposer.composeNfrTable(archs);
        sb.append("## 非功能需求\n\n");
        if (nfr == null || nfr.isBlank()) {
            sb.append("| 维度 | 指标 |\n|------|------|\n");
            sb.append("| 性能 | TBD |\n| 可靠性 | TBD |\n| 安全 | TBD |\n\n");
        } else {
            sb.append(nfr).append("\n\n");
        }

        // 设计约束：标题推导优先，否则用架构决策
        sb.append("## 设计约束\n\n");
        String derivedConstraints = titleDerived ? derived.get("设计约束") : null;
        if (derivedConstraints != null) {
            sb.append(derivedConstraints).append("\n\n");
        } else if (archs.isEmpty()) {
            sb.append("- 待补充\n\n");
        } else {
            for (ArchitectureDecision ad : archs) {
                String text = ad.getTitle();
                if (ad.getDecision() != null && !ad.getDecision().isBlank()) {
                    text = (text == null ? "" : text) + "\n" + ad.getDecision();
                }
                appendItem(sb, null, text, "", "");
                sb.append("\n");
            }
            sb.append("\n");
        }

        // 契约影响：标题推导优先，否则用接口/实体
        sb.append("## 契约影响\n\n");
        String derivedContract = titleDerived ? derived.get("契约影响") : null;
        if (derivedContract != null) {
            sb.append(derivedContract).append("\n\n");
        } else {
            sb.append("- REST:\n");
            if (interfaces.isEmpty()) {
                sb.append("  - 待补充\n");
            } else {
                for (InterfaceProtocol i : interfaces) {
                    String text = i.getMethod() + " " + i.getPath();
                    if (i.getSummary() != null && !i.getSummary().isBlank()) {
                        text = text + "\n" + i.getSummary();
                    }
                    appendItem(sb, null, text, "", "  ");
                    sb.append("\n");
                }
            }
            sb.append("- 模块间通信: TBD\n");
            sb.append("- 数据模型:");
            if (entities.isEmpty()) {
                sb.append(" TBD\n");
            } else {
                sb.append("\n");
                for (DataEntity e : entities) {
                    String text = e.getName();
                    if (e.getDescription() != null && !e.getDescription().isBlank()) {
                        text = text + "\n" + e.getDescription();
                    }
                    appendItem(sb, null, text, "", "  ");
                    sb.append("\n");
                }
            }
            sb.append("\n\n");
        }

        // 影响面：标题推导优先，否则用文档列表
        sb.append("## 影响面\n\n");
        String derivedImpact = titleDerived ? derived.get("影响面") : null;
        if (derivedImpact != null) {
            sb.append(derivedImpact).append("\n\n");
        } else {
            sb.append("- 模块/服务: TBD\n");
            sb.append("- 外部 API: TBD\n");
            sb.append("- wiki:\n");
            if (docs.isEmpty()) {
                sb.append("  - 待补充\n");
            } else {
                for (DocumentSummary doc : docs) {
                    sb.append("  - ").append(doc.getTitle()).append(" (").append(doc.getType()).append(")\n");
                }
            }
            sb.append("\n");
        }

        // 测试策略：标题推导优先，否则用 PRD 产物组合
        sb.append("## 测试策略\n\n");
        String testStrategy = titleDerived ? derived.getOrDefault("测试策略", "") : ChangeComposer.composeTestStrategy(interfaces, entities, archs, edgeCases);
        if (testStrategy == null || testStrategy.isBlank()) {
            testStrategy = "- 待补充\n";
        }
        sb.append(testStrategy).append("\n\n");

        appendExtraSections(sb, entities, interfaces);

        return sb.toString();
    }

    /**
     * 渲染「条目 + 多行内容」为 Typora / GitHub 均可解析的 markdown。
     *
     * <p>条目标题行进列表项（可带后缀如优先级）；正文中的表格、标题、
     * 代码围栏从列表中剥离为独立块（空行分隔、顶格），保证解析器能正确识别；
     * 普通行、子列表行、引用行保留为列表项的嵌套内容（缩进输出）。</p>
     */
    static void appendItem(StringBuilder sb, String id, String text, String suffix, String indent) {
        // PRD 解析数据中段落换行可能存为字面 "\\n"（反斜杠 n），先归一化为真换行再按行处理
        String normalized = (text == null ? "" : text).replace("\\n", "\n");
        String[] raw = normalized.split("\n");
        List<String> lines = new ArrayList<>();
        for (String l : raw) lines.add(l);

        // 摘要行：首个非空且非结构性行；全为结构行时取首行去结构标记
        int summaryIdx = -1;
        for (int i = 0; i < lines.size(); i++) {
            String t = lines.get(i).trim();
            if (t.isEmpty()) continue;
            if (!isStructuralLine(t)) {
                summaryIdx = i;
                break;
            }
        }
        String summary;
        if (summaryIdx >= 0) {
            summary = lines.get(summaryIdx).trim();
        } else {
            // 内容全为结构块（表格/标题/围栏）时，直接用占位摘要，避免把表格行当条目标题
            summary = id != null ? "（详见下方内容）" : "（详见下方内容）";
        }

        sb.append(indent).append("- ");
        if (id != null) {
            sb.append("[").append(id).append("] ");
        }
        sb.append(summary);
        if (suffix != null && !suffix.isBlank()) {
            sb.append(suffix);
        }
        sb.append("\n");

        appendContentBlock(sb, lines, summaryIdx + 1, indent + "  ");
    }

    /** 输出条目的正文内容：结构性块剥离为独立块，普通内容作为列表项嵌套行。 */
    private static void appendContentBlock(StringBuilder sb, List<String> lines, int start, String pad) {
        int i = start;
        while (i < lines.size()) {
            String t = lines.get(i).trim();
            if (t.isEmpty()) {
                i++;
                continue;
            }

            if (t.startsWith("```") || t.startsWith("~~~")) {
                // 代码围栏 → 独立块（自动补闭合，防止半开围栏吞掉后续内容）
                String fence = t;
                StringBuilder code = new StringBuilder();
                i++;
                boolean closed = false;
                while (i < lines.size()) {
                    String l = lines.get(i);
                    if (l.trim().matches("^(```+|~~~+).*")) {
                        i++;
                        closed = true;
                        break;
                    }
                    code.append(l).append("\n");
                    i++;
                }
                sb.append("\n").append(fence).append("\n");
                if (code.length() > 0) sb.append(code);
                // 结尾围栏行：已闭合按原文输出，未闭合时补默认 ``` 防止吞掉后续内容
                sb.append(closed ? fence : "```").append("\n\n");
            } else if (t.startsWith("|")) {
                // 表格 → 独立表格块（表头、分隔行、数据行连续收集）
                List<String> rows = new ArrayList<>();
                while (i < lines.size()) {
                    String l = lines.get(i).trim();
                    if (l.startsWith("|")) {
                        rows.add(l);
                        i++;
                    } else {
                        break;
                    }
                }
                sb.append("\n");
                for (String row : rows) sb.append(row).append("\n");
                sb.append("\n");
            } else if (t.startsWith("#")) {
                // 标题行 → 独立标题块
                sb.append("\n").append(t).append("\n\n");
                i++;
            } else {
                // 普通/引用/子列表行 → 列表项嵌套内容（原样缩进输出，保持源信息）
                boolean any = false;
                while (i < lines.size()) {
                    String l = lines.get(i);
                    String lt = l.trim();
                    if (lt.isEmpty() || isStructuralLine(lt)) break;
                    sb.append(pad).append(lt).append("\n");
                    any = true;
                    i++;
                }
                if (any) sb.append("\n");
            }
        }
    }

    /** 结构性 markdown 行（表格 / 标题 / 代码围栏）——必须从列表项中剥离。 */
    private static boolean isStructuralLine(String t) {
        return t.startsWith("|") || t.startsWith("#")
                || t.startsWith("```") || t.startsWith("~~~");
    }

    public List<ChangeSummary> getChanges(int page, int size) {
        List<ChangeSummary> changes = changeRepo.findChanges(page, size, ProjectContext.get());
        for (ChangeSummary change : changes) {
            change.setDocumentRefs(changeRepo.findDocumentRefs(change.getChangeId()));
        }
        return changes;
    }

    public ChangeDetail getChangeDetail(String changeId) {
        ChangeDetail detail = changeRepo.findChangeDetail(changeId);
        if (detail == null) return null;
        detail.setDocumentRefs(changeRepo.findDocumentRefs(changeId));
        detail.setAcceptanceCriteria(changeRepo.findAcceptanceCriteria(changeId));
        return detail;
    }

    /**
     * 更新 Change 的 markdown 内容（人工编辑后保存）。
     */
    public ChangeDetail updateChangeContent(String changeId, String content) {
        changeRepo.updateChangeContent(changeId, content);
        // 写入日志
        pipelineRepo.insertLog(changeId, "status_change", "Change 内容已手动更新", null, "system", ProjectContext.get());
        return getChangeDetail(changeId);
    }

    /**
     * 重新生成 Change 内容（覆盖已有 change，不新建 changeId）。
     *
     * <p>语义：基于已有 changeId 关联的 ingestionId，重新拉取最新解析产物，
     * 重新渲染 change.md 内容，UPDATE 覆盖原 content。同时清除并重建文档引用。
     * 不新建 changeId、不重置流水线阶段，保持 status 不变。</p>
     */
    public ChangeDetail regenerateChange(String changeId) {
        ChangeDetail existing = changeRepo.findChangeDetail(changeId);
        if (existing == null) return null;

        // 从现有 change 获取 ingestionId
        String ingestionId = changeRepo.findIngestionId(changeId);
        if (ingestionId == null || ingestionId.isBlank()) {
            return null;
        }

        String projectId = changeRepo.findProjectId(changeId);
        if (projectId == null || projectId.isBlank()) projectId = "default";

        // 重新收集文档引用（基于最新解析产物）
        List<DocumentSummary> docs = prdRepo.findDocumentSummaries(ingestionId);
        if (docs == null) docs = List.of();

        // 重新生成 change.md 内容
        String content = buildChangeContent(changeId, existing.getTitle(), ingestionId, docs, projectId);

        // UPDATE 覆盖 content（不新建 changeId，不重置 status）
        changeRepo.updateChangeContent(changeId, content);

        // 清除并重建文档引用
        changeRepo.deleteDocumentRefs(changeId);
        for (DocumentSummary doc : docs) {
            changeRepo.insertDocumentRef(changeId, doc.getDocId(), doc.getType(), doc.getTitle(), doc.getStatus(), projectId);
        }

        // 写入日志
        pipelineRepo.insertLog(changeId, "status_change", "Change 内容已重新生成（覆盖旧内容）", null, "system", projectId);

        return getChangeDetail(changeId);
    }
}