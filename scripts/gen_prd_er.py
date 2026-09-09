# -*- coding: utf-8 -*-
"""prd-ingestion ER 图生成器：字段含 schema.sql COMMENT 简要说明。"""
import html
W, H = 2600, 3400
BW, ROWH, HDR, PAD = 440, 18, 40, 10
T = {}
def tab(name, x, y, *cols):
    T[name] = (x, y, list(cols))

tab("prd_language", 60, 200,
    ("id", "PK", "主键"), ("name", "", "语言名称"), ("slug", "UK", "唯一标识(java等)"),
    ("sort_order", "", "排序序号"), ("created_at", "", "创建时间"))
tab("prd_framework", 580, 200,
    ("id", "PK", "主键"), ("language_id", "FK", "所属语言ID"), ("name", "", "框架名称"),
    ("slug", "UK", "唯一标识"), ("sort_order", "", "排序序号"), ("created_at", "", "创建时间"))
tab("prd_project", 1100, 200,
    ("id", "PK", "主键"), ("project_id", "UK", "项目业务ID(proj-xxx)"), ("name", "", "项目名称"),
    ("description", "", "项目描述"), ("language_id", "FK", "关联语言ID"), ("status", "", "active/archived"),
    ("created_at", "", "创建时间"), ("updated_at", "", "更新时间"))

tab("prd_project_framework", 60, 420,
    ("id", "PK", "主键"), ("project_id", "FK", "项目ID"), ("framework_id", "FK", "框架ID"))
tab("prd_project_template", 580, 420,
    ("id", "PK", "主键"), ("project_id", "FK", "所属项目ID"), ("template_id", "UK", "模板业务ID"),
    ("type", "", "模板类型(4类)"), ("name", "", "模板名称"), ("content", "", "模板Markdown内容"),
    ("version", "", "当前版本号"), ("updated_by", "", "最后更新人"))
tab("prd_ingestion", 60, 660,
    ("id", "PK", "主键"), ("project_id", "FK", "所属项目ID"), ("ingestion_id", "UK", "业务ID(ing-xxx)"),
    ("title", "", "PRD标题"), ("content", "", "PRD原始内容"), ("content_file_md5", "", "文件MD5"),
    ("format", "", "文档格式"), ("status", "", "解析状态"), ("progress", "", "解析进度0-100"),
    ("progress_msg", "", "进度消息"), ("parse_source", "", "解析来源"), ("error_message", "", "失败错误信息"),
    ("created_at", "", "创建时间"), ("updated_at", "", "更新时间"))

tab("prd_llm_result", 60, 990,
    ("id", "PK", "主键"), ("ingestion_id", "UK,FK", "关联导入ID"), ("payload_json", "", "LLM结果JSON"))
tab("prd_requirement", 580, 660,
    ("id", "PK", "主键"), ("project_id", "", "所属项目ID"), ("ingestion_id", "FK", "关联导入ID"),
    ("req_id", "UK", "需求ID(REQ-xxx)"), ("description", "", "需求描述"), ("priority", "", "优先级P0-P3"),
    ("related_entities", "", "关联实体列表"), ("source_paragraph", "", "来源PRD段落"), ("sort_order", "", "排序序号"))
tab("prd_data_entity", 1100, 660,
    ("id", "PK", "主键"), ("project_id", "", "所属项目ID"), ("ingestion_id", "FK", "关联导入ID"),
    ("entity_name", "", "实体名称"), ("description", "", "实体描述"), ("source_paragraph", "", "来源PRD段落"),
    ("sort_order", "", "排序序号"), ("created_at", "", "创建时间"), ("updated_at", "", "更新时间"))

tab("prd_data_entity_attribute", 1100, 890,
    ("id", "PK", "主键"), ("entity_id", "FK", "关联实体ID"), ("attr_name", "", "属性名"),
    ("attr_type", "", "属性类型"), ("description", "", "属性描述"), ("sort_order", "", "排序序号"),
    ("created_at", "", "创建时间"), ("updated_at", "", "更新时间"))
tab("prd_data_entity_relation", 1100, 1090,
    ("id", "PK", "主键"), ("entity_id", "FK", "源实体ID"), ("target_entity", "", "目标实体名"),
    ("relation_type", "", "关系类型(1:N等)"), ("description", "", "关系描述"), ("sort_order", "", "排序序号"),
    ("created_at", "", "创建时间"), ("updated_at", "", "更新时间"))
