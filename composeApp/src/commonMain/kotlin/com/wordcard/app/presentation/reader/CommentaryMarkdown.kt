package com.wordcard.app.presentation.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wordcard.app.presentation.theme.LocalReaderColors
import com.wordcard.app.presentation.theme.LocalReaderTypography

/**
 * Structured representation of a single block of commentary body text.
 *
 * The bundled commentary body uses a light markdown dialect: paragraphs are
 * separated by blank lines, emphasis is wrapped in `**...**`, section headers
 * look like `**제목**(1-15절):`, and numbered points start with `**1. ...**`.
 * We parse it into these blocks so the reader can render real visual hierarchy
 * instead of dumping raw asterisks on screen.
 */
sealed interface CommentaryBlock {
    /** A section heading, e.g. `**니고데모의 방문**(1-15절):`. [verseRange] omits parentheses. */
    data class Heading(val title: String, val verseRange: String?) : CommentaryBlock

    /** A numbered point, e.g. `**1. 바리새인** — 율법의 정수.`. [body] keeps inline `**` markers. */
    data class NumberedItem(val number: String, val body: String) : CommentaryBlock

    /** A regular paragraph; [text] keeps inline `**` markers for emphasis. */
    data class Paragraph(val text: String) : CommentaryBlock
}

private val headingRegex = Regex("""^\*\*(.+?)\*\*\s*(\(([^)]*)\))?\s*:?\s*$""")
private val numberedBoldRegex = Regex("""^\*\*(\d+)\.\s*(.*)$""")
private val numberedPlainRegex = Regex("""^(\d+)\.\s+(.*)$""")

/** Split the raw body into structured blocks for hierarchical rendering. */
fun parseCommentaryBody(body: String): List<CommentaryBlock> =
    body.split("\n\n")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .map { classifyBlock(it) }

private fun classifyBlock(block: String): CommentaryBlock {
    numberedBoldRegex.matchEntire(block)?.let { m ->
        // `**1. 바리새인** — ...` → number "1", body "**바리새인** — ..." (keep emphasis balanced)
        val rest = m.groupValues[2]
        return CommentaryBlock.NumberedItem(m.groupValues[1], "**$rest")
    }
    numberedPlainRegex.matchEntire(block)?.let { m ->
        return CommentaryBlock.NumberedItem(m.groupValues[1], m.groupValues[2])
    }
    headingRegex.matchEntire(block)?.let { m ->
        val verseRange = m.groupValues[3].takeIf { it.isNotBlank() }
        return CommentaryBlock.Heading(m.groupValues[1], verseRange)
    }
    return CommentaryBlock.Paragraph(block)
}

/** Convert inline `**bold**` markup into a styled [AnnotatedString]. */
fun inlineCommentaryText(text: String): AnnotatedString = buildAnnotatedString {
    val parts = text.split("**")
    parts.forEachIndexed { index, part ->
        if (index % 2 == 1) {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(part) }
        } else {
            append(part)
        }
    }
}

/**
 * Renders parsed commentary [body] with section headings, numbered points and
 * paragraphs. [baseFontSizeSp] lets the caller scale text with reader settings.
 */
@Composable
fun CommentaryBody(
    body: String,
    baseFontSizeSp: Float,
    modifier: Modifier = Modifier,
) {
    val colors = LocalReaderColors.current
    val typo = LocalReaderTypography.current
    val blocks = remember(body) { parseCommentaryBody(body) }
    val bodyLineHeight = (baseFontSizeSp * 1.65f).sp

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        blocks.forEachIndexed { index, block ->
            when (block) {
                is CommentaryBlock.Heading -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = if (index == 0) 0.dp else 18.dp, bottom = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = block.title,
                            style = typo.title.copy(
                                fontSize = (baseFontSizeSp + 1f).sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = colors.onSurface,
                        )
                        if (block.verseRange != null) {
                            Spacer(Modifier.width(8.dp))
                            VerseRangeChip(block.verseRange)
                        }
                    }
                }

                is CommentaryBlock.NumberedItem -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Text(
                            text = "${block.number}.",
                            style = typo.body.copy(
                                fontSize = baseFontSizeSp.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = colors.verseNumber,
                            modifier = Modifier.width(22.dp),
                        )
                        Text(
                            text = inlineCommentaryText(block.body),
                            style = typo.body.copy(
                                fontSize = baseFontSizeSp.sp,
                                lineHeight = bodyLineHeight,
                            ),
                            color = colors.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                is CommentaryBlock.Paragraph -> {
                    Text(
                        text = inlineCommentaryText(block.text),
                        style = typo.body.copy(
                            fontSize = baseFontSizeSp.sp,
                            lineHeight = bodyLineHeight,
                        ),
                        color = colors.onSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun VerseRangeChip(verseRange: String) {
    val colors = LocalReaderColors.current
    val typo = LocalReaderTypography.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(colors.verseNumber.copy(alpha = 0.16f))
            .padding(horizontal = 8.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = verseRange,
            style = typo.chrome.copy(fontSize = 12.sp),
            color = colors.verseNumber,
        )
    }
}

/** Shared content padding used by commentary surfaces. */
internal val CommentaryContentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
