package ng.com.chprbn.mobile.core.session

/**
 * Marker interface that every feature contributes an implementation of via
 * Hilt multibinding (`@Binds @IntoSet` in the feature's data module). The
 * [SessionCleaner] runs them all on logout / clear-cache so that a second
 * user on the same device never inherits the previous user's cached rows
 * (A2 audit finding — was a real cross-user data leak).
 *
 * Contract:
 *
 * - Implementations must be idempotent (safe to invoke on an already-empty
 *   feature DB).
 * - Implementations must not throw; wrap DB / IO failures and return
 *   silently. The cleaner logs and moves on so one feature's failure
 *   doesn't leave the others' rows in place.
 */
fun interface SessionScopedCleaner {
    suspend fun clear()
}
