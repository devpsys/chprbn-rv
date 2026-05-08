package ng.com.chprbn.mobile.feature.exam.presentation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ExamCandidatesViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(ExamCandidatesUiState.placeholder())
    val uiState: StateFlow<ExamCandidatesUiState> = _uiState.asStateFlow()

    // Wire candidate list / remarks APIs when backend is ready.
}

