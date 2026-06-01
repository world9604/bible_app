# 장별 AI 해설 사전 생성

이 스크립트는 성경 66권 1189장 전체에 대해 Claude Opus 4.7을 사용하여
"해설 + Q&A" 데이터를 한 번에 생성하고, 그 결과를 앱에 번들로 포함시키기
위한 단일 JSON 파일로 만들어 둡니다. 앱은 런타임에 Anthropic API를 호출하지
않습니다 — 첫 실행 시 번들된 JSON을 SQLite에 시드하고 이후로는 로컬 DB에서만 읽어옵니다.

## 준비

```bash
pip install anthropic
export ANTHROPIC_API_KEY=sk-ant-...
```

## 실행

```bash
# 전체 1189장 생성 (수 시간 소요)
python3 build-scripts/generate_commentaries.py

# 한 권만 미리 돌려보기 (요금/품질 확인용, bundle은 갱신 안 함)
python3 build-scripts/generate_commentaries.py --book GEN

# 한 장만
python3 build-scripts/generate_commentaries.py --book GEN --chapter 1
```

## 동작 방식

- 모든 장은 동시에 6개씩 처리되고 (`CONCURRENCY = 6`) 실패 시 지수 백오프로 재시도합니다.
- 장 단위 결과는 `build-scripts/.commentary_cache/{BOOK}_{CHAPTER}.json`에 저장됩니다.
  스크립트를 중단했다 다시 돌리면 이미 끝난 장은 건너뜁니다.
- 전체 실행에서 모든 장이 성공해야만 최종 번들
  `composeApp/src/commonMain/composeResources/files/commentaries.json` 이 갱신됩니다.

## 비용 추정

- 입력: 장당 평균 ~1000~3000 토큰 (시편 119편 같은 긴 장은 더 많음).
- 출력: 장당 ~1500 토큰 목표 (해설 + Q&A 4개).
- Opus 4.7 가격: 입력 $15/MTok, 출력 $75/MTok.
- 전체 1189장 ≈ **$130~$160** (한 번만 지불하면 앱은 영구 무료 사용).

## 결과 사용

번들 JSON이 만들어지면 그대로 앱을 빌드/배포하면 됩니다. 앱은:

1. 첫 실행 시 `commentaries.json` 을 읽어 SQLite로 시드합니다.
2. 이후로는 `ChapterCommentaryRepository`가 SQL로만 데이터를 반환합니다.
3. 사용자가 다른 장을 열 때마다 화면 하단에 해설과 Q&A가 표시됩니다.

## 일부만 재생성하고 싶을 때

특정 책/장의 품질이 마음에 안 들면 해당 캐시 파일만 삭제하고 다시 실행하면
그 부분만 재생성됩니다:

```bash
rm build-scripts/.commentary_cache/GEN_001.json
python3 build-scripts/generate_commentaries.py
```
