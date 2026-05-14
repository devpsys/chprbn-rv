package ng.com.chprbn.mobile.feature.assessment.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.feature.assessment.domain.model.AssessmentSchedule
import ng.com.chprbn.mobile.feature.assessment.domain.usecase.GetExaminationSchedulesUseCase
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

/**
 * Reads cached schedules from [GetExaminationSchedulesUseCase] and maps
 * them to the design's card shape. The use case writes through from the
 * remote source on first launch; subsequent reads return the cache.
 *
 * Sync status mapping collapses domain `Failed` into the screen's
 * `Pending` because the schedules screen design only models a 2-state
 * pill. A future iteration that surfaces `Failed` distinctly can extend
 * [ScheduleSyncStatus] without touching the use case.
 */
@HiltViewModel
class ExaminationSchedulesViewModel @Inject constructor(
    private val getSchedules: GetExaminationSchedulesUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExaminationSchedulesUiState())
    val uiState: StateFlow<ExaminationSchedulesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val schedules = getSchedules()
            _uiState.value = ExaminationSchedulesUiState(
                schedules = schedules.map { it.toCardUiState() },
                decorativeImageUrl = null,
            )
        }
    }

    private fun AssessmentSchedule.toCardUiState(): ScheduleCardUiState = ScheduleCardUiState(
        id = id,
        title = title,
        dateLabel = DATE_FORMATTER.format(Instant.ofEpochMilli(date)),
        syncStatus = if (syncStatus == SyncStatus.Synced) {
            ScheduleSyncStatus.Synced
        } else {
            // Domain Pending + Failed both collapse to the screen's Pending pill.
            ScheduleSyncStatus.Pending
        },
    )

    private companion object {
        val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter
            .ofPattern("MMM d, yyyy", Locale.US)
            .withZone(ZoneId.systemDefault())
    }
}
