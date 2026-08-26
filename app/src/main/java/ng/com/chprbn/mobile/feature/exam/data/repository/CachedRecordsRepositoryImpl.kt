package ng.com.chprbn.mobile.feature.exam.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.core.network.stripHostsAndUrls
import ng.com.chprbn.mobile.feature.assessment.data.local.PracticalScoreDao
import ng.com.chprbn.mobile.feature.assessment.data.local.PracticalScoreEntity
import ng.com.chprbn.mobile.feature.assessment.data.local.ProjectScoreDao
import ng.com.chprbn.mobile.feature.assessment.data.local.ProjectScoreEntity
import ng.com.chprbn.mobile.feature.exam.data.local.AttendanceDao
import ng.com.chprbn.mobile.feature.exam.data.local.CachedRecordProjection
import ng.com.chprbn.mobile.feature.exam.data.local.CandidateDao
import ng.com.chprbn.mobile.feature.exam.data.local.PaperDao
import ng.com.chprbn.mobile.feature.exam.data.local.RemarkDao
import ng.com.chprbn.mobile.feature.exam.domain.model.CachedRecordEntry
import ng.com.chprbn.mobile.feature.exam.domain.model.RecordType
import ng.com.chprbn.mobile.feature.exam.domain.repository.CachedRecordsRepository
import javax.inject.Inject

/**
 * Merges four sync-tracked tables into one live list per tab.
 *
 * Attendance + remark rows live in `exam.db` alongside the reference
 * `candidates` and `papers` tables, so those two DAOs do the label
 * join in SQL and hand us a fully-formed [CachedRecordProjection].
 *
 * Practical + project score rows live in `assessment.db` — Room can't
 * cross-DB join, so those flows deliver raw entities that this repo
 * hydrates by batch-looking-up candidate + paper labels from the exam
 * DAOs. Batch size is small in practice (dozens of pending rows), so
 * two `getByIds` hops per emission is well inside the budget.
 *
 * Every emission produces a domain list that's:
 *   - sorted by `capturedAt DESC` (freshest work first);
 *   - resilient to a mid-day dossier refresh dropping a
 *     candidate/paper — the row still shows with a `"Candidate #<id>"`
 *     fallback label rather than silently vanishing.
 */
