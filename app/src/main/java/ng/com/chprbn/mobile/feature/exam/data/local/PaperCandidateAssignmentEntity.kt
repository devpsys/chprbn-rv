package ng.com.chprbn.mobile.feature.exam.data.local

import androidx.room.ColumnInfo
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
    /**
     * Added in schema v3 — required to push attendance
     * (`docs/mobile-api-guide.html` §5). Empty string for rows persisted
     * before this column existed (safe: this table is fully wiped and
     * rebuilt on every dossier download — see `ExamSyncRepositoryImpl.downloadDossier`
     * — so a stale-blank row self-heals on the next download; `AttendanceSyncHandler`
     * treats a blank value as "can't push yet" rather than sending it).
     */
    @ColumnInfo(defaultValue = "")
    val scheduledCandidateId: String = "",
    @ColumnInfo(defaultValue = "")
    val scheduleId: String = "",
)
