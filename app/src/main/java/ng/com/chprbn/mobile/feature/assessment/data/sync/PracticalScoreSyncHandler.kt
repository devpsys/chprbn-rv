package ng.com.chprbn.mobile.feature.assessment.data.sync

import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.core.sync.SyncEntityHandler
import ng.com.chprbn.mobile.core.sync.SyncOutcome
import ng.com.chprbn.mobile.feature.assessment.data.local.PracticalScoreDao
import ng.com.chprbn.mobile.feature.assessment.data.mappers.practicalScoreClientId
import ng.com.chprbn.mobile.feature.assessment.data.mappers.toDomain
import ng.com.chprbn.mobile.feature.assessment.data.repository.AssessmentScheduleSyncStatusUpdater
import ng.com.chprbn.mobile.feature.assessment.data.source.AssessmentSyncRemoteSource
import javax.inject.Inject

/**
 * Plugs the practical-score row uploader into the cross-feature
 * `core.sync.SyncWorker` via Hilt multibinding. The runner hands in the
 * batch's slash-delimited entity keys; the handler resolves the rows,
 * sends a single batched HTTP request, flips per-row score syncStatus,
 * then refreshes the parent schedule's status once per unique
 * `scheduleId`.
 */
class PracticalScoreSyncHandler @Inject constructor(
    private val practicalScoreDao: PracticalScoreDao,
    private val remoteSource: AssessmentSyncRemoteSource,
    private val statusUpdater: AssessmentScheduleSyncStatusUpdater,
) : SyncEntityHandler {

    override suspend fun uploadBatch(entityKeys: List<String>): Map<String, SyncOutcome> {
        val outcomes = LinkedHashMap<String, SyncOutcome>(entityKeys.size)
        val toUpload = mutableListOf<UploadRow>()

        for (key in entityKeys) {
            val parsed = PracticalScoreKey.decode(key)
            if (parsed == null) {
                outcomes[key] = SyncOutcome.Failure("Malformed practical-score key: $key")
                continue
            }
            val (scheduleId, candidateId, questionId) = parsed
            val entity = practicalScoreDao.getOne(scheduleId, candidateId, questionId)
            if (entity == null) {
                outcomes[key] = SyncOutcome.Failure("Practical score not found locally: $key")
                continue
            }
            val domain = entity.toDomain()
            toUpload.add(
                UploadRow(
                    entityKey = key,
                    domain = domain,
                    clientId = practicalScoreClientId(
                        domain.scheduleId, domain.candidateId, domain.questionId,
                    ),
                ),
            )
        }

        if (toUpload.isEmpty()) return outcomes

        val remoteResults = remoteSource.uploadPracticalScoreBatch(toUpload.map { it.domain })
        val touchedSchedules = mutableSetOf<String>()

        for (row in toUpload) {
            val result = remoteResults[row.clientId]
                ?: Result.failure(IllegalStateException("No remote result for ${row.clientId}"))
            outcomes[row.entityKey] = result.fold(
                onSuccess = {
                    practicalScoreDao.updateSyncMetadata(
                        scheduleId = row.domain.scheduleId,
                        candidateId = row.domain.candidateId,
                        questionId = row.domain.questionId,
                        syncStatus = SyncStatus.Synced.name,
                        syncError = null,
                    )
                    touchedSchedules += row.domain.scheduleId
                    SyncOutcome.Success
                },
                onFailure = { t ->
                    val message = t.message ?: "Upload failed."
                    practicalScoreDao.updateSyncMetadata(
                        scheduleId = row.domain.scheduleId,
                        candidateId = row.domain.candidateId,
                        questionId = row.domain.questionId,
                        syncStatus = SyncStatus.Failed.name,
                        syncError = message,
                    )
                    touchedSchedules += row.domain.scheduleId
                    SyncOutcome.Failure(message)
                },
            )
        }

        // Refresh once per unique scheduleId — fewer DB writes than per-row.
        for (scheduleId in touchedSchedules) {
            statusUpdater.refresh(scheduleId)
        }

        return outcomes
    }

    private data class UploadRow(
        val entityKey: String,
        val domain: ng.com.chprbn.mobile.feature.assessment.domain.model.PracticalScore,
        val clientId: String,
    )
}
