package com.wordcard.app.presentation.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * @param onProgressChange called with values in [0f, 1f]. 0f when a load starts, 1f when finished.
 *                         Android reports intermediate values; iOS and wasmJs emit only 0f/1f.
 */
@Composable
expect fun PlatformWebView(
    url: String,
    modifier: Modifier = Modifier,
    injectedCss: String? = null,
    onProgressChange: ((Float) -> Unit)? = null,
)
