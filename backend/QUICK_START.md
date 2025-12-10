# 🚀 빠른 시작 가이드

BitBot 자동 거래 시스템 설치 및 사용 가이드

## 📋 1단계: 필수 소프트웨어 설치

### Java 17 설치
1. https://www.oracle.com/java/technologies/downloads/#java17 접속
2. Windows x64 Installer 다운로드 및 설치
3. 설치 확인:
```powershell
java -version
```
예상 출력: `java version "17.0.x"`

### Maven 설치 (Chocolatey 사용)
관리자 권한 PowerShell에서:
```powershell
choco install maven -y
mvn -version
```

또는 수동 설치:
1. https://maven.apache.org/download.cgi 접속
2. Binary zip archive 다운로드
3. 압축 해제 후 PATH에 추가

### JAVA_HOME 설정
관리자 권한 PowerShell에서:
```powershell
[System.Environment]::SetEnvironmentVariable('JAVA_HOME', 'C:\Program Files\Java\jdk-17', 'Machine')
```

현재 세션에만 적용:
```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-17'
```

---

## 🔑 2단계: API 키 발급 및 설정

### Gemini API 키 발급
1. https://aistudio.google.com/apikey 접속
2. "Create API Key" 클릭
3. 키 복사 (예: `AIzaSy...`)

### Binance Testnet API 키 발급
1. https://testnet.binance.vision/ 접속
2. "Generate HMAC_SHA256 Key" 클릭
3. Description: `BitBot-Test` 입력
4. 권한 선택:
   - ✅ **TRADE**: 주문 실행 권한
   - ✅ **USER_DATA**: 계좌 정보 조회 권한
   - ✅ **USER_STREAM**: 실시간 데이터 스트림 권한
5. Generate 클릭 → API Key와 Secret Key 복사

⚠️ **주의**: FIX_API 권한은 필요 없습니다 (Ed25519 키 전용).

### .env 파일 설정
프로젝트 루트 폴더에 `.env` 파일 생성:

```env
# Gemini API (무료)
GEMINI_API_KEY=AIzaSyD9hvkyhrgP1Xy88F1RD5_qfK4xYv2LoC4

# Binance API (Testnet)
BINANCE_API_KEY=your_testnet_api_key
BINANCE_SECRET_KEY=your_testnet_secret_key
BINANCE_USE_TESTNET=true

# Trading Settings
TRADING_MODE=SIMULATION
MAX_INVESTMENT_PERCENT=10
MAX_TOTAL_INVESTMENT_PERCENT=50
STOP_LOSS_PERCENT=-10

# Risk Management
ENABLE_RISK_MANAGEMENT=true
MIN_CONFIDENCE_THRESHOLD=0.70

# Database (MySQL 또는 SQLite)
DB_TYPE=mysql
MYSQL_HOST=203.234.62.223
MYSQL_PORT=3306
MYSQL_DATABASE=bitbot
MYSQL_USERNAME=root
MYSQL_PASSWORD=dsem1010!

# News Data (선택)
CRYPTOPANIC_API_KEY=your_cryptopanic_api_key

# Encryption (선택, API 키 암호화용)
ENCRYPTION_KEY=your_32_character_encryption_key

# Logging
LOG_LEVEL=INFO
LOG_TO_DATABASE=true
```

---

## ▶️ 3단계: 프로젝트 빌드

### 의존성 설치
```bash
mvn dependency:copy-dependencies
```

### 컴파일
```bash
mvn compile
```

---

## 🎮 4단계: 실행

### CLI 테스트 (1회 실행)
```bash
.\run-cli.bat
```

**기능**:
- 환경 설정 확인
- 데이터베이스 연결 테스트
- Binance API 연결 테스트
- 1회 거래 사이클 실행

### 자동 거래 시작
```bash
.\run-auto.bat
```

**기능**:
- 전략별 주기로 자동 거래 실행
- 실시간 손익 모니터링 (1분마다)
- 손절/익절 자동 실행

**중지**: `Ctrl + C`

### 통합 테스트 (여러 사이클)
```bash
.\run-integration-test.bat
```

**기능**:
- 여러 거래 사이클 실행 (기본: 5회)
- 전체 수익률 추적
- 초기/최종 상태 비교
- 거래 통계 출력

### Spring Boot 서버 실행 (REST API)
```bash
.\run-server.bat
```

**기능**:
- Spring Boot 서버 시작 (포트 8080)
- REST API 엔드포인트 제공
- 프론트엔드 개발자용 API 서버

**API 문서**: `API_DOCUMENTATION.md` 참고

### 프로필 리셋
```bash
.\reset-profile.bat
```

**기능**:
- 사용자 프로필 삭제
- 설문조사 데이터 삭제
- 새로운 설문조사 진행 가능

### 단위 테스트 실행
```bash
mvn test
```

**기능**:
- 66개 단위 테스트 실행
- 투자 성향 분류, 입력값 검증, Rate Limit, 암호화 등 테스트

---

## 📝 5단계: 설문조사 (첫 실행 시)

프로그램 실행 시 사용자 프로필이 없으면 설문조사가 자동으로 시작됩니다.

### 설문조사 구성
- **총 15문항** (4개 섹션)
- **재무 상태** (3문항)
- **지식 및 경험** (4문항)
- **위험 감수성** (4문항)
- **거래 스타일 및 시장 관점** (4문항)

### 투자 성향 분류
설문조사 완료 후 자동으로 분류됩니다:

