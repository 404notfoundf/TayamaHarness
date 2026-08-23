package com.huazai.prd.ingestion.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 统一响应格式 {@code {code, message, data}}。
 * 前端约定：code="OK" 表示成功，其余为错误码。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseResponse<T> {

    private String code;
    private String message;
    private T data;

    public BaseResponse() {}

    public BaseResponse(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /** 成功响应 */
    public static <T> BaseResponse<T> ok(T data) {
        return new BaseResponse<>("OK", "success", data);
    }

    /** 成功响应（自定义消息） */
    public static <T> BaseResponse<T> ok(String message, T data) {
        return new BaseResponse<>("OK", message, data);
    }

    /** 错误响应 */
    public static <T> BaseResponse<T> error(String code, String message) {
        return new BaseResponse<>(code, message, null);
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}