tab("prd_interface", 580, 940,
    ("id", "PK", "主键"), ("project_id", "", "所属项目ID"), ("ingestion_id", "FK", "关联导入ID"),
    ("http_method", "", "HTTP方法"), ("path", "", "接口路径"), ("summary", "", "接口概要"),
    ("request_body", "", "请求体描述"), ("response_body", "", "响应体描述"),
    ("source_paragraph", "", "来源PRD段落"), ("sort_order", "", "排序序号"),
    ("created_at", "", "创建时间"), ("updated_at", "", "更新时间"))

tab("prd_architecture_decision", 1100, 1300,
    ("id", "PK", "主键"), ("project_id", "", "所属项目ID"), ("ingestion_id", "FK", "关联导入ID"),
    ("ad_id", "UK", "决策ID(AD-xxx)"), ("title", "", "决策标题"), ("context", "", "决策背景"),
    ("decision", "", "决策内容"), ("consequences", "", "后果(JSON数组)"), ("status", "", "决策状态"),
    ("source_paragraph", "", "来源PRD段落"), ("created_at", "", "创建时间"), ("updated_at", "", "更新时间"))
tab("prd_document", 60, 1600,
    ("id", "PK", "主键"), ("project_id", "", "所属项目ID"), ("doc_id", "UK", "文档业务ID(doc-xxx)"),
    ("ingestion_id", "FK", "关联导入ID"), ("type", "", "文档类型(4类)"), ("title", "", "文档标题"),
    ("status", "", "draft/approved等"), ("version", "", "版本号"), ("content", "", "文档Markdown"),
    ("original_prd_content", "", "原始PRD段落"), ("created_by", "", "创建者"),
    ("created_at", "", "创建时间"), ("updated_at", "", "更新时间"))

tab("prd_document_approval", 580, 1600,
    ("id", "PK", "主键"), ("project_id", "", "所属项目ID"), ("doc_id", "FK", "文档ID"),
    ("action", "", "approve/reject"), ("comment", "", "审批意见"), ("approved_by", "", "审批人"),
    ("created_at", "", "创建时间"))
tab("prd_change", 60, 1910,
    ("id", "PK", "主键"), ("project_id", "", "所属项目ID"), ("change_id", "UK", "Change业务ID"),
    ("title", "", "Change标题"), ("status", "", "状态(drafting等)"), ("content", "", "Change Markdown"),
    ("ingestion_id", "FK", "关联导入ID"), ("requirement_ids", "", "需求ID列表JSON"), ("dependencies", "", "依赖列表JSON"),
    ("created_at", "", "创建时间"), ("updated_at", "", "更新时间"))
tab("prd_change_document_ref", 580, 1910,
    ("id", "PK", "主键"), ("project_id", "", "所属项目ID"), ("change_id", "FK", "ChangeID"),
    ("doc_id", "FK", "文档ID"), ("doc_type", "", "文档类型"), ("doc_status", "", "文档状态"),
    ("created_at", "", "创建时间"))

tab("prd_change_acceptance_criteria", 1100, 1910,
    ("id", "PK", "主键"), ("project_id", "", "所属项目ID"), ("change_id", "FK", "ChangeID"),
    ("ac_id", "", "验收标准ID"), ("description", "", "验收标准描述"), ("created_at", "", "创建时间"))
tab("prd_pipeline_stage", 580, 2110,
    ("id", "PK", "主键"), ("project_id", "", "所属项目ID"), ("change_id", "FK", "ChangeID"),
    ("stage", "", "阶段名"), ("status", "", "pending/active等"), ("started_at", "", "开始时间"),
    ("completed_at", "", "完成时间"), ("created_at", "", "创建时间"), ("updated_at", "", "更新时间"))
tab("prd_change_log", 1100, 2080,
    ("id", "PK", "主键"), ("project_id", "", "所属项目ID"), ("change_id", "FK", "ChangeID"),
    ("log_type", "", "日志类型"), ("message", "", "日志消息"), ("detail", "", "日志详情"),
    ("actor", "", "操作人"), ("created_at", "", "创建时间"))

tab("prd_template", 60, 2410,
    ("id", "PK", "主键"), ("template_id", "UK", "模板业务ID"), ("type", "", "模板类型(4类)"),
    ("name", "", "模板名称"), ("description", "", "模板描述"), ("content", "", "模板Markdown含变量"),
    ("version", "", "当前版本号"), ("updated_by", "", "最后更新人"), ("created_at", "", "创建时间"),
    ("updated_at", "", "更新时间"))
