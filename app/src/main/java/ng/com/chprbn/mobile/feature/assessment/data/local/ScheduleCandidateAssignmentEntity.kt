package ng.com.chprbn.mobile.feature.assessment.data.local

import androidx.room.Entity
import androidx.room.Index

/**
 * Many-to-many join between schedules and candidates. Composite PK; index
 * on `scheduleId` keeps the per-schedule candidate query fast and on
 * `candidateId` keeps the "which schedules is this candidate in" reverse
 * lookup cheap (we don't use it today but it's nearly free).
 */
@Entity(
    tableName = "schedule_candidate_assignments",
    primaryKeys = ["scheduleId", "candidateId"],
    indices = [Index("scheduleId"), Index("candidateId")],
)
data class ScheduleCandidateAssignmentEntity(
    val scheduleId: String,
    val candidateId: String,
)
