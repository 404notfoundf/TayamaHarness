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
     * frontmatter/标题按真实 change 信息替换；「用户故事/设计约束/契约影响/影响面」
     * 章节按模板标题识别并用 PRD 解析产物填充，其余章节（验收标准/边界情况/非功能需求/
     * 测试策略等）严格保留模板原文的占位结构，交由后续 harness 流程完善。</p>
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

        String[] lines = rendered.split("\n");
        StringBuilder out = new StringBuilder();

        // 2) 按模板章节标题识别，用 PRD 解析产物填充对应章节（未命中章节保留模板原文占位）
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line.startsWith("## ")) {
                String section = line.substring(3);
                String block = null;
                if (section.contains("用户故事")) block = buildUserStoriesBlock(ingestionId, section);
                else if (section.contains("设计约束")) block = buildDesignConstraintsBlock(ingestionId, section);
                else if (section.contains("契约影响")) block = buildContractImpactBlock(ingestionId, section);
                else if (section.contains("影响面")) block = buildImpactBlock(docs, section);
                out.append(line).append("\n\n");
                if (block != null) {
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
        return out.toString().trim() + "\n";
    }

    /** 用户故事章节：来自 PRD 需求列表（无需求时保留占位说明）。 */
    private String buildUserStoriesBlock(String ingestionId, String section) {
        List<RequirementEntity> requirements = prdRepo.findRequirements(ingestionId);
        StringBuilder sb = new StringBuilder();
        if (requirements.isEmpty()) {
            sb.append("- 待补充\n");
        } else {
            for (RequirementEntity r : requirements) {
                String suffix = r.getPriority() != null && !r.getPriority().isBlank()
                        ? "（优先级 " + r.getPriority() + "）" : "";
                appendItem(sb, r.getId(), r.getDescription(), suffix, "");
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    /** 设计约束章节：来自架构决策。 */
    private String buildDesignConstraintsBlock(String ingestionId, String section) {
        List<ArchitectureDecision> archs = prdRepo.findArchDecisions(ingestionId);
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
    private String buildContractImpactBlock(String ingestionId, String section) {
        List<InterfaceProtocol> interfaces = prdRepo.findInterfaces(ingestionId);
        List<DataEntity> entities = prdRepo.findDataEntities(ingestionId);
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
    private String buildImpactBlock(List<DocumentSummary> docs, String section) {
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

        // 用户故事：来自 PRD 需求
        List<RequirementEntity> requirements = prdRepo.findRequirements(ingestionId);
        sb.append("## 用户故事\n\n");
        if (requirements.isEmpty()) {
            sb.append("- 待补充\n");
        } else {
            for (RequirementEntity r : requirements) {
                String suffix = r.getPriority() != null && !r.getPriority().isBlank()
                        ? "（优先级 " + r.getPriority() + "）" : "";
                appendItem(sb, r.getId(), r.getDescription(), suffix, "");
                sb.append("\n");
            }
        }
        sb.append("\n");

        sb.append("## 非目标（Out of Scope）\n\n- 待补充\n\n");

        sb.append("## 验收标准\n\n- AC-1: 待补充\n\n");

        sb.append("## 边界情况\n\n- 待补充\n\n");

        sb.append("## 非功能需求\n\n");
        sb.append("| 维度 | 指标 |\n|------|------|\n");
        sb.append("| 性能 | TBD |\n| 可靠性 | TBD |\n| 安全 | TBD |\n\n");

        // 设计约束：来自架构决策
        List<ArchitectureDecision> archs = prdRepo.findArchDecisions(ingestionId);
        sb.append("## 设计约束\n\n");
        if (archs.isEmpty()) {
            sb.append("- 待补充\n");
        } else {
            for (ArchitectureDecision ad : archs) {
                String text = ad.getTitle();
                if (ad.getDecision() != null && !ad.getDecision().isBlank()) {
                    text = (text == null ? "" : text) + "\n" + ad.getDecision();
                }
                // 标题与决策合并渲染为条目 + 内容块，不再用「：」硬拼
                appendItem(sb, null, text, "", "");
                sb.append("\n");
            }
        }
        sb.append("\n");

        // 契约影响：来自接口协议与数据实体
        List<InterfaceProtocol> interfaces = prdRepo.findInterfaces(ingestionId);
        List<DataEntity> entities = prdRepo.findDataEntities(ingestionId);
        sb.append("## 契约影响\n\n");
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
        sb.append("\n");

        // 影响面：涉及文档
        sb.append("## 影响面\n\n");
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

        sb.append("## 测试策略\n\n");
        sb.append("- 先写失败测试: TBD\n");
        sb.append("- 边界测试: TBD\n");
        sb.append("- 降级测试: TBD\n");

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
}