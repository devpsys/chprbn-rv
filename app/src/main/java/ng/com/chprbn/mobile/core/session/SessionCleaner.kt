package ng.com.chprbn.mobile.core.session

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ng.com.chprbn.mobile.core.sync.SyncJobDao

/**
 * Runs every [SessionScopedCleaner] contributed by the feature modules,
 * then wipes the shared sync queue.
 *
 * **Currently unused/unwired.** Was called from `ProfileRepositoryImpl.logout()`
 * (A2 audit finding — shared-device threat model, next user on the
 * device shouldn't inherit cached rows), but that call was removed on
 * explicit request: logout must not clear any downloaded data. Kept in
 * case a distinct "switch account" / "wipe all data" flow wants it later.
 */
@Singleton
class SessionCleaner @Inject constructor(
    private val cleaners: Set<@JvmSuppressWildcards SessionScopedCleaner>,
    private val syncJobDao: SyncJobDao,
) {

    suspend fun clearAllFeatures() = withContext(Dispatchers.IO) {
        for (cleaner in cleaners) {
            runCatching { cleaner.clear() }.onFailure { t ->
                Log.w(TAG, "SessionScopedCleaner failed: ${t.message}", t)
            }
        }
        runCatching { syncJobDao.clearAll() }.onFailure { t ->
            Log.w(TAG, "SyncJobDao.clearAll failed: ${t.message}", t)
        }
    }

    private companion object {
        const val TAG = "SessionCleaner"
    }
}
