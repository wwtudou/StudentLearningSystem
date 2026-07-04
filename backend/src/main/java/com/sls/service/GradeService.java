package com.sls.service;

import com.sls.common.BusinessException;
import com.sls.dto.GradeVO;
import com.sls.dto.OfferingVO;
import com.sls.dto.PageResult;
import com.sls.repository.EnrollmentRepository;
import com.sls.repository.GradeRepository;
import com.sls.repository.OfferingRepository;
import com.sls.repository.StoredProcedureRepository;
import com.sls.repository.StoredProcedureRepository.SpResult;
import com.sls.security.AccessScope;
import com.sls.security.UserContext;
import com.sls.dto.SessionUser;
import com.sls.common.ForbiddenException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 成绩管理（FR-06）
 */
@Service
public class GradeService {

    private static final List<String> EXAM_TYPES = List.of("正常", "补考", "重修");

    private final GradeRepository gradeRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final OfferingRepository offeringRepository;
    private final EnrollmentService enrollmentService;
    private final StoredProcedureRepository storedProcedureRepository;
    private final AccessScope accessScope;

    public GradeService(GradeRepository gradeRepository, EnrollmentRepository enrollmentRepository,
                        OfferingRepository offeringRepository,
                        @Lazy EnrollmentService enrollmentService,
                        StoredProcedureRepository storedProcedureRepository,
                        AccessScope accessScope) {
        this.gradeRepository = gradeRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.offeringRepository = offeringRepository;
        this.enrollmentService = enrollmentService;
        this.storedProcedureRepository = storedProcedureRepository;
        this.accessScope = accessScope;
    }

    /** 教师本人授课的开课计划（教学班） */
    public List<OfferingVO> myOfferings() {
        SessionUser user = UserContext.requireUser();
        if (user.hasRole("TEACHER")) {
            if (user.getLinkedNo() == null || user.getLinkedNo().isBlank()) {
                throw new ForbiddenException("账号未绑定工号");
            }
            return offeringRepository.listByTeacherNo(user.getLinkedNo());
        }
        if (user.hasRole("SYS_ADMIN")) {
            return offeringRepository.findPage(null, null, null, 0, 200);
        }
        throw new ForbiddenException("仅教师或管理员可查看教学班");
    }

    /** 某班选课学生名册及成绩（含未录入） */
    public List<GradeVO> roster(String offeringNo) {
        accessScope.assertTeacherOffering(offeringNo);
        if (offeringRepository.findByNo(offeringNo) == null) {
            throw new BusinessException("开课计划不存在");
        }
        return gradeRepository.findRosterByOffering(offeringNo);
    }

    public PageResult<GradeVO> page(String studentNo, String offeringNo, int page, int pageSize) {
        studentNo = accessScope.resolveStudentNoFilter(studentNo);
        if (page < 1) page = 1;
        if (pageSize < 1) pageSize = 10;
        int offset = (page - 1) * pageSize;
        long total = gradeRepository.count(studentNo, offeringNo);
        return new PageResult<>(gradeRepository.findPage(studentNo, offeringNo, offset, pageSize),
                total, page, pageSize);
    }

    public void save(String studentNo, String offeringNo, BigDecimal totalScore, String examType) {
        accessScope.assertTeacherOffering(offeringNo);
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
      if (existing != null) {
        SessionUser user = UserContext.requireUser();
        String oldJson = String.format("{\"total_score\":%s,\"exam_type\":\"%s\"}", existing.getTotalScore() != null ? existing.getTotalScore().toString() : "null", existing.getExamType() != null ? existing.getExamType() : "");
        String newJson = String.format("{\"total_score\":%s,\"exam_type\":\"%s\"}", totalScore != null ? totalScore.toString() : "null", type);
        gradeRepository.insertGradeChangeLog(studentNo, offeringNo, oldJson, newJson, user.getUsername());
      }
    }

    public void submitOffering(String offeringNo) {
        accessScope.assertTeacherOffering(offeringNo);
        gradeRepository.lockOffering(offeringNo);
    }
    
    public void unlockOffering(String offeringNo) {
        SessionUser user = UserContext.requireUser();
        if (!user.hasRole("SYS_ADMIN")) {
            throw new ForbiddenException("仅管理员可解锁成绩");
        }
        gradeRepository.unlockOffering(offeringNo);
    }

    public void arrangeMakeup(String studentNo, String offeringNo) {
        accessScope.assertTeacherOffering(offeringNo);
        GradeVO g = gradeRepository.findOne(studentNo, offeringNo);
        if (g == null || g.getTotalScore() == null || g.getTotalScore().doubleValue() >= 60) {
            throw new BusinessException("仅不及格记录可安排补考");
        }
        gradeRepository.setMakeup(studentNo, offeringNo);
    }

    public void arrangeRetake(String studentNo, String offeringNo) {
        accessScope.assertStudentSelf(studentNo);
        enrollmentService.enroll(studentNo, offeringNo, true);
    }

    /** 批量保存成绩：CALL sp_batch_save_grades（DB-Tech-01） */
    public int batchSave(String offeringNo, List<Map<String, Object>> grades) {
        accessScope.assertTeacherOffering(offeringNo);
        String json = grades.stream()
                .map(g -> String.format(
                        "{\"studentNo\":\"%s\",\"usualScore\":%s,\"finalScore\":%s}",
                        g.get("studentNo"), g.get("usualScore"), g.get("finalScore")))
                .collect(Collectors.joining(",", "[", "]"));
        SpResult r = storedProcedureRepository.batchSaveGrades(offeringNo, json);
        if (r.code() != 0) {
            throw new BusinessException(r.message());
        }
        return r.count();
    }

    private void validateScore(BigDecimal score) {
        if (score == null || score.doubleValue() < 0 || score.doubleValue() > 100) {
            throw new BusinessException("成绩须在0到100之间");
        }
    }
}
