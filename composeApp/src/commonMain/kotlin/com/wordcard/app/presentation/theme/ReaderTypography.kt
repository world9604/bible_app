package com.wordcard.app.presentation.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp

data class ReaderTypography(
    val bodyFontSizeSp: Float = 18f,
    val lineHeightMultiplier: Float = 1.7f,
    val fontFamily: FontFamily = FontFamily.SansSerif,
) {
    val body: TextStyle
        get() = TextStyle(
            fontSize = bodyFontSizeSp.sp,
            lineHeight = (bodyFontSizeSp * lineHeightMultiplier).sp,
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            lineHeightStyle = LineHeightStyle(
                alignment = LineHeightStyle.Alignment.Center,
                trim = LineHeightStyle.Trim.None,
            ),
        )

    val verseNumber: TextStyle
        get() = TextStyle(
            fontSize = (bodyFontSizeSp * 0.7f).sp,
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
        )

    val title: TextStyle
        get() = TextStyle(
            fontSize = (bodyFontSizeSp * 1.4f).sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
        )

    val cardBody: TextStyle
        get() = TextStyle(
            fontSize = 22.sp,
            lineHeight = 36.sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Normal,
        )

    val cardReference: TextStyle
        get() = TextStyle(
            fontSize = 16.sp,
            lineHeight = 22.sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.SemiBold,
        )
}
