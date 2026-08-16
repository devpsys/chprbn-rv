package ng.com.chprbn.mobile.feature.exam.presentation

import android.content.Context
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.R
import ng.com.chprbn.mobile.core.domain.model.SyncBatchResult
import ng.com.chprbn.mobile.core.utils.MainDispatcherRule
import ng.com.chprbn.mobile.feature.exam.domain.model.ExamStatistics
import ng.com.chprbn.mobile.feature.exam.domain.model.SaveResult
import ng.com.chprbn.mobile.feature.exam.domain.usecase.ClearExamCacheUseCase
import ng.com.chprbn.mobile.feature.exam.domain.usecase.GetExamStatisticsUseCase
import ng.com.chprbn.mobile.feature.exam.domain.usecase.SyncExamRecordsUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ExamStatisticsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getStatistics = mockk<GetExamStatisticsUseCase>()
    private val syncExamRecords = mockk<SyncExamRecordsUseCase>()
    private val clearExamCache = mockk<ClearExamCacheUseCase>()
    private val context = mockk<Context> {
        every { getString(R.string.exam_paper_no_data_yet) } returns "No data yet"
        every { getString(R.string.exam_statistics_updated_just_now) } returns "Updated just now"
        every { getString(R.string.exam_statistics_captured_on_device) } returns "On this device"
        // Context.getString(int, vararg Any?) — see SyncViewModelTest for the
        // vararg-matching rationale.
        every { getString(R.string.exam_statistics_updated_minutes_ago_format, *anyVararg()) } answers {
            @Suppress("UNCHECKED_CAST")
            "Updated ${(invocation.args[1] as Array<Any?>)[0]}m ago"
        }
        every { getString(R.string.exam_statistics_updated_hours_ago_format, *anyVararg()) } answers {
            @Suppress("UNCHECKED_CAST")
            "Updated ${(invocation.args[1] as Array<Any?>)[0]}h ago"
        }
        every { getString(R.string.exam_statistics_updated_days_ago_format, *anyVararg()) } answers {
            @Suppress("UNCHECKED_CAST")
            "Updated ${(invocation.args[1] as Array<Any?>)[0]}d ago"
        }
    }

    @Test
    fun `init loads statistics into ui state`() = runTest {
        coEvery { getStatistics() } returns stats(
            recordsDownloaded = 100,
            attendanceCaptured = 80,
            practicalCaptured = 12,
            projectCaptured = 4,
            syncedCount = 50,
            cachedCount = 100,
            pendingCount = 30,
            failedCount = 0,
            lastUpdatedAt = null,
        )

        val viewModel = ExamStatisticsViewModel(getStatistics, syncExamRecords, clearExamCache, context)

        val state = viewModel.uiState.value
        assertEquals("100", state.recordsDownloaded)
        assertEquals("80", state.attendanceCaptured)
        assertEquals("12", state.practicalCaptured)
        assertEquals("4", state.projectCaptured)
        assertEquals("50", state.syncedRecords)
        assertEquals("80% Completion", state.attendanceSubtitle)
        assertEquals("On this device", state.practicalSubtitle)
        assertEquals("On this device", state.projectSubtitle)
        assertEquals("No data yet", state.recordsUpdatedLabel)
        assertEquals(0.5f, state.syncProgressFraction)
        assertEquals(0.3f, state.cachedBarFraction)
        assertEquals("", state.footnote)
    }

    @Test
    fun `footnote surfaces when failed count is positive`() = runTest {
        coEvery { getStatistics() } returns stats(
            recordsDownloaded = 10,
            attendanceCaptured = 10,
            syncedCount = 5,
            cachedCount = 10,
            pendingCount = 2,
            failedCount = 3,
            lastUpdatedAt = null,
        )

        val viewModel = ExamStatisticsViewModel(getStatistics, syncExamRecords, clearExamCache, context)

        assertEquals("* 3 records failed to sync.", viewModel.uiState.value.footnote)
    }

    @Test
    fun `empty cache uses em-dash attendance subtitle and no completion percentage`() = runTest {
        coEvery { getStatistics() } returns stats(
            recordsDownloaded = 0,
            attendanceCaptured = 0,
            syncedCount = 0,
            cachedCount = 0,
            pendingCount = 0,
            failedCount = 0,
            lastUpdatedAt = null,
        )

        val viewModel = ExamStatisticsViewModel(getStatistics, syncExamRecords, clearExamCache, context)

        val state = viewModel.uiState.value
        assertEquals("—", state.attendanceSubtitle)
        assertEquals("—", state.practicalSubtitle)
        assertEquals("—", state.projectSubtitle)
        assertTrue(state.syncProgressFraction in 0f..1f)
    }

    @Test
    fun `onSyncNow invokes sync then refresh and surfaces the result counts`() = runTest {
        coEvery { getStatistics() } returnsMany listOf(
            stats(),
            stats(syncedCount = 1),
        )
        coEvery { syncExamRecords() } returns SyncBatchResult(attempted = 5, succeeded = 4, failed = 1)

        val viewModel = ExamStatisticsViewModel(getStatistics, syncExamRecords, clearExamCache, context)
        viewModel.onSyncNow()

        coVerify(exactly = 1) { syncExamRecords() }
        coVerify(exactly = 2) { getStatistics() }
        assertEquals(SyncOperationUiState.Result(succeeded = 4, failed = 1), viewModel.syncState.value)
    }

    @Test
    fun `onSyncResultDismissed returns sync state to idle`() = runTest {
        coEvery { getStatistics() } returns stats()
        coEvery { syncExamRecords() } returns SyncBatchResult.Empty

        val viewModel = ExamStatisticsViewModel(getStatistics, syncExamRecords, clearExamCache, context)
        viewModel.onSyncNow()
        viewModel.onSyncResultDismissed()

        assertEquals(SyncOperationUiState.Idle, viewModel.syncState.value)
    }

    @Test
    fun `onClearCachedClicked shows the warning dialog without clearing yet`() = runTest {
        coEvery { getStatistics() } returns stats()

        val viewModel = ExamStatisticsViewModel(getStatistics, syncExamRecords, clearExamCache, context)
        viewModel.onClearCachedClicked()

        assertEquals(ClearCacheUiState.WarningShown, viewModel.clearCacheState.value)
        coVerify(exactly = 0) { clearExamCache() }
    }

    @Test
    fun `onClearCacheConfirmed invokes clear then refresh on success`() = runTest {
        coEvery { getStatistics() } returnsMany listOf(stats(), stats())
        coEvery { clearExamCache() } returns SaveResult.Success

        val viewModel = ExamStatisticsViewModel(getStatistics, syncExamRecords, clearExamCache, context)
        viewModel.onClearCachedClicked()
        viewModel.onClearCacheConfirmed()

        coVerify(exactly = 1) { clearExamCache() }
        coVerify(exactly = 2) { getStatistics() }
        assertEquals(ClearCacheUiState.Success, viewModel.clearCacheState.value)
    }

    @Test
    fun `onClearCacheConfirmed surfaces the error message on failure`() = runTest {
        coEvery { getStatistics() } returns stats()
        coEvery { clearExamCache() } returns SaveResult.Error("Disk write failed.")

        val viewModel = ExamStatisticsViewModel(getStatistics, syncExamRecords, clearExamCache, context)
        viewModel.onClearCachedClicked()
        viewModel.onClearCacheConfirmed()

        coVerify(exactly = 1) { clearExamCache() }
        // Error means the transaction rolled back — no need to re-fetch stats.
        coVerify(exactly = 1) { getStatistics() }
        assertEquals(ClearCacheUiState.Error("Disk write failed."), viewModel.clearCacheState.value)
    }

    @Test
    fun `onClearCacheDismissed returns clear state to idle`() = runTest {
        coEvery { getStatistics() } returns stats()

        val viewModel = ExamStatisticsViewModel(getStatistics, syncExamRecords, clearExamCache, context)
        viewModel.onClearCachedClicked()
        viewModel.onClearCacheDismissed()

        assertEquals(ClearCacheUiState.Idle, viewModel.clearCacheState.value)
    }

    private fun stats(
        recordsDownloaded: Int = 0,
        attendanceCaptured: Int = 0,
        practicalCaptured: Int = 0,
        projectCaptured: Int = 0,
        syncedCount: Int = 0,
        cachedCount: Int = 0,
        pendingCount: Int = 0,
        failedCount: Int = 0,
        lastUpdatedAt: Long? = null,
    ) = ExamStatistics(
        recordsDownloaded = recordsDownloaded,
        attendanceCaptured = attendanceCaptured,
        practicalCaptured = practicalCaptured,
        projectCaptured = projectCaptured,
        syncedCount = syncedCount,
        cachedCount = cachedCount,
        pendingCount = pendingCount,
        failedCount = failedCount,
        lastUpdatedAt = lastUpdatedAt,
    )
}
