package com.sls.repository;

import com.sls.dto.ExplainRowVO;
import com.sls.dto.GradeChangeLogVO;
import com.sls.dto.WindowStatVO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * 高阶数据库技术查询（DB-Tech-04/06/07）
 */
@Repository
public class DbTechRepository {

    private final JdbcTemplate jdbc;

    public DbTechRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** DB-Tech-06：专业内成绩排名 */
    public List<WindowStatVO> majorRank(String semesterCode) {
        StringBuilder sql = new StringBuilder("""
                SELECT student_no, student_name, major_name, course_name, total_score,
                       RANK() OVER (PARTITION BY major_code ORDER BY total_score DESC) AS major_rank
                FROM v_student_grade_summary
                WHERE total_score IS NOT NULL
                """);
        List<Object> params = new ArrayList<>();
        if (semesterCode != null && !semesterCode.isBlank()) {
            sql.append(" AND semester_code = ?");
            params.add(semesterCode);
        }
        sql.append(" ORDER BY major_name, major_rank");
        return jdbc.query(sql.toString(), (rs, rowNum) -> {
            WindowStatVO vo = new WindowStatVO();
            vo.setStudentNo(rs.getString("student_no"));
            vo.setStudentName(rs.getString("student_name"));
            vo.setMajorName(rs.getString("major_name"));
            vo.setCourseName(rs.getString("course_name"));
            vo.setTotalScore(rs.getBigDecimal("total_score"));
            vo.setMajorRank(rs.getInt("major_rank"));
            return vo;
        }, params.toArray());
    }

    /** DB-Tech-06：累计获得学分 */
    public List<WindowStatVO> cumulativeCredit() {
        return jdbc.query("""
                SELECT student_no, student_name, semester_code, course_name, credit, total_score,
                       SUM(CASE WHEN total_score >= 60 THEN credit ELSE 0 END)
                         OVER (PARTITION BY student_no ORDER BY semester_code
                               ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW) AS cumulative_credit
                FROM v_student_grade_summary
                WHERE total_score IS NOT NULL
                ORDER BY student_no, semester_code
                """, (rs, rowNum) -> {
            WindowStatVO vo = new WindowStatVO();
            vo.setStudentNo(rs.getString("student_no"));
            vo.setStudentName(rs.getString("student_name"));
            vo.setSemesterCode(rs.getString("semester_code"));
            vo.setCourseName(rs.getString("course_name"));
            vo.setCredit(rs.getBigDecimal("credit"));
            vo.setTotalScore(rs.getBigDecimal("total_score"));
            vo.setCumulativeCredit(rs.getBigDecimal("cumulative_credit"));
            return vo;
        });
    }

    /** DB-Tech-02：成绩变更日志 */
    public List<GradeChangeLogVO> gradeChangeLogs(String studentNo) {
        StringBuilder sql = new StringBuilder("""
                SELECT log_no, student_no, offering_no, old_value, new_value, changed_by, changed_at
                FROM grade_change_log WHERE 1=1
                """);
        List<Object> params = new ArrayList<>();
        if (studentNo != null && !studentNo.isBlank()) {
            sql.append(" AND student_no LIKE ?");
            params.add("%" + studentNo.trim() + "%");
        }
        sql.append(" ORDER BY changed_at DESC LIMIT 100");
        return jdbc.query(sql.toString(), (rs, rowNum) -> {
            GradeChangeLogVO vo = new GradeChangeLogVO();
            vo.setLogNo(rs.getLong("log_no"));
            vo.setStudentNo(rs.getString("student_no"));
            vo.setOfferingNo(rs.getString("offering_no"));
            vo.setOldValue(rs.getString("old_value"));
            vo.setNewValue(rs.getString("new_value"));
            vo.setChangedBy(rs.getString("changed_by"));
            vo.setChangedAt(rs.getTimestamp("changed_at").toLocalDateTime().toString());
            return vo;
        }, params.toArray());
    }

    /** DB-Tech-07：分区裁剪 EXPLAIN */
    public List<ExplainRowVO> explainPartitionAudit(int academicYear) {
        return explain("EXPLAIN SELECT * FROM audit_log WHERE academic_year = " + academicYear);
    }

    public List<ExplainRowVO> explainGradeByOffering(String offeringNo) {
        return explain("EXPLAIN SELECT * FROM grade WHERE offering_no = '"
                + offeringNo.replace("'", "") + "'");
    }

    /** DB-Tech-04：索引使用 EXPLAIN */
    public List<ExplainRowVO> explainEnrollmentByOffering(String offeringNo) {
        return explain("EXPLAIN SELECT * FROM enrollment WHERE offering_no = '" + offeringNo.replace("'", "") + "'");
    }

    private List<ExplainRowVO> explain(String sql) {
        return jdbc.query(sql, (rs, rowNum) -> {
            ExplainRowVO vo = new ExplainRowVO();
            vo.setId(rs.getString("id"));
            vo.setSelectType(rs.getString("select_type"));
            vo.setTableName(rs.getString("table"));
            vo.setPartitions(getStringOrNull(rs, "partitions"));
            vo.setType(rs.getString("type"));
            vo.setPossibleKeys(getStringOrNull(rs, "possible_keys"));
            vo.setKey(getStringOrNull(rs, "key"));
            vo.setRows(rs.getString("rows"));
            vo.setExtra(getStringOrNull(rs, "Extra"));
            return vo;
        });
    }

    private String getStringOrNull(java.sql.ResultSet rs, String col) throws java.sql.SQLException {
        try {
            return rs.getString(col);
        } catch (java.sql.SQLException e) {
            return null;
        }
    }
}
