-- ============================================================
-- DB-Tech-06 窗口函数示例查询（课程报告验证用）
-- ============================================================

USE slms;

-- 1. 专业内课程成绩排名（RANK）
-- SELECT student_no, student_name, major_name, course_name, total_score,
--        RANK() OVER (PARTITION BY major_code ORDER BY total_score DESC) AS major_rank
-- FROM v_student_grade_summary
-- WHERE total_score IS NOT NULL AND semester_code = '2025-2026-1';

-- 2. 学生累计获得学分（SUM OVER，仅统计及格）
-- SELECT student_no, student_name, semester_code, course_name, credit, total_score,
--        SUM(CASE WHEN total_score >= 60 THEN credit ELSE 0 END)
--          OVER (PARTITION BY student_no ORDER BY semester_code
--                ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW) AS cumulative_credit
-- FROM v_student_grade_summary
-- WHERE total_score IS NOT NULL
-- ORDER BY student_no, semester_code;

-- 3. DB-Tech-07 分区裁剪验证（audit_log）
-- EXPLAIN SELECT * FROM audit_log WHERE academic_year = 2024;

-- 4. DB-Tech-04 索引对比验证
-- EXPLAIN SELECT * FROM enrollment WHERE offering_no = 'OFF25CS101A';
-- EXPLAIN SELECT * FROM grade WHERE offering_no = 'OFF25CS201';
