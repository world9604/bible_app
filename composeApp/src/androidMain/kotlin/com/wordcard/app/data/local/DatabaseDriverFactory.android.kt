package com.wordcard.app.data.local

import android.app.Application
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.wordcard.app.database.BibleDatabase

/**
 * Android driver factory. The hosting [Application] must be assigned to
 * [AndroidAppContext.application] before the first injection — done from MainActivity.
 *
 * Returns `null` when the application context has not yet been registered.
 */
actual class DatabaseDriverFactory actual constructor() {
    actual fun createOrNull(): SqlDriver? {
        val app = AndroidAppContext.application ?: return null
        return AndroidSqliteDriver(
            schema = BibleDatabase.Schema,
            context = app,
            name = "bible.db",
        )
    }
}

/**
 * Static holder for the Android [Application]. Assigned once in MainActivity.onCreate().
 * Required because Koin in this project is started inside the Compose tree (KoinApplication),
 * so the framework's standard `androidContext()` is not available at module-definition time.
 */
object AndroidAppContext {
    @Volatile var application: Application? = null
}
