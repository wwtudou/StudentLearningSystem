package com.sls.controller;

import com.sls.common.ApiResponse;
import com.sls.dto.GradeVO;
import com.sls.dto.OfferingVO;
import com.sls.dto.PageResult;
import com.sls.service.GradeService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 成绩接口（FR-06）
 */
@RestController
@RequestMapping("/api/grades")
public class GradeController {

    private final GradeService gradeService;

    public GradeController(GradeService gradeService) {
        this.gradeService = gradeService;
    }

    @GetMapping
    public ApiResponse<PageResult<GradeVO>> page(
            @RequestParam(required = false) String studentNo,
            @RequestParam(required = false) String offeringNo,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.ok(gradeService.page(studentNo, offeringNo, page, pageSize));
    }

    /** 教师：本人授课的开课计划列表 */
    @GetMapping("/my-offerings")
    public ApiResponse<List<OfferingVO>> myOfferings() {
        return ApiResponse.ok(gradeService.myOfferings());
    }

    /** 教师：某班学生名册（含成绩，未录入者为空） */
    @GetMapping("/roster")
    public ApiResponse<List<GradeVO>> roster(@RequestParam String offeringNo) {
        return ApiResponse.ok(gradeService.roster(offeringNo));
    }

    @PostMapping
    public ApiResponse<Void> save(@RequestBody Map<String, Object> body) {
        gradeService.save(
                (String) body.get("studentNo"),
                (String) body.get("offeringNo"),
                new BigDecimal(body.get("totalScore").toString()),
                (String) body.get("examType"));
        return ApiResponse.ok("保存成功", null);
    }

    @PostMapping("/submit")
    public ApiResponse<Void> submit(@RequestBody Map<String, String> body) {
        gradeService.submitOffering(body.get("offeringNo"));
        return ApiResponse.ok("成绩已提交锁定", null);
    }

    @PostMapping("/makeup")
    public ApiResponse<Void> makeup(@RequestBody Map<String, String> body) {
        gradeService.arrangeMakeup(body.get("studentNo"), body.get("offeringNo"));
        return ApiResponse.ok("已安排补考", null);
    }

    @PostMapping("/retake")
    public ApiResponse<Void> retake(@RequestBody Map<String, String> body) {
        gradeService.arrangeRetake(body.get("studentNo"), body.get("offeringNo"));
        return ApiResponse.ok("重修选课成功", null);
    }

    /** 批量保存成绩（存储过程） */
    @PostMapping("/batch")
    public ApiResponse<Map<String, Object>> batch(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> grades = (List<Map<String, Object>>) body.get("grades");
        int count = gradeService.batchSave((String) body.get("offeringNo"), grades);
        return ApiResponse.ok("批量保存成功", Map.of("count", count));
    }
}
