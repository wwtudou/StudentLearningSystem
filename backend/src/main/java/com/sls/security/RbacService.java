package com.sls.security;

import com.sls.common.ForbiddenException;
import com.sls.dto.SessionUser;
import org.springframework.stereotype.Service;

/**
 * 根据请求路径与方法解析所需权限并校验
 */
@Service
public class RbacService {

    public void checkAccess(SessionUser user, String method, String requestUri) {
        Permission required = resolvePermission(method, requestUri);
        if (required == null) {
            return;
        }
        if (!RolePermissions.hasPermission(user.getRoles(), required)) {
            throw new ForbiddenException("当前角色无权访问：" + describe(required));
        }
    }

    private Permission resolvePermission(String method, String requestUri) {
        String path = requestUri.contains("?") ? requestUri.substring(0, requestUri.indexOf('?')) : requestUri;
        String m = method.toUpperCase();

        if (path.startsWith("/api/users")) {
            return Permission.USER_MANAGE;
        }
        if (path.startsWith("/api/db-tech")) {
            return Permission.DB_TECH;
        }
        if (path.startsWith("/api/org")) {
            return "GET".equals(m) ? Permission.ORG_READ : Permission.ORG_WRITE;
        }
        if (path.startsWith("/api/dicts")) {
            return "GET".equals(m) ? Permission.DICT_READ : Permission.DICT_WRITE;
        }
        if (path.startsWith("/api/students")) {
            return "GET".equals(m) ? Permission.STUDENT_READ : Permission.STUDENT_WRITE;
        }
        if (path.startsWith("/api/teachers")) {
            return "GET".equals(m) ? Permission.TEACHER_READ : Permission.TEACHER_WRITE;
        }
        if (path.startsWith("/api/courses") || path.equals("/api/semesters")) {
            return "GET".equals(m) ? Permission.COURSE_READ : Permission.COURSE_WRITE;
        }
        if (path.startsWith("/api/offerings")) {
            return "GET".equals(m) ? Permission.OFFERING_READ : Permission.OFFERING_WRITE;
        }
        if (path.startsWith("/api/rewards")) {
            return "GET".equals(m) ? Permission.REWARD_READ : Permission.REWARD_WRITE;
        }
        if (path.startsWith("/api/enrollments")) {
            return "GET".equals(m) ? Permission.ENROLLMENT_READ : Permission.ENROLLMENT_WRITE;
        }
        if (path.startsWith("/api/grades")) {
            if ("POST".equals(m) && path.endsWith("/retake")) {
                return Permission.ENROLLMENT_WRITE;
            }
            return "GET".equals(m) ? Permission.GRADE_READ : Permission.GRADE_WRITE;
        }
        if (path.startsWith("/api/reports")) {
            return Permission.REPORT_READ;
        }
        return null;
    }

    private String describe(Permission p) {
        return switch (p) {
            case USER_MANAGE -> "用户权限管理";
            case DB_TECH -> "高阶数据库演示";
            case ORG_READ -> "查看院系专业";
            case ORG_WRITE -> "编辑院系专业";
            case DICT_READ -> "查看字典";
            case DICT_WRITE -> "维护字典";
            case STUDENT_READ -> "查看学生信息";
            case STUDENT_WRITE -> "维护学生信息";
            case TEACHER_READ -> "查看教师信息";
            case TEACHER_WRITE -> "维护教师信息";
            case COURSE_READ -> "查看课程";
            case COURSE_WRITE -> "维护课程";
            case OFFERING_READ -> "查看开课计划";
            case OFFERING_WRITE -> "维护开课计划";
            case REWARD_READ -> "查看奖惩";
            case REWARD_WRITE -> "维护奖惩";
            case ENROLLMENT_READ -> "查看选课";
            case ENROLLMENT_WRITE -> "选课/退课";
            case GRADE_READ -> "查看成绩";
            case GRADE_WRITE -> "录入/修改成绩";
            case REPORT_READ -> "报表统计";
        };
    }
}
