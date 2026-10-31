package ng.com.chprbn.mobile.feature.accreditation.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.core.sync.Clock
import ng.com.chprbn.mobile.core.sync.SyncEntityType
import ng.com.chprbn.mobile.core.sync.SyncJobDao
import ng.com.chprbn.mobile.core.sync.SyncJobEntity
import ng.com.chprbn.mobile.core.sync.SyncWorkScheduler
import ng.com.chprbn.mobile.feature.accreditation.data.local.AssessmentDraftDao
import ng.com.chprbn.mobile.feature.accreditation.data.local.CriteriaDraftDao
import ng.com.chprbn.mobile.feature.accreditation.data.local.ScoreDraftDao
import ng.com.chprbn.mobile.feature.accreditation.data.local.SsqSectionDraftDao
import ng.com.chprbn.mobile.feature.accreditation.data.local.AccreditationAttachmentDao
import ng.com.chprbn.mobile.feature.accreditation.data.mappers.toDomain
import ng.com.chprbn.mobile.feature.accreditation.data.mappers.toEntity
import ng.com.chprbn.mobile.feature.accreditation.data.mappers.toSummary
import ng.com.chprbn.mobile.feature.accreditation.data.source.AccreditationDraftRemoteSource
import ng.com.chprbn.mobile.feature.accreditation.domain.model.AssessmentDraftRecord
import ng.com.chprbn.mobile.feature.accreditation.domain.model.AssessmentOverview
import ng.com.chprbn.mobile.feature.accreditation.domain.model.CriteriaDraftRecord
import ng.com.chprbn.mobile.feature.accreditation.domain.model.DraftOpResult
import ng.com.chprbn.mobile.feature.accreditation.domain.model.DraftPersistenceStatus
import ng.com.chprbn.mobile.feature.accreditation.domain.model.GetOverviewResult
import ng.com.chprbn.mobile.feature.accreditation.domain.model.ScoreDraftRecord
import ng.com.chprbn.mobile.feature.accreditation.domain.model.SsqSectionDraftRecord
import ng.com.chprbn.mobile.feature.accreditation.domain.model.SsqSectionKey
import ng.com.chprbn.mobile.feature.accreditation.domain.repository.AssessmentDraftRepository
import javax.inject.Inject

