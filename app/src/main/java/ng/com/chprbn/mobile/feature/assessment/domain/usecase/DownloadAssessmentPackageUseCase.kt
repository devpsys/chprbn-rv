package ng.com.chprbn.mobile.feature.assessment.domain.usecase

import ng.com.chprbn.mobile.feature.assessment.domain.model.DownloadAssessmentPackageResult
import ng.com.chprbn.mobile.feature.assessment.domain.repository.AssessmentScheduleRepository
import javax.inject.Inject

/**
 * Pulls a schedule's reference package (paper, sections, questions,
 * candidate roster) into the local encrypted cache. Destructive — wipes
 * any existing rows for this schedule first; gated by the UI's
 * download-warning prompt.
 */
class DownloadAssessmentPackageUseCase @Inject constructor(
    private val repository: AssessmentScheduleRepository,
) {
    suspend operator fun invoke(scheduleId: String): DownloadAssessmentPackageResult {
        val trimmed = scheduleId.trim()
        if (trimmed.isEmpty()) {
            return DownloadAssessmentPackageResult.Error("Schedule id is required.")
        }
        return repository.downloadPackage(trimmed)
    }
}
