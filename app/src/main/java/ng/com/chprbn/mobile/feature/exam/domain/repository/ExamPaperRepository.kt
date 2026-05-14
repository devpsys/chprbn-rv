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

    suspend fun getPapersForToday(): List<Paper>

    suspend fun getPaperDetail(paperId: String): ExamPaperDetailResult
}
