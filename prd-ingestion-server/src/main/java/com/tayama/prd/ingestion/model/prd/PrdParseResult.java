package com.tayama.prd.ingestion.model.prd;

import java.util.ArrayList;
import java.util.List;

/**
 * LLM 解析 PRD 的结构化输出结果。
 *
 * <p>对应 LLM 返回的 JSON 结构，直接映射为数据库可写入的数据。</p>
 */
public class PrdParseResult {

    private List<RequirementItem> requirements = new ArrayList<>();
    private List<EntityItem> dataEntities = new ArrayList<>();
    private List<InterfaceItem> interfaces = new ArrayList<>();
    private List<ArchDecisionItem> archDecisions = new ArrayList<>();
    private String parseSource = "section_hierarchy"; // template | llm | section_hierarchy

    public List<RequirementItem> getRequirements() { return requirements; }
    public void setRequirements(List<RequirementItem> requirements) { this.requirements = requirements; }

    public List<EntityItem> getDataEntities() { return dataEntities; }
    public void setDataEntities(List<EntityItem> dataEntities) { this.dataEntities = dataEntities; }

    public List<InterfaceItem> getInterfaces() { return interfaces; }
    public void setInterfaces(List<InterfaceItem> interfaces) { this.interfaces = interfaces; }

    public List<ArchDecisionItem> getArchDecisions() { return archDecisions; }
    public void setArchDecisions(List<ArchDecisionItem> archDecisions) { this.archDecisions = archDecisions; }

    public String getParseSource() { return parseSource; }
    public void setParseSource(String parseSource) { this.parseSource = parseSource; }

    /** 需求条目 */
    public static class RequirementItem {
        private String id;
        private String description;
        private String priority = "P2";
        private String sourceParagraph;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }
        public String getSourceParagraph() { return sourceParagraph; }
        public void setSourceParagraph(String sourceParagraph) { this.sourceParagraph = sourceParagraph; }
    }

    /** 数据实体 */
    public static class EntityItem {
        private String name;
        private String description;
        private String sourceSection;
        private List<AttributeItem> attributes = new ArrayList<>();
        private List<RelationItem> relations = new ArrayList<>();

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public List<AttributeItem> getAttributes() { return attributes; }
        public void setAttributes(List<AttributeItem> attributes) { this.attributes = attributes; }
        public List<RelationItem> getRelations() { return relations; }
        public void setRelations(List<RelationItem> relations) { this.relations = relations; }
        public String getSourceSection() { return sourceSection; }
        public void setSourceSection(String sourceSection) { this.sourceSection = sourceSection; }

        public static class AttributeItem {
            private String name;
            private String type = "String";
            private String description;

            public String getName() { return name; }
            public void setName(String name) { this.name = name; }
            public String getType() { return type; }
            public void setType(String type) { this.type = type; }
            public String getDescription() { return description; }
            public void setDescription(String description) { this.description = description; }
        }

        public static class RelationItem {
            private String target;
            private String type = "ManyToOne";
            private String description;

            public String getTarget() { return target; }
            public void setTarget(String target) { this.target = target; }
            public String getType() { return type; }
            public void setType(String type) { this.type = type; }
            public String getDescription() { return description; }
            public void setDescription(String description) { this.description = description; }
        }
    }

    /** 接口定义 */
    public static class InterfaceItem {
        private String method = "GET";
        private String path;
        private String summary;
        private String sourceSection;
        private String requestBody;
        private String responseBody;

        public String getMethod() { return method; }
        public void setMethod(String method) { this.method = method; }
        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }
        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }
        public String getRequestBody() { return requestBody; }
        public void setRequestBody(String requestBody) { this.requestBody = requestBody; }
        public String getResponseBody() { return responseBody; }
        public void setResponseBody(String responseBody) { this.responseBody = responseBody; }
        public String getSourceSection() { return sourceSection; }
        public void setSourceSection(String sourceSection) { this.sourceSection = sourceSection; }
    }

    /** 架构决策 */
    public static class ArchDecisionItem {
        private String title;
        private String context;
        private String decision;
        private String sourceSection;
        private String status = "proposed";
        private List<String> consequences = new ArrayList<>();

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContext() { return context; }
        public void setContext(String context) { this.context = context; }
        public String getDecision() { return decision; }
        public void setDecision(String decision) { this.decision = decision; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public List<String> getConsequences() { return consequences; }
        public void setConsequences(List<String> consequences) { this.consequences = consequences; }
        public String getSourceSection() { return sourceSection; }
        public void setSourceSection(String sourceSection) { this.sourceSection = sourceSection; }
    }
}