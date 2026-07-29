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
import ng.com.chprbn.mobile.feature.auth.data.network.SessionTokenPolicy
import ng.com.chprbn.mobile.feature.profile.domain.usecase.GetUserProfileUseCase
import javax.inject.Inject

/** Destination after splash: navigate to Dashboard if session exists, else login. */
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
            // Run the auth check in parallel with the splash animation floor
            // so the user waits at most `SPLASH_HOLD_MS` — a fast check
            // doesn't tack on a 2.5s delay it already spent (A7 audit fix).
            // If the check takes longer than the floor, we simply dispatch as
            // soon as it lands.
            val start = System.currentTimeMillis()
            val user = getUserProfileUseCase()
            val destination = if (
                user != null && SessionTokenPolicy.isValidForAuthenticatedApi(user.accessToken)
            ) {
                authTokenStore.setToken(user.accessToken.trim())
                SplashDestination.Dashboard
            } else {
                authTokenStore.clear()
                SplashDestination.Login
            }
            val remaining = SPLASH_HOLD_MS - (System.currentTimeMillis() - start)
            if (remaining > 0) delay(remaining)
            _destination.value = destination
        }
    }

    private companion object {
        /** Matches the splash progress-bar animation duration. */
        const val SPLASH_HOLD_MS = 2500L
    }
}
