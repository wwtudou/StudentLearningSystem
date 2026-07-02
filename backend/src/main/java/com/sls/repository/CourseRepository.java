package com.sls.repository;

import com.sls.dto.CourseVO;
import com.sls.dto.OptionVO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 课程库数据访问
 */
@Repository
public class CourseRepository {

    private final JdbcTemplate jdbc;

    public CourseRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public long count(String courseCode, String courseName, String collegeCode) {
        StringBuilder sql = new StringBuilder("""
                SELECT COUNT(*) FROM course c WHERE 1=1
                """);
        List<Object> params = buildWhere(sql, courseCode, courseName, collegeCode);
        Long c = jdbc.queryForObject(sql.toString(), Long.class, params.toArray());
        return c != null ? c : 0;
    }

    public List<CourseVO> findPage(String courseCode, String courseName, String collegeCode,
                                   int offset, int limit) {
        StringBuilder sql = new StringBuilder("""
                SELECT c.course_code, c.course_name, c.credit, c.hours, c.college_code,
                       col.college_name, c.nature, c.status
                FROM course c JOIN college col ON c.college_code = col.college_code WHERE 1=1
                """);
        List<Object> params = buildWhere(sql, courseCode, courseName, collegeCode);
        sql.append(" ORDER BY c.course_code LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);
        return jdbc.query(sql.toString(), (rs, rowNum) -> mapRow(rs), params.toArray());
    }

    public CourseVO findByCode(String courseCode) {
        List<CourseVO> list = jdbc.query("""
                SELECT c.course_code, c.course_name, c.credit, c.hours, c.college_code,
                       col.college_name, c.nature, c.status
                FROM course c JOIN college col ON c.college_code = col.college_code
                WHERE c.course_code=?
                """, (rs, rowNum) -> mapRow(rs), courseCode);
        return list.isEmpty() ? null : list.get(0);
    }

    public boolean exists(String courseCode) {
        Long c = jdbc.queryForObject("SELECT COUNT(*) FROM course WHERE course_code=?", Long.class, courseCode);
        return c != null && c > 0;
    }

    public void insert(String code, String name, BigDecimal credit, int hours,
                       String collegeCode, String nature) {
        jdbc.update("""
                INSERT INTO course (course_code, course_name, credit, hours, college_code, nature, status)
                VALUES (?, ?, ?, ?, ?, ?, '启用')
                """, code, name, credit, hours, collegeCode, nature);
    }

    public void update(String code, String name, BigDecimal credit, int hours,
                       String collegeCode, String nature, String status) {
        jdbc.update("""
                UPDATE course SET course_name=?, credit=?, hours=?, college_code=?, nature=?, status=?
                WHERE course_code=?
                """, name, credit, hours, collegeCode, nature, status, code);
    }

    public List<OptionVO> options() {
        return jdbc.query("SELECT course_code, course_name FROM course WHERE status='启用' ORDER BY course_code",
                (rs, rowNum) -> new OptionVO(rs.getString("course_code"), rs.getString("course_name")));
    }

    private CourseVO mapRow(java.sql.ResultSet rs) throws java.sql.SQLException {
        CourseVO vo = new CourseVO();
        vo.setCourseCode(rs.getString("course_code"));
        vo.setCourseName(rs.getString("course_name"));
        vo.setCredit(rs.getBigDecimal("credit"));
        vo.setHours(rs.getInt("hours"));
        vo.setCollegeCode(rs.getString("college_code"));
        vo.setCollegeName(rs.getString("college_name"));
        vo.setNature(rs.getString("nature"));
        vo.setStatus(rs.getString("status"));
        return vo;
    }

    private List<Object> buildWhere(StringBuilder sql, String courseCode, String courseName, String collegeCode) {
        List<Object> params = new ArrayList<>();
        if (courseCode != null && !courseCode.isBlank()) {
            sql.append(" AND c.course_code LIKE ?");
            params.add("%" + courseCode.trim() + "%");
        }
        if (courseName != null && !courseName.isBlank()) {
            sql.append(" AND c.course_name LIKE ?");
            params.add("%" + courseName.trim() + "%");
        }
        if (collegeCode != null && !collegeCode.isBlank()) {
            sql.append(" AND c.college_code=?");
            params.add(collegeCode);
        }
        return params;
    }
}
