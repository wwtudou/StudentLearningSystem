package com.sls.security;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 角色与权限映射（FR-08 RBAC）
 */
public final class RolePermissions {

    private static final Map<String, Set<Permission>> ROLE_MAP = Map.of(
            "SYS_ADMIN", EnumSet.allOf(Permission.class),
            "DEPT_ADMIN", EnumSet.of(
                    Permission.ORG_READ, Permission.ORG_WRITE,
                    Permission.DICT_READ,
                    Permission.STUDENT_READ, Permission.STUDENT_WRITE,
                    Permission.TEACHER_READ, Permission.TEACHER_WRITE,
                    Permission.COURSE_READ, Permission.COURSE_WRITE,
                    Permission.OFFERING_READ, Permission.OFFERING_WRITE,
                    Permission.REWARD_READ, Permission.REWARD_WRITE,
                    Permission.ENROLLMENT_READ,
                    Permission.GRADE_READ,
                    Permission.REPORT_READ),
            "TEACHER", EnumSet.of(
                    Permission.ORG_READ,
                    Permission.DICT_READ,
                    Permission.STUDENT_READ,
                    Permission.TEACHER_READ,
                    Permission.COURSE_READ,
                    Permission.OFFERING_READ, Permission.OFFERING_WRITE,
                    Permission.REWARD_READ,
                    Permission.ENROLLMENT_READ,
                    Permission.GRADE_READ, Permission.GRADE_WRITE,
                    Permission.REPORT_READ),
            "STUDENT", EnumSet.of(
                    Permission.STUDENT_READ,
                    Permission.OFFERING_READ,
                    Permission.REWARD_READ,
                    Permission.ENROLLMENT_READ, Permission.ENROLLMENT_WRITE,
                    Permission.GRADE_READ,
                    Permission.REPORT_READ)
    );

    private RolePermissions() {}

    public static boolean hasPermission(List<String> roles, Permission permission) {
        if (roles == null || roles.isEmpty()) {
            return false;
        }
        for (String role : roles) {
            Set<Permission> perms = ROLE_MAP.get(role);
            if (perms != null && perms.contains(permission)) {
                return true;
            }
        }
        return false;
    }
}
