# 성경 앱 PRD (Product Requirements Document)

## 1. 제품 개요
- **이름(가칭)**: 말씀카드 (WordCard)
- **플랫폼**: iOS (1차), Android/Desktop (확장 가능) — Kotlin Compose Multiplatform
- **한 줄 정의**: 가독성 높은 성경 읽기 + 마음에 드는 구절을 예쁜 이미지 카드로 만들어 친구에게 바로 공유할 수 있는 앱.
- **타깃 사용자**: 매일 성경을 읽고 묵상하며, SNS/카카오톡으로 좋아하는 구절을 친구·가족과 나누는 20~50대.

## 2. 핵심 가치 (Why)
1. **읽기 쉬움** — 큰 글자, 넉넉한 행간, 라이트/다크/세피아 테마, 본문 폭 제어.
2. **공유가 쉬움** — 구절을 길게 누르면 즉시 카드가 생성되고, 한 번의 탭으로 시스템 공유 시트로 이미지 전송.
3. **방해 요소 최소화** — 광고/팝업/회원가입 없음. 첫 실행에서 바로 본문이 보임.

## 3. 핵심 기능 (MVP)
| 우선순위 | 기능 | 설명 |
|---|---|---|
| P0 | 성경 본문 읽기 | 책 → 장 → 절 단위 탐색, 스크롤 읽기 |
| P0 | 구절 선택 | 길게 눌러 단일/복수 절 선택 |
| P0 | 이미지 카드 생성 | 선택한 구절을 배경 테마와 함께 카드로 렌더링 |
| P0 | 시스템 공유 | iOS UIActivityViewController로 이미지 + 텍스트 공유 |
| P1 | 테마 전환 | 라이트/다크/세피아 + 글자 크기 슬라이더 |
| P1 | 북마크 | 좋아하는 구절 저장 |
| P2 | 검색 | 책/구절 키워드 검색 |
| P2 | 오늘의 말씀 | 매일 1구절 추천 |

## 4. 사용자 여정 (Golden Path)
1. 앱 실행 → 마지막 읽던 위치(또는 창세기 1장)로 이동.
2. 손가락으로 스크롤하며 읽음.
3. 마음에 드는 절을 **길게 누름** → 선택됨(하이라이트).
4. 인접한 절을 **탭**해서 추가 선택 가능.
5. 하단 플로팅 바의 **"카드 만들기"** 탭 → 미리보기 화면.
6. 배경(은은한 그라디언트/단색) 선택 → **공유** 버튼.
7. 시스템 공유 시트 → 카카오톡/메시지/인스타 스토리 등으로 전송.

## 5. 화면 구조
```
[Splash]
   └─ [Reader]  ← 메인. 좌상단: 책/장 선택, 우상단: 설정(테마/글자크기)
        ├─ [BookPicker]  바텀시트로 슬라이딩
        ├─ [ChapterPicker] 그리드
        └─ [ShareCard]   풀스크린 다이얼로그
              └─ [SystemShareSheet]
```

## 6. 디자인 가이드
- **Typography**: 본문은 Sans-serif 16~22sp 가변, 행간 1.7. 카드 미리보기는 세리프(Serif) 강조.
- **Color**:
  - Light: 배경 #FBF9F4 (아이보리), 텍스트 #1F1B16
  - Dark: 배경 #14110E, 텍스트 #ECE5D8
  - Sepia: 배경 #F4ECD8, 텍스트 #5B4636
- **Card 배경 프리셋**: 8개 (단색 4 + 그라디언트 4)
- **Motion**: 절 선택 시 80ms 페이드 + 스케일 1.02. 카드 화면 진입은 셰어드 트랜지션.

## 7. 데이터
- **버전**: 한국어 개역개정(샘플). 라이선스 사유로 MVP 단계는 공개 도메인(KJV) + 샘플 한국어 구절 일부 동봉.
- **포맷**: 앱 내부 리소스 JSON.
  ```json
  {"book":"GEN","name":"창세기","chapters":[
    {"chapter":1,"verses":[{"v":1,"t":"태초에 하나님이..."}]}
  ]}
  ```

## 8. 아키텍처 (클린 아키텍처)
```
presentation  →  domain  ←  data
(Compose UI/  (UseCase /  (Repository
 ViewModel)    Entity /    impl, JSON
               Repo IF)    DataSource)
```
- **단방향 의존성**: domain은 무엇에도 의존하지 않음.
- **상태**: `StateFlow` + Compose `collectAsState()`.
- **DI**: Koin Multiplatform.

## 9. 모듈 구조
```
bible_app/
├── composeApp/
│   ├── commonMain/kotlin/com/wordcard/app/
│   │   ├── domain/{model,repository,usecase}
│   │   ├── data/{model,source,repository,mapper}
│   │   ├── presentation/{reader,share,picker,theme,common}
│   │   └── di/
│   ├── commonMain/composeResources/
│   ├── iosMain/         (expect actual: ImageSharer)
│   └── androidMain/     (선택적 확장)
└── iosApp/iosApp/       (Swift 진입점)
```

## 10. 비기능 요구사항
- 콜드 스타트 < 1.5s (iPhone 12 기준)
- 60fps 스크롤 유지 (장당 평균 30절 기준)
- 오프라인 100% 동작 (네트워크 불필요)
- 접근성: VoiceOver 라벨, Dynamic Type 일부 대응

## 11. 비범위 (Out of Scope)
- 회원/계정/클라우드 동기화
- 결제, 광고
- 다국어(i18n) — 초기엔 한글/영문만
- 영상/음성 성경

## 12. 마일스톤
- M1 (이번 작업): 프로젝트 골격 + 클린 아키텍처 레이어 + Reader 화면 + ShareCard 동작 + 샘플 데이터.
- M2: 북마크/검색/테마 슬라이더.
- M3: 한글 정식 데이터 + 오늘의 말씀.
