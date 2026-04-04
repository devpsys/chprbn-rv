package ng.com.chprbn.mobile.feature.auth.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ng.com.chprbn.mobile.feature.auth.data.network.AuthTokenStore
import ng.com.chprbn.mobile.feature.profile.domain.usecase.GetUserProfileUseCase
import javax.inject.Inject

/** Destination after splash: navigate to dashboard if session exists, else login. */
enum class SplashDestination { Dashboard, Login }

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val authTokenStore: AuthTokenStore
) : ViewModel() {

    private val _destination = MutableStateFlow<SplashDestination?>(null)
    val destination: StateFlow<SplashDestination?> = _destination.asStateFlow()

    init {
        viewModelScope.launch {
            delay(2500L)
            val user = getUserProfileUseCase()
            if (user != null) {
                authTokenStore.setToken(user.accessToken)
                _destination.value = SplashDestination.Dashboard
            } else {
                authTokenStore.clear()
                _destination.value = SplashDestination.Login
            }
        }
    }
}
