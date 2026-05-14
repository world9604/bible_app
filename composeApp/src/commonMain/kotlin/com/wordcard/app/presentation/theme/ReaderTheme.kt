package com.wordcard.app.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
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
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1A1A),
    onSurfaceMuted = Color(0xFF8A8A85),
    accent = Color(0xFF1A1A1A),
    verseNumber = Color(0xFF74A62E),
    selection = Color(0xFFF1F1EC),
)

internal val DarkReaderColors = ReaderColors(
    background = Color(0xFF111111),
    surface = Color(0xFF111111),
    onSurface = Color(0xFFE6E4DE),
    onSurfaceMuted = Color(0xFF7A7872),
    accent = Color(0xFFE6E4DE),
    verseNumber = Color(0xFF4A4845),
    selection = Color(0xFF24221F),
)

internal val SepiaReaderColors = ReaderColors(
    background = Color(0xFFF4ECDC),
    surface = Color(0xFFF4ECDC),
    onSurface = Color(0xFF2D2620),
    onSurfaceMuted = Color(0xFF9A8A75),
    accent = Color(0xFF2D2620),
    verseNumber = Color(0xFFC0AE92),
    selection = Color(0xFFE5DBC2),
)

val LocalReaderColors = staticCompositionLocalOf { LightReaderColors }
val LocalReaderTypography = compositionLocalOf { ReaderTypography() }

fun readerColorsFor(palette: ReaderPalette): ReaderColors = when (palette) {
    ReaderPalette.Light -> LightReaderColors
    ReaderPalette.Dark -> DarkReaderColors
    ReaderPalette.Sepia -> SepiaReaderColors
}

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

    val baseTypography = Typography()
    val materialTypography = Typography(
        displayLarge = baseTypography.displayLarge.copy(fontFamily = typography.fontFamily),
        displayMedium = baseTypography.displayMedium.copy(fontFamily = typography.fontFamily),
        displaySmall = baseTypography.displaySmall.copy(fontFamily = typography.fontFamily),
        headlineLarge = baseTypography.headlineLarge.copy(fontFamily = typography.fontFamily),
        headlineMedium = baseTypography.headlineMedium.copy(fontFamily = typography.fontFamily),
        headlineSmall = baseTypography.headlineSmall.copy(fontFamily = typography.fontFamily),
        titleLarge = baseTypography.titleLarge.copy(fontFamily = typography.fontFamily),
        titleMedium = baseTypography.titleMedium.copy(fontFamily = typography.fontFamily),
        titleSmall = baseTypography.titleSmall.copy(fontFamily = typography.fontFamily),
        bodyLarge = baseTypography.bodyLarge.copy(fontFamily = typography.fontFamily),
        bodyMedium = baseTypography.bodyMedium.copy(fontFamily = typography.fontFamily),
        bodySmall = baseTypography.bodySmall.copy(fontFamily = typography.fontFamily),
        labelLarge = baseTypography.labelLarge.copy(fontFamily = typography.fontFamily),
        labelMedium = baseTypography.labelMedium.copy(fontFamily = typography.fontFamily),
        labelSmall = baseTypography.labelSmall.copy(fontFamily = typography.fontFamily),
    )

    CompositionLocalProvider(
        LocalReaderColors provides readerColors,
        LocalReaderTypography provides typography,
    ) {
        MaterialTheme(colorScheme = colorScheme, typography = materialTypography, content = content)
    }
}
