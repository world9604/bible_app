package com.wordcard.app.presentation.share

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

@Composable
actual fun rememberImageSharer(): ImageSharer {
    val context = LocalContext.current
    return remember(context) { AndroidImageSharer(context.applicationContext) }
}

private class AndroidImageSharer(private val context: Context) : ImageSharer {
    override suspend fun share(image: ImageBitmap, text: String) {
        val androidBitmap: Bitmap = image.asAndroidBitmap()
        val cacheDir = File(context.cacheDir, "shared").apply { mkdirs() }
        val file = File(cacheDir, "verse_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out ->
            androidBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, text)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "공유").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }
}
