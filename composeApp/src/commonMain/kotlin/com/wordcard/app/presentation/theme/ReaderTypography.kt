package com.wordcard.app.presentation.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

data class ReaderTypography(
    val bodyFontSizeSp: Float = 28f,
    val bodyLineHeightSp: Float = 60f,
    val bodyLetterSpacingEm: Float = 0f,
    val numberFontSizeSp: Float = 16f,
    val fontFamily: FontFamily = FontFamily.SansSerif,
    val serifFontFamily: FontFamily = FontFamily.Serif,
    val numberFontFamily: FontFamily = FontFamily.Monospace,
    val iconFontFamily: FontFamily = FontFamily.SansSerif,
) {
    val body: TextStyle
        get() = TextStyle(
            fontSize = bodyFontSizeSp.sp,
            lineHeight = bodyLineHeightSp.sp,
            fontFamily = serifFontFamily,
            fontWeight = FontWeight.Normal,
            letterSpacing = bodyLetterSpacingEm.em,
            lineHeightStyle = LineHeightStyle(
                alignment = LineHeightStyle.Alignment.Center,
                trim = LineHeightStyle.Trim.None,
            ),
        )

    val verseNumber: TextStyle
        get() = TextStyle(
            fontSize = numberFontSizeSp.sp,
            fontFamily = numberFontFamily,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Italic,
            letterSpacing = 0.5.sp,
        )

    val chrome: TextStyle
        get() = TextStyle(
            fontSize = 14.sp,
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.3.sp,
        )

    val topBar: TextStyle
        get() = TextStyle(
            fontSize = 24.sp,
            fontFamily = serifFontFamily,
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.sp,
        )

    val title: TextStyle
        get() = TextStyle(
            fontSize = 17.sp,
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.2.sp,
        )

    val icon: TextStyle
        get() = TextStyle(
            fontSize = 48.sp,
            fontFamily = iconFontFamily,
            fontWeight = FontWeight.Thin,
            letterSpacing = 0.sp,
        )

    val cardBody: TextStyle
        get() = TextStyle(
            fontSize = 22.sp,
            lineHeight = 38.sp,
            fontFamily = serifFontFamily,
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.05.em,
        )

    val cardReference: TextStyle
        get() = TextStyle(
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.5.sp,
        )
}
