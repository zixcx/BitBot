# 📊 BitBot 데이터베이스 스키마 문서

프론트엔드 개발자를 위한 데이터베이스 구조 설명서

## 📋 개요

- **데이터베이스 타입**: MySQL (원격 서버) 또는 SQLite (로컬 파일)
- **접근 방식**: REST API를 통해서만 접근 (직접 DB 접근 불필요)
- **스키마 위치**: `src/main/resources/db/schema.sql`

---

## 🗂️ 테이블 구조

### 1. users (사용자 테이블)

사용자 인증 정보 및 설정

| 컬럼명 | 타입 | 설명 | 제약조건 |
|--------|------|------|----------|
| id | INTEGER | 사용자 ID (PK) | PRIMARY KEY, AUTO_INCREMENT |
| email | TEXT | 이메일 | UNIQUE, NOT NULL |
| username | TEXT | 사용자명 | UNIQUE, NOT NULL |
| password_hash | TEXT | 비밀번호 해시 (BCrypt) | NOT NULL |
| binance_api_key_encrypted | TEXT | Binance API 키 (AES-256 암호화) | NULL |
| binance_secret_key_encrypted | TEXT | Binance Secret 키 (AES-256 암호화) | NULL |
| trading_enabled | INTEGER | 거래 활성화 여부 (0/1) | DEFAULT 0 |
| risk_management_enabled | INTEGER | 리스크 관리 활성화 여부 (0/1) | DEFAULT 1 |
| max_investment_percent | REAL | 최대 투자 비율 (%) | DEFAULT 10.00 |
| created_at | TEXT | 생성일시 | DEFAULT (datetime('now')) |
| updated_at | TEXT | 수정일시 | DEFAULT (datetime('now')) |

**관련 API**: `/api/auth/register`, `/api/auth/login`

---

### 2. user_profiles (사용자 프로필 테이블)

투자 성향 및 전략 설정

| 컬럼명 | 타입 | 설명 | 제약조건 |
|--------|------|------|----------|
| id | INTEGER | 프로필 ID (PK) | PRIMARY KEY, AUTO_INCREMENT |
| user_id | INTEGER | 사용자 ID (FK) | NOT NULL, UNIQUE, FOREIGN KEY |
| investor_type | TEXT | 투자 성향 | NOT NULL (CONSERVATIVE, MODERATE, AGGRESSIVE, SPECULATIVE) |
| total_score | INTEGER | 설문 총점 | NOT NULL (12-48) |
| risk_settings | TEXT | 리스크 설정 (JSON) | NOT NULL |
| trading_strategy | TEXT | 거래 전략 | NOT NULL (SPOT_DCA, TREND_FOLLOWING, SWING_TRADING, VOLATILITY_BREAKOUT) |
| created_at | TEXT | 생성일시 | DEFAULT (datetime('now')) |
| updated_at | TEXT | 수정일시 | DEFAULT (datetime('now')) |

**risk_settings JSON 구조**:
```json
{
  "leverageAllowed": true,
  "maxLeverage": 3,
  "maxLossPercent": -5.0,
  "maxPositionPercent": 30.0,
  "stopLossPercent": -5.0,
  "takeProfitPercent": 20.0,
  "postStopLossAction": "HOLD",
  "postTakeProfitAction": "HOLD"
}
```

**관련 API**: `/api/profile`, `/api/questionnaire/submit`

---

### 3. questionnaires (설문조사 응답 테이블)

설문조사 답변 및 결과

| 컬럼명 | 타입 | 설명 | 제약조건 |
|--------|------|------|----------|
| id | INTEGER | 설문조사 ID (PK) | PRIMARY KEY, AUTO_INCREMENT |
| user_id | INTEGER | 사용자 ID (FK) | NOT NULL, FOREIGN KEY |
| answers | TEXT | 답변 (JSON) | NOT NULL |
| total_score | INTEGER | 총점 | NOT NULL (12-48) |
| result_type | TEXT | 결과 유형 | NOT NULL (CONSERVATIVE, MODERATE, AGGRESSIVE, SPECULATIVE) |
| completed_at | TEXT | 완료일시 | DEFAULT (datetime('now')) |

**answers JSON 구조**:
```json
{
  "q1": 1,
  "q2": 2,
  "q3": 3,
  ...
  "q15": 4
}
```

**관련 API**: `/api/questionnaire/submit`, `/api/questionnaire/latest`

---

### 4. trades (거래 내역 테이블)

실제 거래 주문 내역

