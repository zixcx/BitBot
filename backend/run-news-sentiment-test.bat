@echo off
chcp 65001 >nul
echo ========================================
echo 뉴스 데이터 및 공포/탐욕 지수 통합 테스트
echo ========================================
echo.

REM JAVA_HOME 설정
set JAVA_HOME=C:\Program Files\Java\jdk-17

REM 환경 변수 로드
if exist .env (
    for /f "usebackq tokens=1,* delims==" %%a in (".env") do (
        set "%%a=%%b"
    )
)

REM 의존성 복사
echo [1/3] 의존성 복사 중...
call mvn dependency:copy-dependencies -q -DoutputDirectory=target/lib
if errorlevel 1 (
    echo ❌ 의존성 복사 실패
    pause
    exit /b 1
)

REM 컴파일
echo [2/3] 컴파일 중...
call mvn compile -q
if errorlevel 1 (
    echo ❌ 컴파일 실패
    pause
    exit /b 1
)

REM 실행
echo [3/3] 테스트 실행 중...
echo.
java -cp "target/classes;target/lib/*" com.bitbot.NewsAndSentimentTest

if errorlevel 1 (
    echo.
    echo ❌ 테스트 실패
    pause
    exit /b 1
) else (
    echo.
    echo ✅ 테스트 완료
)

pause

