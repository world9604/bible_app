package com.wordcard.app.presentation.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wordcard.app.domain.model.Verse
import com.wordcard.app.presentation.common.AppGlyphs
import com.wordcard.app.presentation.theme.LocalReaderColors
import com.wordcard.app.presentation.theme.LocalReaderTypography
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ReaderScreen(
    viewModel: ReaderViewModel = koinViewModel(),
    viewerSettings: ViewerSettings,
    onOpenSettings: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val readerColors = LocalReaderColors.current
    var showYouTubeShorts by remember { mutableStateOf(false) }

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
                onOpenYouTubeShorts = { showYouTubeShorts = true },
                onOpenSettings = onOpenSettings,
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
                            annotations = state.annotations,
                            onToggle = viewModel::toggleVerse,
                            onOpenMemo = { viewModel.openMemoEditor(it) },
                            settings = viewerSettings,
                        )
                    }
                }

                Column(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()) {
                    if (state.currentChapter != null && state.currentBook != null) {
                        val chapterNum = state.currentChapter!!.number
                        val totalChapters = state.currentBook!!.chapterCount
                        SideChapterNavRow(
                            canGoPrev = chapterNum > 1,
                            canGoNext = chapterNum < totalChapters,
                            onPrev = viewModel::previousChapter,
                            onNext = viewModel::nextChapter,
                        )
                    }
                    AnimatedVisibility(
                        visible = state.hasSelection,
                        enter = slideInVertically(initialOffsetY = { it }),
                        exit = slideOutVertically(targetOffsetY = { it }),
                    ) {
                        SelectionActionBar(
                            count = state.selectedVerseNumbers.size,
                            allBookmarked = state.allSelectedBookmarked,
                            showHighlightPicker = state.showHighlightPicker,
                            onClear = viewModel::clearSelection,
                            onShare = viewModel::openShareCard,
                            onBookmark = viewModel::toggleBookmarkOnSelection,
                            onToggleHighlightPicker = viewModel::toggleHighlightPicker,
                            onPickHighlight = viewModel::applyHighlight,
                            onMemo = { viewModel.openMemoEditor() },
                        )
                    }
                    if (!state.hasSelection) {
                        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                    }
                }
            }
        }

        if (state.showBookPicker) {
            BookPickerSheet(
                books = state.books,
                currentBookId = state.currentBook?.id,
                currentChapter = state.currentChapter?.number,
                onPickChapter = viewModel::selectBookAndChapter,
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

        if (showYouTubeShorts && state.currentBook != null && state.currentChapter != null) {
            YouTubeShortsSheet(
                bookName = state.currentBook!!.name,
                chapter = state.currentChapter!!.number,
                onDismiss = { showYouTubeShorts = false },
            )
        }

        if (state.showMemoEditor && state.currentBook != null && state.currentChapter != null) {
            val verse = state.memoEditingVerse!!
            val hasExisting = state.annotations[verse]?.memo?.isNotBlank() == true
            MemoEditorSheet(
                bookName = state.currentBook!!.name,
                chapter = state.currentChapter!!.number,
                verseNumber = verse,
                draft = state.memoDraft,
                hasExistingMemo = hasExisting,
                onChange = viewModel::updateMemoDraft,
                onSave = viewModel::saveMemo,
                onDelete = viewModel::deleteMemo,
                onDismiss = viewModel::dismissMemoEditor,
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
    onOpenYouTubeShorts: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val colors = LocalReaderColors.current
    val typo = LocalReaderTypography.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = AppGlyphs.TableOfContents,
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
        Spacer(Modifier.weight(1f))
        Text(
            text = AppGlyphs.Shorts,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.onSurface,
            modifier = Modifier
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) { onOpenYouTubeShorts() }
                .padding(horizontal = 4.dp, vertical = 6.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = AppGlyphs.Settings,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.onSurface,
            modifier = Modifier
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) { onOpenSettings() }
                .padding(horizontal = 4.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun ChapterContent(
    verses: List<Verse>,
    selected: Set<Int>,
    annotations: Map<Int, com.wordcard.app.domain.model.VerseAnnotation>,
    onToggle: (Int) -> Unit,
    onOpenMemo: (Int) -> Unit,
    settings: ViewerSettings,
) {
    val listState = rememberLazyListState()
    val colors = LocalReaderColors.current
    val typo = LocalReaderTypography.current

    LaunchedEffect(verses.firstOrNull()?.bookId, verses.firstOrNull()?.chapter) {
        listState.scrollToItem(0)
    }

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(
            start = settings.horizontalMarginDp.dp,
            end = settings.horizontalMarginDp.dp,
            top = settings.verticalMarginTopDp.dp,
            bottom = settings.verticalMarginBottomDp.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(settings.paragraphSpacingDp.dp),
    ) {
        items(verses, key = { it.number }) { verse ->
            val isSelected = verse.number in selected
            val annotation = annotations[verse.number]
            val highlight = annotation?.highlight
            val bg = when {
                isSelected -> colors.selection
                highlight != null -> colors.highlightOf(highlight)
                else -> Color.Transparent
            }
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
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp, end = 6.dp)
                        .width(3.dp)
                        .height(18.dp)
                        .clip(RoundedCornerShape(1.5.dp))
                        .background(
                            if (annotation?.bookmarked == true) colors.verseNumber else Color.Transparent,
                        ),
                )
                Text(
                    text = "${verse.number}",
                    style = typo.verseNumber,
                    color = colors.verseNumber,
                    modifier = Modifier
                        .padding(top = 2.dp, end = 10.dp),
                )
                Text(
                    text = verse.text,
                    style = typo.body,
                    color = colors.onSurface,
                    modifier = Modifier.weight(1f),
                )
                if (annotation?.memo?.isNotBlank() == true) {
                    Text(
                        text = AppGlyphs.Note,
                        fontFamily = typo.iconFontFamily,
                        fontSize = 18.sp,
                        color = colors.verseNumber,
                        modifier = Modifier
                            .padding(start = 8.dp, top = 2.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                            ) { onOpenMemo(verse.number) }
                            .padding(horizontal = 2.dp),
                    )
                }
            }
        }
        item {
            Spacer(Modifier.height(120.dp))
        }
    }
}

@Composable
private fun SelectionActionBar(
    count: Int,
    allBookmarked: Boolean,
    showHighlightPicker: Boolean,
    onClear: () -> Unit,
    onShare: () -> Unit,
    onBookmark: () -> Unit,
    onToggleHighlightPicker: () -> Unit,
    onPickHighlight: (com.wordcard.app.domain.model.HighlightColor?) -> Unit,
    onMemo: () -> Unit,
) {
    val colors = LocalReaderColors.current
    val typo = LocalReaderTypography.current
    Surface(
        color = colors.background,
        shape = RoundedCornerShape(2.dp),
        shadowElevation = 0.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.navigationBarsPadding()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(colors.verseNumber.copy(alpha = 0.3f))
            )

            AnimatedVisibility(
                visible = showHighlightPicker,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
            ) {
                HighlightPickerRow(onPick = onPickHighlight)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
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
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ActionPill(
                    glyph = AppGlyphs.Bookmark,
                    label = if (allBookmarked) "책갈피 해제" else "책갈피",
                    active = allBookmarked,
                    onClick = onBookmark,
                    modifier = Modifier.weight(1f),
                )
                ActionPill(
                    glyph = AppGlyphs.Highlight,
                    label = "형광펜",
                    active = showHighlightPicker,
                    onClick = onToggleHighlightPicker,
                    modifier = Modifier.weight(1f),
                )
                ActionPill(
                    glyph = AppGlyphs.Note,
                    label = "메모",
                    active = false,
                    enabled = count == 1,
                    onClick = onMemo,
                    modifier = Modifier.weight(1f),
                )
                ActionPill(
                    glyph = AppGlyphs.Share,
                    label = "카드",
                    active = false,
                    onClick = onShare,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun ActionPill(
    glyph: String,
    label: String,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = LocalReaderColors.current
    val typo = LocalReaderTypography.current
    val bg = if (active) colors.selection else Color.Transparent
    val fg = when {
        !enabled -> colors.onSurfaceMuted.copy(alpha = 0.4f)
        active -> colors.accent
        else -> colors.onSurface
    }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .clickable(
                enabled = enabled,
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
            ) { onClick() }
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = glyph,
            fontFamily = typo.iconFontFamily,
            fontSize = 22.sp,
            color = fg,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            style = typo.chrome.copy(fontSize = 11.sp),
            color = fg,
        )
    }
}

@Composable
private fun HighlightPickerRow(
    onPick: (com.wordcard.app.domain.model.HighlightColor?) -> Unit,
) {
    val colors = LocalReaderColors.current
    val typo = LocalReaderTypography.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ColorDot(color = colors.highlightYellow, onClick = { onPick(com.wordcard.app.domain.model.HighlightColor.Yellow) })
        ColorDot(color = colors.highlightGreen, onClick = { onPick(com.wordcard.app.domain.model.HighlightColor.Green) })
        ColorDot(color = colors.highlightBlue, onClick = { onPick(com.wordcard.app.domain.model.HighlightColor.Blue) })
        ColorDot(color = colors.highlightPink, onClick = { onPick(com.wordcard.app.domain.model.HighlightColor.Pink) })
        ColorDot(color = colors.highlightLavender, onClick = { onPick(com.wordcard.app.domain.model.HighlightColor.Lavender) })
        Spacer(Modifier.weight(1f))
        Text(
            text = "지우기",
            style = typo.chrome,
            color = colors.onSurfaceMuted,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) { onPick(null) }
                .padding(horizontal = 10.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun ColorDot(color: Color, onClick: () -> Unit) {
    val readerColors = LocalReaderColors.current
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(color)
            .border(BorderStroke(1.dp, readerColors.verseNumber.copy(alpha = 0.4f)), CircleShape)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
            ) { onClick() },
    )
}

@Composable
private fun SideChapterNavRow(
    canGoPrev: Boolean,
    canGoNext: Boolean,
    onPrev: () -> Unit,
    onNext: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(horizontal = 16.dp),
    ) {
        if (canGoPrev) {
            NavCircleButton(
                modifier = Modifier.align(Alignment.CenterStart),
                glyph = AppGlyphs.ChevronLeft,
                onClick = onPrev,
            )
        }
        if (canGoNext) {
            NavCircleButton(
                modifier = Modifier.align(Alignment.CenterEnd),
                glyph = AppGlyphs.ChevronRight,
                onClick = onNext,
            )
        }
    }
}

@Composable
private fun NavCircleButton(
    modifier: Modifier = Modifier,
    glyph: String,
    onClick: () -> Unit,
) {
    val colors = LocalReaderColors.current
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(colors.selection)
            .border(BorderStroke(1.5.dp, Color.White), CircleShape)
            .clickable(indication = null, interactionSource = interaction) { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = glyph,
            fontSize = 24.sp,
            lineHeight = 24.sp,
            color = colors.onSurface,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            style = androidx.compose.ui.text.TextStyle(
                lineHeightStyle = androidx.compose.ui.text.style.LineHeightStyle(
                    alignment = androidx.compose.ui.text.style.LineHeightStyle.Alignment.Center,
                    trim = androidx.compose.ui.text.style.LineHeightStyle.Trim.Both,
                ),
            ),
        )
    }
}

