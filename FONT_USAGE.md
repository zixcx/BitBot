# Geist 폰트 사용 가이드

프로젝트에는 **Geist Sans**와 **Geist Mono** 두 가지 폰트 패밀리가 설치되어 있습니다.

## 기본 설정

현재 애플리케이션 전역 폰트는 **Geist Mono**로 설정되어 있습니다.

```css
/* src/main/resources/css/styles.css */
.root {
    -fx-font-family: "Geist Mono", monospace;
}
```

## Geist Sans 사용하기

특정 UI 요소에 Geist Sans 폰트를 적용하려면 CSS 클래스를 사용하세요.

### FXML에서 사용

```xml
<!-- 기본 Geist Sans -->
<Label text="Hello World" styleClass="geist-sans"/>

<!-- Bold -->
<Label text="Bold Text" styleClass="geist-sans-bold"/>

<!-- Medium -->
<Label text="Medium Text" styleClass="geist-sans-medium"/>

<!-- Light -->
<Label text="Light Text" styleClass="geist-sans-light"/>
```

### Java 코드에서 사용

```java
// 기본 Geist Sans
label.getStyleClass().add("geist-sans");

// Bold
label.getStyleClass().add("geist-sans-bold");

// Medium
label.getStyleClass().add("geist-sans-medium");

// Light
label.getStyleClass().add("geist-sans-light");
```

## 전역 폰트 변경

모든 UI 요소의 기본 폰트를 Geist Sans로 변경하려면:

```css
/* src/main/resources/css/styles.css */
.root {
    -fx-font-family: "Geist", sans-serif; /* Geist Mono 대신 Geist 사용 */
}

/* 또는 모든 텍스트 요소 선택자 변경 */
.label,
.button,
.text,
.text-field,
.text-area {
    -fx-font-family: "Geist", sans-serif;
}
```

## 사용 가능한 폰트 목록

### Geist Sans (일반 Sans-serif 폰트)

-   `"Geist"` - Regular (400)
-   `"Geist Light"` - Light (300)
-   `"Geist Medium"` - Medium (500)
-   `"Geist Bold"` - Bold (700)

### Geist Mono (Monospace 폰트)

-   `"Geist Mono"` - Regular (400)
-   `"Geist Mono Light"` - Light (300)
-   `"Geist Mono Medium"` - Medium (500)
-   `"Geist Mono Bold"` - Bold (700)


## 사용 가능한 폰트 목록

### Geist Sans (일반 Sans-serif 폰트)

-   `"Geist"` - Regular (400)
-   `"Geist Light"` - Light (300)
-   `"Geist Medium"` - Medium (500)
-   `"Geist Bold"` - Bold (700)

### Geist Mono (Monospace 폰트)

-   `"Geist Mono"` - Regular (400)
-   `"Geist Mono Light"` - Light (300)
-   `"Geist Mono Medium"` - Medium (500)
-   `"Geist Mono Bold"` - Bold (700)


## 사용 가능한 폰트 목록

### Geist Sans (일반 Sans-serif 폰트)

-   `"Geist"` - Regular (400)
-   `"Geist Light"` - Light (300)
-   `"Geist Medium"` - Medium (500)
-   `"Geist Bold"` - Bold (700)

### Geist Mono (Monospace 폰트)

-   `"Geist Mono"` - Regular (400)
-   `"Geist Mono Light"` - Light (300)
-   `"Geist Mono Medium"` - Medium (500)
-   `"Geist Mono Bold"` - Bold (700)

## 사용 가능한 폰트 목록

### Geist Sans (일반 Sans-serif 폰트)

-   `"Geist"` - Regular (400)
-   `"Geist Light"` - Light (300)
-   `"Geist Medium"` - Medium (500)
-   `"Geist Bold"` - Bold (700)

### Geist Mono (Monospace 폰트)

-   `"Geist Mono"` - Regular (400)
-   `"Geist Mono Light"` - Light (300)
-   `"Geist Mono Medium"` - Medium (500)
-   `"Geist Mono Bold"` - Bold (700)

