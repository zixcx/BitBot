# 🤖 BitBot - LLM 기반 자동 거래 시스템

투자 성향 기반의 지능형 비트코인 자동 거래 시스템

## 📋 주요 기능

- ✅ **투자 성향 분류**: 15문항 설문조사로 4가지 투자 유형 자동 분류
- 🤖 **LLM 에이전트**: Google Gemini 2.5 Flash를 활용한 기술적/심리 분석
- 📊 **전략별 거래**: DCA, 추세 추종, 스윙, 변동성 돌파 4가지 전략
- ⚡ **레버리지 거래**: 투자 성향별 자동 레버리지 적용 (최대 10배)
- 🛡️ **리스크 관리**: 손절/익절 자동 실행 및 실시간 모니터링 (1분마다)
- 💾 **데이터베이스**: MySQL/SQLite 지원 (원격 서버 또는 로컬 파일)
- 📈 **실시간 손익 추적**: 계좌 손익 실시간 모니터링 및 자동 대응
- 📰 **뉴스 데이터 통합**: CryptoPanic/Google News를 통한 시장 뉴스 수집
- 📊 **공포/탐욕 지수**: Alternative.me API를 통한 시장 심리 지수 수집
- 🔔 **모니터링 및 알림**: 손절/익절 실행 시 알림, 시스템 상태 모니터링
- 🧪 **단위 테스트**: 66개 테스트 케이스 (모두 통과)
- 🌐 **REST API 서버**: Spring Boot 기반 REST API 제공

## 🚀 빠른 시작

### 1. 필수 요구사항
- Java 17 이상
- Maven 3.6+
- Windows 10/11 (PowerShell)

### 2. API 키 설정
`.env` 파일에 다음 키를 입력하세요:
```env
GEMINI_API_KEY=your_gemini_api_key
BINANCE_API_KEY=your_binance_testnet_key
BINANCE_SECRET_KEY=your_binance_testnet_secret
BINANCE_USE_TESTNET=true
TRADING_MODE=SIMULATION
```

### 3. 실행
```bash
# CLI 테스트 (1회 실행)
.\run-cli.bat

# 자동 거래 시작
.\run-auto.bat

# 통합 테스트 (여러 사이클 실행)
.\run-integration-test.bat

# Spring Boot 서버 실행 (REST API)
.\run-server.bat

# 프로필 리셋 (설문조사 다시 하기)
.\reset-profile.bat

# 단위 테스트 실행
mvn test
```

## 📖 상세 가이드

- **백엔드 개발자**: 자세한 설치 및 사용 방법은 [QUICK_START.md](QUICK_START.md)를 참고하세요.
- **프론트엔드 개발자**: 
  - REST API 명세서: [API_DOCUMENTATION.md](API_DOCUMENTATION.md)
  - 데이터베이스 스키마: [DATABASE_SCHEMA.md](DATABASE_SCHEMA.md)

## 🏗️ 시스템 구조

```
사용자 설문조사 (15문항)
    ↓
투자 성향 분류 (4가지 유형)
    ↓
전략 및 리스크 설정 자동 적용
    ↓
┌─────────────────────────────────────┐
│  거래 사이클 (전략별 주기)          │
│  - DCA: 4시간마다                   │
│  - 추세 추종: 4시간마다             │
│  - 스윙: 1시간마다                  │
│  - 변동성 돌파: 15분마다            │
└─────────────────────────────────────┘
    ↓
시장 데이터 수집 (전략별 시간봉)
    ↓
LLM 분석 (기술적/심리 분석)
    ↓
전략 실행기 (신호 생성)
    ↓
리스크 검증 (레버리지 포함)
    ↓
주문 실행 (레버리지 적용)
    ↓
┌─────────────────────────────────────┐
│  실시간 손익 모니터링 (1분마다)     │
│  - 손절 기준 확인                   │
│  - 익절 기준 확인                   │
│  - 자동 대응 실행                   │
└─────────────────────────────────────┘
```

## 🎯 투자 성향별 전략 및 설정

| 성향 | 전략 | 실행 주기 | 레버리지 | 진입 비중 | 손절 | 익절 | 손절 후 | 익절 후 |
|------|------|----------|---------|----------|------|------|---------|---------|
| 안정 추구형 | DCA | 4시간 | 1배 (현물) | 5% | -15% | +10% | 재진입 대기 | 관망 |
| 위험 중립형 | 추세 추종 | 4시간 | 1배 (현물) | 20% | -7% | +15% | 관망 | 관망 |
| 적극 투자형 | 스윙 트레이딩 | 1시간 | 3배 | 30% | -5% | +20% | 관망 | 관망 |
| 전문 투기형 | 변동성 돌파 | 15분 | 10배 | 50% | -3% | +30% | 빠른 재진입 | 빠른 재진입 |

