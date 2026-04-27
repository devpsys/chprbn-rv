package ng.com.chprbn.mobile.feature.dashboard.presentation

data class DashboardUiState(
    val isLoading: Boolean = false,
    val userName: String = "Officer Michael Chen",
    val userEmail: String = "m.chen@regulatory.gov",
    val userStatus: String = "Active Duty",
    val profileImageUrl: String? = null,
    val lastSyncTime: String = "Today, 09:45 AM"
)