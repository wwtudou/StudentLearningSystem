package com.sls.repository;

import com.sls.dto.CollegeVO;
import com.sls.dto.MajorVO;
import com.sls.dto.OptionVO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * 学院与专业数据访问
 */
@Repository
public class OrgRepository {

    private final JdbcTemplate jdbc;

    public OrgRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<CollegeVO> listColleges(String status) {
        StringBuilder sql = new StringBuilder("SELECT college_code, college_name, status FROM college WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (status != null && !status.isBlank()) {
            sql.append(" AND status=?");
            params.add(status);
        }
        sql.append(" ORDER BY college_code");
        return jdbc.query(sql.toString(), (rs, rowNum) -> {
            CollegeVO vo = new CollegeVO();
            vo.setCollegeCode(rs.getString("college_code"));
            vo.setCollegeName(rs.getString("college_name"));
            vo.setStatus(rs.getString("status"));
            return vo;
        }, params.toArray());
    }

    public CollegeVO findCollege(String collegeCode) {
        List<CollegeVO> list = jdbc.query(
                "SELECT college_code, college_name, status FROM college WHERE college_code=?",
                (rs, rowNum) -> {
                    CollegeVO vo = new CollegeVO();
                    vo.setCollegeCode(rs.getString("college_code"));
                    vo.setCollegeName(rs.getString("college_name"));
                    vo.setStatus(rs.getString("status"));
                    return vo;
                }, collegeCode);
        return list.isEmpty() ? null : list.get(0);
    }

    public boolean collegeExists(String collegeCode) {
        Long c = jdbc.queryForObject("SELECT COUNT(*) FROM college WHERE college_code=?", Long.class, collegeCode);
        return c != null && c > 0;
    }

    public void insertCollege(String code, String name) {
        jdbc.update("INSERT INTO college (college_code, college_name, status) VALUES (?, ?, '启用')", code, name);
    }

    public void updateCollege(String code, String name, String status) {
        jdbc.update("UPDATE college SET college_name=?, status=? WHERE college_code=?", name, status, code);
    }

    public void updateMajorsStatusByCollege(String collegeCode, String status) {
        jdbc.update("UPDATE major SET status=? WHERE college_code=?", status, collegeCode);
    }

    public int countActiveStudentsByCollege(String collegeCode) {
        Long c = jdbc.queryForObject(
                "SELECT COUNT(*) FROM student WHERE college_code=? AND deleted=0 AND student_status='在读'",
                Long.class, collegeCode);
        return c != null ? c.intValue() : 0;
    }

    public List<MajorVO> listMajors(String collegeCode, String status) {
        StringBuilder sql = new StringBuilder("""
                SELECT m.major_code, m.major_name, m.college_code, c.college_name, m.status
                FROM major m JOIN college c ON m.college_code = c.college_code WHERE 1=1
                """);
        List<Object> params = new ArrayList<>();
        if (collegeCode != null && !collegeCode.isBlank()) {
            sql.append(" AND m.college_code=?");
            params.add(collegeCode);
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND m.status=?");
            params.add(status);
        }
        sql.append(" ORDER BY m.major_code");
        return jdbc.query(sql.toString(), (rs, rowNum) -> mapMajor(rs), params.toArray());
    }

    public MajorVO findMajor(String majorCode) {
        List<MajorVO> list = jdbc.query("""
                SELECT m.major_code, m.major_name, m.college_code, c.college_name, m.status
                FROM major m JOIN college c ON m.college_code = c.college_code
                WHERE m.major_code=?
                """, (rs, rowNum) -> mapMajor(rs), majorCode);
        return list.isEmpty() ? null : list.get(0);
    }

    public boolean majorExists(String majorCode) {
        Long c = jdbc.queryForObject("SELECT COUNT(*) FROM major WHERE major_code=?", Long.class, majorCode);
        return c != null && c > 0;
    }

    public void insertMajor(String code, String name, String collegeCode) {
        jdbc.update("INSERT INTO major (major_code, major_name, college_code, status) VALUES (?, ?, ?, '启用')",
                code, name, collegeCode);
    }

    public void updateMajor(String code, String name, String collegeCode, String status) {
        jdbc.update("UPDATE major SET major_name=?, college_code=?, status=? WHERE major_code=?",
                name, collegeCode, status, code);
    }

    public int countActiveStudentsByMajor(String majorCode) {
        Long c = jdbc.queryForObject(
                "SELECT COUNT(*) FROM student WHERE major_code=? AND deleted=0 AND student_status='在读'",
                Long.class, majorCode);
        return c != null ? c.intValue() : 0;
    }

    public List<OptionVO> collegeOptions() {
        return jdbc.query("SELECT college_code, college_name FROM college WHERE status='启用' ORDER BY college_code",
                (rs, rowNum) -> new OptionVO(rs.getString("college_code"), rs.getString("college_name")));
    }

    private MajorVO mapMajor(java.sql.ResultSet rs) throws java.sql.SQLException {
        MajorVO vo = new MajorVO();
        vo.setMajorCode(rs.getString("major_code"));
        vo.setMajorName(rs.getString("major_name"));
        vo.setCollegeCode(rs.getString("college_code"));
        vo.setCollegeName(rs.getString("college_name"));
        vo.setStatus(rs.getString("status"));
        return vo;
    }
}
