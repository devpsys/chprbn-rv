package ng.com.chprbn.mobile.feature.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ng.com.chprbn.mobile.feature.profile.domain.usecase.GetUserProfileUseCase
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            val user = runCatching { getUserProfileUseCase() }.getOrNull() ?: return@launch
            _uiState.update {
                it.copy(
                    userName = user.fullName?.takeIf(String::isNotBlank) ?: user.username,
                    userEmail = user.email,
                    userStatus = user.role.orEmpty(),
                    profileImageUrl = user.userPhoto
                )
            }
        }
    }
}