tab("prd_template_version", 580, 2410,
    ("id", "PK", "主键"), ("project_id", "", "所属项目ID"), ("template_id", "FK", "模板ID"),
    ("version", "", "版本号"), ("content", "", "模板内容快照"), ("change_log", "", "变更日志"),
    ("updated_by", "", "更新人"), ("created_at", "", "创建时间"))
tab("prd_kanban_list", 60, 2710,
    ("id", "PK", "主键"), ("project_id", "", "所属项目ID"), ("list_key", "", "列标识"),
    ("list_label", "", "列显示名称"), ("sort_order", "", "排序序号"), ("created_at", "", "创建时间"))

tab("prd_kanban_card", 580, 2710,
    ("id", "PK", "主键"), ("project_id", "", "所属项目ID"), ("card_id", "UK", "卡片业务ID"),
    ("title", "", "卡片标题"), ("priority", "", "优先级P0-P3"), ("list_key", "FK", "所属列标识"),
    ("assignee", "", "负责人"), ("source_type", "", "来源类型"), ("source_id", "", "来源业务ID"),
    ("description", "", "卡片描述"), ("created_at", "", "创建时间"), ("updated_at", "", "更新时间"))
tab("prd_file_metadata", 60, 3040,
    ("id", "PK", "主键"), ("project_id", "", "所属项目ID"), ("file_md5", "UK", "文件MD5唯一标识"),
    ("file_name", "", "原始文件名"), ("file_size", "", "文件大小(字节)"), ("total_chunks", "", "总分片数"),
    ("content_type", "", "MIME类型"), ("status", "", "上传状态"), ("minio_object", "", "合并对象路径"),
    ("minio_url", "", "预签名URL"), ("created_at", "", "创建时间"), ("updated_at", "", "更新时间"))

tab("prd_file_chunks", 580, 3040,
    ("id", "PK", "主键"), ("project_id", "", "所属项目ID"), ("file_md5", "FK", "文件MD5"),
    ("file_chunk_md5", "", "分片MD5"), ("file_chunk_index", "", "分片索引"), ("file_chunk_size", "", "分片大小"),
    ("minio_object_name", "", "分片存储路径"), ("created_at", "", "创建时间"), ("updated_at", "", "更新时间"))
tab("sys_user", 1640, 200,
    ("id", "PK", "主键"), ("user_id", "UK", "用户业务ID(user-xxx)"), ("username", "UK", "登录用户名"),
    ("password_hash", "", "密码哈希(BCrypt)"), ("display_name", "", "显示名称"), ("email", "", "邮箱"),
    ("avatar_url", "", "头像URL"), ("status", "", "active/disabled"),
    ("created_at", "", "创建时间"), ("updated_at", "", "更新时间"))
tab("sys_role", 2100, 200,
    ("id", "PK", "主键"), ("role_id", "UK", "角色业务ID"), ("name", "UK", "角色名称"),
    ("description", "", "角色描述"), ("created_at", "", "创建时间"))

tab("sys_user_role", 2100, 370,
    ("id", "PK", "主键"), ("user_id", "FK", "用户ID"), ("role_id", "FK", "角色ID"))
tab("sys_permission", 1640, 460,
    ("id", "PK", "主键"), ("permission_id", "UK", "权限业务ID"), ("name", "UK", "权限名称"),
    ("description", "", "权限描述"), ("created_at", "", "创建时间"))
tab("sys_role_permission", 2100, 500,
    ("id", "PK", "主键"), ("role_id", "FK", "角色ID"), ("permission_id", "FK", "权限ID"))
tab("sys_project_member", 2100, 630,
    ("id", "PK", "主键"), ("project_id", "FK", "项目ID"), ("user_id", "FK", "用户ID"),
    ("role", "", "项目角色"), ("created_at", "", "创建时间"), ("updated_at", "", "更新时间"))

