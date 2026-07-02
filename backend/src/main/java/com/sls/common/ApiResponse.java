package com.sls.common;

/**
 * 统一 API 响应格式：{ code, message, data }
 */
public class ApiResponse<T> {

    private int code;       // 0=成功，-1=失败
    private String message;
    private T data;

    /** 成功响应（默认消息） */
    public static <T> ApiResponse<T> ok(T data) {
        ApiResponse<T> r = new ApiResponse<>();
        r.code = 0;
        r.message = "success";
        r.data = data;
        return r;
    }

    /** 成功响应（自定义消息） */
    public static <T> ApiResponse<T> ok(String message, T data) {
        ApiResponse<T> r = new ApiResponse<>();
        r.code = 0;
        r.message = message;
        r.data = data;
        return r;
    }

    /** 失败响应 */
    public static <T> ApiResponse<T> fail(String message) {
        ApiResponse<T> r = new ApiResponse<>();
        r.code = -1;
        r.message = message;
        return r;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
}
