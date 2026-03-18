package ng.com.chprbn.mobile.feature.auth.data.local

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import android.util.Log

/**
 * Seeds the auth DB with a test user for offline login.
 *
 * Note: for production, move this to a debug-only build flavor or remove entirely.
 */
class AuthSeedCallback : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)

        // Store permissions as JSON string because we use a Gson-backed TypeConverter.
        // Table schema:
        // - auth_user(id TEXT PRIMARY KEY, email TEXT, fullName TEXT, accessToken TEXT, permissions TEXT, userPhoto TEXT)
        val sql = """
            INSERT OR IGNORE INTO auth_user(id, email, fullName, accessToken, permissions, userPhoto)
            VALUES (
              'offline-test-user',
              'offline@chprbn.gov.ng',
              'Offline Test Practitioner',
              'offline-token',
              '["auth:login"]',
              NULL
            )
        """.trimIndent()

        runCatching {
            db.execSQL(sql)
        }.onFailure { t ->
            Log.w("AuthSeedCallback", "Failed to seed auth db: ${t.message}")
        }
    }
}

