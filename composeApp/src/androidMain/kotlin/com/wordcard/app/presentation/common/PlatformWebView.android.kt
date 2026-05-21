package com.wordcard.app.presentation.common

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
@Composable
actual fun PlatformWebView(
    url: String,
    modifier: Modifier,
    injectedCss: String?,
    onProgressChange: ((Float) -> Unit)?,
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    mediaPlaybackRequiresUserGesture = false
                }
                isVerticalScrollBarEnabled = true
                isHorizontalScrollBarEnabled = false
                overScrollMode = WebView.OVER_SCROLL_NEVER
                setOnTouchListener { v, event ->
                    when (event.actionMasked) {
                        MotionEvent.ACTION_DOWN,
                        MotionEvent.ACTION_MOVE,
                        -> v.parent?.requestDisallowInterceptTouchEvent(true)
                    }
                    false
                }
                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView, startedUrl: String?, favicon: android.graphics.Bitmap?) {
                        super.onPageStarted(view, startedUrl, favicon)
                        onProgressChange?.invoke(0f)
                    }

                    override fun onPageFinished(view: WebView, finishedUrl: String?) {
                        super.onPageFinished(view, finishedUrl)
                        injectedCss?.let { css ->
                            view.evaluateJavascript(buildCssInjectionScript(css), null)
                        }
                        onProgressChange?.invoke(1f)
                    }
                }
                webChromeClient = object : WebChromeClient() {
                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        super.onProgressChanged(view, newProgress)
                        onProgressChange?.invoke(newProgress / 100f)
                    }
                }
                loadUrl(url)
            }
        },
        update = { webView ->
            if (webView.url != url) webView.loadUrl(url)
        },
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
