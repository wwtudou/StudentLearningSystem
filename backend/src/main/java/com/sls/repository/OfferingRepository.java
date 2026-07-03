package com.sls.repository;

import com.sls.dto.OfferingVO;
import com.sls.dto.SemesterVO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 开课计划与学期数据访问
 */
@Repository
public class OfferingRepository {

    private final JdbcTemplate jdbc;

    public OfferingRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<SemesterVO> listSemesters() {
        return jdbc.query(
                "SELECT semester_code, semester_name, start_date, end_date FROM semester ORDER BY semester_code",
                (rs, rowNum) -> {
                    SemesterVO vo = new SemesterVO();
                    vo.setSemesterCode(rs.getString("semester_code"));
                    vo.setSemesterName(rs.getString("semester_name"));
                    vo.setStartDate(rs.getDate("start_date").toString());
                    vo.setEndDate(rs.getDate("end_date").toString());
                    return vo;
                });
    }

    public long count(String semesterCode, String courseCode, String status) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM course_offering o WHERE 1=1 ");
        List<Object> params = buildWhere(sql, semesterCode, courseCode, status);
        Long c = jdbc.queryForObject(sql.toString(), Long.class, params.toArray());
        return c != null ? c : 0;
    }

    public List<OfferingVO> findPage(String semesterCode, String courseCode, String status,
                                     int offset, int limit) {
        StringBuilder sql = new StringBuilder(BASE_SELECT + " WHERE 1=1 ");
        List<Object> params = buildWhere(sql, semesterCode, courseCode, status);
        sql.append(" ORDER BY o.offering_no LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);
        return jdbc.query(sql.toString(), (rs, rowNum) -> mapRow(rs), params.toArray());
    }

    public OfferingVO findByNo(String offeringNo) {
        List<OfferingVO> list = jdbc.query(BASE_SELECT + " WHERE o.offering_no=?",
                (rs, rowNum) -> mapRow(rs), offeringNo);
        return list.isEmpty() ? null : list.get(0);
    }

    /** 教师本人授课的开课计划列表 */
    public List<OfferingVO> listByTeacherNo(String teacherNo) {
        return jdbc.query(BASE_SELECT + " WHERE o.teacher_no=? ORDER BY o.semester_code DESC, o.offering_no",
                (rs, rowNum) -> mapRow(rs), teacherNo);
    }

    /** 行锁查询开课计划 */
    public Map<String, Object> findForUpdate(String offeringNo) {
        List<Map<String, Object>> list = jdbc.queryForList(
                "SELECT offering_no, capacity, enrolled_count, status FROM course_offering WHERE offering_no=? FOR UPDATE",
                offeringNo);
        return list.isEmpty() ? null : list.get(0);
    }

    public boolean exists(String offeringNo) {
        Long c = jdbc.queryForObject("SELECT COUNT(*) FROM course_offering WHERE offering_no=?",
                Long.class, offeringNo);
        return c != null && c > 0;
    }

    public void insert(String offeringNo, String courseCode, String semesterCode, String teacherNo,
                       int capacity, String schedule, String status) {
        jdbc.update("""
                INSERT INTO course_offering (offering_no, course_code, semester_code, teacher_no,
                    capacity, enrolled_count, schedule, status)
                VALUES (?, ?, ?, ?, ?, 0, ?, ?)
                """, offeringNo, courseCode, semesterCode, teacherNo, capacity, schedule, status);
    }

    public void update(String offeringNo, String teacherNo, int capacity, String schedule, String status) {
        jdbc.update("""
                UPDATE course_offering SET teacher_no=?, capacity=?, schedule=?, status=?
                WHERE offering_no=?
                """, teacherNo, capacity, schedule, status, offeringNo);
    }

    public void incrementEnrolled(String offeringNo) {
        jdbc.update("UPDATE course_offering SET enrolled_count = enrolled_count + 1 WHERE offering_no=?",
                offeringNo);
    }

    public void decrementEnrolled(String offeringNo) {
        jdbc.update(
                "UPDATE course_offering SET enrolled_count = enrolled_count - 1 WHERE offering_no=? AND enrolled_count > 0",
                offeringNo);
    }

    private static final String BASE_SELECT = """
            SELECT o.offering_no, o.course_code, c.course_name, o.semester_code, s.semester_name,
                   o.teacher_no, t.name AS teacher_name, o.capacity, o.enrolled_count, o.schedule, o.status
            FROM course_offering o
            JOIN course c ON o.course_code = c.course_code
            JOIN semester s ON o.semester_code = s.semester_code
            JOIN teacher t ON o.teacher_no = t.teacher_no
            """;

    private OfferingVO mapRow(java.sql.ResultSet rs) throws java.sql.SQLException {
        OfferingVO vo = new OfferingVO();
        vo.setOfferingNo(rs.getString("offering_no"));
        vo.setCourseCode(rs.getString("course_code"));
        vo.setCourseName(rs.getString("course_name"));
        vo.setSemesterCode(rs.getString("semester_code"));
        vo.setSemesterName(rs.getString("semester_name"));
        vo.setTeacherNo(rs.getString("teacher_no"));
        vo.setTeacherName(rs.getString("teacher_name"));
        vo.setCapacity(rs.getInt("capacity"));
        vo.setEnrolledCount(rs.getInt("enrolled_count"));
        vo.setSchedule(rs.getString("schedule"));
        vo.setStatus(rs.getString("status"));
        return vo;
    }

    private List<Object> buildWhere(StringBuilder sql, String semesterCode, String courseCode, String status) {
        List<Object> params = new ArrayList<>();
        if (semesterCode != null && !semesterCode.isBlank()) {
            sql.append(" AND o.semester_code=?");
            params.add(semesterCode);
        }
        if (courseCode != null && !courseCode.isBlank()) {
            sql.append(" AND o.course_code=?");
            params.add(courseCode);
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND o.status=?");
            params.add(status);
        }
        return params;
    }

    /** 查询开课计划授课教师工号 */
    public String findTeacherNo(String offeringNo) {
        List<String> list = jdbc.query(
                "SELECT teacher_no FROM course_offering WHERE offering_no=?",
                (rs, rowNum) -> rs.getString("teacher_no"), offeringNo);
        return list.isEmpty() ? null : list.get(0);
    }
}
