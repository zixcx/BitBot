@echo off
chcp 65001 >nul
echo ========================================
echo BitBot Trading Server 시작
echo ========================================
echo.

REM JAVA_HOME 설정
set JAVA_HOME=C:\Program Files\Java\jdk-17

REM 환경 변수 로드
if not exist .env (
    echo [오류] .env 파일이 없습니다!
    echo .env.example을 참고하여 .env 파일을 생성하세요.
    pause
    exit /b 1
)

REM Maven 컴파일
echo [1/2] 프로젝트 컴파일 중...
call mvn clean compile -q
if errorlevel 1 (
    echo [오류] 컴파일 실패!
    pause
    exit /b 1
)

REM 의존성 복사
echo [2/2] 의존성 복사 중...
call mvn dependency:copy-dependencies -q
if errorlevel 1 (
    echo [오류] 의존성 복사 실패!
    pause
    exit /b 1
)

echo.
echo ========================================
echo 서버 시작 중...
echo REST API: http://localhost:8080/api
echo ========================================
echo.

REM Spring Boot 서버 실행
java -cp "target/classes;target/lib/*" com.bitbot.server.TradingBotServerApplication

pause

