package ng.com.chprbn.mobile.feature.assessment.data.mappers

import ng.com.chprbn.mobile.core.domain.model.PaperKind
import ng.com.chprbn.mobile.core.domain.model.SyncStatus

/**
 * Shared bridges between domain enums and their Room/wire `String`
 * representation. Centralised so a typo in a stored value (manual SQL,
 * older app version, corrupted row) never crashes a `valueOf` lookup at
 * read time — instead the row is degraded to a safe default.
 *
 * `internal` so the bridges are visible across the mapper files in this
 * package but not from the rest of the app.
 */
internal fun SyncStatus.toDbValue(): String = name

internal fun String.toSyncStatus(): SyncStatus =
    runCatching { SyncStatus.valueOf(this) }.getOrDefault(SyncStatus.Pending)

internal fun PaperKind.toDbValue(): String = name

internal fun String.toPaperKind(): PaperKind =
    runCatching { PaperKind.valueOf(this) }.getOrDefault(PaperKind.Theory)
