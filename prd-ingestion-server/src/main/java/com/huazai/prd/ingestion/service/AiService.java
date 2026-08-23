package com.huazai.prd.ingestion.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huazai.prd.ingestion.config.AiProperties;
import com.huazai.prd.ingestion.model.prd.PrdParseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * AI/LLM 解析服务。
 *
 * <p>调用 LLM API（OpenAI 兼容格式）对 PRD 内容进行结构化分析，
 * 输出 JSON 格式的需求、实体、接口、架构决策。</p>
 */
@Service
public class AiService {

    private static final Logger LOG = LoggerFactory.getLogger(AiService.class);

    private final AiProperties aiProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /** 最近一次 LLM 解析失败的原因（解析成功时为 null），供上层展示/记录。 */
    private volatile String lastError;

    public AiService(AiProperties aiProperties, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.aiProperties = aiProperties;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 最近一次 LLM 解析失败的原因，供上层展示/记录。
     */
    public String getLastError() {
        return lastError;
    }

    /**
     * 使用 LLM 解析 PRD 内容。
     *
     * <p>发送前剔除 base64 图片数据并做超长截断，避免超大 payload 导致超时/内容超限；
     * 失败自动重试 1 次。</p>
     *
     * @param prdContent 原始 PRD 内容
     * @return 结构化解析结果，解析失败返回 null（可通过 {@link #getLastError()} 获取原因）
     */
    public PrdParseResult parse(String prdContent) {
        lastError = null;
        if (prdContent == null || prdContent.isBlank()) return null;

        // Mock 模式：返回预设结果
        if (aiProperties.isMock()) {
            LOG.info("[AiService] 使用 mock 模式解析 PRD");
            return mockParse(prdContent);
        }

        // 真实调用 LLM API：先剔除 base64 图片，压缩 prompt 体积，降低超时/超限概率
        String prompt = buildPrompt(cleanForLlm(prdContent));
        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                String response = callLlmApi(prompt);
                PrdParseResult result = parseResponse(response);
                if (result != null) {
                    result.setParseSource("llm");
                    LOG.info("[AiService] LLM 解析成功: {} 条需求, {} 个实体, {} 个接口, {} 个决策",
                            result.getRequirements().size(), result.getDataEntities().size(),
                            result.getInterfaces().size(), result.getArchDecisions().size());
                }
                return result;
            } catch (Exception e) {
                lastError = e.getMessage();
                if (attempt == 1) {
                    // 网络抖动/瞬时超时常见，重试一次
                    LOG.warn("[AiService] LLM 解析第 1 次尝试失败, 2 秒后重试: {}", e.getMessage());
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return null;
                    }
                } else {
                    // 单行摘要即可（详情已由 callLlmApi 记录），避免 429 配额问题刷两遍完整堆栈
                    LOG.error("[AiService] LLM 解析失败(已重试 1 次): {}", e.getMessage());
                    LOG.debug("[AiService] LLM 解析失败详情", e);
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * 发送给 LLM 前的内容清理：
     * 1. 剔除 base64 图片数据（data:image/...;base64,xxxx），替换为占位标记；
     * 2. 仍超长的内容截断（保留开头 + 结尾），避免 token 超限。
     */
    static String cleanForLlm(String prdContent) {
        if (prdContent == null) return null;
        String cleaned = prdContent.replaceAll("data:image/[a-zA-Z0-9.+-]+;base64,[A-Za-z0-9+/=\\s]+", "[图片]");
        if (cleaned.length() > 30000) {
            cleaned = cleaned.substring(0, 22000)
                    + "\n\n...(中间内容过长已省略，共省略 "
                    + (cleaned.length() - 30000) + " 字符)...\n\n"
                    + cleaned.substring(cleaned.length() - 8000);
        }
        return cleaned;
    }

    /**
     * 构建 LLM 提示词。
     */
    private String buildPrompt(String prdContent) {
        return """
                你是一个资深的 PRD 需求分析师和软件开发架构师。
                请分析以下用户需求文档（可能是产品PRD、业务需求文档、技术设计文档等），输出结构化的 JSON 格式。

                要求：
                1. 仔细分析文档中的每一个要点，不要遗漏任何功能。
                2. 需求描述要具体、完整，使用中文。
                3. 数据实体和属性要贴合业务场景，字段名使用驼峰命名。
                4. 接口路径遵循 RESTful 规范，使用 /api/v1/ 前缀。
                5. 架构决策要有实际技术依据。

                用户需求：
                """
                + prdContent + """

                请严格输出以下 JSON 格式（不要 markdown 代码块标记，直接输出纯 JSON，不要加任何注释和说明）：
                {
                  "requirements": [
                    { "id": "REQ-001", "description": "需求描述", "priority": "P1" }
                  ],
                  "dataEntities": [
                    {
                      "name": "实体名（英文驼峰）",
                      "description": "实体描述",
                      "sourceSection": "该实体在 PRD 中对应的章节标题（如"需求分析"或"功能模块"），无法定位则留空",
                      "attributes": [
                        { "name": "属性名", "type": "String/Long/Integer/Boolean/DateTime/BigDecimal", "description": "属性描述" }
                      ],
                      "relations": [
                        { "target": "目标实体名", "type": "OneToMany/ManyToOne", "description": "关系描述" }
                      ]
                    }
                  ],
                  "interfaces": [
                    { "method": "GET/POST/PUT/DELETE", "path": "/api/v1/...", "summary": "接口描述", "sourceSection": "该接口在 PRD 中对应的章节标题", "requestBody": "请求体示例JSON", "responseBody": "响应体示例JSON" }
                  ],
                  "archDecisions": [
                    { "title": "决策标题", "context": "决策背景", "decision": "决策内容", "sourceSection": "该决策在 PRD 中对应的章节标题", "consequences": ["影响1", "影响2"] }
                  ]
                }
                """;
    }

    /**
     * 调用 LLM API（OpenAI 兼容格式）。
     */
    private String callLlmApi(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(aiProperties.getApiKey());

        // 与官方示例对齐：system 定义角色 + user 携带 PRD 内容
        Map<String, Object> systemMessage = Map.of(
                "role", "system",
                "content", "你是一个资深的 PRD 需求分析师和软件开发架构师，负责从各种需求/设计文档（产品PRD、业务需求文档、技术设计文档等）中提取结构化信息，请严格按用户要求的 JSON 格式输出结构化解析结果，不要输出任何多余说明。\n\n注意：每个 dataEntity 请带 sourceSection 字段（值为该实体在文档中对应的章节标题或关键词，如 \"需求分析\" 或 \"功能模块\"）；每个 interface 请带 sourceSection 字段（值为该接口在文档中对应的章节标题，如 \"接口定义\" 或 \"功能详细设计\"）；每个 architectureDecision 请带 sourceSection 字段（值为该决策在文档中对应的章节标题，如 \"非功能需求\" 或 \"架构设计\"）。如果无法定位，sourceSection 设为空字符串。"
        );
        Map<String, Object> userMessage = Map.of(
                "role", "user",
                "content", prompt
        );

        // stream=false 显式声明（默认即 false，但显式更稳妥）；
        // reasoning_effort=none 关闭思考型模型（glm/sensenova 系列）的深度思考，避免响应超时
        Map<String, Object> requestBody = new java.util.LinkedHashMap<>();
        requestBody.put("model", aiProperties.getModel());
        requestBody.put("messages", List.of(systemMessage, userMessage));
        requestBody.put("temperature", aiProperties.getTemperature());
        requestBody.put("max_tokens", aiProperties.getMaxTokens());
        requestBody.put("stream", false);
        if (aiProperties.getReasoningEffort() != null && !aiProperties.getReasoningEffort().isBlank()) {
            requestBody.put("reasoning_effort", aiProperties.getReasoningEffort());
        }

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        // ---- 详细请求日志（用于排查超时/401/429 等问题） ----
        String apiKey = aiProperties.getApiKey();
        String maskedKey = aiProperties.isLogFullApiKey() ? apiKey : maskKey(apiKey);
        LOG.info("[AiService] LLM 请求 >>> method=POST, url={}, model={}, stream=false, reasoningEffort={}, "
                        + "temperature={}, maxTokens={}, promptChars={}, messageCount=2, apiKeyConfigured={}, apiKey={}, headers={}",
                aiProperties.getApiUrl(), aiProperties.getModel(),
                aiProperties.getReasoningEffort() == null || aiProperties.getReasoningEffort().isBlank() ? "(not set)" : aiProperties.getReasoningEffort(),
                aiProperties.getTemperature(),
                aiProperties.getMaxTokens(), prompt.length(),
                apiKey != null && !apiKey.isBlank(), maskedKey, safeHeaders(headers));

        long start = System.currentTimeMillis();
        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(
                    aiProperties.getApiUrl(), HttpMethod.POST, request, String.class);
        } catch (HttpStatusCodeException e) {
            // 4xx/5xx 是确定性业务错误（429 配额耗尽、401 鉴权失败等），堆栈无诊断价值，
            // 只打单行摘要避免刷屏；需要细节时开 DEBUG 级日志。
            long cost = System.currentTimeMillis() - start;
            LOG.error("[AiService] LLM 请求失败 <<< status={}, costMs={}, responseBody={}",
                    e.getStatusCode().value(), cost, truncate(e.getResponseBodyAsString(), 2000));
            LOG.debug("[AiService] LLM 请求失败详情", e);
            throw new RuntimeException("LLM API 返回错误状态码: " + e.getStatusCode().value()
                    + ", body=" + truncate(e.getResponseBodyAsString(), 500), e);
        } catch (ResourceAccessException e) {
            // 超时/连接失败是常见环境故障（网络抖动、代理），同样只打单行摘要。
            long cost = System.currentTimeMillis() - start;
            LOG.error("[AiService] LLM 请求超时/连接失败 <<< costMs={}, url={}, timeoutSeconds={}, 错误: {}",
                    cost, aiProperties.getApiUrl(), aiProperties.getTimeout(), e.getMessage());
            LOG.debug("[AiService] LLM 请求超时/连接失败详情", e);
            throw new RuntimeException("LLM API 请求超时: " + e.getMessage(), e);
        }
        long cost = System.currentTimeMillis() - start;

        String body = response.getBody() == null ? "" : response.getBody();
        LOG.info("[AiService] LLM 响应 <<< status={}, costMs={}, bodyChars={}, body={}",
                response.getStatusCode().value(), cost, body.length(), truncate(body, 2000));

        if (body == null || body.isBlank()) {
            throw new RuntimeException("LLM API 返回空响应");
        }

        // 从 OpenAI 响应格式中提取 content
        JsonNode json;
        try {
            json = objectMapper.readTree(body);
        } catch (JsonProcessingException e) {
            LOG.error("[AiService] LLM 响应不是合法 JSON: {}", truncate(body, 2000));
            throw new RuntimeException("LLM API 响应不是合法 JSON", e);
        }
        JsonNode choice = json.path("choices").get(0);
        if (choice == null) {
            throw new RuntimeException("LLM API 响应缺少 choices 字段: " + truncate(body, 500));
        }
        String content = choice.path("message").path("content").asText();
        if (content == null || content.isBlank()) {
            throw new RuntimeException("LLM API 返回的 content 为空");
        }

        return content;
    }

    /**
     * 打印请求头，Authorization 值脱敏。
     */
    private String safeHeaders(HttpHeaders headers) {
        java.util.List<String> parts = new java.util.ArrayList<>();
        headers.forEach((name, values) -> {
            String value = String.join(",", values);
            if ("Authorization".equalsIgnoreCase(name)) {
                value = "Bearer " + maskKey(value.replaceFirst("(?i)^Bearer\\s+", ""));
            }
            parts.add(name + "=" + value);
        });
        return "{" + String.join(", ", parts) + "}";
    }

    /**
     * API Key 脱敏：保留前 4 后 4 位。
     */
    private String maskKey(String key) {
        if (key == null || key.isBlank()) return "<empty>";
        if (key.length() <= 8) return "****";
        return key.substring(0, 4) + "****" + key.substring(key.length() - 4);
    }

    /**
     * 超长字符串截断（日志防刷屏）。
     */
    private String truncate(String s, int maxLen) {
        if (s == null) return "null";
        return s.length() <= maxLen ? s : s.substring(0, maxLen) + "...(truncated, total=" + s.length() + ")";
    }

    /**
     * 解析 LLM 返回的 JSON 字符串为 PrdParseResult。
     */
    private PrdParseResult parseResponse(String jsonString) {
        // 清理：如果 LLM 返回了 markdown 代码块标记，去掉
        String cleaned = jsonString.trim();
        if (cleaned.startsWith("```")) {
            int firstNewline = cleaned.indexOf('\n');
            if (firstNewline > 0) {
                cleaned = cleaned.substring(firstNewline).trim();
            }
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3).trim();
        }

        try {
            return objectMapper.readValue(cleaned, PrdParseResult.class);
        } catch (JsonProcessingException e) {
            LOG.error("[AiService] JSON 解析失败: {}", e.getMessage());
            LOG.debug("[AiService] 原始响应: {}", jsonString);
            return null;
        }
    }

