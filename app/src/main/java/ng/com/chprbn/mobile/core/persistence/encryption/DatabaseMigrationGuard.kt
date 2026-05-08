package ng.com.chprbn.mobile.core.persistence.encryption

import android.content.Context
import android.content.SharedPreferences

/**
 * One-shot deletion of the legacy unencrypted Room database files when the app is
 * upgraded to the SQLCipher-encrypted variant.
 *
 * The pre-SQLCipher app stored `auth.db` and `scan.db` as plaintext SQLite files.
 * Once SupportFactory is wired into Room, opening those files with the SQLCipher
 * helper would fail — so on first launch after upgrade we delete them, letting
 * Room re-create them as encrypted databases. The auth token (held in
 * EncryptedSharedPreferences, separate file) survives, so users do not have to
 * sign in again; only cached license records and pending sync entries are lost.
 *
 * Gated by [GUARD_MARKER_KEY] in a plain SharedPreferences file so the migration
 * runs exactly once per install. Subsequent launches are a no-op.
 *
 * Run from [android.app.Application.onCreate] before any DAO is touched, otherwise
 * Room may attempt to open the legacy files first and crash.
 */
class DatabaseMigrationGuard(
    private val context: Context,
    private val prefs: SharedPreferences,
    private val legacyDbNames: List<String> = listOf("auth.db", "scan.db")
) {

    fun migrateIfNeeded() {
        if (prefs.getBoolean(GUARD_MARKER_KEY, false)) return

        for (name in legacyDbNames) {
            // deleteDatabase() also removes the -journal, -wal, and -shm sidecars.
            context.deleteDatabase(name)
        }

        prefs.edit().putBoolean(GUARD_MARKER_KEY, true).commit()
    }

    companion object {
        const val GUARD_MARKER_KEY = "sqlcipher_migration_v1_done"
        const val PREFS_FILE = "db_migration_guard"
    }
}
