-- ============================================================
-- DB-Tech-01 存储过程 + DB-Tech-05 事务与行锁
-- ============================================================

USE slms;

DROP PROCEDURE IF EXISTS sp_enroll_course;
DROP PROCEDURE IF EXISTS sp_drop_course;
DROP PROCEDURE IF EXISTS sp_batch_save_grades;

DELIMITER $$

-- 选课：FOR UPDATE 行锁 + 事务，enrolled_count 由触发器维护
CREATE PROCEDURE sp_enroll_course(
  IN  p_student_no   VARCHAR(20),
  IN  p_offering_no  VARCHAR(30),
  IN  p_is_retake    TINYINT,
  OUT p_result       INT,
  OUT p_message      VARCHAR(200)
)
proc: BEGIN
  DECLARE v_status       VARCHAR(20);
  DECLARE v_stu_status   VARCHAR(10);
  DECLARE v_capacity     INT;
  DECLARE v_enrolled     INT;
  DECLARE v_semester     VARCHAR(20);
  DECLARE v_schedule     VARCHAR(100);
  DECLARE v_conflict     INT DEFAULT 0;

  DECLARE EXIT HANDLER FOR SQLEXCEPTION
  BEGIN
    ROLLBACK;
    SET p_result = 9999;
    SET p_message = '系统异常，请稍后重试';
  END;

  SET p_result = 0;
  SET p_message = '选课成功';

  START TRANSACTION;

  SELECT status, capacity, enrolled_count, semester_code, schedule
    INTO v_status, v_capacity, v_enrolled, v_semester, v_schedule
    FROM course_offering
    WHERE offering_no = p_offering_no
    FOR UPDATE;

  IF v_status IS NULL THEN
    ROLLBACK;
    SET p_result = 1006;
    SET p_message = '开课计划不存在';
    LEAVE proc;
  END IF;

  SELECT student_status INTO v_stu_status
    FROM student
    WHERE student_no = p_student_no AND deleted = 0;

  IF v_stu_status IS NULL THEN
    ROLLBACK;
    SET p_result = 1006;
    SET p_message = '学生不存在';
    LEAVE proc;
  END IF;

  IF p_is_retake = 0 AND v_stu_status <> '在读' THEN
    ROLLBACK;
    SET p_result = 1005;
    SET p_message = '当前学籍状态不允许选课';
    LEAVE proc;
  END IF;

  IF p_is_retake = 0 AND v_status <> '开放选课' THEN
    ROLLBACK;
    SET p_result = 1007;
    SET p_message = '该开课计划未开放选课';
    LEAVE proc;
  END IF;

  IF EXISTS (
    SELECT 1 FROM enrollment
    WHERE student_no = p_student_no AND offering_no = p_offering_no
  ) THEN
    ROLLBACK;
    SET p_result = 1002;
    SET p_message = '您已选择该课程';
    LEAVE proc;
  END IF;

  IF v_enrolled >= v_capacity THEN
    ROLLBACK;
    SET p_result = 1001;
    SET p_message = '该课程已满员，请选择其他教学班';
    LEAVE proc;
  END IF;

  -- 同学期上课时间冲突（简化：schedule 字符串相同视为冲突）
  IF v_schedule IS NOT NULL AND v_schedule <> '' THEN
    SELECT COUNT(*) INTO v_conflict
    FROM enrollment e
    JOIN course_offering o ON e.offering_no = o.offering_no
    WHERE e.student_no = p_student_no
      AND o.semester_code = v_semester
      AND o.schedule = v_schedule;

    IF v_conflict > 0 THEN
      ROLLBACK;
      SET p_result = 1004;
      SET p_message = '与已选课程时间冲突';
      LEAVE proc;
    END IF;
  END IF;

  INSERT INTO enrollment (student_no, offering_no, enroll_time, is_retake)
  VALUES (p_student_no, p_offering_no, NOW(), p_is_retake);

  COMMIT;
END proc$$

