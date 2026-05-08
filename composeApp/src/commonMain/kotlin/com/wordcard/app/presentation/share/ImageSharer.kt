package com.wordcard.app.presentation.share

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap

/**
 * 플랫폼별 이미지 공유 추상화.
 * iOS: UIActivityViewController
 * Android: Intent.ACTION_SEND
 */
interface ImageSharer {
    suspend fun share(image: ImageBitmap, text: String)
}

@Composable
expect fun rememberImageSharer(): ImageSharer
