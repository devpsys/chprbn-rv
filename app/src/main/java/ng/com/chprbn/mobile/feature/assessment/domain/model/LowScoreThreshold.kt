package ng.com.chprbn.mobile.feature.assessment.domain.model

import javax.inject.Qualifier

/**
 * Hilt qualifier for the cohort-level "below this aggregate score = Low"
 * threshold that drives `ScoreLevel.fromScore` and the score-pill colour
 * on the `AssessmentCandidates` screen.
 *
 * Bound in `feature/assessment/data/di/AssessmentDataModule` to
 * [ScoreLevel.DEFAULT_LOW_THRESHOLD] (50) by default. Wired here as an
 * injected `Int` rather than baked into the use case so a future
 * per-cohort / per-schedule override only needs the Hilt binding to
 * change — no use-case constructor or mapper layer touched.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class LowScoreThreshold
