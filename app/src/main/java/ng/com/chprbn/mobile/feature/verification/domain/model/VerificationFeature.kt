package ng.com.chprbn.mobile.feature.verification.domain.model

/**
 * Domain entity: a single feature item in the Verification grid.
 * UI-agnostic; presentation layer maps [FeatureType] to icons and actions.
 */
data class VerificationFeature(
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
