package com.wordcard.app.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.wordcard.app.presentation.theme.LocalReaderColors
import com.wordcard.app.presentation.theme.LocalReaderTypography
import com.wordcard.app.presentation.theme.ReaderPalette

@Composable
fun SettingsSheet(
    palette: ReaderPalette,
    fontSize: Float,
    onPaletteChange: (ReaderPalette) -> Unit,
    onFontSizeChange: (Float) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalReaderColors.current
    val typo = LocalReaderTypography.current
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f))
            .clickable(indication = null, interactionSource = interaction) { onDismiss() }
    ) {
        Surface(
            color = colors.background,
            shape = RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .heightIn(min = 280.dp)
                .clickable(indication = null, interactionSource = interaction) { /* consume */ },
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 32.dp).navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Text(
                    text = "읽기 설정",
                    style = typo.title,
                    color = colors.onSurface,
                )

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "테마",
                        style = typo.chrome,
                        color = colors.onSurfaceMuted,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PaletteChip("라이트", palette == ReaderPalette.Light) { onPaletteChange(ReaderPalette.Light) }
                        PaletteChip("세피아", palette == ReaderPalette.Sepia) { onPaletteChange(ReaderPalette.Sepia) }
                        PaletteChip("다크", palette == ReaderPalette.Dark) { onPaletteChange(ReaderPalette.Dark) }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "글자 크기",
                            style = typo.chrome,
                            color = colors.onSurfaceMuted,
                        )
                        Text(
                            text = "${fontSize.toInt()}",
                            style = typo.chrome,
                            color = colors.onSurfaceMuted,
                        )
                    }
                    Slider(
                        value = fontSize,
                        onValueChange = onFontSizeChange,
                        valueRange = 28f..56f,
                        steps = 27,
                        colors = SliderDefaults.colors(
                            thumbColor = colors.accent,
                            activeTrackColor = colors.accent,
                            inactiveTrackColor = colors.verseNumber.copy(alpha = 0.4f),
                            activeTickColor = Color.Transparent,
                            inactiveTickColor = Color.Transparent,
                        ),
                    )
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun PaletteChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = LocalReaderColors.current
    val typo = LocalReaderTypography.current
    val borderColor = if (selected) colors.onSurface else colors.verseNumber.copy(alpha = 0.5f)
    val textColor = if (selected) colors.onSurface else colors.onSurfaceMuted
    Box(
        modifier = Modifier
            .border(width = if (selected) 1.5.dp else 1.dp, color = borderColor, shape = RoundedCornerShape(2.dp))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
            ) { onClick() }
            .padding(horizontal = 18.dp, vertical = 10.dp),
    ) {
        Text(
            text = label,
            style = typo.chrome,
            color = textColor,
        )
    }
}
