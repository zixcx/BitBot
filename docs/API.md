# 📚 BitBot REST API 문서

## 🌐 기본 정보

- **Base URL**: `http://localhost:8080/api`
- **Content-Type**: `application/json`
- **인증 방식**: Bearer Token (Authorization 헤더)

## 🔐 인증

### 세션 토큰 사용

모든 인증이 필요한 API 호출 시 `Authorization` 헤더에 Bearer 토큰을 포함해야 합니다:

```
Authorization: Bearer {sessionToken}

```

---

## 📋 API 엔드포인트 목록

### 총 22개 엔드포인트

> 인증 표시: 🔒 = 인증 필요, 🌐 = 공개 API (인증 불필요)
> 

### 1. 인증 API (4개)

| 메서드 | 엔드포인트 | 설명 | 인증 |
| --- | --- | --- | --- |
| POST | `/api/auth/register` | 회원가입 | 🌐 |
| POST | `/api/auth/login` | 로그인 | 🌐 |
| POST | `/api/auth/logout` | 로그아웃 | 🔒 |
| GET | `/api/auth/verify` | 세션 확인 | 🔒 |

### 2. 거래 API (6개)

| 메서드 | 엔드포인트 | 설명 | 인증 |
| --- | --- | --- | --- |
| GET | `/api` | API 정보 조회 | 🌐 |
| GET | `/api/trades` | 거래 내역 조회 | 🔒 |
| GET | `/api/account` | 계좌 정보 조회 | 🔒 |
| GET | `/api/profile` | 사용자 프로필 조회 | 🔒 |
| GET | `/api/trade-logs` | 거래 로그 조회 (HOLD 포함) | 🔒 |
| GET | `/api/health` | 시스템 상태 확인 | 🌐 |

### 3. 설문조사 API (3개)

| 메서드 | 엔드포인트 | 설명 | 인증 |
| --- | --- | --- | --- |
| GET | `/api/questionnaire/questions` | 설문조사 문항 목록 조회 | 🌐 |
| POST | `/api/questionnaire/submit` | 설문조사 제출 | 🔒 |
| GET | `/api/questionnaire/latest` | 최신 설문조사 조회 | 🔒 |

### 4. 자동 거래 제어 API (3개)

| 메서드 | 엔드포인트 | 설명 | 인증 |
| --- | --- | --- | --- |
| POST | `/api/trading/start` | 자동 거래 시작 | 🔒 |
| POST | `/api/trading/stop` | 자동 거래 중지 | 🔒 |
| GET | `/api/trading/status` | 자동 거래 상태 조회 | 🔒 |

### 5. 시장 데이터 API (5개)

| 메서드 | 엔드포인트 | 설명 | 인증 |
| --- | --- | --- | --- |
| GET | `/api/market/chart` | 차트 데이터 조회 (OHLCV) | 🌐 |
| GET | `/api/market/price` | 현재 가격 조회 | 🌐 |
| GET | `/api/market/24h-stats` | 24시간 통계 조회 | 🌐 |
| GET | `/api/market/news` | 최근 뉴스 조회 | 🌐 |
| GET | `/api/market/fear-greed` | 공포/탐욕 지수 조회 | 🌐 |

### 6. 통계 API (1개)

| 메서드 | 엔드포인트 | 설명 | 인증 |
| --- | --- | --- | --- |
| GET | `/api/statistics/trades` | 거래 통계 조회 | 🔒 |

### 7. 알림 API (1개)

| 메서드 | 엔드포인트 | 설명 | 인증 |
| --- | --- | --- | --- |
| GET | `/api/notifications` | 최근 알림 조회 | 🌐 |

**총 23개 엔드포인트** (🔒 인증 필요: 12개, 🌐 공개: 11개)

---

## 📋 API 엔드포인트 상세

### 1. 인증 API

### 1.1 회원가입

```
POST /api/auth/register

```

**Request Body:**

```json
{
  "email": "user@example.com",
  "username": "username",
  "password": "Password123!",
  "binanceApiKey": "your_binance_api_key",
  "binanceSecretKey": "your_binance_secret_key"
}

```

**Response (성공):**

```json
{
  "success": true,
  "message": "회원가입 성공",
  "data": {
    "userId": 1,
    "email": "user@example.com",
    "username": "username"
  }
}

```

