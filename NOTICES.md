# Notices

This app bundles third-party content. The notices below identify each item, its source, and its license.

## Bible text — 개역한글 (Korean Revised Version, KRV)

- 역본명: 개역한글 (Korean Revised Version, 1961)
- 약어: KRV
- 언어: 한국어
- 라이선스: 공개 도메인 (Public Domain)
- 출처: getBible.net v2 API — `https://api.getbible.net/v2/korean/{book}/{chapter}.json`
- 통합 방식: `build-scripts/build_krv.py`로 1회 fetch + 검증 후 `composeApp/src/commonMain/composeResources/files/krv.json`에 번들

## Fonts

- **Noto Sans KR** — © Google. SIL Open Font License 1.1.
- **부크크 명조 (Bookk Myungjo)** — © 부크크. 무료 상업 이용 가능 (자세한 조건은 부크크 폰트 라이선스 참고).
- **Prestige Elite Std** — © Adobe. 라이선스 별도 확인 필요.
- **Material Symbols Outlined** — © Google. Apache License 2.0.

## 데이터 재생성 방법

```
python3 build-scripts/build_krv.py
```

스크립트는 `build-scripts/.cache/`에 chapter 단위 응답을 캐시하므로 재실행 시 네트워크를 다시 타지 않습니다. 새로 받으려면 `.cache/`를 삭제하세요.
