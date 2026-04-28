package ng.com.chprbn.mobile.feature.exam.presentation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ExamDashboardViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(ExamDashboardUiState.placeholder())
    val uiState: StateFlow<ExamDashboardUiState> = _uiState.asStateFlow()

    // Wire exam session / candidate APIs and navigation events when backend is ready.
}
