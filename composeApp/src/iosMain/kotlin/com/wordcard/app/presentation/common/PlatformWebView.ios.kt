package com.wordcard.app.presentation.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropInteractionMode
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.WebKit.WKAudiovisualMediaTypeNone
import platform.WebKit.WKNavigation
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKUserContentController
import platform.WebKit.WKUserScript
import platform.WebKit.WKUserScriptInjectionTime
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class, ExperimentalComposeUiApi::class, BetaInteropApi::class)
@Composable
actual fun PlatformWebView(
    url: String,
    modifier: Modifier,
    injectedCss: String?,
    onProgressChange: ((Float) -> Unit)?,
) {
    val onProgressState = rememberUpdatedState(onProgressChange)
    val navigationDelegate = remember {
        object : NSObject(), WKNavigationDelegateProtocol {
            override fun webView(webView: WKWebView, didStartProvisionalNavigation: WKNavigation?) {
                onProgressState.value?.invoke(0f)
            }

            override fun webView(webView: WKWebView, didFinishNavigation: WKNavigation?) {
                onProgressState.value?.invoke(1f)
            }

            override fun webView(
                webView: WKWebView,
                didFailProvisionalNavigation: WKNavigation?,
                withError: NSError,
            ) {
                onProgressState.value?.invoke(1f)
            }

            override fun webView(
                webView: WKWebView,
                didFailNavigation: WKNavigation?,
                withError: NSError,
            ) {
                onProgressState.value?.invoke(1f)
            }
        }
    }

    UIKitView(
        modifier = modifier,
        factory = {
            val controller = WKUserContentController()
            injectedCss?.let { css ->
                controller.addUserScript(
                    WKUserScript(
                        source = buildCssInjectionScript(css),
                        injectionTime = WKUserScriptInjectionTime.WKUserScriptInjectionTimeAtDocumentStart,
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
            webView.navigationDelegate = navigationDelegate
            webView.scrollView.bounces = true
            webView.scrollView.alwaysBounceVertical = true
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
        // NonCooperative lets WKWebView's pan gesture win over any Compose
        // pointer handlers in ancestors (e.g. .clickable on a wrapping Surface),
        // which otherwise swallow the vertical scroll.
        properties = UIKitInteropProperties(
            interactionMode = UIKitInteropInteractionMode.NonCooperative,
            isNativeAccessibilityEnabled = true,
        ),
    )
}

private fun buildCssInjectionScript(css: String): String {
    val escaped = css
        .replace("\\", "\\\\")
        .replace("'", "\\'")
        .replace("\n", " ")
        .replace("\r", "")
    return """
        (function(){
          function inject(){
            if (document.getElementById('__wc_inject_style')) return;
            var s=document.createElement('style');
            s.id='__wc_inject_style';
            s.innerHTML='$escaped';
            (document.head||document.documentElement).appendChild(s);
          }
          inject();
          new MutationObserver(inject).observe(document.documentElement,{childList:true,subtree:true});
        })();
    """.trimIndent()
}
