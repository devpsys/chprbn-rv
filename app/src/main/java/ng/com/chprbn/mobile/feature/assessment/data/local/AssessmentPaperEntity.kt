package ng.com.chprbn.mobile.feature.assessment.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 1:1 with a schedule for v1 (one paper per schedule). The schedule's id is
 * also the paper's primary key — keeps the join trivial.
 *
 * Facility and hall are flattened into four columns rather than a separate
 * entity; both are read-only reference data and never queried independently
 * of the paper.
 */
@Entity(tableName = "assessment_papers")
data class AssessmentPaperEntity(
    @PrimaryKey val scheduleId: String,
    val title: String,
    val statusLabel: String,
    val facilityName: String,
    val facilityAddress: String,
    val hallName: String,
    val hallAddress: String,
    val heroImageUrl: String? = null,
)
