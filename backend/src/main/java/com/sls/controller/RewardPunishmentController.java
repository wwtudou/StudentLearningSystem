package com.sls.controller;

import com.sls.common.ApiResponse;
import com.sls.dto.PageResult;
import com.sls.dto.RewardPunishmentVO;
import com.sls.dto.SessionUser;
import com.sls.service.AuthService;
import com.sls.service.RewardPunishmentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 奖惩管理接口（FR-02）
 */
@RestController
@RequestMapping("/api/rewards")
public class RewardPunishmentController {

    private final RewardPunishmentService service;
    private final AuthService authService;

    public RewardPunishmentController(RewardPunishmentService service, AuthService authService) {
        this.service = service;
        this.authService = authService;
    }

    @GetMapping
    public ApiResponse<PageResult<RewardPunishmentVO>> page(
            @RequestParam(required = false) String studentNo,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.ok(service.page(studentNo, type, level, startDate, endDate, page, pageSize));
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestBody Map<String, String> body, HttpSession session) {
        SessionUser user = authService.currentUser(session);
        service.create(body.get("studentNo"), body.get("type"), body.get("level"),
                body.get("reason"), body.get("occurDate"), user.getUsername());
        return ApiResponse.ok("登记成功", null);
    }

    @PutMapping("/{recordNo}")
    public ApiResponse<Void> update(@PathVariable String recordNo, @RequestBody Map<String, String> body) {
        service.update(recordNo, body.get("type"), body.get("level"), body.get("reason"), body.get("occurDate"));
        return ApiResponse.ok("修改成功", null);
    }

    @PostMapping("/{recordNo}/archive")
    public ApiResponse<Void> archive(@PathVariable String recordNo) {
        service.archive(recordNo);
        return ApiResponse.ok("归档成功", null);
    }
}
