-- ===================================
-- BitBot Trading System Database Schema
-- ===================================
-- SQLite 스키마

-- 사용자 테이블
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    email TEXT UNIQUE NOT NULL,
    username TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    
    -- API 키 (암호화 저장)
    binance_api_key_encrypted TEXT,
    binance_secret_key_encrypted TEXT,
    
    -- 설정
    trading_enabled INTEGER DEFAULT 0,
    risk_management_enabled INTEGER DEFAULT 1,
    max_investment_percent REAL DEFAULT 10.00,
    
    created_at TEXT DEFAULT (datetime('now')),
    updated_at TEXT DEFAULT (datetime('now'))
);

-- 사용자 프로필 테이블 (투자 성향)
CREATE TABLE IF NOT EXISTS user_profiles (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    
    investor_type TEXT NOT NULL,  -- CONSERVATIVE, MODERATE, AGGRESSIVE, SPECULATIVE
    total_score INTEGER NOT NULL,  -- 12~48
    risk_settings TEXT NOT NULL,   -- JSON 형식
    trading_strategy TEXT NOT NULL,
    
    created_at TEXT DEFAULT (datetime('now')),
    updated_at TEXT DEFAULT (datetime('now')),
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE(user_id)
);

-- 설문조사 응답 테이블
CREATE TABLE IF NOT EXISTS questionnaires (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    
    answers TEXT NOT NULL,  -- JSON 형식 (Q1~Q15)
    total_score INTEGER NOT NULL,
    result_type TEXT NOT NULL,
    
    completed_at TEXT DEFAULT (datetime('now')),
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 거래 내역 테이블
CREATE TABLE IF NOT EXISTS trades (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER,
    
    -- 거래 정보
    symbol TEXT NOT NULL DEFAULT 'BTCUSDT',
    order_type TEXT NOT NULL,  -- MARKET_BUY, MARKET_SELL, etc.
    order_status TEXT NOT NULL,  -- PENDING, FILLED, etc.
    
    -- 가격 및 수량
    quantity REAL NOT NULL,
    price REAL NOT NULL,
    executed_price REAL,
    total_cost REAL,
    
    -- 레버리지
    leverage INTEGER DEFAULT 1,  -- 레버리지 배수 (1 = 현물)
    is_futures_trade INTEGER DEFAULT 0,  -- 선물 거래 여부 (0 = 현물, 1 = 선물)
    
    -- 손익
    profit_loss REAL,
    profit_loss_percent REAL,
    
    -- 거래 근거
    decision_reason TEXT,
    agent_name TEXT,
    confidence REAL,
    
    -- 바이낸스 연동
    binance_order_id TEXT,
    
    -- 타임스탬프
    created_at TEXT DEFAULT (datetime('now')),
    executed_at TEXT,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_trades_user_id ON trades(user_id);
CREATE INDEX IF NOT EXISTS idx_trades_created_at ON trades(created_at);
CREATE INDEX IF NOT EXISTS idx_trades_symbol ON trades(symbol);

-- 거래 로그 테이블 (PRD 요구사항: HOLD 포함 모든 AI 판단 기록)
CREATE TABLE IF NOT EXISTS trade_logs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    
    -- 거래 정보
    symbol TEXT NOT NULL DEFAULT 'BTCUSDT',
    
    -- AI Decision (HOLD 포함)
    action_type TEXT NOT NULL,  -- 'BUY', 'SELL', 'HOLD'
    confidence_score REAL,  -- 0.0 ~ 1.0
    
    -- 근거 (요약/상세 구분)
    brief_reason TEXT,  -- 리스트용 요약 (한 줄)
    full_reason TEXT,  -- 상세 분석 내용
    
    -- 실행 상세 (HOLD시 NULL)
    executed_price REAL NULL,
    executed_qty REAL NULL,
    realized_pnl REAL NULL,  -- 매도 시 수익금
    
    -- 시장 스냅샷 (JSON 형식)
    market_snapshot TEXT,  -- {"rsi": 32, "ma20": 98000, "price": 69100.50}
    
    -- 에이전트 정보
    agent_name TEXT,
    
    -- 타임스탬프
    created_at TEXT DEFAULT (datetime('now')),
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_trade_logs_user_id ON trade_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_trade_logs_created_at ON trade_logs(created_at);
CREATE INDEX IF NOT EXISTS idx_trade_logs_action_type ON trade_logs(action_type);
CREATE INDEX IF NOT EXISTS idx_trade_logs_symbol ON trade_logs(symbol);

-- LLM 에이전트 분석 로그 테이블
CREATE TABLE IF NOT EXISTS llm_analysis_logs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER,
    
    -- 에이전트 정보
    agent_name TEXT NOT NULL,  -- Technical, OnChain, Sentiment, Coordinator
    
    -- 프롬프트 및 응답
    request_prompt TEXT NOT NULL,
    response_raw TEXT NOT NULL,
    response_parsed TEXT,  -- JSON 형식
    
    -- 분석 결과
    decision TEXT,  -- BUY, SELL, HOLD
    confidence REAL,
    reason TEXT,
    
    -- 시장 데이터 스냅샷 (JSON)
    market_data_snapshot TEXT,
    
    -- API 사용 정보
    llm_provider TEXT DEFAULT 'gemini',
    tokens_used INTEGER,
    response_time_ms INTEGER,
    
    -- 실행 결과
    action_taken TEXT,
    trade_id INTEGER,
    
    created_at TEXT DEFAULT (datetime('now')),
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (trade_id) REFERENCES trades(id) ON DELETE SET NULL
);

-- 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_llm_logs_user_id ON llm_analysis_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_llm_logs_agent ON llm_analysis_logs(agent_name);
CREATE INDEX IF NOT EXISTS idx_llm_logs_created_at ON llm_analysis_logs(created_at);

-- 포트폴리오 상태 스냅샷 테이블
CREATE TABLE IF NOT EXISTS portfolio_snapshots (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    
    -- 잔고 정보
    total_balance REAL NOT NULL,
    available_balance REAL NOT NULL,
    invested_amount REAL NOT NULL,
    
    -- 보유 자산
    btc_holding REAL NOT NULL DEFAULT 0,
    btc_value REAL NOT NULL DEFAULT 0,
    
    -- 손익
    total_profit_loss REAL NOT NULL DEFAULT 0,
    profit_loss_percent REAL NOT NULL DEFAULT 0,
    
    -- 통계
    total_trades INTEGER NOT NULL DEFAULT 0,
    winning_trades INTEGER NOT NULL DEFAULT 0,
    losing_trades INTEGER NOT NULL DEFAULT 0,
    
    created_at TEXT DEFAULT (datetime('now')),
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_portfolio_user_id ON portfolio_snapshots(user_id);
CREATE INDEX IF NOT EXISTS idx_portfolio_created_at ON portfolio_snapshots(created_at);

-- 시장 데이터 캐시 테이블 (선택적)
CREATE TABLE IF NOT EXISTS market_data_cache (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    symbol TEXT NOT NULL DEFAULT 'BTCUSDT',
    
    -- OHLCV
    timestamp TEXT NOT NULL,
    open_price REAL NOT NULL,
    high_price REAL NOT NULL,
    low_price REAL NOT NULL,
    close_price REAL NOT NULL,
    volume REAL NOT NULL,
    
    -- 기술 지표
    rsi REAL,
    macd REAL,
    macd_signal REAL,
    ma_short REAL,
    ma_long REAL,
    bollinger_upper REAL,
    bollinger_middle REAL,
    bollinger_lower REAL,
    
    created_at TEXT DEFAULT (datetime('now')),
    
    UNIQUE(symbol, timestamp)
);

-- 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_market_data_symbol_timestamp ON market_data_cache(symbol, timestamp);

-- 시스템 이벤트 로그 테이블
CREATE TABLE IF NOT EXISTS system_events (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    event_type TEXT NOT NULL,  -- INFO, WARNING, ERROR, TRADE, ANALYSIS
    event_message TEXT NOT NULL,
    event_details TEXT,  -- JSON 형식
    
    user_id INTEGER,
    
    created_at TEXT DEFAULT (datetime('now')),
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_events_type ON system_events(event_type);
CREATE INDEX IF NOT EXISTS idx_events_created_at ON system_events(created_at);

-- updated_at 자동 갱신 트리거
CREATE TRIGGER IF NOT EXISTS update_users_updated_at
    AFTER UPDATE ON users
    FOR EACH ROW
    BEGIN
        UPDATE users SET updated_at = datetime('now') WHERE id = NEW.id;
    END;

CREATE TRIGGER IF NOT EXISTS update_user_profiles_updated_at
    AFTER UPDATE ON user_profiles
    FOR EACH ROW
    BEGIN
        UPDATE user_profiles SET updated_at = datetime('now') WHERE id = NEW.id;
    END;
