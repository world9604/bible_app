package com.wordcard.app.presentation.share

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wordcard.app.domain.model.Verse
import com.wordcard.app.domain.usecase.BuildShareCardUseCase
import com.wordcard.app.presentation.common.AppGlyphs
import com.wordcard.app.presentation.theme.LocalReaderColors
import com.wordcard.app.presentation.theme.LocalReaderTypography
import kotlinx.coroutines.launch

@Composable
fun ShareCardScreen(
    verses: List<Verse>,
    bookName: String,
    onDismiss: () -> Unit,
) {
    val build = remember { BuildShareCardUseCase() }
    val content = remember(verses, bookName) { build(verses, bookName) }
    val backgrounds = DefaultCardBackgrounds
    var selectedBg by remember { mutableStateOf(backgrounds.first()) }
    val sharer = rememberImageSharer()
    val scope = rememberCoroutineScope()
    val graphicsLayer = rememberGraphicsLayer()
    val readerColors = LocalReaderColors.current

    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black.copy(alpha = 0.92f)) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("닫기 ${AppGlyphs.Close}", color = Color.White, modifier = Modifier.clickable { onDismiss() })
                Spacer(Modifier.weight(1f))
                Text("미리보기", color = Color.White, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.weight(1f))
                Text(" ", color = Color.Transparent)
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                CardCanvas(
                    title = content.title,
                    body = content.body,
                    reference = content.reference,
                    background = selectedBg,
                    graphicsLayer = graphicsLayer,
                )
            }

            BackgroundSelector(
                backgrounds = backgrounds,
                selectedId = selectedBg.id,
                onSelect = { selectedBg = it },
            )

            Spacer(Modifier.height(12.dp))

            ShareButton(
                accent = selectedBg.accentColor,
                onClick = {
                    scope.launch {
                        val bitmap: ImageBitmap = graphicsLayer.toImageBitmap()
                        sharer.share(
                            image = bitmap,
                            text = "${content.body}\n\n— ${content.reference}",
                        )
                    }
                },
            )

            Spacer(Modifier.navigationBarsPadding().height(16.dp))
        }
    }
}

@Composable
private fun CardCanvas(
    title: String,
    body: String,
    reference: String,
    background: CardBackground,
    graphicsLayer: GraphicsLayer,
) {
    val typo = LocalReaderTypography.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(24.dp))
            .background(background.brush)
            .drawWithContent {
                graphicsLayer.record { this@drawWithContent.drawContent() }
                drawLayer(graphicsLayer)
            },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = title,
                color = background.onColor.copy(alpha = 0.7f),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = body,
                style = typo.cardBody,
                color = background.onColor,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 24.dp, height = 2.dp)
                        .background(background.accentColor.copy(alpha = 0.7f))
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = reference,
                    style = typo.cardReference,
                    color = background.onColor,
                )
            }
        }
    }
}

@Composable
private fun BackgroundSelector(
    backgrounds: List<CardBackground>,
    selectedId: String,
    onSelect: (CardBackground) -> Unit,
) {
    LazyRow(
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(backgrounds, key = { it.id }) { bg ->
            val isSelected = bg.id == selectedId
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(bg.brush)
                    .border(
                        width = if (isSelected) 3.dp else 0.dp,
                        color = if (isSelected) Color.White else Color.Transparent,
                        shape = RoundedCornerShape(14.dp),
                    )
                    .clickable { onSelect(bg) },
                contentAlignment = Alignment.Center,
            ) {
                if (isSelected) {
                    Text(AppGlyphs.Check, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ShareButton(accent: Color, onClick: () -> Unit) {
    Surface(
        color = accent,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .clickable { onClick() },
    ) {
        Box(
            modifier = Modifier.padding(vertical = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "${AppGlyphs.Share}  공유하기",
                color = Color.Black.copy(alpha = 0.85f),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
            )
        }
    }
}
