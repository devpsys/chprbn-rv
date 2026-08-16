package ng.com.chprbn.mobile.feature.assessment.data.source

/**
 * A local practical-score row plus the dossier fields
 * `POST /practical/push-record` needs that aren't stored on
 * [ng.com.chprbn.mobile.feature.assessment.domain.model.PracticalScore]
 * itself — [scheduledCandidateId] / venue [scheduleId] resolved from
 * `paper_candidate_assignments` at sync time (paper id == local
 * `PracticalScore.scheduleId`).
 *
 * [clientId] is the local correlation key (`scheduleId:candidateId:questionId`);
 * it is NOT sent on the wire.
 */
data class PracticalScoreUploadRow(
    val clientId: String,
    val paperId: String,
    val candidateId: String,
    val questionId: String,
    val scheduledCandidateId: String,
    val scheduleId: String,
    val score: Int,
)

/** Project equivalent of [PracticalScoreUploadRow] for `POST /project/push-record`. */
data class ProjectScoreUploadRow(
    val clientId: String,
    val paperId: String,
    val candidateId: String,
    val scheduledCandidateId: String,
    val scheduleId: String,
    val score: Double,
)

/**
 * Write-side abstraction for the assessment sync queue. One HTTP call
 * per batch. Returned map is keyed by [PracticalScoreUploadRow.clientId] /
 * [ProjectScoreUploadRow.clientId]; every input row produces exactly one
 * entry. The live endpoints return no per-row results (just the batch
 * `status` + a list of `scheduled_candidate_id`s), so every well-formed
 * row in a batch shares one outcome.
 */
interface AssessmentSyncRemoteSource {

    suspend fun uploadPracticalScoreBatch(rows: List<PracticalScoreUploadRow>): Map<String, Result<Unit>>

    suspend fun uploadProjectScoreBatch(rows: List<ProjectScoreUploadRow>): Map<String, Result<Unit>>
}
