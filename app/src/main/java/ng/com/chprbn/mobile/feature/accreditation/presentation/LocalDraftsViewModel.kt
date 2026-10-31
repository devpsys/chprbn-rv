package ng.com.chprbn.mobile.feature.accreditation.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Presentation-only Local Drafts VM. Emits placeholder drafts until
 * Room + SyncBatchRunner accreditation outbox work lands.
 */
@HiltViewModel
class LocalDraftsViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(LocalDraftsUiState.placeholder())
    val uiState: StateFlow<LocalDraftsUiState> = _uiState.asStateFlow()

    fun onSearchQueryChanged(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                visibleDrafts = state.drafts.applyDraftFilters(query, state.selectedFilter),
            )
        }
    }

    fun onFilterSelected(filter: LocalDraftFilter) {
        _uiState.update { state ->
            state.copy(
                selectedFilter = filter,
                visibleDrafts = state.drafts.applyDraftFilters(state.searchQuery, filter),
            )
        }
    }

    fun onDraftSelectionToggled(draftId: String, selected: Boolean) {
        _uiState.update { state ->
            val drafts = state.drafts.map {
                if (it.id == draftId) it.copy(isSelected = selected) else it
            }
            state.copy(
                drafts = drafts,
                visibleDrafts = drafts.applyDraftFilters(state.searchQuery, state.selectedFilter),
            )
        }
    }

    fun onSyncAllDrafts() {
        if (_uiState.value.isSyncing) return
        _uiState.update { it.copy(isSyncing = true, showSyncSuccess = false) }
        viewModelScope.launch {
            delay(1_200)
            _uiState.update { state ->
                val drafts = state.drafts.map { draft ->
                    when (draft.syncState) {
                        LocalDraftSyncState.PendingSync,
                        LocalDraftSyncState.SyncFailed,
                        LocalDraftSyncState.Syncing,
                        -> draft.copy(
                            syncState = LocalDraftSyncState.Synced,
                            lastRawError = null,
                            attemptCount = 0,
                        )
                        LocalDraftSyncState.Synced -> draft
                    }
                }
                val syncedCount = drafts.count { it.syncState == LocalDraftSyncState.Synced }
                state.copy(
                    isSyncing = false,
                    drafts = drafts,
                    visibleDrafts = drafts.applyDraftFilters(state.searchQuery, state.selectedFilter),
                    showSyncSuccess = true,
                    lastSyncedCount = syncedCount,
                )
            }
        }
    }

    fun dismissSyncSuccess() {
        _uiState.update { it.copy(showSyncSuccess = false) }
    }

    fun onDiscardSelectedClicked() {
        if (_uiState.value.selectedCount <= 0) return
        _uiState.update { it.copy(showDiscardConfirm = true) }
    }

    fun dismissDiscardConfirm() {
        _uiState.update { it.copy(showDiscardConfirm = false) }
    }

    fun confirmDiscardSelected() {
        _uiState.update { state ->
            val drafts = state.drafts.filterNot { it.isSelected }
            state.copy(
                showDiscardConfirm = false,
                drafts = drafts,
                visibleDrafts = drafts.applyDraftFilters(state.searchQuery, state.selectedFilter),
            )
        }
    }
}
