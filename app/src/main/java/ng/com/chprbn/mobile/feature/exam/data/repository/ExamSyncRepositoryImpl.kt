package ng.com.chprbn.mobile.feature.exam.data.repository

import android.util.Log
import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ng.com.chprbn.mobile.core.domain.model.SyncBatchResult
import ng.com.chprbn.mobile.core.sync.SyncBatchRunner
import ng.com.chprbn.mobile.feature.assessment.data.local.AssessmentDatabase
import ng.com.chprbn.mobile.feature.assessment.data.local.PracticalSectionDao
import ng.com.chprbn.mobile.feature.assessment.data.local.SectionQuestionDao
import ng.com.chprbn.mobile.feature.assessment.data.mappers.toEntity as toAssessmentEntity
import ng.com.chprbn.mobile.feature.exam.data.local.CandidateDao
import ng.com.chprbn.mobile.feature.exam.data.local.CenterDao
import ng.com.chprbn.mobile.feature.exam.data.local.ExamDatabase
import ng.com.chprbn.mobile.feature.exam.data.local.PaperCandidateAssignmentEntity
import ng.com.chprbn.mobile.feature.exam.data.local.PaperDao
import ng.com.chprbn.mobile.feature.exam.data.mappers.toEntity
import ng.com.chprbn.mobile.feature.exam.data.mappers.toExamCandidateEntity
import ng.com.chprbn.mobile.feature.exam.data.source.ExamDossierRemoteSource
import ng.com.chprbn.mobile.feature.exam.domain.model.DownloadDossierResult
import ng.com.chprbn.mobile.feature.exam.domain.repository.ExamSyncRepository
import javax.inject.Inject

/**
 * Owns the day-dossier download and the user-initiated "Sync Now".
 *
 * The dossier download runs inside `db.withTransaction { … }` so a
 * partial write can't leave the local cache in an inconsistent state.
 *
 * **Merge semantics — additive roster, authoritative reference data.**
 * - **Candidates** and **paper↔candidate assignments** are additive: a
 *   re-download only adds rows that weren't already local. A candidate
 *   already known is skipped (`OnConflictStrategy.IGNORE`), so a fresher
 *   photo/name coming back down the wire never overwrites what the
 *   officer already trusts. This is the "roster grew mid-day, don't
 *   disturb what I've already touched" case.
 * - **Centre**, **papers**, **practical sections**, and **section
 *   questions** stay authoritative — the pre-download wipe on those
 *   tables is preserved so a server-side rename / edit / cancellation
 *   is captured.
 * - **Captured records** — `attendance`, `remarks`, `practical_scores`,
 *   `project_scores` — are never touched here, at all. The transaction
 *   only writes reference tables.
 *
 * Practical-assessment reference data (sections + nested questions)
 * arrives on the dossier and is persisted here into `assessment.db` in
 * a second, sequential transaction — the two databases can't share
 * one. If the exam-side write succeeded but the assessment-side write
 * threw, the officer sees an error and the next re-download replays
 * both sides idempotently.
 *
 * `syncPending` delegates to the cross-feature [SyncBatchRunner] —
 * running it here flushes assessment-side rows too, which is fine: the
 * user wouldn't want two separate Sync Now buttons.
 */
