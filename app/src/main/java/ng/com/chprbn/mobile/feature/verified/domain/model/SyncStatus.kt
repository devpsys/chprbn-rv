package ng.com.chprbn.mobile.feature.verified.domain.model

/**
 * Sync state for locally stored verified records.
 * UI can treat anything not yet synced as "Syncing".
 */
enum class SyncStatus {
    Pending,
    Synced
}

