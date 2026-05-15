package ng.com.chprbn.mobile.core.sync

/**
 * Discriminator for the polymorphic [SyncJobEntity.entityType] column.
 *
 * Stored as `name` in Room. Adding a new variant is non-breaking; removing one
 * requires a migration that purges or remaps existing rows.
 *
 * Each entry maps to a [SyncEntityHandler] contributed via Hilt multibinding by
 * the owning feature module. The [SyncWorker] dispatches one row at a time to
 * the handler matching the row's `entityType`.
 */
enum class SyncEntityType {
    Attendance,
    Remark,
    PracticalScore,
    ProjectScore,
    VerifiedLicense,
}
