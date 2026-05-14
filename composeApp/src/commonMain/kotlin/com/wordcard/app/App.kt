package com.wordcard.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.wordcard.app.di.appModules
import com.wordcard.app.presentation.reader.ReaderScreen
import com.wordcard.app.presentation.reader.ViewerSettings
import com.wordcard.app.presentation.reader.ViewerSettingsSheet
import com.wordcard.app.presentation.theme.ReaderTypography
import com.wordcard.app.presentation.theme.WordCardTheme
import org.jetbrains.compose.resources.Font
import org.koin.compose.KoinApplication
import wordcard.composeapp.generated.resources.Res
import wordcard.composeapp.generated.resources.bookk_myungjo_bold
import wordcard.composeapp.generated.resources.bookk_myungjo_regular
import wordcard.composeapp.generated.resources.material_symbols_outlined
import wordcard.composeapp.generated.resources.noto_sans_kr_bold
import wordcard.composeapp.generated.resources.noto_sans_kr_regular
import wordcard.composeapp.generated.resources.prestige_elite_std_bold
import wordcard.composeapp.generated.resources.prestige_elite_std_bold_slanted

@Composable
private fun koreanSansFamily(): FontFamily = FontFamily(
    Font(Res.font.noto_sans_kr_regular, weight = FontWeight.Normal),
    Font(Res.font.noto_sans_kr_bold, weight = FontWeight.Bold),
    Font(Res.font.noto_sans_kr_bold, weight = FontWeight.SemiBold),
)

@Composable
private fun bookkMyungjoFamily(): FontFamily = FontFamily(
    Font(Res.font.bookk_myungjo_regular, weight = FontWeight.Normal),
    Font(Res.font.bookk_myungjo_bold, weight = FontWeight.Bold),
    Font(Res.font.bookk_myungjo_bold, weight = FontWeight.SemiBold),
)

@Composable
private fun prestigeEliteFamily(): FontFamily = FontFamily(
    Font(Res.font.prestige_elite_std_bold, weight = FontWeight.Bold, style = androidx.compose.ui.text.font.FontStyle.Normal),
    Font(Res.font.prestige_elite_std_bold_slanted, weight = FontWeight.Bold, style = androidx.compose.ui.text.font.FontStyle.Italic),
    Font(Res.font.prestige_elite_std_bold_slanted, weight = FontWeight.Normal, style = androidx.compose.ui.text.font.FontStyle.Italic),
)

@Composable
private fun materialIconsFamily(): FontFamily = FontFamily(
    Font(Res.font.material_symbols_outlined, weight = FontWeight.Normal),
)

@Composable
fun App() {
    KoinApplication(application = { modules(appModules) }) {
        var viewerSettings by remember { mutableStateOf(ViewerSettings.Default) }
        var showViewerSettings by remember { mutableStateOf(false) }

        val sansFamily = koreanSansFamily()
        val serifFamily = bookkMyungjoFamily()
        val numberFamily = prestigeEliteFamily()
        val iconFamily = materialIconsFamily()

        WordCardTheme(
            palette = viewerSettings.palette,
            typography = ReaderTypography(
                bodyFontSizeSp = viewerSettings.fontSizeSp,
                bodyLineHeightSp = viewerSettings.fontSizeSp * viewerSettings.lineHeightMultiplier,
                fontFamily = sansFamily,
                serifFontFamily = serifFamily,
                numberFontFamily = numberFamily,
                iconFontFamily = iconFamily,
            ),
        ) {
            ReaderScreen(
                viewerSettings = viewerSettings,
                onOpenSettings = { showViewerSettings = true },
            )
            if (showViewerSettings) {
                ViewerSettingsSheet(
                    current = viewerSettings,
                    onSave = { viewerSettings = it },
                    onDismiss = { showViewerSettings = false },
                )
            }
        }
    }
}
