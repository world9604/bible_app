package com.wordcard.app.presentation.common

/**
 * 의존성을 줄이기 위해 아이콘은 단순 유니코드 글리프로 통일.
 * 정식 출시 시 SVG 리소스로 교체 가능.
 */
object AppGlyphs {
    const val Share = "↗"
    const val Close = "×"
    const val Settings = "Aa"
    const val ChevronLeft = "‹"
    const val ChevronRight = "›"
    const val Check = "✓"

    /** Material Symbols Outlined `arrow_back` (U+E5C4) — uses iconFontFamily */
    const val Back = ""

    /** Material Symbols Outlined `menu_book` (U+EA19) — uses iconFontFamily */
    const val TableOfContents = ""

    /** Play/Shorts indicator — uses default text family */
    const val Shorts = "▶"
}
