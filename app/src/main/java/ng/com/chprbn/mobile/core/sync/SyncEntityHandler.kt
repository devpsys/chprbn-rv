package ng.com.chprbn.mobile.core.sync

import dagger.MapKey

/**
 * Contributed by each feature module via Hilt multibinding (`@IntoMap` +
 * [SyncEntityTypeKey]) so the worker can dispatch a queued row to the right
 * feature without taking a compile-time dependency on it.
 *
 * Implementations live in the feature's `data` package — they own the local
 * row read (to materialise the payload), the remote upload call, and the
 * post-upload `syncStatus` flip on their own entity table.
 *
 * Return [SyncOutcome.Success] when the server has accepted the row and the
 * local row has been marked `Synced`. Return [SyncOutcome.Failure] with a
 * human-readable message on any expected error; the worker will increment
 * `attemptCount` and the WorkManager backoff will reschedule.
 *
 * Implementations must not throw; they wrap their own try/catch and translate
 * to [SyncOutcome.Failure]. Any leaked exception is treated by the worker as
 * a transient failure with `t.message ?: "Unknown error"`.
 */
fun interface SyncEntityHandler {
    suspend fun upload(entityKey: String): SyncOutcome
}

sealed interface SyncOutcome {
    data object Success : SyncOutcome
    data class Failure(val message: String) : SyncOutcome
}

/**
 * Dagger multibinding key for [SyncEntityHandler] contributions. Used like:
 *
 * ```
 * @Binds @IntoMap @SyncEntityTypeKey(SyncEntityType.Attendance)
 * abstract fun bindAttendanceHandler(impl: AttendanceSyncHandler): SyncEntityHandler
 * ```
 */
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class SyncEntityTypeKey(val value: SyncEntityType)
