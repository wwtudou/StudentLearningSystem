package com.sls.controller;

import com.sls.common.ApiResponse;
import com.sls.dto.OptionVO;
import com.sls.dto.PageResult;
import com.sls.dto.TeacherVO;
import com.sls.repository.TeacherRepository;
import com.sls.service.TeacherService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 教师管理接口
 */
@RestController
@RequestMapping("/api/teachers")
public class TeacherController {

    private final TeacherService teacherService;
    private final TeacherRepository teacherRepository;

    public TeacherController(TeacherService teacherService, TeacherRepository teacherRepository) {
        this.teacherService = teacherService;
        this.teacherRepository = teacherRepository;
    }

    @GetMapping
    public ApiResponse<PageResult<TeacherVO>> page(
            @RequestParam(required = false) String teacherNo,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String collegeCode,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.ok(teacherService.page(teacherNo, name, collegeCode, page, pageSize));
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestBody Map<String, String> body) {
        teacherService.create(body.get("teacherNo"), body.get("name"), body.get("collegeCode"));
        return ApiResponse.ok("新增成功", null);
    }

    @PutMapping("/{teacherNo}")
    public ApiResponse<Void> update(@PathVariable String teacherNo, @RequestBody Map<String, String> body) {
        teacherService.update(teacherNo, body.get("name"), body.get("collegeCode"), body.get("status"));
        return ApiResponse.ok("修改成功", null);
    }

    @GetMapping("/options")
    public ApiResponse<List<OptionVO>> options(@RequestParam(required = false) String collegeCode) {
        return ApiResponse.ok(teacherRepository.options(collegeCode));
    }
}
