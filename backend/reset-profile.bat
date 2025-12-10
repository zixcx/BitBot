@echo off
REM 프로필 및 설문조사 데이터 초기화 스크립트
REM 사용법: reset-profile.bat

echo ========================================
echo 프로필 및 설문조사 데이터 초기화
echo ========================================
echo.
echo ⚠️ 경고: 이 작업은 사용자 프로필과 설문조사 데이터를 삭제합니다.
echo 거래 내역은 유지됩니다.
echo.
set /p confirm="계속하시겠습니까? (y/n): "
if /i not "%confirm%"=="y" (
    echo 취소되었습니다.
    pause
    exit /b 0
)

echo.
echo [1단계] 데이터베이스 초기화 중...

REM JAVA_HOME 설정
set JAVA_HOME=C:\Program Files\Java\jdk-17

REM Java로 SQL 실행
java -cp "target/classes;target/lib/*" -Dexec.mainClass="com.bitbot.ResetProfile" 2>nul

if errorlevel 1 (
    echo.
    echo [대안] 데이터베이스 파일 삭제 중...
    if exist "data\bitbot.db" (
        del /q "data\bitbot.db"
        del /q "data\bitbot.db-shm" 2>nul
        del /q "data\bitbot.db-wal" 2>nul
        echo ✅ 데이터베이스 파일 삭제 완료
    ) else (
        echo ℹ️ 데이터베이스 파일이 없습니다.
    )
)

echo.
echo ========================================
echo 초기화 완료!
echo ========================================
echo.
echo 이제 설문조사를 다시 진행할 수 있습니다:
echo   .\run-cli.bat
echo.
pause

