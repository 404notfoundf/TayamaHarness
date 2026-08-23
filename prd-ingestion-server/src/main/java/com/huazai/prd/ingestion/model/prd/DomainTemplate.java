package com.huazai.prd.ingestion.model.prd;

import java.util.ArrayList;
import java.util.List;

/**
 * 领域模板：预定义的常见业务场景知识模板。
 *
 * <p>当 PRD 内容匹配模板关键词时，直接实例化模板生成结构化输出，
 * 无需调用 LLM 或走关键词匹配。适用于登录、支付、审批等高频场景。</p>
 */
public class DomainTemplate {

    /** 模板名称 */
    private String name;

    /** 匹配关键词列表（至少命中 MIN_HITS 个不同关键词才会命中，避免界面文案/通用词误判） */
    private List<String> keywords = new ArrayList<>();

    /** 模板命中所需的最少不同关键词数 */
    public static final int MIN_HITS = 3;

    /** 需求列表 */
    private List<PrdParseResult.RequirementItem> requirements = new ArrayList<>();

    /** 数据实体 */
    private List<PrdParseResult.EntityItem> dataEntities = new ArrayList<>();

    /** 接口定义 */
    private List<PrdParseResult.InterfaceItem> interfaces = new ArrayList<>();

    /** 架构决策 */
    private List<PrdParseResult.ArchDecisionItem> archDecisions = new ArrayList<>();

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<String> getKeywords() { return keywords; }
    public void setKeywords(List<String> keywords) { this.keywords = keywords; }

    public List<PrdParseResult.RequirementItem> getRequirements() { return requirements; }
    public void setRequirements(List<PrdParseResult.RequirementItem> requirements) { this.requirements = requirements; }

    public List<PrdParseResult.EntityItem> getDataEntities() { return dataEntities; }
    public void setDataEntities(List<PrdParseResult.EntityItem> dataEntities) { this.dataEntities = dataEntities; }

    public List<PrdParseResult.InterfaceItem> getInterfaces() { return interfaces; }
    public void setInterfaces(List<PrdParseResult.InterfaceItem> interfaces) { this.interfaces = interfaces; }

    public List<PrdParseResult.ArchDecisionItem> getArchDecisions() { return archDecisions; }
    public void setArchDecisions(List<PrdParseResult.ArchDecisionItem> archDecisions) { this.archDecisions = archDecisions; }

    /**
     * 统计 PRD 内容命中多少个不同关键词（大小写不敏感）。
     */
    public int countKeywordHits(String lowerContent) {
        if (lowerContent == null || lowerContent.isBlank()) return 0;
        int hits = 0;
        for (String kw : keywords) {
            if (lowerContent.contains(kw.toLowerCase())) hits++;
        }
        return hits;
    }

    /** 检查 PRD 内容是否匹配此模板（至少命中 MIN_HITS 个不同关键词，防止单关键词误判） */
    public boolean matches(String content) {
        if (content == null || content.isBlank()) return false;
        return countKeywordHits(content.toLowerCase()) >= MIN_HITS;
    }

    /** 将模板实例化为 PrdParseResult */
    public PrdParseResult instantiate() {
        PrdParseResult result = new PrdParseResult();
        result.setRequirements(new ArrayList<>(requirements));
        result.setDataEntities(new ArrayList<>(dataEntities));
        result.setInterfaces(new ArrayList<>(interfaces));
        result.setArchDecisions(new ArrayList<>(archDecisions));
        return result;
    }
}