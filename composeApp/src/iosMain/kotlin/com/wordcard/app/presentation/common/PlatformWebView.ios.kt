package com.wordcard.app.presentation.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.WebKit.WKAudiovisualMediaTypeNone
import platform.WebKit.WKUserContentController
import platform.WebKit.WKUserScript
import platform.WebKit.WKUserScriptInjectionTimeAtDocumentEnd
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun PlatformWebView(url: String, modifier: Modifier, injectedCss: String?) {
    UIKitView(
        modifier = modifier,
        factory = {
            val controller = WKUserContentController()
            injectedCss?.let { css ->
                controller.addUserScript(
                    WKUserScript(
                        source = buildCssInjectionScript(css),
                        injectionTime = WKUserScriptInjectionTimeAtDocumentEnd,
                        forMainFrameOnly = false,
                    ),
                )
            }
            val config = WKWebViewConfiguration().apply {
                allowsInlineMediaPlayback = true
                mediaTypesRequiringUserActionForPlayback = WKAudiovisualMediaTypeNone
                userContentController = controller
            }
            val webView = WKWebView(
                frame = CGRectMake(0.0, 0.0, 0.0, 0.0),
                configuration = config,
            )
            NSURL.URLWithString(url)?.let { nsUrl ->
                webView.loadRequest(NSURLRequest.requestWithURL(nsUrl))
            }
            webView
        },
        update = { webView ->
            val current = webView.URL?.absoluteString
            if (current != url) {
                NSURL.URLWithString(url)?.let { nsUrl ->
                    webView.loadRequest(NSURLRequest.requestWithURL(nsUrl))
                }
            }
        },
    )
}

private fun buildCssInjectionScript(css: String): String {
    val escaped = css
        .replace("\\", "\\\\")
        .replace("'", "\\'")
        .replace("\n", " ")
        .replace("\r", "")
    return "(function(){var s=document.createElement('style');s.innerHTML='$escaped';document.head.appendChild(s);})();"
}
