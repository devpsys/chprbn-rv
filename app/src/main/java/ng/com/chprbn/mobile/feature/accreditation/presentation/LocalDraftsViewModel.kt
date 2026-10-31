package ng.com.chprbn.mobile.feature.accreditation.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ng.com.chprbn.mobile.feature.accreditation.domain.model.SyncDraftsResult
import ng.com.chprbn.mobile.feature.accreditation.domain.usecase.DiscardLocalDraftsUseCase
import ng.com.chprbn.mobile.feature.accreditation.domain.usecase.GetLocalDraftsUseCase
import ng.com.chprbn.mobile.feature.accreditation.domain.usecase.SyncAllAccreditationDraftsUseCase

@HiltViewModel
class LocalDraftsViewModel @Inject constructor(
    private val getLocalDrafts: GetLocalDraftsUseCase,
    private val discardLocalDrafts: DiscardLocalDraftsUseCase,
    private val syncAllDrafts: SyncAllAccreditationDraftsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LocalDraftsUiState(isLoading = true))
    val uiState: StateFlow<LocalDraftsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { reload() }
    }

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
            val result = syncAllDrafts()
            reload()
            _uiState.update { state ->
                state.copy(
                    isSyncing = false,
                    showSyncSuccess = result is SyncDraftsResult.Success,
                    lastSyncedCount = (result as? SyncDraftsResult.Success)?.syncedCount ?: 0,
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
        val selectedIds = _uiState.value.drafts.filter { it.isSelected }.map { it.id }
        viewModelScope.launch {
            discardLocalDrafts(selectedIds)
            reload()
            _uiState.update { it.copy(showDiscardConfirm = false) }
        }
    }

    private suspend fun reload() {
        val drafts = getLocalDrafts().map { it.toUi() }
        _uiState.update { state ->
            state.copy(
                isLoading = false,
                drafts = drafts,
                visibleDrafts = drafts.applyDraftFilters(state.searchQuery, state.selectedFilter),
            )
        }
    }
}
