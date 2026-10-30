package ng.com.chprbn.mobile.feature.accreditation.presentation

/**
 * Local draft lifecycle for an SSQ section. Grades / quotas are never
 * derived from these — they only describe save/sync state on device.
 */
enum class SectionDraftStatus {
    NotStarted,
    InProgress,
    SavedLocally,
    PendingSync,
    Synced,
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
