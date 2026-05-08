package com.wordcard.app.presentation.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wordcard.app.domain.model.Verse
import com.wordcard.app.presentation.common.AppGlyphs
import com.wordcard.app.presentation.theme.LocalReaderColors
import com.wordcard.app.presentation.theme.LocalReaderTypography
import com.wordcard.app.presentation.theme.ReaderTypography
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ReaderScreen(
    viewModel: ReaderViewModel = koinViewModel(),
    onOpenSettings: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val readerColors = LocalReaderColors.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = readerColors.background,
    ) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            ReaderTopBar(
                bookName = state.currentBook?.name ?: "",
                chapter = state.currentChapter?.number,
                onBookClick = viewModel::openBookPicker,
                onChapterClick = viewModel::openChapterPicker,
                onSettings = onOpenSettings,
            )

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    state.isLoading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = readerColors.accent)
                        }
                    }
                    state.currentChapter != null -> {
                        ChapterContent(
                            verses = state.currentChapter!!.verses,
                            selected = state.selectedVerseNumbers,
                            onToggle = viewModel::toggleVerse,
                            onPrevChapter = viewModel::previousChapter,
                            onNextChapter = viewModel::nextChapter,
                        )
                    }
                }

                AnimatedVisibility(
                    visible = state.hasSelection,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it }),
                    modifier = Modifier.align(Alignment.BottomCenter),
                ) {
                    SelectionActionBar(
                        count = state.selectedVerseNumbers.size,
                        onClear = viewModel::clearSelection,
                        onShare = viewModel::openShareCard,
                    )
                }
            }
        }

        if (state.showBookPicker) {
            BookPickerSheet(
                books = state.books,
                onPick = viewModel::selectBook,
                onDismiss = viewModel::closeBookPicker,
            )
        }

        if (state.showChapterPicker) {
            val book = state.currentBook
            if (book != null) {
                ChapterPickerSheet(
                    bookName = book.name,
                    chapterCount = book.chapterCount,
                    current = state.currentChapter?.number ?: 1,
                    onPick = viewModel::selectChapter,
                    onDismiss = viewModel::closeChapterPicker,
                )
            }
        }

        if (state.showShareCard && state.hasSelection && state.currentBook != null) {
            com.wordcard.app.presentation.share.ShareCardScreen(
                verses = state.selectedVerses,
                bookName = state.currentBook!!.name,
                onDismiss = viewModel::closeShareCard,
            )
        }
    }
}

@Composable
private fun ReaderTopBar(
    bookName: String,
    chapter: Int?,
    onBookClick: () -> Unit,
    onChapterClick: () -> Unit,
    onSettings: () -> Unit,
) {
    val colors = LocalReaderColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PillButton(text = bookName.ifBlank { "책 선택" }, onClick = onBookClick, emphasized = true)
        Spacer(Modifier.width(8.dp))
        PillButton(text = chapter?.let { "${it}장" } ?: "-", onClick = onChapterClick)
        Spacer(Modifier.weight(1f))
        Surface(
            color = colors.surface,
            shape = RoundedCornerShape(50),
            modifier = Modifier.clickable { onSettings() },
        ) {
            Text(
                text = AppGlyphs.Settings,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                color = colors.onSurfaceMuted,
                fontSize = 18.sp,
            )
        }
    }
}

@Composable
private fun PillButton(
    text: String,
    onClick: () -> Unit,
    emphasized: Boolean = false,
) {
    val colors = LocalReaderColors.current
    Surface(
        color = colors.surface,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.clickable { onClick() },
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            color = colors.onSurface,
            fontWeight = if (emphasized) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 16.sp,
        )
    }
}

@Composable
private fun ChapterContent(
    verses: List<Verse>,
    selected: Set<Int>,
    onToggle: (Int) -> Unit,
    onPrevChapter: () -> Unit,
    onNextChapter: () -> Unit,
) {
    val listState = rememberLazyListState()
    val colors = LocalReaderColors.current
    val typo = LocalReaderTypography.current

    LaunchedEffect(verses.firstOrNull()?.bookId, verses.firstOrNull()?.chapter) {
        listState.scrollToItem(0)
    }

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        items(verses, key = { it.number }) { verse ->
            val isSelected = verse.number in selected
            val bg = if (isSelected) colors.selection else Color.Transparent
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(bg)
                    .clickable { onToggle(verse.number) }
                    .padding(horizontal = 8.dp, vertical = 6.dp),
            ) {
                Text(
                    text = renderVerse(verse, typo, colors.verseNumber),
                    style = typo.body,
                    color = colors.onSurface,
                )
            }
        }
        item {
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onPrevChapter) {
                    Text("${AppGlyphs.ChevronLeft} 이전 장", color = colors.onSurfaceMuted)
                }
                TextButton(onClick = onNextChapter) {
                    Text("다음 장 ${AppGlyphs.ChevronRight}", color = colors.onSurfaceMuted)
                }
            }
            Spacer(Modifier.height(120.dp))
        }
    }
}

private fun renderVerse(
    verse: Verse,
    typo: ReaderTypography,
    numberColor: Color,
): AnnotatedString = buildAnnotatedString {
    withStyle(
        SpanStyle(
            fontSize = typo.verseNumber.fontSize,
            fontWeight = typo.verseNumber.fontWeight,
            color = numberColor,
        )
    ) { append("${verse.number}  ") }
    append(verse.text)
}

@Composable
private fun SelectionActionBar(
    count: Int,
    onClear: () -> Unit,
    onShare: () -> Unit,
) {
    val colors = LocalReaderColors.current
    Surface(
        color = colors.surface,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 12.dp,
        modifier = Modifier
            .padding(20.dp)
            .navigationBarsPadding(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "${count}개 구절 선택됨", color = colors.onSurface)
            Spacer(Modifier.width(16.dp))
            TextButton(onClick = onClear) { Text("취소", color = colors.onSurfaceMuted) }
            Spacer(Modifier.width(4.dp))
            Surface(
                color = colors.accent,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.clickable { onShare() },
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = AppGlyphs.Share, color = Color.White, fontSize = 16.sp)
                    Spacer(Modifier.width(8.dp))
                    Text("카드 만들기", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
