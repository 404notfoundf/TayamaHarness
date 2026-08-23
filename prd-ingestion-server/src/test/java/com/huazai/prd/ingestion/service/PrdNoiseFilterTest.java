package com.huazai.prd.ingestion.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * isNoiseParagraph 噪声过滤单元测试。
 * 覆盖 LLM 失败走关键词回退时，封面元信息 / 水印 / 版本号 / 模板填空说明等垃圾段落的过滤。
 */
class PrdNoiseFilterTest {

    @Test
    void 封面双下划线行被过滤() {
        assertTrue(PrdIngestionService.isNoiseParagraph("__产品需求说明书__"));
        assertTrue(PrdIngestionService.isNoiseParagraph("__目 录__"));
        assertTrue(PrdIngestionService.isNoiseParagraph("__修订记录：__"));
        assertTrue(PrdIngestionService.isNoiseParagraph("__内部资料 注意保密__"));
        assertTrue(PrdIngestionService.isNoiseParagraph("__腾讯科技 版权所有__"));
    }

    @Test
    void 元信息标签行被过滤() {
        assertTrue(PrdIngestionService.isNoiseParagraph("文档版本号："));
        assertTrue(PrdIngestionService.isNoiseParagraph("文档编号：PRD-2024-001"));
        assertTrue(PrdIngestionService.isNoiseParagraph("文档密级：合适"));
        assertTrue(PrdIngestionService.isNoiseParagraph("归属部门/项目："));
        assertTrue(PrdIngestionService.isNoiseParagraph("产品名：tencent prd"));
        assertTrue(PrdIngestionService.isNoiseParagraph("编写人：张三"));
        assertTrue(PrdIngestionService.isNoiseParagraph("编写日期：2024-01-01"));
        assertTrue(PrdIngestionService.isNoiseParagraph("生效日期："));
        assertTrue(PrdIngestionService.isNoiseParagraph("修订人："));
        assertTrue(PrdIngestionService.isNoiseParagraph("修订日期："));
        assertTrue(PrdIngestionService.isNoiseParagraph("修订描述："));
    }

    @Test
    void 版本号与水印行被过滤() {
        assertTrue(PrdIngestionService.isNoiseParagraph("V 1.0"));
        assertTrue(PrdIngestionService.isNoiseParagraph("v1.0.0"));
        assertTrue(PrdIngestionService.isNoiseParagraph("人人都是产品经理"));
        assertTrue(PrdIngestionService.isNoiseParagraph("[www.woshipm.com](www.woshipm.com) 人人都是产品经理"));
        assertTrue(PrdIngestionService.isNoiseParagraph("版权所有 © 腾讯科技"));
    }

    @Test
    void PRD模板填空说明句被过滤() {
        assertTrue(PrdIngestionService.isNoiseParagraph(
                "[此节高度概括产品的功能与介绍]"));
        assertTrue(PrdIngestionService.isNoiseParagraph(
                "[描述产品的目标]"));
        assertTrue(PrdIngestionService.isNoiseParagraph(
                "[描述产品的总体流程图]"));
        assertTrue(PrdIngestionService.isNoiseParagraph(
                "[简要描述产品的功能点和每个功能点的优先级，参考格式如下]"));
        assertTrue(PrdIngestionService.isNoiseParagraph(
                "[阐明此产品需求说明书文档的目的，如：确定产品目标等。]"));
        assertTrue(PrdIngestionService.isNoiseParagraph(
                "[如果产品对性能要特殊需求，请详细描述，如：大致响应时间、最大并发数等。]"));
        assertTrue(PrdIngestionService.isNoiseParagraph(
                "[列出产品的特性。特性是为让用户获益而必须具备的高级系统功能。]"));
        assertTrue(PrdIngestionService.isNoiseParagraph(
                "此节为设计的系统功能性需求，一般以用例结合自然语言来表达。"));
        assertTrue(PrdIngestionService.isNoiseParagraph(
                "这一节应包含所有的产品需求，其详细程度应使架构设计人员能够设计出满足需求的系统。"));
        assertTrue(PrdIngestionService.isNoiseParagraph(
                "__流程说明：__（用例图、流程图）"));
        assertTrue(PrdIngestionService.isNoiseParagraph(
                "1. 播放区：播放区定义及功能说明；"));
        assertTrue(PrdIngestionService.isNoiseParagraph(
                "1. 状态1：状态1定义及可执行操作说明；"));
        assertTrue(PrdIngestionService.isNoiseParagraph(
                "[嵌入对象图片：无法在浏览器中渲染，请查阅原文档]"));
    }

