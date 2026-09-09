package com.tayama.prd.ingestion.service;

import com.tayama.prd.ingestion.model.prd.CandidateStatus;
import com.tayama.prd.ingestion.model.prd.DataEntity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 数据模型生成器。
 *
 * <p>策略：</p>
 * <ul>
 *   <li>confirmed：数据模型/数据字典/表结构章节的表格与实体定义文本（表格行 → 属性）；</li>
 *   <li>proposed：业务章节中出现的“X中心/X系统/X平台”类业务名词推导为候选实体，
 *       仅接受 2-12 字的名词短语，含界面动词（点击/引导/链接…）的片段一律拒绝——
 *       宁缺毋滥，绝不产生“点击其他引导到该板块”这类假实体；</li>
 *   <li>候选实体自带系统默认字段（id/createdAt…），标记为推导。</li>
 * </ul>
 */
final class DataEntityExtractor {

    private static final Pattern ENTITY_CANDIDATE = Pattern.compile(
            "([\\u4e00-\\u9fa5A-Za-z]{2,8}(?:中心|系统|平台|模块|管理|信息|数据|服务|板块|菜单|区域))");
    private static final Pattern CONJUNCTIONS = Pattern.compile("及|和|与|的|并|或|以及");
    /** 界面动词：出现即拒绝（不可能是实体名）。 */
    private static final String[] VERB_NOISE = {
            "点击", "引导", "链接", "进入", "选择", "查看", "切换", "跳转", "点击后", "点击其他",
            "按", "当用户", "如果", "可", "则", "请"
    };
    private static final String[] SKIP_PREFIXES = {"该", "此", "本", "这", "那", "其它", "其他", "如下"};

    private static final int MAX_ENTITIES = 8;

    private DataEntityExtractor() {
    }

    static List<DataEntity> extract(List<SectionNode> tree) {
        List<DataEntity> out = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        // Phase 1：数据模型章节（confirmed）
        for (SectionNode node : PrdSectionParser.nodesOfType(tree, "data-model")) {
            extractFromChapter(node, out, seen);
        }
        // Phase 2：业务章节推导候选（proposed），仅在 confirmed 不足时补充
        if (out.size() < MAX_ENTITIES) {
            for (SectionNode node : PrdSectionParser.nodesOfType(tree, "business-model")) {
                for (String para : node.paragraphs) {
                    if (PrdIngestionService.isNoiseParagraph(para.trim())) {
                        continue;
                    }
                    Matcher m = ENTITY_CANDIDATE.matcher(para);
                    while (m.find() && out.size() < MAX_ENTITIES) {
                        String name = CONJUNCTIONS.split(m.group(1))[0].trim();
                        if (!isMeaningfulCandidate(name) || !seen.add(name)) {
                            continue;
                        }
                        out.add(candidate(name, node.rawHeading));
                    }
                }
            }
        }
        return out;
    }

    /** data-model 章节内的表格 / 实体定义文本。 */
    private static void extractFromChapter(SectionNode node, List<DataEntity> out, Set<String> seen) {
        String[] lines = node.paragraphs.stream().map(String::trim).toArray(String[]::new);
        List<String[]> rows = PrdIngestionService.extractTableRows(lines);
        if (!rows.isEmpty()) {
            String entityName = PrdIngestionService.guessEntityNameBeforeTable(lines, rows);
            if (entityName.isEmpty()) {
                entityName = node.title;
            }
            if (entityName.length() >= 2 && seen.add(entityName)) {
                DataEntity e = new DataEntity();
                e.setName(entityName);
                e.setDescription("来自 PRD 数据模型章节");
                e.setSourceParagraph(node.rawHeading);
                e.setCandidateStatus(CandidateStatus.CONFIRMED);
                e.setAttributes(attributesFromRows(rows));
                out.add(e);
            }
        } else {
            // 文本型定义：“实体名：字段A、字段B” / 实体描述段落
            for (String para : node.paragraphs) {
                if (PrdIngestionService.isNoiseParagraph(para.trim())) {
                    continue;
                }
                String name = extractInlineEntityName(para);
                if (name != null && seen.add(name)) {
                    DataEntity e = new DataEntity();
                    e.setName(name);
                    e.setDescription(para.length() > 200 ? para.substring(0, 200) : para);
                    e.setSourceParagraph(node.rawHeading);
                    e.setCandidateStatus(CandidateStatus.CONFIRMED);
                    e.setAttributes(defaultAttributes());
                    out.add(e);
                }
            }
        }
    }