| 컬럼명 | 타입 | 설명 | 제약조건 |
|--------|------|------|----------|
| id | INTEGER | 거래 ID (PK) | PRIMARY KEY, AUTO_INCREMENT |
| user_id | INTEGER | 사용자 ID (FK) | FOREIGN KEY |
| symbol | TEXT | 거래 쌍 | NOT NULL, DEFAULT 'BTCUSDT' |
| order_type | TEXT | 주문 타입 | NOT NULL (MARKET_BUY, MARKET_SELL) |
| order_status | TEXT | 주문 상태 | NOT NULL (PENDING, FILLED, FAILED, REJECTED) |
| quantity | REAL | 수량 (BTC) | NOT NULL |
| price | REAL | 주문 가격 | NOT NULL |
| executed_price | REAL | 체결 가격 | NULL |
| total_cost | REAL | 총 비용 | NULL |
| leverage | INTEGER | 레버리지 배수 | DEFAULT 1 |
| is_futures_trade | INTEGER | 선물 거래 여부 (0/1) | DEFAULT 0 |
| profit_loss | REAL | 손익 (USDT) | NULL |
| profit_loss_percent | REAL | 손익률 (%) | NULL |
| decision_reason | TEXT | 거래 사유 | NULL |
| agent_name | TEXT | 에이전트 이름 | NULL |
| confidence | REAL | 신뢰도 (0.0-1.0) | NULL |
| binance_order_id | TEXT | Binance 주문 ID | NULL |
| created_at | TEXT | 생성일시 | DEFAULT (datetime('now')) |
| executed_at | TEXT | 체결일시 | NULL |

**관련 API**: `/api/trades`, `/api/statistics/trades`

---

### 5. trade_logs (거래 로그 테이블)

AI 판단 기록 (HOLD 포함 모든 결정)

| 컬럼명 | 타입 | 설명 | 제약조건 |
|--------|------|------|----------|
| id | INTEGER | 로그 ID (PK) | PRIMARY KEY, AUTO_INCREMENT |
| user_id | INTEGER | 사용자 ID (FK) | NOT NULL, FOREIGN KEY |
| symbol | TEXT | 거래 쌍 | NOT NULL, DEFAULT 'BTCUSDT' |
| action_type | TEXT | 행동 타입 | NOT NULL (BUY, SELL, HOLD, STRONG_BUY, STRONG_SELL) |
| confidence_score | REAL | 신뢰도 (0.0-1.0) | NULL |
| brief_reason | TEXT | 간단한 사유 (한 줄) | NULL |
| full_reason | TEXT | 상세 분석 내용 | NULL |
| executed_price | REAL | 체결 가격 (HOLD시 NULL) | NULL |
| executed_qty | REAL | 체결 수량 (HOLD시 NULL) | NULL |
| realized_pnl | REAL | 실현 손익 (매도 시) | NULL |
| market_snapshot | TEXT | 시장 스냅샷 (JSON) | NULL |
| agent_name | TEXT | 에이전트 이름 | NULL |
| created_at | TEXT | 생성일시 | DEFAULT (datetime('now')) |

**market_snapshot JSON 구조**:
```json
{
  "price": 50000.0,
  "volume": 1000.0,
  "rsi": 35.0,
  "macd": 100.0,
  "ma20": 49000.0
}
```

**관련 API**: `/api/trade-logs`

---

### 6. llm_analysis_logs (LLM 분석 로그 테이블)

LLM 에이전트 분석 상세 로그

| 컬럼명 | 타입 | 설명 | 제약조건 |
|--------|------|------|----------|
| id | INTEGER | 로그 ID (PK) | PRIMARY KEY, AUTO_INCREMENT |
| user_id | INTEGER | 사용자 ID (FK) | FOREIGN KEY |
| agent_name | TEXT | 에이전트 이름 | NOT NULL |
| request_prompt | TEXT | 요청 프롬프트 | NOT NULL |
| response_raw | TEXT | 원본 응답 | NOT NULL |
| response_parsed | TEXT | 파싱된 응답 (JSON) | NULL |
| decision | TEXT | 결정 (BUY, SELL, HOLD) | NULL |
| confidence | REAL | 신뢰도 | NULL |
| reason | TEXT | 사유 | NULL |
| market_data_snapshot | TEXT | 시장 데이터 스냅샷 (JSON) | NULL |
| llm_provider | TEXT | LLM 제공자 | DEFAULT 'gemini' |
| tokens_used | INTEGER | 사용된 토큰 수 | NULL |
| response_time_ms | INTEGER | 응답 시간 (ms) | NULL |
| action_taken | TEXT | 취해진 행동 | NULL |
| trade_id | INTEGER | 거래 ID (FK) | FOREIGN KEY |
| created_at | TEXT | 생성일시 | DEFAULT (datetime('now')) |

**관련 API**: 현재 API 없음 (향후 추가 가능)

