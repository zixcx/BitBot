# 📚 BitBot REST API 문서


## 🌐 기본 정보

- **Base URL**: `http://localhost:8080`
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

### 총 20개 엔드포인트

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

## 📋 API 엔드포인트 상세

### 1. 인증 API

#### 1.1 회원가입
```http
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
  "message": "회원가입 실패: 이메일이 이미 존재합니다.",
  "data": null
}
```

---

#### 1.2 로그인
```http
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
  "message": "로그인 실패: 이메일 또는 비밀번호가 올바르지 않습니다.",
  "data": null
}
```

---

#### 1.3 로그아웃
```http
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

#### 1.4 세션 검증
```http
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
  "message": null,
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
  "message": null,
  "data": {
    "valid": false
  }
}
```

---

### 2. 거래 API

#### 2.1 서버 상태 확인
```http
GET /api/health
```

**Response:**
```json
{
  "success": true,
  "message": null,
  "data": {
    "status": "UP",
    "service": "BitBot Trading Server"
  }
}
```

---

#### 2.2 거래 내역 조회
```http
GET /api/trades?limit=50
```

**Query Parameters:**
- `limit` (optional): 조회할 거래 내역 개수 (기본값: 50)

**Response:**
```json
{
  "success": true,
  "message": null,
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

#### 2.3 계좌 정보 조회
```http
GET /api/account
```

**Response:**
```json
{
  "success": true,
  "message": null,
  "data": {
    "totalBalance": 10000.0,
    "usdtBalance": 9500.0,
    "btcHolding": 0.01,
    "profitLoss": 500.0,
    "profitLossPercent": 5.0
  }
}
```

---

#### 2.4 사용자 프로필 조회
```http
GET /api/profile
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
  "message": "프로필이 없습니다. 설문조사를 먼저 완료하세요.",
  "data": null
}
```

---

#### 2.5 거래 로그 조회 (AI 판단 기록)
```http
GET /api/trade-logs?limit=50
```

**Query Parameters:**
- `limit` (optional): 조회할 로그 개수 (기본값: 50)

**Response:**
```json
{
  "success": true,
  "message": null,
  "data": [
    {
      "id": 1,
      "userId": 1,
      "symbol": "BTCUSDT",
      "decision": "BUY",
      "briefReason": "기술적 분석 결과 매수 신호",
      "fullReason": "RSI가 과매도 구간에서 반등...",
      "confidence": 0.85,
      "executedPrice": 50000.0,
      "executedQty": 0.001,
      "realizedPnl": null,
      "marketSnapshot": {
        "price": 50000.0,
        "volume": 1000.0,
        "rsi": 35.0,
        "macd": 100.0
      },
      "agentName": "MasterCoordinatorAgent",
      "createdAt": "2025-11-29T12:00:00"
    }
  ]
}
```

---

## 📊 데이터 모델

### TradeOrder (거래 주문)
```typescript
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
```typescript
interface AccountInfo {
  totalBalance: number;        // 총 잔고 (USDT)
  usdtBalance: number;         // USDT 잔고
  btcHolding: number;          // 보유 BTC 수량
  profitLoss: number;          // 손익 (USDT)
  profitLossPercent: number;   // 손익률 (%)
}
```

### UserProfile (사용자 프로필)
```typescript
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
```typescript
interface TradeLog {
  id: number;
  userId: number;
  symbol: string;
  decision: "BUY" | "SELL" | "HOLD" | "STRONG_BUY" | "STRONG_SELL";
  briefReason: string;         // 간단한 사유
  fullReason: string;          // 상세 사유
  confidence: number;          // 신뢰도 (0.0-1.0)
  executedPrice: number | null;
  executedQty: number | null;
  realizedPnl: number | null;
  marketSnapshot: object;      // 시장 스냅샷 (JSON)
  agentName: string;           // 에이전트 이름
  createdAt: string;           // ISO 8601 형식
}
```

### MarketData (차트 데이터)
```typescript
interface MarketData {
  timestamp: string;           // ISO 8601 형식
  open: number;
  high: number;
  low: number;
  close: number;
  volume: number;
  rsi?: number;               // RSI 지표
  macd?: number;              // MACD 지표
  maShort?: number;           // 단기 이동평균
  maLong?: number;            // 장기 이동평균
  bollingerUpper?: number;    // 볼린저 밴드 상단
  bollingerMiddle?: number;   // 볼린저 밴드 중간
  bollingerLower?: number;    // 볼린저 밴드 하단
}
```

### NewsItem (뉴스 아이템)
```typescript
interface NewsItem {
  title: string;
  source: string;
  url: string;
  publishedAt: string;        // ISO 8601 형식
  sentimentScore: number;     // -1.0 (부정) ~ 1.0 (긍정)
}
```

### FearGreedIndex (공포/탐욕 지수)
```typescript
interface FearGreedIndex {
  value: number;              // 0-100
  classification: string;     // "Extreme Fear", "Fear", "Neutral", "Greed", "Extreme Greed"
  timestamp: number;          // Unix timestamp
}
```

### TradeStatistics (거래 통계)
```typescript
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
```typescript
interface Notification {
  type: "STOP_LOSS" | "TAKE_PROFIT" | "TRADE_EXECUTION" | "ERROR" | "WARNING" | "SYSTEM_STATUS";
  title: string;
  message: string;
  level: "CRITICAL" | "ERROR" | "WARNING" | "INFO";
  timestamp: string;          // ISO 8601 형식
}
```

### ApiResponse (공통 응답 형식)
```typescript
interface ApiResponse<T> {
  success: boolean;
  message: string | null;
  data: T | null;
}
```

---

### 3. 설문조사 API

#### 3.1 설문조사 제출
```http
POST /api/questionnaire/submit
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

---

#### 3.2 최근 설문조사 조회
```http
GET /api/questionnaire/latest
```

**Response:**
```json
{
  "success": true,
  "message": null,
  "data": {
    "id": 1,
    "userId": 1,
    "answers": { ... },
    "totalScore": 35,
    "createdAt": "2025-11-29T12:00:00"
  }
}
```

---

### 4. 자동 거래 제어 API

#### 4.1 자동 거래 시작
```http
POST /api/trading/start
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

#### 4.2 자동 거래 중지
```http
POST /api/trading/stop
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

#### 4.3 자동 거래 상태 조회
```http
GET /api/trading/status
```

**Response:**
```json
{
  "success": true,
  "message": null,
  "data": {
    "running": true,
    "status": "running",
    "strategy": "SWING_TRADING",
    "intervalMinutes": 60
  }
}
```

---

### 5. 시장 데이터 API

#### 5.1 차트 데이터 조회 (OHLCV)
```http
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
  "message": null,
  "data": [
    {
      "timestamp": "2025-11-29T12:00:00",
      "open": 50000.0,
      "high": 51000.0,
      "low": 49000.0,
      "close": 50500.0,
      "volume": 1000.0,
      "rsi": 55.5,
      "macd": 100.0,
      "maShort": 50000.0,
      "maLong": 49500.0
    }
  ]
}
```

---

#### 5.2 현재 가격 조회
```http
GET /api/market/price?symbol=BTCUSDT
```

**Response:**
```json
{
  "success": true,
  "message": null,
  "data": {
    "symbol": "BTCUSDT",
    "price": 50000.0,
    "timestamp": 1701234567890
  }
}
```

---

#### 5.3 24시간 통계 조회
```http
GET /api/market/24h-stats?symbol=BTCUSDT
```

**Response:**
```json
{
  "success": true,
  "message": null,
  "data": {
    "symbol": "BTCUSDT",
    "priceChange": 1000.0,
    "priceChangePercent": 2.0,
    "highPrice": 51000.0,
    "lowPrice": 49000.0,
    "volume": 1000000.0
  }
}
```

---

#### 5.4 뉴스 조회
```http
GET /api/market/news?limit=10
```

**Query Parameters:**
- `limit` (optional): 조회할 뉴스 개수 (기본값: 10)

**Response:**
```json
{
  "success": true,
  "message": null,
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

#### 5.5 공포/탐욕 지수 조회
```http
GET /api/market/fear-greed
```

**Response:**
```json
{
  "success": true,
  "message": null,
  "data": {
    "value": 65,
    "classification": "Greed",
    "timestamp": "2025-11-29T12:00:00"
  }
}
```

---

### 6. 통계 API

#### 6.1 거래 통계 조회
```http
GET /api/statistics/trades
```

**Response:**
```json
{
  "success": true,
  "message": null,
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

#### 7.1 최근 알림 조회
```http
GET /api/notifications?limit=50
```

**Query Parameters:**
- `limit` (optional): 조회할 알림 개수 (기본값: 50)

**Response:**
```json
{
  "success": true,
  "message": null,
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

**상세 스키마 문서**: [DATABASE_SCHEMA.md](DATABASE_SCHEMA.md) 참고

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
```env
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
.\run-server.bat

# 또는 Maven 직접 실행
mvn spring-boot:run
```

서버는 `http://localhost:8080`에서 실행됩니다.

---

## ⚠️ 주의사항

1. **CORS 설정**: 현재 모든 origin을 허용하도록 설정되어 있습니다. 프로덕션 환경에서는 특정 도메인만 허용하도록 변경해야 합니다.

2. **인증**: 현재 `TradingController`는 기본 사용자 ID(1)를 사용합니다. 실제 운영 환경에서는 세션 토큰에서 사용자 ID를 추출하도록 수정이 필요합니다.

3. **에러 처리**: 모든 API는 `ApiResponse` 형식으로 응답하며, `success: false`일 때 `message`에 에러 메시지가 포함됩니다.

4. **비밀번호 정책**: 
   - 최소 8자 이상
   - 영문, 숫자, 특수문자 중 2가지 이상 포함

---

## 📝 예제 코드

### JavaScript/TypeScript (Fetch API)
```typescript
// 로그인
async function login(email: string, password: string) {
  const response = await fetch('http://localhost:8080/api/auth/login', {
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
```typescript
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
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

**문서 버전**: 1.0.0  
**최종 업데이트**: 2025-11-29

