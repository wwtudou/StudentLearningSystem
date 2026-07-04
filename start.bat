@echo off
title SLMS启动程序
chcp 65001 >nul

echo ========================================
echo   学生专业学习管理系统 - 启动程序
echo ========================================
echo.

set BASE_DIR=%~dp0

echo [1/2] 启动后端服务 (Spring Boot :8080)...
start "SLMS-Backend" cmd /c "cd /d "%BASE_DIR%backend" && mvn spring-boot:run"
echo   后端启动中，等待编译...
timeout /t 8 /nobreak >nul

echo [2/2] 启动前端服务 (Vite :5173)...
start "SLMS-Frontend" cmd /c "cd /d "%BASE_DIR%frontend" && npm run dev"
timeout /t 3 /nobreak >nul

echo.
echo ========================================
echo   启动完成！
echo.
echo   前端地址: http://localhost:5173
echo   后端API:  http://localhost:8080
echo   健康检查: http://localhost:8080/api/health
echo.
echo   注意：关闭此窗口不会停止服务，
echo   请在任务管理器中结束 Java、Node 进程。
echo ========================================
echo.
pause