package com.tayama.prd.ingestion.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 回归测试：change.md 生成内容必须保持合法 markdown。
 * PRD 原始段落中的表格 / 标题 / 代码围栏必须从列表项中剥离为独立块，
 * 保证 Typora / GitHub 均可正常解析；普通行、子列表保留为列表项嵌套内容。
 */
class ChangeServiceTest {

    @Test
    void appendItem_表格从列表中剥离为独立块() {
        StringBuilder sb = new StringBuilder();
        ChangeService.appendItem(sb, "REQ-1", "痛点分析\n| 痛点 | 描述 |\n|------|------|\n| A | B |", "（优先级 P0）", "");
        String out = sb.toString();
        assertTrue(out.contains("- [REQ-1] 痛点分析（优先级 P0）\n"), out);
        // 表格独立成块（前面有空行，不再属于列表项文本）
        assertTrue(out.contains("（优先级 P0）\n\n| 痛点 | 描述 |\n|------|------|\n| A | B |\n"), out);
    }

    @Test
    void appendItem_子列表保留为列表项嵌套内容() {
        StringBuilder sb = new StringBuilder();
        ChangeService.appendItem(sb, "REQ-2", "行业趋势\n- AI 辅助\n- 企业级", "", "");
        String out = sb.toString();
        assertTrue(out.contains("- [REQ-2] 行业趋势\n  - AI 辅助\n  - 企业级\n"), out);
    }

    @Test
    void appendItem_代码围栏独立成块且保持闭合() {
        StringBuilder sb = new StringBuilder();
        ChangeService.appendItem(sb, "REQ-3", "示例\n```\nPRD 内容\n```", "", "");
        String out = sb.toString();
        assertTrue(out.contains("```\nPRD 内容\n```"), out);
        assertTrue(out.contains("- [REQ-3] 示例\n"), out);
    }

    @Test
    void appendItem_半开围栏自动补闭合() {
        StringBuilder sb = new StringBuilder();
        ChangeService.appendItem(sb, "REQ-4", "示例\n```\nPRD 内容\n", "", "");
        String out = sb.toString();
        // 未闭合围栏自动补 ```，后续内容不会被吞掉
        assertTrue(out.contains("```\nPRD 内容\n```\n"), out);
    }

    @Test
    void appendItem_内容全为表格时取摘要行() {
        StringBuilder sb = new StringBuilder();
        ChangeService.appendItem(sb, "REQ-5", "| 痛点 | 描述 |\n|---|---|\n| A | B |", "", "");
        String out = sb.toString();
        assertTrue(out.contains("- [REQ-5] "), out);
        // 表格仍完整独立输出
        assertTrue(out.contains("\n| 痛点 | 描述 |\n|---|---|\n| A | B |\n"), out);
    }

    @Test
    void appendItem_null与空文本安全() {
        StringBuilder sb = new StringBuilder();
        ChangeService.appendItem(sb, "REQ-6", null, "", "");
        assertEquals("- [REQ-6] （详见下方内容）\n", sb.toString());
        StringBuilder sb2 = new StringBuilder();
        ChangeService.appendItem(sb2, null, null, "", "");
        assertTrue(sb2.toString().startsWith("- "));
    }

    @Test
    void appendItem_字面反斜杠n归一化为真换行() {
        // PRD 解析数据中段落换行可能存为字面 "\\n"（反斜杠 n 两个字符）
        StringBuilder sb = new StringBuilder();
        ChangeService.appendItem(sb, "REQ-7", "痛点分析\\n| 痛点 | 描述 |\\n|------|------|\\n| A | B |", "", "");
        String out = sb.toString();
        assertTrue(out.contains("- [REQ-7] 痛点分析\n"), out);
        // 表格仍被剥离为独立块
        assertTrue(out.contains("\n| 痛点 | 描述 |\n|------|------|\n| A | B |\n"), out);
        // 不残留字面 \\n
        assertTrue(!out.contains("\\n"), out);
    }
}