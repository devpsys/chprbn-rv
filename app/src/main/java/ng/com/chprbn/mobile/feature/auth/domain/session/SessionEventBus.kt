package ng.com.chprbn.mobile.feature.auth.domain.session

import android.util.Log
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * App-wide one-shot signal for session lifecycle events. [AuthorizationInterceptor]
 * emits [SessionEvent.Expired] when an authenticated request returns 401; the nav
 * host collects and routes the user back to login with a friendly toast.
 *
 * `tryEmit` with extraBufferCapacity = 1 keeps the bus non-blocking on the
 * OkHttp thread; overflow drops the oldest pending event because back-pressing
 * an interceptor would hang every in-flight network call.
 */
@Singleton
class SessionEventBus @Inject constructor() {
    private val _events = MutableSharedFlow<SessionEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<SessionEvent> = _events.asSharedFlow()

    fun emit(event: SessionEvent) {
        val accepted = _events.tryEmit(event)
        Log.w(TAG, "emit($event) accepted=$accepted subscribers=${_events.subscriptionCount.value}")
    }

    private companion object {
        const val TAG = "SessionExpiry"
    }
}