---

### 7. portfolio_snapshots (포트폴리오 스냅샷 테이블)

포트폴리오 상태 스냅샷 (시간별 기록)

| 컬럼명 | 타입 | 설명 | 제약조건 |
|--------|------|------|----------|
| id | INTEGER | 스냅샷 ID (PK) | PRIMARY KEY, AUTO_INCREMENT |
| user_id | INTEGER | 사용자 ID (FK) | NOT NULL, FOREIGN KEY |
| total_balance | REAL | 총 잔고 (USDT) | NOT NULL |
| available_balance | REAL | 사용 가능 잔고 (USDT) | NOT NULL |
| invested_amount | REAL | 투자 중인 금액 (USDT) | NOT NULL |
| btc_holding | REAL | 보유 BTC 수량 | NOT NULL, DEFAULT 0 |
| btc_value | REAL | 보유 BTC 가치 (USDT) | NOT NULL, DEFAULT 0 |
| total_profit_loss | REAL | 총 손익 (USDT) | NOT NULL, DEFAULT 0 |
| profit_loss_percent | REAL | 손익률 (%) | NOT NULL, DEFAULT 0 |
| total_trades | INTEGER | 총 거래 횟수 | NOT NULL, DEFAULT 0 |
| winning_trades | INTEGER | 수익 거래 수 | NOT NULL, DEFAULT 0 |
| losing_trades | INTEGER | 손실 거래 수 | NOT NULL, DEFAULT 0 |
| created_at | TEXT | 생성일시 | DEFAULT (datetime('now')) |

**관련 API**: 현재 API 없음 (향후 추가 가능)

---

### 8. market_data_cache (시장 데이터 캐시 테이블)

시장 데이터 캐시 (성능 최적화용)

| 컬럼명 | 타입 | 설명 | 제약조건 |
|--------|------|------|----------|
| id | INTEGER | 캐시 ID (PK) | PRIMARY KEY, AUTO_INCREMENT |
| symbol | TEXT | 거래 쌍 | NOT NULL, DEFAULT 'BTCUSDT' |
| timestamp | TEXT | 타임스탬프 | NOT NULL |
| open_price | REAL | 시가 | NOT NULL |
| high_price | REAL | 고가 | NOT NULL |
| low_price | REAL | 저가 | NOT NULL |
| close_price | REAL | 종가 | NOT NULL |
| volume | REAL | 거래량 | NOT NULL |
| rsi | REAL | RSI 지표 | NULL |
| macd | REAL | MACD 지표 | NULL |
| macd_signal | REAL | MACD 시그널 | NULL |
| ma_short | REAL | 단기 이동평균 | NULL |
| ma_long | REAL | 장기 이동평균 | NULL |
| bollinger_upper | REAL | 볼린저 밴드 상단 | NULL |
| bollinger_middle | REAL | 볼린저 밴드 중간 | NULL |
| bollinger_lower | REAL | 볼린저 밴드 하단 | NULL |
| created_at | TEXT | 생성일시 | DEFAULT (datetime('now')) |

**UNIQUE 제약**: (symbol, timestamp)

**관련 API**: `/api/market/chart` (간접적으로 사용)

---

### 9. system_events (시스템 이벤트 로그 테이블)

시스템 이벤트 로그

| 컬럼명 | 타입 | 설명 | 제약조건 |
|--------|------|------|----------|
| id | INTEGER | 이벤트 ID (PK) | PRIMARY KEY, AUTO_INCREMENT |
| event_type | TEXT | 이벤트 타입 | NOT NULL (INFO, WARNING, ERROR, TRADE, ANALYSIS) |
| event_message | TEXT | 이벤트 메시지 | NOT NULL |
| event_details | TEXT | 이벤트 상세 (JSON) | NULL |
| user_id | INTEGER | 사용자 ID (FK) | FOREIGN KEY |
| created_at | TEXT | 생성일시 | DEFAULT (datetime('now')) |

**관련 API**: 현재 API 없음 (향후 추가 가능)

---

## 🔗 테이블 관계도

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
            ├── (N) portfolio_snapshots
            │
            └── (N) system_events

