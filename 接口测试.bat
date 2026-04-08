@echo off
echo ======================================
echo  接口测试脚本
echo ======================================
echo.

set GATEWAY_URL=http://localhost:8095
set CONTENT_TYPE=Content-Type: application/json

:: 检查网关是否启动
echo 检查网关服务...
curl -s %GATEWAY_URL%/actuator/health >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误：网关服务未启动，请先启动服务！
    pause
    exit /b 1
)

echo 网关服务正常
echo.

:: 测试1：用户登录
echo [测试1] 用户登录...
curl -s -X POST %GATEWAY_URL%/user/login ^
  -H "%CONTENT_TYPE%" ^
  -d "{\"username\":\"user001\",\"password\":\"123456\"}" ^
  > test_result_1.json

echo 登录结果：
type test_result_1.json | findstr "code"
echo.

:: 测试2：帖子列表
echo [测试2] 获取帖子列表...
curl -s -X GET "%GATEWAY_URL%/posts/list?pageNum=1&pageSize=5" > test_result_2.json
echo 帖子列表结果：
type test_result_2.json | findstr "code"
echo.

:: 测试3：AI对话
echo [测试3] AI对话测试...
curl -s -X POST %GATEWAY_URL%/ai/chat ^
  -H "%CONTENT_TYPE%" ^
  -d "{\"message\":\"你好，请介绍一下自己\"}" ^
  > test_result_3.json
echo AI回复结果：
type test_result_3.json | findstr "reply"
echo.

:: 测试4：热门帖子
echo [测试4] 获取热门帖子...
curl -s -X GET "%GATEWAY_URL%/posts/hot?pageNum=1&pageSize=5" > test_result_4.json
echo 热门帖子结果：
type test_result_4.json | findstr "code"
echo.

:: 测试5：搜索热词
echo [测试5] 获取搜索热词...
curl -s -X GET %GATEWAY_URL%/hot-keyword/list > test_result_5.json
echo 热词结果：
type test_result_5.json | findstr "code"
echo.

:: 测试6：板块列表
echo [测试6] 获取板块列表...
curl -s -X GET %GATEWAY_URL%/sections/list > test_result_6.json
echo 板块列表结果：
type test_result_6.json | findstr "code"
echo.

echo ======================================
echo  测试完成！
echo ======================================
echo.
echo 详细结果保存在以下文件中：
dir test_result_*.json
echo.
echo 按任意键清理测试文件...
pause >nul

del test_result_*.json
echo 测试文件已清理
echo.
