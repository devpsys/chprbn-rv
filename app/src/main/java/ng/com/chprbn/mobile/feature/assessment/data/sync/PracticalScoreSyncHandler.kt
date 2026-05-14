package ng.com.chprbn.mobile.feature.assessment.data.sync

import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.core.sync.SyncEntityHandler
import ng.com.chprbn.mobile.core.sync.SyncOutcome
import ng.com.chprbn.mobile.feature.assessment.data.local.PracticalScoreDao
import ng.com.chprbn.mobile.feature.assessment.data.mappers.toDomain
import ng.com.chprbn.mobile.feature.assessment.data.repository.AssessmentScheduleSyncStatusUpdater
import ng.com.chprbn.mobile.feature.assessment.data.source.AssessmentSyncRemoteSource
import javax.inject.Inject

/**
 * Plugs the practical-score row uploader into the cross-feature
 * `core.sync.SyncWorker` via Hilt multibinding. The runner hands in the
 * row's slash-delimited entity key; the handler resolves the row, uploads
 * it, and flips `score.syncStatus` plus the parent schedule's status to
 * match the result.
 *
 * `SyncOutcome.Success` deletes the queue row; `SyncOutcome.Failure`
 * leaves it for the worker's backoff retry.
 */
class PracticalScoreSyncHandler @Inject constructor(
    private val practicalScoreDao: PracticalScoreDao,
    private val remoteSource: AssessmentSyncRemoteSource,
    private val statusUpdater: AssessmentScheduleSyncStatusUpdater,
) : SyncEntityHandler {

    override suspend fun upload(entityKey: String): SyncOutcome {
        val parsed = PracticalScoreKey.decode(entityKey)
            ?: return SyncOutcome.Failure("Malformed practical-score key: $entityKey")
        val (scheduleId, candidateId, questionId) = parsed

        val entity = practicalScoreDao.getOne(scheduleId, candidateId, questionId)
            ?: return SyncOutcome.Failure("Practical score not found locally: $entityKey")

        val result = remoteSource.uploadPracticalScore(entity.toDomain())
        return result.fold(
            onSuccess = {
                practicalScoreDao.updateSyncMetadata(
                    scheduleId = scheduleId,
                    candidateId = candidateId,
                    questionId = questionId,
                    syncStatus = SyncStatus.Synced.name,
                    syncError = null,
                )
                statusUpdater.refresh(scheduleId)
                SyncOutcome.Success
            },
            onFailure = { t ->
                val message = t.message ?: "Upload failed."
                practicalScoreDao.updateSyncMetadata(
                    scheduleId = scheduleId,
                    candidateId = candidateId,
                    questionId = questionId,
                    syncStatus = SyncStatus.Failed.name,
                    syncError = message,
                )
                statusUpdater.refresh(scheduleId)
                SyncOutcome.Failure(message)
            },
        )
    }
}
