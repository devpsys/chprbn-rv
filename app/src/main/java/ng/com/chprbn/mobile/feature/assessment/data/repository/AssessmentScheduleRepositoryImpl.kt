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
import ng.com.chprbn.mobile.feature.assessment.data.local.ScheduleCandidateAssignmentEntity
import ng.com.chprbn.mobile.feature.assessment.data.local.SectionQuestionDao
import ng.com.chprbn.mobile.feature.assessment.data.mappers.toAssessmentCandidateEntity
import ng.com.chprbn.mobile.feature.assessment.data.mappers.toDomain
import ng.com.chprbn.mobile.feature.assessment.data.mappers.toEntity
import ng.com.chprbn.mobile.feature.assessment.data.source.AssessmentPackageRemoteSource
import ng.com.chprbn.mobile.feature.assessment.domain.model.AssessmentPaperDetailResult
import ng.com.chprbn.mobile.feature.assessment.domain.model.AssessmentSchedule
import ng.com.chprbn.mobile.feature.assessment.domain.model.DownloadAssessmentPackageResult
import ng.com.chprbn.mobile.feature.assessment.domain.model.SaveResult
import ng.com.chprbn.mobile.feature.assessment.domain.repository.AssessmentScheduleRepository
import ng.com.chprbn.mobile.feature.exam.domain.model.Paper as ExamPaper
import ng.com.chprbn.mobile.feature.exam.domain.repository.ExamPaperRepository
import java.io.IOException
import javax.inject.Inject

/**
 * Owns the schedule list, paper-detail read, and the destructive
 * per-schedule package download. The package replace runs inside
 * `db.withTransaction { … }` so a partial write can't leave the local
 * cache in an inconsistent state.
 *
 * Critically, [downloadPackage] never touches `practical_scores` /
 * `project_scores` — pending writes survive a re-download (the explicit
 * UX contract behind the download-warning prompt).
 *
 * **Schedules are not a separate wire concept.** They're just the PE
 * (`Practical`) + PA (`Project`) subset of the exam dossier — the same
 * `/exam/dossier` payload the exam feature reads. [getSchedules] adapts
 * `ExamPaperRepository.getAssessmentPapers()` into [AssessmentSchedule]
 * rows and computes each schedule's aggregate `syncStatus` on read from
 * the score DAOs (was previously a persisted column on the removed
 * `assessment_schedules` table).
 */
class AssessmentScheduleRepositoryImpl @Inject constructor(
    private val db: AssessmentDatabase,
    private val paperDao: AssessmentPaperDao,
    private val sectionDao: PracticalSectionDao,
    private val questionDao: SectionQuestionDao,
    private val candidateDao: AssessmentCandidateDao,
    private val practicalScoreDao: PracticalScoreDao,
    private val projectScoreDao: ProjectScoreDao,
    private val remoteSource: AssessmentPackageRemoteSource,
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
                val entity = paperDao.getByScheduleId(scheduleId)
                    ?: return@withContext AssessmentPaperDetailResult.NotFound
                AssessmentPaperDetailResult.Success(entity.toDomain())
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

    override suspend fun downloadPackage(scheduleId: String): DownloadAssessmentPackageResult =
        withContext(Dispatchers.IO) {
            // remoteSource.fetchPackage throws on transport/envelope error
            // (A-S1 fix). Preserve the caught message so the officer sees
            // the specific reason rather than a generic "could not download."
            val bundle = runCatching { remoteSource.fetchPackage(scheduleId) }.fold(
                onSuccess = { it },
                onFailure = { t ->
                    return@withContext DownloadAssessmentPackageResult.Error(
                        t.message ?: "Could not download package for $scheduleId.",
                    )
                },
            ) ?: return@withContext DownloadAssessmentPackageResult.Error(
                "The server has no package for $scheduleId.",
            )

            try {
                db.withTransaction {
                    // Wipe stale reference rows in FK-respecting order.
                    // Scores are deliberately untouched — pending writes survive.
                    questionDao.deleteByScheduleId(scheduleId)
                    sectionDao.deleteByScheduleId(scheduleId)
                    paperDao.deleteByScheduleId(scheduleId)
                    candidateDao.deleteAssignmentsForSchedule(scheduleId)

                    paperDao.upsert(bundle.paper.toEntity())
                    sectionDao.upsertAll(bundle.sections.map { it.toEntity() })
                    questionDao.upsertAll(bundle.questions.map { it.toEntity() })
                    candidateDao.upsertAll(bundle.candidates.map { it.toAssessmentCandidateEntity() })
                    candidateDao.upsertAssignments(
                        bundle.candidates.map { candidate ->
                            ScheduleCandidateAssignmentEntity(
                                scheduleId = bundle.paper.scheduleId,
                                candidateId = candidate.id,
                            )
                        },
                    )
                }
                DownloadAssessmentPackageResult.Success(
                    scheduleId = scheduleId,
                    candidatesCount = bundle.candidates.size,
                    sectionsCount = bundle.sections.size,
                    questionsCount = bundle.questions.size,
                )
            } catch (t: Throwable) {
                DownloadAssessmentPackageResult.Error(
                    t.message ?: "Could not persist downloaded package.",
                )
            }
        }

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
