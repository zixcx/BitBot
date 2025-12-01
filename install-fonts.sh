#!/bin/bash

# Geist Mono 폰트 자동 설치 스크립트 (macOS / Linux)

set -e  # 에러 발생 시 스크립트 중단

echo "=================================="
echo "Geist Mono 폰트 설치 시작"
echo "=================================="
echo ""

# Node.js 및 npm 확인
if ! command -v npm &> /dev/null; then
    echo "❌ npm이 설치되어 있지 않습니다."
    echo "Node.js를 먼저 설치해주세요: https://nodejs.org/"
    exit 1
fi

echo "✓ npm 확인 완료: $(npm --version)"
echo ""

# fonts 디렉토리 확인
FONTS_DIR="src/main/resources/fonts"
if [ ! -d "$FONTS_DIR" ]; then
    echo "📁 폰트 디렉토리 생성 중..."
    mkdir -p "$FONTS_DIR"
fi

# npm으로 geist 설치
echo "📦 npm으로 Geist 폰트 패키지 다운로드 중..."
npm install --silent geist

# 폰트 파일 복사
echo "📋 Geist Sans 폰트 파일 복사 중..."
cp node_modules/geist/dist/fonts/geist-sans/Geist-Regular.ttf "$FONTS_DIR/"
cp node_modules/geist/dist/fonts/geist-sans/Geist-Bold.ttf "$FONTS_DIR/"
cp node_modules/geist/dist/fonts/geist-sans/Geist-Medium.ttf "$FONTS_DIR/"
cp node_modules/geist/dist/fonts/geist-sans/Geist-Light.ttf "$FONTS_DIR/"

echo "📋 Geist Mono 폰트 파일 복사 중..."
cp node_modules/geist/dist/fonts/geist-mono/GeistMono-Regular.ttf "$FONTS_DIR/"
cp node_modules/geist/dist/fonts/geist-mono/GeistMono-Bold.ttf "$FONTS_DIR/"
cp node_modules/geist/dist/fonts/geist-mono/GeistMono-Medium.ttf "$FONTS_DIR/"
cp node_modules/geist/dist/fonts/geist-mono/GeistMono-Light.ttf "$FONTS_DIR/"

echo "✓ 폰트 파일 복사 완료"
echo ""

# npm 파일 정리
echo "🧹 임시 파일 정리 중..."
rm -rf node_modules package-lock.json

echo ""
echo "=================================="
echo "✅ 폰트 설치 완료!"
echo "=================================="
echo ""
echo "설치된 폰트 파일:"
ls -lh "$FONTS_DIR/" | grep -E "Geist"
echo ""
echo "이제 'mvn javafx:run' 명령어로 애플리케이션을 실행할 수 있습니다."

