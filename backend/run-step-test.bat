@echo off
chcp 65001 >nul
echo ========================================
echo 단계별 시스템 테스트 실행
echo ========================================
echo.

set JAVA_HOME=C:\Program Files\Java\jdk-17
set PATH=%JAVA_HOME%\bin;%PATH%

echo [1단계] 컴파일 중...
call mvn clean compile -q
if %ERRORLEVEL% NEQ 0 (
    echo ❌ 컴파일 실패
    pause
    exit /b 1
)
echo ✅ 컴파일 성공
echo.

echo [2단계] 테스트 실행 중...
call mvn exec:java -Dexec.mainClass="com.bitbot.StepByStepTest" -q
if %ERRORLEVEL% NEQ 0 (
    echo ❌ 테스트 실패
    pause
    exit /b 1
)

echo.
echo ✅ 모든 테스트 완료!
pause

