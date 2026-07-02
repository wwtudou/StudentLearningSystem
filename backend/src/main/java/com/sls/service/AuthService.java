package com.sls.service;

import com.sls.common.BusinessException;
import com.sls.dto.LoginRequest;
import com.sls.dto.SessionUser;
import com.sls.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 登录认证服务
 */
@Service
public class AuthService {

    public static final String SESSION_KEY = "SLMS_USER";

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public SessionUser login(LoginRequest req, HttpSession session) {
        if (req.getUsername() == null || req.getUsername().isBlank()
                || req.getPassword() == null || req.getPassword().isBlank()) {
            throw new BusinessException("用户名或密码错误");
        }
        String hash = userRepository.findPasswordHash(req.getUsername().trim());
        if (hash == null || !passwordEncoder.matches(req.getPassword(), hash)) {
            throw new BusinessException("用户名或密码错误");
        }
        SessionUser user = userRepository.findSessionUser(req.getUsername().trim());
        if (user == null) {
            throw new BusinessException("用户已停用");
        }
        session.setAttribute(SESSION_KEY, user);
        return user;
    }

    public void logout(HttpSession session) {
        session.invalidate();
    }

    public SessionUser currentUser(HttpSession session) {
        Object obj = session.getAttribute(SESSION_KEY);
        return obj instanceof SessionUser ? (SessionUser) obj : null;
    }

    public BCryptPasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }
}
