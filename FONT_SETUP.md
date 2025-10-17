# Geist Mono 폰트 설치 가이드

## 필요 조건

-   Node.js 및 npm이 설치되어 있어야 합니다
-   설치 확인: `npm --version`

## 설치 방법

### 1단계: npm으로 Geist 폰트 다운로드

프로젝트 루트 디렉토리에서 다음 명령어를 실행합니다:

```bash
npm install geist
```

### 2단계: 폰트 파일을 프로젝트로 복사

#### macOS / Linux

```bash
# 한 줄 명령어 (Geist Sans)
cp node_modules/geist/dist/fonts/geist-sans/Geist-Regular.ttf src/main/resources/fonts/ && \
cp node_modules/geist/dist/fonts/geist-sans/Geist-Bold.ttf src/main/resources/fonts/ && \
cp node_modules/geist/dist/fonts/geist-sans/Geist-Medium.ttf src/main/resources/fonts/ && \
cp node_modules/geist/dist/fonts/geist-sans/Geist-Light.ttf src/main/resources/fonts/

# 한 줄 명령어 (Geist Mono)
cp node_modules/geist/dist/fonts/geist-mono/GeistMono-Regular.ttf src/main/resources/fonts/ && \
cp node_modules/geist/dist/fonts/geist-mono/GeistMono-Bold.ttf src/main/resources/fonts/ && \
cp node_modules/geist/dist/fonts/geist-mono/GeistMono-Medium.ttf src/main/resources/fonts/ && \
cp node_modules/geist/dist/fonts/geist-mono/GeistMono-Light.ttf src/main/resources/fonts/

# 또는 와일드카드 사용
cp node_modules/geist/dist/fonts/geist-sans/Geist-*.ttf src/main/resources/fonts/
cp node_modules/geist/dist/fonts/geist-mono/GeistMono-*.ttf src/main/resources/fonts/
```

#### Windows (PowerShell)

```powershell
# Geist Sans 폰트 복사
Copy-Item node_modules\geist\dist\fonts\geist-sans\Geist-Regular.ttf src\main\resources\fonts\
Copy-Item node_modules\geist\dist\fonts\geist-sans\Geist-Bold.ttf src\main\resources\fonts\
Copy-Item node_modules\geist\dist\fonts\geist-sans\Geist-Medium.ttf src\main\resources\fonts\
Copy-Item node_modules\geist\dist\fonts\geist-sans\Geist-Light.ttf src\main\resources\fonts\

# Geist Mono 폰트 복사
Copy-Item node_modules\geist\dist\fonts\geist-mono\GeistMono-Regular.ttf src\main\resources\fonts\
Copy-Item node_modules\geist\dist\fonts\geist-mono\GeistMono-Bold.ttf src\main\resources\fonts\
Copy-Item node_modules\geist\dist\fonts\geist-mono\GeistMono-Medium.ttf src\main\resources\fonts\
Copy-Item node_modules\geist\dist\fonts\geist-mono\GeistMono-Light.ttf src\main\resources\fonts\
```

#### Windows (Command Prompt)

```cmd
REM Geist Sans 폰트 복사
copy node_modules\geist\dist\fonts\geist-sans\Geist-Regular.ttf src\main\resources\fonts\
copy node_modules\geist\dist\fonts\geist-sans\Geist-Bold.ttf src\main\resources\fonts\
copy node_modules\geist\dist\fonts\geist-sans\Geist-Medium.ttf src\main\resources\fonts\
copy node_modules\geist\dist\fonts\geist-sans\Geist-Light.ttf src\main\resources\fonts\

REM Geist Mono 폰트 복사
copy node_modules\geist\dist\fonts\geist-mono\GeistMono-Regular.ttf src\main\resources\fonts\
copy node_modules\geist\dist\fonts\geist-mono\GeistMono-Bold.ttf src\main\resources\fonts\
copy node_modules\geist\dist\fonts\geist-mono\GeistMono-Medium.ttf src\main\resources\fonts\
copy node_modules\geist\dist\fonts\geist-mono\GeistMono-Light.ttf src\main\resources\fonts\
```

### 3단계: 정리 (선택사항)

폰트 복사가 완료되면 node_modules 디렉토리를 삭제할 수 있습니다:

#### macOS / Linux

```bash
rm -rf node_modules package-lock.json
```

