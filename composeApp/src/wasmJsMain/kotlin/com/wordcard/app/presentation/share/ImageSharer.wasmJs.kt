package com.wordcard.app.presentation.share

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap

@Composable
actual fun rememberImageSharer(): ImageSharer = remember { WebImageSharer() }

private class WebImageSharer : ImageSharer {
    override suspend fun share(image: ImageBitmap, text: String) {
        // 웹 빌드는 미리보기용. 시스템 공유 시트 없음.
        kotlinx.browser.window.alert("공유 기능은 모바일에서만 동작합니다.\n$text")
    }
}
