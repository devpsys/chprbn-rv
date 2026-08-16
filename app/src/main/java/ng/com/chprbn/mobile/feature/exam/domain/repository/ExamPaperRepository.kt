package ng.com.chprbn.mobile.feature.exam.domain.repository

import ng.com.chprbn.mobile.feature.exam.domain.model.ExamDashboardResult
import ng.com.chprbn.mobile.feature.exam.domain.model.ExamPaperDetailResult
import ng.com.chprbn.mobile.feature.exam.domain.model.Paper

/**
 * Read-side for the exam dashboard, the day's papers, and a single-paper
 * detail. Papers and centre data are downloaded as part of the dossier
 * and read back from cache here; no remote calls happen on these paths.
 */
interface ExamPaperRepository {

    suspend fun getDashboardSummary(): ExamDashboardResult

    /**
     * Papers scheduled today whose kind is NOT `Practical` / `Project`.
     * PE/PA papers are surfaced by the assessment feature (see
     * [getAssessmentPapers]) — same source table, different filter.
     */
    suspend fun getPapersForToday(): List<Paper>

    /**
     * Papers scheduled today whose kind IS `Practical` / `Project` — the
     * source rows the assessment feature adapts into `AssessmentSchedule`
     * for its schedules list. Kept on this repository (not duplicated
     * server-side) so both features read from the single `/exam/dossier`
     * download instead of a dedicated `/assessments/schedules` endpoint.
     */
    suspend fun getAssessmentPapers(): List<Paper>

    /**
     * Lightweight single-paper lookup — returns the `Paper` row from the
     * dossier's cache without the attendance/centre join that
     * [getPaperDetail] performs. Used by the assessment feature to render
     * a paper-detail shell for a PE/PA paper before the officer has
     * downloaded that schedule's package.
     */
    suspend fun getPaperById(id: String): Paper?

    suspend fun getPaperDetail(paperId: String): ExamPaperDetailResult
}
