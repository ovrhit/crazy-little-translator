# Crazy Little Translator

화면에 보이는 텍스트를 **실시간으로 캡처 → OCR → 번역**해서 원래 위치 위에 겹쳐 보여주는 안드로이드 오버레이 번역기입니다.
번역은 **온디바이스 Gemini Nano**(ML Kit GenAI Prompt API)로 처리하며, 단순 직역이 아니라 **캐릭터 말투(페르소나)** 까지 입혀서 자연스러운 한국어로 옮깁니다.

> 게임·만화·영상 등 자막이 없는 화면의 일본어/영어 텍스트를, 생동감 있는 실제 캐릭터 같은 말투로 번역해 보세요.

## 주요 기능

- **실시간 화면 오버레이 번역** — `MediaProjection`으로 화면을 캡처하고, 번역 결과를 원문 위치에 오버레이로 표시
- **온디바이스 AI** — 네트워크 없이 기기 안에서 동작 (Gemini Nano), API 키·서버 불필요
- **캐릭터 페르소나** — 기본(존댓말) / 츤데레 / 선비 / 친근한 친구 / 직접 입력 중 선택, 말투를 살려 번역
- **문맥 인식** — 화면 전체 텍스트를 컨텍스트로 함께 전달해 등장인물·관계·어조를 추론
- **성능 최적화** — OCR 결과가 이전 프레임과 같으면 재번역 생략(캐싱), 비트맵 즉시 `recycle()`, 캡처 루프는 백그라운드 스레드에서 처리

## 동작 방식

```
화면 캡처(MediaProjection) → ML Kit OCR(텍스트 블록) → Gemini Nano 번역(페르소나 프롬프트) → 오버레이 렌더링(Compose)
```

## 기술 스택

- **언어/UI**: Kotlin 2.2.0, Jetpack Compose (MVVM)
- **빌드**: Gradle 8.7, AGP 8.5.2 (compileSdk 34 / minSdk 26)
- **AI**: ML Kit GenAI Prompt API (`com.google.mlkit:genai-prompt`, 온디바이스 Gemini Nano)
- **OCR**: ML Kit Text Recognition
- **기타**: DataStore(페르소나 영속화), Coroutines, Lifecycle/Foreground Service

## 요구 사항 (중요)

온디바이스 Gemini Nano는 **AICore를 지원하는 기기에서만** 실제로 동작합니다.

- 지원 기기 예: Pixel 8 / 8 Pro / 9 계열, Galaxy S24+ 등 (Android 14+, AICore + Private Compute Core 탑재)
- 그 외 기기·에뮬레이터에서는 설치·실행은 되지만 AI 상태가 `UNAVAILABLE`로 표시되고 번역은 동작하지 않습니다.

## 빌드 & 실행

자세한 절차는 [BUILD.md](BUILD.md)를 참고하세요. 요약:

```powershell
# Android Studio: Gradle JDK를 jbr-17 또는 jbr-21로 지정 후 Sync & Run
# 커맨드라인:
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat :app:assembleDebug
# → app/build/outputs/apk/debug/app-debug.apk
```

## 사용법

앱 실행 후 버튼 순서대로:

1. **다른 앱 위에 표시(Overlay) 권한 허용**
2. **화면 캡처(MediaProjection) 시작** → 오버레이 번역 시작
3. 상단에서 **페르소나 프리셋 선택** (또는 직접 입력)

기기별 설정(권한, 절전 예외 등)은 [DEPLOYMENT.md](DEPLOYMENT.md)를 참고하세요.

## 문서

- [BUILD.md](BUILD.md) — 빌드 환경/스택 버전, Android Studio 설정
- [DEPLOYMENT.md](DEPLOYMENT.md) — 실기기 배포 및 기기 설정 가이드
- [RULES.md](RULES.md) — 개발 원칙(성능·아키텍처·페르소나·예외 처리)