CN = {
    "prd_language": "编程语言字典表",
    "prd_framework": "框架字典表（关联语言）",
    "prd_project": "项目管理表（一项目一语言多框架）",
    "prd_project_framework": "项目-框架多对多关联表",
    "prd_project_template": "项目自定义模板表",
    "prd_ingestion": "PRD 导入记录表（原始内容+解析状态）",
    "prd_llm_result": "LLM 解析结果表（旁路展示）",
    "prd_requirement": "需求实体表（REQ-*）",
    "prd_data_entity": "数据实体表",
    "prd_data_entity_attribute": "数据实体属性表",
    "prd_data_entity_relation": "数据实体关系表",
    "prd_interface": "接口协议表",
    "prd_architecture_decision": "架构决策表（AD-*）",
    "prd_document": "PRD 生成的文档表（4 类）",
    "prd_document_approval": "文档审批记录表",
    "prd_change": "Change 记录表",
    "prd_change_document_ref": "Change-文档引用关系表",
    "prd_change_acceptance_criteria": "Change 验收标准表",
    "prd_pipeline_stage": "流水线阶段记录表",
    "prd_change_log": "流水线变更日志表",
    "prd_template": "文档模板表（全局模板）",
    "prd_template_version": "模板版本历史表",
    "prd_kanban_list": "看板列定义表",
    "prd_kanban_card": "看板卡片表",
    "prd_file_metadata": "文件上传元数据表（MinIO 分块）",
    "prd_file_chunks": "文件分片信息表（MinIO 分块）",
    "sys_user": "系统用户表",
    "sys_role": "角色表",
    "sys_user_role": "用户-角色关联表",
    "sys_permission": "权限表",
    "sys_role_permission": "角色-权限关联表",
    "sys_project_member": "项目成员表（用户-项目角色）",
}

# 逻辑外键关系：(来源表, 目标表, 来源字段, 目标字段)
REL = [
    ("prd_framework", "prd_language", "language_id", "id"),
    ("prd_project", "prd_language", "language_id", "id"),
    ("prd_project_framework", "prd_project", "project_id", "project_id"),
    ("prd_project_framework", "prd_framework", "framework_id", "id"),
    ("prd_project_template", "prd_project", "project_id", "project_id"),
    ("prd_ingestion", "prd_project", "project_id", "project_id"),
    ("prd_llm_result", "prd_ingestion", "ingestion_id", "ingestion_id"),
    ("prd_requirement", "prd_ingestion", "ingestion_id", "ingestion_id"),
    ("prd_data_entity", "prd_ingestion", "ingestion_id", "ingestion_id"),
    ("prd_data_entity_attribute", "prd_data_entity", "entity_id", "id"),
    ("prd_data_entity_relation", "prd_data_entity", "entity_id", "id"),
    ("prd_interface", "prd_ingestion", "ingestion_id", "ingestion_id"),
    ("prd_architecture_decision", "prd_ingestion", "ingestion_id", "ingestion_id"),
    ("prd_document", "prd_ingestion", "ingestion_id", "ingestion_id"),
    ("prd_document_approval", "prd_document", "doc_id", "doc_id"),
    ("prd_change", "prd_ingestion", "ingestion_id", "ingestion_id"),
    ("prd_change_document_ref", "prd_change", "change_id", "change_id"),
    ("prd_change_document_ref", "prd_document", "doc_id", "doc_id"),
    ("prd_change_acceptance_criteria", "prd_change", "change_id", "change_id"),
    ("prd_pipeline_stage", "prd_change", "change_id", "change_id"),
    ("prd_change_log", "prd_change", "change_id", "change_id"),
    ("prd_template_version", "prd_template", "template_id", "template_id"),
    ("prd_kanban_card", "prd_kanban_list", "list_key", "list_key"),
    ("prd_file_chunks", "prd_file_metadata", "file_md5", "file_md5"),
    ("sys_user_role", "sys_user", "user_id", "user_id"),
    ("sys_user_role", "sys_role", "role_id", "role_id"),
    ("sys_role_permission", "sys_role", "role_id", "role_id"),
    ("sys_role_permission", "sys_permission", "permission_id", "permission_id"),
    ("sys_project_member", "prd_project", "project_id", "project_id"),
    ("sys_project_member", "sys_user", "user_id", "user_id"),
]

FLAGCOL = {"PK": "#B45309", "UK": "#C2410C", "FK": "#1D4ED8"}

def rowy(name, col):
    x, y, cols = T[name]
    try:
        i = [c[0] for c in cols].index(col)
    except ValueError:
        i = 0
    return y + HDR + ROWH * i + ROWH // 2

def cut(s, n=20):
    return s if len(s) <= n else s[: n - 1] + "…"

