# 말씀카드 (WordCard)

> 가독성 높은 성경 읽기 + 좋아하는 구절을 예쁜 이미지 카드로 친구에게 공유하는 앱.

Kotlin Compose Multiplatform · Clean Architecture · iOS 우선 (Android/Desktop 확장 가능)

## 핵심 기능 (MVP)
- 성경 본문 읽기 (책/장 탐색, 부드러운 스크롤)
- 구절 탭으로 단일/다중 선택
- 라이트 / 세피아 / 다크 테마, 글자 크기 슬라이더
- **공유 카드 생성**: 6가지 배경 프리셋, 한 번의 탭으로 시스템 공유 시트 호출

## 아키텍처
```
presentation  →  domain  ←  data
   (Compose UI/         (Repository
    ViewModel)           구현, JSON
                         DataSource)
```
- `domain`은 어떤 외부 라이브러리/플랫폼에도 의존하지 않음 (순수 Kotlin).
- `data`는 `domain`이 정의한 인터페이스를 구현.
- `presentation`은 `domain`의 UseCase를 통해서만 데이터에 접근.
- 단방향 의존성, Compose `StateFlow` 기반 상태 흐름.

## 모듈/디렉터리
```
bible_app/
├── docs/PRD.md
├── settings.gradle.kts
├── build.gradle.kts
├── gradle/libs.versions.toml
├── composeApp/
│   ├── build.gradle.kts
│   └── src/
│       ├── commonMain/kotlin/com/wordcard/app/
│       │   ├── App.kt
│       │   ├── di/AppModule.kt
│       │   ├── domain/{model,repository,usecase}
│       │   ├── data/{model,mapper,source,repository}
│       │   └── presentation/{theme,common,reader,share,settings}
│       ├── androidMain/   (Activity + ImageSharer.actual + Manifest)
│       └── iosMain/       (MainViewController + ImageSharer.actual)
└── iosApp/iosApp/         (Swift 진입점, Info.plist)
```

## 빌드 / 실행

### 사전 준비
- macOS, Xcode 15+, JDK 17+
- (Android용) Android Studio Iguana / Koala

### iOS
```bash
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
open iosApp/iosApp.xcodeproj   # Xcode 프로젝트는 첫 빌드 시 만들어주세요
```
또는 Android Studio의 "Run iOS" 구성 사용.

### Android
```bash
./gradlew :composeApp:installDebug
```

## 데이터
- 본문: **개역한글(KRV)** 66권 전체. 공개 도메인.
- 번들 위치: `composeApp/src/commonMain/composeResources/files/krv.json` (Compose Resources, 모든 타깃 공통)
- 데이터 소스: `KrvBibleDataSource` → `BibleRepositoryImpl`이 Mutex 캐싱
- 라이선스/출처: [`NOTICES.md`](./NOTICES.md)

### KRV 데이터 재생성
```bash
python3 build-scripts/build_krv.py
```
getBible v2 API에서 1189개 장을 fetch해 `krv.json`을 갱신합니다. `build-scripts/.cache/`(gitignore)에 응답을 캐시하므로 재실행 시 네트워크를 다시 타지 않습니다.

## 다음 단계 (M2)
- [ ] 북마크/하이라이트 영구 저장 (Room/SQLDelight)
- [ ] 검색 (책/구절 키워드, FTS)
- [ ] 오늘의 말씀 알림
- [ ] 다역본(KJV/개역개정) 추가 — `BibleFileDto.version` 필드로 확장 가능
- [ ] 카드 폰트/레이아웃 커스터마이즈
