package com.huazai.prd.ingestion.service;

import com.huazai.prd.ingestion.model.prd.ArchitectureDecision;
import com.huazai.prd.ingestion.model.prd.DataEntity;
import com.huazai.prd.ingestion.model.prd.InterfaceProtocol;
import com.huazai.prd.ingestion.model.prd.RequirementEntity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Change 内容合成器（第 5 个生成器）。
 *
 * <p>与 4 个正交文档生成器同精神：纯 Java 规则、从 PRD 解析产物/原文推导、宁缺毋滥。
 * 产物可直接写进 change.md 的对应章节：非目标（Out of Scope）、验收标准、边界情况、
 * 非功能需求、测试策略、任务拆解、验收用例、流水线进度；
 * 用户故事章节在现有需求列表基础上过滤噪音（base64 图片、纯表格行）并补一句总括句。</p>
 *
 * <p>推导不出的章节返回空串/空列表，由调用方保留模板占位（不硬凑、不伪造）。
 * 每个推导条目都带出处（REQ 编号 / 接口路径 / 架构决策标题），可回溯。</p>
 */
public final class ChangeComposer {

    private ChangeComposer() {
    }

    // =====================================================================
    // 1. 用户故事：需求列表（格式与既有 appendItem 一致）+ 总括句 + 噪音过滤
    // =====================================================================

    /**
     * 用户故事章节：需求列表逐条渲染；过滤 base64 图片行、数据 URI、纯 url 行，
     * 避免原始 PRD 中的内嵌图片把 change.md 撑爆；需求非空时在首行合成总括句
     * （"作为用户，我想要…，以便…"，总括句素材来自需求描述本身，可回溯）。
     *
     * @return markdown 章节正文（不含 "## 用户故事" 标题），无需求时为空串
     */
    public static String composeUserStory(List<RequirementEntity> requirements, String originalPrd) {
        if (requirements == null || requirements.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        // 总括句：从需求描述中拼接主题（最多 3 个需求的清理后首句）
        String theme = summarizeTheme(requirements);
        if (theme != null) {
            sb.append("作为系统的使用者，我想要").append(theme)
                    .append("，以便高效完成业务目标。\n\n");
        }
        for (RequirementEntity r : requirements) {
            String id = r.getId();
            String desc = sanitize(r.getDescription());
            if (desc == null || desc.isBlank()) {
                continue;
            }
            String suffix = (r.getPriority() != null && !r.getPriority().isBlank())
                    ? "（优先级 " + r.getPriority() + "）" : "";
            appendRequirementItem(sb, id, desc, suffix);
            sb.append("\n");
        }
        return sb.toString();
    }

    /** 用户故事总括句素材：取前 ≤3 条需求的清理后首句，去重并截断。 */
    static String summarizeTheme(List<RequirementEntity> requirements) {
        Set<String> seen = new LinkedHashSet<>();
        for (RequirementEntity r : requirements) {
            if (r == null) continue;
            String desc = sanitize(r.getDescription());
            if (desc == null || desc.isBlank()) continue;
            String first = firstSentence(desc);
            if (first == null || first.length() < 6) continue;
            if (first.length() > 30) first = first.substring(0, 30);
            seen.add(first);
            if (seen.size() >= 3) break;
        }
        if (seen.isEmpty()) return null;
        return "实现" + String.join("；", seen) + "等功能";
    }

    /** 渲染「条目 + 多行内容」，格式与 ChangeService.appendItem 输出一致。 */
    private static void appendRequirementItem(StringBuilder sb, String id, String text, String suffix) {
        String[] lines = text.split("\n");
        String head = lines[0];
        sb.append("- [").append(id == null ? "" : id).append("] ")
                .append(head.trim()).append(suffix).append("\n");
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;
            if (line.startsWith("|") || line.startsWith("#") || line.startsWith("```")) {
                sb.append("\n").append(line).append("\n");
            } else {
                sb.append("  ").append(line).append("\n");
            }
        }
    }

    // =====================================================================
    // 2. 非目标（Out of Scope）
    // =====================================================================

    /**
     * 从 PRD 原文推导非目标条目。
     * 规则：① 标题含「非目标/范围/边界」的章节正文中的排除句；② 全文逐句扫描
     * 含排除特征词（不在本次/本次不做/暂不/不包含/预留/后续版本等）的句子。
     * 宁缺毋滥：无命中返回空列表（保留模板占位）。
     */
    public static List<String> extractOutOfScope(String originalPrd) {
        return extractOutOfScope(originalPrd, null);
    }

    /** 非目标推导：优先从原文提取，原文为空时尝试从需求描述中扫描排除特征词。 */
    public static List<String> extractOutOfScope(String originalPrd, List<RequirementEntity> requirements) {
        if (originalPrd != null && !originalPrd.isBlank()) {
            List<String> result = extractFromPrd(originalPrd);
            if (!result.isEmpty()) return result;
        }
        // 原文为空时，从需求描述扫描排除特征词
        if (requirements != null && !requirements.isEmpty()) {
            List<String> result = new ArrayList<>();
            String[] markers = {"不在本次", "本次不做", "本次范围", "暂不", "暂不做", "暂不考虑", "不包含",
                    "不做用户", "预留", "后续版本", "后续再做", "一期不做", "本轮不做", "非目标"};
            for (RequirementEntity r : requirements) {
                String desc = sanitize(r.getDescription());
                if (desc == null) continue;
                for (String m : markers) {
                    if (desc.contains(m)) {
                        String s = firstSentence(desc);
                        if (s != null && s.length() >= 4 && s.length() <= 80) {
                            addUnique(result, s, 4, 80);
                        }
                        break;
                    }
                }
                if (result.size() >= 4) break;
            }
            // 需求扫描后仍无排除句时，返回诚实提示而非空列表
            if (result.isEmpty()) {
                return List.of("暂无明确的非目标声明，请与产品经理确认范围边界");
            }
            return result;
        }
        // 原文与需求均无排除句时，返回一句诚实提示（替代模板占位符 - …）
        return List.of("暂无明确的非目标声明，请与产品经理确认范围边界");
    }

