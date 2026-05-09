package ng.com.chprbn.mobile.feature.verification.domain.model

/**
 * Domain entity: a single feature item in the Verification grid.
 * UI-agnostic; the presentation layer maps [FeatureType] to icons, titles,
 * subtitles, and click actions. The domain stays Android-free — no
 * `@StringRes` ids or copy strings live here.
 */
data class VerificationFeature(
    val id: FeatureType,
    val isPrimary: Boolean
)

enum class FeatureType {
    ScanQr,
    VerifiedList,
    Sync,
    Profile
}
