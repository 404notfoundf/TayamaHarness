package com.tayama.prd.ingestion.service;

import com.tayama.prd.ingestion.model.prd.ArchitectureDecision;
import com.tayama.prd.ingestion.model.prd.DataEntity;
import com.tayama.prd.ingestion.model.prd.InterfaceProtocol;
import com.tayama.prd.ingestion.model.prd.RequirementEntity;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ChangeComposer（第 5 个生成器）单测：覆盖 8 个章节推导的前提是「有据可依」——
 * 全部从真实解析产物/原文推导，宁缺毋滥；空输入返回空，不硬凑。
 */
class ChangeComposerTest {

    private RequirementEntity req(String id, String desc) {
        RequirementEntity r = new RequirementEntity();
        r.setId(id);
        r.setDescription(desc);
        r.setPriority("P2");
        return r;
    }

    private InterfaceProtocol iface(String method, String path, String summary) {
        InterfaceProtocol i = new InterfaceProtocol();
        i.setMethod(method);
        i.setPath(path);
        i.setSummary(summary);
        return i;
    }

    private DataEntity entity(String name) {
        DataEntity e = new DataEntity();
        e.setName(name);
        return e;
    }

    private ArchitectureDecision decision(String title, String decision) {
        ArchitectureDecision ad = new ArchitectureDecision();
        ad.setTitle(title);
        ad.setDecision(decision);
        return ad;
    }

    // ---------- 1. 用户故事 ----------

    @Test
    void userStory_filtersBase64ImageAndKeepsReqFormat() {
        List<RequirementEntity> reqs = List.of(
                req("REQ-1", "用户需登录系统查看学校新闻。"),
                req("REQ-2", "首页展示公告。\n![配图](data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==)")
        );
        String story = ChangeComposer.composeUserStory(reqs, null);
        assertTrue(story.contains("- [REQ-1] 用户需登录系统查看学校新闻。（优先级 P2）"),
                "应包含 REQ-1 条目，got: " + story);
        assertTrue(story.contains("data:image") == false, "应过滤 base64 图片行");
        assertTrue(story.contains("作为系统的使用者，我想要"), "应合成总括句");
    }

    @Test
    void userStory_emptyReqsReturnsEmpty() {
        assertEquals("", ChangeComposer.composeUserStory(List.of(), null));
    }

    // ---------- 2. 非目标 ----------

    @Test
    void outOfScope_extractsExclusionSentences() {
        String prd = "# 产品需求说明书\n"
                + "## 非目标（Out of Scope）\n"
                + "1. 本次不做多端离线缓存；\n"
                + "2. 不包含消息推送能力。\n"
                + "## 目标\n"
                + "统一信息发布。";
        List<String> out = ChangeComposer.extractOutOfScope(prd);
        assertFalse(out.isEmpty(), "应提出排除句");
        assertTrue(out.get(0).contains("不做"), "grep 出的首条应含排除特征词，got: " + out);
    }

    @Test
    void outOfScope_withoutHintsReturnsFallback() {
        // 无排除句时返回诚实提示，替代模板占位符 - …
        var result = ChangeComposer.extractOutOfScope("## 目标\n普通内容");
        assertEquals(1, result.size(), "应返回一条提示");
        assertTrue(result.get(0).contains("暂无"), "提示应包含「暂无」");
    }

    // ---------- 3. 验收标准 ----------

    @Test
    void acceptanceCriteria_mapsEachReqToAC() {
        List<RequirementEntity> reqs = List.of(
                req("REQ-1", "支持按发布时间倒序展示新闻列表。"),
                req("REQ-2", "公告支持撤回与重新发布。")
        );
        List<String> acs = ChangeComposer.composeAcceptanceCriteria(reqs);
        assertEquals(2, acs.size());
        assertTrue(acs.get(0).startsWith("AC-1: **"), acs.get(0));
        assertTrue(acs.get(0).endsWith("（出处 REQ-1）"), acs.get(0));
    }

    @Test
    void acceptanceCriteria_emptyReqsReturnsEmpty() {
        assertEquals(List.of(), ChangeComposer.composeAcceptanceCriteria(null));
    }

    // ---------- 4. 边界情况 ----------

    @Test
    void edgeCases_conditionSentenceAndRestBoundaries() {
        String original = "当用户未登录时，直接返回登录页。";
        List<RequirementEntity> reqs = List.of(req("REQ-1", "分页查询新闻列表。"));
        List<InterfaceProtocol> ifaces = List.of(
                iface("GET", "/api/v1/news/list", "分页查询"),
                iface("GET", "/api/v1/news/{id}", "详情"),
                iface("POST", "/api/v1/news/create", "发布")
        );
        List<String> edges = ChangeComposer.composeEdgeCases(original, reqs, ifaces, List.of());
        assertFalse(edges.isEmpty());
        assertTrue(edges.stream().anyMatch(e -> e.contains("未登录")), "原文条件句应产出边界");
        assertTrue(edges.stream().anyMatch(e -> e.contains("404")), "按标识查询应推导 404 边界");
        assertTrue(edges.stream().anyMatch(e -> e.contains("400")), "写操作应推导校验失败边界");
    }

