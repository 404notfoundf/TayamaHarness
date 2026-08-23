package com.huazai.prd.ingestion.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 用实际 PRD 模板内容测试 bestDocType 分类是否正确。
 * 验证标准 PRD 模板中的章节标题能正确分配到 4 个文档类型。
 */
class PrdSplitClassificationTest {

    /** 标准 PRD 模板的章节标题（从测试文件提取） */
    static final String[] PRD_HEADINGS = {
            "# 简介",
            "## 目的",
            "## 范围",
            "# 用户角色描述",
            "## 大学生用户",
            "## 游客",
            "## 管理员",
            "# 产品概述",
            "## 目标",
            "## 总体流程",
            "## 功能摘要",
            "# 产品特性",
            "## 第一部分  功能模块1",
            "### 产品概述",
            "### 产品结构（功能摘要）",
            "### 状态说明",
            "### 特性说明",
            "#### 特性1：功能点1",
            "#### 特性2：功能点2",
            "## 第二部分  功能模块2",
            "### 产品概述",
            "### 产品结构（功能摘要）",
            "### 状态说明",
            "### 特性说明",
            "# 其它产品需求",
            "## 性能需求",
            "## 监控需求",
            "## 兼容性需求",
            "## 风险分析",
            "## 相关文档",
            "## 附件",
    };

    /** 预期每个标题应该归属的文档类型。
     *  null = 纯关键词无匹配，但在 collectOriginalParagraphs 中会被阶段一章节归属机制继承父标题类型 */
    static final String[] EXPECTED_TYPES = {
            "business-model",      // # 简介
            "business-model",      // ## 目的
            "business-model",      // ## 范围
            "business-model",      // # 用户角色描述
            null,                  // ## 大学生用户（无关键词，会被父标题"用户角色描述"的阶段一归属继承）
            null,                  // ## 游客
            null,                  // ## 管理员
            "business-model",      // # 产品概述
            "business-model",      // ## 目标
            "business-model",      // ## 总体流程
            "business-model",      // ## 功能摘要
            "business-model",      // # 产品特性
            "business-model",      // ## 第一部分  功能模块1
            "business-model",      // ### 产品概述
            "business-model",      // ### 产品结构（功能摘要）
            "business-model",      // ### 状态说明
            "business-model",      // ### 特性说明
            "business-model",      // #### 特性1：功能点1
            "business-model",      // #### 特性2：功能点2
            "business-model",      // ## 第二部分  功能模块2
            "business-model",      // ### 产品概述
            "business-model",      // ### 产品结构（功能摘要）
            "business-model",      // ### 状态说明
            "business-model",      // ### 特性说明
            "business-model",      // # 其它产品需求
            "architecture-decision", // ## 性能需求
            "architecture-decision", // ## 监控需求
            "architecture-decision", // ## 兼容性需求
            "business-model",      // ## 风险分析
            "business-model",      // ## 相关文档
            "business-model",      // ## 附件
    };

    @Test
    void testAllHeadingsClassified() {
        assertEquals(PRD_HEADINGS.length, EXPECTED_TYPES.length,
                "headings and expected types arrays must have same length");

        List<String> failures = new ArrayList<>();
        for (int i = 0; i < PRD_HEADINGS.length; i++) {
            String heading = PRD_HEADINGS[i];
            String expected = EXPECTED_TYPES[i];
            String actual = PrdIngestionService.bestDocType(heading);
            if (!Objects.equals(expected, actual)) {
                failures.add(String.format("heading=[%s] expected=%s actual=%s",
                        heading, expected == null ? "null" : expected,
                        actual == null ? "null" : actual));
            }
        }

        if (!failures.isEmpty()) {
            fail("分类失败 " + failures.size() + " 处:\n" +
                    failures.stream().collect(Collectors.joining("\n")));
        }
    }

    @Test
    void testBusinessModelKeywords() {
        // 验证业务模型能匹配到的关键词
        assertTrue(PrdIngestionService.matchesDocType("简介", "business-model"));
        assertTrue(PrdIngestionService.matchesDocType("目的", "business-model"));
        assertTrue(PrdIngestionService.matchesDocType("范围", "business-model"));
        assertTrue(PrdIngestionService.matchesDocType("用户角色描述", "business-model"));
        assertTrue(PrdIngestionService.matchesDocType("产品概述", "business-model"));
        assertTrue(PrdIngestionService.matchesDocType("目标", "business-model"));
        assertTrue(PrdIngestionService.matchesDocType("总体流程", "business-model"));
        assertTrue(PrdIngestionService.matchesDocType("功能摘要", "business-model"));
        assertTrue(PrdIngestionService.matchesDocType("功能模块", "business-model"));
        assertTrue(PrdIngestionService.matchesDocType("产品特性", "business-model"));
        assertTrue(PrdIngestionService.matchesDocType("状态说明", "business-model"));
        assertTrue(PrdIngestionService.matchesDocType("特性说明", "business-model"));
        assertTrue(PrdIngestionService.matchesDocType("功能点", "business-model"));
        assertTrue(PrdIngestionService.matchesDocType("其它产品需求", "business-model"));
        assertTrue(PrdIngestionService.matchesDocType("风险分析", "business-model"));
        assertTrue(PrdIngestionService.matchesDocType("相关文档", "business-model"));
        assertTrue(PrdIngestionService.matchesDocType("附件", "business-model"));
    }

    @Test
    void testArchitectureDecisionKeywords() {
        assertTrue(PrdIngestionService.matchesDocType("性能需求", "architecture-decision"));
        assertTrue(PrdIngestionService.matchesDocType("监控需求", "architecture-decision"));
        assertTrue(PrdIngestionService.matchesDocType("兼容性需求", "architecture-decision"));
        assertTrue(PrdIngestionService.matchesDocType("架构决策", "architecture-decision"));
        assertTrue(PrdIngestionService.matchesDocType("ADR", "architecture-decision"));
    }

    @Test
    void testSpecificity() {
        // 验证"性能需求"正确匹配 architecture-decision 而非 business-model
        String type = PrdIngestionService.bestDocType("## 性能需求");
        assertEquals("architecture-decision", type,
                "性能需求应该归入 architecture-decision 而非 " + type);

        // 验证"功能模块"正确匹配 business-model
        type = PrdIngestionService.bestDocType("## 功能模块1");
        assertEquals("business-model", type,
                "功能模块应该归入 business-model 而非 " + type);
    }

    @Test
    void testIsHeading() {
        assertTrue(PrdIngestionService.isHeading("# 简介"));
        assertTrue(PrdIngestionService.isHeading("## 目的"));
        assertTrue(PrdIngestionService.isHeading("### 产品概述"));
        assertTrue(PrdIngestionService.isHeading("#### 特性1：功能点1"));
        assertFalse(PrdIngestionService.isHeading("普通段落文本内容"));
        assertFalse(PrdIngestionService.isHeading(""));
        assertFalse(PrdIngestionService.isHeading(" "));
    }
}