package ng.com.chprbn.mobile.feature.exam.data.mappers

import ng.com.chprbn.mobile.core.domain.model.PaperKind
import ng.com.chprbn.mobile.core.domain.model.SyncStatus
import ng.com.chprbn.mobile.feature.exam.domain.model.AttendanceStatus
import ng.com.chprbn.mobile.feature.exam.domain.model.RemarkSeverity

/**
 * Shared bridges between domain enums and their Room/wire `String`
 * representation. Centralised so a typo in a stored value (manual SQL,
 * older app version, corrupted row) never crashes a `valueOf` lookup at
 * read time — instead the row is degraded to a safe default.
 *
 * Mirrors the assessment-side `EnumBridges` deliberately; per-feature
 * scoping (`internal`) keeps imports unambiguous between features.
 */
internal fun SyncStatus.toDbValue(): String = name

internal fun String.toSyncStatus(): SyncStatus =
    runCatching { SyncStatus.valueOf(this) }.getOrDefault(SyncStatus.Pending)

internal fun PaperKind.toDbValue(): String = name

internal fun String.toPaperKind(): PaperKind =
    runCatching { PaperKind.valueOf(this) }.getOrDefault(PaperKind.Theory)

internal fun AttendanceStatus.toDbValue(): String = name

internal fun String.toAttendanceStatus(): AttendanceStatus =
    runCatching { AttendanceStatus.valueOf(this) }.getOrDefault(AttendanceStatus.SignedOut)

internal fun RemarkSeverity.toDbValue(): String = name

internal fun String.toRemarkSeverity(): RemarkSeverity =
    runCatching { RemarkSeverity.valueOf(this) }.getOrDefault(RemarkSeverity.Info)
