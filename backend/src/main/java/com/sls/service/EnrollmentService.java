package com.sls.service;

import com.sls.common.BusinessException;
import com.sls.dto.EnrollmentVO;
import com.sls.dto.PageResult;
import com.sls.repository.EnrollmentRepository;
import com.sls.repository.GradeRepository;
import com.sls.repository.OfferingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * 选课管理（FR-06）
 */
@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final OfferingRepository offeringRepository;
    private final GradeRepository gradeRepository;

    public EnrollmentService(EnrollmentRepository enrollmentRepository,
                             OfferingRepository offeringRepository,
                             GradeRepository gradeRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.offeringRepository = offeringRepository;
        this.gradeRepository = gradeRepository;
    }

    public PageResult<EnrollmentVO> page(String studentNo, String offeringNo, int page, int pageSize) {
        if (page < 1) page = 1;
        if (pageSize < 1) pageSize = 10;
        int offset = (page - 1) * pageSize;
        long total = enrollmentRepository.count(studentNo, offeringNo);
        return new PageResult<>(enrollmentRepository.findPage(studentNo, offeringNo, offset, pageSize),
                total, page, pageSize);
    }

    @Transactional
    public void enroll(String studentNo, String offeringNo, boolean retake) {
        String status = enrollmentRepository.findStudentStatus(studentNo);
        if (status == null) throw new BusinessException("学生不存在");
        if (!"在读".equals(status) && !retake) {
            throw new BusinessException("当前学籍状态不允许选课");
        }
        if (enrollmentRepository.exists(studentNo, offeringNo)) {
            throw new BusinessException("您已选择该课程");
        }
        Map<String, Object> offering = offeringRepository.findForUpdate(offeringNo);
        if (offering == null) throw new BusinessException("开课计划不存在");
        if (!retake && !"开放选课".equals(offering.get("status"))) {
            throw new BusinessException("该开课计划未开放选课");
        }
        int capacity = ((Number) offering.get("capacity")).intValue();
        int enrolled = ((Number) offering.get("enrolled_count")).intValue();
        if (enrolled >= capacity) throw new BusinessException("该课程已满员，请选择其他教学班");
        enrollmentRepository.insert(studentNo, offeringNo, retake);
        offeringRepository.incrementEnrolled(offeringNo);
    }

    @Transactional
    public void drop(String studentNo, String offeringNo) {
        if (!enrollmentRepository.exists(studentNo, offeringNo)) {
            throw new BusinessException("未找到选课记录");
        }
        var offering = offeringRepository.findByNo(offeringNo);
        if (offering == null) throw new BusinessException("开课计划不存在");
        if (!"开放选课".equals(offering.getStatus())) {
            throw new BusinessException("当前不在退课期内");
        }
        gradeRepository.deleteByEnrollment(studentNo, offeringNo);
        enrollmentRepository.delete(studentNo, offeringNo);
        offeringRepository.decrementEnrolled(offeringNo);
    }
}
