# BitBot Client - 실행 가이드

## 필수 요구사항

1. **Java 17 이상** 설치
2. **Maven 3.8.x 이상** 설치
3. **API 키 준비**:
   - Binance API Key & Secret (테스트넷 권장)
   - Gemini API Key

## 프로젝트 빌드

```bash
# 의존성 다운로드 및 빌드
mvn clean install

# 빌드 건너뛰고 실행
mvn javafx:run
```

## 애플리케이션 실행

### 방법 1: Maven을 통한 실행

```bash
mvn javafx:run
```

### 방법 2: IDE에서 실행

1. IntelliJ IDEA / Eclipse에서 프로젝트 열기
2. `BitBotApplication.java` 파일을 찾아서 실행 (Main Class)
3. JavaFX 모듈이 자동으로 로드됩니다

## 초기 설정

1. **애플리케이션 시작 시:**
   - 자동으로 Binance API에 연결 (시세 데이터 수신)
   - 실시간 차트가 표시됩니다

2. **API 키 설정 (Settings 메뉴):**
   - Settings 탭 클릭
   - Binance API Key 및 Secret 입력
   - Gemini API Key 입력
   - 비밀번호 설정 (API 키 암호화에 사용)
   - 저장 버튼 클릭

3. **투자 성향 설정 (최초 1회):**
   - 15문항 설문조사 진행
   - 자동으로 위험 프로필 생성

## 기능 사용법

### 1. 대시보드 (Dashboard)
- 실시간 BTC/USDT 가격 확인
- 캔들스틱 차트 (1시간봉 기준)
- 기술적 지표 (RSI, MACD, Bollinger Bands)

### 2. 자동 매매 시작
- Auto Trade 버튼 클릭
- AI가 주기적으로 시장 분석 수행 (기본 60초 간격)
- 매수/매도/관망 의사결정 자동 실행

### 3. Agent Feed
- AI의 의사결정 내역 실시간 확인
- 카드 클릭 시 상세 분석 내용 확인
- 가격 목표, 손절가, 익절가 표시

### 4. 로그 확인 (Journal)
- 과거 매매 이력 조회
- 수익률 및 통계 확인

## 데이터 저장 위치

- **API 키 (암호화):** `~/.bitbot/config.dat`
- **로그 파일:** `logs/bitbot.log`

**⚠️ 중요:** 
- `~/.bitbot/config.dat` 파일은 **절대 공유하지 마세요**
- 암호화되어 있지만, 비밀번호를 알면 복호화 가능합니다

## 서버 연결

기본 서버 URL: `http://203.234.62.223:8080/api`

- 로그인 후 자동으로 연결됩니다
- 모든 매매 로그가 서버에 저장됩니다
- 투자 성향 프로필도 서버에 동기화됩니다

## 문제 해결

### JavaFX 모듈 에러
```
Error: JavaFX runtime components are missing
```
**해결:** Maven을 통해 실행하면 자동으로 해결됩니다.
```bash
mvn javafx:run
```

### Binance API 연결 실패
- 인터넷 연결 확인
- 방화벽 설정 확인
- API 키 유효성 확인

### Gemini API 에러
- API 키 유효성 확인
- API 사용량 한도 확인 (무료: 분당 15회)

### 로그 확인
```bash
tail -f logs/bitbot.log
```

## 개발 모드

### 디버그 로그 활성화
`src/main/resources/logback.xml` 파일에서:
```xml
<logger name="com.bitbot" level="DEBUG" />
```

### 테스트 실행
```bash
mvn test
```

## 주의사항

1. **실거래 주의:**
   - 현재는 Binance Spot API를 사용합니다
   - 실제 자금이 사용되므로 주의하세요
   - 테스트넷 사용을 권장합니다 (코드 수정 필요)

2. **API 키 보안:**
   - API 키는 절대 코드에 포함하지 마세요
   - 로컬에 암호화되어 저장됩니다
   - 서버에는 전송되지 않습니다

3. **투자 책임:**
   - 이 소프트웨어는 교육 목적입니다
   - 실제 투자로 인한 손실에 대해 개발자는 책임지지 않습니다

## 라이선스

교육 목적 프로젝트 - 자바프로그래밍심화 전공 텀프로젝트

---

**문의:** [GitHub Issues](https://github.com/your-repo/bitbot/issues)


