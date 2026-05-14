package com.wordcard.app.presentation.reader

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wordcard.app.presentation.common.AppGlyphs
import com.wordcard.app.presentation.common.PlatformWebView
import com.wordcard.app.presentation.theme.LocalReaderColors

@Composable
fun YouTubeShortsSheet(
    bookName: String,
    chapter: Int,
    onDismiss: () -> Unit,
) {
    val colors = LocalReaderColors.current
    val url = remember(bookName, chapter) { buildShortsSearchUrl(bookName, chapter) }

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
                    text = "$bookName ${chapter}장 · YouTube Shorts",
                    fontSize = 18.sp,
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

            PlatformWebView(
                url = url,
                modifier = Modifier.fillMaxWidth().weight(1f),
                injectedCss = HIDE_BOTTOM_NAV_CSS,
            )
        }
    }
}

private val HIDE_BOTTOM_NAV_CSS = """
    ytm-pivot-bar-renderer,
    .pivot-bar,
    #pivot-bar,
    ytm-app-footer,
    ytm-mealbar-promo-renderer { display: none !important; }
    body, ytm-app { padding-bottom: 0 !important; margin-bottom: 0 !important; }
""".trimIndent()

private fun buildShortsSearchUrl(bookName: String, chapter: Int): String {
    val query = "$bookName ${chapter}장"
    return "https://m.youtube.com/results?search_query=${urlEncodeQuery(query)}&sp=EgIYAQ%253D%253D"
}

private fun urlEncodeQuery(s: String): String {
    val bytes = s.encodeToByteArray()
    val sb = StringBuilder()
    for (b in bytes) {
        val v = b.toInt() and 0xFF
        when {
            v == 0x20 -> sb.append('+')
            v in 0x30..0x39 ||
            v in 0x41..0x5A ||
            v in 0x61..0x7A ||
            v == 0x2D || v == 0x5F || v == 0x2E || v == 0x7E -> sb.append(v.toChar())
            else -> {
                sb.append('%')
                sb.append(v.toString(16).uppercase().padStart(2, '0'))
            }
        }
    }
    return sb.toString()
}
