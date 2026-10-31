package ng.com.chprbn.mobile.feature.accreditation.domain.model

/**
 * Canonical local draft lifecycle for accreditation assessment pieces.
 * Presentation [ng.com.chprbn.mobile.feature.accreditation.presentation.SectionDraftStatus]
 * maps 1:1 from this enum.
 */
enum class DraftPersistenceStatus {
    NotStarted,
    InProgress,
    SavedLocally,
    PendingSync,
    Synced,
    SyncFailed,
}

enum class SsqSectionKey(val key: String) {
    A("A"),
    B("B"),
    C("C"),
    D("D"),
    E("E"),
    F("F"),
    ;

    companion object {
        fun fromKey(raw: String): SsqSectionKey =
            entries.firstOrNull { it.key.equals(raw.trim(), ignoreCase = true) } ?: A
    }
}

enum class InstitutionAssignmentSyncStatus {
    Synced,
    DraftPendingSync,
    SyncFailed,
    NeverSynced,
}

enum class AttachmentUploadState {
    LocalOnly,
    PendingUpload,
    Uploaded,
    Failed,
}
