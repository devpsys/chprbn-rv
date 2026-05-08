package ng.com.chprbn.mobile

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import ng.com.chprbn.mobile.core.persistence.encryption.DatabaseMigrationGuard

@HiltAndroidApp
class ChprbnApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // sqlcipher-android 4.x resolves JNI symbols by linker dependency, but an
        // explicit System.loadLibrary("sqlcipher") here forces the pull-out of the
        // native .so before any DAO is opened so failures surface at app start
        // rather than buried under a Room coroutine.
        System.loadLibrary("sqlcipher")

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
