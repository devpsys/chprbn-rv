package ng.com.chprbn.mobile.feature.auth.data.local

import android.util.Log
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Seeds the auth DB with a demo row (empty token) so the first launch still shows login.
 * A real session is created only after a successful online login (token stored in Room).
 */
class AuthSeedCallback : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)

        val sql = """
            INSERT OR IGNORE INTO auth_user(id, username, email, fullName, accessToken, permissions, userPhoto, role, staffId, unit, organization, lastLoginAt)
            VALUES (
              'offline-test-user',
              'OFFLINE-DEMO',
              'offline@chprbn.gov.ng',
              'Sylux Endyusa',
              '',
              '["auth:login"]',
              NULL,
              'Field Officer',
              '44920',
              'Unit 7B',
              'Health Council',
              'Today, 10:30 AM'
            )
        """.trimIndent()

        runCatching {
            db.execSQL(sql)
        }.onFailure { t ->
            Log.w("AuthSeedCallback", "Failed to seed auth db: ${t.message}")
        }
    }
}
