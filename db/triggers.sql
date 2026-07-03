-- ============================================================
-- DB-Tech-02 触发器
-- ============================================================

USE slms;

DROP TRIGGER IF EXISTS trg_enrollment_after_insert;
DROP TRIGGER IF EXISTS trg_enrollment_after_delete;
DROP TRIGGER IF EXISTS trg_grade_after_update;
DROP TRIGGER IF EXISTS trg_offering_prevent_count_update;

DELIMITER $$

-- 选课后已选人数 +1（须临时允许更新 enrolled_count）
CREATE TRIGGER trg_enrollment_after_insert
AFTER INSERT ON enrollment
FOR EACH ROW
BEGIN
  SET @slms_allow_count_update = 1;
  UPDATE course_offering
    SET enrolled_count = enrolled_count + 1
    WHERE offering_no = NEW.offering_no;
  SET @slms_allow_count_update = NULL;
END$$

-- 退课后已选人数 -1
CREATE TRIGGER trg_enrollment_after_delete
AFTER DELETE ON enrollment
FOR EACH ROW
BEGIN
  SET @slms_allow_count_update = 1;
  UPDATE course_offering
    SET enrolled_count = GREATEST(enrolled_count - 1, 0)
    WHERE offering_no = OLD.offering_no;
  SET @slms_allow_count_update = NULL;
END$$

-- 成绩变更写入 grade_change_log
CREATE TRIGGER trg_grade_after_update
AFTER UPDATE ON grade
FOR EACH ROW
BEGIN
  IF NOT (OLD.total_score <=> NEW.total_score)
     OR NOT (OLD.usual_score <=> NEW.usual_score)
     OR NOT (OLD.final_score <=> NEW.final_score)
     OR NOT (OLD.locked <=> NEW.locked)
     OR NOT (OLD.exam_type <=> NEW.exam_type) THEN
    INSERT INTO grade_change_log (student_no, offering_no, old_value, new_value, changed_by)
    VALUES (
      NEW.student_no,
      NEW.offering_no,
      JSON_OBJECT(
        'usual_score', OLD.usual_score,
        'final_score', OLD.final_score,
        'total_score', OLD.total_score,
        'exam_type', OLD.exam_type,
        'locked', OLD.locked
      ),
      JSON_OBJECT(
        'usual_score', NEW.usual_score,
        'final_score', NEW.final_score,
        'total_score', NEW.total_score,
        'exam_type', NEW.exam_type,
        'locked', NEW.locked
      ),
      USER()
    );
  END IF;
END$$

-- 禁止应用层直接篡改 enrolled_count（须由选课触发器维护）
CREATE TRIGGER trg_offering_prevent_count_update
BEFORE UPDATE ON course_offering
FOR EACH ROW
BEGIN
  IF OLD.enrolled_count <> NEW.enrolled_count
     AND @slms_allow_count_update IS NULL THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'enrolled_count 由选课触发器自动维护，不可手动修改';
  END IF;
END$$

DELIMITER ;

-- 初始化/修复 enrolled_count 时临时允许更新：
-- SET @slms_allow_count_update = 1; UPDATE ...; SET @slms_allow_count_update = NULL;
