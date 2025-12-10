@echo off
chcp 65001 >nul
echo ========================================
echo REST API 테스트 실행
echo ========================================
echo.
echo ⚠️  주의: 먼저 서버를 실행해야 합니다!
echo    서버 실행: .\run-server.bat
echo.
pause

set JAVA_HOME=C:\Program Files\Java\jdk-17
set PATH=%JAVA_HOME%\bin;%PATH%

echo [1단계] 컴파일 중...
call mvn compile -q
if %ERRORLEVEL% NEQ 0 (
    echo ❌ 컴파일 실패
    pause
    exit /b 1
)
echo ✅ 컴파일 성공
echo.

echo [2단계] API 테스트 실행 중...
echo ========================================
echo.
call mvn exec:java -Dexec.mainClass="com.bitbot.ApiTest"
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ 테스트 실패
    pause
    exit /b 1
)

echo.
echo ✅ 모든 API 테스트 완료!
pause

