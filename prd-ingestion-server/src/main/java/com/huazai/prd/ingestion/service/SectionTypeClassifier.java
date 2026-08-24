package com.huazai.prd.ingestion.service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 四类文档的关键词评分与类型判定（共享词表，供章节树解析与旧版兼容方法使用）。
 *
 * <p>类型优先级：data-model &gt; interface-protocol &gt; architecture-decision &gt; business-model；
 * 同一文本只归属一个类型。词表从 {@code PrdIngestionService} 迁移至此，保证新旧链路口径一致。</p>
 */
final class SectionTypeClassifier {

    private SectionTypeClassifier() {
    }

    /** 四类文档的关键词权重表（用于章节层级加权评分，替代 binary containsAny）。 */
    private static final Map<String, Map<String, Integer>> TYPE_KEYWORD_WEIGHTS = buildTypeKeywordWeights();

    private static Map<String, Map<String, Integer>> buildTypeKeywordWeights() {
        Map<String, Map<String, Integer>> map = new LinkedHashMap<>();
        // business-model
        Map<String, Integer> bm = new LinkedHashMap<>();
        bm.put("功能概述", 10); bm.put("功能模块", 10); bm.put("产品特性", 10);
        bm.put("用户故事", 8);  bm.put("User Story", 8); bm.put("业务流程", 8);
        bm.put("用户角色", 8);  bm.put("产品概述", 8);   bm.put("功能点", 8);
        bm.put("简介", 5);      bm.put("目的", 5);       bm.put("范围", 5);
        bm.put("需求描述", 6);  bm.put("需求分析", 6);   bm.put("用户场景", 6);
        bm.put("特性说明", 6);  bm.put("补充说明", 5);   bm.put("状态说明", 5);
        bm.put("目标", 5);      bm.put("总体流程", 6);   bm.put("风险分析", 5);
        bm.put("相关文档", 3);  bm.put("附件", 3);       bm.put("Feature", 6);
        map.put("business-model", bm);
        // data-model
        Map<String, Integer> dm = new LinkedHashMap<>();
        dm.put("数据模型", 10); dm.put("数据字典", 10);  dm.put("E-R", 10);
        dm.put("ER 图", 10);    dm.put("表结构", 10);    dm.put("数据结构", 8);
        dm.put("实体", 8);      dm.put("Entity", 8);     dm.put("数据库", 8);
        dm.put("Schema", 8);    dm.put("主键", 8);       dm.put("外键", 8);
        dm.put("字段", 6);      dm.put("字段定义", 8);   dm.put("核心模型", 8);
        dm.put("存储方案", 8);  dm.put("数据表", 6);
        map.put("data-model", dm);
        // interface-protocol
        Map<String, Integer> ip = new LinkedHashMap<>();
        ip.put("对外接口", 10); ip.put("通信协议", 10);  ip.put("请求参数", 10);
        ip.put("请求体", 10);   ip.put("响应体", 10);    ip.put("响应码", 8);
        ip.put("状态码", 8);    ip.put("请求方式", 8);   ip.put("方法名", 8);
        ip.put("接口", 8);      ip.put("API", 8);        ip.put("HTTP", 8);
        ip.put("REST", 8);      ip.put("/api/", 8);      ip.put("URL", 6);
        ip.put("报文", 6);      ip.put("模块间通信", 8);  ip.put("端点", 6);
        map.put("interface-protocol", ip);
        // architecture-decision
        Map<String, Integer> ad = new LinkedHashMap<>();
        ad.put("技术选型", 10); ad.put("部署架构", 10);  ad.put("架构决策", 10);
        ad.put("ADR", 10);      ad.put("非功能需求", 10);ad.put("非功能性需求", 10);
        ad.put("架构", 8);      ad.put("非功能性", 8);
        ad.put("系统设计", 8);  ad.put("高可用", 8);     ad.put("性能需求", 8);
        ad.put("安全需求", 8);  ad.put("性能指标", 8);   ad.put("监控需求", 8);
        ad.put("兼容性需求", 8);ad.put("扩展性", 8);    ad.put("容量", 6);
        ad.put("SLA", 6);       ad.put("Architecture", 8); ad.put("部署", 6);
        map.put("architecture-decision", ad);
        return Collections.unmodifiableMap(map);
    }

