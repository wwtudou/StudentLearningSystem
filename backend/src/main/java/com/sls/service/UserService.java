package com.sls.service;

import com.sls.common.BusinessException;
import com.sls.dto.PageResult;
import com.sls.dto.UserRequest;
import com.sls.dto.UserVO;
import com.sls.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户权限管理（FR-08）
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final AuthService authService;

    public UserService(UserRepository userRepository, AuthService authService) {
        this.userRepository = userRepository;
        this.authService = authService;
    }

    public PageResult<UserVO> page(String username, String realName, int page, int pageSize) {
        if (page < 1) page = 1;
        if (pageSize < 1) pageSize = 10;
        int offset = (page - 1) * pageSize;
        long total = userRepository.countUsers(username, realName);
        return new PageResult<>(userRepository.findUserPage(username, realName, offset, pageSize),
                total, page, pageSize);
    }

    @Transactional
    public void create(UserRequest req) {
        if (req.getUsername() == null || req.getUsername().isBlank()) {
            throw new BusinessException("用户名不能为空");
        }
        if (req.getPassword() == null || req.getPassword().length() < 6) {
            throw new BusinessException("密码至少6位");
        }
        if (userRepository.exists(req.getUsername().trim())) {
            throw new BusinessException("用户名已存在");
        }
        String hash = authService.getPasswordEncoder().encode(req.getPassword());
        userRepository.insert(req.getUsername().trim(), hash, req.getRealName().trim());
        saveRoles(req.getUsername().trim(), req.getRoles());
    }

    @Transactional
    public void update(String username, UserRequest req) {
        if (!userRepository.exists(username)) throw new BusinessException("用户不存在");
        if (req.getRealName() == null || req.getRealName().isBlank()) {
            throw new BusinessException("真实姓名不能为空");
        }
        String status = req.getStatus() != null ? req.getStatus() : "启用";
        if (!status.equals("启用") && !status.equals("停用")) {
            throw new BusinessException("状态不合法");
        }
        if ("停用".equals(status) && isLastSysAdmin(username, req.getRoles())) {
            throw new BusinessException("系统至少保留一个管理员");
        }
        userRepository.update(username, req.getRealName().trim(), status);
        if (req.getRoles() != null) {
            userRepository.deleteRoles(username);
            saveRoles(username, req.getRoles());
        }
    }

    public void resetPassword(String username, String newPassword) {
        if (!userRepository.exists(username)) throw new BusinessException("用户不存在");
        if (newPassword == null || newPassword.length() < 6) {
            throw new BusinessException("密码至少6位");
        }
        userRepository.updatePassword(username, authService.getPasswordEncoder().encode(newPassword));
    }

    private void saveRoles(String username, List<String> roles) {
        if (roles == null || roles.isEmpty()) return;
        for (String role : roles) {
            userRepository.insertRole(username, role);
        }
    }

    private boolean isLastSysAdmin(String username, List<String> newRoles) {
        List<String> current = userRepository.listRoles(username);
        boolean wasAdmin = current.contains("SYS_ADMIN");
        boolean willBeAdmin = newRoles != null && newRoles.contains("SYS_ADMIN");
        if (wasAdmin && !willBeAdmin && userRepository.countSysAdmins() <= 1) {
            return true;
        }
        return false;
    }
}