## 사용 가능한 폰트 목록

### Geist Sans (일반 Sans-serif 폰트)

-   `"Geist"` - Regular (400)
-   `"Geist Light"` - Light (300)
-   `"Geist Medium"` - Medium (500)
-   `"Geist Bold"` - Bold (700)

### Geist Mono (Monospace 폰트)

-   `"Geist Mono"` - Regular (400)
-   `"Geist Mono Light"` - Light (300)
-   `"Geist Mono Medium"` - Medium (500)
-   `"Geist Mono Bold"` - Bold (700)

## 사용 가능한 폰트 목록

### Geist Sans (일반 Sans-serif 폰트)

-   `"Geist"` - Regular (400)
-   `"Geist Light"` - Light (300)
-   `"Geist Medium"` - Medium (500)
-   `"Geist Bold"` - Bold (700)

### Geist Mono (Monospace 폰트)

-   `"Geist Mono"` - Regular (400)
-   `"Geist Mono Light"` - Light (300)
-   `"Geist Mono Medium"` - Medium (500)
-   `"Geist Mono Bold"` - Bold (700)

## 사용 가능한 폰트 목록

### Geist Sans (일반 Sans-serif 폰트)

-   `"Geist"` - Regular (400)
-   `"Geist Light"` - Light (300)
-   `"Geist Medium"` - Medium (500)
-   `"Geist Bold"` - Bold (700)

### Geist Mono (Monospace 폰트)

-   `"Geist Mono"` - Regular (400)
-   `"Geist Mono Light"` - Light (300)
-   `"Geist Mono Medium"` - Medium (500)
-   `"Geist Mono Bold"` - Bold (700)

## 사용 가능한 폰트 목록

### Geist Sans (일반 Sans-serif 폰트)

-   `"Geist"` - Regular (400)
-   `"Geist Light"` - Light (300)
-   `"Geist Medium"` - Medium (500)
-   `"Geist Bold"` - Bold (700)

### Geist Mono (Monospace 폰트)

-   `"Geist Mono"` - Regular (400)
-   `"Geist Mono Light"` - Light (300)
-   `"Geist Mono Medium"` - Medium (500)
-   `"Geist Mono Bold"` - Bold (700)

## 사용 가능한 폰트 목록

### Geist Sans (일반 Sans-serif 폰트)

-   `"Geist"` - Regular (400)
-   `"Geist Light"` - Light (300)
-   `"Geist Medium"` - Medium (500)
-   `"Geist Bold"` - Bold (700)

### Geist Mono (Monospace 폰트)

-   `"Geist Mono"` - Regular (400)
-   `"Geist Mono Light"` - Light (300)
-   `"Geist Mono Medium"` - Medium (500)
-   `"Geist Mono Bold"` - Bold (700)

## 사용 가능한 폰트 목록

### Geist Sans (일반 Sans-serif 폰트)

-   `"Geist"` - Regular (400)
-   `"Geist Light"` - Light (300)
-   `"Geist Medium"` - Medium (500)
-   `"Geist Bold"` - Bold (700)

### Geist Mono (Monospace 폰트)

-   `"Geist Mono"` - Regular (400)
-   `"Geist Mono Light"` - Light (300)
-   `"Geist Mono Medium"` - Medium (500)
-   `"Geist Mono Bold"` - Bold (700)

## 사용 가능한 폰트 목록

### Geist Sans (일반 Sans-serif 폰트)

-   `"Geist"` - Regular (400)
-   `"Geist Light"` - Light (300)
-   `"Geist Medium"` - Medium (500)
-   `"Geist Bold"` - Bold (700)

### Geist Mono (Monospace 폰트)

-   `"Geist Mono"` - Regular (400)
-   `"Geist Mono Light"` - Light (300)
-   `"Geist Mono Medium"` - Medium (500)
-   `"Geist Mono Bold"` - Bold (700)

