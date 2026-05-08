package ng.com.chprbn.mobile.feature.exam.presentation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ExamPaperViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(ExamPaperUiState.placeholder())
    val uiState: StateFlow<ExamPaperUiState> = _uiState.asStateFlow()

    // Wire paper / session APIs when backend is ready.
}
