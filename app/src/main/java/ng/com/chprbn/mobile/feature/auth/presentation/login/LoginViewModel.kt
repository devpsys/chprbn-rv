package ng.com.chprbn.mobile.feature.auth.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ng.com.chprbn.mobile.feature.auth.domain.model.AuthResult
import ng.com.chprbn.mobile.feature.auth.domain.model.User
import ng.com.chprbn.mobile.feature.auth.domain.usecase.LoginUseCase
import javax.inject.Inject

data class LoginUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val authenticatedUser: User? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun signIn(username: String, password: String) {
        val trimmedUsername = username.trim()
        if (trimmedUsername.isBlank() || password.isBlank()) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "License number and password are required.",
                    authenticatedUser = null
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null, authenticatedUser = null) }

        viewModelScope.launch {
            when (val result = loginUseCase(username = trimmedUsername, password = password)) {
                is AuthResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            authenticatedUser = result.user
                        )
                    }
                }

                is AuthResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message,
                            authenticatedUser = null
                        )
                    }
                }
            }
        }
    }

    fun consumeAuthSuccess() {
        _uiState.update { it.copy(authenticatedUser = null) }
    }
}
