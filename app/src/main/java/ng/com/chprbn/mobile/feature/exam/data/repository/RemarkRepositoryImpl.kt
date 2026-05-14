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
import ng.com.chprbn.mobile.feature.exam.domain.repository.RemarkRepository
import java.util.UUID
import javax.inject.Inject

/**
 * Append-only remark surface. The repository generates a UUID as the
 * client-side primary key; once the server accepts the row, the sync
 * handler optionally REPLACES with the server-assigned id (a feature
 * the wire DTO supports but the v1 handler doesn't yet exercise — the
 * UUID stays stable end-to-end).
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
    ): AddRemarkResult = withContext(Dispatchers.IO) {
        val createdAt = clock.nowMillis()
        val remark = Remark(
            id = UUID.randomUUID().toString(),
            candidateId = candidateId,
            paperId = paperId,
            body = body,
            severity = severity,
            createdAt = createdAt,
            syncStatus = SyncStatus.Pending,
        )
        try {
            remarkDao.upsert(remark.toEntity())
            syncJobDao.enqueue(
                SyncJobEntity(
                    entityType = SyncEntityType.Remark.name,
                    entityKey = RemarkKey.encode(remark.id),
                    enqueuedAt = createdAt,
                    status = SyncStatus.Pending.name,
                ),
            )
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
}
