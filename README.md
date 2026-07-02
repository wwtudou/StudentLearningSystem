# 学生专业学习管理信息系统 (SLMS)

Spring Boot + Vue3 + MySQL 8.0

## 项目结构

```
StudentLearningSystem/
├── backend/          # Spring Boot 后端
├── frontend/         # Vue3 + Vite 前端
├── db/               # 数据库脚本
│   ├── schema.sql    # 建表（16 张表，业务主键）
│   └── init-data.sql # 初始数据
└── docs/             # 需求与设计文档
```

## 环境要求

- JDK 17+
- Maven 3.8+
- Node.js 18+
- MySQL 8.0+

## 数据库初始化

1. 启动 MySQL，配置本机数据库连接（见下方「本地配置」）

2. 执行建表脚本（**须运行整个 schema.sql 文件**，不要只选中部分语句）：

```bash
mysql -u root -p < db/schema.sql
mysql -u root -p < db/init-data.sql
```

若 `USE slms` 报 **1049 Unknown database**，说明 `slms` 库未创建成功，请用 root 执行，或先手动建库：

```sql
CREATE DATABASE slms DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

`init-data.sql` 会**清空并重建**全部演示数据，可重复执行。数据概览：

| 表 | 数量 | 说明 |
|----|------|------|
| 学院 / 专业 | 3 / 6 | 含 1 个停用专业 |
| 学生 | 20 | 含在读、休学、毕业、退学 |
| 教师 | 7 | 含 1 名停用 |
| 课程 | 10 | 必修/选修/公选 |
| 开课计划 | 11 | 草稿、开放选课、选课结束、已结束 |
| 选课 | 52 | enrolled_count 与记录一致 |
| 成绩 | 29 | 含不及格、补考、锁定样例 |
| 奖惩 | 8 | 含已归档记录 |
| 用户 | 8 | admin、院系管理员、教师、学生 |
| 审计日志 | 5 | 操作追溯样例 |

演示账号密码采用固定规则 **序号 + 用户类型**（BCrypt 存储，登录时输入明文）：

| 用户名 | 密码 |
|--------|------|
| admin | `1admin6` |
| dept_cs | `1dept` |
| dept_ee | `2dept` |
| teacher01 / 02 / 03 | `1teacher` / `2teacher` / `3teacher` |
| stu2022001 / stu2023001 | `1student` / `2student` |

> 请勿在 SQL 中直接写 `1admin6` 这类明文作为 password 值；`password` 列存的是 BCrypt 哈希，见 init-data.sql 中的 `@pwd_xxx` 变量。

学生身份证密文使用密钥 **`SLMS-SECRET-KEY1`** 加密，请确保 `application-local.yml` 中 `slms.id-card-secret` 与该值一致（`application-local.yml.example` 已默认配置）。

重新生成身份证密文可运行：`cd db && javac GenIdCardHex.java && java GenIdCardHex SLMS-SECRET-KEY1`

## 本地配置（数据库密码）

个人数据库密码**不要**写在 `application.yml` 里提交 Git。

首次克隆项目后执行：

```bash
cd backend/src/main/resources
copy application-local.yml.example application-local.yml   # Windows
# cp application-local.yml.example application-local.yml   # Linux/macOS
```

然后编辑 `application-local.yml`，填入本机 MySQL 用户名和密码。该文件已在 `.gitignore` 中忽略。

`application.yml` 仅保留公共配置，并通过 `optional:application-local.yml` 自动加载本地覆盖项。

## 启动前端

```bash
cd frontend
npm install
npm run dev
```

浏览器访问 http://localhost:5173 ，使用 `admin` / `1admin6` 登录。

## 功能模块

| 模块 | 路径 | 说明 |
|------|------|------|
| 学生管理 | /students | FR-01 |
| 院系专业 | /org | FR-03 |
| 字典维护 | /dicts | FR-04 |
| 教师管理 | /teachers | 开课计划依赖 |
| 课程/开课 | /courses | FR-05 |
| 奖惩管理 | /rewards | FR-02 |
| 选课 | /enrollments | FR-06 |
| 成绩 | /grades | FR-06 |
| 报表 | /reports | FR-07 |
| 用户权限 | /users | FR-08 |

## 启动后端

```bash
cd backend
mvn spring-boot:run
```

健康检查：http://localhost:8080/api/health

## 启动前端

```bash
cd frontend
npm install
npm run dev
```

访问：http://localhost:5173

## 逻辑表与物理表对照

| 逻辑关系 | 物理表名 |
|----------|----------|
| 学院 | college |
| 专业 | major |
| 学生 | student |
| 教师 | teacher |
| 系统用户 | sys_user |
| 角色 | role |
| 用户角色 | user_role |
| 课程 | course |
| 学期 | semester |
| 开课计划 | course_offering |
| 选课 | enrollment |
| 成绩 | grade |
| 奖惩 | reward_punishment |
| 字典类型 | dict_type |
| 字典项 | dict_item |
| 审计日志 | audit_log |

主键均为业务主键（学号、课程编号、联合主键等），与关系模式设计一致。

## 学生管理 API

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/students | 分页查询（学号、姓名、学院、专业、学籍状态） |
| GET | /api/students/{studentNo} | 详情 |
| POST | /api/students | 新增 |
| PUT | /api/students/{studentNo} | 修改（学号不可改） |
| DELETE | /api/students/{studentNo} | 逻辑删除 |
| GET | /api/students/options/colleges | 学院下拉 |
| GET | /api/students/options/majors | 专业下拉 |

前端入口：http://localhost:5173/students

## 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | 1admin6 | 系统管理员 |
| dept_cs | 1dept | 院系管理员 |
| teacher01 | 1teacher | 教师 |
| stu2022001 | 1student | 学生 |

> 密码规则：同类型用户按序号编号，如 `1dept`、`2teacher`