class CachedRecordsRepositoryImpl @Inject constructor(
    private val attendanceDao: AttendanceDao,
    private val remarkDao: RemarkDao,
    private val practicalScoreDao: PracticalScoreDao,
    private val projectScoreDao: ProjectScoreDao,
    private val candidateDao: CandidateDao,
    private val paperDao: PaperDao,
) : CachedRecordsRepository {

    override fun observePending(): Flow<List<CachedRecordEntry>> =
        observeByStatuses(PENDING_STATUSES)

    override fun observeFailed(): Flow<List<CachedRecordEntry>> =
        observeByStatuses(FAILED_STATUSES)

    private fun observeByStatuses(statuses: List<String>): Flow<List<CachedRecordEntry>> =
        combine(
            attendanceDao.observeCachedRecords(statuses),
            remarkDao.observeCachedRecords(statuses),
            practicalScoreDao.observeByStatus(statuses),
            projectScoreDao.observeByStatus(statuses),
        ) { attendance, remarks, practicals, projects ->
            val examSourced = attendance.map { it.toDomain(RecordType.Attendance) } +
                remarks.map { it.toDomain(RecordType.Remark) }
            val assessmentSourced = hydrateAssessmentRows(practicals, projects)
            (examSourced + assessmentSourced).sortedByDescending { it.capturedAt }
        }

    /**
     * Assessment scores don't share a DB with `candidates` / `papers`,
     * so we batch-fetch labels once per emission and stitch them in.
     * Missing labels degrade to the `Candidate #<id>` / empty-title
     * fallback so an in-flight dossier refresh can't hide a stuck row.
     */
    private suspend fun hydrateAssessmentRows(
        practicals: List<PracticalScoreEntity>,
        projects: List<ProjectScoreEntity>,
    ): List<CachedRecordEntry> {
        if (practicals.isEmpty() && projects.isEmpty()) return emptyList()

        val candidateIds = (practicals.map { it.candidateId } + projects.map { it.candidateId })
            .distinct()
        val paperIds = (practicals.map { it.scheduleId } + projects.map { it.scheduleId })
            .distinct()

        val candidatesById = candidateDao.getByIds(candidateIds).associateBy { it.id }
        val papersById = paperDao.getByIds(paperIds).associateBy { it.id }

        val practicalEntries = practicals.map { row ->
            val candidate = candidatesById[row.candidateId]
            val paper = papersById[row.scheduleId]
            CachedRecordEntry(
                id = "practical|${row.scheduleId}|${row.candidateId}|${row.questionId}",
                candidateId = row.candidateId,
                candidateName = candidate?.fullName ?: "Candidate #${row.candidateId}",
                examNumber = candidate?.examNumber.orEmpty(),
                paperTitle = paper?.title.orEmpty(),
                recordType = RecordType.PracticalScore,
                syncStatus = row.syncStatus.toSyncStatus(),
                syncError = row.syncError.sanitiseForDisplay(),
                capturedAt = row.scoredAt,
                // Practical/project score entities don't carry a
                // last-attempt column; the repo surfaces null so the
                // UI hides the "last attempted" line for these rows.
                lastAttemptAt = null,
            )
        }
        val projectEntries = projects.map { row ->
            val candidate = candidatesById[row.candidateId]
            val paper = papersById[row.scheduleId]
            CachedRecordEntry(
                id = "project|${row.scheduleId}|${row.candidateId}",
                candidateId = row.candidateId,
                candidateName = candidate?.fullName ?: "Candidate #${row.candidateId}",
                examNumber = candidate?.examNumber.orEmpty(),
                paperTitle = paper?.title.orEmpty(),
                recordType = RecordType.ProjectScore,
                syncStatus = row.syncStatus.toSyncStatus(),
                syncError = row.syncError.sanitiseForDisplay(),
                capturedAt = row.scoredAt,
                lastAttemptAt = null,
            )
        }
        return practicalEntries + projectEntries
    }

    private fun CachedRecordProjection.toDomain(recordType: RecordType): CachedRecordEntry {
        val prefix = when (recordType) {
            RecordType.Attendance -> "attendance"
            RecordType.Remark -> "remark"
            else -> error("toDomain(projection) is exam-side only")
        }
        return CachedRecordEntry(
            id = "$prefix|$paperId|$candidateId",
            candidateId = candidateId,
            candidateName = candidateName,
            examNumber = examNumber,
            paperTitle = paperTitle,
            recordType = recordType,
            syncStatus = syncStatus.toSyncStatus(),
            syncError = syncError.sanitiseForDisplay(),
            capturedAt = capturedAt,
            lastAttemptAt = lastAttemptAt,
        )
    }

    /** Defensive: a corrupted stored string degrades to Pending. */
    private fun String.toSyncStatus(): SyncStatus =
        runCatching { SyncStatus.valueOf(this) }.getOrDefault(SyncStatus.Pending)

    /**
     * Last-mile scrub of any stored `syncError` before it reaches the
     * screen. The sync handlers already call `toUserFacingMessage` at
     * write time, but this defends against two paths that would
     * otherwise leak the backend host into the Failed tab:
     *
     * 1. **Historical rows** — anything captured before the sanitiser
     *    landed still has the raw hostname on disk until the next sync
     *    attempt overwrites the column. This scrub covers those in
     *    place, no migration or rewrite needed.
     * 2. **New write paths we haven't audited yet** — any future
     *    handler that forgets to route through `toUserFacingMessage`
     *    gets scrubbed here anyway, so the domain never surfaces even
     *    if the write-side sanitiser is bypassed.
     */
    private fun String?.sanitiseForDisplay(): String? =
        this?.stripHostsAndUrls()

    private companion object {
        val PENDING_STATUSES = listOf(SyncStatus.Pending.name)
        val FAILED_STATUSES = listOf(SyncStatus.Failed.name, SyncStatus.Abandoned.name)
    }
}
