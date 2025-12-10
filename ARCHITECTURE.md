# BitBot 프로젝트 구조

## 📁 디렉토리 구조

```
bitbot-client/
├── src/main/java/com/bitbot/client/
│   ├── BitBotApplication.java          # 메인 애플리케이션
│   │
│   ├── ui/                              # UI 컴포넌트
│   │   ├── navigation/
│   │   │   └── NavigationBar.java      # 네비게이션 바
│   │   ├── dashboard/
│   │   │   └── DashboardView.java      # 대시보드 뷰
│   │   ├── chart/
│   │   │   └── CandleStickChart.java   # 커스텀 캔들차트
│   │   └── feed/
│   │       ├── AgentFeedView.java      # AI 피드 뷰
│   │       └── TradeDecisionCard.java  # 의사결정 카드
│   │
│   ├── service/                         # 비즈니스 로직
│   │   ├── MarketDataService.java      # 시장 데이터 관리
│   │   ├── AutoTradingEngine.java      # 자동 매매 엔진
│   │   ├── api/
│   │   │   ├── BinanceApiClient.java   # Binance API 클라이언트
│   │   │   └── ServerApiClient.java    # 서버 API 클라이언트
│   │   ├── ai/
│   │   │   └── GeminiApiClient.java    # Gemini AI 클라이언트
│   │   ├── analysis/
│   │   │   ├── TechnicalIndicators.java # 기술적 지표 계산
│   │   │   └── MarketAnalysisService.java
│   │   └── security/
│   │       ├── EncryptionService.java   # AES-256 암호화
│   │       └── CredentialStorage.java   # 자격증명 저장
│   │
│   ├── model/                           # 데이터 모델
│   │   ├── Candle.java                 # 캔들 데이터
│   │   ├── TradeDecision.java          # 매매 의사결정
│   │   ├── MarketAnalysis.java         # 시장 분석 결과
│   │   ├── ApiCredentials.java         # API 자격증명
│   │   └── UserProfile.java            # 사용자 프로필
│   │
│   └── dto/                             # 데이터 전송 객체
│       ├── ApiResponse.java
│       ├── BinanceKline.java
│       ├── BinanceTicker.java
│       └── UserProfileDto.java
│
├── src/main/resources/
│   └── logback.xml                      # 로깅 설정
│
├── docs/                                # 문서
│   ├── PRD.md                          # 제품 요구사항 명세
│   ├── API.md                          # API 문서
│   └── LLM_INPUT_DATA.md               # LLM 입력 데이터 명세
│
├── pom.xml                              # Maven 설정
├── README.md                            # 프로젝트 개요
└── SETUP.md                             # 설치 및 실행 가이드
```

## 🎯 핵심 기능

### ✅ Phase 1: GUI Foundation
- ✅ JavaFX 프로젝트 세팅 완료
- ✅ 3단 레이아웃 (Navigation | Dashboard | Agent Feed)
- ✅ Custom CandleStickChart 구현
- ✅ 실시간 차트 렌더링

### ✅ Phase 2: Data & Security
- ✅ Binance API 연동 (시세, 캔들 데이터)
- ✅ AES-256-GCM 암호화 구현
- ✅ PBKDF2 키 유도 함수
- ✅ 안전한 로컬 자격증명 저장

### ✅ Phase 3: AI Brain & Logic
- ✅ 기술적 지표 계산 (RSI, MACD, Bollinger Bands)
- ✅ Gemini API 연동
- ✅ 3-Way Decision Logic (BUY/SELL/HOLD)
- ✅ 자동 매매 엔진

### ✅ Phase 4: Backend Integration
- ✅ 서버 API 클라이언트
- ✅ 로그인 / 인증
- ✅ 프로필 관리
- ✅ 거래 로그 전송

### ✅ Phase 5: Final Polish
- ✅ Agent Feed UI 개선
- ✅ 확장 가능한 의사결정 카드
- ✅ 전체 시스템 통합

## 🚀 빠른 시작

### 1. 프로젝트 빌드
```bash
mvn clean install
```

### 2. 애플리케이션 실행
```bash
mvn javafx:run
```

### 3. 초기 설정
1. Binance API 키 설정 (Settings 메뉴)
2. Gemini API 키 설정
3. 투자 성향 설문조사 완료
4. Auto Trade 시작!

## 📊 아키텍처 하이라이트

### 비동기 처리
- `CompletableFuture`로 모든 API 호출 비차단 처리
- `Platform.runLater()`로 UI 업데이트

### 보안
- API 키는 **절대 서버에 전송 안 함**
- AES-256-GCM 암호화
- 사용자 비밀번호 기반 키 유도

### AI 통합
- Gemini 1.5 Flash 모델 사용
- 50개 캔들 + 기술적 지표를 컨텍스트로 제공
- JSON 형식으로 구조화된 의사결정 수신

### 3-Way Decision
- **BUY**: 상승 시그널 + 충분한 자금
- **SELL**: 목표가 도달 OR 손절가 도달
- **HOLD**: 불확실한 시장 (수수료 절감)

## 🔧 TODO (향후 개선사항)

1. **Settings 화면 구현**
   - API 키 입력 UI
   - 투자 성향 설문조사 UI
   - 자동매매 설정

2. **Portfolio 화면 구현**
   - 현재 보유 자산
   - 수익률 그래프
   - 미체결 주문

3. **Journal 화면 구현**
   - 과거 매매 이력
   - 통계 및 분석

4. **실거래 연동**
   - Binance Order API (실제 주문 실행)
   - 체결 결과 처리
   - 포지션 관리

5. **뉴스 통합**
   - CryptoPanic API 연동
   - 감정 분석
   - LLM 프롬프트에 포함

6. **테스트 코드 작성**
   - 단위 테스트 (JUnit)
   - 통합 테스트

## 📝 개발 가이드

### 코드 스타일
- Java 17 features 활용 (Records, Enhanced Switch, Text Blocks)
- 함수형 프로그래밍 패턴 (CompletableFuture, Streams)
- JavaFX 베스트 프랙티스

### 로깅
```java
logger.debug("디버그 메시지");
logger.info("정보 메시지");
logger.warn("경고 메시지");
logger.error("에러 메시지", exception);
```

### 에러 처리
- 모든 API 호출은 try-catch로 감싸기
- 사용자에게 친화적인 에러 메시지 표시
- 로그에 상세 스택 트레이스 기록

## 📚 참고 문서

- [PRD.md](docs/PRD.md) - 제품 요구사항 명세
- [API.md](docs/API.md) - 서버 API 문서
- [LLM_INPUT_DATA.md](docs/LLM_INPUT_DATA.md) - LLM 통합 가이드
- [SETUP.md](SETUP.md) - 설치 및 실행 가이드

---

**Made with ❤️ by BitBot Team**


