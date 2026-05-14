package com.wordcard.app.presentation.reader

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wordcard.app.presentation.common.AppGlyphs
import com.wordcard.app.presentation.theme.LocalReaderColors
import com.wordcard.app.presentation.theme.ReaderPalette
import com.wordcard.app.presentation.theme.readerColorsFor
import kotlin.math.roundToInt

@Composable
fun ViewerSettingsSheet(
    current: ViewerSettings,
    onSave: (ViewerSettings) -> Unit,
    onDismiss: () -> Unit,
) {
    var draft by remember { mutableStateOf(current) }
    val colors = LocalReaderColors.current

    BottomSheetScaffold(onDismiss = onDismiss) {
        Column(modifier = Modifier.fillMaxSize().padding(top = 12.dp).navigationBarsPadding()) {
            DragHandle()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "뷰어 설정",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = AppGlyphs.Close,
                    fontSize = 26.sp,
                    color = colors.onSurfaceMuted,
                    modifier = Modifier
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                        ) { onDismiss() }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }

            Spacer(Modifier.height(4.dp))

            ThemeSelector(
                current = draft.palette,
                onSelect = { draft = draft.copy(palette = it) },
            )

            Spacer(Modifier.height(8.dp))

            PillSliderRow(
                icon = "가",
                label = "글자 크기",
                value = draft.fontSizeOffset,
                range = ViewerSettings.Ranges.FontSize,
                onChange = { draft = draft.copy(fontSizeOffset = it) },
            )
            PillSliderRow(
                icon = "≡",
                label = "줄간격",
                value = draft.lineSpacingOffset,
                range = ViewerSettings.Ranges.LineSpacing,
                onChange = { draft = draft.copy(lineSpacingOffset = it) },
            )
            PillSliderRow(
                icon = "¶",
                label = "문단 간격",
                value = draft.paragraphSpacingOffset,
                range = ViewerSettings.Ranges.ParagraphSpacing,
                onChange = { draft = draft.copy(paragraphSpacingOffset = it) },
            )
            PillSliderRow(
                icon = "↕",
                label = "상하여백",
                value = draft.verticalMarginOffset,
                range = ViewerSettings.Ranges.VerticalMargin,
                onChange = { draft = draft.copy(verticalMarginOffset = it) },
            )
            PillSliderRow(
                icon = "↔",
                label = "좌우여백",
                value = draft.horizontalMarginOffset,
                range = ViewerSettings.Ranges.HorizontalMargin,
                onChange = { draft = draft.copy(horizontalMarginOffset = it) },
            )

            Spacer(Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                FooterButton(
                    label = "초기화",
                    onClick = { draft = ViewerSettings.Default },
                    isPrimary = false,
                    modifier = Modifier.weight(1f),
                )
                FooterButton(
                    label = "저장",
                    onClick = {
                        onSave(draft)
                        onDismiss()
                    },
                    isPrimary = true,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun PillSliderRow(
    icon: String,
    label: String,
    value: Int,
    range: IntRange,
    onChange: (Int) -> Unit,
) {
    val colors = LocalReaderColors.current
    val total = (range.last - range.first).coerceAtLeast(1)
    val frac = ((value - range.first).toFloat() / total).coerceIn(0f, 1f)

    BoxWithConstraints(
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.selection)
            .pointerInput(range) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    val widthPx = size.width.toFloat()
                    fun set(x: Float) {
                        val f = (x / widthPx).coerceIn(0f, 1f)
                        val newVal = range.first + (f * total).roundToInt()
                        onChange(newVal.coerceIn(range.first, range.last))
                    }
                    set(down.position.x)
                    down.consume()
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.first()
                        if (!change.pressed) break
                        set(change.position.x)
                        change.consume()
                    }
                }
            },
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(maxWidth * frac)
                .background(colors.onSurfaceMuted.copy(alpha = 0.28f))
        )
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = icon,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface,
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                color = colors.onSurface,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = if (value > 0) "+$value" else value.toString(),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface,
                textAlign = TextAlign.End,
            )
        }
    }
}

@Composable
private fun FooterButton(
    label: String,
    onClick: () -> Unit,
    isPrimary: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = LocalReaderColors.current
    val bg = if (isPrimary) colors.accent else colors.selection
    val fg = if (isPrimary) {
        if (colors.accent.luminance() > 0.5f) Color(0xFF111111) else Color(0xFFFAFAF7)
    } else colors.onSurface
    Surface(
        color = bg,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .height(48.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick,
            ),
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = label,
                color = fg,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun ThemeSelector(
    current: ReaderPalette,
    onSelect: (ReaderPalette) -> Unit,
) {
    val colors = LocalReaderColors.current
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp)) {
        Text(
            text = "테마",
            fontSize = 14.sp,
            color = colors.onSurfaceMuted,
        )
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            ThemeSwatch(
                palette = ReaderPalette.Light,
                selected = current == ReaderPalette.Light,
                onClick = { onSelect(ReaderPalette.Light) },
            )
            ThemeSwatch(
                palette = ReaderPalette.Sepia,
                selected = current == ReaderPalette.Sepia,
                onClick = { onSelect(ReaderPalette.Sepia) },
            )
            ThemeSwatch(
                palette = ReaderPalette.Dark,
                selected = current == ReaderPalette.Dark,
                onClick = { onSelect(ReaderPalette.Dark) },
            )
        }
    }
}

@Composable
private fun ThemeSwatch(
    palette: ReaderPalette,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val swatchColors = readerColorsFor(palette)
    val outerColors = LocalReaderColors.current
    val borderColor = if (selected) outerColors.accent else outerColors.onSurfaceMuted.copy(alpha = 0.3f)
    val borderWidth = if (selected) 2.5.dp else 1.dp
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(swatchColors.background)
            .border(BorderStroke(borderWidth, borderColor), CircleShape)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Text(
                text = AppGlyphs.Check,
                fontSize = 18.sp,
                color = swatchColors.onSurface,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

private fun Color.luminance(): Float =
    0.299f * red + 0.587f * green + 0.114f * blue
