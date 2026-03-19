package ng.com.chprbn.mobile.feature.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ng.com.chprbn.mobile.feature.profile.domain.usecase.GetUserProfileUseCase
import ng.com.chprbn.mobile.feature.profile.domain.usecase.LogoutUseCase
import javax.inject.Inject

/**
 * Profile ViewModel. Loads user from local cache; handles logout (clears DB, emits LoggedOut).
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _state.value = ProfileUiState.Loading
            runCatching { getUserProfileUseCase() }
                .onSuccess { user ->
                    _state.value = if (user != null) ProfileUiState.Success(user)
                    else ProfileUiState.Error("Not logged in")
                }
                .onFailure { e ->
                    _state.value = ProfileUiState.Error(e.message ?: "Unknown error")
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            runCatching { logoutUseCase() }
                .onSuccess { _state.value = ProfileUiState.LoggedOut }
                .onFailure { e ->
                    _state.value = ProfileUiState.Error(e.message ?: "Logout failed")
                }
        }
    }
}
