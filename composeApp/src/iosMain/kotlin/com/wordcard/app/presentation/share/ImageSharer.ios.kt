package com.wordcard.app.presentation.share

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIWindow

@Composable
actual fun rememberImageSharer(): ImageSharer = remember { IosImageSharer() }

private class IosImageSharer : ImageSharer {
    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    override suspend fun share(image: ImageBitmap, text: String) {
        val skiaImage = Image.makeFromBitmap(image.asSkiaBitmap())
        val pngBytes = skiaImage.encodeToData(EncodedImageFormat.PNG)?.bytes ?: return
        val nsData = pngBytes.usePinned { pinned ->
            NSData.create(bytes = pinned.addressOf(0), length = pngBytes.size.toULong())
        }
        val uiImage = UIImage.imageWithData(nsData) ?: return

        val controller = UIActivityViewController(
            activityItems = listOf(uiImage, text),
            applicationActivities = null,
        )

        val rootController = UIApplication.sharedApplication
            .connectedScenes
            .map { it as? platform.UIKit.UIWindowScene }
            .firstOrNull()
            ?.windows
            ?.firstOrNull { (it as? UIWindow)?.isKeyWindow() == true }
            ?.let { it as? UIWindow }
            ?.rootViewController
            ?: UIApplication.sharedApplication.keyWindow?.rootViewController
            ?: return

        rootController.presentViewController(controller, animated = true, completion = null)
    }
}
