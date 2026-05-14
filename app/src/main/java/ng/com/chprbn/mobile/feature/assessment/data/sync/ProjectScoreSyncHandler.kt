package ng.com.chprbn.mobile.feature.assessment.data.sync

import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.core.sync.SyncEntityHandler
import ng.com.chprbn.mobile.core.sync.SyncOutcome
import ng.com.chprbn.mobile.feature.assessment.data.local.ProjectScoreDao
import ng.com.chprbn.mobile.feature.assessment.data.mappers.toDomain
import ng.com.chprbn.mobile.feature.assessment.data.repository.AssessmentScheduleSyncStatusUpdater
import ng.com.chprbn.mobile.feature.assessment.data.source.AssessmentSyncRemoteSource
import javax.inject.Inject

/** Project-score equivalent of [PracticalScoreSyncHandler]. */
class ProjectScoreSyncHandler @Inject constructor(
    private val projectScoreDao: ProjectScoreDao,
    private val remoteSource: AssessmentSyncRemoteSource,
    private val statusUpdater: AssessmentScheduleSyncStatusUpdater,
) : SyncEntityHandler {

    override suspend fun upload(entityKey: String): SyncOutcome {
        val parsed = ProjectScoreKey.decode(entityKey)
            ?: return SyncOutcome.Failure("Malformed project-score key: $entityKey")
        val (scheduleId, candidateId) = parsed

        val entity = projectScoreDao.getOne(scheduleId, candidateId)
            ?: return SyncOutcome.Failure("Project score not found locally: $entityKey")

        val result = remoteSource.uploadProjectScore(entity.toDomain())
        return result.fold(
            onSuccess = {
                projectScoreDao.updateSyncMetadata(
                    scheduleId = scheduleId,
                    candidateId = candidateId,
                    syncStatus = SyncStatus.Synced.name,
                    syncError = null,
                )
                statusUpdater.refresh(scheduleId)
                SyncOutcome.Success
            },
            onFailure = { t ->
                val message = t.message ?: "Upload failed."
                projectScoreDao.updateSyncMetadata(
                    scheduleId = scheduleId,
                    candidateId = candidateId,
                    syncStatus = SyncStatus.Failed.name,
                    syncError = message,
                )
                statusUpdater.refresh(scheduleId)
                SyncOutcome.Failure(message)
            },
        )
    }
}
