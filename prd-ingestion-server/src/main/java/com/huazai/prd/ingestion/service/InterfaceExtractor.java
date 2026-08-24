package com.huazai.prd.ingestion.service;

import com.huazai.prd.ingestion.model.prd.CandidateStatus;
import com.huazai.prd.ingestion.model.prd.DataEntity;
import com.huazai.prd.ingestion.model.prd.InterfaceProtocol;

import java.util.ArrayList;
import java.util.List;

/**
 * 接口协议生成器。
 *
 * <p>策略：</p>
 * <ul>
 *   <li>confirmed：接口/API 章节中能解析出真实路径（含 /api/ 等）的段落；无法解析出路径的段落不再伪造
 *       {@code /api/v1/endpointXXX}；</li>
 *   <li>proposed：PRD 未定义接口且存在数据实体时，为每个实体推导 CRUD 候选接口
 *       （GET/POST 列表与创建、GET/PUT/DELETE 单条），并在 notes 中标注“推导生成，需人工确认”。</li>
 * </ul>
 */
final class InterfaceExtractor {

    private static final int MAX_DERIVED_INTERFACES = 25;

    private InterfaceExtractor() {
    }

    static List<InterfaceProtocol> extract(List<SectionNode> tree, List<DataEntity> entities) {
        List<InterfaceProtocol> out = new ArrayList<>();
        // Phase 1：接口章节（confirmed）
        for (SectionNode node : PrdSectionParser.nodesOfType(tree, "interface-protocol")) {
            for (String para : node.paragraphs) {
                String trimmed = para.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")
                        || PrdIngestionService.isNoiseParagraph(trimmed)) {
                    continue;
                }
                String path = extractPathStrict(trimmed);
                if (path.isEmpty()) {
                    continue; // 没有真实 /api/ 路径，不伪造
                }
                InterfaceProtocol ip = new InterfaceProtocol();
                ip.setMethod(detectMethod(trimmed));
                ip.setPath(path);
                ip.setSummary(firstLine(trimmed, 60));
                ip.setSourceParagraph(trimmed);
                ip.setCandidateStatus(CandidateStatus.CONFIRMED);
                out.add(ip);
            }
        }
        // Phase 2：从数据实体推导 CRUD 候选（proposed）
        if (out.isEmpty() && !entities.isEmpty()) {
            for (DataEntity e : entities) {
                String resource = entityResource(e.getName());
                if (resource.isEmpty() || out.size() >= MAX_DERIVED_INTERFACES) {
                    continue;
                }
                out.add(crud("GET", "/api/v1/" + resource, "查询" + e.getName() + "列表", e, "列表"));
                out.add(crud("POST", "/api/v1/" + resource, "创建" + e.getName(), e, "创建参数"));
                out.add(crud("GET", "/api/v1/" + resource + "/{id}", "查询" + e.getName() + "详情", e, "详情"));
                out.add(crud("PUT", "/api/v1/" + resource + "/{id}", "更新" + e.getName(), e, "更新参数"));
                out.add(crud("DELETE", "/api/v1/" + resource + "/{id}", "删除" + e.getName(), e, null));
            }
        }
        return out;
    }

    private static InterfaceProtocol crud(String method, String path, String summary, DataEntity entity,
                                          String bodyKind) {
        InterfaceProtocol ip = new InterfaceProtocol();
        ip.setMethod(method);
        ip.setPath(path);
        ip.setSummary(summary);
        ip.setRequestBody(bodyKind == null ? null
                : bodyJson(entity, bodyKind));
        ip.setResponseBody("{\n  \"code\": 0,\n  \"data\": " + (("列表".equals(bodyKind) ? "[" : "{") + " … }\n}") + "\n}");
        ip.setNotes("候选接口：由「" + entity.getName() + "」推导生成，路径与报文需人工确认");
        ip.setCandidateStatus(CandidateStatus.PROPOSED);
        return ip;
    }

    private static String bodyJson(DataEntity entity, String kind) {
        StringBuilder sb = new StringBuilder("{\n");
        if (entity.getAttributes() != null) {
            for (DataEntity.Attribute a : entity.getAttributes()) {
                sb.append("  \"").append(a.getName()).append("\": ").append("\"").append(a.getType()).append("\",\n");
            }
        }
        sb.append("} // ").append(kind);
        return sb.toString();
    }

    /** 实体名 → REST 资源名：中文原样（候选，路径命名待人工调整）。 */
    static String entityResource(String entityName) {
        if (entityName == null) return "";
        String name = entityName.trim().replaceAll("\\s+", "-");
        return name.length() > 0 && name.length() <= 40 ? name : "";
    }

    private static String detectMethod(String para) {
        String upper = para.toUpperCase();
        if (upper.contains("POST") || upper.contains("创建") || upper.contains("新增")) return "POST";
        if (upper.contains("PUT") || upper.contains("更新") || upper.contains("修改")) return "PUT";
        if (upper.contains("DELETE") || upper.contains("删除")) return "DELETE";
        if (upper.contains("PATCH") || upper.contains("部分更新")) return "PATCH";
        return "GET";
    }

    /** 只返回真实出现在段落中的 /api/ 路径；不出现则返回空串（不伪造）。 */
    private static String extractPathStrict(String para) {
        int idx = para.indexOf("/api/");
        if (idx < 0) {
            return "";
        }
        int end = idx + "/api/".length();
        while (end < para.length() && para.charAt(end) != ' ' && para.charAt(end) != '\n'
                && para.charAt(end) != '\t' && para.charAt(end) != '|') {
            end++;
        }
        String path = para.substring(idx, end).trim();
        return path.length() > 4 ? path : "";
    }

    private static String firstLine(String text, int maxLen) {
        String first = text.replaceAll("\\s+", " ").trim();
        return first.length() > maxLen ? first.substring(0, maxLen) : first;
    }
}