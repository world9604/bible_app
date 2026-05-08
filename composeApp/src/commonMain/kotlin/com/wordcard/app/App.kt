package com.wordcard.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.wordcard.app.di.appModules
import com.wordcard.app.presentation.reader.ReaderScreen
import com.wordcard.app.presentation.settings.SettingsSheet
import com.wordcard.app.presentation.theme.ReaderPalette
import com.wordcard.app.presentation.theme.ReaderTypography
import com.wordcard.app.presentation.theme.WordCardTheme
import org.koin.compose.KoinApplication

@Composable
fun App() {
    KoinApplication(application = { modules(appModules) }) {
        var palette by remember { mutableStateOf(ReaderPalette.Light) }
        var fontSize by remember { mutableStateOf(18f) }
        var showSettings by remember { mutableStateOf(false) }

        WordCardTheme(
            palette = palette,
            typography = ReaderTypography(bodyFontSizeSp = fontSize),
        ) {
            ReaderScreen(onOpenSettings = { showSettings = true })
            if (showSettings) {
                SettingsSheet(
                    palette = palette,
                    fontSize = fontSize,
                    onPaletteChange = { palette = it },
                    onFontSizeChange = { fontSize = it },
                    onDismiss = { showSettings = false },
                )
            }
        }
    }
}
