package com.wordcard.app.presentation.reader

import com.wordcard.app.presentation.theme.ReaderPalette

/**
 * 본문 뷰어 설정값. 각 필드는 디폴트(0)로부터의 오프셋이며,
 * 실제 사이즈/마진은 `Defaults`의 베이스에 오프셋을 더해 계산한다.
 */
data class ViewerSettings(
    val palette: ReaderPalette = ReaderPalette.Light,
    val fontSizeOffset: Int = 0,
    val lineSpacingOffset: Int = 0,
    val paragraphSpacingOffset: Int = 0,
    val verticalMarginOffset: Int = 0,
    val horizontalMarginOffset: Int = 0,
) {
    val fontSizeSp: Float get() = (Defaults.FontSizeSp + fontSizeOffset).coerceAtLeast(8f)
    val lineHeightMultiplier: Float get() = (Defaults.LineHeightMultiplier + lineSpacingOffset * 0.05f).coerceAtLeast(1.2f)
    val paragraphSpacingDp: Int get() = (Defaults.ParagraphSpacingDp + paragraphSpacingOffset).coerceAtLeast(0)
    val verticalMarginTopDp: Int get() = (Defaults.VerticalMarginTopDp + verticalMarginOffset * 2).coerceAtLeast(0)
    val verticalMarginBottomDp: Int get() = (Defaults.VerticalMarginBottomDp + verticalMarginOffset * 2).coerceAtLeast(0)
    val horizontalMarginDp: Int get() = (Defaults.HorizontalMarginDp + horizontalMarginOffset * 2).coerceAtLeast(8)

    companion object {
        val Default = ViewerSettings()
    }

    object Defaults {
        const val FontSizeSp = 19f
        const val LineHeightMultiplier = 2.15f
        const val ParagraphSpacingDp = 2
        const val VerticalMarginTopDp = 16
        const val VerticalMarginBottomDp = 8
        const val HorizontalMarginDp = 36
    }

    object Ranges {
        val FontSize = -8..8
        val LineSpacing = -10..10
        val ParagraphSpacing = -2..12
        val VerticalMargin = -8..16
        val HorizontalMargin = -14..14
    }
}
