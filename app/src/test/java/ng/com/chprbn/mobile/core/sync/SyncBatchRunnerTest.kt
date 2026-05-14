package ng.com.chprbn.mobile.core.sync

import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SyncBatchRunnerTest {

    private val now = 1_700_000_000_000L
    private val clock = Clock { now }

    @Test
    fun `empty queue returns Empty result`() = runTest {
        val runner = SyncBatchRunner(
            syncJobDao = FakeSyncJobDao(),
            handlers = emptyMap(),
            clock = clock,
        )

        val result = runner.runBatch()

        assertEquals(0, result.attempted)
        assertEquals(0, result.succeeded)
        assertEquals(0, result.failed)
    }

    @Test
    fun `all successful uploads remove jobs and return success counts`() = runTest {
        val dao = FakeSyncJobDao().apply {
            seed(
                attendanceJob(id = 1, key = "p1/c1"),
                attendanceJob(id = 2, key = "p1/c2"),
            )
        }
        val runner = SyncBatchRunner(
            syncJobDao = dao,
            handlers = mapOf(SyncEntityType.Attendance to SyncEntityHandler { SyncOutcome.Success }),
            clock = clock,
        )

        val result = runner.runBatch()

        assertEquals(2, result.attempted)
        assertEquals(2, result.succeeded)
        assertEquals(0, result.failed)
        assertTrue("jobs should be deleted on success", dao.snapshot().isEmpty())
    }

    @Test
    fun `failed handler marks job Failed with error message and continues batch`() = runTest {
        val dao = FakeSyncJobDao().apply {
            seed(
                attendanceJob(id = 1, key = "p1/c1"),
                attendanceJob(id = 2, key = "p1/c2"),
            )
        }
        var callIndex = 0
        val runner = SyncBatchRunner(
            syncJobDao = dao,
            handlers = mapOf(
                SyncEntityType.Attendance to SyncEntityHandler {
                    callIndex++
                    if (callIndex == 1) SyncOutcome.Failure("network down") else SyncOutcome.Success
                },
            ),
            clock = clock,
        )

        val result = runner.runBatch()

        assertEquals(2, result.attempted)
        assertEquals(1, result.succeeded)
        assertEquals(1, result.failed)
        assertEquals(listOf("network down"), result.errors)
        val remaining = dao.snapshot()
        assertEquals(1, remaining.size)
        assertEquals(SyncStatus.Failed.name, remaining.single().status)
        assertEquals(1, remaining.single().attemptCount)
    }

    @Test
    fun `handler that throws is caught as transient failure`() = runTest {
        val dao = FakeSyncJobDao().apply { seed(attendanceJob(id = 1, key = "p1/c1")) }
        val runner = SyncBatchRunner(
            syncJobDao = dao,
            handlers = mapOf(
                SyncEntityType.Attendance to SyncEntityHandler { error("boom") },
            ),
            clock = clock,
        )

        val result = runner.runBatch()

        assertEquals(1, result.failed)
        assertEquals("boom", result.errors.single())
    }

    @Test
    fun `unknown entity type is marked Failed with descriptive message`() = runTest {
        val dao = FakeSyncJobDao().apply {
            seed(
                SyncJobEntity(
                    id = 1,
                    entityType = "Garbage",
                    entityKey = "x",
                    enqueuedAt = now,
                    status = SyncStatus.Pending.name,
                ),
            )
        }
        val runner = SyncBatchRunner(dao, handlers = emptyMap(), clock = clock)

        val result = runner.runBatch()

        assertEquals(1, result.failed)
        assertEquals("Unknown entity type: Garbage", result.errors.single())
    }

    @Test
    fun `missing handler for known type is marked Failed`() = runTest {
        val dao = FakeSyncJobDao().apply { seed(attendanceJob(id = 1, key = "p1/c1")) }
        val runner = SyncBatchRunner(dao, handlers = emptyMap(), clock = clock)

        val result = runner.runBatch()

        assertEquals(1, result.failed)
        assertEquals("No handler bound for Attendance", result.errors.single())
        assertNull("attempt should advance", null)
    }

    private fun attendanceJob(id: Long, key: String): SyncJobEntity = SyncJobEntity(
        id = id,
        entityType = SyncEntityType.Attendance.name,
        entityKey = key,
        enqueuedAt = now,
        status = SyncStatus.Pending.name,
    )
}
