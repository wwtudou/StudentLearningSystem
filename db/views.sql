-- ============================================================
-- DB-Tech-03 视图
-- 执行顺序：schema.sql → init-data.sql → views.sql → procedures.sql → triggers.sql → grants.sql
-- ============================================================

USE slms;

DROP VIEW IF EXISTS v_student_grade_summary;
DROP VIEW IF EXISTS v_student_public;

-- 学生成绩一览（多表 JOIN，供报表与 UC-07）
CREATE VIEW v_student_grade_summary AS
SELECT
  s.student_no,
  s.name           AS student_name,
  s.major_code,
  m.major_name,
  c.course_code,
  c.course_name,
  c.credit,
  sem.semester_code,
  sem.semester_name,
  o.offering_no,
  g.usual_score,
  g.final_score,
  g.total_score,
  g.exam_type,
  g.locked,
  g.academic_year,
  e.is_retake
FROM student s
JOIN major m ON s.major_code = m.major_code
JOIN enrollment e ON s.student_no = e.student_no
JOIN course_offering o ON e.offering_no = o.offering_no
JOIN course c ON o.course_code = c.course_code
JOIN semester sem ON o.semester_code = sem.semester_code
LEFT JOIN grade g ON g.student_no = e.student_no AND g.offering_no = e.offering_no
WHERE s.deleted = 0;

-- 学生公开信息（不含身份证密文，脱敏由应用层 AES 解密后处理）
CREATE VIEW v_student_public AS
SELECT
  student_no,
  name,
  college_code,
  major_code,
  age,
  gender,
  enroll_year,
  student_status
FROM student
WHERE deleted = 0;
