package ng.com.chprbn.mobile.feature.assessment.domain.usecase

import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.core.sync.Clock
import ng.com.chprbn.mobile.feature.assessment.domain.model.PracticalScore
import ng.com.chprbn.mobile.feature.assessment.domain.model.SaveResult
import ng.com.chprbn.mobile.feature.assessment.domain.repository.PracticalScoringRepository
import javax.inject.Inject

/**
 * Upserts one per-question score for a candidate. Called from the scoring
 * screen on every stepper tap; the ViewModel debounces (~250ms) so a rapid
 * `+ + + +` collapses to one repository call.
 *
 * Range validation enforces `0 ≤ score ≤ maxScore`. The repository owns
 * persistence + enqueueing the matching `SyncJobEntity`; this use case is
 * the seam where the score is stamped with `scoredAt = clock.now()` and
 * left at `SyncStatus.Pending`.
 */
class RecordPracticalScoreUseCase @Inject constructor(
    private val repository: PracticalScoringRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(
        scheduleId: String,
        candidateId: String,
        questionId: String,
        score: Int,
        maxScore: Int,
    ): SaveResult {
        val schedule = scheduleId.trim()
        val candidate = candidateId.trim()
        val question = questionId.trim()
        if (schedule.isEmpty() || candidate.isEmpty() || question.isEmpty()) {
            return SaveResult.Error("Schedule, candidate, and question are required.")
        }
        if (score < 0 || score > maxScore) {
            return SaveResult.Error("Score must be between 0 and $maxScore.")
        }
        return repository.recordScore(
            PracticalScore(
                scheduleId = schedule,
                candidateId = candidate,
                questionId = question,
                score = score,
                scoredAt = clock.nowMillis(),
                syncStatus = SyncStatus.Pending,
            ),
        )
    }
}
