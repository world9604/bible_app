package com.wordcard.app.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.wordcard.app.database.BibleDatabase

actual class DatabaseDriverFactory actual constructor() {
    actual fun createOrNull(): SqlDriver? = NativeSqliteDriver(
        schema = BibleDatabase.Schema,
        name = "bible.db",
    )
}