#### Windows (PowerShell)

```powershell
Remove-Item -Recurse -Force node_modules, package-lock.json
```

#### Windows (Command Prompt)

```cmd
rmdir /s /q node_modules
del package-lock.json
```

## 설치 확인

### 폰트 파일 확인

#### macOS / Linux

```bash
ls -lh src/main/resources/fonts/
```

#### Windows (PowerShell)

```powershell
Get-ChildItem src\main\resources\fonts\
```

#### Windows (Command Prompt)

```cmd
dir src\main\resources\fonts\
```

다음 8개의 파일이 있어야 합니다:

**Geist Sans:**

-   `Geist-Regular.ttf`
-   `Geist-Bold.ttf`
-   `Geist-Medium.ttf`
-   `Geist-Light.ttf`

**Geist Mono:**

-   `GeistMono-Regular.ttf`
-   `GeistMono-Bold.ttf`
-   `GeistMono-Medium.ttf`
-   `GeistMono-Light.ttf`

### 애플리케이션 실행

```bash
mvn javafx:run
```

애플리케이션이 실행되고 모든 텍스트가 Geist Mono 폰트로 표시되면 설치가 성공한 것입니다.

## 자동 설치 스크립트

### macOS / Linux

`install-fonts.sh` 파일을 생성하여 사용할 수 있습니다:

```bash
#!/bin/bash

echo "Geist 폰트 설치 시작..."

# npm으로 geist 설치
npm install geist

# Geist Sans 폰트 파일 복사
cp node_modules/geist/dist/fonts/geist-sans/Geist-Regular.ttf src/main/resources/fonts/
cp node_modules/geist/dist/fonts/geist-sans/Geist-Bold.ttf src/main/resources/fonts/
cp node_modules/geist/dist/fonts/geist-sans/Geist-Medium.ttf src/main/resources/fonts/
cp node_modules/geist/dist/fonts/geist-sans/Geist-Light.ttf src/main/resources/fonts/

# Geist Mono 폰트 파일 복사
cp node_modules/geist/dist/fonts/geist-mono/GeistMono-Regular.ttf src/main/resources/fonts/
cp node_modules/geist/dist/fonts/geist-mono/GeistMono-Bold.ttf src/main/resources/fonts/
cp node_modules/geist/dist/fonts/geist-mono/GeistMono-Medium.ttf src/main/resources/fonts/
cp node_modules/geist/dist/fonts/geist-mono/GeistMono-Light.ttf src/main/resources/fonts/

# 정리
rm -rf node_modules package-lock.json

echo "폰트 설치 완료!"
ls -lh src/main/resources/fonts/
```

실행:

```bash
chmod +x install-fonts.sh
./install-fonts.sh
```

### Windows (PowerShell)

`install-fonts.ps1` 파일을 생성하여 사용할 수 있습니다:

```powershell
Write-Host "Geist 폰트 설치 시작..."

# npm으로 geist 설치
npm install geist

# Geist Sans 폰트 파일 복사
Copy-Item node_modules\geist\dist\fonts\geist-sans\Geist-Regular.ttf src\main\resources\fonts\
Copy-Item node_modules\geist\dist\fonts\geist-sans\Geist-Bold.ttf src\main\resources\fonts\
Copy-Item node_modules\geist\dist\fonts\geist-sans\Geist-Medium.ttf src\main\resources\fonts\
Copy-Item node_modules\geist\dist\fonts\geist-sans\Geist-Light.ttf src\main\resources\fonts\

# Geist Mono 폰트 파일 복사
Copy-Item node_modules\geist\dist\fonts\geist-mono\GeistMono-Regular.ttf src\main\resources\fonts\
Copy-Item node_modules\geist\dist\fonts\geist-mono\GeistMono-Bold.ttf src\main\resources\fonts\
Copy-Item node_modules\geist\dist\fonts\geist-mono\GeistMono-Medium.ttf src\main\resources\fonts\
Copy-Item node_modules\geist\dist\fonts\geist-mono\GeistMono-Light.ttf src\main\resources\fonts\

# 정리
Remove-Item -Recurse -Force node_modules, package-lock.json

Write-Host "폰트 설치 완료!"
Get-ChildItem src\main\resources\fonts\
```

실행 (PowerShell):

