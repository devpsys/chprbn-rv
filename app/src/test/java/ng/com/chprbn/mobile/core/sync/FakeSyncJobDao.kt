package ng.com.chprbn.mobile.core.sync

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * In-memory test double for [SyncJobDao]. Mirrors only the behaviour the
 * unit tests need; transactional / index semantics are out of scope and live
 * in the Room-backed instrumentation test.
 */
internal class FakeSyncJobDao : SyncJobDao {

    private val rows = mutableMapOf<Long, SyncJobEntity>()
    private val flow = MutableStateFlow<List<SyncJobEntity>>(emptyList())
    private var nextAutoId = 1L

    fun seed(vararg entities: SyncJobEntity) {
        entities.forEach { rows[it.id] = it; if (it.id >= nextAutoId) nextAutoId = it.id + 1 }
        publish()
    }

    fun snapshot(): List<SyncJobEntity> = rows.values.sortedBy { it.id }

    override suspend fun enqueue(job: SyncJobEntity): Long {
        val id = if (job.id == 0L) nextAutoId++ else job.id
        rows[id] = job.copy(id = id)
        publish()
        return id
    }

    override suspend fun pendingAndFailed(limit: Int): List<SyncJobEntity> =
        rows.values
            .filter { it.status == "Pending" || it.status == "Failed" }
            .sortedBy { it.enqueuedAt }
            .take(limit)

    override suspend fun countByStatus(status: String): Int =
        rows.values.count { it.status == status }

    override suspend fun countByTypeAndStatus(entityType: String, status: String): Int =
        rows.values.count { it.entityType == entityType && it.status == status }

    override fun observeAll(): Flow<List<SyncJobEntity>> = flow

    override suspend fun markAttempted(
        id: Long,
        status: String,
        attemptedAt: Long,
        error: String?,
    ): Int {
        val current = rows[id] ?: return 0
        rows[id] = current.copy(
            status = status,
            attemptCount = current.attemptCount + 1,
            lastAttemptAt = attemptedAt,
            lastError = error,
        )
        publish()
        return 1
    }

    override suspend fun delete(id: Long): Int {
        val removed = rows.remove(id) != null
        publish()
        return if (removed) 1 else 0
    }

    override suspend fun pruneSynced(): Int {
        val before = rows.size
        rows.values.removeAll { it.status == "Synced" }
        publish()
        return before - rows.size
    }

    override suspend fun clearAll(): Int {
        val before = rows.size
        rows.clear()
        publish()
        return before
    }

    private fun publish() {
        flow.value = rows.values.sortedByDescending { it.enqueuedAt }
    }
}
