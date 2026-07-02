package com.sls.controller;

import com.sls.common.ApiResponse;
import com.sls.dto.StatRowVO;
import com.sls.service.ReportService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 报表统计接口（FR-07）
 */
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/students")
    public ApiResponse<List<StatRowVO>> students(@RequestParam(defaultValue = "college") String groupBy) {
        return ApiResponse.ok(reportService.studentCount(groupBy));
    }

    @GetMapping("/enrollments")
    public ApiResponse<List<StatRowVO>> enrollments() {
        return ApiResponse.ok(reportService.enrollmentCount());
    }

    @GetMapping("/grades")
    public ApiResponse<List<StatRowVO>> grades(@RequestParam(required = false) String offeringNo) {
        return ApiResponse.ok(reportService.gradeStats(offeringNo));
    }

    @GetMapping("/rewards")
    public ApiResponse<List<StatRowVO>> rewards() {
        return ApiResponse.ok(reportService.rewardStats());
    }
}
