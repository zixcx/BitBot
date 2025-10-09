# Geist Mono 폰트 자동 설치 스크립트 (Windows PowerShell)

$ErrorActionPreference = "Stop"  # 에러 발생 시 스크립트 중단

Write-Host "==================================" -ForegroundColor Cyan
Write-Host "Geist Mono 폰트 설치 시작" -ForegroundColor Cyan
Write-Host "==================================" -ForegroundColor Cyan
Write-Host ""

# Node.js 및 npm 확인
try {
    $npmVersion = npm --version
    Write-Host "✓ npm 확인 완료: $npmVersion" -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host "❌ npm이 설치되어 있지 않습니다." -ForegroundColor Red
    Write-Host "Node.js를 먼저 설치해주세요: https://nodejs.org/" -ForegroundColor Yellow
    exit 1
}

# fonts 디렉토리 확인
$fontsDir = "src\main\resources\fonts"
if (-not (Test-Path $fontsDir)) {
    Write-Host "📁 폰트 디렉토리 생성 중..." -ForegroundColor Yellow
    New-Item -ItemType Directory -Path $fontsDir -Force | Out-Null
}

# npm으로 geist 설치
Write-Host "📦 npm으로 Geist 폰트 패키지 다운로드 중..." -ForegroundColor Yellow
npm install --silent geist

# 폰트 파일 복사
Write-Host "📋 Geist Sans 폰트 파일 복사 중..." -ForegroundColor Yellow
Copy-Item "node_modules\geist\dist\fonts\geist-sans\Geist-Regular.ttf" $fontsDir
Copy-Item "node_modules\geist\dist\fonts\geist-sans\Geist-Bold.ttf" $fontsDir
Copy-Item "node_modules\geist\dist\fonts\geist-sans\Geist-Medium.ttf" $fontsDir
Copy-Item "node_modules\geist\dist\fonts\geist-sans\Geist-Light.ttf" $fontsDir

Write-Host "📋 Geist Mono 폰트 파일 복사 중..." -ForegroundColor Yellow
Copy-Item "node_modules\geist\dist\fonts\geist-mono\GeistMono-Regular.ttf" $fontsDir
Copy-Item "node_modules\geist\dist\fonts\geist-mono\GeistMono-Bold.ttf" $fontsDir
Copy-Item "node_modules\geist\dist\fonts\geist-mono\GeistMono-Medium.ttf" $fontsDir
Copy-Item "node_modules\geist\dist\fonts\geist-mono\GeistMono-Light.ttf" $fontsDir

Write-Host "✓ 폰트 파일 복사 완료" -ForegroundColor Green
Write-Host ""

# npm 파일 정리
Write-Host "🧹 임시 파일 정리 중..." -ForegroundColor Yellow
Remove-Item -Recurse -Force "node_modules"
Remove-Item -Force "package-lock.json"
Remove-Item -Force "package.json"

Write-Host ""
Write-Host "==================================" -ForegroundColor Green
Write-Host "✅ 폰트 설치 완료!" -ForegroundColor Green
Write-Host "==================================" -ForegroundColor Green
Write-Host ""
Write-Host "설치된 폰트 파일:" -ForegroundColor Cyan
Get-ChildItem $fontsDir | Where-Object { $_.Name -like "Geist*" } | Format-Table Name, Length -AutoSize
Write-Host ""
Write-Host "이제 'mvn javafx:run' 명령어로 애플리케이션을 실행할 수 있습니다." -ForegroundColor Yellow

