package com.sls.repository;

import com.sls.dto.OptionVO;
import com.sls.dto.SessionUser;
import com.sls.dto.UserVO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * 系统用户数据访问
 */
@Repository
public class UserRepository {

    private final JdbcTemplate jdbc;

    public UserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public SessionUser findSessionUser(String username) {
        List<SessionUser> users = jdbc.query(
                "SELECT username, real_name, status, linked_no, college_code FROM sys_user WHERE username = ?",
                (rs, rowNum) -> {
                    if (!"启用".equals(rs.getString("status"))) {
                        return null;
                    }
                    SessionUser su = new SessionUser();
                    su.setUsername(rs.getString("username"));
                    su.setRealName(rs.getString("real_name"));
                    su.setLinkedNo(rs.getString("linked_no"));
                    su.setCollegeCode(rs.getString("college_code"));
                    return su;
                }, username);
        SessionUser user = users.stream().filter(u -> u != null).findFirst().orElse(null);
        if (user != null) {
            user.setRoles(listRoles(username));
        }
        return user;
    }

    public String findPasswordHash(String username) {
        List<String> list = jdbc.query(
                "SELECT password FROM sys_user WHERE username = ?",
                (rs, rowNum) -> rs.getString("password"), username);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<String> listRoles(String username) {
        return jdbc.query(
                "SELECT role_code FROM user_role WHERE username = ?",
                (rs, rowNum) -> rs.getString("role_code"), username);
    }

    public boolean exists(String username) {
        Long c = jdbc.queryForObject("SELECT COUNT(*) FROM sys_user WHERE username = ?", Long.class, username);
        return c != null && c > 0;
    }

    public long countUsers(String username, String realName) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM sys_user WHERE 1=1 ");
        List<Object> params = buildUserWhere(sql, username, realName);
        Long c = jdbc.queryForObject(sql.toString(), Long.class, params.toArray());
        return c != null ? c : 0;
    }

    public List<UserVO> findUserPage(String username, String realName, int offset, int limit) {
        StringBuilder sql = new StringBuilder(
                "SELECT username, real_name, status FROM sys_user WHERE 1=1 ");
        List<Object> params = buildUserWhere(sql, username, realName);
        sql.append(" ORDER BY username LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);
        return jdbc.query(sql.toString(), (rs, rowNum) -> {
            UserVO vo = new UserVO();
            vo.setUsername(rs.getString("username"));
            vo.setRealName(rs.getString("real_name"));
            vo.setStatus(rs.getString("status"));
            vo.setRoles(listRoles(vo.getUsername()));
            return vo;
        }, params.toArray());
    }

    public void insert(String username, String passwordHash, String realName) {
        jdbc.update(
                "INSERT INTO sys_user (username, password, real_name, status) VALUES (?, ?, ?, '启用')",
                username, passwordHash, realName);
    }

    public void update(String username, String realName, String status) {
        jdbc.update("UPDATE sys_user SET real_name=?, status=? WHERE username=?",
                realName, status, username);
    }

    public void updatePassword(String username, String passwordHash) {
        jdbc.update("UPDATE sys_user SET password=? WHERE username=?", passwordHash, username);
    }

    public void deleteRoles(String username) {
        jdbc.update("DELETE FROM user_role WHERE username=?", username);
    }

    public void insertRole(String username, String roleCode) {
        jdbc.update("INSERT INTO user_role (username, role_code) VALUES (?, ?)", username, roleCode);
    }

    public long countSysAdmins() {
        Long c = jdbc.queryForObject(
                "SELECT COUNT(*) FROM user_role WHERE role_code='SYS_ADMIN'", Long.class);
        return c != null ? c : 0;
    }

    public List<OptionVO> listRoleOptions() {
        return jdbc.query("SELECT role_code, role_name FROM role ORDER BY role_code",
                (rs, rowNum) -> new OptionVO(rs.getString("role_code"), rs.getString("role_name")));
    }

    private List<Object> buildUserWhere(StringBuilder sql, String username, String realName) {
        List<Object> params = new ArrayList<>();
        if (username != null && !username.isBlank()) {
            sql.append(" AND username LIKE ?");
            params.add("%" + username.trim() + "%");
        }
        if (realName != null && !realName.isBlank()) {
            sql.append(" AND real_name LIKE ?");
            params.add("%" + realName.trim() + "%");
        }
        return params;
    }
}
