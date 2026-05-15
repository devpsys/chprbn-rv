package ng.com.chprbn.mobile.feature.exam.data.repository

import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.feature.exam.data.local.AttendanceDao
import ng.com.chprbn.mobile.feature.exam.data.local.CandidateDao
import ng.com.chprbn.mobile.feature.exam.data.local.CenterDao
import ng.com.chprbn.mobile.feature.exam.data.local.ExamDatabase
import ng.com.chprbn.mobile.feature.exam.data.local.PaperDao
import ng.com.chprbn.mobile.feature.exam.data.local.RemarkDao
import ng.com.chprbn.mobile.feature.exam.domain.model.ExamStatistics
import ng.com.chprbn.mobile.feature.exam.domain.model.SaveResult
import ng.com.chprbn.mobile.feature.exam.domain.repository.ExamStatisticsRepository
import javax.inject.Inject

/**
 * Aggregations for `ExamStatisticsScreen` plus the destructive
 * `clearLocalCache` companion behind the "Clear Cached Records" button.
 *
 * `recordsDownloaded` counts assignments (each paper × candidate pair),
 * `attendanceCaptured` counts attendance rows regardless of status,
 * and the cached / synced / pending / failed buckets are pulled
 * straight from the attendance table's `syncStatus` column.
 */
class ExamStatisticsRepositoryImpl @Inject constructor(
    private val db: ExamDatabase,
    private val centerDao: CenterDao,
    private val paperDao: PaperDao,
    private val candidateDao: CandidateDao,
    private val attendanceDao: AttendanceDao,
    private val remarkDao: RemarkDao,
) : ExamStatisticsRepository {

    override suspend fun getStatistics(): ExamStatistics = withContext(Dispatchers.IO) {
        val attendanceCaptured = attendanceDao.totalCount()
        val pendingCount = attendanceDao.countBySyncStatus(SyncStatus.Pending.name)
        val failedCount = attendanceDao.countBySyncStatus(SyncStatus.Failed.name)
        val syncedCount = attendanceDao.countBySyncStatus(SyncStatus.Synced.name)
        ExamStatistics(
            recordsDownloaded = candidateDao.assignmentCount(),
            attendanceCaptured = attendanceCaptured,
            syncedCount = syncedCount,
            cachedCount = attendanceCaptured,
            pendingCount = pendingCount,
            failedCount = failedCount,
            lastUpdatedAt = attendanceDao.mostRecentMarkedAt(),
        )
    }

    override suspend fun clearLocalCache(): SaveResult = withContext(Dispatchers.IO) {
        try {
            db.withTransaction {
                attendanceDao.clearAll()
                remarkDao.clearAll()
                candidateDao.clearAssignments()
                candidateDao.clearCandidates()
                paperDao.clearAll()
                centerDao.clearAll()
            }
            SaveResult.Success
        } catch (t: Throwable) {
            SaveResult.Error(t.message ?: "Unable to clear cache.")
        }
    }
}
