package com.wordcard.app.presentation.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wordcard.app.domain.model.Book
import com.wordcard.app.domain.model.Testament
import com.wordcard.app.presentation.theme.LocalReaderColors

@Composable
fun BookPickerSheet(
    books: List<Book>,
    currentBookId: String?,
    currentChapter: Int?,
    onPickChapter: (bookId: String, chapter: Int) -> Unit,
    onDismiss: () -> Unit,
) {
    BottomSheetScaffold(onDismiss = onDismiss) {
        val colors = LocalReaderColors.current
        var expandedBookId by remember { mutableStateOf(currentBookId) }

        Column(modifier = Modifier.fillMaxSize().padding(top = 12.dp).navigationBarsPadding()) {
            DragHandle()
            Text(
                text = "성경",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            )
            LazyColumn(
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                val (oldT, newT) = books.partition { it.testament == Testament.OLD }
                if (oldT.isNotEmpty()) {
                    item { SectionHeader("구약") }
                    items(oldT, key = { it.id }) { book ->
                        BookRowWithChapters(
                            book = book,
                            expanded = expandedBookId == book.id,
                            currentChapter = if (currentBookId == book.id) currentChapter else null,
                            onClickBook = {
                                expandedBookId = if (expandedBookId == book.id) null else book.id
                            },
                            onPickChapter = { ch -> onPickChapter(book.id, ch) },
                        )
                    }
                }
                if (newT.isNotEmpty()) {
                    item { SectionHeader("신약") }
                    items(newT, key = { it.id }) { book ->
                        BookRowWithChapters(
                            book = book,
                            expanded = expandedBookId == book.id,
                            currentChapter = if (currentBookId == book.id) currentChapter else null,
                            onClickBook = {
                                expandedBookId = if (expandedBookId == book.id) null else book.id
                            },
                            onPickChapter = { ch -> onPickChapter(book.id, ch) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun BottomSheetScaffold(
    onDismiss: () -> Unit,
    content: @Composable BoxScope.() -> Unit,
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
                .heightIn(min = 360.dp)
                .fillMaxHeight(0.85f)
                .clickable(indication = null, interactionSource = interaction) { /* consume */ },
            content = { Box(modifier = Modifier.fillMaxSize(), content = content) },
        )
    }
}

@Composable
internal fun DragHandle() {
    val colors = LocalReaderColors.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .height(4.dp)
                .width(40.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(colors.onSurfaceMuted.copy(alpha = 0.3f))
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    val colors = LocalReaderColors.current
    Text(
        text = text,
        color = colors.onSurfaceMuted,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 20.dp, top = 12.dp, bottom = 4.dp),
    )
}

@Composable
private fun BookRowWithChapters(
    book: Book,
    expanded: Boolean,
    currentChapter: Int?,
    onClickBook: () -> Unit,
    onPickChapter: (Int) -> Unit,
) {
    val colors = LocalReaderColors.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClickBook() }
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                book.name,
                color = colors.onSurface,
                fontSize = 16.sp,
                fontWeight = if (expanded) FontWeight.SemiBold else FontWeight.Normal,
            )
            Spacer(Modifier.weight(1f))
            Text("${book.chapterCount}장", color = colors.onSurfaceMuted, fontSize = 13.sp)
        }
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            ChapterGrid(
                chapterCount = book.chapterCount,
                current = currentChapter,
                onPick = onPickChapter,
            )
        }
    }
}

@Composable
private fun ChapterGrid(
    chapterCount: Int,
    current: Int?,
    onPick: (Int) -> Unit,
) {
    val colors = LocalReaderColors.current
    val columns = 6
    val rows = (chapterCount + columns - 1) / columns
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        for (r in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                for (c in 0 until columns) {
                    val n = r * columns + c + 1
                    if (n <= chapterCount) {
                        val selected = n == current
                        val bg = if (selected) colors.accent else colors.background
                        val fg = if (selected) Color.White else colors.onSurface
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(bg)
                                .clickable { onPick(n) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "$n",
                                color = fg,
                                fontSize = 14.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
