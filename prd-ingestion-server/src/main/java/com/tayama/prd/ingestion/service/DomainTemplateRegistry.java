package com.tayama.prd.ingestion.service;

import com.tayama.prd.ingestion.model.prd.DomainTemplate;
import com.tayama.prd.ingestion.model.prd.PrdParseResult;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 领域模板注册中心。
 *
 * <p>内置高频业务场景的领域知识模板，用于快速匹配常见需求。
 * 当 PRD 内容关键词命中模板时，直接实例化模板而不需要 LLM 调用。</p>
 */
@Component
public class DomainTemplateRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(DomainTemplateRegistry.class);

    private final List<DomainTemplate> templates = new ArrayList<>();

    @PostConstruct
    public void init() {
        registerOAuthLoginTemplate();
        registerPaymentTemplate();
        registerApprovalTemplate();
        registerNotificationTemplate();
        registerCrudTemplate();
        registerFileUploadTemplate();
        registerSearchTemplate();
        registerDataExportTemplate();
        LOG.info("[DomainTemplateRegistry] 已注册 {} 个领域模板", templates.size());
    }

    /**
     * 根据 PRD 内容匹配最合适的模板。
     *
     * @return 匹配的模板实例化结果，无匹配返回 null
     */
    public PrdParseResult match(String content) {
        if (content == null || content.isBlank()) return null;
        String lower = content.toLowerCase();
        // 计分制：选命中关键词数最多的模板，且必须达到 MIN_HITS 才采用。
        // 避免“待审核”“上传”等界面文案/通用词单点命中就返回无关的预设数据。
        DomainTemplate best = null;
        int bestHits = 0;
        for (DomainTemplate tpl : templates) {
            int hits = tpl.countKeywordHits(lower);
            if (hits > bestHits) {
                bestHits = hits;
                best = tpl;
            }
        }
        if (best != null && bestHits >= DomainTemplate.MIN_HITS) {
            LOG.info("[DomainTemplateRegistry] 模板命中: name={}, hits={}/{}, keywords={}",
                    best.getName(), bestHits, best.getKeywords().size(), best.getKeywords());
            PrdParseResult result = best.instantiate();
            result.setParseSource("template");
            return result;
        }
        if (best != null) {
            LOG.info("[DomainTemplateRegistry] 模板未达命中阈值(单关键词误判风险): name={}, hits={}, 阈值={}, 跳过模板匹配",
                    best.getName(), bestHits, DomainTemplate.MIN_HITS);
        }
        return null;
    }

    // ========== 内置模板 ==========

    /** OAuth 第三方登录（GitHub / 微信 / Google 等） */
    private void registerOAuthLoginTemplate() {
        DomainTemplate tpl = new DomainTemplate();
        tpl.setName("OAuth 第三方登录");
        tpl.setKeywords(List.of("github 登录", "github 授权", "github oauth",
                "微信登录", "微信授权", "wechat login",
                "google 登录", "google 授权", "google oauth",
                "第三方登录", "oauth 登录", "授权登录",
                "sso 登录", "单点登录", "social login"));

        // 需求
        var req = new PrdParseResult.RequirementItem();
        req.setId("REQ-001");
        req.setDescription("用户可以通过第三方账号（GitHub/微信/Google）授权登录系统");
        req.setPriority("P1");
        tpl.getRequirements().add(req);

        var req2 = new PrdParseResult.RequirementItem();
        req2.setId("REQ-002");
        req2.setDescription("用户首次第三方登录时自动创建本地账号，绑定第三方身份");
        req2.setPriority("P1");
        tpl.getRequirements().add(req2);

        var req3 = new PrdParseResult.RequirementItem();
        req3.setId("REQ-003");
        req3.setDescription("未登录用户访问受保护资源时重定向到登录页面");
        req3.setPriority("P2");
        tpl.getRequirements().add(req3);

        // 数据实体
        var user = new PrdParseResult.EntityItem();
        user.setName("User");
        user.setDescription("系统用户");
        addAttr(user, "id", "Long", "用户唯一标识");
        addAttr(user, "username", "String", "用户名");
        addAttr(user, "avatarUrl", "String", "头像 URL");
        addAttr(user, "email", "String", "邮箱地址");
        addAttr(user, "status", "String", "账号状态: ACTIVE/DISABLED");
        addAttr(user, "createdAt", "DateTime", "创建时间");
        addAttr(user, "updatedAt", "DateTime", "更新时间");
        tpl.getDataEntities().add(user);

        var oauthAccount = new PrdParseResult.EntityItem();
        oauthAccount.setName("OAuthAccount");
        oauthAccount.setDescription("第三方账号绑定信息");
        addAttr(oauthAccount, "id", "Long", "唯一标识");
        addAttr(oauthAccount, "platform", "String", "第三方平台: GITHUB/WECHAT/GOOGLE");
        addAttr(oauthAccount, "platformUserId", "String", "第三方平台用户 ID");
        addAttr(oauthAccount, "accessToken", "String", "第三方 Access Token（加密存储）");
        addAttr(oauthAccount, "refreshToken", "String", "第三方 Refresh Token（加密存储）");
        addAttr(oauthAccount, "tokenExpiresAt", "DateTime", "Token 过期时间");
        addAttr(oauthAccount, "userId", "Long", "关联的系统用户 ID");
        addAttr(oauthAccount, "createdAt", "DateTime", "绑定时间");
        tpl.getDataEntities().add(oauthAccount);

        var rel = new PrdParseResult.EntityItem.RelationItem();
        rel.setTarget("User");
        rel.setType("ManyToOne");
        rel.setDescription("一个用户可绑定多个第三方账号");
        oauthAccount.getRelations().add(rel);

        // 接口
        tpl.getInterfaces().add(makeInterface("GET", "/api/v1/auth/{platform}/login",
                "跳转第三方授权页面", "", "{\"redirectUrl\": \"https://github.com/login/oauth/authorize?...\"}"));
        tpl.getInterfaces().add(makeInterface("GET", "/api/v1/auth/{platform}/callback",
                "第三方授权回调处理", "code=abc123&state=xyz", "{\"token\": \"jwt...\", \"user\": {...}}"));
        tpl.getInterfaces().add(makeInterface("POST", "/api/v1/auth/logout",
                "用户登出", "{}", "{\"message\": \"ok\"}"));

        // 架构决策
        tpl.getArchDecisions().add(makeArchDecision(
                "采用 OAuth 2.0 授权码模式",
                "第三方登录需要走标准 OAuth 2.0 流程",
                "使用授权码模式（Authorization Code Grant），后端完成 code → token 交换，前端只负责跳转",
                List.of("安全性高，access_token 不暴露给前端", "需要后端维护 token 刷新逻辑", "回调 URL 需在第三方平台注册")
        ));
        tpl.getArchDecisions().add(makeArchDecision(
                "使用 JWT 作为登录态凭证",
                "需要无状态、跨服务共享的登录态",
                "用户登录成功后发放 JWT，设置合理的过期时间（默认 24h），支持 refresh token 续期",
                List.of("无需服务端 session 存储", "JWT 可携带用户基础信息减少数据库查询", "需配置 JWT 密钥轮换机制")
        ));

        templates.add(tpl);
    }

    /** 支付模板 */
    private void registerPaymentTemplate() {
        DomainTemplate tpl = new DomainTemplate();
        tpl.setName("支付功能");
        tpl.setKeywords(List.of("支付", "付款", "下单", "订单", "购买", "充值", "支付回调", "退款"));

        var req = new PrdParseResult.RequirementItem();
        req.setId("REQ-001");
        req.setDescription("用户可以选择支付方式完成订单支付");
        req.setPriority("P1");
        tpl.getRequirements().add(req);

        var req2 = new PrdParseResult.RequirementItem();
        req2.setId("REQ-002");
        req2.setDescription("支付完成后系统异步接收支付回调并更新订单状态");
        req2.setPriority("P1");
        tpl.getRequirements().add(req2);

        var req3 = new PrdParseResult.RequirementItem();
        req3.setId("REQ-003");
        req3.setDescription("用户可查询订单支付状态和历史记录");
        req3.setPriority("P2");
        tpl.getRequirements().add(req3);

        // 实体
        var order = new PrdParseResult.EntityItem();
        order.setName("Order");
        order.setDescription("订单信息");
        addAttr(order, "id", "Long", "订单 ID");
        addAttr(order, "orderNo", "String", "订单编号");
        addAttr(order, "userId", "Long", "用户 ID");
        addAttr(order, "totalAmount", "BigDecimal", "订单总金额");
        addAttr(order, "status", "String", "订单状态: PENDING/PAID/REFUNDING/REFUNDED/CLOSED");
        addAttr(order, "payMethod", "String", "支付方式: WECHAT/ALIPAY");
        addAttr(order, "paidAt", "DateTime", "支付时间");
        addAttr(order, "createdAt", "DateTime", "创建时间");
        tpl.getDataEntities().add(order);

        var payment = new PrdParseResult.EntityItem();
        payment.setName("PaymentRecord");
        payment.setDescription("支付流水记录");
        addAttr(payment, "id", "Long", "流水 ID");
        addAttr(payment, "orderId", "Long", "关联订单 ID");
        addAttr(payment, "tradeNo", "String", "第三方支付交易号");
        addAttr(payment, "amount", "BigDecimal", "支付金额");
        addAttr(payment, "status", "String", "支付状态: SUCCESS/FAILED/REFUND");
        addAttr(payment, "payTime", "DateTime", "支付时间");
        tpl.getDataEntities().add(payment);

        // 接口
        tpl.getInterfaces().add(makeInterface("POST", "/api/v1/orders/create",
                "创建订单", "{\"productId\": 1, \"quantity\": 2}", "{\"orderNo\": \"ORD20260819001\", \"amount\": 99.00}"));
        tpl.getInterfaces().add(makeInterface("POST", "/api/v1/payments/pay",
                "发起支付", "{\"orderNo\": \"ORD20260819001\", \"payMethod\": \"WECHAT\"}", "{\"payUrl\": \"https://...\", \"tradeNo\": \"...\"}"));
        tpl.getInterfaces().add(makeInterface("POST", "/api/v1/payments/callback",
                "支付回调通知", "{\"tradeNo\": \"...\", \"status\": \"SUCCESS\"}", "{\"code\": \"SUCCESS\"}"));
        tpl.getInterfaces().add(makeInterface("GET", "/api/v1/orders/{orderNo}/status",
                "查询订单状态", "", "{\"orderNo\": \"...\", \"status\": \"PAID\"}"));
        tpl.getInterfaces().add(makeInterface("POST", "/api/v1/payments/refund",
                "申请退款", "{\"orderNo\": \"...\", \"amount\": 99.00}", "{\"refundNo\": \"...\", \"status\": \"REFUNDING\"}"));

        // 决策
        tpl.getArchDecisions().add(makeArchDecision(
                "对接第三方支付网关",
                "需要支持微信支付和支付宝",
                "统一封装支付网关接口，通过策略模式支持多支付渠道，支付回调使用异步通知+主动查询双保险",
                List.of("需在支付平台注册商户号并配置回调 URL", "回调通知需做签名验证防止伪造", "需设计对账机制处理掉单")
        ));

        templates.add(tpl);
    }

    /** 审批流程模板 */
    private void registerApprovalTemplate() {
        DomainTemplate tpl = new DomainTemplate();
        tpl.setName("审批流程");
        tpl.setKeywords(List.of("审批", "审核", "流程审批", "工作流", "approval", "workflow"));

        var req = new PrdParseResult.RequirementItem();
        req.setId("REQ-001");
        req.setDescription("用户可提交审批申请，指定审批人和审批流程");
        req.setPriority("P1");
        tpl.getRequirements().add(req);

        var req2 = new PrdParseResult.RequirementItem();
        req2.setId("REQ-002");
        req2.setDescription("审批人可查看待审批列表，执行通过/驳回操作");
        req2.setPriority("P1");
        tpl.getRequirements().add(req2);

        var req3 = new PrdParseResult.RequirementItem();
        req3.setId("REQ-003");
        req3.setDescription("审批流程支持多级审批，任一节点驳回则流程终止");
        req3.setPriority("P2");
        tpl.getRequirements().add(req3);

        // 实体
        var approval = new PrdParseResult.EntityItem();
        approval.setName("ApprovalRequest");
        approval.setDescription("审批申请单");
        addAttr(approval, "id", "Long", "申请 ID");
        addAttr(approval, "title", "String", "审批标题");
        addAttr(approval, "type", "String", "审批类型");
        addAttr(approval, "applicantId", "Long", "申请人 ID");
        addAttr(approval, "status", "String", "状态: PENDING/APPROVED/REJECTED/CANCELLED");
        addAttr(approval, "formData", "String", "表单数据（JSON）");
        addAttr(approval, "createdAt", "DateTime", "创建时间");
        tpl.getDataEntities().add(approval);

        var node = new PrdParseResult.EntityItem();
        node.setName("ApprovalNode");
        node.setDescription("审批节点记录");
        addAttr(node, "id", "Long", "节点 ID");
        addAttr(node, "approvalId", "Long", "关联审批单 ID");
        addAttr(node, "approverId", "Long", "审批人 ID");
        addAttr(node, "nodeOrder", "Integer", "节点顺序");
        addAttr(node, "status", "String", "节点状态: PENDING/APPROVED/REJECTED");
        addAttr(node, "comment", "String", "审批意见");
        addAttr(node, "operatedAt", "DateTime", "操作时间");
        tpl.getDataEntities().add(node);

        var rel = new PrdParseResult.EntityItem.RelationItem();
        rel.setTarget("ApprovalRequest");
        rel.setType("ManyToOne");
        rel.setDescription("一个审批单包含多个审批节点");
        node.getRelations().add(rel);

        // 接口
        tpl.getInterfaces().add(makeInterface("POST", "/api/v1/approvals/create",
                "提交审批申请", "{\"title\": \"...\", \"type\": \"...\", \"formData\": {...}}", "{\"approvalId\": 1, \"status\": \"PENDING\"}"));
        tpl.getInterfaces().add(makeInterface("GET", "/api/v1/approvals/pending",
                "获取待审批列表", "", "[{\"approvalId\": 1, \"title\": \"...\"}]"));
        tpl.getInterfaces().add(makeInterface("POST", "/api/v1/approvals/{id}/approve",
                "审批通过", "{\"comment\": \"同意\"}", "{\"status\": \"APPROVED\"}"));
        tpl.getInterfaces().add(makeInterface("POST", "/api/v1/approvals/{id}/reject",
                "审批驳回", "{\"comment\": \"驳回理由\"}", "{\"status\": \"REJECTED\"}"));
        tpl.getInterfaces().add(makeInterface("GET", "/api/v1/approvals/{id}/history",
                "查询审批历史", "", "[{\"approver\": \"...\", \"action\": \"APPROVED\"}]"));

        // 决策
        tpl.getArchDecisions().add(makeArchDecision(
                "使用责任链模式处理审批流程",
                "审批流程可能动态变化，需要灵活的节点编排",
                "采用责任链模式，每个审批节点是一个独立处理器，通过配置决定审批链顺序和分支条件",
                List.of("新增审批节点只需添加新的处理器类", "审批链配置可外部化，支持动态调整", "每个节点独立事务，失败不影响其他节点")
        ));

        templates.add(tpl);
    }

    /** 消息通知模板 */
    private void registerNotificationTemplate() {
        DomainTemplate tpl = new DomainTemplate();
        tpl.setName("消息通知");
        tpl.setKeywords(List.of("通知", "消息", "推送", "提醒", "notification", "push", "站内信"));

        var req = new PrdParseResult.RequirementItem();
        req.setId("REQ-001");
        req.setDescription("系统可向用户发送站内通知和消息");
        req.setPriority("P1");
        tpl.getRequirements().add(req);

        var req2 = new PrdParseResult.RequirementItem();
        req2.setId("REQ-002");
        req2.setDescription("用户可查看未读消息列表和消息历史");
        req2.setPriority("P1");
        tpl.getRequirements().add(req2);

        var req3 = new PrdParseResult.RequirementItem();
        req3.setId("REQ-003");
        req3.setDescription("用户可将消息标记为已读，支持批量操作");
        req3.setPriority("P2");
        tpl.getRequirements().add(req3);

        var notification = new PrdParseResult.EntityItem();
        notification.setName("Notification");
        notification.setDescription("系统通知消息");
        addAttr(notification, "id", "Long", "消息 ID");
        addAttr(notification, "userId", "Long", "接收用户 ID");
        addAttr(notification, "title", "String", "消息标题");
        addAttr(notification, "content", "String", "消息内容");
        addAttr(notification, "type", "String", "消息类型: SYSTEM/APPROVAL/ORDER");
        addAttr(notification, "isRead", "Boolean", "是否已读");
        addAttr(notification, "readAt", "DateTime", "阅读时间");
        addAttr(notification, "createdAt", "DateTime", "发送时间");
        tpl.getDataEntities().add(notification);

        tpl.getInterfaces().add(makeInterface("GET", "/api/v1/notifications/list",
                "获取消息列表", "?page=0&size=20", "{\"content\": [...], \"total\": 100}"));
        tpl.getInterfaces().add(makeInterface("GET", "/api/v1/notifications/unread-count",
                "获取未读消息数", "", "{\"count\": 5}"));
        tpl.getInterfaces().add(makeInterface("POST", "/api/v1/notifications/{id}/read",
                "标记已读", "", "{\"status\": \"ok\"}"));
        tpl.getInterfaces().add(makeInterface("POST", "/api/v1/notifications/read-all",
                "全部标记已读", "", "{\"status\": \"ok\"}"));

        tpl.getArchDecisions().add(makeArchDecision(
                "使用 WebSocket 实现实时推送",
                "用户需要实时收到新消息通知",
                "建立 WebSocket 连接推送实时消息，同时保留轮询接口作为降级方案",
                List.of("WebSocket 连接需做鉴权", "离线消息通过轮询接口补推", "消息持久化至数据库支持历史查询")
        ));

        templates.add(tpl);
    }

    /** 增删改查模板 */
    private void registerCrudTemplate() {
        DomainTemplate tpl = new DomainTemplate();
        tpl.setName("基础 CRUD");
        tpl.setKeywords(List.of("增删改查", "crud")); // 仅保留强信号词，避免"管理/新增/编辑/删除"等泛词误命中任意 PRD

        var req = new PrdParseResult.RequirementItem();
        req.setId("REQ-001");
        req.setDescription("支持对业务数据的新增、修改、删除、查询操作");
        req.setPriority("P1");
        tpl.getRequirements().add(req);

        var req2 = new PrdParseResult.RequirementItem();
        req2.setId("REQ-002");
        req2.setDescription("列表查询支持分页、排序、条件筛选");
        req2.setPriority("P1");
        tpl.getRequirements().add(req2);

        var entity = new PrdParseResult.EntityItem();
        entity.setName("BusinessEntity");
        entity.setDescription("业务实体");
        addAttr(entity, "id", "Long", "唯一标识");
        addAttr(entity, "name", "String", "名称");
        addAttr(entity, "status", "String", "状态: ENABLED/DISABLED");
        addAttr(entity, "sortOrder", "Integer", "排序号");
        addAttr(entity, "createdAt", "DateTime", "创建时间");
        addAttr(entity, "updatedAt", "DateTime", "更新时间");
        tpl.getDataEntities().add(entity);

        tpl.getInterfaces().add(makeInterface("GET", "/api/v1/entities/list",
                "分页查询列表", "?page=0&size=20&keyword=xxx", "{\"content\": [...], \"total\": 100}"));
        tpl.getInterfaces().add(makeInterface("GET", "/api/v1/entities/{id}",
                "查询详情", "", "{\"id\": 1, \"name\": \"...\"}"));
        tpl.getInterfaces().add(makeInterface("POST", "/api/v1/entities",
                "新增记录", "{\"name\": \"...\", \"status\": \"ENABLED\"}", "{\"id\": 1, \"name\": \"...\"}"));
        tpl.getInterfaces().add(makeInterface("PUT", "/api/v1/entities/{id}",
                "修改记录", "{\"name\": \"...\"}", "{\"id\": 1, \"name\": \"...\"}"));
        tpl.getInterfaces().add(makeInterface("DELETE", "/api/v1/entities/{id}",
                "删除记录", "", "{\"message\": \"ok\"}"));

        templates.add(tpl);
    }

    /** 文件上传模板 */
    private void registerFileUploadTemplate() {
        DomainTemplate tpl = new DomainTemplate();
        tpl.setName("文件上传");
        tpl.setKeywords(List.of("上传", "文件上传", "图片上传", "导入", "file upload")); // 去掉"附件"（通用章节标题，如"# 附件"）

        var req = new PrdParseResult.RequirementItem();
        req.setId("REQ-001");
        req.setDescription("用户可上传文件（图片/文档等），支持拖拽和选择文件两种方式");
        req.setPriority("P1");
        tpl.getRequirements().add(req);

        var req2 = new PrdParseResult.RequirementItem();
        req2.setId("REQ-002");
        req2.setDescription("上传的文件需进行类型和大小校验，防止恶意文件");
        req2.setPriority("P1");
        tpl.getRequirements().add(req2);

        var fileEntity = new PrdParseResult.EntityItem();
        fileEntity.setName("FileRecord");
        fileEntity.setDescription("文件记录");
        addAttr(fileEntity, "id", "Long", "文件 ID");
        addAttr(fileEntity, "fileName", "String", "原始文件名");
        addAttr(fileEntity, "fileSize", "Long", "文件大小（字节）");
        addAttr(fileEntity, "fileType", "String", "文件 MIME 类型");
        addAttr(fileEntity, "storagePath", "String", "存储路径");
        addAttr(fileEntity, "md5", "String", "文件 MD5 值");
        addAttr(fileEntity, "uploadedBy", "Long", "上传人 ID");
        addAttr(fileEntity, "createdAt", "DateTime", "上传时间");
        tpl.getDataEntities().add(fileEntity);

        tpl.getInterfaces().add(makeInterface("POST", "/api/v1/files/upload",
                "上传文件", "multipart/form-data", "{\"fileId\": 1, \"url\": \"https://...\"}"));
        tpl.getInterfaces().add(makeInterface("GET", "/api/v1/files/{id}/download",
                "下载文件", "", "binary"));
        tpl.getInterfaces().add(makeInterface("DELETE", "/api/v1/files/{id}",
                "删除文件", "", "{\"message\": \"ok\"}"));

        tpl.getArchDecisions().add(makeArchDecision(
                "使用 MinIO 作为文件存储",
                "需要高性能、可扩展的对象存储",
                "采用 MinIO 对象存储，文件上传后返回可访问 URL，支持分片上传大文件",
                List.of("MinIO 兼容 S3 API", "需要配置存储桶策略和访问权限", "大文件建议使用分片上传")
        ));

        templates.add(tpl);
    }

    /** 搜索功能模板 */
    private void registerSearchTemplate() {
        DomainTemplate tpl = new DomainTemplate();
        tpl.setName("搜索功能");
        tpl.setKeywords(List.of("搜索", "检索", "search", "全文检索", "模糊搜索")); // 去掉"查询"（通用词，任意 PRD 都可能出现）

        var req = new PrdParseResult.RequirementItem();
        req.setId("REQ-001");
        req.setDescription("用户可通过关键词搜索相关数据");
        req.setPriority("P1");
        tpl.getRequirements().add(req);

        var req2 = new PrdParseResult.RequirementItem();
        req2.setId("REQ-002");
        req2.setDescription("搜索结果支持分页、排序和高亮显示匹配内容");
        req2.setPriority("P2");
        tpl.getRequirements().add(req2);

        tpl.getInterfaces().add(makeInterface("GET", "/api/v1/search",
                "全文搜索", "?keyword=xxx&page=0&size=20", "{\"content\": [...], \"total\": 50, \"highlight\": {...}}"));
        tpl.getInterfaces().add(makeInterface("GET", "/api/v1/search/suggest",
                "搜索建议（自动补全）", "?keyword=xxx", "[\"suggest1\", \"suggest2\"]"));

        tpl.getArchDecisions().add(makeArchDecision(
                "使用 Elasticsearch 作为搜索引擎",
                "需要快速全文检索和复杂查询能力",
                "采用 Elasticsearch 存储可搜索数据，建立索引时配置合适的分词器（中文使用 IK 分词器）",
                List.of("需要维护索引与数据库的数据同步", "搜索性能远优于数据库 LIKE 查询", "支持高亮、排序、聚合等高级搜索功能")
        ));

        templates.add(tpl);
    }

    /** 数据导出模板 */
    private void registerDataExportTemplate() {
        DomainTemplate tpl = new DomainTemplate();
        tpl.setName("数据导出");
        tpl.setKeywords(List.of("导出", "报表", "报表导出", "excel 导出", "csv 导出", "export", "report"));

        var req = new PrdParseResult.RequirementItem();
        req.setId("REQ-001");
        req.setDescription("用户可按条件筛选数据并导出为 Excel/CSV 文件");
        req.setPriority("P1");
        tpl.getRequirements().add(req);

        var req2 = new PrdParseResult.RequirementItem();
        req2.setId("REQ-002");
        req2.setDescription("大数据量导出支持异步处理，导出完成后通知用户下载");
        req2.setPriority("P2");
        tpl.getRequirements().add(req2);

        tpl.getInterfaces().add(makeInterface("POST", "/api/v1/export",
                "发起导出任务", "{\"type\": \"ORDER\", \"filters\": {...}, \"format\": \"EXCEL\"}", "{\"exportId\": 1, \"status\": \"PROCESSING\"}"));
        tpl.getInterfaces().add(makeInterface("GET", "/api/v1/export/{id}/download",
                "下载导出文件", "", "binary"));
        tpl.getInterfaces().add(makeInterface("GET", "/api/v1/export/{id}/status",
                "查询导出进度", "", "{\"status\": \"COMPLETED\", \"fileUrl\": \"https://...\"}"));

        tpl.getArchDecisions().add(makeArchDecision(
                "异步导出 + 文件存储",
                "导出数据量大时不能阻塞用户操作",
                "导出任务异步处理，使用线程池 + 消息队列，完成后文件存储至 MinIO，用户通过链接下载",
                List.of("需限制导出频率防止资源耗尽", "导出文件设置过期时间自动清理", "大数据量导出建议使用流式写入避免 OOM")
        ));

        templates.add(tpl);
    }

    // ========== 工具方法 ==========

    private static void addAttr(PrdParseResult.EntityItem entity, String name, String type, String description) {
        var attr = new PrdParseResult.EntityItem.AttributeItem();
        attr.setName(name);
        attr.setType(type);
        attr.setDescription(description);
        entity.getAttributes().add(attr);
    }

    private static PrdParseResult.InterfaceItem makeInterface(String method, String path, String summary,
                                                               String requestBody, String responseBody) {
        var iface = new PrdParseResult.InterfaceItem();
        iface.setMethod(method);
        iface.setPath(path);
        iface.setSummary(summary);
        iface.setRequestBody(requestBody);
        iface.setResponseBody(responseBody);
        return iface;
    }

    private static PrdParseResult.ArchDecisionItem makeArchDecision(String title, String context,
                                                                     String decision, List<String> consequences) {
        var ad = new PrdParseResult.ArchDecisionItem();
        ad.setTitle(title);
        ad.setContext(context);
        ad.setDecision(decision);
        ad.setConsequences(consequences);
        return ad;
    }
}