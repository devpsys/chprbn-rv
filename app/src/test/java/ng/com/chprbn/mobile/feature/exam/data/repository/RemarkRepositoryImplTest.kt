package ng.com.chprbn.mobile.feature.exam.data.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.core.sync.Clock
import ng.com.chprbn.mobile.core.sync.SyncEntityType
import ng.com.chprbn.mobile.core.sync.SyncJobDao
import ng.com.chprbn.mobile.core.sync.SyncJobEntity
import ng.com.chprbn.mobile.core.sync.SyncWorkScheduler
import ng.com.chprbn.mobile.feature.exam.data.local.RemarkDao
import ng.com.chprbn.mobile.feature.exam.data.local.RemarkEntity
import ng.com.chprbn.mobile.feature.exam.domain.model.AddRemarkResult
import ng.com.chprbn.mobile.feature.exam.domain.model.RemarkSeverity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RemarkRepositoryImplTest {

    private val now = 1_700_000_000_000L
    private val clock = Clock { now }
    private val remarkDao = mockk<RemarkDao>(relaxUnitFun = true) {
        coEvery { upsert(any()) } returns 1L
    }
    private val syncJobDao = mockk<SyncJobDao>(relaxUnitFun = true) {
        coEvery { enqueue(any()) } returns 1L
    }
    private val workScheduler = mockk<SyncWorkScheduler>(relaxUnitFun = true)

    private val repository = RemarkRepositoryImpl(remarkDao, syncJobDao, workScheduler, clock)

    @Test
    fun `addRemark generates a UUID id, upserts, enqueues, and schedules`() = runTest {
        val captured = slot<SyncJobEntity>()
        coEvery { syncJobDao.enqueue(capture(captured)) } returns 1L

        val result = repository.addRemark(
            candidateId = "c1",
            paperId = "p1",
            body = "Late arrival",
            severity = RemarkSeverity.Warning,
        )

        assertTrue(result is AddRemarkResult.Success)
        val remark = (result as AddRemarkResult.Success).remark
        // UUID format: 8-4-4-4-12 hex digits
        assertTrue(
            "expected UUID id, got: ${remark.id}",
            remark.id.matches(Regex("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")),
        )
        assertEquals("c1", remark.candidateId)
        assertEquals("p1", remark.paperId)
        assertEquals("Late arrival", remark.body)
        assertEquals(RemarkSeverity.Warning, remark.severity)
        assertEquals(now, remark.createdAt)
        assertEquals(SyncStatus.Pending, remark.syncStatus)

        val job = captured.captured
        assertEquals(SyncEntityType.Remark.name, job.entityType)
        assertEquals(remark.id, job.entityKey)
    }

    @Test
    fun `addRemark preserves null paperId (centre-wide remark)`() = runTest {
        val result = repository.addRemark(
            candidateId = "c1",
            paperId = null,
            body = "x",
            severity = RemarkSeverity.Info,
        )

        assertTrue(result is AddRemarkResult.Success)
        assertNull((result as AddRemarkResult.Success).remark.paperId)
    }

    @Test
    fun `getRemarksForCandidate maps DAO entities to domain`() = runTest {
        coEvery { remarkDao.getForCandidate("c1") } returns listOf(
            RemarkEntity(
                id = "r1",
                candidateId = "c1",
                paperId = "p1",
                body = "x",
                severity = "Warning",
                createdAt = 0L,
                syncStatus = SyncStatus.Pending.name,
            ),
        )

        val remarks = repository.getRemarksForCandidate("c1")

        assertEquals(1, remarks.size)
        assertEquals("r1", remarks.single().id)
        assertEquals(RemarkSeverity.Warning, remarks.single().severity)
    }

    @Test
    fun `DAO throw is mapped to Error and no sync work scheduled`() = runTest {
        coEvery { remarkDao.upsert(any()) } throws RuntimeException("disk full")

        val result = repository.addRemark("c1", null, "x", RemarkSeverity.Info)

        assertTrue(result is AddRemarkResult.Error)
        coVerify(exactly = 0) { workScheduler.scheduleSyncWork() }
    }
}
