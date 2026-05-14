package ng.com.chprbn.mobile.feature.assessment.domain.model

import ng.com.chprbn.mobile.core.domain.model.Candidate
import ng.com.chprbn.mobile.core.domain.model.SyncStatus

/**
 * One candidate row in the assessment-side directory. Aggregates per-question
 * `PracticalScore` rows into a single rolled-up view for list rendering:
 *
 * - [aggregateScore]: sum of `PracticalScore.score` plus `ProjectScore.score`
 *   (rounded) across all sections for this candidate; computed in SQL via
 *   `SUM(score)` rather than pulled into the JVM.
 * - [scoredQuestions] / [totalQuestions]: drives the "Synced/Unsynced" per-row
 *   pill on `AssessmentPaperDetail`.
 * - [syncStatus]: derived from the candidate's score rows — `Failed` if any
 *   are failed, else `Pending` if any are pending, else `Synced`.
 */
data class AssessmentCandidateRow(
    val candidate: Candidate,
    val aggregateScore: Int,
    val level: ScoreLevel,
    val scoredQuestions: Int,
    val totalQuestions: Int,
    val syncStatus: SyncStatus,
)
