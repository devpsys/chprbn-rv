package ng.com.chprbn.mobile.feature.dashboard.domain.model

/**
 * Domain entity: a single feature item in the dashboard grid.
 * UI-agnostic; presentation layer maps [FeatureType] to icons and actions.
 */
data class DashboardFeature(
    val id: FeatureType,
    val title: String,
    val subtitle: String,
    val isPrimary: Boolean
)

enum class FeatureType {
    ScanQr,
    VerifiedList,
    Sync,
    Profile
}
