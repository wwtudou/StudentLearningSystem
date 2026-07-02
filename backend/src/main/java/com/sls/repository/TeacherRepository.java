package com.sls.repository;

import com.sls.dto.OptionVO;
import com.sls.dto.TeacherVO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * 教师数据访问
 */
@Repository
public class TeacherRepository {

    private final JdbcTemplate jdbc;

    public TeacherRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public long count(String teacherNo, String name, String collegeCode) {
        StringBuilder sql = new StringBuilder("""
                SELECT COUNT(*) FROM teacher t WHERE 1=1
                """);
        List<Object> params = buildWhere(sql, teacherNo, name, collegeCode);
        Long c = jdbc.queryForObject(sql.toString(), Long.class, params.toArray());
        return c != null ? c : 0;
    }

    public List<TeacherVO> findPage(String teacherNo, String name, String collegeCode, int offset, int limit) {
        StringBuilder sql = new StringBuilder("""
                SELECT t.teacher_no, t.name, t.college_code, c.college_name, t.status
                FROM teacher t JOIN college c ON t.college_code = c.college_code WHERE 1=1
                """);
        List<Object> params = buildWhere(sql, teacherNo, name, collegeCode);
        sql.append(" ORDER BY t.teacher_no LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);
        return jdbc.query(sql.toString(), (rs, rowNum) -> mapRow(rs), params.toArray());
    }

    public TeacherVO findByNo(String teacherNo) {
        List<TeacherVO> list = jdbc.query("""
                SELECT t.teacher_no, t.name, t.college_code, c.college_name, t.status
                FROM teacher t JOIN college c ON t.college_code = c.college_code
                WHERE t.teacher_no=?
                """, (rs, rowNum) -> mapRow(rs), teacherNo);
        return list.isEmpty() ? null : list.get(0);
    }

    public boolean exists(String teacherNo) {
        Long c = jdbc.queryForObject("SELECT COUNT(*) FROM teacher WHERE teacher_no=?", Long.class, teacherNo);
        return c != null && c > 0;
    }

    public void insert(String teacherNo, String name, String collegeCode) {
        jdbc.update("INSERT INTO teacher (teacher_no, name, college_code, status) VALUES (?, ?, ?, '在职')",
                teacherNo, name, collegeCode);
    }

    public void update(String teacherNo, String name, String collegeCode, String status) {
        jdbc.update("UPDATE teacher SET name=?, college_code=?, status=? WHERE teacher_no=?",
                name, collegeCode, status, teacherNo);
    }

    public List<OptionVO> options(String collegeCode) {
        if (collegeCode == null || collegeCode.isBlank()) {
            return jdbc.query(
                    "SELECT teacher_no, name FROM teacher WHERE status='在职' ORDER BY teacher_no",
                    (rs, rowNum) -> new OptionVO(rs.getString("teacher_no"), rs.getString("name")));
        }
        return jdbc.query(
                "SELECT teacher_no, name FROM teacher WHERE college_code=? AND status='在职' ORDER BY teacher_no",
                (rs, rowNum) -> new OptionVO(rs.getString("teacher_no"), rs.getString("name")),
                collegeCode);
    }

    private TeacherVO mapRow(java.sql.ResultSet rs) throws java.sql.SQLException {
        TeacherVO vo = new TeacherVO();
        vo.setTeacherNo(rs.getString("teacher_no"));
        vo.setName(rs.getString("name"));
        vo.setCollegeCode(rs.getString("college_code"));
        vo.setCollegeName(rs.getString("college_name"));
        vo.setStatus(rs.getString("status"));
        return vo;
    }

    private List<Object> buildWhere(StringBuilder sql, String teacherNo, String name, String collegeCode) {
        List<Object> params = new ArrayList<>();
        if (teacherNo != null && !teacherNo.isBlank()) {
            sql.append(" AND t.teacher_no LIKE ?");
            params.add("%" + teacherNo.trim() + "%");
        }
        if (name != null && !name.isBlank()) {
            sql.append(" AND t.name LIKE ?");
            params.add("%" + name.trim() + "%");
        }
        if (collegeCode != null && !collegeCode.isBlank()) {
            sql.append(" AND t.college_code=?");
            params.add(collegeCode);
        }
        return params;
    }
}
