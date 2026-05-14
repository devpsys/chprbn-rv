package ng.com.chprbn.mobile.feature.exam.domain.usecase

import ng.com.chprbn.mobile.feature.exam.domain.model.SaveResult
import ng.com.chprbn.mobile.feature.exam.domain.repository.ExamStatisticsRepository
import javax.inject.Inject

/**
 * Wipes every locally-cached exam row. Pending attendance / remark
 * writes are lost — the caller must surface the destructive-warning
 * dialog before invoking this.
 */
class ClearExamCacheUseCase @Inject constructor(
    private val repository: ExamStatisticsRepository,
) {
    suspend operator fun invoke(): SaveResult = repository.clearLocalCache()
}
