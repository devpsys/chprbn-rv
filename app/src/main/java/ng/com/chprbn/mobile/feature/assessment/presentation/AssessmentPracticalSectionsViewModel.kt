package ng.com.chprbn.mobile.feature.assessment.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ng.com.chprbn.mobile.feature.assessment.domain.model.PracticalSectionCadre
import ng.com.chprbn.mobile.feature.assessment.domain.model.PracticalSectionSummary
import ng.com.chprbn.mobile.feature.assessment.domain.usecase.GetPracticalSectionsUseCase
import ng.com.chprbn.mobile.feature.assessment.domain.usecase.LookupAssessmentCandidateUseCase
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

/**
 * Loads the candidate's profile (for the screen header) and the per-section
 * scoring summary for the hub cards. Sections are filtered to the cadre
 * encoded in the candidate exam number (see [PracticalSectionCadre]) so a
 * CHEW candidate never sees JCHEW/CHO/BCHS cards. Footer text follows the
 * design:
 *
 * - Complete sections: HH:mm timestamp of the most recent score in the section.
 * - Incomplete sections: count of remaining questions (raw integer; the
 *   screen formats via strings.xml).
 * - NotStarted sections: empty string.
 */
@HiltViewModel
class AssessmentPracticalSectionsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val lookupCandidate: LookupAssessmentCandidateUseCase,
    private val getSections: GetPracticalSectionsUseCase,
) : ViewModel() {

    private val scheduleId: String = savedStateHandle.get<String>("scheduleId").orEmpty()
    // Nav arg is named `candidateId` for historical reasons, but the value
    // is the QR-extracted registration/exam number — resolved to a domain
    // id below via [LookupAssessmentCandidateUseCase].
    private val scannedPayload: String = savedStateHandle.get<String>("candidateId").orEmpty()

    private val _uiState = MutableStateFlow(AssessmentPracticalSectionsUiState())
    val uiState: StateFlow<AssessmentPracticalSectionsUiState> = _uiState.asStateFlow()

    init {
        // Resolve the scanned exam number → domain candidate first, then
        // (only if that succeeds) start collecting sections keyed on the
        // real DB id. Kicking off both flows in parallel with the raw
        // scan payload was the original bug — sections joined on the
        // wrong id and always came back empty.
        viewModelScope.launch {
            val candidate = lookupCandidate(scheduleId, scannedPayload)
            if (candidate == null) {
                _uiState.update {
                    it.copy(
                        candidateExamId = scannedPayload,
                        candidateNotFound = true,
                    )
                }
                return@launch
            }
            _uiState.update {
                it.copy(
                    candidateName = candidate.fullName,
                    candidateExamId = candidate.examNumber,
                    candidatePhotoUrl = candidate.photoUrl,
                )
            }
            getSections.observe(scheduleId, candidate.id).collectLatest { summaries ->
                val visible = PracticalSectionCadre.filter(summaries, candidate.examNumber)
                val done = visible.count {
                    it.status ==
                        ng.com.chprbn.mobile.feature.assessment.domain.model.PracticalSectionStatus.Complete
                }
                _uiState.update {
                    it.copy(
                        sectionsDone = done,
                        sectionsTotal = visible.size,
                        sectionsRemaining = (visible.size - done).coerceAtLeast(0),
                        sections = visible.map { summary -> summary.toSectionUi() },
                    )
                }
            }
        }
    }

    private fun PracticalSectionSummary.toSectionUi(): PracticalSectionUiState =
        PracticalSectionUiState(
            id = section.id,
            sectionTitle = "Section ${section.title}",
            sectionSubtitle = section.subtitle,
            status = when (status) {
                ng.com.chprbn.mobile.feature.assessment.domain.model.PracticalSectionStatus.Complete ->
                    PracticalSectionStatus.Complete
                ng.com.chprbn.mobile.feature.assessment.domain.model.PracticalSectionStatus.Incomplete ->
                    PracticalSectionStatus.Incomplete
                ng.com.chprbn.mobile.feature.assessment.domain.model.PracticalSectionStatus.NotStarted ->
                    PracticalSectionStatus.NotStarted
            },
            footerText = when (status) {
                ng.com.chprbn.mobile.feature.assessment.domain.model.PracticalSectionStatus.Complete ->
                    lastUpdatedAt?.let { TIME_FORMATTER.format(Instant.ofEpochMilli(it)) }.orEmpty()
                ng.com.chprbn.mobile.feature.assessment.domain.model.PracticalSectionStatus.Incomplete ->
                    (totalCount - scoredCount).coerceAtLeast(0).toString()
                ng.com.chprbn.mobile.feature.assessment.domain.model.PracticalSectionStatus.NotStarted ->
                    ""
            },
        )

    private companion object {
        val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter
            .ofPattern("h:mm a", Locale.US)
            .withZone(ZoneId.systemDefault())
    }
}
