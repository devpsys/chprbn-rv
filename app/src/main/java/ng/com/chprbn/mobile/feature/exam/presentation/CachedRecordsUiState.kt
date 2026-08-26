package ng.com.chprbn.mobile.feature.exam.presentation

import ng.com.chprbn.mobile.feature.exam.domain.model.CachedRecordEntry
import ng.com.chprbn.mobile.feature.exam.domain.model.RecordType

/** Which tab is currently rendered on the Cached Records screen. */
enum class CachedRecordsTab {
    /** `syncStatus == Pending` — captured on-device, awaiting upload. */
    Pending,
    /** `syncStatus in (Failed, Abandoned)` — server-rejected or retry-capped. */
    Failed,
}

data class CachedRecordsUiState(
    val selectedTab: CachedRecordsTab = CachedRecordsTab.Pending,
    val pendingCount: Int = 0,
    val failedCount: Int = 0,
    /**
     * Records for the currently-selected tab, post-filter. The tab
     * counters above are the pre-filter totals so the user can see
     * "Failed (3)" while filtering the list down to zero visible rows.
     */
    val visibleEntries: List<CachedRecordEntry> = emptyList(),
    val searchQuery: String = "",
    /**
     * Empty set == "no filter, show every record type". Non-empty ==
     * "show only these types". Kept as a Set rather than a Boolean per
     * type because a chip row communicates the "any of" intent better
     * than three separate flags.
     */
    val recordTypeFilters: Set<RecordType> = emptySet(),
    val isLoading: Boolean = true,
    /**
     * One-shot toast target for the copy-error action. VM clears it via
     * [onCopyToastShown] once the screen has rendered the toast.
     */
    val copyToastMessage: String? = null,
)
