package ng.com.chprbn.mobile.core.session

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ng.com.chprbn.mobile.core.sync.SyncJobDao

/**
 * Runs every [SessionScopedCleaner] contributed by the feature modules,
 * then wipes the shared sync queue. Called from [ProfileRepositoryImpl.logout]
 * (via injection) so the next user on the device starts with an empty
 * cache — closes the A2 audit gap.
 *
 * Auth-side clears (user DAO row, token store) stay in `ProfileRepositoryImpl`
 * so this class doesn't reach into the auth feature.
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