def table_svg(name):
    x, y, cols = T[name]
    h = HDR + ROWH * len(cols) + PAD
    s = []
    s.append(f'<rect x="{x}" y="{y}" width="{BW}" height="{h}" rx="8" fill="#FFFFFF" stroke="#94A3B8" stroke-width="1.2"/>')
    s.append(f'<rect x="{x}" y="{y}" width="{BW}" height="{HDR}" rx="8" fill="#1E40AF"/>')
    s.append(f'<rect x="{x}" y="{y+32}" width="{BW}" height="8" fill="#1E40AF"/>')
    s.append(f'<text x="{x+14}" y="{y+17}" font-size="14" font-weight="700" fill="#FFFFFF">{name}</text>')
    s.append(f'<text x="{x+14}" y="{y+33}" font-size="11.5" fill="#DBEAFE">{html.escape(CN.get(name, ""))}</text>')
    for i, (cn, fl, cm) in enumerate(cols):
        ty = y + HDR + ROWH * i + 13
        s.append(f'<text x="{x+14}" y="{ty}" font-size="12.5" fill="#0F172A">{cn}</text>')
        if fl:
            lab = fl.split(",")[0]
            col = FLAGCOL.get(lab, "#64748B")
            s.append(f'<text x="{x+168}" y="{ty}" font-size="11" font-weight="700" fill="{col}">{lab}</text>')
        s.append(f'<text x="{x+206}" y="{ty}" font-size="11.5" fill="#64748B">{html.escape(cut(cm))}</text>')
    return "".join(s)

def line_svg(f, t, fc, tc):
    fx, fy = T[f][0], T[f][1]
    tx, ty = T[t][0], T[t][1]
    y1, y2 = rowy(f, fc), rowy(t, tc)
    if tx > fx + BW:
        d = f"M {fx+BW} {y1} L {fx+BW+16} {y1} L {fx+BW+16} {y2} L {tx} {y2}"
    else:
        d = f"M {fx} {y1} L {fx-16} {y1} L {fx-16} {y2} L {tx+BW} {y2}"
    return f'<path d="{d}" fill="none" stroke="#94A3B8" stroke-width="1.2" marker-end="url(#arw)"/>'

GROUPS = [
    (40, 150, 1520, 480, "① 基础字典与项目"),
    (40, 620, 1520, 960, "② PRD 导入与解析产物（prd_ingestion 为核心：1 条解析 → N 条结构产物）"),
    (40, 1560, 1520, 340, "③ 文档与审批"),
    (40, 1870, 1520, 520, "④ Change 与流水线"),
    (40, 2370, 1520, 310, "⑤ 模板"),
    (40, 2670, 1520, 330, "⑥ 看板"),
    (40, 3000, 1520, 340, "⑦ 文件上传（MinIO 分块）"),
    (1620, 150, 940, 660, "⑧ 用户权限（sys_*）"),
]

parts = []
parts.append(f'<svg xmlns="http://www.w3.org/2000/svg" width="{W}" height="{H}" viewBox="0 0 {W} {H}" font-family="Microsoft YaHei, PingFang SC, sans-serif">')
parts.append('<defs><marker id="arw" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="6" markerHeight="6" orient="auto-start-reverse">'
             '<path d="M 0 0 L 10 5 L 0 10 z" fill="#64748B"/></marker></defs>')
parts.append(f'<rect x="0" y="0" width="{W}" height="{H}" fill="#F1F5F9"/>')
parts.append('<text x="1300" y="46" text-anchor="middle" font-size="26" font-weight="700" fill="#0F172A">数据库 ER 图 · harness_prd_ingestion（共 32 张表，字段列出 schema.sql COMMENT 简要说明）</text>')
parts.append('<text x="1300" y="72" text-anchor="middle" font-size="13.5" fill="#475569">PK = 主键 ｜ UK = 业务唯一键(uk_*) ｜ FK = 逻辑外键（schema 中为 INDEX，无物理 FOREIGN KEY）｜ 完整类型/索引见 schema.sql</text>')
parts.append('<text x="1300" y="94" text-anchor="middle" font-size="13.5" fill="#64748B">连线 = 逻辑外键关系（圆点端为 1，箭头端为 N，一对多）</text>')
for (gx, gy, gw, gh, gt) in GROUPS:
    parts.append(f'<rect x="{gx}" y="{gy}" width="{gw}" height="{gh}" rx="12" fill="#FFFFFF" stroke="#E2E8F0"/>')
    parts.append(f'<text x="{gx+16}" y="{gy+26}" font-size="15" font-weight="700" fill="#334155">{html.escape(gt)}</text>')
for (f, t, fc, tc) in REL:
    parts.append(line_svg(f, t, fc, tc))
for name in T:
    parts.append(table_svg(name))
parts.append('</svg>')

out_path = "C:/code/tayama-harness-skills/docs/prd-ingestion-er.svg"
with open(out_path, "w", encoding="utf-8") as f:
    f.write("\n".join(parts))
print("OK tables=", len(T), "relations=", len(REL))
