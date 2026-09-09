package com.tayama.prd.ingestion.service;

import static org.junit.jupiter.api.Assertions.*;

import com.tayama.prd.ingestion.model.prd.ArchitectureDecision;
import com.tayama.prd.ingestion.model.prd.DataEntity;
import com.tayama.prd.ingestion.model.prd.InterfaceProtocol;
import com.tayama.prd.ingestion.model.prd.RequirementEntity;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 章节树 + 4 个正交生成器的金本位测试。
 *
 * <p>两个场景：</p>
 * <ul>
 *   <li>真实 PRD 模板骨架 fixture（大量 […] 占位符）：管线必须健壮——不抛异常、
 *       不产生由占位/例句抠出的假实体（宁缺毋滥），内容为空时诚实为空而非伪造；</li>
 *   <li>内容充实的合成 PRD：业务模型 / 数据模型 / 接口协议 / 架构决策四类都必须
 *       提取出与原文一致的完整结果，且渲染块非空、候选带 proposed 标记。</li>
 * </ul>
 */
class PrdSectionPipelineTest {

    // ---------- 工具 ----------

    private static List<String> splitParagraphs(String text) {
        List<String> out = new ArrayList<>();
        for (String p : text.split("\\r\\n\\r\\n|\\n\\n")) {
            String t = p.trim();
            if (!t.isEmpty()) {
                out.add(t);
            }
        }
        return out;
    }

    private static ParseOutcome run(String text) {
        List<SectionNode> tree = PrdSectionParser.parse(splitParagraphs(text));
        assertFalse(tree.isEmpty(), "章节树不应为空");
        ParseOutcome outcome = new ParseOutcome();
        outcome.requirements.addAll(RequirementExtractor.extract(tree));
        outcome.dataEntities.addAll(DataEntityExtractor.extract(tree));
        outcome.interfaces.addAll(InterfaceExtractor.extract(tree, outcome.dataEntities));
        outcome.archDecisions.addAll(ArchDecisionExtractor.extract(tree));
        return outcome;
    }

    private static List<SectionNode> flatten(List<SectionNode> nodes) {
        List<SectionNode> out = new ArrayList<>();
        for (SectionNode n : nodes) {
            out.addAll(n.flatten());
        }
        return out;
    }

    // ---------- 场景 A：真实模板骨架 fixture（占位符多） ----------

    @Test
    void templateSkeletonIsRobustAndNeverFabricates() throws IOException {
        String text;
        try (InputStream in = PrdSectionPipelineTest.class.getResourceAsStream("/fixtures/prd-template-sample.txt")) {
            assertNotNull(in, "fixture /fixtures/prd-template-sample.txt 不存在");
            text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }

        // 结构：特性1（功能点1）应识别为 business-model 章节
        boolean feature1Found = flatten(PrdSectionParser.parse(splitParagraphs(text))).stream()
                .anyMatch(n -> n.title != null && n.title.contains("功能点1") && "business-model".equals(n.docType));
        assertTrue(feature1Found, "应识别出 business-model 章节「特性1：功能点1」");

        ParseOutcome outcome = run(text);

        // 宁缺毋滥：绝不产生由例句/占位符抠出的假实体
        for (DataEntity e : outcome.dataEntities) {
            assertTrue(DataEntityExtractor.isMeaningfulCandidate(e.getName()),
                    "实体名应通过质量门禁（非界面动词/介词拼接）: " + e.getName());
        }

        // 占位符模板内容极少：实体/接口/决策允许为空，但非空时必须规范和带候选标记
        for (InterfaceProtocol ip : outcome.interfaces) {
            assertTrue(ip.getPath().startsWith("/api/v1/") || ip.getPath().startsWith("/api/"),
                    "接口路径应规范: " + ip.getPath());
            assertTrue("proposed".equals(ip.getCandidateStatus()) || "confirmed".equals(ip.getCandidateStatus()),
                    "接口应带候选状态: " + ip.getPath());
        }
        for (ArchitectureDecision ad : outcome.archDecisions) {
            assertEquals("proposed", ad.getStatus(), "规则推导的决策必须标记 proposed");
            assertNotNull(ad.getTitle());
            assertFalse(ad.getTitle().isEmpty());
            assertNotNull(ad.getDecision());
        }
    }

