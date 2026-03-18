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
        // Table schema includes role, staffId, unit (nullable).
        val sql = """
            INSERT OR IGNORE INTO auth_user(id, email, fullName, accessToken, permissions, userPhoto, role, staffId, unit)
            VALUES (
              'offline-test-user',
              'offline@chprbn.gov.ng',
              'Sylux Endyusa',
              'offline-token',
              '["auth:login"]',
              NULL,
              'Field Officer',
              '44920',
              'Unit 7B'
            )
        """.trimIndent()

        runCatching {
            db.execSQL(sql)
        }.onFailure { t ->
            Log.w("AuthSeedCallback", "Failed to seed auth db: ${t.message}")
        }
    }
}

