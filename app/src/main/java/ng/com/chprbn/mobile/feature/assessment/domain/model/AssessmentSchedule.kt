package ng.com.chprbn.mobile.feature.assessment.domain.model

import ng.com.chprbn.mobile.core.domain.model.PaperKind
import ng.com.chprbn.mobile.core.domain.model.SyncStatus

/**
 * One row on the Examination Schedules screen. The mobile examiner is assigned
 * a small set of these per session (e.g. "PE-2024 / Practical Exam").
 *
 * [syncStatus] is **derived** from the underlying score tables — a schedule is
 * `Pending` when any of its `PracticalScore` / `ProjectScore` rows still have
 * `syncStatus != Synced`, `Failed` when at least one row is `Failed`, else
 * `Synced`. Computed in the repository, not stored.
 */
data class AssessmentSchedule(
    val id: String,
    val title: String,
    val date: Long,
    val paperKind: PaperKind,
    val centerId: String,
    val syncStatus: SyncStatus,
)
