package com.sls.controller;

import com.sls.common.ApiResponse;
import com.sls.dto.CourseVO;
import com.sls.dto.OfferingVO;
import com.sls.dto.PageResult;
import com.sls.dto.SemesterVO;
import com.sls.repository.CourseRepository;
import com.sls.repository.OfferingRepository;
import com.sls.service.CourseService;
import com.sls.service.OfferingService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 课程与开课计划接口（FR-05）
 */
@RestController
@RequestMapping("/api")
public class CourseController {

    private final CourseService courseService;
    private final OfferingService offeringService;
    private final CourseRepository courseRepository;
    private final OfferingRepository offeringRepository;

    public CourseController(CourseService courseService, OfferingService offeringService,
                            CourseRepository courseRepository, OfferingRepository offeringRepository) {
        this.courseService = courseService;
        this.offeringService = offeringService;
        this.courseRepository = courseRepository;
        this.offeringRepository = offeringRepository;
    }

    @GetMapping("/courses")
    public ApiResponse<PageResult<CourseVO>> courses(
            @RequestParam(required = false) String courseCode,
            @RequestParam(required = false) String courseName,
            @RequestParam(required = false) String collegeCode,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.ok(courseService.page(courseCode, courseName, collegeCode, page, pageSize));
    }

    @PostMapping("/courses")
    public ApiResponse<Void> createCourse(@RequestBody Map<String, Object> body) {
        courseService.create(
                (String) body.get("courseCode"),
                (String) body.get("courseName"),
                new BigDecimal(body.get("credit").toString()),
                ((Number) body.get("hours")).intValue(),
                (String) body.get("collegeCode"),
                (String) body.get("nature"));
        return ApiResponse.ok("新增成功", null);
    }

    @PutMapping("/courses/{courseCode}")
    public ApiResponse<Void> updateCourse(@PathVariable String courseCode, @RequestBody Map<String, Object> body) {
        courseService.update(
                courseCode,
                (String) body.get("courseName"),
                new BigDecimal(body.get("credit").toString()),
                ((Number) body.get("hours")).intValue(),
                (String) body.get("collegeCode"),
                (String) body.get("nature"),
                (String) body.get("status"));
        return ApiResponse.ok("修改成功", null);
    }

    @GetMapping("/courses/options")
    public ApiResponse<List<com.sls.dto.OptionVO>> courseOptions() {
        return ApiResponse.ok(courseRepository.options());
    }

    @GetMapping("/semesters")
    public ApiResponse<List<SemesterVO>> semesters() {
        return ApiResponse.ok(offeringRepository.listSemesters());
    }

    @GetMapping("/offerings")
    public ApiResponse<PageResult<OfferingVO>> offerings(
            @RequestParam(required = false) String semesterCode,
            @RequestParam(required = false) String courseCode,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.ok(offeringService.page(semesterCode, courseCode, status, page, pageSize));
    }

    @PostMapping("/offerings")
    public ApiResponse<Void> createOffering(@RequestBody Map<String, Object> body) {
        offeringService.create(
                (String) body.get("offeringNo"),
                (String) body.get("courseCode"),
                (String) body.get("semesterCode"),
                (String) body.get("teacherNo"),
                ((Number) body.get("capacity")).intValue(),
                (String) body.get("schedule"),
                (String) body.get("status"));
        return ApiResponse.ok("新增成功", null);
    }

    @PutMapping("/offerings/{offeringNo}")
    public ApiResponse<Void> updateOffering(@PathVariable String offeringNo, @RequestBody Map<String, Object> body) {
        offeringService.update(
                offeringNo,
                (String) body.get("teacherNo"),
                ((Number) body.get("capacity")).intValue(),
                (String) body.get("schedule"),
                (String) body.get("status"));
        return ApiResponse.ok("修改成功", null);
    }
}