```powershell
# 실행 정책 설정 (최초 1회)
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser

# 스크립트 실행
.\install-fonts.ps1
```

## 문제 해결

### npm이 설치되어 있지 않은 경우

#### macOS

```bash
# Homebrew로 Node.js 설치
brew install node
```

#### Windows

Node.js 공식 웹사이트에서 설치 프로그램 다운로드:
https://nodejs.org/

### 폰트가 제대로 로드되지 않는 경우

1. 폰트 파일이 올바른 위치에 있는지 확인
2. Maven 클린 빌드 후 다시 실행:
    ```bash
    mvn clean compile javafx:run
    ```
3. CSS 파일 경로 확인: `src/main/resources/css/styles.css`

### 권한 오류 (Windows)

PowerShell 스크립트 실행 시 권한 오류가 발생하면:

```powershell
Set-ExecutionPolicy -ExecutionPolicy Bypass -Scope Process
```

## 추가 폰트 Weight

더 많은 font weight가 필요한 경우, 다음 파일들도 복사할 수 있습니다:

-   `GeistMono-Thin.ttf`
-   `GeistMono-UltraLight.ttf`
-   `GeistMono-SemiBold.ttf`
-   `GeistMono-Black.ttf`
-   `GeistMono-UltraBlack.ttf`

CSS 파일에 @font-face 선언을 추가해야 합니다.

## 라이선스

Geist 폰트는 SIL Open Font License 1.1 하에 배포됩니다.
자세한 내용: https://github.com/vercel/geist-font


# Geist Mono 폰트 설치 가이드

## 필요 조건

-   Node.js 및 npm이 설치되어 있어야 합니다
-   설치 확인: `npm --version`

## 설치 방법

### 1단계: npm으로 Geist 폰트 다운로드

프로젝트 루트 디렉토리에서 다음 명령어를 실행합니다:

```bash
npm install geist
```

### 2단계: 폰트 파일을 프로젝트로 복사

#### macOS / Linux

```bash
# 한 줄 명령어 (Geist Sans)
cp node_modules/geist/dist/fonts/geist-sans/Geist-Regular.ttf src/main/resources/fonts/ && \
cp node_modules/geist/dist/fonts/geist-sans/Geist-Bold.ttf src/main/resources/fonts/ && \
cp node_modules/geist/dist/fonts/geist-sans/Geist-Medium.ttf src/main/resources/fonts/ && \
cp node_modules/geist/dist/fonts/geist-sans/Geist-Light.ttf src/main/resources/fonts/

# 한 줄 명령어 (Geist Mono)
cp node_modules/geist/dist/fonts/geist-mono/GeistMono-Regular.ttf src/main/resources/fonts/ && \
cp node_modules/geist/dist/fonts/geist-mono/GeistMono-Bold.ttf src/main/resources/fonts/ && \
cp node_modules/geist/dist/fonts/geist-mono/GeistMono-Medium.ttf src/main/resources/fonts/ && \
cp node_modules/geist/dist/fonts/geist-mono/GeistMono-Light.ttf src/main/resources/fonts/

# 또는 와일드카드 사용
cp node_modules/geist/dist/fonts/geist-sans/Geist-*.ttf src/main/resources/fonts/
cp node_modules/geist/dist/fonts/geist-mono/GeistMono-*.ttf src/main/resources/fonts/
```

#### Windows (PowerShell)

```powershell
# Geist Sans 폰트 복사
Copy-Item node_modules\geist\dist\fonts\geist-sans\Geist-Regular.ttf src\main\resources\fonts\
Copy-Item node_modules\geist\dist\fonts\geist-sans\Geist-Bold.ttf src\main\resources\fonts\
Copy-Item node_modules\geist\dist\fonts\geist-sans\Geist-Medium.ttf src\main\resources\fonts\
Copy-Item node_modules\geist\dist\fonts\geist-sans\Geist-Light.ttf src\main\resources\fonts\

# Geist Mono 폰트 복사
Copy-Item node_modules\geist\dist\fonts\geist-mono\GeistMono-Regular.ttf src\main\resources\fonts\
Copy-Item node_modules\geist\dist\fonts\geist-mono\GeistMono-Bold.ttf src\main\resources\fonts\
Copy-Item node_modules\geist\dist\fonts\geist-mono\GeistMono-Medium.ttf src\main\resources\fonts\
Copy-Item node_modules\geist\dist\fonts\geist-mono\GeistMono-Light.ttf src\main\resources\fonts\
```

