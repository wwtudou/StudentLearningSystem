package com.sls.service;

import com.sls.common.BusinessException;
import com.sls.dto.OfferingVO;
import com.sls.dto.PageResult;
import com.sls.repository.CourseRepository;
import com.sls.repository.OfferingRepository;
import com.sls.repository.TeacherRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 开课计划管理（FR-05）
 */
@Service
public class OfferingService {

    private static final List<String> STATUSES = List.of("草稿", "开放选课", "选课结束", "已结束");

    private final OfferingRepository offeringRepository;
    private final CourseRepository courseRepository;
    private final TeacherRepository teacherRepository;

    public OfferingService(OfferingRepository offeringRepository, CourseRepository courseRepository,
                           TeacherRepository teacherRepository) {
        this.offeringRepository = offeringRepository;
        this.courseRepository = courseRepository;
        this.teacherRepository = teacherRepository;
    }

    public PageResult<OfferingVO> page(String semesterCode, String courseCode, String status,
                                       int page, int pageSize) {
        if (page < 1) page = 1;
        if (pageSize < 1) pageSize = 10;
        int offset = (page - 1) * pageSize;
        long total = offeringRepository.count(semesterCode, courseCode, status);
        return new PageResult<>(offeringRepository.findPage(semesterCode, courseCode, status, offset, pageSize),
                total, page, pageSize);
    }

    public void create(String offeringNo, String courseCode, String semesterCode, String teacherNo,
                       Integer capacity, String schedule, String status) {
        if (offeringNo == null || offeringNo.isBlank()) throw new BusinessException("计划编号不能为空");
        if (offeringRepository.exists(offeringNo.trim())) throw new BusinessException("计划编号已存在");
        if (courseRepository.findByCode(courseCode) == null) throw new BusinessException("课程不存在");
        if (teacherRepository.findByNo(teacherNo) == null) throw new BusinessException("教师不存在");
        if (capacity == null || capacity <= 0) throw new BusinessException("容量须大于0");
        String st = status != null && !status.isBlank() ? status : "草稿";
        if (!STATUSES.contains(st)) throw new BusinessException("开课状态不合法");
        offeringRepository.insert(offeringNo.trim(), courseCode, semesterCode, teacherNo, capacity, schedule, st);
    }

    public void update(String offeringNo, String teacherNo, Integer capacity, String schedule, String status) {
        OfferingVO existing = offeringRepository.findByNo(offeringNo);
        if (existing == null) throw new BusinessException("开课计划不存在");
        if (teacherRepository.findByNo(teacherNo) == null) throw new BusinessException("教师不存在");
        if (capacity == null || capacity < existing.getEnrolledCount()) {
            throw new BusinessException("容量不能小于已选人数");
        }
        if (status == null || !STATUSES.contains(status)) throw new BusinessException("开课状态不合法");
        offeringRepository.update(offeringNo, teacherNo, capacity, schedule, status);
    }
}
