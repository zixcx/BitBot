# 단계별 시스템 테스트 (PowerShell)
# 사용법: .\run-step-test.ps1

$ErrorActionPreference = "Stop"

# JAVA_HOME 설정
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "단계별 시스템 테스트 실행" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1단계: 컴파일
Write-Host "[1단계] 컴파일 중..." -ForegroundColor Yellow
mvn clean compile -q
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ 컴파일 실패" -ForegroundColor Red
    exit 1
}
Write-Host "✅ 컴파일 성공" -ForegroundColor Green
Write-Host ""

# 2단계: 테스트 실행
Write-Host "[2단계] 테스트 실행 중..." -ForegroundColor Yellow
mvn exec:java -Dexec.mainClass="com.bitbot.StepByStepTest" -q
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ 테스트 실패" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "✅ 모든 테스트 완료!" -ForegroundColor Green
Write-Host ""

