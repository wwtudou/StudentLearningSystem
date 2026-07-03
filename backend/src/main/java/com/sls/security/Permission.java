package com.sls.security;

/**
 * 应用层权限点（对照需求规格 2.4 权限矩阵）
 */
public enum Permission {
    USER_MANAGE,
    DB_TECH,
    ORG_READ,
    ORG_WRITE,
    DICT_READ,
    DICT_WRITE,
    STUDENT_READ,
    STUDENT_WRITE,
    TEACHER_READ,
    TEACHER_WRITE,
    COURSE_READ,
    COURSE_WRITE,
    OFFERING_READ,
    OFFERING_WRITE,
    REWARD_READ,
    REWARD_WRITE,
    ENROLLMENT_READ,
    ENROLLMENT_WRITE,
    GRADE_READ,
    GRADE_WRITE,
    REPORT_READ
}
