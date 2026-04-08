@echo off
echo ======================================
echo  编译项目
echo ======================================
echo.

:: 检查Maven
echo 正在检查Maven环境...
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误：未找到Maven，请先安装Maven！
    pause
    exit /b 1
)

echo Maven环境正常
echo.

:: 执行编译
echo 开始编译项目（跳过测试）...
mvn clean package -DskipTests

if %errorlevel% equ 0 (
    echo.
    echo ======================================
    echo  编译成功！
    echo ======================================
    echo.
    echo JAR包位置：
    dir /s /b target\*.jar
) else (
    echo.
    echo ======================================
    echo  编译失败！
    echo ======================================
    echo 请查看错误信息并修复
)

echo.
pause
