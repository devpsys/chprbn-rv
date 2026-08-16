package ng.com.chprbn.mobile.feature.assessment.data.repository

import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.feature.assessment.data.local.AssessmentCandidateDao
import ng.com.chprbn.mobile.feature.assessment.data.local.AssessmentDatabase
import ng.com.chprbn.mobile.feature.assessment.data.local.AssessmentPaperDao
import ng.com.chprbn.mobile.feature.assessment.data.local.PracticalScoreDao
import ng.com.chprbn.mobile.feature.assessment.data.local.PracticalSectionDao
import ng.com.chprbn.mobile.feature.assessment.data.local.ProjectScoreDao
import ng.com.chprbn.mobile.feature.assessment.data.local.SectionQuestionDao
import ng.com.chprbn.mobile.feature.assessment.data.mappers.toDomain
import ng.com.chprbn.mobile.feature.assessment.domain.model.AssessmentPaperDetailResult
import ng.com.chprbn.mobile.feature.assessment.domain.model.AssessmentSchedule
import ng.com.chprbn.mobile.feature.assessment.domain.model.SaveResult
import ng.com.chprbn.mobile.feature.assessment.domain.repository.AssessmentScheduleRepository
import ng.com.chprbn.mobile.feature.exam.domain.model.Paper as ExamPaper
import ng.com.chprbn.mobile.feature.exam.domain.repository.ExamPaperRepository
import java.io.IOException
import javax.inject.Inject

/**
 * Owns the schedule list + paper-detail read for the assessment feature.
 *
 * **Schedules are not a separate wire concept.** They're just the PE
 * (`Practical`) + PA (`Project`) subset of the exam dossier — the same
 * `/exam/dossier` payload the exam feature reads. [getSchedules] adapts
 * `ExamPaperRepository.getAssessmentPapers()` into [AssessmentSchedule]
 * rows and computes each schedule's aggregate `syncStatus` on read from
 * the score DAOs (was previously a persisted column on the removed
 * `assessment_schedules` table).
 *
 * **Sections and questions are not fetched here either.** They arrive on
 * the exam dossier's `sections[]` and are persisted into
 * `practical_sections` + `section_questions` by `ExamSyncRepositoryImpl`.
 * [clearCache] wipes the assessment-side rows for a rebuild — pending
 * score rows are only wiped on the global (`scheduleId == null`) path.
 */
class AssessmentScheduleRepositoryImpl @Inject constructor(
    private val db: AssessmentDatabase,
    private val paperDao: AssessmentPaperDao,
    private val sectionDao: PracticalSectionDao,
    private val questionDao: SectionQuestionDao,
    private val candidateDao: AssessmentCandidateDao,
    private val practicalScoreDao: PracticalScoreDao,
    private val projectScoreDao: ProjectScoreDao,
    private val examPaperRepository: ExamPaperRepository,
) : AssessmentScheduleRepository {

    override suspend fun getSchedules(): List<AssessmentSchedule> = withContext(Dispatchers.IO) {
        examPaperRepository.getAssessmentPapers().map { it.toAssessmentSchedule() }
    }

    /**
     * Priority: `Failed > Pending > Synced`. A schedule with no score rows
     * is vacuously `Synced` — the UI already suppresses the pill in that
     * case via row counts on the candidates list.
     */
    private suspend fun ExamPaper.toAssessmentSchedule(): AssessmentSchedule {
        val scheduleId = id
        val failed = practicalScoreDao.countByStatusForSchedule(scheduleId, SyncStatus.Failed.name) +
            projectScoreDao.countByStatusForSchedule(scheduleId, SyncStatus.Failed.name)
        val syncStatus = when {
            failed > 0 -> SyncStatus.Failed
            else -> {
                val pending = practicalScoreDao.countByStatusForSchedule(scheduleId, SyncStatus.Pending.name) +
                    projectScoreDao.countByStatusForSchedule(scheduleId, SyncStatus.Pending.name)
                if (pending > 0) SyncStatus.Pending else SyncStatus.Synced
            }
        }
        return AssessmentSchedule(
            id = scheduleId,
            title = title.ifBlank { subtitle },
            date = startAt,
            paperKind = paperKind,
            centerId = centerId,
            syncStatus = syncStatus,
        )
    }

    override suspend fun getPaperDetail(scheduleId: String): AssessmentPaperDetailResult =
        withContext(Dispatchers.IO) {
            try {
                // Full record — populated by downloadPackage(scheduleId).
                val entity = paperDao.getByScheduleId(scheduleId)
                if (entity != null) {
                    return@withContext AssessmentPaperDetailResult.Success(entity.toDomain())
                }
                // Fallback: no package downloaded yet, but the schedule row
                // on the schedules screen came from the exam dossier — so
                // the same paper id is already in exam.db. Render a shell
                // (title + code, empty facility/hall) so the user sees the
                // paper immediately and can trigger the Download Package
                // action from the screen for the full data. Returns
                // NotFound only when the id doesn't exist anywhere, which
                // shouldn't happen for a schedule the officer just tapped.
                val examPaper = examPaperRepository.getPaperById(scheduleId)
                    ?: return@withContext AssessmentPaperDetailResult.NotFound
                AssessmentPaperDetailResult.Success(examPaper.toAssessmentPaperShell())
            } catch (t: IOException) {
                AssessmentPaperDetailResult.Error(
                    "Network error. Please check your connection.",
                )
            } catch (t: Throwable) {
                AssessmentPaperDetailResult.Error(
                    t.message ?: "Unable to load paper detail.",
                )
            }
        }

    /**
     * Minimal `AssessmentPaper` shell built from an exam-side [ExamPaper].
     * Only the identity + display fields ([scheduleId], [title]) carry
     * real data; the rest (facility, hall, hero) are placeholders until
     * `downloadPackage(scheduleId)` populates the full row.
     */
    private fun ExamPaper.toAssessmentPaperShell(): ng.com.chprbn.mobile.feature.assessment.domain.model.AssessmentPaper =
        ng.com.chprbn.mobile.feature.assessment.domain.model.AssessmentPaper(
            scheduleId = id,
            title = title.ifBlank { subtitle },
            statusLabel = "",
            facility = ng.com.chprbn.mobile.feature.assessment.domain.model.Facility("", ""),
            hall = ng.com.chprbn.mobile.feature.assessment.domain.model.Hall("", ""),
            heroImageUrl = null,
        )

    override suspend fun clearCache(scheduleId: String?): SaveResult =
        withContext(Dispatchers.IO) {
            try {
                db.withTransaction {
                    if (scheduleId == null) {
                        // Global clear: wipe scores too. Called from the
                        // logout / clear-cache paths where a different user
                        // must never inherit another officer's rows. The
                        // per-schedule branch below intentionally preserves
                        // scores so a re-download of a package doesn't drop
                        // pending writes.
                        practicalScoreDao.clearAll()
                        projectScoreDao.clearAll()
                        questionDao.clearAll()
                        sectionDao.clearAll()
                        paperDao.clearAll()
                        candidateDao.clearCandidates()
                    } else {
                        questionDao.deleteByScheduleId(scheduleId)
                        sectionDao.deleteByScheduleId(scheduleId)
                        paperDao.deleteByScheduleId(scheduleId)
                        candidateDao.deleteAssignmentsForSchedule(scheduleId)
                        practicalScoreDao.deleteForSchedule(scheduleId)
                        projectScoreDao.deleteForSchedule(scheduleId)
                    }
                }
                SaveResult.Success
            } catch (t: Throwable) {
                SaveResult.Error(t.message ?: "Unable to clear cache.")
            }
        }
}
