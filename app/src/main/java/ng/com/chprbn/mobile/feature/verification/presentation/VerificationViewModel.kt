package ng.com.chprbn.mobile.feature.verification.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ng.com.chprbn.mobile.feature.verification.domain.usecase.GetVerificationDataUseCase
import javax.inject.Inject

/**
 * Presentation ViewModel. Exposes [VerificationUiState] via [state];
 * depends only on domain use case (single flow: UseCase → Repository → Local/Remote).
 */
@HiltViewModel
class VerificationViewModel @Inject constructor(
    private val getVerificationDataUseCase: GetVerificationDataUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<VerificationUiState>(VerificationUiState.Loading)
    val state: StateFlow<VerificationUiState> = _state.asStateFlow()

    init {
        loadVerification()
    }

    private fun loadVerification() {
        viewModelScope.launch {
            _state.value = VerificationUiState.Loading
            runCatching { getVerificationDataUseCase() }
                .onSuccess { data ->
                    _state.value = VerificationUiState.Success(
                        user = data.user,
                        features = data.features
                    )
                }
                .onFailure { e ->
                    _state.value = VerificationUiState.Error(e.message ?: "Unknown error")
                }
        }
    }

    fun retry() = loadVerification()
}