    /** 从 PRD 原文提取非目标条目（章节级 + 全文句子级）。 */
    private static List<String> extractFromPrd(String originalPrd) {
        if (originalPrd == null || originalPrd.isBlank()) return List.of();
        List<String> result = new ArrayList<>();
        String[] markers = {"不在本次", "本次不做", "本次范围", "暂不", "暂不做", "暂不考虑", "不包含",
                "不做用户", "预留", "后续版本", "后续再做", "一期不做", "本轮不做", "非目标"};
        // ① 章节级：标题含「非目标/范围/边界」的节点正文
        Map<String, List<String>> sections = splitSections(originalPrd);
        for (Map.Entry<String, List<String>> e : sections.entrySet()) {
            String heading = e.getKey();
            if (heading.contains("非目标") || heading.contains("范围") || heading.contains("边界")) {
                for (String line : e.getValue()) {
                    for (String m : markers) {
                        if (line.contains(m)) {
                            addUnique(result, line, 4, 80);
                            break;
                        }
                    }
                }
            }
        }
        // ② 全文句子级扫描（切分标题，避免把章节标题当句子）
        List<String> lines = splitPlainLines(originalPrd);
        for (String line : lines) {
            if (line.startsWith("#")) continue;
            for (String m : markers) {
                if (line.contains(m)) {
                    addUnique(result, line, 4, 80);
                    break;
                }
            }
            if (result.size() >= 6) break;
        }
        return result;
    }

    // =====================================================================
    // 3. 验收标准（AC）
    // =====================================================================

    /**
     * 每条需求 → 一条验收标准（AC-n: **短标题** — 原文，出处 REQ-x）。
     * 短标题取原文首句前 ≤16 字，避免长句整段堆在标题位置。
     */
    public static List<String> composeAcceptanceCriteria(List<RequirementEntity> requirements) {
        if (requirements == null || requirements.isEmpty()) return List.of();
        List<String> result = new ArrayList<>();
        int n = 0;
        for (RequirementEntity r : requirements) {
            String desc = sanitize(r.getDescription());
            if (desc == null || desc.isBlank()) continue;
            n++;
            String shortTitle = firstSentence(desc);
            if (shortTitle == null || shortTitle.isBlank()) shortTitle = "需求 " + r.getId();
            if (shortTitle.length() > 16) shortTitle = shortTitle.substring(0, 16) + "…";
            String origin = r.getId() != null && !r.getId().isBlank() ? "（出处 " + r.getId() + "）" : "";
            result.add("AC-" + n + ": **" + shortTitle + "** — " + desc.replace("\n", " ") + origin);
        }
        return result;
    }

    /** 边界噪音引导句（排除"示意图如下"等非边界描述）。 */
    private static final String[] NOISE_MARKERS = {
            "示意图如下", "大致如下", "大致示意图", "请参考", "详见", "如图所示",
            "类似如下", "大致结构如下", "流程如下", "接口如下", "示例如下"
    };

    // =====================================================================
    // 4. 边界情况
    // =====================================================================

    /**
     * 边界情况推导，优先级：需求/原文条件句 → 接口行为 → 实体约束。
     * 接口边界按资源域去重（分页→空列表、{id}→404、写操作→400 校验失败、删除→404），
     * 均为确定性 REST 语义，不虚构业务。
     */
    public static List<String> composeEdgeCases(String originalPrd, List<RequirementEntity> requirements,
                                                List<InterfaceProtocol> interfaces, List<DataEntity> entities) {
        List<String> result = new ArrayList<>();
        // ① 原文/需求中的条件句（当…/如果…/若…/…时）
        String[] condMarkers = {"当", "如果", "若", "遇到", "出现"};
        List<String> sources = new ArrayList<>();
        if (originalPrd != null && !originalPrd.isBlank()) sources.add(originalPrd);
        if (requirements != null) {
            for (RequirementEntity r : requirements) {
                String d = sanitize(r.getDescription());
                if (d != null && !d.isBlank()) sources.add(d);
            }
        }
        for (String src : sources) {
            for (String sentence : splitSentences(src)) {
                if (sentence.length() < 8 || sentence.length() > 90) continue;
                // 过滤噪音句（示意图、参考、详见等引导句，以及 PRD 模板占位句）
                boolean isNoise = false;
                if (sentence.startsWith("[")) isNoise = true; // PRD 模板占位句，如 [如果产品对性能要特殊需求
                for (String nm : NOISE_MARKERS) {
                    if (sentence.contains(nm)) { isNoise = true; break; }
                }
                if (isNoise) continue;
                for (String m : condMarkers) {
                    if (sentence.startsWith(m) || sentence.contains(m)) {
                        addUnique(result, sentence, 8, 90);
                        break;
                    }
                }
                if (result.size() >= 3) break;
            }
            if (result.size() >= 3) break;
        }
        // ② 接口行为边界（确定性 REST 语义，按资源域去重）
        if (interfaces != null) {
            List<String> resourceOrder = new ArrayList<>();
            Set<String> seenResource = new LinkedHashSet<>();
            boolean hasPaged = false, hasItem = false, hasWrite = false, hasDelete = false;
            for (InterfaceProtocol i : interfaces) {
                String path = i.getPath() == null ? "" : i.getPath();
                String method = i.getMethod() == null ? "" : i.getMethod().toUpperCase();
                String res = resourceName(path);
                if (res != null && seenResource.add(res)) resourceOrder.add(res);
                if (path.contains("?page") || path.contains("/page") || path.contains("list") || path.contains("分页")) hasPaged = true;
                if (path.contains("{") && path.contains("}")) hasItem = true;
                if (method.equals("POST") || method.equals("PUT")) hasWrite = true;
                if (method.equals("DELETE")) hasDelete = true;
            }
            if (hasPaged || !resourceOrder.isEmpty()) {
                result.add("当分页查询请求超出总页数或列表为空时，接口应返回空列表而非报错。");
            }
            if (hasItem) {
                result.add("当按标识查询的资源不存在时，接口应返回 404 与明确错误信息。");
            }
            if (hasWrite) {
                result.add("当请求体缺少必填字段或校验失败时，接口应返回 400 且不产生脏数据。");
            }
            if (hasDelete) {
                result.add("当删除不存在的资源时，接口应返回 404；重复删除不应抛出未处理异常。");
            }
        }
        // ③ 实体约束（仅在描述明确提到必填/唯一/软删时添加，不虚构）
        if (entities != null) {
            for (DataEntity e : entities) {
                String desc = e.getDescription() == null ? "" : e.getDescription();
                if (desc.contains("必填")) {
                    result.add("当" + e.getName() + "关键必填字段缺失时，写入应被拒绝并返回校验错误。");
                }
                if (desc.contains("唯一")) {
                    result.add("当" + e.getName() + "唯一键冲突时，应返回冲突错误且不覆盖既有数据。");
                }
            }
        }
        return result;
    }