**Response (실패):**

```json
{
  "success": false,
  "error": "회원가입 실패: 이메일이 이미 존재합니다."
}

```

---

### 1.2 로그인

```
POST /api/auth/login

```

**Request Body:**

```json
{
  "email": "user@example.com",
  "password": "Password123!"
}

```

**Response (성공):**

```json
{
  "success": true,
  "message": "로그인 성공",
  "data": {
    "sessionToken": "abc123def456...",
    "email": "user@example.com"
  }
}

```

**Response (실패):**

```json
{
  "success": false,
  "error": "로그인 실패: 이메일 또는 비밀번호가 올바르지 않습니다."
}

```

---

### 1.3 로그아웃

```
POST /api/auth/logout

```

**Headers:**

```
Authorization: Bearer {sessionToken}

```

**Response:**

```json
{
  "success": true,
  "message": "로그아웃 성공",
  "data": null
}

```

---

### 1.4 세션 검증

```
GET /api/auth/verify

```

**Headers:**

```
Authorization: Bearer {sessionToken}

```

**Response (유효한 세션):**

```json
{
  "success": true,
  "message": "성공",
  "data": {
    "valid": true,
    "userId": 1
  }
}

```

**Response (유효하지 않은 세션):**

```json
{
  "success": true,
  "message": "성공",
  "data": {
    "valid": false
  }
}

```

---

### 2. 거래 API

### 2.1 API 정보 조회

```
GET /api

```

**Response:**

```json
{
  "success": true,
  "message": "성공",
  "data": {
    "name": "BitBot Trading API",
    "version": "1.0.0",
    "description": "LLM 기반 자동 거래 시스템 REST API",
    "baseUrl": "/api",
    "endpoints": {
      "health": "GET /api/health",
      "trades": "GET /api/trades",
      "account": "GET /api/account",
      "profile": "GET /api/profile",
      "tradeLogs": "GET /api/trade-logs",
      "auth": "POST /api/auth/login, /api/auth/register",
      "questionnaire": "GET /api/questionnaire/questions, POST /api/questionnaire/submit, GET /api/questionnaire/latest",
      "trading": "POST /api/trading/start, POST /api/trading/stop, GET /api/trading/status",
      "market": "GET /api/market/chart, /api/market/price, /api/market/news, /api/market/fear-greed",
      "statistics": "GET /api/statistics/trades",
      "notifications": "GET /api/notifications"
    },
    "documentation": "See API_DOCUMENTATION.md for detailed API documentation"
  }
}

```

---

### 2.2 서버 상태 확인

```
GET /api/health

```

**Response:**

```json
{
  "success": true,
  "message": "성공",
  "data": {
    "status": "UP",
    "service": "BitBot Trading Server"
  }
}

```

---

### 2.3 거래 내역 조회

```
GET /api/trades?limit=50

```

**Headers:**

```
Authorization: Bearer {sessionToken}

```

**Query Parameters:**

- `limit` (optional): 조회할 거래 내역 개수 (기본값: 50)

**Response:**

```json
{
  "success": true,
  "message": "성공",
  "data": [
    {
      "id": 1,
      "symbol": "BTCUSDT",
      "type": "MARKET_BUY",
      "quantity": 0.001,
      "price": 50000.0,
      "executedPrice": 50000.0,
      "totalCost": 50.0,
      "status": "FILLED",
      "decision": "BUY",
      "reason": "기술적 분석 결과 매수 신호",
      "leverage": 1,
      "isFuturesTrade": false,
      "profitLoss": 0.0,
      "profitLossPercent": 0.0,
      "executedAt": "2025-11-29T12:00:00"
    }
  ]
}

```

---

### 2.4 계좌 정보 조회

```
GET /api/account

```

**Headers:**

```
Authorization: Bearer {sessionToken}

```

**Response:**

```json
{
  "success": true,
  "message": "성공",
  "data": {
    "totalBalance": 10000.0,
    "availableBalance": 10000.0,
    "investedAmount": 0.0,
    "btcHolding": 0.0,
    "btcValue": 0.0,
    "totalProfitLoss": 0.0,
    "profitLossPercent": 0.0,
    "totalTrades": 0,
    "winningTrades": 0,
    "losingTrades": 0,
    "winRate": 0.0,
    "investmentRatio": 0.0
  }
}

```

