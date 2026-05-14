package com.wordcard.app.data.local

import app.cash.sqldelight.db.SqlDriver

/**
 * Browser/Wasm has no native SQLite. Returning null causes the DI module to fall back
 * to the in-memory repositories. A future implementation could swap in SQLDelight's
 * `web-worker-driver` (sql.js + IndexedDB persistence) without changing any other code.
 */
actual class DatabaseDriverFactory actual constructor() {
    actual fun createOrNull(): SqlDriver? = null
}
