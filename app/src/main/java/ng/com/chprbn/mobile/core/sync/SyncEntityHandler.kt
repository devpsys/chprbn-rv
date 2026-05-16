package ng.com.chprbn.mobile.core.sync

import dagger.MapKey

/**
 * Contributed by each feature module via Hilt multibinding (`@IntoMap` +
 * [SyncEntityTypeKey]) so the worker can dispatch queued rows to the right
 * feature without taking a compile-time dependency on it.
 *
 * Contract:
 *
 * - The runner groups all currently-pending jobs by [SyncEntityType] and
 *   calls [uploadBatch] **once per type** with the whole list of entity
 *   keys for that type. Implementations send one batched HTTP request
 *   per call (matches the batched server contract — see
 *   `docs/api/full-api-documentation.md` §10.3).
 * - The returned map MUST contain exactly one entry per input key. A
 *   missing key is treated by the runner as a contract violation and
 *   surfaced as a synthetic [SyncOutcome.Failure]; that should never
 *   happen in practice.
 * - Implementations must not throw; they wrap their own try/catch and
 *   translate to per-key [SyncOutcome.Failure]. A leaked exception is
 *   caught by the runner and applied to every key in the batch as a
 *   transient failure.
 *
 * Implementations live in the feature's `data` package — they own the
 * local row read, the remote upload call, and the post-upload
 * `syncStatus` flip on their own entity tables.
 */
fun interface SyncEntityHandler {
    suspend fun uploadBatch(entityKeys: List<String>): Map<String, SyncOutcome>
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
