# 🧪 단계별 테스트 가이드

프로젝트를 처음부터 끝까지 단계별로 테스트하는 방법

## 📋 테스트 순서

### 1단계: 컴파일 확인
```bash
mvn clean compile
```
**확인 사항**: 컴파일 오류 없음

---

### 2단계: 데이터베이스 연결 테스트
```bash
.\run-mysql-test.bat
```
**확인 사항**: 
- ✅ 데이터베이스 연결 성공
- ✅ 스키마 초기화 완료

---

### 3단계: 기본 시스템 테스트
```bash
.\run-step-test.bat
```
**확인 사항**:
- ✅ 환경 설정 로드
- ✅ 데이터베이스 연결
- ✅ 스키마 확인 (users, user_profiles, questionnaires, trades, trade_logs)
- ✅ 프로필 조회
- ✅ 거래 내역 조회
- ✅ 거래 로그 조회

---

### 4단계: 설문조사 → 프로필 생성 테스트
```bash
.\run-cli.bat
```
**확인 사항**:
- 설문조사 진행 (y 입력)
- 15문항 답변
- 프로필 생성 확인
- 데이터베이스에 저장 확인

**수동 확인**:
```sql
-- MySQL 또는 SQLite에서 확인
SELECT * FROM user_profiles WHERE user_id = 1;
SELECT * FROM questionnaires WHERE user_id = 1;
```

---

### 5단계: 거래 사이클 실행 테스트
```bash
.\run-cli.bat
```
**확인 사항**:
- 프로필이 있으면 자동으로 거래 사이클 실행
- 시장 데이터 수집
- LLM 분석
- 전략 적용
- 리스크 검증
- 주문 실행 (시뮬레이션)

---

### 6단계: 데이터베이스 저장 확인

**거래 내역 확인**:
```sql
SELECT * FROM trades ORDER BY created_at DESC LIMIT 10;
```

**거래 로그 확인 (HOLD 포함)**:
```sql
SELECT * FROM trade_logs ORDER BY created_at DESC LIMIT 10;
```

**확인 사항**:
- ✅ trades 테이블에 거래 내역 저장
- ✅ trade_logs 테이블에 모든 판단 기록 (HOLD 포함)
- ✅ executed_price, executed_qty 등 정상 저장

---

### 7단계: REST API 서버 테스트
```bash
.\run-server.bat
```

**별도 터미널에서 테스트**:
```bash
# 서버 상태 확인
curl http://localhost:8080/api/health

# 계좌 정보 조회
curl http://localhost:8080/api/account

# 거래 내역 조회
curl http://localhost:8080/api/trades?limit=10
```

---

## 🔍 각 단계별 상세 확인

### 데이터베이스 저장 확인 방법

#### MySQL
```bash
mysql -h 203.234.62.223 -u root -p bitbot
```

#### SQLite
```bash
sqlite3 data/bitbot.db
```

**확인 쿼리**:
```sql
-- 프로필 확인
SELECT * FROM user_profiles;

-- 거래 내역 확인
SELECT id, order_type, quantity, executed_price, profit_loss, created_at 
FROM trades 
ORDER BY created_at DESC 
LIMIT 5;

-- 거래 로그 확인 (HOLD 포함)
SELECT id, action_type, confidence_score, brief_reason, created_at 
FROM trade_logs 
ORDER BY created_at DESC 
LIMIT 10;

-- 거래 개수 확인
SELECT COUNT(*) as total_trades FROM trades;
SELECT COUNT(*) as total_logs FROM trade_logs;
```

---

## ⚠️ 문제 해결

### 컴파일 오류
- `mvn clean compile` 실행
- 오류 메시지 확인 후 수정

### 데이터베이스 연결 실패
- `.env` 파일의 DB 설정 확인
- MySQL 서버 실행 확인
- 방화벽 설정 확인

### 프로필 없음
- `.\run-cli.bat` 실행 후 설문조사 진행

### 거래 내역 없음
- 거래 사이클이 실행되었는지 확인
- 시뮬레이션 모드인지 확인 (`TRADING_MODE=SIMULATION`)

---

## ✅ 테스트 체크리스트

- [ ] 1단계: 컴파일 성공
- [ ] 2단계: 데이터베이스 연결 성공
- [ ] 3단계: 기본 시스템 테스트 통과
- [ ] 4단계: 설문조사 → 프로필 생성 성공
- [ ] 5단계: 거래 사이클 실행 성공
- [ ] 6단계: 데이터베이스 저장 확인
  - [ ] trades 테이블에 데이터 저장
  - [ ] trade_logs 테이블에 데이터 저장 (HOLD 포함)
- [ ] 7단계: REST API 서버 정상 작동

---

**테스트 완료 후**: 모든 체크리스트가 완료되면 시스템이 정상 작동하는 것입니다.

