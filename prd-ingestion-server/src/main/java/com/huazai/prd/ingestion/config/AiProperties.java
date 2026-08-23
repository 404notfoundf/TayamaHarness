package com.huazai.prd.ingestion.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI/LLM 服务配置。
 *
 * <p>支持 OpenAI 兼容接口，可通过环境变量覆盖：
 * <ul>
 *   <li>AI_ENABLED=false — 完全禁用 LLM 解析（仅用模板 + 关键词）</li>
 *   <li>AI_API_URL — LLM API 端点（默认 https://api.openai.com/v1/chat/completions）</li>
 *   <li>AI_API_KEY — API 密钥</li>
 *   <li>AI_MODEL — 模型名（默认 gpt-4o-mini）</li>
 *   <li>AI_MOCK=true — 启用 mock 模式（返回预设结果，不真正调用 API）</li>
 * </ul>
 */
@Component
@ConfigurationProperties(prefix = "ai")
public class AiProperties {

    /** 是否启用 LLM 解析 */
    private boolean enabled = true;

    /** 是否启用 mock 模式（开发调试用，返回预设结果） */
    private boolean mock = false;

    /** LLM API 端点（OpenAI 兼容格式） */
    private String apiUrl = "https://api.openai.com/v1/chat/completions";

    /** API 密钥 */
    private String apiKey = "";

    /** 模型名称 */
    private String model = "gpt-4o-mini";

    /** 请求超时（秒） */
    private int timeout = 60;

    /** 最大 token 数 */
    private int maxTokens = 4096;

    /** 温度参数 */
    private double temperature = 0.3;

    /** 是否在日志中打印完整 API Key（默认脱敏；仅排查问题时临时开启） */
    private boolean logFullApiKey = false;

    /**
     * 推理强度（sensenova/glm 思考型模型专用）。
     * "none" 关闭深度思考（快速响应，官方示例推荐）；空字符串则不传该参数（兼容 OpenAI 等不认识的 API）。
     */
    private String reasoningEffort = "none";

    // ---- 短内容判定阈值（字符数） ----
    private int shortContentThreshold = 200;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public boolean isMock() { return mock; }
    public void setMock(boolean mock) { this.mock = mock; }

    public String getApiUrl() { return apiUrl; }
    public void setApiUrl(String apiUrl) { this.apiUrl = apiUrl; }

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public int getTimeout() { return timeout; }
    public void setTimeout(int timeout) { this.timeout = timeout; }

    public int getMaxTokens() { return maxTokens; }
    public void setMaxTokens(int maxTokens) { this.maxTokens = maxTokens; }

    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }

    public boolean isLogFullApiKey() { return logFullApiKey; }
    public void setLogFullApiKey(boolean logFullApiKey) { this.logFullApiKey = logFullApiKey; }

    public String getReasoningEffort() { return reasoningEffort; }
    public void setReasoningEffort(String reasoningEffort) { this.reasoningEffort = reasoningEffort; }

    public int getShortContentThreshold() { return shortContentThreshold; }
    public void setShortContentThreshold(int shortContentThreshold) { this.shortContentThreshold = shortContentThreshold; }
}