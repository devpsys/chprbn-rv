package ng.com.chprbn.mobile.feature.exam.data.repository

import android.util.Log
import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ng.com.chprbn.mobile.core.domain.model.SyncBatchResult
import ng.com.chprbn.mobile.core.sync.SyncBatchRunner
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
 * Crucially, it never touches `attendance` or `remarks` — pending writes
 * survive a re-download (the explicit UX contract behind the
 * download-warning prompt).
 *
 * `syncPending` delegates to the cross-feature [SyncBatchRunner] —
 * running it here flushes assessment-side rows too, which is fine: the
 * user wouldn't want two separate Sync Now buttons.
 */
class ExamSyncRepositoryImpl @Inject constructor(
    private val db: ExamDatabase,
    private val centerDao: CenterDao,
    private val paperDao: PaperDao,
    private val candidateDao: CandidateDao,
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
            db.withTransaction {
                // Wipe stale reference rows in FK-respecting order.
                // Scores/attendance/remarks are deliberately untouched.
                candidateDao.clearAssignments()
                candidateDao.clearCandidates()
                paperDao.clearAll()
                centerDao.clearAll()

                centerDao.upsert(bundle.center.toEntity())
                paperDao.upsertAll(bundle.papers.map { it.toEntity() })
                candidateDao.upsertAll(bundle.candidates.map { it.toExamCandidateEntity() })
                candidateDao.upsertAssignments(
                    bundle.assignments.map {
                        PaperCandidateAssignmentEntity(
                            paperId = it.paperId,
                            candidateId = it.candidateId,
                        )
                    },
                )
            }
            DownloadDossierResult.Success(
                papersCount = bundle.papers.size,
                candidatesCount = bundle.candidates.size,
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
                    "assignments=${bundle.assignments.size})",
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
