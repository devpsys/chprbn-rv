package ng.com.chprbn.mobile.feature.assessment.domain.repository

import ng.com.chprbn.mobile.feature.assessment.domain.model.ProjectScore
import ng.com.chprbn.mobile.feature.assessment.domain.model.SaveResult

/**
 * Single project-score read/write for the
 * `AssessmentProjectAssessment` screen. One row per `(scheduleId,
 * candidateId)`; `getProjectScore` returns `null` when the candidate's
 * project has not yet been scored, which the UI uses to seed the input
 * field blank.
 *
 * `recordProjectScore` is the only write path — both the dialog and
 * full-screen variants funnel through it.
 */
interface ProjectScoringRepository {

    suspend fun getProjectScore(scheduleId: String, candidateId: String): ProjectScore?

    suspend fun recordProjectScore(score: ProjectScore): SaveResult
}
