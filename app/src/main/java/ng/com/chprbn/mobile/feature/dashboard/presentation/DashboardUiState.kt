package ng.com.chprbn.mobile.feature.dashboard.presentation

data class DashboardUiState(
    val isLoading: Boolean = false,
    val userName: String = "",
    val userEmail: String = "",
    val userStatus: String = "",
    val profileImageUrl: String? = null,
    val lastSyncTime: String = "",
    val roles: List<String> = emptyList()
)