---

### 2.5 사용자 프로필 조회

```
GET /api/profile

```

**Headers:**

```
Authorization: Bearer {sessionToken}

```

**Response:**

```json
{
  "success": true,
  "message": null,
  "data": {
    "userId": 1,
    "investorType": "AGGRESSIVE",
    "totalScore": 35,
    "tradingStrategy": "SWING_TRADING",
    "riskSettings": {
      "leverageAllowed": true,
      "maxLeverage": 3,
      "maxLossPercent": -5.0,
      "maxPositionPercent": 30.0,
      "stopLossPercent": -5.0,
      "takeProfitPercent": 20.0,
      "postStopLossAction": "HOLD",
      "postTakeProfitAction": "HOLD"
    }
  }
}

```

**Response (프로필 없음):**

```json
{
  "success": false,
  "error": "프로필이 없습니다. 설문조사를 먼저 완료하세요."
}

```

---

### 2.6 거래 로그 조회 (AI 판단 기록)

```
GET /api/trade-logs?limit=50

```

**Headers:**

```
Authorization: Bearer {sessionToken}

```

**Query Parameters:**

- `limit` (optional): 조회할 로그 개수 (기본값: 50)

**Response:**

```json
{
  "success": true,
  "message": "성공",
  "data": [
    {
      "id": 1,
      "userId": 1,
      "symbol": "BTCUSDT",
      "actionType": "BUY",
      "confidenceScore": 0.85,
      "briefReason": "기술적 분석 결과 매수 신호",
      "fullReason": "RSI가 과매도 구간에서 반등...",
      "executedPrice": 50000.0,
      "executedQty": 0.001,
      "realizedPnl": null,
      "marketSnapshot": "{\\"price\\":50000.0,\\"volume\\":1000.0,\\"rsi\\":35.0,\\"macd\\":100.0}",
      "agentName": "MasterCoordinatorAgent",
      "createdAt": "2025-11-29T12:00:00"
    }
  ]
}

```

**참고:**

- `actionType`: "BUY", "SELL", "HOLD" 중 하나
- `marketSnapshot`: JSON 문자열 형식으로 저장됨

---

## 📊 데이터 모델

### TradeOrder (거래 주문)

```tsx
interface TradeOrder {
  id: number;
  symbol: string;              // "BTCUSDT"
  type: "MARKET_BUY" | "MARKET_SELL";
  quantity: number;            // BTC 수량
  price: number;               // 주문 가격
  executedPrice: number;       // 체결 가격
  totalCost: number;           // 총 비용
  status: "FILLED" | "PENDING" | "FAILED" | "REJECTED";
  decision: "BUY" | "SELL" | "HOLD" | "STRONG_BUY" | "STRONG_SELL";
  reason: string;              // 거래 사유
  leverage: number;            // 레버리지 배수
  isFuturesTrade: boolean;     // 선물 거래 여부
  profitLoss: number;          // 손익
  profitLossPercent: number;   // 손익률 (%)
  executedAt: string;          // ISO 8601 형식
}

```

### AccountInfo (계좌 정보)

```tsx
interface AccountInfo {
  totalBalance: number;        // 총 잔고 (USDT)
  availableBalance: number;    // 사용 가능 잔고 (USDT)
  investedAmount: number;      // 투자 중인 금액 (USDT)
  btcHolding: number;          // 보유 BTC 수량
  btcValue: number;            // 보유 BTC 가치 (USDT)
  totalProfitLoss: number;     // 총 손익 (USDT)
  profitLossPercent: number;   // 손익률 (%)
  totalTrades: number;         // 총 거래 횟수
  winningTrades: number;       // 수익 거래 수
  losingTrades: number;        // 손실 거래 수
  winRate: number;             // 승률 (%)
  investmentRatio: number;     // 투자 비율 (%)
}

```

### UserProfile (사용자 프로필)

