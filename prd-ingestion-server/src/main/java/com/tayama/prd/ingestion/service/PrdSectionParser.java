package com.tayama.prd.ingestion.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * PRD 章节树解析器（替代平面 {@code classifyBySectionHierarchy} 的面向对象版本）。
 *
 * <p>解析步骤：</p>
 * <ol>
 *   <li>按空行分割段落，依标题层级（H1-H6 / 数字编号 / 中文序号）构建章节树；</li>
 *   <li>每个标题用 {@link SectionTypeClassifier#scoreDocType} 加权评分，评分 &lt; 5 时继承父节点类型；</li>
 *   <li>非标题正文段落到当前章节节点名下；文档开头的零散段落归入虚拟根（作为业务模型前言）。</li>
 * </ol>
 *
 * <p>相比平面 owners 数组，树保留了「章节 → 子章节 → 段落」的结构，各提取器可精确定位
 * 需求章节的正文、数据字典表格、非功能需求章节等，而不是把段落塞进 4 个桶。</p>
 */
final class PrdSectionParser {

    private static final String ROOT_TYPE = "business-model";

    private PrdSectionParser() {
    }

    /**
     * 解析段落列表为章节树，返回根级章节节点列表（不含虚拟根自身）。
     */
    static List<SectionNode> parse(List<String> paragraphs) {
        SectionNode root = new SectionNode(0, "", "", ROOT_TYPE, null);
        SectionNode current = root;
        for (String raw : paragraphs) {
            String para = raw.trim();
            if (para.isEmpty()) {
                continue;
            }
            int level = PrdIngestionService.detectHeadingLevel(para);
            if (level > 0) {
                String title = stripTitlePrefix(para);
                String type = classify(level, title, current);
                SectionNode node = new SectionNode(level, para, title, type, current);
                while (current.level >= level && current.parent != null) {
                    current = current.parent;
                }
                current.children.add(node);
                current = node;
            } else {
                // 非标题正文：归入当前章节；开头无标题的段落归虚拟根（业务模型前言）
                current.paragraphs.add(raw);
            }
        }

        // 无标题章节时：将虚拟根中的零散段落合成一个「需求概述」章节，
        // 使一句话/模糊需求也能被各提取器处理（正交算法兜底）。
        if (root.children.isEmpty() && !root.paragraphs.isEmpty()) {
            SectionNode synthetic = new SectionNode(2, "## 需求概述", "需求概述", "business-model", root);
            synthetic.paragraphs.addAll(root.paragraphs);
            root.children.add(synthetic);
            root.paragraphs.clear();
        }

        return root.children;
    }

    /**
     * 章节类型：标题加权评分；评分 &lt; 5 且存在父章节时继承父类型（父为虚拟根则兜底业务模型）。
     */
    private static String classify(int level, String title, SectionNode parent) {
        Map.Entry<String, Integer> scored = SectionTypeClassifier.scoreDocType(title);
        String type = scored.getKey();
        int score = scored.getValue();
        if (score < 5 && parent != null && !parent.isRoot()) {
            return parent.docType;
        }
        return type;
    }

    /** 去掉 Markdown #、数字/中文序号前缀，得到纯标题文本。 */
    static String stripTitlePrefix(String heading) {
        if (heading == null) return "";
        String t = heading.trim();
        while (t.startsWith("#")) {
            t = t.substring(1).trim();
        }
        // 数字编号：1.2.3 / 5.1 / 1. / 1、
        t = t.replaceFirst("^\\d+(\\.\\d+)*[.、]?\\s*", "");
        // 中文序号：一、 / （一）
        t = t.replaceFirst("^（?[一二三四五六七八九十]+）?[、]?\\s*", "");
        return t.trim();
    }

    /** 收集指定类型的章节节点（深度优先）。 */
    static List<SectionNode> nodesOfType(List<SectionNode> tree, String docType) {
        List<SectionNode> out = new ArrayList<>();
        for (SectionNode root : tree) {
            collect(root, docType, out);
        }
        return out;
    }

    private static void collect(SectionNode node, String docType, List<SectionNode> out) {
        if (docType.equals(node.docType)) {
            out.add(node);
        }
        for (SectionNode child : node.children) {
            collect(child, docType, out);
        }
    }
}