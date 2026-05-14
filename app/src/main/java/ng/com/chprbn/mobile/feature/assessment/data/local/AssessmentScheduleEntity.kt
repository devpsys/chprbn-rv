package ng.com.chprbn.mobile.feature.assessment.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One row per scheduled assessment the officer is assigned to grade
 * (e.g. `"PE-2024"`). Comes from a separate endpoint than the package
 * download — schedules are listed first, then the officer picks one and
 * downloads its package on demand.
 *
 * `paperKind` and `syncStatus` are stored as the enum `name` string (the
 * verification feature's convention; no TypeConverters).
 *
 * NB: the row's `syncStatus` column is NOT the row's own sync state —
 * schedules themselves are server-of-truth and never uploaded. It is a
 * **cached derived value** of "has this schedule's score writes all
 * uploaded?", computed by the repository on read and persisted here so the
 * list screen renders without a per-row aggregation query.
 */
@Entity(tableName = "assessment_schedules")
data class AssessmentScheduleEntity(
    @PrimaryKey val id: String,
    val title: String,
    val date: Long,
    val paperKind: String,
    val centerId: String,
    val syncStatus: String,
)