```tsx
interface UserProfile {
  userId: number;
  investorType: "CONSERVATIVE" | "MODERATE" | "AGGRESSIVE" | "SPECULATIVE";
  totalScore: number;          // 설문 점수 (12-48)
  tradingStrategy: "SPOT_DCA" | "TREND_FOLLOWING" | "SWING_TRADING" | "VOLATILITY_BREAKOUT";
  riskSettings: RiskSettings;
}

interface RiskSettings {
  leverageAllowed: boolean;
  maxLeverage: number;
  maxLossPercent: number;      // 최대 손실 (%)
  maxPositionPercent: number;  // 최대 포지션 비율 (%)
  stopLossPercent: number;     // 손절 기준 (%)
  takeProfitPercent: number;   // 익절 기준 (%)
  postStopLossAction: "HOLD" | "WAIT_REENTRY" | "QUICK_REENTRY" | "REVERSE_POSITION";
  postTakeProfitAction: "HOLD" | "WAIT_REENTRY" | "QUICK_REENTRY" | "REVERSE_POSITION";
}

```

### TradeLog (거래 로그)

```tsx
interface TradeLog {
  id: number;
  userId: number;
  symbol: string;
  actionType: "BUY" | "SELL" | "HOLD";  // 결정 타입
  confidenceScore: number;     // 신뢰도 (0.0-1.0)
  briefReason: string;         // 간단한 사유
  fullReason: string;          // 상세 사유
  executedPrice: number | null;
  executedQty: number | null;
  realizedPnl: number | null;
  marketSnapshot: string;      // 시장 스냅샷 (JSON 문자열)
  agentName: string;           // 에이전트 이름
  createdAt: string;           // ISO 8601 형식
}

```

### MarketData (차트 데이터)

```tsx
interface MarketData {
  timestamp: string;           // ISO 8601 형식
  open: number;
  high: number;
  low: number;
  close: number;
  volume: number;              // 거래량 (BTC)
  quoteVolume?: number;        // 거래 금액 (USD)
  tradeCount?: number;         // 거래 횟수
  takerBuyVolume?: number;     // 테이커 매수량
  takerBuyQuote?: number;      // 테이커 매수 금액
  rsi?: number | null;         // RSI 지표 (계산되지 않으면 null)
  macd?: number | null;        // MACD 지표
  macdSignal?: number | null;  // MACD 시그널
  maShort?: number | null;     // 단기 이동평균
  maLong?: number | null;      // 장기 이동평균
  bollingerUpper?: number | null;    // 볼린저 밴드 상단
  bollingerMiddle?: number | null;   // 볼린저 밴드 중간
  bollingerLower?: number | null;    // 볼린저 밴드 하단
}

```

### NewsItem (뉴스 아이템)

```tsx
interface NewsItem {
  title: string;
  source: string;
  url: string;
  publishedAt: string;        // ISO 8601 형식
  sentimentScore: number;     // -1.0 (부정) ~ 1.0 (긍정)
}

```

### FearGreedIndex (공포/탐욕 지수)

```tsx
interface FearGreedIndex {
  value: number;              // 0-100
  classification: string;     // "Extreme Fear", "Fear", "Neutral", "Greed", "Extreme Greed"
  timestamp: number;          // Unix timestamp
}

```

### TradeStatistics (거래 통계)

```tsx
interface TradeStatistics {
  totalTrades: number;
  buyTrades: number;
  sellTrades: number;
  winningTrades: number;
  losingTrades: number;
  winRate: number;            // 승률 (%)
  totalProfit: number;
  totalLoss: number;
  netProfit: number;
  avgProfit: number;
  avgLoss: number;
  maxProfit: number;
  maxLoss: number;
}

```

### Notification (알림)

```tsx
interface Notification {
  type: "STOP_LOSS" | "TAKE_PROFIT" | "TRADE_EXECUTION" | "ERROR" | "WARNING" | "SYSTEM_STATUS";
  title: string;
  message: string;
  level: "CRITICAL" | "ERROR" | "WARNING" | "INFO";
  timestamp: string;          // ISO 8601 형식
}

```

### ApiResponse (공통 응답 형식)

```tsx
interface ApiResponse<T> {
  success: boolean;
  message?: string;            // 성공 시: "성공" 또는 커스텀 메시지, 에러 시: 없을 수 있음
  data?: T;                    // 성공 시: 데이터, 에러 시: 없음
  error?: string;              // 에러 시: 에러 메시지
}

```

**응답 규칙:**

- **성공 응답**: `success: true`, `message: "성공"` (또는 커스텀 메시지), `data: {...}`
- **에러 응답**: `success: false`, `error: "에러 메시지"` (또는 `message: "에러 메시지"`)