class ExamSyncRepositoryImpl @Inject constructor(
    private val db: ExamDatabase,
    private val assessmentDb: AssessmentDatabase,
    private val centerDao: CenterDao,
    private val paperDao: PaperDao,
    private val candidateDao: CandidateDao,
    private val practicalSectionDao: PracticalSectionDao,
    private val sectionQuestionDao: SectionQuestionDao,
    private val remoteSource: ExamDossierRemoteSource,
    private val runner: SyncBatchRunner,
) : ExamSyncRepository {

    override suspend fun downloadDossier(): DownloadDossierResult = withContext(Dispatchers.IO) {
        // remoteSource.fetchDossier() throws on transport/envelope error
        // (E1 fix) so a real backend problem doesn't silently degrade to a
        // Fake bundle in release. Surface the caught message rather than a
        // generic "could not download" so the officer sees "HTTP 500" or
        // "Network error" — actionable, not misleading.
        val bundle = runCatching { remoteSource.fetchDossier() }.fold(
            onSuccess = { it },
            onFailure = { t ->
                Log.e(TAG, "Failed to fetch dossier from the remote source", t)
                return@withContext DownloadDossierResult.Error(
                    t.message ?: "Could not download dossier.",
                )
            },
        ) ?: return@withContext DownloadDossierResult.Error(
            "The server returned no dossier for today.",
        )

        try {
            var newCandidatesCount = 0
            db.withTransaction {
                // Centre + papers stay authoritative: a renamed centre or a
                // cancelled paper must actually disappear from the local
                // cache, so wipe then upsert.
                paperDao.clearAll()
                centerDao.clearAll()
                centerDao.upsert(bundle.center.toEntity())
                paperDao.upsertAll(bundle.papers.map { it.toEntity() })

                // Additive candidate merge — `insertMissing` uses IGNORE
                // on the PK so an already-known candidate is skipped
                // wholesale (photo/name from the wire never clobbers a
                // local row). Returned rowIds are -1 for skipped rows;
                // count the non-negatives to report "N new candidates."
                val inserted = candidateDao.insertMissing(
                    bundle.candidates.map { it.toExamCandidateEntity() },
                )
                newCandidatesCount = inserted.count { it >= 0L }

                // Assignments upsert on (paperId, candidateId) — same-PK
                // rows are replaced so a corrected scheduledCandidateId
                // is captured, new pairings are inserted. Pre-existing
                // assignments not mentioned in the bundle stay put —
                // matches the additive philosophy for the roster.
                candidateDao.upsertAssignments(
                    bundle.assignments.map {
                        PaperCandidateAssignmentEntity(
                            paperId = it.paperId,
                            candidateId = it.candidateId,
                            scheduledCandidateId = it.scheduledCandidateId,
                            scheduleId = it.scheduleId,
                        )
                    },
                )
            }
            // Separate transaction — cross-DB atomicity isn't available,
            // and practical reference data is authoritative (question
            // edits / section renames must actually land).
            assessmentDb.withTransaction {
                bundle.practicalSections.map { it.scheduleId }.distinct().forEach { scheduleId ->
                    // Delete questions first (dependent on section rows),
                    // then the sections themselves.
                    sectionQuestionDao.deleteByScheduleId(scheduleId)
                    practicalSectionDao.deleteByScheduleId(scheduleId)
                }
                practicalSectionDao.upsertAll(bundle.practicalSections.map { it.toAssessmentEntity() })
                sectionQuestionDao.upsertAll(bundle.practicalQuestions.map { it.toAssessmentEntity() })
            }
            DownloadDossierResult.Success(
                papersCount = bundle.papers.size,
                candidatesCount = bundle.candidates.size,
                newCandidatesCount = newCandidatesCount,
                skippedCandidatesCount = bundle.candidates.size - newCandidatesCount,
            )
        } catch (t: Throwable) {
            // Full stack trace only reaches Logcat — the officer only ever
            // sees `t.message` (often a terse SQLite/Room string), so this
            // is the only way to pin down which row/column/entity a
            // persistence failure actually came from.
            Log.e(
                TAG,
                "Failed to persist downloaded dossier (center=${bundle.center.id}, " +
                    "papers=${bundle.papers.size}, candidates=${bundle.candidates.size}, " +
                    "assignments=${bundle.assignments.size}, " +
                    "sections=${bundle.practicalSections.size}, " +
                    "questions=${bundle.practicalQuestions.size})",
                t,
            )
            DownloadDossierResult.Error(t.message ?: "Could not persist downloaded dossier.")
        }
    }

    override suspend fun syncPending(): SyncBatchResult = withContext(Dispatchers.IO) {
        runner.runBatch()
    }

    private companion object {
        const val TAG = "ExamDossier"
    }
}
