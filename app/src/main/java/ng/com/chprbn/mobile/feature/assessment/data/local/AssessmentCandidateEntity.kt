package ng.com.chprbn.mobile.feature.assessment.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Cross-feature candidate identity — same shape as the exam-side roster
 * row. Stored once per candidate regardless of how many schedules they're
 * assigned to; `schedule_candidate_assignments` carries the join.
 *
 * Indexed on `examNumber` because that's what the QR scan + manual-entry
 * paths resolve against.
 */
@Entity(
    tableName = "assessment_candidates",
    indices = [Index("examNumber")],
)
data class AssessmentCandidateEntity(
    @PrimaryKey val id: String,
    val examNumber: String,
    val fullName: String,
    val photoUrl: String? = null,
)
