package ng.com.chprbn.mobile.feature.assessment.domain.repository

import ng.com.chprbn.mobile.feature.assessment.domain.model.AssessmentPaperDetailResult
import ng.com.chprbn.mobile.feature.assessment.domain.model.AssessmentSchedule
import ng.com.chprbn.mobile.feature.assessment.domain.model.DownloadAssessmentPackageResult
import ng.com.chprbn.mobile.feature.assessment.domain.model.SaveResult

/**
 * Owns the schedule list, paper-detail read, and the "download the world"
 * package fetch for one schedule (paper + sections + questions + candidate
 * roster). [clearCache] is the destructive companion exposed behind the UI's
 * download-warning prompt; `null` scheduleId wipes every schedule's local
 * package.
 */
interface AssessmentScheduleRepository {

    suspend fun getSchedules(): List<AssessmentSchedule>

    suspend fun getPaperDetail(scheduleId: String): AssessmentPaperDetailResult

    suspend fun downloadPackage(scheduleId: String): DownloadAssessmentPackageResult

    suspend fun clearCache(scheduleId: String? = null): SaveResult
}
