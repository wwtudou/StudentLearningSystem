package com.sls.controller;

import com.sls.common.ApiResponse;
import com.sls.common.BusinessException;
import com.sls.dto.LoginRequest;
import com.sls.dto.SessionUser;
import com.sls.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

/**
 * 登录认证接口（FR-08）
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<SessionUser> login(@RequestBody LoginRequest request, HttpSession session) {
        return ApiResponse.ok("登录成功", authService.login(request, session));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpSession session) {
        authService.logout(session);
        return ApiResponse.ok("已退出", null);
    }

    @GetMapping("/me")
    public ApiResponse<SessionUser> me(HttpSession session) {
        SessionUser user = authService.currentUser(session);
        if (user == null) {
            throw new BusinessException("未登录");
        }
        return ApiResponse.ok(user);
    }
}
