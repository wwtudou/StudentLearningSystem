package com.sls.common;

/**
 * 业务异常：校验失败、数据不存在等可预期错误
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
