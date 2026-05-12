package com.wordcard.app.data.source

import com.wordcard.app.data.model.BibleBookDto
import com.wordcard.app.data.model.ChapterDto
import com.wordcard.app.data.model.VerseDto

/**
 * 프리뷰/테스트 전용 인메모리 데이터 (6권 부분 발췌).
 * 실제 앱은 [KrvBibleDataSource]를 사용한다 — DI 바인딩은 [com.wordcard.app.di.AppModule] 참고.
 * Compose Preview에서 리소스 접근이 어려운 경우 임시로 이 소스를 주입할 수 있다.
 */
class SampleBibleDataSource : BibleDataSource {

    override suspend fun loadAll(): List<BibleBookDto> = books

    private val books: List<BibleBookDto> = listOf(
        BibleBookDto(
            id = "GEN",
            name = "창세기",
            abbr = "창",
            testament = "OLD",
            chapters = listOf(
                ChapterDto(
                    n = 1,
                    verses = listOf(
                        VerseDto(1, "태초에 하나님이 천지를 창조하시니라."),
                        VerseDto(2, "땅이 혼돈하고 공허하며 흑암이 깊음 위에 있고 하나님의 영은 수면 위에 운행하시니라."),
                        VerseDto(3, "하나님이 이르시되 빛이 있으라 하시니 빛이 있었고"),
                        VerseDto(4, "빛이 하나님이 보시기에 좋았더라 하나님이 빛과 어둠을 나누사"),
                        VerseDto(5, "하나님이 빛을 낮이라 부르시고 어둠을 밤이라 부르시니라 저녁이 되고 아침이 되니 이는 첫째 날이니라."),
                        VerseDto(6, "하나님이 이르시되 물 가운데에 궁창이 있어 물과 물로 나뉘라 하시고"),
                        VerseDto(7, "하나님이 궁창을 만드사 궁창 아래의 물과 궁창 위의 물로 나뉘게 하시니 그대로 되니라."),
                        VerseDto(8, "하나님이 궁창을 하늘이라 부르시니라 저녁이 되고 아침이 되니 이는 둘째 날이니라."),
                        VerseDto(9, "하나님이 이르시되 천하의 물이 한 곳으로 모이고 뭍이 드러나라 하시니 그대로 되니라."),
                        VerseDto(10, "하나님이 뭍을 땅이라 부르시고 모인 물을 바다라 부르시니 하나님이 보시기에 좋았더라."),
                    )
                ),
                ChapterDto(
                    n = 2,
                    verses = listOf(
                        VerseDto(1, "천지와 만물이 다 이루어지니라."),
                        VerseDto(2, "하나님이 그가 하시던 일을 일곱째 날에 마치시니 그가 하시던 모든 일을 그치고 일곱째 날에 안식하시니라."),
                        VerseDto(3, "하나님이 그 일곱째 날을 복되게 하사 거룩하게 하셨으니 이는 하나님이 그 창조하시며 만드시던 모든 일을 마치시고 그 날에 안식하셨음이니라."),
                    )
                ),
            )
        ),
        BibleBookDto(
            id = "PSA",
            name = "시편",
            abbr = "시",
            testament = "OLD",
            chapters = listOf(
                ChapterDto(
                    n = 23,
                    verses = listOf(
                        VerseDto(1, "여호와는 나의 목자시니 내게 부족함이 없으리로다."),
                        VerseDto(2, "그가 나를 푸른 풀밭에 누이시며 쉴 만한 물 가로 인도하시는도다."),
                        VerseDto(3, "내 영혼을 소생시키시고 자기 이름을 위하여 의의 길로 인도하시는도다."),
                        VerseDto(4, "내가 사망의 음침한 골짜기로 다닐지라도 해를 두려워하지 않을 것은 주께서 나와 함께 하심이라 주의 지팡이와 막대기가 나를 안위하시나이다."),
                        VerseDto(5, "주께서 내 원수의 목전에서 내게 상을 차려 주시고 기름을 내 머리에 부으셨으니 내 잔이 넘치나이다."),
                        VerseDto(6, "내 평생에 선하심과 인자하심이 반드시 나를 따르리니 내가 여호와의 집에 영원히 살리로다."),
                    )
                ),
                ChapterDto(
                    n = 1,
                    verses = listOf(
                        VerseDto(1, "복 있는 사람은 악인들의 꾀를 따르지 아니하며 죄인들의 길에 서지 아니하며 오만한 자들의 자리에 앉지 아니하고"),
                        VerseDto(2, "오직 여호와의 율법을 즐거워하여 그의 율법을 주야로 묵상하는도다."),
                        VerseDto(3, "그는 시냇가에 심은 나무가 철을 따라 열매를 맺으며 그 잎사귀가 마르지 아니함 같으니 그가 하는 모든 일이 다 형통하리로다."),
                    )
                ),
            )
        ),
        BibleBookDto(
            id = "JHN",
            name = "요한복음",
            abbr = "요",
            testament = "NEW",
            chapters = listOf(
                ChapterDto(
                    n = 1,
                    verses = listOf(
                        VerseDto(1, "태초에 말씀이 계시니라 이 말씀이 하나님과 함께 계셨으니 이 말씀은 곧 하나님이시니라."),
                        VerseDto(2, "그가 태초에 하나님과 함께 계셨고"),
                        VerseDto(3, "만물이 그로 말미암아 지은 바 되었으니 지은 것이 하나도 그가 없이는 된 것이 없느니라."),
                        VerseDto(4, "그 안에 생명이 있었으니 이 생명은 사람들의 빛이라."),
                        VerseDto(5, "빛이 어둠에 비치되 어둠이 깨닫지 못하더라."),
                    )
                ),
                ChapterDto(
                    n = 3,
                    verses = listOf(
                        VerseDto(16, "하나님이 세상을 이처럼 사랑하사 독생자를 주셨으니 이는 그를 믿는 자마다 멸망하지 않고 영생을 얻게 하려 하심이라."),
                        VerseDto(17, "하나님이 그 아들을 세상에 보내신 것은 세상을 심판하려 하심이 아니요 그로 말미암아 세상이 구원을 받게 하려 하심이라."),
                    )
                ),
            )
        ),
        BibleBookDto(
            id = "MAT",
            name = "마태복음",
            abbr = "마",
            testament = "NEW",
            chapters = listOf(
                ChapterDto(
                    n = 5,
                    verses = listOf(
                        VerseDto(3, "심령이 가난한 자는 복이 있나니 천국이 그들의 것임이요"),
                        VerseDto(4, "애통하는 자는 복이 있나니 그들이 위로를 받을 것임이요"),
                        VerseDto(5, "온유한 자는 복이 있나니 그들이 땅을 기업으로 받을 것임이요"),
                        VerseDto(6, "의에 주리고 목마른 자는 복이 있나니 그들이 배부를 것임이요"),
                        VerseDto(7, "긍휼히 여기는 자는 복이 있나니 그들이 긍휼히 여김을 받을 것임이요"),
                        VerseDto(8, "마음이 청결한 자는 복이 있나니 그들이 하나님을 볼 것임이요"),
                        VerseDto(9, "화평하게 하는 자는 복이 있나니 그들이 하나님의 아들이라 일컬음을 받을 것임이요"),
                    )
                ),
                ChapterDto(
                    n = 6,
                    verses = listOf(
                        VerseDto(33, "그런즉 너희는 먼저 그의 나라와 그의 의를 구하라 그리하면 이 모든 것을 너희에게 더하시리라."),
                        VerseDto(34, "그러므로 내일 일을 위하여 염려하지 말라 내일 일은 내일이 염려할 것이요 한 날의 괴로움은 그 날로 족하니라."),
                    )
                ),
            )
        ),
        BibleBookDto(
            id = "ROM",
            name = "로마서",
            abbr = "롬",
            testament = "NEW",
            chapters = listOf(
                ChapterDto(
                    n = 8,
                    verses = listOf(
                        VerseDto(28, "우리가 알거니와 하나님을 사랑하는 자 곧 그의 뜻대로 부르심을 입은 자들에게는 모든 것이 합력하여 선을 이루느니라."),
                        VerseDto(31, "그런즉 이 일에 대하여 우리가 무슨 말 하리요 만일 하나님이 우리를 위하시면 누가 우리를 대적하리요."),
                        VerseDto(38, "내가 확신하노니 사망이나 생명이나 천사들이나 권세자들이나 현재 일이나 장래 일이나 능력이나"),
                        VerseDto(39, "높음이나 깊음이나 다른 어떤 피조물이라도 우리를 우리 주 그리스도 예수 안에 있는 하나님의 사랑에서 끊을 수 없으리라."),
                    )
                ),
            )
        ),
        BibleBookDto(
            id = "PHP",
            name = "빌립보서",
            abbr = "빌",
            testament = "NEW",
            chapters = listOf(
                ChapterDto(
                    n = 4,
                    verses = listOf(
                        VerseDto(4, "주 안에서 항상 기뻐하라 내가 다시 말하노니 기뻐하라."),
                        VerseDto(6, "아무 것도 염려하지 말고 다만 모든 일에 기도와 간구로, 너희 구할 것을 감사함으로 하나님께 아뢰라."),
                        VerseDto(7, "그리하면 모든 지각에 뛰어난 하나님의 평강이 그리스도 예수 안에서 너희 마음과 생각을 지키시리라."),
                        VerseDto(13, "내게 능력 주시는 자 안에서 내가 모든 것을 할 수 있느니라."),
                    )
                ),
            )
        ),
    )
}
