package com.sls.controller;

import com.sls.common.ApiResponse;
import com.sls.dto.ExplainRowVO;
import com.sls.dto.GradeChangeLogVO;
import com.sls.dto.WindowStatVO;
import com.sls.service.DbTechService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 高阶数据库技术演示接口（DB-Tech-02/04/06/07）
 */
@RestController
@RequestMapping("/api/db-tech")
public class DbTechController {

    private final DbTechService dbTechService;

    public DbTechController(DbTechService dbTechService) {
        this.dbTechService = dbTechService;
    }

    /** DB-Tech-06 专业内排名 */
    @GetMapping("/rank")
    public ApiResponse<List<WindowStatVO>> rank(@RequestParam(required = false) String semesterCode) {
        return ApiResponse.ok(dbTechService.majorRank(semesterCode));
    }

    /** DB-Tech-06 累计学分 */
    @GetMapping("/cumulative-credit")
    public ApiResponse<List<WindowStatVO>> cumulativeCredit() {
        return ApiResponse.ok(dbTechService.cumulativeCredit());
    }

    /** DB-Tech-02 成绩变更日志 */
    @GetMapping("/grade-change-logs")
    public ApiResponse<List<GradeChangeLogVO>> gradeChangeLogs(@RequestParam(required = false) String studentNo) {
        return ApiResponse.ok(dbTechService.gradeChangeLogs(studentNo));
    }

    /** DB-Tech-07 audit_log 分区裁剪 EXPLAIN */
    @GetMapping("/explain/audit-partition")
    public ApiResponse<List<ExplainRowVO>> explainAudit(@RequestParam(defaultValue = "2024") int year) {
        return ApiResponse.ok(dbTechService.explainAuditPartition(year));
    }

    /** DB-Tech-04 grade 索引 EXPLAIN */
    @GetMapping("/explain/grade-index")
    public ApiResponse<List<ExplainRowVO>> explainGradeIndex(@RequestParam String offeringNo) {
        return ApiResponse.ok(dbTechService.explainGradeIndex(offeringNo));
    }

    /** DB-Tech-04 选课索引 EXPLAIN */
    @GetMapping("/explain/enrollment-index")
    public ApiResponse<List<ExplainRowVO>> explainEnrollment(@RequestParam String offeringNo) {
        return ApiResponse.ok(dbTechService.explainEnrollmentIndex(offeringNo));
    }
}
