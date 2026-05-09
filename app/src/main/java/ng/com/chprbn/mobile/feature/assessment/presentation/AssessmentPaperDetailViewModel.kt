package ng.com.chprbn.mobile.feature.assessment.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Hardcoded placeholder data matching the paper-detail design. The
 * `scheduleId` route arg is captured but not used to vary the demo content;
 * once a real data source lands the VM will look up the paper by schedule
 * id and populate the state from there.
 */
@HiltViewModel
class AssessmentPaperDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    @Suppress("unused")
    private val scheduleId: String = savedStateHandle.get<String>("scheduleId").orEmpty()

    private val _uiState = MutableStateFlow(
        AssessmentPaperDetailUiState(
            paperTitle = "Regulatory Medical Paper A-14",
            statusLabel = "Active",
            progressFraction = 0.75f,
            checkedInCount = 90,
            totalCount = 120,
            facilityName = "St. Jude Metropolitan Hospital",
            facilityAddress = "Regulatory District 04, North Campus",
            hallName = "Auditorium C-12",
            hallAddress = "3rd Floor, West Wing Elevator",
            candidates = listOf(
                CandidateRowUiState(
                    id = "RE-40112",
                    initials = "JS",
                    fullName = "Jonathan Smith",
                    syncStatus = CandidateSyncStatus.Synced,
                ),
                CandidateRowUiState(
                    id = "RE-40115",
                    initials = "AM",
                    fullName = "Anita Meyer",
                    syncStatus = CandidateSyncStatus.Unsynced,
                ),
            ),
            // Placeholder hero image from the design. Swap to the brand CDN
            // asset when one exists.
            heroImageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAafDl5YNtmkZELv1vLNJKQm3oAK2ktrFz6PsRdEzyLqgb9BDYbOC6VlXI6TZBHHOGW0yHeqw0BdQDutjPSiAdyIq-T4Opub7OVN6Bxh6Bcuygl2n7JzVnongpEuahZZX98uVyC9Je_M9r4W12lhy4uGCvJuzSIWBl0sIswltfsRYhVdKJQVA2tPE8CfPK4uZoIa24R2qPuRGN8StGRqqsGaQiTJ-Nj14RyLWwQdMETVMKrlz7VZIQPvk03vChf_U8U9SOAiK61t5YP",
        ),
    )
    val uiState: StateFlow<AssessmentPaperDetailUiState> = _uiState.asStateFlow()
}