    @Test
    void edgeCases_nothingReturnsEmpty() {
        assertEquals(List.of(), ChangeComposer.composeEdgeCases("普通内容。", List.of(), List.of(), List.of()));
    }

    // ---------- 5. 非功能需求 ----------

    @Test
    void nfrTable_mapsDecisionsToDimensions() {
        List<ArchitectureDecision> ds = List.of(
                decision("性能需求", "首屏加载不超过 2 秒，支持 1 万并发。"),
                decision("监控需求", "核心接口接入日志与告警。"),
                decision("兼容性需求", "兼容主流浏览器。")
        );
        String table = ChangeComposer.composeNfrTable(ds);
        assertTrue(table.startsWith("| 维度 | 指标 |"), table);
        assertTrue(table.contains("| 性能 |"), "应有性能行");
        assertTrue(table.contains("| 可观测 |"), "监控应落到可观测维度");
        assertTrue(table.contains("| 兼容性 |"), "应有兼容性行");
    }

    @Test
    void nfrTable_noHitsReturnsEmpty() {
        assertEquals("", ChangeComposer.composeNfrTable(List.of(decision("业务决策", "采用模块化架构。"))));
        assertEquals("", ChangeComposer.composeNfrTable(null));
    }

    // ---------- 6. 测试策略 ----------

    @Test
    void testStrategy_derivesFromInterfacesAndEdges() {
        List<InterfaceProtocol> ifaces = List.of(
                iface("GET", "/api/v1/news/list", "分页查询"),
                iface("POST", "/api/v1/news/create", "发布")
        );
        String strategy = ChangeComposer.composeTestStrategy(ifaces, List.of(), null, List.of("边界一。" , "边界二。"));
        assertTrue(strategy.contains("先写失败测试"), strategy);
        assertTrue(strategy.contains("news"), strategy);
        assertTrue(strategy.contains("B-1..B-2"), "边界测试应引用边界章节条数");
    }

    @Test
    void testStrategy_emptyKeepsTbd() {
        String strategy = ChangeComposer.composeTestStrategy(List.of(), List.of(), null, List.of());
        assertTrue(strategy.contains("待补充"), strategy);
    }

    // ---------- 7. 任务拆解 ----------

    @Test
    void tasks_derivesLayeredTasksWithDeps() {
        List<DataEntity> entities = List.of(entity("新闻表"));
        List<InterfaceProtocol> ifaces = List.of(
                iface("GET", "/api/v1/news/list", "分页查询")
        );
        String tasks = ChangeComposer.composeTasks(entities, ifaces);
        assertTrue(tasks.contains("T-1: 新闻表 — 表结构与数据模型定义 · P0 · 依赖 - · 模块 新闻表"), tasks);
        assertTrue(tasks.contains("T-2"), "应有依赖链后续任务");
        assertTrue(tasks.contains("依赖 T-1"), "T-2 应依赖 T-1");
    }

    @Test
    void tasks_emptyReturnsEmpty() {
        assertEquals("", ChangeComposer.composeTasks(List.of(), List.of()));
    }

    // ---------- 8. 验收用例 / 流水线进度 ----------

    @Test
    void acceptanceCases_andPipelineProgress() {
        List<InterfaceProtocol> ifaces = List.of(
                iface("GET", "/api/v1/news/list", "分页查询")
        );
        String cases = ChangeComposer.composeAcceptanceCases(ifaces);
        assertTrue(cases.contains("Case-1"), cases);
        assertTrue(cases.contains("news"), cases);

        String progress = ChangeComposer.composePipelineProgress();
        assertTrue(progress.contains("需求分析"), progress);
        assertTrue(progress.contains("部署验证"), progress);
    }

    // ---------- 辅助工具 ----------

    @Test
    void helpers_sentenceSplitAndResourceName() {
        assertEquals(2, ChangeComposer.splitSentences("第一句。第二句！").size());
        assertEquals("news", ChangeComposer.resourceName("/api/v1/news/list"));
        assertEquals("news", ChangeComposer.resourceName("/api/v1/news/{id}?page=1"));
        assertEquals(null, ChangeComposer.resourceName(""));
    }

    @Test
    void sanitize_removesImageAndBlankLines() {
        String cleaned = ChangeComposer.sanitize("第一行。\n\n![图](x)\n第二行。");
        assertEquals("第一行。\n第二行。", cleaned);
    }

    // ---------- 9. 标题推导 ----------

    @Test
    void classifyDomain_oauthLoginByTitle() {
        assertEquals("oauth-login", ChangeComposer.classifyDomain("github 授权登录"));
        assertEquals("oauth-login", ChangeComposer.classifyDomain("微信 SSO 认证"));
        assertEquals("oauth-login", ChangeComposer.classifyDomain("OAuth Login"));
        assertNull(ChangeComposer.classifyDomain("普通变更"));
        assertNull(ChangeComposer.classifyDomain(null));
        assertNull(ChangeComposer.classifyDomain(""));
    }

