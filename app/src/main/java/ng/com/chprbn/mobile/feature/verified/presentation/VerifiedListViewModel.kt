package ng.com.chprbn.mobile.feature.verified.presentation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import ng.com.chprbn.mobile.feature.verified.presentation.VerifiedPractitioner
import ng.com.chprbn.mobile.feature.verified.presentation.VerifiedStatus
import ng.com.chprbn.mobile.feature.verified.presentation.samplePractitioners
import javax.inject.Inject

enum class VerifiedFilter {
    All, Active, Expired, PendingSync
}

data class VerifiedListUiState(
    val selectedFilter: VerifiedFilter = VerifiedFilter.All,
    val practitioners: List<VerifiedPractitioner> = samplePractitioners,
    val query: String = ""
) {
    val filteredPractitioners: List<VerifiedPractitioner>
        get() {
            val byFilter = when (selectedFilter) {
                VerifiedFilter.All -> practitioners
                VerifiedFilter.Active -> practitioners.filter { it.status == VerifiedStatus.Active }
                VerifiedFilter.Expired -> practitioners.filter { it.status == VerifiedStatus.Expired }
                VerifiedFilter.PendingSync -> practitioners.filter { it.status == VerifiedStatus.Syncing }
            }
            val q = query.trim()
            if (q.isEmpty()) return byFilter
            val lower = q.lowercase()
            return byFilter.filter { p ->
                p.name.lowercase().contains(lower) ||
                    p.license.lowercase().contains(lower)
            }
        }
}

@HiltViewModel
class VerifiedListViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(VerifiedListUiState())
    val uiState: StateFlow<VerifiedListUiState> = _uiState

    fun onFilterSelected(filter: VerifiedFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
    }
}