    // ---------- 场景 B：内容充实的合成 PRD（四类章节齐全） ----------

    @Test
    void richPrdProducesCompleteAndCorrectFourDocs() {
        String prd = ""
                + "# 产品需求说明书\n"
                + "\n"
                + "# 产品概述\n"
                + "\n"
                + "## 总体目标\n"
                + "\n"
                + "构建校园社区的信息汇聚体系，统一发布学校新闻与系统公告给全校师生。\n"
                + "\n"
                + "# 产品特性\n"
                + "\n"
                + "## 功能模块1：校园新闻\n"
                + "\n"
                + "### 产品结构\n"
                + "\n"
                + "1. 新闻发布：统一管理学校新闻的发布与下线；\n"
                + "\n"
                + "2. 行政楼区域：展示管理层介绍与新闻中心入口。\n"
                + "\n"
                + "### 特性1：新闻查看\n"
                + "\n"
                + "__需求描述：__\n"
                + "\n"
                + "用户点击“行政楼”菜单时，展示学校的新闻中心和管理层介绍，按发布时间倒序展示。\n"
                + "\n"
                + "# 数据模型\n"
                + "\n"
                + "## 用户表\n"
                + "\n"
                + "| 字段 | 类型 | 说明 |\n"
                + "\n"
                + "| id | bigint | 主键 |\n"
                + "\n"
                + "| nickname | varchar | 昵称 |\n"
                + "\n"
                + "## 新闻表\n"
                + "\n"
                + "| 字段 | 类型 | 说明 |\n"
                + "\n"
                + "| id | bigint | 主键 |\n"
                + "\n"
                + "| title | varchar | 标题 |\n"
                + "\n"
                + "| publish_time | datetime | 发布时间 |\n"
                + "\n"
                + "# 接口协议\n"
                + "\n"
                + "## 新闻接口\n"
                + "\n"
                + "GET /api/v1/news/list 分页查询新闻列表\n"
                + "\n"
                + "POST /api/v1/news/create 发布新闻\n"
                + "\n"
                + "# 其它产品需求\n"
                + "\n"
                + "## 性能需求\n"
                + "\n"
                + "首屏加载不超过 2 秒，支持 1 万并发访问。\n"
                + "\n"
                + "## 兼容性需求\n"
                + "\n"
                + "兼容主流浏览器与移动端。\n";

        ParseOutcome outcome = run(prd);

        // --- 业务模型：特性1 的完整需求描述被转录 ---
        assertFalse(outcome.requirements.isEmpty(), "应提取出需求");
        boolean hasNewsReq = outcome.requirements.stream()
                .anyMatch(r -> r.getDescription().contains("新闻") && r.getDescription().contains("倒序"));
        assertTrue(hasNewsReq, "应提取出特性1的完整需求描述(含'倒序'), actual:\n"
                + descriptions(outcome.requirements));

        // --- 数据模型：两张表 confirmed，属性完整 ---
        assertTrue(outcome.dataEntities.size() >= 2, "应提取出至少用户表+新闻表: " + names(outcome.dataEntities));
        DataEntity userTable = outcome.dataEntities.stream()
                .filter(e -> e.getName().contains("用户")).findFirst().orElse(null);
        assertNotNull(userTable, "应为数据模型章节的表格生成「用户」实体: " + names(outcome.dataEntities));
        assertEquals("confirmed", userTable.getCandidateStatus());
        assertTrue(userTable.getAttributes().stream().anyMatch(a -> "id".equals(a.getName())),
                "用户表应含 id 属性: " + attributes(userTable));
        assertTrue(userTable.getAttributes().stream().anyMatch(a -> "nickname".equals(a.getName())),
                "用户表应含 nickname 属性");
        DataEntity newsTable = outcome.dataEntities.stream()
                .filter(e -> e.getName().contains("新闻")).findFirst().orElse(null);
        assertNotNull(newsTable, "应生成「新闻」实体");
        assertTrue(newsTable.getAttributes().stream().anyMatch(a -> "publish_time".equals(a.getName())),
                "新闻表应含 publish_time 属性");

        // --- 接口协议：接口章节的真实路径被保留（不再空模板） ---
        assertFalse(outcome.interfaces.isEmpty(), "接口章节有真实路径，不应为空");
        assertTrue(outcome.interfaces.stream().anyMatch(ip -> ip.getPath().startsWith("/api/v1/news/")),
                "应保留接口章节中的真实路径 /api/v1/news/..., actual: " + paths(outcome.interfaces));

        // --- 架构决策：性能/兼容 各推导一条候选 ADR ---
        assertFalse(outcome.archDecisions.isEmpty(), "非功能需求章节应推导候选 ADR");
        assertTrue(outcome.archDecisions.stream().anyMatch(ad -> ad.getTitle().contains("性能")),
                "应包含「性能需求」决策: " + titles(outcome.archDecisions));
        assertTrue(outcome.archDecisions.stream().anyMatch(ad -> ad.getTitle().contains("兼容")),
                "应包含「兼容性需求」决策");
        for (ArchitectureDecision ad : outcome.archDecisions) {
            assertEquals("proposed", ad.getStatus());
            assertFalse(ad.getDecision().isEmpty());
        }

        // --- 渲染：四个文档块都能渲染出内容（不再是空骨架），候选带标记 ---
        assertFalse(PrdIngestionService.buildRequirementsBlock(outcome.requirements).isBlank());
        assertFalse(PrdIngestionService.buildEntitiesBlock(outcome.dataEntities).isBlank());
        String ifaceBlock = PrdIngestionService.buildInterfacesBlock(outcome.interfaces);
        assertFalse(ifaceBlock.isBlank());
        assertTrue(ifaceBlock.contains("/api/v1/news/list"),
                "接口文档块应包含接口章节的真实路径 /api/v1/news/list, got: " + ifaceBlock);
        assertTrue(PrdIngestionService.buildDecisionsBlock(outcome.archDecisions).contains("候选"),
                "决策文档块应含候选确认提示");
    }

