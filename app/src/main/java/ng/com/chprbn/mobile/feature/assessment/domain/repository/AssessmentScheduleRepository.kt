package ng.com.chprbn.mobile.feature.assessment.domain.repository

import ng.com.chprbn.mobile.feature.assessment.domain.model.AssessmentPaperDetailResult
import ng.com.chprbn.mobile.feature.assessment.domain.model.AssessmentSchedule
import ng.com.chprbn.mobile.feature.assessment.domain.model.SaveResult

/**
 * Owns the schedule list and paper-detail read for the assessment feature.
 *
 * There is no per-schedule "download package" step any longer — sections
 * and questions arrive on the exam dossier and are persisted by
 * `ExamSyncRepositoryImpl`. [clearCache] wipes the schedule's practical
 * reference + score rows for a rebuild; `null` wipes every schedule's
 * local data.
 */
interface AssessmentScheduleRepository {

    suspend fun getSchedules(): List<AssessmentSchedule>

    suspend fun getPaperDetail(scheduleId: String): AssessmentPaperDetailResult

    suspend fun clearCache(scheduleId: String? = null): SaveResult
}
