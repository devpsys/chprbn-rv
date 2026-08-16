package ng.com.chprbn.mobile.feature.exam.data.repository

import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.feature.assessment.data.local.AssessmentDatabase
import ng.com.chprbn.mobile.feature.assessment.data.local.PracticalScoreDao
import ng.com.chprbn.mobile.feature.assessment.data.local.ProjectScoreDao
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
 * `recordsDownloaded` counts assignments (each paper × candidate pair).
 * Attendance and project captured counts are row totals; practical
 * captured is distinct candidates assessed (not per-question rows).
 * The cached / synced / pending / failed buckets still sum the three
 * tables' rows so they match what Sync Now flushes through
 * [ng.com.chprbn.mobile.core.sync.SyncBatchRunner].
 */
class ExamStatisticsRepositoryImpl @Inject constructor(
    private val db: ExamDatabase,
    private val assessmentDb: AssessmentDatabase,
    private val centerDao: CenterDao,
    private val paperDao: PaperDao,
    private val candidateDao: CandidateDao,
    private val attendanceDao: AttendanceDao,
    private val remarkDao: RemarkDao,
    private val practicalScoreDao: PracticalScoreDao,
    private val projectScoreDao: ProjectScoreDao,
) : ExamStatisticsRepository {

    override suspend fun getStatistics(): ExamStatistics = withContext(Dispatchers.IO) {
        val attendanceCaptured = attendanceDao.totalCount()
        val practicalRows = practicalScoreDao.totalCount()
        val practicalCaptured = practicalScoreDao.assessedCandidateCount()
        val projectCaptured = projectScoreDao.totalCount()
        val pendingCount = countByStatus(SyncStatus.Pending)
        val failedCount = countByStatus(SyncStatus.Failed)
        val syncedCount = countByStatus(SyncStatus.Synced)
        ExamStatistics(
            recordsDownloaded = candidateDao.assignmentCount(),
            attendanceCaptured = attendanceCaptured,
            practicalCaptured = practicalCaptured,
            projectCaptured = projectCaptured,
            syncedCount = syncedCount,
            cachedCount = attendanceCaptured + practicalRows + projectCaptured,
            pendingCount = pendingCount,
            failedCount = failedCount,
            lastUpdatedAt = maxTimestamp(
                attendanceDao.mostRecentMarkedAt(),
                practicalScoreDao.mostRecentScoredAt(),
                projectScoreDao.mostRecentScoredAt(),
            ),
        )
    }

    override suspend fun clearLocalCache(): SaveResult = withContext(Dispatchers.IO) {
        try {
            // Two files, two transactions — Room can't span exam.db and
            // assessment.db. Exam wipe first, then practical/project scores.
            db.withTransaction {
                attendanceDao.clearAll()
                remarkDao.clearAll()
                candidateDao.clearAssignments()
                candidateDao.clearCandidates()
                paperDao.clearAll()
                centerDao.clearAll()
            }
            assessmentDb.withTransaction {
                practicalScoreDao.clearAll()
                projectScoreDao.clearAll()
            }
            SaveResult.Success
        } catch (t: Throwable) {
            SaveResult.Error(t.message ?: "Unable to clear cache.")
        }
    }

    private suspend fun countByStatus(status: SyncStatus): Int {
        val name = status.name
        return attendanceDao.countBySyncStatus(name) +
            practicalScoreDao.countByStatus(name) +
            projectScoreDao.countByStatus(name)
    }

    private fun maxTimestamp(vararg values: Long?): Long? =
        values.filterNotNull().maxOrNull()
}
