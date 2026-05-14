package ng.com.chprbn.mobile.feature.exam.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Exam-side candidate roster row. Mirrors the cross-feature
 * `core.domain.model.Candidate` shape. The assessment feature has its own
 * `AssessmentCandidateEntity` in its own database — feature DBs each
 * store a local copy of the same canonical candidate identity.
 *
 * `examNumber` is indexed because the QR-scan path resolves against it.
 */
@Entity(
    tableName = "candidates",
    indices = [Index("examNumber")],
)
data class CandidateEntity(
    @PrimaryKey val id: String,
    val examNumber: String,
    val fullName: String,
    val photoUrl: String? = null,
)
