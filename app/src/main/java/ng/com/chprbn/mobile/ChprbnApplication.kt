package ng.com.chprbn.mobile

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import net.sqlcipher.database.SQLiteDatabase
import ng.com.chprbn.mobile.core.persistence.encryption.DatabaseMigrationGuard

@HiltAndroidApp
class ChprbnApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // SQLCipher 4.x requires loadLibs() before any encrypted database is opened;
        // it pulls the native .so out of the APK and resolves JNI symbols.
        SQLiteDatabase.loadLibs(this)

        // One-shot wipe of any pre-SQLCipher unencrypted auth.db / scan.db files.
        // Must run before Hilt provides the Room databases (which happens lazily on
        // first DAO injection inside a ViewModel coroutine).
        val prefs = getSharedPreferences(
            DatabaseMigrationGuard.PREFS_FILE,
            MODE_PRIVATE
        )
        DatabaseMigrationGuard(this, prefs).migrateIfNeeded()
    }
}
