cd "D:\2025-2학기\JAVA심화\git\BitBot"

$startDate = Get-Date '2025-09-26'
$endDate = Get-Date '2025-12-30'
$random = New-Object System.Random
$totalCommits = 0
$targetCommits = 1000

Write-Host "=== 랜덤 커밋 생성 시작 ==="
Write-Host "목표: 약 $targetCommits 개 커밋"
Write-Host "날짜 범위: 2025-09-26 ~ 2025-12-30"
Write-Host ""

# 날짜 범위의 일수 계산
$daysDiff = ($endDate - $startDate).Days

# 날짜별 커밋 수를 저장할 딕셔너리
$commitsByDate = @{}

# 랜덤하게 1000개 정도의 커밋을 생성
while ($totalCommits -lt $targetCommits) {
    # 랜덤 날짜 선택
    $randomDays = $random.Next(0, $daysDiff + 1)
    $randomDate = $startDate.AddDays($randomDays)
    $dateStr = $randomDate.ToString('yyyy-MM-dd')
    
    # 해당 날짜에 커밋 추가
    if (-not $commitsByDate.ContainsKey($dateStr)) {
        $commitsByDate[$dateStr] = 0
    }
    
    # 랜덤 개수 (1~10개)
    $commitCount = $random.Next(1, 11)
    
    for ($i = 1; $i -le $commitCount; $i++) {
        Add-Content -Path "README.md" -Value "a"
        git add . | Out-Null
        $env:GIT_AUTHOR_DATE = "$dateStr`T12:00:00"
        $env:GIT_COMMITTER_DATE = "$dateStr`T12:00:00"
        git commit -m "10월31일 커밋" | Out-Null
        $totalCommits++
        $commitsByDate[$dateStr]++
        
        if ($totalCommits -ge $targetCommits) {
            break
        }
    }
    
    # 진행 상황 출력 (100개마다)
    if ($totalCommits % 100 -eq 0) {
        Write-Host "진행: $totalCommits / $targetCommits 커밋 완료" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "=== 모든 커밋 완료! ==="
Write-Host "총 $totalCommits 개 커밋 생성됨" -ForegroundColor Green
Write-Host ""
Write-Host "원격 저장소에 푸시 중..." -ForegroundColor Cyan
git push origin main

