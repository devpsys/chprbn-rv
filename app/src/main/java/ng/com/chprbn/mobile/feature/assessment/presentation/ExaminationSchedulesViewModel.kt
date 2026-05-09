package ng.com.chprbn.mobile.feature.assessment.presentation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Hardcoded placeholders for the design's two sample cards. When a real
 * data source lands (use case + repository), this VM grows the dependency
 * and the placeholders go away. Title/date strings are domain values, not
 * UI labels — they stay here rather than in `strings.xml`.
 */
@HiltViewModel
class ExaminationSchedulesViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(
        ExaminationSchedulesUiState(
            schedules = listOf(
                ScheduleCardUiState(
                    id = "PE-2024",
                    title = "PE-2024 / Practical Exam",
                    dateLabel = "Oct 24, 2024",
                    syncStatus = ScheduleSyncStatus.Synced,
                ),
                ScheduleCardUiState(
                    id = "MD-801",
                    title = "MD-801 / Theory",
                    dateLabel = "Oct 26, 2024",
                    syncStatus = ScheduleSyncStatus.Pending,
                ),
            ),
            // Placeholder hero image from the design. Replace with the brand
            // CDN's real asset when one is available.
            decorativeImageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDUUWh9eom3W7LUXXdq-GcpMH2LoOcSZKbsk6QcVVMnp4TVruY_pcLB9--_C4ddmBd-DFXbXy8lKLSW0gNGAd2gUmXzgsI_OlPmHcTaJW9FuaHYRHaEBVMhJnC8D0gMUHMli-ROKy0lYY9mLF6sNQvv49ghmLl91bM9UrlXCXDPa_0k_3hbP2lgwfzHfwtxh98wmJcBUQrNEDCC0jSIGq-UzXJfwwfYWvkgLkkGk9MstU6PNPE3zhIq9-mbDQP4r4mUE1l6yS7Ut8Fn",
        ),
    )
    val uiState: StateFlow<ExaminationSchedulesUiState> = _uiState.asStateFlow()
}