---

### 3. 설문조사 API

### 3.1 설문조사 문항 목록 조회

```
GET /api/questionnaire/questions

```

**설명:**

- 설문조사 문항 목록을 조회합니다.
- 프론트엔드에서 설문조사 UI를 동적으로 구성할 수 있도록 문항 정보를 제공합니다.
- 인증이 필요하지 않은 공개 API입니다.

**Response:**

```json
{
  "success": true,
  "message": "성공",
  "data": {
    "sections": [
      {
        "id": "A",
        "title": "재무 상황 및 자금 성격",
        "description": "손실 발생 시 사용자의 경제적 타격 정도를 파악하여 자금 투입 비중(Position Sizing)을 조절함.",
        "questions": [
          {
            "id": "Q1",
            "text": "본 프로그램에 투입할 투자 자산의 비중은 귀하의 전체 금융 자산 중 어느 정도입니까?",
            "hasScore": true,
            "options": [
              {
                "value": 1,
                "text": "1. 10% 미만 (없어도 생활에 지장 없는 자금) [4점]"
              },
              {
                "value": 2,
                "text": "2. 10% ~ 30% (여유 자금의 일부) [3점]"
              },
              {
                "value": 3,
                "text": "3. 30% ~ 50% (상당한 비중의 목돈) [2점]"
              },
              {
                "value": 4,
                "text": "4. 50% 이상 (전 재산에 가까움) [1점]"
              }
            ]
          }
        ]
      },
      {
        "id": "B",
        "title": "투자 경험 및 지식",
        "description": "사용자의 이해도를 파악하여 프로그램의 복잡도(UI)와 고급 기능(선물, 숏) 개방 여부를 결정함.",
        "questions": [ ... ]
      },
      {
        "id": "C",
        "title": "위험 감수 성향",
        "description": "손절매(Stop-loss) 비율과 최대 허용 낙폭(MDD)을 설정함.",
        "questions": [ ... ]
      },
      {
        "id": "D",
        "title": "매매 스타일 선호도",
        "description": "진입 알고리즘(추세 vs 역추세)과 거래 시간봉(Timeframe)을 결정함.",
        "questions": [ ... ]
      },
      {
        "id": "E",
        "title": "비트코인 시장관",
        "description": "초기 포지션 방향성(Long Only vs Long/Short) 설정.",
        "questions": [ ... ]
      }
    ],
    "totalQuestions": 15,
    "scoringQuestions": ["Q1", "Q2", "Q3", "Q4", "Q5", "Q6", "Q7", "Q8", "Q9", "Q10", "Q11", "Q14"],
    "nonScoringQuestions": ["Q12", "Q13", "Q15"],
    "scoreRange": {
      "min": 12,
      "max": 48
    }
  }
}

```

**참고:**

- `hasScore: true`: 점수 합산에 포함되는 문항 (Q1~Q11, Q14)
- `hasScore: false`: 점수 합산에 포함되지 않는 문항 (Q12, Q13, Q15)
- 총 15개 문항 중 12개 문항이 점수 합산에 포함됩니다.
- 점수 범위: 12점 ~ 48점

---

### 3.2 설문조사 제출

```
POST /api/questionnaire/submit

```

**설명:**

- 설문조사를 제출하고 사용자 프로필을 생성/업데이트합니다.
- **첫 제출**: 프로필이 생성됩니다.
- **재제출**: 기존 프로필이 새로운 설문조사 결과로 업데이트됩니다.
- 설문조사는 여러 번 제출 가능하며, 각 제출은 `questionnaires` 테이블에 저장됩니다.
- 프로필은 최신 설문조사 결과로 자동 업데이트됩니다.

**Headers:**

```
Authorization: Bearer {sessionToken}
Content-Type: application/json

```

**Request Body:**

```json
{
  "answers": {
    "q1": 1,
    "q2": 2,
    "q3": 3,
    ...
    "q15": 4
  }
}

```

**Response (성공):**

```json
{
  "success": true,
  "message": "설문조사 제출 성공",
  "data": {
    "profile": { ... },
    "investorType": "AGGRESSIVE",
    "tradingStrategy": "SWING_TRADING",
    "totalScore": 35
  }
}

```

**참고:**

