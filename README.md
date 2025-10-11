# BitBot

JavaFX GUI 애플리케이션 프로젝트

## 개발 환경

-   **Java Version**: 17
-   **JavaFX Version**: 21.0.1
-   **Build Tool**: Maven
-   **IDE**: IntelliJ IDEA / Eclipse / VS Code
-   **Font**: Geist Sans, Geist Mono (전역 적용)

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
│   │   │               └── MainController.java     # 메인 화면 컨트롤러
│   │   └── resources/
│   │       ├── css/
│   │       │   └── styles.css                      # 전역 스타일시트 (Geist Mono 폰트 설정)
│   │       ├── fonts/
│   │       │   ├── Geist-Regular.ttf               # Geist Sans 일반 폰트
│   │       │   ├── Geist-Bold.ttf                  # Geist Sans 볼드 폰트
│   │       │   ├── Geist-Medium.ttf                # Geist Sans 미디엄 폰트
│   │       │   ├── Geist-Light.ttf                 # Geist Sans 라이트 폰트
│   │       │   ├── GeistMono-Regular.ttf           # Geist Mono 일반 폰트
│   │       │   ├── GeistMono-Bold.ttf              # Geist Mono 볼드 폰트
│   │       │   ├── GeistMono-Medium.ttf            # Geist Mono 미디엄 폰트
│   │       │   └── GeistMono-Light.ttf             # Geist Mono 라이트 폰트
│   │       └── fxml/
│   │           └── main.fxml                       # 메인 화면 레이아웃
│   └── test/
│       └── java/
├── pom.xml                                         # Maven 설정 파일
├── FONT_SETUP.md                                   # 폰트 설치 가이드
├── FONT_USAGE.md                                   # 폰트 사용 가이드
├── install-fonts.sh                                # 폰트 자동 설치 스크립트 (macOS/Linux)
├── install-fonts.ps1                               # 폰트 자동 설치 스크립트 (Windows)
└── README.md
```

## 폰트 설치 (최초 1회)

프로젝트를 처음 받았다면 Geist Mono 폰트를 설치해야 합니다.

### macOS / Linux

**자동 설치 스크립트:**

-   macOS/Linux: `./install-fonts.sh`
-   Windows: `.\install-fonts.ps1`

**상세 가이드:**

-   폰트 설치: [FONT_SETUP.md](FONT_SETUP.md)
-   폰트 사용법: [FONT_USAGE.md](FONT_USAGE.md)

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

## 개발 가이드

### 새로운 화면 추가

1. `src/main/resources/fxml/` 디렉토리에 FXML 파일 생성
2. `src/main/java/com/bitbot/controller/` 디렉토리에 컨트롤러 클래스 생성
3. FXML 파일과 컨트롤러 연결

### 의존성 추가

`pom.xml` 파일의 `<dependencies>` 섹션에 필요한 라이브러리 추가

## 빌드 및 배포

```bash
# JAR 파일 생성
mvn clean package

# 생성된 JAR 파일 위치
# target/bitbot-1.0.0.jar
```

## 라이선스

Copyright © 2025

## 참고 자료

-   [JavaFX Documentation](https://openjfx.io/)
-   [Maven Documentation](https://maven.apache.org/)
