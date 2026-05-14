package ng.com.chprbn.mobile.feature.exam.domain.usecase

import ng.com.chprbn.mobile.feature.exam.domain.model.ExamDashboardResult
import ng.com.chprbn.mobile.feature.exam.domain.repository.ExamPaperRepository
import javax.inject.Inject

class GetExamDashboardUseCase @Inject constructor(
    private val repository: ExamPaperRepository,
) {
    suspend operator fun invoke(): ExamDashboardResult = repository.getDashboardSummary()
}