    // ========== Mock 模式 ==========

    /**
     * Mock 模式：根据关键词返回预设的解析结果。
     * 用于开发调试，不需要真实 LLM API。
     */
    private PrdParseResult mockParse(String content) {
        String lower = content.toLowerCase();

        // GitHub 登录
        if (lower.contains("github") || lower.contains("oauth") || lower.contains("授权登录")) {
            return mockGitHubLogin();
        }

        // 支付
        if (lower.contains("支付") || lower.contains("付款") || lower.contains("下单")) {
            return mockPayment();
        }

        // 审批
        if (lower.contains("审批") || lower.contains("审核")) {
            return mockApproval();
        }

        // 通用 CRUD
        return mockGenericCrud();
    }

    private PrdParseResult mockGitHubLogin() {
        PrdParseResult result = new PrdParseResult();

        // 需求
        var req1 = new PrdParseResult.RequirementItem();
        req1.setId("REQ-001");
        req1.setDescription("用户可以通过 GitHub 账号授权登录系统");
        req1.setPriority("P1");
        result.getRequirements().add(req1);

        var req2 = new PrdParseResult.RequirementItem();
        req2.setId("REQ-002");
        req2.setDescription("首次登录时自动创建本地账号并绑定 GitHub 身份");
        req2.setPriority("P1");
        result.getRequirements().add(req2);

        var req3 = new PrdParseResult.RequirementItem();
        req3.setId("REQ-003");
        req3.setDescription("未登录用户访问受保护资源时自动跳转到登录页面");
        req3.setPriority("P2");
        result.getRequirements().add(req3);

        // 实体
        var user = new PrdParseResult.EntityItem();
        user.setName("User");
        user.setDescription("系统用户");
        var u1 = new PrdParseResult.EntityItem.AttributeItem();
        u1.setName("id"); u1.setType("Long"); u1.setDescription("用户 ID");
        user.getAttributes().add(u1);
        var u2 = new PrdParseResult.EntityItem.AttributeItem();
        u2.setName("username"); u2.setType("String"); u2.setDescription("GitHub 用户名");
        user.getAttributes().add(u2);
        var u3 = new PrdParseResult.EntityItem.AttributeItem();
        u3.setName("avatarUrl"); u3.setType("String"); u3.setDescription("头像 URL");
        user.getAttributes().add(u3);
        var u4 = new PrdParseResult.EntityItem.AttributeItem();
        u4.setName("email"); u4.setType("String"); u4.setDescription("邮箱");
        user.getAttributes().add(u4);
        result.getDataEntities().add(user);

        var oauth = new PrdParseResult.EntityItem();
        oauth.setName("OAuthAccount");
        oauth.setDescription("第三方账号绑定信息");
        var o1 = new PrdParseResult.EntityItem.AttributeItem();
        o1.setName("id"); o1.setType("Long"); o1.setDescription("唯一标识");
        oauth.getAttributes().add(o1);
        var o2 = new PrdParseResult.EntityItem.AttributeItem();
        o2.setName("platform"); o2.setType("String"); o2.setDescription("平台: GITHUB");
        oauth.getAttributes().add(o2);
        var o3 = new PrdParseResult.EntityItem.AttributeItem();
        o3.setName("platformUserId"); o3.setType("String"); o3.setDescription("GitHub 用户 ID");
        oauth.getAttributes().add(o3);
        var o4 = new PrdParseResult.EntityItem.AttributeItem();
        o4.setName("accessToken"); o4.setType("String"); o4.setDescription("Access Token（加密）");
        oauth.getAttributes().add(o4);
        var o5 = new PrdParseResult.EntityItem.AttributeItem();
        o5.setName("userId"); o5.setType("Long"); o5.setDescription("关联系统用户 ID");
        oauth.getAttributes().add(o5);
        result.getDataEntities().add(oauth);

        var rel = new PrdParseResult.EntityItem.RelationItem();
        rel.setTarget("User"); rel.setType("ManyToOne"); rel.setDescription("一个用户可绑定多个第三方账号");
        oauth.getRelations().add(rel);

        // 接口
        var if1 = new PrdParseResult.InterfaceItem();
        if1.setMethod("GET"); if1.setPath("/api/v1/auth/github/login");
        if1.setSummary("跳转 GitHub 授权页面"); if1.setResponseBody("{\"redirectUrl\": \"https://github.com/login/oauth/authorize?...\"}");
        result.getInterfaces().add(if1);

        var if2 = new PrdParseResult.InterfaceItem();
        if2.setMethod("GET"); if2.setPath("/api/v1/auth/github/callback");
        if2.setSummary("GitHub 授权回调处理"); if2.setRequestBody("code=abc123&state=xyz");
        if2.setResponseBody("{\"token\": \"jwt...\", \"user\": {\"id\": 1, \"username\": \"octocat\"}}");
        result.getInterfaces().add(if2);

        var if3 = new PrdParseResult.InterfaceItem();
        if3.setMethod("POST"); if3.setPath("/api/v1/auth/logout");
        if3.setSummary("用户登出"); if3.setResponseBody("{\"message\": \"ok\"}");
        result.getInterfaces().add(if3);

        // 决策
        var ad1 = new PrdParseResult.ArchDecisionItem();
        ad1.setTitle("采用 OAuth 2.0 授权码模式");
        ad1.setContext("GitHub 授权登录需要走标准 OAuth 2.0 流程");
        ad1.setDecision("使用授权码模式，后端完成 code → access_token 交换，前端只负责跳转");
        ad1.setConsequences(List.of("access_token 不暴露给前端", "需要配置 GitHub OAuth App"));
        result.getArchDecisions().add(ad1);

        var ad2 = new PrdParseResult.ArchDecisionItem();
        ad2.setTitle("使用 JWT 作为登录态凭证");
        ad2.setContext("需要无状态、跨服务共享的登录态");
        ad2.setDecision("用户登录成功后发放 JWT，设置 24h 过期，支持 refresh token 续期");
        ad2.setConsequences(List.of("无需服务端 session 存储", "JWT 可携带用户基础信息"));
        result.getArchDecisions().add(ad2);

        return result;
    }