### 레버리지 설명

- **안정 추구형/위험 중립형**: 레버리지 없이 현물 거래만 사용
- **적극 투자형**: 최대 3배 레버리지 (예: $3,000 투자 → $9,000 포지션)
- **전문 투기형**: 최대 10배 레버리지 (예: $5,000 투자 → $50,000 포지션)

⚠️ **주의**: 레버리지는 손익을 배수로 확대합니다. 높은 레버리지는 높은 수익과 함께 높은 손실 위험을 수반합니다.

## ⚙️ 주요 설정

`.env` 파일에서 다음 설정을 조정할 수 있습니다:

### 필수 설정
- `GEMINI_API_KEY`: Google Gemini API 키
- `BINANCE_API_KEY`: Binance API 키
- `BINANCE_SECRET_KEY`: Binance Secret 키
- `BINANCE_USE_TESTNET`: `true` (테스트넷) / `false` (실거래)
- `TRADING_MODE`: `SIMULATION` (시뮬레이션) / `LIVE` (실거래)

### 선택 설정
- `MAX_INVESTMENT_PERCENT`: 1회 최대 투자 비율 (기본: 10%)
- `MAX_TOTAL_INVESTMENT_PERCENT`: 총 투자 비율 제한 (기본: 50%)
- `STOP_LOSS_PERCENT`: 전역 손절 기준 (투자 성향별 설정이 우선)
- `MIN_CONFIDENCE_THRESHOLD`: 최소 신뢰도 기준 (기본: 0.70)
- `LOG_LEVEL`: 로그 레벨 (DEBUG, INFO, WARN, ERROR)
- `DB_TYPE`: 데이터베이스 타입 (`mysql` 또는 `sqlite`, 기본: `sqlite`)
- `MYSQL_HOST`: MySQL 호스트 (DB_TYPE=mysql일 때)
- `MYSQL_PORT`: MySQL 포트 (기본: 3306)
- `MYSQL_DATABASE`: MySQL 데이터베이스 이름
- `MYSQL_USERNAME`: MySQL 사용자명
- `MYSQL_PASSWORD`: MySQL 비밀번호
- `CRYPTOPANIC_API_KEY`: CryptoPanic API 키 (선택, 없으면 Google News 사용)
- `ENCRYPTION_KEY`: API 키 암호화용 키 (선택, 없으면 기본 키 사용)

## 🔒 보안 주의사항

⚠️ **중요**: 
- `.env` 파일은 절대 공개 저장소에 업로드하지 마세요
- `.gitignore`에 `.env`가 포함되어 있는지 확인하세요
- 실거래 전 반드시 시뮬레이션 모드로 충분히 테스트하세요
- Testnet 키를 사용하여 연습하세요
- API 키는 정기적으로 갱신하세요

## 📁 프로젝트 구조

```
project2/
├── src/main/java/com/bitbot/
│   ├── agents/              # LLM 에이전트
│   │   ├── TechnicalAnalystAgent.java      # 기술적 분석
│   │   ├── SentimentAnalystAgent.java      # 시장 심리 분석
│   │   ├── MasterCoordinatorAgent.java     # 종합 결정
│   │   └── RiskManagementAgent.java        # 리스크 관리
│   ├── classification/      # 투자 성향 분류
│   │   └── InvestorTypeClassifier.java
│   ├── strategy/            # 거래 전략
│   │   └── StrategyExecutor.java
│   ├── database/            # SQLite 연동
│   │   ├── DatabaseConnection.java
│   │   ├── TradeRepository.java
│   │   └── UserProfileRepository.java
│   ├── data/                # 데이터 수집
│   │   └── BinanceDataCollector.java
│   ├── trading/             # 주문 실행
│   │   └── OrderExecutor.java
│   ├── models/              # 데이터 모델
│   ├── indicators/          # 기술 지표
│   ├── TradingEngine.java   # 거래 엔진
│   ├── AutoTradingService.java  # 자동 거래 서비스
│   ├── LossMonitor.java     # 손익 모니터링
│   ├── CLITester.java       # CLI 테스트
│   ├── IntegrationTest.java # 통합 테스트
│   └── ...
├── src/main/resources/
│   ├── db/schema.sql        # 데이터베이스 스키마
│   └── logback.xml          # 로깅 설정
├── data/                    # SQLite 데이터베이스
│   └── bitbot.db
├── logs/                    # 로그 파일
├── pom.xml                  # Maven 설정
├── .env                     # 환경 변수 (직접 생성)
├── run-cli.bat                    # CLI 테스트 실행
├── run-auto.bat                   # 자동 거래 시작
├── run-integration-test.bat       # 통합 테스트
├── run-server.bat                 # Spring Boot 서버 실행
├── run-mysql-test.bat             # MySQL 연결 테스트
├── run-news-sentiment-test.bat    # 뉴스/감정 테스트
├── reset-profile.bat              # 프로필 리셋
└── setup-java-home.ps1            # JAVA_HOME 설정 스크립트
```