    @Test
    void deriveFromTitle_githubLoginFillsAllSections() {
        var derived = ChangeComposer.deriveFromTitle("github login");
        assertFalse(derived.isEmpty(), "github login 应命中 oauth-login 域，返回非空 Map");
        assertTrue(derived.containsKey("用户故事"), "应包含用户故事章节");
        assertTrue(derived.get("用户故事").contains("GitHub"), "用户故事应提及 GitHub");
        assertTrue(derived.get("验收标准").contains("OAuth 2.0"), "验收标准应含 OAuth 2.0");
        assertTrue(derived.get("边界情况").contains("state"), "边界情况应含 state 参数");
        assertTrue(derived.get("非功能需求").contains("HTTPS"), "非功能需求应含 HTTPS");
        assertTrue(derived.get("设计约束").contains("Client ID"), "设计约束应含 Client ID");
        assertTrue(derived.get("契约影响").contains("/api/v1/oauth/github/"), "契约影响应含 GitHub 回调路径");
        assertTrue(derived.get("测试策略").contains("409"), "测试策略应含 409 冲突");
    }

    @Test
    void deriveFromTitle_wechatLoginUsesWechatProvider() {
        var derived = ChangeComposer.deriveFromTitle("微信登录");
        assertTrue(derived.get("用户故事").contains("微信"), "微信登录的用户故事应提及微信");
        assertTrue(derived.get("契约影响").contains("/api/v1/oauth/微信"), "契约影响应含微信回调路径");
    }

    @Test
    void deriveFromTitle_nonMatchingTitleReturnsEmpty() {
        var derived = ChangeComposer.deriveFromTitle("普通功能变更");
        assertTrue(derived.isEmpty(), "不匹配域的标题应返回空 Map，走 PRD 产物路径");
    }

    @Test
    void classifyDomain_adminBackend() {
        assertEquals("admin-backend", ChangeComposer.classifyDomain("管理后台 CRUD"));
        assertEquals("admin-backend", ChangeComposer.classifyDomain("admin dashboard"));
        assertEquals("admin-backend", ChangeComposer.classifyDomain("控制台功能"));
    }

    @Test
    void classifyDomain_search() {
        assertEquals("search", ChangeComposer.classifyDomain("全文搜索"));
        assertEquals("search", ChangeComposer.classifyDomain("检索引擎"));
    }

    @Test
    void classifyDomain_payment() {
        assertEquals("payment", ChangeComposer.classifyDomain("微信支付对接"));
        assertEquals("payment", ChangeComposer.classifyDomain("支付宝订单结算"));
    }

    @Test
    void classifyDomain_messagePush() {
        assertEquals("message-push", ChangeComposer.classifyDomain("消息推送通知"));
        assertEquals("message-push", ChangeComposer.classifyDomain("push notification"));
    }

    @Test
    void deriveFromTitle_adminBackendFillsAllSections() {
        var derived = ChangeComposer.deriveFromTitle("管理后台 CRUD");
        assertFalse(derived.isEmpty());
        assertTrue(derived.get("用户故事").contains("管理员"));
        assertTrue(derived.get("验收标准").contains("审计日志"));
        assertTrue(derived.get("契约影响").contains("/api/v1/admin/"));
        assertTrue(derived.get("测试策略").contains("403"));
    }

    @Test
    void deriveFromTitle_searchFillsAllSections() {
        var derived = ChangeComposer.deriveFromTitle("全文搜索");
        assertFalse(derived.isEmpty());
        assertTrue(derived.get("用户故事").contains("搜索"));
        assertTrue(derived.get("验收标准").contains("1 秒"));
        assertTrue(derived.get("契约影响").contains("/api/v1/search"));
        assertTrue(derived.get("测试策略").contains("降级数据库"));
    }

    @Test
    void deriveFromTitle_paymentFillsAllSections() {
        var derived = ChangeComposer.deriveFromTitle("微信支付");
        assertFalse(derived.isEmpty());
        assertTrue(derived.get("用户故事").contains("微信支付"));
        assertTrue(derived.get("验收标准").contains("回调"));
        assertTrue(derived.get("边界情况").contains("幂等"));
        assertTrue(derived.get("契约影响").contains("/api/v1/payment/"));
    }

    @Test
    void deriveFromTitle_messagePushFillsAllSections() {
        var derived = ChangeComposer.deriveFromTitle("消息推送");
        assertFalse(derived.isEmpty());
        assertTrue(derived.get("用户故事").contains("通知"));
        assertTrue(derived.get("验收标准").contains("推送偏好"));
        assertTrue(derived.get("契约影响").contains("WebSocket"));
        assertTrue(derived.get("测试策略").contains("死信队列"));
    }
}