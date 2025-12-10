@echo off
REM BitBot CLI 실행 스크립트
REM 사용법: run-cli.bat

echo ========================================
echo BitBot CLI Tester 실행
echo ========================================

REM JAVA_HOME 설정 (필요시)
set JAVA_HOME=C:\Program Files\Java\jdk-17

REM Maven 컴파일
echo.
echo [1단계] 컴파일 중...
call mvn compile
if errorlevel 1 (
    echo 컴파일 실패!
    pause
    exit /b 1
)

echo.
echo [2단계] 의존성 라이브러리 복사 중...
call mvn dependency:copy-dependencies -DoutputDirectory=target/lib
if errorlevel 1 (
    echo 의존성 복사 실패!
    pause
    exit /b 1
)

echo.
echo [3단계] 프로그램 실행 중...
echo ========================================

REM Java 직접 실행 (의존성 포함)
java -cp "target/classes;target/lib/*" com.bitbot.CLITester

echo.
echo ========================================
echo 실행 완료
pause

