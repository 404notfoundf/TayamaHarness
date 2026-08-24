package com.huazai.prd.ingestion.service;

import com.huazai.prd.ingestion.model.prd.ArchitectureDecision;
import com.huazai.prd.ingestion.model.prd.DataEntity;
import com.huazai.prd.ingestion.model.prd.InterfaceProtocol;
import com.huazai.prd.ingestion.model.prd.RequirementEntity;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 快速验证 ChangeComposer 函数在真实/模拟数据条件下的行为。
 */
class ChangeComposerVerificationTest {

    @Test
    void acceptanceCriteriaWithRealReqs() {
        RequirementEntity r1 = new RequirementEntity();
        r1.setId("REQ-001");
        r1.setDescription("用户点击大学生社区-行政楼，或点击其他引导到该板块的链接\n用户已登录且为社团成员\n当用户点击菜单时展示新闻中心和管理层介绍\n![配图](data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...)");
        r1.setPriority("P2");

        RequirementEntity r2 = new RequirementEntity();
        r2.setId("REQ-002");
        r2.setDescription("新闻发布中心主要展示编辑后台发布的校园新闻及系统公告；");
        r2.setPriority("P2");

        RequirementEntity r3 = new RequirementEntity();
        r3.setId("REQ-003");
        r3.setDescription("列表形式按发布时间由近到远顺序展示，默认显示前若干条");
        r3.setPriority("P2");

        List<RequirementEntity> reqs = List.of(r1, r2, r3);

        // 1. composeAcceptanceCriteria
        var acs = ChangeComposer.composeAcceptanceCriteria(reqs);
        System.out.println("=== ACs ===");
        for (var ac : acs) System.out.println("  " + ac);
        assertFalse(acs.isEmpty(), "应有 3 条 AC");
        assertTrue(acs.get(0).startsWith("AC-1:"), "AC-1 格式");

        // 2. extractOutOfScope with empty content
        var oos = ChangeComposer.extractOutOfScope("");
        System.out.println("=== OOS (empty content) ===");
        assertEquals(1, oos.size(), "空 content 应返回一条提示");

        // 3. composeEdgeCases with empty original + interfaces
        var edges = ChangeComposer.composeEdgeCases("", reqs, List.of(), List.of());
        System.out.println("=== EdgeCases (empty content, no interfaces) ===");
        System.out.println("  size=" + edges.size());

        // 4. composeNfrTable with empty archs
        var nfr = ChangeComposer.composeNfrTable(List.of());
        System.out.println("=== NFR (empty archs) ===");
        assertEquals("", nfr, "空 archs 应返回空");

        // 5. composeTestStrategy with empty data
        var strategy = ChangeComposer.composeTestStrategy(List.of(), List.of(), List.of(), List.of());
        System.out.println("=== TestStrategy (empty) ===");
        assertTrue(strategy.contains("待补充"), "空数据应返回待补充");

        // 6. 用户故事：验证 sanitize 过滤 base64
        var story = ChangeComposer.composeUserStory(reqs, "");
        System.out.println("=== Story ===");
        System.out.println(story);
        // 检查是否包含 REQ 条目
        assertTrue(story.contains("- [REQ-001]"), "应包含 REQ-001");
        // 检查是否过滤了 base64 图片行
        assertEquals(false, story.contains("data:image"), "应过滤 base64 图片");
    }
}