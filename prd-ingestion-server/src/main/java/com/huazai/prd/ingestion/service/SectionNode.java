package com.huazai.prd.ingestion.service;

import java.util.ArrayList;
import java.util.List;

/**
 * PRD 章节树的节点。
 *
 * <p>每个节点对应一个标题（H1-H6）及其直属正文段落；子章节挂在 {@link #children}。
 * 虚拟根节点 level=0（标题为空、docsType=BRUSINESS_MODEL），用于容纳文档开头的零散段落。</p>
 */
final class SectionNode {

    /** 章节层级：虚拟根为 0，H1=1 … H6=6。 */
    final int level;
    /** 原始标题行（含 # / 序号前缀），非标题节点为空。 */
    final String rawHeading;
    /** 去前缀后的标题文本；虚拟根为空串。 */
    final String title;
    /** 该章节归属的文档类型（标题评分 + 父级继承后确定）。 */
    final String docType;
    final SectionNode parent;
    /** 该节点直属正文段落（不含子章节标题及其内容）。 */
    final List<String> paragraphs = new ArrayList<>();
    final List<SectionNode> children = new ArrayList<>();

    SectionNode(int level, String rawHeading, String title, String docType, SectionNode parent) {
        this.level = level;
        this.rawHeading = rawHeading;
        this.title = title;
        this.docType = docType;
        this.parent = parent;
    }

    boolean isRoot() {
        return parent == null;
    }

    /** 深度优先遍历该节点及其全部后代。 */
    List<SectionNode> flatten() {
        List<SectionNode> out = new ArrayList<>();
        flattenInto(out);
        return out;
    }

    private void flattenInto(List<SectionNode> out) {
        out.add(this);
        for (SectionNode child : children) {
            child.flattenInto(out);
        }
    }

    @Override
    public String toString() {
        return "SectionNode{" + rawHeading + " -> " + docType + ", paras=" + paragraphs.size() + ", children=" + children.size() + "}";
    }
}