trades (1) ── (N) llm_analysis_logs
```

---

## 📊 주요 인덱스

### 성능 최적화를 위한 인덱스

1. **trades 테이블**
   - `idx_trades_user_id`: 사용자별 거래 조회
   - `idx_trades_created_at`: 시간순 정렬
   - `idx_trades_symbol`: 심볼별 조회

2. **trade_logs 테이블**
   - `idx_trade_logs_user_id`: 사용자별 로그 조회
   - `idx_trade_logs_created_at`: 시간순 정렬
   - `idx_trade_logs_action_type`: 행동 타입별 필터링
   - `idx_trade_logs_symbol`: 심볼별 조회

3. **llm_analysis_logs 테이블**
   - `idx_llm_logs_user_id`: 사용자별 로그 조회
   - `idx_llm_logs_agent`: 에이전트별 조회
   - `idx_llm_logs_created_at`: 시간순 정렬

4. **market_data_cache 테이블**
   - `idx_market_data_symbol_timestamp`: 심볼+시간 조회 (UNIQUE)

---

## 🔄 데이터 흐름

### 1. 사용자 등록 및 설문조사
```
사용자 등록 (users)
    ↓
설문조사 제출 (questionnaires)
    ↓
프로필 생성 (user_profiles)
```

### 2. 거래 실행
```
거래 사이클 실행
    ↓
LLM 분석 (llm_analysis_logs)
    ↓
거래 결정 기록 (trade_logs)
    ↓
주문 실행 (trades)
    ↓
포트폴리오 스냅샷 (portfolio_snapshots)
```

### 3. 시장 데이터 수집
```
Binance API 호출
    ↓
시장 데이터 캐시 (market_data_cache)
    ↓
API 응답 (/api/market/chart)
```

---

## 💡 프론트엔드 개발자를 위한 참고사항

### ✅ REST API 사용 권장

프론트엔드 개발자는 **데이터베이스에 직접 접근하지 않고**, REST API를 통해서만 데이터를 조회/수정해야 합니다.

**이유**:
- 보안: 데이터베이스 접근 권한 불필요
- 일관성: 모든 데이터 접근이 API를 통해 검증됨
- 유지보수: 백엔드 로직 변경 시 프론트엔드 수정 최소화

### 📋 API와 테이블 매핑

| 테이블 | 관련 API 엔드포인트 |
|--------|-------------------|
| users | `/api/auth/register`, `/api/auth/login` |
| user_profiles | `/api/profile` |
| questionnaires | `/api/questionnaire/submit`, `/api/questionnaire/latest` |
| trades | `/api/trades`, `/api/statistics/trades` |
| trade_logs | `/api/trade-logs` |
| market_data_cache | `/api/market/chart` (간접) |

### 🔍 데이터 조회 예시

**거래 내역 조회**:
```typescript
// ❌ 직접 DB 접근 (하지 않음)
// SELECT * FROM trades WHERE user_id = 1;

// ✅ REST API 사용
const response = await fetch('http://localhost:8080/api/trades?limit=50');
const data = await response.json();
const trades = data.data; // TradeOrder[]
```

**프로필 조회**:
```typescript
// ✅ REST API 사용
const response = await fetch('http://localhost:8080/api/profile');
const data = await response.json();
const profile = data.data; // UserProfile
```

---

## 🛠️ 데이터베이스 설정

### MySQL (원격 서버)

`.env` 파일 설정:
```env
DB_TYPE=mysql
MYSQL_HOST=203.234.62.223
MYSQL_PORT=3306
MYSQL_DATABASE=bitbot
MYSQL_USERNAME=root
MYSQL_PASSWORD=dsem1010!
```

### SQLite (로컬 파일)

`.env` 파일 설정:
```env
DB_TYPE=sqlite
```

데이터베이스 파일 위치: `data/bitbot.db`

---

## 📝 데이터 타입 참고

### TEXT 타입
- MySQL: `TEXT` 또는 `VARCHAR`
- SQLite: `TEXT`
- JSON 데이터는 TEXT로 저장 (파싱 필요)

### INTEGER 타입
- MySQL: `INT` 또는 `BIGINT`
- SQLite: `INTEGER`
- Boolean 값은 0/1로 저장

### REAL 타입
- MySQL: `DOUBLE` 또는 `DECIMAL`
- SQLite: `REAL`
- 금액, 비율 등 소수점 값

### TIMESTAMP 타입
- MySQL: `TIMESTAMP` 또는 `DATETIME`
- SQLite: `TEXT` (ISO 8601 형식: 'YYYY-MM-DD HH:MM:SS')

---

## ⚠️ 주의사항

1. **직접 DB 접근 금지**: 프론트엔드는 반드시 REST API를 통해서만 데이터 접근
2. **JSON 파싱**: `risk_settings`, `answers`, `market_snapshot` 등은 JSON 문자열로 저장되므로 파싱 필요
3. **타임스탬프 형식**: ISO 8601 형식 (`YYYY-MM-DDTHH:MM:SS`) 또는 Unix timestamp
4. **Boolean 값**: 데이터베이스에서는 0/1 (INTEGER)로 저장, API에서는 boolean으로 변환

---

**문서 버전**: 1.0.0  
**최종 업데이트**: 2025-11-29

