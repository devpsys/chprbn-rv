package ng.com.chprbn.mobile.feature.assessment.data.source

import ng.com.chprbn.mobile.feature.assessment.domain.model.PracticalScore
import ng.com.chprbn.mobile.feature.assessment.domain.model.ProjectScore

/**
 * Write-side abstraction for the assessment sync queue. Uploads happen
 * one row per HTTP request (the only template the documented backend API
 * offers — see plan §12). Returns [Result.success] on a 2xx acceptance,
 * [Result.failure] on anything else with the error message in the
 * exception. Per-feature sync handlers convert the [Result] to a
 * [ng.com.chprbn.mobile.core.sync.SyncOutcome].
 *
 * No `Fake*` / `Composite*` companions — uploading to nowhere is never
 * the right dev behaviour. Tests inject a mock or a hand-rolled stub.
 */
interface AssessmentSyncRemoteSource {

    suspend fun uploadPracticalScore(score: PracticalScore): Result<Unit>

    suspend fun uploadProjectScore(score: ProjectScore): Result<Unit>
}
