package com.sls.dto;

import java.util.List;

/**
 * 当前登录用户信息
 */
public class SessionUser {
    private String username;
    private String realName;
    private List<String> roles;

    public SessionUser() {}

    public SessionUser(String username, String realName, List<String> roles) {
        this.username = username;
        this.realName = realName;
        this.roles = roles;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
}
