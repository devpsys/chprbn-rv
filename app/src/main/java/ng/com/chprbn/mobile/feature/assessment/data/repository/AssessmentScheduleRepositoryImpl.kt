package ng.com.chprbn.mobile.feature.assessment.data.repository

import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ng.com.chprbn.mobile.feature.assessment.data.local.AssessmentCandidateDao
import ng.com.chprbn.mobile.feature.assessment.data.local.AssessmentDatabase
import ng.com.chprbn.mobile.feature.assessment.data.local.AssessmentPaperDao
import ng.com.chprbn.mobile.feature.assessment.data.local.AssessmentScheduleDao
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
 */
class AssessmentScheduleRepositoryImpl @Inject constructor(
    private val db: AssessmentDatabase,
    private val scheduleDao: AssessmentScheduleDao,
    private val paperDao: AssessmentPaperDao,
    private val sectionDao: PracticalSectionDao,
    private val questionDao: SectionQuestionDao,
    private val candidateDao: AssessmentCandidateDao,
    private val practicalScoreDao: PracticalScoreDao,
    private val projectScoreDao: ProjectScoreDao,
    private val remoteSource: AssessmentPackageRemoteSource,
) : AssessmentScheduleRepository {

    override suspend fun getSchedules(): List<AssessmentSchedule> = withContext(Dispatchers.IO) {
        val cached = scheduleDao.getAll()
        if (cached.isNotEmpty()) {
            return@withContext cached.map { it.toDomain() }
        }
        // First-launch path: pull from remote, write through, return.
        val fetched = runCatching { remoteSource.fetchSchedules() }.getOrNull().orEmpty()
        if (fetched.isNotEmpty()) {
            scheduleDao.upsertAll(fetched.map { it.toEntity() })
        }
        fetched
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
            val bundle = runCatching { remoteSource.fetchPackage(scheduleId) }.getOrNull()
                ?: return@withContext DownloadAssessmentPackageResult.Error(
                    "Could not download package for $scheduleId.",
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
                        questionDao.clearAll()
                        sectionDao.clearAll()
                        paperDao.clearAll()
                        candidateDao.clearCandidates()
                        // Score wipe is also fair game on a global clear —
                        // the user explicitly asked for everything to go.
                        // Per-schedule clears (below) preserve scores.
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