    /** 四类文档的二进制关键词词表（matchesDocType 用，保持旧语义）。 */
    private static final String[] BM_KEYWORDS = {
            "功能概述", "功能模块", "功能点", "功能摘要", "产品特性",
            "用户故事", "User Story", "Feature", "业务流程",
            "用户场景", "需求描述", "补充说明", "状态说明", "特性说明",
            "简介", "目的", "范围", "用户角色", "产品概述",
            "目标", "总体流程", "相关文档", "附件", "风险分析",
            "其它产品需求", "其它需求"
    };
    private static final String[] DM_KEYWORDS = {
            "实体", "Entity", "数据模型", "数据结构", "数据字典", "表结构",
            "字段", "主键", "外键", "E-R", "ER 图", "数据表", "Schema", "数据库",
            "核心模型", "存储方案"
    };
    private static final String[] IP_KEYWORDS = {
            "接口", "API", "端点", "HTTP", "REST", "/api/",
            "请求参数", "请求方式", "请求体", "响应体", "响应码", "状态码", "URL", "报文", "方法名",
            "对外接口", "通信协议", "模块间通信"
    };
    private static final String[] AD_KEYWORDS = {
            "架构", "技术选型", "系统设计", "性能需求", "性能指标", "监控需求",
            "兼容性需求", "非功能需求", "安全需求", "容量", "扩展性", "高可用", "SLA", "部署架构",
            "Architecture", "架构决策", "ADR", "部署"
    };

    /** 判断文本是否包含任意一个关键词。 */
    static boolean containsAny(String text, String... keywords) {
        for (String kw : keywords) {
            if (text != null && text.contains(kw)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 对一段文本计算四类文档的加权关键词评分，返回评分最高的类型及分数。
     *
     * @return 类型名 + 分数；所有类型均为 0 分时返回 ["business-model", 0]
     */
    static Map.Entry<String, Integer> scoreDocType(String text) {
        if (text == null || text.isBlank()) {
            return Map.entry("business-model", 0);
        }
        String best = "business-model";
        int bestScore = 0;
        // 按优先级：data-model > interface-protocol > architecture-decision > business-model
        for (String type : new String[]{"data-model", "interface-protocol", "architecture-decision", "business-model"}) {
            Map<String, Integer> weights = TYPE_KEYWORD_WEIGHTS.get(type);
            if (weights == null) continue;
            int score = 0;
            for (Map.Entry<String, Integer> kw : weights.entrySet()) {
                if (text.contains(kw.getKey())) {
                    score += kw.getValue();
                }
            }
            // 相同分数时优先级高的类型胜出（即先出现的类型）
            if (score > bestScore) {
                bestScore = score;
                best = type;
            }
        }
        return Map.entry(best, bestScore);
    }

    /**
     * 按优先级返回文本最能归属的文档类型；无任何特征时返回 null。
     * 优先级：data-model &gt; interface-protocol &gt; architecture-decision &gt; business-model。
     */
    static String bestDocType(String para) {
        for (String type : new String[]{"data-model", "interface-protocol", "architecture-decision", "business-model"}) {
            if (matchesDocType(para, type)) {
                return type;
            }
        }
        return null;
    }

    /** 文本是否命中指定类型的二进制关键词词表。 */
    static boolean matchesDocType(String para, String docType) {
        switch (docType) {
            case "business-model":
                return containsAny(para, BM_KEYWORDS);
            case "data-model":
                return containsAny(para, DM_KEYWORDS);
            case "interface-protocol":
                return containsAny(para, IP_KEYWORDS);
            case "architecture-decision":
                return containsAny(para, AD_KEYWORDS);
            default:
                return false;
        }
    }
}