package ng.com.chprbn.mobile.feature.exam.presentation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ExamStatisticsViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(ExamStatisticsUiState.placeholder())
    val uiState: StateFlow<ExamStatisticsUiState> = _uiState.asStateFlow()

    // Wire statistics / sync repository when backend is ready.
}
