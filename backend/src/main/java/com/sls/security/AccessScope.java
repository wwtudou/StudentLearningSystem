package com.sls.security;

import com.sls.common.ForbiddenException;
import com.sls.dto.SessionUser;
import com.sls.repository.OfferingRepository;
import com.sls.repository.StudentRepository;
import org.springframework.stereotype.Component;

/**
 * 数据范围校验：学生仅本人、院系管理员仅本院、教师仅所教开课计划
 */
@Component
public class AccessScope {

    private final StudentRepository studentRepository;
    private final OfferingRepository offeringRepository;

    public AccessScope(StudentRepository studentRepository, OfferingRepository offeringRepository) {
        this.studentRepository = studentRepository;
        this.offeringRepository = offeringRepository;
    }

    /** 学生角色强制只能查本人学号 */
    public String resolveStudentNoFilter(String requested) {
        SessionUser user = UserContext.requireUser();
        if (user.hasRole("STUDENT")) {
            requireLinkedNo(user);
            return user.getLinkedNo();
        }
        return requested;
    }

    /** 读取/修改指定学号数据时校验本人范围 */
    public void assertStudentAccess(String studentNo) {
        SessionUser user = UserContext.requireUser();
        if (user.hasRole("STUDENT")) {
            requireLinkedNo(user);
            if (!user.getLinkedNo().equals(studentNo)) {
                throw new ForbiddenException("只能访问本人学籍数据");
            }
        }
        if (user.hasRole("DEPT_ADMIN") && user.getCollegeCode() != null && studentNo != null) {
            String college = studentRepository.findCollegeCode(studentNo);
            if (college != null && !user.getCollegeCode().equals(college)) {
                throw new ForbiddenException("只能访问本院系学生数据");
            }
        }
    }

    /** 院系管理员列表查询时限定学院 */
    public String resolveCollegeFilter(String requested) {
        SessionUser user = UserContext.requireUser();
        if (user.hasRole("DEPT_ADMIN") && user.getCollegeCode() != null) {
            return user.getCollegeCode();
        }
        return requested;
    }

    /** 写入学生档案时校验学院范围 */
    public void assertCollegeScope(String collegeCode) {
        SessionUser user = UserContext.requireUser();
        if (user.hasRole("DEPT_ADMIN") && user.getCollegeCode() != null) {
            if (collegeCode == null || !user.getCollegeCode().equals(collegeCode)) {
                throw new ForbiddenException("只能管理本院系数据");
            }
        }
    }

    /** 学生选课/退课/重修仅本人 */
    public void assertStudentSelf(String studentNo) {
        SessionUser user = UserContext.requireUser();
        if (user.hasRole("STUDENT")) {
            requireLinkedNo(user);
            if (studentNo == null || !user.getLinkedNo().equals(studentNo)) {
                throw new ForbiddenException("只能操作本人选课");
            }
        }
    }

    /** 教师录入/提交成绩须为本人授课计划 */
    public void assertTeacherOffering(String offeringNo) {
        SessionUser user = UserContext.requireUser();
        if (user.hasRole("TEACHER") && !user.hasRole("SYS_ADMIN")) {
            requireLinkedNo(user);
            String teacherNo = offeringRepository.findTeacherNo(offeringNo);
            if (teacherNo == null || !teacherNo.equals(user.getLinkedNo())) {
                throw new ForbiddenException("只能操作本人授课计划的成绩");
            }
        }
    }

    private void requireLinkedNo(SessionUser user) {
        if (user.getLinkedNo() == null || user.getLinkedNo().isBlank()) {
            throw new ForbiddenException("账号未绑定学号/工号，请联系管理员");
        }
    }
}
