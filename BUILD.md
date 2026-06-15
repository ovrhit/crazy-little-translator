# 빌드 가이드 (Android Studio)

## 사용 스택 (정렬 완료)

| 구성 | 버전 |
|---|---|
| Gradle | 8.7 |
| Android Gradle Plugin (AGP) | 8.5.2 |
| Kotlin | 2.2.0 |
| Compose Compiler | `org.jetbrains.kotlin.plugin.compose` 2.2.0 (Kotlin 버전에 연동) |
| compileSdk / targetSdk | 34 |
| minSdk | 26 |
| JDK (Gradle 실행용) | 17 또는 21 |

> ⚠️ Kotlin은 **2.2.0** 이상이어야 합니다. on-device AI 라이브러리(`com.google.mlkit:genai-prompt`)가 Kotlin 2.2로 컴파일돼 있어, 1.9.x로는 메타데이터를 못 읽어 빌드가 깨집니다.

## Android Studio에서 빌드하는 법

1. **프로젝트 열기**: `File > Open` → 이 폴더(`crazy-little-translator`) 선택. (폴더를 옮겼어도 `.git`만 같이 있으면 git 연동은 그대로 유지됩니다.)

2. **Gradle JDK 확인** — 가장 중요:
   - `Settings > Build, Execution, Deployment > Build Tools > Gradle`
   - **Gradle JDK**를 `jbr-17` 또는 `jbr-21` (Android Studio 번들 JBR)로 지정.
   - 시스템에 설치된 JDK 20은 쓰지 마세요. AGP는 JDK 17/21을 권장합니다.

3. **SDK**: `local.properties`의 `sdk.dir`가 본인 SDK 경로를 가리키는지 확인. (이 파일은 git에 올리지 않음)
   - 필요한 SDK Platform: **Android 14 (API 34)**.

4. **Sync & Build**:
   - 코끼리 아이콘(Sync Project with Gradle Files) 클릭 → 의존성 다운로드.
   - `Build > Make Project` (Ctrl+F9) 또는 ▶ Run.

5. **커맨드라인으로도 가능**:
   ```powershell
   $env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
   .\gradlew.bat :app:assembleDebug
   ```
   결과물: `app/build/outputs/apk/debug/app-debug.apk`

## 실행 / 테스트 시 주의 (중요)

이 앱은 **on-device Gemini Nano (ML Kit GenAI Prompt API)** 를 씁니다. 실제 번역이 동작하려면:

- **AICore 지원 기기**가 필요합니다: Pixel 8/8 Pro/9 계열, Galaxy S24+ 등 (Android 14+, AICore + Private Compute Core 탑재).
- 그 외 기기/에뮬레이터에서는 AI 상태가 `UNAVAILABLE`로 뜨고 번역이 안 됩니다 (빌드/설치는 정상).
- **에뮬레이터는 권장하지 않음**: AICore가 없고, 화면 캡처(MediaProjection) + 오버레이 동작도 실기기에서 확인하는 게 정확합니다.
- 최초 사용 시 모델 다운로드(`DOWNLOADABLE` → 자동 다운로드)가 필요할 수 있습니다.

런타임 권한(앱 내 버튼 순서대로):
1. **다른 앱 위에 표시(SYSTEM_ALERT_WINDOW)** 권한 허용
2. **화면 캡처(MediaProjection)** 허용 → 오버레이 번역 시작

## AI 백엔드

이 앱은 **on-device Gemini Nano만** 사용합니다. 클라우드 Gemini API 연결은 쓰지 않으며,
API 키를 담고 있던 `Secret.kt`는 제거했습니다. (해당 파일은 `.gitignore`로 제외돼 있어 git에 올라간 적 없음)
