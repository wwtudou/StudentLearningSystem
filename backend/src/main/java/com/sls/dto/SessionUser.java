package com.sls.dto;

import java.util.List;

/**
 * 当前登录用户信息
 */
public class SessionUser {
    private String username;
    private String realName;
    private List<String> roles;
    /** 关联学号或工号 */
    private String linkedNo;
    /** 院系管理员所属学院 */
    private String collegeCode;

    public SessionUser() {}

    public SessionUser(String username, String realName, List<String> roles) {
        this.username = username;
        this.realName = realName;
        this.roles = roles;
    }

    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
    public String getLinkedNo() { return linkedNo; }
    public void setLinkedNo(String linkedNo) { this.linkedNo = linkedNo; }
    public String getCollegeCode() { return collegeCode; }
    public void setCollegeCode(String collegeCode) { this.collegeCode = collegeCode; }
}
