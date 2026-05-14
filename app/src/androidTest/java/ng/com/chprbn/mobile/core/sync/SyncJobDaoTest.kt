package ng.com.chprbn.mobile.core.sync

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
class SyncJobDaoTest {

    private lateinit var db: SyncDatabase
    private lateinit var dao: SyncJobDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, SyncDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.syncJobDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun enqueueThenPendingAndFailedReturnsRow() = runTest {
        dao.enqueue(jobAt(now = 1L, type = SyncEntityType.Attendance, key = "p1/c1"))

        val pending = dao.pendingAndFailed()

        assertEquals(1, pending.size)
        assertEquals("p1/c1", pending.single().entityKey)
        assertEquals(SyncStatus.Pending.name, pending.single().status)
    }

    @Test
    fun enqueueIsUpsertOnEntityTypeAndKey() = runTest {
        dao.enqueue(jobAt(now = 1L, type = SyncEntityType.Attendance, key = "p1/c1"))
        dao.enqueue(jobAt(now = 2L, type = SyncEntityType.Attendance, key = "p1/c1"))

        val pending = dao.pendingAndFailed()

        assertEquals("duplicate (type,key) must REPLACE, not coexist", 1, pending.size)
        assertEquals(2L, pending.single().enqueuedAt)
    }

    @Test
    fun markAttemptedIncrementsCountAndPersistsError() = runTest {
        val id = dao.enqueue(jobAt(now = 1L, type = SyncEntityType.Attendance, key = "p1/c1"))

        dao.markAttempted(id = id, status = SyncStatus.Failed.name, attemptedAt = 9L, error = "boom")

        val row = dao.pendingAndFailed().single()
        assertEquals(SyncStatus.Failed.name, row.status)
        assertEquals(1, row.attemptCount)
        assertEquals(9L, row.lastAttemptAt)
        assertEquals("boom", row.lastError)
    }

    @Test
    fun deleteRemovesRow() = runTest {
        val id = dao.enqueue(jobAt(now = 1L, type = SyncEntityType.Attendance, key = "p1/c1"))

        val removed = dao.delete(id)

        assertEquals(1, removed)
        assertEquals(0, dao.pendingAndFailed().size)
    }

    @Test
    fun pendingAndFailedRespectsLimit() = runTest {
        repeat(10) { dao.enqueue(jobAt(now = it.toLong(), type = SyncEntityType.Attendance, key = "p1/c$it")) }

        val page = dao.pendingAndFailed(limit = 3)

        assertEquals(3, page.size)
        assertEquals(0L, page.first().enqueuedAt)
    }

    @Test
    fun countByTypeAndStatusReflectsState() = runTest {
        dao.enqueue(jobAt(now = 1L, type = SyncEntityType.Attendance, key = "p/c1"))
        dao.enqueue(jobAt(now = 2L, type = SyncEntityType.PracticalScore, key = "s/c/q"))
        val attendanceId = dao.pendingAndFailed().first { it.entityType == SyncEntityType.Attendance.name }.id
        dao.markAttempted(attendanceId, SyncStatus.Failed.name, 3L, "x")

        assertEquals(1, dao.countByTypeAndStatus(SyncEntityType.Attendance.name, SyncStatus.Failed.name))
        assertEquals(0, dao.countByTypeAndStatus(SyncEntityType.Attendance.name, SyncStatus.Pending.name))
        assertEquals(1, dao.countByTypeAndStatus(SyncEntityType.PracticalScore.name, SyncStatus.Pending.name))
    }

    private fun jobAt(now: Long, type: SyncEntityType, key: String): SyncJobEntity = SyncJobEntity(
        id = 0,
        entityType = type.name,
        entityKey = key,
        enqueuedAt = now,
        status = SyncStatus.Pending.name,
    )
}