## 사용 가능한 폰트 목록

### Geist Sans (일반 Sans-serif 폰트)

-   `"Geist"` - Regular (400)
-   `"Geist Light"` - Light (300)
-   `"Geist Medium"` - Medium (500)
-   `"Geist Bold"` - Bold (700)

### Geist Mono (Monospace 폰트)

-   `"Geist Mono"` - Regular (400)
-   `"Geist Mono Light"` - Light (300)
-   `"Geist Mono Medium"` - Medium (500)
-   `"Geist Mono Bold"` - Bold (700)

## 사용 가능한 폰트 목록

### Geist Sans (일반 Sans-serif 폰트)

-   `"Geist"` - Regular (400)
-   `"Geist Light"` - Light (300)
-   `"Geist Medium"` - Medium (500)
-   `"Geist Bold"` - Bold (700)

### Geist Mono (Monospace 폰트)

-   `"Geist Mono"` - Regular (400)
-   `"Geist Mono Light"` - Light (300)
-   `"Geist Mono Medium"` - Medium (500)
-   `"Geist Mono Bold"` - Bold (700)

## 사용 가능한 폰트 목록

### Geist Sans (일반 Sans-serif 폰트)

-   `"Geist"` - Regular (400)
-   `"Geist Light"` - Light (300)
-   `"Geist Medium"` - Medium (500)
-   `"Geist Bold"` - Bold (700)

### Geist Mono (Monospace 폰트)

-   `"Geist Mono"` - Regular (400)
-   `"Geist Mono Light"` - Light (300)
-   `"Geist Mono Medium"` - Medium (500)
-   `"Geist Mono Bold"` - Bold (700)

## 사용 가능한 폰트 목록

### Geist Sans (일반 Sans-serif 폰트)

-   `"Geist"` - Regular (400)
-   `"Geist Light"` - Light (300)
-   `"Geist Medium"` - Medium (500)
-   `"Geist Bold"` - Bold (700)

### Geist Mono (Monospace 폰트)

-   `"Geist Mono"` - Regular (400)
-   `"Geist Mono Light"` - Light (300)
-   `"Geist Mono Medium"` - Medium (500)
-   `"Geist Mono Bold"` - Bold (700)

## 사용 가능한 폰트 목록

### Geist Sans (일반 Sans-serif 폰트)

-   `"Geist"` - Regular (400)
-   `"Geist Light"` - Light (300)
-   `"Geist Medium"` - Medium (500)
-   `"Geist Bold"` - Bold (700)

### Geist Mono (Monospace 폰트)

-   `"Geist Mono"` - Regular (400)
-   `"Geist Mono Light"` - Light (300)
-   `"Geist Mono Medium"` - Medium (500)
-   `"Geist Mono Bold"` - Bold (700)

## 사용 가능한 폰트 목록

### Geist Sans (일반 Sans-serif 폰트)

-   `"Geist"` - Regular (400)
-   `"Geist Light"` - Light (300)
-   `"Geist Medium"` - Medium (500)
-   `"Geist Bold"` - Bold (700)

### Geist Mono (Monospace 폰트)

-   `"Geist Mono"` - Regular (400)
-   `"Geist Mono Light"` - Light (300)
-   `"Geist Mono Medium"` - Medium (500)
-   `"Geist Mono Bold"` - Bold (700)

## 사용 가능한 폰트 목록

### Geist Sans (일반 Sans-serif 폰트)

-   `"Geist"` - Regular (400)
-   `"Geist Light"` - Light (300)
-   `"Geist Medium"` - Medium (500)
-   `"Geist Bold"` - Bold (700)

### Geist Mono (Monospace 폰트)

-   `"Geist Mono"` - Regular (400)
-   `"Geist Mono Light"` - Light (300)
-   `"Geist Mono Medium"` - Medium (500)
-   `"Geist Mono Bold"` - Bold (700)

## 사용 가능한 폰트 목록

### Geist Sans (일반 Sans-serif 폰트)

