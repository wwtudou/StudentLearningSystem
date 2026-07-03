package com.sls.common;

/**
 * 无权限访问（HTTP 403）
 */
public class ForbiddenException extends BusinessException {

    public ForbiddenException(String message) {
        super(message);
    }
}