| 점수 범위 | 투자 성향 | 전략 | 레버리지 |
|----------|----------|------|---------|
| 12-20점 | 안정 추구형 | DCA | 1배 (현물) |
| 21-28점 | 위험 중립형 | 추세 추종 | 1배 (현물) |
| 29-36점 | 적극 투자형 | 스윙 트레이딩 | 3배 |
| 37-48점 | 전문 투기형 | 변동성 돌파 | 10배 |

### 전략 및 리스크 설정 자동 적용
- 전략별 실행 주기 자동 설정
- 손절/익절 기준 자동 설정
- 레버리지 자동 적용
- 포지션 크기 자동 조정

---

## 🔍 문제 해결

### `mvn` 명령을 찾을 수 없음
**해결책**:
```powershell
# PowerShell 재시작
# 또는
refreshenv
```
```powershell
#주석처리
refreshenv
```
### JAVA_HOME 오류
**해결책**:
```powershell
# 현재 세션에만 적용
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-17'

# 영구 적용 (관리자 권한 필요)
[System.Environment]::SetEnvironmentVariable('JAVA_HOME', 'C:\Program Files\Java\jdk-17', 'Machine')
```

### API 오류
**확인 사항**:
- `.env` 파일의 API 키 확인
- Testnet 키 사용 여부 확인 (`BINANCE_USE_TESTNET=true`)
- Binance API 키 권한 확인 (TRADE, USER_DATA, USER_STREAM)

### 데이터베이스 오류
**SQLite 사용 시**:
- `data/` 폴더가 생성되었는지 확인
- `data/bitbot.db` 파일 권한 확인
- 프로젝트 폴더에 쓰기 권한 확인

**MySQL 사용 시**:
- `.env` 파일의 MySQL 설정 확인
- MySQL 서버 연결 가능 여부 확인
- 사용자 권한 확인 (`GRANT` 명령어)
- `.\run-mysql-test.bat` 실행하여 연결 테스트

### 컴파일 오류
**해결책**:
```bash
# 클린 빌드
mvn clean compile

# 의존성 재설치
mvn dependency:copy-dependencies
```

---

## 📊 실행 결과 확인

### 정상 실행 시 출력 예시
```
✅ 환경 설정 로드 완료
✅ 데이터베이스 연결 성공
✅ SQLite 데이터베이스 커넥션 풀 초기화 완료
✅ Binance Testnet 모드로 초기화
✅ 사용자 프로필 조회 완료
✅ 거래 사이클 시작
✅ 시장 데이터 수집 완료
✅ LLM 분석 완료
✅ 거래 결정 및 실행
```

### 로그 파일
- 위치: `logs/trading-bot.log`
- 일별 로그: `logs/trading-bot-YYYY-MM-DD.log`

### 데이터베이스
- 위치: `data/bitbot.db`
- SQLite 브라우저로 확인 가능

---

## 💡 주요 명령어

### 빌드 및 실행
```bash
# 컴파일
mvn compile

# 의존성 설치
mvn dependency:copy-dependencies

# 클린 빌드
mvn clean compile

# CLI 테스트
.\run-cli.bat

# 자동 거래
.\run-auto.bat

# 통합 테스트
.\run-integration-test.bat

# Spring Boot 서버 실행
.\run-server.bat

# 프로필 리셋
.\reset-profile.bat

# 단위 테스트
mvn test
```

### 수동 실행 (Java 직접 실행)
```bash
# 의존성 복사 후
java -cp "target/classes;target/lib/*" com.bitbot.CLITester
java -cp "target/classes;target/lib/*" com.bitbot.AutoTradingCLI
java -cp "target/classes;target/lib/*" com.bitbot.IntegrationTest
```

---

## 📈 성능 모니터링

### 실시간 모니터링
- 자동 거래 실행 시 실시간 로그 출력
- 손익 모니터링: 1분마다 계좌 상태 확인
- 손절/익절 자동 실행 시 알림

### 통합 테스트 결과
통합 테스트 실행 시 다음 정보를 확인할 수 있습니다:
- 초기 자산
- 최종 자산
- 총 수익률
- 거래 횟수
- 승률
- 평균 수익/손실

---

## 🎯 다음 단계

1. **시뮬레이션 모드로 테스트**
   - `TRADING_MODE=SIMULATION` 유지
   - 여러 거래 사이클 실행
   - 전략별 동작 확인

2. **투자 성향별 테스트**
   - 프로필 리셋 후 다른 투자 성향으로 테스트
   - 레버리지 적용 확인
   - 손절/익절 동작 확인

3. **실거래 준비** (주의!)
   - 충분한 시뮬레이션 테스트 완료
   - `.env` 파일에서 `TRADING_MODE=LIVE` 설정
   - `BINANCE_USE_TESTNET=false` 설정
   - 실거래 Binance API 키 사용

---

## ⚠️ 주의사항

1. **시뮬레이션 모드 권장**
   - 실거래 전 반드시 시뮬레이션 모드로 충분히 테스트
   - Testnet 키 사용 권장

2. **레버리지 위험**
   - 레버리지는 손익을 배수로 확대
   - 높은 레버리지 = 높은 위험
   - 적극 투자형/전문 투기형은 특히 주의

3. **API 키 보안**
   - `.env` 파일은 절대 공개하지 마세요
   - Git에 커밋하지 마세요 (`.gitignore` 확인)
   - 정기적으로 키 갱신

4. **투자 금액**
   - 절대로 감당할 수 없는 금액을 투자하지 마세요
   - 손실 가능성을 항상 염두에 두세요

---

**자세한 내용은 README.md를 참고하세요.**
