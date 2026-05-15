package ng.com.chprbn.mobile.feature.exam.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.domain.model.PaperKind
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.feature.exam.data.local.AttendanceEntity
import ng.com.chprbn.mobile.feature.exam.data.local.CandidateEntity
import ng.com.chprbn.mobile.feature.exam.data.local.CenterEntity
import ng.com.chprbn.mobile.feature.exam.data.local.ExamDatabase
import ng.com.chprbn.mobile.feature.exam.data.local.PaperCandidateAssignmentEntity
import ng.com.chprbn.mobile.feature.exam.data.local.PaperEntity
import ng.com.chprbn.mobile.feature.exam.data.local.RemarkEntity
import ng.com.chprbn.mobile.feature.exam.domain.model.AttendanceStatus
import ng.com.chprbn.mobile.feature.exam.domain.model.RemarkSeverity
import ng.com.chprbn.mobile.feature.exam.domain.model.SaveResult
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Real Room-backed test of [ExamStatisticsRepositoryImpl]. Uses an
 * in-memory database because `clearLocalCache` runs through
 * `db.withTransaction { … }`, which is awkward to mock.
 */
@RunWith(AndroidJUnit4::class)
class ExamStatisticsRepositoryImplTest {

    private lateinit var db: ExamDatabase
    private lateinit var repository: ExamStatisticsRepositoryImpl

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, ExamDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = ExamStatisticsRepositoryImpl(
            db = db,
            centerDao = db.centerDao(),
            paperDao = db.paperDao(),
            candidateDao = db.candidateDao(),
            attendanceDao = db.attendanceDao(),
            remarkDao = db.remarkDao(),
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun getStatisticsAggregatesAcrossSyncBuckets() = runTest {
        db.attendanceDao().upsert(attendance("c1", SyncStatus.Synced, markedAt = 100L))
        db.attendanceDao().upsert(attendance("c2", SyncStatus.Pending, markedAt = 300L))
        db.attendanceDao().upsert(attendance("c3", SyncStatus.Failed, markedAt = 200L))

        val stats = repository.getStatistics()

        assertEquals(3, stats.attendanceCaptured)
        assertEquals(1, stats.syncedCount)
        assertEquals(1, stats.pendingCount)
        assertEquals(1, stats.failedCount)
        assertEquals(300L, stats.lastUpdatedAt)
    }

    @Test
    fun getStatisticsReturnsZeroesWhenTableEmpty() = runTest {
        val stats = repository.getStatistics()

        assertEquals(0, stats.attendanceCaptured)
        assertEquals(0, stats.syncedCount)
        assertEquals(0, stats.pendingCount)
        assertEquals(0, stats.failedCount)
        assertNull(stats.lastUpdatedAt)
    }

    @Test
    fun clearLocalCacheWipesEveryTable() = runTest {
        db.centerDao().upsert(center())
        db.paperDao().upsertAll(listOf(paper()))
        db.candidateDao().upsertAll(listOf(candidate()))
        db.candidateDao().upsertAssignments(
            listOf(PaperCandidateAssignmentEntity(paperId = "p1", candidateId = "c1")),
        )
        db.attendanceDao().upsert(attendance("c1"))
        db.remarkDao().upsert(remark())

        val result = repository.clearLocalCache()

        assertTrue(result is SaveResult.Success)
        assertEquals(0, db.attendanceDao().totalCount())
        assertNull(db.paperDao().getById("p1"))
        assertNull(db.centerDao().getById("c1"))
    }

    private fun center() = CenterEntity(
        id = "c1",
        name = "Lagos Centre",
        code = "LAG",
        location = "Lagos",
    )

    private fun paper() = PaperEntity(
        id = "p1",
        centerId = "c1",
        title = "Paper 1",
        subtitle = "Practical",
        paperKind = PaperKind.Practical.name,
        startAt = 0L,
        endAt = 1L,
        hall = "Hall A",
        totalCandidates = 30,
    )

    private fun candidate() = CandidateEntity(
        id = "c1",
        examNumber = "EX1",
        fullName = "Ada",
    )

    private fun attendance(
        candidateId: String,
        syncStatus: SyncStatus = SyncStatus.Pending,
        markedAt: Long = 1_700_000_000_000L,
    ) = AttendanceEntity(
        paperId = "p1",
        candidateId = candidateId,
        status = AttendanceStatus.SignedIn.name,
        markedAt = markedAt,
        syncStatus = syncStatus.name,
    )

    private fun remark() = RemarkEntity(
        id = "r1",
        candidateId = "c1",
        paperId = "p1",
        body = "Late arrival",
        severity = RemarkSeverity.Info.name,
        createdAt = 0L,
        syncStatus = SyncStatus.Pending.name,
    )
}
