package ng.com.chprbn.mobile.feature.assessment.domain.model

import ng.com.chprbn.mobile.core.domain.model.Candidate
import ng.com.chprbn.mobile.core.domain.model.SyncStatus

/**
 * One candidate row in the assessment-side directory.
 *
 * - [aggregateScore]: the candidate's `ProjectScore.score` (rounded).
 *   Practical marks are not rolled in.
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
