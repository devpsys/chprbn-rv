package ng.com.chprbn.mobile.feature.assessment.data.sync

import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.core.network.toUserFacingMessage
import ng.com.chprbn.mobile.core.sync.SyncEntityHandler
import ng.com.chprbn.mobile.core.sync.SyncOutcome
import ng.com.chprbn.mobile.feature.assessment.data.local.PracticalScoreDao
import ng.com.chprbn.mobile.feature.assessment.data.mappers.practicalScoreClientId
import ng.com.chprbn.mobile.feature.assessment.data.mappers.toDomain
import ng.com.chprbn.mobile.feature.assessment.data.source.AssessmentSyncRemoteSource
import ng.com.chprbn.mobile.feature.assessment.data.source.PracticalScoreUploadRow
import ng.com.chprbn.mobile.feature.exam.data.local.CandidateDao
import javax.inject.Inject

/**
 * Plugs the practical-score row uploader into the cross-feature
 * `core.sync.SyncWorker` via Hilt multibinding.
 *
 * `practical/push-record` needs `scheduledCandidateId` / venue `scheduleId`
 * from the cached dossier assignment (paper id == local score `scheduleId`).
 * A row whose assignment can't be resolved fails with a clear message
 * rather than being dropped.
 */
class PracticalScoreSyncHandler @Inject constructor(
    private val practicalScoreDao: PracticalScoreDao,
    private val candidateDao: CandidateDao,
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
                outcomes[key] = SyncOutcome.Drop
                continue
            }
            val assignment = candidateDao.getAssignment(scheduleId, candidateId)
                ?.takeIf { it.scheduledCandidateId.isNotBlank() && it.scheduleId.isNotBlank() }
            if (assignment == null) {
                outcomes[key] = SyncOutcome.Failure(
                    "Missing scheduledCandidateId/scheduleId for this candidate — " +
                        "re-download today's dossier and retry.",
                )
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
                    uploadRow = PracticalScoreUploadRow(
                        clientId = practicalScoreClientId(
                            domain.scheduleId, domain.candidateId, domain.questionId,
                        ),
                        paperId = domain.scheduleId,
                        candidateId = domain.candidateId,
                        questionId = domain.questionId,
                        scheduledCandidateId = assignment.scheduledCandidateId,
                        scheduleId = assignment.scheduleId,
                        score = domain.score,
                    ),
                ),
            )
        }

        if (toUpload.isEmpty()) return outcomes

        val remoteResults = remoteSource.uploadPracticalScoreBatch(toUpload.map { it.uploadRow })

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
                    // See AttendanceSyncHandler for the sanitiser rationale.
                    val message = t.toUserFacingMessage(default = "Practical-score upload failed.")
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
        val uploadRow: PracticalScoreUploadRow,
    )
}
