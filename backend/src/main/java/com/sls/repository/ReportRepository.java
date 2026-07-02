package com.sls.repository;

import com.sls.dto.StatRowVO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 统计报表数据访问（FR-07）
 */
@Repository
public class ReportRepository {

    private final JdbcTemplate jdbc;

    public ReportRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** 按学院/专业统计在读学生人数 */
    public List<StatRowVO> studentCountBy(String groupBy) {
        if ("major".equals(groupBy)) {
            return jdbc.query("""
                    SELECT m.major_code AS group_key, m.major_name AS group_name, COUNT(*) AS cnt
                    FROM student s JOIN major m ON s.major_code = m.major_code
                    WHERE s.deleted=0 AND s.student_status='在读'
                    GROUP BY m.major_code, m.major_name ORDER BY cnt DESC
                    """, (rs, rowNum) -> mapCount(rs));
        }
        return jdbc.query("""
                SELECT c.college_code AS group_key, c.college_name AS group_name, COUNT(*) AS cnt
                FROM student s JOIN college c ON s.college_code = c.college_code
                WHERE s.deleted=0 AND s.student_status='在读'
                GROUP BY c.college_code, c.college_name ORDER BY cnt DESC
                """, (rs, rowNum) -> mapCount(rs));
    }

    /** 按开课计划统计选课人数 */
    public List<StatRowVO> enrollmentCount() {
        return jdbc.query("""
                SELECT o.offering_no AS group_key,
                       CONCAT(c.course_name, ' / ', sem.semester_name) AS group_name,
                       o.enrolled_count AS cnt
                FROM course_offering o
                JOIN course c ON o.course_code = c.course_code
                JOIN semester sem ON o.semester_code = sem.semester_code
                ORDER BY o.enrolled_count DESC
                """, (rs, rowNum) -> mapCount(rs));
    }

    /** 按开课计划统计平均分与及格率 */
    public List<StatRowVO> gradeStats(String offeringNo) {
        StringBuilder sql = new StringBuilder("""
                SELECT o.offering_no AS group_key,
                       CONCAT(c.course_name, ' / ', sem.semester_name) AS group_name,
                       COUNT(g.total_score) AS cnt,
                       AVG(g.total_score) AS avg_score,
                       SUM(CASE WHEN g.total_score >= 60 THEN 1 ELSE 0 END) * 100.0 / NULLIF(COUNT(g.total_score), 0) AS pass_rate
                FROM grade g
                JOIN course_offering o ON g.offering_no = o.offering_no
                JOIN course c ON o.course_code = c.course_code
                JOIN semester sem ON o.semester_code = sem.semester_code
                WHERE g.total_score IS NOT NULL
                """);
        if (offeringNo != null && !offeringNo.isBlank()) {
            sql.append(" AND o.offering_no=?");
            sql.append(" GROUP BY o.offering_no, c.course_name, sem.semester_name");
            return jdbc.query(sql.toString(), (rs, rowNum) -> mapGrade(rs), offeringNo);
        }
        sql.append(" GROUP BY o.offering_no, c.course_name, sem.semester_name ORDER BY avg_score DESC");
        return jdbc.query(sql.toString(), (rs, rowNum) -> mapGrade(rs));
    }

    /** 奖惩次数统计 */
    public List<StatRowVO> rewardStats() {
        return jdbc.query("""
                SELECT rp.type AS group_key, rp.type AS group_name, COUNT(*) AS cnt
                FROM reward_punishment rp
                GROUP BY rp.type ORDER BY cnt DESC
                """, (rs, rowNum) -> mapCount(rs));
    }

    private StatRowVO mapCount(java.sql.ResultSet rs) throws java.sql.SQLException {
        StatRowVO vo = new StatRowVO();
        vo.setGroupKey(rs.getString("group_key"));
        vo.setGroupName(rs.getString("group_name"));
        vo.setCount(rs.getLong("cnt"));
        return vo;
    }

    private StatRowVO mapGrade(java.sql.ResultSet rs) throws java.sql.SQLException {
        StatRowVO vo = mapCount(rs);
        double avg = rs.getDouble("avg_score");
        if (!rs.wasNull()) vo.setAvgScore(Math.round(avg * 100.0) / 100.0);
        double pass = rs.getDouble("pass_rate");
        if (!rs.wasNull()) vo.setPassRate(Math.round(pass * 100.0) / 100.0);
        return vo;
    }
}
