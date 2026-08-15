package ng.com.chprbn.mobile.feature.exam.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ng.com.chprbn.mobile.feature.exam.domain.model.AttendanceStatus
import ng.com.chprbn.mobile.feature.exam.domain.model.Remark
import ng.com.chprbn.mobile.feature.exam.domain.model.SaveResult
import ng.com.chprbn.mobile.feature.exam.domain.usecase.ClearRemarksForCandidateUseCase
import ng.com.chprbn.mobile.feature.exam.domain.usecase.GetAttendanceStatusUseCase
import ng.com.chprbn.mobile.feature.exam.domain.usecase.GetCandidateByIdUseCase
import ng.com.chprbn.mobile.feature.exam.domain.usecase.GetRemarksForCandidateUseCase
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

/** Drives the "Clear All Remarks" destructive action. Mirrors `ClearCacheUiState`'s shape. */
sealed interface ClearRemarksUiState {
    data object Idle : ClearRemarksUiState
    data object WarningShown : ClearRemarksUiState
    data object Clearing : ClearRemarksUiState
    data object Success : ClearRemarksUiState
    data class Error(val message: String) : ClearRemarksUiState
}

/**
 * Candidate profile reached from "View Profile" on the roster —
 * candidate identity, their attendance status for [paperId] (the
 * roster they were opened from — same vocabulary and lookup as the
 * scan-result screen's [GetAttendanceStatusUseCase]), and their full
 * remark history ([GetRemarksForCandidateUseCase], not scoped to any
 * one paper).
 *
 * [clearRemarksState] gates [ClearRemarksForCandidateUseCase] behind a
 * warning dialog (same contract as [ExamStatisticsViewModel]'s cache
 * clear) — the officer must confirm before every remark on file for
 * this candidate is permanently deleted.
 */
@HiltViewModel
class CandidateProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getCandidateById: GetCandidateByIdUseCase,
    private val getRemarks: GetRemarksForCandidateUseCase,
    private val getAttendanceStatus: GetAttendanceStatusUseCase,
    private val clearRemarks: ClearRemarksForCandidateUseCase,
) : ViewModel() {

    private val candidateId: String = savedStateHandle.get<String>("candidateId").orEmpty()
    private val paperId: String = savedStateHandle.get<String>("paperId").orEmpty()

    private val _uiState = MutableStateFlow(CandidateProfileUiState.placeholder())
    val uiState: StateFlow<CandidateProfileUiState> = _uiState.asStateFlow()

    private val _clearRemarksState = MutableStateFlow<ClearRemarksUiState>(ClearRemarksUiState.Idle)
    val clearRemarksState: StateFlow<ClearRemarksUiState> = _clearRemarksState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val candidate = getCandidateById(candidateId)
            if (candidate == null) {
                _uiState.update {
                    it.copy(hasLoaded = true, notFound = true, remarks = emptyList())
                }
                return@launch
            }
            val remarks = getRemarks(candidateId).map { it.toRowUi() }
            val status = getAttendanceStatus(paperId, candidateId)
            _uiState.value = CandidateProfileUiState(
                candidateName = candidate.fullName,
                examNumberLabel = "ID: ${candidate.examNumber}",
                photoUrl = candidate.photoUrl,
                remarks = remarks,
                statusPillLabel = status.toPillLabel(),
                hasLoaded = true,
                notFound = false,
            )
        }
    }

    fun onClearAllClicked() {
        if (_clearRemarksState.value !is ClearRemarksUiState.Clearing) {
            _clearRemarksState.value = ClearRemarksUiState.WarningShown
        }
    }

    fun onClearAllConfirmed() {
        if (_clearRemarksState.value is ClearRemarksUiState.Clearing) return
        _clearRemarksState.value = ClearRemarksUiState.Clearing
        viewModelScope.launch {
            _clearRemarksState.value = when (val result = clearRemarks(candidateId)) {
                SaveResult.Success -> {
                    refresh()
                    ClearRemarksUiState.Success
                }
                is SaveResult.Error -> ClearRemarksUiState.Error(result.message)
            }
        }
    }

    fun onClearAllDismissed() {
        if (_clearRemarksState.value !is ClearRemarksUiState.Clearing) {
            _clearRemarksState.value = ClearRemarksUiState.Idle
        }
    }

    // Same vocabulary as ExamCandidatesViewModel.toCardUi's status pill.
    private fun AttendanceStatus?.toPillLabel(): String = when (this) {
        AttendanceStatus.SignedIn -> "Signed In"
        AttendanceStatus.SignedOut -> "Signed Out"
        AttendanceStatus.Flagged -> "Flagged"
        null -> "Pending"
    }

    private fun Remark.toRowUi(): RemarkRowUiState = RemarkRowUiState(
        id = id,
        body = body,
        severity = severity,
        createdAtLabel = if (createdAt > 0L) {
            DATE_FORMATTER.format(Instant.ofEpochMilli(createdAt))
        } else {
            ""
        },
    )

    private companion object {
        val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter
            .ofPattern("MMM d, yyyy 'at' h:mm a", Locale.US)
            .withZone(ZoneId.systemDefault())
    }
}
