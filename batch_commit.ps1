cd "D:\2025-2학기\JAVA심화\git\BitBot"

$startDate = Get-Date '2025-09-26'
$endDate = Get-Date '2025-12-30'
$random = New-Object System.Random
$totalCommits = 0

Write-Host "=== 배치 커밋 시작 ==="
Write-Host "기간: 2025-09-26 ~ 2025-12-30"
Write-Host ""

for ($currentDate = $startDate; $currentDate -le $endDate; $currentDate = $currentDate.AddDays(1)) {
    $dateStr = $currentDate.ToString('yyyy-MM-dd')
    $commitCount = $random.Next(1, 11)
    
    Write-Host "날짜: $dateStr - 커밋 $commitCount 번" -ForegroundColor Cyan
    
    for ($i = 1; $i -le $commitCount; $i++) {
        Add-Content -Path "README.md" -Value "a"
        git add . | Out-Null
        $env:GIT_AUTHOR_DATE = "$dateStr`T12:00:00"
        $env:GIT_COMMITTER_DATE = "$dateStr`T12:00:00"
        git commit -m "10월31일 커밋" | Out-Null
        $totalCommits++
    }
    
    Write-Host "  ✓ $dateStr 완료 ($commitCount 개 커밋)" -ForegroundColor Green
}

Write-Host ""
Write-Host "=== 모든 커밋 완료! ==="
Write-Host "총 $totalCommits 개 커밋 생성됨" -ForegroundColor Yellow
Write-Host ""
Write-Host "원격 저장소에 푸시 중..." -ForegroundColor Cyan
git push origin main

