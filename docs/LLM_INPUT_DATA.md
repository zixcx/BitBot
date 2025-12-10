## 1. LLM Input Data (프롬프트에 주입할 JSON)

LLM에게 보낼 데이터는 **"Context(맥락) + Market(시장) + Technical(기술) + News(정보)"** 4가지 영역으로 구성됩니다.

### 1.1. Context & User (사용자 및 계좌 상태)

- **목적:** 사용자의 성향과 자금 상황에 맞는 조언 유도.

```json
"user_context": {
  "risk_profile": "AGGRESSIVE", // 안정형, 중립형, 적극형, 투기형
  "strategy_rules": "Leverage 3x Max, Stop-Loss -5%", // 성향별 규칙 명시
  "wallet": {
    "balance_usdt": 1250.00,       // 가용 현금
    "holding_btc": 0.05,           // 보유 코인 수량
    "avg_buy_price": 68000.00,     // 평단가 (보유 시)
    "unrealized_pnl_pct": -1.25    // 현재 수익률 (보유 시)
  }
}

```

### 1.2. Market Status (시장 현황)

- **목적:** 현재가와 호가창 유동성 파악.

```json
"market_status": {
  "symbol": "BTCUSDT",
  "current_price": 69100.50,
  "fear_greed_index": 72, // 0~100 (탐욕 단계)
  "order_book_snapshot": {
    "bid_strength": "STRONG", // 매수 벽이 두터움 (Java에서 호가창 분석 후 요약해서 전달 추천)
    "ask_strength": "WEAK"    // 매도 벽이 얇음
  }
}

```

### 1.3. Technical Data (기술적 분석)

- **목적:** **50개 캔들로 파악하기 힘든 장기 추세**는 Java가 미리 계산해서 전달.

```json
"technical_indicators": {
  "trend_summary": "UP_TREND", // MA 정배열 여부 등
  "RSI_14": 68.5,
  "MACD": { "value": 150.2, "signal": 140.5, "status": "GOLDEN_CROSS" },
  "Bollinger_Bands": { "upper": 70000, "middle": 68500, "lower": 67000, "pct_b": 0.8 },
  "MA_Lines": { "MA20": 68800, "MA60": 67500, "MA120": 65000 }
}

```

### 1.4. Candle History (캔들 데이터) - **[50개 확정]**

- **목적:** 캔들 패턴(망치형, 장악형 등) 및 단기 변동성 분석.
- **형식:** 최신순(내림차순) 또는 시간순(오름차순) 통일 필수.

```json
"candles_recent_50": [
  { "time": "14:45", "o": 69000, "h": 69150, "l": 68950, "c": 69100, "v": 150.5 },
  { "time": "14:30", "o": 68950, "h": 69050, "l": 68900, "c": 69000, "v": 98.2 },
  // ... 총 50개 데이터 ...
]

```

### 1.5. News Data (뉴스) - **[팀 상의 필요]**

팀 회의 시 아래 기준을 참고하여 소스를 결정하세요.

- **소스 후보:**
    1. **CryptoPanic API:** 암호화폐 전용 뉴스 애그리게이터 (가장 추천, API 제공).
    2. **Google News (via Firecrawl):** "Bitcoin price", "Crypto regulation" 키워드 크롤링.
    3. **Binance News API:** 거래소 내부 뉴스 (영어 데이터 품질 좋음).
- **데이터 구조:**

```json
"news_summary": [
  { "source": "CoinDesk", "title": "BTC ETF 유입량 역대 최고치 경신", "sentiment": "POSITIVE" },
  { "source": "Reuters", "title": "연준 금리 인하 가능성 시사", "sentiment": "NEUTRAL" }
]

```

---

## 2. DB 저장용 로그 설계 (Output & Logs)

MySQL `trade_logs` 테이블에 저장될 상세 내용입니다.

### 2.1. 요청 및 응답 로그 (AI Interaction)

- `request_timestamp`: API 호출 시각.
- `prompt_tokens`: 사용된 입력 토큰 수 (비용 관리용).
- `response_tokens`: 생성된 출력 토큰 수.
- `ai_raw_response`: Gemini가 뱉은 JSON 원본 (디버깅용).
- `ai_reasoning_summary`: 사용자에게 보여줄 한 줄 요약 (예: "RSI 과열 및 매도 벽 저항으로 인해 관망").

### 2.2. 실행 결과 로그 (Execution)

- `decision`: `BUY` / `SELL` / `HOLD`.
- `target_price`: AI가 제안한 진입/청산 목표가.
- `executed_price`: 실제 체결된 가격 (슬리피지 확인용).
- `execution_latency`: 분석 완료부터 체결까지 걸린 시간(ms).

---

### 💡 개발 팁

- **캔들 개수 최적화:** 50개 데이터를 JSON으로 변환할 때, `Open`, `High`, `Low` 등의 키 값을 `o`, `h`, `l` 처럼 줄이면(Minify) 토큰을 약 20~30% 절약할 수 있습니다.
- **뉴스:** 팀 회의에서 "어디서 가져오냐"도 중요하지만, **"얼마나 최신이냐(Latency)"**가 더 중요합니다. 1시간 전 뉴스는 코인 시장에서 이미 낡은 정보일 수 있으니, **'최근 1시간 이내'** 필터링이 가능한 소스를 선택하시길 권장합니다.