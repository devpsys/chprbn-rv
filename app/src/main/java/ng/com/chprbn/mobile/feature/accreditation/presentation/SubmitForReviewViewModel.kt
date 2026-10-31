package ng.com.chprbn.mobile.feature.accreditation.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ng.com.chprbn.mobile.feature.accreditation.domain.model.SubmitAssessmentResult
import ng.com.chprbn.mobile.feature.accreditation.domain.usecase.GetAssessmentOverviewUseCase
import ng.com.chprbn.mobile.feature.accreditation.domain.usecase.SubmitAssessmentUseCase
import ng.com.chprbn.mobile.feature.accreditation.domain.model.GetOverviewResult

@HiltViewModel
class SubmitForReviewViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val submitAssessment: SubmitAssessmentUseCase,
    private val getOverview: GetAssessmentOverviewUseCase,
) : ViewModel() {

    private val institutionId: String =
        savedStateHandle.get<String>("institutionId").orEmpty()
    private val assessmentIdArg: String =
        savedStateHandle.get<String>("assessmentId").orEmpty()

    private val _uiState = MutableStateFlow<SubmitForReviewUiState>(
        if (institutionId.isBlank()) {
            SubmitForReviewUiState.Error("Missing institution")
        } else {
            SubmitForReviewUiState.Loading
        },
    )
    val uiState: StateFlow<SubmitForReviewUiState> = _uiState.asStateFlow()

    init {
        if (institutionId.isNotBlank()) {
            viewModelScope.launch { load() }
        }
    }

    private suspend fun load() {
        val overview = when (val result = getOverview(institutionId)) {
            is GetOverviewResult.Success -> result.overview
            is GetOverviewResult.Error -> null
        }
        val assessmentId = assessmentIdArg.ifBlank {
            overview?.assessmentId ?: "assess-$institutionId"
        }
        val placeholder = SubmitForReviewUiState.placeholder(
            institutionId = institutionId,
            assessmentId = assessmentId,
        ).data
        _uiState.value = SubmitForReviewUiState.Success(
            if (overview == null) {
                placeholder
            } else {
                placeholder.copy(
                    institutionName = overview.institutionName,
                    locationLabel = overview.locationLabel,
                    facilityCode = overview.facilityCode,
                    cycleLabel = overview.cycleLabel,
                    sectionsCompleted = overview.sections.count {
                        it.status != ng.com.chprbn.mobile.feature.accreditation.domain.model.DraftPersistenceStatus.NotStarted &&
                            it.status != ng.com.chprbn.mobile.feature.accreditation.domain.model.DraftPersistenceStatus.InProgress
                    },
                    sectionsTotal = overview.sections.size.coerceAtLeast(6),
                    assessmentId = assessmentId,
                )
            },
        )
    }

    fun onSubmitAssessment() {
        val success = _uiState.value as? SubmitForReviewUiState.Success ?: return
        updateSuccess { it.copy(isSubmitting = true, showErrorDialog = false) }
        viewModelScope.launch {
            when (val result = submitAssessment(success.data.assessmentId)) {
                is SubmitAssessmentResult.Success -> updateSuccess {
                    it.copy(
                        isSubmitting = false,
                        requestResultNavigation = true,
                        submittedDecisionId = result.decisionId,
                    )
                }
                is SubmitAssessmentResult.Error -> updateSuccess {
                    it.copy(isSubmitting = false, showErrorDialog = true)
                }
            }
        }
    }

    fun consumeResultNavigation() {
        updateSuccess { it.copy(requestResultNavigation = false) }
    }

    fun dismissErrorDialog() {
        updateSuccess { it.copy(showErrorDialog = false) }
    }

    fun retrySubmit() {
        dismissErrorDialog()
        onSubmitAssessment()
    }

    private fun updateSuccess(transform: (SubmitForReviewData) -> SubmitForReviewData) {
        _uiState.update { state ->
            val success = state as? SubmitForReviewUiState.Success ?: return@update state
            SubmitForReviewUiState.Success(transform(success.data))
        }
    }
}
