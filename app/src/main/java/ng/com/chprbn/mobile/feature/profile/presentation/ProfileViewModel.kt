package ng.com.chprbn.mobile.feature.profile.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ng.com.chprbn.mobile.R
import ng.com.chprbn.mobile.feature.profile.domain.usecase.GetUserProfileUseCase
import ng.com.chprbn.mobile.feature.profile.domain.usecase.LogoutUseCase
import javax.inject.Inject

/**
 * Profile ViewModel. Loads user from local cache; handles logout (clears DB, emits LoggedOut).
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val logoutUseCase: LogoutUseCase,
    @ApplicationContext private val context: Context,
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
                    else ProfileUiState.Error(context.getString(R.string.profile_error_not_logged_in))
                }
                .onFailure { e ->
                    _state.value = ProfileUiState.Error(
                        e.message ?: context.getString(R.string.profile_error_unknown),
                    )
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            runCatching { logoutUseCase() }
                .onSuccess { _state.value = ProfileUiState.LoggedOut }
                .onFailure { e ->
                    _state.value = ProfileUiState.Error(
                        e.message ?: context.getString(R.string.profile_error_logout_failed),
                    )
                }
        }
    }
}