## 🛠️ 기술 스택

- **언어**: Java 17
- **LLM**: Google Gemini 2.5 Flash
- **거래소**: Binance API (Testnet/실거래)
- **데이터베이스**: MySQL (원격 서버) / SQLite (로컬 파일)
- **서버 프레임워크**: Spring Boot 3.x
- **빌드 도구**: Maven
- **로깅**: SLF4J + Logback
- **JSON 처리**: Jackson
- **연결 풀**: HikariCP
- **보안**: BCrypt (비밀번호), AES-256-GCM (API 키 암호화)
- **테스트**: JUnit 5

## 📊 주요 기능 상세

### 1. 투자 성향 분류
- 15문항 설문조사 (재무 상태, 지식/경험, 위험 감수성, 거래 스타일, 시장 관점)
- 점수 기반 자동 분류 (12~48점)
- 4가지 투자 유형: 안정 추구형, 위험 중립형, 적극 투자형, 전문 투기형

### 2. LLM 에이전트 시스템
- **Technical Analyst**: 기술적 지표 분석 (RSI, MACD, 볼린저 밴드 등)
- **Sentiment Analyst**: 시장 심리 및 뉴스 분석
- **Master Coordinator**: 종합 분석 및 최종 결정

### 3. 전략별 거래
- **DCA (Dollar Cost Averaging)**: 정기적 분할 매수
- **추세 추종**: 이동평균선 기반 추세 매매
- **스윙 트레이딩**: 중기 변동성 활용
- **변동성 돌파**: 변동성 확대 시점 포착

### 4. 레버리지 거래
- 투자 성향별 자동 레버리지 적용
- 포지션 크기 자동 계산
- 리스크 관리 통합

### 5. 실시간 손익 모니터링
- 1분마다 계좌 손익 확인
- 손절/익절 기준 자동 감지
- 손절/익절 후 자동 대응 (관망, 재진입, 반대 포지션 등)

### 6. 뉴스 및 시장 심리 데이터
- CryptoPanic API를 통한 암호화폐 뉴스 수집
- Google News RSS를 통한 뉴스 수집 (대체)
- Alternative.me API를 통한 공포/탐욕 지수 수집
- LLM 분석에 통합하여 시장 심리 반영

### 7. 모니터링 및 알림 시스템
- 손절/익절 실행 시 알림
- 거래 실행 시 알림
- 시스템 에러 알림
- 시스템 상태 모니터링 (5분마다)
- 알림 히스토리 관리

### 8. REST API 서버
- Spring Boot 기반 REST API
- 인증/인가 시스템 (BCrypt, AES-256)
- CORS 지원
- 프론트엔드 개발자용 API 문서 제공

### 9. 단위 테스트
- 66개 테스트 케이스
- 투자 성향 분류, 입력값 검증, Rate Limit, 암호화 등 테스트
- 모든 테스트 통과

## 🎮 사용 방법

### 첫 실행
1. `.env` 파일에 API 키 설정
2. `.\run-cli.bat` 실행
3. 설문조사 완료 (15문항)
4. 투자 성향 자동 분류 및 전략 적용

### 자동 거래 시작
```bash
.\run-auto.bat
```
- 전략별 주기로 자동 거래 실행
- 실시간 손익 모니터링 자동 시작

### 통합 테스트
```bash
.\run-integration-test.bat
```
- 여러 거래 사이클 실행
- 전체 수익률 추적
- 거래 통계 출력

### 프로필 리셋
```bash
.\reset-profile.bat
```
- 사용자 프로필 및 설문조사 데이터 삭제
- 새로운 설문조사 진행 가능

## ⚖️ 면책 조항

이 소프트웨어는 **교육 목적**으로 제작되었습니다.
실제 투자에 사용할 경우 발생하는 모든 손실에 대해 개발자는 책임지지 않습니다.
암호화폐 투자는 고위험 투자이며, 투자 손실이 발생할 수 있습니다.

---

**⚠️ 투자 주의사항**: 
- 이 시스템은 완벽하지 않으며, AI의 예측이 항상 정확하지는 않습니다.
- 절대로 감당할 수 없는 금액을 투자하지 마세요.
- 레버리지 거래는 손실 위험이 매우 높습니다.
- 실거래 전 반드시 시뮬레이션 모드로 충분히 테스트하세요.
