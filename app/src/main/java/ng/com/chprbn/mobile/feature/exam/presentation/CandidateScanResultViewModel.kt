package ng.com.chprbn.mobile.feature.exam.presentation

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ng.com.chprbn.mobile.feature.exam.domain.usecase.LookupCandidateByExamNumberUseCase
import javax.inject.Inject

/**
 * Resolves the scanned QR payload against the local candidate roster
 * via [LookupCandidateByExamNumberUseCase]. If a candidate is found,
 * the UiState's name + exam-number fields are populated; otherwise the
 * placeholder content seeded by `fromScannedPayload` stays.
 *
 * Mark-attendance is still a nav-only gesture in the Screen layer
 * because the paperId needed to commit attendance comes from the
 * caller's session context, which isn't wired in this VM today.
 */
@HiltViewModel
class CandidateScanResultViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext context: Context,
    private val lookupCandidate: LookupCandidateByExamNumberUseCase,
) : ViewModel() {

    private val scannedPayload: String =
        savedStateHandle.get<String>("scannedPayload")?.let(Uri::decode).orEmpty()

    private val _uiState = MutableStateFlow(
        CandidateScanResultUiState.fromScannedPayload(
            scannedPayload = scannedPayload,
            context = context,
        ),
    )
    val uiState: StateFlow<CandidateScanResultUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val candidate = lookupCandidate(scannedPayload)
            if (candidate != null) {
                _uiState.update {
                    it.copy(
                        candidateName = candidate.fullName,
                        examNumberLine = context.getString(
                            ng.com.chprbn.mobile.R.string.candidate_scan_exam_number_format,
                            candidate.examNumber,
                        ),
                    )
                }
            }
        }
    }
}
