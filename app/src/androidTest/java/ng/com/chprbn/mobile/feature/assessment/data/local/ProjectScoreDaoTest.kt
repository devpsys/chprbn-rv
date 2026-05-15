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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProjectScoreDaoTest {

    private lateinit var db: AssessmentDatabase
    private lateinit var dao: ProjectScoreDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, AssessmentDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.projectScoreDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun upsertReplacesOnCompositePrimaryKey() = runTest {
        dao.upsert(score(scheduleId = "s1", candidateId = "c1", value = 7.0))
        dao.upsert(score(scheduleId = "s1", candidateId = "c1", value = 9.5))

        assertEquals(9.5, dao.getOne("s1", "c1")?.score!!, 0.0001)
    }

    @Test
    fun getOneIsolatesByCompositeKey() = runTest {
        dao.upsert(score(scheduleId = "s1", candidateId = "c1", value = 7.0))
        dao.upsert(score(scheduleId = "s1", candidateId = "c2", value = 8.0))
        dao.upsert(score(scheduleId = "s2", candidateId = "c1", value = 9.0))

        assertEquals(7.0, dao.getOne("s1", "c1")?.score!!, 0.0001)
        assertEquals(8.0, dao.getOne("s1", "c2")?.score!!, 0.0001)
        assertEquals(9.0, dao.getOne("s2", "c1")?.score!!, 0.0001)
    }

    @Test
    fun getOneReturnsNullWhenAbsent() = runTest {
        assertNull(dao.getOne("s1", "missing"))
    }

    @Test
    fun updateSyncMetadataFlipsStatusAndError() = runTest {
        dao.upsert(
            score(scheduleId = "s1", candidateId = "c1", syncStatus = SyncStatus.Pending.name),
        )

        val updated = dao.updateSyncMetadata("s1", "c1", SyncStatus.Failed.name, "boom")

        assertEquals(1, updated)
        val row = dao.getOne("s1", "c1")!!
        assertEquals(SyncStatus.Failed.name, row.syncStatus)
        assertEquals("boom", row.syncError)
    }

    @Test
    fun pendingAndFailedReturnsBothStatusesBoundedByLimit() = runTest {
        dao.upsert(score("s1", "c1", syncStatus = SyncStatus.Pending.name))
        dao.upsert(score("s1", "c2", syncStatus = SyncStatus.Failed.name))
        dao.upsert(score("s1", "c3", syncStatus = SyncStatus.Synced.name))

        val rows = dao.pendingAndFailed(limit = 50)

        assertEquals(2, rows.size)
        assertTrue(rows.all { it.syncStatus != SyncStatus.Synced.name })
    }

    @Test
    fun pendingAndFailedRespectsLimit() = runTest {
        repeat(5) { i ->
            dao.upsert(score("s1", "c$i", syncStatus = SyncStatus.Pending.name))
        }

        assertEquals(3, dao.pendingAndFailed(limit = 3).size)
    }

    @Test
    fun countByStatusForScheduleScopesToThatSchedule() = runTest {
        dao.upsert(score("s1", "c1", syncStatus = SyncStatus.Pending.name))
        dao.upsert(score("s1", "c2", syncStatus = SyncStatus.Pending.name))
        dao.upsert(score("s2", "c1", syncStatus = SyncStatus.Pending.name))

        assertEquals(2, dao.countByStatusForSchedule("s1", SyncStatus.Pending.name))
        assertEquals(1, dao.countByStatusForSchedule("s2", SyncStatus.Pending.name))
        assertEquals(0, dao.countByStatusForSchedule("s1", SyncStatus.Synced.name))
    }

    @Test
    fun deleteForScheduleLeavesOtherSchedulesIntact() = runTest {
        dao.upsert(score("s1", "c1"))
        dao.upsert(score("s1", "c2"))
        dao.upsert(score("s2", "c1"))

        val deleted = dao.deleteForSchedule("s1")

        assertEquals(2, deleted)
        assertNull(dao.getOne("s1", "c1"))
        assertNull(dao.getOne("s1", "c2"))
        assertNotNull(dao.getOne("s2", "c1"))
    }

    private fun score(
        scheduleId: String,
        candidateId: String,
        value: Double = 5.0,
        syncStatus: String = SyncStatus.Pending.name,
    ) = ProjectScoreEntity(
        scheduleId = scheduleId,
        candidateId = candidateId,
        score = value,
        maxScore = 10,
        scoredAt = 1_700_000_000_000L,
        syncStatus = syncStatus,
        syncError = null,
    )
}
