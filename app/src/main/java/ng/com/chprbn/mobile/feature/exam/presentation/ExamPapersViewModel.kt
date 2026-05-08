package ng.com.chprbn.mobile.feature.exam.presentation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ExamPapersViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(ExamPapersUiState.placeholder())
    val uiState: StateFlow<ExamPapersUiState> = _uiState.asStateFlow()

    // Wire attendance APIs when backend is ready.
}

