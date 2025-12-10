@echo off
chcp 65001 >nul
echo ========================================
echo MySQL 연결 테스트 실행
echo ========================================
echo.

set JAVA_HOME=C:\Program Files\Java\jdk-17

echo [1/2] 의존성 다운로드 중...
call mvn dependency:copy-dependencies -q
if errorlevel 1 (
    echo ❌ 의존성 다운로드 실패
    pause
    exit /b 1
)

echo [2/2] MySQL 연결 테스트 실행 중...
echo.

java -cp "target/classes;target/lib/*" com.bitbot.MySQLConnectionTest

if errorlevel 1 (
    echo.
    echo ❌ 테스트 실패
) else (
    echo.
    echo ✅ 테스트 완료
)

pause

