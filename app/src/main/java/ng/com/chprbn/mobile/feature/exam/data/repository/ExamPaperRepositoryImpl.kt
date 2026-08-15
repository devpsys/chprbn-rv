package ng.com.chprbn.mobile.feature.exam.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.feature.exam.data.local.AttendanceDao
import ng.com.chprbn.mobile.feature.exam.data.local.CenterDao
import ng.com.chprbn.mobile.feature.exam.data.local.PaperDao
import ng.com.chprbn.mobile.feature.exam.data.mappers.toDomain
import ng.com.chprbn.mobile.feature.exam.domain.model.Center
import ng.com.chprbn.mobile.feature.exam.domain.model.ExamDashboardResult
import ng.com.chprbn.mobile.feature.exam.domain.model.ExamDashboardSummary
import ng.com.chprbn.mobile.feature.exam.domain.model.ExamPaperDetail
import ng.com.chprbn.mobile.feature.exam.domain.model.ExamPaperDetailResult
import ng.com.chprbn.mobile.feature.exam.domain.model.ExamTaskSummary
import ng.com.chprbn.mobile.feature.exam.domain.model.OfficerSession
import ng.com.chprbn.mobile.feature.exam.domain.model.Paper
import ng.com.chprbn.mobile.feature.exam.domain.repository.ExamPaperRepository
import java.io.IOException
import java.time.LocalDate
import javax.inject.Inject

/**
 * Read-side over the exam reference data and derived counters. The
 * dashboard summary is synthesised from whichever centre is currently
 * cached locally — the v1 implementation does not yet integrate with an
 * auth-feature `OfficerSession` source; that's a follow-up once login
 * carries officer/centre identity.
 *
 * `getPaperDetail` derives the checked-in count and pending-sync count
 * by querying [AttendanceDao] aggregates rather than persisting them.
 */
class ExamPaperRepositoryImpl @Inject constructor(
    private val centerDao: CenterDao,
    private val paperDao: PaperDao,
    private val attendanceDao: AttendanceDao,
) : ExamPaperRepository {

    override suspend fun getDashboardSummary(): ExamDashboardResult =
        withContext(Dispatchers.IO) {
            try {
                // Resolved directly from `centers`, not derived from a paper's
                // centerId — a center can be downloaded with zero papers
                // scheduled for today, and that's a valid Success, not Empty.
                val centerEntity = centerDao.getFirst()
                    ?: return@withContext ExamDashboardResult.Empty

                val papers = paperDao.getForCenter(centerEntity.id)
                val totalCandidates = papers.sumOf { it.totalCandidates }
                val checkedIn = papers.firstOrNull()?.let {
                    attendanceDao.countMarkedForPaper(paperId = it.id)
                } ?: 0

                ExamDashboardResult.Success(
                    ExamDashboardSummary(
                        // Placeholder OfficerSession — wire to auth feature
                        // when login exposes the active officer/centre/day.
                        session = OfficerSession(
                            officerId = "",
                            centerId = centerEntity.id,
                            dayIso = LocalDate.now().toString(),
                        ),
                        center = centerEntity.toDomain(),
                        papersCount = papers.size,
                        attendanceCard = ExamTaskSummary(
                            statusLabel = if (papers.isNotEmpty()) "Active Session" else "No Session",
                            countLabel = "$checkedIn / $totalCandidates checked in",
                        ),
                        practicalCard = ExamTaskSummary(
                            statusLabel = "Pending Grading",
                            countLabel = "${papers.count { it.paperKind == ng.com.chprbn.mobile.core.domain.model.PaperKind.Practical.name }} papers",
                        ),
                    ),
                )
            } catch (t: IOException) {
                ExamDashboardResult.Error("Network error. Please check your connection.")
            } catch (t: Throwable) {
                ExamDashboardResult.Error(t.message ?: "Unable to load dashboard.")
            }
        }

    override suspend fun getPapersForToday(): List<Paper> = withContext(Dispatchers.IO) {
        // SQL-side filter: PE/PA go to the assessment feature, everything
        // else shows on the exam-papers screen. Same source table.
        paperDao.getExamPapers().map { it.toDomain() }
    }

    override suspend fun getAssessmentPapers(): List<Paper> = withContext(Dispatchers.IO) {
        paperDao.getAssessmentPapers().map { it.toDomain() }
    }

    override suspend fun getPaperDetail(paperId: String): ExamPaperDetailResult =
        withContext(Dispatchers.IO) {
            try {
                val paper = paperDao.getById(paperId)
                    ?: return@withContext ExamPaperDetailResult.NotFound
                val center = centerDao.getById(paper.centerId)
                    ?: return@withContext ExamPaperDetailResult.NotFound

                val checkedIn = attendanceDao.countMarkedForPaper(paperId)
                val pending = attendanceDao.countBySyncStatus(SyncStatus.Pending.name) +
                    attendanceDao.countBySyncStatus(SyncStatus.Failed.name)
                val lastMarked = attendanceDao.mostRecentMarkedAt()

                ExamPaperDetailResult.Success(
                    ExamPaperDetail(
                        paper = paper.toDomain(),
                        center = Center(
                            id = center.id,
                            name = center.name,
                            code = center.code,
                            location = center.location,
                            heroImageUrl = center.heroImageUrl,
                        ),
                        totalCandidates = paper.totalCandidates,
                        checkedInCount = checkedIn,
                        lastSyncAt = lastMarked,
                        pendingSyncCount = pending,
                    ),
                )
            } catch (t: IOException) {
                ExamPaperDetailResult.Error("Network error. Please check your connection.")
            } catch (t: Throwable) {
                ExamPaperDetailResult.Error(t.message ?: "Unable to load paper detail.")
            }
        }
}
