# BitBot - AI Bitcoin Trading Bot 🤖💰

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![JavaFX](https://img.shields.io/badge/JavaFX-21-blue.svg)](https://openjfx.io/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

**BitBot**은 LLM(Gemini)을 활용한 AI 기반 비트코인 자동매매 시스템입니다.

## 🎯 핵심 기능

- **AI-Driven Trading**: Gemini API를 활용한 지능형 매매 의사결정
- **XAI (설명 가능한 AI)**: Agent Feed를 통한 투명한 의사결정 과정 시각화
- **Risk Profiling**: 사용자 투자 성향 분석 및 맞춤형 전략 제공
- **Secure Architecture**: API 키 로컬 암호화(AES-256) 저장
- **Real-time Dashboard**: 실시간 차트, 지표, 뉴스 통합 대시보드

## 🏗️ 시스템 아키텍처

```
┌─────────────────────────────────────────────────────────┐
│                   JavaFX Desktop Client                  │
│  ┌────────────┐  ┌─────────────┐  ┌─────────────────┐  │
│  │ Navigation │  │  Dashboard  │  │   Agent Feed    │  │
│  │    Bar     │  │   (Charts)  │  │ (AI Decisions)  │  │
│  └────────────┘  └─────────────┘  └─────────────────┘  │
└─────────────────────────────────────────────────────────┘
                          ▲  │
                          │  ▼
┌─────────────────────────────────────────────────────────┐
│              Spring Boot Logging Server                  │
│        (Authentication, Profiles, Trade Logs)            │
└─────────────────────────────────────────────────────────┘
                          ▲  │
                          │  ▼
                    ┌──────────────┐
                    │  MySQL 8.0   │
                    └──────────────┘

External APIs:
- Binance API (Market Data & Trading)
- Gemini API (AI Analysis)
- CryptoPanic API (News)
```

## 📋 요구사항

- **Java**: JDK 17 이상
- **Maven**: 3.8.x 이상
- **서버**: Spring Boot 서버 실행 (별도 제공)
- **API Keys**:
  - Binance API Key & Secret
  - Gemini API Key

## 🚀 시작하기

### 1. 프로젝트 클론

```bash
git clone https://github.com/your-repo/bitbot.git
cd bitbot
```

### 2. 의존성 설치

```bash
mvn clean install
```

### 3. 애플리케이션 실행

```bash
mvn javafx:run
```

또는 IDE에서 `BitBotApplication.java` 실행

## 📂 프로젝트 구조

```
bitbot-client/
├── src/main/java/com/bitbot/client/
│   ├── BitBotApplication.java       # Main entry point
│   ├── ui/                           # UI Components
│   │   ├── navigation/               # Navigation bar
│   │   ├── dashboard/                # Dashboard views
│   │   ├── chart/                    # Custom CandleStickChart
│   │   └── feed/                     # Agent Feed components
│   ├── service/                      # Business logic
│   │   ├── api/                      # API clients
│   │   ├── trading/                  # Trading engine
│   │   ├── analysis/                 # Technical indicators
│   │   └── security/                 # Encryption
│   ├── model/                        # Data models
│   └── dto/                          # Data transfer objects
├── src/main/resources/
│   ├── styles/                       # CSS files
│   └── images/                       # Icons and images
├── docs/                             # Documentation
│   ├── PRD.md                        # Product Requirements
│   ├── API.md                        # API Documentation
│   └── LLM_INPUT_DATA.md            # LLM Integration Guide
└── pom.xml                           # Maven configuration
```

## 🔐 보안

- API 키는 **절대 코드에 하드코딩하지 않습니다**
- 로컬에 AES-256-GCM으로 암호화하여 저장
- 서버에는 API 키를 전송하지 않음
- 사용자 비밀번호 기반 PBKDF2 키 유도

## 📊 투자 성향 프로필

| 유형 | 점수 | 특징 | 손절 기준 |
|------|------|------|-----------|
| 안정 추구형 | 12-24 | 분할 매수, 레버리지 금지 | -15% |
| 위험 중립형 | 25-33 | 추세 추종 | -7% |
| 적극 투자형 | 34-42 | 볼린저 밴드, 레버리지 3x | -5% |
| 전문 투기형 | 43-48 | 변동성 돌파, 고배율 | -3% |

## 🛠️ 개발 로드맵

- [x] Phase 1: GUI Foundation
  - [x] JavaFX 프로젝트 세팅
  - [ ] Main Layout 구성
  - [ ] Custom CandleStickChart
- [ ] Phase 2: Data & Security
  - [ ] Binance API 연동
  - [ ] AES-256 암호화
- [ ] Phase 3: AI Brain & Logic
  - [ ] 기술적 지표 계산
  - [ ] Gemini API 연동
  - [ ] 3-Way Decision Logic
- [ ] Phase 4: Backend Integration
  - [ ] 서버 API 연동
  - [ ] 로그 전송
- [ ] Phase 5: Final Polish
  - [ ] Agent Feed UI
  - [ ] 통합 테스트

## 📝 API 서버

API 서버는 별도 레포지토리에서 관리됩니다.

- **Base URL**: `http://203.234.62.223:8080/api`
- **문서**: [docs/API.md](docs/API.md)

## 🤝 기여

자바프로그래밍심화 전공 텀프로젝트

## 📄 라이선스

이 프로젝트는 교육 목적으로만 사용됩니다.

## ⚠️ 면책 조항

이 소프트웨어는 교육 목적으로 제작되었습니다. 실제 투자에 사용할 경우 발생하는 모든 손실에 대해 개발자는 책임지지 않습니다.

---

Made with ❤️ by BitBot Team

