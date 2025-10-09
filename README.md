# BitBot

JavaFX GUI 애플리케이션 프로젝트

## 개발 환경

-   **Java Version**: 17
-   **JavaFX Version**: 21.0.1
-   **Build Tool**: Maven
-   **IDE**: IntelliJ IDEA / Eclipse / VS Code

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
│   │       └── fxml/
│   │           └── main.fxml                       # 메인 화면 레이아웃
│   └── test/
│       └── java/
├── pom.xml                                         # Maven 설정 파일
└── README.md
```

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
