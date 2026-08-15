package ng.com.chprbn.mobile.feature.assessment.data.sync

import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.core.sync.SyncEntityHandler
import ng.com.chprbn.mobile.core.sync.SyncOutcome
import ng.com.chprbn.mobile.feature.assessment.data.local.PracticalScoreDao
import ng.com.chprbn.mobile.feature.assessment.data.mappers.practicalScoreClientId
import ng.com.chprbn.mobile.feature.assessment.data.mappers.toDomain
import ng.com.chprbn.mobile.feature.assessment.data.source.AssessmentSyncRemoteSource
import javax.inject.Inject

/**
 * Plugs the practical-score row uploader into the cross-feature
 * `core.sync.SyncWorker` via Hilt multibinding. The runner hands in the
 * batch's slash-delimited entity keys; the handler resolves the rows,
 * sends a single batched HTTP request, and flips per-row score syncStatus.
 *
 * The schedules-list pill's aggregate status is derived at read time in
 * `AssessmentScheduleRepositoryImpl.getSchedules`, so no explicit
 * per-schedule status refresh is needed here.
 */
class PracticalScoreSyncHandler @Inject constructor(
    private val practicalScoreDao: PracticalScoreDao,
    private val remoteSource: AssessmentSyncRemoteSource,
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
                // Ghost sync job — see PracticalScoringRepositoryImpl.recordScore.
                outcomes[key] = SyncOutcome.Drop
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
                    SyncOutcome.Failure(message)
                },
            )
        }

        return outcomes
    }

    private data class UploadRow(
        val entityKey: String,
        val domain: ng.com.chprbn.mobile.feature.assessment.domain.model.PracticalScore,
        val clientId: String,
    )
}
