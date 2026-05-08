package com.wordcard.app.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

enum class ReaderPalette { Light, Dark, Sepia }

data class ReaderColors(
    val background: Color,
    val surface: Color,
    val onSurface: Color,
    val onSurfaceMuted: Color,
    val accent: Color,
    val verseNumber: Color,
    val selection: Color,
)

internal val LightReaderColors = ReaderColors(
    background = Color(0xFFFBF9F4),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1F1B16),
    onSurfaceMuted = Color(0xFF6B6358),
    accent = Color(0xFFB07A3B),
    verseNumber = Color(0xFFB6A98F),
    selection = Color(0xFFFCE9C7),
)

internal val DarkReaderColors = ReaderColors(
    background = Color(0xFF14110E),
    surface = Color(0xFF1C1814),
    onSurface = Color(0xFFECE5D8),
    onSurfaceMuted = Color(0xFF9A9080),
    accent = Color(0xFFE0B97D),
    verseNumber = Color(0xFF6E6354),
    selection = Color(0xFF3A2E1B),
)

internal val SepiaReaderColors = ReaderColors(
    background = Color(0xFFF4ECD8),
    surface = Color(0xFFFAF3E0),
    onSurface = Color(0xFF5B4636),
    onSurfaceMuted = Color(0xFF8A765C),
    accent = Color(0xFF8C5A2B),
    verseNumber = Color(0xFFB99E78),
    selection = Color(0xFFE6CFA1),
)

val LocalReaderColors = staticCompositionLocalOf { LightReaderColors }
val LocalReaderTypography = compositionLocalOf { ReaderTypography() }

@Composable
fun WordCardTheme(
    palette: ReaderPalette = if (isSystemInDarkTheme()) ReaderPalette.Dark else ReaderPalette.Light,
    typography: ReaderTypography = ReaderTypography(),
    content: @Composable () -> Unit,
) {
    val readerColors = when (palette) {
        ReaderPalette.Light -> LightReaderColors
        ReaderPalette.Dark -> DarkReaderColors
        ReaderPalette.Sepia -> SepiaReaderColors
    }

    val colorScheme = if (palette == ReaderPalette.Dark) {
        darkColorScheme(
            background = readerColors.background,
            surface = readerColors.surface,
            onSurface = readerColors.onSurface,
            primary = readerColors.accent,
        )
    } else {
        lightColorScheme(
            background = readerColors.background,
            surface = readerColors.surface,
            onSurface = readerColors.onSurface,
            primary = readerColors.accent,
        )
    }

    CompositionLocalProvider(
        LocalReaderColors provides readerColors,
        LocalReaderTypography provides typography,
    ) {
        MaterialTheme(colorScheme = colorScheme, content = content)
    }
}
