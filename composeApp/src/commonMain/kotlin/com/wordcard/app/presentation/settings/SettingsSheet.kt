package com.wordcard.app.presentation.settings

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wordcard.app.presentation.theme.LocalReaderColors
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
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
            .clickable(indication = null, interactionSource = interaction) { onDismiss() }
    ) {
        Surface(
            color = colors.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .heightIn(min = 240.dp)
                .clickable(indication = null, interactionSource = interaction) { /* consume */ },
        ) {
            Column(
                modifier = Modifier.padding(20.dp).navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "읽기 설정",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = colors.onSurface,
                )

                Text("테마", color = colors.onSurfaceMuted, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PaletteChip("라이트", palette == ReaderPalette.Light) { onPaletteChange(ReaderPalette.Light) }
                    PaletteChip("세피아", palette == ReaderPalette.Sepia) { onPaletteChange(ReaderPalette.Sepia) }
                    PaletteChip("다크", palette == ReaderPalette.Dark) { onPaletteChange(ReaderPalette.Dark) }
                }

                Spacer(Modifier.height(4.dp))
                Text("글자 크기  ${fontSize.toInt()}sp", color = colors.onSurfaceMuted, fontSize = 13.sp)
                Slider(
                    value = fontSize,
                    onValueChange = onFontSizeChange,
                    valueRange = 14f..28f,
                    steps = 13,
                    colors = SliderDefaults.colors(
                        thumbColor = colors.accent,
                        activeTrackColor = colors.accent,
                    ),
                )
            }
        }
    }
}

@Composable
private fun PaletteChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = LocalReaderColors.current
    val bg = if (selected) colors.accent else colors.background
    val fg = if (selected) Color.White else colors.onSurface
    Surface(
        color = bg,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.clickable { onClick() },
    ) {
        Text(
            text = label,
            color = fg,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}
