package com.huazai.prd.ingestion.model.prd;

import java.util.List;

/**
 * 需求实体。
 */
public class RequirementEntity {
    private String id;
    private String description;
    private String priority;          // P0 | P1 | P2 | P3
    private List<String> relatedEntities;
    private String sourceParagraph;
    private String notes;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public List<String> getRelatedEntities() { return relatedEntities; }
    public void setRelatedEntities(List<String> relatedEntities) { this.relatedEntities = relatedEntities; }
    public String getSourceParagraph() { return sourceParagraph; }
    public void setSourceParagraph(String sourceParagraph) { this.sourceParagraph = sourceParagraph; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}