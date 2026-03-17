package ng.com.chprbn.mobile.feature.sync.presentation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

enum class SyncHistoryFilter { All, Synced, Failed }

data class SyncHistoryItem(
    val id: String,
    val timestamp: String,
    val status: SyncStatus,
    val message: String
)

enum class SyncStatus { Success, Failed }

data class SyncHistoryUiState(
    val query: String = "",
    val filter: SyncHistoryFilter = SyncHistoryFilter.All,
    val items: List<SyncHistoryItem> = sampleSyncHistory
) {
    val filteredItems: List<SyncHistoryItem>
        get() {
            val base = when (filter) {
                SyncHistoryFilter.All -> items
                SyncHistoryFilter.Synced -> items.filter { it.status == SyncStatus.Success }
                SyncHistoryFilter.Failed -> items.filter { it.status == SyncStatus.Failed }
            }
            val q = query.trim()
            if (q.isEmpty()) return base
            val lower = q.lowercase()
            return base.filter { item ->
                item.id.lowercase().contains(lower) ||
                    item.timestamp.lowercase().contains(lower)
            }
        }
}

@HiltViewModel
class SyncHistoryViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(SyncHistoryUiState())
    val uiState: StateFlow<SyncHistoryUiState> = _uiState

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun onFilterSelected(filter: SyncHistoryFilter) {
        _uiState.update { it.copy(filter = filter) }
    }
}

// Static sample data mirroring sync_history design
val sampleSyncHistory = listOf(
    SyncHistoryItem(
        id = "Record #8831-C",
        timestamp = "10:45 AM, Oct 24",
        status = SyncStatus.Success,
        message = "Successfully uploaded"
    ),
    SyncHistoryItem(
        id = "Record #8830-B",
        timestamp = "09:12 AM, Oct 24",
        status = SyncStatus.Success,
        message = "Successfully uploaded"
    ),
    SyncHistoryItem(
        id = "Record #8829-A",
        timestamp = "08:30 AM, Oct 24",
        status = SyncStatus.Failed,
        message = "Network timeout"
    ),
    SyncHistoryItem(
        id = "Record #8815-F",
        timestamp = "04:20 PM, Oct 23",
        status = SyncStatus.Success,
        message = "Successfully uploaded"
    ),
    SyncHistoryItem(
        id = "Record #8812-D",
        timestamp = "11:05 AM, Oct 23",
        status = SyncStatus.Failed,
        message = "Invalid data format"
    )
)