    private PrdParseResult mockPayment() {
        PrdParseResult result = new PrdParseResult();

        var req = new PrdParseResult.RequirementItem();
        req.setId("REQ-001"); req.setDescription("用户可选择支付方式完成支付"); req.setPriority("P1");
        result.getRequirements().add(req);

        var entity = new PrdParseResult.EntityItem();
        entity.setName("Order"); entity.setDescription("订单信息");
        var a1 = new PrdParseResult.EntityItem.AttributeItem();
        a1.setName("id"); a1.setType("Long"); a1.setDescription("订单 ID");
        entity.getAttributes().add(a1);
        var a2 = new PrdParseResult.EntityItem.AttributeItem();
        a2.setName("orderNo"); a2.setType("String"); a2.setDescription("订单编号");
        entity.getAttributes().add(a2);
        var a3 = new PrdParseResult.EntityItem.AttributeItem();
        a3.setName("amount"); a3.setType("BigDecimal"); a3.setDescription("金额");
        entity.getAttributes().add(a3);
        var a4 = new PrdParseResult.EntityItem.AttributeItem();
        a4.setName("status"); a4.setType("String"); a4.setDescription("状态");
        entity.getAttributes().add(a4);
        result.getDataEntities().add(entity);

        var if1 = new PrdParseResult.InterfaceItem();
        if1.setMethod("POST"); if1.setPath("/api/v1/payments/pay");
        if1.setSummary("发起支付"); if1.setResponseBody("{\"payUrl\": \"...\"}");
        result.getInterfaces().add(if1);

        result.getArchDecisions().add(makeMockAd("对接第三方支付网关", "需要支持微信/支付宝支付", "统一封装支付网关接口"));
        return result;
    }

