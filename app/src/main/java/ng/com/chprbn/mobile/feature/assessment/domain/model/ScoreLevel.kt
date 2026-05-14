package ng.com.chprbn.mobile.feature.assessment.domain.model

/**
 * Coarse aggregate-score band used to colour the score pill on the
 * `AssessmentCandidates` screen.
 *
 * The "below threshold = Low" rule lives here as [fromScore], with the
 * threshold defaulted to [DEFAULT_LOW_THRESHOLD] (50). Callers that need a
 * different cohort threshold pass an explicit value — keeps the convenience
 * default while allowing the use case to override without touching the
 * mapper layer.
 */
enum class ScoreLevel {
    Normal,
    Low;

    companion object {
        const val DEFAULT_LOW_THRESHOLD = 50

        fun fromScore(score: Int, threshold: Int = DEFAULT_LOW_THRESHOLD): ScoreLevel =
            if (score < threshold) Low else Normal
    }
}
