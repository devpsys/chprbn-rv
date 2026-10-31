package ng.com.chprbn.mobile.feature.accreditation.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.core.sync.Clock
import ng.com.chprbn.mobile.core.sync.SyncEntityType
import ng.com.chprbn.mobile.core.sync.SyncJobDao
import ng.com.chprbn.mobile.core.sync.SyncJobEntity
import ng.com.chprbn.mobile.core.sync.SyncWorkScheduler
import ng.com.chprbn.mobile.feature.accreditation.data.local.AccreditationAttachmentDao
import ng.com.chprbn.mobile.feature.accreditation.data.local.AssessmentDraftDao
import ng.com.chprbn.mobile.feature.accreditation.data.local.SsqSectionDraftDao
import ng.com.chprbn.mobile.feature.accreditation.data.mappers.toEntity
import ng.com.chprbn.mobile.feature.accreditation.data.mappers.toLocalDraftListItem
import ng.com.chprbn.mobile.feature.accreditation.data.source.AccreditationDraftRemoteSource
import ng.com.chprbn.mobile.feature.accreditation.data.source.AccreditationSyncRemoteSource
import ng.com.chprbn.mobile.feature.accreditation.domain.model.DraftPersistenceStatus
import ng.com.chprbn.mobile.feature.accreditation.domain.model.FailedDraftDetail
import ng.com.chprbn.mobile.feature.accreditation.domain.model.LocalDraftListItem
import ng.com.chprbn.mobile.feature.accreditation.domain.model.LocalDraftSyncState
import ng.com.chprbn.mobile.feature.accreditation.domain.model.OverallSyncStatus
import ng.com.chprbn.mobile.feature.accreditation.domain.model.SyncDraftsResult
import ng.com.chprbn.mobile.feature.accreditation.domain.model.SyncStatusSnapshot
import ng.com.chprbn.mobile.feature.accreditation.domain.repository.AccreditationSyncRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class AccreditationSyncRepositoryImpl @Inject constructor(
    private val draftDao: AssessmentDraftDao,
    private val sectionDao: SsqSectionDraftDao,
    private val attachmentDao: AccreditationAttachmentDao,
    private val draftRemote: AccreditationDraftRemoteSource,
    private val syncRemote: AccreditationSyncRemoteSource,
    private val syncJobDao: SyncJobDao,
    private val workScheduler: SyncWorkScheduler,
    private val clock: Clock,
) : AccreditationSyncRepository {

    private val seedMutex = Mutex()
    @Volatile private var seededSampleDrafts = false

    override fun observeLocalDrafts(): Flow<List<LocalDraftListItem>> = flow {
        ensureSampleDraftsSeeded()
        emit(buildLocalDraftItems())
        draftDao.observeAll().collect {
            emit(buildLocalDraftItems())
        }
    }

    override suspend fun listLocalDrafts(): List<LocalDraftListItem> =
        withContext(Dispatchers.IO) {
            ensureSampleDraftsSeeded()
            buildLocalDraftItems()
        }

    override suspend fun syncAll(): SyncDraftsResult = withContext(Dispatchers.IO) {
        try {
            ensureSampleDraftsSeeded()
            val drafts = draftDao.getAll().filter {
                it.status != DraftPersistenceStatus.Synced.name
            }
            var synced = 0
            drafts.forEach { draft ->
                syncJobDao.enqueue(
                    SyncJobEntity(
                        entityType = SyncEntityType.AccreditationDraft.name,
                        entityKey = draft.id,
                        enqueuedAt = clock.nowMillis(),
                        status = SyncStatus.Pending.name,
                    ),
                )
                val ok = syncRemote.upload(draft.id)
                if (ok) {
                    draftDao.updateStatus(
                        draft.id,
                        DraftPersistenceStatus.Synced.name,
                        clock.nowMillis(),
                    )
                    synced++
                } else {
                    draftDao.updateStatus(
                        draft.id,
                        DraftPersistenceStatus.SyncFailed.name,
                        clock.nowMillis(),
                    )
                }
            }
            workScheduler.scheduleSyncWork()
            SyncDraftsResult.Success(syncedCount = synced)
        } catch (t: Throwable) {
            SyncDraftsResult.Error(t.message ?: "Unable to sync accreditation drafts.")
        }
    }

    override suspend fun retryFailed(): SyncDraftsResult = withContext(Dispatchers.IO) {
        try {
            syncJobDao.resetAbandonedForRetry()
            val failed = draftDao.getAll().filter {
                it.status == DraftPersistenceStatus.SyncFailed.name
            }
            var synced = 0
            failed.forEach { draft ->
                syncJobDao.enqueue(
                    SyncJobEntity(
                        entityType = SyncEntityType.AccreditationDraft.name,
                        entityKey = draft.id,
                        enqueuedAt = clock.nowMillis(),
                        status = SyncStatus.Pending.name,
                    ),
                )
                val ok = syncRemote.upload(draft.id)
                if (ok) {
                    draftDao.updateStatus(
                        draft.id,
                        DraftPersistenceStatus.Synced.name,
                        clock.nowMillis(),
                    )
                    synced++
                }
            }
            workScheduler.scheduleSyncWork()
            SyncDraftsResult.Success(syncedCount = synced)
        } catch (t: Throwable) {
            SyncDraftsResult.Error(t.message ?: "Unable to retry failed syncs.")
        }
    }

    override suspend fun getSyncStatus(): SyncStatusSnapshot = withContext(Dispatchers.IO) {
        ensureSampleDraftsSeeded()
        val drafts = buildLocalDraftItems()
        val failed = drafts.filter { it.syncState == LocalDraftSyncState.SyncFailed }
        val pending = drafts.count {
            it.syncState == LocalDraftSyncState.PendingSync ||
                it.syncState == LocalDraftSyncState.Syncing
        }
        val synced = drafts.count { it.syncState == LocalDraftSyncState.Synced }
        val total = drafts.size.coerceAtLeast(1)
        val overall = when {
            failed.isNotEmpty() -> OverallSyncStatus.SyncFailed
            pending > 0 -> OverallSyncStatus.Syncing
            else -> OverallSyncStatus.Synced
        }
        SyncStatusSnapshot(
            overallStatus = overall,
            progressFraction = synced.toFloat() / total.toFloat(),
            lastSuccessfulSyncLabel = formatNow(),
            failedDrafts = failed.map { FailedDraftDetail(it) },
        )
    }

    private suspend fun buildLocalDraftItems(): List<LocalDraftListItem> =
        draftDao.getAll().map { entity ->
            val sections = sectionDao.getForAssessment(entity.id)
            val saved = sections.count {
                it.status != DraftPersistenceStatus.NotStarted.name &&
                    it.status != DraftPersistenceStatus.InProgress.name
            }
            entity.toLocalDraftListItem(
                sectionsCompleted = saved,
                attachmentsCount = attachmentDao.countForAssessment(entity.id),
            )
        }

    private suspend fun ensureSampleDraftsSeeded() {
        seedMutex.withLock {
            if (seededSampleDrafts) return
            if (draftDao.getAll().isNotEmpty()) {
                seededSampleDrafts = true
                return
            }
            val samples = draftRemote.sampleLocalDrafts()
            val now = clock.nowMillis()
            samples.forEach { sample ->
                val status = when (sample.syncState) {
                    LocalDraftSyncState.Synced -> DraftPersistenceStatus.Synced
                    LocalDraftSyncState.SyncFailed -> DraftPersistenceStatus.SyncFailed
                    LocalDraftSyncState.PendingSync,
                    LocalDraftSyncState.Syncing,
                    -> DraftPersistenceStatus.PendingSync
                }
                val bundle = draftRemote.getSeedBundle(sample.institutionId) ?: return@forEach
                draftDao.upsert(
                    bundle.draft.copy(
                        id = sample.id,
                        status = status,
                        updatedAt = now,
                        facilityCode = sample.assignmentCode,
                    ).toEntity(),
                )
                sectionDao.upsertAll(
                    bundle.sections.mapIndexed { index, section ->
                        val completed = index < sample.sectionsCompleted
                        section.copy(
                            assessmentId = sample.id,
                            status = if (completed) {
                                DraftPersistenceStatus.SavedLocally
                            } else {
                                DraftPersistenceStatus.NotStarted
                            },
                            itemsCompleted = if (completed) section.totalItems else 0,
                        ).toEntity()
                    },
                )
            }
            seededSampleDrafts = true
        }
    }

    private fun formatNow(): String =
        SimpleDateFormat("'Today,' hh:mm a", Locale.US).format(Date(clock.nowMillis()))
}
