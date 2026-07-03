package com.sls.service;

import com.sls.common.BusinessException;
import com.sls.dto.EnrollmentVO;
import com.sls.dto.OfferingVO;
import com.sls.dto.PageResult;
import com.sls.repository.EnrollmentRepository;
import com.sls.repository.OfferingRepository;
import com.sls.security.AccessScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 选课管理（FR-06）：应用层事务 + 触发器维护人数；存储过程见 db/procedures.sql 供答辩演示
 */
@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final OfferingRepository offeringRepository;
    private final AccessScope accessScope;

    public EnrollmentService(EnrollmentRepository enrollmentRepository,
                             OfferingRepository offeringRepository,
                             AccessScope accessScope) {
        this.enrollmentRepository = enrollmentRepository;
        this.offeringRepository = offeringRepository;
        this.accessScope = accessScope;
    }

    public PageResult<EnrollmentVO> page(String studentNo, String offeringNo, int page, int pageSize) {
        studentNo = accessScope.resolveStudentNoFilter(studentNo);
        if (page < 1) page = 1;
        if (pageSize < 1) pageSize = 10;
        int offset = (page - 1) * pageSize;
        long total = enrollmentRepository.count(studentNo, offeringNo);
        return new PageResult<>(enrollmentRepository.findPage(studentNo, offeringNo, offset, pageSize),
                total, page, pageSize);
    }

    @Transactional
    public void enroll(String studentNo, String offeringNo, boolean retake) {
        accessScope.assertStudentSelf(studentNo);
        OfferingVO offering = offeringRepository.findByNo(offeringNo);
        if (offering == null) {
            throw new BusinessException("开课计划不存在");
        }
        String stuStatus = enrollmentRepository.findStudentStatus(studentNo);
        if (stuStatus == null) {
            throw new BusinessException("学生不存在");
        }
        if (!retake && !"在读".equals(stuStatus)) {
            throw new BusinessException("当前学籍状态不允许选课");
        }
        if (!retake && !"开放选课".equals(offering.getStatus())) {
            throw new BusinessException("该开课计划未开放选课");
        }
        if (enrollmentRepository.exists(studentNo, offeringNo)) {
            throw new BusinessException("您已选择该课程");
        }
        if (offering.getEnrolledCount() != null && offering.getCapacity() != null
                && offering.getEnrolledCount() >= offering.getCapacity()) {
            throw new BusinessException("该课程已满员，请选择其他教学班");
        }
        if (offering.getSchedule() != null && !offering.getSchedule().isBlank()
                && enrollmentRepository.hasScheduleConflict(
                studentNo, offering.getSemesterCode(), offering.getSchedule())) {
            throw new BusinessException("与已选课程时间冲突");
        }
        enrollmentRepository.insert(studentNo, offeringNo, retake);
    }

    @Transactional
    public void drop(String studentNo, String offeringNo) {
        accessScope.assertStudentSelf(studentNo);
        if (!enrollmentRepository.exists(studentNo, offeringNo)) {
            throw new BusinessException("未找到选课记录");
        }
        OfferingVO offering = offeringRepository.findByNo(offeringNo);
        if (offering != null && ("已结束".equals(offering.getStatus()) || "草稿".equals(offering.getStatus()))) {
            throw new BusinessException("当前不在退课期内");
        }
        enrollmentRepository.deleteGrade(studentNo, offeringNo);
        enrollmentRepository.delete(studentNo, offeringNo);
    }
}
