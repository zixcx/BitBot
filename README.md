# BitBot

JavaFX 기반 비트코인 자동매매 프로그램입니다.

## 개발 환경

- **Java Version**: 17
- **JavaFX Version**: 21.0.1
- **Build Tool**: Maven
- **IDE**: IntelliJ IDEA / Eclipse / VS Code
- **Font**: Geist Sans, Geist Mono (전역 적용)

## 프로젝트 구조

```
BitBot/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── bitbot/
│   │   │           ├── App.java                    # 메인 애플리케이션 클래스
│   │   │           └── controller/
│   │   │               ├── MainController.java     # 메인 화면 컨트롤러
│   │   │               ├── SecondController.java   # 두 번째 화면 컨트롤러
│   │   │               ├── DashboardController.java # 대시보드 컨트롤러
│   │   │               ├── TradingHistoryController.java # 거래 내역 컨트롤러
│   │   │               └── SettingsController.java # 설정 컨트롤러
│   │   └── resources/
│   │       ├── css/
│   │       │   └── styles.css                      # 전역 스타일시트
│   │       ├── fonts/
│   │       │   └── GeistMono-*.ttf                 # Geist Mono 폰트
│   │       └── fxml/
│   │           ├── main.fxml                       # 메인 화면 레이아웃
│   │           ├── second.fxml                     # 두 번째 화면 레이아웃
│   │           ├── dashboard.fxml                  # 대시보드 화면
│   │           ├── trading-history.fxml            # 거래 내역 화면
│   │           └── settings.fxml                   # 설정 화면
├── backend/
│   └── data/
│       └── bitbot.db                               # SQLite 데이터베이스
├── pom.xml                                         # Maven 설정 파일
├── FONT_SETUP.md                                   # 폰트 설치 가이드
├── FONT_USAGE.md                                   # 폰트 사용 가이드
├── install-fonts.sh                                # 폰트 자동 설치 스크립트 (macOS/Linux)
├── install-fonts.ps1                               # 폰트 자동 설치 스크립트 (Windows)
└── README.md
```

## 폰트 설치 (최초 1회)

프로젝트를 처음 받았다면 Geist Mono 폰트를 설치해야 합니다.

### 자동 설치 스크립트

- macOS/Linux: `./install-fonts.sh`
- Windows: `.\install-fonts.ps1`

## 실행 방법

### Maven을 사용한 실행

```bash
# 프로젝트 빌드
mvn clean install

# 애플리케이션 실행
mvn javafx:run
```

### IDE에서 실행

1. 프로젝트를 IDE에서 열기
2. Maven 의존성 다운로드 (자동 또는 수동)
3. `App.java`의 `main` 메서드 실행

## 주요 기능

- 📊 대시보드: 실시간 거래 정보 및 계좌 상태 확인
- 📈 거래 내역: 과거 거래 내역 조회
- ⚙️ 설정: API 키 및 거래 설정 관리
- 🎨 현대적인 UI: Geist 폰트 기반의 깔끔한 인터페이스

## 개발 가이드

### 새로운 화면 추가

1. `src/main/resources/fxml/` 디렉토리에 FXML 파일 생성
2. `src/main/java/com/bitbot/controller/` 디렉토리에 컨트롤러 클래스 생성
3. FXML 파일과 컨트롤러 연결

### 의존성 추가

`pom.xml` 파일의 `<dependencies>` 섹션에 필요한 라이브러리 추가

## 상세 문서

- **빠른 시작**: [backend/QUICK_START.md](backend/QUICK_START.md)
- **폰트 설정**: [FONT_SETUP.md](FONT_SETUP.md)
- **폰트 사용**: [FONT_USAGE.md](FONT_USAGE.md)
