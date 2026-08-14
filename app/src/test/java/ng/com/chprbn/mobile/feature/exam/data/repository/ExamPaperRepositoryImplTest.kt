package ng.com.chprbn.mobile.feature.exam.data.repository

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.domain.model.PaperKind
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.feature.exam.data.local.AttendanceDao
import ng.com.chprbn.mobile.feature.exam.data.local.CenterDao
import ng.com.chprbn.mobile.feature.exam.data.local.CenterEntity
import ng.com.chprbn.mobile.feature.exam.data.local.PaperDao
import ng.com.chprbn.mobile.feature.exam.data.local.PaperEntity
import ng.com.chprbn.mobile.feature.exam.domain.model.AttendanceStatus
import ng.com.chprbn.mobile.feature.exam.domain.model.ExamDashboardResult
import ng.com.chprbn.mobile.feature.exam.domain.model.ExamPaperDetailResult
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

class ExamPaperRepositoryImplTest {

    private val centerDao = mockk<CenterDao>()
    private val paperDao = mockk<PaperDao>()
    private val attendanceDao = mockk<AttendanceDao>()
    private val repository = ExamPaperRepositoryImpl(centerDao, paperDao, attendanceDao)

    private val centerEntity = CenterEntity(
        id = "c1",
        name = "Lagos Centre",
        code = "LAG",
        location = "Lagos",
    )
    private val paperEntity = PaperEntity(
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

    @Test
    fun `dashboard returns Empty when no center cached`() = runTest {
        coEvery { centerDao.getFirst() } returns null

        val result = repository.getDashboardSummary()

        assertEquals(ExamDashboardResult.Empty, result)
    }

    @Test
    fun `dashboard builds summary with checked-in count, total candidates, and papers count`() = runTest {
        coEvery { centerDao.getFirst() } returns centerEntity
        coEvery { paperDao.getForCenter("c1") } returns listOf(paperEntity)
        coEvery {
            attendanceDao.countByStatusForPaper("p1", AttendanceStatus.SignedIn.name)
        } returns 12

        val result = repository.getDashboardSummary()

        val summary = (result as ExamDashboardResult.Success).summary
        assertEquals("c1", summary.center.id)
        assertEquals(1, summary.papersCount)
        assertEquals("Active Session", summary.attendanceCard.statusLabel)
        assertEquals("12 / 30 checked in", summary.attendanceCard.countLabel)
    }

    @Test
    fun `dashboard builds a Success with zero papers when centre has no schedule today`() = runTest {
        coEvery { centerDao.getFirst() } returns centerEntity
        coEvery { paperDao.getForCenter("c1") } returns emptyList()

        val result = repository.getDashboardSummary()

        val summary = (result as ExamDashboardResult.Success).summary
        assertEquals(0, summary.papersCount)
        assertEquals("No Session", summary.attendanceCard.statusLabel)
        assertEquals("0 / 0 checked in", summary.attendanceCard.countLabel)
    }

    @Test
    fun `dashboard maps IOException to friendly Error message`() = runTest {
        coEvery { centerDao.getFirst() } throws IOException("offline")

        val result = repository.getDashboardSummary()

        assertEquals(
            "Network error. Please check your connection.",
            (result as ExamDashboardResult.Error).message,
        )
    }

    @Test
    fun `getPapersForToday maps every paper entity to its domain model`() = runTest {
        coEvery { paperDao.getAll() } returns listOf(paperEntity)

        val papers = repository.getPapersForToday()

        assertEquals(1, papers.size)
        assertEquals("p1", papers.single().id)
        assertEquals(PaperKind.Practical, papers.single().paperKind)
    }

    @Test
    fun `getPaperDetail returns NotFound when paper missing`() = runTest {
        coEvery { paperDao.getById("p1") } returns null

        assertEquals(ExamPaperDetailResult.NotFound, repository.getPaperDetail("p1"))
    }

    @Test
    fun `getPaperDetail returns NotFound when centre row missing`() = runTest {
        coEvery { paperDao.getById("p1") } returns paperEntity
        coEvery { centerDao.getById("c1") } returns null

        assertEquals(ExamPaperDetailResult.NotFound, repository.getPaperDetail("p1"))
    }

    @Test
    fun `getPaperDetail derives counters from attendance dao aggregates`() = runTest {
        coEvery { paperDao.getById("p1") } returns paperEntity
        coEvery { centerDao.getById("c1") } returns centerEntity
        coEvery {
            attendanceDao.countByStatusForPaper("p1", AttendanceStatus.SignedIn.name)
        } returns 8
        coEvery { attendanceDao.countBySyncStatus(SyncStatus.Pending.name) } returns 5
        coEvery { attendanceDao.countBySyncStatus(SyncStatus.Failed.name) } returns 2
        coEvery { attendanceDao.mostRecentMarkedAt() } returns 1_700L

        val detail = (repository.getPaperDetail("p1") as ExamPaperDetailResult.Success).detail

        assertEquals(8, detail.checkedInCount)
        assertEquals(7, detail.pendingSyncCount) // 5 pending + 2 failed
        assertEquals(1_700L, detail.lastSyncAt)
        assertEquals(30, detail.totalCandidates)
    }

    @Test
    fun `getPaperDetail maps thrown exception to Error arm`() = runTest {
        coEvery { paperDao.getById("p1") } throws RuntimeException("disk corrupt")

        val result = repository.getPaperDetail("p1")

        assertEquals("disk corrupt", (result as ExamPaperDetailResult.Error).message)
    }
}
