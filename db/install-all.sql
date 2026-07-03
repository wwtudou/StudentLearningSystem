-- ============================================================
-- 一键安装：建库 + 演示数据 + 高阶数据库对象
-- 用法：mysql -u root -p < db/install-all.sql
-- ============================================================

SOURCE schema.sql;
SOURCE init-data.sql;
SOURCE views.sql;
SOURCE procedures.sql;
SOURCE triggers.sql;
SOURCE grants.sql;
