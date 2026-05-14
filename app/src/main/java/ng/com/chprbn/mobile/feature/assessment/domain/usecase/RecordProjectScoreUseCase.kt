package ng.com.chprbn.mobile.feature.assessment.domain.usecase

import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.core.sync.Clock
import ng.com.chprbn.mobile.feature.assessment.domain.model.ProjectScore
import ng.com.chprbn.mobile.feature.assessment.domain.model.SaveResult
import ng.com.chprbn.mobile.feature.assessment.domain.repository.ProjectScoringRepository
import javax.inject.Inject

/**
 * One-shot save of a candidate's project score. Backs both the full-screen
 * and dialog variants of `AssessmentProjectAssessment`.
 *
 * Validates:
 * - all identifiers non-blank,
 * - `0.0 ≤ score ≤ maxScore`,
 * - at most two decimal places (matches the UI's `\d{1,2}(\.\d?)?` regex —
 *   the use case is the authoritative truth so a misbehaving caller can't
 *   slip a `1.234` past the persistence layer).
 *
 * The mid-typing `scoreText` parsing belongs in the ViewModel; by the time
 * this use case is invoked the value has been parsed and is a real Double.
 */
class RecordProjectScoreUseCase @Inject constructor(
    private val repository: ProjectScoringRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(
        scheduleId: String,
        candidateId: String,
        score: Double,
        maxScore: Int,
    ): SaveResult {
        val schedule = scheduleId.trim()
        val candidate = candidateId.trim()
        if (schedule.isEmpty() || candidate.isEmpty()) {
            return SaveResult.Error("Schedule and candidate are required.")
        }
        if (score.isNaN() || score.isInfinite()) {
            return SaveResult.Error("Score must be a real number.")
        }
        if (score < 0.0 || score > maxScore.toDouble()) {
            return SaveResult.Error("Score must be between 0 and $maxScore.")
        }
        if (!hasAtMostTwoDecimals(score)) {
            return SaveResult.Error("Score may have at most two decimal places.")
        }
        return repository.recordProjectScore(
            ProjectScore(
                scheduleId = schedule,
                candidateId = candidate,
                score = score,
                maxScore = maxScore,
                scoredAt = clock.nowMillis(),
                syncStatus = SyncStatus.Pending,
            ),
        )
    }

    // Compares `score * 100` to its rounded long; tolerates floating-point
    // imprecision via the small epsilon. Avoids `BigDecimal` round-trip cost
    // on a hot path (every project Save tap goes through here).
    private fun hasAtMostTwoDecimals(score: Double): Boolean {
        val scaled = score * 100.0
        return kotlin.math.abs(scaled - kotlin.math.round(scaled)) < EPSILON
    }

    private companion object {
        const val EPSILON = 1e-9
    }
}
