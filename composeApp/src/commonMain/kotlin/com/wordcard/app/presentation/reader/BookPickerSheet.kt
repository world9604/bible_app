package com.wordcard.app.presentation.reader

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.wordcard.app.domain.model.Book
import com.wordcard.app.domain.model.Testament
import com.wordcard.app.presentation.theme.LocalReaderColors

@Composable
fun BookPickerSheet(
    books: List<Book>,
    onPick: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    BottomSheetScaffold(onDismiss = onDismiss) {
        val colors = LocalReaderColors.current
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
                    items(oldT, key = { it.id }) { BookRow(it) { onPick(it.id) } }
                }
                if (newT.isNotEmpty()) {
                    item { SectionHeader("신약") }
                    items(newT, key = { it.id }) { BookRow(it) { onPick(it.id) } }
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
private fun BookRow(book: Book, onClick: () -> Unit) {
    val colors = LocalReaderColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(book.name, color = colors.onSurface, fontSize = 16.sp)
        Spacer(Modifier.weight(1f))
        Text("${book.chapterCount}장", color = colors.onSurfaceMuted, fontSize = 13.sp)
    }
}
