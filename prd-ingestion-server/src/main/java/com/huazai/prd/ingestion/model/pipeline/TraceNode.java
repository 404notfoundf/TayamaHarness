package com.huazai.prd.ingestion.model.pipeline;

import java.util.List;

/**
 * 需求追溯树节点。
 */
public class TraceNode {
    private String id;
    private String type;     // requirement | document | change | commit | pr | deploy
    private String label;
    private String status;
    private List<TraceNode> children;
    private String url;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<TraceNode> getChildren() { return children; }
    public void setChildren(List<TraceNode> children) { this.children = children; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
}