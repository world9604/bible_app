package com.wordcard.app.presentation.share

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

data class CardBackground(
    val id: String,
    val name: String,
    val brush: Brush,
    val onColor: Color,
    val accentColor: Color,
)

val DefaultCardBackgrounds: List<CardBackground> = listOf(
    CardBackground(
        id = "ivory",
        name = "아이보리",
        brush = Brush.verticalGradient(listOf(Color(0xFFFBF7EE), Color(0xFFEFE5CC))),
        onColor = Color(0xFF2A231A),
        accentColor = Color(0xFFB07A3B),
    ),
    CardBackground(
        id = "midnight",
        name = "미드나잇",
        brush = Brush.verticalGradient(listOf(Color(0xFF1A1A2E), Color(0xFF2C2540))),
        onColor = Color(0xFFF4F0E6),
        accentColor = Color(0xFFE0B97D),
    ),
    CardBackground(
        id = "dawn",
        name = "새벽",
        brush = Brush.linearGradient(listOf(Color(0xFFFFD3A5), Color(0xFFFD6585))),
        onColor = Color(0xFF2D1A2E),
        accentColor = Color(0xFFFFFFFF),
    ),
    CardBackground(
        id = "ocean",
        name = "오션",
        brush = Brush.linearGradient(listOf(Color(0xFF89F7FE), Color(0xFF66A6FF))),
        onColor = Color(0xFF11243D),
        accentColor = Color(0xFFFFFFFF),
    ),
    CardBackground(
        id = "sage",
        name = "세이지",
        brush = Brush.verticalGradient(listOf(Color(0xFFE8F0E2), Color(0xFFC9D9C0))),
        onColor = Color(0xFF2C3E29),
        accentColor = Color(0xFF4F7A4A),
    ),
    CardBackground(
        id = "graphite",
        name = "그라파이트",
        brush = Brush.verticalGradient(listOf(Color(0xFF2C2C2C), Color(0xFF1A1A1A))),
        onColor = Color(0xFFF4F0E6),
        accentColor = Color(0xFFCFA85C),
    ),
)
