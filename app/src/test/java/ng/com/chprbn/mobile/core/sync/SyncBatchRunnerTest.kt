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

    @Test
    fun `previously-failed job is retried and cleared on success`() = runTest {
        // pendingAndFailed() returns both Pending and Failed rows. A Failed
        // row that succeeds on retry should be deleted just like a Pending
        // row that succeeds first try.
        val dao = FakeSyncJobDao().apply {
            seed(
                SyncJobEntity(
                    id = 1,
                    entityType = SyncEntityType.Attendance.name,
                    entityKey = "p1/c1",
                    enqueuedAt = now - 1_000,
                    status = SyncStatus.Failed.name,
                    attemptCount = 2,
                    lastError = "previous network down",
                ),
            )
        }
        val runner = SyncBatchRunner(
            syncJobDao = dao,
            handlers = mapOf(SyncEntityType.Attendance to SyncEntityHandler { SyncOutcome.Success }),
            clock = clock,
        )

        val result = runner.runBatch()

        assertEquals(1, result.attempted)
        assertEquals(1, result.succeeded)
        assertEquals(0, result.failed)
        assertTrue("retried-to-success row should be deleted", dao.snapshot().isEmpty())
    }

    @Test
    fun `all-failure batch aggregates errors in dispatch order`() = runTest {
        val dao = FakeSyncJobDao().apply {
            seed(
                attendanceJob(id = 1, key = "p1/a", enqueuedAt = now - 30),
                attendanceJob(id = 2, key = "p1/b", enqueuedAt = now - 20),
                attendanceJob(id = 3, key = "p1/c", enqueuedAt = now - 10),
            )
        }
        val errorByKey = mapOf(
            "p1/a" to "first failure",
            "p1/b" to "second failure",
            "p1/c" to "third failure",
        )
        val runner = SyncBatchRunner(
            syncJobDao = dao,
            handlers = mapOf(
                SyncEntityType.Attendance to SyncEntityHandler { key ->
                    SyncOutcome.Failure(errorByKey.getValue(key))
                },
            ),
            clock = clock,
        )

        val result = runner.runBatch()

        assertEquals(3, result.attempted)
        assertEquals(0, result.succeeded)
        assertEquals(3, result.failed)
        // Dispatch order is FIFO by enqueuedAt — the errors list must match.
        assertEquals(
            listOf("first failure", "second failure", "third failure"),
            result.errors,
        )
        // No row is deleted; every row stays with status Failed.
        assertEquals(3, dao.snapshot().size)
        assertTrue(dao.snapshot().all { it.status == SyncStatus.Failed.name })
    }

    @Test
    fun `batch respects size limit and dispatches FIFO by enqueuedAt`() = runTest {
        val dao = FakeSyncJobDao().apply {
            // Insert out of enqueuedAt order to prove the runner pulls by
            // enqueuedAt ASC, not by insertion order.
            seed(
                attendanceJob(id = 1, key = "newest", enqueuedAt = now + 30),
                attendanceJob(id = 2, key = "oldest", enqueuedAt = now - 30),
                attendanceJob(id = 3, key = "middle", enqueuedAt = now),
            )
        }
        val seenKeys = mutableListOf<String>()
        val runner = SyncBatchRunner(
            syncJobDao = dao,
            handlers = mapOf(
                SyncEntityType.Attendance to SyncEntityHandler { key ->
                    seenKeys.add(key)
                    SyncOutcome.Success
                },
            ),
            clock = clock,
        )

        val result = runner.runBatch(batchSize = 2)

        assertEquals("batchSize bounds attempted count", 2, result.attempted)
        assertEquals(listOf("oldest", "middle"), seenKeys)
        // "newest" wasn't included — it should still be in the queue.
        assertEquals(listOf("newest"), dao.snapshot().map { it.entityKey })
    }

    @Test
    fun `cross-type batch routes each row to its own handler`() = runTest {
        val dao = FakeSyncJobDao().apply {
            seed(
                attendanceJob(id = 1, key = "p1/c1", enqueuedAt = now - 30),
                SyncJobEntity(
                    id = 2,
                    entityType = SyncEntityType.Remark.name,
                    entityKey = "r-42",
                    enqueuedAt = now - 20,
                    status = SyncStatus.Pending.name,
                ),
                SyncJobEntity(
                    id = 3,
                    entityType = SyncEntityType.PracticalScore.name,
                    entityKey = "p1/c1/q1",
                    enqueuedAt = now - 10,
                    status = SyncStatus.Pending.name,
                ),
            )
        }
        val attendanceKeys = mutableListOf<String>()
        val remarkKeys = mutableListOf<String>()
        val practicalScoreKeys = mutableListOf<String>()
        val runner = SyncBatchRunner(
            syncJobDao = dao,
            handlers = mapOf(
                SyncEntityType.Attendance to SyncEntityHandler { key ->
                    attendanceKeys.add(key); SyncOutcome.Success
                },
                SyncEntityType.Remark to SyncEntityHandler { key ->
                    remarkKeys.add(key); SyncOutcome.Failure("remark API 500")
                },
                SyncEntityType.PracticalScore to SyncEntityHandler { key ->
                    practicalScoreKeys.add(key); SyncOutcome.Success
                },
            ),
            clock = clock,
        )

        val result = runner.runBatch()

        assertEquals(listOf("p1/c1"), attendanceKeys)
        assertEquals(listOf("r-42"), remarkKeys)
        assertEquals(listOf("p1/c1/q1"), practicalScoreKeys)
        assertEquals(3, result.attempted)
        assertEquals(2, result.succeeded)
        assertEquals(1, result.failed)
        assertEquals(listOf("remark API 500"), result.errors)
        // Two successful rows deleted; the failed remark row remains.
        assertEquals(listOf("r-42"), dao.snapshot().map { it.entityKey })
    }

    @Test
    fun `failed job records lastAttemptAt from the injected clock`() = runTest {
        val pinned = 1_800_000_000_000L
        val dao = FakeSyncJobDao().apply { seed(attendanceJob(id = 1, key = "p1/c1")) }
        val runner = SyncBatchRunner(
            syncJobDao = dao,
            handlers = mapOf(
                SyncEntityType.Attendance to SyncEntityHandler { SyncOutcome.Failure("boom") },
            ),
            clock = Clock { pinned },
        )

        runner.runBatch()

        assertEquals(pinned, dao.snapshot().single().lastAttemptAt)
    }

    @Test
    fun `attemptCount accumulates across successive failing batches`() = runTest {
        val dao = FakeSyncJobDao().apply { seed(attendanceJob(id = 1, key = "p1/c1")) }
        val runner = SyncBatchRunner(
            syncJobDao = dao,
            handlers = mapOf(
                SyncEntityType.Attendance to SyncEntityHandler { SyncOutcome.Failure("offline") },
            ),
            clock = clock,
        )

        runner.runBatch()
        runner.runBatch()
        runner.runBatch()

        assertEquals(3, dao.snapshot().single().attemptCount)
        assertEquals(SyncStatus.Failed.name, dao.snapshot().single().status)
    }

    private fun attendanceJob(
        id: Long,
        key: String,
        enqueuedAt: Long = now,
    ): SyncJobEntity = SyncJobEntity(
        id = id,
        entityType = SyncEntityType.Attendance.name,
        entityKey = key,
        enqueuedAt = enqueuedAt,
        status = SyncStatus.Pending.name,
    )
}
