package com.sls.repository;

import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Types;

/**
 * 高阶数据库技术：存储过程调用（DB-Tech-01 / DB-Tech-05）
 */
@Repository
public class StoredProcedureRepository {

    private final JdbcTemplate jdbc;

    public StoredProcedureRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** 存储过程返回结果 */
    public record SpResult(int code, String message, int count) {
        public static SpResult of(int code, String message) {
            return new SpResult(code, message, 0);
        }
    }

    /** 调用 sp_enroll_course，含死锁重试（DB-Tech-05） */
    public SpResult enrollCourse(String studentNo, String offeringNo, boolean retake) {
        int maxRetry = 3;
        for (int i = 0; i < maxRetry; i++) {
            try {
                return callEnroll(studentNo, offeringNo, retake ? 1 : 0);
            } catch (DeadlockLoserDataAccessException e) {
                if (i == maxRetry - 1) {
                    return SpResult.of(9998, "操作冲突，请重试");
                }
            }
        }
        return SpResult.of(9998, "操作冲突，请重试");
    }

    /** 调用 sp_drop_course */
    public SpResult dropCourse(String studentNo, String offeringNo) {
        return jdbc.execute((ConnectionCallback<SpResult>) conn -> {
            try (CallableStatement cs = conn.prepareCall("{CALL sp_drop_course(?, ?, ?, ?)}")) {
                cs.setString(1, studentNo);
                cs.setString(2, offeringNo);
                cs.registerOutParameter(3, Types.INTEGER);
                cs.registerOutParameter(4, Types.VARCHAR);
                cs.execute();
                drainResults(cs);
                return SpResult.of(cs.getInt(3), cs.getString(4));
            }
        });
    }

    /** 调用 sp_batch_save_grades */
    public SpResult batchSaveGrades(String offeringNo, String gradesJson) {
        return jdbc.execute((ConnectionCallback<SpResult>) conn -> {
            try (CallableStatement cs = conn.prepareCall(
                    "{CALL sp_batch_save_grades(?, ?, ?, ?, ?)}")) {
                cs.setString(1, offeringNo);
                cs.setString(2, gradesJson);
                cs.registerOutParameter(3, Types.INTEGER);
                cs.registerOutParameter(4, Types.INTEGER);
                cs.registerOutParameter(5, Types.VARCHAR);
                cs.execute();
                drainResults(cs);
                return new SpResult(cs.getInt(3), cs.getString(5), cs.getInt(4));
            }
        });
    }

    private SpResult callEnroll(String studentNo, String offeringNo, int retake) {
        return jdbc.execute((ConnectionCallback<SpResult>) conn -> {
            try (CallableStatement cs = conn.prepareCall("{CALL sp_enroll_course(?, ?, ?, ?, ?)}")) {
                cs.setString(1, studentNo);
                cs.setString(2, offeringNo);
                cs.setInt(3, retake);
                cs.registerOutParameter(4, Types.INTEGER);
                cs.registerOutParameter(5, Types.VARCHAR);
                cs.execute();
                drainResults(cs);
                return SpResult.of(cs.getInt(4), cs.getString(5));
            }
        });
    }

    /** MySQL JDBC：读取 OUT 参数前须消费全部结果集/更新计数 */
    private static void drainResults(CallableStatement cs) throws SQLException {
        while (cs.getUpdateCount() != -1 || cs.getMoreResults()) {
            // advance
        }
    }
}
