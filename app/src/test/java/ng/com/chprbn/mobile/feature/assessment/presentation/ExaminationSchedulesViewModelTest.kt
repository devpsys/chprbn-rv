package ng.com.chprbn.mobile.feature.assessment.presentation

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ng.com.chprbn.mobile.core.domain.model.PaperKind
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.core.utils.MainDispatcherRule
import ng.com.chprbn.mobile.feature.assessment.domain.model.AssessmentSchedule
import ng.com.chprbn.mobile.feature.assessment.domain.usecase.GetExaminationSchedulesUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ExaminationSchedulesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getSchedules = mockk<GetExaminationSchedulesUseCase>()

    @Test
    fun `init loads schedules and maps each to a ScheduleCardUiState`() = runTest {
        coEvery { getSchedules() } returns listOf(
            AssessmentSchedule(
                id = "PE-2024",
                title = "PE-2024 / Practical Exam",
                date = 1_700_000_000_000L, // 2023-11-14 in UTC
                paperKind = PaperKind.Practical,
                centerId = "C-1",
                syncStatus = SyncStatus.Synced,
            ),
            AssessmentSchedule(
                id = "MD-801",
                title = "MD-801 / Medical Diagnostics",
                date = 1_731_000_000_000L,
                paperKind = PaperKind.Practical,
                centerId = "C-1",
                syncStatus = SyncStatus.Pending,
            ),
        )

        val vm = ExaminationSchedulesViewModel(getSchedules)

        val state = vm.uiState.value
        assertEquals(2, state.schedules.size)
        val pe = state.schedules.first { it.id == "PE-2024" }
        assertEquals("PE-2024 / Practical Exam", pe.title)
        assertEquals(ScheduleSyncStatus.Synced, pe.syncStatus)
        // dateLabel is timezone-dependent, so assert the format shape only.
        assertTrue("dateLabel should look like 'MMM d, yyyy', was: ${pe.dateLabel}", pe.dateLabel.matches(Regex("[A-Za-z]{3} \\d{1,2}, \\d{4}")))

        val md = state.schedules.first { it.id == "MD-801" }
        assertEquals(ScheduleSyncStatus.Pending, md.syncStatus)
    }

    @Test
    fun `domain Failed collapses to screen Pending`() = runTest {
        coEvery { getSchedules() } returns listOf(
            AssessmentSchedule(
                id = "X-1",
                title = "X-1",
                date = 0L,
                paperKind = PaperKind.Theory,
                centerId = "C-1",
                syncStatus = SyncStatus.Failed,
            ),
        )

        val vm = ExaminationSchedulesViewModel(getSchedules)

        assertEquals(ScheduleSyncStatus.Pending, vm.uiState.value.schedules.single().syncStatus)
    }

    @Test
    fun `empty result yields empty schedule list`() = runTest {
        coEvery { getSchedules() } returns emptyList()

        val vm = ExaminationSchedulesViewModel(getSchedules)

        assertTrue(vm.uiState.value.schedules.isEmpty())
    }
}
