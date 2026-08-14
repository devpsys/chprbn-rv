package ng.com.chprbn.mobile

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import coil.ImageLoader
import coil.ImageLoaderFactory
import dagger.hilt.android.HiltAndroidApp
import ng.com.chprbn.mobile.core.network.DataUriFetcher
import ng.com.chprbn.mobile.core.persistence.encryption.DatabaseMigrationGuard
import javax.inject.Inject

@HiltAndroidApp
class ChprbnApplication : Application(), Configuration.Provider, ImageLoaderFactory {

    @Inject lateinit var workerFactory: HiltWorkerFactory

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

    // Wires Hilt's WorkerFactory into WorkManager so @HiltWorker workers
    // (currently: SyncWorker) get their dependencies injected. The default
    // androidx.startup initializer is disabled in AndroidManifest.xml so this
    // Configuration is used instead.
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    // Registers DataUriFetcher so every `data:image/...;base64,...` photo
    // string (candidate photos, exam candidates, verification records) is
    // actually fetchable — Coil has no built-in fetcher for the `data` scheme.
    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .components { add(DataUriFetcher.Factory()) }
            .build()
}
