cd "D:\2025-2학기\JAVA심화\git\BitBot"

for ($i = 1; $i -le 7; $i++) {
    # README.md에 'a' 추가
    Add-Content -Path "README.md" -Value "a"
    
    # Git add
    git add .
    
    # 환경 변수 설정 및 커밋
    $env:GIT_AUTHOR_DATE = "2025-10-10T12:00:00"
    $env:GIT_COMMITTER_DATE = "2025-10-10T12:00:00"
    git commit -m "10월31일 커밋"
    
    Write-Host "커밋 $i 완료"
}

# 마지막에 push
Write-Host "원격 저장소에 푸시 중..."
git push origin main