#### Windows (Command Prompt)

```cmd
REM Geist Sans 폰트 복사
copy node_modules\geist\dist\fonts\geist-sans\Geist-Regular.ttf src\main\resources\fonts\
copy node_modules\geist\dist\fonts\geist-sans\Geist-Bold.ttf src\main\resources\fonts\
copy node_modules\geist\dist\fonts\geist-sans\Geist-Medium.ttf src\main\resources\fonts\
copy node_modules\geist\dist\fonts\geist-sans\Geist-Light.ttf src\main\resources\fonts\

REM Geist Mono 폰트 복사
copy node_modules\geist\dist\fonts\geist-mono\GeistMono-Regular.ttf src\main\resources\fonts\
copy node_modules\geist\dist\fonts\geist-mono\GeistMono-Bold.ttf src\main\resources\fonts\
copy node_modules\geist\dist\fonts\geist-mono\GeistMono-Medium.ttf src\main\resources\fonts\
copy node_modules\geist\dist\fonts\geist-mono\GeistMono-Light.ttf src\main\resources\fonts\
```

### 3단계: 정리 (선택사항)

폰트 복사가 완료되면 node_modules 디렉토리를 삭제할 수 있습니다:

#### macOS / Linux

```bash
rm -rf node_modules package-lock.json
```

#### Windows (PowerShell)

```powershell
Remove-Item -Recurse -Force node_modules, package-lock.json
```

#### Windows (Command Prompt)

```cmd
rmdir /s /q node_modules
del package-lock.json
```

## 설치 확인

### 폰트 파일 확인

#### macOS / Linux

```bash
ls -lh src/main/resources/fonts/
```

#### Windows (PowerShell)

```powershell
Get-ChildItem src\main\resources\fonts\
```

#### Windows (Command Prompt)

```cmd
dir src\main\resources\fonts\
```

다음 8개의 파일이 있어야 합니다:

**Geist Sans:**

-   `Geist-Regular.ttf`
-   `Geist-Bold.ttf`
-   `Geist-Medium.ttf`
-   `Geist-Light.ttf`

**Geist Mono:**

-   `GeistMono-Regular.ttf`
-   `GeistMono-Bold.ttf`
-   `GeistMono-Medium.ttf`
-   `GeistMono-Light.ttf`

### 애플리케이션 실행

```bash
mvn javafx:run
```

애플리케이션이 실행되고 모든 텍스트가 Geist Mono 폰트로 표시되면 설치가 성공한 것입니다.

## 자동 설치 스크립트

### macOS / Linux

`install-fonts.sh` 파일을 생성하여 사용할 수 있습니다:

```bash
#!/bin/bash

echo "Geist 폰트 설치 시작..."

# npm으로 geist 설치
npm install geist

# Geist Sans 폰트 파일 복사
cp node_modules/geist/dist/fonts/geist-sans/Geist-Regular.ttf src/main/resources/fonts/
cp node_modules/geist/dist/fonts/geist-sans/Geist-Bold.ttf src/main/resources/fonts/
cp node_modules/geist/dist/fonts/geist-sans/Geist-Medium.ttf src/main/resources/fonts/
cp node_modules/geist/dist/fonts/geist-sans/Geist-Light.ttf src/main/resources/fonts/

# Geist Mono 폰트 파일 복사
cp node_modules/geist/dist/fonts/geist-mono/GeistMono-Regular.ttf src/main/resources/fonts/
cp node_modules/geist/dist/fonts/geist-mono/GeistMono-Bold.ttf src/main/resources/fonts/
cp node_modules/geist/dist/fonts/geist-mono/GeistMono-Medium.ttf src/main/resources/fonts/
cp node_modules/geist/dist/fonts/geist-mono/GeistMono-Light.ttf src/main/resources/fonts/

# 정리
rm -rf node_modules package-lock.json

echo "폰트 설치 완료!"
ls -lh src/main/resources/fonts/
```

실행:

```bash
chmod +x install-fonts.sh
./install-fonts.sh
```

### Windows (PowerShell)

`install-fonts.ps1` 파일을 생성하여 사용할 수 있습니다:

