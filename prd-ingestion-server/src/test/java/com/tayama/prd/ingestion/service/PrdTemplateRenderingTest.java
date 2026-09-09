package com.tayama.prd.ingestion.service;

import com.tayama.prd.ingestion.model.prd.ArchitectureDecision;
import com.tayama.prd.ingestion.model.prd.DataEntity;
import com.tayama.prd.ingestion.model.prd.InterfaceProtocol;
import com.tayama.prd.ingestion.model.prd.RequirementEntity;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 模板驱动渲染单测：拆分文档必须按「模板管理」中配置的模板（数据库读取，
 * 见 TemplateRepository#findContentByType）渲染，且支持占位符与章节名语义匹配两种填充方式。
 */
class PrdTemplateRenderingTest {

    // ==================== 占位符渲染 ====================

    @Test
    void 模板含占位符时直接替换数据块_保留模板骨架() {
        String template = "# 业务模型\n\n> 引言\n\n## 业务全景\n{{REQUIREMENTS}}\n\n## 领域边界\n<占位>\n";
        String out = PrdIngestionService.renderDocumentFromTemplate(
                "business-model", "腾讯云消息队列", template, requirements(), null, null, null);
        // 标题统一为 baseTitle - 类型名
        assertTrue(out.startsWith("# 腾讯云消息队列 - 业务模型\n"));
        // 占位符被替换为需求数据，且业务全景章节保留了模板骨架
        assertTrue(out.contains("### REQ-001 [P0]"));
        assertTrue(out.contains("支持消息的生产与消费"));
        // 非数据章节保留模板骨架标题，但占位文字被清理（stripTemplatePlaceholders）
        assertTrue(out.contains("## 领域边界"));
        assertFalse(out.contains("<占位>"));
        // 占位符本身不再残留
        assertFalse(out.contains("{{REQUIREMENTS}}"));
    }

    @Test
    void 占位符模板的标题占位符TITLE也支持() {
        String template = "# {{TITLE}}\n\n## 需求列表\n{{REQUIREMENTS}}\n";
        String out = PrdIngestionService.renderDocumentFromTemplate(
                "business-model", "腾讯云消息队列", template, requirements(), null, null, null);
        assertTrue(out.startsWith("# 腾讯云消息队列 - 业务模型\n"));
        assertFalse(out.contains("{{TITLE}}"));
    }

    // ==================== 章节名语义匹配（数据库种子模板无占位符） ====================

    @Test
    void dataModel_模板核心模型章节填充实体数据_存储方案保留原文() {
        // 与 schema.sql 中 data-model 种子模板同构的章节结构
        String template = "# 数据模型\n\n> 引言\n\n## 核心模型\n| 模型 | 说明 | 存储 |\n|------|------|------|\n| <模型名> | <描述> | <MySQL/Redis/…> |\n\n## 存储方案\n<描述整体存储架构>\n";
        String out = PrdIngestionService.renderDocumentFromTemplate(
                "data-model", "腾讯云消息队列", template, null, entities(), null, null);
        // 标题统一
        assertTrue(out.startsWith("# 腾讯云消息队列 - 数据模型\n"));
        // 章节「核心模型」被实体数据替换（模板表格占位不再残留）
        assertFalse(out.contains("<模型名>"));
        assertTrue(out.contains("### 消息队列"));
        assertTrue(out.contains("topic"));
        // 非数据章节「存储方案」保留章节标题，但占位文字被清理
        assertTrue(out.contains("## 存储方案"));
        assertFalse(out.contains("<描述整体存储架构>"));
    }

    @Test
    void interfaceProtocol_对外接口章节填充接口数据() {
        String template = "# 接口协议\n\n## 对外接口\n| 端点 | 方法 | 说明 |\n|<路径> | <GET> | <描述> |\n\n## 模块间通信协议\n<描述通信方式>\n";
        String out = PrdIngestionService.renderDocumentFromTemplate(
                "interface-protocol", "腾讯云消息队列", template, null, null, interfaces(), null);
        assertTrue(out.contains("### POST /api/v1/messages"));
        // 对外接口章节的表格占位被替换
        assertFalse(out.contains("<路径>"));
        // 模块间通信协议保留章节标题，占位文字被清理
        assertTrue(out.contains("## 模块间通信协议"));
        assertFalse(out.contains("<描述通信方式>"));
    }

    @Test
    void architectureDecision_ADR列表章节填充决策数据() {
        String template = "# 架构决策\n\n## ADR 列表\n<决策列表>\n\n## ADR-001: 模板化渲染\n<背景>\n";
        List<ArchitectureDecision> decisions = Arrays.asList(decision("AD-1", "使用模板驱动拆分", "accepted"));
        String out = PrdIngestionService.renderDocumentFromTemplate(
                "architecture-decision", "腾讯云消息队列", template, null, null, null, decisions);
        assertTrue(out.contains("### AD-1: 使用模板驱动拆分"));
        assertTrue(out.contains("**状态**: accepted"));
    }

    @Test
    void 章节名未命中任何数据章节时_数据块追加到末尾不丢失() {
        // 模板只有说明性章节（如旧版业务模型种子模板），数据必须追加在末尾
        String template = "# 业务模型\n\n## 业务全景\n<描述>\n\n## 业务规则\n- 规则 1\n";
        String out = PrdIngestionService.renderDocumentFromTemplate(
                "business-model", "腾讯云消息队列", template, requirements(), null, null, null);
        assertTrue(out.contains("## 业务全景"));
        assertFalse(out.contains("<描述>"));
        assertTrue(out.contains("## 业务规则"));
        assertTrue(out.contains("- 规则 1"));
        assertTrue(out.contains("## 需求列表"));
        assertTrue(out.contains("### REQ-001 [P0]"));
        // 追加的数据位于模板之后
        assertTrue(out.indexOf("### REQ-001 [P0]") > out.indexOf("## 业务规则"));
    }

