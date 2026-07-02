package com.sls.service;

import com.sls.common.BusinessException;
import com.sls.dto.CourseVO;
import com.sls.dto.PageResult;
import com.sls.repository.CourseRepository;
import com.sls.repository.OrgRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 课程库管理（FR-05）
 */
@Service
public class CourseService {

    private static final List<String> NATURES = List.of("必修", "选修", "公选");

    private final CourseRepository courseRepository;
    private final OrgRepository orgRepository;

    public CourseService(CourseRepository courseRepository, OrgRepository orgRepository) {
        this.courseRepository = courseRepository;
        this.orgRepository = orgRepository;
    }

    public PageResult<CourseVO> page(String courseCode, String courseName, String collegeCode,
                                     int page, int pageSize) {
        if (page < 1) page = 1;
        if (pageSize < 1) pageSize = 10;
        int offset = (page - 1) * pageSize;
        long total = courseRepository.count(courseCode, courseName, collegeCode);
        return new PageResult<>(courseRepository.findPage(courseCode, courseName, collegeCode, offset, pageSize),
                total, page, pageSize);
    }

    public void create(String code, String name, BigDecimal credit, Integer hours,
                       String collegeCode, String nature) {
        validate(code, name, credit, hours, collegeCode, nature);
        if (courseRepository.exists(code.trim())) throw new BusinessException("课程编号已存在");
        courseRepository.insert(code.trim(), name.trim(), credit, hours, collegeCode, nature);
    }

    public void update(String code, String name, BigDecimal credit, Integer hours,
                       String collegeCode, String nature, String status) {
        if (courseRepository.findByCode(code) == null) throw new BusinessException("课程不存在");
        validate(code, name, credit, hours, collegeCode, nature);
        if (status == null || (!status.equals("启用") && !status.equals("停用"))) {
            throw new BusinessException("状态须为启用或停用");
        }
        courseRepository.update(code, name.trim(), credit, hours, collegeCode, nature, status);
    }

    private void validate(String code, String name, BigDecimal credit, Integer hours,
                          String collegeCode, String nature) {
        if (code == null || code.isBlank()) throw new BusinessException("课程编号不能为空");
        if (name == null || name.isBlank()) throw new BusinessException("课程名称不能为空");
        if (credit == null || credit.compareTo(BigDecimal.ZERO) <= 0) throw new BusinessException("学分须大于0");
        if (hours == null || hours <= 0) throw new BusinessException("学时须大于0");
        if (orgRepository.findCollege(collegeCode) == null) throw new BusinessException("学院不存在");
        if (nature == null || !NATURES.contains(nature)) throw new BusinessException("课程性质不合法");
    }
}
