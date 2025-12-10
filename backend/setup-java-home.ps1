# ===================================
# Java JAVA_HOME 자동 설정 스크립트
# ===================================
# 관리자 권한 PowerShell에서 실행하세요
# 실행: .\setup-java-home.ps1

Write-Host "=".PadRight(60, '=')
Write-Host "Java JAVA_HOME 설정 스크립트"
Write-Host "=".PadRight(60, '=')

# Java 설치 경로 찾기
$javaPath = (Get-Command java -ErrorAction SilentlyContinue).Source

if ($javaPath) {
    Write-Host "✅ Java 발견: $javaPath"
    
    # JAVA_HOME 경로 추출 (bin 디렉토리 제외)
    $javaHome = Split-Path (Split-Path $javaPath -Parent) -Parent
    
    Write-Host "Java 설치 디렉토리: $javaHome"
    
    # 시스템 환경 변수 설정
    Write-Host "`n시스템 환경 변수 JAVA_HOME 설정 중..."
    
    try {
        [System.Environment]::SetEnvironmentVariable('JAVA_HOME', $javaHome, 'Machine')
        Write-Host "✅ JAVA_HOME 설정 완료: $javaHome"
        
        # 현재 세션에도 적용
        $env:JAVA_HOME = $javaHome
        
        Write-Host "`n=".PadRight(60, '=')
        Write-Host "설정 완료!"
        Write-Host "=".PadRight(60, '=')
        Write-Host "`n⚠️  새 PowerShell 창을 열어서 확인하세요:"
        Write-Host "mvn -version"
        
    } catch {
        Write-Host "❌ 오류 발생: $_"
        Write-Host "`n수동으로 설정하세요:"
        Write-Host "1. Win + X → 시스템"
        Write-Host "2. 고급 시스템 설정 → 환경 변수"
        Write-Host "3. JAVA_HOME = $javaHome"
    }
    
} else {
    Write-Host "❌ Java를 찾을 수 없습니다."
    Write-Host "`nJava가 설치되어 있는지 확인하세요:"
    Write-Host "java -version"
}


