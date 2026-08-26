package ng.com.chprbn.mobile.feature.exam.domain.model

import ng.com.chprbn.mobile.core.domain.model.SyncStatus

/**
 * A single locally-captured record awaiting upload or blocked on a
 * server-side error. Merges the four sync-tracked tables
 * (`attendance`, `remarks`, `practical_scores`, `project_scores`) into
 * one domain shape so the Cached Records screen can show them in a
 * single, sortable list without the presentation layer needing to
 * know about per-feature tables.
 *
 * [id] is a synthetic display id — never a database PK — assembled by
 * the repo so each entry is unique across all four sources for the
 * LazyColumn's `key` and for the "copy error" action. See the
 * repository's `toEntry` mappers for the exact format.
 *
 * [candidateName] and [paperTitle] may fall back to `"Candidate #<id>"`
 * or `""` when the referenced candidate/paper has been dropped by a
 * dossier refresh mid-day — we never surface a bare id without a label
 * because that reads as broken to an officer.
 */
data class CachedRecordEntry(
    val id: String,
    val candidateId: String,
    val candidateName: String,
    val examNumber: String,
    val paperTitle: String,
    val recordType: RecordType,
    val syncStatus: SyncStatus,
    /** Sanitised via `core.network.UserFacingError` before it lands here. */
    val syncError: String?,
    val capturedAt: Long,
    val lastAttemptAt: Long?,
)

/**
 * Which of the four sync-tracked tables this row came from. Used for
 * filter chips on the Cached Records screen and for the row's chip
 * badge (Attendance / Remark / Practical / Project).
 */
enum class RecordType {
    Attendance,
    Remark,
    PracticalScore,
    ProjectScore,
}