```powershell
Write-Host "Geist 폰트 설치 시작..."

# npm으로 geist 설치
npm install geist

# Geist Sans 폰트 파일 복사
Copy-Item node_modules\geist\dist\fonts\geist-sans\Geist-Regular.ttf src\main\resources\fonts\
Copy-Item node_modules\geist\dist\fonts\geist-sans\Geist-Bold.ttf src\main\resources\fonts\
Copy-Item node_modules\geist\dist\fonts\geist-sans\Geist-Medium.ttf src\main\resources\fonts\
Copy-Item node_modules\geist\dist\fonts\geist-sans\Geist-Light.ttf src\main\resources\fonts\

# Geist Mono 폰트 파일 복사
Copy-Item node_modules\geist\dist\fonts\geist-mono\GeistMono-Regular.ttf src\main\resources\fonts\
Copy-Item node_modules\geist\dist\fonts\geist-mono\GeistMono-Bold.ttf src\main\resources\fonts\
Copy-Item node_modules\geist\dist\fonts\geist-mono\GeistMono-Medium.ttf src\main\resources\fonts\
Copy-Item node_modules\geist\dist\fonts\geist-mono\GeistMono-Light.ttf src\main\resources\fonts\

# 정리
Remove-Item -Recurse -Force node_modules, package-lock.json

Write-Host "폰트 설치 완료!"
Get-ChildItem src\main\resources\fonts\
```

실행 (PowerShell):

```powershell
# 실행 정책 설정 (최초 1회)
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser

# 스크립트 실행
.\install-fonts.ps1
```

## 문제 해결

### npm이 설치되어 있지 않은 경우

#### macOS

```bash
# Homebrew로 Node.js 설치
brew install node
```

#### Windows

Node.js 공식 웹사이트에서 설치 프로그램 다운로드:
https://nodejs.org/

### 폰트가 제대로 로드되지 않는 경우

1. 폰트 파일이 올바른 위치에 있는지 확인
2. Maven 클린 빌드 후 다시 실행:
    ```bash
    mvn clean compile javafx:run
    ```
3. CSS 파일 경로 확인: `src/main/resources/css/styles.css`

### 권한 오류 (Windows)

PowerShell 스크립트 실행 시 권한 오류가 발생하면:

```powershell
Set-ExecutionPolicy -ExecutionPolicy Bypass -Scope Process
```

## 추가 폰트 Weight

더 많은 font weight가 필요한 경우, 다음 파일들도 복사할 수 있습니다:

-   `GeistMono-Thin.ttf`
-   `GeistMono-UltraLight.ttf`
-   `GeistMono-SemiBold.ttf`
-   `GeistMono-Black.ttf`
-   `GeistMono-UltraBlack.ttf`

CSS 파일에 @font-face 선언을 추가해야 합니다.

## 라이선스

Geist 폰트는 SIL Open Font License 1.1 하에 배포됩니다.
자세한 내용: https://github.com/vercel/geist-font
# Geist Mono 폰트 설치 가이드

## 필요 조건

-   Node.js 및 npm이 설치되어 있어야 합니다
-   설치 확인: `npm --version`

## 설치 방법

### 1단계: npm으로 Geist 폰트 다운로드

프로젝트 루트 디렉토리에서 다음 명령어를 실행합니다:

```bash
npm install geist
```

### 2단계: 폰트 파일을 프로젝트로 복사

#### macOS / Linux

```bash
# 한 줄 명령어 (Geist Sans)
cp node_modules/geist/dist/fonts/geist-sans/Geist-Regular.ttf src/main/resources/fonts/ && \
cp node_modules/geist/dist/fonts/geist-sans/Geist-Bold.ttf src/main/resources/fonts/ && \
cp node_modules/geist/dist/fonts/geist-sans/Geist-Medium.ttf src/main/resources/fonts/ && \
cp node_modules/geist/dist/fonts/geist-sans/Geist-Light.ttf src/main/resources/fonts/

# 한 줄 명령어 (Geist Mono)
cp node_modules/geist/dist/fonts/geist-mono/GeistMono-Regular.ttf src/main/resources/fonts/ && \
cp node_modules/geist/dist/fonts/geist-mono/GeistMono-Bold.ttf src/main/resources/fonts/ && \
cp node_modules/geist/dist/fonts/geist-mono/GeistMono-Medium.ttf src/main/resources/fonts/ && \
cp node_modules/geist/dist/fonts/geist-mono/GeistMono-Light.ttf src/main/resources/fonts/

# 또는 와일드카드 사용
cp node_modules/geist/dist/fonts/geist-sans/Geist-*.ttf src/main/resources/fonts/
cp node_modules/geist/dist/fonts/geist-mono/GeistMono-*.ttf src/main/resources/fonts/
```

