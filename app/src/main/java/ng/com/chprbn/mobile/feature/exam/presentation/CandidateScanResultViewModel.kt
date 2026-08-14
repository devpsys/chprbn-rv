package ng.com.chprbn.mobile.feature.exam.presentation

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ng.com.chprbn.mobile.R
import ng.com.chprbn.mobile.feature.exam.domain.model.AttendanceStatus
import ng.com.chprbn.mobile.feature.exam.domain.model.MarkAttendanceResult
import ng.com.chprbn.mobile.feature.exam.domain.usecase.GetAttendanceStatusUseCase
import ng.com.chprbn.mobile.feature.exam.domain.usecase.LookupCandidateByExamNumberUseCase
import ng.com.chprbn.mobile.feature.exam.domain.usecase.MarkAttendanceUseCase
import javax.inject.Inject

sealed interface MarkAttendanceUiState {
    data object Idle : MarkAttendanceUiState
    data object Marking : MarkAttendanceUiState
    data class Error(val message: String) : MarkAttendanceUiState
}

/**
 * Resolves the scanned QR payload against the local candidate roster
 * via [LookupCandidateByExamNumberUseCase]. If a candidate is found,
 * the UiState's name + exam-number + photo fields are populated;
 * otherwise the placeholder content seeded by `fromScannedPayload` stays.
 *
 * The "Mark Attendance" FAB toggles: if [GetAttendanceStatusUseCase] reports
 * the candidate is already [AttendanceStatus.SignedIn] for this paper, the
 * FAB signs them out instead (and vice versa) — [MarkAttendanceUseCase] is
 * called with whichever status is the opposite of their current one. The
 * [paperId] is carried in via nav args (from
 * [ng.com.chprbn.mobile.core.navigation.Routes.CandidateScanResult]).
 * [attendanceMarked] fires once on success so the Screen layer can leave
 * this destination; [markAttendanceState] drives the in-progress/error UI.
 */
@HiltViewModel
class CandidateScanResultViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
    private val lookupCandidate: LookupCandidateByExamNumberUseCase,
    private val getAttendanceStatus: GetAttendanceStatusUseCase,
    private val markAttendance: MarkAttendanceUseCase,
) : ViewModel() {

    private val paperId: String = savedStateHandle.get<String>("paperId").orEmpty()
    private val scannedPayload: String =
        savedStateHandle.get<String>("scannedPayload")?.let(Uri::decode).orEmpty()

    private var resolvedCandidateId: String? = null

    private val _uiState = MutableStateFlow(
        CandidateScanResultUiState.fromScannedPayload(
            scannedPayload = scannedPayload,
            context = context,
        ),
    )
    val uiState: StateFlow<CandidateScanResultUiState> = _uiState.asStateFlow()

    private val _markAttendanceState =
        MutableStateFlow<MarkAttendanceUiState>(MarkAttendanceUiState.Idle)
    val markAttendanceState: StateFlow<MarkAttendanceUiState> = _markAttendanceState.asStateFlow()

    private val _attendanceMarked = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val attendanceMarked: SharedFlow<Unit> = _attendanceMarked.asSharedFlow()

    init {
        viewModelScope.launch {
            val candidate = lookupCandidate(scannedPayload)
            if (candidate != null) {
                resolvedCandidateId = candidate.id
                val status = getAttendanceStatus(paperId, candidate.id)
                _uiState.update {
                    it.copy(
                        candidateName = candidate.fullName,
                        examNumberLine = context.getString(
                            R.string.candidate_scan_exam_number_format,
                            candidate.examNumber,
                        ),
                        photoUrl = candidate.photoUrl,
                        isSignedIn = status == AttendanceStatus.SignedIn,
                    )
                }
            }
        }
    }

    fun onMarkAttendance() {
        if (_markAttendanceState.value is MarkAttendanceUiState.Marking) return
        val candidateId = resolvedCandidateId
        if (candidateId == null) {
            _markAttendanceState.value = MarkAttendanceUiState.Error(
                context.getString(R.string.candidate_scan_mark_attendance_error_no_candidate),
            )
            return
        }
        viewModelScope.launch {
            _markAttendanceState.value = MarkAttendanceUiState.Marking
            // Re-check the freshest status right before writing (not the
            // init-time snapshot) so a stale screen doesn't sign someone
            // back in when they were already signed out elsewhere.
            val currentStatus = getAttendanceStatus(paperId, candidateId)
            val targetStatus = if (currentStatus == AttendanceStatus.SignedIn) {
                AttendanceStatus.SignedOut
            } else {
                AttendanceStatus.SignedIn
            }
            when (val result = markAttendance(paperId, candidateId, targetStatus)) {
                is MarkAttendanceResult.Success -> {
                    _uiState.update { it.copy(isSignedIn = targetStatus == AttendanceStatus.SignedIn) }
                    _markAttendanceState.value = MarkAttendanceUiState.Idle
                    _attendanceMarked.emit(Unit)
                }
                is MarkAttendanceResult.Error -> {
                    _markAttendanceState.value = MarkAttendanceUiState.Error(result.message)
                }
            }
        }
    }

    fun dismissMarkAttendanceError() {
        _markAttendanceState.value = MarkAttendanceUiState.Idle
    }
}
