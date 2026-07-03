package com.sls.security;

import com.sls.common.ForbiddenException;
import com.sls.dto.SessionUser;

/**
 * 当前请求登录用户（由拦截器写入）
 */
public final class UserContext {

    private static final ThreadLocal<SessionUser> CURRENT = new ThreadLocal<>();

    private UserContext() {}

    public static void set(SessionUser user) {
        CURRENT.set(user);
    }

    public static SessionUser get() {
        return CURRENT.get();
    }

    public static SessionUser requireUser() {
        SessionUser user = CURRENT.get();
        if (user == null) {
            throw new ForbiddenException("未登录或会话已失效");
        }
        return user;
    }

    public static void clear() {
        CURRENT.remove();
    }
}
