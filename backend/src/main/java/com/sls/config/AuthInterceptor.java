package com.sls.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sls.common.ApiResponse;
import com.sls.common.ForbiddenException;
import com.sls.dto.SessionUser;
import com.sls.security.RbacService;
import com.sls.security.UserContext;
import com.sls.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录拦截 + RBAC 权限校验
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RbacService rbacService;

    public AuthInterceptor(RbacService rbacService) {
        this.rbacService = rbacService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        HttpSession session = request.getSession(false);
        Object attr = session != null ? session.getAttribute(AuthService.SESSION_KEY) : null;
        if (!(attr instanceof SessionUser user)) {
            writeJson(response, HttpServletResponse.SC_UNAUTHORIZED, ApiResponse.fail("请先登录"));
            return false;
        }
        UserContext.set(user);
        try {
            rbacService.checkAccess(user, request.getMethod(), request.getRequestURI());
        } catch (ForbiddenException e) {
            writeJson(response, HttpServletResponse.SC_FORBIDDEN, ApiResponse.fail(e.getMessage()));
            UserContext.clear();
            return false;
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        UserContext.clear();
    }

    private void writeJson(HttpServletResponse response, int status, ApiResponse<?> body) throws Exception {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
