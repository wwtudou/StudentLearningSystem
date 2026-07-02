package com.sls.controller;

import com.sls.common.ApiResponse;
import com.sls.dto.OptionVO;
import com.sls.dto.PageResult;
import com.sls.dto.UserRequest;
import com.sls.dto.UserVO;
import com.sls.repository.UserRepository;
import com.sls.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户管理接口（FR-08）
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    public UserController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ApiResponse<PageResult<UserVO>> page(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String realName,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.ok(userService.page(username, realName, page, pageSize));
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestBody UserRequest request) {
        userService.create(request);
        return ApiResponse.ok("创建成功", null);
    }

    @PutMapping("/{username}")
    public ApiResponse<Void> update(@PathVariable String username, @RequestBody UserRequest request) {
        userService.update(username, request);
        return ApiResponse.ok("修改成功", null);
    }

    @PostMapping("/{username}/reset-password")
    public ApiResponse<Void> resetPassword(@PathVariable String username, @RequestBody Map<String, String> body) {
        userService.resetPassword(username, body.get("password"));
        return ApiResponse.ok("密码已重置", null);
    }

    @GetMapping("/options/roles")
    public ApiResponse<List<OptionVO>> roles() {
        return ApiResponse.ok(userRepository.listRoleOptions());
    }
}
