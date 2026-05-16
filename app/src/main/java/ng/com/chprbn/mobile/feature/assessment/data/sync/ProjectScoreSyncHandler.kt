package ng.com.chprbn.mobile.feature.assessment.data.sync

import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.core.sync.SyncEntityHandler
import ng.com.chprbn.mobile.core.sync.SyncOutcome
import ng.com.chprbn.mobile.feature.assessment.data.local.ProjectScoreDao
import ng.com.chprbn.mobile.feature.assessment.data.mappers.projectScoreClientId
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

    override suspend fun uploadBatch(entityKeys: List<String>): Map<String, SyncOutcome> {
        val outcomes = LinkedHashMap<String, SyncOutcome>(entityKeys.size)
        val toUpload = mutableListOf<UploadRow>()

        for (key in entityKeys) {
            val parsed = ProjectScoreKey.decode(key)
            if (parsed == null) {
                outcomes[key] = SyncOutcome.Failure("Malformed project-score key: $key")
                continue
            }
            val (scheduleId, candidateId) = parsed
            val entity = projectScoreDao.getOne(scheduleId, candidateId)
            if (entity == null) {
                outcomes[key] = SyncOutcome.Failure("Project score not found locally: $key")
                continue
            }
            val domain = entity.toDomain()
            toUpload.add(
                UploadRow(
                    entityKey = key,
                    domain = domain,
                    clientId = projectScoreClientId(domain.scheduleId, domain.candidateId),
                ),
            )
        }

        if (toUpload.isEmpty()) return outcomes

        val remoteResults = remoteSource.uploadProjectScoreBatch(toUpload.map { it.domain })
        val touchedSchedules = mutableSetOf<String>()

        for (row in toUpload) {
            val result = remoteResults[row.clientId]
                ?: Result.failure(IllegalStateException("No remote result for ${row.clientId}"))
            outcomes[row.entityKey] = result.fold(
                onSuccess = {
                    projectScoreDao.updateSyncMetadata(
                        scheduleId = row.domain.scheduleId,
                        candidateId = row.domain.candidateId,
                        syncStatus = SyncStatus.Synced.name,
                        syncError = null,
                    )
                    touchedSchedules += row.domain.scheduleId
                    SyncOutcome.Success
                },
                onFailure = { t ->
                    val message = t.message ?: "Upload failed."
                    projectScoreDao.updateSyncMetadata(
                        scheduleId = row.domain.scheduleId,
                        candidateId = row.domain.candidateId,
                        syncStatus = SyncStatus.Failed.name,
                        syncError = message,
                    )
                    touchedSchedules += row.domain.scheduleId
                    SyncOutcome.Failure(message)
                },
            )
        }

        for (scheduleId in touchedSchedules) {
            statusUpdater.refresh(scheduleId)
        }

        return outcomes
    }

    private data class UploadRow(
        val entityKey: String,
        val domain: ng.com.chprbn.mobile.feature.assessment.domain.model.ProjectScore,
        val clientId: String,
    )
}
