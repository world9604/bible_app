package com.wordcard.app.presentation.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wordcard.app.presentation.theme.LocalReaderColors

@Composable
fun ChapterPickerSheet(
    bookName: String,
    chapterCount: Int,
    current: Int,
    onPick: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    BottomSheetScaffold(onDismiss = onDismiss) {
        val colors = LocalReaderColors.current
        Column(modifier = Modifier.fillMaxSize().padding(top = 12.dp).navigationBarsPadding()) {
            DragHandle()
            Text(
                text = "$bookName · 장 선택",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items((1..chapterCount).toList()) { n ->
                    val selected = n == current
                    val bg = if (selected) colors.accent else colors.background
                    val fg = if (selected) androidx.compose.ui.graphics.Color.White else colors.onSurface
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(bg)
                            .clickable { onPick(n) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "$n",
                            color = fg,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        )
                    }
                }
            }
        }
    }
}
