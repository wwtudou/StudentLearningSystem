package com.sls.controller;

import com.sls.common.ApiResponse;
import com.sls.dto.EnrollmentVO;
import com.sls.dto.PageResult;
import com.sls.service.EnrollmentService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 选课接口（FR-06）
 */
@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @GetMapping
    public ApiResponse<PageResult<EnrollmentVO>> page(
            @RequestParam(required = false) String studentNo,
            @RequestParam(required = false) String offeringNo,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.ok(enrollmentService.page(studentNo, offeringNo, page, pageSize));
    }

    @PostMapping
    public ApiResponse<Void> enroll(@RequestBody Map<String, Object> body) {
        boolean retake = Boolean.TRUE.equals(body.get("retake"));
        enrollmentService.enroll((String) body.get("studentNo"), (String) body.get("offeringNo"), retake);
        return ApiResponse.ok("选课成功", null);
    }

    @DeleteMapping
    public ApiResponse<Void> drop(@RequestParam String studentNo, @RequestParam String offeringNo) {
        enrollmentService.drop(studentNo, offeringNo);
        return ApiResponse.ok("退课成功", null);
    }
}
