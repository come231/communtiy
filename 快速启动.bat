@echo off
chcp 65001 >nul
echo ======================================
echo  社区项目快速启动脚本
echo ======================================
echo.

:: 设置Java路径（如果JAVA_HOME未设置，请手动修改）
if "%JAVA_HOME%"=="" (
    set JAVA_HOME=C:\Program Files\Java\jdk-17
    echo 警告：未设置JAVA_HOME，使用默认路径：%JAVA_HOME%
)

set PATH=%JAVA_HOME%\bin;%PATH%

:: 检查Java版本
echo 正在检查Java环境...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误：未找到Java环境，请先安装JDK 17！
    pause
    exit /b 1
)

echo Java环境正常
echo.

:: 检查JAR包是否存在
echo 正在检查JAR包...
if not exist "community-user\target\community-user-1.0-SNAPSHOT.jar" (
    echo 错误：未找到JAR包，请先执行编译！
    echo 运行命令：mvn clean package -DskipTests
    pause
    exit /b 1
)

echo JAR包检查完成
echo.

:: 创建日志目录
if not exist "logs" mkdir logs

echo ======================================
echo  开始启动服务
echo ======================================
echo.

:: 启动用户服务
echo [1/9] 正在启动用户服务（端口8096）...
start "用户服务-8096" java -jar community-user\target\community-user-1.0-SNAPSHOT.jar > logs\user.log 2>&1
timeout /t 10 /nobreak >nul
netstat -ano | findstr "8096" >nul
if %errorlevel% equ 0 (
    echo ✓ 用户服务启动成功
echo.
) else (
    echo ✗ 用户服务启动失败，请查看 logs\user.log
)

:: 启动社区内容服务
echo [2/9] 正在启动社区内容服务（端口8091）...
start "社区服务-8091" java -jar community-community\target\community-community-1.0-SNAPSHOT.jar > logs\community.log 2>&1
timeout /t 10 /nobreak >nul
netstat -ano | findstr "8091" >nul
if %errorlevel% equ 0 (
    echo ✓ 社区内容服务启动成功
echo.
) else (
    echo ✗ 社区内容服务启动失败，请查看 logs\community.log
)

:: 启动AI服务
echo [3/9] 正在启动AI服务（端口8092）...
start "AI服务-8092" java -jar community-ai\target\community-ai-1.0-SNAPSHOT.jar > logs\ai.log 2>&1
timeout /t 10 /nobreak >nul
netstat -ano | findstr "8092" >nul
if %errorlevel% equ 0 (
    echo ✓ AI服务启动成功
echo.
) else (
    echo ✗ AI服务启动失败，请查看 logs\ai.log
)

:: 启动互动服务
echo [4/9] 正在启动互动服务（端口8093）...
start "互动服务-8093" java -jar community-interact\target\community-interact-1.0-SNAPSHOT.jar > logs\interact.log 2>&1
timeout /t 10 /nobreak >nul
netstat -ano | findstr "8093" >nul
if %errorlevel% equ 0 (
    echo ✓ 互动服务启动成功
echo.
) else (
    echo ✗ 互动服务启动失败，请查看 logs\interact.log
)

:: 启动搜索服务
echo [5/9] 正在启动搜索服务（端口8094）...
start "搜索服务-8094" java -jar community-search\target\community-search-1.0-SNAPSHOT.jar > logs\search.log 2>&1
timeout /t 10 /nobreak >nul
netstat -ano | findstr "8094" >nul
if %errorlevel% equ 0 (
    echo ✓ 搜索服务启动成功
echo.
) else (
    echo ✗ 搜索服务启动失败，请查看 logs\search.log
)

:: 启动消息服务
echo [6/9] 正在启动消息服务（端口8098）...
start "消息服务-8098" java -jar community-message\target\community-message-1.0-SNAPSHOT.jar > logs\message.log 2>&1
timeout /t 10 /nobreak >nul
netstat -ano | findstr "8098" >nul
if %errorlevel% equ 0 (
    echo ✓ 消息服务启动成功
echo.
) else (
    echo ✗ 消息服务启动失败，请查看 logs\message.log
)

:: 启动文件服务
echo [7/9] 正在启动文件服务（端口8097）...
start "文件服务-8097" java -jar community-file\target\community-file-1.0-SNAPSHOT.jar > logs\file.log 2>&1
timeout /t 10 /nobreak >nul
netstat -ano | findstr "8097" >nul
if %errorlevel% equ 0 (
    echo ✓ 文件服务启动成功
echo.
) else (
    echo ✗ 文件服务启动失败，请查看 logs\file.log
)

:: 启动后台管理服务
echo [8/9] 正在启动后台管理服务（端口8099）...
start "后台服务-8099" java -jar community-admin\target\community-admin-1.0-SNAPSHOT.jar > logs\admin.log 2>&1
timeout /t 10 /nobreak >nul
netstat -ano | findstr "8099" >nul
if %errorlevel% equ 0 (
    echo ✓ 后台管理服务启动成功
echo.
) else (
    echo ✗ 后台管理服务启动失败，请查看 logs\admin.log
)

:: 启动网关服务
echo [9/9] 正在启动网关服务（端口8095）...
start "网关服务-8095" java -jar community-gateway\target\community-gateway-1.0-SNAPSHOT.jar > logs\gateway.log 2>&1
timeout /t 15 /nobreak >nul
netstat -ano | findstr "8095" >nul
if %errorlevel% equ 0 (
    echo ✓ 网关服务启动成功
echo.
) else (
    echo ✗ 网关服务启动失败，请查看 logs\gateway.log
)

echo ======================================
echo  服务启动完成！
echo ======================================
echo.
echo 服务状态检查：
echo.
netstat -ano | findstr "8095 8096 8091 8092 8093 8094 8098 8097 8099" | findstr "LISTENING"
echo.
echo 网关地址：http://localhost:8095
echo 后台管理地址：http://localhost:8099
echo.
echo 日志目录：.\logs\
echo.
echo 按任意键退出...
pause >nul
