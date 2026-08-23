package com.huazai.prd.ingestion.model.prd;

import java.util.List;

/**
 * 数据实体（领域对象）。
 */
public class DataEntity {
    private String name;
    private String description;
    private String sourceParagraph;
    private List<Attribute> attributes;
    private List<Relation> relations;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSourceParagraph() { return sourceParagraph; }
    public void setSourceParagraph(String sourceParagraph) { this.sourceParagraph = sourceParagraph; }
    public List<Attribute> getAttributes() { return attributes; }
    public void setAttributes(List<Attribute> attributes) { this.attributes = attributes; }
    public List<Relation> getRelations() { return relations; }
    public void setRelations(List<Relation> relations) { this.relations = relations; }

    public static class Attribute {
        private String name;
        private String type;
        private String description;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class Relation {
        private String target;
        private String type;
        private String description;

        public String getTarget() { return target; }
        public void setTarget(String target) { this.target = target; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}