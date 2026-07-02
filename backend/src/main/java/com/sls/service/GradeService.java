package com.sls.service;

import com.sls.common.BusinessException;
import com.sls.dto.GradeVO;
import com.sls.dto.PageResult;
import com.sls.repository.EnrollmentRepository;
import com.sls.repository.GradeRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 成绩管理（FR-06）
 */
@Service
public class GradeService {

    private static final List<String> EXAM_TYPES = List.of("正常", "补考", "重修");

    private final GradeRepository gradeRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final EnrollmentService enrollmentService;

    public GradeService(GradeRepository gradeRepository, EnrollmentRepository enrollmentRepository,
                        @Lazy EnrollmentService enrollmentService) {
        this.gradeRepository = gradeRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.enrollmentService = enrollmentService;
    }

    public PageResult<GradeVO> page(String studentNo, String offeringNo, int page, int pageSize) {
        if (page < 1) page = 1;
        if (pageSize < 1) pageSize = 10;
        int offset = (page - 1) * pageSize;
        long total = gradeRepository.count(studentNo, offeringNo);
        return new PageResult<>(gradeRepository.findPage(studentNo, offeringNo, offset, pageSize),
                total, page, pageSize);
    }

    public void save(String studentNo, String offeringNo, BigDecimal totalScore, String examType) {
        if (!enrollmentRepository.exists(studentNo, offeringNo)) {
            throw new BusinessException("选课记录不存在");
        }
        GradeVO existing = gradeRepository.findOne(studentNo, offeringNo);
        if (existing != null && existing.isLocked()) {
            throw new BusinessException("成绩已提交，请联系管理员");
        }
        validateScore(totalScore);
        String type = examType != null && !examType.isBlank() ? examType : "正常";
        if (!EXAM_TYPES.contains(type)) throw new BusinessException("考试类型不合法");
        int year = LocalDate.now().getYear();
        gradeRepository.upsert(studentNo, offeringNo, totalScore, type, year);
    }

    public void submitOffering(String offeringNo) {
        gradeRepository.lockOffering(offeringNo);
    }

    public void arrangeMakeup(String studentNo, String offeringNo) {
        GradeVO g = gradeRepository.findOne(studentNo, offeringNo);
        if (g == null || g.getTotalScore() == null || g.getTotalScore().doubleValue() >= 60) {
            throw new BusinessException("仅不及格记录可安排补考");
        }
        gradeRepository.setMakeup(studentNo, offeringNo);
    }

    public void arrangeRetake(String studentNo, String offeringNo) {
        enrollmentService.enroll(studentNo, offeringNo, true);
    }

    private void validateScore(BigDecimal score) {
        if (score == null || score.doubleValue() < 0 || score.doubleValue() > 100) {
            throw new BusinessException("成绩须在0到100之间");
        }
    }
}
