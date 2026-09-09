package com.tayama.prd.ingestion.model.prd;

import java.util.List;

/**
 * 架构决策。
 */
public class ArchitectureDecision {
    private String id;
    private String title;
    private String context;
    private String decision;
    private List<String> consequences;
    private String sourceParagraph;
    private String status;     // proposed | accepted | deprecated | superseded

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContext() { return context; }
    public void setContext(String context) { this.context = context; }
    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }
    public List<String> getConsequences() { return consequences; }
    public void setConsequences(List<String> consequences) { this.consequences = consequences; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getSourceParagraph() { return sourceParagraph; }
    public void setSourceParagraph(String sourceParagraph) { this.sourceParagraph = sourceParagraph; }
}