#### Windows (PowerShell)

```powershell
# Geist Sans 폰트 복사
Copy-Item node_modules\geist\dist\fonts\geist-sans\Geist-Regular.ttf src\main\resources\fonts\
Copy-Item node_modules\geist\dist\fonts\geist-sans\Geist-Bold.ttf src\main\resources\fonts\
Copy-Item node_modules\geist\dist\fonts\geist-sans\Geist-Medium.ttf src\main\resources\fonts\
Copy-Item node_modules\geist\dist\fonts\geist-sans\Geist-Light.ttf src\main\resources\fonts\

# Geist Mono 폰트 복사
Copy-Item node_modules\geist\dist\fonts\geist-mono\GeistMono-Regular.ttf src\main\resources\fonts\
Copy-Item node_modules\geist\dist\fonts\geist-mono\GeistMono-Bold.ttf src\main\resources\fonts\
Copy-Item node_modules\geist\dist\fonts\geist-mono\GeistMono-Medium.ttf src\main\resources\fonts\
Copy-Item node_modules\geist\dist\fonts\geist-mono\GeistMono-Light.ttf src\main\resources\fonts\
```

#### Windows (Command Prompt)

```cmd
REM Geist Sans 폰트 복사
copy node_modules\geist\dist\fonts\geist-sans\Geist-Regular.ttf src\main\resources\fonts\
copy node_modules\geist\dist\fonts\geist-sans\Geist-Bold.ttf src\main\resources\fonts\
copy node_modules\geist\dist\fonts\geist-sans\Geist-Medium.ttf src\main\resources\fonts\
copy node_modules\geist\dist\fonts\geist-sans\Geist-Light.ttf src\main\resources\fonts\

REM Geist Mono 폰트 복사
copy node_modules\geist\dist\fonts\geist-mono\GeistMono-Regular.ttf src\main\resources\fonts\
copy node_modules\geist\dist\fonts\geist-mono\GeistMono-Bold.ttf src\main\resources\fonts\
copy node_modules\geist\dist\fonts\geist-mono\GeistMono-Medium.ttf src\main\resources\fonts\
copy node_modules\geist\dist\fonts\geist-mono\GeistMono-Light.ttf src\main\resources\fonts\
```

### 3단계: 정리 (선택사항)

폰트 복사가 완료되면 node_modules 디렉토리를 삭제할 수 있습니다:

#### macOS / Linux

```bash
rm -rf node_modules package-lock.json
```

#### Windows (PowerShell)

```powershell
Remove-Item -Recurse -Force node_modules, package-lock.json
```

#### Windows (Command Prompt)

```cmd
rmdir /s /q node_modules
del package-lock.json
```

## 설치 확인

### 폰트 파일 확인

#### macOS / Linux

```bash
ls -lh src/main/resources/fonts/
```

#### Windows (PowerShell)

```powershell
Get-ChildItem src\main\resources\fonts\
```

#### Windows (Command Prompt)

```cmd
dir src\main\resources\fonts\
```

다음 8개의 파일이 있어야 합니다:

**Geist Sans:**

-   `Geist-Regular.ttf`
-   `Geist-Bold.ttf`
-   `Geist-Medium.ttf`
-   `Geist-Light.ttf`

**Geist Mono:**

-   `GeistMono-Regular.ttf`
-   `GeistMono-Bold.ttf`
-   `GeistMono-Medium.ttf`
-   `GeistMono-Light.ttf`

### 애플리케이션 실행

```bash
mvn javafx:run
```

애플리케이션이 실행되고 모든 텍스트가 Geist Mono 폰트로 표시되면 설치가 성공한 것입니다.

## 자동 설치 스크립트

### macOS / Linux

`install-fonts.sh` 파일을 생성하여 사용할 수 있습니다:

