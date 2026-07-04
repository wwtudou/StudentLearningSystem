-- ============================================================
-- SLMS 高阶数据库技术集中演示脚本（DB-Tech-01 ~ 08）
-- ============================================================
-- 前置：schema → init-data → views → procedures → triggers → grants
-- 用法：Navicat「运行 SQL 文件」整文件执行，或按章节分段执行
-- 注意：不要用 SOURCE 命令；含 DELIMITER 的文件须整文件运行
-- ============================================================

USE slms;

-- ############################################################
-- DB-Tech-01  存储过程（选课演示 · 推荐按顺序执行下面 ①②③）
-- ############################################################

-- ① 演示前：若之前失败过，先退课清状态（无记录时 result=1009 可忽略）
SET @r = 0; SET @m = '';
CALL sp_drop_course('2023001006', 'OFF25CS501', @r, @m);
SELECT @r AS drop_code, @m AS drop_msg;

-- ② 查看选课前人数
SELECT offering_no, enrolled_count, capacity, status
FROM course_offering
WHERE offering_no = 'OFF25CS501';

-- ③ 选课（须已用 root 执行 procedures.sql + triggers.sql）
SET @r = 0; SET @m = '';
CALL sp_enroll_course('2023001006', 'OFF25CS501', 0, @r, @m);
SELECT @r AS result_code, @m AS message;
-- 期望：result_code=0, message=选课成功

-- ④ 重复选课（期望 1002）
SET @r = 0; SET @m = '';
CALL sp_enroll_course('2023001006', 'OFF25CS501', 0, @r, @m);
SELECT @r AS result_code, @m AS message;

-- ⑤ 批量保存成绩
SET @r = 0; SET @c = 0; SET @m = '';
CALL sp_batch_save_grades(
  'OFF25CS101A',
  '[{"studentNo":"2022001002","usualScore":88,"finalScore":92}]',
  @r, @c, @m
);
SELECT @r AS result_code, @c AS saved_count, @m AS message;


-- ############################################################
-- DB-Tech-02  触发器
-- ############################################################

SELECT offering_no, enrolled_count, capacity
FROM course_offering
WHERE offering_no IN ('OFF25CS501', 'OFF25CS101A');

SELECT log_no, student_no, offering_no, old_value, new_value, changed_by, changed_at
FROM grade_change_log
ORDER BY changed_at DESC
LIMIT 10;

-- 预期失败：禁止手改 enrolled_count
-- UPDATE course_offering SET enrolled_count = 99 WHERE offering_no = 'OFF25CS101A';


-- ############################################################
-- DB-Tech-03  视图
-- ############################################################

SELECT student_no, student_name, major_name, course_name, semester_name,
       total_score, exam_type, locked
FROM v_student_grade_summary
WHERE semester_code = '2025-2026-1'
LIMIT 10;

SELECT student_no, name, college_code, major_code, student_status
FROM v_student_public
LIMIT 5;


-- ############################################################
-- DB-Tech-04  索引优化
-- ############################################################

EXPLAIN SELECT * FROM enrollment WHERE offering_no = 'OFF25CS101A';
EXPLAIN SELECT * FROM grade WHERE offering_no = 'OFF25CS101A';
EXPLAIN SELECT * FROM student
WHERE college_code = 'CS' AND major_code = 'CSSE' AND student_status = '在读';


-- ############################################################
-- DB-Tech-05  事务与并发（FOR UPDATE 见 procedures.sql 源码）
-- ############################################################

SELECT offering_no, enrolled_count, capacity,
       capacity - enrolled_count AS remaining
FROM course_offering
WHERE status = '开放选课'
ORDER BY remaining;


-- ############################################################
-- DB-Tech-06  窗口函数
-- ############################################################

SELECT student_no, student_name, major_name, course_name, total_score,
       RANK() OVER (PARTITION BY major_code ORDER BY total_score DESC) AS major_rank
FROM v_student_grade_summary
WHERE total_score IS NOT NULL AND semester_code = '2025-2026-1'
ORDER BY major_name, major_rank
LIMIT 20;

SELECT student_no, student_name, semester_code, course_name, credit, total_score,
       SUM(CASE WHEN total_score >= 60 THEN credit ELSE 0 END)
         OVER (PARTITION BY student_no ORDER BY semester_code
               ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW) AS cumulative_credit
FROM v_student_grade_summary
WHERE total_score IS NOT NULL
ORDER BY student_no, semester_code
LIMIT 30;


-- ############################################################
-- DB-Tech-07  分区表
-- ############################################################

EXPLAIN SELECT * FROM audit_log WHERE academic_year = 2025;
EXPLAIN SELECT * FROM audit_log WHERE academic_year = 2024;

SELECT log_no, table_name, operation, record_id, operator, operate_time, academic_year
FROM audit_log
WHERE academic_year = 2025
LIMIT 5;


-- ############################################################
-- DB-Tech-08  数据库用户权限（须切换连接账号后执行）
-- db_student / 123456dbstudent  |  db_teacher / 123456dbteacher
-- ############################################################

-- 【db_student】应成功：
-- SELECT * FROM v_student_public LIMIT 3;
-- 【db_student】应失败 ERROR 1142：
-- UPDATE grade SET total_score = 100 WHERE student_no = '2022001001' AND offering_no = 'OFF25CS101A';
-- 【db_teacher】应成功：
-- UPDATE grade SET total_score = 85 WHERE student_no = '2022001003' AND offering_no = 'OFF25CS101A' AND locked = 0;
