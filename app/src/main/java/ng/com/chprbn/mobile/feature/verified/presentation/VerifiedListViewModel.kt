package ng.com.chprbn.mobile.feature.verified.presentation

import androidx.lifecycle.viewModelScope
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.update
import ng.com.chprbn.mobile.feature.verified.presentation.VerifiedStatus
import ng.com.chprbn.mobile.feature.verified.domain.model.SyncStatus
import ng.com.chprbn.mobile.feature.verified.domain.usecase.GetVerifiedLicensesUseCase
import ng.com.chprbn.mobile.feature.verified.domain.model.VerifiedLicense
import javax.inject.Inject

enum class VerifiedFilter {
    All, Active, Expired, PendingSync
}

data class VerifiedListUiState(
    val selectedFilter: VerifiedFilter = VerifiedFilter.All,
    val practitioners: List<VerifiedPractitioner> = emptyList(),
    val query: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val filteredPractitioners: List<VerifiedPractitioner>
        get() {
            val byFilter = when (selectedFilter) {
                VerifiedFilter.All -> practitioners
                VerifiedFilter.Active -> practitioners.filter { it.status == VerifiedStatus.Active }
                VerifiedFilter.Expired -> practitioners.filter { it.status == VerifiedStatus.Expired }
                VerifiedFilter.PendingSync -> practitioners.filter {
                    it.syncStatus == VerifiedSyncStatus.Pending ||
                        it.syncStatus == VerifiedSyncStatus.Failed
                }
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
class VerifiedListViewModel @Inject constructor(
    private val getVerifiedLicensesUseCase: GetVerifiedLicensesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(VerifiedListUiState(isLoading = true))
    val uiState: StateFlow<VerifiedListUiState> = _uiState

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = runCatching { getVerifiedLicensesUseCase() }
            result.onSuccess { licenses ->
                _uiState.update {
                    it.copy(
                        practitioners = licenses.map { it.toVerifiedPractitioner() },
                        isLoading = false
                    )
                }
            }.onFailure { t ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = t.message ?: "Unable to load verified records."
                    )
                }
            }
        }
    }

    fun onFilterSelected(filter: VerifiedFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    private fun VerifiedLicense.toVerifiedPractitioner(): VerifiedPractitioner {
        val status =
            if (licenseStatus.equals(
                    "Active",
                    ignoreCase = true
                )
            ) VerifiedStatus.Active else VerifiedStatus.Expired
        val sync = when (syncStatus) {
            SyncStatus.Pending -> VerifiedSyncStatus.Pending
            SyncStatus.Synced -> VerifiedSyncStatus.Synced
            SyncStatus.Failed -> VerifiedSyncStatus.Failed
        }

        val expiryText = "• Exp: ${expiryDate}"
        val verifiedAtText = SimpleDateFormat("MMM d, yyyy hh:mm a", Locale.getDefault())
            .format(Date(verifiedAt))

        return VerifiedPractitioner(
            name = fullName,
            license = registrationNumber,
            status = status,
            expiryText = expiryText,
            verifiedAtText = verifiedAtText,
            syncStatus = sync,
            photoUrl = photoUrl
        )
    }
}

