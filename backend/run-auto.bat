@echo off
setlocal

REM JAVA_HOME 설정
set JAVA_HOME=C:\Program Files\Java\jdk-17

echo ========================================
echo BitBot 자동 거래 시스템 실행
echo ========================================
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
echo [3단계] 자동 거래 시스템 실행 중...
echo ========================================
echo.
echo 주의: 이 프로그램은 계속 실행되며 주기적으로 거래를 수행합니다.
echo 종료하려면 'q' 또는 'quit'를 입력하세요.
echo ========================================
echo.

REM Java 직접 실행 (의존성 포함)
java -cp "target/classes;target/lib/*" com.bitbot.AutoTradingCLI

echo.
echo ========================================
echo 실행 완료
pause

