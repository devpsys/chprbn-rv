package ng.com.chprbn.mobile.feature.exam.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.core.sync.Clock
import ng.com.chprbn.mobile.core.sync.SyncEntityType
import ng.com.chprbn.mobile.core.sync.SyncJobDao
import ng.com.chprbn.mobile.core.sync.SyncJobEntity
import ng.com.chprbn.mobile.core.sync.SyncWorkScheduler
import ng.com.chprbn.mobile.feature.exam.data.local.RemarkDao
import ng.com.chprbn.mobile.feature.exam.data.mappers.toDomain
import ng.com.chprbn.mobile.feature.exam.data.mappers.toEntity
import ng.com.chprbn.mobile.feature.exam.data.sync.RemarkKey
import ng.com.chprbn.mobile.feature.exam.domain.model.AddRemarkResult
import ng.com.chprbn.mobile.feature.exam.domain.model.Remark
import ng.com.chprbn.mobile.feature.exam.domain.model.RemarkSeverity
import ng.com.chprbn.mobile.feature.exam.domain.model.SaveResult
import ng.com.chprbn.mobile.feature.exam.domain.repository.RemarkRepository
import java.util.UUID
import javax.inject.Inject

/**
 * One remark per candidate. [addRemark] always builds a fresh
 * client-generated UUID as `id` and upserts by `candidateId` (the
 * entity's primary key), so it REPLACES whatever remark was already on
 * file — any sync job still pointing at the previous `id` becomes a
 * ghost job that [ng.com.chprbn.mobile.feature.exam.data.sync.RemarkSyncHandler]
 * self-cleans the next time it runs.
 */
class RemarkRepositoryImpl @Inject constructor(
    private val remarkDao: RemarkDao,
    private val syncJobDao: SyncJobDao,
    private val workScheduler: SyncWorkScheduler,
    private val clock: Clock,
) : RemarkRepository {

    override suspend fun addRemark(
        candidateId: String,
        paperId: String?,
        body: String,
        severity: RemarkSeverity,
        code: String,
    ): AddRemarkResult = withContext(Dispatchers.IO) {
        val createdAt = clock.nowMillis()
        val remark = Remark(
            id = UUID.randomUUID().toString(),
            candidateId = candidateId,
            paperId = paperId,
            code = code,
            body = body,
            severity = severity,
            createdAt = createdAt,
            syncStatus = SyncStatus.Pending,
        )
        // See AttendanceRepositoryImpl.markAttendance for the enqueue-before-upsert
        // rationale (cross-database — no shared transaction; ghost sync jobs are
        // self-cleaned by RemarkSyncHandler when the local row is missing).
        try {
            syncJobDao.enqueue(
                SyncJobEntity(
                    entityType = SyncEntityType.Remark.name,
                    entityKey = RemarkKey.encode(remark.id),
                    enqueuedAt = createdAt,
                    status = SyncStatus.Pending.name,
                ),
            )
            remarkDao.upsert(remark.toEntity())
            workScheduler.scheduleSyncWork()
            AddRemarkResult.Success(remark)
        } catch (t: Throwable) {
            AddRemarkResult.Error(t.message ?: "Unable to save remark.")
        }
    }

    override suspend fun getRemarksForCandidate(candidateId: String): List<Remark> =
        withContext(Dispatchers.IO) {
            remarkDao.getForCandidate(candidateId).map { it.toDomain() }
        }

    override suspend fun clearRemarksForCandidate(candidateId: String): SaveResult =
        withContext(Dispatchers.IO) {
            try {
                remarkDao.deleteForCandidate(candidateId)
                SaveResult.Success
            } catch (t: Throwable) {
                SaveResult.Error(t.message ?: "Unable to clear remarks.")
            }
        }
}