- 설문조사를 다시 하려면 동일한 API를 다시 호출하면 됩니다.
- 프로필이 자동으로 업데이트되며, 자동거래에 새로운 전략이 적용됩니다.

---

### 3.3 최근 설문조사 조회

```
GET /api/questionnaire/latest

```

**Headers:**

```
Authorization: Bearer {sessionToken}

```

**Response:**

```json
{
  "success": true,
  "message": "성공",
  "data": {
    "id": 1,
    "userId": 1,
    "answers": { ... },
    "totalScore": 35,
    "createdAt": "2025-11-29T12:00:00"
  }
}

```

**Response (설문조사 없음):**

```json
{
  "success": false,
  "error": "설문조사가 없습니다."
}

```

---

### 4. 자동 거래 제어 API

### 4.1 자동 거래 시작

```
POST /api/trading/start

```

**Headers:**

```
Authorization: Bearer {sessionToken}

```

**Response (성공):**

```json
{
  "success": true,
  "message": "자동 거래 시작",
  "data": {
    "status": "started",
    "strategy": "SWING_TRADING",
    "intervalMinutes": 60
  }
}

```

---

### 4.2 자동 거래 중지

```
POST /api/trading/stop

```

**Headers:**

```
Authorization: Bearer {sessionToken}

```

**Response (성공):**

```json
{
  "success": true,
  "message": "자동 거래 중지",
  "data": {
    "status": "stopped"
  }
}

```

---

### 4.3 자동 거래 상태 조회

```
GET /api/trading/status

```

**Headers:**

```
Authorization: Bearer {sessionToken}

```

**Response (실행 중):**

```json
{
  "success": true,
  "message": "성공",
  "data": {
    "running": true,
    "status": "running",
    "strategy": "SWING_TRADING",
    "intervalMinutes": 60
  }
}

```

**Response (중지됨):**

```json
{
  "success": true,
  "message": "성공",
  "data": {
    "running": false,
    "status": "stopped",
    "strategy": "SWING_TRADING",
    "intervalMinutes": 60
  }
}

```

**Response (초기화되지 않음):**

```json
{
  "success": true,
  "message": "성공",
  "data": {
    "running": false,
    "status": "not_initialized"
  }
}

```

---

### 5. 시장 데이터 API

### 5.1 차트 데이터 조회 (OHLCV)

```
GET /api/market/chart?symbol=BTCUSDT&timeframe=1h&limit=100

```

**Query Parameters:**

- `symbol` (optional): 거래 쌍 (기본값: BTCUSDT)
- `timeframe` (optional): 시간봉 (1m, 5m, 15m, 1h, 4h, 1d 등, 기본값: 1h)
- `limit` (optional): 조회할 캔들 개수 (기본값: 100)

**Response:**

```json
{
  "success": true,
  "message": "성공",
  "data": [
    {
      "timestamp": "2025-11-29T12:00:00",
      "open": 50000.0,
      "high": 51000.0,
      "low": 49000.0,
      "close": 50500.0,
      "volume": 1000.0,
      "quoteVolume": 50500000.0,
      "tradeCount": 1000,
      "takerBuyVolume": 500.0,
      "takerBuyQuote": 25250000.0,
      "rsi": 55.5,
      "macd": 100.0,
      "macdSignal": 95.0,
      "maShort": 50000.0,
      "maLong": 49500.0,
      "bollingerUpper": 51000.0,
      "bollingerMiddle": 50000.0,
      "bollingerLower": 49000.0
    }
  ]
}

```

**참고:**

- 기술 지표(`rsi`, `macd` 등)는 계산된 경우에만 포함되며, 없으면 `null`
- `quoteVolume`: 거래 금액 (USD)
- `tradeCount`: 거래 횟수
- `takerBuyVolume`, `takerBuyQuote`: 테이커 매수량 및 금액

---

### 5.2 현재 가격 조회

```
GET /api/market/price?symbol=BTCUSDT

```

**Response:**

```json
{
  "success": true,
  "message": "성공",
  "data": {
    "symbol": "BTCUSDT",
    "price": 50000.0,
    "timestamp": 1701234567890
  }
}

```

---

### 5.3 24시간 통계 조회

```
GET /api/market/24h-stats?symbol=BTCUSDT

```

**Response:**

