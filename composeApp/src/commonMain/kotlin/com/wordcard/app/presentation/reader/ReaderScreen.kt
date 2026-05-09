package com.wordcard.app.presentation.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.remember
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
                onBack = viewModel::openBookPicker,
                onTitleClick = viewModel::openChapterPicker,
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

                Column(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()) {
                    AnimatedVisibility(
                        visible = state.hasSelection,
                        enter = slideInVertically(initialOffsetY = { it }),
                        exit = slideOutVertically(targetOffsetY = { it }),
                    ) {
                        SelectionActionBar(
                            count = state.selectedVerseNumbers.size,
                            onClear = viewModel::clearSelection,
                            onShare = viewModel::openShareCard,
                        )
                    }
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
    onBack: () -> Unit,
    onTitleClick: () -> Unit,
) {
    val colors = LocalReaderColors.current
    val typo = LocalReaderTypography.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 56.dp, end = 56.dp, top = 50.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = AppGlyphs.Back,
            style = typo.icon,
            color = colors.onSurface,
            modifier = Modifier
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) { onBack() },
        )
        Spacer(Modifier.weight(1f))
        if (bookName.isNotBlank() && chapter != null) {
            Text(
                text = "$bookName ${chapter}장",
                style = typo.topBar,
                color = colors.onSurface,
                modifier = Modifier
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                    ) { onTitleClick() },
            )
        }
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
        contentPadding = PaddingValues(start = 72.dp, end = 64.dp, top = 56.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        items(verses, key = { it.number }) { verse ->
            val isSelected = verse.number in selected
            val bg = if (isSelected) colors.selection else Color.Transparent
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(bg)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                    ) { onToggle(verse.number) }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = "${verse.number}",
                    style = typo.verseNumber,
                    color = colors.verseNumber,
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .width(32.dp),
                )
                Text(
                    text = verse.text,
                    style = typo.body,
                    color = colors.onSurface,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        item {
            Spacer(Modifier.height(40.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${AppGlyphs.ChevronLeft}  이전",
                    style = typo.chrome,
                    color = colors.onSurfaceMuted,
                    modifier = Modifier
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                        ) { onPrevChapter() }
                        .padding(8.dp),
                )
                Text(
                    text = "다음  ${AppGlyphs.ChevronRight}",
                    style = typo.chrome,
                    color = colors.onSurfaceMuted,
                    modifier = Modifier
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                        ) { onNextChapter() }
                        .padding(8.dp),
                )
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
            fontStyle = typo.verseNumber.fontStyle,
            fontFamily = typo.verseNumber.fontFamily,
            letterSpacing = typo.verseNumber.letterSpacing,
            color = numberColor,
            baselineShift = androidx.compose.ui.text.style.BaselineShift.Superscript,
        )
    ) { append("${verse.number}") }
    append("  ")
    append(verse.text)
}

@Composable
private fun SelectionActionBar(
    count: Int,
    onClear: () -> Unit,
    onShare: () -> Unit,
) {
    val colors = LocalReaderColors.current
    val typo = LocalReaderTypography.current
    val onAccent = if (colors.accent.luminance() > 0.5f) Color(0xFF111111) else Color(0xFFFAFAF7)
    Surface(
        color = colors.background,
        shape = RoundedCornerShape(2.dp),
        shadowElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(colors.verseNumber.copy(alpha = 0.3f))
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${count}개 선택",
                    style = typo.chrome,
                    color = colors.onSurfaceMuted,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "취소",
                    style = typo.chrome,
                    color = colors.onSurfaceMuted,
                    modifier = Modifier
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                        ) { onClear() }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                )
                Spacer(Modifier.width(4.dp))
                Surface(
                    color = colors.accent,
                    shape = RoundedCornerShape(2.dp),
                    modifier = Modifier.clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                    ) { onShare() },
                ) {
                    Text(
                        text = "카드 만들기",
                        style = typo.chrome,
                        color = onAccent,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                    )
                }
            }
        }
    }
}

private fun Color.luminance(): Float =
    0.299f * red + 0.587f * green + 0.114f * blue
