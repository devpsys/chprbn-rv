package ng.com.chprbn.mobile.feature.auth.domain.session

sealed interface SessionEvent {
    data object Expired : SessionEvent
}