    // ---------- 断言辅助 ----------

    private static String descriptions(List<RequirementEntity> reqs) {
        StringBuilder sb = new StringBuilder();
        for (RequirementEntity r : reqs) {
            sb.append("- ").append(r.getDescription()).append('\n');
        }
        return sb.toString();
    }

    private static String names(List<DataEntity> entities) {
        StringBuilder sb = new StringBuilder();
        for (DataEntity e : entities) {
            sb.append(e.getName()).append(", ");
        }
        return sb.toString();
    }

    private static String attributes(DataEntity e) {
        StringBuilder sb = new StringBuilder();
        for (DataEntity.Attribute a : e.getAttributes()) {
            sb.append(a.getName()).append(":").append(a.getType()).append(", ");
        }
        return sb.toString();
    }

    private static String paths(List<InterfaceProtocol> ifaces) {
        StringBuilder sb = new StringBuilder();
        for (InterfaceProtocol ip : ifaces) {
            sb.append(ip.getMethod()).append(' ').append(ip.getPath()).append(", ");
        }
        return sb.toString();
    }

    private static String titles(List<ArchitectureDecision> ads) {
        StringBuilder sb = new StringBuilder();
        for (ArchitectureDecision ad : ads) {
            sb.append(ad.getTitle()).append(", ");
        }
        return sb.toString();
    }
}