```json
{
  "success": true,
  "message": "성공",
  "data": {
    "priceChange": 1000.0,
    "priceChangePercent": 2.0,
    "highPrice": 51000.0,
    "lowPrice": 49000.0,
    "volume": 1000000.0,
    "quoteVolume": 50500000000.0,
    "count": 100000
  }
}

```

**참고:**

- 실제 응답 필드명은 Binance API 응답에 따라 다를 수 있음

---

### 5.4 뉴스 조회

```
GET /api/market/news?limit=10

```

**Query Parameters:**

- `limit` (optional): 조회할 뉴스 개수 (기본값: 10)

**Response:**

```json
{
  "success": true,
  "message": "성공",
  "data": [
    {
      "title": "Bitcoin Price Surges",
      "source": "CryptoPanic",
      "url": "https://...",
      "sentimentScore": 0.8,
      "publishedAt": "2025-11-29T12:00:00"
    }
  ]
}

```

---

### 5.5 공포/탐욕 지수 조회

```
GET /api/market/fear-greed

```

**Response:**

```json
{
  "success": true,
  "message": "성공",
  "data": {
    "value": 65,
    "classification": "Greed",
    "timestamp": "2025-11-29T12:00:00"
  }
}

```

---

### 6. 통계 API

### 6.1 거래 통계 조회

```
GET /api/statistics/trades

```

**Headers:**

```
Authorization: Bearer {sessionToken}

```

**Response:**

```json
{
  "success": true,
  "message": "성공",
  "data": {
    "totalTrades": 100,
    "buyTrades": 50,
    "sellTrades": 50,
    "winningTrades": 60,
    "losingTrades": 40,
    "winRate": 60.0,
    "totalProfit": 5000.0,
    "totalLoss": 2000.0,
    "netProfit": 3000.0,
    "avgProfit": 83.33,
    "avgLoss": 50.0,
    "maxProfit": 500.0,
    "maxLoss": -200.0
  }
}

```

---

### 7. 알림 API

### 7.1 최근 알림 조회

```
GET /api/notifications?limit=50

```

**Query Parameters:**

- `limit` (optional): 조회할 알림 개수 (기본값: 50)

**Response:**

```json
{
  "success": true,
  "message": "성공",
  "data": [
    {
      "type": "STOP_LOSS",
      "title": "손절 실행",
      "message": "🚨 [긴급] 손절 실행...",
      "level": "CRITICAL",
      "timestamp": "2025-11-29T12:00:00"
    }
  ]
}

```

---

---

## 📊 데이터베이스 스키마

프론트엔드 개발자는 데이터베이스에 직접 접근하지 않고 REST API를 통해서만 데이터를 조회합니다.

**상세 스키마 문서**: [DATABASE_SCHEMA.md](https://www.notion.so/DATABASE_SCHEMA.md) 참고

### 주요 테이블

- `users`: 사용자 인증 정보
- `user_profiles`: 투자 성향 및 전략 설정
- `questionnaires`: 설문조사 응답
- `trades`: 거래 내역
- `trade_logs`: AI 판단 기록 (HOLD 포함)
- `llm_analysis_logs`: LLM 분석 상세 로그
- `portfolio_snapshots`: 포트폴리오 상태 스냅샷
- `market_data_cache`: 시장 데이터 캐시
- `system_events`: 시스템 이벤트 로그

---

## 🔧 서버 실행 방법

### 1. 환경 설정

`.env` 파일에 다음 정보를 설정하세요:

```
GEMINI_API_KEY=your_gemini_api_key
BINANCE_API_KEY=your_binance_api_key
BINANCE_SECRET_KEY=your_binance_secret_key
BINANCE_USE_TESTNET=true
TRADING_MODE=SIMULATION
DB_TYPE=mysql
MYSQL_HOST=203.234.62.223
MYSQL_PORT=3306
MYSQL_DATABASE=bitbot
MYSQL_USERNAME=root
MYSQL_PASSWORD=dsem1010!

```

### 2. 서버 시작

```bash
# Windows
.\\run-server.bat

# 또는 Maven 직접 실행
mvn spring-boot:run

```

서버는 `http://localhost:8080`에서 실행되며, API는 `http://localhost:8080/api`를 통해 접근할 수 있습니다.

---

## ⚠️ 주의사항

1. **CORS 설정**: 현재 모든 origin을 허용하도록 설정되어 있습니다. 프로덕션 환경에서는 특정 도메인만 허용하도록 변경해야 합니다.
2. **인증**: 모든 사용자별 API는 세션 토큰 기반 인증을 사용합니다. 각 사용자는 자신의 데이터만 조회/수정할 수 있습니다.
3. **에러 처리**: 모든 API는 `ApiResponse` 형식으로 응답하며, `success: false`일 때 `message`에 에러 메시지가 포함됩니다.
4. **비밀번호 정책**:
    - 최소 8자 이상
    - 영문, 숫자, 특수문자 중 2가지 이상 포함

---

## 📝 예제 코드

### JavaScript/TypeScript (Fetch API)

```tsx
// 로그인
async function login(email: string, password: string) {
  const response = await fetch('<http://localhost:8080/api/auth/login>', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ email, password }),
  });

  const data = await response.json();
  if (data.success) {
    localStorage.setItem('sessionToken', data.data.sessionToken);
    return data.data;
  } else {
    throw new Error(data.message);
  }
}

// 인증이 필요한 API 호출
async function getTrades(limit: number = 50) {
  const token = localStorage.getItem('sessionToken');
  const response = await fetch(`http://localhost:8080/api/trades?limit=${limit}`, {
    headers: {
      'Authorization': `Bearer ${token}`,
    },
  });

  const data = await response.json();
  return data.data;
}