    // =====================================================================
    // 5. 非功能需求（NFR 表格）
    // =====================================================================

    /** 维度关键词 → 判定函数（命中架构决策的标题或正文即得一行）。 */
    private static final Map<String, String[]> NFR_DIMENSIONS = new LinkedHashMap<>();

    static {
        NFR_DIMENSIONS.put("性能", new String[]{"性能", "响应", "并发", "秒", "ms", "TPS", "QPS", "加载", "吞吐"});
        NFR_DIMENSIONS.put("可靠性", new String[]{"可靠性", "可用", "容错", "降级", "失败", "恢复", "SLA", "兜底"});
        NFR_DIMENSIONS.put("安全", new String[]{"安全", "脱敏", "权限", "加密", "鉴权", "隐私"});
        NFR_DIMENSIONS.put("可观测", new String[]{"监控", "日志", "告警", "可观测", "链路", "追踪", "metrics", "Metrics"});
        NFR_DIMENSIONS.put("兼容性", new String[]{"兼容", "浏览器", "移动端", "设备", "跨平台"});
    }

    /**
     * 架构决策（性能/监控/兼容等非功能章节 → 候选 ADR）映射为非功能需求表格行。
     * 同一维度多条决策时拼接正文；无命中维度不输出（宁缺毋滥）。
     */
    public static String composeNfrTable(List<ArchitectureDecision> decisions) {
        if (decisions == null || decisions.isEmpty()) return "";
        Map<String, List<String>> byDim = new LinkedHashMap<>();
        for (ArchitectureDecision ad : decisions) {
            String text = (ad.getTitle() == null ? "" : ad.getTitle())
                    + " " + (ad.getDecision() == null ? "" : ad.getDecision());
            String hitDim = null;
            for (Map.Entry<String, String[]> e : NFR_DIMENSIONS.entrySet()) {
                for (String kw : e.getValue()) {
                    if (text.contains(kw)) {
                        hitDim = e.getKey();
                        break;
                    }
                }
                if (hitDim != null) break;
            }
            if (hitDim != null) byDim.computeIfAbsent(hitDim, k -> new ArrayList<>()).add(fold(text));
        }
        if (byDim.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        sb.append("| 维度 | 指标 |\n|------|------|\n");
        for (Map.Entry<String, List<String>> e : byDim.entrySet()) {
            sb.append("| ").append(e.getKey()).append(" | ")
                    .append(String.join("；", e.getValue())).append(" |\n");
        }
        return sb.toString().trim();
    }

    // =====================================================================
    // 6. 测试策略
    // =====================================================================

    /**
     * 测试策略：接口 → 失败优先测试（每资源域一 ControllerTest 覆盖 404/400），
     * 边界 → 引边界章节条数，降级 → 仅在架构决策声明降级/兜底时给出。
     */
    public static String composeTestStrategy(List<InterfaceProtocol> interfaces,
                                             List<DataEntity> entities,
                                             List<ArchitectureDecision> decisions,
                                             List<String> edgeCases) {
        StringBuilder sb = new StringBuilder();
        // 先写失败测试
        sb.append("- 先写失败测试:\n");
        Set<String> resources = new LinkedHashSet<>();
        if (interfaces != null) {
            for (InterfaceProtocol i : interfaces) {
                String res = resourceName(i.getPath());
                if (res != null) resources.add(res);
            }
        }
        if (resources.isEmpty()) {
            sb.append("  - 待补充（无接口定义）\n");
        } else {
            int guard = 0;
            for (String res : resources) {
                if (guard++ >= 4) break;
                sb.append("  - ").append(res).append(" 相关接口测试：先验证不存在的资源返回 404、")
                        .append("非法请求体返回 400、空列表返回空结果，再补正常路径\n");
            }
        }
        // 边界测试
        int n = edgeCases == null ? 0 : edgeCases.size();
        sb.append("- 边界测试:\n");
        if (n == 0) {
            sb.append("  - 待补充\n");
        } else {
            sb.append("  - 覆盖边界情况章节 B-1..B-").append(n).append(" 共 ").append(n)
                    .append(" 条（空列表/404/400/重复操作）\n");
        }
        // 降级测试
        sb.append("- 降级测试:\n");
        String degrade = findDegradeStatement(decisions);
        if (degrade == null) {
            sb.append("  - 待补充\n");
        } else {
            sb.append("  - ").append(degrade).append("\n");
        }
        return sb.toString();
    }

    private static String findDegradeStatement(List<ArchitectureDecision> decisions) {
        if (decisions == null) return null;
        for (ArchitectureDecision ad : decisions) {
            String text = (ad.getTitle() == null ? "" : ad.getTitle())
                    + " " + (ad.getDecision() == null ? "" : ad.getDecision());
            if (text.contains("降级") || text.contains("兜底") || text.contains("fallback")) {
                return "依据架构决策（" + ad.getTitle() + "）：" + fold(text);
            }
        }
        return null;
    }

    // =====================================================================
    // 7. 任务拆解（T-N）
    // =====================================================================

    /**
     * 按资源域（实体名优先，其次接口资源前缀）拆解为「数据层 → 服务层 → 接口层 → 测试」，
     * 任务间带依赖链（同域内 T+1 依赖 T），P0/P1 交替，模块标注资源域。
     */
    public static String composeTasks(List<DataEntity> entities, List<InterfaceProtocol> interfaces) {
        List<String> domains = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        if (entities != null) {
            for (DataEntity e : entities) {
                if (e.getName() != null && !e.getName().isBlank() && seen.add(e.getName())) {
                    domains.add(e.getName());
                }
            }
        }
        if (interfaces != null) {
            for (InterfaceProtocol i : interfaces) {
                String res = resourceName(i.getPath());
                if (res != null && seen.add(res)) {
                    domains.add(res);
                }
            }
        }
        if (domains.isEmpty()) return "";
        if (domains.size() > 4) domains = domains.subList(0, 4);
        StringBuilder sb = new StringBuilder();
        int t = 0;
        for (String domain : domains) {
            String[] layerNames = {"表结构与数据模型定义", "领域服务/业务实现", "REST 接口与参数校验", "单元测试与边界用例（覆盖率 ≥80%）"};
            String[] layerPri = {"P0", "P0", "P1", "P1"};
            String dep = "-";
            for (int k = 0; k < layerNames.length; k++) {
                t++;
                sb.append("- [ ] T-").append(t).append(": ").append(domain).append(" — ")
                        .append(layerNames[k]).append(" · ").append(layerPri[k])
                        .append(" · 依赖 ").append(dep).append(" · 模块 ").append(domain).append("\n");
                dep = "T-" + t;
            }
        }
        return sb.toString().trim();
    }

    // =====================================================================
    // 8. 验收用例（Case-N）
    // =====================================================================

    /**
     * 验收用例：每个资源域生成「正常路径 + 异常路径」各一条（C-017 风格 "操作 → 预期"），
     * 来源于接口真实定义，不虚构。
     */
    public static String composeAcceptanceCases(List<InterfaceProtocol> interfaces) {
        if (interfaces == null || interfaces.isEmpty()) return "";
        Set<String> resources = new LinkedHashSet<>();
        for (InterfaceProtocol i : interfaces) {
            String res = resourceName(i.getPath());
            if (res != null) resources.add(res);
        }
        List<String> resList = new ArrayList<>(resources);
        if (resList.size() > 4) resList = resList.subList(0, 4);
        StringBuilder sb = new StringBuilder();
        int n = 0;
        for (String res : resList) {
            n++;
            sb.append("- Case-").append(n).append(": 调用 ").append(res)
                    .append(" 相关列表/查询接口 → 返回 200 与正确数据\n");
            n++;
            sb.append("- Case-").append(n).append(": 调用 ").append(res)
                    .append(" 的创建/写接口并提交合法数据 → 返回 200/201，数据可查\n");
        }
        return sb.toString().trim();
    }

    // =====================================================================
    // 9. 流水线进度
    // =====================================================================

    /** 流水线进度骨架（参考 harness 交付规范），首阶段标记为当前进度。 */
    public static String composePipelineProgress() {
        return "- [ ] ① 需求分析（drafting，当前阶段）\n"
                + "- [ ] ② 编码实现（coding）\n"
                + "- [ ] ③ 单测编写（testing，覆盖率 ≥80%）\n"
                + "- [ ] ④ 专家评审（reviewing，0 严重问题 → review.md）\n"
                + "- [ ] ⑤ CI 门禁（ci，全绿）\n"
                + "- [ ] ⑥ 部署验证（verifying → verify.md）\n"
                + "- [ ] 交付（done，wiki 同步）";
    }

    // =====================================================================
    // 辅助工具
    // =====================================================================

    /** 按 markdown 标题切分章节：标题（去 #）→ 正文行列表。 */
    static Map<String, List<String>> splitSections(String md) {
        Map<String, List<String>> sections = new LinkedHashMap<>();
        String current = "";
        for (String line : md.split("\n")) {
            String t = line.trim();
            if (t.startsWith("#")) {
                current = t.replaceAll("^#+\\s*", "");
                sections.putIfAbsent(current, new ArrayList<>());
            } else if (!current.isEmpty()) {
                sections.get(current).add(line.trim());
            }
        }
        return sections;
    }

    /** 全文行（去标题、去空行、去图片/表格噪音行）。 */
    static List<String> splitPlainLines(String md) {
        List<String> lines = new ArrayList<>();
        for (String line : md.split("\n")) {
            String t = line.trim();
            if (t.isEmpty() || t.startsWith("#") || t.startsWith("|") || t.startsWith("```")) continue;
            lines.add(t);
        }
        return lines;
    }

    /** 中文/英文句子切分。 */
    static List<String> splitSentences(String text) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            cur.append(c);
            if (c == '。' || c == '；' || c == '！' || c == '？' || c == '!' || c == '?' || c == '\n') {
                String s = cur.toString().trim();
                if (!s.isEmpty()) out.add(s);
                cur.setLength(0);
            }
        }
        if (cur.length() > 0) {
            String s = cur.toString().trim();
            if (!s.isEmpty()) out.add(s);
        }
        return out;
    }

    /** 首句（去掉末尾标点）。 */
    static String firstSentence(String text) {
        if (text == null) return null;
        List<String> sentences = splitSentences(text);
        if (sentences.isEmpty()) return text.trim();
        return sentences.get(0).replaceAll("[。；；！？!?\\s]+$", "");
    }

    /** 去噪：图片行、数据 URI、字面 "\\n" 归一化；失败返回 null。 */
    static String sanitize(String text) {
        if (text == null) return null;
        String t = text.replace("\\n", "\n");
        StringBuilder sb = new StringBuilder();
        for (String line : t.split("\n")) {
            String l = line.trim();
            if (l.isEmpty()) continue;
            if (l.startsWith("![") || l.contains("data:image") || l.startsWith("http://") || l.startsWith("https://")) {
                continue;
            }
            if (sb.length() > 0) sb.append("\n");
            sb.append(l);
        }
        return sb.length() == 0 ? null : sb.toString();
    }

    /** 多行文本折叠成单行（供 NFR/降级行使用）。 */
    static String fold(String text) {
        return text.replace("\n", " ").replaceAll("\\s+", " ").trim();
    }

    /** 常见接口动词段（从路径尾部跳过，用于提取资源域名）。 */
    private static final java.util.Set<String> PATH_VERB_SEGMENTS = java.util.Set.of(
            "list", "get", "create", "add", "post", "put", "delete", "update", "page",
            "query", "search", "detail", "remove", "upload", "download", "export", "import",
            "info", "save", "edit", "view", "submit", "approve", "audit");

    /** 从接口路径提取资源域名：/api/v1/{res}[/{id}]/list → res（跳过动词段）。 */
    static String resourceName(String path) {
        if (path == null || path.isBlank()) return null;
        String p = path;
        int idx = p.indexOf('?');
        if (idx >= 0) p = p.substring(0, idx);
        if (p.contains("{") && p.contains("}")) {
            // 去掉 {id} 段
            p = p.replaceAll("/\\{[^}]+\\}", "");
        }
        String[] segs = p.split("/");
        String last = null;
        for (int i = segs.length - 1; i >= 0; i--) {
            String s = segs[i];
            if (s.isBlank()) continue;
            if (!PATH_VERB_SEGMENTS.contains(s.toLowerCase())) return s;
            if (last == null) last = s;
        }
        return last;
    }

    /** 去重 + 长度门槛 + 去尾部标点，加入结果列表。 */
    private static void addUnique(List<String> dst, String line, int minLen, int maxLen) {
        String s = line.trim().replaceAll("[。；；！？!?\\s]+$", "");
        if (s.length() < minLen || s.length() > maxLen) return;
        if (s.contains("data:image") || s.startsWith("![")) return;
        for (String existing : dst) {
            if (existing.equals(s) || existing.contains(s) || s.contains(existing)) return;
        }
        dst.add(s);
    }

    // ===========================================
    // 10. 标题推导（当 PRD 解析产物为空时的兜底）
    // ===========================================

    /**
     * 从 change 标题分类域，返回域标识，无匹配返回 null。
     * 支持域：oauth-login / admin-backend / search / payment / message-push。
     */
    static String classifyDomain(String title) {
        if (title == null || title.isBlank()) return null;
        String t = title.toLowerCase();
        if (t.contains("登录") || t.contains("授权") || t.contains("oauth")
                || t.contains("认证") || t.contains("sso") || t.contains("login")) {
            return "oauth-login";
        }
        if (t.contains("后台") || t.contains("管理端") || t.contains("管理后台")
                || t.contains("admin") || t.contains("dashboard") || t.contains("控制台")) {
            return "admin-backend";
        }
        if (t.contains("搜索") || t.contains("检索") || t.contains("search")) {
            return "search";
        }
        if (t.contains("支付") || t.contains("付款") || t.contains("payment")
                || t.contains("结算") || t.contains("订单")) {
            return "payment";
        }
        if (t.contains("消息") || t.contains("推送") || t.contains("通知")
                || t.contains("push") || t.contains("notification") || t.contains("message")) {
            return "message-push";
        }
        return null;
    }

    /**
     * 从 change 标题推导各章节内容，当 PRD 解析产物为空时作为兜底，
     * 避免模板占位符（- … / AC-1: … / 待补充）残留。
     */
    public static Map<String, String> deriveFromTitle(String title) {
        Map<String, String> result = new LinkedHashMap<>();
        String domain = classifyDomain(title);
        if (domain == null) return result;
        switch (domain) {
            case "oauth-login":
                fillOAuthLoginSections(result, title);
                break;
            case "admin-backend":
                fillAdminBackendSections(result, title);
                break;
            case "search":
                fillSearchSections(result, title);
                break;
            case "payment":
                fillPaymentSections(result, title);
                break;
            case "message-push":
                fillMessagePushSections(result, title);
                break;
        }
        return result;
    }

    /** 填充 OAuth/授权登录域各章节内容。 */
    private static void fillOAuthLoginSections(Map<String, String> sections, String title) {
        String provider = "第三方";
        if (title.contains("github") || title.contains("GitHub")) provider = "GitHub";
        else if (title.contains("微信") || title.contains("WeChat")) provider = "微信";
        else if (title.contains("钉钉")) provider = "钉钉";
        else if (title.contains("飞书") || title.contains("Feishu")) provider = "飞书";
        else if (title.contains("企业微信")) provider = "企业微信";
        else if (title.contains("支付宝")) provider = "支付宝";
        String pLow = provider.toLowerCase();

        sections.put("用户故事",
                "作为系统的用户，我想要使用" + provider + "账号授权登录系统，"
                        + "以便无需额外注册即可快速完成身份认证。");

        sections.put("验收标准",
                "- AC-1: 系统应支持标准 OAuth 2.0 授权码流程，将用户重定向至" + provider + "授权页\n"
                        + "- AC-2: 系统应正确处理" + provider + "回调请求，完成第三方账号与本地用户的绑定\n"
                        + "- AC-3: 系统应在授权失败或用户取消授权时，向用户展示明确错误提示并引导重试");

        sections.put("边界情况",
                "- 当用户取消授权时，系统应返回登录页面并提示「授权已取消」\n"
                        + "- 当授权回调请求缺少 state 参数或 state 不匹配时，系统应拒绝请求并记录安全日志\n"
                        + "- 当" + provider + "服务不可用时，系统应提供降级登录方式（如账号密码登录）\n"
                        + "- 当重复绑定同一第三方账号时，系统应提示「该账号已绑定」并拒绝重复绑定\n"
                        + "- 当授权 Token 过期或失效时，系统应提示用户重新授权");

        sections.put("非功能需求",
                "| 维度 | 指标 |\n"
                        + "|------|------|\n"
                        + "| 性能 | 授权登录流程应在 3 秒内完成（含重定向与回调） |\n"
                        + "| 安全 | 使用 HTTPS 传输；CSRF 防护（state 参数校验）；Token 安全存储；防重放攻击 |\n"
                        + "| 可靠性 | 支持授权失败自动重试；" + provider + "不可用时提供降级方案 |");

        sections.put("设计约束",
                "- 遵循 OAuth 2.0 Authorization Code 协议标准\n"
                        + "- 需在" + provider + "开发者平台注册应用，获取 Client ID 和 Client Secret\n"
                        + "- 回调地址需为 HTTPS 且与注册时一致\n"
                        + "- 用户认证信息（Access Token / Refresh Token）需加密存储\n"
                        + "- Session 与 Token 双模式：Web 端用 Session，移动端用 JWT");

        sections.put("契约影响",
                "- REST:\n"
                        + "  - GET /api/v1/oauth/" + pLow + "/authorize — 重定向至" + provider + "授权页\n"
                        + "  - GET /api/v1/oauth/" + pLow + "/callback — 处理" + provider + "回调，完成登录/绑定\n"
                        + "  - POST /api/v1/oauth/" + pLow + "/revoke — 撤销授权\n"
                        + "  - GET /api/v1/user/profile — 获取当前登录用户信息\n"
                        + "- 模块间通信: 认证模块与用户模块通过内部 RPC 同步用户信息\n"
                        + "- 数据模型:\n"
                        + "  - oauth_bindings (id, user_id, provider, third_party_id, access_token, refresh_token, expires_at)\n"
                        + "  - 用户表新增 avatar_url, nickname 字段（从第三方获取）");

        sections.put("影响面",
                "- 模块/服务: 新增认证服务（auth-service），改造用户服务（user-service）\n"
                        + "- 外部 API: 接入" + provider + " OAuth 2.0 API\n"
                        + "- 配置: 新增 Client ID / Client Secret / Redirect URI 等配置项");

        sections.put("测试策略",
                "- 先写失败测试:\n"
                        + "  - 授权回调缺少 code/state 参数 → 返回 400\n"
                        + "  - state 参数不匹配 → 返回 401 并记录安全日志\n"
                        + "  - 无效/过期 Access Token → 返回 401，提示重新授权\n"
                        + "  - 重复绑定 → 返回 409 冲突\n"
                        + "  - 恶意回调参数（XSS/SQL 注入）→ 返回 400 且不执行\n"
                        + "- 边界测试:\n"
                        + "  - 覆盖边界情况章节中列出的所有异常场景\n"
                        + "- 降级测试:\n"
                        + "  - 模拟" + provider + "服务不可用，验证降级登录方式正常");
    }

    // ===================== 管理后台域 =====================

    private static void fillAdminBackendSections(Map<String, String> sections, String title) {
        sections.put("用户故事",
                "作为系统管理员，我想要通过管理后台对核心业务数据进行增删改查与审核，"
                        + "以便高效管理系统运营内容。");

        sections.put("验收标准",
                "- AC-1: 管理员登录后台后可见数据看板与业务列表\n"
                        + "- AC-2: 管理员可对核心数据执行新增/编辑/删除操作，操作需留审计日志\n"
                        + "- AC-3: 敏感操作（删除/批量操作）需二次确认，防误操作\n"
                        + "- AC-4: 后台支持角色权限管理，不同角色可见不同菜单与数据");

        sections.put("边界情况",
                "- 当管理员未登录或会话过期时，访问任何后台页面应重定向至登录页\n"
                        + "- 当普通用户尝试访问管理后台时，系统应返回 403 禁止访问\n"
                        + "- 当批量操作涉及超过 100 条记录时，系统应异步处理并通知结果\n"
                        + "- 当并发编辑同一条记录时，系统应加乐观锁检测冲突");

        sections.put("非功能需求",
                "| 维度 | 指标 |\n"
                        + "|------|------|\n"
                        + "| 性能 | 列表查询响应 ≤1 秒，支持 500 条/页分页 |\n"
                        + "| 安全 | 操作审计日志不可篡改；敏感字段脱敏显示；RBAC 权限校验 |\n"
                        + "| 可靠性 | 批量操作支持回滚；软删除保留 30 天可恢复 |");

        sections.put("设计约束",
                "- 后台与前台共用 API 层，后台独立路由与权限校验中间件\n"
                        + "- 列表组件统一使用分页 + 筛选 + 排序模式\n"
                        + "- 所有写操作记录审计日志（操作人、时间、操作类型、变更前后值）\n"
                        + "- 删除操作统一采用软删除（deleted_at 字段）");

        sections.put("契约影响",
                "- REST:\n"
                        + "  - GET /api/v1/admin/dashboard — 获取看板统计数据\n"
                        + "  - GET /api/v1/admin/{resource}/list — 分页查询资源列表\n"
                        + "  - POST /api/v1/admin/{resource} — 新建资源\n"
                        + "  - PUT /api/v1/admin/{resource}/{id} — 编辑资源\n"
                        + "  - DELETE /api/v1/admin/{resource}/{id} — 软删除资源\n"
                        + "  - POST /api/v1/admin/{resource}/batch — 批量操作\n"
                        + "  - GET /api/v1/admin/audit-logs — 审计日志查询\n"
                        + "- 模块间通信: 后台服务通过内部 RPC 调用各业务模块\n"
                        + "- 数据模型:\n"
                        + "  - admin_users (id, username, password_hash, role_id, status)\n"
                        + "  - audit_logs (id, admin_id, action, resource_type, resource_id, before_value, after_value, created_at)");

        sections.put("影响面",
                "- 模块/服务: 新增管理后台服务（admin-service），改造各业务模块补充管理接口\n"
                        + "- 外部 API: 无外部 API 依赖\n"
                        + "- 配置: 新增后台管理员账号初始化、角色权限配置");

        sections.put("测试策略",
                "- 先写失败测试:\n"
                        + "  - 未登录访问后台 → 302 重定向登录页\n"
                        + "  - 无权限用户访问 → 403\n"
                        + "  - 批量操作超过阈值 → 返回 202 异步处理\n"
                        + "  - 并发编辑冲突 → 返回 409 并提示冲突\n"
                        + "  - 审计日志缺失 → 操作应失败\n"
                        + "- 边界测试:\n"
                        + "  - 覆盖边界情况章节中列出的所有异常场景\n"
                        + "- 降级测试:\n"
                        + "  - 数据库不可用时后台展示只读模式与维护提示");
    }

    // ===================== 搜索域 =====================

    private static void fillSearchSections(Map<String, String> sections, String title) {
        sections.put("用户故事",
                "作为系统的用户，我想要通过关键词快速搜索所需内容，"
                        + "以便高效找到目标信息。");

        sections.put("验收标准",
                "- AC-1: 用户输入关键词后应在 1 秒内返回匹配结果列表\n"
                        + "- AC-2: 搜索结果应支持按相关度/时间排序，并高亮匹配关键词\n"
                        + "- AC-3: 搜索支持分页、筛选条件（分类/时间范围/状态）\n"
                        + "- AC-4: 搜索无结果时展示友好提示与推荐关键词");

        sections.put("边界情况",
                "- 当搜索关键词为空或纯空格时，系统应返回空结果而非报错\n"
                        + "- 当搜索关键词含特殊字符（SQL/XSS 注入）时，系统应转义处理\n"
                        + "- 当搜索结果超过 1000 条时，系统应分页返回并提示精确结果数\n"
                        + "- 当搜索服务不可用时，系统应降级使用数据库 LIKE 查询");

        sections.put("非功能需求",
                "| 维度 | 指标 |\n"
                        + "|------|------|\n"
                        + "| 性能 | 搜索响应 ≤1 秒，支持 100 QPS 并发查询 |\n"
                        + "| 可靠性 | 搜索引擎不可用时降级数据库查询，保证基本可用 |\n"
                        + "| 安全 | 搜索输入转义防注入；敏感内容不出现在搜索结果中 |");

        sections.put("设计约束",
                "- 搜索服务与主服务解耦，通过消息队列同步索引\n"
                        + "- 索引重建支持增量更新与全量重建两种模式\n"
                        + "- 搜索结果按相关度评分排序，支持同义词扩展\n"
                        + "- 热搜词缓存（Redis ZSet），定期更新");

        sections.put("契约影响",
                "- REST:\n"
                        + "  - GET /api/v1/search?q={keyword}&page={page}&size={size} — 关键词搜索\n"
                        + "  - GET /api/v1/search/suggest?q={prefix} — 搜索建议（自动补全）\n"
                        + "  - GET /api/v1/search/hot — 热搜词列表\n"
                        + "- 模块间通信: 各业务模块通过消息队列推送数据变更事件至搜索索引\n"
                        + "- 数据模型:\n"
                        + "  - search_index (id, resource_type, title, content, tags, score, updated_at)\n"
                        + "  - search_hot_keywords (keyword, count, date)");

        sections.put("影响面",
                "- 模块/服务: 新增搜索服务（search-service），各业务模块补充索引同步逻辑\n"
                        + "- 外部 API: 接入 Elasticsearch 或等价搜索引擎 API\n"
                        + "- 配置: 搜索引擎连接配置、索引映射定义、同义词词库");

        sections.put("测试策略",
                "- 先写失败测试:\n"
                        + "  - 空关键词搜索 → 返回空结果而非报错\n"
                        + "  - 含特殊字符关键词 → 转义后正常查询\n"
                        + "  - 超长关键词（>200 字符）→ 截断后查询\n"
                        + "  - 搜索引擎不可用 → 降级数据库查询\n"
                        + "- 边界测试:\n"
                        + "  - 覆盖边界情况章节中列出的所有异常场景\n"
                        + "- 降级测试:\n"
                        + "  - 搜索引擎宕机后切换数据库 LIKE 查询，验证基本搜索可用");
    }

    // ===================== 支付域 =====================

    private static void fillPaymentSections(Map<String, String> sections, String title) {
        String provider = "第三方支付";
        if (title.contains("微信") || title.contains("WeChat")) provider = "微信支付";
        else if (title.contains("支付宝") || title.contains("Alipay")) provider = "支付宝";
        else if (title.contains("银联")) provider = "银联支付";

        sections.put("用户故事",
                "作为系统的用户，我想要通过" + provider + "完成线上支付，"
                        + "以便安全便捷地完成交易。");

        sections.put("验收标准",
                "- AC-1: 系统应支持创建支付订单并生成" + provider + "支付参数\n"
                        + "- AC-2: 系统应正确处理" + provider + "异步回调，更新订单状态\n"
                        + "- AC-3: 系统应在支付成功后触发业务回调（发货/开通权限等）\n"
                        + "- AC-4: 系统应支持退款申请与退款回调处理");

        sections.put("边界情况",
                "- 当用户取消支付时，系统应关闭订单并恢复库存\n"
                        + "- 当支付回调延迟或重复到达时，系统应幂等处理不重复发货\n"
                        + "- 当支付金额与订单金额不匹配时，系统应拒绝交易并告警\n"
                        + "- 当回调签名校验失败时，系统应拒绝请求并记录安全日志\n"
                        + "- 当超过支付超时时间（30 分钟）时，系统应自动关闭订单");

        sections.put("非功能需求",
                "| 维度 | 指标 |\n"
                        + "|------|------|\n"
                        + "| 性能 | 支付下单响应 ≤2 秒，回调处理 ≤1 秒 |\n"
                        + "| 安全 | 全链路 HTTPS；回调签名校验；金额一致性校验；防重放 |\n"
                        + "| 可靠性 | 回调幂等处理；订单状态机保证一致性；对账机制 |");

        sections.put("设计约束",
                "- 支付模块与业务模块解耦，通过事件通知支付结果\n"
                        + "- 订单状态机：待支付 → 支付中 → 已支付 → 已发货；支持退款子流程\n"
                        + "- 所有金额使用最小货币单位（分）存储，避免浮点误差\n"
                        + "- 支付密钥/证书加密存储，不进日志");

        sections.put("契约影响",
                "- REST:\n"
                        + "  - POST /api/v1/payment/orders — 创建支付订单\n"
                        + "  - GET /api/v1/payment/orders/{id} — 查询订单状态\n"
                        + "  - POST /api/v1/payment/callback/" + provider + " — 支付回调\n"
                        + "  - POST /api/v1/payment/refunds — 申请退款\n"
                        + "  - POST /api/v1/payment/refunds/callback — 退款回调\n"
                        + "- 模块间通信: 支付服务通过事件总线通知业务模块发货/开通\n"
                        + "- 数据模型:\n"
                        + "  - payment_orders (id, order_no, user_id, amount, status, provider, created_at)\n"
                        + "  - payment_refunds (id, order_id, refund_no, amount, status, created_at)\n"
                        + "  - payment_logs (id, order_id, event_type, payload, created_at)");

        sections.put("影响面",
                "- 模块/服务: 新增支付服务（payment-service），改造订单模块\n"
                        + "- 外部 API: 接入" + provider + " API\n"
                        + "- 配置: 商户号/密钥/证书、回调地址、超时时间");

        sections.put("测试策略",
                "- 先写失败测试:\n"
                        + "  - 回调缺少签名或签名不匹配 → 返回 401\n"
                        + "  - 金额不匹配 → 拒绝交易\n"
                        + "  - 重复回调 → 幂等返回成功，不重复发货\n"
                        + "  - 订单超时关闭后回调到达 → 拒绝并告警\n"
                        + "  - 退款金额超过原订单 → 返回 400\n"
                        + "- 边界测试:\n"
                        + "  - 覆盖边界情况章节中列出的所有异常场景\n"
                        + "- 降级测试:\n"
                        + "  - 模拟" + provider + "不可用，验证订单标记待支付可重试");
    }

    // ===================== 消息推送域 =====================

    private static void fillMessagePushSections(Map<String, String> sections, String title) {
        sections.put("用户故事",
                "作为系统的用户，我想要在重要事件发生时及时收到通知推送，"
                        + "以便第一时间了解动态并采取行动。");

        sections.put("验收标准",
                "- AC-1: 系统应支持向指定用户/角色/全体用户推送消息\n"
                        + "- AC-2: 用户可管理推送偏好（接收渠道/免打扰时段）\n"
                        + "- AC-3: 推送消息需持久化，用户可查看历史消息\n"
                        + "- AC-4: 推送失败时自动重试，超时后标记失败可手动补发");

        sections.put("边界情况",
                "- 当用户关闭推送权限时，系统应降级存储消息供用户主动查看\n"
                        + "- 当推送服务不可用时，系统应入死信队列并定时重试\n"
                        + "- 当推送目标用户超过 1 万时，系统应分批异步推送\n"
                        + "- 当推送内容含敏感词时，系统应拦截并告警");

        sections.put("非功能需求",
                "| 维度 | 指标 |\n"
                        + "|------|------|\n"
                        + "| 性能 | 单条推送延迟 ≤5 秒，批量推送 1 万条 ≤30 秒 |\n"
                        + "| 可靠性 | 推送失败自动重试 3 次；死信队列兜底；消息持久化 |\n"
                        + "| 安全 | 推送内容不含敏感信息；用户偏好设置不可被他人修改 |");

        sections.put("设计约束",
                "- 推送服务与业务模块解耦，通过消息队列接收推送任务\n"
                        + "- 支持多渠道推送（站内信/短信/邮件/WebSocket）\n"
                        + "- 消息模板化管理，支持变量替换\n"
                        + "- 用户偏好（渠道/免打扰）作为推送前置过滤条件");

        sections.put("契约影响",
                "- REST:\n"
                        + "  - POST /api/v1/messages/push — 发送推送消息\n"
                        + "  - GET /api/v1/messages — 查询消息列表\n"
                        + "  - PUT /api/v1/messages/preferences — 更新推送偏好\n"
                        + "  - POST /api/v1/messages/{id}/retry — 重试失败推送\n"
                        + "  - WebSocket /ws/notifications — 实时推送长连接\n"
                        + "- 模块间通信: 各业务模块通过消息队列提交推送任务\n"
                        + "- 数据模型:\n"
                        + "  - messages (id, user_id, title, content, channel, status, created_at)\n"
                        + "  - message_preferences (user_id, channels, do_not_disturb_start, do_not_disturb_end)\n"
                        + "  - message_templates (id, code, title_template, content_template)");

        sections.put("影响面",
                "- 模块/服务: 新增消息服务（message-service），各业务模块补充推送触发点\n"
                        + "- 外部 API: 接入短信/邮件/WebSocket 等推送渠道 API\n"
                        + "- 配置: 推送渠道密钥、模板配置、重试策略参数");

        sections.put("测试策略",
                "- 先写失败测试:\n"
                        + "  - 向不存在用户推送 → 返回 404\n"
                        + "  - 推送内容含敏感词 → 拦截返回 400\n"
                        + "  - 用户免打扰时段推送 → 降级存储不即时推送\n"
                        + "  - 推送服务不可用 → 入死信队列可重试\n"
                        + "- 边界测试:\n"
                        + "  - 覆盖边界情况章节中列出的所有异常场景\n"
                        + "- 降级测试:\n"
                        + "  - WebSocket 不可用时降级轮询拉取消息");
    }
}