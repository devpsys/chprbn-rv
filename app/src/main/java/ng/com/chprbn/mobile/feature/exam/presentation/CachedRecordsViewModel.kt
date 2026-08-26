package ng.com.chprbn.mobile.feature.exam.presentation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ng.com.chprbn.mobile.feature.exam.domain.model.CachedRecordEntry
import ng.com.chprbn.mobile.feature.exam.domain.model.RecordType
import ng.com.chprbn.mobile.feature.exam.domain.repository.CachedRecordsRepository
import javax.inject.Inject

/**
 * Backing VM for the Cached Records screen.
 *
 * The screen presents two tabs (Pending / Failed). Both counters are
 * always live — the officer sees "Pending (5) • Failed (2)" without
 * switching tabs, because both repository flows are collected in one
 * pass. Only the list body swaps.
 *
 * Search + record-type filters are applied on the JVM side because
 * pending/failed row counts are tens, not thousands — a per-emit
 * re-filter is cheap and keeps the DAO queries simple.
 */
@HiltViewModel
class CachedRecordsViewModel @Inject constructor(
    private val repository: CachedRecordsRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CachedRecordsUiState())
    val uiState: StateFlow<CachedRecordsUiState> = _uiState.asStateFlow()

    /**
     * Cached pre-filter snapshots of each tab's rows. Kept as a plain
     * field (not part of UiState) so a filter/search toggle can
     * re-apply against the last known good data without re-collecting
     * or exposing implementation state to the screen.
     */
    private val latestByTab: MutableMap<CachedRecordsTab, List<CachedRecordEntry>> =
        mutableMapOf(
            CachedRecordsTab.Pending to emptyList(),
            CachedRecordsTab.Failed to emptyList(),
        )

    init {
        viewModelScope.launch {
            combine(
                repository.observePending(),
                repository.observeFailed(),
            ) { pending, failed -> pending to failed }
                .collect { (pending, failed) ->
                    latestByTab[CachedRecordsTab.Pending] = pending
                    latestByTab[CachedRecordsTab.Failed] = failed
                    _uiState.update { state ->
                        val forTab = when (state.selectedTab) {
                            CachedRecordsTab.Pending -> pending
                            CachedRecordsTab.Failed -> failed
                        }
                        state.copy(
                            pendingCount = pending.size,
                            failedCount = failed.size,
                            visibleEntries = forTab.applyFilters(
                                state.searchQuery,
                                state.recordTypeFilters,
                            ),
                            isLoading = false,
                        )
                    }
                }
        }
    }

    fun onTabSelected(tab: CachedRecordsTab) {
        // Tab switch pulls the freshest snapshot from the cache; filter
        // state (search + chips) intentionally survives — the officer's
        // mental model is "show me my captures", not "reset when I
        // switch views".
        _uiState.update { state ->
            state.copy(
                selectedTab = tab,
                visibleEntries = latestByTab.getValue(tab).applyFilters(
                    state.searchQuery,
                    state.recordTypeFilters,
                ),
            )
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                visibleEntries = latestByTab.getValue(state.selectedTab).applyFilters(
                    query,
                    state.recordTypeFilters,
                ),
            )
        }
    }

    fun onRecordTypeFilterToggled(type: RecordType) {
        _uiState.update { state ->
            val next = state.recordTypeFilters.toMutableSet().apply {
                if (contains(type)) remove(type) else add(type)
            }
            state.copy(
                recordTypeFilters = next,
                visibleEntries = latestByTab.getValue(state.selectedTab).applyFilters(
                    state.searchQuery,
                    next,
                ),
            )
        }
    }

    /**
     * Long-press copy of a row's sanitised error message so an officer
     * can share it with support verbatim. The message already passed
     * through `core.network.UserFacingError` — no hostname/URL leak.
     */
    fun onCopyError(entry: CachedRecordEntry) {
        val message = entry.syncError ?: return
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            ?: return
        clipboard.setPrimaryClip(ClipData.newPlainText("Sync error", message))
        _uiState.update { it.copy(copyToastMessage = "Error copied to clipboard") }
    }

    fun onCopyToastShown() {
        _uiState.update { it.copy(copyToastMessage = null) }
    }
}

/**
 * Case-insensitive AND across search + type filters. Empty filter set
 * means "any type", empty search means "any candidate".
 */
private fun List<CachedRecordEntry>.applyFilters(
    query: String,
    typeFilters: Set<RecordType>,
): List<CachedRecordEntry> {
    val trimmed = query.trim()
    return filter { entry ->
        val typeOk = typeFilters.isEmpty() || entry.recordType in typeFilters
        val searchOk = trimmed.isEmpty() ||
            entry.candidateName.contains(trimmed, ignoreCase = true) ||
            entry.examNumber.contains(trimmed, ignoreCase = true)
        typeOk && searchOk
    }
}