-   `"Geist"` - Regular (400)
-   `"Geist Light"` - Light (300)
-   `"Geist Medium"` - Medium (500)
-   `"Geist Bold"` - Bold (700)

### Geist Mono (Monospace 폰트)

-   `"Geist Mono"` - Regular (400)
-   `"Geist Mono Light"` - Light (300)
-   `"Geist Mono Medium"` - Medium (500)
-   `"Geist Mono Bold"` - Bold (700)

## 사용 가능한 폰트 목록

### Geist Sans (일반 Sans-serif 폰트)

-   `"Geist"` - Regular (400)
-   `"Geist Light"` - Light (300)
-   `"Geist Medium"` - Medium (500)
-   `"Geist Bold"` - Bold (700)

### Geist Mono (Monospace 폰트)

-   `"Geist Mono"` - Regular (400)
-   `"Geist Mono Light"` - Light (300)
-   `"Geist Mono Medium"` - Medium (500)
-   `"Geist Mono Bold"` - Bold (700)

## 사용 가능한 폰트 목록

### Geist Sans (일반 Sans-serif 폰트)

-   `"Geist"` - Regular (400)
-   `"Geist Light"` - Light (300)
-   `"Geist Medium"` - Medium (500)
-   `"Geist Bold"` - Bold (700)

### Geist Mono (Monospace 폰트)

-   `"Geist Mono"` - Regular (400)
-   `"Geist Mono Light"` - Light (300)
-   `"Geist Mono Medium"` - Medium (500)
-   `"Geist Mono Bold"` - Bold (700)

## 사용 가능한 폰트 목록

### Geist Sans (일반 Sans-serif 폰트)

-   `"Geist"` - Regular (400)
-   `"Geist Light"` - Light (300)
-   `"Geist Medium"` - Medium (500)
-   `"Geist Bold"` - Bold (700)

### Geist Mono (Monospace 폰트)

-   `"Geist Mono"` - Regular (400)
-   `"Geist Mono Light"` - Light (300)
-   `"Geist Mono Medium"` - Medium (500)
-   `"Geist Mono Bold"` - Bold (700)

## 사용 가능한 폰트 목록

### Geist Sans (일반 Sans-serif 폰트)

-   `"Geist"` - Regular (400)
-   `"Geist Light"` - Light (300)
-   `"Geist Medium"` - Medium (500)
-   `"Geist Bold"` - Bold (700)

### Geist Mono (Monospace 폰트)

-   `"Geist Mono"` - Regular (400)
-   `"Geist Mono Light"` - Light (300)
-   `"Geist Mono Medium"` - Medium (500)
-   `"Geist Mono Bold"` - Bold (700)

## 사용 가능한 폰트 목록

### Geist Sans (일반 Sans-serif 폰트)

-   `"Geist"` - Regular (400)
-   `"Geist Light"` - Light (300)
-   `"Geist Medium"` - Medium (500)
-   `"Geist Bold"` - Bold (700)

### Geist Mono (Monospace 폰트)

-   `"Geist Mono"` - Regular (400)
-   `"Geist Mono Light"` - Light (300)
-   `"Geist Mono Medium"` - Medium (500)
-   `"Geist Mono Bold"` - Bold (700)

## 사용 가능한 폰트 목록

### Geist Sans (일반 Sans-serif 폰트)

-   `"Geist"` - Regular (400)
-   `"Geist Light"` - Light (300)
-   `"Geist Medium"` - Medium (500)
-   `"Geist Bold"` - Bold (700)

### Geist Mono (Monospace 폰트)

-   `"Geist Mono"` - Regular (400)
-   `"Geist Mono Light"` - Light (300)
-   `"Geist Mono Medium"` - Medium (500)
-   `"Geist Mono Bold"` - Bold (700)

## 사용 가능한 폰트 목록

### Geist Sans (일반 Sans-serif 폰트)