    private static List<DataEntity.Attribute> attributesFromRows(List<String[]> rows) {
        List<DataEntity.Attribute> attrs = new ArrayList<>();
        for (String[] row : rows) {
            if (row.length < 2) {
                continue;
            }
            String field = row[0];
            if (field.matches("(?i)(字段|名称|列名|属性|序号|no|name|field)")) {
                continue; // 跳过表头
            }
            DataEntity.Attribute a = new DataEntity.Attribute();
            a.setName(field);
            a.setType(guessType(row.length >= 3 ? row[1] : ""));
            a.setDescription(row[row.length - 1].length() > 100
                    ? row[row.length - 1].substring(0, 100) : row[row.length - 1]);
            attrs.add(a);
        }
        return attrs;
    }

    private static String guessType(String raw) {
        String t = raw == null ? "" : raw.trim();
        if (t.isEmpty()) return "String";
        if (t.contains("int") || t.contains("整数") || t.contains("number")) return "Integer";
        if (t.contains("date") || t.contains("时间") || t.contains("日期")) return "DateTime";
        if (t.contains("bool") || t.contains("布尔")) return "Boolean";
        if (t.contains("long") || t.contains("长文本")) return "LongText";
        if (t.contains("decimal") || t.contains("金额") || t.contains("double")) return "Decimal";
        return "String";
    }

    /** 文本型实体定义：匹配 “实体名：字段A、字段B” 等。 */
    private static String extractInlineEntityName(String para) {
        Matcher m = Pattern.compile("^([\\u4e00-\\u9fa5A-Za-z]{2,12})(?:实体|表|：|:)").matcher(para.trim());
        if (m.find()) {
            String name = m.group(1);
            return isMeaningfulCandidate(name) ? name : null;
        }
        return null;
    }

    private static DataEntity candidate(String name, String sourceHeading) {
        DataEntity e = new DataEntity();
        e.setName(name);
        e.setDescription("由业务章节内容推导的候选实体，需人工确认");
        e.setSourceParagraph(sourceHeading);
        e.setCandidateStatus(CandidateStatus.PROPOSED);
        e.setAttributes(defaultAttributes());
        return e;
    }

    /** 候选实体系统默认字段（标记为推导生成）。 */
    private static List<DataEntity.Attribute> defaultAttributes() {
        List<DataEntity.Attribute> attrs = new ArrayList<>();
        attrs.add(attr("id", "Long", "主键（系统生成）"));
        attrs.add(attr("created_at", "DateTime", "创建时间（系统生成）"));
        attrs.add(attr("updated_at", "DateTime", "更新时间（系统生成）"));
        return attrs;
    }

    private static DataEntity.Attribute attr(String name, String type, String desc) {
        DataEntity.Attribute a = new DataEntity.Attribute();
        a.setName(name);
        a.setType(type);
        a.setDescription(desc);
        return a;
    }

    /** 候选名词质量门禁：长度 2-12、非代词开头、不含界面动词与介词。 */
    static boolean isMeaningfulCandidate(String name) {
        if (name == null || name.length() < 2 || name.length() > 12) {
            return false;
        }
        for (String v : VERB_NOISE) {
            if (name.contains(v)) {
                return false;
            }
        }
        for (String p : SKIP_PREFIXES) {
            if (name.startsWith(p)) {
                return false;
            }
        }
        return true;
    }
}