package com.wordcard.app.presentation.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wordcard.app.domain.model.ChapterCommentary
import com.wordcard.app.domain.model.ChapterQna
import com.wordcard.app.presentation.common.AppGlyphs
import com.wordcard.app.presentation.theme.LocalReaderColors
import com.wordcard.app.presentation.theme.LocalReaderTypography

private enum class CommentaryTab(val label: String) {
    Summary("요약"),
    Body("본문 해설"),
    Qna("묻고답하기"),
}

/**
 * Full-screen-ish bottom sheet that presents a chapter's AI commentary across
 * three segments (summary / exposition / Q&A). Follows the app's existing
 * scrim + bottom Surface sheet pattern rather than Material3 ModalBottomSheet.
 */
@Composable
fun CommentarySheet(
    bookName: String,
    chapter: Int,
    commentary: ChapterCommentary,
    baseFontSizeSp: Float,
    onDismiss: () -> Unit,
) {
    val colors = LocalReaderColors.current
    val typo = LocalReaderTypography.current
    val interaction = remember { MutableInteractionSource() }
    val hasQna = commentary.qna.isNotEmpty()
    var tab by remember(commentary.bookId, commentary.chapter) { mutableStateOf(CommentaryTab.Body) }
    val scrollState = rememberScrollState()

    Box(
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
                .fillMaxHeight(0.92f)
                .clickable(indication = null, interactionSource = interaction) { /* consume */ },
        ) {
            Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
                SheetHandle()
                CommentaryHeader(
                    bookName = bookName,
                    chapter = chapter,
                    onDismiss = onDismiss,
                )
                SegmentTabs(
                    selected = tab,
                    showQna = hasQna,
                    onSelect = { tab = it },
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(CommentaryContentPadding),
                ) {
                    when (tab) {
                        CommentaryTab.Summary -> SummaryContent(commentary.summary, baseFontSizeSp)
                        CommentaryTab.Body -> CommentaryBody(
                            body = commentary.body,
                            baseFontSizeSp = baseFontSizeSp,
                        )
                        CommentaryTab.Qna -> QnaContent(commentary.qna, baseFontSizeSp)
                    }
                    Spacer(Modifier.height(24.dp))
                }

                CommentaryFooter(model = commentary.model, generatedAt = commentary.generatedAt)
            }
        }
    }
}

@Composable
private fun SheetHandle() {
    val colors = LocalReaderColors.current
    Box(modifier = Modifier.fillMaxWidth().padding(top = 10.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(colors.onSurfaceMuted.copy(alpha = 0.4f)),
        )
    }
}

@Composable
private fun CommentaryHeader(
    bookName: String,
    chapter: Int,
    onDismiss: () -> Unit,
) {
    val colors = LocalReaderColors.current
    val typo = LocalReaderTypography.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 16.dp, top = 14.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = AppGlyphs.Commentary,
            fontFamily = typo.iconFontFamily,
            fontSize = 20.sp,
            color = colors.verseNumber,
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "$bookName ${chapter}장 해설",
            style = typo.title.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
            color = colors.onSurface,
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = AppGlyphs.Close,
            fontSize = 24.sp,
            color = colors.onSurfaceMuted,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) { onDismiss() }
                .padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun SegmentTabs(
    selected: CommentaryTab,
    showQna: Boolean,
    onSelect: (CommentaryTab) -> Unit,
) {
    val tabs = CommentaryTab.entries.filter { it != CommentaryTab.Qna || showQna }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        tabs.forEach { tab ->
            SegmentPill(
                label = tab.label,
                active = tab == selected,
                onClick = { onSelect(tab) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SegmentPill(
    label: String,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalReaderColors.current
    val typo = LocalReaderTypography.current
    val bg = if (active) colors.accent else colors.selection
    val fg = if (active) onAccentColor(colors.accent) else colors.onSurfaceMuted
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
            ) { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = typo.chrome.copy(fontSize = 13.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal),
            color = fg,
        )
    }
}

@Composable
private fun SummaryContent(summary: String, baseFontSizeSp: Float) {
    val colors = LocalReaderColors.current
    val typo = LocalReaderTypography.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.selection)
            .padding(horizontal = 16.dp, vertical = 16.dp),
    ) {
        Text(
            text = inlineCommentaryText(summary),
            style = typo.body.copy(
                fontSize = (baseFontSizeSp + 1f).sp,
                lineHeight = ((baseFontSizeSp + 1f) * 1.7f).sp,
            ),
            color = colors.onSurface,
        )
    }
}

@Composable
private fun QnaContent(qna: List<ChapterQna>, baseFontSizeSp: Float) {
    val colors = LocalReaderColors.current
    val typo = LocalReaderTypography.current
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        qna.forEach { item ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.selection)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        text = "Q.",
                        style = typo.title.copy(fontSize = (baseFontSizeSp).sp, fontWeight = FontWeight.Bold),
                        color = colors.verseNumber,
                        modifier = Modifier.width(22.dp),
                    )
                    Text(
                        text = item.question,
                        style = typo.body.copy(
                            fontSize = baseFontSizeSp.sp,
                            lineHeight = (baseFontSizeSp * 1.6f).sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = colors.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        text = "A.",
                        style = typo.title.copy(fontSize = (baseFontSizeSp).sp, fontWeight = FontWeight.Bold),
                        color = colors.onSurfaceMuted,
                        modifier = Modifier.width(22.dp),
                    )
                    Text(
                        text = inlineCommentaryText(item.answer),
                        style = typo.body.copy(
                            fontSize = baseFontSizeSp.sp,
                            lineHeight = (baseFontSizeSp * 1.65f).sp,
                        ),
                        color = colors.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun CommentaryFooter(model: String, generatedAt: String) {
    val colors = LocalReaderColors.current
    val typo = LocalReaderTypography.current
    val date = generatedAt.substringBefore('T')
    Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(colors.onSurfaceMuted.copy(alpha = 0.18f)),
        )
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = AppGlyphs.Commentary,
                fontFamily = typo.iconFontFamily,
                fontSize = 13.sp,
                color = colors.onSurfaceMuted,
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "AI가 생성한 해설입니다 · $model · $date",
                style = typo.chrome.copy(fontSize = 11.sp),
                color = colors.onSurfaceMuted,
            )
        }
    }
}

/** Pick a legible foreground for [accent] backgrounds (mirrors MemoEditorSheet). */
internal fun onAccentColor(accent: Color): Color =
    if (accent.red * 0.299f + accent.green * 0.587f + accent.blue * 0.114f > 0.5f) {
        Color(0xFF111111)
    } else {
        Color(0xFFFAFAF7)
    }