    @Test
    void docx表格表头与单元格碎片被过滤() {
        assertTrue(PrdIngestionService.isNoiseParagraph("用户角色"));
        assertTrue(PrdIngestionService.isNoiseParagraph("用户描述"));
        assertTrue(PrdIngestionService.isNoiseParagraph("功能模块"));
        assertTrue(PrdIngestionService.isNoiseParagraph("主要功能点"));
        assertTrue(PrdIngestionService.isNoiseParagraph("功能描述"));
        assertTrue(PrdIngestionService.isNoiseParagraph("优先级"));
        assertTrue(PrdIngestionService.isNoiseParagraph("功能模块1"));
        assertTrue(PrdIngestionService.isNoiseParagraph("功能点1"));
        assertTrue(PrdIngestionService.isNoiseParagraph("高"));
        assertTrue(PrdIngestionService.isNoiseParagraph("中"));
        assertTrue(PrdIngestionService.isNoiseParagraph("低"));
        assertTrue(PrdIngestionService.isNoiseParagraph("风险"));
        assertTrue(PrdIngestionService.isNoiseParagraph("可能性"));
        assertTrue(PrdIngestionService.isNoiseParagraph("严重性"));
        assertTrue(PrdIngestionService.isNoiseParagraph("应对策略"));
        assertTrue(PrdIngestionService.isNoiseParagraph("可应对性"));
        assertTrue(PrdIngestionService.isNoiseParagraph("状态转换图："));
        assertTrue(PrdIngestionService.isNoiseParagraph("]"));
        assertTrue(PrdIngestionService.isNoiseParagraph("1."));
    }

    @Test
    void 真实需求与正文保留() {
        assertFalse(PrdIngestionService.isNoiseParagraph(
                "用户可以通过手机号+验证码登录，支持国际手机号格式。"));
        assertFalse(PrdIngestionService.isNoiseParagraph(
                "注册流程要求邮箱验证，验证链接有效期 24 小时。"));
        assertFalse(PrdIngestionService.isNoiseParagraph(
                "本文档为陌生视界 v1.0.0 的产品需求文档，描述了陌生人社交的核心功能。"));
        assertFalse(PrdIngestionService.isNoiseParagraph(
                "3.1 功能需求：用户可发布最多 9 张图片的动态。"));
        assertFalse(PrdIngestionService.isNoiseParagraph(
                "支付成功后 5 分钟内到账，支持微信支付与支付宝。"));
    }

    @Test
    void 需求描述清理尾部孤立右括号() {
        assertEquals("用户已登录，且为社团成员", PrdIngestionService.cleanReqDescription("用户已登录，且为社团成员]"));
        assertEquals("当用户点击\"行政楼\"菜单时，展示新闻中心介绍", PrdIngestionService.cleanReqDescription("当用户点击\"行政楼\"菜单时，展示新闻中心介绍]"));
        assertEquals("列表按发布时间由近到远展示", PrdIngestionService.cleanReqDescription("列表按发布时间由近到远展示]"));
        // 正常描述不受影响
        assertEquals("用户可发布最多 9 张图片的动态。", PrdIngestionService.cleanReqDescription("用户可发布最多 9 张图片的动态。"));
        // 空输入安全
        assertEquals("", PrdIngestionService.cleanReqDescription(null));
        assertEquals("", PrdIngestionService.cleanReqDescription(""));
    }
}