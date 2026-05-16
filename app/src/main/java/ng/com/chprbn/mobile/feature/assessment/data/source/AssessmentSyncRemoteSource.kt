package ng.com.chprbn.mobile.feature.assessment.data.source

import ng.com.chprbn.mobile.feature.assessment.domain.model.PracticalScore
import ng.com.chprbn.mobile.feature.assessment.domain.model.ProjectScore

/**
 * Write-side abstraction for the assessment sync queue. One HTTP call
 * per batch — matches the server contract in
 * `docs/api/full-api-documentation.md` §9.3 / §9.4.
 *
 * **Returned map shape.** Keys are the rows' `clientId` strings; every
 * input row produces exactly one entry in the map. On a transport-level
 * failure every row is failed with the same exception.
 *
 * No `Fake*` / `Composite*` companions — uploading to nowhere is never
 * the right dev behaviour. Tests inject a mock or hand-rolled stub.
 */
interface AssessmentSyncRemoteSource {

    suspend fun uploadPracticalScoreBatch(rows: List<PracticalScore>): Map<String, Result<Unit>>

    suspend fun uploadProjectScoreBatch(rows: List<ProjectScore>): Map<String, Result<Unit>>
}
