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

1. 启动 MySQL，修改 `backend/src/main/resources/application.yml` 中的数据库账号密码（默认 root/root）

2. 执行建表脚本：

```bash
mysql -u root -p < db/schema.sql
mysql -u root -p < db/init-data.sql
```

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

## 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | 系统管理员 |

> 密码哈希已写入 init-data.sql，登录功能待后续实现。
