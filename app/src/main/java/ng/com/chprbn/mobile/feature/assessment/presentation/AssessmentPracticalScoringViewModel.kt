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
 * Hardcoded placeholder data for the practical-scoring screen, matching
 * the `practical_scoring` design. Captures `scheduleId`, `candidateId`
 * and `sectionId` from the route so a future use case can fetch the
 * section's questions; today the placeholders are the same regardless.
 */
@HiltViewModel
class AssessmentPracticalScoringViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    @Suppress("unused")
    private val scheduleId: String = savedStateHandle.get<String>("scheduleId").orEmpty()

    @Suppress("unused")
    private val candidateId: String = savedStateHandle.get<String>("candidateId").orEmpty()

    private val sectionId: String = savedStateHandle.get<String>("sectionId").orEmpty()

    private val _uiState = MutableStateFlow(
        AssessmentPracticalScoringUiState(
            sectionTitle = sectionTitleFor(sectionId),
            questions = listOf(
                ScoreQuestionUiState(
                    id = "q1",
                    number = 1,
                    prompt = "Measure blood pressure accurately.",
                    imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDnHBNuzO9tuAgTsRBgZ1rsFP7ZHBw_SFD-SyFFvtMZkGUOuLbWFx7M-JzR4Zy0zrdMNE-aiVJ-33EJwXSw83bFXGeWQYK470nfrdZ1Pn2E_NdVrwa5Eb8RdCDHPtHHDFPULuFT6XszztKxDm04Tl0PbVspVlb1c8MJV3nO9AUIGYOrF-a25AhTqh5fkfxLru1YDQy-_WNPlndt_qe0RL2w1TXx4q2BTjcT8QTNdvCMLcrOeMlHBobiMZxyuLTDnsETZEVas-ZbMcFE",
                    maxScore = 10,
                    score = 10,
                ),
                ScoreQuestionUiState(
                    id = "q2",
                    number = 2,
                    prompt = "Demonstrate respiratory rate check.",
                    imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDszUUOe0BJp_rDfG5tVDvSjvmpSeYN3S1dqyy_33VYuqrhjA3L-wrMxwf0hodm_uVJTKthuWhlMXU7qvQrFUYwNRqp15gDn1RQo8tSVytKeRnzJJCBTU9uomDVW24KWktFGuwv2szxasjOAabeUeefRZ4ImVqipQFE7keES-tEjmTaXZW42VqrdRK_J795HFqIlwr1pdAjlyYHwA1Dds6BOoA7SdQi_NNXlL_qqg_wn7d8ZAnjIGdwPqeTKk_ZqTOJTwsNN3sRabYe",
                    maxScore = 10,
                    score = 0,
                ),
            ),
        ),
    )
    val uiState: StateFlow<AssessmentPracticalScoringUiState> = _uiState.asStateFlow()

    fun onIncrement(questionId: String) {
        adjustScore(questionId, +1)
    }

    fun onDecrement(questionId: String) {
        adjustScore(questionId, -1)
    }

    private fun adjustScore(questionId: String, delta: Int) {
        _uiState.update { state ->
            state.copy(
                questions = state.questions.map { q ->
                    if (q.id != questionId) q
                    else q.copy(score = (q.score + delta).coerceIn(0, q.maxScore))
                },
            )
        }
    }

    /** Placeholder title resolution while real data isn't wired. */
    private fun sectionTitleFor(id: String): String = when (id.uppercase()) {
        "A" -> "Section A — Vital Signs"
        "B" -> "Section B — Clinical Diagnosis"
        "C" -> "Section C — Ethical Standards"
        else -> "Section $id"
    }
}