```

### Axios 예제

```tsx
import axios from 'axios';

const api = axios.create({
  baseURL: '<http://localhost:8080/api>',
});

// 요청 인터셉터: 토큰 자동 추가
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('sessionToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 로그인
const login = async (email: string, password: string) => {
  const response = await api.post('/auth/login', { email, password });
  if (response.data.success) {
    localStorage.setItem('sessionToken', response.data.data.sessionToken);
  }
  return response.data;
};

// 거래 내역 조회
const getTrades = async (limit: number = 50) => {
  const response = await api.get('/trades', { params: { limit } });
  return response.data.data;
};

```

---

## 🐛 문제 해결

### CORS 오류

- 서버의 `CorsConfig`가 올바르게 설정되어 있는지 확인
- 프론트엔드에서 `credentials: 'include'` 옵션 사용

### 인증 오류

- `Authorization` 헤더 형식 확인: `Bearer {token}`
- 세션 토큰이 만료되었는지 확인 (`/api/auth/verify` 호출)

### 연결 오류

- 서버가 실행 중인지 확인 (`/api/health` 호출)
- 포트 번호 확인 (기본값: 8080)

---

**문서 버전**: 1.3.0

**최종 업데이트**: 2025-01-XX

## 🔄 변경 이력

### v1.3.0 (2025-01-XX)

- ✅ 설문조사 문항 목록 조회 API 추가 (`GET /api/questionnaire/questions`)
- ✅ 총 엔드포인트 개수 업데이트 (22개 → 23개)
- ✅ 공개 API 개수 업데이트 (10개 → 11개)

### v1.2.0 (2025-01-XX)

- ✅ 실제 API 응답 구조에 맞게 문서 수정
- ✅ 에러 응답 구조 수정 (`error` 필드 추가)
- ✅ `AccountInfo` 필드 추가 (`investedAmount`, `totalProfitLoss`, `totalTrades` 등)
- ✅ `TradeLog` 필드명 수정 (`decision` → `actionType`, `confidence` → `confidenceScore`)
- ✅ `MarketData` 필드 추가 (`quoteVolume`, `tradeCount`, `takerBuyVolume` 등)
- ✅ 자동거래 상태 응답에 `not_initialized` 상태 추가
- ✅ 모든 성공 응답에 `message: "성공"` 포함 확인

### v1.1.1 (2025-01-XX)

- ✅ 엔드포인트 개수 수정 (21개 → 22개)
- ✅ 공개 API 개수 수정 (8개 → 9개)

### v1.1.0 (2025-11-30)

- ✅ 모든 사용자별 API에 세션 토큰 기반 인증 적용
- ✅ 사용자별 Binance API 키 및 테스트넷 설정 지원
- ✅ 다중 사용자 동시 사용 지원
- ✅ API 목록에 인증 필요 여부 표시 추가

### v1.0.0 (2025-11-29)

- 초기 API 문서 작성