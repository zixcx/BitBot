# 🤖 BitBot 백엔드 완전 분석 문서

> **작성일**: 2025-11-29  
> **버전**: 1.0.0  
> **목적**: 노션 문서화 및 백엔드 개발 전체 이해

---

## 📋 목차

1. [프로젝트 개요](#1-프로젝트-개요)
2. [시스템 아키텍처](#2-시스템-아키텍처)
3. [데이터베이스 구조](#3-데이터베이스-구조)
4. [핵심 모듈 상세 분석](#4-핵심-모듈-상세-분석)
5. [LLM 거래 로직 완전 분석](#5-llm-거래-로직-완전-분석)
6. [API 엔드포인트](#6-api-엔드포인트)
7. [주요 기능 흐름](#7-주요-기능-흐름)
8. [설정 및 환경 변수](#8-설정-및-환경-변수)
9. [보안 및 인증](#9-보안-및-인증)
10. [모니터링 및 알림](#10-모니터링-및-알림)
11. [기술 스택](#11-기술-스택)

---

## 1. 프로젝트 개요

### 1.1 프로젝트명
**BitBot - LLM 기반 자동 거래 시스템**

### 1.2 핵심 기능
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

### 1.3 기술 스택
- **언어**: Java 17
- **빌드 도구**: Maven 3.6+
- **프레임워크**: Spring Boot 3.2.0
- **데이터베이스**: MySQL 8.2 / SQLite 3.44
- **LLM**: Google Gemini 2.5 Flash
- **거래소 API**: Binance API (Spot + Futures)
- **연결 풀**: HikariCP 5.1.0
- **로깅**: Logback + SLF4J
- **JSON 처리**: Jackson 2.16.0
- **HTTP 클라이언트**: OkHttp 4.12.0

---

## 2. 시스템 아키텍처

### 2.1 전체 구조도

```
┌─────────────────────────────────────────────────────────────┐
│                    사용자 인터페이스 레이어                    │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   CLI        │  │  REST API    │  │  Auto Trading│      │
│  │  (테스트)    │  │  (Spring)    │  │  Service     │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                    비즈니스 로직 레이어                        │
│  ┌──────────────────────────────────────────────────────┐   │
│  │              TradingEngine (거래 엔진)                │   │
│  │  ┌──────────────┐  ┌──────────────┐                 │   │
│  │  │  Data        │  │  LLM Agents  │                 │   │
│  │  │  Collector   │  │  - Technical │                 │   │
│  │  │              │  │  - Sentiment │                 │   │
│  │  │              │  │  - Master    │                 │   │
│  │  └──────────────┘  └──────────────┘                 │   │
│  │  ┌──────────────┐  ┌──────────────┐                 │   │
│  │  │  Strategy    │  │  Risk        │                 │   │
│  │  │  Executor    │  │  Management  │                 │   │
│  │  └──────────────┘  └──────────────┘                 │   │
│  │  ┌──────────────┐  ┌──────────────┐                 │   │
│  │  │  Order       │  │  Loss        │                 │   │
│  │  │  Executor    │  │  Monitor     │                 │   │
│  │  └──────────────┘  └──────────────┘                 │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                    데이터 레이어                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  Repository  │  │  Database    │  │  External    │      │
│  │  Layer       │  │  (MySQL/     │  │  APIs        │      │
│  │              │  │   SQLite)    │  │  - Binance   │      │
│  │              │  │              │  │  - Gemini    │      │
│  │              │  │              │  │  - News      │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 레이어별 역할

#### **프레젠테이션 레이어**
- **CLI**: 테스트 및 디버깅용 명령줄 인터페이스
- **REST API**: Spring Boot 기반 RESTful API 서버
- **Auto Trading Service**: 자동 거래 스케줄링 서비스

#### **비즈니스 로직 레이어**
- **TradingEngine**: 전체 거래 프로세스 조율
- **LLM Agents**: 기술적/심리 분석 에이전트
- **Strategy Executor**: 전략별 거래 신호 생성
- **Risk Management**: 리스크 검증 및 관리
- **Order Executor**: 주문 실행
- **Loss Monitor**: 실시간 손익 모니터링

#### **데이터 레이어**
- **Repository**: 데이터베이스 CRUD 작업
- **Database**: MySQL/SQLite 데이터 저장
- **External APIs**: Binance, Gemini, 뉴스 API 연동

---

## 3. 데이터베이스 구조

### 3.1 테이블 목록 (9개)

| 테이블명 | 설명 | 주요 컬럼 |
|---------|------|----------|
| `users` | 사용자 인증 정보 | id, email, username, password_hash, binance_api_key_encrypted |
| `user_profiles` | 투자 성향 및 전략 설정 | user_id, investor_type, risk_settings (JSON), trading_strategy |
| `questionnaires` | 설문조사 응답 | user_id, answers (JSON), total_score, result_type |
| `trades` | 거래 내역 | user_id, symbol, order_type, quantity, price, leverage, profit_loss |
| `trade_logs` | AI 판단 기록 (HOLD 포함) | user_id, action_type, confidence_score, brief_reason, full_reason |
| `llm_analysis_logs` | LLM 분석 상세 로그 | user_id, agent_name, request_prompt, response_raw, tokens_used |
| `portfolio_snapshots` | 포트폴리오 상태 스냅샷 | user_id, total_balance, btc_holding, total_profit_loss |
| `market_data_cache` | 시장 데이터 캐시 | symbol, timestamp, open, high, low, close, volume, rsi, macd |
| `system_events` | 시스템 이벤트 로그 | event_type, event_message, event_details, user_id |

### 3.2 주요 관계

```
users (1) ──┬── (1) user_profiles
            │
            ├── (N) questionnaires
            │
            ├── (N) trades
            │
            ├── (N) trade_logs
            │
            ├── (N) llm_analysis_logs
            │
            └── (N) portfolio_snapshots

trades (1) ── (N) llm_analysis_logs
```

### 3.3 데이터베이스 연결

**MySQL (원격 서버)**
```java
// DatabaseConnection.java
String jdbcUrl = "jdbc:mysql://203.234.62.223:3306/bitbot?characterEncoding=UTF-8&useSSL=false";
String username = "root";
String password = "dsem1010!";
```

**SQLite (로컬 파일)**
```java
String jdbcUrl = "jdbc:sqlite:data/bitbot.db";
```

**연결 풀 설정 (HikariCP)**
- MySQL: `maximumPoolSize=10`, `connectionTimeout=30000ms`
- SQLite: `maximumPoolSize=3`, `connectionTimeout=10000ms`

---

## 4. 핵심 모듈 상세 분석

### 4.1 TradingEngine (거래 엔진)

**역할**: 전체 거래 프로세스를 조율하는 핵심 엔진

**주요 메서드**:
- `runOneCycle()`: 1회 거래 사이클 실행
- `executeEmergencyStopLoss()`: 긴급 손절 실행
- `executeEmergencyTakeProfit()`: 긴급 익절 실행

**거래 사이클 흐름**:
```
1. 사용자 프로필 조회
2. 전략별 시간봉 결정
3. 시장 데이터 수집 (50개 캔들)
4. 기술 지표 계산 (RSI, MACD, 이동평균, 볼린저밴드)
5. LLM 에이전트 병렬 분석 (Technical + Sentiment)
6. 총괄 코디네이터 종합 결정
7. 전략별 로직 적용
8. 리스크 관리 검증
9. 주문 실행
10. 데이터베이스 저장
11. 거래 로그 저장 (HOLD 포함)
```

**동시성 제어**:
- `synchronized (cycleLock)`: 거래 사이클 동시 실행 방지
- `volatile boolean isCycleExecuting`: 실행 상태 플래그
- `ExecutorService llmExecutorService`: LLM 병렬 실행 (2개 스레드)

### 4.2 LLM 에이전트 시스템

#### 4.2.1 TechnicalAnalystAgent (기술적 분석 에이전트)

**역할**: 차트, 가격, 기술 지표를 분석하여 매매 의견 제시

**분석 데이터**:
- 현재 가격, 24시간 고가/저가, 거래량
- RSI(14), 단기/장기 이동평균, MACD, 볼린저밴드

**프롬프트 구조**:
```
시스템 역할: "당신은 20년 경력의 월스트리트 퀀트 트레이더이자 차트 분석 전문가입니다."

작업:
- 주어진 비트코인 시장 데이터를 기술적 분석 관점에서 분석
- 다음 1시간 내 비트코인 가격 방향성 예측
- 'BUY', 'SELL', 'HOLD' 중 가장 유리한 포지션 추천

투자자 프로필 반영:
- 투자 성향 (CONSERVATIVE, MODERATE, AGGRESSIVE, SPECULATIVE)
- 레버리지 정보
- 손절/익절 기준
- 진입 비중

출력 형식: JSON
{
  "agent": "Technical Analyst",
  "decision": "BUY",
  "confidence": 0.85,
  "reason": "RSI 지수가 28로 과매도 구간에 진입..."
}
```

**투자 성향별 분석 스타일**:
- **CONSERVATIVE**: 원금 보호 최우선, 확실한 신호만 추천, 신뢰도 0.8 이상
- **MODERATE**: 안정적 수익 추구, 추세 확인 후 진입, 신뢰도 0.7 이상
- **AGGRESSIVE**: 변동성 활용, 볼린저밴드 상/하단 매매, 신뢰도 0.6 이상
- **SPECULATIVE**: 높은 수익 추구, 변동성 돌파, 신뢰도 0.5 이상

#### 4.2.2 SentimentAnalystAgent (시장 심리 분석 에이전트)

**역할**: 뉴스, 소셜 미디어, 공포-탐욕 지수를 분석

**수집 데이터**:
- **뉴스**: CryptoPanic API 또는 Google News RSS (최대 10개)
- **공포/탐욕 지수**: Alternative.me API (0-100, 정규화: -1.0 ~ 1.0)

**프롬프트 구조**:
```
시스템 역할: "당신은 글로벌 투자은행의 거시 경제 및 시장 심리 분석가입니다."

작업:
- 주어진 시장 상황을 바탕으로 현재 암호화폐 시장의 투자 심리 진단
- 이것이 단기 비트코인 가격에 미칠 영향을 예측

분석 고려사항:
1. 공포(Fear) vs 탐욕(Greed) - 현재 시장 심리 상태
2. 주요 뉴스의 긍정/부정 영향
3. 기관 투자자 vs 개인 투자자 동향
4. 거시경제 환경이 리스크 자산에 미치는 영향
5. 과거 유사한 상황에서의 시장 반응

출력 형식: JSON
{
  "agent": "Sentiment Analyst",
  "decision": "SELL",
  "confidence": 0.70,
  "reason": "최근 주요 거래소 보안 이슈로 시장 불안감이 커지며 공포 지수 상승..."
}
```

**투자 성향별 심리 분석 스타일**:
- **CONSERVATIVE**: 보수적 관점, 불안한 시장 심리는 매수 기회로 보지 않음
- **MODERATE**: 균형잡힌 관점, 심리와 기술적 분석 조화
- **AGGRESSIVE**: 공격적 관점, 시장 심리 극단화를 기회로 활용
- **SPECULATIVE**: 매우 공격적 관점, 심리 변동성을 수익 기회로 활용

#### 4.2.3 MasterCoordinatorAgent (총괄 코디네이터)

**역할**: 모든 전문 에이전트의 분석 결과를 종합하여 최종 예비 결정

**의사결정 원칙**:
1. 3개 에이전트의 의견이 2개 이상 일치하면 그 방향을 우선 고려
2. 의견이 첨예하게 대립하면 'HOLD'로 보수적 접근
3. 높은 신뢰도의 의견에 더 큰 가중치 부여
4. 시장 심리가 극단적(극단적 공포/탐욕)이면 변동성 우려하여 신중히 판단
5. 기술적 분석과 심리 분석이 상충하면, 단기 변동성이 크다고 판단

**프롬프트 구조**:
```
시스템 역할: "당신은 수조 원을 운용하는 헤지펀드의 최고 투자 책임자(CIO)입니다."

작업:
- 각 분야 전문가들의 상이한 보고서를 종합적으로 검토
- 가장 합리적인 단일 '예비 투자 결정(Preliminary Decision)'을 내리기

투자자 프로필 반영:
- 투자 성향별 의사결정 원칙 적용
- 레버리지 정보 고려
- 손절/익절 기준 고려

출력 형식: JSON
{
  "agent": "Master Coordinator",
  "preliminary_decision": "HOLD",
  "summary_reason": "기술적으로는 강력한 매수 신호가 있으나, 시장 심리를 악화시키는 외부 뉴스가 있어 잠재적 변동성이 매우 크다..."
}
```

**투자 성향별 의사결정 원칙**:
- **CONSERVATIVE**: 모든 에이전트가 일치할 때만 행동, 불확실하면 HOLD 우선, 신뢰도 0.8 이상
- **MODERATE**: 2개 이상 에이전트 일치 시 행동, 신뢰도 0.7 이상
- **AGGRESSIVE**: 1개 에이전트라도 강한 신호면 행동 고려, 신뢰도 0.6 이상
- **SPECULATIVE**: 약한 신호도 기회로 활용, 신뢰도 0.5 이상

### 4.3 StrategyExecutor (전략 실행기)

**역할**: 전략별 거래 신호를 능동적으로 생성하고 LLM 결정과 결합

**동작 방식**:
1. 전략이 먼저 신호 생성 (능동적)
2. LLM 결정과 결합
3. 전략 신호가 우선순위를 가짐 (전략 조건 만족 시)

**4가지 전략**:

#### 4.3.1 SPOT_DCA (현물 달러 코스트 평균법)
- **시간봉**: 1일봉
- **실행 주기**: 4시간마다
- **신호 생성 로직**:
  - RSI < 30: **STRONG_BUY** (신뢰도 0.9)
  - RSI 30-40: **BUY** (신뢰도 0.7)
  - RSI ≥ 40: **HOLD** (신뢰도 0.8)
- **포지션 크기**: 기본값의 50% (작은 단위 분할 매수)

#### 4.3.2 TREND_FOLLOWING (추세 추종)
- **시간봉**: 4시간봉
- **실행 주기**: 4시간마다
- **신호 생성 로직**:
  - 골든크로스 (단기 MA > 장기 MA) + MACD 양수: **STRONG_BUY** (신뢰도 0.9)
  - 데드크로스 또는 MACD 음수: **SELL** (신뢰도 0.8)
  - 추세 미확인: **HOLD** (신뢰도 0.7)
- **포지션 크기**: 기본값 유지

#### 4.3.3 SWING_TRADING (스윙 트레이딩)
- **시간봉**: 1시간봉
- **실행 주기**: 1시간마다
- **신호 생성 로직**:
  - 볼린저밴드 하단 터치 (3% 이내): **STRONG_BUY** (신뢰도 0.9)
  - 볼린저밴드 하단 근처 (5% 이내): **BUY** (신뢰도 0.8)
  - 볼린저밴드 상단 터치 (3% 이내): **STRONG_SELL** (신뢰도 0.9)
  - 볼린저밴드 상단 근처 (5% 이내): **SELL** (신뢰도 0.8)
  - 중간 구간: **HOLD** (신뢰도 0.7)
- **포지션 크기**: 기본값 유지

#### 4.3.4 VOLATILITY_BREAKOUT (변동성 돌파)
- **시간봉**: 15분봉
- **실행 주기**: 15분마다
- **신호 생성 로직**:
  - 상단 돌파 (전일 고가 + 변동폭의 50%): **STRONG_BUY** (신뢰도 0.95)
  - 하단 돌파 (전일 저가 - 변동폭의 50%): **STRONG_SELL** (신뢰도 0.95)
  - 돌파 없음: **HOLD** (신뢰도 0.6)
- **포지션 크기**: 기본값의 150% (최대 50%, 큰 단위 빠른 진입)

**전략 신호와 LLM 결정 결합 로직**:
```java
// 전략이 강한 신호를 생성한 경우 (STRONG_BUY, STRONG_SELL)
if (strategyDecision == STRONG_BUY || strategyDecision == STRONG_SELL) {
    return strategySignal;  // 전략 신호 우선
}

// 전략이 매수 신호를 생성한 경우
if (strategyDecision == BUY) {
    // LLM이 매도 권하면 전략 신호 우선 (전략 조건 만족)
    if (llmDecision == SELL || llmDecision == STRONG_SELL) {
        return strategySignal;  // 전략 매수 신호 우선
    }
    return strategySignal;  // 전략 신호 승인
}

// 전략이 HOLD 신호를 생성한 경우
// LLM 결정 따름
return llmDecision;
```

### 4.4 RiskManagementAgent (리스크 관리 에이전트)

**역할**: 하드코딩된 규칙을 적용하여 예비 거래 결정을 승인 또는 거부

**검증 항목**:
1. **최대 포지션 크기**: `maxPositionPercent` 초과 시 거부
2. **최대 손실 한도**: `maxLossPercent` 초과 시 거부
3. **레버리지 제한**: `maxLeverage` 초과 시 거부
4. **최소 신뢰도**: 신뢰도가 너무 낮으면 거부
5. **잔고 확인**: 주문 금액이 잔고를 초과하면 거부

**레버리지 고려 계산**:
```java
// 레버리지 적용 시 포지션 크기
double positionSize = orderAmount * leverage;

// 레버리지 적용 시 최대 손실
double maxLossWithLeverage = Math.abs(maxLossPercent) * leverage;

// 레버리지로 인한 손실 확대 위험 고려
if (leverage > 1 && currentLossPercent * leverage <= maxLossPercent) {
    // 레버리지로 손실이 확대될 수 있으므로 더 엄격한 검증
}
```

### 4.5 OrderExecutor (주문 실행기)

**역할**: Binance API를 통해 실제 매수/매도 주문 실행

**지원 모드**:
- **SIMULATION**: 실제 주문 없이 로깅만 수행
- **LIVE**: 실제 주문 실행

**레버리지 지원**:
- **현물 거래**: 레버리지 1배 (기본)
- **선물 거래**: 레버리지 3배~10배 (시뮬레이션만 지원)

**주문 실행 흐름**:
```
1. 거래 모드 확인 (SIMULATION / LIVE)
2. 레버리지 확인 (1배 = 현물, 3배 이상 = 선물)
3. 수량 검증 (ValidationUtil)
4. Rate Limit 확인 (RateLimiter)
5. Binance API 호출
6. 응답 파싱 및 TradeOrder 객체 생성
7. 주문 결과 반환
```

**레버리지 시뮬레이션**:
```java
// 레버리지 적용 시뮬레이션
double positionSize = orderAmount * leverage;  // 포지션 크기
double quantity = positionSize / currentPrice;  // 수량

// 손익 계산 (레버리지 적용)
double profitLoss = (currentPrice - entryPrice) * quantity * leverage;
```

### 4.6 LossMonitor (손익 모니터링)

**역할**: 실시간으로 손익률을 모니터링하고, 손절/익절 기준 도달 시 즉시 대응

**모니터링 간격**: 1분마다 (거래 주기와 독립적)

**모니터링 로직**:
```
1. 계좌 정보 조회
2. 사용자 프로필 조회 (리스크 설정)
3. 현재 손익률 계산
4. 익절 기준 도달 확인 (우선순위 1)
   - 도달 시: executeEmergencyTakeProfit() 호출
5. 손절 기준 도달 확인 (우선순위 2)
   - 도달 시: executeEmergencyStopLoss() 호출
6. 최대 손실 기준 근접 경고
7. 익절 기준 근접 경고 (80% 도달)
```

**손절/익절 실행**:
```java
// 손절 실행
TradeOrder order = tradingEngine.executeEmergencyStopLoss(btcQuantity, reason);

// 익절 실행
TradeOrder order = tradingEngine.executeEmergencyTakeProfit(btcQuantity, reason);
```

**손절/익절 후 대응 전략**:
- **HOLD**: 관망 모드, 다음 거래 사이클까지 대기
- **WAIT_REENTRY**: 재진입 대기, 더 좋은 진입 기회 모니터링
- **QUICK_REENTRY**: 빠른 재진입 모드, 즉시 재진입 기회 모니터링
- **REVERSE_POSITION**: 반대 포지션 검토, 추세 전환 가능성 모니터링

### 4.7 AutoTradingService (자동 거래 서비스)

**역할**: 거래 엔진을 주기적으로 실행하는 스케줄링 서비스

**전략별 실행 주기**:
- **SPOT_DCA**: 4시간마다
- **TREND_FOLLOWING**: 4시간마다
- **SWING_TRADING**: 1시간마다
- **VOLATILITY_BREAKOUT**: 15분마다

**주요 기능**:
- `start()`: 자동 거래 시작
- `stop()`: 자동 거래 중지
- `startLossMonitoring()`: 손익 모니터링 시작 (병렬 실행)

**스케줄링**:
```java
ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

// 거래 사이클 스케줄링
scheduler.scheduleAtFixedRate(
    () -> tradingEngine.runOneCycle(),
    initialDelay,
    intervalMinutes,
    TimeUnit.MINUTES
);

// 손익 모니터링 스케줄링 (병렬)
lossMonitor.start();  // 1분마다 체크
```

---

## 5. LLM 거래 로직 완전 분석

### 5.1 전체 거래 사이클 상세 흐름

```
┌─────────────────────────────────────────────────────────────┐
│                    거래 사이클 시작                           │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ [1단계] 사용자 프로필 조회                                    │
│ - UserProfileRepository.findByUserId()                      │
│ - 투자 성향, 전략, 리스크 설정 조회                          │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ [2단계] 전략별 시간봉 결정                                    │
│ - SPOT_DCA: 1일봉                                           │
│ - TREND_FOLLOWING: 4시간봉                                  │
│ - SWING_TRADING: 1시간봉                                    │
│ - VOLATILITY_BREAKOUT: 15분봉                               │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ [3단계] 시장 데이터 수집                                      │
│ - BinanceDataCollector.getKlines("BTCUSDT", timeframe, 50) │
│ - 50개 캔들 데이터 수집 (PRD 요구사항: 토큰 절약)            │
│ - MarketDataCache를 통한 캐싱 (5분 이내 데이터)              │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ [4단계] 기술 지표 계산                                        │
│ - TechnicalIndicators.calculateAllIndicators()              │
│ - RSI(14), MACD, 이동평균(20, 60), 볼린저밴드               │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ [5단계] LLM 에이전트 병렬 분석                                │
│ ┌────────────────────┐  ┌────────────────────┐             │
│ │ Technical Analyst  │  │ Sentiment Analyst  │             │
│ │ - 차트 분석        │  │ - 뉴스 분석        │             │
│ │ - 기술 지표 분석   │  │ - 공포/탐욕 지수   │             │
│ │ - 프로필 반영      │  │ - 프로필 반영      │             │
│ └────────────────────┘  └────────────────────┘             │
│         CompletableFuture (병렬 실행)                        │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ [6단계] 총괄 코디네이터 종합 결정                              │
│ - MasterCoordinatorAgent.coordinateDecision()               │
│ - 각 에이전트의 분석 결과 종합                               │
│ - 투자 성향별 의사결정 원칙 적용                             │
│ - 최종 예비 결정 생성 (BUY/SELL/HOLD)                        │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ [7단계] 전략별 로직 적용                                      │
│ - StrategyExecutor.applyStrategy()                          │
│ - 전략이 능동적으로 신호 생성                                │
│ - 전략 신호와 LLM 결정 결합                                  │
│ - 전략 신호가 우선순위를 가짐                                │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ [8단계] 리스크 관리 검증                                      │
│ - RiskManagementAgent.validateDecision()                    │
│ - 최대 포지션 크기 확인                                      │
│ - 최대 손실 한도 확인                                        │
│ - 레버리지 제한 확인                                         │
│ - 최소 신뢰도 확인                                           │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ [9단계] 주문 실행                                             │
│ - OrderExecutor.executeMarketOrder()                        │
│ - 레버리지 적용 (투자 성향별)                                │
│ - 주문 수량 계산 (포지션 크기 = 투자 금액 × 레버리지)        │
│ - Binance API 호출 (SIMULATION / LIVE)                      │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ [10단계] 데이터베이스 저장                                    │
│ - TradeRepository.save() (트랜잭션 관리)                     │
│ - TradeLogRepository.save() (모든 판단 기록, HOLD 포함)      │
│ - 시장 스냅샷 저장 (JSON 형식)                               │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ [11단계] 알림 전송                                            │
│ - NotificationService.notifyTradeExecution()                │
│ - 거래 실행 알림                                             │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                    거래 사이클 종료                           │
└─────────────────────────────────────────────────────────────┘
```

### 5.2 LLM 프롬프트 상세 구조

#### 5.2.1 TechnicalAnalystAgent 프롬프트

**시스템 역할**:
```
당신은 20년 경력의 월스트리트 퀀트 트레이더이자 차트 분석 전문가입니다.
```

**시장 데이터 요약**:
```
현재 가격: $50,000.00 (전일 대비 2.00%)
24시간 고가: $51,000.00
24시간 저가: $49,000.00
거래량: 1000.00 BTC

기술 지표:
- RSI(14): 35.00 (과매도)
- 단기 이동평균(20): $49,000.00
- 장기 이동평균(60): $48,500.00
- MACD: 100.00, Signal: 95.00
- 볼린저밴드 상단: $52,000.00, 하단: $48,000.00
```

**투자자 프로필 정보** (프로필이 있는 경우):
```
[투자자 프로필]
투자 성향: 적극 투자형
거래 전략: 스윙 트레이딩
레버리지: 3배
손절 기준: -5.0%
익절 기준: +20.0%
진입 비중: 30.0%

분석 시 주의사항:
- 변동성을 활용한 수익 추구
- 볼린저밴드 상/하단 매매 기회 포착
- 역추세 전략 고려
- 단기 스윙 기회 적극 활용
- 신뢰도 0.6 이상도 고려
- 레버리지 3배 사용: 손절 -5%를 염두에 두고 신중하게
- 스윙 트레이딩: 중기 변동성 활용, 빠른 진입/청산
- 손실 확대 위험이 있으므로 명확한 신호만 추천
```

**작업 지시**:
```
주어진 비트코인 시장 데이터를 기술적 분석 관점에서 분석하여, 
다음 1시간 내 비트코인 가격 방향성을 예측하고 'BUY', 'SELL', 'HOLD' 중 
가장 유리한 포지션을 추천하세요.

분석 시 고려사항:
1. RSI가 30 이하면 과매도(매수 시그널), 70 이상이면 과매수(매도 시그널)
2. 단기 이평선이 장기 이평선을 상향 돌파하면 골든크로스(매수)
3. MACD가 시그널선을 상향 돌파하면 매수 시그널
4. 볼린저밴드 하단 터치 후 반등은 매수, 상단 터치는 매도
5. 거래량 증가와 함께 가격 상승은 강한 신호

신뢰도는 0.0~1.0 사이로 표현하며, 근거를 명확히 제시하세요.
```

**출력 형식**:
```json
{
  "agent": "Technical Analyst",
  "decision": "BUY",
  "confidence": 0.85,
  "reason": "RSI 지수가 28로 과매도 구간에 진입했으며, 볼린저밴드 하단을 터치 후 반등 시그널이 포착됨. 거래량도 증가 추세."
}
```

#### 5.2.2 SentimentAnalystAgent 프롬프트

**시스템 역할**:
```
당신은 글로벌 투자은행의 거시 경제 및 시장 심리 분석가입니다.
```

**시장 심리 데이터**:
```
현재 비트코인 가격: $50,000.00

[최근 주요 뉴스]
- [2025-11-29] 비트코인 ETF 승인 가능성 높아짐 (감정 점수: 0.75, 출처: CoinDesk)
- [2025-11-29] 주요 거래소 보안 강화 발표 (감정 점수: 0.60, 출처: Binance)
- [2025-11-28] 기관 투자자 대규모 매수 (감정 점수: 0.80, 출처: Bloomberg)

[공포/탐욕 지수]
현재 지수: 45/100 (공포)
정규화 값: -0.10 (-1.0=극도의 공포, 0=중립, 1.0=극도의 탐욕)
```

**투자자 프로필 정보** (프로필이 있는 경우):
```
[투자자 프로필]
투자 성향: 적극 투자형
거래 전략: 스윙 트레이딩
레버리지: 3배
손절 기준: -5.0%
익절 기준: +20.0%

분석 스타일:
- 공격적 관점: 시장 심리 극단화를 기회로 활용
- 공포 지수 높을 때 역매매 기회 포착
- 신뢰도 0.6 이상도 고려
- 레버리지 3배 사용: 손절 -5%를 염두에 두고 신중하게
- 스윙 트레이딩: 심리 변동성을 중기 수익 기회로 활용
- 레버리지로 손실 확대 위험이 있으므로, 극단적 심리 시 주의
```

**작업 지시**:
```
주어진 시장 상황을 바탕으로 현재 암호화폐 시장의 투자 심리를 진단하고,
이것이 단기 비트코인 가격에 미칠 영향을 예측하세요.

분석 시 고려사항:
1. 공포(Fear) vs 탐욕(Greed) - 현재 시장 심리 상태
2. 주요 뉴스의 긍정/부정 영향
3. 기관 투자자 vs 개인 투자자 동향
4. 거시경제 환경이 리스크 자산에 미치는 영향
5. 과거 유사한 상황에서의 시장 반응

'BUY', 'SELL', 'HOLD' 중 추천 포지션과 신뢰도(0.0~1.0), 근거를 제시하세요.
```

**출력 형식**:
```json
{
  "agent": "Sentiment Analyst",
  "decision": "SELL",
  "confidence": 0.70,
  "reason": "최근 주요 거래소 보안 이슈로 시장 불안감이 커지며 공포 지수 상승. 단기적으로 투매 가능성 높음."
}
```

#### 5.2.3 MasterCoordinatorAgent 프롬프트

**시스템 역할**:
```
당신은 수조 원을 운용하는 헤지펀드의 최고 투자 책임자(CIO)입니다.
```

**에이전트 보고서**:
```json
[
  {
    "agent": "Technical Analyst",
    "decision": "BUY",
    "confidence": 0.85,
    "reason": "RSI 지수가 28로 과매도 구간에 진입했으며, 볼린저밴드 하단을 터치 후 반등 시그널이 포착됨."
  },
  {
    "agent": "Sentiment Analyst",
    "decision": "SELL",
    "confidence": 0.70,
    "reason": "최근 주요 거래소 보안 이슈로 시장 불안감이 커지며 공포 지수 상승."
  }
]
```

**투자자 프로필 정보** (프로필이 있는 경우):
```
[투자자 프로필]
투자 성향: 적극 투자형
거래 전략: 스윙 트레이딩
레버리지: 3배
손절 기준: -5.0%
익절 기준: +20.0%
진입 비중: 30.0%

[공격적 의사결정]
- 1개 에이전트라도 강한 신호면 행동 고려
- 신뢰도 0.6 이상도 고려
- 변동성 기회 적극 활용
- 레버리지 3배 사용: 손절 -5%를 염두에 두고 매우 신중하게
- 스윙 트레이딩: 중기 변동성 활용, 빠른 진입/청산
- 레버리지로 손실이 3배 확대될 수 있으므로, 확실한 기회만 선택
- 손실 확대 위험을 항상 염두에 두고 결정하세요
```

**의사결정 원칙**:
```
의사결정 원칙:
1. 3개 에이전트의 의견이 2개 이상 일치하면 그 방향을 우선 고려
2. 의견이 첨예하게 대립하면 'HOLD'로 보수적 접근
3. 높은 신뢰도의 의견에 더 큰 가중치 부여
4. 시장 심리가 극단적(극단적 공포/탐욕)이면 변동성 우려하여 신중히 판단
5. 기술적 분석과 심리 분석이 상충하면, 단기 변동성이 크다고 판단
```

**작업 지시**:
```
각 분야 전문가들의 상이한 보고서를 종합적으로 검토하여,
가장 합리적인 단일 '예비 투자 결정(Preliminary Decision)'을 내리세요.

결정의 배경이 된 핵심 요약 근거를 2~3문장으로 명확히 정리하세요.
```

**출력 형식**:
```json
{
  "agent": "Master Coordinator",
  "preliminary_decision": "HOLD",
  "summary_reason": "기술적으로는 강력한 매수 신호가 있으나, 시장 심리를 악화시키는 외부 뉴스가 있어 잠재적 변동성이 매우 크다. 따라서 신규 진입보다는 현재 상황을 관망하는 것이 유리하다."
}
```

### 5.3 LLM 호출 최적화

**병렬 실행**:
```java
// CompletableFuture를 사용한 병렬 실행
CompletableFuture<TradingDecision> techFuture = CompletableFuture.supplyAsync(
    () -> technicalAnalyst.analyze(marketData, userProfile),
    llmExecutorService
);

CompletableFuture<TradingDecision> sentimentFuture = CompletableFuture.supplyAsync(
    () -> sentimentAnalyst.analyze(latest.getClose(), userProfile),
    llmExecutorService
);

// 모든 분석 완료 대기
CompletableFuture.allOf(techFuture, sentimentFuture).join();

// 결과 수집
List<TradingDecision> agentReports = new ArrayList<>();
agentReports.add(techFuture.join());
agentReports.add(sentimentFuture.join());
```

**재시도 로직**:
```java
// RetryUtil을 통한 재시도 (지수 백오프)
String response = RetryUtil.retryIfRetryable(
    () -> geminiClient.callGemini(prompt),
    3,  // 최대 3회 재시도
    1000,  // 초기 지연 1초
    2.0  // 지수 백오프 배수
);
```

**토큰 절약**:
- 캔들 데이터: 200개 → 50개로 감소 (PRD 요구사항)
- MarketDataCache: 5분 이내 데이터는 캐시에서 조회
- 프롬프트 최적화: 불필요한 정보 제거

### 5.4 투자 성향별 LLM 프롬프트 차별화

#### CONSERVATIVE (안정 추구형)
- **분석 스타일**: 원금 보호 최우선, 확실한 신호만 추천
- **신뢰도 기준**: 0.8 이상
- **의사결정**: 모든 에이전트가 일치할 때만 행동
- **레버리지**: 1배 (현물만)
- **손절/익절**: -15% / +10%

#### MODERATE (위험 중립형)
- **분석 스타일**: 안정적 수익 추구, 추세 확인 후 진입
- **신뢰도 기준**: 0.7 이상
- **의사결정**: 2개 이상 에이전트 일치 시 행동
- **레버리지**: 1배 (현물만)
- **손절/익절**: -7% / +15%

#### AGGRESSIVE (적극 투자형)
- **분석 스타일**: 변동성 활용, 볼린저밴드 상/하단 매매
- **신뢰도 기준**: 0.6 이상
- **의사결정**: 1개 에이전트라도 강한 신호면 행동 고려
- **레버리지**: 3배
- **손절/익절**: -5% / +20%

#### SPECULATIVE (전문 투기형)
- **분석 스타일**: 높은 수익 추구, 변동성 돌파
- **신뢰도 기준**: 0.5 이상
- **의사결정**: 약한 신호도 기회로 활용
- **레버리지**: 10배
- **손절/익절**: -3% / +30%

---

## 6. API 엔드포인트

### 6.1 인증 API

| 메서드 | 엔드포인트 | 설명 |
|--------|-----------|------|
| POST | `/api/auth/register` | 회원가입 |
| POST | `/api/auth/login` | 로그인 |
| POST | `/api/auth/logout` | 로그아웃 |
| GET | `/api/auth/verify` | 세션 확인 |

### 6.2 거래 API

| 메서드 | 엔드포인트 | 설명 |
|--------|-----------|------|
| GET | `/api/trades` | 거래 내역 조회 |
| GET | `/api/account` | 계좌 정보 조회 |
| GET | `/api/profile` | 사용자 프로필 조회 |
| GET | `/api/trade-logs` | 거래 로그 조회 (HOLD 포함) |
| GET | `/api/health` | 시스템 상태 확인 |

### 6.3 설문조사 API

| 메서드 | 엔드포인트 | 설명 |
|--------|-----------|------|
| POST | `/api/questionnaire/submit` | 설문조사 제출 |
| GET | `/api/questionnaire/latest` | 최신 설문조사 조회 |

### 6.4 자동 거래 제어 API

| 메서드 | 엔드포인트 | 설명 |
|--------|-----------|------|
| POST | `/api/trading/start` | 자동 거래 시작 |
| POST | `/api/trading/stop` | 자동 거래 중지 |
| GET | `/api/trading/status` | 자동 거래 상태 조회 |

### 6.5 시장 데이터 API

| 메서드 | 엔드포인트 | 설명 |
|--------|-----------|------|
| GET | `/api/market/chart` | 차트 데이터 조회 (OHLCV) |
| GET | `/api/market/price` | 현재 가격 조회 |
| GET | `/api/market/24h-stats` | 24시간 통계 조회 |
| GET | `/api/market/news` | 최근 뉴스 조회 |
| GET | `/api/market/fear-greed` | 공포/탐욕 지수 조회 |

### 6.6 통계 API

| 메서드 | 엔드포인트 | 설명 |
|--------|-----------|------|
| GET | `/api/statistics/trades` | 거래 통계 조회 |

### 6.7 알림 API

| 메서드 | 엔드포인트 | 설명 |
|--------|-----------|------|
| GET | `/api/notifications` | 최근 알림 조회 |

**총 20개 엔드포인트**

---

## 7. 주요 기능 흐름

### 7.1 사용자 등록 및 설문조사 흐름

```
1. 사용자 등록 (POST /api/auth/register)
   - 이메일, 사용자명, 비밀번호 입력
   - 비밀번호 BCrypt 해싱
   - users 테이블에 저장

2. 설문조사 제출 (POST /api/questionnaire/submit)
   - 15문항 답변 제출
   - 점수 계산 (12-48점)
   - 투자 성향 분류 (InvestorTypeClassifier)
   - user_profiles 테이블에 저장

3. 프로필 생성
   - 투자 성향별 리스크 설정 자동 적용
   - 거래 전략 자동 할당
   - 레버리지 설정 자동 적용
```

### 7.2 자동 거래 시작 흐름

```
1. 자동 거래 시작 (POST /api/trading/start)
   - 사용자 프로필 조회
   - 전략별 실행 주기 결정
   - AutoTradingService.start() 호출

2. 거래 사이클 스케줄링
   - ScheduledExecutorService로 주기적 실행
   - 전략별 주기: 15분~4시간

3. 손익 모니터링 시작
   - LossMonitor.start() 호출
   - 1분마다 손익 체크 (병렬 실행)

4. 거래 사이클 실행
   - TradingEngine.runOneCycle() 호출
   - LLM 분석 → 전략 적용 → 리스크 검증 → 주문 실행
```

### 7.3 손절/익절 실행 흐름

```
1. 손익 모니터링 (1분마다)
   - LossMonitor.checkProfitLoss()
   - 현재 손익률 계산

2. 손절 기준 도달 확인
   - currentProfitLossPercent <= stopLossPercent
   - executeEmergencyStopLoss() 호출

3. 긴급 손절 실행
   - TradingEngine.executeEmergencyStopLoss()
   - 전체 포지션 청산 (MARKET_SELL)
   - 주문 실행 및 데이터베이스 저장

4. 손절 알림 전송
   - NotificationService.notifyStopLoss()
   - 알림 히스토리에 저장

5. 손절 후 대응 전략 실행
   - postStopLossAction 확인
   - HOLD / WAIT_REENTRY / QUICK_REENTRY / REVERSE_POSITION
```

---

## 8. 설정 및 환경 변수

### 8.1 필수 환경 변수 (.env)

```env
# Gemini API
GEMINI_API_KEY=your_gemini_api_key

# Binance API
BINANCE_API_KEY=your_binance_api_key
BINANCE_SECRET_KEY=your_binance_secret_key
BINANCE_USE_TESTNET=true

# 거래 모드
TRADING_MODE=SIMULATION  # SIMULATION 또는 LIVE

# 데이터베이스
DB_TYPE=mysql  # mysql 또는 sqlite
MYSQL_HOST=203.234.62.223
MYSQL_PORT=3306
MYSQL_DATABASE=bitbot
MYSQL_USERNAME=root
MYSQL_PASSWORD=dsem1010!

# 암호화 (선택적)
ENCRYPTION_KEY=your_32_byte_encryption_key

# 뉴스 API (선택적)
CRYPTOPANIC_API_KEY=your_cryptopanic_api_key
```

### 8.2 선택적 환경 변수

```env
# 최대 투자 비율
MAX_INVESTMENT_PERCENT=10.0

# 최대 총 투자 비율
MAX_TOTAL_INVESTMENT_PERCENT=50.0

# 전역 손절 기준 (투자 성향별 설정이 우선)
STOP_LOSS_PERCENT=-10.0

# 최소 신뢰도 기준
MIN_CONFIDENCE_THRESHOLD=0.70
```

---

## 9. 보안 및 인증

### 9.1 비밀번호 해싱

**BCrypt 사용**:
```java
// PasswordUtil.java
String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(12));
boolean isValid = BCrypt.checkpw(password, hashedPassword);
```

**강도**: 12 rounds (약 250ms 소요)

### 9.2 API 키 암호화

**AES-256-GCM 사용**:
```java
// EncryptionUtil.java
String encrypted = EncryptionUtil.encrypt(apiKey, encryptionKey);
String decrypted = EncryptionUtil.decrypt(encrypted, encryptionKey);
```

**키 길이**: 32바이트 (256비트)

### 9.3 세션 관리

**인메모리 세션 토큰**:
```java
// AuthService.java
String sessionToken = UUID.randomUUID().toString();
sessionStore.put(sessionToken, userId);
```

**세션 만료**: 24시간 (향후 구현)

### 9.4 입력값 검증

**ValidationUtil 사용**:
```java
// 이메일 검증
ValidationUtil.validateEmail(email);

// 비밀번호 강도 검증
ValidationUtil.validatePasswordStrength(password);

// 수량 검증
ValidationUtil.validateQuantity(quantity);
```

---

## 10. 모니터링 및 알림

### 10.1 NotificationService

**역할**: 시스템 알림 중앙 관리

**알림 타입**:
- `notifyStopLoss()`: 손절 실행 알림
- `notifyTakeProfit()`: 익절 실행 알림
- `notifyTradeExecution()`: 거래 실행 알림
- `notifyError()`: 오류 알림
- `notifyWarning()`: 경고 알림
- `notifySystemStatus()`: 시스템 상태 알림

**알림 히스토리**: 최근 100개 저장 (메모리)

### 10.2 SystemMonitor

**역할**: 시스템 상태 모니터링

**모니터링 항목**:
- 데이터베이스 연결 상태
- Binance API 연결 상태
- Rate Limiter 사용률 (80% 이상 시 경고)

**모니터링 주기**: 5분마다

### 10.3 Rate Limiter

**역할**: API 호출 제한 관리

**토큰 버킷 알고리즘**:
- Binance API: 1200 요청/분
- Gemini API: 60 요청/분

**사용률 모니터링**: 80% 이상 시 경고

---

## 11. 기술 스택

### 11.1 핵심 라이브러리

| 라이브러리 | 버전 | 용도 |
|-----------|------|------|
| Spring Boot | 3.2.0 | REST API 서버 |
| Binance Connector | 3.2.0 | Binance API 연동 |
| Google Cloud AI | 3.35.0 | Gemini API 연동 |
| OkHttp | 4.12.0 | HTTP 클라이언트 |
| Jackson | 2.16.0 | JSON 처리 |
| HikariCP | 5.1.0 | 연결 풀 |
| Logback | 1.4.14 | 로깅 |
| Spring Security Crypto | 6.2.1 | BCrypt 해싱 |

### 11.2 데이터베이스 드라이버

| 드라이버 | 버전 | 용도 |
|---------|------|------|
| SQLite JDBC | 3.44.1.0 | SQLite 연결 |
| MySQL Connector | 8.2.0 | MySQL 연결 |

### 11.3 테스트 프레임워크

| 프레임워크 | 버전 | 용도 |
|-----------|------|------|
| JUnit Jupiter | 5.10.1 | 단위 테스트 |

---

## 📝 결론

이 문서는 BitBot 백엔드 시스템의 완전한 분석을 제공합니다. 주요 특징:

1. **LLM 기반 의사결정**: Google Gemini 2.5 Flash를 활용한 다중 에이전트 시스템
2. **투자 성향별 맞춤화**: 4가지 투자 유형에 따른 차별화된 분석 및 거래 전략
3. **실시간 리스크 관리**: 1분마다 손익 모니터링 및 자동 손절/익절
4. **전략별 최적화**: 4가지 거래 전략에 따른 시간봉 및 실행 주기 차별화
5. **레버리지 지원**: 투자 성향별 자동 레버리지 적용 (최대 10배)
6. **REST API**: Spring Boot 기반 20개 엔드포인트 제공
7. **보안**: BCrypt 비밀번호 해싱, AES-256-GCM API 키 암호화
8. **모니터링**: 실시간 시스템 상태 모니터링 및 알림

**문서 버전**: 1.0.0  
**최종 업데이트**: 2025-11-29

