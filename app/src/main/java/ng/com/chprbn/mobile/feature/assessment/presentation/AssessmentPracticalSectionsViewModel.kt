package ng.com.chprbn.mobile.feature.assessment.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Hardcoded placeholder data matching the `practical_sections_2` design.
 * Captures `scheduleId` and `candidateId` from the route so a future use
 * case can fetch the candidate's section progress; today the placeholders
 * are the same regardless.
 */
@HiltViewModel
class AssessmentPracticalSectionsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    @Suppress("unused")
    private val scheduleId: String = savedStateHandle.get<String>("scheduleId").orEmpty()

    @Suppress("unused")
    private val candidateId: String = savedStateHandle.get<String>("candidateId").orEmpty()

    private val _uiState = MutableStateFlow(
        AssessmentPracticalSectionsUiState(
            candidateName = "Jane Doe",
            candidateExamId = "EXAM-2024-001",
            candidatePhotoUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuD3ZbwiLnp_GCC_W4bUfj3SsIWlterH9eUjyz45iikMZo1UorYg3BL5A-K7OAp5j2TrU0sYjr-pnOd4Qp-aWWr8T3iiGMmMXOdnDET8hRTu9VOCyTRX_EkVcHAh7X5I7QcGU59-Tr3yF3gHX_fGHbtekK5WZ2sd4Iwi2UDbqCUj_rbXtfZdbI5w1ZEk8vXw1vcB0kDZGw7LkwOK12KTOyUCVQA25jdigKE8LtwpKkArKCYIyG3Q0xkMRd1TSMEb-eXAv5REqmsycWNK",
            sectionsDone = 1,
            sectionsTotal = 3,
            sectionsRemaining = 2,
            sections = listOf(
                PracticalSectionUiState(
                    id = "A",
                    sectionTitle = "Section A",
                    sectionSubtitle = "Patient Assessment",
                    status = PracticalSectionStatus.Complete,
                    footerText = "09:45 AM",
                ),
                PracticalSectionUiState(
                    id = "B",
                    sectionTitle = "Section B",
                    sectionSubtitle = "Clinical Diagnosis",
                    status = PracticalSectionStatus.Incomplete,
                    // Stored as a count; the screen formats via the
                    // assessment_practical_sections_incomplete_remaining_format
                    // string. Carrying the raw "2" keeps the formatter at the UI
                    // boundary where strings.xml lives.
                    footerText = "2",
                ),
                PracticalSectionUiState(
                    id = "C",
                    sectionTitle = "Section C",
                    sectionSubtitle = "Ethical Standards",
                    status = PracticalSectionStatus.NotStarted,
                    footerText = "",
                ),
            ),
        ),
    )
    val uiState: StateFlow<AssessmentPracticalSectionsUiState> = _uiState.asStateFlow()
}
