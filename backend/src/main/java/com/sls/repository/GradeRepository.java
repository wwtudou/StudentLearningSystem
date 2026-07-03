package com.sls.repository;

import com.sls.dto.GradeVO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 成绩数据访问
 */
@Repository
public class GradeRepository {

    private final JdbcTemplate jdbc;

    public GradeRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final String BASE_SELECT = """
            SELECT g.student_no, s.name AS student_name, g.offering_no, c.course_name,
                   sem.semester_name, g.total_score, g.exam_type, g.locked, g.academic_year
            FROM grade g
            JOIN student s ON g.student_no = s.student_no
            JOIN course_offering o ON g.offering_no = o.offering_no
            JOIN course c ON o.course_code = c.course_code
            JOIN semester sem ON o.semester_code = sem.semester_code
            """;

    public long count(String studentNo, String offeringNo) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM grade g WHERE 1=1 ");
        List<Object> params = buildWhere(sql, studentNo, offeringNo);
        Long c = jdbc.queryForObject(sql.toString(), Long.class, params.toArray());
        return c != null ? c : 0;
    }

    public List<GradeVO> findPage(String studentNo, String offeringNo, int offset, int limit) {
        StringBuilder sql = new StringBuilder(BASE_SELECT + " WHERE 1=1 ");
        List<Object> params = buildWhere(sql, studentNo, offeringNo);
        sql.append(" ORDER BY g.student_no LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);
        return jdbc.query(sql.toString(), (rs, rowNum) -> mapRow(rs), params.toArray());
    }

    public GradeVO findOne(String studentNo, String offeringNo) {
        List<GradeVO> list = jdbc.query(BASE_SELECT + " WHERE g.student_no=? AND g.offering_no=?",
                (rs, rowNum) -> mapRow(rs), studentNo, offeringNo);
        return list.isEmpty() ? null : list.get(0);
    }

    /** 某开课计划选课学生名册（含未录入成绩者） */
    public List<GradeVO> findRosterByOffering(String offeringNo) {
        String sql = """
                SELECT e.student_no, s.name AS student_name, e.offering_no, c.course_name,
                       sem.semester_name, g.total_score, g.exam_type, IFNULL(g.locked, 0) AS locked,
                       g.academic_year
                FROM enrollment e
                JOIN student s ON e.student_no = s.student_no
                JOIN course_offering o ON e.offering_no = o.offering_no
                JOIN course c ON o.course_code = c.course_code
                JOIN semester sem ON o.semester_code = sem.semester_code
                LEFT JOIN grade g ON g.student_no = e.student_no AND g.offering_no = e.offering_no
                WHERE e.offering_no = ?
                ORDER BY e.student_no
                """;
        return jdbc.query(sql, (rs, rowNum) -> {
            GradeVO vo = mapRow(rs);
            if (vo.getExamType() == null) {
                vo.setExamType("正常");
            }
            return vo;
        }, offeringNo);
    }

    public boolean exists(String studentNo, String offeringNo) {
        Long c = jdbc.queryForObject(
                "SELECT COUNT(*) FROM grade WHERE student_no=? AND offering_no=?",
                Long.class, studentNo, offeringNo);
        return c != null && c > 0;
    }

    public void upsert(String studentNo, String offeringNo, BigDecimal totalScore,
                       String examType, int academicYear) {
        if (exists(studentNo, offeringNo)) {
            jdbc.update("""
                    UPDATE grade SET total_score=?, exam_type=?
                    WHERE student_no=? AND offering_no=? AND locked=0
                    """, totalScore, examType, studentNo, offeringNo);
        } else {
            jdbc.update("""
                    INSERT INTO grade (student_no, offering_no, total_score, exam_type, locked, academic_year)
                    VALUES (?, ?, ?, ?, 0, ?)
                    """, studentNo, offeringNo, totalScore, examType, academicYear);
        }
    }

    public void lockOffering(String offeringNo) {
        jdbc.update("UPDATE grade SET locked=1 WHERE offering_no=? AND locked=0", offeringNo);
    }

    public void setMakeup(String studentNo, String offeringNo) {
        jdbc.update("""
                UPDATE grade SET exam_type='补考', locked=0
                WHERE student_no=? AND offering_no=?
                """, studentNo, offeringNo);
    }

    public int deleteByEnrollment(String studentNo, String offeringNo) {
        return jdbc.update("DELETE FROM grade WHERE student_no=? AND offering_no=?", studentNo, offeringNo);
    }

    private GradeVO mapRow(java.sql.ResultSet rs) throws java.sql.SQLException {
        GradeVO vo = new GradeVO();
        vo.setStudentNo(rs.getString("student_no"));
        vo.setStudentName(rs.getString("student_name"));
        vo.setOfferingNo(rs.getString("offering_no"));
        vo.setCourseName(rs.getString("course_name"));
        vo.setSemesterName(rs.getString("semester_name"));
        vo.setTotalScore(rs.getBigDecimal("total_score"));
        vo.setExamType(rs.getString("exam_type"));
        vo.setLocked(rs.getInt("locked") == 1);
        int year = rs.getInt("academic_year");
        if (!rs.wasNull()) {
            vo.setAcademicYear(year);
        }
        return vo;
    }

    private List<Object> buildWhere(StringBuilder sql, String studentNo, String offeringNo) {
        List<Object> params = new ArrayList<>();
        if (studentNo != null && !studentNo.isBlank()) {
            sql.append(" AND g.student_no LIKE ?");
            params.add("%" + studentNo.trim() + "%");
        }
        if (offeringNo != null && !offeringNo.isBlank()) {
            sql.append(" AND g.offering_no=?");
            params.add(offeringNo);
        }
        return params;
    }
}
