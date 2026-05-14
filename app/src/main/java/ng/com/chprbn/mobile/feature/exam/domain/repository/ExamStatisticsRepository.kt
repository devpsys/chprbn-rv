package ng.com.chprbn.mobile.feature.exam.domain.repository

import ng.com.chprbn.mobile.feature.exam.domain.model.ExamStatistics
import ng.com.chprbn.mobile.feature.exam.domain.model.SaveResult

/**
 * Backs the Statistics screen. [getStatistics] aggregates SQL counts;
 * [clearLocalCache] is the destructive companion behind the "Clear
 * Cached Records" button, gated by the UI's warning dialog.
 */
interface ExamStatisticsRepository {

    suspend fun getStatistics(): ExamStatistics

    suspend fun clearLocalCache(): SaveResult
}
