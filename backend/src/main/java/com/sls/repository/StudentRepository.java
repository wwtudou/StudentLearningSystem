package com.sls.repository;

import com.sls.dto.OptionVO;
import com.sls.dto.StudentVO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 学生数据访问层（JdbcTemplate 直接操作 SQL）
 */
@Repository
public class StudentRepository {

    private final JdbcTemplate jdbc;

    public StudentRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** 将查询结果行映射为 StudentVO */
    private static final RowMapper<StudentVO> ROW_MAPPER = new RowMapper<>() {
        @Override
        public StudentVO mapRow(ResultSet rs, int rowNum) throws SQLException {
            StudentVO vo = new StudentVO();
            vo.setStudentNo(rs.getString("student_no"));
            vo.setName(rs.getString("name"));
            vo.setCollegeCode(rs.getString("college_code"));
            vo.setCollegeName(rs.getString("college_name"));
            vo.setMajorCode(rs.getString("major_code"));
            vo.setMajorName(rs.getString("major_name"));
            vo.setAge(rs.getInt("age"));
            vo.setGender(rs.getString("gender"));
            vo.setEnrollYear(rs.getInt("enroll_year"));
            vo.setStudentStatus(rs.getString("student_status"));
            return vo;
        }
    };

    /** 统计符合条件的记录数 */
    public long count(String studentNo, String name, String collegeCode,
                      String majorCode, String studentStatus, boolean includeDeleted) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM student s WHERE 1=1 ");
        List<Object> params = buildWhere(sql, studentNo, name, collegeCode, majorCode, studentStatus, includeDeleted);
        Long count = jdbc.queryForObject(sql.toString(), Long.class, params.toArray());
        return count != null ? count : 0;
    }

    /** 分页查询学生（关联学院、专业名称） */
    public List<StudentVO> findPage(String studentNo, String name, String collegeCode,
                                    String majorCode, String studentStatus, boolean includeDeleted,
                                    int offset, int limit) {
        StringBuilder sql = new StringBuilder("""
                SELECT s.student_no, s.name, s.college_code, c.college_name,
                       s.major_code, m.major_name, s.age, s.gender,
                       s.enroll_year, s.student_status
                FROM student s
                JOIN college c ON s.college_code = c.college_code
                JOIN major m ON s.major_code = m.major_code
                WHERE 1=1
                """);
        List<Object> params = buildWhere(sql, studentNo, name, collegeCode, majorCode, studentStatus, includeDeleted);
        sql.append(" ORDER BY s.student_no LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);
        return jdbc.query(sql.toString(), ROW_MAPPER, params.toArray());
    }

    /** 按学号查询单个学生 */
    public StudentVO findByStudentNo(String studentNo) {
        String sql = """
                SELECT s.student_no, s.name, s.college_code, c.college_name,
                       s.major_code, m.major_name, s.age, s.gender,
                       s.enroll_year, s.student_status
                FROM student s
                JOIN college c ON s.college_code = c.college_code
                JOIN major m ON s.major_code = m.major_code
                WHERE s.student_no = ? AND s.deleted = 0
                """;
        List<StudentVO> list = jdbc.query(sql, ROW_MAPPER, studentNo);
        return list.isEmpty() ? null : list.get(0);
    }

    /** 查询加密后的身份证号 */
    public byte[] findIdCardEncrypted(String studentNo) {
        String sql = "SELECT id_card FROM student WHERE student_no = ? AND deleted = 0";
        List<byte[]> list = jdbc.query(sql, (rs, rowNum) -> rs.getBytes("id_card"), studentNo);
        return list.isEmpty() ? null : list.get(0);
    }

    /** 学号是否已存在 */
    public boolean existsByStudentNo(String studentNo) {
        String sql = "SELECT COUNT(*) FROM student WHERE student_no = ?";
        Long count = jdbc.queryForObject(sql, Long.class, studentNo);
        return count != null && count > 0;
    }

    /** 统计学生选课记录数（用于删除前校验） */
    public int countEnrollment(String studentNo) {
        String sql = "SELECT COUNT(*) FROM enrollment WHERE student_no = ?";
        Long count = jdbc.queryForObject(sql, Long.class, studentNo);
        return count != null ? count.intValue() : 0;
    }

    /** 插入新学生 */
    public void insert(String studentNo, String name, String collegeCode, String majorCode,
                       int age, String gender, byte[] idCardEnc, int enrollYear, String studentStatus) {
        String sql = """
                INSERT INTO student (student_no, name, college_code, major_code, age, gender,
                                     id_card, enroll_year, student_status, deleted)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 0)
                """;
        jdbc.update(sql, studentNo, name, collegeCode, majorCode, age, gender,
                idCardEnc, enrollYear, studentStatus);
    }

    /** 更新学生信息 */
    public void update(String studentNo, String name, String collegeCode, String majorCode,
                       int age, String gender, byte[] idCardEnc, int enrollYear, String studentStatus) {
        String sql = """
                UPDATE student SET name=?, college_code=?, major_code=?, age=?, gender=?,
                    id_card=?, enroll_year=?, student_status=?
                WHERE student_no=? AND deleted=0
                """;
        jdbc.update(sql, name, collegeCode, majorCode, age, gender,
                idCardEnc, enrollYear, studentStatus, studentNo);
    }

    /** 逻辑删除：deleted 置为 1 */
    public void logicalDelete(String studentNo) {
        jdbc.update("UPDATE student SET deleted=1 WHERE student_no=? AND deleted=0", studentNo);
    }

    /** 查询学生所属学院（权限范围校验） */
    public String findCollegeCode(String studentNo) {
        List<String> list = jdbc.query(
                "SELECT college_code FROM student WHERE student_no=? AND deleted=0",
                (rs, rowNum) -> rs.getString("college_code"), studentNo);
        return list.isEmpty() ? null : list.get(0);
    }

    /** 校验专业是否属于指定学院 */
    public boolean majorBelongsToCollege(String majorCode, String collegeCode) {
        String sql = "SELECT COUNT(*) FROM major WHERE major_code=? AND college_code=?";
        Long count = jdbc.queryForObject(sql, Long.class, majorCode, collegeCode);
        return count != null && count > 0;
    }

    /** 查询启用的学院列表（下拉用） */
    public List<OptionVO> listColleges() {
        return jdbc.query(
                "SELECT college_code, college_name FROM college WHERE status='启用' ORDER BY college_code",
                (rs, rowNum) -> new OptionVO(rs.getString("college_code"), rs.getString("college_name")));
    }

    /** 查询启用的专业列表（可按学院筛选） */
    public List<OptionVO> listMajors(String collegeCode) {
        if (collegeCode == null || collegeCode.isBlank()) {
            return jdbc.query(
                    "SELECT major_code, major_name FROM major WHERE status='启用' ORDER BY major_code",
                    (rs, rowNum) -> new OptionVO(rs.getString("major_code"), rs.getString("major_name")));
        }
        return jdbc.query(
                "SELECT major_code, major_name FROM major WHERE college_code=? AND status='启用' ORDER BY major_code",
                (rs, rowNum) -> new OptionVO(rs.getString("major_code"), rs.getString("major_name")),
                collegeCode);
    }

    /** 动态拼接 WHERE 条件 */
    private List<Object> buildWhere(StringBuilder sql, String studentNo, String name,
                                    String collegeCode, String majorCode, String studentStatus,
                                    boolean includeDeleted) {
        List<Object> params = new ArrayList<>();
        if (!includeDeleted) {
            sql.append(" AND s.deleted = 0");
        }
        if (studentNo != null && !studentNo.isBlank()) {
            sql.append(" AND s.student_no LIKE ?");
            params.add("%" + studentNo.trim() + "%");
        }
        if (name != null && !name.isBlank()) {
            sql.append(" AND s.name LIKE ?");
            params.add("%" + name.trim() + "%");
        }
        if (collegeCode != null && !collegeCode.isBlank()) {
            sql.append(" AND s.college_code = ?");
            params.add(collegeCode);
        }
        if (majorCode != null && !majorCode.isBlank()) {
            sql.append(" AND s.major_code = ?");
            params.add(majorCode);
        }
        if (studentStatus != null && !studentStatus.isBlank()) {
            sql.append(" AND s.student_status = ?");
            params.add(studentStatus);
        }
        return params;
    }
}
