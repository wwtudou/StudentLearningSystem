package com.sls.repository;

import com.sls.dto.EnrollmentVO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 选课数据访问
 */
@Repository
public class EnrollmentRepository {

    private final JdbcTemplate jdbc;

    public EnrollmentRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final String BASE_SELECT = """
            SELECT e.student_no, s.name AS student_name, e.offering_no, c.course_name,
                   sem.semester_name, t.name AS teacher_name, e.enroll_time, e.is_retake,
                   o.status AS offering_status
            FROM enrollment e
            JOIN student s ON e.student_no = s.student_no
            JOIN course_offering o ON e.offering_no = o.offering_no
            JOIN course c ON o.course_code = c.course_code
            JOIN semester sem ON o.semester_code = sem.semester_code
            JOIN teacher t ON o.teacher_no = t.teacher_no
            """;

    public long count(String studentNo, String offeringNo) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM enrollment e WHERE 1=1 ");
        List<Object> params = buildWhere(sql, studentNo, offeringNo);
        Long c = jdbc.queryForObject(sql.toString(), Long.class, params.toArray());
        return c != null ? c : 0;
    }

    public List<EnrollmentVO> findPage(String studentNo, String offeringNo, int offset, int limit) {
        StringBuilder sql = new StringBuilder(BASE_SELECT + " WHERE 1=1 ");
        List<Object> params = buildWhere(sql, studentNo, offeringNo);
        sql.append(" ORDER BY e.enroll_time DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);
        return jdbc.query(sql.toString(), (rs, rowNum) -> mapRow(rs), params.toArray());
    }

    public boolean exists(String studentNo, String offeringNo) {
        Long c = jdbc.queryForObject(
                "SELECT COUNT(*) FROM enrollment WHERE student_no=? AND offering_no=?",
                Long.class, studentNo, offeringNo);
        return c != null && c > 0;
    }

    public void insert(String studentNo, String offeringNo, boolean retake) {
        jdbc.update("""
                INSERT INTO enrollment (student_no, offering_no, enroll_time, is_retake)
                VALUES (?, ?, ?, ?)
                """, studentNo, offeringNo, LocalDateTime.now(), retake ? 1 : 0);
    }

    public void delete(String studentNo, String offeringNo) {
        jdbc.update("DELETE FROM enrollment WHERE student_no=? AND offering_no=?", studentNo, offeringNo);
    }

    public void deleteGrade(String studentNo, String offeringNo) {
        jdbc.update("DELETE FROM grade WHERE student_no=? AND offering_no=?", studentNo, offeringNo);
    }

    /** 同学期上课时间是否冲突 */
    public boolean hasScheduleConflict(String studentNo, String semesterCode, String schedule) {
        Long c = jdbc.queryForObject("""
                SELECT COUNT(*) FROM enrollment e
                JOIN course_offering o ON e.offering_no = o.offering_no
                WHERE e.student_no = ? AND o.semester_code = ? AND o.schedule = ?
                """, Long.class, studentNo, semesterCode, schedule);
        return c != null && c > 0;
    }

    public String findStudentStatus(String studentNo) {
        List<String> list = jdbc.query(
                "SELECT student_status FROM student WHERE student_no=? AND deleted=0",
                (rs, rowNum) -> rs.getString("student_status"), studentNo);
        return list.isEmpty() ? null : list.get(0);
    }

    private EnrollmentVO mapRow(java.sql.ResultSet rs) throws java.sql.SQLException {
        EnrollmentVO vo = new EnrollmentVO();
        vo.setStudentNo(rs.getString("student_no"));
        vo.setStudentName(rs.getString("student_name"));
        vo.setOfferingNo(rs.getString("offering_no"));
        vo.setCourseName(rs.getString("course_name"));
        vo.setSemesterName(rs.getString("semester_name"));
        vo.setTeacherName(rs.getString("teacher_name"));
        vo.setEnrollTime(rs.getTimestamp("enroll_time").toLocalDateTime().toString());
        vo.setRetake(rs.getInt("is_retake") == 1);
        vo.setOfferingStatus(rs.getString("offering_status"));
        return vo;
    }

    private List<Object> buildWhere(StringBuilder sql, String studentNo, String offeringNo) {
        List<Object> params = new ArrayList<>();
        if (studentNo != null && !studentNo.isBlank()) {
            sql.append(" AND e.student_no LIKE ?");
            params.add("%" + studentNo.trim() + "%");
        }
        if (offeringNo != null && !offeringNo.isBlank()) {
            sql.append(" AND e.offering_no=?");
            params.add(offeringNo);
        }
        return params;
    }
}
