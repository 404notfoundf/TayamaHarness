package com.huazai.prd.ingestion.model.prd;

/**
 * 接口协议。
 */
public class InterfaceProtocol {
    private String method;      // GET | POST | PUT | DELETE | PATCH
    private String path;
    private String summary;
    private String requestBody;
    private String responseBody;
    private String sourceParagraph;
    private String notes;

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
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getSourceParagraph() { return sourceParagraph; }
    public void setSourceParagraph(String sourceParagraph) { this.sourceParagraph = sourceParagraph; }
}