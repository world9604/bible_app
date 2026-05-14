package com.wordcard.app.data.local

import app.cash.sqldelight.db.SqlDriver

/**
 * Platform-specific entry point for creating the SQLite driver that backs [com.wordcard.app.database.BibleDatabase].
 *
 * Implementations return `null` when local persistence is not available on the current
 * platform (e.g. wasmJs in the browser, where we currently fall back to in-memory storage).
 * Callers must then degrade gracefully — see [com.wordcard.app.di.appModules].
 */
expect class DatabaseDriverFactory() {
    fun createOrNull(): SqlDriver?
}
