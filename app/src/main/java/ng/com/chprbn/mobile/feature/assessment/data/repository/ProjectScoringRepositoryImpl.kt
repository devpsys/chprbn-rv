package ng.com.chprbn.mobile.feature.assessment.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.core.sync.SyncEntityType
import ng.com.chprbn.mobile.core.sync.SyncJobDao
import ng.com.chprbn.mobile.core.sync.SyncJobEntity
import ng.com.chprbn.mobile.core.sync.SyncWorkScheduler
import ng.com.chprbn.mobile.feature.assessment.data.local.ProjectScoreDao
import ng.com.chprbn.mobile.feature.assessment.data.mappers.toDomain
import ng.com.chprbn.mobile.feature.assessment.data.mappers.toEntity
import ng.com.chprbn.mobile.feature.assessment.data.sync.ProjectScoreKey
import ng.com.chprbn.mobile.feature.assessment.domain.model.ProjectScore
import ng.com.chprbn.mobile.feature.assessment.domain.model.SaveResult
import ng.com.chprbn.mobile.feature.assessment.domain.repository.ProjectScoringRepository
import javax.inject.Inject

class ProjectScoringRepositoryImpl @Inject constructor(
    private val projectScoreDao: ProjectScoreDao,
    private val syncJobDao: SyncJobDao,
    private val workScheduler: SyncWorkScheduler,
    private val statusUpdater: AssessmentScheduleSyncStatusUpdater,
) : ProjectScoringRepository {

    override suspend fun getProjectScore(
        scheduleId: String,
        candidateId: String,
    ): ProjectScore? = withContext(Dispatchers.IO) {
        projectScoreDao.getOne(scheduleId, candidateId)?.toDomain()
    }

    override suspend fun recordProjectScore(score: ProjectScore): SaveResult =
        withContext(Dispatchers.IO) {
            try {
                projectScoreDao.upsert(score.toEntity())
                syncJobDao.enqueue(
                    SyncJobEntity(
                        entityType = SyncEntityType.ProjectScore.name,
                        entityKey = ProjectScoreKey.encode(
                            scheduleId = score.scheduleId,
                            candidateId = score.candidateId,
                        ),
                        enqueuedAt = score.scoredAt,
                        status = SyncStatus.Pending.name,
                    ),
                )
                statusUpdater.refresh(score.scheduleId)
                workScheduler.scheduleSyncWork()
                SaveResult.Success
            } catch (t: Throwable) {
                SaveResult.Error(t.message ?: "Unable to save project score.")
            }
        }
}
