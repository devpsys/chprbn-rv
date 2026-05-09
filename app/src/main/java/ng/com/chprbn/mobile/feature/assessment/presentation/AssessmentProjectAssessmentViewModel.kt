package ng.com.chprbn.mobile.feature.assessment.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * Hardcoded placeholder data matching the `project_assessment_full_screen`
 * design. Captures `scheduleId` and `candidateId` from the route so a
 * future use case can fetch the candidate; today the placeholder data
 * matches the design regardless.
 */
@HiltViewModel
class AssessmentProjectAssessmentViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    @Suppress("unused")
    private val scheduleId: String = savedStateHandle.get<String>("scheduleId").orEmpty()

    @Suppress("unused")
    private val candidateId: String = savedStateHandle.get<String>("candidateId").orEmpty()

    private val _uiState = MutableStateFlow(
        AssessmentProjectAssessmentUiState(
            candidateName = "Johnathan Doe",
            examId = "EX-2024-0092",
            role = "Clinical Practitioner",
            photoUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBxh4dB1QAHSXmfHAbtE2vzit_dZk0-7xG9qAqXJh_Ax7MW-AyRlV76YWemN0KMKRTmmdKOAMynr28n8iKdkFqY1JA1P-DtAeTlDq1n7MFTEj5ZiY8jRuKr4gxpuJwb9JO-Zm8MDzs80RSOCIMfjJq32R8Y3moVAzc4zSa-wJcAYz_dp88cRuvOH8dqt7XH5A1W-6DQjXFudpYtnUszU7n4tj8XCH9Bj6Io1ShAaIteUPYrdPhEmirIAtakbvNq8kltbGjOLMmBRoXJ",
            verified = true,
            scoreText = "",
            maxScore = 10,
        ),
    )
    val uiState: StateFlow<AssessmentProjectAssessmentUiState> = _uiState.asStateFlow()

    /** Accepts only digits and at most one decimal point with one fractional
     *  digit, and rejects values that would parse outside `[0, maxScore]`.
     *  Empty input is allowed so the user can clear the field. */
    fun onScoreChange(text: String) {
        if (text.isEmpty()) {
            _uiState.update { it.copy(scoreText = "") }
            return
        }
        if (!text.matches(SCORE_PATTERN)) return
        val parsed = text.toDoubleOrNull()
        val state = _uiState.value
        // Allow trailing decimal (e.g. "8.") even though it isn't parseable —
        // the user is mid-entry and `parsed` will be null.
        if (parsed == null || parsed in 0.0..state.maxScore.toDouble()) {
            _uiState.update { it.copy(scoreText = text) }
        }
    }

    private companion object {
        // Up to two leading digits + optional single fractional digit. Allows
        // "10" for the max but not "11" or "10.5".
        private val SCORE_PATTERN = Regex("^\\d{1,2}(\\.\\d?)?$")
    }
}