    private PrdParseResult mockApproval() {
        PrdParseResult result = new PrdParseResult();
        var req = new PrdParseResult.RequirementItem();
        req.setId("REQ-001"); req.setDescription("用户可提交审批申请"); req.setPriority("P1");
        result.getRequirements().add(req);

        var entity = new PrdParseResult.EntityItem();
        entity.setName("ApprovalRequest"); entity.setDescription("审批申请单");
        var a1 = new PrdParseResult.EntityItem.AttributeItem();
        a1.setName("id"); a1.setType("Long"); a1.setDescription("申请 ID");
        entity.getAttributes().add(a1);
        var a2 = new PrdParseResult.EntityItem.AttributeItem();
        a2.setName("title"); a2.setType("String"); a2.setDescription("审批标题");
        entity.getAttributes().add(a2);
        var a3 = new PrdParseResult.EntityItem.AttributeItem();
        a3.setName("status"); a3.setType("String"); a3.setDescription("状态");
        entity.getAttributes().add(a3);
        result.getDataEntities().add(entity);

        result.getInterfaces().add(makeMockIf("POST", "/api/v1/approvals/create", "提交审批申请"));
        result.getInterfaces().add(makeMockIf("POST", "/api/v1/approvals/{id}/approve", "审批通过"));
        result.getInterfaces().add(makeMockIf("POST", "/api/v1/approvals/{id}/reject", "审批驳回"));
        return result;
    }