    // ==================== 无模板 / 异常 → 回退硬编码 ====================

    @Test
    void 无模板时回退硬编码生成_四类文档骨架正确() {
        String bm = PrdIngestionService.renderDocumentFromTemplate(
                "business-model", "P", null, requirements(), null, null, null);
        assertTrue(bm.contains("# P - 业务模型"));
        assertTrue(bm.contains("## 需求列表"));
        assertTrue(bm.contains("### REQ-001 [P0]"));

        String dm = PrdIngestionService.renderDocumentFromTemplate(
                "data-model", "P", null, null, entities(), null, null);
        assertTrue(dm.contains("# P - 数据模型"));
        assertTrue(dm.contains("### 消息队列"));

        String ip = PrdIngestionService.renderDocumentFromTemplate(
                "interface-protocol", "P", null, null, null, interfaces(), null);
        assertTrue(ip.contains("# P - 接口协议"));
        assertTrue(ip.contains("### POST /api/v1/messages"));

        String ad = PrdIngestionService.renderDocumentFromTemplate(
                "architecture-decision", "P", null, null, null, null,
                Arrays.asList(decision("AD-1", "决策标题", "accepted")));
        assertTrue(ad.contains("# P - 架构决策"));
        assertTrue(ad.contains("### AD-1: 决策标题"));
    }

    // ==================== 标题统一 / 章节语义识别 ====================

    @Test
    void replaceTemplateTitle_统一为baseTitle加类型名() {
        String out = PrdIngestionService.replaceTemplateTitle("# 业务模型\n\n正文", "腾讯云消息队列", "business-model");
        assertTrue(out.startsWith("# 腾讯云消息队列 - 业务模型\n\n正文"));
        // 模板无 H1 时在开头补一行
        String out2 = PrdIngestionService.replaceTemplateTitle("## 业务全景\n正文", "P", "data-model");
        assertTrue(out2.startsWith("# P - 数据模型\n\n## 业务全景\n正文"));
    }

    @Test
    void matchesSection_强信号词命中_弱词不误伤() {
        // 数据模型：核心模型 → entities 命中
        assertTrue(PrdIngestionService.matchesSection("entities", "data-model", "核心模型"));
        // 业务模型的「核心实体关系」章节不应误填 data-model 实体数据（docType 不符）
        assertFalse(PrdIngestionService.matchesSection("entities", "business-model", "核心实体关系"));
        // 接口协议：对外接口 → interfaces 命中；但关键词不与数据模型混淆
        assertTrue(PrdIngestionService.matchesSection("interfaces", "interface-protocol", "对外接口"));
        assertFalse(PrdIngestionService.matchesSection("interfaces", "data-model", "对外接口"));
        // 架构决策：ADR 列表 → decisions 命中；非架构文档不误命中
        assertTrue(PrdIngestionService.matchesSection("decisions", "architecture-decision", "ADR 列表"));
        assertFalse(PrdIngestionService.matchesSection("decisions", "business-model", "ADR 列表"));
        // 需求章节识别
        assertTrue(PrdIngestionService.matchesSection("requirements", "business-model", "需求列表"));
        // 弱信号：模板导语「大致响应时间」不命中接口协议
        assertFalse(PrdIngestionService.matchesSection("interfaces", "interface-protocol", "大致响应时间"));
    }

    // ==================== 测试数据构造 ====================

    private static List<RequirementEntity> requirements() {
        RequirementEntity r = new RequirementEntity();
        r.setId("REQ-001");
        r.setPriority("P0");
        r.setDescription("支持消息的生产与消费");
        r.setSourceParagraph("用户需要可靠的消息传递能力");
        return Arrays.asList(r);
    }

    private static List<DataEntity> entities() {
        DataEntity e = new DataEntity();
        e.setName("消息队列");
        e.setDescription("消息中间件核心实体");
        DataEntity.Attribute attr = new DataEntity.Attribute();
        attr.setName("topic");
        attr.setType("String");
        attr.setDescription("消息主题");
        e.setAttributes(Arrays.asList(attr));
        DataEntity.Relation rel = new DataEntity.Relation();
        rel.setTarget("消费者");
        rel.setType("OneToMany");
        rel.setDescription("一个队列被多个消费者订阅");
        e.setRelations(Arrays.asList(rel));
        return Arrays.asList(e);
    }

    private static List<InterfaceProtocol> interfaces() {
        InterfaceProtocol i = new InterfaceProtocol();
        i.setMethod("POST");
        i.setPath("/api/v1/messages");
        i.setSummary("发送消息");
        i.setRequestBody("{\"topic\":\"order\"}");
        i.setResponseBody("{\"messageId\":\"m-001\"}");
        return Arrays.asList(i);
    }

    private static ArchitectureDecision decision(String id, String title, String status) {
        ArchitectureDecision d = new ArchitectureDecision();
        d.setId(id);
        d.setTitle(title);
        d.setStatus(status);
        d.setContext("需要统一的文档生成方式");
        d.setDecision("采用数据库模板驱动的占位符+章节名双支持渲染");
        d.setConsequences(Arrays.asList("模板管理即时生效", "旧数据回退硬编码"));
        return Arrays.asList(d).get(0);
    }
}