-   `"Geist"` - Regular (400)
-   `"Geist Light"` - Light (300)
-   `"Geist Medium"` - Medium (500)
-   `"Geist Bold"` - Bold (700)

### Geist Mono (Monospace 폰트)

-   `"Geist Mono"` - Regular (400)
-   `"Geist Mono Light"` - Light (300)
-   `"Geist Mono Medium"` - Medium (500)
-   `"Geist Mono Bold"` - Bold (700)

## 사용 가능한 폰트 목록

### Geist Sans (일반 Sans-serif 폰트)

-   `"Geist"` - Regular (400)
-   `"Geist Light"` - Light (300)
-   `"Geist Medium"` - Medium (500)
-   `"Geist Bold"` - Bold (700)

### Geist Mono (Monospace 폰트)

-   `"Geist Mono"` - Regular (400)
-   `"Geist Mono Light"` - Light (300)
-   `"Geist Mono Medium"` - Medium (500)
-   `"Geist Mono Bold"` - Bold (700)

## 사용 예제

### 타이틀과 본문에 다른 폰트 적용

```xml
<VBox>
  <!-- 타이틀: Geist Sans Bold -->
  <Label text="Application Title" styleClass="geist-sans-bold">
    <font>
      <Font size="24.0"/>
    </font>
  </Label>

  <!-- 본문: Geist Sans Regular -->
  <Label text="Application description text" styleClass="geist-sans">
    <font>
      <Font size="14.0"/>
    </font>
  </Label>

  <!-- 코드 영역: Geist Mono (기본값) -->
  <TextArea text="Code here..." />
</VBox>
```

### 버튼에 다른 폰트 적용

```xml
<Button text="Click Me" styleClass="geist-sans-medium">
  <font>
    <Font size="16.0"/>
  </font>
</Button>
```

## CSS 커스터마이징

필요에 따라 `styles.css`에 추가 클래스를 정의할 수 있습니다:

```css
/* 큰 타이틀용 */
.title-large {
    -fx-font-family: "Geist Bold", sans-serif;
    -fx-font-size: 32px;
}

/* 작은 캡션용 */
.caption-small {
    -fx-font-family: "Geist Light", sans-serif;
    -fx-font-size: 12px;
    -fx-text-fill: #666666;
}

/* 코드 블록용 */
.code-block {
    -fx-font-family: "Geist Mono", monospace;
    -fx-font-size: 14px;
    -fx-background-color: #f5f5f5;
    -fx-padding: 10px;
}
```

## 폰트 조합 추천

### UI 텍스트 (일반적인 경우)

-   **타이틀/헤더**: Geist Sans Bold
-   **본문/설명**: Geist Sans Regular
-   **캡션/보조**: Geist Sans Light

### 코드/데이터 표시

-   **코드 에디터**: Geist Mono Regular
-   **로그/터미널**: Geist Mono Regular
-   **데이터 테이블**: Geist Mono Regular

### 혼합 사용

```xml
<VBox>
  <!-- 섹션 제목 -->
  <Label text="Configuration" styleClass="geist-sans-bold"/>

  <!-- 설명 텍스트 -->
  <Label text="Edit your settings below" styleClass="geist-sans"/>

  <!-- 설정값 (코드) -->
  <TextField text="config.json" />  <!-- 기본 Geist Mono 적용 -->
</VBox>
```

## 참고사항

-   JavaFX는 CSS의 `font-weight` 속성을 완전히 지원하지 않으므로, 각 weight를 별도의 font-family로 정의했습니다.
-   폰트 파일은 `src/main/resources/fonts/` 디렉토리에 있습니다.
-   빌드 시 모든 폰트 파일이 자동으로 JAR에 포함됩니다.
-   폰트 로딩 실패 시 시스템 기본 폰트로 fallback됩니다.

## 추가 정보

-   **Geist 폰트 공식 저장소**: https://github.com/vercel/geist-font
-   **라이선스**: SIL Open Font License 1.1
-   **설치 가이드**: [FONT_SETUP.md](FONT_SETUP.md)
