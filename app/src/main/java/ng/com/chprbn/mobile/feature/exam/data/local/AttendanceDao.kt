package ng.com.chprbn.mobile.feature.exam.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
@JvmSuppressWildcards
interface AttendanceDao {

    @Query(
        """
        SELECT * FROM attendance
        WHERE paperId = :paperId AND candidateId = :candidateId
        """,
    )
    suspend fun getOne(paperId: String, candidateId: String): AttendanceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(attendance: AttendanceEntity): Long

    @Query(
        """
        UPDATE attendance
        SET syncStatus = :syncStatus,
            syncError = :syncError,
            lastSyncAttemptAt = :lastSyncAttemptAt
        WHERE paperId = :paperId AND candidateId = :candidateId
        """,
    )
    suspend fun updateSyncMetadata(
        paperId: String,
        candidateId: String,
        syncStatus: String,
        syncError: String?,
        lastSyncAttemptAt: Long?,
    ): Int

    @Query("SELECT * FROM attendance WHERE syncStatus IN ('Pending', 'Failed') LIMIT :limit")
    suspend fun pendingAndFailed(limit: Int = 50): List<AttendanceEntity>

    /**
     * Every row for [paperId], regardless of current status. A candidate who
     * was signed in and later toggled to SignedOut still counts here — the
     * row's mere existence means they were checked in at some point today;
     * "checked in" is a day-scoped fact, not the candidate's live presence.
     */
    @Query("SELECT COUNT(*) FROM attendance WHERE paperId = :paperId")
    suspend fun countMarkedForPaper(paperId: String): Int

    @Query("SELECT COUNT(*) FROM attendance WHERE syncStatus = :syncStatus")
    suspend fun countBySyncStatus(syncStatus: String): Int

    @Query("SELECT COUNT(*) FROM attendance")
    suspend fun totalCount(): Int

    @Query("SELECT MAX(markedAt) FROM attendance")
    suspend fun mostRecentMarkedAt(): Long?

    @Query("DELETE FROM attendance")
    suspend fun clearAll(): Int

    /**
     * Live projection for the Cached Records screen — joins attendance
     * with candidate + paper reference data so the row is displayable
     * without a second DAO hop. `COALESCE`s protect against a dossier
     * refresh dropping a candidate/paper mid-day: the row still shows
     * (with the id as the fallback label) rather than disappearing
     * from the officer's queue silently. [statuses] is caller-side
     * filtered so the same query serves both the Pending tab
     * (`['Pending']`) and the Failed tab (`['Failed', 'Abandoned']`).
     */
    @Query(
        """
        SELECT
            'Attendance'                                       AS recordType,
            a.candidateId                                       AS candidateId,
            COALESCE(c.fullName, 'Candidate #' || a.candidateId) AS candidateName,
            COALESCE(c.examNumber, '')                          AS examNumber,
            a.paperId                                           AS paperId,
            COALESCE(p.title, '')                               AS paperTitle,
            a.syncStatus                                        AS syncStatus,
            a.syncError                                         AS syncError,
            a.markedAt                                          AS capturedAt,
            a.lastSyncAttemptAt                                 AS lastAttemptAt
        FROM attendance a
        LEFT JOIN candidates c ON c.id = a.candidateId
        LEFT JOIN papers p ON p.id = a.paperId
        WHERE a.syncStatus IN (:statuses)
        ORDER BY a.markedAt DESC
        """,
    )
    fun observeCachedRecords(statuses: List<String>): Flow<List<CachedRecordProjection>>
}