    private PrdParseResult mockGenericCrud() {
        PrdParseResult result = new PrdParseResult();
        var req = new PrdParseResult.RequirementItem();
        req.setId("REQ-001"); req.setDescription("支持对业务数据的新增、修改、删除、查询操作"); req.setPriority("P1");
        result.getRequirements().add(req);

        var entity = new PrdParseResult.EntityItem();
        entity.setName("BusinessEntity"); entity.setDescription("业务实体");
        var a1 = new PrdParseResult.EntityItem.AttributeItem();
        a1.setName("id"); a1.setType("Long"); a1.setDescription("唯一标识");
        entity.getAttributes().add(a1);
        var a2 = new PrdParseResult.EntityItem.AttributeItem();
        a2.setName("name"); a2.setType("String"); a2.setDescription("名称");
        entity.getAttributes().add(a2);
        var a3 = new PrdParseResult.EntityItem.AttributeItem();
        a3.setName("status"); a3.setType("String"); a3.setDescription("状态");
        entity.getAttributes().add(a3);
        result.getDataEntities().add(entity);

        result.getInterfaces().add(makeMockIf("GET", "/api/v1/entities/list", "分页查询"));
        result.getInterfaces().add(makeMockIf("POST", "/api/v1/entities", "新增记录"));
        result.getInterfaces().add(makeMockIf("PUT", "/api/v1/entities/{id}", "修改记录"));
        result.getInterfaces().add(makeMockIf("DELETE", "/api/v1/entities/{id}", "删除记录"));
        return result;
    }

    private static PrdParseResult.InterfaceItem makeMockIf(String method, String path, String summary) {
        var iface = new PrdParseResult.InterfaceItem();
        iface.setMethod(method); iface.setPath(path); iface.setSummary(summary);
        return iface;
    }

    private static PrdParseResult.ArchDecisionItem makeMockAd(String title, String context, String decision) {
        var ad = new PrdParseResult.ArchDecisionItem();
        ad.setTitle(title); ad.setContext(context); ad.setDecision(decision);
        return ad;
    }
}