package com.wordcard.app.presentation.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wordcard.app.presentation.theme.LocalReaderColors
import com.wordcard.app.presentation.theme.LocalReaderTypography

@Composable
fun MemoEditorSheet(
    bookName: String,
    chapter: Int,
    verseNumber: Int,
    draft: String,
    hasExistingMemo: Boolean,
    onChange: (String) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalReaderColors.current
    val typo = LocalReaderTypography.current
    val interaction = remember { MutableInteractionSource() }

    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
            .clickable(indication = null, interactionSource = interaction) { onDismiss() },
    ) {
        Surface(
            color = colors.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clickable(indication = null, interactionSource = interaction) { /* consume */ },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "메모",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "$bookName ${chapter}:${verseNumber}",
                        style = typo.chrome,
                        color = colors.onSurfaceMuted,
                    )
                }

                Spacer(Modifier.heightIn(min = 12.dp))

                BasicTextField(
                    value = draft,
                    onValueChange = onChange,
                    textStyle = typo.body.copy(color = colors.onSurface),
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(colors.accent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 160.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, colors.verseNumber.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    decorationBox = { inner ->
                        if (draft.isEmpty()) {
                            Text(
                                text = "이 절에 대한 메모를 입력하세요...",
                                style = typo.body,
                                color = colors.onSurfaceMuted,
                            )
                        }
                        inner()
                    },
                )

                Spacer(Modifier.heightIn(min = 16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                ) {
                    if (hasExistingMemo) {
                        Text(
                            text = "삭제",
                            style = typo.chrome,
                            color = Color(0xFFB94A4A),
                            modifier = Modifier
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                ) { onDelete() }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        text = "취소",
                        style = typo.chrome,
                        color = colors.onSurfaceMuted,
                        modifier = Modifier
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                            ) { onDismiss() }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    val onAccent =
                        if (colors.accent.red * 0.299f + colors.accent.green * 0.587f + colors.accent.blue * 0.114f > 0.5f) {
                            Color(0xFF111111)
                        } else {
                            Color(0xFFFAFAF7)
                        }
                    Surface(
                        color = colors.accent,
                        shape = RoundedCornerShape(2.dp),
                        modifier = Modifier.clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                        ) { onSave() },
                    ) {
                        Text(
                            text = "저장",
                            style = typo.chrome,
                            color = onAccent,
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                        )
                    }
                }
            }
        }
    }
}
