package ng.com.chprbn.mobile.feature.exam.data.local

import androidx.room.Entity
import androidx.room.Index

/**
 * Many-to-many join between papers and candidates. Composite PK;
 * indexes on each leg keep the per-paper roster query and the
 * reverse "which papers is this candidate in" lookup cheap.
 */
@Entity(
    tableName = "paper_candidate_assignments",
    primaryKeys = ["paperId", "candidateId"],
    indices = [Index("paperId"), Index("candidateId")],
)
data class PaperCandidateAssignmentEntity(
    val paperId: String,
    val candidateId: String,
)
