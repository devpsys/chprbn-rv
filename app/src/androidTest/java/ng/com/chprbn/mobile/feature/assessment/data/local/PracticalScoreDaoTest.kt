package ng.com.chprbn.mobile.feature.assessment.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PracticalScoreDaoTest {

    private lateinit var db: AssessmentDatabase
    private lateinit var dao: PracticalScoreDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, AssessmentDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.practicalScoreDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun upsertReplacesExistingRow() = runTest {
        dao.upsert(score(score = 5))
        dao.upsert(score(score = 7))

        val row = dao.getOne("s1", "c1", "q1")

        assertNotNull(row)
        assertEquals("second upsert must REPLACE, not append", 7, row!!.score)
    }

    @Test
    fun updateSyncMetadataFlipsStatusAndPreservesScore() = runTest {
        dao.upsert(score(score = 8))

        val rows = dao.updateSyncMetadata(
            scheduleId = "s1",
            candidateId = "c1",
            questionId = "q1",
            syncStatus = SyncStatus.Synced.name,
            syncError = null,
        )

        assertEquals(1, rows)
        val row = dao.getOne("s1", "c1", "q1")!!
        assertEquals(SyncStatus.Synced.name, row.syncStatus)
        assertEquals(8, row.score)
        assertNull(row.syncError)
    }

    @Test
    fun pendingAndFailedFiltersBySyncStatus() = runTest {
        dao.upsert(score(questionId = "q1", status = SyncStatus.Pending))
        dao.upsert(score(questionId = "q2", status = SyncStatus.Synced))
        dao.upsert(score(questionId = "q3", status = SyncStatus.Failed))

        val pending = dao.pendingAndFailed()

        assertEquals(2, pending.size)
        assertEquals(setOf("q1", "q3"), pending.map { it.questionId }.toSet())
    }

    @Test
    fun countByStatusForScheduleMatches() = runTest {
        dao.upsert(score(scheduleId = "s1", questionId = "q1", status = SyncStatus.Pending))
        dao.upsert(score(scheduleId = "s1", questionId = "q2", status = SyncStatus.Pending))
        dao.upsert(score(scheduleId = "s1", questionId = "q3", status = SyncStatus.Synced))
        dao.upsert(score(scheduleId = "s2", questionId = "q1", status = SyncStatus.Pending))

        assertEquals(2, dao.countByStatusForSchedule("s1", SyncStatus.Pending.name))
        assertEquals(1, dao.countByStatusForSchedule("s1", SyncStatus.Synced.name))
        assertEquals(1, dao.countByStatusForSchedule("s2", SyncStatus.Pending.name))
    }

    @Test
    fun deleteForScheduleRemovesOnlyThatSchedule() = runTest {
        dao.upsert(score(scheduleId = "s1", questionId = "q1"))
        dao.upsert(score(scheduleId = "s2", questionId = "q1"))

        val removed = dao.deleteForSchedule("s1")

        assertEquals(1, removed)
        assertNull(dao.getOne("s1", "c1", "q1"))
        assertNotNull(dao.getOne("s2", "c1", "q1"))
    }

    private fun score(
        scheduleId: String = "s1",
        candidateId: String = "c1",
        questionId: String = "q1",
        score: Int = 5,
        status: SyncStatus = SyncStatus.Pending,
    ) = PracticalScoreEntity(
        scheduleId = scheduleId,
        candidateId = candidateId,
        questionId = questionId,
        score = score,
        scoredAt = 1_700_000_000_000L,
        syncStatus = status.name,
    )
}
