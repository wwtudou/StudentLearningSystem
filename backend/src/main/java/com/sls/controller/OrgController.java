package com.sls.controller;

import com.sls.common.ApiResponse;
import com.sls.dto.CollegeVO;
import com.sls.dto.MajorVO;
import com.sls.dto.OptionVO;
import com.sls.service.OrgService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 院系专业管理接口（FR-03）
 */
@RestController
@RequestMapping("/api/org")
public class OrgController {

    private final OrgService orgService;

    public OrgController(OrgService orgService) {
        this.orgService = orgService;
    }

    @GetMapping("/tree")
    public ApiResponse<List<CollegeVO>> tree() {
        return ApiResponse.ok(orgService.tree());
    }

    @GetMapping("/colleges")
    public ApiResponse<List<CollegeVO>> colleges() {
        return ApiResponse.ok(orgService.listColleges());
    }

    @PostMapping("/colleges")
    public ApiResponse<Void> createCollege(@RequestBody Map<String, String> body) {
        orgService.createCollege(body.get("collegeCode"), body.get("collegeName"));
        return ApiResponse.ok("新增成功", null);
    }

    @PutMapping("/colleges/{collegeCode}")
    public ApiResponse<Void> updateCollege(@PathVariable String collegeCode, @RequestBody Map<String, String> body) {
        orgService.updateCollege(collegeCode, body.get("collegeName"), body.get("status"));
        return ApiResponse.ok("修改成功", null);
    }

    @GetMapping("/majors")
    public ApiResponse<List<MajorVO>> majors(@RequestParam(required = false) String collegeCode) {
        return ApiResponse.ok(orgService.listMajors(collegeCode));
    }

    @PostMapping("/majors")
    public ApiResponse<Void> createMajor(@RequestBody Map<String, String> body) {
        orgService.createMajor(body.get("majorCode"), body.get("majorName"), body.get("collegeCode"));
        return ApiResponse.ok("新增成功", null);
    }

    @PutMapping("/majors/{majorCode}")
    public ApiResponse<Void> updateMajor(@PathVariable String majorCode, @RequestBody Map<String, String> body) {
        orgService.updateMajor(majorCode, body.get("majorName"), body.get("collegeCode"), body.get("status"));
        return ApiResponse.ok("修改成功", null);
    }

    @GetMapping("/options/colleges")
    public ApiResponse<List<OptionVO>> collegeOptions() {
        return ApiResponse.ok(orgService.collegeOptions());
    }
}
