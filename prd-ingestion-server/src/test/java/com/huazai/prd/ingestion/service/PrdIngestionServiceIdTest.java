package com.huazai.prd.ingestion.service;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 回归测试：修复「REQ-xxx / AD-xxx / doc-xxx 业务 ID 跨进程碰撞导致 uk_req_id 唯一索引冲突」。
 *
 * <p>旧实现：业务 ID 只取 ingestionId 后 6 位（秒末两位 + 进程内静态计数器），
 * 进程重启后新 ingestion 会与历史数据撞出相同业务 ID（如 REQ-02-001-001），
 * 整个解析直接 failed。修复后业务 ID 基于完整 ingestionId 生成，且 ingestionId
 * 本身带 UUID 片段，跨进程/重启不再重复。</p>
 */
class PrdIngestionServiceIdTest {

    @Test
    void generateIngestionId_统一格式且多次调用不重复() {
        assertTrue(PrdIngestionService.generateIngestionId().startsWith("ing-"));
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < 2000; i++) {
            String id = PrdIngestionService.generateIngestionId();
            assertTrue(ids.add(id), "生成重复 ingestionId: " + id);
        }
    }

    @Test
    void idKey_去掉ing前缀保留唯一键() {
        assertEquals("20260818T145902-a1b2c3d4",
                PrdIngestionService.idKey("ing-20260818T145902-a1b2c3d4"));
        // 旧格式 ingestionId 同样支持（reparse 兼容历史数据）
        assertEquals("20260818T145902-001",
                PrdIngestionService.idKey("ing-20260818T145902-001"));
        // 不带 ing- 前缀时原样返回（防御空实现）
        assertEquals("20260818T145902-001", PrdIngestionService.idKey("20260818T145902-001"));
    }

    @Test
    void businessIds_不同ingestion必然生成不同前缀() {
        // 模拟 simulateParsing 中的 REQ-/AD- 前缀：key 不同 → 业务 ID 前缀必不同
        String key1 = PrdIngestionService.idKey(PrdIngestionService.generateIngestionId());
        String key2 = PrdIngestionService.idKey(PrdIngestionService.generateIngestionId());
        assertNotEquals(key1, key2);
        assertNotEquals("REQ-" + key1 + "-001", "REQ-" + key2 + "-001");
        assertNotEquals("AD-" + key1 + "-001", "AD-" + key2 + "-001");
        assertNotEquals("doc-bm-" + key1, "doc-bm-" + key2);
    }
}