package com.sls.controller;

import com.sls.common.ApiResponse;
import com.sls.dto.OptionVO;
import com.sls.dto.PageResult;
import com.sls.dto.StudentRequest;
import com.sls.dto.StudentVO;
import com.sls.repository.StudentRepository;
import com.sls.service.StudentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学生信息管理 REST 接口（FR-01）
 */
@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;
    private final StudentRepository studentRepository;

    public StudentController(StudentService studentService, StudentRepository studentRepository) {
        this.studentService = studentService;
        this.studentRepository = studentRepository;
    }

    /** 分页查询学生列表 */
    @GetMapping
    public ApiResponse<PageResult<StudentVO>> page(
            @RequestParam(required = false) String studentNo,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String collegeCode,
            @RequestParam(required = false) String majorCode,
            @RequestParam(required = false) String studentStatus,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.ok(studentService.page(studentNo, name, collegeCode, majorCode,
                studentStatus, page, pageSize));
    }

    /** 按学号查询学生详情 */
    @GetMapping("/{studentNo}")
    public ApiResponse<StudentVO> get(@PathVariable String studentNo) {
        return ApiResponse.ok(studentService.getByStudentNo(studentNo));
    }

    /** 新增学生 */
    @PostMapping
    public ApiResponse<Void> create(@RequestBody StudentRequest request) {
        studentService.create(request);
        return ApiResponse.ok("新增成功", null);
    }

    /** 修改学生（学号不可变） */
    @PutMapping("/{studentNo}")
    public ApiResponse<Void> update(@PathVariable String studentNo, @RequestBody StudentRequest request) {
        studentService.update(studentNo, request);
        return ApiResponse.ok("修改成功", null);
    }

    /** 逻辑删除学生 */
    @DeleteMapping("/{studentNo}")
    public ApiResponse<Void> delete(@PathVariable String studentNo) {
        studentService.delete(studentNo);
        return ApiResponse.ok("删除成功", null);
    }

    /** 学院下拉选项 */
    @GetMapping("/options/colleges")
    public ApiResponse<List<OptionVO>> colleges() {
        return ApiResponse.ok(studentRepository.listColleges());
    }

    /** 专业下拉选项（可按学院筛选） */
    @GetMapping("/options/majors")
    public ApiResponse<List<OptionVO>> majors(@RequestParam(required = false) String collegeCode) {
        return ApiResponse.ok(studentRepository.listMajors(collegeCode));
    }
}
