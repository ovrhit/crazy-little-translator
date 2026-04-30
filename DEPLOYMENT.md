# Galaxy S25 배포 및 설정 가이드

이 가이드는 개발된 '실시간 오버레이 번역기'를 Galaxy S25 기기에서 실제로 구동하고 테스트하기 위한 절차를 설명합니다.

## 1. Git Remote 업로드 방법

프로젝트를 GitHub 등의 원격 저장소에 업로드하려면 다음 명령어를 터미널에서 실행하세요.

```bash
# 1. 로컬 저장소 초기화 및 파일 스테이징
git init
git add .

# 2. 첫 커밋 생성
git commit -m "Initial commit: Real-time Overlay Translator with AI Persona"

# 3. 원격 저장소 연결 (GITHUB_URL을 실제 주소로 변경)
git remote add origin https://github.com/사용자이름/crazy-little-translator.git

# 4. 업로드
git push -u origin main
```
*주의: `app/src/main/java/com/example/crazytranslator/Secret.kt`에 API 키를 입력했다면, 보안을 위해 해당 파일을 `.gitignore`에 추가하거나 키를 제거 후 업로드하세요.*

## 2. APK 빌드 및 배포

Android Studio가 설치되어 있다면 다음 단계를 따르세요.

1. **빌드 메뉴:** `Build` > `Build Bundle(s) / APK(s)` > `Build APK(s)` 선택.
2. **APK 위치:** 빌드가 완료되면 오른쪽 하단 알림의 `locate`를 클릭합니다. (통상 `app/build/outputs/apk/debug/app-debug.apk`에 위치)
3. **기기 전송:** 해당 APK 파일을 카카오톡 나에게 보내기, Google Drive, 혹은 USB 케이블을 통해 Galaxy S25로 전송합니다.

## 3. Galaxy S25 기기 설정 (중요)

앱이 정상 작동하려면 기기에서 다음 설정이 반드시 필요합니다.

### A. 개발자 옵션 및 USB 디버깅 활성화
Android Studio에서 직접 실행(Run)하여 테스트하려면 필요합니다.
1. `설정` > `휴대폰 정보` > `소프트웨어 정보` > **'빌드 번호'**를 7번 연속 클릭.
2. `설정` 메인 화면 하단에 생긴 `개발자 옵션` 진입.
3. **'USB 디버깅'** 활성화.

### B. 앱 권한 허용 (실행 시 필수)
앱을 설치하고 처음 실행할 때 다음 두 가지를 설정해야 합니다.
1. **다른 앱 위에 표시 (Overlay Permission):** 앱 내 버튼을 누르면 설정 화면으로 이동합니다. 목록에서 'Crazy Little Translator'를 찾아 **'허용'**으로 바꿉니다.
2. **화면 녹화/전송 시작:** 'Start Translation Overlay' 버튼을 누르면 "Crazy Little Translator에서 화면에 표시되는 모든 내용을 캡처합니다"라는 시스템 팝업이 뜹니다. 이때 **'지금 시작'**을 눌러야 OCR이 작동합니다.

### C. 절전 예외 설정 (장시간 사용 시)
Galaxy 기기는 백그라운드 서비스를 강제 종료할 수 있습니다.
1. `설정` > `애플리케이션` > `Crazy Little Translator` 선택.
2. `배터리` > **'제한 없음(Unrestricted)'** 선택.

## 4. 기기 특이사항 (Galaxy S25)
- **에지 패널:** 오버레이 버튼이 에지 패널과 겹칠 수 있으니 위치를 조정하세요.
- **화면 해상도:** 설정에서 화면 해상도(QHD+ 등)를 변경할 경우 앱을 재시작하는 것이 좋습니다.
