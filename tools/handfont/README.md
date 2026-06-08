# 손글씨 폰트 (Sonsulssi Handwriting)

`references/성경 어플 /1.jpg` 의 파란 종이 손글씨를 인식해 만든 한글 TrueType 폰트입니다.
나중에 사용자가 고른 성경 구절을 이미지 카드로 만들 때 쓸 용도입니다.

## 결과물 (`out/`)
- **SonsulssiHandwriting-Regular.ttf** — 폰트 본체 (이걸 앱에 넣으면 됩니다)
- specimen.png — 폰트에 들어간 글자 견본
- demo_card.png — 원문을 이 폰트로 렌더링한 데모 카드

## 수록 범위 (중요)
한글 완성형은 11,172자가 필요하지만, 사진 한 장에는 고유 음절이 ~65개뿐입니다.
그래서 **이 이미지에 등장한 64개 음절만** 손글씨로 렌더링됩니다. 그 외 글자는 빈칸(.notdef)으로 나옵니다.

수록된 64자:
```
가 갖 거 것 게 교 기 나 남 내 는 다 답 대 도 동 두 들 람 랑
로 를 릇 리 만 모 민 부 비 사 산 세 숨 시 않 양 에 여 연 요
용 워 은 을 의 이 인 자 정 조 좋 중 지 진 징 킨 특 피 하 학
함 해 허 히
```
+ 마침표(.), 공백

품질 때문에 자동 제외한 글자: **고, 과** (원본에서 한 번씩만 나오는데 옆 글자와 붙어 깨끗하게 분리되지 않음).

## 한계
- 손글씨를 그대로 떠온 것이라 글자마다 크기·기울기가 들쭉날쭉합니다(원본 충실 = 손맛). 큰 카드에선 붓글씨 느낌으로 자연스럽습니다.
- 자모 조합형이 아니라 **음절 통째 매핑**입니다. 위 64자 조합 밖의 텍스트는 표현 못 합니다.

## 파이프라인 (재생성 / 글자 추가)
의존성은 `pylibs/`(fontTools, scipy)에 로컬 설치돼 있습니다.
```bash
export PYTHONPATH=tools/handfont/pylibs
python3 tools/handfont/seg4.py        # 1.jpg → 음절 분리 → glyphs/*.png, glyphs.json
python3 tools/handfont/build_font.py  # glyphs → out/SonsulssiHandwriting-Regular.ttf
```
- `seg4.py` : 잉크 이진화 → 8줄 분리(연결요소 cy k-means) → 띄어쓰기(큰 공백)로 단어 분리 → 단어 안에서 음절 수만큼 분할.
  전사(transcription)는 `SLINES` 리스트에 있습니다. 인식이 틀린 글자가 있으면 여기서 고치면 됩니다.
- `build_font.py` : 글자마다 가장 깨끗한 인스턴스 선택(병합·얇은 조각·검은 덩어리 배제) →
  비트맵을 LANCZOS 확대·블러로 매끈하게 → 외곽선 추적 → Douglas-Peucker 단순화 → fontTools로 TTF 조립.
  `DROP`(품질 임계값), `TARGET_ASC`, `SB`(자간) 등으로 튜닝.

## 글자를 더 늘리려면
같은 손글씨로 빠진 음절(예: 사랑, 믿음, 소망, 은혜 …)을 더 써서 사진을 추가하고,
`seg4.py`의 `SLINES`에 그 줄의 전사를 넣은 뒤 두 스크립트를 다시 돌리면 폰트가 확장됩니다.
