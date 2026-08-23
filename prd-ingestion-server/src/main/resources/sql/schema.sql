-- =============================================================================
-- PRD 前置转换系统 DDL（MySQL 8.0+）
-- 数据库：harness_prd_ingestion
-- =============================================================================

CREATE DATABASE IF NOT EXISTS harness_prd_ingestion
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE harness_prd_ingestion;

-- =============================================================================
-- 0. 基础数据字典：语言、框架、项目
-- =============================================================================

-- 0.1 编程语言字典
CREATE TABLE IF NOT EXISTS prd_language (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    name        VARCHAR(64)    NOT NULL COMMENT '语言名称（如 Java、Python、Go）',
    slug        VARCHAR(32)    NOT NULL COMMENT '唯一标识符（如 java、python、go）',
    sort_order  INT            NOT NULL DEFAULT 0 COMMENT '排序序号',
    created_at  DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',

    UNIQUE INDEX uk_slug (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='编程语言字典表';

-- 0.2 框架字典
CREATE TABLE IF NOT EXISTS prd_framework (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    language_id BIGINT         NOT NULL COMMENT '所属语言 ID',
    name        VARCHAR(128)   NOT NULL COMMENT '框架名称（如 Spring Boot、Gin、FastAPI）',
    slug        VARCHAR(64)    NOT NULL COMMENT '唯一标识符（如 spring-boot、gin、fastapi）',
    sort_order  INT            NOT NULL DEFAULT 0 COMMENT '排序序号',
    created_at  DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',

    UNIQUE INDEX uk_slug (slug),
    INDEX idx_language_id (language_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='框架字典表，关联语言';

-- 0.3 项目表
CREATE TABLE IF NOT EXISTS prd_project (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    project_id      VARCHAR(64)    NOT NULL COMMENT '项目业务唯一标识（proj-xxx）',
    name            VARCHAR(256)   NOT NULL COMMENT '项目名称',
    description     TEXT           NULL COMMENT '项目描述',
    language_id     BIGINT         NOT NULL COMMENT '关联语言 ID',
    status          VARCHAR(32)    NOT NULL DEFAULT 'active' COMMENT '项目状态（active/archived）',
    created_at      DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at      DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',

    UNIQUE INDEX uk_project_id (project_id),
    INDEX idx_language_id (language_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='项目管理表，一个项目关联一种语言和多个框架';

-- 0.4 项目-框架关联表（多对多）
CREATE TABLE IF NOT EXISTS prd_project_framework (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    project_id      VARCHAR(64) NOT NULL COMMENT '项目 ID',
    framework_id    BIGINT      NOT NULL COMMENT '框架 ID',

    UNIQUE INDEX uk_project_framework (project_id, framework_id),
    INDEX idx_project_id (project_id),
    INDEX idx_framework_id (framework_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='项目与框架的多对多关联表';


-- 项目模板表（项目自定义模板，覆盖全局模板）
CREATE TABLE IF NOT EXISTS prd_project_template (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    project_id      VARCHAR(64)    NOT NULL COMMENT '所属项目 ID',
    template_id     VARCHAR(64)    NOT NULL COMMENT '模板业务 ID',
    type            VARCHAR(32)    NOT NULL COMMENT '模板类型（business-model/data-model/interface-protocol/architecture-decision）',
    name            VARCHAR(128)   NOT NULL COMMENT '模板名称',
    description     VARCHAR(512)   NULL COMMENT '模板描述',
    content         MEDIUMTEXT     NOT NULL COMMENT '模板 Markdown 内容（含 {{PLACEHOLDER}} 变量）',
    version         INT            NOT NULL DEFAULT 1 COMMENT '当前版本号',
    updated_by      VARCHAR(64)    NOT NULL DEFAULT 'system' COMMENT '最后更新人',
    created_at      DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at      DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',

    UNIQUE INDEX uk_project_template_id (project_id, template_id),
    INDEX idx_project_id (project_id),
    INDEX idx_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='项目自定义模板表，覆盖全局模板';

-- =============================================================================
-- 1. PRD 导入与解析
-- =============================================================================

-- 1.1 PRD 导入记录（核心表）
CREATE TABLE IF NOT EXISTS prd_ingestion (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    project_id      VARCHAR(64)    NOT NULL COMMENT '所属项目 ID',
    ingestion_id    VARCHAR(64)    NOT NULL COMMENT '业务唯一标识（ing-xxxx）',
    title           VARCHAR(256)   NOT NULL DEFAULT '' COMMENT 'PRD 标题',
    content         MEDIUMTEXT     NOT NULL COMMENT 'PRD 原始内容',
    content_file_md5 VARCHAR(64)   NULL COMMENT 'PRD 文件 MD5（从 MinIO 上传时使用）',
    format          VARCHAR(32)    NOT NULL DEFAULT 'markdown' COMMENT '文档格式（markdown/docx/pdf/txt/confluence）',
    source_url      VARCHAR(1024)  NULL COMMENT '来源 URL',
    status          VARCHAR(32)    NOT NULL DEFAULT 'pending' COMMENT '解析状态（pending/parsing/extracting/generating/completed/failed）',
    progress        INT            NOT NULL DEFAULT 0 COMMENT '解析进度 0-100',
    progress_msg    VARCHAR(512)   NULL COMMENT '进度消息',
    parse_source    VARCHAR(32)    NOT NULL DEFAULT 'keyword_fallback' COMMENT '解析来源（template/llm/keyword_fallback）',
    error_message   TEXT           NULL COMMENT '失败错误信息',
    created_at      DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at      DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',

    UNIQUE INDEX uk_ingestion_id (ingestion_id),
    INDEX idx_project_id (project_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='PRD 导入记录表：存储 PRD 文档原始内容与解析状态';

-- 旧表加列迁移（parse_source、source_paragraph 等）已改为 Java 启动迁移，
-- 见 com.huazai.prd.ingestion.config.SchemaMigrationRunner（Spring ScriptUtils 不执行 DELIMITER 存储过程）。
-- 1.2 需求实体
CREATE TABLE IF NOT EXISTS prd_requirement (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    project_id        VARCHAR(64)    NOT NULL COMMENT '所属项目 ID',
    ingestion_id      VARCHAR(64)    NOT NULL COMMENT '关联导入记录 ID',
    req_id            VARCHAR(64)    NOT NULL COMMENT '需求业务 ID（REQ-xxx）',
    description       TEXT           NOT NULL COMMENT '需求描述',
    priority          VARCHAR(8)     NOT NULL DEFAULT 'P2' COMMENT '优先级（P0/P1/P2/P3）',
    related_entities  VARCHAR(512)   NULL COMMENT '关联实体名列表（JSON 数组）',
    source_paragraph  TEXT           NULL COMMENT '原始 PRD 段落引用',
    notes             TEXT           NULL COMMENT '备注',
    sort_order        INT            NOT NULL DEFAULT 0 COMMENT '排序序号',
    created_at        DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at        DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',

    UNIQUE INDEX uk_req_id (req_id),
    INDEX idx_project_id (project_id),
    INDEX idx_ingestion_id (ingestion_id),
    INDEX idx_priority (priority)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='PRD 解析出的需求实体表';

-- 1.3 数据实体（领域对象）
CREATE TABLE IF NOT EXISTS prd_data_entity (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    project_id      VARCHAR(64)    NOT NULL COMMENT '所属项目 ID',
    ingestion_id    VARCHAR(64)    NOT NULL COMMENT '关联导入记录 ID',
    entity_name     VARCHAR(128)   NOT NULL COMMENT '实体名称',
    description     TEXT           NULL COMMENT '实体描述',
    source_paragraph TEXT          NULL COMMENT '来源 PRD 段落',
    sort_order      INT            NOT NULL DEFAULT 0 COMMENT '排序序号',
    created_at      DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at      DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',

    INDEX idx_project_id (project_id),
    INDEX idx_ingestion_id (ingestion_id),
    INDEX idx_entity_name (entity_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='PRD 解析出的数据实体表';

-- 1.4 数据实体属性
CREATE TABLE IF NOT EXISTS prd_data_entity_attribute (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    entity_id       BIGINT         NOT NULL COMMENT '关联实体 ID',
    attr_name       VARCHAR(128)   NOT NULL COMMENT '属性名',
    attr_type       VARCHAR(64)    NOT NULL COMMENT '属性类型',
    description     VARCHAR(512)   NULL COMMENT '属性描述',
    sort_order      INT            NOT NULL DEFAULT 0 COMMENT '排序序号',
    created_at      DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at      DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',

    INDEX idx_entity_id (entity_id),
    INDEX idx_attr_name (attr_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='数据实体属性表';

-- 1.5 数据实体关系
CREATE TABLE IF NOT EXISTS prd_data_entity_relation (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    entity_id       BIGINT         NOT NULL COMMENT '源实体 ID',
    target_entity   VARCHAR(128)   NOT NULL COMMENT '目标实体名',
    relation_type   VARCHAR(64)    NOT NULL COMMENT '关系类型（1:N, N:M, 1:1 等）',
    description     VARCHAR(512)   NULL COMMENT '关系描述',
    sort_order      INT            NOT NULL DEFAULT 0 COMMENT '排序序号',
    created_at      DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at      DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',

    INDEX idx_entity_id (entity_id),
    INDEX idx_target_entity (target_entity)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='数据实体间关系表';

-- 1.6 接口协议
CREATE TABLE IF NOT EXISTS prd_interface (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    project_id      VARCHAR(64)    NOT NULL COMMENT '所属项目 ID',
    ingestion_id    VARCHAR(64)    NOT NULL COMMENT '关联导入记录 ID',
    http_method     VARCHAR(16)    NOT NULL COMMENT 'HTTP 方法（GET/POST/PUT/DELETE/PATCH）',
    path            VARCHAR(512)   NOT NULL COMMENT '接口路径',
    summary         VARCHAR(512)   NOT NULL COMMENT '接口概要',
    request_body    TEXT           NULL COMMENT '请求体描述',
    response_body   TEXT           NULL COMMENT '响应体描述',
    notes           TEXT           NULL COMMENT '备注',
    source_paragraph TEXT          NULL COMMENT '来源 PRD 段落',
    sort_order      INT            NOT NULL DEFAULT 0 COMMENT '排序序号',
    created_at      DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at      DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',

    INDEX idx_project_id (project_id),
    INDEX idx_ingestion_id (ingestion_id),
    INDEX idx_path (path(255))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='PRD 解析出的接口协议表';

-- 1.7 架构决策
CREATE TABLE IF NOT EXISTS prd_architecture_decision (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    project_id      VARCHAR(64)    NOT NULL COMMENT '所属项目 ID',
    ingestion_id    VARCHAR(64)    NOT NULL COMMENT '关联导入记录 ID',
    ad_id           VARCHAR(64)    NOT NULL COMMENT '决策业务 ID（AD-xxx）',
    title           VARCHAR(256)   NOT NULL COMMENT '决策标题',
    context         TEXT           NULL COMMENT '决策背景',
    decision        TEXT           NOT NULL COMMENT '决策内容',
    consequences    TEXT           NULL COMMENT '后果（JSON 数组）',
    status          VARCHAR(32)    NOT NULL DEFAULT 'proposed' COMMENT '状态（proposed/accepted/deprecated/superseded）',
    source_paragraph TEXT          NULL COMMENT '来源 PRD 段落',
    sort_order      INT            NOT NULL DEFAULT 0 COMMENT '排序序号',
    created_at      DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at      DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',

    UNIQUE INDEX uk_ad_id (ad_id),
    INDEX idx_project_id (project_id),
    INDEX idx_ingestion_id (ingestion_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='PRD 解析出的架构决策表';

-- =============================================================================
-- 2. 文档管理
-- =============================================================================

CREATE TABLE IF NOT EXISTS prd_document (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    project_id          VARCHAR(64)    NOT NULL COMMENT '所属项目 ID',
    doc_id              VARCHAR(64)    NOT NULL COMMENT '文档业务 ID（doc-xxx）',
    ingestion_id        VARCHAR(64)    NOT NULL COMMENT '关联导入记录 ID',
    type                VARCHAR(32)    NOT NULL COMMENT '文档类型（business-model/data-model/interface-protocol/architecture-decision）',
    title               VARCHAR(256)   NOT NULL COMMENT '文档标题',
    status              VARCHAR(32)    NOT NULL DEFAULT 'draft' COMMENT '状态（draft/in_review/approved/rejected）',
    version             INT            NOT NULL DEFAULT 1 COMMENT '版本号',
    content             MEDIUMTEXT     NULL COMMENT '文档 Markdown 内容',
    original_prd_content TEXT          NULL COMMENT '原始 PRD 段落',
    change_log          VARCHAR(1024)  NULL COMMENT '变更日志',
    created_by          VARCHAR(64)    NOT NULL DEFAULT 'system' COMMENT '创建者',
    created_at          DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at          DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',

    UNIQUE INDEX uk_doc_id (doc_id),
    INDEX idx_project_id (project_id),
    INDEX idx_ingestion_id (ingestion_id),
    INDEX idx_type (type),
    INDEX idx_status (status),
    INDEX idx_updated_at (updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='PRD 生成的 Wiki 文档表';

-- 文档审批记录
CREATE TABLE IF NOT EXISTS prd_document_approval (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    project_id      VARCHAR(64)    NOT NULL COMMENT '所属项目 ID',
    doc_id          VARCHAR(64)    NOT NULL COMMENT '文档 ID',
    action          VARCHAR(16)    NOT NULL COMMENT '审批动作（approve/reject）',
    comment         TEXT           NULL COMMENT '审批意见',
    approved_by     VARCHAR(64)    NOT NULL DEFAULT 'system' COMMENT '审批人',
    created_at      DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',

    INDEX idx_project_id (project_id),
    INDEX idx_doc_id (doc_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='文档审批记录表';

-- =============================================================================
-- 3. Change 管理
-- =============================================================================

CREATE TABLE IF NOT EXISTS prd_change (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    project_id      VARCHAR(64)    NOT NULL COMMENT '所属项目 ID',
    change_id       VARCHAR(64)    NOT NULL COMMENT 'Change 业务 ID',
    title           VARCHAR(256)   NOT NULL COMMENT 'Change 标题',
    status          VARCHAR(32)    NOT NULL DEFAULT 'drafting' COMMENT '状态（drafting/reviewing/approved/coding/testing/verifying/completed）',
    content         MEDIUMTEXT     NULL COMMENT 'Change Markdown 内容',
    ingestion_id    VARCHAR(64)    NOT NULL COMMENT '关联导入记录 ID',
    requirement_ids TEXT           NULL COMMENT '关联需求 ID 列表（JSON 数组）',
    dependencies    TEXT           NULL COMMENT '依赖列表（JSON 数组）',
    created_at      DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at      DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',

    UNIQUE INDEX uk_change_id (change_id),
    INDEX idx_project_id (project_id),
    INDEX idx_ingestion_id (ingestion_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Change 记录表';

-- Change 文档引用
CREATE TABLE IF NOT EXISTS prd_change_document_ref (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    project_id      VARCHAR(64)    NOT NULL COMMENT '所属项目 ID',
    change_id       VARCHAR(64)    NOT NULL COMMENT 'Change ID',
    doc_id          VARCHAR(64)    NOT NULL COMMENT '文档 ID',
    doc_type        VARCHAR(32)    NOT NULL COMMENT '文档类型',
    doc_title       VARCHAR(256)   NOT NULL DEFAULT '' COMMENT '文档标题',
    doc_status      VARCHAR(32)    NOT NULL DEFAULT 'draft' COMMENT '文档状态',
    created_at      DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',

    INDEX idx_project_id (project_id),
    INDEX idx_change_id (change_id),
    INDEX idx_doc_id (doc_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Change 文档引用关系表';

-- Change 验收标准
CREATE TABLE IF NOT EXISTS prd_change_acceptance_criteria (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    project_id      VARCHAR(64)    NOT NULL COMMENT '所属项目 ID',
    change_id       VARCHAR(64)    NOT NULL COMMENT 'Change ID',
    ac_id           VARCHAR(64)    NOT NULL COMMENT '验收标准 ID',
    description     TEXT           NOT NULL COMMENT '验收标准描述',
    created_at      DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',

    INDEX idx_project_id (project_id),
    INDEX idx_change_id (change_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Change 验收标准表';

-- =============================================================================
-- 4. 流水线管理
-- =============================================================================

CREATE TABLE IF NOT EXISTS prd_pipeline_stage (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    project_id      VARCHAR(64)    NOT NULL COMMENT '所属项目 ID',
    change_id       VARCHAR(64)    NOT NULL COMMENT 'Change ID',
    stage           VARCHAR(32)    NOT NULL COMMENT '阶段（drafting/reviewing/approved/coding/testing/verifying/completed）',
    status          VARCHAR(32)    NOT NULL DEFAULT 'pending' COMMENT '状态（pending/active/completed/failed/skipped）',
    started_at      DATETIME(3)    NULL COMMENT '开始时间',
    completed_at    DATETIME(3)    NULL COMMENT '完成时间',
    created_at      DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at      DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',

    INDEX idx_project_id (project_id),
    INDEX idx_change_id (change_id),
    INDEX idx_stage (stage),
    UNIQUE INDEX uk_change_stage (change_id, stage)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='流水线阶段记录表';

-- 变更日志
CREATE TABLE IF NOT EXISTS prd_change_log (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    project_id      VARCHAR(64)    NOT NULL COMMENT '所属项目 ID',
    change_id       VARCHAR(64)    NOT NULL COMMENT 'Change ID',
    log_type        VARCHAR(32)    NOT NULL COMMENT '日志类型（status_change/approval/rejection/comment/commit/pr）',
    message         VARCHAR(1024)  NOT NULL COMMENT '日志消息',
    detail          TEXT           NULL COMMENT '日志详情',
    actor           VARCHAR(64)    NOT NULL DEFAULT 'system' COMMENT '操作人',
    created_at      DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',

    INDEX idx_project_id (project_id),
    INDEX idx_change_id (change_id),
    INDEX idx_log_type (log_type),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='流水线变更日志表';

-- =============================================================================
-- 5. 模板管理
-- =============================================================================

-- 模板表（全局模板，不绑定具体项目）
CREATE TABLE IF NOT EXISTS prd_template (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    template_id     VARCHAR(64)    NOT NULL COMMENT '模板业务 ID',
    type            VARCHAR(32)    NOT NULL COMMENT '模板类型（business-model/data-model/interface-protocol/architecture-decision）',
    name            VARCHAR(128)   NOT NULL COMMENT '模板名称',
    description     VARCHAR(512)   NULL COMMENT '模板描述',
    content         MEDIUMTEXT     NOT NULL COMMENT '模板 Markdown 内容（含 {{PLACEHOLDER}} 变量）',
    version         INT            NOT NULL DEFAULT 1 COMMENT '当前版本号',
    updated_by      VARCHAR(64)    NOT NULL DEFAULT 'system' COMMENT '最后更新人',
    created_at      DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at      DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',

    UNIQUE INDEX uk_template_id (template_id),
    INDEX idx_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='文档模板表（全局模板，不绑定具体项目）';

-- 模板版本历史
CREATE TABLE IF NOT EXISTS prd_template_version (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    project_id      VARCHAR(64)    NOT NULL COMMENT '所属项目 ID',
    template_id     VARCHAR(64)    NOT NULL COMMENT '模板 ID',
    version         INT            NOT NULL COMMENT '版本号',
    content         MEDIUMTEXT     NOT NULL COMMENT '模板内容快照',
    change_log      VARCHAR(1024)  NULL COMMENT '变更日志',
    updated_by      VARCHAR(64)    NOT NULL DEFAULT 'system' COMMENT '更新人',
    created_at      DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',

    INDEX idx_project_id (project_id),
    INDEX idx_template_id (template_id),
    UNIQUE INDEX uk_template_version (template_id, version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='模板版本历史表';

-- =============================================================================
-- 6. 需求看板
-- =============================================================================

-- 看板列定义
CREATE TABLE IF NOT EXISTS prd_kanban_list (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    project_id      VARCHAR(64)    NOT NULL COMMENT '所属项目 ID',
    list_key        VARCHAR(64)    NOT NULL COMMENT '列标识（prd_imported/document_review/design_review/coding/testing/completed）',
    list_label      VARCHAR(128)   NOT NULL COMMENT '列显示名称',
    sort_order      INT            NOT NULL DEFAULT 0 COMMENT '排序序号',
    created_at      DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',

    UNIQUE INDEX uk_project_list (project_id, list_key),
    INDEX idx_project_id (project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='看板列定义表';

-- 看板卡片
CREATE TABLE IF NOT EXISTS prd_kanban_card (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    project_id      VARCHAR(64)    NOT NULL COMMENT '所属项目 ID',
    card_id         VARCHAR(64)    NOT NULL COMMENT '卡片业务 ID',
    title           VARCHAR(256)   NOT NULL COMMENT '卡片标题',
    priority        VARCHAR(8)     NOT NULL DEFAULT 'P2' COMMENT '优先级（P0/P1/P2/P3）',
    list_key        VARCHAR(64)    NOT NULL COMMENT '所属列标识',
    assignee        VARCHAR(64)    NULL COMMENT '负责人',
    source_type     VARCHAR(32)    NOT NULL DEFAULT 'requirement' COMMENT '来源类型（requirement/change/task）',
    source_id       VARCHAR(64)    NULL COMMENT '来源业务 ID',
    description     TEXT           NULL COMMENT '卡片描述',
    sort_order      INT            NOT NULL DEFAULT 0 COMMENT '排序序号',
    created_at      DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at      DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',

    UNIQUE INDEX uk_card_id (card_id),
    INDEX idx_project_id (project_id),
    INDEX idx_list_key (list_key),
    INDEX idx_priority (priority)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='看板卡片表';

-- =============================================================================
-- 7. 文件上传（MinIO 分块上传）
-- =============================================================================

-- 7.1 文件元数据表
CREATE TABLE IF NOT EXISTS prd_file_metadata (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    project_id      VARCHAR(64)    NOT NULL COMMENT '所属项目 ID',
    file_md5        VARCHAR(64)    NOT NULL COMMENT '文件 MD5 唯一标识',
    file_name       VARCHAR(256)   NOT NULL COMMENT '原始文件名',
    file_size       BIGINT         NOT NULL DEFAULT 0 COMMENT '文件大小(字节)',
    total_chunks    INT            NOT NULL DEFAULT 0 COMMENT '总分片数量',
    content_type    VARCHAR(128)   NULL COMMENT '文件 MIME 类型',
    status          VARCHAR(32)    NOT NULL DEFAULT 'UPLOADING' COMMENT '上传状态: UPLOADING/MERGING/COMPLETED/FAILED',
    minio_object    VARCHAR(1024)  NULL COMMENT 'MinIO 合并后的对象路径',
    minio_url       VARCHAR(1024)  NULL COMMENT 'MinIO 预签名访问 URL',
    created_at      DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at      DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',

    UNIQUE INDEX uk_file_md5 (file_md5),
    INDEX idx_project_id (project_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='文件上传元数据表（MinIO 分块上传）';

-- 7.2 文件分片信息表
CREATE TABLE IF NOT EXISTS prd_file_chunks (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    project_id          VARCHAR(64)  NOT NULL COMMENT '所属项目 ID',
    file_md5            VARCHAR(64)  NOT NULL COMMENT '文件 MD5',
    file_chunk_md5      VARCHAR(64)  NOT NULL COMMENT '分片 MD5',
    file_chunk_index    INT          NOT NULL COMMENT '分片索引(0-based)',
    file_chunk_size     BIGINT       NOT NULL DEFAULT 0 COMMENT '分片大小(字节)',
    minio_object_name   VARCHAR(512) NOT NULL COMMENT 'MinIO 分片存储路径',
    created_at          DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at          DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',

    INDEX idx_project_id (project_id),
    INDEX idx_file_md5 (file_md5),
    INDEX idx_file_md5_index (file_md5, file_chunk_index)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='文件分片信息表（MinIO 分块上传）';

-- =============================================================================
-- 8. 用户权限系统
-- =============================================================================

-- 8.1 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_id         VARCHAR(64)    NOT NULL COMMENT '用户业务唯一标识（user-xxx）',
    username        VARCHAR(64)    NOT NULL COMMENT '登录用户名',
    password_hash   VARCHAR(256)   NOT NULL COMMENT '密码哈希（BCrypt）',
    display_name    VARCHAR(128)   NOT NULL DEFAULT '' COMMENT '显示名称',
    email           VARCHAR(256)   NULL COMMENT '邮箱',
    avatar_url      VARCHAR(512)   NULL COMMENT '头像 URL',
    status          VARCHAR(32)    NOT NULL DEFAULT 'active' COMMENT '状态（active/disabled）',
    created_at      DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at      DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',

    UNIQUE INDEX uk_user_id (user_id),
    UNIQUE INDEX uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='系统用户表';

-- 8.2 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    role_id         VARCHAR(64)    NOT NULL COMMENT '角色业务唯一标识',
    name            VARCHAR(64)    NOT NULL COMMENT '角色名称（如 ADMIN、PROJECT_MANAGER）',
    description     VARCHAR(256)   NULL COMMENT '角色描述',
    created_at      DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',

    UNIQUE INDEX uk_role_id (role_id),
    UNIQUE INDEX uk_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='角色表';

-- 8.3 用户-角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_id         VARCHAR(64)    NOT NULL COMMENT '用户 ID',
    role_id         VARCHAR(64)    NOT NULL COMMENT '角色 ID',

    UNIQUE INDEX uk_user_role (user_id, role_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='用户角色关联表';

-- 8.4 权限表
CREATE TABLE IF NOT EXISTS sys_permission (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    permission_id   VARCHAR(64)    NOT NULL COMMENT '权限业务唯一标识',
    name            VARCHAR(128)   NOT NULL COMMENT '权限名称',
    description     VARCHAR(256)   NULL COMMENT '权限描述',
    created_at      DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',

    UNIQUE INDEX uk_permission_id (permission_id),
    UNIQUE INDEX uk_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='权限表';

-- 8.5 角色-权限关联表
CREATE TABLE IF NOT EXISTS sys_role_permission (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    role_id         VARCHAR(64)    NOT NULL COMMENT '角色 ID',
    permission_id   VARCHAR(64)    NOT NULL COMMENT '权限 ID',

    UNIQUE INDEX uk_role_permission (role_id, permission_id),
    INDEX idx_role_id (role_id),
    INDEX idx_permission_id (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='角色权限关联表';

-- 8.6 项目成员表
CREATE TABLE IF NOT EXISTS sys_project_member (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    project_id      VARCHAR(64)    NOT NULL COMMENT '项目 ID',
    user_id         VARCHAR(64)    NOT NULL COMMENT '用户 ID',
    role            VARCHAR(32)    NOT NULL DEFAULT 'contributor' COMMENT '项目角色（owner/maintainer/contributor/reader）',
    created_at      DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at      DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',

    UNIQUE INDEX uk_project_user (project_id, user_id),
    INDEX idx_project_id (project_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='项目成员表，关联用户与项目的角色';

-- =============================================================================
-- 9. 初始化种子数据
-- =============================================================================

-- 9.1 默认角色
INSERT INTO sys_role (role_id, name, description) VALUES
('role-admin', 'ADMIN', '系统管理员，拥有所有权限'),
('role-pm', 'PROJECT_MANAGER', '项目经理，可管理项目和模板'),
('role-dev', 'DEVELOPER', '开发人员，可创建和更新变更'),
('role-viewer', 'VIEWER', '只读用户，仅可查看')
ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description);

-- 9.2 默认权限
INSERT INTO sys_permission (permission_id, name, description) VALUES
('perm-prd-create', 'prd:create', '创建/导入 PRD'),
('perm-prd-read', 'prd:read', '查看 PRD'),
('perm-prd-update', 'prd:update', '更新 PRD'),
('perm-prd-delete', 'prd:delete', '删除 PRD'),
('perm-change-create', 'change:create', '创建变更'),
('perm-change-read', 'change:read', '查看变更'),
('perm-change-update', 'change:update', '更新变更'),
('perm-change-approve', 'change:approve', '审批变更'),
('perm-document-read', 'document:read', '查看文档'),
('perm-document-update', 'document:update', '更新文档'),
('perm-document-approve', 'document:approve', '审批文档'),
('perm-project-create', 'project:create', '创建项目'),
('perm-project-read', 'project:read', '查看项目'),
('perm-project-update', 'project:update', '更新项目'),
('perm-project-delete', 'project:delete', '删除/归档项目'),
('perm-template-create', 'template:create', '创建模板'),
('perm-template-read', 'template:read', '查看模板'),
('perm-template-update', 'template:update', '更新模板'),
('perm-template-delete', 'template:delete', '删除模板'),
('perm-user-manage', 'user:manage', '用户管理')
ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description);

-- 9.3 角色-权限分配（ADMIN = 所有权限）
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 'role-admin', permission_id FROM sys_permission
ON DUPLICATE KEY UPDATE role_id = role_id;

-- PROJECT_MANAGER 权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 'role-pm', permission_id FROM sys_permission
WHERE name NOT IN ('user:manage', 'project:delete')
ON DUPLICATE KEY UPDATE role_id = role_id;

-- DEVELOPER 权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 'role-dev', permission_id FROM sys_permission
WHERE name IN ('prd:read', 'prd:create', 'change:read', 'change:create', 'change:update',
               'document:read', 'document:update', 'project:read')
ON DUPLICATE KEY UPDATE role_id = role_id;

-- VIEWER 权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 'role-viewer', permission_id FROM sys_permission
WHERE name IN ('prd:read', 'change:read', 'document:read', 'project:read', 'template:read')
ON DUPLICATE KEY UPDATE role_id = role_id;

-- 9.4 默认管理员用户（密码: admin123）
INSERT INTO sys_user (user_id, username, password_hash, display_name, email) VALUES
('user-admin', 'admin', '$2a$10$KOmVJgPk5PR4mHfunAVU9.mGr8QbRWSJWVIvOeIJDErkF.OKjVuci', '系统管理员', 'admin@huazai.com')
ON DUPLICATE KEY UPDATE password_hash = VALUES(password_hash), display_name = VALUES(display_name);

-- 9.5 默认管理员用户角色
INSERT INTO sys_user_role (user_id, role_id) VALUES ('user-admin', 'role-admin')
ON DUPLICATE KEY UPDATE user_id = user_id;

-- 9.6 将管理员加入所有已有项目（owner 角色），确保能管理历史数据
INSERT INTO sys_project_member (project_id, user_id, role)
SELECT p.project_id, 'user-admin', 'owner'
FROM prd_project p
WHERE NOT EXISTS (
    SELECT 1 FROM sys_project_member pm
    WHERE pm.project_id = p.project_id AND pm.user_id = 'user-admin'
);

-- =============================================================================
-- 10. 初始化种子数据（原有数据）
-- =============================================================================

-- 8.1 编程语言
INSERT INTO prd_language (name, slug, sort_order) VALUES
('Java', 'java', 1),
('Python', 'python', 2),
('Go', 'go', 3),
('Rust', 'rust', 4),
('PHP', 'php', 5)
ON DUPLICATE KEY UPDATE name = VALUES(name), sort_order = VALUES(sort_order);

-- 8.2 框架（Java）
INSERT INTO prd_framework (language_id, name, slug, sort_order) VALUES
((SELECT id FROM prd_language WHERE slug = 'java'), 'Spring Boot', 'spring-boot', 1),
((SELECT id FROM prd_language WHERE slug = 'java'), 'Spring MVC', 'spring-mvc', 2),
((SELECT id FROM prd_language WHERE slug = 'java'), 'Spring Cloud Alibaba', 'spring-cloud-alibaba', 3),
((SELECT id FROM prd_language WHERE slug = 'java'), 'Spring AI', 'spring-ai', 4),
((SELECT id FROM prd_language WHERE slug = 'java'), 'Spring AI Alibaba', 'spring-ai-alibaba', 5),
((SELECT id FROM prd_language WHERE slug = 'java'), 'Quarkus', 'quarkus', 6),
((SELECT id FROM prd_language WHERE slug = 'java'), 'Micronaut', 'micronaut', 7),
((SELECT id FROM prd_language WHERE slug = 'java'), 'Dubbo', 'dubbo', 8),
((SELECT id FROM prd_language WHERE slug = 'java'), 'Vert.x', 'vertx', 9),
((SELECT id FROM prd_language WHERE slug = 'java'), 'Dropwizard', 'dropwizard', 10),
((SELECT id FROM prd_language WHERE slug = 'java'), 'LangChain4j', 'langchain4j', 11),
((SELECT id FROM prd_language WHERE slug = 'java'), 'AgentScope Java', 'agentscope-java', 12),
((SELECT id FROM prd_language WHERE slug = 'java'), 'Semantic Kernel', 'semantic-kernel-java', 13),
((SELECT id FROM prd_language WHERE slug = 'java'), 'Genkit Java', 'genkit-java', 14),
((SELECT id FROM prd_language WHERE slug = 'java'), '其他', 'java-other', 99)
ON DUPLICATE KEY UPDATE name = VALUES(name), sort_order = VALUES(sort_order);

-- 8.3 框架（Python）
INSERT INTO prd_framework (language_id, name, slug, sort_order) VALUES
((SELECT id FROM prd_language WHERE slug = 'python'), 'Django', 'django', 1),
((SELECT id FROM prd_language WHERE slug = 'python'), 'FastAPI', 'fastapi', 2),
((SELECT id FROM prd_language WHERE slug = 'python'), 'Flask', 'flask', 3),
((SELECT id FROM prd_language WHERE slug = 'python'), 'Tornado', 'tornado', 4),
((SELECT id FROM prd_language WHERE slug = 'python'), 'LangChain', 'langchain', 5),
((SELECT id FROM prd_language WHERE slug = 'python'), 'LangGraph', 'langgraph', 6),
((SELECT id FROM prd_language WHERE slug = 'python'), 'CrewAI', 'crewai', 7),
((SELECT id FROM prd_language WHERE slug = 'python'), 'PydanticAI', 'pydantic-ai', 8),
((SELECT id FROM prd_language WHERE slug = 'python'), 'SmolAgents', 'smolagents', 9),
((SELECT id FROM prd_language WHERE slug = 'python'), 'OpenAI Agents SDK', 'openai-agents-sdk', 10),
((SELECT id FROM prd_language WHERE slug = 'python'), 'TensorFlow', 'tensorflow', 11),
((SELECT id FROM prd_language WHERE slug = 'python'), 'PyTorch', 'pytorch', 12),
((SELECT id FROM prd_language WHERE slug = 'python'), 'Keras', 'keras', 13),
((SELECT id FROM prd_language WHERE slug = 'python'), 'scikit-learn', 'scikit-learn', 14),
((SELECT id FROM prd_language WHERE slug = 'python'), 'XGBoost', 'xgboost', 15),
((SELECT id FROM prd_language WHERE slug = 'python'), 'Hugging Face Transformers', 'huggingface-transformers', 16),
((SELECT id FROM prd_language WHERE slug = 'python'), '其他', 'python-other', 99)
ON DUPLICATE KEY UPDATE name = VALUES(name), sort_order = VALUES(sort_order);

-- 8.4 框架（Go）
INSERT INTO prd_framework (language_id, name, slug, sort_order) VALUES
((SELECT id FROM prd_language WHERE slug = 'go'), 'Gin', 'gin', 1),
((SELECT id FROM prd_language WHERE slug = 'go'), 'Echo', 'echo', 2),
((SELECT id FROM prd_language WHERE slug = 'go'), 'Fiber', 'fiber', 3),
((SELECT id FROM prd_language WHERE slug = 'go'), 'Go-Zero', 'go-zero', 4),
((SELECT id FROM prd_language WHERE slug = 'go'), 'Kratos', 'kratos', 5),
((SELECT id FROM prd_language WHERE slug = 'go'), 'Hertz', 'hertz', 6),
((SELECT id FROM prd_language WHERE slug = 'go'), 'Go Kit', 'go-kit', 7),
((SELECT id FROM prd_language WHERE slug = 'go'), 'Chi', 'chi', 8),
((SELECT id FROM prd_language WHERE slug = 'go'), 'Beego', 'beego', 9),
((SELECT id FROM prd_language WHERE slug = 'go'), 'Gorilla Mux', 'gorilla-mux', 10),
((SELECT id FROM prd_language WHERE slug = 'go'), 'Kitex', 'kitex', 11),
((SELECT id FROM prd_language WHERE slug = 'go'), 'Iris', 'iris', 12),
((SELECT id FROM prd_language WHERE slug = 'go'), 'Macaron', 'macaron', 13),
((SELECT id FROM prd_language WHERE slug = 'go'), 'Tango', 'tango', 14),
((SELECT id FROM prd_language WHERE slug = 'go'), 'GoFrame', 'goframe', 15),
((SELECT id FROM prd_language WHERE slug = 'go'), 'LangChainGo', 'langchaingo', 16),
((SELECT id FROM prd_language WHERE slug = 'go'), 'Google ADK-Go', 'google-adk-go', 17),
((SELECT id FROM prd_language WHERE slug = 'go'), 'Eino (CloudWeGo)', 'eino', 18),
((SELECT id FROM prd_language WHERE slug = 'go'), 'tRPC-Agent-Go', 'trpc-agent-go', 19),
((SELECT id FROM prd_language WHERE slug = 'go'), 'Firebase Genkit', 'firebase-genkit', 20),
((SELECT id FROM prd_language WHERE slug = 'go'), 'Anyi', 'anyi', 21),
((SELECT id FROM prd_language WHERE slug = 'go'), '其他', 'go-other', 99)
ON DUPLICATE KEY UPDATE name = VALUES(name), sort_order = VALUES(sort_order);

-- 8.5 框架（Rust）
INSERT INTO prd_framework (language_id, name, slug, sort_order) VALUES
((SELECT id FROM prd_language WHERE slug = 'rust'), 'Axum', 'axum', 1),
((SELECT id FROM prd_language WHERE slug = 'rust'), 'Actix Web', 'actix-web', 2),
((SELECT id FROM prd_language WHERE slug = 'rust'), 'Rocket', 'rocket', 3),
((SELECT id FROM prd_language WHERE slug = 'rust'), 'Warp', 'warp', 4),
((SELECT id FROM prd_language WHERE slug = 'rust'), 'Poem', 'poem', 5),
((SELECT id FROM prd_language WHERE slug = 'rust'), 'Loco', 'loco', 6),
((SELECT id FROM prd_language WHERE slug = 'rust'), 'Salvo', 'salvo', 7),
((SELECT id FROM prd_language WHERE slug = 'rust'), 'Tauri', 'tauri', 8),
((SELECT id FROM prd_language WHERE slug = 'rust'), 'Iced', 'iced', 9),
((SELECT id FROM prd_language WHERE slug = 'rust'), 'egui', 'egui', 10),
((SELECT id FROM prd_language WHERE slug = 'rust'), 'Dioxus', 'dioxus', 11),
((SELECT id FROM prd_language WHERE slug = 'rust'), 'Candle', 'candle', 12),
((SELECT id FROM prd_language WHERE slug = 'rust'), 'Burn', 'burn', 13),
((SELECT id FROM prd_language WHERE slug = 'rust'), 'tch-rs', 'tch-rs', 14),
((SELECT id FROM prd_language WHERE slug = 'rust'), 'ADK-Rust', 'adk-rust', 15),
((SELECT id FROM prd_language WHERE slug = 'rust'), 'Blockcell', 'blockcell', 16),
((SELECT id FROM prd_language WHERE slug = 'rust'), 'vLLM (Rust)', 'vllm-rust', 17),
((SELECT id FROM prd_language WHERE slug = 'rust'), '其他', 'rust-other', 99)
ON DUPLICATE KEY UPDATE name = VALUES(name), sort_order = VALUES(sort_order);

-- 8.6 框架（PHP）
INSERT INTO prd_framework (language_id, name, slug, sort_order) VALUES
((SELECT id FROM prd_language WHERE slug = 'php'), 'Laravel', 'laravel', 1),
((SELECT id FROM prd_language WHERE slug = 'php'), 'Laravel AI SDK', 'laravel-ai-sdk', 2),
((SELECT id FROM prd_language WHERE slug = 'php'), 'ThinkPHP', 'thinkphp', 3),
((SELECT id FROM prd_language WHERE slug = 'php'), 'Hyperf', 'hyperf', 4),
((SELECT id FROM prd_language WHERE slug = 'php'), 'Symfony', 'symfony', 5),
((SELECT id FROM prd_language WHERE slug = 'php'), 'Yii 2', 'yii2', 6),
((SELECT id FROM prd_language WHERE slug = 'php'), 'Yii 3', 'yii3', 7),
((SELECT id FROM prd_language WHERE slug = 'php'), 'Workerman', 'workerman', 8),
((SELECT id FROM prd_language WHERE slug = 'php'), 'Webman', 'webman', 9),
((SELECT id FROM prd_language WHERE slug = 'php'), 'CodeIgniter 4', 'codeigniter4', 10),
((SELECT id FROM prd_language WHERE slug = 'php'), 'Slim', 'slim', 11),
((SELECT id FROM prd_language WHERE slug = 'php'), 'Phalcon', 'phalcon', 12),
((SELECT id FROM prd_language WHERE slug = 'php'), 'Yaf', 'yaf', 13),
((SELECT id FROM prd_language WHERE slug = 'php'), 'CakePHP', 'cakephp', 14),
((SELECT id FROM prd_language WHERE slug = 'php'), 'Craft CMS', 'craft-cms', 15),
((SELECT id FROM prd_language WHERE slug = 'php'), 'October CMS', 'october-cms', 16),
((SELECT id FROM prd_language WHERE slug = 'php'), 'OpenCart', 'opencart', 17),
((SELECT id FROM prd_language WHERE slug = 'php'), 'GravCMS', 'gravcms', 18),
((SELECT id FROM prd_language WHERE slug = 'php'), 'Symfony2', 'symfony2', 19),
((SELECT id FROM prd_language WHERE slug = 'php'), 'Swoole', 'swoole', 20),
((SELECT id FROM prd_language WHERE slug = 'php'), 'Drupal', 'drupal', 21),
((SELECT id FROM prd_language WHERE slug = 'php'), 'WordPress', 'wordpress', 22),
((SELECT id FROM prd_language WHERE slug = 'php'), 'WooCommerce', 'woocommerce', 23),
((SELECT id FROM prd_language WHERE slug = 'php'), 'Joomla', 'joomla', 24),
((SELECT id FROM prd_language WHERE slug = 'php'), 'PHP CMS (phpcms)', 'phpcms', 25),
((SELECT id FROM prd_language WHERE slug = 'php'), 'DedeCMS', 'dedecms', 26),
((SELECT id FROM prd_language WHERE slug = 'php'), 'Discuz', 'discuz', 27),
((SELECT id FROM prd_language WHERE slug = 'php'), 'Neuron AI (PHP)', 'neuron-ai-php', 28),
((SELECT id FROM prd_language WHERE slug = 'php'), 'LLPhant', 'llphant', 29),
((SELECT id FROM prd_language WHERE slug = 'php'), 'Prism (PHP)', 'prism-php', 30),
((SELECT id FROM prd_language WHERE slug = 'php'), 'PocketFlow PHP', 'pocketflow-php', 31),
((SELECT id FROM prd_language WHERE slug = 'php'), 'Cognesy Instructor PHP', 'cognesy-instructor-php', 32),
((SELECT id FROM prd_language WHERE slug = 'php'), 'Papiai', 'papiai', 33),
((SELECT id FROM prd_language WHERE slug = 'php'), '其他', 'php-other', 99)
ON DUPLICATE KEY UPDATE name = VALUES(name), sort_order = VALUES(sort_order);


INSERT INTO prd_template (`template_id`, `type`, `name`, `description`, `content`, `version`, `updated_by`, `created_at`, `updated_at`)
VALUES
    ('tpl-20260817T1558154673681798122', 'business-model', '业务模型', '', '# 业务模型\n\n> 本项目的业务全景、核心实体关系、领域边界。\n> 领域术语的定义应以 `.harness/CONTEXT.md` 为准，此处只描述业务结构和关系。\n\n---\n\n## 业务全景\n\n<描述项目的核心业务和价值主张。一两段话，让新加入的开发者能快速理解业务逻辑。>\n\n## 核心实体关系\n\n```\n┌──────────┐       ┌──────────┐\n│  实体A   │──────▶│  实体B   │\n│          │       │          │\n└──────────┘       └──────────┘\n```\n\n| 实体 | 业务含义 | 关系 |\n|------|---------|------|\n| <实体名> | <是什么> | 一对多 / 依赖 / 聚合 |\n| <实体名> | <是什么> | — |\n\n## 领域边界\n\n<明确各模块/服务的职责边界。什么归谁管，什么不归谁管。>\n\n## 业务规则\n\n- 规则 1：<描述>\n- 规则 2：<描述>', 1, 'system', '2026-08-17 23:50:48.930', '2026-08-17 23:50:48.930'),
    ('tpl-20260818T072818156101Z758743', 'data-model', '数据模型', '', '# 数据模型\n\n> 本项目的核心数据模型定义、存储方案。\n> 请根据项目实际数据模型补充完善。\n\n## 核心模型\n| 模型 | 说明 | 存储 |\n|------|------|------|\n| <模型名> | <描述> | <MySQL/Redis/…> |\n\n## 存储方案\n<描述整体存储架构：关系型数据库、缓存、搜索引擎等>', 1, 'system', '2026-08-18 15:28:18.165', '2026-08-18 15:28:18.165'),
    ('tpl-20260818T072834509692600Z683', 'interface-protocol', '接口协议', '', '# 接口协议\n\n> 本项目的对外 API 定义、模块间通信协议。\n> 请根据项目实际接口设计补充完善。\n\n## 对外接口\n| 端点 | 方法 | 说明 |\n|------|------|------|\n| <路径> | <GET/POST/…> | <描述> |\n\n## 模块间通信协议\n<描述模块间通信方式：REST / gRPC / 消息队列 / A2A 等>', 1, 'system', '2026-08-18 15:28:34.513', '2026-08-18 15:28:34.513'),
    ('tpl-20260818T072849704441700Z473', 'architecture-decision', '架构决策', '', '# 架构决策\n\n> 本文件记录项目的架构决策记录（ADR）。\n> 仅当同时满足三个条件才创建 ADR：**难逆转** / **脱离上下文会惊讶** / **有真实权衡**。\n> 见 `.harness/wiki/ADR-FORMAT.md` 了解完整规范。\n\n---\n\n## ADR 列表\n\n| 编号 | 标题 | 状态 |\n|------|------|------|\n| ADR-001 | <标题> | accepted |\n\n---\n\n## ADR-001: <标题>\n\n{1-3 句话：上下文是什么、做了什么决定、为什么。}\n\n> 可选：Status / Considered Options / Consequences 见 ADR-FORMAT.md', 1, 'system', '2026-08-18 15:28:49.711', '2026-08-18 15:28:49.711'),
    ('tpl-20260818T074039331660300Z890', 'change-model', 'change 开发清单', '', '---\nid: C-NNN\nslug: <slug>\nstatus: analyzing\ncreated: <date>\n---\n\n# C-NNN <标题>\n\n## 用户故事\n作为 <角色>，我想要 <功能>，以便 <价值>。\n\n## 非目标（Out of Scope）\n- …\n\n## 验收标准\n- AC-1: …\n- AC-2: …\n- AC-3: …\n\n## 边界情况\n- 当 … 时，系统应 …\n- 当 … 时，系统应 …\n- 当 … 时，系统应 …\n\n## 非功能需求\n| 维度 | 指标 |\n|------|------|\n| 性能 | … |\n| 可靠性 | … |\n| 安全 | … |\n\n## 设计约束\n- …\n\n## 契约影响\n- REST: …\n- 模块间通信: …\n- 数据模型: …\n\n## 影响面\n- 模块/服务: …\n- 外部 API: …\n- wiki: …\n\n## 测试策略\n- 先写失败测试: …\n- 边界测试: …\n- 降级测试: …', 1, 'system', '2026-08-18 15:40:39.339', '2026-08-18 15:40:39.339')
ON DUPLICATE KEY UPDATE template_id = template_id;

-- 8.8 默认看板列（每个项目创建时需初始化）
-- 注意：项目创建时由后端代码初始化，此处仅作参考
-- INSERT INTO prd_kanban_list (project_id, list_key, list_label, sort_order) VALUES
-- ('proj-demo', 'prd_imported', 'PRD 已导入', 1),
-- ('proj-demo', 'document_review', '文档评审', 2),
-- ('proj-demo', 'design_review', '设计评审', 3),
-- ('proj-demo', 'coding', '开发中', 4),
-- ('proj-demo', 'testing', '测试中', 5),
-- ('proj-demo', 'completed', '已完成', 6);

-- LLM 解析结果旁路存储（仅用于前端左侧"LLM 提取结果"展示，不参与 4 份文档渲染）
CREATE TABLE IF NOT EXISTS prd_llm_result (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ingestion_id VARCHAR(64) NOT NULL,
    payload_json MEDIUMTEXT NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    UNIQUE INDEX uk_llm_ingestion (ingestion_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
