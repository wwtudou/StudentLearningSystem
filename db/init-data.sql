-- ============================================================
-- 初始化基础数据
-- ============================================================

USE slms;

-- 角色
INSERT INTO role (role_code, role_name) VALUES
  ('SYS_ADMIN',  '系统管理员'),
  ('DEPT_ADMIN', '院系管理员'),
  ('TEACHER',    '教师'),
  ('STUDENT',    '学生');

-- 默认管理员（密码明文：admin123，后续应用层使用 BCrypt 校验）
INSERT INTO sys_user (username, password, real_name, status) VALUES
  ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', '启用');

INSERT INTO user_role (username, role_code) VALUES
  ('admin', 'SYS_ADMIN');

-- 学院
INSERT INTO college (college_code, college_name, status) VALUES
  ('CS',  '计算机学院', '启用'),
  ('EE',  '电子工程学院', '启用'),
  ('MGT', '管理学院', '启用');

-- 专业
INSERT INTO major (major_code, major_name, college_code, status) VALUES
  ('CSSE', '软件工程', 'CS', '启用'),
  ('CSCE', '计算机科学与技术', 'CS', '启用'),
  ('EECE', '电子信息工程', 'EE', '启用');

-- 学期
INSERT INTO semester (semester_code, semester_name, start_date, end_date) VALUES
  ('2024-2025-1', '2024-2025学年第一学期', '2024-09-01', '2025-01-15'),
  ('2024-2025-2', '2024-2025学年第二学期', '2025-02-20', '2025-07-05');

-- 字典类型
INSERT INTO dict_type (type_code, type_name) VALUES
  ('gender',          '性别'),
  ('student_status',  '学籍状态'),
  ('course_nature',   '课程性质'),
  ('exam_type',       '考试类型'),
  ('offering_status', '开课计划状态');

-- 字典项
INSERT INTO dict_item (type_code, item_code, label, status) VALUES
  ('gender', 'M', '男', '启用'),
  ('gender', 'F', '女', '启用'),
  ('student_status', 'ACTIVE',   '在读', '启用'),
  ('student_status', 'SUSPEND',  '休学', '启用'),
  ('student_status', 'GRADUATE', '毕业', '启用'),
  ('student_status', 'QUIT',     '退学', '启用'),
  ('course_nature', 'REQUIRED',  '必修', '启用'),
  ('course_nature', 'ELECTIVE',  '选修', '启用'),
  ('course_nature', 'PUBLIC',    '公选', '启用'),
  ('exam_type', 'NORMAL',  '正常', '启用'),
  ('exam_type', 'MAKEUP',  '补考', '启用'),
  ('exam_type', 'RETAKE',  '重修', '启用'),
  ('offering_status', 'DRAFT',  '草稿', '启用'),
  ('offering_status', 'OPEN',   '开放选课', '启用'),
  ('offering_status', 'CLOSED', '选课结束', '启用'),
  ('offering_status', 'ENDED',  '已结束', '启用');
