package ng.com.chprbn.mobile.feature.exam.data.repository

import ng.com.chprbn.mobile.core.session.SessionScopedCleaner
import ng.com.chprbn.mobile.feature.exam.domain.repository.ExamStatisticsRepository
import javax.inject.Inject

/**
 * SessionScopedCleaner wrapper around [ExamStatisticsRepository.clearLocalCache].
 * Bound via `@IntoSet` in `ExamDataModule`. **Currently unused/unwired** —
 * [SessionCleaner] itself is no longer called from logout, see its doc
 * comment.
 */
class ExamSessionCleaner @Inject constructor(
    private val examStatisticsRepository: ExamStatisticsRepository,
) : SessionScopedCleaner {
    override suspend fun clear() {
        // Repository returns SaveResult.Error on failure — we don't
        // propagate, per SessionScopedCleaner's "must not throw" contract.
        // The parent SessionCleaner logs and moves on.
        examStatisticsRepository.clearLocalCache()
    }
}
