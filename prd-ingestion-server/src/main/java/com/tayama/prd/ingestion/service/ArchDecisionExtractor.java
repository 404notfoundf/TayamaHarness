package com.tayama.prd.ingestion.service;

import com.tayama.prd.ingestion.model.prd.ArchitectureDecision;
import com.tayama.prd.ingestion.model.prd.CandidateStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 架构决策生成器。
 *
 * <p>PRD 通常不含架构设计，但「性能需求 / 监控需求 / 兼容性需求」等非功能需求章节存在：
 * 每个非功能需求章节推导为一条候选 ADR（status=proposed，title=章节名，
 * decision=按章节类型的通用方案建议，需架构师评审确认），使架构决策文档始终有依据可循，
 * 而不是渲染出空的 ADR 骨架。</p>
 */
final class ArchDecisionExtractor {

    /** 非功能需求章节标题 → 推导候选 ADR 并给出推荐决策文案。 */
    private static final Set<String> NON_FUNCTIONAL_TITLES = new LinkedHashSet<>(Arrays.asList(
            "性能需求", "性能指标", "监控需求", "兼容性需求", "安全需求",
            "非功能需求", "非功能性需求", "容量", "扩展性", "高可用", "SLA"));

    private ArchDecisionExtractor() {
    }

    static List<ArchitectureDecision> extract(List<SectionNode> tree) {
        List<ArchitectureDecision> out = new ArrayList<>();
        for (SectionNode node : PrdSectionParser.nodesOfType(tree, "architecture-decision")) {
            if (node.isRoot()) {
                continue;
            }
            String body = aggregateBody(node);
            ArchitectureDecision d = new ArchitectureDecision();
            d.setId("ADR-" + (out.size() + 1));
            d.setTitle(node.title);
            d.setSourceParagraph(node.rawHeading);
            if (isNonFunctional(node.title)) {
                d.setContext(body.isEmpty()
                        ? "PRD 提出「" + node.title + "」相关要求，具体指标待业务确认"
                        : body);
                d.setDecision(decisionFor(node.title));
                d.setConsequences(new ArrayList<>(Arrays.asList(
                        "候选决策：由架构师评审并补充具体技术方案",
                        "决策确认后需在数据模型与接口协议中落地")));
            } else {
                d.setContext(body.isEmpty()
                        ? "PRD 在「" + node.title + "」章节给出架构相关描述（内容待补充）"
                        : body);
                d.setDecision("原文引用以上内容，具体技术方案待架构评审");
                d.setConsequences(new ArrayList<>());
            }
            d.setStatus(CandidateStatus.PROPOSED);
            out.add(d);
        }
        // 兜底：无架构决策章节但有业务内容时，生成一条候选 ADR，
        // 使一句话/模糊需求也有架构决策文档可展示，与有 AI 场景行为一致。
        if (out.isEmpty()) {
            for (SectionNode node : PrdSectionParser.nodesOfType(tree, "business-model")) {
                if (node.isRoot() || node.paragraphs.isEmpty()) {
                    continue;
                }
                String body = aggregateBody(node);
                if (body.length() < 8) {
                    continue;
                }
                ArchitectureDecision d = new ArchitectureDecision();
                d.setId("ADR-1");
                d.setTitle("系统架构设计");
                d.setContext("PRD 提出「" + truncateTitle(body) + "」相关需求，需进行系统架构设计以满足业务目标");
                d.setDecision("以满足业务需求为目标进行架构设计，建议采用分层架构，确保系统可扩展、可维护");
                d.setConsequences(new ArrayList<>(Arrays.asList(
                        "候选决策：由架构师评审并补充具体技术方案",
                        "决策确认后需在数据模型与接口协议中落地")));
                d.setSourceParagraph(node.rawHeading);
                d.setStatus(CandidateStatus.PROPOSED);
                out.add(d);
                break;
            }
        }
        return out;
    }

    private static boolean isNonFunctional(String title) {
        for (String kw : NON_FUNCTIONAL_TITLES) {
            if (title.contains(kw)) {
                return true;
            }
        }
        return false;
    }

    private static String decisionFor(String title) {
        if (title.contains("性能")) {
            return "通过水平扩展、缓存与异步化等手段满足响应时间与吞吐量要求，具体指标需基准测试验证";
        }
        if (title.contains("监控")) {
            return "规划日志采集、指标监控与告警体系（如 Prometheus + Grafana），关键链路配置埋点与告警";
        }
        if (title.contains("兼容")) {
            return "制定浏览器/设备兼容矩阵，前端按渐进增强策略实现，关键功能提供降级方案";
        }
        if (title.contains("安全")) {
            return "遵循最小权限原则，敏感数据加密存储与传输，接入统一认证与鉴权体系";
        }
        if (title.contains("容量") || title.contains("扩展") || title.contains("高可用")) {
            return "按可水平扩展设计无状态服务，存储层预留分片/分区能力，关键组件高可用部署";
        }
        return "以满足 PRD「" + title + "」章节要求为目标进行技术方案设计";
    }

    private static String aggregateBody(SectionNode node) {
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
        return joined.length() > 300 ? joined.substring(0, 300) + "…" : joined;
    }

    /** 截取正文前 20 字作为 ADR 标题引用。 */
    private static String truncateTitle(String text) {
        if (text == null) return "";
        String t = text.replaceAll("\\s+", "").trim();
        return t.length() > 20 ? t.substring(0, 20) + "…" : t;
    }
}