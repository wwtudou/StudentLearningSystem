-- ============================================================
-- DB-Tech-08 数据库用户权限（最小权限原则）
-- 演示密码：123456 + 角色标识（与 sys_user 规则类似）
-- ============================================================

USE slms;

-- 清理旧用户（可选，首次执行）
DROP USER IF EXISTS 'db_admin'@'localhost';
DROP USER IF EXISTS 'db_dept'@'localhost';
DROP USER IF EXISTS 'db_teacher'@'localhost';
DROP USER IF EXISTS 'db_student'@'localhost';

CREATE USER 'db_admin'@'localhost'   IDENTIFIED BY '123456dbadmin';
CREATE USER 'db_dept'@'localhost'    IDENTIFIED BY '123456dbdept';
CREATE USER 'db_teacher'@'localhost' IDENTIFIED BY '123456dbteacher';
CREATE USER 'db_student'@'localhost' IDENTIFIED BY '123456dbstudent';

-- 系统管理员：全部权限
GRANT ALL PRIVILEGES ON slms.* TO 'db_admin'@'localhost';

-- 院系管理员：组织、学生、奖惩读写；其他只读
GRANT SELECT, INSERT, UPDATE ON slms.college TO 'db_dept'@'localhost';
GRANT SELECT, INSERT, UPDATE ON slms.major TO 'db_dept'@'localhost';
GRANT SELECT, INSERT, UPDATE ON slms.student TO 'db_dept'@'localhost';
GRANT SELECT, INSERT, UPDATE ON slms.reward_punishment TO 'db_dept'@'localhost';
GRANT SELECT ON slms.* TO 'db_dept'@'localhost';

-- 教师：课程/开课/选课/学生只读；成绩读写；批量成绩存储过程
GRANT SELECT ON slms.course TO 'db_teacher'@'localhost';
GRANT SELECT ON slms.course_offering TO 'db_teacher'@'localhost';
GRANT SELECT ON slms.enrollment TO 'db_teacher'@'localhost';
GRANT SELECT ON slms.student TO 'db_teacher'@'localhost';
GRANT SELECT ON slms.semester TO 'db_teacher'@'localhost';
GRANT SELECT ON slms.teacher TO 'db_teacher'@'localhost';
GRANT SELECT, INSERT, UPDATE ON slms.grade TO 'db_teacher'@'localhost';
GRANT SELECT ON slms.v_student_grade_summary TO 'db_teacher'@'localhost';
GRANT EXECUTE ON PROCEDURE slms.sp_batch_save_grades TO 'db_teacher'@'localhost';

-- 学生：脱敏视图与成绩视图只读；选课/退课存储过程
GRANT SELECT ON slms.v_student_public TO 'db_student'@'localhost';
GRANT SELECT ON slms.v_student_grade_summary TO 'db_student'@'localhost';
GRANT SELECT ON slms.course TO 'db_student'@'localhost';
GRANT SELECT ON slms.course_offering TO 'db_student'@'localhost';
GRANT SELECT ON slms.enrollment TO 'db_student'@'localhost';
GRANT EXECUTE ON PROCEDURE slms.sp_enroll_course TO 'db_student'@'localhost';
GRANT EXECUTE ON PROCEDURE slms.sp_drop_course TO 'db_student'@'localhost';

FLUSH PRIVILEGES;

-- 越权验证示例（以 db_student 登录后执行，应报错）：
-- UPDATE slms.grade SET total_score = 100 WHERE student_no = '2022001001';
