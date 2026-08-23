package com.huazai.prd.ingestion.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.*;

/**
 * 快速验证 classifyBySectionHierarchy 方法是否能正确识别 PRD 中的章节。
 */
class ClassifyBySectionHierarchyTest {

    /** 模拟用户 PRD 的章节标题（按实际段落分割后的样例） */
    static final String[] PRD_HEADINGS = {
        "# PRD 版本迭代记录",
        "## 1. 项目背景与目标",
        "## 2. 目标用户",
        "### 2.1 大学生用户",
        "### 2.2 游客/访客",
        "## 3. 业务需求概述",
        "## 4. 功能需求",
        "### 4.1 用户管理",
        "## 5. 数据模型",
        "### 5.1 用户",
        "### 5.2 题目",
        "### 5.3 答题记录",
        "## 6. 接口设计",
        "### 6.1 用户接口",
        "### 6.2 题目接口",
        "### 6.3 答题接口",
        "## 7. 非功能性需求",
    };

    @Test
    void testClassifyBySectionHierarchy() {
        List<String> paragraphs = new ArrayList<>();
        for (String h : PRD_HEADINGS) {
            paragraphs.add(h);
            paragraphs.add("这是标题 \"" + h + "\" 下的正文内容段落。");
        }

        String[] owners = PrdIngestionService.classifyBySectionHierarchy(paragraphs);

        // 统计每个类型的段落数
        Map<String, Long> counts = Arrays.stream(owners)
            .collect(Collectors.groupingBy(o -> o, Collectors.counting()));

        System.out.println("=== 分类结果 ===");
        counts.forEach((type, cnt) -> System.out.println("  " + type + ": " + cnt));
        System.out.println("=== 每个标题的归属 ===");
        for (int i = 0; i < paragraphs.size(); i++) {
            System.out.println("  [" + owners[i] + "] " + paragraphs.get(i));
        }

        assertTrue(counts.getOrDefault("data-model", 0L) >= 2,
            "应该至少找到 2 个 data-model 段落（标题+正文），实际: " + counts.getOrDefault("data-model", 0L));
        assertTrue(counts.getOrDefault("interface-protocol", 0L) >= 2,
            "应该至少找到 2 个 interface-protocol 段落，实际: " + counts.getOrDefault("interface-protocol", 0L));
        assertTrue(counts.getOrDefault("business-model", 0L) >= 2,
            "应该至少找到 2 个 business-model 段落，实际: " + counts.getOrDefault("business-model", 0L));
    }

    @Test
    void testDetectHeadingLevel() {
        assertEquals(1, PrdIngestionService.detectHeadingLevel("# 简介"));
        assertEquals(2, PrdIngestionService.detectHeadingLevel("## 目的"));
        assertEquals(3, PrdIngestionService.detectHeadingLevel("### 产品概述"));
        assertEquals(4, PrdIngestionService.detectHeadingLevel("#### 特性1"));
        assertEquals(1, PrdIngestionService.detectHeadingLevel("1. 项目背景"));
        assertEquals(2, PrdIngestionService.detectHeadingLevel("## 1. 项目背景"));
        assertEquals(3, PrdIngestionService.detectHeadingLevel("### 2.1 大学生用户"));
        assertEquals(0, PrdIngestionService.detectHeadingLevel("普通段落"));
        assertEquals(2, PrdIngestionService.detectHeadingLevel("5.1 用户"));
    }

    @Test
    void testDetectHeadingLevelForNumberedHeads() {
        assertEquals(1, PrdIngestionService.detectHeadingLevel("5. 数据模型"));
        assertEquals(2, PrdIngestionService.detectHeadingLevel("## 5. 数据模型"));
        assertEquals(2, PrdIngestionService.detectHeadingLevel("5.1 用户"));
        assertEquals(3, PrdIngestionService.detectHeadingLevel("5.1.2 子功能"));
    }
}