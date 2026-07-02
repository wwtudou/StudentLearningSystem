package com.sls.repository;

import com.sls.dto.RewardPunishmentVO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 奖惩数据访问
 */
@Repository
public class RewardPunishmentRepository {

    private final JdbcTemplate jdbc;

    public RewardPunishmentRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public long count(String studentNo, String type, String level, LocalDate start, LocalDate end) {
        StringBuilder sql = new StringBuilder("""
                SELECT COUNT(*) FROM reward_punishment rp WHERE 1=1
                """);
        List<Object> params = buildWhere(sql, studentNo, type, level, start, end);
        Long c = jdbc.queryForObject(sql.toString(), Long.class, params.toArray());
        return c != null ? c : 0;
    }

    public List<RewardPunishmentVO> findPage(String studentNo, String type, String level,
                                             LocalDate start, LocalDate end, int offset, int limit) {
        StringBuilder sql = new StringBuilder("""
                SELECT rp.record_no, rp.student_no, s.name AS student_name, rp.type, rp.level,
                       rp.reason, rp.occur_date, rp.recorder_username, rp.archived
                FROM reward_punishment rp
                JOIN student s ON rp.student_no = s.student_no
                WHERE 1=1
                """);
        List<Object> params = buildWhere(sql, studentNo, type, level, start, end);
        sql.append(" ORDER BY rp.occur_date DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);
        return jdbc.query(sql.toString(), (rs, rowNum) -> mapRow(rs), params.toArray());
    }

    public RewardPunishmentVO findByNo(String recordNo) {
        List<RewardPunishmentVO> list = jdbc.query("""
                SELECT rp.record_no, rp.student_no, s.name AS student_name, rp.type, rp.level,
                       rp.reason, rp.occur_date, rp.recorder_username, rp.archived
                FROM reward_punishment rp
                JOIN student s ON rp.student_no = s.student_no
                WHERE rp.record_no=?
                """, (rs, rowNum) -> mapRow(rs), recordNo);
        return list.isEmpty() ? null : list.get(0);
    }

    public void insert(String recordNo, String studentNo, String type, String level, String reason,
                       LocalDate occurDate, String recorder) {
        jdbc.update("""
                INSERT INTO reward_punishment (record_no, student_no, type, level, reason,
                    occur_date, recorder_username, archived)
                VALUES (?, ?, ?, ?, ?, ?, ?, 0)
                """, recordNo, studentNo, type, level, reason, occurDate, recorder);
    }

    public void update(String recordNo, String type, String level, String reason, LocalDate occurDate) {
        jdbc.update("""
                UPDATE reward_punishment SET type=?, level=?, reason=?, occur_date=?
                WHERE record_no=? AND archived=0
                """, type, level, reason, occurDate, recordNo);
    }

    public void archive(String recordNo) {
        jdbc.update("UPDATE reward_punishment SET archived=1 WHERE record_no=?", recordNo);
    }

    private RewardPunishmentVO mapRow(java.sql.ResultSet rs) throws java.sql.SQLException {
        RewardPunishmentVO vo = new RewardPunishmentVO();
        vo.setRecordNo(rs.getString("record_no"));
        vo.setStudentNo(rs.getString("student_no"));
        vo.setStudentName(rs.getString("student_name"));
        vo.setType(rs.getString("type"));
        vo.setLevel(rs.getString("level"));
        vo.setReason(rs.getString("reason"));
        vo.setOccurDate(rs.getDate("occur_date").toString());
        vo.setRecorderUsername(rs.getString("recorder_username"));
        vo.setArchived(rs.getInt("archived") == 1);
        return vo;
    }

    private List<Object> buildWhere(StringBuilder sql, String studentNo, String type, String level,
                                   LocalDate start, LocalDate end) {
        List<Object> params = new ArrayList<>();
        if (studentNo != null && !studentNo.isBlank()) {
            sql.append(" AND rp.student_no LIKE ?");
            params.add("%" + studentNo.trim() + "%");
        }
        if (type != null && !type.isBlank()) {
            sql.append(" AND rp.type=?");
            params.add(type);
        }
        if (level != null && !level.isBlank()) {
            sql.append(" AND rp.level=?");
            params.add(level);
        }
        if (start != null) {
            sql.append(" AND rp.occur_date >= ?");
            params.add(start);
        }
        if (end != null) {
            sql.append(" AND rp.occur_date <= ?");
            params.add(end);
        }
        return params;
    }
}
