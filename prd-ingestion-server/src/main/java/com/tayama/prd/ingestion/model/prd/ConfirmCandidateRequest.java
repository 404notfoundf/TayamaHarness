package com.tayama.prd.ingestion.model.prd;

/**
 * 候选条目人工确认请求。
 *
 * <p>{@code type} ∈ requirement | entity | interface | decision；
 * {@code id} 分别为 req_id / entity_name / "method|path"（接口复合键）/ ad_id。</p>
 */
public class ConfirmCandidateRequest {

    private String type;
    private String id;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}