package com.wordcard.app.presentation.common

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import kotlinx.browser.document
import org.w3c.dom.HTMLIFrameElement

@Composable
actual fun PlatformWebView(url: String, modifier: Modifier) {
    val density = LocalDensity.current.density
    val iframe = remember {
        (document.createElement("iframe") as HTMLIFrameElement).apply {
            style.position = "absolute"
            style.border = "0"
            style.zIndex = "1000"
            setAttribute("allow", "autoplay; encrypted-media; picture-in-picture; fullscreen")
            setAttribute("allowfullscreen", "true")
        }
    }

    DisposableEffect(iframe) {
        document.body?.appendChild(iframe)
        onDispose { iframe.remove() }
    }

    DisposableEffect(url) {
        iframe.src = url
        onDispose { }
    }

    Box(
        modifier = modifier.onGloballyPositioned { coords ->
            val pos = coords.positionInWindow()
            val cssX = pos.x / density
            val cssY = pos.y / density
            val cssW = coords.size.width / density
            val cssH = coords.size.height / density
            iframe.style.left = "${cssX}px"
            iframe.style.top = "${cssY}px"
            iframe.style.width = "${cssW}px"
            iframe.style.height = "${cssH}px"
        },
    )
}
