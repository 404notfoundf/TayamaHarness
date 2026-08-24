package com.huazai.prd.ingestion.service;

import com.huazai.prd.ingestion.model.prd.RequirementEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 业务模型生成器：从章节树中聚合需求。
 *
 * <p>策略（替代旧的「每个 business-model 段落直抄一条 REQ」）：</p>
 * <ul>
 *   <li>特性/需求点章节（功能点、功能模块、用户故事、用户场景、需求描述、特性等）：整章正文聚合为一条结构化需求；</li>
 *   <li>产品概述/总体流程/功能摘要等业务章节：正文按段生成需求（保证完整性）；</li>
 *   <li>背景信息章节（简介、目的、范围、用户角色、风险分析、相关文档、附件）不生成需求。</li>
 * </ul>
 */
final class RequirementExtractor {

    /** 特性/需求点章节标题关键词：整章聚合为一条需求。 */
    private static final Set<String> FEATURE_TITLES = new LinkedHashSet<>(Arrays.asList(
            "功能点", "功能模块", "功能概述", "用户故事", "User Story", "Feature",
            "业务流程", "用户场景", "需求描述", "需求分析", "特性说明", "特性"));

    /** 背景信息章节：不生成需求。 */
    private static final Set<String> BACKGROUND_TITLES = new LinkedHashSet<>(Arrays.asList(
            "简介", "目的", "范围", "用户角色", "用户角色描述", "风险分析", "相关文档", "附件", "目标"));

    private static final int MAX_DESC_LEN = 500;

    private RequirementExtractor() {
    }

    static List<RequirementEntity> extract(List<SectionNode> tree) {
        List<RequirementEntity> out = new ArrayList<>();
        for (SectionNode node : PrdSectionParser.nodesOfType(tree, "business-model")) {
            if (node.isRoot() || node.title == null || node.title.isBlank()) {
                continue;
            }
            if (isBackground(node.title)) {
                continue;
            }
            if (isFeature(node.title)) {
                String desc = aggregate(node);
                if (!desc.isEmpty()) {
                    out.add(build(node, "REQ-" + (out.size() + 1), desc));
                }
                // 子章节（如“特性说明”下的各个特性）会在此循环中继续被处理
            } else {
                // 普通业务章节（产品概述/总体流程/目标等）：正文逐段生成
                for (String para : node.paragraphs) {
                    if (PrdIngestionService.isNoiseParagraph(para.trim())) {
                        continue;
                    }
                    String cleaned = PrdIngestionService.cleanReqDescription(para);
                    if (cleaned.length() < 8 || isPreamble(cleaned)) {
                        continue;
                    }
                    out.add(build(node, "REQ-" + (out.size() + 1), cleaned));
                }
            }
        }
        return dedupe(out);
    }

    /** 将章节自身正文段聚合为一条需求描述（去噪声、去重、限长）。 */
    static String aggregate(SectionNode node) {
        List<String> parts = new ArrayList<>();
        for (String para : node.paragraphs) {
            if (PrdIngestionService.isNoiseParagraph(para.trim())) {
                continue;
            }
            String cleaned = PrdIngestionService.cleanReqDescription(para);
            if (cleaned.length() < 8) {
                continue;
            }
            if (!parts.contains(cleaned)) {
                parts.add(cleaned);
            }
        }
        String joined = String.join("\n", parts);
        if (joined.length() > MAX_DESC_LEN) {
            return joined.substring(0, MAX_DESC_LEN) + "…";
        }
        return joined;
    }

    private static RequirementEntity build(SectionNode node, String id, String description) {
        RequirementEntity req = new RequirementEntity();
        req.setId(id);
        req.setDescription(description);
        req.setPriority(detectPriority(node.title + "\n" + description));
        req.setSourceParagraph(node.rawHeading);
        req.setCandidateStatus(com.huazai.prd.ingestion.model.prd.CandidateStatus.CONFIRMED);
        return req;
    }

    private static String detectPriority(String text) {
        if (text.contains("P0") || text.contains("紧急") || text.contains("关键")) return "P0";
        if (text.contains("P1") || text.contains("重要")) return "P1";
        if (text.contains("P3")) return "P3";
        return "P2";
    }

    private static boolean isFeature(String title) {
        for (String kw : FEATURE_TITLES) {
            if (title.contains(kw)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isBackground(String title) {
        for (String kw : BACKGROUND_TITLES) {
            if (title.contains(kw)) {
                return true;
            }
        }
        return false;
    }

    /** 文档导语类段落（“本文档为…验证…依据”）不算需求。 */
    private static boolean isPreamble(String text) {
        return text.startsWith("本文档") || text.startsWith("本文为") || text.startsWith("本产品")
                || text.startsWith("文档为") || text.startsWith("内容如");
    }

    private static List<RequirementEntity> dedupe(List<RequirementEntity> list) {
        List<RequirementEntity> out = new ArrayList<>();
        Set<String> seenDesc = new LinkedHashSet<>();
        for (RequirementEntity r : list) {
            if (seenDesc.add(r.getDescription())) {
                out.add(r);
            }
        }
        return out;
    }
}