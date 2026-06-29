-- ============================================================
-- 学生专业学习管理信息系统 (SLMS)
-- 逻辑设计：业务主键，属性对应概念 ER
-- MySQL 8.0+ / utf8mb4 / InnoDB
-- ============================================================

CREATE DATABASE IF NOT EXISTS slms
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE slms;

-- ------------------------------------------------------------
-- 1. 学院
-- ------------------------------------------------------------
CREATE TABLE college (
  college_code  VARCHAR(20)  NOT NULL COMMENT '学院编号',
  college_name  VARCHAR(100) NOT NULL COMMENT '学院名称',
  status        VARCHAR(10)  NOT NULL DEFAULT '启用' COMMENT '状态：启用/停用',
  PRIMARY KEY (college_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学院';

-- ------------------------------------------------------------
-- 2. 专业
-- ------------------------------------------------------------
CREATE TABLE major (
  major_code    VARCHAR(20)  NOT NULL COMMENT '专业编号',
  major_name    VARCHAR(100) NOT NULL COMMENT '专业名称',
  college_code  VARCHAR(20)  NOT NULL COMMENT '学院编号',
  status        VARCHAR(10)  NOT NULL DEFAULT '启用' COMMENT '状态：启用/停用',
  PRIMARY KEY (major_code),
  CONSTRAINT fk_major_college
    FOREIGN KEY (college_code) REFERENCES college (college_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专业';

-- ------------------------------------------------------------
-- 3. 学生
-- ------------------------------------------------------------
CREATE TABLE student (
  student_no     VARCHAR(20)  NOT NULL COMMENT '学号',
  name           VARCHAR(50)  NOT NULL COMMENT '姓名',
  college_code   VARCHAR(20)  NOT NULL COMMENT '学院编号',
  major_code     VARCHAR(20)  NOT NULL COMMENT '专业编号',
  age            INT          NOT NULL COMMENT '年龄',
  gender         VARCHAR(10)  NOT NULL COMMENT '性别',
  id_card        VARBINARY(255) NOT NULL COMMENT '身份证号（加密存储）',
  enroll_year    INT          NOT NULL COMMENT '入学年份',
  student_status VARCHAR(10)  NOT NULL DEFAULT '在读' COMMENT '学籍状态',
  deleted        TINYINT      NOT NULL DEFAULT 0 COMMENT '是否删除：0否 1是',
  PRIMARY KEY (student_no),
  CONSTRAINT fk_student_college FOREIGN KEY (college_code) REFERENCES college (college_code),
  CONSTRAINT fk_student_major   FOREIGN KEY (major_code)   REFERENCES major (major_code),
  CONSTRAINT chk_student_age CHECK (age BETWEEN 15 AND 50)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生';

-- ------------------------------------------------------------
-- 4. 教师
-- ------------------------------------------------------------
CREATE TABLE teacher (
  teacher_no   VARCHAR(20) NOT NULL COMMENT '工号',
  name         VARCHAR(50) NOT NULL COMMENT '姓名',
  college_code VARCHAR(20) NOT NULL COMMENT '学院编号',
  status       VARCHAR(10) NOT NULL DEFAULT '在职' COMMENT '状态：在职/停用',
  PRIMARY KEY (teacher_no),
  CONSTRAINT fk_teacher_college FOREIGN KEY (college_code) REFERENCES college (college_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师';

-- ------------------------------------------------------------
-- 5. 系统用户
-- ------------------------------------------------------------
CREATE TABLE sys_user (
  username      VARCHAR(50)  NOT NULL COMMENT '用户名',
  password      VARCHAR(255) NOT NULL COMMENT '密码（哈希存储）',
  real_name     VARCHAR(50)  NOT NULL COMMENT '真实姓名',
  status        VARCHAR(10)  NOT NULL DEFAULT '启用' COMMENT '状态：启用/停用',
  PRIMARY KEY (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户';

-- ------------------------------------------------------------
-- 6. 角色
-- ------------------------------------------------------------
CREATE TABLE role (
  role_code VARCHAR(30) NOT NULL COMMENT '角色代码',
  role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
  PRIMARY KEY (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色';

-- ------------------------------------------------------------
-- 7. 用户角色（M:N 联系「拥有」）
-- ------------------------------------------------------------
CREATE TABLE user_role (
  username  VARCHAR(50) NOT NULL COMMENT '用户名',
  role_code VARCHAR(30) NOT NULL COMMENT '角色代码',
  PRIMARY KEY (username, role_code),
  CONSTRAINT fk_user_role_user FOREIGN KEY (username)  REFERENCES sys_user (username),
  CONSTRAINT fk_user_role_role FOREIGN KEY (role_code) REFERENCES role (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色';

-- ------------------------------------------------------------
-- 8. 课程
-- ------------------------------------------------------------
CREATE TABLE course (
  course_code  VARCHAR(20)    NOT NULL COMMENT '课程编号',
  course_name  VARCHAR(100)   NOT NULL COMMENT '课程名称',
  credit       DECIMAL(3, 1)  NOT NULL COMMENT '学分',
  hours        INT            NOT NULL COMMENT '学时',
  college_code VARCHAR(20)    NOT NULL COMMENT '学院编号',
  nature       VARCHAR(10)    NOT NULL COMMENT '课程性质：必修/选修/公选',
  status       VARCHAR(10)    NOT NULL DEFAULT '启用' COMMENT '状态：启用/停用',
  PRIMARY KEY (course_code),
  CONSTRAINT fk_course_college FOREIGN KEY (college_code) REFERENCES college (college_code),
  CONSTRAINT chk_course_credit CHECK (credit > 0),
  CONSTRAINT chk_course_hours  CHECK (hours > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程';

-- ------------------------------------------------------------
-- 9. 学期
-- ------------------------------------------------------------
CREATE TABLE semester (
  semester_code VARCHAR(20) NOT NULL COMMENT '学期编码',
  semester_name VARCHAR(50) NOT NULL COMMENT '学期名称',
  start_date    DATE        NOT NULL COMMENT '开始日期',
  end_date      DATE        NOT NULL COMMENT '结束日期',
  PRIMARY KEY (semester_code),
  CONSTRAINT chk_semester_date CHECK (end_date > start_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学期';

-- ------------------------------------------------------------
-- 10. 开课计划
-- ------------------------------------------------------------
CREATE TABLE course_offering (
  offering_no     VARCHAR(30) NOT NULL COMMENT '计划编号',
  course_code     VARCHAR(20) NOT NULL COMMENT '课程编号',
  semester_code   VARCHAR(20) NOT NULL COMMENT '学期编码',
  teacher_no      VARCHAR(20) NOT NULL COMMENT '教师工号',
  capacity        INT         NOT NULL COMMENT '容量上限',
  enrolled_count  INT         NOT NULL DEFAULT 0 COMMENT '已选人数',
  schedule        VARCHAR(100) NULL COMMENT '上课时间',
  status          VARCHAR(20) NOT NULL DEFAULT '草稿' COMMENT '状态',
  PRIMARY KEY (offering_no),
  CONSTRAINT fk_offering_course   FOREIGN KEY (course_code)   REFERENCES course (course_code),
  CONSTRAINT fk_offering_semester FOREIGN KEY (semester_code) REFERENCES semester (semester_code),
  CONSTRAINT fk_offering_teacher  FOREIGN KEY (teacher_no)    REFERENCES teacher (teacher_no),
  CONSTRAINT chk_offering_capacity CHECK (capacity > 0),
  CONSTRAINT chk_offering_enrolled CHECK (enrolled_count >= 0 AND enrolled_count <= capacity)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='开课计划';

-- ------------------------------------------------------------
-- 11. 选课（M:N 联系，弱实体）
-- ------------------------------------------------------------
CREATE TABLE enrollment (
  student_no   VARCHAR(20) NOT NULL COMMENT '学号',
  offering_no  VARCHAR(30) NOT NULL COMMENT '计划编号',
  enroll_time  DATETIME    NOT NULL COMMENT '选课时间',
  is_retake    TINYINT     NOT NULL DEFAULT 0 COMMENT '是否重修：0否 1是',
  PRIMARY KEY (student_no, offering_no),
  CONSTRAINT fk_enrollment_student  FOREIGN KEY (student_no)  REFERENCES student (student_no),
  CONSTRAINT fk_enrollment_offering FOREIGN KEY (offering_no) REFERENCES course_offering (offering_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='选课';

-- ------------------------------------------------------------
-- 12. 成绩（与选课 1:1）
-- ------------------------------------------------------------
CREATE TABLE grade (
  student_no    VARCHAR(20)   NOT NULL COMMENT '学号',
  offering_no   VARCHAR(30)   NOT NULL COMMENT '计划编号',
  total_score   DECIMAL(5, 2) NULL COMMENT '总评成绩',
  exam_type     VARCHAR(10)   NOT NULL DEFAULT '正常' COMMENT '考试类型：正常/补考/重修',
  locked        TINYINT       NOT NULL DEFAULT 0 COMMENT '是否锁定：0否 1是',
  academic_year INT           NOT NULL COMMENT '学年',
  PRIMARY KEY (student_no, offering_no),
  CONSTRAINT fk_grade_enrollment
    FOREIGN KEY (student_no, offering_no) REFERENCES enrollment (student_no, offering_no),
  CONSTRAINT chk_grade_score CHECK (total_score IS NULL OR (total_score >= 0 AND total_score <= 100))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成绩';

-- ------------------------------------------------------------
-- 13. 奖惩
-- ------------------------------------------------------------
CREATE TABLE reward_punishment (
  record_no          VARCHAR(30)  NOT NULL COMMENT '记录编号',
  student_no         VARCHAR(20)  NOT NULL COMMENT '学号',
  type               VARCHAR(10)  NOT NULL COMMENT '类型：奖励/惩罚',
  level              VARCHAR(20)  NOT NULL COMMENT '级别',
  reason             VARCHAR(500) NOT NULL COMMENT '原因',
  occur_date         DATE         NOT NULL COMMENT '发生日期',
  recorder_username  VARCHAR(50)  NOT NULL COMMENT '登记用户名',
  archived           TINYINT      NOT NULL DEFAULT 0 COMMENT '是否归档：0否 1是',
  PRIMARY KEY (record_no),
  CONSTRAINT fk_rp_student  FOREIGN KEY (student_no)        REFERENCES student (student_no),
  CONSTRAINT fk_rp_recorder FOREIGN KEY (recorder_username) REFERENCES sys_user (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='奖惩';

-- ------------------------------------------------------------
-- 14. 字典类型
-- ------------------------------------------------------------
CREATE TABLE dict_type (
  type_code VARCHAR(30) NOT NULL COMMENT '类型编码',
  type_name VARCHAR(50) NOT NULL COMMENT '类型名称',
  PRIMARY KEY (type_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典类型';

-- ------------------------------------------------------------
-- 15. 字典项
-- ------------------------------------------------------------
CREATE TABLE dict_item (
  type_code VARCHAR(30) NOT NULL COMMENT '类型编码',
  item_code VARCHAR(30) NOT NULL COMMENT '项编码',
  label     VARCHAR(50) NOT NULL COMMENT '显示名称',
  status    VARCHAR(10) NOT NULL DEFAULT '启用' COMMENT '状态：启用/停用',
  PRIMARY KEY (type_code, item_code),
  CONSTRAINT fk_dict_item_type FOREIGN KEY (type_code) REFERENCES dict_type (type_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典项';

-- ------------------------------------------------------------
-- 16. 审计日志（按学年分区）
-- ------------------------------------------------------------
CREATE TABLE audit_log (
  log_no        BIGINT       NOT NULL AUTO_INCREMENT COMMENT '日志编号',
  table_name    VARCHAR(50)  NOT NULL COMMENT '表名',
  operation     VARCHAR(10)  NOT NULL COMMENT '操作类型',
  record_id     VARCHAR(100) NOT NULL COMMENT '记录标识',
  old_value     JSON         NULL COMMENT '旧值',
  new_value     JSON         NULL COMMENT '新值',
  operator      VARCHAR(50)  NOT NULL COMMENT '操作者',
  operate_time  DATETIME     NOT NULL COMMENT '操作时间',
  academic_year INT          NOT NULL COMMENT '学年',
  PRIMARY KEY (log_no, academic_year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审计日志'
PARTITION BY RANGE (academic_year) (
  PARTITION p2023 VALUES LESS THAN (2024),
  PARTITION p2024 VALUES LESS THAN (2025),
  PARTITION p2025 VALUES LESS THAN (2026),
  PARTITION p_future VALUES LESS THAN MAXVALUE
);

-- ------------------------------------------------------------
-- 索引（高频查询）
-- ------------------------------------------------------------
CREATE INDEX idx_student_college_major ON student (college_code, major_code, student_status);
CREATE INDEX idx_enrollment_offering    ON enrollment (offering_no);
CREATE INDEX idx_grade_academic_year  ON grade (academic_year);
CREATE INDEX idx_offering_semester      ON course_offering (semester_code, status);