```bash
#!/bin/bash

echo "Geist 폰트 설치 시작..."

# npm으로 geist 설치
npm install geist

# Geist Sans 폰트 파일 복사
cp node_modules/geist/dist/fonts/geist-sans/Geist-Regular.ttf src/main/resources/fonts/
cp node_modules/geist/dist/fonts/geist-sans/Geist-Bold.ttf src/main/resources/fonts/
cp node_modules/geist/dist/fonts/geist-sans/Geist-Medium.ttf src/main/resources/fonts/
cp node_modules/geist/dist/fonts/geist-sans/Geist-Light.ttf src/main/resources/fonts/

# Geist Mono 폰트 파일 복사
cp node_modules/geist/dist/fonts/geist-mono/GeistMono-Regular.ttf src/main/resources/fonts/
cp node_modules/geist/dist/fonts/geist-mono/GeistMono-Bold.ttf src/main/resources/fonts/
cp node_modules/geist/dist/fonts/geist-mono/GeistMono-Medium.ttf src/main/resources/fonts/
cp node_modules/geist/dist/fonts/geist-mono/GeistMono-Light.ttf src/main/resources/fonts/

# 정리
rm -rf node_modules package-lock.json

echo "폰트 설치 완료!"
ls -lh src/main/resources/fonts/
```

실행:

```bash
chmod +x install-fonts.sh
./install-fonts.sh
```

### Windows (PowerShell)

`install-fonts.ps1` 파일을 생성하여 사용할 수 있습니다:

```powershell
Write-Host "Geist 폰트 설치 시작..."

# npm으로 geist 설치
npm install geist

# Geist Sans 폰트 파일 복사
Copy-Item node_modules\geist\dist\fonts\geist-sans\Geist-Regular.ttf src\main\resources\fonts\
Copy-Item node_modules\geist\dist\fonts\geist-sans\Geist-Bold.ttf src\main\resources\fonts\
Copy-Item node_modules\geist\dist\fonts\geist-sans\Geist-Medium.ttf src\main\resources\fonts\
Copy-Item node_modules\geist\dist\fonts\geist-sans\Geist-Light.ttf src\main\resources\fonts\

# Geist Mono 폰트 파일 복사
Copy-Item node_modules\geist\dist\fonts\geist-mono\GeistMono-Regular.ttf src\main\resources\fonts\
Copy-Item node_modules\geist\dist\fonts\geist-mono\GeistMono-Bold.ttf src\main\resources\fonts\
Copy-Item node_modules\geist\dist\fonts\geist-mono\GeistMono-Medium.ttf src\main\resources\fonts\
Copy-Item node_modules\geist\dist\fonts\geist-mono\GeistMono-Light.ttf src\main\resources\fonts\

# 정리
Remove-Item -Recurse -Force node_modules, package-lock.json

Write-Host "폰트 설치 완료!"
Get-ChildItem src\main\resources\fonts\
```

실행 (PowerShell):

```powershell
# 실행 정책 설정 (최초 1회)
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser

# 스크립트 실행
.\install-fonts.ps1
```

## 문제 해결

### npm이 설치되어 있지 않은 경우

#### macOS

```bash
# Homebrew로 Node.js 설치
brew install node
```

#### Windows

Node.js 공식 웹사이트에서 설치 프로그램 다운로드:
https://nodejs.org/

### 폰트가 제대로 로드되지 않는 경우

1. 폰트 파일이 올바른 위치에 있는지 확인
2. Maven 클린 빌드 후 다시 실행:
    ```bash
    mvn clean compile javafx:run
    ```
3. CSS 파일 경로 확인: `src/main/resources/css/styles.css`

### 권한 오류 (Windows)

PowerShell 스크립트 실행 시 권한 오류가 발생하면:

```powershell
Set-ExecutionPolicy -ExecutionPolicy Bypass -Scope Process
```

## 추가 폰트 Weight

더 많은 font weight가 필요한 경우, 다음 파일들도 복사할 수 있습니다:

-   `GeistMono-Thin.ttf`
-   `GeistMono-UltraLight.ttf`
-   `GeistMono-SemiBold.ttf`
-   `GeistMono-Black.ttf`
-   `GeistMono-UltraBlack.ttf`

CSS 파일에 @font-face 선언을 추가해야 합니다.

## 라이선스

Geist 폰트는 SIL Open Font License 1.1 하에 배포됩니다.
자세한 내용: https://github.com/vercel/geist-font