-- 退课：删除选课记录，enrolled_count 由触发器 -1
CREATE PROCEDURE sp_drop_course(
  IN  p_student_no   VARCHAR(20),
  IN  p_offering_no  VARCHAR(30),
  OUT p_result       INT,
  OUT p_message      VARCHAR(200)
)
proc: BEGIN
  DECLARE v_status VARCHAR(20);

  DECLARE EXIT HANDLER FOR SQLEXCEPTION
  BEGIN
    ROLLBACK;
    SET p_result = 9999;
    SET p_message = '系统异常，请稍后重试';
  END;

  SET p_result = 0;
  SET p_message = '退课成功';

  START TRANSACTION;

  SELECT status INTO v_status
    FROM course_offering
    WHERE offering_no = p_offering_no
    FOR UPDATE;

  IF v_status IS NULL THEN
    ROLLBACK;
    SET p_result = 1006;
    SET p_message = '开课计划不存在';
    LEAVE proc;
  END IF;

  IF v_status IN ('已结束', '草稿') THEN
    ROLLBACK;
    SET p_result = 1008;
    SET p_message = '当前不在退课期内';
    LEAVE proc;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM enrollment
    WHERE student_no = p_student_no AND offering_no = p_offering_no
  ) THEN
    ROLLBACK;
    SET p_result = 1009;
    SET p_message = '未找到选课记录';
    LEAVE proc;
  END IF;

  DELETE FROM grade
    WHERE student_no = p_student_no AND offering_no = p_offering_no;

  DELETE FROM enrollment
    WHERE student_no = p_student_no AND offering_no = p_offering_no;

  COMMIT;
END proc$$

-- 批量保存成绩：JSON 数组，总评 = 平时*0.3 + 期末*0.7
CREATE PROCEDURE sp_batch_save_grades(
  IN  p_offering_no   VARCHAR(30),
  IN  p_grades_json   JSON,
  OUT p_result        INT,
  OUT p_count         INT,
  OUT p_message       VARCHAR(200)
)
proc: BEGIN
  DECLARE v_i       INT DEFAULT 0;
  DECLARE v_len     INT;
  DECLARE v_stu     VARCHAR(20);
  DECLARE v_usual   DECIMAL(5,2);
  DECLARE v_final   DECIMAL(5,2);
  DECLARE v_total   DECIMAL(5,2);
  DECLARE v_locked  TINYINT;
  DECLARE v_year    INT DEFAULT YEAR(CURDATE());

  DECLARE EXIT HANDLER FOR SQLEXCEPTION
  BEGIN
    ROLLBACK;
    SET p_result = 9999;
    SET p_message = '批量保存失败';
    SET p_count = 0;
  END;

  SET p_result = 0;
  SET p_count = 0;
  SET p_message = '保存成功';
  SET v_len = JSON_LENGTH(p_grades_json);

  IF v_len IS NULL OR v_len = 0 THEN
    SET p_result = 2001;
    SET p_message = '成绩数据为空';
    LEAVE proc;
  END IF;

  START TRANSACTION;

  WHILE v_i < v_len DO
    SET v_stu     = JSON_UNQUOTE(JSON_EXTRACT(p_grades_json, CONCAT('$[', v_i, '].studentNo')));
    SET v_usual   = JSON_EXTRACT(p_grades_json, CONCAT('$[', v_i, '].usualScore'));
    SET v_final   = JSON_EXTRACT(p_grades_json, CONCAT('$[', v_i, '].finalScore'));
    SET v_locked  = 0;

    IF v_usual IS NULL OR v_final IS NULL
       OR v_usual < 0 OR v_usual > 100 OR v_final < 0 OR v_final > 100 THEN
      ROLLBACK;
      SET p_result = 2002;
      SET p_message = CONCAT('成绩须在0到100之间，学号=', IFNULL(v_stu, '?'));
      SET p_count = 0;
      LEAVE proc;
    END IF;

    IF NOT EXISTS (
      SELECT 1 FROM enrollment
      WHERE student_no = v_stu AND offering_no = p_offering_no
    ) THEN
      ROLLBACK;
      SET p_result = 2003;
      SET p_message = CONCAT('选课记录不存在，学号=', v_stu);
      SET p_count = 0;
      LEAVE proc;
    END IF;

    SELECT locked INTO v_locked FROM grade
      WHERE student_no = v_stu AND offering_no = p_offering_no
      LIMIT 1;

    IF v_locked = 1 THEN
      ROLLBACK;
      SET p_result = 2004;
      SET p_message = CONCAT('成绩已锁定，学号=', v_stu);
      SET p_count = 0;
      LEAVE proc;
    END IF;

    SET v_total = ROUND(v_usual * 0.3 + v_final * 0.7, 2);

    IF EXISTS (
      SELECT 1 FROM grade WHERE student_no = v_stu AND offering_no = p_offering_no
    ) THEN
      UPDATE grade
        SET usual_score = v_usual, final_score = v_final, total_score = v_total
        WHERE student_no = v_stu AND offering_no = p_offering_no AND locked = 0;
    ELSE
      INSERT INTO grade (student_no, offering_no, usual_score, final_score, total_score, exam_type, locked, academic_year)
      VALUES (v_stu, p_offering_no, v_usual, v_final, v_total, '正常', 0, v_year);
    END IF;

    SET p_count = p_count + 1;
    SET v_i = v_i + 1;
  END WHILE;

  COMMIT;
END proc$$

DELIMITER ;
