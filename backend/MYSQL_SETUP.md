# MySQL 서버 설정 확인 가이드

## ✅ 코드 수정 완료

MySQL 연결 지원 코드가 추가되었습니다:
- ✅ `pom.xml`: MySQL JDBC 드라이버 추가
- ✅ `DatabaseConnection.java`: MySQL 및 SQLite 지원
- ✅ `ConfigLoader.java`: MySQL 설정 메서드 추가
- ✅ `SchemaInitializer.java`: MySQL 및 SQLite 스키마 자동 변환
- ✅ `.env`: MySQL 설정 예시 추가

---

## 서버 MySQL에서 확인해야 할 정보

서버(203.234.62.223)의 MySQL에 접속하여 다음 정보를 확인하세요:

### 1. 데이터베이스 확인
```sql
-- MySQL에 접속 후 실행
SHOW DATABASES;

-- bitbot 데이터베이스가 없으면 생성
CREATE DATABASE IF NOT EXISTS bitbot 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

USE bitbot;
```

### 2. 사용자 및 권한 확인
```sql
-- 현재 사용자 확인
SELECT USER(), CURRENT_USER();

-- root 사용자 호스트 확인 (중요!)
SELECT User, Host FROM mysql.user WHERE User = 'root';

-- 결과 해석:
-- - Host가 'localhost'만 있으면 → 원격 접속 불가
-- - Host에 '%'가 있으면 → 모든 IP에서 접속 가능
-- - Host에 특정 IP가 있으면 → 해당 IP에서만 접속 가능
```

### 2-1. 원격 접속 허용 설정 (필수!)
현재 `root` 사용자의 `Host`가 `localhost`로만 설정되어 있으면 원격 접속이 불가합니다.

다음 SQL을 실행하여 원격 접속을 허용하세요:

```sql
-- 방법 1: root@'%' 사용자 생성 및 권한 부여 (권장)
CREATE USER IF NOT EXISTS 'root'@'%' IDENTIFIED BY 'dsem1010!';
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;

-- 방법 2: 기존 root에 원격 접속 권한 추가 (MySQL 5.7 이하)
-- GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' IDENTIFIED BY 'dsem1010!' WITH GRANT OPTION;
-- FLUSH PRIVILEGES;

-- 확인
SELECT User, Host FROM mysql.user WHERE User = 'root';
-- 결과에 'root' | '%' 가 보이면 성공!
```

### 3. 필요한 정보 정리

다음 정보를 `.env` 파일에 입력해야 합니다:

- **MYSQL_HOST**: `203.234.62.223` (이미 알고 있음)
- **MYSQL_PORT**: `3306` (기본값, 다르면 확인 필요)
- **MYSQL_DATABASE**: `bitbot` (또는 다른 이름)
- **MYSQL_USERNAME**: `root` 또는 다른 사용자명
- **MYSQL_PASSWORD**: 사용자 비밀번호

### 4. 원격 접속 테스트 (로컬에서)

로컬 컴퓨터에서 다음 명령어로 연결 테스트:

```bash
# MySQL 클라이언트가 설치되어 있다면
mysql -h 203.234.62.223 -P 3306 -u [사용자명] -p

# 또는 telnet으로 포트 확인
telnet 203.234.62.223 3306
```

---

## 다음 단계

1. 위 정보를 확인한 후 `.env` 파일에 입력
2. 코드 수정 완료 후 연결 테스트
3. 스키마 자동 생성 확인

