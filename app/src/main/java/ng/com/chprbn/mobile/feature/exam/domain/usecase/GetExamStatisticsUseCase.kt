package ng.com.chprbn.mobile.feature.exam.domain.usecase

import ng.com.chprbn.mobile.feature.exam.domain.model.ExamStatistics
import ng.com.chprbn.mobile.feature.exam.domain.repository.ExamStatisticsRepository
import javax.inject.Inject

class GetExamStatisticsUseCase @Inject constructor(
    private val repository: ExamStatisticsRepository,
) {
    suspend operator fun invoke(): ExamStatistics = repository.getStatistics()
}