class AssessmentDraftRepositoryImpl @Inject constructor(
    private val draftDao: AssessmentDraftDao,
    private val sectionDao: SsqSectionDraftDao,
    private val criteriaDao: CriteriaDraftDao,
    private val scoreDao: ScoreDraftDao,
    private val attachmentDao: AccreditationAttachmentDao,
    private val remote: AccreditationDraftRemoteSource,
    private val syncJobDao: SyncJobDao,
    private val workScheduler: SyncWorkScheduler,
    private val clock: Clock,
) : AssessmentDraftRepository {

    private val seedMutex = Mutex()

    override suspend fun getOrCreateCurrent(institutionId: String): AssessmentDraftRecord =
        withContext(Dispatchers.IO) {
            seedMutex.withLock {
                val existing = draftDao.getByInstitutionId(institutionId)
                if (existing != null) return@withLock existing.toDomain()
                seedFromRemote(institutionId)
            }
        }

    override suspend fun getOverview(institutionId: String): GetOverviewResult =
        withContext(Dispatchers.IO) {
            try {
                val draft = getOrCreateCurrent(institutionId)
                val sections = sectionDao.getForAssessment(draft.id).map { it.toSummary() }
                GetOverviewResult.Success(
                    AssessmentOverview(
                        assessmentId = draft.id,
                        institutionId = draft.institutionId,
                        institutionName = draft.institutionName,
                        locationLabel = draft.locationLabel,
                        facilityCode = draft.facilityCode,
                        cycleLabel = draft.cycleLabel,
                        sections = sections,
                    ),
                )
            } catch (t: Throwable) {
                GetOverviewResult.Error(t.message ?: "Unable to load assessment overview.")
            }
        }

    override suspend fun getSection(
        assessmentId: String,
        sectionKey: SsqSectionKey,
    ): SsqSectionDraftRecord? = withContext(Dispatchers.IO) {
        sectionDao.get(assessmentId, sectionKey.key)?.toDomain()
    }

    override suspend fun saveSection(
        assessmentId: String,
        sectionKey: SsqSectionKey,
        payloadJson: String,
        status: DraftPersistenceStatus,
        itemsCompleted: Int,
        totalItems: Int,
    ): DraftOpResult = withContext(Dispatchers.IO) {
        try {
            val existing = sectionDao.get(assessmentId, sectionKey.key)
                ?: return@withContext DraftOpResult.Error("Section not found")
            sectionDao.upsert(
                existing.copy(
                    status = status.name,
                    payloadJson = payloadJson,
                    itemsCompleted = itemsCompleted,
                    totalItems = totalItems,
                ),
            )
            touchDraftPending(assessmentId)
            enqueueSync(assessmentId)
            DraftOpResult.Success
        } catch (t: Throwable) {
            DraftOpResult.Error(t.message ?: "Unable to save section draft.")
        }
    }

    override suspend fun getCriteria(assessmentId: String): CriteriaDraftRecord? =
        withContext(Dispatchers.IO) {
            criteriaDao.get(assessmentId)?.toDomain()
        }

    override suspend fun saveCriteria(
        assessmentId: String,
        payloadJson: String,
        evaluatedCount: Int,
        totalCriteria: Int,
        status: DraftPersistenceStatus,
    ): DraftOpResult = withContext(Dispatchers.IO) {
        try {
            criteriaDao.upsert(
                CriteriaDraftRecord(
                    assessmentId = assessmentId,
                    payloadJson = payloadJson,
                    evaluatedCount = evaluatedCount,
                    totalCriteria = totalCriteria,
                    status = status,
                ).toEntity(),
            )
            touchDraftPending(assessmentId)
            enqueueSync(assessmentId)
            DraftOpResult.Success
        } catch (t: Throwable) {
            DraftOpResult.Error(t.message ?: "Unable to save criteria draft.")
        }
    }

    override suspend fun getScores(assessmentId: String): ScoreDraftRecord? =
        withContext(Dispatchers.IO) {
            scoreDao.get(assessmentId)?.toDomain()
        }

    override suspend fun saveScores(
        assessmentId: String,
        payloadJson: String,
        completedCount: Int,
        totalGroups: Int,
        status: DraftPersistenceStatus,
    ): DraftOpResult = withContext(Dispatchers.IO) {
        try {
            scoreDao.upsert(
                ScoreDraftRecord(
                    assessmentId = assessmentId,
                    payloadJson = payloadJson,
                    completedCount = completedCount,
                    totalGroups = totalGroups,
                    status = status,
                ).toEntity(),
            )
            touchDraftPending(assessmentId)
            enqueueSync(assessmentId)
            DraftOpResult.Success
        } catch (t: Throwable) {
            DraftOpResult.Error(t.message ?: "Unable to save score draft.")
        }
    }

    override suspend fun saveAllSections(assessmentId: String): DraftOpResult =
        withContext(Dispatchers.IO) {
            try {
                sectionDao.markAllSaved(assessmentId, DraftPersistenceStatus.SavedLocally.name)
                touchDraftPending(assessmentId)
                enqueueSync(assessmentId)
                DraftOpResult.Success
            } catch (t: Throwable) {
                DraftOpResult.Error(t.message ?: "Unable to save all sections.")
            }
        }

    override suspend fun discard(assessmentIds: List<String>): DraftOpResult =
        withContext(Dispatchers.IO) {
            try {
                if (assessmentIds.isEmpty()) return@withContext DraftOpResult.Success
                sectionDao.deleteForAssessments(assessmentIds)
                criteriaDao.deleteForAssessments(assessmentIds)
                scoreDao.deleteForAssessments(assessmentIds)
                attachmentDao.deleteForAssessments(assessmentIds)
                draftDao.deleteByIds(assessmentIds)
                DraftOpResult.Success
            } catch (t: Throwable) {
                DraftOpResult.Error(t.message ?: "Unable to discard drafts.")
            }
        }

    override suspend fun markSynced(assessmentId: String): DraftOpResult =
        withContext(Dispatchers.IO) {
            try {
                draftDao.updateStatus(
                    id = assessmentId,
                    status = DraftPersistenceStatus.Synced.name,
                    updatedAt = clock.nowMillis(),
                )
                DraftOpResult.Success
            } catch (t: Throwable) {
                DraftOpResult.Error(t.message ?: "Unable to mark draft synced.")
            }
        }

    override suspend fun getDraft(assessmentId: String): AssessmentDraftRecord? =
        withContext(Dispatchers.IO) {
            draftDao.getById(assessmentId)?.toDomain()
        }

    private suspend fun seedFromRemote(institutionId: String): AssessmentDraftRecord {
        val bundle = remote.getSeedBundle(institutionId)
            ?: error("No seed data for institution $institutionId")
        draftDao.upsert(bundle.draft.toEntity())
        sectionDao.upsertAll(bundle.sections.map { it.toEntity() })
        criteriaDao.upsert(bundle.criteria.toEntity())
        scoreDao.upsert(bundle.scores.toEntity())
        return bundle.draft
    }

    private suspend fun touchDraftPending(assessmentId: String) {
        draftDao.updateStatus(
            id = assessmentId,
            status = DraftPersistenceStatus.PendingSync.name,
            updatedAt = clock.nowMillis(),
        )
    }

    private suspend fun enqueueSync(assessmentId: String) {
        syncJobDao.enqueue(
            SyncJobEntity(
                entityType = SyncEntityType.AccreditationDraft.name,
                entityKey = assessmentId,
                enqueuedAt = clock.nowMillis(),
                status = SyncStatus.Pending.name,
            ),
        )
        workScheduler.scheduleSyncWork()
    }
}
