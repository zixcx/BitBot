# 🎨 BitBot 프론트엔드 개발 가이드

프론트엔드 개발자를 위한 완전한 개발 가이드

## 📋 목차

1. [개발 환경 설정](#1-개발-환경-설정)
2. [프로젝트 구조 제안](#2-프로젝트-구조-제안)
3. [인증 플로우 상세](#3-인증-플로우-상세)
4. [API 클라이언트 설정](#4-api-클라이언트-설정)
5. [주요 기능 개발 가이드](#5-주요-기능-개발-가이드)
6. [UI/UX 가이드라인](#6-uiux-가이드라인)
7. [상태 관리](#7-상태-관리)
8. [실시간 데이터 업데이트](#8-실시간-데이터-업데이트)
9. [에러 처리](#9-에러-처리)
10. [개발 순서 및 체크리스트](#10-개발-순서-및-체크리스트)
11. [테스트 방법](#11-테스트-방법)
12. [배포 가이드](#12-배포-가이드)

---

## 1. 개발 환경 설정

### 1.1 필수 도구

- **Node.js**: 18.x 이상
- **npm** 또는 **yarn**: 패키지 관리자
- **TypeScript**: 타입 안정성 (권장)
- **React** / **Vue** / **Angular**: 프레임워크 선택
- **VS Code** (권장): 개발 도구

### 1.2 프로젝트 초기화

#### React + TypeScript 예제
```bash
# Create React App
npx create-react-app bitbot-frontend --template typescript
cd bitbot-frontend

# 또는 Vite (더 빠름)
npm create vite@latest bitbot-frontend -- --template react-ts
cd bitbot-frontend
npm install
```

#### 필수 패키지 설치
```bash
# HTTP 클라이언트
npm install axios

# 상태 관리 (선택)
npm install zustand  # 또는 redux, recoil 등

# 라우팅
npm install react-router-dom

# 차트 라이브러리
npm install recharts  # 또는 chart.js, tradingview-lightweight-charts

# 날짜 처리
npm install date-fns

# 폼 관리
npm install react-hook-form
```

### 1.3 환경 변수 설정

프로젝트 루트에 `.env` 파일 생성:
```env
REACT_APP_API_BASE_URL=http://localhost:8080/api
REACT_APP_WS_URL=ws://localhost:8080/ws  # WebSocket (향후 구현)
```

---

## 2. 프로젝트 구조 제안

```
bitbot-frontend/
├── src/
│   ├── api/                    # API 클라이언트
│   │   ├── client.ts          # Axios 인스턴스
│   │   ├── auth.ts            # 인증 API
│   │   ├── trading.ts         # 거래 API
│   │   ├── market.ts          # 시장 데이터 API
│   │   └── types.ts           # API 타입 정의
│   ├── components/            # 재사용 가능한 컴포넌트
│   │   ├── common/           # 공통 컴포넌트
│   │   │   ├── Button.tsx
│   │   │   ├── Input.tsx
│   │   │   ├── Card.tsx
│   │   │   └── Loading.tsx
│   │   ├── chart/            # 차트 컴포넌트
│   │   │   ├── PriceChart.tsx
│   │   │   └── IndicatorChart.tsx
│   │   └── layout/           # 레이아웃 컴포넌트
│   │       ├── Header.tsx
│   │       ├── Sidebar.tsx
│   │       └── Layout.tsx
│   ├── pages/                # 페이지 컴포넌트
│   │   ├── Login.tsx
│   │   ├── Register.tsx
│   │   ├── Dashboard.tsx
│   │   ├── Questionnaire.tsx
│   │   ├── Trading.tsx
│   │   └── Settings.tsx
│   ├── hooks/                # 커스텀 훅
│   │   ├── useAuth.ts
│   │   ├── useTrading.ts
│   │   ├── useMarketData.ts
│   │   └── useWebSocket.ts
│   ├── store/                # 상태 관리
│   │   ├── authStore.ts
│   │   ├── tradingStore.ts
│   │   └── marketStore.ts
│   ├── utils/                # 유틸리티
│   │   ├── formatters.ts
│   │   ├── validators.ts
│   │   └── constants.ts
│   ├── types/                # TypeScript 타입
│   │   ├── api.ts
│   │   ├── trading.ts
│   │   └── user.ts
│   └── App.tsx
├── public/
└── package.json
```

---

## 3. 인증 플로우 상세

### 3.1 전체 인증 플로우

```
1. 사용자 회원가입
   POST /api/auth/register
   ↓
2. 로그인
   POST /api/auth/login
   → sessionToken 받기
   ↓
3. sessionToken을 localStorage에 저장
   ↓
4. 모든 API 요청에 Authorization 헤더 추가
   Authorization: Bearer {sessionToken}
   ↓
5. 세션 만료 시 자동 로그아웃
   GET /api/auth/verify (주기적 확인)
```

### 3.2 인증 상태 관리

#### useAuth 훅 예제
```typescript
// hooks/useAuth.ts
import { useState, useEffect } from 'react';
import { authApi } from '../api/auth';
import { useNavigate } from 'react-router-dom';

export const useAuth = () => {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    // 앱 시작 시 세션 확인
    checkSession();
  }, []);

  const checkSession = async () => {
    const token = localStorage.getItem('sessionToken');
    if (!token) {
      setLoading(false);
      return;
    }

    try {
      const response = await authApi.verify(token);
      if (response.data.valid) {
        // 세션 유효 → 사용자 정보 로드
        await loadUser();
      } else {
        // 세션 만료 → 로그아웃
        logout();
      }
    } catch (error) {
      logout();
    } finally {
      setLoading(false);
    }
  };

  const login = async (email: string, password: string) => {
    const response = await authApi.login(email, password);
    if (response.success && response.data) {
      localStorage.setItem('sessionToken', response.data.sessionToken);
      await loadUser();
      navigate('/dashboard');
    }
    return response;
  };

  const logout = () => {
    localStorage.removeItem('sessionToken');
    setUser(null);
    navigate('/login');
  };

  const loadUser = async () => {
    // 프로필 정보 로드
    const profile = await tradingApi.getProfile();
    setUser(profile);
  };

  return { user, loading, login, logout, checkSession };
};
```

### 3.3 Protected Route 구현

```typescript
// components/ProtectedRoute.tsx
import { Navigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

export const ProtectedRoute = ({ children }: { children: React.ReactNode }) => {
  const { user, loading } = useAuth();

  if (loading) {
    return <div>Loading...</div>;
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  return <>{children}</>;
};
```

---

## 4. API 클라이언트 설정

### 4.1 Axios 인스턴스 설정

```typescript
// api/client.ts
import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080/api';

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 요청 인터셉터: 토큰 자동 추가
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('sessionToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 응답 인터셉터: 에러 처리
apiClient.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    if (error.response?.status === 401) {
      // 인증 오류 → 로그아웃
      localStorage.removeItem('sessionToken');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);
```

### 4.2 API 함수 정의

```typescript
// api/auth.ts
import { apiClient } from './client';

export const authApi = {
  register: async (data: RegisterRequest) => {
    const response = await apiClient.post('/auth/register', data);
    return response.data;
  },

  login: async (email: string, password: string) => {
    const response = await apiClient.post('/auth/login', { email, password });
    return response.data;
  },

  logout: async () => {
    const response = await apiClient.post('/auth/logout');
    return response.data;
  },

  verify: async (token: string) => {
    const response = await apiClient.get('/auth/verify', {
      headers: { Authorization: `Bearer ${token}` },
    });
    return response.data;
  },
};
```

```typescript
// api/trading.ts
import { apiClient } from './client';

export const tradingApi = {
  getTrades: async (limit: number = 50) => {
    const response = await apiClient.get('/trades', { params: { limit } });
    return response.data;
  },

  getAccount: async () => {
    const response = await apiClient.get('/account');
    return response.data;
  },

  getProfile: async () => {
    const response = await apiClient.get('/profile');
    return response.data;
  },

  getTradeLogs: async (limit: number = 50) => {
    const response = await apiClient.get('/trade-logs', { params: { limit } });
    return response.data;
  },

  startTrading: async () => {
    const response = await apiClient.post('/trading/start');
    return response.data;
  },

  stopTrading: async () => {
    const response = await apiClient.post('/trading/stop');
    return response.data;
  },

  getTradingStatus: async () => {
    const response = await apiClient.get('/trading/status');
    return response.data;
  },
};
```

---

## 5. 주요 기능 개발 가이드

### 5.1 설문조사 페이지

**15문항 설문조사 구현**

```typescript
// pages/Questionnaire.tsx
import { useState } from 'react';
import { questionnaireApi } from '../api/questionnaire';

const QUESTIONS = [
  { id: 'q1', text: '귀하의 연령대는?', options: [...] },
  { id: 'q2', text: '투자 경험은?', options: [...] },
  // ... 15개 질문
];

export const Questionnaire = () => {
  const [answers, setAnswers] = useState<Record<string, number>>({});
  const [currentStep, setCurrentStep] = useState(0);

  const handleSubmit = async () => {
    const response = await questionnaireApi.submit(answers);
    if (response.success) {
      // 프로필 생성 완료 → 대시보드로 이동
      navigate('/dashboard');
    }
  };

  return (
    <div>
      {/* 질문 표시 */}
      {/* 답변 선택 */}
      {/* 진행률 표시 */}
      {/* 제출 버튼 */}
    </div>
  );
};
```

**주의사항**:
- 모든 질문에 답변해야 제출 가능
- 답변 값: 1~4 (점수)
- 제출 후 프로필 자동 생성

### 5.2 대시보드 페이지

**필수 표시 정보**:
1. 계좌 정보 (총 잔고, BTC 보유량, 손익)
2. 현재 가격 (실시간 업데이트)
3. 자동 거래 상태 (실행 중/중지)
4. 최근 거래 내역
5. 통계 (승률, 총 수익 등)

```typescript
// pages/Dashboard.tsx
import { useEffect, useState } from 'react';
import { tradingApi, marketApi } from '../api';

export const Dashboard = () => {
  const [account, setAccount] = useState<AccountInfo | null>(null);
  const [trades, setTrades] = useState<TradeOrder[]>([]);
  const [currentPrice, setCurrentPrice] = useState<number>(0);
  const [tradingStatus, setTradingStatus] = useState<TradingStatus | null>(null);

  useEffect(() => {
    loadDashboardData();
    // 5초마다 계좌 정보 업데이트
    const interval = setInterval(loadDashboardData, 5000);
    return () => clearInterval(interval);
  }, []);

  const loadDashboardData = async () => {
    const [accountRes, tradesRes, priceRes, statusRes] = await Promise.all([
      tradingApi.getAccount(),
      tradingApi.getTrades(10),
      marketApi.getPrice(),
      tradingApi.getTradingStatus(),
    ]);

    setAccount(accountRes.data);
    setTrades(tradesRes.data);
    setCurrentPrice(priceRes.data.price);
    setTradingStatus(statusRes.data);
  };

  return (
    <div>
      {/* 계좌 정보 카드 */}
      {/* 현재 가격 */}
      {/* 자동 거래 제어 버튼 */}
      {/* 최근 거래 내역 테이블 */}
      {/* 통계 차트 */}
    </div>
  );
};
```

### 5.3 차트 시각화

**OHLCV 차트 구현**

```typescript
// components/chart/PriceChart.tsx
import { useEffect, useState } from 'react';
import { LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer } from 'recharts';
import { marketApi } from '../../api/market';

export const PriceChart = ({ timeframe = '1h' }: { timeframe?: string }) => {
  const [data, setData] = useState<MarketData[]>([]);

  useEffect(() => {
    loadChartData();
    // 1분마다 차트 데이터 업데이트
    const interval = setInterval(loadChartData, 60000);
    return () => clearInterval(interval);
  }, [timeframe]);

  const loadChartData = async () => {
    const response = await marketApi.getChart({ timeframe, limit: 100 });
    setData(response.data);
  };

  return (
    <ResponsiveContainer width="100%" height={400}>
      <LineChart data={data}>
        <XAxis dataKey="timestamp" />
        <YAxis />
        <Tooltip />
        <Line type="monotone" dataKey="close" stroke="#8884d8" />
      </LineChart>
    </ResponsiveContainer>
  );
};
```

**기술 지표 표시**:
- RSI: 30 이하 (과매도), 70 이상 (과매수)
- MACD: 양수/음수로 추세 표시
- 이동평균: 골든크로스/데드크로스 표시
- 볼린저밴드: 상단/하단 터치 표시

### 5.4 거래 내역 페이지

**필수 기능**:
- 거래 내역 테이블 (필터링, 정렬)
- 거래 로그 (HOLD 포함 모든 AI 판단)
- 상세 정보 모달

```typescript
// pages/TradingHistory.tsx
export const TradingHistory = () => {
  const [trades, setTrades] = useState<TradeOrder[]>([]);
  const [tradeLogs, setTradeLogs] = useState<TradeLog[]>([]);
  const [filter, setFilter] = useState<'all' | 'buy' | 'sell'>('all');

  const filteredTrades = trades.filter(trade => {
    if (filter === 'all') return true;
    return filter === 'buy' ? trade.isBuyOrder() : !trade.isBuyOrder();
  });

  return (
    <div>
      {/* 필터 버튼 */}
      {/* 거래 내역 테이블 */}
      {/* 거래 로그 탭 */}
    </div>
  );
};
```

### 5.5 자동 거래 제어

**시작/중지 버튼**

```typescript
// components/TradingControl.tsx
export const TradingControl = () => {
  const [status, setStatus] = useState<TradingStatus | null>(null);
  const [loading, setLoading] = useState(false);

  const handleStart = async () => {
    setLoading(true);
    try {
      const response = await tradingApi.startTrading();
      if (response.success) {
        setStatus(response.data);
        // 성공 알림
      }
    } catch (error) {
      // 에러 알림
    } finally {
      setLoading(false);
    }
  };

  const handleStop = async () => {
    // 확인 다이얼로그
    if (window.confirm('자동 거래를 중지하시겠습니까?')) {
      await tradingApi.stopTrading();
    }
  };

  return (
    <div>
      {status?.running ? (
        <button onClick={handleStop}>자동 거래 중지</button>
      ) : (
        <button onClick={handleStart}>자동 거래 시작</button>
      )}
      <div>전략: {status?.strategy}</div>
      <div>실행 주기: {status?.intervalMinutes}분</div>
    </div>
  );
};
```

---

## 6. UI/UX 가이드라인

### 6.1 색상 팔레트

**거래 상태별 색상**:
- **매수 (BUY)**: 초록색 (#10B981)
- **매도 (SELL)**: 빨간색 (#EF4444)
- **관망 (HOLD)**: 회색 (#6B7280)
- **수익**: 초록색 (#10B981)
- **손실**: 빨간색 (#EF4444)

**알림 레벨별 색상**:
- **CRITICAL**: 빨간색 (#DC2626)
- **ERROR**: 주황색 (#F59E0B)
- **WARNING**: 노란색 (#FCD34D)
- **INFO**: 파란색 (#3B82F6)

### 6.2 투자 성향별 표시

**투자 성향 뱃지**:
- **CONSERVATIVE (안정 추구형)**: 파란색
- **MODERATE (위험 중립형)**: 초록색
- **AGGRESSIVE (적극 투자형)**: 주황색
- **SPECULATIVE (전문 투기형)**: 빨간색

### 6.3 숫자 포맷팅

```typescript
// utils/formatters.ts
export const formatCurrency = (value: number): string => {
  return new Intl.NumberFormat('ko-KR', {
    style: 'currency',
    currency: 'USD',
    minimumFractionDigits: 2,
  }).format(value);
};

export const formatBTC = (value: number): string => {
  return `${value.toFixed(6)} BTC`;
};

export const formatPercent = (value: number): string => {
  const sign = value >= 0 ? '+' : '';
  return `${sign}${value.toFixed(2)}%`;
};

export const formatDate = (date: string): string => {
  return new Date(date).toLocaleString('ko-KR');
};
```

### 6.4 로딩 상태 표시

모든 API 호출에 로딩 상태 표시:
```typescript
const [loading, setLoading] = useState(false);

const fetchData = async () => {
  setLoading(true);
  try {
    const data = await api.getData();
    // ...
  } finally {
    setLoading(false);
  }
};
```

### 6.5 에러 메시지 표시

사용자 친화적인 에러 메시지:
```typescript
const getErrorMessage = (error: ApiError): string => {
  if (error.message.includes('이메일이 이미 존재')) {
    return '이미 사용 중인 이메일입니다.';
  }
  if (error.message.includes('비밀번호')) {
    return '비밀번호가 올바르지 않습니다.';
  }
  return '오류가 발생했습니다. 다시 시도해주세요.';
};
```

---

## 7. 상태 관리

### 7.1 Zustand 예제

```typescript
// store/authStore.ts
import create from 'zustand';

interface AuthState {
  user: User | null;
  token: string | null;
  setUser: (user: User) => void;
  setToken: (token: string) => void;
  logout: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  token: localStorage.getItem('sessionToken'),
  setUser: (user) => set({ user }),
  setToken: (token) => {
    localStorage.setItem('sessionToken', token);
    set({ token });
  },
  logout: () => {
    localStorage.removeItem('sessionToken');
    set({ user: null, token: null });
  },
}));
```

### 7.2 거래 상태 관리

```typescript
// store/tradingStore.ts
import create from 'zustand';

interface TradingState {
  account: AccountInfo | null;
  trades: TradeOrder[];
  tradeLogs: TradeLog[];
  tradingStatus: TradingStatus | null;
  setAccount: (account: AccountInfo) => void;
  setTrades: (trades: TradeOrder[]) => void;
  setTradeLogs: (logs: TradeLog[]) => void;
  setTradingStatus: (status: TradingStatus) => void;
}

export const useTradingStore = create<TradingState>((set) => ({
  account: null,
  trades: [],
  tradeLogs: [],
  tradingStatus: null,
  setAccount: (account) => set({ account }),
  setTrades: (trades) => set({ trades }),
  setTradeLogs: (logs) => set({ tradeLogs: logs }),
  setTradingStatus: (status) => set({ tradingStatus: status }),
}));
```

---

## 8. 실시간 데이터 업데이트

### 8.1 폴링 방식 (현재 권장)

**계좌 정보 폴링**:
```typescript
// hooks/useAccountPolling.ts
import { useEffect } from 'react';
import { tradingApi } from '../api/trading';

export const useAccountPolling = (interval: number = 5000) => {
  useEffect(() => {
    const fetchAccount = async () => {
      const response = await tradingApi.getAccount();
      // 상태 업데이트
    };

    fetchAccount();
    const timer = setInterval(fetchAccount, interval);
    return () => clearInterval(timer);
  }, [interval]);
};
```

**권장 폴링 간격**:
- 계좌 정보: 5초
- 현재 가격: 3초
- 거래 내역: 10초
- 자동 거래 상태: 5초
- 알림: 10초

### 8.2 WebSocket (향후 구현)

현재는 WebSocket이 구현되지 않았지만, 향후 구현 시:
```typescript
// hooks/useWebSocket.ts
export const useWebSocket = (url: string) => {
  const [data, setData] = useState(null);

  useEffect(() => {
    const ws = new WebSocket(url);
    
    ws.onmessage = (event) => {
      const message = JSON.parse(event.data);
      setData(message);
    };

    return () => ws.close();
  }, [url]);

  return data;
};
```

---

## 9. 에러 처리

### 9.1 공통 에러 처리

```typescript
// utils/errorHandler.ts
export const handleApiError = (error: any): string => {
  if (error.response) {
    // 서버 응답 에러
    const message = error.response.data?.message;
    if (message) {
      return message;
    }
    return `서버 오류: ${error.response.status}`;
  } else if (error.request) {
    // 요청 전송 실패
    return '서버에 연결할 수 없습니다.';
  } else {
    // 기타 에러
    return error.message || '알 수 없는 오류가 발생했습니다.';
  }
};
```

### 9.2 에러 바운더리

```typescript
// components/ErrorBoundary.tsx
import React from 'react';

export class ErrorBoundary extends React.Component {
  state = { hasError: false, error: null };

  static getDerivedStateFromError(error: Error) {
    return { hasError: true, error };
  }

  render() {
    if (this.state.hasError) {
      return <div>오류가 발생했습니다. 페이지를 새로고침해주세요.</div>;
    }
    return this.props.children;
  }
}
```

---

## 10. 개발 순서 및 체크리스트

### 10.1 1단계: 기본 설정 (1일)

- [ ] 프로젝트 초기화
- [ ] API 클라이언트 설정
- [ ] 라우팅 설정
- [ ] 기본 레이아웃 구성

### 10.2 2단계: 인증 시스템 (2일)

- [ ] 로그인 페이지
- [ ] 회원가입 페이지
- [ ] 세션 관리
- [ ] Protected Route
- [ ] 로그아웃 기능

### 10.3 3단계: 설문조사 (2일)

- [ ] 설문조사 페이지
- [ ] 15문항 구현
- [ ] 진행률 표시
- [ ] 제출 및 프로필 생성

### 10.4 4단계: 대시보드 (3일)

- [ ] 계좌 정보 표시
- [ ] 현재 가격 표시
- [ ] 자동 거래 제어
- [ ] 최근 거래 내역
- [ ] 통계 표시

### 10.5 5단계: 차트 시각화 (3일)

- [ ] OHLCV 차트
- [ ] 기술 지표 표시
- [ ] 시간봉 선택
- [ ] 줌/팬 기능

### 10.6 6단계: 거래 내역 (2일)

- [ ] 거래 내역 테이블
- [ ] 거래 로그 (HOLD 포함)
- [ ] 필터링/정렬
- [ ] 상세 정보 모달

### 10.7 7단계: 시장 데이터 (2일)

- [ ] 뉴스 표시
- [ ] 공포/탐욕 지수
- [ ] 24시간 통계

### 10.8 8단계: 알림 시스템 (2일)

- [ ] 알림 목록
- [ ] 실시간 알림 업데이트
- [ ] 알림 읽음 처리

### 10.9 9단계: 최적화 및 테스트 (3일)

- [ ] 성능 최적화
- [ ] 반응형 디자인
- [ ] 브라우저 호환성 테스트
- [ ] 에러 처리 강화

**총 예상 기간**: 약 20일

---

## 11. 테스트 방법

### 11.1 API 테스트

**Postman 또는 Insomnia 사용**:
1. 서버 실행 확인: `GET /api/health`
2. 회원가입: `POST /api/auth/register`
3. 로그인: `POST /api/auth/login`
4. 세션 토큰 저장
5. 인증 필요한 API 테스트

### 11.2 프론트엔드 테스트

**개발자 도구 활용**:
- Network 탭: API 요청/응답 확인
- Console: 에러 메시지 확인
- Application 탭: localStorage 확인

**테스트 시나리오**:
1. 회원가입 → 로그인 → 대시보드 접근
2. 설문조사 제출 → 프로필 생성 확인
3. 자동 거래 시작 → 상태 확인
4. 거래 내역 조회 → 데이터 표시 확인
5. 차트 데이터 로드 → 시각화 확인

---

## 12. 배포 가이드

### 12.1 빌드

```bash
# React
npm run build

# 빌드 결과물: build/ 폴더
```

### 12.2 환경 변수 설정

프로덕션 환경 변수:
```env
REACT_APP_API_BASE_URL=https://api.bitbot.com/api
```

### 12.3 배포 옵션

1. **Vercel** (권장)
   ```bash
   npm install -g vercel
   vercel
   ```

2. **Netlify**
   - GitHub 연동
   - 빌드 명령: `npm run build`
   - 배포 폴더: `build`

3. **AWS S3 + CloudFront**
   - S3에 빌드 파일 업로드
   - CloudFront로 CDN 구성

---

## 📚 참고 문서

- **API 명세서**: [API_DOCUMENTATION.md](API_DOCUMENTATION.md)
- **데이터베이스 스키마**: [DATABASE_SCHEMA.md](DATABASE_SCHEMA.md)
- **백엔드 분석**: [BACKEND_COMPLETE_ANALYSIS.md](BACKEND_COMPLETE_ANALYSIS.md)

---

## ⚠️ 주의사항

1. **CORS**: 개발 환경에서는 모든 origin 허용, 프로덕션에서는 특정 도메인만 허용
2. **세션 토큰**: localStorage에 저장 (향후 httpOnly cookie로 변경 권장)
3. **에러 처리**: 모든 API 호출에 try-catch 적용
4. **로딩 상태**: 사용자 경험을 위해 로딩 상태 항상 표시
5. **반응형**: 모바일/태블릿/데스크톱 모두 지원

---

## 🆘 문제 해결

### CORS 오류
- 서버가 실행 중인지 확인
- `CorsConfig` 설정 확인

### 인증 오류
- 세션 토큰이 올바른지 확인
- `/api/auth/verify` 호출하여 세션 확인

### 데이터가 표시되지 않음
- Network 탭에서 API 응답 확인
- 콘솔 에러 확인
- 서버 로그 확인

---

**문서 버전**: 1.0.0  
**최종 업데이트**: 2025-11-29

